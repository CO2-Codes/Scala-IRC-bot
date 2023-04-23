package codes.co2.ircbot.listeners.links

import codes.co2.ircbot.config.{GeneralConfig, LinkListenerConfig}
import codes.co2.ircbot.http.{FxTwitterClient, HttpClient, TitleParser}
import codes.co2.ircbot.listeners.GenericListener
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import org.pircbotx.hooks.events.{ActionEvent, MessageEvent}
import org.pircbotx.hooks.types.GenericMessageEvent
import org.pircbotx.{Channel, Colors}
import org.slf4j.{Logger, LoggerFactory}
import cats.effect.unsafe.implicits.global

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*
import scala.util.control.NonFatal

class LinkListener(
  httpClient: HttpClient,
  fxTwitterClient: FxTwitterClient,
  config: LinkListenerConfig,
  generalConfig: GeneralConfig,
)(implicit ec: ExecutionContext) extends GenericListener(generalConfig) {
  val log: Logger = LoggerFactory.getLogger(getClass)

  case class YoutubeClient(client: YouTube, key: String)

  val youtubeClientOpt: Option[YoutubeClient] = {
    config.youtubeApiKey.map { youtubeKey =>
      log.info("Starting youtube client.")

      val httpTransport = GoogleNetHttpTransport.newTrustedTransport
      YoutubeClient(new YouTube.Builder(httpTransport, GsonFactory.getDefaultInstance, null).build, youtubeKey)

    }
  }

  private val (boldTag, normalTag) = if (config.boldTitles.getOrElse(false)) (Colors.BOLD, Colors.NORMAL) else ("", "")

  override def onAcceptedUserMsg(event: MessageEvent): Unit = {
    onMessageAndAction(event, event.getChannel)
  }

  override def onAcceptedUserAction(event: ActionEvent): Unit = {
    if (event.getChannel != null) {
      onMessageAndAction(event, event.getChannel)
    }
  }

  private def onMessageAndAction(event: GenericMessageEvent, channel: Channel): Unit = {
    val eventMessage = event.getMessage
    val lowerCase = eventMessage.toLowerCase()

    def send(title: String): Unit = {
      log.info(s"Sending $title to ${channel.getName}")

      val lowerCaseTitle = title.toLowerCase()

      if (config.lowerCaseSpamList.exists(spamWord => lowerCaseTitle.contains(spamWord))) {
        channel.send().message("Are you a spammer?")
      } else {
        channel.send().message(s"$boldTag$title$normalTag")
      }

    }

    if (lowerCase.contains("http://") || lowerCase.contains("https://")) {
      LinkParser.findLink(eventMessage).foreach {
        link =>
          getAsTweetOpt(link).map { fut =>
            fut.recover {
              case NonFatal(ex) =>
                log.error(s"Twitter future failed: $ex")
                throw ex
            }
              .map(text => send(TitleParser.sanitizeToIrcMessage(text.getOrElse("[Error parsing twitter link]"))))
          }.orElse {
            getAsYoutubeOpt(link).map { fut =>
              fut.recover {
                case NonFatal(ex) =>
                  log.error(s"Youtube future failed: $ex")
                  throw ex

              }.map(text => send(TitleParser.sanitizeToIrcMessage(text.getOrElse("[Video Not Found]"))))
            }
          }.getOrElse (
            httpClient.getTitle(link).map(_.foreach(text => send(TitleParser.sanitizeToIrcMessage(text)))).unsafeRunAsync(_ => ())
          ) // Fallback to normal title parsing
      }

    }
  }

  /*
  Outer option will be None if it's not a twitter status link. The inner option will be
  None only if there's something wrong with fxtwitter.
   */
  private def getAsTweetOpt(link: String): Option[Future[Option[String]]] = {

    val fxTwitterUrlOpt = LinkParser.convertTwitterStatusUrlToFxtwitter(link)

    fxTwitterUrlOpt.map(fxTwitterClient.getResult(_).unsafeToFuture())

  }

  /*
  Outer option will be None if it's not a youtube link or the youtube client is turned off, the inner option will be
  None only if the video is not found.
   */
  private def getAsYoutubeOpt(link: String): Option[Future[Option[String]]] = {
    for {

      youtubeClient <-
        youtubeClientOpt // This order because don't even bother the regex if the youtubeClient doesn't exist
      youtubeId <- LinkParser.tryGetYoutubeId(link)
      request = youtubeClient.client.videos().list(List("snippet").asJava)
      response = Future(request.setId(List(youtubeId).asJava).setKey(youtubeClient.key).execute())
      title = response.map(resp => resp.getItems.asScala.headOption.map(video => video.getSnippet.getTitle))
    } yield title
  }

}

package codes.co2.ircbot.listeners.links

import codes.co2.ircbot.config.{GeneralConfig, LinkListenerConfig}
import codes.co2.ircbot.http.{HttpClient, TitleParser}
import codes.co2.ircbot.listeners.GenericListener
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.enums.TweetMode
import com.danielasfregola.twitter4s.exceptions.TwitterException
import org.pircbotx.hooks.events.{ActionEvent, MessageEvent}
import org.pircbotx.hooks.types.GenericMessageEvent
import org.pircbotx.{Channel, Colors}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class LinkListener(httpClient: HttpClient, config: LinkListenerConfig, generalConfig: GeneralConfig)(implicit
  ec: ExecutionContext
) extends GenericListener(generalConfig) {
  val log: Logger = LoggerFactory.getLogger(getClass)

  val twitterClientOpt: Option[TwitterRestClient] =
    config.twitterApi.map { twitterApi =>
      log.info("Starting twitter client.")
      TwitterRestClient(twitterApi.consumerToken, twitterApi.accessToken)
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
      channel.send().message(s"$boldTag$title$normalTag")
    }

    if (lowerCase.contains("http://") || lowerCase.contains("https://")) {
      LinkParser.findLink(eventMessage).foreach {
        link =>
          val tweetOption: Option[Future[String]] = for {

            twitterClient <- twitterClientOpt // This order because don't even bother the regex if the twitterClient doesn't exist
            tweetId <- LinkParser.tryGetTwitterId(link)
            tweet = twitterClient.getTweet(
              tweetId,
              trim_user = false,
              include_my_retweet = false,
              include_entities = false,
              tweet_mode = TweetMode.Extended,
            )
            message = tweet.map(data => s"${data.data.text} - ${data.data.user.map(user => user.name).getOrElse("")}")
          } yield message

          tweetOption.map { fut =>
            fut.recover {
              case TwitterException(code, _) if code.intValue() == 404 => "404 Not Found"
              case NonFatal(ex) =>
                log.error(s"Twitter future failed: $ex")
                throw ex

            }.map(text => send(TitleParser.sanitizeToIrcMessage(text)))
          }.getOrElse(httpClient.getTitle(link).map(_.foreach(text => send(TitleParser.sanitizeToIrcMessage(text))))) // Fallback to normal title parsing

      }
    }
  }
}

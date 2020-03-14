package codes.co2.ircbot.listeners.links

import codes.co2.ircbot.config.{GeneralConfig, LinkListenerConfig}
import codes.co2.ircbot.http.HttpClient
import codes.co2.ircbot.listeners.GenericListener
import org.pircbotx.hooks.events.{ActionEvent, MessageEvent}
import org.pircbotx.hooks.types.GenericMessageEvent
import org.pircbotx.{Channel, Colors}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

class LinkListener(httpClient: HttpClient, config: LinkListenerConfig, generalConfig: GeneralConfig)(implicit ec: ExecutionContext) extends GenericListener(generalConfig) {
  val log: Logger = LoggerFactory.getLogger(getClass)


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

    if (lowerCase.contains("http://") || lowerCase.contains("https://")) {
      LinkParser.findLink(eventMessage).map(httpClient.getTitle).map(_.map(_.foreach(title => {
        log.info(s"Sending $title to ${channel.getName}")
        channel.send().message(s"$boldTag$title$normalTag")
      })))

    }
    ()

  }
}

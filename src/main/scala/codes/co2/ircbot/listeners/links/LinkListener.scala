package codes.co2.ircbot.listeners.links

import codes.co2.ircbot.config.LinkListenerConfig
import codes.co2.ircbot.http.HttpClient
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.{ActionEvent, MessageEvent}
import org.pircbotx.hooks.types.GenericMessageEvent
import org.pircbotx.{Channel, Colors}

import scala.concurrent.ExecutionContext

class LinkListener(httpClient: HttpClient, config: LinkListenerConfig)(implicit ec: ExecutionContext) extends ListenerAdapter {

  private val (boldTag, normalTag) = if (config.boldTitles.getOrElse(false)) (Colors.BOLD, Colors.NORMAL) else ("", "")

  override def onMessage(event: MessageEvent): Unit = {
    onMessageAndAction(event, event.getChannel)
  }

  override def onAction(event: ActionEvent): Unit = {
    if (event.getChannel != null) {
      onMessageAndAction(event, event.getChannel)
    }
  }

  private def onMessageAndAction(event: GenericMessageEvent, channel: Channel): Unit = {
    val eventMessage = event.getMessage
    val lowerCase = eventMessage.toLowerCase()

    if (lowerCase.contains("http://") || lowerCase.contains("https://")) {
      LinkParser.findLink(eventMessage).map(httpClient.getTitle).map(_.map(_.foreach(title => channel.send()
        .message(s"$boldTag$title$normalTag"))))

    }
    ()

  }
}

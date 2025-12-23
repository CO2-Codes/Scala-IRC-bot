package codes.co2.ircbot.listeners.links

import cats.effect.unsafe.implicits.global
import codes.co2.ircbot.config.GeneralConfig
import codes.co2.ircbot.http.WaybackAPIClient
import codes.co2.ircbot.listeners.GenericListener
import org.pircbotx.hooks.events.{MessageEvent, PrivateMessageEvent}
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.{Logger, LoggerFactory}

class LinkReplaceListener(waybackAPIClient: WaybackAPIClient, generalConfig: GeneralConfig) extends GenericListener(generalConfig) {
  val log: Logger = LoggerFactory.getLogger(getClass)

  override def onAcceptedUserPrivateMsg(event: PrivateMessageEvent): Unit = {
    if (event.getMessage.startsWith("!wayback")) {
      onMessageOrPm(event)
    }
  }

  override def onAcceptedUserMsg(event: MessageEvent): Unit = {
    if (event.getMessage.startsWith("!wayback")) {
      onMessageOrPm(event)
    }
  }

  private def onMessageOrPm(event: GenericMessageEvent): Unit = {
    val eventMessage = event.getMessage.drop(8) // Remove !wayback command
    val lowerCase = eventMessage.toLowerCase()

    if (lowerCase.contains("http://") || lowerCase.contains("https://")) {
      LinkParser.findLink(eventMessage).foreach {
        link =>
          waybackAPIClient.getResult(link).map {
            case WaybackAPIClient.Success(waybackUrl) =>
              // We can respond with a bucket replacement syntax with commas, if there's no commas in the url or replacement
              val bucketEditOpt = if (!waybackUrl.contains(',') && !link.contains(',')) {
                // If the first word is not the link, we assume the user sent the full factoid so we can make a full replacement syntax
                if (!lowerCase.trim.startsWith("http://") && !lowerCase.trim.startsWith("https://")) {
                  val firstWord = eventMessage.trim.takeWhile(char => char != ' ')
                  Some(s"Bucket: $firstWord =~ s,$link,$waybackUrl,")

                } else {
                  Some(s"Bucket: <INSERT_FACTOID_NAME> =~ s,$link,$waybackUrl,")
                }
              } else None

              bucketEditOpt match {
                case None =>
                  val message1ToSend = s"Archived URL found: $waybackUrl"
                  log.info(s"Sending $message1ToSend for archive url request $eventMessage")
                  event.respond(message1ToSend)
                case Some(bucketEditStr) =>
                  val message1ToSend = s"Archived URL found: $waybackUrl. Here is how to update it in bucket:"
                  log.info(s"Sending $message1ToSend for archive url request $eventMessage")
                  event.respond(message1ToSend)
                  log.info(s"Sending $bucketEditStr for archive url request $eventMessage")
                  event.respondWith(bucketEditStr)

              }

            case WaybackAPIClient.NotFound =>
              val messageToSend = "Sorry, no archived url found. :( This may happen sometimes if the Wayback API is unavailable, or the link might really not have been archived."
              log.info(s"Sending $messageToSend for archive url request $eventMessage")
              event.respond(messageToSend)
            case WaybackAPIClient.Error =>
              val messageToSend = "Sorry, there was an error calling the Wayback API. Please report this to your friendly bot maintainer."
              log.info(s"Sending $messageToSend for archive url request $eventMessage")
              event.respond(messageToSend)

          }.unsafeRunAsync(_ => ())

      }
    }
  }

}

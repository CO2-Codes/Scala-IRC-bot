package codes.co2.ircbot.listeners.pronouns

import java.io.File
import codes.co2.ircbot.config.{GeneralConfig, PronounListenerConfig}
import codes.co2.ircbot.listeners.GenericListener
import codes.co2.ircbot.listeners.GenericListener.*
import codes.co2.ircbot.listeners.pronouns.PronounListener.*
import codes.co2.ircbot.pronouns
import codes.co2.ircbot.pronouns.PronounsHandler
import codes.co2.ircbot.pronouns.PronounsHandler.Pronoun
import org.pircbotx.hooks.WaitForQueue
import org.pircbotx.hooks.events.{MessageEvent, PrivateMessageEvent, WhoisEvent}
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec
import scala.concurrent.duration.*
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

class PronounListener(config: PronounListenerConfig, generalConfig: GeneralConfig)(implicit ec: ExecutionContext) extends GenericListener(generalConfig) {

  private val pronounsHandler: PronounsHandler = new pronouns.PronounsHandler(new File(config.filePath))

  override def onAcceptedUserPrivateMsg(event: PrivateMessageEvent): Unit = {
    onAcceptedMsg(event)
  }

  override def onAcceptedUserMsg(event: MessageEvent): Unit = {
    onAcceptedMsg(event)
  }

  private def onAcceptedMsg(event: GenericMessageEvent): Unit = {
    if (event.getMessage.startsWith("!pronouns-admin")) {
      handleAdminEvent(event: GenericMessageEvent)
    } else if (event.getMessage.startsWith("!pronouns")) {
      val msg = event.getMessage

      val splitByWords = msg.split(" ")

      splitByWords.head match {
          /* This first case uses getNick instead of getNickservNick because for a get, verifying it's the right person
          is not worth an extra /whois */
        case "!pronouns" if splitByWords.length == 1 => pronounsHandler.get(event.getUser.getNick, event)


        case "!pronouns" => pronounsHandler.get(splitByWords(1), event)

        case "!pronouns-add" if splitByWords.length > 1 =>
          getPronoun(splitByWords(1), event)
            .foreach(pronoun => getNickservNick(event)
              .foreach(nick => pronounsHandler.add(nick, pronoun, event)))

        case "!pronouns-remove" if splitByWords.length > 1 =>
          getPronoun(splitByWords(1), event)
            .foreach(pronoun => getNickservNick(event)
              .foreach(nick => pronounsHandler.remove(nick, pronoun, event)))

        case "!pronouns-forget" =>
          getNickservNick(event).foreach(nick => pronounsHandler.forget(nick, event))

      }

    }
  }

  private def handleAdminEvent(event: GenericMessageEvent): Unit = {
    if (admins.contains(event.getUser.getNick) && event.getUser.isVerified) {
      val msg = event.getMessage

      val splitByWords = msg.split(" ")

      splitByWords.head match {
        case "!pronouns-admin-add" if splitByWords.length > 2 =>
          getPronoun(splitByWords(1), event).foreach(pronoun => pronounsHandler.add(splitByWords(2), pronoun, event))

        case "!pronouns-admin-remove" if splitByWords.length > 2 =>
          getPronoun(splitByWords(1), event).foreach(pronoun => pronounsHandler.remove(splitByWords(2), pronoun, event))

        case "!pronouns-admin-forget" if splitByWords.length > 1 =>
          pronounsHandler.forget(splitByWords(1), event)
      }
    }

  }

  private def getPronoun(pronoun: String, event: GenericMessageEvent): Option[Pronoun] = {
    Pronoun.fromString(pronoun).orElse {
      event.respondWith(s"Unsupported pronoun. Use one of: ${Pronoun.validPronouns}")
      None
    }
  }
}

object PronounListener {

  val log: Logger = LoggerFactory.getLogger(getClass)

  private def getNickservNick(event: GenericMessageEvent)(implicit ec: ExecutionContext): Option[String] = {

    event.getUser.send.whoisDetail()

    Try {

      val registeredNickname = Await.result(Future(tailRecWaitForWhoisResponse(new WaitForQueue(getBot(event)), event.getUser.getNick)), 10.seconds)
      if (registeredNickname.isEmpty) {
        event.respond("You are not logged in to NickServ. For security reasons, you cannot edit your pronouns.")
      }

      registeredNickname

    }.recover {
      case NonFatal(ex) =>
        log.warn("Got a timeout on waiting for whois", ex)
        None

    }.get

  }

  @tailrec
  private def tailRecWaitForWhoisResponse(waitForQueue: WaitForQueue, nick: String): Option[String] = {

    val whoisEvent = waitForQueue.waitFor(classOf[WhoisEvent])

    if (whoisEvent.getNick == nick) {
      //Got our event
      waitForQueue.close()
      Option(whoisEvent.getRegisteredAs) // can be null, so wrap in an option
        // Handle the case where the server does not explicitly send the nickserv nick. Nickserv instances that have this
        // normally do not support staying logged in when changing nicks, so using the regular nick is okay in that case.
        .map(nickServNick => if (nickServNick.isEmpty) whoisEvent.getNick else nickServNick)
    } else {
      tailRecWaitForWhoisResponse(waitForQueue, nick)
    }
  }

}

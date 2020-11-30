package codes.co2.ircbot.listeners.administration

import akka.actor.ActorSystem
import codes.co2.ircbot.config.{AdminListenerConfig, GeneralConfig}
import codes.co2.ircbot.listeners.GenericListener
import codes.co2.ircbot.listeners.GenericListener._
import org.pircbotx.PircBotX
import org.pircbotx.hooks.events.{ConnectEvent, ExceptionEvent, ListenerExceptionEvent, MessageEvent, PrivateMessageEvent}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future

class AdminListener(config: AdminListenerConfig, generalConfig: GeneralConfig)(implicit actorSystem: ActorSystem) extends GenericListener(generalConfig) {
  val log: Logger = LoggerFactory.getLogger(getClass)

  private val puppetMasters = config.puppetMasters.getOrElse(Seq.empty)

  override def onConnect(event: ConnectEvent): Unit = {
    // Set IRC server bot user mode
    getBot(event).sendIRC().mode(getBot(event).getNick, "+B")
  }

  override def onAcceptedUserPrivateMsg(event: PrivateMessageEvent): Unit = {
    if (event.getMessage.startsWith("!")) {
      event.getMessage match {
        case "!help" => event.respondWith(config.helpText)
        case "!quit" if admins.contains(event.getUser.getNick) && event.getUser.isVerified =>
          shutdown(getBot(event))

        case msg if puppetMasters.contains(event.getUser.getNick) && event.getUser.isVerified &&
          (msg.startsWith("!say") || msg.startsWith("!act")) =>
          val command = msg.split(" ", 3)
          command.headOption match {
            case Some("!say") if command.sizeIs >= 2 =>
              getBot(event).sendIRC().message(command(1), command(2))
            case Some("!act") if command.sizeIs >= 2 =>
              getBot(event).sendIRC().action(command(1), command(2))
            case _ => ()
          }
        case _ => ()
      }
    }
  }

  override def onAcceptedUserMsg(event: MessageEvent): Unit = {
    if (event.getMessage.startsWith("!")) {
      event.getMessage match {
        case "!help" => event.respondWith(config.helpText)
        case "!quit" if admins.contains(event.getUser.getNick) && event.getUser.isVerified =>
          shutdown(getBot(event))
        case _ => ()
      }
    } else if (event.getMessage.equalsIgnoreCase("botsnack")) {
      event.getChannel.send().message(":D")
    }
  }

  // As it turns out the bot swallows exceptions and instead of logging them, expects you to do this using this listener
  // method.
  override def onException(event: ExceptionEvent): Unit = {
    event match {
      case lExEvent: ListenerExceptionEvent =>
        log.error(s"Got an exception from ${lExEvent.getListener} on event ${lExEvent.getSourceEvent}, exception: ${lExEvent.getException}")
      case _ => log.error(s"Got a bot exception event, exception: ${event.getException}, message: ${event.getMessage}")
    }
  }

  private def shutdown(bot: PircBotX): Unit = {
    bot.stopBotReconnect()
    bot.sendIRC().quitServer("I don't wanna go...")
    Future{
      Thread.sleep(100)
      java.lang.Runtime.getRuntime.halt(0)
    }(actorSystem.dispatcher)

    ()
  }

}

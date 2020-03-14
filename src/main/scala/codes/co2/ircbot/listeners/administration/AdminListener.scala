package codes.co2.ircbot.listeners.administration

import akka.actor.ActorSystem
import codes.co2.ircbot.config.{AdminListenerConfig, GeneralConfig}
import codes.co2.ircbot.listeners.GenericListener
import codes.co2.ircbot.listeners.GenericListener._
import org.pircbotx.PircBotX
import org.pircbotx.hooks.events.{ConnectEvent, MessageEvent, PrivateMessageEvent}

class AdminListener(config: AdminListenerConfig, generalConfig: GeneralConfig)(implicit actorSystem: ActorSystem) extends GenericListener(generalConfig) {

  private val puppetMasters = config.puppetMasters.getOrElse(Seq.empty)

  override def onConnect(event: ConnectEvent): Unit = {
    // Set IRC server bot user mode
    getBot(event).send().mode(getBot(event).getNick, "+B")
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
              getBot(event).send().message(command(1), command(2))
            case Some("!act") if command.sizeIs >= 2 =>
              getBot(event).send().action(command(1), command(2))
          }
      }
    }
  }

  override def onAcceptedUserMsg(event: MessageEvent): Unit = {
    if (event.getMessage.startsWith("!")) {
      event.getMessage match {
        case "!help" => event.respondWith(config.helpText)
        case "!quit" if admins.contains(event.getUser.getNick) && event.getUser.isVerified =>
          shutdown(getBot(event))
      }
    } else if (event.getMessage.equalsIgnoreCase("botsnack")) {
      event.getChannel.send().message(":D")
    }
  }

  private def shutdown(bot: PircBotX): Unit = {
    bot.stopBotReconnect()
    bot.sendIRC().quitServer("I don't wanna go...")
    actorSystem.terminate()
    ()

  }

}

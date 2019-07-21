package codes.co2.ircbot.listeners.administration

import akka.actor.ActorSystem
import codes.co2.ircbot.config.AdminListenerConfig
import codes.co2.ircbot.listeners.GenericListener
import org.pircbotx.PircBotX
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.events.{ConnectEvent, MessageEvent, PrivateMessageEvent}

class AdminListener(config: AdminListenerConfig, nicksToIgnore: Seq[String], actorSystem: ActorSystem) extends GenericListener(nicksToIgnore) {

  // Scala doesn't seem to like Java's Generics very much...
  private def getBot(event: Event): PircBotX = {
    event.getBot[PircBotX]
  }

  override def onConnect(event: ConnectEvent): Unit = {
    // Set IRC server bot user mode
    getBot(event).send().mode(getBot(event).getNick, "+B")
  }

  override def onAcceptedUserPrivateMsg(event: PrivateMessageEvent): Unit = {
    event.getMessage match {
      case "!quit" if config.botAdmins.contains(event.getUser.getNick) && event.getUser.isVerified =>
        shutdown(getBot(event))

      case msg if config.puppetMasters.contains(event.getUser.getNick) && event.getUser.isVerified &&
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

  override def onAcceptedUserMsg(event: MessageEvent): Unit = {
    event.getMessage match {
      case string if string.equalsIgnoreCase("botsnack") => event.getChannel.send().message(":D")
      case "!help" => event.getChannel.send().message(config.helpText)
      case "!quit" if config.botAdmins.contains(event.getUser.getNick) && event.getUser.isVerified =>
        shutdown(getBot(event))
    }
  }

  private def shutdown(bot: PircBotX): Unit = {
    bot.stopBotReconnect()
    bot.sendIRC().quitServer("I don't wanna go...")
    actorSystem.terminate()
    ()

  }

}

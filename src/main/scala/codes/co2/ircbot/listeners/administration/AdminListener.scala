package codes.co2.ircbot.listeners.administration

import codes.co2.ircbot.config.AdminListenerConfig
import org.pircbotx.PircBotX
import org.pircbotx.hooks.events.{ConnectEvent, MessageEvent, PrivateMessageEvent}
import org.pircbotx.hooks.{Event, ListenerAdapter}

class AdminListener(config: AdminListenerConfig) extends ListenerAdapter {

  // Scala doesn't seem to like Java's Generics very much...
  private def getBot(event: Event): PircBotX = {
    event.getBot[PircBotX]
  }

  override def onConnect(event: ConnectEvent): Unit = {
    // Set IRC server bot user mode
    getBot(event).send().mode(getBot(event).getNick, "+B")
  }

  override def onPrivateMessage(event: PrivateMessageEvent): Unit = {
    event.getMessage match {
      case "!quit" if config.botAdmins.contains(event.getUser.getNick) && event.getUser.isVerified =>
        shutdown(getBot(event))
    }
  }

  override def onMessage(event: MessageEvent): Unit = {
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
    System.exit(0)
  }

}

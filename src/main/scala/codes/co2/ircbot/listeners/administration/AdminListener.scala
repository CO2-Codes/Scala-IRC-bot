package codes.co2.ircbot.listeners.administration

import org.pircbotx.PircBotX
import org.pircbotx.hooks.events.{ConnectEvent, MessageEvent}
import org.pircbotx.hooks.{Event, ListenerAdapter}

class AdminListener(botAdmins: Seq[String], helpText: String) extends ListenerAdapter {

  // Scala doesn't seem to like Java's Generics very much...
  private def getBot(event: Event): PircBotX = {
    event.getBot[PircBotX]
  }

  override def onConnect(event: ConnectEvent): Unit = {
    // Set IRC server bot user mode
    getBot(event).send().mode(getBot(event).getNick, "+B")
  }

  override def onMessage(event: MessageEvent): Unit = {
    event.getMessage match {
      case "!help" => event.getChannel.send().message(helpText)
      case "!quit" if botAdmins.contains(event.getUser.getNick) && event.getUser.isVerified => {
        getBot(event).stopBotReconnect()
        getBot(event).sendIRC().quitServer("I don't wanna go...")
        System.exit(0)
      }

    }
  }

}

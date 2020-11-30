package codes.co2.ircbot.listeners

import codes.co2.ircbot.config.GeneralConfig
import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.{ActionEvent, MessageEvent, PrivateMessageEvent}
import org.pircbotx.hooks.types.{GenericChannelUserEvent, GenericEvent}

import scala.annotation.nowarn

abstract class GenericListener(config: GeneralConfig) extends ListenerAdapter {
  private val channelsToIgnore = config.ignoredChannels
  private val nicksToIgnore = config.ignoredNicks
  protected val admins: Seq[String] = config.botAdmins

  private def sentInIgnoredChannel(event: GenericChannelUserEvent): Boolean = {
    // Wrap in an option because getChannel returns null for PM action events.
    Option(event.getChannel).exists(chan => channelsToIgnore.contains(chan.getName))
  }

  // Warnings on unused params expected here
  @nowarn def onAcceptedUserPrivateMsg(event: PrivateMessageEvent): Unit = ()
  @nowarn def onAcceptedUserAction(event: ActionEvent): Unit = ()
  @nowarn def onAcceptedUserMsg(event: MessageEvent): Unit = ()


  final override def onPrivateMessage(event: PrivateMessageEvent): Unit = {
    if (!nicksToIgnore.contains(event.getUser.getNick)) onAcceptedUserPrivateMsg(event)
  }

  final override def onAction(event: ActionEvent): Unit = {
    if (!nicksToIgnore.contains(event.getUser.getNick) && !sentInIgnoredChannel(event)) onAcceptedUserAction(event)
  }

  final override def onMessage(event: MessageEvent): Unit = {
    if (!nicksToIgnore.contains(event.getUser.getNick) && !sentInIgnoredChannel(event)) onAcceptedUserMsg(event)
  }
}

object GenericListener {

  // Scala doesn't seem to like Java's Generics very much...
  def getBot(event: GenericEvent): PircBotX = {
    event.getBot[PircBotX]
  }

}
package codes.co2.ircbot.listeners

import codes.co2.ircbot.config.ListenerConfig
import com.github.ghik.silencer.silent
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.{ActionEvent, MessageEvent, PrivateMessageEvent}
import org.pircbotx.hooks.types.GenericChannelUserEvent

abstract class GenericListener(config: ListenerConfig, nicksToIgnore: Seq[String]) extends ListenerAdapter {
  private val channelsToIgnore = config.ignoredChannels

  private def sentInIgnoredChannel(event: GenericChannelUserEvent): Boolean = {
    // Wrap in an option because getChannel returns null for PM action events.
    Option(event.getChannel).exists(chan => channelsToIgnore.contains(chan.getName))
  }

  // Warnings on unused params expected here
  @silent def onAcceptedUserPrivateMsg(event: PrivateMessageEvent): Unit = ()
  @silent def onAcceptedUserAction(event: ActionEvent): Unit = ()
  @silent def onAcceptedUserMsg(event: MessageEvent): Unit = ()


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

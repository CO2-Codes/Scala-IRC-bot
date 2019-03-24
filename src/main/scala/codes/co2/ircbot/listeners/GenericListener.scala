package codes.co2.ircbot.listeners

import com.github.ghik.silencer.silent
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.{ActionEvent, MessageEvent, PrivateMessageEvent}

abstract class GenericListener(nicksToIgnore: Seq[String]) extends ListenerAdapter {

  // Warnings on unused params expected here
  @silent def onAcceptedUserPrivateMsg(event: PrivateMessageEvent): Unit = ()
  @silent def onAcceptedUserAction(event: ActionEvent): Unit = ()
  @silent def onAcceptedUserMsg(event: MessageEvent): Unit = ()


  final override def onPrivateMessage(event: PrivateMessageEvent): Unit = {
    if (!nicksToIgnore.contains(event.getUser.getNick)) onAcceptedUserPrivateMsg(event)
  }

  final override def onAction(event: ActionEvent): Unit = {
    if (!nicksToIgnore.contains(event.getUser.getNick)) onAcceptedUserAction(event)
  }

  final override def onMessage(event: MessageEvent): Unit = {
    if (!nicksToIgnore.contains(event.getUser.getNick)) onAcceptedUserMsg(event)
  }
}

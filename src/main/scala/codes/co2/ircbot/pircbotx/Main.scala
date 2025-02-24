package codes.co2.ircbot.pircbotx

import java.nio.file.Paths
import codes.co2.ircbot.config.BotConfiguration
import org.pircbotx.delay.AdaptingDelay
import org.pircbotx.{Configuration, PircBotX}
import org.slf4j.{Logger, LoggerFactory}

import concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*
import java.nio.file.Path

object Main {
  
  /* The Scala3 top level main function syntax doesn't seem to allow for throwing custom errors, or at least not as
  easily as this syntax does.
   */

  def main(args: Array[String]): Unit = {
    val log: Logger = LoggerFactory.getLogger(getClass)

    implicit val ec: ExecutionContext = ExecutionContext.global

    if (args.sizeIs != 1) {
      log.error("Please have (only) the path to the .conf file as a command line argument")
      System.exit(1)
    }

    val path = Paths.get(args.head)

    val config = BotConfiguration.loadConfig(path)

    def pircConfiguration: Configuration = {
      val waitForNickservBeforeJoining = config.nickservDelay
        .map(nickServDelay => nickServDelay && config.nickservDelay.nonEmpty)
        .getOrElse(config.nickservDelay.nonEmpty)
      
      new Configuration.Builder()
        .addServer(config.connection.serverName, config.connection.port).setSocket(config.connection.ssl)
        .addAutoJoinChannels(config.channels.asJava)
        .setAutoReconnect(true).setAutoReconnectAttempts(5).setAutoReconnectDelay(new AdaptingDelay(1, 120000))
        .setAutoSplitMessage(false)
        .setFinger(config.fingerMsg)
        .setName(config.nickname)
        .setLogin(config.ident.getOrElse(config.nickname))
        .setRealName(config.realname.getOrElse(config.nickname))
        .setServerPassword(config.serverPassword.orNull)
        .setNickservPassword(config.nickservPassword.orNull).setNickservDelayJoin(waitForNickservBeforeJoining)
        .setAutoNickChange(true)
        .addListeners(config, path)
        .buildConfiguration()
    }

    val bot = new PircBotX(pircConfiguration)
    bot.startBot()

  }

}

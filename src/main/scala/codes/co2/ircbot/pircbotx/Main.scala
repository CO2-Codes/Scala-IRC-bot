package codes.co2.ircbot.pircbotx

import java.nio.file.Paths

import akka.actor.ActorSystem
import codes.co2.ircbot.config.BotConfiguration
import org.pircbotx.delay.AdaptingDelay
import org.pircbotx.{Configuration, PircBotX}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

object Main extends App {
  val log: Logger = LoggerFactory.getLogger(getClass)

  if (args.sizeIs != 1) {
    log.error("Please have (only) the path to the .conf file as a command line argument")
    System.exit(1)
  }

  val path = Paths.get(args.head)

  val config = BotConfiguration.loadConfig(path)

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  def pircConfiguration: Configuration = {
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
      .setNickservPassword(config.nickservPassword.orNull).setNickservDelayJoin(config.nickservPassword.nonEmpty)
      .setAutoNickChange(true)
      .addListeners(config, path)
      .buildConfiguration()
  }

  val bot = new PircBotX(pircConfiguration)
  bot.startBot()

}

package codes.co2.ircbot.pircbotx

import java.nio.file.Paths

import akka.actor.ActorSystem
import codes.co2.ircbot.config.BotConfiguration
import org.pircbotx.{Configuration, PircBotX}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

object Main extends App {
  val log: Logger = LoggerFactory.getLogger(getClass)

  if (args.length != 1) {
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
      .setAutoReconnect(true).setAutoReconnectAttempts(5).setAutoReconnectDelay(0)
      .setAutoSplitMessage(false)
      .setFinger(config.fingerMsg)
      .setLogin(config.nickname)
      .setName(config.nickname)
      .setRealName(config.realname.getOrElse(config.nickname))
      .setNickservPassword(config.nickservPassword.orNull).setNickservDelayJoin(config.nickservPassword.nonEmpty)
      .setAutoNickChange(true)
      .addListeners(config, path)
      .buildConfiguration()
  }

  val bot = new PircBotX(pircConfiguration)
  bot.startBot()
}

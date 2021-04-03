package codes.co2.ircbot.config

import java.nio.file.Path

import org.slf4j.{Logger, LoggerFactory}
import pureconfig.ConfigSource
import pureconfig.generic.auto._ // IntelliJ might see this as an unused import. IntelliJ is wrong.
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}

case class BotConfiguration(
                             connection: Connection,
                             serverPassword: Option[String],
                             nickname: String,
                             ident: Option[String],
                             realname: Option[String],
                             nickservPassword: Option[String],
                             channels: Seq[String],
                             fingerMsg: Option[String],
                             listeners: Seq[String],
                             generalConfig: GeneralConfig,
                           )

case class Connection(serverName: String, port: Int, ssl: Boolean)

case class GeneralConfig(
                          ignoreNicks: Option[Seq[String]],
                          ignoreChannels: Option[Seq[String]],
                          botAdmins: Seq[String],
                        ) {
  val ignoredChannels: Seq[String] = ignoreChannels.getOrElse(Seq.empty)
  val ignoredNicks: Seq[String] = ignoreNicks.getOrElse(Seq.empty)
}

case class TwitterApi(consumerToken: ConsumerToken, accessToken: AccessToken)

case class LinkListenerConfig(boldTitles: Option[Boolean], twitterApi: Option[TwitterApi], youtubeApiKey: Option[String])

case class AdminListenerConfig(helpText: String, puppetMasters: Option[Seq[String]])

case class PronounListenerConfig(filePath: String)

object BotConfiguration {
  val log: Logger = LoggerFactory.getLogger(getClass)

  def loadConfig(path: Path): BotConfiguration = ConfigSource.default(ConfigSource.file(path))
    .at("bot-configuration").load[BotConfiguration]
    .fold(failures => throw new Exception(failures.toString), success => success)

  def loadLinkListenerConfig(path: Path): LinkListenerConfig = ConfigSource.default(ConfigSource.file(path))
    .at("link-listener").load[LinkListenerConfig]
    .fold(failures => {
      log.info(s"Could not load link-listener config, reason ${failures.toList.map(_.description)} Using default config.")
      LinkListenerConfig(None, None, None)
    }, success => success)

  def loadAdminListenerConfig(path: Path): AdminListenerConfig = ConfigSource.default(ConfigSource.file(path))
    .at("admin-listener").load[AdminListenerConfig]
    .fold(failures => {
      log.info(s"Could not load admin-listener config, reason ${failures.toList.map(_.description)} Using default config.")
      AdminListenerConfig("", None)
    }, success => success)

    def loadPronounListenerConfig(path: Path): PronounListenerConfig = ConfigSource.default(ConfigSource.file(path))
      .at("pronoun-listener").load[PronounListenerConfig]
      .fold(failures => {
        log.info(s"Could not load pronoun-listener config, reason ${failures.toList.map(_.description)} Using default config.")
        PronounListenerConfig("pronouns.txt")
      }, success => success)

}

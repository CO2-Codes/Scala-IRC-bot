package codes.co2.ircbot.config

import codes.co2.ircbot.config
import org.slf4j.{Logger, LoggerFactory}
import pureconfig.{ConfigReader, ConfigSource, _}

import java.nio.file.Path

/* Pureconfig's new Scala 3 derivations with the ConfigReader is still in beta
but seems to work well enough for our usecase. Just make sure to add the ConfigReader
to every case class or it might not be able to parse them at runtime.
 */

case class BotConfiguration(
  connection: Connection,
  serverPassword: Option[String],
  nickname: String,
  ident: Option[String],
  realname: Option[String],
  nickservPassword: Option[String],
  nickservDelay: Option[Boolean],
  channels: Seq[String],
  fingerMsg: Option[String],
  listeners: Seq[String],
  generalConfig: GeneralConfig,
) derives ConfigReader

case class Connection(serverName: String, port: Int, ssl: Boolean) derives ConfigReader

case class GeneralConfig(
  ignoreNicks: Option[Seq[String]],
  ignoreChannels: Option[Seq[String]],
  botAdmins: Seq[String],
) derives ConfigReader {
  val ignoredChannels: Seq[String] = ignoreChannels.getOrElse(Seq.empty)
  val ignoredNicks: Seq[String] = ignoreNicks.getOrElse(Seq.empty)
}

case class LinkListenerConfig(
  boldTitles: Option[Boolean],
  youtubeApiKey: Option[String],
  spamList: Option[Seq[String]],
) derives ConfigReader {
  val lowerCaseSpamList: Seq[String] = spamList.map(_.map(_.toLowerCase)).getOrElse(Seq.empty)
}

case class AdminListenerConfig(helpText: String, puppetMasters: Option[Seq[String]]) derives ConfigReader

case class PronounListenerConfig(filePath: String) derives ConfigReader

object BotConfiguration {
  val log: Logger = LoggerFactory.getLogger(getClass)

  def loadConfig(path: Path): BotConfiguration = ConfigSource.default(ConfigSource.file(path))
    .at("bot-configuration").load[BotConfiguration]
    .fold(failures => throw new Exception(failures.toString), success => success)

  def loadLinkListenerConfig(path: Path): LinkListenerConfig = ConfigSource.default(ConfigSource.file(path))
    .at("link-listener").load[LinkListenerConfig]
    .fold(
      failures => {
        log.info(
          s"Could not load link-listener config, reason ${failures.toList.map(_.description)} Using default config."
        )
        LinkListenerConfig(None, None, None)
      },
      success => success,
    )

  def loadAdminListenerConfig(path: Path): AdminListenerConfig = ConfigSource.default(ConfigSource.file(path))
    .at("admin-listener").load[AdminListenerConfig]
    .fold(
      failures => {
        log.info(
          s"Could not load admin-listener config, reason ${failures.toList.map(_.description)} Using default config."
        )
        AdminListenerConfig("", None)
      },
      success => success,
    )

  def loadPronounListenerConfig(path: Path): PronounListenerConfig = ConfigSource.default(ConfigSource.file(path))
    .at("pronoun-listener").load[PronounListenerConfig]
    .fold(
      failures => {
        log.info(
          s"Could not load pronoun-listener config, reason ${failures.toList.map(_.description)} Using default config."
        )
        PronounListenerConfig("pronouns.txt")
      },
      success => success,
    )

}

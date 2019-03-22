package codes.co2.ircbot.config

import java.nio.file.Path

import pureconfig.generic.auto._ // IntelliJ might see this as an unused import. IntelliJ is wrong.

case class BotConfiguration(
                          connection: Connection,
                          nickname: String,
                          realname: Option[String],
                          nickservPassword: Option[String],
                          channels: Seq[String],
                          fingerMsg: Option[String],
                          botAdmins: Seq[String],
                          listeners: Seq[String],
                          helpText: String,
                        )

case class Connection(
                       serverName: String,
                       port: Int,
                       ssl: Boolean,
                     )

object BotConfiguration{
  def loadConfig(path: Path): BotConfiguration = pureconfig.loadConfig[BotConfiguration](path, "bot-configuration")
    .fold(failures => throw new Exception(failures.toString), success => success)

}

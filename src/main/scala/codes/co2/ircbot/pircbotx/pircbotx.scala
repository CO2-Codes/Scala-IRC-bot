package codes.co2.ircbot

import java.nio.file.Path
import codes.co2.ircbot.config.BotConfiguration
import codes.co2.ircbot.http.{FxTwitterClient, HttpClient, WaybackAPIClient}
import codes.co2.ircbot.listeners.administration.AdminListener
import codes.co2.ircbot.listeners.links.{LinkListener, LinkReplaceListener}
import codes.co2.ircbot.listeners.pronouns.PronounListener
import org.pircbotx.hooks.Listener
import org.pircbotx.{Configuration, UtilSSLSocketFactory}

import scala.jdk.CollectionConverters.*
import scala.concurrent.ExecutionContext

package object pircbotx {

  implicit class ConfigBuilderOps(builder: Configuration.Builder) {

    def setSocket(ssl: Boolean): Configuration.Builder = {
      if (ssl) {
        // Yes this is unsafe. But so many IRC servers have self-signed certs, things get very annoying otherwise
        builder.setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates())
      } else builder

    }

    def addListeners(config: BotConfiguration, configPath: Path)(implicit ec: ExecutionContext): Configuration.Builder = {

      val listeners: Seq[Listener] = config.listeners.map{
        case "adminListener" => new AdminListener(BotConfiguration.loadAdminListenerConfig(configPath), config.generalConfig)
        case "linkListener" => new LinkListener(new HttpClient, new FxTwitterClient, BotConfiguration.loadLinkListenerConfig(configPath), config.generalConfig)
        case "linkReplaceListener" => new LinkReplaceListener(new WaybackAPIClient, config.generalConfig)
        case "pronounListener" => new PronounListener(BotConfiguration.loadPronounListenerConfig(configPath), config.generalConfig)
        case other => throw new Exception(s"$other is not a valid listener type.")
      }

      builder.addListeners(listeners.asJava)
    }

    def setFinger(fingerString: Option[String]) : Configuration.Builder = {
      fingerString.map(builder.setFinger).getOrElse(builder)
    }

  }

}

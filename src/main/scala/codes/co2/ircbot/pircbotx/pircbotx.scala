package codes.co2.ircbot

import akka.actor.ActorSystem
import codes.co2.ircbot.config.BotConfiguration
import codes.co2.ircbot.http.HttpClient
import codes.co2.ircbot.listeners.administration.AdminListener
import codes.co2.ircbot.listeners.links.LinkListener
import org.pircbotx.hooks.Listener
import org.pircbotx.{Configuration, UtilSSLSocketFactory}
import scala.collection.JavaConverters._

import scala.concurrent.ExecutionContext

package object pircbotx {

  implicit class ConfigBuilderOps(builder: Configuration.Builder) {

    def setSocket(ssl: Boolean): Configuration.Builder = {
      if (ssl) {
        // Yes this is unsafe. But so many IRC servers have self-signed certs, things get very annoying otherwise
        builder.setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates())
      } else builder

    }

    def addListeners(config: BotConfiguration)(implicit ac: ActorSystem, ec: ExecutionContext): Configuration.Builder = {

      val listeners: Seq[Listener] = config.listeners.map{
        case "adminListener" => new AdminListener(config.botAdmins, config.helpText)
        case "linkListener" => new LinkListener(new HttpClient)
        case other => throw new Exception(s"$other is not a valid listener type.")
      }

      builder.addListeners(listeners.asJava)
    }

  }

}

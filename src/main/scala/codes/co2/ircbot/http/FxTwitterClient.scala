package codes.co2.ircbot.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import org.slf4j.{Logger, LoggerFactory}
import io.circe.generic.auto.*
import io.circe.parser.*

import scala.concurrent.{ExecutionContext, Future}
import FxTwitterClient._

class FxTwitterClient(implicit system: ActorSystem) {

  val log: Logger = LoggerFactory.getLogger(getClass)
  implicit val ec: ExecutionContext = system.dispatcher

  def getResult(url: String)(implicit s: ConnectionPoolSettings): Future[Option[String]] = {
    Http().singleRequest(HttpRequest(uri = url), settings = s)
      .flatMap(response => {
        val result = response.status match {
          case status if status.intValue == 401 =>
            Future.successful(Some("[Cannot load private tweet]"))
          case status if status.intValue == 404 =>
            Future.successful(Some("")) // To prevent triggering an error message

          case status
              if status.isSuccess() || response.headers.find(header => header.is("content-type"))
                .exists(header => header.value() == "application/json") =>
            log.info(s"Getting page from $url")
            parseJson(response).map(Some(_))

          case _ =>
            log.warn(s"Got an error from $url")
            Future.successful(None)
        }

        result.onComplete(_ => response.discardEntityBytes())

        result
      })
  }

  private def parseJson(response: HttpResponse): Future[String] = {

    // Temporary solution until I have time to get rid of Akka
    val bodyBytes =
      response.entity.dataBytes.runWith(Sink.fold(ByteString.empty)(_ ++ _)).map(byteString => byteString.utf8String)

    bodyBytes.map { body =>
      val tweet: Tweet = decode[FxTwitterResponse](body).toTry.get.tweet

      val mediaLink = tweet.media.flatMap{media =>
        media
          .mosaic.map(_.formats.webp)
          .orElse{ media.videos.flatMap(_.headOption.map(_.url)) }
          .orElse{ media.photos.flatMap(_.headOption.map(_.url)) }
          .orElse{ media.external.map(_.url)}
          .map(url => s"- [ $url ]")
      }.getOrElse("")

      s"${tweet.text} $mediaLink - ${tweet.author.name}"

    }

  }

}

object FxTwitterClient {
  private case class FxTwitterResponse(tweet: Tweet)

  private case class Tweet(text: String, author: Author, poll: Option[Unit], media: Option[Media])

  private case class Author(name: String)

  private case class Media(
    external: Option[MediaObject],
    photos: Option[Seq[MediaObject]],
    videos: Option[Seq[MediaObject]],
    mosaic: Option[Mosaic],
  )

  private case class MediaObject(url: String)

  private case class Mosaic(formats: MosaicFormats)

  private case class MosaicFormats(webp: String, jpeg: String)

}

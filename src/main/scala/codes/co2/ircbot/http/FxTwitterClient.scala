package codes.co2.ircbot.http

import org.slf4j.{Logger, LoggerFactory}
import io.circe.generic.auto.*
import io.circe.parser.*
import sttp.client3.*

import FxTwitterClient.*
import cats.effect.IO
import sttp.client3.httpclient.fs2.HttpClientFs2Backend
import sttp.client3.circe._

class FxTwitterClient {

  val log: Logger = LoggerFactory.getLogger(getClass)

  def getResult(url: String): IO[Option[String]] = {
    HttpClientFs2Backend.resource[IO]().use { backend =>
      basicRequest
        .get(uri"$url")
        .response(asJson[FxTwitterResponse])
        .send(backend)
        .map { response =>
          if (response.isSuccess && response.header("content-type").contains("application/json")) {
            log.info(s"Getting json from $url")

            response.body match {
              case Left(ex) =>
                log.error("Error parsing fxtwitter json", ex)
                Some("[Error parsing fxtwitter API result]")
              case Right(resp) => Some(fxResponseToMsg(resp))
            }

          } else if (response.code.code == 401) {
            Some("[Cannot load private tweet]")
          } else if (response.code.code == 404) {
            Some("") // To prevent triggering an error message
          } else {
            log.warn(s"Got an error from $url")
            None
          }
        }
    }
  }

  private def fxResponseToMsg(fxResponse: FxTwitterResponse): String = {

    val tweet = fxResponse.tweet

    val mediaLink = tweet.media.flatMap { media =>
      media
        .mosaic.map(_.formats.webp)
        .orElse { media.videos.flatMap(_.headOption.map(_.url)) }
        .orElse { media.photos.flatMap(_.headOption.map(_.url)) }
        .orElse { media.external.map(_.url) }
        .map(url => s"- [ $url ]")
    }.getOrElse("")

    s"${tweet.text} $mediaLink - ${tweet.author.name}"

  }

}

object FxTwitterClient {
  private case class FxTwitterResponse(tweet: Tweet)

  private case class Tweet(text: String, author: Author, media: Option[Media])

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

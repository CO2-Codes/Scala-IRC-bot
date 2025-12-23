package codes.co2.ircbot.http

import cats.effect.IO
import codes.co2.ircbot.http.WaybackAPIClient.*
import io.circe.generic.auto.*
import org.slf4j.{Logger, LoggerFactory}
import sttp.client3.*
import sttp.client3.circe.*
import sttp.client3.httpclient.fs2.HttpClientFs2Backend

class WaybackAPIClient {

  val log: Logger = LoggerFactory.getLogger(getClass)

  def getResult(originalUrl: String): IO[WaybackResult] = {
    HttpClientFs2Backend.resource[IO]().use { backend =>
      basicRequest
        .get(uri"https://archive.org/wayback/available?url=$originalUrl")
        .response(asJson[ArchiveResponse])
        .send(backend)
        .map { response =>
          if (response.isSuccess && response.header("content-type").exists(_.contains("application/json"))) {
            log.info(s"Getting archive data for $originalUrl")
            log.info(s"${response.body}")

            response.body match {
              case Left(ex) =>
                log.error("Error parsing wayback json", ex)
                Error
              case Right(resp) => archiveResponseToUrl(resp)
            }

          } else {
            log.warn(s"Got an error getting wayback response for $originalUrl: ${response.code.code} ${response.body}")
            Error
          }
        }
    }
  }

  private def archiveResponseToUrl(archiveResponse: ArchiveResponse): WaybackResult = {
    archiveResponse.archived_snapshots.closest.map(closest => Success(secureArchivedUrl(closest.url))).getOrElse(NotFound)
  }

  // This old API returns http URLS. We change the START of the url to https (as to not touch the internal archived url)
  private def secureArchivedUrl(url: String) = {
    if (url.startsWith("http://")) {
        s"https://${url.drop(7)}"
      } else {
        url
      }
  }

}

object WaybackAPIClient {
  private case class ArchiveResponse(archived_snapshots: ArchivedSnapshots)

  private case class ArchivedSnapshots(closest: Option[Closest])

  private case class Closest(url: String)

  sealed trait WaybackResult
  case class Success(url: String) extends WaybackResult
  case object NotFound extends WaybackResult
  case object Error extends WaybackResult

}

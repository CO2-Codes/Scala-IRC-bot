package codes.co2.ircbot.http

import cats.effect.IO
import fs2.text
import org.slf4j.{Logger, LoggerFactory}
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.*
import sttp.client3.httpclient.fs2.HttpClientFs2Backend

class HttpClient {

  val log: Logger = LoggerFactory.getLogger(getClass)

  def getTitle(url: String): IO[Option[String]] = {
    getPage(url).map(pageOpt =>
      pageOpt.flatMap(page => {
        val titleOpt = TitleParser.findTitle(page)
        log.info(s"Title of page $url: $titleOpt")
        titleOpt
      })
    )
  }

  private def getPage(url: String): IO[Option[String]] = {

    HttpClientFs2Backend.resource[IO]().use { backend =>
      basicRequest
        .get(uri"$url")
        .maxRedirects(1)
        // Only take the first 10K of the response. If the title isn't in there it's a very badly formatted webpage.
        .response(asStreamAlways(Fs2Streams[IO])(_.take(10000).through(text.utf8.decode).compile.foldMonoid))
        .send(backend)
        .map { response =>
          // Deal with situation where header has a charset set
          if (response.isSuccess && response.header("content-type").exists(str => str.contains("text/html"))) {
            Some(response.body)
          } else {
            log.info(s"Did not get a (correct) html page from $url")
            None
          }
        }
    }

  }

}

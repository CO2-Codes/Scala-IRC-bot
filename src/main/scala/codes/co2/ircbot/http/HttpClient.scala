package codes.co2.ircbot.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

class HttpClient(implicit system: ActorSystem) {

  val log: Logger = LoggerFactory.getLogger(getClass)
  implicit val ec: ExecutionContext = system.dispatcher

  def getTitle(url: String): Future[Option[String]] = {
    getPage(url).map(pageOpt => pageOpt.flatMap(page =>
      {
        val titleOpt = TitleParser.findTitle(page)
        log.info(s"Title of page $url: $titleOpt")
        titleOpt
      }))
  }

  def getPage(url: String, alreadyRedirected: Boolean = false): Future[Option[String]] = {
    Http().singleRequest(HttpRequest(uri = url))
      .flatMap(response => {
        val result = response.status match {
          case status if status.isRedirection() && !alreadyRedirected =>
            // Try to follow a redirect just once
            redirect(url, response)

          case status if status.isSuccess() || response.headers.find(header => header.is("content-type"))
            .exists(header => header.value() == "text/html") =>

            log.info(s"Getting page from $url")
            downloadHead(response)

          case _ =>
            log.info(s"Did not get a (correct) html page from $url")
            Future.successful(None)
        }

        // Discarding entitybytes only after completing the future to prevent rare race conditions.
        result.onComplete(_ => response.discardEntityBytes())

        result
      })
  }

  /* Okay so this is ugly and I apologize. In my defense, it was the best the Akka Gitter could come up with.
   * In short, I don't need more than 10000 bytes or so, that should be the entire html <head> segment.
   * If I get (much) more it's just needless network traffic. What happens is that the low-level TCP stack
   * buffers data as it comes in, and then akka streams the dataBytes in 'chunks'. I keep a count of the total
   * bytes after each chunk and close the connection immediately as soon as I have enough. Often this is quite
   * a bit more than 10K but that's fine. In fact, in my tests, the first chunk ( take(1) ) would probably
   * suffice in most cases, but that would make things too dependent on the network setup. This is safer.
   */
  private def downloadHead(response: HttpResponse) = {
    var bytes: Int = 0


    response.entity.dataBytes.takeWhile(
      byteStr => {
        val res = bytes < 10000
        bytes += byteStr.size
        res
      }

    ).runWith(Sink.fold(ByteString.empty)(_ ++ _)).map(byteString => Some(byteString.utf8String))
  }

  private def redirect(url: String, response: HttpResponse) = {
    response.headers.find(header => header.is("location")).map(
      header => {
        log.info(s"redirecting from $url to ${header.value}")
        getPage(header.value, alreadyRedirected = true)
      }).getOrElse(Future.successful(None))
  }
}

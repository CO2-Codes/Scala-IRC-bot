package codes.co2.ircbot.listeners.links

object LinkParser {

  def findLink(message: String): Option[String] = {
    val lowerCase = message.toLowerCase
    val httpIndex = lowerCase.indexOf("http://")
    val httpsIndex = lowerCase.indexOf("https://")

    val indexToUse =
      (httpIndex, httpsIndex) match {
        case (-1, -1) => None
        case (_, -1)  => Some(httpIndex)
        case (-1, _)  => Some(httpsIndex)
        case (x, y)   => Some(x.min(y))
      }

    indexToUse.map(index => message.substring(index).split(" ").head)

  }

  private val twitterStatusUrlRegex = "https?://(?:[^/]*\\.)*twitter\\.com/[a-zA-Z0-9_]*/status/\\d+.*".r

  private val mainTwitterUrl = "twitter.com"

  /** If the param is a twitter /status URL, return a valid fxtwitter API URL. Otherwise return None.
    */
  def convertTwitterStatusUrlToFxtwitter(url: String): Option[String] = {

    url match {
      case twitterStatusUrlRegex() =>
        val remainder = url.drop(url.indexOf(mainTwitterUrl) + mainTwitterUrl.length)

        Some(s"https://api.fxtwitter.com$remainder")

      case _ => None
    }
  }

  private val youtubeLongVideoUrlRegex = "https?://(?:[^/]*\\.)*youtube\\.com/watch\\?(?:.*&)*v=([a-zA-Z0-9_\\-]+)(?:&.*)*".r
  private val youtubeShortVideoUrlRegex = "https?://youtu\\.be/([a-zA-Z0-9_\\-]+).*".r

  def tryGetYoutubeId(url: String): Option[String] = {

    url match {
      case youtubeLongVideoUrlRegex(id)  => Some(id)
      case youtubeShortVideoUrlRegex(id) => Some(id)
      case _                             => None
    }
  }

}

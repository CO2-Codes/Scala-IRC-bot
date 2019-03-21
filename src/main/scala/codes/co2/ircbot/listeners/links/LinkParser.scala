package codes.co2.ircbot.listeners.links

object LinkParser {

  def findLink(message: String): Option[String] = {
    val lowerCase = message.toLowerCase
    val httpIndex = lowerCase.indexOf("http://")
    val httpsIndex = lowerCase.indexOf("https://")

    val indexToUse =
    (httpIndex, httpsIndex) match {
      case (-1, -1) => None
      case (_, -1) => Some(httpIndex)
      case (-1, _) => Some(httpsIndex)
      case (x, y) => Some(x.min(y))
    }

    indexToUse.map(index => message.substring(index).split(" ").head)

  }

}

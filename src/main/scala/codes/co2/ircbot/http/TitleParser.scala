package codes.co2.ircbot.http

object TitleParser {

  def findTitle(page: String): Option[String] = {
    val lowerCase = page.toLowerCase
    val startTagIdx = lowerCase.indexOf("<title>")
    val endTagIdx = lowerCase.indexOf("</title>")

    if (startTagIdx >= 0 && endTagIdx > startTagIdx) {
      Some(page.substring(startTagIdx + 7, endTagIdx))
    } else None

  }

}

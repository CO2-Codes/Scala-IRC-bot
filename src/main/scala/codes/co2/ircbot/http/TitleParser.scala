package codes.co2.ircbot.http

object TitleParser {

  def findTitle(page: String): Option[String] = {
    val lowerCase = page.toLowerCase
    val startTagIdx = lowerCase.indexOf("<title>")
    val endTagIdx = lowerCase.indexOf("</title>")

    if (startTagIdx >= 0 && endTagIdx > startTagIdx) {
      Some(stripSpecialChars(page.substring(startTagIdx + 7, endTagIdx)))
    } else None

  }

  /* If pircbotX gets a newline it'll actually try to send what's on the 2nd line as a new raw IRC command, so
     we have to replace this stuff
   */
  private def stripSpecialChars(string: String): String = {
    string
      .replaceAll("\n", " ")
      .replaceAll("\r", " ")
      .replaceAll("\t", " ")
      .replaceAll("\b", " ")
      .replaceAll("\\p{C}", "?") // Strip control codes; https://stackoverflow.com/a/6199346
  }

}

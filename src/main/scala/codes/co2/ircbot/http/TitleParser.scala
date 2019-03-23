package codes.co2.ircbot.http

import org.apache.commons.text.StringEscapeUtils


object TitleParser {

  def findTitle(page: String): Option[String] = {
    val lowerCase = page.toLowerCase
    val startTagIdx = lowerCase.indexOf("<title>")
    val endTagIdx = lowerCase.indexOf("</title>")

    if (startTagIdx >= 0 && endTagIdx > startTagIdx) {
      Some(page.substring(startTagIdx + 7, endTagIdx)).map(StringEscapeUtils.unescapeHtml4).map(stripSpecialChars)
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

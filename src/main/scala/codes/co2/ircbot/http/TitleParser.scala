package codes.co2.ircbot.http

import org.apache.commons.text.StringEscapeUtils

object TitleParser {

  def findTitle(page: String): Option[String] = {
    val lowerCase = page.toLowerCase

    val startTagIdx = Some(lowerCase.indexOf("<title"))
    for {
      startTagIdxOpt <- startTagIdx if startTagIdxOpt >= 0
      startTagClosingBraceIdx = lowerCase.indexOf('>', startTagIdxOpt) if startTagClosingBraceIdx >= 0
      endTagIdx = lowerCase.indexOf("</title>", startTagClosingBraceIdx) if endTagIdx >= 0
      title = page.substring(startTagClosingBraceIdx + 1, endTagIdx)
    } yield title

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

  def sanitizeToIrcMessage(text: String): String = {
    stripSpecialChars(StringEscapeUtils.unescapeHtml4(text)).trim
  }

}

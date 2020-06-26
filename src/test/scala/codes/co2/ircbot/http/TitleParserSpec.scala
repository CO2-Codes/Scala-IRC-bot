package codes.co2.ircbot.http

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TitleParserSpec extends AnyFlatSpec with Matchers {

  "findTitle" should "return the first title in the document" in {

    TitleParser.findTitle("Some <html> with here a <title>This is my title</title> </body") should be(Some("This is my title"))
    TitleParser.findTitle("<TITLE>title 1</TITLE> and <title>title 2</title>") should be(Some("title 1"))
    TitleParser.findTitle("<TITLE></TITLE>") should be(Some(""))

    TitleParser.findTitle("Some <html> with here a <title id = \"yo\">This is my title</title> </body") should be(Some("This is my title"))
    TitleParser.findTitle("Some <html> with </title> here a <title id = \"yo\">This is my title</title> </body") should be(Some("This is my title"))

  }

  "findTitle" should "return none if there is no valid title" in {

    TitleParser.findTitle("Some <html> with here a </title>This is my title</title> </body") should be(None)
    TitleParser.findTitle("Bla") should be(None)
    TitleParser.findTitle("<Title>") should be(None)

    TitleParser.findTitle("Some <html> with here a <title id = \"yo\" This is my title</title> </body") should be(None)
    TitleParser.findTitle("Some <html> with </title> here a <title id = \"yo\">This is my title </body") should be(None)

  }

  "sanitizeToIrcMessage" should "strip newlines and so on" in {
    TitleParser.sanitizeToIrcMessage("a\nb\rc\td\be") should be("a b c d e")
  }

  "sanitizeToIrcMessage" should "unescape html special chars" in {
    TitleParser.sanitizeToIrcMessage("&quot; &gt; &euro;") should be("\" > €")
  }

  "sanitizeToIrcMessage" should "trim whitespace" in {
    TitleParser.sanitizeToIrcMessage("      test       ") should be("test")
  }

}

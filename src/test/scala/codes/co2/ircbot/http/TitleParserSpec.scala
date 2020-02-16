package codes.co2.ircbot.http

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TitleParserSpec extends AnyFlatSpec with Matchers {

  "findTitle" should "return the first title in the document" in {

    TitleParser.findTitle("Some <html> with here a <title>This is my title</title> </body") should be(Some("This is my title"))
    TitleParser.findTitle("<TITLE>title 1</TITLE> and <title>title 2</title>") should be(Some("title 1"))
    TitleParser.findTitle("<TITLE></TITLE>") should be(Some(""))

  }

  "findTitle" should "return none if there is no valid title" in {

    TitleParser.findTitle("Some <html> with here a </title>This is my title</title> </body") should be(None)
    TitleParser.findTitle("Bla") should be(None)
    TitleParser.findTitle("<Title>") should be(None)

  }

  "findTitle" should "strip newlines and so on" in {
    TitleParser.findTitle("<title>a\nb\rc\td\be</title>") should be(Some("a b c d e"))
  }

  "findTitle" should "unescape html special chars" in {
    TitleParser.findTitle("<title>&quot; &gt; &euro;</title>") should be(Some("\" > €"))
  }

  "findTitle" should "trim whitespace" in {
    TitleParser.findTitle("<title>      test       </title>") should be(Some("test"))
  }

}

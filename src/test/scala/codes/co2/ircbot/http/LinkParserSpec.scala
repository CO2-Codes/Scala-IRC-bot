package codes.co2.ircbot.http

import codes.co2.ircbot.listeners.links.LinkParser
import org.scalatest.{FlatSpec, Matchers}

class LinkParserSpec extends FlatSpec with Matchers {

  "findLink" should "return the first link in the message" in {

    LinkParser.findLink("http://google.com") should be(Some("http://google.com"))
    LinkParser.findLink("http://google.com bla") should be(Some("http://google.com"))
    LinkParser.findLink("http://google.com ") should be(Some("http://google.com"))
    LinkParser.findLink("bla http://google.com") should be(Some("http://google.com"))
    LinkParser.findLink("bla http://google.com bla") should be(Some("http://google.com"))

    LinkParser.findLink("bla http://google.com bla https://github.com") should be(Some("http://google.com"))
    LinkParser.findLink("bla https://github.com bla http://google.com") should be(Some("https://github.com"))
  }


  "findLink" should "return none if the message does not contain a link" in {

    LinkParser.findLink("something http blabla") should be(None)
    LinkParser.findLink("something blabla") should be(None)
    LinkParser.findLink("") should be(None)

  }


}

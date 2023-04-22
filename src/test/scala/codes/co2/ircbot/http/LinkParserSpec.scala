package codes.co2.ircbot.http

import codes.co2.ircbot.listeners.links.LinkParser
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LinkParserSpec extends AnyFlatSpec with Matchers {

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

  "tryGetTwitterId" should "get the twitter status id given a status link" in {

    val id = 1253198059040673793L

    LinkParser.convertTwitterStatusUrlToFxtwitter("something random") should be(None)

    LinkParser.convertTwitterStatusUrlToFxtwitter(s"https://twitter.com/scala_lang/status/$id") should be(Some(id))
    LinkParser.convertTwitterStatusUrlToFxtwitter(s"http://twitter.com/scala_lang/status/$id") should be(Some(id))
    LinkParser.convertTwitterStatusUrlToFxtwitter(s"https://mobile.twitter.com/scala_lang/status/$id") should be(Some(id))

    LinkParser.convertTwitterStatusUrlToFxtwitter(s"https://twitter.com.evil.org/scala_lang/status/$id") should be(None)
    LinkParser.convertTwitterStatusUrlToFxtwitter(s"https://eviltwitter.com/scala_lang/status/$id") should be(None)

    LinkParser.convertTwitterStatusUrlToFxtwitter(s"https://twitter.com/scala_lang/status/$id?url_param=whatever&status=123") should be(
      Some(id)
    )
    LinkParser.convertTwitterStatusUrlToFxtwitter(s"https://twitter.com/scala_lang/status/$id/photo/2") should be(Some(id))

  }

  "tryGetYoutubeId" should "get the youtube video id given a video link" in {

    val id = "BxV14h0kFs0"

    LinkParser.tryGetYoutubeId("something random") should be(None)

    LinkParser.tryGetYoutubeId(s"https://youtu.be/$id") should be(Some(id))
    LinkParser.tryGetYoutubeId(s"http://youtu.be/$id?t=2") should be(Some(id))

    LinkParser.tryGetYoutubeId(s"https://www.youtube.com/watch?v=$id") should be(Some(id))
    LinkParser.tryGetYoutubeId(s"http://m.youtube.com/watch?v=$id") should be(Some(id))
    LinkParser.tryGetYoutubeId(s"https://www.youtube.com/watch?v=$id&feature=youtu.be&t=2") should be(Some(id))
    LinkParser.tryGetYoutubeId(s"https://www.youtube.com/watch?feature=youtu.be&v=$id&t=2") should be(Some(id))
    LinkParser.tryGetYoutubeId(s"https://www.youtube.com/watch?feature=youtu.be&t=2&v=$id") should be(Some(id))

    LinkParser.tryGetYoutubeId(s"http://m.youtube.com/watch?v=ab-_") should be(Some("ab-_"))

  }

  "tryGetYoutubeId" should "ignore non-video links" in {

    LinkParser.tryGetYoutubeId(s"https://www.youtube.com/user/enyay") should be(None)
    LinkParser.tryGetYoutubeId(s"https://www.youtube.com/channel/UCRUULstZRWS1lDvJBzHnkXA") should be(None)

  }

}

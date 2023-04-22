package codes.co2.ircbot.pronouns

import java.io.{BufferedWriter, File, FileWriter}

import akka.actor.{Actor, ActorLogging, Props}
import codes.co2.ircbot.pronouns.PronounsActor.Pronoun
import org.pircbotx.hooks.types.GenericMessageEvent

import scala.io.Source

class PronounsActor(file: File) extends Actor with ActorLogging {

  import PronounsActor.Contract._

  private def pronounsToString(name: String, pronouns: Set[Pronoun]) = {
    if (pronouns.isEmpty) {
      s"$name has no pronouns set. See !help for info on how to set them."
    } else {
      val pronounString = pronouns.foldLeft("") { case (soFar, next) => s"$soFar ${next.description}," }.init
      s"$name's pronouns are:$pronounString."
    }
  }

  override def preStart(): Unit = {
    self ! Init
  }

  override def receive: Receive = {
    case Init =>
      val newFileCreated = file.createNewFile()
      if (newFileCreated) {
        log.info("PronounsActor file did not yet exist, created.")
      }

      val source = Source.fromFile(file)

      val pronounMap = source.getLines().map { rawLine =>
        val split = rawLine.split(" ")
        split.head -> split(1)

      }.toMap.view.mapValues { rawValue =>
        rawValue.flatMap(pronounLetter => Pronoun.validPronouns.find(pronoun => pronoun.letter == pronounLetter)).toSet
      }.toMap

      log.info(s"Read ${pronounMap.size} users with pronouns from file.")

      source.close()

      context.become(running(pronounMap))
    case other => log.error(s"Got non-init message before initiating. $other")
  }

  private def updateState(newState: Map[String, Set[Pronoun]]): Map[String, Set[Pronoun]] = {
    writeAsNewFile(newState)
    context.become(running(newState))
    newState
  }

  // Yeah this is not optimized but it should work for now.
  private def writeAsNewFile(state: Map[String, Set[Pronoun]]): Unit = {
    val lines = state.map(Pronoun.convertToStrForFile)
    file.delete()
    file.createNewFile()
    val writer = new BufferedWriter(new FileWriter(file))
    lines.foreach(line => writer.write(line))
    writer.close()
  }

  def running(state: Map[String, Set[Pronoun]]): Receive = {
    case Get(name, resp) => resp.respondWith(pronounsToString(name, state.getOrElse(name.toLowerCase, Set.empty)))

    case Add(name, pronoun, resp) =>
      val currentPronouns = state.getOrElse(name.toLowerCase, Set.empty)
      if (currentPronouns.contains(pronoun)) {
        resp.respondWith("I already had it that way.")
      } else {
        val newPronouns = currentPronouns + pronoun

        updateState(state.updated(name.toLowerCase, newPronouns))

        resp.respondWith(s"Okay, ${pronounsToString(name, newPronouns)}")
      }

    case Remove(name, pronoun, resp) =>
      val currentPronouns = state.getOrElse(name.toLowerCase, Set.empty)
      if (!currentPronouns.contains(pronoun)) {
        resp.respondWith("I already had it that way.")
      } else {
        val newPronouns = currentPronouns - pronoun

        updateState(state.updated(name.toLowerCase, newPronouns))

        resp.respondWith(s"Okay, ${pronounsToString(name, newPronouns)}")
      }

    case Forget(name, resp) =>
      if (state.contains(name.toLowerCase)) {

        updateState(state.removed(name.toLowerCase))

        resp.respondWith(s"Okay, I have forgotten all pronoun info for $name.")
      } else {
        resp.respondWith(s"I already don't have any pronoun info for $name.")
      }

    case Init => log.warning("Actor was already initiated. Ignoring this message.")

  }
}


object PronounsActor {

  sealed trait Contract

  object Contract {

    private[PronounsActor] case object Init extends Contract

    case class Get(name: String, event: GenericMessageEvent) extends Contract
    case class Add(name: String, pronoun: Pronoun, event: GenericMessageEvent) extends Contract
    case class Remove(name: String, pronoun: Pronoun, event: GenericMessageEvent) extends Contract
    case class Forget(name: String, event: GenericMessageEvent) extends Contract

  }

  def props(file: File): Props = Props(new PronounsActor(file))

  sealed trait Pronoun {
    val letter: Char
    val description: String
  }

  object Pronoun {

    private case object He extends Pronoun {
      override val letter: Char = 'm' // male
      override val description: String = "he/him"
    }

    private case object She extends Pronoun {
      override val letter: Char = 'f' // female
      override val description: String = "she/her"
    }

    private case object They extends Pronoun {
      override val letter: Char = 'n' // neutral
      override val description: String = "they/them"
    }

    private case object It extends Pronoun {
      override val letter: Char = 'i' // inanimate
      override val description: String = "it/its"
    }

    private case object Other extends Pronoun {
      override val letter: Char = 'o' // other
      override val description: String = "other, please ask"
    }

    val validPronouns: Seq[Pronoun] = Seq(He, She, They, It, Other)

    def fromString(string: String): Option[Pronoun] = {
      string.toLowerCase match {
        case "he" => Some(He)
        case "she" => Some(She)
        case "they" => Some(They)
        case "it" => Some(It)
        case "other" => Some(Other)
        case _ => None
      }
    }

    private[pronouns] def convertToStrForFile(entry: (String, Set[Pronoun])): String = {
      val pronouns = new String(entry._2.map(pr => pr.letter).toArray)
      s"${entry._1} $pronouns\n"
    }

  }
}

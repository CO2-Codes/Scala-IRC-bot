package codes.co2.ircbot.pronouns

import codes.co2.ircbot.pronouns.PronounsHandler.Pronoun
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.{Logger, LoggerFactory}

import java.io.{BufferedWriter, File, FileWriter}
import java.util.concurrent.atomic.AtomicReference
import scala.io.Source

class PronounsHandler(file: File) {

  val log: Logger = LoggerFactory.getLogger(getClass)

  private[this] val state: AtomicReference[Map[String, Set[Pronoun]]] =
    new AtomicReference(Map.empty[String, Set[Pronoun]])

  private def start(): Unit = {

    val newFileCreated = file.createNewFile()
    if (newFileCreated) {
      log.info("Pronouns file did not yet exist, created.")
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

    state.set(pronounMap)

  }

  start()

  private def pronounsToString(name: String, pronouns: Set[Pronoun], addHelp: Boolean) = {
    if (pronouns.isEmpty && addHelp) {
      s"$name has no pronouns set. See !help for info on how to set them."
    } else if (pronouns.isEmpty) {
      s"$name has no pronouns set."
    } else {
      val pronounString = pronouns.foldLeft("") { case (soFar, next) => s"$soFar ${next.description}," }.init
      s"$name's pronouns are:$pronounString."
    }
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

  def get(name: String, event: GenericMessageEvent): Unit = {
    event.respondWith(pronounsToString(name, state.get().getOrElse(name.toLowerCase, Set.empty), true))
  }

  def add(name: String, pronoun: Pronoun, event: GenericMessageEvent): Unit = {
    state.updateAndGet { oldState =>

      val currentPronouns = oldState.getOrElse(name.toLowerCase, Set.empty)
      if (currentPronouns.contains(pronoun)) {
        event.respondWith("I already had it that way.")
        oldState
      } else {
        val newPronouns = currentPronouns + pronoun

        val newState = oldState.updated(name.toLowerCase, newPronouns)

        writeAsNewFile(newState)

        event.respondWith(s"Okay, ${pronounsToString(name, newPronouns, false)}")

        newState
      }

    }

    ()

  }

  def remove(name: String, pronoun: Pronoun, event: GenericMessageEvent): Unit = {
    state.updateAndGet { oldState =>
      val currentPronouns = oldState.getOrElse(name.toLowerCase, Set.empty)
      if (!currentPronouns.contains(pronoun)) {
        event.respondWith("I already had it that way.")
        oldState
      } else {
        val newPronouns = currentPronouns - pronoun

        val newState = if (newPronouns.isEmpty) {
          oldState.removed(name.toLowerCase)
        } else {
          oldState.updated(name.toLowerCase, newPronouns)
        }

        writeAsNewFile(newState)

        event.respondWith(s"Okay, ${pronounsToString(name, newPronouns, false)}")

        newState
      }
    }
  }

  def forget(name: String, event: GenericMessageEvent): Unit = {
    state.updateAndGet { oldState =>
      if (oldState.contains(name.toLowerCase)) {

        val newState = oldState.removed(name.toLowerCase)

        writeAsNewFile(newState)

        event.respondWith(s"Okay, I have forgotten all pronoun info for $name.")

        newState
      } else {
        event.respondWith(s"I already don't have any pronoun info for $name.")

        oldState
      }

    }
  }

}

object PronounsHandler {

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
        case "he"    => Some(He)
        case "she"   => Some(She)
        case "they"  => Some(They)
        case "it"    => Some(It)
        case "other" => Some(Other)
        case _       => None
      }
    }

    private[pronouns] def convertToStrForFile(entry: (String, Set[Pronoun])): String = {
      val pronouns = new String(entry._2.map(pr => pr.letter).toArray)
      s"${entry._1} $pronouns\n"
    }

  }
}

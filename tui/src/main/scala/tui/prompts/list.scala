package tui.prompts

import scala.jdk.CollectionConverters.*

import tui.prompts.handlers._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import cats.Monad
import cats.effect.Sync
import cats.implicits._

object list:
  case class Item(id: String, text: String)

  def getInputFromList[F[_]: Sync](
      prompt: ConsolePrompt,
      message: String,
      choices: List[String]
  ): F[String] =
    val builder = prompt.getPromptBuilder()
    val handler = createListFormPrompt(message, choices)

    for
      rawResult <- prompt.prompt(handler.combinePrompts(builder)).asScala.pure
      result <- handler.handle(rawResult).map(_.get)
    yield result

  private def createListFormPrompt[F[_]: Monad](
      promptMessage: String,
      items: List[String]
  ): Handler[F, ListPromptBuilder, String] =
    val subHandler =
      createItemsSubHandler(items.map(i => Item(i, i)), handle = _.pure)

    makeListHandler(
      List(subHandler),
      "list-form-input",
      promptMessage
    )

  private def createItemsSubHandler[F[_], Output](
      items: List[Item],
      handle: String => F[Output]
  ): SinglePropHandler[F, ListPromptBuilder, Output] =
    SinglePropHandler[F, ListPromptBuilder, Output](
      addToPrompt = (builder) =>
        items
          .foldLeft(builder) { (builder, item) =>
            builder
              .newItem(item.id)
              .text(item.text)
              .add()
          },
      check = (result) => items.find(_.id == result).isDefined,
      handle = handle
    )

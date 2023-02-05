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
    val subHandlers = List(
      createItemsSubHandler(choices.map(x => Item(x, x)), handle = _.pure)
    )
    val handler = makeListHandler(subHandlers, "list-form-input", message)

    executeHandler(prompt, handler)

  def runOnInputFromList[F[_]: Sync](
      prompt: ConsolePrompt,
      message: String,
      handlers: List[SinglePropHandler[F, ListPromptBuilder, Unit]]
  ): F[Unit] =
    val handler = makeListHandler(handlers, "list-input", message)

    executeHandler(prompt, handler)

  private def executeHandler[F[_]: Sync, Output](
      prompt: ConsolePrompt,
      handler: Handler[F, ListPromptBuilder, Output]
  ) =
    val builder = prompt.getPromptBuilder()

    for
      rawResult <- prompt.prompt(handler.combinePrompts(builder)).asScala.pure
      result <- handler.handle(rawResult).map(_.get)
    yield result

  def createItemsSubHandler[F[_], Output](
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

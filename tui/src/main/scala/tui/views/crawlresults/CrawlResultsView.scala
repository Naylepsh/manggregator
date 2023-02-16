package tui.views.crawlresults

import scala.jdk.CollectionConverters.*

import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import library.domain.asset.AssetSummary
import tui.prompts.menu
import tui.views.{Context, View}

class CrawlResultsView[F[_]: Sync: Console](
    context: Context[F],
    assets: List[AssetSummary],
    previous: View[F]
) extends View[F]:

  def view(): F[Unit] =
    menu.make(
      context.prompt,
      "Select an asset to see recent releases of:",
      actions,
      previous
    )

  private val actions = assets
    .sortBy(_.asset.name)
    .map(summary =>
      summary.asset.id.value.toString -> menu
        .Action(
          text = summary.asset.name.value,
          handle = _ => new ChaptersView(context, summary.chapters, this).view()
        )
    )

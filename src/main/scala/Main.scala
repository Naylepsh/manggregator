import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect._
import cats.effect.std._
import cats._
import cats.implicits._
import org.legogroup.woof.{given, *}
import crawler.domain.Library
import crawler.services.Crawling
import manggregator.Entrypoints
import api.Http
import library.persistence._
import library.domain.asset._
import library.domain.page._

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    for {
      given Logger[IO] <- Entrypoints.logger()
      storage = Entrypoints.storage()
      library = Entrypoints.library(storage)
      crawling = Entrypoints.crawling()
      _ <- seedAssetRepository(storage)
      server <- httpServer(storage, library, crawling)
    } yield server

  def httpServer[F[_]: Async: Console](
      storage: Storage[F],
      library: Library[F],
      crawling: Crawling[F]
  ): F[ExitCode] =

    val docs = Http.Docs(title = "MANGgregator", version = "0.0.1")
    val server = api.Http(Http.Props(docs, library, storage, crawling))

    server.as(ExitCode.Success)

  def seedAssetRepository[F[_]: FlatMap: Logger](storage: Storage[F]) =
    val eliteKnight = CreateAsset(AssetName("Elite Knight"), Enabled(true))
    val eliteKnightChaptersPage = (assetId: AssetId) =>
      CreateChaptersPage(
        Site("mangakakalot"),
        PageUrl("https://readmanganato.com/manga-gx984006"),
        assetId
      )
    val saisa = CreateAsset(AssetName("Saisa"), Enabled(true))
    val saisaChaptersPage = (assetId: AssetId) =>
      CreateChaptersPage(
        Site("mangakakalot"),
        PageUrl("https://mangakakalot.com/manga/2_saisa_no_osananajimi"),
        assetId
      )

    for {
      eliteKnightId <- storage.assets.create(eliteKnight)
      _ <- storage.pages.create(eliteKnightChaptersPage(eliteKnightId))
      saisaId <- storage.assets.create(saisa)
      _ <- storage.pages.create(saisaChaptersPage(saisaId))
      _ <- Logger[F].info(s"${eliteKnightId} :: ${saisaId}")
    } yield ()

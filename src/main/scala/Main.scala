import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect._
import cats.effect.std._
import cats._
import cats.implicits._
import crawler.domain.Library
import crawler.services.Crawling
import manggregator.Entrypoints
import api.Http
import library.persistence._
import library.domain.asset._
import library.domain.page._

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    val storage = Entrypoints.storage()
    val library = Entrypoints.library(storage)
    val crawling = Entrypoints.crawling()

    seedAssetRepository(storage) *> httpServer(storage, library, crawling)

  def httpServer[F[_]: Async: Console](
      storage: Storage[F],
      library: Library[F],
      crawling: Crawling[F]
  ): F[ExitCode] =

    val docs = Http.Docs(title = "MANGgregator", version = "0.0.1")
    val server = api.Http(Http.Props(docs, library, storage, crawling))

    server.as(ExitCode.Success)

  def seedAssetRepository[F[_]: FlatMap: Console](storage: Storage[F]) =
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
      _ <- Console[F].println(s"${eliteKnightId} :: ${saisaId}")
    } yield ()

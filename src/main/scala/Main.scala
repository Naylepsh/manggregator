import cats.effect._
import cats.effect.std._
import cats._
import cats.implicits._
import org.legogroup.woof.{given, *}
import crawler.domain.Library
import crawler.services.Crawling
import manggregator.Entrypoints
import library.persistence._
import library.domain.asset._
import library.domain.page._
import api.Http
import api.library.routes.Services

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    for {
      given Logger[IO] <- Entrypoints.logger()
      storage = Entrypoints.storage()
      library = Entrypoints.library(storage)
      crawling = Entrypoints.crawling()
      libraryServices = Entrypoints.libraryServices(storage)
      _ <- seedAssetRepository(storage)
      docs = Http.Docs(title = "MANGgregator", version = "0.0.1")
      server <- api
        .Http(Http.Props(docs, library, crawling, libraryServices))
        .useForever
    } yield ExitCode.Success

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

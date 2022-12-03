import api.config._
import api.library.routes.Services
import cats._
import cats.effect._
import cats.effect.std._
import cats.implicits._
import com.comcast.ip4s._
import crawler.domain.Library
import crawler.services.Crawling
import library.domain.asset._
import library.domain.page._
import library.persistence._
import manggregator.Entrypoints
import org.legogroup.woof.{_, given}

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    val serverConfig = ServerConfig(
      ipv4"0.0.0.0",
      port"8080"
    )
    val docs = Docs(title = "MANGgregator", version = "0.0.1")

    for {
      given Logger[IO] <- Entrypoints.logger()
      storage = Entrypoints.storage()
      library = Entrypoints.library(storage)
      crawling = Entrypoints.crawling()
      libraryServices = Entrypoints.libraryServices(storage)
      _ <- seedAssetRepository(storage)
      _ <- Entrypoints
        .http(docs, library, crawling, libraryServices, serverConfig)
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

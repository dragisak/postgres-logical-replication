package postgresrep.streams

import cats.effect.{IO, IOApp}
import com.typesafe.config.{Config, ConfigFactory}
import fs2.*
import org.slf4j.LoggerFactory
import postgresrep.Main.getClass
import postgresrep.Postgres
import postgresrep.protocol.PgMessage
import scodec.Attempt
import scodec.bits.BitVector

object Main extends IOApp.Simple {

  private val logger = LoggerFactory.getLogger(getClass)

  private val stream: Stream[IO, Option[BitVector]] = makeStream()

  override def run: IO[Unit] = {
    stream
      .filter(_.isDefined)
      .map(_.get)
      .map(bitVector => PgMessage.codec.decode(bitVector))
      .evalMap {
        case Attempt.Successful(decoded) => IO(logger.info(decoded.value.toString))
        case Attempt.Failure(err)        => IO(logger.error(err.messageWithContext))
      }
      .compile
      .drain

  }

  private def makeStream() = {
    val config = ConfigFactory.load()
    val pg     = new Postgres(config.getConfig("postgres"))

    val stream = for {
      conn     <- Stream.bracket(IO(pg.createConnection()))(c => IO(c.close()))
      pgStream <- Stream.bracket(IO(pg.createStream(conn)))(c => IO(c.close()))
      s        <- ReplicationStream.createStream(pgStream)
    } yield s
    stream
  }
}

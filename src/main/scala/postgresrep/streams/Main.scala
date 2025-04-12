package postgresrep.streams

import cats.effect.{IO, IOApp}
import com.typesafe.config.{Config, ConfigFactory}
import fs2.*
import org.slf4j.LoggerFactory
import postgresrep.Main.getClass
import postgresrep.Postgres
import postgresrep.protocol.PgMessage
import scodec.Attempt
import scodec.bits.{BitVector, ByteVector}
import fs2.interop.scodec.*

object Main extends IOApp.Simple {

  private val logger = LoggerFactory.getLogger(getClass)

  private val stream = makeStream()

  override def run: IO[Unit] = {

    stream
      .map(BitVector(_))
      .map(PgMessage.codec.decode)
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

    for {
      conn     <- Stream.bracket(IO(pg.createConnection()))(c => IO(c.close()))
      pgStream <- Stream.bracket(IO(pg.createStream(conn)))(c => IO(c.close()))
      s        <- ReplicationStream.createStream(pgStream)
    } yield s
  }
}

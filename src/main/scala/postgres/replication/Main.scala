package postgres.replication

import cats.effect.{IO, IOApp}
import cats.implicits.*
import com.typesafe.config.ConfigFactory
import fs2.*
import org.slf4j.LoggerFactory
import postgres.replication.protocol.PgMessage
import postgres.replication.streams.ReplicationStream
import scodec.Attempt
import scodec.bits.BitVector

import java.util.logging.LogManager

object Main extends IOApp.Simple {

  try {
    val is = getClass.getClassLoader.getResourceAsStream("logging.properties")
    LogManager.getLogManager.readConfiguration(is)
  } catch {
    case e: Throwable => e.printStackTrace()
  }

  private val logger = LoggerFactory.getLogger(getClass)
  private val stream = createStream()

  override def run: IO[Unit] = {

    stream
      .evalMap {
        case Attempt.Successful(decoded) =>
          IO {
            logger.info(decoded.value.toString)
            if (decoded.remainder.nonEmpty) {
              logger.warn(s"Remainder: ${decoded.remainder.size} bytes")
            }
          }
        case Attempt.Failure(err) =>
          IO(logger.error(err.messageWithContext))
      }
      .compile
      .drain

  }

  private def createStream() = {
    val config = ConfigFactory.load()
    val pg     = new Postgres(config.getConfig("postgres"))

    for {
      conn     <- Stream.bracket(IO(pg.createConnection()))(c => IO(c.close()))
      _        <- Stream.bracket(IO(pg.createReplicationSlot(conn)))(_ => IO(pg.dropReplicationSlot(conn)))
      pgStream <- Stream.bracket(IO(pg.createStream(conn)))(c => IO(c.close()))
      msg      <- ReplicationStream.createStream(pgStream)
      bitVector = BitVector(msg.content)
      decoded   = PgMessage.codec.decode(bitVector)
      _ = decoded match {
        case Attempt.Successful(_) =>
          pgStream.setAppliedLSN(msg.lastReceiveLSN)
          pgStream.setFlushedLSN(msg.lastReceiveLSN)
        case _ =>
      }
    } yield decoded
  }
}

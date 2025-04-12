package postgresrep

import com.typesafe.config.ConfigFactory
import org.postgresql.replication.PGReplicationStream
import org.slf4j.LoggerFactory
import scodec.Err
import scodec.bits.BitVector

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.util.Using
import scala.util.Using.Releasable

object Main {

  private val logger = LoggerFactory.getLogger(getClass)

  private val config = ConfigFactory.load()

  private val doRun = new AtomicBoolean(true)

  sys.addShutdownHook {
    logger.info("Shutting down application...")
    doRun.set(false)
  }

  @main def provision(): Unit = {
    val pg = new Postgres(config.getConfig("postgres"))
    Using.resource(pg.createConnection()) { conn =>
      pg.createReplicationSlot(conn)
    }
  }

  @main def execute(): Unit = {
    val pg = new Postgres(config.getConfig("postgres"))

    Using.resource(pg.createConnection()) { conn =>
      Using.resource(pg.createStream(conn)) { stream =>
        val f = start(stream)
        Await.ready(f, Duration.Inf)
      }
    }
  }

  private def start(stream: PGReplicationStream): Future[Unit] = Future {
    while (doRun.get()) {
      val msg = stream.readPending()

      if (msg == null) {
        TimeUnit.MILLISECONDS.sleep(10L)
      } else {
        val bitVector = BitVector(msg)

        MessageType.codec.decodeValue(bitVector) match {
          case scodec.Attempt.Successful(decoded) => logger.info(decoded.toString)
          case scodec.Attempt.Failure(err)        => logger.error(err.toString)
        }

        // feedback
        stream.setAppliedLSN(stream.getLastReceiveLSN)
        stream.setFlushedLSN(stream.getLastReceiveLSN)
      }
    }
  }
}

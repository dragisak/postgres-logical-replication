package postgresrep

import com.typesafe.config.ConfigFactory
import org.postgresql.replication.PGReplicationStream
import org.slf4j.LoggerFactory

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.util.Using
import scala.util.Using.Releasable

object Main extends App:

  private val logger = LoggerFactory.getLogger(getClass)

  private val config = ConfigFactory.load()

  private val pg = new Postgres(config.getConfig("postgres"))

  Using.resource(pg.createConnection()): conn =>
    val replicationSlot = pg.createReplicationSlot(conn)
    Using.resource(pg.createStream(conn)): stream =>
      val (s, f) = start(stream)
      f.onComplete(_ => pg.dropReplicationSlot(conn, replicationSlot))
      sys.runtime.addShutdownHook(new Thread {
        override def run(): Unit =
          logger.info("Shutting down application...")
          s.set(true)
      })
      Await.ready(f, Duration.Inf)

  private def start(stream: PGReplicationStream): (AtomicBoolean, Future[Unit]) =
    val stop = new AtomicBoolean(false)
    val f = Future {
      while !stop.get() do
        val msg = stream.readPending()

        if (msg == null) {
          TimeUnit.MILLISECONDS.sleep(10L)
        }

        val offset = msg.arrayOffset()
        val source = msg.array()
        val length = source.length - offset
        logger.info(s"$source, $offset, $length")

        // feedback
        stream.setAppliedLSN(stream.getLastReceiveLSN)
        stream.setFlushedLSN(stream.getLastReceiveLSN)
      end while
    }

    (stop, f)
  end start

end Main

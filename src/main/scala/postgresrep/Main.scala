package postgresrep

import com.typesafe.config.ConfigFactory
import org.postgresql.replication.PGReplicationStream

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.util.Using
import scala.util.Using.Releasable

object Main extends App:

  private val config = ConfigFactory.load()

  private val pg = new Postgres(config.getConfig("postgres"))

  Using.resource(pg.createConnection()): conn =>
    val replicationSlot = pg.createReplicationSlot(conn)
    Using.resource(pg.createStream(conn)): stream =>
      val (stop, f) = start(stream)
      sys.addShutdownHook(stop.set(true))
      f.onComplete(_ => pg.dropReplicationSlot(conn, replicationSlot))
      Await.result(f, Duration.Inf)

  private def start(stream: PGReplicationStream): (AtomicBoolean, Future[Unit]) =
    val stop = new AtomicBoolean(false)
    val f = Future {
      while true do
        val msg = stream.readPending()

        if (msg == null) {
          TimeUnit.MILLISECONDS.sleep(10L)
        }

        val offset = msg.arrayOffset()
        val source = msg.array()
        val length = source.length - offset
        println(s"$source, $offset, $length")

        // feedback
        stream.setAppliedLSN(stream.getLastReceiveLSN)
        stream.setFlushedLSN(stream.getLastReceiveLSN)
      end while
    }

    (stop, f)
  end start

end Main

package postgres.replication.streams

import cats.effect.IO
import fs2.*
import org.postgresql.replication.PGReplicationStream

import java.nio.ByteBuffer

object ReplicationStream {

  def createStream(stream: PGReplicationStream): Stream[IO, PgMessage] = Stream
    .repeatEval(IO.delay(stream.readPending()))
    .filter(_ != null)
    .map(b => PgMessage(b, stream.getLastReceiveLSN))
}

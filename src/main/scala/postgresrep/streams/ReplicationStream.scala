package postgresrep.streams

import cats.effect.IO
import fs2.*
import org.postgresql.replication.PGReplicationStream
import scodec.bits.BitVector

import java.nio.ByteBuffer

object ReplicationStream {

  def createStream(stream: PGReplicationStream): Stream[IO, ByteBuffer] = Stream
    .repeatEval(IO.delay(stream.readPending()))
    .filter(_ != null)
}

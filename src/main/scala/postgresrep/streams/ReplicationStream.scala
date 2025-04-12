package postgresrep.streams

import cats.effect.IO
import fs2.*
import org.postgresql.replication.PGReplicationStream
import scodec.bits.BitVector

object ReplicationStream {

  def createStream(stream: PGReplicationStream): Stream[IO, BitVector] = Stream
    .repeatEval(IO.delay(stream.readPending()))
    .filter(_ != null)
    .map(msg => BitVector(msg))
}

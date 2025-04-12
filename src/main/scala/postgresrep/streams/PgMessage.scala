package postgresrep.streams

import org.postgresql.replication.LogSequenceNumber

import java.nio.ByteBuffer

case class PgMessage(
    content: ByteBuffer,
    lastReceiveLSN: LogSequenceNumber
)

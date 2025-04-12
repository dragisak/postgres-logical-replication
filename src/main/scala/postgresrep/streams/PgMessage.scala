package postgresrep.streams

import org.postgresql.replication.LogSequenceNumber

case class PgMessage[T](
    content: T,
    lastReceiveLSN: LogSequenceNumber
)

package postgres.replication

import scodec.*
import scodec.bits.ByteVector
import scodec.codecs.*

import java.time.*
import java.time.temporal.ChronoUnit

package object protocol {
  private val postgresEpoch = Instant.parse("2000-01-01T00:00:00Z")

  val instantCodec: Codec[Instant] = int64.xmap(
    micros => postgresEpoch.plus(micros, ChronoUnit.MICROS),
    instant => Duration.between(postgresEpoch, instant).toNanos / 1000L
  )

  val transactionIdCodec: Codec[Int]    = int32
  val lsnCodec: Codec[Long]             = int64
  val oidCodec: Codec[Int]              = int32
  val replicaIdentityCodec: Codec[Char] = int8.xmap(_.toChar, _.toByte)

  val byteArrayCodec: Codec[Array[Byte]] = variableSizeBytes(int32, bytes).xmap(
    _.toArray,    // Decode: Convert ByteVector to Array[Byte]
    ByteVector(_) // Encode: Convert Array[Byte] to ByteVector
  )

  val textCodec: Codec[String] = variableSizeBytes(int32, utf8)
}

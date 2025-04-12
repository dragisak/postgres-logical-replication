package postgresrep

import scodec.codecs.*
import scodec.*
import scodec.*

import java.time.*
import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.util.concurrent.TimeUnit

package object protocol {
  private val postgresEpoch = Instant.parse("2000-01-01T00:00:00Z")

  implicit val instantCodec: Codec[Instant] = int64.xmap(ms => postgresEpoch.plus(ms, ChronoUnit.MICROS), i => Duration.between(postgresEpoch, i).toNanos / 1000L)
}

package postgres.replication.protocol

import scodec.*
import scodec.codecs.*

sealed trait TupleData

object TupleData {
  case object NullValue extends TupleData

  case object ToastedValue extends TupleData

  case class TextValue(text: String) extends TupleData

  case class BinaryValue(bytes: Array[Byte]) extends TupleData

  implicit val codec: Codec[TupleData] = discriminated[TupleData]
    .by(byte)
    .typecase('n', provide(NullValue))
    .typecase('u', provide(ToastedValue))
    .typecase('t', textCodec.as[TextValue])
    .typecase('b', byteArrayCodec.as[BinaryValue])

}

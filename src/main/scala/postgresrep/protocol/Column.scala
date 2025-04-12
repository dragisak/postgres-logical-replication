package postgresrep.protocol

import scodec.Codec
import scodec.codecs.*

case class Column(
    columnFlags: Int,
    columnName: String,
    oidOfColumnDataType: Int,
    typeModifier: Int
)

object Column {
  implicit val codec: Codec[Column] = (int8 ::
    cstring ::
    oidCodec ::
    int32).as[Column]
}

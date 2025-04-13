package postgres.replication.protocol

import scodec.Codec
import scodec.codecs.*

case class Column(
    columnFlags: Column.ColumnFlag,
    columnName: String,
    oidOfColumnDataType: Int,
    typeModifier: Int
)

object Column {

  sealed trait ColumnFlag
  object ColumnFlag {
    case object NoFlag    extends ColumnFlag
    case object PartOfKey extends ColumnFlag
    implicit val codec: Codec[ColumnFlag] = discriminated[ColumnFlag]
      .by(int8)
      .typecase(0, provide(NoFlag))
      .typecase(1, provide(PartOfKey))
  }

  implicit val codec: Codec[Column] = (ColumnFlag.codec ::
    cstring ::
    oidCodec ::
    int32).as[Column]
}

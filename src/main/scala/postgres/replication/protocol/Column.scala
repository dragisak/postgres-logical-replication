package postgres.replication.protocol

import scodec.Codec
import scodec.codecs.*

case class Column(
    columnFlags: Option[Column.ColumnFlag],
    columnName: String,
    oidOfColumnDataType: Int,
    typeModifier: Int
)

object Column {

  sealed trait ColumnFlag
  object ColumnFlag {
    case object PartOfKey extends ColumnFlag
    implicit val codec: Codec[Option[ColumnFlag]] = discriminated[Option[ColumnFlag]]
      .by(int8)
      .typecase(0, provide(None))
      .typecase(1, provide(Some(PartOfKey)))
  }

  implicit val codec: Codec[Column] = (ColumnFlag.codec ::
    cstring ::
    oidCodec ::
    int32).as[Column]
}

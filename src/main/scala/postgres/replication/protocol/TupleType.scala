package postgres.replication.protocol

import scodec.Codec
import scodec.codecs.*

sealed trait TupleType

object TupleType {
  case object New extends TupleType
  case object Old extends TupleType
  case object Key extends TupleType

  implicit val codec: Codec[TupleType] = discriminated[TupleType]
    .by(byte)
    .typecase('N', provide(New))
    .typecase('O', provide(Old))
    .typecase('K', provide(Key))
}

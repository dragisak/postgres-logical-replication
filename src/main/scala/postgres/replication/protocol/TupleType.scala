package postgres.replication.protocol

sealed trait TupleType

import scodec.Codec
import scodec.codecs.*

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

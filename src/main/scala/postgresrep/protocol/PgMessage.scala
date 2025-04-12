package postgresrep.protocol

import scodec.*
import scodec.bits.ByteVector
import scodec.codecs.*

import java.nio.ByteBuffer
import java.time.Instant

sealed trait PgMessage

object PgMessage {

  case class Begin(finalLSN: Long, timestamp: Instant, transactionId: Int) extends PgMessage
  object Begin {
    implicit val codec: Codec[Begin] = (lsnCodec :: instantCodec :: transactionIdCodec).as[Begin]
  }

  case class Message(content: String) extends PgMessage
  object Message {
    implicit val codec: Codec[Message] = utf8_32.as[Message]
  }

  case class Commit(lsnOfTheCommit: Long, lsnOfTransaction: Long, timestamp: Instant) extends PgMessage
  object Commit {
    implicit val codec: Codec[Commit] = (ignore(8) :: lsnCodec :: lsnCodec :: instantCodec).as[Commit]
  }

  case class Origin() extends PgMessage
  object Origin {
    implicit val codec: Codec[Origin] = provide(Origin())
  }

  case class Relation(
      transactionId: Int,
      oid: Int,
      namespace: String,
      relName: String,
      replicaIdentity: Char,
      columns: List[Column]
  ) extends PgMessage
  object Relation {
    implicit val codec: Codec[Relation] = (
      transactionIdCodec ::
        oidCodec ::
        cstring ::
        cstring ::
        replicaIdentityCodec ::
        listOfN(int16, Column.codec)
    ).as[Relation]
  }

  case class Type() extends PgMessage
  object Type {
    implicit val codec: Codec[Type] = provide(Type())
  }

  case class Insert(
//      transactionId: Int, PG17
      oid: Int,
      tupleType: TupleType,
      tuples: List[TupleData]
  ) extends PgMessage
  object Insert {
    implicit val codec: Codec[Insert] =
      (oidCodec ::
        TupleType.codec ::
        listOfN(int16, TupleData.codec)).as[Insert]
  }

  case class Update() extends PgMessage
  object Update {
    implicit val codec: Codec[Update] = provide(Update())
  }

  case class Delete() extends PgMessage
  object Delete {
    implicit val codec: Codec[Delete] = provide(Delete())
  }

  case class Truncate() extends PgMessage
  object Truncate {
    implicit val codec: Codec[Truncate] = provide(Truncate())
  }

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

  sealed trait TupleData

  object TupleData {
    case object NullValue                      extends TupleData
    case object ToastedValue                   extends TupleData
    case class TextValue(text: String)         extends TupleData
    case class BinaryValue(bytes: Array[Byte]) extends TupleData

    implicit val codec: Codec[TupleData] = discriminated[TupleData]
      .by(byte)
      .typecase('n', provide(NullValue))
      .typecase('u', provide(ToastedValue))
      .typecase('t', textCodec.as[TextValue])
      .typecase('b', byteArrayCodec.as[BinaryValue])

  }

  implicit val codec: Codec[PgMessage] = discriminated[PgMessage]
    .by(MessageType.codec)
    .typecase(MessageType.Begin, Begin.codec)
    .typecase(MessageType.Message, Message.codec)
    .typecase(MessageType.Commit, Commit.codec)
    .typecase(MessageType.Origin, Origin.codec)
    .typecase(MessageType.Relation, Relation.codec)
    .typecase(MessageType.Type, Type.codec)
    .typecase(MessageType.Insert, Insert.codec)
    .typecase(MessageType.Update, Update.codec)
    .typecase(MessageType.Delete, Delete.codec)
    .typecase(MessageType.Truncate, Truncate.codec)
}

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

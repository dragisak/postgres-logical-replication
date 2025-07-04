package postgres.replication.protocol

import scodec.*
import scodec.codecs.*

import java.time.Instant

sealed trait PgMessage

object PgMessage {

  case class Begin(finalLSN: Long, timestamp: Instant, transactionId: Int) extends PgMessage
  object Begin {
    implicit val codec: Codec[Begin] = (lsnCodec :: instantCodec :: transactionIdCodec).as[Begin]
  }

  case class Message(
      flag: Option[Message.Flag],
      lsn: Long,
      prefix: String,
      content: String
  ) extends PgMessage
  object Message {
    sealed trait Flag
    object Flag {
      case object Transactional extends Flag

      implicit val codec: Codec[Option[Flag]] = discriminated[Option[Flag]]
        .by(int8)
        .typecase(0, provide(None))
        .typecase(1, provide(Some(Transactional)))
    }

    implicit val codec: Codec[Message] = (
      Flag.codec ::
        lsnCodec ::
        cstring ::
        variableSizeBytes(int32, utf8)
    ).as[Message]
  }

  case class Commit(lsnOfTheCommit: Long, lsnOfTransaction: Long, timestamp: Instant) extends PgMessage
  object Commit {
    implicit val codec: Codec[Commit] = (ignore(8) :: lsnCodec :: lsnCodec :: instantCodec).as[Commit]
  }

  case class Origin(lsn: Long, name: String) extends PgMessage
  object Origin {
    implicit val codec: Codec[Origin] = (lsnCodec :: cstring).as[Origin]
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

  case class Update(
      oid: Int,
      tuples: Option[(TupleType, List[TupleData])],
      newTupleType: TupleType,
      newTuples: List[TupleData]
  ) extends PgMessage
  object Update {

    private val isPresent: Codec[Boolean] = peek(byte).xmap(
      b => b == 'K'.toByte || b == 'O'.toByte,                               // Map the byte to a Boolean
      _ => throw new UnsupportedOperationException("Encoding not supported") // Encoding is not needed
    )

    private val pair: Codec[(TupleType, List[TupleData])] = TupleType.codec :: listOfN(int16, TupleData.codec)

    implicit val codec: Codec[Update] =
      (oidCodec ::
        optional(isPresent, pair) ::
        TupleType.codec ::
        listOfN(int16, TupleData.codec)).as[Update]
  }

  case class Delete(
      oid: Int,
      tupleType: TupleType,
      tuples: List[TupleData]
  ) extends PgMessage
  object Delete {
    implicit val codec: Codec[Delete] =
      (oidCodec ::
        TupleType.codec ::
        listOfN(int16, TupleData.codec)).as[Delete]
  }

  case class Truncate(
      truncateType: Option[Truncate.TruncateType],
      oids: List[Int]
  ) extends PgMessage
  object Truncate {
    sealed trait TruncateType
    object TruncateType {
      case object Cascade         extends TruncateType
      case object RestartIdentity extends TruncateType

      implicit val codec: Codec[Option[TruncateType]] = discriminated[Option[TruncateType]]
        .by(int8)
        .typecase(0, provide(None))
        .typecase(1, provide(Some(Cascade)))
        .typecase(2, provide(Some(RestartIdentity)))

    }

    implicit val codec: Codec[Truncate] =
      int32
        .consume(n =>
          TruncateType.codec ::
            listOfN(provide(n), oidCodec)
        )(_._2.size)
        .as[Truncate]

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

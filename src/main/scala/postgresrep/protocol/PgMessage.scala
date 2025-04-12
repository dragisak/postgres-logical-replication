package postgresrep.protocol

import scodec.*
import scodec.codecs.*

import java.time.Instant

sealed trait PgMessage

object PgMessage {

  case class Begin(firstLSNOfTransaction: Long, timestamp: Instant) extends PgMessage
  object Begin {
    implicit val codec: Codec[Begin] = (int64 :: instantCodec).as[Begin]
  }

  case class Message(content: String) extends PgMessage
  object Message {
    implicit val codec: Codec[Message] = utf8_32.as[Message]
  }

  case class Commit(commit: Long) extends PgMessage
  object Commit {
    implicit val codec: Codec[Commit] = int64.as[Commit]
  }

  case class Origin() extends PgMessage
  object Origin {
    implicit val codec: Codec[Origin] = provide(Origin())
  }

  case class Relation() extends PgMessage
  object Relation {
    implicit val codec: Codec[Relation] = provide(Relation())
  }

  case class Type() extends PgMessage
  object Type {
    implicit val codec: Codec[Type] = provide(Type())
  }

  case class Insert() extends PgMessage
  object Insert {
    implicit val codec: Codec[Insert] = provide(Insert())
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

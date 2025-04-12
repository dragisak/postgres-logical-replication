package postgresrep

sealed trait PgMessage[MT <: MessageType] {
  def messageType: MT

}

object PgMessage {

  case class Begin() extends PgMessage[MessageType.Begin.type] {
    override val messageType: MessageType.Begin.type = MessageType.Begin
  }

  case class Message() extends PgMessage[MessageType.Message.type] {
    override val messageType: MessageType.Message.type = MessageType.Message
  }

  case class Commit() extends PgMessage[MessageType.Commit.type] {
    override val messageType: MessageType.Commit.type = MessageType.Commit
  }

  case class Origin() extends PgMessage[MessageType.Origin.type] {
    override val messageType: MessageType.Origin.type = MessageType.Origin
  }

  case class Relation() extends PgMessage[MessageType.Relation.type] {
    override val messageType: MessageType.Relation.type = MessageType.Relation
  }

  case class Type() extends PgMessage[MessageType.Type.type] {
    override val messageType: MessageType.Type.type = MessageType.Type
  }

  case class Insert() extends PgMessage[MessageType.Insert.type] {
    override val messageType: MessageType.Insert.type = MessageType.Insert
  }

  case class Update() extends PgMessage[MessageType.Update.type] {
    override val messageType: MessageType.Update.type = MessageType.Update
  }

  case class Delete() extends PgMessage[MessageType.Delete.type] {
    override val messageType: MessageType.Delete.type = MessageType.Delete
  }

  case class Truncate() extends PgMessage[MessageType.Truncate.type] {
    override val messageType: MessageType.Truncate.type = MessageType.Truncate
  }

  val codec = MessageType.codec.xmapc {
    case MessageType.Begin    => Begin()
    case MessageType.Message  => Message()
    case MessageType.Commit   => Commit()
    case MessageType.Origin   => Origin()
    case MessageType.Relation => Relation()
    case MessageType.Type     => Type()
    case MessageType.Insert   => Insert()
    case MessageType.Update   => Update()
    case MessageType.Delete   => Delete()
    case MessageType.Truncate => Truncate()
  } { _.messageType }

}

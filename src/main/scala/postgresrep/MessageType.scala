package postgresrep

sealed trait MessageType

import scodec.Codec
import scodec.codecs.*

object MessageType {
  // Protocol 1
  sealed trait Protocol1Message extends MessageType
  case object Begin             extends Protocol1Message
  case object Message           extends Protocol1Message
  case object Commit            extends Protocol1Message
  case object Origin            extends Protocol1Message
  case object Relation          extends Protocol1Message
  case object Type              extends Protocol1Message
  case object Insert            extends Protocol1Message
  case object Update            extends Protocol1Message
  case object Delete            extends Protocol1Message
  case object Truncate          extends Protocol1Message
//  // Protocol 2
//  sealed trait Protocol2Message extends MessageType
//  case object StreamStart       extends Protocol2Message
//  case object StreamStop        extends Protocol2Message
//  case object StreamCommit      extends Protocol2Message
//  case object StreamAbort       extends Protocol2Message
//  case object BeginPrepare      extends Protocol2Message
//  case object Prepare           extends Protocol2Message
//  case object CommitPrepared    extends Protocol2Message
//  case object RollbackPrepared  extends Protocol2Message
//  case object StreamPrepare     extends Protocol2Message

  // Codec for MessageType
  implicit val codec: Codec[MessageType] = discriminated[MessageType]
    .by(byte)
    .typecase('B', provide(Begin))
    .typecase('M', provide(Message))
    .typecase('C', provide(Commit))
    .typecase('O', provide(Origin))
    .typecase('R', provide(Relation))
    .typecase('Y', provide(Type))
    .typecase('I', provide(Insert))
    .typecase('U', provide(Update))
    .typecase('D', provide(Delete))
    .typecase('T', provide(Truncate))
//    .typecase('S', provide(StreamStart))
//    .typecase('E', provide(StreamStop))
//    .typecase('c', provide(StreamCommit))
//    .typecase('A', provide(StreamAbort))
//    .typecase('b', provide(BeginPrepare))
//    .typecase('P', provide(Prepare))
//    .typecase('K', provide(CommitPrepared))
//    .typecase('r', provide(RollbackPrepared))
//    .typecase('p', provide(StreamPrepare))
}

package postgresrep

import com.typesafe.config.Config
import org.postgresql.PGProperty
import org.postgresql.jdbc.PgConnection
import org.postgresql.replication.{PGReplicationStream, ReplicationSlotInfo}

import java.sql.DriverManager
import java.util.Properties

class Postgres(config: Config):

  def createConnection(): PgConnection =

    val url      = config.getString("url")
    val user     = config.getString("user")
    val password = config.getString("password")

    val props = new Properties()
    PGProperty.USER.set(props, "postgres")
    PGProperty.PASSWORD.set(props, "postgres")
    PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "9.4")
    PGProperty.REPLICATION.set(props, "database")
    PGProperty.PREFER_QUERY_MODE.set(props, "simple")
    PGProperty.USER.set(props, user)
    PGProperty.PASSWORD.set(props, password)

    val con = DriverManager.getConnection(url, props)
    con.unwrap(classOf[PgConnection])

  end createConnection

  def createReplicationSlot(conn: PgConnection): ReplicationSlotInfo =
    val replicationSlot = config.getString("replication-slot")
    val publication     = config.getString("publication")
    val slot = conn.getReplicationAPI
      .createReplicationSlot()
      .logical()
      .withSlotName(replicationSlot)
      .withOutputPlugin("pgoutput")
      .make()

    conn.execSQLUpdate(s"CREATE PUBLICATION $publication FOR ALL TABLES")
    slot
  end createReplicationSlot

  def dropReplicationSlot(conn: PgConnection, slot: ReplicationSlotInfo): Unit =
    val publication = config.getString("publication")
    conn.getReplicationAPI.dropReplicationSlot(slot.getSlotName)
    conn.execSQLUpdate(s"DROP PUBLICATION $publication")
  end dropReplicationSlot

  def createStream(conn: PgConnection): PGReplicationStream =
    val replicationSlot = config.getString("replication-slot")
    val publication     = config.getString("publication")

    conn.getReplicationAPI
      .replicationStream()
      .logical()
      .withSlotName(replicationSlot)
      .withSlotOption("proto_version", 1)
      .withSlotOption("publication_names", publication)
      .start()
  end createStream

end Postgres

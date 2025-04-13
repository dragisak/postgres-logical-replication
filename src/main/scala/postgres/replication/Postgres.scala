package postgres.replication

import com.typesafe.config.Config
import org.postgresql.PGProperty
import org.postgresql.jdbc.PgConnection
import org.postgresql.replication.{PGReplicationStream, ReplicationSlotInfo}
import org.slf4j.LoggerFactory

import java.sql.DriverManager
import java.util.Properties

class Postgres(config: Config) {
  private val logger = LoggerFactory.getLogger(getClass)

  def createConnection(): PgConnection = {

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
  }

  def createReplicationSlot(conn: PgConnection): ReplicationSlotInfo = {
    val replicationSlot = config.getString("replication-slot")
    val publication     = config.getString("publication")
    logger.info(s"Creating replication slot $replicationSlot")
    val slot = conn.getReplicationAPI
      .createReplicationSlot()
      .logical()
      .withSlotName(replicationSlot)
      .withOutputPlugin("pgoutput")
      .make()
    logger.info(s"Creating publication $publication")
    conn.execSQLUpdate(s"CREATE PUBLICATION $publication FOR ALL TABLES")
    slot
  }

  def dropReplicationSlot(conn: PgConnection): Unit = {
    val replicationSlot = config.getString("replication-slot")
    val publication     = config.getString("publication")
    logger.info(s"Dropping publication $publication")
    conn.execSQLUpdate(s"DROP PUBLICATION $publication")
    logger.info(s"Dropping replication slot $replicationSlot")
    conn.getReplicationAPI.dropReplicationSlot(replicationSlot)
  }

  def createStream(conn: PgConnection): PGReplicationStream = {
    val replicationSlot = config.getString("replication-slot")
    val publication     = config.getString("publication")
    logger.info(s"Creating stream")

    conn.getReplicationAPI
      .replicationStream()
      .logical()
      .withSlotName(replicationSlot)
      .withSlotOption("proto_version", 4)
      .withSlotOption("binary", false)
      .withSlotOption("messages", true)
      .withSlotOption("streaming", "parallel")
      .withSlotOption("publication_names", publication)
      .start()
  }
}

package postgresrep

import com.typesafe.config.ConfigFactory

import scala.util.Using
import scala.util.Using.Releasable

object Main extends App:

  private val config = ConfigFactory.load()

  private val pg = new Postgres(config.getConfig("postgres"))

  Using.resource(pg.createConnection()): conn =>
    pg.createReplicationSlot(conn)

end Main

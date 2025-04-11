package postgresrep

import com.typesafe.config.Config
import org.postgresql.{PGConnection, PGProperty}

import java.sql.DriverManager
import java.util.Properties

object Postgres:

  def getConnection(config: Config): PGConnection =

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
    con.unwrap(classOf[PGConnection])

  end getConnection

end Postgres

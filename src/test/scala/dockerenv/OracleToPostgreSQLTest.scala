package dockerenv

//import oracle.jdbc.datasource.OracleDataSource
import _root_.oracle.jdbc.pool.OracleDataSource
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.sql.{Connection, DatabaseMetaData, DriverManager}

class OracleToPostgreSQLTest extends AnyWordSpec with Matchers {
  "O2P" should {
    "work" in {
      val started = DockerEnv.oracle().start()
      println(s"Started oracle? $started")

      val url = "jdbc:oracle:thin:@//localhost:1521:xe"

      def example = {
        val username   = "sys as sysdba"
        val password   = "123456"
        val connection = DriverManager.getConnection(url, username, password)
      }

      {
        val ods = new OracleDataSource()
//        ods.setURL("jdbc:oracle:thin:scott/tiger@host:port/service")
        ods.setURL(url)
        ods.setUser("ADMIN")
        ods.setPassword("admin")
        val conn: Connection = ods.getConnection()

        // Create Oracle DatabaseMetaData object
        val meta: DatabaseMetaData = conn.getMetaData()

        // gets driver info:
        println("JDBC driver version is " + meta.getDriverVersion())
      }
    }
  }

}

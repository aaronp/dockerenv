package dockerenv

//import oracle.jdbc.datasource.OracleDataSource
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.{Success, Using}

class OracleToPostgreSQLTest extends AnyWordSpec with Matchers {
  "O2P" should {
    "work" in {
      DockerEnv.oracle().withLogger(dockerenv.stdOut).start()

      val Success(tables) = Using(OracleConf().connect) { conn =>
        import conn.session
        println("JDBC driver version is " + conn.metadata.getDriverVersion())

        WideTable.create shouldBe true
        conn.listTables
      }
      println(tables.mkString("\n"))
      tables.size should be > 0
    }
  }

}

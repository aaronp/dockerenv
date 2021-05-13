package dockerenv

//import oracle.jdbc.datasource.OracleDataSource
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Using

class OracleToPostgreSQLTest extends AnyWordSpec with Matchers {
  "O2P" should {
    "work" in {
      val started = DockerEnv.oracle().withLogger(dockerenv.stdOut).start()

      Using(OracleConf().connect) { conn =>
        println("JDBC driver version is " + conn.metadata.getDriverVersion())
      }
    }
  }

}

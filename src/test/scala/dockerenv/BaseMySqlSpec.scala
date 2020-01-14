package dockerenv

import org.scalatest.time.{Millis, Seconds, Span}

import scala.util.{Success, Try}

abstract class BaseMySqlSpec(docker: DockerEnv.Instance = DockerEnv.mysql()) extends BaseDockerSpec(docker) {

  implicit override def patienceConfig =
    PatienceConfig(timeout = scaled(Span(testTimeout.toSeconds, Seconds)), interval = scaled(Span(500, Millis)))

  def createDatabase(db: String): String = {
    eventually {
      val Success((0, out)) = MySql(dockerHandle).createDatabase(db)
      out
    }
  }

  def dropDatabase(db: String) = {
    eventually {
      val Success((0, out)) = MySql(dockerHandle).dropDatabase(db)
      out
    }
  }

  def mysqlExec(query: String): Try[(Int, String)] = MySql(dockerHandle).mysqlExec(query)

  def listDatabases(): List[String] = {
    eventually {
      val Success(dbs) = MySql(dockerHandle).listDatabases()
      dbs
    }
  }

  def listTables(database: String): List[String] = {
    eventually {
      val Success(tables) = MySql(dockerHandle).listTables(database)
      tables
    }
  }
}

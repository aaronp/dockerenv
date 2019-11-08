package dockerenv

import org.scalatest.time.{Millis, Seconds, Span}

import scala.util.{Success, Try}

abstract class BaseMySqlSpec extends BaseDockerSpec(DockerEnv.mysql()) {

  implicit override def patienceConfig =
    PatienceConfig(timeout = scaled(Span(testTimeout.toSeconds, Seconds)), interval = scaled(Span(500, Millis)))

  def createDatabase(db: String) = {
    eventually {
      val Success((0, out)) = dockerHandle.runInScriptDir("createDatabase.sh", db)
      out
    }
  }

  def dropDatabase(db: String) = {
    eventually {
      val Success((0, out)) = dockerHandle.runInScriptDir("dropDatabase.sh", db)
      out
    }
  }

  def mysqlExec(query: String): Try[(Int, String)] = {
    dockerHandle.runInScriptDir("mysqlExec.sh", query)
  }

  def listDatabases(): List[String] = {
    eventually {
      val Success((0, output)) = dockerHandle.runInScriptDir("showDatabases.sh")
      parseAsciiOut(output)
    }
  }

  private def parseAsciiOut(out: String) = {
    val DbNameR = """.*\| *(.*) *\|.*""".r
    val names = out.linesIterator.collect {
      case DbNameR(name) => name.trim
    }

    /**
      * drop the 'Database' entry from the mysql 'show databases' output, which looks like this:
      * {{{
      * +--------------------+
      * | Database           |
      * +--------------------+
      * | foo                |
      * | bar                |
      * +--------------------+
      * }}}
      */
    val list = names.toList
    list.tail
  }

  def listTables(database: String): List[String] = {
    eventually {
      val query              = s"USE ${database}; SHOW TABLES;"
      val Success((0, tbls)) = mysqlExec(query)
      parseAsciiOut(tbls)
    }
  }
}

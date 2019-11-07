package dockerenv

import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.duration._
import scala.util.{Success, Try}

abstract class BaseMySqlSpec extends BaseDockerSpec(DockerEnv.mysql()) {

  implicit override def patienceConfig =
    PatienceConfig(timeout = scaled(Span(testTimeout.toSeconds, Seconds)), interval = scaled(Span(500, Millis)))

  def createDatabase(db: String): Try[(Int, String)] = {
    dockerHandle.runInScriptDir("createDatabase.sh", db)
  }

  def dropDatabase(db: String): Try[(Int, String)] = {
    dockerHandle.runInScriptDir("dropDatabase.sh", db)
  }

  def listDatabases() = {
    val out = eventually {
      val Success((0, output)) = dockerHandle.runInScriptDir("showDatabases.sh")
      output
    }

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
    names.toSet - "Database"
  }
}

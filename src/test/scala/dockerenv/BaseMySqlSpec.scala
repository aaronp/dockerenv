package dockerenv

import scala.util.{Success, Try}

abstract class BaseMySqlSpec extends BaseDockerSpec(DockerEnv.mysql()) {
  def createDatabase(db: String): Try[(Int, String)] = {
    dockerHandle.runInScriptDir("createDatabase.sh", db)
  }

  def dropDatabase(db: String): Try[(Int, String)] = {
    dockerHandle.runInScriptDir("dropDatabase.sh", db)
  }

  def listDatabases() = {
    val Success((0, out)) = dockerHandle.runInScriptDir("showDatabases.sh")

    val DbNameR = """.*\| *(.*) *\|.*""".r
    /**
      * drop the:
      * {{{
      * +--------------------+
      * | Database           |
      * +--------------------+
      * }}}
      */
    val names = out.linesIterator.collect {
      case DbNameR(name) => name.trim
    }
    names.toSet - "Database"
  }
}

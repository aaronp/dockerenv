package dockerenv

import scala.util.{Failure, Success, Try}

final case class MySql(dockerHandle: DockerEnv.Instance) {

  def createDatabase(db: String) = dockerHandle.runInScriptDir("createDatabase.sh", db)

  def dropDatabase(db: String) = dockerHandle.runInScriptDir("dropDatabase.sh", db)

  def mysqlExec(query: String): Try[(Int, String)] = dockerHandle.runInScriptDir("mysqlExec.sh", query)

  def listDatabases(): Try[List[String]] = {
    val result = dockerHandle.runInScriptDir("showDatabases.sh")
    parseTabularResult(result)
  }

  def parseTabularResult(result: Try[(Int, String)]) = {
    result match {
      case Success((0, output))    => Success(parseAsciiOut(output))
      case Success((code, output)) => Failure(new Exception(s"showDatabases.sh completed w/ $code: $output"))
      case Failure(err)            => Failure(err)
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

  def listTables(database: String) = {
    val result = mysqlExec(s"USE ${database}; SHOW TABLES;")
    parseTabularResult(result)
  }
}

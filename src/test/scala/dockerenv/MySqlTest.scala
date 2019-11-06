package dockerenv

import cats.effect.IO
import doobie._

import scala.concurrent.ExecutionContext
class MySqlTest extends BaseMySqlSpec {

  implicit val cs = IO.contextShift(ExecutionContext.global)

  "dockerenv.mySql" should {
    "connect to mysql" in {
      lazy val postgres = Transactor.fromDriverManager[IO](
        "org.postgresql.Driver",
        "jdbc:postgresql:world",
        "postgres",
        ""
      )
//      val d : com.mysql.jdbc.Driver = ???

      val xa = Transactor.fromDriverManager[IO](
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://localhost/test?user=minty&password=greatsqldb",
        "postgres",
        ""
      )

    }
  }
}

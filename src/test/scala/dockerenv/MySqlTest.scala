package dockerenv

import cats.effect.{ContextShift, IO}
import doobie.implicits._
import doobie.util.transactor.Transactor

import scala.concurrent.ExecutionContext
import scala.util.Success

class MySqlTest extends BaseMySqlSpec {

  "dockerenv.createDatabase" should {
    "be able to create a database" in {
      implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

      val name = s"testDb${System.currentTimeMillis()}"
      isDockerRunning() shouldBe true

      listDatabases() should contain allElementsOf List("mysql", "information_schema")
      listDatabases() should not contain (name)
      val Success((0, msg)) = createDatabase(name)
      msg should include(s"CREATING $name")

      listDatabases() should contain(name)

      val xa = Transactor.fromDriverManager[IO](
        "com.mysql.cj.jdbc.Driver",
        s"jdbc:mysql://localhost:7777/$name?useSSL=false&allowPublicKeyRetrieval=true",
        "root",
        "docker"
      )

      val io: doobie.ConnectionIO[Int] = sql"CREATE TABLE FOO(name VARCHAR(20))".update.run
      io.transact(xa).unsafeRunSync shouldBe 0

      val Success((0, _)) = dropDatabase(name)
      listDatabases() should not contain (name)
    }
  }
}

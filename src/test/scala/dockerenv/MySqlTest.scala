package dockerenv

import scala.concurrent.duration._
import scala.util.Success

class MySqlTest extends BaseMySqlSpec {

  // wow - mysql fails to connect for AGES with:
  // "ERROR 2002 (HY000): Can't connect to local MySQL server through socket '/var/run/mysqld/mysqld.sock' (2)"
  override def testTimeout: FiniteDuration = 2.minutes

  "dockerenv.createDatabase" should {
    "be able to create a database" in {

      val name = s"testDb${System.currentTimeMillis()}"
      isDockerRunning() shouldBe true

      listDatabases() should contain allElementsOf List("mysql", "information_schema")
      listDatabases() should not contain (name)
      val msg = createDatabase(name)
      msg should include(s"CREATING $name")

      listDatabases() should contain(name)

      val Success((0, _)) = mysqlExec(s"CREATE TABLE IF NOT EXISTS ${name}.FOO (SOME_ID VARCHAR(20) PRIMARY KEY, NAME VARCHAR(20) NULL)")
      listTables(name) should contain only ("FOO")

      dropDatabase(name)
      listDatabases() should not contain (name)
    }
  }
}

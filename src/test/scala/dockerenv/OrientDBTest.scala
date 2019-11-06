package dockerenv

import scala.util.Success

class OrientDBTest extends BaseOrientDBSpec {

  "OrientDB" should {
    "allow us to exec orientdb commands" in {
      isDockerRunning() shouldBe true

      val Success((_, listOutput)) = dockerHandle.runInScriptDir("listDatabases.sh")
      listOutput should include("orientdb> LIST DATABASES")
    }
  }
}

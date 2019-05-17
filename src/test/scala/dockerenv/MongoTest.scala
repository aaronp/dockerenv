package dockerenv

import scala.util.Success

class MongoTest extends BaseMongoSpec {

  "BaseMongoSpec" should {
    "allow us to connect to the running mongo container" in insideRunningEnvironment {
      isDockerRunning() shouldBe true

      val Success((0, createOutput)) = dockerEnv.runInScriptDir("createUser.sh")
      createOutput should include("serviceUser")
    }
  }
}

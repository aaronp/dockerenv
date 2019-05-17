package dockerenv

import scala.util.Success

class MongoTest extends BaseMongoSpec {

  "BaseMongoSpec" should {
    "allow us to connect to the running mongo container and ensure the service user exists" in insideRunningEnvironment {
      isDockerRunning() shouldBe true

      val Success((0, listOutput)) = dockerEnv.runInScriptDir("mongo.sh", "listUsers.js")

      if (!listOutput.contains("serviceUser")) {
        val Success((0, createOutput)) = dockerEnv.runInScriptDir("mongo.sh", "createUser.js")
        createOutput should include("serviceUser")
      } else {
        val Success((0, secondListOutput)) = dockerEnv.runInScriptDir("mongo.sh", "listUsers.js")
        secondListOutput should include("serviceUser")
      }
    }
  }
}

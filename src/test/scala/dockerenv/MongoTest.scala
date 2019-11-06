package dockerenv

import scala.util.Success

class MongoTest extends BaseMongoSpec {

  "BaseMongoSpec" should {
    "allow us to connect to the running mongo container and ensure the service user exists" in insideRunningEnvironment {
      isDockerRunning() shouldBe true

      val listOutput = eventually {
        val Success((0, output)) = MongoEnv.listUsers(dockerHandle)
        output
      }

      if (!listOutput.contains("serviceUser")) {
        val Success((0, createOutput)) = MongoEnv.createUser(dockerHandle)
        createOutput should include("serviceUser")
      } else {
        val Success((0, secondListOutput)) = MongoEnv.listUsers(dockerHandle)
        secondListOutput should include("serviceUser")
      }
    }
  }
}

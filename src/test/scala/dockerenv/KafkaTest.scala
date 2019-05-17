package dockerenv

import scala.util.Success

class KafkaTest extends BaseKafkaSpec {

  "BaseKafkaSpec" should {
    "allow us to connect to the running kafka container" in insideRunningEnvironment {
      isDockerRunning() shouldBe true

      //
      // When trying to create a topic, I've seen:
      // Replication factor: 1 larger than available brokers: 0
      //
      // So there may be some work there. But for now we just want to be sure kafka is at least running
      //
      //      val topic = randomString()
      //      val Success((0, createOutput)) = dockerEnv.runInScriptDir("createTopic.sh", topic)
      //
      //

      val Success((0, listOutput)) = dockerEnv.runInScriptDir("listTopics.sh")
      listOutput should include("topics for test-kafka are")
    }
  }
}

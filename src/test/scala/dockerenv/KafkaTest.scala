package dockerenv

import scala.util.Success

class KafkaTest extends BaseKafkaSpec {

  "Kafka" should {
    "run" in insideRunningEnvironment {
      isDockerRunning() shouldBe true

      val topic = randomString()

      val Success((0, createOutput)) = dockerEnv.runInScriptDir("createTopic.sh", topic)
      println(createOutput)
      val Success((0, listOutput)) = dockerEnv.runInScriptDir("listTopics.sh")
      println(listOutput)
      listOutput should include(topic)
      //      ServiceHook.run("./kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic foo")
    }
  }
}
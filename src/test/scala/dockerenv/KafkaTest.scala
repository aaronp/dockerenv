package dockerenv

import scala.collection.mutable.ListBuffer
import scala.util.Success

class KafkaTest extends BaseKafkaSpec {

  "BaseKafkaSpec.withEnv(...).withLogger(...)" should {
    "allow us to control the mount-point via the 'PROJECT_DIR' env property" in {
      val dir    = "someDir"
      val output = ListBuffer[String]()
      val e = dockerHandle.withEnv("PROJECT_DIR" -> dir).withLogger { out =>
        output ++= out.linesIterator
      }

      e.stop()
      e.start()
      e.isRunning() shouldBe (true)
      val Some(projectDirIsLineFromTheIsDockerRunningScript) = output.find(_.contains("PROJECT_DIR is "))
      KafkaTestAccessor.linesHead(projectDirIsLineFromTheIsDockerRunningScript) shouldBe s"PROJECT_DIR is $dir"

    }
  }
  "BaseKafkaSpec" should {
    "allow us to connect to the running kafka container" in insideRunningEnvironment {
      isDockerRunning() shouldBe true

      //
      // When trying to create a topic, I've seen:
      // Replication factor: 1 larger than available brokers: 0
      //
      // So there may be some work there. But for now we just want to be sure kafka is at least running
      val topic = randomString()
      eventually {
        val Success((0, createOutput)) = dockerHandle.runInScriptDir("createTopic.sh", topic)
        createOutput should include(s"Creating topic '$topic' in test-kafka")
      }

      val Success((0, listOutput)) = dockerHandle.runInScriptDir("listTopics.sh")
      listOutput should include("topics for test-kafka are")
      listOutput should include(topic)
    }
  }
}

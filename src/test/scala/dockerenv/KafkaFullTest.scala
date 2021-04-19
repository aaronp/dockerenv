package dockerenv

import scala.collection.mutable.ListBuffer
import scala.util.Success

class KafkaFullTest extends BaseKafkaFullSpec {

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
    }
  }
}

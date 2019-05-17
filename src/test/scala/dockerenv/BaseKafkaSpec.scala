package dockerenv

/**
  * Represents a test which needs Kafka Running
  */
abstract class BaseKafkaSpec extends BaseDockerSpec("scripts/kafka")
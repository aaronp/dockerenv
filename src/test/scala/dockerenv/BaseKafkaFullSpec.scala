package dockerenv

/**
  * Represents a test which needs Kafka Running
  */
abstract class BaseKafkaFullSpec(docker: DockerEnv.Instance = DockerEnv.kafkaFull()) extends BaseDockerSpec(docker)

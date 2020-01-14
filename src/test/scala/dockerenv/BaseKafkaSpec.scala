package dockerenv

/**
  * Represents a test which needs Kafka Running
  */
abstract class BaseKafkaSpec(docker: DockerEnv.Instance = DockerEnv.kafka()) extends BaseDockerSpec(docker)

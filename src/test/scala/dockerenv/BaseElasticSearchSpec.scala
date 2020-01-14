package dockerenv

/**
  * Represents a test which needs Kafka Running
  */
abstract class BaseElasticSearchSpec(docker : DockerEnv.Instance = DockerEnv.elasticSearch()) extends BaseDockerSpec(docker)

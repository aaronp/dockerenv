package dockerenv

abstract class BaseMongoSpec(docker : DockerEnv.Instance = DockerEnv.mongo()) extends BaseDockerSpec(docker)

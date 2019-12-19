package dockerenv

abstract class BasePostgresSpec(docker : DockerEnv.Instance = DockerEnv.postgres()) extends BaseDockerSpec(docker)

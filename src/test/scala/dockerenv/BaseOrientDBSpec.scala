package dockerenv

abstract class BaseOrientDBSpec(docker: DockerEnv.Instance = DockerEnv.orientDb()) extends BaseDockerSpec(docker)

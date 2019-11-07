package dockerenv

import java.nio.file.{Files, Paths}

import dockerenv.logging.BufferLogger

import scala.sys.process
import scala.util.{Failure, Success, Try}

/**
  * The 'DockerEnv' represents a handle onto environment scripts which can be started, stopped and checked to see if
  * they're running.
  *
  */
trait DockerEnv {

  /** @return true if we think Docker is running
    */
  def isRunning(): Boolean

  /** @return true if this command succeeded, false otherwise
    */
  def start(): Boolean

  /** @return true if this command succeeded, false otherwise
    */
  def stop(): Boolean

  /**
    * Invoke the 'thunk' within the context of the service running
    *
    * @param thunk
    * @tparam T
    * @return
    */
  final def bracket[T](thunk: => T): T = {
    if (!isRunning()) {
      start()
      try {
        thunk
      } finally {
        stop()
      }
    } else {
      thunk
    }
  }
}

object DockerEnv {

  /** Convenience method for returning kafka services
    *
    * @param workDir the directory under which the docker run scripts should be extracted
    * @return a kafka environment
    */
  def kafka(workDir: String = DefaultWorkDir) = dockerenv.kafka(workDir)

  /** Convenience method for returning orientdb services
    *
    * @param workDir the directory under which the docker run scripts should be extracted
    * @return a orientdb environment
    */
  def orientDb(workDir: String = DefaultWorkDir) = dockerenv.orientdb(workDir)

  /** Convenience method for returning mongo services
    *
    * @param workDir the directory under which the docker run scripts should be extracted
    * @return a mongo environment
    */
  def mongo(workDir: String = DefaultWorkDir) = dockerenv.mongo(workDir)

  /** Convenience method for returning postgres services
    *
    * @param workDir the directory under which the docker run scripts should be extracted
    * @return a mongo environment
    */
  def postgres(workDir: String = DefaultWorkDir) = dockerenv.postgres(workDir)

  /** Convenience method for returning postgres services
    *
    * @param workDir the directory under which the docker run scripts should be extracted
    * @return a mongo environment
    */
  def mysql(workDir: String = DefaultWorkDir): Instance = dockerenv.mysql(workDir)

  /**
    * Creates a 'DockerEnv' for the given script location (e.g. 'scripts/kafka').
    *
    * This will entail having to extract the scripts from the 'dockerenv' jar dependency under the 'workDir'
    *
    * @param scriptDir the relative path to the scripts (e.g. 'scripts/kafka')
    * @param workDir   the local working directory to extract the files to
    * @return a new DockerEnv instance
    */
  def apply(scriptDir: String, workDir: String = DefaultWorkDir, logger: Logger = defaultLogger) = {
    dockerenv.envFor(scriptDir, workDir, logger)
  }

  private[dockerenv] def newInstance(scriptDir: String, logger: Logger): Instance = {
    new Instance(scriptDir, Map.empty, logger)
  }

  final case class Instance(scriptDir: String, extraEnv: Map[String, String], scriptLogger: Logger) extends DockerEnv {

    override def isRunning(): Boolean = {
      runInScriptDir("isDockerRunning.sh").toOption.exists {
        case (_, output) =>
          output.contains("docker image ") && output.contains(" is running")
      }
    }

    def withLogger(logger: Logger) = copy(scriptLogger = logger)

    def withEnv(env: Map[String, String]): Instance = copy(extraEnv = extraEnv ++ env)

    def withEnv(first: (String, String), env: (String, String)*): Instance = withEnv((first +: env).toMap)

    override def start(): Boolean = runInScriptDir("startDocker.sh").isSuccess

    override def stop(): Boolean = runInScriptDir("stopDocker.sh").isSuccess

    def runInScriptDir(script: String, args: String*): Try[(Int, String)] = tryRunScript(s"$scriptDir/$script", args: _*)

    def tryRunScript(script: String, args: String*): Try[(Int, String)] = {
      tryRunScript(script, args.toSeq, Map[String, String]())
    }

    def tryRunScript(script: String, args: Seq[String], env: Map[String, String]): Try[(Int, String)] = {
      scriptLogger(args.mkString(script + " ", " ", " returned:"))
      Try(run(script, args, extraEnv ++ env)) match {
        case res @ Success((returnCode, output)) =>
          scriptLogger(s"return code $returnCode : $output")
          res
        case res @ Failure(error) =>
          scriptLogger(error.toString)
          res
      }
    }
  }

  def run(script: String, args: Seq[String], env: Map[String, String]): (Int, String) = {
    val buffer                          = new BufferLogger(s"$script: ")
    val builder: process.ProcessBuilder = parseScript(script, args, env)
    val res                             = builder.run(buffer)
    res.exitValue() -> buffer.output
  }

  /**
    * resolves the script and returns a process which will run in the script's parent directory
    *
    * @param script
    * @param args
    * @return
    */
  def parseScript(script: String, args: Seq[String], env: Map[String, String]): process.ProcessBuilder = {
    import java.nio.file.attribute.PosixFilePermission

    import sys.process._

    val (path, fileName) = getClass.getClassLoader.getResource(script) match {
      case location if location != null =>
        val path = Paths.get(location.toURI)
        import scala.collection.JavaConverters._
        Files.setPosixFilePermissions(path, PosixFilePermission.values().toSet.asJava)
        path -> s"./${path.getFileName}"
      case _ if Files.exists(Paths.get(script)) =>
        val path = Paths.get(script)
        path -> path.toAbsolutePath.toString
      case _ => sys.error(s"Couldn't resolve '${script}' on the classpath or file system")
    }

    Process(fileName +: args, path.getParent.toFile, env.toSeq: _*)
  }
}

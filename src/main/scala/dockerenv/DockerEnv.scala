package dockerenv

import java.nio.file.{Files, Path, Paths}

import scala.collection.mutable.ArrayBuffer
import scala.sys.process
import scala.sys.process.ProcessLogger
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

  def apply(scriptDir: String, scriptLogger: String => Unit = println(_: String)) = new Instance(scriptDir)

  class Instance(scriptDir: String, scriptLogger: String => Unit = println(_: String)) extends DockerEnv {
    override def isRunning(): Boolean = {
      tryRunScript(s"$scriptDir/isDockerRunning.sh").toOption.exists {
        case (_, output) =>
          output.contains("docker image ") && output.contains(" is running")
      }
    }

    override def start(): Boolean = runInScriptDir("startDocker.sh").isSuccess

    override def stop(): Boolean = runInScriptDir("stopDocker.sh").isSuccess

    def runInScriptDir(script: String, args: String*): Try[(Int, String)] = tryRunScript(s"$scriptDir/$script", args: _*)

    def tryRunScript(script: String, args: String*): Try[(Int, String)] = {
      scriptLogger(args.mkString(script + " ", " ", " returned:"))
      Try(run(script, args: _*)) match {
        case res @ Success(output) =>
          scriptLogger(output.toString())
          res
        case res @ Failure(output) =>
          scriptLogger(output.toString())
          res
      }
    }
  }

  def run(script: String, args: String*): (Int, String) = {
    val buffer                          = new BufferLogger(s"$script: ")
    val builder: process.ProcessBuilder = parseScript(script, args.toSeq)
    val res                             = builder.run(buffer)
    res.exitValue() -> buffer.output
  }

  private def parseScript(script: String, args: Seq[String]): process.ProcessBuilder = {
    import java.nio.file.attribute.PosixFilePermission
    val location = getClass.getClassLoader.getResource(script)
    require(location != null, s"Couldn't find $script")
    import sys.process._

    val scriptLoc: Path = Paths.get(location.toURI)
    import scala.collection.JavaConverters._
    Files.setPosixFilePermissions(scriptLoc, PosixFilePermission.values.toSet.asJava)
    val scriptFile = s"./${scriptLoc.getFileName.toString}"
    Process(scriptFile +: args, scriptLoc.getParent.toFile)
  }

  private class BufferLogger(prefix: String) extends ProcessLogger {
    private val outputBuffer = ArrayBuffer[String]()

    def output = outputBuffer.mkString("\n")

    override def out(s: => String): Unit = {
      outputBuffer.append(s)
    }

    override def err(s: => String): Unit = {
      outputBuffer.append(s"ERR: $s")
    }

    override def buffer[T](f: => T): T = f
  }

}

import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{FileAlreadyExistsException, Files, Path, Paths}
import java.util.zip.ZipInputStream

package object dockerenv {

  type Logger = String => Unit

  def mysql(workDir: String = DefaultWorkDir, logger: Logger = defaultLogger): DockerEnv.Instance = envFor("scripts/mysql", workDir, logger)

  def postgres(workDir: String = DefaultWorkDir, logger: Logger = defaultLogger): DockerEnv.Instance = envFor("scripts/postgres", workDir, logger)

  object using {
    def apply[A <: AutoCloseable, T](resource: A)(thunk: A => T): T = {
      try {
        thunk(resource)
      } finally {
        resource.close()
      }
    }
  }

  def elasticSearch(workDir: String = DefaultWorkDir, logger: Logger = defaultLogger): DockerEnv.Instance = envFor("scripts/elasticsearch", workDir, logger)

  def kafka(workDir: String = DefaultWorkDir, logger: Logger = defaultLogger): DockerEnv.Instance = envFor("scripts/kafka", workDir, logger)

  def mongo(workDir: String = DefaultWorkDir, logger: Logger = defaultLogger): DockerEnv.Instance = envFor("scripts/mongo", workDir, logger)

  def mqtt(workDir: String = DefaultWorkDir, logger: Logger = defaultLogger): DockerEnv.Instance = envFor("scripts/mqtt", workDir, logger)

  def orientdb(workDir: String = DefaultWorkDir, logger: Logger = defaultLogger): DockerEnv.Instance = envFor("scripts/orientdb", workDir, logger)

  def envFor(scriptDir: String, workDir: String, logger: Logger): DockerEnv.Instance = {

    val scriptWeCanAssumeIsThere = s"$scriptDir/isDockerRunning.sh"
    val JarPath                  = ("jar:file:(.*)!/" + scriptWeCanAssumeIsThere).r

    val url = {
      val scriptUrl = getClass.getClassLoader.getResource(scriptWeCanAssumeIsThere)
      if (scriptUrl == null) {
        val source = getClass.getProtectionDomain.getCodeSource
        source.getLocation
      } else {
        scriptUrl
      }
    }
    require(url != null, s"Couldn't find $scriptWeCanAssumeIsThere on the classpath")
    val toDir = Paths.get(workDir)
    url.toString match {
      case JarPath(pathToJar) =>
        val extractedScriptsDir = extractScriptsFromJar(pathToJar, toDir).resolve(scriptDir)
        DockerEnv.newInstance(extractedScriptsDir.toAbsolutePath.toString, logger)
      case _ => DockerEnv.newInstance(scriptDir, logger)
    }
  }

  /**
    * log to std output. see [[defaultLogger]]
    */
  lazy val stdOut: Logger = println(_: String)

  /**
    * An ignoring logging function. see [[defaultLogger]]
    */
  lazy val devNull: Logger = (_: String) => {}

  /**
    * A default logger, which can be *ahem* globally replaced if needed.
    *
    * I didn't want to bind any additional dependencies to this project, including loggers, even slf4j ones.
    *
    * You could explicitly pass a logging function to a dockerenv.* function, or set the global default:
    *
    * {{{
    *   dockerenv.defaultLogger = dockerenv.stdOut
    * }}}
    */
  var defaultLogger: Logger = devNull

  /**
    * The default location under which scripts are extracted
    */
  lazy val DefaultWorkDir = sys.env.getOrElse("DockerEnvWorkDir", "target/docker-env")

  /**
    * We won't be able to execute the .sh scripts (or address anything else) while they're in the dockerenv jar,
    * so this script explodes the resource artifacts from that jar into the 'toDir' location (which is presumably /target/dockerenv
    * or something)
    *
    * @param fromPath the path to the jar artifact
    * @param toDir    the local destination directory
    * @return the extracted location (same as the 'toDir')
    */
  def extractScriptsFromJar(fromPath: String, toDir: Path): Path = {
    val fromFile = Paths.get(fromPath)

    def mkDirs(path: Path) = {
      if (!Files.isDirectory(path)) {
        Files.createDirectories(path)
      }
    }

    mkDirs(toDir)

    val jarDest = toDir.resolve(fromFile.getFileName.toString)
    try {
      Files.copy(fromFile, toDir.resolve(fromFile.getFileName.toString))
    } catch {
      case _: FileAlreadyExistsException =>
    }

    using(new ZipInputStream(Files.newInputStream(jarDest))) { is: ZipInputStream =>
      var entry = is.getNextEntry
      while (entry != null) {
        if (entry.getName.contains("script") && !entry.isDirectory) {
          val target = toDir.resolve(entry.getName)
          if (!Files.exists(target)) {
            mkDirs(target.getParent)
            Files.copy(is, target)

            // our noddy scripts are ok for 777
            import scala.collection.JavaConverters._
            Files.setPosixFilePermissions(target, PosixFilePermission.values().toSet.asJava)
          }
        }
        is.closeEntry
        entry = is.getNextEntry
      }
    }
    toDir
  }
}

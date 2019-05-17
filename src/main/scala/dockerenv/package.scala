import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{FileAlreadyExistsException, Files, Path, Paths}
import java.util.zip.ZipInputStream

import dockerenv.DockerEnv.defaultLogger

package object dockerenv {


  object using {
    def apply[A <: AutoCloseable, T](resource: A)(thunk: A => T): T = {
      try {
        thunk(resource)
      } finally {
        resource.close()
      }
    }
  }

  def envFor(scriptDir: String, workDir: String = "target/docker-env", scriptLogger: String => Unit = defaultLogger): DockerEnv.Instance = {

    val scriptWeCanAssumeIsThere = s"$scriptDir/isDockerRunning.sh"
    val JarPath = ("jar:file:(.*)!/" + scriptWeCanAssumeIsThere).r

    val url = getClass.getClassLoader.getResource(scriptWeCanAssumeIsThere)
    require(url != null, s"Couldn't find $scriptWeCanAssumeIsThere on the classpath")
    val toDir = Paths.get(workDir)
    url.toString match {
      case JarPath(pathToJar) =>
        val extractedScriptsDir = extractScriptsFromJar(pathToJar, toDir).resolve(scriptDir)
        DockerEnv.newInstance(extractedScriptsDir.toAbsolutePath.toString, scriptLogger)
      case _ => DockerEnv.newInstance(scriptDir, scriptLogger)
    }
  }

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

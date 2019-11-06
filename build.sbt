import org.scoverage.coveralls.Imports.CoverallsKeys._
import sbtbuildinfo.BuildInfoPlugin.autoImport.buildInfoPackage

name := "dockerenv"

organization := "com.github.aaronp"

enablePlugins(GhpagesPlugin)
enablePlugins(ParadoxPlugin)
enablePlugins(SiteScaladocPlugin)
enablePlugins(ParadoxMaterialThemePlugin)
enablePlugins(BuildInfoPlugin)

val scalaThirteen = "2.13.0"
crossScalaVersions := Seq(scalaThirteen, "2.12.10")
scalaVersion := "2.12.10"

paradoxProperties += ("project.url" -> "https://aaronp.github.io/dockerenv/docs/current/")

Compile / paradoxMaterialTheme ~= {
  _.withLanguage(java.util.Locale.ENGLISH)
    .withColor("red", "orange")
    .withLogoIcon("cloud")
    .withRepository(uri("https://github.com/aaronp/dockerenv"))
    .withSocial(uri("https://github.com/aaronp"))
    .withoutSearch()
}

//scalacOptions += Seq("-encoding", "UTF-8")

siteSourceDirectory := target.value / "paradox" / "site" / "main"

siteSubdirName in SiteScaladoc := "api/latest"

libraryDependencies ++= List(
  "mysql"         % "mysql-connector-java" % "8.0.15"          % "test",
  "postgresql"    % "postgresql"           % "9.1-901-1.jdbc4" % "test",
  "org.tpolecat"  %% "doobie-core"         % "0.8.4"           % "test",
  "org.scalactic" %% "scalactic"           % "3.0.8"           % "test",
  "org.scalatest" %% "scalatest"           % "3.0.8"           % "test",
  "org.pegdown"   % "pegdown"              % "1.6.0"           % "test",
  "junit"         % "junit"                % "4.12"            % "test"
)

publishMavenStyle := true
releaseCrossBuild := true
coverageMinimum := 90
coverageFailOnMinimum := true
git.remoteRepo := s"git@github.com:aaronp/dockerenv.git"
ghpagesNoJekyll := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)

publishArtifact in Test := true

test in assembly := {}
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

// https://coveralls.io/github/aaronp/dockerenv
// https://github.com/scoverage/sbt-coveralls#specifying-your-repo-token
coverallsTokenFile := Option((Path.userHome / ".sbt" / ".coveralls.dockerenv").asPath.toString)

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
buildInfoPackage := "dockerenv.build"

// see http://scalameta.org/scalafmt/
scalafmtOnCompile in ThisBuild := true
scalafmtVersion in ThisBuild := "1.4.0"

// see http://www.scalatest.org/user_guide/using_scalatest_with_sbt
testOptions in Test += (Tests.Argument(TestFrameworks.ScalaTest, "-h", s"target/scalatest-reports", "-oN"))

pomExtra := {
  <url>https://github.com/aaronp/dockerenv</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <developers>
      <developer>
        <id>Aaron</id>
        <name>Aaron Pritzlaff</name>
        <url>http://github.com/aaronp</url>
      </developer>
    </developers>
}

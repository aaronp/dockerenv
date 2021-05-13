import org.scoverage.coveralls.Imports.CoverallsKeys._
//import sbtbuildinfo.BuildInfoPlugin.autoImport.buildInfoPackage

name := "dockerenv"

organization := "com.github.aaronp"

enablePlugins(GhpagesPlugin)
enablePlugins(ParadoxPlugin)
enablePlugins(SiteScaladocPlugin)
enablePlugins(ParadoxMaterialThemePlugin)
//enablePlugins(BuildInfoPlugin)

val scala13 = "2.13.5"
crossScalaVersions := Seq(scala13)
scalaVersion := scala13

paradoxProperties += ("project.url" -> "https://aaronp.github.io/dockerenv/docs/current/")

Compile / paradoxMaterialTheme ~= {
  _.withLanguage(java.util.Locale.ENGLISH)
    .withColor("red", "orange")
    .withRepository(uri("https://github.com/aaronp/dockerenv"))
    .withSocial(uri("https://github.com/aaronp"))
    .withoutSearch()
}

//scalacOptions += Seq("-encoding", "UTF-8")

siteSourceDirectory := target.value / "paradox" / "site" / "main"

siteSubdirName in SiteScaladoc := "api/latest"

// see
// https://repo1.maven.org/maven2/com/oracle/ojdbc/
// https://medium.com/oracledevs/your-own-way-oracle-jdbc-drivers-19-7-0-0-on-maven-central-9a7dbb648995
val dbTestDeps = List(
  "com.oracle.database.jdbc" % "ojdbc8" % "19.7.0.0" % "test"
//  "com.oracle.jdbc" % "ojdbc-bom" % "19.3.0.0"   % "test"
//  "com.oracle" % "classes12" % "10.2.0.2.0" % "test"
)

val scalaJDBC = Seq(
  "org.scalikejdbc" %% "scalikejdbc"      % "3.5.0" % "test",
  "org.scalikejdbc" %% "scalikejdbc-test" % "3.5.0" % "test",
  "ch.qos.logback"  % "logback-classic"   % "1.2.3" % "test"
)

libraryDependencies ++= dbTestDeps ++ scalaJDBC

libraryDependencies ++= List(
  "org.scalactic"        %% "scalactic"   % "3.2.2"   % "test",
  "org.scalatest"        %% "scalatest"   % "3.2.2"   % "test",
  "org.pegdown"          % "pegdown"      % "1.6.0"   % "test",
  "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % "test",
  "junit"                % "junit"        % "4.13"    % "test"
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

//buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
//buildInfoPackage := "dockerenv.buildinfo"

// see http://scalameta.org/scalafmt/
scalafmtOnCompile in ThisBuild := true
ThisBuild / scalafmtVersion := "1.4.0"

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

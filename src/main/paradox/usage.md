## Usage

The goal is to ensure some docker service is running/available, typically for running tests.

The main published resource is just the 'DockerEnv' trait, which provides a hook to start, stop and check running containers.

This repo also contains a collection of common scripts for using different containers (mongo, kafka, orientdb, etc).

As the primary use-case is really for testing, you will typically want to depend on the test-artifact like this:

```scala
libraryDependencies += "com.github.aaronp" %% "dockerenv" % "latest version" % "test" classifier "tests"
libraryDependencies += "com.github.aaronp" %% "dockerenv" % "latest version" % "test" 
``` 


You could also use this for running example applications like this:

assuming a project layout where 'Main' is an application which talks to mongo: 
 * src/main/scala/myapp/Main.scala 
 * src/test/scala/myapp/DevMain.scala
 
You could create a 'DevMain' to as a convenience to run your app stand-alone:

```scala
object DevMain {
  def main(args :Array[Sring]) : Unit = {

     // make sure mongo is running while my app is:
      dockerenv.postgres().bracket { // the postgres DB is started here if it wasn't already running
        dockerenv.mysql().bracket { 
          // both postgres and mysql DB is started here if it wasn't already running
        }
        // mysql has been stopped, unless it was running
      }
  }
}


```

Otherwise you would typically do this in tests themselves. There are 'BaseXXXSpec' abstract classes for the supported
docker services which you can extend (e.g. see BaseKafkaSpec, BaseMongoSpec, BaseMySqlSpec, etc)

We can then write tests like this:

```scala
class KafkaTest extends BaseKafkaSpec {

  "Kafka" should {
    "run" in {
      isDockerRunning() shouldBe true

      val Success((0, listOutput)) = dockerHandle.runInScriptDir("listTopics.sh")
      listOutput should include(topic)
    }
  }
}

```

NOTE: As we're starting/stopping external resources, you will want to ensure these sorts of tests are NOT run in parallel!

You could tag them as integration tests, or just put your integration tests in a separate test module.

## Usage

dockerenv is just a thin script wrapper which can ensure some docker container is started/stopped, typically to support real integration testing rather than
having to mock things out.

As the primary use-case is really for testing, you may want to include a dependency on the "test" artifact as well as the main one:

```scala
libraryDependencies += "com.github.aaronp" %% "dockerenv" % "latest version" % "test" classifier "tests"
libraryDependencies += "com.github.aaronp" %% "dockerenv" % "latest version" % "test" 
``` 

 
In doing so this gives you access to the 'BaseXXXSpec' traits (e.g. BaseKafkaSpec) which ensures that kafka is started/stopped for those tests:

```scala
class MyKafkaTest extends BaseKafkaSpec {

  "Kafka" should {
    "connect to a running kafka instance" in {
      isDockerRunning() shouldBe true
 
      val topic = "testTopic"
  

      // insert your code here which needs to do something with kafka (e.g. publish/consume some data)
      // here we just execute a script within the kafka container to demonstrate it's running by invoking 
      // 'kafka-topics.sh' script within the container via our listTopics.sh wrapper 
      val Success((0, listOutput)) = dockerHandle.runInScriptDir("listTopics.sh")
      listOutput should not be(empty)
    }
  }
}
```

### NOTE: 
As we're starting/stopping external resources, you will want to ensure these sorts of tests are NOT run in parallel!

You could tag them as integration tests, or just put your integration tests in a separate test module.

You don't need to use those ScalaTest convenience wrappers, however. Just depending on dockerenv gives you access to the currently
supported containers. You could for example run a db migration application locally like this: 

```scala
// get the 'mysql' implementation which logs the script output to standard out:
val mysql = dockerEnv.mysql().withLogger(dockerEnv.stdOut)

// ensure mysql is running in this scope:
mysql.bracket { 
  // mysql is running here. If it was already running then this has no effect - if it wasn't then mysql was started and 
  // will be stopped outside of this scope 
  
  dockerEnv.postgres().bracket {
    // we now have both mysql and postgres database running. We could invoke now run our app here - 
    // perhaps some DB migration code for instance.
  }
}
```
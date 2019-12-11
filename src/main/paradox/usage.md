## Usage

dockerenv was created to expose a handle on common docker containers so they can easily be started/stopped in code.

The typical use-case is to support real integration testing against real services rather than having to mock things out.

The main project gives you a [DockerEnv](https://oss.sonatype.org/service/local/repositories/releases/archive/com/github/aaronp/dockerenv_2.12/0.4.3/dockerenv_2.12-0.4.3-javadoc.jar/!/dockerenv/DockerEnv.html)
interface for common containers (kafka, mysql, postgres, mongo, etc).

With that you can do things like:

```scala
dockerenv.mysql().start() // start mysql
dockerenv.mysql().stop() // stop mysql 
dockerenv.mysql().bracket {
  // start mysql if it wasn't already running, and stop it when this bracket closes if it was started 
} 
```

As the primary use-case is really for testing, you may want to include a dependency on the "test" artifact as well as the main one:

```scala
libraryDependencies += "com.github.aaronp" %% "dockerenv" % "latest version" % "test" classifier "tests"
libraryDependencies += "com.github.aaronp" %% "dockerenv" % "latest version" % "test" 
``` 

 
In doing so this gives you access to the 'BaseXXXSpec' traits (e.g. BaseKafkaSpec) which ensures that kafka is started/stopped for those tests:

```scala
// the BaseXYZSpec classes ensure XYZ is started/stopped for your tests, and expose a 'dockerHandle' should you need to 
// stop the services for run more complex scenarios, like testing failover/retries  
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
supported containers. You could for example run a DB migration application locally like this: 

```scala
  println(step("Let's do some ETL! Starting Mysql...", 0))
  dockerenv.mysql().bracket {
    println(step("Mysql Started, Starting Kafka", 1))
    dockerenv.kafka().bracket {
      println(step("Kafka Started, Starting Mongo", 2))
      dockerenv.mongo().bracket {
        println(step("All up! Killing Mongo...", 3))
      }
      println(step("Mongo Down, Killing Kafka", 2))
    }
    println(step("Kafka Down, Killing Mysql", 1))
  }
  println(step("All done!", 0))

  // pretty-prints the message with the current 'docker ps' status
  private def step(msg: String, indent: Int): String = {
    val status      = dockerPS
    ...
  }

  def dockerPS: String = "docker ps".!!
```

The output for which is:

```
_____________________________________________________________________________________________________________________________
Let's do some ETL! Starting Mysql...
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES

    ________________________________________________________________________________________________________________________________________________________________
    Mysql Started, Starting Kafka
    CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS                  PORTS                               NAMES
    39d16d02b95c        mysql:8.0.18        "docker-entrypoint.s…"   1 second ago        Up Less than a second   33060/tcp, 0.0.0.0:7777->3306/tcp   dockerenv-mysql
    
        __________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
        Kafka Started, Starting Mongo
        CONTAINER ID        IMAGE                  COMMAND                  CREATED             STATUS                  PORTS                                                                                                                      NAMES
        a6e9bea0393c        dockerenv-kafka:test   "supervisord -n"         1 second ago        Up Less than a second   0.0.0.0:2181->2181/tcp, 0.0.0.0:8083->8083/tcp, 0.0.0.0:9080->9080/tcp, 0.0.0.0:9092->9092/tcp, 0.0.0.0:29092->29092/tcp   test-kafka
        39d16d02b95c        mysql:8.0.18           "docker-entrypoint.s…"   2 seconds ago       Up 1 second             33060/tcp, 0.0.0.0:7777->3306/tcp                                                                                          dockerenv-mysql
        
            __________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
            All up! Killing Mongo...
            CONTAINER ID        IMAGE                  COMMAND                  CREATED             STATUS                  PORTS                                                                                                                      NAMES
            fef2d8c0662a        mongo:4.0              "docker-entrypoint.s…"   1 second ago        Up Less than a second   0.0.0.0:9010->27017/tcp                                                                                                    dockerenv-mongo
            a6e9bea0393c        dockerenv-kafka:test   "supervisord -n"         2 seconds ago       Up 1 second             0.0.0.0:2181->2181/tcp, 0.0.0.0:8083->8083/tcp, 0.0.0.0:9080->9080/tcp, 0.0.0.0:9092->9092/tcp, 0.0.0.0:29092->29092/tcp   test-kafka
            39d16d02b95c        mysql:8.0.18           "docker-entrypoint.s…"   3 seconds ago       Up 2 seconds            33060/tcp, 0.0.0.0:7777->3306/tcp                                                                                          dockerenv-mysql
            
        ______________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
        Mongo Down, Killing Kafka
        CONTAINER ID        IMAGE                  COMMAND                  CREATED             STATUS              PORTS                                                                                                                      NAMES
        a6e9bea0393c        dockerenv-kafka:test   "supervisord -n"         3 seconds ago       Up 1 second         0.0.0.0:2181->2181/tcp, 0.0.0.0:8083->8083/tcp, 0.0.0.0:9080->9080/tcp, 0.0.0.0:9092->9092/tcp, 0.0.0.0:29092->29092/tcp   test-kafka
        39d16d02b95c        mysql:8.0.18           "docker-entrypoint.s…"   4 seconds ago       Up 3 seconds        33060/tcp, 0.0.0.0:7777->3306/tcp                                                                                          dockerenv-mysql
        
    ____________________________________________________________________________________________________________________________________________________________
    Kafka Down, Killing Postgres
    CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                               NAMES
    39d16d02b95c        mysql:8.0.18        "docker-entrypoint.s…"   6 seconds ago       Up 4 seconds        33060/tcp, 0.0.0.0:7777->3306/tcp   dockerenv-mysql
    
_____________________________________________________________________________________________________________________________
All done!
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES

```

Under the hood this just issues CLI commands to docker, the output of which you can see by changing the script logging:

```scala

// get the 'mysql' implementation which logs the script output to standard out:
val mysql = dockerEnv.mysql().withLogger(dockerEnv.stdOut)
mysql.start()
mysql.stop()
mysql.bracket {
  // mysql is running here
}
```
dockerenv
====

[![Build Status](https://travis-ci.org/aaronp/dockerenv.svg?branch=master)](https://travis-ci.org/aaronp/dockerenv)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.aaronp/dockerenv_2.13/badge.png)](https://maven-badges.herokuapp.com/maven-central/com.github.aaronp/dockerenv_2.13)
[![Coverage Status](https://coveralls.io/repos/github/aaronp/dockerenv/badge.svg?branch=master)](https://coveralls.io/github/aaronp/dockerenv?branch=master)
[![Scaladoc](https://javadoc-badge.appspot.com/com.github.aaronp/dockerenv_2.13.svg?label=scaladoc)](https://javadoc-badge.appspot.com/com.github.aaronp/dockerenv_2.13)

Dockerenv provides a handle on useful docker containers, typically for running tests against real services as opposed to
having to mock.
 

See docs [here](https://aaronp.github.io/dockerenv/index.html)

The gist is that we can easily spin up real services (databases, kafka clusters, etc) which we can then connect against.

e.g. Import docker env in your build like this:

```scala
libraryDependencies += "com.github.aaronp" %% "dockerenv" % "latest version" % "test" 
```

Then use it like this:


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

You can also depend on the test artifact to extend common base test classes:

```scala
libraryDependencies += "com.github.aaronp" %% "dockerenv" % "latest version" % "test" classifier "tests"
```

which gives you:


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
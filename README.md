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
libraryDependencies += "com.github.aaronp" %% "dockerenv" % "latest version" % "test" classifier "tests"
libraryDependencies += "com.github.aaronp" %% "dockerenv" % "latest version" % "test" 
```

Then use it like this:

```scala
      dockerenv.postgres().bracket { // the postgres DB is started here if it wasn't already running
        dockerenv.mysql().bracket { 
          // both postgres and mysql DB is started here if it wasn't already running
        }
        // mysql has been stopped, unless it was running
      }
```
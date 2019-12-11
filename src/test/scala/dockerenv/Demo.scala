package dockerenv

import scala.sys.process._

object Demo extends App {

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

  private def step(msg: String, indent: Int): String = {
    val status      = dockerPS
    val sep: String = status.linesIterator.map(_.trim).toList.maxBy(_.length).map(_ => '_')
    val all         = s"$sep\n$msg\n$status\n"
    val offset      = " " * (indent * 4)
    all.linesIterator
      .map { line =>
        s"$offset$line"
      }
      .mkString("\n")
  }

  def dockerPS: String = "docker ps".!!
}

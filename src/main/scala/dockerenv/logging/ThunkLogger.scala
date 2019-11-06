package dockerenv.logging

import scala.sys.process.ProcessLogger

case class ThunkLogger(onOut: String => Unit) extends ProcessLogger {
  override def out(s: => String): Unit = {
    onOut(s)
  }

  override def err(s: => String): Unit = {
    onOut(s)
  }

  override def buffer[T](f: => T): T = f
}

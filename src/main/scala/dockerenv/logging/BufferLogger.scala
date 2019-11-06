package dockerenv.logging

import scala.collection.mutable.ArrayBuffer
import scala.sys.process.ProcessLogger

class BufferLogger(prefix: String) extends ProcessLogger {
  private val outputBuffer = ArrayBuffer[String]()

  def output = outputBuffer.mkString("\n")

  override def out(s: => String): Unit = {
    outputBuffer.append(s)
  }

  override def err(s: => String): Unit = {
    outputBuffer.append(s"ERR: $s")
  }

  override def buffer[T](f: => T): T = f
}

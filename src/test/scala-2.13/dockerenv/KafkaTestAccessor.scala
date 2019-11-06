package dockerenv

object KafkaTestAccessor {

  def linesHead(str : String): String = {
    str.linesIterator.toList.head
  }
}

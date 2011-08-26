package org.nlogo.awt

import java.util.{ List => JList }
import java.awt.FontMetrics
import collection.JavaConverters._

object LineBreaker {
  def breakLines(_text: String, metrics: FontMetrics, width: Int): JList[String] = {
    var text = _text
    val result = collection.mutable.Buffer[String]()
    def nonEmptyResult =
      if (result.isEmpty) List("").asJava
      else result.asJava
    while (text.nonEmpty) {
      var index = 0
      while (index < text.size && (metrics.stringWidth(text.substring(0, index + 1)) < width
                                   || text(index) == ' ')) {
        if (text(index) == '\n') {
          text = text.substring(0, index) + ' ' + text.substring(index + 1)
          index += 1
          return nonEmptyResult
        }
        index += 1
      }

      // if index is still 0, then this line will never wrap
      // so just give up and return the whole thing as one line
      if (index == 0) {
        result += text
        return nonEmptyResult
      }

      // invariant: index is now the index of the first non-space
      // character which won't fit in the current line

      if (index < text.size) {
        val spaceIndex = text.substring(0, index).lastIndexOf(' ')
        if (spaceIndex >= 0)
          index = spaceIndex + 1
      }

      // invariant: index is now the index of the first character
      // which will *not* be included in the current line

      val thisLine = text.substring(0, index)
      if (index < text.size) {
        text = text.substring(index, text.length())
      } else {
        text = ""
      }
      result += thisLine
    }
    nonEmptyResult
  }
}

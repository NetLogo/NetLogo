// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.util.{ List => JList }
import java.awt.FontMetrics
import collection.JavaConverters._

// This is pretty raw converted Java code that ought to be made a lot more Scala-tastic.

object LineBreaker {
  def breakLines(_text: String, metrics: FontMetrics, width: Int): JList[String] = {
    var text = _text
    val result = collection.mutable.Buffer[String]()
    def nonEmptyResult =
      if (result.isEmpty) List("").asJava
      else result.asJava
    while (text.nonEmpty) {
      var index = 0
      var done = false
      while (!done && index < text.size && (metrics.stringWidth(text.take(index + 1)) < width
                                            || text(index) == ' ')) {
        if (text.charAt(index) == '\n') {
          text = text.take(index) + ' ' + text.drop(index + 1)
          done = true
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
        val spaceIndex = text.take(index).lastIndexOf(' ')
        if (spaceIndex >= 0)
          index = spaceIndex + 1
      }

      // invariant: index is now the index of the first character
      // which will *not* be included in the current line
      result += text.take(index)
      text = text.drop(index)
    }
    nonEmptyResult
  }
}

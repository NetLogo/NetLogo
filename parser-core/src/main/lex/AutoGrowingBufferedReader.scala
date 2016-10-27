// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.io.BufferedReader

class AutoGrowingBufferedReader(reader: BufferedReader) extends LexReader {
  private var markSize: Int = 65536
  private var remainingMark: Int = 0

  def mark(): Unit = {
    reader.mark(markSize)
    remainingMark = markSize
  }

  def reset(): Unit = {
    reader.reset()
    remainingMark = markSize
  }

  def skip(l: Long): Unit = {
    reader.skip(l)
    remainingMark -= l.toInt
  }

  def read(): Int = {
    if (remainingMark == 0) {
      reader.reset()
      reader.mark(markSize * 2)
      reader.skip(markSize)
      remainingMark = markSize
      markSize = markSize * 2
    }
    val i = reader.read()
    remainingMark -= 1
    i
  }
}

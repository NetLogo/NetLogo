// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

object Replacement {
  class FailedException(ex: Exception) extends Exception(ex)
}

private class Replacement(start: Int, end: Int, before: String, after: String) {
  def replace(buf: java.lang.StringBuilder, offset: Int): Int = {
    try
      if (!buf.substring(start + offset, end + offset).equalsIgnoreCase(before))
        throw new Replacement.FailedException(null)
    catch { case ex:RuntimeException =>
      throw new Replacement.FailedException(ex) }
    buf.delete(start + offset, end + offset)
    buf.insert(start + offset, after)
    // return new offset
    offset - before.length + after.length
  }
}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

trait LexReader {
  def mark(): Unit
  def reset(): Unit
  def skip(l: Long): Unit
  def read(): Int
}

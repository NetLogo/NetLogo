// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

object Using {
  def apply[A, B](a: => A, close: A => Unit): Usage[A, B] = new Usage(a, close)
}

class Usage[+A, B](a: => A, close: A => Unit) {
  def apply(f: A => B): B = {
    val aInstance = a
    try {
      f(aInstance)
    } finally {
      close(aInstance)
    }
  }
}

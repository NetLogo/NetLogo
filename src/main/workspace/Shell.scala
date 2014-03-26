// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.{ BufferedReader, InputStreamReader }

abstract class Shell {
  val input: Iterator[String] = {
    val reader = new BufferedReader(new InputStreamReader(System.in))
    Iterator.continually(reader.readLine())
      .takeWhile(_ != null)
  }

  def isQuit(s: String) =
    List(":QUIT", ":EXIT").contains(s.trim.toUpperCase)

}

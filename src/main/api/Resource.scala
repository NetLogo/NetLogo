// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object Resource {

  def getResourceLines(path: String): Iterator[String] = {
    val in = new java.io.BufferedReader(
      new java.io.InputStreamReader(
        getClass.getResourceAsStream(path)))
    Iterator.continually(in.readLine())
      .takeWhile(_ != null)
  }

  def getResourceAsString(path: String): String =
    getResourceLines(path)
      .mkString("", "\n", "\n")

}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object Resource {

  def getResource(path: String): io.Source =
    io.Source.fromURL(getClass.getResource(path))

  def getResourceLines(path: String): Iterator[String] =
    getResource(path).getLines

  def getResourceAsString(path: String): String =
    getResource(path).mkString

}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.io.Source

object Resource {

  def get(path: String): Source =
    Source.fromURL(getClass.getResource(path))

  def lines(path: String): Iterator[String] =
    get(path).getLines()

  def asString(path: String): String =
    get(path).mkString

}

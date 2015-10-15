// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.{ Context => BlackBoxContext}

object Resource {

  def lines(resourcePath: String): Iterator[String] = macro getLines

  def getLines(c: BlackBoxContext)(resourcePath: c.Tree): c.Tree = {
    import c.universe._
    resourcePath match {
      case q"${resource: String}" =>
        val lines = getResource(resource).getLines.map(s => q"$s").toList
        q"Seq(..$lines).toIterator"
      case _ => c.abort(c.enclosingPosition, "Must supply a string literal to Resource.lines")
    }
  }

  def getResource(path: String): io.Source =
    io.Source.fromURL(getClass.getResource(path))
}

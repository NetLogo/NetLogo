// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.quoted.*

import scala.io.Source

object Resource {

  def getResource(path: String): Source =
    Source.fromURL(getClass.getResource(path))

  inline def asString(resourcePath: String): String = ${ getString('resourcePath) }

  inline def lines(resourcePath: String): Seq[String] = ${ getLines('resourcePath) }

  private def getString(using Quotes)(resourcePath: Expr[String]): Expr[String] = {
    Expr(getResource(resourcePath.valueOrAbort).mkString)
  }

  private def getLines(using Quotes)(resourcePath: Expr[String]): Expr[Seq[String]] = {
    Expr(getResource(resourcePath.valueOrAbort).getLines().toSeq)
  }

}

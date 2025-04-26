// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.prim.Lambda
import org.nlogo.core.{ Let, Syntax }

object LambdaArgs {
  def fromPrim(arguments: Lambda.Arguments): LambdaArgs =
    LambdaArgs(arguments.argumentNames, arguments.argumentSyntax)

  def fromFormals(formals: Array[Let]): LambdaArgs = {
    val args = formals.map( (f) => (f.name, Syntax.WildcardType) ).unzip
    LambdaArgs(args._1.toIndexedSeq, args._2.toIndexedSeq)
  }
}

case class LambdaArgs(argumentNames: Seq[String], argumentSyntax: Seq[Int]) {
  def isVariadic: Boolean = Syntax.isVariadic(argumentSyntax)
}

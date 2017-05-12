// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ Expression, Syntax }

object RichSyntax {
  def apply(syntax: Syntax, arguments: Seq[Expression]): RichSyntax = new RichSyntax(syntax, arguments)

  // syntaxes with defaults and variadics are bimodal.
  //
  // Mode 1 (not directly enclosed by parens = non-variadic):
  // Mode 2 (directly enclosed by parens = varidic):
  //   This has two sub-modes:
  //   a. The repeatable argument is the last argument on the right
  //   b. The repeatable argument is not the last argument on the right
  //
  // In Mode 1, the number of arguments is entirely deterministic.
  // The repeatable is extruded as necessary to reach the default argument count
  //  (this is often only once, but can be 2 or more times)
  // This is a linear state machine
  //
  // In mode 2a, the number of arguments prior to the repeatable argument is entirely
  // deterministic, then it is followed by a list of arguments which can be repeated any
  // number of times. If minimumOption is supplied, there must be at least minimum arguments.
  // This is a linear state machine, until it reaches the repeatable argument, at which point it
  // simply verifies.
  //
  // In mode 2a, only the arguments following the repeatable argument are entirely deterministic.
  // Once the first argument has been seen which cannot be repeatable, the rest of the types
  // are entirely determined.
}

class RichSyntax(syntax: Syntax, arguments: Seq[Expression]) {
  lazy val allArgs = syntax.left +: syntax.right

  val argCount = arguments.length

  def nextArgumentType: Option[Int] =
    if (syntax.isInfix && arguments.length < allArgs.length)
      Some(allArgs(arguments.length))
    else if (syntax.isVariadic && argCount >= syntax.right.length)
      syntax.right.find(a => (a & Syntax.RepeatableType) != 0).map(_ & (~ Syntax.RepeatableType))
    else if (arguments.length >= syntax.right.length)
      None
    else
      Some(syntax.right(arguments.length) & (~ Syntax.RepeatableType))

  def withArgument(arg: Expression): RichSyntax =
    new RichSyntax(syntax, arguments :+ arg)
}

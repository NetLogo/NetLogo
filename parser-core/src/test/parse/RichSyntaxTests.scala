// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ Expression, ReporterApp, SourceLocation, Syntax }
import org.nlogo.core.prim.{ _const }
import org.scalatest.FunSuite

class RichSyntaxTests extends FunSuite {
  def location = SourceLocation(0, 0, "")
  def constTwo = new ReporterApp(_const(Double.box(2.0)), Seq(), location)
  def constString = new ReporterApp(_const("abc"), Seq(), location)
  def rich(s: Syntax, args: Seq[Expression] = Seq.empty[Expression]): RichSyntax = RichSyntax(s, args)
  // The syntax object is both extremely complicated in terms of data and quite opaque in terms of API.
  // The purpose of this wrapper class is to simplify the operation without needing to modify it in org.nlogo.core.
  // Eventually, we might hope to replace the implementation in org.nlogo.core with something like this, which
  // captures requirements with a more succinct API.

  trait Helper {
    var syntax: RichSyntax = null
    def rich(s: Syntax, args: Seq[Expression] = Seq.empty[Expression]): RichSyntax = {
      syntax = RichSyntax(s, args)
      syntax
    }
    def withArgument(arg: Expression) = {
      syntax = syntax.withArgument(arg)
      syntax
    }
  }

  test("basic command syntax") { new Helper {
    rich(Syntax.commandSyntax())
    assertResult(None)(syntax.nextArgumentType)
  } }

  test("one-argument command syntax") { new Helper {
    rich(Syntax.commandSyntax(right = List(Syntax.NumberType)))
    assertResult(Some(Syntax.NumberType))(syntax.nextArgumentType)
    withArgument(constTwo)
    assertResult(None)(syntax.nextArgumentType)
  } }

  test("infix reporter syntax") { new Helper {
    rich(Syntax.reporterSyntax(left = Syntax.NumberType, right = List(Syntax.StringType), ret = Syntax.StringType))
    assertResult(Some(Syntax.NumberType))(syntax.nextArgumentType)
    withArgument(constTwo)
    assertResult(Some(Syntax.StringType))(syntax.nextArgumentType)
    withArgument(constString)
    assertResult(None)(syntax.nextArgumentType)
  } }

  test("variadic reporter syntax") { new Helper {
    rich(Syntax.reporterSyntax(right = List(Syntax.NumberType | Syntax.RepeatableType), ret = Syntax.StringType))
    assertResult(Some(Syntax.NumberType))(syntax.nextArgumentType)
    withArgument(constTwo)
    assertResult(Some(Syntax.NumberType))(syntax.nextArgumentType)
  } }

  test("variadic reporter syntax with variadics after other args") { new Helper {
    rich(Syntax.reporterSyntax(right = List(Syntax.StringType, Syntax.NumberType | Syntax.RepeatableType), ret = Syntax.StringType))
    assertResult(Some(Syntax.StringType))(syntax.nextArgumentType)
    withArgument(constString)
    assertResult(Some(Syntax.NumberType))(syntax.nextArgumentType)
    withArgument(constTwo)
    assertResult(Some(Syntax.NumberType))(syntax.nextArgumentType)
  } }

  // there are three options here:
  // 1. Throw an exception
  // 2. Return a RichSyntax indicating an exception in the future
  // 3. Return a failed parseResult
  test("withArgument passes in bad type") {
    pending
  }

  test("variadic reporter with defaultOption syntax") {
    pending
  }

  test("variadic reporter with repeated type in the middle syntax") {
    pending
  }

  test("variadic infix reporter") {
    pending
  }

  test("properly matches core.Expressions to their type") {
    pending
  }
}

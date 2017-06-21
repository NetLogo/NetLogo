// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ Expression, ReporterApp, SourceLocation, Syntax }
import org.nlogo.core.prim.{ _const }
import org.scalatest.FunSuite

import RichSyntax._

class RichSyntaxTests extends FunSuite {
  def location = SourceLocation(0, 0, "")
  def constTwo = new ReporterApp(_const(Double.box(2.0)), Seq(), location)
  def constString = new ReporterApp(_const("abc"), Seq(), location)
  def numericVariable = new ReporterApp(_turtlevariable(5, Syntax.NumberType), Seq(), location)

  val variadicReporterSyntax =
    Syntax.reporterSyntax(right = List(Syntax.StringType, Syntax.NumberType | Syntax.RepeatableType), ret = Syntax.StringType)
  val variadicLeftReporterSyntax =
    Syntax.reporterSyntax(right = List(Syntax.NumberType | Syntax.RepeatableType, Syntax.StringType), ret = Syntax.ListType)

  // The syntax object is both extremely complicated in terms of data and quite opaque in terms of API.
  // The purpose of this wrapper class is to simplify the operation without needing to modify it in org.nlogo.core.
  // Eventually, we might hope to replace the implementation in org.nlogo.core with something like this, which
  // captures requirements with a more succinct API.

  trait Helper {
    var syntax: RichSyntax = null
    var failure = Option.empty[ParseFailure]
    def typedArguments = syntax.typedArguments
    def rich(s: Syntax, variadic: Boolean = false): RichSyntax = {
      syntax = RichSyntax("FOO", s, variadic)
      syntax
    }
    def withArgument(arg: Expression) = {
      syntax.withArgument(arg) match {
        case SuccessfulParse(s: RichSyntax) => syntax = s
        case FailedParse(f) => failure = Some(f)
      }
    }
    def assertFailure(fail: ParseFailure): Unit = {
      assert(failure.nonEmpty)
      assertResult(fail)(failure.get)
    }
    def assertNextArg(aType: ArgumentType): Unit = {
      assertResult(aType)(syntax.nextArgumentType)
    }
  }

  test("basic command syntax") { new Helper {
    rich(Syntax.commandSyntax())
    assertNextArg(NoMoreArguments)
    assertResult(Seq.empty[(Expression, Int)])(typedArguments)
  } }

  test("one-argument command syntax") { new Helper {
    rich(Syntax.commandSyntax(right = List(Syntax.NumberType)))
    assertNextArg(Argument(Syntax.NumberType))
    withArgument(constTwo)
    assertNextArg(NoMoreArguments)
    assertResult(constTwo)(typedArguments.head._1)
    assertResult(List(constTwo -> Syntax.NumberType))(typedArguments)
  } }

  test("infix reporter syntax") { new Helper {
    rich(Syntax.reporterSyntax(left = Syntax.NumberType, right = List(Syntax.StringType), ret = Syntax.StringType))
    assertNextArg(Argument(Syntax.NumberType))
    withArgument(constTwo)
    assertNextArg(Argument(Syntax.StringType))
    withArgument(constString)
    assertNextArg(NoMoreArguments)
    assertResult(List(constTwo -> Syntax.NumberType, constString -> Syntax.StringType))(
      typedArguments)
  } }

  test("variadic reporter syntax") { new Helper {
    rich(Syntax.reporterSyntax(right = List(Syntax.NumberType | Syntax.RepeatableType), ret = Syntax.StringType), variadic = true)
    assertNextArg(MaybeArgument(Syntax.NumberType))
    withArgument(constTwo)
    assertNextArg(MaybeArgument(Syntax.NumberType))
    assertResult(List(constTwo -> Syntax.NumberType))(typedArguments)
  } }

  test("variadic reporter syntax with variadics after other args") { new Helper {
    rich(variadicReporterSyntax, variadic = true)
    assertNextArg(Argument(Syntax.StringType))
    withArgument(constString)
    assertNextArg(MaybeArgument(Syntax.NumberType))
    withArgument(constTwo)
    assertNextArg(MaybeArgument(Syntax.NumberType))
    assertResult(List(constString -> Syntax.StringType, constTwo -> Syntax.NumberType))(
      typedArguments)
  } }

  test("variadic reporter in non-variadic context") { new Helper {
    rich(variadicReporterSyntax)
    withArgument(constString)
    withArgument(constTwo)
    assertNextArg(NoMoreArguments)
  } }

  test("variadic reporter with defaultOption syntax (non-variadic context)") { new Helper {
    rich(Syntax.reporterSyntax(right = List(Syntax.WildcardType | Syntax.RepeatableType), ret = Syntax.ListType, defaultOption = Some(3)))
    assertNextArg(Argument(Syntax.WildcardType))
    withArgument(constString)
    withArgument(constString)
    assertNextArg(Argument(Syntax.WildcardType))
    withArgument(constString)
    assertNextArg(NoMoreArguments)
    val stringWithType = constString -> Syntax.StringType
    assertResult(List(stringWithType, stringWithType, stringWithType))(
      typedArguments)
  } }

  test("variadic reporter with repeated type first argument (non-variadic context)") { new Helper {
    rich(variadicLeftReporterSyntax)
    assertNextArg(Argument(Syntax.NumberType))
    withArgument(constTwo)
    assertNextArg(Argument(Syntax.StringType))
    withArgument(constString)
    assertNextArg(NoMoreArguments)
  } }

  test("variadic reporter with repeated type first argument (variadic context)") { new Helper {
    rich(variadicLeftReporterSyntax, variadic = true)
    assertNextArg(OneOfArgument(Syntax.NumberType, Syntax.StringType))
    withArgument(constTwo)
    assertNextArg(OneOfArgument(Syntax.NumberType, Syntax.StringType))
    withArgument(constString)
    assertNextArg(NoMoreArguments)
  } }

  test("given an extra argument, returns an error") { new Helper {
    rich(Syntax.commandSyntax())
    withArgument(constTwo)
    assertFailure(ParseFailure("additional unwanted argument: _const(2.0)[]", 0, 0, ""))
  } }

  test("given an argument of the wrong type, returns an error") { new Helper {
    rich(Syntax.commandSyntax(right = List(Syntax.NumberType)))
    withArgument(constString)
    assertFailure(ParseFailure("FOO expected this input to be a number, but got a string instead", 0, 0, ""))
  } }

  test("given an argument of a type which can be a variable, returns an error without mentioning that") { new Helper {
    rich(Syntax.commandSyntax(right = List(Syntax.StringType)))
    withArgument(numericVariable)
    assertFailure(ParseFailure("FOO expected this input to be a string, but got a number instead", 0, 0, ""))
  } }

  test("has a method to test for missing inputs") { new Helper {
    pending
  } }

  /* We don't expect the typer to be in charge of passing arguments on to itself
  test("withArgument passes in bad type") {
    rich(Syntax.commandSyntax())
    assertNextArg(FailedParse(TooManyArguments)
    pending
  }
  */
}

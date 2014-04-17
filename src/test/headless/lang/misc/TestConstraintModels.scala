// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

import org.nlogo.agent.BooleanConstraint
import org.nlogo.api
import org.nlogo.core._

class TestConstraintModels extends FixtureSuite {

  test("Boolean Constraint Constructor") { implicit fixture =>
    import fixture._
    open(Model(widgets =
      List(View(), Switch(varName="on?", display="on?", on=true),
        Switch(varName="off?", display="on?", on=false))))
    testReporter("on?", "true")
    testReporter("off?", "false")
    object con extends BooleanConstraint {
      def default = defaultValue.asInstanceOf[java.lang.Boolean].booleanValue
      def apply(s:String): Boolean = coerceValue(s).asInstanceOf[java.lang.Boolean].booleanValue
    }
    assert(!con.default)
    assert(!con("false"))
    assert(!con("FALSE"))
    assert(!con("foo"))
    assert(con("true"))
    assert(con("TRUE"))
    testCommand("set on? true")
    testCommand("set on? false")
    checkError("set on? 9999")
  }

  test("Chooser Constraint") { implicit fixture =>
    import fixture._
    open(Model(widgets = List(
      View(),
      Chooser(display = "foo", varName = "foo", choices = List(Double.box(1), Double.box(2), Double.box(3), Double.box(4), Double.box(5)),
        currentChoice = 0),
      Chooser(display = "bar", varName = "bar", choices = List("a", "b", "c", "d"), currentChoice = 3),
      Chooser(display = "mix", varName = "mix", choices = List(Double.box(12), "aaa", Double.box(34), "bbb", Double.box(56)),
      currentChoice = 0))))
    testReporter("foo", "1")
    testReporter("bar", "\"d\"")
    testReporter("mix", "12")
    testCommand("set foo 5")
    testCommand("set bar \"b\"")
    testCommand("set mix \"aaa\"")
    checkError("set foo 9999")
    checkError("set bar \"j\"")
  }

  test("InputBox Constraint Loading") { implicit fixture =>
    import fixture._
    open(Model(widgets = List(
      View(),
      InputBox(varName="number", value=5d, boxtype=Num),
      InputBox(varName="string", value="this is a string", boxtype=Str),
      InputBox(varName="reporter", value="max-pxcor", boxtype=StrReporter),
      InputBox(varName="commands", value="show 1", boxtype=StrCommand),
      InputBox(varName="colors", value=0, boxtype=Col)
      )))

    testReporter("string", "\"this is a string\"")
    testCommand("set string \"some string\"")
    testReporter("string", "\"some string\"")
    testCommand("set string \"max-pxcor\"")
    testReporter("string", "\"max-pxcor\"")
    //errors
    checkError("set string 5")

    testReporter("number", "5")
    testCommand("set number 0")
    testReporter("number", "0")
    testCommand("set number max-pxcor")
    testReporter("number = max-pxcor", "true")
    // errors
    checkError("set number \"5\"")
    checkError("set number \"max-pxcor\"")
    checkError("set number \"show 1\"")


    testReporter("colors", "0")
    testCommand("set colors 5")
    testReporter("colors", "5")

    // errors
    checkError("set colors \"max-pxcor\"")
    checkError("set colors \"show 1\"")
    checkError("set colors \"some string\"")
    checkError("set colors \"5\"")

    testReporter("reporter", "\"max-pxcor\"")
    testCommand("set reporter \"5\"")
    testReporter("reporter", "\"5\"")
    testCommand("set reporter \"max-pxcor\"")
    testReporter("reporter", "\"max-pxcor\"")
    // errors
    checkError("set reporter 5")
    checkError("set reporter max-pxcor")

    testReporter("commands", "\"show 1\"")
    testCommand("set commands \"show 2\"")
    testReporter("commands", "\"show 2\"")
    // errors
    checkError("set commands 5")
    checkError("set commands max-pxcor")
  }

  test("Slider Constraints") { implicit fixture =>
    import fixture._
    open(Model(widgets = List(View(), Slider(display="x-loc", varName="x-loc", min="0", max="100", default=10, step="1"))))
    testReporter("x-loc", "10")
    testCommand(("set x-loc 20"))
    testReporter("x-loc", "20")
    checkError("set x-loc false")
  }

  def checkError(commands: String)(implicit fixture: Fixture) {
    intercept[api.LogoException] {
      fixture.workspace.command(commands)
    }
    fixture.workspace.clearLastLogoException()
  }


}

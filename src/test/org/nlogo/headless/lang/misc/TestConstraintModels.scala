// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

import org.nlogo.agent.BooleanConstraint
import org.nlogo.api, ModelCreator._

class TestConstraintModels extends FixtureSuite {

  test("Boolean Constraint Constructor") { implicit fixture =>
    import fixture._
    open(Model(widgets =
      List(Switch(name="on?", on=true),
        Switch(name="off?", on=false))))
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
      Chooser(name = "foo", choices = List(1, 2, 3, 4, 5), index = 0),
      Chooser(name = "bar", choices = List("a", "b", "c", "d"), index = 3),
      Chooser(name = "mix", choices = List(12, "aaa", 34, "bbb", 56), index = 0))))
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
      InputBox(name="number", value=5d, typ=InputBoxTypes.Num),
      InputBox(name="string", value="this is a string", typ=InputBoxTypes.Str),
      InputBox(name="reporter", value="max-pxcor", typ=InputBoxTypes.StrReporter),
      InputBox(name="commands", value="show 1", typ=InputBoxTypes.StrCommand),
      InputBox(name="colors", value=0, typ=InputBoxTypes.Col)
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
    open(Model(widgets = List(Slider(name="x-loc", min="0", max="100", current="10", inc="1"))))
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

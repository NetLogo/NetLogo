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
    openModel(Model(widgets =
      List(View(), Switch(variable=Some("on?"), display=Some("on?"), on=true),
        Switch(variable=Some("off?"), display=Some("on?"), on=false))))
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
    openModel(Model(widgets = List(
      View(),
      Chooser(display = Some("foo"), variable = Some("foo"), choices = List(1, 2, 3, 4, 5).map(Double.box(_)).map(ChooseableDouble(_)),
        currentChoice = 0),
      Chooser(display = Some("bar"), variable = Some("bar"), choices = List("a", "b", "c", "d").map(ChooseableString(_)), currentChoice = 3),
      Chooser(display = Some("mix"), variable = Some("mix"),
        choices = List(
          ChooseableDouble(Double.box(12)),
          ChooseableString("aaa"),
          ChooseableDouble(Double.box(34)),
          ChooseableString("bbb"),
          ChooseableDouble(Double.box(56))),
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
    openModel(Model(widgets = List(
      View(),
      InputBox(variable=Some("number"),
        boxedValue = NumericInput(5d, NumericInput.NumberLabel)),
      InputBox(variable=Some("string"),
        boxedValue = StringInput("this is a string", StringInput.StringLabel, false)),
      InputBox(variable=Some("reporter"),
        boxedValue = StringInput("max-pxcor", StringInput.ReporterLabel, false)),
      InputBox(variable=Some("commands"),
        boxedValue = StringInput("show 1", StringInput.CommandLabel, false)),
      InputBox(variable=Some("colors"),
        boxedValue = NumericInput(0, NumericInput.ColorLabel))
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
    openModel(Model(widgets = List(View(), Slider(display=Some("x-loc"), variable=Some("x-loc"), min="0", max="100", default=10, step="1"))))
    testReporter("x-loc", "10")
    testCommand(("set x-loc 20"))
    testReporter("x-loc", "20")
    checkError("set x-loc false")
  }

  def checkError(commands: String)(implicit fixture: Fixture): Unit = {
    intercept[api.LogoException] {
      fixture.workspace.command(commands)
    }
    fixture.workspace.clearLastLogoException()
  }


}

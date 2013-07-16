// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

// This is in org.nlogo.headless and not org.nlogo.workspace because it uses workspace.open(), which
// (at present!) DummyAbstractWorkspace does not support. - ST 4/8/08

import org.nlogo.agent.BooleanConstraint
import org.nlogo.api.ModelCreator._

class TestConstraintModels extends AbstractTestModels {

  testModel("Boolean Constraint Constructor",
            Model(widgets = List(Switch(name="on?", on=true),
                                 Switch(name="off?", on=false)))) {

    reporter("on?") -> true
    reporter("off?") -> false

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

    observer>>"set on? true"
    observer>>"set on? false"

    checkError("set on? 9999")
  }

  testModel("Chooser Constraint",
    Model(widgets = List(
      Chooser(name = "foo", choices = List(1, 2, 3, 4, 5), index = 0),
      Chooser(name = "bar", choices = List("a", "b", "c", "d"), index = 3),
      Chooser(name = "mix", choices = List(12, "aaa", 34, "bbb", 56), index = 0)))) {

    reporter("foo") -> 1d
    reporter("bar") -> "d"
    reporter("mix") -> 12d

    observer>>"set foo 5"
    observer>>"set bar \"b\""

    observer>>"set mix \"aaa\""

    checkError("set foo 9999")
    checkError("set bar \"j\"")
  }

  testModel("InputBox Constraint Loading",
    Model(widgets = List(
      InputBox(name="number", value=5d, typ=InputBoxTypes.Num),
      InputBox(name="string", value="this is a string", typ=InputBoxTypes.Str),
      InputBox(name="reporter", value="max-pxcor", typ=InputBoxTypes.StrReporter),
      InputBox(name="commands", value="show 1", typ=InputBoxTypes.StrCommand),
      InputBox(name="colors", value=0, typ=InputBoxTypes.Col)
      ))){

    reporter("string") -> "this is a string"
    observer>>"set string \"some string\""
    reporter("string") -> "some string"
    observer>>"set string \"max-pxcor\""
    reporter("string") -> "max-pxcor"
    //errors
    checkError("set string 5")

    reporter("number") -> reporter("5")
    observer>>"set number 0"
    reporter("number") -> reporter("0")
    observer>>"set number max-pxcor"
    reporter("number") -> reporter("max-pxcor")
    // errors
    checkError("set number \"5\"")
    checkError("set number \"max-pxcor\"")
    checkError("set number \"show 1\"")


    reporter("colors") -> org.nlogo.agent.World.ZERO
    observer>>"set colors 5"
    reporter("colors") -> reporter("5")
    observer>>"set colors max-pxcor"
    reporter("colors") -> reporter("max-pxcor")
    // errors
    checkError("set colors \"max-pxcor\"")
    checkError("set colors \"show 1\"")
    checkError("set colors \"some string\"")
    checkError("set colors \"5\"")

    reporter("reporter") -> "max-pxcor"
    observer>>"set reporter \"5\""
    reporter("reporter") -> "5"
    observer>>"set reporter \"max-pxcor\""
    reporter("reporter") -> "max-pxcor"
    // errors
    checkError("set reporter 5")
    checkError("set reporter max-pxcor")

    reporter("commands") -> "show 1"
    observer>>"set commands \"show 2\""
    reporter("commands") -> "show 2"
    // errors
    checkError("set commands 5")
    checkError("set commands max-pxcor")
  }

  testModel("Slider Constraints",
    Model(widgets = List(Slider(name="x-loc", min="0", max="100", current="10", inc="1")))) {
    reporter("x-loc") -> 10d
    observer>>("set x-loc 20")
    reporter("x-loc") -> 20d
    checkError("set x-loc false")
  }

}

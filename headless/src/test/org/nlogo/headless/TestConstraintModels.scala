// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

// This is in org.nlogo.headless and not org.nlogo.workspace because it uses workspace.open(), which
// (at present!) DummyAbstractWorkspace does not support. - ST 4/8/08

import org.nlogo.agent.{BooleanConstraint, ConstantSliderConstraint, DynamicSliderConstraint, SliderConstraint}

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

  testModelFile("Slider Constraints1", "test/constraint/density-slider.nlogo") {
    observer>>"set density 57.00001"
    observer>>"set density 9999"
    observer>>"set density -0001"
  }

  testModelFile("Slider Constraint Constructor", "test/constraint/density-slider.nlogo") {
    var con = SliderConstraint.makeSliderConstraint(
      world.observer(), "0", "100", "1", 50d, "", workspace)
    assert(con.isInstanceOf[ConstantSliderConstraint])
    con = SliderConstraint.makeSliderConstraint(
      world.observer(), "min-pxcor", "max-pxcor", "1",
      50d, "", workspace)
    assert(con.isInstanceOf[DynamicSliderConstraint])
  }

  testModel("Slider Constraints2",
    Model("globals [ foo ] to setup set foo 10 end",
      widgets = List(Slider(name="x-loc", min="min-pxcor", max="foo + 30", current="0", inc="1")))) {

    // world is -16 16 -16 16 at start
    observer>>"setup"
    observer>>"set x-loc -16"
    reporter("x-loc") -> -16d
    observer>>"set x-loc foo + 30"
    reporter("x-loc") -> 40d
    observer>>"set x-loc -17"
    reporter("x-loc") -> -17d
    observer>>"set x-loc foo + 40"
    reporter("x-loc") -> 50d
  }

  testModel("Slider Bounds Do Not Affect RNG",
    Model(widgets = List(
      Slider(name="foo", min="0", max="10 + random 10", current="0", inc="1")))) {
    val randomState = workspace.report("__random-state")
    observer>>"set foo 5"
    reporter("__random-state") -> randomState
  }

  testModel("Slider Constraints Coercion",
    Model("globals [ foo ] to setup set foo 10 end",
      widgets = List(Slider(name="x-loc", min="min-pxcor", max="foo + 30", current="0", inc="1")))) {

    observer>>"setup"
    val index = world.observerOwnsIndexOf("X-LOC")
    val con = world.observer().variableConstraint(index).asInstanceOf[SliderConstraint]

    // the maximum should be 40
    var coerced = con.coerceValue(Double.box(41)).asInstanceOf[java.lang.Double]
    assert(Double.box(41) === coerced)

    // the minumum should be -16
    coerced = con.coerceValue(Double.box(-17)).asInstanceOf[java.lang.Double]
    assert(Double.box(-17) === coerced)

    // the increment should be 1
    coerced = con.coerceValue(Double.box(10.23123)).asInstanceOf[java.lang.Double]
    assert(Double.box(10.23123) === coerced)
  }
}

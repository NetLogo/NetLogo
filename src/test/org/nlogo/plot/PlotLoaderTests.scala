// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.scalatest.FunSuite
import org.nlogo.api.DummyLogoThunkFactory
import org.nlogo.api.ModelCreator.{Plot => MCPlot, Pen, Pens}

class PlotLoaderTests extends TestPlotLoaderHelper{

  // PlotLoader.parseStringLiterals

  test("none") {
    assertResult(Nil)(PlotLoader.parseStringLiterals(""))
  }
  test("one") {
    assertResult(List("foo"))(PlotLoader.parseStringLiterals("\"foo\""))
  }
  test("one empty") {
    assertResult(List(""))(PlotLoader.parseStringLiterals("\"\""))
  }
  test("two") {
    assertResult(List("foo", "bar"))(PlotLoader.parseStringLiterals("\"foo\" \"bar\""))
  }
  test("escaped quotes") {
    assertResult(List("print \"foo\""))(PlotLoader.parseStringLiterals("\"print \\\"foo\\\"\""))
  }
  test("extra spaces") {  // bug #48
    assertResult(List("foo" , "bar" ))(PlotLoader.parseStringLiterals("\"foo\" \"bar\""))
    assertResult(List(" foo", "bar" ))(PlotLoader.parseStringLiterals("\" foo\" \"bar\""))
    assertResult(List(" foo", " bar"))(PlotLoader.parseStringLiterals("\" foo\" \" bar\""))
    assertResult(List("foo" , " bar"))(PlotLoader.parseStringLiterals("\"foo\" \" bar\""))
    assertResult(List("foo ", "bar" ))(PlotLoader.parseStringLiterals("\"foo \" \"bar\""))
    assertResult(List("foo ", "bar "))(PlotLoader.parseStringLiterals("\"foo \" \"bar \""))
    assertResult(List("foo" , "bar "))(PlotLoader.parseStringLiterals("\"foo\" \"bar \""))
  }

  // PlotLoader.parsePen

  test("easy pen") {
    assertResult("PenSpec(sheep,1.0,0,-13345367,true,,)")(
      PlotLoader.parsePen("\"sheep\" 1.0 0 -13345367 true").toString)
  }

  test("pen with spaces in name") {
    assertResult("PenSpec(grass / 4,1.0,0,-10899396,true,,)")(
      PlotLoader.parsePen("\"grass / 4\" 1.0 0 -10899396 true").toString)
  }

  test("pen with adjacent spaces in name") {
    assertResult("PenSpec(  grass  /  4    ,1.0,0,-10899396,true,,)")(
      PlotLoader.parsePen("\"  grass  /  4    \" 1.0 0 -10899396 true").toString)
  }

  test("pen with double quotes in name") {
    assertResult("PenSpec(\"\"\",1.0,0,-10899396,true,,)")(
      PlotLoader.parsePen("\"\\\"\\\"\\\"\" 1.0 0 -10899396 true").toString)
  }

  test("a bunch of white space before code is ok.") {
    assertResult("PenSpec(sheep,1.0,0,-13345367,true,count turtles,)")(
      PlotLoader.parsePen("\"sheep\" 1.0 0 -13345367 true       \"count turtles\" \"\"").toString)
  }

  test("pen with command code, with escaped quotes command code") {
    assertResult("PenSpec(sheep,1.0,0,-13345367,true,ticks \" \" ticks,)")(
      PlotLoader.parsePen("\"sheep\" 1.0 0 -13345367 true \"ticks \\\" \\\" ticks\" \"\"").toString)
  }

  test("pen with simple command code") {
    assertResult("PenSpec(sheep,1.0,0,-13345367,true,crt 1,)")(
      PlotLoader.parsePen("\"sheep\" 1.0 0 -13345367 true \"crt 1\" \"\"").toString)
  }

  test("pen with no x and y axis code") {
    assertResult("PenSpec(sheep,1.0,0,-13345367,true,,)")(
      PlotLoader.parsePen("\"sheep\" 1.0 0 -13345367 true").toString)
  }

  test("pen with empty command code") {
    assertResult("PenSpec(sheep,1.0,0,-13345367,true,,)")(
      PlotLoader.parsePen("\"sheep\" 1.0 0 -13345367 true \"\" \"\"").toString)
  }

  test("pen with multi-line code") {
    assertResult("PenSpec(sheep,1.0,0,-13345367,true,foo\nbar,)")(
      PlotLoader.parsePen("\"sheep\" 1.0 0 -13345367 true \"foo\\nbar\" \"\"").toString)
  }

    // PlotLoader.parsePlot

  test("parse plot with no pen line") {
val plotLines="""
PLOT
26
160
301
360
Population
Time
Number
0.0
100.0
0.0
100.0
true
false""".replaceAll("\r\n", "\n")
    val plot = load(plotLines)
    assertResult(0)(plot.pens.size) // no default pen anymore.
  }

  def twice(s: String) = s + s

  test("converter converts plot setup code") {
    val plot = load(MCPlot(setupCode = "crt 1").toString, twice)
    assertResult("crt 1crt 1")(plot.setupCode)
  }

  test("converter converts plot update code") {
    val plot = load(MCPlot(updateCode = "crt 2").toString, twice)
    assertResult("crt 2crt 2")(plot.updateCode)
  }

  test("converter converts plot pen setup code") {
    val plot = load(MCPlot(pens = Pens(Pen(setupCode = "crt 3"))).toString, twice)
    assertResult("crt 3crt 3")(plot.pens(0).setupCode)
  }

  test("converter converts plot pen update code") {
    val plot = load(MCPlot(pens = Pens(Pen(updateCode = "crt 4"))).toString, twice)
    assertResult("crt 4crt 4")(plot.pens(0).updateCode)
  }

  test("convert everything") {
    val plot = load(
      MCPlot(setupCode = "crt 1", updateCode = "crt 2",
        pens = Pens(
          Pen(setupCode = "crt 3", updateCode = "crt 4"),
          Pen(setupCode = "crt 5", updateCode = "crt 6"))).toString, twice)
    assertResult("crt 1crt 1")(plot.setupCode)
    assertResult("crt 2crt 2")(plot.updateCode)
    assertResult("crt 3crt 3")(plot.pens(0).setupCode)
    assertResult("crt 4crt 4")(plot.pens(0).updateCode)
    assertResult("crt 5crt 5")(plot.pens(1).setupCode)
    assertResult("crt 6crt 6")(plot.pens(1).updateCode)
  }
}

class LoadingSomePlotsFromModelsTests extends TestPlotLoaderHelper{

  val bugHuntCamoPlots = List("""
PLOT
10
278
170
398
Current Hues
hue
number
0.0
255.0
0.0
5.0
true
false
"" "histogram [item 0 extract-hsb approximate-rgb (item 0 color) (item 1 color) (item 2 color)] of bugs\nset max-number-of-hue plot-y-max"
PENS
"default" 16.0 1 -16777216 true "" ""
""".replaceAll("\r\n", "\n"),
"""
PLOT
170
278
347
398
Current Brightnesses
<-- dark   bright --->
number
0.0
255.0
0.0
5.0
true
false
"" ""
PENS
"default" 16.0 1 -16777216 true "" "histogram [item 2 extract-hsb approximate-rgb (item 0 color) (item 1 color) (item 2 color)] of bugs\nset max-number-of-brightness plot-y-max"
""".replaceAll("\r\n", "\n"),
"""
PLOT
347
278
532
398
Current Saturations
<-- grayish   colorful-->
number
0.0
255.0
0.0
5.0
true
false
"" "histogram [item 1 extract-hsb approximate-rgb (item 0 color) (item 1 color) (item 2 color)] of bugs\nset max-number-of-saturation plot-y-max"
PENS
"default" 16.0 1 -16777216 true "" ""
""".replaceAll("\r\n", "\n"),
"""
PLOT
9
399
169
519
Initial Hues
hue
number
0.0
255.0
0.0
5.0
true
false
"histogram [item 0 extract-hsb  approximate-rgb (item 0 color) (item 1 color) (item 2 color)] of bugs" "if max-number-of-hue > 5 [set-plot-y-range 0 max-number-of-hue]"
PENS
"default" 16.0 1 -7500403 true "" ""
""".replaceAll("\r\n", "\n"),
"""
PLOT
347
399
532
519
Initial Saturations
<--grayish   colorful-->
number
0.0
255.0
0.0
5.0
true
false
"histogram [item 1 extract-hsb approximate-rgb (item 0 color) (item 1 color) (item 2 color)] of bugs" "if max-number-of-saturation > 5 [set-plot-y-range 0 max-number-of-saturation]"
PENS
"default" 16.0 1 -7500403 true "" ""
""".replaceAll("\r\n", "\n"),
"""
PLOT
170
398
347
518
Initial Brightnesses
<-- dark   bright --->
 number
0.0
255.0
0.0
5.0
true
false
"histogram [item 2 extract-hsb approximate-rgb (item 0 color) (item 1 color) (item 2 color)] of bugs" "if max-number-of-brightness > 5 [set-plot-y-range 0 max-number-of-brightness]"
PENS
"default" 16.0 1 -7500403 true "" ""
""".replaceAll("\r\n", "\n"),
"""
PLOT
259
133
530
253
Average HSB Values
time
value
0.0
10.0
0.0
255.0
true
true
"" ""
PENS
"hue" 1.0 0 -16777216 true "" "plotxy ticks mean [item 0 extract-hsb  approximate-rgb (item 0 color) (item 1 color) (item 2 color)] of bugs"
"brightness" 1.0 0 -955883 true "" "plotxy ticks mean [item 2 extract-hsb approximate-rgb (item 0 color) (item 1 color) (item 2 color)] of bugs"
"saturation" 1.0 0 -7500403 true "" "plotxy ticks mean [item 1 extract-hsb approximate-rgb (item 0 color) (item 1 color) (item 2 color)] of bugs"
""".replaceAll("\r\n", "\n"),
"""
PLOT
536
451
953
571
Vector difference in average genotype
time
vector diff
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "plotxy ticks vector-difference"
""".replaceAll("\r\n", "\n"))

  val adiabaticPlots = List("""
PLOT
651
171
889
350
Pressure vs. Time
time
pressure
0.0
20.0
0.0
100.0
true
false
"" ""
PENS
"default" 1.0 0 -955883 true "" "plotxy ticks (mean pressure-history)"
""".replaceAll("\r\n", "\n"),
"""
PLOT
653
358
892
535
Energy vs. Time
time
energy
0.0
50.0
0.0
40000.0
true
true
"set-plot-y-range 0 max list 1 (tot-particle-energy * 2)" ""
PENS
"Total" 1.0 0 -16777216 true "" "plotxy ticks total-energy"
"Gas" 1.0 0 -2674135 true "" "plotxy ticks tot-particle-energy"
"Piston" 1.0 0 -13345367 true "" "plotxy ticks piston-energy"
""".replaceAll("\r\n", "\n"),
"""
PLOT
651
11
889
161
Piston Height vs. Time
time
height
0.0
50.0
0.0
70.0
true
false
"set-plot-y-range 0 (2 * raw-height)" ""
PENS
"height" 1.0 0 -13345367 true "" "plotxy ticks piston-height"
""".replaceAll("\r\n", "\n")
    )

  testPlotsFromModels("Bug Hunt Camouflage", bugHuntCamoPlots)
  testPlotsFromModels("GasLab Adiabatic Piston", adiabaticPlots)
}



trait TestPlotLoaderHelper extends FunSuite {

  def testPlotsFromModels(model:String, plots:List[String]) {
    plots.zipWithIndex.foreach{ case (lines,i) =>
      test("test "+model+" plots without exploding: " + i) { load(lines) }
    }
  }

  def load(lines:String, converter: String => String = identity): Plot = {
    val plot = new PlotManager(new DummyLogoThunkFactory).newPlot("test")
    PlotLoader.parsePlot(lines.trim.split("\n"), plot, converter)
    plot
  }
}

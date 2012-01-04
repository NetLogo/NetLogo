// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite
import collection.mutable.Buffer

class WidgetIOTest extends FunSuite {

  def button = widget("""
    |BUTTON
    |131
    |10
    |209
    |43
    |NIL
    |go
    |T
    |1
    |T
    |OBSERVER
    |NIL
    |NIL
    |NIL
    |NIL
    |1""")

  def slider = widget("""
    |SLIDER
    |31
    |181
    |226
    |214
    |infection-chance
    |infection-chance
    |0
    |100
    |100
    |1
    |1
    |%
    |HORIZONTAL""")

  def switch = widget("""
    |SWITCH
    |30
    |62
    |133
    |95
    |a-switch
    |a-switch
    |1
    |1
    |-1000""")

  def chooser = widget("""
    |CHOOSER
    |36
    |72
    |174
    |117
    |my-choices
    |my-choices
    |42 "zoo" [1 2 3]
    |2""")

  def input = widget("""
    |INPUTBOX
    |62
    |138
    |297
    |198
    |my-input
    |null
    |1
    |0
    |String""")

  def monitor = widget("""
    |MONITOR
    |69
    |247
    |157
    |292
    |NIL
    |my-monitor
    |17
    |1
    |11""")

  def output = widget("""
    |OUTPUT
    |76
    |323
    |316
    |377
    |12""")

  def note = widget("""
    |TEXTBOX
    |81
    |414
    |231
    |432
    |random random.\nrandom random.
    |11
    |15.0
    |1""")

  def plot = widget("""
    |PLOT
    |83
    |455
    |283
    |605
    |plot 1
    |NIL
    |NIL
    |0.0
    |10.0
    |0.0
    |10.0
    |true
    |false
    |"crt 1 ; setup command" "crt 2 ; update command"
    |PENS
    |"default" 1.0 0 -16777216 true "" "plot count turtles"
    |"pen-1" 1.0 0 -7500403 true "" "plotxy 6 7"""")


  test("parse button"){
    verboseAssert(parse1(button), "ButtonSpec(Loc(131,10,209,43),None,go,true,OBSERVER,None,false)")
    verboseAssert(parseN(Buffer(button, button)), List(
      "ButtonSpec(Loc(131,10,209,43),None,go,true,OBSERVER,None,false)",
      "ButtonSpec(Loc(131,10,209,43),None,go,true,OBSERVER,None,false)"))
  }
  test("parse button display name"){
    val b = button
    b(5) = "press me" // set display name
    verboseAssert(parse1(b), "ButtonSpec(Loc(131,10,209,43),Some(press me),go,true,OBSERVER,None,false)")
  }
  test("parse button forever"){
    val b = button
    b(7) = "NIL" // set forever to NIL
    verboseAssert(parse1(b), "ButtonSpec(Loc(131,10,209,43),None,go,false,OBSERVER,None,false)")
  }
  test("parse button agent type"){
    val b = button
    b(10) = "TURTLE" // set agent type
    verboseAssert(parse1(b), "ButtonSpec(Loc(131,10,209,43),None,go,true,TURTLE,None,false)")
    b(10) = "PATCH" // set agent type
    verboseAssert(parse1(b), "ButtonSpec(Loc(131,10,209,43),None,go,true,PATCH,None,false)")
    b(10) = "LINK" // set agent type
    verboseAssert(parse1(b), "ButtonSpec(Loc(131,10,209,43),None,go,true,LINK,None,false)")
  }
  test("parse button action key"){
    val b = button
    b(12) = "X" // set forever to NIL
    verboseAssert(parse1(b), "ButtonSpec(Loc(131,10,209,43),None,go,true,OBSERVER,Some(X),false)")
  }
  test("parse button go time"){
    val b = button
    b(15) = "0" // set go time (0 = true)
    verboseAssert(parse1(b), "ButtonSpec(Loc(131,10,209,43),None,go,true,OBSERVER,None,true)")
  }
  
  test("parse slider"){
    verboseAssert(parse1(slider), "SliderSpec(Loc(31,181,226,214),infection-chance,0,100,100.0,1,Some(%),false)")
    verboseAssert(parseN(Buffer(slider, slider)), List(
      "SliderSpec(Loc(31,181,226,214),infection-chance,0,100,100.0,1,Some(%),false)",
      "SliderSpec(Loc(31,181,226,214),infection-chance,0,100,100.0,1,Some(%),false)"))
  }
  test("parse slider code"){
    val s = slider
    s(7) = "count turtles" // set min
    s(8) = "count turtles + 100" // set max
    verboseAssert(parse1(s), "SliderSpec(Loc(31,181,226,214),infection-chance,count turtles,count turtles + 100,100.0,1,Some(%),false)")
  }
  test("parse slider direction"){
    val s = slider
    s(13) = "VERTICAL" // set direction
    verboseAssert(parse1(s), "SliderSpec(Loc(31,181,226,214),infection-chance,0,100,100.0,1,Some(%),true)")
  }

  test("parse switch"){
    verboseAssert(parse1(switch), "SwitchSpec(Loc(30,62,133,95),a-switch,false)")
    verboseAssert(parseN(Buffer(switch, switch)), List(
      "SwitchSpec(Loc(30,62,133,95),a-switch,false)",
      "SwitchSpec(Loc(30,62,133,95),a-switch,false)"))
    val s = switch
    s(7) = "0" // set isOn to true...really.
    verboseAssert(parse1(s), "SwitchSpec(Loc(30,62,133,95),a-switch,true)")
  }

  test("parse chooser"){
    verboseAssert(parse1(chooser), """ChooserSpec(Loc(36,72,174,117),my-choices,42 "zoo" [1 2 3],2)""")
    verboseAssert(parseN(Buffer(chooser, chooser)), List(
      """ChooserSpec(Loc(36,72,174,117),my-choices,42 "zoo" [1 2 3],2)""",
      """ChooserSpec(Loc(36,72,174,117),my-choices,42 "zoo" [1 2 3],2)"""))
    val c = chooser
    c(8) = "0" // set selected index to 0
    verboseAssert(parse1(c), """ChooserSpec(Loc(36,72,174,117),my-choices,42 "zoo" [1 2 3],0)""")
    c(8) = "1" // set selected index to 1
    verboseAssert(parse1(c), """ChooserSpec(Loc(36,72,174,117),my-choices,42 "zoo" [1 2 3],1)""")
  }

  test("parse input"){
    verboseAssert(parse1(input), "InputBoxSpec(Loc(62,138,297,198),my-input,null,false,String)")
  }

  test("parse output"){
    verboseAssert(parse1(output), "OutputSpec(Loc(76,323,316,377),Some(12))")
  }

  test("parse monitor"){
    verboseAssert(parse1(monitor), "MonitorSpec(Loc(69,247,157,292),None,Some(my-monitor),Some(17),Some(11))")
  }

  test("parse note"){
    verboseAssert(parse1(note), "NoteSpec(Loc(81,414,231,432),random random.\\nrandom random.,Some(11),Some(15.0),true)")
  }

  test("parse plot"){
    verboseAssert(parse1(plot), "PlotSpec(Loc(83,455,283,605),plot 1,None,None,0.0,10.0,0.0,10.0,true,false,Some(crt 1 ; setup command),Some(crt 2 ; update command),List(PlotPenSpec(default,1.0,0,-16777216,true,None,Some(plot count turtles)), PlotPenSpec(pen-1,1.0,0,-7500403,true,None,Some(plotxy 6 7))))")
  }

  test("parse everyyhing") {
    verboseAssert(parseN(Buffer(button, slider, switch, chooser, input, output, monitor, note, plot)), List(
      "ButtonSpec(Loc(131,10,209,43),None,go,true,OBSERVER,None,false)",
      "SliderSpec(Loc(31,181,226,214),infection-chance,0,100,100.0,1,Some(%),false)",
      "SwitchSpec(Loc(30,62,133,95),a-switch,false)",
      """ChooserSpec(Loc(36,72,174,117),my-choices,42 "zoo" [1 2 3],2)""",
      "InputBoxSpec(Loc(62,138,297,198),my-input,null,false,String)",
      "OutputSpec(Loc(76,323,316,377),Some(12))",
      "MonitorSpec(Loc(69,247,157,292),None,Some(my-monitor),Some(17),Some(11))",
      "NoteSpec(Loc(81,414,231,432),random random.\\nrandom random.,Some(11),Some(15.0),true)",
      "PlotSpec(Loc(83,455,283,605),plot 1,None,None,0.0,10.0,0.0,10.0,true,false,Some(crt 1 ; setup command),Some(crt 2 ; update command),List(PlotPenSpec(default,1.0,0,-16777216,true,None,Some(plot count turtles)), PlotPenSpec(pen-1,1.0,0,-7500403,true,None,Some(plotxy 6 7))))"
    ))
  }

  def widget(s:String): Buffer[String] = s.trim.stripMargin.split("\n").toBuffer
  def parse1(widget: Buffer[String]): String = parseN(Buffer(widget))(0)
  def parseN(widgets: Buffer[Buffer[String]]): List[String] = {
    WidgetIO.parseWidgets(widgets.map(_ :+ "").flatten).map(_.toString).toList
  }

  def verboseAssert(actual:Any, expected:Any){
    if( actual != expected ){
      println("assertion error. expected followed by actual:")
      println("================")
      println(expected)
      println(actual)
      println("================")
    }
    assert(actual === expected)
  }
}


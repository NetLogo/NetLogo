// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat


import org.nlogo.core.{ CompilationOperand, LiteralParser,
  Model, Program, SourceRewriter }
import org.nlogo.core.{ Button, CompilerException, Monitor,
  Pen, Plot, Slider, Switch, View }

import org.scalatest.FunSuite

import scala.util.Try

class ModelConverterTests extends FunSuite with ConversionHelper {
  test("if the model is empty, returns the model") {
    val model = Model()
    assertResult(model)(convert(model))
  }

  test("if a code tab contains the affected prims, runs the code tab rewrites on the code tab") {
    val model = Model(code = "to bar fd 1 end")
    val convertedModel = convert(model, conversion(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("fd")))
    assertResult("globals [foo]\nto bar fd 1 end")(convertedModel.code)
  }

  test("if the model doesn't contain any targets, returns the model") {
    val model = Model(code = "to foo fd 1 bk 1 end")
    assertResult(model)(convert(model, conversion(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("left", "right"))))
  }

  test("if the model code doesn't compile when passed in, returns a failure") {
    val model = Model(code = "to foo fd 1")
    tryConvert(model, conversion(name = "add global foo", codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("fd"))) match {
      case ErroredConversion(m, e) =>
        assertResult(model)(m)
        assert(e.conversionDescription === "add global foo")
        assert(e.componentDescription === "code tab")
        assert(e.errors.head.isInstanceOf[CompilerException])
      case other => fail(s"Expected failure, got $other")
    }
  }

  test("if a model component doesn't compile, returns a component failure") {
    val model = Model(widgets = Seq(View(), Button(Some("fd 1 foobar"), 0, 0, 0, 0)))
    tryConvert(model, conversion(name = "fd to back", otherCodeConversions = Seq(_.replaceCommand("fd" -> "bk {0}")), targets = Seq("fd"))) match {
      case ConversionWithErrors(_, m, e) =>
        assert(e.head.componentDescription === "NetLogo interface")
        assert(e.head.conversionDescription === "fd to back")
        assert(e.head.errors.head.isInstanceOf[CompilerException])
      case other => fail(s"Expected failure, got $other")
    }
  }

  test("applies multiple conversions when supplied") {
    val model = Model(code = "to foo fd 1 bk 1 end")
    assert(convert(model,
      conversion(codeTabConversions = Seq(_.replaceCommand("fd" -> "rt 90")), targets = Seq("fd")),
      conversion(codeTabConversions = Seq(_.replaceCommand("bk" -> "lt 90")), targets = Seq("bk"))).code ==
        "to foo rt 90 lt 90 end")
  }

  test("if a button contains the affected prims, runs the code tab rewrites on the code tab") {
    val model = Model(widgets = Seq(View(), Button(Some("fd 1"), 0, 0, 0, 0)))
    assertResult("globals [foo]")(convert(model, conversion(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("fd"))).code)
  }

  test("if a slider contains the affected prims, runs the code tab rewrites on the code tab") {
    val model = Model(widgets = Seq(View(), Slider(variable = Some("bar"), max = "e 10")))
    assertResult("globals [foo]")(convert(model, conversion(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("e"))).code)
  }

  test("if a monitor contains the affected prims, runs the code tab rewrites on the code tab") {
    val model = Model(widgets = Seq(View(), Monitor(source = Some("e 10"), 0, 0, 0, 0, None, 1)))
    assertResult("globals [foo]")(convert(model, conversion(codeTabConversions = Seq(_.addGlobal("foo")),targets =  Seq("e"))).code)
  }

  test("if a plot contains the affected prims, runs the code tab rewrites on the code tab") {
    val model = Model(widgets = Seq(View(), Plot(display = None, setupCode = "plot e 10")))
    assertResult("globals [foo]")(convert(model, conversion(codeTabConversions = Seq(_.addGlobal("foo")),targets =  Seq("e"))).code)
  }

  test("if a plot pen contains the affected prims, runs the code tab rewrites on the code tab") {
    val model = Model(widgets = Seq(View(), Plot(display = None, pens = List(Pen(display = "", setupCode = "plot e 10")))))
    assertResult("globals [foo]")(convert(model, conversion(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("e"))).code)
  }

  test("if a widget contains the affected prims, runs the source rewrites on the widget") {
    val model = Model(widgets = Seq(View(), Button(Some("fd 1"), 0, 0, 0, 0)))
    assertResult("bk 1")(convert(model, conversion(otherCodeConversions = Seq((_.replaceCommand("fd" -> "bk 1"))), targets = Seq("fd"))).widgets(1).asInstanceOf[Button].source.get)
  }

  test("if the model has a widgets which don't compile, converts all compiling widgets") {
    val model = Model(widgets = Seq(View(), Button(Some("fd 1"), 0, 0, 0, 0), Button(Some("qux"), 0, 0, 0, 0)))
    assertResult("bk 1")(convert(model, conversion(otherCodeConversions = Seq((_.replaceCommand("fd" -> "bk 1"))), targets = Seq("fd"))).widgets(1).asInstanceOf[Button].source.get)
  }

  test("converts widgets which reference code tab code") {
    val model = Model(code = "to qux rt 30 end", widgets = Seq(View(), Button(Some("fd 1"), 0, 0, 0, 0), Button(Some("qux fd 2"), 0, 0, 0, 0)))
    assertResult("bk 1")(convert(model, conversion(otherCodeConversions = Seq(_.replaceCommand("fd" -> "bk 1")), targets = Seq("fd"))).widgets(1).asInstanceOf[Button].source.get)
    assertResult("qux bk 1")(convert(model, conversion(otherCodeConversions = Seq((_.replaceCommand("fd" -> "bk 1"))), targets = Seq("fd"))).widgets(2).asInstanceOf[Button].source.get)
  }

  test("converts code tab when referencing interface values") {
    val model = Model(code = "to foo if on? [ fd 1 ] end", widgets = Seq(View(), Switch(Some("on?"))))
    assertResult("to foo if on? [ bk 1 ] end")(convert(model, conversion(codeTabConversions = Seq(_.replaceCommand("fd" -> "bk 1")), targets = Seq("fd"))).code)
  }

  test("if the model has a widgets which don't compile, continues to convert the code tab") {
    val model = Model(code = "to baz fd 1 end", widgets = Seq(View(), Button(Some("bar"), 0, 0, 0, 0)))
    assertResult("globals [foo]\nto baz fd 1 end")(convert(model, conversion(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("fd"))).code)
  }

  test("handles the conversion of movie prims") {
    val originalSource =
      """|to start
         |  movie-start user-new-file
         |end
         |to go
         |  if movie-status != "" [
         |    movie-grab-view
         |  ]
         |end
         |to finish
         |  movie-close
         |end
         |to abort
         |  movie-cancel
         |end""".stripMargin
    val convertedSource =
      """|extensions [vid]
         |globals [_recording-save-file-name]
         |to start
         |  set _recording-save-file-name user-new-file
         |  vid:start-recorder
         |end
         |to go
         |  if vid:recorder-status != "" [
         |    vid:record-view
         |  ]
         |end
         |to finish
         |  vid:save-recording _recording-save-file-name
         |end
         |to abort
         |  vid:reset-recorder
         |end""".stripMargin
    val model = Model(code = originalSource)
    val changes = Seq[SourceRewriter => String](
      _.addGlobal("_recording-save-file-name"),
      _.addExtension("vid"),
      _.replaceCommand("movie-grab-view" -> "vid:record-view"),
      _.replaceCommand("movie-grab-interface" -> "vid:record-interface"),
      _.replaceCommand("movie-cancel" -> "vid:reset-recorder"),
      _.replaceCommand("movie-close" -> "vid:save-recording _recording-save-file-name"),
      _.addCommand("movie-start" -> "set _recording-save-file-name {0}"),
      _.replaceCommand("movie-start" -> "vid:start-recorder"),
      _.replaceReporter("movie-status" -> "vid:recorder-status"))
    val targets = Seq("movie-start", "movie-cancel", "movie-close", "movie-grab-view", "movie-grab-interface", "movie-status")
    val converted = convert(model, conversion(codeTabConversions = changes, targets = targets))
    assertResult(convertedSource)(converted.code)
  }

  test("lambda-izes") {
    val conversionSet= AutoConversionList.conversions.filter(_._1 == "NetLogo 6.0-RC1").map(_._2).head
    val targets = Seq("?1")
    val model = Model(code = """|to foo run task [ clear-all ] foreach [] [ tick ] end to bar __ignore sort-by [?1 > ?2] [1 2 3] end
                                |to baz show is-reporter-task? 1 show is-command-task? task tick end""".stripMargin)
    val converted = convert(model, conversionSet)
    val expectedResult = """|to foo run [ [] ->  clear-all ] foreach [] [ tick ] end to bar __ignore sort-by [ [?1 ?2] -> ?1 > ?2 ] [1 2 3] end
                            |to baz show is-anonymous-reporter? 1 show is-anonymous-command? [ [] -> tick ] end""".stripMargin
    assertResult(expectedResult)(converted.code)
  }

  test("handles models with trailing comments properly") {
    val originalSource =
      """|to abort
         |  movie-cancel
         |end
         |; comment at end""".stripMargin
    val expectedSource =
      """|to abort
         |  vid:reset-recorder
         |end
         |; comment at end""".stripMargin

    val model = Model(code = originalSource)
    val changes = Seq[SourceRewriter => String](_.replaceCommand("movie-cancel" -> "vid:reset-recorder"))
    val targets = Seq("movie-cancel")
    val converted = convert(model, conversion(codeTabConversions = changes, targets = targets))
    assertResult(expectedSource)(converted.code)
  }

  test("handles models with includes properly") {
    val originalSource =
      """|__includes [ "foo.nls" ]
         |to foo
         |  bar
         |  fd 1
         |end""".stripMargin
    val expectedSource =
      """|__includes [ "foo.nls" ]
         |to foo
         |  bar
         |  rt 90
         |end""".stripMargin
    val model = Model(code = originalSource)
    val converted = convert(model, conversion(codeTabConversions = Seq(_.replaceCommand("fd" -> "rt 90")), targets = Seq("fd")))
    assertResult(expectedSource)(converted.code)
  }
}

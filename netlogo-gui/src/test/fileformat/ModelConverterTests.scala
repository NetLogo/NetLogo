// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.NetLogoLegacyDialect

import org.nlogo.core.{ CompilationOperand, FrontEndInterface, Model, Program, SourceRewriter }, FrontEndInterface.ProceduresMap
import org.nlogo.core.{ Button, Monitor, Pen, Plot, Slider, Switch, View }
import org.nlogo.core.{ DummyCompilationEnvironment, DummyExtensionManager }

import org.scalatest.FunSuite

class ModelConverterTests extends FunSuite {
  def converter(conversions: Model => Seq[ConversionSet] = (_ => Seq())) =
    new ModelConverter(VidExtensionManager, FooCompilationEnvironment, NetLogoLegacyDialect, conversions)

  val componentConverters = Seq(new WidgetConverter() {})

  def convert(model: Model, conversions: ConversionSet*): Model =
    converter(_ => conversions)(model, componentConverters)

  test("if the model is empty, returns the model") {
    val model = Model()
    assertResult(model)(convert(model))
  }

  test("if a code tab contains the affected prims, runs the code tab rewrites on the code tab") {
    val model = Model(code = "to bar fd 1 end")
    val convertedModel = convert(model, ConversionSet(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("fd")))
    assertResult("globals [foo]\nto bar fd 1 end")(convertedModel.code)
  }

  test("if the model doesn't contain any targets, returns the model") {
    val model = Model(code = "to foo fd 1 bk 1 end")
    assertResult(model)(convert(model, ConversionSet(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("left", "right"))))
  }

  test("if the model code tab doesn't compile, returns the model as-is") {
    val model = Model(code = "fd 1")
    assert(convert(model, ConversionSet(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("fd"))) == model)
  }

  test("applies multiple conversions when supplied") {
    val model = Model(code = "to foo fd 1 bk 1 end")
    assert(convert(model,
      ConversionSet(codeTabConversions = Seq(_.replaceCommand("fd" -> "rt 90")), targets = Seq("fd")),
      ConversionSet(codeTabConversions = Seq(_.replaceCommand("bk" -> "lt 90")), targets = Seq("bk"))).code ==
        "to foo rt 90 lt 90 end")
  }

  test("if a button contains the affected prims, runs the code tab rewrites on the code tab") {
    val model = Model(widgets = Seq(View(), Button(Some("fd 1"), 0, 0, 0, 0)))
    assertResult("globals [foo]")(convert(model, ConversionSet(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("fd"))).code)
  }

  test("if a slider contains the affected prims, runs the code tab rewrites on the code tab") {
    val model = Model(widgets = Seq(View(), Slider(variable = Some("bar"), max = "e 10")))
    assertResult("globals [foo]")(convert(model, ConversionSet(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("e"))).code)
  }

  test("if a monitor contains the affected prims, runs the code tab rewrites on the code tab") {
    val model = Model(widgets = Seq(View(), Monitor(source = Some("e 10"), 0, 0, 0, 0, None, 1)))
    assertResult("globals [foo]")(convert(model, ConversionSet(codeTabConversions = Seq(_.addGlobal("foo")),targets =  Seq("e"))).code)
  }

  test("if a plot contains the affected prims, runs the code tab rewrites on the code tab") {
    val model = Model(widgets = Seq(View(), Plot(display = None, setupCode = "plot e 10")))
    assertResult("globals [foo]")(convert(model, ConversionSet(codeTabConversions = Seq(_.addGlobal("foo")),targets =  Seq("e"))).code)
  }

  test("if a plot pen contains the affected prims, runs the code tab rewrites on the code tab") {
    val model = Model(widgets = Seq(View(), Plot(display = None, pens = List(Pen(display = "", setupCode = "plot e 10")))))
    assertResult("globals [foo]")(convert(model, ConversionSet(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("e"))).code)
  }

  test("if a widget contains the affected prims, runs the source rewrites on the widget") {
    val model = Model(widgets = Seq(View(), Button(Some("fd 1"), 0, 0, 0, 0)))
    assertResult("bk 1")(convert(model, ConversionSet(otherCodeConversions = Seq((_.replaceCommand("fd" -> "bk 1"))), targets = Seq("fd"))).widgets(1).asInstanceOf[Button].source.get)
  }

  test("if the model has a widgets which doesn't compile, converts all compiling widgets") {
    val model = Model(widgets = Seq(View(), Button(Some("fd 1"), 0, 0, 0, 0), Button(Some("qux"), 0, 0, 0, 0)))
    assertResult("bk 1")(convert(model, ConversionSet(otherCodeConversions = Seq((_.replaceCommand("fd" -> "bk 1"))), targets = Seq("fd"))).widgets(1).asInstanceOf[Button].source.get)
  }

  test("converts widgets which reference code tab code") {
    val model = Model(code = "to qux rt 30 end", widgets = Seq(View(), Button(Some("fd 1"), 0, 0, 0, 0), Button(Some("qux fd 2"), 0, 0, 0, 0)))
    assertResult("bk 1")(convert(model, ConversionSet(otherCodeConversions = Seq(_.replaceCommand("fd" -> "bk 1")), targets = Seq("fd"))).widgets(1).asInstanceOf[Button].source.get)
    assertResult("qux bk 1")(convert(model, ConversionSet(otherCodeConversions = Seq((_.replaceCommand("fd" -> "bk 1"))), targets = Seq("fd"))).widgets(2).asInstanceOf[Button].source.get)
  }

  test("converts code tab when referencing interface values") {
    val model = Model(code = "to foo if on? [ fd 1 ] end", widgets = Seq(View(), Switch(Some("on?"))))
    assertResult("to foo if on? [ bk 1 ] end")(convert(model, ConversionSet(codeTabConversions = Seq(_.replaceCommand("fd" -> "bk 1")), targets = Seq("fd"))).code)
  }

  test("if the model has a widgets which don't compile, continues to convert the code tab") {
    val model = Model(code = "to baz fd 1 end", widgets = Seq(View(), Button(Some("bar"), 0, 0, 0, 0)))
    assertResult("globals [foo]\nto baz fd 1 end")(convert(model, ConversionSet(codeTabConversions = Seq(_.addGlobal("foo")), targets = Seq("fd"))).code)
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
    val converted = convert(model, ConversionSet(codeTabConversions = changes, targets = targets))
    assertResult(convertedSource)(converted.code)
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
    val converted = convert(model, ConversionSet(codeTabConversions = Seq(_.replaceCommand("fd" -> "rt 90")), targets = Seq("fd")))
    assertResult(expectedSource)(converted.code)
  }
}

object VidExtensionManager extends DummyExtensionManager {
  import org.nlogo.core.{ Syntax, Primitive, PrimitiveCommand, PrimitiveReporter}

  override def anyExtensionsLoaded = true
  override def importExtension(path: String, errors: org.nlogo.core.ErrorSource): Unit = { }
  override def replaceIdentifier(name: String): Primitive = {
    name match {
      case "VID:SAVE-RECORDING" =>
        new PrimitiveCommand { override def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType)) }
      case "VID:RECORDER-STATUS" =>
        new PrimitiveReporter { override def getSyntax = Syntax.reporterSyntax(ret = Syntax.StringType) }
      case vid if vid.startsWith("VID") =>
        new PrimitiveCommand { override def getSyntax = Syntax.commandSyntax() }
      case _ => null
    }
  }
}

object FooCompilationEnvironment extends DummyCompilationEnvironment {
  import java.nio.file.Files
  override def resolvePath(filename: String): String = {
    val file = Files.createTempFile("foo", ".nls")
    Files.write(file, "to bar bk 1 end".getBytes)
    file.toString
  }
}

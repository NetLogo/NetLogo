// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.agent.{BooleanConstraint, ChooserConstraint, InputBoxConstraint, SliderConstraint}
import org.nlogo.api.{ FileIO, LogoException, 
                      ModelReader, ModelSection, ValueConstraint, Version}
import org.nlogo.core.LogoList
import org.nlogo.core.Program
import org.nlogo.core.CompilerException
import org.nlogo.plot.PlotLoader
import org.nlogo.shape.{LinkShape, VectorShape}
import org.nlogo.api.StringUtils.escapeString
import org.nlogo.api.PreviewCommands

object HeadlessModelOpener {
  def protocolSection(path: String) =
    ModelReader.parseModel(FileIO.file2String(path)).get(ModelSection.BehaviorSpace).mkString("", "\n", "\n")
}

// this class is an abomination
// everything works off of side effects, asking the workspace to update something
// but not only that, some of the internals of this class work off side effects as well
// when they don't have to. - JC 10/27/09
class HeadlessModelOpener(ws: HeadlessWorkspace) {

  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  def openFromMap(map: java.util.Map[ModelSection, Array[String]]) {

    // get out if the model is opened. (WHY? - JC 10/27/09)
    if (ws.modelOpened) throw new IllegalStateException
    ws.modelOpened = true

    // get out if unknown version
    val netLogoVersion = map.get(ModelSection.Version).head
    if (!Version.knownVersion(netLogoVersion))
      throw new IllegalStateException("unknown NetLogo version: " + netLogoVersion)

    // parse all the widgets in the WIDGETS section
    val (interfaceGlobals, constraints, buttonCode, monitorCode, interfaceGlobalCommands) = {
      WidgetParser.parseWidgets(map.get(ModelSection.Interface), netLogoVersion)
    }

    // read system dynamics modeler diagram
    val sdmLines = map.get(ModelSection.SystemDynamics)
    if (!sdmLines.isEmpty) ws.aggregateManager.load(sdmLines.mkString("", "\n", "\n"), ws)

    // read procedures, compile them.
    val results = {
      val code = map.get(ModelSection.Code).mkString("", "\n", "\n")
      // we could convert right here.
      // we'd still need to convert slider constraints, plots, monitors and buttons.
      // JC - 9/14/10
      // val convertedCode = ws.autoConvert(code, false, false, netLogoVersion)
      import collection.JavaConverters._
      val newProg = Program.empty().copy(interfaceGlobals = interfaceGlobals)
      ws.compiler.compileProgram(code, newProg, ws.getExtensionManager, ws.getCompilationEnvironment)
    }
    ws.setProcedures(results.proceduresMap)
    ws.codeBits.clear() //(WTH IS THIS? - JC 10/27/09)

    // Read preview commands. If the model doesn't specify preview commands, the default ones will be used.
    val previewCommandsSource = map.get(ModelSection.PreviewCommands).mkString("\n")
    ws.previewCommands = PreviewCommands(previewCommandsSource)

    // parse turtle and link shapes, updating the workspace.
    parseShapes(map.get(ModelSection.TurtleShapes), map.get(ModelSection.LinkShapes), netLogoVersion)

    ws.getHubNetManager.load(map.get(ModelSection.HubNetClient), netLogoVersion)

    ws.init()
    ws.world.program(results.program)

    // test code is mixed with actual code here, which is a bit funny.
    if (ws.compilerTestingMode)
      testCompileWidgets(results.program, netLogoVersion, buttonCode.toList, monitorCode.toList)
    else
      finish(Map() ++ constraints, results.program, interfaceGlobalCommands)
  }

  private def parseShapes(turtleShapeLines: Array[String], linkShapeLines: Array[String], netLogoVersion: String) {
    ws.world.turtleShapeList.replaceShapes(VectorShape.parseShapes(turtleShapeLines, netLogoVersion))
    if (turtleShapeLines.isEmpty) ws.world.turtleShapeList.add(VectorShape.getDefaultShape)

    // A new model is being loaded, so get rid of all previous shapes
    ws.world.linkShapeList.replaceShapes(LinkShape.parseShapes(linkShapeLines, netLogoVersion))
    if (linkShapeLines.isEmpty) ws.world.linkShapeList.add(LinkShape.getDefaultLinkShape)
  }

  private def finish(constraints: Map[String, List[String]], program: Program, interfaceGlobalCommands: StringBuilder) {
    ws.world.realloc()

    val errors = ws.plotManager.compileAllPlots()
    if(errors.nonEmpty) throw errors(0)

    for ((vname, spec) <- constraints) {
      val con: ValueConstraint = spec(0) match {
        // to autoconvert sliders, we would have to convert spec 1, 2 and 3.
        // JC - 9/14/10
        case "SLIDER" =>
          SliderConstraint.makeSliderConstraint(
            ws.world.observer(), spec(1), spec(2), spec(3), spec(4).toDouble, vname, ws)
        case "CHOOSER" =>
          val vals = ws.compiler.readFromString(spec(1), ws.world.program.dialect.is3D).asInstanceOf[LogoList]
          val defaultIndex = spec(2).toInt
          val defaultAsString = org.nlogo.api.Dump.logoObject(vals.get(defaultIndex), true, false)
          interfaceGlobalCommands.append("set " + vname + " " + defaultAsString + "\n")
          new ChooserConstraint(vals, defaultIndex)
        case "SWITCH" => new BooleanConstraint(spec(1))
        case "INPUTBOX" =>
          var defaultVal: AnyRef = spec(1)
          if (spec(2) == "Number" || spec(2) == "Color")
            defaultVal = ws.compiler.readNumberFromString(spec(1), ws.world,
              ws.getExtensionManager,
              ws.world.program.dialect.is3D)
          new InputBoxConstraint(spec(2), defaultVal)
      }
      ws.world.observer().variableConstraint(ws.world.observerOwnsIndexOf(vname.toUpperCase), con)
    }
    ws.command(interfaceGlobalCommands.toString)
  }

  private def testCompileWidgets(program: Program, netLogoVersion: String, buttons: List[String], monitors:List[String]) {
    val errors = ws.plotManager.compileAllPlots()
    if(errors.nonEmpty) throw errors(0)
    for (widgetSource <- buttons ::: monitors)
      // we could convert monitors and buttons by using this line:
      // ws.autoConvert(widgetSource, true, false, netLogoVersion)
      // JC - 9/14/10
      try ws.compileCommands(widgetSource)
      catch {
        case ex: CompilerException =>
          println("compiling: \"" + ModelReader.stripLines(widgetSource) + "\"")
          throw ex
      }
  }

  private object WidgetParser {

    def parseWidgets(widgetsSection: Array[String], netLogoVersion: String) = {

      // parsing widgets dumps information into these four mutable vals.
      // as well as a few places in the workspace.
      val interfaceGlobals = new collection.mutable.ArrayBuffer[String]
      val constraints = new collection.mutable.HashMap[String, List[String]]
      val buttons = new collection.mutable.ArrayBuffer[String] // for TestCompileAll
      val monitors = new collection.mutable.ArrayBuffer[String] // for TestCompileAll
      val interfaceGlobalCommands = new StringBuilder

      //===
      // each widget type has its own parsing method
      //===

      def parseSlider(widget: Array[String]) {
        interfaceGlobals += widget(6)
        interfaceGlobalCommands.append("set " + widget(6) + " " + widget(9) + "\n")
        constraints(widget(6)) = List("SLIDER", widget(7), widget(8), widget(10), widget(9))
      }

      def parseSwitch(widget: Array[String]) {
        interfaceGlobals += widget(6)
        val defaultAsString = (widget(7).toDouble == 0).toString
        interfaceGlobalCommands.append("set " + widget(6) + " " + defaultAsString + "\n")
        constraints(widget(6)) = List("SWITCH", defaultAsString)
      }

      def parseChoiceOrChooser(widget: Array[String]) {
        interfaceGlobals += widget(6)
        val valSpec = "[" + widget(7) + "]"
        constraints(widget(6)) = List("CHOOSER", valSpec, widget(8))
      }

      def parseInputBox(widget: Array[String]) {
        interfaceGlobals += widget(5)
        val defaultVal = escapeString(ModelReader.restoreLines(widget(6)))
        if (widget(9) == "Number" || widget(9) == "Color")
          interfaceGlobalCommands.append("set " + widget(5) + " " + defaultVal + "\n")
        else
          interfaceGlobalCommands.append("set " + widget(5) + " \"" + defaultVal + "\"\n")
        constraints(widget(5)) = List("INPUTBOX", defaultVal, widget(9))
      }

      def parsePlot(widget: Array[String]) {
        // ick, side effects.
        // might replace identity soon as we might actually convert old models for headless.
        // JC - 9/14/10
        PlotLoader.parsePlot(widget, ws.plotManager.newPlot(""), identity)
      }

      def parseButton(widget: Array[String]) {
        val buttonSource = ModelReader.restoreLines(widget(6)) match {
          case "NIL" => ""
          case s => s
        }
        buttons +=
          (widget(10) match {
            case "TURTLE" => "ask turtles [" + buttonSource + "\n]" // newline to protect against comments
            case "PATCH"  => "ask patches [" + buttonSource + "\n]" // newline to protect against comments
            case "OBSERVER" => buttonSource
          })
      }

      def parseMonitor(widget: Array[String]) {
        val monitorSource = widget(6)
        if (monitorSource != "NIL")
        // add "__ignore" to turn the reporter into a command, so we can handle buttons and
        // monitors uniformly.  newline to protect against comments
          monitors += "__ignore (" + ModelReader.restoreLines(monitorSource) + "\n)"
      }

      def parseGraphicsWindow(widget: Array[String]) {
        ws.loadWorld(widget, netLogoVersion, ws)
      }

      // finally parse all the widgets in the WIDGETS section
      val widgets = ModelReader.parseWidgets(widgetsSection)

      import collection.JavaConverters._
      for (widget <- widgets.asScala.map(_.asScala.toArray))
        widget(0) match {
          case "SLIDER" => parseSlider(widget)
          case "SWITCH" => parseSwitch(widget)
          case "CHOICE" | "CHOOSER" => parseChoiceOrChooser(widget)
          case "INPUTBOX" => parseInputBox(widget)
          case "PLOT" => parsePlot(widget)
          case "BUTTON" if ws.compilerTestingMode => parseButton(widget)
          case "MONITOR" if ws.compilerTestingMode => parseMonitor(widget)
          case "GRAPHICS-WINDOW" => parseGraphicsWindow(widget)
          case _ => // ignore
        }

      (interfaceGlobals, constraints, buttons, monitors, interfaceGlobalCommands)

    }

  }
}

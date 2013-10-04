// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.agent.{BooleanConstraint, ChooserConstraint, InputBoxConstraint, NumericConstraint}
import org.nlogo.api.{CompilerException, FileIO, LogoList,
                      ModelReader, ModelSection, Program, ValueConstraint, Version}
import org.nlogo.shape.{LinkShape, VectorShape}

object HeadlessModelOpener {
  def protocolSection(path: String) =
    ModelReader.parseModel(FileIO.file2String(path))(
      ModelSection.BehaviorSpace).mkString("", "\n", "\n")
}

// this class is an abomination
// everything works off of side effects, asking the workspace to update something
// but not only that, some of the internals of this class work off side effects as well
// when they don't have to. - JC 10/27/09
class HeadlessModelOpener(ws: HeadlessWorkspace) {

  def openFromMap(map: ModelReader.ModelMap) {

    require(!ws.modelOpened, "HeadlessWorkspace can only open one model")
    ws.setModelOpened()

    // get out if unknown version
    val netLogoVersion = map(ModelSection.Version).head
    if (!Version.knownVersion(netLogoVersion))
      throw new IllegalStateException("unknown NetLogo version: " + netLogoVersion)

    // parse all the widgets in the WIDGETS section
    val (interfaceGlobals, constraints, buttonCode, monitorCode, interfaceGlobalCommands) =
      new WidgetParser(ws).parseWidgets(map(ModelSection.Interface), netLogoVersion)

    // read procedures, compile them.
    val results = {
      val code = map(ModelSection.Code).mkString("", "\n", "\n")
      ws.compiler.compileProgram(
        code, Program.empty.copy(
          interfaceGlobals = interfaceGlobals), ws.getExtensionManager)
    }
    ws.procedures = results.proceduresMap
    ws.clearRunCache()

    // read preview commands. (if the model doesn't specify preview commands, allow the default ones
    // from our superclass to stand)
    val previewCommands = map(ModelSection.PreviewCommands).mkString("", "\n", "\n")
    if (!previewCommands.trim.isEmpty) ws.previewCommands = previewCommands

    // parse turtle and link shapes, updating the workspace.
    parseShapes(map(ModelSection.TurtleShapes).toArray,
                map(ModelSection.LinkShapes).toArray,
                netLogoVersion)

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
        case "SLIDER" =>
          new NumericConstraint(spec(4))
        case "CHOOSER" =>
          val vals = ws.compiler.frontEnd.readFromString(spec(1)).asInstanceOf[LogoList]
          val defaultIndex = spec(2).toInt
          val defaultAsString = org.nlogo.api.Dump.logoObject(vals.get(defaultIndex), true, false)
          interfaceGlobalCommands.append("set " + vname + " " + defaultAsString + "\n")
          new ChooserConstraint(vals, defaultIndex)
        case "SWITCH" => new BooleanConstraint(spec(1))
        case "INPUTBOX" =>
          var defaultVal: AnyRef = spec(1)
          if (spec(2) == "Number" || spec(2) == "Color")
            defaultVal = ws.compiler.frontEnd.readNumberFromString(spec(1), ws.world,
              ws.getExtensionManager)
          new InputBoxConstraint(spec(2), defaultVal)
      }
      ws.world.observer().setConstraint(ws.world.observerOwnsIndexOf(vname.toUpperCase), con)
    }
    ws.command(interfaceGlobalCommands.toString)
  }

  private def testCompileWidgets(program: Program, netLogoVersion: String, buttons: List[String], monitors:List[String]) {
    val errors = ws.plotManager.compileAllPlots()
    if(errors.nonEmpty) throw errors(0)
    for (widgetSource <- buttons ::: monitors)
      try ws.compileCommands(widgetSource)
      catch {
        case ex: CompilerException =>
          println("compiling: \"" + ModelReader.stripLines(widgetSource) + "\"")
          throw ex
      }
  }

}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.headless

import org.nlogo.workspace
import workspace.WorldLoader
import org.nlogo.plot.PlotLoader
import org.nlogo.agent.{BooleanConstraint, ChooserConstraint, InputBoxConstraint, NumericConstraint}
import org.nlogo.api.{ValueConstraint, Version}
import org.nlogo.core.{model, Shape, ShapeParser, CompilerException, Program, ConstraintSpecification, LogoList, Model},
  model.ModelReader,
  ConstraintSpecification._,
  Shape.{ LinkShape => CoreLinkShape, VectorShape => CoreVectorShape }

import org.nlogo.shape.{ShapeConverter, LinkShape, VectorShape}

// this class is an abomination
// everything works off of side effects, asking the workspace to update something
// but not only that, some of the internals of this class work off side effects as well
// when they don't have to. - JC 10/27/09
class HeadlessModelOpener(ws: HeadlessWorkspace) {
  def stripLines(st: String): String =
    st.flatMap{
      case '\n' => "\\n"
      case '\\' => "\\\\"
      case '\"' => "\\\""
      case c => c.toString
    }

  def openFromModel(model: Model) {
    require(!ws.modelOpened, "HeadlessWorkspace can only open one model")
    ws.setModelOpened()

    // get out if unknown version
    val netLogoVersion = model.version
    if (!Version.knownVersion(netLogoVersion))
      throw new IllegalStateException("unknown NetLogo version: " + netLogoVersion)

    val buttonCode = new collection.mutable.ArrayBuffer[String] // for TestCompileAll
    val monitorCode = new collection.mutable.ArrayBuffer[String] // for TestCompileAll

    WorldLoader.load(model.view, ws)

    for(plot <- model.plots)
      PlotLoader.loadPlot(plot, ws.plotManager.newPlot(""))

    // read procedures, compile them.
    val results = {
      val code = model.code
      ws.compiler.compileProgram(
        code, Program.empty.copy(
          interfaceGlobals = model.interfaceGlobals),
        ws.getExtensionManager, ws.flags)
    }
    ws.procedures = results.proceduresMap
    ws.clearRunCache()

    // read preview commands. (if the model doesn't specify preview commands, allow the default ones
    // from our superclass to stand)
    val previewCommands = model.previewCommands.mkString("", "\n", "\n")
    if (!previewCommands.trim.isEmpty) ws.previewCommands = previewCommands

    // parse turtle and link shapes, updating the workspace.
    parseShapes(model.turtleShapes,
                model.linkShapes,
                netLogoVersion)

    ws.init()
    ws.world.program(results.program)

    // test code is mixed with actual code here, which is a bit funny.
    if (ws.compilerTestingMode)
      testCompileWidgets(results.program, netLogoVersion, buttonCode.toList, monitorCode.toList)
    else
      finish(model.constraints, results.program, model.interfaceGlobalCommands.mkString("\n"))
  }


  private def parseShapes(turtleShapes: List[CoreVectorShape], linkShapes: List[CoreLinkShape], netLogoVersion: String) {
    ws.world.turtleShapeList.replaceShapes(
      turtleShapes.map(ShapeConverter.baseVectorShapeToVectorShape))
    if (turtleShapes.isEmpty) ws.world.turtleShapeList.add(VectorShape.getDefaultShape)

    // A new model is being loaded, so get rid of all previous shapes
    ws.world.linkShapeList.replaceShapes(
      linkShapes.map(ShapeConverter.baseLinkShapeToLinkShape))
    if (linkShapes.isEmpty) ws.world.linkShapeList.add(LinkShape.getDefaultLinkShape)
  }

  private def finish(constraints: Map[String, ConstraintSpecification], program: Program, interfaceGlobalCommands: String) {
    ws.world.realloc()

    val errors = ws.plotManager.compileAllPlots()
    if(errors.nonEmpty) throw errors(0)

    for ((vname, spec) <- constraints) {
      val con: ValueConstraint = spec match {
        case NumericConstraintSpecification(default) => new NumericConstraint(default)
        case ChoiceConstraintSpecification(vals, defaultIndex) => new ChooserConstraint(
          LogoList.fromIterator(vals.iterator),
          defaultIndex
        )
        case BooleanConstraintSpecification(default) => new BooleanConstraint(default)
        case StringInputConstraintSpecification(typeName, default) => new InputBoxConstraint(typeName, default)
        case NumericInputConstraintSpecification(typeName, default) => new InputBoxConstraint(typeName, default)
      }
      ws.world.observer().setConstraint(ws.world.observerOwnsIndexOf(vname.toUpperCase), con)
    }
    ws.command(interfaceGlobalCommands)
  }

  private def testCompileWidgets(program: Program, netLogoVersion: String, buttons: List[String], monitors:List[String]) {
    val errors = ws.plotManager.compileAllPlots()
    if(errors.nonEmpty) throw errors(0)
    for (widgetSource <- buttons ::: monitors)
      try ws.compileCommands(widgetSource)
      catch {
        case ex: CompilerException =>
          println("compiling: \"" + stripLines(widgetSource) + "\"")
          throw ex
      }
  }

}

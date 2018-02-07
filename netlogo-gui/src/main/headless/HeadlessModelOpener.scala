// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.agent.{BooleanConstraint, ChooserConstraint, CompilationManagement,
  ConstantSliderConstraint, InputBoxConstraint, NumericConstraint }
import org.nlogo.api.{ LogoException, NetLogoLegacyDialect, NetLogoThreeDDialect,
  SourceOwner, ValueConstraint, Version}
import org.nlogo.core.{ Button, CompilerException, ConstraintSpecification, LogoList, Model, Monitor, Program }
import org.nlogo.plot.PlotLoader
import org.nlogo.core.Shape.{ LinkShape => CoreLinkShape, VectorShape => CoreVectorShape }
import org.nlogo.shape.{LinkShape, VectorShape}
import org.nlogo.api.PreviewCommands

import org.nlogo.shape.{ShapeConverter, LinkShape, VectorShape}

// this class is an abomination
// everything works off of side effects, asking the workspace to update something
// but not only that, some of the internals of this class work off side effects as well
// when they don't have to. - JC 10/27/09
class HeadlessModelOpener(ws: HeadlessWorkspace) {

  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  def openFromModel(model: Model) {
    // get out if the model is opened. (WHY? - JC 10/27/09)
    if (ws.modelOpened) throw new IllegalStateException
    ws.setOpenModel(model)

    if (!Version.compatibleVersion(model.version))
      throw new IllegalStateException("unknown NetLogo version: " + model.version)

    ws.loadWorld(model.view, ws)

    for (plot <- model.plots)
      PlotLoader.loadPlot(plot, ws.plotManager.newPlot(""))

    val dialect = if (Version.is3D(model.version)) NetLogoThreeDDialect
      else NetLogoLegacyDialect

    // load system dynamics model (if present)
    ws.aggregateManager.load(model, ws.compilerServices)

    // read procedures, compile them.
    val results = {
      val additionalSources: Seq[SourceOwner] = if (ws.aggregateManager.isLoaded) Seq(ws.aggregateManager) else Seq()
      val code = model.code
      val newProg = Program.fromDialect(dialect).copy(interfaceGlobals = model.interfaceGlobals)
      ws.compiler.compileProgram(code, additionalSources, newProg, ws.getExtensionManager, ws.getCompilationEnvironment)
    }
    ws.procedures = results.proceduresMap
    ws.clearRunCache() //(WTH IS THIS? - JC 10/27/09)

    // Read preview commands. If the model doesn't specify preview commands, the default ones will be used.
    model.optionalSectionValue[PreviewCommands]("org.nlogo.modelsection.previewcommands").foreach(ws.previewCommands = _)

    // parse turtle and link shapes, updating the workspace.
    attachWorldShapes(model.turtleShapes, model.linkShapes)

    if (model.hasValueForOptionalSection("org.nlogo.modelsection.hubnetclient")) {
      ws.getHubNetManager.foreach(_.load(model))
    }

    ws.init()
    ws.world.asInstanceOf[CompilationManagement].program(results.program)

    // test code is mixed with actual code here, which is a bit funny.
    if (ws.compilerTestingMode)
      testCompileWidgets(
        model.widgets.collect { case b: Button => b },
        model.widgets.collect { case m: Monitor => m })
    else
      finish(model.constraints, results.program, model.interfaceGlobalCommands.mkString("\n"))
  }

  private def attachWorldShapes(turtleShapes: Seq[CoreVectorShape], linkShapes: Seq[CoreLinkShape]) = {
    ws.world.turtleShapes.replaceShapes(turtleShapes.map(ShapeConverter.baseVectorShapeToVectorShape))
    if (turtleShapes.isEmpty)
      ws.world.turtleShapes.add(VectorShape.getDefaultShape)

    ws.world.linkShapes.replaceShapes(linkShapes.map(ShapeConverter.baseLinkShapeToLinkShape))
    if (linkShapes.isEmpty)
      ws.world.linkShapes.add(LinkShape.getDefaultLinkShape)
  }

  private def finish(constraints: Map[String, ConstraintSpecification], program: Program, interfaceGlobalCommands: String) {
    ws.world.realloc()

    val errors = ws.plotManager.compileAllPlots()
    if(errors.nonEmpty) throw errors(0)

    import ConstraintSpecification._
    for ((vname, spec) <- constraints) {
      val con: ValueConstraint = spec match {
        case BoundedNumericConstraintSpecification(min, default, max, step) =>
          val constraint = new ConstantSliderConstraint(min.doubleValue, max.doubleValue, step.doubleValue)
          constraint.defaultValue = default.doubleValue
          constraint
        case NumericConstraintSpecification(default) => new NumericConstraint(default)
        case ChoiceConstraintSpecification(vals, defaultIndex) => new ChooserConstraint(
          LogoList.fromIterator(vals.iterator),
          defaultIndex
        )
        case BooleanConstraintSpecification(default) => new BooleanConstraint(default)
        case StringInputConstraintSpecification(typeName, default) => new InputBoxConstraint(typeName, default)
        case NumericInputConstraintSpecification(typeName, default) => new InputBoxConstraint(typeName, default)
      }
      ws.world.observer.setConstraint(ws.world.observerOwnsIndexOf(vname.toUpperCase), con)
    }

    ws.command(interfaceGlobalCommands)
  }

  private def testCompileWidgets(buttons: Seq[Button], monitors: Seq[Monitor]) {
    val errors = ws.plotManager.compileAllPlots()
    if(errors.nonEmpty) throw errors(0)
    for {
      button <- buttons
      source <- button.source
    }
      try ws.compileCommands(source, button.buttonKind)
      catch {
        case ex: CompilerException =>
          println("compiling: \"" + button + "\"")
          throw ex
      }
    for {
      monitor <- monitors
      source <- monitor.source
    }
      try ws.compileReporter(source)
      catch {
        case ex: CompilerException =>
          println("compiling: \"" + monitor + "\"")
          throw ex
      }
  }
}

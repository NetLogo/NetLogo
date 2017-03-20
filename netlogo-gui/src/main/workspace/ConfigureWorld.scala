// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.workspace

import org.nlogo.core.{ AgentKind, Button => CoreButton, Chooser => CoreChooser,
  CompilerException, InputBox => CoreInputBox, Model, NumericInput, Program,
  Slider => CoreSlider, StringInput, Switch => CoreSwitch, Widget }
import org.nlogo.agent.World
import org.nlogo.internalapi.CompiledModel
import org.nlogo.shape.ShapeConverter

// TODO: Consider moving parts of CompileAll (esp. procedure setup?) into this class
object ConfigureWorld {
  def apply(workspace: AbstractWorkspace, compiledModel: CompiledModel) = {
    import workspace.world
    import compiledModel.model
    world.createPatches(model.view.dimensions)
    world.patchSize(model.view.dimensions.patchSize)
    world.realloc()
    setDefaultValues(world, model.widgets)
    // TODO: Maybe these should be core.Shapes instead of org.nlogo.shape.VectorShape
    world.turtleShapes.replaceShapes(model.turtleShapes.map(ShapeConverter.baseShapeToShape))
    world.linkShapes.replaceShapes(model.linkShapes.map(ShapeConverter.baseLinkShapeToLinkShape))
  }

  def setDefaultValues(world: World, widgets: Seq[Widget]): Unit = {
    val observerGlobals: Seq[(String, AnyRef)] = widgets.collect {
      case CoreSwitch(Some(name), _, _, _, _, _, isOn) =>
        name -> Boolean.box(isOn)
      case CoreSlider(Some(name), _, _, _, _, _, _, _, default, _, _, _) =>
        name -> Double.box(default)
      case CoreChooser(Some(name), _, _, _, _, _, choices, selected) =>
        name -> choices(selected).value
      case CoreInputBox(Some(name), _, _, _, _, NumericInput(default, _)) =>
        name -> Double.box(default)
      case CoreInputBox(Some(name), _, _, _, _, StringInput(default, _, _)) =>
        name -> default
    }

    observerGlobals.foreach {
      case (name, value) =>
        if (world.observerOwnsIndexOf(name) != -1)
          world.setObserverVariableByName(name, value)
    }
  }
}

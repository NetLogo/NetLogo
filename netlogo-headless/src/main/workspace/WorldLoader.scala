// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.{WorldDimensions, View}

object WorldLoader {
  def load(view: View, worldInterface: WorldLoaderInterface): Unit = {
    val d = view.dimensions

    worldInterface.tickCounterLabel(view.tickCounterLabel)

    worldInterface.showTickCounter(view.showTickCounter)

    val width = getWidth(worldInterface, d)
    val patchSize = adjustPatchSize(worldInterface, d)
    val height = getHeight(worldInterface, d)
    worldInterface.setDimensions(d, patchSize)
    worldInterface.clearTurtles()
    worldInterface.fontSize(view.fontSize)

    worldInterface.changeTopology(d.wrappingAllowedInX, d.wrappingAllowedInY)
    worldInterface.updateMode(view.updateMode)
    worldInterface.frameRate(view.frameRate)
    worldInterface.setSize(width, height)
  }

  def getWidth(world: WorldLoaderInterface, d: WorldDimensions): Int = {
    val widgetWidth = world.calculateWidth(d.width, d.patchSize)
    val minWidth = world.getMinimumWidth
    widgetWidth max minWidth
  }

  def getHeight(world: WorldLoaderInterface, d: WorldDimensions): Int =
    world.calculateHeight(d.height, d.patchSize)

  def adjustPatchSize(world: WorldLoaderInterface, d: WorldDimensions): Double = {
    val widgetWidth = world.calculateWidth(d.width, d.patchSize)
    val minWidth = world.getMinimumWidth
    if(widgetWidth < minWidth)
      world.computePatchSize(minWidth - world.insetWidth(), d.width)
    else
      d.patchSize
  }
}

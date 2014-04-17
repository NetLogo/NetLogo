// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.I18N
import org.nlogo.core.{WorldDimensions, UpdateMode, View}

object WorldLoader {
  def load(view: View, worldInterface: WorldLoaderInterface) {
    val d = view.dimensions
    // set the visiblity of the ticks counter first because it changes the minimum size of the
    // viewWidget which could cause patchSize ugliness down the line ev 7/30/07
    val label = view.tickCounterLabel
    worldInterface.tickCounterLabel(if(label == "NIL") "" else label)

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
      world.computePatchSize(minWidth - world.insetWidth, d.width)
    else
      d.patchSize
  }
}

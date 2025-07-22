// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.{ View => CoreView, WorldDimensions }

class WorldLoader {
  def load(view: CoreView, worldInterface: WorldLoaderInterface): Unit = {
    val d = view.dimensions

    // set the visiblity of the ticks counter first because it changes the minimum size of the
    // viewWidget which could cause patchSize ugliness down the line ev 7/30/07
    worldInterface.tickCounterLabel(view.tickCounterLabel)
    worldInterface.showTickCounter(view.showTickCounter)

    worldInterface.setDimensions(d, adjustPatchSize(worldInterface, d))

    // we have to clear turtles before we change the topology otherwise we might have extra links
    // lying around in the world that go kerplooey when we try to reposition them and after we set
    // the dimensions because that's where every thing gets allocated initially. ev 7/19/06
    worldInterface.clearTurtles()

    worldInterface.fontSize(view.fontSize)

    worldInterface.changeTopology(d.wrappingAllowedInX, d.wrappingAllowedInY)

    worldInterface.updateMode(view.updateMode)
    worldInterface.frameRate(view.frameRate)

    worldInterface.setSize(view.width, view.height)
  }

  def getWidth(world: WorldLoaderInterface, d: WorldDimensions, v: CoreView): Int = {
    val widgetWidth = world.calculateWidth(d.width, d.patchSize)
    val minWidth = world.getMinimumWidth
    widgetWidth max minWidth
  }

  def getHeight(world: WorldLoaderInterface, d: WorldDimensions, patchSize: Double, v: CoreView): Int =
    world.calculateHeight(d.height, patchSize)

  def adjustPatchSize(world: WorldLoaderInterface, d: WorldDimensions): Double = {
    val widgetWidth = world.calculateWidth(d.width, d.patchSize)
    val minWidth = world.getMinimumWidth
    if(widgetWidth < minWidth)
      world.computePatchSize(minWidth - world.insetWidth(), d.width)
    else
      d.patchSize
  }
}

class WorldLoader3D extends WorldLoader {
  override def getWidth(world: WorldLoaderInterface, d: WorldDimensions, v: CoreView) =
    v.width
  override def getHeight(world: WorldLoaderInterface, d: WorldDimensions, adjustedPatchSize: Double, v: CoreView) =
    v.height
  override def adjustPatchSize(world: WorldLoaderInterface, d: WorldDimensions) =
    d.patchSize
}

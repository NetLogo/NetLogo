// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.{ I18N, UpdateMode, WorldDimensions }

class WorldLoader {

  val updateModeIndex = 21
  val tickCounterIndex = 23
  val tickCounterLabelIndex = 24
  val frameRateIndex = 25

  def load(strings: Array[String], worldInterface: WorldLoaderInterface) {
    val d = getWorldDimensions(strings)
    // set the visiblity of the ticks counter first because it changes the minimum size of the
    // viewWidget which could cause patchSize ugliness down the line ev 7/30/07
    if(strings.length > tickCounterLabelIndex) {
      val label = strings(tickCounterLabelIndex)
      worldInterface.tickCounterLabel(
        if(label == "NIL") ""
        else label)
    }
    else
      worldInterface.tickCounterLabel("ticks")
    if(strings.length > tickCounterIndex)
      worldInterface.showTickCounter(1 == strings(tickCounterIndex).toInt)
    else
      worldInterface.showTickCounter(true)
    var patchSize = strings(7).toDouble
    val width = getWidth(worldInterface, d, patchSize, strings)
    patchSize = adjustPatchSize(worldInterface, d, patchSize, strings)
    val height = getHeight(worldInterface, d, patchSize, strings)
    worldInterface.setDimensions(d, patchSize)
    // we have to clear turtles before we change the topology otherwise we might have extra links
    // lying around in the world that go kerplooey when we try to reposition them and after we set
    // the dimensions because that's where every thing gets allocated initially. ev 7/19/06
    worldInterface.clearTurtles()
    if(strings.length > 9)
      worldInterface.fontSize(strings(9).toInt)
    // note we ignore items 10, 11, 12 which had the old exactDraw
    // settings which are now always on - ST 5/27/05
    // ignore item 13, which was for old, now-removed hex support - ST 1/4/07
    var wrapX = true
    var wrapY = true
    if(strings.length > 15) {
      wrapX = 0 != strings(14).toInt
      wrapY = 0 != strings(15).toInt
    }
    worldInterface.changeTopology(wrapX, wrapY)
    worldInterface.updateMode(
      if(strings.length > updateModeIndex)
        UpdateMode.load(strings(updateModeIndex).toInt)
      else
        UpdateMode.Continuous)
    worldInterface.frameRate(
      if(strings.length > frameRateIndex)
        strings(frameRateIndex).toDouble
      else
        30)
    // ignore strings(22), used to be timeBasedUpdates flag - ST 1/25/07
    worldInterface.setSize(width, height)
  }

  def getWorldDimensions(strings: Array[String]): WorldDimensions = {
    var maxx = strings(5).toInt
    var maxy = strings(6).toInt
    var minx = -1
    var miny = -1
    if(maxx != -1 && maxy != -1) {
      minx = -maxx
      miny = -maxy
    }
    else if(strings.length > 20) {
      minx = strings(17).toInt
      maxx = strings(18).toInt
      miny = strings(19).toInt
      maxy = strings(20).toInt
    }
    new WorldDimensions(minx, maxx, miny, maxy)
  }

  def getWidth(world: WorldLoaderInterface, d: WorldDimensions, patchSize: Double, strings: Array[String]): Int = {
    val widgetWidth = world.calculateWidth(d.width, patchSize)
    val minWidth = world.getMinimumWidth
    widgetWidth max minWidth
  }

  def getHeight(world: WorldLoaderInterface, d: WorldDimensions, patchSize: Double, strings: Array[String]): Int =
    world.calculateHeight(d.height,  patchSize)

  def adjustPatchSize(world: WorldLoaderInterface, d: WorldDimensions, patchSize: Double, strings: Array[String]): Double = {
    val widgetWidth = world.calculateWidth(d.width, patchSize)
    val minWidth = world.getMinimumWidth
    if(widgetWidth < minWidth)
      world.computePatchSize(minWidth - world.insetWidth, d.width)
    else
      patchSize
  }
}

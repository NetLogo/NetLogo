// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.{ View => CoreView }
import org.nlogo.api.WorldResizer

class WorldLoader {
  def load(view: CoreView, worldInterface: WorldLoaderInterface) {
    val d = view.dimensions

    // set the visiblity of the ticks counter first because it changes the minimum size of the
    // viewWidget which could cause patchSize ugliness down the line ev 7/30/07
    val label = view.tickCounterLabel.getOrElse("")
    worldInterface.tickCounterLabel(label)
    worldInterface.showTickCounter(view.showTickCounter)

    // we have to clear turtles before we change the topology otherwise we might have extra links
    // lying around in the world that go kerplooey when we try to reposition them and after we set
    // the dimensions because that's where every thing gets allocated initially. ev 7/19/06
    worldInterface.clearTurtles()

    worldInterface.fontSize(view.fontSize)

    val adjustedDimensions = worldInterface.adjustDimensions(d)
    val (width, height) = worldInterface.calculateViewSize(adjustedDimensions, view)

    worldInterface.setDimensions(adjustedDimensions, false, WorldResizer.StopNonObserverJobs)
    worldInterface.updateMode(view.updateMode)
    worldInterface.frameRate(view.frameRate)
    worldInterface.setSize(width, height)
  }

}

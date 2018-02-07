// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core

/** used by Importer for resizing the world during an import.  also used by BehaviorSpace when the
  * experiment varies the world size */

object WorldResizer {
  sealed trait JobStop
  case object StopNothing extends JobStop
  case object StopNonObserverJobs extends JobStop
  case object StopEverything extends JobStop

  def stopNothing = StopNothing
  def stopNonObserverJobs = StopNonObserverJobs
  def stopEverything = StopEverything
}

import WorldResizer._

trait WorldResizer {
  @deprecated("use setDimensions(WorldDimensions, boolean, WorldResizer.JobStop) instead of setting dimensions and calling resizeView()", "6.1.0")
  def resizeView()

  def patchSize(patchSize: Double)

  @deprecated("use setDimensions(WorldDimensions, boolean, WorldResizer.JobStop) instead", "6.1.0")
  def setDimensions(dim: core.WorldDimensions): Unit = setDimensions(dim, false, StopNonObserverJobs)

  @deprecated("use setDimensions(WorldDimensions, boolean, WorldResizer.JobStop) instead", "6.1.0")
  def setDimensions(dim: core.WorldDimensions, patchSize: Double): Unit = setDimensions(dim, false, StopNonObserverJobs)

  def setDimensions(dim: core.WorldDimensions, showProgress: Boolean, stop: WorldResizer.JobStop): Unit
}

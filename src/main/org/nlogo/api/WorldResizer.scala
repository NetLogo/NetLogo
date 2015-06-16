// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/** used by Importer for resizing the world during an import.  also used by BehaviorSpace when the
  * experiment varies the world size */

trait WorldResizer {
  def resizeView(): Unit
  def patchSize(patchSize: Double): Unit
  def setDimensions(dim: WorldDimensions): Unit
  def setDimensions(dim: WorldDimensions, patchSize: Double): Unit
}

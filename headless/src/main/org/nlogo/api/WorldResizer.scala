// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/** used by Importer for resizing the world during an import.  also used by BehaviorSpace when the
  * experiment varies the world size */

trait WorldResizer {
  def resizeView()
  def patchSize(patchSize: Double)
  def setDimensions(dim: WorldDimensions)
  def setDimensions(dim: WorldDimensions, patchSize: Double)
}

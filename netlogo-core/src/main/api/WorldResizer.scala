// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core

/** used by Importer for resizing the world during an import.  also used by BehaviorSpace when the
  * experiment varies the world size */

trait WorldResizer {
  def resizeView(): Unit
  def patchSize(patchSize: Double): Unit
  def setDimensions(dim: core.WorldDimensions): Unit
  def setDimensions(dim: core.WorldDimensions, patchSize: Double): Unit
}

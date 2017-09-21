// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.WorldDimensions

object RichWorldDimensions {
  implicit class WorldDimensionsEnriched(w: WorldDimensions) {
    def defaultMinPzcor =
      w match {
        case w3d: WorldDimensions3D => w3d.minPzcor
        case w: WorldDimensions => 0
      }

    def defaultMaxPzcor =
      w match {
        case w3d: WorldDimensions3D => w3d.maxPzcor
        case w: WorldDimensions => 0
      }

    def defaultWrappingInZ =
      w match {
        case w3d: WorldDimensions3D => w3d.wrappingAllowedInZ
        case w: WorldDimensions => false
      }

    def to3D: WorldDimensions3D =
      w match {
        case w3d: WorldDimensions3D => w3d
        case w: WorldDimensions =>
          WorldDimensions3D(w.minPxcor, w.maxPxcor, w.minPycor, w.maxPycor, 0, 0, w.patchSize,
            w.wrappingAllowedInX, w.wrappingAllowedInY, true)
      }

    def to2D: WorldDimensions =
      w.copy()

    def copyPreservingArity(
      minPxcor: Int = w.minPxcor,
      maxPxcor: Int = w.maxPxcor,
      minPycor: Int = w.minPycor,
      maxPycor: Int = w.maxPycor,
      minPzcor: Int = defaultMinPzcor,
      maxPzcor: Int = defaultMaxPzcor,
      patchSize: Double = w.patchSize,
      wrappingAllowedInX: Boolean = w.wrappingAllowedInX,
      wrappingAllowedInY: Boolean = w.wrappingAllowedInY,
      wrappingAllowedInZ: Boolean = defaultWrappingInZ) = {
        w match {
          case w3d: WorldDimensions3D =>
            new WorldDimensions3D(minPxcor, maxPxcor, minPycor, maxPycor, minPzcor, maxPzcor, patchSize, wrappingAllowedInX, wrappingAllowedInY, wrappingAllowedInZ)
          case w: WorldDimensions =>
            w.copy(minPxcor, maxPxcor, minPycor, maxPycor, patchSize, wrappingAllowedInY, wrappingAllowedInY)
        }
    }
  }
}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version

object ChecksumsAndPreviewsSettings {
  val ChecksumsPath = if (Version.is3D) "test/checksums-3d" else "test/checksums"
  val DumpsPath = "test/benchdumps/"
}

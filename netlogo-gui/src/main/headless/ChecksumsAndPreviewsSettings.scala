// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version

object ChecksumsAndPreviewsSettings {
  val ChecksumsPath = if (Version.is3D) "test/exports/threed" else "test/exports/gui"
  val DumpsPath = "test/benchdumps/"
}

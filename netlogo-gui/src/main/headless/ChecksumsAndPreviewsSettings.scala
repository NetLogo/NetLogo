// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version

object ChecksumsAndPreviewsSettings {
  val ChecksumsFilePath = if (Version.is3D) "test/checksums3d.txt" else "test/checksums.txt"
  val DumpsPath = "test/benchdumps/"
}

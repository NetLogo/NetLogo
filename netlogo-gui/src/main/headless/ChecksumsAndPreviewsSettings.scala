// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version

object ChecksumsAndPreviewsSettings {
  val ChecksumsFilePath = if (Version.is3D) "test/checksums3d.txt" else "test/checksums.txt"

  val DumpsPath = "test/benchdumps/"

  val ModelsToSkip = Seq(

      None -> List("HUBNET", "/CURRICULAR MODELS/")

    , Some("it renders slightly differently on Mac vs. Linux") -> List(
      "/CODE EXAMPLES/LINK BREEDS EXAMPLE.NLOGO" // see 407ddcdd49f88395915b1a87c663b13000758d35 in `models` repo
    )

    , Some("it uses the sound extension") -> List(
      "/SAMPLE MODELS/GAMES/FROGGER.NLOGO",
      "/SAMPLE MODELS/ART/SOUND MACHINES.NLOGO",
      "/SAMPLE MODELS/ART/GENJAM - DUPLE.NLOGO",
      "/CODE EXAMPLES/EXTENSIONS EXAMPLES/SOUND/"
    )

    , Some("it uses the vid extension") -> List(
      "/CODE EXAMPLES/EXTENSIONS EXAMPLES/VID/"
    )

  )
}

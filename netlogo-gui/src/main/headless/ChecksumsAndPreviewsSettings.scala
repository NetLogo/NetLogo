// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version

object ChecksumsAndPreviewsSettings {
  val ChecksumsFilePath = if (Version.is3D) "test/checksums3d.txt" else "test/checksums.txt"

  val DumpsPath = "test/benchdumps/"

  val ModelsToSkip = Seq(

      None -> List("/CURRICULAR MODELS/")

    , Some("it uses HubNet") -> List(
      "HUBNET"
    )

    , Some("it renders slightly differently on Mac vs. Linux") -> List(
      "/CODE EXAMPLES/LINK BREEDS EXAMPLE.NLOGO" // see 407ddcdd49f88395915b1a87c663b13000758d35 in `models` repo
    )

    , Some("it uses non-standard setup/go procedures") -> List(
      "/test/benchmarks/ANN Benchmark.nlogo",
      "/Code Examples/3D Shapes Example.nlogo",
      "/Code Examples/Case Conversion Example.nlogo",
      "/Code Examples/Extensions Examples/matrix/Matrix Example.nlogo",
      "/Code Examples/Extensions Examples/profiler/Profiler Example.nlogo",
      "/Code Examples/Extensions Examples/table/Table Example.nlogo",
      "/Code Examples/Extensions Examples/time/Discrete Event Mousetrap.nlogo",
      "/Code Examples/File Input Example.nlogo",
      "/Code Examples/Info Tab Example.nlogo",
      "/Code Examples/Mouse Example.nlogo",
      "/Code Examples/Perspective Demos/Termites (Perspective Demo).nlogo",
      "/Code Examples/User Interaction Example.nlogo",
      "/Sample Models/Mathematics/Probability/ProbLab/Unverified/Equidistant Probability.nlogo"
    )

    , Some("it uses the arduino extension") -> List(
      "/IABM Textbook/chapter 8/Arduino Example.nlogo"
    )

    , Some("it uses the gogo extension") -> List(
      "/Code Examples/Extensions Examples/gogo/"
    )

    , Some("it uses the ls extension") -> List(
      "/Code Examples/Extensions Examples/ls/"
    )

    , Some("it uses finnicky Python packages") -> List(
      "/Code Examples/Extensions Examples/py/Python Flocking Clusters.nlogo"
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

    , Some("it uses the view2.5d extension") -> List(
      "/Code Examples/Extensions Examples/view2.5d/"
    )

  )
}

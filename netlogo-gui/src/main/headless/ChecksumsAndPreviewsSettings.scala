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
      "/CODE EXAMPLES/LINK BREEDS EXAMPLE.NLOGOX" // see 407ddcdd49f88395915b1a87c663b13000758d35 in `models` repo
    )

    , Some("it uses non-standard setup/go procedures") -> List(
      "/test/benchmarks/ANN Benchmark.nlogox",
      "/Code Examples/3D Shapes Example.nlogox",
      "/Code Examples/Case Conversion Example.nlogox",
      "/Code Examples/Extensions Examples/matrix/Matrix Example.nlogox",
      "/Code Examples/Extensions Examples/profiler/Profiler Example.nlogox",
      "/Code Examples/Extensions Examples/table/Table Example.nlogox",
      "/Code Examples/Extensions Examples/time/Discrete Event Mousetrap.nlogox",
      "/Code Examples/File Input Example.nlogox",
      "/Code Examples/Info Tab Example.nlogox",
      "/Code Examples/Mouse Example.nlogox",
      "/Code Examples/Perspective Demos/Termites (Perspective Demo).nlogox",
      "/Code Examples/User Interaction Example.nlogox",
      "/Sample Models/Mathematics/Probability/ProbLab/Unverified/Equidistant Probability.nlogox"
    )

    , Some("it uses the arduino extension") -> List(
      "/IABM Textbook/chapter 8/Arduino Example.nlogox"
    )

    , Some("it uses the gogo extension") -> List(
      "/Code Examples/Extensions Examples/gogo/"
    )

    , Some("it uses the ls extension") -> List(
      "/Code Examples/Extensions Examples/ls/",
      "/Sample Models/Biology/Wolf Sheep Predation - Agent Cognition/Wolf Sheep Predation - Micro-Sims.nlogox",
      "/Sample Models/Biology/Wolf Sheep Predation - Agent Cognition/Wolf Sheep Predation - Micro-Sims Cognitive Model.nlogox"
    )

    , Some("it uses the sound extension") -> List(
      "/CODE EXAMPLES/EXTENSIONS EXAMPLES/SOUND/",
      "/Sample Models/Art/GenJam - Duple.nlogox",
      "/Sample Models/Art/Sound Machines.nlogox",
      "/Sample Models/Games/Frogger.nlogox"
    )

    , Some("it uses the vid extension") -> List(
      "/CODE EXAMPLES/EXTENSIONS EXAMPLES/VID/"
    )

    , Some("it uses the view2.5d extension") -> List(
      "/Code Examples/Extensions Examples/view2.5d/"
    )

    // TODO: find a good solution for this (Isaac B 8/28/25)
    , Some("it has different behavior on GitHub Actions") -> List(
      "/Code Examples/Extensions Examples/sr/Linear Regression Example.nlogox"
    )

  )
}

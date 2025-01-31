// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

object ChecksumsAndPreviewsSettings {
  val ChecksumsFilePath = "test/checksums-headless.txt"

  val DumpsPath = "test/benchdumps-headless/"

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

    , Some("it uses the sound extension") -> List(
      "/SAMPLE MODELS/GAMES/FROGGER.NLOGOX",
      "/SAMPLE MODELS/ART/SOUND MACHINES.NLOGOX",
      "/SAMPLE MODELS/ART/GENJAM - DUPLE.NLOGOX",
      "/CODE EXAMPLES/EXTENSIONS EXAMPLES/SOUND/"
    )

    , Some("it uses behaviorspace-experiment-name") -> List(
      "/SAMPLE MODELS/BIOLOGY/EVOLUTION/ANISOGAMY.NLOGOX"
    )

    , Some("it uses the ls extension, not setup for headless") -> List(
      "/Curricular Models/GenEvo/GenEvo 4 Competition.nlogox",
      "/SAMPLE MODELS/CHEMISTRY & PHYSICS/KICKED ROTATORS.NLOGOX",
      "/CODE EXAMPLES/EXTENSIONS EXAMPLES/LS/",
      "Sample Models/Biology/CRISPR/CRISPR Ecosystem LevelSpace.nlogox"
    )

    , Some("it involves the system dynamics modeler") -> List(
      "/SAMPLE MODELS/SYSTEM DYNAMICS/"
    )

    , Some("it involves user interaction") -> List(
      "/CODE EXAMPLES/USER INTERACTION EXAMPLE.NLOGOX",
      "/CODE EXAMPLES/MOUSE EXAMPLE.NLOGOX"
    )

    , Some("it uses the palette extension") -> List(
      "/CODE EXAMPLES/EXTENSIONS EXAMPLES/PALETTE/"
    )

    , Some("it uses the nw extension") -> List(
      "/CODE EXAMPLES/EXTENSIONS EXAMPLES/NW/"
    )

    , Some("it uses the time extension") -> List(
      "/Code Examples/Extensions Examples/time/",
    )

    , Some("it uses the vid extension") -> List(
      "/CODE EXAMPLES/EXTENSIONS EXAMPLES/VID/"
    )

    , Some("it uses the view2.5d extension") -> List(
      "/CODE EXAMPLES/EXTENSIONS EXAMPLES/VIEW2.5D/"
    )

    , Some("it uses the gogo extension") -> List(
      "/CODE EXAMPLES/EXTENSIONS EXAMPLES/GOGO/"
    )

    , Some("it uses the arduino extension") -> List(
      "/IABM TEXTBOOK/CHAPTER 8/ARDUINO EXAMPLE.NLOGOX"
    )

    , Some("it uses the gis extension") -> List(
      "/CODE EXAMPLES/EXTENSIONS EXAMPLES/GIS/",
      "/IABM TEXTBOOK/CHAPTER 8/TICKET SALES.NLOGOX"
    )

    , Some("it uses the nw extension") -> List(
      "/IABM TEXTBOOK/CHAPTER 8/SIMPLE VIRAL MARKETING.NLOGOX"
    )

    , Some("it uses finnicky Python packages") -> List(
      "/Code Examples/Extensions Examples/py/Python Flocking Clusters.nlogox"
    )

  )
}

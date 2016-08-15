import sbt._

object ModelCrossReference {
  def apply(modelsDir: File): Unit = {
    directoriesToCreate.foreach(modelsDir / _)
    modelDuplications.foreach {
      case (src, filter, dest) =>
        (modelsDir / src * filter).get.foreach { f =>
          FileActions.copyFile(f, modelsDir / dest / f.getName)
        }
    }
    foldersToCopy.foreach {
      case (src, dest) =>
        FileActions.copyDirectory(modelsDir / src, modelsDir / dest)
    }
  }

  val directoriesToCreate = Seq(
    "Curricular Models/EACH/Unverified",
    "Code Examples/Extensions Examples/bitmap",
    "Code Examples/Extensions Examples/arduino")

  val foldersToCopy = Seq(
    "Sample Models/Chemistry & Physics/GasLab"       -> "Curricular Models/GasLab",
    "Sample Models/Chemistry & Physics/MaterialSim"  -> "Curricular Models/MaterialSim",
    "IABM Textbook/chapter 8/arduino-example-sketch" -> "Code Examples/Extensions Examples/arduino/arduino-example-sketch")

  val modelDuplications = Seq[(String, FileFilter, String)](
  // these need to fuzzy-find, since we may have pngs
  // these are particular models
  ( "Sample Models/Biology", "AIDS*",                        "Sample Models/Social Science"),
  ( "Sample Models/Biology", "Simple Birth*",                "Sample Models/Social Science"),
  ( "Sample Models/Networks", "Team Assembly*",              "Sample Models/Social Science"),
  ( "Sample Models/Biology/Evolution", "Altruism*",          "Sample Models/Social Science"),
  ( "Sample Models/Biology/Evolution", "Cooperation*",       "Sample Models/Social Science"),
  ( "Sample Models/Biology/Evolution/Unverified", "Divide*", "Sample Models/Social Science/Unverified"),
  ( "Sample Models/System Dynamics/Unverified", "Tabonuco*", "Sample Models/Biology/Unverified"),

  ( "Sample Models/Mathematics/Probability/ProbLab", new SimpleFileFilter(_.isFile), "Curricular Models/ProbLab"), // copy the files, but not the Unverified folder
  ( "Sample Models/Mathematics/Probability/ProbLab/Unverified", "*",                 "Curricular Models/ProbLab"), // copy all of the files from the Unverified folder

  ( "Sample Models/Biology/Evolution",            "Altruism*",             "Curricular Models/EACH"),
  ( "Sample Models/Biology/Evolution",            "Cooperation*",          "Curricular Models/EACH"),
  ( "Sample Models/Biology/Evolution/Unverified", "Divide*",               "Curricular Models/EACH/Unverified"),
  ( "Code Examples/Extensions Examples/vid",      "Video Camera Example*", "Code Examples/Extensions Examples/bitmap"),

  // BEAGLE curricular models
  ("Sample Models/Biology", "Wolf Sheep Predation*",                        "Curricular Models/BEAGLE Evolution"),
  ("Sample Models/Biology/Evolution/Genetic Drift", "GenDrift T interact*", "Curricular Models/BEAGLE Evolution"),
  ("Sample Models/Biology/Evolution", "Bug Hunt Speeds*",                   "Curricular Models/BEAGLE Evolution"),
  ("Sample Models/Biology/Evolution", "Bug Hunt Camouflage*",               "Curricular Models/BEAGLE Evolution"),
  // needs to fuzzy-find only jpg
  ("Sample Models/Biology/Evolution", "*.jpg",                             "Curricular Models/BEAGLE Evolution"),
  ("Sample Models/Biology/Evolution", "*.jpg",                             "Curricular Models/BEAGLE Evolution/HubNet Activities"),
  ("HubNet Activities/Unverified", "Guppy Spots*",                          "Curricular Models/BEAGLE Evolution"),
  ("HubNet Activities/Unverified", "aquarium.jpg",                         "Curricular Models/BEAGLE Evolution"),
  ("HubNet Activities", "Bug Hunters Camouflage*",                          "Curricular Models/BEAGLE Evolution/HubNet Activities"),
  ("Sample Models/Biology", "Daisyworld*",                                  "Curricular Models/BEAGLE Evolution"),
  ("Sample Models/Biology/Evolution", "Mimicry*",                           "Curricular Models/BEAGLE Evolution"),
  ("Sample Models/Biology/Evolution", "Altruism*",                          "Curricular Models/BEAGLE Evolution"),
  ("Sample Models/Biology/Evolution", "Cooperation*",                       "Curricular Models/BEAGLE Evolution"),

  // BEAGLE HubNet models
  ("Curricular Models/BEAGLE Evolution/HubNet Activities", "Bird Breeders HubNet*",           "HubNet Activities"),
  ("Curricular Models/BEAGLE Evolution/HubNet Activities", "Bug Hunters Competition HubNet*", "HubNet Activities"),
  ("Curricular Models/BEAGLE Evolution/HubNet Activities", "Critter Designers HubNet*",       "HubNet Activities"),
  ("Curricular Models/BEAGLE Evolution/HubNet Activities", "Fish Spotters HubNet*",           "HubNet Activities"),

  // Copy Oil Cartel HubNet to HubNet Activities
  ("Sample Models/Social Science",                         "Oil Cartel HubNet*",              "HubNet Activities"),

  // IABM Textbook models duplicated in Sample Models and Code Examples
  ("IABM Textbook/chapter 3/El Farol Extensions", "El Farol.nlogo",   "Sample Models/Social Science"),
  ("IABM Textbook/chapter 3/DLA extensions",      "DLA Simple.nlogo", "Sample Models/Chemistry & Physics/Diffusion Limited Aggregation"),
  ("IABM Textbook/chapter 8",                     "Arduino*",       , "Code Examples/Extensions Examples/arduino"),
)

}

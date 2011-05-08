import sbt._

trait BehaviorSpaceSources extends DefaultProject {

  private val labSrcs =
    ("src" / "main" / "org" / "nlogo" / "lab" ** "*.scala") +++
    ("src" / "test" / "org" / "nlogo" / "lab" ** "*.scala")
  private val labMiscFiles =
    (path("dist") * "lab*") +++
    ("docs" / "LGPL-3.txt")
  val behaviorspaceSources = task {
    FileUtilities.clean("tmp" / "BehaviorSpace", log)
    FileUtilities.createDirectory("tmp" / "BehaviorSpace", log)
    FileUtilities.copy(labSrcs.get, "tmp" / "BehaviorSpace", true, true, log)
    FileUtilities.copyDirectory("test" / "lab", "tmp" / "BehaviorSpace" / "test" / "lab", log)
    FileUtilities.clean(("tmp" / "BehaviorSpace" ** ".svn").get, log)
    FileUtilities.copyFile("dist" / "lab-readme.txt", "tmp" / "BehaviorSpace" / "README.txt", log)
    FileUtilities.copyFile("dist" / "lab-build.sh", "tmp" / "BehaviorSpace" / "build.sh", log)
    FileUtilities.copyFile("dist" / "lab-test.sh", "tmp" / "BehaviorSpace" / "test.sh", log)
    FileUtilities.copyFile("docs" / "LGPL-3.txt", "tmp" / "BehaviorSpace" / "LGPL-3.txt", log)
    FileUtilities.zip(List(("tmp" ##) / "BehaviorSpace"), path("BehaviorSpace-src.zip"), true, log)
  }

}


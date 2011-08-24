// original by Jason Zaugg. swiped from the scalaz project.

import sbt._
import xsbt.FileUtilities._

trait ScalaVersionBumper extends Project {

  lazy val bumpScalaVersion: MethodTask =
  task {args: Seq[String] =>
    if (args.length == 1)
      bumpScalaVersion(args(0))
    else
      task {Some("Usage: bump-scala-version <string>")}
  } describedAs("Update build.properties and other files with a new Scala version.")

  def bumpScalaVersion(newVersion: String) = task {
    val files = path(".") ** ("*.iml" | "Makefile" | "*.scala") +++
                path(".") * "build.sbt" +++
                path("project") / "build" / "proguard" * ("*.txt") +++
                path("bin") * ("scala*") +++
                path(".idea") / "libraries" * ("*.xml") +++
                path("bin") * ("release.sh")
    for (f <- files.getFiles) {
      val oldVersion = buildScalaVersion
      val oldText = read(f)
      val newText = oldText.replaceAll("\\Q" + oldVersion, newVersion)
      if(newText != oldText) {
        log.info("Updating: " + f.getAbsolutePath)
        write(f, newText)
      }
    }
    buildScalaVersions() = newVersion
    None
  }
}

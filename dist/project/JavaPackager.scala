import sbt.Package

import java.io.File
import java.util.jar.Attributes.Name._

object JavaPackager {
  def jarAttributes(jarDeps: Seq[File]): Package.ManifestAttributes = {
    val distClassPath = jarDeps.map(_.getName).mkString(" ")
    import java.util.jar.Attributes.Name._
    Package.ManifestAttributes(
      "Permissions"                   -> "sandbox",
      "JavaFX-Version"                -> "8.0", // this is required for javapackager to determine the main jar
      "Created-By"                    -> "JavaFX Packager",
      CLASS_PATH.toString             -> distClassPath,
      IMPLEMENTATION_VENDOR.toString  -> "netlogo",
      IMPLEMENTATION_TITLE.toString   -> "NetLogo",
      IMPLEMENTATION_VERSION.toString -> "5.2.2-SNAPSHOT",
      SPECIFICATION_VENDOR.toString   -> "netlogo",
      SPECIFICATION_TITLE.toString    -> "NetLogo",
      SPECIFICATION_VERSION.toString  -> "5.2.2-SNAPSHOT"
    )
  }
}

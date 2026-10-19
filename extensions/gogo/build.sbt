import java.io.InputStream
import java.net.URI
import java.nio.file.{ Files, StandardCopyOption }

import org.nlogo.build.{ NetLogoExtension, ExtensionDocumentationPlugin }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name := "gogo"
version := "2.2.1"
isSnapshot := true

scalaVersion := "3.7.0"

Compile / resourceDirectory := { baseDirectory.value / "resources" }

Compile / javaSource := baseDirectory.value / "src"
javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path", "-encoding", "us-ascii", "--release", "11")

netLogoVersion      := "7.1.0-internal1-0194cd8"
netLogoClassManager := "gogohid.extension.HIDGogoExtension"

netLogoPackageExtras ++= Seq(
  (baseDirectory.value / "lib" / "hid4java-develop-SNAPSHOT.jar", None),
  (baseDirectory.value / "lib" / "jna.jar", None)
)

// Note that we don't care about it being a dependency.  The JVM that loads this code doesn't even want to load this
// particular version of JNA.  It's only the GoGo daemon that should be loading this version of JNA.
// --Jason B. (10/28/25)
lazy val downloadJNA = taskKey[Unit]("Obtain the JNA '.jar' file")

downloadJNA := {
  val target = (baseDirectory.value / "lib" / "jna.jar").toPath

  if (!Files.exists(target)) {
    val url    = new URI("https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.8.0/jna-5.8.0.jar").toURL
    val urlIS  = url.openStream()
    try {
      Files.copy(urlIS, target, StandardCopyOption.REPLACE_EXISTING)
    } finally {
      urlIS.close()
    }
  }
}

Compile / compile := ((Compile / compile).dependsOn(downloadJNA)).value

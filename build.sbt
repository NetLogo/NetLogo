import org.nlogo.build.{ NetLogoExtension, ExtensionDocumentationPlugin}

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name       := "vid"
version    := "1.3.3"
isSnapshot := true

organization := "org.nlogo"
scalaVersion := "3.7.0"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-encoding", "us-ascii",
  "-Xfatal-warnings",
  "-release", "11"
)

netLogoClassManager := "org.nlogo.extensions.vid.VidExtension"
netLogoVersion      := "7.1.0-internal1-0194cd8"
netLogoZipExtras   ++= Seq(baseDirectory.value / "README.md")

// settings for the `sbt-javacpp` sbt plugin
javaCppVersion    :=  "1.5.7"
// opencv depends on openblas so get those platform-specific binaries, too
javaCppPresetLibs ++= Seq("opencv" -> "4.5.5", "openblas" -> "0.3.19")
// only include the supported NetLogo platforms
javaCppPlatform := {
  (System.getProperty("os.name"), System.getProperty("os.arch")) match {
    case (name, arch) if name.startsWith("Linux") =>
      if (arch.contains("x86")) {
        Seq("linux-x86")
      } else {
        Seq("linux-x86_64")
      }

    case (name, arch) if name.startsWith("Mac") =>
      if (arch.contains("aarch64")) {
        Seq("macosx-arm64")
      } else {
        Seq("macosx-x86_64")
      }

    case (name, arch) if name.startsWith("Windows") =>
      if (arch.contains("x86")) {
        Seq("windows-x86")
      } else {
        Seq("windows-x86_64")
      }

    case _ => throw new Exception("Unknown platform!")
  }
}

libraryDependencies ++= Seq(
// only include `javacv` and not `javacv-platform` as we manually specify the native libraries
// throught the `sbt-javacpp` sbt plugin
  "org.bytedeco" % "javacv" % "1.5.7"
, "org.jcodec" % "jcodec" % "0.1.9"
, "org.jcodec" % "jcodec-javase" % "0.1.9"
)

// necessary for testing camera functionality.
// See https://groups.google.com/forum/#!topic/nativelibs4java/WNmOZPknRiU
Test / fork := true

// Add JavaFX dependencies
val javaFXVersion = "21"
libraryDependencies ++= {
  // Determine OS version of JavaFX binaries
  lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux") => "linux"
    case n if n.startsWith("Mac") => "mac"
    case n if n.startsWith("Windows") => "win"
    case _ => throw new Exception("Unknown platform!")
  }
  Seq("base", "controls", "media", "swing")
    .map(m => "org.openjfx" % s"javafx-$m" % javaFXVersion classifier osName)
}

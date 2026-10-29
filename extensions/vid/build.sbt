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

// only include the supported NetLogo platforms
val javaCppPlatform = {
  (System.getProperty("os.name"), System.getProperty("os.arch")) match {
    case (name, arch) if name.startsWith("Linux") =>
      if (arch.contains("x86")) {
        "linux-x86"
      } else {
        "linux-x86_64"
      }

    case (name, arch) if name.startsWith("Mac") =>
      if (arch.contains("aarch64")) {
        "macosx-arm64"
      } else {
        "macosx-x86_64"
      }

    case (name, arch) if name.startsWith("Windows") =>
      if (arch.contains("x86")) {
        "windows-x86"
      } else {
        "windows-x86_64"
      }

    case _ => throw new Exception("Unknown platform!")
  }
}

libraryDependencies ++= Seq(
  "org.bytedeco" % "javacv" % "1.5.7",
  "org.jcodec" % "jcodec" % "0.1.9",
  "org.jcodec" % "jcodec-javase" % "0.1.9",
  "org.bytedeco" % "javacv-platform" % "1.5.7"
    exclude("org.bytedeco", "openblas-platform")
    exclude("org.bytedeco", "opencv-platform")
    exclude("org.bytedeco", "ffmpeg-platform")
    exclude("org.bytedeco", "flycapture-platform")
    exclude("org.bytedeco", "libdc1394-platform")
    exclude("org.bytedeco", "libfreenect-platform")
    exclude("org.bytedeco", "libfreenect2-platform")
    exclude("org.bytedeco", "librealsense-platform")
    exclude("org.bytedeco", "librealsense2-platform")
    exclude("org.bytedeco", "videoinput-platform")
    exclude("org.bytedeco", "artoolkitplus-platform")
    exclude("org.bytedeco", "flandmark-platform")
    exclude("org.bytedeco", "leptonica-platform")
    exclude("org.bytedeco", "tesseract-platform"),
  // now include the specific packages we need for each platform
  "org.bytedeco" % "javacpp" % "1.5.7" classifier javaCppPlatform,
  "org.bytedeco" % "opencv" % "4.5.5-1.5.7" classifier javaCppPlatform,
  "org.bytedeco" % "openblas" % "0.3.19-1.5.7" classifier javaCppPlatform
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

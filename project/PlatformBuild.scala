import sbt._
import Keys.target
import java.io.File
import Def.Initialize
import NetLogoPackaging.netLogoRoot

trait PlatformBuild {
  def shortName: String

  def jvmOptions: Seq[String] = "-Xmx1024m -Dfile.encoding=UTF-8".split(" ")

  def nativeFormat: String
}

object WindowsPlatform extends PlatformBuild {
  override def shortName = "windows"

  override def nativeFormat: String = "image"
}

object LinuxPlatform extends PlatformBuild {
  override def shortName: String = "linux"

  override def nativeFormat: String = "image"
}

class MacPlatform(macApp: Project) extends PlatformBuild {
  import Keys._

  override def shortName: String = "macosx"

  override def jvmOptions =
    super.jvmOptions ++ Seq(
      "-Dapple.awt.graphics.UseQuartz=true",
      "-Dnetlogo.quaqua.laf=ch.randelshofer.quaqua.snowleopard.Quaqua16SnowLeopardLookAndFeel")

  override def nativeFormat = "dmg"
}

// used in the mac aggregate build
class MacImagePlatform(macApp: Project) extends MacPlatform(macApp) {
  override def nativeFormat = "image"
}

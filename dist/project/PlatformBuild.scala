import sbt._
import Keys.target
import java.io.File
import Def.Initialize

import DistSettings.netLogoRoot

trait PlatformBuild {
  def shortName: String

  def bundledDirs: Seq[BundledDirectory] = Seq(
    new ExtensionDir(),
    new ModelsDir(),
    new DocsDir()
  )

  def scalaJar: File =
    file(System.getProperty("user.home") + "/.sbt/boot/scala-2.9.2/lib/scala-library.jar")

  def mainJarAndDependencies(app: SubApplication): Initialize[Task[(File, Seq[File])]] = {
    Def.bind(repackageJar(app)) { repackagedJar =>
      Def.task { (repackagedJar.value, standardJars(app, netLogoRoot.value)) }
    }
  }

  protected def jarExcluded(f: File): Boolean = (
    f.getName.contains("scalatest") ||
      f.getName.contains("scalacheck") ||
      f.getName.contains("jmock") ||
      f.getName.contains("junit") ||
      f.getName.contains("hamcrest"))

  protected def standardJars(app: SubApplication, netLogoDir: File): Seq[File] =
    (netLogoDir / "lib_managed" ** "*.jar").get
      .filter(_.isFile)
      .filterNot(jarExcluded) :+ scalaJar

  protected def repackageJar(app: SubApplication): Initialize[Task[File]] =
    Def.task {
      val netLogoJar = netLogoRoot.value / s"${app.jarName}.jar"
      val platformBuildDir = target.value / s"$shortName-build"
      IO.createDirectory(platformBuildDir)
      val newJarLocation = platformBuildDir / s"${app.jarName}.jar"
      if (app.name.contains("HubNet"))
        JavaPackager.packageJar(netLogoJar, newJarLocation,
          Some("org.nlogo.hubnet.client.App"))
      else
        JavaPackager.packageJar(netLogoJar, newJarLocation, None)
      newJarLocation
    }

  def jvmOptions: Seq[String] = "-Xmx1024m -Dfile.encoding=UTF-8".split(" ")

  def nativeFormat: String
}

object WindowsPlatform extends PlatformBuild {
  override def shortName = "windows"

  override def bundledDirs =
    super.bundledDirs ++ Seq(new NativesDir("windows-amd64", "windows-i586"))

  override def nativeFormat: String = "image"
}

object LinuxPlatform extends PlatformBuild {
  override def shortName: String = "linux"

  override def bundledDirs =
    super.bundledDirs ++ Seq(new NativesDir("linux-amd64", "linux-i586"))

  override def nativeFormat: String = "image"
}

class MacPlatform(macApp: Project) extends PlatformBuild {
  import Keys._

  override def shortName: String = "macosx"

  override def bundledDirs =
    super.bundledDirs ++ Seq(new LibDir(), new NativesDir("macosx-universal"))

  override def mainJarAndDependencies(app: SubApplication): Def.Initialize[Task[(File, Seq[File])]] =
    if (! app.name.contains("HubNet"))
      Def.task {
        ((packageBin in Compile in macApp).value,
          (dependencyClasspath in macApp in Runtime).value.files
            .filterNot(jarExcluded)
            .filterNot(_.isDirectory))
      }
    else super.mainJarAndDependencies(app)

  override def jvmOptions =
    Seq("-Dapple.awt.graphics.UseQuartz=true",
      "-Dnetlogo.quaqua.laf=ch.randelshofer.quaqua.snowleopard.Quaqua16SnowLeopardLookAndFeel"
      ) ++ super.jvmOptions

  override def nativeFormat =
    "dmg"
}

// used in the mac aggregate build
class MacImagePlatform(macApp: Project) extends MacPlatform(macApp) {
  override def nativeFormat = "image"
}

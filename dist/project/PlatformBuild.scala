import sbt._
import Keys.target
import java.io.File
import Def.Initialize

import DistSettings.netLogoRoot

trait PlatformBuild {
  def productName: String

  def shortName: String

  def mainJarName: String = "NetLogo.jar"

  def bundledDirs: Seq[BundledDirectory] = Seq(
    new ExtensionDir(),
    new ModelsDir()
  )

  def scalaJar: File =
    file(System.getProperty("user.home") + "/.sbt/boot/scala-2.9.2/lib/scala-library.jar")

  def mainJarAndDependencies(app: SubApplication): Initialize[Task[(File, Seq[File])]] = {
    Def.bind(repackageJar(app)) { repackagedJar =>
      Def.task { (repackagedJar.value, standardJars(app, netLogoRoot.value)) }
    }
  }

  protected def standardJars(app: SubApplication, netLogoDir: File): Seq[File] = {
    val standardDeps =
      (netLogoDir / "lib_managed" ** "*.jar").get
        .filter(_.isFile)
        .filterNot(f => f.getName.contains("scalatest") || f.getName.contains("scalacheck")) :+ scalaJar
    if (app.jarName.contains("HubNet"))
      standardDeps :+ netLogoDir / "NetLogoLite.jar"
    else
      standardDeps
  }

  protected def repackageJar(app: SubApplication): Initialize[Task[File]] =
    Def.task {
      val netLogoJar = netLogoRoot.value / s"${app.jarName}.jar"
      val platformBuildDir = target.value / s"$shortName-build"
      IO.createDirectory(platformBuildDir)
      val newJarLocation = platformBuildDir / s"${app.jarName}.jar"
      JavaPackager.packageJar(netLogoJar, newJarLocation)
      newJarLocation
    }

  def jvmOptions: Seq[String] = "-Djava.ext.dirs= -Xmx1024m -Dfile.encoding=UTF-8".split(" ")

  def nativeFormat: String
}

object WindowsPlatform extends PlatformBuild {
  override def shortName = "windows"

  override def bundledDirs =
    super.bundledDirs ++ Seq(new NativesDir("windows-amd64", "windows-i586"))

  override def nativeFormat: String = "image"

  override def productName = "NetLogo-win"
}

object LinuxPlatform extends PlatformBuild {
  override def shortName: String = "linux"

  override def productName: String = "NetLogo-linux"

  override def bundledDirs =
    super.bundledDirs ++ Seq(new NativesDir("linux-amd64", "linux-i586"))

  override def nativeFormat: String = "image"
}

class MacPlatform(macApp: Project) extends PlatformBuild {
  import Keys._

  override def shortName: String = "macosx"

  override def mainJarName: String = "netlogo-mac-app.jar"

  override def bundledDirs =
    super.bundledDirs ++ Seq(new LibDir(), new NativesDir("macosx-universal"))

  override def mainJarAndDependencies(app: SubApplication): Def.Initialize[Task[(File, Seq[File])]] =
    if (app.jarName == "NetLogo")
      Def.task {
        ((packageBin in Compile in macApp).value,
          (dependencyClasspath in macApp in Runtime).value.files
            .filterNot(f => f.getName.contains("scalatest") || f.getName.contains("scalacheck") || f.getName.contains("jmock"))
            .filterNot(_.isDirectory))
      }
    else super.mainJarAndDependencies(app)

  override def jvmOptions =
    "-Dapple.awt.graphics.UseQuartz=true" +: super.jvmOptions

  override def nativeFormat =
    "dmg"

  override def productName: String = "NetLogo-1.0.dmg"
}

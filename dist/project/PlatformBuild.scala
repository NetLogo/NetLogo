import sbt._
import java.io.File

trait PlatformBuild {
  def productName: String

  def shortName: String

  def mainJarName: String = "NetLogo.jar"

  def bundledDirs: Seq[BundledDirectory] = Seq(
    new ExtensionDir(),
    new ModelsDir()
  )

  def additionalResources(distDir: File): Seq[File]

  def scalaJar: File = {
    file(System.getProperty("user.home") + "/.sbt/boot/scala-2.9.2/lib/scala-library.jar")
  }

  def dependencyJars(netLogoDir: File): Def.Initialize[Task[Seq[File]]] =
    Def.task { standardJars(netLogoDir) }

  protected def standardJars(netLogoDir: File): Seq[File] =
    (netLogoDir / "lib_managed" ** "*.jar").get
      .filter(_.isFile)
      .filterNot(f => f.getName.contains("scalatest") || f.getName.contains("scalacheck")) :+ scalaJar

  def jvmOptions: Seq[String] = "-Djava.ext.dirs= -Xmx1024m -Dfile.encoding=UTF-8".split(" ")

  def nativeFormat: String
}

object WindowsPlatform extends PlatformBuild {
  override def shortName = "windows"

  override def bundledDirs =
    super.bundledDirs ++ Seq(new NativesDir("windows-amd64", "windows-i586"))

  override def nativeFormat: String = "image"

  override def dependencyJars(netLogoDir: File): Def.Initialize[Task[Seq[File]]] = {
    import Keys.target
    import java.util.jar.Manifest
    import java.util.jar.Attributes.Name._

    Def.task {
      val jars = standardJars(netLogoDir)
      val netLogoJar = netLogoDir / "NetLogo.jar"
      val tmpDir = IO.createTemporaryDirectory
      IO.createDirectory(target.value / "win-build")
      IO.unzip(netLogoJar, tmpDir)
      IO.delete(tmpDir / "META-INF")
      val attributes = JavaPackager.jarAttributes(jars)
      val manifest = new Manifest()
      attributes.attributes.foreach {
        case (k, v) => manifest.getMainAttributes.put(k, v)
      }
      manifest.getMainAttributes.put(MAIN_CLASS, "org.nlogo.app.App")
      val newJarLocation = target.value / "win-build" / "NetLogo.jar"
      IO.jar(Path.allSubpaths(tmpDir), newJarLocation, manifest)
      jars :+ newJarLocation
    }
  }

  override def additionalResources(distDir: File): Seq[File] = Seq() // not sure what windows resources are needed...

  override def productName = "NetLogo-win"
}

object LinuxPlatform extends PlatformBuild {
  override def shortName: String = "linux"

  override def productName: String = "NetLogo-linux"

  override def bundledDirs =
    super.bundledDirs ++ Seq(new NativesDir("linux-amd64", "linux-i586"))

  override def nativeFormat: String = "image"

  override def dependencyJars(netLogoDir: File): Def.Initialize[Task[Seq[File]]] = {
    import Keys.target
    import java.util.jar.Manifest
    import java.util.jar.Attributes.Name._

    Def.task {
      val jars = standardJars(netLogoDir)
      val netLogoJar = netLogoDir / "NetLogo.jar"
      val tmpDir = IO.createTemporaryDirectory
      IO.createDirectory(target.value / "linux-build")
      IO.unzip(netLogoJar, tmpDir)
      IO.delete(tmpDir / "META-INF")
      val attributes = JavaPackager.jarAttributes(jars)
      val manifest = new Manifest()
      attributes.attributes.foreach {
        case (k, v) => manifest.getMainAttributes.put(k, v)
      }
      manifest.getMainAttributes.put(MAIN_CLASS, "org.nlogo.app.App")
      val newJarLocation = target.value / "linux-build" / "NetLogo.jar"
      IO.jar(Path.allSubpaths(tmpDir), newJarLocation, manifest)
      jars :+ newJarLocation
    }
  }

  override def additionalResources(distDir: File): Seq[File] = Seq() // not sure what linux resources are needed...

}

class MacPlatform(macApp: Project) extends PlatformBuild {
  import Keys._

  override def shortName: String = "macosx"

  override def mainJarName: String = "netlogo-mac-app.jar"

  override def bundledDirs =
    super.bundledDirs ++ Seq(new LibDir(), new NativesDir("macosx-universal"))

  override def additionalResources(distDir: File) = Seq(
    distDir / "NetLogo.app" / "Contents" / "Resources",
    distDir / "NetLogo.app" / "Contents" / "PkgInfo")

  override def dependencyJars(netLogoDir: File): Def.Initialize[Task[Seq[File]]] =
    Def.task {
      (packageBin in Compile in macApp).value +:
      (dependencyClasspath in macApp in Runtime).value.files
        .filterNot(f => f.getName.contains("scalatest") || f.getName.contains("scalacheck") || f.getName.contains("jmock"))
    }

  override def jvmOptions =
    "-Dapple.awt.graphics.UseQuartz=true" +: super.jvmOptions

  override def nativeFormat =
    "dmg"

  override def productName: String = "NetLogo-1.0.dmg"
}

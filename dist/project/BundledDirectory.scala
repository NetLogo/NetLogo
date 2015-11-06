import sbt._
import java.io.File

trait BundledDirectory {
  def directoryName: String
  def files(sourceDir: File): Seq[File]
}

class ExtensionDir extends BundledDirectory {
  val directoryName = "extensions"

  def files(sourceDir: File): Seq[File] = {
    sourceDir.listFiles.flatMap(_.listFiles)
      .filter(_.getName.endsWith(".jar"))
      .filterNot(f => f.getName.contains("NetLogo") || f.getName.contains("scala-library") || f.getName.contains("QTJava"))
  }
}

class ModelsDir extends BundledDirectory {
  val directoryName = "models"

  def files(sourceDir: File): Seq[File] =
    (sourceDir ** ("index.txt" || "*.nlogo" || "*.nls" || "*.png" || "*.nlogo3d")).get
}

class LibDir extends BundledDirectory {
  val directoryName = "lib"
  def files(sourceDir: File): Seq[File] =
    (sourceDir / "Mac OS X").listFiles
}

class NativesDir(platforms: String*) extends BundledDirectory {
  val directoryName = "natives"
  def files(sourceDir: File): Seq[File] =
    platforms.flatMap(platform => (sourceDir / platform).listFiles)
}

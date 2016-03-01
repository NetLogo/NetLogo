import sbt._
import java.io.File

abstract class BundledDirectory(val sourceDir: File) {
  def directoryName: String
  def files:         Seq[File]
  def fileMappings:  Seq[(File, String)] = {
    files zip files.map { f =>
      val Some(relativePath) = Path.relativeTo(sourceDir)(f)
      directoryName + java.io.File.separator + relativePath
    }
  }
}

class ExtensionDir(sourceDir: File) extends BundledDirectory(sourceDir) {
  val directoryName = "extensions"

  def files: Seq[File] = {
    sourceDir.listFiles.flatMap(_.listFiles)
      .filter(_.getName.endsWith(".jar"))
      .filterNot(f => f.getName.contains("NetLogo") || f.getName.contains("scala-library") || f.getName.contains("QTJava"))
  }
}

class ModelsDir(sourceDir: File) extends BundledDirectory(sourceDir) {
  val directoryName = "models"

  def files: Seq[File] =
    Seq("3D",
      "Alternative Visualizations",
      "Code Examples",
      "Curricular Models",
      "HubNet Activities",
      "IABM Textbook",
      "Sample Models")
        .flatMap(subDir => Path.allSubpaths(sourceDir / subDir))
        .map(_._1)
        .filterNot(_.isHidden) :+ (sourceDir / "index.txt")
}

class LibDir(sourceDir: File) extends BundledDirectory(sourceDir) {
  val directoryName = "lib"
  def files: Seq[File] =
    (sourceDir / "Mac OS X").listFiles
}

class NativesDir(sourceDir: File, platforms: String*) extends BundledDirectory(sourceDir) {
  val directoryName = "natives"
  def files: Seq[File] =
    platforms.flatMap(platform => (sourceDir / platform).listFiles)
}

class DocsDir(sourceDir: File) extends BundledDirectory(sourceDir) {
  val directoryName = "docs"
  def files: Seq[File] =
    Path.allSubpaths(sourceDir).map(_._1).filterNot(_.isHidden).toSeq
}

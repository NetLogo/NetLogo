import sbt._
import java.io.File

trait BundledDirectory {
  def directoryName: String

  def files(sourceDir: File): Seq[File]

  def bundleFiles(sourceDir: File, targetDir: File): Seq[(File, File)] = {
    val fs = files(sourceDir)
    fs zip fs.map(repathFile(sourceDir.getParentFile, targetDir))
  }

  def repathFile(originalBase: File, targetDir: File)(f: File): File = {
    val Some(relativeFile) = f relativeTo originalBase
    new java.io.File(targetDir, relativeFile.getPath)
  }
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

class LibDir extends BundledDirectory {
  val directoryName = "lib"

  def files(sourceDir: File): Seq[File] =
    Option((sourceDir / "Mac OS X").listFiles)
      .getOrElse(throw new Exception("Mac lib directory not present, run buildNetLogo first"))
}

class NativesDir(platforms: String*) extends BundledDirectory {
  val directoryName = "natives"
  def files(sourceDir: File): Seq[File] =
    platforms.flatMap(platform => (sourceDir / platform).listFiles)
}

class DocsDir extends BundledDirectory {
  val directoryName = "docs"
  def files(sourceDir: File): Seq[File] =
    Path.allSubpaths(sourceDir).map(_._1).filterNot(_.isHidden).toSeq
}

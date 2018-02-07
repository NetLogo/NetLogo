import sbt._
import java.io.File
import scala.language.postfixOps

abstract class BundledDirectory(val sourceDir: File) {
  def directoryName: String
  def files:         Seq[File]
  def fileMappings:  Seq[(File, String)] = {
    files zip files.map { f =>
      val Some(relativePath) = Path.relativeTo(sourceDir)(f)
      directoryName + File.separator + relativePath
    }
  }

  def fileToPathTuple(f: File): (File, String) = {
    val Some(relativePath) = Path.relativeTo(sourceDir)(f)
    f -> (directoryName + File.separator + relativePath)
  }
}

class ExtensionDir(sourceDir: File) extends BundledDirectory(sourceDir) {
  val directoryName = "extensions"

  override def fileMappings: Seq[(File, String)] = {
    sourceDir.listFiles.filter(_.isDirectory)
      .flatMap { anExtensionDir =>
        if ((anExtensionDir / ".bundledFiles").exists) {
          IO.readLines(anExtensionDir / ".bundledFiles")
            .map { line =>
              val sections = line.split("->")
              anExtensionDir / sections.last -> (directoryName + File.separator + anExtensionDir.getName + File.separator + sections.last)
            }
          } else {
            anExtensionDir.listFiles
              .filter(_.getName.endsWith(".jar"))
              .filterNot(f => f.getName.contains("netlogo") || f.getName.contains("scala-library"))
              .map(fileToPathTuple)
          }
      }
  }

  override def files: Seq[File] = Seq()
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
        .filterNot(_.isHidden)
        .filterNot(_.toString.endsWith(".nlogo"))
        .filterNot(_.toString.endsWith(".nlogo3d")) ++
        Seq(sourceDir / "index.conf", sourceDir / "crossReference.conf")
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

class BehaviorsearchDir(baseDirectory: File, platformShortName: String) extends BundledDirectory(baseDirectory) {
  val directoryName = "behaviorsearch"
  private def headlessScriptMapping: (File, String) = {
    val distInclude = baseDirectory / "dist" / "dist_include"
    platformShortName match {
      case "windows" =>
        distInclude / "behaviorsearch_headless.bat" -> "behaviorsearch/behaviorsearch_headless.bat"
      case "linux"   =>
        distInclude / "behaviorsearch_headless.sh"  -> "behaviorsearch/behaviorsearch_headless.sh"
      case "macosx"  =>
        distInclude / "behaviorsearch_headless.command"  -> "behaviorsearch/behaviorsearch_headless.command"
    }
  }

  override def fileMappings = super.fileMappings :+ headlessScriptMapping
  def files: Seq[File] =
    ((baseDirectory * "*.TXT") +++
      (baseDirectory / "documentation" ***) +++
      (baseDirectory / "resources" ***) +++
      (baseDirectory / "examples" ***)).get
}

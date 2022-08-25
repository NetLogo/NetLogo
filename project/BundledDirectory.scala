import sbt.io.{ FileFilter, IO, Path }
import sbt.io.syntax._

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

object ExtensionDir {
  def createIsUnneededCheck(platform: String, arch: String) = {
    val allPlatforms = Set("linux-x86", "linux-x86_64", "macosx-arm64", "macosx-x86_64", "windows-x86", "windows-x86_64")
    val invalidPlatforms = if ("macosx".equals(platform)) {
      allPlatforms -- Set("macosx-arm64", "macosx-x86_64")
    } else {
      if ("32".equals(arch)) {
        allPlatforms - s"$platform-x86"
      } else {
        allPlatforms - s"$platform-x86_64"
      }
    }
    def isInvalidForPlatform(fileP: String): Boolean = {
      invalidPlatforms.exists( (p) => fileP.endsWith(p) )
    }
    def isJavaFX(starter: String): Boolean = {
      starter.startsWith("javafx-")
    }

    val isUnneededCheck = (f: File) => {
      val fName = f.getName
      val splits = fName.split("""\.""")
      val isUnneeded = (
        f.isFile &&
        (splits.length > 1) &&
        "jar".equals(splits(splits.length - 1)) &&
        (isInvalidForPlatform(splits(splits.length - 2)) || isJavaFX(splits(0)) )
      )
      isUnneeded
    }

    isUnneededCheck
  }
}

class ExtensionDir(sourceDir: File, platform: String, arch: String) extends BundledDirectory(sourceDir) {
  val directoryName = s"extensions${File.separator}.bundled"

  override def fileMappings: Seq[(File, String)] = {
    val isUnneeded = ExtensionDir.createIsUnneededCheck(platform, arch)

    sourceDir.listFiles.filter(_.isDirectory)
      .flatMap { anExtensionDir =>
        if ((anExtensionDir / ".bundledFiles").exists) {
          IO.readLines(anExtensionDir / ".bundledFiles")
            .map { line =>
              val sections   = line.split("->")
              val fileName   = sections.last
              val sourcePath = anExtensionDir / fileName
              val targetPath = directoryName + File.separator + anExtensionDir.getName + File.separator + fileName
              sourcePath -> targetPath
            }
            .filter { case (sourcePath, _) => !isUnneeded(sourcePath) }
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
        .filterNot(_.isHidden) ++
        Seq(sourceDir / "index.conf", sourceDir / "crossReference.conf")
}

class NativesDir(sourceDir: File, platforms: String*) extends BundledDirectory(sourceDir) {
  val directoryName = "natives"
  def files: Seq[File] =
    platforms.flatMap(platform => (sourceDir / platform).listFiles)
}

class DocsDir(sourceDir: File) extends BundledDirectory(sourceDir) {
  val directoryName = "docs"
  def files: Seq[File] = {
    val children = sourceDir.listFiles
    val validChildren = children.filter( (f) => !"scaladoc".equals(f.getName) )
    validChildren.flatMap( (f) =>
      if (!f.isDirectory) {
        Seq(f)
      } else {
        Path.allSubpaths(f).map(_._1).filterNot(_.isHidden).toSeq
      }
    )
  }
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
    ((baseDirectory * FileFilter.globFilter("*.TXT")) +++
      ((baseDirectory / "documentation").allPaths) +++
      ((baseDirectory / "resources").allPaths) +++
      ((baseDirectory / "examples").allPaths)).get
}

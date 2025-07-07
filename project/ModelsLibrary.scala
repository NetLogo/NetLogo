import java.io.File
import java.nio.file.{ Files, Path, Paths }
import java.util.regex.Pattern
import sbt._
import Keys._
import Def.Initialize
import sbt.complete.{ Parser, DefaultParsers }, DefaultParsers.{ EOF, Space }
import Running.makeMainTask

object ModelsLibrary {

  val modelsDirectory = settingKey[File]("models directory")

  val resaveModel = InputKey[Unit]("resaveModel", "resave a single model")

  val resaveModels = TaskKey[Unit]("resaveModels", "resave all library models")

  val modelIndex = TaskKey[Seq[File]](
    "modelIndex", "builds models/index.txt for use in Models Library dialog")

  val exportResizedModels = TaskKey[Unit](
    "exportResizedModels", "resize all models to the new widget sizes and export the interface")

  val convertWidgetSizes = TaskKey[Unit]("convertWidgetSizes", "run widget resize tool on all models in the library")

  val benchmark = TaskKey[Unit]("benchmark", "Run all benchmark models and record runtime statistics")
  val benchmarkGUI = TaskKey[Unit]("benchmarkGUI", "Run all benchmark models in the GUI and record runtime statistics")

  val modelParser: Initialize[Parser[Option[Path]]] = {
    import Parser._
    Def.setting {
      val modelDir = modelsDirectory.value
      val allModels = modelFiles(modelDir)
      if (allModels.isEmpty)
        EOF ^^^ None
      else
        (Space ~>
          allModels
            .map(d => (modelDir.getName + File.separator + modelDir.toPath.relativize(d).toString ^^^ Some(d)))
            .reduce(_ | _))
    }
  }

  def modelFiles(directory: File): Seq[Path] = {
    val dirPath = directory.toPath
    FileActions.enumeratePaths(dirPath)
      .filter(p => p.getFileName.toString.endsWith(".nlogox") || p.getFileName.toString.endsWith(".nlogox3d"))
  }

  lazy val settings = Seq(
    modelIndex := {
      streams.value.log.info("creating models/index.conf")
      val path = modelsDirectory.value / "index.conf"
      IO.write(path, generateIndex(modelsDirectory.value, streams.value.log))
      Seq(path)
    },
    javaOptions += "-Dnetlogo.models.dir=" + modelsDirectory.value.getAbsolutePath.toString,
    resaveModels := {
      makeMainTask("org.nlogo.tools.ModelResaver",
        classpath = (Test / fullClasspath),
        workingDirectory = baseDirectory(_.getParentFile)).toTask("").value
    },
    resaveModel := {
      makeMainTask("org.nlogo.tools.ModelResaver",
        classpath = (Test / fullClasspath),
        workingDirectory = baseDirectory(_.getParentFile))
    },
    exportResizedModels := {
      makeMainTask("org.nlogo.tools.ExportResizedModels",
        classpath = (Test / fullClasspath),
        workingDirectory = baseDirectory(_.getParentFile)).toTask("").value
    },
    convertWidgetSizes := {
      makeMainTask("org.nlogo.tools.ConvertWidgetSizes",
        classpath = (Test / fullClasspath),
        workingDirectory = baseDirectory(_.getParentFile)).toTask("").value
    },
    benchmark := {
      makeMainTask("org.nlogo.headless.HeadlessBenchmarker",
        classpath = (Test / fullClasspath),
        workingDirectory = baseDirectory(_.getParentFile)).toTask("").value
    },
    benchmarkGUI := {
      makeMainTask("org.nlogo.workspace.Benchmarker",
        classpath = (Test / fullClasspath),
        workingDirectory = baseDirectory(_.getParentFile)).toTask("").value
    }
  )

  private def generateIndex(modelsPath: File, logger: Logger): String = {
    import scala.collection.JavaConverters._
    val buf = new StringBuilder
    def println(s: String): Unit = { buf ++= s + "\n" }
    val paths = FileActions
      .enumeratePaths(modelsPath.toPath)
      .filterNot(p => p.toString.contains("test"))
      .filterNot(p => Files.isDirectory(p))
      .filter(p => p.getFileName.toString.endsWith("nlogox") || p.getFileName.toString.endsWith("nlogox3d"))
    def infoTab(path: Path) = try {
      InfoExtractor(Files.readAllLines(path).asScala.mkString("\n"))
    } catch {
      case e: Exception =>
        logger.error(s"while generating index, encountered error on file $path : ${e.getMessage}")
        throw e
    }
    println("models.indexes = [")
    for(path <- paths) {
      val info = infoTab(path)
      // The (?s) part allows . to match line endings
      val whatIsItPattern = "(?s).*## WHAT IS IT\\?\\s*\\n"
      if (info.matches(whatIsItPattern + ".*") ) {
        val firstParagraph = info.replaceFirst(whatIsItPattern, "").split('\n').head
        val formattedFirstParagraph = Markdown(firstParagraph, "", false)
        val q3 = "\"\"\""
        val relativeText = modelsPath.toPath.relativize(path).toString
        // if we generate the `index.conf` on Windows, we need to replace any backslashes so they'll work
        // when reading in later on in `ModelsLibraryDialog` where we assume forward-slashes make up the key.
        // -Jeremy B December 2020
        val keyText = relativeText.replace(System.getProperty("file.separator"), "/")
        println(s"  { path: ${q3}models/${keyText}${q3}, info: ${q3}${formattedFirstParagraph}${q3} },")
      } else {
        System.err.println("WHAT IS IT not found: " + path)
      }
    }
    println("]")
    buf.toString
  }

}

import java.io.File
import java.nio.file.{ Files, Path, Paths }
import java.util.regex.Pattern
import sbt._
import Keys._
import Def.Initialize
import sbt.complete.{ Parser, DefaultParsers }, DefaultParsers.{ EOF, Space }

object ModelsLibrary {

  val modelsDirectory = settingKey[File]("models directory")

  val resaveModel = InputKey[Unit]("resaveModel", "resave a single model")

  val resaveModels = TaskKey[Unit]("resaveModels", "resave all library models")

  val modelIndex = TaskKey[Seq[File]](
    "modelIndex", "builds models/index.txt for use in Models Library dialog")

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
      .filter(p => p.getFileName.toString.endsWith(".nlogo") || p.getFileName.toString.endsWith(".nlogo3d"))
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
      (runMain in Test).toTask(" org.nlogo.tools.ModelResaver").value
    },
    resaveModel := {
      modelParser.parsed.foreach { model =>
        val runner = new ForkRun(ForkOptions()
          .withWorkingDirectory(Some(baseDirectory.value.getParentFile))
          .withRunJVMOptions(Vector("-Dorg.nlogo.is3d=" + System.getProperty("org.nlogo.is3d"))))
        runner.run("org.nlogo.tools.ModelResaver",
          (fullClasspath in Test).value.map(_.data), Seq(model.toString), streams.value.log)
      }
    }
  )

  private def generateIndex(modelsPath: File, logger: Logger): String = {
    import scala.collection.JavaConverters._
    val buf = new StringBuilder
    def println(s: String) { buf ++= s + "\n" }
    val paths = FileActions
      .enumeratePaths(modelsPath.toPath)
      .filterNot(p => p.toString.contains("test"))
      .filterNot(p => Files.isDirectory(p))
      .filter(p => p.getFileName.toString.endsWith("nlogo") || p.getFileName.toString.endsWith("nlogo3d"))
    def infoTab(path: Path) = try {
      Files.readAllLines(path).asScala.mkString("\n").split("\\@\\#\\$\\#\\@\\#\\$\\#\\@\n")(2)
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
        val q3 = "\"\"\""
        println(s"  { path: ${q3}models/${modelsPath.toPath.relativize(path)}${q3}, info: ${q3}${firstParagraph}${q3} },")
      } else {
        System.err.println("WHAT IS IT not found: " + path)
      }
    }
    println("]")
    buf.toString
  }

}

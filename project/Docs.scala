import sbt._
import Keys._
import NetLogoBuild.{ buildDate, marketingVersion, numericMarketingVersion, autogenRoot }
import ModelsLibrary.modelsDirectory
import Extensions.extensionRoot
import Scaladoc.apiScaladoc
import NetLogoPackaging.{ netLogoRoot, buildVariables }

object Docs {
  lazy val netLogoDocs            = taskKey[NetLogoDocs]("netlogo docs object used to build documentation")
  lazy val allDocs                = taskKey[Seq[File]]("all documentation: html and pdf")
  lazy val htmlDocs               = taskKey[Seq[File]]("html documentation and prim indices")
  lazy val manualPDF              = taskKey[File]("NetLogo manual PDF")
  lazy val docsRoot               = settingKey[File]("location to which docs are generated")
  lazy val documentedExtensions   = settingKey[Seq[(String, String)]]("list of extensions setup to use the documenter")
  lazy val extensionDocConfigFile = settingKey[File]("extension documentation config file")
  lazy val extensionDocs          = taskKey[Seq[File]]("generate extension documentation")
  lazy val extensionDocsGen       = taskKey[ExtensionDocs]("extension docs object used to build extension documentation")
  lazy val testDocLinks           = taskKey[Map[String, Seq[String]]]("check for broken links in the documentation")

  lazy val settings = Seq(
    javaOptions    += "-Dnetlogo.docs.dir=" + docsRoot.value.getAbsolutePath.toString,
    docsRoot       := baseDirectory.value / "docs",
    buildVariables := Map[String, String](
      "version"            -> marketingVersion.value,
      "numericOnlyVersion" -> numericMarketingVersion.value,
      "date"               -> buildDate.value,
      "year"               -> buildDate.value.takeRight(4)),
    netLogoRoot := baseDirectory.value.getParentFile,
    netLogoDocs := {
      new NetLogoDocs(
        autogenRoot.value,
        docsRoot.value.getAbsoluteFile,
        baseDirectory.value,
        modelsDirectory.value,
        extensionDocsGen.value)
    },
    allDocs := {
      htmlDocs.value :+ manualPDF.value :+ apiScaladoc.value
    },
    htmlDocs := {
      netLogoDocs.value.generateHTML(buildVariables.value, documentedExtensions.value)
    },
    manualPDF := {
      netLogoDocs.value.generatePDF(buildVariables.value, documentedExtensions.value)
    },
    extensionDocConfigFile := {
      baseDirectory.value.getParentFile / "project" / "documentation.conf"
    },
    // keys are page name / extension name, values are title in sidebar
    documentedExtensions := {
      Seq(
        "arduino"  -> "Arduino",
        "array"    -> "Array",
        "bitmap"   -> "Bitmap",
        "cf"       -> "Control Flow",
        "csv"      -> "CSV",
        "gis"      -> "GIS",
        "gogo"     -> "GoGo",
        "ls"       -> "LevelSpace",
        "matrix"   -> "Matrices",
        "nw"       -> "Networks",
        "palette"  -> "Palette",
        "profiler" -> "Profiler",
        "r"        -> "R",
        "rnd"      -> "Rnd",
        "sound"    -> "Sound",
        "table"    -> "Table",
        "vid"      -> "Vid",
        "view2.5d" -> "View2.5D"
      )
    },
    extensionDocs := {
      extensionDocsGen.value.generateExtensionDocs(
        docsRoot.value, documentedExtensions.value, buildVariables.value)
      .map(_.toFile)
    },
    extensionDocsGen := {
      new ExtensionDocs(extensionRoot.value, extensionDocConfigFile.value)
    },
    testDocLinks := {
      val res = NetLogoDocsTest(docsRoot.value.getAbsoluteFile)
      res.foreach {
        case (file, links) =>
          println(file)
          links.foreach { link => println(s"\t$link") }
      }
      res
    }
  )
}

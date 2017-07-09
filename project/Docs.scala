import sbt._
import Keys._
import NetLogoBuild.{ buildDate, marketingVersion, numericMarketingVersion, autogenRoot }
import ModelsLibrary.modelsDirectory
import Extensions.extensionRoot
import Scaladoc.apiScaladoc
import NetLogoPackaging.{ netLogoRoot, buildVariables }

object Docs {
  lazy val netLogoDocs = taskKey[NetLogoDocs]("netlogo docs object used to build documentation")

  lazy val allDocs = taskKey[Seq[File]]("all documentation: html and pdf")

  lazy val htmlDocs = taskKey[Seq[File]]("html documentation and prim indices")

  lazy val manualPDF = taskKey[File]("NetLogo manual PDF")

  lazy val docsRoot = settingKey[File]("location to which docs are generated")

  lazy val documentedExtensions = settingKey[Seq[(String, String)]]("list of extensions setup to use the documenter")

  lazy val extensionDocConfigFile = settingKey[File]("extension documentation config file")

  lazy val extensionDocs = taskKey[Seq[File]]("generate extension documentation")

  lazy val settings = Seq(
    javaOptions    += "-Dnetlogo.docs.dir=" + docsRoot.value.getAbsolutePath.toString,
    docsRoot       := baseDirectory.value / "docs",
    buildVariables := Map[String, String](
      "version"               -> marketingVersion.value,
      "numericOnlyVersion"    -> numericMarketingVersion.value,
      "date"                  -> buildDate.value),
    netLogoRoot := baseDirectory.value.getParentFile,
    netLogoDocs := {
      new NetLogoDocs(
        autogenRoot.value / "docs",
        docsRoot.value.getAbsoluteFile,
        baseDirectory.value,
        modelsDirectory.value,
        extensionRoot.value,
        extensionDocConfigFile.value)
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
      documentedExtensions.value.map(_._1).map(extensionName =>
          netLogoDocs.value.generateHTMLPageForExtension(
            extensionName,
            (docsRoot.value / (extensionName + ".html").toLowerCase).toPath,
            buildVariables.value).toFile)
    }
  )
}

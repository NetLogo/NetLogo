import sbt._
import Keys._
import NetLogoBuild.{ buildDate, marketingVersion, numericMarketingVersion, autogenRoot }
import ModelsLibrary.modelsDirectory
import Extensions.extensionRoot
import NetLogoPackaging.{ netLogoRoot, buildVariables }

object Docs {
  lazy val netLogoDocs = taskKey[NetLogoDocs]("netlogo docs object used to build documentation")

  lazy val allDocs = taskKey[Seq[File]]("all documentation: html and pdf")

  lazy val htmlDocs = taskKey[Seq[File]]("html documentation and prim indices")

  lazy val manualPDF = taskKey[File]("NetLogo manual PDF")

  lazy val docsRoot = settingKey[File]("location to which docs are generated")

  lazy val documentedExtensions = settingKey[Seq[(String, String)]]("list of extensions setup to use the documenter")

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
        extensionRoot.value)
    },
    allDocs := {
      htmlDocs.value :+ manualPDF.value
    },
    htmlDocs := {
      netLogoDocs.value.generateHTML(buildVariables.value, documentedExtensions.value)
    },
    manualPDF := {
      netLogoDocs.value.generatePDF(buildVariables.value)
    },
    // keys are page name / extension name, values are title in sidebar
    documentedExtensions := {
      Seq(
        "arduino"  -> "Arduino",
        "array"    -> "Array",
        "bitmap"   -> "Bitmap",
        "gis"      -> "GIS",
        "gogo"     -> "GoGo",
        "matrix"   -> "Matrix",
        "palette"  -> "Palette",
        "profiler" -> "Matrices",
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
            (baseDirectory.value.getParentFile / "project" / "documentation.conf").toPath,
            buildVariables.value).toFile)
    }
  )
}

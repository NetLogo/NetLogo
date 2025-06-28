import sbt._
import Keys._
import NetLogoBuild.{ buildDate, marketingVersion, numericMarketingVersion, autogenRoot }
import ModelsLibrary.modelsDirectory
import Extensions.extensionRoot
import NetLogoPackaging.buildVariables

object Docs {
  lazy val netLogoDocs                  = taskKey[NetLogoDocs]("netlogo docs object used to build documentation")
  lazy val allDocs                      = taskKey[Seq[File]]("all documentation: html and pdf")
  lazy val htmlDocs                     = taskKey[Seq[File]]("html documentation and prim indices")
  lazy val manualPDF                    = taskKey[File]("NetLogo manual PDF")
  lazy val docsRoot                     = settingKey[File]("location to which docs are generated")
  lazy val autoDocumentedExtensions     = settingKey[Seq[(String, String)]]("list of extensions setup to use the documenter")
  lazy val manuallyDocumentedExtensions = settingKey[Seq[String]]("list of extensions with manually generated documentation")
  lazy val extensionDocConfigFile       = settingKey[File]("extension documentation config file")
  lazy val extensionDocs                = taskKey[Seq[File]]("generate extension documentation")
  lazy val extensionDocsGen             = taskKey[ExtensionDocs]("extension docs object used to build extension documentation")
  lazy val testDocLinks                 = taskKey[Map[String, Seq[String]]]("check for broken links in the documentation")

  lazy val settings = Seq(
    javaOptions    += "-Dnetlogo.docs.dir=" + docsRoot.value.getAbsolutePath.toString,
    docsRoot       := baseDirectory.value / "docs",
    buildVariables := Map[String, String](
      "version"            -> marketingVersion.value,
      "numericOnlyVersion" -> numericMarketingVersion.value,
      "year"               -> buildDate.value.takeRight(4),
      "date"               -> buildDate.value),
    netLogoDocs := {
      new NetLogoDocs(
        autogenRoot.value / "docs",
        docsRoot.value.getAbsoluteFile,
        baseDirectory.value,
        modelsDirectory.value,
        extensionDocsGen.value,
        extensionRoot.value)
    },
    allDocs := {
      htmlDocs.value :+ manualPDF.value :+ (Compile / doc).value
    },
    htmlDocs := {
      netLogoDocs.value.generateHTML(buildVariables.value, autoDocumentedExtensions.value,
                                     manuallyDocumentedExtensions.value)
    },
    manualPDF := {
      netLogoDocs.value.generatePDF(buildVariables.value, autoDocumentedExtensions.value,
                                    manuallyDocumentedExtensions.value)
    },
    extensionDocConfigFile := {
      baseDirectory.value.getParentFile / "project" / "documentation.conf"
    },
    // keys are page name / extension name, values are title in sidebar
    autoDocumentedExtensions := {
      Seq(
        "arduino"  -> "Arduino",
        "array"    -> "Array",
        "bitmap"   -> "Bitmap",
        "csv"      -> "CSV",
        "gis"      -> "GIS",
        "gogo"     -> "GoGo",
        "ls"       -> "LevelSpace",
        "matrix"   -> "Matrices",
        "nw"       -> "Networks",
        "palette"  -> "Palette",
        "profiler" -> "Profiler",
        "py"       -> "Python",
        "resource" -> "Resource",
        "rnd"      -> "Rnd",
        "sound"    -> "Sound",
        "sr"       -> "Simple R",
        "table"    -> "Table",
        "time"     -> "Time",
        "vid"      -> "Vid",
        "view2.5d" -> "View2.5D"
      )
    },
    // expects the documentation file at extensions/<name>/README.md.mustache (Isaac B 6/28/25)
    manuallyDocumentedExtensions := {
      Seq(
        "bspace"
      )
    },
    extensionDocs := {
      extensionDocsGen.value.generateExtensionDocs(
        docsRoot.value, autoDocumentedExtensions.value, buildVariables.value)
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

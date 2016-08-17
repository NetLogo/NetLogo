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
      netLogoDocs.value.generateHTML(buildVariables.value)
    },
    manualPDF := {
      netLogoDocs.value.generatePDF(buildVariables.value)
    }
  )
}

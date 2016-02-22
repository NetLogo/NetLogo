import sbt._
import Keys._
import NetLogoBuild.{ buildDate, marketingVersion, numericMarketingVersion, autogenRoot }

object Docs {
  lazy val netLogoDocs = taskKey[NetLogoDocs]("netlogo docs object used to build documentation")

  lazy val buildVariables = taskKey[Map[String, String]]("NetLogo template variables")

  lazy val netLogoRoot = taskKey[File]("root of the netlogo project")

  lazy val allDocs = taskKey[Seq[File]]("all documentation: html and pdf")

  lazy val htmlDocs = taskKey[Seq[File]]("html documentation and prim indices")

  lazy val manualPDF = taskKey[File]("NetLogo manual PDF")

  lazy val settings = Seq(
    buildVariables := Map[String, String](
      "version"               -> marketingVersion.value,
      "numericOnlyVersion"    -> numericMarketingVersion.value,
      "date"                  -> buildDate.value),
    netLogoRoot := baseDirectory.value.getParentFile,
    netLogoDocs := {
      new NetLogoDocs(autogenRoot.value / "docs", baseDirectory.value / "docs", netLogoRoot.value)
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

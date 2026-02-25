import sbt._
import Keys._
import NetLogoBuild.{ buildDate, marketingVersion, year, autogenRoot }
import ModelsLibrary.modelsDirectory
import Extensions.extensionRoot

import java.nio.file.{ Files, StandardCopyOption }

import scala.sys.process.Process

object Docs {
  lazy val allDocs                      = taskKey[Unit]("all documentation: html and pdf")
  lazy val manualPDF                    = taskKey[Unit]("NetLogo manual PDF")
  lazy val helioRoot                    = settingKey[File]("location of helio root")
  lazy val docsSource                   = settingKey[File]("location of docs source files")
  lazy val docsDest                     = settingKey[File]("location to which docs are generated")
  lazy val testDocLinks                 = taskKey[Map[String, Seq[String]]]("check for broken links in the documentation")

  lazy val settings = Seq(
    javaOptions += "-Dnetlogo.docs.dir=" + baseDirectory.value.getAbsolutePath,
    helioRoot   := baseDirectory.value.getParentFile / "helio",
    docsSource  := helioRoot.value / "apps" / "docs",
    docsDest    := baseDirectory.value / "docs",
    allDocs := {
      manualPDF.value
      (Compile / doc).value
    },
    manualPDF := {
      Process(Seq("yarn", "run", "init"), helioRoot.value).!
      Process(Seq("yarn", "run", "docs:build"), docsSource.value, "HELIO_HEADLESS" -> "1").!
      Process(Seq("yarn", "run", "docs:generate-manual"), docsSource.value).!

      val manualSource = (docsSource.value / ".build" / "NetLogo_User_Manual.pdf").toPath
      val manualDest = (baseDirectory.value / "NetLogo_User_Manual.pdf").toPath

      Files.copy(manualSource, manualDest, StandardCopyOption.REPLACE_EXISTING)

      val linksSource = (docsSource.value / ".build" / "manual-links.csv").toPath
      val linksDest = (baseDirectory.value / "manual-links.csv").toPath

      Files.copy(linksSource, linksDest, StandardCopyOption.REPLACE_EXISTING)
    },
    testDocLinks := {
      val res = NetLogoDocsTest(docsDest.value.getAbsoluteFile)
      res.foreach {
        case (file, links) =>
          println(file)
          links.foreach { link => println(s"\t$link") }
      }
      res
    }
  )
}

import sbt._
import Keys._
import NetLogoBuild.{ buildDate, year, autogenRoot }
import NetLogoPackaging.RunProcess
import ModelsLibrary.modelsDirectory
import Extensions.extensionRoot

import java.nio.file.{ Files, Path, StandardCopyOption }

object Docs {
  lazy val staticDocs   = taskKey[Unit]("NetLogo static documentation")
  lazy val helioRoot    = settingKey[File]("location of helio root")
  lazy val docsSource   = settingKey[File]("location of docs source files")
  lazy val docsDest     = settingKey[File]("location to which docs are generated")
  lazy val testDocLinks = taskKey[Map[String, Seq[String]]]("check for broken links in the documentation")

  lazy val settings = Seq(
    javaOptions        += "-Dnetlogo.docs.dir=" + baseDirectory.value.getAbsolutePath,
    Test / javaOptions += "-Dnetlogo.docs.dir=" + baseDirectory.value.getAbsolutePath,
    helioRoot          := baseDirectory.value.getParentFile / "helio",
    docsSource         := helioRoot.value / "apps" / "docs",
    docsDest           := baseDirectory.value / "docs",
    staticDocs := {
      if (System.getenv("CI") == null) {
        val yarnBin =
          if (!System.getProperty("os.name").toLowerCase.startsWith("windows"))
            "yarn"
          else
            "yarn.cmd"
        RunProcess(Seq(yarnBin, "run", "init"), helioRoot.value, "Initialize Helio")
        RunProcess(Seq(yarnBin, "run", "docs:build:offline"), docsSource.value, "Build static documentation",
                   "HELIO_HEADLESS" -> "1")
      }

      val manualSource = (docsSource.value / ".build" / "NetLogo_User_Manual.pdf").toPath
      val manualDest = (baseDirectory.value / "NetLogo_User_Manual.pdf").toPath

      Files.copy(manualSource, manualDest, StandardCopyOption.REPLACE_EXISTING)

      val linksSource = (docsSource.value / ".build" / "manual-links.csv").toPath
      val linksDest = (baseDirectory.value / "resources" / "system" / "manual-links.csv").toPath

      Files.copy(linksSource, linksDest, StandardCopyOption.REPLACE_EXISTING)

      val htmlSource = (docsSource.value / ".build" / "latest").toPath
      val htmlDest = (baseDirectory.value / "docs" / "static").toPath

      Files.walk(htmlSource).forEach { path =>
        if (!Files.isDirectory(path)) {
          val dest: Path = htmlDest.resolve(htmlSource.relativize(path))

          Files.createDirectories(dest.getParent)
          Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING)
        }
      }
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

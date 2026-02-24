// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.io.File
import java.nio.file.{ Files, Path, Paths }
import javax.imageio.ImageIO

import org.nlogo.api.{ FileIO, Version }
import org.nlogo.core.CompilerException
import org.nlogo.headless.ChecksumsAndPreviewsSettings.ChecksumsPath
import org.nlogo.nvm.Workspace
import org.nlogo.workspace.{ Checksummer, ModelsLibrary, PreviewCommandsRunner }

import scala.io.Source

object ChecksumsAndPreviews {

  val allBenchmarks =
    Seq("ANN", "Ants", "Bureaucrats", "BZ", "CA1D", "Erosion", "Fire", "FireBig", "Flocking", "GasLabCirc",
        "GasLabNew", "GasLabOld", "GridWalk", "Heatbugs", "Ising", "Life", "PrefAttach",
        "Team", "Termites", "VirusNet", "Wealth", "Wolf", "ImportWorld")

  def main(argv: Array[String]): Unit = {
    Main.setHeadlessProperty()

    def paths(includeBenchmarks: Boolean): Seq[Path] = {
      val allLibrary: Seq[Path] = ModelsLibrary.getModelPaths(true, false).map { path =>
        Paths.get(ModelsLibrary.modelsRoot).toAbsolutePath.getParent.relativize(Paths.get(path))
      }.toSeq

      if (includeBenchmarks) {
        allBenchmarks.map(name => Paths.get(s"models/test/benchmarks/$name Benchmark.nlogox")) ++ allLibrary
      } else {
        allLibrary
      }
    }

    def readVariants(): Map[Path, Seq[String]] = {
      val source = Source.fromFile("test/checksum-variants.txt")
      val lines = source.getLines.collect {
        case line if !line.trim.startsWith("#") && line.trim.nonEmpty =>
          line.trim
      }

      if (!lines.hasNext) {
        source.close()

        return Map()
      }

      val firstModelLine = lines.next()

      if (!firstModelLine.startsWith("*")) {
        source.close()

        throw new IllegalStateException(s"variants file must start with a '*' model entry: $lines")
      }

      def getModelPath(path: String): Path = Paths.get(path.stripPrefix("*").trim)

      def mapper(current: (Path, Map[Path, Seq[String]]), line: String): (Path, Map[Path, Seq[String]]) = {
        if (line.startsWith("*")) {
          val newModel = getModelPath(line)
          (newModel, current._2 + (newModel -> Seq()))
        } else {
          val currentVariants = current._2.getOrElse(current._1, Seq())
          (current._1, current._2 + (current._1 -> (currentVariants :+ line.trim)))
        }
      }

      val firstModel = getModelPath(firstModelLine)
      val variants = lines.foldLeft((firstModel, Map(firstModel -> Seq())))(mapper)

      source.close()

      variants._2
    }

    try {
      // The option names correspond to task names in sbt - ST 2/12/09
      // except "checksums" is "all-checksums" since in sbt the former
      // is already taken - ST 6/28/12
      argv match {
        case Array("--checksum", path) =>
          Checksums.update(Seq(Paths.get(path)), readVariants())
        case Array("--checksums") =>
          Checksums.update(paths(includeBenchmarks = !Version.is3D), readVariants())
        case Array("--preview", path) =>
          Previews.remake(Paths.get(path))
        case Array("--previews") =>
          paths(false).foreach(Previews.remake)
        case _ =>
          throw new Exception(s"Unexpected input arguments: $argv")
      }

      println("done")

      // this call forces any lingering resources to be cleaned up, ensuring
      // that the task always exits immediately (Isaac B 10/2/25)
      System.exit(0)
    } catch {
      case e: Exception =>
        e.printStackTrace()

        System.exit(1)
    }
  }

  def checksumEntries(file: File = new File(ChecksumsPath)): Array[Entry] = {
    val children = file.listFiles

    if (children.exists(_.isDirectory)) {
      children.flatMap(checksumEntries)
    } else {
      val name = file.getName

      val (baseName, variant) = {
        val extIndex = name.lastIndexOf(".nlogox")
        val varIndex = name.indexOf('(', extIndex)

        if (varIndex == -1) {
          (name, None)
        } else {
          (name.substring(0, varIndex - 1), Option(name.substring(varIndex + 1, name.lastIndexOf(')'))))
        }
      }

      val checksumPath = file.toPath
      val modelPath = Paths.get(ChecksumsPath).relativize(checksumPath).resolveSibling(baseName)

      Array(Entry(modelPath, checksumPath, variant))
    }
  }

  case class Entry(modelPath: Path, checksumPath: Path, variant: Option[String])

  object Previews {

    def remake(path: Path): Unit = {
      try {
        val runner = PreviewCommandsRunner.fromModelPath(new WorkspaceFactory {
          def openCurrentModelIn(workspace: Workspace): Unit = {}
        }, path.toString)
        println(s"making preview for: $path")
        FileIO.writeImageFile(runner.previewImage.get, s"$path.png", "PNG")
      } catch {
        case _: PreviewCommandsRunner.NonCompilableCommandsException =>
          println(s"skipping: $path\n  (non-compilable preview commands)")
        case e: CompilerException =>
          println(s"skipping: $path\n  ${e.getMessage}")
      }
    }

  }

  /// checksums

  object Checksums {

    // image output stream caching can result in null pointer exceptions at the end of checksum
    // generation, and it's not really necessary for what we're doing here, so it can be disabled.
    // (Isaac B 12/17/25)
    ImageIO.setUseCache(false)

    private def variantPath(path: Path, variant: String): Path = {
      if (variant.isEmpty) {
        path
      } else {
        path.resolveSibling(s"${path.getFileName} ($variant)")
      }
    }

    def update(paths: Seq[Path], variants: Map[Path, Seq[String]]): Unit = {
      // prevent annoying JAI message on Linux when using JAI extension
      // (old.nabble.com/get-rid-of-%22Could-not-find-mediaLib-accelerator-wrapper-classes%22-td11025745.html)
      System.setProperty("com.sun.media.jai.disableMediaLib", "true")

      paths.foreach(p => updateOne(p, variants.getOrElse(p, Seq())))
    }

    private def updateOne(modelPath: Path, variants: Seq[String]): Unit = {
      val workspace = HeadlessWorkspace.newInstance

      workspace.silent = true

      if (Files.exists(modelPath)) {
        workspace.open(modelPath.toString, true)

        ("" +: variants).foreach(updateOneHelper(modelPath, _, workspace))
      } else {
        // if the model doesn't exist and it's in the checksum directory just remove it. if it's not in
        // the checksum directory let it fall through and report the error
        println(s"Model does not exist, deleting checksum for: $modelPath")

        if (variants.nonEmpty) {
          println(s"** Variants found for a deleted model, they should be removed if the model is really gone or changed if the model moved: $variants")

          variants.foreach(v => deleteRecursive(variantPath(modelPath, v).toFile))
        }
      }

      workspace.dispose()
    }

    private def updateOneHelper(modelPath: Path, variant: String, workspace: HeadlessWorkspace): Unit = {
      val checksumPath = variantPath(Paths.get(ChecksumsPath).resolve(modelPath), variant)

      println(s"Making checksum for: $modelPath")

      Checksummer.initModelForChecksumming(workspace, variant)

      Files.createDirectories(checksumPath)

      Files.writeString(checksumPath.resolve("world.csv"), Checksummer.exportWorld(workspace))
      ImageIO.write(workspace.renderer.exportView(workspace), "png", checksumPath.resolve("graphics.png").toFile)
    }

    private def deleteRecursive(file: File): Unit = {
      if (file.isDirectory) {
        file.listFiles.foreach(deleteRecursive)

        file.delete()
      } else {
        file.delete()
      }
    }

  }
}

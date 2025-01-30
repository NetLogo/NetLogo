// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import collection.mutable.LinkedHashMap

import java.io.{ BufferedReader, File, FileWriter, InputStreamReader }
import java.nio.file.{ Files, Paths }

import scala.io.Source

import org.nlogo.api.{ FileIO, Version }
import org.nlogo.core.CompilerException
import org.nlogo.headless.ChecksumsAndPreviewsSettings.ChecksumsFilePath
import org.nlogo.workspace.{ Checksummer, ModelsLibrary, PreviewCommandsRunner }

object ChecksumsAndPreviews {

  val allBenchmarks =
    List("ANN", "Ants", "Bureaucrats", "BZ", "CA1D", "Erosion", "Fire", "FireBig", "Flocking", "GasLabCirc",
         "GasLabNew", "GasLabOld", "GridWalk", "Heatbugs", "Ising", "Life", "PrefAttach",
         "Team", "Termites", "VirusNet", "Wealth", "Wolf", "ImportWorld")

  def main(argv: Array[String]) {
    Main.setHeadlessProperty()
    def paths(fn: String => Boolean, includeBenchmarks: Boolean) = {
      val allLibrary = ModelsLibrary.getModelPaths(true, false).toList
      val library = if (includeBenchmarks)
        allBenchmarks.map("models/test/benchmarks/" + _ + " Benchmark.nlogox") ::: allLibrary
      else
        allLibrary

      library
        .filter(fn)
        .map(p => {
          val iOf = p.indexOf(ModelsLibrary.modelsRoot)
          val r = p.substring(iOf)
          r
        })
        .distinct
        .toList
    }

    def readVariants(): Map[String, Seq[String]] = {
      val lines = Source.fromFile("test/checksum-variants.txt").getLines
        .map((s: String) => s.trim.split("#")(0))
        .filter((s: String) => !s.startsWith("#") && !s.isEmpty)
      if (!lines.hasNext) {
        return Map()
      }
      val firstModelLine = lines.next
      if (!firstModelLine.startsWith("*")) {
        throw new IllegalStateException(s"variants file must start with a '*' model entry: $lines")
      }

      def cleanModelPath(path: String): String = path.split("\\*")(1).trim

      def mapper(current: (String, Map[String, Seq[String]]), line: String): (String, Map[String, Seq[String]]) = {
        if (line.startsWith("*")) {
          val newModel = cleanModelPath(line)
          (newModel, current._2 + (newModel -> Seq()))
        } else {
          val currentVariants = current._2.getOrElse(current._1, Seq())
          (current._1, current._2 + (current._1 -> (currentVariants :+ line)))
        }
      }

      val firstModel = cleanModelPath(firstModelLine)
      val starter: (String, Map[String, Seq[String]]) = (firstModel, Map(firstModel -> Seq("")))
      val variants = lines.foldLeft(starter)(mapper)
      variants._2
    }

    // The option names correspond to task names in sbt - ST 2/12/09
    // except "checksums" is "all-checksums" since in sbt the former
    // is already taken - ST 6/28/12
    argv match {
      case Array("--checksum", path) =>
        if (Checksums.okPath(path)) Checksums.update(List(path), readVariants)
      case Array("--checksums") =>
        Checksums.update(paths(Checksums.okPath, includeBenchmarks = !Version.is3D), readVariants)
      case Array("--preview", path) =>
        Previews.remake(path)
      case Array("--previews") =>
        paths(Previews.okPath, false).foreach(Previews.remake)
      case Array("--checksum-export", path) =>
        ChecksumExports.export(List(path))
      case Array("--checksum-exports") =>
        ChecksumExports.export(paths(Checksums.okPath, includeBenchmarks = !Version.is3D))
    }
    println("done")
  }

  object Previews {

    def needsManualPreview(previewCommands: String) =
      previewCommands contains "need-to-manually-make-preview-for-this-model"

    // NW is only included temporarily until extension compiles correctly - AAB 7-2022
    def okPath(path: String) =
      List("HUBNET", "/GOGO/", "/CODE EXAMPLES/SOUND/", "GIS GRADIENT EXAMPLE", "/NW/")
        .forall(!path.toUpperCase.containsSlice(_))

    def remake(path: String) {
      val previewPath = path.replaceFirst("\\.nlogo$", ".png")
      try {
        val runner = PreviewCommandsRunner.fromModelPath(new WorkspaceFactory, path)
        println("making preview for: " + path)
        FileIO.writeImageFile(runner.previewImage.get, previewPath, "PNG")
      } catch {
        case _: PreviewCommandsRunner.NonCompilableCommandsException =>
          println("skipping: " + path + "\n  (non-compilable preview commands)")
        case e: CompilerException =>
          println("skipping: " + path + "\n  " + e.getMessage)
      }
    }

  }

  /// checksums

  object Checksums {

    val separator = " * " // used to separate fields in the checksums file

    def variantKey(path: String, variant: String): String =
      s"$path${if (variant == "") "" else s" ($variant)"}"

    case class Entry(path: String, variant: String, worldSum: String, graphicsSum: String, revision: String) {

      def equalsExceptRevision(other: Entry) =
        path == other.path && variant == other.variant && worldSum == other.worldSum && graphicsSum == other.graphicsSum

      val key = Checksums.variantKey(path, variant)

      override def toString =
        List(path, variant, worldSum, graphicsSum, revision).mkString(separator)

    }

    type ChecksumMap = LinkedHashMap[String, Entry]

    def okPath(path: String): Boolean = {
      val skips = ChecksumsAndPreviewsSettings.ModelsToSkip.filter({ case (_, paths) =>
        paths.exists((s) => path.toUpperCase.containsSlice(s.toUpperCase))
      })
      if (skips.length > 0) {
        println(s"SKIPPING MODEL: $path because ${skips.head._1.getOrElse("of unspecified reasons.")}")
        false
      } else {
        true
      }
    }

    def update(paths: List[String], variants: Map[String, Seq[String]]) {
      // prevent annoying JAI message on Linux when using JAI extension
      // (old.nabble.com/get-rid-of-%22Could-not-find-mediaLib-accelerator-wrapper-classes%22-td11025745.html)
      System.setProperty("com.sun.media.jai.disableMediaLib", "true")

      val m = load()
      val results: List[(String, Exception)] = paths.flatMap( (p) => updateOne(m, p, variants.getOrElse(p, Seq())) )
      write(m, ChecksumsFilePath)
      if (!results.isEmpty) {
        val errors = results.map({ case (model, ex) => s"Exception in $model: ${ex.toString()}" }).mkString("\n\n")
        throw new Exception(s"Not all checksums completed.  Review the errors and resolve them or add to `ModelsToSkip` as needed.\n\n$errors")
      }
    }

    def updateOne(m: ChecksumMap, model: String, variants: Seq[String]): Option[(String, Exception)] = {
      val workspace = HeadlessWorkspace.newInstance
      workspace.silent = true
      try {
        if (!new File(model).exists && m.contains(model)) {
          // if the model doesn't exist and it's in the checksum file just remove it. if it's not in
          // the checksum file let it fall through and report the error
          m.remove(model)
          println("Model does not exist, deleting checksum for: " + model)
          if (variants.length != 0) {
            variants.foreach((v) => m.remove(Checksums.variantKey(model, v)))
            println(s"** Variants found for a deleted model, they should be removed if the model is really gone or changed if the model moved: $variants")
          }
        }
        else {
          workspace.open(model)
          updateOneHelper(m, model, "", workspace)
          variants.sortWith(_ < _).foreach(updateOneHelper(m, model, _, workspace))
        }
      }
      catch {
        case e: Exception =>
          println(s"Exception occured making checksums for $model")
          return Some((model, e))
      }
      finally { workspace.dispose() }
      None
    }

    def updateOneHelper(m: ChecksumMap, model: String, variant: String, workspace: HeadlessWorkspace) {
      val key = Checksums.variantKey(model, variant)
      Checksummer.initModelForChecksumming(workspace, variant)
      val newCheckSum = Checksummer.calculateWorldChecksum(workspace)
      val newGraphicsChecksum = Checksummer.calculateGraphicsChecksum(workspace)
      val revision = getRevisionNumber(workspace.getModelPath)
      val oldEntry = m.get(key)
      val newEntry = Entry(model, variant, newCheckSum, newGraphicsChecksum, revision)
      // figure out if the entry is new, changed, or the same
      val action =
        if (!m.contains(key)) "* Added"
        else if (oldEntry.get == newEntry) "Didn't change"
        else if (oldEntry.get.equalsExceptRevision(newEntry)) "* Changed rev # only"
        else "* Changed"
      m.put(newEntry.key, newEntry)
      if (action != "Didn't change")
        println(action + ": \"" + newEntry.toString + "\"")
    }

    def load(): ChecksumMap = {
      val m = new ChecksumMap
      for (line <- Source.fromFile(ChecksumsFilePath).getLines.map(_.trim))
        if (!line.startsWith("#") && !line.isEmpty) {
          val strs = line.split(java.util.regex.Pattern.quote(separator))
          if (strs.size != 5)
            throw new IllegalStateException("bad line: " + line)
          val entry = Entry(strs(0), strs(1), strs(2), strs(3), strs(4))
          m.put(entry.key, entry)
        }
      m
    }

    def write(m: ChecksumMap, path: String) {
      val fw = new FileWriter(path)
      m.values.foreach(entry => fw.write(entry.toString + '\n'))
      fw.close()
    }

    def getRevisionNumber(modelPath: String): String = {
      val cmds = Array("git", "log", "--pretty=format:%H",
        new File(modelPath).getAbsolutePath)
      val proc =
        Runtime.getRuntime().exec(cmds, Array[String](), new File(ModelsLibrary.modelsRoot))
      val stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream))
      val stdError = Source.fromInputStream(proc.getErrorStream)
      // rather than use %h, we take the first 10 of %H. Git changed things making %h different
      // across versions (see https://github.com/git/git/commit/e6c587c733b4634030b353f4024794b08bc86892)
      Option(stdInput.readLine()).map(_.trim.take(10)).getOrElse(
        throw new Exception("Error fetching SHA1 of model: " + stdError.mkString))
    }
  }

  // For when you need to know what the checksummed world exports are
  object ChecksumExports {

    def export(paths: List[String]): Unit = {
      paths.foreach(exportOne)
    }

    def exportOne(path: String): Unit = {
      val workspace = HeadlessWorkspace.newInstance
      try {
        import scala.collection.JavaConverters._
        workspace.open(path)
        Checksummer.initModelForChecksumming(workspace, "")
        val modelPath = Paths.get(path)
        val modelIndex = modelPath.iterator.asScala.indexWhere(_.toString == "models")
        val pathCount = modelPath.getNameCount
        val modelName = modelPath.getName(pathCount - 1).toString
        val exportPath =
          Paths.get("tmp/checksum-exports")
            .resolve(
              modelPath.subpath(modelIndex, pathCount - 2)
                .resolve(modelName.replaceAllLiterally(".nlogo", ".csv")))

        Files.createDirectories(exportPath.getParent)
        workspace.exportWorld(exportPath.toString)
      } catch {
        case e: Exception =>
          println("SKIPPING MODEL: " + path + "\n  because of exception:")
          e.printStackTrace()
      }
      finally { workspace.dispose() }
    }

  }
}

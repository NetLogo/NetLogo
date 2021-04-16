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
      val benchmarks = allBenchmarks.map("models/test/benchmarks/" + _ + " Benchmark.nlogo")
      val library =
        ModelsLibrary.getModelPaths(true)
          .filter(fn)
          .map(p => p.substring(p.indexOf(ModelsLibrary.modelsRoot)))
          .distinct
          .toList
      if (includeBenchmarks)
        benchmarks ::: library
      else
        library
    }

    def readVariants(): Map[String, Seq[String]] = {
      // val lines = Source.fromFile("test/checksum-variants.txt").getLines
      //   .map(_.trim)
      //   .filter(!_.startsWith("#") && !_.isEmpty)
      Map()
    }

    // The option names correspond to task names in sbt - ST 2/12/09
    // except "checksums" is "all-checksums" since in sbt the former
    // is already taken - ST 6/28/12
    argv match {
      case Array("--checksum", path) =>
        Checksums.update(List(path), readVariants)
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

    def okPath(path: String) =
      List("HUBNET", "/GOGO/", "/CODE EXAMPLES/SOUND/", "GIS GRADIENT EXAMPLE")
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

    case class Entry(path: String, variant: String, worldSum: String, graphicsSum: String, revision: String) {

      def equalsExceptRevision(other: Entry) =
        path == other.path && variant == other.variant && worldSum == other.worldSum && graphicsSum == other.graphicsSum

      override def toString =
        List(path, variant, worldSum, graphicsSum, revision).mkString(separator)

    }

    type ChecksumMap = LinkedHashMap[String, Entry]

    def okPath(path: String) = (for {
      (message, slices) <- ChecksumsAndPreviewsSettings.ModelsToSkip
      slice <- slices
      if path.toUpperCase.containsSlice(slice)
    } yield {
      for (msg <- message) println("SKIPPING MODEL: " + path + "  because " + msg)
    }).isEmpty

    def update(paths: List[String], variants: Map[String, Seq[String]]) {
      val m = load()
      paths.foreach((p) => updateOne(m, p, variants.getOrElse(p, Seq())))
      write(m, ChecksumsFilePath)
    }

    def updateOne(m: ChecksumMap, model: String, variants: Seq[String]) {
      val workspace = HeadlessWorkspace.newInstance
      workspace.silent = true
      try {
        if (!new File(model).exists && m.contains(model)) {
          // if the model doesn't exist and it's in the checksum file just remove it. if it's not in
          // the checksum file let it fall through and report the error
          m.remove(model)
          println("Model does not exist, deleting checksum for: " + model)
        }
        else {
          workspace.open(model)
          updateOneHelper(m, model, "", workspace)
          variants.foreach(updateOneHelper(m, model, _, workspace))
        }
      }
      catch { case e: Exception =>
                println("SKIPPING MODEL: " + model + "\n  because of exception:")
                e.printStackTrace() }
      finally { workspace.dispose() }
    }

    def updateOneHelper(m: ChecksumMap, model: String, variant: String, workspace: HeadlessWorkspace) {
      Checksummer.initModelForChecksumming(workspace)
      val newCheckSum = Checksummer.calculateWorldChecksum(workspace)
      val newGraphicsChecksum = Checksummer.calculateGraphicsChecksum(workspace)
      val revision = getRevisionNumber(workspace.getModelPath)
      val oldEntry = m.get(model)
      val newEntry = Entry(model, variant, newCheckSum, newGraphicsChecksum, revision)
      // figure out if the entry is new, changed, or the same
      val action =
        if (!m.contains(model)) "* Added"
        else if (oldEntry.get == newEntry) "Didn't change"
        else if (oldEntry.get.equalsExceptRevision(newEntry)) "* Changed rev # only"
        else "* Changed"
      m.put(model, newEntry)
      if (action != "Didn't change")
        println(action + ": \"" + model + separator + newCheckSum
                + separator + newGraphicsChecksum + separator + revision + "\"")
    }

    def load(): ChecksumMap = {
      val m = new ChecksumMap
      for (line <- Source.fromFile(ChecksumsFilePath).getLines.map(_.trim))
        if (!line.startsWith("#") && !line.isEmpty) {
          val strs = line.split(java.util.regex.Pattern.quote(separator))
          if (strs.size != 5)
            throw new IllegalStateException("bad line: " + line)
          m.put(strs(0), Entry(strs(0), strs(1), strs(2), strs(3), strs(4)))
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
        Checksummer.initModelForChecksumming(workspace)
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

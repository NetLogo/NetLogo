// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.nio.file.{ Files, Paths }

import org.nlogo.api.{ PreviewCommands, TwoDVersion }
import org.nlogo.workspace.{ ModelsLibrary, Checksummer }

object ChecksumsAndPreviews {

  val Path = "test/checksums-headless.txt"

  val allBenchmarks =
    List("Ants", "Bureaucrats", "BZ", "CA1D", "Erosion", "Fire", "FireBig", "Flocking", "GasLabCirc",
         "GasLabNew", "GasLabOld", "GridWalk", "Heatbugs", "Ising", "Life", "PrefAttach",
         "Team", "Termites", "VirusNet", "Wealth", "Wolf", "ImportWorld")

  def main(argv: Array[String]) {
    Main.setHeadlessProperty()
    def paths(fn: String => Boolean, includeBenchmarks: Boolean) = {
      val benchmarks = allBenchmarks.map("models/test/benchmarks/" + _ + " Benchmark.nlogo")
      val library =
        ModelsLibrary.getModelPaths(TwoDVersion, true)
          .filter(fn)
          .map(p => p.substring(p.indexOf("models/")))
          .toList
      if (includeBenchmarks)
        benchmarks ::: library
      else
        library
    }
    // The option names correspond to task names in sbt - ST 2/12/09
    // except "checksums" is "all-checksums" since in sbt the former
    // is already taken - ST 6/28/12
    argv match {
      case Array("--checksum", path) =>
        Checksums.update(List(path))
      case Array("--checksums") =>
        Checksums.update(paths(Checksums.okPath, includeBenchmarks = true))
      case Array("--preview", path) =>
        Previews.remake(path)
      case Array("--previews") =>
        paths(Previews.okPath, false).foreach(Previews.remake)
      case Array("--checksum-export", path) =>
        ChecksumExports.export(List(path))
      case Array("--checksum-exports") =>
        ChecksumExports.export(paths(Checksums.okPath, includeBenchmarks = true))
    }
    println("done")
  }

  object Previews {
    def okPath(path: String) =
      List("HUBNET", "/GOGO/", "/CODE EXAMPLES/SOUND/")
        .forall(!path.toUpperCase.containsSlice(_))
    def remake(path: String) {
      val previewPath = path.replaceFirst("\\.nlogo$", ".png")
      val workspace = HeadlessWorkspace.newInstance
      workspace.silent = true
      try {
        // we set the random seed before opening the model, so that the random-seed will affect the
        // startup procedure if any - ST 7/12/06
        workspace.command("random-seed 0")
        workspace.open(path)
        if (workspace.previewCommands == PreviewCommands.Manual)
          println("skipping: " + path)
        else {
          println("making preview for: " + path)
          workspace.command(workspace.previewCommands.source)
          workspace.exportView(previewPath, "PNG")
        }
      }
      catch { case e: Exception => e.printStackTrace() }
      finally { workspace.dispose() }
    }
  }

  /// checksums

  object Checksums {
    val separator = " * " // used to separate fields in the checksums file
    case class Entry(path: String, worldSum: String, graphicsSum: String, revision: String) {
      def equalsExceptRevision(other: Entry) =
        path == other.path && worldSum == other.worldSum && graphicsSum == other.graphicsSum
      override def toString = List(path, worldSum, graphicsSum, revision).mkString(separator)
    }
    type ChecksumMap = collection.mutable.LinkedHashMap[String, Entry]

    def okPath(path: String) = (for {
      (message, slices) <- Seq(
        None -> List("HUBNET", "/CURRICULAR MODELS/"),
        Some("it renders slightly differently on Mac vs. Linux") -> List(
          "/CODE EXAMPLES/LINK BREEDS EXAMPLE.NLOGO"), // see 407ddcdd49f88395915b1a87c663b13000758d35 in `models` repo
        Some("it uses the sound extension") -> List(
          "/GAMES/FROGGER.NLOGO",
          "/ART/SOUND MACHINES.NLOGO",
          "/ART/GENJAM - DUPLE.NLOGO",
          "/CODE EXAMPLES/EXTENSIONS EXAMPLES/SOUND/"),
        Some("it uses behaviorspace-experiment-name") -> List(
          "/SAMPLE MODELS/BIOLOGY/EVOLUTION/ANISOGAMY.NLOGO"),
        Some("it uses the ls extension, not setup for headless") -> List(
          "/SAMPLE MODELS/CHEMISTRY & PHYSICS/KICKED ROTATORS.NLOGO",
          "/CODE EXAMPLES/EXTENSIONS EXAMPLES/LS/"),
        Some("it involves the system dynamics modeler") -> List(
          "/SAMPLE MODELS/SYSTEM DYNAMICS/"),
        Some("it involves user interaction") -> List(
          "/CODE EXAMPLES/USER INTERACTION EXAMPLE.NLOGO",
          "/CODE EXAMPLES/MOUSE EXAMPLE.NLOGO"),
        Some("it uses the palette extension") -> List(
          "/CODE EXAMPLES/EXTENSIONS EXAMPLES/PALETTE/"),
        Some("it uses the nw extension") -> List(
          "/CODE EXAMPLES/EXTENSIONS EXAMPLES/NW/"),
        Some("it uses the vid extension") -> List(
          "/CODE EXAMPLES/EXTENSIONS EXAMPLES/VID/"),
        Some("it uses the view2.5d extension") -> List(
          "/CODE EXAMPLES/EXTENSIONS EXAMPLES/VIEW2.5D/"),
        Some("it uses the gogo extension") -> List(
          "/CODE EXAMPLES/EXTENSIONS EXAMPLES/GOGO/"),
        Some("it uses the arduino extension") -> List(
          "/IABM TEXTBOOK/CHAPTER 8/ARDUINO EXAMPLE.NLOGO"),
        Some("it uses the gis extension") -> List(
          "/CODE EXAMPLES/EXTENSIONS EXAMPLES/GIS/",
          "/IABM TEXTBOOK/CHAPTER 8/TICKET SALES.NLOGO"
          ),
        Some("it uses the nw extension") -> List(
          "/IABM TEXTBOOK/CHAPTER 8/SIMPLE VIRAL MARKETING.NLOGO"))
      slice <- slices
      if path.toUpperCase.containsSlice(slice)
    } yield {
      for (msg <- message) println("SKIPPING MODEL: " + path + "  because " + msg)
    }).isEmpty

    def update(paths: List[String]) {
      val m = load()
      paths.foreach(updateOne(m, _))
      write(m, Path)
    }
    def updateOne(m: ChecksumMap, model: String) {
      val workspace = HeadlessWorkspace.newInstance
      workspace.silent = true
      try {
        if(!new java.io.File(model).exists && m.contains(model)) {
          // if the model doesn't exist and it's in the checksum file just remove it. if it's not in
          // the checksum file let it fall through and report the error
          m.remove(model)
          println("Model does not exist, deleting checksum for: " + model)
        }
        else {
          workspace.open(model)
          updateOneHelper(m, model, workspace)
        }
      }
      catch { case e: Exception =>
                println("SKIPPING MODEL: " + model + "\n  because of exception:")
                e.printStackTrace() }
      finally { workspace.dispose() }
    }
    def updateOneHelper(m: ChecksumMap, model: String, workspace: HeadlessWorkspace) {
      Checksummer.initModelForChecksumming(workspace)
      val newCheckSum = Checksummer.calculateWorldChecksum(workspace)
      val newGraphicsChecksum = Checksummer.calculateGraphicsChecksum(workspace)
      val revision = getRevisionNumber(workspace.getModelPath)
      val oldEntry = m.get(model)
      val newEntry = Entry(model, newCheckSum, newGraphicsChecksum, revision)
      // figure out if the entry is new, changed, or the same
      val action =
        if(!m.contains(model)) "* Added"
        else if(oldEntry.get == newEntry) "Didn't change"
        else if(oldEntry.get.equalsExceptRevision(newEntry)) "* Changed rev # only"
        else "* Changed"
      m.put(model, newEntry)
      if(action != "Didn't change")
        println(action + ": \"" + model + separator + newCheckSum
                + separator + newGraphicsChecksum + separator + revision + "\"")
    }
    def load(): ChecksumMap = {
      val m = new ChecksumMap
      for(line <- io.Source.fromFile(Path).getLines.map(_.trim))
        if(!line.startsWith("#") && !line.isEmpty) {
          val strs = line.split(java.util.regex.Pattern.quote(separator))
          if(strs.size != 4)
            throw new IllegalStateException("bad line: " + line)
          m.put(strs(0), Entry(strs(0), strs(1), strs(2), strs(3)))
        }
      m
    }

    def write(m: ChecksumMap, path: String) {
      val fw = new java.io.FileWriter(path)
      m.values.foreach(entry => fw.write(entry.toString + '\n'))
      fw.close()
    }

    def getRevisionNumber(modelPath: String): String = {
      val cmds = Array("git", "log", "--pretty=format:%H",
                       new java.io.File(modelPath).getAbsolutePath)
      val stdInput = new java.io.BufferedReader(
        new java.io.InputStreamReader(
          Runtime.getRuntime().exec(cmds,
                                    Array[String](),
                                    new java.io.File("models"))
          .getInputStream))
      // rather than use %h, we take the first 10 of %H. Git changed things making %h different
      // across versions (see https://github.com/git/git/commit/e6c587c733b4634030b353f4024794b08bc86892)
      Option(stdInput.readLine()).map(_.trim.take(10)).getOrElse(throw new Exception(s"Filepath $modelPath not found"))
    }
  }

  // For when you need to know what the checksummed world exports are
  object ChecksumExports {
    import scala.collection.JavaConverters._

    def export(paths: List[String]): Unit = {
      paths.foreach(exportOne)
    }

    def exportOne(path: String): Unit = {
      val workspace = HeadlessWorkspace.newInstance
      try {
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
      } catch { case e: Exception =>
        println("SKIPPING MODEL: " + path + "\n  because of exception:")
        e.printStackTrace()
      }
      finally { workspace.dispose() }
    }
  }
}

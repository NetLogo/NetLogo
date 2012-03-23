// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version
import org.nlogo.workspace.ModelsLibrary

object ChecksumsAndPreviews {

  val allBenchmarks =
    List("Ants", "Bureaucrats", "BZ", "CA1D", "Erosion", "Fire", "FireBig", "Flocking", "GasLabCirc", 
         "GasLabNew", "GasLabOld", "GridWalk", "Heatbugs", "Ising", "Life", "PrefAttach", 
         "Team", "Termites", "VirusNet", "Wealth", "Wolf", "ImportWorld")

  def main(argv: Array[String]) {
    Main.setHeadlessProperty()
    def paths(fn: String => Boolean, includeBenchmarks: Boolean) = {
      val benchmarks = allBenchmarks.map("models/test/benchmarks/" + _ + " Benchmark.nlogo")
      val library =
        ModelsLibrary.getModelPaths(true)
          .filter(fn)
          .map(p => p.substring(p.indexOf("models/")))
          .toList
      if (includeBenchmarks)
        benchmarks ::: library
      else
        library
    }
    // The option names correspond to target names in the Makefile - ST 2/12/09
    argv match {
      case Array("--checksum", path) =>
        Checksums.update(List(path))
      case Array("--checksums") =>
        Checksums.update(paths(Checksums.okPath, includeBenchmarks = !Version.is3D))
      case Array("--preview", path) =>
        Previews.remake(path)
      case Array("--previews") =>
        paths(Previews.okPath, false).foreach(Previews.remake)
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
      try {
        // we set the random seed before opening the model, so that the random-seed will affect the
        // startup procedure if any - ST 7/12/06
        workspace.command("random-seed 0")
        workspace.open(path)
        if(workspace.previewCommands.containsSlice("need-to-manually-make-preview-for-this-model"))
          println("skipping: " + path)
        else {
          println("making preview for: " + path)
          workspace.command(workspace.previewCommands)
          workspace.exportView(previewPath, "PNG")
        }
      }
      catch { case e: Exception => e.printStackTrace() }
      finally { workspace.dispose() }
    }
  }
  
  /// checksums

  object Checksums {
    case class Entry(path: String, worldSum: String, graphicsSum: String, revision: String) {
      def equalsExceptRevision(other: Entry) =
        path == other.path && worldSum == other.worldSum && graphicsSum == other.graphicsSum
      override def toString = List(path, worldSum, graphicsSum, revision).mkString(" - ")
    }
    type ChecksumMap = collection.mutable.LinkedHashMap[String, Entry]
    def okPath(path: String) =
      !List("HUBNET", "/CURRICULAR MODELS/")
            .exists(path.toUpperCase.containsSlice(_)) &&
        (if(List("/GAMES/FROGGER.NLOGO", "/ART/SOUND MACHINES.NLOGO", "/CODE EXAMPLES/SOUND/")
           .exists(path.toUpperCase.containsSlice(_))) {
          println("SKIPPING MODEL: " +  path)
          println("  because it uses the sound extension")
          false
        }
        else true)
    def update(paths: List[String]) {
      val path = if(Version.is3D) "models/test/checksums3d.txt"
                 else "models/test/checksums.txt"
      val m = load(path)
      paths.foreach(updateOne(m, _))
      write(m, path)
    }
    def updateOne(m: ChecksumMap, model: String) {
      val workspace = HeadlessWorkspace.newInstance
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
        println(action + ": \"" + model + " - " + newCheckSum 
                + " - " + newGraphicsChecksum + " - " + revision + "\"")
    }
    def load(path: String): ChecksumMap = {
      val m = new ChecksumMap
      for(line <- io.Source.fromFile(path).getLines.map(_.trim))
        if(!line.startsWith("#") && !line.isEmpty) {
          val strs = line.split(" - ")
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
    // This code gets fidgetty with Windows.  Alter with care! --JAB
    def getRevisionNumber(modelPath: String): String = {
      val modelFolderName = "models"
      val builder = new ProcessBuilder("git", "log", "--pretty=format:%h", modelPath drop ((modelFolderName + '/').size))
      builder.directory(new java.io.File(modelFolderName))
      val stdInput = new java.io.BufferedReader(new java.io.InputStreamReader(builder.start().getInputStream))
      stdInput.readLine().trim
    }
  }
}

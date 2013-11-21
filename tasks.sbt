///
/// nogen
///

val nogen = taskKey[Unit]("disable bytecode generator")

nogen := {
  System.setProperty("org.nlogo.noGenerator", "true")
}

///
/// extensions
///

val extensions = taskKey[Seq[File]]("builds extensions")

extensions := {
  "git submodule --quiet update --init" ! streams.value.log
  val isDirectory = new java.io.FileFilter {
    override def accept(f: File) = f.isDirectory
  }
  val dirs = IO.listFiles(isDirectory)(baseDirectory.value / "extensions")
  for(dir <- dirs.toSeq)
  yield buildExtension(dir, scalaInstance.value.libraryJar, streams.value.log)
}

def buildExtension(dir: File, scalaLibrary: File, log: Logger): File = {
  log.info("extension: " + dir.getName)
  val jar = dir / (dir.getName + ".jar")
  val exitCode =
    if((dir / "build.sbt").exists)
      Process(Seq("./sbt", "package"), dir,
              "SCALA_JAR" -> scalaLibrary.getPath) ! log
    else
      Process(Seq("make", "-s", jar.getName), dir,
              "SCALA_JAR" -> scalaLibrary.getPath) ! log
  assert(exitCode == 0, "extension build failed, exitCode = " + exitCode)
  jar
}

///
/// checksums and previews
///

val csap = "org.nlogo.headless.ChecksumsAndPreviews"

val checksum = inputKey[Unit]("update one model checksum")

fullRunInputTask(checksum, Compile, csap, "--checksum")

val allChecksums = inputKey[Unit]("update all model checksums")

fullRunInputTask(allChecksums, Compile, csap, "--checksums")

val preview = inputKey[Unit]("update one model preview image")

fullRunInputTask(preview,   Compile, csap, "--preview")

val allPreviews = inputKey[Unit]("update all model preview images")

fullRunInputTask(allPreviews, Compile, csap, "--previews")

///
/// Classycle
///

val classycle = taskKey[File]("run Classycle and display a dependency report")

classycle := {
  val _ = (compile in Compile).value  // run it, ignore result
  "mkdir -p target/classycle".!
  "cp -f project/classycle/reportXMLtoHTML.xsl target/classycle".!
  "rm -rf target/classycle/images".!
  "cp -rp project/classycle/images target/classycle/images".!
  _root_.classycle.Analyser.main(
    Array(
      "-xmlFile=target/classycle/classycle.xml",
      "-mergeInnerClasses",
      (classDirectory in Compile).value.getAbsolutePath.toString))
  "open -a Safari target/classycle/classycle.xml".!
  baseDirectory.value / "target" / "classycle" / "classycle.xml"
}

///
/// dump
///

// e.g. dump "models/Sample\ Models/Earth\ Science/Fire.nlogo"
// e.g. dump Fire   (to dump a benchmark model)
// e.g. dump bench  (to replace all of the benchmark model dumps in test/bench)
// e.g. dump all    (to dump all models to target/dumps)

val dumper = InputKey[Unit]("dump", "dump compiled models")

fullRunInputTask(dumper, Test, "org.nlogo.headless.misc.Dump")

///
/// all
///

val all = taskKey[Unit]("build all the things!!!")

all := { val _ = (
  (packageBin in Compile).value,
  (packageBin in Test).value,
  (compile in Test).value,
  extensions.value
)}

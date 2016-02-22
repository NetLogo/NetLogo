///
/// checksums and previews
///

// These have a suffix to avoid conflict with the names in project/ChecksumsAndPreviews
val csap = "org.nlogo.headless.ChecksumsAndPreviews"

val checksumHL = inputKey[Unit]("update one model checksum")

fullRunInputTask(checksumHL, Compile, csap, "--checksum")

val allChecksumsHL = inputKey[Unit]("update all model checksums")

fullRunInputTask(allChecksumsHL, Compile, csap, "--checksums")

val previewHL = inputKey[Unit]("update one model preview image")

fullRunInputTask(previewHL,   Compile, csap, "--preview")

val allPreviewsHL = inputKey[Unit]("update all model preview images")

fullRunInputTask(allPreviewsHL, Compile, csap, "--previews")

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


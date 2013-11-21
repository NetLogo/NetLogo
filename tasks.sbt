///
/// all
///

val all = taskKey[Unit]("build all the things!!!")

all := { val _ = (
  (packageBin in Compile).value,
  (packageBin in Test).value,
  (compile in Test).value,
  Extensions.extensions.value
)}

///
/// nogen
///

val nogen = taskKey[Unit]("disable bytecode generator")

nogen := {
  System.setProperty("org.nlogo.noGenerator", "true")
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

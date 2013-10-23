import sbt._
import Keys._

object ChecksumsAndPreviews {

  // sbt already has a command called "checksums", so we prepend "all-" - ST 6/23/12

  val checksum = inputKey[Option[String]]("update one model checksum")
  val allChecksums = inputKey[Option[String]]("update all model checksums")
  val preview = inputKey[Option[String]]("update one model preview image")
  val allPreviews = inputKey[Option[String]]("update all model preview images")

  val settings: Seq[Setting[InputTask[Option[String]]]] =
    Seq(
      checksum := makeTask("--checksum").evaluated,
      allChecksums := makeTask("--checksums").evaluated,
      preview := makeTask("--preview").evaluated,
      allPreviews := makeTask("--previews").evaluated
    )

  private def makeTask(flag: String): Def.Initialize[InputTask[Option[String]]] =
    Def.inputTask {
      val args: Seq[String] = Def.spaceDelimited("<args>").parsed
      val cp = (fullClasspath in Compile).value
      Run.run("org.nlogo.headless.misc.ChecksumsAndPreviews",
        cp.map(_.data), flag +: args, streams.value.log)(runner.value)
    }

}

import sbt._
import Def.spaceDelimited
import Keys._

object ChecksumsAndPreviews {

  // sbt already has a command called "checksums", so we prepend "all-" - ST 6/23/12

  lazy val checksum = InputKey[Option[String]]("checksum", "update one model checksum")
  lazy val allChecksums = InputKey[Option[String]]("all-checksums", "update all model checksums")
  lazy val preview = InputKey[Option[String]]("preview", "update one model preview image")
  lazy val allPreviews = InputKey[Option[String]]("all-previews", "update all model preview images")

  val settings = Seq(
    checksum <<= makeTask("--checksum"),
    allChecksums <<= makeTask("--checksums"),
    preview <<= makeTask("--preview"),
    allPreviews <<= makeTask("--previews")
  )

  private def makeTask(flag: String) =
    Def.inputTask {
      val args = spaceDelimited("").parsed
      Run.run("org.nlogo.headless.ChecksumsAndPreviews",
        (fullClasspath in Compile).value.map(_.data), flag +: args, streams.value.log)(runner.value)
    }.dependsOn(compile in Compile)

}

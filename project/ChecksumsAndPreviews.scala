import sbt._
import Def.spaceDelimited
import Keys._
import Extensions.extensions
import scala.util.Try

object ChecksumsAndPreviews {

  // sbt already has a command called "checksums", so we prepend "all-" - ST 6/23/12

  lazy val checksum = InputKey[Try[Unit]]("checksum", "update one model checksum")
  lazy val allChecksums = InputKey[Try[Unit]]("all-checksums", "update all model checksums")
  lazy val preview = InputKey[Try[Unit]]("preview", "update one model preview image")
  lazy val allPreviews = InputKey[Try[Unit]]("all-previews", "update all model preview images")
  lazy val checksumExport = InputKey[Try[Unit]]("checksumExport", "run export on preview commands for one model")
  lazy val allChecksumsExport = InputKey[Try[Unit]]("allChecksumsExport", "run export on preview commands for all models")
  lazy val allRevisions = InputKey[Try[Unit]]("all-revisions", "update all model revision numbers")

  val settings = Seq(
    Def.setting(checksum, makeTask("--checksum")),
    Def.setting(allChecksums, makeTask("--checksums")),
    Def.setting(preview, makeTask("--preview")),
    Def.setting(allPreviews, makeTask("--previews"))
  )

  private def makeTask(flag: String): Def.Initialize[InputTask[Try[Unit]]] =
    Running.makeMainTask(
      "org.nlogo.headless.ChecksumsAndPreviews",
      Seq(flag),
      workingDirectory = baseDirectory(_.getParentFile))
        .dependsOn(Compile / compile, extensions)

}

import sbt._
import Keys._

object ChecksumsAndPreviews {

  // sbt already has a command called "checksums", so we prepend "all-" - ST 6/23/12

  val checksum = inputKey[Unit]("update one model checksum")
  val allChecksums = inputKey[Unit]("update all model checksums")
  val preview = inputKey[Unit]("update one model preview image")
  val allPreviews = inputKey[Unit]("update all model preview images")

  private val main = "org.nlogo.headless.ChecksumsAndPreviews"

  val settings: Seq[Setting[InputTask[Unit]]] =
    Seq(
      fullRunInputTask(checksum,     Compile, main, "--checksum"  ),
      fullRunInputTask(allChecksums, Compile, main, "--checksums" ),
      fullRunInputTask(preview,      Compile, main, "--preview"   ),
      fullRunInputTask(allPreviews,  Compile, main, "--previews"  )
    )

}

import java.io.File
import sbt._
import Keys._

object Tortoise {

  lazy val tortoiseLibsTask = TaskKey[Seq[File]](
    "tortoise-libs", "download JavaScript libraries for Tortoise")

  lazy val settings = Seq(
    resourceGenerators in Compile <+= tortoiseLibs
  )

  // path handling details are inelegant/repetitive, should be cleaned up - ST 5/30/12
  lazy val tortoiseLibs =
    (resourceManaged, streams) map {
      (resourceDir, s) =>
      val path = resourceDir / "json2.js"
      if (!path.exists) {
        s.log.info("downloading json2.js")
        IO.download(
          new java.net.URL("http://ccl.northwestern.edu/devel/json2-43d7836c.js"),
          path)
      }
      Seq(path)
    }

}

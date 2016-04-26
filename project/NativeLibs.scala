import sbt._
import Keys._

import java.net.URL

import scala.language.postfixOps

// native libraries for JOGL

object NativeLibs {

  val nativeLibs = TaskKey[Seq[File]](
    "native-libs", "download native libraries for JOGL")

  lazy val nativeLibsTask =
    nativeLibs <<= (baseDirectory, streams) map {
      (base, s) =>
        val baseURL = "http://ccl-artifacts.s3.amazonaws.com/"
        val joglNatives = base / "natives"
        val joglTmp = base / "jogl-2.3.2.zip"
        val joglUrl = new URL(baseURL + "jogl-2.3.2.zip")
        IO.createDirectory(joglNatives)
        IO.download(joglUrl, joglTmp)
        IO.unzip(joglTmp, joglNatives)
        IO.delete(joglTmp)

        // if we need any additional libraries
        // excluded by this, make sure the copyright notice is updated
        (joglNatives ***).get.foreach { f =>
          if (Seq("jocl", "mobile", "newt", "openal", "oal", "jogl_cg", "joal")
            .exists(notwanted => f.getName.contains(notwanted)))
            IO.delete(f)
        }
        (joglNatives ***).get
      }

}

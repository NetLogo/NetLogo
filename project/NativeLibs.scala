import sbt.TaskKey, sbt.Keys._

import sbt.io.{ IO, syntax }, syntax._

import java.io.File
import java.net.URL

import NetLogoBuild.cclArtifacts

import scala.language.postfixOps

// native libraries for JOGL

object NativeLibs {

  val nativeLibs = TaskKey[Seq[File]](
    "native-libs", "download native libraries for JOGL")

  val cocoaLibs = TaskKey[Seq[File]](
    "download native libraries for mac interaction")

  lazy val nativeLibsTask =
    nativeLibs := {
      val joglFile = "jogl-2.4.0-natives.zip"
      val baseURL = "https://s3.amazonaws.com/ccl-artifacts/"
      val joglNatives = baseDirectory.value / "natives"

      if (!joglNatives.exists) {
        val joglTmp = baseDirectory.value / joglFile
        val joglUrl = new URL(baseURL + joglFile)
        IO.createDirectory(joglNatives)
        FileActions.download(joglUrl, joglTmp)
        IO.unzip(joglTmp, joglNatives)
        IO.delete(joglTmp)
      }

      // if we need any additional libraries
      // excluded by this, make sure the copyright notice is updated
      (joglNatives.allPaths).get.foreach { f =>
        if (Seq("jocl", "mobile", "newt", "openal", "oal", "jogl_cg", "joal")
          .exists(notwanted => f.getName.contains(notwanted)))
        IO.delete(f)
      }
      (joglNatives.allPaths).get
    }

  lazy val cocoaLibsTask =
    cocoaLibs := {
      val libDir = baseDirectory.value / "natives" / "macosx-universal"
      IO.createDirectory(libDir / "natives")
      FileActions.download(new java.net.URL(cclArtifacts("libjcocoa.dylib")), libDir / "libjcocoa.dylib")
      Seq(libDir / "libjcocoa.dylib")
    }

}

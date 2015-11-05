import sbt._
import Keys._

import java.net.URL

// native libraries for JOGL and Quaqua

object NativeLibs {

  val nativeLibs = TaskKey[Seq[File]](
    "native-libs", "download native libraries for JOGL and Quaqua")

  lazy val nativeLibsTask =
    nativeLibs <<= (baseDirectory, streams) map {
      (base, s) =>
        val baseURL = "http://ccl-artifacts.s3.amazonaws.com/"
        val joglNatives = base / "natives"
        val joglTmp = base / "jogl-2.3.2.zip"
        val joglUrl = new URL(baseURL + "jogl-2.3.2.zip")
        val quaquas = Seq(
          new URL(baseURL + "libquaqua-7.3.4.jnilib")   -> base / "lib" / "Mac OS X" / "libquaqua.jnilib",
          new URL(baseURL + "libquaqua64-7.3.4.jnilib") -> base / "lib" / "Mac OS X" / "libquaqua64.jnilib")
        IO.createDirectory(base / "lib" / "Mac OS X")
        for ((url, file) <- quaquas) {
          IO.download(url, file)
        }
        IO.createDirectory(joglNatives)
        IO.download(joglUrl, joglTmp)
        IO.unzip(joglTmp, joglNatives)
        IO.delete(joglTmp)
        (joglNatives ***).get ++ quaquas.map(_._2)
      }

}

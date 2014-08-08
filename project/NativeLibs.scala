import sbt._
import Keys._

// native libraries for JOGL and Quaqua

object NativeLibs {

  val nativeLibs = TaskKey[Seq[File]](
    "native-libs", "download native libraries for JOGL and Quaqua")

  lazy val nativeLibsTask =
    nativeLibs <<= (baseDirectory, streams) map { (base, s) =>
      for((path, version) <- pathsAndVersions(base)) yield {
        download(path, version, s.log.info(_))
        path
      }
    }

  /// implementation

  private val urlBase = "http://ccl.northwestern.edu/devel/"
  private val quaquaVersion = "7.3.4"
  private val joglVersion = "2.1.5"

  private def pathsAndVersions(base: File): Seq[(File, String)] = Seq(
    (base / "lib" / "Mac OS X" / "libquaqua.jnilib", quaquaVersion),
    (base / "lib" / "Mac OS X" / "libquaqua64.jnilib", quaquaVersion),
    (base / "lib" / "Mac OS X" / "libjogl.jnilib", joglVersion),
    (base / "lib" / "Mac OS X" / "libjogl_awt.jnilib", joglVersion),
    (base / "lib" / "Mac OS X" / "libgluegen-rt.jnilib", joglVersion),
    (base / "lib" / "Windows" / "jogl.dll", joglVersion),
    (base / "lib" / "Windows" / "jogl_awt.dll", joglVersion),
    (base / "lib" / "Windows" / "gluegen-rt.dll", joglVersion),
    (base / "lib" / "Linux-x86" / "libjogl.so", "x86-" + joglVersion),
    (base / "lib" / "Linux-x86" / "libjogl_awt.so", "x86-" + joglVersion),
    (base / "lib" / "Linux-x86" / "libgluegen-rt.so", "x86-" + joglVersion),
    (base / "lib" / "Linux-amd64" / "libjogl.so", "amd64-" + joglVersion),
    (base / "lib" / "Linux-amd64" / "libjogl_awt.so", "amd64-" + joglVersion),
    (base / "lib" / "Linux-amd64" / "libgluegen-rt.so", "amd64-" + joglVersion),
    (base / "lib" / "Linux-arm" / "libjogl.so", "arm-" + joglVersion),
    (base / "lib" / "Linux-arm" / "libjogl_awt.so", "arm-" + joglVersion),
    (base / "lib" / "Linux-arm" / "libgluegen-rt.so", "arm-" + joglVersion)
  )

  private def download(path: File, version: String, log: String => Unit) {
    IO.createDirectory(path.getParentFile)
    val versionedName = {
      val name = path.getName
      name.patch(name.lastIndexOf('.'), "-" + version, 0)
    }
    if(!path.exists) {
      log("downloading " + versionedName)
      IO.download(
        new java.net.URL(urlBase + versionedName),
        path)
    }
  }

}

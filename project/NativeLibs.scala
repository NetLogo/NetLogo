import sbt._
import Keys._

// native libraries for JOGL and Quaqua

object NativeLibs {

  val nativeLibs = TaskKey[Seq[File]](
    "native-libs", "download native libraries for JOGL and Quaqua")

  lazy val nativeLibsTask =
    nativeLibs <<= (baseDirectory, streams) map {
      (base, s) =>
        val libs_mac = Seq(
          base / "lib" / "Mac OS X" / "libjogl.jnilib",
          base / "lib" / "Mac OS X" / "libjogl_awt.jnilib",
          base / "lib" / "Mac OS X" / "libgluegen-rt.jnilib",
          base / "lib" / "Mac OS X" / "libquaqua.jnilib",
          base / "lib" / "Mac OS X" / "libquaqua64.jnilib")
        val libs_win = Seq(
          base / "lib" / "Windows" / "jogl.dll",
          base / "lib" / "Windows" / "jogl_awt.dll",
          base/ "lib" / "Windows" / "gluegen-rt.dll")
        val libs_x86 = Seq(
          base / "lib" / "Linux-x86" / "libjogl.so",
          base / "lib" / "Linux-x86" / "libjogl_awt.so",
          base / "lib" / "Linux-x86" / "libgluegen-rt.so")
        val libs_amd64 = Seq(
          base / "lib" / "Linux-amd64" / "libjogl.so",
          base / "lib" / "Linux-amd64" / "libjogl_awt.so",
          base /"lib" / "Linux-amd64" / "libgluegen-rt.so")
        val libs_all = libs_mac ++ libs_win ++ libs_x86 ++ libs_amd64
        IO.createDirectory(base / "lib" / "Mac OS X")
        IO.createDirectory(base / "lib" / "Windows")
        IO.createDirectory(base / "lib" / "Linux-amd64")
        IO.createDirectory(base / "lib" / "Linux-x86")
        for(path <- libs_all) {
          val pathString = path.asFile.toString
          val filename =
            pathString.reverse.takeWhile(x => (x != '/') && (x != '\\')).mkString
              .replaceFirst("\\.", (if(pathString.containsSlice("quaqua"))
                                      "-7.3.4."
                                    else if(pathString.containsSlice("Linux-x86"))
                                      "-x86-1.1.1."
                                    else if(pathString.containsSlice("Linux-amd64"))
                                      "-amd64-1.1.1."
                                    else
                                      "-1.1.1.").reverse)
              .reverse
          val url = "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/" + filename
          if(!path.exists) {
            s.log.info("downloading " + path)
            IO.download(new java.net.URL(url), path)
          }
        }
        libs_all
      }

}

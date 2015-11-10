import sbt._

import java.io.File
import java.util.jar.Attributes.Name._

object JavaPackager {
  def jarAttributes(jarDeps: Seq[File]): Package.ManifestAttributes = {
    val distClassPath = jarDeps.map(_.getName).mkString(" ")
    import java.util.jar.Attributes.Name._
    Package.ManifestAttributes(
      "Permissions"                   -> "sandbox",
      "JavaFX-Version"                -> "8.0", // this is required for javapackager to determine the main jar
      "Created-By"                    -> "JavaFX Packager",
      CLASS_PATH.toString             -> distClassPath,
      IMPLEMENTATION_VENDOR.toString  -> "netlogo",
      IMPLEMENTATION_TITLE.toString   -> "NetLogo",
      IMPLEMENTATION_VERSION.toString -> "5.2.2-SNAPSHOT",
      SPECIFICATION_VENDOR.toString   -> "netlogo",
      SPECIFICATION_TITLE.toString    -> "NetLogo",
      SPECIFICATION_VERSION.toString  -> "5.2.2-SNAPSHOT"
    )
  }

  def apply(appClass: String, platform: PlatformBuild, app: SubApplication, srcDir: File, srcFiles: Seq[File], outDir: File, buildDirectory: File) = {
    val srcFileFilter = new FileFilter {
      override def accept(f: File): Boolean = srcFiles.contains(f)
    }
    val args = Seq[String]("javapackager",
      "-deploy", "-verbose",
      "-title",    app.name,
      "-name",     app.name,
      "-outfile",  app.name,
      "-appclass", appClass,
      "-native",   platform.nativeFormat,
      "-outdir",   outDir.getAbsolutePath,
      "-srcdir",   srcDir.getAbsolutePath,
      "-srcfiles", srcDir.listFiles.map(_.getName).mkString(File.pathSeparator),
      "-BmainJar=" + platform.mainJarName,
      "-BappVersion=5.2.2") ++
    (app.jvmOptions ++ platform.jvmOptions).map(s => "-BjvmOptions=" + s) ++
    app.jvmArguments.flatMap(arg => Seq("-argument", arg))

    val ret = Process(args, buildDirectory).!
    if (ret != 0)
      sys.error("packaging failed!")

    outDir / platform.productName
  }
}

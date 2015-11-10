import sbt._

import java.io.File
import java.util.jar.Attributes.Name._

object JavaPackager {
  def jarAttributes: Package.ManifestAttributes = {
    import java.util.jar.Attributes.Name._
    Package.ManifestAttributes(
      "Permissions"                   -> "sandbox",
      "JavaFX-Version"                -> "8.0", // this is required for javapackager to determine the main jar
      "Created-By"                    -> "JavaFX Packager",
      IMPLEMENTATION_VENDOR.toString  -> "org.nlogo",
      IMPLEMENTATION_TITLE.toString   -> "NetLogo",
      SPECIFICATION_VENDOR.toString   -> "org.nlogo",
      SPECIFICATION_TITLE.toString    -> "NetLogo"
    )
  }

  def packageJar(jarFile: File, targetFile: File): Unit = {
    import java.util.jar.Manifest

    val tmpDir = IO.createTemporaryDirectory
    IO.unzip(jarFile, tmpDir)
    val oldManifest = Using.fileInputStream(tmpDir / "META-INF" / "MANIFEST.MF") { is =>
      new Manifest(is)
    }
    IO.delete(tmpDir / "META-INF")
    val manifest = new Manifest()
    JavaPackager.jarAttributes.attributes.foreach {
      case (k, v) => manifest.getMainAttributes.put(k, v)
    }
    manifest.getMainAttributes.put(MAIN_CLASS, oldManifest.getMainAttributes.getValue(MAIN_CLASS))
    manifest.getMainAttributes.put(CLASS_PATH, oldManifest.getMainAttributes.getValue(CLASS_PATH))
    IO.jar(Path.allSubpaths(tmpDir), targetFile, manifest)
    IO.delete(tmpDir)
  }

  def apply(appClass: String, platform: PlatformBuild, app: SubApplication, srcDir: File, srcFiles: Seq[File], outDir: File, buildDirectory: File, mainJar: File) = {
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
      "-BmainJar=" + mainJar.getName,
      "-BappVersion=5.2.2") ++
    (app.jvmOptions ++ platform.jvmOptions).map(s => "-BjvmOptions=" + s) ++
    app.jvmArguments.flatMap(arg => Seq("-argument", arg)) ++
    app.jvmProperties.map(p => "-BjvmProperties=" + p._1 + "=" + p._2)

    val ret = Process(args, buildDirectory).!
    if (ret != 0)
      sys.error("packaging failed!")

    outDir / platform.productName
  }
}

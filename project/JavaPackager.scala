import sbt._

import Keys.{ artifactPath, dependencyClasspath, packageOptions, packageBin }
import java.nio.file.FileSystems
import java.io.File
import java.util.jar.Attributes.Name._

object JavaPackager {
  def mainArtifactSettings: Seq[Setting[_]] =
    Seq(
      packageOptions in (Compile, packageBin) += {
        Package.ManifestAttributes(CLASS_PATH.toString ->
          ((dependencyClasspath in Runtime).value.files :+
            (artifactPath in Compile in packageBin).value)
          .map(_.getName).filter(_.endsWith("jar")).mkString(" "))
      },
      packageOptions in (Compile, packageBin) += jarAttributes
    )

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

  def systemPackagerOptions: Seq[BuildJDK] = {
    if (System.getProperty("os.name").contains("Windows"))
      windowsPackagerOptions
    else if (System.getProperty("os.name").contains("Mac"))
      Seq() // this causes it to use the default
    else
      linuxPackagerOptions
  }

  // maps from a descriptive string to the javapackager path associated
  // with it
  def windowsPackagerOptions: Seq[BuildJDK] = {
    val is64 = System.getenv("PROCESSOR_ARCHITECTURE") == "AMD64"
    val pkgers = windowsJavaPackagers
    pkgers.flatMap { p =>
      val arch = if (is64 && ! p.contains("(x86)")) "64" else "32"
      p.split("\\\\")
        .find(_.contains("jdk"))
        .map(n => SpecifiedJDK(arch, n.drop(3), p))
    }
  }

  def windowsJavaPackagers: Seq[String] = {
    import scala.collection.JavaConversions._
    val fs = FileSystems.getDefault
    fs.getRootDirectories.toSeq.flatMap(r =>
        Seq(fs.getPath(r.toString, "Program Files", "Java"),
          fs.getPath(r.toString, "Program Files (x86)", "Java")))
       .map(_.toFile)
       .filter(f => f.exists && f.isDirectory)
       .flatMap(_.listFiles)
       .filter(_.getName.contains("jdk"))
       .map(_ / "bin" / "javapackager.exe")
       .filter(_.exists)
       .map(_.getAbsolutePath)
  }

  // maps from a descriptive string to the javapackager path associated
  // assumes java installations are named with format
  // jdk<version>-<arch> , and installed in the alternatives
  // system, which is accessible via `update-alternatives`.
  // Additionally, if the jdk name contains '64', it will be labelled
  // as a 64-bit build. YMMV.
  def linuxPackagerOptions: Seq[BuildJDK] = {
    val alternatives = Process("update-alternatives --list javapackager".split(" ")).!!
    val options = alternatives.split("\n").filterNot(_ == "")
    if (options.size < 2)
      Seq(PathSpecifiedJDK)
    else
      for {
        n       <- options
        jdkName <- n.split("/").find(_.contains("jdk"))
      } yield {
        val jdkSplit = jdkName.split('-')
        val arch = if (jdkSplit(1).contains("64")) "64" else "32"
        SpecifiedJDK(arch, jdkSplit(0).drop(3), n, javaHome = Some(n.split('/').dropRight(2).mkString("/")))
      }
  }


  def packageJar(jarFile: File, targetFile: File, mainClass: Option[String]): Unit = {
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
    manifest.getMainAttributes.put(MAIN_CLASS,
      mainClass.getOrElse(oldManifest.getMainAttributes.getValue(MAIN_CLASS)))
    manifest.getMainAttributes.put(CLASS_PATH, oldManifest.getMainAttributes.getValue(CLASS_PATH))
    IO.jar(Path.allSubpaths(tmpDir), targetFile, manifest)
    IO.delete(tmpDir)
  }

  def apply(
    packagerJDK: BuildJDK,
    appClass: String,
    nativeFormat: String,
    platformJvmOptions: Seq[String],
    app: SubApplication,
    srcDir: File,
    srcFiles: Seq[File],
    outDir: File,
    buildDirectory: File,
    mainJar: File,
    appVersion: String,
    jvmOptions: Seq[String] = Seq()) = {
    val srcFileFilter = new FileFilter {
      override def accept(f: File): Boolean = srcFiles.contains(f)
    }
    val args = Seq[String](packagerJDK.javapackager,
      "-deploy", "-verbose",
      "-title",    app.name,
      "-name",     app.name,
      "-outfile",  app.name,
      "-appclass", appClass,
      "-nosign",
      "-native",   nativeFormat,
      "-outdir",   outDir.getAbsolutePath,
      "-srcdir",   srcDir.getAbsolutePath,
      "-srcfiles", srcDir.listFiles.map(_.getName).mkString(File.pathSeparator),
      "-BmainJar=" + mainJar.getName,
      s"-BappVersion=${appVersion}") ++
    (jvmOptions ++ app.jvmOptions ++ platformJvmOptions).map(s => "-BjvmOptions=" + s) ++
    app.jvmArguments.flatMap(arg => Seq("-argument", arg)) ++
    app.jvmProperties.map(p => "-BjvmProperties=" + p._1 + "=" + p._2)

    val envArgs = packagerJDK.javaHome.map(h => Seq("JAVA_HOME" -> h)).getOrElse(Seq())
    val ret = Process(args, buildDirectory, envArgs: _*).!
    if (ret != 0)
      sys.error("packaging failed!")

    (outDir / "bundles").listFiles.head
  }
}

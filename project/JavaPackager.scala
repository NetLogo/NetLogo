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
      val arch = if (is64 && ! p.getAbsolutePath.contains("(x86)")) "64" else "32"
      p.getAbsolutePath
        .split("\\\\") // File.separator doesn't play nice with Regex
        .find(_.contains("jdk"))
        .map(_.drop(3))
        .map(jdkVersion =>
          SpecifiedJDK(arch, jdkVersion, p, javaHome = Some(p.getParentFile.getParentFile.getAbsolutePath)))
    }
  }

  def windowsJavaPackagers: Seq[File] = {
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
  }

  // maps from a descriptive string to the javapackager path associated
  // assumes java installations are named with format
  // jdk<version>-<arch> , and installed in the alternatives
  // system, which is accessible via `update-alternatives`.
  // Additionally, if the jdk name contains '64', it will be labelled
  // as a 64-bit build. YMMV.
  def linuxPackagerOptions: Seq[BuildJDK] = {
    val alternatives =
      try {
        Process("update-alternatives --list javapackager".split(" ")).!!
      } catch {
        case ex: java.lang.RuntimeException => "" // RHEL bugs out with this command, just use path-specified
      }
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
        SpecifiedJDK(arch, jdkSplit(0).drop(3), file(n), javaHome = Some(n.split('/').dropRight(2).mkString("/")))
      }
  }

  def repackageJar(app: SubApplication, mainClass: Option[String], sourceJar: File, outDir: File): File =
    repackageJar(s"${app.jarName}.jar", mainClass, sourceJar, outDir)

  def repackageJar(jarName: String, mainClass: Option[String], sourceJar: File, outDir: File): File = {
    IO.createDirectory(outDir)
    val newJarLocation = outDir / jarName
    packageJar(sourceJar, newJarLocation, mainClass)
    newJarLocation
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

  def generateStubApplication(
    packagerJDK: BuildJDK,
    title: String,
    nativeFormat: String,
    srcDir: File,
    outDir: File,
    buildDirectory: File,
    mainJar: File) = {

    FileActions.copyFile(mainJar, buildDirectory / mainJar.getName)

    val args = Seq[String](packagerJDK.javapackager,
      "-deploy", "-verbose",
      "-title",    title,
      "-name",     title,
      "-outfile",  title,
      "-appclass", "org.nlogo.app.App",
      "-nosign",
      "-native",   nativeFormat,
      "-outdir",   outDir.getAbsolutePath,
      "-srcdir",   srcDir.getAbsolutePath,
      "-BmainJar=" + mainJar.getName)

    val envArgs = packagerJDK.javaHome.map(h => Seq("JAVA_HOME" -> h)).getOrElse(Seq())

    println("running: " + args.mkString(" "))
    val ret = Process(args, buildDirectory, envArgs: _*).!
    if (ret != 0)
      sys.error("packaging failed!")

    (outDir / "bundles").listFiles.head
  }

  /* This function copies the stub application to <specified-directory> / newName.app.
   * It renames the app and the name, the product stub, and the configuration file to the
   * appropriate locations, but doesn't alter their contents.
   * Does not alter the classpath or Info.plist, but deletes the JRE from the copied directory */
  def copyMacStubApplication(
    stubBuildDirectory:          File,
    stubApplicationName:         String,
    newApplicationDirectory:     File,
    newApplicationDirectoryName: String,
    newApplicationName:          String): Unit = {
      val macRoot = stubBuildDirectory / "bundles" / (stubApplicationName + ".app")
      val newRoot = newApplicationDirectory / (newApplicationDirectoryName + ".app")
      FileActions.createDirectories(newRoot)
      FileActions.copyDirectory((macRoot / "Contents").toPath,
        (newRoot / "Contents").toPath,
        p => !p.toString.contains("Java.runtime"))
      FileActions.moveFile(
        newRoot / "Contents" / "MacOS" / stubApplicationName,
        newRoot / "Contents" / "MacOS" / newApplicationName)
      FileActions.moveFile(
        newRoot / "Contents" / "Java" / (stubApplicationName + ".cfg"),
        newRoot / "Contents" / "Java" / (newApplicationName + ".cfg"))
  }

  def copyWinStubApplications(
    stubBuildDirectory:      File,
    stubApplicationName:     String,
    newApplicationDirectory: File,
    subApplicationNames:     Seq[String]): Unit = {
      val winRoot = stubBuildDirectory / "bundles" / stubApplicationName
      FileActions.createDirectories(newApplicationDirectory)
      FileActions.copyDirectory(winRoot, newApplicationDirectory)
      subApplicationNames.foreach { appName =>
        FileActions.copyFile(winRoot / (stubApplicationName + ".exe"), newApplicationDirectory / (appName + ".exe"))
        FileActions.copyFile(winRoot / (stubApplicationName + ".ico"), newApplicationDirectory / (appName + ".ico"))
        FileActions.copyFile(winRoot / "app" / (stubApplicationName + ".cfg"), newApplicationDirectory / "app" / (appName + ".cfg"))
      }
      IO.delete(newApplicationDirectory / (stubApplicationName + ".exe"))
      IO.delete(newApplicationDirectory / (stubApplicationName + ".ico"))
      IO.delete(newApplicationDirectory / "app" / (stubApplicationName + ".cfg"))
  }


  def copyLinuxStubApplications(
    stubBuildDirectory:      File,
    stubApplicationName:     String,
    newApplicationDirectory: File,
    subApplicationNames:     Seq[String]): Unit = {
      val linuxRoot = stubBuildDirectory / "bundles" / stubApplicationName
      FileActions.createDirectories(newApplicationDirectory)
      FileActions.copyDirectory(linuxRoot, newApplicationDirectory)
      subApplicationNames.foreach { appName =>
        val normalizedAppName = appName.replaceAllLiterally(" ", "")
        FileActions.copyFile(linuxRoot / stubApplicationName, newApplicationDirectory / normalizedAppName)
        FileActions.copyFile(linuxRoot / "app" / (stubApplicationName + ".cfg"),
          newApplicationDirectory / "app" / (normalizedAppName + ".cfg"))
      }
      IO.delete(newApplicationDirectory / stubApplicationName)
      IO.delete(newApplicationDirectory / "app" / (stubApplicationName + ".cfg"))
  }
}

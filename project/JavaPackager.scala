import sbt._
import sbt.util.Logger

import Keys.{ artifactPath, dependencyClasspath, packageOptions, packageBin }
import sbt.io.Using
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.io.File
import java.util.jar.Attributes.Name._
import scala.sys.process.Process

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
      "JavaFX-Version"                -> "16", // this was required for javapackager to determine the main jar
                                               // it might not be needed for jpackage AAB April 2022
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

  // expects folder structures like `C:\Program Files\BellSoft\Liberica-17-Full`
  // searches 64 and 32 bit program directories on each drive letter
  def windowsPackagerOptions: Seq[BuildJDK] = {
    val is64 = System.getenv("PROCESSOR_ARCHITECTURE") == "AMD64"
    val jpackageFiles = windowsJavaPackagers
    val specificJdks = jpackageFiles.map { jpackageFile =>
      val jdkRootFile = jpackageFile.getParentFile.getParentFile
      val arch        = if (is64 && ! jpackageFile.getAbsolutePath.contains("(x86)")) "64" else "32"
      val jdkVersion  = s"java${jdkRootFile.getName.split("-")(1)}"
      SpecifiedJDK(arch, jdkVersion, jpackageFile, javaHome = Some(jdkRootFile.getAbsolutePath))
    }

    if (specificJdks.isEmpty) {
      Seq(PathSpecifiedJDK)
    } else {
      specificJdks
    }

  }

  def windowsJavaPackagers: Seq[File] = {
    import scala.collection.JavaConverters._

    val fs = FileSystems.getDefault
    fs.getRootDirectories.asScala.toSeq.flatMap(r =>
      Seq(fs.getPath(r.toString, "Program Files", "BellSoft"),
        fs.getPath(r.toString, "Program Files (x86)", "BellSoft")))
      .map(_.toFile)
      .filter(f => f.exists && f.isDirectory)
      .flatMap(_.listFiles)
      .filter(_.getName.contains("JDK"))
      .map(_ / "bin" / "jpackage.exe")
      .filter(_.exists)
  }

  // assumes java installations are named with format bellsoft-java<version>-full-<arch> ,
  // and installed in the alternatives system, which is accessible via
  // `update-alternatives`.  Additionally, if the jdk name contains '64', it will be
  // labelled as a 64-bit build. YMMV.
  def linuxPackagerOptions: Seq[BuildJDK] = {
    val alternatives =
      try {
        Process("update-alternatives --list javac".split(" ")).!!
      } catch {
        case ex: java.lang.RuntimeException => "" // RHEL bugs out with this command, just use path-specified
      }

    val jpackageFiles = alternatives
      .split("\n")
      .filterNot(_ == "")
      .map( (javacPath) => file(javacPath).getParentFile / "jpackage" )
      .filter(_.exists)

    val specificJdks = jpackageFiles.flatMap( (jpackageFile) => {
      val jdkRootFile = jpackageFile.getParentFile.getParentFile
      val jdkName     = jdkRootFile.getName
      if (jdkName.contains("bellsoft-java")) {
        val jdkVersion = jdkName.split("-")(1)
        val arch       = if (jdkName.contains("64")) "64" else "32"
        Some(SpecifiedJDK(arch, jdkVersion, jpackageFile, javaHome = Some(jdkRootFile.getAbsolutePath.toString)))
      } else {
        None
      }
    })

    if (specificJdks.isEmpty) {
      Seq(PathSpecifiedJDK)
    } else {
      specificJdks
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

// jpackage, which replaces javapackager for Java 17, packages all files and
// directories in the jarDir. Formerly it was the same as the inputDir
// used in NetLogoPackaging.scala.
  def generateStubApplication(
    packagerJDK: BuildJDK,
    title: String,
    nativeFormat: String,
    jarDir: File,
    outDir: File,
    buildDirectory: File,
    mainJar: File) = {
    FileActions.copyFile(mainJar, buildDirectory / mainJar.getName)

    val additionalArgs : Option[Seq[String]] =
    if (System.getProperty("os.name").contains("Mac")) {
      Some(Seq[String]("--java-options", "--add-exports java.desktop/com.apple.laf=ALL-UNNAME"))
    } else {
      None
    }
    val args = Seq[String](packagerJDK.javapackager,
      "--verbose",
      "--name",         title,
      "--main-class",   "org.nlogo.app.App",
      "--type",         "app-image",
      "--input",        jarDir.getAbsolutePath,
      "--dest",         outDir.getAbsolutePath,
      "--main-jar",     mainJar.getName) ++
      additionalArgs.toList.flatten

    val envArgs = packagerJDK.javaHome.map(h => Seq("JAVA_HOME" -> h)).getOrElse(Seq())

    println("running: " + args.mkString(" "))
    val ret = Process(args, buildDirectory, envArgs: _*).!
    if (ret != 0) {
      sys.error("packaging failed!")
    }
    outDir.listFiles.head
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

      val macRoot = stubBuildDirectory / (stubApplicationName + ".app")
      val newRoot = newApplicationDirectory / (newApplicationDirectoryName + ".app")

      // Create the new top level directory structure
      FileActions.createDirectories(newRoot)
      FileActions.copyDirectory((macRoot / "Contents").toPath,
        (newRoot / "Contents").toPath,
        p => !p.toString.contains("runtime"))

      FileActions.moveFile(
        newRoot / "Contents" / "MacOS" / stubApplicationName,
        newRoot / "Contents" / "MacOS" / newApplicationName)
      FileActions.moveFile(
        newRoot / "Contents" / "app" / (stubApplicationName + ".cfg"),
        newRoot / "Contents" / "app" / (newApplicationName + ".cfg"))
  }

  def copyWinStubApplications(
    stubBuildDirectory:      File,
    stubApplicationName:     String,
    newApplicationDirectory: File,
    subApplicationNames:     Seq[String]): Unit = {
      val winRoot = stubBuildDirectory / stubApplicationName

      // Create the new top level directory structure
      FileActions.createDirectories(newApplicationDirectory)
      FileActions.copyDirectory(winRoot, newApplicationDirectory)

      // Copy in each sub application executable and config file
      subApplicationNames.foreach { appName =>
        FileActions.copyFile(winRoot / (stubApplicationName + ".exe"), newApplicationDirectory / (appName + ".exe"))
        FileActions.copyFile(winRoot / (stubApplicationName + ".ico"), newApplicationDirectory / (appName + ".ico"))
        FileActions.copyFile(winRoot / "app" / (stubApplicationName + ".cfg"), newApplicationDirectory / "app" / (appName + ".cfg"))
      }

      // Remove unneeded files
      IO.delete(newApplicationDirectory / (stubApplicationName + ".exe"))
      IO.delete(newApplicationDirectory / (stubApplicationName + ".ico"))
      IO.delete(newApplicationDirectory / "app" / (stubApplicationName + ".cfg"))
  }

  def copyLinuxStubApplications(
    stubBuildDirectory:      File,
    stubApplicationName:     String,
    newApplicationDirectory: File,
    subApplicationNames:     Seq[String]): Unit = {
      val permissions = {
        import PosixFilePermission._
        import scala.collection.JavaConverters._
        Set(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, OTHERS_READ).asJava
      }

      // Create the new top level directory structure
      FileActions.createDirectories(newApplicationDirectory / "bin")

      // Prepare and copy files from the stub directory
      val linuxRoot = stubBuildDirectory / stubApplicationName
      val libapplauncherSO  = linuxRoot / "lib" / "libapplauncher.so"
      Files.setPosixFilePermissions(libapplauncherSO.toPath, permissions)
      FileActions.copyDirectory(linuxRoot / "lib" , newApplicationDirectory / "lib" )

      // Copy in each sub application executable and config file
      subApplicationNames.foreach { appName =>
        val normalizedAppName = appName.replaceAllLiterally(" ", "")
        FileActions.copyFile(linuxRoot / "bin" / stubApplicationName,
          newApplicationDirectory / "bin" / normalizedAppName)
        FileActions.copyFile(linuxRoot / "lib" / "app" / (stubApplicationName + ".cfg"),
          newApplicationDirectory / "lib" / "app" / (normalizedAppName + ".cfg"))
      }

      // Remove unneeded files
      IO.delete(newApplicationDirectory / "lib" / (stubApplicationName + ".png"))
      IO.delete(newApplicationDirectory / "lib" / "app" / (stubApplicationName + ".cfg"))
  }

  def setupAppImageInput(log: Logger, version: String, buildJDK: BuildJDK, buildDir: File, netLogoJar: File, dependencies: Seq[File]) = {
    val inputDir = buildDir / s"input-${buildJDK.version}-${buildJDK.arch}"
    log.info(s"Setting up jpackage input director: $inputDir")
    FileActions.remove(inputDir)
    FileActions.createDirectory(inputDir)

    FileActions.copyFile(netLogoJar, inputDir / s"netlogo-$version.jar")
    val netLogoDeps = dependencies.foreach( (jar) => {
      if (!jar.getName.equals(netLogoJar.getName)) {
        FileActions.copyFile(jar, inputDir / jar.getName)
      }
    })

    inputDir
  }

  val launchers = Set("NetLogo", "NetLogo 3D", "HubNet Client", "Behaviorsearch")

  def generateAppImage(log: Logger, platform: String, version: String, configDir: File, buildDir: File, inputDir: File, destDir: File, extraArgs: Seq[String] = Seq()) = {
    FileActions.remove(destDir)

    val netLogo3dPropsPath      = (configDir / "netlogo-3d-launcher.properties").getAbsolutePath
    val hubNetPropsPath         = (configDir / "hubnet-client-launcher.properties").getAbsolutePath
    val behaviorsearchPropsPath = (configDir / "behaviorsearch-launcher.properties").getAbsolutePath

    val args = Seq[String](
      "jpackage"
    , "--verbose"
    , "--resource-dir", buildDir.getAbsolutePath
    , "--name",         s"NetLogo"
    , "--description",  s"NetLogo $version"
    , "--type",         "app-image"
    , "--main-jar",     s"netlogo-$version.jar"
    , "--main-class",   "org.nlogo.app.App"
    , "--input",        inputDir.getAbsolutePath
    , "--dest",         destDir.getAbsolutePath
    , "--java-options", "-Xmx1024m"
    , "--java-options", "-XX:+UseParallelGC"
    , "--java-options", "-Dfile.encoding=UTF-8"
    , "--java-options", "--add-exports=java.base/java.lang=ALL-UNNAMED"
    , "--java-options", "--add-exports=java.desktop/sun.awt=ALL-UNNAMED"
    , "--java-options", "--add-exports=java.desktop/sun.java2d=ALL-UNNAMED"
    , "--add-launcher", s"""NetLogo 3D=$netLogo3dPropsPath"""
    , "--add-launcher", s"""HubNet Client=$hubNetPropsPath"""
    , "--add-launcher", s"""Behaviorsearch=$behaviorsearchPropsPath"""
    ) ++ extraArgs

    log.info(s"running: ${args.mkString(" ")}")
    val returnValue = Process(args, buildDir).!
    if (returnValue != 0) {
      sys.error("packaging failed!")
    }
    destDir.listFiles.head
  }

  def copyExtraFiles(log: Logger, extraDirs: Seq[BundledDirectory], platform: PlatformBuild, arch: String, appImageDir: File, appDir: File, rootFiles: Seq[File]) = {
    log.info("Copying the extra bundled directories")
    extraDirs.foreach( (dir) => {
      dir.fileMappings.foreach {
        case (f, p) =>
          val targetFile = appDir / p
          if (!targetFile.getParentFile.exists) {
            FileActions.createDirectories(targetFile.getParentFile)
          }
          FileActions.copyFile(f, targetFile)
      }
    })
    ExtenionDir.removeVidNativeLibs(platform.shortName, arch, appDir)

    log.info("Copying the extra root directory files")
    rootFiles.foreach( (f) => {
      FileActions.copyAny(f, appImageDir / f.getName)
    })
  }

  def createScripts(log: Logger, appImageDir: File, configDir: File, appDir: File, platform: PlatformBuild, headlessScript: String, guiScript: String, variables: Map[String, String]) = {
    log.info("Creating GUI/headless run scripts")
    val headlessClasspath =
      ("netlogoJar" ->
        appDir.listFiles
          .filter( f => f.getName.startsWith("netlogo") && f.getName.endsWith(".jar") )
          .map( jar => appImageDir.toPath.relativize(jar.toPath).toString )
          .take(1)
          .mkString(""))

    val platformConfigDir = configDir / platform.shortName

    val headlessFile = appImageDir / headlessScript
    Mustache(
      platformConfigDir / s"$headlessScript.mustache",
      headlessFile,
      variables + headlessClasspath + ("mainClass" -> "org.nlogo.headless.Main")
    )
    headlessFile.setExecutable(true)

    val guiFile = appImageDir / guiScript
    Mustache(
      platformConfigDir / s"$headlessScript.mustache",
      guiFile,
      variables + headlessClasspath + ("mainClass" -> "org.nlogo.app.App")
    )
    guiFile.setExecutable(true)
  }

}

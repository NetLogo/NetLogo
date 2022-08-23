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

  def generateAppImage(log: Logger, platform: String, mainLauncher: Launcher, configDir: File, buildDir: File, inputDir: File, destDir: File, extraJpackageArgs: Seq[String], extraLaunchers: Seq[Launcher]) = {

    val extraLauncherArgs = extraLaunchers.flatMap( (settings) => {
      val inFile  = configDir / s"extra-launcher.properties.mustache"
      val outFile = buildDir / s"${settings.mustachePrefix}.properties"
      Mustache(inFile, outFile, settings.toVariables)
      Seq("--add-launcher", s"${settings.name}=${outFile.getAbsolutePath}")
    })

    val args = Seq[String](
      "jpackage"
    , "--verbose"
    , "--resource-dir", buildDir.getAbsolutePath
    , "--name",         mainLauncher.name
    , "--description",  mainLauncher.description
    , "--type",         "app-image"
    , "--main-jar",     mainLauncher.mainJar
    , "--main-class",   mainLauncher.mainClass
    , "--input",        inputDir.getAbsolutePath
    , "--dest",         destDir.getAbsolutePath
    ) ++ extraLauncherArgs ++ extraJpackageArgs

    log.info(s"running: ${args.mkString(" ")}")
    val returnValue = Process(args, buildDir).!
    if (returnValue != 0) {
      sys.error("packaging failed!")
    }
    destDir.listFiles.head
  }

  def copyExtraFiles(log: Logger, extraDirs: Seq[BundledDirectory], platform: String, arch: String, appImageDir: File, appDir: File, rootFiles: Seq[File]) = {
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

    log.info("Copying the extra root directory files")
    rootFiles.foreach( (f) => {
      FileActions.copyAny(f, appImageDir / f.getName)
    })
  }

  def createScripts(log: Logger, appImageDir: File, configDir: File, appDir: File, platform: String, headlessScript: String, guiScript: String, variables: Map[String, String]) = {
    log.info("Creating GUI/headless run scripts")
    val headlessClasspath =
      ("netlogoJar" ->
        appDir.listFiles
          .filter( f => f.getName.startsWith("netlogo") && f.getName.endsWith(".jar") )
          .map( jar => appImageDir.toPath.relativize(jar.toPath).toString )
          .take(1)
          .mkString(""))

    val platformConfigDir = configDir / platform

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

import sbt._
import sbt.util.Logger

import Keys.{ artifactPath, dependencyClasspath, packageOptions, packageBin }
import sbt.io.Using
import java.nio.file.FileSystems
import java.nio.file.{ Files, Paths }
import java.nio.file.attribute.PosixFilePermission
import java.io.File
import java.util.jar.Attributes.Name._
import scala.sys.process.Process

/*

Oh hello.  We use `jpackage` to create "app images" on macOS, Windows, and Linux.  These
are the base binaries/executables/directories that we take to finalize a distribution
package.

Netlogo bundles 4 apps, NetLogo, NetLogo 3D, HubNet Client, and Behaviorsearch.

For Linux and Windows we use the `--add-launcher` flag to specify the settings for the
extra 3 launchers beyond NetLogo.

Linux has to create some sym links so the binaries and the NetLogo-specific resource dirs
(models, extensions, natives, etc) are easily accessible.  Otherwise, it just makes its
TGZ file and is good to go.

Windows has to do some work to get the icons in the right place to be used, then also to
handle its UUIDs and WiX setup to generate the MSI file.

For macOS it's not as simple as using `--add-launcher`, as it puts the executables inside
the main `NetLogo.app` package where there is no easy way for a user to find and execute
them.  So for macOS we instead do this:

1. Generate each `${launcher.name}.app` separately as a single `generateAppImage()` call.
2. Copy out the Java runtime and app library directory to a new bundle directory.
3. Remove the runtime and app library directories from each app.  Do some work on the
   `${launcher.name}.cfg` file to get it working with the updated paths, and include some
   macOS-specific flags.
4. Code sign the bundled dir files as needed.
5. Build the DMG.
6. Sign the DMG.

macOS also has a little more work to do to get its icon (icns) files in the right places.
A lot (but not all) of the weirdness for macOS is because it uses the `MacApplication`
wrapper to handle actually running the 4 apps, so extra deps need to be copied over and
extra config mashed in to make sure it runs correctly.

The final result of `packageMacAggregate`, `packageLinuxAggregate`, or
`packageWinAggregate` should be a new archive file (dmg, msi, or tgz) in the
`dist/target/downloadPages` directory if everything goes correctly..

Here is the generate workflow for `JavaPackager`.

- Call `setupAppImageInput()` to create the directory that `jpackage` will use as its
  `--input`.
- Call `generateAppImage()` to turn that input into a `--dest` directory.
- Call `copyExtraFiles()` to get the NetLogo-specific resource directories and root files
  needed by NetLogo put in place.
- Call `createScripts()` to create the runner scripts for headless/gui.

Before and after each of those calls any extra prep work can be done.  Most of the work is
usually after the `createScripts()` to finalize the package.

-Jeremy B August 2022

*/

object JavaPackager {
  def mainArtifactSettings: Seq[Setting[_]] =
    Seq(
      Compile / packageBin / packageOptions += {
        Package.ManifestAttributes(CLASS_PATH.toString ->
          ((Runtime / dependencyClasspath).value.files :+
            (Compile / packageBin / artifactPath).value)
          .map(_.getName).filter(_.endsWith("jar")).mkString(" "))
      },
      (Compile / packageBin / packageOptions) += jarAttributes
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
      macPackagerOptions
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

  // looks for OpenJDK at a path like /Library/Java/JavaVirtualMachines/jdk-17*.jdk
  // the architecture can't be specified for the build process, so generate both options and assume developer competency
  def macPackagerOptions: Seq[BuildJDK] = {
    val specificJdks = FileActions.listDirectory(Paths.get("/Library/Java/JavaVirtualMachines")).collect {
      case path if """^jdk-17.*\.jdk$""".r.findFirstIn(path.getFileName.toString).isDefined =>
        val jpackage = (path / "Contents" / "Home" / "bin" / "jpackage").toFile
        val home = Some((path / "Contents" / "Home").toString)

        Seq(SpecifiedJDK("x86_64", "17", jpackage, home), SpecifiedJDK("aarch64", "17", jpackage, home))
    }.flatten

    if (specificJdks.isEmpty) {
      Seq(PathSpecifiedJDK)
    } else {
      specificJdks
    }
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
        // This command may not work on all distros, just use path-specified
        case ex: java.lang.RuntimeException => ""
        case ex: java.io.IOException => ""
      }

    val jpackageFiles = alternatives
      .split("\n")
      .filterNot(_ == "")
      .map( (javacPath) => file(javacPath).getParentFile / "jpackage" )
      .filter(_.exists)

    val specificJdks = jpackageFiles.flatMap( (jpackageFile) => {
      val jdkRootFile = jpackageFile.getParentFile.getParentFile
      val jdkName     = jdkRootFile.getName
      if (jdkName.startsWith("bellsoft-java") || jdkName.startsWith("zulu")) {
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

  def generateAppImage(log: Logger, jpackage: String, platform: String, mainLauncher: Launcher, configDir: File, buildDir: File, inputDir: File, destDir: File, extraJpackageArgs: Seq[String], extraLaunchers: Seq[Launcher]) = {

    val extraLauncherArgs = extraLaunchers.flatMap( (settings) => {
      val inFile  = configDir / s"extra-launcher.properties.mustache"
      val outFile = buildDir / s"${settings.mustachePrefix}.properties"
      Mustache(inFile, outFile, settings.toVariables)
      Seq("--add-launcher", s"${settings.name}=${outFile.getAbsolutePath}")
    })

    val javaOptions = mainLauncher.javaOptions.flatMap( (option) => {
      Seq("--java-options", option)
    })

    val args = Seq[String](
      jpackage
    , "--verbose"
    , "--resource-dir", buildDir.getAbsolutePath
    , "--name",         mainLauncher.name
    , "--description",  mainLauncher.description
    , "--type",         "app-image"
    , "--main-jar",     mainLauncher.mainJar
    , "--main-class",   mainLauncher.mainClass
    , "--input",        inputDir.getAbsolutePath
    , "--dest",         destDir.getAbsolutePath
    ) ++ javaOptions ++ extraLauncherArgs ++ extraJpackageArgs

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

  def createScripts(log: Logger, appImageDir: File, appDir: File, scriptSourceDir: File, headlessScript: String, guiScript: String, variables: Map[String, String]) = {
    log.info("Creating GUI/headless run scripts")
    val headlessClasspath =
      ("netlogoJar" ->
        appDir.listFiles
          .filter( f => f.getName.startsWith("netlogo") && f.getName.endsWith(".jar") )
          .map( jar => appImageDir.toPath.relativize(jar.toPath).toString )
          .take(1)
          .mkString(""))

    val scriptSource = scriptSourceDir / s"$headlessScript.mustache"

    val headlessFile = appImageDir / headlessScript
    Mustache(
      scriptSource,
      headlessFile,
      variables + headlessClasspath + ("mainClass" -> "org.nlogo.headless.Main")
    )
    headlessFile.setExecutable(true)

    val guiFile = appImageDir / guiScript
    Mustache(
      scriptSource,
      guiFile,
      variables + headlessClasspath + ("mainClass" -> "org.nlogo.app.App")
    )
    guiFile.setExecutable(true)
  }

}

import io.circe

import scala.io.Source

import sbt._
import sbt.util.Logger

import Keys.{ artifactPath, dependencyClasspath, packageOptions, packageBin }
import sbt.io.Using
import java.nio.file.FileSystems
import java.nio.file.{ Files, Paths }
import java.nio.file.attribute.PosixFilePermission
import java.io.File
import java.util.jar.Attributes.Name._

import scala.collection.JavaConverters.asJavaIterableConverter
import scala.sys.process.Process

/*

Oh hello.  We use `jpackage` to create "app images" on macOS, Windows, and Linux.  These
are the base binaries/executables/directories that we take to finalize a distribution
package.

Netlogo bundles 4 apps, NetLogo, NetLogo 3D, HubNet Client, and BehaviorSearch.

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
  def mainArtifactSettings: Seq[Setting[?]] =
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

  def jdks: Seq[JDK] = {

    import circe.generic.auto._

    val text = Source.fromFile(".jdks.yaml").mkString

    circe.yaml.parser.parse(text).flatMap(_.as[Seq[JDK]]).fold(
      (e) => Seq()
    , identity
    )

  }

  def setupAppImageInput(log: Logger, version: String, buildJDK: JDK, buildDir: File, netLogoJar: File, dependencies: Seq[File]) = {
    val inputDir = buildDir / s"input-${buildJDK.majorVersion}-${buildJDK.architecture}"
    log.info(s"Setting up jpackage input directory: $inputDir")
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

  def generateAppImage(log: Logger, jpackage: File, platform: String, mainLauncher: Launcher, configDir: File, buildDir: File, inputDir: File, destDir: File, extraLaunchers: Seq[Launcher]) = {

    val extraLauncherArgs = extraLaunchers.flatMap { launcher =>
      val inFile  = configDir / s"extra-launcher.properties.mustache"
      val outFile = buildDir / s"${launcher.mustachePrefix}.properties"

      Mustache(inFile, outFile, Map(
        "javaOptions" -> s"java-options=${launcher.javaOptions.map(opt => s""""$opt"""").mkString(" \\\n  ")}",
        "extraProperties" -> launcher.extraProperties.asJava,
        "mainJar" -> launcher.mainJar,
        "mainClass" -> launcher.mainClass,
        "icon" -> s"icon=${launcher.icon}"
      ))

      Seq("--add-launcher", s"${launcher.name}=${outFile.getAbsolutePath}")
    }

    val javaOptions = mainLauncher.javaOptions.flatMap( (option) => {
      Seq("--java-options", option)
    })

    val jpVersion = Process(Seq[String](jpackage.getAbsolutePath, "--version")).!!
    log.info(s"Using jpackage version: ${jpVersion}")

    val args = Seq[String](
      jpackage.getAbsolutePath
    , "--verbose"
    , "--resource-dir", buildDir.getAbsolutePath
    , "--name",         mainLauncher.name
    , "--description",  mainLauncher.description
    , "--type",         "app-image"
    , "--main-jar",     mainLauncher.mainJar
    , "--main-class",   mainLauncher.mainClass
    , "--input",        inputDir.getAbsolutePath
    , "--dest",         destDir.getAbsolutePath
    , "--icon",         mainLauncher.icon
    ) ++ javaOptions ++ extraLauncherArgs

    log.info(s"running: ${args.mkString(" ")}")
    val returnValue = Process(args, buildDir).!
    if (returnValue != 0) {
      sys.error("packaging failed!")
    }

    // required for subprocesses like the GoGo daemon to use the bundled Java
    log.info("Copying Java executable into runtime")

    val os = System.getProperty("os.name").toLowerCase

    val javaExec: File = {
      if (os.startsWith("win")) {
        jpackage.getParentFile / "java.exe"
      } else {
        jpackage.getParentFile / "java"
      }
    }

    val javaDest: File = {
      if (os.startsWith("linux")) {
        destDir / "NetLogo" / "lib" / "runtime" / "bin" / javaExec.getName
      } else {
        destDir / "NetLogo" / "runtime" / "bin" / javaExec.getName
      }
    }

    javaDest.getParentFile.mkdirs()

    FileActions.copyFile(javaExec, javaDest)

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

  def createScripts(log: Logger, appImageDir: File, appDir: File, scriptSourceDir: File, headlessScript: String,
                    guiScript: String, javaOptions: Seq[String] = Seq()) = {
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
      Map("javaOptions" -> javaOptions.mkString(" "), headlessClasspath, "mainClass" -> "org.nlogo.headless.Main")
    )
    headlessFile.setExecutable(true)

    val guiFile = appImageDir / guiScript
    Mustache(
      scriptSource,
      guiFile,
      Map("javaOptions" -> javaOptions.mkString(" "), headlessClasspath, "mainClass" -> "org.nlogo.app.App")
    )
    guiFile.setExecutable(true)
  }

}

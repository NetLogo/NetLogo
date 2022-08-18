import sbt._
import NetLogoPackaging.RunProcess
import java.nio.file.{ Files, Paths }
import java.nio.file.attribute.PosixFilePermission
import java.io.File

object PackageLinuxAggregate {
  // We're given a dummy package with a directory structure like:
  // dummy
  // └── dummy
  //     ├── bin
  //     │   └── dummy
  //     └── lib
  //         ├── app
  //         │   ├── dummy.cfg
  //         │   └── netlogo-6.2.2.jar
  //         ├── dummy.png
  //         ├── libapplauncher.so
  //         └── runtime
  //             ├── conf
  //             ├── legal
  //             ├── lib
  //             └── release
  //
  //  The desire is to create each sub-application, and copy in auxilliary files
  // The resulting structure is
  // └── NetLogo 6.x.x
  //     ├── Behaviorsearch -> bin/Behaviorsearch
  //     ├── HubNet Client  -> bin/HubNet Client
  //     ├── NetLogo        -> bin/NetLogo
  //     ├── NetLogo 3D     -> bin/NetLogo 3D
  //     ├── netlogo-gui.sh
  //     ├── netlogo-headless.sh
  //     ├── NetLogo User Manual.pdf
  //     ├── readme.md
  //     ├── bin
  //     │   ├── Behaviorsearch
  //     │   ├── HubNetClient
  //     │   ├── NetLogo
  //     │   └── NetLogo3D
  //     ├── lib
  //     │   ├── app
  //     │   │   ├── Behaviorsearch.cfg
  //     │   │   ├── HubNetClient.cfg
  //     │   │   ├── NetLogo3D.cfg
  //     │   │   ├── NetLogo.cfg
  //     │   │   ├── behaviorsearch
  //     │   │   ├── docs
  //     │   │   ├── extensions
  //     │   │   ├── models
  //     │   │   ├── natives
  //     │   │   ├── args4j-2.0.12.jar
  //                 ...
  //     │   │   ├── netlogo-6.2.2.jar
  //                 ...
  //     │   │   └── zip4j-2.9.0.jar
  //     │   ├── dummy.png
  //     │   ├── libapplauncher.so
  //     │   └── runtime
  //     │       ├── conf
  //     │       ├── legal
  //     │       ├── lib
  //     │       └── release
  //     ├── Mathematica Link
  //     │   ├── mathematica-link.jar
  //     │   ├── NetLogo.m
  //     │   ├── NetLogo-Mathematica Tutorial.nb
  //     └── └── NetLogo-Mathematica Tutorial.pdf
  //
  // note: this shares a lot of code with the windows aggregate packager.
  // There may be an opportunity for abstraction.

  def apply(
    log: sbt.util.Logger
  , version: String
  , arch: String
  , configDir: File
  , appImageDir: File
  , webDir: File
  , extraDirs: Seq[BundledDirectory]
  , launchers: Set[String]
  , rootFiles: Seq[File]
  , variables: Map[String, String]
  ): File = {
    log.info("Adding launcher sym links")
    launchers.foreach( (launcher) => {
      FileActions.createRelativeSoftLink(
        appImageDir / s"$launcher $version",
        appImageDir / "bin" / s"$launcher $version"
      )
    })
    log.info("Copying the extra bundled directories")
    extraDirs.foreach( (dir) => {
      dir.fileMappings.foreach {
        case (f, p) =>
          val targetFile = appImageDir / "bin" / p
          if (!targetFile.getParentFile.exists) {
            FileActions.createDirectories(targetFile.getParentFile)
          }
          FileActions.copyFile(f, targetFile)
      }
      val dirName = dir.sourceDir.getName
      FileActions.createRelativeSoftLink(
        appImageDir / dirName
      , appImageDir / "bin" / dirName
      )
    })
    ExtenionDir.removeVidNativeLibs("linux", arch, appImageDir / "bin")

    log.info("Copying the extra root directory files")
    rootFiles.foreach( (f) => {
      FileActions.copyAny(f, appImageDir / f.getName)
    })

    log.info("Creating GUI/headless run scripts")
    val appDir = appImageDir / "lib" / "app"
    val headlessClasspath =
      ("classpathJars" ->
        appDir.listFiles
          .filter( f => f.getName.endsWith(".jar") )
          .map( jar => Paths.get("lib", "app", jar.getName).toString )
          .sorted
          .mkString(File.pathSeparator))

    val permissions = {
      import PosixFilePermission._
      import scala.collection.JavaConverters._
      Set(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE).asJava
    }

    val headlessFile = appImageDir / "netlogo-headless.sh"
    Mustache(
      configDir / "linux" / "netlogo-headless.sh.mustache",
      headlessFile,
      variables + headlessClasspath + ("mainClass" -> "org.nlogo.headless.Main")
    )
    Files.setPosixFilePermissions(headlessFile.toPath, permissions)
    headlessFile.setExecutable(true)

    val guiFile = appImageDir / "netlogo-gui.sh"
    Mustache(
      configDir / "linux" / "netlogo-headless.sh.mustache",
      guiFile,
      variables + headlessClasspath + ("mainClass" -> "org.nlogo.app.App")
    )
    Files.setPosixFilePermissions(guiFile.toPath, permissions)
    guiFile.setExecutable(true)

    log.info("Creating the compressed archive file")
    val archiveName  = s"NetLogo-$version-$arch.tgz"
    val tarBuildDir  = appImageDir.getParentFile
    val tarBuildFile = tarBuildDir / archiveName
    val archiveFile  = webDir / archiveName
    IO.delete(tarBuildFile)
    IO.delete(archiveFile)

    RunProcess(Seq("tar", "-zcf", archiveName, appImageDir.getName), tarBuildDir, "tar linux aggregate")

    FileActions.createDirectory(webDir)
    FileActions.moveFile(tarBuildFile, archiveFile)

    archiveFile
  }
}

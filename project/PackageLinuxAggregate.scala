import sbt._
import NetLogoPackaging.RunProcess
import java.nio.file.{ Files, Paths }
import java.nio.file.attribute.PosixFilePermission
import java.io.File

import scala.collection.JavaConverters._

object PackageLinuxAggregate {
  def apply(
    log: sbt.util.Logger
  , version: String
  , arch: String
  , configDir: File
  , appImageDir: File
  , webDir: File
  , extraDirs: Seq[BundledDirectory]
  , launchers: Seq[Launcher]
  ): File = {

    val shellScriptPermissions = {
      import PosixFilePermission._
      import scala.collection.JavaConverters.setAsJavaSetConverter
      Set(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE).asJava
    }

    // these could be simple symlinks, but there is an issue with `file` that affects Ubuntu and other common distros.
    // The `jpackage`-produced executables are detected as `ELF 64-bit LSB shared object` libraries (which is correct
    // according to the specs), but file reports them as non-executable libraries, so they cannot be launched from the
    // GUIs that use that info to determine file associations, like Nautilus.  So we have to do this, which at least
    // lets users get the things running from the GUI, albeit possibly with a warning or question about running a text
    // file.  With javapackager the bins produced were `LSB executables` so didn't have this issue.
    // https://bugs.launchpad.net/ubuntu/+source/file/+bug/1747711 -Jeremy B September 2022
    log.info("Adding launcher shell scripts")
    launchers.foreach( (launcher) => {
      val shellScriptLauncher = appImageDir / launcher.name
      Mustache(
        configDir / "linux" / "shell-launcher.sh.mustache",
        shellScriptLauncher,
        Map("bin" -> launcher.name.replace(" ", "\\ "))
      )
      shellScriptLauncher.setExecutable(true)
      Files.setPosixFilePermissions(shellScriptLauncher.toPath, shellScriptPermissions)
    })

    log.info("Generating install script")

    Mustache(configDir / "linux" / "install.sh.mustache", appImageDir / "install.sh", Map(
      "version" -> s"NetLogo-$version",
      "launchers" -> launchers.map { launcher =>
        (Map("name" -> launcher.name, "display-name" -> s"${launcher.name} $version",
             "path-name" -> s"${launcher.name}-$version") ++ launcher.icon.map("icon" -> _)).asJava
      }.asJava
    ))

    Files.setPosixFilePermissions((appImageDir / "install.sh").toPath, shellScriptPermissions)

    log.info("Creating NetLogo_Console sym link")
    FileActions.createRelativeSoftLink(appImageDir / "NetLogo_Console", appImageDir / "bin" / "NetLogo")

    log.info("Setting headless/gui script posix permissions")
    val headlessFile = appImageDir / "netlogo-headless.sh"
    Files.setPosixFilePermissions(headlessFile.toPath, shellScriptPermissions)
    val guiFile = appImageDir / "netlogo-gui.sh"
    Files.setPosixFilePermissions(guiFile.toPath, shellScriptPermissions)

    log.info("Rename app image directory to include version")
    val versionedAppImageDir = appImageDir.getParentFile / s"${appImageDir.getName} $version"
    Files.move(appImageDir.toPath, versionedAppImageDir.toPath)

    log.info("Creating the compressed archive file")
    val archiveName  = s"NetLogo-$version-$arch.tgz"
    val tarBuildDir  = appImageDir.getParentFile
    val tarBuildFile = tarBuildDir / archiveName
    val archiveFile  = webDir / archiveName
    IO.delete(tarBuildFile)
    IO.delete(archiveFile)

    RunProcess(Seq("tar", "-zcf", archiveName, versionedAppImageDir.getName), tarBuildDir, "tar linux aggregate")

    log.info("Moving tgz file to final location.")
    FileActions.createDirectory(webDir)
    FileActions.moveFile(tarBuildFile, archiveFile)

    archiveFile
  }
}

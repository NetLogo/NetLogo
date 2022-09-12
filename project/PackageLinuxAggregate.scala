import sbt._
import NetLogoPackaging.RunProcess
import java.nio.file.{ Files, Paths }
import java.nio.file.attribute.PosixFilePermission
import java.io.File

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

    log.info("Adding launcher sym links")
    launchers.foreach( (launcher) => {
      FileActions.createRelativeSoftLink(
        appImageDir / launcher.name,
        appImageDir / "bin" / launcher.name
      )
    })

    log.info("Creating NetLogo_Console sym link")
    FileActions.createRelativeSoftLink(appImageDir / "NetLogo_Console", appImageDir / "bin" / "NetLogo")

    log.info("Setting headless/gui script posix permissions")
    val permissions = {
      import PosixFilePermission._
      import scala.collection.JavaConverters._
      Set(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE).asJava
    }
    val headlessFile = appImageDir / "netlogo-headless.sh"
    Files.setPosixFilePermissions(headlessFile.toPath, permissions)
    val guiFile = appImageDir / "netlogo-gui.sh"
    Files.setPosixFilePermissions(guiFile.toPath, permissions)

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

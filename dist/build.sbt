import sbt._
import sbt.complete.Parser, Parser._

import java.util.jar.Attributes.Name.{ CLASS_PATH => JAR_CLASS_PATH }

import java.io.File

import DistSettings.{ aggregateJDKParser, buildNetLogo, buildVariables,
  mapToParser, netLogoRoot, netLogoVersion, netLogoLongVersion, numericOnlyVersion,
  packageAppParser, platformMap, settings, subApplicationMap, webTarget }

val bootCp = System.getProperty("java.home") + "/lib/rt.jar"

lazy val jfxPackageOptions = taskKey[Package.ManifestAttributes]("Manifest attributes marking package for javapackager")

lazy val packageApp = inputKey[File]("package specified app on specified platform")

lazy val packageLinuxAggregate = inputKey[File]("package all linux apps into a single directory")

lazy val packageMacAggregate = taskKey[File]("package all mac apps into a dmg")

lazy val packageWinAggregate = inputKey[File]("package all win apps into a single directory")

lazy val buildDownloadPages  = taskKey[Seq[File]]("package the web download pages")

lazy val uploadWebsite       = inputKey[Unit]("package the web download pages")

lazy val buildVersionedSite  = taskKey[File]("package the web download pages")

// this value is unfortunately dependent upon both the platform and the application
val appMainClass: PartialFunction[(String, String), String] = {
  case ("windows" | "linux", "NetLogo" | "NetLogo 3D" | "NetLogo Logging") => "org.nlogo.app.App"
  case ("macosx",            "NetLogo" | "NetLogo 3D" | "NetLogo Logging") => "org.nlogo.app.MacApplication"
  case (_,                   "HubNet Client")                              => "org.nlogo.hubnet.client.App"
}

def jvmOptions(platform: PlatformBuild, app: SubApplication): Seq[String] = {
  (platform.shortName, app.name) match {
    case ("macosx", "HubNet Client") => Seq("-Xdock:name=HubNet")
    case ("macosx", _              ) => Seq("-Xdock:name=NetLogo")
    case _                           => Seq()
  }
}

val sharedAppProjectSettings = Seq(
  fork in run := true,
  javacOptions ++=
    s"-bootclasspath $bootCp -deprecation -g -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.6 -target 1.6"
      .split(" ").toSeq,
      netLogoRoot              := baseDirectory.value.getParentFile.getParentFile,
      scalaVersion             := "2.9.2",
      libraryDependencies      += "org.scala-lang" % "scala-library" % "2.9.2",
      unmanagedJars in Compile += netLogoRoot.value / "target" / "NetLogo.jar",
      unmanagedJars in Compile ++= (netLogoRoot.value / "lib_managed" ** "*.jar").get,
      jfxPackageOptions                       := JavaPackager.jarAttributes,
      packageOptions in (Compile, packageBin) += {
        Package.ManifestAttributes(JAR_CLASS_PATH.toString ->
          ((dependencyClasspath in Runtime).value.files :+
            (artifactPath in Compile in packageBin).value)
          .map(_.getName).filter(_.endsWith("jar")).mkString(" "))
      },
      packageOptions in (Compile, packageBin) <+= jfxPackageOptions
    )

lazy val macApp = project.in(file("mac-app")).
  settings(sharedAppProjectSettings: _*).
  settings(
    name                                  := "NetLogo-Mac-App",
    artifactPath in Compile in packageBin := target.value / "netlogo-mac-app.jar")

lazy val dist = project.in(file("."))
  .settings(DistSettings.settings: _*)
  .settings(
    netLogoRoot := baseDirectory.value.getParentFile,
    platformMap := Map(
      "linux"  -> LinuxPlatform,
      "mac"    -> new MacPlatform(macApp),
      "macimg" -> new MacImagePlatform(macApp),
      "win"    -> WindowsPlatform),
    subApplicationMap := Map(
      "NetLogo"         -> NetLogoCoreApp,
      "NetLogo 3D"      -> NetLogoThreeDApp,
      "NetLogo Logging" -> NetLogoLoggingApp,
      "HubNet Client"   -> HubNetClientApp),
    netLogoVersion     := "5.3",
    netLogoLongVersion := { if (netLogoVersion.value.length == 3) netLogoVersion.value + ".0" else netLogoVersion.value },
    numericOnlyVersion := "5.3",
    buildVariables := Map[String, String](
      "version"               -> netLogoVersion.value,
      "numericOnlyVersion"    -> numericOnlyVersion.value,
      "date"                  -> "December 14, 2015"),
    packageApp            <<=
      InputTask.createDyn(packageAppParser)(PackageAction.subApplication(appMainClass, jvmOptions)),
    packageLinuxAggregate <<=
      InputTask.createDyn(aggregateJDKParser)(Def.task(
        PackageAction.aggregate("linux", AggregateLinuxBuild, packageApp))),
    packageWinAggregate   <<=
      InputTask.createDyn(aggregateJDKParser)(Def.task(
        PackageAction.aggregate("win", AggregateWindowsBuild, packageApp))),
    packageMacAggregate   <<=
      PackageAction.aggregate("macimg", AggregateMacBuild, packageApp)(),
    packageAppParser := { (s: State) =>
      ((" " ~> mapToParser(platformMap.value)) ~
        (" " ~> mapToParser(subApplicationMap.value)) ~
        aggregateJDKParser.value(s)).map {
          case ((platform: PlatformBuild, subApp: SubApplication), jpkgr: BuildJDK) => (platform, subApp, jpkgr)
        }
    },
    webTarget := target.value / "downloadPages",
    buildDownloadPages := {
      val webSource = file("downloadPages")
      val downloadLocations =
        Map(
          "macInstaller"     -> s"NetLogo-${netLogoVersion.value}.dmg",
          "winInstaller32"   -> s"NetLogo-${netLogoVersion.value}-32.msi",
          "winInstaller64"   -> s"NetLogo-${netLogoVersion.value}-64.msi",
          "linuxInstaller32" -> s"NetLogo-${netLogoVersion.value}-32.tgz",
          "linuxInstaller64" -> s"NetLogo-${netLogoVersion.value}-64.tgz")
              .map(t => (t._1, webTarget.value / t._2))

      downloadLocations.map(_._2).filterNot(_.exists).foreach { f =>
        sys.error(s"missing $f, please run build on linux, mac, and windows before building download pages")
      }

      val downloadSizes = downloadLocations.map {
        case (name, f) => name.replaceAllLiterally("Installer", "Size") ->
            ((f.length / 1000000).round.toString + " MB")
      }

      val vars = buildVariables.value ++ downloadSizes ++
        downloadLocations.map(t => (t._1, t._2.getName))
      Mustache.betweenDirectories(webSource, webTarget.value, vars)
    },
    uploadWebsite := {
      val tmpTarget = target.value / netLogoLongVersion.value
      val user = System.getenv("USER")
      val host = "ccl.northwestern.edu"
      val targetDir = "/usr/local/www/netlogo"
      IO.copyDirectory(webTarget.value, tmpTarget)
      IO.copyDirectory(netLogoRoot.value / "docs", tmpTarget / "docs")
      RunProcess(Seq("rsync", "-av", "--inplace", "--progress", tmpTarget.getPath, s"${user}@${host}:${targetDir}"), "rsync")
      RunProcess(Seq("ssh", s"${user}@${host}", s""""chgrp -R apache ${targetDir}/${netLogoLongVersion.value}""""), "ssh - change release group")
      RunProcess(Seq("ssh", s"${user}@${host}", s""""chmod -R g+rwX ${targetDir}/${netLogoLongVersion.value}""""), "ssh - change release permissions")
    }
  )


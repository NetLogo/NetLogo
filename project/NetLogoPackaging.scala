import sbt._
import sbt.complete.Parser, Parser._
import Keys.{ baseDirectory, dependencyClasspath, packageBin, runMain, target }
import Docs.manualPDF
import NetLogoBuild.all
import Extensions.extensionRoot
import ModelsLibrary.modelsDirectory
import ChecksumsAndPreviews.allPreviews
import ModelsLibrary.modelIndex
import NativeLibs.nativeLibs

object NetLogoPackaging {

  lazy val aggregateJDKParser = settingKey[State => Parser[BuildJDK]]("parser for packageApp settings")

  // build application jar, resources
  lazy val buildNetLogo = taskKey[Unit]("build NetLogo")
  lazy val buildVariables = taskKey[Map[String, String]]("NetLogo template variables")
  lazy val modelCrossReference = taskKey[Unit]("add model cross references")
  lazy val netLogoRoot = settingKey[File]("Root directory of NetLogo project")
  lazy val netLogoVersion = settingKey[String]("Version of NetLogo under construction")
  lazy val numericOnlyVersion = settingKey[String]("Version of NetLogo under construction (only numbers and periods)")
  lazy val packageAppParser = settingKey[State => Parser[(PlatformBuild, SubApplication, BuildJDK)]]("parser for packageApp settings")
  lazy val platformMap = settingKey[Map[String, PlatformBuild]]("map of names to platforms")
  lazy val subApplicationMap = settingKey[Map[String, SubApplication]]("map of names to sub-application")
  lazy val webTarget = settingKey[File]("location of finished website")
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

  def bundledDirs(netlogo: Project): Def.Initialize[PlatformBuild => Seq[BundledDirectory]] =
    Def.setting {
      { (platform: PlatformBuild) =>
        val nlDir = (baseDirectory in netlogo).value
        Seq(
          new ExtensionDir((extensionRoot in netlogo).value),
          new ModelsDir((modelsDirectory in netlogo).value),
          new DocsDir((baseDirectory in netlogo).value.getParentFile / "docs")
        ) ++ (platform.shortName match {
          case "windows" => Seq(new NativesDir(nlDir / "natives", "windows-amd64", "windows-i586"))
          case "linux"   => Seq(new NativesDir(nlDir / "natives", "linux-amd64", "linux-i586"))
          case "macosx"  => Seq(new LibDir(nlDir / "lib"), new NativesDir(nlDir / "natives", "macosx-universal"))
        })
      }
    }

  def mainJarAndDependencies(netlogo: Project, macApp: Project)(platform: PlatformBuild, app: SubApplication): Def.Initialize[Task[(File, Seq[File])]] = {
    def jarExcluded(f: File): Boolean =
      Seq("scalatest", "scalacheck", "jmock", "junit", "hamcrest")
        .exists(excludedName => f.getName.contains(excludedName))

    def repackageJar(app: SubApplication): Def.Initialize[Task[File]] =
      Def.task {
        val netLogoJar = (packageBin in Compile in netlogo).value
        val platformBuildDir = target.value / s"${platform.shortName}-build"
        IO.createDirectory(platformBuildDir)
        val newJarLocation = platformBuildDir / s"${app.jarName}.jar"
        if (app.name.contains("HubNet"))
          JavaPackager.packageJar(netLogoJar, newJarLocation,
            Some("org.nlogo.hubnet.client.App"))
        else
          JavaPackager.packageJar(netLogoJar, newJarLocation, None)
        newJarLocation
      }

    if (! app.name.contains("HubNet") && platform.shortName == "macosx")
      Def.task {
        ((packageBin in Compile in macApp).value,
          (dependencyClasspath in macApp in Runtime).value.files
            .filterNot(jarExcluded)
            .filterNot(_.isDirectory))
      }
    else
      Def.bind(repackageJar(app)) { repackagedJar =>
        Def.task { (repackagedJar.value,
          (dependencyClasspath in netlogo in Runtime).value.files.filterNot(jarExcluded).filterNot(_.isDirectory)) }
      }
  }

  def jvmOptions(platform: PlatformBuild, app: SubApplication): Seq[String] = {
    (platform.shortName, app.name) match {
      case ("macosx", "HubNet Client") => Seq("-Xdock:name=HubNet")
      case ("macosx", _              ) => Seq("-Xdock:name=NetLogo")
      case _                           => Seq()
    }
  }

  lazy val jdkParser: Parser[BuildJDK] =
    (mapToParserOpt(JavaPackager.systemPackagerOptions.map(j => (j.version + "-" + j.arch -> j)).toMap)
      .map(p => (" " ~> p))
      .getOrElse(Parser.success(PathSpecifiedJDK)))

  def settings(netlogo: Project, macApp: Project): Seq[Setting[_]] = Seq(
    buildNetLogo := {
      (all in netlogo).value
      (manualPDF in netlogo).value
      (allPreviews in netlogo).value
      (runMain in Test in netlogo).toTask(" org.nlogo.tools.ModelResaver").value
      modelCrossReference.value
      (modelIndex in netlogo).value
      (nativeLibs in netlogo).value
    },
    modelCrossReference := {
      ModelCrossReference((baseDirectory in netlogo).value)
    },
    aggregateJDKParser := Def.toSParser(jdkParser),
    netLogoRoot := (baseDirectory in netlogo).value,
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
    netLogoVersion     := "6.0-PREVIEW-12-15",
    numericOnlyVersion := "6.0",
    buildVariables := Map[String, String](
      "version"               -> netLogoVersion.value,
      "numericOnlyVersion"    -> numericOnlyVersion.value,
      "date"                  -> "December 4, 2015"),
    packageAppParser := { (s: State) =>
      ((" " ~> mapToParser(platformMap.value)) ~
        (" " ~> mapToParser(subApplicationMap.value)) ~
        aggregateJDKParser.value(s)).map {
          case ((platform: PlatformBuild, subApp: SubApplication), jpkgr: BuildJDK) => (platform, subApp, jpkgr)
        }
    },
    packageApp            <<=
      InputTask.createDyn(packageAppParser)(PackageAction.subApplication(appMainClass,
        mainJarAndDependencies(netlogo, macApp), bundledDirs(netlogo), jvmOptions)),
    packageLinuxAggregate <<=
      InputTask.createDyn(aggregateJDKParser)(Def.task(
        PackageAction.aggregate("linux", AggregateLinuxBuild, packageApp))),
    packageWinAggregate   <<=
      InputTask.createDyn(aggregateJDKParser)(Def.task(
        PackageAction.aggregate("win", AggregateWindowsBuild, packageApp))),
    packageMacAggregate   <<=
      PackageAction.aggregate("macimg", AggregateMacBuild, packageApp)(),
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
      val tmpTarget = target.value / netLogoVersion.value
      val user = System.getenv("USER")
      val host = "ccl.northwestern.edu"
      val targetDir = "/usr/local/www/netlogo"
      IO.copyDirectory(webTarget.value, tmpTarget)
      IO.copyDirectory(netLogoRoot.value / "docs", tmpTarget)
      RunProcess(Seq("rsync", "-av", "--inplace", "--progress", tmpTarget.getPath, s"${user}@${host}:${targetDir}"), "rsync")
    }
  )

  def mapToParser[T](m: Map[String, T]): Parser[T] = {
    m.map(t => t._1 ^^^ t._2).reduceLeft(_ | _)
  }

  def mapToParserOpt[T](m: Map[String, T]): Option[Parser[T]] = {
    if (m.isEmpty)
      None
    else
      Some(m.map(t => t._1 ^^^ t._2).reduceLeft(_ | _))
  }

  object RunProcess {
    def apply(args: Seq[String], taskName: String): Unit = {
      apply(args, None, taskName)
    }

    def apply(args: Seq[String], workingDirectory: File, taskName: String): Unit = {
      apply(args, Some(workingDirectory), taskName)
    }

    def apply(args: Seq[String], workingDirectory: Option[File], taskName: String): Unit = {
      val res = Process(args, workingDirectory).!
      if (res != 0) {
        sys.error(s"$taskName failed!\n" +
          args.map(_.replaceAllLiterally(" ", "\\ ")).mkString(" "))
      }
    }
  }
}

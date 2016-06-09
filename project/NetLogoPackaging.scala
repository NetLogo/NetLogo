import sbt._
import sbt.complete.Parser, Parser._
import Keys.{ baseDirectory, dependencyClasspath, packageBin, runMain, target }
import Docs.{ allDocs, docsRoot, manualPDF }
import NetLogoBuild.{ all, buildDate, marketingVersion, numericMarketingVersion }
import Extensions.extensionRoot
import ModelsLibrary.modelsDirectory
import ChecksumsAndPreviews.allPreviews
import ModelsLibrary.modelIndex
import NativeLibs.nativeLibs
import Scaladoc.apiScaladoc

object NetLogoPackaging {

  lazy val aggregateJDKParser = settingKey[State => Parser[BuildJDK]]("parser for packageApp settings")

  // build application jar, resources
  lazy val aggregateOnlyFiles = taskKey[Seq[File]]("Files to be included in the aggregate root")
  lazy val buildNetLogo = taskKey[Unit]("build NetLogo")
  lazy val buildVariables = taskKey[Map[String, String]]("NetLogo template variables")
  lazy val mathematicaRoot = settingKey[File]("root of Mathematica-Link directory")
  lazy val localSiteTarget = settingKey[File]("directory into which local copy of the site is built")
  lazy val modelCrossReference = taskKey[Unit]("add model cross references")
  lazy val netLogoRoot = settingKey[File]("Root directory of NetLogo project")
  lazy val netLogoLongVersion = settingKey[String]("Long version number (including trailing zero) of NetLogo under construction")
  lazy val packageAppParser = settingKey[State => Parser[(PlatformBuild, SubApplication, BuildJDK)]]("parser for packageApp settings")
  lazy val platformMap = settingKey[Map[String, PlatformBuild]]("map of names to platforms")
  lazy val subApplicationMap = settingKey[Map[String, SubApplication]]("map of names to sub-application")
  lazy val webTarget = settingKey[File]("location of finished website")
  lazy val jfxPackageOptions = taskKey[Package.ManifestAttributes]("Manifest attributes marking package for javapackager")
  lazy val packageApp = inputKey[File]("package specified app on specified platform")
  lazy val packageLinuxAggregate = inputKey[File]("package all linux apps into a single directory")
  lazy val packageMacAggregate = taskKey[File]("package all mac apps into a dmg")
  lazy val packageWinAggregate = inputKey[File]("package all win apps into a single directory")
  lazy val packagedMathematicaLink = taskKey[File]("Mathematica link, ready for packaging")
  lazy val buildDownloadPages  = taskKey[Seq[File]]("package the web download pages")
  lazy val generateLocalWebsite = taskKey[File]("package the web download pages")
  lazy val uploadWebsite       = inputKey[Unit]("upload the web download pages to the ccl server")

  // this value is unfortunately dependent upon both the platform and the application
  val appMainClass: PartialFunction[(String, String), String] = {
    case ("macosx",            _)                                            => "org.nlogo.app.MacApplication"
    case ("windows" | "linux", "NetLogo" | "NetLogo 3D" | "NetLogo Logging") => "org.nlogo.app.App"
    case (_,                   "HubNet Client")                              => "org.nlogo.hubnet.client.App"
  }

  def bundledDirs(netlogo: Project, macApp: Project): Def.Initialize[PlatformBuild => Seq[BundledDirectory]] =
    Def.setting {
      { (platform: PlatformBuild) =>
        val nlDir = (baseDirectory in netlogo).value
        Seq(
          new ExtensionDir((extensionRoot in netlogo).value),
          new ModelsDir((modelsDirectory in netlogo).value),
          new DocsDir((docsRoot in netlogo).value)
        ) ++ (platform.shortName match {
          case "windows" => Seq(new NativesDir(nlDir / "natives", "windows-amd64", "windows-i586"))
          case "linux"   => Seq(new NativesDir(nlDir / "natives", "linux-amd64", "linux-i586"))
          case "macosx"  => Seq(
            new NativesDir(nlDir / "natives", "macosx-universal"),
            new NativesDir((baseDirectory in macApp).value / "natives", "macosx-universal")
          )
        })
      }
    }

  def mainJarAndDependencies(netlogo: Project, macApp: Project)(platform: PlatformBuild, app: SubApplication): Def.Initialize[Task[(File, Seq[File])]] = {
    def jarExcluded(f: File): Boolean =
      Seq("scalatest", "scalacheck", "jmock", "junit", "hamcrest")
        .exists(f.getName.contains)

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

    if (platform.shortName == "macosx")
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
      case ("macosx", "HubNet Client") => Seq(
          "-Xdock:name=HubNet",
          "-Dorg.nlogo.mac.appClassName=org.nlogo.hubnet.client.App$")
      case ("macosx", _              ) => Seq(
          "-Xdock:name=NetLogo",
          "-Dorg.nlogo.mac.appClassName=org.nlogo.app.App$")
      case _                           => Seq()
    }
  }

  lazy val jdkParser: Parser[BuildJDK] =
    (mapToParserOpt(JavaPackager.systemPackagerOptions.map(j => (j.version + "-" + j.arch -> j)).toMap)
      .map(p => (" " ~> p))
      .getOrElse(Parser.success(PathSpecifiedJDK)))

  def modelTasks(netlogo: Project): Def.Initialize[Task[Unit]] = {
    val resaveModels = (runMain in Test in netlogo).toTask(" org.nlogo.tools.ModelResaver")
    val generatePreviews = (allPreviews in netlogo).toTask("")
    val crossReference = modelCrossReference
    val indexTask = (modelIndex in netlogo)

    Def.task {
      System.setProperty("netlogo.extensions.gogo.javaexecutable",
        (file(System.getProperty("java.home")) / "bin" / "java").getAbsolutePath)
      (resaveModels dependsOn (all in netlogo)).value
      (crossReference dependsOn generatePreviews).value
      (indexTask dependsOn crossReference).value
    }
  }

  def settings(netlogo: Project, macApp: Project): Seq[Setting[_]] = Seq(
    buildNetLogo := {
      (all in netlogo).value
      (allDocs in netlogo).value
      modelTasks(netlogo).value
      RunProcess(Seq("./sbt", "package"), mathematicaRoot.value, s"package mathematica link")
    },
    mathematicaRoot := netLogoRoot.value.getParentFile / "Mathematica-Link",
    packagedMathematicaLink := {
      val mathematicaLinkDir = mathematicaRoot.value
      IO.createDirectory(target.value / "Mathematica Link")
      Seq(
        mathematicaLinkDir / "NetLogo-Mathematica Tutorial.nb",
        mathematicaLinkDir / "NetLogo-Mathematica Tutorial.pdf",
        mathematicaLinkDir / "NetLogo.m",
        mathematicaLinkDir / "target" / "mathematica-link.jar")
        .foreach { f =>
          IO.copyFile(f, target.value / "Mathematica Link" / f.getName)
        }
      target.value / "Mathematica Link"
    },
    aggregateOnlyFiles := {
      Mustache(baseDirectory.value / "readme.md", target.value / "readme.md", buildVariables.value)
      Seq(target.value / "readme.md", netLogoRoot.value / "NetLogo User Manual.pdf", packagedMathematicaLink.value)
    },
    aggregateOnlyFiles in packageLinuxAggregate += {
      val targetFile = target.value / "netlogo-headless.sh"
      Mustache(baseDirectory.value / "netlogo-headless.sh", targetFile, buildVariables.value)
      targetFile.setExecutable(true)
      targetFile
    },
    modelCrossReference := {
      ModelCrossReference((modelsDirectory in netlogo).value)
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
    netLogoLongVersion := { if (marketingVersion.value.length == 3) marketingVersion.value + ".0" else marketingVersion.value },
    buildVariables := Map[String, String](
      "version"               -> marketingVersion.value,
      "numericOnlyVersion"    -> numericMarketingVersion.value,
      "date"                  -> buildDate.value),
    packageAppParser := { (s: State) =>
      ((" " ~> mapToParser(platformMap.value)) ~
        (" " ~> mapToParser(subApplicationMap.value)) ~
        aggregateJDKParser.value(s)).map {
          case ((platform: PlatformBuild, subApp: SubApplication), jpkgr: BuildJDK) => (platform, subApp, jpkgr)
        }
    },
    packageApp            <<=
      InputTask.createDyn(packageAppParser)(PackageAction.subApplication(appMainClass,
        mainJarAndDependencies(netlogo, macApp), bundledDirs(netlogo, macApp), jvmOptions)),
    packageLinuxAggregate <<=
      InputTask.createDyn(aggregateJDKParser)(Def.task(
        PackageAction.aggregate("linux", AggregateLinuxBuild, packageApp, packageLinuxAggregate))),
    packageWinAggregate   <<=
      InputTask.createDyn(aggregateJDKParser)(Def.task(
        PackageAction.aggregate("win", AggregateWindowsBuild, packageApp, packageWinAggregate))),
    packageMacAggregate   <<=
      PackageAction.aggregate("macimg", AggregateMacBuild, packageApp, packageMacAggregate)(),
    webTarget := target.value / "downloadPages",
    buildDownloadPages := {
      val webSource = baseDirectory.value / "downloadPages"
      val downloadLocations =
        Map(
          "macInstaller"     -> s"NetLogo-${marketingVersion.value}.dmg",
          "winInstaller32"   -> s"NetLogo-${marketingVersion.value}-32.msi",
          "winInstaller64"   -> s"NetLogo-${marketingVersion.value}-64.msi",
          "linuxInstaller32" -> s"NetLogo-${marketingVersion.value}-32.tgz",
          "linuxInstaller64" -> s"NetLogo-${marketingVersion.value}-64.tgz")
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
    localSiteTarget := target.value / marketingVersion.value,
    generateLocalWebsite := {
      IO.copyDirectory(webTarget.value, localSiteTarget.value)
      IO.copyDirectory((modelsDirectory in netlogo).value, localSiteTarget.value / "models")
      IO.copyDirectory(netLogoRoot.value / "docs", localSiteTarget.value / "docs")
      localSiteTarget.value
    },
    uploadWebsite := {
      val user = System.getenv("USER")
      val host = "ccl.northwestern.edu"
      val targetDir = "/usr/local/www/netlogo"
      val generatedSite = generateLocalWebsite.value
      RunProcess(Seq("rsync", "-av", "--inplace", "--progress", generatedSite.getPath, s"${user}@${host}:${targetDir}"), "rsync")
      RunProcess(Seq("ssh", s"${user}@${host}", "chgrp", "-R", "apache", s"${targetDir}/${netLogoLongVersion.value}"), "ssh - change release group")
      RunProcess(Seq("ssh", s"${user}@${host}", "chmod", "-R", "g+rwX",  s"${targetDir}/${netLogoLongVersion.value}"), "ssh - change release permissions")
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

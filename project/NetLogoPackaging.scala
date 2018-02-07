import sbt._
import sbt.complete.Parser, Parser._
import Keys.{ baseDirectory, buildStructure, dependencyClasspath, packageBin, runMain, state, streams, target }
import ChecksumsAndPreviews.allPreviews
import Docs.{ allDocs, docsRoot, manualPDF }
import Extensions.{ extensions, extensionRoot }
import ModelsLibrary.{ modelsDirectory, modelIndex }
import NativeLibs.nativeLibs
import NetLogoBuild.{ all, buildDate, marketingVersion, numericMarketingVersion }
import SbtSubdirectory.runSubdirectoryCommand
import java.nio.file.Paths

import scala.collection.JavaConverters._

object NetLogoPackaging {

  lazy val aggregateJDKParser      = settingKey[State => Parser[BuildJDK]]("parser for packageApp settings")
  lazy val aggregateOnlyFiles      = taskKey[Seq[File]]("Files to be included in the aggregate root")
  lazy val behaviorsearchRoot      = settingKey[File]("root of behaviorsearch directory")
  lazy val buildNetLogo            = taskKey[Unit]("build NetLogo")
  lazy val buildVariables          = taskKey[Map[String, String]]("NetLogo template variables")
  lazy val buildDownloadPages      = taskKey[Seq[File]]("package the web download pages")
  lazy val configRoot              = settingKey[File]("configuration directory")
  lazy val iconFiles               = settingKey[Seq[File]]("icon files to make available")
  lazy val resaveModels            = taskKey[Unit]("prep models library for packaging")
  lazy val generateLocalWebsite    = taskKey[File]("package the web download pages")
  lazy val localSiteTarget         = settingKey[File]("directory into which local copy of the site is built")
  lazy val mathematicaRoot         = settingKey[File]("root of Mathematica-Link directory")
  lazy val netLogoLongVersion      = settingKey[String]("Long version number (including trailing zero) of NetLogo under construction")
  lazy val netLogoRoot             = settingKey[File]("Root directory of NetLogo project")
  lazy val packagedMathematicaLink = taskKey[File]("Mathematica link, ready for packaging")
  lazy val packageLinuxAggregate   = inputKey[File]("package all linux apps into a single directory")
  lazy val packageMacAggregate     = taskKey[File]("package all mac apps into a dmg")
  lazy val packageWinAggregate     = inputKey[File]("package all win apps into a single directory")
  lazy val packagingClasspath      = taskKey[Seq[File]]("Jars to include when packaging")
  lazy val packagingMainJar        = taskKey[File]("Main jar to use when packaging")
  lazy val subApplications         = settingKey[Seq[SubApplication]]("map of names to sub-application")
  lazy val uploadWebsite           = inputKey[Unit]("upload the web download pages to the ccl server")
  lazy val webTarget               = settingKey[File]("location of finished website")

  def bundledDirs(netlogo: Project, macApp: Project, behaviorsearchProject: Project): Def.Initialize[PlatformBuild => Seq[BundledDirectory]] =
    Def.setting {
      { (platform: PlatformBuild) =>
        val nlDir = (baseDirectory in netlogo).value
        Seq(
          new ExtensionDir((extensionRoot in netlogo).value),
          new ModelsDir((modelsDirectory in netlogo).value),
          new DocsDir((docsRoot in netlogo).value),
          new BehaviorsearchDir((baseDirectory in behaviorsearchProject).value, platform.shortName)
        ) ++ (platform.shortName match {
          case "windows" => Seq(new NativesDir(nlDir / "natives", "windows-amd64", "windows-i586"))
          case "linux"   => Seq(new NativesDir(nlDir / "natives", "linux-amd64", "linux-i586"))
          case "macosx"  => Seq(new NativesDir(nlDir / "natives", "macosx-universal"))
        })
      }
    }

  def repackageJar(app: SubApplication, platform: PlatformBuild, netlogo: Project): Def.Initialize[Task[File]] =
    Def.task {
      val mainClass = if (app.name.contains("HubNet")) Some("org.nlogo.hubnet.client.App") else None
      val netLogoJar = (packageBin in Compile in netlogo).value
      val platformBuildDir = target.value / s"${platform.shortName}-build"
      JavaPackager.repackageJar(app, mainClass, netLogoJar, platformBuildDir)
    }

  private def jarExcluded(f: File): Boolean =
    Seq("scalatest", "scalacheck", "jmock", "junit", "hamcrest")
      .exists(f.getName.contains)

  lazy val jdkParser: Parser[BuildJDK] =
    (mapToParserOpt(JavaPackager.systemPackagerOptions.map(j => (j.version + "-" + j.arch -> j)).toMap)
      .map(p => (" " ~> p))
      .getOrElse(Parser.success(PathSpecifiedJDK)))

  def settings(netlogo: Project, macApp: Project, behaviorsearchProject: Project): Seq[Setting[_]] = Seq(
    netLogoRoot     := (baseDirectory in netlogo).value,
    behaviorsearchRoot := netLogoRoot.value.getParentFile / "behaviorsearch",
    mathematicaRoot := netLogoRoot.value.getParentFile / "Mathematica-Link",
    configRoot      := baseDirectory.value / "configuration",
    localSiteTarget := target.value / marketingVersion.value,
    aggregateJDKParser := Def.toSParser(jdkParser),
    subApplications    := Seq(NetLogoCoreApp, NetLogoLoggingApp, HubNetClientApp, BehaviorsearchApp),
    netLogoLongVersion := { if (marketingVersion.value.length == 3) marketingVersion.value + ".0" else marketingVersion.value },
    buildNetLogo := {
      (all in netlogo).value
      (allDocs in netlogo).value
      (allPreviews in netlogo).toTask("").value
      resaveModels.value
      SbtSubdirectory.runSubdirectoryCommand(mathematicaRoot.value, state.value, (packageBin in Compile in netlogo).value, Seq("package"))
      (packageBin in Compile in behaviorsearchProject).value
    },
    resaveModels := {
      (runMain in Test in netlogo).toTask(" org.nlogo.tools.ModelResaver").value
    },
    resaveModels := (resaveModels dependsOn (extensions in netlogo)).value,
    packagedMathematicaLink := {
      val mathematicaLinkDir = mathematicaRoot.value
      IO.createDirectory(target.value / "Mathematica Link")
      Seq(
        mathematicaLinkDir / "NetLogo-Mathematica Tutorial.nb",
        mathematicaLinkDir / "NetLogo-Mathematica Tutorial.pdf",
        mathematicaLinkDir / "NetLogo.m",
        mathematicaLinkDir / "target" / "mathematica-link.jar")
        .foreach { f =>
          FileActions.copyFile(f, target.value / "Mathematica Link" / f.getName)
        }
      target.value / "Mathematica Link"
    },
    aggregateOnlyFiles := {
      Mustache(baseDirectory.value / "readme.md", target.value / "readme.md", buildVariables.value)
      Seq(target.value / "readme.md", netLogoRoot.value / "NetLogo User Manual.pdf", packagedMathematicaLink.value)
    },
    buildVariables := Map[String, String](
      "version"            -> marketingVersion.value,
      "numericOnlyVersion" -> numericMarketingVersion.value,
      "date"               -> buildDate.value,
      "year"               -> buildDate.value.takeRight(4)),
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
      Mustache.betweenDirectories(webSource, webTarget.value,
        Map("index" -> "NetLogo {{version}} Downloads"), vars)
    },
    generateLocalWebsite := {
      FileActions.copyDirectory(webTarget.value, localSiteTarget.value)
      FileActions.copyDirectory((modelsDirectory in netlogo).value, localSiteTarget.value / "models")
      FileActions.copyDirectory(netLogoRoot.value / "docs", localSiteTarget.value / "docs")
      FileActions.copyFile(netLogoRoot.value / "NetLogo User Manual.pdf", localSiteTarget.value / "docs" / "NetLogo User Manual.pdf")
      localSiteTarget.value
    },
    uploadWebsite := {
      val user = System.getenv("USER")
      val host = "ccl.northwestern.edu"
      val targetDir = "/usr/local/www/netlogo"
      val generatedSite = generateLocalWebsite.value
      RunProcess(Seq("rsync", "-av", "--inplace", "--progress", generatedSite.getPath, s"${user}@${host}:${targetDir}"), "rsync")
      RunProcess(Seq("ssh", s"${user}@${host}", "chgrp", "-R", "apache", s"${targetDir}/${marketingVersion.value}"), "ssh - change release group")
      RunProcess(Seq("ssh", s"${user}@${host}", "chmod", "-R", "g+rwX",  s"${targetDir}/${marketingVersion.value}"), "ssh - change release permissions")
    },
    packagingClasspath := {
      val allDeps = (dependencyClasspath in netlogo in Runtime).value ++
        (dependencyClasspath in behaviorsearchProject in Runtime).value

      ((removeSjsLibraries _ compose removeJdkLibraries _ compose filterDuplicateDeps _)
        (allDeps).files :+
        (packageBin in Compile in behaviorsearchProject).value)
        .filterNot(jarExcluded)
        .filterNot(_.isDirectory) :+ packagingMainJar.value
    },
    packagingMainJar := {
      (packageBin in Compile in netlogo).value
    },
    packageLinuxAggregate := {
      val buildJDK = aggregateJDKParser.parsed
      val outDir = target.value / s"packaged-linux-${buildJDK.arch}-${buildJDK.version}"

      val srcDir = target.value / s"to-package-linux-${buildJDK.arch}-${buildJDK.version}"
      FileActions.createDirectories(srcDir)

      // need to consolidate with other platforms
      val mainJar = packagingMainJar.value
      FileActions.copyFile(mainJar, srcDir / mainJar.getName)

      JavaPackager.generateStubApplication(buildJDK, "dummy", "image", srcDir, outDir, target.value, mainJar)

      FileActions.remove(srcDir)

      val bundled = bundledDirs(netlogo, macApp, behaviorsearchProject).value(LinuxPlatform)

      val commonConfig = CommonConfiguration(
        mainJar,
        "",
        bundled,
        packagingClasspath.value,
        Seq(),
        Seq(),
        (aggregateOnlyFiles in packageLinuxAggregate).value,
        configRoot.value,
        marketingVersion.value,
        buildJDK,
        webTarget.value
      )

      PackageLinuxAggregate(
        target.value / s"linux-aggregate-${buildJDK.arch}",
        commonConfig,
        (outDir -> "dummy"),
        subApplications.value,
        buildVariables.value
      )
    },
    iconFiles in packageWinAggregate := {
      ((configRoot.value ** "*.ico") +++
        ((baseDirectory in behaviorsearchProject).value ** "Behaviorsearch.ico") +++
        ((baseDirectory in behaviorsearchProject).value ** "behaviorsearch_model.ico")).get.toSeq
    },
    packageWinAggregate := {
      val buildJDK = aggregateJDKParser.parsed
      val netLogoJar = repackageJar(DummyApp, WindowsPlatform, netlogo).value
      val outDir = target.value / s"packaged-win-${buildJDK.arch}-${buildJDK.version}"

      val srcDir = target.value / s"to-package-win-${buildJDK.arch}-${buildJDK.version}"
      FileActions.createDirectories(srcDir)

      val mainJar = packagingMainJar.value
      FileActions.copyFile(mainJar, srcDir / mainJar.getName)

      JavaPackager.generateStubApplication(buildJDK, "dummy", "image", srcDir, outDir, target.value, mainJar)

      FileActions.remove(srcDir)

      val bundled = bundledDirs(netlogo, macApp, behaviorsearchProject).value(WindowsPlatform)

      val commonConfig = CommonConfiguration(
        mainJar,
        "",
        bundled,
        packagingClasspath.value,
        Seq(),
        (iconFiles in packageWinAggregate).value,
        aggregateOnlyFiles.value,
        configRoot.value,
        marketingVersion.value,
        buildJDK,
        webTarget.value
      )

      PackageWinAggregate(
        target.value / s"win-aggregate-${buildJDK.arch}",
        commonConfig,
        outDir -> "dummy",
        subApplications.value,
        buildVariables.value)
    },
    iconFiles in packageMacAggregate := {
      ((configRoot.value ** "*.icns") +++
        ((baseDirectory in behaviorsearchProject).value ** "*.icns")).get.toSeq
    },
    packageMacAggregate := {
      val buildJDK = PathSpecifiedJDK
      val netLogoJar = repackageJar(DummyApp, new MacImagePlatform(macApp), netlogo).value
      val outDir = target.value / "packaged-mac"

      val mainJar = packagingMainJar.value
      val macAppMainJar = (packageBin in Compile in macApp).value
      JavaPackager.generateStubApplication(buildJDK, "dummy", "image", target.value, outDir, target.value, mainJar)

      val classPath =
        (packagingClasspath.value ++
          removeSjsLibraries((dependencyClasspath in macApp in Runtime).value).files)
          .filterNot(jarExcluded)
          .filterNot(_.isDirectory) :+ macAppMainJar

      val fileAssociations =
        Seq(
          Map(
            "extension" -> "nlogo",
            "icon"      -> "Model.icns"
          ).asJava,
          Map(
            "extension" -> "nlogo3d",
            "icon"      -> "Model.icns"
          ).asJava,
          Map(
            "extension" -> "nlogox",
            "icon"      -> "Model.icns"
          ).asJava
        ).asJava
      val nlAppConfig = Map[String, AnyRef](
        "bundleIdentifier"    -> "org.nlogo.NetLogo",
        "bundleName"          -> "NetLogo",
        "bundleSignature"     -> "nLo1",
        "fileAssociations"    -> fileAssociations,
        "fileAssociationIcon" -> "Model.icns",
        "iconFile"            -> "NetLogo.icns",
        "packageID"           -> "APPLnLo1"
      )
      val bsearchFileAssociations = Seq(
        Map(
          "extension" -> "bsearch",
          "icon"      -> "Behaviorsearch.icns"
        ).asJava
      ).asJava
      val appSpecificConfig = Map(
        NetLogoCoreApp    -> nlAppConfig,
        NetLogoLoggingApp -> (nlAppConfig - "fileAssociations"),
        HubNetClientApp   -> Map(
          "bundleIdentifier" -> "org.nlogo.HubNetClient",
          "bundleName"       -> "HubNet Client",
          "bundleSignature"  -> "????",
          "iconFile"         -> "HubNet Client.icns",
          "packageID"        -> "APPL????"
        ),
        BehaviorsearchApp -> Map(
          "bundleIdentifier"    -> "org.nlogo.Behaviorsearch",
          "bundleName"          -> "Behaviorsearch",
          "bundleSignature"     -> "????",
          "fileAssociations"    -> bsearchFileAssociations,
          "iconFile"            -> "Behaviorsearch.icns",
          "packageID"           -> "APPL????"
        )
      )


      val bundled = bundledDirs(netlogo, macApp, behaviorsearchProject).value(new MacImagePlatform(macApp))
      val commonConfig = CommonConfiguration(
        macAppMainJar,
        "org.nlogo.app.MacApplication",
        bundled,
        classPath,
        bundled.filter(_.directoryName == "natives"),
        (iconFiles in packageMacAggregate).value,
        aggregateOnlyFiles.value,
        configRoot.value,
        marketingVersion.value,
        buildJDK,
        webTarget.value
      )
      PackageMacAggregate(
        target.value / "mac-aggregate",
        commonConfig,
        appSpecificConfig,
        outDir -> "dummy",
        subApplications.value,
        buildVariables.value)
    }
  )


  def filterDuplicateDeps(cp: Def.Classpath): Def.Classpath = {
    val modId = AttributeKey[ModuleID]("moduleId")
    val (modules, others) = cp.partition(_.get(modId).isDefined)
    val filteredModules =
      modules.foldLeft(Seq.empty[Attributed[File]]) {
        case (acc, jar) =>
          val id = jar.get(modId).get
          if (acc.exists { j =>
            val otherId = j.get(modId).get
            otherId.organization == id.organization &&
            otherId.name == id.name &&
            isNewer(otherId.revision, id.revision)
          })
          acc
        else
          acc :+ jar
      }
    filteredModules ++ others
  }

  private def isNewer(s1: String, s2: String): Boolean = {
    (s1.split("\\.") zip s2.split("\\."))
      .filter(t => t._1 != t._2)
      .headOption
      .map((compareRevisionPart _).tupled)
      .getOrElse(false)
  }

  private def compareRevisionPart(p1: String, p2: String): Boolean = {
    val p1Int = try { p1.toInt } catch { case f: NumberFormatException => -1 }
    val p2Int = try { p2.toInt } catch { case f: NumberFormatException => -1 }
    if (p1Int > -1 && p2Int > -1) Ordering.Int.compare(p1Int, p2Int) > 0
    else                          Ordering.String.compare(p1, p2) > 0
  }

  private def removeJdkLibraries(cp: Def.Classpath): Def.Classpath = {
    cp.filterNot(_.get(AttributeKey[Boolean]("jdkLibrary")).getOrElse(false))
  }

  private def removeSjsLibraries(cp: Def.Classpath): Def.Classpath = {
    cp.filterNot(_.data.getName.contains("_sjs"))
  }

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

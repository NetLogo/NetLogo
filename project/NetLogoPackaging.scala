import sbt._
import sbt.complete.Parser, Parser._
import Keys.{ baseDirectory, buildStructure, dependencyClasspath, packageBin, state, streams, target }
import ChecksumsAndPreviews.allPreviews
import Docs.{ allDocs, docsRoot, extensionDocs, htmlDocs, manualPDF }
import Extensions.{ extensions, extensionRoot }
import ModelsLibrary.{ modelsDirectory, modelIndex }
import NativeLibs.nativeLibs
import NetLogoBuild.{ all, buildDate, marketingVersion, numericMarketingVersion }
import SbtSubdirectory.runSubdirectoryCommand
import Running.makeMainTask
import java.nio.file.Paths
import java.nio.file.Files
import scala.sys.process.Process

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
  lazy val buildMathematicaLink    = taskKey[Unit]("build and package Mathematica Link submodule")
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
  lazy val uploadWebsite           = inputKey[Unit]("upload the web download pages to the ccl server")
  lazy val uploadDocs              = inputKey[Unit]("Upload the web docs pages only to the CCL server")
  lazy val webTarget               = settingKey[File]("location of finished website")

  def bundledDirs(netlogo: Project, macApp: Project, behaviorsearchProject: Project): Def.Initialize[(String, String) => Seq[BundledDirectory]] =
    Def.setting {
      { (platform: String, arch: String) =>
        val nlDir = (baseDirectory in netlogo).value
        Seq(
          new ExtensionDir((extensionRoot in netlogo).value, platform, arch),
          new ModelsDir((modelsDirectory in netlogo).value),
          new DocsDir((docsRoot in netlogo).value),
          new BehaviorsearchDir((baseDirectory in behaviorsearchProject).value, platform)
        ) ++ (platform match {
          case "windows" => Seq(new NativesDir(nlDir / "natives", "windows-amd64", "windows-i586"))
          case "linux"   => Seq(new NativesDir(nlDir / "natives", "linux-amd64", "linux-i586"))
          case "macosx"  => Seq(new NativesDir(nlDir / "natives", "macosx-universal"))
        })
      }
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
    netLogoLongVersion := { if (marketingVersion.value.length == 3) marketingVersion.value + ".0" else marketingVersion.value },

    buildNetLogo := {
      (all in netlogo).value
      (allDocs in netlogo).value
      (allPreviews in netlogo).toTask("").value
      resaveModels.value
      buildMathematicaLink.value
      (packageBin in Compile in behaviorsearchProject).value
    },

    resaveModels := {
      makeMainTask("org.nlogo.tools.ModelResaver",
        classpath = (Keys.fullClasspath in Test in netlogo),
        workingDirectory = baseDirectory(_.getParentFile)).toTask("").value
    },

    resaveModels := (resaveModels dependsOn (extensions in netlogo)).value,

    buildMathematicaLink := {
      val sbt = if (System.getProperty("os.name").contains("Windows")) "sbt.bat" else "sbt"
      RunProcess(Seq(sbt, "package"), mathematicaRoot.value, "package mathematica link")
    },

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
      "year"               -> buildDate.value.takeRight(4),
      "date"               -> buildDate.value),
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
            ((f.length / 1000000).toString + " MB")
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
      // Use `--ignore-existing` because generally the website is only uploaded once during a release.  If there is a failure
      // and it needs to be re-uploaded, the directory should be wiped on the server and then this task can be re-run.  If
      // just the docs need to be updated, see `uploadDocs` below, which will change existing files.  -Jeremy B October 2021
      RunProcess(Seq("rsync", "-rltv", "--ignore-existing", "--progress", generatedSite.getPath, s"${user}@${host}:${targetDir}"), "rsync")
      RunProcess(Seq("ssh", s"${user}@${host}", "chgrp", "-R", "apache", s"${targetDir}/${marketingVersion.value}"), "ssh - change release group")
      RunProcess(Seq("ssh", s"${user}@${host}", "chmod", "-R", "g+rwX",  s"${targetDir}/${marketingVersion.value}"), "ssh - change release permissions")
    },

    uploadDocs := {
      val user = System.getenv("USER")
      val host = "ccl.northwestern.edu"
      val sourceDir = netLogoRoot.value / "docs"
      val targetDir = s"/usr/local/www/netlogo/${netLogoLongVersion.value}"
      val manualSource = netLogoRoot.value / "NetLogo User Manual.pdf"
      val manualTarget = s"$targetDir/docs/NetLogo User Manual.pdf"
      (allDocs in netlogo).value
      RunProcess(Seq("rsync", "-rltv", "--inplace", "--progress", sourceDir.getPath, s"$user@$host:$targetDir"), "rsync docs")
      RunProcess(Seq("rsync", "-rltv", "--inplace", "--progress", manualSource.getPath, s"$user@$host:$manualTarget"), "rsync user manual")
      RunProcess(Seq("ssh", s"$user@$host", "chgrp", "-R", "apache", targetDir), "ssh - change release group")
      RunProcess(Seq("ssh", s"$user@$host", "chmod", "-R", "g+rwX", targetDir), "ssh - change release permissions")
    },

    packagingClasspath := {
      val allDeps = (dependencyClasspath in netlogo in Runtime).value ++
        (dependencyClasspath in behaviorsearchProject in Runtime).value

      (removeJdkLibraries(filterDuplicateDeps(allDeps)).files :+
        (packageBin in Compile in behaviorsearchProject).value)
        .filterNot(jarExcluded)
        .filterNot(_.isDirectory) :+ packagingMainJar.value
    },

    packagingMainJar := {
      (packageBin in Compile in netlogo).value
    },

    packageLinuxAggregate := {
      val log          = streams.value.log
      val version      = marketingVersion.value
      val buildJDK     = aggregateJDKParser.parsed
      val buildDir     = target.value
      val platform     = "linux"
      val configDir    = configRoot.value
      val netLogoJar   = (netlogo / Compile / packageBin).value
      val dependencies = packagingClasspath.value
      val rootFiles    = (packageLinuxAggregate / aggregateOnlyFiles).value
      val variables    = buildVariables.value

      val mainLauncher = new NetLogoLauncher(version)
      val launchers    = Seq(new NetLogo3dLauncher(version), new HubNetClientLauncher(version), new BehaviorsearchLauncher(version))

      val inputDir = JavaPackager.setupAppImageInput(log, version, buildJDK, buildDir, netLogoJar, dependencies)
      val destDir  = buildDir / s"${platform}-dest-${buildJDK.version}-${buildJDK.arch}"
      FileActions.remove(destDir)
      val appImageDir = JavaPackager.generateAppImage(log, buildJDK.jpackage, platform, mainLauncher, configDir, buildDir, inputDir, destDir, Seq(), launchers)

      val extraDirs = bundledDirs(netlogo, macApp, behaviorsearchProject).value(platform, buildJDK.arch)
      JavaPackager.copyExtraFiles(log, extraDirs, platform, buildJDK.arch, appImageDir, appImageDir / "bin", rootFiles)
      JavaPackager.createScripts(log, appImageDir, appImageDir / "lib" / "app", configDir, "netlogo-headless.sh", "netlogo-gui.sh", variables)

      PackageLinuxAggregate(
        log
      , version
      , buildJDK.arch
      , configDir
      , destDir / "NetLogo"
      , webTarget.value
      , extraDirs
      , mainLauncher +: launchers
      )
    },

    packageWinAggregate := {
      val log          = streams.value.log
      val version      = marketingVersion.value
      val buildJDK     = aggregateJDKParser.parsed
      val buildDir     = target.value
      val platform     = "windows"
      val configDir    = configRoot.value
      val netLogoJar   = (netlogo / Compile / packageBin).value
      val dependencies = packagingClasspath.value
      val rootFiles    = (packageWinAggregate / aggregateOnlyFiles).value
      val variables    = buildVariables.value

      val icons = Seq(
        (behaviorsearchProject / baseDirectory).value / "resources" / "Behaviorsearch.ico"
      , (behaviorsearchProject / baseDirectory).value / "resources" / "behaviorsearch_model.ico"
      , configDir / "windows" / "NetLogo.ico"
      , configDir / "windows" / "HubNet Client.ico"
      , configDir / "windows" / "model.ico"
      )
      icons.foreach( (i) => FileActions.copyFile(i, buildDir / i.getName) )

      val mainLauncher = new NetLogoLauncher(version, Some("NetLogo.ico"), Seq(), Seq("icon="))
      val launchers = Seq(
        new NetLogoLauncher(version, Some("NetLogo.ico"), Seq(), Seq("win-console=true")) {
          override def id = "NetLogo_Console"
          override def mustachePrefix = "win-console-launcher"
        }
      , new NetLogo3dLauncher(version, Some("NetLogo.ico"))
      , new HubNetClientLauncher(version, Some("HubNet Client.ico"))
      , new BehaviorsearchLauncher(version, Some("Behaviorsearch.ico"))
      )

      val inputDir    = JavaPackager.setupAppImageInput(log, version, buildJDK, buildDir, netLogoJar, dependencies)
      val destDir     = buildDir / s"${platform}-dest-${buildJDK.version}-${buildJDK.arch}"
      val extraArgs   = Seq("--icon", "NetLogo.ico")
      FileActions.remove(destDir)
      val appImageDir = JavaPackager.generateAppImage(log, buildJDK.jpackage, platform, mainLauncher, configDir, buildDir, inputDir, destDir, extraArgs, launchers)

      // this makes the file association icons available for wix
      icons.foreach( (i) => FileActions.copyFile(i, appImageDir / i.getName) )

      val extraDirs = bundledDirs(netlogo, macApp, behaviorsearchProject).value(platform, buildJDK.arch)
      JavaPackager.copyExtraFiles(log, extraDirs, platform, buildJDK.arch, appImageDir, appImageDir, rootFiles)
      JavaPackager.createScripts(log, appImageDir, appImageDir / "app", configDir / platform, "netlogo-headless.bat", "netlogo-gui.bat", variables)

      PackageWinAggregate(
        log
      , version
      , buildJDK.arch
      , configDir
      , destDir / "NetLogo"
      , webTarget.value
      , variables
      , mainLauncher +: launchers
      )
    },

    iconFiles in packageMacAggregate := {
      ((configRoot.value ** "*.icns") +++
        ((baseDirectory in behaviorsearchProject).value ** "*.icns")).get.toSeq
    },

    packageMacAggregate := {
      val log          = streams.value.log
      val version      = marketingVersion.value
      val buildJDK     = PathSpecifiedJDK
      val buildDir     = target.value
      val platform     = "macosx"
      val configDir    = configRoot.value
      val netLogoJar   = (netlogo / Compile / packageBin).value
      val dependencies = packagingClasspath.value
      val rootFiles    = (packageLinuxAggregate / aggregateOnlyFiles).value
      val variables    = buildVariables.value

      val icons = Seq(
        (behaviorsearchProject / baseDirectory).value / "resources" / "Behaviorsearch.icns"
      , configDir / "macosx" / "NetLogo.icns"
      , configDir / "macosx" / "HubNet Client.icns"
      , configDir / "macosx" / "Model.icns"
      )
      icons.foreach( (i) => FileActions.copyFile(i, buildDir / i.getName) )

      val macOsJavaOptions = Seq(
        "-Dapple.awt.graphics.UseQuartz=true"
      , "--add-exports=java.desktop/com.apple.laf=ALL-UNNAMED"
      , "-Dnetlogo.extensions.dir={{{ROOTDIR}}}/extensions"
      , "-Dnetlogo.models.dir={{{ROOTDIR}}}/models"
      , "-Dnetlogo.docs.dir={{{ROOTDIR}}}/docs"
      , "-Djava.library.path={{{ROOTDIR}}}/natives/macosx-universal"
      , "-Djogamp.gluegen.UseTempJarCache=false"
      )
      val launchers = Seq(
        new NetLogoLauncher(
          version
        , Some("NetLogo.icns")
        , macOsJavaOptions :+ "-Xdock:name=NetLogo" :+ "-Dorg.nlogo.mac.appClassName=org.nlogo.app.App$"
        , Seq()
        , Some("netlogo-mac-app.jar")
        , Some("org.nlogo.app.MacApplication")
        ) {
          override def name = s"NetLogo $version"
        }
      , new NetLogo3dLauncher(
          version
        , Some("NetLogo.icns")
        , macOsJavaOptions :+ "-Xdock:name=NetLogo 3D" :+ "-Dorg.nlogo.mac.appClassName=org.nlogo.app.App$"
        , Seq()
        , Some("netlogo-mac-app.jar")
        , Some("org.nlogo.app.MacApplication")
        ) {
          override def name = s"NetLogo 3D $version"
        }
      , new HubNetClientLauncher(
          version
        , Some("HubNet Client.icns")
        , macOsJavaOptions ++ Seq(
            "-Xdock:name=HubNet"
          , "-Dorg.nlogo.mac.appClassName=org.nlogo.hubnet.client.App$"
          , "-Dapple.laf.useScreenMenuBar=true"
         )
        , Seq()
        , Some("netlogo-mac-app.jar")
        , Some("org.nlogo.app.MacApplication")
        ) {
          override def name = s"HubNet Client $version"
        }
      , new BehaviorsearchLauncher(
          version
        , Some("Behaviorsearch.icns")
        , macOsJavaOptions ++ Seq(
            "-Xdock:name=Behaviorsearch"
          , "-Dorg.nlogo.mac.appClassName=bsearch.fx.MainGUI"
          , "-Dbsearch.appfolder={{{ROOTDIR}}}/behaviorsearch"
          , "-Dbsearch.startupfolder={{{ROOTDIR}}}"
          )
        ) {
          override def name = s"Behaviorsearch $version"
        }
      )

      val inputDir = JavaPackager.setupAppImageInput(log, version, buildJDK, buildDir, netLogoJar, dependencies)

      val macAppMainJar = (macApp / Compile / packageBin).value
      val macAppDeps = removeJdkLibraries((macApp / Runtime / dependencyClasspath).value).files
      (macAppDeps :+ macAppMainJar).foreach( (dep) => {
        FileActions.copyFile(dep, inputDir / dep.getName)
      })

      val destDir = buildDir / s"${platform}-dest-${buildJDK.version}-${buildJDK.arch}"
      FileActions.remove(destDir)

      launchers.foreach( (launcher) => {
        val jOpts     = launcher.javaOptions.map( (opt) => s""""$opt"""" ).mkString(" ")
        val extraArgs = Seq("--icon", launcher.icon.getOrElse(""), "--java-options", jOpts)
        val appPackage = JavaPackager.generateAppImage(log, buildJDK.jpackage, platform, launcher, configDir, buildDir, inputDir, destDir, extraArgs, Seq())
        FileActions.copyFile(configDir / "macosx" / "Model.icns", destDir / s"${launcher.name}.app" / "Contents" / "Resources" / "Model.icns")
      })

      val appImageDir = destDir / s"NetLogo $version"
      FileActions.remove(appImageDir)
      val extraDirs = bundledDirs(netlogo, macApp, behaviorsearchProject).value(platform, buildJDK.arch)
      JavaPackager.copyExtraFiles(log, extraDirs, platform, buildJDK.arch, appImageDir, appImageDir, rootFiles)
      val bundleDir = PackageMacAggregate.createBundleDir(log, version, destDir, configDir, launchers)
      JavaPackager.createScripts(log, bundleDir, bundleDir / "app", configDir, "netlogo-headless.sh", "netlogo-gui.sh", variables + ("javaOptions" -> "--add-exports=java.desktop/com.apple.laf=ALL-UNNAMED"))

      PackageMacAggregate(
        log
      , version
      , destDir
      , bundleDir
      , configDir
      , webTarget.value
      , launchers
      )
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

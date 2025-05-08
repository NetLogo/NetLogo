import sbt._
import sbt.complete.Parser, Parser._
import Keys.{ baseDirectory, buildStructure, dependencyClasspath, packageBin, state, streams, target }
import ChecksumsAndPreviews.allPreviews
import Docs.{ allDocs, docsRoot, extensionDocs, htmlDocs, manualPDF }
import Extensions.{ extensions, extensionRoot }
import ModelsLibrary.{ modelsDirectory, modelIndex }
import NativeLibs.nativeLibs
import NetLogoBuild.{ all, buildDate, marketingVersion, numericMarketingVersion }
import Running.makeMainTask
import java.nio.file.Paths
import java.nio.file.Files
import scala.sys.process.Process

object NetLogoPackaging {

  lazy val aggregateJDKParser      = settingKey[State => Parser[BuildJDK]]("parser for packageApp settings")
  lazy val aggregateOnlyFiles      = taskKey[Seq[File]]("Files to be included in the aggregate root")
  lazy val buildNetLogo            = taskKey[Unit]("build NetLogo")
  lazy val buildVariables          = taskKey[Map[String, String]]("NetLogo template variables")
  lazy val buildDownloadPages      = taskKey[Seq[File]]("package the web download pages")
  lazy val configRoot              = settingKey[File]("configuration directory")
  lazy val resaveModels            = taskKey[Unit]("prep models library for packaging")
  lazy val buildMathematicaLink    = taskKey[Unit]("build and package Mathematica Link submodule")
  lazy val generateLocalWebsite    = taskKey[File]("package the web download pages")
  lazy val localSiteTarget         = settingKey[File]("directory into which local copy of the site is built")
  lazy val mathematicaRoot         = settingKey[File]("root of Mathematica-Link directory")
  lazy val netLogoLongVersion      = settingKey[String]("Long version number (including trailing zero) of NetLogo under construction")
  lazy val netLogoRoot             = settingKey[File]("Root directory of NetLogo project")
  lazy val packagedMathematicaLink = taskKey[File]("Mathematica link, ready for packaging")
  lazy val packageLinuxAggregate   = inputKey[File]("package all linux apps into a single directory")
  lazy val packageMacAggregate     = inputKey[File]("package all mac apps into a dmg")
  lazy val packageWinAggregate     = inputKey[File]("package all win apps into a single directory")
  lazy val packagingClasspath      = taskKey[Seq[File]]("Jars to include when packaging")
  lazy val packagingMainJar        = taskKey[File]("Main jar to use when packaging")
  lazy val uploadWebsite           = inputKey[Unit]("upload the web download pages to the ccl server")
  lazy val uploadDocs              = inputKey[Unit]("Upload the web docs pages only to the CCL server")
  lazy val webTarget               = settingKey[File]("location of finished website")

  def bundledDirs(netlogo: Project, behaviorsearchProject: Project): Def.Initialize[(String, String) => Seq[BundledDirectory]] =
    Def.setting {
      { (platform: String, arch: String) =>
        val nlDir = (netlogo / baseDirectory).value
        Seq(
          new ExtensionDir((netlogo / extensionRoot).value, platform, arch),
          new ModelsDir((netlogo / modelsDirectory).value),
          new DocsDir((netlogo / docsRoot).value),
          new BehaviorsearchDir((behaviorsearchProject / baseDirectory).value, platform)
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

  def settings(netlogo: Project, macApp: Project, behaviorsearchProject: Project): Seq[Setting[?]] = Seq(
    netLogoRoot     := (netlogo / baseDirectory).value,
    mathematicaRoot := netLogoRoot.value.getParentFile / "Mathematica-Link",
    configRoot      := baseDirectory.value / "configuration",
    localSiteTarget := target.value / marketingVersion.value,
    aggregateJDKParser := Def.toSParser(jdkParser),
    netLogoLongVersion := { if (marketingVersion.value.length == 3) marketingVersion.value + ".0" else marketingVersion.value },

    buildNetLogo := {
      (netlogo / all).value
      (netlogo / allDocs).value
      (netlogo / allPreviews).toTask("").value
      resaveModels.value
      buildMathematicaLink.value
      (behaviorsearchProject / Compile / packageBin).value
    },

    resaveModels := {
      makeMainTask("org.nlogo.tools.ModelResaver",
        classpath = (netlogo / Test / Keys.fullClasspath),
        workingDirectory = baseDirectory(_.getParentFile)).toTask("").value
    },

    resaveModels := (resaveModels dependsOn (netlogo / extensions)).value,

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
        case (name, f) => name.replace("Installer", "Size") ->
            ((f.length / 1000000).toString + " MB")
      }

      val vars = buildVariables.value ++ downloadSizes ++
        downloadLocations.map(t => (t._1, t._2.getName))
      Mustache.betweenDirectories(webSource, webTarget.value,
        Map("index" -> "NetLogo {{version}} Downloads"), vars)
    },

    generateLocalWebsite := {
      FileActions.copyDirectory(webTarget.value, localSiteTarget.value)
      FileActions.copyDirectory((netlogo / modelsDirectory).value, localSiteTarget.value / "models")
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
      (netlogo / allDocs).value
      RunProcess(Seq("rsync", "-rltv", "--inplace", "--progress", sourceDir.getPath, s"$user@$host:$targetDir"), "rsync docs")
      RunProcess(Seq("rsync", "-rltv", "--inplace", "--progress", manualSource.getPath, s"$user@$host:$manualTarget"), "rsync user manual")
      RunProcess(Seq("ssh", s"$user@$host", "chgrp", "-R", "apache", targetDir), "ssh - change release group")
      RunProcess(Seq("ssh", s"$user@$host", "chmod", "-R", "g+rwX", targetDir), "ssh - change release permissions")
    },

    packagingClasspath := {
      val allDeps = (netlogo / Runtime / dependencyClasspath).value ++
        (behaviorsearchProject / Runtime / dependencyClasspath).value

      (removeJdkLibraries(filterDuplicateDeps(allDeps)).files :+
        (behaviorsearchProject / Compile / packageBin).value)
        .filterNot(jarExcluded)
        .filterNot(_.isDirectory) :+ packagingMainJar.value
    },

    packagingMainJar := {
      (netlogo / Compile / packageBin).value
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

      // $APPDIR on Linux is `./lib/app`, so move two levels up for the extra dirs
      val extraJavaOptions = Seq(
        "-Dnetlogo.extensions.dir=$APPDIR/../../extensions"
      , "-Dnetlogo.models.dir=$APPDIR/../../models"
      , "-Dnetlogo.docs.dir=$APPDIR/../../docs"
      , s"-Djava.library.path=$$APPDIR/../../natives/linux-${buildJDK.nativesArch}"
      )
      val mainLauncher = new NetLogoLauncher(version, None, extraJavaOptions)
      val launchers    = Seq(
        // Linux apps usually avoid spaces in directories and binary names, so we follow along.  -Jeremy B September
        // 2022
        new NetLogo3dLauncher(version, None, extraJavaOptions) { override def id: String = "NetLogo3D" }
      , new HubNetClientLauncher(version, None, extraJavaOptions) { override def id: String = "HubNetClient" }
      , new BehaviorsearchLauncher(version, None, extraJavaOptions)
      )

      val inputDir = JavaPackager.setupAppImageInput(log, version, buildJDK, buildDir, netLogoJar, dependencies)
      val destDir  = buildDir / s"${platform}-dest-${buildJDK.version}-${buildJDK.arch}"
      FileActions.remove(destDir)
      val appImageDir = JavaPackager.generateAppImage(log, buildJDK.jpackage.getAbsolutePath, platform, mainLauncher, configDir, buildDir, inputDir, destDir, Seq(), launchers)

      val extraDirs = bundledDirs(netlogo, behaviorsearchProject).value(platform, buildJDK.arch)
      JavaPackager.copyExtraFiles(log, extraDirs, platform, buildJDK.arch, appImageDir, appImageDir, rootFiles)
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
        (behaviorsearchProject / baseDirectory).value / "resources" / "Behaviorsearch.ico",
        (behaviorsearchProject / baseDirectory).value / "resources" / "behaviorsearch_model.ico",
        configDir / "windows" / "NetLogo.ico",
        configDir / "windows" / "HubNet Client.ico",
        configDir / "windows" / "model.ico"
      )

      icons.foreach(i => FileActions.copyFile(i, buildDir / i.getName))

      // $APPDIR on Windows is `./app`, so move one levels up for the extra dirs
      val extraJavaOptions = Seq(
        "-Dnetlogo.extensions.dir=$APPDIR/../extensions"
      , "-Dnetlogo.models.dir=$APPDIR/../models"
      , "-Dnetlogo.docs.dir=$APPDIR/../docs"
      , s"-Djava.library.path=$$APPDIR/../natives/windows-${buildJDK.nativesArch}"
      )
      val mainLauncher = new NetLogoLauncher(version, Some("NetLogo.ico"), extraJavaOptions, Seq("icon="))
      val launchers = Seq(
        new NetLogoLauncher(version, Some("NetLogo.ico"), extraJavaOptions, Seq("win-console=true")) {
          override def id = "NetLogo_Console"
          override def mustachePrefix = "win-console-launcher"
        }
      , new NetLogo3dLauncher(version, Some("NetLogo3D.ico"), extraJavaOptions)
      , new HubNetClientLauncher(version, Some("HubNet Client.ico"), extraJavaOptions)
      , new BehaviorsearchLauncher(version, Some("Behaviorsearch.ico"), extraJavaOptions)
      )

      val inputDir  = JavaPackager.setupAppImageInput(log, version, buildJDK, buildDir, netLogoJar, dependencies)
      val destDir   = buildDir / s"${platform}-dest-${buildJDK.version}-${buildJDK.arch}"
      val extraArgs = Seq("--icon", "NetLogo.ico")
      FileActions.remove(destDir)
      val appImageDir = JavaPackager.generateAppImage(log, buildJDK.jpackage.getAbsolutePath, platform, mainLauncher, configDir, buildDir, inputDir, destDir, extraArgs, launchers)

      val extraDirs = bundledDirs(netlogo, behaviorsearchProject).value(platform, buildJDK.arch)
      JavaPackager.copyExtraFiles(log, extraDirs, platform, buildJDK.arch, appImageDir, appImageDir, rootFiles)
      JavaPackager.createScripts(log, appImageDir, appImageDir / "app", configDir / platform, "netlogo-headless.bat", "netlogo-gui.bat", variables)

      // clean up unwanted icon files
      FileActions.listDirectory(appImageDir.toPath).foreach(path => {
        if (path.toString.endsWith(".ico"))
          FileActions.remove(path.toFile)
      })

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

    packageMacAggregate := {
      val log          = streams.value.log
      val version      = marketingVersion.value
      val buildJDK     = aggregateJDKParser.parsed
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
      , configDir / "macosx" / "NetLogo3D.icns"
      , configDir / "macosx" / "HubNet Client.icns"
      , configDir / "macosx" / "Model.icns"
      )
      icons.foreach( (i) => FileActions.copyFile(i, buildDir / i.getName) )

      val extraJavaOptions = Seq(
        "-Dapple.awt.graphics.UseQuartz=true"
      , "--add-exports=java.desktop/com.apple.laf=ALL-UNNAMED"
      // See comment in `PackageMacAggregate` for more info on the `{{{ROOTDIR}}}` placeholder.  -Jeremy B September
      // 2022
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
        , extraJavaOptions ++ Seq(
            "-Xdock:name=NetLogo"
          , "-Dorg.nlogo.mac.appClassName=org.nlogo.app.App$"
          )
        , Seq()
        , Some("netlogo-mac-app.jar")
        , Some("org.nlogo.app.MacApplication")
        ) {
          override def name = s"NetLogo ${this.version}"
        }
      , new NetLogo3dLauncher(
          version
        , Some("NetLogo3D.icns")
        , extraJavaOptions ++ Seq(
            "\"-Xdock:name=NetLogo 3D\""
          , "-Dorg.nlogo.mac.appClassName=org.nlogo.app.App$"
          )
        , Seq()
        , Some("netlogo-mac-app.jar")
        , Some("org.nlogo.app.MacApplication")
        ) {
          override def name = s"NetLogo 3D ${this.version}"
        }
      , new HubNetClientLauncher(
          version
        , Some("HubNet Client.icns")
        , extraJavaOptions ++ Seq(
            "-Xdock:name=HubNet"
          , "-Dorg.nlogo.mac.appClassName=org.nlogo.hubnet.client.App$"
          , "-Dapple.laf.useScreenMenuBar=true"
         )
        , Seq()
        , Some("netlogo-mac-app.jar")
        , Some("org.nlogo.app.MacApplication")
        ) {
          override def name = s"HubNet Client ${this.version}"
        }
      , new BehaviorsearchLauncher(
          version
        , Some("Behaviorsearch.icns")
        , extraJavaOptions ++ Seq(
            "-Xdock:name=Behaviorsearch"
          , "-Dorg.nlogo.mac.appClassName=bsearch.fx.MainGUIEntry"
          , "-Dbsearch.appfolder={{{ROOTDIR}}}/behaviorsearch"
          , "-Dbsearch.startupfolder={{{ROOTDIR}}}"
          )
        ) {
          override def name = s"Behaviorsearch ${this.version}"
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
        val extraArgs  = Seq("--icon", launcher.icon.getOrElse(""))
        val appPackage = JavaPackager.generateAppImage(log, "jpackage", platform, launcher, configDir, buildDir, inputDir, destDir, extraArgs, Seq())
        FileActions.copyFile(configDir / "macosx" / "Model.icns", destDir / s"${launcher.name}.app" / "Contents" / "Resources" / "Model.icns")
      })

      val appImageDir = destDir / s"NetLogo ${version}"
      FileActions.remove(appImageDir)
      val extraDirs = bundledDirs(netlogo, behaviorsearchProject).value(platform, buildJDK.arch)
      JavaPackager.copyExtraFiles(log, extraDirs, platform, buildJDK.arch, appImageDir, appImageDir, rootFiles)
      val bundleDir = PackageMacAggregate.createBundleDir(log, version, destDir, configDir, launchers)
      JavaPackager.createScripts(log, bundleDir, bundleDir / "app", configDir, "netlogo-headless.sh", "netlogo-gui.sh", variables + ("javaOptions" -> "--add-exports=java.desktop/com.apple.laf=ALL-UNNAMED"))

      // clean up unwanted icon files
      FileActions.listDirectory(appImageDir.toPath).foreach(path => {
        if (path.toString.endsWith(".icns"))
          FileActions.remove(path.toFile)
      })

      PackageMacAggregate(
        log
      , version
      , buildJDK.arch
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
          args.map(_.replace(" ", "\\ ")).mkString(" "))
      }
    }
  }
}

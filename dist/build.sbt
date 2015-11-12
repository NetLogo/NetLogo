import sbt._
import sbt.complete.Parser, Parser._

import java.util.jar.Attributes.Name.{ CLASS_PATH => JAR_CLASS_PATH }

import java.io.File

import DistSettings._

val bootCp = System.getProperty("java.home") + "/lib/rt.jar"

lazy val jfxPackageOptions = taskKey[Package.ManifestAttributes]("Manifest attributes marking package for javapackager")

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
    ) ++ DistSettings.settings

lazy val macApp = project.in(file("mac-app")).
  settings(sharedAppProjectSettings: _*).
  settings(
    name                                  := "NetLogo-Mac-App",
    artifactPath in Compile in packageBin := target.value / "netlogo-mac-app.jar")

DistSettings.settings

netLogoRoot := baseDirectory.value.getParentFile

// this value is unfortunately dependent upon both the platform and the application
val appMainClass: PartialFunction[(String, String), String] = {
  case ("windows" | "linux", "NetLogo" | "NetLogo 3D" | "NetLogo Logging") => "org.nlogo.app.App"
  case ("macosx",            "NetLogo" | "NetLogo 3D" | "NetLogo Logging") => "org.nlogo.app.MacApplication"
  case (_,                   "HubNet Client")                              => "org.nlogo.hubnet.client.App"
}

lazy val subAppMap: Map[String, SubApplication] =
  Map("NetLogo"         -> NetLogoCoreApp,
      "NetLogo 3D"      -> NetLogoThreeDApp,
      "NetLogo Logging" -> NetLogoLoggingApp,
      "HubNet Client"   -> HubNetClientApp)

lazy val platformMap: Map[String, PlatformBuild] =
  Map("linux"  -> LinuxPlatform,
      "mac"    -> new MacPlatform(macApp),
      "macimg" -> new MacImagePlatform(macApp),
      "win"    -> WindowsPlatform)

lazy val appParser: Parser[SubApplication] = subAppMap.map(t => t._1 ^^^ t._2).reduceLeft(_ | _)

lazy val platformParser: Parser[PlatformBuild] = platformMap.map(t => t._1 ^^^ t._2).reduceLeft(_ | _)

lazy val packageApp = inputKey[File]("package specifief app on specified platform")

lazy val packageAction: Def.Initialize[Task[((PlatformBuild, SubApplication)) => Def.Initialize[Task[File]]]] =
  Def.task[((PlatformBuild, SubApplication)) => Def.Initialize[Task[File]]] {
  { (platform: PlatformBuild, app: SubApplication) =>
    Def.bind(platform.mainJarAndDependencies(app)) { jarTask =>
      Def.task {
        val (mainJar, dependencies) = jarTask.value
        val distDir         = baseDirectory.value
        val netLogoDir      = netLogoRoot.value
        val buildDirectory  = target.value / app.name / platform.shortName
        val artifactsDir    = buildDirectory / "out" / "artifacts"
        val outputDirectory = buildDirectory / "target"
        IO.delete(buildDirectory)
        IO.createDirectory(buildDirectory)

        val variables = Map(
          "appName"        -> app.name,
          "version"        -> "5.2.2",
          "buildDirectory" -> buildDirectory.getAbsolutePath
        )
        ConfigurationFiles.writeConfiguration(platform, app, distDir / "configuration", buildDirectory, variables)

        def repathFile(originalBase: File)(f: File): File = {
          val Some(relativeFile) = f relativeTo originalBase
          new java.io.File(artifactsDir, relativeFile.getPath)
        }

        val copiedBundleFiles: Seq[(File, File)] =
          platform.bundledDirs.flatMap { bd =>
            val files = bd.files(netLogoDir / bd.directoryName)
            files zip files.map(repathFile(netLogoDir))
          }

        val jarMap = {
          val allJars = (mainJar +: dependencies)
          allJars zip allJars.map(f => artifactsDir / f.getName)
        }

        val copyLogging = (distDir / "netlogo_logging.xml", artifactsDir / "netlogo_logging.xml")

        val allFileCopies: Seq[(File, File)] = jarMap ++ copiedBundleFiles :+ copyLogging

        IO.copy(allFileCopies)

        val allFiles: Seq[File] = allFileCopies.map(_._2)

        JavaPackager(appMainClass(platform.shortName, app.name), platform, app,
          srcDir = artifactsDir, srcFiles = allFiles, outDir = outputDirectory, buildDirectory = buildDirectory, mainJar = mainJar)
      }
    }
  }.tupled
}

packageApp <<= InputTask.createDyn(Def.setting((s: State) => " " ~> (platformParser <~ " ") ~ appParser))(packageAction)

lazy val packageLinuxAggregate = taskKey[File]("package all linux apps into a single directory")

packageLinuxAggregate <<= packageAppAggregate("linux", AggregateLinuxBuild.apply _)

lazy val packageMacAggregate = taskKey[File]("package all mac apps into a single directory")

packageMacAggregate <<= packageAppAggregate("macimg", AggregateMacBuild.apply _)

def packageAppAggregate(platformName: String, aggregatePackager: (File, Map[SubApplication, File]) => File): Def.Initialize[Task[File]] = {
  val subApps = Seq(NetLogoCoreApp, NetLogoThreeDApp, NetLogoLoggingApp, HubNetClientApp)
  val app = subApps.head

  type AppPair = (SubApplication, File)

  def appTuple(app: SubApplication): Def.Initialize[Task[AppPair]] =
    Def.map(packageApp.toTask(s" $platformName " + app.name))(_.map(f => (app -> f)))

  // this is a mess. both Task and Initialize have applicative instances, so it should
  // be possible to make this conversion easier, but I couldn't get the type params to work out
  val tuples: Def.Initialize[Task[Seq[(SubApplication, File)]]] =
    subApps.map(appTuple).foldLeft(Def.task(Seq.empty[AppPair])) {
      case (st, tt) => Def.bind(st)((st: Task[Seq[AppPair]]) =>
          Def.map(tt)((t: Task[AppPair]) =>
              st.flatMap((seq: Seq[AppPair]) =>
                  t.map((tv: AppPair) => seq :+ tv))))

    }

  val appMap: Def.Initialize[Task[Map[SubApplication, File]]] =
    Def.map(tuples)((appPairTask: Task[Seq[AppPair]]) => appPairTask.map(ts => Map(ts: _*)))

  Def.task(aggregatePackager(target.value, appMap.value))
}

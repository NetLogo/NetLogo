import ModelIndex.modelsDirectory
import Extensions.extensionRoot

lazy val all = TaskKey[Unit]("all", "build everything!!!")
lazy val threed = TaskKey[Unit]("threed", "enable NetLogo 3D")
lazy val nogen = TaskKey[Unit]("nogen", "disable bytecode generator")

lazy val root = project.in(file("netlogo-gui"))
   .settings(Defaults.coreDefaultSettings ++
             Testing.settings ++
             Packaging.settings ++
             Running.settings ++
             Dump.settings ++
             Scaladoc.settings ++
             ChecksumsAndPreviews.settings ++
             Extensions.extensionsTask ++
             InfoTab.infoTabTask ++
             ModelIndex.modelIndexTask ++
             NativeLibs.nativeLibsTask ++
             Depend.dependTask: _*)
  .settings(
    scalaVersion := "2.11.7",
    organization := "org.nlogo",
    name := "NetLogo",
    onLoadMessage := "",
    resourceDirectory in Compile := baseDirectory.value / "resources",
    scalacOptions ++=
      "-deprecation -unchecked -feature -Xfatal-warnings -Xcheckinit -encoding us-ascii"
      .split(" ").toSeq,
    javacOptions ++=
      "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.8 -target 1.8"
      .format(java.io.File.pathSeparator)
      .split(" ").toSeq,
    // only log problems plz
    ivyLoggingLevel := UpdateLogging.Quiet,
    // this makes jar-building and script-writing easier
    retrieveManaged := true,
    // we're not cross-building for different Scala versions
    crossPaths := false,
    scalaSource in Compile := baseDirectory.value / "src" / "main",
    scalaSource in Test    := baseDirectory.value / "src" / "test",
    javaSource in Compile  := baseDirectory.value / "src" / "main",
    javaSource in Test     := baseDirectory.value / "src" / "test",
    javaOptions in run ++= Seq(
      "-Dnetlogo.models.dir="     + modelsDirectory.value.getAbsolutePath.toString,
      "-Dnetlogo.extensions.dir=" + extensionRoot.value.getAbsolutePath.toString,
      "-Dnetlogo.docs.dir="       + file("docs").getAbsolutePath.toString
    ),
    javaOptions in run ++= (
      if (System.getProperty("os.name").contains("Mac"))
        Seq(
          "-Dapple.awt.graphics.UseQuartz=true",
          "-Dnetlogo.quaqua.laf=ch.randelshofer.quaqua.snowleopard.Quaqua16SnowLeopardLookAndFeel",
          "-Dapple.awt.showGrowBox=true",
          "-Dapple.laf.useScreenMenuBar=true")
      else
        Seq()),
    unmanagedSourceDirectories in Test      += baseDirectory.value / "src" / "tools",
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
    mainClass in (Compile, run)        := Some("org.nlogo.app.App"),
    mainClass in (Compile, packageBin) := Some("org.nlogo.app.App"),
    sourceGenerators in Compile += EventsGenerator.task.taskValue,
    sourceGenerators in Compile += JFlexRunner.task.taskValue,
    resourceGenerators in Compile <+= I18n.resourceGeneratorTask,
    threed := { System.setProperty("org.nlogo.is3d", "true") },
    nogen  := { System.setProperty("org.nlogo.noGenerator", "true") },
    modelsDirectory := file("models"),
    extensionRoot := file("extensions"),
    libraryDependencies ++= Seq(
      "org.ow2.asm" % "asm-all" % "5.0.3",
      "org.picocontainer" % "picocontainer" % "2.13.6",
      "log4j" % "log4j" % "1.2.16",
      "javax.media" % "jmf" % "2.1.1e",
      "org.pegdown" % "pegdown" % "1.5.0",
      "org.parboiled" % "parboiled-java" % "1.0.2",
      "steveroy" % "mrjadapter" % "1.2"            from "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/mrjadapter-1.2.jar",
      "org.jhotdraw" % "jhotdraw" % "6.0b1"        from "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/jhotdraw-6.0b1.jar",
      "ch.randelshofer" % "quaqua" % "9.1"         from "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/quaqua-9.1.jar",
      "ch.randelshofer" % "swing-layout" % "9.1"   from "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/swing-layout-9.1.jar",
      "com.jogamp" % "jogl" % "2.3.2"              from "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/jogl-all-2.3.2.jar",
      "com.jogamp" % "gluegen-rt" % "2.3.2"        from "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/gluegen-rt-2.3.2.jar",
      "org.jmock" % "jmock" % "2.5.1" % "test",
      "org.jmock" % "jmock-legacy" % "2.5.1" % "test",
      "org.jmock" % "jmock-junit4" % "2.5.1" % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.2" % "test",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.apache.httpcomponents" % "httpclient" % "4.2",
      "org.apache.httpcomponents" % "httpmime" % "4.2",
      "com.googlecode.json-simple" % "json-simple" % "1.1.1"
    ),
    all <<= (baseDirectory, streams) map { (base, s) =>
      s.log.info("making resources/system/dict.txt and docs/dict folder")
      IO.delete(base / "docs" / "dict")
      Process("python bin/dictsplit.py").!!
    },
    all <<= all.dependsOn(
      packageBin in Test,
      Extensions.extensions,
      NativeLibs.nativeLibs,
      ModelIndex.modelIndex,
      InfoTab.infoTab,
      Scaladoc.docSmaller))

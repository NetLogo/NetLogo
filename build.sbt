import org.scalajs.sbtplugin.cross.{ CrossProject, CrossType }
import org.scalastyle.sbt.ScalastylePlugin.scalastyleTarget
import ModelIndex.modelsDirectory
import Extensions.{ excludedExtensions, extensionRoot }

lazy val root =
   project.in(file(".")).
   aggregate(netlogo, parserJVM)

lazy val netlogo = project.in(file("netlogo-gui"))
   .dependsOn(parserJVM)
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
    javaOptions in Test ++= Seq(
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
    unmanagedResourceDirectories in Compile ++= (unmanagedResourceDirectories in Compile in sharedResources).value,
    mainClass in (Compile, run)        := Some("org.nlogo.app.App"),
    mainClass in (Compile, packageBin) := Some("org.nlogo.app.App"),
    sourceGenerators in Compile += EventsGenerator.task.taskValue,
    sourceGenerators in Compile += JFlexRunner.task.taskValue,
    resourceGenerators in Compile <+= I18n.resourceGeneratorTask,
    threed := { System.setProperty("org.nlogo.is3d", "true") },
    nogen  := { System.setProperty("org.nlogo.noGenerator", "true") },
    modelsDirectory := file("models"),
    extensionRoot := file("extensions").getAbsoluteFile,
    libraryDependencies ++= Seq(
      "org.ow2.asm" % "asm-all" % "5.0.4",
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
    unmanagedSourceDirectories in Compile += file(".").getAbsoluteFile / "netlogo-core" / "src" / "main",
    excludeFilter in unmanagedSources in Compile := {
      (excludeFilter in unmanagedSources in Compile).value ||
      new SimpleFileFilter({ f =>
        f.getParentFile.getPath.contains((file("netlogo-core") / "src" / "main").getPath) &&
          (baseDirectory.value / "src" / "main" / f.getParentFile.getName / f.getName).exists
      })
    },
    // includes all classes from parserJVM in the finished jar
    products in Compile in packageBin ++= (products in Compile in parserJVM).value,
    mappings in Compile in packageBin ~= { mappings =>
      mappings.filterNot(t => t._2 == "version.txt" && t._1.getPath.contains("parser-jvm")) },
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

lazy val commonSettings = Seq(
  organization := "org.nlogo",
  licenses += ("GPL-2.0", url("http://opensource.org/licenses/GPL-2.0")),
  scalacOptions ++=
    "-deprecation -unchecked -feature -Xcheckinit -encoding us-ascii -target:jvm-1.7 -Xlint -Xfatal-warnings"
      .split(" ").toSeq,
  scalaSource in Compile := baseDirectory.value / "src" / "main",
  scalaSource in Test := baseDirectory.value / "src" / "test",
  ivyLoggingLevel := UpdateLogging.Quiet,
  logBuffered in testOnly in Test := false,
  onLoadMessage := "",
  scalaVersion := "2.11.7",
  // don't cross-build for different Scala versions
  crossPaths := false,
  scalastyleTarget in Compile := {
    file("target") / s"scalastyle-result-${name.value}.xml"
  }
)

lazy val jvmSettings = Seq(
  javacOptions ++=
    "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.8 -target 1.8"
    .split(" ").toSeq,
  javaSource in Compile := baseDirectory.value / "src" / "main",
  javaSource in Test := baseDirectory.value / "src" / "test",
  publishArtifact in Test := true
)

lazy val scalatestSettings = Seq(
  // show test failures again at end, after all tests complete.
  // T gives truncated stack traces; change to G if you need full.
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oT"),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.1" % "test",
    // Using a 1.12.2 until fix is available for https://github.com/rickynils/scalacheck/issues/173
    "org.scalacheck" %% "scalacheck" % "1.12.2" % "test"
  )
)

lazy val testSettings = scalatestSettings ++ Seq(
  libraryDependencies ++= Seq(
    "org.jmock" % "jmock" % "2.8.1" % "test",
    "org.jmock" % "jmock-legacy" % "2.8.1" % "test",
    "org.jmock" % "jmock-junit4" % "2.8.1" % "test",
    "org.reflections" % "reflections" % "0.9.10" % "test",
    "org.slf4j" % "slf4j-nop" % "1.7.12" % "test"
  )
)

lazy val publicationSettings =
  bintrayPublishSettings ++
  Seq(
    bintray.Keys.repository in bintray.Keys.bintray := "NetLogoHeadless",
    bintray.Keys.bintrayOrganization in bintray.Keys.bintray := Some("netlogo")
  )

lazy val docOptions = Seq(
  netlogoVersion := {
    (testLoader in Test).value
      .loadClass("org.nlogo.api.Version")
      .getMethod("version")
      .invoke(null).asInstanceOf[String]
      .stripPrefix("NetLogo ")
  },
  scalacOptions in (Compile, doc) ++= {
    Seq("-encoding", "us-ascii") ++
    Opts.doc.title("NetLogo") ++
    Opts.doc.version(version.value) ++
    Opts.doc.sourceUrl("https://github.com/NetLogo/NetLogo/blob/" +
      version.value + "/src/main€{FILE_PATH}.scala")
  },
  sources in (Compile, doc) ++= (sources in (parserJVM, Compile)).value,
  // compensate for issues.scala-lang.org/browse/SI-5388
  doc in Compile := {
    val path = (doc in Compile).value
    for (file <- Process(Seq("find", path.toString, "-name", "*.html")).lines)
      IO.write(
        new File(file),
        IO.read(new File(file)).replaceAll("\\.java\\.scala", ".java"))
    path
  }
)

lazy val parserSettings: Seq[Setting[_]] = Seq(
  isSnapshot := true,
  name := "parser",
  version := "0.0.1",
  unmanagedSourceDirectories in Compile += file(".").getAbsoluteFile / "parser-core" / "src" / "main",
  unmanagedSourceDirectories in Test += file(".").getAbsoluteFile / "parser-core" / "src" / "test"
)

lazy val sharedResources = (project in file ("shared")).
  settings(commonSettings: _*).
  settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "resources" / "main")

// these projects exist only to allow the code contained therein to be scalastyled
lazy val parserCore = (project in file("parser-core")).
  settings(commonSettings: _*).
  settings(skip in (Compile, compile) := true)

lazy val netlogoCore = (project in file("netlogo-core")).
  settings(commonSettings: _*).
  settings(
    scalaSource in Compile := baseDirectory.value / "src" / "main",
    scalaSource in Test    := baseDirectory.value / "src" / "test",
    javaSource in Compile  := baseDirectory.value / "src" / "main",
    javaSource in Test     := baseDirectory.value / "src" / "test",
    skip in (Compile, compile) := true)

lazy val macros = (project in file("macros")).
  dependsOn(sharedResources).
  settings(commonSettings: _*).
  settings(libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value)

lazy val parser = CrossProject("parser", file("."),
  new CrossType {
    override def projectDir(crossBase: File, projectType: String): File =
      crossBase / s"parser-$projectType"
    override def sharedSrcDir(projectBase: File, conf: String): Option[File] =
      Some(projectBase / "parser-core" / "src" / conf)
  }).
  settings(commonSettings: _*).
  settings(parserSettings: _*).
  jsConfigure(_.dependsOn(sharedResources % "compile-internal->compile")).
  jsConfigure(_.dependsOn(macros % "compile-internal->compile;test-internal->compile")).
  jsSettings(publicationSettings: _*).
  jsSettings(
      name := "parser-js",
      ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
      resolvers += Resolver.sonatypeRepo("releases"),
      libraryDependencies ++= {
      import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.toScalaJSGroupID
        Seq(
          "org.scala-js"  %%%! "scala-parser-combinators" % "1.0.2",
          "org.scalatest" %%%! "scalatest" % "3.0.0-M15" % "test",
          "org.scalacheck" %%%! "scalacheck" % "1.12.5" % "test"
      )}).
  jvmConfigure(_.dependsOn(sharedResources)).
  jvmSettings(jvmSettings: _*).
  jvmSettings(scalatestSettings: _*).
  jvmSettings(
      mappings in (Compile, packageBin) ++= mappings.in(sharedResources, Compile, packageBin).value,
      mappings in (Compile, packageSrc) ++= mappings.in(sharedResources, Compile, packageSrc).value,
      libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3"
    )

lazy val parserJVM = parser.jvm
lazy val parserJS  = parser.js

lazy val headless = (project in file ("netlogo-headless")).
  dependsOn(parserJVM % "test-internal->test;compile-internal->compile").
  settings(commonSettings: _*).
  settings(jvmSettings: _*).
  settings(testSettings: _*).
  settings(Testing.settings: _*).
  settings(docOptions: _*).
  settings(Depend.dependTask: _*).
  settings(Extensions.extensionsTask: _*).
  settings(publicationSettings: _*).
  settings(
    version := "6.0",
    isSnapshot := true,
    libraryDependencies += "org.ow2.asm" % "asm-all" % "5.0.4",
    (fullClasspath in Runtime) ++= (fullClasspath in Runtime in parserJVM).value,
    mainClass in Compile := Some("org.nlogo.headless.Main"),
    onLoadMessage := "",
    name := "NetLogoHeadless",
    extensionRoot := file("extensions"),
    mappings in (Compile, packageBin) ++= mappings.in(parserJVM, Compile, packageBin).value,
    mappings in (Compile, packageSrc) ++= mappings.in(parserJVM, Compile, packageSrc).value,
    unmanagedSourceDirectories in Compile += file(".").getAbsoluteFile / "netlogo-core" / "src" / "main",
    unmanagedResourceDirectories in Compile += (baseDirectory in parserJVM).value / "resources" / "main",
    unmanagedResourceDirectories in Test += baseDirectory.value / "resources" / "test",
    excludedExtensions := Seq("arduino", "bitmap", "gis", "gogo", "nw", "palette", "sound")
  )

lazy val netlogoVersion = taskKey[String]("from api.Version")
lazy val all = TaskKey[Unit]("all", "build everything!!!")
lazy val threed = TaskKey[Unit]("threed", "enable NetLogo 3D")
lazy val nogen = TaskKey[Unit]("nogen", "disable bytecode generator")


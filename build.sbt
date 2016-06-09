import org.scalajs.sbtplugin.cross.{ CrossProject, CrossType }
import org.scalastyle.sbt.ScalastylePlugin.scalastyleTarget
import ModelsLibrary.modelsDirectory
import Extensions.{ excludedExtensions, extensionRoot }
import NetLogoBuild.{ all, autogenRoot, cclArtifacts, includeProject, marketingVersion, numericMarketingVersion, netlogoVersion, shareSourceDirectory }
import Docs.htmlDocs
import Testing.testTempDirectory


// these settings are common to ALL BUILDS
// if it doesn't belong in every build, it can't go in here
lazy val commonSettings = Seq(
  organization          := "org.nlogo",
  licenses              += ("GPL-2.0", url("http://opensource.org/licenses/GPL-2.0")),
  javaSource in Compile := baseDirectory.value / "src" / "main",
  javaSource in Test    := baseDirectory.value / "src" / "test",
  onLoadMessage         := "",
  testTempDirectory     := (baseDirectory.value.getParentFile / "tmp").getAbsoluteFile,
  ivyLoggingLevel       := UpdateLogging.Quiet
)

// These settings are common to all builds involving scala
// Any scala-specific settings should change here (and thus for all projects at once)
lazy val scalaSettings = Seq(
  scalaVersion           := "2.11.8",
  scalaSource in Compile := baseDirectory.value / "src" / "main",
  scalaSource in Test    := baseDirectory.value / "src" / "test",
  crossPaths             := false, // don't cross-build for different Scala versions
  scalacOptions ++=
    "-deprecation -unchecked -feature -Xcheckinit -encoding us-ascii -target:jvm-1.8 -Xlint -Xfatal-warnings"
      .split(" ").toSeq
)

// These settings are common to all builds that compile against Java
// Any java-specific settings should change here (and thus for all java projects at once)
lazy val jvmSettings = Seq(
  javaSource in Compile   := baseDirectory.value / "src" / "main",
  javaSource in Test      := baseDirectory.value / "src" / "test",
  publishArtifact in Test := true,
  javacOptions ++=
    "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.8 -target 1.8"
    .split(" ").toSeq
)

// These are scalatest-specific settings
// Any scalatest-specific settings should change here
lazy val scalatestSettings = Seq(
  // show test failures again at end, after all tests complete.
  // T gives truncated stack traces; change to G if you need full.
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oS"),
  logBuffered in testOnly in Test := false,
  libraryDependencies ++= Seq(
    "org.scalatest"  %% "scalatest"  % "2.2.6"  % "test",
    // scalatest doesn't yet play nice with scalacheck 1.13.0
    "org.scalacheck" %% "scalacheck" % "1.12.5" % "test"
  )
)

lazy val mockDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.jmock" % "jmock" % "2.8.1" % "test",
    "org.jmock" % "jmock-legacy" % "2.8.1" % "test",
    "org.jmock" % "jmock-junit4" % "2.8.1" % "test",
    "org.reflections" % "reflections" % "0.9.10" % "test",
    "org.slf4j" % "slf4j-nop" % "1.7.12" % "test"
  )
)

lazy val scalastyleSettings = Seq(
  scalastyleTarget in Compile := {
  baseDirectory.value.getParentFile / "target" / s"scalastyle-result-${name.value}.xml"
  })

def publicationSettings(repository: String) =
  Seq(
    bintrayRepository   := repository,
    bintrayOrganization := Some("netlogo"))

lazy val root =
   project.in(file(".")).
   aggregate(netlogo, parserJVM)

lazy val netlogo = project.in(file("netlogo-gui")).
   dependsOn(parserJVM).
   settings(commonSettings: _*).
   settings(jvmSettings: _*).
   settings(scalaSettings: _*).
   settings(scalatestSettings: _*).
   settings(NetLogoBuild.settings: _*).
   settings(JFlexRunner.settings: _*).
   settings(EventsGenerator.settings: _*).
   settings(Docs.settings: _*).
   settings(includeProject(parserJVM): _*).
   settings(publicationSettings("NetLogo-JVM"): _*).
   settings(shareSourceDirectory("netlogo-core"): _*).
   settings(Defaults.coreDefaultSettings ++
             Testing.settings ++
             Packaging.settings ++
             Running.settings ++
             Dump.settings ++
             Scaladoc.settings ++
             ChecksumsAndPreviews.settings ++
             Extensions.settings ++
             InfoTab.infoTabTask ++
             ModelsLibrary.settings ++
             NativeLibs.nativeLibsTask ++
             GUISettings.settings ++
             Depend.dependTask: _*).
  settings(
    name := "NetLogo",
    version := "6.0.0-M8",
    isSnapshot := false,
    mainClass in Compile := Some("org.nlogo.app.App"),
    modelsDirectory := baseDirectory.value.getParentFile / "models",
    extensionRoot   := (baseDirectory.value.getParentFile / "extensions").getAbsoluteFile,
    autogenRoot     := baseDirectory.value.getParentFile / "autogen",
    javaOptions     += "-Dnetlogo.docs.dir=" + (baseDirectory.value.getParentFile / "docs").getAbsolutePath.toString,
    unmanagedSourceDirectories in Test      += baseDirectory.value / "src" / "tools",
    resourceDirectory in Compile            := baseDirectory.value / "resources",
    unmanagedResourceDirectories in Compile ++= (unmanagedResourceDirectories in Compile in sharedResources).value,
    resourceGenerators in Compile <+= I18n.resourceGeneratorTask,
    threed := { System.setProperty("org.nlogo.is3d", "true") },
    nogen  := { System.setProperty("org.nlogo.noGenerator", "true") },
    libraryDependencies ++= Seq(
      "org.ow2.asm" % "asm-all" % "5.0.4",
      "org.picocontainer" % "picocontainer" % "2.13.6",
      "log4j" % "log4j" % "1.2.16",
      "javax.media" % "jmf" % "2.1.1e",
      "org.pegdown" % "pegdown" % "1.5.0",
      "org.parboiled" %% "parboiled-scala" % "1.1.7",
      "org.jogamp.jogl" % "jogl-all" % "2.3.2",
      "org.jogamp.gluegen" % "gluegen-rt" % "2.3.2",
      "org.jhotdraw" % "jhotdraw" % "6.0b1"      from cclArtifacts("jhotdraw-6.0b1.jar"),
      "org.jmock" % "jmock" % "2.5.1" % "test",
      "org.jmock" % "jmock-legacy" % "2.5.1" % "test",
      "org.jmock" % "jmock-junit4" % "2.5.1" % "test",
      "org.apache.httpcomponents" % "httpclient" % "4.2",
      "org.apache.httpcomponents" % "httpmime" % "4.2",
      "com.googlecode.json-simple" % "json-simple" % "1.1.1"
    ),
    all := {},
    all <<= all.dependsOn(
      htmlDocs,
      packageBin in Test,
      Extensions.extensions,
      NativeLibs.nativeLibs,
      ModelsLibrary.modelIndex,
      Scaladoc.apiScaladoc)
  )

lazy val threed = TaskKey[Unit]("threed", "enable NetLogo 3D")
lazy val nogen = TaskKey[Unit]("nogen", "disable bytecode generator")

lazy val headless = (project in file ("netlogo-headless")).
  dependsOn(parserJVM % "test-internal->test;compile-internal->compile").
  settings(scalaSettings: _*).
  settings(scalastyleSettings: _*).
  settings(jvmSettings: _*).
  settings(scalatestSettings: _*).
  settings(mockDependencies: _*).
  settings(Scaladoc.settings: _*).
  settings(Testing.settings: _*).
  settings(Depend.dependTask: _*).
  settings(Extensions.settings: _*).
  settings(publicationSettings("NetLogoHeadless"): _*).
  settings(JFlexRunner.settings: _*).
  settings(includeProject(parserJVM): _*).
  settings(shareSourceDirectory("netlogo-core"): _*).
  settings(
    name          := "NetLogoHeadless",
    version       := "6.0",
    isSnapshot    := true,
    autogenRoot   := (baseDirectory.value.getParentFile / "autogen").getAbsoluteFile,
    extensionRoot := baseDirectory.value.getParentFile / "extensions",
    mainClass in Compile         := Some("org.nlogo.headless.Main"),
    nogen                        := { System.setProperty("org.nlogo.noGenerator", "true") },
    libraryDependencies          += "org.ow2.asm" % "asm-all" % "5.0.4",
    libraryDependencies          += "org.parboiled" %% "parboiled-scala" % "1.1.7",
    (fullClasspath in Runtime)   ++= (fullClasspath in Runtime in parserJVM).value,
    resourceDirectory in Compile := baseDirectory.value / "resources" / "main",
    resourceDirectory in Test    := baseDirectory.value / "resources" / "test",
    excludedExtensions           := Seq("arduino", "bitmap", "csv", "gis", "gogo", "nw", "palette", "sound"),
    all := { val _ = (
      (packageBin in Compile).value,
      (packageBin in Test).value,
      (compile in Test).value,
      Extensions.extensions
    )}
  )

// this project exists as a wrapper for the mac-specific NetLogo components
lazy val macApp = project.in(file("mac-app")).
  settings(commonSettings: _*).
  settings(jvmSettings: _*).
  settings(scalaSettings: _*).
  settings(JavaPackager.mainArtifactSettings: _*).
  settings(NativeLibs.cocoaLibsTask).
  settings(Running.settings).
  settings(
    fork in run                           := true,
    name                                  := "NetLogo-Mac-App",
    compile in Compile                    <<= (compile in Compile) dependsOn (packageBin in Compile in netlogo),
    unmanagedJars in Compile              += (packageBin in Compile in netlogo).value,
    libraryDependencies                   ++= Seq(
      "net.java.dev.jna" % "jna" % "4.2.2",
      "ca.weblite" % "java-objc-bridge" % "1.0.0"),
    libraryDependencies                   ++= (libraryDependencies in netlogo).value,
    libraryDependencies                   ++= (libraryDependencies in parserJVM).value,
    run in Compile                        <<= (run in Compile) dependsOn NativeLibs.cocoaLibs,
    javaOptions in run                    += "-Djava.library.path=" + (Seq(
      baseDirectory.value / "natives" / "macosx-universal" / "libjcocoa.dylib") ++
      ((baseDirectory in netlogo).value / "natives" / "macosx-universal" * "*.jnilib").get).mkString(":"),
    artifactPath in Compile in packageBin := target.value / "netlogo-mac-app.jar",
    javacOptions                          ++= Seq("-bootclasspath", System.getProperty("java.home") + "/lib/rt.jar"))

// this project is all about packaging NetLogo for distribution
lazy val dist = project.in(file("dist")).
  settings(NetLogoPackaging.settings(netlogo, macApp): _*).
  settings(NetLogoBuild.settings: _*)

lazy val sharedResources = (project in file ("shared")).
  settings(commonSettings: _*).
  settings(scalaSettings: _*).
  settings(scalastyleSettings: _*).
  settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "resources" / "main")

lazy val macros = (project in file("macros")).
  dependsOn(sharedResources).
  settings(commonSettings: _*).
  settings(scalaSettings: _*).
  settings(scalastyleSettings: _*).
  settings(libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value)

lazy val parser = CrossProject("parser", file("."),
  new CrossType {
    override def projectDir(crossBase: File, projectType: String): File =
      crossBase / s"parser-$projectType"
    override def sharedSrcDir(projectBase: File, conf: String): Option[File] =
      Some(projectBase / "parser-core" / "src" / conf)
  }).
  settings(commonSettings: _*).
  settings(scalaSettings: _*).
  settings(scalastyleSettings: _*).
  settings(
    isSnapshot := true,
    name := "parser",
    version := "0.0.1",
    unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile / "parser-core" / "src" / "main",
    unmanagedSourceDirectories in Test    += baseDirectory.value.getParentFile / "parser-core" / "src" / "test").
  jsConfigure(_.dependsOn(sharedResources % "compile-internal->compile")).
  jsConfigure(_.dependsOn(macros % "compile-internal->compile;test-internal->compile")).
  jsSettings(publicationSettings("NetLogoHeadless"): _*).
  jsSettings(
      name := "parser-js",
      ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
      resolvers += Resolver.sonatypeRepo("releases"),
      libraryDependencies ++= {
      import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.toScalaJSGroupID
        Seq(
          "org.scala-js"   %%%! "scala-parser-combinators" % "1.0.2",
          "org.scalatest"  %%%! "scalatest" % "3.0.0-M15" % "test",
          // scalatest doesn't yet play nice with scalacheck 1.13.0
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

// only exists for scalastyling
lazy val parserCore = (project in file("parser-core")).
  settings(scalastyleSettings: _*).
  settings(skip in (Compile, compile) := true)

// only exists for scalastyling
lazy val netlogoCore = (project in file("netlogo-core")).
  settings(scalastyleSettings: _*).
  settings(skip in (Compile, compile) := true)

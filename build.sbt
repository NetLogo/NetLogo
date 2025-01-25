import org.scalajs.linker.interface.ESVersion

import sbtcrossproject.CrossPlugin.autoImport.{ crossProject, CrossType }
import sbtcrossproject.Platform

import ModelsLibrary.modelsDirectory
import Extensions.{ excludedExtensions, extensionRoot }
import NetLogoBuild.{ all, autogenRoot, cclArtifacts, includeInPackaging,
  marketingVersion, numericMarketingVersion, netlogoVersion, shareSourceDirectory }
import Docs.htmlDocs
import Dump.dumpClassName
import Testing.testTempDirectory

// these settings are common to ALL BUILDS
// if it doesn't belong in every build, it can't go in here
lazy val commonSettings = Seq(
  organization          := "org.nlogo",
  licenses              += ("GPL-2.0", url("http://opensource.org/licenses/GPL-2.0")),
  Compile / javaSource  := baseDirectory.value / "src" / "main",
  Test / javaSource     := baseDirectory.value / "src" / "test",
  onLoadMessage         := "",
  testTempDirectory     := (baseDirectory.value.getParentFile / "tmp").getAbsoluteFile,
  ivyLoggingLevel       := UpdateLogging.Quiet,
  Compile / console / scalacOptions := scalacOptions.value.filterNot(_ == "-Xlint")
)

// These settings are common to all builds involving scala
// Any scala-specific settings should change here (and thus for all projects at once)
lazy val scalaSettings = Seq(
  scalaVersion          := "2.12.18",
  Compile / scalaSource := baseDirectory.value / "src" / "main",
  Test / scalaSource    := baseDirectory.value / "src" / "test",
  crossPaths            := false, // don't cross-build for different Scala versions
  scalacOptions ++=
    "-deprecation -unchecked -feature -Xcheckinit -encoding us-ascii -release 11 -opt:l:method -Xlint -Xfatal-warnings"
      .split(" ").toSeq
)

// These settings are common to all builds that compile against Java
// Any java-specific settings should change here (and thus for all java projects at once)
lazy val jvmSettings = Seq(
  Compile / javaSource   := baseDirectory.value / "src" / "main",
  Test / javaSource      := baseDirectory.value / "src" / "test",
  Test / publishArtifact := true,
  javacOptions ++=
    "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path"
    .split(" ").toSeq,
  javaOptions ++=Seq(
    //  These add-exports are needed for JOGL
    "--add-exports", "java.base/java.lang=ALL-UNNAMED",
    "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED",
    "--add-exports", "java.desktop/sun.java2d=ALL-UNNAMED")
  )

// These are scalatest-specific settings
// Any scalatest-specific settings should change here
lazy val scalatestSettings = Seq(
  // show test failures again at end, after all tests complete.
  // T gives truncated stack traces; change to G if you need full.
  Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oS")
, Test / testOnly / logBuffered := false
, libraryDependencies ++= Seq(
    "org.scalatest"     %% "scalatest"       % "3.2.17"   % Test
  , "org.scalatestplus" %% "scalacheck-1-16" % "3.2.14.0" % Test
  )
  // This lets us mock up some Java library classes for testing.
  // -Jeremy B August 2022
, Test / javaOptions := { Seq(
    "--add-opens", "java.desktop/java.awt=ALL-UNNAMED"
  , "--add-opens", "java.base/java.io=ALL-UNNAMED"
  , "-Dapple.awt.graphics.UseQuartz=false"
  , s"-Dorg.nlogo.is3d=${System.getProperty("org.nlogo.is3d", "false")}"
  , s"-Dorg.nlogo.noGenerator=${System.getProperty("org.nlogo.noGenerator", "false")}"
  , s"-Dorg.nlogo.noOptimizer=${System.getProperty("org.nlogo.noOptimizer", "false")}"
  ) }
  // Tests must be forked to get the above `javaOptions`
, Test / fork := true
, threed := { System.setProperty("org.nlogo.is3d", "true") }
, nogen  := { System.setProperty("org.nlogo.noGenerator", "true") }
, noopt  := { System.setProperty("org.nlogo.noOptimizer", "true") }
)

lazy val flexmarkDependencies = {
  val flexmarkVersion = "0.20.2"
  Seq(
    libraryDependencies ++= Seq(
      "com.vladsch.flexmark" % "flexmark" % flexmarkVersion,
      "com.vladsch.flexmark" % "flexmark-ext-autolink" % flexmarkVersion,
      "com.vladsch.flexmark" % "flexmark-ext-escaped-character" % flexmarkVersion,
      "com.vladsch.flexmark" % "flexmark-ext-typographic" % flexmarkVersion,
      "com.vladsch.flexmark" % "flexmark-util" % flexmarkVersion
      )
    )
}

lazy val mockDependencies = {
  val mockVersion = "2.12.0"
  Seq(
    libraryDependencies ++= Seq(
      // replace byte-buddy as we get a "No code generation strategy found" with the older
      // designated version from jmock:  https://github.com/jmock-developers/jmock-library/issues/204
      // replace hamcrest as it just seems wrong in the jmock POM?
      // -Jeremy B August 2022
      "org.jmock"     % "jmock"        % mockVersion % "test"
        exclude ("net.bytebuddy", "byte-buddy")
        exclude ("org.hamcrest", "hamcrest")
    , "org.jmock"     % "jmock-legacy" % mockVersion % "test"
    , "org.jmock"     % "jmock-junit5" % mockVersion % "test"
    , "net.bytebuddy" % "byte-buddy"   % "1.12.23"   % "test"
    , "org.hamcrest"  % "hamcrest"     % "2.2"       % "test"
    )
  )
}

lazy val asmDependencies = {
  val asmVersion = "9.6"
  Seq(
    libraryDependencies ++= Seq(
      "org.ow2.asm" % "asm"         % asmVersion,
      "org.ow2.asm" % "asm-commons" % asmVersion,
      "org.ow2.asm" % "asm-util"    % asmVersion,
      )
    )
}

lazy val scalastyleSettings = Seq(
  Compile / scalastyleTarget := {
    baseDirectory.value.getParentFile / "target" / s"scalastyle-result-${name.value}.xml"
  })

lazy val root =
  project.in(file(".")).
  aggregate(netlogo, parserJVM)

lazy val netlogo = project.in(file("netlogo-gui")).
  dependsOn(parserJVM % "test->test;compile->compile").
  settings(NetLogoBuild.settings: _*).
  settings(includeInPackaging(parserJVM): _*).
  settings(shareSourceDirectory("netlogo-core"): _*).
  settings(commonSettings: _*).
  settings(jvmSettings: _*).
  settings(scalaSettings: _*).
  settings(scalatestSettings: _*).
  settings(JFlexRunner.settings: _*).
  settings(EventsGenerator.settings: _*).
  settings(Docs.settings: _*).
  settings(flexmarkDependencies).
  settings(mockDependencies: _*).
  settings(asmDependencies).
  settings(Defaults.coreDefaultSettings ++
          Testing.settings ++
          Testing.useLanguageTestPrefix("org.nlogo.headless.Test") ++
          Packaging.settings ++
          Running.settings ++
          Dump.settings ++
          //Scaladoc.settings ++
          ChecksumsAndPreviews.settings ++
          Extensions.settings ++
          InfoTab.infoTabTask ++
          ModelsLibrary.settings ++
          NativeLibs.nativeLibsTask ++
          NetLogoWebExport.settings ++
          GUISettings.settings ++
          Depend.dependTask: _*).
  settings(
    name := "NetLogo",
    version := "7.0.0",
    isSnapshot := true,
    publishTo := { Some("Cloudsmith API" at "https://maven.cloudsmith.io/netlogo/netlogo/") },
    Compile / mainClass := Some("org.nlogo.app.App"),
    javacOptions   ++= Seq("--release", "11"),
    modelsDirectory := baseDirectory.value.getParentFile / "models",
    extensionRoot   := (baseDirectory.value.getParentFile / "extensions").getAbsoluteFile,
    autogenRoot     := baseDirectory.value.getParentFile / "autogen",
    Test / unmanagedSourceDirectories      += baseDirectory.value / "src" / "tools",
    Compile / resourceDirectory            := baseDirectory.value / "resources",
    Compile / unmanagedResourceDirectories ++= (sharedResources / Compile / unmanagedResourceDirectories).value,
    libraryDependencies ++= Seq(
      "com.formdev" % "flatlaf" % "3.4",
      "org.picocontainer" % "picocontainer" % "2.15",
      "javax.media" % "jmf" % "2.1.1e",
      "commons-codec" % "commons-codec" % "1.16.0",
      "org.parboiled" %% "parboiled" % "2.5.0",
      "org.jogamp.jogl" % "jogl-all" % "2.4.0" from "https://jogamp.org/deployment/archive/rc/v2.4.0/jar/jogl-all.jar",
      "org.jogamp.gluegen" % "gluegen-rt" % "2.4.0" from "https://jogamp.org/deployment/archive/rc/v2.4.0/jar/gluegen-rt.jar",
      "org.jhotdraw" % "jhotdraw" % "6.0b1" % "provided,optional" from cclArtifacts("jhotdraw-6.0b1.jar"),
      "org.apache.httpcomponents" % "httpclient" % "4.2",
      "org.apache.httpcomponents" % "httpmime" % "4.2",
      "com.googlecode.json-simple" % "json-simple" % "1.1.1",
      "com.fifesoft" % "rsyntaxtextarea" % "3.3.0",
      "com.typesafe" % "config" % "1.4.3",
      "net.lingala.zip4j" % "zip4j" % "2.11.5"
    ),
    all := {},
    all := {
      all.dependsOn(
        htmlDocs,
        Test / packageBin,
        Extensions.extensions,
        NativeLibs.nativeLibs,
        ModelsLibrary.modelIndex,
        Compile / doc
      ).value
    }
  , Test / baseDirectory := baseDirectory.value.getParentFile
  // I don't think this `apiMappings` setup works anymore for the JDK.  I'm not going to worry about it because it
  // hasn't worked in the last few releases, no one has noticed, and the JDK docs are easy enough to lookup outside of
  // NetLogo's docs.  There is a `-jdk-api-doc-base` option in Scala 2.13+, and a more generalized/powerful
  // `-external-mappings` in 3+.  We can attempt to get this working again once one of those updates is completed.
  // -Jeremy B March 2023
  , apiMappings += (file(System.getenv("JAVA_HOME") + "/jre/lib/rt.jar") -> url("https://docs.oracle.com/javase/17/docs/api"))
  , autoAPIMappings := true
  , Compile / doc / target := baseDirectory.value / "docs" / "scaladoc"
  , Compile / doc / scalacOptions :=
    Seq(
      "-no-link-warnings"
    , "-encoding", "us-ascii"
    , "-sourcepath", baseDirectory.value.getParentFile.getAbsolutePath
    , "-doc-title", "NetLogo"
    , "-doc-version", netlogoVersion.value
    , "-skip-packages", Scaladoc.excludedPackages.mkString(":")
    , "-doc-source-url", s"https://github.com/NetLogo/NetLogo/blob/${netlogoVersion.value}€{FILE_PATH_EXT}"
    )
  )

lazy val threed = TaskKey[Unit]("threed", "enable NetLogo 3D")
lazy val nogen = TaskKey[Unit]("nogen", "disable bytecode generator")
lazy val noopt = TaskKey[Unit]("noopt", "disable compiler optimizations")

lazy val headless = (project in file ("netlogo-headless")).
  dependsOn(parserJVM % "test-internal->test;compile-internal->compile").
  enablePlugins(org.nlogo.build.PublishVersioned).
  settings(commonSettings: _*).
  settings(scalaSettings: _*).
  settings(scalastyleSettings: _*).
  settings(jvmSettings: _*).
  settings(scalatestSettings: _*).
  settings(mockDependencies: _*).
  settings(asmDependencies).
  //settings(Scaladoc.settings: _*).
  settings(Testing.settings: _*).
  settings(Testing.useLanguageTestPrefix("org.nlogo.headless.lang.Test"): _*).
  settings(Depend.dependTask: _*).
  settings(Extensions.settings: _*).
  settings(JFlexRunner.settings: _*).
  settings(includeInPackaging(parserJVM): _*).
  settings(shareSourceDirectory("netlogo-core"): _*).
  settings(Dump.settings: _*).
  settings(ChecksumsAndPreviews.settings: _*).
  settings(
    name          := "NetLogoHeadless",
    version       := "7.0.0",
    isSnapshot := true,
    publishTo     := { Some("Cloudsmith API" at "https://maven.cloudsmith.io/netlogo/netlogo/") },
    autogenRoot   := (baseDirectory.value.getParentFile / "autogen").getAbsoluteFile,
    extensionRoot := baseDirectory.value.getParentFile / "extensions",
    javacOptions ++= Seq("--release", "11"),
    Compile / mainClass         := Some("org.nlogo.headless.Main"),
    libraryDependencies        ++= Seq(
      "org.parboiled" %% "parboiled" % "2.5.0",
      "commons-codec" % "commons-codec" % "1.16.0",
      "com.typesafe" % "config" % "1.4.3",
      "net.lingala.zip4j" % "zip4j" % "2.11.5",
      "org.reflections" % "reflections" % "0.9.10" % "test",
    ),
    (Runtime / fullClasspath)  ++= (parserJVM / Runtime / fullClasspath).value,
    Compile / resourceDirectory := baseDirectory.value / "resources" / "main",
    Compile / unmanagedResourceDirectories ++= (sharedResources / Compile / unmanagedResourceDirectories).value,
    Test / resourceDirectory    := baseDirectory.value.getParentFile / "test",
    dumpClassName               := "org.nlogo.headless.misc.Dump",
    excludedExtensions          := Seq("arduino", "bitmap", "csv", "gis", "gogo", "ls", "nw", "palette", "py", "sound", "time", "vid", "view2.5d"),
    all := { val _ = (
      (Compile / packageBin).value,
      (Test / packageBin).value,
      (Test / compile).value,
      Extensions.extensions
    )}
  , Test / baseDirectory := baseDirectory.value.getParentFile
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
    Compile / run / mainClass           := Some("org.nlogo.app.MacApplication"),
    // all other projects can use `--release 11`, but since this one uses `--add-exports`
    // for a system library it is incompatible.  So we let it target 17, as it will only
    // use the bundled Java.  -Jeremy B August 2022
    javacOptions ++= Seq("-source", "17", "-target", "17"),
    run / fork                          := true,
    name                                := "NetLogo-Mac-App",
    Compile / compile                   := {
      ((Compile / compile) dependsOn (netlogo / Compile / packageBin)).value
    },
    Compile / unmanagedJars             += (netlogo / Compile / packageBin).value,
    libraryDependencies                ++= Seq(
      "net.java.dev.jna" % "jna" % "4.2.2",
      "ca.weblite" % "java-objc-bridge" % "1.0.0"),
    libraryDependencies                ++= (netlogo / libraryDependencies).value,
    libraryDependencies                ++= (parserJVM/ libraryDependencies).value,
    Compile / run                       := {
      ((Compile / run) dependsOn NativeLibs.cocoaLibs).evaluated
    },
    run / javaOptions                   += "-Djava.library.path=" + (Seq(
      baseDirectory.value / "natives" / "macosx-universal" / "libjcocoa.dylib") ++
      ((netlogo / baseDirectory).value / "natives" / "macosx-universal" * "*.jnilib").get).mkString(":"),
    Compile / packageBin / artifactPath := target.value / "netlogo-mac-app.jar",
    javacOptions ++= Seq("-bootclasspath", System.getProperty("java.home") + "/lib/rt.jar",
//  Needed because MacTabbedPaneUI uses com.apple.laf.AquaTabbedPaneContrastUI
    "--add-exports", "java.desktop/com.apple.laf=ALL-UNNAMED"))

// this project is all about packaging NetLogo for distribution
lazy val dist = project.in(file("dist")).
  settings(NetLogoBuild.settings: _*).
  settings(NetLogoPackaging.settings(netlogo, macApp, behaviorsearchProject): _*)

lazy val sharedResources = (project in file ("shared")).
  settings(commonSettings: _*).
  settings(scalaSettings: _*).
  settings(scalastyleSettings: _*).
  settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources" / "main")

lazy val macros = (project in file("macros")).
  dependsOn(sharedResources).
  settings(commonSettings: _*).
  settings(scalaSettings: _*).
  settings(scalastyleSettings: _*).
  settings(libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value)

lazy val parser = crossProject(JSPlatform, JVMPlatform).
  crossType(new CrossType {
    override def projectDir(crossBase: File, projectType: String): File =
      crossBase / s"parser-$projectType"
    override def projectDir(crossBase: File, platform: Platform): File =
      projectDir(crossBase, if (platform == JSPlatform) "js" else "jvm")
    override def sharedSrcDir(projectBase: File, conf: String): Option[File] =
      Some(projectBase / "parser-core" / "src" / conf)
  }).
  in(file(".")).
  settings(commonSettings: _*).
  settings(scalaSettings: _*).
  settings(scalastyleSettings: _*).
  settings(
    isSnapshot := true,
    name := "parser",
    publishTo := { Some("Cloudsmith API" at "https://maven.cloudsmith.io/netlogo/netlogo/") },
    version := "0.4.0",
    Compile / unmanagedSourceDirectories += baseDirectory.value.getParentFile / "parser-core" / "src" / "main",
    Test / unmanagedSourceDirectories    += baseDirectory.value.getParentFile / "parser-core" / "src" / "test").
  jsConfigure(_.dependsOn(sharedResources % "compile-internal->compile")).
  jsConfigure(_.dependsOn(         macros % "compile-internal->compile;test-internal->compile")).
  jsSettings(
    name := "parser-js",
    scalaModuleInfo := scalaModuleInfo.value map { _.withOverrideScalaVersion(true) },
    resolvers ++= Resolver.sonatypeOssRepos("releases"),
    Test / parallelExecution := false,
    scalaJSLinkerConfig ~= { _.withESFeatures(_.withESVersion(ESVersion.ES2018)) },
    libraryDependencies ++= {
      import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
      Seq(
        "org.scala-lang.modules" %%% "scala-parser-combinators" %    "2.1.1"
      ,          "org.scalatest" %%%                "scalatest" %   "3.2.17" % Test
      ,      "org.scalatestplus" %%%          "scalacheck-1-16" % "3.2.14.0" % Test
      )
    }).
  jvmConfigure(_.dependsOn(sharedResources % "compile-internal->compile")).
  jvmSettings(jvmSettings: _*).
  jvmSettings(scalatestSettings: _*).
  jvmSettings(
    javacOptions ++= Seq("--release", "11"),
    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
    // you can get these included by just depending on the `sharedResources` project directly
    // but then when you publish the parser JVM package, the POM file lists sharedResources
    // as a dependency.  It seems like a weird thing to publish separately, so this just
    // gets them jammed into the jar of the parser for use (as NetLogo does, too).
    // -Jeremy B May 2022
    Compile / unmanagedResourceDirectories ++= (sharedResources / Compile / unmanagedResourceDirectories).value,
    Compile / resourceGenerators           ++= (sharedResources / Compile / resourceGenerators).value
  )

lazy val parserJVM = parser.jvm
lazy val parserJS  = parser.js

// only exists for scalastyling
lazy val parserCore = (project in file("parser-core")).
  settings(scalastyleSettings: _*).
  settings(Compile / compile / skip := true)

// only exists for scalastyling
lazy val netlogoCore = (project in file("netlogo-core")).
  settings(scalastyleSettings: _*).
  settings(Compile / compile / skip := true)

// only exists for packaging
lazy val behaviorsearchProject: Project =
  project.in(file("behaviorsearch"))
    .dependsOn(netlogo % "test-internal->test;compile-internal->compile")
    .settings(description := "subproject of NetLogo")

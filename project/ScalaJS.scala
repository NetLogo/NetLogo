import sbt._
import Keys._

import ScalaJSSourceMaps.catJSFilesAndTheirSourceMaps

object ScalaJS {

  object ScalaJSKeys {
    val packageJS     = TaskKey[File]("package-js")
    val compileJS     = TaskKey[inc.Analysis]("compile-js")
    val scalaJSFiles  = TaskKey[Compiler.Inputs]("scala-js-files")
    val scalaJSSrcDir = SettingKey[File]("scala-js-src-dir")
    val scalaJSOutDir = SettingKey[File]("scala-js-out-dir")
  }

  import ScalaJSKeys._

  val baseScalaJSSettings: Seq[Setting[_]] = Seq(

    // you had better use the same version of Scala as Scala.js
    scalaVersion := "2.10.1",

    scalaJSFiles <<= (sourceDirectory in Compile, streams, compileInputs in Compile, scalaJSSrcDir in Compile, scalaJSOutDir in Compile) map {
      (base, s, inputs, srcDir, output) =>
        val files = (srcDir ** "*.scala").get
        Compiler.inputs(Seq(), files, output, Seq(), Seq(), 0, xsbti.compile.CompileOrder.Mixed)(inputs.compilers, inputs.incSetup, s.log)
    },

    compileJS in Compile <<= (compile in Compile, javaHome, streams, compileInputs in Compile, scalaJSFiles in Compile) map {

      (_, javaHome, s, inputs, scalaJS) =>

      import scalaJS.config._

      val logger = s.log
      logger.info("Compiling %d Scala.js sources to %s...".format(sources.size, classesDirectory))

      val (compilerCpStr, cpStr) = {
        val compileCP      = inputs.config.classpath
        val compilerJars   = compileCP filter isCompilerJar
        val scalaJSClasses = scalaJS.config.classpath ++ (compileCP filter (_.getName.startsWith("scalajs-library")))
        (cpToString(compilerJars), cpToString(scalaJSClasses))
      }

      val sourcesArgs = sources.map(_.getAbsolutePath).toList
      val compile     = doCompileJS(_: List[String], javaHome, classesDirectory, compilerCpStr, cpStr, options, logger)

      val isWindows         = System.getProperty("os.name").toLowerCase.contains("win")
      val tooLongForWindows = isWindows && (sourcesArgs.map(_.length).sum > 1536)

      if (tooLongForWindows)
        IO.withTemporaryFile("sourcesargs", ".txt") { sourceListFile =>
          IO.writeLines(sourceListFile, sourcesArgs)
          compile(List("@" + sourceListFile.getAbsolutePath))
        }
      else
        compile(sourcesArgs)

      // We do not have dependency analysis for Scala.js code
      sbt.inc.Analysis.Empty

    },

    packageJS in Compile <<= (
        compileJS in Compile, classDirectory in Compile,
        crossTarget in Compile, moduleName
    ) map { (_, classDir, target, modName) =>
      val allJSFiles = (classDir ** "*.js").get
      val output     = target / (modName + ".js")
      catJSFilesAndTheirSourceMaps(allJSFiles, output)
      output
    }

  )

  val scalaJSSettings: Seq[Setting[_]] =
    baseScalaJSSettings ++ Seq(
      scalaVersion := "2.10.1",
      libraryDependencies ++= Seq(
        "ch.epfl.lamp" %% "scalajs-compiler" % "0.1-SNAPSHOT" from "http://ccl.northwestern.edu/devel/scalajs-compiler_2.10-0.1-SNAPSHOT.jar",
        "ch.epfl.lamp" %% "scalajs-library" % "0.1-SNAPSHOT" from "http://ccl.northwestern.edu/devel/scalajs-library_2.10-0.1-SNAPSHOT.jar",
        "org.scala-lang" % "scala-compiler" % "2.10.1"
      )
    )

  private def isCompilerJar(item: File): Boolean = {
    val compilerModuleNames = Seq("scala-library", "scala-compiler", "scala-reflect", "scalajs-compiler")
    val name = item.getName
    name.endsWith(".jar") && compilerModuleNames.exists(name.startsWith)
  }

  private def cpToString(cp: Seq[File]) =
    cp.map(_.getAbsolutePath).mkString(java.io.File.pathSeparator)

  private def doCompileJS(sourcesArgs: List[String], javaHome: Option[File], dir: File, compilerClasspath: String,
                          classpath: String, options: Seq[String], logger: Logger): Option[String] =
    Run.executeTrapExit({
      dir.mkdir()
      val args = Seq(
        "-Xbootclasspath/a:" + compilerClasspath,
        "-Xmx512M", "scala.tools.nsc.scalajs.Main",
        "-cp", classpath, "-d", dir.getAbsolutePath
      ) ++ options ++ sourcesArgs ++ Seq("-language:_")
      Fork.java(javaHome, args, logger)
    }, logger)

}

import sbt._
import Keys._

object Coffee {

  private val npmCmd = "npm"

  val version = "1.6.3"

  lazy val settings = Seq(
    resourceGenerators in Compile <+= coffee,
    watchSources <++= coffeeSources
  )

  lazy val coffeeSourceDir = Def.setting[File] {
    baseDirectory.value / "src" / "main" / "coffee"
  }

  lazy val coffeeSources = Def.task[Seq[File]] {
    IO.listFiles(coffeeSourceDir.value)
      .filter(_.getName.endsWith(".coffee"))
      .toSeq
  }

  lazy val coffeeCompiler = Def.setting[File] {
    val node = baseDirectory.value / "node_modules"
    val compiler = node / "coffee-script" / "bin" / "coffee"
    def coffeeInstalled(): Boolean = {
      util.Try(Process(Seq(compiler.toString, "--version")).lines.head).toOption ==
        Some(s"CoffeeScript version $version")
    }
    if (!coffeeInstalled()) {
      assert(Process(Seq(npmCmd, "-v")).!!.matches("""\s*\d+\.\d+\.\d+\s*"""), "NPM not installed or on $PATH")
      IO.createDirectory(node)
      Process(Seq(npmCmd, "install", s"coffee-script@$version")).!
      assert(coffeeInstalled(), s"couldn't install/run CoffeeScript $version")
    }
    compiler
  }

  lazy val coffee = Def.task[Seq[File]] {
    val outDir = (resourceManaged in Compile).value / "js"
    IO.createDirectory(outDir)
    for (src <- coffeeSources.value)
    yield {
      val outName = src.getName.stripSuffix(".coffee") + ".js"
      val outPath = outDir / outName
      if (src.newerThan(outPath)) {
        streams.value.log.info(s"generating $outName")
        val cmd = Seq(coffeeCompiler.value.toString,
          "-b", "-c", "-o", outDir.toString, src.toString)
        val exitCode = Process(cmd).!
        assert(exitCode == 0,
          s"CoffeeScript compilation failed, exit code = $exitCode")
      }
      outPath
    }
  }

}

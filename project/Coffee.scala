import java.io.File
import sbt._
import Keys._

object Coffee {

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
    val expected = "1.6.3"
    def coffeeInstalled(): Boolean = {
      util.Try(Process(Seq(compiler.toString, "--version")).lines.head).toOption ==
        Some(s"CoffeeScript version $expected")
    }
    if (!coffeeInstalled()) {
      IO.createDirectory(node)
      Process(Seq("npm", "install", s"coffee-script@$expected")).!
      assert(coffeeInstalled(), s"couldn't install/run CoffeeScript $expected")
    }
    compiler
  }

  lazy val coffee = Def.task[Seq[File]] {
    val base = baseDirectory.value
    val outDir = (resourceManaged in Compile).value / "js"
    IO.createDirectory(outDir)
    for (src <- coffeeSources.value)
    yield {
      val outName = src.getName.stripSuffix(".coffee") + ".js"
      val outPath = outDir / outName
      if (src.newerThan(outPath)) {
        val info: String => Unit = streams.value.log.info(_)
        info(s"generating $outName")
        Process(Seq(coffeeCompiler.value.toString, "-b", "-c", "-o", outDir.toString, src.toString)).!
      }
      outPath
    }
  }

}

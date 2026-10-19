import java.nio.file.{ Files, Path, Paths }

name := "Mathematica-Link"

Compile / javaSource := baseDirectory.value / "src" / "main"

crossPaths := false

version := "7.0.0"

scalaVersion := "3.7.0"

javacOptions ++=
  "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path --release 11 ".split(" ").toSeq

val netLogoVersion = "7.0.0-beta2-7e8f7a4"

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  "mathematica-link.jar"
}

// this section is copied from the NetLogo extension plugin
val netLogoDep = {
  val netLogoJarFile =
    Option(System.getProperty("netlogo.jar.file"))
      .map { f =>
        val jar = file(f)
        val testJar = file(f.replaceAllLiterally(".jar", "-tests.jar"))
        Seq(Compile / unmanagedJars ++= Seq(jar, testJar))
      }

  val netLogoJarURL =
    Option(System.getProperty("netlogo.jar.url"))
      .map { url =>
        val urlVersion = url.split("/").last
          .stripPrefix("NetLogo")
          .stripPrefix("-")
          .stripSuffix(".jar")
          val version = if (urlVersion == "") "DEV" else urlVersion
          val testUrl = url.replaceAllLiterally(".jar", "-tests.jar")
          Seq(libraryDependencies ++= Seq(
            "org.nlogo" % "NetLogo" % version changing() from url,
            "org.nlogo" % "NetLogo-tests" % version changing() from testUrl,
            "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5"
          ))
      }

  (netLogoJarFile orElse netLogoJarURL).getOrElse {
    Seq(
      resolvers += "netlogo" at "https://dl.cloudsmith.io/public/netlogo/netlogo/maven/",
      libraryDependencies ++= Seq(
        "org.nlogo" % "netlogo" % netLogoVersion intransitive,
        "org.nlogo" % "netlogo" % netLogoVersion % "test" intransitive() classifier "tests"))
  }
}

netLogoDep

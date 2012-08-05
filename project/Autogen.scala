import java.io.File
import sbt._
import Keys._

object Autogen {

  val sourceGeneratorTask =
    (cacheDirectory, scalaSource in Compile, javaSource in Compile, baseDirectory, streams) map {
      (cacheDir, sdir, jdir, base, s) =>
        val cache =
          FileFunction.cached(cacheDir / "autogen", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              Set(events(s.log.info(_), base, sdir, "window"),
                  events(s.log.info(_), base, sdir, "app"),
                  flex(s.log.info(_), base, jdir, "agent", "ImportLexer"),
                  flex(s.log.info(_), base, jdir, "lex", "TokenLexer"))
          }
        cache(Set(base / "project" / "autogen" / "warning.txt",
                  base / "project" / "autogen" / "events.txt",
                  base / "project" / "autogen" / "ImportLexer.flex",
                  base / "project" / "autogen" / "TokenLexer.flex")).toSeq
    }

  def events(log: String => Unit, base: File, dir: File, ppackage: String): File = {
    val file = dir / "org" / "nlogo" / ppackage / "Events.scala"
    log("creating: " + file)

    var codeString = ""
    def append(s: String) = codeString += (s + "\n")

    append(IO.read(base / "project" / "autogen" / "warning.txt"))

    append("package org.nlogo." + ppackage + "\n")
    if(ppackage == "app")
      append("import org.nlogo.window.Event\n")
    append("object Events {")

    for{line <- IO.read(base /"project" / "autogen" / "events.txt").split("\n")
        if !line.trim.isEmpty // skip blank lines
        if !line.startsWith("#") // skip comment lines
        if line.startsWith(ppackage)} // skip unless in right package
      {
        val splitt = line.split("-").filter(!_.isEmpty)
        val name = splitt(0).split('.')(1).trim + "Event"
        val fieldString = splitt.drop(1).mkString("-")
        val fields = fieldString.split(""" \s*-\s* """).filter(!_.isEmpty).map(_.trim)
        append("  case class " + name + "(" +
          fields
            .map{_.split("""\s+""")}
            .collect{
              case Array(tpe, variable, _*) =>
                variable + ": " + tpe
            }.mkString(", ") + ") extends Event {")
        append("    override def beHandledBy(handler: AnyRef) {")
        append("      handler.asInstanceOf[" + name + "Handler].handle(this)")
        append("    }")
        append("  }")
        append("  trait " + name + "Handler {")
        append("    def handle(e: " + name + ")")
        append("  }")
      }
    append("}")
    IO.write(file, codeString)
    file
  }

  // this used to be broken into two tasks, but jflex doesnt seem to be threadsafe
  // so we have to run them serially, which means we have to generate them both each time. -JC 6/8/10
  def flex(log: String => Unit, base: File, dir: File, ppackage: String, kind: String): File = {
    val autogenFolder = base / "project" / "autogen"
    log("creating autogen/" + kind + ".java")
    JFlex.Main.main(Array("--quiet", (autogenFolder / (kind + ".flex")).asFile.toString))
    log("creating src/main/org/nlogo/" + ppackage + "/" + kind + ".java")
    val nlogoPackage = dir / "org" / "nlogo"
    val result = nlogoPackage / ppackage / (kind + ".java")
    IO.write(result,
      IO.read(autogenFolder / "warning.txt") +
      IO.read(autogenFolder / (kind + ".java")))
    (autogenFolder / (kind + ".java")).asFile.delete()
    result
  }

}

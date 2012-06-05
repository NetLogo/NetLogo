import java.io.File
import sbt._
import Keys._

object Autogen {

  val sourceGeneratorTask =
    (cacheDirectory, javaSource in Compile, baseDirectory, streams) map {
      (cacheDir, dir, base, s) => {
        val cache =
          FileFunction.cached(cacheDir / "autogen", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              Set(events(s.log.info(_), base, dir, "window"),
                  events(s.log.info(_), base, dir, "app"),
                  flex(s.log.info(_), base, dir, "agent", "ImportLexer"),
                  flex(s.log.info(_), base, dir, "lex", "TokenLexer"))
          }
        cache(Set(base / "project" / "autogen" / "warning.txt",
                  base / "project" / "autogen" / "events.txt",
                  base / "project" / "autogen" / "ImportLexer.flex",
                  base / "project" / "autogen" / "TokenLexer.flex")).toSeq
      }}

  def events(log: String => Unit, base: File, dir: File, ppackage: String): File = {
    val file = dir / "org" / "nlogo" / ppackage / "Events.java"
    log("creating: " + file)

    var codeString = ""
    def append(s: String) = codeString += (s + "\n")

    val (access, qualify) = ppackage match {
      case "window" => ("public ", "")
      case "app" => ("", "org.nlogo.window.")
    }

    append(IO.read(base / "project" / "autogen" / "warning.txt"))

    append("package org.nlogo." + ppackage + " ;")
    append("\n")
    append(access + "final strictfp class Events")
    append("{")
    append("    // not instantiable")
    append("    private Events() { throw new IllegalStateException() ; }")
    append("\n")

    for{line <- IO.read(base /"project" / "autogen" / "events.txt").split("\n")
        if !line.trim.isEmpty // skip blank lines
        if !line.startsWith("#") // skip comment lines
        if line.startsWith(ppackage)} // skip unless in right package
      {
        val splitt = line.split("-").filter(!_.isEmpty)
        val shortName = splitt(0).split('.')(1).trim
        val fieldString = splitt.drop(1).mkString("-")

        val name = shortName + "Event"
        append("    " + access + "static strictfp class " + name + " extends " + qualify + "Event")
        append("    {")
        val fields = fieldString.split(""" \s*-\s* """).filter(!_.isEmpty).map(_.trim)
        if (!fields.isEmpty) {
          for (field <- fields)
            append("        " + access + "final " + field + " ;")
          append("        " + access + name + fields.mkString("( ", " , ", " )"))
          append("        {")
          for (field <- fields) {
            val variable = field.split("""\s+""").drop(1)(0)
            append("            this." + variable + " = " + variable + " ;")
          }
          append("        }")
        }
        append("        @Override public void beHandledBy( " + qualify + "Event.Handler handler )")
        append("        {")
        append("            ( (Handler) handler ).handle( this ) ;")
        append("        }")
        append("        " + access + "interface Handler extends " + qualify + "Event.Handler")
        append("        {")
        append("            void handle( " + name + " e ) ;")
        append("        }")
        append("    }")
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

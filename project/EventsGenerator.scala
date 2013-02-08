import java.io.File
import sbt._
import Keys._

object EventsGenerator {

  val task =
    (cacheDirectory, javaSource in Compile, baseDirectory, streams) map {
      (cacheDir, dir, base, s) =>
        val cache =
          FileFunction.cached(cacheDir / "autogen", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              Set(events(s.log.info(_), base, dir, "window"),
                  events(s.log.info(_), base, dir, "app"))
          }
        cache(Set(base / "project" / "autogen" / "warning.txt",
                  base / "project" / "autogen" / "events.txt")).toSeq
    }

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

}

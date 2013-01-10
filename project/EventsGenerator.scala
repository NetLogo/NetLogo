import java.io.File
import sbt._
import Keys._

object EventsGenerator {

  val task =
    (cacheDirectory, scalaSource in Compile, baseDirectory, streams) map {
      (cacheDir, src, base, s) =>
        val cache =
          FileFunction.cached(cacheDir / "autogen", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              Set(events(s.log.info(_), base, src, "window"),
                  events(s.log.info(_), base, src, "app"))
          }
        cache(Set(base / "headless" / "project" / "warning.txt",
                  base / "project" / "events.txt")).toSeq
    }

  def events(log: String => Unit, base: File, dir: File, ppackage: String): File = {
    val file = dir / "org" / "nlogo" / ppackage / "Events.scala"
    log("generating " + file)

    var codeString = ""
    def append(s: String) = codeString += (s + "\n")

    append(IO.read(base / "headless" / "project" / "warning.txt"))

    append("package org.nlogo." + ppackage + "\n")
    append("import org.nlogo._")
    if(ppackage == "app")
      append("import window.Event")
    append("\nobject Events {")

    for{line <- IO.read(base /"project" / "events.txt").split("\n")
        if !line.trim.isEmpty // skip blank lines
        if !line.startsWith("#") // skip comment lines
        if line.startsWith(ppackage)} // skip unless in right package
      {
        val splitt = line.split("-").filter(!_.isEmpty)
        val name = splitt(0).split('.')(1).trim + "Event"
        val fieldString = splitt.drop(1).mkString("-")
        val fields = fieldString.split(""" \s*-\s* """).filter(!_.isEmpty).map(_.trim)
        append("  case class " + name + "(" + fields.mkString(", ") + ") extends Event {")
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

}

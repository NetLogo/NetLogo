#!/bin/sh
exec scala -classpath bin -deprecation -Dfile.encoding=UTF-8 "$0" "$@"
!#

// Check for problems with plain text files including:
// 1) No newline at end of file
// 2) Tab characters
// 3) Carriage return characters

import scala.collection.mutable.Buffer
import scala.io.Source
import scala.sys.process.Process

def ignore(path: String) =
  path.contains("/extensions/gis/") ||
  path.contains("/src_managed/") ||
  path.contains("/tmp/") ||
  path.endsWith("Lexer.java") ||
  path.endsWith("net-logo-web.html") ||
  path.startsWith("./.idea/") ||
  path.startsWith("./docs/scaladoc/") ||
  path.startsWith("./dist/i18n/")

// probably there are a lot more that could be here
val extensions =
  List("java", "scala", "py", "txt", "sh", "nlogo", "nlogo3d", "nlogox", "nlogox3d", "html", "css",
       "properties", "md", "csv", "asc", "prj", "xml")

val directories =
  List("extensions", "netlogo-gui", "netlogo-core", "netlogo-headless", "parser-core",
    "parser-js", "parser-jvm", "test")

def paths =
  Process("find" + directories.mkString(" ", " ", " ") + "! -ipath */target/* -and " + extensions.map("-name *." + _).mkString("( ", " -or ", " )")).lazyLines

for(path <- paths.filterNot(ignore)) {
  val contents = Source.fromFile(path).mkString
  val problems = Buffer[String]()
  if(contents.nonEmpty && contents.last != '\n')
    problems += "Missing newline at eof"
  if(contents.contains('\r'))
    problems += "Carriage return character(s) found"
  if(contents.contains('\t'))
    problems += "Tab character(s) found"
  if(problems.nonEmpty) {
    println(path)
    problems.foreach(p => println("  " + p))
  }
}

// Local Variables:
// mode: scala
// End:

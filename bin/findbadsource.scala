#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon -Dfile.encoding=UTF-8 "$0" "$@"
!#

// Check for problems with plain text files including:
// 1) No newline at end of file
// 2) Tab characters
// 3) Carriage return characters

import sys.process.Process
import collection.mutable.Buffer

def ignore(path: String) =
  path.contains("/src_managed/") ||
  path.contains("/tmp/") ||
  path.endsWith("Lexer.java") ||
  path.startsWith("./.idea/") ||
  path.startsWith("./docs/scaladoc/") ||
  path.startsWith("./dist/i18n/")

// probably there are a lot more that could be here
val extensions =
  List("java", "scala", "py", "txt", "sh", "nlogo", "nlogo3d", "html", "css",
       "properties", "md", "csv", "asc", "prj", "xml")

def paths =
  Process("find . " + extensions.map("-name *." + _).mkString(" -or ")).lines

for(path <- paths.filterNot(ignore)) {
  val contents = io.Source.fromFile(path).mkString
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

#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

// Check for problems with our Scala & Java source files including:
// 1) No newline at end of file
// 2) Tab characters
// 3) Carriage return characters

import Scripting.{shell, read}
import collection.mutable.Buffer

def ignore(path: String) =
  path.contains("/extensions/gis/") ||
  path.contains("/src_managed/") ||
  path.contains("/tmp/") ||
  path.endsWith("Lexer.java")

def paths =
  shell("find . -name \\*.java -or -name \\*.scala")  

for(path <- paths.filterNot(ignore)) {
  val contents = io.Source.fromFile(path).mkString
  val problems = Buffer[String]()
  if(contents.last != '\n')
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

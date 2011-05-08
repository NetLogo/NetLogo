#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

// Check for problems with our Scala & Java source files including:
// 1) No newline at end of file
// 2) Tab characters in Scala source
// 3) Carriage return characters

import Scripting.{shell, read}
import collection.mutable.Buffer

for{path <- shell("find . -name \\*.java -or -name \\*.scala")
    if(!path.contains("/devel/behaviorsearch/") &&
       !path.contains("/src_managed/") &&
       !path.contains("/tmp/"))} {
  val contents = io.Source.fromFile(path).mkString
  val problems = Buffer[String]()
  if(contents.last != '\n')
    problems += "Missing newline at eof"
  if(contents.contains('\r'))
    problems += "Carriage return character(s) found"
  if(path.endsWith(".scala") && contents.contains('\t'))
    problems += "Tab character(s) found"
  if(problems.nonEmpty) {
    println(path)
    problems.foreach(p => println("  " + p))
  }
}

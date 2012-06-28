#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon -Dfile.encoding=UTF-8 "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

// finds spelling mistakes in the User Manual (because yea, verily,
// spelling mistakes are an abomination unto the Lord)

// installing aspell: brew install aspell --lang=en

import sys.process._
import java.io.File

for{path <- stringToProcess("find docs -name *.html").lines
    if !path.startsWith("docs/scaladoc/")
    lines = (new File(path) #> "aspell -H -p ./dist/docwords.txt list").lines
    if lines.nonEmpty}
{
  println(path)
  lines.map("  " + _).foreach(println)
}

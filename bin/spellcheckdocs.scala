#!/bin/sh
exec scala -classpath bin -deprecation -Dfile.encoding=UTF-8 "$0" "$@"
!#

// finds spelling mistakes in the User Manual (because yea, verily,
// spelling mistakes are an abomination unto the Lord)

// installing aspell: brew install aspell --lang=en

import sys.process.Process
import java.io.File

for{path <- Process("find autogen/docs -name *.html.mustache").lazyLines
    if !path.startsWith("docs/scaladoc/")
    lines = (Process(new File(path)) #> "aspell -H -p ./dist/docwords.txt list").lazyLines
    if lines.nonEmpty}
{
  println(path)
  lines.map("  " + _).foreach(println)
}

// Local Variables:
// mode: scala
// End:

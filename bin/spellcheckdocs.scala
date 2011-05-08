#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

// finds spelling mistakes in the User Manual (because yea, verily,
// spelling mistakes are an abomination unto the Lord)

import Scripting.{shell,read}

for{path <- shell("find docs -name \\*.html")
    if !path.startsWith("docs/javadoc/")
    lines = shell("ispell -H -l -p dist/docwords.txt < " + path)
    if lines.hasNext}
{
  println(path)
  lines.foreach(line => println("  " + line))
}

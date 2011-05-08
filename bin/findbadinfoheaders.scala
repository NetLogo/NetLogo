#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

/// finds models that have lines that the text->HTML info tab converter
/// thinks are headers, but in reality weren't intended to be

import Scripting.{shell,readChars,pipe}

val ignore = List("Info Tab Example.nlogo")
for(i <- ignore) println("IGNORING: " + i)
println

val headers = List("WHAT IS IT?","HOW IT WORKS","HOW TO USE IT","THINGS TO NOTICE",
                   "THINGS TO TRY","EXTENDING THE MODEL","RELATED MODELS",
                   "CREDITS AND REFERENCES","NETLOGO FEATURES")

for{path <- shell("find models -name \\*.nlogo -o -name \\*.nlogo3d")
    if !ignore.exists(path.containsSlice(_))
    info = readChars(path).mkString.split("\\@\\#\\$\\#\\@\\#\\$\\#\\@\n")(2)
    line <- info.split("\n")
    if line.startsWith("#") && ! line.startsWith("## ")}
{
  println(path + ": " + line)
}

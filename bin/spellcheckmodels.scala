#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

// finds models with spelling mistakes (because yea, verily,
// spelling mistakes are an abomination unto the Lord)

import Scripting.{shell,read}

val ignores = List("/3D/", "/Curricular Models/Urban Suite/")

for{path <- shell("find models -name \\*.nlogo")
    if ignores.forall(!path.containsSlice(_))
    lines = shell("cat \"" + path + "\" | sed -e 's/\\\\n/ /g' | ispell -l -p dist/modelwords.txt")
    if lines.hasNext}
{
  println(path)
  lines.foreach(line => println("  " + line))
}

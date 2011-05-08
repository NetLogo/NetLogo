#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

// finds models with non-integer patch sizes (because yea, verily,
// non-integer patch sizes are an abomination)
import Scripting.{shell,read}
println
for{path <- shell("find models -name \\*.nlogo -o -name \\*.nlogo3d")
    patchSize = read(path).dropWhile(_ != "GRAPHICS-WINDOW")
                  .takeWhile(!_.isEmpty).drop(7).next
    if !patchSize.endsWith(".0")}
  println(path + ": " + patchSize)

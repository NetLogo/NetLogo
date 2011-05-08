#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

// finds models with tabs anywhere in them
// (because yea, verily, tabs are an abomination)

import Scripting.{shell,read}

shell("find models -name \\*.nlogo -o -name \\*.nlogo3d")
  .filter(read(_).exists(_.contains('\t')))
  .foreach(println(_))

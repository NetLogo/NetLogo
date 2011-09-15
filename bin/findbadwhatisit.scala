#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

// finds models that have first paragraphs of their WHAT IS IT? sections that are too long, or
// otherwise malformed

import Scripting.{ shell, slurp }

for{path <- shell("find models -name \\*.nlogo -o -name \\*.nlogo3d")
    info = slurp(path).split("\\@\\#\\$\\#\\@\\#\\$\\#\\@\n")(2)}
{
  val what = "## WHAT IS IT?\n\n"
  if(!info.startsWith(what))
    println("*** "+ path + ": WHAT IS IT? not found")
  else {
    val firstParagraph = info.drop(what.size).split('\n').head
    // The allowed range here is arbitrary.  Perhaps 540 is still too large?
    if(firstParagraph.size < 42 || firstParagraph.size > 540) {
      println("*** " + path)
      println("*** length is " + firstParagraph.size)
      println(firstParagraph)
    }
  }
}

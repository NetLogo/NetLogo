#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

// find models with malformed interface tabs

import Scripting.{shell,slurp}

val allowedLengths = Map("BUTTON" -> (11,16),
                         "SLIDER" -> (12,14),
                         "SWITCH" -> (10,10),
                         "MONITOR" -> (9,10),
                         "GRAPHICS-WINDOW" -> (10,28),
                         "TEXTBOX" -> (6,9),
                         "CHOICE" -> (9,9),
                         "CHOOSER" -> (9,9),
                         "OUTPUT" -> (5,6),
                         "INPUTBOX" -> (8,10),
                         "PLOT" -> (14,999999)) // any number of pens

for{path <- shell("find models -name \\*.nlogo -o -name \\*.nlogo3d")
    interface = slurp(path).split("\\@\\#\\$\\#\\@\\#\\$\\#\\@\n")(1)
    widget <- interface.split("\n\n")}
{
  val kind = widget.split("\n").head
  val len = widget.split("\n").size
  def complain() { println((path, kind)) }
  allowedLengths.get(kind) match {
    case Some((min, max)) =>
      if(len < min || len > max)
        complain()
    case None =>
      complain()
  }
}

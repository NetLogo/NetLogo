#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

// find models with malformed interface tabs

import Scripting.{shell,readChars}

val allowedLengths = Map("BUTTON" -> (11,16),
                         "SLIDER" -> (12,14),
                         "SWITCH" -> (10,10),
                         "MONITOR" -> (9,10),
                         "GRAPHICS-WINDOW" -> (10,26),
                         "TEXTBOX" -> (6,9),
                         "CHOICE" -> (9,9),
                         "CHOOSER" -> (9,9),
                         "OUTPUT" -> (5,6),
                         "INPUTBOX" -> (8,10),
                         "PLOT" -> (14,999999)) // any number of pens

// it would be better if we handled .nlogo3d too - ST 7/8/06, 4/19/11
for{path <- shell("find models -name \\*.nlogo")
    interface = readChars(path).mkString.split("\\@\\#\\$\\#\\@\\#\\$\\#\\@\n")(1)
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

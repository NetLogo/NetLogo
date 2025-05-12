// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ Command, Instruction, Reporter }

object TokenMapping {

  val commandClassNames: Map[String, String] =
    Map( "__APPLY"                       -> "org.nlogo.core.prim.etc._apply"
       , "__BENCH"                       -> "org.nlogo.core.prim.etc._bench"
       , "__CLEAR-ALL-AND-RESET-TICKS"   -> "org.nlogo.core.prim.etc._clearallandresetticks"
       , "__DONE"                        -> "org.nlogo.core.prim._done"
       , "__EXPERIMENTSTEPEND"           -> "org.nlogo.core.prim.etc._experimentstepend"
       , "__EXPORT-DRAWING"              -> "org.nlogo.core.prim.etc._exportdrawing"
       , "__FOREVERBUTTONEND"            -> "org.nlogo.core.prim.etc._foreverbuttonend"
       , "__IGNORE"                      -> "org.nlogo.core.prim.etc._ignore"
       , "__LET"                         -> "org.nlogo.core.prim._let"
       , "__LINKCODE"                    -> "org.nlogo.core.prim.etc._linkcode"
       , "__MKDIR"                       -> "org.nlogo.core.prim.etc._mkdir"
       , "__OBSERVERCODE"                -> "org.nlogo.core.prim.etc._observercode"
       , "__PATCHCODE"                   -> "org.nlogo.core.prim.etc._patchcode"
       , "__PLOT-PEN-HIDE"               -> "org.nlogo.core.prim.etc._plotpenhide"
       , "__PLOT-PEN-SHOW"               -> "org.nlogo.core.prim.etc._plotpenshow"
       , "__PWD"                         -> "org.nlogo.core.prim.etc._pwd"
       , "__RELOAD-EXTENSIONS"           -> "org.nlogo.core.prim.etc._reloadextensions"
       , "__SET-LINE-THICKNESS"          -> "org.nlogo.core.prim.etc._setlinethickness"
       , "__STDERR"                      -> "org.nlogo.core.prim.etc._stderr"
       , "__STDOUT"                      -> "org.nlogo.core.prim.etc._stdout"
       , "__THUNK-DID-FINISH"            -> "org.nlogo.core.prim.etc._thunkdidfinish"
       , "__TURTLECODE"                  -> "org.nlogo.core.prim.etc._turtlecode"
       , "ASK"                           -> "org.nlogo.core.prim._ask"
       , "ASK-CONCURRENT"                -> "org.nlogo.core.prim._askconcurrent"
       , "AUTO-PLOT-OFF"                 -> "org.nlogo.core.prim.etc._autoplotoff"
       , "AUTO-PLOT-ON"                  -> "org.nlogo.core.prim.etc._autoploton"
       , "AUTO-PLOT-X-OFF"               -> "org.nlogo.core.prim.etc._autoplotxoff"
       , "AUTO-PLOT-X-ON"                -> "org.nlogo.core.prim.etc._autoplotxon"
       , "AUTO-PLOT-Y-OFF"               -> "org.nlogo.core.prim.etc._autoplotyoff"
       , "AUTO-PLOT-Y-ON"                -> "org.nlogo.core.prim.etc._autoplotyon"
       , "BACK"                          -> "org.nlogo.core.prim._bk"
       , "BEEP"                          -> "org.nlogo.core.prim.etc._beep"
       , "BK"                            -> "org.nlogo.core.prim._bk"
       , "CA"                            -> "org.nlogo.core.prim.etc._clearall"
       , "CAREFULLY"                     -> "org.nlogo.core.prim._carefully"
       , "CD"                            -> "org.nlogo.core.prim.etc._cleardrawing"
       , "CLEAR-ALL"                     -> "org.nlogo.core.prim.etc._clearall"
       , "CLEAR-ALL-PLOTS"               -> "org.nlogo.core.prim.etc._clearallplots"
       , "CLEAR-DRAWING"                 -> "org.nlogo.core.prim.etc._cleardrawing"
       , "CLEAR-GLOBALS"                 -> "org.nlogo.core.prim.etc._clearglobals"
       , "CLEAR-LINKS"                   -> "org.nlogo.core.prim.etc._clearlinks"
       , "CLEAR-OUTPUT"                  -> "org.nlogo.core.prim.etc._clearoutput"
       , "CLEAR-PATCHES"                 -> "org.nlogo.core.prim.etc._clearpatches"
       , "CLEAR-PLOT"                    -> "org.nlogo.core.prim.etc._clearplot"
       , "CLEAR-TICKS"                   -> "org.nlogo.core.prim.etc._clearticks"
       , "CLEAR-TURTLES"                 -> "org.nlogo.core.prim.etc._clearturtles"
       , "CP"                            -> "org.nlogo.core.prim.etc._clearpatches"
       , "CREATE-LINK-FROM"              -> "org.nlogo.core.prim.etc._createlinkfrom"
       , "CREATE-LINK-TO"                -> "org.nlogo.core.prim.etc._createlinkto"
       , "CREATE-LINK-WITH"              -> "org.nlogo.core.prim.etc._createlinkwith"
       , "CREATE-LINKS-FROM"             -> "org.nlogo.core.prim.etc._createlinksfrom"
       , "CREATE-LINKS-TO"               -> "org.nlogo.core.prim.etc._createlinksto"
       , "CREATE-LINKS-WITH"             -> "org.nlogo.core.prim.etc._createlinkswith"
       , "CREATE-ORDERED-TURTLES"        -> "org.nlogo.core.prim._createorderedturtles"
       , "CREATE-TEMPORARY-PLOT-PEN"     -> "org.nlogo.core.prim.etc._createtemporaryplotpen"
       , "CREATE-TURTLES"                -> "org.nlogo.core.prim._createturtles"
       , "CRO"                           -> "org.nlogo.core.prim._createorderedturtles"
       , "CRT"                           -> "org.nlogo.core.prim._createturtles"
       , "CT"                            -> "org.nlogo.core.prim.etc._clearturtles"
       , "DIE"                           -> "org.nlogo.core.prim.etc._die"
       , "DIFFUSE"                       -> "org.nlogo.core.prim.etc._diffuse"
       , "DIFFUSE4"                      -> "org.nlogo.core.prim.etc._diffuse4"
       , "DISPLAY"                       -> "org.nlogo.core.prim.etc._display"
       , "DOWNHILL"                      -> "org.nlogo.core.prim.etc._downhill"
       , "DOWNHILL4"                     -> "org.nlogo.core.prim.etc._downhill4"
       , "ERROR"                         -> "org.nlogo.core.prim.etc._error"
       , "EVERY"                         -> "org.nlogo.core.prim.etc._every"
       , "EXPORT-ALL-PLOTS"              -> "org.nlogo.core.prim.etc._exportplots"
       , "EXPORT-INTERFACE"              -> "org.nlogo.core.prim.etc._exportinterface"
       , "EXPORT-OUTPUT"                 -> "org.nlogo.core.prim.etc._exportoutput"
       , "EXPORT-PLOT"                   -> "org.nlogo.core.prim.etc._exportplot"
       , "EXPORT-VIEW"                   -> "org.nlogo.core.prim.etc._exportview"
       , "EXPORT-WORLD"                  -> "org.nlogo.core.prim.etc._exportworld"
       , "FACE"                          -> "org.nlogo.core.prim.etc._face"
       , "FACEXY"                        -> "org.nlogo.core.prim.etc._facexy"
       , "FD"                            -> "org.nlogo.core.prim._fd"
       , "FILE-CLOSE"                    -> "org.nlogo.core.prim.etc._fileclose"
       , "FILE-CLOSE-ALL"                -> "org.nlogo.core.prim.etc._filecloseall"
       , "FILE-DELETE"                   -> "org.nlogo.core.prim.etc._filedelete"
       , "FILE-FLUSH"                    -> "org.nlogo.core.prim.etc._fileflush"
       , "FILE-OPEN"                     -> "org.nlogo.core.prim.etc._fileopen"
       , "FILE-PRINT"                    -> "org.nlogo.core.prim.etc._fileprint"
       , "FILE-SHOW"                     -> "org.nlogo.core.prim.etc._fileshow"
       , "FILE-TYPE"                     -> "org.nlogo.core.prim.etc._filetype"
       , "FILE-WRITE"                    -> "org.nlogo.core.prim.etc._filewrite"
       , "FOLLOW"                        -> "org.nlogo.core.prim.etc._follow"
       , "FOLLOW-ME"                     -> "org.nlogo.core.prim.etc._followme"
       , "FOREACH"                       -> "org.nlogo.core.prim.etc._foreach"
       , "FORWARD"                       -> "org.nlogo.core.prim._fd"
       , "HATCH"                         -> "org.nlogo.core.prim._hatch"
       , "HIDE-LINK"                     -> "org.nlogo.core.prim.etc._hidelink"
       , "HIDE-TURTLE"                   -> "org.nlogo.core.prim.etc._hideturtle"
       , "HISTOGRAM"                     -> "org.nlogo.core.prim.etc._histogram"
       , "HOME"                          -> "org.nlogo.core.prim.etc._home"
       , "HT"                            -> "org.nlogo.core.prim.etc._hideturtle"
       , "HUBNET-BROADCAST"              -> "org.nlogo.core.prim.hubnet._hubnetbroadcast"
       , "HUBNET-BROADCAST-CLEAR-OUTPUT" -> "org.nlogo.core.prim.hubnet._hubnetbroadcastclearoutput"
       , "HUBNET-BROADCAST-MESSAGE"      -> "org.nlogo.core.prim.hubnet._hubnetbroadcastmessage"
       , "HUBNET-CLEAR-OVERRIDE"         -> "org.nlogo.core.prim.hubnet._hubnetclearoverride"
       , "HUBNET-CLEAR-OVERRIDES"        -> "org.nlogo.core.prim.hubnet._hubnetclearoverrides"
       , "HUBNET-FETCH-MESSAGE"          -> "org.nlogo.core.prim.hubnet._hubnetfetchmessage"
       , "HUBNET-KICK-ALL-CLIENTS"       -> "org.nlogo.core.prim.hubnet._hubnetkickallclients"
       , "HUBNET-KICK-CLIENT"            -> "org.nlogo.core.prim.hubnet._hubnetkickclient"
       , "HUBNET-RESET"                  -> "org.nlogo.core.prim.hubnet._hubnetreset"
       , "HUBNET-RESET-PERSPECTIVE"      -> "org.nlogo.core.prim.hubnet._hubnetresetperspective"
       , "HUBNET-SEND"                   -> "org.nlogo.core.prim.hubnet._hubnetsend"
       , "HUBNET-SEND-CLEAR-OUTPUT"      -> "org.nlogo.core.prim.hubnet._hubnetsendclearoutput"
       , "HUBNET-SEND-FOLLOW"            -> "org.nlogo.core.prim.hubnet._hubnetsendfollow"
       , "HUBNET-SEND-MESSAGE"           -> "org.nlogo.core.prim.hubnet._hubnetsendmessage"
       , "HUBNET-SEND-OVERRIDE"          -> "org.nlogo.core.prim.hubnet._hubnetsendoverride"
       , "HUBNET-SEND-WATCH"             -> "org.nlogo.core.prim.hubnet._hubnetsendwatch"
       , "IF"                            -> "org.nlogo.core.prim.etc._if"
       , "IF-ELSE"                       -> "org.nlogo.core.prim.etc._ifelse"
       , "IFELSE"                        -> "org.nlogo.core.prim.etc._ifelse"
       , "IMPORT-DRAWING"                -> "org.nlogo.core.prim.etc._importdrawing"
       , "IMPORT-PCOLORS"                -> "org.nlogo.core.prim.etc._importpatchcolors"
       , "IMPORT-PCOLORS-RGB"            -> "org.nlogo.core.prim.etc._importpcolorsrgb"
       , "IMPORT-WORLD"                  -> "org.nlogo.core.prim.etc._importworld"
       , "INSPECT"                       -> "org.nlogo.core.prim.etc._inspect"
       , "JUMP"                          -> "org.nlogo.core.prim._jump"
       , "LAYOUT-CIRCLE"                 -> "org.nlogo.core.prim.etc._layoutcircle"
       , "LAYOUT-RADIAL"                 -> "org.nlogo.core.prim.etc._layoutradial"
       , "LAYOUT-SPRING"                 -> "org.nlogo.core.prim.etc._layoutspring"
       , "LAYOUT-TUTTE"                  -> "org.nlogo.core.prim.etc._layouttutte"
       , "LEFT"                          -> "org.nlogo.core.prim.etc._left"
       , "LET"                           -> "org.nlogo.core.prim._let"
       , "LOOP"                          -> "org.nlogo.core.prim.etc._loop"
       , "LT"                            -> "org.nlogo.core.prim.etc._left"
       , "MOVE-TO"                       -> "org.nlogo.core.prim.etc._moveto"
       , "NO-DISPLAY"                    -> "org.nlogo.core.prim.etc._nodisplay"
       , "OUTPUT-PRINT"                  -> "org.nlogo.core.prim.etc._outputprint"
       , "OUTPUT-SHOW"                   -> "org.nlogo.core.prim.etc._outputshow"
       , "OUTPUT-TYPE"                   -> "org.nlogo.core.prim.etc._outputtype"
       , "OUTPUT-WRITE"                  -> "org.nlogo.core.prim.etc._outputwrite"
       , "PD"                            -> "org.nlogo.core.prim.etc._pendown"
       , "PE"                            -> "org.nlogo.core.prim.etc._penerase"
       , "PEN-DOWN"                      -> "org.nlogo.core.prim.etc._pendown"
       , "PEN-ERASE"                     -> "org.nlogo.core.prim.etc._penerase"
       , "PEN-UP"                        -> "org.nlogo.core.prim.etc._penup"
       , "PENDOWN"                       -> "org.nlogo.core.prim.etc._pendown"
       , "PENUP"                         -> "org.nlogo.core.prim.etc._penup"
       , "PLOT"                          -> "org.nlogo.core.prim.etc._plot"
       , "PLOT-PEN-DOWN"                 -> "org.nlogo.core.prim.etc._plotpendown"
       , "PLOT-PEN-RESET"                -> "org.nlogo.core.prim.etc._plotpenreset"
       , "PLOT-PEN-UP"                   -> "org.nlogo.core.prim.etc._plotpenup"
       , "PLOTXY"                        -> "org.nlogo.core.prim.etc._plotxy"
       , "PRINT"                         -> "org.nlogo.core.prim.etc._print"
       , "PU"                            -> "org.nlogo.core.prim.etc._penup"
       , "RANDOM-SEED"                   -> "org.nlogo.core.prim.etc._randomseed"
       , "REPEAT"                        -> "org.nlogo.core.prim._repeat"
       , "REPORT"                        -> "org.nlogo.core.prim._report"
       , "RESET-PERSPECTIVE"             -> "org.nlogo.core.prim.etc._resetperspective"
       , "RESET-TICKS"                   -> "org.nlogo.core.prim.etc._resetticks"
       , "RESET-TIMER"                   -> "org.nlogo.core.prim.etc._resettimer"
       , "RESIZE-WORLD"                  -> "org.nlogo.core.prim.etc._resizeworld"
       , "RIDE"                          -> "org.nlogo.core.prim.etc._ride"
       , "RIDE-ME"                       -> "org.nlogo.core.prim.etc._rideme"
       , "RIGHT"                         -> "org.nlogo.core.prim.etc._right"
       , "RP"                            -> "org.nlogo.core.prim.etc._resetperspective"
       , "RT"                            -> "org.nlogo.core.prim.etc._right"
       , "RUN"                           -> "org.nlogo.core.prim._run"
       , "SET"                           -> "org.nlogo.core.prim._set"
       , "SET-CURRENT-DIRECTORY"         -> "org.nlogo.core.prim.etc._setcurdir"
       , "SET-CURRENT-PLOT"              -> "org.nlogo.core.prim.etc._setcurrentplot"
       , "SET-CURRENT-PLOT-PEN"          -> "org.nlogo.core.prim.etc._setcurrentplotpen"
       , "SET-DEFAULT-SHAPE"             -> "org.nlogo.core.prim.etc._setdefaultshape"
       , "SET-HISTOGRAM-NUM-BARS"        -> "org.nlogo.core.prim.etc._sethistogramnumbars"
       , "SET-PATCH-SIZE"                -> "org.nlogo.core.prim.etc._setpatchsize"
       , "SET-PLOT-PEN-COLOR"            -> "org.nlogo.core.prim.etc._setplotpencolor"
       , "SET-PLOT-PEN-INTERVAL"         -> "org.nlogo.core.prim.etc._setplotpeninterval"
       , "SET-PLOT-PEN-MODE"             -> "org.nlogo.core.prim.etc._setplotpenmode"
       , "SET-PLOT-X-RANGE"              -> "org.nlogo.core.prim.etc._setplotxrange"
       , "SET-PLOT-Y-RANGE"              -> "org.nlogo.core.prim.etc._setplotyrange"
       , "SET-TOPOLOGY"                  -> "org.nlogo.core.prim.etc._settopology"
       , "SETUP-PLOTS"                   -> "org.nlogo.core.prim.etc._setupplots"
       , "SETXY"                         -> "org.nlogo.core.prim.etc._setxy"
       , "SHOW"                          -> "org.nlogo.core.prim.etc._show"
       , "SHOW-LINK"                     -> "org.nlogo.core.prim.etc._showlink"
       , "SHOW-TURTLE"                   -> "org.nlogo.core.prim.etc._showturtle"
       , "SPROUT"                        -> "org.nlogo.core.prim._sprout"
       , "ST"                            -> "org.nlogo.core.prim.etc._showturtle"
       , "STAMP"                         -> "org.nlogo.core.prim.etc._stamp"
       , "STAMP-ERASE"                   -> "org.nlogo.core.prim.etc._stamperase"
       , "STOP"                          -> "org.nlogo.core.prim._stop"
       , "STOP-INSPECTING"               -> "org.nlogo.core.prim.etc._stopinspecting"
       , "STOP-INSPECTING-DEAD-AGENTS"   -> "org.nlogo.core.prim.etc._stopinspectingdeadagents"
       , "TICK"                          -> "org.nlogo.core.prim.etc._tick"
       , "TICK-ADVANCE"                  -> "org.nlogo.core.prim.etc._tickadvance"
       , "TIE"                           -> "org.nlogo.core.prim.etc._tie"
       , "TYPE"                          -> "org.nlogo.core.prim.etc._type"
       , "UNTIE"                         -> "org.nlogo.core.prim.etc._untie"
       , "UPDATE-PLOTS"                  -> "org.nlogo.core.prim.etc._updateplots"
       , "UPHILL"                        -> "org.nlogo.core.prim.etc._uphill"
       , "UPHILL4"                       -> "org.nlogo.core.prim.etc._uphill4"
       , "USER-MESSAGE"                  -> "org.nlogo.core.prim.etc._usermessage"
       , "WAIT"                          -> "org.nlogo.core.prim.etc._wait"
       , "WATCH"                         -> "org.nlogo.core.prim.etc._watch"
       , "WATCH-ME"                      -> "org.nlogo.core.prim.etc._watchme"
       , "WHILE"                         -> "org.nlogo.core.prim.etc._while"
       , "WITH-LOCAL-RANDOMNESS"         -> "org.nlogo.core.prim.etc._withlocalrandomness"
       , "WITHOUT-INTERRUPTION"          -> "org.nlogo.core.prim.etc._withoutinterruption"
       , "WRITE"                         -> "org.nlogo.core.prim.etc._write"
       )

  val reporterClassNames: Map[String, String] =
    Map( "!="                            -> "org.nlogo.core.prim._notequal"
       , "*"                             -> "org.nlogo.core.prim.etc._mult"
       , "+"                             -> "org.nlogo.core.prim.etc._plus"
       , "-"                             -> "org.nlogo.core.prim._minus"
       , "/"                             -> "org.nlogo.core.prim.etc._div"
       , "<"                             -> "org.nlogo.core.prim._lessthan"
       , "<="                            -> "org.nlogo.core.prim.etc._lessorequal"
       , "="                             -> "org.nlogo.core.prim._equal"
       , ">"                             -> "org.nlogo.core.prim._greaterthan"
       , ">="                            -> "org.nlogo.core.prim.etc._greaterorequal"
       , "^"                             -> "org.nlogo.core.prim.etc._pow"
       , "__APPLY-RESULT"                -> "org.nlogo.core.prim.etc._applyresult"
       , "__BOOM"                        -> "org.nlogo.core.prim.etc._boom"
       , "__BLOCK"                       -> "org.nlogo.core.prim.etc._block"
       , "__CHECK-SYNTAX"                -> "org.nlogo.core.prim.etc._checksyntax"
       , "__CHECKSUM"                    -> "org.nlogo.core.prim.etc._checksum"
       , "__DUMP"                        -> "org.nlogo.core.prim.etc._dump"
       , "__DUMP-EXTENSION-PRIMS"        -> "org.nlogo.core.prim.etc._dumpextensionprims"
       , "__DUMP-EXTENSIONS"             -> "org.nlogo.core.prim.etc._dumpextensions"
       , "__DUMP1"                       -> "org.nlogo.core.prim.etc._dump1"
       , "__NANO-TIME"                   -> "org.nlogo.core.prim.etc._nanotime"
       , "__PROCESSORS"                  -> "org.nlogo.core.prim.etc._processors"
       , "__RANDOM-STATE"                -> "org.nlogo.core.prim.etc._randomstate"
       , "__REFERENCE"                   -> "org.nlogo.core.prim.etc._reference"
       , "__STACK-TRACE"                 -> "org.nlogo.core.prim.etc._stacktrace"
       , "__SYMBOL"                      -> "org.nlogo.core.prim.etc._symbolstring"
       , "__TO-STRING"                   -> "org.nlogo.core.prim.etc._tostring"
       , "ABS"                           -> "org.nlogo.core.prim.etc._abs"
       , "ACOS"                          -> "org.nlogo.core.prim.etc._acos"
       , "ALL?"                          -> "org.nlogo.core.prim.etc._all"
       , "AND"                           -> "org.nlogo.core.prim._and"
       , "ANY?"                          -> "org.nlogo.core.prim._any"
       , "APPROXIMATE-HSB"               -> "org.nlogo.core.prim.etc._approximatehsb"
       , "APPROXIMATE-RGB"               -> "org.nlogo.core.prim.etc._approximatergb"
       , "ASIN"                          -> "org.nlogo.core.prim.etc._asin"
       , "AT-POINTS"                     -> "org.nlogo.core.prim.etc._atpoints"
       , "ATAN"                          -> "org.nlogo.core.prim.etc._atan"
       , "AUTOPLOT?"                     -> "org.nlogo.core.prim.etc._autoplot"
       , "AUTOPLOTX?"                    -> "org.nlogo.core.prim.etc._autoplotx"
       , "AUTOPLOTY?"                    -> "org.nlogo.core.prim.etc._autoploty"
       , "BASE-COLORS"                   -> "org.nlogo.core.prim.etc._basecolors"
       , "BEHAVIORSPACE-EXPERIMENT-NAME" -> "org.nlogo.core.prim.etc._behaviorspaceexperimentname"
       , "BEHAVIORSPACE-RUN-NUMBER"      -> "org.nlogo.core.prim.etc._behaviorspacerunnumber"
       , "BF"                            -> "org.nlogo.core.prim.etc._butfirst"
       , "BL"                            -> "org.nlogo.core.prim.etc._butlast"
       , "BOTH-ENDS"                     -> "org.nlogo.core.prim.etc._bothends"
       , "BUT-FIRST"                     -> "org.nlogo.core.prim.etc._butfirst"
       , "BUT-LAST"                      -> "org.nlogo.core.prim.etc._butlast"
       , "BUTFIRST"                      -> "org.nlogo.core.prim.etc._butfirst"
       , "BUTLAST"                       -> "org.nlogo.core.prim.etc._butlast"
       , "CAN-MOVE?"                     -> "org.nlogo.core.prim.etc._canmove"
       , "CEILING"                       -> "org.nlogo.core.prim.etc._ceil"
       , "COS"                           -> "org.nlogo.core.prim.etc._cos"
       , "COUNT"                         -> "org.nlogo.core.prim._count"
       , "DATE-AND-TIME"                 -> "org.nlogo.core.prim.etc._dateandtime"
       , "DISTANCE"                      -> "org.nlogo.core.prim.etc._distance"
       , "DISTANCEXY"                    -> "org.nlogo.core.prim.etc._distancexy"
       , "DX"                            -> "org.nlogo.core.prim.etc._dx"
       , "DY"                            -> "org.nlogo.core.prim.etc._dy"
       , "EMPTY?"                        -> "org.nlogo.core.prim.etc._empty"
       , "ERROR-MESSAGE"                 -> "org.nlogo.core.prim._errormessage"
       , "EXP"                           -> "org.nlogo.core.prim.etc._exp"
       , "EXTRACT-HSB"                   -> "org.nlogo.core.prim.etc._extracthsb"
       , "EXTRACT-RGB"                   -> "org.nlogo.core.prim.etc._extractrgb"
       , "FILE-AT-END?"                  -> "org.nlogo.core.prim.etc._fileatend"
       , "FILE-EXISTS?"                  -> "org.nlogo.core.prim.etc._fileexists"
       , "FILE-READ"                     -> "org.nlogo.core.prim.etc._fileread"
       , "FILE-READ-CHARACTERS"          -> "org.nlogo.core.prim.etc._filereadchars"
       , "FILE-READ-LINE"                -> "org.nlogo.core.prim.etc._filereadline"
       , "FILTER"                        -> "org.nlogo.core.prim.etc._filter"
       , "FIRST"                         -> "org.nlogo.core.prim.etc._first"
       , "FLOOR"                         -> "org.nlogo.core.prim.etc._floor"
       , "FPUT"                          -> "org.nlogo.core.prim.etc._fput"
       , "HOME-DIRECTORY"                -> "org.nlogo.core.prim._homedirectory"
       , "HSB"                           -> "org.nlogo.core.prim.etc._hsb"
       , "HUBNET-CLIENTS-LIST"           -> "org.nlogo.core.prim.hubnet._hubnetclientslist"
       , "HUBNET-ENTER-MESSAGE?"         -> "org.nlogo.core.prim.hubnet._hubnetentermessage"
       , "HUBNET-EXIT-MESSAGE?"          -> "org.nlogo.core.prim.hubnet._hubnetexitmessage"
       , "HUBNET-MESSAGE"                -> "org.nlogo.core.prim.hubnet._hubnetmessage"
       , "HUBNET-MESSAGE-SOURCE"         -> "org.nlogo.core.prim.hubnet._hubnetmessagesource"
       , "HUBNET-MESSAGE-TAG"            -> "org.nlogo.core.prim.hubnet._hubnetmessagetag"
       , "HUBNET-MESSAGE-WAITING?"       -> "org.nlogo.core.prim.hubnet._hubnetmessagewaiting"
       , "IFELSE-VALUE"                  -> "org.nlogo.core.prim.etc._ifelsevalue"
       , "IN-CONE"                       -> "org.nlogo.core.prim.etc._incone"
       , "IN-LINK-FROM"                  -> "org.nlogo.core.prim.etc._inlinkfrom"
       , "IN-LINK-NEIGHBOR?"             -> "org.nlogo.core.prim.etc._inlinkneighbor"
       , "IN-LINK-NEIGHBORS"             -> "org.nlogo.core.prim.etc._inlinkneighbors"
       , "IN-RADIUS"                     -> "org.nlogo.core.prim._inradius"
       , "INSERT-ITEM"                   -> "org.nlogo.core.prim.etc._insertitem"
       , "INT"                           -> "org.nlogo.core.prim.etc._int"
       , "IS-AGENT?"                     -> "org.nlogo.core.prim.etc._isagent"
       , "IS-AGENTSET?"                  -> "org.nlogo.core.prim.etc._isagentset"
       , "IS-ANONYMOUS-COMMAND?"         -> "org.nlogo.core.prim.etc._isanonymouscommand"
       , "IS-ANONYMOUS-REPORTER?"        -> "org.nlogo.core.prim.etc._isanonymousreporter"
       , "IS-BOOLEAN?"                   -> "org.nlogo.core.prim.etc._isboolean"
       , "IS-DIRECTED-LINK?"             -> "org.nlogo.core.prim.etc._isdirectedlink"
       , "IS-LINK-SET?"                  -> "org.nlogo.core.prim.etc._islinkset"
       , "IS-LINK?"                      -> "org.nlogo.core.prim.etc._islink"
       , "IS-LIST?"                      -> "org.nlogo.core.prim.etc._islist"
       , "IS-NUMBER?"                    -> "org.nlogo.core.prim.etc._isnumber"
       , "IS-PATCH-SET?"                 -> "org.nlogo.core.prim.etc._ispatchset"
       , "IS-PATCH?"                     -> "org.nlogo.core.prim.etc._ispatch"
       , "IS-STRING?"                    -> "org.nlogo.core.prim.etc._isstring"
       , "IS-TURTLE-SET?"                -> "org.nlogo.core.prim.etc._isturtleset"
       , "IS-TURTLE?"                    -> "org.nlogo.core.prim.etc._isturtle"
       , "IS-UNDIRECTED-LINK?"           -> "org.nlogo.core.prim.etc._isundirectedlink"
       , "ITEM"                          -> "org.nlogo.core.prim.etc._item"
       , "LAST"                          -> "org.nlogo.core.prim.etc._last"
       , "LENGTH"                        -> "org.nlogo.core.prim.etc._length"
       , "LINK"                          -> "org.nlogo.core.prim.etc._link"
       , "LINK-HEADING"                  -> "org.nlogo.core.prim.etc._linkheading"
       , "LINK-LENGTH"                   -> "org.nlogo.core.prim.etc._linklength"
       , "LINK-NEIGHBOR?"                -> "org.nlogo.core.prim.etc._linkneighbor"
       , "LINK-NEIGHBORS"                -> "org.nlogo.core.prim.etc._linkneighbors"
       , "LINK-SET"                      -> "org.nlogo.core.prim.etc._linkset"
       , "LINK-SHAPES"                   -> "org.nlogo.core.prim.etc._linkshapes"
       , "LINK-WITH"                     -> "org.nlogo.core.prim.etc._linkwith"
       , "LINKS"                         -> "org.nlogo.core.prim.etc._links"
       , "LIST"                          -> "org.nlogo.core.prim._list"
       , "LN"                            -> "org.nlogo.core.prim.etc._ln"
       , "LOG"                           -> "org.nlogo.core.prim.etc._log"
       , "LPUT"                          -> "org.nlogo.core.prim.etc._lput"
       , "MAP"                           -> "org.nlogo.core.prim.etc._map"
       , "MAX"                           -> "org.nlogo.core.prim.etc._max"
       , "MAX-N-OF"                      -> "org.nlogo.core.prim.etc._maxnof"
       , "MAX-ONE-OF"                    -> "org.nlogo.core.prim.etc._maxoneof"
       , "MAX-PXCOR"                     -> "org.nlogo.core.prim.etc._maxpxcor"
       , "MAX-PYCOR"                     -> "org.nlogo.core.prim.etc._maxpycor"
       , "MEAN"                          -> "org.nlogo.core.prim.etc._mean"
       , "MEDIAN"                        -> "org.nlogo.core.prim.etc._median"
       , "MEMBER?"                       -> "org.nlogo.core.prim.etc._member"
       , "MIN"                           -> "org.nlogo.core.prim.etc._min"
       , "MIN-N-OF"                      -> "org.nlogo.core.prim.etc._minnof"
       , "MIN-ONE-OF"                    -> "org.nlogo.core.prim.etc._minoneof"
       , "MIN-PXCOR"                     -> "org.nlogo.core.prim.etc._minpxcor"
       , "MIN-PYCOR"                     -> "org.nlogo.core.prim.etc._minpycor"
       , "MOD"                           -> "org.nlogo.core.prim.etc._mod"
       , "MODES"                         -> "org.nlogo.core.prim.etc._modes"
       , "MOUSE-DOWN?"                   -> "org.nlogo.core.prim.etc._mousedown"
       , "MOUSE-INSIDE?"                 -> "org.nlogo.core.prim.etc._mouseinside"
       , "MOUSE-XCOR"                    -> "org.nlogo.core.prim.etc._mousexcor"
       , "MOUSE-YCOR"                    -> "org.nlogo.core.prim.etc._mouseycor"
       , "MY-IN-LINKS"                   -> "org.nlogo.core.prim.etc._myinlinks"
       , "MY-LINKS"                      -> "org.nlogo.core.prim.etc._mylinks"
       , "MY-OUT-LINKS"                  -> "org.nlogo.core.prim.etc._myoutlinks"
       , "MYSELF"                        -> "org.nlogo.core.prim.etc._myself"
       , "N-OF"                          -> "org.nlogo.core.prim.etc._nof"
       , "N-VALUES"                      -> "org.nlogo.core.prim.etc._nvalues"
       , "NEIGHBORS"                     -> "org.nlogo.core.prim._neighbors"
       , "NEIGHBORS4"                    -> "org.nlogo.core.prim._neighbors4"
       , "NETLOGO-APPLET?"               -> "org.nlogo.core.prim.etc._netlogoapplet"
       , "NETLOGO-VERSION"               -> "org.nlogo.core.prim.etc._netlogoversion"
       , "NETLOGO-WEB?"                  -> "org.nlogo.core.prim.etc._netlogoweb"
       , "NEW-SEED"                      -> "org.nlogo.core.prim.etc._newseed"
       , "NO-LINKS"                      -> "org.nlogo.core.prim.etc._nolinks"
       , "NO-PATCHES"                    -> "org.nlogo.core.prim.etc._nopatches"
       , "NO-TURTLES"                    -> "org.nlogo.core.prim.etc._noturtles"
       , "NOT"                           -> "org.nlogo.core.prim._not"
       , "OF"                            -> "org.nlogo.core.prim._of"
       , "ONE-OF"                        -> "org.nlogo.core.prim._oneof"
       , "OR"                            -> "org.nlogo.core.prim._or"
       , "OTHER"                         -> "org.nlogo.core.prim._other"
       , "OTHER-END"                     -> "org.nlogo.core.prim.etc._otherend"
       , "OUT-LINK-NEIGHBOR?"            -> "org.nlogo.core.prim.etc._outlinkneighbor"
       , "OUT-LINK-NEIGHBORS"            -> "org.nlogo.core.prim.etc._outlinkneighbors"
       , "OUT-LINK-TO"                   -> "org.nlogo.core.prim.etc._outlinkto"
       , "PATCH"                         -> "org.nlogo.core.prim.etc._patch"
       , "PATCH-AHEAD"                   -> "org.nlogo.core.prim.etc._patchahead"
       , "PATCH-AT"                      -> "org.nlogo.core.prim._patchat"
       , "PATCH-AT-HEADING-AND-DISTANCE" -> "org.nlogo.core.prim.etc._patchatheadinganddistance"
       , "PATCH-HERE"                    -> "org.nlogo.core.prim.etc._patchhere"
       , "PATCH-LEFT-AND-AHEAD"          -> "org.nlogo.core.prim.etc._patchleftandahead"
       , "PATCH-RIGHT-AND-AHEAD"         -> "org.nlogo.core.prim.etc._patchrightandahead"
       , "PATCH-SET"                     -> "org.nlogo.core.prim.etc._patchset"
       , "PATCH-SIZE"                    -> "org.nlogo.core.prim.etc._patchsize"
       , "PATCHES"                       -> "org.nlogo.core.prim._patches"
       , "PLOT-NAME"                     -> "org.nlogo.core.prim.etc._plotname"
       , "PLOT-PEN-EXISTS?"              -> "org.nlogo.core.prim.etc._plotpenexists"
       , "PLOT-X-MAX"                    -> "org.nlogo.core.prim.etc._plotxmax"
       , "PLOT-X-MIN"                    -> "org.nlogo.core.prim.etc._plotxmin"
       , "PLOT-Y-MAX"                    -> "org.nlogo.core.prim.etc._plotymax"
       , "PLOT-Y-MIN"                    -> "org.nlogo.core.prim.etc._plotymin"
       , "POSITION"                      -> "org.nlogo.core.prim.etc._position"
       , "PRECISION"                     -> "org.nlogo.core.prim.etc._precision"
       , "RANDOM"                        -> "org.nlogo.core.prim._random"
       , "RANDOM-EXPONENTIAL"            -> "org.nlogo.core.prim.etc._randomexponential"
       , "RANDOM-FLOAT"                  -> "org.nlogo.core.prim.etc._randomfloat"
       , "RANDOM-GAMMA"                  -> "org.nlogo.core.prim.etc._randomgamma"
       , "RANDOM-NORMAL"                 -> "org.nlogo.core.prim.etc._randomnormal"
       , "RANDOM-POISSON"                -> "org.nlogo.core.prim.etc._randompoisson"
       , "RANDOM-PXCOR"                  -> "org.nlogo.core.prim.etc._randompxcor"
       , "RANDOM-PYCOR"                  -> "org.nlogo.core.prim.etc._randompycor"
       , "RANDOM-XCOR"                   -> "org.nlogo.core.prim.etc._randomxcor"
       , "RANDOM-YCOR"                   -> "org.nlogo.core.prim.etc._randomycor"
       , "RANGE"                         -> "org.nlogo.core.prim.etc._range"
       , "READ-FROM-STRING"              -> "org.nlogo.core.prim.etc._readfromstring"
       , "REDUCE"                        -> "org.nlogo.core.prim.etc._reduce"
       , "REMAINDER"                     -> "org.nlogo.core.prim.etc._remainder"
       , "REMOVE"                        -> "org.nlogo.core.prim.etc._remove"
       , "REMOVE-DUPLICATES"             -> "org.nlogo.core.prim.etc._removeduplicates"
       , "REMOVE-ITEM"                   -> "org.nlogo.core.prim.etc._removeitem"
       , "REPLACE-ITEM"                  -> "org.nlogo.core.prim.etc._replaceitem"
       , "REVERSE"                       -> "org.nlogo.core.prim.etc._reverse"
       , "RGB"                           -> "org.nlogo.core.prim.etc._rgb"
       , "ROUND"                         -> "org.nlogo.core.prim.etc._round"
       , "RUN-RESULT"                    -> "org.nlogo.core.prim.etc._runresult"
       , "RUNRESULT"                     -> "org.nlogo.core.prim.etc._runresult"
       , "SCALE-COLOR"                   -> "org.nlogo.core.prim.etc._scalecolor"
       , "SE"                            -> "org.nlogo.core.prim._sentence"
       , "SELF"                          -> "org.nlogo.core.prim.etc._self"
       , "SENTENCE"                      -> "org.nlogo.core.prim._sentence"
       , "SHADE-OF?"                     -> "org.nlogo.core.prim.etc._shadeof"
       , "SHAPES"                        -> "org.nlogo.core.prim.etc._shapes"
       , "SHUFFLE"                       -> "org.nlogo.core.prim.etc._shuffle"
       , "SIN"                           -> "org.nlogo.core.prim.etc._sin"
       , "SORT"                          -> "org.nlogo.core.prim.etc._sort"
       , "SORT-BY"                       -> "org.nlogo.core.prim.etc._sortby"
       , "SORT-ON"                       -> "org.nlogo.core.prim.etc._sorton"
       , "SQRT"                          -> "org.nlogo.core.prim.etc._sqrt"
       , "STANDARD-DEVIATION"            -> "org.nlogo.core.prim.etc._standarddeviation"
       , "SUBJECT"                       -> "org.nlogo.core.prim.etc._subject"
       , "SUBLIST"                       -> "org.nlogo.core.prim.etc._sublist"
       , "SUBSTRING"                     -> "org.nlogo.core.prim.etc._substring"
       , "SUBTRACT-HEADINGS"             -> "org.nlogo.core.prim.etc._subtractheadings"
       , "SUM"                           -> "org.nlogo.core.prim._sum"
       , "TAN"                           -> "org.nlogo.core.prim.etc._tan"
       , "TICKS"                         -> "org.nlogo.core.prim.etc._ticks"
       , "TIMER"                         -> "org.nlogo.core.prim.etc._timer"
       , "TOWARDS"                       -> "org.nlogo.core.prim.etc._towards"
       , "TOWARDSXY"                     -> "org.nlogo.core.prim.etc._towardsxy"
       , "TURTLE"                        -> "org.nlogo.core.prim._turtle"
       , "TURTLE-SET"                    -> "org.nlogo.core.prim.etc._turtleset"
       , "TURTLES"                       -> "org.nlogo.core.prim._turtles"
       , "TURTLES-AT"                    -> "org.nlogo.core.prim.etc._turtlesat"
       , "TURTLES-HERE"                  -> "org.nlogo.core.prim.etc._turtleshere"
       , "TURTLES-ON"                    -> "org.nlogo.core.prim._turtleson"
       , "UP-TO-N-OF"                    -> "org.nlogo.core.prim.etc._uptonof"
       , "USER-DIRECTORY"                -> "org.nlogo.core.prim.etc._userdirectory"
       , "USER-FILE"                     -> "org.nlogo.core.prim.etc._userfile"
       , "USER-INPUT"                    -> "org.nlogo.core.prim.etc._userinput"
       , "USER-NEW-FILE"                 -> "org.nlogo.core.prim.etc._usernewfile"
       , "USER-ONE-OF"                   -> "org.nlogo.core.prim.etc._useroneof"
       , "USER-YES-OR-NO?"               -> "org.nlogo.core.prim.etc._useryesorno"
       , "VARIANCE"                      -> "org.nlogo.core.prim.etc._variance"
       , "WHO-ARE-NOT"                   -> "org.nlogo.core.prim._whoarenot"
       , "WITH"                          -> "org.nlogo.core.prim._with"
       , "WITH-MAX"                      -> "org.nlogo.core.prim.etc._withmax"
       , "WITH-MIN"                      -> "org.nlogo.core.prim.etc._withmin"
       , "WORD"                          -> "org.nlogo.core.prim._word"
       , "WORLD-HEIGHT"                  -> "org.nlogo.core.prim.etc._worldheight"
       , "WORLD-WIDTH"                   -> "org.nlogo.core.prim.etc._worldwidth"
       , "WRAP-COLOR"                    -> "org.nlogo.core.prim.etc._wrapcolor"
       , "XOR"                           -> "org.nlogo.core.prim.etc._xor"
       )

  def commandPrimToInstance(name: String): Option[Command] =
    commandClassNames.get(name).flatMap(commandClassToInstance)

  def commandClassToInstance: (String) => Option[Command] =
    ({
      case "org.nlogo.core.prim._askconcurrent"                     => new org.nlogo.core.prim._askconcurrent
      case "org.nlogo.core.prim._ask"                               => new org.nlogo.core.prim._ask
      case "org.nlogo.core.prim._bk"                                => new org.nlogo.core.prim._bk
      case "org.nlogo.core.prim._carefully"                         => new org.nlogo.core.prim._carefully
      case "org.nlogo.core.prim._createorderedturtles"              => new org.nlogo.core.prim._createorderedturtles
      case "org.nlogo.core.prim._createturtles"                     => new org.nlogo.core.prim._createturtles
      case "org.nlogo.core.prim._done"                              => new org.nlogo.core.prim._done
      case "org.nlogo.core.prim.etc._apply"                         => new org.nlogo.core.prim.etc._apply
      case "org.nlogo.core.prim.etc._autoplotoff"                   => new org.nlogo.core.prim.etc._autoplotoff
      case "org.nlogo.core.prim.etc._autoploton"                    => new org.nlogo.core.prim.etc._autoploton
      case "org.nlogo.core.prim.etc._autoplotxoff"                  => new org.nlogo.core.prim.etc._autoplotxoff
      case "org.nlogo.core.prim.etc._autoplotxon"                   => new org.nlogo.core.prim.etc._autoplotxon
      case "org.nlogo.core.prim.etc._autoplotyoff"                  => new org.nlogo.core.prim.etc._autoplotyoff
      case "org.nlogo.core.prim.etc._autoplotyon"                   => new org.nlogo.core.prim.etc._autoplotyon
      case "org.nlogo.core.prim.etc._beep"                          => new org.nlogo.core.prim.etc._beep
      case "org.nlogo.core.prim.etc._bench"                         => new org.nlogo.core.prim.etc._bench
      case "org.nlogo.core.prim.etc._clearallandresetticks"         => new org.nlogo.core.prim.etc._clearallandresetticks
      case "org.nlogo.core.prim.etc._clearall"                      => new org.nlogo.core.prim.etc._clearall
      case "org.nlogo.core.prim.etc._clearallplots"                 => new org.nlogo.core.prim.etc._clearallplots
      case "org.nlogo.core.prim.etc._cleardrawing"                  => new org.nlogo.core.prim.etc._cleardrawing
      case "org.nlogo.core.prim.etc._clearglobals"                  => new org.nlogo.core.prim.etc._clearglobals
      case "org.nlogo.core.prim.etc._clearlinks"                    => new org.nlogo.core.prim.etc._clearlinks
      case "org.nlogo.core.prim.etc._clearoutput"                   => new org.nlogo.core.prim.etc._clearoutput
      case "org.nlogo.core.prim.etc._clearpatches"                  => new org.nlogo.core.prim.etc._clearpatches
      case "org.nlogo.core.prim.etc._clearplot"                     => new org.nlogo.core.prim.etc._clearplot
      case "org.nlogo.core.prim.etc._clearticks"                    => new org.nlogo.core.prim.etc._clearticks
      case "org.nlogo.core.prim.etc._clearturtles"                  => new org.nlogo.core.prim.etc._clearturtles
      case "org.nlogo.core.prim.etc._createlinkfrom"                => new org.nlogo.core.prim.etc._createlinkfrom
      case "org.nlogo.core.prim.etc._createlinksfrom"               => new org.nlogo.core.prim.etc._createlinksfrom
      case "org.nlogo.core.prim.etc._createlinksto"                 => new org.nlogo.core.prim.etc._createlinksto
      case "org.nlogo.core.prim.etc._createlinkswith"               => new org.nlogo.core.prim.etc._createlinkswith
      case "org.nlogo.core.prim.etc._createlinkto"                  => new org.nlogo.core.prim.etc._createlinkto
      case "org.nlogo.core.prim.etc._createlinkwith"                => new org.nlogo.core.prim.etc._createlinkwith
      case "org.nlogo.core.prim.etc._createtemporaryplotpen"        => new org.nlogo.core.prim.etc._createtemporaryplotpen
      case "org.nlogo.core.prim.etc._die"                           => new org.nlogo.core.prim.etc._die
      case "org.nlogo.core.prim.etc._diffuse4"                      => new org.nlogo.core.prim.etc._diffuse4
      case "org.nlogo.core.prim.etc._diffuse"                       => new org.nlogo.core.prim.etc._diffuse
      case "org.nlogo.core.prim.etc._display"                       => new org.nlogo.core.prim.etc._display
      case "org.nlogo.core.prim.etc._downhill4"                     => new org.nlogo.core.prim.etc._downhill4
      case "org.nlogo.core.prim.etc._downhill"                      => new org.nlogo.core.prim.etc._downhill
      case "org.nlogo.core.prim.etc._error"                         => new org.nlogo.core.prim.etc._error
      case "org.nlogo.core.prim.etc._every"                         => new org.nlogo.core.prim.etc._every
      case "org.nlogo.core.prim.etc._experimentstepend"             => new org.nlogo.core.prim.etc._experimentstepend
      case "org.nlogo.core.prim.etc._exportdrawing"                 => new org.nlogo.core.prim.etc._exportdrawing
      case "org.nlogo.core.prim.etc._exportinterface"               => new org.nlogo.core.prim.etc._exportinterface
      case "org.nlogo.core.prim.etc._exportoutput"                  => new org.nlogo.core.prim.etc._exportoutput
      case "org.nlogo.core.prim.etc._exportplot"                    => new org.nlogo.core.prim.etc._exportplot
      case "org.nlogo.core.prim.etc._exportplots"                   => new org.nlogo.core.prim.etc._exportplots
      case "org.nlogo.core.prim.etc._exportview"                    => new org.nlogo.core.prim.etc._exportview
      case "org.nlogo.core.prim.etc._exportworld"                   => new org.nlogo.core.prim.etc._exportworld
      case "org.nlogo.core.prim.etc._face"                          => new org.nlogo.core.prim.etc._face
      case "org.nlogo.core.prim.etc._facexy"                        => new org.nlogo.core.prim.etc._facexy
      case "org.nlogo.core.prim.etc._filecloseall"                  => new org.nlogo.core.prim.etc._filecloseall
      case "org.nlogo.core.prim.etc._fileclose"                     => new org.nlogo.core.prim.etc._fileclose
      case "org.nlogo.core.prim.etc._filedelete"                    => new org.nlogo.core.prim.etc._filedelete
      case "org.nlogo.core.prim.etc._fileflush"                     => new org.nlogo.core.prim.etc._fileflush
      case "org.nlogo.core.prim.etc._fileopen"                      => new org.nlogo.core.prim.etc._fileopen
      case "org.nlogo.core.prim.etc._fileprint"                     => new org.nlogo.core.prim.etc._fileprint
      case "org.nlogo.core.prim.etc._fileshow"                      => new org.nlogo.core.prim.etc._fileshow
      case "org.nlogo.core.prim.etc._filetype"                      => new org.nlogo.core.prim.etc._filetype
      case "org.nlogo.core.prim.etc._filewrite"                     => new org.nlogo.core.prim.etc._filewrite
      case "org.nlogo.core.prim.etc._followme"                      => new org.nlogo.core.prim.etc._followme
      case "org.nlogo.core.prim.etc._follow"                        => new org.nlogo.core.prim.etc._follow
      case "org.nlogo.core.prim.etc._foreach"                       => new org.nlogo.core.prim.etc._foreach
      case "org.nlogo.core.prim.etc._foreverbuttonend"              => new org.nlogo.core.prim.etc._foreverbuttonend
      case "org.nlogo.core.prim.etc._hidelink"                      => new org.nlogo.core.prim.etc._hidelink
      case "org.nlogo.core.prim.etc._hideturtle"                    => new org.nlogo.core.prim.etc._hideturtle
      case "org.nlogo.core.prim.etc._histogram"                     => new org.nlogo.core.prim.etc._histogram
      case "org.nlogo.core.prim.etc._home"                          => new org.nlogo.core.prim.etc._home
      case "org.nlogo.core.prim.etc._ifelse"                        => new org.nlogo.core.prim.etc._ifelse
      case "org.nlogo.core.prim.etc._if"                            => new org.nlogo.core.prim.etc._if
      case "org.nlogo.core.prim.etc._ignore"                        => new org.nlogo.core.prim.etc._ignore
      case "org.nlogo.core.prim.etc._importdrawing"                 => new org.nlogo.core.prim.etc._importdrawing
      case "org.nlogo.core.prim.etc._importpatchcolors"             => new org.nlogo.core.prim.etc._importpatchcolors
      case "org.nlogo.core.prim.etc._importpcolorsrgb"              => new org.nlogo.core.prim.etc._importpcolorsrgb
      case "org.nlogo.core.prim.etc._importworld"                   => new org.nlogo.core.prim.etc._importworld
      case "org.nlogo.core.prim.etc._inspect"                       => new org.nlogo.core.prim.etc._inspect
      case "org.nlogo.core.prim.etc._layoutcircle"                  => new org.nlogo.core.prim.etc._layoutcircle
      case "org.nlogo.core.prim.etc._layoutradial"                  => new org.nlogo.core.prim.etc._layoutradial
      case "org.nlogo.core.prim.etc._layoutspring"                  => new org.nlogo.core.prim.etc._layoutspring
      case "org.nlogo.core.prim.etc._layouttutte"                   => new org.nlogo.core.prim.etc._layouttutte
      case "org.nlogo.core.prim.etc._left"                          => new org.nlogo.core.prim.etc._left
      case "org.nlogo.core.prim.etc._linkcode"                      => new org.nlogo.core.prim.etc._linkcode
      case "org.nlogo.core.prim.etc._loop"                          => new org.nlogo.core.prim.etc._loop
      case "org.nlogo.core.prim.etc._mkdir"                         => new org.nlogo.core.prim.etc._mkdir
      case "org.nlogo.core.prim.etc._moveto"                        => new org.nlogo.core.prim.etc._moveto
      case "org.nlogo.core.prim.etc._nodisplay"                     => new org.nlogo.core.prim.etc._nodisplay
      case "org.nlogo.core.prim.etc._observercode"                  => new org.nlogo.core.prim.etc._observercode
      case "org.nlogo.core.prim.etc._outputprint"                   => new org.nlogo.core.prim.etc._outputprint
      case "org.nlogo.core.prim.etc._outputshow"                    => new org.nlogo.core.prim.etc._outputshow
      case "org.nlogo.core.prim.etc._outputtype"                    => new org.nlogo.core.prim.etc._outputtype
      case "org.nlogo.core.prim.etc._outputwrite"                   => new org.nlogo.core.prim.etc._outputwrite
      case "org.nlogo.core.prim.etc._patchcode"                     => new org.nlogo.core.prim.etc._patchcode
      case "org.nlogo.core.prim.etc._pendown"                       => new org.nlogo.core.prim.etc._pendown
      case "org.nlogo.core.prim.etc._penerase"                      => new org.nlogo.core.prim.etc._penerase
      case "org.nlogo.core.prim.etc._penup"                         => new org.nlogo.core.prim.etc._penup
      case "org.nlogo.core.prim.etc._plot"                          => new org.nlogo.core.prim.etc._plot
      case "org.nlogo.core.prim.etc._plotpendown"                   => new org.nlogo.core.prim.etc._plotpendown
      case "org.nlogo.core.prim.etc._plotpenhide"                   => new org.nlogo.core.prim.etc._plotpenhide
      case "org.nlogo.core.prim.etc._plotpenreset"                  => new org.nlogo.core.prim.etc._plotpenreset
      case "org.nlogo.core.prim.etc._plotpenshow"                   => new org.nlogo.core.prim.etc._plotpenshow
      case "org.nlogo.core.prim.etc._plotpenup"                     => new org.nlogo.core.prim.etc._plotpenup
      case "org.nlogo.core.prim.etc._plotxy"                        => new org.nlogo.core.prim.etc._plotxy
      case "org.nlogo.core.prim.etc._print"                         => new org.nlogo.core.prim.etc._print
      case "org.nlogo.core.prim.etc._pwd"                           => new org.nlogo.core.prim.etc._pwd
      case "org.nlogo.core.prim.etc._randomseed"                    => new org.nlogo.core.prim.etc._randomseed
      case "org.nlogo.core.prim.etc._reloadextensions"              => new org.nlogo.core.prim.etc._reloadextensions
      case "org.nlogo.core.prim.etc._resetperspective"              => new org.nlogo.core.prim.etc._resetperspective
      case "org.nlogo.core.prim.etc._resetticks"                    => new org.nlogo.core.prim.etc._resetticks
      case "org.nlogo.core.prim.etc._resettimer"                    => new org.nlogo.core.prim.etc._resettimer
      case "org.nlogo.core.prim.etc._resizeworld"                   => new org.nlogo.core.prim.etc._resizeworld
      case "org.nlogo.core.prim.etc._rideme"                        => new org.nlogo.core.prim.etc._rideme
      case "org.nlogo.core.prim.etc._ride"                          => new org.nlogo.core.prim.etc._ride
      case "org.nlogo.core.prim.etc._right"                         => new org.nlogo.core.prim.etc._right
      case "org.nlogo.core.prim.etc._setcurdir"                     => new org.nlogo.core.prim.etc._setcurdir
      case "org.nlogo.core.prim.etc._setcurrentplot"                => new org.nlogo.core.prim.etc._setcurrentplot
      case "org.nlogo.core.prim.etc._setcurrentplotpen"             => new org.nlogo.core.prim.etc._setcurrentplotpen
      case "org.nlogo.core.prim.etc._setdefaultshape"               => new org.nlogo.core.prim.etc._setdefaultshape
      case "org.nlogo.core.prim.etc._sethistogramnumbars"           => new org.nlogo.core.prim.etc._sethistogramnumbars
      case "org.nlogo.core.prim.etc._setlinethickness"              => new org.nlogo.core.prim.etc._setlinethickness
      case "org.nlogo.core.prim.etc._setpatchsize"                  => new org.nlogo.core.prim.etc._setpatchsize
      case "org.nlogo.core.prim.etc._setplotpencolor"               => new org.nlogo.core.prim.etc._setplotpencolor
      case "org.nlogo.core.prim.etc._setplotpeninterval"            => new org.nlogo.core.prim.etc._setplotpeninterval
      case "org.nlogo.core.prim.etc._setplotpenmode"                => new org.nlogo.core.prim.etc._setplotpenmode
      case "org.nlogo.core.prim.etc._setplotxrange"                 => new org.nlogo.core.prim.etc._setplotxrange
      case "org.nlogo.core.prim.etc._setplotyrange"                 => new org.nlogo.core.prim.etc._setplotyrange
      case "org.nlogo.core.prim.etc._settopology"                   => new org.nlogo.core.prim.etc._settopology
      case "org.nlogo.core.prim.etc._setupplots"                    => new org.nlogo.core.prim.etc._setupplots
      case "org.nlogo.core.prim.etc._setxy"                         => new org.nlogo.core.prim.etc._setxy
      case "org.nlogo.core.prim.etc._showlink"                      => new org.nlogo.core.prim.etc._showlink
      case "org.nlogo.core.prim.etc._show"                          => new org.nlogo.core.prim.etc._show
      case "org.nlogo.core.prim.etc._showturtle"                    => new org.nlogo.core.prim.etc._showturtle
      case "org.nlogo.core.prim.etc._stamperase"                    => new org.nlogo.core.prim.etc._stamperase
      case "org.nlogo.core.prim.etc._stamp"                         => new org.nlogo.core.prim.etc._stamp
      case "org.nlogo.core.prim.etc._stderr"                        => new org.nlogo.core.prim.etc._stderr
      case "org.nlogo.core.prim.etc._stdout"                        => new org.nlogo.core.prim.etc._stdout
      case "org.nlogo.core.prim.etc._stopinspectingdeadagents"      => new org.nlogo.core.prim.etc._stopinspectingdeadagents
      case "org.nlogo.core.prim.etc._stopinspecting"                => new org.nlogo.core.prim.etc._stopinspecting
      case "org.nlogo.core.prim.etc._thunkdidfinish"                => new org.nlogo.core.prim.etc._thunkdidfinish
      case "org.nlogo.core.prim.etc._tickadvance"                   => new org.nlogo.core.prim.etc._tickadvance
      case "org.nlogo.core.prim.etc._tick"                          => new org.nlogo.core.prim.etc._tick
      case "org.nlogo.core.prim.etc._tie"                           => new org.nlogo.core.prim.etc._tie
      case "org.nlogo.core.prim.etc._turtlecode"                    => new org.nlogo.core.prim.etc._turtlecode
      case "org.nlogo.core.prim.etc._type"                          => new org.nlogo.core.prim.etc._type
      case "org.nlogo.core.prim.etc._untie"                         => new org.nlogo.core.prim.etc._untie
      case "org.nlogo.core.prim.etc._updateplots"                   => new org.nlogo.core.prim.etc._updateplots
      case "org.nlogo.core.prim.etc._uphill4"                       => new org.nlogo.core.prim.etc._uphill4
      case "org.nlogo.core.prim.etc._uphill"                        => new org.nlogo.core.prim.etc._uphill
      case "org.nlogo.core.prim.etc._usermessage"                   => new org.nlogo.core.prim.etc._usermessage
      case "org.nlogo.core.prim.etc._wait"                          => new org.nlogo.core.prim.etc._wait
      case "org.nlogo.core.prim.etc._watchme"                       => new org.nlogo.core.prim.etc._watchme
      case "org.nlogo.core.prim.etc._watch"                         => new org.nlogo.core.prim.etc._watch
      case "org.nlogo.core.prim.etc._while"                         => new org.nlogo.core.prim.etc._while
      case "org.nlogo.core.prim.etc._withlocalrandomness"           => new org.nlogo.core.prim.etc._withlocalrandomness
      case "org.nlogo.core.prim.etc._withoutinterruption"           => new org.nlogo.core.prim.etc._withoutinterruption
      case "org.nlogo.core.prim.etc._write"                         => new org.nlogo.core.prim.etc._write
      case "org.nlogo.core.prim._fd"                                => new org.nlogo.core.prim._fd
      case "org.nlogo.core.prim._hatch"                             => new org.nlogo.core.prim._hatch
      case "org.nlogo.core.prim.hubnet._hubnetbroadcastclearoutput" => new org.nlogo.core.prim.hubnet._hubnetbroadcastclearoutput
      case "org.nlogo.core.prim.hubnet._hubnetbroadcastmessage"     => new org.nlogo.core.prim.hubnet._hubnetbroadcastmessage
      case "org.nlogo.core.prim.hubnet._hubnetbroadcast"            => new org.nlogo.core.prim.hubnet._hubnetbroadcast
      case "org.nlogo.core.prim.hubnet._hubnetclearoverride"        => new org.nlogo.core.prim.hubnet._hubnetclearoverride
      case "org.nlogo.core.prim.hubnet._hubnetclearoverrides"       => new org.nlogo.core.prim.hubnet._hubnetclearoverrides
      case "org.nlogo.core.prim.hubnet._hubnetfetchmessage"         => new org.nlogo.core.prim.hubnet._hubnetfetchmessage
      case "org.nlogo.core.prim.hubnet._hubnetkickallclients"       => new org.nlogo.core.prim.hubnet._hubnetkickallclients
      case "org.nlogo.core.prim.hubnet._hubnetkickclient"           => new org.nlogo.core.prim.hubnet._hubnetkickclient
      case "org.nlogo.core.prim.hubnet._hubnetreset"                => new org.nlogo.core.prim.hubnet._hubnetreset
      case "org.nlogo.core.prim.hubnet._hubnetresetperspective"     => new org.nlogo.core.prim.hubnet._hubnetresetperspective
      case "org.nlogo.core.prim.hubnet._hubnetsendclearoutput"      => new org.nlogo.core.prim.hubnet._hubnetsendclearoutput
      case "org.nlogo.core.prim.hubnet._hubnetsendfollow"           => new org.nlogo.core.prim.hubnet._hubnetsendfollow
      case "org.nlogo.core.prim.hubnet._hubnetsendmessage"          => new org.nlogo.core.prim.hubnet._hubnetsendmessage
      case "org.nlogo.core.prim.hubnet._hubnetsend"                 => new org.nlogo.core.prim.hubnet._hubnetsend
      case "org.nlogo.core.prim.hubnet._hubnetsendoverride"         => new org.nlogo.core.prim.hubnet._hubnetsendoverride
      case "org.nlogo.core.prim.hubnet._hubnetsendwatch"            => new org.nlogo.core.prim.hubnet._hubnetsendwatch
      case "org.nlogo.core.prim._jump"                              => new org.nlogo.core.prim._jump
      case "org.nlogo.core.prim._let"                               => new org.nlogo.core.prim._let
      case "org.nlogo.core.prim._repeat"                            => new org.nlogo.core.prim._repeat
      case "org.nlogo.core.prim._report"                            => new org.nlogo.core.prim._report
      case "org.nlogo.core.prim._run"                               => new org.nlogo.core.prim._run
      case "org.nlogo.core.prim._set"                               => new org.nlogo.core.prim._set
      case "org.nlogo.core.prim._sprout"                            => new org.nlogo.core.prim._sprout
      case "org.nlogo.core.prim._stop"                              => new org.nlogo.core.prim._stop
    }: PartialFunction[String, Command]).lift

  def reporterPrimToInstance(name: String): Option[Reporter] =
    reporterClassNames.get(name).flatMap(reporterClassToInstance)

  def reporterClassToInstance: (String) => Option[Reporter] =
    ({
      case "org.nlogo.core.prim._and"                             => new org.nlogo.core.prim._and
      case "org.nlogo.core.prim._any"                             => new org.nlogo.core.prim._any
      case "org.nlogo.core.prim._count"                           => new org.nlogo.core.prim._count
      case "org.nlogo.core.prim._equal"                           => new org.nlogo.core.prim._equal
      case "org.nlogo.core.prim._errormessage"                    => new org.nlogo.core.prim._errormessage
      case "org.nlogo.core.prim.etc._abs"                         => new org.nlogo.core.prim.etc._abs
      case "org.nlogo.core.prim.etc._acos"                        => new org.nlogo.core.prim.etc._acos
      case "org.nlogo.core.prim.etc._all"                         => new org.nlogo.core.prim.etc._all
      case "org.nlogo.core.prim.etc._applyresult"                 => new org.nlogo.core.prim.etc._applyresult
      case "org.nlogo.core.prim.etc._approximatehsb"              => new org.nlogo.core.prim.etc._approximatehsb
      case "org.nlogo.core.prim.etc._approximatergb"              => new org.nlogo.core.prim.etc._approximatergb
      case "org.nlogo.core.prim.etc._asin"                        => new org.nlogo.core.prim.etc._asin
      case "org.nlogo.core.prim.etc._atan"                        => new org.nlogo.core.prim.etc._atan
      case "org.nlogo.core.prim.etc._atpoints"                    => new org.nlogo.core.prim.etc._atpoints
      case "org.nlogo.core.prim.etc._autoplot"                    => new org.nlogo.core.prim.etc._autoplot
      case "org.nlogo.core.prim.etc._autoplotx"                   => new org.nlogo.core.prim.etc._autoplotx
      case "org.nlogo.core.prim.etc._autoploty"                   => new org.nlogo.core.prim.etc._autoploty
      case "org.nlogo.core.prim.etc._basecolors"                  => new org.nlogo.core.prim.etc._basecolors
      case "org.nlogo.core.prim.etc._behaviorspaceexperimentname" => new org.nlogo.core.prim.etc._behaviorspaceexperimentname
      case "org.nlogo.core.prim.etc._behaviorspacerunnumber"      => new org.nlogo.core.prim.etc._behaviorspacerunnumber
      case "org.nlogo.core.prim.etc._block"                       => new org.nlogo.core.prim.etc._block
      case "org.nlogo.core.prim.etc._boom"                        => new org.nlogo.core.prim.etc._boom
      case "org.nlogo.core.prim.etc._bothends"                    => new org.nlogo.core.prim.etc._bothends
      case "org.nlogo.core.prim.etc._butfirst"                    => new org.nlogo.core.prim.etc._butfirst
      case "org.nlogo.core.prim.etc._butlast"                     => new org.nlogo.core.prim.etc._butlast
      case "org.nlogo.core.prim.etc._canmove"                     => new org.nlogo.core.prim.etc._canmove
      case "org.nlogo.core.prim.etc._ceil"                        => new org.nlogo.core.prim.etc._ceil
      case "org.nlogo.core.prim.etc._checksum"                    => new org.nlogo.core.prim.etc._checksum
      case "org.nlogo.core.prim.etc._checksyntax"                 => new org.nlogo.core.prim.etc._checksyntax
      case "org.nlogo.core.prim.etc._cos"                         => new org.nlogo.core.prim.etc._cos
      case "org.nlogo.core.prim.etc._dateandtime"                 => new org.nlogo.core.prim.etc._dateandtime
      case "org.nlogo.core.prim.etc._distance"                    => new org.nlogo.core.prim.etc._distance
      case "org.nlogo.core.prim.etc._distancexy"                  => new org.nlogo.core.prim.etc._distancexy
      case "org.nlogo.core.prim.etc._div"                         => new org.nlogo.core.prim.etc._div
      case "org.nlogo.core.prim.etc._dump1"                       => new org.nlogo.core.prim.etc._dump1
      case "org.nlogo.core.prim.etc._dumpextensionprims"          => new org.nlogo.core.prim.etc._dumpextensionprims
      case "org.nlogo.core.prim.etc._dumpextensions"              => new org.nlogo.core.prim.etc._dumpextensions
      case "org.nlogo.core.prim.etc._dump"                        => new org.nlogo.core.prim.etc._dump
      case "org.nlogo.core.prim.etc._dx"                          => new org.nlogo.core.prim.etc._dx
      case "org.nlogo.core.prim.etc._dy"                          => new org.nlogo.core.prim.etc._dy
      case "org.nlogo.core.prim.etc._empty"                       => new org.nlogo.core.prim.etc._empty
      case "org.nlogo.core.prim.etc._exp"                         => new org.nlogo.core.prim.etc._exp
      case "org.nlogo.core.prim.etc._extracthsb"                  => new org.nlogo.core.prim.etc._extracthsb
      case "org.nlogo.core.prim.etc._extractrgb"                  => new org.nlogo.core.prim.etc._extractrgb
      case "org.nlogo.core.prim.etc._fileatend"                   => new org.nlogo.core.prim.etc._fileatend
      case "org.nlogo.core.prim.etc._fileexists"                  => new org.nlogo.core.prim.etc._fileexists
      case "org.nlogo.core.prim.etc._filereadchars"               => new org.nlogo.core.prim.etc._filereadchars
      case "org.nlogo.core.prim.etc._filereadline"                => new org.nlogo.core.prim.etc._filereadline
      case "org.nlogo.core.prim.etc._fileread"                    => new org.nlogo.core.prim.etc._fileread
      case "org.nlogo.core.prim.etc._filter"                      => new org.nlogo.core.prim.etc._filter
      case "org.nlogo.core.prim.etc._first"                       => new org.nlogo.core.prim.etc._first
      case "org.nlogo.core.prim.etc._floor"                       => new org.nlogo.core.prim.etc._floor
      case "org.nlogo.core.prim.etc._fput"                        => new org.nlogo.core.prim.etc._fput
      case "org.nlogo.core.prim.etc._greaterorequal"              => new org.nlogo.core.prim.etc._greaterorequal
      case "org.nlogo.core.prim.etc._hsb"                         => new org.nlogo.core.prim.etc._hsb
      case "org.nlogo.core.prim.etc._ifelsevalue"                 => new org.nlogo.core.prim.etc._ifelsevalue
      case "org.nlogo.core.prim.etc._incone"                      => new org.nlogo.core.prim.etc._incone
      case "org.nlogo.core.prim.etc._inlinkfrom"                  => new org.nlogo.core.prim.etc._inlinkfrom
      case "org.nlogo.core.prim.etc._inlinkneighbor"              => new org.nlogo.core.prim.etc._inlinkneighbor
      case "org.nlogo.core.prim.etc._inlinkneighbors"             => new org.nlogo.core.prim.etc._inlinkneighbors
      case "org.nlogo.core.prim.etc._insertitem"                  => new org.nlogo.core.prim.etc._insertitem
      case "org.nlogo.core.prim.etc._int"                         => new org.nlogo.core.prim.etc._int
      case "org.nlogo.core.prim.etc._isagent"                     => new org.nlogo.core.prim.etc._isagent
      case "org.nlogo.core.prim.etc._isagentset"                  => new org.nlogo.core.prim.etc._isagentset
      case "org.nlogo.core.prim.etc._isanonymouscommand"          => new org.nlogo.core.prim.etc._isanonymouscommand
      case "org.nlogo.core.prim.etc._isanonymousreporter"         => new org.nlogo.core.prim.etc._isanonymousreporter
      case "org.nlogo.core.prim.etc._isboolean"                   => new org.nlogo.core.prim.etc._isboolean
      case "org.nlogo.core.prim.etc._isdirectedlink"              => new org.nlogo.core.prim.etc._isdirectedlink
      case "org.nlogo.core.prim.etc._islink"                      => new org.nlogo.core.prim.etc._islink
      case "org.nlogo.core.prim.etc._islinkset"                   => new org.nlogo.core.prim.etc._islinkset
      case "org.nlogo.core.prim.etc._islist"                      => new org.nlogo.core.prim.etc._islist
      case "org.nlogo.core.prim.etc._isnumber"                    => new org.nlogo.core.prim.etc._isnumber
      case "org.nlogo.core.prim.etc._ispatch"                     => new org.nlogo.core.prim.etc._ispatch
      case "org.nlogo.core.prim.etc._ispatchset"                  => new org.nlogo.core.prim.etc._ispatchset
      case "org.nlogo.core.prim.etc._isstring"                    => new org.nlogo.core.prim.etc._isstring
      case "org.nlogo.core.prim.etc._isturtle"                    => new org.nlogo.core.prim.etc._isturtle
      case "org.nlogo.core.prim.etc._isturtleset"                 => new org.nlogo.core.prim.etc._isturtleset
      case "org.nlogo.core.prim.etc._isundirectedlink"            => new org.nlogo.core.prim.etc._isundirectedlink
      case "org.nlogo.core.prim.etc._item"                        => new org.nlogo.core.prim.etc._item
      case "org.nlogo.core.prim.etc._last"                        => new org.nlogo.core.prim.etc._last
      case "org.nlogo.core.prim.etc._length"                      => new org.nlogo.core.prim.etc._length
      case "org.nlogo.core.prim.etc._lessorequal"                 => new org.nlogo.core.prim.etc._lessorequal
      case "org.nlogo.core.prim.etc._linkheading"                 => new org.nlogo.core.prim.etc._linkheading
      case "org.nlogo.core.prim.etc._linklength"                  => new org.nlogo.core.prim.etc._linklength
      case "org.nlogo.core.prim.etc._linkneighbor"                => new org.nlogo.core.prim.etc._linkneighbor
      case "org.nlogo.core.prim.etc._linkneighbors"               => new org.nlogo.core.prim.etc._linkneighbors
      case "org.nlogo.core.prim.etc._link"                        => new org.nlogo.core.prim.etc._link
      case "org.nlogo.core.prim.etc._linkset"                     => new org.nlogo.core.prim.etc._linkset
      case "org.nlogo.core.prim.etc._linkshapes"                  => new org.nlogo.core.prim.etc._linkshapes
      case "org.nlogo.core.prim.etc._links"                       => new org.nlogo.core.prim.etc._links
      case "org.nlogo.core.prim.etc._linkwith"                    => new org.nlogo.core.prim.etc._linkwith
      case "org.nlogo.core.prim.etc._ln"                          => new org.nlogo.core.prim.etc._ln
      case "org.nlogo.core.prim.etc._log"                         => new org.nlogo.core.prim.etc._log
      case "org.nlogo.core.prim.etc._lput"                        => new org.nlogo.core.prim.etc._lput
      case "org.nlogo.core.prim.etc._map"                         => new org.nlogo.core.prim.etc._map
      case "org.nlogo.core.prim.etc._max"                         => new org.nlogo.core.prim.etc._max
      case "org.nlogo.core.prim.etc._maxnof"                      => new org.nlogo.core.prim.etc._maxnof
      case "org.nlogo.core.prim.etc._maxoneof"                    => new org.nlogo.core.prim.etc._maxoneof
      case "org.nlogo.core.prim.etc._maxpxcor"                    => new org.nlogo.core.prim.etc._maxpxcor
      case "org.nlogo.core.prim.etc._maxpycor"                    => new org.nlogo.core.prim.etc._maxpycor
      case "org.nlogo.core.prim.etc._mean"                        => new org.nlogo.core.prim.etc._mean
      case "org.nlogo.core.prim.etc._median"                      => new org.nlogo.core.prim.etc._median
      case "org.nlogo.core.prim.etc._member"                      => new org.nlogo.core.prim.etc._member
      case "org.nlogo.core.prim.etc._min"                         => new org.nlogo.core.prim.etc._min
      case "org.nlogo.core.prim.etc._minnof"                      => new org.nlogo.core.prim.etc._minnof
      case "org.nlogo.core.prim.etc._minoneof"                    => new org.nlogo.core.prim.etc._minoneof
      case "org.nlogo.core.prim.etc._minpxcor"                    => new org.nlogo.core.prim.etc._minpxcor
      case "org.nlogo.core.prim.etc._minpycor"                    => new org.nlogo.core.prim.etc._minpycor
      case "org.nlogo.core.prim.etc._modes"                       => new org.nlogo.core.prim.etc._modes
      case "org.nlogo.core.prim.etc._mod"                         => new org.nlogo.core.prim.etc._mod
      case "org.nlogo.core.prim.etc._mousedown"                   => new org.nlogo.core.prim.etc._mousedown
      case "org.nlogo.core.prim.etc._mouseinside"                 => new org.nlogo.core.prim.etc._mouseinside
      case "org.nlogo.core.prim.etc._mousexcor"                   => new org.nlogo.core.prim.etc._mousexcor
      case "org.nlogo.core.prim.etc._mouseycor"                   => new org.nlogo.core.prim.etc._mouseycor
      case "org.nlogo.core.prim.etc._mult"                        => new org.nlogo.core.prim.etc._mult
      case "org.nlogo.core.prim.etc._myinlinks"                   => new org.nlogo.core.prim.etc._myinlinks
      case "org.nlogo.core.prim.etc._mylinks"                     => new org.nlogo.core.prim.etc._mylinks
      case "org.nlogo.core.prim.etc._myoutlinks"                  => new org.nlogo.core.prim.etc._myoutlinks
      case "org.nlogo.core.prim.etc._myself"                      => new org.nlogo.core.prim.etc._myself
      case "org.nlogo.core.prim.etc._nanotime"                    => new org.nlogo.core.prim.etc._nanotime
      case "org.nlogo.core.prim.etc._netlogoapplet"               => new org.nlogo.core.prim.etc._netlogoapplet
      case "org.nlogo.core.prim.etc._netlogoversion"              => new org.nlogo.core.prim.etc._netlogoversion
      case "org.nlogo.core.prim.etc._netlogoweb"                  => new org.nlogo.core.prim.etc._netlogoweb
      case "org.nlogo.core.prim.etc._newseed"                     => new org.nlogo.core.prim.etc._newseed
      case "org.nlogo.core.prim.etc._nof"                         => new org.nlogo.core.prim.etc._nof
      case "org.nlogo.core.prim.etc._nolinks"                     => new org.nlogo.core.prim.etc._nolinks
      case "org.nlogo.core.prim.etc._nopatches"                   => new org.nlogo.core.prim.etc._nopatches
      case "org.nlogo.core.prim.etc._noturtles"                   => new org.nlogo.core.prim.etc._noturtles
      case "org.nlogo.core.prim.etc._nvalues"                     => new org.nlogo.core.prim.etc._nvalues
      case "org.nlogo.core.prim.etc._otherend"                    => new org.nlogo.core.prim.etc._otherend
      case "org.nlogo.core.prim.etc._outlinkneighbor"             => new org.nlogo.core.prim.etc._outlinkneighbor
      case "org.nlogo.core.prim.etc._outlinkneighbors"            => new org.nlogo.core.prim.etc._outlinkneighbors
      case "org.nlogo.core.prim.etc._outlinkto"                   => new org.nlogo.core.prim.etc._outlinkto
      case "org.nlogo.core.prim.etc._patchahead"                  => new org.nlogo.core.prim.etc._patchahead
      case "org.nlogo.core.prim.etc._patchatheadinganddistance"   => new org.nlogo.core.prim.etc._patchatheadinganddistance
      case "org.nlogo.core.prim.etc._patchhere"                   => new org.nlogo.core.prim.etc._patchhere
      case "org.nlogo.core.prim.etc._patchleftandahead"           => new org.nlogo.core.prim.etc._patchleftandahead
      case "org.nlogo.core.prim.etc._patch"                       => new org.nlogo.core.prim.etc._patch
      case "org.nlogo.core.prim.etc._patchrightandahead"          => new org.nlogo.core.prim.etc._patchrightandahead
      case "org.nlogo.core.prim.etc._patchset"                    => new org.nlogo.core.prim.etc._patchset
      case "org.nlogo.core.prim.etc._patchsize"                   => new org.nlogo.core.prim.etc._patchsize
      case "org.nlogo.core.prim.etc._plotname"                    => new org.nlogo.core.prim.etc._plotname
      case "org.nlogo.core.prim.etc._plotpenexists"               => new org.nlogo.core.prim.etc._plotpenexists
      case "org.nlogo.core.prim.etc._plotxmax"                    => new org.nlogo.core.prim.etc._plotxmax
      case "org.nlogo.core.prim.etc._plotxmin"                    => new org.nlogo.core.prim.etc._plotxmin
      case "org.nlogo.core.prim.etc._plotymax"                    => new org.nlogo.core.prim.etc._plotymax
      case "org.nlogo.core.prim.etc._plotymin"                    => new org.nlogo.core.prim.etc._plotymin
      case "org.nlogo.core.prim.etc._plus"                        => new org.nlogo.core.prim.etc._plus
      case "org.nlogo.core.prim.etc._position"                    => new org.nlogo.core.prim.etc._position
      case "org.nlogo.core.prim.etc._pow"                         => new org.nlogo.core.prim.etc._pow
      case "org.nlogo.core.prim.etc._precision"                   => new org.nlogo.core.prim.etc._precision
      case "org.nlogo.core.prim.etc._processors"                  => new org.nlogo.core.prim.etc._processors
      case "org.nlogo.core.prim.etc._randomexponential"           => new org.nlogo.core.prim.etc._randomexponential
      case "org.nlogo.core.prim.etc._randomfloat"                 => new org.nlogo.core.prim.etc._randomfloat
      case "org.nlogo.core.prim.etc._randomgamma"                 => new org.nlogo.core.prim.etc._randomgamma
      case "org.nlogo.core.prim.etc._randomnormal"                => new org.nlogo.core.prim.etc._randomnormal
      case "org.nlogo.core.prim.etc._randompoisson"               => new org.nlogo.core.prim.etc._randompoisson
      case "org.nlogo.core.prim.etc._randompxcor"                 => new org.nlogo.core.prim.etc._randompxcor
      case "org.nlogo.core.prim.etc._randompycor"                 => new org.nlogo.core.prim.etc._randompycor
      case "org.nlogo.core.prim.etc._randomstate"                 => new org.nlogo.core.prim.etc._randomstate
      case "org.nlogo.core.prim.etc._randomxcor"                  => new org.nlogo.core.prim.etc._randomxcor
      case "org.nlogo.core.prim.etc._randomycor"                  => new org.nlogo.core.prim.etc._randomycor
      case "org.nlogo.core.prim.etc._range"                       => new org.nlogo.core.prim.etc._range
      case "org.nlogo.core.prim.etc._readfromstring"              => new org.nlogo.core.prim.etc._readfromstring
      case "org.nlogo.core.prim.etc._reduce"                      => new org.nlogo.core.prim.etc._reduce
      case "org.nlogo.core.prim.etc._reference"                   => new org.nlogo.core.prim.etc._reference
      case "org.nlogo.core.prim.etc._remainder"                   => new org.nlogo.core.prim.etc._remainder
      case "org.nlogo.core.prim.etc._removeduplicates"            => new org.nlogo.core.prim.etc._removeduplicates
      case "org.nlogo.core.prim.etc._removeitem"                  => new org.nlogo.core.prim.etc._removeitem
      case "org.nlogo.core.prim.etc._remove"                      => new org.nlogo.core.prim.etc._remove
      case "org.nlogo.core.prim.etc._replaceitem"                 => new org.nlogo.core.prim.etc._replaceitem
      case "org.nlogo.core.prim.etc._reverse"                     => new org.nlogo.core.prim.etc._reverse
      case "org.nlogo.core.prim.etc._rgb"                         => new org.nlogo.core.prim.etc._rgb
      case "org.nlogo.core.prim.etc._round"                       => new org.nlogo.core.prim.etc._round
      case "org.nlogo.core.prim.etc._runresult"                   => new org.nlogo.core.prim.etc._runresult
      case "org.nlogo.core.prim.etc._scalecolor"                  => new org.nlogo.core.prim.etc._scalecolor
      case "org.nlogo.core.prim.etc._self"                        => new org.nlogo.core.prim.etc._self
      case "org.nlogo.core.prim.etc._shadeof"                     => new org.nlogo.core.prim.etc._shadeof
      case "org.nlogo.core.prim.etc._shapes"                      => new org.nlogo.core.prim.etc._shapes
      case "org.nlogo.core.prim.etc._shuffle"                     => new org.nlogo.core.prim.etc._shuffle
      case "org.nlogo.core.prim.etc._sin"                         => new org.nlogo.core.prim.etc._sin
      case "org.nlogo.core.prim.etc._sortby"                      => new org.nlogo.core.prim.etc._sortby
      case "org.nlogo.core.prim.etc._sort"                        => new org.nlogo.core.prim.etc._sort
      case "org.nlogo.core.prim.etc._sorton"                      => new org.nlogo.core.prim.etc._sorton
      case "org.nlogo.core.prim.etc._sqrt"                        => new org.nlogo.core.prim.etc._sqrt
      case "org.nlogo.core.prim.etc._stacktrace"                  => new org.nlogo.core.prim.etc._stacktrace
      case "org.nlogo.core.prim.etc._standarddeviation"           => new org.nlogo.core.prim.etc._standarddeviation
      case "org.nlogo.core.prim.etc._subject"                     => new org.nlogo.core.prim.etc._subject
      case "org.nlogo.core.prim.etc._sublist"                     => new org.nlogo.core.prim.etc._sublist
      case "org.nlogo.core.prim.etc._substring"                   => new org.nlogo.core.prim.etc._substring
      case "org.nlogo.core.prim.etc._subtractheadings"            => new org.nlogo.core.prim.etc._subtractheadings
      case "org.nlogo.core.prim.etc._symbolstring"                => new org.nlogo.core.prim.etc._symbolstring
      case "org.nlogo.core.prim.etc._tan"                         => new org.nlogo.core.prim.etc._tan
      case "org.nlogo.core.prim.etc._ticks"                       => new org.nlogo.core.prim.etc._ticks
      case "org.nlogo.core.prim.etc._timer"                       => new org.nlogo.core.prim.etc._timer
      case "org.nlogo.core.prim.etc._tostring"                    => new org.nlogo.core.prim.etc._tostring
      case "org.nlogo.core.prim.etc._towards"                     => new org.nlogo.core.prim.etc._towards
      case "org.nlogo.core.prim.etc._towardsxy"                   => new org.nlogo.core.prim.etc._towardsxy
      case "org.nlogo.core.prim.etc._turtlesat"                   => new org.nlogo.core.prim.etc._turtlesat
      case "org.nlogo.core.prim.etc._turtleset"                   => new org.nlogo.core.prim.etc._turtleset
      case "org.nlogo.core.prim.etc._turtleshere"                 => new org.nlogo.core.prim.etc._turtleshere
      case "org.nlogo.core.prim.etc._uptonof"                     => new org.nlogo.core.prim.etc._uptonof
      case "org.nlogo.core.prim.etc._userdirectory"               => new org.nlogo.core.prim.etc._userdirectory
      case "org.nlogo.core.prim.etc._userfile"                    => new org.nlogo.core.prim.etc._userfile
      case "org.nlogo.core.prim.etc._userinput"                   => new org.nlogo.core.prim.etc._userinput
      case "org.nlogo.core.prim.etc._usernewfile"                 => new org.nlogo.core.prim.etc._usernewfile
      case "org.nlogo.core.prim.etc._useroneof"                   => new org.nlogo.core.prim.etc._useroneof
      case "org.nlogo.core.prim.etc._useryesorno"                 => new org.nlogo.core.prim.etc._useryesorno
      case "org.nlogo.core.prim.etc._variance"                    => new org.nlogo.core.prim.etc._variance
      case "org.nlogo.core.prim.etc._withmax"                     => new org.nlogo.core.prim.etc._withmax
      case "org.nlogo.core.prim.etc._withmin"                     => new org.nlogo.core.prim.etc._withmin
      case "org.nlogo.core.prim.etc._worldheight"                 => new org.nlogo.core.prim.etc._worldheight
      case "org.nlogo.core.prim.etc._worldwidth"                  => new org.nlogo.core.prim.etc._worldwidth
      case "org.nlogo.core.prim.etc._wrapcolor"                   => new org.nlogo.core.prim.etc._wrapcolor
      case "org.nlogo.core.prim.etc._xor"                         => new org.nlogo.core.prim.etc._xor
      case "org.nlogo.core.prim._greaterthan"                     => new org.nlogo.core.prim._greaterthan
      case "org.nlogo.core.prim._homedirectory"                   => new org.nlogo.core.prim._homedirectory
      case "org.nlogo.core.prim.hubnet._hubnetclientslist"        => new org.nlogo.core.prim.hubnet._hubnetclientslist
      case "org.nlogo.core.prim.hubnet._hubnetentermessage"       => new org.nlogo.core.prim.hubnet._hubnetentermessage
      case "org.nlogo.core.prim.hubnet._hubnetexitmessage"        => new org.nlogo.core.prim.hubnet._hubnetexitmessage
      case "org.nlogo.core.prim.hubnet._hubnetmessage"            => new org.nlogo.core.prim.hubnet._hubnetmessage
      case "org.nlogo.core.prim.hubnet._hubnetmessagesource"      => new org.nlogo.core.prim.hubnet._hubnetmessagesource
      case "org.nlogo.core.prim.hubnet._hubnetmessagetag"         => new org.nlogo.core.prim.hubnet._hubnetmessagetag
      case "org.nlogo.core.prim.hubnet._hubnetmessagewaiting"     => new org.nlogo.core.prim.hubnet._hubnetmessagewaiting
      case "org.nlogo.core.prim._inradius"                        => new org.nlogo.core.prim._inradius
      case "org.nlogo.core.prim._lessthan"                        => new org.nlogo.core.prim._lessthan
      case "org.nlogo.core.prim._list"                            => new org.nlogo.core.prim._list
      case "org.nlogo.core.prim._minus"                           => new org.nlogo.core.prim._minus
      case "org.nlogo.core.prim._neighbors4"                      => new org.nlogo.core.prim._neighbors4
      case "org.nlogo.core.prim._neighbors"                       => new org.nlogo.core.prim._neighbors
      case "org.nlogo.core.prim._notequal"                        => new org.nlogo.core.prim._notequal
      case "org.nlogo.core.prim._not"                             => new org.nlogo.core.prim._not
      case "org.nlogo.core.prim._of"                              => new org.nlogo.core.prim._of
      case "org.nlogo.core.prim._oneof"                           => new org.nlogo.core.prim._oneof
      case "org.nlogo.core.prim._or"                              => new org.nlogo.core.prim._or
      case "org.nlogo.core.prim._other"                           => new org.nlogo.core.prim._other
      case "org.nlogo.core.prim._patchat"                         => new org.nlogo.core.prim._patchat
      case "org.nlogo.core.prim._patches"                         => new org.nlogo.core.prim._patches
      case "org.nlogo.core.prim._random"                          => new org.nlogo.core.prim._random
      case "org.nlogo.core.prim._sentence"                        => new org.nlogo.core.prim._sentence
      case "org.nlogo.core.prim._sum"                             => new org.nlogo.core.prim._sum
      case "org.nlogo.core.prim._turtle"                          => new org.nlogo.core.prim._turtle
      case "org.nlogo.core.prim._turtles"                         => new org.nlogo.core.prim._turtles
      case "org.nlogo.core.prim._turtleson"                       => new org.nlogo.core.prim._turtleson
      case "org.nlogo.core.prim._whoarenot"                       => new org.nlogo.core.prim._whoarenot
      case "org.nlogo.core.prim._with"                            => new org.nlogo.core.prim._with
      case "org.nlogo.core.prim._word"                            => new org.nlogo.core.prim._word
    }: PartialFunction[String, Reporter]).lift

  def breeded(breedName: String): (String) => Option[Instruction] =
    ({
      case "_createorderedturtles"  => new org.nlogo.core.prim._createorderedturtles(breedName)
      case "_createturtles"         => new org.nlogo.core.prim._createturtles(breedName)
      case "_hatch"                 => new org.nlogo.core.prim._hatch(breedName)
      case "_sprout"                => new org.nlogo.core.prim._sprout(breedName)
      case "_breed"                 => new org.nlogo.core.prim._breed(breedName)
      case "etc._breedat"           => new org.nlogo.core.prim.etc._breedat(breedName)
      case "etc._breedhere"         => new org.nlogo.core.prim.etc._breedhere(breedName)
      case "_breedon"               => new org.nlogo.core.prim._breedon(breedName)
      case "etc._breedsingular"     => new org.nlogo.core.prim.etc._breedsingular(breedName)
      case "etc._createlinksfrom"   => new org.nlogo.core.prim.etc._createlinksfrom(breedName)
      case "etc._createlinkfrom"    => new org.nlogo.core.prim.etc._createlinkfrom(breedName)
      case "etc._createlinksto"     => new org.nlogo.core.prim.etc._createlinksto(breedName)
      case "etc._createlinkswith"   => new org.nlogo.core.prim.etc._createlinkswith(breedName)
      case "etc._createlinkto"      => new org.nlogo.core.prim.etc._createlinkto(breedName)
      case "etc._createlinkwith"    => new org.nlogo.core.prim.etc._createlinkwith(breedName)
      case "etc._inlinkfrom"        => new org.nlogo.core.prim.etc._inlinkfrom(breedName)
      case "etc._inlinkneighbor"    => new org.nlogo.core.prim.etc._inlinkneighbor(breedName)
      case "etc._inlinkneighbors"   => new org.nlogo.core.prim.etc._inlinkneighbors(breedName)
      case "etc._isbreed"           => new org.nlogo.core.prim.etc._isbreed(breedName)
      case "etc._linkbreed"         => new org.nlogo.core.prim.etc._linkbreed(breedName)
      case "etc._linkbreedsingular" => new org.nlogo.core.prim.etc._linkbreedsingular(breedName)
      case "etc._linkneighbor"      => new org.nlogo.core.prim.etc._linkneighbor(breedName)
      case "etc._linkneighbors"     => new org.nlogo.core.prim.etc._linkneighbors(breedName)
      case "etc._linkwith"          => new org.nlogo.core.prim.etc._linkwith(breedName)
      case "etc._myinlinks"         => new org.nlogo.core.prim.etc._myinlinks(breedName)
      case "etc._mylinks"           => new org.nlogo.core.prim.etc._mylinks(breedName)
      case "etc._myoutlinks"        => new org.nlogo.core.prim.etc._myoutlinks(breedName)
      case "etc._outlinkneighbor"   => new org.nlogo.core.prim.etc._outlinkneighbor(breedName)
      case "etc._outlinkneighbors"  => new org.nlogo.core.prim.etc._outlinkneighbors(breedName)
      case "etc._outlinkto"         => new org.nlogo.core.prim.etc._outlinkto(breedName)
    }: PartialFunction[String, Instruction]).lift

}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

private val file = """C __apply etc._apply
C __bench etc._bench
C __clear-all-and-reset-ticks etc._clearallandresetticks
C __done _done
C __experimentstepend etc._experimentstepend
C __export-drawing etc._exportdrawing
C __foreverbuttonend etc._foreverbuttonend
C __ignore etc._ignore
C __let _let
C __linkcode etc._linkcode
C __mkdir etc._mkdir
C __observercode etc._observercode
C __patchcode etc._patchcode
C __plot-pen-hide etc._plotpenhide
C __plot-pen-show etc._plotpenshow
C __pwd etc._pwd
C __reload-extensions etc._reloadextensions
C __set-line-thickness etc._setlinethickness
C __stderr etc._stderr
C __stdout etc._stdout
C __thunk-did-finish etc._thunkdidfinish
C __turtlecode etc._turtlecode
C ask _ask
C ask-concurrent _askconcurrent
C auto-plot-off etc._autoplotoff
C auto-plot-on etc._autoploton
C auto-plot-x-off etc._autoplotxoff
C auto-plot-x-on etc._autoplotxon
C auto-plot-y-off etc._autoplotyoff
C auto-plot-y-on etc._autoplotyon
C back _bk
C beep etc._beep
C bk _bk
C ca etc._clearall
C carefully _carefully
C cd etc._cleardrawing
C clear-all etc._clearall
C clear-all-plots etc._clearallplots
C clear-drawing etc._cleardrawing
C clear-globals etc._clearglobals
C clear-links etc._clearlinks
C clear-output etc._clearoutput
C clear-patches etc._clearpatches
C clear-plot etc._clearplot
C clear-ticks etc._clearticks
C clear-turtles etc._clearturtles
C cp etc._clearpatches
C create-link-from etc._createlinkfrom
C create-link-to etc._createlinkto
C create-link-with etc._createlinkwith
C create-links-from etc._createlinksfrom
C create-links-to etc._createlinksto
C create-links-with etc._createlinkswith
C create-ordered-turtles _createorderedturtles
C create-temporary-plot-pen etc._createtemporaryplotpen
C create-turtles _createturtles
C cro _createorderedturtles
C crt _createturtles
C ct etc._clearturtles
C die etc._die
C diffuse etc._diffuse
C diffuse4 etc._diffuse4
C display etc._display
C downhill etc._downhill
C downhill4 etc._downhill4
C error etc._error
C every etc._every
C export-all-plots etc._exportplots
C export-interface etc._exportinterface
C export-output etc._exportoutput
C export-plot etc._exportplot
C export-view etc._exportview
C export-world etc._exportworld
C face etc._face
C facexy etc._facexy
C fd _fd
C file-close etc._fileclose
C file-close-all etc._filecloseall
C file-delete etc._filedelete
C file-flush etc._fileflush
C file-open etc._fileopen
C file-print etc._fileprint
C file-show etc._fileshow
C file-type etc._filetype
C file-write etc._filewrite
C follow etc._follow
C follow-me etc._followme
C foreach etc._foreach
C forward _fd
C hatch _hatch
C hide-link etc._hidelink
C hide-turtle etc._hideturtle
C histogram etc._histogram
C home etc._home
C ht etc._hideturtle
C hubnet-broadcast hubnet._hubnetbroadcast
C hubnet-broadcast-clear-output hubnet._hubnetbroadcastclearoutput
C hubnet-broadcast-message hubnet._hubnetbroadcastmessage
C hubnet-clear-override hubnet._hubnetclearoverride
C hubnet-clear-overrides hubnet._hubnetclearoverrides
C hubnet-fetch-message hubnet._hubnetfetchmessage
C hubnet-kick-all-clients hubnet._hubnetkickallclients
C hubnet-kick-client hubnet._hubnetkickclient
C hubnet-reset hubnet._hubnetreset
C hubnet-reset-perspective hubnet._hubnetresetperspective
C hubnet-send hubnet._hubnetsend
C hubnet-send-clear-output hubnet._hubnetsendclearoutput
C hubnet-send-follow hubnet._hubnetsendfollow
C hubnet-send-message hubnet._hubnetsendmessage
C hubnet-send-override hubnet._hubnetsendoverride
C hubnet-send-watch hubnet._hubnetsendwatch
C if etc._if
C if-else etc._ifelse
C ifelse etc._ifelse
C import-drawing etc._importdrawing
C import-pcolors etc._importpatchcolors
C import-pcolors-rgb etc._importpcolorsrgb
C import-world etc._importworld
C inspect etc._inspect
C jump _jump
C layout-circle etc._layoutcircle
C layout-radial etc._layoutradial
C layout-spring etc._layoutspring
C layout-tutte etc._layouttutte
C left etc._left
C let _let
C loop etc._loop
C lt etc._left
C move-to etc._moveto
C no-display etc._nodisplay
C output-print etc._outputprint
C output-show etc._outputshow
C output-type etc._outputtype
C output-write etc._outputwrite
C pd etc._pendown
C pe etc._penerase
C pen-down etc._pendown
C pen-erase etc._penerase
C pen-up etc._penup
C pendown etc._pendown
C penup etc._penup
C plot etc._plot
C plot-pen-down etc._plotpendown
C plot-pen-reset etc._plotpenreset
C plot-pen-up etc._plotpenup
C plotxy etc._plotxy
C print etc._print
C pu etc._penup
C random-seed etc._randomseed
C repeat _repeat
C report _report
C reset-perspective etc._resetperspective
C reset-ticks etc._resetticks
C reset-timer etc._resettimer
C resize-world etc._resizeworld
C ride etc._ride
C ride-me etc._rideme
C right etc._right
C rp etc._resetperspective
C rt etc._right
C run _run
C set _set
C set-current-directory etc._setcurdir
C set-current-plot etc._setcurrentplot
C set-current-plot-pen etc._setcurrentplotpen
C set-default-shape etc._setdefaultshape
C set-histogram-num-bars etc._sethistogramnumbars
C set-patch-size etc._setpatchsize
C set-plot-pen-color etc._setplotpencolor
C set-plot-pen-interval etc._setplotpeninterval
C set-plot-pen-mode etc._setplotpenmode
C set-plot-x-range etc._setplotxrange
C set-plot-y-range etc._setplotyrange
C set-topology etc._settopology
C setup-plots etc._setupplots
C setxy etc._setxy
C show etc._show
C show-link etc._showlink
C show-turtle etc._showturtle
C sprout _sprout
C st etc._showturtle
C stamp etc._stamp
C stamp-erase etc._stamperase
C stop _stop
C stop-inspecting etc._stopinspecting
C stop-inspecting-dead-agents etc._stopinspectingdeadagents
C tick etc._tick
C tick-advance etc._tickadvance
C tie etc._tie
C type etc._type
C untie etc._untie
C update-plots etc._updateplots
C uphill etc._uphill
C uphill4 etc._uphill4
C user-message etc._usermessage
C wait etc._wait
C watch etc._watch
C watch-me etc._watchme
C while etc._while
C with-local-randomness etc._withlocalrandomness
C without-interruption etc._withoutinterruption
C write etc._write
R != _notequal
R * etc._mult
R + etc._plus
R - _minus
R / etc._div
R < _lessthan
R <= etc._lessorequal
R = _equal
R > _greaterthan
R >= etc._greaterorequal
R ^ etc._pow
R __apply-result etc._applyresult
R __boom etc._boom
R __block etc._block
R __check-syntax etc._checksyntax
R __checksum etc._checksum
R __dump etc._dump
R __dump-extension-prims etc._dumpextensionprims
R __dump-extensions etc._dumpextensions
R __dump1 etc._dump1
R __nano-time etc._nanotime
R __processors etc._processors
R __random-state etc._randomstate
R __reference etc._reference
R __stack-trace etc._stacktrace
R __symbol etc._symbolstring
R __to-string etc._tostring
R abs etc._abs
R acos etc._acos
R all? etc._all
R and _and
R any? _any
R approximate-hsb etc._approximatehsb
R approximate-rgb etc._approximatergb
R asin etc._asin
R at-points etc._atpoints
R atan etc._atan
R autoplot? etc._autoplot
R autoplotx? etc._autoplotx
R autoploty? etc._autoploty
R base-colors etc._basecolors
R behaviorspace-experiment-name etc._behaviorspaceexperimentname
R behaviorspace-run-number etc._behaviorspacerunnumber
R bf etc._butfirst
R bl etc._butlast
R both-ends etc._bothends
R but-first etc._butfirst
R but-last etc._butlast
R butfirst etc._butfirst
R butlast etc._butlast
R can-move? etc._canmove
R ceiling etc._ceil
R cos etc._cos
R count _count
R date-and-time etc._dateandtime
R distance etc._distance
R distancexy etc._distancexy
R dx etc._dx
R dy etc._dy
R empty? etc._empty
R error-message _errormessage
R exp etc._exp
R extract-hsb etc._extracthsb
R extract-rgb etc._extractrgb
R file-at-end? etc._fileatend
R file-exists? etc._fileexists
R file-read etc._fileread
R file-read-characters etc._filereadchars
R file-read-line etc._filereadline
R filter etc._filter
R first etc._first
R floor etc._floor
R fput etc._fput
R home-directory _homedirectory
R hsb etc._hsb
R hubnet-clients-list hubnet._hubnetclientslist
R hubnet-enter-message? hubnet._hubnetentermessage
R hubnet-exit-message? hubnet._hubnetexitmessage
R hubnet-message hubnet._hubnetmessage
R hubnet-message-source hubnet._hubnetmessagesource
R hubnet-message-tag hubnet._hubnetmessagetag
R hubnet-message-waiting? hubnet._hubnetmessagewaiting
R ifelse-value etc._ifelsevalue
R in-cone etc._incone
R in-link-from etc._inlinkfrom
R in-link-neighbor? etc._inlinkneighbor
R in-link-neighbors etc._inlinkneighbors
R in-radius _inradius
R insert-item etc._insertitem
R int etc._int
R is-agent? etc._isagent
R is-agentset? etc._isagentset
R is-anonymous-command? etc._isanonymouscommand
R is-anonymous-reporter? etc._isanonymousreporter
R is-boolean? etc._isboolean
R is-directed-link? etc._isdirectedlink
R is-link-set? etc._islinkset
R is-link? etc._islink
R is-list? etc._islist
R is-number? etc._isnumber
R is-patch-set? etc._ispatchset
R is-patch? etc._ispatch
R is-string? etc._isstring
R is-turtle-set? etc._isturtleset
R is-turtle? etc._isturtle
R is-undirected-link? etc._isundirectedlink
R item etc._item
R last etc._last
R length etc._length
R link etc._link
R link-heading etc._linkheading
R link-length etc._linklength
R link-neighbor? etc._linkneighbor
R link-neighbors etc._linkneighbors
R link-set etc._linkset
R link-shapes etc._linkshapes
R link-with etc._linkwith
R links etc._links
R list _list
R ln etc._ln
R log etc._log
R lput etc._lput
R map etc._map
R max etc._max
R max-n-of etc._maxnof
R max-one-of etc._maxoneof
R max-pxcor etc._maxpxcor
R max-pycor etc._maxpycor
R mean etc._mean
R median etc._median
R member? etc._member
R min etc._min
R min-n-of etc._minnof
R min-one-of etc._minoneof
R min-pxcor etc._minpxcor
R min-pycor etc._minpycor
R mod etc._mod
R modes etc._modes
R mouse-down? etc._mousedown
R mouse-inside? etc._mouseinside
R mouse-xcor etc._mousexcor
R mouse-ycor etc._mouseycor
R my-in-links etc._myinlinks
R my-links etc._mylinks
R my-out-links etc._myoutlinks
R myself etc._myself
R n-of etc._nof
R n-values etc._nvalues
R neighbors _neighbors
R neighbors4 _neighbors4
R netlogo-applet? etc._netlogoapplet
R netlogo-version etc._netlogoversion
R netlogo-web? etc._netlogoweb
R new-seed etc._newseed
R no-links etc._nolinks
R no-patches etc._nopatches
R no-turtles etc._noturtles
R not _not
R of _of
R one-of _oneof
R or _or
R other _other
R other-end etc._otherend
R out-link-neighbor? etc._outlinkneighbor
R out-link-neighbors etc._outlinkneighbors
R out-link-to etc._outlinkto
R patch etc._patch
R patch-ahead etc._patchahead
R patch-at _patchat
R patch-at-heading-and-distance etc._patchatheadinganddistance
R patch-here etc._patchhere
R patch-left-and-ahead etc._patchleftandahead
R patch-right-and-ahead etc._patchrightandahead
R patch-set etc._patchset
R patch-size etc._patchsize
R patches _patches
R plot-name etc._plotname
R plot-pen-exists? etc._plotpenexists
R plot-x-max etc._plotxmax
R plot-x-min etc._plotxmin
R plot-y-max etc._plotymax
R plot-y-min etc._plotymin
R position etc._position
R precision etc._precision
R random _random
R random-exponential etc._randomexponential
R random-float etc._randomfloat
R random-gamma etc._randomgamma
R random-normal etc._randomnormal
R random-poisson etc._randompoisson
R random-pxcor etc._randompxcor
R random-pycor etc._randompycor
R random-xcor etc._randomxcor
R random-ycor etc._randomycor
R range etc._range
R read-from-string etc._readfromstring
R reduce etc._reduce
R remainder etc._remainder
R remove etc._remove
R remove-duplicates etc._removeduplicates
R remove-item etc._removeitem
R replace-item etc._replaceitem
R reverse etc._reverse
R rgb etc._rgb
R round etc._round
R run-result etc._runresult
R runresult etc._runresult
R scale-color etc._scalecolor
R se _sentence
R self etc._self
R sentence _sentence
R shade-of? etc._shadeof
R shapes etc._shapes
R shuffle etc._shuffle
R sin etc._sin
R sort etc._sort
R sort-by etc._sortby
R sort-on etc._sorton
R sqrt etc._sqrt
R standard-deviation etc._standarddeviation
R subject etc._subject
R sublist etc._sublist
R substring etc._substring
R subtract-headings etc._subtractheadings
R sum _sum
R tan etc._tan
R ticks etc._ticks
R timer etc._timer
R towards etc._towards
R towardsxy etc._towardsxy
R turtle _turtle
R turtle-set etc._turtleset
R turtles _turtles
R turtles-at etc._turtlesat
R turtles-here etc._turtleshere
R turtles-on _turtleson
R up-to-n-of etc._uptonof
R user-directory etc._userdirectory
R user-file etc._userfile
R user-input etc._userinput
R user-new-file etc._usernewfile
R user-one-of etc._useroneof
R user-yes-or-no? etc._useryesorno
R variance etc._variance
R who-are-not _whoarenot
R with _with
R with-max etc._withmax
R with-min etc._withmin
R word _word
R world-height etc._worldheight
R world-width etc._worldwidth
R wrap-color etc._wrapcolor
R xor etc._xor"""

object TokenMapping1 {
  val reporters: Map[String, String] = file.split("\n").filter(_.startsWith("R")).map((line) => line.split(" ").tail).map { case Array(x, y) => x.toUpperCase -> s"org.nlogo.core.prim.${y}" }.toMap
  val  commands: Map[String, String] = file.split("\n").filter(_.startsWith("C")).map((line) => line.split(" ").tail).map { case Array(x, y) => x.toUpperCase -> s"org.nlogo.core.prim.${y}" }.toMap
}

object TokenMapping2 {

  def command(name: String): Command = {
    name match {
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
    }

  }

  def reporter(name: String): Reporter = {
    name match {
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
    }
  }

  def breeded(primName: String, breedName: String): Option[Instruction] = {
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

    }: PartialFunction[String, Instruction]).lift(primName)
  }

}

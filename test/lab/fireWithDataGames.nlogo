globals [
  initial-trees   ;; how many trees (green patches) we started with
  burned-trees    ;; how many have burned so far
]

breed [fires fire]    ;; bright red turtles -- the leading edge of the fire
breed [embers ember]  ;; turtles gradually fading from red to near black

to setup
  clear-all
  ;; make some green trees
  ask patches with [(random-float 100) < density]
    [ set pcolor green ]
  ;; make a column of burning trees
  ask patches with [pxcor = min-pxcor]
    [ ignite ]
  ;; set tree counts
  set initial-trees count patches with [pcolor = green]
  set burned-trees 0
  reset-ticks
end

to go
  if not any? turtles  ;; either fires or embers
    [ stop ]
  ask fires
    [ ask neighbors4 with [pcolor = green]
        [ ignite ]
      set breed embers ]
  fade-embers
  tick
end

;; creates the fire turtles
to ignite  ;; patch procedure
  sprout-fires 1
    [ set color red ]
  set pcolor black
  set burned-trees burned-trees + 1
end

;; achieve fading color effect for the fire as it burns
to fade-embers
  ask embers
    [ set color color - 0.3  ;; make red darker
      if color < red - 3.5     ;; are we almost at black?
        [ set pcolor color
          die ] ]
end

to-report percent-burned
  report (burned-trees / initial-trees) * 100
end
@#$#@#$#@
GRAPHICS-WINDOW
200
10
712
543
125
125
2.0
1
10
1
1
1
0
0
0
1
-125
125
-125
125
1
1
1
ticks
30.0

MONITOR
43
131
158
176
percent burned
percent-burned
1
1
11

SLIDER
5
38
190
71
density
density
0.0
99.0
45
1.0
1
%
HORIZONTAL

BUTTON
106
79
175
115
go
go
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

BUTTON
26
79
96
115
setup
setup
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
NetLogo 5.0.2
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
<experiments>
  <experiment name="experiment" repetitions="1" runMetricsEveryStep="true">
    <setup>random-seed 0 setup</setup>
    <go>go</go>
    <metric>percent-burned</metric>
    <steppedValueSet variable="density" first="45" step="1" last="65"/>
  </experiment>
</experiments>
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
0
@#$#@#$#@

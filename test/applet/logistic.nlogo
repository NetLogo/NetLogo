patches-own [own-weight
             neighbours-weight
             neighbours-state
             own-state
             ]

globals [average-state]

to setup

ask patches [set own-state random-float 1.0
             set pcolor 10 * own-state
              ]
reset-ticks           
end

to go

ask patches [ifelse (neighbours?)
               [set own-weight 0.5
                set neighbours-weight 0.5 / 4]
               [set own-weight 1
                set neighbours-weight 0]
             set neighbours-state (sum [own-state] of neighbors4)
             set own-state (A * (own-weight * own-state + neighbours-weight * neighbours-state) * (1 -
             own-weight * own-state - neighbours-weight * neighbours-state))]
ask patches [set pcolor 10 * own-state
             ]
tick
do-plot
end


to do-plot
set-current-plot "Patch(1,5)"
set-current-plot-pen "own-state"
plot [own-state] of (patch 1 5)
end
@#$#@#$#@
GRAPHICS-WINDOW
321
10
634
344
50
50
3.0
1
10
1
1
1
0
1
1
1
-50
50
-50
50
1
1
1
ticks
30.0

BUTTON
650
48
717
81
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

SLIDER
646
151
753
184
A
A
0
4
4
0.1
1
NIL
HORIZONTAL

BUTTON
655
90
717
125
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

PLOT
15
51
283
236
Patch(1,5)
time
own-state
0.0
100.0
0.0
1.0
true
false
"" ""
PENS
"own-state" 1.0 0 -13345367 true "" ""

SWITCH
643
207
778
240
neighbours?
neighbours?
0
1
-1000

@#$#@#$#@
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

@#$#@#$#@
NetLogo 5.0beta3
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
default
0.0
-0.2 0 1.0 0.0
0.0 1 1.0 0.0
0.2 0 1.0 0.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

@#$#@#$#@
0
@#$#@#$#@

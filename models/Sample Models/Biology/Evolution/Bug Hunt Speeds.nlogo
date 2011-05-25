breed [predators predator]
breed [bugs bug]
bugs-own [
  speed    ;; either 1, 2, 3, 4, 5, or 6
]

globals [
    total-speed-6-caught         ;; keeps track of the number of bugs caught with speed of 6
    total-speed-5-caught         ;; keeps track of the number of bugs caught with speed of 5
    total-speed-4-caught         ;; keeps track of the number of bugs caught with speed of 4
    total-speed-3-caught         ;; keeps track of the number of bugs caught with speed of 3
    total-speed-2-caught         ;; keeps track of the number of bugs caught with speed of 2
    total-speed-1-caught         ;; keeps track of the number of bugs caught with speed of 1
]

to setup
  clear-all
  set   total-speed-6-caught 0
  set   total-speed-5-caught 0
  set   total-speed-4-caught 0
  set   total-speed-3-caught 0
  set   total-speed-2-caught 0
  set   total-speed-1-caught 0
  set-default-shape bugs "bug"
  set-default-shape predators "bird"
  ask patches [ set pcolor white ]   ;; white background
  foreach [1 2 3 4 5 6] [
    create-bugs initial-bugs-each-speed [ set speed ? ]
  ]
  ask bugs [
    setxy random-xcor random-ycor
    set-color
  ]
  ;; the predator breed contains one turtle that is used to represent
  ;; a predator of the bugs (a bird)
  create-predators 1 [
    set shape "bird"
    set color black
    set size 1.5
    set heading 315
    hide-turtle
  ]
  reset-ticks
end

to go
  ;; use EVERY to limit the overall speed of the model
  every 0.03 [
    check-caught
    move-predator
    ;; recolor the bugs in case the user changed SPEED-COLOR-MAP
    ask bugs [ set-color move-bugs]
    ;; advance the clock without plotting
    tick-advance 1
    ;; plotting takes time, so only plot every 10 ticks
    if ticks mod 10 = 0 [ update-plots ]
  ]
end

;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures
;;;;;;;;;;;;;;;;;;;;;


to move-bugs
    let candidate-predator nobody
    let target-heading 0

    ask bugs [
       if wiggle? [right (random-float 5 - random-float 5)]
       fd speed * 0.001

       ifelse flee? [
         ifelse any? predators in-cone 2 120 [
           set candidate-predator one-of predators in-cone 2  120
           set target-heading 180 + towards candidate-predator

           set heading target-heading
           set label "!"
         ]
         [set label ""]
       ]
       [set label ""]
     ]
end


to move-predator
  ask predators [
    setxy mouse-xcor mouse-ycor
    ;; only show the predator if the mouse pointer is
    ;; actually inside the view
    set hidden? not mouse-inside?
  ]
end

to check-caught
  if not mouse-down? or not mouse-inside? [ stop ]
  let prey [bugs in-radius (size / 2)] of one-of predators
  ;; no prey here? oh well
  if not any? prey [ stop ]
  ;; eat only one of the bugs at the mouse location
  ask one-of prey [
    if speed = 6 [ set total-speed-6-caught total-speed-6-caught + 1 ]
    if speed = 5 [ set total-speed-5-caught total-speed-5-caught + 1 ]
    if speed = 4 [ set total-speed-4-caught total-speed-4-caught + 1 ]
    if speed = 3 [ set total-speed-3-caught total-speed-3-caught + 1 ]
    if speed = 2 [ set total-speed-2-caught total-speed-2-caught + 1 ]
    if speed = 1 [ set total-speed-1-caught total-speed-1-caught + 1 ]
    die
  ]
  ;; replace the eaten bug with a random offspring from the remaining population
  ask one-of bugs [ hatch 1 [ rt random 360 ] ]
end

to set-color  ;; turtle procedure
  if speed-color-map = "all green"
    [ set color green ]
  if speed-color-map = "violet shades"
    [ set color violet - 4 + speed ]
  if speed-color-map = "rainbow"
    [ set color item (speed - 1) [violet blue green brown orange red] ]
end
@#$#@#$#@
GRAPHICS-WINDOW
271
10
741
501
11
11
20.0
1
10
1
1
1
0
1
1
1
-11
11
-11
11
1
1
1
ticks
30.0

MONITOR
57
95
117
140
caught
total-speed-1-caught +\ntotal-speed-2-caught +\ntotal-speed-3-caught +\ntotal-speed-4-caught +\ntotal-speed-5-caught +\ntotal-speed-6-caught
0
1
11

BUTTON
5
52
66
85
NIL
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

BUTTON
68
52
129
85
NIL
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
4
259
269
379
Current Bug Population
speeds
frequency
0.0
8.0
0.0
50.0
true
false
"" ";; the HISTOGRAM primitive can't make a multi-colored histogram,\n;; so instead we plot each bar individually\nclear-plot\nforeach [1 2 3 4 5 6] [\n  set-current-plot-pen word \"pen\" ?\n  plotxy ? count bugs with [speed = ?]\n]\n"
PENS
"pen1" 1.0 1 -8630108 true "" ""
"pen2" 1.0 1 -13345367 true "" ""
"pen3" 1.0 1 -10899396 true "" ""
"pen4" 1.0 1 -3355648 true "" ""
"pen5" 1.0 1 -955883 true "" ""
"pen6" 1.0 1 -2674135 true "" ""

CHOOSER
136
87
264
132
speed-color-map
speed-color-map
"all green" "rainbow" "violet shades"
1

PLOT
3
140
269
260
Avg. Bug Speed vs. Time
time
rate
0.0
1000.0
0.0
0.5
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "plotxy ticks mean [speed] of bugs"

MONITOR
6
95
56
140
bugs
(count bugs)
0
1
11

SLIDER
6
15
175
48
initial-bugs-each-speed
initial-bugs-each-speed
1
50
10
1
1
NIL
HORIZONTAL

PLOT
4
378
269
498
Bugs Caught
speeds
number
0.0
8.0
0.0
10.0
true
false
"" "clear-plot\n\n  plotxy 2 total-speed-2-caught\n\n  set-histogram-num-bars 8\n  set-current-plot-pen \"pen3\"\n  plotxy 3 total-speed-3-caught\n\n  set-histogram-num-bars 8\n  set-current-plot-pen \"pen4\"\n  plotxy 4 total-speed-3-caught\n\n  set-histogram-num-bars 8\n  set-current-plot-pen \"pen5\"\n  plotxy 5  total-speed-5-caught\n\n  set-histogram-num-bars 8\n  set-current-plot-pen \"pen6\"\n  plotxy 6  total-speed-6-caught\n"
PENS
"pen1" 1.0 1 -8630108 true "set-histogram-num-bars 8" "plotxy 1 total-speed-1-caught"
"pen2" 1.0 1 -13345367 true "set-histogram-num-bars 8" "plotxy 2 total-speed-2-caught"
"pen3" 1.0 1 -10899396 true "set-histogram-num-bars 8" "plotxy 3 total-speed-3-caught"
"pen4" 1.0 1 -4079321 true "set-histogram-num-bars 8" "plotxy 4 total-speed-4-caught"
"pen5" 1.0 1 -955883 true "set-histogram-num-bars 8" "plotxy 5 total-speed-5-caught"
"pen6" 1.0 1 -2674135 true "set-histogram-num-bars 8" "plotxy 6 total-speed-6-caught"

SWITCH
175
15
265
48
wiggle?
wiggle?
0
1
-1000

SWITCH
175
49
265
82
flee?
flee?
0
1
-1000

@#$#@#$#@
## WHAT IS IT?

This is a natural/artificial selection model that shows the result of two competing forces on natural selection of the speed of prey.  Which force dominates depends on the behavior of predators.

One force is that predators that chase prey, tend to catch slower moving prey more often, thereby selecting for prey that are faster over many generations of offspring.

Another force is that predators who wait for their prey without moving, tend to catch prey that are moving faster more often, thereby selecting for prey that are slower over many generations of offspring.

By also adjusting whether bugs try to avoid the predator and the predictability of their motion, a different one of these competing forces will tend to dominate the selective pressure on the population.

## HOW IT WORKS

You assume the role of a predator amongst a population of bugs.  To begin your pursuit of bugs as a predator, press SETUP to create a population of bugs, determined by six times the INITIAL-BUGS-EACH-SPEED slider.  These bugs that are created are randomly distributed around the world and assigned a speed.

When you press GO the bugs begin to move at their designated speeds.  As they move around, try to eat as many bugs as fast as you can by clicking on them.  Alternatively, you may hold the mouse button down and move the predator over the bugs.

The six different speeds that a bug might move at are distributed amongst six different sub-populations of the bugs.  These speeds are inherited.  With each bug you eat, a new bug is randomly chosen from the population to produce one offspring.  The offspring is an exact duplicate of the parent (in its speed and location).  The creation of new offspring keeps the overall population of the bugs constant.

Initially there are equal numbers of each sub-population of bug (e.g. ten bugs at each of the 6 speeds).  Over time, however, as you eat bugs, the distribution of the bugs will change as shown in the "Number of bugs" histogram.  In the histogram, you might see the distribution shift to the left (showing that more slow bugs are surviving) or to the right (showing that more fast bugs are surviving).  Sometimes one sub-population of a single speed of bug will be exterminated.  At this point, no other bugs of this speed can be created in the population.

## HOW TO USE IT

INITIAL-BUGS-EACH-SPEED is the number of bugs you start with in each of the six sub-populations.  The overall population of bugs is determined by multiplying this value by 6.

SPEED-COLOR-MAP settings help you apply or remove color visualization to the speed of the bugs:

- The "all green" setting does not show a different color for each bug based on its speed.  Keeping the color settings switched to something besides "all green" can tend to result in the predator (the user) unconsciously selecting bugs based on color instead of speed.

- The "rainbow" setting shows 6 distinct colors for the 6 different speeds a bug might have.  These color settings correspond to the plot pen colors in the graphs.

- The "purple shades" setting shows a gradient of dark purple to light purple for slow to fast bug speed.

NUMBER OF BUGS is a histogram showing the distribution of bugs at different speeds.

BUGS CAUGHT is a histogram showing the historical record of the distribution of bugs caught at different speeds.

WIGGLE?, when set to "on" adds a small amount of random twist in the motion of the bugs as they move forward each time step.

FLEE?, when set to "on" has bugs turn around (to face in the opposite direction) when they detect your mouse click (as a predator) in their detection cone (an arc of 120 degrees that has a range of 2 units).  Bugs can detect the predator only in this arc in front of them, and so will not react when caught from behind.

## THINGS TO NOTICE

The CURRENT BUGS histogram tends to shift right (increasing average speed) if you assume the role of chasing easy prey.

The CURRENT BUGS histogram tends to shift left (decreasing average speed) if you assume the role of waiting for prey come to you.  The same effect can also be achieved by moving the predator around the world randomly.

## THINGS TO TRY

Set the model up with INITIAL-BUGS-EACH-SPEED set to 1.  Slow the model down and watch where new bugs come from when you eat a bug.  You should see a new bug hatch from one of the five remaining and it should be moving at the same speed as its parent.

Wait in one location for the bugs to come to you by placing the predator in one location and holding down the mouse button.  All bugs that run into you will be eaten.

Chase bugs around trying to catch the bug nearest you at any one time by holding the mouse button down and moving the predator around the view after the nearest bug.

## EXTENDING THE MODEL

A HubNet version of the model with adjustable starting populations of bugs would help show what happens when two or more competitors assume similar vs. different hunting strategies on the same population at the same time.

## RELATED MODELS

Bug Hunt Camouflage

## CREDITS AND REFERENCES

Inspired by EvoDots software:  
http://faculty.washington.edu/~herronjc/SoftwareFolder/EvoDots.html
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

arrow
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

bird
true
0
Polygon -7500403 true true 151 170 136 170 123 229 143 244 156 244 179 229 166 170
Polygon -16777216 true false 152 154 137 154 125 213 140 229 159 229 179 214 167 154
Polygon -7500403 true true 151 140 136 140 126 202 139 214 159 214 176 200 166 140
Polygon -16777216 true false 151 125 134 124 128 188 140 198 161 197 174 188 166 125
Polygon -7500403 true true 152 86 227 72 286 97 272 101 294 117 276 118 287 131 270 131 278 141 264 138 267 145 228 150 153 147
Polygon -7500403 true true 160 74 159 61 149 54 130 53 139 62 133 81 127 113 129 149 134 177 150 206 168 179 172 147 169 111
Circle -16777216 true false 144 55 7
Polygon -16777216 true false 129 53 135 58 139 54
Polygon -7500403 true true 148 86 73 72 14 97 28 101 6 117 24 118 13 131 30 131 22 141 36 138 33 145 72 150 147 147

box
false
0
Polygon -7500403 true true 150 285 285 225 285 75 150 135
Polygon -7500403 true true 150 135 15 75 150 15 285 75
Polygon -7500403 true true 15 75 15 225 150 285 150 135
Line -16777216 false 150 285 150 135
Line -16777216 false 150 135 15 75
Line -16777216 false 150 135 285 75

bug
true
0
Circle -7500403 true true 96 182 108
Circle -7500403 true true 110 127 80
Circle -7500403 true true 110 75 80
Line -7500403 true 150 100 80 30
Line -7500403 true 150 100 220 30

butterfly
true
0
Polygon -7500403 true true 150 165 209 199 225 225 225 255 195 270 165 255 150 240
Polygon -7500403 true true 150 165 89 198 75 225 75 255 105 270 135 255 150 240
Polygon -7500403 true true 139 148 100 105 55 90 25 90 10 105 10 135 25 180 40 195 85 194 139 163
Polygon -7500403 true true 162 150 200 105 245 90 275 90 290 105 290 135 275 180 260 195 215 195 162 165
Polygon -16777216 true false 150 255 135 225 120 150 135 120 150 105 165 120 180 150 165 225
Circle -16777216 true false 135 90 30
Line -16777216 false 150 105 195 60
Line -16777216 false 150 105 105 60

car
false
0
Polygon -7500403 true true 300 180 279 164 261 144 240 135 226 132 213 106 203 84 185 63 159 50 135 50 75 60 0 150 0 165 0 225 300 225 300 180
Circle -16777216 true false 180 180 90
Circle -16777216 true false 30 180 90
Polygon -16777216 true false 162 80 132 78 134 135 209 135 194 105 189 96 180 89
Circle -7500403 true true 47 195 58
Circle -7500403 true true 195 195 58

circle
false
0
Circle -7500403 true true 0 0 300

circle 2
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240

cow
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

cylinder
false
0
Circle -7500403 true true 0 0 300

dot
false
0
Circle -7500403 true true 90 90 120

face happy
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 255 90 239 62 213 47 191 67 179 90 203 109 218 150 225 192 218 210 203 227 181 251 194 236 217 212 240

face neutral
false
0
Circle -7500403 true true 8 7 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Rectangle -16777216 true false 60 195 240 225

face sad
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 168 90 184 62 210 47 232 67 244 90 220 109 205 150 198 192 205 210 220 227 242 251 229 236 206 212 183

fish
false
0
Polygon -1 true false 44 131 21 87 15 86 0 120 15 150 0 180 13 214 20 212 45 166
Polygon -1 true false 135 195 119 235 95 218 76 210 46 204 60 165
Polygon -1 true false 75 45 83 77 71 103 86 114 166 78 135 60
Polygon -7500403 true true 30 136 151 77 226 81 280 119 292 146 292 160 287 170 270 195 195 210 151 212 30 166
Circle -16777216 true false 215 106 30

flag
false
0
Rectangle -7500403 true true 60 15 75 300
Polygon -7500403 true true 90 150 270 90 90 30
Line -7500403 true 75 135 90 135
Line -7500403 true 75 45 90 45

flower
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Circle -7500403 true true 85 132 38
Circle -7500403 true true 130 147 38
Circle -7500403 true true 192 85 38
Circle -7500403 true true 85 40 38
Circle -7500403 true true 177 40 38
Circle -7500403 true true 177 132 38
Circle -7500403 true true 70 85 38
Circle -7500403 true true 130 25 38
Circle -7500403 true true 96 51 108
Circle -16777216 true false 113 68 74
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240

house
false
0
Rectangle -7500403 true true 45 120 255 285
Rectangle -16777216 true false 120 210 180 285
Polygon -7500403 true true 15 120 150 15 285 120
Line -16777216 false 30 120 270 120

leaf
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195

line
true
0
Line -7500403 true 150 0 150 300

line half
true
0
Line -7500403 true 150 0 150 150

pentagon
false
0
Polygon -7500403 true true 150 15 15 120 60 285 240 285 285 120

person
false
0
Circle -7500403 true true 110 5 80
Polygon -7500403 true true 105 90 120 195 90 285 105 300 135 300 150 225 165 300 195 300 210 285 180 195 195 90
Rectangle -7500403 true true 127 79 172 94
Polygon -7500403 true true 195 90 240 150 225 180 165 105
Polygon -7500403 true true 105 90 60 150 75 180 135 105

plant
false
0
Rectangle -7500403 true true 135 90 165 300
Polygon -7500403 true true 135 255 90 210 45 195 75 255 135 285
Polygon -7500403 true true 165 255 210 210 255 195 225 255 165 285
Polygon -7500403 true true 135 180 90 135 45 120 75 180 135 210
Polygon -7500403 true true 165 180 165 210 225 180 255 120 210 135
Polygon -7500403 true true 135 105 90 60 45 45 75 105 135 135
Polygon -7500403 true true 165 105 165 135 225 105 255 45 210 60
Polygon -7500403 true true 135 90 120 45 150 15 180 45 165 90

square
false
0
Rectangle -7500403 true true 30 30 270 270

square 2
false
0
Rectangle -7500403 true true 30 30 270 270
Rectangle -16777216 true false 60 60 240 240

star
false
0
Polygon -7500403 true true 151 1 185 108 298 108 207 175 242 282 151 216 59 282 94 175 3 108 116 108

target
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60

tree
false
0
Circle -7500403 true true 118 3 94
Rectangle -6459832 true false 120 195 180 300
Circle -7500403 true true 65 21 108
Circle -7500403 true true 116 41 127
Circle -7500403 true true 45 90 120
Circle -7500403 true true 104 74 152

triangle
false
0
Polygon -7500403 true true 150 30 15 255 285 255

triangle 2
false
0
Polygon -7500403 true true 150 30 15 255 285 255
Polygon -16777216 true false 151 99 225 223 75 224

truck
false
0
Rectangle -7500403 true true 4 45 195 187
Polygon -7500403 true true 296 193 296 150 259 134 244 104 208 104 207 194
Rectangle -1 true false 195 60 195 105
Polygon -16777216 true false 238 112 252 141 219 141 218 112
Circle -16777216 true false 234 174 42
Rectangle -7500403 true true 181 185 214 194
Circle -16777216 true false 144 174 42
Circle -16777216 true false 24 174 42
Circle -7500403 false true 24 174 42
Circle -7500403 false true 144 174 42
Circle -7500403 false true 234 174 42

turtle
true
0
Polygon -10899396 true false 215 204 240 233 246 254 228 266 215 252 193 210
Polygon -10899396 true false 195 90 225 75 245 75 260 89 269 108 261 124 240 105 225 105 210 105
Polygon -10899396 true false 105 90 75 75 55 75 40 89 31 108 39 124 60 105 75 105 90 105
Polygon -10899396 true false 132 85 134 64 107 51 108 17 150 2 192 18 192 52 169 65 172 87
Polygon -10899396 true false 85 204 60 233 54 254 72 266 85 252 107 210
Polygon -7500403 true true 119 75 179 75 209 101 224 135 220 225 175 261 128 261 81 224 74 135 88 99

wheel
false
0
Circle -7500403 true true 3 3 294
Circle -16777216 true false 30 30 240
Line -7500403 true 150 285 150 15
Line -7500403 true 15 150 285 150
Circle -7500403 true true 120 120 60
Line -7500403 true 216 40 79 269
Line -7500403 true 40 84 269 221
Line -7500403 true 40 216 269 79
Line -7500403 true 84 40 221 269

x
false
0
Polygon -7500403 true true 270 75 225 30 30 225 75 270
Polygon -7500403 true true 30 75 75 30 270 225 225 270

@#$#@#$#@
NetLogo 5.0beta3
@#$#@#$#@
setup
repeat 17
[
  check-caught
  move-predator
  ask bugs [ set-color move-bugs]
  tick
  if ticks mod 10 = 0 [ update-plots ]
]
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
default
0.0
-0.2 0 0.0 1.0
0.0 1 1.0 0.0
0.2 0 0.0 1.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

@#$#@#$#@
0
@#$#@#$#@

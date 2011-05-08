;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; variable declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

globals
[
  num-turtles-right  ;; how many turned right this tick
  num-turtles-left   ;; how many turned left this tick
  sum-right-turns  ;; the sum of turtles that turned right for the entire run
  sum-left-turns   ;; the sum of turtles that turned left for the entire run
]

;;;;;;;;;;;;;;;;;;;;;;
;; setup procedures ;;
;;;;;;;;;;;;;;;;;;;;;;

;; sets up the patches and creates turtles
to setup
  clear-all
  ask patches [ set pcolor gray + 3 ]
  draw-line-up-middle
  setup-turtles
  init-vars
  reset-ticks
end

;; draw a line up the middle
to draw-line-up-middle
  crt 1
  [
    set heading 0
    set pen-size 3
    set pcolor 8
    pd
    fd world-height
    die
  ]
end

;; determines the number of turtles and their color
;; if turtle-trails? is on, the turtles' pens should be down,
;; otherwise they should be up
to setup-turtles
  ;; the origin is at the bottom of the view so there is no need
  ;; to relocate the turtles upon creation
  crt number-of-turtles
  [
    set color black
    set pen-size 3
    ifelse turtle-trails?
      [ pd ]  ;; have the turtles put down their pens
      [ pu ]  ;; have the turtles pick up their pens
    set heading 0
  ]
end

;; counts the number of right and left turns and paces of the turtles
to init-vars
  set sum-right-turns 0
  set sum-left-turns 0
end

;;;;;;;;;;;;;;;;;;;;;;;;
;; runtime procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;;

;; have the turtles randomly pick either right or left and
;; have them move in that direction
;; if one turtle cannot move, then stop
to go

  set num-turtles-right 0  ;; the number of turtles turning right currently
  set num-turtles-left 0   ;; the number of turtles turning left currently

  ;; if one turtles cannot move because it is at the edge of the world, then stop
  if any? turtles with
       [ patch-at 0 number-of-steps = nobody or
         patch-at (- number-of-steps) 0 = nobody or
         patch-at number-of-steps 0 = nobody ]
    [ stop ]

  ask turtles
  [
    ifelse ((random 2) = 0)
    [
      go-left
      set num-turtles-left (num-turtles-left + 1)
    ]
    [
      go-right
      set num-turtles-right (num-turtles-right + 1)
    ]

    ;; set the color of the turtles to give a rough idea of how many turtles
    ;; are at each location -- the lighter the color, the greater the number
    ;; of turtles
    set color scale-color blue (count turtles-here) (number-of-turtles / 5) 0
  ]
  ;; update the sums of right and left turns
  set sum-right-turns (sum-right-turns + num-turtles-right)
  set sum-left-turns (sum-left-turns + num-turtles-left)
  tick
end

;; turn left and go one-pace
to go-left  ;; turtle procedure
  lt 90
  one-pace
end

;; go forward number-of-steps, turn upwards and go forward number-of-steps
to one-pace  ;; turtle procedure
  fd number-of-steps
  set heading 0
  fd number-of-steps
end

;; turn right and go one-pace
to go-right  ;; turtle procedure
  rt 90
  one-pace
end
@#$#@#$#@
GRAPHICS-WINDOW
373
10
733
391
17
-1
10.0
1
10
1
1
1
0
0
0
1
-17
17
0
34
1
1
1
ticks
30

MONITOR
19
95
140
140
sum of left turns
sum-left-turns
3
1
11

MONITOR
19
149
145
194
sum of right turns
sum-right-turns
3
1
11

PLOT
9
205
364
391
Steps each direction
ticks
# of steps
0.0
40.0
0.0
200.0
true
true
"set-plot-y-range 0 (count turtles * .75)\nset-plot-x-range 0 (world-height)" ""
PENS
"turned right" 1.0 0 -2674135 true "" "plot num-turtles-right"
"turned left" 1.0 0 -13345367 true "" "plot num-turtles-left"

SWITCH
185
82
326
115
turtle-trails?
turtle-trails?
0
1
-1000

SLIDER
160
166
332
199
number-of-steps
number-of-steps
1
10
2
1
1
NIL
HORIZONTAL

SLIDER
160
126
332
159
number-of-turtles
number-of-turtles
1
500
250
1
1
NIL
HORIZONTAL

BUTTON
221
38
302
74
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

BUTTON
113
38
194
74
go once
go
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
18
38
90
74
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

@#$#@#$#@
## WHAT IS IT?

This is a model to simulate a random walk.  In this simulation, all turtles walk to the left and forward or they walk to the right and forward.  The turtles randomly choose between either direction each time they move.

The path traced out by the turtles moving in this pattern is called a lattice.

## HOW IT WORKS

As the simulation continues, one can expect the turtles to become more spread out.  Observe the kinds of patterns that develop as the turtles move.

Shading of turtles is representative of how many turtles share that location. Dark shades imply more turtles.  Light shades imply fewer turtles.

For purposes of this simulation, "forward" will mean moving toward the top of the view, "left" will mean moving toward the left edge of the view, and "right" will mean moving toward the right edge of the view.

## HOW TO USE IT

Use the NUMBER-OF-TURTLES slider to select how many turtles will participate in the random walk.

Use the NUMBER-OF-STEPS slider to decide how many steps a turtle will take on each turn.

How steps are implemented:  
- If NUMBER-OF-STEPS is set to 1 and a turtle is going left, it will go left one step and then go forward one step. (Imagine a turtle walking along the bottom and left edge of a 1 x 1 square.)  
- If NUMBER-OF-STEPS is set to 4 and a turtle is going left, it will go left four steps and then go forward four steps. (Imagine a turtle walking along the bottom and left edge of a 4 x 4 square.)  
- Each of the above movements would be considered a single "pace."

Use the TURTLE-TRAILS? switch to have the turtles put their pens down to trace their paths and show the part of the lattice they are covering.  This switch must be set before the SETUP button is pressed.

All sliders except NUMBER-OF-TURTLES may be changed during a simulation.

Press the SETUP when all of the above selections have been made. This will create the selected number of turtles at the bottom center of the world.

Press GO ONCE button to make the turtles move one pace.

Press the GO button to make the turtles move until one of the turtles cannot complete its number of steps.  When one turtle reaches this point, all the other turtles will stop even if they can complete the step.

To stop the simulation while it is running, press the GO button again.

The gray bar in the middle of the world is at xcor = 0.  This is where all the turtles start.

## THINGS TO TRY

Try to answer the questions below before running the simulations.

Record your predictions.  Compare your predicted results with the actual results.  
- What reasoning led you to correct predictions?  
- What assumptions that you made need to be revised?

Try different numbers of turtles while keeping all other slider values the same.

Try different numbers of steps while keeping all other slider values the same.

## THINGS TO NOTICE

Think about how you would define an "average" turtle and an "average" walk.

Where would you expect an average turtle to end up at the end of the simulation?  Why?

How many paces would you expect there to be in an average walk?  Why?

What kinds of calculations or measurements would you use in trying to answer these questions?

How do your answers to the above questions compare to the average of the x coordinates of all the turtles?

## EXTENDING THE MODEL

As the model stands, it plots two lines (right and left) over time. Another way to look at this simulation is to plot the distribution of turtles.  Create a histogram to show this type of data.

The turtles will stop if they come up to an obstacle (cannot move forward or to the right/left), give the turtles the ability to think ahead and choose a different step/direction.

Give the turtles the ability to walk backwards.

Create a three dimensional lattice.

## NETLOGO FEATURES

Since turtles in this model only move in the positive direction and they start at the bottom the origin is relocated to be at the bottom of the view also, so there are no patches with negative pycor.

## RELATED MODELS

Random Walk 360, Galton Box, Binomial Rabbits

## CREDITS AND REFERENCES
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
NetLogo 5.0beta1
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

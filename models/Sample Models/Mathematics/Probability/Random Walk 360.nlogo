turtles-own
[
  xc     ; unwrapped xcor
  yc     ; unwrapped ycor
  dist   ; distance from home using xc, yc
]

to setup
  clear-all
  ; if the user so chooses, draw rings around (0, 0) to give a sense of how far turtles have traveled
  ; the furthest in-view distance from home is world-width * sqrt (2) / 2
  if draw-rings? [
    ;; draw the rings by making ring shape turtles that stamp their shape
    ;; and then go away
    crt floor (world-width * sqrt 2 / (ring-radius * 2)) [
      set color gray - 1
      set shape "ring"
      set size ring-radius * 2 * (who + 1)
      stamp
      die
    ]
    ;; draw X and Y axes
    crt 1 [
      set heading 0
      set color gray - 1
      set shape "line"
      set size world-height
      stamp
      rt 90
      set size world-width
      stamp
      die
    ]
  ]
  crt num-turtles
  reset-ticks
end

to go
  ask turtles
  [
    ; head in a random direction
    rt random-float 360
    set xc xc + (step-size * dx)
    set yc yc + (step-size * dy)
    ; hide turtles who have moved out of the world
    ifelse patch-at (xc - xcor) (yc - ycor) = nobody
      [ ht ]
      [ st
        set xcor xc
        set ycor yc ]
    set dist sqrt (xc * xc + yc * yc)
  ]
  tick
end
@#$#@#$#@
GRAPHICS-WINDOW
290
10
666
407
30
30
6.0
1
10
1
1
1
0
0
0
1
-30
30
-30
30
1
1
1
ticks
10

BUTTON
190
121
250
154
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
107
121
188
154
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
45
121
105
154
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

SLIDER
125
157
244
190
step-size
step-size
1
10
1
1
1
NIL
HORIZONTAL

SLIDER
127
78
284
111
ring-radius
ring-radius
1
20
10
1
1
NIL
HORIZONTAL

SLIDER
4
38
284
71
num-turtles
num-turtles
0
1000
500
10
1
NIL
HORIZONTAL

SWITCH
5
78
123
111
draw-rings?
draw-rings?
0
1
-1000

PLOT
2
409
231
566
Average Distance
Time
Avg Dist
0.0
20.0
0.0
10.0
true
false
"set-plot-y-range 0 (5 * step-size)" ""
PENS
"default" 1.0 0 -13345367 true "" "plot ((sum [ dist ] of turtles) / count turtles)"

PLOT
232
409
474
566
Average X distance
Time
Avg Dist
0.0
20.0
0.0
10.0
true
false
"set-plot-y-range 0 (5 * step-size)" ""
PENS
"default" 1.0 0 -2674135 true "" "plot (sum [abs xc] of turtles) / num-turtles"

PLOT
476
409
708
566
Std. Dev. of Distance
Time
Std Dev
0.0
20.0
0.0
10.0
true
false
"set-plot-y-range 0 (5 * step-size)" ""
PENS
"default" 1.0 0 -10899396 true "" "plot (standard-deviation [ dist ] of turtles)"

@#$#@#$#@
## WHAT IS IT?

In this model the turtles engage in a "random walk."  Each turtle walks one step away from its current location in a different random direction at each clock tick. This movement is known as walking a 360-gon "lattice."  A lattice is a set of points on the plane (or in space) that form a grid on which turtles walk.

As the simulation continues, one can expect the turtles to become more spread out.  Will they ever return home (to their point of origin at 0 0)? Observe the kinds of patterns that develop as the turtles move.

## HOW TO USE IT

General Settings

Use the NUM-TURTLES slider to select how many turtles will participate in the random walk.

Use the STEP-SIZE slider to decide how far from its current location a turtle will move on each step.

How steps are implemented: if STEP-SIZE is set to n it will add (n * cos(theta)) to its current xcor value and it will add (n * sin(theta)) to its current ycor value.  It will have moved n patch units from its current location.  (Why?)

Each of the above movements would be considered a single "pace."

### Distance Rings

Set DRAW-RINGS to ON to draw the x-axis, y-axis, and a set of concentric circles about the point (0 0).  This switch must be set prior to pressing SETUP.

Use the RING-RADIUS slider to set the incremental distance between the distance rings.

### Plots

"Average Distance"  
- measures the average distance over all turtles, from home (the origin at 0,0) to their current coordinates.

"Average X-Distance"  
- measures the average distance over all turtles along the x-axis away from the origin (x = 0).

"Std.Dev. of Distance"  
- measures the standard deviation of all turtles' true distance from home to their current position.  NetLogo's standard deviation primitive is used.

Note that xcor and ycor values are stored in such a way that turtles are tracked even if they move outside the boundaries of the world.  These 'out of bounds' values are used to measure distances.  They are stored at turtle variable xc (for xcor) and yc (for ycor).

Distances are either zero or positive.

### Buttons

Press SETUP when all of the above selections have been made.  This will create the selected number of turtles at the bottom center of the world.

Press GO-ONCE to make the turtles move one pace.

Press GO to make the turtles move continuously.  To stop the turtles, press the GO button again.

## THINGS TO NOTICE

Try starting with 500 turtles with a step size of 1.  If you want to show distance rings, try setting the radius increment to 20.

Press SETUP then press GO.

The turtles all start at (0,0) (home).

Think about how you would define an 'average' turtle and an 'average' walk.

Where would you expect an average turtle to end up at a given time?  Why?

Will all turtles eventually return home?  If so, how many paces would you expect a turtle to travel before it returned to home?  Why?

What kinds of calculations or measurements would you use in trying to answer these questions?

Two characteristics of the plots you see are their smoothness and their slope.

To think about smoothness of the plots, notice how much the lines move up and down over time.

To think about slope of the plots, consider whether the plot appears to be going 'uphill' or 'downhill' and consider whether the 'hill' is steep or flat.

What do you notice about the smoothness and slope of the plots?  
- Are they smooth?  Why or why not?  
- Are they steep?  Why or why not?  
- For a given plot, is its steepness the same at the far edges and at the center?  Why or why not?

Does the plot have any symmetry?  Would you expect it to?  Why?

Does a single line plot ever change between positive and negative slopes?  Why or why not?  If it does, what does that mean?

## THINGS TO TRY

Try to answer the following questions before running the simulations.  Record your predictions.  Compare your predicted results with the actual results.  
- What reasoning led you to correct predictions?  
- What assumptions that you made need to be revised?

Try different numbers of turtles while keeping all other slider values the same.  
- What happens to the plot's smoothness and slope when the number of turtles is increased?  
- What happens to the plot's smoothness and slope when the number of turtles is decreased?  
- Does the simulation finish faster or slower than the first model?

Try different numbers of steps while keeping all other slider values the same.  
- What happens to the plot's smoothness and slope when the number of steps is increased?  
- What happens to the plot's smoothness and slope when the number of turtles is decreased?  
- Does the simulation finish faster or slower than the first model?

How do your answers to the above questions compare to the average values calculated for all turtles in a given simulation?

## EXTENDING THE MODEL

In this simulation, turtles can only move based on integer values of heading.  Change the model so that any value of heading between 0 and 360 degrees is possible.

Does this change the amount of time it would take for a turtle to return home?  How?  Why?

How does this change impact answers to other questions asked above?

One of the plots measures the average x-distance away from the origin over all turtles.  Obviously, it could have measured the y-distance instead.  Watch the plot under one run of the model, and then change the procedure so that it measures y-distance.  How do the plots differ?

Create a new monitor called home-turtles.  Have it display the number of turtles at (or very near) home.  Create a plot to display this information.

## NETLOGO FEATURES

Since NetLogo does not allow the world to be unbounded, the special code below is needed for this model to simulate the behavior, instead of stopping at the edge of the world the turtles hide themselves when they have moved outside the world.

    set xc xc + (step-size * dx)
    set yc yc + (step-size * dy)
    ; hide turtles who have moved out of the world
    ifelse patch-at (xc - xcor) (yc - ycor) = nobody
      [ ht ]
      [ st
        set xcor xc
        set ycor yc ]
    set dist sqrt (xc * xc + yc * yc)

Stamping of turtle shapes is used to draw a polar coordinate grid in the drawing layer.

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

ring
true
0
Circle -7500403 false true -1 -1 301

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

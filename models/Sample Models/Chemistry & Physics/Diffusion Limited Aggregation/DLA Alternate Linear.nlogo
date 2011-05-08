globals [
  next-color  ;; this is the next color we'll use when a new aggregate starts
              ;; (when a particle touches the bottom edge of the world)
]

breed [ movers mover ]     ;; gray particles zooming down from the top
breed [ stayers stayer ]   ;; colored particles that stay where they are


;; throughout, we assume that the turtles are all of size 1.
;; to support different-sized turtles, minor adjustments
;; would be needed.

to setup
  clear-all
  set-default-shape turtles "circle"  ;; applies to both breeds
  set next-color gray                 ;; first aggregate will be gray
  reset-ticks
end

to go
  ;; if any aggregates reach the top of the world, we're done
  if any? stayers with [pycor = max-pycor] [
    ask movers [ die ]
    stop
  ]
  ;; make one new falling particle per tick
  make-mover
  ;; we sort the green particles by who number, because we
  ;; need to make sure that closer particles move before farther
  ;; particles, otherwise we could get wrong results when two
  ;; greens are very close together.  SORT reports a list and
  ;; not an agentset (since agentsets are always in random
  ;; order), so we use FOREACH to iterate over the sorted list
  foreach sort movers [ ask ? [ move ] ]
  tick
end

to make-mover
  create-movers 1 [
    ;; start at a random location along the top edge
    setxy random-xcor max-pycor
    ;; head down
    set heading 180
  ]
end

to move  ;; mover procedure
  ;; any greens within a 2 step radius are potential
  ;; candidates for colliding with.  (Remember, in-radius
  ;; measures the distances between turtles' centers; but
  ;; we're checking for how far apart the edges are, hence
  ;; the 2 step radius: 1 is the sum of the two radii, and
  ;; another 1 for the distance from edge to edge.)  The
  ;; closest one we find is the one we actually collide with.
  let blocker min-one-of (stayers in-radius 2)
               [ collision-distance myself ]
  ;; if there's nothing within 2 steps, then we're free
  ;; to take a step forward
  if blocker = nobody
    [ fd 1
      if ycor = min-pycor
       [ set breed stayers
         set color next-color
         ;; the next aggregate that starts will be a new color
         set next-color next-color + 10 ]
      stop ]
  ;; see how far away the one we're about to collide with is.
  let dist [collision-distance myself] of blocker
  ;; if it's more than one step away, we're still free to
  ;; take a step forward.
  if dist > 1
    [ fd 1
      stop ]
  ;; otherwise, go forward until we're just touching the
  ;; "blocker", then cease any further motion
  fd dist
  set breed stayers
  ;; turn the same color as the particle we touched
  set color [color] of blocker
end

to fall  ;; mover procedure
  fd min list 1 (ycor + max-pycor)
end

;; essentially what all this math does is compute the intersection
;; point between a line and a circle.  the line is the straight path the
;; center point of the moving particle is following.  the circle is
;; a circle of radius 1 around the center of the stationary particle.
;; see http://mathworld.wolfram.com/Circle-LineIntersection.html
;; The details of the math are hard to follow, but the gist of it
;; is that the system of two equations, one for the circle and one
;; for the line, can be combined into a single quadratic equation,
;; and then that equation can be solved by the quadratic formula.
;; Note the strong resemblance of the formulas on the web page
;; to the quadratic formula.
to-report collision-distance [ incoming ]  ;; stayer procedure
  let x1 [xcor] of incoming - xcor
  let y1 [ycor] of incoming - ycor
  let d-x sin [heading] of incoming
  let d-y cos [heading] of incoming
  let x2 x1 + d-x
  let y2 y1 + d-y
  let big-d x1 * y2 - x2 * y1
  let discriminant 1 - big-d ^ 2
  ;; if the discriminant isn't positive, then there is no collision.
  if discriminant <= 0
    [ report false ]
  ;; the line and the circle will intersect at two points.  we find
  ;; both points, then look to see which one is closer
  let new-x-1 (   big-d *  d-y + sign-star d-y * d-x * sqrt discriminant)
  let new-y-1 ((- big-d) * d-x + abs       d-y       * sqrt discriminant)
  let new-x-2 (   big-d *  d-y - sign-star d-y * d-x * sqrt discriminant)
  let new-y-2 ((- big-d) * d-x - abs       d-y       * sqrt discriminant)
  let distance1 sqrt ((x1 - new-x-1) ^ 2 + (y1 - new-y-1) ^ 2)
  let distance2 sqrt ((x1 - new-x-2) ^ 2 + (y1 - new-y-2) ^ 2)
  ifelse distance1 < distance2
    [ report distance1 ]
    [ report distance2 ]
end

to-report sign-star [ x ]
  report ifelse-value (x < 0) [-1] [1]
end
@#$#@#$#@
GRAPHICS-WINDOW
110
10
617
538
35
35
7.0
1
10
1
1
1
0
0
0
1
-35
35
-35
35
1
1
1
ticks
30

BUTTON
16
41
91
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

BUTTON
16
80
91
113
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

@#$#@#$#@
## WHAT IS IT?

Like the main DLA model, this model demonstrates diffusion-limited aggregation.  In this variant, instead of particles forming a circular aggregate that grows from a central point, particles form vertical aggregates that grow along an edge.

In diffusion limited aggregation, particles moving (diffusing) in random trajectories stick together (aggregate) to form beautiful treelike branching fractal structures.  There are many patterns found in nature that resemble the patterns produced by this model: crystals, coral, fungi, lightning, and so on.

This model has "Alternate" in its name because, like DLA Alternate, it is based on slightly different rules than the main DLA model.  In the main model, the aggregate forms on a grid.  In this version, there is no grid; the particles are circles and we do the necessary calculations to determine exactly when the circles touch each other.  That makes this version more complicated, but arguably more physically accurate.

## HOW IT WORKS

Gray particles fall from the top of the world, straight down.  When a falling particle touches the floor, or a stationary particle, the falling particle stops moving.  Periodically, a new particle is created to keep the process going.

When a particle touches the floor, it changes color.  When a particle touches another particle, it takes that particle's color.  That makes it easy to see the separate aggregates that form.

## HOW TO USE IT

Press SETUP, then press GO.

SETUP: eliminates all existing particles and starts over

GO: runs the simulation

## THINGS TO NOTICE

Stuck particles block the passage of falling particles.  We get black areas that do not have any stuck particles, because the falling particles are moving in straight lines, so they cannot work their way in to fill the black areas.

The simulation runs slower as it goes on.  That's because there are more and more particles in the view that have to be redrawn.  If you freeze the view, the model will always run at the same speed.  You can also use the speed slider to "fast forward" the model.

The number of "live" aggregates (that can still grow) decreases over time.

## THINGS TO TRY

Try changing the size of the world (by editing the view).  What happens when the world is very tall?  How many live aggregates do you end up with?

## EXTENDING THE MODEL

Create a plot showing the number of live aggregates over time.  Over several runs, does this graph have a typical shape?

Introduce a "wiggle" into the path of falling particles.  That means that just before the particle moves forward, it turns a bit, randomly.  How does the affect the aggregate?  Experiment with different amounts of wiggle.

## NETLOGO FEATURES

The code is mostly the same as in DLA Alternate.

In particular, the `collision-distance` procedure, the one with all the hairy math, is the same.  It would actually be possible to use simpler math in this model, because we know that only the y coordinate of the moving particle is changing, while its x coordinate remains the same.

## RELATED MODELS

DLA and DLA Alternate are the same idea, but with particles all falling towards a central point instead of straight down.

## CREDITS AND REFERENCES

The mathematical procedures used in calculating the collision paths comes from: Eric W. Weisstein, "Circle-Line Intersection". From MathWorld--A Wolfram Web Resource.  http://mathworld.wolfram.com/Circle-LineIntersection.html
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
setup repeat 1500 [ go ]
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

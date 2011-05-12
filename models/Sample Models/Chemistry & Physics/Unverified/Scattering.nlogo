turtles-own
[
  force-x  ;; x-component of force vector
  force-y  ;; y-component of force vector
  vel-x    ;; x-component of velocity vector this time step
  vel-y    ;; y-component of velocity vector this time step
  dist     ;; the distance from the center

  ;; the following are needed to keep track of when the turtles go out of bounds:
  real-xcor
  real-ycor
]

globals
[
  scatter-angle
]

;;;
;;; setup procedures
;;;

to setup
  clear-all
  setup-target
  setup-particles
  set scatter-angle atan [vel-x] of turtle 0
                         [vel-y] of turtle 0
  reset-ticks
end

to setup-target
  if show-target?
  [
    ask patches
    [
      if (distancexy 0 0 < radius)
        [ set pcolor yellow ]
    ]
  ]
end

to setup-particles
  crt number
  [
    colorize
    distribute
    set vel-x 0
    set vel-y velocity

    ;; turtle 0 is treated specially to help distinguish an individual's behavior
    if (who = 0)
      [ set xcor turtle-0-position ]
    set real-xcor xcor
    set real-ycor ycor
    set dist distancexy 0 0
  ]
end

to colorize ;; turtle procedure
  ifelse ( trace? )
    [ set color (who / (number - 1)) * 139 ]
    [ set color white ]
end

to distribute ;; turtle procedure
  let x ( (min-pxcor) + ( ( who - 1 ) / ( number - 2 ) ) * (world-width - 1) )

  if patch-at (x - xcor) 0 = nobody
  [
    ifelse xcor > x
    [ set x ( x + world-width ) ]
    [ set x ( x - world-width ) ]
  ]

  setxy x (min-pycor)
end

;;;
;;; main procedures
;;;

to go
  if not any? turtles
    [ stop ]
  ask turtles
    [ move-particle ] ;; if the turtles move out of the world, they die
  if turtle 0 != nobody ;; if turtle 0 hasn't died yet
    [ set scatter-angle atan [vel-x] of turtle 0
                             [vel-y] of turtle 0 ]
  tick
end

to move-particle  ;; turtle procedure
  calc-force
  update-velocity
  update-position
  if trace?
    [ set pcolor color ]
  if who = 0
    [ set pcolor color ]
end

;; force function:  1/(r*r+target-radius*target-radius)  repulsion
to calc-force ;; turtle procedure
  set force-x  ((cos (atan (- real-ycor) (- real-xcor))) * (charge / (dist * dist + radius * radius)))
  set force-y  ((sin (atan (- real-ycor) (- real-xcor))) * (charge / (dist * dist + radius * radius)))
end

to update-velocity ;; turtle procedure
  set vel-x (vel-x - force-x)
  set vel-y (vel-y - force-y)
end

to update-position ;; turtle procedure
  set real-xcor (real-xcor + vel-x)
  set real-ycor (real-ycor + vel-y)
  ifelse patch-at (real-xcor - xcor) (real-ycor - ycor) != nobody
    ;; if the turtle is in the visible world, update xcor, ycor, and dist
    [
      setxy real-xcor real-ycor
      set dist distancexy 0 0
      show-turtle
    ]
    ;; if the turtle has moved out of the world, die
    [ die ]
end
@#$#@#$#@
GRAPHICS-WINDOW
163
10
455
323
70
70
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
-70
70
-70
70
1
1
1
ticks
30.0

SLIDER
10
39
153
72
number
number
10.0
300.0
280
10.0
1
NIL
HORIZONTAL

SLIDER
161
324
457
357
turtle-0-position
turtle-0-position
-70.0
70.0
-1
1.0
1
NIL
HORIZONTAL

SWITCH
173
365
306
398
trace?
trace?
0
1
-1000

SLIDER
10
105
154
138
charge
charge
0.0
100.0
70
5.0
1
NIL
HORIZONTAL

SLIDER
10
138
154
171
radius
radius
1.0
35.0
20
1.0
1
NIL
HORIZONTAL

SLIDER
10
71
153
104
velocity
velocity
1.0
5.0
3
0.5
1
NIL
HORIZONTAL

BUTTON
11
180
155
253
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

BUTTON
41
275
131
345
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
470
179
658
322
Speed
Time
Speed
0.0
25.0
0.0
5.0
true
false
"set-plot-y-range 0 velocity" ""
PENS
"turtle 0" 1.0 0 -2674135 true "" "if turtle 0 != nobody ;; if turtle 0 hasn't died yet\n  [ plot [sqrt (vel-x * vel-x + vel-y * vel-y)] of turtle 0 ]"

PLOT
470
38
658
180
Distance
Time
Distance
0.0
25.0
0.0
70.0
true
false
"set-plot-y-range 0 max-pycor" ""
PENS
"turtle 0" 1.0 0 -13345367 true "" "if turtle 0 != nobody ;; if turtle 0 hasn't died yet\n  [ plot [dist] of turtle 0 ]\n"

MONITOR
491
322
638
367
NIL
scatter-angle
3
1
11

SWITCH
307
365
440
398
show-target?
show-target?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

This project models the scattering of particles from a target that repels them.  An example of this is the scattering of alpha particles (helium nuclei) from a heavy nucleus such as gold.  This experiment, first done by Rutherford, provided important evidence that the positive charge in an atom is concentrated in a small place.

In this model, the target is an immovable patch with a variable charge and a variable radius in the center of the world.  A parallel beam of particles is sent upward from the bottom of the world, and the path of each particle is traced.  Each particle is repelled from the target according to Coulomb's inverse square law, modified for a distributed nuclear charge.  The particles do not interact with each other.

## HOW IT WORKS

Each particle is given a position, a velocity, and a charge.  Every time tick, each particle calculates the force that is enacted on it by the repulsion of the central charge.  This equation is Coulomb's inverse square law.  After the force is determined, it will revise it current velocity according the equation F = M * A where M = 1.  After which, the particle's new position is found by adding its new velocity to its current position.

## HOW TO USE IT

First select the number of particles with the NUMBER slider.  Set their initial velocity with the VELOCITY slider. Set the charge of the target with the CHARGE slider.  Set the radius of the target with the RADIUS slider.  Then press the SETUP button.

When the sliders have been set to a desired level and SETUP has been pressed, press the GO button to begin the simulation.

The TRACE? switch, if on, has each turtle mark out its position every time-tick.  In this way, you can see the arcs formed by different particles' travels.  When TRACE? is off, only one particle (turtle 0) marks out its position.

The TURTLE-0-POSITION slider sets the starting x-coordinate of turtle 0.  If TURTLE-0-POSITION is 0, the particle approaches the target head-on.  If it's positive, turtle 0 starts off to the right of center, and if it's negative, turtle 0 starts off to the left of center.

The SPEED of turtle 0 is displayed in a plot as well as its DISTANCE from the target.  The SCATTER-ANGLE monitor shows turtle 0's heading. (Zero is straight up, 90 is right, and so on.)

If set to on, the SHOW-TARGET? switch allows you to see the target.

## THINGS TO NOTICE

Each setting gives a family of paths for particles of equivalent initial velocity but different starting positions.  What is the shape of each trajectory?  Is it the same shape approaching and leaving the target?  What is the shape of the family of curves?

Do any of the paths intersect?  Does it depend on the settings of the sliders?

If two particles start off close to each other, will they end up close to each other?

A very large nucleus represents J.J. Thompson's "plum pudding" model of the atom, in which the charge was thought to be spread out in a volume as large as the atom itself. A very small nucleus represents Rutherford's discovery, namely that the charge is concentrated in a very small nucleus, about 1/10000 the size of the atom.

## THINGS TO TRY

You can study the trajectory of one particle by turning off TRACE?.  Change the TURTLE-0-POSITION slider to change the single particle's initial position.  This will allow you to study individual paths.  What happens to the particle's path when its velocity and the charge of the target are changed?  What needs to be true for particles to bounce almost straight backward?

The value of the SCATTER-ANGLE monitor, averaged over millions of particles, along with the particles' speed and the charge on the nucleus, is what an experimenter would actually be able to measure.  Devise an experiment that would give information about the size of the nucleus from this information alone.

If you knew the particle velocity and nuclear charge from other experiments, could you devise an experiment, using this model that would determine the size of the target?

## EXTENDING THE MODEL

Put in a different function for the force between the nucleus and the particles --- 1/r dependence, r dependence, or attraction instead of repulsion.  This can be done in the procedure `calc-force`.  A repulsive force will "scatter" the particles, but an attractive force will put some of them into orbits.

Let the particles begin with a constant velocity, or give them all a random velocity.  Or try giving each particle a variable charge, which directly affects the strength of the acting force upon it.

Try having a lattice of targets, and vary the targets' spacing.

## NETLOGO FEATURES

Notice that the procedure `move-particle` is all turtle commands.

When you examine the Code tab, note that standard turtle movement commands such as `fd 1` aren't used.  Instead the x-coordinates and y-coordinates of the turtles are manipulated directly.

## RELATED MODELS

Gravitation also calculates an inverse-square force between particles and changes their motion accordingly.  In Gravitation, each particle looks at every other particle, whereas in Scattering, each particle interacts only with the target.

## CREDITS AND REFERENCES

Martin Rocek made important modifications to this model. He writes, "the main point of my modifications was introducing rcore (radius); it has the effect of smoothing out the target, that is, making something more like the old 'plum-pudding' model of the atom that held sway before Rutherford's experiment. When rcore is large enough, even though the scattering of particles with impact parameters significantly bigger than rcore is essentially unchanged, no particles experience large deflections. As you make rcore smaller, the hard core is restored, and large angle scattering returns."
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
NetLogo 5.0beta2
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

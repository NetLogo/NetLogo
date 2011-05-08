turtles-own
[ fx     ;; x-component of force vector
  fy     ;; y-component of force vector
  vx     ;; x-component of velocity vector
  vy     ;; y-component of velocity vector
  xc     ;; real x-coordinate (in case particle leaves world)
  yc     ;; real y-coordinate (in case particle leaves world)
  r-sqrd ;; square of the distance to the mouse
]

globals
[ m-xc  ;; x-coordinate of acting mass
  m-yc  ;; y-coordinate of acting mass
  g     ;; Gravitational Constant to slow the acceleration
]

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;
to setup
  clear-all
  set g 0.5
  set-default-shape turtles "circle"
  crt number
  [
    if (not colors?)
    [ set color white ]
    set size 10
    fd (random-float (max-pxcor - 6))
    set vx 0
    set vy 0
    set xc xcor
    set yc ycor
  ]
  reset-ticks
end

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Runtime Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;
to go
  if mouse-down? [
    ;; Get the mouse(mass) position.
    set m-xc mouse-xcor
    set m-yc mouse-ycor
    ask turtles [ gravitate ]
    fade-patches
    tick
  ]
end

to gravitate ;; Turtle Procedure
  update-force
  update-velocity
  update-position
end

to update-force ;; Turtle Procedure
  ;; Similar to 'distancexy', except using an unbounded plane.
  set r-sqrd (((xc - m-xc) * (xc - m-xc)) + ((yc - m-yc) * (yc - m-yc)))

  ;; prevents divide by zero
  ifelse (r-sqrd != 0)
  [
    ;; Calculate component forces using inverse square law
    set fx ((cos (atan (m-yc - yc) (m-xc - xc))) * (mass / r-sqrd))
    set fy ((sin (atan (m-yc - yc) (m-xc - xc))) * (mass / r-sqrd))
  ]
  [
    ;; if r-sqrd = 0, then it's at the mass, thus there's no force.
    set fx 0
    set fy 0
  ]
end

to update-velocity ;; Turtle Procedure
  ;; Now we update each particle's velocity, by taking the old velocity and
  ;; adding the force to it.
  set vx (vx + (fx * g))
  set vy (vy + (fy * g))
end

to update-position ;; Turtle Procedure
  set xc (xc + vx)
  set yc (yc + vy)

  ifelse patch-at (xc - xcor) (yc - ycor) != nobody
  [
    setxy xc yc
    ifelse (colors?)
    [
      if (color = white)
      [ set color 5 + 10 * random 14 ]
    ]
    [ set color white ]
    show-turtle
    if (fade-rate != 100)
    [ ifelse (color = white)
      [ set pcolor red + 3 ]
      [ set pcolor color + 3 ]
    ]
  ]
  [ hide-turtle ]
end

to fade-patches
  ask patches with [pcolor != black]
  [ ifelse (fade-rate = 100)
    [ set pcolor black ]
    [ if (fade-rate != 0)
      [ fade ]
    ]
  ]
end

to fade ;; Patch Procedure
  let new-color pcolor - 8 * fade-rate / 100
  ;; if the new-color is no longer the same shade then it's faded to black.
  ifelse (shade-of? pcolor new-color)
  [ set pcolor new-color ]
  [ set pcolor black ]
end
@#$#@#$#@
GRAPHICS-WINDOW
195
10
607
443
100
100
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
-100
100
-100
100
1
1
1
ticks
10

BUTTON
10
42
182
75
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
10
75
182
108
number
number
0
100
10
1
1
particles
HORIZONTAL

SLIDER
10
151
182
184
mass
mass
0.0
1000
50
1.0
1
NIL
HORIZONTAL

BUTTON
10
118
72
151
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

SWITCH
72
118
182
151
colors?
colors?
0
1
-1000

SLIDER
10
184
182
217
fade-rate
fade-rate
0
100
0.3
0.1
1
%
HORIZONTAL

TEXTBOX
10
232
182
311
Click and hold the mouse in the view while GO is running to act as a gravitational source of mass MASS.
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

This project displays the common natural phenomenon expressed by the inverse-square law.  Essentially this model displays what happens when the strength of the force between two objects varies inversely with the square of the distance between these two objects.

## HOW IT WORKS

In this model the formula used to guide each object's behavior is the standard formula for the Law of Gravitational Attraction:

>(m1 * m2 * G) / r<sup>2</sup>

This is a single force 'n-body' model, where we have a certain number of small particles, and one large acting mass (the mouse pointer). The force is entirely one-way: the large mass remains unaffected by the smaller particles around it.  And the smaller particles remain unaffected by each other as well.  (Note that this is purely for purposes of simulation.  In the real world, a force such as gravity acts on all bodies around it.)

Gravity is the best example of such a force. You can watch the particles form elliptic orbits around the mouse pointer, or watch them slingshot around it, similar to how a comet streaks past our sun. Think of the individual objects as planets or other solar bodies, and see how they react to various masses that move or remain stationary.

## HOW TO USE IT

First select the number of particles with the NUMBER slider. Then press the SETUP button to create and scatter them across the world.

The MASS slider sets the value of the mass of the acting force. (Thus, it also determines at what distances the particles can safely orbit before they get sucked in by an overwhelming force.)

The FADE-RATE slider controls the percent of color that the paths marked by the particles fade after each cycle.  Thus at 100% there won't be any paths as they fade immediately, and at 0% the paths won't fade at all.  With this you can see the ellipses and parabolas formed by different particles' travels.

The COLORS? switch, when set to ON, assigns different colors to the particles, otherwise they will all be white.

When the sliders have been set to desirable levels, press the GO button to begin the simulation. Move the mouse to where you wish it to begin, and click and hold the mouse button. This will start the particles moving. If you wish to stop the simulation (say, to change the value of MASS), release the mouse button and the particles will stop moving.  You may then change any settings you wish (except PARTICLES).  Then, to continue the simulation, simply put your mouse in the window again and click and hold. Objects in the window will only move while the mouse button is pressed down within the window.

## THINGS TO NOTICE

The most important thing to observe is the behavior of the particles. Notice that, as the particles have no initial velocity of their own, a single motionless acting mass will just pull them all in. Even a very slight mass (MASS set to a small value) will pull in all the particles. (Due to limited precision beyond a certain point, the motive-force on a particle can become zero.)

Move the mouse around - watch what happens if you move it quickly or slowly. Jiggle it around in a single place, or let it sit still. Observe what patterns the particles fall into. (Keep FADE-RATE low to watch this explicitly.)

## THINGS TO TRY

There are a few other parameters, set in the code, that affect the behavior of the model.  The force acting upon each particle is multiplied by a constant, 'g' (another global variable).  Feel free to play with their values, set in the procedure 'setup'. (Of course, the default value of g for the model, 0.5, is much higher than the value used in Newtonian Mechanics, 6.67e-11.)

Initial conditions are very important for a model such as this one. Try changing how the particles are placed during the `setup` procedure.

Make sure to watch how different values of the MASS slider impact the model.

## EXTENDING THE MODEL

Let the particles begin with a constant velocity, or give them all a random velocity. You could add a slider that would let the user set the velocities, and thus be able to compare the effects of different speeds. Or try giving each particle a variable mass, which directly affects the strength of the acting force upon it.

The model assumes the force to be an attractive force (the particles tend to be pulled towards it). However, it should be a relatively easy change to make this into a repulsive force. Try setting up the model with a repulsive force, and observe what happens.

## NETLOGO FEATURES

This model creates the illusion of a plane of infinite size, to better model the behavior of the particles. Notice that, with path marking you can see most of the ellipse a particle draws, even though the particle periodically shoots out of bounds. This is done through a combination of the basic turtle primitives `hide-turtle` and `show-turtle`, when a particle hits the boundary, keeping every turtle's `xcor` and `ycor` as special turtle variables `xc` and `yc`, and calculations similar to the `distance` primitive that uses `xc` and `yc` instead of `xcor` and `ycor`.

When you examine the code, take note that standard turtle commands like `set heading`, `fd 1`, and so on aren't used here. Everything is done directly to the x  and y coordinates of the turtles.

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

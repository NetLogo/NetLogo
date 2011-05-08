breed [ particles particle ]  ;; the things that are affected by the field
breed [ vectors vector ]    ;; the vectors that affect the particles


globals
[
  max-modulus  ;; the maximum modulus of all the vectors
  clicked?     ;; true if we have clicked the mouse button but have not yet placed a particle
  step-size    ;; the amount a particle moves forward
]

vectors-own
[
  modulus  ;; the length of the vector
]

;; set up the field, and create vectors
to setup
  clear-all
  set clicked? false

  ;; have particles move forward a small amount each time so that we
  ;; don't spiral too much but also so that the model doesn't run too slowly
  set step-size 0.0001

  ;; create vectors at regular intervals to see the effect of the force
  ;; at a particular place.
  ask patches
  [
    if (pxcor mod 13 = 0) and (pycor mod 13 = 0)
    [
      sprout-vectors 1
      [ setup-vector ]
    ]
  ]
  ;; draw vector field
  set max-modulus (max [modulus] of vectors)
  ask vectors [ show-vector ]
  reset-ticks
end

;; make the turtle become a vector and initialize the vector's variables
to setup-vector  ;; turtle procedure
  set color green
  pen-down
  if (force-x != 0) or (force-y != 0)
    [ set heading atan force-x force-y ]
  set modulus distancexy 0 0
end

;; particles update their orientation according to the vector-field force
;; operating on the patch where they are at
to go
  let stop? false
  ask particles
  [
    ;; calculate the heading based on the force where this turtle is
    if force-x != 0 or force-y != 0
      [ set heading (atan force-x force-y) ]

    forward step-size * (distancexy 0 0)
  ]
  ;; 100 is an arbitrary factor used to produce a reasonable
  ;; frequency of view updates
  tick-advance 100 * step-size
  ;; if one of the particles was going to wrap around the world, stop.
  if stop?
  [ stop ]
end

;; report true if we will wrap around if we move forward by step-size
to-report going-to-wrap?  ;; turtle procedure
  let next-patch patch-ahead step-size
  report next-patch = nobody
end

;; place test particles
to place-particles
  if mouse-down?
  [ set clicked? true ]
  if (not mouse-down?) and clicked?
  [ place-particle mouse-xcor mouse-ycor
    display ]
end

;; create a particle at (x,y)
to place-particle [x y]
  create-particles 1
  [
    setxy x y
    set size 10
    set heading 0
    set color red
    pd  ;; put the pen down so that we can see where it has traveled
    if force-x != 0 or force-y != 0
      [ set heading (atan force-x force-y) ]
  ]
  set clicked? false
end

;; calculate the horizontal force where the turtle is located
to-report force-x  ;; turtle procedure
  report ycor
end

;; calculate the vertical force where the turtle is located
to-report force-y  ;; turtle procedure
  report (- xcor)
end

;; draw the vector using a turtle to display strength and direction of field
to show-vector  ;; turtle procedure
  set modulus (10 * modulus / max-modulus)
  forward modulus
  set color yellow
end
@#$#@#$#@
GRAPHICS-WINDOW
134
10
687
584
90
90
3.0
1
10
1
1
1
0
0
0
1
-90
90
-90
90
1
1
1
ticks

BUTTON
10
42
124
75
setup-field
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
10
124
124
157
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
9
83
124
116
place-particles
place-particles
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

This is a mathematical model that demonstrates abstract vector fields and integral curves.

Generally speaking, a field is a "region in which a body experiences a force as the result of the presence of some other body or bodies.  A field is thus a method of representing the way in which bodies are able to influence each other.  For example, a body that has mass is surrounded by a region in which another body that has mass experiences a force tending to draw the two bodies together.... The strength of any field can be described as the ratio of the force experienced by a small appropriate specimen to the relevant property of that specimen, e.g. force/mass for the gravitational field"  (Oxford Minidictionary of Physics).

By 'abstract vector fields' we mean that this model is not committed to any specific type of force, such as gravity or magnetism.  Rather, it simulates a general field, in which some focal property of influence affects a "small appropriate specimen", or particle, placed in the field.

Normally, if you look at a field with bare eyes, you will not necessarily see the forces.  For instance, if you drop an apple it falls down, even though you cannot see the gravitational force.  The apple is an object in the gravitational field.  You saw how it behaved so you could guess that there is some force that made it go down.  Humans do not perceive (visually) forces of gravitation or electro-magnetic forces.  However, in a model, we can use little arrows (vectors) to show where, how forceful, and in which direction there are forces in this field.

## HOW IT WORKS

In this model, the field is plotted using vector graphics: green streaks are individual vectors with yellow turtles serving as arrowheads.  The length of each vector is roughly proportional to the magnitude of the vector field at each point.  In this model, it is just the distance from the origin: The further away from the origin, the larger the vector.  Also, all vectors are aimed clockwise along tangents to circles centered on the origin.

The vectors show you in what direction and how forcefully an appropriate specimen -- here, a 'particle' -- will be "knocked about" once it is placed the field.  Once the particle is "knocked" to a new location, it will be knocked yet again by the force there (represented by the vector).  Actually, it being "knocked about" continuously, but in this simulation, the "knock" occurs at discrete points in the field.  Since the particle does not use up the forces, it will keep being knocked about.  The path the particle takes is called its 'trajectory.'  You will be able to track this trajectory because the particle will leave a red trail behind it as it moves along its trajectory.  Trajectories in vector fields are called 'integral curves.'

Even though behavior of particles can be interesting and possibly unanticipated, owing to forces not being distributed uniformly in the field, or some other factor, we have chosen, for clarity, a vector field with a logical and consistent relation between location in space and size/orientation of the force.  The vector field chosen for this particular model is

    - y d/dx  +  x d/dy

Ideally, in the particular force field modeled here, the particle trajectories should be concentric circles (that is, the particle should go round and round along the same circular trajectory).

## HOW TO USE IT

SETUP: Clears the world and computes the vector field.
PLACE-PARTICLES: Puts the program into the mode in which you can position red test-particles by clicking anywhere in the View.
GO: Runs the simulation continuously to show the integral curves.

## THINGS TO NOTICE

Notice that the vectors grow in length as you move away from the origin.  What effect do short vectors have on a particle?  Long vectors?

The way this model is programmed, each particle moves some finite amount before calculating its new heading.  Therefore, the particles do not turn as much as they would if their headings were continuously recalculated.  This causes their trajectories to spiral slowly outward.  (You have to let the model run for a while before this becomes apparent.)  We tried to minimize this by having the particles move forward only a very small amount at each time step (the variable `step-size`).  We couldn't make this amount too small since the model would then run too slowly.  If you want the particles to spiral less, or you want the model to run faster, change this value.

## THINGS TO TRY

Place particles in different parts of the world.  Does the particle's position have any effect on the trajectory?

## EXTENDING THE MODEL

Try a different vector field by changing it in the `setup-vector`, `force-x`, and `force-y` procedures.  For instance, if you choose

    x d/dx - y d/dy

the integral curves will be hyperbolas.

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
setup
place-particle 0 max-pycor - 3
repeat 50000 [ go ]
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

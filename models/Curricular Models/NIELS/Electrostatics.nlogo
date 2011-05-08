breed [centers center]
breed [particles particle]

particles-own [
  fx     ;; x-component of force vector
  fy     ;; y-component of force vector
  vx     ;; x-component of velocity vector
  vy     ;; y-component of velocity vector
]

globals [
  r          ;; distance between particle and center
  potential  ;; potential energy of particle
  force      ;; modulus of force vector
  previous-permittivity ;; the last recorded value of permittivity
  c-charge   ;; charge of the center particle
]

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  set-default-shape turtles "circle"
  ask patches [ set pcolor background ]
  set previous-permittivity permittivity
  set c-charge abs charge
  create-centers 1 [
    set size 20
    set color gray
    hide-turtle
  ]
  create-particles 1 [
    set color blue
    set size 10
    fd random-float (max-pxcor - 6)
  ]
  reset-ticks
end

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Runtime Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

to go
  if not any? particles [ stop ]
  ;; label the particles with the appropriate charge
  ask particles [ set label charge ]
  ;; color the patches for the appropriate permittivity
  ;; if the permittivity slider has changed
  if permittivity != previous-permittivity
  [
    ask patches [ set pcolor background ]
    set previous-permittivity permittivity
  ]
  set c-charge abs charge
  if mouse-down?
  [
    ask centers [ show-turtle ]
    every 0.1
    [
      ;; Get the mouse(mass) position.
      let m-xc mouse-xcor
      let m-yc mouse-ycor
      ask centers
      [
        setxy m-xc m-yc
        set label c-charge
      ]
      ask particles [
        update-force
        move
      ]
      ask particles [ calculate-potential-energy ]
      fade-patches
      tick
    ]
  ]
end

to update-force  ;; particle procedure
  set r distance one-of centers
  ;; if r = 0, then it's at the mass, and then the model stops;
  ;; prevents divide by zero
  if r = 0 [ die ]
  ;; Calculate force using inverse square law
  set force (- charge) * c-charge / (r ^ 2)
  ;; Separate force into x and y components
  face one-of centers
  set fx force * dx
  set fy force * dy
end

to move  ;; particle procedure
  ;; update the particle's velocity, by taking the old velocity and
  ;; adding the force to it.
  set vx vx + fx * permittivity
  set vy vy + fy * permittivity
  ;; disappear if we reached the edge
  if patch-at vx vy = nobody [ die ]
  ;; update the particle's position
  setxy (xcor + vx) (ycor + vy)
  ;; leave a trail
  set pcolor background - 5
end

to calculate-potential-energy  ;; particle procedure
  set r distance one-of centers
  set potential charge / r
end

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Visual Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;

to-report background
  report permittivity * 8 + 5
end

to fade-patches
  ask patches with [pcolor != background] [
    set pcolor min list (pcolor + fade-rate * 0.005) background
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
194
10
706
543
125
125
2.0
1
18
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

BUTTON
9
40
95
73
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
7
157
190
190
charge
charge
-8
8.0
-4
4
1
units
HORIZONTAL

BUTTON
104
40
190
73
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

SLIDER
740
502
930
535
fade-rate
fade-rate
0
100
40
5
1
%
HORIZONTAL

TEXTBOX
15
83
187
157
Click and hold the mouse in the view while GO is pressed to control a positive electrostatic charge.
11
0.0
0

PLOT
709
194
957
344
Potential Energy
time
potential
0.0
10.0
0.0
1.0
true
true
"" ""
PENS
"potential" 1.0 0 -16777216 true "" "plot potential"

SLIDER
7
191
190
224
permittivity
permittivity
1
9
9
2
1
units
HORIZONTAL

MONITOR
8
269
126
314
Coulomb's force
force
3
1
11

MONITOR
7
367
173
412
distance (r)
r
3
1
11

MONITOR
7
318
172
363
potential energy
potential
3
1
11

PLOT
709
345
957
495
Distance (r)
time
distance
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"r" 1.0 0 -16777216 true "" "plot r"

PLOT
709
36
957
193
Coulomb's Force
time
force
0.0
10.0
0.0
0.05
true
false
"" ""
PENS
"force" 1.0 0 -16777216 true "" "plot force"

@#$#@#$#@
## WHAT IS IT?

This model displays the common natural phenomenon expressed by the Coulomb's inverse-square law.  It shows what happens when the strength of the force between two charges varies inversely with the square of the distance between them.

## HOW IT WORKS

In this model the formula used to guide each charge's behavior is the standard formula for Coulomb's law:

F = (q1 * q2 * Permittivity) / (r^2)

In this formula:
- "F" is the force between the two charges q1 and q2.
- "Permittivity", the constant of proportionality here, is a property of the medium in which the two charges q1 and q2 are situated.
- "r" is the distance between the centers of the two charges.

This is a single force two body model, where we have a charge q1 (the blue particle that is created when you press SETUP) and a proton (q2) (the gray particle that appears  when you press the mouse button in the view).  The force is entirely one-way: only q1 is attracted towards (or repelled from) the proton (q2), while the proton (q2) remains unaffected.  Note that this is purely for purposes of simulation.  In the real world, Coulomb's force acts on all bodies around it.

Gravity is another example of an inverse square force.  Roughly speaking, our solar system resembles a nucleus (sun) with electrons (planets) orbiting around it.

For certain values of q1 (which you can control by using the CHARGE slider), you can watch q1 form elliptic orbits around the mouse pointer (q2), or watch q1 slingshot around q2, similar to how a comet streaks past our sun. The charges q1 and q2 are always set equal in magnitude to each other, although they can differ in their sign.

## HOW TO USE IT

When you press the SETUP button, the charge q1 is created in a medium determined by the permittivity value from the PERMITTIVITY slider. When you click and hold down the mouse anywhere within the view, the model creates a unit of positive charge (q2) at the position of the mouse.

The CHARGE slider sets the value of the charge on q1.  First, select the value of CHARGE on q1.  (For simulation ease, value of the Charge on q2 is set to be +1 unit.  Thus, it also determines at what distances the particles can safely orbit before they get sucked in by an overwhelming force.)

The FADE-RATE slider controls how fast the paths marked by the particles fade.  At 100% there won't be any paths as they fade immediately, and at 0% the paths won't fade at all.

The PERMITTIVITY slider allows you to change values of the constant of proportionality in Coulomb's law. What does this variable depend on -- the charges, or the medium in which the charges are immersed?

When the sliders have been set to desirable levels, press the GO button to begin the simulation.  Move the mouse to where you wish q2 to begin, and click and hold the mouse button. This will start the particles moving. If you wish to stop the simulation (say, to change the value of CHARGE), release the mouse button and the particles will stop moving.  You may then change any settings you wish.  Then, to continue the simulation, simply put your mouse in the window again and click and hold. Objects in the window will only move while the mouse button is pressed down within the window.

## THINGS TO NOTICE

The most important thing to observe is the behavior of the q1 (the blue charge).

What is the initial velocity for q1?

What happens as you change the value of q1 from negative to positive?

As you run the model, watch the graphs on the right hand side of the world. What can you infer from the graphs about the relationship between potential energy and distance between charges? What can you say about the relationship between Coulomb's force and distance between the charges from the graphs?

Move the mouse around -- watch what happens if you move it quickly or slowly. Jiggle it around in a single place, or let it sit still.  Observe what patterns the particles fall into.  (You may keep FADE-RATE low to watch this explicitly.)

## THINGS TO TRY

Run the simulation playing with different values of:
a) charge -- make sure to watch how different values of the CHARGE slider impact the model for any fixed value of permittivity.
b) permittivity -- make sure to watch how different values of the PERMITTIVITY slider impact the model for any fixed value of charge.

Can you make q1 revolve around q2?  Imagine, if q1 would be an electron and q2 a proton, then you have just built a hydrogen atom...

As the simulation progresses, you can take data on how
a) Force between the two charges varies with distance between charges;
b) Potential energy changes with distance between charges;
c) Force depends on permittivity.

In each case, take 8 to 10 data points.  Plot your results by hand or by any plotting program.

## EXTENDING THE MODEL

Assign a fixed position to the proton (q1), i.e., make it independent of the mouse position.  Assign a variable to its magnitude.

Now create another charge of the breed "centers", and assign a fixed position to it in the graphics window.  Run the model for different positions, magnitude and signs (i.e., "+"ve or "-"ve) of the new "center".

Create many test-charges.  Then place the two "centers", of opposite signs and comparable magnitudes, near the two horizontal edges of the world.  Now run the model.

## RELATED MODELS

Gravitation

## NETLOGO FEATURES

When a particle moves off of the edge of the world, it doesn't re-appear by wrapping onto the other side (as in most other NetLogo models). The model stops when the particle exits the world.

## CREDITS AND REFERENCES

This model is a part of the NIELS curriculum. The NIELS curriculum has been and is currently under development at Northwestern's Center for Connected Learning and Computer-Based Modeling and the Mind, Matter and Media Lab at Vanderbilt University. For more information about the NIELS curriculum please refer to http://ccl.northwestern.edu/NIELS.
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
need-to-manually-make-preview-for-this-model
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

breed [particles particle]
breed [temp-particles temp-particle]

globals [ num-temp-particles ]

to setup
  clear-all
  set-default-shape particles "circle"
  set-default-shape temp-particles "circle"
  ;; place the first particle in an arbitrary location on the first ring
  create-particles 1 [
    set size particle-size
    fd ring-dist
    set color yellow
  ]
  reset-ticks
  tick
end

;; each tick we add just one new particle
to go
  let ring-number count particles + 1
  let dist ring-dist * ring-number
  if dist >= max-pxcor [ stop ] ;; stop when we get to the world edge
  ;; as radius gets bigger, we can either always use 360 temp particles,
  ;; or we can scale up based on the circumference of the ring
  let ntemps ifelse-value fixed-temps? [360] [round (dist * 30)]

  create-temp-particles ntemps [
    set size 1
    fd dist
    set color green
    ifelse show-temps? [ show-turtle ] [ hide-turtle ]
  ]
  set num-temp-particles count temp-particles

  ;; place the next particle at the location  on the ring with least potential energy

  if energy-approximation = "inverse distance" [
    let min-particle min-one-of temp-particles [sum ([1 / (distance myself) ^ alpha] of particles)]
    ask min-particle [     ;; ask the min-temp to create a clone that is a permanent particle
      hatch-particles 1 [
        show-turtle
        set color yellow
        set size particle-size
      ]
    ]
  ]

  if energy-approximation = "exponential" [
    let min-particle min-one-of temp-particles [sum [exp (- (distance myself) / beta)] of particles]
    ask min-particle [     ;; ask the min-temp to create a clone that is a permanent particle
      hatch-particles 1 [
        show-turtle
        set color yellow
        set size particle-size
      ]
    ]
  ]

  ;; if we tick before killing off the temporary particles, they'll be visible
  ;; in the view.
  if show-temps? [ tick ]
  ask temp-particles [ die ]  ;; kill off the temporary particles
  if not show-temps? [ tick ]
end
@#$#@#$#@
GRAPHICS-WINDOW
225
15
718
529
80
80
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
-80
80
-80
80
1
1
1
ring-number
30

BUTTON
15
95
75
128
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
80
95
145
128
step
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

SLIDER
15
15
210
48
ring-dist
ring-dist
0.01
1
0.2
.01
1
NIL
HORIZONTAL

BUTTON
150
95
210
128
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

MONITOR
70
275
155
320
# temps
num-temp-particles
17
1
11

SWITCH
15
230
210
263
show-temps?
show-temps?
0
1
-1000

SWITCH
15
190
210
223
fixed-temps?
fixed-temps?
1
1
-1000

SLIDER
15
55
210
88
particle-size
particle-size
0.1
3
1.8
0.1
1
NIL
HORIZONTAL

MONITOR
70
135
155
180
# particles
count particles
17
1
11

SLIDER
15
380
107
413
alpha
alpha
1
3
3
2
1
NIL
HORIZONTAL

SLIDER
110
380
211
413
beta
beta
0
1
0.6
.1
1
NIL
HORIZONTAL

CHOOSER
15
330
210
375
energy-approximation
energy-approximation
"inverse distance" "exponential"
0

@#$#@#$#@
## WHAT IS IT?

This model shows the characteristic double spiral formation we see in many plants.  Most plants have leaves that spiral up the stem, appearing as a double spiral when viewed from above. This same double spiral is also seen in flower heads such as sunflowers and pine cones.

The key idea is that the pattern emerges from simple physics--the minimization of repulsive energy. Neither evolutionary biology nor the geometry of angles are invoked in this model.

## HOW IT WORKS

The model is initialized with one particle in an inner ring around the origin.

At each tick of the model, a new particle is added and placed in a concentric ring, a fixed distance from the last ring.

The location of the new particle is determined in the following manner. Place 360 or more temporary particles on the ring, calculate the repulsive energy for each temporary particle by choosing one of two energy approximation methods, and choose the temporary particle has the minimum total repulsive energy. The rest of the temporary particles are then removed.

This model includes two different methods to approximate the total repulsive energy of a particle. Both methods are based on the assumption that particles that are closest to one another exert much more repulsive energy than particles that are further away. The first method assumes that the relationship between distance and repulsion is inverse, while the second assumes that it is one of exponential decay.

## HOW TO USE IT

The SETUP procedure places one particle at a distance of RING-DIST from the origin.

ADD-PARTICLE adds one new particle into the next concentric ring.

RING-DIST is the distance between successive rings.

The PARTICLE-SIZE is just the visual display size for the particles. It does not affect the growth placement pattern.

GO adds particles until the spiral reaches the edge of the world.

The FIXED-TEMPS? switch determines whether to use a fixed or variable number of temporary particles. If on, then 360 temporary particles are used. If off, then the number of particles increases with distance from the origin.

The SHOW-TEMPS? switch, if on, displays the temporary particles.

The ENERGY-APPROXIMATION chooser determines which energy approximation method is used. INVERSE DISTANCE uses the repulsive energy approximation 1/distance^ALPHA, and EXPONENTIAL uses the Born and Mayer repulsive energy approximation, e^(-distance/BETA). The ALPHA and BETA sliders determine the constants used in each of these approximations.

## THINGS TO NOTICE

Notice the double spiral pattern that emerges from these simple rules. Is the spiral pattern always the same with the same settings? Why or why not?

Do you notice any other patterns in the left and right handed spirals?

## THINGS TO TRY

Try varying the RING-DIST to get looser or tighter spirals.

Play with the various switches to visualize the temporary particles and the effect of different energy approximations.

## EXTENDING THE MODEL

Can you find a way to generate pleasingly colored spirals?

What happens if you allow some random perturbations in particle placement?

## NETLOGO FEATURES

Note the use of the `min-one-of` primitive to find the temporary particle with the least repulsive energy.

## RELATED MODELS

Sunflower
Sunflower Biomorphs

## CREDITS AND REFERENCES

This model is a replication of Victor Stenger's double spiral model in:
Stenger, V. (2008). God: The Failed Hypothesis.

The double spiral patterns as seen in nature are described in:
Ball, P. (1995). The Self-Made Tapestry.

Douady, S. and Couder, Y. (1992). Phyllotaxis as a Physical Self-Organized Process. Physical Review Letters, 68(13), 2098--2101.

Dove, M. T. Structure and Dynamics: An Atomic View of Materials. New York: Oxford University Press, 2003.
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

sheep
false
0
Rectangle -7500403 true true 151 225 180 285
Rectangle -7500403 true true 47 225 75 285
Rectangle -7500403 true true 15 75 210 225
Circle -7500403 true true 135 75 150
Circle -16777216 true false 165 76 116

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
1
@#$#@#$#@

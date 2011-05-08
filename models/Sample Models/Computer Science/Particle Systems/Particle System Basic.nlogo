turtles-own
[
  velocity-x             ; particle velocity in the x axis
  velocity-y             ; particle velocity in the y axis
  force-accumulator-x    ; force exerted in the x axis
  force-accumulator-y    ; force exerted in the y axis
]

to setup
  clear-all
  ask patch 0 (max-pycor / 2) [
    sprout num-particles [
      set velocity-x 10 - (random-float 20) ; initial x velocity
      set velocity-y 10 - (random-float 20) ; initial y velocity
      pen-down
    ]
  ]
  reset-ticks
end

to go
  if not any? turtles [ stop ]
  compute-forces ; calculate the forces and add them to the accumulator
  apply-forces   ; calculate the new location and speed by multiplying the
                 ; forces by the step-size
  tick-advance step-size
  display
end

; calculate and sum all the forces exerted on the particles
to compute-forces
  ask turtles
  [
    ; clear force accumulators
    set force-accumulator-x 0
    set force-accumulator-y 0
    ; calculate forces
    apply-gravity
  ]
end

; updates the accumulator with the gravity force
to apply-gravity  ;; turtle procedure
  set force-accumulator-y force-accumulator-y - gravity-constant
end

; calculates the position of the particles at each step
to apply-forces
  ask turtles
  [
    ; calculate the new velocity of the particle
    set velocity-x velocity-x + (force-accumulator-x * step-size)
    set velocity-y velocity-y + (force-accumulator-y * step-size)
    ; calculate the displacement of the particle
    let step-x velocity-x * step-size
    let step-y velocity-y * step-size
    ;; if the turtle tries to leave the world let it die
    if patch-at step-x step-y = nobody [ die ]
    ;; if the turtle does not go out of bounds
    ;; add the displacement to the current position
    let new-x xcor + step-x
    let new-y ycor + step-y
    facexy new-x new-y
    setxy new-x new-y
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
245
15
675
466
17
17
12.0
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
-17
17
1
1
1
ticks
1000.0

BUTTON
126
78
231
111
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
12
115
232
148
gravity-constant
gravity-constant
-10
20
8
0.1
1
NIL
HORIZONTAL

SLIDER
12
152
232
185
step-size
step-size
0.001
0.1
0.01
0.001
1
NIL
HORIZONTAL

BUTTON
14
78
119
111
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
12
42
232
75
num-particles
num-particles
0
1000
50
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

Particle systems are used in computer graphics to simulate the appearance of physical phenomena that can be modeled as a collection of particles.  For example, some typical particle systems include: waterfalls, fire, smoke, explosions, snow, and meteors.

This example demonstrates how to write a very simple particle system only.  For example, particles are only created at setup time.   See the other particle system models for elaborations on the basic particle system idea.

## HOW IT WORKS

At each iteration of the GO routine, tiny forces steer the particle through its trajectory. Particles have a velocity in the x and y axes, a step and a force accumulator. This model uses a procedure to compute the forces (COMPUTE-FORCES) and another one to apply the forces (APPLY-FORCES). Combined, these procedures continuously move the particles over time.

Below are the steps for moving each particle:

1.- Initialization  
First, the force accumulators are cleared of the previously calculated forces.

2.- Force Calculation  
Force calculation is trivial for this particular particle system since the sole force of the model, gravity, is represented by a constant negative number. However, force calculation in general can be harder when more complicated forces, such as springs, are involved.

3.- Force Summation  
After all of the individual forces are computed, the APPLY-FORCES routine sums all of them and calculates the resulting velocity of the particle.

4.- Displacement  
Finally, a new position is calculated by multiplying the velocity by STEP-SIZE and adding the displacement to the current particle location. STEP-SIZE represents the small amount of time during which the forces are applied.

Notice that in this model the particles die when they reach the world boundaries.

## HOW TO USE IT

To observe only one particle at a time:

1.- Change the PARTICLES-NUMBER and STEP-SIZE.  
2.- Press SETUP  
3.- Press GO (Observe how the turtles move through the world.)  
5.- You can optionally change the GRAVITY-CONSTANT and observe how the behavior changes.

Note that you can change the GRAVITY-CONSTANT and the STEP-SIZE while the particle is moving. For example, if the user wants the particles to fly higher he can decrease the gravity, but if he wishes the particles to stay lower he can increase the gravity pull.

## THINGS TO NOTICE

The more particles you create, the slower the model runs.

The greater the step size, the faster the model runs.  (What happens if you make the step size too large?)

## THINGS TO TRY

Change the GRAVITY-CONSTANT slider while the particle is moving. Note how it stays floating or falls quickly depending when and how you adjust the gravity constant slider.

## RELATED MODELS

Particle System Fountain  
Particle System Waterfall  
Particle System Flame

## CREDITS AND REFERENCES

Particle Systems  
http://www.siggraph.org/education/materials/HyperGraph/animation/particle.htm

Particle Systems by Allen Martin  
http://web.cs.wpi.edu/~matt/courses/cs563/talks/psys.html

William T.  Reeves, "Particle Systems - A Technique for Modeling a Class of Fuzzy Objects", Computer Graphics 17:3 pp.  359-376, 1983 (SIGGRAPH 83).  
http://portal.acm.org/citation.cfm?id=357320

Physically based modeling Online SIGGRAPH 2001 Course Notes  
http://www.pixar.com/companyinfo/research/pbm2001/

Particle Systems on Wikipedia  
http://en.wikipedia.org/wiki/Particle_system

Thanks to Daniel Kornhauser for his work on this model.
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

drop
true
0
Circle -7500403 true true 73 15 152
Polygon -7500403 true true 219 119 205 148 185 180 174 205 163 236 156 263 149 293 147 134
Polygon -7500403 true true 79 118 95 148 115 180 126 205 137 236 144 263 150 294 154 135

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

smoke
true
15
Rectangle -7500403 true false 120 120 120 135
Circle -1 true true 131 47 4
Circle -1 true true 158 210 4
Circle -1 true true 213 183 4
Circle -1 true true 204 147 4
Circle -1 true true 156 177 4
Circle -1 true true 175 130 4
Circle -1 true true 153 104 4
Circle -1 true true 108 139 4
Circle -1 true true 100 121 4
Circle -1 true true 103 88 4
Circle -1 true true 68 125 4
Circle -1 true true 240 145 4
Circle -1 true true 106 209 4
Circle -1 true true 163 194 4
Circle -1 true true 201 269 4
Circle -1 true true 114 274 4
Circle -1 true true 210 40 4
Circle -1 true true 161 142 4
Circle -1 true true 145 129 4
Circle -1 true true 193 205 4
Circle -1 true true 125 136 4
Circle -1 true true 168 237 4
Circle -1 true true 219 119 4
Circle -1 true true 127 152 4
Circle -1 true true 136 112 4
Circle -1 true true 190 127 4
Circle -1 true true 103 159 4
Circle -1 true true 164 153 4
Circle -1 true true 117 123 4
Circle -1 true true 167 103 4
Circle -1 true true 120 101 4
Circle -1 true true 143 84 4
Circle -1 true true 109 179 4
Circle -1 true true 170 177 4
Circle -1 true true 195 165 4
Circle -1 true true 58 103 4
Circle -1 true true 72 94 4
Circle -1 true true 67 148 4
Circle -1 true true 74 176 4
Circle -1 true true 50 197 4
Circle -1 true true 37 141 4
Circle -1 true true 41 106 4
Circle -1 true true 30 171 4
Circle -1 true true 262 167 4
Circle -1 true true 263 96 4
Circle -1 true true 144 232 4
Circle -1 true true 218 209 4
Circle -1 true true 153 158 4
Circle -7500403 true false 157 50 11
Circle -7500403 true false 157 50 11
Circle -7500403 true false 192 66 11
Circle -7500403 true false 78 149 11
Circle -7500403 true false 80 119 11
Circle -7500403 true false 50 227 11
Circle -7500403 true false 89 220 11
Circle -7500403 true false 146 254 11
Circle -7500403 true false 196 228 11
Circle -7500403 true false 255 193 11
Circle -7500403 true false 204 100 11
Circle -7500403 true false 257 121 11
Circle -7500403 true false 182 23 11
Circle -7500403 true false 221 58 11
Circle -7500403 true false 115 21 11
Circle -7500403 true false 73 64 11
Circle -7500403 true false 124 194 11
Circle -7500403 true false 180 114 11
Circle -7500403 true false 143 110 11
Circle -1 true true 195 137 4
Circle -1 true true 189 105 4
Circle -1 true true 143 174 4
Circle -1 true true 176 162 4
Circle -1 true true 163 165 4
Circle -1 true true 173 139 4
Circle -1 true true 184 173 4
Circle -1 true true 216 154 4
Circle -1 true true 231 173 4
Circle -1 true true 135 93 8
Circle -1 true true 86 99 8
Circle -1 true true 43 160 8
Circle -1 true true 198 116 8
Circle -1 true true 156 89 8
Circle -1 true true 104 104 8
Circle -1 true true 93 138 8
Circle -1 true true 145 184 8
Circle -1 true true 156 123 8
Circle -1 true true 177 182 8
Circle -1 true true 123 122 8
Circle -1 true true 114 157 8
Circle -1 true true 147 145 10
Circle -1 true true 167 115 8
Circle -1 true true 119 74 8
Circle -7500403 true false 178 146 10
Circle -1 true true 132 161 12
Circle -1 true true 131 136 10
Circle -1 true true 208 170 8
Circle -1 true true 168 73 8
Circle -1 true true 97 74 8
Circle -1 true true 142 67 8
Circle -1 true true 179 86 8
Circle -1 true true 194 190 8
Circle -1 true true 97 187 8
Circle -1 true true 120 216 8
Circle -1 true true 170 211 8
Circle -1 true true 218 137 8
Circle -1 true true 177 198 8
Circle -1 true true 206 194 8
Circle -1 true true 137 214 8
Circle -1 true true 180 223 8
Circle -1 true true 94 168 8

smoke2
true
0
Circle -1 true false 168 122 13
Circle -1 true false 152 152 13
Circle -1 true false 151 138 13
Circle -1 true false 136 153 13
Circle -1 true false 135 137 13
Circle -1 true false 125 167 13
Circle -1 true false 106 144 13
Circle -1 true false 119 121 13
Circle -1 true false 166 166 13
Circle -1 true false 106 125 6
Circle -1 true false 187 124 6
Circle -1 true false 109 170 6
Circle -1 true false 186 144 13
Circle -1 true false 184 171 6
Circle -1 true false 124 157 4
Circle -1 true false 124 141 4
Circle -1 true false 124 141 4
Circle -1 true false 172 140 4
Circle -1 true false 142 172 4
Circle -1 true false 156 172 4
Circle -1 true false 155 127 4
Circle -1 true false 140 125 4
Circle -1 true false 172 157 4
Circle -1 true false 172 140 4
Circle -1 true false 172 140 4
Circle -1 true false 148 236 4
Circle -1 true false 56 149 4
Circle -1 true false 232 150 4
Circle -1 true false 107 95 4
Circle -1 true false 105 205 4
Circle -1 true false 187 204 4
Circle -1 true false 186 94 4
Circle -1 true false 144 191 13
Circle -1 true false 147 100 13

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

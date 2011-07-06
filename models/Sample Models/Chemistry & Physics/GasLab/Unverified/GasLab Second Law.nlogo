globals [
  fast average slow               ;; current counts
  avg-speed avg-energy            ;; current averages
  avg-speed-init avg-energy-init  ;; initial averages
  vsplit vclock                   ;; clock variables
  left-count right-count          ;; # of particles on each side
  propeller-velocity              ;; current velocity of the propeller
  left-pressure right-pressure    ;; pressure in each chamber
  left-walls right-walls          ;; agentsets of the walls on each side (used when calculating pressure)
  propeller-angle                 ;; current angular position of the propeller
]

turtles-own [
  speed mass energy new-speed           ;; turtle info
  v1t v1l tmp-turtle                    ;; collision info (turtle 1)
  heading2 mass2 speed2 v2t v2l turtle2 ;; collision info (turtle 2)
  theta                                 ;; collision info (both particles)
]

patches-own [
  wall?       ;; is this patch part of the wall?
  pressure    ;; sum of momentums of particles that have bounced here during this time slice
]

to setup [mode]
  clear-all
  make-box
  set vclock  0
  crt number
  [ set new-speed 10.0
    set shape "circle"
    set mass 1.0
    setup-position mode
    recolor ]
  update-variables
  set avg-speed-init avg-speed
  set avg-energy-init avg-energy
  rotate-propeller
  reset-ticks
end

to setup-position [mode]  ;; turtle procedure
  if mode = "corner"
    [ setxy (- (max-pxcor - 1)) (- (max-pycor - 1))
      set heading random-float 90
      fd random-float 8 ]
  if mode = "one side"
    [ setxy (- (1 + random-float (max-pxcor - 2)))
            (random-float (world-height - 3) + min-pycor + 1) ]
  if mode = "both sides"
    [ setxy (random-float (world-width - 3) + min-pxcor + 1)
            (random-float (world-height - 3) + min-pycor + 1) ]
end

to update-variables
  ask turtles
    [ set speed new-speed
      set energy (0.5 * speed * speed * mass) ]
  set average count turtles with [color = green]
  set slow    count turtles with [color = blue]
  set fast    count turtles with [color = red]
  set avg-speed  mean [speed] of turtles
  set avg-energy mean [energy] of turtles
  set vsplit (round ((max [speed] of turtles) * 1.2))
  set left-count count turtles with [xcor < 0]
  set right-count count turtles with [xcor >= 0]
  set left-pressure sum [pressure] of left-walls
  ask left-walls [ set pressure 0 ]
  set right-pressure sum [pressure] of right-walls
  ask right-walls [ set pressure 0 ]
end

to go
  ask turtles [ bounce ]
  ask turtles [ move ]
  ask turtles [ check-for-collision ]
  set vclock (vclock + 1)
  rotate-propeller
  ifelse (vclock = vsplit)
  [
    tick
    set vclock 0
    update-variables
  ]
  [ display ]
end

to rotate-propeller
  set-current-plot "Propeller"
  plot-pen-reset
  set propeller-angle propeller-angle - propeller-velocity / 2
  plot-pen-up
  plotxy cos propeller-angle sin propeller-angle
  plot-pen-down
  plotxy 0 - cos propeller-angle 0 - sin propeller-angle
  plot-pen-up
  plotxy cos (propeller-angle + 90) sin (propeller-angle + 90)
  plot-pen-down
  plotxy 0 - cos (propeller-angle + 90) 0 - sin (propeller-angle + 90)
  ;; slow down the propeller due to friction
  set propeller-velocity propeller-velocity * 0.999
end

to bounce ;; turtle procedure
  ; if we're not about to hit a wall (yellow patch),
  ; we don't need to do any further checks
  if [pcolor] of patch-ahead 1 != yellow [ stop ]
  ; get the coordinates of the patch we'll be on if we go forward 1
  let new-px [pxcor] of patch-ahead 1
  let new-py [pycor] of patch-ahead 1
  let pressure-increment mass * speed
  ask patch new-px new-py [ set pressure pressure + pressure-increment ]
  ; check: hitting left, right, or middle wall?
  if (abs new-px = max-pxcor) or (pxcor != 0 and new-px = 0)
    ; if so, reflect heading around x axis
    [ set heading (- heading) ]
  ; check: hitting top or bottom wall?
  if (abs new-py = max-pycor) or (pxcor = 0)
    ; if so, reflect heading around y axis
    [ set heading (180 - heading) ]
end

to move  ;; turtle procedure
  let old-xcor xcor
  jump (speed / vsplit)
  if (old-xcor < 0) and (xcor >= 0)
    [ set propeller-velocity propeller-velocity + 0.03 * speed ]
  if (old-xcor > 0) and (xcor <= 0)
    [ set propeller-velocity propeller-velocity - 0.03 * speed ]
end

to check-for-collision  ;; turtle procedure
  if count other turtles-here = 1
    [ set tmp-turtle one-of other turtles-here
      if ((who > [who] of tmp-turtle) and (turtle2 != tmp-turtle))
        [ collide ] ]
end

to collide  ;; turtle procedure
  get-turtle2-info
  calculate-velocity-components
  set-new-speed-and-headings
end

to get-turtle2-info  ;; turtle procedure
  set turtle2 tmp-turtle
  set mass2 [mass] of turtle2
  set speed2 [new-speed] of turtle2
  set heading2 [heading] of turtle2
end

to calculate-velocity-components  ;; turtle procedure
  set theta (random-float 360)
  set v1l (new-speed * sin (theta - heading))
  set v1t (new-speed * cos (theta - heading))
  set v2l (speed2 * sin (theta - heading2))
  set v2t (speed2 * cos (theta - heading2))
  ;; CM vel. along dir. theta
  let vcm (((mass * v1t) + (mass2 * v2t)) / (mass + mass2))
  set v1t (vcm + vcm - v1t)
  set v2t (vcm + vcm - v2t)
end

;; set new speed and headings of each turtles that has had a collision
to set-new-speed-and-headings
  set new-speed sqrt ((v1t * v1t) + (v1l * v1l))
  set heading (theta - (atan v1l v1t))

  let new-speed2 sqrt ((v2t * v2t) + (v2l * v2l))
  let new-heading (theta - (atan v2l v2t))
  ask turtle2 [
    set new-speed new-speed2
    set heading new-heading
  ]

  recolor
  ask turtle2 [ recolor ]
end

to recolor  ;; turtle procedure
  ifelse new-speed < 5.0
    [ set color blue ]
    [ ifelse new-speed > 15.0
        [ set color red ]
        [ set color green ] ]
end

to make-box
  ask patches
    [ set pressure 0
      set wall? false
      if count neighbors != 8 or
         ((pxcor = 0) and (abs pycor > propeller-radius))
        [ set pcolor yellow
          set wall? true ]
      if (pxcor = 0) and (abs pycor <= propeller-radius)
        [ set pcolor gray ] ]
  set left-walls patches with [wall? and (pxcor < 0)]
  set right-walls patches with [wall? and (pxcor > 0)]
end


to-report calculate-order
  let x-patches-per-grid-cell (world-width / 5)
  let y-patches-per-grid-cell (world-height / 5)
  let counts-list []
  let gridx -2
  repeat 5
    [ let gridy -2
      repeat 5
        [ let gridcount count turtles with [int (pxcor / x-patches-per-grid-cell) = gridx
                                            and int (pycor / y-patches-per-grid-cell) = gridy]
          set counts-list lput gridcount counts-list
          set gridy gridy + 1 ]
      set gridx gridx + 1 ]
   ;; show counts-list
  report variance counts-list
end
@#$#@#$#@
GRAPHICS-WINDOW
254
10
548
325
35
35
4.0
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
30.0

SLIDER
7
119
249
152
number
number
10
500
100
10
1
particles
HORIZONTAL

BUTTON
133
76
249
109
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
8
41
125
74
setup (corner)
setup \"corner\"
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
152
249
185
propeller-radius
propeller-radius
0
20
17
1
1
NIL
HORIZONTAL

BUTTON
132
41
249
74
setup (one side)
setup \"one side\"
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
8
76
125
109
setup (both sides)
setup \"both sides\"
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

PLOT
7
195
249
409
Particle Counts
time
# particles
0.0
20.0
0.0
100.0
true
true
"" ""
PENS
"left" 1.0 0 -955883 true "" "plot left-count"
"right" 1.0 0 -13791810 true "" "plot right-count"

PLOT
7
409
249
623
Propeller Velocity
time
velocity
0.0
20.0
-8.0
8.0
true
false
"set-plot-y-range ceiling (number / -25) ceiling (number / 25)\nauto-plot-off\nset-current-plot-pen \"x-axis\"\nplotxy 0 0\nplotxy 1000 0\nauto-plot-on" ""
PENS
"velocity" 1.0 0 -11365376 true "" "plot propeller-velocity"
"x-axis" 1.0 0 -1184463 true "" ""

TEXTBOX
319
335
750
409
RULES:\n- Particle crossing left to right pushes propeller clockwise.\n- Particle crossing right to left pushes propeller counterclockwise.\n- Propeller slows down over time as work is extracted from the system.
11
0.0
0

PLOT
491
409
733
623
Pressures
time
pressure
0.0
20.0
0.0
8.0
true
true
"set-plot-y-range 0 ceiling (number / 25)" ""
PENS
"left" 1.0 0 -955883 true "" "plot left-pressure"
"right" 1.0 0 -13791810 true "" "plot right-pressure"

PLOT
249
409
491
623
Entropy
time
entropy
0.0
20.0
0.0
1.0
true
false
"" ""
PENS
"entropy" 1.0 0 -2674135 true "plot 100 * (1 / calculate-order)" ""

PLOT
552
41
780
244
Propeller
NIL
NIL
-1.0
1.0
-1.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

@#$#@#$#@
## WHAT IS IT?

This model is one in a series of GasLab models. They use the same basic rules for simulating the behavior of gases.  Each model integrates different features in order to highlight different aspects of gas behavior.

The basic principle of the models is that gas particles are assumed to have two elementary actions: they move and they collide --- either with other particles or with any other objects such as walls.

This model simulates the Second Law of Thermodynamics via the behavior of gas particles in a box. The Second Law of Thermodynamics states that systems tend towards increased entropy.  Essentially what this means is that over time ordered systems become less ordered unless work is done on the system to keep it ordered.

## HOW IT WORKS

Particles are modeled as perfectly elastic particles with no energy except their kinetic energy --- that which is due to their motion.  Collisions between particles are elastic.  Particles are colored according to speed -- blue for slow, green for medium, and red for high speeds.

The exact way two particles collide is as follows:  
1. Two turtles "collide" if they find themselves on the same patch.  
2. A random axis is chosen, as if they are two balls that hit each other and this axis is the line connecting their centers.  
3. They exchange momentum and energy along that axis, according to the conservation of momentum and energy.  This calculation is done in the center of mass system.  
4. Each turtle is assigned its new velocity, energy, and heading.  
5. If a turtle finds itself on or very close to a wall of the container, it "bounces" -- that is, reflects its direction and keeps its same speed.

The propeller is modeled such that it shows the effect of the flux of the particles between the two sides of the box, but does not effect or interact with the particles as they pass through.  When particles move from the left side to the right side they accelerate the propeller clockwise, and likewise, when particles move from the right side to the left side they accelerate the propeller counter-clockwise.

## HOW TO USE IT

SETUP: sets up the initial conditions and distributes the particles in one of three different modes.  Be sure to wait till the Setup button stops before pushing go.  
CORNER: all the particles are created in the lower left corner of the box and diffuse outwards from there.  
ONE SIDE: all the particles are created in the left side of the box evenly distributed.  
BOTH SIDES: all the particles are created evenly distributed throughout the entire box.  
GO: runs the code again and again.  This is a "forever" button.  
NUMBER: the number of gas particles  
PROPELLER-RADIUS: the radius of the propeller in the opening between the sides of the box.  The size of the opening is based on the size of the propeller.

### About the plots

PARTICLE COUNTS: plots the number of particles on each side of the box.  
PROPELLER VELOCITY: plots the velocity of the propeller: positive is clockwise, negative is counter-clockwise.  
PRESSURES: plots the pressure of the gas on each side of the box.  
ENTROPY: plots a measure of the entropy of the system.  As the particles become more evenly and randomly distributed the entropy will increase.

## THINGS TO NOTICE

When the particles are evenly distributed throughout the box, what do you notice about the behavior of the propeller?

In what ways is this model a correct or incorrect idealization of the real world?

In what ways can you quantify entropy?  What is the best way to quantify entropy in this model?  Does this model use this method?  If not, what is wrong with the method being used?

## THINGS TO TRY

Set all the particles in part of the world, or with the same heading -- what happens?  Does this correspond to a physical possibility?

Are there other interesting quantities to keep track of?

## EXTENDING THE MODEL

Could you find a way to measure or express the "temperature" of this imaginary gas?  Try to construct a thermometer.

What happens if there are particles of different masses?  (See GasLab Two Gas model.)

How does this 2-D model differ from the 3-D model?

If *more* than two particles arrive on the same patch, the current code says they don't collide.  Is this a mistake?  How does it affect the results?

Is this model valid for fluids in any aspect?  How could it be made to be fluid-like?

## RELATED MODELS

The GasLab suite of models, especially GasLab Maxwell's Demon, which models a theoretical system that seems to violate the Second Law of Thermodynamics.

## CREDITS AND REFERENCES

Thanks to Brent Collins and Seth Tisue for their work on this model.
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

clock
true
0
Circle -7500403 true true 30 30 240
Polygon -16777216 true false 150 31 128 75 143 75 143 150 158 150 158 75 173 75
Circle -16777216 true false 135 135 30

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
NetLogo 5.0beta4
@#$#@#$#@
setup "corner"
repeat 75 [ go ]
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

breed [frogs frog]
breed [snakes snake]
frogs-own [struggle? poison]
snakes-own [resistance]

to setup
  ca
  ask patches [ set pcolor white ]
  set-default-shape frogs "frog top"
  setup-individuals
  growth-plot
  trait-plot
  reset-ticks
end

to setup-individuals
  create-frogs initial-number-frogs
  [
    set size 1
    set poison random-normal initial-poison-mean 1
    setxy random-xcor random-ycor
    set struggle? false
    set color red
  ]

  create-snakes initial-number-snakes
  [
    set size 1
    set resistance random-normal initial-resistance-mean 1
    setxy random-xcor random-ycor
    set color blue
  ]
end

to go
  ask snakes [
    if resistance < 0 [die]
    rt random-float 50 - random-float 50
    fd 1
    if any? frogs-here
    [
      hunt-frogs
    ]
  ]

  ask frogs [
    if poison < 0 [die]
    rt random-float 50 - random-float 50
    fd 1
    set struggle? false
  ]

;; original global reproduction mechanism
;;  while [count turtles < max-population] [
;;    ifelse random 2 = 0 [
;;      ask one-of snakes [ reproduce ]
;;    ]
;;    [
;;      ask one-of frogs [ reproduce ]
;;    ]
;;  ]

;; new individual-based reproduction mechanism
  ask turtles [
    if breed = frogs and count frogs < max-population / 2 and random 100 < 5 [
      reproduce
    ]
    if breed = snakes and count snakes < max-population / 2 and random 100 < 5 [
      reproduce
    ]
  ]

  tick
  growth-plot
  trait-plot
end

to hunt-frogs
  let hunted one-of (frogs-here with [not struggle?])
  if hunted != nobody [
    ask hunted [ set struggle? true ]
    ifelse resistance > [ poison ] of hunted [
      ask hunted [ die ]
    ]
    [
      if resistance != [ poison ] of hunted [ die ]
    ]
  ]
end

to reproduce
 if breed = frogs [
   hatch 1 [
     set poison random-normal [ poison ] of myself 1
   ]
 ]
 if breed = snakes [
   hatch 1 [
     set resistance random-normal [resistance] of myself 1
   ]
 ]
end

to growth-plot
  set-current-plot "populations"
  set-current-plot-pen "frogs"
  plot count frogs
  set-current-plot-pen "snakes"
  plot count snakes
end

to trait-plot
  set-current-plot "traits"
  set-current-plot-pen "poison"
  plot average-poison
  set-current-plot-pen "resistance"
  plot average-resistance
end

to-report average-poison
  ifelse count frogs > 0
    [ report mean [ poison ] of frogs ]
    [ report 0 ]
end

to-report average-resistance
  ifelse count snakes > 0
    [ report mean [ resistance ] of snakes ]
    [ report 0 ]
end
@#$#@#$#@
GRAPHICS-WINDOW
227
10
657
461
17
17
12.0
1
10
1
1
1
0
1
1
1
-17
17
-17
17
1
1
1
ticks

BUTTON
112
120
186
172
Go
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
35
120
112
171
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

SLIDER
4
203
208
236
initial-number-frogs
initial-number-frogs
1
150
150
1
1
NIL
HORIZONTAL

SLIDER
4
236
207
269
initial-number-snakes
initial-number-snakes
1
150
150
1
1
NIL
HORIZONTAL

BUTTON
65
171
157
204
step once
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

TEXTBOX
440
477
590
510
Red = Frogs\nBlue = Snakes
11
0.0
0

MONITOR
3
418
95
463
Snakes
count snakes
3
1
11

MONITOR
3
370
95
415
Frogs
count frogs
3
1
11

MONITOR
95
370
220
415
Average Poison
average-poison
3
1
11

MONITOR
95
418
221
463
Average resistance
average-resistance\n
3
1
11

PLOT
2
468
202
611
populations
NIL
NIL
0.0
10.0
120.0
160.0
true
false
"" ""
PENS
"frogs" 1.0 0 -2674135 true "" ""
"snakes" 1.0 0 -13345367 true "" ""

PLOT
202
467
402
611
traits
NIL
NIL
0.0
10.0
10.0
10.0
true
false
"" ""
PENS
"poison" 1.0 0 -2674135 true "" ""
"resistance" 1.0 0 -13345367 true "" ""

SLIDER
4
269
207
302
initial-poison-mean
initial-poison-mean
0
50
25
1
1
NIL
HORIZONTAL

SLIDER
4
302
207
335
initial-resistance-mean
initial-resistance-mean
0
50
25
1
1
NIL
HORIZONTAL

SLIDER
4
334
207
367
max-population
max-population
0
500
500
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This model demonstrates the ideas of competitive co-evolution. In the model there are two species:  frogs and snakes. The snakes are the only predators of the frogs, but the frogs produce a fast acting poison that kills the snakes before they can be eaten. However, the snakes have developed an anti-venom to counter the frog's poison. In this model, we assume that there are no other predators of the frogs, or prey that are consumed by the snakes.  As such the two species enter a biological arms race in order to keep up with each other.

The name Red Queen comes from Lewis Carroll's "Through the Looking Glass", where the Red Queen says, "...it takes all the running you can do, to keep in the same place."  In this model under the right conditions, the two species evolve as fast as they can, but neither is able to gain an advantage over the other.

## HOW IT WORKS

When SETUP is pressed, INITIAL-NUMBER-SNAKES and INITIAL-NUMBER-FROGS are created.  Each of these have a poison or resistance drawn from a normal distribution with means of INITIAL-RESISTANCE-MEAN and INITIAL-POISON-MEAN with a standard deviation of 1.

Once GO is pressed, all organisms move with a random walk.  When a snake encounters a frog, the snake will try to eat the frog. If the frog's poison is stronger than snake's resistance the snake will die. If the snake's resistance is higher than the frog's poison the frog will die.  If the poison and resistance are equal then both individuals survive.

At the end of every tick, each animal get a chance to reproduce.  In order to reproduce the count of its species must be below half of MAX-INDIVIDUALS.  The chance of reproduction is still 5 in 100.  If the animal does reproduce, a new individual is created which has a resistance or poison drawn from a normal distribution with mean equal to the parent's value, and standard deviation of 1.

## HOW TO USE IT

First set the parameters of the model.  INITIAL-NUMBER-SNAKES and INITIAL-NUMBER-FROGS controls the initial number of each species.  INITIAL-RESISTANCE-MEAN and INITIAL-POISON-MEAN control the distributions from which the initial values for the snakes and frogs, respectively.  MAX-INDIVIDUALS controls the total carrying capacity of the environment.  Once these values are set, press SETUP and GO to watch the model run.

## THINGS TO NOTICE

With the initial settings of the model, both of the species will usually persist for a long period of time.  Both of the species persist, but their population levels change over time, what is the relationship between the populations of the frogs and snakes?  What happens to the levels of the poison and resistance during this time?

## THINGS TO TRY

Modify the INITIAL-RESISTANCE-MEAN and INITIAL-POISON-MEAN, do both species continue to persist?  What happens to the resistance and poison values?

Set the INITIAL-RESISTANCE-MEAN and INITIAL-POISON-MEAN to the same value, but change the INITIAL-NUMBER-FROGS and INITIAL-NUMBER-SNAKES, what happens to the population levels over time?  What happens to the poison and resistance values?

## EXTENDING THE MODEL

The frogs have their own shape, "frog top" but the snakes use the default turtle shape.  Create a snake shape for the snakes.

Originally, the reproduction of the individuals in this model is a global mechanism in that random individuals all over the environment are selected to reproduce, and snakes and frogs are selected equally likely.  We have changed this mechanism so that each agent gets a chance to reproduce each turn, making the reproduction rate population dependent, but there are many ways to make this change. Implement another individual-based mechanism. After this, change the model so that individuals that succeed in fights reproduce preferentially.

Currently the species in this model reproduce asexually.  Change the model so that it uses sexual reproduction.

## NETLOGO FEATURES

This model uses the "frog top" shape that was imported from the Shapes Library.

## RELATED MODELS

This model is related to the other BEAGLE models since they all examine evolution.

## CREDITS AND REFERENCES

This model is related to a model that was used as the basis for a poster published at the Genetic and Evolutionary Computation Conference.  This model uses a more individual-based reproductive mechanism, whereas the model in the paper used a global one:

"Coevolution of Predators and Prey in a Spatial Model: An Exploration of the Red Queen Effect" Jules Ottino-Loffler, William Rand, and Uri Wilensky (2007) GECCO 2007, London, UK
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

frog top
true
0
Polygon -7500403 true true 146 18 135 30 119 42 105 90 90 150 105 195 135 225 165 225 195 195 210 150 195 90 180 41 165 30 155 18
Polygon -7500403 true true 91 176 67 148 70 121 66 119 61 133 59 111 53 111 52 131 47 115 42 120 46 146 55 187 80 237 106 269 116 268 114 214 131 222
Polygon -7500403 true true 185 62 234 84 223 51 226 48 234 61 235 38 240 38 243 60 252 46 255 49 244 95 188 92
Polygon -7500403 true true 115 62 66 84 77 51 74 48 66 61 65 38 60 38 57 60 48 46 45 49 56 95 112 92
Polygon -7500403 true true 200 186 233 148 230 121 234 119 239 133 241 111 247 111 248 131 253 115 258 120 254 146 245 187 220 237 194 269 184 268 186 214 169 222
Circle -16777216 true false 157 38 18
Circle -16777216 true false 125 38 18

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
<experiments>
  <experiment name="Beta2" repetitions="10" runMetricsEveryStep="false">
    <setup>setup</setup>
    <go>go</go>
    <timeLimit steps="5000"/>
    <metric>count frogs</metric>
    <metric>count snakes</metric>
    <metric>average-poison</metric>
    <metric>average-resistance</metric>
    <enumeratedValueSet variable="initial-resistance-mean">
      <value value="25"/>
    </enumeratedValueSet>
    <steppedValueSet variable="initial-poison-mean" first="25" step="5" last="50"/>
    <enumeratedValueSet variable="initial-number-frogs">
      <value value="150"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-number-snakes">
      <value value="150"/>
    </enumeratedValueSet>
  </experiment>
  <experiment name="experiment" repetitions="10" runMetricsEveryStep="false">
    <setup>setup</setup>
    <go>go</go>
    <metric>count turtles</metric>
    <enumeratedValueSet variable="initial-number-frogs">
      <value value="150"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-resistance-mean">
      <value value="25"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-number-snakes">
      <value value="150"/>
    </enumeratedValueSet>
    <steppedValueSet variable="initial-poison-mean" first="0" step="5" last="50"/>
  </experiment>
  <experiment name="Final Counts" repetitions="10" runMetricsEveryStep="false">
    <setup>setup</setup>
    <go>go</go>
    <timeLimit steps="5000"/>
    <metric>count frogs</metric>
    <metric>count snakes</metric>
    <enumeratedValueSet variable="initial-number-frogs">
      <value value="150"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-resistance-mean">
      <value value="25"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-number-snakes">
      <value value="150"/>
    </enumeratedValueSet>
    <steppedValueSet variable="initial-poison-mean" first="25" step="5" last="50"/>
  </experiment>
</experiments>
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

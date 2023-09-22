breed [ sheep a-sheep ]
breed [ wolves wolf ]

turtles-own [ energy ]  ;; agents own energy

patches-own [ grass-amount ]  ;; patches have grass

;; this procedures sets up the model
to setup
  clear-all
  ask patches [
    ;; give grass to the patches, color it shades of green
    set grass-amount random-float 10.0
    recolor-grass ;; change the world green
  ]
  create-sheep number-of-sheep [  ;; create the initial sheep
    setxy random-xcor random-ycor
    set color white
    set shape "sheep"
    set energy 100  ;; set the initial energy to 100
  ]
  create-wolves number-of-wolves [  ;; create the initial wolves
    setxy random-xcor random-ycor
    set color brown
    set shape "wolf"
    set size 2 ;; increase their size so they are a little easier to see
    set energy 100  ;; set the initial energy to 100
  ]
  reset-ticks
end

;; make the model run
to go
  if not any? turtles [  ;; now check for any turtles, that is both wolves and sheep
    stop
  ]
  ask turtles [  ;; ask both wolves and sheep
    wiggle  ;; first turn a little bit
    move  ;; then step forward
    check-if-dead  ;; check to see if agent should die
    eat            ;; sheep eat grass, wolves eat sheep
    reproduce
  ]
  regrow-grass ;; regrow the grass
  tick
  my-update-plots  ;; plot the population counts
end

;; wolf procedure, wolves eat sheep
to eat-sheep
  if any? sheep-here [  ;; if there are sheep here then eat one
    let target one-of sheep-here
    ask target [
      die
    ]
    ;; increase the energy by the parameter setting
    set energy energy + energy-gain-from-sheep
  ]
end

;; turtle procedure (both wolves and sheep); check to see if this turtle has enough energy to reproduce
to reproduce
  if energy > 200 [
    set energy energy - 100  ;; reproduction transfers energy
    hatch 1 [ set energy 100 ] ;; to the new agent
  ]
end

;; recolor the grass to indicate how much has been eaten
to recolor-grass
;;  set pcolor scale-color green grass 0 20
set pcolor scale-color green (10 - grass-amount) -10 20
end

;; regrow the grass
to regrow-grass
  ask patches [
    set grass-amount grass-amount + grass-regrowth-rate
    if grass-amount > 10.0 [
      set grass-amount 10.0
    ]
    recolor-grass
  ]
end

to eat
  ifelse breed = sheep
  [eat-grass]
  [eat-sheep]
end

;; sheep procedure, sheep eat grass
to eat-grass
  ;; check to make sure there is grass here
  if ( grass-amount >= energy-gain-from-grass ) [
    ;; increment the sheep's energy
    set energy energy + energy-gain-from-grass
    ;; decrement the grass
    set grass-amount grass-amount - energy-gain-from-grass
    recolor-grass
  ]
end

;; turtle procedure, both wolves and sheep
to check-if-dead
 if energy < 0 [
    die
  ]
end

;; update the plots
to my-update-plots
  set-current-plot-pen "sheep"
  plot count sheep

  set-current-plot-pen "wolves"
  plot count wolves * 10 ;; scaling factor so plot looks nice

  set-current-plot-pen "grass"
  plot sum [ grass-amount ] of patches / 50 ;; scaling factor so plot looks nice
end

;; turtle procedure, the agent changes its heading
to wiggle
  ;; turn right then left, so the average is straight ahead
  rt random 90
  lt random 90
end

;; turtle procedure, the agent moves which costs it energy
to move
  forward 1
  set energy energy - movement-cost ;; reduce the energy by the cost of movement
end


; Copyright 2007 Uri Wilensky.
; See Info tab for full copyright and license.
@#$#@#$#@
GRAPHICS-WINDOW
303
10
766
474
-1
-1
13.0
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
30.0

BUTTON
35
90
101
123
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
177
90
240
123
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
0

PLOT
29
306
272
456
Population over Time
Time
Population
0.0
10.0
0.0
10.0
true
true
"" ""
PENS
"sheep" 1.0 0 -16777216 true "" ""
"grass" 1.0 0 -10899396 true "" ""
"wolves" 1.0 0 -6459832 true "" ""

SLIDER
34
8
239
41
number-of-sheep
number-of-sheep
0
1000
500.0
1
1
NIL
HORIZONTAL

SLIDER
35
130
240
163
movement-cost
movement-cost
0
2.0
0.4
0.1
1
NIL
HORIZONTAL

SLIDER
35
170
240
203
grass-regrowth-rate
grass-regrowth-rate
0
2.0
0.3
0.1
1
NIL
HORIZONTAL

SLIDER
35
210
240
243
energy-gain-from-grass
energy-gain-from-grass
0
2.0
1.7
0.1
1
NIL
HORIZONTAL

SLIDER
35
50
240
83
number-of-wolves
number-of-wolves
0
100
10.0
1
1
NIL
HORIZONTAL

SLIDER
35
250
240
283
energy-gain-from-sheep
energy-gain-from-sheep
0
10.0
3.5
0.1
1
NIL
HORIZONTAL

@#$#@#$#@
## ACKNOWLEDGMENT

This model is from Chapter Four of the book "Introduction to Agent-Based Modeling: Modeling Natural, Social and Engineered Complex Systems with NetLogo", by Uri Wilensky & William Rand.

* Wilensky, U. & Rand, W. (2015). Introduction to Agent-Based Modeling: Modeling Natural, Social and Engineered Complex Systems with NetLogo. Cambridge, MA. MIT Press.

This model is in the IABM Textbook folder of the NetLogo Models Library. The model, as well as any updates to the model, can also be found on the textbook website: http://www.intro-to-abm.com/.

## WHAT IS IT?

This is the fifth model in a set of models that build towards a predator prey model of population dynamics.  This fifth model adds wolves and completes the predator prey model.

It extends the model Wolf Sheep Simple 4.

## HOW IT WORKS

The model creates a population of sheep that wander around the landscape.  For each step the sheep take it costs them some energy and if there energy gets too low they die.  However, the sheep can eat grass in the environment to regain energy and the grass regrows over time.  If the energy of the sheep gets above a certain level then they can reproduce.

In this fifth model, there are also wolves.  Wolves have the same behaviors as sheep except for eating; rather than grass, they eat sheep.

## HOW TO USE IT

Set the NUMBER-OF-SHEEP slider and press SETUP to create the initial population. You can also change the MOVEMENT-COST slider to affect the energy cost of movement for the sheep.  The GRASS-REGROWTH-RATE slider affects how fast the grass grows back, while the ENERGY-GAIN-FROM-GRASS slider affects how much energy the sheep can gain from eating the grass, and the ENERGY-GAIN-FROM-SHEEP slider affects how much energy the wolves gain from eating sheep.

After this, press the GO button to make the sheep and wolves move around the landscape, and interact.

## THINGS TO NOTICE

How does the number of sheep affect the population levels?  How does the number of wolves affect the population levels?

Is there a spatial relationship between where the sheep do well and where the wolves do well?

How does the presence of wolves affect the system?

## THINGS TO TRY

Change the NUMBER-OF-WOLVES, while leaving the NUMBER-OF-SHEEP constant, how does this affect the model results?

How does the ENERGY-GAIN-FROM-SHEEP affect the model results?

Try to play around with ENERGY-GAIN-FROM-GRASS and GRASS-REGROWTH-RATE. Does keeping the influx of energy constant but with different slider valeus (e.g. ENERGY-GAIN-FROM-GRASS as 1 and GRASS-REGROWTH-RATE as 2, and vice versa) give the same or different results? Why might that be?

## RELATED MODELS

The Wolf Sheep Predation Model in the Biology section of the NetLogo models library.

## CREDITS AND REFERENCES

This model is a simplified version of:

* Wilensky, U. (1997).  NetLogo Wolf Sheep Predation model.  http://ccl.northwestern.edu/netlogo/models/WolfSheepPredation.  Center for Connected Learning and Computer-Based Modeling, Northwestern University, Evanston, IL.

## HOW TO CITE

This model is part of the textbook, “Introduction to Agent-Based Modeling: Modeling Natural, Social and Engineered Complex Systems with NetLogo.”

If you mention this model or the NetLogo software in a publication, we ask that you include the citations below.

For the model itself:

* Wilensky, U. (2007).  NetLogo Wolf Sheep Simple 5 model.  http://ccl.northwestern.edu/netlogo/models/WolfSheepSimple5.  Center for Connected Learning and Computer-Based Modeling, Northwestern Institute on Complex Systems, Northwestern University, Evanston, IL.

Please cite the NetLogo software as:

* Wilensky, U. (1999). NetLogo. http://ccl.northwestern.edu/netlogo/. Center for Connected Learning and Computer-Based Modeling, Northwestern University, Evanston, IL.

Please cite the textbook as:

* Wilensky, U. & Rand, W. (2015). Introduction to Agent-Based Modeling: Modeling Natural, Social and Engineered Complex Systems with NetLogo. Cambridge, MA. MIT Press.

## COPYRIGHT AND LICENSE

Copyright 2007 Uri Wilensky.

![CC BY-NC-SA 3.0](http://ccl.northwestern.edu/images/creativecommons/byncsa.png)

This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License.  To view a copy of this license, visit https://creativecommons.org/licenses/by-nc-sa/3.0/ or send a letter to Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.

Commercial licenses are also available. To inquire about commercial licenses, please contact Uri Wilensky at uri@northwestern.edu.

<!-- 2007 -->
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

link
true
0
Line -7500403 true 150 0 150 300

link direction
true
0
Line -7500403 true 150 150 30 225
Line -7500403 true 150 150 270 225

moose
false
0
Polygon -7500403 true true 196 228 198 297 180 297 178 244 166 213 136 213 106 213 79 227 73 259 50 257 49 229 38 197 26 168 26 137 46 120 101 122 147 102 181 111 217 121 256 136 294 151 286 169 256 169 241 198 211 188
Polygon -7500403 true true 74 258 87 299 63 297 49 256
Polygon -7500403 true true 25 135 15 186 10 200 23 217 25 188 35 141
Polygon -7500403 true true 270 150 253 100 231 94 213 100 208 135
Polygon -7500403 true true 225 120 204 66 207 29 185 56 178 27 171 59 150 45 165 90
Polygon -7500403 true true 225 120 249 61 241 31 265 56 272 27 280 59 300 45 285 90

moose-face
false
0
Circle -7566196 true true 101 110 95
Circle -7566196 true true 111 170 77
Polygon -7566196 true true 135 243 140 267 144 253 150 272 156 250 158 258 161 241
Circle -16777216 true false 127 222 9
Circle -16777216 true false 157 222 8
Circle -1 true false 118 143 16
Circle -1 true false 159 143 16
Polygon -7566196 true true 106 135 88 135 71 111 79 95 86 110 111 121
Polygon -7566196 true true 205 134 190 135 185 122 209 115 212 99 218 118
Polygon -7566196 true true 118 118 95 98 69 84 23 76 8 35 27 19 27 40 38 47 48 16 55 23 58 41 71 35 75 15 90 19 86 38 100 49 111 76 117 99
Polygon -7566196 true true 167 112 190 96 221 84 263 74 276 30 258 13 258 35 244 38 240 11 230 11 226 35 212 39 200 15 192 18 195 43 169 64 165 92

newwolf
false
0
Polygon -7500403 true true 20 205 26 181 45 154 54 144 70 135 80 135 98 133 132 132 128 129 161 126 178 123 191 123 212 122 225 111 226 122 224 123 234 120 247 113 243 124 258 131 261 135 281 138 276 152 254 155 246 169 235 174 219 182 198 189 194 213 194 228 196 239 204 246 190 248 187 232 184 217 185 198 183 225 190 248 193 255 182 255 174 226 173 200 135 208 117 204 101 205 80 207 77 177 80 216 67 231 52 238 54 249 61 259 65 263 55 265 45 245 54 265 46 264 38 254 34 235 39 225 46 218 46 201 41 209 35 218 24 220 21 211 21 216
Line -16777216 false 275 153 259 150
Polygon -16777216 true false 253 133 245 131 245 133

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
15
Circle -1 true true 203 65 88
Circle -1 true true 70 65 162
Circle -1 true true 150 105 120
Polygon -7500403 true false 218 120 240 165 255 165 278 120
Circle -7500403 true false 214 72 67
Rectangle -1 true true 164 223 179 298
Polygon -1 true true 45 285 30 285 30 240 15 195 45 210
Circle -1 true true 3 83 150
Rectangle -1 true true 65 221 80 296
Polygon -1 true true 195 285 210 285 210 240 240 210 195 210
Polygon -7500403 true false 276 85 285 105 302 99 294 83
Polygon -7500403 true false 219 85 210 105 193 99 201 83

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

wolf
false
0
Polygon -16777216 true false 253 133 245 131 245 133
Polygon -7500403 true true 2 194 13 197 30 191 38 193 38 205 20 226 20 257 27 265 38 266 40 260 31 253 31 230 60 206 68 198 75 209 66 228 65 243 82 261 84 268 100 267 103 261 77 239 79 231 100 207 98 196 119 201 143 202 160 195 166 210 172 213 173 238 167 251 160 248 154 265 169 264 178 247 186 240 198 260 200 271 217 271 219 262 207 258 195 230 192 198 210 184 227 164 242 144 259 145 284 151 277 141 293 140 299 134 297 127 273 119 270 105
Polygon -7500403 true true -1 195 14 180 36 166 40 153 53 140 82 131 134 133 159 126 188 115 227 108 236 102 238 98 268 86 269 92 281 87 269 103 269 113

x
false
0
Polygon -7500403 true true 270 75 225 30 30 225 75 270
Polygon -7500403 true true 30 75 75 30 270 225 225 270
@#$#@#$#@
NetLogo 6.4.0
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
<experiments>
  <experiment name="Wolf Sheep Simple model analysis" repetitions="10" runMetricsEveryStep="false">
    <setup>setup</setup>
    <go>go</go>
    <timeLimit steps="1000"/>
    <metric>count wolves</metric>
    <metric>count sheep</metric>
    <metric>sum [grass-amount] of patches</metric>
    <enumeratedValueSet variable="energy-gain-from-grass">
      <value value="2"/>
    </enumeratedValueSet>
    <steppedValueSet variable="number-of-wolves" first="5" step="1" last="15"/>
    <enumeratedValueSet variable="movement-cost">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="energy-gain-from-sheep">
      <value value="5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="number-of-sheep">
      <value value="500"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="grass-regrowth-rate">
      <value value="0.3"/>
    </enumeratedValueSet>
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
1
@#$#@#$#@

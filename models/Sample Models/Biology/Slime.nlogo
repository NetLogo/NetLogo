patches-own [chemical]

to setup
  clear-all
  crt population
  [ set color red
    set size 2  ;; easier to see
    setxy random-xcor random-ycor ]
  ask patches [ set chemical 0 ]
  reset-ticks
end

to go
  ask turtles
  [ if chemical > sniff-threshold                  ;; ignore pheromone unless there's enough here
      [ turn-toward-chemical ]
    rt random-float wiggle-angle - random-float wiggle-angle + wiggle-bias
    fd 1
    set chemical chemical + 2 ]                    ;; drop chemical onto patch
  diffuse chemical 1                               ;; diffuse chemical to neighboring patches
  ask patches
  [ set chemical chemical * 0.9                    ;; evaporate chemical
    set pcolor scale-color green chemical 0.1 3 ]  ;; update display of chemical concentration
  tick
end

to turn-toward-chemical  ;; turtle procedure
  ;; examine the patch ahead of you and two nearby patches;
  ;; turn in the direction of greatest chemical
  let ahead [chemical] of patch-ahead 1
  let myright [chemical] of patch-right-and-ahead sniff-angle 1
  let myleft [chemical] of patch-left-and-ahead sniff-angle 1
  ifelse (myright >= ahead) and (myright >= myleft)
  [ rt sniff-angle ]
  [ if myleft >= ahead
    [ lt sniff-angle ] ]
    ;; default: don't turn
end
@#$#@#$#@
GRAPHICS-WINDOW
210
10
625
446
40
40
5.0
1
10
1
1
1
0
1
1
1
-40
40
-40
40
1
1
1
ticks

SLIDER
7
35
199
68
population
population
1
1500
400
1
1
NIL
HORIZONTAL

SLIDER
6
214
197
247
sniff-threshold
sniff-threshold
0.0
5.0
1
0.1
1
NIL
HORIZONTAL

SLIDER
6
251
197
284
sniff-angle
sniff-angle
0.0
180.0
45
1.0
1
degrees
HORIZONTAL

SLIDER
6
138
197
171
wiggle-angle
wiggle-angle
0.0
45.0
40
1.0
1
degrees
HORIZONTAL

SLIDER
6
176
197
209
wiggle-bias
wiggle-bias
-40.0
40.0
0
1.0
1
degrees
HORIZONTAL

BUTTON
35
88
98
121
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
104
88
168
121
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

@#$#@#$#@
## WHAT IS IT?

This project is inspired by the aggregation behavior of slime-mold cells. It shows how creatures can aggregate into clusters without the control of a "leader."

In this example, each turtle drops a chemical pheromone (shown in green). The turtles also "sniff" ahead, trying to follow the gradient of other turtles' chemicals. Meanwhile, the patches diffuse and evaporate the pheromone. Following these simple, decentralized rules, the turtles aggregate into clusters.

## HOW TO USE IT

Click the SETUP button to set up a collection of slime-mold cells. Click the GO button to start the simulation.

The POPULATION slider controls the number of slime mold cells in the simulation. Changes in the POPULATION slider do not have any effect until the next SETUP command.

The other sliders affect the way turtles move. Changes to them will immediately affect the model run.

SNIFF-THRESHHOLD -- The minimum amount of chemical that must be present in a turtle's patch before the turtle will look for a chemical gradient to follow. This parameter causes the turtles to aggregate only when there are enough other cells nearby. The default value is 1.0.

SNIFF-ANGLE -- The amount, in degrees, that a turtle turns to the left and right to check for greater chemical concentrations. The default value is 45.

WIGGLE-ANGLE -- The maximum amount, in degrees, that a turtle will turn left or right in its random movements. When WIGGLE-ANGLE is set to zero, the turtle will remain at the same heading until it finds a chemical gradient to follow. The default value is 40.

WIGGLE-BIAS -- The bias of a turtle's average wiggle. When WIGGLE-BIAS = 0, the turtle's average movement is straight ahead. When WIGGLE-BIAS > 0, the turtle will tend to move more right than left. When BIAS < 0, the turtle will tend to move more left than right. The default value is 0.

There are several other critical parameters in the model that are not accessible by sliders. They can be changed by modifying the code in the procedures window.  They are:
- the evaporation rate of the chemical -- set to 0.9
- the diffusion rate of the chemical -- set to 1
- the amount of chemical deposited at each step -- set to 2

## THINGS TO NOTICE

With 100 turtles, not much happens. The turtles wander around dropping chemical, but the chemical evaporates and diffuses too quickly for the turtles to aggregate.

With 400 turtles, the result is quite different. When a few turtles happen (by chance) to wander near one another, they create a small "puddle" of chemical that can attract any number of other turtles in the vicinity. The puddle then becomes larger and more attractive as more turtles enter it and deposit their own chemicals. This process is a good example of positive feedback: the more turtles, the larger the puddle; and the larger the puddle, the more likely it is to attract more turtles.

## THINGS TO TRY

Try different values for the SNIFF-THRESHOLD, SNIFF-ANGLE, WIGGLE-ANGLE, and WIGGLE-BIAS sliders. How do they affect the turtles' movement and the formation of clumps?

Change the SNIFF-ANGLE and WIGGLE-ANGLE sliders after some clumps have formed. What happens to the clumps? Try the same with SNIFF-THRESHOLD and WIGGLE-BIAS.

## EXTENDING THE MODEL

Modify the program so that the turtles aggregate into a single large cluster.

How do the results change if there is more (or less) randomness in the turtles' motion?

Notice that the turtles only sniff for chemical in three places: forward, SNIFF-ANGLE to the left, and SNIFF-ANGLE to the right. Modify the model so that the turtles sniff all around. How does their clustering behavior change? Modify the model so that the turtles sniff in even fewer places. How does their clustering behavior change?

What "critical number" of turtles is needed for the clusters to form? How does the critical number change if you modify the evaporation or diffusion rate?

Can you find an algorithm that will let you plot the number of distinct clusters over time?

## NETLOGO FEATURES

Note the use of the `patch-ahead`, `patch-left-and-ahead`, and `patch-right-and-ahead` primitives to do the "sniffing".

## RELATED MODELS

Ants uses a similar idea of creatures that both drop chemical and follow the gradient of the chemical.

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

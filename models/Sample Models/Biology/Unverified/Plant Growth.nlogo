turtles-own
[
  water     ;; Amount of stored water
  sugar     ;; Amount of stored sugar
  adjacent  ;; Holds the identity of the adjacent turtles when nutrients are being shared
]

patches-own
[
  moisture  ;; Amount of water in the soil
  light     ;; Amount of light available for the leaves to turn into sugar
]

to setup-patches
  clear-all
  ask patches
  [
    ifelse pycor > 0
    [ ;; Allocate Light
      ifelse (random (world-width ^ 2)) < (nutrient-density * world-width)
      [ set light random nutrient-concentration ]
      [ set light 0 ]
    ]
    [ ;; Allocate Moisture
      ifelse (random (world-width ^ 2)) < (nutrient-density * world-width)
      [ set moisture random nutrient-concentration ]
      [ set moisture 0 ]
    ]
  ]
  diffuse-light
  diffuse-moisture
  ask patches
  [
    ifelse pycor > 0
    [
      set pcolor scale-color yellow light 14 -1
      set moisture 0  ;; No moisture in the light area
    ]
    [
      set pcolor scale-color blue moisture 14 -1
      set light 0  ;; No light in the moisture area
    ]
    ;; draw the ground
    if pycor = 0 and abs pxcor > 2
    [ set pcolor gray ]
  ]
  reset-ticks
end

to diffuse-light
  diffuse light 0.1
  if max [light] of patches > 15
  [ diffuse-light ]
end

to diffuse-moisture
  diffuse moisture 0.1
  if max [moisture] of patches > 15
  [ diffuse-moisture ]
end


to setup-plant
  set-default-shape turtles "circle"
  ;; Kill the old Plant
  ask turtles [ die ]
  ;; Create the new Plant
  crt 1
  [
    set color brown
    set sugar 5000
    set water 5000
    set heading 0
    hatch 1
    [
      set color green fd 1
    ]
  ]
end

to grow
  ask turtles [
  ;; Get Nutrients from environment
  ifelse color = green
  [ set sugar sugar + light ]
  [ set water water + moisture ]
  ;; Grow Plant
  if random 100 < 1
  [
    hatch 1
    [
      move
      ;; Five Conditions under which the new growth should be aborted
      if sum [count turtles-here] of neighbors >= 3 [ die ]  ;; Overcrowding
      if any? other turtles-here [ die ]  ;; Overlapping
      if color = green and pycor < 1 [ die ]  ;; Leaves Underground
      if color = brown and pycor > 0 [ die ]  ;; Roots Aboveground
      if pcolor = gray [ die ]  ;; In the ground
      set sugar 1
      set water 1
    ]
  ]
  share-with-gs
  ;; Use Resources
  set sugar sugar - 0.1
  set water water - 0.1
  if sugar <= 0 or water <= 0 [ die ]
  ]
  tick
end

to move
  ifelse cactus?
  [ ;; Plant grows up and down only
    set heading 180 * random 2
    rt 30 - 30 * random 3
  ]
  [ ;; Plant grows in all directions
    rt random-float 360
  ]
  ;; if this is the edge of the world obviously don't grow there.
  ifelse can-move? 1
  [ fd 1 ]
  [ die ]
end

to share-with-gs
  set adjacent nobody
  if any? turtles-at 1 1
  [ set adjacent one-of turtles-at 1 1
    share-up
  ]
  if any? turtles-at 0 1
  [ set adjacent one-of turtles-at 0 1
    share-up
  ]
  if any? turtles-at -1 1
  [ set adjacent one-of turtles-at -1 1
    share-up
  ]
  if any? turtles-at 1 0
  [ set adjacent one-of turtles-at 1 0
    share-side
  ]
  if any? turtles-at 1 -1
  [ set adjacent one-of turtles-at 1 -1
    share-down
  ]
  if any? turtles-at 0 -1
  [ set adjacent one-of turtles-at 0 -1
    share-down
  ]
  if any? turtles-at -1 -1
  [ set adjacent one-of turtles-at -1 -1
    share-down
  ]
end

to share-up
  let old-water water
  set water 0.95 * water + 0.02 * [water] of adjacent
  ask adjacent [ set water 0.98 * water + 0.02 * old-water ]
end

;; Nutrients are shared equally, but the sharing is executed by the left turtle
to share-side
  let old-water water
  set water 0.95 * water + 0.05 * [water] of adjacent
  ask adjacent [ set water 0.95 * water + 0.05 * old-water ]
  let old-sugar sugar
  set sugar 0.95 * sugar + 0.05 * [sugar] of adjacent
  ask adjacent [ set sugar 0.95 * sugar + 0.05 * old-sugar ]
end

to share-down
  let old-sugar sugar
  set sugar 0.95 * sugar + 0.02 * [sugar] of adjacent
  ask adjacent [ set sugar 0.98 * sugar + 0.05 * old-sugar ]
end
@#$#@#$#@
GRAPHICS-WINDOW
219
10
553
525
40
60
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
-40
40
-60
60
1
1
1
ticks
30

BUTTON
17
38
140
71
NIL
setup-patches
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
17
81
140
114
NIL
setup-plant
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
17
124
140
157
NIL
grow
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
17
170
207
203
nutrient-density
nutrient-density
1
3
2
1
1
NIL
HORIZONTAL

SLIDER
17
213
207
246
nutrient-concentration
nutrient-concentration
20
100
49
1
1
NIL
HORIZONTAL

SWITCH
17
256
121
289
cactus?
cactus?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

Plants have the interesting tendency to "find" resources in their environment. It is not uncommon to see plants whose leaves and stalks have bent over time in the direction of nearby sunlight, or plants that have grown long roots directed to a nearby source of moisture. It almost seems as if these plants are actually scanning the environment around them to find stable sources of nutrients. Since plants do not have eyes, we might ask how they are able to accomplish this.

This model addresses the question of how a plant is able to effectively locate resources in its environment, generally focusing growth in 'promising' areas. It is not intended to be biologically realistic.

## HOW IT WORKS

The plant is composed of two kinds of cells --- light collecting (leaves), and water collecting (roots). The plant germinates with only one leaf cell and one root cell, and will grow itself by adding new cells as long as it has the nutrients to sustain itself. Nutrients (sunlight and moisture) are concentrated in randomly-determined areas of the environment, and are collected wherever a root or leaf of the plant is located. These nutrients are then circulated around the plant as 'sugar' and 'water', when, in each turn, cells exchange resources with adjacent cells. This circulation is critical, since leaf cells do not collect any water and root cells do not produce sugar, and yet both root and leaf cells each need both water and sugar --- a cell dies if it runs out of either resource. All cells of the plant use up a fixed amount of both sugar and water every turn.

The main problem that confronts the plant is that in order to explore the environment it needs to grow outwards in many directions, but the roots/leaves that result from this may be bad investments. That is, they take up nutrients but do not contribute any. What rules can we introduce so that the plant will focus growth mainly in sunny or watery areas?

The strategy employed here is to allow sugar to propagate down to the roots more effectively than it propagates up to other leaves, and to allow water to propagate up to the leaves more effectively than it propagates down to other roots. That is, we have intentionally privileged the traffic of nutrients both in specific directions and according to the nature of the nutrient. This tends to isolate subsections of the plant that fail to collect adequate nutrients.

## HOW TO USE IT

First click the SETUP-PATCHES button to allocate moisture and light and to setup the environment. You may want to click it again if you are not satisfied with the allocation of these nutrients. To adjust how these nutrients are distributed among the patches 1) use the NUTRIENT-DENSITY slider to determine the density of loci of light/moisture, 2) the NUTRIENT-CONCENTRATION slider to determine the concentration of light/moisture at each locus.

Second, click the SETUP-PLANT button to create a "seed". This can be clicked at any time to create a new plant so that it is possible to test multiple plants over the same environment.

Finally, click the GO button to watch the seed develop.

Very often, due to a lack of nutrients in the immediate environment of the seed, a new plant will fail. The plant (seed) begins with enough reserve nutrients to explore some of the area around it, but it will quickly die if it does not otherwise locate adequate nutrients in the environment. In this case, try creating a new plant to see if perhaps another plant will take hold (because of the use of a random function in the model, no two plants fare the same - even in an identical environment). If this does not work, then try resetting the environment, or even try increasing the concentration or density of nutrients in the environment.

Also, for variety, the CACTUS? switch controls whether the plant will grow only up and down instead of in all directions.

## THINGS TO NOTICE

Observe the location of nutrients within the environment before running a plant. The colors in the environment are scaled to reveal where sources of nutrients are. Squares of yellow with a dark center indicate sunny areas, squares of blue with a dark center indicate watery areas.

How large do you expect a plant to grow (if at all) with the given setup?

Are plants more likely to grow (i.e. not die) in cactus mode or bush mode?

What happens when a cell in the middle of a branch, formerly connecting other cells to the rest of the plant, dies? Why does this happen?

## THINGS TO TRY

Try growing different plants with the same patch setup (nutrient allocation). Can you generalize about the growth of a plant in a given environment?

Try growing a plant in different environments, and with different CACTUS? settings. Do you notice any limits to how large the plant can grow?

## EXTENDING THE MODEL

Sunlight and water are presented in this model as dots along a flat landscape. Real sunlight beams down from above though, and real water is generally present in a continuous gradient beneath the ground. Come up with an alternative scheme for representing sunlight and water in this model.

In order to be able to explore a larger range of ecologies, it may be useful to add interface features (sliders) and code that allows a separate setup of sun and water resources.

Currently, water resources are not depleted -- they are not even replenished, they are simply held at constant values.  You can try making the model more realistic by addressing this issue.

Improve the growth rules used in this model. A simple way to explore this would be to try and improve upon the parameters in the procedures `share-up`, `share-down`, and `share-side` (which are fixed). For this, it may be useful to set these parameters as values of sliders on the Interface.  A more in depth way would be to come up with an entirely new set of sharing or growth rules, or a different strategy altogether.

This model explores rules that will cause an artificial plant to grow in an "efficient" manner. Efficiency can be roughly defined here as the number of leaves in the plant that are "good investments" for the plant as opposed to those that only use up resources but do not contribute any. An alternative approach is to equate efficiency with the total amount of water and sunlight collected by a plant in a given environment. Think of a quantifiable measure ( a "metric") of efficiency for a plant and add this measure to the model. Now, use this measure in order to improve upon the growth rules of the plant? Does any one set of such rules work better than all others for all tested environments?

The settings in this model allow plants to grow in two varieties (cactus and bush) by varying the rules for where a new cell can be located relative to its parent cell. Can you come up with rules that will yield alternative shapes for the plant (i.e. palm tree, ivy...)?

## NETLOGO FEATURES

Note the use of the `diffuse` primitive to spread out the water and sunlight.

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
set nutrient-density 3
setup-patches
setup-plant
repeat 5000 [ grow ]
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

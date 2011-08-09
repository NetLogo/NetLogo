patches-own [new-activity activity]

to setup
  clear-all
  ask patches [ set activity random-float 1.0
                recolor ]
  set-plot-x-range 0 count patches
  reset-ticks
  update-plot
end

to recolor  ;; patch procedure
  set pcolor 9.9 * activity
end

to go
  ifelse moore-neighborhood?
    [ ask patches [ set new-activity ((activity + sum [activity] of neighbors) / 9) ^ alpha ] ]
    [ ask patches [ set new-activity activity ^ alpha ] ]
  ask patches [ set activity new-activity ]
  let scaling-factor max [activity] of patches
  ask patches [ set activity activity / scaling-factor ]
  ask patches [ recolor ]
  update-plot
  tick
end

to update-plot
  plot-pen-reset
  foreach reverse sort [activity] of patches
    [ plot ? ]
end
@#$#@#$#@
GRAPHICS-WINDOW
266
10
696
461
10
10
20.0
1
10
1
1
1
0
1
1
1
-10
10
-10
10
1
1
1
ticks

BUTTON
25
37
91
70
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
178
37
241
70
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
51
81
223
114
alpha
alpha
0
2.0
1.1
0.01
1
NIL
HORIZONTAL

PLOT
39
175
239
325
Activity distribution
NIL
NIL
0.0
10.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

BUTTON
94
37
175
70
go once
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

SWITCH
36
127
234
160
moore-neighborhood?
moore-neighborhood?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

This model demonstrates the effect of "positive feedback".  In particular, it is an implementation of the model described in the book "Cities and Complexity" by Michael Batty, on pages 38-42.  For analysis and discussion beyond that provided with this model, the reader is encouraged to refer to this text.

Positive feedback is, colloquially speaking, a situation where the rich get richer and the poor get poorer.  More technically, positive feedback describes the situation where the rate of growth of a quantity is positively correlated with the magnitude of that quantity.  With positive feedback, growth leads to size which leads to increased rate of growth.  Positive feedback is sometimes called "increasing returns to scale".  There can also be "decreasing returns to scale".  In this case, all quantities are reduced over time until they eventually equalize - which is beneficial for the poor and harmful for the rich.  With "constant returns", there is no growth or decay, so the ratios between rich and poor players stay fixed.  This model lets the user explore the realms of increasing, constant, and decreasing returns.

## HOW IT WORKS

In this model, we have a 21 by 21 grid of squares, each of which is assigned an initial random "activity" value.  These squares could represent cities, and the activity value could express populations.  Or the squares could represent corporations, with the activity values being market share or profits.  This model is merely demonstrating the basic mathematical concept, so the particular analogy to real-world phenomena is not important.

The initial activity values being chosen randomly are suggestive of historical accident.  Perhaps two railroad lines happened to meet here, or gold was found in nearby hills, so a large city sprang up.  The question this model answers is this: starting with a random distribution of activity levels, what happens to the distribution over time, as a result of the following growth process.

If MOORE-NEIGHBORHOOD? is switched off, these are the rules:

In each time step (or tick), the grid square takes its current activity value to the power of ALPHA (adjustable by a slider), and sets its activity equal to that result.  Basically each square is experiencing exponential growth (or decay, if ALPHA < 1).  For instance, if ALPHA=2, then the activity value in each location is squared.  In the next time step, each activity value is squared again.  When ALPHA > 1, the situation is one of positive returns.  A location that has a high activity value to begin with has the greatest advantage and will eventually overshadow all the others.  When ALPHA = 1, it is a "constant returns" situation, and when ALPHA < 1, it is "decreasing returns".

If MOORE-NEIGHBORHOOD? is switched on, then the rules are only slightly different:

Instead of taking the current activity value to the power ALPHA, each location first averages its own activity value with the activity values of the eight neighboring locations, then takes the resulting average to the power ALPHA.

After each tick, all the activity values are normalized to lie between 0 and 1, with the maximum activity value being scaled to 1.

## HOW TO USE IT

Press the SETUP button to initialize the grid with random activity values.

Press the GO button to run the model.  You will see the view quickly converges to a steady state.

Press the "GO ONCE" button to move forward one tick in model time.  This is particularly useful because the model runs very quickly, so the user may miss seeing the process if they just push GO.

The MOORE-NEIGHBORHOOD? switch determines whether grid locations use the average of their neighbors' activity values (ON) or just use their own activity values (OFF), to be raised to the ALPHA power each tick.

The ACTIVITY DISTRIBUTION plot shows the distribution of the activity levels in each of the 441 cells, normalized such that the maximum activity level equals 1.

## THINGS TO NOTICE

If MOORE-NEIGHBORHOOD? is switched ON and ALPHA is barely in the positive returns regime (e.g. ALPHA = 1.02), then the whole screen turns white, rather than having a white spot in the midst of black (which occurs with high values of ALPHA).  Why is this?

## THINGS TO TRY

Turn MOORE-NEIGHBORHOOD? to ON.  Compare the visual results when you use ALPHA = 1.1 and ALPHA = 2.  Both of these are in the increasing returns regime, but the ALPHA = 2 shows a much more concentrated white spot.  Can you explain this?

## EXTENDING THE MODEL

Change the mathematical update function so that some constant amount (determined by a slider) is added to the activity level of each patch each time.  Would this be more beneficial for those who are initially poor, or those who are initially rich?  Or would it matter at all?

## NETLOGO FEATURES

To color the patches appropriately, the activity value (which has been normalized to lie between 0 and 1) is simply multiplied by 9.9.  This is because in the NetLogo default color scheme, 0.0 is black and 9.9 is white.  See the colors documentation in the NetLogo help for more information about how the color scheme works.

## RELATED MODELS

This model is related to all of the other models in the "Urban Suite".  In particular, it is demonstrating the same concept as the "Urban Suite - Path Dependence" model, although the focus is slightly different.

## CREDITS AND REFERENCES

This model is based on pages 38-42 of the book "Cities and Complexity" by Michael Batty.

Thanks to Seth Tisue for his work on this model.

The Urban Suite models were developed as part of the Procedural Modeling of Cities project, under the sponsorship of NSF ITR award 0326542, Electronic Arts & Maxis.

Please see the project web site (http://ccl.northwestern.edu/cities/ ) for more information.
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
setup repeat 20 [ go ]
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

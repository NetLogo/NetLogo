globals [results]  ;; accumulates values from repeated runs; these are histogrammed
patches-own [my-color]
breed [frames frame]  ;; frames are black frames around each patch; they help count the colored patches

to setup
  clear-all
  set results []
  ask patches [ set pcolor white ]
  setup-frames
  reset-ticks
end

to setup-frames ;; each individual patch in the block is framed, creating an overall effect
                 ;; of a grid that helps distinguish individual patches in the block
  set-default-shape frames "frame"
  ask patches [
    sprout 1 [
      set breed frames
      set color black
    ]
  ]
end

to go
  ask patches [ set pcolor gray - 2 ]
  ask patches [
    set pcolor one-of [green blue]
    if one-by-one-choices? [ display wait 0.1 ]
  ]
  tick
  let result count patches with [pcolor = green]
  set results fput result results
  histogram results
  let maxbar modes results
  let maxrange length ( filter [ ? = item 0 maxbar ] results )
  set-plot-y-range 0 max list 100 maxrange
  if one-by-one-choices? [ wait 0.5 ]
end
@#$#@#$#@
GRAPHICS-WINDOW
16
10
326
341
1
1
100.0
1
10
1
1
1
0
1
1
1
-1
1
-1
1
1
1
1
ticks
30.0

BUTTON
51
409
114
456
Setup
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
121
409
184
456
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

MONITOR
39
349
132
394
# target color
count patches with [pcolor = green]
3
1
11

PLOT
337
10
569
595
Combinations
targets
how many times
0.0
10.0
0.0
100.0
true
false
"" ""
PENS
"target-color" 1.0 1 -16777216 true "" ""

MONITOR
191
349
301
394
how many trials
length results
3
1
11

SWITCH
70
476
248
509
one-by-one-choices?
one-by-one-choices?
0
1
-1000

BUTTON
191
409
276
456
Go Once
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

@#$#@#$#@
## WHAT IS IT?

9-Blocks accompanies classroom work on the Combinations Tower, the giant bell-shaped histogram of all the 512 different green/blue combinations of the 3-by-3 array.  Whereas building the Combinations Tower is a form of theoretical probability --- combinatorial analysis --- the 9-Block model complements with empirical probability of the same 3-by-3 object.  In the plot window, a tall histogram grows that has the same shape as the Combinations Tower.  How can that be?  That is the theme question of this model.

To better see the resemblance between the Combinations Tower and 9-Blocks histogram, you can either open another NetLogo window in the 9-Block Stalagmite model or open a JPEG of the 9-Block Stalagmite model as it looks when the entire sample space has been found.  (This JPEG will be available with ProbLab curricular material.)  Place this JPEG alongside the 9-Blocks histogram, to its right, editing the .jpg's dimensions as necessary to maximize the resemblance between the histogram and the .jpg.

The 9-Blocks model is a simplified version of the Stochastic Patchwork model.  Here, your green/blue combinations are always of size 3 by 3.  Also, the probability of a patch being either green or blue is always .5 in this model.  Finally, the plot shows the number of green squares and not the percentage of green squares out of all the squares.

This model is a part of the ProbLab curriculum.  The ProbLab Curriculum is currently under development at the CCL.  For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

At every iteration through Go, each patch "flips a coin" to decide whether it should be green or blue.  This "coin" works as follows: The patch chooses randomly between "0" and "1."  If it got "1," it becomes green on this run, but if it got "0," it becomes blue on this run.  At every run through Go, the number of green patches in the block is counted up.  This number is added to a list that grows in length from run to run.  The entire list is plotted as a histogram at each run.  The columns or bars in this histogram represent how many 9-blocks have occurred with 0 green squares, 1 green square, 2 green squares, ..., 8 green squares, and 9 green squares (for a total of 10 possible columns).

Over many runs, the histogram begins to look bell-shaped, just like the Combinations Tower that you may have built in your classroom and just like the tower in the 9-Block Stalagmite model.

## HOW TO USE IT

Buttons:  
'Setup; - initializes the variables and erases the plot.  
'Go' - activates the procedures just once.  So you will get a single 9-block and a short column in the histogram.  
'Go'  - activates the procedures.  It is set to work "forever," that is, it will repeat until you press it again.

Switches:  
'one-by-one-choices?' - when On, each square will settle on its color at a different moment. Also, there will be a pause between 9-blocks, as though the lights were switched off for a moment. This is meant to remind us that even though we are looking at 9-blocks, actually each square chooses its color independently of other squares. Notice how the monitor '# target color' updates per each target color that is added.

Monitors:  
'# target color' - shows how many patches are green.  
'how many trials' - shows how many times the model has chosen random 9-blocks in this experiment (so it's also showing how many items we have in the list that is being plotted every run).

## THINGS TO NOTICE

As you run this model, the histogram grows.  Pretty soon, the central columns grow taller than other columns.  This shows us that there is a higher chance of getting 9-blocks that have 4 or 5 green squares as compared to 9-blocks that have 3 or 6 green squares.  Likewise, there is a higher chance of getting 9-blocks that have 3 or 6 green squares as compared to 9-blocks that have 2 or 7 green squares.  Also, there is a higher chance of getting 9-blocks that have 2 or 7 green squares as compared to 9-blocks that have 1 or 8 green squares.  Finally, there is a higher chance of getting 9-blocks that have 1 or 8 green squares as compared to 9-blocks that have 0 or 9 green squares.

## THINGS TO TRY

Compare between the histogram you are getting and the Combinations Tower.  Why is it that they are the same shape?

How many 9-blocks do you need to sample before the histogram begins to look like the Combinations Tower?

## NETLOGO FEATURES

Look at the histogram as it grows.  What happens when it reaches the top?  More and more combinations are coming but there is no room to count them.  Instead of leaping out of the box, the number at the top-left corner of the plot --- the value of the y-axis --- updates and the histogram is redrawn for a larger scale. This helps us attend to whether and how the shape of the histogram changes after the number of combinations exceeds the range of values on the original histogram.

## EXTENDING THE MODEL

In many ways, the Stochastic Patchwork model extends this model.  However, there are other ways of extending this model.  For instance, you may want to plot different aspects of the probabilistic experiment, to answer such questions as:  
- how often do we get the same combinations twice one after the other?  
- are there particular combinations you like?  You could add code to see how long it takes the model to find these combinations (as in the Random Combinations and Permutations model).

## CREDITS AND REFERENCES

This model is a part of the ProbLab curriculum. The ProbLab Curriculum is currently under development at Northwestern's Center for Connected Learning and Computer-Based Modeling. . For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.
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

frame
false
0
Rectangle -7500403 true true -8 2 18 298
Rectangle -7500403 true true 1 0 300 16
Rectangle -7500403 true true 283 2 299 300
Rectangle -7500403 true true 1 285 300 299

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
set one-by-one-choices? false
setup
go
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

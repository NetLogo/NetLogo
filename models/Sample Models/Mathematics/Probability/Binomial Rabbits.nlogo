;; The procedures for this model have been kept largely intact from
;; the original code written by the student. With advances in the
;; language, this code is no longer at all an optimal way of writing
;; this model. We have kept the original code for research purposes --
;; please do not use it as an example of good NetLogo coding.

turtles-own [ random-no total-ratio step x-zoom x-ave ]
patches-own [ turtle-quant turtle-bottom ]

globals  [
  x-ave-glob
  x-scale kx
  y-scale ky
  y-scale-distr ky-distr
  step1 step2 step3 step4 step5 steps
  ratio1 ratio2 ratio3 ratio4 ratio5 ratios total-ratios
]

;; x-zoom: intermediate variable for zooming
;; x-ave: intermediate variable for average line
;; x-ave-glob: intermediate variable for average line
;; kx: x axis scaling factor = 2 ^ x_scale
;; ky: normalization factor for the original line
;; ky-distr: normalization factor for the distribution line
;; step1 to step5: step sizes
;; ratio1 to ratio5: probability ratio corresponding to steps
;; turtle-quant: number of turtles on each patch
;; turtle-bottom: number of turtles on each patch of the bottom line


to setup
  clear-all
  set steps sentence hop-1 list hop-2 hop-3
  set ratios sentence ratio-1 list ratio-2 ratio-3
  crt number [
    set x-zoom  0
    set x-ave  0
    setxy 0  0
    set color red
    set shape "rabbit"
    set heading  90
  ]
  set kx  x-scaling

;; below is the y-scale factor to ensure that the height of the original line and
;; average line are always about the same height, 90% of the world-height.
  set ky  (number / (world-height - 1)) / 0.9
  set y-scale-distr  (world-width - 1) * ky
  set y-scale  0
  set x-scale  (world-width - 1) * kx
  set ky-distr  0
  ask patches [ plot-green ]
  define-steps
  define-ratios
  reset-ticks
end

to go-one-hop
  hopping
  plot-violet
  plot-yellow
  tick
end

to go
   if hops = ticks [ stop ]
   go-one-hop
end

to hopping
  set kx  x-scaling
  set x-scale  (world-width - 1) * kx
  ask turtles  [
    ht
    set-step
    set x-zoom  (x-zoom + step)
    set x-ave  (x-zoom / ky)
    setxy (x-zoom / kx)  ycor
    st
  ]
end

to set-step  ;; turtle procedure
  set total-ratio  (ratio1 + ratio2 + ratio3 + ratio4 + ratio5)
  set random-no  (random-float total-ratio)
  if random-no <= ratio1
    [ set step step1 ]
  if (random-no > ratio1) and (random-no <= ratio1 + ratio2)
    [ set step step2 ]
  if (random-no > ratio1 + ratio2) and (random-no <= ratio1 + ratio2 + ratio3)
    [ set step step3 ]
  if (random-no > ratio1 + ratio2 + ratio3)
     and (random-no <= ratio1 + ratio2 + ratio3 + ratio4)
    [ set step step4 ]
  if random-no > ratio1 + ratio2 + ratio3 + ratio4
    [ set step step5 ]
end

to plot-yellow
  ask patches  [
    unplot-yellow
    set turtle-quant (count turtles-here)
  ]

  let most-turtles  max [ turtle-quant ] of patches
  ask patches [
    set ky-distr  (most-turtles / ky / int (.80 * world-height))
    set y-scale  (100 * ky-distr * ky)
    set turtle-bottom  [turtle-quant] of patch pxcor 0
    if pycor < turtle-bottom / (ky * ky-distr)
      [ set pcolor yellow ]
  ]
end

to plot-violet
  ask patches [ unplot-violet ]
  set x-ave-glob (((sum [ x-ave ] of turtles) * ky) / number)
  ask patches [
   if   ((pxcor = round  (x-ave-glob / kx))
         or  (pxcor = (round  ((x-ave-glob / kx) - world-width)))
         or  (pxcor = (round  ((x-ave-glob / kx) - (2 * world-width)))))
        and  (pycor <= (number / ky) )
       [ set pcolor  violet ]
  ]
end

to plot-green   ;; patch procedure
  if (pxcor = 0) and (pycor <= (number / ky))
    [ set pcolor green ]
end

to define-steps
  if not empty? steps  [
    set step1 first steps
    set steps butfirst steps
  ]
  if not empty? steps  [
    set step2 first steps
    set steps butfirst steps
  ]
  if not empty? steps  [
     set step3 first steps
    set steps butfirst steps
  ]
  if not empty? steps  [
    set step4 first steps
    set steps butfirst steps
  ]
  if not empty? steps  [
    set step5 first steps
    set steps butfirst steps
  ]
end

to define-ratios
  set total-ratios 0
  if not empty? ratios  [
    set ratio1 first ratios
    set ratios butfirst ratios
  ]
  if not empty? ratios  [
    set ratio2 first ratios
    set ratios butfirst ratios
  ]
  if not empty? ratios  [
    set ratio3 first ratios
    set ratios butfirst ratios
  ]
  if not empty? ratios  [
    set ratio4 first ratios
    set ratios butfirst ratios
  ]
  if not empty? ratios  [
    set ratio5 first ratios
    set ratios butfirst ratios
  ]
end

to unplot-yellow   ;; patch procedure
  if pcolor = yellow
    [ set pcolor black ]
end

to unplot-violet   ;; patch procedure
  if pcolor = violet
    [ set pcolor black ]
  plot-green
end
@#$#@#$#@
GRAPHICS-WINDOW
282
10
697
446
22
-1
9.0
1
10
1
1
1
0
1
0
1
-22
22
0
44
1
1
1
ticks
5

BUTTON
193
111
281
144
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
193
42
281
76
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
193
76
281
111
go-one-hop
go-one-hop
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
3
42
95
75
hop-1
hop-1
-5
5
1
1
1
NIL
HORIZONTAL

SLIDER
3
80
95
113
hop-2
hop-2
-5
5
-1
1
1
NIL
HORIZONTAL

SLIDER
3
119
95
152
hop-3
hop-3
-5
5
0
1
1
NIL
HORIZONTAL

SLIDER
95
42
187
75
ratio-1
ratio-1
0
10
1
1
1
NIL
HORIZONTAL

SLIDER
95
80
187
113
ratio-2
ratio-2
0
10
1
1
1
NIL
HORIZONTAL

SLIDER
3
199
187
232
hops
hops
0
99
20
1
1
NIL
HORIZONTAL

SLIDER
74
251
176
284
x-scaling
x-scaling
1
10
1
1
1
NIL
HORIZONTAL

SLIDER
3
166
187
199
number
number
1
4000
2400
1
1
NIL
HORIZONTAL

SLIDER
95
119
187
152
ratio-3
ratio-3
0
10
0
1
1
NIL
HORIZONTAL

MONITOR
3
240
67
285
x-scale
x-scale
0
1
11

MONITOR
3
292
67
337
y-scale
y-scale
0
1
11

TEXTBOX
19
374
268
428
GREEN LINE - Starting rabbit position\nPURPLE LINE - Average rabbit position\nYELLOW LINES - Number of rabbits at that position
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

This model simulates a binomial probability distribution or (in the limit) normal distribution.  It works by analogizing height variations to rabbit hops.

This model was created by a student in an effort to make sense of normal distributions.  In particular, he sought to understand  why height is distributed normally in human populations. For a detailed account of this case, see: Wilensky, U. (1997). What is Normal Anyway? Therapy for Epistemological Anxiety.  Educational Studies in Mathematics. Volume 33, No. 2. pp. 171-202. http://ccl.northwestern.edu/papers/normal/.

The procedures for this model have been kept largely intact from the original code written by the student. With advances in the language, this code is no longer at all an optimal way of writing this model. We have kept the original code for research purposes --- please do not use it as an example of good NetLogo coding.

## HOW IT WORKS

A number of rabbits are placed at the center of the bottom of the world.  A move pattern determines the way a rabbit moves.  Each rabbit can choose to hop right or left a certain hop-size. The likelihood of a rabbit following each move pattern is given in terms of ratios.  Each rabbit may have up to five different move patterns.

## HOW TO USE IT

### Setup

Method one (sliders setup): Press SETUP button. This creates the number of rabbits from the NUMBER slider and up to three hops and associated probability ratios from the six sliders above the NUMBER slider.  Each time a rabbit hops, it chooses one of the three moves -- hop-1, hop-2, or hop-3 -- with a likelihood in the ratio of ratio-1, ratio-2, and ratio-3 to each other.  For example, if ratio-1 = 2, ratio-2 = 4, and ratio-3 = 6, the rabbit has a 2-in-12 chance of making the hop-1 move, a 4-in-12 chance of making the hop-2 move, and a 6-in-12 chance of making the hop-3 move.

Method two (manual setup): In the Command Center, type "setup [number] [list of hops [list of probability ratios]" to initialize the rabbits (e.g. "setup 4000 [1 -1] [1 2]" will set up 4000 rabbits hopping either one unit to the right(1) or one unit to the left (-1) with a chance of hopping to the left being twice as much as that to the right.)  Up to five steps and corresponding probability ratios can be used.

### Running

The GO-ONE-HOP button makes each rabbit hop once.

The GO button tells the rabbits to hop the number of times set by the HOPS slider. For example, if HOPS is set to 10, the GO button makes each rabbit hop 10 times.  To stop the rabbits from hopping once they've started, press the GO button again.

There are two scale monitors and one scale slider in the Interface Window.  X-SCALING is used to magnify the width of the world to facilitate more hops. It is manually set by users with the X-SCALING slider. The setting can be changed as the model runs.  Y-SCALE is used to regulate the vertical scale -- to ensure that the highest yellow distribution bar is always 80% of the height of the world. This is done at each hop.

The figure inside the "y-scale" monitor is the number of rabbits a yellow line the height of the world represents.  The figure inside the "x-scale" monitor is the number of steps represented by a full view. (The rabbits wrap around the left and right edges, so if they get to the edge, you should increase the x-scale.)

The following formulae can be used to evaluate the actual numbers of rabbits or steps hopped:

Actual Number of Rabbits for a Yellow Line = height of line * ( y-scale / 100 )

Cumulative Number of Steps Hopped so far = X-coordinate of a line * ( x-scale / 100 )

To find out exactly how many rabbits are represented by a line, control-click (Mac) or right-click (other) anywhere on the line and choose inspect patch from the menu that appears.  The inspector will have a variable "turtle-bottom" which will tell you how many turtles (rabbits) are at the bottom of the line.)


## THINGS TO NOTICE

The purple average line shows where an average rabbit would be. Observe the movement of this line -- both its position and velocity -- and try to relate these to the settings.

Play with the NUMBER slider to see if what you predict is what you see when the number of rabbits is small. For what numbers of rabbits are your predictions the most accurate?

## THINGS TO TRY

Try different values for list of steps. What happens to the distribution?

Try different values for probability ratios.  What happens to the distribution?

Is the distribution always symmetric? What would you expect?

## EXTENDING THE MODEL

Create a plot for 'hopping'. First decide what to plot, and then implement the proper NetLogo plot functions.  
Rewrite the model so rabbits take list variables. Are there now new capabilities you can give the rabbits?

## NETLOGO FEATURES

The limitation on the number of turtles constrains the limits of the "number" slider. You can make the corresponding change to the `number` slider --- select the slider by clicking and dragging the mouse button over it.  Then click on the edit button and change 'Maximum' to the new number. Having more rabbits to jump can be useful for certain statistical simulations.

You can also change the settings to have a bigger world to fit more hops or show very fine distribution diagrams.

Note that since turtles could not have list variables in earlier versions of the language, the global lists steps and ratios are used to hold the movement patterns and ratios. The turtles access these globals to know how to move. (if we were writing this model now, we would not code it this way as turtles in NetLogo can have list variables). The procedures `define-steps` and 'define-ratios' use the primitives `first` and `butfirst`. Both of these are list operators --- that is, they operate on lists of things. The `first` of a list is simply its first element. Likewise, the `butfirst` of a list is a list of all elements except for the first.

## RELATED MODELS

Galton Box, Random Walk Left Right

## CREDITS AND REFERENCES

See: Wilensky, U. (1997). What is Normal Anyway? Therapy for Epistemological Anxiety.  Educational Studies in Mathematics. Volume 33, No. 2. pp. 171-202. http://ccl.northwestern.edu/cm/papers/normal/
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

rabbit
false
0
Circle -7500403 true true 67 127 171
Polygon -7500403 true true 87 157 18 66 10 27 25 5 54 5 77 24 111 147 105 170
Polygon -7500403 true true 192 140 205 37 220 10 248 2 274 7 283 29 279 50 256 77 209 151 185 167

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
-0.2 0 1.0 0.0
0.0 1 1.0 0.0
0.2 0 1.0 0.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

@#$#@#$#@
0
@#$#@#$#@

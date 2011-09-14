globals
[
  row             ;; current row we are now calculating
  done?           ;; flag used to allow you to press the go button multiple times
]

patches-own
[
  value           ;; some real number between 0 and 1
]

;; Setup general working environment.  The other setup procedures call this.
to setup-general
  set row max-pycor   ;; Set the current row to be the top
  set done? false
  cp ct
end

;; Setup for a single point
to setup-single
  setup-general
  reset-ticks
  ;; Set only the middle patch to be 1 on the top row
  ask patches with [pycor = row]
  [
    ifelse pxcor = 0
      [ set value 1 ]
      [ set value 0 ]
    color-patch
  ]
end

;; Setup for a random top row
to setup-random
  setup-general
  reset-ticks
  ;; Randomize the values of the top row
  ask patches with [pycor = row]
  [
    set value random-float max-value
    color-patch
  ]
end

;; This is called when auto-continue? is enabled or go is clicked after a run.
;; Setup the patches to continue a particular model run.  This will copy the i
;; bottom row of patches to the top row.
to setup-continue
  let value-list []
  if not done?  ;; make sure go has already been called
    [ stop ]

  set value-list map [[value] of ?] sort patches with [pycor = row]  ;; copy cell states from bottom row to a list
  setup-general
  ask patches with [pycor = row]  ;; copy states from list to top row
  [
    set value item (pxcor + max-pxcor) value-list
    color-patch
  ]
  set done? false
end

;; Run the model.
to go
  ;; Did we reach the bottom row?
  if row = min-pycor
  [
    ;; If auto-continue? is enabled or we are clicking go again,
    ;; we continue from the top, otherwise we stop
    ifelse auto-continue? or done?
    [
       set done? true
       display    ;; ensure all the patches get drawn before we clear
       setup-continue
    ]
    [
       set done? true
       stop
    ]
  ]

  ;; Go down a row
  set row (row - 1)

  ;; Calculate the values for the current row
  ask patches with [pycor = row]
  [
    calculate-value
    color-patch
  ]
  tick
end

to calculate-value  ;; patch procedure
  ;; Retrieve the three patches that touch this patch one row above it
  let top-neighbors neighbors with [pycor = [pycor] of myself + 1]

  ;; Calculate the average of their values
  let mean-value mean [value] of top-neighbors

  ;; Perform a function to the average and set it as our value
  set value iterated-map mean-value
end

;; This is a simple function that takes in a number, adds a constant to it and
;; only reports the decimal portion rounded to a given number of decimal places.
to-report iterated-map [x]
  report precision (fractional-part (add-constant + x))
                   precision-level
end

;; Scales the patch's color according to its value
to color-patch  ;; patch procedure
  set pcolor scale-color green value 0 2
end

;; Reports only the decimal part of the number
to-report fractional-part [x]
  report x - (int x)
end
@#$#@#$#@
GRAPHICS-WINDOW
257
10
669
443
100
100
2.0
1
10
1
1
1
0
1
0
1
-100
100
-100
100
1
1
1
ticks

BUTTON
123
42
243
75
Setup Random
setup-random
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
20
124
94
157
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

SWITCH
99
124
243
157
auto-continue?
auto-continue?
1
1
-1000

SLIDER
8
172
252
205
add-constant
add-constant
0
1
0.47
0.0010
1
NIL
HORIZONTAL

BUTTON
18
42
121
75
Setup Single
setup-single
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
123
78
243
111
max-value
max-value
0
1
0.5
0.01
1
NIL
HORIZONTAL

SLIDER
8
209
253
242
precision-level
precision-level
1
16
16
1
1
decimal places
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This is a continuous cellular automaton, otherwise known as a "coupled map lattice."  (See CA 1D Elementary and CA 1D Totalistic if you are unfamiliar with cellular automata.)  It operates just like a standard cellular automaton, except for the fact that its states are not discrete, but continuous values.

In this particular continuous cellular automaton, the cell's values range from 0.0 to 1.0, 0.0 being black and 1.0 being green.

Unlike two- or three-state cellular automata, each cell in a continuous cellular automaton can ideally have infinite states, but is limited by the precision of the computer (in this case the maximum is 16 decimal places, which is the most NetLogo can handle). The continuous nature of the CA and its particular rules allow this model to generate a great deal of complex behavior under almost any settings.

## HOW IT WORKS

The rules are a cross between a totalistic cellular automaton and an iterated map.

An iterated map is a fixed map, or function, that is applied to a number repeatedly.  The iterated map that is used in this model takes in a number, adds a constant (ADD-CONSTANT) between 0 and 1, and then the fractional part of that number is taken.  For example, if the given number is 0.8 and the add-constant is 0.6, the sum would be 1.4, and the fractional part would be 0.4.

The cellular automaton is totalistic, which means that at every time step, each cell's new state is determined by taking the average of itself and its nearest neighbors, and then passing it through the iterated map.

The precision of the values in the cells is determined by the PRECISION slider.

## HOW TO USE IT

Set up:
- SETUP SINGLE initializes the model with a single cell set to 1 in the center.
- SETUP RANDOM initializes the model with each initial cell being a random value ranging from 0 to MAX-VALUE

Parameters:
- ADD-CONSTANT is the constant added to the average value of the nearest cells before the fractional part is taken.
- PRECISION-LEVEL is the precision of the value of each cell, with a slider value of 1 being an accuracy of 1 decimal place, and 16 being an accuracy of 16 decimal places.

Running the model:
- GO begins running the model with the currently set parameters. It continues until it reaches the last row of patches.  If it is clicked again, GO will continue the run from the top.
- AUTO-CONTINUE? automatically wraps to the top once it reaches the last row when the switch is on

## THINGS TO NOTICE

Like their discrete counterparts, continuous cellular automata exhibit certain classes of behavior:
Class I - cellular automaton is ordered and always ends up in a uniform state.
Class II - cellular automaton is ordered and produces repetitive patterns.
Class III - cellular automaton is "chaotic," or almost completely random.
Class IV - cellular automaton is neither in class I, II, or III.  It exhibits "complex" behavior, which often lies between order and chaos.

Can you find any cellular automaton configurations that exhibit these properties?  What does this tell you about the nature of cellular automata?

Unlike elementary two-state cellular automata, certain configurations grow outwards at a rate that may be faster or slower than others, and less linear than the permitted one-cell per step growth in two-state cellular automata.  Why do you think this is?  What might this tell you about the relationship between the number of states and the growth of cellular automata?

The growth of continuous cellular automata is closely related to the continuity of space that it can produce.  With certain ADD-VALUES, this model is able to produce very smooth curves.  This is a sharp contrast to the jagged triangular patterns often found in two or three state cellular automata.  Why do you think this happens?  What might this tell you about the relationship between the number of states and spatial continuity in cellular automata?

Structures might seem to spontaneously occur from parts that seem very regular, especially when the model is run with initial random conditions with a with a very small MAX-VALUE.  It is important to note that this is generally caused by small changes in cell values that cannot be seen due to the color limitations of NetLogo, and ultimately the human eye.

## THINGS TO TRY

Try these with single point initial conditions:

Run the model with ADD-CONSTANT set to 0.0 and 1.0. Why do they produce the same pattern?

Why does it make sense that an ADD-CONSTANT of 0.5 produces relatively solid alternating green and black patterns?  Why does the line produced by the rule diffuse faster when the ACCURACY is higher?

Set the MAX-VALUE slider to a number between 0 and 0.10, and click setup random. Run the model a couple of times with different values for MAX-VALUE:
- Why do you think there is a delay in the occurrence of complex structures?
- Is there a relationship between the initial MAX-VALUE and the length of the delay?  If so, Why?

Change ADD-CONSTANT back to 0.47, and experiment with different values of the ACCURACY slider:
- How does the accuracy affect the patterns?
- How come the effect of the accuracy is more noticeable after a large number of steps? - Why might low accuracies create incongruities in the cellular automata?

Try out different ADD-CONSTANTS under various the ACCURACY slider values.  Now ask yourself, what disadvantages do computer-simulated continuous cellular automata have that discrete cellular automata do not?

## EXTENDING THE MODEL

There are many iterated maps that could be used to produce chaotic or complex behavior in a continuous cellular automaton.  Try coming up with your own iterated map function.

Try increasing the spatial continuity of the model by increasing the number of neighbors each cell has on either side.

Try making your own number data structure that has a greater accuracy than the normal NetLogo math primitives permit.

Can you come up with a new coloring function to represent values using a wider array of colors, not just a gradient between green and black?

## RELATED MODELS

CA 1D Elementary- the elementary two-state 1D cellular automaton
CA 1D Totalistic- a three-state totalistic 1D cellular automaton
Turbulence (in the Chemistry & Physics section)- a 1D continuous cellular automaton which demonstrates turbulence

## CREDITS AND REFERENCES

Pattern Dynamics in Spatiotemporal Chaos: Kunihiko Kaneko. 1989 Physics D 34 1-41
A New Kind of Science. Wolfram, S. 2002. Wolfram Media Inc.  Champaign, IL.

Thanks to Eytan Bakshy for his work on this model.
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
setup-random
repeat world-height - 1
  [ go ]
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

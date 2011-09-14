globals [
  current-row
  last-code    ;; used to track whether the rule switches should be updated
               ;; to match the slider, or vice versa
  gone?
]

patches-own [value]

to startup
  set gone? false
end

;; setup single cell of color-one in the top center row
to setup-single
  setup
  ask patches with [pycor = current-row] [ set pcolor color-zero set value 0 ]
  ask patch 0 current-row
  [
    set pcolor color-one
    set value 1
  ]
end

;; setup cells of random distribution across the top row
to setup-random
  setup
  ask patches with [pycor = current-row]
  [
    ifelse random-float 100.0 < density
    [
      ifelse random-float 100.0 > one-two-proportion  ;; proportion between color-one and color-two
        [ set pcolor color-one set value 1 ]
        [ set pcolor color-two set value 2 ]
    ]
        [ set pcolor color-zero set value 0 ]
  ]
end

to setup
  ifelse code = last-code  ;; determine whether to update the switches or the code slider
    [ switch-to-code ]
    [ code-to-switch ]
  set last-code code
  cp ct
  reset-ticks
  set current-row max-pycor  ;; set current row to top position
  set gone? false
end


to setup-continue
  if not gone? [stop]

  let value-list []
  set value-list map [[value] of ?] sort patches with [pycor = current-row]  ;; copy cell states from the current row to a list
  cp ct
  set current-row max-pycor  ;; reset current row to top
  ask patches with [ pycor = current-row ]
  [
    set value item (pxcor + max-pxcor) value-list  ;; copy states from list to top row
    set pcolor value-to-color value
  ]
  set gone? false
end

to go
  if current-row = min-pycor  ;; if we hit the bottom row
  [
    ifelse auto-continue?  ;; continue
    [
      set gone? true
      display    ;; ensure full view gets drawn before we clear it
      setup-continue
    ]
    [
      ifelse gone?
        [ setup-continue ]       ;; a run has already been completed, so continue with another
        [ set gone? true stop ]  ;; otherwise stop
    ]
  ]
  ask patches with [pycor = current-row]
    [ do-rule ]
  set current-row (current-row - 1)
  tick
end

to do-rule  ;; patch procedure
  ask patch-at 0 -1
  [
    ;; set the next state of the cell based on the left, center, and right
    set value get-next-value ([value] of patch-at -1 1 +
                              [value] of myself +
                              [value] of patch-at 1 1)
    ;; paint the next cell based on the new value
    set pcolor value-to-color value
  ]
end

to-report value-to-color [v]  ;; convert cell value to color
  ifelse v = 0
  [ report color-zero ]
  [ ifelse v = 1
      [ report color-one ]
      [ report color-two ]
  ]
end

to-report get-next-value [sum-value]  ;; determines the next state of the CA cell
  if sum-value = 0 [ report sum-0 ]
  if sum-value = 1 [ report sum-1 ]
  if sum-value = 2 [ report sum-2 ]
  if sum-value = 3 [ report sum-3 ]
  if sum-value = 4 [ report sum-4 ]
  if sum-value = 5 [ report sum-5 ]
  if sum-value = 6 [ report sum-6 ]
end

;; switch / code utility interface procedures
to switch-to-code  ;; changes code based on the positions of the switches
  set code sum-0
  set code (code + sum-1 * 3)
  set code (code + sum-2 * 9)
  set code (code + sum-3 * 27)
  set code (code + sum-4 * 81)
  set code (code + sum-5 * 243)
  set code (code + sum-6 * 729)
end

to code-to-switch  ;; changes switches based on the code slider
  let next (trinary-div code) ;; perform long division (base 3)
  set sum-0 (first next)    set next (trinary-div (last next))
  set sum-1 (first next)    set next (trinary-div (last next))
  set sum-2 (first next)    set next (trinary-div (last next))
  set sum-3 (first next)    set next (trinary-div (last next))
  set sum-4 (first next)    set next (trinary-div (last next))
  set sum-5 (first next)    set next (trinary-div (last next))
  set sum-6 (first next)
end

to-report trinary-div [number]  ;; helper function for long division in base 3
  let tri number mod 3
  report (list tri ((number - tri) / 3))
end
@#$#@#$#@
GRAPHICS-WINDOW
233
10
757
299
128
64
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
-128
128
-64
64
1
1
1
ticks

SLIDER
7
308
108
341
sum-0
sum-0
0.0
2.0
0
1.0
1
NIL
HORIZONTAL

SLIDER
108
308
207
341
sum-1
sum-1
0.0
2.0
2
1.0
1
NIL
HORIZONTAL

SLIDER
7
341
108
374
sum-2
sum-2
0.0
2.0
1
1.0
1
NIL
HORIZONTAL

SLIDER
108
341
207
374
sum-3
sum-3
0.0
2.0
0
1.0
1
NIL
HORIZONTAL

SLIDER
7
374
108
407
sum-4
sum-4
0.0
2.0
2
1.0
1
NIL
HORIZONTAL

SLIDER
108
374
207
407
sum-5
sum-5
0.0
2.0
0
1.0
1
NIL
HORIZONTAL

SLIDER
7
407
107
440
sum-6
sum-6
0.0
2.0
2
1.0
1
NIL
HORIZONTAL

BUTTON
11
12
116
45
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

BUTTON
10
193
78
226
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

SLIDER
236
330
408
363
color-zero
color-zero
0.0
139.0
90
1.0
1
NIL
HORIZONTAL

SLIDER
236
363
408
396
color-one
color-one
0.0
139.0
93
1.0
1
NIL
HORIZONTAL

SLIDER
236
396
408
429
color-two
color-two
0.0
139.0
96
1.0
1
NIL
HORIZONTAL

SLIDER
7
276
207
309
Code
Code
0.0
2186.0
1635
1.0
1
NIL
HORIZONTAL

SLIDER
10
103
218
136
one-two-proportion
one-two-proportion
0.0
100.0
50
1.0
1
NIL
HORIZONTAL

SLIDER
10
136
218
169
Density
Density
0.0
100.0
10
1.0
1
%
HORIZONTAL

BUTTON
10
67
115
100
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

TEXTBOX
124
65
215
98
Random Settings:
11
0.0
0

TEXTBOX
239
308
329
326
Colors:
11
0.0
0

TEXTBOX
9
251
99
269
Rules:
11
0.0
0

SWITCH
78
193
218
226
auto-continue?
auto-continue?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

This program is a one-dimensional three-color totalistic cellular automata. In a totalistic CA, the value of the next cell state is determined by the sum of the current cell and its neighbors, not by the values of each individual neighbor. The model allows you to explore all 2,187 3-color totalistic configurations.

This model is intended for the more sophisticated users who are already familiar with basic 1D CA's. If you are exploring CA for the first time, we suggest you first look at one of the simpler CA models such as CA 1D Rule 30.

## HOW IT WORKS

Each cell may have one of three colors with the value 0, 1, or 2.  The next state of a cell is determined by taking the sum value of the center, right, and left cell, yielding seven possible sums, 0-6, represented as the state-transition sliders sum-0 through sum-6.  Each of these seven possible states maps on to one of the 3 colors which can be set using the state-transition sliders.

## HOW TO USE IT

SETUP SINGLE: Sets up a single color-two cell centered in the top row
SETUP RANDOM: Sets up cells of random colors across the top row based on the following settings:
- one-two-proportion: the proportion between color-one and color-two
- density: what percentage of the top row should be filled randomly with color-one and color-two
AUTO-CONTINUE?: Automatically continue the CA from the top once it reaches the bottom row
GO: Run the CA.  If GO is clicked again after a run, the run continues from the top
CODE: Decimal representation of the seven base three configurations of the totalistic CA
SWITCHES: The rules for the CA.  Examples:
- sum-0: all color-zero
- sum-1: two color-zero and one color-one
- sum-2: two color-one and one color-zero, OR two color-zero and one color-two
- sum-6: all color-two
COLORS: Set the three colors used in the CA

## THINGS TO NOTICE

How does the complexity of the three-color totalistic CA differ from the two-color CA?  (see the CA 1D Elementary model)

Do most configurations lead to constantly repeating patterns, nesting, or randomness? What does this tell you about the nature of complexity?

## THINGS TO TRY

CAs often have a great deal of symmetry.  Can you find any rules that don't exhibit such qualities?  Why do you think that may be?

Try starting different configurations under a set of initial random conditions.  How does this effect the behavior of the CA?

How does the density of the initial random condition relate to the behavior of the CA?

Does the proportion between the first and second color make a difference when starting from a random condition?

## EXTENDING THE MODEL

Try having the CA use more than three colors.

What if the CA didn't just look at its immediate neighbors, but also its second neighbors?

Try making a two-dimensional cellular automaton.  The neighborhood could be the eight cells around it, or just the cardinal cells (the cells to the right, left, above, and below).

## RELATED MODELS

Life - an example of a two-dimensional cellular automaton
CA 1D Rule 30 - the basic rule 30 model
CA 1D Rule 30 Turtle - the basic rule 30 model implemented using turtles
CA 1D Rule 90 - the basic rule 90 model
CA 1D Rule 250 - the basic rule 250 model
CA 1D Elementary - a simple one-dimensional 2-state cellular automata model
CA Continuous - a totalistic continuous-valued cellular automata with thousands of states

## CREDITS AND REFERENCES

Thanks to Eytan Bakshy for his help with this model.

The first cellular automaton was conceived by John Von Neumann in the late 1940's for his analysis of machine reproduction under the suggestion of Stanislaw M. Ulam. It was later completed and documented by Arthur W. Burks in the 1960's. Other two-dimensional cellular automata, and particularly the game of "Life," were explored by John Conway in the 1970's. Many others have since researched CA's. In the late 1970's and 1980's Chris Langton, Tom Toffoli and Stephen Wolfram did some notable research. Wolfram classified all 256 one-dimensional two-state single-neighbor cellular automata. In his recent book, "A New Kind of Science," Wolfram presents many examples of cellular automata and argues for their fundamental importance in doing science.

See also:

Von Neumann, J. and Burks, A. W., Eds, 1966. Theory of Self-Reproducing Automata. University of Illinois Press, Champaign, IL.

Toffoli, T. 1977. Computation and construction universality of reversible cellular automata. J. Comput. Syst. Sci. 15, 213-231.

Langton, C. 1984. Self-reproduction in cellular automata. Physica D 10, 134-144

Wolfram, S. 1986. Theory and Applications of Cellular Automata: Including Selected Papers 1983-1986. World Scientific Publishing Co., Inc., River Edge, NJ.

Bar-Yam, Y. 1997. Dynamics of Complex Systems. Perseus Press. Reading, Ma.

Wolfram, S. 2002. A New Kind of Science. Wolfram Media Inc.  Champaign, IL.
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
repeat world-height - 1 [ go ]
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

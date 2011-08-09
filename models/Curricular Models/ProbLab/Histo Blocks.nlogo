globals [
  unordered-probabilities
  num-orderings
  expected-results

  number-of-items-in-column-text
  probability-of-items-in-column-text
  number-of-items-in-column
  probability-of-items-in-column
]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Setup Procedures
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to setup
  ca reset-ticks
  set-plot-x-range 0 5
  setup-view
  set number-of-items-in-column-text "-"
  set probability-of-items-in-column-text "-"
  set number-of-items-in-column "-"
  set probability-of-items-in-column "-"
  go
end

to setup-view
  ask patches with [pcolor = black] [set pcolor grey + 1]
  update-unordered-probabilities
  update-num-orderings
  let active-x min-x
  let active-y round min-pycor
  let delta-y round (world-height / 5)
  let possibility-index 0
  foreach n-values 5 [?] [
    foreach n-values item ? num-orderings [?] [
      ask patch active-x active-y [
        let current-4-block item possibility-index all-possibilities
        ask patch-at 0 1 [
          sprout 1 [
            set shape "bordered-square"
            ifelse item 0 current-4-block = 1 [
              set color green
            ] [
              set color blue
            ]
          ]
        ]
        ask patch-at 1 1 [
          sprout 1 [
            set shape "bordered-square"
            ifelse item 1 current-4-block = 1 [
              set color green
            ] [
              set color blue
            ]
          ]
        ]
        ask patch-at 0 0 [
          sprout 1 [
            set shape "bordered-square"
            ifelse item 2 current-4-block = 1 [
              set color green
            ] [
              set color blue
            ]
          ]
        ]
        ask patch-at 1 0 [
          sprout 1 [
            set shape "bordered-square"
            ifelse item 3 current-4-block = 1 [
              set color green
            ] [
              set color blue
            ]
          ]
        ]
      ]
      set active-y active-y + delta-y
      set possibility-index possibility-index + 1
    ]
    set active-y min-pycor
    set active-x active-x + delta-x
  ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to go
  every .1 [
    handle-mouse
  ]
  update-unordered-probabilities
  update-num-orderings
  set expected-results (map [?1 * ?2] unordered-probabilities num-orderings)
  update-plot
  ask turtles [
    ifelse color = green [
      set label precision p 2
    ] [
      set label precision (1 - p) 2
    ]
    if length (word label) = 3 [ set label word label "0" ] ;; force to 2 decimal places
    if length (word label) = 1 [ set label word label ".00" ] ;; force to 2 decimal places
  ]

  tick
end

to handle-mouse
  ifelse mouse-down? [
    let column round ((mouse-xcor - min-x) / delta-x)
    if column < 0 [ set column 0 ]
    if column > 4 [ set column 4 ]
    set number-of-items-in-column-text num-items-in-column column
    set probability-of-items-in-column-text chance-of-each-item-in-column column
    set number-of-items-in-column item column num-orderings
    set probability-of-items-in-column item column unordered-probabilities
  ] [
    set number-of-items-in-column-text "-"
    set probability-of-items-in-column-text  "-"
  set number-of-items-in-column "-"
  set probability-of-items-in-column "-"
  ]
end

to update-unordered-probabilities
  set unordered-probabilities n-values 5 [
    column-prob 4 ?
  ]
end

to update-num-orderings
  set num-orderings n-values 5 [
    choose 4 ?
  ]
end

to-report column-prob [n c]
  let result 1
  repeat c [
    set result result * p
  ]
  repeat (n - c) [
    set result result * (1 - p)
  ]
  report result
end

to-report choose [n c]
  report ((factorial n)/((factorial c) * (factorial (n - c))))
end

to-report factorial [n]
  report ifelse-value (n = 0) [
    1
  ] [
    n * factorial (n - 1)
  ]
end

to update-plot
  plot-pen-reset
  let max-expected-result max expected-results
  ifelse auto-adjust-y-axis? [
    set-plot-y-range 0 precision ((max-expected-result * 1000 + 10) / 1000)  3
  ] [
    set-plot-y-range 0 1
  ]
  ifelse plot-individual-blocks? [
    let greenness 0
    (foreach expected-results num-orderings [
      let per-block ?1 / ?2
      let value-to-plot per-block
      repeat ?2 [
        plotxy greenness value-to-plot
        set value-to-plot value-to-plot + per-block
      ]
      set greenness greenness + 1
    ])
  ] [
    foreach expected-results [
      plot ?
    ]
  ]
end

;;;;;;;;;;;;;;;;;;;;;;;;
;; monitor work is below
;;;;;;;;;;;;;;;;;;;;;;;;

to-report num-items-in-column [index]
  report (word 4 "-choose-" index " = " item index num-orderings)
end

to-report chance-of-each-item-in-column [index]
  let result ""
  foreach n-values 4 [?] [
    if ? > 0 [
      set result word result " * "
    ]
    ifelse ? < index [
      set result (word result precision p 2)
    ] [
      set result (word result precision (1 - p) 2)
    ]
  ]
  report (word result " ~= " (precision item index unordered-probabilities 4))
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;Reporter Procedures
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to-report min-x
  report round (2 * min-pxcor / 3) - 1
end

to-report delta-x
  report round (world-width / 6)
end

;; all possible 4-blocks
to-report all-possibilities
  report (list
    [0 0 0 0]

    [0 0 0 1]
    [0 0 1 0]
    [0 1 0 0]
    [1 0 0 0]

    [0 0 1 1]
    [0 1 0 1]
    [0 1 1 0]
    [1 0 0 1]
    [1 0 1 0]
    [1 1 0 0]

    [0 1 1 1]
    [1 0 1 1]
    [1 1 0 1]
    [1 1 1 0]

    [1 1 1 1]
  )
end
@#$#@#$#@
GRAPHICS-WINDOW
260
225
648
613
-1
8
21.0
1
7
1
1
1
0
1
1
1
-9
8
-8
8
1
1
0
ticks

PLOT
255
10
645
223
Expected Outcome Distribution
NIL
NIL
0.0
5.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" ""

SLIDER
265
615
650
648
p
p
0
1
0.6
0.05
1
NIL
HORIZONTAL

BUTTON
25
10
130
43
NIL
Setup
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
135
10
240
43
NIL
Go
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
25
50
240
83
plot-individual-blocks?
plot-individual-blocks?
0
1
-1000

MONITOR
10
245
245
290
number of items in this column
number-of-items-in-column-text
3
1
11

MONITOR
10
310
245
355
probability of each item in this column
probability-of-items-in-column-text
3
1
11

MONITOR
10
375
245
420
n-choose-k * compound p
number-of-items-in-column * probability-of-items-in-column
4
1
11

TEXTBOX
28
133
240
203
Press Setup, then Go.\nUse the slider to change the p value.\nClick on a column in the View to see its information in the monitors. \n
11
0.0
1

SWITCH
25
88
240
121
auto-adjust-y-axis?
auto-adjust-y-axis?
1
1
-1000

TEXTBOX
15
220
165
238
Binomial Function:
11
0.0
0

TEXTBOX
205
290
230
308
X
11
0.0
0

TEXTBOX
205
355
231
373
=
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

This model is a part of the ProbLab curriculum.  The ProbLab Curriculum is currently under development at the Embodied Design Research Laboratory (EDRL), University of California, Berkeley.  For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

Histo-Blocks is a model for exploring the binomial function.  The random generator is a "4-Block," a 2-by-2 matrix, in which each of the four squares independently can be either green or blue.  The 4-Block is thus just like four coins that each can land either on heads or tails, only that here you can easily adjust the collective chance of these four independent singleton events.  For instance, you could adjust the model so that it will behave like four coins that each has a .6 chance of landing on heads.  The model shows connections between the random generator's sample space, probabilities of its singleton events, its expected outcome distribution, and the binomial function.

(This model is on theoretical probability only -- not empirical probability -- so there is no simulated experiment here.)

## HOW IT WORKS

The View displays the 16 unique elemental events of the green/blue 4-Block, arranged in five columns by the number of green squares in each permutation (0 through 4).  Labels on each of the singleton green squares show the current p value, and labels on the blue blocks show the complementary (1 - p) value.  The p value can be changed using a slider that is below the View.  When you click and hold the mouse button over a column in the View, three monitors to the left of the View display information: the number of blocks in that column (n-choose-k), the compound probability of each of the blocks in that column (the product of the probabilities of the four independent singleton events), and the product of these two latter components. This product -- the number of blocks in a column multiplied by the probability of each block in the column -- represents the chance of randomly getting any one of the blocks in that column, when you operate the random generator (that is, the chance of getting the combination regardless of the particular permutation). For example, the chance of getting any 4-Block with exactly 1 green is the same as the chance of getting exactly one heads when you toss four coins.

The plot shows a special histogram, in which each column is partitioned equally into as many parts as there are blocks in its corresponding View column, below. For example, the "2" column in the histogram in partitioned into 6 equal segments, because there are 6 unique 4-blocks that have two green squares in the block.  Whereas the blocks are equal in height within the columns, they differ in height between columns (for p values other than .5).  The relative heights index the compound probability.  So the histogram blocks -- the "histo-blocks" -- feature both factors at play in the binomial function: the n-choose-k coefficients are represented by the number of blocks in the column, and p^k * (1 - p)^(n - k) is represented by these blocks' individual heights.

## HOW TO USE IT

Press on SETUP, then GO.  Now, grab the slider and change the p value.

Buttons:
SETUP builds the "combinations tower" in the View.
GO enables the functioning of clicking on the screen and of the slider.

Switch:
PLOT-INDIVIDUAL-BLOCKS? toggles between having or not having the histogram partitioned.
AUTO-ADJUST-Y-AXIS? when set to 'On,' the histogram will keep adjusting for new p values so that the tallest column reaches to the top. When off, the max y value is 1.

Sliders:
P sets the p value.

Monitors:
When you click and hold down the mouse button over a given column in the view, the NUMBER OF ITEMS IN THIS COLUMN, PROBABILITY OF EACH ITEM IN THIS COLUMN, and N-CHOOSE K * COMPOUND P monitors update to provide information about that column.

## THINGS TO NOTICE

Note the probability values appearing in little labels on the squares.  Also note the shape of the histogram.  Both the labels and histogram change with p.

## THINGS TO TRY

Set the p value (on the slider) to .6.  Looking at the plot, what is special about this p value?  Can you find other p values that give this same effect?  With p set to .6, click anywhere on the middle column (2-green column).  Observe the monitors.  Now, with the mouse still down, drag the mouse one column to the right.  What happened in the monitors?

Set the auto-adjust-y-axis? to 'Off,' and slide the p value.  Look at the histogram as you do this.  What is happening to the histogram? -- What is changing?;  What is not changing?  What does this mean, in terms of the probabilities?

## EXTENDING THE MODEL

Add empirical functions to the model: create another histogram that shows actual outcomes of a simulated probability experiment with a 4-block (a sample of 4 independent events that each take on one of two possible values).  Place this new histogram on the interface such that it will readily compare to the histo-block histogram.  You can partition this histogram, too, according to sub-groups in the outcomes.

## NETLOGO FEATURES

This model uses a special procedure in order to partition the histogram columns.

## RELATED MODELS

Several of ProbLab's models are related to Histo Blocks, notably Sample Stalagmite.

## CREDITS AND REFERENCES

Thanks to Dor Abrahamson for his work on the design of this model and the ProbLab curriculum. Thank you to Josh Unterman for his talent and work on producing this model.
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

bordered-square
false
0
Rectangle -7500403 true true 15 15 285 285

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
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

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
1
@#$#@#$#@

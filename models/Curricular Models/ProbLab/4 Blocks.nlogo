breed [ frames frame ]
;; frames are black frames around each patch; they help count the colored patches

globals [
  target-color other-color ;; colors of patches before and during the runs
  target-color-list ;; accumulates values from repeated runs, these are histogrammed
  stratified-list
  stratified-indices
]

;;;;;;;;;;;;;;;;;;
;;; Setup Procedures
;;;;;;;;;;;;;;;;;;

to setup
  initialize
  create-the-frames
end

to initialize
  clear-all
  reset-ticks
  set-colors
  set target-color-list []
  ask patches [ set pcolor white - 1]
  set stratified-list n-values 16 [0] ;; counts the frequency of each of the 16 permutations.
  ;; This list indexes the positions of the 16 permutations in the CT by the bitmask of the four patches.
  set stratified-indices (list 0 1 2 5 3 6 7 11 4 8 9 12 10 13 14 15)
end

;; We used color variables and not just colors so that you could change the color values
;; to suit you. For instance, perhaps you would want to change the green to cyan
to set-colors
  set target-color green
  set other-color blue
end

to create-the-frames ;; each individual patch in the block is framed, creating an overall effect
                     ;; of a grid that helps distinguish individual patches in the block
  set-default-shape frames "frame"
  ask patches [
    sprout 1 [
      set breed frames
      set color black
    ]
  ]
end

;;;;;;;;;;;;;;;;;;
;;; Go Procedures
;;;;;;;;;;;;;;;;;;

to go
  if one-by-one-choices? [ask patches [set pcolor white - 1 ] display wait .3]
  ask patches
  [
    ifelse random-float 100 < probability-to-be-target-color [
      set pcolor target-color
      ;set flag 1
    ] [
      set pcolor other-color
      ;set flag 0
    ]

    if one-by-one-choices? [ display wait 0.1 ]
  ]

  make-histogram
  tick
  if one-by-one-choices? [ wait 0.5 ]
end

;;;;;;;;;;;;;;;;;;
;;; Utilities
;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;
;;; Plotting
;;;;;;;;;;;;;;;;;;

to make-histogram
  set target-color-list fput count patches with [pcolor = target-color] target-color-list
  ;; i is the bitmask of the patches
  let i sum [ 2 ^ ( pxcor + 2 * pycor ) ] of patches with [pcolor = target-color]
  set i item i stratified-indices
  set stratified-list replace-item i stratified-list (item i stratified-list + 1)

  set-current-plot "Distribution by Target Color"

  ifelse stratified? [
    clear-plot
    plotxy 0 0
    plotxy 4 0
    let range 0 ;; keeps track of the highest column
    let col-height 0 ;; keeps track of total height of a given histogram column
    let perm-category 0 ;; keeps track of which segment of a given histogram column
                        ;; corresponds to a specific block permutation
    foreach [0 1 2 3 4] [ ;; foreach histogram column, which represents the number
                          ;; of blocks that are the target-color
      let column ?
      set col-height 0 ; since it's a new column, start at the bottom
      foreach n-values (choose 4 column) [?] [ ;; foreach possible permutation
                                               ;; of blocks in this column, add
                                               ;; a piece of the histogram
        set col-height col-height + item perm-category stratified-list
        plotxy column col-height
        set perm-category perm-category + 1
        set range max list range col-height
      ]
    ]
    if range > 9 [set-plot-y-range 0 range + 1]
  ] [
    histogram target-color-list
    let maxbar modes target-color-list
    let maxrange filter [ ? = item 0 maxbar ] target-color-list
    set-plot-y-range 0 length maxrange
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
15
10
315
331
-1
-1
145.0
1
10
1
1
1
0
1
1
1
0
1
0
1
1
1
1
ticks
30

BUTTON
15
335
110
375
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
115
335
210
374
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

PLOT
320
10
558
379
Distribution by Target Color
targets
how many times
0.0
5.0
0.0
10.0
true
false
"" ""
PENS
"target-color" 1.0 1 -16777216 true "" ""

SWITCH
60
420
260
453
one-by-one-choices?
one-by-one-choices?
1
1
-1000

BUTTON
215
335
315
375
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

SLIDER
15
380
315
413
probability-to-be-target-color
probability-to-be-target-color
0
100
50
1
1
%
HORIZONTAL

SWITCH
375
385
505
418
stratified?
stratified?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

4-Blocks simulates an empirical probability experiment in which the randomness generator is a compound of 4 squares that each can independently be either green or blue.  The model helps us conceptualize relations between theoretical and empirical aspects of the binomial function: combinatorial analysis (what we can get) and experimentation (what we actually get).

This model is a part of the ProbLab curriculum.  The ProbLab Curriculum is currently under development at the CCL.  For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/ and http://edrl.berkeley.edu/design.shtml.

## HOW IT WORKS

The model operates all four squares simultaneously, just like flipping four coins at once. At every Go, each square "flips a coin" to decide whether it should be green or blue at a probability you have set.  The number of green squares in the block is counted up and added to a list that is plotted as a histogram.  Over many runs, the histogram begins to "take shape." When the 'stratified?' switch is set to 'On,' each of the five columns will be parsed into one, four, six, four, or one sub-groups that record the within-column distribution of the five aggregate events.

## PEDAGOGICAL NOTE

In this probability experiment, there are five possible compound events: no-green, one-green, two-green, three-green, or four-green. These aggregate events (combinations) expand into a total of 16 elemental events (permutations) that collectively comprise the sample space: 1 no-green, 4 one-green, 6 two-green, 4 three-green, and 1 four-green. These values, 1-4-6-4-1, are the n-choose-k coefficients of the binomial function, where n is 4 and 'green' is the favorable event. A histogram tracks the accumulation of these compound outcomes. When the probability of getting green is set at 50%, the emergent empirical distribution will dynamically converge on a 1:4:6:4:1 distribution, and when we offset the p value of 50%, the distribution is accordingly offset.

A "stratification" feature allows us to monitor within-column accumulation corresponding with the 16 unique elemental events. The pedagogical objective of this stratification, which parses the distribution into the 16 chunks of elemental events, is to support learners in teasing out two complementary aspects of the distribution: whereas within-column elemental events all share the same chance, the fact that they are stacked together is an arbitrary decision of the experimenter. For example, we could have, hypothetically, sorted the 16 unique types of experimental outcomes into two groups - those that have a green cell in the bottom left corner and those that do not. In sum, the stratification feature was designed as one step toward demystifying for students the apparent ineluctability of the experiment's convergence on the anticipated distribution.

## HOW TO USE IT

Buttons:
SETUP initializes the variables and erases the plot.
GO ONCE activates the procedures just once.  So you will get a single 4-block in the view and the corresponding column in the histogram will rise by one "notch."
GO activates the procedures repeatedly until you press it again.

Sliders:
PROBABILITY-TO-BE-TARGET-COLOR sets the chance for each of the independent squares to be green.

Switches:
ONE-BY-ONE-CHOICES? Helps users see the 4-Block as a set of four independent randomness generators (like four flipping coins). When On, each square will settle on its color at a different moment. Also, when Go is pressed, there will be a pause between 4-blocks.  You'll want to set this switch to Off once the nature of the experiment is clear.
STRATIFIED? Makes the histogram columns be partitioned to reflect accumulation by unique elemental event. As more samples are taken, you will see the 1-4-6-4-1 structure emerge; the within-column blocks will equalize, but the between-column blocks will be equal only for a probability of 50%.

## THINGS TO NOTICE

Using the default model settings, press GO ONCE. See how each of the four squares takes time to decide whether it is green or blue.  This is like four coins that are spinning on the table until they each settle either on Head or Tail.

## THINGS TO TRY

Run the model with the probability-to-be-target-color set at 50%.  As the model runs, the histogram grows.  Pretty soon, the central column grows taller than other columns.  This demonstrates that there is a higher chance of getting 4-blocks that have exactly two green squares as compared to 4-blocks that have either zero, one, three, or four green squares.  But  there is more structure to interpret.  The lowest chance is to get a 4-block with no green squares or with four green squares.  The chance of getting a 4-block with either exactly one green or three green squares is in between.

Now change the value of the probability slider, press Setup, and run again.  What do you see?

Set the Stratified? switch to On, the model speed to slow, and the probability value to 50%  Run the model. See in the histogram how new blocks pop up.  Eventually, the number of blocks in the columns will be 1, 4, 6, 4, and 1, respectively.

## NETLOGO FEATURES

Auto-Plot: Look at the histogram as it grows. What happens when it reaches the top? The simulation is generating more and more samples,  but there is no room to count them. Instead of leaping up out of the box, the number at the top-left corner of the plot -- the value of the y-axis - updates, and the histogram is redrawn to fit.
Stratification: The partitioning of the histogram columns by unique elemental events was created especially for this model. See the procedures to learn how this was accomplished. Essentially, a nested structure was used to create within each column as many sub-columns as necessary. For example, the one-green columns (second from the left) is comprised of four stacked columns.

## EXTENDING THE MODEL

You may want to monitor different aspects of the probabilistic experiment, to answer such questions as:
- how often do we get the same combinations twice one after the other?
- is there particular permutations you like?  You could add code to see how long it takes the model to find this permutation.
- what is the dynamic ratio between the number of one-green and three-green outcomes?

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

frame
false
0
Rectangle -7500403 true true 0 283 298 298
Rectangle -7500403 true true 0 0 16 298
Rectangle -7500403 true true 0 0 298 16
Rectangle -7500403 true true 282 0 298 298

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

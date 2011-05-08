globals [
  generators        ;; agentset of two patches where the dice first appear
  top-row           ;; agentset of just the top row of patches
  single-outcomes   ;; list of single dice values
  pair-outcomes     ;; list of dice pair sums
]

patches-own [
  column            ;; what number (single die or sum of pair) this column of patches is for
]

breed [paired-dice paired-die]   ;; dice considered as part of pairs
breed [single-dice single-die]   ;; dice considered singly
breed [stacked-dice stacked-die] ;; dice that have stopped moving

;; all three breeds have this variable
turtles-own [
  die-value        ;; 1 through 6
]

paired-dice-own [
  pair-sum         ;; 2 through 12
]

to setup
  clear-all
  set single-outcomes []
  set pair-outcomes []
  ;; assign outcomes to columns
  ask patches with [pxcor > 4] [
    set column floor ((pxcor - 1) / 2)
  ]
  ask patches with [pxcor < -4] [
    set column pxcor - min-pxcor  + 1
  ]
  ;; color patches
  ask patches [ set pcolor gray + 3 ]
  ask patches with [column != 0] [
    ifelse column mod 2 = 0
      [ set pcolor gray ]
      [ set pcolor brown - 1 ]
  ]
  ;; set up agentsets
  set top-row patches with [pycor = max-pycor]
  set generators top-row with [pxcor = -1 or pxcor = 0]
  ;; start clock and plot initial state
  reset-ticks
end

to go
  if stop-at-top? and any? turtles-on top-row [
    user-message "The top has been reached. Turn STOP-AT-TOP? off to keep going."
    stop
  ]
  if not stop-at-top? [
    bump-down stacked-dice with [pxcor < 0]
    bump-down stacked-dice with [pxcor > 0]
  ]
  roll-dice
  while [any? single-dice or any? paired-dice] [
    move-paired-dice
    move-single-dice
    display    ;; force the view to update, so we see the dice move smoothly
  ]
  tick
end

;; creates a new pair of dice (both singles and pairs)
to roll-dice
  ;; ask each generator patch to create two paired dice
  ask generators [
    sprout-paired-dice 1 [
      set color white
      set die-value 1 + random 6
      set shape word "die " die-value
      set heading 90
    ]
  ]
  ;; clone the paired dice to make the single dice
  ask paired-dice [
    hatch-single-dice 1 [
      set heading 270
      ;; changing breeds resets our shape, so we must explicitly adopt
      ;; our parent's shape
      set shape [shape] of myself
    ]
  ]
  ;; set the sum variable of the pairs
  let total sum [die-value] of paired-dice
  ask paired-dice [ set pair-sum total ]
  ;; add to outcomes lists
  set pair-outcomes lput total pair-outcomes
  ask single-dice [ set single-outcomes lput die-value single-outcomes ]
end

to move-paired-dice
  ;; if either of the two dice isn't at the right column yet,
  ;; both dice move
  ifelse any? paired-dice with [pair-sum != column]
    [ ask paired-dice [ fd 1 ] ]
    ;; otherwise both dice fall
    [ ask paired-dice [
        ;; if at the bottom of the view, check if we should go "underwater"
        if pycor = min-pycor [ paired-die-check-visible ]
        fall
      ]
    ]
end

to move-single-dice
  ;; two single dice may be falling in the same column, so we have
  ;; to make sure that the bottom one moves before the top one,
  ;; otherwise they could get confused
  let how-many count single-dice
  if how-many > 0 [
    ask min-one-of single-dice [pycor] [ move-single-die ]
  ]
  if how-many > 1 [
    ask max-one-of single-dice [pycor] [ move-single-die ]
  ]
end

to move-single-die  ;; single-die procedure
  ifelse die-value != column
    [ fd 1 ]
    [ ;; if at the bottom of the view, check if we should go "underwater"
      if pycor = min-pycor [ single-die-check-visible ]
      fall
    ]
end

to fall  ;; single-die or paired-die procedure
  set heading 180
  ifelse (pycor > min-pycor) and (not any? stacked-dice-on patch-ahead 1)
    [ fd 1 ]
    ;; stop falling
    [ ;; changing breeds resets our shape, so we have to remember our old shape
      let old-shape shape
      set breed stacked-dice
      set shape old-shape
    ]
end

;; determines if my column is tall enough to be seen
to single-die-check-visible  ;; single-die procedure
  if single-outcomes = [] [ stop ]
  let mode first modes single-outcomes
  let height-of-tallest-column length filter [? = mode] single-outcomes
  let height-of-my-column length filter [? = die-value] single-outcomes
  if (height-of-tallest-column - height-of-my-column) >= world-height - 2 [ die ]
end

;; determines if my column is tall enough to be seen
to paired-die-check-visible  ;; paired-die procedure
  if pair-outcomes = [] [ stop ]
  let mode first modes pair-outcomes
  let height-of-tallest-column length filter [? = mode] pair-outcomes
  let height-of-my-column length filter [? = pair-sum] pair-outcomes
  if (height-of-tallest-column - height-of-my-column) >= world-height - 2 [ die ]
end

to bump-down [candidates]
  while [any? candidates with [pycor = max-pycor - 2]] [
    ask candidates [
      if pycor = min-pycor [ die ]
      fd 1
    ]
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
177
39
557
590
-1
-1
10.0
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
26
0
51
1
1
1
ticks
30.0

BUTTON
11
10
90
43
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
92
10
171
43
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
11
48
171
590
Single Dice
Die Value
Count
1.0
7.0
0.0
51.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" "histogram single-outcomes\nlet maxbar modes single-outcomes\nlet maxrange length filter [ ? = item 0 maxbar ] single-outcomes\nset-plot-y-range 0 max list 51 maxrange\n"

PLOT
562
49
783
593
Pair Sums
Dice Total
Count
2.0
13.0
0.0
51.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" "histogram pair-outcomes\nlet maxbar modes pair-outcomes\nlet maxrange length filter [ ? = item 0 maxbar ] pair-outcomes\nset-plot-y-range 0 max list 51 maxrange"

SWITCH
374
4
498
37
stop-at-top?
stop-at-top?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

Dice Stalagmite is a model for thinking about the relations between independent and dependent random events.  Pairs of dice are rolled, then the dice fall into columns in two bar charts.  One of these charts records the dice as two independent outcomes, and the other, as a single compound event (sum) of these two outcomes.  Because the columns grow from the bottom up, we call this a "stalagmite."

Different distributions emerge: the independent-event bar chart is flat (equally distributed) whereas the dependent-event bar chart is peaked.  (It does not quite approach a normal distribution, because there are only two compound outcomes.)

This model is a part of the ProbLab curriculum.  The ProbLab curriculum is currently under development at the CCL.  For more information about the ProbLab curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

The outcomes from rolling the two dice are represented in two different ways.

On the left, they are plotted as individual events.  This representation treats the dice individually, not as pairs.  Each die is stacked in its respective column, one through six, in the resulting histogram.

On the right, you see a second histogram with the same dice stacked in pairs according to their sum.  There are eleven columns, 2 through 12, since those are the possible sums of two dice.

When the model is run, the right chart never reaches the top before the left chart.  (Why?)  The left bar chart is "bumped" down by one row so as to leave more room for the bars to grow.  This allows for the bar chart on the right to grow further and take on its typical (peaked) shape.

## HOW TO USE IT

Switches:  
STOP-AT-TOP? -- if 'On', stops the model when the right side of the display bar chart (the dice totals) has reached the top.  If 'Off', then both stacks "bump" down one row when a column hits the top.  (The plots on either side of the view are always scaled to show all of the data, even if the view is only showing the top portion.)

Buttons:  
SETUP -- prepares the model for running.

GO -- runs the model.  In a single run of GO, a random pair of dice appears, is copied, and then the copies fall into their stacks.  Also, the plots are updated.

Plots:  
SINGLE DICE -- plots the number of occurrences of each die-number (1-6).  
PAIR SUMS -- plots the number of occurrences of each die-total (2-12).  
The plots show the same information as the view, except that the plots always show all of the data, while if the STOP-AT-TOP? switch is off, the view only shows the tops of the stacks.

## PEDAGOGICAL NOTE

As in other ProbLab activities, here we are interested in exploring relations between the anticipated frequency distribution (the relative probabilities), which we determine through combinatorial analysis, and the outcome distribution we receive in computer-based simulations of probability experiments.  To facilitate the exploration of the relationship between such theoretical and empirical work, we build tools that bridge between them.  These bridging tools have characteristics of both the theoretical and empirical work.  Specifically, we structure our combinatorial spaces in formats that resemble outcome distributions, and structure our experiments so as to sustain the raw data (not just graphs representing the data).  The "picture bar chart" of the combinatorial space of dice-pair totals can be found with the ProbLab materials.

Beside each bar chart -- the 'dependent' and the 'independent' -- there is a histogram that represents the data correspondingly.  Whereas the bar charts stack the outcomes so as to sustain the images of the discrete events (the "raw data" themselves), the histograms grow in continuous columns (without partition lines).  Twinning each picture bar chart with its respective histogram may help students both to understand the histograms and to shift from additive interpretation of the columns in the picture bar chart (focusing on differences between heights of columns) to a multiplicative interpretation of the bar chart (focusing on the proportions of the column heights).

In a classroom, students should work with the triangular combinatorial space they created (not the one from the model, but one with all 36 different possible outcomes of a dice pair that are arranged in a bar chart).  Discussion should focus on the relation between the theoretical and empirical distribution, that is, between the combinatorial space and the distribution of random outcomes.  Why is it that they are similar?

## THINGS TO NOTICE

Note the shape of the outcomes in the right-hand bar chart.  The top is triangular.  What does this mean? Specifically, if each event is random and independent, why are we getting a shape that is not random (always the same shape)?  How can randomness and determinism coexist like this?  The bar chart on the left hones this discussion, because, from run to run, it is basically a "flat" distribution -- for instance, you can never predict, with certainty, which die column will be first to reach the top.

If the model runs long enough and if STOP-AT-TOP? is set to 'Off,' you will notice that some columns in the picture bar chart on the left vanish.  That is, you will see a die descending to the bottom of its column and "going below sea level" so it is no longer visible.  What happens is that this die's column is now too short to appear in the display.  It might grow tall enough later to come back in, or it might not.  Meanwhile, the histogram in the plot keeps all of its columns, so you can keep comparing between them.

## THINGS TO TRY

How many pairs are needed until the dice-pair bar chart reaches the top?  Is this number constant?  How much does it vary?

What is the biggest vertical gap between columns in the single-die bar chart?  Does the gap get larger or smaller the more you run the model?  Does any particular column win more often than others?

Which column in the dice-pair bar chart gets to the top first most often?

## EXTENDING THE MODEL

Currently, the model sums two dice.  An interesting idea would be to extend this model to have a sum of three or more dice.  There would be more columns for the different dice-totals.  How many?  How would this change affect the dice-total distribution?

Currently the model puts all pairs of dice that sum to the same number in the same column.  What would happen if you added additional columns so that different combinations were in different columns, for example, so that 2+5 and 5+2 were considered different?  Would this change the shape of the dice-total distribution?

## NETLOGO FEATURES

In this model, the origin (patch 0,0) is placed between the single and pair bar charts rather than in the center, which makes computations simpler and extending the model easier.

## RELATED MODELS

Dice Stalagmite uses the same basic metaphor as the ProbLab model 9-Block Stalagmite.  In that model, a random 9-block or 4-block is selected from a sample space.  Then, the block finds is correct column, according to the number of green squares in the block, and stacks up in that column.

The idea of juxtaposing two or more different representations of the same running data is used in several ProbLab models, such as Prob Graphs Basic or Random Combinations and Permutations.

Dice are also used in the ProbLab model Dice for generating a distribution of random outcomes.

The Galton Box model also features raw data that descend and stack up in columns.

## CREDITS AND REFERENCES

This model is a part of the ProbLab curriculum.  The ProbLab curriculum is currently under development at Northwestern's Center for Connected Learning and Computer-Based Modeling.  For more information about the ProbLab curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

Thanks to Josh Unterman for building the original version of this model.  Thanks to Steve Gorodetskiy for his contribution to the design of this model.
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

die 1
false
0
Rectangle -7500403 true true 45 45 255 255
Circle -16777216 true false 129 129 42

die 2
false
0
Rectangle -7500403 true true 45 45 255 255
Circle -16777216 true false 69 69 42
Circle -16777216 true false 189 189 42

die 3
false
0
Rectangle -7500403 true true 45 45 255 255
Circle -16777216 true false 69 69 42
Circle -16777216 true false 129 129 42
Circle -16777216 true false 189 189 42

die 4
false
0
Rectangle -7500403 true true 45 45 255 255
Circle -16777216 true false 69 69 42
Circle -16777216 true false 69 189 42
Circle -16777216 true false 189 69 42
Circle -16777216 true false 189 189 42

die 5
false
0
Rectangle -7500403 true true 45 45 255 255
Circle -16777216 true false 69 69 42
Circle -16777216 true false 129 129 42
Circle -16777216 true false 69 189 42
Circle -16777216 true false 189 69 42
Circle -16777216 true false 189 189 42

die 6
false
0
Rectangle -7500403 true true 45 45 255 255
Circle -16777216 true false 84 69 42
Circle -16777216 true false 84 129 42
Circle -16777216 true false 84 189 42
Circle -16777216 true false 174 69 42
Circle -16777216 true false 174 129 42
Circle -16777216 true false 174 189 42

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
NetLogo 5.0beta3
@#$#@#$#@
setup repeat 150 [ go ]
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

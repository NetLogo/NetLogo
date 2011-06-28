globals
[
  counter         ;; used in setting up the messengers
  at-top?         ;; Boolean that discontinues run when columns reach the top of the space
  max-y-histogram ;; how high the columns can rise (or how far up the yellow goes)
  list-of-labels  ;; list of all the outcomes
  list-of-label-means ;; list of the mean values of messengers in each iteration through Go


  height          ;; height, from the bottom of the view, that bricks may rise to
  space-color     ;; background color of space where bricks stack up
]

breed [ messengers messenger ]      ;; messengers carry a brick to the random-value column.
                                    ;; they place the brick on top of the column
breed [ column-counters column-counter ] ;; column-counters live on top of each column. Messengers go to column-counters.
breed [ bricks brick ]          ;; when a messenger brings a square bricks that indicate events in histogram columns

messengers-own [ destination ] ;; destination is the top of the column that the messenger heads towards

column-counters-own
[
  my-column                     ;; what column it is in, with the left-most pycor being "1"
  my-column-patches             ;; all patches that are in its column as an agentset
  num-messengers-pointing-to-me ;; how many messengers are about to move towards it
  my-ycor                       ;; its ycor, even if this ycor is out of bounds below the edge of the world
]

to setup
  clear-all
  set height world-height - 10
  set space-color white
  set max-y-histogram (min-pycor + height)  ;; bricks placed above this line cause
                                                    ;; all bricks to bump down
  create-histogram-space
  setup-column-counters
  set at-top? false
  set list-of-label-means []
  reset-ticks
end

to create-histogram-space  ;; this code supports a slider for sample-space (the slider does not exist now)
  ask patches
  [
    ifelse (pxcor < (min-pxcor + sample-space) )
            and (pycor < max-y-histogram)
    [ set pcolor space-color ]
    [ set pcolor white - 2 ]
  ]
end

to setup-column-counters  ;; column-counters show messengers where to take their bricks.
  ask patches with [ (pycor = min-pycor ) and pcolor = space-color ]
  [
    sprout 1
    [
      set breed column-counters
      ht
      set heading 0
      ;; each column-counter knows its column in the histogram
      set my-column floor (max-pxcor + pxcor + 1)
      ;; each column-counter knows its patches
      set my-column-patches patches with [ pxcor = [pxcor] of myself ]
      set my-ycor ycor
    ]
  ]
end

to go ;; forever button
  if at-top? and (not bump-down?) [ stop ]
  place-messengers-and-choose-random-values
  ask messengers [point-to-your-column]
  ask column-counters [count-num-messengers-pointing-to-you]
  if bump-down? [ bump-down ]
  move-messengers
  tick
  histogram-labels
  histogram-label-means
  ask messengers [ die ]
end

to place-messengers-and-choose-random-values
  ;; each messenger is born and gets a random value from the sample space
  ;; messengers are centered over the middle of the space and equally distributed across it
  let increment ( sample-space / num-messengers )
  let it round (min-pxcor +  increment / 2)

  repeat num-messengers
  [
    ask patch round (it + counter * increment) (max-y-histogram + 4)
    [
      sprout 1
      [
        set breed messengers
        set shape "messenger"
        ht
        set color 3 + 10 * counter
        set label-color red
        set heading 180
        set size world-height - height + 2
        set label 1 + random sample-space
      ]
     ]
   set counter counter + 1
  ]
  set counter 0
  ask messengers [st]
end

to point-to-your-column ;; turtle procedures. Each messenger points towards the top of its column
    set destination one-of column-counters with [ my-column = [label] of myself ]
    face destination  ;; messenger heads to column top
end

;; Each column-counter figures out how many messengers point at it
to count-num-messengers-pointing-to-you  ;; turtle procedure
  set num-messengers-pointing-to-me count messengers with [destination = myself]
end

to move-messengers
  ;; Each messenger goes to the top of the column of its value
  while [any? messengers with [distance destination > 3] ] [
    ask messengers with [distance destination > 3] [  ;; messenger will stop when it appears on top of its destination
      face destination ;; this is a precaution in case the column-counter has gone up
      fd 1
    ]
    display
  ]
  ask messengers [ lay-your-brick ]
end

to lay-your-brick ;; when messenger has arrived, the column-counter builds a brick and moves above it
    ask destination
    [
      ifelse my-ycor < min-pycor
      [
        set my-ycor my-ycor + 1
      ]
      [
         create-brick
         fd 1
         if (ycor = max-y-histogram) [ set at-top? true ]
         set my-ycor ycor
      ]
    ]

    ht
end

to create-brick ;; turtle procedure  ;; its patch creates the square brick
  ask patch-here
  [
    sprout 1
    [
      set breed bricks
      set shape "brick"
      set color black
    ]
  ]
end

to histogram-labels
  set list-of-labels ( sentence list-of-labels [label] of messengers )
  set-current-plot "All Values From Messengers"
  histogram list-of-labels
  let maxbar modes list-of-labels
  let maxrange length ( filter [ ? = item 0 maxbar ] list-of-labels )
  set-plot-y-range 0 max list 10 maxrange
end

to histogram-label-means
  ;; the histogram displays the mean values from the batches of messengers over all runs.
  ;; for even numbered sample spaces, mean values that fall in between the center columns
  ;; are assigned randomly either to the left-side column or to the right-side column
  let mean-num ( mean [ label ] of messengers )
  ifelse ( mean-num - (int mean-num) ) = .5
     [
       if random 2 = 0 [ set mean-num round mean-num ]
     ]
     [
       set mean-num round mean-num
     ]
  set list-of-label-means ( sentence mean-num list-of-label-means )
  set-current-plot "Mean Values of Batches of Messengers"
  histogram list-of-label-means
end

to bump-down ;; when columns have reached the top of the space, they all go down by one brick
  ;; first, we determine how far above the max line we would go if we didn't bump down (it might zero)
  let expected-max-height ( max [ my-ycor + num-messengers-pointing-to-me ] of column-counters )
  let num-over-the-top ( expected-max-height - max-y-histogram )
  if num-over-the-top <= 0 [stop]

  repeat num-over-the-top
  [
     ask column-counters [
        ifelse my-ycor <= min-pycor
           [ set my-ycor my-ycor - 1 ]
           [ (set heading 180) (fd 1) (ask bricks-here [die]) (set heading 0) (set my-ycor ycor)]
     ]
  ]
end

to-report max-diff  ;; difference in height between tallest and shortest columns;
                    ;; "short" can be below the bottom of the world)
  report max [my-ycor] of column-counters - min [my-ycor] of column-counters
end
@#$#@#$#@
GRAPHICS-WINDOW
187
10
803
275
50
19
6.0
1
12
1
1
1
0
0
0
1
-50
50
-19
19
1
1
1
ticks
30.0

BUTTON
19
35
89
68
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
92
35
158
68
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
12
114
168
147
num-messengers
num-messengers
1
30
2
1
1
NIL
HORIZONTAL

PLOT
172
396
823
530
Mean Values of Batches of Messengers
Mean Values
Count
1.0
101.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" ""

SWITCH
13
203
136
236
bump-down?
bump-down?
0
1
-1000

SLIDER
12
78
169
111
sample-space
sample-space
1
100
100
1
1
NIL
HORIZONTAL

MONITOR
11
467
102
512
std dev
standard-deviation list-of-label-means
3
1
11

MONITOR
12
336
130
381
Biggest Difference
max-diff
0
1
11

PLOT
172
276
823
396
All Values from Messengers
Values
Count
1.0
101.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" ""

@#$#@#$#@
## WHAT IS IT?

Random Basic Advanced explores the effect of sample size on the distribution of sample mean.

At each run, a sample of random values is selected and displayed on "messengers," who each carry a brick to the top of a corresponding column in a bar chart.  (So a messenger with "5" will carry the brick to the top of the fifth column from the left.) The values are also added into a histogram (below the view). The mean value from each batch of messenger is added to yet another histogram (at the bottom of the interface).

The larger the sample size, the smaller the variance of the distribution. That is, the sample space does not change, but extreme values become more and more rare as the sample size increases.  Combinatorial analysis helps understand this relation.

This model is a part of the ProbLab curriculum. The ProbLab curriculum is currently under development at the CCL. For more information about the ProbLab curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

At every trial (sampling), random values are assigned to as many "messengers" as you choose. The messengers each carry a brick. They go to a column according to their value and lay the brick at the top of that column. The mean value of the batch of messengers is plotted in a histogram.

## HOW TO USE IT

### Buttons

SETUP - prepares the model for running with your sample-space parameter setting.

GO - runs the model with the current settings.

### Sliders

SAMPLE-SIZE --- set the total number of integer values, beginning from 1, that can be randomly assigned to the messengers

### Switches

BUMP-DOWN? --- when set to "on," the columns in the view will all go down by one step to anticipate the columns reaching the top of the space.

### Monitors

BIGGEST-DIFFERENCE --- show the vertical difference between the tallest column and the shortest column. Note that columns can be so short that they are below min-pycor.

STD DEV --- standard deviation of the sample-mean distribution. This is a measure of shape the distribution tends towards. A lower standard deviation implies a narrower distribution.

### Plots

ALL VALUES FROM MESSENGERS --- plots all of the randomly assigned values in the messenger labels.

MEAN VALUES OF BATCHES OF MESSENGERS --- plots the mean of all the randomly-assigned values in the messenger labels.

## THINGS TO NOTICE

Each messenger "carries" a little brick. It swoops down to the highest point in a brick tower. It goes to the brick tower that is as far from the left wall as is the messenger's number. So a messenger with "7" will go to the seventh column from the left.

When the bricks are about to hit the top of the space, all the columns "bump down" to make space for the new bricks. But the monitor "ALL VALUES" keeps a record of all the bricks that have been laid. So, sometimes a column of bricks will be empty, because it is not tall enough to make it into the view, yet its corresponding column in the the plot will still be there.

## THINGS TO TRY

Run the model with NUM-MESSENGERS = 1. Compare the histogram in the plot to the towers in the graphic windows. What do you see? Now setup and run the model with NUM-MESSENGERS = 2. Is the histogram any different from before? Repeat this for values of NUM-MESSENGERS 10, 20, and 30. For a sample size of 1, the raw data (the bricks in the towers) are exactly the same as the histogram. For a sample size of 2, we begin to see a certain shallow bump in the middle of the distribution. For larger sample sizes, this bump becomes more and more acute.  For a sample size of 30, the distribution is narrow.

Another comparison is to do with the likelihood of getting a low value, say "1," in different settings of NUM-MESSENGERS. Run the model 100 trial for different values of NUM-MESSENGERS and see if there is a pattern to this comparison.

What is the standard deviation dependent on? For a fixed number of messengers, would a larger sample space change the standard deviation? If so, why? For a fixed sample space, should a change in the number of messengers affect the standard deviation? If so, why? Can you determine a relation between these three values (NUM-MESSENGERS, SAMPLE-SPACE, and STD-DEV)? One way to begin would be to use NetLogo's BehaviorSpace.

Once the model has run for many trials, should the BIGGEST-DIFFERENCE increase or decrease? On the one hand, individual columns have "opportunities" to get very tall, but on the other hand, all columns have the same opportunities. Is this a paradox?

## PEDAGOGICAL NOTE

Why are we getting this pattern? Let's think about the case of NUM-MESSENGERS = 2 to understand the bump in the distribution plot:

The only way to get a value of "1," is if both the messengers have a value of "1," but to get a value of, say, "2," either both messengers have "2," or one has "1" and the other "3" or vice versa. So there are three different ways of getting "2." How about getting a "3?" There are more than three ways of getting "3": [3,3]; [2,4]; [4,2]; [1,5]; [5,1]. So there are five ways of getting "3." You can see that the nearer the value is to the middle (50), there are more and more ways of getting that value as a mean of two values. Because these values are random, over many runs we will get more means that are closer to the middle, so we get the bump.

This way of finding all the different possible compound events is called combinatorial analysis --- determining all the combinations for a given set of variables each with a set of possible values. Once we know all the combinations, we can group them so as to predict the chances of getting any outcome from each group. For instance, if we know that there are triple as many ways for two messengers to form a mean of "2" than to form a mean of "1," then we can predict that we will get a "2" three times as much as we get a "1," if we run the simulation long enough. That is where combinatorial analysis (theoretical probability) meets experimental simulations (empirical probability).

Can we extend this way of thinking in order to understand the difference between the distribution we get for NUM-MESSENGERS = 2 as compared to the distribution we get for NUM-MESSENGERS = 3? For NUM-MESSENGERS = 3, we get an even narrower distribution. Why? Extending our previous way of thinking, we can expect that with three messengers the number of combinations for getting mean values of 1, 2, 3 etc. rises even more sharply than for NUM-MESSENGERS = 2. Let's see: for "1" there is only one combination: [1,1,1], just like for NUM-MESSENGERS = 2. But for "2" there are more than just three as in the previous example. Look: [2,2,2]; [1,2,3]; [1,3,2]; [2,3,1]; [2,1,3]; [3,1,2]; [3,2,1]; [1,1,4]; [1,4,1]; [4,1,1], for a total of ten combinations.

You might notice that the more messengers in a batch (the higher the setting of NUM-MESSENGERS), the less likely it is to get a low mean sample value, say "1." For NUM-MESSENGERS = 10, you would get a "1" only if all the messengers chose "1" randomly at the same time. That happens once every 100^10, which is "1" with 1000 zeros after it. That's pretty rare...

## EXTENDING THE MODEL

In the model, the bricks laid by the messengers are NetLogo "turtles".  However, in NetLogo, the more turtles are visible, the longer it takes to update the display. Thus, the closer the display gets to being full of bricks, the slower the model runs.  To eliminate this problem, one may want to use colored NetLogo "patches", rather than "turtles", to represent the bricks in the display histogram.

Currently, the brick columns in the display show the absolute height of each column --- not the proportionate height of the columns.  This means that once a column is considerably taller than other columns, these other columns may be shown as empty.  They are not tall enough to enter the display --- they are "under water."  We have chosen this design option, because we think it helps users "get into" the model when they first start working with it. But you might want to change this or add an option that gives you the proportionate height of the columns.  For a clue on how to make this work, take a look at the code of the Rugby model.

## NETLOGO FEATURES

The `display` command is used to make the motion of the messengers animate.

## RELATED MODELS

See the ProbLab model Random Basic. In that model, there is only a single messenger. This model extends Random Basic in that we now have compound events, that is, we look to understand the chances of two or more independent events occurring simultaneously.

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

brick
false
0
Rectangle -16777216 true false -1 1 15 301
Rectangle -16777216 true false 1 -5 303 16
Rectangle -16777216 true false 285 0 315 305
Rectangle -16777216 true false 0 285 297 299
Rectangle -16777216 true false 13 10 30 292
Rectangle -16777216 true false 9 31 11 32
Rectangle -16777216 true false 16 8 293 30
Rectangle -16777216 true false 270 23 299 292
Rectangle -16777216 true false 14 273 15 274
Rectangle -16777216 true false 15 272 16 272
Rectangle -16777216 true false 15 271 290 289

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

messenger
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250
Rectangle -7500403 false true 141 69 156 84
Rectangle -7500403 false true 144 83 159 98
Rectangle -16777216 false false 142 83 160 102

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
random-seed 0 set sample-space 50 set height 35 setup repeat 100 [ go ]
place-messengers-and-choose-random-values
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

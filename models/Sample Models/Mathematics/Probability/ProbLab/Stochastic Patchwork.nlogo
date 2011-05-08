globals [target-color other-color background-color patches-in-block %-target-color-list]
patches-own [my-color]

breed [ frames frame ]

to startup
  initialize
  checker
end

to initialize
  clear-all
  set-colors
  set %-target-color-list []
  set patches-in-block []
  reset-ticks
end

;; We used color variables and not just colors so that you could change the color values
;; to suit you. For instance, perhaps you would want to change the green to violet?
to set-colors
  set target-color green
  set other-color blue
  set background-color 44
end

;; This procedure checkers the patches.
;; This procedure's function is (a) to give the user a sense of the layout of the land
;; (b) aesthetic. You can copy this basic code to your own models.
to checker
  ask patches
  [
    ifelse  ( ( pxcor / 2  = int (pxcor / 2) ) and ( pycor / 2  = int (pycor / 2) ) ) or
    ( ( pxcor / 2 != int (pxcor / 2) ) and ( pycor / 2 != int (pycor / 2) ) )

      [set pcolor target-color]
      [set pcolor other-color]
  ]
end

to setup
  initialize
  create-block
  setup-frames
end

to create-block ;; creates a block of all patches-in-block
  ask patch 0 0
  [set patches-in-block ( in-rectangle patches ((block-side - 1) / 2)
        ((block-side - 1) / 2) ) ]
  ask patches [set pcolor brown]
  ask patches-in-block [set pcolor background-color]
end

;; report the agentset within a box that has a width = (half-width * 2) + 1
;; and a height = (half-height * 2) + 1 and is centered on the calling agent
to-report in-rectangle [patchset half-width half-height]
  report patchset with
  [
    pxcor <= ([pxcor] of myself + half-width) and pxcor >= ([pxcor] of myself - half-width) and
    pycor <= ([pycor] of myself + half-height) and pycor >= ([pycor] of myself - half-height)
  ]
end

to setup-frames ;; each individual patch in the block is framed, creating an overall effect
  ;; of a grid that helps distinguish individual patches in the block
  set-default-shape frames "frame"
  ask patches-in-block
  [
    sprout-frames 1
      [ set color black ]
  ]
end

to go
  if target-color = 0 [stop]
  ask patches-in-block
  [
    ifelse random-float 100 < %-target-color
      [set pcolor target-color ] [set pcolor other-color]
  ]

  ;; We multiply by 100 in order to convert a probability, e.g., .23, into a percent -- 23.
  set %-target-color-list fput ( 100 * ( count patches-in-block with [pcolor = target-color] )
      / count patches-in-block )
  %-target-color-list
  tick
  plot-color-distribution
end

to plot-color-distribution
  set-current-plot "Color Distribution"
  set-current-plot-pen "%-target-color"
  histogram %-target-color-list
  let maxbar modes %-target-color-list
  let maxrange filter [ ? = item 0 maxbar ] %-target-color-list
  set-plot-y-range 0 length maxrange
end
@#$#@#$#@
GRAPHICS-WINDOW
309
10
676
398
10
10
17.0
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
30.0

SLIDER
9
36
296
69
block-side
block-side
1
17
3
2
1
NIL
HORIZONTAL

SLIDER
33
324
297
357
%-target-color
%-target-color
0
100
50
1
1
%
HORIZONTAL

BUTTON
9
73
72
120
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
233
73
296
120
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

MONITOR
111
81
204
126
# target color
count patches-in-block with [pcolor = target-color]
3
1
11

PLOT
14
152
297
321
color distribution
% target color
occurrences
0.0
101.0
0.0
10.0
true
false
"" ""
PENS
"%-target-color" 0.0010 1 -10899396 true "" ""

@#$#@#$#@
## WHAT IS IT?

Stochastic Patchwork is a simple model for thinking about a profound idea in the domain of probability: the relation between, on the one hand, the independent chance of  elements to take on the value of a favored event, and on the other hand, the distribution of means of grouped outcomes, that is 'samples.'  Should the distribution of sample means reflect the independent probability?  If so, why?  In this dynamic model, a histogram representing the accumulating distribution of sample means grows before our eyes.

This model is a part of the ProbLab curriculum.  The ProbLab curriculum is currently under development at the CCL.  For more information about the ProbLab curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

Squares that participate in the sample are those that are in the block you have set up. For instance, for a block-side of 3 you get a "9-block." At every run through GO, each square in the block "rolls a die" to decide whether it should be green (the favored event) or blue. This "die" works as follows: The square chooses a random number between 0 and 100. Next, the square checks to see whether this number is smaller or larger than the value you set for the "%-target-color" slider. If, for instance, the random number was 75 and the slider value was 85, then the square will become green, but if -- say on the immediately consecutive run through Go -- the random number happened to be 95 (and you have not changed the slider value from 85), then the square will be blue on this run. At every run through Go, the overall value of the population's "green-ness" is calculated as the number of green squares in the block divided by the total number of squares in the block. This calculated value is added to a growing list that thus grows in length from trial to trial. The entire list is plotted as a histogram at each run.

Over many runs, the histogram begins to form a bell-shaped curve with your chosen %-target-color as the mode.

Note that for this model, different possible permutations of a given block that all have the same number of green squares in them are considered the same (compound) 'event' and are thus clumped in the same histogram bar.

Note also the ambiguity of the statistic 'greenness.' If 21/25 squares in a block are green, we can say that the greenness of this sample block is 84%. But we also can say that in this sample block, each square is on average .84 green. That is the sample mean. Now, .84 green possibly does not make much sense here, because each square is either completely green or not green at all. But in terms of prediction, one can speak of being .84 sure that a square in the population is totally green.

## HOW TO USE IT

Buttons:  
'Setup' - prepares the size of population according to the block-side slider value.  
'Go'  - activates the procedures. It is set to work "forever," that is, repeatedly until you press it again.

Sliders:  
'block-side' - the larger the value you set here, the larger the size of your square sample. If the block-side is X, then the block will be of dimensions X^2. For example, a block-side of 3 will give a sample of 9 square squares.  
'%-target-color' - Use this slider to control the average probability that each square will be green in each iteration through Go.

Monitors:  
'# target color' - shows how many squares in the block are green. (Note that the plot window shows a histogram of the percent of green squares, and not of the number of green squares.)

Set the sliders to the values of your choice, press Setup, and then press Go.

## THINGS TO NOTICE

The larger your block, the more possible sample means there are for samples, so the histogram becomes denser (but check the cases of 0% and 100%). For instance, for a sample of 9 squares (block-side = 3) there are 10 different events: 0 out of 9 are green, 1 out of 9 are green, 2 out of 9 are green, 3/9 are green, 4/9, 5/9, 6/9, 7/9, 8/9, and 9/9. But for a sample of 25 squares (block-side = 5) there are 26 different outcomes: 0/25, 1/25, 2/25...24/25, 25/25. Try running the model under the settings block-side 3 and then 5 to appreciate this difference. Interestingly, if you were working with a line graph and not a histogram, you would never see this difference. To try this, you can edit the plot by selecting it and clicking the EDIT button in the Toolbar. Now change the plot pen's mode from 'Bar' to 'Line,' and run the model again. Another way to notice the difference between small and large samples is to see how many different sample means are accumulating into the list of all sample means. Run the model with a block-side 3 for about 10 seconds, and then, in the command center, write "show %-target-color-list." Notice how there are 9 different outcomes. You can also type in:

    show length remove-duplicates %-target-color-list

This will report the number of different outcomes in the list. Compare this to a sample of block-side 5.

## THINGS TO TRY

OK, so we know that a sample of 9 squares gives 10 different events (see previous section). But what happens if you change the %-target-color value? Surely that should change the number of different possible events! Or does it?... How many samples do we need to take to check our answer? Can we ever be sure of our answer? If you have an opportunity, run this model overnight and track for any changes in the distribution range. Otherwise, you should consult an expert on this issue.

How long does it take for the histogram to start taking its typical bell shape? How is this duration of time related, if at all, to the size of the sample?

How is the shape of the curve related to the %-target-color value? Choose a largish block-side value, then set the %-target-color slider to an extremely high value, such as 98%. Let the model run until it begins to take some distinctive shape. Now, stop the model by pressing the Go button again. Then, without pressing Setup, re-set the %-target-color slider to, say, 70% and let it run again. Repeat this several times for smaller and smaller percentages, ending with another extreme percentage, say 2%. You will have a set of curves. Are they all bell shaped? -- If not, why not?

When the block-side is 3 and we run the model, then pretty soon -- that is, after a few thousand trials -- we have 10 bars. That means we have had each of the 10 possible events at least once. So the distribution of outcomes ranges from 0 to 100. How about the case of block-side 7? Will we get the same distribution range?

Ask a friend to set up the %-target-color value for you without your looking. Now, cover the left-hand side of the NetLogo Window (so that you see only the view). You can also do that by asking your friend to shift the entire NetLogo window to the left so that the left-hand side of the window is hidden off screen.(Or, move the slider out of sight and type in the command center "set %-target-color random 101") Next, run the model. Looking at the flashing colors on the sample block, try to guess the percentage of green. Practice this, then switch roles with your friend. Who is better at this task? Discuss your strategies. (see 'Experts' in the EXTENDING section.)

## EXTENDING THE MODEL

A first jab at programming is to change the two focal colors from green and blue to whatever you like. In the Code tab, find the `set-colors` procedures and replace the existing colors with others. You may wish to consult the NetLogo manual (from the "Help" menu) to find out which colors can be named and which have to be numbered. Also, you can use a combination such as `violet + 3`.

A bigger challenge, that would involve some NetLogo programming, is to find and represent interesting patterns in the data that is currently displayed in the existing plot, COLOR DISTRIBUTION. What other variables could be interesting to monitor? For instance, you can monitor how many trials you have had. For this, you want a monitor that will show the length of the list that keeps all the samples. Also, you can add a monitor that shows the average probability, or the occurrence of a particular value, such as 0%. Create monitors that keep track of the distribution range (maximum - minimum). You could create another plot that graphs the average sample mean as it changes over time. What do you expect that graph would look like --- initially, later? How, if at all, would the shape of that graph be related to the settings of the slider values?

Experts (continued from above section): Try extending the model so as to be able to improve your guesstimation strategies. You can even create interactive procedures that will enable you to track your friend's and your progress. For instance, you could modify the model to assign random values to the %-target-color slider without showing you the value. Perhaps it is worthwhile to find ways to make your "guesstimations" more precise. What could such techniques be? How are they related to what scientists call "doing statistics"? Finally, what can we learn about our perceptual mechanism (the way our eyes and brain work together) from this exercise?

## NETLOGO FEATURES

Look in the code at the procedure `create-block` and the reporter procedure `in-rectangle` immediately below it. Note how adjustments need to be made in order to get the code to produce a block of squares with the `block-side` the user sets. You may wish to familiarize yourself with what might seem at first confusing: Even though the squares create a grid-like structure, this grid is offset from the underlying coordinate system that gives squares their names. This real coordinate system runs through the squares' centers and not along their perimeters. For instance, the location of `patch 0 0` is precisely that --- [0 0] --- because its center, that is, its `pxcor` and `pycor`, is on the origin. But in fact, the patch extends from -0.5 to +0.5 along both the x and the y axes.

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
14
Rectangle -16777216 true true -8 2 18 298
Rectangle -16777216 true true 1 0 300 16
Rectangle -16777216 true true 283 2 299 300
Rectangle -16777216 true true 1 285 300 299

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
set block-side 7
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

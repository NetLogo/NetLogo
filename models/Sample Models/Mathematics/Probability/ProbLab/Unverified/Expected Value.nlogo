globals [
  ratio-list ; list of ratio-unit sliders
  my-sample-size ; keeps track of the original sample-size
  #patches ;; keeps the number of patches, so don't have to recalculate all the time

  ;; outputs that will be printed in the monitors
  expected-value-calculation
  expected-value
  ratio-monitor-list ; the "% in Population" monitors pick items from ratio-monitor-list

  the-little-guy ; wanders around picking tiles to turn over

  all-totals ; list of the totals from each run

  colors ; list of the colors given for each number of marbles

  target-color ; the default base color of the display

  little-guy-color ; the color of the little-guy
]

patches-own [ value index ] ; how many marbles are under the tile

breed [ outlines outline ]
breed [ little-guys little-guy ]

little-guys-own [ values ] ; list of numbers in the running total of the sample

to startup
  set expected-value-calculation ""
end

to setup
  ca
  set expected-value-calculation ""
  setup-misc-globals ; collects ratio-unit sliders, etc.
  ;;checks for all 0s
  if sum ratio-list = 0 [ user-message "Cannot set all ratio-units sliders to 0"  stop ]
  setup-patches ; sets the values and colors of the patches
  setup-little-guy
  setup-monitors ; sets the global variables the monitors display
  set all-totals lput ( [sum values] of the-little-guy ) all-totals
  reset-ticks
end

to go
  clear-selection
  ask little-guys
  [
    get-ready-for-next-sample
    repeat my-sample-size
    [
      set values ( lput value values )
      select-patch
      let prev-heading heading
      fd 1
      rt one-of [ -90 0 90 ]
    ]
  ]
  set all-totals lput ( [sum values] of the-little-guy ) all-totals
  tick
end

;; collects ratio-unit sliders, sets colors, etc.
to setup-misc-globals
  set ratio-list ( list ratio-0-units
                        ratio-1-units
                        ratio-2-units
                        ratio-3-units
                        ratio-4-units
                        ratio-5-units
                        ratio-6-units )

  set my-sample-size sample-size
  set #patches count patches

  set all-totals []

  set little-guy-color 94

  ;; sets the range of patch colors from dark to light
  set target-color green
  set colors ( list (target-color - 4.5)
                    (target-color - 3)
                    (target-color - 1.5)
                     target-color
                    (target-color + 1.5)
                    (target-color + 3)
                    (target-color + 4.5) )
end

;; sets the values and colors of the patches
to setup-patches
  ;;makes all patches have no value
  ask patches [ set value -1 ]
  let cur-val 0

  ;;finds the number of patches of each type that need to be printed:
  ;;makes the percentages, multiplies them by the number of patches
  let list-of-nums map [? / (sum ratio-list) * #patches] ratio-list
  ;;rounds the numbers of patches with a specific value
  let r-list-of-nums map [round ?] list-of-nums

  ;;tweaks the values, either in the positive or in the negative direction,
  ;;for the sum to equal to the number of patches on the display
  let nums setup-patches-polish list-of-nums r-list-of-nums

  ;;prints out the values on random patches that are not already taken
  foreach nums
  [
    ask n-of ? patches with [ value = -1 ]
    [ set value cur-val ]
    set cur-val cur-val + 1
  ]

  ;;if population to be shown, shows in block, then scrambles
  ifelse setup-apart?
  [
    print-out nums
    wait 2
    repeat (#patches / 4) [ disperse ]
    ask patches [ set value position pcolor colors ]
  ]
  ;;colors the patches from their given values
  [ recolor-patches ]
end

;;tweaks the values so that they add up to the number of patches
;;(in the positive or negative direction)
to-report setup-patches-polish [ list-of-nums r-list-of-nums ]
  ;;tweaks the values, either in the positive or in the negative direction,
  ;;for the sum to equal to the number of patches on the display:
  ;;if the sum is below the number of patches, then searches for the highest remainder to round up
  while [sum r-list-of-nums < #patches]
  [
    let remainders map [remainder (? * #patches) #patches] list-of-nums
    ;;finds the position of the maximum remainder
    let pos-of-max position (max remainders) remainders
    ;;updates the list of numbers of each type of patch
    set r-list-of-nums (replace-item pos-of-max r-list-of-nums ((item pos-of-max r-list-of-nums) + 1))
    set list-of-nums (replace-item pos-of-max list-of-nums (floor (item pos-of-max list-of-nums) + 1))
  ]
  ;;if the sum is above the number of patches, then searches for the lowest remainder to round up
  ;;to prevent a bug that would make 0 always be the minimum remainder, all the 0's are changed
  ;;to large numbers, for them to not become negative
  while [sum r-list-of-nums > #patches]
  [
    let remainders map [remainder (? * #patches) #patches ] list-of-nums
    ;;remainders1 makes sure that the 0 is not deemed the minimum remainder, but
    ;;looks for minimum remainder above 0
    let remainders1 []
    foreach remainders
    [
      ifelse ? = 0
      [ set remainders1 (lput #patches remainders1) ]
      [ set remainders1 (lput ? remainders1) ]
    ]
    ;;finds the position of the minimum remainder
    let pos-of-min position (min remainders1) remainders1
    ;;updates the list of numbers of each type of patch
    set r-list-of-nums (replace-item pos-of-min r-list-of-nums ((item pos-of-min r-list-of-nums) - 1))
    set list-of-nums (replace-item pos-of-min list-of-nums (floor (item pos-of-min list-of-nums)))
  ]
  report r-list-of-nums
end

;;shows the populations separately
to print-out [ list-of-nums ]
  ;;indexes the patches (from left to right, down to up), if had not already done so
  ask patches [ set index ((pxcor + max-pxcor) * (max-pxcor * 2 + 1) + (pycor + max-pycor)) ]

  ;;shows the separated populations
  let patch-now 0
  let temp 0
  foreach list-of-nums
  [
    repeat ?
    [
      ask patches with [index = patch-now] [ set pcolor item temp colors ]
      set patch-now patch-now + 1
    ]
    set temp temp + 1
  ]
end

;;asks all the patches to switch its color with one of its neighbors
to disperse
  ask patches
  [
    let other-patch one-of neighbors4
    let temp-color pcolor
    set pcolor [pcolor] of other-patch
    ask other-patch [ set pcolor temp-color ]
  ]
end

;;colors the patches according to their value
to recolor-patches
  ifelse black-out? [ ask patches [ set pcolor 0 ] ] [ ask patches [ set pcolor item value colors ] ]
end

to setup-little-guy
  create-little-guys 1 [
    hide-turtle
    set color little-guy-color
    set shape "person"
    move-to one-of patches
    face one-of neighbors4
    set values []
    show-turtle
  ]
  set the-little-guy one-of little-guys
end

;; sets the global variables the monitors display
to setup-monitors
  ;; the "% in Population" monitors pick items from ratio-monitor-list
  set ratio-monitor-list map [(word ? "/" (sum ratio-list) " = "
                                    precision (100 * ? / (sum ratio-list)) 1
                                    "%")]
                             ratio-list
  set expected-value  my-sample-size * sum map [ ? * item ? ratio-list / (sum ratio-list) ]
                                           [ 0 1 2 3 4 5 6 ]
  set expected-value-calculation word my-sample-size " * ("
  foreach [ 0 1 2 3 4 5 ] [
    set expected-value-calculation (word expected-value-calculation
                                         ? " * " item ? ratio-list "/" (sum ratio-list) " + ")
  ]
  ;; we separated out the "6 case" because we don't add a "+", but we do add a ") ="
  set expected-value-calculation (word expected-value-calculation
                                       6 " * " item 6 ratio-list "/" (sum ratio-list) ") = ")
end


;; clears the red from the patches chosen before
to clear-selection
  recolor-patches
  ask turtles with [ breed = outlines ] [ die ]
end

to get-ready-for-next-sample ;; little-guy procedure
  slide-to-random-patch
  face one-of neighbors4
  set values []
end

to slide-to-random-patch ;; little-guy procedure
  let random-patch one-of patches
  while [ patch-here != random-patch ] [
    face random-patch
    fd 1
  ]
  setxy pxcor pycor
end

;; highlights the point given to it
to select-patch ;; little-guy procedure
  let shapes-list (list "outline"
                        "outline1"
                        "outline2"
                        "outline3"
                        "outline4"
                        "outline5"
                        "outline6")
  ask patch-here
  [
    set pcolor item value colors
    sprout 1
    [
      set breed outlines
      ;; set the shape and label correctly.
      ifelse die-nums?
      [
        ;; if die-nums? the shape holds the value and there are no labels
        set shape (item value shapes-list)
      ]
      [
        ;; if not die-nums? the shape doesn't hold the label, so set the label
        set shape "outline"
        ;; sets label color to be opposite color in the list of colors
        set label-color item ((value + 2) mod length colors) colors
        ;; the " " on the end pushes the label into the middle of the patch
        set label word value " "
      ]
    ]
  ]
end

to plot-vert-line [ x-coord ]
  plotxy x-coord plot-y-min
  plot-pen-down
  plotxy x-coord plot-y-max
  plot-pen-up
end
@#$#@#$#@
GRAPHICS-WINDOW
253
64
728
560
7
7
31.0
1
20
1
1
1
0
1
1
1
-7
7
-7
7
1
1
1
ticks

TEXTBOX
73
310
123
329
Regular
11
0.0
0

TEXTBOX
73
160
119
178
Darkest
11
0.0
0

TEXTBOX
73
212
119
231
Darker
11
0.0
0

TEXTBOX
73
260
126
280
Darkish
11
0.0
0

TEXTBOX
73
360
126
379
Lightish
11
0.0
0

TEXTBOX
73
410
121
429
Lighter
11
0.0
0

TEXTBOX
73
462
123
482
Lightest
11
0.0
0

BUTTON
4
12
121
48
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

SLIDER
3
181
120
214
ratio-1-units
ratio-1-units
0
10
1
1
1
NIL
HORIZONTAL

SLIDER
4
229
120
262
ratio-2-units
ratio-2-units
0
10
6
1
1
NIL
HORIZONTAL

SLIDER
3
279
120
312
ratio-3-units
ratio-3-units
0
10
5
1
1
NIL
HORIZONTAL

SLIDER
3
329
119
362
ratio-4-units
ratio-4-units
0
10
0
1
1
NIL
HORIZONTAL

SLIDER
4
379
120
412
ratio-5-units
ratio-5-units
0
10
4
1
1
NIL
HORIZONTAL

MONITOR
845
12
958
57
Mean of Samples
sum all-totals / length all-totals
2
1
11

MONITOR
128
171
248
216
% in Population
item 1 ratio-monitor-list
0
1
11

MONITOR
128
221
248
266
% in Population
item 2 ratio-monitor-list
0
1
11

MONITOR
128
271
248
316
% in Population
item 3 ratio-monitor-list
0
1
11

MONITOR
128
321
248
366
% in Population
item 4 ratio-monitor-list
0
1
11

MONITOR
128
371
248
416
% in Population
item 5 ratio-monitor-list
0
1
11

SWITCH
5
527
122
560
black-out?
black-out?
1
1
-1000

SWITCH
5
488
122
521
die-nums?
die-nums?
1
1
-1000

MONITOR
844
336
959
381
This Sample Total
[sum values] of the-little-guy
0
1
11

MONITOR
731
336
841
381
# of Runs
length all-totals
0
1
11

SLIDER
128
488
248
521
sample-size
sample-size
1
20
10
1
1
NIL
HORIZONTAL

BUTTON
127
50
247
86
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

BUTTON
4
50
121
86
Clear Selection
clear-selection
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
21
94
232
122
Note: Press SETUP to apply slider changes
11
0.0
0

PLOT
731
389
959
560
Sample Totals
NIL
NIL
20.0
40.0
0.0
10.0
true
false
"" ";; changes range\nif length all-totals > 0 and length all-totals mod 20 = 0 [\n  set-plot-x-range min all-totals max all-totals + 1\n]"
PENS
"Sample Count" 1.0 1 -16777216 true "" "plot-pen-reset\nhistogram all-totals\nlet maxbar modes all-totals\nlet maxrange filter [ ? = item 0 maxbar ] all-totals\nset-plot-y-range 0 length maxrange"
"Expected Value" 1.0 0 -2674135 true "plot-vert-line expected-value" "plot-pen-reset\nplot-vert-line expected-value"
"Mean" 1.0 0 -10899396 true "" "plot-pen-reset\nplot-vert-line ((sum all-totals) / (length all-totals))"

MONITOR
731
12
842
57
Expected Value
expected-value
3
1
11

PLOT
731
64
958
331
Cumulative Mean
NIL
NIL
0.0
10.0
20.0
50.0
true
false
";; makes a line on the graph, signifying the theoretical expected value\nset-plot-y-range round expected-value - 10  round expected-value + 10" ""
PENS
"Average" 1.0 0 -16777216 true "" "plot sum all-totals / length all-totals"
"Expected Value" 1.0 0 -2674135 true "plotxy plot-x-min expected-value\nplotxy plot-x-max expected-value" "if length all-totals > 10 [ plot expected-value ]"

SLIDER
3
129
120
162
ratio-0-units
ratio-0-units
0
10
0
1
1
NIL
HORIZONTAL

MONITOR
128
121
248
166
% in Population
item 0 ratio-monitor-list
0
1
11

SLIDER
4
431
120
464
ratio-6-units
ratio-6-units
0
10
0
1
1
NIL
HORIZONTAL

MONITOR
128
421
248
466
% in Population
item 6 ratio-monitor-list
0
1
11

MONITOR
253
12
728
57
Expected Value Calculation
expected-value-calculation
0
1
11

BUTTON
127
12
247
48
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
129
527
248
560
setup-apart?
setup-apart?
0
1
-1000

@#$#@#$#@
## WHAT IS IT?

In this model you run experiments that demonstrate the mathematical idea "expected value" (sometimes called "expectation value"). There is a set of different possible outcomes, and each of these outcomes has a different value. The model predicts the expected value based on the probabilities of each of these outcomes. The user can then take samples from the population and compare them to the values predicted by the model.

This model is a part of the ProbLab curriculum. The ProbLab Curriculum is currently under development at the CCL. For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## PEDAGOGICAL NOTE

Expected-value analyses look at the 'value' of outcomes in probability experiments in terms of some utilitarian framework, such as money or points. As in life, some events are more significant to us, not just because they happen rarely or often. In that sense, expected-value simulations go beyond looking just at chance -- they introduces a 'worth' factor (weight, coefficient) associated with each type of experimental outcome. That is, these simulations examine not only the issue of what I should expect, but also, what it would be worth for me.  For instance, if I draw 12 coins randomly from a sack containing an equal number of pennies, nickels, dimes, and quarters, we could talk about which 12 'coins' I could expect to get (a nominal classification) -- 3 'pennies,' 3 'nickels,' 3 'dimes,' and 3 'quarters,' on average. But we could extend the discussion to how much money I would get, that is, what would be the 'value' of my draw. To do this, we'd have to multiply the expected frequency of each type of coin by its value:
3 * 1 + 3 * 5 + 3 * 10 + 3 * 25 = 123 cents.

A more general way of putting this all together -- the sample size (12), the probabilities of each coin (1/4 for each of them), and the value of each coin (1, 5, 10, and 25) -- is:
12 * (1 * 1/4 + 5 * 1/4 + 10 * 1/4 + 25 * 1/4) = 123 cents.

Thus, the term 'expected value' may be defined as the sum of the products of the value of each possible event and the probability of that possible event.

## HOW IT WORKS

The analogy utilized for the model is one of a tiled playground, with a certain number of marbles beneath each tile (0, 1, 2, 3, 4, 5 or 6 marbles).  A wandering kid flips up some tiles at every go and counts up the marbles found beneath the tiles. That's a "sample."  The kid cannot see the color of the tiles -- the colors are for us.

The idea of 'expected value' is that if we know how many tiles have 0, 1, 2,... or 6 marbles beneath them, then we can formulate an educated guess of how many marbles the wandering kid will find, on average. It's similar to asking, 'How many marbles are there on average under each tile?' We need to somehow take into account the chance of getting each one of the marble sets.

In the setting-up phase of using this model, you get to set up the relative number of tiles hiding 0 marbles, 1 marble, 2 marbles, ... and 6 marbles. We set the ratio between the number of tiles hiding each set of marbles. The computer program will do much of the calculations for us, but here's the gist of what it does:

Let's say that we set the sliders to the following ratio units: 0 : 1 : 6 : 5 : 0 : 4 : 0.

The number '5,' for example, indicates our value setting for marble sets of exactly 3 marbles. You can see immediately that the chance of getting a '2' (6 ratio units) is greater than the chance of getting a '3' (5 ratio units). But in order to determine just how big the chance is of getting each marble set, we need to state the ratio units relative to each other. We need a common denominator. In this particular setting, there is a total of 16 'ratio units': 0 + 1 + 6 + 5 + 0 + 4 + 0 = 16.  Now we can say, for instance, that there is a 4/16 chance of getting a 5-marble set. That is a 25% chance of striking upon a tile that has exactly 5 marbles beneath it. We can also say that these sets of 5 marbles contribute .25 * 5, that is, 1.25 marbles, to the overall average marble-value of a single tile in the playground. Similarly, we can say there is a 5/16 chance of getting a set of 3, a 0/16 chance of getting 6-marble set, etc. If we sum up all pairs of 'value' and 'probability,' we get:
(0 * 0/16) + (1 * 1/16) + (2 * 6/16) + (3 * 5/16) + (4 * 0/16) + (5 * 4/16) + (6 * 0/16) = 48/16 = 3 marbles per tile.

This tells us that on any single pick within these settings, you should expect to find, on average, 3 marbles. "3" is our expected value. Thus, if you were to flip over 10 tiles, you should expect to find a total of 30 marbles.

## HOW TO USE IT

Begin by choosing ratios of marble sets (or just use the default settings). Click SETUP, and watch the information updated in the view. Below are more features of the models that will let you change the way it looks and runs.

Sliders:
RATIO-0-UNITS, RATIO-1-UNITS, RATIO-2-UNITS, RATIO-3-UNITS, RATIO-4-UNITS, RATIO-5-UNITS, RATIO-6-UNITS -- settings for the distribution of 'marble sets' (= 'points'). Note that the ratios are set in proportion to each other (See 'THINGS TO TRY'). (The labels under the sliders let the user know that the highest value is the lightest tile color, while the lowest value is the darkest tile color.)

SAMPLE-SIZE -- sets the number of tiles selected by the wandering kid at each run.  Note that the kid can turn over the same tile more than once. This won't be visible in the view.

Switches:
SETUP-APART? -- if 'On,' when SETUP is pressed, the display initially separates the population by point value, to illustrate the relative number of patches of different values.  After a 3 second pause, the model scrambles the populations, producing a randomized display.

DIE-NUMS? -- if 'On,' you will see the marbles and not only colors, when the tiles are flipped over; if 'Off,' you'll see the colors and the numerical value assigned to them.

BLACK-OUT? -- if 'On,' you will only see the tiles the kid selected in the present sample; if 'Off,' you will always see all the tiles. In any case, the kid does not see the colors. The 'black-out' feature makes this model more coherent with S.A.M.P.L.E.R., a participatory simulation in ProbLab.

Buttons:
SETUP -- initializes variables, re-colors the tiles, and resets the monitors and the graphs.

GO ONCE -- picks a starting point randomly among the tiles in the view, sends the kid for a single run over random tiles, counts up the total number of marbles found, and graphs this total, that is, 'bumps up' the histogram at the corresponding value.

CLEAR SELECTION -- re-covers the tiles that have been flipped (does not re-initialize variables).

GO -- does everything that the GO ONCE button does, but over and over, until turned off, that is, until you press on it again.

Monitors:
% IN POPULATION -- shows the distribution percentage of each marble-set in the population, including the calculation that illustrates how that percentage was obtained. For instance, if the RATIO-3-UNITS's % IN POPULATION monitor is at 50%, then roughly half of all the tiles will have exactly 3 marbles beneath them.

EXPECTATION VALUE CALCULATION -- This monitor shows the calculation of the expected number of marbles that the kid will find beneath all the tiles the kid flips in a sample. The calculation first determines the value of the 'average tile' and then multiplies this value by the number of tiles in the kid's sample (how many tiles the kid flips). Thus, 'average value' * 'sample-size' = total expected value of sample. The formula for the expectation calculation is:
sample-size * (0 * (ratio-0-units / ratios-total) + 1 * (ratio-1-units / ratios-total) + 2 * (ratio-2-units / ratios-total) + 3 * (ratio-3-units / ratios-total) + 4 * (ratio-4-units / ratios-total) + 5 * (ratio-5-units / ratios-total) + 6 * (ratio-6-units / ratios-total))

EXPECTED VALUE -- shows the result of the EXPECTED VALUE CALCULATION

MEAN OF SAMPLES-- monitors the cumulative average number of points obtained in a sample (so it keeps updating for each sample).

\# OF RUNS -- monitors the number of times the tiles were selected and counted as a sample.

POINT TOTAL -- monitors the total number of marbles in the current sample. (updates the total as each tile is selected).

Plots:
SAMPLE TOTALS -- plots the frequency of each point-total of a run.  The green vertical line represents the average sample total, while the red vertical line represents the expected sample total.

CUMULATIVE MEAN -- plots the average number of points obtained in a single run in black against the expected value in red.

## THINGS TO NOTICE

If the model is slowed down, using the bar on top of the display, one may see that the sample total is added on every time that the wandering kid selects another tile. Thus, one may monitor the running total for the current run of the kid.

A feature of this model is that the wandering kid may resample the same tiles during a single run. While the kid cannot go back to a tile it just selected, it can potentially go in a circle and come back to such a tile. This may skew the average away from the expected value, which does not account for such re-sampling (see 'EXTENDING THE MODEL,' below).

## THINGS TO TRY

Run the model under different setting of the three switches.  Why does DIE-NUMS? help
one visualize the model better in some situations and under some settings, while not
in others?

The relative ratios of the various marble sets are determined by relative values of all of the sliders on the left.  In other words, if one sets RATIO-0-UNIT : RATIO-2-UNITS : ..., : RATIO-6-UNITS to 1 : 1 : 1 : 1 : 1 : 1 : 1, it is going to mean the same to the computer as the setting of 2 : 2 : 2 : 2 : 2 : 2 : 2 or 5 : 5 : 5 : 5 : 5 : 5 : 5.  The sliders are designed to allow an exploration of a rich range of proportions of the different marble sets.  Try extending the maximum value of the RATIO sliders, to obtain a richer range of proportions of marble sets.

Change the target-color to some color other than green. Change the 'little-guy-color,' which alters the color of the wandering kid.

Set the ratio-0-units to 0. Set all the other ratio sliders to some value, for instance 2. (It doesn't matter which, as long as they are the same.) Press SETUP. The model now simulates the sample space of dice, which do not have a '0,' and for which each 1-thru-6 value is equally likely to occur.

Set the ratios of the 0 and 1 to some non-zero value. Set the ratios of 2, 3, 4, 5 and 6 to 0. Press SETUP. This produces a sample space of a Boolean type ('true' or 'false'). This sample space would be coherent with the S.A.M.P.L.E.R. model of ProbLab, where tiles are either green (true) or blue (false), without in-between values.

Set the ratios of 5 and 6 to 0. Set the other ratios to non-zero values. Press SETUP. The model produces a sample space that could be seen as a tessellation of '4-blocks,' which appear in the 9-Block Stalagmite model of ProbLab. In that model, blocks have either 0, 1, 2, 3, or all 4 squares with the target-color.

## EXTENDING THE MODEL

Add and change code so that the wandering kid cannot re-sample within a single sample.

Add and change code so that the wandering kid cannot re-sample within a single run of many samples.

Add a monitor that show the % difference between the expected value and the mean of samples.

Add another unit-value slider. It could for 7, so you'd need to create a die shape with 7 dots on it and integrate that die shape and its name into the lists in the code. You could possibly create a 'negative value,' which could be interpreted as an added expense, like owing marbles.

## RELATED MODELS

This model is closely related to a variety of models in the ProbLab curriculum. As described in the 'THINGS TO TRY' section, one may set the ratios up so that they mirror the sample population of other models in the curriculum, including 9-Block Stalagmite and S.A.M.P.L.E.R. For instance, if one sets BLACK-OUT? to 'On,' this model would produce an effect that is similar to that of S.A.M.P.L.E.R., where one chooses a certain number of tiles and doesn't see any of the other tiles in the population.

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

outline
false
0
Rectangle -2674135 true false 0 -15 315 15
Rectangle -2674135 true false -15 0 15 375
Rectangle -2674135 true false 0 285 360 330
Rectangle -7500403 true true 300 -90 360 300
Rectangle -2674135 true false 285 -60 345 285

outline1
false
0
Rectangle -2674135 true false 0 0 30 300
Rectangle -2674135 true false 30 270 360 360
Rectangle -2674135 true false 270 -15 300 270
Rectangle -2674135 true false 15 0 270 30
Circle -2674135 true false 129 129 42

outline2
false
0
Rectangle -2674135 true false 0 0 30 300
Rectangle -2674135 true false 30 270 360 360
Rectangle -2674135 true false 270 -15 300 270
Rectangle -2674135 true false 15 0 270 30
Circle -2674135 true false 69 189 42
Circle -2674135 true false 189 69 42

outline3
false
0
Rectangle -2674135 true false 0 0 30 300
Rectangle -2674135 true false 30 270 360 360
Rectangle -2674135 true false 270 -15 300 270
Rectangle -2674135 true false 15 0 270 30
Circle -2674135 true false 69 189 42
Circle -2674135 true false 129 129 42
Circle -2674135 true false 189 69 42

outline4
false
0
Rectangle -2674135 true false 0 0 30 300
Rectangle -2674135 true false 30 270 360 360
Rectangle -2674135 true false 270 -15 300 270
Rectangle -2674135 true false 15 0 270 30
Circle -2674135 true false 69 69 42
Circle -2674135 true false 189 69 42
Circle -2674135 true false 69 189 42
Circle -2674135 true false 189 189 42

outline5
false
0
Rectangle -2674135 true false 0 0 30 300
Rectangle -2674135 true false 30 270 360 360
Rectangle -2674135 true false 270 -15 300 270
Rectangle -2674135 true false 15 0 270 30
Circle -2674135 true false 69 69 42
Circle -2674135 true false 189 69 42
Circle -2674135 true false 69 189 42
Circle -2674135 true false 189 189 42
Circle -2674135 true false 129 129 42

outline6
false
0
Rectangle -2674135 true false 0 0 30 300
Rectangle -2674135 true false 30 270 360 360
Rectangle -2674135 true false 270 -15 300 270
Rectangle -2674135 true false 15 0 270 30
Circle -2674135 true false 69 69 42
Circle -2674135 true false 189 69 42
Circle -2674135 true false 69 189 42
Circle -2674135 true false 189 189 42
Circle -2674135 true false 69 129 42
Circle -2674135 true false 189 129 42

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
need-to-manually-make-preview-for-this-model
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

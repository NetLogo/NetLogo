globals
[
  ;;current coordinates of the picked point
  picked-x picked-y

  ;;bounds (measures how many patches away from the picked point)
  ;;area = (dimension-left + dimension-right + 1) * (dimension-top + dimension-bottom + 1)
  dimension-left dimension-bottom dimension-right dimension-top

  ;;the list of all of the totals per sample (value)
  all-totals
  ;;the list of all of the "number of fish" per sample (number)
  all-numbers

  ;;the total in the current viewing area
  current-total

  ;;list of the colors given for each number of marbles
  colors

  ;;true if clicked before last clearing
  just-down?

  ;;stores values at setup for easier use in procedures
  ratio-list   #fish

  ;;this is a mathematical combination of slider settings to compare
  ;;against the current combination of slider settings to see if any of them had been changed
  setup-constant

  ;;for moving red lines at setup
  prev-exp-val  prev-squares
]

breed [ fish a-fish]

;;orig-color holds the fish's given color
;;selected? is true when the fush is currently selected
fish-own [ orig-color selected? ]

;;blue-color holds the patch's given color
patches-own [ index  blue-color ]

to startup
  setup
end

;;creates the fish, initializes variables and graphs
to setup
  clear-all

  setup-misc-globals
  setup-dimensions
  reset-ticks

  ;;makes background light blue
  ask patches [ set pcolor blue + 1 + random 3  set blue-color pcolor ]

  ;;creates the fish (# of fish = # of patches) and colors them
  create-fish #fish
  [
    pick-random-place
    setxy picked-x picked-y
    set size .75
    set selected? false
  ]
  setup-fish-color
  clear
end

;;sets the global variables to their initial values
to setup-misc-globals
  set-default-shape fish "fish-eye-fin"
  set all-totals []
  set all-numbers []
  set just-down? false

  set ratio-list (list $1-fish $2-fish $3-fish $4-fish $5-fish)
  let setup-values (sentence ratio-list height-of-sample width-of-sample)
  ;;calculates the setup-constant -- a combination of slider settings to compare against the current constant
  ;;it is a faster way of checking whether any slider settings have been changed
  set setup-constant sum (map [?1 ^ ?2] setup-values [1.21 1.22 1.23 1.24 1.25 1.26 1.27])
  set #fish count patches

  set prev-exp-val monitor-exp-val
  set prev-squares (width-of-sample * height-of-sample)

  ;;sets the colors to what they need to be (to show the value) [lighter = more valuable]
  let c-color green
  set colors (list (c-color - 3) (c-color - 1.5) c-color (c-color + 1.5) (c-color + 3))
end

;;if pressed, looks for a mouse-click in the view;
;;when clicked, selects area of the sample and
;;counts up the value and number of fish in the sample
to click-select
  ifelse mouse-down?
  [
    set just-down? true

    ;;sets the 'previous' variables
    let prev-x picked-x
    let prev-y picked-y

    ;sets currently-picked variables
    set picked-x [pxcor] of patch mouse-xcor mouse-ycor
    set picked-y [pycor] of patch mouse-xcor mouse-ycor

    ;;checks so point is different than one before
    ;;-- no holding mouse down and getting more than one selection
    if not (prev-x = picked-x and prev-y = picked-y)
    [
      ;;clears the view from previous click
      clear
      ;;appends the numbers for average, selects area in the view
      select-area
      set all-numbers lput (count fish with [ selected? = true ]) all-numbers
      set all-totals lput current-total all-totals
      ;;updates the histogram and plot
      plot-graphs
    ]
  ]
  ;;clear if no click and haven't cleared yet
  [
    if just-down? [ clear  set picked-x -1000 ]
  ]
end

;;randomly selects coordinates and runs the simulation
;;it is the computer's click-select, randomly choosing the picked point
to random-select
  ;;choose a random patch location
  set picked-x random-pxcor
  set picked-y random-pycor

  ;;selects the area and updates the numbers for average and plots
  select-area
  set all-numbers lput (count fish with [ selected? = true ]) all-numbers
  set all-totals lput current-total all-totals

  ;;clears the view from previous selection
  clear
  tick

  ;;updates the histogram and plot
  plot-graphs
end

;;picks a random point in the valid bounds
to pick-random-place
  set picked-x random-xcor
  set picked-y random-ycor
end

;;re-sets the patches and fish to original state
to clear
  ;;re-colors patches to lake color
  ask patches [ set pcolor blue-color ]

  ;;re-sets fish to original state
  ask fish [ set selected? false ]
  if show-val?
    [ ask fish [ set label "" ] ]
  ifelse not blind?
    [ ask fish [ set hidden? false ] ]
    [ ask fish [ set hidden? true ]  ]
end

;;selects the area, highlighting the patches with yellow
;;puts the fish's value as the label, if show-val? is 'On'
to select-area
  ;;re-sets current-total
  set current-total 0
  ;;colors patches gray
  ask patches [ set pcolor gray - 1 ]

  ;;loop that selects the area, using dimension-top, bottom, right, left
  ;;wrapping around is allowed
  let cur-y (- dimension-bottom)
  let cur-x (- dimension-left)
  ask patch picked-x picked-y
  [
    while [cur-y <= dimension-top]
    [
      set cur-x (- dimension-left)
      while [cur-x <= dimension-right]
      [
        ask (fish-at cur-x cur-y)
        [
          ;adds this fish's value to total
          set current-total current-total + position color colors + 1
          ;;shows the fishs value, if show-val? is set to 'On'
          if show-val?
          [
            set label-color red
            set label position color colors + 1
          ]
          set selected? true
          set hidden? false
        ]
        ask patch-at cur-x cur-y [ set pcolor yellow ]
        set cur-x cur-x + 1
      ]
      set cur-y cur-y + 1
    ]
  ]
  ;;hide turtles which are not selected
  ask fish with [ selected? = false ] [ set hidden? true ]
end

;;sets the colors of the fish
to setup-fish-color
  ;;makes all fish have no value
  ask fish [ set orig-color -1 ]
  let cur-val 0

  ;;finds the number of fish of each type that need to be colored:
  ;;makes the percentages, multiplies them by the number of fish
  let list-of-nums map [? / (sum ratio-list) * #fish] ratio-list
  ;;rounds the numbers of fish with a specific value
  let r-list-of-nums map [round ?] list-of-nums

  ;;tweaks the values, either in the positive or in the negative direction,
  ;;for the sum to equal to the number of fish on the display
  let nums setup-fish-color-polish list-of-nums r-list-of-nums

  ifelse not setup-apart?
  [
    ;;prints out the values on random fish that are not already taken
    foreach nums
    [
      ask n-of ? fish with [ orig-color = -1 ]
      [ set orig-color item cur-val colors ]
      set cur-val cur-val + 1
    ]
    ask fish [ set color orig-color ]
  ]
  [
    ;;shows fish apart, then moves them to new, random location
    histogram-patches nums
    ask fish [ die ]
    ask patches
    [
      sprout-fish 1 [ set size .75  set selected? false
                      set orig-color pcolor  set color pcolor ]
      set pcolor gray - 1
    ]
    display  wait 3
    ask patches [ set pcolor blue-color ]
    ;;disperses the fish
    swim-to-new
  ]
end

;;tweaks the values so that they add up to the number of fish
;;(in the positive or negative direction)
to-report setup-fish-color-polish [ list-of-nums r-list-of-nums ]
  ;;tweaks the values, either in the positive or in the negative direction,
  ;;for the sum to equal to the number of fish on the display:
  ;;if the sum is below the number of fish, then searches for the highest remainder to round up
  while [sum r-list-of-nums < #fish]
  [
    let remainders map [remainder (? * #fish) #fish] list-of-nums
    ;;finds the position of the maximum remainder
    let pos-of-max position (max remainders) remainders
    ;;updates the list of numbers of each type of fish
    set r-list-of-nums (replace-item pos-of-max r-list-of-nums ((item pos-of-max r-list-of-nums) + 1))
    set list-of-nums (replace-item pos-of-max list-of-nums (floor (item pos-of-max list-of-nums) + 1))
  ]
  ;;if the sum is above the number of fish, then searches for the lowest remainder to round up
  ;;to prevent a bug that would make 0 always be the minimum remainder, all the 0's are changed
  ;;to large numbers, for them to not become negative
  while [sum r-list-of-nums > #fish]
  [
    let remainders map [remainder (? * #fish) #fish ] list-of-nums
    ;;remainders1 makes sure that the 0 is not deemed the minimum remainder, but
    ;;looks for minimum remainder above 0
    let remainders1 []
    foreach remainders
    [
      ifelse ? = 0
      [ set remainders1 (lput #fish remainders1) ]
      [ set remainders1 (lput ? remainders1) ]
    ]
    ;;finds the position of the minimum remainder
    let pos-of-min position (min remainders1) remainders1
    ;;updates the list of numbers of each type of fish
    set r-list-of-nums (replace-item pos-of-min r-list-of-nums ((item pos-of-min r-list-of-nums) - 1))
    set list-of-nums (replace-item pos-of-min list-of-nums (floor (item pos-of-min list-of-nums)))
  ]
  report r-list-of-nums
end

;;sets the selecting area variables, given height and width
to setup-dimensions
  let h height-of-sample - 1
  let w width-of-sample - 1
  set dimension-left floor (w / 2)        ;sets how many patches on left
  set dimension-right floor (w / 2)       ;sets how many patches on bottom
  if (w mod 2 != 0) [ set dimension-right dimension-right + 1 ]
  set dimension-bottom floor (h / 2)      ;sets how many patches on right
  set dimension-top floor (h / 2)         ;sets how many patches on top
  if (h mod 2 != 0) [ set dimension-bottom dimension-bottom + 1 ]
end

;;fish method run by the [Wander] button
;;makes fish wander around aimlessly, if not currently selected
to wander-around
  every .5
  [
    if not selected?
    [
      ;;moves one, changes direction
      fd 1
      rt random 360
    ]
  ]
end

;;updates the histogram
to plot-graphs
  set-current-plot "Value per Sample"
  set-current-plot-pen "Count"
  ;;changes range
  if length all-totals > 5 and length all-totals mod 10 = 0
  [ set-plot-x-range (floor (min all-totals / 5)) * 5 (ceiling ((max all-totals + 1) / 5)) * 5 ]
  plot-pen-reset
  histogram all-totals
  let maxbar modes all-totals
  let maxrange length filter [ ? = item 0 maxbar ] all-totals
  set-plot-y-range 0 max list 10 maxrange
  ;;plots the "average" line
  set-current-plot-pen "Mean"
  plot-pen-reset
  plot-vert-line ((sum all-totals) / (length all-totals))
  ;;plots the expected value
  set-current-plot-pen "ExpVal"
  plot-pen-reset
  plot-vert-line monitor-exp-val

  set-current-plot "Number per Sample"
  set-current-plot-pen "default"
  if length all-numbers > 5 and length all-numbers mod 10 = 0
  [ set-plot-x-range (floor (min all-numbers / 5)) * 5 (ceiling ((max all-numbers + 1) / 5)) * 5 ]
  plot-pen-reset
  histogram all-numbers
  set maxbar modes all-numbers
  set maxrange length filter [ ? = item 0 maxbar ] all-numbers
  set-plot-y-range 0 max list 10 maxrange
  ;;plots the "average" line
  set-current-plot-pen "Mean"
  plot-pen-reset
  plot-vert-line ((sum all-numbers) / (length all-numbers))
  ;;plots the number of squares in the selection
  set-current-plot-pen "Squ"
  plot-pen-reset
  plot-vert-line height-of-sample * width-of-sample

  set-current-plot "Mean Value Over Time"
  set-current-plot-pen "Mean"
  plot sum all-totals / length all-totals
  set-current-plot-pen "ExpVal"
  if length all-totals > 10 [ plot monitor-exp-val ]
  if plot-y-min < 0 [ set-plot-y-range 0 plot-y-max ]
end

;;generates the expected value calculation for the monitor
to-report monitor-exp-val-calculation
  let my-ratio-list 0

  set my-ratio-list ratio-list

  ;; the "% in Population" monitors pick items from ratio-monitor-list
  let ratio-monitor-list map [ (word ? "/" (sum my-ratio-list) " = "
                                     precision (100 * ? / (sum my-ratio-list)) 1
                                     "%") ]
                             my-ratio-list
  let exp-val-calc word (width-of-sample * height-of-sample) " * ("
  foreach [ 1 2 3 4 ]
  [
    set exp-val-calc (word exp-val-calc
                           ? " * "
                           item (? - 1) my-ratio-list
                           "/"
                           (sum my-ratio-list)
                           " + ")
  ]
  ;; we separated out the "5 case" because we don't add a "+", but we do add a ") ="
  set exp-val-calc (word exp-val-calc 5 " * "
                         item 4 my-ratio-list
                         "/" (sum my-ratio-list) ") = ")

  report exp-val-calc
end

;;generates the expected value for the monitor
to-report monitor-exp-val
  let my-ratio-list 0

  set my-ratio-list ratio-list

  report (width-of-sample * height-of-sample)
         * sum map [ ? * item (? - 1) my-ratio-list / (sum my-ratio-list) ] [ 1 2 3 4 5 ]
end

;;shows the populations separately
to histogram-patches [ list-of-nums ]
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

;;disperses fish population
to swim-to-new
  let list-of-moves []
  let steps (round (#fish / 4))
  ;;finds the movement amount of each fish per step, changes heading
  ask fish
  [
    pick-random-place
    set list-of-moves (lput (distancexy picked-x picked-y  / steps) list-of-moves)
    facexy picked-x picked-y
  ]
  ;;moves the fish
  repeat steps [ ask fish [ fd (item who list-of-moves) wait .05] ]
end

;;plots a vertical line at x-coord
to plot-vert-line [ x-coord ]
  plotxy x-coord plot-y-min
  plot-pen-down
  plotxy x-coord plot-y-max
  plot-pen-up
end

;;reports the output for the % in Population monitors, given the $index
;;also checks if changes were made to the current ratios slider from the
;;original setup position
to-report monitor-%-in-pop [ which ]
  let ratios (list $1-fish $2-fish $3-fish $4-fish $5-fish)
  report (word item (which - 1) ratios
               "/" sum ratios " = "
               precision (100 * item (which - 1) ratios / sum ratios) 1
               "%")
end
@#$#@#$#@
GRAPHICS-WINDOW
248
61
632
466
5
5
34.0
1
10
1
1
1
0
1
1
1
-5
5
-5
5
1
1
1
ticks
30.0

TEXTBOX
9
52
78
70
Darkest
11
0.0
0

TEXTBOX
9
102
68
120
Darker
11
0.0
0

TEXTBOX
5
147
75
165
Regular
11
0.0
0

TEXTBOX
9
226
60
244
Lighter
11
0.0
0

TEXTBOX
9
250
66
268
Lightest
11
0.0
0

BUTTON
3
350
126
383
NIL
click-select\n
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
3
299
246
348
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

SLIDER
3
18
149
51
$1-fish
$1-fish
0
10
7
1
1
ratio-units
HORIZONTAL

SLIDER
3
68
149
101
$2-fish
$2-fish
0
10
6
1
1
ratio-units
HORIZONTAL

SLIDER
3
116
149
149
$3-fish
$3-fish
0
10
5
1
1
ratio-units
HORIZONTAL

SLIDER
3
166
149
199
$4-fish
$4-fish
0
10
0
1
1
ratio-units
HORIZONTAL

SLIDER
3
216
149
249
$5-fish
$5-fish
0
10
4
1
1
ratio-units
HORIZONTAL

MONITOR
634
10
735
55
Expected Value
monitor-exp-val
2
1
11

MONITOR
737
10
874
55
Mean Value per Sample
sum all-totals / length all-totals
2
1
11

BUTTON
3
389
126
422
NIL
random-select
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
150
10
246
55
% in Population
monitor-%-in-pop 1
1
1
11

MONITOR
150
110
246
155
% in Population
monitor-%-in-pop 3
1
1
11

MONITOR
150
160
246
205
% in Population
monitor-%-in-pop 4
1
1
11

MONITOR
150
210
246
255
% in Population
monitor-%-in-pop 5
1
1
11

BUTTON
3
428
126
461
wander
ask fish [ wander-around ]\ndisplay
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
3
264
127
297
height-of-sample
height-of-sample
1
6
2
1
1
NIL
HORIZONTAL

SLIDER
129
264
246
297
width-of-sample
width-of-sample
1
6
3
1
1
NIL
HORIZONTAL

PLOT
634
61
874
198
Value per Sample
Totals
Count
10.0
50.0
0.0
10.0
true
true
"clear-plot\nset-plot-x-range (ceiling (monitor-exp-val / 5)) * 5 - 10  (ceiling (monitor-exp-val / 5)) * 5 + 10\n\n  " ""
PENS
"Count" 1.0 1 -16777216 true "" ""
"Mean" 1.0 0 -13840069 true "" ""
"ExpVal" 1.0 0 -2674135 true "plot-vert-line monitor-exp-val" ""

SWITCH
130
389
246
422
blind?
blind?
1
1
-1000

SWITCH
130
428
246
461
show-val?
show-val?
0
1
-1000

PLOT
634
199
874
330
Number per Sample
Totals
Count
0.0
10.0
0.0
10.0
true
true
"clear-plot\nset-plot-x-range 0 ((ceiling (((width-of-sample * height-of-sample) * 2) / 5)) * 5)\n\n  " ""
PENS
"default" 1.0 1 -16777216 true "" ""
"Mean" 1.0 0 -13840069 true "" ""
"Squ" 1.0 0 -2674135 true "plot-vert-line height-of-sample * width-of-sample" ""

MONITOR
248
10
632
55
Expected Value Calculation
monitor-exp-val-calculation
0
1
11

PLOT
634
331
874
466
Mean Value Over Time
Time
Mean Value
0.0
10.0
0.0
10.0
true
true
"clear-plot\nset-plot-y-range (ceiling (monitor-exp-val / 5)) * 5 - 10  (ceiling (monitor-exp-val / 5)) * 5 + 10\n" ""
PENS
"Mean" 1.0 0 -16777216 true "" ""
"ExpVal" 1.0 0 -2674135 true "plotxy plot-x-min monitor-exp-val\nplotxy plot-x-max monitor-exp-val" ""

MONITOR
818
199
874
244
Current
last all-numbers
0
1
11

MONITOR
818
133
874
178
Current
last all-totals
0
1
11

MONITOR
818
400
874
445
Runs
length all-totals
0
1
11

MONITOR
150
60
246
105
% in Population
monitor-%-in-pop 2
3
1
11

MONITOR
818
240
874
285
Average
(sum all-numbers) / (length all-numbers)
2
1
11

MONITOR
818
281
874
326
Squares
height-of-sample * width-of-sample
0
1
11

SWITCH
130
350
246
383
setup-apart?
setup-apart?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

Expected Value Advanced illustrates expected-value analysis under the special condition that the sample size varies.  This model extends the ProbLab model Expected Value, where the sample size is fixed.

Expected-value analyses look at the 'value' of outcomes in probability experiments in terms of some utilitarian framework, such as money or points. As in life, some events are more significant to us --- they are "worth" more for our endeavors. In that sense, expected-value simulations go beyond looking just at chance --- how often or how rarely something happens --- they introduce a "worth" factor (weight, coefficient) associated with experimental outcomes. Value and worth are not synonymous; sometimes a low value has a high worth. For instance, in golf, the lowest value has the highest worth. These simulations examine not only the issue of what I should expect from a phenomenon involving random behavior, but also, what it would be worth for me.  For instance, if I draw 12 coins randomly from a sack containing an equal number of pennies, nickels, dimes, and quarters, we could talk about which 12 'coins' I could expect to get (a nominal classification) --- 3 'pennies,' 3 'nickels,' 3 'dimes,' and 3 'quarters,' on average. But we could extend the discussion to how much money I would get, that is, what would be the 'value' of my draw. To do this, we'd have to multiply the expected frequency of each type of coin by its value:

>3 * 1 + 3 * 5 + 3 * 10 + 3 * 25 = 123 cents.

A more general way of putting this all together -- the sample size (12), the probabilities of each coin (1/4 for each of them), and the value of each coin (1, 5, 10, and 25) -- is:

>12 * (1/4 * 1 + 1/4 * 5 + 1/4 * 10 + 1/4 * 25) = 123 cents.

Thus, the term 'expected value' may be defined as the sum of the products of the probability of each possible event and the value of that event multiplied by the sample size.

This model is a part of the ProbLab curriculum.  The ProbLab curriculum is currently under development at the CCL.  For more information about the ProbLab curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

The analogy in the model is a lake with fish swimming around.  Each type of fish is worth a certain number of dollars (1, 2, 3, 4 or 5) [other currencies or point systems apply just as well]. The distribution of types of fish by amount-of-worth -- how many $1-fish, $2-fish, ..., or $5-fish there are in the pond --- is set by the sliders on the left. With the sliders, we can set the distribution of fish by type and, therefore, the chance of catching each type of fish. That is, the higher you have set a given slider as compared to other sliders (see the % IN POPULATION monitors), the higher the chance of catching a fish with that worth. For instance, the more $2-fish there are, the higher is the chance of catching a $2-fish in a random sample.  Note that the more valuable the fish, the lighter its body color. You can press CLICK SELECTION and then click in the view to "catch" a random sample, or press RANDOM SELECTION to have the choosing done for you. The computer selects randomly. You, too, can select "blindly," if you turn on the BLIND? switch.

Note that the sampling in this model is of arrays, e.g., a 2-by-3 array of 6 squares. There are as many fish in this model as there are squares. One might expect to catch 6 fish when one samples from 6 squares. However, when the WANDER button is pressed, the fish wander randomly, and so sometimes 6 squares have more than 6 fish and sometimes they have less. You can think of each selection as a fishing net that is dipped into the lake --- the fisher doesn't know how many fish will be in the net. This feature of the model creates variation in sample size. Thus, one idea that this model explores is that even under variation in sample size, we still receive outcomes that correspond to the expected value that we calculate before taking samples.

## PEDAGOGICAL NOTE

The idea of 'expected value' is that we can formulate an educated guess of the dollar worth of the fish we catch. It's similar to asking, "How much does the average fish cost?" We need to somehow take into account both the chance of getting each type of fish and its dollar value. The computer program will do much of the calculations for us, but here's the gist of what it does:

Let's say that the ratio units we set up for $1, $2, $3, $4 and $5 fish were, respectively,

>1 : 6 : 5 : 0 : 4.

The number '6,' for example, indicates our ratio setting for fish worth 2 dollars. You can immediately see that the chance of getting a $2-fish is greater than the chance of getting a $3-fish, because the chance of getting a $2-fish (6 units) has more ratio units than the $3-fish (5 units). But in order to determine precisely the chance is of getting each type of fish, we need to state the ratio units relative to each other. We need a common denominator. In this particular setting, there is a total of 16 'ratio units':

>1 + 6 + 5 + 0 + 4 = 16.

Now we can say that if we catch a fish, there is, for instance, a 4-in-16 chance that it is a $5-fish. That is a 25% chance of catching a fish that is worth exactly 5 dollars. We can also say that this relative proportion of $5-fish in the lake contributes .25 * 5, that is, $1.25, to the mean value of a single fish in the lake. Similarly, we can say there is a 5-in-16 chance of getting a $3-fish, a 6-in-16 chance of getting a $2-fish, etc. If we sum up all products of 'value' and 'probability,' we get the expected value per single fish:

>(1 * 1/16) + (2 * 6/16) + (3 * 5/16) + (4 * 0/16) + (5 * 4/16)
>= 48/16 = 3 dollars per fish.

This tells us that if you pick any single fish under these settings, you should expect to get a value of 3 dollars.  If you were to select a sample of 6 fish, then you would expect to pocket 18 dollars (6 fish * 3 dollars-per-fish).

## HOW TO USE IT

Begin by setting fish-value ratios (or just use the default settings). Click SETUP and watch the information updated in the view. Below are more features of the model that will let you change the way it looks and runs.

If you change any of the sliders you will have to press SETUP for the changes to take effect.

### Sliders

$1-FISH, $2-FISH, $3-FISH, $4-FISH, $5-FISH -- ratio-unit settings for the distribution of fish by value.  Note that the ratios are set in proportion to each other. The labels under the sliders indicate that the higher-valued fish have the lighter fish body color.

HEIGHT-OF-SAMPLE -- sets the height of the sample selection.

WIDTH-OF-SAMPLE -- sets the width of the sample selection.

The two previous sliders determine the selection area (width x height).

### Switches

SETUP-APART? -- when set to 'On,' and the SETUP button is pressed, the fish populations group by value, wait 3 seconds, and then swim to a random locations in the display.

BLIND? -- if 'On,' you will only see the fish you caught in the currently-selected sample; if 'Off,' you will always see all of the fish.

SHOW-VAL? -- when set to 'On,' selected fish display their monetary value (worth).

### Buttons

SETUP -- initializes variables, re-colors the fish, and resets monitors and graphs.

CLICK SELECTION -- waits for the user to select an area in the pool, counts up the total number of fish in that area as well as their values, calculates, and graphs totals of these values.

RANDOM SELECTION-- randomly chooses sample areas in the pool.

WANDER -- moves all fish in random directions.

### Monitors

% IN POPULATION -- shows the percentage of each type of fish in the population. For instance, if the % IN POPULATION monitor for $3-FISH is at 50%, then half of all the fish in the lake will have a value of exactly 3 dollars.

EXPECTED VALUE CALCULATION -- This monitor reports the calculation of the expected number of dollars per sample. The calculation first determines the value of the 'average fish, ' given the $-unit slider settings for the population distribution by value, and then multiplies this value by the number of fish in an average sample of size 'height * width.' Thus, 'average value' * 'sample-size' = total is the expected value of sample.

EXPECTED VALUE -- shows the result of the expected-value calculation (see above).

MEAN VALUE PER SAMPLE -- shows the cumulative mean value of samples over all samples taken.

CURRENT SAMPLE VALUE -- shows the total value of the current sample selection.

CURRENT NUMBER OF FISH -- shows the number of fish in the current sample selection.

AVERAGE NUMBER OF FISH PER SAMPLE -- shows the average number of fish over all samples taken.

SQUARES -- monitors the number of squares in the selection area. The value of SQUARES is the product of HEIGHT-OF-SAMPLE and WIDTH-OF-SAMPLE

RUNS -- monitors the number of times that the user or computer sampled from the pool.

### Plots

VALUE PER SAMPLE -- plots the number of occurrences of specific value totals in the samples.  For instance, it shows how many times your sample was worth exactly $4. It also plots the expected value (in red) and the mean value per sample (in green),

NUMBER PER SAMPLE -- plots the number of fish in the samples.  It also plots the average number of fish per sample (in green) as well as the number of squares, that is, the expected average number of fish in samples (in red).

MEAN VALUE OVER TIME -- plots, over time, the values of each sample (in black), as well as the expected value (in red), which does not change after Setup.

## THINGS TO NOTICE

In NetLogo, the location of a turtle in terms of the world coordinate system is determined by the location of the center of its shape. So when one selects an area in this model, a fish may be sampled even though it is not completely inside the selected area (for instance, its tail might be sticking out of that area).

Fish may be unevenly distributed throughout the 'lake.'  This feature distinguishes the Expected Value Advanced model from the Expected Value model, where the elements (tiles) are evenly distributed (1 tile per patch).  The model monitors the number of fish per sample in the NUMBER PER SAMPLE plot.

The SQUARES monitor changes with the HEIGHT-OF-SAMPLE and WIDTH-OF-SAMPLE sliders to show the size of the selection array of squares (NetLogo "patches").  Because in this version of the model there are exactly as many fish as there are squares, SQUARES shows the expected average number of fish per sample. It is only "expected" and not fixed, both because the fish are not distributed uniformly in the view when you setup and because the fish may optionally move (if you have pressed WANDER).

For equal HEIGHT-OF-SAMPLE and WIDTH-OF-SAMPLE settings, mouse clicks are in the center of the sample array. For other settings, the click in not in the center.

At setup, the distribution of fish by value in the sample space is often an approximation and not completely accurate. The program sets the probabilities according to the ratios, but it still produces a very small error. There are a fixed number of patches and fish (in the default setting of the model there are 121 squares and 121 fish). This number cannot precisely accommodate all the different possible ratio settings. For instance, we cannot have two equal halves. That is, there will be settings where the program will make approximations. These approximations will lead to some minor degree of experimental error.

Look at the plot MEAN VALUE OVER TIME. The more samples you take, the closer the red line gets to the black line. Can you explain this?

## THINGS TO TRY

Run the model under different setting of the switches. Does it take longer for the model to converge on the expected values when you are not working entirely randomly?

Change the height and width of the selection area.  Run the experiment, looking at the MEAN VALUE OVER TIME plot. Does this plot behave differently for different height and width settings?

As noted above, the relative ratios of the $1-fish, $2-fish, etc. in the lake are determined by relative values of all of the sliders on the left.  In other words, if you set the ratio units to 1 : 1 : 1 : 1 : 1, it is going to mean the same to the computer as the setting of 2 : 2 : 2 : 2 : 2 or 5 : 5 : 5 : 5 : 5.  The sliders are designed to allow an exploration of a rich range of proportions of the different fish.  Try extending the maximum value of the ratio sliders, to obtain an even richer range of proportions of fish populations.  
In the Code tab, go to the 'setup-misc-globals' procedure and change the color value assigned to the 'c-color' local variable.  This will change the fish base color.

## EXTENDING THE MODEL

Currently, samples wrap around the view. Edit the code in the `select-area` procedure so the selection does not wrap around.  One way to go about this may be to shift the selected area. For instance, if the user clicks near the right side of the window, a procedure could translate the location of the mouse-click as many patches to the left as necessary.

Add another ratio-unit slider, either for 0 value or beyond 5. It could also be a decimal value between 1 and 5, such as 1.7. It could even be a 'negative value,' which could be interpreted as an added expense, like catching a whale that breaks your fishing rod.

## NETLOGO FEATURES

This utilizes the mouse-clicking capabilities of NetLogo.  Note that in order to use the mouse-clicking functionality a forever button must be running, so that there are active procedures to "catch" your clicks.

## RELATED MODELS

This model is considered more advanced than Expected Value. Both models utilize the idea of 'expected value,' but Expected Value Advanced supplements this with variation in sample size.

The SETUP-APART? functionality arranges the raw data (the fish themselves) in "histograms" as in 9-Block Stalagmite. Also, the BLIND? functionality produces an effect that is similar to that in HubNet SAMPLER, where the population is hidden and only the sample is visible.

## CREDITS AND REFERENCES

This model is a part of the ProbLab curriculum. The ProbLab Curriculum is currently under development at Northwestern's Center for Connected Learning and Computer-Based Modeling. For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.
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

fish-eye-fin
true
0
Rectangle -7500403 true true 105 90 135 90
Polygon -7500403 true true 90 255 210 255 171 179 127 180 90 255
Polygon -2674135 true false 120 180 105 165
Polygon -7500403 true true 118 180 110 174 101 162 93 149 85 131 83 116 81 98 85 76 92 61 101 54 113 50 132 48 150 80 169 48 180 50 194 53 207 62 213 77 216 97 214 118 207 139 196 158 185 172 171 185 128 188
Circle -1 true false 97 68 30
Circle -16777216 true false 108 79 8
Polygon -16777216 false false 108 144 138 114 153 114 183 144 108 144

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
NetLogo 5.0beta4
@#$#@#$#@
setup
repeat 75 [ random-select ]
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

globals [
         ;; colors of the background of the view and of the two possible colors in samples
         background-color column-color target-color other-color
         num-columns ;; how many columns there are in the graphics-window histogram
         num-target-color  ;; how many of the squares (patches) in the sample are of the favored color
         sample-right-xcor ;; the x-coordinate of the moving sample (not the magnified sample)
         sample-location-patch-agentset ;; patches where the moving sample will sprout
         token-sample-dude ;; bottom-left turtle in the moving sample
         stop-all? ;; Boolean variable for stopping the experiment
         ]
patches-own [ column ]

breed [ column-kids column-kid ]
breed [ sample-dudes sample-dude ]
breed [ baby-dudes baby-dude ]

sample-dudes-own [distance-for-jump ]
column-kids-own [ binomial-coefficient sample-list ]

;; This procedure colors the view, divides patches into columns of equal length ( plus a single partition column),
;; and numbers these columns, beginning from the left, 0, 1, 2, 3, etc.
to setup
  clear-all
  set-default-shape turtles "square big"
  set background-color white - 1
  set column-color gray
  set target-color green
  set other-color blue
  set-plot-x-range 0 ( 2 ^ ( side ^ 2 ) ) ;; the max of the range is set to be large enough to begin with

  ;; num-columns is how many columns (bars) there are in the graphics-window histogram.
  ;; We need side ^ 2 + 1 columns in a histogram. For example, 3-by-3 samples (9 patches)
  ;; have 10 -- that is, 3 ^ 2 + 1 -- different possible counts of target-color (0, 1, 2, 3, ...9).
  set num-columns ( side ^ 2 + 1)

  ;; determines the location of the sample array beginning one column to the left of the histogram
  set sample-right-xcor -1 * round ( ( num-columns / 2 ) * ( side + 1 ) )

  ;; assigns each patch with a column number. Each column is as wide as the value set in the 'side' slider
  ask patches
  [
    set pcolor background-color
    ;; The following both centers the columns and assigns a column number to each patch
    ;; We use "side + 1" and not just "side" so as to create an empty column between samples
    set column floor ( ( pxcor + ( ( num-columns * ( side + 1 ) ) / 2 ) ) / ( side + 1 ) )
    if column < 0 or column >= num-columns
      [ set column -100 ]
  ]

  ;; leave one-patch strips between the columns empty
  ask patches with
  [ [column] of patch-at -1 0 != column ]
  [
    set column -100  ;; so that they do not take part in commands that report relevant column numbers
  ]

   ;; colors the columns with two shades of some color, alternately
   ask patches
   [
     if column != -100
     [
       ifelse int ( column / 2 ) = column / 2
       [ set pcolor column-color ][ set pcolor column-color - 1 ]
     ]
   ]

  ;; This draws the yellow base-line and creates a sample-kids turtle at the base of each column
  ask patches with
  [ ( pycor = -1 * max-pycor + side + 3 ) and  ;; The base line is several patches above the column labels.
    ( column != -100 ) ]
  [
    set pcolor yellow
    if [column] of patch-at -1 0 != column   ;; find the leftmost patch in the column...
    [
      ask patch (pxcor + side - 1)  ;; ...then move over to the right of the column
                ( -1 * max-pycor + 1 )
        [ set plabel column ]
      ask patch (pxcor + floor (side / 2))  ;; ...then move over to the middle of the column
                ( -1 * max-pycor + 1 )
      [
       sprout 1
        [
          hide-turtle
          set color pcolor
          set breed column-kids
          set sample-list []

          ;; each column-kid knows how many different combinations his column has
          set binomial-coefficient item column binomrow (num-columns - 1)
        ]
      ]
    ]
  ]
  set stop-all? false
  set num-target-color false
  reset-ticks
end

to go
  if stop-all? [stop]
  ;; The model keeps track of which different combinations have been discovered. Each
  ;; column-kid reports whether or not its column has all the possible combinations. When bound? is true,
  ;; a report from ALL column-kids that their columns are full will stop the run.
  if stop-at-all-found? [if count column-kids with [length sample-list = binomial-coefficient] = count column-kids
    [stop]]
  sample
  ifelse magnify? [ magnify-on-side ] [ ask baby-dudes [ die ] ]
  drop-in-bin
  ;; the counter keeps track of how many samples have been taken (not how many different samples have been discovered)
  tick
  if plot? [ plot %-full ]
end

;; This procedure creates a square sample of dimensions side-times-side, e.g., 3-by-3,
;; located to the left of the columns. Each patch in this sample sprouts a turtle.
;; The color of the sample-dudes in this square are either target-color or other-color,
;; based on a random algorithm (see below)
to sample
  ;; creates a square agentset of as many sample-dudes as determined by the 'side' slider,
  ;; positioning these sample sample-dudes at the top of the view and to the left of the histogram columns
  set sample-location-patch-agentset patches with
  [
    ( pxcor <= sample-right-xcor ) and
    ( pxcor > sample-right-xcor - side ) and
    ( pycor > ( max-pycor - side ) ) ]
  ask sample-location-patch-agentset
  [
    sprout 1
    [
      ht
      set breed sample-dudes
      setxy pxcor pycor

    ;; Each turtle in the sample area chooses randomly between the target-color and the other color.
    ;; The higher you have set the probability slider, the higher the chance the turtle will get the target color
    ifelse random 100 < probability-to-be-target-color
      [ set color target-color ]
      [ set color other-color ]
    st
    ]
  ]
  ;; num-target-color reports how many sample-dudes in the random sample are of the target color
  set num-target-color count sample-dudes with [ color = target-color ]
end

;; procedure in which the sample turtles create an enlarged duplicate on the left side of the view and this enlarged sample makes
;; a large duplicate of the sample. This helps users see the samples that may otherwise be too small to see comfortably.
;; Samples are small for side = 3 because we want the entire sample space to fit into the view.
to magnify-on-side
  ask baby-dudes [die] ;; clears the way for new magnified sample
  ask sample-dudes
  [
    hatch-a-big-baby
  ]
end

to hatch-a-big-baby ;; sample-dudes procedure
  hatch 1
  [
    set breed baby-dudes
    set size 12 * ( 8 - side ) / side
    ;; This is tricky.  We want to center the new turtles vertically, and
    ;; put them to the right of all the columns, with a little space between.
    setxy -1 * ( size * .75 ) + ( sample-right-xcor + ( size * ( xcor - sample-right-xcor ) ) )
          ( ( side - 1 ) * size / 2 ) + ( size * ( ycor + min-pycor ) )
    set size size * .95
  ]
end

;; This procedure moves the random sample sideways to its column and then down above other previous samples
;; in that column.
to drop-in-bin
  find-your-column
  descend
end

;; The random sample moves to the right until it is in its correct column, that is, until it is in the column
;; that collects samples which have exactly as many sample-dudes of the target color as this sample has.
;; The rationale is that the as long as the sample is not in its column, it keeps moving sideways.
;; So, if the sample has 9 sample-dudes (3-by-3) and is moving sideways, but 6 of them are not yet in their correct column,
;; the sample keeps moving. When all of the 9 sample-dudes are the sample's correct column, this procedure stops.
to find-your-column
  ask sample-dudes [ set heading 90 ]
  while
  [ count sample-dudes with [ column = num-target-color ] != side ^ 2  ]
  [
    ask sample-dudes
    [ fd 1 ]
  ]
end

;; Moves the sample downwards along the column until it is either on the base line or
;; exactly over another sample in that column.
to descend
  let lowest-in-sample min [ pycor ] of sample-dudes
  ask sample-dudes
  [ set heading 180 ]

  ;; The lowest row in the square sample is in charge of checking whether or not the sample has arrived all the way down
  ;; In order to determine who this row is -- as the samples keeps moving down -- we find a turtle with the lowest y coordinate
  ;; checks whether the row directly below the sample's lowest row is available to keep moving down
  set token-sample-dude one-of sample-dudes with [ pycor = lowest-in-sample ]
  while
  [
    [pcolor] of [patch-at 0 -2] of token-sample-dude != yellow and
    [pcolor] of [patch-at 0 -2] of token-sample-dude != target-color and
    [pcolor] of [patch-at 0 -2] of token-sample-dude != other-color
  ]
  [
    ;; As in find-your-column, shift the sample one row down
    ask sample-dudes
    [ fd 1 ]
    display
  ;; Instead of establishing again the lowest row in the sample, the y coordinate of the row
  ;; gets smaller by 1 because the sample is now one row lower than when it started this 'while' procedure
  set lowest-in-sample ( lowest-in-sample - 1 )
  ]

  ;; Once sample-dudes have reached as low down in the column as they can go (they are on top of either the base line
  ;; or a previous sample) they might color the patch with their own color before they "die."
  finish-off

  ;; If the column has been stacked up so far that it is near the top of the view, the whole supra-procedure stops
  ;; and so the experiment ends
  if max-pycor - lowest-in-sample < ( side + 1 ) [ set stop-all? true ]
end

to finish-off
  ;; creates local list of the colors of this specific sample, for instance the color combination of a 9-square,
  ;; beginning from its top-left corner and running to the right and then taking the next row and so on
  ;; might be "green green red green red green"
  let sample-color-combination [ color ] of sample-dudes

  ;; determines which turtle lives at the bottom of the column where the sample is
  let this-column-kid one-of column-kids with [ column = [column] of token-sample-dude]

  ;; accepts to list only new samples and makes a previously encountered sample if keep-duplicates? is on
  ifelse not member? sample-color-combination [sample-list] of this-column-kid
  [
    ask this-column-kid
    [ set sample-list fput sample-color-combination sample-list ]
    ask sample-dudes
    [ set pcolor color die ]
  ]
  [
    ifelse keep-repeats? [
      ask sample-dudes
       [ set pcolor color die ]
    ] [
      ask sample-dudes
        [  die ]
    ]
  ]
end

;; procedure for calculating the row of coefficients
;; column-kids needs their coefficient so as to judge if their column has all the possible different combinations
to-report binomrow [n]
  if n = 0 [ report [1] ]
  let prevrow binomrow (n - 1)
  report (map [?1 + ?2] fput 0 prevrow
                        lput 0 prevrow)
end

;;if the model has been run, report the number of patches
;;with the target-color -- otherwise, display "--"
to-report #-target-color
  ifelse ticks != 0.0
  [ report count patches with [ pcolor = target-color ] ]
  [ report "--" ]
end

;;if has been run, report the number of patches
;;with the other-color -- otherwise, display "--"
to-report #-other-color
  ifelse ticks != 0.0
  [ report count patches with [ pcolor = other-color ] ]
  [ report "--" ]
end

;; reports the proportion of the sample space that has been generated up to now
to-report %-full
  report precision ( samples-found / ( 2 ^ ( side ^ 2 ) ) ) 3
end

to-report samples-found
  report sum [ length sample-list ] of column-kids
end

to-report total-samples-to-find
  report 2 ^ ( side ^ 2 )
end
@#$#@#$#@
GRAPHICS-WINDOW
237
10
958
554
118
85
3.0
1
10
1
1
1
0
1
1
1
-118
118
-85
85
1
1
1
ticks
60.0

SLIDER
2
45
168
78
side
side
1
5
3
1
1
NIL
HORIZONTAL

SLIDER
1
10
235
43
probability-to-be-target-color
probability-to-be-target-color
0
100
50
1
1
%
HORIZONTAL

BUTTON
159
189
234
222
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

BUTTON
4
189
78
222
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

SWITCH
2
80
168
113
keep-repeats?
keep-repeats?
0
1
-1000

PLOT
5
387
232
554
filling-up-sample-space
trials
#-combinations
0.0
500.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

MONITOR
5
341
128
386
combinations found
(word samples-found \"/\" total-samples-to-find \"=\" %-full)
3
1
11

SWITCH
3
149
120
182
magnify?
magnify?
0
1
-1000

MONITOR
5
279
95
324
#-other-color
#-other-color
3
1
11

MONITOR
5
229
95
274
#-target-color
#-target-color
3
1
11

SWITCH
3
115
167
148
stop-at-all-found?
stop-at-all-found?
1
1
-1000

SWITCH
121
149
233
182
plot?
plot?
1
1
-1000

MONITOR
176
44
235
89
?-block
side ^ 2
3
1
11

MONITOR
97
248
188
293
%-target-color
precision (100 * #-target-color / (#-target-color + #-other-color)) 1
3
1
11

BUTTON
79
189
157
222
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

@#$#@#$#@
## WHAT IS IT?

9-Block Stalagmite draws on and connects several central ideas in the study of probability, both theoretical and empirical: combinatorics, sample space, binomial distributions, and frequency distribution function.  In the related curricular material, students use crayons and paper to build the Combinations Tower.  The Combinations Tower is a giant bell-shaped histogram of all the 512 different combinations of a 3-by-3 array of squares that can each be either green or blue.

In the tower, these combinations are grouped in columns according to how many green squares there are in each.  9-Block Stalagmite accompanies these curricular activities.

The model generates random samples (with repetitions), counts how many green squares are in each sample, and collects these samples into columns, creating a histogram that grows up like a stalagmite.  If enough samples are taken, then the stalagmite approximates a bell curve.  When the probability in the model is set at .5, the shape of this histogram resembles the combinations tower.  If all repeating samples are removed from this histogram as it grows up, the model eventually exhausts the sample space at each run of the experiment, providing the view allows the tower to grow tall enough.

This model is a part of the ProbLab curriculum.  The ProbLab Curriculum is currently under development at the CCL.  For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

At the top of the view, a square array of "patches" (the NetLogo square agents that don't move) generates green and blue "turtles" (the NetLogo agents that move).  These turtles' colors --- whether they are green or blue --- are determined randomly according to probabilities set by the user.  For instance, an 80% chance for each turtle to be green (so 20% chance for each turtle to be blue).  Next, the model moves this turtle-sample towards the right so that it arrive in a column that matches the number of green turtles in the sample.  Lastly, the model moves the sample down along the column and stacks it on top either of the bottom of the column or a previous sample that has already been positioned there.  When the turtles stop, they mark their color onto the patches, and then they "die".  For each sample that is generated, a larger duplicate appears on the side for the user's viewing convenience.

Note that for a 9-block there are 10 columns in the view.  This is because we need a column for all combinations that have 0 greens, a column for 1 green, a column for 2 green, ..., and a columns for 9 green for a total of 10 columns (9 + 1).

## HOW TO USE IT

### Buttons

'Setup' - prepares the view, including erasing the coloration from a previous run of the experiment.  
'Go' - a forever button that runs the experiment according to the values you have set in the sliders.  
'Go Once' - a button that runs the experiment only once.

### Sliders

'side' - determines the size of the square sample arrays.  For instance, for a value of 3, the model will create a 3-by-3 square (9 turtles).  
'probability-to-be-target-color' - determines the chance of each turtle in the sample array to be green (or any other color you have set the model to).  A value of 50 means that each turtle has an equal chance of being green or blue, a value of 80 means that each turtle has a 80% chance of being green (so each has a 20% chance of being blue).

### Switches

'keep-repeats?' - when "Off," each column will have only different combinations with no repetitions; when "On," there will most probably be repetitions in at least some of the columns.  (For 2-by-2 samples, there will be very rare cases that the whole sample space will be discovered without repetitions.  For 3-by-3 samples, the cases are so rare that you probably won't ever see it!! Actually, how rare are they?..)  
'magnify?' - when "On," you get a larger version of the sample on the side.  This helps when you're working with very small patch sizes, such as when you want to have very tall columns so as to exhaust the sample space corresponding to the value of 'side,' e.g,. 512 for 'side = 3.'  
'stop-at-all-found?' - when "On," each run will end if all the different combinations have been discovered.  When "Off," the run will continue until one of the columns reaches the top.

### Monitors

'#-target-color' - displays the total number of squares of target-color, e.g., 'green.'  
'#-other-color' - displays the total number of squares of other-color, e.g., 'blue.'  
'%-target-color' - displays as a percentage the proportion of target-color squares out of all colored squares.  
'combinations found' - displays the proportion of the sample space that has been revealed  
'?-blocks' shows the square of 'side.' So if 'side' is set to 3, '?-blocks' will show 9. This means that each sample of side 3 has 9 squares.

### Plots

'filling-up-sample-space' - plots the total number of combinations that have been discovered as a proportion of the sample space that has been discovered.

### How to use it

Set the sliders according to the values you want, press Setup, and then press Go.

Note that once you have pressed Go, changing the slider values would "confuse" the model.  Instead, you should un-press Go, wait until the button jumps back up, and only then change the sliders as you wish, press Setup, and then press Go

## THINGS TO NOTICE

Setup the model in its default settings ('side' slider set to the value of 3; 'probability' slider set to the value of .5), slow down the model with the speed slider at the top left of the view, and press Go.  Note the following: the more green squares there are in the random sample the further to the right the block will move before descending down. See that at the bottom of each column there is a number.  This number tells you the type of sample that will be collected in this column.  For instance, in the column labeled '2,' samples that have exactly 2 green patches will stop moving to the right and will descend.  So, as the columns grow up, note that each sample will descend over a previous sample that has exactly as many green squares.  Overall, the columns on the right are greener than the columns on the left.  As you shift your gaze from side to side, the color density changes.

Run the model over and over and watch the shape of the colorful histogram that is plotted.  Note any consistencies in the shape of this histogram.

## THINGS TO TRY

Work in the 'side' = 2 setup. Set 'keep-duplicates?' to Off.  How many samples do you need to fill the entire sample space?  Does this change according to the settings of 'probability'?  If so, why might this be so? If the probability is set at 80%, does it take as many trials to fill the sample space as compared to a setting of 50%? If not, why not?

Setup the model with the 'side' value at 2 and the 'probability' at 70%.  Press Setup and then Go.  (If you have previously slowed down the model, you're welcome to speed it up now.)  Note that the %-target-color tends to 70%. Also note that more sample combinations collect up in the column of 3 green patches than any other column.  So '3' is the mode.  We have found it interesting and perhaps somewhat confusing to shift between thinking about the 'mode' and thinking about the 'mean.' For instance, if you set the 'probability' to 83%, you might think that 83% of 4 is 3.32 and so the mode should still be '3.' Is it? What is going on here? So for some probability setting, the 3-column will most often get to the top before other columns. For other probability settings, the 4-column wins. Try to determine the probability ranges in which each column "rules." Hint: How many ranges should there be?

Edit the view so that the y-axis value is 260 and the patch size is 1 pixel. Now, if you set up in 'side = 3,' all 512 different 9-blocks will fit in. Try this.

## EXTENDING THE MODEL

Add monitors and/or graphs that will feature aspects of the experiments that are difficult to see in the current version.  For instance:  
- How many samples does it take for the experiment to produce an all-green sample?  How is this dependent on the various settings?  
- Are there more samples with an even number of green squares as compared to those with an odd number of green squares?  
- How symmetrical is the histogram?  How would you define "symmetry?"  How would you quantify and display its change over time?

## NETLOGO FEATURES

Turtle Shapes --- The top panel of the view features a red arrow-like turtle shape.  Pressing on this button toggles between Turtles-Shape "On" and "Off."  In many NetLogo models, turtles represent specific agents ("creatures") in the situation or phenomenon that is being modeled.  So in many models the turtles have distinctive shapes (icons) that approximate or at least suggest what they are modeling.  However, this model is an example of a case where we do not want any special turtle shape.  In fact, we actually want the turtles to look like the patches --- just squares.  So we have turned the Turtle-Shapes to "Off."  Try running the model with the Turtles-Shapes "On" (you can change this while running the model) and see which mode you prefer.  The difference between these two modes will be most noticeable when you are running the model slowly and/or when the 'magnify?' switch is turned to 'On."  You could also edit the size of the turtles.  Perhaps you will discover that you prefer having the turtles be some special shape.  How might that affect the histogram?

## RELATED MODELS

Some of the other ProbLab (curricular) models, including SAMPLER --- HubNet Participatory Simulation --- feature related visuals and activities.  In Stochastic Patchwork and especially in 9-Blocks you will see the same 3-by-3 array of green-or-blue squares.  In the 9-Block Stalagmite model, when 'keep-duplicates?' is set to 'Off' and the y-axis value of the view is set to 260, we get the whole sample space without any duplicates.  In the Stochastic Patchwork model and especially in 9-Blocks model, we see frequency distribution histograms.  These histograms compare in interesting ways with the shape of the combinations tower in this model.

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

square big
false
0
Rectangle -7500403 true true 0 0 300 300

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
repeat 150 [ go ]
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

breed [ wanderers wanderer ]    ;; turtles in Equidistant-Point that walk out from green patches
breed [ darts dart ]           ;; turtles in Epicenter: They all emerge from the middle

globals
[
  num-squares                  ;; number of selected squares (patches) from which wanderers emerge
  attempts                     ;; number of times the wanderers have attempted to convene on the same patch since the previous success
  attempts-list                ;; list of how many attempts it took until a success
  successes-per-this-sample    ;; how many successes occurred in the current sample of attempts
  successes-per-sample-list    ;; list of how many successes occurred in all samples up to the current moment
  successful-attempt?          ;; Boolean variable that is true if an attempt was successful
]

patches-own
[
  counter      ;; in Epicenter, this variable keeps track of how many times darts have visited the patch
  pmy-color    ;; "reminds" patches of their color from before it is temporarily changed
  conventions  ;; how many times all the wanderers meet in the patch
]

;; SETTING UP THE MODEL

to setup
  ca
  set attempts-list []
  set successes-per-sample-list []
  set successful-attempt? false
  checker
end

;; creates the 9-by-9 "chess-board" appearance of the view with a blue perimeter
to checker
  ask patches
  [
    ;; even patches and odd patches
    ifelse ( pxcor mod 2  = pycor mod 2 )
    [ set pcolor 3 ]
    [ set pcolor 7 ]
    if count neighbors != 8 [ set pcolor 104 ]
    set pmy-color pcolor
  ]
end

;; user uses mouse to paint squares red
to select-squares
  if mouse-down?
  [
    ask patch round mouse-xcor round mouse-ycor
    [
      if pcolor = 104 [ stop ]
      ifelse pcolor = red
      [ set pcolor pmy-color ]
      [ set pcolor red ]
    ]
    wait .2 ;; to give the user a chance to retract his/her clicking finger
    set num-squares count patches with [ pcolor = red ]
  ]
end

;; some preset suggestions for setting up the squares
to preset1 ;; two abutters
  setup
  ask patch 0 1 [ set pcolor red ]
  ask patch 0 -1 [ set pcolor red ]
  set num-squares 2
end

to preset2 ;; two corners
  setup
  ask patch -1 1 [ set pcolor red ]
  ask patch 1 -1 [ set pcolor red ]
  set num-squares 2
end

to preset3 ;; three abutters -- mean #attempt to success will be 1/ (6*6*6) =1/216
  setup
  ask patch 1 0 [ set pcolor red ]
  ask patch 0 -1 [ set pcolor red ]
  ask patch -1 0 [ set pcolor red ]
  set num-squares 3
end

to preset4 ;; two abutters, one corner -- mean #attempt to success will be 1/ (6*6*12) =1/432
  setup
  ask patch 1 0 [ set pcolor red ]
  ask patch 0 -1 [ set pcolor red ]
  ask patch -1 1 [ set pcolor red ]
  set num-squares 3
end

to preset5 ;; one abutter, two corners -- mean #attempt to success will be 1/ (6*12*12) =1/864
  setup
  ask patch 1 0 [ set pcolor red ]
  ask patch -1 -1 [ set pcolor red ]
  ask patch -1 1 [ set pcolor red ]
  set num-squares 3
end

to preset6 ;; three corners -- mean #attempt to success will be 1/ (12*12*12) =1/1728
  setup
  ask patch 1 -1 [ set pcolor red ]
  ask patch -1 -1 [ set pcolor red ]
  ask patch -1 1 [ set pcolor red ]
  set num-squares 3
end

;; RUNNING THE MODEL

;; from every red square a wanderer emerges, heads off randomly, and steps forward one step
to find-equidistant-point
  if num-squares < 2 [ stop ]
  if single-success? and successful-attempt? [ set successful-attempt? false stop ]
  set attempts attempts + 1
  birth-wanderers
  sprint-straight
  update-sampling-data
end

to birth-wanderers
ask patches with [pcolor = red]
  [
    sprout-wanderers 1
    [
      set color magenta
    ]
  ]
end

;; if there are as many turtles on the same patch as there are red squares, then that means that all
;; our wanderers have convened together. This patch is equidistant from all red squares because
;; all wanderers have traveled as far over the same time.
to sprint-straight
  if not any? wanderers [ stop ]
  ask wanderers [ fd 1 ]
  if any? patches with [ ( pcolor != 104 ) and ( count wanderers-here = num-squares ) ]
  [
    set successful-attempt? true
    ask patches with [ ( pcolor != 104 ) and ( count wanderers-here = num-squares ) ]
      [set conventions conventions + 1]
    update-labels
    do-plots
  ]
  ask wanderers [ die ] ;; because we need to start a new attempt now
end

to update-labels
  ask patches with [ conventions != 0 ]
  [
    set pcolor green
    set plabel word precision ( 100 * conventions / ( sum attempts-list + attempts ) ) 2 "%"
  ]
end

to do-plots
  set attempts-list ( fput attempts attempts-list )
  set-current-plot "Attempts Until Success"
  set-plot-x-range 0 ( max attempts-list + 1 )
  set-current-plot-pen "attempts until success"
  histogram attempts-list
  let maxbar modes attempts-list
  let maxrange filter [ ? = item 0 maxbar ] attempts-list
  set-plot-y-range 0 length maxrange
  set-current-plot-pen "mean attempts"
  plot-pen-reset
  plotxy (mean attempts-list) plot-y-min
  plotxy (mean attempts-list) plot-y-max
  set attempts 0
end

;; updates each time a sample has ended running
to update-sampling-data
  if ( sum attempts-list + attempts ) mod sample-size = 0 ;; if the number of attempts is a multiple of sample size
  [
    ;; finds increment in successes since last sampling point.
    ;; note that the length of a list here is in effect how many times the list
    ;; was updated, that is, how many times whatever it is listing actually happened
    set successes-per-this-sample ( length attempts-list - sum successes-per-sample-list )
    set successes-per-sample-list ( fput successes-per-this-sample successes-per-sample-list )
    set-current-plot "#Successes Per Sample"
    if not empty? ( remove 0 successes-per-sample-list )
      [ set-plot-x-range 0 ( max successes-per-sample-list + 1 ) ]
    set-current-plot-pen "#Successes Per Sample"
    histogram successes-per-sample-list
    let maxbar modes successes-per-sample-list
    let maxrange filter [ ? = item 0 maxbar ] successes-per-sample-list
    set-plot-y-range 0 length maxrange
    set-current-plot-pen "mean-successes-per-sample"
    plot-pen-reset
    plotxy ( mean successes-per-sample-list ) plot-y-min
    plotxy ( mean successes-per-sample-list ) plot-y-max
  ]
end

to-report cumulative-frequency
  let cum-probab precision (100 * (length attempts-list / (sum attempts-list + attempts))) 2
  report word cum-probab " %"
end

to-report samples-count
  let total-attempts ( sum attempts-list + attempts )
  let total-samples floor ( total-attempts / sample-size )
  report (word total-samples " samples + "
               ( total-attempts - total-samples * sample-size )
               " attempts this sample")
end

to epicenter
  setup ; same setup as used in Equidistant-Point
  ask patch 0 0 [ if pcolor != red [ set pcolor red ] ]
  birth-darts
  move-dart
  ask darts [ die ]
  label-results
end

to birth-darts
  create-darts 10000
    [ set size .5 ]
end

to move-dart ;; the patches keep track of their visits
  ask darts [ fd 1 ]
  ask patches [ set counter counter + count turtles-here ]
end

to label-results
  let helper sum [ counter ] of patches
  ask patches with [ ( pcolor != 104 ) and ( pcolor != red ) ]
  [
    set plabel-color pcolor + 5
    ifelse Display-Results = "by-%"
      [ set plabel word precision ( 100 * counter / helper) 2 "%" ]
      [ set plabel precision counter 0 ]
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
420
10
815
426
2
2
77.0
1
16
1
1
1
0
0
0
1
-2
2
-2
2
0
0
0
ticks

BUTTON
6
48
163
85
Select Square
select-squares
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
6
198
164
248
Find Equidistant Point
find-equidistant-point
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
6
10
163
44
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

SWITCH
6
253
164
286
single-success?
single-success?
0
1
-1000

MONITOR
173
10
253
55
#Successes
length attempts-list
3
1
11

MONITOR
256
10
336
55
#Attempts
sum attempts-list + attempts
0
1
11

MONITOR
342
10
415
55
Frequency
cumulative-frequency
2
1
11

MONITOR
201
191
383
236
Mean #Attempts to Success
mean attempts-list
2
1
11

BUTTON
7
346
163
380
Epicenter
epicenter
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
6
288
163
321
sample-size
sample-size
0
10000
1000
100
1
NIL
HORIZONTAL

PLOT
171
306
413
426
#Successes Per Sample
#successes
count
0.0
15.0
0.0
4.0
true
false
"" ""
PENS
"#Successes Per Sample" 1.0 1 -6459832 true "" ""
"mean-successes-per-sample" 1.0 0 -8630108 true "" ""

MONITOR
171
255
413
300
Samples Taken Up To Now
samples-count
3
1
11

PLOT
173
71
414
191
Attempts Until Success
#attempts
count
0.0
10.0
0.0
4.0
true
false
"" ""
PENS
"attempts until success" 1.0 1 -13791810 true "" ""
"mean attempts" 1.0 0 -2064490 true "" ""

BUTTON
6
89
84
122
NIL
Preset1
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
85
89
163
122
NIL
Preset2
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
6
123
84
156
NIL
Preset3
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
85
123
163
156
NIL
Preset4
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

CHOOSER
7
381
163
426
Display-Results
Display-Results
"by-total" "by-%"
1

BUTTON
6
158
84
191
NIL
Preset5
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
85
158
163
191
NIL
Preset6
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

Equidistant Probability connects between probability and geometry. You select two or more squares, and the model searches randomly for squares that are equally distant from the squares you selected. To do this random search, creatures pop out of each one of your selected squares and simultaneously step forward one step in some random direction. If they all land in the same square, that's a hit. Can you guess how often this will happen?

This model is a part of the ProbLab curriculum. The ProbLab curriculum is currently under development at the CCL. For more information about the ProbLab curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

The user sets up an experiment by selecting some squares on the view. When the model runs, from each of these selected squares emerges a creature, headed in a random direction, and then all these creatures step forward at the same moment. Because there are exactly as many creatures as there are selected squares, and because they all step forward at the same moment, if all the creatures arrive at the same square at the same moment, then that square is "equidistant" (equally far away) from all the selected squares. In that case, the square where they all landed becomes green and keeps a count of how often, out of all their "attempts," the creatures met there. Note that the creatures don't need to land on the exact same spot in the view (the same pixel) -- it's enough that they land in the same square.

There is a procedure in this model, "Epicenter," that helps think about how often a single creature might land in its neighboring squares. If we knew that, we might be able to figure out how often two or more creatures might land on the same square at the same time. For instance, if one creature has a 1-in-2 chance of landing on some square, and another creature has a 1-in-3 chance of landing on that same square, then there's a 1-in-6 chance that they will land on that square at the same moment. In this procedure, 10,000 creatures pop out of the middle square and take a step forward in some random direction. The other eight squares count up how many creatures landed on them, and these values are shown either as totals or as percentages.


## HOW TO USE IT

Here is a quick way to get familiar with the model. Press "PRESET1," slow down the model (using the slider on the top-left corner of the view), make sure SINGLE-SUCCESS? is at "Off," then press FIND EQUIDISTANT POINT. Watch how two creatures first emerge, each from its red square, and then step one step forward in some random direction. Once you understand this, speed up the model gradually. Quite soon, the creatures will land on the same square at the same time, that square will become green, and it will display a percentage, for instance 5.00% (that is, 1/20). That would mean that it took 20 attempts to find that square. Also, the plot ATTEMPTS UNTIL SUCCESS will show a histogram bar at "20," and the average that is, simply, "20" at this time. Also, the monitor below the plot, MEAN ATTEMPTS TO SUCCESS, will show "20." Now press FIND EQUIDISTANT POINT again. It could be that a different square will be found this time. The plot and monitor will update, again. If you switch the SINGLE-SUCCESS switch to "Off" and press FIND EQUIDISTANT POINT, the model will keep searching and finding. Watch the monitor SAMPLES TAKEN UP TO NOW to see how many attempts have been taken towards completing a sample (the default setting of SAMPLE-SIZE is 1,000). Once a sample has been completed, the plot #SUCCESSES PER SAMPLE will show how many successful attempts there were in that sample.

Buttons:
SETUP -- initialize variables
SELECT SQUARE -- once you press this, you can click on the view to choose squares that become red. If you click again on a red square, it will be un-selected. When you are through with selecting, un-press the button so as not to make further selections by mistake while the model is running.
FIND EQUIDISTANT POINT -- sets off the search procedures. From every red square, dart-like creatures pop up, each headed at some random direction, and then they step forward.
PRESET 1 through 6 -- each 'Preset' button sets the model up with a pre-selected configuration of red squares.
EPICENTER -- activates a procedure in which 10,000 turtles emerge from the center square at random orientations, and all step forward a single step. Pressing this causes you to lose all data from an experiment that you may have been running.

Switches:
SINGLE-SUCCESS -- when "On", the procedure FIND EQUIDISTANT POINT will stop the moment a square has been found that is equally distant from the red squares. When "Off", the procedure will continue again and again, until you un-press FIND EQUIDISTANT POINT.

Monitors:
\#SUCCESSES -- shows how many successes you have had in this experiment, that is, how many times all the creatures landed at the same time in one and the same square.
\#ATTEMPTS  -- shows how many attempts you have had in this experiment, that is, how many times all the creatures have "tried" to land at the same time in one and the same square.
FREQUENCY -- how many successes there have been out of the total number of attempts, expressed as a percentage. This can never be larger than 100 because there cannot be more successes than there are attempts.
SAMPLES TAKEN UP TO NOW -- shows how many samples have been completed, for instance of 1,000 attempts each, and how many attempts have been taken in the current sample.

Plots:
ATTEMPTS UNTIL SUCCESS -- a histogram that updates each time an equidistant square is found, to show how many attempts were needed to find that square.

SUCCESSES PER SAMPLE -- a histogram that updates each time a sample has been completed, for instance of 1,000 attempts, to show how many successes there were in that sample.

Choice:
DISPLAY RESULTS (relates to the 'Epicenter' button)
'by-total' -- squares show the number of creatures that landed on them.
'by-%' -- squares show the percentage of creatures that landed on them out of all landings.

## THINGS TO NOTICE

The creatures' steps are equal in length to the side of a square. If a creature has just emerged in the middle of a square and is heading at 90 degrees, then the step forward will land it at exactly the middle of the square to its right. But if it's headed at 45 degrees, then the step forward will put it in the square that is diagonally neighboring it on the top right, short of the middle of that square.

The values of 'Frequency' and 'Mean #Attempts to Success' are reciprocal:
- Frequency is what you get when you divide #Successes by #Attempts
- Mean #Attempts to Success is what you get by dividing #Attempts by #Successes.
So if you multiply these values, you get 1 (or 100, if you ignore the "%" sign).

For Presets 1 and 2, the creatures will find more than a single square that is equally distant from the red squares. But for Presents 3, 4, 5, and 6, they will only find one such square.

When you press EPICENTER with the choice set at "by-%," the percentages you get are such that the North/West/South/East squares each has roughly 1/6 of the creatures (16.7%), and the cornering patches each has 1/12 of the creature (8.33%).

## THINGS TO TRY

Set the model with Preset1 and run the model with SINGLE-SUCCESS? set to "Off." Note that the middle square will register four times as many successes as compared to the center-left square and as compared to the center-right square. Try to come up with a logical explanation for this.

Set the model with Preset3 and run the model with SINGLE-SUCCESS? set to "Off." Track the values you get in the monitor MEAN #ATTEMPTS TO SUCCESS. Try to find a different selection of squares that gives the same value. What does this selection have in common with the Preset3 selection? Repeat this for the preset conditions 4, 5, and 6.

A very big challenge: The experiments in this model are all based on random choices. After enough attempts, the results begin to stabilize around certain values. Can you determine what these numbers will be before you run an experiment? For instance, can you anticipate the value of MEAN #ATTEMPTS TO SUCCESS on the basis of the configuration of squares that you have selected? To do this, you could think about the information you get from the "Epicenter" procedure. Also, you may want to analyze this problem using pencil and paper.

## EXTENDING THE MODEL

Add a plot for the cumulative ratio of '#Successes' to '#Attempts.' The plot should update at every attempt.

Edit the size of the view so as to include more squares. You will be able to create configurations that you could not create before.

For the "Epicenter" procedure, increase the number of squares in the view. Add interface widgets and code that allow for broader experimentation, such as variety in the number of steps the creatures take and the size of these steps.

## RELATED MODELS

The three different ways of looking at the data in this model are the same as in Prob Graphs Basic: cumulative ratio, attempts until success ("waiting time"), and sampling.

## CREDITS AND REFERENCES

This model is a part of the ProbLab curriculum. The ProbLab Curriculum is currently under development at Northwestern's Center for Connected Learning and Computer-Based Modeling. . For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

Additional reading:
Abrahamson, D. & Wilensky, U.  (2003).  The quest of the bell curve: A constructionist approach to learning statistics through designing computer-based probability experiments. Proceedings of the Third Conference of the European Society for Research in Mathematics Education, Bellaria, Italy, Feb. 28 - March 3, 2003.
Available for download at http://ccl.northwestern.edu/ps/papers/Probability/BellCurve.html
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

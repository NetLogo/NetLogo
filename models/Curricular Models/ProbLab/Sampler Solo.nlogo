;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variable and Breed declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

breed [frames frame]
breed [gatherers gatherer]
breed [clients client]
breed [pending-clients pending-client]

globals
[
  ;; holds the actual value used to determine how many patches should choose
  ;; target-color.  this is set when the setup or rerun buttons are pressed.
  ;; if random-rerun? is true, this will be set to a random value from 0 to 100.
  ;; otherwise, this is set to whatever the current value of %-target-color is.
  real-%-target-color

  ;; Users guess the % of target-color in the population
  target-color  ;; set to green currently
  other-color  ;; set to blue currently
  hide-patch-color  ;; the color that patches are colored when they are not revealed

  ;; holds the number of patches in the observer's last sample
  num-patches-in-observer-sample
  ;;  holds the number of target color patches in the observer's last sample
  num-target-color-patches-in-observer-sample

  ;; if you were to segregate the patches by color, target-color to the left and
  ;; other-color to the right, you would create a contour line at approximately
  ;; xcor-of-organize-contour
  xcor-of-organize-contour

  ;; variables for storing various statistics of guesses
  guesses-list  ;; list of all the clients guesses for this current population
  guess-average  ;; the average of all the students' guesses
  averages-list  ;; list of all the averages since we last pressed rerun or setup
]

patches-own
[
  my-color  ;; variables for when user toggles between samples and reveal displays

  ;; a list of the users (including the observer/instructor/server and all active clients)
  ;; that have this patch in their current sample
  agents-with-me-in-sample
]


;;;;;;;;;;;;;;;;;;;;;
;; Setup Functions ;;
;;;;;;;;;;;;;;;;;;;;;

;; setup the model to be used
to initialize [ full-setup? ]
  clear-plot
  initialize-variables
  initialize-patches
  hide-frames
  abnormality-distribution
  set organize? false
  reset-ticks
  tick
end

;; initialize-variables when rerunning or setting up
to initialize-variables

  init-color-vars

  ;; the contour line between colors will be at a location proportionate to the
  ;; real-%-target-color of screen-size-x.  for example, if real-%-target-color
  ;; is 70 then about 70% of the Graphics Window will be solid target-color
  ;; beginning from the left and extending to the right.
  set xcor-of-organize-contour min-pxcor + (real-%-target-color * world-width / 100)
  set averages-list []
  start-over-variables
end

;; initialize the color-related variables
to init-color-vars
  set target-color green - 1
  set other-color blue
  set hide-patch-color 3
  ifelse random-rerun?
  [
    set real-%-target-color random 101
    set %-target-color 0
  ]
  [ set real-%-target-color %-target-color ]
end

;; initialize variables that are needed when collecting a set of data for the
;; current population
to start-over-variables
  set num-patches-in-observer-sample 0
  set num-target-color-patches-in-observer-sample 0
  set guesses-list []
  set guess-average 0
end

;; initialize patch variables
to initialize-patches
  set-default-shape frames "frame"
  ask patches
  [
    initialize-patch-colors
    setup-frames
    set agents-with-me-in-sample []
  ]
end

;; initialize patches colors.  this is where the patches determine whether they
;; are target-color or other-color
to initialize-patch-colors  ;; patch procedure
  set pcolor hide-patch-color  ;; default color of patches when not revealed
  ;; each patch "throws a die" to determine if it's target-color or other-color
  ifelse random-float 100.0 < real-%-target-color
  [ set my-color target-color ]
  [ set my-color other-color ]
end

;; each patch creates a frame which give the optional grid appearance
to setup-frames  ;; patch procedure
  if not any? frames-here
  [
    sprout 1
    [
      set breed frames
      ht
    ]
  ]
end

;; ask all the frames to hide, even if grid? is true
to hide-frames
  ask frames
  [ ht ]
end

;; create and kill gatherers which modify the bi-linear random distribution of
;; target-color and other-color patches in the population.  in other words, it
;; creates turtles which clump like-colored patches
to abnormality-distribution
  create-gatherers (4 + random 4)  ;; create gatherers
  [
    ht
    setxy (random-float world-width) (random-float world-height)
    rt random-float 360.0
    gather
    die
  ]
end

;; clump like-colored patches by swapping patch colors from progressively
;; further patches around your location based on the abnormality slider
to gather  ;; gatherer procedure
  let index 0
  let a-patch 0
  let another-patch 0

  set index ( 20 * abnormality )
  while [ index > 0 ]
  [
    set a-patch one-of patches in-radius 16 with [ my-color = target-color ]
    set another-patch one-of patches in-radius 8 with [ my-color = other-color ]
    if (a-patch != nobody and another-patch != nobody)
    [ swap-my-color a-patch another-patch ]
    set index (index - 1)
  ]
end

;; swap my-colors between two patches
to swap-my-color [ a-patch another-patch ]
  let color-buffer 0

  set color-buffer [my-color] of a-patch
  ask a-patch [ set my-color [my-color] of another-patch ]
  ask another-patch [ set my-color color-buffer ]
end

;; setup the model for use.  this kills all the turtles and should only be used
;; with a new group
to setup
    ca
    set organize? false
    initialize true ;; initialize turns back on the display
    reset-ticks
 ; ]
end

;; setup the model for use.  this doesn't kill all the turtles and so should be
;; used if you want to create a new population for the current set of clients.
to go
  initialize false
end

;; make sure that the user really wants to use the same population

;; prepare the patches to be sampled with the same population
to start-over-patches
  ask patches
  [
    set pcolor hide-patch-color
    ;; don't clear the agents-with-me-in-sample list if we are keeping the samples
    if not keep-samples?
    [ set agents-with-me-in-sample [] ]
  ]
  hide-frames
end

;; plot means and mean of means
to start-over-plot
  let list-counter 0

  clear-plot
  set-current-plot-pen "means"
  set list-counter 0
  ;; plot a line for each item in averages-list
  repeat length averages-list
  [
    plot-pen-up
    plotxy (item list-counter averages-list)  0
    plot-pen-down
    plotxy (item list-counter averages-list) 25
    set list-counter list-counter + 1
  ]
  ;; if there are at least 2 items in averages-list, plot the mean of all the items
  if length averages-list > 1
  [
    set-current-plot-pen "mean-of-means"
    plot-pen-up
    plotxy mean averages-list 0
    plot-pen-down
    plotxy mean averages-list 25
  ]
end


;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;

;; if the user clicks in the graphics window, take a sample where the click
;; occurred.  the size of the sample depends upon the current value of the
;; sample-block-side slider
to sample
  if mouse-down?
  [
    change-sample (round mouse-xcor) (round mouse-ycor) sample-block-side "observer"
  ]
  tick
end

;; based on input, create the chosen sample and have all patches in it set pcolor to my-color
to change-sample [ x y block-side agent ]
  let patches-in-sample 0

  set patches-in-sample nobody

  ;; designates all patches in a square around the mouse spot as being in the sample.
  ;; the sample-block side is set on the interface using the slider
  ask patch x y
  [ set patches-in-sample (in-rectangle patches ((block-side - 1) / 2)  ((block-side - 1) / 2) ) ]

  ;; hide all the patches and frames
  ask patches with [ true ]
  [ set pcolor hide-patch-color ]
  ask frames
  [ ht ]

  ;; show the patches their frames (if grid? is true) in the currently chosen sample
  ask patches-in-sample
  [
    set pcolor my-color
    ask frames-here
    [ set hidden? not grid? ]
  ]

  if keep-samples?
  [
    ;; show all the patches which were previously sampled while
    ;; keep-samples? was true
    ask patches with [ member? agent agents-with-me-in-sample ]
    [ set pcolor my-color ]

    ask patches-in-sample
    [
      ;; add agent to my list of agents that have me in their samples
      ;; so that the agent will keep seeing me when taking subsequent
      ;; samples (if keep-samples? is true)
      if( not member? agent agents-with-me-in-sample )
      [ set agents-with-me-in-sample fput agent agents-with-me-in-sample ]
    ]
  ]
  if agent = "observer"
  [
    set num-patches-in-observer-sample count patches-in-sample
    set num-target-color-patches-in-observer-sample count patches-in-sample with [pcolor = target-color]
  ]
end

;; report the agentset within a box that has a width = (width/2 * 2) + 1
;; and a height = (height/2 * 2) + 1 and is centered on the calling agent
to-report in-rectangle [ patchset width/2 height/2 ]  ;; turtle and patch procedure
  ;; this procedure does not work with a non-patch-agentset variable
  if not is-patch-set? patchset
  [ report nobody ]
  report patchset with
  [
    pxcor <= ([pxcor] of myself + width/2) and pxcor >= ([pxcor] of myself - width/2) and
    pycor <= ([pycor] of myself + height/2) and pycor >= ([pycor] of myself - height/2)
  ]
end

;; reveal the whole population - have all the patches show their 'true' colors
to reveal
  set num-patches-in-observer-sample 0

  ;; grid? is the option to have frames around each patch
  ask frames
  [ set hidden? not grid? ]

  ask patches
  [
    ifelse organize?
    [
      ;; When we reveal the population with organize? true, we get a vertical
      ;; contour. Patches with target-color are on the left and other
      ;; patches are on the right
      ifelse pxcor < round xcor-of-organize-contour
      [ set pcolor target-color ]
      [ set pcolor other-color ]
     set %-target-color real-%-target-color
    ]
    [ set pcolor my-color ]
  ]
  tick
end

;; reports the proportion of patches with target-color in the current sample
;; if there has been no sample chosen yet, patches-in-sample is nobody.  so
;; report -1 as an error code.
to-report target-color-%-in-sample
  ifelse (num-patches-in-observer-sample = 0)
  [ report -1 ]
  [ report ( 100 * num-target-color-patches-in-observer-sample / num-patches-in-observer-sample ) ]
end


;; cause the graphics window to show all the samples that students have
;; taken this far
to pool-samples
  ask patches
  [
    ifelse length agents-with-me-in-sample = 0
    [ set pcolor hide-patch-color ]
    [
      set pcolor my-color
      ;; put all the clients (including "observer") into agents-with-me-in-sample
      set agents-with-me-in-sample lput "observer" sort clients
    ]
  ]
  tick
end


to simulate-classroom-histogram
  let class-size 25
  let err 0
  let class-mean 0
  let stand-dev 0
  if real-%-target-color = 0 [set stand-dev 0 set class-mean 0 set err 0]
  if real-%-target-color > 0 and  real-%-target-color <= 5  [set class-mean %-target-color + 2 set stand-dev 1.5 set err 1]
  if real-%-target-color > 5 and  real-%-target-color <= 7 [set class-mean %-target-color + 3 set stand-dev 2 set err -2 + random 5]
  if real-%-target-color > 7 and real-%-target-color <= 30 [set class-mean %-target-color + 3    set stand-dev 6 set err -2 + random 5]
  if real-%-target-color > 30 and real-%-target-color <= 70 [set class-mean %-target-color + 3    set stand-dev 8 set err -3 + random 7]
  if real-%-target-color > 70 and real-%-target-color <= 93 [set class-mean %-target-color + 3    set stand-dev 6 set err -2 + random 5]
  if real-%-target-color > 93 and real-%-target-color < 100 [set class-mean %-target-color - 2   set stand-dev 1.5 set err -1]
  if real-%-target-color = 100 [set stand-dev 0 set class-mean 100 set err 0]

  set class-mean ( real-%-target-color + err )
  set organize? true
  reveal
  set guesses-list []
  repeat class-size [ set guesses-list ( lput random-normal class-mean stand-dev guesses-list ) ]
  plot-guesses
end


to plot-guesses
  set-current-plot "Student Guesses"
  clear-plot
  set-current-plot-pen "guesses"
  set-histogram-num-bars 100 / cluster-guesses
  histogram guesses-list
  set-current-plot-pen "mean-of-guesses"
  plot-pen-up plotxy (mean guesses-list) plot-y-min
  plot-pen-down plotxy (mean guesses-list) plot-y-max
end
@#$#@#$#@
GRAPHICS-WINDOW
280
45
690
476
-1
-1
4.0
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
99
0
99
1
1
0
ticks
30

SLIDER
120
160
265
193
sample-block-side
sample-block-side
1
11
3
2
1
NIL
HORIZONTAL

PLOT
260
480
805
615
Student Guesses
 %-target-color
# students
0.0
100.0
0.0
6.0
true
true
"" ""
PENS
"guesses" 1.0 1 -2674135 true "" ""
"mean-of-guesses" 1.0 0 -5825686 true "" ""
"means" 1.0 0 -6459832 true "" ""
"mean-of-means" 1.0 0 -11221820 true "" ""

BUTTON
10
120
118
155
Reveal Pop.
reveal
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
121
121
266
154
organize?
organize?
1
1
-1000

SWITCH
120
230
265
263
grid?
grid?
1
1
-1000

SLIDER
280
10
690
43
%-target-color
%-target-color
0
100
50
1
1
NIL
HORIZONTAL

BUTTON
10
45
117
115
Rerun
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
10
355
265
388
cluster-guesses
cluster-guesses
1
10
3
1
1
NIL
HORIZONTAL

BUTTON
10
160
118
265
Sample
sample
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
120
195
265
228
keep-samples?
keep-samples?
1
1
-1000

BUTTON
10
10
265
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

SLIDER
120
80
265
113
abnormality
abnormality
0
10
4
1
1
NIL
HORIZONTAL

SWITCH
120
45
265
78
random-rerun?
random-rerun?
1
1
-1000

BUTTON
10
270
265
319
Simulate Classroom Histogram
simulate-classroom-histogram
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
320
265
353
Clear Histogram
clear-plot
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
25
480
245
605
The histogram simulates a typical run in a classroom. The aggregation of students' guesses approximates a normal distribution, and the classroom mean is a few % scores off of the population value.
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

SAMPLER Solo is part of the ProbLab curricular models, in which students engage in probability-and-statistics activities as individuals and as a classroom.  SAMPLER stands for Statistics as Multi-Participant Learning-Environment Resource.  It is called "multi-participant", because there is a HubNet version of this model, which was created before the solo version was built.

In both versions, users take samples from a giant grid consisting of tiny squares that are revealed as either green or blue, and then the users guess the greenness of the grid on the basis of these samples. The suggested activities are designed to create opportunities for learners to discover and discuss several basic notions of statistics, such as mean, distribution, and margin of error.

In SAMPLER Solo, a teacher can prepare to run the HubNet version by getting to know the environment and encountering features that simulate the classroom functioning of the model.  Or a student could work on the model on her own, in class or at home, whether to prepare for participating in the classroom version - becoming an expert sampler! - or as a follow-up activity.  Or one could work on the model regardless of the classroom context.

## PEDAGOGICAL NOTE

In SAMPLER, statistics is presented as a task of making inferences about a population under conditions of uncertainty due to limited resources. For example, if you wanted to know what percentage of students in your city speak a language other than English, how would you go about it? Would it be enough to measure the distribution of this variable in your own classroom? If so, then how sure could you be that your conclusions hold for a school in another neighborhood? Are there certain groups of people that it would make more sense to use as a sample? For instance, would it make sense to stand outside a movie house that is showing a French film with no subtitles and ask each patron whether they speak a second language? Is this a representative sample? Should we look at certain parts of town? Would all parts of town be the same? Oh, and by the way, what is an average (a mean)? A variable? A value? What does it mean to measure a distribution of a variable within a population?

Many students experience difficulty understanding statistics--not only in middle and high school, but also in college and beyond.  Yet, we all share certain natural capabilities that could be thought of as naive statistics. These capabilities are related to proportional judgments that we cast in our everyday life. We make proportional judgments when we need to decide how to maximize the utility of our actions on the basis of our cumulative experience of how events were distributed For instance, when we come to a new place we may say, "People in this town are very nice." How did we decide that? Or, "Don't buy fruit there--it's often overripe." How did we infer that? Or, "To get to school, take Main street--it's the fastest route in the morning; but drive back through High street, I find that's faster in the afternoon."

## HOW IT WORKS

The View features a "population" of 10,000 tiny squares that are each either green or blue, like a giant matrix of coins that are each either heads or tail.  The model can be set to select a random value for the proportion of green and blue in the population, so that the user does not know this value.  Moreover, these colors are not initially shown - the squares are grey - so that we cannot initially use proportional judgment to guess the proportion of green in the population.  Yet we can reveal the hidden color by "sampling", that is, by clicking on the View.  When you click on a square, not only do you reveal that particular square, but also its surrounding squares, so that the overall shape of the sample is a larger square. The total number of tiny squares revealed in this sample will be determined by the current value set in a slider called "sample-block-side". For example, if the slider is set at 3, taking a sample will expose a total of 9 squares (3-by-3), with the clicked square in the center. On the basis of the revealed colors, you can make a calculated guesses as to the percentage of green squares within the entire population. You can then reveal the population to find out this unknown value.

A special feature of this model is that it includes an element that simulates what it looks like to use the HubNet version of this model. In the classroom PSA, all students input their guesses for green, and these collective guesses are plotted as a histogram. In the solo model it's only you guessing, and so the model creates histograms that are meant to demonstrate distributions of guesses that a classroom might produce. These demo distributions are based on what we saw in classes where we researched SAMPLER.

## HOW TO USE IT

SETUP -- initializes all variables
RERUN - creates a new population
REVEAL POP - all tiny squares reveal their color
SAMPLE - enables sampling by clicking on the View
SIMULATE CLASSROOM HISTOGRAM - creates in the graphics window STUDENT GUESSES a histogram similar to what you would see if you were working as part of a large group.
CLEAR HISTOGRAM - returns the histogram to its initial blank state.
CLUSTER-GUESSES? - use this slider to control the size of the bins in the histogram. This can be helpful to see the distribution of guesses in the simulated classroom collective guesses.
RANDOM-RERUN? - When Off, the Rerun will create a population with a greenness proportion set in the %-TARGET-COLOR slider. When On, the model will assign a random value for the greenness, irrespective of the slider value (and it will send that slider to 0).
ABNORMALITY - determines the "clumpiness" of the green and blue squares in the population. When set at 0, there is typically no clumpiness (the distribution is normal). The higher the value, the larger the clusters of green (the distribution is "abnormal").
ORGANIZE? - if at Off, then when you reveal the population, each square will show its true color. However, when On, the green and blue squares relocate, as through there were a green magnet on the left and a blue one on the right. This results in a contour running vertically down the View. Note: the contour is always a straight line, because it rounds up to the nearest line.
SAMPLE-BLOCK-SIDE - determines the size of the sample, for example the value 3 will give a 3-by-3 sample for a total of 9 exposed squares.
KEEP-SAMPLES? - When set to Off, each time you take a sample, the previous sample becomes dark grey again. When set to On, the next sample you take, and all subsequent samples, will remain revealed.
GRID? - When set to On, the View will feature a grid. The grid is helpful to interpret a sample in cases of extreme green or blue, because it enables you to count the number of squares.

## THINGS TO NOTICE

When you press REVEAL POP and Organize? Is On, the green and blue colors "move" to the left and the right of the screen, respectively, forming a contour line. The location of this contour line is comparable to two other elements on the interface: the contour line falls directly below the slider handle above it and directly above the mean line of the histogram. The reason we can compare these three features directly is because the 0 and 'whole' (100%) of each of these features are aligned. That is, the sliders, graphics window, and plot have all been placed carefully so as to subtend each other precisely.

## THINGS TO TRY

Set RANDOM-RERUN? to ON, press RERUN, and now take some samples. What is the minimal number of samples you need in order to feel you have a reasonable sense of the overall greenness in the population?

Try setting the ABNORMALITY slider to different values and press RERUN over and over for the same percentage green, for instance 50%. Can you think of situations in the world where a certain attribute is distributed in a population in a way that corresponds to a high value of ABNORMALITY? What do we mean when we speak of a 'uniform distribution' within a population? For instance, is a distribution of ABNORMALITY = 0 uniform? Or must there be strict order, for instance stripes of target-color, in order for you to feel that the distribution is uniform? Also, is there a difference between your sense of uniformity whether you're looking at the whole population or just at certain parts of it? If you threw a handful of pebbles onto a square area, would you say they fell 'uniformly'? What kinds of patterns are natural, and what kinds of patterns would you think of as coincidental?

## EXTENDING THE MODEL

Change the procedure that separates green an blue, so that same green and blue squares remain when you reveal the population.

## NETLOGO FEATURES

The abnormality distribution feature does not take much code to write, but is effective. Look at the code and try to understand it.

## RELATED MODELS

See the HubNet SAMPLER model.

## CREDITS AND REFERENCES

This model is a part of the ProbLab curriculum  Thanks to Dor Abrahamson for his design of ProbLab.  The ProbLab curriculum is currently under development at Northwestern's Center for Connected Learning and Computer-Based Modeling and at the Graduate School of Education at University of California, Berkeley. For more information about the ProbLab curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

ant
true
0
Polygon -7500403 true true 136 61 129 46 144 30 119 45 124 60 114 82 97 37 132 10 93 36 111 84 127 105 172 105 189 84 208 35 171 11 202 35 204 37 186 82 177 60 180 44 159 32 170 44 165 60
Polygon -7500403 true true 150 95 135 103 139 117 125 149 137 180 135 196 150 204 166 195 161 180 174 150 158 116 164 102
Polygon -7500403 true true 149 186 128 197 114 232 134 270 149 282 166 270 185 232 171 195 149 186
Polygon -7500403 true true 225 66 230 107 159 122 161 127 234 111 236 106
Polygon -7500403 true true 78 58 99 116 139 123 137 128 95 119
Polygon -7500403 true true 48 103 90 147 129 147 130 151 86 151
Polygon -7500403 true true 65 224 92 171 134 160 135 164 95 175
Polygon -7500403 true true 235 222 210 170 163 162 161 166 208 174
Polygon -7500403 true true 249 107 211 147 168 147 168 150 213 150

arrow
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

bee
true
0
Polygon -1184463 true false 152 149 77 163 67 195 67 211 74 234 85 252 100 264 116 276 134 286 151 300 167 285 182 278 206 260 220 242 226 218 226 195 222 166
Polygon -16777216 true false 150 149 128 151 114 151 98 145 80 122 80 103 81 83 95 67 117 58 141 54 151 53 177 55 195 66 207 82 211 94 211 116 204 139 189 149 171 152
Polygon -7500403 true true 151 54 119 59 96 60 81 50 78 39 87 25 103 18 115 23 121 13 150 1 180 14 189 23 197 17 210 19 222 30 222 44 212 57 192 58
Polygon -16777216 true false 70 185 74 171 223 172 224 186
Polygon -16777216 true false 67 211 71 226 224 226 225 211 67 211
Polygon -16777216 true false 91 257 106 269 195 269 211 255
Line -1 false 144 100 70 87
Line -1 false 70 87 45 87
Line -1 false 45 86 26 97
Line -1 false 26 96 22 115
Line -1 false 22 115 25 130
Line -1 false 26 131 37 141
Line -1 false 37 141 55 144
Line -1 false 55 143 143 101
Line -1 false 141 100 227 138
Line -1 false 227 138 241 137
Line -1 false 241 137 249 129
Line -1 false 249 129 254 110
Line -1 false 253 108 248 97
Line -1 false 249 95 235 82
Line -1 false 235 82 144 100

bird1
false
0
Polygon -7500403 true true 2 6 2 39 270 298 297 298 299 271 187 160 279 75 276 22 100 67 31 0

bird2
false
0
Polygon -7500403 true true 2 4 33 4 298 270 298 298 272 298 155 184 117 289 61 295 61 105 0 43

blues
true
0
Circle -13345367 true false 77 76 148

boat1
false
0
Polygon -1 true false 63 162 90 207 223 207 290 162
Rectangle -6459832 true false 150 32 157 162
Polygon -13345367 true false 150 34 131 49 145 47 147 48 149 49
Polygon -7500403 true true 158 33 230 157 182 150 169 151 157 156
Polygon -7500403 true true 149 55 88 143 103 139 111 136 117 139 126 145 130 147 139 147 146 146 149 55

boat2
false
0
Polygon -1 true false 63 162 90 207 223 207 290 162
Rectangle -6459832 true false 150 32 157 162
Polygon -13345367 true false 150 34 131 49 145 47 147 48 149 49
Polygon -7500403 true true 157 54 175 79 174 96 185 102 178 112 194 124 196 131 190 139 192 146 211 151 216 154 157 154
Polygon -7500403 true true 150 74 146 91 139 99 143 114 141 123 137 126 131 129 132 139 142 136 126 142 119 147 148 147

boat3
false
0
Polygon -1 true false 63 162 90 207 223 207 290 162
Rectangle -6459832 true false 150 32 157 162
Polygon -13345367 true false 150 34 131 49 145 47 147 48 149 49
Polygon -7500403 true true 158 37 172 45 188 59 202 79 217 109 220 130 218 147 204 156 158 156 161 142 170 123 170 102 169 88 165 62
Polygon -7500403 true true 149 66 142 78 139 96 141 111 146 139 148 147 110 147 113 131 118 106 126 71

box
true
0
Polygon -7500403 true true 45 255 255 255 255 45 45 45

butterfly1
true
0
Polygon -16777216 true false 151 76 138 91 138 284 150 296 162 286 162 91
Polygon -7500403 true true 164 106 184 79 205 61 236 48 259 53 279 86 287 119 289 158 278 177 256 182 164 181
Polygon -7500403 true true 136 110 119 82 110 71 85 61 59 48 36 56 17 88 6 115 2 147 15 178 134 178
Polygon -7500403 true true 46 181 28 227 50 255 77 273 112 283 135 274 135 180
Polygon -7500403 true true 165 185 254 184 272 224 255 251 236 267 191 283 164 276
Line -7500403 true 167 47 159 82
Line -7500403 true 136 47 145 81
Circle -7500403 true true 165 45 8
Circle -7500403 true true 134 45 6
Circle -7500403 true true 133 44 7
Circle -7500403 true true 133 43 8

circle
false
0
Circle -7500403 true true 35 35 230

frame
false
14
Rectangle -7500403 false false 0 1 296 296
Rectangle -7500403 false false 0 2 297 297
Rectangle -7500403 false false 0 0 298 298
Rectangle -7500403 false false 1 3 295 295
Rectangle -7500403 false false 1 1 299 299

person
false
0
Circle -7500403 true true 155 20 63
Rectangle -7500403 true true 158 79 217 164
Polygon -7500403 true true 158 81 110 129 131 143 158 109 165 110
Polygon -7500403 true true 216 83 267 123 248 143 215 107
Polygon -7500403 true true 167 163 145 234 183 234 183 163
Polygon -7500403 true true 195 163 195 233 227 233 206 159

reds
true
0
Circle -7500403 true true 76 77 148
Circle -2674135 true false 77 78 148

spacecraft
true
0
Polygon -7500403 true true 150 0 180 135 255 255 225 240 150 180 75 240 45 255 120 135

thin-arrow
true
0
Polygon -7500403 true true 150 0 0 150 120 150 120 293 180 293 180 150 300 150

truck-down
false
0
Polygon -7500403 true true 225 30 225 270 120 270 105 210 60 180 45 30 105 60 105 30
Polygon -8630108 true false 195 75 195 120 240 120 240 75
Polygon -8630108 true false 195 225 195 180 240 180 240 225

truck-left
false
0
Polygon -7500403 true true 120 135 225 135 225 210 75 210 75 165 105 165
Polygon -8630108 true false 90 210 105 225 120 210
Polygon -8630108 true false 180 210 195 225 210 210

truck-right
false
0
Polygon -7500403 true true 180 135 75 135 75 210 225 210 225 165 195 165
Polygon -8630108 true false 210 210 195 225 180 210
Polygon -8630108 true false 120 210 105 225 90 210

wolf-left
false
3
Polygon -6459832 true true 117 97 91 74 66 74 60 85 36 85 38 92 44 97 62 97 81 117 84 134 92 147 109 152 136 144 174 144 174 103 143 103 134 97
Polygon -6459832 true true 87 80 79 55 76 79
Polygon -6459832 true true 81 75 70 58 73 82
Polygon -6459832 true true 99 131 76 152 76 163 96 182 104 182 109 173 102 167 99 173 87 159 104 140
Polygon -6459832 true true 107 138 107 186 98 190 99 196 112 196 115 190
Polygon -6459832 true true 116 140 114 189 105 137
Rectangle -6459832 true true 109 150 114 192
Rectangle -6459832 true true 111 143 116 191
Polygon -6459832 true true 168 106 184 98 205 98 218 115 218 137 186 164 196 176 195 194 178 195 178 183 188 183 169 164 173 144
Polygon -6459832 true true 207 140 200 163 206 175 207 192 193 189 192 177 198 176 185 150
Polygon -6459832 true true 214 134 203 168 192 148
Polygon -6459832 true true 204 151 203 176 193 148
Polygon -6459832 true true 207 103 221 98 236 101 243 115 243 128 256 142 239 143 233 133 225 115 214 114

wolf-right
false
3
Polygon -6459832 true true 170 127 200 93 231 93 237 103 262 103 261 113 253 119 231 119 215 143 213 160 208 173 189 187 169 190 154 190 126 180 106 171 72 171 73 126 122 126 144 123 159 123
Polygon -6459832 true true 201 99 214 69 215 99
Polygon -6459832 true true 207 98 223 71 220 101
Polygon -6459832 true true 184 172 189 234 203 238 203 246 187 247 180 239 171 180
Polygon -6459832 true true 197 174 204 220 218 224 219 234 201 232 195 225 179 179
Polygon -6459832 true true 78 167 95 187 95 208 79 220 92 234 98 235 100 249 81 246 76 241 61 212 65 195 52 170 45 150 44 128 55 121 69 121 81 135
Polygon -6459832 true true 48 143 58 141
Polygon -6459832 true true 46 136 68 137
Polygon -6459832 true true 45 129 35 142 37 159 53 192 47 210 62 238 80 237
Line -16777216 false 74 237 59 213
Line -16777216 false 59 213 59 212
Line -16777216 false 58 211 67 192
Polygon -6459832 true true 38 138 66 149
Polygon -6459832 true true 46 128 33 120 21 118 11 123 3 138 5 160 13 178 9 192 0 199 20 196 25 179 24 161 25 148 45 140
Polygon -6459832 true true 67 122 96 126 63 144

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
-0.2 0 1.0 0.0
0.0 1 1.0 0.0
0.2 0 1.0 0.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

@#$#@#$#@
1
@#$#@#$#@

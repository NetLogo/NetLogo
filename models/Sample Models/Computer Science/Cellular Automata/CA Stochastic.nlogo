globals [
  row        ;; current row we are now calculating
  done?      ;; flag set to allow you to press the go button multiple times
  prob-list  ;; list of the probabilities of each pattern occurring
]

patches-own [on?]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; SETUP PROCEDURES     ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; setup general working environment.  the other setup procedures call this.
to setup-general
  clear-patches
  set row max-pycor   ;; reset current row
  set done? false
end

;; setup a random selection of patches in the top row to have on? true
to setup-random
  setup-general
  clear-all-plots
  ;; randomly place cells across the top of the world
  ask patches with [pycor = row]
  [
    set on? ((random-float 100) < density)
    color-patch
  ]
  build-prob-list
  reset-ticks
end

;; setup the patches to continue a particular model run.  this will copy the bottom
;; row of patches to the top row.
to setup-continue
  let on?-list []

  ;; make sure go has already been called
  if not done?  [ stop ]

  if auto-clear?
  [
    clear-plot
    set-plot-x-range ticks (ticks + world-height)
  ]

  set on?-list map [ [on?] of ?] sort patches with [ pycor = row ]  ;; copy states from bottom row to list
  setup-general
  ask patches with [ pycor = row ]
  [
    set on? item (pxcor + max-pxcor) on?-list  ;; copy states from list to top row
    color-patch
  ]
  set done? false
end

;; setup the sliders to have specific values that are interesting to study
to setup-example
  if( example = 1 )
    [ set III 0 set IIO 50 set IOI 50 set IOO 50 set OII 50 set OIO 50 set OOI 50 set OOO 0 ]
  if( example = 2 )
    [ set III 0 set IIO 50 set IOI 0 set IOO 50 set OII 50 set OIO 100 set OOI 50 set OOO 100 ]
  if( example = 3 )
    [ set III 0 set IIO 50 set IOI 50 set IOO 66 set OII 50 set OIO 50 set OOI 100 set OOO 0 ]
  if( example = 4 )
    [ set III 0 set IIO 50 set IOI 50 set IOO 66 set OII 50 set OIO 50 set OOI 50 set OOO 0 ]
  if( example = 5 )
    [ set III 0 set IIO 100 set IOI 0 set IOO 66 set OII 100 set OIO 0 set OOI 66 set OOO 0 ]
  set density 25
  setup-random
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; RUNTIME PROCEDURES   ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; run the model.  this applies the current rules to the patches with pycor equal to row.
;; if that row of patches is the bottom row and auto-continue? is true, we will setup the
;; model to continue.
to go
   ;; if the end has been reached, continue from the top or stop
  if (row = min-pycor)
  [
    ifelse auto-continue?
    [
       ;; if we are stuck in an absorbing state, there is not reason to continue
       ifelse( ((sum ([true-false-to-int on?] of patches with [pycor = row]) = 0) and OOO = 0.0) or
               ((sum ([true-false-to-int on?] of patches with [pycor = row]) = world-width)
                 and III = 100.0))
         [ stop ]
         [
           set done? true
           display    ;; ensure all the patches get drawn before we clear
           setup-continue
         ]
    ]
    [
      ;; if a run has already been completed, continue with another.  otherwise just stop
      ifelse done?
        [ setup-continue ]
        [
          set done? true
          stop
        ]
    ]
  ]
  ask patches with [ pycor = row ]  ;; apply rule
    [ do-rule ]

  set row (row - 1)
  ask patches with [ pycor = row ]  ;; color in changed cells
    [ color-patch ]

  build-prob-list
  tick
end

;; the patch will set the on? value of the patch below it based on three factors,
;; 1) its own on? value
;; 2) the on? values of the patches to the left and right of it
;; 3) the current settings for the rules
to do-rule  ;; patch procedure
  let left-on? [on?] of patch-at -1 0  ;; set to true if the patch to the left is on
  let right-on? [on?] of patch-at 1 0  ;; set to true if the patch to the right is on

  ;; each of these lines checks the local area and (possibly)
  ;; sets the lower cell according to the corresponding switch
  let new-value
    (III != 0  and left-on?       and on?       and right-on?          and (random-float 100) < III)    or
    (IIO != 0  and left-on?       and on?       and (not right-on?)    and (random-float 100) < IIO)    or
    (IOI != 0  and left-on?       and (not on?) and right-on?          and (random-float 100) < IOI)    or
    (IOO != 0  and left-on?       and (not on?) and (not right-on?)    and (random-float 100) < IOO)    or
    (OII != 0  and (not left-on?) and on?       and right-on?          and (random-float 100) < OII)    or
    (OIO != 0  and (not left-on?) and on?       and (not right-on?)    and (random-float 100) < OIO)    or
    (OOI != 0  and (not left-on?) and (not on?) and right-on?          and (random-float 100) < OOI)    or
    (OOO != 0  and (not left-on?) and (not on?) and (not right-on?)    and (random-float 100) < OOO)
  ask patch-at 0 -1 [ set on? new-value ]
end

;; for plotting
to build-prob-list
  let i min-pxcor  ;; index of where in the row we are currently doing calculations
  ;; make a 16 element list for storing the number of occurrences of each distinct 4-patch pattern
  let counter-list n-values 16 [0]

  ;; fill the counter-list with the appropriate values.  that is to say, count
  ;; the number of occurrences of each distinct 4-patch pattern of on? values.
  while [ i < max-pxcor ]
  [
    let an-index binary-list-to-index (patches-to-binary-list i)
    set counter-list replace-item an-index counter-list (item an-index counter-list + 1)
    set i (i + 1)
  ]

  ;; make a list of the probabilities of each pattern occurring
  set prob-list map [? / (sum counter-list)] counter-list
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; UTILITY PROCEDURES   ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; report the base 10 equivalent of a binary number represented by a list of 0's and 1's.
;; in the list, the 0th item is the highest power of 2 and the highest item is the lowest power.
to-report binary-list-to-index [binary-list]
  let list-sum 0
  let i 0
  let list-length length binary-list
  while [i < list-length ] [
    set list-sum list-sum + (2 ^ i) * ((item (list-length - i - 1) binary-list) mod 2)
    set i i + 1
  ]
  report list-sum
end

;; report a list of binary digits based on the values of on? for the current row's
;; patches starting at 'offset'
to-report patches-to-binary-list [offset]
  let binary-list []
  set binary-list lput ([true-false-to-int on?] of patch offset row) binary-list
  set binary-list lput ([true-false-to-int on?] of patch (offset + 1) row) binary-list
  set binary-list lput ([true-false-to-int on?] of patch (offset + 2) row) binary-list
  set binary-list lput ([true-false-to-int on?] of patch (offset + 3) row) binary-list
  report binary-list
end

;; convert true/false values to 1/0
to-report true-false-to-int [b]
  ifelse b
    [ report 1 ]
    [ report 0 ]
end

;; color the patch based on whether on? is true or false
to color-patch  ;; patch procedure
  ifelse on?
    [ set pcolor on-color ]
    [ set pcolor off-color ]
end
@#$#@#$#@
GRAPHICS-WINDOW
348
10
680
363
80
80
2.0
1
10
1
1
1
0
1
0
1
-80
80
-80
80
1
1
1
ticks
30.0

BUTTON
11
98
110
131
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

SLIDER
11
159
116
192
III
III
0
100
0
0.5
1
%
HORIZONTAL

SLIDER
119
159
224
192
IIO
IIO
0
100
50
0.5
1
%
HORIZONTAL

SLIDER
11
194
116
227
IOI
IOI
0
100
50
0.5
1
%
HORIZONTAL

SLIDER
119
194
224
227
IOO
IOO
0
100
50
0.5
1
%
HORIZONTAL

SLIDER
11
229
116
262
OII
OII
0
100
50
0.5
1
%
HORIZONTAL

SLIDER
119
229
224
262
OIO
OIO
0
100
50
0.5
1
%
HORIZONTAL

SLIDER
11
264
116
297
OOI
OOI
0
100
50
0.5
1
%
HORIZONTAL

SLIDER
119
264
224
297
OOO
OOO
0
100
0
0.5
1
%
HORIZONTAL

SLIDER
130
16
248
49
density
density
0
100
25
0.5
1
%
HORIZONTAL

BUTTON
11
16
130
49
Setup Random
setup-random
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
110
98
248
131
auto-continue?
auto-continue?
1
1
-1000

TEXTBOX
21
141
210
159
Probability Cell Turns On:
11
0.0
0

SLIDER
12
358
184
391
off-color
off-color
0
100
0
0.5
1
NIL
HORIZONTAL

SLIDER
12
323
184
356
on-color
on-color
0
100
95
0.5
1
NIL
HORIZONTAL

TEXTBOX
24
305
114
323
Colors:
11
0.0
0

BUTTON
11
51
130
84
Setup Example
setup-example
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
130
51
248
84
example
example
1
5
1
1
1
NIL
HORIZONTAL

PLOT
212
365
680
536
entropy-plot
time
entropy
0.0
320.0
0.0
1.0
true
true
"" "if not plot? [ stop ]"
PENS
"topologic" 1.0 0 -8630108 true "" ";; X = 4 (correlation length): size of subsequences analyzed\n;; topological entropy = 1/X * the log of the sum of the probabilities rounded up\nplotxy ticks 1 / 4 * (log (sum (map [1] (filter [? > 0] prob-list))) 2)\n"
"metric" 1.0 0 -955883 true "" ";; metric entropy = -1/X * the sum of the products of each probability and its log\nplotxy ticks -1 / 4 * sum ( map[? * log ? 2] (filter [? > 0] prob-list))"

SWITCH
225
298
345
331
plot?
plot?
0
1
-1000

SWITCH
225
330
345
363
auto-clear?
auto-clear?
0
1
-1000

@#$#@#$#@
## WHAT IS IT?

This is a one-dimensional stochastic cellular automaton.  (See the CA 1D Elementary model if you are unfamiliar with cellular automata.)  Unlike most cellular automata, whose behavior is deterministic, the behavior of a stochastic cellular automaton is probabilistic.  Stochastic cellular automata are models of "noisy" systems in which processes do not function exactly as expected, like most processes found in natural systems.

The behavior of these cellular automata tend to be very rich and complex, often forming self-similar tree-like or chaotic behavior.  They are capable of mimicking many phenomena found in nature such as crystal growth, boiling, and turbulence.

## HOW IT WORKS

At each time step, every cell in the current row evaluates the state of itself and its immediate neighbors to the right and left.  There are 8 possible on/off rule configurations for every 3-cell neighborhood, each with a certain probability of turning on the cell below it at the next time step.  The rules are applied accordingly, and the next state of the cellular automaton appears in the row directly below, creating a space vs. time view of the cellular automaton's evolution.

## HOW TO USE IT

Set up:
- SETUP RANDOM initializes the model with a percentage of the cells "on". The percentage on is determined by the DENSITY slider.
- SETUP EXAMPLE initializes the rule settings according to the EXAMPLE slider
- AUTO-CONTINUE? automatically wraps to the top once it reaches the last row when the switch is on
- GO begins running the model with the currently set rule. It runs until it reaches the bottom of the world.  If GO is pressed after it has completed, it will wrap to the top and continue.
- ON-COLOR & OFF-COLOR set the "on" and "off" cell colors respectively.

Rule Setup:
There are 8 sliders, the names of which correspond to cell states.  "O" means off, "I" means on. For example, the upper-right slider is called "IIO," corresponding to the state where a cell and its left neighbor is on, and its right neighbor is off.  (NOTE: the switch names are composed of the letters "I" and "O", not the numbers zero or one, because NetLogo switches can't have numbers for names.)  If this slider is set to 70%, then the following rule is created: when a cell is on, its left neighbor cell is on and its right neighbor cell is off, then there is a 70% chance the cell below it will be set "on" at the next time step, otherwise the cell below it will be set to "off" at the next time step.

Plot:
This plot measures two types of entropy, or disorder in a system.  Cellular automata can produce patterns with varying degrees of randomness.  If a pattern is perfectly random, each subsequence occurs with an equal probability, and the entropy is 1.  The more likely certain subsequences occur, the lower the entropy.  If a pattern is perfectly ordered, then the entropy is 0.  In this plot, 4-cell subsequences ("correlation length") are used to calculate the entropy. The first type of entropy is the spatial topologic entropy, which measures how many subsequences are present.  The second type of entropy, spatial metric entropy, measures the probability that all subsequences occur with the same frequency.

Plot Configuration:
- PLOT? switches plot on or off
- AUTO-CLEAR? if on, the plot is automatically cleared after each complete screen of cellular automata evolution

## THINGS TO NOTICE

Why is it a better idea to have a density that isn't too big or too small?

What is the relationship between the cellular automata display and the entropy plot?

How does the size of the black triangles affect the entropy?

What kinds of configurations lead to long lived chaotic behavior?

## THINGS TO TRY

You may want to set AUTO-CONTINUE? to 'on' in order to study the long-term behavior of each cellular automaton configuration.  Also, if you have a fast enough computer, you may want to increase the size of the world in order to get a better view of the "big picture."  If you turn the plot off, it will also increase the speed of your model.

Change the example slider to 1, and click SETUP EXAMPLE.  Click GO and experiment with the III slider, running each configuration a couple of times:
- What happens when III is set to 0%?
- - Why do you think the cellular automaton always ends up in the same uniform, or "absorbing" state?
- As you increase III, what happens to the density of the trees that are formed?
- - Does this seem to effect the time it takes to reach an absorbing state?
- What happens when III is set to 100%?
- - It seems very unlikely that this configuration will reach an absorbing state, but is it possible?
- - Why or why not?

Change the example slider to 2, and click SETUP EXAMPLE.  Click GO and experiment with the IOO slider, running each configuration a couple of times:
- Why does this configuration want to have either vertical or horizontal stripes?
- What happens when you change IOO to 0% or 100%?
- - Why does it always end up producing a majority of horizontal or vertical stripes?
- How does changing the OOI slider in conjunction with the IOO slider affect the model?

Change the example slider to 3, and click SETUP EXAMPLE.  Click GO and experiment with the IOO slider, running each configuration a couple of times:
- As you change IOO, what happens to the outer shape of the cellular automaton?
- - What happens to the shape of the triangles inside?
- - How is the value of the IOO slider related to the spread of on cells?
- - What should IOO be set to if you want perfectly symmetric triangles?
- Set IOO to 100%, and experiment with the OOI slider in a similar fashion.
- What is the relation between the IOO and OOI slider?

Change the example slider to 4, and click SETUP EXAMPLE. Click GO and experiment with the IOO slider, running each configuration a couple of times:
- As you increase IOO, what transition do you see in the structures formed by the cellular automata?
- - Why do you think this transition, or "phase change," happens?
- Once you get to 100%, notice that the triangles aren't very symmetric.
- - Which slider should you move accordingly in order to make the triangles look more symmetric?
- Now try moving both sliders together in order to find the point at which the cellular automaton makes its phase transition.

Change the example slider to 5, and click SETUP EXAMPLE Experiment with the OOI and IOO sliders together.
- What differences do you notice between the this example and the previous one?
- Does either cellular automaton have lower phase transition point with respect to the OOI and IOO sliders?
- - If so, what accounts for this difference?

## EXTENDING THE MODEL

Often times one might want to change multiple sliders in parallel, while leaving other sliders unchanged.  Try automating this process by creating additional switches and sliders.

Can you measure the entropy more accurately by using subsequences greater than 4?

There are many other ways to measure order in a system besides entropy, can you think of any?

Can you make a stochastic cellular automata with more neighbors?  For example, the cellular automata might have two neighbors on each side.

Try making a two-dimensional stochastic cellular automaton.  The neighborhood could be the eight cells around it, or just the cardinal cells (the cells to the right, left, above, and below).

## NETLOGO FEATURES

The plot-entropy procedure makes extensive use of the MAP and FILTER primitives.  First, MAP is used to convert a list containing the number of occurrences of each pattern of ON? values to a list of probabilities of each pattern occurring.  This is done by dividing each item in the first list by the total number of possible patterns.  Since both entropy calculations involve the use of a logarithm, FILTER is used to remove all elements in the list that are equal to 0 so that no errors occur.  For calculating the topological entropy, MAP is used to change all the remaining elements in the probability list to 1.  When calculating the metric entropy, MAP is used to multiply each element in the probability list by its logarithm.  Using MAP and FILTER allows these complex calculations to be done in a clean, compact manner.

## RELATED MODELS

CA 1D Elementary - a widely studied deterministic equivalent to this model
CA 1D Totalistic - a three color 1D cellular automata
Percolation (in Earth Science) - a model demonstrating the percolation of an oil spill using probabilistic rules similar to this model
Turbulence (in Chemistry & Physics) - a continuous cellular automata that exhibits phase change behavior similar to this model
Ising (in Chemistry & Physics) - a microscopic view of a magnetic field which undergoes phase changes with respect to temperature
DLA (in Chemistry & Physics) - a growth model demonstrating how the accumulation of randomly placed particles can lead to complex structures found throughout nature

## CREDITS AND REFERENCES

Chate, H. & Manneville, P. (1990). Criticality in cellular automata. Physica D (45), 122-135.
Li, W., Packard, N., & Langton, C. (1990). Transition Phenomena in Cellular Automata Rule Space. Physica D (45), 77-94.
Wolfram, S. (1983). Statistical Mechanics of Cellular Automata. Rev. Mod. Phys. (55), 601.
Wolfram, S. (2002). A New Kind of Science. Champaign, IL: Wolfram Media, Inc.

Thanks to Eytan Bakshy for his work on this model.
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
NetLogo 5.0RC2
@#$#@#$#@
set example 4
setup-example
repeat world-height - 1
  [ go ]
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

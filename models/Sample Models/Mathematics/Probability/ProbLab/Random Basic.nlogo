globals
[
  time-to-stop?   ;; boolean that discontinues run when columns reach to top
  the-messenger       ;; holds identity of the single turtle of breed 'messengers'
                  ;; (see EXTENDING THE MODEL)
  max-y-histogram ;; how high the columns can rise (or how far up the yellow goes)
]

breed [ column-counters column-counter ] ;; they keep track of their respective histogram columns
breed [ frames frame ]    ;; square frames that indicate events in histogram columns
breed [ messengers messenger  ]  ;; carry the random value to its column
                  ;; (currently just one single messenger implemented)


column-counters-own
[
  ;; if you choose a sample-space 7 then you get 7 column-counters
  ;; and their respective my-columns will be 1 thru 7
  my-column
  ;; each column-counter holds all patches that are in its column as an agentset
  my-column-patches
]

to setup
  clear-all
  ;; computes the height the user has requested so as to get the value that makes sense
  ;; in this model because the histogram grows from the negative-y values and not from 0
  set max-y-histogram (min-pycor + height)
  create-histogram-width
  setup-column-counters
  set time-to-stop? false
  reset-ticks
end

to create-histogram-width
  ask patches
  [
    ;; deals with both even and odd sample-spaces
    ;; this is one way of centering the histogram.
    ;; that means that the '50' of the red-green slider
    ;; will always be aligned with the middle of the histogram
    ifelse (pxcor >= (- sample-space) / 2) and (pxcor < sample-space / 2)
            and (pycor < max-y-histogram) ;; this shapes the top of the yellow zone
    [ set pcolor yellow ]
    [ set pcolor brown ]
  ]
end

    ;; column-counters are turtles who form "place-holders" so that
    ;; the messenger will "know" where to take its value.
    ;; they are like the values on the x-axis of your sample space.
to setup-column-counters
  ask patches with [(pycor = min-pycor) ;; bottom of the view
                       and pcolor = yellow]      ;; and in the histogram band width
  [
    sprout-column-counters 1
    [
      ht  ;; it is nice to see them but probably visually redundant
      set heading 0
      ;; this assigns a column name to column-counters that
      ;; corresponds with the parameter setting of sample-space
      set my-column floor (pxcor + sample-space / 2 + 1)
      set my-column-patches patches with [ pxcor = [pxcor] of myself ]
    ]
  ]

end

to go ;; forever button
  if time-to-stop? [ stop ]
  select-random-value
  send-messenger-to-its-column
  ifelse colors?
    [ paint ]
    [ ask patches with [pcolor != brown] [ set pcolor yellow ]]
  tick
end

    ;; 'messenger' is a turtle who carries the random value
    ;; on its back as a label
to select-random-value
  ask patch 0 (max-y-histogram + 4)
  [
    sprout-messengers 1
    [
      set shape "default"
      set color black
      set heading 180
      set size 12
      set label 1 + random sample-space
      ;; currently there is just one messenger, so we assign it to a 'messenger'
      ;; variable. this will save time when the model run. if the user chooses
      ;; to add more messengers then this shortcut may have to be done away with
      set the-messenger self
    ]
  ]
end

    ;; messenger is the dart-shaped large turtle that carries the random value
    ;; on its back. it takes this value directly to the appropriate column
to send-messenger-to-its-column
                ;; 'it' holds the column-counter who is master of the
                ;; column towards which the messenger orients and advances
                ;; to dispatch its event
  let it one-of column-counters with [ my-column = [label] of the-messenger ]

  ask the-messenger
  [
    face it
    ;; keep advancing until you're all but covering your destination
    while [ distance it > 3 ]
    [
      fd 1 ;; to the patch above you to prepare for next event
      display
    ]
    die
  ]
  ask it
  [ create-frame
    fd 1
    ;; if the histogram has become too high, we just stop.
    ;; this could be extended so as to have the whole population
    ;; of events collapse down one patch, as in Galton Box
    if ycor = max-y-histogram [ set time-to-stop? true ]
  ]

end

;; make the square frames that look like accumulating cubes
to create-frame ;; turtle procedure
  ask patch-here
  [
    sprout-frames 1
    [
      set shape "frame"
      set color black
    ]
  ]
end

    ;; patches are red if they are as far to the right within the sample-space
    ;; as indexed by the red-green slider; otherwise, the are green
    ;; Note that currently there is no rounding -- just a cut-off contour.
to paint
  ask column-counters
  [
    ifelse my-column <= (red-green * sample-space / 100)
    [ ask my-column-patches with [ pycor < [pycor] of myself ] [ set pcolor red ] ]
    [ ask my-column-patches with [ pycor < [pycor] of myself ] [ set pcolor green ] ]
  ]
end

;; reports the percentage of red patches out of all patches that have frames
;; so we know what percent of events are to the left of the cut off line
to-report %-red
  report precision (100 * count patches with [pcolor = red] / count frames) 2
end

to-report %-full
  report precision ( 100 * (count frames ) / ( height * sample-space ) ) 2

end

;; biggest-gap is the greatest difference in height between all columns
to-report biggest-gap
  let max-column max [count my-column-patches with [pycor < [pycor] of myself] ] of column-counters

  let min-column min [count my-column-patches with [pycor < [pycor] of myself] ] of column-counters

  report max-column - min-column
end
@#$#@#$#@
GRAPHICS-WINDOW
178
10
895
468
50
30
7.0
1
10
1
1
1
0
0
0
1
-50
50
-30
30
1
1
1
ticks
30.0

SLIDER
173
469
890
502
red-green
red-green
0
100
50
1
1
%
HORIZONTAL

BUTTON
12
109
75
142
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

BUTTON
78
109
141
142
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
33
266
123
299
colors?
colors?
0
1
-1000

MONITOR
32
212
124
257
%-red
%-red
3
1
11

SLIDER
1
36
173
69
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
32
150
124
195
biggest gap
biggest-gap
3
1
11

SLIDER
1
70
173
103
height
height
1
50
30
1
1
NIL
HORIZONTAL

MONITOR
47
320
104
365
NIL
%-full
3
1
11

@#$#@#$#@
## WHAT IS IT?

Random Basic is the simplest of all the ProbLab models. It can either be used first or as a detour from a more complex model to explain randomness.  This model introduces the user to the random generator in NetLogo. Randomness means that in the short term you cannot guess a value but in the long term all possible values will have occurred more or less equally often.

Here we see a bar chart grow reflecting a real-time succession of random events. You can think of the bar chart as a competition between the columns and ask whether one of the columns wins more often than others. Or you can think of it as some goodies being given out and think about whether the goodies are being given out fairly. Or think about it some other way -- that's fine.

You can set the size of the sample space. Also, you can split the bar chart into two sub-regions (red and green) and track the convergence of the distribution on proportion values that you have set.

This model is a part of the ProbLab curriculum. The ProbLab Curriculum is currently under development at the CCL. For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

At every tick, the computer selects a random number between 1 and "sample-space" (a slider in the interface tab). For instance, if the slider reads 100, then the random number will be between 1 and 100, inclusive. This random number piggy-backs on a virtual dart. In this program the dart is called the 'messenger' because it carries the number from the top of the view to the appropriate column. Each column is associated with one number. Once the messenger gets to its column, the messenger vanishes but the column becomes taller by one square (the squares are called 'frames' in this and other models in ProbLab). The columns grow until one of them reaches the brown half of the view, which stops the run.

## HOW TO USE IT

Choose a sample-space (you can leave it at the default value of '100') and press Setup. Now press Go. The messenger (the dart) will obtain a random number and carry it to the correct column. The red-green slider changes which columns are red and which are green. If it's set at 50% and if the sample space is 100 then patches up to 50 will be red and the rest will be green. If it's set at 50% and the sample space is 30 then patches up to 15 will be red. There are cases where this might be confusing: for instance, if it's set at 50% and if the sample space is 5 then patches up to 2 will be red. The '3'-column will be green because it goes further than 50% of 5.  That is, all columns up to and including the 3-column are more than 50% of the columns in the sample-space of 5 -- they are actually 60% because each of the 5 columns makes up 20% (and 5 * 20% is 100%). (See the section EXTENDING THE MODEL, below.)

These are the widgets in order of appearance from top to bottom:  
SAMPLE-SPACE is a slider for setting how many columns you want to be filling up. Also, it sets the range of numbers that will be randomly selected. If you set the slider to 3 then you will have 3 columns and the values will always be either 1, 2, or 3.  
SETUP - prepares the model for running with your sample-space parameter setting.  
GO - runs the model with the current settings.  
BIGGEST GAP - shows the biggest vertical difference between all columns. For instance, if the highest column is 10 squares high and the lowest is at 3 squares high then this monitor will show '7'.  
%-RED - shows what percentage of the squares are red out of all the squares.  
COLORS? - when this switch is on the patches get painted either red or green depending on the red-green slider value and their position.  
RED-GREEN - sets the cut-off line for which patches are painted red and which green. When your sample-space is 100 then the position of the little handle on the slider (what you grab and move) is exactly at the cut-off line. For sample spaces other than 100 it will be under the cut-off line only when it is set at 50. This is because currently this slider works according to percentage of events and not column value.  
%-FULL - the proportion of the yellow area that has been filled up by squares.

## THINGS TO NOTICE

What happens to the biggest gap as the model runs. Does it change? Does it get consistently bigger? Smaller? Is this connected to the size of the sample space or not? Come up with an explanation for this. Also, if you keep running the model with the same sample space until it stops, are you getting the same biggest gap each time? Is it "kind of the same"?

## THINGS TO TRY

How does the size of the Sample-Space affect your sense of "fairness?" Is it more "fair" when the sample-space is small (narrow) or when it is big (wide)? Try changing the sample-space slider and see if you feel that the events are being equally distributed across the bar chart. You can set the red-green slider at 50%, then at other values, and, looking at the %-red monitor, evaluate how long it takes for the red-green and the %-red values to be more or less the same. Perhaps a good way to go about this is by using a sample-space of size 2. This is much like flipping a coin. Now set it to a sample-space of 6. This is kind of like rolling a die.

Actually, what is fairness? Is the difference between two columns of heights 0 and 3 the same as the difference between two columns of heights 20 and 23?

How does the passage of time -- more and more events -- affect how close the red-green slider and the %-red are?

## EXTENDING THE MODEL

Currently, the red-green slider shows you how many patches there are that are smaller than a cut-off value. But you may want to know, for instance, how often the value '1' appeared and compare it to how many times the value '2' appeared. You could just count, but you may want to compare the ratio between these accumulations and see what happens to it as the program runs.

Another idea is to keep track of how many times, on average, a certain value, for instance '1' occurs out of every 10 trials. Partitioning the events into groups of 10 is called "sampling."

Keep track of how long it takes, on average, to get a certain value (for instance, '1'). That's called "waiting time".

Even another thing to check is how often you get a "double," that is, how often you get the same value back to back. How would the sample size affect that?

Build plots of your extensions to the model.

Implement a monitor that shows the smallest gap. There is currently a monitor which shows the biggest gap.

The red-green slider could work differently. For instance, instead of indexing the percentage, it could index the column number. If the red-green were set to 7 then it could index all the columns from 1 through 7.

What would happen if you added more messengers? Perhaps you could sprout them at different locations and have them execute the same code. Would this change the way the type of experimental outcomes?

Add a slider that allows you to choose specific columns and find out how many squares have accumulated in it.

Perhaps you noticed that each time you setup and run the model (each experiment) the random numbers come in a different order. But you may want to explore the same set of random numbers in several experiments where you modify some parameters. To do this, you should try working with the NetLogo primitive "random-seed" (see the NetLogo User Manual for details). If you keep sample-space constant, you should get the same numbers in the same order.

## NETLOGO FEATURES

We use the `random` primitive a great deal in NetLogo and especially in ProbLab, where we care about probability. But how does NetLogo produce random numbers? Does it roll a die or flip a coin? Actually, NetLogo uses the "Mersenne Twister" random generator. This generator is sometimes called a “pseudo-random” number generator because it uses a certain mathematical algorithm to produce numbers that appear random, but are actually predictable given enough time to figure out the pattern.

## RELATED MODELS

All of the ProbLab models use randomness. Having worked with this model, you may now have a better sense of where these random numbers are coming from and what to expect when the `random` primitive is used. In particular, note how other ProbLab models use randomness to control the movement of agents.

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

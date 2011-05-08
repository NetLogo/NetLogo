turtles-own [start-patch] ;; original position of ball on kick line
patches-own [score        ;; score for this position along the kick line
             left-angle   ;; angle towards left goal-post
             right-angle  ;; angle towards right goal-post
             goal-angle   ;; size of arc between left-angle and right-angle
             slope]       ;; slope of line from this patch towards a goal-post
globals [current-max      ;; the best patch-score so far
         col              ;; current color for our level-curves
         ang              ;; viewing angle of the current level curve
         best-dist        ;; distance from try-line of best kick
         analytic         ;; what the best distance should be, analytically
         try-line         ;; agentset containing only those patches on the try line
         histogram-area   ;; agentset containing only patches inside the histogram
         kicks            ;; total number of balls kicked
         goals]           ;; total number of goals scored

;; the origin of this model is set in the bottom left corner so
;; the y distance to the goal corresponds to the y-coordinate of the patch
;; this makes many of the calculations simpler.

to setup
  clear-all
  setup-field
  setup-balls
  set current-max 0
  set best-dist -1
  set kicks 0
  ask try-line [ set score 0 ]
  find-analytic-solution
  if show-level-curves? [ draw-level-curves ]
  reset-ticks
end

to setup-field
  ;; Draw lines for border, kick line, and goal line
  ask patches [
    if count neighbors != 8
     [set pcolor red ]
    if (pycor = min-pycor) and
       (pxcor >= goal-pos) and
       (pxcor < (goal-pos + goal-size))
     [set pcolor green]
  ]
  set try-line patches with [ pxcor = kick-line and pcolor = black ]
  ask try-line [ set pcolor yellow ]
  set histogram-area patches with [ pxcor < kick-line and pcolor = black ]
end

;; turtle procedure that resets all balls to kick line at end of each round
to setup-balls
  set-default-shape turtles "circle"
  ask try-line
    [ sprout 1
        [ set color orange
          set start-patch patch-here
          set heading (random-float 90) + 90 ] ]
  plot-scores
end

to go
  while [any? turtles] [
    ask turtles [ move ]
    display
  ]
  set kicks kicks + count try-line
  set goals sum [score] of try-line
  setup-balls
  tick
end

;; turtle procedure that moves all balls
to move
  ;; for speed, only check success/failure once we're near the
  ;; edge of the playing field
  if pxcor >= max-pxcor - 1 or pycor >= min-pycor + 1
     [ ;; in this model we approximate continuous motion by making
       ;; the turtles jump forward a step at a time.  but this can
       ;; throw the results off a little because sometimes a ball
       ;; will jump over the corner of a patch.  so to get correct
       ;; results, we need to check two patches.  "next-patch" is
       ;; the patch we would hit if we actually moved continuously.
       ;; "patch-ahead 1" is the patch we're going to land on when
       ;; we make our discrete jump.
       check-patch next-patch
       check-patch patch-ahead 1 ]
  fd 1
end

to check-patch [the-patch]  ;; turtle procedure
  if ([pcolor] of the-patch = red)
    [ die ]       ;; the ball has hit the border wall
  if ([pcolor] of the-patch = green) ;; the ball has reached the goal
    [ ;; increment the number of times a goal has been scored from this point on the kick line
      ask start-patch
        [ set score score + 1 ]
      die ]
end

;; see Next Patch Example, in the Code Examples section of
;; the Models Library, for a discussion of this code.
to-report next-patch  ;; turtle procedure
  if heading < towardsxy (pxcor + 0.5) (pycor + 0.5)
    [ report patch-at 0 1 ]
  if heading < towardsxy (pxcor + 0.5) (pycor - 0.5)
    [ report patch-at 1 0 ]
  if heading < towardsxy (pxcor - 0.5) (pycor - 0.5)
    [ report patch-at 0 -1 ]
  if heading < towardsxy (pxcor - 0.5) (pycor + 0.5)
    [ report patch-at -1 0 ]
  report patch-at 0 1
end

;; do histogramming in the view
to plot-scores
  ;; set the maximum goals scored from any patch
  set current-max (max [score] of try-line)
  if current-max = 0
  [
    ask histogram-area [ set pcolor black ]
    stop  ; otherwise we'll get division-by-zero errors below
  ]
  ask try-line [
    ifelse score = current-max
      [ set best-dist pycor
        ask patch-at 2 0 [ set plabel pycor ] ]
      [ if pcolor != magenta
        [ ask patch-at 2 0 [ set plabel "" ] ] ]
  ]
  ask histogram-area
  [;; make the histogram bar
    ifelse  pxcor > (kick-line - (([score] of patch-at (kick-line - pxcor) 0) * (kick-line - min-pxcor) / current-max))
    ;; make the yellow histogram bars at the maximal locations
    [ifelse ([score] of patch-at (kick-line - pxcor) 0 = current-max)
      [set pcolor yellow]
      ;; other locations get blue bars
      [set pcolor blue] ]
    [set pcolor black]
  ]
end

to find-analytic-solution
  ask patches with [pycor > min-pycor]
    [ calc-goal-angle ]
  ;; calculate the analytic solution for best kicking point
  let winning-patch min-one-of try-line [goal-angle]
  ask winning-patch
  [ set pcolor magenta
    ask patch-at 2 0 [ set plabel pycor ] ]
  set analytic [ pycor ] of winning-patch
end

to draw-level-curves
  ask patches with [(pxcor > kick-line) and (pcolor < 10) ]
    [ if goal-angle > 270
      [ set pcolor (360 - goal-angle mod 10) * 0.8 ] ]
end

;; calculate angle between patch and goal
to calc-goal-angle
  set left-angle  towardsxy (goal-pos - 0.5)
                            (min-pycor + 0.5)
  set right-angle towardsxy (goal-pos + goal-size - 0.5)
                            (min-pycor + 0.5)
  set goal-angle (right-angle - left-angle) mod 360
end
@#$#@#$#@
GRAPHICS-WINDOW
317
10
693
527
-1
-1
6.0
1
10
1
1
1
0
0
0
1
0
60
0
80
1
1
1
ticks

BUTTON
83
192
148
225
setup
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
18
84
155
117
kick-line
kick-line
1
59
20
1
1
NIL
HORIZONTAL

SLIDER
82
121
219
154
goal-size
goal-size
1
22
11
1
1
NIL
HORIZONTAL

SLIDER
159
84
296
117
goal-pos
goal-pos
1
49
40
1
1
NIL
HORIZONTAL

BUTTON
160
192
226
225
go
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
132
258
301
303
best distance (experimental)
best-dist
2
1
11

MONITOR
132
308
301
353
best distance (analytic)
analytic
2
1
11

SWITCH
63
47
239
80
show-level-curves?
show-level-curves?
1
1
-1000

MONITOR
15
258
116
303
NIL
kicks
0
1
11

MONITOR
15
308
116
353
NIL
goals
0
1
11

@#$#@#$#@
## WHAT IS IT?

"In rugby, after a try has been scored, the scoring team has the opportunity to gain further points by 'kicking a conversion'. The kick can be taken from anywhere on an imaginary line that is perpendicular to the try line (aka the goal line) and goes through the location on the try line where the try was scored.  Where should the kick be taken from to maximize the chance of a score?"

## HOW IT WORKS

Instead of trying to solve this problem with calculus or geometry, we'll take a probabilistic approach and use NetLogo turtles and patches. Essentially, we'll have a player stand on each patch along the imaginary kick line (the vertical yellow line), and kick many balls in random directions.  Players receive one 'point' for each conversion they score.  The idea is that since each kick-angle is randomly chosen, those players who score the most points must be standing in the best positions.

Note that this approach is quite different from the analytic approach. In the standard mathematical solution, one is expected to equate the best chance of scoring with the maximal angle from the kick line to the goal. In the probabilistic solution, this is not assumed. It emerges as the result if the playing field is assumed to be uniform. If, however, the model is extended to include wind or grass grain or decaying kick speeds, this solution adjusts gracefully in contrast to the brittle analytic solution.

## HOW TO USE IT

We use three sliders to initialize the model:
- GOAL-SIZE determines the size of the goal.
- GOAL-POS is the x-coordinate of the left goal-post. It is placed GOAL-POS units away from the leftmost edge of the world.
- KICK-LINE determines the x-coordinate of the kick-line. The kick line is KICK-LINE units away from the leftmost edge of the world.

The left goal-post is at `(goal-pos - 0.5, min-pycor - 0.5)`, and the right goal-post is at `(goal-pos + goal-size - 0.5, min-pycor - 0.5)`. (0.5 is the horizontal or vertical distance between the center of the patch and its edge; this is needed because the goal posts are effectively in the corners of their patches.)

Note that the values of KICK-LINE, GOAL-SIZE, and GOAL-POS may need to be adjusted to fit your current world-width.

Press the SETUP button to initialize the model, after you've chosen appropriate values for the sliders. You'll see one rugby ball on each patch on the try line. When you're ready to star, press the GO forever-button. This will commence the kicking; rounds will be repeated and results accumulated until you stop the GO button. Two monitors show the distance of the winning player, and the distance the analytic solution says should be the winner. Two other monitors display how many balls have been kicked and how many resulted in goals.

Watch the left-hand side of the playing field as the model runs. A histogram is being plotted of total points scored from each position along the kick line. The best scores (to date) for each round are highlighted in yellow (and their distance fromm the goal-line is displayed just to their right)-- the others are drawn in blue.The patch that is theoretically calculated to have the best score is shown in magenta and its distance is also displayed alongside it to the right.

In addition to the sequence of rounds described above, if you turn on the SHOW-LEVEL-CURVES? slider and press SETUP, you will see the level-curves associated with the given set of slider settings. This allows you to visualize the field of solutions for the analytic case of a uniform field. Patches are colored according to how large the goal looks from that position. Along each connected curve of the same color, the goal appears to be the same size. From straight ahead, the goal appears maximally wide.  From a shallow angle, the goal looks smaller at the same distance along the try line.

## THINGS TO NOTICE

Given a particular goal-line and kick-line, what are the best positions to kick from? Change the position and size of the goal. How do the 'best positions' change in relation to the goal's position? Then change the position of the kick line, and redo the trial. How does the performance compare when the kick line is farther away? Or closer?

Examine the histogram at the left side of the world. What do you notice about it? What shape does it have? What does it tell you about the best locations to kick from?

How does the experimental solution compare to the analytic solution? Given enough time, will the experimental solution converge to the analytic solution? If so, how long does it take?

## THINGS TO TRY

Can you generalize the results given by the model? What are the relationships between the kick-line, the goal, and the best patch to kick from?

Re-read the problem description given in the first paragraph of "What Is It?" Do you think that this model adequately answers the question asked? Why or why not?  What alternative solutions could you provide?

It's important to understand that this NetLogo project isn't exactly the standard method for solving problems of this sort. (A more 'classical' approach would be to use techniques from geometry or calculus.) However, there are many advantages offered by the NetLogo method. Consider the following other variables that might affect the solution to the problem: wind speed, grass height, or the size/weight/skill of the player kicking. (Such a problem that considers 'real-world' parameters such as these may quickly become intractable under classical mathematics.) Try and think of how you'd solve the 'rugby' problem in its idealized version without using NetLogo. Then try and solve it, taking into account one or more of these extra features.

In general, what advantages does the NetLogo solution have over the other methods? What disadvantages does it have?

## EXTENDING THE MODEL

Implement some of the features descibed above under "Things To Try"- e.g. wind speed, or the size of the player to kick the conversion from a particular position.

There are a variety of plots you could have 'rugby' draw in a plot. Implement plotting procedures for some or all of the following: the number of successful kicks compared to the overall kicks, the plot of both types of kicks over time, or the difference in histograms depending on the locations of the kick-line (i.e. the value of KICK-LINE) and the goal-posts (GOAL-POS and GOAL-SIZE).

## NETLOGO FEATURES

We draw our histogram straight to the view, unlike most other NetLogo models where the histogram is drawn in a separate plot.

In order to make the actual patch coordinate system more consistent with the conceptual coordinate system (the distances used in the model). The origin is at the bottom left corner of the world, so all coordinates are positive.

## CREDITS AND REFERENCES

The problem is taken from a British mathematics textbook.

For a fuller discussion, see Wilensky, U. (1996). Modeling Rugby: Kick First, Generalize Later? International Journal of Computers for Mathematical Learning. Vol. 1,  No. 1. p. 124 - 131.   http://ccl.northwestern.edu/cm/papers/rugby/
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
setup
repeat 100 [ go ]
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

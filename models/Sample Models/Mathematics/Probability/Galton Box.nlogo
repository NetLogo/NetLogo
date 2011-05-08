globals [
  balls        ;; how many balls have been created
  counters     ;; agentset of patches where ball counts are displayed
]

;; We use two breeds of turtle: one for balls which are still falling,
;; and second for balls which have landed in piles.
breed [falling-balls falling-ball]
breed [piled-balls piled-ball]

;;; SETUP PROCEDURES

to setup
  clear-all
  set-default-shape turtles "circle"
  set balls 0
  ask patch 0 (max-pycor - 2) [
    sprout 1 [ propagate ]
  ]
  set counters patches with [counter?]
  ask counters [
    set plabel 0
    set pcolor green - 3
    ;; make the column numbers at the bottom
    ask patch pxcor min-pycor [
      set plabel round (abs pxcor / 2)
    ]
  ]
  reset-ticks
end

;; The way we make the pegs is a bit tricky.  We use turtles,
;; starting with one turtle at the top.  Each turtle colors
;; the patch yellow, then creates two more turtles, one below
;; and left, one below and right.  The parent dies, and the
;; the cycle continues until the last row has been made.
;; This procedure is recursive.
to propagate  ;; peg procedure
  if ycor < max-pycor - 2 - 2 * number-of-rows [ die ]
  set pcolor yellow
  set ycor ycor - 2
  hatch 1 [
    set xcor xcor - 1
    propagate
  ]
  hatch 1 [
    set xcor xcor + 1
    propagate
  ]
  die
end

to-report counter?  ;; patch procedure
  report (yellow-at? 1 2 or yellow-at? -1 2)
    and not yellow-at? -2 0
    and not yellow-at? 2 0
end

to-report yellow-at? [x-offset y-offset] ;; patch procedure
  let p patch-at x-offset y-offset
  report p != nobody and [pcolor] of p = yellow
end

;;; GO PROCEDURES

to go
  if time-for-new-ball? [ new-ball ]
  if full? [
    ask falling-balls with [ycor > [pycor] of one-of counters] [
      set balls balls - 1
      die
    ]
  ]
  if not any? falling-balls [ stop ]
  ask falling-balls [ fall ]
  ask falling-balls-on counters [
    set plabel plabel + 1
    if not pile-up? [ die ]
  ]
  tick
end

to fall  ;; falling-balls procedure
  ifelse [pcolor] of patch-at 0 -1 = yellow
  ;; if peg below, go left or right
  [ ifelse random-float 100 < chance-of-bouncing-right
      [ set xcor xcor + 1 ]
      [ set xcor xcor - 1 ]
  ]
  ;; if no peg below, go down
  [ set ycor ycor - 1
    if done-falling? [
      set breed piled-balls
    ]
  ]
end

to-report done-falling?  ;; falling-ball procedure
  report ycor = (min-pycor + 1)
         or any? piled-balls-on patch-at 0 -1
end

to new-ball
  ask patch 0 max-pycor [
    sprout-falling-balls 1 [
      set color red
    ]
  ]
  set balls balls + 1
end

to-report time-for-new-ball?
  ;; we release a ball every other tick; keeping space
  ;; between the balls makes it easier to ensure that two
  ;; balls never simultaneously occupy the same patch
  report balls < number-of-balls
         and not any? falling-balls-on patch 0 (max-pycor - 1)
end

to-report full?
  report any? counters with [any? piled-balls-on patch-at 0 -1]
end
@#$#@#$#@
GRAPHICS-WINDOW
231
10
556
590
17
30
9.0
1
9
1
1
1
0
0
0
1
-17
17
-30
30
1
1
1
ticks
30

SLIDER
14
37
210
70
number-of-rows
number-of-rows
1
15
11
1
1
NIL
HORIZONTAL

BUTTON
29
124
106
157
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
14
80
210
113
number-of-balls
number-of-balls
1
1000
75
1
1
NIL
HORIZONTAL

SLIDER
4
167
226
200
chance-of-bouncing-right
chance-of-bouncing-right
0
100
50
1
1
%
HORIZONTAL

BUTTON
118
124
194
157
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

SWITCH
58
210
163
243
pile-up?
pile-up?
0
1
-1000

MONITOR
70
253
148
298
NIL
balls
3
1
11

@#$#@#$#@
## WHAT IS IT?

A Galton box is a triangular board that contains several rows of staggered but equally spaced pegs.  Balls are dropped from the top, bounce off the pegs and stack up at the bottom of the triangle.  The resulting stacks of balls have a characteristic shape.

The model enables you to observe how nature produces the binomial coefficients from Pascal's Triangle and their relation to a Gaussian bell-shaped normal curve. The model can also simulate coin tossing experiments with biased coins which result in skewed distributions

There are many applications for the concepts encompassed in a Galton box.  People employed in a wide variety of fields use binomial and normal distributions to make precise calculations about the likelihood of events or groups of events occurring.

## HOW IT WORKS

With the default settings, the model reproduces a traditional Galton box.  But you can also adjust the probability of the balls bouncing right or left when it hits a peg to be something other than 50-50.

## HOW TO USE IT

Click the SETUP to set up the rows of the triangle, the number of balls, and other parameters.  Click to GO button to begin the simulation.

The PILE-UP? button controls if the balls create piles or simply disappear when they reach the bottom of the triangle.  If PILE-UP? is on and the pile of balls reaches the bottom of the triangle, the model will stop.  Note: if you are running a trial with a large number of balls you might want to turn PILE-UP? off.

## THINGS TO NOTICE

With a small number of balls it is hard to notice any consistent patterns in the results.

As you increase the number of balls, clear patterns and distributions start to form.  By adjusting the CHANCE-OF-BOUNCING-RIGHT you can see how different factors can change the distribution of balls.  What types of distributions form when the CHANCE-OF-BOUNCING-RIGHT is set at 20, 50, or 100?

This model is a good example of an independent trials process.  Each ball has a probability of falling one way, and its decision is unrelated to that of any of the other balls.  The number of rows the balls must fall through affects the amount of variation present in a run of the model.

## THINGS TO TRY

Change the NUMBER-OF-BALLS and NUMBER-OF-ROWS sliders.  How does varying numbers alter how balls stack up?

Change the CHANCE-OF-BOUNCING-RIGHT slider as balls have begun to fall.  What kinds of ball distributions can you produce?

Change the NUMBER-OF-BALLS slider.  What is the best way to produce a standard binomial distribution (or approximate a bell curve)?

Set a CHANCE-OF-BOUNCING-RIGHT then try to predict the resulting stacks of balls.  How would you calculate the mean and variances of a given stack for a given setting?

## EXTENDING THE MODEL

Make the balls shade the patches as they fall, so the more balls pass a patch the lighter it gets.  This will let the user how frequently different paths are traveled.

Modify the program to allow independent adjustment of each peg, so that they can adjust their own orientation, rather than having all the pegs synchronized.

Change the shape of the board.  Maybe flip the triangle upside down.  How does this effect how the balls get distributed?

In addition to changing the shape of the board, change the direction balls can go.  Maybe allow balls to go in all directions.

Make it so you can select a specific peg.  If a ball bounces off that peg, stop the ball.  Keep track of how many balls are stopped.  What specific insight does this provide about the independent trials process and ball distributions?.

## RELATED MODELS

 * Random Walk Left Right
 * Binomial Rabbits

## CREDITS AND REFERENCES
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

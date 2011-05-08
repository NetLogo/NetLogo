globals
[
  max-grain    ; maximum amount any patch can hold
  result
]

patches-own
[
  grain-here      ; the current amount of grain on this patch
  max-grain-here  ; the maximum amount of grain this patch can hold
]

turtles-own
[
  age              ; how old a turtle is
  wealth           ; the amount of grain a turtle has
  life-expectancy  ; maximum age that a turtle can reach
  metabolism       ; how much grain a turtle eats each time
  vision           ; how many patches ahead a turtle can see
]

to benchmark
  random-seed 48281
  reset-timer
  setup
  repeat 2400 [ go ]
  set result timer
end

;;;
;;; SETUP AND HELPERS
;;;

to setup
  ca reset-ticks
  ;; set global variables to appropriate values
  set max-grain 50
  ;; call other procedures to set up various parts of the world
  setup-patches
  setup-turtles
  setup-plotz
  ;; plot the initial state of the world
  update-plotz
end

;; set up the initial amounts of grain each patch has
to setup-patches
  ;; give some patches the highest amount of grain possible --
  ;; these patches are the "best land"
  ask patches
    [ set max-grain-here 0
      if (random-float 100.0) <= percent-best-land
        [ set max-grain-here max-grain
          set grain-here max-grain-here ] ]
  ;; spread that grain around the window a little and put a little back
  ;; into the patches that are the "best land" found above
  repeat 5
    [ ask patches with [max-grain-here != 0]
        [ set grain-here max-grain-here ]
      diffuse grain-here 0.25 ]
  repeat 10
    [ diffuse grain-here 0.25 ]          ;; spread the grain around some more
  ask patches
    [ set grain-here floor grain-here    ;; round grain levels to whole numbers
      set max-grain-here grain-here      ;; initial grain level is also maximum
      recolor-patch ]
end

to recolor-patch  ;; patch procedure -- use color to indicate grain level
  set pcolor scale-color yellow grain-here 0 max-grain
end

;; set up the initial values for the turtle variables
to setup-turtles
  set-default-shape turtles "person"
  cro num-people
    [ setxy random-pxcor random-pycor  ;; put turtles on patch centers
      set size 1.5  ;; easier to see
      set-initial-turtle-vars
      set age random life-expectancy ]
  recolor-turtles
end

to set-initial-turtle-vars
  set age 0
  set heading 90 * random 4
  set life-expectancy life-expectancy-min +
                        random (life-expectancy-max - life-expectancy-min + 1)
  set metabolism 1 + random metabolism-max
  set wealth metabolism + random 50
  set vision 1 + random max-vision
end

;; Set the class of the turtles -- if a turtle has less than a third
;; the wealth of the richest turtle, color it red.  If between one
;; and two thirds, color it green.  If over two thirds, color it blue.
to recolor-turtles
  let max-wealth max [wealth] of turtles
  ask turtles
    [ ifelse (wealth <= max-wealth / 3)
        [ set color red ]
        [ ifelse (wealth <= (max-wealth * 2 / 3))
            [ set color green ]
            [ set color blue ] ] ]
end

;;;
;;; GO AND HELPERS
;;;

to go
  tick  ;; unorthodox to put this first, but that's how this model has always done it - ST 2/1/07
  ask turtles
    [ turn-towards-grain ]  ;; choose direction holding most grain within the turtle's vision
  ;; grow grain every grain-growth-interval clock ticks
  if ticks mod grain-growth-interval = 0
    [ ask patches
        [ grow-grain ] ]
  harvest
  ask turtles
    [ move-eat-age-die ]
  recolor-turtles
  update-plotz
end

;; determine the direction which is most profitable for each turtle in
;; the surrounding patches within the turtles' vision
to turn-towards-grain  ;; turtle procedure
  set heading 0
  let best-direction 0
  let best-amount grain-ahead
  set heading 90
  if (grain-ahead > best-amount)
    [ set best-direction 90
      set best-amount grain-ahead ]
  set heading 180
  if (grain-ahead > best-amount)
    [ set best-direction 180
      set best-amount grain-ahead ]
  set heading 270
  if (grain-ahead > best-amount)
    [ set best-direction 270
      set best-amount grain-ahead ]
  set heading best-direction
end

to-report grain-ahead  ;; turtle procedure
  let total 0
  let how-far 1
  repeat vision
    [ set total total + [grain-here] of patch-ahead how-far
      set how-far how-far + 1 ]
  report total
end

to grow-grain  ;; patch procedure
  ;; if a patch does not have it's maximum amount of grain, add
  ;; num-grain-grown to its grain amount
  if (grain-here < max-grain-here)
    [ set grain-here grain-here + num-grain-grown
      ;; if the new amount of grain on a patch is over its maximum
      ;; capacity, set it to its maximum
      if (grain-here > max-grain-here)
        [ set grain-here max-grain-here ]
      recolor-patch ]
end

;; each turtle harvests the grain on its patch.  if there are multiple
;; turtles on a patch, divide the grain evenly among the turtles
to harvest
  ; have turtles harvest before any turtle sets the patch to 0
  ask turtles
    [ set wealth floor (wealth + (grain-here / (count turtles-here))) ]
  ;; now that the grain has been harvested, have the turtles make the
  ;; patches which they are on have no grain
  ask turtles
    [ set grain-here 0
      recolor-patch ]
end

to move-eat-age-die  ;; turtle procedure
  fd 1
  ;; consume some grain according to metabolism
  set wealth (wealth - metabolism)
  ;; grow older
  set age (age + 1)
  ;; check for death conditions: if you have no grain or
  ;; you're older than the life expectancy or if some random factor
  ;; holds, then you "die" and are "reborn" (in fact, your variables
  ;; are just reset to new random values)
  if (wealth < 0) or (age >= life-expectancy)
    [ set-initial-turtle-vars ]
end

;;;
;;; PLOTTING
;;;

to setup-plotz
  set-current-plot "Class Plot"
  set-plot-y-range 0 num-people
  set-current-plot "Class Histogram"
  set-plot-y-range 0 num-people
end

to update-plotz
  update-class-plot
  update-class-histogram
  update-lorenz-and-gini-plots
end

;; this does a line plot of the number of people of each class
to update-class-plot
  set-current-plot "Class Plot"
  set-current-plot-pen "low"
  plot count turtles with [color = red]
  set-current-plot-pen "mid"
  plot count turtles with [color = green]
  set-current-plot-pen "up"
  plot count turtles with [color = blue]
end

;; this does a histogram of the number of people of each class
to update-class-histogram
  set-current-plot "Class Histogram"
  plot-pen-reset
  set-plot-pen-color red
  plot count turtles with [color = red]
  set-plot-pen-color green
  plot count turtles with [color = green]
  set-plot-pen-color blue
  plot count turtles with [color = blue]
end

to update-lorenz-and-gini-plots
  set-current-plot "Lorenz Curve"
  clear-plot

  ;; draw a straight line from lower left to upper right
  set-current-plot-pen "equal"
  plot 0
  plot 100

  set-current-plot-pen "lorenz"
  set-plot-pen-interval 100 / num-people
  plot 0

  let sorted-wealths sort [wealth] of turtles
  let total-wealth sum sorted-wealths
  let wealth-sum-so-far 0
  let index 0
  let gini-index-reserve 0

  ;; now actually plot the Lorenz curve -- along the way, we also
  ;; calculate the Gini index
  repeat num-people [
    set wealth-sum-so-far (wealth-sum-so-far + item index sorted-wealths)
    plot (wealth-sum-so-far / total-wealth) * 100
    set index (index + 1)
    set gini-index-reserve
      gini-index-reserve +
      (index / num-people) -
      (wealth-sum-so-far / total-wealth)
  ]

  ;; plot Gini Index
  set-current-plot "Gini-Index v. Time"
  plot (gini-index-reserve / num-people) / area-of-equality-triangle
end

to-report area-of-equality-triangle
  ;; not really necessary to compute this when num-people is large;
  ;; if num-people is large, could just use estimate of 0.5
  report (num-people * (num-people - 1) / 2) / (num-people ^ 2)
end
@#$#@#$#@
GRAPHICS-WINDOW
184
10
602
449
25
25
8.0
1
10
1
1
1
0
1
1
1
-25
25
-25
25
1
1
1
ticks
30

BUTTON
8
256
79
289
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

BUTTON
108
256
175
289
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

SLIDER
8
72
176
105
max-vision
max-vision
1
15
5
1
1
NIL
HORIZONTAL

SLIDER
8
301
176
334
grain-growth-interval
grain-growth-interval
1
10
1
1
1
NIL
HORIZONTAL

SLIDER
8
106
176
139
metabolism-max
metabolism-max
1
25
15
1
1
NIL
HORIZONTAL

SLIDER
8
38
176
71
num-people
num-people
2
1000
250
1
1
NIL
HORIZONTAL

SLIDER
8
208
176
241
percent-best-land
percent-best-land
5.0
25.0
10
1.0
1
%
HORIZONTAL

SLIDER
8
174
176
207
life-expectancy-max
life-expectancy-max
1
100
83
1
1
NIL
HORIZONTAL

PLOT
3
454
255
634
Class Plot
Time
Turtles
0.0
50.0
0.0
250.0
true
true
"" ""
PENS
"low" 1.0 0 -2674135 true "" ""
"mid" 1.0 0 -10899396 true "" ""
"up" 1.0 0 -13345367 true "" ""

SLIDER
8
335
176
368
num-grain-grown
num-grain-grown
1
10
4
1
1
NIL
HORIZONTAL

SLIDER
8
140
176
173
life-expectancy-min
life-expectancy-min
1
100
1
1
1
NIL
HORIZONTAL

PLOT
257
454
469
634
Class Histogram
Classes
Turtles
0.0
3.0
0.0
250.0
false
false
"" ""
PENS
"default" 1.0 1 -2674135 true "" ""

PLOT
471
454
672
634
Lorenz Curve
Pop %
Wealth %
0.0
100.0
0.0
100.0
false
true
"" ""
PENS
"lorenz" 1.0 0 -2674135 true "" ""
"equal" 100.0 0 -16777216 true "" ""

PLOT
674
454
908
634
Gini-Index v. Time
Time
Gini
0.0
50.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -13345367 true "" ""

BUTTON
604
92
773
235
NIL
benchmark
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

MONITOR
638
183
733
228
NIL
result
3
1
11

@#$#@#$#@
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
benchmark set result 0
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

globals [
  ;; This list contains the numbers of the switches that are on,
  ;; for example, if all six switches are on, the list will be
  ;; [1 2 3 4 5 6].  The list is only built during SETUP and
  ;; READ-SWITCHES.
  switches
  ;; This is a list of cells which are eligible to become alive.
  eligibles
]

;; About the use of lists in this model:
;;
;; The model could be coded more simply without the "switches" and
;; "eligibles" lists.  But using those lists enables the model to run
;; much faster.
;;
;; The "switches" list is used so a cell can quickly check its live
;; neighbors count against the six switches without having to actually
;; inspect the switches one by one.  If the user flips a switch,
;; the list will be out of date, which is why we ask the user to
;; press the SETUP or READ-SWITCHES buttons after changing switches.
;;
;; The "eligibles" list is used so that when we are trying to decide
;; what cell will become alive next, we don't have to check every
;; cell.  The list contains only those cells we know are eligible.
;; Every time a cell becomes alive, we remove it from the list.
;; We must also check that cell's neighbors to see if they need
;; to be added or removed from the list.

breed [cells cell]

cells-own [
  hex-neighbors
  live-neighbor-count
  eligible?
]

to setup
  clear-all
  setup-grid
  read-switches
  ;; start with one live cell in the middle
  ask cells-on patch 0 0 [ become-alive ]
  reset-ticks
end

to go
  if empty? eligibles [ stop ]
  ask one-of eligibles [ become-alive ]
  tick
end

to become-alive  ;; cell procedure
  show-turtle
  set eligible? false
  set eligibles remove self eligibles
  ask hex-neighbors [
    set live-neighbor-count live-neighbor-count + 1
    if live-neighbor-count = 6 [ set color red ]
    update-eligibility
  ]
end

to update-eligibility  ;; cell procedure
  ifelse eligible?
  ;; case 1: currently eligible
  [
    if not member? live-neighbor-count switches [
      set eligible? false
      set eligibles remove self eligibles
    ]
  ]
  ;; case 2: not currently eligible
  [
    ;; the check for hidden? ensures the cell isn't already alive
    if hidden? and member? live-neighbor-count switches [
      set eligible? true
      ;; The order of the list doesn't matter, but in NetLogo
      ;; (as in Logo and Lisp generally), FPUT is much much
      ;; faster than LPUT.
      set eligibles fput self eligibles
    ]
  ]
end

;;; only allow the new alive cells to have number of neighbors as allowed by the switches
to read-switches
  set switches []
  if one-neighbor?    [ set switches lput 1 switches ]
  if two-neighbors?   [ set switches lput 2 switches ]
  if three-neighbors? [ set switches lput 3 switches ]
  if four-neighbors?  [ set switches lput 4 switches ]
  if five-neighbors?  [ set switches lput 5 switches ]
  if six-neighbors?   [ set switches lput 6 switches ]
  ask cells [
    set eligible? hidden? and member? live-neighbor-count switches
  ]
  set eligibles [self] of cells with [eligible?]
end

;;; this was mostly taken from Hex Cells Example
to setup-grid
  set-default-shape turtles "hex"
  ask patches [
    sprout-cells 1 [
      hide-turtle
      set color orange
      set eligible? false
      if pxcor mod 2 = 0 [
        set ycor ycor - 0.5
      ]
    ]
  ]
  ask cells [
    ifelse pxcor mod 2 = 0 [
      set hex-neighbors cells-on patches at-points [[0  1] [ 1  0] [ 1 -1]
                                                    [0 -1] [-1 -1] [-1  0]]
    ][
      set hex-neighbors cells-on patches at-points [[0  1] [ 1  1] [ 1  0]
                                                    [0 -1] [-1  0] [-1  1]]
    ]
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
289
10
703
445
50
50
4.0
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
-50
50
1
1
1
ticks
30

BUTTON
8
37
172
70
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

BUTTON
179
71
279
104
NIL
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

BUTTON
179
37
279
70
NIL
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

SWITCH
9
72
172
105
one-neighbor?
one-neighbor?
0
1
-1000

SWITCH
9
106
172
139
two-neighbors?
two-neighbors?
0
1
-1000

SWITCH
9
140
172
173
three-neighbors?
three-neighbors?
0
1
-1000

SWITCH
9
174
172
207
four-neighbors?
four-neighbors?
0
1
-1000

SWITCH
9
208
172
241
five-neighbors?
five-neighbors?
0
1
-1000

SWITCH
10
242
172
275
six-neighbors?
six-neighbors?
0
1
-1000

BUTTON
10
278
172
311
NIL
read-switches
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
181
129
280
196
The switches are only considered during SETUP and READ-SWITCHES.
10
0.0
0

@#$#@#$#@
## WHAT IS IT?

This is a model of growth.  It takes place on a two-dimensional hexagonal grid of cells. Cells can either be alive or dead.  Various growth patterns result, depending on the exact rules governing growth.

## HOW IT WORKS

SETUP arranges cells in a hexagonal grid, as in Hex Cells Example.  The edges of the grid do not wrap.

Only the center cell is alive at the start.

The switches determine which dead cells are eligible to become alive.  For example, if the ONE-NEIGHBOR? switch is on, dead cells that have exactly one alive neighbor are eligible for growth, and so on for the rest of the switches.

Each tick, one eligible dead cell goes live.

Dead cells are invisible (black).  Live cells are shown in orange or red depending on whether they have any dead neighbors.

## HOW TO USE IT

SETUP places one alive cell in the middle of the grid.

GO advances the growth process.

X-NEIGHBORS? are switches that, when on, allow new cells to grow where they will have X alive neighbors. X ranges from one to six because the cells are on a hexagonal grid and so each cell has 6 neighbors.

READ-SWITCHES makes your settings for the switches take effect. (They don't take effect right away because the model is coded in a special way in order to run faster.)

## THINGS TO NOTICE

When some switches are turned off, "holes" appear in the pattern. Depending on which X-NEIGHBORS? switches are on and which are off, those holes can be different shapes. Some interesting configurations are {1, 2, 4} (ONE-NEIGHBOR?, TWO-NEIGHBORS?, AND FOUR-NEIGHBORS? on while all other switches are off), {1}, {1, 4, 5}, {1, 3, 5, 6}, and {1, 3, 4, 5, 6}.

Often, as the alive cells approach the border, the overall shape resembles a circle.

For different configurations of the X-NEIGHBORS? switches, the "Cell Types" plot shows very different numbers of alive, dead, and inner-edge cells when the model stops.

## THINGS TO TRY

Change the size of the world. If it's much bigger, the model might run too slowly. If it's smaller, can you get different patterns?

Switch off TWO-NEIGHBORS? for a run. Does the overall shape look any different? After the model has been running for a while, change the switches to not allow 1 or 2 neighbors while allowing for 3 and up. (Don't forget to press the CHANGE-SWITCHES button.) Watch it go. What happens if you then change it to allow only for 1 or 2?

## EXTENDING THE MODEL

Implement the model on a regular square grid using both `neighbors4` and `neighbors` instead of the `neighbors6` we used in this model. Figure out a way to measure how quickly the alive cells spread to the edge in different configurations.

To better see the near-circular shape of the aggregation as the growth gets near the edge, add a check that stops the model when a cell on the edge becomes alive.

Add a plot that tracks the ratio of orange to red cells.

Each tick one eligible dead cell goes live.  This one-at-a-time update rule differs from many cell-based models which update all the cells at once.  (This update rule is specified in the reference in the CREDITS AND REFERENCES section.)  Change the rules so that each tick, all of the eligible dead cells go live. What different result do you observe, if any?

## NETLOGO FEATURES

The code uses lists in order to make the model run faster.  The code would be considerably simpler if these lists weren't used, but it would also run much slower.  See the comments in the Code tab for details on the use of lists in this model.

## RELATED MODELS

Diffusion Limited Aggregation  
Life  
Hex Cells Example

## CREDITS AND REFERENCES

This model was inspired by Stephen Wolfram's A New Kind of Science. A very similar model is discussed here: http://www.wolframscience.com/nksonline/page-331?firstview=1. In the notes at the end of the book, many extensions are suggested, although none on a hexagonal grid.

Thanks to Josh Unterman and Seth Tisue for their work on this model.
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

hex
false
0
Polygon -7500403 true true 0 150 75 30 225 30 300 150 225 270 75 270

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
setup repeat 3800 [ go ]
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

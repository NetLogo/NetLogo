;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variable and Breed declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

globals [
  red-vertex          ;; the red vertex
]

breed [ vertices vertex ]

;; vertices keep track of who is next in line
;; because the area calculation requires it.
vertices-own [ next-vertex ]

;;
;; Setup Procedures
;;

;; Initializes the display, and creates a list that contains the names of the shapes
;; used by turtles in this activity.  Also initializes the data lists.
to setup
  clear-all
  set-default-shape vertices "circle"
  create-vertices number-vertices [
    setup-vertex-vars
  ]
  ;; one of the vertices is specially controlled by the
  ;; controls in the interface, designate it here.
  ask one-of vertices [
    set color red
    set shape "monster"
    set red-vertex self
  ]
  ;; make the polygon
  ask one-of vertices [ edgify ]
  ;; update the data in the display
  update
end

;; sets the turtle variables to appropriate initial values
to setup-vertex-vars  ;; vertex procedure
  set color brown
  setxy random-xcor random-ycor
  set heading 0
  set label-color white
  set next-vertex nobody
end

;; recursive procedure that links all the vertices together
;; one at a time.
to edgify
  ;; each vertex is linked to once and then, in turn links to
  ;; another vertex that has not yet been linked, when we
  ;; run out of vertices we've made a line and we just need
  ;; to close the polygon by linking back to the beginning
  let candidates other vertices with [ not any? link-neighbors ]
  ifelse any? candidates
  [
    set next-vertex one-of candidates
    create-link
    ask next-vertex [ edgify ]
  ]
  [
    set next-vertex one-of other vertices with [ count link-neighbors = 1 ]
    create-link
  ]
end

to create-link
  create-link-with next-vertex [
    set color white
    set label-color white
  ]
end

;;
;; Runtime Procedures
;;

to go
  if mouse-down? [
    ;; clicking "picks up" the closest vertex
    ask min-one-of vertices [ distancexy mouse-xcor mouse-ycor ] [
      ;; if the mouse is more than 1 patch away from any
      ;; vertex just ignore the click.
      if distancexy mouse-xcor mouse-ycor < 1
      [
        while [mouse-down?] [
          ;; use EVERY to limit how much data we end up plotting
          every 0.05 [
            ;; don't move vertices directly on top of one another
            if all? other vertices [ xcor != mouse-xcor or ycor != mouse-ycor ] [
              setxy mouse-xcor mouse-ycor
              update
            ]
          ]
        ]
      ]
    ]
  ]
end

to update
  ask links [
    set label less-precise link-length
  ]
  ask vertices [
    ;; make sure the angle is positive
    let my-neighbors sort link-neighbors
    let angle (subtract-headings towards first my-neighbors towards last my-neighbors) mod 360
    ;; make sure the angle is the interior angle
    if angle > 180 [ set angle 360 - angle ]
    set label less-precise angle
  ]
  display
  update-plots
end

;;; used to keep the labels from having too much cluttering detail
to-report less-precise [ precise-num ]
  report precision precise-num 1
end

to-report perimeter
  report sum [link-length] of links
end

;; this area calculation is based on the formula found here:
;; http://mathworld.wolfram.com/PolygonArea.html
to-report area
  let result 0
  ask vertices [
    let addend ((xcor * [ycor] of next-vertex) -
        (ycor * [xcor] of next-vertex))
    set result result + addend
  ]
  report abs (result / 2)
end


;; red-vertex commands

to move-fd
  ask red-vertex [
    ;; pre-check that no other vertices are on the point we're headed to
    let new-xcor (xcor + step-size * dx)
    let new-ycor (ycor + step-size * dy)
    if all? vertices [xcor != new-xcor or ycor != new-ycor] [
      fd step-size
    ]
  ]
  update
end

to move-bk
  ask red-vertex [
    ;; pre-check that no other vertices are on the point we're headed to
    let new-xcor (xcor + step-size * (- dx))
    let new-ycor (ycor + step-size * (- dy))
    if all? vertices [xcor != new-xcor or ycor != new-ycor] [
      bk step-size
    ]
  ]
  update
end

to turn-right
  ask red-vertex [
    rt turn-amount
  ]
end

to turn-left
  ask red-vertex [
    lt turn-amount
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
347
10
907
591
5
5
50.0
1
20
1
1
1
0
0
0
1
-5
5
-5
5
1
1
0
ticks
30

BUTTON
18
79
82
112
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
19
40
83
73
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

PLOT
14
414
337
574
PANDA
NIL
NIL
0.0
10.0
0.0
10.0
true
true
"" ""
PENS
"perimeter" 1.0 0 -2064490 true "" "plot perimeter"
"area" 1.0 0 -8630108 true "" "plot area"

MONITOR
203
363
269
408
perimeter
less-precise perimeter
3
1
11

MONITOR
271
363
337
408
area
less-precise area
3
1
11

SLIDER
93
40
288
73
number-vertices
number-vertices
3
10
3
1
1
NIL
HORIZONTAL

BUTTON
121
174
208
207
NIL
move-fd
NIL
1
T
OBSERVER
NIL
W
NIL
NIL
1

BUTTON
121
209
208
242
NIL
move-bk
NIL
1
T
OBSERVER
NIL
S
NIL
NIL
1

BUTTON
210
192
297
225
NIL
turn-right
NIL
1
T
OBSERVER
NIL
D
NIL
NIL
1

BUTTON
33
191
119
224
NIL
turn-left
NIL
1
T
OBSERVER
NIL
A
NIL
NIL
1

INPUTBOX
71
248
156
308
step-size
1
1
0
Number

INPUTBOX
158
248
243
308
turn-amount
90
1
0
Number

BUTTON
14
379
111
412
NIL
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
85
146
235
164
controls for the red vertex
11
0.0
0

TEXTBOX
93
84
307
113
click and drag the vertices with the mouse
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

This is a Solo version of the HubNet activity called Perimeters and Areas by Embodied Agent Reasoning, or PANDA BEAR. PANDA BEAR Solo can be used as a standalone activity or as an introduction to PANDA BEAR.

PANDA BEAR is a microworld for mathematics learning that lies at the intersection of dynamic geometry environments and participatory simulation activities. Whereas PANDA BEAR involves many people controlling individual vertices of a shared, group-polygon, in PANDA BEAR Solo, an individual user controls all of the vertices of a polygon. The measures of perimeter and area of the polygon are foregrounded in the environment. The model user can be given challenges regarding the polygon's perimeter and area as suggested in the THINGS TO TRY section.

## HOW TO USE IT

SETUP initializes the model to create a polygon containing NUMBER-VERTICES vertices. GO allows the user to move the vertices around with the mouse. The PERIMETER and AREA monitors update automatically as the vertices move around. The PANDA plot shows both of those measures over time as a record of the user's actions as they work towards a goal. SETUP-PLOT resets the plot to start a new challenge with the same polygon. The MOVE-FD, MOVE-BK, TURN-RIGHT, and TURN-RIGHT buttons change the red vertex's location and heading. The STEP-SIZE and TURN-AMOUNT input boxes control the amount of movement of the MOVE-FD, MOVE-BK, TURN-RIGHT, and TURN-RIGHT buttons.

## THINGS TO NOTICE

In a triangle, for an individual vertex, moving "between" the other two vertices minimizes the perimeter for a given area.

In a triangle, when all three vertices attempt to form an isosceles triangle, and equilateral triangle is formed.

Strategies that work for challenges at the triangle level often work at the square level as well.

As the number of vertices is increased, the polygon that maximizes the area given a perimeter and minimizes the perimeter given an area gets closer and closer to a circle.

## THINGS TO TRY

With three vertices, make the area as big as possible while keeping the perimeter at or below 25.

With three vertices, make the perimeter as small as possible while keeping the area at or above 25.

Increase the number of vertices in the polygon from three to four (and beyond - approaching a circle) and do the above.

Modify the challenges in a patterned way. For example, with four vertices, doubling the allowed perimeter should quadruple the maximum area.

## EXTENDING THE MODEL

Add different methods of movement. For example, instead of turning and going forward and backward, the user could be allowed to move the red vertex in the 4 cardinal directions.

Allow the user to give the vertices movement rules to follow over and over so that the group-polygon "dances".

## NETLOGO FEATURES

This model uses links to form the sides of the polygon, each vertex is linked to exactly two other vertices.  The sum of the lengths of all the links is the perimeter of the polygon.

The area calculation is based on information found here: http://mathworld.wolfram.com/PolygonArea.html

## RELATED MODELS

PANDA BEAR

## CREDITS AND REFERENCES

Thanks to Josh Unterman for his work on this model.
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

android
true
0
Polygon -7500403 true true 210 90 240 195 210 210 165 90
Circle -7500403 true true 110 3 80
Polygon -7500403 true true 105 88 120 193 105 240 105 298 135 300 150 210 165 300 195 298 195 240 180 193 195 88
Rectangle -7500403 true true 127 81 172 96
Rectangle -16777216 true false 135 33 165 60
Polygon -7500403 true true 90 90 60 195 90 210 135 90

box
false
0
Polygon -7500403 true true 150 285 285 225 285 75 150 135
Polygon -7500403 true true 150 135 15 75 150 15 285 75
Polygon -7500403 true true 15 75 15 225 150 285 150 135
Line -16777216 false 150 285 150 135
Line -16777216 false 150 135 15 75
Line -16777216 false 150 135 285 75

butterfly
true
0
Rectangle -7500403 true true 92 135 207 224
Circle -7500403 true true 158 53 134
Circle -7500403 true true 165 180 90
Circle -7500403 true true 45 180 90
Circle -7500403 true true 8 53 134
Line -16777216 false 43 189 253 189
Rectangle -7500403 true true 135 60 165 285
Circle -7500403 true true 165 15 30
Circle -7500403 true true 105 15 30
Line -7500403 true 120 30 135 60
Line -7500403 true 165 60 180 30
Line -16777216 false 135 60 135 285
Line -16777216 false 165 285 165 60

cactus
true
0
Rectangle -7500403 true true 135 30 175 177
Rectangle -7500403 true true 67 105 100 214
Rectangle -7500403 true true 217 89 251 167
Rectangle -7500403 true true 157 151 220 185
Rectangle -7500403 true true 94 189 148 233
Rectangle -7500403 true true 135 162 184 297
Circle -7500403 true true 219 76 28
Circle -7500403 true true 138 7 34
Circle -7500403 true true 67 93 30
Circle -7500403 true true 201 145 40
Circle -7500403 true true 69 193 40

car
false
0
Polygon -7500403 true true 300 180 279 164 261 144 240 135 226 132 213 106 203 84 185 63 159 50 135 50 75 60 0 150 0 165 0 225 300 225 300 180
Circle -16777216 true false 180 180 90
Circle -16777216 true false 30 180 90
Polygon -16777216 true false 162 80 132 78 134 135 209 135 194 105 189 96 180 89
Circle -7500403 true true 47 195 58
Circle -7500403 true true 195 195 58

cat
true
0
Line -7500403 true 285 240 210 240
Line -7500403 true 195 300 165 255
Line -7500403 true 15 240 90 240
Line -7500403 true 285 285 195 240
Line -7500403 true 105 300 135 255
Line -16777216 false 150 270 150 285
Line -16777216 false 15 75 15 120
Polygon -7500403 true true 300 15 285 30 255 30 225 75 195 60 255 15
Polygon -7500403 true true 285 135 210 135 180 150 180 45 285 90
Polygon -7500403 true true 120 45 120 210 180 210 180 45
Polygon -7500403 true true 180 195 165 300 240 285 255 225 285 195
Polygon -7500403 true true 180 225 195 285 165 300 150 300 150 255 165 225
Polygon -7500403 true true 195 195 195 165 225 150 255 135 285 135 285 195
Polygon -7500403 true true 15 135 90 135 120 150 120 45 15 90
Polygon -7500403 true true 120 195 135 300 60 285 45 225 15 195
Polygon -7500403 true true 120 225 105 285 135 300 150 300 150 255 135 225
Polygon -7500403 true true 105 195 105 165 75 150 45 135 15 135 15 195
Polygon -7500403 true true 285 120 270 90 285 15 300 15
Line -7500403 true 15 285 105 240
Polygon -7500403 true true 15 120 30 90 15 15 0 15
Polygon -7500403 true true 0 15 15 30 45 30 75 75 105 60 45 15
Line -16777216 false 164 262 209 262
Line -16777216 false 223 231 208 261
Line -16777216 false 136 262 91 262
Line -16777216 false 77 231 92 261

circle
false
0
Circle -7500403 true true 0 0 300

cow skull
true
0
Polygon -7500403 true true 150 90 75 105 60 150 75 210 105 285 195 285 225 210 240 150 225 105
Polygon -16777216 true false 150 150 90 195 90 150
Polygon -16777216 true false 150 150 210 195 210 150
Polygon -16777216 true false 105 285 135 270 150 285 165 270 195 285
Polygon -7500403 true true 240 150 263 143 278 126 287 102 287 79 280 53 273 38 261 25 246 15 227 8 241 26 253 46 258 68 257 96 246 116 229 126
Polygon -7500403 true true 60 150 37 143 22 126 13 102 13 79 20 53 27 38 39 25 54 15 73 8 59 26 47 46 42 68 43 96 54 116 71 126

dog
false
0
Polygon -7500403 true true 300 165 300 195 270 210 183 204 180 240 165 270 165 300 120 300 0 240 45 165 75 90 75 45 105 15 135 45 165 45 180 15 225 15 255 30 225 30 210 60 225 90 225 105
Polygon -16777216 true false 0 240 120 300 165 300 165 285 120 285 10 221
Line -16777216 false 210 60 180 45
Line -16777216 false 90 45 90 90
Line -16777216 false 90 90 105 105
Line -16777216 false 105 105 135 60
Line -16777216 false 90 45 135 60
Line -16777216 false 135 60 135 45
Line -16777216 false 181 203 151 203
Line -16777216 false 150 201 105 171
Circle -16777216 true false 171 88 34
Circle -16777216 false false 261 162 30

ghost
true
0
Polygon -7500403 true true 30 165 13 164 -2 149 0 135 -2 119 0 105 15 75 30 75 58 104 43 119 43 134 58 134 73 134 88 104 73 44 78 14 103 -1 193 -1 223 29 208 89 208 119 238 134 253 119 240 105 238 89 240 75 255 60 270 60 283 74 300 90 298 104 298 119 300 135 285 135 285 150 268 164 238 179 208 164 208 194 238 209 253 224 268 239 268 269 238 299 178 299 148 284 103 269 58 284 43 299 58 269 103 254 148 254 193 254 163 239 118 209 88 179 73 179 58 164
Line -16777216 false 189 253 215 253
Circle -16777216 true false 102 30 30
Polygon -16777216 true false 165 105 135 105 120 120 105 105 135 75 165 75 195 105 180 120
Circle -16777216 true false 160 30 30

heart
true
0
Circle -7500403 true true 152 19 134
Polygon -7500403 true true 150 105 240 105 270 135 150 270
Polygon -7500403 true true 150 105 60 105 30 135 150 270
Line -7500403 true 150 270 150 135
Rectangle -7500403 true true 135 90 180 135
Circle -7500403 true true 14 19 134

key
false
0
Rectangle -7500403 true true 90 120 300 150
Rectangle -7500403 true true 270 135 300 195
Rectangle -7500403 true true 195 135 225 195
Circle -7500403 true true 0 60 150
Circle -16777216 true false 30 90 90

leaf
true
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195

monster
true
0
Polygon -7500403 true true 75 150 90 195 210 195 225 150 255 120 255 45 180 0 120 0 45 45 45 120
Circle -16777216 true false 165 60 60
Circle -16777216 true false 75 60 60
Polygon -7500403 true true 225 150 285 195 285 285 255 300 255 210 180 165
Polygon -7500403 true true 75 150 15 195 15 285 45 300 45 210 120 165
Polygon -7500403 true true 210 210 225 285 195 285 165 165
Polygon -7500403 true true 90 210 75 285 105 285 135 165
Rectangle -7500403 true true 135 165 165 270

moon
false
0
Polygon -7500403 true true 175 7 83 36 25 108 27 186 79 250 134 271 205 274 281 239 207 233 152 216 113 185 104 132 110 77 132 51

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

@#$#@#$#@
NetLogo 5.0beta1
@#$#@#$#@
set number-vertices 6
random-seed 4
setup
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

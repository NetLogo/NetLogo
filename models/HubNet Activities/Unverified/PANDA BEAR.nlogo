;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variable and Breed declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

globals [
  ;; variables used to assign unique color and shape to clients
  shape-names        ;; list that holds the names of the shapes a student's turtle can have
  colors             ;; list that holds the colors used for students' turtles
  color-names        ;; list that holds the names of the colors used for students' turtles
  used-shape-colors  ;; list that holds the shape-color pairs that are already being used
  max-possible-codes ;; total number of possible unique shape/color combinations
]

breed [ students student ]  ;; created and controlled by the clients, also vertices in the polygon

students-own [
  user-id      ;; unique id, input by the client when they log in, to identify each student turtle
  step-size    ;; the current value of the STEP-SIZE input box on the client
  turn-amount  ;; the current value of the TURN-AMOUNT input box on the client
  next-student ;; points to the next student in the polygon (used when calculating area)
]

;;;;;;;;;;;;;;;;;;;;;
;; Setup Functions ;;
;;;;;;;;;;;;;;;;;;;;;

to startup
  hubnet-reset
  setup-vars
  setup
end

;; Deletes polygon sides, and shuffles the order of students
;; so that polygon is rearranged when edgify is next pressed.
;; Also initializes the plot.
to setup
  ask links [ die ]
  ask students [ set label "" ]
  send-info-to-all-clients
  reset-ticks
end

;; initialize global variables
to setup-vars
  set shape-names [
    "airplane"
    "android"
    "butterfly"
    "cactus"
    "cat"
    "cow skull"
    "ghost"
    "heart"
    "leaf"
    "monster"
  ]
  set colors (list brown green (violet + 1) (sky - 1))
  set color-names ["brown" "green" "purple" "blue"]
  set max-possible-codes (length colors * length shape-names)
  set used-shape-colors []
end

;; initialize the plot
to setup-plot
  set-current-plot "PANDA"
  clear-plot
end

;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;

to go
  every 0.1 [
    ;; get commands from the clients,
    ;; execute them,
    ;; and send the clients new data
    listen-clients
    display-angles-and-lengths
    tick
  ]
end

to display-angles-and-lengths
  if any? links
  [
    ask links [
      set label less-precise link-length
    ]
    ask students [
      if any? link-neighbors [
        ;; make sure the angle is positive
        let angle (subtract-headings towards first sort link-neighbors towards last sort link-neighbors) mod 360
        ;; make sure the angle is the interior angle
        if angle > 180 [ set angle 360 - angle ]
        set label less-precise angle
      ]
    ]
  ]
end

;; recursive procedure that links all the vertices together
;; one at a time.
to edgify  ;; student procedure
  ;; each student is linked to once and then, in turn links to
  ;; another student that has not yet been linked, when we
  ;; run out of students we've made a line and we just need
  ;; to close the polygon by linking back to the beginning
  let candidates other students with [ not any? link-neighbors ]
  ifelse any? candidates
  [
    set next-student one-of candidates
    create-link
    ask next-student [ edgify ]
  ]
  [
    set next-student one-of other students with [ count link-neighbors = 1 ]
    create-link
  ]
end

to create-link
  create-link-with next-student [
    set color white
    set label-color white
  ]
end

to-report perimeter
  report sum [link-length] of links
end

;; this area calculation is based on the formula found here:
;; http://mathworld.wolfram.com/PolygonArea.html
to-report area
  let result 0
  ask students [
    let addend ((xcor * [ycor] of next-student) -
                (ycor * [xcor] of next-student))
    set result result + addend
  ]
  report abs (result / 2)
end

;;; used to keep the clients from having too much cluttering detail
to-report less-precise [ precise-num ]
  report precision precise-num 1
end

;;;;;;;;;;;;;;;;;;;;;;;
;; HubNet Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;

;; determines which client sent a command, and what the command was
to listen-clients
  while [ hubnet-message-waiting? ]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ create-new-student ]
    [
      ifelse hubnet-exit-message?
      [ remove-student ]
      [
        ask students with [user-id = hubnet-message-source]
        [
          execute-command hubnet-message-tag
          send-info-to-all-clients
        ]
      ]
    ]
  ]
end

;; executes the correct command sent by client
to execute-command [command]
  if command = "Change Appearance"
  [ change-turtle stop ]
  if command = "Fd"
  [ move 1 stop ]
  if command = "Bk"
  [ move -1 stop ]
  if command = "Rt"
  [ rt turn-amount stop ]
  if command = "Lt"
  [ lt turn-amount stop ]
  if command = "Get Centered" [
    setxy round xcor round ycor
    stop
  ]
  if command = "step-size" [
    set step-size hubnet-message
    stop
  ]
  if command = "turn-amount" [
    set turn-amount hubnet-message
    stop
  ]
end

to move [ direction ] ;; student procedure
  let new-xcor (xcor + step-size * (dx * direction))
  let new-ycor (ycor + step-size * (dy * direction))
  ;; don't end up at the same place as any other students
  if not any? students with [xcor = new-xcor and ycor = new-ycor] [
    fd step-size * direction
    update-plots
  ]
  display
end

;; Create a turtle, set its shape, color, and position
;; and tell the node what its turtle looks like and where it is
to create-new-student
  create-students 1 [
    setup-student-vars
    if any? links [
      edgify
      update-plots
    ]
    send-info-to-client
  ]
end

;; sets the turtle variables to appropriate initial values
to setup-student-vars  ;; turtle procedure
  set user-id hubnet-message-source
  set-unique-shape-and-color
  setxy random-xcor random-ycor
  set heading 0
  set step-size 1
  set turn-amount 90
  set label-color white
end

;; Kill the turtle, set its shape, color, and position
;; and tell the node what its turtle looks like and where it is
to remove-student
  ask students with [user-id = hubnet-message-source] [
    set used-shape-colors remove my-code used-shape-colors
    die
  ]
  ;; when a student leaves if there is already a polygon
  ;; automatically make a new one if there are
  ;; enough students
  ifelse any? links and count students > 2
  [
    ask links [ die ]
    ask one-of students [ edgify ]
  ]
  [
    ;; if we don't have at least 3 students
    ;; dismantle the polygon since it is no longer a polygon
    ask links [ die ]
    ask students [ set label "" ]
  ]
  update-plots
end

to send-info-to-all-clients
  ask students [
    send-info-to-client
  ]
end

;; sends the appropriate monitor information back to the client
to send-info-to-client ;; student procedure
  hubnet-send user-id "You are a:" (word (color-string color) " " shape)
  hubnet-send user-id "Located at:" (word "(" less-precise xcor "," less-precise ycor ")")
  hubnet-send user-id "Heading:" heading
  ;; if there are no links perimeter and area don't apply
  hubnet-send user-id "Perimeter:" ifelse-value any? links [ less-precise perimeter ] [ "" ]
  hubnet-send user-id "Area:" ifelse-value any? links [ less-precise area ] [ "" ]
end

to change-turtle ;; student procedure
  set used-shape-colors remove my-code used-shape-colors
  set-unique-shape-and-color
end

;; pick a base-shape and color for the turtle
to set-unique-shape-and-color ;; student procedure
  let code random max-possible-codes
  while [member? code used-shape-colors and count students < max-possible-codes]
  [
    set code random max-possible-codes
  ]
  set used-shape-colors (lput code used-shape-colors)
  set shape item (code mod length shape-names) shape-names
  set color item (code / length shape-names) colors
end

;; report the string version of the turtle's color
to-report color-string [color-value]
  report item (position color-value colors) color-names
end

;; translates a student turtle's shape and color into a code
to-report my-code ;; student procedure
  report (position shape shape-names) + (length shape-names) * (position color colors)
end
@#$#@#$#@
GRAPHICS-WINDOW
330
10
890
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
1
ticks
30.0

BUTTON
107
15
202
48
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
204
15
299
48
edgify
ask links [ die ]\nifelse count students > 2\n[ ask one-of students [ edgify ] ]\n[ user-message \"You need at least 3 students to edgify\" ]
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
15
105
48
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
10
130
315
385
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
"perimeter" 1.0 0 -2064490 true "" "if any? links [ plot perimeter ]"
"area" 1.0 0 -8630108 true "" "if any? links [ plot area ]"

MONITOR
111
65
206
110
perimeter
less-precise perimeter
3
1
11

MONITOR
208
65
303
110
area
less-precise area
3
1
11

BUTTON
11
69
106
102
NIL
setup-plot
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

Perimeters and Areas by Embodied Agent Reasoning, or PANDA BEAR, is a microworld for mathematics learning that lies at the intersection of dynamic geometry environments and participatory simulation activities. In PANDA BEAR, individual students identify with and control a single vertex of a shared, group-polygon.

The measures of perimeter and area of the group-polygon are foregrounded in the environment. Group-level challenges involving the perimeter and area that cannot be solved by one individual are issued by the activity leader to the students. Through the communication of ideas and strategies with each other, the students collaboratively build solutions for the challenges.

## HOW TO USE IT

### Server interface

SETUP is run automatically when the activity is opened, and can be run whenever the teacher wants to clear the links comprising a group-polygon and prepare students to be formed into a different polygon when EDGIFY is pressed next. GO is the main button for allowing students to log in and move around. After the EDGIFY button has been pressed, the students will all be connected in a single group-polygon. When the students are connected, the PERIMETER and AREA monitors update automatically as students move their vertices around. The PANDA plot shows both of those measures over time as a record of the group's actions as they work towards a goal. SETUP-PLOT resets the plot to start a new challenge with the same group-polygon. To force all students' clients to exit the activity and re-enter, the teacher can press the RESET button on the HubNet Control Center.

### Client interface

The client interface allows each student to control one vertex in the group-polygon. The YOU ARE A: monitor shows a description of the shape and color of the vertex the student is controlling. CHANGE APPEARANCE changes the vertex's shape and color. The LOCATED AT: monitor shows the current coordinates of the student's vertex. The HEADING: monitor shows the current heading of the student's vertex - that is, the direction in which the vertex travels if asked to move forward. The PERIMETER: and AREA: monitors show the current measures of the group-polygon. The FD (forward), BK (back), LT (right-turn), and RT (right-turn) buttons change the student's vertex's location and heading. The STEP-SIZE and TURN-AMOUNT input boxes control the amount of movement of the FD, BK, LT, and RT buttons. The GET-CENTERED button rounds the student's vertex's x and y coordinates to the nearest integer, which can be helpful for coordinating with other students.

## THINGS TO NOTICE

In a triangle, for an individual vertex, moving "between" the other two vertices minimizes the perimeter for a given area.

In a triangle, when all three vertices attempt to form an isosceles triangle, an equilateral triangle is formed.

Strategies that work for challenges at the triangle level often work at the square level as well.

As the number of vertices is increased, the polygon that maximizes the area given a perimeter and minimizes the perimeter given an area gets closer and closer to a circle.

## THINGS TO TRY

With three students (and so, three vertices), ask the students to make the area as big as possible while keeping the perimeter at or below 25.

With three students (and so, three vertices), ask the students to make the perimeter as small as possible while keeping the area at or above 25.

Increase the number of students in the polygon from three to four (and beyond - approaching a circle) and issue similar challenges.

Modify the challenges in a patterned way. For example, with four students, doubling the allowed perimeter should quadruple the maximum area.

## EXTENDING THE MODEL

Add vertices that the students can't control.

Add different methods of movement. For example, instead of turning and going forward and backward, the students could be allowed to move in the 4 cardinal directions or with a mouse.

Allow the students to give their vertex movement rules to follow over and over so that the group-polygon "dances".

## NETLOGO FEATURES

This model uses links to form the sides of the polygon; each vertex is linked to exactly two other vertices.  The sum of the lengths of all the links is the perimeter of the polygon.

The area calculation is based on information found here: http://mathworld.wolfram.com/PolygonArea.html

## RELATED MODELS

PANDA BEAR Solo

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
NetLogo 5.0beta5
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
VIEW
222
10
772
560
0
0
0
1
1
1
1
1
0
1
1
1
-5
5
-5
5

MONITOR
33
117
120
166
Located at:
NIL
3
1

MONITOR
31
20
181
69
You are a:
NIL
3
1

BUTTON
31
67
181
100
Change Appearance
NIL
NIL
1
T
OBSERVER
NIL
NIL

BUTTON
81
226
144
259
Fd
NIL
NIL
1
T
OBSERVER
NIL
W

BUTTON
18
259
81
292
Lt
NIL
NIL
1
T
OBSERVER
NIL
A

BUTTON
144
259
207
292
Rt
NIL
NIL
1
T
OBSERVER
NIL
D

BUTTON
81
259
144
292
Bk
NIL
NIL
1
T
OBSERVER
NIL
S

MONITOR
121
117
181
166
Heading:
NIL
3
1

MONITOR
34
167
108
216
Perimeter:
NIL
1
1

MONITOR
109
167
181
216
Area:
NIL
1
1

INPUTBOX
113
301
194
361
turn-amount
90
1
0
Number

INPUTBOX
31
301
112
361
step-size
1
1
0
Number

BUTTON
61
371
167
404
Get Centered
NIL
NIL
1
T
OBSERVER
NIL
NIL

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

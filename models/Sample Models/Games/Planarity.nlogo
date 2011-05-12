globals [
  level    ;; determines how many nodes you have to untangle;
           ;; the formula is below
]

to setup
  clear-all
  set-default-shape turtles "circle"
  ask patches [ set pcolor white ]      ;; plain white background
  set level starting-level
  setup-level
end

to setup-level
  reset-ticks  ;; use tick counter as a move counter
  clear-turtles  ;; when the turtles die, the links connecting them die too
  ;; create nodes and position them randomly
  create-turtles 4 + level [
    set color blue
    setxy random-xcor random-ycor
  ]
  ;; Now we need to make some links.  We have to be careful that
  ;; the resulting graph has a solution.  Probably there are lots
  ;; of ways this could be done, but this was the simplest way we
  ;; could think of.
  ;; First make a bunch of links at random.
  while [count links < count turtles] [
    ask one-of turtles [
      ask one-of other turtles [ attempt-link ]
    ]
  ]
  ;; Then fill in all remaining allowable links.
  ask turtles [
    ask other turtles [ attempt-link ]
  ]
  ;; Now we have a graph which we know is solvable,
  ;; because the current layout is a solution.
  ;; Time to scramble the nodes around!
  while [solved?] [ scramble ]
  display
end

to attempt-link  ;; link procedure
  ;; note that if the link already exists, nothing happens
  create-link-with myself [
    if any-intersections? [ die ]
  ]
end

to scramble
  ;; The turtles agentset is always in random order,
  ;; so this makes a random layout.
  layout-circle turtles (world-width / 2 - 1)
end

;; This procedure lets us find the next turtle,
;; or the turtle two over, and so on.
to-report turtle-plus [n]  ;; turtle procedure
  report turtle ((who + n) mod count turtles)
end

to go
  if mouse-down? [
    ;; find the closest node
    let grabbed min-one-of turtles [distancexy mouse-xcor mouse-ycor]
    ;; loop until the mouse button is released
    while [mouse-down?] [
      ask grabbed [ setxy mouse-xcor mouse-ycor ]
      display
    ]
    ;; use tick counter as a move counter
    tick
    ;; check if the level is solved
    if solved? [
      user-message "You rock. Now try this..."
      set level level + 1
      setup-level
    ]
  ]
end

to-report solved?
  report all? links [not any-intersections?]
end

to-report any-intersections?  ;; link procedure
  report any? other links with [crossed? self myself]
end

to-report crossed? [link-a link-b]
  ;; store nodes in variables for easy access
  let a1 [end1] of link-a
  let a2 [end2] of link-a
  let b1 [end1] of link-b
  let b2 [end2] of link-b
  let nodes (turtle-set a1 a2 b1 b2)
  ;; if the links share a node, they don't cross
  if 4 > count nodes [ report false ]
  ;; but if two nodes are on top of each other, we will say
  ;; the links do cross (so you can't cheat that way)
  if 4 > length remove-duplicates [list xcor ycor] of nodes
    [ report true ]
  ;; if the ends of link-a are on opposite sides of link-b,
  ;; and the ends of link-b are on opposite sides of link-a,
  ;; then the links cross
  report [subtract-headings towards a2 towards b1 < 0 xor
          subtract-headings towards a2 towards b2 < 0] of a1
     and [subtract-headings towards b2 towards a1 < 0 xor
          subtract-headings towards b2 towards a2 < 0] of b1
end
@#$#@#$#@
GRAPHICS-WINDOW
219
10
649
461
17
17
12.0
1
10
1
1
1
0
0
0
1
-17
17
-17
17
1
1
1
moves

BUTTON
68
90
142
123
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
68
127
142
160
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

MONITOR
61
171
146
216
NIL
level
0
1
11

SLIDER
20
49
192
82
starting-level
starting-level
1
20
1
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This is a puzzle game where you try to untangle a graph. (A graph is a collection of nodes connected by lines.) Try to reposition the nodes so that no two lines cross. The more nodes, the harder it gets!

## HOW IT WORKS

The game knows how to generate solvable graphs, and it also knows how to detect whether any lines intersect. The details are in the Code tab.

## HOW TO USE IT

Use the STARTING-LEVEL slider to choose the initial difficulty level.  If you're a beginner, start at 1.  Press SETUP to set up a new board, then press GO to play.  Once the GO button is pressed, you can use your mouse to drag the nodes around.

Every level is solvable. One you find a solution, you will automatically be taken to the next level.

## THINGS TO NOTICE

The game only gives you solvable graphs. How might the game be able to guarantee this?  (One answer is in the Code tab.)

Can you draw an example of an unsolvable graph on a piece of paper?  How many nodes are in the smallest unsolvable graph?

On early levels, you can usually untangle the nodes without too much thought.  On later levels, you'll probably need to develop some conscious strategies.  What strategies do you find most effective?  When your friends play, do they use the same strategies you do?

## THINGS TO TRY

See how high a level you can solve.

Try to solve each level in the fewest number of moves. (The tick counter shows you how many moves you've made.)

## EXTENDING THE MODEL

Are there any other ways of generating solvable graphs besides the SETUP-LEVEL?  Does it matter what method is used? The more links you can make, the harder the level will be, but if you make too many links, the level might not be solvable at all!

Wherever two links intersect, add a small, brightly colored turtle to mark the intersection.  (You'll need two breeds of turtle, one for the nodes, one for the markers.  Intersecting Links Example has code for locating the intersection points.)

Make it possible to select multiple nodes and move them together.

## NETLOGO FEATURES

The nodes are turtles; the lines connecting them are links.  The code does not make use of patches (other than to make a plain white background).

NetLogo does not have a primitive which detects whether two links intersect.  To do the detection, the code uses the `subtract-headings` primitive and some math.

## RELATED MODELS

Intersecting Links Example -- has sample code for finding the point where two links intersect (unlike this model, which only determines whether that point exists or not)

## CREDITS AND REFERENCES

Thanks to Josh Unterman and Seth Tisue for their work on this model and to Jim Lyons for coding advice.

Original version created by John Tantalo, from an original concept by Mary Radcliffe. Tantalo's site is here: http://www.planarity.net/.

Solvable graphs are called "planar graphs" by mathematicians.  See http://en.wikipedia.org/wiki/Planar_graph.
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
set starting-level 8
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

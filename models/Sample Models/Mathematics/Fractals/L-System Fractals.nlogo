globals [rule-set]
turtles-own [len]

to setup
  clear-all
  crt 1 [
    set color init-color
    setxy init-x init-y
    set heading 0
    set len 128
    pen-down
  ]
  set rule-set "apply-rules"
  reset-ticks
end

to go
  ask turtles [ run rule-set ]
  tick
end

; write your own rules here.  use these commands:
;    "rt <number>" turns the turtle right by number degrees
;    "lt <number>" turns the turtle left by number degrees
;    "fd <number>" moves the turtle forward number steps and draws a line
;    "skip <number>" moves the turtle without drawing
;    "spawn" causes the turtle to duplicate itself
to apply-rules
;enter your rules here
  fd 3 rt 15 spawn fd 3 lt 30 fd 3
;end of your rules
end

;;;
;;; helper procedures
;;;

;hatch a new turtle; change color of old turtle
to spawn
  hatch 1
  set color color + color-inc
end

;move forward by steps but do not draw
to skip [steps]
  pen-up fd steps pen-down
end


;-----------------------------
;        example buttons
;-----------------------------

to setup-swirl
  set color-inc 9
  set init-color 9
  set init-x 0
  set init-y 0
  setup
  set rule-set "swirl"
end

to swirl
  fd 3 rt 10 spawn skip 3 lt 60 fd 5
end

to setup-ball
  set color-inc 9
  set init-color 9
  set init-x 0
  set init-y 0
  setup
  set rule-set "ball"
end

to ball
  fd 2 rt 45 spawn skip 3 lt 45 fd 3 lt 45
end

to setup-tree1
  set color-inc 9
  set init-color 9
  set init-x 0
  set init-y -50
  setup
  set rule-set "tree1"
end

to tree1
  fd 4 rt 15 fd 8 spawn rt 180 skip 8 rt 180 lt 15 fd 4 lt 15 spawn fd 8 die
end

to setup-tree2
  set color-inc 9
  set init-color 9
  set init-x 0
  set init-y -50
  setup
  set rule-set "tree2"
end

to tree2
  fd 10 rt 30 fd 5 spawn skip -5 lt 60 fd 5 spawn skip -5 rt 30 die
end

to setup-sierpinski
  set color-inc 3
  set init-color 9
  set init-x 0
  set init-y -25
  setup
  ask turtles [ set len 110 ]
  set rule-set "sierpinski"
end

to sierpinski
  set len len / 2
  repeat 3 [ fd len spawn rt 180 skip len rt 300 ]
  die
end

to setup-figure8
  set color-inc 9
  set init-color 9
  set init-x 0
  set init-y 0
  setup
  ask turtles [ set len 100 ]
  set rule-set "figure8"
end

to figure8
  set len len / 2
  repeat 4 [ fd len spawn rt 180 skip len lt 90 ]
  die
end

to setup-koch
  set color-inc 5
  set init-color 9
  set init-x -47
  set init-y -82
  setup
  ask turtles [ set len 160 ]
  set rule-set "koch"
end

to koch
  set len len / 3
  ifelse ticks = 0
  [
    repeat 3  ; we do these rules three times so that we can get the snowflake shape
    [ spawn fd len lt 60 spawn fd len rt 120 spawn fd len lt 60 spawn fd len rt 120 ]
  ]
  ; notice these are the same rules as above, except that we only do them once
  [ spawn fd len lt 60 spawn fd len rt 120 spawn fd len lt 60 spawn fd len rt 120 ]
  die
end
@#$#@#$#@
GRAPHICS-WINDOW
284
17
696
450
100
100
2.0
1
10
1
1
1
0
1
1
1
-100
100
-100
100
1
1
1
ticks
30

BUTTON
105
121
186
154
go once
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

BUTTON
36
121
91
154
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

SLIDER
12
164
170
197
color-inc
color-inc
0.0
100.0
9
1.0
1
NIL
HORIZONTAL

SLIDER
58
83
219
116
init-color
init-color
0.0
140.0
9
1.0
1
NIL
HORIZONTAL

SLIDER
24
46
145
79
init-x
init-x
-100.0
100.0
0
1.0
1
NIL
HORIZONTAL

SLIDER
148
46
263
79
init-y
init-y
-100.0
100.0
0
1.0
1
NIL
HORIZONTAL

BUTTON
189
121
244
154
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
15
297
70
330
Swirl
setup-swirl
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
297
133
330
Ball
setup-ball
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
144
297
199
330
Tree1
setup-tree1
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
207
297
262
330
Tree2
setup-tree2
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
15
338
124
374
Sierpinski's Tree
setup-sierpinski
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
76
383
202
416
Koch's Snowflake
setup-koch
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
133
338
275
374
Figure Eight Covering
setup-figure8
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
5
240
272
291
Examples: Press the buttons to setup examples of L-System Fractals.  Then press the GO or GO ONCE buttons to see them draw.
11
0.0
0

MONITOR
184
158
275
203
count turtles
count turtles
10
1
11

@#$#@#$#@
## WHAT IS IT?

This program draws special types of pictures called fractals.  A fractal is a shape that is self-similar --- that is, it looks the same no matter how closely you zoom in or out  For instance, a tree can be thought of as a fractal since if you look at the tree as a whole, you see a stick, that is to say the trunk, with branches coming out of it.  Then if you look at a smaller portion of it, say a branch, you see a similar thing, namely, a stick with branches coming out of it.

This model allows you to draw and look at one class of fractals, called L-system fractals.  L-System fractals are made by following a set of rules over and over.  The rules can be few, but a fascinating and complicated fractal can still form. By using different sets of rules, you can create a wide variety of different fractal designs.

## HOW IT WORKS

In the beginning, there will be only one turtle for any fractal.  Rules tell the turtle what to do.  This turtle can do things such as draw a line, or turn to the right or left.  This turtle can also split itself into two turtles. Each new turtle follows the same rules as the original turtle, and draws its own "branch" of the emerging fractal design.

## HOW TO USE IT

Perhaps the best way to start with this program is by looking at examples of some L-system fractals.  In the "Interface" tab, you will find a series of buttons: "Swirl", "Ball", etc.  Each button sets up a different fractal. Some of these examples are famous fractals, such as Sierpinski's tree and Koch's snowflake.  To view these examples, simply click on one of the example buttons and then press the GO ONCE button repeatedly.

You can also press GO and leave it pressed instead of pressing GO ONCE repeatedly.  Watch the number of turtles though (in the COUNT TURTLES monitor).  If it becomes very large the model may become very slow.

If you don't like the location of the fractal in the world you can change it by modifying the value of the following sliders:  
- INIT-X sets the initial x coordinate of the first turtle.  It changes the horizontal starting location of  the original turtle.  
- INIT-Y sets the initial y coordinate of the first turtle.  It changes the vertical starting location of  the original turtle.

If you don't like the color scheme of the fractal you can change it by modifying the value of the following sliders:  
- INIT-COLOR controls the initial color of the first turtle.  
- The value of COLOR-INC is added to the turtles color anytime a new turtle hatches.

The example buttons and the SETUP button choose the rules for a fractal.  Each of the example buttons sets up the world to draw the fractal of the same name.  The SETUP button sets up the world for drawing the rules found in the applyRules procedure in the Code tab.

## THINGS TO NOTICE

Notice the self-similarity of the fractals at each iteration.  What if one were to perform an infinite number of iterations?  Would looking at any piece up close look any different than looking at the whole?  Also notice how the number of turtles in each of the example is multiplied by some number at each iteration.  Does this make sense?  Try to figure out the number of turtles at some arbitrary step n for one or more of the examples.

## THINGS TO TRY

Now that you have played around with settings for the examples provided, why not try making your own fractals.  You can do this by changing the commands in the `apply-rules` procedure in the Code tab.  The comments in that procedure list the commands you can use.

## EXTENDING THE MODEL

Try adding switches such as increment-by-random-length or decrement-by-random-degree that could impose a random factor to the movement of the turtles.  Then add to the forward and turning functions, for example, a random value to the number of steps and degrees.  This would increase the realism in a fractal.

Pick up a book on fractals or search on the internet to find fractals that are interesting and try to create them using the rules of L-system fractals.  You may find some resources below.

Try extending the set of rules so that there are more than the basic ones included in this model.  Then use these rules to make new fractals.

Try starting with more than just one turtle, in a different location or heading, and see how that can affect the fractals that you have made.  Does it ruin them or does it make them more interesting and complex?

## NETLOGO FEATURES

The `hatch` command is used to make an exact duplicate of an existing turtle.

The `run` command is used to run the current rule set.

## RELATED MODELS

all of the other models in the Fractals section of the Models Library (under Mathematics)

## CREDITS AND REFERENCES

You may find more information on L-System fractals in the following locations:

This site offers a nice history and explanation of L-system fractals as well as quit a few classic examples of L-systems.  
http://spanky.triumf.ca/www/fractint/LSYS/tutor.html

This site offers an introduction to fractals, including L-system fractals as well as others.  
http://www.cs.wpi.edu/~matt/courses/cs563/talks/cbyrd/pres1.html

_The Fractal Geometry of Nature_ by Benoit Mandelbrot
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
repeat 14 [ go ]
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

turtles-own
[ new? ]

; setup the initial turtle and its properties
to setup
  clear-all
  crt 1
  [
    set shape "line"
    set color init-color
    setxy init-x init-y
    set heading 0
    pd
  ]
  reset-ticks
end

; have all non-new turtles draw an iteration of the tree
to go
  ask turtles
  [
    set new? false
    pen-down
  ]
; here are the commands to draw the tree
  ask turtles with [ not new? ]
  [
    fd 4
    rt 15
    fd 8
    hatch 1 [ set new? true ]
    set color (color + color-inc)
    rt 180
    jump 8
    rt 180
    lt 15
    fd 4
    lt 15
    hatch 1 [ set new? true ]
    set color (color + color-inc)
    fd 8
    die
  ]
  tick
end
@#$#@#$#@
GRAPHICS-WINDOW
265
10
677
443
100
100
2.0
1
10
1
1
1
0
0
0
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
94
42
175
75
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
22
42
77
75
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
8
79
252
112
color-inc
color-inc
0.0
100.0
7
1.0
1
NIL
HORIZONTAL

SLIDER
8
118
252
151
init-color
init-color
0.0
140.0
45
1.0
1
NIL
HORIZONTAL

SLIDER
8
154
252
187
init-x
init-x
-125.0
125.0
0
1.0
1
NIL
HORIZONTAL

SLIDER
8
191
252
224
init-y
init-y
-100.0
100.0
-50
1.0
1
NIL
HORIZONTAL

BUTTON
176
42
231
75
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
85
239
173
284
Num Turtles
count turtles
10
1
11

@#$#@#$#@
## WHAT IS IT?

This program draws special types of pictures called fractals.  A fractal is a shape that is self-similar - that is, it looks the same no matter how closely you zoom in or out.  For instance, a tree can be thought of as a fractal since if you look at the tree as a whole, you see a stick, that is to say the trunk, with branches coming out of it.  Then if you look at a smaller portion of it, say a branch, you see a similar thing, namely, a stick with branches coming out of it.

## HOW TO USE IT

Perhaps the best way to start with this program is by looking at an example of a fractal.  In the "Interface" tab, press the SETUP button to set up a fractal that draws a tree. Then press the GO button and watch it draw.

You can also have the fractal drawn one step a time.  Once you click the SETUP button, instead of clicking the GO button, click on the GO ONCE button.  This draws the fractal by drawing one iteration of the fractal per click as opposed to continuously, like the GO button.

If you don't like the location of the fractal you can change it by modifying the value of the following sliders:  
The INIT-X slider sets the initial x coordinate of the first turtle.  It changes the horizontal starting location of the original turtle.  
The INIT-Y slider sets the initial y coordinate of the first turtle.  It changes the vertical starting location of the original turtle.

If you don't like the color scheme of the fractal you can change it by modifying the value of the following sliders:  
The INIT-COLOR slider sets the initial color of the first turtle.  
The value of the COLOR-INC slider is added to the turtles color anytime a new turtle hatches.

The NUM TURTLES monitor indicates how many turtles are currently alive.

It might be best to limit the number of iterations to about 8 or 9 of any particular fractal.  If you go much more than that, NetLogo might run out of memory.

## THINGS TO NOTICE

Notice the self-similarity of the fractal at each iteration.  What if one were to perform an infinite number of iterations?  Would looking at any piece up close look any different than looking at the whole?  Also notice how the number of turtles in each of the example is multiplied by some number at each iteration.  Does this make sense?  Try to figure out the number of turtles at some arbitrary step n.

## THINGS TO TRY

Now that you have played around with settings for the example provided, why not try making your own tree fractals.  Use NetLogo commands to make your own fractals that look like trees.  Then change the initial color and color increment values to make the fractal more interesting to look at.

## EXTENDING THE MODEL

Try adding switches or sliders such as max-increment-random-length or min-increment-random-degree or random-length that could impose a random factor to the movement of the turtles.  The use of these plus the NetLogo primitive `random` would increase the realism in a fractal.  This would be especially useful in drawing realistic trees since branches are not spaced uniformly on a tree nor do they all branch from the trunk at the same angle.

Pick up a book on fractals or search on the internet to find fractals that are interesting and try to create them.  Also try to find different types of fractals such as L-System Fractals.  You may find some resources below.

Try starting with more than just one turtle, in a different location or heading, and see how that can affect the fractals that you have made.  Does it ruin them or does it make them more interesting and complex?

Try to make a realistic forest.  Does this require vastly different commands from making a realistic tree?

## NETLOGO FEATURES

Notice the use of agentsets to make some of the commands only affect certain turtles. For example, the reporter `with` is used to isolate non-new turtles and have the rules only affect them.

Also notice how the fractals are formed using several agents following the same rules through the use of the `hatch` primitive which makes it so simple to generate fractals like a tree.

## RELATED MODELS

L-System Fractals

## CREDITS AND REFERENCES

This site offers an introduction to fractals, including L-system fractals as well as others: http://www.cs.wpi.edu/~matt/courses/cs563/talks/cbyrd/pres1.html

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
repeat 10 [ go ]
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

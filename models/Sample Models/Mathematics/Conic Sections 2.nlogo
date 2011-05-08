globals [
  f0x    ;; xcor of focus 0 (for hyperbola)
  f0y    ;; ycor of focus 0 (for hyperbola)
  f1x    ;; xcor of focus 1 (for hyperbola)
  f1y    ;; ycor of focus 1 (for hyperbola)
  state  ;; flag to alternate between different values in mouse code
]

turtles-own [
  dist     ;; distance from 'goal' in current state
  diff     ;; modified distance to x and y intercepts in current state
]

to setup
  clear-all
  set f0x 0
  set f0y 0
  set f1x 0
  set f1y 0
  set state 0
  create-turtles num-turtles [
    set color green
    setxy random-xcor random-ycor
  ]
  move-focus-directrix 0 0
  reset-ticks
end

to go
   ifelse mouse-down?
   [ move-focus-directrix round mouse-xcor round mouse-ycor  ]
   [
     ask turtles [ move-turtles ]
     set state (state + 1) mod 2
   ]
  tick
end

to move-turtles
  ifelse directrix?
    [ parab ]
    [ hyperb ]
end

to hyperb  ;;turtle procedure
  let old-dist dist
  ;;distance between two foci at const
  set dist abs ((distancexy f0x f0y) - (distancexy f1x f1y))

  ;;find location to stop
  if round dist = const [ stop ]
  ifelse (dist < old-dist) and (dist > const)
  [ fd 1 ]
  [
    ifelse (dist > old-dist) and (dist < const)
    [ fd 1 ]
    [
      rt random-float 360
      fd 1
    ]
  ]
end

to parab   ;; turtle procedure
  let old-diff diff
  set dist distancexy f0x f0y
  let intx xcor
  let inty f1y
  ;;distance from directrix
  set diff (distancexy intx inty) - dist

  ;; locate position to stop
  if (abs diff) < 1 [ stop ]
  if (abs diff) > (abs old-diff)
    [ rt random-float 360 ]
  if not can-move? 1
    [ rt 180 ]
  fd 1
end

to move-focus-directrix [ x y ]
  ifelse state = 0
  [
   set f0x x
   set f0y y
  ]
  [
   set f1x x
   set f1y y
  ]
  clear-patches
  ask patch f0x f0y [ set pcolor white ]
  ifelse directrix?
    [ ask patches with [pycor = f1y] [ set pcolor white ] ]
    [ ask patch f1x f1y [ set pcolor white ] ]
end
@#$#@#$#@
GRAPHICS-WINDOW
242
10
621
410
20
20
9.0
1
10
1
1
1
0
0
0
1
-20
20
-20
20
1
1
1
ticks
30

BUTTON
15
76
112
109
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
123
76
229
109
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

SLIDER
9
156
231
189
const
const
0
20
6
1
1
NIL
HORIZONTAL

SWITCH
64
120
182
153
directrix?
directrix?
1
1
-1000

SLIDER
10
36
232
69
num-turtles
num-turtles
0
2000
800
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

The model displays two basic conic sections: hyperbolas and parabolas.  The figures are generated behaviorally as opposed to algebraically - the turtles attempt to behave like points on the specified shape. The partner to this model is called "Conic Sections 1".

A parabola is the set of all points that are the same distance from a point (focus) and a line (directrix).  A hyperbola is based on the same idea as is a parabola except that it is reflected over the directrix.

The ancient Greeks discovered that each conic section can be found by taking a cross section of one or two cones with their points pointing toward each other.  A circle results from taking a slice that is perpendicular to the axis, while an ellipse results from taking a slice of one cone that is not perpendicular to the axis. Similarly, a parabola results from a cross section that passes through one cone in a vertical fashion, such that the plane of the cut is parallel to one face. A hyperbola results from a vertical section that passes through both cones.

## HOW IT WORKS

The turtles use feedback to make decisions about how they behave. They set out in random directions, and then they receive information as to whether or not they are getting closer to where they want to be. If they are getting closer, they continue moving forward in the direction they are going. If they are moving farther away, they set out in a new random direction. This process is akin to the children's game of "Hot and Cold", in which players are told whether they are getting "hotter" or "colder" in relation to a hidden goal.

## HOW TO USE IT

### Hyperbolas

-Select the number of turtles with the NUM-TURTLES slider.  
-Press SETUP.  
-Make sure the DIRECTRIX? switch is set to OFF.  
-Press GO  
-click at two points in the view to set the foci
 
### Parabolas

-Select the number of turtles with the NUM-TURTLES slider.  
-Press SETUP.  
-Make sure the DIRECTRIX? switch is set to TRUE.  
-Press GO  
-click at two different locations in the view to set the directrix and the focus.

## THINGS TO NOTICE

When forming a hyperbola, turtles adjust their positions from two user-defined foci so that the difference between their distances from the foci attains a  
value of CONSTANT.

When forming a parabola, turtles move to an equal distance from the directrix to the focus.

## THINGS TO TRY

Adjust the slope of the parabola's directrix or the value of CONSTANT for the hyperbola while the turtles are still moving. See how they react to the changes in their environment.

You may be able to get a better feeling for the turtles' behavior if only a few turtles are in the world at one time.  Try setting num to a small value (like 16 or 1), and watching the turtles.

Both of these conic sections can be observed by shining a flashlight at a cone and looking at its shadow.  Can you figure out at what angles the cone must be held?

## EXTENDING THE MODEL

Look at the StarLogoT model 'emergent-circle'. Watch how the turtles react with each other- something that is missing from 'Conic Sections'. Implement this emergent behavior for one or all of the conics in this project.

## NETLOGO FEATURES

Like more traditional programming languages (e.g. Java), NetLogo can have procedures that report a value to the caller. The command used is called `report` --- it takes one input, the value to be reported. Look at the function `pos`. It takes two inputs, `x` and `y`, and reports a boolean.

## RELATED MODELS

Conic Sections 1

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
NetLogo 5.0beta2
@#$#@#$#@
setup
set f0x 12
set f0y 10
set f1x 0
set f1y -13
ask (patch-set patch f0x f0y patch f1x f1y) [ set pcolor white ]
ask turtles [ repeat 25 [ move-turtles ] ]
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

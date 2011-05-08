turtles-own [
  leader    ;; the turtle this turtle is following,
            ;; or nobody if not following
  follower  ;; the turtle that is following this turtle,
            ;; or nobody if not being followed
]

to setup
  clear-all
  crt population
  [ set color magenta
    setxy random-xcor random-ycor
    set leader nobody
    set follower nobody ]
  reset-ticks
end

to go
  ask turtles
  [ if leader = nobody
    [ attach-turtle ] ]
  ask turtles [ turn-turtle ]
  ask turtles [ fd 1 ]
  tick
end

to attach-turtle  ;; turtle procedure
  ;; find a random patch to test inside the donut
  let xd near-radius + random (far-radius - near-radius)
  let yd near-radius + random (far-radius - near-radius)
  if random 2 = 0 [ set xd (- xd) ]
  if random 2 = 0 [ set yd (- yd) ]
  ;; check for free turtles on that patch
  let candidate one-of (turtles-at xd yd) with [follower = nobody]
  ;; if we didn't find a suitable turtle, stop
  if candidate = nobody [ stop ]
  ;; we're all set, so latch on!
  ask candidate [ set follower myself ]
  set leader candidate
  ;; change our color
  ifelse follower = nobody
  [ set color lime ]
  [ set color sky
    set shape "line" ]
  ;; change our leader's color
  ask candidate
  [ ifelse leader = nobody
    [ set color yellow ]
    [ set color sky
      set shape "line" ] ]
end

to turn-turtle  ;; turtle procedure
  ;; if we are still unattached...
  ifelse leader = nobody
  ;; do a somewhat random glide
  [ rt random-float waver - random-float waver ]
  ;; otherwise follow the leader
  [ face leader ]
end
@#$#@#$#@
GRAPHICS-WINDOW
318
10
816
529
30
30
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
-30
30
-30
30
1
1
1
ticks

BUTTON
153
73
240
106
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

BUTTON
53
73
138
106
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
73
186
223
219
waver
waver
0.0
359.0
70
1.0
1
NIL
HORIZONTAL

SLIDER
73
151
223
184
far-radius
far-radius
0.0
50.0
10
1.0
1
NIL
HORIZONTAL

SLIDER
52
33
240
66
population
population
0
10000
1500
10
1
NIL
HORIZONTAL

SLIDER
73
116
223
149
near-radius
near-radius
0.0
50.0
5
1.0
1
NIL
HORIZONTAL

PLOT
5
232
313
529
Turtle Count
time
#
0.0
50.0
0.0
350.0
true
true
"set-plot-y-range 0 population" ""
PENS
"unattached" 1.0 0 -5825686 true "" "plot count turtles with [color = magenta]"
"heads" 1.0 0 -1184463 true "" "plot count turtles with [color = yellow]"
"bodies" 1.0 0 -13791810 true "" "plot count turtles with [color = sky]"
"tails" 1.0 0 -10899396 true "" "plot count turtles with [color = lime]"

@#$#@#$#@
## WHAT IS IT?

In Follower, turtles attempt to "connect" with other turtles, forming long chains according to a small set of simple rules.

## HOW IT WORKS

A turtle can follow only one other turtle, and similarly it can only be followed by one other turtle. This means there are four turtle states, each represented by a different color:

- Magenta: Unattached
- Green: Following another turtle (a "tail")
- Yellow: Being followed by another turtle (a "head")
- Blue: Following and being followed by other turtles (a "body" segment)

Turtles are created in the magenta (unattached) state. At each turn, each turtle checks a random patch in a square donut shape around itself. If it finds a turtle there who is not already being followed, it will "latch on" and start following the movements of that turtle. An unattached turtle (one that has not yet "latched on" to another turtle) will move randomly.

## HOW TO USE IT

SETUP: Clears the world and creates the number of turtles specified in the POPULATION slider. All the turtles are created in an unattached state (magenta).

GO: Runs the simulation.

POPULATION: Specifies the number of turtles created in SETUP.

NEAR-RADIUS: The inner radius of the square donut turtles search in.

FAR-RADIUS: The outer radius of the square donut turtles search in.

WAVER: The amount of randomness in the movement of non-following turtles.

There is also a plot called "Turtle Count" that displays, over time, the number of turtles in each state.

## THINGS TO NOTICE

Each of the sliders has a different effect on the simulation.

Notice that the when the number of turtles is high, chains tend to form very quickly. This is because there are more turtles and therefore more chances for each turtle to attach on each turn.

Varying the size of the donut tends to affect how the turtles start to attach, but in the long run doesn't have a big effect on the simulation outcome. Donut size is most interesting at very small values, which causes the turtles to attach into very small loops. Also, very large thick donuts (big FAR-RADIUS, small NEAR-RADIUS) looks interesting with a large number of turtles.

The amount of movement randomness (set by WAVER) can also change the simulation outcome. Very high values for WAVER result in small loops being formed because the turtles are continuously moving over themselves, increasing the chances of connecting the head of the chain to its tail.

The simulation, under any parameters, moves towards forming loops. Loops may be formed by wrapping around the world, but in almost all cases if the simulation is left running long enough you will get many small loops or one big loop. It is possible that a chain will never connect to itself if the WAVER slider is set to zero, but otherwise the simulation should proceed towards loops.

## THINGS TO TRY

Try making the waver setting very high. Notice how the turtles clump up into little clusters. This is because they are moving over themselves frequently, which increases the chances of attaching to their tail. This makes sense if the donut allows the turtles to check close to themselves, but if it does not (NEAR-RADIUS is big) then the same thing still happens. Why is this the case?

Notice that we are plotting all four possible turtle states, but only three appear on the plot. Do you know why?

## EXTENDING THE MODEL

Try implementing different rules for how turtles follow each other. For instance, interesting results can be seen if you turn off world wrapping. Or, what if two turtles are allowed to have the same leader?

You might also try giving the turtles a certain probability of breaking apart again. See how this affects the patterns they make.

## NETLOGO FEATURES

Note the use of the `towards` primitive to make the turtles follow each other.

Note also that we are careful to ensure that we never have two turtles following the same leader.

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

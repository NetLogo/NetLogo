turtles-own [
  visible-neighbors      ;; what birds can I see nearby?
  closest-neighbor       ;; who's the closest bird I can see?
  speed                  ;; what speed am I flying at?
  happy?                 ;; am I content with my current place?
]

;;
;; Setup Procedures
;;

to setup
  clear-all
  create-turtles number-of-birds [
    setxy random-xcor random-ycor
    set speed base-speed
    set size 1.5 ; easier to see
    set happy? false
    recolor
  ]
  reset-ticks
end

;;
;; Runtime Procedures
;;

to go
  ask turtles [
    set speed base-speed
    set visible-neighbors (other turtles in-cone vision-distance vision-cone)
    ifelse any? visible-neighbors
      [ adjust ]
      [ set happy? true ]
    recolor
    fd speed  ; fly forward!
  ]
  tick
end

to adjust ;; turtle procedure
  set closest-neighbor min-one-of visible-neighbors [distance myself]
  let closest-distance distance closest-neighbor
  ;; if I am too far away from the nearest bird I can see, then try to get near them
  if closest-distance > updraft-distance [
    turn-towards (towards closest-neighbor)
    set speed base-speed * (1 + speed-change-factor)
    set happy? false
    stop
  ]

  ;; if my view is obstructed, move sideways randomly
  if any? visible-neighbors in-cone vision-distance obstruction-cone [
    turn-at-most (random-float (max-turn * 2) - max-turn)
    set speed base-speed * (1 + speed-change-factor)
    set happy? false
    stop
  ]

  ;; if i am too close to the nearest bird slow down
  if closest-distance < too-close [
    set happy? false
    set speed base-speed * (1 - speed-change-factor)
    stop
  ]

  ;; if all three conditions are filled, adjust
  ;; to the speed and heading of my neighbor and take it easy
  set speed [speed] of closest-neighbor
  turn-towards [heading] of closest-neighbor
  set happy? true
end

to recolor ;; turtle procedure
  ifelse show-unhappy? [
    ifelse happy?
      [ set color white ]
      [ set color red ]
  ][
    ;; This changes the bird's color some shade of yellow --
    ;; note that the color is based on WHO number, not RANDOM, so birds
    ;; won't change color if the SHOW-UNHAPPY? switch is flicked on and off
    set color yellow - 2 + (who mod 7)
  ]
end

;;
;; TURTLE UTILITY PROCEDURES, for turning gradually towards a new heading
;;

to turn-towards [new-heading]  ;; turtle procedure
  turn-at-most (subtract-headings new-heading heading)
end

to turn-at-most [turn]  ;; turtle procedure
  ifelse abs turn > max-turn [
    ifelse turn >= 0
      [ rt max-turn ]
      [ lt max-turn ]
  ] [
    rt turn
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
200
10
707
538
35
35
7.0
1
10
1
1
1
0
1
1
1
-35
35
-35
35
1
1
1
ticks
45.0

SLIDER
10
115
190
148
vision-distance
vision-distance
0
20
20
1
1
NIL
HORIZONTAL

SLIDER
10
325
190
358
updraft-distance
updraft-distance
1
50
4
1
1
NIL
HORIZONTAL

SLIDER
10
185
190
218
obstruction-cone
obstruction-cone
0
vision-cone
45
1
1
deg
HORIZONTAL

BUTTON
100
50
185
83
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
10
50
95
83
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
10
255
190
288
base-speed
base-speed
0
2
0.2
.01
1
NIL
HORIZONTAL

SLIDER
10
150
190
183
vision-cone
vision-cone
0
270
120
1
1
deg
HORIZONTAL

SLIDER
10
360
190
393
too-close
too-close
.5
updraft-distance - .1
1.6
.1
1
NIL
HORIZONTAL

SLIDER
10
395
190
428
max-turn
max-turn
1
30
4
1
1
deg
HORIZONTAL

SWITCH
10
460
190
493
show-unhappy?
show-unhappy?
1
1
-1000

SLIDER
10
10
185
43
number-of-birds
number-of-birds
0
100
30
1
1
NIL
HORIZONTAL

TEXTBOX
40
95
175
113
Vision Parameters
13
0.0
1

TEXTBOX
40
235
170
253
Motion Parameters
13
0.0
1

TEXTBOX
55
440
180
458
Visualization
13
0.0
1

SLIDER
10
290
190
323
speed-change-factor
speed-change-factor
0
1
0.15
.05
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This is an adaptation of the Flocking model to produce V-like formations in artificial flocks.  Not all birds produce V-like formations when they flock, but they are often observed in large birds traveling together distances (e.g. migrating Canada geese).  This model demonstrates simple rules that cause V-formations to occur.

## HOW IT WORKS

Each bird starts out with a random position and heading in the world. If the bird cannot see any other birds in its limited vision range, it will continue to fly straight at its normal base speed.  Otherwise, each bird follow four basic rules, given by this order of precedence.

1. If a bird is too far away (that is, further than the distance for getting an updraft benefit) from the nearest visible bird, it will turn toward that bird and speed up to get near it.
2. Once a bird is near enough another bird, it will move randomly to one side or another until its view is no longer obstructed.
3. If a bird gets too close to another bird, it will slow down.
4. Once the three conditions above are met (the bird has an unobstructed view and is sufficiently close but not too close to another bird), the bird will set both its speed and its heading that of its closest visible neighbor.

## HOW TO USE IT

NUMBER-OF-BIRDS sets the number of birds in the world.  
Use SETUP to populate the world with birds, and GO to run the model.

Vision Parameters:  
VISION-DISTANCE and VISION-CONE defines the radius and angle span, respectively, of the area within which a bird can see another bird. A VISION-CONE of 120 means that the bird can see up to 60 degrees to the right and 60 degrees to the left. OBSTRUCTION-CONE defines the angle span for which a bird considers its vision to be obstructed by another bird.

Motion Parameters:  
BASE-SPEED defines the speed that birds will fly if they are not speeding up to catch another bird that they see, or slowing down to avoid colliding with a bird.  
SPEED-CHANGE-FACTOR is the factor by which birds increase or decrease their speed, given as a fraction of their base speed.  A BASE-SPEED of 1 with a SPEED-CHANGE-FACTOR of 0.25 means that birds will travel at speeds of 0.75 (slow speed), 1.0 (normal speed), or 1.25 (fast speed).  
UPDRAFT-DISTANCE defines how near to another bird one must be to take advantage of its upwash.  
TOO-CLOSE defines how close one can be to another bird before slowing down to avoid collision.  
MAX-TURN sets the maximum number of degrees that a bird can turn during a single tick.

Visualization Parameters:  
If SHOW-UNHAPPY? is switched ON, birds that have not satisfied the conditions outlined in the HOW IT WORKS section are colored red, and all the birds that have are colored white. If SHOW-UNHAPPY? is OFF, birds are colored varying shades of yellow.

## THINGS TO NOTICE

After forming a vee-flock for a while, birds tend to drift apart from one another.

Birds following these rules may form flock formations other than a balanced V. These include asymmetrical (unbalanced) V shapes, echelons (diagonal lines), or inverted V shapes.  In fact, imperfect formations such as these are more commonly observed than balanced vees in nature as well.

## THINGS TO TRY

Play with the sliders to see if you can get tighter flocks, looser flocks, fewer flocks, more flocks, more or less splitting and joining of flocks, more or less rearranging of birds within flocks, etc.

Does having lots of birds make it more or less likely to form good V shapes?

Can you find parameters such that running the model for a long time produces a single static flock?  Or will the birds never settle down to a fixed formation?

## EXTENDING THE MODEL

What would happen if you put walls around the edges of the world that the birds can't fly into?

What would happen if you gave birds limited energy levels, so that flying fast would make them expend more energy and eventually become exhausted.  Flying slowly, or within another bird's updraft, could allow them to recoup some energy.

Try making a 3D version of this model.  What additional considerations have to be taken into account in three dimensions?

## NETLOGO FEATURES

Birds use `in-cone` to find neighbors and determine whether their view is obstructed.

The `subtract-headings` primitive is also useful, for turning gradually (only partway) toward some new heading.

## RELATED MODELS

Flocking

## CREDITS AND REFERENCES

This model is loosely based on rules introduced in the paper:  
Nathan, A. & Barbosa, V. C. (2008). V-like formations in flocks of artificial birds. Artificial Life, 14(2), pp. 179-188. (available at http://arxiv.org/pdf/cs/0611032)
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
NetLogo 5.0beta3
@#$#@#$#@
setup
repeat 1000 [ go ]
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
1
@#$#@#$#@

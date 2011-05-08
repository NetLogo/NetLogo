breed [ lights light ]
breed [ moths moth ]

globals
[
  scale-factor  ;; to control the form of the light field
]

lights-own
[
  intensity
]

moths-own
[
  ;; +1 means the moths turn to the right to try to evade a bright light
  ;; (and thus circle the light source clockwise). -1 means the moths
  ;; turn to the left (and circle the light counter-clockwise)
  ;; The direction tendency is assigned to each moth when it is created and does not
  ;; change during the moth's lifetime.
  direction
]

patches-own
[
  light-level ;; represents the light energy from all light sources
]

to setup
  clear-all
  set-default-shape lights "circle 2"
  set-default-shape moths "butterfly"
  set scale-factor 50
  if number-lights > 0
  [
    make-lights number-lights
    ask patches [ generate-field ]
  ]
  make-moths number-moths
  reset-ticks
end

to go
  ask moths [ move-thru-field ]
  tick
end

;;;;;;;;;;;;;;;;;;;;;;
;; Setup Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;

to make-lights [ number ]
  create-lights number [
    set color white
    jump 10 + random-float (max-pxcor - 30)
    set intensity random luminance + 20
    set size sqrt intensity
  ]
end

to make-moths [ number ]
  create-moths number [
    ifelse (random 2 = 0)
      [ set direction 1 ]
      [ set direction -1 ]
    set color white
    jump random-float max-pxcor
    set size 5
  ]
end

to generate-field ;; patch procedure
  set light-level 0
  ;; every patch needs to check in with every light
  ask lights
    [ set-field myself ]
  set pcolor scale-color blue (sqrt light-level) 0.1 ( sqrt ( 20 * max [intensity] of lights ) )
end

;; do the calculations for the light on one patch due to one light
;; which is proportional to the distance from the light squared.
to set-field [p]  ;; turtle procedure; input p is a patch
  let rsquared (distance p) ^ 2
  let amount intensity * scale-factor
  ifelse rsquared = 0
    [ set amount amount * 1000 ]
    [ set amount amount / rsquared ]
  ask p [ set light-level light-level + amount ]
end

;;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;;

to move-thru-field    ;; turtle procedure
  ifelse (light-level <= ( 1 / (10 * sensitivity) ))
  [
    ;; if there is no detectable light move randomly
    rt flutter-amount 45
  ]
  [
    ifelse (random 25 = 0)
    ;; add some additional randomness to the moth's movement, this allows some small
    ;; probability that the moth might "escape" from the light.
    [
      rt flutter-amount 60
    ]
    [
      ;; turn toward the brightest light
      maximize
      ;; if the light ahead is not above the sensitivity threshold  head towards the light
      ;; otherwise move randomly
      ifelse ( [light-level] of patch-ahead 1 / light-level > ( 1 + 1 / (10 * sensitivity) ) )
      [
        lt ( direction * turn-angle )
      ]
      [
        rt flutter-amount 60
      ]
    ]
  ]
  if not can-move? 1
    [ maximize ]
  fd 1
end

to maximize  ;; turtle procedure
  face max-one-of patches in-radius 1 [light-level]
end

to-report flutter-amount [limit]
  ;; This routine takes a number as an input and returns a random value between
  ;; (+1 * input value) and (-1 * input value).
  ;; It is used to add a random flutter to the moth's movements
  report random-float (2 * limit) - limit
end
@#$#@#$#@
GRAPHICS-WINDOW
280
10
692
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

BUTTON
73
157
139
190
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
140
115
262
148
luminance
luminance
1
10
3
1
1
NIL
HORIZONTAL

BUTTON
141
157
204
190
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
16
115
138
148
number-lights
number-lights
0
5
2
1
1
NIL
HORIZONTAL

SLIDER
54
80
226
113
number-moths
number-moths
1
20
10
1
1
NIL
HORIZONTAL

SLIDER
45
198
230
231
sensitivity
sensitivity
1
3
2
0.25
1
NIL
HORIZONTAL

SLIDER
45
233
230
266
turn-angle
turn-angle
45
180
105
5
1
degrees
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This model demonstrates moths flying in circles around a light.  Each moth follows a set of simple rules.  None of the rules specify that the moth should seek and then circle a light.  Rather, the observed pattern arises out of the combination of the moth's random flight and the simple behavioral rules described below.

Scientists have proposed several explanations for why moths are attracted to and then circle lights. For example, scientists once believed that moths navigated through the sky by orienting themselves to the moon, and that the moths' attraction to nearby, earthly light sources (such as a street lamp) arose because they mistook the terrestrial lights for the moon.  However, while this explanation may seem reasonable, it is not supported by available scientific evidence.

## HOW IT WORKS

Moths exhibit two basic kinds of behavior.  When they detect a light source from a distance (as far as 200 feet away) moths tend to fly straight toward the light.  Then, when moths get close to the light, they tend to turn away from the light in order to avoid it.

First, moths sense the light in their immediate vicinity and turn toward the direction where the light is greatest.

Second, moths compare the light immediately ahead of them with the light at their current position.  If the ratio of 'light just ahead' to 'light here' is below a threshold value, then the moths fly forward toward the light.  If the ratio of 'light just ahead' to 'light here' is above a threshold value, then moths turns away from the light.  The threshold is determined by the moths' sensitivity to light.

If the moths do not detect any light, or if there simply are no lights in the space where the moths are flying, then the moths flutter about randomly.

Note that light energy is represented in this model as decreasing with the square of the distance from the light source.  This characteristic is known as a "one over r-squared relationship," and is comparable to the way electrical field strength decreases with the distance from an electrical charge and the way that gravitational field strength decreases with the distance from a massive body.

## HOW TO USE IT

Click the SETUP button to create NUMBER-LIGHTS with LUMINANCE and NUMBER-MOTHS.  Click the GO button to start the simulation.

NUMBER-MOTHS:  This slider determines how many lights will be created when the SETUP button is pressed.

NUMBER-LIGHTS:  This slider determines how many lights will be created when the SETUP button pressed.  Note that this value only affects the model at setup.

LUMINANCE:  This slider influences how bright the lights will be.  When a light is created, it is assigned a luminance of 20 plus a random value between 0 and LUMINANCE. Lights with a higher luminance can be sensed by moths from farther away.  Note that changing LUMINANCE while the model is running has no effect.

SENSITIVITY:  This slider determines how sensitive the moths are to light.  When SENSITIVITY is higher, moths are able to detect a given light source from a greater distance and will turn away from the light source at a greater distance.

TURN-ANGLE:  This slider determines the angle that moths turn away when they sense that the ratio of 'light ahead' to 'light here' is above their threshold value.

## THINGS TO NOTICE

When the model begins, notice how moths are attracted to the two lights.  What happens when the lights are created very close together?  What happens when the lights are created very far apart?

Do all of the moths circle the same light?  When a moth begins to circle one light, does it ever change to circling the other light?  Why or why not?

## THINGS TO TRY

Run the simulation without any lights.  What can you say about the moths' flight patterns?

With the simulation stopped, use the following values:
- NUMBER-LIGHTS: 1
- LUMINANCE: 1
- NUMBER-MOTHS: 10
- SENSITIVITY: 1.00
- TURN-ANGLE: 95
Notice that, at first, some moths might fly about randomly while others are attracted to the light immediately.  Why?

While the model is running increase SENSITIVITY.  What happens to the moths' flight patterns?  See if you can create conditions in which one or more of the moths can 'escape' from its state of perpetually circling the light.

Vary the TURN-ANGLE.  What happens?  Why do you think the moths behave as observed with different values of TURN-ANGLE?  What value or values do you think are most realistic?

It would be interesting to better understand the flight patterns of the moths in the model.  Add code to the model that allows you to track the movements of one or more moths (for example, by using the pen features).  Do you see a pattern?  Why might such a pattern appear and how can it be altered?

## EXTENDING THE MODEL

This model offers only one set of rules for generating moths' circular flight around a light source.  Can you think of different ways to define the rules?

Alternatively, can you imagine a way to model an earlier theory of moth behavior in which moths navigate straight lines by orienting themselves to the moon?  Do rules that allow moths to navigate according to their position relative to the moon lead to the observed circling behavior around light sources that are much, much closer than the far-away moon?

## NETLOGO FEATURES

This model creates a field of light across the patches, using `scale-color` to display the value, and the moths use `face` and `max-one-of` to traverse the light field.

## RELATED MODELS

Ants, Ant Lines, Fireflies, Flocking

## CREDITS AND REFERENCES

Adams, C.  (1989).  Why are moths attracted to bright lights?  Retrieved May 1, 2005, from http://www.straightdope.com/classics/a5_038.html
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
Circle -7500403 true true 16 16 270
Circle -16777216 true false 46 46 210

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

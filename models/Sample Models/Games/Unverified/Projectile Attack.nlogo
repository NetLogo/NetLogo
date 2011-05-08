breed [ clouds cloud ]
breed [ targets target ]
breed [ tanks tank ]
breed [ guns gun ]
breed [ shells shell ]


globals [ x-vel y-vel velocity previous-wall-height previous-wall-pos]

to setup
  clear-all
  set-default-shape tanks "tank"
  set-default-shape guns "gun"
  set-default-shape clouds "cloud"
  set-default-shape shells "ball"

  ask patches [ set pcolor 97 ]
  ask patches with [ pycor < -15 ]
    [ set pcolor black ]
  ; next we create the tank, it's gun, and the aiming arrow
  create-tanks 1
  [
    set heading angle
    setxy -16 -14.2
    set color green - 2
    set size 3
    hatch-guns 1 ; the tank's gun/nozzle
    [
      set size 1
      set color green - 3
      ; this confusing line of code creates a directed "tie" link
      ; between the tank and its gun
      ask myself [ create-link-to myself  [ tie hide-link ]]

      hatch 1 ; the "aiming arrow"
      [
        set breed turtles
        fd 3
        set color red - 1
        ; and now we create a directed "tie" link between
        ; the gun and its aiming arrow
        ask myself [ create-link-to myself [ tie hide-link ]]
      ]
    ]
  ]
  create-clouds 1
  [
    set shape "cloud"
    setxy 0 15
    set color 8
    set size 3.5
    set heading 90
  ]
  create-targets 1
  [
    set color black
    set shape "heli"
    setxy 15 (random 32) - 15
    set size 2
  ]

  ; create the wall
  ask patches with [ pycor > -16 ]
  [
    ifelse (pycor < wall-height - 15 and pxcor = wall-position )
      [ set pcolor gray ]
      [ set pcolor 97 ]
  ]
  set previous-wall-height wall-height
  set previous-wall-pos wall-position
end

to go
  if not any? targets [ stop ]

  ask tanks [ set heading angle ]

  if ( previous-wall-height != wall-height or previous-wall-pos != wall-position )
  [
    ask patches with [ pycor > -16 ]
    [ ifelse (pycor < wall-height - 15 and pxcor = wall-position )
        [ set pcolor gray ]
        [ set pcolor 97 ]
    ]
    set previous-wall-height wall-height
    set previous-wall-pos wall-position
  ]
  every .05
  [
    ask shells
    [
      setxy (xcor + x-vel) (ycor + y-vel)
      set y-vel (y-vel - .01)
      ; NOTE: this is *NOT* modeling a realistic wind effect on a projectile.
      ; Instead, we just treat wind like gravitational acceleration along x-axis
      if (wall-block-wind? = false or
         (wall-block-wind? = true and (ycor > wall-height - 15) or ((xcor - wall-position) * wind < 0)))
        [ set x-vel (x-vel + (wind / 10000)) ]
      set velocity sqrt (( x-vel ^ 2 ) + (y-vel ^ 2))
      if (velocity > 1)
      [
        set x-vel x-vel / velocity
        set y-vel y-vel / velocity
        set velocity 1
      ]
      check-shell
    ]
    ask clouds [ setxy (xcor + wind / 100) (ycor) ]
  ]
  display
end

to fire
  if not any? shells
  [
    ask tanks
    [
      hatch-shells 1
      [
        set size 1
        set color black
        set x-vel ( sin angle * ( Power / 100 ))
        set y-vel ( cos angle * ( Power / 100 ))
        set velocity Power / 100
      ]
    ]
  ]
end

; This procedure uses a clever hack to simulate having no ceiling for the projectile
; Basically, if the projectile goes up above the ceiling, it actually wraps around,
; but it becomes hidden, until it comes back down again.  Obviously, this method
; isn't bulletproof, but it works pretty well for the purposes of this game.
to check-shell
  if ( pycor = -17 and hidden? = false )
      [ hide-turtle ]
  if ( pycor = 17 and hidden? = true )
      [ show-turtle ]
  if ( hidden? = false )
  [
     if ( xcor > 17 or ycor < -15 or pcolor = gray )
       [ die ]
     if ( any? targets-here )
       [
         ask patches in-radius 2 [ set pcolor yellow ]
         ask patches in-radius 1 [ set pcolor red ]
         ask targets-here [ die ]
         die
       ]
     set pcolor scale-color sky velocity 2 0
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
225
15
655
466
17
17
12.0
1
10
1
1
1
0
1
1
1
-17
17
-17
17
1
1
0
ticks

BUTTON
25
15
110
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

BUTTON
115
15
200
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

SLIDER
25
55
200
88
angle
angle
1
90
20
1
1
NIL
HORIZONTAL

SLIDER
25
95
200
128
power
power
1
100
80
1
1
NIL
HORIZONTAL

BUTTON
50
135
170
168
NIL
fire
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
25
405
200
438
wind
wind
-50
50
-17
1
1
NIL
HORIZONTAL

MONITOR
15
230
110
275
NIL
velocity
3
1
11

MONITOR
115
230
210
275
X Component
x-vel
3
1
11

MONITOR
15
182
110
227
Y Component
y-vel
3
1
11

SLIDER
25
285
200
318
wall-height
wall-height
0
30
20
1
1
NIL
HORIZONTAL

SLIDER
25
325
200
358
wall-position
wall-position
-10
10
5
1
1
NIL
HORIZONTAL

SWITCH
25
365
200
398
wall-block-wind?
wall-block-wind?
0
1
-1000

@#$#@#$#@
## WHAT IS IT?

This model is based on the classic game Tank Wars. Players must adjust angle and power of fire to hit a target with a projectile. Several factors, including gravity and wind must be taken into account.

## HOW IT WORKS

The warhead is fired and moves ballistically towards its target.

## HOW TO USE IT

The SETUP button creates a new randomly-positioned target and obstacle wall.

The GO button causes the game logic to be active and must be pressed anytime the game is in play.

The FIRE button launches the projectile with the currently specified parameters.

The WIND slider adjusts the direction and magnitude of the wind.

Properties of the obstacle wall can be configured including its height (WALL-HEIGHT slider), it's location (WALL-POSITION slider), and what effect it has on wind (WALL-BLOCKS-WIND? switch)

## THINGS TO TRY

Try to hit the target!

## NETLOGO FEATURES

This model uses the `tie` command for links, to conveniently make it so that changing the heading of the tank causes its gun to swivel and the "aiming arrow" to also swivel.

This model also fakes having no ceiling to the world, by having the shell (projectile) wrap around to the bottom of the view, but it is hidden until it comes back down again.

## RELATED MODELS

Lunar Lander

## CREDITS AND REFERENCES

Thanks to James Newell for his work on this model.
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

ball
true
0
Circle -7500403 true true 75 75 148

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

cloud
false
0
Circle -7500403 true true 44 34 85
Circle -7500403 true true 5 93 90
Circle -7500403 true true 72 87 97
Circle -7500403 true true 51 138 105
Circle -7500403 true true 114 118 94
Circle -7500403 true true 111 51 93
Circle -7500403 true true 160 72 105
Circle -7500403 true true 180 155 68
Circle -7500403 true true 161 39 68
Circle -7500403 true true 200 53 79
Circle -7500403 true true 205 125 80
Line -16777216 false 74 117 94 110
Line -16777216 false 95 111 108 120
Line -16777216 false 182 146 196 136
Line -16777216 false 196 136 222 140
Line -16777216 false 97 181 104 178
Line -16777216 false 106 176 133 180
Line -16777216 false 175 85 193 77
Line -16777216 false 193 77 207 79

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

gun
true
0
Rectangle -7500403 true true 108 13 194 293

heli
false
0
Polygon -7500403 true true 85 68 51 94 45 106 33 122 43 174 63 218 99 232 141 236 191 204 237 160 273 134 281 110 285 52 285 22 283 14 259 20 257 36 243 68 227 100 201 116 159 88 117 70
Polygon -11221820 true false 63 118 57 132 73 198 121 218 143 178 125 124 63 118
Polygon -1 true false 85 34 43 54 25 68 7 84 47 106 115 112 167 104 213 70 191 44 151 18 67 24 19 66 25 78
Polygon -1 true false 29 78 75 88
Polygon -1 true false 75 88 139 68
Rectangle -1 true false 21 60 57 70
Rectangle -1 true false 29 46 69 78
Rectangle -1 true false 61 38 83 44
Rectangle -1 true false 53 32 93 52
Rectangle -1 true false 25 68 37 74
Polygon -1 true false 67 24 31 44 11 82 45 82
Line -16777216 false 97 40 59 58
Line -16777216 false 49 70 67 84
Line -16777216 false 117 84 157 68
Line -16777216 false 129 42 147 52
Line -16777216 false 97 62 107 56
Line -16777216 false 105 66 117 60
Polygon -1 true false 271 28 251 44 243 56 247 72 267 90 287 72 291 54 291 36 271 28
Polygon -1 true false 271 82 283 80 291 70 291 56 277 50
Line -16777216 false 255 54 269 42
Line -16777216 false 271 72 281 62
Line -16777216 false 267 56 271 60
Line -16777216 false 259 62 267 66
Line -16777216 false 275 46 281 52

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

tank
false
0
Circle -7500403 true true 210 195 90
Circle -7500403 true true 0 195 90
Rectangle -7500403 true true 45 195 255 285
Circle -16777216 true false 225 255 30
Rectangle -7500403 true true 120 150 180 210
Circle -16777216 true false 180 255 30
Circle -16777216 true false 135 255 30
Circle -16777216 true false 90 255 30
Circle -16777216 true false 45 255 30
Rectangle -7500403 true true 195 225 240 225

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

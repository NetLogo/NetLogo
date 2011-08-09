globals [
  algae-height-list
  ;;; some patch agentsets to make things easier to read
  air
  water
  ground
]

patches-own [ light ]

breed [ algae an-algae ]

algae-own [ ballast ]

;;;;;;;;;;;;;;;;;;;;;;
;; SETUP PROCEDURES ;;
;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  setup-globals
  setup-environment
  setup-algae
  update-algae-height-list
  reset-ticks
end

to setup-globals
  set algae-height-list []
  set air patches with [ pxcor = 0 and pycor = max-pycor ]
  set water patches with [ pxcor = 0 and abs pycor != max-pycor ]
  set ground patches with [ pxcor = 0 and pycor = min-pycor ]
end

to setup-environment
  ask air [ set light 1 ]
  ask ground [ set light 0 ]
  ask water [ set light 0 ]
  ask patches with [ pxcor != 0 ] [
    set pcolor gray + 1.75
  ]
  recolor-environment true
end

to recolor-environment [setting-up?]
  ask air [
    if day-and-night? [
      ifelse setting-up? or daytime? [
        set pcolor yellow + 3
      ] [
        set pcolor black
      ]
    ]
  ]
  ask ground [
    set pcolor brown
  ]
  ask water [
    ifelse light > 0 [
      ;; make water with more light brighter yellow
      set pcolor yellow - 4 + light * 7
    ] [
      ;; make deeper water darker so things are prettier
      set pcolor blue - 2 - 0.1 * distance one-of air
    ]
  ]
end

to setup-algae
  set-default-shape algae "algae"
  create-algae 50 [
    set ballast 0.3 + random-float 0.4
    set heading 0
    set color one-of [red yellow lime sky]
    setxy 0 [pycor] of one-of water
    ;; spread the algae throughout the water patches
    setxy xcor - 0.2 + random-float 0.4
          ycor - 0.5 + random-float 1
  ]
end

;;;;;;;;;;;;;;;;;;;
;; GO PROCEDURES ;;
;;;;;;;;;;;;;;;;;;;

to go
  if not any? algae [ stop ]
  ;; 24 hour days
  let day ticks / 24
  ;; assume all months are the same length
  let month day / 10
  ;; day length cycles up and down based on the time of year
  if change-day-length? [
    set day-length 12 + 4 * sin ( month * 180 / 12 )
  ]
  if day-and-night? [
    spread-light
  ]
  ask algae [
    change-ballast light
  ]
  ask algae [
    move
  ]
  recolor-environment false
  update-algae-height-list
  ;; advance tick counter without plotting
  tick-advance 1
  ;; let things go for a bit before starting the plotting
  if ticks > 100 [ update-plots ]
end

to change-ballast [ light-present ] ; algae procedure
  ;; algae lose some ballast per hour (but don't get less than 0)
  set ballast max list 0 (ballast - 0.1)
  ;; the amount of new ballast depends on how much light and has some randomness
  let new-ballast light-present - random-float 0.05
  ;; ballast can't be greater than 1
  set ballast min list 1 ballast + new-ballast
end

to move ; algae procedure
  ;; if ballast is empty, amount-to-move is 1
  ;; if ballast is full, amount-to-move is -1
  ;; ballast stays between 0 (empty) and 1 (full)
  let amount-to-move (1 - (2 * ballast))
  if amount-to-move > 0 [
    ;; algae don't go into the air
    let distance-to-air (max-pycor - 0.5 - ycor)
    set amount-to-move min list distance-to-air amount-to-move
  ]
  fd amount-to-move
  ;; algae die if on the bottom
  if member? patch-here ground [ die ]
end

to spread-light
  ifelse daytime? [
    ask air [ set light 1 ]
    ask water [ set light 0 ]
    ;; we sort the water patches top to bottom and then ask them in turn
    ;; to grab some light from above
    foreach sort water [
      ask ? [
        let light-gained light-spreadiness * [light] of patch-at 0 1
        set light light + light-gained
      ]
    ]
  ] [
    ask air [ set light 0 ]
    ask water [ set light 0 ]
  ]
end

to-report daytime?
  report ticks mod 24 < day-length
end

to update-algae-height-list
  let current-algae-height mean [ycor] of algae
  set algae-height-list fput current-algae-height algae-height-list
  if length algae-height-list > 24
    [ set algae-height-list sublist algae-height-list 0 24 ]
end
@#$#@#$#@
GRAPHICS-WINDOW
309
10
589
671
4
10
30.0
1
8
1
1
1
0
0
0
1
-4
4
-10
10
1
1
1
ticks
30.0

BUTTON
77
62
141
95
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
101
299
251
average height
NIL
NIL
0.0
10.0
2.5
4.0
true
false
"" ""
PENS
"average height" 1.0 0 -16777216 true "" "plot mean algae-height-list"

BUTTON
146
62
209
95
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

PLOT
11
257
299
407
day length
NIL
NIL
0.0
10.0
8.0
16.0
true
false
"" ""
PENS
"day length" 1.0 0 -16777216 true "" "plot day-length"

SLIDER
9
22
177
55
light-spreadiness
light-spreadiness
0.5
0.85
0.75
0.05
1
NIL
HORIZONTAL

PLOT
12
550
300
670
algae distribution
NIL
NIL
-10.0
10.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" "histogram [ ycor ] of algae"

SLIDER
14
500
197
533
day-length
day-length
8
16
12.143958568001993
0.1
1
hours
HORIZONTAL

SWITCH
15
457
198
490
change-day-length?
change-day-length?
0
1
-1000

SWITCH
14
414
197
447
day-and-night?
day-and-night?
0
1
-1000

TEXTBOX
205
504
298
531
light hours per 24
11
0.0
0

TEXTBOX
207
410
306
457
change light throughout the day
11
0.0
0

TEXTBOX
205
455
305
497
change duration of light from day to day
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

This is a model of a simplified aquatic ecosystem consisting of a column of water containing algae, light, and nutrients.  There is more light at the top but more food at the bottom, so algae move up and down to balance their needs.

## HOW IT WORKS

Algae need both light and nutrients (such as ammonia and phosphate) in order to grow. Although there is ample light near the lake surface, there are few nutrients.  On the other hand, it is dark deep in the lake, but there is a lot of ammonia and phosphate down there. Some algae can move up and down in the water column to get both light and nutrients. These algae make dense substances using the energy from light in order to make them sink, and then burn up those dense substances in the dark at the deeper parts of the water column.

The algae move up and down in the water column based on how dense they are. The algae's density is something that changes continuously based on how much light they are able to get during the day. Density decreases some every turn because the algae use the dense substances to produce chemical energy. However, the algae can also use light energy to make the dense substances. The more light present, the denser they get. When they become denser than water, they move down. Deeper water gets less light, so the deeper algae goes, the less density it is able to add. When algae becomes less dense than water, it moves up towards the surface again. Also, as the seasons change, the length of the day changes, which changes how much light is present throughout the day.

## HOW TO USE IT

The SETUP button prepares the ecosystem and the GO button runs the simulation.

The LIGHT-SPREADINESS slider changes how much the light penetrates the water. The higher the "spreadiness," the more light can reach down into the water. If the DAY-AND-NIGHT? switch is on, the DAY-LENGTH slider changes how much of each day has sunlight. If it is off, light does not change throughout the day. If the CHANGE-DAY-LENGTH? switch is on, DAY-LENGTH changes as GO is running, simulating the changes of the seasons. If it is off, DAY-LENGTH will only change manually.

The AVERAGE-HEIGHT plot shows a history of the average height of the algae over the past 24 hour period. The DAY-LENGTH plot show the history of the DAY-LENGTH slider. The ALGAE-DISTRIBUTION plot shows the current position of the algae. Lower algae appear farther to the right in the plot.

## THINGS TO NOTICE

Notice how as the days get longer, the average height of the algae gets lower. Similarly, as the days get shorter, the average height of the algae gets higher. Also, the more the light penetrates the water, the lower the algae are on average.

The color of the patches indicate the light intensity at that depth in the water column --- the more yellow, the more light is present.

Sometimes the algae clumps all together and sometimes the algae forms multiple clumps.

## THINGS TO TRY

To get time to pass faster, use the speed slider.

Turn off DAY-AND-NIGHT? to see that the algae still go up and down when the sun is always shining.

How does having lots of LIGHT-SPREADINESS and a small DAY-LENGTH compare to having little LIGHT-SPREADINESS and a big DAY-LENGTH? (It'll help to turn off CHANGE-DAY-LENGTH?.)

## EXTENDING THE MODEL

Even though algae need both light and nutrients, this simple model only considers the role of light. Add a nutrient gradient in the water column. The floor could contain nutrients and the algae might contribute to the gradient when they die.

When real algae are healthy, they grow new algae. Also, when they run out of food, they die. Add these rules to the model.

## NETLOGO FEATURES

In the `spread-light` procedure, the water patches grab light from the patch directly above them. Note the use of the `foreach` and `sort` primitives to ensure that the patches are asked in the desired order.

In order to display all of the elements of the control strip above it, the 2D View needs to be of a certain minimum width, so world-width is larger than it needs to be. The necessary patches are stored as patch agentsets in the `air`, `water`, and `ground` global variables and the `setup-environment` procedure "gets rid" of the unnecessary patches:

    ask patches with [pxcor != 0] [
      set pcolor gray + 1.75
    ]


## CREDITS AND REFERENCES

This model is based on a preliminary model developed by Allan Konopka at the 2004 NetLogo workshop at Northwestern University.

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

algae
true
0
Line -7500403 true 45 198 238 90

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
NetLogo 5.0beta5
@#$#@#$#@
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

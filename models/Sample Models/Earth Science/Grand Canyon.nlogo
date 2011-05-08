breed [waters water]
breed [raindrops raindrop]

patches-own [
  elevation
]

globals [
  color-min
  color-max
  old-show-water?
  water-height    ;; how many feet tall one unit of water is
  border          ;; keep the patches around the edge in a global
                  ;; so we don't ever have to ask patches in go
]

;;
;; Setup Procedures
;;

;; reading the external file is startup rather
;; than setup so we only do it once in the model
;; running the model does not change the elevations

to startup
  ;; read the elevations from an external file
  ;; note that the file is formatted as a list
  ;; so we only have to read once into a local variable.
  file-open "Grand Canyon data.txt"
  let patch-elevations file-read
  file-close
  set color-max max patch-elevations + 200 ;; put a little padding on the upper bound so we don't get too much
                                           ;; white and higher elevations have a little more variation.
  let min-elevation min patch-elevations
  ;; adjust the color-min a little so patches don't end up black
  set color-min min-elevation - ((color-max - min-elevation) / 10)
  ;; transfer the date from the file into the sorted patches
  ( foreach sort patches patch-elevations
    [ ask ?1 [ set elevation ?2 ] ] )
  set-default-shape turtles "circle"
  setup
end

;; just clean up the marks that the raindrops have made
;; and set some global variable to defaults
to setup
  clear-drawing
  clear-turtles
  ask patches
    [ set pcolor scale-color brown elevation color-min color-max ]
  set water-height 10
  set border patches with [ count neighbors != 8 ]
  reset-ticks
end

;;
;; Runtime Procedures
;;
to go
  ;; check for mouse clicks on empty patches.
  ;; if we've got a winner make a manual raindrop that
  ;; is red.
  if mouse-down? and not any? turtles-on patch mouse-xcor mouse-ycor
  [
    ;; even when raindrops are hidden
    ;; newly created manual drops will
    ;; be visible
    create-raindrops 1
    [ setxy mouse-xcor mouse-ycor
      set size 2
      set color red
    ]
  ]
  ;; make rain-rate drops randomly
  create-raindrops rain-rate
  [ move-to one-of patches
    set size 2
    set color blue ]

  ifelse draw?
    [ ask turtles [ pd ] ]
    [ ask turtles [ pu ] ]

  ask raindrops [ flow ]

  ask border
  [
    ;; when raindrops reach the edge of the world
    ;; kill them so they exit the system and we
    ;; don't get pooling at the edges
    ask turtles-here [ die ]
  ]
  tick
end

to flow ;; turtle procedure
  ;; get the lowest neighboring patch taking into account
  ;; how much water is on each patch.
  let target min-one-of neighbors [ elevation + (count turtles-here * water-height) ]
  ;; if the elevation + water on the neighboring patch is
  ;; lower than here move to that patch.
  ifelse [elevation + (count turtles-here * water-height)] of target
     < (elevation + (count turtles-here * water-height))
    [ move-to target ]
    [ set breed waters ]
end
@#$#@#$#@
GRAPHICS-WINDOW
216
10
828
643
150
150
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
-150
150
-150
150
1
1
1
ticks
30

BUTTON
113
36
189
69
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
30
36
105
69
NIL
setup\n
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
15
92
205
125
rain-rate
rain-rate
0
10
5
1
1
drops / tick
HORIZONTAL

TEXTBOX
36
138
190
170
Click on the map to place raindrops manually
11
0.0
0

BUTTON
15
341
192
374
watch random raindrop
watch one-of raindrops
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
16
409
191
442
reset-perspective
rp\n
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
16
375
192
408
watch my raindrop
if any? turtles with [ color = red ]\n[\n  watch one-of turtles with [ color = red ]\n]
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

SWITCH
54
211
157
244
draw?
draw?
1
1
-1000

BUTTON
48
250
162
283
clear drawing
clear-drawing
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

This model simulates rainfall on a patch of terrain on the eastern end of the Grand Canyon, approximately 6 miles (9.7 km) on each side, where Crazy Jug Canyon and Saddle Canyon meet to form Tapeats Canyon. Each patch represents an area approximately 105 feet (32 m) on each side. The model was created as an experiment in using NetLogo with My World GIS.

The elevation data comes from the National Elevation Dataset available at http://seamless.usgs.gov. It was converted from an ESRI Grid into an ASCII grid file using ArcGIS, then resampled to its current resolution and rescaled to lie in the range 0-999 using My World GIS.

## HOW IT WORKS

Raindrops fall in random locations or at locations selected by the user, then flow downhill.  If no nearby patch has a lower elevation, the raindrop stays where it is.  Raindrops pool until they flow over the land nearby. Some raindrops may always stay in these pools at higher ground. Others will flow out of the system at the edges.

## HOW TO USE IT

When you open the model, the STARTUP procedure automatically runs and imports the data from an external file.  Press SETUP to color the patches according to their elevation, and to remove raindrops and drawings from previous runs. Press the GO button to start the simulation.  With each tick, RAIN-RATE raindrops will fall at random locations, traveling downhill across the landscape.

As the simulation runs, you may click anywhere on the map to create raindrops. Manually placed raindrops are red, while those created randomly by the model are blue.  The WATCH RANDOM RAINDROP button sets the perspective to watch a randomly selected raindrop (of any type). The WATCH MY RAINDROP button watches a red raindrop, if one exists.

When the DRAW? switch is turned on each raindrop marks its path in the drawing layer.

## THINGS TO NOTICE

Elevations are represented by lighter and darker colors. The higher the elevation, the lighter the color used to draw that patch. Raindrops flow from high to low elevations, meaning that they flow toward darker patches.

When you let the model run for a long time, you will see pools start to form at certain locations where a bit of low land is surrounded by higher land.  If you let the model run long enough, the water will eventually overflow from these dips, flowing to the rivers below.

## THINGS TO TRY

Put the turtle pens down (by turning on the DRAW? switch), and see the kinds of patterns that emerge.

Try to place all of the raindrops manually.  Trace the path of one drop all the way down the landscape.

Find more GIS data and import different data sets.

## EXTENDING THE MODEL

Add erosion to the model, so the raindrops pick up or deposit some amount of elevation from the patches they travel over.

## NETLOGO FEATURES

When there is no lower neighboring patch, raindrops change breed (from raindrop to waters) so they will no longer move.

Elevation data is read only once, when the model is loaded, in the `startup` procedure.  The external data file (Grand Canyon data.txt) is formatted such that its contents can be assigned (with `file-read`) to a NetLogo variable.

## RELATED MODELS

Erosion

## CREDITS AND REFERENCES

National Elevation Dataset: http://seamless.usgs.gov
ArcGIS: http://esri.com/software/arcgis/
My World GIS: http://myworldgis.org/myworld

Thanks to Eric Russell for his work on this model.
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
startup
set draw? true
repeat 200 [ go ]
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

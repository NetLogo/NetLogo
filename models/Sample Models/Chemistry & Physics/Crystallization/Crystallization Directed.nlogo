turtles-own [
  temp                 ;; this turtle's temperature
  neighboring-turtles  ;; agentset of surrounding turtles
  sides-exposed        ;; number of sides exposed to cooling walls  (between 0 and 4)
]

globals [
  ave-metal-temp   ;; shows average temperature of all metal
  num-frozen       ;; keeps track of how many atoms are frozen
  temp-range       ;; for histogram
  colors           ;; used both to color turtles, and for histogram
  pens             ;; keeps track of all the histogram's pen names
]

to setup
  clear-all
  set colors sentence (white - 1) [cyan sky blue violet magenta red]
  set pens []
  set temp-range (init-metal-temp - melting-temp) / (length colors - 1)
  let ymax (height + 1) / 2
  let xmax (width + 1) / 2
  ;; create turtles everywhere inside the given box range
  ask patches [
    if ((abs pycor) < ymax) and ((abs pxcor) < xmax)
    [
      sprout 1
      [
        set shape "T"
        set temp init-metal-temp
        set-color
      ]
    ]
  ]
  ;; set sides of box for cooling
  ask patches [
    if      (heat-top? and (pycor = ymax) and (abs pxcor <= xmax)) or
      (heat-left? and (pxcor = (- xmax)) and (abs pycor <= ymax)) or
          (heat-right? and (pxcor = xmax) and (abs pycor <= ymax)) or
    (heat-bottom? and (pycor = (- ymax)) and (abs pxcor <= xmax))
    [ set pcolor 16 ]
  ]
  ask turtles [
    set neighboring-turtles (turtles at-points [[-1  1] [ 0  1] [1  1]
                                                [-1  0] [ 0  0] [1  0]
                                                [-1 -1] [ 0 -1] [1 -1]])
    set sides-exposed (count patches at-points [[-1  1] [ 0  1] [1  1]
                                                [-1  0] [ 0  0] [1  0]
                                                [-1 -1] [ 0 -1] [1 -1]]
                       with [(not any? turtles-here) and (pcolor = black)])
  ]
  set ave-metal-temp init-metal-temp
  reset-ticks
end


to go
  ;; stop if all turtles are below melting temp
  if (max ([temp] of turtles) < melting-temp) [ stop ]
  ;; otherwise...
  set num-frozen 0
  ask turtles [ cool-turtles ]
  ask turtles [ set-color ]
  ask turtles [ rotate ]
  set ave-metal-temp (mean [temp] of turtles)
  tick
end


;; turtle procedure -- if metal is liquid and it is next to a solid,
;; change its heading to that of the solid; otherwise, just rotate
;; randomly
to rotate
  if (temp >= melting-temp) [
    let frozen-neighbors (neighboring-turtles with [temp <= melting-temp])
    ifelse (any? frozen-neighbors)
      [ set heading ([heading] of (one-of frozen-neighbors)) ]
      [ rt random-float 360 ]
  ]
end


;; turtle procedure -- sets turtle's temp to ave temp of all
;; neighboring turtles and patches added turtle's own temp in twice so
;; it changes more slowly
to cool-turtles
  let total-temp ((sum [temp] of neighboring-turtles) +
                  (room-temp * sides-exposed) + temp)
  set temp (total-temp / (count neighboring-turtles + sides-exposed + 1))
end

;; turtle procedure
to set-color
  ; create index ranging from 1 to 8 for all melting colors
  let index (floor ((temp - melting-temp) / temp-range)) + 1
  ifelse (index < 1 ) [
    set color white - 1
    set num-frozen (num-frozen + 1)
  ]
  [
    if index >= length colors
      [ set index (length colors) - 1 ]
    set color item index colors
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
383
10
789
437
16
16
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
-16
16
-16
16
1
1
1
ticks
30.0

BUTTON
263
72
331
108
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
221
24
287
61
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
9
27
172
60
room-temp
room-temp
-20
100
20
1
1
NIL
HORIZONTAL

SLIDER
9
66
172
99
init-metal-temp
init-metal-temp
1550
2500
1550
10
1
NIL
HORIZONTAL

SLIDER
9
104
172
137
melting-temp
melting-temp
500
1500
500
1
1
NIL
HORIZONTAL

MONITOR
180
401
282
446
ave-metal-temp
ave-metal-temp
3
1
11

SLIDER
13
152
126
185
width
width
1
31
31
2
1
atoms
HORIZONTAL

SLIDER
13
189
126
222
height
height
1
31
31
2
1
atoms
HORIZONTAL

PLOT
4
443
279
644
Average Metal Temperature
time
ave-metal-temp
0.0
100.0
20.0
1550.0
true
true
"set-plot-y-range room-temp init-metal-temp" ""
PENS
"ave-metal-temp" 1.0 0 -2674135 false "" "plot ave-metal-temp"

PLOT
282
443
495
644
Number Solidified
time
crystal quant.
0.0
100.0
0.0
625.0
true
false
"set-plot-y-range 0 count turtles" ""
PENS
"amount" 1.0 0 -16777216 false "" "plot num-frozen"

PLOT
501
441
749
642
Temperatures
colors
quantity
0.0
8.0
0.0
625.0
false
true
"set-plot-y-range 0 count turtles\nset-histogram-num-bars 1 + (length colors)\n\nlet bottom (round room-temp)\nlet top (round melting-temp)\nlet index 0\nforeach colors [\n  create-temporary-plot-pen (word bottom \" - \" top)\n  set-plot-pen-mode 1\n  set-plot-pen-color ?\n  set pens lput (word bottom \" - \" top) pens\n\n  set index index + 1\n  set bottom top\n  set top (round ((index * temp-range) + melting-temp))\n]" "if histogram? [\n  let index 0\n  foreach colors [\n    set-current-plot-pen (item index pens)\n    plot-pen-reset\n    if any? turtles with [color = ?]\n    [ plotxy index count turtles with [color = ?] ]\n    set index index + 1\n  ]\n]\n"
PENS
"quantity" 1.0 1 -2674135 false "" ""

SWITCH
208
248
340
281
histogram?
histogram?
0
1
-1000

BUTTON
183
72
250
107
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

SWITCH
183
123
325
156
heat-top?
heat-top?
1
1
-1000

SWITCH
180
191
322
224
heat-bottom?
heat-bottom?
1
1
-1000

SWITCH
248
157
372
190
heat-right?
heat-right?
1
1
-1000

SWITCH
133
157
247
190
heat-left?
heat-left?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

Some metal applications require that a metal's grains form specific patterns. To achieve this, the metal is only cooled from certain sides.  This can be done in several ways.  For example, when molds are made of metal, wires are used to heat up the mold where they want the metal to crystallize last.  For molds made of sand, pieces of metal are put where the liquid metal should crystallize first.  The inserted metal (known as a heat sink), works to quickly suck the heat out of the liquid metal.

## HOW IT WORKS

This model represents the cross-section of a block of liquid metal cooling in a metal mold.  The surrounding mold quickly sucks the heat out of the liquid metal.  However, you can select some of the sides to be heated, which prevents heat from escaping out of those sides.   By selecting which sides of the metal are heated, you can control the shapes of the resulting grains.  (Note that the actual number of atoms is small compared to a real metal sample and the metal is only two-dimensional.)

The information below assumes you have already read and understood the original Crystallization Basic model.

## HOW TO USE IT

### Buttons

SETUP: Resets the simulation, and sets the metal to the correct size.  
GO-ONCE: Runs the simulation for one time step.  
GO: Runs the simulation continuously until either the GO button is pressed again, or all of the atoms are frozen.

### Sliders

WIDTH: How many atoms wide the metal is.  
HEIGHT: How many atoms high the metal is.  
ROOM-TEMP: Varies the temperature of the room.  
INIT-METAL-TEMP: Varies the initial temperature of the metal.  
MELTING-TEMP: Varies the temperature at which the metal solidifies.

### Monitors

AVE-METAL-TEMP: Monitors the average temperature of all the atoms.  
TIME: Keeps track of the time that has elapsed during each run.

### Switches

HEAT-TOP?: Prevents the top side of the metal from starting to cool.  
HEAT-LEFT?: Prevents the left side of the metal from starting to cool.  
HEAT-RIGHT?: Prevents the right side of the metal from starting to cool.  
HEAT-BOTTOM?: Prevents the bottom side of the metal from starting to cool.  
HISTOGRAM?: Turns the histogram plotting on and off.  Turning off the histogram speeds up the model.

### Graphs

AVERAGE METAL TEMPERATURE: Plots the average temperature of all the metal over time.  
NUMBER SOLIDIFIED: Plots how many metal atoms are below the melting temperature over time.  
TEMPERATURES:  Histograms how many atoms are in each temperature range.  (Note that the colors of the histogram match the actual colors of the atoms.)

## THINGS TO TRY

Set HEAT-TOP? and HEAT-RIGHT? to On and HEAT-BOTTOM? and HEAT-LEFT? to Off. Predict which atom will be the last to solidify.  Now run the model and see if your prediction was correct.

A contractor asks you to create a piece of metal for her that only has horizontal grain boundaries, and no vertical ones.  Find settings for HEAT-TOP?, HEAT-RIGHT?, HEAT-BOTTOM?, and HEAT-LEFT? that result in approximately these sorts of grain boundaries.

Set HEAT-TOP? and HEAT-BOTTOM? to Off and HEAT-LEFT? and HEAT-RIGHT to On. Run the model to completion.  How do these settings affect the grain boundaries? How would these grain boundaries affect the properties of the metal?

## EXTENDING THE MODEL

In this model, heating a side simply results in the room temperature having no affect on that side.  However, in real applications, heated sides still cool the metal, but at a slower rate.  Change the cool-turtles procedure so heated sides factor into how atoms are cooled.

As mentioned in the above, molds made of sand use inserted pieces of metal to control which side of the metal should crystallize first.  The inserted metal (known as a heat sink), works to quickly suck the heat out of the liquid metal.  Change the model so instead of just having heated sides, there are also cooled sides that cause a faster cooling rate.

## NETLOGO FEATURES

In the setup procedure, a turtle is created on every patch within the requested dimensions.  This is achieved by asking every patch satisfying certain conditions to `sprout 1`.

Note how we can draw a multi-colored histogram.  The `histogram` primitive can only draw in one color at a time, but we work around this by calling it over and over again, plotting only one bar each time, changing the pen color each time.

## RELATED MODELS

Crystallization Basic  
Crystallization Moving

## CREDITS AND REFERENCES

Original implementation: Carrie Hobbs, for the Center for Connected Learning and Computer-Based Modeling.
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

t
true
0
Rectangle -7500403 true true 46 47 256 75
Rectangle -7500403 true true 135 76 167 297

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
NetLogo 5.0RC2
@#$#@#$#@
setup
repeat 37 [ go ]
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

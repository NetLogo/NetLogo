patches-own [ x y rod? ]

globals
[ power
  old-power
  old-power-2  ; Used to compute average-power
  old-power-3  ; Used to compute average-power
  old-power-4  ; Used to compute average-power
  average-power
  power-change
  rod-length
  n-rods   ; Number of rods
  r ; Constant for half the reactor size
]


to setup
  clear-all
  set-default-shape turtles "circle"
  setup-globals
  ask patches
  [ set x (abs pxcor)
    set y (abs pycor)
    set rod? false
    build-reactor
    setup-nuclear-fuel
  ]
  setup-control-rods
  reset-ticks
end

to setup-globals
  set power   0
  set old-power  0
  set old-power-2  0
  set old-power-3  0
  set old-power-4  0
  set r (reactor-size / 2)
  set rod-length rod-depth
  set n-rods (reactor-size / (rod-spacing + 1)) - 1
end

to build-reactor ;; Patch Procedure
  if ((x = r) and (y <= r)) or ((y = r) and (x <= r))
  [ set pcolor gray
    set rod? false
  ]
end

to setup-nuclear-fuel ;; Patch Procedure
  if (pcolor = black) and (x < r) and (y < r)
  [ set pcolor red ]
end

to setup-control-rods
  if rod-depth > reactor-size [set rod-depth reactor-size]
  if (rod-spacing = 5 or rod-spacing = 6 and reactor-size = 10)
  [ user-message "Spacing too large for reactor size.  Spacing set to 4."
    set rod-spacing 4
    set n-rods 1
  ]
  let rod-x 1 - r + rod-spacing

  ;; Make the rods more evenly spaced at particular settings
  if (rod-spacing = 2 and reactor-size != 30 and reactor-size != 60)
  [ set rod-x rod-x + 1 ]
  if (rod-spacing = 3 and (reactor-size mod 20) != 0)
  [ set n-rods n-rods + 1
    set rod-x rod-x - 1
  ]
  if (rod-spacing = 5 and (reactor-size = 20 or reactor-size = 40 or reactor-size = 70))
  [ ifelse (reactor-size = 20)
    [ set rod-x rod-x + 1 ]
    [ set rod-x rod-x + 2 ]
  ]
  if (rod-spacing = 6 and (reactor-size mod 20) = 0)
  [ set n-rods n-rods + 1
    ifelse (reactor-size = 80)
    [ set rod-x rod-x - 2 ]
    [ set rod-x rod-x - 1 ]
  ]

  repeat n-rods
  [ ask patches with [ pxcor = rod-x ]
    [ set rod? true ]
    set rod-x rod-x + rod-spacing + 1
  ]
  ask patches [ build-reactor ]
  place-control-rods
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Run Time Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;Forever Button
to auto-react
  if not any? turtles
    [stop]
  ifelse power-change >= 0
  [ if (power - power-rated) >= 0
    [ set rod-length ( rod-length + 50 ) ]
  ]
  [ if (power - power-rated) < 0
    [ set rod-length ( rod-length - 10 ) ]
  ]
  if rod-length < 0
  [ set rod-length 0 ]
  if rod-length > reactor-size
  [ set rod-length reactor-size ]
  react
end

;;Forever Button
to manu-react
  if not any? turtles
    [stop]
  if rod-depth > reactor-size [set rod-depth reactor-size]
  set rod-length rod-depth
  react
end

to react
  place-control-rods
  set power 0
  ask turtles
  [ fd  1
    if (pcolor = gray)
    [ die ]
    if (pcolor = red)
    [ fission ]
  ]
  set average-power ((power + old-power + old-power-2 + old-power-3 + old-power-4) / 5)
  set power-change (power - old-power)
  set old-power-4 old-power-3
  set old-power-3 old-power-2
  set old-power-2 old-power
  set old-power power
  tick
end

to release-neutron ;; Button
  let whom nobody
  crt 1
  [ set color yellow
    set xcor ((random (reactor-size - 2)) - r)
    set ycor ((random (reactor-size - 2)) - r)
    set whom self
    if (pcolor = gray)
    [ die ]
  ]
  if whom = nobody
  [ release-neutron ]
end

to place-control-rods
  ask patches with [ rod? ]
  [ ifelse (pycor >= (r - rod-length))
    [ set pcolor  gray ]
    [ set pcolor black ]
  ]
end

to fission ;; Turtle Procedure
  rt random 360
  if (pcolor = red)
  [ if (spend-fuel?)
    [ set pcolor brown ]
    let gain (1 / count turtles-here)
    set power power + gain
    hatch ((2 + random 2) * gain)
      [ rt random 360 ]
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
273
10
706
464
70
70
3.0
1
10
1
1
1
0
0
0
1
-70
70
-70
70
1
1
1
ticks
15.0

SLIDER
7
138
157
171
power-rated
power-rated
0.0
100.0
35
1.0
1
NIL
HORIZONTAL

SLIDER
7
39
157
72
reactor-size
reactor-size
10
136
122
2
1
NIL
HORIZONTAL

BUTTON
7
261
89
294
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
181
261
267
294
manual
manu-react
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
89
261
182
294
automatic
auto-react
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
165
39
266
84
Power
power
3
1
11

MONITOR
165
89
266
134
Power change
power-change
3
1
11

PLOT
7
299
267
464
Power
time
power
0.0
250.0
0.0
105.0
true
false
"set-plot-y-range 0 (3 * power-rated)" ""
PENS
"power-rated" 1.0 0 -13345367 true "" "plot power-rated"
"avg-power" 1.0 0 -2674135 true "" "plot average-power"

SLIDER
7
179
157
212
rod-depth
rod-depth
0
80
0
1
1
NIL
HORIZONTAL

BUTTON
104
221
267
254
NIL
release-neutron
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
7
72
157
105
rod-spacing
rod-spacing
1
6
4
1
1
NIL
HORIZONTAL

SWITCH
7
105
157
138
spend-fuel?
spend-fuel?
0
1
-1000

@#$#@#$#@
## WHAT IS IT?

This project simulates a nuclear fission reaction in a nuclear power plant. In a fission reaction, free neutrons hit uranium atoms, causing each uranium atom to generate 2 or 3 neutrons and a unit of energy. The uranium atom itself splits into two smaller atoms. The newly generated neutrons, together with the neutron that caused the reaction, keep moving and continue to hit more uranium atoms, which release more neutrons, etc.  This is the chain reaction that happens inside an atomic bomb.

Most nuclear energy is used for peaceful purpose, however. Generated in a nuclear power plant, nuclear fission goes on in a much more controlled fashion. Heavy metal plates made of lead help to absorb the free neutrons, thus fewer atoms of uranium are smashed, which in turn limits how much energy is released.

This model simulates the process of a nuclear fission reaction inside such a plant. The reactor core is built of concrete, with adjustable control rods to help control the speed of the reaction. The reactor has a built-in automatic controller, and has a set of manual controls as well.

## HOW TO USE IT

The REACTOR-SIZE slider controls the size of the reactor.  

The ROD-SPACING slider controls the distance between the control rods.  

The SPEND-FUEL switch controls whether or not the fuel is used up when a neutron hits it.  

The POWER-RATED slider controls the rated power, which is used for automatic control.  

The ROD-DEPTH slider controls the rod depth when under manual control.  

The RELEASE NEUTRON button introduces a neutron into the reactor. It can be used at anytime before or during the reaction.

Press the SETUP button to set up the reactor.

When MANUAL is on, use the ROD-DEPTH slider to control the rod depth. If they are too short, too many neutrons can escape and the reactor will generate too much power. If they are too long, all free neutrons will be absorbed, and no power will be generated.

When AUTO-REACT is on, the reactor will adjust itself as needed to keep power production under control.

The POWER monitor shows the current power output.  
The POWER-CHANGE monitor shows the change in power over the last clock tick.

Look at the POWER-PLOT plot to see the power curve.

## THINGS TO NOTICE

Observe the fluctuation of the power curve. The blue line is the rated-power line and the red line is the actually generated power line.

## THINGS TO TRY

There are two procedures, one for manual control, one for automatic control. (Respectively called 'manu-react' and 'auto-controller'.)

The manual controls are very simple.  The ROD-DEPTH slider controls how deep the control rods are inserted into the reactor.

The automatic controller is basically an ON and OFF controller. It is based on 1) comparison of power generated and power rated and 2) the power change. The mechanism is described in the following table:

                         Power > Power Rated   Power < Power Rated
                         -------------------   -------------------
      Power Change > 0:    Increase Length         Do Nothing
    
      Power Change < 0:      Do nothing          Decrease Length

The controller should be fine-tuned, and the length to increase and decrease varies.

* In Manual Mode:  
Use the slider to adjust the control rod length according to the information given through the two monitors and the plot. You want the power curve to be as flat as possible. The height of the curve should be close to the horizontal line representing the power-rate.

* In Automatic Mode:  
Fine tune the controller by changing the mechanisms to get a flat power curve with the least fluctuation.

## EXTENDING THE MODEL

The automatic controller used is just an on and off controller. Try using  Proportional, Integral and Differential (PID) controllers to make the reaction more smooth. You could also use some ideas of fuzzy logic (e.g. fuzzy membership) to build a fuzzy controller.

Absorbing free neutrons to control the reaction process is only one of the control mechanisms and is not the most important one. The important factor in controlling the reaction is to keep the speed of a neutron lower than a certain level (to stimulate fission reaction). The speeds of free neutrons depend on the density of the steam. Higher density yields lower speed and vice-versa. This is by nature a negative feedback control mechanism. It works as follows. If generated power is high, then the steam temperature is high and the density of the steam is low, thus the speed of free neutrons is high and is less likely to stimulate fission reaction. Hence a lower generated power. Try to incorporate this process into the automatic controller.

## RELATED MODELS

Together with the Reactor Top Down model you can get a decent representation of how a Nuclear Reactor might work in three dimensions.  These two models are variations of one another based off of the Fission model from StarLogoT.

## NETLOGO FEATURES

More so than most other NetLogo models, the plot of 'Power' plays an active role in the simulation. When using the manual controller, it is quite helpful to watch the power curve, to tell when things are getting out of hand.

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
NetLogo 5.0beta3
@#$#@#$#@
setup
release-neutron
repeat 45 [ auto-react ]
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

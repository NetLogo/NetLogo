turtles-own [ new? ]

globals [
  temperature
  temperatures
]

to setup
  clear-all
  set-default-shape turtles "circle"
  set temperature initial-temp
  set temperatures n-values 10 [initial-temp]
  ask patches
  [
    ;; create the thermometer
    if (((abs pxcor) < 5) and
        (pycor < (max-pxcor - 15)) and
        (pycor > (max-pxcor - 25)))
    [ set pcolor green ]
    ;; create the sides of room
    if (((abs pycor) <= (max-pxcor - 7)) and
        ((abs pxcor) =  (max-pxcor - 7)))
    [ set pcolor yellow ]
    ;; create the front and back of room
    if (((abs pycor) =  (max-pxcor - 7)) and
        ((abs pxcor) <= (max-pxcor - 7)))
    [ set pcolor yellow ]
  ]
  ;; create enough turtles to fill room to init-temp temperature
  crt (round (initial-temp * (((world-width - 16) * (world-width - 16)) / 81)))
  [
    set color red
    fd (random-float (max-pxcor - 8))
  ]
  reset-ticks
end

to go
  ask turtles
  [ circulate-heat ]
  take-temperature
  thermo-control  ;; the control for the thermostat
  tick
end

to thermo-control
  ifelse (temperature < goal-temp )
  [
    run-heater
    ask patches  ;; show the heater
    [
      if (((distancexy 0 0) <= 2))
      [ set pcolor  white ]
    ]
  ]
  [
    ask patches
    [
      if (((distancexy 0 0) <= 2))  ;; hide the heater
      [ set pcolor black ]
    ]
  ]
end

to run-heater
  crt heater-strength
  [
    if (new? = 0)
    [ set new? 1 ]
    set color red
  ]
end

to circulate-heat  ;; turtle procedure
  if (pcolor = yellow)
  [
    ;; to be reflected back into the room, turtles choose a random point
    ;; inside and set their heading in that direction. This diffuses the
    ;; heat evenly around the room.
    if ((random-float insulation) > 1)
    [
      facexy ((random-float (world-width - 13)) - (max-pxcor - 6))
                            ((random-float (world-width - 14)) - (max-pxcor - 6))
    ]
  ]
  fd 1
  if not can-move? 1
  [ die ]
end

to take-temperature
  set temperatures but-last fput count turtles with [ pcolor = green ] temperatures
  set temperature mean temperatures
end
@#$#@#$#@
GRAPHICS-WINDOW
309
11
816
539
35
35
7.0
1
10
1
1
1
0
0
0
1
-35
35
-35
35
1
1
1
ticks
30.0

BUTTON
151
30
214
63
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
85
30
145
63
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
7
78
143
111
initial-temp
initial-temp
0.0
99.0
53
1.0
1
NIL
HORIZONTAL

SLIDER
152
78
288
111
goal-temp
goal-temp
0.0
99.0
67
1.0
1
NIL
HORIZONTAL

SLIDER
8
122
144
155
heater-strength
heater-strength
1
10
4
1
1
NIL
HORIZONTAL

SLIDER
152
122
288
155
insulation
insulation
0.0
100.0
50
1.0
1
NIL
HORIZONTAL

MONITOR
107
168
188
213
temperature
temperature
1
1
11

PLOT
10
228
301
468
Temperature
Time
Temp
0.0
300.0
0.0
134.0
true
false
"set-plot-y-range 0 (goal-temp * 2)" ""
PENS
"temperature" 1.0 0 -2674135 true "" "plot temperature"
"goal-temp" 1.0 0 -10899396 true "" "plot goal-temp"

@#$#@#$#@
## WHAT IS IT?

A thermostat is a device that responds to the temperature of a room in order to maintain the temperature at some desired level.  This is often used as an example of feedback control, where a system adjusts its behavior in response the effects of its prior behavior.

Generally speaking, heating systems have only two settings - on and off - and it is the job of the thermostat to turn the heater on and off at the appropriate times. A simple thermostat does this by switching the heater on when the temperature of the room has fallen below the set desired temperature, and switching the heater off once the desired temperature has been reached or exceeded.

## HOW IT WORKS

In this model, the red turtles represent heat, and the yellow border demarcates the room whose temperature is being regulated. The yellow border is semi-permeable, allowing some of the heat that hits it to escape from the room. This heat disappears from the model once it reaches the edge of the world. A thermometer, indicated by the green square, measures the approximate temperature of the room (effectively, the density of red turtles). The heater is located in the center of the room, represented by a white patch.

It should be noted that use of turtles in this model to represent heat is not intended to be physically realistic. Instead, it is an example where a model is simplified in such a way so as to make another feature of the model more salient. In this case, it is the regulating function of the thermostat that we are primarily concerned with.

## HOW TO USE IT

GO: Starts and stops the simulation.  
SETUP: Resets the simulation, and sets the initial temperature according to init-temp.  
TEMPERATURE: Monitors the temperature in the room, as detected by the green box near the top.  
GOAL-TEMP: The thermostat aims to maintain the room at this temperature. It may be adjusted in the middle of a simulation.  
HEATER-STRENGTH: The number of red turtles created by the heater in a tick (if the heater is 'on').  
INSULATION: The efficiency of the room's insulation, or the rate at which heat escapes from the room. Higher numbers allow less heat to escape; lower number numbers allow more. This may be adjusted during a simulation.  
INITIAL-TEMP: The initial temperature of the room. This takes effect only when the SETUP button is pressed.

There is also a plot, which tracks the temperature over time (in red) and the desired temperature (in green).

## THINGS TO NOTICE

With some settings, the room cannot be heated to the desired temperature (for example, the room attains a maximum temperature that is lower than the desired temperature). Under what circumstances does this happen?

Look at the plot: does the thermostat do a good job of keeping the temperature at the desired level? If we hold the variables constant, to what factors can we attribute fluctuations of the temperature (in red) over the desired temperature (in green)?

Try adjusting the insulation of the room and the strength of the heater. Do these factors affect the efficiency of the thermostat (i.e. cause the temperature to stay closer or further from the desired temperature)?

Notice that there is a delay from the time the heater is turned on to the time when this added heat reached the thermometer. What are the consequences of this delay?

## EXTENDING THE MODEL

The thermostat in this model uses a very simple rule to control the heater based on the temperature. It might be possible to improve the performance of this system by making it 'smarter'. One suggestion is to write a control program that turns the heater on and off before the temperature hits the desired temperature --- this would compensate for the delay mentioned above. Try rewriting the function THERMO-CONTROL. Keep in mind that the only inputs your control function should have are GOAL-TEMP and TEMPERATURE, and the only action should be to either call RUN-HEATER or not. Notice this leaves open the possibility of creating variables to store past information.

Much of observed instability in the temperature might simply be attributed to the thermometer that we are using. The current thermometer takes an average of the number of turtles occupying the green patches over the past ten ticks. Why do you suppose we are measuring the temperature in such a way, instead of simply counting the number of turtles inside the room? Can you design a better thermometer?

This model doesn't account for how the outside temperature (outside the yellow box) could effect the inside temperature.  Alter the model so that this now becomes a factor.

The heater in this model puts out heat at a fixed rate, regardless of how long it has been on. Real heaters generally have a warm up period during which time they slowly increase their output, until they reach their maximum rate. Try adjusting the heater in this model to act more like a real heater. How does this affect the behavior or efficiency of the thermostat? How might we alter THERMO-CONTROL to account for this?

Begin a simulation with INITIAL-TEMP set to zero, and notice how long it takes to heat up the room. This means that if we wanted to warm the room up at a certain time it might make sense to turn the heater on beforehand. Introduce time into this model, and try adjusting the thermostat so that it heats the room according to some schedule.

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

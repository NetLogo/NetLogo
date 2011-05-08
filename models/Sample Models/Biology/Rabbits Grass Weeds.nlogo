breed [rabbits rabbit]
rabbits-own [ energy ]

to setup
  clear-all
  grow-grass-and-weeds
  set-default-shape rabbits "rabbit"
  create-rabbits number [
    set color white
    setxy random-xcor random-ycor
    set energy random 10  ;start with a random amt. of energy
  ]
  reset-ticks
end

to go
  if not any? rabbits [ stop ]
  grow-grass-and-weeds
  ask rabbits
  [ move
    eat-grass
    eat-weeds
    reproduce
    death ]
  tick
end

to grow-grass-and-weeds
  ask patches [
    if pcolor = black [
      if random-float 1000 < weeds-grow-rate
        [ set pcolor violet ]
      if random-float 1000 < grass-grow-rate
        [ set pcolor green ]
  ] ]
end

to move  ;; rabbit procedure
  rt random 50
  lt random 50
  fd 1
  ;; moving takes some energy
  set energy energy - 0.5
end

to eat-grass  ;; rabbit procedure
  ;; gain "grass-energy" by eating grass
  if pcolor = green
  [ set pcolor black
    set energy energy + grass-energy ]
end

to eat-weeds  ;; rabbit procedure
  ;; gain "weed-energy" by eating weeds
  if pcolor = violet
  [ set pcolor black
    set energy energy + weed-energy ]
end

to reproduce     ;; rabbit procedure
  ;; give birth to a new rabbit, but it takes lots of energy
  if energy > birth-threshold
    [ set energy energy / 2
      hatch 1 [ fd 1 ] ]
end

to death     ;; rabbit procedure
  ;; die if you run out of energy
  if energy < 0 [ die ]
end
@#$#@#$#@
GRAPHICS-WINDOW
296
13
798
536
20
20
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
-20
20
-20
20
1
1
1
ticks

BUTTON
86
83
141
116
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
151
83
206
116
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

SLIDER
4
160
142
193
grass-grow-rate
grass-grow-rate
0.0
20.0
15
1.0
1
NIL
HORIZONTAL

SLIDER
4
193
151
226
weeds-grow-rate
weeds-grow-rate
0.0
20.0
0
1.0
1
NIL
HORIZONTAL

SLIDER
144
160
275
193
grass-energy
grass-energy
0.0
10.0
5
0.5
1
NIL
HORIZONTAL

SLIDER
153
193
275
226
weed-energy
weed-energy
0.0
10.0
0
0.5
1
NIL
HORIZONTAL

SLIDER
35
42
261
75
number
number
0.0
500.0
150
1.0
1
NIL
HORIZONTAL

SLIDER
65
127
225
160
birth-threshold
birth-threshold
0.0
20.0
15
1.0
1
NIL
HORIZONTAL

PLOT
4
228
275
424
Populations
Time
Pop
0.0
100.0
0.0
111.0
true
true
"set-plot-y-range 0 number" ""
PENS
"grass" 1.0 0 -10899396 true "" "plot count patches with [pcolor = green] / 4"
"rabbits" 1.0 0 -2674135 true "" "plot count rabbits"
"weeds" 1.0 0 -8630108 true "" "plot count patches with [pcolor = violet] / 4"

MONITOR
186
424
275
469
count rabbits
count rabbits
1
1
11

@#$#@#$#@
## WHAT IS IT?

This project explores a simple ecosystem made up of rabbits, grass, and weeds. The rabbits wander around randomly, and the grass and weeds grow randomly.   When a rabbit bumps into some grass or weeds, it eats the grass and gains energy. If the rabbit gains enough energy, it reproduces. If it doesn't gain enough energy, it dies.

The grass and weeds can be adjusted to grow at different rates and give the rabbits differing amounts of energy.  The model can be used to explore the competitive advantages of these variables.

## HOW TO USE IT

Click the SETUP button to setup the rabbits (red), grass (green), and weeds (violet). Click the GO button to start the simulation.

The NUMBER slider controls the initial number of rabbits. The BIRTH-THRESHOLD slider sets the energy level at which the rabbits reproduce.  The GRASS-GROWTH-RATE slider controls the rate at which the grass grows.  The WEEDS-GROWTH-RATE slider controls the rate at which the weeds grow.

The model's default settings are such that at first the weeds are not present (weeds-grow-rate = 0, weeds-energy = 0).  This is so that you can look at the interaction of just rabbits and grass.  Once you have done this, you can start to add in the effect of weeds.

## THINGS TO NOTICE

Watch the COUNT RABBITS monitor and the POPULATIONS plot to see how the rabbit population changes over time. At first, there is not enough grass for the rabbits, and many rabbits die. But that allows the grass to grow more freely, providing an abundance of food for the remaining rabbits. The rabbits gain energy and reproduce. The abundance of rabbits leads to a shortage of grass, and the cycle begins again.

The rabbit population goes through a damped oscillation, eventually stabilizing in a narrow range. The total amount of grass also oscillates, out of phase with the rabbit population.

These dual oscillations are characteristic of predator-prey systems. Such systems are usually described by a set of differential equations known as the Lotka-Volterra equations. NetLogo provides a new way of studying predatory-prey systems and other ecosystems.

## THINGS TO TRY

Leaving other parameters alone, change the grass-grow-rate and let the system stabilize again.  Would you expect that there would now be more grass?  More rabbits?

Change only the birth-threshold of the rabbits.  How does this affect the steady-state levels of rabbits and grass?

With the current settings, the rabbit population goes through a damped oscillation. By changing the parameters, can you create an undamped oscillation? Or an unstable oscillation?

In the current version, each rabbit has the same birth-threshold. What would happen if each rabbit had a different birth-threshold? What if the birth-threshold of each new rabbit was slightly different from the birth-threshold of its parent? How would the values for birth-threshold evolve over time?

Now add weeds by making the sliders WEEDS-GROW-RATE the same as GRASS-GROW-RATE and WEEDS-ENERGY the same as GRASS-ENERGY.  Notice that the amount of grass and weeds is about the same.

Now make grass and weeds grow at different rates.  What happens?

What if the weeds grow at the same rate as grass, but they give less energy to the rabbits when eaten (WEEDS-ENERGY is less than GRASS-ENERGY)?

Think of other ways that two plant species might differ and try them out to see what happens to their relative populations.  For example, what if a weed could grow where there was already grass, but grass couldn't grow where there was a weed?  What if the rabbits preferred the plant that gave them the most energy?

Run the model for a bit, then suddenly change the birth threshold to zero.  What happens?

## NETLOGO FEATURES

Notice that every black patch has a random chance of growing grass or
weeds each turn, using the rule:

    if random-float 1000 < weeds-grow-rate
      [ set pcolor violet ]
    if random-float 1000 < grass-grow-rate
      [ set pcolor green ]

## RELATED MODELS

Wolf Sheep Predation is another interacting ecosystem with different rules.

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

rabbit
false
0
Circle -7500403 true true 76 150 148
Polygon -7500403 true true 176 164 222 113 238 56 230 0 193 38 176 91
Polygon -7500403 true true 124 164 78 113 62 56 70 0 107 38 124 91

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

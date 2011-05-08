to setup
  clear-all
  set-default-shape turtles "square"
  ;; make green atoms on left
  ask patches with [pxcor < 0]
    [ sprout 1 [ set color green ] ]
  ;; make blue atoms on right
  ask patches with [pxcor > 0]
    [ sprout 1 [ set color blue ] ]
  ;; plot the initial state of the system
  reset-ticks
end

to go
  ;; asks vacancies to ask a neighboring atom to
  ;; move into the vacancy
  ask patches with [not any? turtles-here]
    [ move-atom-to-here ]
  tick
end

;; chooses a neighboring atom to move onto a empty patch (vacancy)
to move-atom-to-here  ;; patch procedure
  let atom one-of turtles-on neighbors4
  if atom != nobody [
    ask atom [ move-to myself ]  ;; myself is the calling patch
  ]
end

;;; plotting procedures

to-report greens
  report turtles with [color = green]
end

to-report blues
  report turtles with [color = blue]
end

to plot-atoms [atoms]
  plot-pen-reset
  plot-pen-up
  let column min-pxcor
  repeat world-width [
    let y count atoms with [pxcor = column]
    plotxy column y
    plot-pen-down
    set column column + 1
    plotxy column y
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
285
10
610
356
17
17
9.0
1
10
1
1
1
0
0
0
1
-17
17
-17
17
1
1
1
ticks
30

BUTTON
16
38
101
71
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
106
38
186
71
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
269
356
613
511
Atoms by Column
NIL
NIL
-1.0
1.0
0.0
1.0
false
false
"set-plot-x-range min-pxcor (max-pxcor + 1)\nset-plot-y-range 0 world-height" ""
PENS
"Greens" 1.0 0 -10899396 true "" "plot-atoms greens"
"Blues" 1.0 0 -13345367 true "" "plot-atoms blues"

PLOT
16
83
276
313
Max Diffusion Distance
time
distance squared
0.0
10.0
0.0
100.0
true
false
"" "let rightmost max [pxcor] of greens\nlet leftmost  min [pxcor] of blues\nplot (rightmost - leftmost) ^ 2"
PENS
"default" 1.0 0 -16777216 true "" ""

BUTTON
191
38
273
71
go-once
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

@#$#@#$#@
## WHAT IS IT?

This model describes how diffusion occurs between two adjacent solids.

Diffusion is one of the most important phenomena in fields such as biology, chemistry, geology, chemistry, engineering and physics.  Interestingly, before becoming a famous for the Relativity Laws, Albert Einstein wrote extensively about diffusion, and was one of the first to connect diffusion to the Brownian motion of atoms.

Diffusion can take place in gases, liquids, or solids.  In solids, particularly, diffusion occurs due to thermally-activated random motion of atoms - unless the material is at absolute zero temperature (zero Kelvin), individual atoms keep vibrating and eventually move within the material. One of the possible net effects of diffusion is that atoms move from regions of high concentration of one element to regions with low concentration, until the concentration is equal throughout the sample.

This model demonstrates a solid diffusion couple, such as copper and nickel.  In a real laboratory, such experiment would take place at very high temperatures, for the process to take place in a reasonable amount of time (note that the diffusion coefficient varies exponentially with the inverse of the temperature). There are many mechanisms for diffusion in solids. In this model we demonstrate one of them, which is caused by missing atoms in the metal crystal. The locations, of the missing atoms are often called vacancies.  Therefore, this type of diffusion mechanism is referred to as "vacancy diffusion".  The extent to which the diffusion can happen depends on the temperature and the number of vacancies in the crystal.

In addition, there are various other conditions that are needed for solid diffusion to occur.  Some examples of these are similar atomic size, similar crystal structure, and similar electronegativity.  This model assumes all of these conditions are present.

## HOW IT WORKS

There are two types of atoms, green and blue.  At the beginning, all green atoms are on the left and the blue atoms are on the right.  All the vacancies start out between the two metals.  As atoms move into vacancies, the vacancies disperse.  In most real-world scenarios, vacancies are scattered in the material to begin with.  In this model, for simplification purposes, we assume that the materials have no vacancies in the beginning, and that all the vacancies start off in between the two materials.

In this model we also assume that the heat is evenly distributed throughout the metals.  Therefore, each atom has an equal chance of breaking bonds with its neighbors and moving to a vacancy.

## HOW TO USE IT

To run the model, first press the SETUP button, then press the GO button.

"Atoms by Column" is a distribution diagram of the two atom types.  The other graph is a maximum diffusion distance, squared, versus time. If the model runs long enough, this plot will show an approximately linear relationship between the squared distance and time, following the known equation (for one-dimensional diffusion):

> x<sup>2</sup> = 2 * D * t

where x is the maximum diffusion distance, D is the diffusion coefficient, and t is elapsed time.

## THINGS TO NOTICE

If you run the model for a few hundred ticks, the distribution graph should look like two interleaving curves.  The far edges remain purely one color, while the middle is about 50-50.

The other graph should be generally linear.  The "diffusion coefficient" of the system is proportional to the slope, and can be easily calculated using the above equation.

## THINGS TO TRY

Let the model run for a long time.  (You can use the speed slider to make the model run faster.)  Do you think the metal will ever become completely diffused?

Try increasing the dimensions of the world.  Does the behavior change at all?

## EXTENDING THE MODEL

The model uses a very simple initial state in which there is always exactly one column of vacancies and they are all located in the middle.  Try adding settings that dictate how many vacancies there are and where they start out.

Give the two metals, or the two sides of the world, different characteristics.  For example, a temperature difference could be simulated by making atomic movements on one side happen less often than on the other.

Try changing the crystal structure of the atoms.  In close-packed atoms in two dimensions, atoms actually have six neighbors (hexagonal) instead of four (square).

## NETLOGO FEATURES

This model uses a non-wrapping world.

## RELATED MODELS

MaterialSim Grain Growth  
GasLab Two Gas

## CREDITS AND REFERENCES

Thanks to James Newell for his work on this model.

For additional information:

Porter, D.A., and Easterling, K.E., Phase Transformations in Metals and Alloys, 2nd ed., Chapman & Hall, 1992

Shewmon, P.G., Diffusion in solids, 2nd ed., TMS, 1989
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

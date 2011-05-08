globals
[
  row      ;; this is the current row we are now calculating
  done?    ;; a flag set to allow you to press the go button multiple times
]

patches-own
[
  value    ;; this variable is a floating point number between 0 and 1.5
]


;; initializes patches and globals
to setup-general
  clear-patches
  set row max-pycor   ;; set the current row to the top
  set done? false
end


;; set up a random initial condition, parameterized by initial-turbulence
to setup-random
  setup-general
  reset-ticks
  ;; randomize the values of the current row (top row in this case)
  ask patches with [pycor = row]
  [
    set value ((3 / 2) - ((random-float initial-turbulence) / 100 * (3 / 2)))
    color-patch
  ]
end


;; this is called to copy the bottom row to the top row when the user presses
;; GO again
to setup-continue
  let value-list []
  if not done?  ;; make sure go has already been called
    [ stop ]

  ;; copy values from bottom row of patches to top row
  set value-list map [[value] of ?] sort patches with [pycor = row]
  setup-general
  foreach sort patches with [pycor = row]
  [
    ask ?
    [
      set value item (pxcor + max-pxcor) value-list
      color-patch
    ]
  ]
  set done? false
end


;; main routine
to go
  let row-patches patches with [pycor = row]
  ;; if the end has been reached, continue from the top or stop
  if (row = min-pycor)
  [
    ifelse auto-continue?
    [
      ;; if we are stuck in an absorbing state, there is no reason to continue
      ifelse (roughness = 0.0
              and not any? row-patches with [value > 0 and value <= 1])
      [
        stop
      ]
      [
        set done? true
        display    ;; ensure everything gets drawn before we clear it
        setup-continue
        set row-patches patches with [pycor = row]
      ]
    ]
    [
      ifelse done?
      [
        ;; a run has already been completed, so continue with another
        setup-continue
        set row-patches patches with [pycor = row]
      ]
      [
        ;; otherwise just stop
        set done? true
        stop
      ]
    ]
  ]

  ask row-patches     ;; apply rule
  [
    calculate-next-value
    color-patch
  ]

  set row row - 1
  tick
end


;; calculates and sets the value of the patch immediately below self.
to calculate-next-value  ;; patch procedure
  let patch-below (patch-at 0 -1)

  ;; coupling step: diffuse values
  let value-sum ([value] of (patch-at -1 0) + [value] of (patch-at 1 0))
  let new-value
        ((1 - coupling-strength) * value + ((coupling-strength / 2) * value-sum))

  ;; set variables
  ask patch-below [
    set value new-value
    ;; scale & apply roughness
    set value scale value - roughness
    ;; if we've subtracted too much, just set the value to 0
    if value < 0 [ set value 0 ]
  ]
end


to-report scale [ x ]
  ifelse (x >= 0 and x <= 0.5)
    [ report 3 * x ]
    [
      ifelse (x > 0.5 and x <= 1)
        [ report 3 * (1 - x) ]
        [ report x ]
    ]
end


to color-patch  ;; patch procedure
  set pcolor scale-color pink value 1.5 -1.5
end
@#$#@#$#@
GRAPHICS-WINDOW
282
10
694
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
1
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

TEXTBOX
15
215
203
234
Roughness of pipe
11
0.0
0

TEXTBOX
15
150
272
169
Effect that cells have upon one another
11
0.0
0

SLIDER
40
30
215
63
initial-turbulence
initial-turbulence
0
100
75
0.5
1
%
HORIZONTAL

SLIDER
17
234
242
267
roughness
roughness
0
0.025
0
5.0E-4
1
NIL
HORIZONTAL

SWITCH
40
100
215
133
auto-continue?
auto-continue?
0
1
-1000

SLIDER
15
170
241
203
coupling-strength
coupling-strength
0
1
0.345
0.0050
1
NIL
HORIZONTAL

BUTTON
130
65
215
98
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
40
65
125
98
setup
setup-random
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

This model demonstrates the transition from order, or "laminarity", to disorder, or "turbulence" in fluids.  Using a one-dimensional continuous cellular automaton, this model allows you to explore the relationship between turbulence, laminarity, and the viscosity of a fluid flowing through a "pipe." It also shows you how the roughness of pipes in which the fluid travels through affects the fluid's behavior.

## HOW IT WORKS

This model is based on a "coupled map lattice," otherwise known as a continuous cellular automaton.  See CA 1D Elementary for an introduction to one-dimension cellular automata, and see CA 1D Continuous for an introduction to continuous CA's.

Each cell has a value ranging continuously from 0 to 1.5, where 0 is the greatest degree of turbulence, and 1.5 is the greatest degree of laminarity.  Cells are considered laminar when their value is greater than 1, and turbulent when their value is less than or equal to 1.

To draw each subsequent row, each cell in the previous row is updated in three steps:

1) The first step is coupling, where the cell's value "diffuses," or is averaged with its nearest neighbors on each side. The degree of influence that the cells have on one another is determined by the COUPLING-STRENGTH slider.  The higher the value of the coupling strength, the more the cells will influence one another.  COUPLING-STRENGTH is a rough analog of viscosity.

2) In the second step, another function is applied in order to properly scale the result of the coupling function.

3) The third and last step is the application of friction, which is the analog of roughness (on, for instance, the inside of a pipe or the surface of a sheet of glass).  The ROUGHNESS parameter controls the amount of friction imposed upon the fluid, increasing the cell's turbulence. This may seem a bit strange, but it is important to remember turbulence is measured in terms of disorder; when the fluid's velocity is changed, these fluctuations cause more disorder.

These steps are then repeated for the next row, and so on.

## HOW TO USE IT

Setup:

 * SETUP initializes the model with a mix of turbulent and non-turbulent initial conditions.
 * INITIAL-TURBULENCE controls the degree of initial turbulence.

Parameters:

 * COUPLING-STRENGTH is the amount of "diffusion," or the influence that cells have on one another.
 * ROUGHNESS is the amount of turbulence that is added to each cell at every time step.
 * DISPLAY-CONTINUOUS? will display state values as a gradient from dark to light (laminar and turbulent, respectively) if on, otherwise they will discretized and displayed as either black and white.

Running the model:

 * If AUTO-CONTINUE? is off, GO runs the model until it reaches the bottom (`min-pycor`).  If pressed again, GO will continue the same run at the top row of patches. If AUTO-CONTINUE? is on, upon reaching the bottom row of patches, GO will immediately continue the run from the top.
 * AUTO-CONTINUE? will cause GO to automatically wrap back to the top and continue when switched on. If the model has reached a completely laminar state, and cannot produce any more turbulence, it will not run again.

## THINGS TO NOTICE

Under what circumstances do darker, more "laminar" patches occur?  How is this dependent on the turbulence?

Why do straight, non-budding turbulent streams eventually die out when ROUGHNESS is set to 0?  How come this doesn't occur so much when ROUGHNESS is greater than 0?

## THINGS TO TRY

Set ROUGHNESS to 0:

Change the COUPLING STRENGTH until you find the "critical value" that the COUPLING-STRENGTH must be above in order to produce long-lived turbulence.  This transition is very fast, and occurs within a very small range of the COUPLING-STRENGTH parameter.

When the COUPLING-STRENGTH is above the critical value, what change do you see that might explain why the turbulence is able to perpetuate itself so well?

With a COUPLING-STRENGTH above the critical value, about how much INITIAL-TURBULENCE is required for the turbulence to continue indefinitely?

What happens when the COUPLING-STRENGTH is too high?  Can you give a physical interpretation of this?

Think about these questions while experimenting with ROUGHNESS values above 0:

How does ROUGHNESS affect turbulence in the fluid?

How does ROUGHNESS affect the critical value required for long-lived turbulence?

With higher ROUGHNESS values, is it necessary for there to be a certain amount of INITIAL-TURBULENCE in order to produce long-lived turbulence?  Why or why not?

Is there a difference between turbulence caused by large COUPLING-STRENGTHs and the turbulence caused by friction?  If so, how do they differ?

Can you give a physical interpretation of a ROUGHNESS of 0?

## EXTENDING THE MODEL

Although this is a continuous-valued CA, in a sense the system is discrete in that there is a sharp distinction between "turbulent" and "laminar" cells.  If you modify the COLOR-PATCH procedure to color the cells only as black or white depending on which of these two discrete states they are in, you will see a discrete "view" of this continuous model.

In physical systems, "coupling strength" is a product of various factors, such as temperature, pressure, and viscosity.  Can you come up with a way to take these factors into account in this model?

Surface tension is a subject common to fluid dynamics and turbulence.  Can you figure out a way to integrate it into the model?

There are many ways to quantitatively analyze turbulence.  Try coming up with a plot of the entropy (see 'CA Stochastic' for an example), or the average size of the laminar regions.

Can you create a turbulence model in two dimensions?

## RELATED MODELS

 * CA 1D Elementary - the elementary two-state 1D cellular automata
 * CA Continuous - an elementary continuous 1D cellular automaton
 * CA Stochastic - a probabilistic cellular automaton that exhibits critical phase transitions similar to this model
 * Lattice Gas Automaton - two-dimensional wave propagation through a lattice gas
 * Wave Machine - wave motion in a two-dimensional membrane
 * Percolation - the percolation of oil through soil

## CREDITS AND REFERENCES

Criticality in cellular automata: H. Chate and P. Manneville. 1990 Physica D 45 122-135
Pattern Dynamics in Spatiotemporal Chaos: Kunihiko Kaneko. 1989 Physica D 34 1-41
Supertransients, spatiotemporal intermittency and stability of fully developed spatiotemporal chaos: Kunihiko Kaneko. 1990 Physics Letters A Vol 149, Number 2,3
Shepherd, Dennis G. 1965. Elements of Fluid Mechanics.  Harcourt, Brace, & World, Inc., New York, NY.

Thanks to Eytan Bakshy for his work on this model.
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
set roughness 0.002
setup-random
repeat world-height - 1 [ go ]
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

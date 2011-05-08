breed [ reds a-red ]
breed [ greens a-green ]

turtles-own [ stuck ]

;;;
;;; Setup procedures
;;;

to setup
  clear-all
  set-default-shape turtles "circle"
  setup-patches
  create-molecules 400 reds red
  create-molecules 400 greens green
  reset-ticks
end

to setup-patches
  draw-absorber
  erode
  draw-walls
end

to draw-absorber
  ask patches
    [ if abs pycor < thickness-of-medium
        [ set pcolor blue ]
      ;; if wall-effect? is on, create two channels near the edges
      if wall-effect? and ( pxcor > (max-pxcor - 15) or pxcor < (min-pxcor + 15) )
        [ set pcolor black ] ]
end

to draw-walls
  ask patches
    [ if patch-at 3 0 = nobody or patch-at -3 0 = nobody
        [ set pcolor (violet - 1) ] ]
end

to erode
  crt number-of-pores
    [ setxy random-xcor thickness-of-medium
      dig-tunnel 180 90 ]
end

; tunnels are dug using random walks
to dig-tunnel [direction wiggle]  ;; turtle procedure
  loop
    [ if ycor < (- thickness-of-medium)
        [ die ]
      set heading direction + wiggle - 2 * random-float wiggle
      set pcolor black
      fd 1
      set pcolor black
      fd 1 ]
end

to create-molecules [number new-breed new-color]
  crt number
    [ set xcor (max-pxcor - 3) - random-float (2 * (max-pxcor - 3))
      set ycor thickness-of-medium + random-float (max-pycor - thickness-of-medium)
      set breed new-breed
      set color new-color
      set stuck 0 ]
end

;;;
;;; Running the model
;;;

to go
  if not any? turtles
    [ stop ]
  ask turtles
    [ if patch-at 0 -1 = nobody
        [ die ]
      wander ]
  tick
end

; The red turtles are inert and don't stick to the blue patches
; Green turtles stick to blue.  When a green turtle encounters
; a blue patch its stuck variable is set to stickiness and then
; decreases with time.  When stuck hits zero the particle is
; no longer stuck.
to wander
  set heading (270 - random-float 180)
  ifelse stuck = 0
    [ if ([pcolor] of patch-ahead 1) = black
        [ fd 1 ]
      let p patch-ahead 1
      if p = nobody [ die ]
      if (breed = greens) and (([pcolor] of p) = blue)
        [ set stuck stickiness ] ]
    [ set stuck stuck - 1 ]
end

; We need to compute concentrations of reds and greens that made
; it thru the absorbing layer.
to-report reds-out
  report count reds with [ycor < (- thickness-of-medium)]
end

to-report greens-out
  report count greens with [ycor < (- thickness-of-medium)]
end
@#$#@#$#@
GRAPHICS-WINDOW
256
10
779
554
85
85
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
-85
85
-85
85
1
1
1
ticks
30

PLOT
2
231
245
435
Gas Output
Time
Molecules
0.0
500.0
0.0
25.0
true
false
"" ""
PENS
"Reds" 1.0 0 -2674135 true "" "plot (100 * reds-out) / 400"
"Greens" 1.0 0 -10899396 true "" "plot (100 * greens-out) / 400"

SLIDER
7
77
187
110
thickness-of-medium
thickness-of-medium
1.0
40.0
16
1.0
1
NIL
HORIZONTAL

SLIDER
7
113
187
146
number-of-pores
number-of-pores
0.0
120.0
70
1.0
1
NIL
HORIZONTAL

SLIDER
7
191
188
224
stickiness
stickiness
0.0
10.0
5
1.0
1
NIL
HORIZONTAL

SWITCH
30
41
158
74
wall-effect?
wall-effect?
1
1
-1000

BUTTON
23
152
92
185
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
99
152
168
185
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

@#$#@#$#@
## WHAT IS IT?

This is a model of gas chromatography.  Much of modern chemistry depends on chromatography for the separation of chemicals.  (Gas chromatography is one form of chromatography, involving gases.)  Chromatography can even be so sensitive as to separate enantiomers (i.e., molecules that differ only by being mirror images of each other!).

Chromatography separates chemicals using surface interactions. The idea is simple.  Different chemicals have different tendencies to move through small spaces (for example, if they are of different sizes) and different tendencies to stick to surfaces.  This can be observed in everyday life.  For example, if you put a drop of water and a drop of glue on an inclined plane, the water will roll off, but the glue will stick where it was.  Single molecules, too, can stick very differently to surfaces.  For instance, water vapor will condense on a cold glass, but the oxygen in the air will not.  This is because the bonds that oxygen can make with glass are not strong enough to hold the oxygen there.  It takes only a small step to imagine using the different stickiness of molecules to separate them.

Practically, in gas chromatography molecules are forced to pass through a porous medium, which acts as the sticky surface.  A porous medium is a material that has holes in it -- like swiss cheese, but these holes are microscopic in size.  The holes allow molecules to pass from one side of the medium to the other.  One example would be packed silica.  Under a microscope, packed silica looks not unlike a lump of wood shavings, so molecules can go through it like water would through the wood shavings.

## HOW IT WORKS

The blue area of the world represents the porous medium. The molecules start at the top; they are collected at the bottom.  The red and green particles represent two different kind of molecules.  (In real gas chromatography, these would typically be carried by an inert gas.  The inert gas is forced through the medium by applying pressure.  But gas chromatography can also be used to simply separate gases without a carrier.)  Molecules in this model wander randomly downward through the medium.  Red molecules don't stick to the medium, but green molecules do.  Chemically, this is caused by a number of factors: surface interactions, geometry and size of the molecule, etc.

The amount of stickiness is controlled by a slider.  For example, if the stickiness is set to five, then green molecules sticks to each part of the blue medium for five cycles, essentially slowing its downward motion when compared to the red molecules.  This leads to separation.

This model also attempts to demonstrate the concept of "wall effect".  If there is some space near the walls then the molecules can get through these channels.  This will drastically reduce the quality of separation.  The empty space near the walls may result from bad quality of absorber or from inadequate stuffing.

## HOW TO USE IT

First set the porosity of the blue absorber with the NUMBER-OF-PORES slider.  You can change the thickness of the absorbent layer with a slider named THICKNESS-OF-MEDIUM.  The WALL-EFFECT? switch turns the wall effect on and off.

To run the model, press SETUP, then press GO.

The STICKINESS slider changes the stickiness parameter.  The higher the value of stickiness the slower the green turtles move through absorbing layer.

The program works fastest when STICKINESS is between 3 and 7 and when NUMBER-OF-PORES is over 30.  If STICKINESS is too high, or NUMBER-OF-PORES is too small, it might take a long time for the green turtles to pass through the absorbing layer.

## THINGS TO NOTICE

Separation depends on porosity.  If porosity is too small the absorbent gets contaminated and clogged up and the process stops.  If porosity is too high then there is not enough separation.  Also wall effect reduces the quality of separation in a drastic way.

## THINGS TO TRY

One could try to improve the plotting routine.  In a real chromatograph there is only one output curve showing the rate at which molecules come out and a device called an integrator that tells the amount of matter that has come out.

Try separating more than two gases.

## NETLOGO FEATURES

Notice the routine that digs tunnels.  It is very easy to implement parallel random walk algorithms with NetLogo.

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
NetLogo 5.0beta1
@#$#@#$#@
setup
repeat 125 [ go ]
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

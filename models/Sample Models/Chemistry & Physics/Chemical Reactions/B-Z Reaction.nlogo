patches-own [state new-state]

to setup
  clear-all
  ask patches
    [ set state random (max-state + 1)   ;; pick a state from 0 to max-state
      set pcolor scale-color red state 0 max-state ]
  reset-ticks
end

to go
  ;; first all the patches compute their new state
  ask patches [ find-new-state ]
  ;; only once all the patches have computed their new state
  ;; do they actually change state
  ask patches
  [ set state new-state
    set pcolor scale-color red state 0 max-state ]
  tick
end

to find-new-state  ;; patch procedure
  ifelse state = max-state  ;; ill?
    [ set new-state 0 ] ;; get well
    [ let a count neighbors with [state > 0 and state < max-state]  ;; count infected
      let b count neighbors with [state = max-state] ;; count ill
      ifelse state = 0  ;; healthy?
      [ set new-state int (a / k1) + int (b / k2) ]
        [ let s state + sum [state] of neighbors
          set new-state int (s / (a + b + 1)) + g ]
      if new-state > max-state   ;; don't exceed the maximum state
        [ set new-state max-state ] ]
end
@#$#@#$#@
GRAPHICS-WINDOW
205
10
619
445
50
50
4.0
1
10
1
1
1
0
1
1
1
-50
50
-50
50
1
1
1
ticks
10.0

SLIDER
15
95
187
128
max-state
max-state
1
200
200
1
1
NIL
HORIZONTAL

BUTTON
36
46
99
79
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
46
169
79
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
15
146
187
179
k1
k1
1
8
3
1
1
NIL
HORIZONTAL

SLIDER
15
180
187
213
k2
k2
1
8
3
1
1
NIL
HORIZONTAL

SLIDER
15
214
187
247
g
g
0
100
28
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

The Belousov-Zhabotinsky reaction (or B-Z reaction for short) is an unusual chemical reaction.  Instead of steadily moving towards a single equilibrium state, it oscillates back and forth between two such states.  Before this "chemical oscillator" was discovered, it was thought that such a reaction could not exist.

If you do the reaction in a beaker, the whole beaker regularly changes color from yellow to clear and back again, over and over.  In this case, we say that the reaction is oscillating in time.  However, if you do the reaction in a thin layer of fluid trapped between two glass plates, then a beautiful pattern emerges of concentric or spiral waves of color change passing through the fluid.  Here, the reaction is oscillating in both time and space.

This model is a cellular automaton (or CA) that produces spiral waves that resemble those produced by the B-Z reaction.  Similar spiral waves have also been observed in biological systems, such as slime molds.

The B-Z reaction is a redox reaction that periodically moves between an oxidized and a reduced state, and has been demonstrated for various chemicals.  This model does not attempt to replicate the actual mechanism of the chemical reaction, which is quite complex (including 18 reactions and 21 species, according to the Fields-Koros-Noyes model).  The abstract features shared by the real reaction and this model include:

1. Two end states.  
2. A positive feedback mechanism.  
3. A negative feedback mechanism.

The positive feedback mechanism acts to push the system further in the direction that it is already going, reinforcing and amplifying the initial change.  (In the chemical reaction, positive feedback comes from auto-catalysis.)  The negative feedback mechanism pushes the system back in the opposite direction once a threshold is reached, suppressing or counteracting the effected change.

## HOW IT WORKS

Each cell has a state which is an integer from 0 to max-state.  We choose to show state 0 as black, max-state as white, and intermediate states as shades of red.

Suppose we call state 0 "healthy", max-state "sick", and anything in between "infected".  Then the rules for how each cell changes at each step can be described as follows:

1. A cell that is sick becomes healthy.
2. A cell that is healthy may become infected, if enough of its eight neighbors are infected or sick.  Whether this happens is affected by the k1 and k2 sliders.  (Lower k1 means higher tendency to be infected by infected neighbors; lower k2 means higher tendency to be infected by sick neighbors.)
3. A cell that is infected computes its new state by averaging the states of itself and its eight neighbors, then adding the value of the g slider.  (Higher g means infected cells get sicker more rapidly.)

1 is the negative feedback; 2 and 3 are the positive feedback.

These are only qualitative descriptions.  To see the actual math used, look at the FIND-NEW-STATE procedure in the Code tab.

## HOW TO USE IT

Press SETUP to initialize each cell in the grid to a random state.

Press GO to run the model.

## THINGS TO NOTICE

Run the model with the default slider settings.

What happens near the beginning of run?

After about 100 ticks, you should start to see spirals emerging.

After about 200 ticks, the spirals should fill the world.

Can you work out why the specific rules used produce patterns like the ones you see?

## THINGS TO TRY

What if you do a really long run --- what happens?

What is the effect of varying the different sliders?  You can think of k1 and k2 as affecting the tendency for healthy cells to become infected, and g as affecting the speed with which the infection gets worse.

## EXTENDING THE MODEL

This automaton is an example of a "reaction-diffusion" system.  By altering the CA rules, you may be able to simulate other reaction-diffusion systems.

## NETLOGO FEATURES

`find-new-state` is a long and rather complicated procedure.  It could be clearer if it were split into subprocedures, but then the model wouldn't run quite as fast.  Since this particular CA takes so many iterations to settle into its characteristic pattern, we decided that speed was important.

## RELATED MODELS

Boiling, in the Physics/Heat section, is another cellular automaton that uses similar, though simpler, rules.  The early stages of the Boiling model resemble the early stages of this model.

Fireflies, in the Biology section, is analogous to the B-Z reaction in a stirred beaker (the whole beaker "synchronizes" so it's switching back and forth all at once, like the fireflies).

Many models in the NetLogo models library can be thought as systems composed of positive and/or negative feedback mechanisms.

## CREDITS AND REFERENCES

The B-Z reaction is named after Boris Belousov and Anatol Zhabotinsky, the Russian scientists who discovered it in the 1950's.

A discussion of the chemistry behind the reaction, plus a movie and some pictures, are available at http://online.redwoods.cc.ca.us/instruct/darnold/DEProj/Sp98/Gabe/intro.htm .

The cellular automaton was presented by A.K. Dewdney in his "Computer Recreations" column in the August 1988 of Scientific American.

See http://www.hermetic.ch/pca/bz.htm for a pretty screen shot of the cellular automaton running on a very large grid (using custom software for Windows, not NetLogo).
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
repeat 200 [ go ]
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

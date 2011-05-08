globals [
  sum-of-spins   ;; sum of all the spins -- keeping track of this
                 ;; means that we can always instantly calculate
                 ;; the magnetization (which is the average spin)
]

patches-own [
  spin           ;; holds -1 or 1
]

to setup [initial-magnetization]
  clear-all
  ask patches
    [ ifelse initial-magnetization = 0
        [ set spin one-of [-1 1] ]
        [ set spin initial-magnetization ]
      recolor ]
  set sum-of-spins sum [spin] of patches
  reset-ticks
end

to go
  ask one-of patches [ update ]
  tick
end

to update  ;; patch procedure
  ;; flipping changes the sign on our energy, so the difference in energy
  ;; if we flip is -2 times our current energy
  let Ediff 2 * spin * sum [spin] of neighbors4
  if (Ediff <= 0) or
     (temperature > 0 and (random-float 1.0 < exp ((- Ediff) / temperature)))
    [ set spin (- spin)
      set sum-of-spins sum-of-spins + 2 * spin
      recolor ]
end

to recolor  ;; patch procedure
  ifelse spin = 1
    [ set pcolor blue + 2 ]
    [ set pcolor blue - 2 ]
end

to-report magnetization
  report sum-of-spins / count patches
end
@#$#@#$#@
GRAPHICS-WINDOW
315
10
730
446
40
40
5.0
1
10
1
1
1
0
1
1
1
-40
40
-40
40
1
1
1
ticks

BUTTON
4
41
89
74
NIL
setup -1
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
82
92
225
125
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
10
139
306
172
temperature
temperature
0
10
2.24
0.01
1
NIL
HORIZONTAL

MONITOR
73
186
187
231
magnetization
magnetization
3
1
11

PLOT
9
238
305
437
Magnetization
time
average spin
0.0
20.0
-1.0
1.0
true
false
"" ""
PENS
"average spin" 1.0 0 -13345367 true "" "if ticks mod plotting-interval = 0 [\n  plotxy ticks magnetization\n]"
"axis" 1.0 0 -16777216 true ";; draw a horizontal line to show the x axis\nauto-plot-off\nplotxy 0 0\nplotxy 1000000000000000 0\nauto-plot-on" ""

BUTTON
93
41
214
74
setup-random
setup 0
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
218
41
306
74
setup +1
setup 1
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
72
439
244
472
plotting-interval
plotting-interval
1
1000
100
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This is a model of a magnet at the microscopic level.  The magnetic moments (spins) of the atoms in the magnet can either be up or down.  Spins can change as a result of being influenced by neighboring spins and by the ambient temperature.  The overall behavior of the system will vary depending on the temperature.

When the temperature is low, there is spontaneous magnetization, and we say that the system is in the ferromagnetic phase.  When the temperature is high, there is no spontaneous magnetization, and we say that the system is in the paramagnetic phase.  (At room temperature, a refrigerator magnet is ferromagnetic, but an ordinary piece of iron is paramagnetic.)

This very abstract model can be interpreted in other ways as well, for example as a model of liquid/gas phase transitions (where the two states are liquid and gas, instead of two magnetic spin states).  It has also been used as a basis for simulating phenomena in the social sciences that involve phase-transition-like behavior.

## HOW IT WORKS

We represent the two possible spin states with the numbers +1 or -1.  Spins of +1 are shown in light blue, spins of -1 in dark blue.

The energy at each spin is defined as the negative of the sum of the products of the spin with each of its neighboring four spins.  So for example if a spin is surrounded by four opposing spins, then the energy is 4, the maximum possible.  But if a spin is surrounded by four like spins, then the energy is -4, the minimum possible.  Basically, the energy measures how many like or opposite neighbors the spin has.

A spin decides whether to "flip" to its opposite as follows.  The spins are seeking a low energy state, so a spin will always flip if flipping would decrease its energy.  But the spins sometimes also flip into a higher energy state.  We calculate the exact probability of flipping using the Metropolis algorithm, which works as follows.  Call the potential gain in energy Ediff.  Then the probability of flipping is e ^ (-Ediff/temperature).

The gist of this formula is that as the temperature increases, flipping to a higher energy state becomes increasingly likely, but as the energy to be gained by flipping increases, the likelihood of flipping decreases.  You could use a different formula with the same gist, but the Metropolis algorithm is most commonly used.

To run the model, we repeatedly pick a single random spin and give it the chance to flip.

## HOW TO USE IT

Choose an initial temperature with the TEMPERATURE slider, then press SETUP to set up the grid and give each spin a random initial state.  (Or, if you want to start with all the spins set to the same state, press SETUP -1 or SETUP +1.)

Then press GO to watch the model run.

You can move the TEMPERATURE slider as the model runs if you want.

The magnetization of the system is the average (mean) of all the spins.  The MAGNETIZATION monitor and plot show you the current magnetization and how it has varied so far over time.

The default speed of the model is fairly slow.  If you move the speed slider to the right, you'll see a dramatic speedup (because NetLogo will spend more of its time computing and less of its time redrawing the grid).

You can also speed up the model with the PLOTTING-INTERVAL slider.  If the slider is set to 1, we plot a point every tick.  If the slider is set to 100 (default), we plot a point every 100 ticks.  The less plotting takes place, the faster the model runs.

## THINGS TO NOTICE

In the default settings for the model, the temperature is set fairly low.  What happens to the system?

## THINGS TO TRY

What happens when the temperature slider is very high?  (This is called the "paramagnetic" state.)  Try this with all three setup buttons.

What happens when the temperature slider is set very low?  (This is called the "ferromagnetic" state.)  Again, try this with all three setup buttons.

Between these two very different behaviors is a transition point.  On an infinite grid, the transition point can be proved to be 2 / ln (1 + sqrt 2), which is about 2.27.  On a large enough finite toroidal grid, the transition point is near this number.

What happens when the temperature is near, but above the transition point?  What happens when the temperature is near, but below the transition point?  Note that the nearer you are to the transition point, the longer the system takes to exhibit its characteristic behavior -- in such cases, we say that the system has a long "correlation length".  So near the transition point, you'll need to do more and longer runs in order to be sure you understand what the typical behavior of the system is.

When the temperature is low, the system should quickly reach a fully magnetized state.  Note that if you set the temperature very low, sometimes fairly stable thick "stripes" of opposite colors can form, preventing the system from reaching full magnetization.  This is an effect of the finite size of the grid.

## EXTENDING THE MODEL

The formula for Ediff given above can be modified by multiplying the result by a "coupling constant" (if we don't do this, effectively we are using a coupling constant of 1).  Using a negative coupling constant changes the model into its "antiferromagnetic" variant.  Experiment with the effect of varying the coupling constant.

Can you eliminate the exponential formula for the probability of flipping and replace it with a simple discrete rule that gives similar behavior?

How could you relate TEMPERATURE in this model to temperature in Fahrenheit, Celsius or Kelvin?

If you think of the two grid states as "present" and "absent", then this model can be changed slightly to be a model of the motion of particles if you add a rule that states can only flip in pairs, so that the total number of particles is conserved.  Add this rule and see what behaviors result.

At each iteration, we give only one spin a chance to flip.  There are other possible methods, for example, the "checkerboard" update rule: if you imagine the rectangular lattice as being made of alternating white and black squares like a checkerboard, then at each iteration first all the white squares update simultaneously, then all the black squares update simultaneously.  On a parallel computer with many processors, and a programming language which takes advantage of them, the checkerboard rule would run much much faster.  Since NetLogo only simulates parallelism using a single processor, the checkerboard rule does not run dramatically faster.  (Note that if you use the checkerboard rule, the grid of spins must have even dimensions.)

## NETLOGO FEATURES

Because only a very small amount happens during each tick, this is a model where the speed slider is especially important, since it lets the user speed the model up by not updating the view every tick.

## RELATED MODELS

Voting (a social science model, but the update rules are very similar, except that there is no concept of "temperature")

## CREDITS AND REFERENCES

Thanks to Seth Tisue for his work on this model and to Sara Solla and Kan Chen for their assistance.

The Ising model was first proposed by Wilhelm Lenz in 1920.  It is named after his student Ernst Ising, who also studied it.  This NetLogo model implements the Monte Carlo simulation of the Metropolis algorithm for the two dimensional Ising model.  The Metropolis algorithm comes from a 1953 paper by Nicholas Metropolis et al.

There are many web pages which explore the Ising model in greater detail than attempted here.  Here are a few:
http://bartok.ucsc.edu/peter/java/ising/keep/ising.html
http://oscar.cacr.caltech.edu/Hrothgar/Ising/references.html
http://www.physics.cornell.edu/sethna/teaching/sss/ising/intro.htm
http://physics.syr.edu/courses/ijmp_c/Ising.html
http://www.npac.syr.edu/users/paulc/lectures/montecarlo/
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
setup 0
repeat 100000 [ go ]
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

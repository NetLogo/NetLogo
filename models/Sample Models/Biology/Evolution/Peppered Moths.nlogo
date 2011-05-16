breed [moths moth] ;; might extend the model with other breeds: birds, bugs, etc.

moths-own [
  age               ;; moth's age: 0, 1 = young (can't reproduce), 2, 3 = mature (can reproduce), > 3 = old (can't reproduce)
]

globals [
  light-moths       ;; number of moths in the lightest third of possible colors
  medium-moths      ;; number of moths in the medium third of possible colors
  dark-moths        ;; number of moths in the darkest third of possible colors
  darkness          ;; darkness (pollution) level in the world
  darkening?        ;; is the world getting darker (more polluted)?
]

;; reports color value that reflects current pollution level.
;; 1 = black. 9 = white. color = white - darkness. darkness range: 0 - 8. color range: 1 - 9.
to-report env-color
  report 9 - darkness
end

;; reports numerical color change value that reflects user's speed % choice.
to-report delta-env
  report (speed / 100)
end

;; generates random color integers in black-white range (1-9)
to-report random-color
  report ((random 9) + 1)
end

;; reports maximum moth population for a given environment
to-report upper-bound
  report (4 * num-moths)
end

to setup
  clear-all
  setup-world
  setup-moths
  update-monitors
  reset-ticks
end

to setup-world
  set darkness 0
  set darkening? true ;; world starts out clean - can only get polluted
  ask patches [ set pcolor env-color ]
end

to setup-moths
  create-moths num-moths
  [
    set color random-color
    moths-pick-shape
    set age (random 3) ;; start out with random ages
    setxy random-xcor random-ycor
  ]
end

to go
  ask moths [
    moths-mate
    moths-grim-reaper
    moths-get-eaten
    moths-age
  ]
  if cycle-pollution? [
    cycle-pollution
  ]
  tick
  update-monitors
end

;; asexual reproduction - moths just hatch other moths
to moths-mate ;; moth procedure
  if (age = 2 or age = 3) [
    hatch 2 [
      if (random-float 100.0 < mutation) [
    ifelse ((random 2 = 0)) [ ;; flip a coin -- darker or lighter?
      set color (round (color + ((random-float mutation) / 12.5)))
      if (color >= 9) [
        set color 9
      ]
    ][
      set color (round (color - ((random-float mutation) / 12.5 )))
      if (color <= 1) or (color >= 130) [  ;; to prevent color from wrapping
        set color 1
      ]
    ]
      ]
      moths-pick-shape
      set age 0
      rt random-float 360.0
      fd 1 ;; move away from your parent so you can be seen
    ]
  ]
end

;; we have a range of 'well-camouflaged-ness', dependent on the rate of selection
to moths-get-eaten ;; moth procedure
  if (random-float 1000.0 < ((selection * (abs (env-color - color))) + 200)) [
    die
  ]
end

;; disease, children, entomologists, etc...
;; the moth's world is a cruel place.
to moths-grim-reaper ;; moth procedure
  if ((random 13) = 0) [
    die
  ]

  ;; population overshoot / resource scarcity
  if ((count moths) > upper-bound) [
    if ((random 2) = 0) [
      die
    ]
  ]
end

to moths-age ;; moth procedure
  set age (age + 1)
end


to moths-pick-shape ;; moth procedure
  ifelse (color < 5 ) [
    set shape "moth dark"
  ][
    set shape "moth light"
  ]
end

to update-monitors
  ;; colors range from 1 - 9. dark moths = 1-3. medium moths = 4-6. light moths = 7-9.
  set light-moths (count moths with [color >= 7])
  set dark-moths (count moths with [color <= 3])
  set medium-moths (count moths - (light-moths + dark-moths))
end


;; single pollution step. called by cycle-pollution. can also be invoked by "pollute" button.
to pollute-world
  ifelse (darkness <= (8 - delta-env)) [ ;; can the environment get more polluted?
    set darkness (darkness + delta-env)
    ask patches [ set pcolor env-color ]
  ][
    set darkening? false
  ]
end

;; single de-pollution step. called by cycle-pollution. can also be invoked by "clean up" button.
to clean-up-world
  ifelse (darkness >= (0 + delta-env)) [ ;; can the environment get cleaner?
    set darkness (darkness - delta-env)
    ask patches [ set pcolor env-color ]
  ][
    set darkening? true
  ]
end


;; world dims, then lightens, all in lockstep
;; a monochrome world is best for this, because otherwise it'd be very
;; difficult to tell what is a moth and what is a patch
to cycle-pollution
  ifelse (darkening? = true) [
    pollute-world
  ][
    clean-up-world
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
296
10
636
451
16
20
10.0
1
10
1
1
1
0
1
1
1
-16
16
-20
20
1
1
1
ticks
15.0

SLIDER
4
152
262
185
num-moths
num-moths
0
200
100
1
1
NIL
HORIZONTAL

PLOT
642
36
942
335
Moth Colors Over Time
Time
Moth Color Count
0.0
100.0
0.0
200.0
true
true
"set-plot-y-range 0 upper-bound" ""
PENS
"Light" 1.0 0 -1184463 true "" "plot light-moths"
"Medium" 1.0 0 -10899396 true "" "plot medium-moths"
"Dark" 1.0 0 -13345367 true "" "plot dark-moths"
"Pollution" 1.0 0 -7500403 true "" "plot ((upper-bound / 3) * darkness / 8)"

BUTTON
4
41
59
74
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

MONITOR
4
316
86
361
Light Moths
light-moths
0
1
11

MONITOR
200
316
288
361
Dark Moths
dark-moths
0
1
11

MONITOR
92
316
195
361
Medium Moths
medium-moths
0
1
11

BUTTON
65
41
121
74
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
261
264
294
mutation
mutation
0.0
100.0
15
1.0
1
NIL
HORIZONTAL

SLIDER
4
206
263
239
selection
selection
0.0
100.0
50
1.0
1
NIL
HORIZONTAL

MONITOR
4
378
85
423
Total Moths
light-moths + medium-moths + dark-moths
0
1
11

BUTTON
4
91
59
124
pollute
pollute-world
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
65
91
132
124
clean up
clean-up-world
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

MONITOR
92
378
194
423
Pollution (%)
100 * darkness / 8
1
1
11

SLIDER
144
91
277
124
speed
speed
1.0
100.0
10
1.0
1
NIL
HORIZONTAL

SWITCH
127
41
278
74
cycle-pollution?
cycle-pollution?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

This project models a classic example of natural selection - the peppered moths of Manchester, England. The peppered moths use their coloration as camouflage from the birds that would eat them. (Note that in this model, the birds act invisibly.) Historically, light-colored moths predominated because they blended in well against the white bark of the trees they rested on.

However, due to the intense pollution caused by the Industrial Revolution, Manchester's trees became discolored with soot, and the light-colored moths began to stick out, while the dark-colored moths blended in. Consequently, the darker moths began to predominate.

Now, in the past few decades, pollution controls have helped clean up the environment, and the trees are returning to their original color. Hence, the lighter moths are once again thriving at expense of their darker cousins.

## HOW IT WORKS

This model simulates these environmental changes, and how a population of moths, initially of all different colors, changes under the pressures of natural selection.

## HOW TO USE IT

The NUM-MOTHS slider controls how many moths are initially present in the world. Their coloration is randomly distributed over the possible colors of the world (white to black). Simply select how many moths you'd like to begin with (around 200 is good), and press the SETUP button. Then press the GO button to begin the simulation.

The MUTATION slider controls the rate of mutation at birth. For the purposes of the simulation, the mutation rate is much higher than it might be in real life. When MUTATION is set to 0, moths are exactly the same as the parent that hatched them. When it is set to 100, there is no correlation between a parent's color and the color of its children. (Best results are seen when MUTATION is set to around 10 or 15, but experiment with the rate and watch what happens.)

The SELECTION slider determines how moths are harvested by the birds that feed on them. SELECTION wraps up nicely many factors that determine the survivability of a species - how many birds there are, how hungry they are, and just how important camouflage is to escaping predation. SELECTION provides a probabilistic window - the lower the level of the slider, the wider this window. At 0, a moth's color ceases to matter. At 100, a moth needs to be perfectly camouflaged to avoid being seen (and thus devoured). You might first try running the model with SELECTION set to around 50.

The POLLUTE and CLEAN UP once-buttons, along with the CYCLE-POLLUTION? switch, control the pollution levels in the environment. To watch the cycle described above - from clean environment to industrial revolution to pollution control - set CYCLE-POLLUTION? to on. To directly manipulate the pollution levels in the environment, set CYCLE-POLLUTION? to off, and use the POLLUTE and CLEAN UP buttons to add and remove pollution from the environment.

The SPEED slider controls just how rapidly pollution levels change. As you might guess, 1 is slow, and 100 is fast. A good speed to start with is 10.

Finally, there are six monitors, all of which are straightforward. TICKS reports how much time has elapsed. TOTAL MOTHS displays how many moths are present in the world. LIGHT MOTHS, MEDIUM MOTHS, and DARK MOTHS report the total numbers of moths with each color gradation. The moth population is just divided into thirds over the range of colors. POLLUTION reports the pollution level in the environment on a scale from 0% (no pollution) to 100% (maximum pollution).

## THINGS TO NOTICE

The most important thing to watch is how the entire set of moths seems to change color over time. Let the model run by itself the first time - watch the world change from white to black back to white. Then see how manipulating the sliders effects the populations of moths.

Notice that during the first few initial time-steps, the moth population booms. You might then see the moth population fluctuate between different levels, some of which are quite large. The moths give birth to many offspring, but the world in which they live is finite --- it has finite space and resources. If the population exceeds the available resources (carrying capacity), the moths tend die a lot faster than they would otherwise. Under normal circumstances, the average population will tend to stay constant, at a level dependent on the speed and selection rates.

Watch what happens when a drastic change in the environment occurs. (You can force this with the POLLUTE-WORLD and CLEAN-UP-WORLD buttons.) Can you kill off all of the moths in a matter of a few time-steps?

You can watch the ratios between the types of moths change either in the monitors, or graphically in the plot. The yellow line represents the lighter-colored moths, the green line represents the intermediate moths, and the blue line represents the darker-colored moths.

## THINGS TO TRY

How do different levels of mutation and selection change the population? How does the speed of the model effect the rate at which the moths change? Is there a speed at which the moths can't keep up, i.e. the world changes faster than small pockets of discolored moths or mutants can help keep the population up to size?

The upper-bound for the moth population is defined as a global variable, `upper-bound`. It is initially set to 4 * the moth population, but you can change it and watch what happens.

## EXTENDING THE MODEL

'Peppered Moths' is a nice introduction into modeling genetic and evolutionary phenomena. The code is fairly simple, and divided up into several small procedures that handle the different stages of each generation. This makes it easy for other extensions to be added to the model.

Each moth has one gene that effectively determines its survivability under current conditions. This is a turtle variable, simply the turtle's color. Add the concept of the recessive gene to 'Peppered Moths'- each moth might have two color genes (additional turtle variables), that together determine its color. Moths will then need to seek out mates, and use sexual reproduction as opposed to the unnatural asexual reproduction we see here.

## NETLOGO FEATURES

Note that all of the commands given to the moths are in a block of code that begins `ask moths`. This is because each moth is given a breed, `moths`. This makes the code far easier to modify, especially if you want to add a different kind of animal, say, the birds that eat the moths. You would then add a new breed, `birds`, and put all code that birds are to execute in the body of `ask birds`.

## CREDITS AND REFERENCES

The peppered moths of Manchester, England as a case study in natural selection were originally studied by British scientist H. B. D. Kettlewell.

In 1998, Michael Majerus of the University of Cambridge re-examined Kettlewell's work and found that though his experimental design was questionable in some respects, his conclusions were likely correct nonetheless.  In any case, the mechanism of natural selection illustrated by this model is not in doubt.
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

moth dark
false
14
Polygon -1 true false 150 61 105 16 76 2 46 2 14 16 0 45 0 89 16 122 30 135 61 151 29 166 1 196 1 239 16 273 46 287 18 275 59 299 105 299 121 286 150 256
Polygon -1 true false 150 61 196 16 226 1 254 1 286 16 299 45 299 91 285 121 271 136 240 151 271 167 299 196 299 242 286 271 242 299 196 299 151 258
Rectangle -16777216 true true 136 16 165 286
Polygon -16777216 true true 136 46 105 16 77 2 45 2 13 16 0 44 0 88 17 125 29 136 60 151 30 165 0 194 1 242 16 275 57 299 108 299 138 269
Polygon -16777216 true true 164 49 195 17 225 1 255 1 287 15 299 41 299 93 285 121 270 138 241 151 269 165 299 193 299 245 286 272 243 299 195 299 164 272 164 49
Line -1 false 136 46 106 16
Line -1 false 106 16 76 1
Line -1 false 165 48 196 17
Line -1 false 196 17 226 1
Line -1 false 226 1 256 1
Line -1 false 256 1 287 15
Line -1 false 287 15 300 45
Line -1 false 76 2 45 2
Line -1 false 45 2 15 14
Line -1 false 15 14 1 43
Line -1 false 1 43 1 89
Line -1 false 1 89 14 119
Line -1 false 14 119 30 137
Line -1 false 31 138 60 151
Line -1 false 299 44 299 93
Line -1 false 299 93 285 119
Line -1 false 285 119 272 136
Line -1 false 272 136 242 150
Line -1 false 61 153 30 165
Line -1 false 30 165 2 193
Line -1 false 2 195 2 242
Line -1 false 2 243 16 273
Line -1 false 16 273 58 297
Line -1 false 241 152 270 165
Line -1 false 270 165 299 195
Line -1 false 299 195 298 250
Line -1 false 298 250 285 271
Line -1 false 285 271 244 298
Line -1 false 244 298 193 297
Line -1 false 193 297 163 270
Line -1 false 135 269 104 298
Line -1 false 104 298 58 298
Rectangle -7500403 true false 136 17 164 287

moth light
false
15
Polygon -1 true true 150 61 105 16 76 2 46 2 14 16 0 45 0 89 16 122 30 135 61 151 29 166 1 196 1 239 16 273 46 287 18 275 59 299 105 299 121 286 150 256
Polygon -1 true true 150 61 196 16 226 1 254 1 286 16 299 45 299 91 285 121 271 136 240 151 271 167 299 196 299 242 286 271 242 299 196 299 151 258
Line -16777216 false 150 60 105 16
Line -16777216 false 105 16 78 1
Line -16777216 false 78 1 45 1
Line -16777216 false 45 1 15 14
Line -16777216 false 15 14 0 43
Line -16777216 false 0 43 0 86
Line -16777216 false 0 86 16 123
Line -16777216 false 16 123 30 134
Line -16777216 false 30 134 60 151
Line -16777216 false 60 151 30 165
Line -16777216 false 30 165 0 194
Line -16777216 false 0 194 1 240
Line -16777216 false 1 240 15 272
Line -16777216 false 15 272 57 299
Line -16777216 false 57 299 105 298
Line -16777216 false 105 298 149 257
Line -16777216 false 149 257 196 298
Line -16777216 false 196 298 242 298
Line -16777216 false 242 298 285 271
Line -16777216 false 285 271 299 242
Line -16777216 false 299 242 299 194
Line -16777216 false 299 194 271 167
Line -16777216 false 271 167 242 152
Line -16777216 false 242 152 270 137
Line -16777216 false 270 137 285 121
Line -16777216 false 285 121 299 91
Line -16777216 false 299 91 299 44
Line -16777216 false 299 44 285 15
Line -16777216 false 285 15 253 0
Line -16777216 false 253 0 225 0
Line -16777216 false 225 0 195 16
Line -16777216 false 195 16 149 62
Rectangle -7500403 true false 135 16 164 286

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

globals [
  max-age               ;; maximum age that all daisies live to
  global-temperature    ;; the average temperature of the patches in the world
  num-blacks            ;; the number of black daisies
  num-whites            ;; the number of white daisies
  scenario-phase        ;; interval counter used to keep track of what portion of scenario is currently occurring
  ]

breed [daisies daisy]

patches-own [temperature]  ;; local temperature at this location

daisies-own [
  age       ;; age of the daisy
  albedo    ;; fraction (0-1) of energy absorbed as heat from sunlight
]


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Setup Procedures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  set-default-shape daisies "flower"
  ask patches [ set pcolor gray ]

  set max-age 25
  set global-temperature 0

  if (scenario = "ramp-up-ramp-down"    ) [ set solar-luminosity 0.8 ]
  if (scenario = "low solar luminosity" ) [ set solar-luminosity 0.6 ]
  if (scenario = "our solar luminosity" ) [ set solar-luminosity 1.0 ]
  if (scenario = "high solar luminosity") [ set solar-luminosity 1.4 ]

  seed-blacks-randomly
  seed-whites-randomly
  ask daisies [set age random max-age]
  ask patches [calc-temperature]
  set global-temperature (mean [temperature] of patches)
  update-display
  reset-ticks
end

to seed-blacks-randomly
   ask n-of round ((start-%-blacks * count patches) / 100) patches with [not any? daisies-here]
     [ sprout-daisies 1 [set-as-black] ]
end

to seed-whites-randomly
   ask n-of floor ((start-%-whites * count patches) / 100) patches with [not any? daisies-here]
     [ sprout-daisies 1 [set-as-white] ]
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


to go
   ask patches [calc-temperature]
   diffuse temperature .5
   ask daisies [check-survivability]
   set global-temperature (mean [temperature] of patches)
   update-display
   tick
   if (scenario = "ramp-up-ramp-down")
   [
     if (ticks > 200 and ticks <= 400) [set solar-luminosity solar-luminosity + 0.005]
     if (ticks > 600 and ticks <= 850) [set solar-luminosity solar-luminosity - 0.0025]
   ]
   if (scenario = "low solar luminosity")  [set solar-luminosity 0.6 ]
   if (scenario = "our solar luminosity")  [set solar-luminosity 1.0 ]
   if (scenario = "high solar luminosity") [set solar-luminosity 1.4 ]
end

to set-as-black ;; turtle procedure
  set color black
  set albedo albedo-of-blacks
  set age 0
  set size 0.6
end

to set-as-white  ;; turtle procedure
  set color white
  set albedo albedo-of-whites
  set age 0
  set size 0.6
end

to check-survivability ;; turtle procedure
  let seed-threshold 0
  let not-empty-spaces nobody
  let seeding-place nobody

  set age (age + 1)
  ifelse age < max-age
  [
     set seed-threshold ((0.1457 * temperature) - (0.0032 * (temperature ^ 2)) - (0.6443))
     ;; This equation may look complex, but it is just a parabola.
     ;; This parabola has a peak value of 1 -- the maximum growth factor possible at an optimum
     ;; temperature of 22.5 degrees C
     ;; -- and drops to zero at local temperatures of 5 degrees C and 40 degrees C. [the x-intercepts]
     ;; Thus, growth of new daisies can only occur within this temperature range,
     ;; with decreasing probability of growth new daisies closer to the x-intercepts of the parabolas
     ;; remember, however, that this probability calculation is based on the local temperature.

     if (random-float 1.0 < seed-threshold) [
       set seeding-place one-of neighbors with [not any? daisies-here]

       if (seeding-place != nobody)
       [
         if (color = white)
         [
           ask seeding-place [sprout-daisies 1 [set-as-white]  ]
         ]
         if (color = black)
         [
           ask seeding-place [sprout-daisies 1 [set-as-black]  ]
         ]
       ]
     ]
  ]
  [die]
end

to calc-temperature  ;; patch procedure
  let absorbed-luminosity 0
  let local-heating 0
  ifelse not any? daisies-here
  [   ;; the percentage of absorbed energy is calculated (1 - albedo-of-surface) and then multiplied by the solar-luminosity
      ;; to give a scaled absorbed-luminosity.
    set absorbed-luminosity ((1 - albedo-of-surface) * solar-luminosity)
  ]
  [
      ;; the percentage of absorbed energy is calculated (1 - albedo) and then multiplied by the solar-luminosity
      ;; to give a scaled absorbed-luminosity.
    ask one-of daisies-here
      [set absorbed-luminosity ((1 - albedo) * solar-luminosity)]
  ]
  ;; local-heating is calculated as logarithmic function of solar-luminosity
  ;; where a absorbed-luminosity of 1 yields a local-heating of 80 degrees C
  ;; and an absorbed-luminosity of .5 yields a local-heating of approximately 30 C
  ;; and a absorbed-luminosity of 0.01 yields a local-heating of approximately -273 C
  ifelse absorbed-luminosity > 0
      [set local-heating 72 * LN(absorbed-luminosity) + 80]
      [set local-heating 80]
  set temperature ((temperature + local-heating) / 2)
     ;; set the temperature at this patch to be the average of the current temperature and the local-heating effect
end

to paint-daisies   ;; daisy painting procedure which uses the mouse location draw daisies when the mouse button is down
  if mouse-down?
  [
    ask patch mouse-xcor mouse-ycor [
      ifelse not any? daisies-here
      [
        if paint-daisies-as = "add black"
          [sprout-daisies 1 [set-as-black]]
        if paint-daisies-as = "add white"
          [sprout-daisies 1 [set-as-white]]
      ]
      [
        if paint-daisies-as = "remove"
          [ask daisies-here [die]]
      ]
      display  ;; update view
    ]
  ]
end

to update-display
  ifelse (show-temp-map? = true)
    [ ask patches [set pcolor scale-color red temperature -50 110] ]  ;; scale color of patches to the local temperature
    [ ask patches [set pcolor grey] ]

  ifelse (show-daisies? = true)
    [ ask daisies [set hidden? false] ]
    [ ask daisies [set hidden? true] ]
end
@#$#@#$#@
GRAPHICS-WINDOW
424
10
869
476
14
14
15.0
1
10
1
1
1
0
1
1
1
-14
14
-14
14
1
1
1
ticks

SLIDER
5
180
200
213
albedo-of-surface
albedo-of-surface
0
1
0.4
0.01
1
NIL
HORIZONTAL

CHOOSER
5
90
200
135
scenario
scenario
"ramp-up-ramp-down" "maintain current luminosity" "low solar luminosity" "our solar luminosity" "high solar luminosity"
1

SLIDER
5
140
200
173
solar-luminosity
solar-luminosity
0.0010
3
0.8
0.0010
1
NIL
HORIZONTAL

SLIDER
245
10
415
43
start-%-blacks
start-%-blacks
0
50
20
1
1
NIL
HORIZONTAL

SLIDER
75
10
240
43
start-%-whites
start-%-whites
0
50
20
1
1
NIL
HORIZONTAL

SWITCH
425
525
575
558
show-temp-map?
show-temp-map?
0
1
-1000

BUTTON
6
45
71
78
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
6
8
71
41
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

SWITCH
424
485
574
518
show-daisies?
show-daisies?
0
1
-1000

PLOT
215
85
415
235
Luminosity
NIL
NIL
0.0
100.0
0.5
1.5
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "plot solar-luminosity"

PLOT
215
245
415
395
Global Temperature
NIL
NIL
0.0
100.0
-20.0
50.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "plot global-temperature"

PLOT
215
405
415
555
Population
NIL
NIL
0.0
100.0
0.0
100.0
true
false
"" "set num-whites count turtles with [color = white]\nset num-blacks count turtles with [color = black]"
PENS
"black" 1.0 0 -16777216 true "" "plot num-blacks"
"white" 1.0 0 -7500403 true "" "plot num-whites"

SLIDER
75
45
240
78
albedo-of-whites
albedo-of-whites
0
0.99
0.75
0.01
1
NIL
HORIZONTAL

SLIDER
245
45
415
78
albedo-of-blacks
albedo-of-blacks
0
0.99
0.25
0.01
1
NIL
HORIZONTAL

BUTTON
734
485
869
530
remove all daisies
ask daisies [die]\ndisplay
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
594
535
729
578
paint daisies
paint-daisies
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

CHOOSER
594
485
730
530
paint-daisies-as
paint-daisies-as
"add black" "add white" "remove"
2

@#$#@#$#@
## WHAT IS IT?

This model explores the "Gaia hypothesis", which considers the Earth as a single, self-regulating system including both living and non-living parts. In particular, this model explores how living organisms both alter and are altered by climate, which is non-living. The example organisms are daisies and the climatic factor considered is temperature.

Daisyworld is a world filled with two different types of daisies: black daisies and white daisies.  They differ in albedo, which is how much energy they absorb as heat from sunlight.  White daisies have a high surface albedo and thus reflect light and heat, thus cooling the area around them.  Black daisies have a low surface albedo and thus absorb light and heat, thus heating the area around them.  However, there is only a certain temperature range in which daisies can reproduce; if the temperature around a daisy is outside of this range, the daisy will produce no offspring and eventually die of old age.

When the climate is too cold it is necessary for the black daisies to propagate in order to raise the temperature, and vice versa -- when the climate is too warm, it is necessary for more white daisies to be produced in order to cool the temperature.  For a wide range of parameter settings, the temperature and the population of daisies will eventually stabilize.  However, it is possible for Daisyworld to get either too hot or too cold, in which case the daisies are not able to bring the temperature back under control and all of the daisies will eventually die.

## HOW IT WORKS

White daisies, black daisies, and open ground (empty patches) each have an albedo or percentage of energy they absorb as heat from sunlight. Sunlight energy can be changed with the SOLAR-LUMINOSITY slider (a value of 1.0 simulates the average solar luminosity of our sun).

Each time step, every patch will calculate the temperature at that spot based on (1) the energy absorbed by the daisy at that patch and (2) the diffusion of 50% of the temperature value at that patch between its neighbors.  Open ground patches that are adjacent to a daisy have a probability of sprouting a daisy that is the same color as the neighboring daisy, based on a parabolic probability function that depends on the local temperature (where an optimum temperature of 22.5 yields a maximum probability of 100% of sprouting a new daisy). Daisies age each step of the simulation until they reach a maximum age, at which point they die and the patch they were in becomes open.

## HOW TO USE IT

START-%-WHITES and START-%-BLACKS sets the starting percentage of the patches that will be occupied by daisies (of either color) after pressing SETUP.

Selecting PAINT-DAISIES-AS and pressing PAINT-DAISIES allows the user to draw or erase daisies in the VIEW, by left clicking on patches.

ALBEDO-OF-WHITES and ALBEDO-OF-BLACKS sets the amount of heat absorbed by each of these daisy colors. ALBEDO-OF-SURFACE sets the amount of heat absorbed by an empty patch.

The SOLAR-LUMINOSITY sets the amount of incident energy on each patch from sunlight. But this value only will stay fixed at the user set value if the SCENARIO chooser is set to "maintain current luminosity". Other values of this chooser will change the albedo values. For example "ramp-up-ramp-down" will start the solar luminosity at a low value, then start increasing it to a high value, and then bring it back down again over the course of a model run.

SHOW-TEMP-MAP? shows a color map of temperature at each patch. Light red represents hotter temperatures, and darker red represents colder temperatures.

## THINGS TO NOTICE

Run the simulation. What happens to the daisies?  Do the populations ever remain stable? Are there ever population booms and busts?  If so, what causes them? (Hint: how do the daisies affects the climate? How does the climate then affect the daisies?)

What happens if boom and bust cycles just keep getting bigger and bigger? The swings can't keep getting bigger forever.

Does the planet ever become completely filled with life, or completely devoid of life?

Try running the simulation without the daisies. What happens to the planet's temperature? How is it different from what happens with the daisies?

Can the Daisyworld system be said to exhibit "hysteresis"?  Hysteresis is a property of systems that do not instantly follow the forces applied to them, but react slowly, or do not return completely to their original state.  The state of such systems depend on their immediate history.

## THINGS TO TRY

Try running the model with SHOW-DAISIES? off and SHOW-TEMP-MAP? on. You might be able to see interesting spatial patterns that emerge in temperature concentrations and periodic redistricting of temperature regions more easily in this mode.

Try adjusting the fixed temperature diffusion setting in the procedures (change it from 0.5). What happens to the behavior of Daisyworld if temperature is never diffused (set to 0.0)?

## EXTENDING THE MODEL

Black and white daisies represent two extreme types of daisies that could exist in this world.  Implement a third species of daisy.  You will need to choose what your daisy does and how it is different from black and white daisies.  How does your new daisy affect the results of this model?

Sunlight is only one aspect that controls the growth of daisies and other forms of life. Change the model so different parts of the world have different levels of soil quality.  How will this affect the outcome?

Many people feel that the Gaia hypothesis can be disturbed by human causes.  Implement pollution in the model.  Does this cause the daisies to die off quicker or more often?

Can you think of any other ways in which living organisms both alter and are altered by their environment?

## NETLOGO FEATURES

Uses the `diffuse` primitive to distribute heat between patches.

## RELATED MODELS

An alternate Daisyworld model is listed on the [User Community Models](http://ccl.northwestern.edu/netlogo/models/community/) page. It uses patches only, no turtles.

## CREDITS AND REFERENCES

The Daisyworld model was first proposed and implemented by Lovelock and Andrew Watson. The original Gaia hypothesis is due to Lovelock.

Watson, A.J., and J.E. Lovelock, 1983, "Biological homeostasis of the global environment: the parable of Daisyworld", Tellus 35B, 286-289. (The original paper by Watson and Lovelock introducing the Daisyworld model.)

http://www.carleton.edu/departments/geol/DaveSTELLA/Daisyworld/daisyworld_model.htm
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
true
0
Circle -7500403 true true 30 30 240
Circle -7500403 true true 0 120 60
Circle -7500403 true true 240 120 60
Circle -7500403 true true 120 0 60
Circle -7500403 true true 120 240 60
Circle -7500403 true true 60 225 60
Circle -7500403 true true 180 225 60
Circle -7500403 true true 225 180 60
Circle -7500403 true true 15 180 60
Circle -7500403 true true 15 60 60
Circle -7500403 true true 225 60 60
Circle -7500403 true true 180 15 60
Circle -7500403 true true 60 15 60

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

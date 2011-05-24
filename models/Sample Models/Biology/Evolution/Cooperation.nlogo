turtles-own [ energy ]
patches-own [ grass ]

breed [cooperative-cows cooperative-cow]
breed [greedy-cows greedy-cow]

to setup
  clear-all
  setup-cows
  ask patches [
    set grass max-grass-height
    color-grass
  ]
  reset-ticks
end

to setup-cows
  set-default-shape turtles "cow"   ;; applies to both breeds
  crt initial-cows [
    setxy random-xcor random-ycor
    set energy metabolism * 4
    ifelse (random-float 1.0 < cooperative-probability) [
      set breed cooperative-cows
      set color red - 1.5
    ] [
      set breed greedy-cows
      set color sky - 2
    ]
  ]
end

to go
  ask turtles [  ;; includes both breeds
    move
    eat
    reproduce
  ]
  ask patches [
    grow-grass
    color-grass
  ]
  tick
end

to reproduce  ;; turtle procedure
  if energy > reproduction-threshold [
    set energy energy - reproduction-cost
    hatch 1
  ]
end

to grow-grass  ;; patch procedure
  ifelse ( grass >= low-high-threshold) [
    if high-growth-chance >= random-float 100
      [ set grass grass + 1 ]
  ][
    if low-growth-chance >= random-float 100
      [ set grass grass + 1 ]
  ]
  if grass > max-grass-height
    [ set grass max-grass-height ]
end

to color-grass  ;; patch procedure
  set pcolor scale-color (green - 1) grass 0 (2 * max-grass-height)
end

to move  ;; turtle procedure
  rt random 360
  fd stride-length
  set energy energy - metabolism
  if energy < 0 [ die ]
end

to eat  ;; turtle procedure
  ifelse breed = cooperative-cows [
    eat-cooperative
  ] [
    if breed = greedy-cows
      [ eat-greedy ]
  ]
end

to eat-cooperative  ;; turtle procedure
  if grass > low-high-threshold [
    set grass grass - 1
    set energy energy + grass-energy
  ]
end

to eat-greedy  ;; turtle procedure
  if grass > 0 [
    set grass grass - 1
    set energy energy + grass-energy
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
336
10
661
356
10
10
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
-10
10
-10
10
1
1
1
ticks
30.0

BUTTON
218
42
273
75
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
38
41
93
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

SLIDER
7
115
214
148
cooperative-probability
cooperative-probability
0
1.0
0.5
0.01
1
NIL
HORIZONTAL

SLIDER
7
80
154
113
initial-cows
initial-cows
0
100
20
1
1
NIL
HORIZONTAL

SLIDER
504
425
661
458
low-high-threshold
low-high-threshold
0.0
99.0
5
1.0
1
NIL
HORIZONTAL

SLIDER
337
392
505
425
high-growth-chance
high-growth-chance
0.0
99.0
77
1.0
1
NIL
HORIZONTAL

SLIDER
155
80
332
113
stride-length
stride-length
0.0
0.3
0.08
0.01
1
NIL
HORIZONTAL

SLIDER
504
392
661
425
max-grass-height
max-grass-height
1
40
10
1
1
NIL
HORIZONTAL

SLIDER
7
182
215
215
reproduction-threshold
reproduction-threshold
0.0
200.0
102
1.0
1
NIL
HORIZONTAL

SLIDER
503
359
661
392
grass-energy
grass-energy
0.0
200.0
51
1.0
1
NIL
HORIZONTAL

SLIDER
216
115
332
148
metabolism
metabolism
0.0
99.0
6
1.0
1
NIL
HORIZONTAL

SLIDER
337
425
505
458
low-growth-chance
low-growth-chance
0.0
99.0
30
1.0
1
NIL
HORIZONTAL

SLIDER
7
150
215
183
reproduction-cost
reproduction-cost
0.0
99.0
54
1.0
1
NIL
HORIZONTAL

PLOT
7
221
320
401
Cows over time
Time
Cows
0.0
10.0
0.0
100.0
true
true
"" ""
PENS
"greedy" 1.0 0 -14985354 true "" "plot count greedy-cows"
"cooperative" 1.0 0 -6675684 true "" "plot count cooperative-cows"

TEXTBOX
113
62
203
80
basic sliders:
11
0.0
0

TEXTBOX
369
372
483
392
advanced sliders:
11
0.0
0

MONITOR
8
414
134
471
# greedy cows
count greedy-cows
1
1
14

MONITOR
143
414
307
471
# cooperative cows
count cooperative-cows
1
1
14

@#$#@#$#@
## WHAT IS IT?

This model (and Altruism and Divide the Cake) are part of the EACH unit ("Evolution of Altruistic and Cooperative Habits: Learning About Complexity in Evolution").  See http://ccl.northwestern.edu/cm/EACH/ for more information on the EACH unit. The EACH unit is embedded within the BEAGLE (Biological Experiments in Adaptation, Genetics, Learning and Evolution) evolution curriculum. (See http://ccl.northwestern.edu/curriculum/simevolution/beagle.shtml .)

This is an evolutionary biology model.  In it, agents (cows) compete for natural resources (grass).  Cows that are more successful in getting grass reproduce more often, and will thus be more evolutionarily successful.  This model includes two kinds of cows, greedy and cooperative.  It shows how these two different strategies do when competing against each other within a population that evolves over time.

## HOW IT WORKS

Every turn, each cow looks at the patch that it is currently on, and eats a unit of grass.  The greedy cows eat the grass regardless of the length of the grass on the current patch.  The cooperative cows won't eat the grass below a certain height.  This behavior is significant because below a certain height (called the "growth threshold"), the grass grows at a far slower rate than above it.  Thus, the cooperative agents leave more food for the overall population at a cost to their individual well-being, while the greedy agents eat the grass down to the nub, regardless of the effect on the overall population.

## HOW TO USE IT

GO: Starts and stops the model.

SETUP: Resets the simulation according to the parameters set by the sliders.

INITIAL-COWS: Sets the number of initial cows.

COOPERATIVE-PROBABILITY: Sets the chance an initial cow will be of the cooperative breed

STRIDE-LENGTH: This value determines the movement of the cows. Each cow will move forward a distance of STRIDE-LENGTH  each turn.  As the value is increased, the cows will move to other patches more frequently.

GRASS-ENERGY: Each time a cow can eat some grass from the patch that it currently occupies, it increases its energy by the value of this slider.

METABOLISM: Every time step, each cow loses the amount of energy set by this slider.  If the cows energy dips below 0, it dies.  Every cow starts with a default energy of 50, which means it can go 50 / METABOLISM turns without eating.

REPRODUCTION-THRESHOLD: If a cow's energy reaches the value of this slider, it reproduces.  This value represents the food-gathering success that a cow would have to have in order to be able to reproduce.

REPRODUCTION-COST: Each time a cow reproduces, it loses the amount of energy set by this slider.  This value represents the energy cost of reproduction.

LOW-GROWTH-CHANCE: This value is the percentage chance that the grass below the growth threshold will grow back.  The higher this value, the less the discrepancy between the behaviors of the cooperative and greedy cows.

HIGH-GROWTH-CHANCE: This value is the percentage chance that the grass above the growth threshold will grow back.  The lower this value, the less the discrepancy between the behaviors of the cooperative and greedy cows.

MAX-GRASS-HEIGHT:  This value sets the highest length to which the grass can grow.

LOW-HIGH-THRESHOLD:  This value sets the grass growth threshold.  At, or above this value, the grass grows back with HIGH-GROWTH-CHANCE.  Below this value, the grass grows back with LOW-GROWTH-CHANCE.

## THINGS TO NOTICE

Run the model with the default settings.  Watch the different growth curves on the population plot.  Which population expands first?  Which population wins in the end?

## THINGS TO TRY

Slowly decrease the STRIDE-LENGTH slider. What happens to the populations?

At what value of STRIDE-LENGTH do the populations' growth rates change dramatically?  What does this indicate about the evolutionary advantages of cooperating versus being greedy?  What are the important environmental factors?

Change the METABOLISM and the GRASS-ENERGY values.  How do these values affect the model?

Change the LOW-GROWTH-CHANCE and the HIGH-GROWTH-CHANCE values.  How do these values affect the model?

How does the LOW-HIGH-THRESHOLD value affect the growth of the populations?

Can you find settings that maximize the advantage of the cooperative cows?

## EXTENDING THE MODEL

This model explores only one type of cooperative behavior, namely eating the grass above the growth threshold (the LOW-HIGH-THRESHOLD value).  What other cooperative, or altruistic, behaviors could be modeled that hurt individual fitness, while helping the group overall?  What environmental conditions other than grass length could be used to affect the health of a population?

This model relies primarily upon population "viscosity" (the STRIDE-LENGTH slider) to alter the behavior of the cows to allow for the success of the cooperative agents.  What other variables could have such a drastic effect on the evolutionary success of populations?

Also, consider that in this model the behaviors are fixed.  What would happen if the agents learned, or changed their behavior based on food availability?

## NETLOGO FEATURES

Breeds are used to represent the two different kinds of agents.  The `turtles` primitive is used to refer to both breeds together.

## RELATED MODELS

Altruism

## CREDITS AND REFERENCES

This model and the Altruism model are part of the EACH unit "Evolution of Altruistic and Cooperative Habits: Learning About Complexity in Evolution".  See http://ccl.northwestern.edu/cm/EACH/ for more information. EACH is embedded with the BEAGLE (Biological Experiments in Adaptation, Genetics, Learning and Evolution) evolution curriculum. See http://ccl.northwestern.edu/curriculum/simevolution/beagle.shtml .

Thanks to Damon Centola, Eamon McKenzie, Josh Mitteldorf, and Scott Styles.
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

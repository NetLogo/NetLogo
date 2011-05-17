patches-own [
  benefit-out                 ;; 1 for altruists, 0 for selfish
  altruism-benefit
  fitness
  self-weight self-fitness
  alt-weight alt-fitness
  harsh-weight harsh-fitness
]

to setup
  clear-all
  ask patches [ initialize ]
  reset-ticks
end

to initialize  ;; patch procedure
  let ptype random-float 1.0
  ifelse (ptype < altruistic-probability) [
    set benefit-out 1
    set pcolor pink
  ] [
    set benefit-out 0
    ifelse (ptype < altruistic-probability + selfish-probability) [
      set pcolor green
    ] [
      set pcolor black
    ]
  ]
end

to go
  ;; if all altruistic and selfish patches are gone, stop
  if all? patches [pcolor != pink and pcolor != green]
    [ stop ]
  ask patches [
    set altruism-benefit   benefit-from-altruism * (benefit-out + sum [benefit-out] of neighbors4) / 5
  ]
  ask patches [
    perform-fitness-check
  ]
  lottery
  tick
end

to perform-fitness-check  ;; patch procedure
  if (pcolor = green) [
    set fitness (1 + altruism-benefit)
  ]
  if(pcolor = pink) [
    set fitness ((1 - cost-of-altruism) + altruism-benefit)
  ]
  if (pcolor = black) [
    set fitness harshness
  ]
end

to lottery
  ask patches [ record-neighbor-fitness ]
  ask patches [ find-lottery-weights ]
  ask patches [ next-generation ]
end

to record-neighbor-fitness  ;; patch procedure
  set alt-fitness 0
  set self-fitness 0
  set harsh-fitness 0
  if (pcolor = pink) [
    set alt-fitness fitness
  ]
  if (pcolor = green) [
    set self-fitness fitness
  ]
  if (pcolor = black) [
    set harsh-fitness fitness
  ]
  update-fitness-from-neighbor 1 0
  update-fitness-from-neighbor -1 0
  update-fitness-from-neighbor 0 1
  update-fitness-from-neighbor 0 -1
end

to update-fitness-from-neighbor [x y]  ;; patch procedure
  let neighbor-color [pcolor] of patch-at x y
  let neighbor-fitness [fitness] of patch-at x y
  if (neighbor-color = pink)
    [set alt-fitness (alt-fitness + neighbor-fitness)]
  if (neighbor-color = green)
    [set self-fitness (self-fitness + neighbor-fitness)]
  if(neighbor-color = black)
    [set harsh-fitness (harsh-fitness + neighbor-fitness)]
end

to find-lottery-weights ;; patch procedure
  let fitness-sum alt-fitness + self-fitness + harsh-fitness + disease
  ifelse (fitness-sum > 0) [
    set alt-weight (alt-fitness / fitness-sum)
    set self-weight (self-fitness / fitness-sum)
    set harsh-weight ((harsh-fitness + disease) / fitness-sum)
  ] [
    set alt-weight 0
    set self-weight 0
    set harsh-weight 0
  ]
end

to next-generation ;; patch procedure
  let breed-chance random-float 1.0
  ifelse (breed-chance < alt-weight) [
    set pcolor pink
    set benefit-out 1
  ] [
    ifelse (breed-chance < (alt-weight + self-weight))[
      set pcolor green
      set benefit-out 0
    ] [
      clear-patch
    ]
  ]
end

to clear-patch ;; patch procedure
  set pcolor black
  set altruism-benefit 0
  set fitness 0
  set alt-weight 0
  set self-weight 0
  set harsh-weight 0
  set alt-fitness 0
  set self-fitness 0
  set harsh-fitness 0
  set benefit-out 0
end
@#$#@#$#@
GRAPHICS-WINDOW
330
10
709
410
20
20
9.0
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
30.0

BUTTON
75
18
144
51
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
53
58
270
91
altruistic-probability
altruistic-probability
0.0
0.5
0.26
0.01
1
NIL
HORIZONTAL

SLIDER
53
91
270
124
selfish-probability
selfish-probability
0.0
0.5
0.26
0.01
1
NIL
HORIZONTAL

SLIDER
53
190
270
223
disease
disease
0.0
1.0
0
0.01
1
NIL
HORIZONTAL

SLIDER
53
157
270
190
benefit-from-altruism
benefit-from-altruism
0.0
0.9
0.48
0.01
1
NIL
HORIZONTAL

SLIDER
53
124
270
157
cost-of-altruism
cost-of-altruism
0.0
0.9
0.13
0.01
1
NIL
HORIZONTAL

BUTTON
181
18
250
51
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
53
222
270
255
harshness
harshness
0.0
1.0
0
0.01
1
NIL
HORIZONTAL

PLOT
3
262
325
410
Populations
time
frequency
0.0
50.0
0.0
100.0
true
true
"" ""
PENS
"altruists" 1.0 0 -2064490 true "" "plot count patches with [pcolor = pink]"
"selfish" 1.0 0 -10899396 true "" "plot count patches with [pcolor = green]"

MONITOR
21
430
108
487
#altruists
count patches with [pcolor = pink]
1
1
14

MONITOR
175
429
254
486
#selfish
count patches with [pcolor = green]
1
1
14

@#$#@#$#@
## WHAT IS IT?

This model (and Cooperation and Divide the Cake) are part of the EACH unit ("Evolution of Altruistic and Cooperative Habits: Learning About Complexity in Evolution").  See http://ccl.northwestern.edu/cm/EACH/ for more information on the EACH unit. The EACH unit is embedded within the BEAGLE (Biological Experiments in Adaptation, Genetics, Learning and Evolution) evolution curriculum. (See http://ccl.northwestern.edu/curriculum/simevolution/beagle.shtml .)

This is an evolutionary biology model.  It models population genetics with respect to the fitness of traits that are affected by social and environmental conditions.  The model has two types of patch agents: altruistic agents and selfish agents.

The basic premise of the model is that the selfish agents and the altruistic agents are competing for each spot in the world by entering into a genetic lottery.  You can imagine these agents as plants who "seed" for a spot, and the dominant seed generally wins.  The details of the lottery are explained below in HOW IT WORKS.

Under normal (non-interfering) environmental conditions, the selfish agents win, and the altruistic population is driven to extinction.  However, as outlined in HOW TO USE IT, when the environmental conditions are made more harsh, the altruistic population is able to survive, and even dominate the selfish population.

## HOW IT WORKS

1. Patches live in five-cell, plus-sign-shaped neighborhoods.  Whenever a patch is calculating something about its fitness, it is the center of the neighborhood.  For another patch, when that patch is calculating, it becomes merely one of the neighbors.

2. Each patch is an agent that has a fitness.  Each patch is also the location of a lottery for its space.  The patch and the four surrounding patches put in "seeds" to try to get the patch turned to their type of patch, altruist or selfish. Being successful in the lottery is getting patches to turn to your type.  We're assuming here that the type (altruistic or selfish) is the important genetic trait.

3.  Each patch calculates its own fitness using equation:  
if it is A (altruist): 1 - cost + (Number Altruists in Neighborhood / 5 * benefit from Altruists)  
if it is S (selfish):  1 + (Number Altruists in Neighborhood / 5 * benefit from Altruists)  
Thus, the fitness of the S patch will be higher than the fitness of the A's.  If the cost is 0.2 and benefit is 0.5, for an A surrounded by two S's and two A's, then the fitness of this spot is 1 - 0.2 + (3/5 * 0.5) = 1.1.

4.  After each patch has calculated its fitness, it looks to its four neighbors.  Each of the five patches, including itself, puts a weighted seed into a genetic lottery for this center spot.  So, for example, if the neighborhood is ASASA, each of the three A's register their fitness value, and each of the two S's put in their fitness.  The A's are added, and the S's are added.  Let us assume that the A's add up to 3.2 (this includes the A in the center spot), and the S's add up to 2.6.  These two numbers are the altruist weight and selfish weight respectively, in the lottery for the center spot.  Now, the larger number, whichever it is, is called the Major seed; it is divided by the sum of all the fitnesses.
Thus, 3.2/(3.2 + 2.6) = .552.  This number is the Altruism seed in the lottery.  The minor seed is 2.6/(3.2 + 2.6) = .448. (Notice that the Altruism seed of the parent is 3/5 = .600, while the child's is .552.  Even though altruism is dominating, it is losing ground.)

5.  There are a number of ways of doing the lottery itself.  Currently, we choose a random number between 0 and 1.  Now, if the Number is below the Minor seed, the minor weight gets the spot, and if it is above the major seed, the major seed gets the spot.  So, in the example, if the random number is anywhere from .449 to 1, then the Major seed gets it. If it is between 0 and .448, the minor seed gets it.

## HOW TO USE IT

SETUP button --- sets up the model by creating the agents.

GO button --- runs the model

ALTRUISTIC-PROBABILITY slider --- lets you determine the initial proportion of altruists

SELFISH-PROBABILITY slider --- determines the initial proportion of selfish agents.

ALTRUISM-COST slider --- determines the value of cost in the above fitness equations.

BENEFIT-FROM-ALTRUISM slider --- determines the value of benefit in the above fitness equations.

There are two sliders for controlling environmental variables:

HARSHNESS slider --- sets the value for the resistance of empty patch spots to being populated by agents.  The value for this slider determines a corresponding value in the lottery for each empty (black) spot on the grid; the higher this value, the more difficult it is to populate.

DISEASE slider --- sets the value for the possibility that the agents in occupied spots will die.  The value for this slider is factored into the genetic lottery, and determines the percentage chance that each agent will die out from their spot.

## THINGS TO TRY

1.  At first, run the model with Harshness and Disease both at 0.  Notice that the selfish population quickly dominates the world, driving the altruistic population to extinction.  How do respective population sizes affect the outcome?

2.  Play with the values of cost and benefit.  What are realistic values for actual genetic competition?  How does initial population size effect the significance of these values?

3.  Increase the Harshness and Disease values, independently, and with respect to one another.  What are the effects of the Harshness Model?  of Disease?  How are the values dependent on one another?  At what values does the altruistic population begin to have greater success?

4.  Consider why the introduction of Harshness and Disease conditions affects the success of the altruistic population.  How does each population, run alone, respond to the Harshness and Disease conditions?  If you imagine the black spots as Voids (a third type of competing agent), what is the fitness relationship between Altruists and Voids?  Selfish agents and Voids?

5.  Can you find slider values that maximize the advantage of the altruistic agents?

6.  Try running BehaviorSpace on this model to explore the model's behavior under a range of initial conditions.

## EXTENDING THE MODEL

The model can be extended in a number of interesting directions, including adding new environmental variables, adding different types of agents, and changing the altruistic and selfish weighting under different environmental conditions.

This model does not address the behaviors of individuals, only the relative weights of genetic traits.  A next step in considering the evolution of altruism is to model altruistic behaviors.  (See the related model: Cooperation.)

## NETLOGO FEATURES

This model uses patches as its basic agents. Can you design an "equivalent" model using turtles?  How would the model dynamics be affected?

## RELATED MODELS

Cooperation

## CREDITS AND REFERENCES

This model and the Cooperation model are part of the curriculum unit "Evolution of Altruistic and Cooperative Habits: Learning About Complexity in Evolution".  See http://ccl.northwestern.edu/cm/EACH/ for more information. The EACH unit is embedded within the BEAGLE (Biological Experiments in Adaptation, Genetics, Learning and Evolution) evolution curriculum. See http://ccl.northwestern.edu/curriculum/simevolution/beagle.shtml .

This model is based on a paper by Mitteldorf and Wilson, 2000, "Population Viscosity and the Evolution of Altruism", Journal of Theoretical Biology, v.204, pp.481-496.

Thanks also to Damon Centola and Scott Styles.
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

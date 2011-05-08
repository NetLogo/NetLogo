breed [ people person ]

; just used to make pretty graphics
breed [ trees tree ]

turtles-own [ health ]

patches-own[ pollution
             is-power-plant?
             is-tree? ]
to setup
  ca
  reset-ticks

  set-default-shape people "person"
  set-default-shape trees "tree"

  ask patches [
    set pollution 0
    set is-power-plant? false
  ]

  create-power-plants

  ask patches [ pollute ]

  create-people initial-population [
    set color black
    randomize-position
    set health 5
  ]

  do-plot
end

to go
  ask people
  [
    wander
    reproduce
    maybe-plant
    eat-pollution
    maybe-die
  ]

diffuse pollution 0.8
 ask patches [ pollute ]

 ask trees [ cleanup maybe-die ]

 if not any? people
   [ stop ]

 do-plot
 tick
end

to create-power-plants
  ask n-of power-plants patches [ set is-power-plant? true ]
end

to pollute  ;; patch procedure
  if ( is-power-plant? )
  [
    set pcolor red
    set pollution polluting-rate
  ]
  set pcolor scale-color red ( pollution - .1 ) 5 0
end

to cleanup  ;; tree procedure
    set pcolor green + 3
    set pollution max (list 0 ( pollution - 1 ) )
    ask neighbors [ set pollution max (list 0 ( pollution - .5 ) ) ]
    set health health - 0.1
end

to wander   ;; person procedure
  rt random-float 50
  lt random-float 50
  fd 1
  set health health - 0.1
end

to reproduce ;; person procedure
  if ( ( health > 4 ) and ( ( random-float 1 ) < birth-rate ) )
    [ hatch-people 1 [ set health 5 ] ]
end

to maybe-plant ;; person procedure
  if ( ( random-float 1 ) < planting-rate )
  [ hatch-trees 1 [ set health 5 set color green ] ]
end

to eat-pollution  ;; person procedure
  if ( pollution > 0.5 )
  [
    set health (health - (pollution / 10))
  ]
end


to maybe-die     ;;die if you run out of health
  if ( health <= 0 )
    [ die ]
end

to do-plot
  set-current-plot-pen "trees"
  plot count trees
  set-current-plot-pen "people"
  plot count people
  set-current-plot-pen "pollution"
  plot sum [pollution] of patches
end

to randomize-position
  setxy random-float world-width
        random-float world-height
end
@#$#@#$#@
GRAPHICS-WINDOW
296
13
803
485
35
31
7.0
1
10
1
1
1
0
1
1
1
-35
35
-31
31
1
1
1
years

BUTTON
35
35
90
68
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
110
35
165
68
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
25
200
175
233
power-plants
power-plants
0
20
2
1
1
NIL
HORIZONTAL

SLIDER
25
80
175
113
initial-population
initial-population
0
100
30
10
1
NIL
HORIZONTAL

PLOT
15
290
285
486
World Status
Time
Pop
0.0
100.0
0.0
111.0
true
true
"" ""
PENS
"trees" 1.0 0 -10899396 true "" ""
"people" 1.0 0 -2674135 true "" ""
"pollution" 1.0 0 -8630108 true "" ""

MONITOR
195
240
284
285
count people
count people
1
1
11

SLIDER
25
120
175
153
birth-rate
birth-rate
0
0.2
0.1
0.01
1
NIL
HORIZONTAL

SLIDER
25
240
175
273
polluting-rate
polluting-rate
0
5
3
1
1
NIL
HORIZONTAL

SLIDER
25
160
175
193
planting-rate
planting-rate
0
0.1
0.05
0.01
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This model is an examination of the fragile equilibrium of a predator-prey ecosystem. Populations of (1) people, (2) landscape elements and (3) swarms of airborne pollutant-agents compete for resources within an enclosed environment. Using this model, one can explore the behavior of the populations over time as they dynamically interact: the predators (pollution) and prey (people) can be compared over multiple generations as their populations demonstrate regular or irregular reproductive success.

Regular oscillations (cycles) of population size indicate balance and stability in the eco-system where, despite fluctuations, populations maintain themselves over time. Conversely, irregular oscillations indicate instability leading toward potential extinction of both co-dependant populations. The model establishes a negative feedback loop: predators inhibit the density of prey, and prey stimulates the density of predators.

## HOW IT WORKS

Power plants create pollution, which diffuses into the environment.  The health of people who are exposed to this pollution is adversely affected, reducing their chances of reproduction.  Those who can reproduce will create healthy children at a given birth rate.  People can also take some action to alleviate the pollution problem, which is represented in this model by planting trees.  The presence of trees helps curb the pollution.

Even without pollution, people's health naturally degrades over time, and they will eventually die of natural causes. To allow the populations of people to endure, people are cloned at a certain rate (see the BIRTH-RATE slider). A stable eco-system is achieved if pollutant-agent levels are held in check, and neither the populations of people nor landscape elements overtake the environment. As in all agent-based modeling, rules define the behavior of each individual agent in each population.

Rules:

Power plants are grid cells with a very high fixed pollution value (determined by the POLLUTING-RATE slider).

All grid cells have some pollution value, although it may be 0.  Pollution diffuses throughout the grid, so each grid shares part of its pollution value with its neighboring cells.  Since the pollution is fixed at a high amount at power plants, this has the effect that pollution emanates out from the power plants.

Trees, however, clean up pollution in the cell they are planted, and the neighboring cells.  Thus, they block the spread of pollution, by emanating low-pollution values.
Trees live for a set period of time and cannot reproduce.

Each time step (tick) of the model, people agents
1. move randomly to an adjacent cell
2. with some probability, they may plant a landscape element
3. if they are healthy enough, with some probability, they may reproduce (clone)
4. if their health has dropped to 0, they die.

## HOW TO USE IT

Press SETUP and GO to run the model.  (Note: the model will automatically stop when there are no people left in the world.)

INITIAL-POPULATION controls the number of people created at the start of the model run.

BIRTH-RATE controls the chance each person has of producing offspring.  The initial rate of 0.10 means they have a 10% chance each year of having a child, provided they are healthy enough.  People must have 4 health points or greater to reproduce, and they lose 0.1 points each year.  This means they have at most 10 years to reproduce in, and even less if they are hurt by pollution.  The default setting is very near "replacement rate" which means that on average, each person has one offspring.

PLANTING-RATE controls the change a person has of planting a tree each year.  The default setting of 0.05 means they have a 5% chance.  Trees live for 50 years and never reproduce themselves.  In this sense, they are not literally trees, but represent any pollution treatment mechanism.

At the start of the model, POWER-PLANTS controls how many power plants are created.

POLLUTION-RATE is the pollution that each power plant outputs in a year.  This pollution is then spread to the surrounding area.

The WORLD STATUS plot shows how many trees there are, how many people there are, and how much pollution there is, plotted over time as the model runs.

## THINGS TO NOTICE

What are the relationships between the amount of pollution, the number of people and the number of trees?  If there is an increase in the number of people, the number of trees will increase, but they will lag behind?  Run the model several times until you see that lag.  However, sometimes the number of trees reduces pollution so much that the people increase.  Which is the cause and which is the effect?

How does the location and grouping of the power plants effect the population over time? Is it better for the power plants to be closer, or further apart?

## THINGS TO TRY

With the default setting, populations will eventually die out, but the length of time they survive varies quite a bit.  Try increasing or decreasing the BIRTH-RATE just a little bit and runt he model several times.  How long does the population survive?

Reset the BIRTH-RATE to 0.1 and then do several runs while varying POWER-PLANTS and POLLUTION-RATE.

Set POWER-PLANTS to 0 and PLANTING-RATE to 0.  Run the model several times while varying BIRTH-RATE.  Why is there a spike at 50 ticks?  If you run the model several times you will see that it sometimes peaks at 50 ticks before dying out, but other times it will continue on for hundreds of more ticks and more than double the population at tick 50.

## EXTENDING THE MODEL

Make the pollution rate dependent upon the number of people.

## NETLOGO FEATURES

This model uses the DIFFUSE command to spread pollution.

## RELATED MODELS

This model is related to all of the other models in the "Urban Suite".

Another slightly related model, which examines environmental issues and the relationship between creatures and their environment, is the DaisyWorld model, found in the NetLogo models library.

## CREDITS AND REFERENCES

The original version of this model was developed during the Sprawl/Swarm Class at Illinois Institute of Technology in Fall 2006 under the supervision of Sarah Dunn and Martin Felsen, by the following student: Young Jang.  See http://www.sprawlcity.us/ for more information about the course.

Further modifications and refinements on the model were made by members of the Center for Connected Learning and Computer-Based Modeling before its release as an Urban Suite model.

The Urban Suite models were developed as part of the Procedural Modeling of Cities project, under the sponsorship of NSF ITR award 0326542, Electronic Arts & Maxis.

Please see the project web site ( http://ccl.northwestern.edu/cities/  ) for more information.
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

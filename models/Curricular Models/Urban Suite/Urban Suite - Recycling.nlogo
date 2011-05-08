turtles-own [ energy ]
patches-own [ resource-type ]

breed [ recyclers recycler ]
breed [ wastefuls wasteful ]

to setup
  clear-all
  set-default-shape turtles "person"
  create-recyclers num-recyclers [
    set color blue
  ]
  create-wastefuls num-wastefuls [
    set color red
  ]
  ask turtles [
    set size 1.5  ;; easier to see
    setxy random-pxcor random-pycor
    set energy max-stored-energy / 2
  ]
  ask patches [
    set resource-type "new"
    update-patch
  ]
  reset-ticks
end


to go
  ask recyclers [
    recycler-process-patch
  ]
  ask wastefuls
  [
    wasteful-process-patch
  ]
  ask turtles [
    move
    if (energy > max-stored-energy)
      [ set energy max-stored-energy ]
    if energy < 0
      [ die ]
  ]

  ifelse (show-energy?)
    [ ask turtles [ set label (round energy) ] ]
    [ ask turtles [ set label "" ] ]

  update-environment
  ask patches [ update-patch ]
  tick
end

to move  ;; turtle procedure
  let target-patch one-of neighbors
  if (agents-seek-resources?)
  [
    let candidate-moves neighbors with [ resource-type = "new" ]
    ifelse any? candidate-moves
      [ set target-patch one-of candidate-moves ]
    [
      set candidate-moves neighbors with [ resource-type = "recycled" ]
      if any? candidate-moves
        [ set target-patch one-of candidate-moves ]
    ]
  ]
  face target-patch
  move-to target-patch

  set energy (energy - 1)
end

to recycler-process-patch
  ifelse (resource-type = "new" )
  [
    if (energy <= max-stored-energy - 2)
      [ set energy energy + 2 ]
  ]
  [
    ifelse (resource-type = "recycled" )
    [
      if (energy <= max-stored-energy - 1)
      [
        set energy energy + 1
      ]
    ]
    [
      set energy energy - recycling-waste-cost
      set resource-type "recycled"
    ]
  ]
end

to wasteful-process-patch
  ifelse (resource-type = "new" )
  [
    if (energy <= max-stored-energy - 4)
    [
      set energy energy + 4
     set resource-type "waste"
    ]
  ]
  [
    if (resource-type = "recycled")
    [
      if (energy <= max-stored-energy - 2)
      [
        set energy energy + 2
        set resource-type "waste"
      ]
    ]
  ]
  ; if resource-type is "waste", then we gain nothing.

end

to update-patch
  ifelse (resource-type = "new")
    [ set pcolor green ]
  [ ifelse (resource-type = "recycled")
      [ set pcolor lime ]
      [ set pcolor yellow - 1 ]
  ]
end

to update-environment
  ask patches with [ resource-type = "recycled" ]
  [
    if random 100 < (resource-regeneration / 10)
      [ set resource-type "new" ]
  ]
  ; waste is less likely to be renewed naturally by the environment
  ; in this model, we arbitrarily assume 5 times less likely
  ask patches with [ resource-type = "waste" ]
  [
    if (random 5 = 0) and (random 100 < (resource-regeneration / 10))
      [ set resource-type "new" ]
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
195
10
625
461
17
17
12.0
1
10
1
1
1
0
1
1
1
-17
17
-17
17
1
1
1
ticks

BUTTON
55
30
130
63
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
105
75
180
108
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
5
120
180
153
num-recyclers
num-recyclers
0
50
25
1
1
NIL
HORIZONTAL

SLIDER
5
160
180
193
num-wastefuls
num-wastefuls
0
50
25
1
1
NIL
HORIZONTAL

SWITCH
5
410
180
443
show-energy?
show-energy?
0
1
-1000

SLIDER
5
255
180
288
recycling-waste-cost
recycling-waste-cost
0
2
0.5
0.25
1
NIL
HORIZONTAL

SLIDER
5
295
180
328
resource-regeneration
resource-regeneration
0
100
25
1
1
NIL
HORIZONTAL

SLIDER
5
215
180
248
max-stored-energy
max-stored-energy
10
100
50
5
1
NIL
HORIZONTAL

MONITOR
635
35
732
80
recyclers (blue)
count recyclers
17
1
11

MONITOR
740
35
840
80
wastefuls (red)
count wastefuls
17
1
11

SWITCH
0
350
185
383
agents-seek-resources?
agents-seek-resources?
1
1
-1000

PLOT
635
95
840
245
Population
ticks
# of developers
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"recyclers" 1.0 0 -13345367 true "" "plot count recyclers"
"wastefuls" 1.0 0 -2674135 true "" "plot count wastefuls"

PLOT
635
270
840
420
Land Use
ticks
percent
0.0
10.0
0.0
100.0
true
false
"" ""
PENS
"new" 1.0 0 -10899396 true "" "plot (count patches with [ resource-type = \"new\" ]) / count patches * 100"
"recycled" 1.0 0 -13840069 true "" "plot (count patches with [ resource-type = \"recycled\" ]) / count patches * 100"
"waste" 1.0 0 -4079321 true "" "plot (count patches with [ resource-type = \"waste\" ]) / count patches * 100"

BUTTON
5
75
77
108
go once
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

This model demonstrates the relationship of agents (people) trying to sustain their natural resource of land over the course of time.  In this simplified scenario, two types of developers exist: recyclers and wastefuls.

Recyclers are more conscious of the amount of land they use in order to gain energy (money, utility, etc). Therefore, they use the land but do not destroy it by using part of the resource and recycling everything. On the other hand, wasteful individuals consume the whole patch of land and afterwards dump their waste on it making it unusable to anyone until it has been restored by a recycler.

While the model is unrealistic in many respects, it brings up very important issues of sustainability in land and natural resource usage.  This model shows the energy and effort it takes to sustain an environment in order to make it continuously habitable for people. To maintain resources, a balance must be maintained between use and recycling/renewal.  While recycling leads to a better environment overall, it comes at an additional cost, since it often means not exploiting resources to their fullest potential, and the effort of recycling/renewing resources can take considerably energy.  Furthermore, it takes effort to clean up the mess left by the wasteful individuals.

All in all, it should serve as an excellent conversation piece, sparking debate regarding which assumptions are and aren't reasonable, and what the consequences of those assumptions are.

## HOW IT WORKS

The environment consists of a grid of cells, each of which may be in three discrete states: new (shown as green), recycled (shown as lime green), or waste (shown as yellow).  The cells are all initially in the "new" state.

There are two types of people agents - recyclers and wastefuls.  All agents have a property called "energy", which they collect from the environment.  All agents start with half of the maximum possible stored energy (defined by the MAX-STORED-ENERGY slider).  If an agent runs out of energy, they disappear.  This energy might be loosely construed as money or other economic resources, and when an agent runs out of funds, they are no longer a player in the land development game.

Each time step (tick) agents first process the cell they are on, and then move to one of the eight neighboring cells.  The movement may be random, or the agent may be more intelligent about seeking resources (if the PEOPLE-SEEK-RESOURCES? switch is turned on).

Wastefuls always totally exploit the cell they are processing, taking all the energy possible from the resource, and leaving the cell as "waste".  They gain 4 energy from "new" cells, and 2 energy from "recycled" cells, and nothing from "waste" cells.

Recyclers are conscientious, and only use half of the available resources in the cell, allowing the cell to rejuvenate the resources.  Thus, "new" cells stay "new" after being processed, but the recycler only gains 2 energy.  Recyclers also only take half (1 energy) from "recycled" cells, leaving the cell still in a recycled state.  When a recycler encounters a "waste" cell, they take the effort to recycle it, but this actually costs them energy (controlled by the RECYCLING-WASTE-COST slider), rather than gaining them anything.

Additionally, resources are randomly regenerated in the environment (at a rate controlled by the RESOURCE-REGENERATION slider).  This process causes cells change back into the "new" state.  Cells in the "recycled" state are five times more likely to change back to the new state than those in the "waste" state.  Although the factor of five is arbitrary, it is logical that squares that have been exploited/wasted are slower to regenerate.

## HOW TO USE IT

Press the SETUP button to initialize the world.  All cells are set to the "new" state, and recyclers and wastefuls are placed on random cells.

Press the GO button to run the model.  Press GO ONCE to run a single time step (tick).

Use the NUM-RECYCLERS and NUM-WASTEFULS sliders to control the initial populations of recyclers and wastefuls.

The MAX-STORED-ENERGY slider determines the maximum amount of energy that an agent can store up.  If processing a cell would cause the agent to have more than this maximum amount of energy, the agent does not process the cell.

The RECYCLING-WASTE-COST slider determines how much energy a recycler loses when it turns a "waste" cell into a "recycled" cell.

The RESOURCE-REGENERATION controls the rate at which the environment naturally regenerates resources.  A value of 0 means that the environment does not regenerate, and a value of 100 means that the environment regenerates fairly quickly.

If the AGENTS-SEEK-RESOURCES? switch is ON, then agents look at the eight neighboring cells, and first try to move to a "new" cell.  If none exists, they try to move to a "recycled" cell.  If none exists, then they move to a random cell.  If AGENTS-SEEK-RESOURCES? is switched OFF, then agents always move to a random neighboring cell.

If SHOW-ENERGY? is switched ON, then the energy for each individual agent is overlaid on the view.

The current number of recyclers and number of wastefuls are shown in the "RECYCLERS (BLUE)" and "WASTEFULS (RED)" monitors.

The populations over time are plotted in the POPULATION plot.

The "LAND USE" plot records the percentage of the cells that are in each state: new, recycled, and waste.

## THINGS TO NOTICE

In this model there is a notion of carrying-capacity.  That is, how many agents can the environment support in a relatively stable manner?  The answer depends on which type of agent is being discussed.  In this model the environment can support an arbitrarily large number of recyclers without any difficulty.  However, there is a limit on the number of wastefuls that the environment can support.  The carrying-capacity of wastefuls is dependent on several factors, particularly the RESOURCE-REGENERATION parameter, and the number of recycler agents that are in the world.

## THINGS TO TRY

Set MAX-STORED-ENERGY to 50, RECYCLING-WASTE-COST to 0.5, and RESOURCE-REGENERATION to 25.  Run the model with 25 recyclers and 25 wastefuls for 10,000 ticks.  How many of each type survived?  Now run it again for 10,000 ticks, this time with 50 recyclers and 25 wastefuls.  How many of each survived?

Try running the model with no recyclers, and 50 wastefuls.  Look at the LAND USE plot.  The cells all start as "new", but as time passes, the number of "waste" cells increases and passes the number of "new" cells, rising as high as perhaps 75% of the land use.  After this peak, the number of "waste" cells falls, until it is back down below 25% again.  Look at this plot in comparison to the POPULATION plot above it -- how do the two relate to each other?

## EXTENDING THE MODEL

The MAX-STORED-ENERGY constraint exists because without a cap on the amount of energy agents can store, it generally gave the wastefuls too strong an advantage in the world.  They would soak up vast quantities of energy (they acquire energy at a rate that is twice that of the recyclers), and then the recyclers would die off before the wastefuls had expended their reserves.  See if you can find another way to keep the model somewhat in balance, instead of having a MAX-STORED-ENERGY cap.

## NETLOGO FEATURES

The SHOW-ENERGY? feature of this model takes advantage of the fact that every turtle (and every patch) in NetLogo has a "label" property.  This means that you can assign some text to be displayed next to a turtle.  In this particular case, we show the energy level with the code:  ASK TURTLES [ SET LABEL (ROUND ENERGY) ]

This first rounds the energy to the nearest whole number (so that we don't get long labels like "48.25"), and then sets each turtle's label to be the result.

## RELATED MODELS

This model is related to all of the other models in the "Urban Suite".

An early version of this model was inspired and based on the Cooperation model in the NetLogo models library.  The Cooperation model discusses how cooperation might have arisen in the course of biological evolution.

## CREDITS AND REFERENCES

The original version of this model was developed during the Sprawl/Swarm Class at Illinois Institute of Technology in Fall 2006 under the supervision of Sarah Dunn and Martin Felsen, by the following students: Anita Phetkhamphou and Tidza Causevic .  See http://www.sprawlcity.us/ for more information about this course.

Further modifications and refinements were made by members of the Center for Connected Learning and Computer-Based Modeling before releasing it as an Urban Suite model.

The Urban Suite models were developed as part of the Procedural Modeling of Cities project, under the sponsorship of NSF ITR award 0326542, Electronic Arts & Maxis.

Please see the project web site ( http://ccl.northwestern.edu/cities/ ) for more information.
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

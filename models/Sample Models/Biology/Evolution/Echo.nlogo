globals [
  energy-threshold    ;; minimum amount of energy needed by creatures to reproduce
  all-tags            ;; sorted list of all possible tags
]

patches-own [
  well      ;; the amount of resources a patch has
  max-well  ;; the max amount of resources for that patch
]

turtles-own [
  energy    ;; current resource level
  offense   ;; offense tag
  defense   ;; defense tag
  mating    ;; mating tag (can be thought of as identifying a species)
]

to setup
  clear-all
  set energy-threshold 100
  setup-all-tags-list
  setup-patches
  setup-creatures
  reset-ticks
end

;; Creates a list of all possible tags (of length 1, 2, or 3)
to setup-all-tags-list
  set all-tags []
  let tag-elements ["a" "b" "c"]
  foreach tag-elements
  [
    let i ?
    set all-tags lput (list ?) all-tags
    foreach tag-elements [
      let j ?
      set all-tags fput (list i ?) all-tags
      foreach tag-elements
        [ set all-tags fput (list i j ?) all-tags ]
    ]
  ]
end

to setup-patches
  ask patches [
    set max-well random 50
    set well max-well
    recolor-patch
  ]
end

to setup-creatures
  create-turtles init-num-creatures
  [
    setxy random-xcor random-ycor
    set offense one-of all-tags
    set defense one-of all-tags
    set mating one-of all-tags
    set size 0.5   ;; since the patch size is so big make the turtles smaller
    set energy random energy-threshold
    recolor-turtle
  ]
end

to recolor-turtle  ;; turtle procedure
  set color tag-color mating
  set label reduce [word ?1 ?2] (sentence offense "." defense)
end

to-report tag-color [tag]
  report 5 + 10 * position tag all-tags
end

to recolor-patch  ;; patch procedure
   set pcolor scale-color green well 0 100
end

to go
  if not any? turtles [ stop ]
  ask patches [ replenish ]
  ask turtles [ grab-resources ]
  ask turtles [ fight ]
  ask turtles [ reproduce ]
  ask turtles [ move ]
  ask turtles [ replicate ]
  ask patches [ recolor-patch ]
  tick
end

to replenish  ;; patch procedure
  if well < max-well [
    set well well + ((max-well - well) * (replenish-speed / 100))
  ]
end

;; Get resources from the environment, if you need them.  You don't fight for these,
;; but instead they are shared with other agents in the same location.
to grab-resources    ;; turtle procedure
  if (energy < energy-threshold) and (well > 0) [
    set energy energy + ( well / count turtles-here )
    set well well - ( well / count turtles-here )
  ]
end

;; Try to get resources from another agent at your location.
;; This is done by comparing tags.
;; We compare id numbers to prevent the same pair from fighting
;; twice during the same tick.
to fight  ;; turtle procedure
  let candidates turtles-here with [self > myself]
  if any? candidates [ match-off-def self one-of candidates ]
end

;; if there are any other agents at your location, reproduce with them
;; We compare id numbers to prevent the same pair from fighting
;; twice during the same tick.
to reproduce   ;; turtle procedure
  let candidates turtles-here with [self > myself]
  if any? candidates [ reproduce-match self one-of candidates ]
end

to move  ;; turtle procedure
  set energy energy - 1
  ifelse energy < 20
    [ die ]
    [ rt random-normal 0 20
      fd random-float 1 ]
end

to replicate   ;; creature procedure
  if energy > energy-threshold and random-float 100 < replicate-chance
  [
    set energy energy / 2  ;; give half of your energy to your offspring
    hatch 1
  ]
end

;; Creates offspring from mating, including selective mating
to reproduce-match [agent1 agent2]
  if ( [energy] of agent1 > energy-threshold ) and
     ( [energy] of agent2 > energy-threshold )
  [
    if (not selective-mating?) or
        ( ( match-score [mating] of agent1 [offense] of agent2 > mating-selectivity ) and
          ( match-score [mating] of agent2 [offense] of agent1 > mating-selectivity ) )
    [
      hatch 1
      [
        set mating mutate cross [mating] of agent1 [mating] of agent2
        set offense mutate cross [offense] of agent1 [offense] of agent2
        set defense mutate cross [defense] of agent1 [defense] of agent2
        setxy random-xcor random-ycor
        set energy random-normal 50 20
        recolor-turtle
      ]
      ask agent1 [ set energy energy / 2 ]
      ask agent2 [ set energy energy / 2 ]
    ]
  ]
end

;; This crosses tags during reproduction by walking through
;; each position of the string.  When the letters from each tag differ at a position
;; there is an equal chance of it being either one.  When the tags are different lengths,
;; the additional letters are just appended on to the tage of the offspring.
to-report cross [tag1 tag2]
  if length tag1 > length tag2
    [ report sentence cross sublist tag1 0 length tag2 tag2 sublist tag1 length tag2 length tag1 ]
  if length tag2 > length tag1
    [ report sentence cross tag1 sublist tag2 0 length tag1 sublist tag2 length tag1 length tag2 ]
  report (map [one-of (list ?1 ?2)] tag1 tag2)
end

;; Mutates a given string
to-report mutate [tag]
  report map [ifelse-value (random-float 100 >= mutation-rate)
                [?] [one-of remove ? ["a" "b" "c"]]]
             tag
end

;; This procedure determines how resources are transferred between
;; agents.  The offense tag of an agent1 is matched with the defense
;; tag of agent 2, and the offense tag of agent2 is matched with the
;; defense tag of agent1.
to match-off-def [agent1 agent2]

  ;; match respective offense and defense tags
  let a1a2 match-score [offense] of agent1  [defense] of agent2
  let a2a1 match-score [offense] of agent2  [defense] of agent1

  ;; scale resulting scores so that all values are positive
  set a1a2 a1a2 + 6
  set a2a1 a2a1 + 6

  ;; Determine how much resource is transferred between the agents.
  ;; Each agent takes from the other based on the relative magnitude
  ;; of its match score
  set a1a2 (a1a2 / 12) * ([energy] of agent2)
  set a2a1 (a2a1 / 12) * ([energy] of agent1)

  ;; You win some, you lose some.
  ask agent1 [ set energy energy + a1a2 - a2a1 ]
  ask agent2 [ set energy energy + a2a1 - a1a2 ]

end

;; This reports a score that represents how well tag 1 matches tag 2.
;; The match takes places letter by letter -- the first
;; letters of each tag are compared, then the second letters, and so on.
;; An adjustment is made for unequal length tags.The resulting "match-score"
;; is ultimately used to help determine how resources are transferred between agents
;; or the extent to which a potential mate matches an agent's mating conditions
to-report match-score [tag1 tag2]
  if length tag1 > length tag2
    [ report (length tag2 - length tag1) + match-score sublist tag1 0 length tag2 tag2 ]
  if length tag2 > length tag1
    [ report (length tag1 - length tag2) + match-score tag1 sublist tag2 0 length tag1 ]
  report sum (map [ifelse-value (?1 = ?2) [2] [-2]] tag1 tag2)
end
@#$#@#$#@
GRAPHICS-WINDOW
513
10
963
481
5
5
40.0
1
10
1
1
1
0
1
1
1
-5
5
-5
5
1
1
1
ticks
30.0

SLIDER
12
23
188
56
init-num-creatures
init-num-creatures
25
1000
400
25
1
NIL
HORIZONTAL

BUTTON
190
23
279
56
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
282
23
366
56
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
329
102
504
135
replenish-speed
replenish-speed
0
5
1
0.1
1
NIL
HORIZONTAL

SLIDER
329
68
504
101
replicate-chance
replicate-chance
0
10
0
0.1
1
%
HORIZONTAL

PLOT
4
481
505
618
Speciation
ticks
NIL
0.0
50.0
0.0
1.0
true
true
"" ""
PENS
"species" 1.0 0 -2674135 true "" "plot length remove-duplicates [mating] of turtles"
"offense" 1.0 0 -13345367 true "" "plot length remove-duplicates [offense] of turtles"
"defense" 1.0 0 -2064490 true "" "plot length remove-duplicates [defense] of turtles"

PLOT
345
342
505
479
Species Counts
NIL
NIL
0.0
100.0
0.0
1.0
true
false
"set-plot-x-range 0 length all-tags" ""
PENS
"default" 1.0 1 -16777216 true "" ";; The HISTOGRAM primitive doesn't support giving different bars\n;; different colors, so we roll our own histogramming code here.\nplot-pen-reset\nforeach all-tags [\n  set-plot-pen-color tag-color ?\n  plot count turtles with [mating = ?]\n]\n"

SLIDER
329
136
504
169
mutation-rate
mutation-rate
0
10
0
0.1
1
%
HORIZONTAL

SWITCH
137
102
322
135
selective-mating?
selective-mating?
1
1
-1000

PLOT
4
175
505
340
Populations
ticks
NIL
0.0
50.0
0.0
1.0
true
true
"" "set-current-plot-pen \"largest species\"\nlet biggest-mating modes [mating] of turtles\nifelse (length biggest-mating > 0)\n[\n  set-plot-pen-color first modes [color] of turtles with [first biggest-mating = mating]\n  plot count turtles with [first biggest-mating = mating]\n  set-current-plot-pen \"2nd largest\"\n  let second-mating modes [mating] of turtles with [first biggest-mating != mating]\n  ifelse (length second-mating > 0 )\n  [\n    set-plot-pen-color first modes [color] of turtles with [first second-mating = mating]\n    plot count turtles with [first second-mating = mating]\n  ]\n  [ plot 0 ]\n ]\n [ plot 0 ]\n"
PENS
"largest species" 1.0 0 -13345367 true "" ""
"2nd largest" 1.0 0 -6459832 true "" ""

PLOT
4
342
181
479
Offense Tags
NIL
NIL
0.0
39.0
0.0
1.0
true
false
"set-plot-x-range 0 length all-tags" ""
PENS
"default" 1.0 1 -16777216 true "" "histogram [position offense all-tags] of turtles"

PLOT
183
342
343
479
Defense Tags
NIL
NIL
0.0
39.0
0.0
1.0
true
false
"set-plot-x-range 0 length all-tags" ""
PENS
"default" 1.0 1 -16777216 true "" "histogram [position defense all-tags] of turtles"

SLIDER
137
136
322
169
mating-selectivity
mating-selectivity
-6
6
0
1
1
NIL
HORIZONTAL

MONITOR
407
266
501
311
total pop.
count turtles
17
1
11

@#$#@#$#@
## WHAT IS IT?

Echo is a model about the evolution of fitness, adapted from John Holland's book _Hidden Order_ (1995).  It can be used to facilitate experiments in a variety of domains where an agent's fitness varies with its context.  This particular adaptation of Echo  has a biological flavor and refers to the agents as "creatures", and groups of agents with identical mating preferences as "species."

With Echo, Holland attempted to codify intuitions about complex adaptive systems into a more rigorous and abstract model.  Holland was inspired by notions from ecological systems research, especially with regards to the dynamics of niches.  How does a change in the population dynamics of an ecosystem create new niches and destroy old ones?  Echo was an attempt to create an abstract model that could facilitate understanding of the interplay of evolutionary and ecological processes.  In Echo, different niches are created where creatures can consume, transform, and exchange resources, but this model is not necessarily just a biological model --- it can also be viewed as a model of other phenomena like the emergence of new markets.

It should be noted that Echo as laid out in _Hidden Order_ is not meant to be a specific model, but rather a framework for building models.  There are a number of mechanisms that are not fully specified in _Hidden Order_ and the description includes multiple different mechanisms that can be implemented.  As a result, there is no model which is "the Echo model" there can only be "an Echo model."  This NetLogo Echo model is one particular instantiation of this framework.

## HOW IT WORKS

Each agent, or creature, has two components -- a reservoir to keep resources it collects, and a chromosome that contains its genetic material and defines its capabilities.

A creature expends resources moving around the world looking for resources.  A creature can gain resources either directly from its environment, or from another creature at its current location.

The amount of resources gained from its environment depends on the amount of available resources and the number of other creatures at its current location.

The amount of resources gained from another creature depends on the match between the creatures' chromosomes.  More precisely, a creature's chromosome is compromised of three "tags" --- an offense tag, a defense tag, and a mating condition tag -- which are represented by strings of "a's", "b's" and "c's".  The amount transferred from one creature to another depends on how closely the first creature's offense tag matches the second creature's defense tag.  However, all interactions, or "fights", between creatures are two-way exchanges --- i.e., both agents match their offense tags to the other's defense tag.  Consequently, resources can be either gained or lost on any given interaction with another agent.  For example, offense tag "aaa" of creature 1 to defense tag "aaa" of creature 2 would be a perfect match. It would entitle creature 1 to all of creature 2 resources, but the second creature could possibly get some, or even all of the resources back based on the match of his offense tag to the other agent's defense tag.

If an agent's resource level drops below a minimum threshold, it dies.

An agent can reproduce only if it has acquired a minimum level of resources.  Reproduction occurs in two ways:  1) simple replication, as determined by the REPLICATE-CHANCE slider, in which case the replicated agent is identical to the parent, and 2) mating with other agents.  By default, an agent can mate with any other agent, resulting in an offspring that contains a mixed set of elements, or a "cross", of both parent's chromosomes.  If the SELECTIVE-MATING? switch is enabled, an agent will not mate with just anyone, but instead only mate with agents whose offense tags match its own mating condition tag.  The similarity between the mating tag and offense tag necessary for a match to occur is determined by the MATING-SELECTIVITY slider.  With either process, it is possible that the characteristics of offspring are not an exact copy or cross of the parent's chromosomes.  Mutations can occur at a rate determined by the MUTATION-RATE slider.

## HOW TO USE IT

Click the SETUP button to setup the world, then click the GO button.  The agents will begin to move, exchange resources, and reproduce.  The sliders allow you to change each of the parameters of the model as described below.  The plots provide an update on the composition of the populations of agents.

The agents are colored by mating tags.  Identical mating tags share the same color; mating tags that are close are similar in color; and so on.  Each creature has its offense and defense tags as its label in the format "offense-tag.defense-tag".  For example, a label of "aab.bcc" represents a creature with an offense tag of "aab" and defense tag of "bcc."

The model includes the following parameters:

NUM-CREATURES --- The number of agents with which to start the simulation.

REPLENISH-SPEED --- The speed at which the resources in the environment replenish.

REPLICATE-CHANCE --- The chance that a creature with sufficient energy will replicate.

SELECTIVE-MATING? --- If 'Off' it is possible for agents to mate with any other agent that has enough energy to reproduce.  If 'On' agents will only mate with other agents whose offense tag matches their own mating tag.

MATING-SELECTIVITY? --- Determines how similar a mating tag and offense tag must be in order for two agents to mate.  Higher, positive numbers are more selective.  '0' is the midpoint.

MUTATION-RATE --- The rate at which a letter in the offense or defense tags of offspring may change during replication.

Here are descriptions of all the plots in the model:

"Offense Tags" --- A histogram of the number of each offense tag currently in existence.

"Defense Tags" --- A histogram of the number of each defense tag currently in existence.

"Species Counts" --- A histogram of the number of each species currently alive.  A species is defined as a group of agents that share an identical mating tag.

"Populations" --- This shows the number of creatures in the two largest species currently in the world.

"Speciation" --- This shows the current total number of different species, the total number of different offense tags, and the total number of different defense tags in the world.

Some other options are described below in THINGS TO TRY.

## THINGS TO NOTICE

Populations often become dynamically stable with several species at oscillating population levels.

Increasing the initial NUM-CREATURES slider does not necessarily result in a larger stable population, indeed the opposite may occur as creatures compete for limited resources.

## THINGS TO TRY

Change REPLENISH-SPEED to examine the impact of making resources more scarce.

See what impact increasing or lowering the MUTATION-RATE and/or switching SELECTIVE-MATING? 'On' has on the number of species that ultimately survive in situation with dynamically stable populations.

## EXTENDING THE MODEL

Add a mechanism that allows selective interaction for the exchange of resources, in addition to selective mating.

Resources could be more nuanced than the simple "energy" variable.  Certain locations in the environment could give off certain resources.  For example, every creature might need at least one "c" resource to live, but only certain wells might give resources of type "c".  Furthermore, resources could be transformed.  For example, one "a" and two "b's" could be turned into a "c" for some cost.

Currently the exchange of resources between two agents is a fight --- a zero-sum exchange.  However, this interaction could be changed to an interaction where both parties can become better off --- a situation that resembles a trade more than a fight.

Holland describes several additional extensions in some detail in Chapter 3 of _Hidden Order_.

## EXTENDING THE MODEL (CATERPILLAR-FLY-ANT)

Holland discusses an interesting example from nature that can be imitated in Echo -- the Caterpillar-Fly-Ant triangle.  The triangle refers to the stable, triangular relation between the three different species: Species 1 --  Flies lay eggs on caterpillars and become prey through their larva.  Species 2 -- Ants are aggressive predators on the flies, but not of caterpillars.  Species 3 -- Caterpillars produce nectar on their skin which ants find very attractive.  The more ants around a caterpillar the less likely it is to be attacked by a predatory fly.  This can be represented in Echo with the following tags:

     ant - offense: aaa, defense: cb
     fly - offense: aab, defense: aaa
     caterpillar - offense: c, defense: aab

Notice that the ants' offense tag matches perfectly with the fly's defense tag, making it a perfect predatory on the fly.  Likewise, the fly's offense tag matches perfectly on the caterpillar making it a perfect predator on the caterpillar.  The ant consumes the nectar of the caterpillar but does not prey on the caterpillar; thus, the ant and caterpillar have tags that result in an exchange of resources.

Try replicating this scenario.  You'll probably want to set SELECTIVE-MATING? to on and MATING-SELECTIVITY to a high value such as 5.  Is it possible to achieve a stable relationship between the three species?  (Are changes to the parameters and/or the rules of the model necessary...?)

To get you started, here's some code for creating a roughly equal number of the three species.  (You may want to experiment with different initial proportions.)  The mating tags are completely different, to discourage interspecies mating.

     create-turtles init-num-creatures
     [
       let choice random 3
       if choice = 0  ; ant
       [
         set offense ["a" "a" "a"]
         set defense ["c" "b"]
         set mating  ["a" "a" "a"]
       ]
       if choice = 1  ; fly
       [
         set offense ["a" "a" "b"]
         set defense ["a" "a" "a"]
         set mating  ["b" "b" "b"]
       ]
       if choice = 2  ; caterpillar
       [
         set offense ["c"]
         set defense ["a" "a" "b"]
         set mating  ["c" "c" "c"]
       ]
     ]


## NETLOGO FEATURES

Tags are represented by lists of strings, so the code is heavily dependent on lists. `map`, `reduce`, and `foreach` are used to do list processing.

## RELATED MODELS

Sample Models > Biology > Evolution > (entire section)  
Sample Models > Biology > Rabbits Grass Weeds  
Sample Models > Biology > Wolf Sheep Predation

## CREDITS AND REFERENCES

Holland, J. (1995). _Hidden Order: How Adaptation Builds Complexity_. Addison-Wesley, Reading, Massachusetts.
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

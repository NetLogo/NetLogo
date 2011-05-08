;; agents have a probablity to reproduce and a strategy
turtles-own [ PTR cooperate-with-same? cooperate-with-different? ]

globals [
  ;; the remaining variables support the replication of published experiments
  meet                  ;; how many interactions occurred this turn
  meet-agg              ;; how many interactions occurred through the run
  last100meet           ;; meet for the last 100 ticks
  meetown               ;; what number of individuals met someone of their own color this turn
  meetown-agg           ;; what number of individuals met someone of their own color throughout the run
  last100meetown        ;; meetown for the last 100 ticks
  meetother             ;; what number of individuals met someone of a different color this turn
  meetother-agg         ;; what number of individuals met someone of a different color throughout the run
  last100meetother      ;; meetother for the last 100 ticks
  coopown               ;; how many interactions this turn were cooperating with the same color
  coopown-agg           ;; how many interactions throughout the run were cooperating with the same color
  last100coopown        ;; coopown for the last 100 ticks
  coopother             ;; how many interactions this turn were cooperating with a different color
  coopother-agg         ;; how many interactions throughout the run were cooperating with a different color
  defother              ;; how many interactions this turn were defecting with a different color
  defother-agg          ;; how many interactions throughout the run were defecting with a different color
  last100defother       ;; defother for the last 100 ticks
  last100cc             ;; how many cooperate-cooperate genotypes have there been in the last 100 ticks
  last100cd             ;; how many cooperate-defect genotypes have there been in the last 100 ticks
  last100dc             ;; how many defect-cooperate genotypes have there been in the last 100 ticks
  last100dd             ;; how many defect-defect genotypes have there been in the last 100 ticks
  last100consist-ethno  ;; how many interactions consistent with ethnocentrism in the last 100 ticks
  last100coop           ;; how many interactions have been cooperation in the last 100 ticks
]

to setup-empty
  clear-all
  initialize-variables
  reset-ticks
end

;; creates a world with an agent on each patch
to setup-full
  clear-all
  initialize-variables
  ask patches [ create-turtle ]
  reset-ticks
end

to initialize-variables
  ;; initialize all the variables
  set meetown 0
  set meetown-agg 0
  set meet 0
  set meet-agg 0
  set coopown 0
  set coopown-agg 0
  set defother 0
  set defother-agg 0
  set meetother 0
  set meetother-agg 0
  set coopother 0
  set coopother-agg 0
  set last100dd []
  set last100cd []
  set last100cc []
  set last100dc []
  set last100coopown []
  set last100defother []
  set last100consist-ethno []
  set last100meetown []
  set last100meetother []
  set last100meet []
  set last100coop []
end

;; creates a new agent in the world
to create-turtle  ;; patch procedure
  sprout 1 [
    set color random-color
    ;; determine the strategy for interacting with someone of the same color
    set cooperate-with-same? (random-float 1.0 < immigrant-chance-cooperate-with-same)
    ;; determine the strategy for interacting with someone of a different color
    set cooperate-with-different? (random-float 1.0 < immigrant-chance-cooperate-with-different)
    ;; change the shape of the agent on the basis of the strategy
    update-shape
  ]
end

to-report random-color
  report one-of [red blue yellow green]
end

;; this is used to clear stats that change between each tick
to clear-stats
  set meetown 0
  set meet 0
  set coopown 0
  set defother 0
  set meetother 0
  set coopother 0
end

;; the main routine
to go
  clear-stats     ;; clear the turn based stats
  immigrate       ;; new agents immigrate into the world

  ;; reset the probability to reproduce
  ask turtles [ set PTR initial-PTR ]

  ;; have all of the agents interact with other agents if they can
  ask turtles [ interact ]
  ;; now they reproduce
  ask turtles [ reproduce ]
  death           ;; kill some of the agents
  update-stats    ;; update the states for the aggregate and last 100 ticks
  tick
end

;; random individuals enter the world on empty cells
to immigrate
  let empty-patches patches with [not any? turtles-here]
  ;; we can't have more immigrants than there are empty patches
  let how-many min list immigrants-per-day (count empty-patches)
  ask n-of how-many empty-patches [ create-turtle ]
end

to interact  ;; turtle procedure

  ;; interact with Von Neumann neighborhood
  ask turtles-on neighbors4 [
    ;; the commands inside the ASK are written from the point of view
    ;; of the agent being interacted with.  To refer back to the agent
    ;; that initiated the interaction, we use the MYSELF primitive.
    set meet meet + 1
    set meet-agg meet-agg + 1
    ;; do one thing if the individual interacting is the same color as me
    if color = [color] of myself [
      ;; record the fact the agent met someone of the own color
      set meetown meetown + 1
      set meetown-agg meetown-agg + 1
      ;; if I cooperate then I reduce my PTR and increase my neighbors
      if [cooperate-with-same?] of myself [
        set coopown coopown + 1
        set coopown-agg coopown-agg + 1
        ask myself [ set PTR PTR - cost-of-giving ]
        set PTR PTR + gain-of-receiving
      ]
    ]
    ;; if we are different colors we take a different strategy
    if color != [color] of myself [
      ;; record stats on encounters
      set meetother meetother + 1
      set meetother-agg meetother-agg + 1
      ;; if we cooperate with different colors then reduce our PTR and increase our neighbors
      ifelse [cooperate-with-different?] of myself [
        set coopother coopother + 1
        set coopother-agg coopother-agg + 1
        ask myself [ set PTR PTR - cost-of-giving ]
        set PTR PTR + gain-of-receiving
      ]
      [
        set defother defother + 1
        set defother-agg defother-agg + 1
      ]
    ]
  ]
end

;; use PTR to determine if the agent gets to reproduce
to reproduce  ;; turtle procedure
  ;; if a random variable is less than the PTR the agent can reproduce
  if random-float 1.0 < PTR [
    ;; find an empty location to reproduce into
    let destination one-of neighbors4 with [not any? turtles-here]
    if destination != nobody [
      ;; if the location exists hatch a copy of the current turtle in the new location
      ;;  but mutate the child
      hatch 1 [
        move-to destination
        mutate
      ]
    ]
  ]
end

;; modify the children of agents according to the mutation rate
to mutate  ;; turtle procedure
  ;; mutate the color
  if random-float 1.0 < mutation-rate [
    let old-color color
    while [color = old-color]
      [ set color random-color ]
  ]
  ;; mutate the strategy flags;
  ;; use NOT to toggle the flag
  if random-float 1.0 < mutation-rate [
    set cooperate-with-same? not cooperate-with-same?
  ]
  if random-float 1.0 < mutation-rate [
    set cooperate-with-different? not cooperate-with-different?
  ]
  ;; make sure the shape of the agent reflects its strategy
  update-shape
end

to death
  ;; check to see if a random variable is less than the death rate for each agent
  ask turtles [
    if random-float 1.0 < death-rate [ die ]
  ]
end

;; make sure the shape matches the strategy
to update-shape
  ;; if the agent cooperates with same they are a circle
  ifelse cooperate-with-same? [
    ifelse cooperate-with-different?
      [ set shape "circle" ]    ;; filled in circle (altruist)
      [ set shape "circle 2" ]  ;; empty circle (ethnocentric)
  ]
  ;; if the agent doesn't cooperate with same they are a square
  [
    ifelse cooperate-with-different?
      [ set shape "square" ]    ;; filled in square (cosmopolitan)
      [ set shape "square 2" ]  ;; empty square (egoist)
  ]
end

;; this routine calculates a moving average of some stats over the last 100 ticks
to update-stats
  set last100dd        shorten lput (count turtles with [shape = "square 2"]) last100dd
  set last100cc        shorten lput (count turtles with [shape = "circle"]) last100cc
  set last100cd        shorten lput (count turtles with [shape = "circle 2"]) last100cd
  set last100dc        shorten lput (count turtles with [shape = "square"]) last100dc
  set last100coopown   shorten lput coopown last100coopown
  set last100defother  shorten lput defother last100defother
  set last100meetown   shorten lput meetown last100meetown
  set last100coop      shorten lput (coopown + coopother) last100coop
  set last100meet      shorten lput meet last100meet
  set last100meetother shorten lput meetother last100meetother
end

;; this is used to keep all of the last100 lists the right length
to-report shorten [the-list]
  ifelse length the-list > 100
    [ report butfirst the-list ]
    [ report the-list ]
end

;; these are used in the BehaviorSpace experiments

to-report meetown-percent
  report meetown / max list 1 meet
end
to-report meetown-agg-percent
  report meetown-agg / max list 1 meet-agg
end
to-report coopown-percent
  report coopown / max list 1 meetown
end
to-report coopown-agg-percent
  report coopown-agg / max list 1 meetown-agg
end
to-report defother-percent
  report defother / max list 1 meetother
end
to-report defother-agg-percent
  report defother-agg / max list 1 meetother-agg
end
to-report consist-ethno-percent
  report (defother + coopown) / (max list 1 meet )
end
to-report consist-ethno-agg-percent
  report (defother-agg + coopown-agg) / (max list 1 meet-agg )
end
to-report coop-percent
  report (coopown + coopother) / (max list 1 meet )
end
to-report coop-agg-percent
  report (coopown-agg + coopother-agg) / (max list 1 meet-agg)
end
to-report cc-count
  report sum last100cc / max list 1 length last100cc
end
to-report cd-count
  report sum last100cd / max list 1 length last100cd
end
to-report dc-count
  report sum last100dc / max list 1 length last100dc
end
to-report dd-count
  report sum last100dd / max list 1 length last100dd
end
to-report cc-percent
  report cc-count / (max list 1 (cc-count + cd-count + dc-count + dd-count))
end
to-report cd-percent
  report cd-count / (max list 1 (cc-count + cd-count + dc-count + dd-count))
end
to-report dc-percent
  report dc-count / (max list 1 (cc-count + cd-count + dc-count + dd-count))
end
to-report dd-percent
  report dd-count / (max list 1 (cc-count + cd-count + dc-count + dd-count))
end
to-report last100coopown-percent
  report sum last100coopown / max list 1 sum last100meetown
end
to-report last100defother-percent
  report sum last100defother / max list 1 sum last100meetother
end
to-report last100consist-ethno-percent
  report (sum last100defother + sum last100coopown) / max list 1 sum last100meet
end
to-report last100meetown-percent
  report sum last100meetown / max list 1 sum last100meet
end
to-report last100coop-percent
  report sum last100coop / max list 1 sum last100meet
end
@#$#@#$#@
GRAPHICS-WINDOW
323
10
792
500
-1
-1
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
0
50
0
50
1
1
1
ticks
30

SLIDER
5
150
171
183
mutation-rate
mutation-rate
0.0
1.0
0.0050
0.0010
1
NIL
HORIZONTAL

SLIDER
5
184
171
217
death-rate
death-rate
0.0
1.0
0.1
0.05
1
NIL
HORIZONTAL

SLIDER
5
218
171
251
immigrants-per-day
immigrants-per-day
0.0
100.0
1
1.0
1
NIL
HORIZONTAL

SLIDER
172
150
318
183
initial-PTR
initial-PTR
0.0
1.0
0.12
0.01
1
NIL
HORIZONTAL

SLIDER
172
184
318
217
cost-of-giving
cost-of-giving
0.0
1.0
0.01
0.01
1
NIL
HORIZONTAL

SLIDER
172
218
318
251
gain-of-receiving
gain-of-receiving
0.0
1.0
0.03
0.01
1
NIL
HORIZONTAL

BUTTON
20
37
128
70
setup empty
setup-empty
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
221
37
294
70
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

PLOT
6
323
318
525
Strategy Counts
time
count
0.0
10.0
0.0
1.0
true
true
"" ""
PENS
"CC" 1.0 0 -10899396 true "" "plotxy ticks count turtles with [shape = \"circle\"]"
"CD" 1.0 0 -2674135 true "" "plotxy ticks count turtles with [shape = \"circle 2\"]"
"DC" 1.0 0 -4079321 true "" "plotxy ticks count turtles with [shape = \"square\"]"
"DD" 1.0 0 -16777216 true "" "plotxy ticks count turtles with [shape = \"square 2\"]"

BUTTON
130
37
219
70
setup full
setup-full
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
5
252
318
285
immigrant-chance-cooperate-with-same
immigrant-chance-cooperate-with-same
0.0
1.0
0.5
0.01
1
NIL
HORIZONTAL

SLIDER
5
286
318
319
immigrant-chance-cooperate-with-different
immigrant-chance-cooperate-with-different
0.0
1.0
0.5
0.01
1
NIL
HORIZONTAL

TEXTBOX
35
82
294
144
Circles cooperate with same color\nSquares defect with same color\nFilled-in shapes cooperate with different color\nEmpty shapes defect with different color\n
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

This model, due to Robert Axelrod and Ross A. Hammond, suggests that "ethnocentric" behavior can evolve under a wide variety of conditions, even when there are no native "ethnocentrics" and no way to differentiate between agent types.  Agents compete for limited space via Prisoner Dilemma's type interactions. "Ethnocentric" agents treat agents within their group more beneficially than those outside their group.  The model includes a mechanism for inheritance (genetic or cultural) of strategies.

## HOW IT WORKS

Each agent has three traits: a) color, b) whether they cooperate with same colored agents, and c) whether they cooperate with different colored agents.  An "ethnocentric" agent is one which cooperates with same colored agents, but does not cooperate with different colored agents. An "altruist" cooperates with all agents, while an "egoist" cooperates with no one.  A "cosmopolitan" cooperates with agents of a different color but not of their own color.

At each time step, the following events occur:

1. Up to IMMIGRANTS-PER-DAY, new agents appear in random locations with random traits.

2. Agents start with an INITIAL-PTR (Potential-To-Reproduce) chance of reproducing.  Each pair of adjacent agents interact in a one-move Prisoner's Dilemma in which each chooses whether or not to help the other.  They either gain, or lose some of their potential to reproduce.

3. In random order, each agent is given a chance to reproduce.  Offspring have the same traits as their parents, with a MUTATION-RATE chance of each trait mutating.  Agents are only allowed to reproduce if their is an empty space next to them.  Each agent's birth-rate is reset to the INITIAL-PTR.

4. The agent has a DEATH-RATE chance of dying, making room for future offspring and immigrants.

## HOW TO USE IT

To prepare the simulation for a new run, press SETUP EMPTY.  Press GO to start the simulation running, press GO again to stop it.

SETUP FULL will allow you to start with a full world of random agents.

COST-OF-GIVING indicates how much it costs an agent to cooperate with another agent.

GAIN-OF-RECEIVING indicates how much an agent gains if another agent cooperates with them.

IMMIGRANT-CHANCE-COOPERATE-WITH-SAME indicates the probability that an immigrating agent will have the COOPERATE-WITH-SAME? variable set to true.

IMMIGRANT-CHANCE-COOPERATE-WITH-DIFFERENT indicates the probability that an immigrating agent will have the COOPERATE-WITH-DIFFERENT? variable set to true.

The STRATEGY COUNTS plot tracks the number of agents that utilize a given cooperation strategy:

CC --- People who cooperate with everyone
CD --- People who cooperate only with people of the same type
DD --- People who do not cooperate with anyone
DC --- People who only cooperate with people of different types

## THINGS TO NOTICE

Agents appear as circles if they cooperate with the same color.  They are filled in if they also cooperate with a different color (altruists) or empty if they do not (ethnocentrics).  Agents are squares if they do not cooperate with the same color.  The agents are filled in if they cooperate with a different color (cosmopolitans) or empty if they do not (egoists).

Observe the interaction along the edge of a group of ethnocentric agents, and non-ethnocentric agents.  What behaviors do you see?  Is one more stable?  Does one expand into the other group?

Observer the STRATEGY COUNTS plot.  Does one strategy occur more than others?  What happens when we change the model?

## THINGS TO TRY

Set the IMMIGRANT-CHANCE-COOPERATE sliders both to 1.0.  This means there are only altruists created.  Do ethnocentrics and other strategies ever evolve?  Do they ever out compete the altruists?

Change the values of COST-OF-GIVING and GAIN-OF-RECEIVING and observe the effects on the model and the level of ethnocentricity.

This model comes with a group of BehaviorSpace experiments defined.  You can access them by choosing BehaviorSpace on the Tools menu.  These are the original experiments that Axelrod and Hammond ran to test the robustness of this model. These experiments vary lots of parameters like the size of the world, IMMIGRANTS-PER-DAY and COST-OF-GIVING.  These experiments are detailed at   http://www-personal.umich.edu/~axe/Shared_Files/Axelrod.Hammond/index.htm

## EXTENDING THE MODEL

Add more colors to the model.  Does the behavior change?

Make some patches richer than others, so that agents on them have a higher chance of reproducing.  Distribute this advantage across the world in different ways such as randomly, in blobs, or in quarters.

Tag patches with a color.  distribute the colors across the world in different ways: blobs, randomly, in discrete quarters.  Agents use the patch color under other agents to determine whether to cooperate with them or not.

## NETLOGO FEATURES

To ensure fairness, the agents should run in random order.  Agentsets in NetLogo are always in random order, so no extra code is needed to achieve this.

## RELATED MODELS

 * Segregation
 * PD Basic

## CREDITS AND REFERENCES

This model is a NetLogo version of the ethnocentrism model presented by Robert Axelrod at Northwestern University at the NICO (Northwestern Institute on Complex Systems) conference on October 25th, 2003.

See also Ross A. Hammond and Robert Axelrod, The Evolution of Ethnocentrism, http://www-personal.umich.edu/~axe/research/AxHamm_Ethno.pdf
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
setup-full repeat 150 [ go ]
@#$#@#$#@
@#$#@#$#@
<experiments>
  <experiment name="experiment104" repetitions="10" runMetricsEveryStep="false">
    <setup>setup-empty</setup>
    <go>go</go>
    <timeLimit steps="2000"/>
    <metric>coopown-percent</metric>
    <metric>defother-percent</metric>
    <metric>consist-ethno-percent</metric>
    <metric>meetown-percent</metric>
    <metric>coop-percent</metric>
    <metric>last100coopown-percent</metric>
    <metric>last100defother-percent</metric>
    <metric>last100consist-ethno-percent</metric>
    <metric>last100meetown-percent</metric>
    <metric>last100coop-percent</metric>
    <metric>cc-percent</metric>
    <metric>cd-percent</metric>
    <metric>dc-percent</metric>
    <metric>dd-percent</metric>
    <enumeratedValueSet variable="gain-of-receiving">
      <value value="0.03"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-ptr">
      <value value="0.12"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrants-per-day">
      <value value="1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-same">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="mutation-rate">
      <value value="0.0050"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="cost-of-giving">
      <value value="0.01"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-different">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="death-rate">
      <value value="0.1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pxcor">
      <value value="50"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pycor">
      <value value="50"/>
    </enumeratedValueSet>
  </experiment>
  <experiment name="experiment105" repetitions="10" runMetricsEveryStep="false">
    <setup>setup-empty</setup>
    <go>go</go>
    <timeLimit steps="2000"/>
    <metric>coopown-percent</metric>
    <metric>defother-percent</metric>
    <metric>consist-ethno-percent</metric>
    <metric>meetown-percent</metric>
    <metric>coop-percent</metric>
    <metric>last100coopown-percent</metric>
    <metric>last100defother-percent</metric>
    <metric>last100consist-ethno-percent</metric>
    <metric>last100meetown-percent</metric>
    <metric>last100coop-percent</metric>
    <metric>cc-percent</metric>
    <metric>cd-percent</metric>
    <metric>dc-percent</metric>
    <metric>dd-percent</metric>
    <enumeratedValueSet variable="gain-of-receiving">
      <value value="0.03"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-ptr">
      <value value="0.12"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrants-per-day">
      <value value="1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-same">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="mutation-rate">
      <value value="0.0050"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="cost-of-giving">
      <value value="0.01"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-different">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="death-rate">
      <value value="0.1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pxcor">
      <value value="100"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pycor">
      <value value="100"/>
    </enumeratedValueSet>
  </experiment>
  <experiment name="experiment106" repetitions="10" runMetricsEveryStep="false">
    <setup>setup-empty</setup>
    <go>go</go>
    <timeLimit steps="4000"/>
    <metric>coopown-percent</metric>
    <metric>defother-percent</metric>
    <metric>consist-ethno-percent</metric>
    <metric>meetown-percent</metric>
    <metric>coop-percent</metric>
    <metric>last100coopown-percent</metric>
    <metric>last100defother-percent</metric>
    <metric>last100consist-ethno-percent</metric>
    <metric>last100meetown-percent</metric>
    <metric>last100coop-percent</metric>
    <metric>cc-percent</metric>
    <metric>cd-percent</metric>
    <metric>dc-percent</metric>
    <metric>dd-percent</metric>
    <enumeratedValueSet variable="gain-of-receiving">
      <value value="0.03"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-ptr">
      <value value="0.12"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrants-per-day">
      <value value="1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-same">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="mutation-rate">
      <value value="0.0050"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="cost-of-giving">
      <value value="0.01"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-different">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="death-rate">
      <value value="0.1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pxcor">
      <value value="50"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pycor">
      <value value="50"/>
    </enumeratedValueSet>
  </experiment>
  <experiment name="experiment107" repetitions="10" runMetricsEveryStep="false">
    <setup>setup-empty</setup>
    <go>go</go>
    <timeLimit steps="1000"/>
    <metric>coopown-percent</metric>
    <metric>defother-percent</metric>
    <metric>consist-ethno-percent</metric>
    <metric>meetown-percent</metric>
    <metric>coop-percent</metric>
    <metric>last100coopown-percent</metric>
    <metric>last100defother-percent</metric>
    <metric>last100consist-ethno-percent</metric>
    <metric>last100meetown-percent</metric>
    <metric>last100coop-percent</metric>
    <metric>cc-percent</metric>
    <metric>cd-percent</metric>
    <metric>dc-percent</metric>
    <metric>dd-percent</metric>
    <enumeratedValueSet variable="gain-of-receiving">
      <value value="0.03"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-ptr">
      <value value="0.12"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrants-per-day">
      <value value="1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-same">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="mutation-rate">
      <value value="0.0050"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="cost-of-giving">
      <value value="0.01"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-different">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="death-rate">
      <value value="0.1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pxcor">
      <value value="50"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pycor">
      <value value="50"/>
    </enumeratedValueSet>
  </experiment>
  <experiment name="experiment108" repetitions="10" runMetricsEveryStep="false">
    <setup>setup-empty</setup>
    <go>go</go>
    <timeLimit steps="2000"/>
    <metric>coopown-percent</metric>
    <metric>defother-percent</metric>
    <metric>consist-ethno-percent</metric>
    <metric>meetown-percent</metric>
    <metric>coop-percent</metric>
    <metric>last100coopown-percent</metric>
    <metric>last100defother-percent</metric>
    <metric>last100consist-ethno-percent</metric>
    <metric>last100meetown-percent</metric>
    <metric>last100coop-percent</metric>
    <metric>cc-percent</metric>
    <metric>cd-percent</metric>
    <metric>dc-percent</metric>
    <metric>dd-percent</metric>
    <enumeratedValueSet variable="gain-of-receiving">
      <value value="0.03"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-ptr">
      <value value="0.12"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrants-per-day">
      <value value="1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-same">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="mutation-rate">
      <value value="0.0050"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="cost-of-giving">
      <value value="0.01"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-different">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="death-rate">
      <value value="0.1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pxcor">
      <value value="25"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pycor">
      <value value="25"/>
    </enumeratedValueSet>
  </experiment>
  <experiment name="experiment109" repetitions="10" runMetricsEveryStep="false">
    <setup>setup-empty</setup>
    <go>go</go>
    <timeLimit steps="2000"/>
    <metric>coopown-percent</metric>
    <metric>defother-percent</metric>
    <metric>consist-ethno-percent</metric>
    <metric>meetown-percent</metric>
    <metric>coop-percent</metric>
    <metric>last100coopown-percent</metric>
    <metric>last100defother-percent</metric>
    <metric>last100consist-ethno-percent</metric>
    <metric>last100meetown-percent</metric>
    <metric>last100coop-percent</metric>
    <metric>cc-percent</metric>
    <metric>cd-percent</metric>
    <metric>dc-percent</metric>
    <metric>dd-percent</metric>
    <enumeratedValueSet variable="gain-of-receiving">
      <value value="0.03"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-ptr">
      <value value="0.12"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrants-per-day">
      <value value="1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-same">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="mutation-rate">
      <value value="0.0050"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="cost-of-giving">
      <value value="0.02"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-different">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="death-rate">
      <value value="0.1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pxcor">
      <value value="50"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pycor">
      <value value="50"/>
    </enumeratedValueSet>
  </experiment>
  <experiment name="experiment110" repetitions="10" runMetricsEveryStep="false">
    <setup>setup-empty</setup>
    <go>go</go>
    <timeLimit steps="2000"/>
    <metric>coopown-percent</metric>
    <metric>defother-percent</metric>
    <metric>consist-ethno-percent</metric>
    <metric>meetown-percent</metric>
    <metric>coop-percent</metric>
    <metric>last100coopown-percent</metric>
    <metric>last100defother-percent</metric>
    <metric>last100consist-ethno-percent</metric>
    <metric>last100meetown-percent</metric>
    <metric>last100coop-percent</metric>
    <metric>cc-percent</metric>
    <metric>cd-percent</metric>
    <metric>dc-percent</metric>
    <metric>dd-percent</metric>
    <enumeratedValueSet variable="gain-of-receiving">
      <value value="0.03"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-ptr">
      <value value="0.12"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrants-per-day">
      <value value="1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-same">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="mutation-rate">
      <value value="0.0025"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="cost-of-giving">
      <value value="0.01"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-different">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="death-rate">
      <value value="0.1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pxcor">
      <value value="50"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pycor">
      <value value="50"/>
    </enumeratedValueSet>
  </experiment>
  <experiment name="experiment111" repetitions="10" runMetricsEveryStep="false">
    <setup>setup-empty</setup>
    <go>go</go>
    <timeLimit steps="2000"/>
    <metric>coopown-percent</metric>
    <metric>defother-percent</metric>
    <metric>consist-ethno-percent</metric>
    <metric>meetown-percent</metric>
    <metric>coop-percent</metric>
    <metric>last100coopown-percent</metric>
    <metric>last100defother-percent</metric>
    <metric>last100consist-ethno-percent</metric>
    <metric>last100meetown-percent</metric>
    <metric>last100coop-percent</metric>
    <metric>cc-percent</metric>
    <metric>cd-percent</metric>
    <metric>dc-percent</metric>
    <metric>dd-percent</metric>
    <enumeratedValueSet variable="gain-of-receiving">
      <value value="0.03"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-ptr">
      <value value="0.12"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrants-per-day">
      <value value="1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-same">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="mutation-rate">
      <value value="0.01"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="cost-of-giving">
      <value value="0.01"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-different">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="death-rate">
      <value value="0.1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pxcor">
      <value value="50"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pycor">
      <value value="50"/>
    </enumeratedValueSet>
  </experiment>
  <experiment name="experiment113" repetitions="10" runMetricsEveryStep="false">
    <setup>setup-empty</setup>
    <go>go</go>
    <timeLimit steps="2000"/>
    <metric>coopown-percent</metric>
    <metric>defother-percent</metric>
    <metric>consist-ethno-percent</metric>
    <metric>meetown-percent</metric>
    <metric>coop-percent</metric>
    <metric>last100coopown-percent</metric>
    <metric>last100defother-percent</metric>
    <metric>last100consist-ethno-percent</metric>
    <metric>last100meetown-percent</metric>
    <metric>last100coop-percent</metric>
    <metric>cc-percent</metric>
    <metric>cd-percent</metric>
    <metric>dc-percent</metric>
    <metric>dd-percent</metric>
    <enumeratedValueSet variable="gain-of-receiving">
      <value value="0.03"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="initial-ptr">
      <value value="0.12"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrants-per-day">
      <value value="2"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-same">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="mutation-rate">
      <value value="0.0050"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="cost-of-giving">
      <value value="0.01"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="immigrant-chance-cooperate-with-different">
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="death-rate">
      <value value="0.1"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pxcor">
      <value value="50"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="max-pycor">
      <value value="50"/>
    </enumeratedValueSet>
  </experiment>
</experiments>
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

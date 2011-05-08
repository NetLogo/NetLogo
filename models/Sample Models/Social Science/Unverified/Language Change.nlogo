;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Variables
;;;;;;;;;;;;;;;;;;;;;;;;;

breed [nodes node]

nodes-own [
  state            ; current grammar state (ranges from 0 to 1)
  orig-state       ; remember this so we can reset the states
  spoken-state     ; output of agent's speech (1 or 0)
]

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup
;;;;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  set-default-shape nodes "circle"
  ask patches [ set pcolor gray ]
  repeat num-nodes [ make-node ]
  distribute-grammars
  create-network
  repeat 100 [ layout ]
  reset-ticks
end

;;; initialize nodes so a proportion start out with grammar 1
to distribute-grammars
  ;; wipe everyone's grammars back to 0
  ask nodes [ set state 0 ]
  ;; then ask a select few to switch on to 1
  ask n-of ((percent-grammar-1 / 100) * num-nodes) nodes
    [ set state 1.0 ]
  ask nodes [
    set orig-state state     ; used for resetting states
    set spoken-state state   ; initial spoken state, for first timestep
    update-color
  ]
end

to create-network
  ;; make the initial network of two nodes and an edge
  let partner nobody
  let first-node one-of nodes
  let second-node one-of nodes with [self != first-node]
  ;; make the first edge
  ask first-node [ create-link-with second-node [ set color white ] ]
  ;; randomly select unattached node to add to network
  let new-node one-of nodes with [not any? link-neighbors]
  ;; and connect it to a partner already in the network
  while [new-node != nobody] [
    set partner find-partner
    ask new-node [ create-link-with partner [ set color white ] ]
    layout
    set new-node one-of nodes with [not any? link-neighbors]
  ]
end

;;; creates a new node, initializes its state
to make-node
  create-nodes 1 [
    ;; start in random position near edge of world
    rt random-float 360
    fd max-pxcor
    set size 2
    set state 0.0
  ]
end

to update-color
  set color scale-color red state 0 1
end

to reset-nodes
  clear-all-plots
  ask nodes [
    set state orig-state
    update-color
  ]
  reset-ticks
end

to redistribute-grammars
  clear-all-plots
  distribute-grammars
  reset-ticks
end

;;; reports a string of the agent's initial grammar - "one" or "zero"
to-report orig-grammar-string
  report ifelse-value (orig-state = 1.0) ["one"] ["zero"]
end

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Go
;;;;;;;;;;;;;;;;;;;;;;;;;

to go
  ask nodes [ communicate-via update-algorithm ]
  ask nodes [ update-color ]
  tick
end

to communicate-via [ algorithm ] ; node procedure
  ;; Discrete Grammars ;;
  ifelse (algorithm = "threshold") [
    listen-threshold
    ] [
    ifelse (algorithm = "individual") [
      listen-individual
    ] [
      ;; Probabilistic Grammar ;;
      ;; speak and ask all neighbors to listen
      if (algorithm = "reward") [
        speak
        ask link-neighbors [
          listen [spoken-state] of myself
        ]
  ]]]
end

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Speaking & listening
;;;;;;;;;;;;;;;;;;;;;;;;;

to listen-threshold ; node procedure
  let grammar-one-sum sum [state] of link-neighbors
  ifelse grammar-one-sum >= (count link-neighbors * threshold-val) [
    set state 1
  ][
    ;; else - you don't have enough neighbors with grammar 1, and 1 is not a sink state, then change to 0
    if not sink-state-1? [ set state 0 ]
  ]
end

to listen-individual ; node procedure
  set state [state] of one-of link-neighbors
end

to speak ; node procedure
  ;; *FILTERING* ;;
  ;; alpha is the level of bias in favor of grammar 1
  ;; alpha is constant for all nodes. value of 0.025 works best with logistic
  ;; this is the logistic equation
  ;; it looks complicated in order to input values [0,1] and output to [0,1]
  if logistic? [
    let gain (alpha + 0.1) * 20
    let filter-val 1 / (1 + exp (- (gain * state - 1) * 5))
    ifelse random-float 1.0 <= filter-val [
      set spoken-state 1
    ][
      set spoken-state 0
    ]
  ]
  ;; for probabilistic learners who only have bias for grammar 1
  ;; no preference for discrete grammars (ie, no logistic)
  if not logistic? [
    ;; slope needs to be greater than 1... arbitrarily set to 1.5
    ;; when state is >= 2/3, the biased-val would be greater than 1
    let biased-val 1.5 * state
    if biased-val > 1 [ set biased-val 1 ]
    ;; let biased-val (state / (state + .9 * (1 - state)))
    ifelse random-float 1.0 <= biased-val [
      set spoken-state 1
    ][
      set spoken-state 0
    ]
  ]
end

;;; listening uses a linear reward/punish algorithm
to listen [heard-state] ; node procedure
  let gamma 0.01 ; for now gamma is the same for all nodes
  ;; choose a grammar state to be in
  ifelse random-float 1.0 <= state [
    ;; if grammar 1 was heard
    ifelse heard-state = 1 [
      set state state + (gamma * (1 - state))
    ][
      set state (1 - gamma) * state
    ]
  ][
    ;; if grammar 0 was heard
    ifelse heard-state = 0 [
      set state state * (1 - gamma)
    ][
      set state gamma + state * (1 - gamma)
    ]
  ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Making the network
;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This code is borrowed from Lottery Example, from the Code Examples
;;; section of the Models Library.
;;; The idea behind this procedure is a bit tricky to understand.
;;; Basically we take the sum of the sizes of the turtles, and
;;; that's how many "tickets" we have in our lottery.  Then we pick
;;; a random "ticket" (a random number).  Then we step through the
;;; turtles to figure out which turtle holds that ticket.
to-report find-partner
  let pick random-float sum [count link-neighbors] of (nodes with [any? link-neighbors])
  let partner nobody
  ask nodes [
    ;; if there's no winner yet...
    if partner = nobody [
      ifelse count link-neighbors > pick [
        set partner self
      ][
        set pick pick - (count link-neighbors)
      ]
    ]
  ]
  report partner
end

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Layout
;;;;;;;;;;;;;;;;;;;;;;;;;

to layout
  layout-spring (turtles with [any? link-neighbors]) links 0.4 6 1
end

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Highlighting
;;;;;;;;;;;;;;;;;;;;;;;;;

to highlight
  ifelse mouse-inside?
    [ do-highlight ]
    [ undo-highlight ]
  display
end

;;; remove any previous highlights
to undo-highlight
  clear-output
  ask nodes [ update-color ]
  ask links [ set color white ]
end

to do-highlight
  let highlight-color blue
  let min-d min [distancexy mouse-xcor mouse-ycor] of nodes
  ;; get the node closest to the mouse
  let the-node one-of nodes with [any? link-neighbors and distancexy mouse-xcor mouse-ycor = min-d]
  ;; get the node that was previously the highlight-color
  let highlighted-node one-of nodes with [color = highlight-color]
  if the-node != nobody and the-node != highlighted-node [
    ;; highlight the chosen node
    ask the-node [
      undo-highlight
      output-print word "original grammar state: "  orig-grammar-string
      output-print word "state: " precision state 5
      set color highlight-color
      ;; highlight edges connecting the chosen node to its neighbors
      ask my-links [ set color cyan - 1 ]
      ;; highlight neighbors
      ask link-neighbors [ set color blue + 1 ]
    ]
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
354
10
637
314
45
45
3.0
1
10
1
1
1
0
0
0
1
-45
45
-45
45
1
1
1
ticks
30

PLOT
10
375
595
550
Mean state of agents in the network
Time
State
0.0
100.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "plot mean [state] of nodes"

BUTTON
10
10
115
43
setup everything
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
205
10
280
43
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
120
10
200
43
layout
layout\ndisplay
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
10
295
110
328
reset states
reset-nodes
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
257
324
347
357
NIL
highlight
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

OUTPUT
355
317
638
372
12

SLIDER
10
50
118
83
num-nodes
num-nodes
2
100
40
1
1
NIL
HORIZONTAL

SLIDER
10
90
178
123
percent-grammar-1
percent-grammar-1
0
100
25
1
1
%
HORIZONTAL

CHOOSER
186
90
320
135
update-algorithm
update-algorithm
"individual" "threshold" "reward"
2

SLIDER
184
256
318
289
alpha
alpha
0
0.05
0.025
0.0050
1
NIL
HORIZONTAL

BUTTON
10
333
150
368
NIL
redistribute-grammars
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
186
140
320
173
threshold-val
threshold-val
0
1
0.3
0.05
1
NIL
HORIZONTAL

SWITCH
186
179
320
212
sink-state-1?
sink-state-1?
0
1
-1000

SWITCH
184
218
319
251
logistic?
logistic?
0
1
-1000

TEXTBOX
12
145
177
286
*threshold-val and sink state only apply for \"individual\" and \"threshold\" updating algorithms\n\n*when logistic? is off, there's a built-in bias towards grammar 1\n\n*alpha only applies for the \"reward\" updating algorithm, when logistic? is on
10
0.0
0

BUTTON
205
50
280
83
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

This model explores how the properties of language users and the structure of their social networks can affect the course of language change.

In this model, there are two linguistic variants in competition within the social network --- one variant generated by grammar 0 and the other generated by grammar 1. Language users interact with each other based on who they are connected to in the network. At each iteration, everyone speaks by passing an utterance using either grammar 0 or grammar 1 to their neighbors in the network. Individuals then listen to their neighbors by changing their grammars based on what they received as input from the speakers.

## HOW IT WORKS

The networks in this model are constructed through the process of "preferential attachment" in which individuals enter the network one by one, and prefer to connect to those language users who already have many connections. This leads to the emergence of a few "hubs", or language users who are very well connected; most other language users have very few connections.

There are three different options to control how language users listen and learn from their neighbors, listed in the UPDATE-ALGORITHM chooser. For two of these options, "individual" and "threshold", language users can only access one grammar at a time. Those that can only access grammar 1 are white in color, and those that can access only grammar 0 are black. For the third option, "reward", each grammar is associated with a weight, which determines the language user's probability of accessing that grammar. Because there are only two grammars in competition here, the weights are represented with a single value - the weight of grammar 1. The color of the nodes represent this probability; the larger the weight of grammar 1, the lighter the node.

- Individual: Learners choose one of their neighbors randomly, and adopt that neighbor's grammar.

- Threshold: Learners adopt grammar 1 if some proportion of their neighbors are already using grammar 1. This proportion is set with the THRESHOLD-VAL slider. For example, if THRESHOLD-VAL is 0.30, then a learner will adopt grammar 1 if at least 30% of his neighbors have grammar 1.

- Reward: Learners update their probability of using one grammar or the other. In this algorithm, if an individual hears an utterance from grammar 1, the individual's weight of grammar 1 is increased, and they will be more likely to access that grammar in the next iteration. Similarly, hearing an utterance from grammar 0 increases the likelihood of accessing grammar 0 in the next iteration.

## HOW TO USE IT

The NUM-NODES slider determines the number of nodes to be included in the network population. PERCENT-GRAMMAR-1 determines the proportion of these nodes which will be initialized to use grammar 1. The remaining nodes will be initialized to use grammar 0.

Pressing the SETUP-EVERYTHING button generates a new network based on NUM-NODES and PERCENT-GRAMMAR-1.

The REDISTRIBUTE-GRAMMARS button keeps the same proportion of nodes with grammar 0 or 1, but reassigns who has these initial grammars. For example, if 20% of nodes are initialized with grammar 1, clicking REDISTRIBUTE-GRAMMARS will assign grammar 1 to a new sample of 20% of the population.

Press RESET-STATES to reinitialize all nodes to their original grammars. This allows you to run the model multiple times without generating a new network structure.

The LAYOUT button attempts to move the nodes around to make the structure of the network easier to see.

When the HIGHLIGHT button is pressed, roll over a node in the network to see who that node is connected to. Additionally, information about that node's initial and current grammar state will be displayed in the output area.

Press GO ONCE to allow all nodes to "speak" and "listen" once, according to the algorithm in the UPDATE-ALGORITHM dropdown menu (see the above section for more about these options). Press GO for this procedure to repeat continually.

The SINK-STATE-1? switch applies only for the "individual" and "threshold" updating algorithms. If on, once someone adopts grammar 1, they can never go back to grammar 0.

The LOGISTIC? switch applies only for the "reward" updating algorithm. If on, a speaker's probability of using one of the grammars while speaking is pushed to the extremes (closer to 0% or 100%), based on the output of the logistic function (see 
http://en.wikipedia.org/wiki/Logistic_function ).

The ALPHA slider also applies only for the "reward" updating algorithm, and only when LOGISTIC? is turned on. ALPHA represents a bias in favor of grammar 1. Probabilities are pushed to the extremes, then shifted toward selecting grammar 1. The larger the value of ALPHA, the more likely a language user is to speak using grammar 1.

The plot "Mean state of agents in the network" calculates the average weight of grammar 1 for all nodes in the network, at each iteration.

## THINGS TO NOTICE

Over time, language users tend to arrive at using just one grammar all of the time. However, they may not all converge to the same grammar. It is possible for sub-groups to emerge, which may be seen as the formation of different dialects.

## THINGS TO TRY

Under what conditions is it possible to get one grammar to spread through the entire network? Try manipulating PERCENT-GRAMMAR-1, the updating algorithm, and the various other parameters. Does the number of nodes matter too?

## EXTENDING THE MODEL

Whether or not two language users interact with each other is determined by the network structure. How would the model behave if language users were connected by a small-world network rather than a preferential attachment network?

In this model, only two grammars are in competition in the network. Try extending the model to allow competition between three grammars.

The updating algorithm currently has agents updating asynchronously. Currently, the grammar may spread one step or several within one tick, depending on the links. Try implementing synchronous updating.

Regardless of the updating algorithm, language users always start out using one grammar categorically (that is, with a weight of 0 or 1). Edit the model to allow some language users to be initialized to an intermediate weight (i.e., 0.5)

## NETLOGO FEATURES

Networks are represented using turtles (nodes) and links.  In NetLogo, both turtles and links are agents.

## RELATED MODELS

Networks --> Preferential Attachment

## CREDITS AND REFERENCES

This model was also described in Troutman, C., Clark, B., and Goldrick, M. "Social Networks and Intraspeaker Variation During Periods of Language Change." Presented at the 31st Annual Penn Linguistics Colloquium 2007, University of Pennsylvania.  
http://ling.northwestern.edu/~cet883/PLC_TroutmanClarkGoldrick.pdf
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

link
true
0
Line -7500403 true 150 0 150 300

link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

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
NetLogo 5.0beta2
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
<experiments>
  <experiment name="threshold" repetitions="1000" runMetricsEveryStep="false">
    <setup>setup</setup>
    <go>go</go>
    <timeLimit steps="20"/>
    <metric>mean [state] of nodes</metric>
    <enumeratedValueSet variable="num-nodes">
      <value value="100"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="threshold-val">
      <value value="0.05"/>
      <value value="0.1"/>
      <value value="0.15"/>
      <value value="0.2"/>
      <value value="0.5"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="categorical?">
      <value value="&quot;threshold&quot;"/>
    </enumeratedValueSet>
  </experiment>
  <experiment name="one run" repetitions="1" runMetricsEveryStep="true">
    <setup>reset-nodes</setup>
    <go>go</go>
    <timeLimit steps="1000"/>
    <metric>mean [state] of nodes</metric>
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

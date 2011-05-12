globals [
  ;;number of turtles with each strategy
  num-random
  num-cooperate
  num-defect
  num-tit-for-tat
  num-unforgiving
  num-unknown

  ;;number of interactions by each strategy
  num-random-games
  num-cooperate-games
  num-defect-games
  num-tit-for-tat-games
  num-unforgiving-games
  num-unknown-games

  ;;total score of all turtles playing each strategy
  random-score
  cooperate-score
  defect-score
  tit-for-tat-score
  unforgiving-score
  unknown-score
]

turtles-own [
  score
  strategy
  defect-now?
  partner-defected? ;;action of the partner
  partnered?        ;;am I partnered?
  partner           ;;WHO of my partner (nobody if not partnered)
  partner-history   ;;a list containing information about past interactions
                    ;;with other turtles (indexed by WHO values)
]


;;;;;;;;;;;;;;;;;;;;;;
;;;Setup Procedures;;;
;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  store-initial-turtle-counts ;;record the number of turtles created for each strategy
  setup-turtles ;;setup the turtles and distribute them randomly
  reset-ticks
end

;;record the number of turtles created for each strategy
;;The number of turtles of each strategy is used when calculating average payoffs.
;;Slider values might change over time, so we need to record their settings.
;;Counting the turtles would also work, but slows the model.
to store-initial-turtle-counts
  set num-random n-random
  set num-cooperate n-cooperate
  set num-defect n-defect
  set num-tit-for-tat n-tit-for-tat
  set num-unforgiving n-unforgiving
  set num-unknown n-unknown
end

;;setup the turtles and distribute them randomly
to setup-turtles
  make-turtles ;;create the appropriate number of turtles playing each strategy
  setup-common-variables ;;sets the variables that all turtles share
end

;;create the appropriate number of turtles playing each strategy
to make-turtles
  crt num-random [ set strategy "random" set color gray - 1 ]
  crt num-cooperate [ set strategy "cooperate" set color red ]
  crt num-defect [ set strategy "defect" set color blue ]
  crt num-tit-for-tat [ set strategy "tit-for-tat" set color lime ]
  crt num-unforgiving [ set strategy "unforgiving" set color turquoise - 1 ]
  crt num-unknown [set strategy "unknown" set color magenta ]
end

;;set the variables that all turtles share
to setup-common-variables
  ask turtles [
    set score 0
    set partnered? false
    set partner nobody
    setxy random-xcor random-ycor
  ]
  setup-history-lists ;;initialize PARTNER-HISTORY list in all turtles
end

;;initialize PARTNER-HISTORY list in all turtles
to setup-history-lists
  let num-turtles count turtles

  let default-history [] ;;initialize the DEFAULT-HISTORY variable to be a list

  ;;create a list with NUM-TURTLE elements for storing partner histories
  repeat num-turtles [ set default-history (fput false default-history) ]

  ;;give each turtle a copy of this list for tracking partner histories
  ask turtles [ set partner-history default-history ]
end


;;;;;;;;;;;;;;;;;;;;;;;;
;;;Runtime Procedures;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to go
  clear-last-round
  ask turtles [ partner-up ]                        ;;have turtles try to find a partner
  let partnered-turtles turtles with [ partnered? ]
  ask partnered-turtles [ select-action ]           ;;all partnered turtles select action
  ask partnered-turtles [ play-a-round ]
  do-scoring
  tick
end

to clear-last-round
  let partnered-turtles turtles with [ partnered? ]
  ask partnered-turtles [ release-partners ]
end

;;release partner and turn around to leave
to release-partners
  set partnered? false
  set partner nobody
  rt 180
  set label ""
end

;;have turtles try to find a partner
;;Since other turtles that have already executed partner-up may have
;;caused the turtle executing partner-up to be partnered,
;;a check is needed to make sure the calling turtle isn't partnered.

to partner-up ;;turtle procedure
  if (not partnered?) [              ;;make sure still not partnered
    rt (random-float 90 - random-float 90) fd 1     ;;move around randomly
    set partner one-of (turtles-at -1 0) with [ not partnered? ]
    if partner != nobody [              ;;if successful grabbing a partner, partner up
      set partnered? true
      set heading 270                   ;;face partner
      ask partner [
        set partnered? true
        set partner myself
        set heading 90
      ]
    ]
  ]
end

;;choose an action based upon the strategy being played
to select-action ;;turtle procedure
  if strategy = "random" [ act-randomly ]
  if strategy = "cooperate" [ cooperate ]
  if strategy = "defect" [ defect ]
  if strategy = "tit-for-tat" [ tit-for-tat ]
  if strategy = "unforgiving" [ unforgiving ]
  if strategy = "unknown" [ unknown ]
end

to play-a-round ;;turtle procedure
  get-payoff     ;;calculate the payoff for this round
  update-history ;;store the results for next time
end

;;calculate the payoff for this round and
;;display a label with that payoff.
to get-payoff
  set partner-defected? [defect-now?] of partner
  ifelse partner-defected? [
    ifelse defect-now? [
      set score (score + 1) set label 1
    ] [
      set score (score + 0) set label 0
    ]
  ] [
    ifelse defect-now? [
      set score (score + 5) set label 5
    ] [
      set score (score + 3) set label 3
    ]
  ]
end

;;update PARTNER-HISTORY based upon the strategy being played
to update-history
  if strategy = "random" [ act-randomly-history-update ]
  if strategy = "cooperate" [ cooperate-history-update ]
  if strategy = "defect" [ defect-history-update ]
  if strategy = "tit-for-tat" [ tit-for-tat-history-update ]
  if strategy = "unforgiving" [ unforgiving-history-update ]
  if strategy = "unknown" [ unknown-history-update ]
end


;;;;;;;;;;;;;;;;
;;;Strategies;;;
;;;;;;;;;;;;;;;;

;;All the strategies are described in the Info tab.

to act-randomly
  set num-random-games num-random-games + 1
  ifelse (random-float 1.0 < 0.5) [
    set defect-now? false
  ] [
    set defect-now? true
  ]
end

to act-randomly-history-update
;;uses no history- this is just for similarity with the other strategies
end

to cooperate
  set num-cooperate-games num-cooperate-games + 1
  set defect-now? false
end

to cooperate-history-update
;;uses no history- this is just for similarity with the other strategies
end

to defect
  set num-defect-games num-defect-games + 1
  set defect-now? true
end

to defect-history-update
;;uses no history- this is just for similarity with the other strategies
end

to tit-for-tat
  set num-tit-for-tat-games num-tit-for-tat-games + 1
  set partner-defected? item ([who] of partner) partner-history
  ifelse (partner-defected?) [
    set defect-now? true
  ] [
    set defect-now? false
  ]
end

to tit-for-tat-history-update
  set partner-history
    (replace-item ([who] of partner) partner-history partner-defected?)
end

to unforgiving
  set num-unforgiving-games num-unforgiving-games + 1
  set partner-defected? item ([who] of partner) partner-history
  ifelse (partner-defected?)
    [set defect-now? true]
    [set defect-now? false]
end

to unforgiving-history-update
  if partner-defected? [
    set partner-history
      (replace-item ([who] of partner) partner-history partner-defected?)
  ]
end

;;defaults to tit-for-tat
;;can you do better?
to unknown
  set num-unknown-games num-unknown-games + 1
  set partner-defected? item ([who] of partner) partner-history
  ifelse (partner-defected?) [
    set defect-now? true
  ] [
    set defect-now? false
  ]
end

;;defaults to tit-for-tat-history-update
;;can you do better?
to unknown-history-update
  set partner-history
    (replace-item ([who] of partner) partner-history partner-defected?)
end


;;;;;;;;;;;;;;;;;;;;;;;;;
;;;Plotting Procedures;;;
;;;;;;;;;;;;;;;;;;;;;;;;;

;;calculate the total scores of each strategy
to do-scoring
  set random-score  (calc-score "random" num-random)
  set cooperate-score  (calc-score "cooperate" num-cooperate)
  set defect-score  (calc-score "defect" num-defect)
  set tit-for-tat-score  (calc-score "tit-for-tat" num-tit-for-tat)
  set unforgiving-score  (calc-score "unforgiving" num-unforgiving)
  set unknown-score  (calc-score "unknown" num-unknown)
end

;; returns the total score for a strategy if any turtles exist that are playing it
to-report calc-score [strategy-type num-with-strategy]
  ifelse num-with-strategy > 0 [
    report (sum [ score ] of (turtles with [ strategy = strategy-type ]))
  ] [
    report 0
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
303
8
733
459
10
10
20.0
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
10.0

BUTTON
8
19
86
62
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

PLOT
8
330
292
547
Average Payoff
Iterations
Ave Payoff
0.0
10.0
0.0
5.0
true
true
"" ""
PENS
"random" 1.0 0 -7500403 true "" "if num-random-games > 0 [ plot random-score / (num-random-games) ]"
"cooperate" 1.0 0 -2674135 true "" "if num-cooperate-games > 0 [ plot cooperate-score / (num-cooperate-games) ]"
"defect" 1.0 0 -13345367 true "" "if num-defect-games > 0 [ plot defect-score / (num-defect-games) ]"
"tit-for-tat" 1.0 0 -13840069 true "" "if num-tit-for-tat-games > 0 [ plot tit-for-tat-score / (num-tit-for-tat-games) ]"
"unforgiving" 1.0 0 -14835848 true "" "if num-unforgiving-games > 0 [ plot unforgiving-score / (num-unforgiving-games) ]"
"unknown" 1.0 0 -5825686 true "" "if num-unknown-games > 0 [ plot unknown-score / (num-unknown-games) ]"

BUTTON
85
19
174
62
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
8
61
134
94
n-random
n-random
0
20
10
1
1
NIL
HORIZONTAL

SLIDER
8
94
134
127
n-cooperate
n-cooperate
0
20
10
1
1
NIL
HORIZONTAL

SLIDER
8
127
134
160
n-defect
n-defect
0
20
10
1
1
NIL
HORIZONTAL

SLIDER
133
61
259
94
n-tit-for-tat
n-tit-for-tat
0
20
10
1
1
NIL
HORIZONTAL

SLIDER
133
94
259
127
n-unforgiving
n-unforgiving
0
20
10
1
1
NIL
HORIZONTAL

SLIDER
133
127
259
160
n-unknown
n-unknown
0
20
10
1
1
NIL
HORIZONTAL

TEXTBOX
12
175
178
315
      PAYOFF:\n             Partner    \nTurtle    C       D\n-------------------------\n    C        3      0  \n-------------------------\n    D        5      1\n-------------------------\n(C = Cooperate, D = Defect)
11
0.0
0

BUTTON
174
19
260
63
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

This model is a multiplayer version of the iterated prisoner's dilemma. It is intended to explore the strategic implications that emerge when the world consists entirely of prisoner's dilemma like interactions. If you are unfamiliar with the basic concepts of the prisoner's dilemma or the iterated prisoner's dilemma, please refer to the PD BASIC and PD TWO PERSON ITERATED models found in the PRISONER'S DILEMMA suite.


## HOW IT WORKS

The PD TWO PERSON ITERATED model demonstrates an interesting concept: When interacting with someone over time in a prisoner's dilemma scenario, it is possible to tune your strategy to do well with theirs. Each possible strategy has unique strengths and weaknesses that appear through the course of the game. For instance, always defect does best of any against the random strategy, but poorly against itself. Tit-for-tat does poorly with the random strategy, but well with itself.

This makes it difficult to determine a single "best" strategy. One such approach to doing this is to create a world with multiple agents playing a variety of strategies in repeated prisoner's dilemma situations. This model does just that. The turtles with different strategies wander around randomly until they find another turtle to play with. (Note that each turtle remembers their last interaction with each other turtle. While some strategies don't make use of this information, other strategies do.)

Payoffs

When two turtles interact, they display their respective payoffs as labels.

Each turtle's payoff for each round will determined as follows:

                 | Partner's Action
      Turtle's   |
       Action    |   C       D
     ------------|-----------------
           C     |   3       0
     ------------|-----------------
           D     |   5       1
     ------------|-----------------
      (C = Cooperate, D = Defect)

(Note: This way of determining payoff is the opposite of how it was done in the PD BASIC model. In PD BASIC, you were awarded something bad- jail time. In this model, something good is awarded- money.)


## HOW TO USE IT

### Buttons

SETUP: Setup the world to begin playing the multi-person iterated prisoner's dilemma. The number of turtles and their strategies are determined by the slider values.

GO: Have the turtles walk around the world and interact.

GO ONCE: Same as GO except the turtles only take one step.

### Sliders

N-STRATEGY: Multiple sliders exist with the prefix N- then a strategy name (e.g., n-cooperate). Each of these determines how many turtles will be created that use the STRATEGY. Strategy descriptions are found below:

### Strategies

RANDOM - randomly cooperate or defect

COOPERATE - always cooperate

DEFECT - always defect

TIT-FOR-TAT - If an opponent cooperates on this interaction cooperate on the next interaction with them. If an opponent defects on this interaction, defect on the next interaction with them. Initially cooperate.

UNFORGIVING - Cooperate until an opponent defects once, then always defect in each interaction with them.

UNKNOWN - This strategy is included to help you try your own strategies. It currently defaults to Tit-for-Tat.

### Plots

AVERAGE-PAYOFF - The average payoff of each strategy in an interaction vs. the number of iterations. This is a good indicator of how well a strategy is doing relative to the maximum possible average of 5 points per interaction.

## THINGS TO NOTICE

Set all the number of player for each strategy to be equal in distribution.  For which strategy does the average-payoff seem to be highest?  Do you think this strategy is always the best to use or will there be situations where other strategy will yield a higher average-payoff?

Set the number of n-cooperate to be high, n-defects to be equivalent to that of n-cooperate, and all other players to be 0.  Which strategy will yield the higher average-payoff?

Set the number of n-tit-for-tat to be high, n-defects to be equivalent to that of n-tit-for-tat, and all other playerst to be 0.  Which strategy will yield the higher average-payoff?  What do you notice about the average-payoff for tit-for-tat players and defect players as the iterations increase?  Why do you suppose this change occurs?

Set the number n-tit-for-tat to be equal to the number of n-cooperate.  Set all other players to be 0.  Which strategy will yield the higher average-payoff?  Why do you suppose that one strategy will lead to higher or equal payoff?

## THINGS TO TRY

1. Observe the results of running the model with a variety of populations and population sizes. For example, can you get cooperate's average payoff to be higher than defect's? Can you get Tit-for-Tat's average payoff higher than cooperate's? What do these experiments suggest about an optimal strategy?

2. Currently the UNKNOWN strategy defaults to TIT-FOR-TAT. Modify the UNKOWN and UNKNOWN-HISTORY-UPDATE procedures to execute a strategy of your own creation. Test it in a variety of populations.  Analyze its strengths and weaknesses. Keep trying to improve it.

3. Relate your observations from this model to real life events. Where might you find yourself in a similar situation? How might the knowledge obtained from the model influence your actions in such a situation? Why?

## EXTENDING THE MODEL

Relative payoff table - Create a table which displays the average payoff of each strategy when interacting with each of the other strategies.

Complex strategies using lists of lists - The strategies defined here are relatively simple, some would even say naive.  Create a strategy that uses the PARTNER-HISTORY variable to store a list of history information pertaining to past interactions with each turtle.

Evolution - Create a version of this model that rewards successful strategies by allowing them to reproduce and punishes unsuccessful strategies by allowing them to die off.

Noise - Add noise that changes the action perceived from a partner with some probability, causing misperception.

Spatial Relations - Allow turtles to choose not to interact with a partner.  Allow turtles to choose to stay with a partner.

Environmental resources - include an environmental (patch) resource and incorporate it into the interactions.

## NETLOGO FEATURES

Note the use of the `to-report` keyword in the `calc-score` procedure to report a number.

Note the use of lists and turtle ID's to keep a running history of interactions in the `partner-history` turtle variable.

Note how agentsets that will be used repeatedly are stored when created and reused to increase speed.

## RELATED MODELS

PD Basic, PD Two Person Iterated, PD Basic Evolutionary

## CREDITS AND REFERENCES
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

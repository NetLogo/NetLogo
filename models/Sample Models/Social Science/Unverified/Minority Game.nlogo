globals [
  history      ;; the history of which number was the minority (encoded into a binary number)
  minority     ;; the current number in the minority
  avg-score    ;; keeps track of the turtles' average score
  stdev-score  ;; keeps tracks of the standard deviation of the turtles' scores
]

turtles-own [
  score             ;; each turtle's score
  choice            ;; each turtle's choice
  strategies        ;; each turtle's strategies (a list of lists)
  current-strategy  ;; each turtle's current strategy (index in above list)
  strategies-scores ;; the accumulated virtual scores for each of the turtle's strategies (a list)
]

;; setup procedure
to setup
  clear-all
  if (memory = 1 and strategies-per-agent > 4 )  ;; prevent an infinite loop from occurring
    [ user-message word "You need to increase the memory variable or\n"
                        "decrease the strategies-per-agent variable"
      stop ]
  initialize-system
  initialize-turtles
  update-system
  reset-ticks
end

;; resets state variables
to initialize-system
  set history random (2 ^ memory)
  set avg-score 0
  set stdev-score 0
end

;; creates the specified number of turtles
to initialize-turtles
  crt number
    [ setxy 0 (world-height * who / number)  ;; disperse over the y-axis
      set heading 90
      assign-strategies
      set current-strategy random strategies-per-agent
      set choice item history (item current-strategy strategies)
      ifelse (color-by = "choice")
        [ recolor-by-choice ]
        [ set color green ]  ;; we initially set all to green to prevent divide by zero error
      set score 0
      set strategies-scores n-values strategies-per-agent [0] ]
end

;; gives the turtles their allotted number of unique strategies
to assign-strategies  ;; turtle procedure
  set strategies []
  while [ length remove-duplicates strategies < strategies-per-agent ]
    [ set strategies n-values strategies-per-agent [create-strategy] ]
end

;; reports a random strategy (a list of 1 or 0's)
to-report create-strategy
  report n-values (2 ^ memory) [random 2]
end

to go
  ask turtles [ update-scores-and-strategy ]
  set history decimal (lput minority but-first full-history)
  ask turtles [ update-choice-and-color ]
  tick
  update-system
  move-turtles
end

;; moves the turtles about the world (a visual aid to see their collective behavior)
to move-turtles
  ask turtles [ fd score / avg-score ]
end

;; updates minority, avg-score, and stdev-score globals
to update-system
  let num-picked-zero count turtles with [choice = 0]
  ifelse (num-picked-zero <= (number - 1) / 2)
    [ set minority 0 ]
    [ set minority 1 ]
  ;; plot this here for speed or optimization
  set-current-plot "Number Picking Zero"
  plot num-picked-zero

  set avg-score mean [score] of turtles
  set stdev-score standard-deviation [score] of turtles
end

;; updates turtle's score and their strategies' virtual scores
to update-scores-and-strategy  ;; turtles procedure
  increment-scores
  let max-score max strategies-scores
  let max-strategies []
  let counter 0
  ;; this picks a strategy with the largest virtual score
  foreach strategies-scores
    [ if (? = max-score)
        [ set max-strategies lput counter max-strategies ]
      set counter counter + 1 ]
  set current-strategy one-of max-strategies
  if (choice = minority)
    [ set score score + 1 ]
end

;; this increases the virtual scores of each strategy
;; that selected the minority
to increment-scores  ;; turtles procedure
  ;; here we use MAP to simultaneously walk down both the list
  ;; of strategies, and the list of those strategies' scores.
  ;; ?1 is the current strategy, and ?2 is the current score.
  ;; For each strategy, we check to see if that strategy selected
  ;; the minority.  If it did, we increase its score by one,
  ;; otherwise we leave the score alone.
  set strategies-scores
      (map [ifelse-value (item history ?1 = minority)
              [?2 + 1] [?2]]
           strategies strategies-scores)
end

;; updates turtle's choice and re-colors them
to update-choice-and-color ;; turtles procedure
  set choice (item history (item current-strategy strategies))
  ifelse (color-by = "choice")
    [ recolor-by-choice ]
    [ recolor-by-success ]
end

to recolor-by-choice ;; turtles procedure
  ifelse (choice = 0)
      [ set color red ]
      [ set color blue ]
end

to recolor-by-success ;; turtles procedure
  ifelse (score > avg-score + stdev-score)
    [ set color red ]
    [ ifelse (score < avg-score - stdev-score)
        [ set color blue ]
        [ set color green ] ]
end

;; reports the history in binary format (with padding if needed)
to-report full-history
  report sentence n-values (memory - length binary history) [0] (binary history)
end

;; converts a decimal number to a binary number (stored in a list of 0's and 1's)
to-report binary [decimal-num]
  let binary-num []
  loop
    [ set binary-num fput (decimal-num mod 2) binary-num
      set decimal-num int (decimal-num / 2)
      if (decimal-num = 0)
        [ report binary-num ] ]
end

;; converts a binary number (stored in a list of 0's and 1's) to a decimal number
to-report decimal [binary-num]
  report reduce [(2 * ?1) + ?2] binary-num
end
@#$#@#$#@
GRAPHICS-WINDOW
402
10
727
356
17
17
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
-17
17
-17
17
1
1
1
ticks
30

BUTTON
33
10
102
43
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
107
10
170
43
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
17
50
189
83
number
number
1
1501
501
2
1
NIL
HORIZONTAL

SLIDER
17
87
189
120
memory
memory
1
12
6
1
1
NIL
HORIZONTAL

SLIDER
17
124
189
157
strategies-per-agent
strategies-per-agent
1
10
5
1
1
NIL
HORIZONTAL

PLOT
214
325
394
475
Success rate
time
success rate
0.0
1.0
0.0
0.05
true
false
"" ""
PENS
"max" 1.0 0 -2674135 true "" "let max-score max [score] of turtles\nif (ticks != 0)[ plot max-score / ticks ]"
"min" 1.0 0 -13345367 true "" "let min-score min [score] of turtles\nif (ticks != 0)[ plot min-score / ticks ]"
"avg" 1.0 0 -10899396 true "" "if (ticks != 0)[ plot avg-score / ticks ]"

PLOT
3
325
209
475
Number Picking Zero
time
count
0.0
1.0
0.0
100.0
true
false
"set-plot-y-range 0 number" ""
PENS
"number" 1.0 0 -10899396 true "" ""

MONITOR
205
40
378
85
History
full-history
3
1
11

PLOT
3
173
209
323
Scores
time
score
0.0
1.0
0.0
1.0
true
true
"" ""
PENS
"max" 1.0 0 -2674135 true "" "let max-score max [score] of turtles\nplot max-score"
"min" 1.0 0 -13345367 true "" "let min-score min [score] of turtles\nplot min-score"
"avg" 1.0 0 -10899396 true "" "plot avg-score"

CHOOSER
243
106
335
151
color-by
color-by
"choice" "success"
1

PLOT
214
173
394
323
Success histogram
success
frequency
0.0
1.0
0.0
1.0
true
false
"set-histogram-num-bars 25" ""
PENS
"default" 0.02 1 -16777216 false "" "if (ticks != 0) [ histogram [score / ticks] of turtles ]"

@#$#@#$#@
## WHAT IS IT?

This is a simplified model of an economic market.  In each time step, agents choose one of two sides, 0 or 1, and those on the minority side win a point.  This problem is inspired by the "El Farol" bar problem.  Each agent uses a finite set of strategies to make their decision based upon past record; however, the record consists only of which side, 0 or 1, was in the minority, not the actual population count of how many chose each side.

## HOW IT WORKS

Each agent begins with a score of 0 and STRATEGIES-PER-AGENT strategies. Initially, they choose a random one of these strategies to use.  The initial historical record is generated randomly.  If their current strategy correctly predicted whether 0 or 1 would be the minority, they add one point to their score.  Each strategy also earns virtual points according to if it would have been correct or not.  From then on, the agents will then use their strategy with the highest virtual point total to predict whether they should select 0 or 1.

This strategy consist of a list of 1's and 0's that is 2<sup>MEMORY</sup> long.  The choice the turtle then makes is based off of the history of past choices.  This history is also a list of 1's and 0's that is MEMORY long, but it is encoded into a binary number.  The binary number is then used as an index into the strategy list to determine the choice.

This means that once the number of agents, the number of strategies, and the length of the historical record are chosen, all parameters are fixed and the behavior of the system is of interest.

## HOW TO USE IT

GO: Starts and stops the model.

SETUP: Resets the simulation according to the parameters set by the sliders.

NUMBER: Sets the number of agents to participate.  This is always odd to insure a minority.

MEMORY: Sets the length of the history which the agents use to predict their behavior.  Most interesting between 3 and 12, though there is some interesting behavior at 1 and 2.  Note that when using a MEMORY of 1, the STRATEGIES-PER-AGENT needs to be 4 or less.

STRATEGIES-PER-AGENT: Sets the number of strategies each agent has in their toolbox.  Five is typically a good value.  However, this can be changed for investigative purposes using the slider, if desired.

COLOR-BY: "Choice" represents the agents changing their colors depending on if they have chosen 0 (red) or 1 (blue).  "Success" represents the agents changing their color depending upon their success rate (the number of times they have been in the minority divided by the number of selections).  An agent is green if they are within one standard deviation above or below the mean success rate, red if they are more than one standard deviation above, and blue if they are more than one standard deviation below.

**RECOMMENDED SETTINGS**: NUMBER=501, MEMORY=6, STRATEGIES-PER-AGENT=5 (Should be loaded by default)

**CAUTION**: Beware setting the MEMORY slider to higher values.  It scales exponentially (2<sup>MEMORY</sup>), however this only has an effect when SETUP is run.  This means that for each increased unit of MEMORY, it takes twice as long for SETUP to run.

## THINGS TO NOTICE

There are two extremes possible for each turn: the size of the minority is 1 agent or (NUMBER-1)/2 agents (since NUMBER is always odd).  The former would represent a "wasting of resources", while the latter represents a situation which is more "for the common good."  However, each agent acts in an inherently selfish manner, as they care only if they and they alone are in the minority.  Nevertheless, the latter situation is prevalent.  Does this represent unintended cooperation between agents, or merely coordination and well developed powers of prediction?

The agents move according to how successful they are relative to the mean success rate.  After running for about 100 time steps (at just about any parameter setting), how do the fastest and slowest agents compare?  What does this imply?

## THINGS TO TRY

Notice how the population of agents choosing 0 stays close to NUMBER/2.  How do the deviations change as you change the value of MEMORY?

How do things change if you keep everything the same but change the STRATEGIES-PER-AGENT?

## EXTENDING THE MODEL

There are a few evolutionary possibilities for this model which could be coded.

(1) Maybe after some (long) amount of time, the least successful agent is replaced by a clone of the most successful agent, with zeroed scores and possibly mutated strategies.  How would things change then?

(2) Similar to (1), you could start the agents with a very small memory value, and again replace the least successful agent with a clone of the most successful agent.  But this time instead of just zeroing the scores and giving mutated strategies, you also add or subtract one unit of memory for the new agent.  What would happen here? Would their brains continue to get bigger or find some happy value?  Would people with small memory be altogether eliminated, or would they survive (maybe even still thrive)?

## NETLOGO FEATURES

The `n-values` primitive is used to set up strategies for each player.

The primitives `map` and `reduce` were also used to simplify code.

## RELATED MODELS

* any of the Prisoner's Dilemma models  
* Altruism  
* Cooperation

## CREDITS AND REFERENCES

Original implementation: Daniel B. Stouffer, for the Center for Connected Learning and Computer-Based Modeling.

This model was based upon studies by Dr. Damien Challet, et al.  
Information can be found on the web at http://www.unifr.ch/econophysics/minority/

Challet, D. and Zhang, Y.-C. Emergence of Cooperation and Organization in an Evolutionary Game. Physica A 246, 407 (1997).

Zhang, Y.-C. Modeling Market Mechanism with Evolutionary Games. Europhys. News 29, 51 (1998).
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
NetLogo 5.0beta2
@#$#@#$#@
setup
repeat max-pxcor [ go ]
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

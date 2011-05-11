patches-own [
  cooperate?       ;; patch will cooperate
  old-cooperate?   ;; patch has cooperated before
  score            ;; score resulting from interaction of neighboring patches
  color-class      ;; numeric value from 1= blue, 2= red, 3= green, 4= yellow.
]

to setup
  clear-all
  ask patches [
    ifelse random-float 1.0 < (initial-cooperation / 100)
      [setup-cooperation true]
      [setup-cooperation false]
    establish-color
  ]
  reset-ticks
  update-plot
end

to setup-cooperation [value]
  set cooperate? value
  set old-cooperate? value
end

to go
  ask patches [interact]          ;; to play with a neighboring patch
  ask patches [select-strategy]   ;; adopt the strategy of the neighbor (who had the highest score)
  tick
  update-plot
end

to update-plot
  set-current-plot "Cooperation/Defection Frequency"
  plot-histogram-helper "cc" blue
  plot-histogram-helper "dd" red
  plot-histogram-helper "cd" green
  plot-histogram-helper "dc" yellow
end

to plot-histogram-helper [pen-name color-name]
  set-current-plot-pen pen-name
  histogram [color-class] of patches with [pcolor = color-name]
end

to interact  ;; patch procedure
  let total-cooperaters count neighbors with [cooperate?]  ;; total number neighbors who cooperated
  ifelse cooperate?
    [set score total-cooperaters]                   ;; cooperator gets score of # of neighbors who cooperated
    [set score Defection-Award * total-cooperaters] ;; non-cooperator get score of a multiple of the
                                                    ;; neighbors who cooperated
end

to select-strategy  ;; patch procedure
  set old-cooperate? cooperate?
  set cooperate? [cooperate?] of max-one-of neighbors [score] ;;choose strategy (cooperate, not cooperate)
                                                            ;; of neighbor who performed the best
  establish-color
end

to establish-color  ;; patch procedure
  ifelse old-cooperate?
    [ifelse cooperate?
      [set pcolor blue
       set color-class 1]
      [set pcolor green
       set color-class 3]
    ]
    [ifelse cooperate?
      [set pcolor yellow
       set color-class 4]
      [set pcolor red
       set color-class 2]
    ]
end
@#$#@#$#@
GRAPHICS-WINDOW
426
10
840
445
50
50
4.0
1
10
1
1
1
0
1
1
1
-50
50
-50
50
1
1
1
ticks
8.0

BUTTON
20
42
101
75
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
115
42
192
75
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
2
80
210
113
initial-cooperation
initial-cooperation
0
100
66.6
0.1
1
%
HORIZONTAL

TEXTBOX
235
49
426
198
Color Coordination to Strategy\n                              Round \n                   Previous    Current\nBlue                C                 C\nRed                 D                 D\nGreen             C                 D\nYellow             D                C\n                        C = Cooperate \n                        D = Defect
11
0.0
0

SLIDER
1
117
210
150
Defection-Award
Defection-Award
0
3
1.59
0.01
1
x
HORIZONTAL

PLOT
10
218
395
431
Cooperation/Defection Frequency
Class
Frequency (%)
1.0
5.0
0.0
1.0
true
false
"" ""
PENS
"cc" 1.0 1 -13345367 true "" ""
"dd" 1.0 1 -2674135 true "" ""
"cd" 1.0 1 -10899396 true "" ""
"dc" 1.0 1 -1184463 true "" ""

@#$#@#$#@
## WHAT IS IT?

One of the most prominently studied phenomena in Game Theory is the "Prisoner's Dilemma."  The Prisoner's Dilemma, which was formulated by Melvin Drescher and Merrill Flood and named by Albert W. Tucker, is an example of a class of games called non-zero-sum games.

In zero-sum games, total benefit to all players add up to zero, or in other words, each player can only benefit at the expense of other players (e.g. chess, football, poker --- one person can only win when the opponent loses).  On the other hand, in non-zero-games, each person's benefit does not necessarily come at the expense of someone else.  In many non-zero-sum situations, a person can benefit only when others benefit as well.  Non-zero-sum situations exist where the supply of a resource is not fixed or limited in any way (e.g. knowledge, artwork, and trade).  Prisoner's Dilemma, as a non-zero-sum game, demonstrates a conflict between rational individual behavior and the benefits of cooperation in certain situations.  The classical prisoner's dilemma is as follows:

Two suspects are apprehended by the police.  The police do have enough evidence to convict these two suspects.  As a result, they separate the two, visit each of them, and offer both the same deal:  "If you confess, and your accomplice remains silent, he goes to jail for 10 years and you can go free.  If you both remain silent, only minor charges can be brought upon both of you and you guys get 6 months each.  If you both confess, then each of you two gets 5 years."

Each suspect may reason as follows: "Either my partner confesses or not.  If he does confess and I remain silent, I get 10 years while if I confess, I get 5 years.  So, if my partner confesses, it is best that I confess and get only 5 years than 10 years in prison.  If he didn't, then by confessing, I go free, whereby remaining silent, I get 6 months.  Thus, if he didn't confess, it is best to confess, so that I can go free.   Whether or not my partner confesses or not, it is best that I confess."

In a non-iterated prisoner's dilemma, the two partners will never have to work together again.  Both partners are thinking in the above manner and decide to confess.  Consequently, they both receive 5 years in prison.  If neither would have confessed, they would have only gotten 6 months each.  The rational behavior paradoxically leads to a socially unbeneficial outcome.


                                 Payoff Matrix
                                 -------------
                                  YOUR PARTNER
                         Cooperate            Defect
                        -----------------------------
           Cooperate |   (0.5, 0.5)           (0, 10)
      YOU            |
           Defect    |(10, 0)              (5, 5)
    
            (x, y) = x: your score, y: your partner's score
            Note: lower the score (number of years in prison), the better.


In an Iterated Prisoner's Dilemma where you have more than two players and multiple rounds, such as this one, the scoring is different.  In this model, it is assumed that an increase in the number of people who cooperate will increase proportionately the benefit for each cooperating player (which would be a fine assumption, for example, in the sharing of knowledge).  For those who do not cooperate, assume that their benefit is some factor (alpha) multiplied by the number of people who cooperate (that is, to continue the previous example, the non-cooperating players take knowledge from others but do not share any knowledge themselves).  How much cooperation is incited is dependent on the factor multiple for not cooperating.  Consequently, in an iterated prisoner's dilemma with multiple players, the dynamics of the evolution in cooperation may be observed.

                                 Payoff Matrix
                                 -------------
                                    OPPONENT
                         Cooperate            Defect
                        -----------------------------
           Cooperate |(1, 1)            (0, alpha)
      YOU            |
           Defect    |(alpha, 0)        (0, 0)
    
            (x, y) = x: your score, y: your partner's score
            Note: higher the score (amount of the benefit), the better.


## HOW TO USE IT

Decide what percentage of patches should cooperate at the initial stage of the simulation and change the INITIAL-COOPERATION slider to match what you would like.  Next, determine the DEFECTION-AWARD multiple (mentioned as alpha in the payoff matrix above) for defecting or not cooperating.  The Defection-Award multiple varies from range of 0 to 3.  Press SETUP and note that red patches (that will defect) and blue patches (cooperate) are scattered across the  .  Press GO to make the patches interact with their eight neighboring patches.  First, they count the number of neighboring patches that are cooperating.  If a patch is cooperating, then its score is number of neighboring patches that also cooperated.   If a patch is defecting, then its score is the product of the number of neighboring patches who are cooperating and the Defection-Award multiple.


## HOW IT WORKS

Each patch will either cooperate (blue) or defect (red) in the initial start of the model.  At each cycle, each patch will interact with all of its 8 neighbors to determine the score for the interaction.  Should a patch have cooperated, its score will be the number of neighbors that also cooperated.  Should a patch defect, then the score for this patch will be the product of the Defection-Award multiple and the number of neighbors that cooperated (i.e. the patch has taken advantage of the patches that cooperated).

In the subsequent round, the patch will set its old-cooperate? to be the strategy it used in the previous round.  For the upcoming round, the patch will adopt the strategy of one of its neighbors that scored the highest in the previous round.

If a patch is blue, then the patch cooperated in the previous and current round.  
If a patch is red, then the patch defected in the previous iteration as well as the current round.  
If a patch is green, then the patch cooperated in the previous round but defected in the current round.  
If a patch is yellow, then the patch defected in the previous round but cooperated in the current round.


## THINGS TO NOTICE

Notice the effect the Defection-Award multiple plays in determining the number of patches that will completely cooperate (red) or completely defect (blue). At what Defection-Award multiple value will a patch be indifferent to defecting or cooperating?  At what Defection-Award multiple value will there be a dynamic change between red, blue, green, and yellow - where in the end of the model no particular color dominates all of the patches (i.e. view is not all red or all blue)?

Note the Initial-Cooperation percentage.  Given that Defection-Award multiple is low (below 1), if the initial percentage of cooperating patches is high, will there be more defecting or cooperating patches eventually?  How about when the Defection-Award multiple is high?  Does the initial percentage of cooperation effect the outcome of the model, and, if so, how?

## THINGS TO TRY

Increase the Defection-Award multiple by moving the "Defection-Award" slider (just increase the "Defection-Award" slider while model is running), and observe how the histogram for each color of patch changes. In particular, pay attention to the red and blue bars.  Does the number of pure cooperation or defection decrease or increase with the increase of the Defection-Award multiple?  How about with a decrease of the Defection-Award multiple? (Just increase the "Defection-Award" slider while model is running.)

At each start of the model, either set the initial-cooperation percentage to be very high or very low (move the slider for "initial-cooperation"), and proportionally value the Defection-Award multiple (move the slider for "Defection-Award" in the same direction) with regards to the initial-cooperation percentage.  Which color dominates the world, when the initial-cooperation is high and the Defection-Award is high?  Which color dominates the world when initial-cooperation is low and the Defection-Award multiple is also low?


## EXTENDING THE MODEL

Alter the code so that the patches have a strategy to implement.  For example, instead of adopting to cooperated or defect based on the neighboring patch with the maximum score.  Instead, let each patch consider the history of cooperation or defection of it neighboring patches, and allow it to decide whether to cooperate or defect as a result.

Implement these four strategies:

1. Cooperate-all-the-time: regardless of neighboring patches' history, cooperate.  
2. Tit-for-Tat:  only cooperate with neighboring patches, if they have never defected.  Otherwise, defect.  
3. Tit-for-Tat-with-forgiveness: cooperate if on the previous round, the patch cooperated.  Otherwise, defect.  
4. Defect-all-the-time: regardless of neighboring patches' history, defect.

How are the cooperating and defecting patches distributed?  Which strategy results with the highest score on average?  On what conditions will this strategy be a poor strategy to use?

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

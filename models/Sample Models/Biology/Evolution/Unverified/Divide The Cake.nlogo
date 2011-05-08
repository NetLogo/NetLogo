turtles-own [
  ;; "appetite" will reflect the behavior of the three types of agents: '2' for modest,
  ;; '3' for fair, and '4' for greedy.  Note that the ratio between these three numbers
  ;; 2:3:4 is equal to the ratio of agent types' eating habits: 1/3 : 1/2 : 2/3.
  ;; In principle, we could have worked with 1/3, 1/2, and 2/3 but it's just easier to
  ;; work with integers.   So we will think of a whole as "6" and not as "1".
  appetite
  ;; true or false for "is it my turn to run the 'eat' procedure?," will help us manage
  ;; the behavior of turtles who are on the same patch.
  turn?
]

to setup
  ca
  ask patches
    [ set pcolor green ]
  setup-turtles
  reset-ticks
end

to setup-turtles
  let total (modest-proportion + fair-proportion + greedy-proportion)
  crt initial-number
    [ setxy random-xcor random-ycor
      ;; This will assign proportions of the turtles to agent types
      ;; according to the ratio of greedy, fair, and modest turtles
      ;; as set in their respective sliders.  The procedure uses the
      ;; fact that, given enough turtles, the random yet uniform
      ;; distribution of individual turtles' values from 0 to 1.0
      ;; will assign these turtles to different agent types so as to
      ;; approximate a correspondence with the number of turtles in
      ;; each type, as determined by the user's ratio settings.
      let breed-rand random-float 1.0
      ifelse breed-rand < (fair-proportion / total)
        [ set appetite 3
          set color red ]
        [ ifelse breed-rand < ((total - greedy-proportion) / total)
            [ set appetite 2
              set color brown ]
            [ set appetite 4
              set color blue ] ] ]
end

to go
  ;; Main loop - agents move around and eat the grass
  ;; Here all agents receive a 'true' value for their 'turn?' Boolean variable.
  ;; Later, in the 'eat' procedure, only a single turtle of turtle pairs who are on the same patch will get to use this value.
  ask turtles
    [ set turn? true
      move
      eat ]
  tick
end

to move  ;; turtle procedure
  ;; Agents move in random directions every turn,
  ;; at least one patch per turn.
  rt random-float 360

  ;; Note that 'travel-distance' is scaled down by the (1 - viscosity) factor, unless 'viscosity' is 0.
  fd 1 + (travel-distance * (1 - viscosity))
end

to eat ;; turtle procedure
  ;; We need the turn? variable so turtles who are eligible
  ;; to eat will not get two chances to reproduce.
  ;; Only where there is exactly a pair of turtles on a patch are both turtles eligible to eat.
  ;; Logically, we can't manage more than two turtles, because we could not, then, distinguish between
  ;; the presence of three modest turtles (2 + 2 + 2) or two fair turtles (3 + 3);
  ;; Also, if appetites on a patch exceeded resources, that is if their total was greater than 6, we would not know if this
  ;; were because two turtles have gone beyond 6 and they should die, or because there were three or more turtles on the patch.
  if (count turtles-here = 2 and turn?)
    [ ifelse (6 >= sum [appetite] of turtles-here)
        [ ask turtles-here  ;; including myself!
            [ reproduce
              set turn? false ] ]
        [ ask turtles-here  ;; including myself!
           [ die ] ] ]
  set turn? false
end

to reproduce ;; turtle procedure
  ;; Note that the higher a turtle's appetite value, the higher are its
  ;; chances of reproduction.
  if (random 6) < appetite
    [ hatch 1 ]
end
@#$#@#$#@
GRAPHICS-WINDOW
361
10
779
449
25
25
8.0
1
10
1
1
1
0
1
1
1
-25
25
-25
25
1
1
1
ticks
30

PLOT
7
186
356
347
Population vs. Time
time
population
0.0
1.0
0.0
1.0
true
true
"" ""
PENS
"modest" 1.0 0 -6459832 true "" "plot count turtles with [color = brown]"
"fair" 1.0 0 -2674135 true "" "plot count turtles with [color = red]"
"greedy" 1.0 0 -13345367 true "" "plot count turtles with [color = blue]"

SLIDER
7
108
179
141
viscosity
viscosity
0
1
0.8
0.01
1
NIL
HORIZONTAL

SLIDER
7
146
179
179
travel-distance
travel-distance
0
10
1
1
1
NIL
HORIZONTAL

SLIDER
183
70
355
103
modest-proportion
modest-proportion
0
99
50
1
1
NIL
HORIZONTAL

SLIDER
183
108
355
141
fair-proportion
fair-proportion
0
99
50
1
1
NIL
HORIZONTAL

SLIDER
183
146
355
179
greedy-proportion
greedy-proportion
0
99
50
1
1
NIL
HORIZONTAL

SLIDER
7
70
179
103
initial-number
initial-number
2
1000
200
1
1
NIL
HORIZONTAL

BUTTON
112
28
179
61
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
183
28
246
61
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

MONITOR
70
442
187
487
greedy
count turtles with [color = blue]
3
1
11

MONITOR
70
350
187
395
modest
count turtles with [color = brown]
3
1
11

MONITOR
70
396
187
441
fair
count turtles with [color = red]
3
1
11

MONITOR
188
350
276
395
% modest
(count turtles with [color = brown ])\n/ (count turtles)\n* 100
1
1
11

MONITOR
188
396
276
441
% fair
(count turtles with [color = red])\n/ (count turtles)\n* 100
1
1
11

MONITOR
188
442
276
487
% greedy
(count turtles with [color = blue])\n/ (count turtles)\n* 100
1
1
11

@#$#@#$#@
## WHAT IS IT?

This model (and Cooperation and Altruism) are part of the EACH curriculum: "Evolution of Altruistic and Cooperative Habits: Learning About Complexity in Evolution".  See http://ccl.northwestern.edu/cm/EACH/ for more information.

This is an evolutionary game-playing model.  Three types of agents must divide a common resource.  In the original model it's cake, but let's think of it as edible grass instead, which is green.  The agents come in three types: modest, fair, and greedy.  The agents move around competing for grass.  Agents need grass in order to produce offspring, so over time, the agent types that get more grass will tend to increase in number.

## HOW IT WORKS

When there are only two agents on a patch of grass, each of the agents tries to eat a certain amount of the grass.

There are fair agents (red), modest agents (brown), and greedy agents (blue).  Fair agents try to eat half the grass, modest agents try to eat a third of the grass, and greedy agents try to eat two-thirds of the grass.

If the total amount requested by both agents is greater than 100%, then both agents die.  Otherwise, each agent gets his requested share of the patch's resources.   Each agent then enters a reproduction lottery based on its appetite:  The greater the appetite, the greater the chance of reproduction.  This factor gives a fitness advantage to the agents with a greater appetite that counteracts the disadvantage of having a greater appetite (viz., the higher chance of asking for too much food and dying).

Each turn, every patch resets to the full amount of grass.

## HOW TO USE IT

SETUP: Creates the agents.

GO: Starts the model running.

MODEST-PROPORTION, FAIR-PROPORTION, GREEDY-PROPORTION:  These sliders set the proportions of the three agent types within the initial number of agents.  Note that the actual numbers chosen for these three sliders are irrelevant -- only the ratio of the three numbers counts vis-a-vis the setting of the INITIAL-NUMBER slider.  For example, the ratio settings "1 1 1" will produce roughly equal numbers of each type, as will the settings "79 79 79" or what-have-you.  Likewise, the setting "0 0 1" is no different from "0 0 88", etc.

INITIAL-NUMBER: Creates this number of turtles of all three types together.

TRAVEL-DISTANCE: The value of this variable determines the number of steps an agent moves each turn.  This value is the mobility of an agent.

VISCOSITY: This variable is the difficulty of movement.  It limits the general mobility of agents.

## THINGS TO NOTICE

If you run the model with the default settings (high viscosity), notice how the population of fair (1/2) agents increases.  Why does this happen?  Also, notice how the modest (1/3) and greedy (2/3) populations and the modest (1/3) and fair (1/2) populations congregate in dynamically-stable communities but the greedy (2/3) and fair (1/2) populations do not seem to co-exist.  Why does this happen?

## THINGS TO TRY

Try changing the population VISCOSITY value.  What happens when this value is decreased?  How does this affect the survival and the grouping of the different populations?

Change the starting ratios of the populations.  How do these ratios affect the behavior of the model?  Why should the model depend on these ratios?

Change TRAVEL-DISTANCE.  How does it affect the model?  What is the relationship between the values of TRAVEL-DISTANCE and VISCOSITY?

## EXTENDING THE MODEL

What environmental variables might affect the model?  Should the grass grow back to its full length each turn?  How would changing the rejuvenation of available resources affect the model?

Should the agents be informed of each other's anticipated demands prior to making their claims?  If they did, then agents could bargain before asking for food, and therefore reduce their chances of death as a result of a joint demand that exceeds the available resources, e.g., 1/2 + 2/3 > 1.  This bargaining behavior would change the model drastically, but is an interesting way of exploring how populations may adapt their interpersonal behaviors in order to survive.  A question would then arise of whether such survival does or does not ultimately play against agents as a type, because the price of survival could be a depletion of resources in future generations.

## RELATED MODELS

Altruism, Cooperation, PD Basic

## CREDITS AND REFERENCES

This model is based on William Harms's "Divide the Cake" model, described in Brian Skyrms's book "The Evolution of the Social Contract".

Thanks to Damon Centola for his implementation of this model.
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
setup
repeat 20 [ go ]
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

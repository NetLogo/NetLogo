globals [ color-by-unhappiness? ]

turtles-own
[
  ideal-temp       ;; The temperature I want to be at
  output-heat      ;; How much heat I emit per time step
  unhappiness      ;; The magnitude of the difference between my ideal
                   ;;   temperature and the actual current temperature here
]

patches-own
[
  temp             ;; short for "temperature"
]

to setup
  clear-all
  set color-by-unhappiness? false

  ;; creating the bugs the following way ensures that we won't
  ;; wind up with more than one bug on a patch
  ask n-of bug-count patches [
    sprout 1 [
      set ideal-temp  min-ideal-temp  + random (max-ideal-temp  - min-ideal-temp)
      set output-heat min-output-heat + random (max-output-heat - min-output-heat)
      set unhappiness abs (ideal-temp - temp)
      color-by-ideal-temp
      face one-of neighbors
      set size 2  ;; easier to see
    ]
  ]
  ;; plot the initial state of the system
  reset-ticks
end

to color-by-ideal-temp
  ;; when scaling the color of turtles, adjust the value
  ;; range by this amount to avoid turtles being too dark or too light.
  let range-adjustment ( max-ideal-temp - min-ideal-temp ) / 2
  set color scale-color lime ideal-temp ( min-ideal-temp - range-adjustment )
                                        ( max-ideal-temp + range-adjustment )
end

to color-by-unhappiness [ max-unhappiness ]
  set color scale-color blue unhappiness  max-unhappiness 0
end

to go
  if not any? turtles [ stop ]
  ;; diffuse heat through world
  diffuse temp diffusion-rate
  ;; The world retains a percentage of its heat each cycle.
  ;; (The Swarm and Repast versions have 1.0 meaning no
  ;; evaporation and 0.0 meaning complete evaporation;
  ;; we reverse the scale to better match the name.)
  ask patches [ set temp temp * (1 - evaporation-rate) ]
  ;; agentsets in NetLogo are always in random order, so
  ;; "ask turtles" automatically shuffles the order of execution
  ;; each time.
  ask turtles [ step ]
  recolor-turtles
  recolor-patches
  tick
end

to recolor-turtles
  if color-by-unhappiness?
  [
    let max-unhappiness max [unhappiness] of turtles
    ask turtles [ color-by-unhappiness max-unhappiness ]
  ]
end

to recolor-patches
  ;; hotter patches will be red verging on white;
  ;; cooler patches will be black
  ask patches [ set pcolor scale-color red temp 0 150 ]
end

to step  ;; turtle procedure
  ;; my unhappiness is the magnitude or absolute value of the difference
  ;; between by ideal temperature and the temperature of this patch
  set unhappiness abs (ideal-temp - temp)
  ;; if unhappy and not at the hottest neighbor,
  ;; then move to an open neighbor
  if unhappiness > 0
    [ ifelse random-float 100 < random-move-chance
        [ bug-move one-of neighbors ]
        [ bug-move best-patch ] ]
  set temp temp + output-heat
end

;; find the hottest or coolest location next to me; also
;; take my current patch into consideration
to-report best-patch  ;; turtle procedure
  ifelse temp < ideal-temp
    [ let winner max-one-of neighbors [temp]
      ifelse [temp] of winner > temp
        [ report winner ]
        [ report patch-here ] ]
    [ let winner min-one-of neighbors [temp]
      ifelse [temp] of winner < temp
        [ report winner ]
        [ report patch-here ] ]
end

to bug-move [target]  ;; turtle procedure
  ;; if we're already there, there's nothing to do
  if target = patch-here [ stop ]
  ;; move to the target patch (if it is not already occupied)
  if not any? turtles-on target [
    face target
    move-to target
    stop
  ]
  set target one-of neighbors with [not any? turtles-here]
  if target != nobody [ move-to target ]
  ;; The code above is a bit different from the original Heatbugs
  ;; model in Swarm.  In the NetLogo version, the bug will always
  ;; find an empty patch if one is available.
  ;; In the Swarm version, the bug picks a random
  ;; nearby patch, checks to see if it is occupied, and if it is,
  ;; picks again.  If after 10 tries it hasn't found an empty
  ;; patch, it gives up and stays where it is.  Since each try
  ;; is random and independent, even if there is an available
  ;; empty patch the bug will not always find it.  Presumably
  ;; the Swarm version is coded that way because there is no
  ;; concise equivalent in Swarm/Objective C to NetLogo's
  ;; 'one-of neighbors with [not any? turtles-here]'.
  ;; If you want to match the Swarm version exactly, remove the
  ;; last two lines of code above and replace them with this:
  ; let tries 0
  ; while [tries <= 9]
  ;   [ set tries tries + 1
  ;     set target one-of neighbors
  ;     if not any? turtles-on target [
  ;       move-to target
  ;       stop
  ;     ]
  ;   ]
end

;;; the following procedures support the two extra buttons
;;; in the interface

;; remove all heat from the world
to deep-freeze
  ask patches [ set temp 0 ]
end

;; add max-output-heat to all locations in the world, heating it evenly
to heat-up
  ask patches [ set temp temp + max-output-heat ]
end
@#$#@#$#@
GRAPHICS-WINDOW
371
10
781
441
-1
-1
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
0
99
0
99
1
1
1
ticks
30

SLIDER
14
30
276
63
bug-count
bug-count
1
500
100
1
1
bugs
HORIZONTAL

BUTTON
27
177
96
210
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
98
177
166
210
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
164
236
357
269
evaporation-rate
evaporation-rate
0
1
0.01
0.01
1
NIL
HORIZONTAL

SLIDER
164
270
357
303
diffusion-rate
diffusion-rate
0
1
0.9
0.1
1
NIL
HORIZONTAL

SLIDER
164
305
357
338
random-move-chance
random-move-chance
0
100
0
1.0
1
%
HORIZONTAL

PLOT
14
354
358
513
Avg. Bug Unhappiness
time
unhappiness
0.0
100.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "plot mean [unhappiness] of turtles"

SLIDER
14
69
186
102
min-ideal-temp
min-ideal-temp
0
200
10
1
1
NIL
HORIZONTAL

SLIDER
14
102
186
135
max-ideal-temp
max-ideal-temp
0
200
40
1
1
NIL
HORIZONTAL

SLIDER
189
102
362
135
max-output-heat
max-output-heat
0
100
25
1
1
NIL
HORIZONTAL

SLIDER
189
69
362
102
min-output-heat
min-output-heat
0
100
5
1
1
NIL
HORIZONTAL

BUTTON
179
177
271
210
NIL
deep-freeze
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
274
177
362
210
NIL
heat-up
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

TEXTBOX
10
10
160
28
Initial settings for bugs
11
0.0
0

TEXTBOX
13
230
140
258
Other parameters
11
0.0
0

TEXTBOX
12
156
162
174
Actions
11
0.0
0

TEXTBOX
12
251
153
280
(OK to change during run)
11
0.0
0

BUTTON
430
445
536
478
ideal-temp
set color-by-unhappiness? false\nask turtles [ color-by-ideal-temp ]
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
541
445
657
478
happiness
set color-by-unhappiness? true
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

TEXTBOX
371
466
418
496
Color bug by:
11
0.0
0

BUTTON
430
481
537
514
watch saddest
watch max-one-of turtles [ unhappiness ]
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
541
481
659
514
watch happiest
watch min-one-of turtles [ unhappiness ]
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
662
481
799
514
NIL
reset-perspective
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

Heatbugs is an abstract model of the behavior of biologically-inspired agents that attempt to maintain an optimum temperature around themselves.  It demonstrates how simple rules defining the behavior of agents can produce several different kinds of emergent behavior.

Heatbugs has been used as a demonstration model for many agent-based modeling toolkits. We provide a NetLogo version to assist users in learning and comparing different toolkits.  It demonstrates coding techniques in NetLogo and may be useful as a starting point for building other models.

While this NetLogo model attempts to match the Repast and Swarm versions (see "Credits" below), we haven't done a rigorous comparative analysis of the different versions, so it is possible that there are small inadvertent differences in the underlying rules and behavior.

## HOW IT WORKS

The bugs move around on a grid of square "patches".  A bug may not move to a patch that already has another bug on it.

Each bug radiates a small amount of heat.  Heat gradually diffuses through the world; some heat is lost to cooling.

Each bug has an "ideal" temperature it wants to be.  The bigger the difference between the temperature of the patch where the bug is and the bug's ideal temperature, the more "unhappy" the bug is.  When a bug is unhappy, it moves.  If it is too hot, it moves to the coolest adjacent empty patch.  Conversely, if a bug is too cold, it moves to the warmest adjacent empty patch.  (Note that these bugs aren't smart enough to always move to the best available patch.)

## HOW TO USE IT

After choosing the number of bugs to create, and setting the model variables, press the GO button to set the heatbugs into motion.

BUG-COUNT: The number of bugs that will inhabit the model

EVAPORATION-RATE: The percentage of the world's heat that evaporates each cycle.  A lower number means a world which cools slowly, a higher number is a world which cools quickly.

DIFFUSION-RATE: How much heat a patch (a spot in the world) diffuses to its neighbors.  A higher number means that heat diffuses through the world quickly.  A lower number means that patches retain more of their heat.

MIN/MAX-IDEAL-TEMP: The minimum and maximum ideal temperatures for heatbugs.  Each bug is given an ideal temperature between the min and max ideal temperature.

MIN/MAX-OUTPUT-HEAT: The minimum and maximum heat that heatbugs generate each cycle.  Each bug is given a output-heat value between the min and max output heat.

RANDOM-MOVE-CHANCE: The chance that a bug will make a random move even if it would prefer to stay where it is (because no more ideal patch is available).

DEEP-FREEZE: This button removes all heat from the world.

HEAT-UP: This button adds MAX-OUTPUT-HEAT to every patch in the world.

Beneath the view are two "Color By:" buttons.  The IDEAL-TEMP button colors the bugs according to their IDEAL-TEMP value.  Bugs with higher IDEAL-TEMP values will be brighter.  The HAPPINESS button does the same, but is based upon the HAPPINESS value of each agent, with happier bugs being brighter.

The WATCH-HAPPIEST and WATCH-SADDEST buttons will highlight the happiest or saddest bug at the time the button is pressed.

## THINGS TO NOTICE

Depending on their ideal temperatures, some bugs will tend to clump together, while others will tend to avoid all other bugs, and others still flutter around the edges of clumps.  All of these behaviors are affected as well by the evaporation rate.

The diffusion rate affects the cohesiveness of clumps.  If diffusion-rate is slow, many tiny clumps form.  Why?

Most interesting behaviors occur when the number of bugs, how much heat they generate, and how quickly the world cools are balanced such that excessive heat does not build up.

## THINGS TO TRY

Vary DIFFUSION-RATE.

Vary EVAPORATION-RATE in relation to the output-heat range of the bugs.

Use the HEAT-UP button to scramble clumped heatbugs and watch as they re-assemble into new clumps.

## EXTENDING THE MODEL

Randomize the amount of heat bugs generate each cycle.

Allow users to introduce heat into the system with the mouse.

## NETLOGO FEATURES

`n-of` and `sprout` together let us initially place each bug on its own patch with a minimum of code.

Notice how the code does not make any use of X and Y coordinates.  The `neighbors` and `move-to` primitives take care of sensing and motion on a toroidal grid without the need for any explicit coordinate math.

The `diffuse` command is used to diffuse the heat around the patch grid.

## RELATED MODELS

Slime

## CREDITS AND REFERENCES

Swarm version of Heatbugs -- http://www.swarm.org/wiki/Examples_of_Swarm_applications

RePast version of Heatbugs -- http://repast.sourceforge.net/examples/index.html
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
Rectangle -7500403 true true 0 0 300 300

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

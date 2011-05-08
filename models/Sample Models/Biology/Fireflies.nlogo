turtles-own
[ clock        ;; each firefly's clock
  threshold    ;; the clock tick at which a firefly stops its flash
  reset-level  ;; the clock tick a firefly will reset to when it is triggered by other flashing
  window       ;; a firefly can't reset its cycle if (clock <= window)
]

to setup
  clear-all
  crt number
    [ setxy random-xcor random-ycor
      set clock random (round cycle-length)
      set threshold flash-length
      ifelse strategy = "delay"
      [ set reset-level threshold
        set window -1 ]
      [ set reset-level 0
        set window (threshold + 1) ]
      set size 2  ;; easier to see
      recolor ]
  reset-ticks
end

to go
  ask turtles [
    move
    increment-clock
    if ( (clock > window) and (clock >= threshold) )
      [ look ]
  ]
  ask turtles [
    recolor
  ]
  tick
end

to recolor ; turtle procedure
  ifelse (clock < threshold)
    [ st
      set color yellow ]
    [ set color gray - 2
      ifelse show-dark-fireflies?
        [ st ]
        [ ht ] ]
end

to move ; turtle procedure
  rt random-float 90 - random-float 90
  fd 1
end

to increment-clock ; turtle procedure
  set clock (clock + 1)
  if clock = cycle-length
    [ set clock 0 ]
end

to look ; turtle procedure
  if count turtles in-radius 1 with [color = yellow] >= flashes-to-reset
    [ set clock reset-level ]
end
@#$#@#$#@
GRAPHICS-WINDOW
301
10
737
467
35
35
6.0
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
-35
35
1
1
1
ticks

SLIDER
9
37
282
70
number
number
0
2000
1500
1
1
NIL
HORIZONTAL

SLIDER
54
154
211
187
cycle-length
cycle-length
5
100
10
1
1
NIL
HORIZONTAL

SLIDER
146
118
282
151
flash-length
flash-length
1
10
1
1
1
NIL
HORIZONTAL

SLIDER
11
118
145
151
flashes-to-reset
flashes-to-reset
1
3
1
1
1
NIL
HORIZONTAL

BUTTON
48
190
136
223
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

SWITCH
47
226
229
259
show-dark-fireflies?
show-dark-fireflies?
0
1
-1000

BUTTON
138
190
226
223
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

PLOT
11
268
295
467
Flashing Fireflies
Time
Number
0.0
100.0
0.0
1500.0
true
false
"set-plot-y-range 0 number" ""
PENS
"flashing" 1.0 0 -2674135 true "" "plot count turtles with [color = yellow]"

CHOOSER
82
72
220
117
strategy
strategy
"delay" "advance"
0

@#$#@#$#@
## WHAT IS IT?

This model demonstrates a population of fireflies which synchronize their flashing using only the interactions between the individual fireflies. It is a good example of how a distributed system (i.e. a system with many interacting elements, but no 'leader') can coordinate itself without any central coordinator.

Though most species of firefly are not generally known to synchronize in groups, there are some (for example, Pteroptyx cribellata, Luciola pupilla,and Pteroptyx malaccae) that have been observed to do so in certain settings. This model generalizes two main strategies used by such insects to synchronize with each other (phase delay and phase advance synchronization, as described below), retaining the essentials of the strategies while downplaying biological detail.

## HOW IT WORKS

Each firefly constantly cycles through its own clock, flashing at the beginning of each cycle and then resetting the clock to zero once it has reached the maximum. At the start of each simulation all fireflies begin at a random point in their cycles (though they all have the same cycle lengths) and so flashing will occur erratically through the population. As fireflies perceive other flashes around them they are able to use this information to reset their own clocks to try and synchronize with the other fireflies in their vicinity. Each firefly uses the same set of rules to govern its own clock, and depending on the parameters of the simulation, the population may synchronize more or less effectively.

## HOW TO USE IT

GO: starts and stops the simulation.

SETUP: resets the simulation according to the parameters set by the sliders.

NUMBER: sets the number of fireflies to be created.

CYCLE-LENGTH: sets the length of each firefly's clock before it resets to 0.

FLASHES-TO-RESTART: sets the number of flashes a firefly must see in a single tick before its clock resets.

FLASH-LENGTH: sets the duration, in ticks, of each flash.

STRATEGY: sets the synchronization strategy to be used. One value is phase delay, where upon seeing FLASHES-TO-RESTART flashes a firefly will reset its clock to the FLASH-LENGTH tick (just after a flash would normally occur). This causes the firefly to synchronize with the next flash of the firefly it is responding to. The other value is phase advance, where upon seeing FLASHES-TO-RESTART flashes a firefly will reset its clock to zero. This causes the firefly to flash immediately. Under phase advance, fireflies can only begin to reset their clocks during a window which begins two ticks after they have flashed. This assures that flashes do not get stuck in a short cycle where they persistently reset their clocks and stay lit indefinitely.

SHOW-DARK-FIREFLIES: if switch set to on, non-flashing fireflies are displayed in gray. If switch set to off, non-flashing fireflies are colored black and, thus, invisible.

All settings (except SHOW-DARK-FIREFLIES) must be set before pressing the SETUP button. Changes to the sliders (except SHOW-DARK-FIREFLIES) will have no effect on a simulation in progress.

## THINGS TO NOTICE

Using the default settings (number: 1500, cycle-length: 10, flash-length: 1, number-flashes: 1, strategy: "delay"), notice how local clusters of synchronization begin to form. See if you can figure out where each cluster is represented on the plot. As the simulation proceeds, try to determine which local cluster will eventually "take over" the population. Did this cluster originally have the highest spike on the plot?

In phase advance simulations, why do the plots generally top off before the peaks reach the entire population?

In this model fireflies cannot reset their cycle when they are in the middle of a flash. Why was this restriction imposed?

## THINGS TO TRY

Change the strategy chooser between "delay" and "advance" while keeping the other settings steady (in particular, keep FLASHES-TO-RESTART at 2). Which strategy seems more effective? Why?

Try adjusting FLASHES-TO-RESTART between 0, 1 and 2 using both phase delay and phase advance settings. Notice that each setting will give a characteristically different plot, and some of them do not allow for synchronization at all (for example, with the delay strategy, contrast FLASHES-TO-RESTART set to 1 as opposed to 2). Why does this control make such a difference in the outcome of the simulation?

Changing the number of fireflies in a simulation affects the density of the population (as does adjusting the size of the world). What effect does this have on a simulation?

## EXTENDING THE MODEL

This model explores only two general strategies for attaining synchrony in such cycle-governed fireflies. Can you find any others? Can you improve the existing strategies (i.e., by speeding them up)?

There are many other possible situations in which distributed agents must synchronize their behavior through the the use of simple rules. What if, instead of perceiving only other discrete flashes, an insect could sense where another insect was in its cycle (perhaps by hearing an increasingly loud hum)? What kinds of strategies for synchronization might be useful in such a situation?

If all fireflies had adjustable cycle-lengths (initially set to random intervals) would it then be possible to coordinate both their cycle-lengths and their flashing?

## NETLOGO FEATURES

Note the use of agentsets to count the number of nearby fireflies that are flashing:

    count turtles in-radius 1 with [color = yellow]


## CREDITS AND REFERENCES

Buck, John. (1988). Synchronous Rhythmic Flashing of Fireflies. The Quarterly Review of Biology, September 1988, 265 - 286.

Carlson, A.D. & Copeland, J. (1985). Flash Communication in Fireflies. The Quarterly Review of Biology, December 1985, 415 - 433.
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

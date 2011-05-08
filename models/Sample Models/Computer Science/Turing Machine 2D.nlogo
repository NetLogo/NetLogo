turtles-own [rule-set bg fg state]
patches-own [on?]

to setup
  clear-all
  create-turtles heads [
    set size 5
    set state 0    ;; initialize all head states to 0
    set heading 0
    fd who    ;; have all the turtles line up vertically
    set bg black
    ifelse color-paths?  ;; if show-path? is enabled, color the turtles
      [ set fg color ]
      [ set fg white ]
  ]
  ask patches [ set on? false ] ;; clear cell values
  reset-ticks
end

to setup-example
  if example = 1
    [ apply-rules [[1 180 true] [1 90 true] [0 180 true] [1 270 true]] ]
  if example = 2
    [ apply-rules [[0 270 true] [1 180 false] [1 90 true] [0 0 true]] ]
  if example = 3
    [ apply-rules [[1 90 false] [0 90 true] [0 0 false] [0 180 false]] ]
  if example = 4
    [ apply-rules [[0 90 true] [0 0 false] [0 270 false] [0 0 false]] ]
  if example = 5
    [ apply-rules [[1 180 true] [1 270 true] [0 270 true] [1 90 false]] ]
  setup
end

to go
  ask turtles [ set rule-set find-rule ]
  ask turtles [
    set on? last rule-set
    ifelse on?
      [ set pcolor fg ]
      [ set pcolor bg ]
  ]
  ask turtles [
    rt item 1 rule-set
    fd 1
    set state first rule-set
  ]
  tick
end

;; find the rule to use for current head state and cell state
to-report find-rule  ;; turtle procedure
  ifelse on?  ;; check state of this patch
  [
    if state = 0 [ report fput on-0-state list (direction-to-number on-0-turn) on-0-on? ]
    if state = 1 [ report fput on-1-state list (direction-to-number on-1-turn) on-1-on? ]
  ]
  [
    if state = 0 [ report fput off-0-state list (direction-to-number off-0-turn) off-0-on? ]
    if state = 1 [ report fput off-1-state list (direction-to-number off-1-turn) off-1-on? ]
  ]
end

;; change the current configuration to the given list of rules
to apply-rules [l]
  set off-0-state  item 0 item 0 l
  set off-0-turn   number-to-direction (item 1 item 0 l)
  set off-0-on?    item 2 item 0 l
  set off-1-state  item 0 item 1 l
  set off-1-turn   number-to-direction (item 1 item 1 l)
  set off-1-on?    item 2 item 1 l
  set on-0-state   item 0 item 2 l
  set on-0-turn    number-to-direction (item 1 item 2 l)
  set on-0-on?     item 2 item 2 l
  set on-1-state   item 0 item 3 l
  set on-1-turn    number-to-direction (item 1 item 3 l)
  set on-1-on?     item 2 item 3 l
end

to-report direction-to-number [direction]
  if direction = "--"
    [ report 0 ]
  if direction = "Right"
    [ report 90 ]
  if direction = "Backwards"
    [ report 180 ]
  if direction = "Left"
    [ report 270 ]
end

to-report number-to-direction [number]
  if number = 0
    [ report "--" ]
  if number = 90
    [ report "Right" ]
  if number = 180
    [ report "Backwards" ]
  if number = 270
    [ report "Left" ]
end
@#$#@#$#@
GRAPHICS-WINDOW
493
11
805
344
75
75
2.0
1
10
1
1
1
0
1
1
1
-75
75
-75
75
1
1
1
ticks
30

SLIDER
262
182
369
215
off-0-state
off-0-state
0
1
1
1
1
NIL
HORIZONTAL

SLIDER
260
310
371
343
on-0-state
on-0-state
0
1
0
1
1
NIL
HORIZONTAL

SLIDER
260
246
369
279
off-1-state
off-1-state
0
1
1
1
1
NIL
HORIZONTAL

SLIDER
260
378
370
411
on-1-state
on-1-state
0
1
1
1
1
NIL
HORIZONTAL

BUTTON
6
18
117
51
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
125
18
217
51
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

SWITCH
369
182
480
215
off-0-on?
off-0-on?
0
1
-1000

SWITCH
372
310
480
343
on-0-on?
on-0-on?
0
1
-1000

SWITCH
369
245
478
278
off-1-on?
off-1-on?
0
1
-1000

SWITCH
371
378
481
411
on-1-on?
on-1-on?
0
1
-1000

TEXTBOX
270
146
370
164
New Head State
11
0.0
0

TEXTBOX
139
144
239
162
Turn
11
0.0
0

TEXTBOX
381
146
471
164
New Cell State
11
0.0
0

SLIDER
225
19
377
52
heads
heads
1
256
32
1
1
NIL
HORIZONTAL

SWITCH
225
57
377
90
color-paths?
color-paths?
0
1
-1000

BUTTON
125
56
217
89
step
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

SLIDER
7
89
118
122
example
example
1
5
1
1
1
NIL
HORIZONTAL

BUTTON
7
56
118
89
setup example
setup-example
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
17
181
122
226
If cell is off and head is in state 0:
11
0.0
0

TEXTBOX
16
240
121
285
If cell is off and head is in state 1:
11
0.0
0

TEXTBOX
16
308
121
353
If cell is on and head is in state 0:
11
0.0
0

TEXTBOX
17
377
119
422
If cell is on and head is in state 1:
11
0.0
0

CHOOSER
124
176
262
221
off-0-turn
off-0-turn
"--" "Right" "Backwards" "Left"
2

CHOOSER
123
240
261
285
off-1-turn
off-1-turn
"--" "Right" "Backwards" "Left"
1

CHOOSER
123
304
261
349
on-0-turn
on-0-turn
"--" "Right" "Backwards" "Left"
0

CHOOSER
123
371
261
416
on-1-turn
on-1-turn
"--" "Right" "Backwards" "Left"
3

@#$#@#$#@
## WHAT IS IT?

This is a model of a multi-headed Turing machine on a 2 dimensional tape (2D MTM), which is an extension of the original machine proposed by Turing in 1936. 

A Turing machine is a simple computing machine capable of doing anything a modern computer can do.  A traditional Turing machine has a single processor, or "head," with a limited number of internal states that change depending on the data that is read by the head.  As the Turing machine computes, the head moves back and forth across the tape, changing its internal state and the value of the current cell.

A multi-headed Turing machine on a 2 dimensional tape (2D MTM) can be emulated on a single headed one dimensional Turing machine (TM), but the rules of a 2D MTM may be significantly simpler than those of a traditional TM emulating a 2D MTM.  In the model, there can be up to 256 "heads" which all follow the same set of basic rules.  In this 2-state 2D MTM, the head may change its state, the value of its current cell, and move either north, east, west, or south, depending on whether or not the current cell is on or off and its current state.

This model demonstrates how multiple processors may interact with each other to complete complex processing tasks. It also shows that these machines can be heavily dependent on the number of processing agents. In addition to exploring the complexity of parallel computation, this model also demonstrates some of the caveats of parallel algorithms.

## HOW IT WORKS

On each turn, each head moves forward. Depending on whether the patch ("cell") it's over is "on" (showing color) or "off" (invisible), the head will use the rule which transitions from its current state to the new state. In other words, if head A (which is in state 1, with position 2) is over cell B (which is on), then the on-1-state will show what the next state will be, the on-1-position slider will show what the next position will be for that head (0: North, 1: East, 2: South, 3: West), and if on-1-on? is true, then it will remain on. If on-1-on? is false, then it will turn off, becoming invisible.

## HOW TO USE IT

HEADS: the number of heads used by the Turing machine

COLOR-PATHS?: Tells the heads to write color information onto the cell. This feature allows the user to analyze both the information written and the process in which it was written. It is only a device to keep track of which cell had been most recently turned on by a particular head; it does not affect on/off data written on the tape: colored cells are on, black cells are off.

HEAD-SIZE: How large the head, or turtle appears.

The rules of all heads are given by three parameters:  
[cell state]-[head state]-state [cell state]-[head state]-position [cell state]-[head state]-on?

The prefix of each slider/toggle refers to the current state of the machine and the cell it is on.

First prefix  - current cell state: on/off  
Second prefix - current state of head, which may be either 0 or 1

These prefixes specify the initial state which the following rules are applied to:

Turn- specifies what direction the head will turn before moving forward 1 cell  
New Head State- specifies the new head state  
New Cell State- specifies the state to apply to the current cell

## THINGS TO NOTICE

Here are some interesting phenomena that occur with the example setups:

Example 1: In the first example, the machine does nothing with only one head. With two or more heads, the heads cooperate with each other to fill the tape with data in a repetitive fashion forming a shape similar to a square rotated by 45 degrees.  When the heads wrap around and converge with the written data, they get stuck in a loop and do not write any more data. If the tape was infinite, the growth of the square would be unbounded.

Example 2: Like the first example, the processors also aid each other in filling up the tape with data.  The pattern they form is less symmetric, and two processors may get stuck in a small loop with each other as the tape gets filled with data, potentially stunting the growth of the data.  This is an example where multiprocessing fails to work effectively.  This is analogous common problems faced by parallel algorithms. The processing of shared resources between multiple processors may conflict with proper functioning.

Example 3: This example demonstrates the codependency of processors to perform complex computational tasks.  If a head does not have a pair, it will get stuck in a tight loop and not be able to process data.  The minimum number of heads required to produce chaotic behavior is 3 heads.  Greater numbers of heads will produce chaotic behavior that will most likely produce complex patterns.  Often times, the movement of heads is short lived and the entire machine will get stuck in a loop.

Example 4: This particular type of Turing machine is commonly known as Langton's vants (see references for more information).  In this example, the head evaluates only the state of the current cell. If it is black, it changes the cell to white, turns white, and moves forward. If the cell is white, it turns off the cell, and turns left. With a single head, the van" moves chaotically on the tape, eventually creating "highways" of repetitive patterns that diverge from the main cluster.  The behavior of this machine is highly dependent on its initial configuration.  When small odd numbers of heads are created, they behave like the classic vants, moving randomly, building highways which other heads may follow and possibly deconstruct. When the machine is initially setup with an even number of heads arranged vertically, the heads will follow each other forming a shape similar to a rotated square which is constantly expanding. Once the square wraps around the horizontal edges, the system becomes chaotic.  This is an example where pairs of "chaotic" computational machines may combine to produce a somewhat stable behavior.

Example 5: The rules of this machine is very similar to that of Langton's vants, and its behavior is similar but has more stable properties. With two processors, there is a tight loop that doesn't seem to go anywhere.  Larger even numbers of heads create a 1-bit path which stably oscillates back and forth.  The processing of data remains constrained.  Odd numbers of heads start off similarly to even numbers of heads, but unwind as they oscillate.  Their growth is unconstrained and chaotic, with behavior very similar to vants.  When the number of heads is even and exceeds 128 processors (the tape has 256x256 cells), the end of the path being drawn hits the point of origin of the path and behaves similarly to configurations with larger odd numbers of heads.  This model demonstrates that pairs of "chaotic" heads can produce very stable behavior given the proper initial conditions.

## THINGS TO TRY

Start by tinkering around with the number of heads in the example models, and consider the following:

How do the heads interact with each other?

What can you conclude about the number of heads in relation to the complexity of their execution?

Can you find any other configurations that will yield interesting results?

## EXTENDING THE MODEL

Change the starting position of the heads.  How does this affect the execution of the 2D MTM?

What happens to the execution of configurations when they start with random bits of data on the tape?

Try adding more states to the cells and/or heads to the model.  Do different types of complexities emerge?  Are there more states that yield complex behavior?

## CREDITS AND REFERENCES

Langton, C. 1984. Self-reproduction in cellular automata. Physica D 10, 134-144  
Langton, C. 1986. Studying artificial life with cellular automata. Physica D 22, 120-149
Sipser, M. 1997. Introduction to the Theory of Computation. PWS Publishing 125-147.  
Wolfram, S. 2002. A New Kind of Science. Wolfram Media Inc.  Champaign, IL.  
Pegg, E. 2002. Turmite. http://mathworld.wolfram.com/Turmite.html.

Thanks to Ethan Bakshy for his work on this model.
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
repeat 525 [ go ]
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

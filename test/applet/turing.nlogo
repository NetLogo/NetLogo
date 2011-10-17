turtles-own [rule-set bg fg state]
patches-own [on?]
__includes [ "appletinclude.nls" ]

to startup
  setup 
  repeat 500 [ go ]
  if ( not includedReporter ) [ error "include file definition not read" ]
end

to setup
  clear-all
  crt heads
  [
    set size 5
    set state 0    ;; initialize all head states to 0
    set heading 0
    fd who    ;; have all the turtles line up vertically
    set bg black
    ifelse color-paths?  ;; if show-path? is enabled, color the turtles
      [ set fg color ]
      [ set fg white ]
  ]
  ask patches
  [
    set on? false  ;; clear cell values
  ]
  reset-ticks
end

to setup-example
  if (example = 1)
    [ apply-rules [[1 180 true] [1 90 true] [0 180 true] [1 270 true]] ]
  if (example = 2)
    [ apply-rules [[0 270 true] [1 180 false] [1 90 true] [0 0 true]] ]
  if (example = 3)
    [ apply-rules [[1 90 false] [0 90 true] [0 0 false] [0 180 false]] ]
  if (example = 4)
    [ apply-rules [[0 90 true] [0 0 false] [0 270 false] [0 0 false]] ]
  if (example = 5)
    [ apply-rules [[1 180 true] [1 270 true] [0 270 true] [1 90 false]] ]
  setup
end

to go
  ask turtles
  [
    set rule-set find-rule
  ]
  ask turtles
  [
    set on? last rule-set
    ifelse on?
      [ set pcolor fg ]
      [ set pcolor bg ]
  ]
  ask turtles
  [
    rt (item 1 rule-set)
    fd 1
    set state (first rule-set)
  ]
  tick
end

;; find the rule to use for current head state and cell state
to-report find-rule  ;; turtle procedure
  ifelse on?  ;; check state of this patch
  [
    if state = 0 [ report (fput on-0-state list (direction-to-number on-0-turn) on-0-on?) ]
    if state = 1 [ report (fput on-1-state list (direction-to-number on-1-turn) on-1-on?) ]
  ]
  [
    if state = 0 [ report (fput off-0-state list (direction-to-number off-0-turn) off-0-on?) ]
    if state = 1 [ report (fput off-1-state list (direction-to-number off-1-turn) off-1-on?) ]
  ]
end

;; change the current configuration to the given list of rules
to apply-rules [l]
  set off-0-state  (item 0 item 0 l)
  set off-0-turn   (number-to-direction (item 1 item 0 l))
  set off-0-on?    (item 2 item 0 l)
  set off-1-state  (item 0 item 1 l)
  set off-1-turn   (number-to-direction (item 1 item 1 l))
  set off-1-on?    (item 2 item 1 l)
  set on-0-state   (item 0 item 2 l)
  set on-0-turn    (number-to-direction (item 1 item 2 l))
  set on-0-on?     (item 2 item 2 l)
  set on-1-state   (item 0 item 3 l)
  set on-1-turn    (number-to-direction (item 1 item 3 l))
  set on-1-on?     (item 2 item 3 l)
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
30.0

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
12
0.0
0

TEXTBOX
139
144
239
162
Turn
12
0.0
0

TEXTBOX
381
146
471
164
New Cell State
12
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
12
0.0
0

TEXTBOX
16
240
121
285
If cell is off and head is in state 1:
12
0.0
0

TEXTBOX
16
308
121
353
If cell is on and head is in state 0:
12
0.0
0

TEXTBOX
17
377
119
422
If cell is on and head is in state 1:
12
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
2

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

A Turing machine, first proposed by Alan Turing in 1936, is a simple computing machine capable of doing anything a modern computer can do.  A traditional Turing machine has a single processor, or "head," with a limited number of internal states that change depending on the data that is read by the head.  As the Turing machine computes, the head moves back and forth across the tape, changing its internal state and the value of the current cell.

This is a model of a multi-headed Turing machine on a 2 dimensional tape (2D MTM), which is an extension of the original machine proposed by Turing in 1936. 2D MTMs can be emulated on a single headed one dimensional Turing machine (TM), but the rules of a 2D MTM may be significantly simpler than those of a traditional TM emulating a 2D MTM.  In the model, there can be up to 256 "heads" which all follow the same set of basic rules.  In this 2-state 2D MTM, the head may change its state, the value of its current cell, and move either north, east, west, or south, depending on whether or not the current cell is on or off and its current state.

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

ant
true
0
Polygon -7500403 true true 136 61 129 46 144 30 119 45 124 60 114 82 97 37 132 10 93 36 111 84 127 105 172 105 189 84 208 35 171 11 202 35 204 37 186 82 177 60 180 44 159 32 170 44 165 60
Polygon -7500403 true true 150 95 135 103 139 117 125 149 137 180 135 196 150 204 166 195 161 180 174 150 158 116 164 102
Polygon -7500403 true true 149 186 128 197 114 232 134 270 149 282 166 270 185 232 171 195 149 186
Polygon -7500403 true true 225 66 230 107 159 122 161 127 234 111 236 106
Polygon -7500403 true true 78 58 99 116 139 123 137 128 95 119
Polygon -7500403 true true 48 103 90 147 129 147 130 151 86 151
Polygon -7500403 true true 65 224 92 171 134 160 135 164 95 175
Polygon -7500403 true true 235 222 210 170 163 162 161 166 208 174
Polygon -7500403 true true 249 107 211 147 168 147 168 150 213 150

arrow
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

bee
true
0
Polygon -1184463 true false 152 149 77 163 67 195 67 211 74 234 85 252 100 264 116 276 134 286 151 300 167 285 182 278 206 260 220 242 226 218 226 195 222 166
Polygon -16777216 true false 150 149 128 151 114 151 98 145 80 122 80 103 81 83 95 67 117 58 141 54 151 53 177 55 195 66 207 82 211 94 211 116 204 139 189 149 171 152
Polygon -7500403 true true 151 54 119 59 96 60 81 50 78 39 87 25 103 18 115 23 121 13 150 1 180 14 189 23 197 17 210 19 222 30 222 44 212 57 192 58
Polygon -16777216 true false 70 185 74 171 223 172 224 186
Polygon -16777216 true false 67 211 71 226 224 226 225 211 67 211
Polygon -16777216 true false 91 257 106 269 195 269 211 255
Line -1 false 144 100 70 87
Line -1 false 70 87 45 87
Line -1 false 45 86 26 97
Line -1 false 26 96 22 115
Line -1 false 22 115 25 130
Line -1 false 26 131 37 141
Line -1 false 37 141 55 144
Line -1 false 55 143 143 101
Line -1 false 141 100 227 138
Line -1 false 227 138 241 137
Line -1 false 241 137 249 129
Line -1 false 249 129 254 110
Line -1 false 253 108 248 97
Line -1 false 249 95 235 82
Line -1 false 235 82 144 100

bird1
false
0
Polygon -7500403 true true 2 6 2 39 270 298 297 298 299 271 187 160 279 75 276 22 100 67 31 0

bird2
false
0
Polygon -7500403 true true 2 4 33 4 298 270 298 298 272 298 155 184 117 289 61 295 61 105 0 43

boat1
false
0
Polygon -1 true false 63 162 90 207 223 207 290 162
Rectangle -6459832 true false 150 32 157 162
Polygon -13345367 true false 150 34 131 49 145 47 147 48 149 49
Polygon -7500403 true true 158 33 230 157 182 150 169 151 157 156
Polygon -7500403 true true 149 55 88 143 103 139 111 136 117 139 126 145 130 147 139 147 146 146 149 55

boat2
false
0
Polygon -1 true false 63 162 90 207 223 207 290 162
Rectangle -6459832 true false 150 32 157 162
Polygon -13345367 true false 150 34 131 49 145 47 147 48 149 49
Polygon -7500403 true true 157 54 175 79 174 96 185 102 178 112 194 124 196 131 190 139 192 146 211 151 216 154 157 154
Polygon -7500403 true true 150 74 146 91 139 99 143 114 141 123 137 126 131 129 132 139 142 136 126 142 119 147 148 147

boat3
false
0
Polygon -1 true false 63 162 90 207 223 207 290 162
Rectangle -6459832 true false 150 32 157 162
Polygon -13345367 true false 150 34 131 49 145 47 147 48 149 49
Polygon -7500403 true true 158 37 172 45 188 59 202 79 217 109 220 130 218 147 204 156 158 156 161 142 170 123 170 102 169 88 165 62
Polygon -7500403 true true 149 66 142 78 139 96 141 111 146 139 148 147 110 147 113 131 118 106 126 71

box
true
0
Polygon -7500403 true true 45 255 255 255 255 45 45 45

butterfly1
true
0
Polygon -16777216 true false 151 76 138 91 138 284 150 296 162 286 162 91
Polygon -7500403 true true 164 106 184 79 205 61 236 48 259 53 279 86 287 119 289 158 278 177 256 182 164 181
Polygon -7500403 true true 136 110 119 82 110 71 85 61 59 48 36 56 17 88 6 115 2 147 15 178 134 178
Polygon -7500403 true true 46 181 28 227 50 255 77 273 112 283 135 274 135 180
Polygon -7500403 true true 165 185 254 184 272 224 255 251 236 267 191 283 164 276
Line -7500403 true 167 47 159 82
Line -7500403 true 136 47 145 81
Circle -7500403 true true 165 45 8
Circle -7500403 true true 134 45 6
Circle -7500403 true true 133 44 7
Circle -7500403 true true 133 43 8

circle
false
0
Circle -7500403 true true 35 35 230

link
true
0
Line -7500403 true 150 0 150 300

link direction
true
0
Line -7500403 true 150 150 30 225
Line -7500403 true 150 150 270 225

person
false
0
Rectangle -7500403 true true 105 120 195 210
Circle -7500403 true true 105 15 90
Rectangle -7500403 true true 60 105 240 135
Rectangle -7500403 true true 105 195 135 270
Rectangle -7500403 true true 165 195 195 270

spacecraft
true
0
Polygon -7500403 true true 150 0 180 135 255 255 225 240 150 180 75 240 45 255 120 135

thin-arrow
true
0
Polygon -7500403 true true 150 0 0 150 120 150 120 293 180 293 180 150 300 150

truck-down
false
0
Polygon -7500403 true true 225 30 225 270 120 270 105 210 60 180 45 30 105 60 105 30
Polygon -8630108 true false 195 75 195 120 240 120 240 75
Polygon -8630108 true false 195 225 195 180 240 180 240 225

truck-left
false
0
Polygon -7500403 true true 120 135 225 135 225 210 75 210 75 165 105 165
Polygon -8630108 true false 90 210 105 225 120 210
Polygon -8630108 true false 180 210 195 225 210 210

truck-right
false
0
Polygon -7500403 true true 180 135 75 135 75 210 225 210 225 165 195 165
Polygon -8630108 true false 210 210 195 225 180 210
Polygon -8630108 true false 120 210 105 225 90 210

turtle
true
0
Polygon -7500403 true true 138 75 162 75 165 105 225 105 225 142 195 135 195 187 225 195 225 225 195 217 195 202 105 202 105 217 75 225 75 195 105 187 105 135 75 142 75 105 135 105

wolf-left
false
3
Polygon -6459832 true true 117 97 91 74 66 74 60 85 36 85 38 92 44 97 62 97 81 117 84 134 92 147 109 152 136 144 174 144 174 103 143 103 134 97
Polygon -6459832 true true 87 80 79 55 76 79
Polygon -6459832 true true 81 75 70 58 73 82
Polygon -6459832 true true 99 131 76 152 76 163 96 182 104 182 109 173 102 167 99 173 87 159 104 140
Polygon -6459832 true true 107 138 107 186 98 190 99 196 112 196 115 190
Polygon -6459832 true true 116 140 114 189 105 137
Rectangle -6459832 true true 109 150 114 192
Rectangle -6459832 true true 111 143 116 191
Polygon -6459832 true true 168 106 184 98 205 98 218 115 218 137 186 164 196 176 195 194 178 195 178 183 188 183 169 164 173 144
Polygon -6459832 true true 207 140 200 163 206 175 207 192 193 189 192 177 198 176 185 150
Polygon -6459832 true true 214 134 203 168 192 148
Polygon -6459832 true true 204 151 203 176 193 148
Polygon -6459832 true true 207 103 221 98 236 101 243 115 243 128 256 142 239 143 233 133 225 115 214 114

wolf-right
false
3
Polygon -6459832 true true 170 127 200 93 231 93 237 103 262 103 261 113 253 119 231 119 215 143 213 160 208 173 189 187 169 190 154 190 126 180 106 171 72 171 73 126 122 126 144 123 159 123
Polygon -6459832 true true 201 99 214 69 215 99
Polygon -6459832 true true 207 98 223 71 220 101
Polygon -6459832 true true 184 172 189 234 203 238 203 246 187 247 180 239 171 180
Polygon -6459832 true true 197 174 204 220 218 224 219 234 201 232 195 225 179 179
Polygon -6459832 true true 78 167 95 187 95 208 79 220 92 234 98 235 100 249 81 246 76 241 61 212 65 195 52 170 45 150 44 128 55 121 69 121 81 135
Polygon -6459832 true true 48 143 58 141
Polygon -6459832 true true 46 136 68 137
Polygon -6459832 true true 45 129 35 142 37 159 53 192 47 210 62 238 80 237
Line -16777216 false 74 237 59 213
Line -16777216 false 59 213 59 212
Line -16777216 false 58 211 67 192
Polygon -6459832 true true 38 138 66 149
Polygon -6459832 true true 46 128 33 120 21 118 11 123 3 138 5 160 13 178 9 192 0 199 20 196 25 179 24 161 25 148 45 140
Polygon -6459832 true true 67 122 96 126 63 144

@#$#@#$#@
NetLogo 5.0beta3
@#$#@#$#@
setup
repeat 525 [ go ]
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

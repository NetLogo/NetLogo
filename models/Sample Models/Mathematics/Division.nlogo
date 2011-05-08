globals [
  block-to-go              ;; which block is acting during go
  circles-on-block-to-go
  other-block              ;; the block with which block-to-go compares itself
  circles-on-other-block
]

patches-own [ block-number ]

;;;;;;;;;;;;;;;;;;;;;;
;; SETUP PROCEDURES ;;
;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  setup-blocks
  setup-circles
  set block-to-go 0
  set circles-on-block-to-go circles-on-block block-to-go
  reset-ticks
end

to setup-blocks
  let size-of-columns floor (world-width / (divisor + 1))
  let extra-in-base world-width mod (divisor + 1)
  let active-block-number 0
  let active-column-number 0
  ;; draws a few extra columns in the base
  repeat extra-in-base + size-of-columns
         [ ask patches with [ pxcor = active-column-number + min-pxcor ]
             [ set pcolor black
               set block-number 0
             ]
           set active-column-number active-column-number + 1
         ]
  set active-block-number 1
  ;; now the rest of the blocks get drawn
  repeat divisor
    [ repeat size-of-columns
        [ ask patches with [ pxcor = active-column-number + min-pxcor ]
            [ set pcolor 10 * active-block-number + 5
              set block-number active-block-number
            ]
          set active-column-number active-column-number + 1
        ]
      set active-block-number active-block-number + 1
    ]
  ;; now go through and give each block a label with its number
  set active-block-number 0
  let label-pxcor min-pxcor
  ask patch label-pxcor min-pycor
        [ set plabel-color white
          set plabel active-block-number
        ]
  set active-block-number active-block-number + 1
  set label-pxcor label-pxcor + extra-in-base
  repeat divisor
    [ set label-pxcor label-pxcor + size-of-columns
      ask patch label-pxcor min-pycor
        [ set plabel-color black
          set plabel active-block-number
        ]
      set active-block-number active-block-number + 1
    ]
end

to setup-circles
  set-default-shape turtles "circle"
  crt dividend [ set color white
                 setxy ( min-pxcor ) random-ycor
               ]
end


;;;;;;;;;;;;;;;;;;;;;;;;
;; RUNTIME PROCEDURES ;;
;;;;;;;;;;;;;;;;;;;;;;;;

to go
  if done? [ stop ]
  compare-blocks
  get-new-blocks
  tick
end

to compare-blocks
  ifelse block-to-go = 0
    [ if circles-on-block-to-go >= divisor
        [ ask turtles with [ block-number = 0 ]
            [ set xcor [pxcor] of one-of
                patches with [ block-number = ((random divisor) + 1)]
            ]
        ]
    ]
    [ ifelse (circles-on-block-to-go - circles-on-other-block) > 1
        [ move-turtle block-to-go other-block ]
        [ if (circles-on-block-to-go - circles-on-other-block) = 1
            [ move-turtle block-to-go 0 ]
        ]
    ]
end

to get-new-blocks
  ifelse cycle-in-order?
    [ set block-to-go block-to-go + 1 ]
    [ set block-to-go random (divisor + 1) ]
  if (block-to-go > divisor)
    [ set block-to-go 0 ]
  set circles-on-block-to-go circles-on-block block-to-go

  ifelse not next-is-other?
    [ set other-block (random divisor) + 1
      while [ other-block = block-to-go ]
        [ set other-block (random divisor) + 1 ]
    ]
    [ ifelse block-to-go != divisor
        [ set other-block block-to-go + 1 ]
        [ set other-block 1 ]
    ]
  set circles-on-other-block circles-on-block other-block
end

;; checks to see if anything is going to change
;; if nothing is going to, returns true
to-report done?
  report equal-stripes-check and (circles-on-block 0 < divisor)
end


;;;;;;;;;;;;;;;;;;;;;;;
;; MONITOR REPORTERS ;;
;;;;;;;;;;;;;;;;;;;;;;;

to-report average-answer
  report count turtles-on patches with [block-number != 0] / divisor
end

to-report remainder-count
  report count turtles-on patches with [block-number = 0]
end


;;;;;;;;;;;;;;;;;;;;;;;
;; HELPER PROCEDURES ;;
;;;;;;;;;;;;;;;;;;;;;;;

; reports how many circles are on the given block
to-report circles-on-block [ index ]
  report count turtles-on patches with [block-number = index]
end

to move-turtle [ from-block to-block ]
  ask one-of turtles with [ block-number = from-block ]
    [ set xcor ([pxcor] of one-of patches with [ block-number = to-block]) ]
end

to-report equal-stripes-check
  let equal-stripes-answer true
  let index 1
    repeat divisor - 1
      [ if circles-on-block index != circles-on-block (index + 1)
          [ set equal-stripes-answer false ]
        set index index + 1
      ]
  report equal-stripes-answer
end
@#$#@#$#@
GRAPHICS-WINDOW
11
82
429
281
25
10
8.0
1
10
1
1
1
0
0
0
1
-25
25
-10
10
1
1
1
ticks
30

BUTTON
10
10
91
43
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
10
42
91
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
106
10
278
43
dividend
dividend
0.0
100.0
65
1.0
1
NIL
HORIZONTAL

SLIDER
106
42
278
75
divisor
divisor
1.0
20.0
14
1.0
1
NIL
HORIZONTAL

BUTTON
305
24
388
58
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

MONITOR
280
289
339
334
quotient
average-answer
2
1
11

MONITOR
73
290
143
335
remainder
remainder-count
0
1
11

MONITOR
433
112
516
157
NIL
block-to-go
0
1
11

MONITOR
433
160
516
205
NIL
other-block
0
1
11

MONITOR
516
112
666
157
NIL
circles-on-block-to-go
0
1
11

MONITOR
516
160
666
205
NIL
circles-on-other-block
0
1
11

SWITCH
432
42
573
75
cycle-in-order?
cycle-in-order?
1
1
-1000

SWITCH
432
74
573
107
next-is-other?
next-is-other?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

"What's 65 divided by 14?" There are many ways to answer this question. Some examples are 4.643, 4, four and nine fourteenths, and 4 with 9 left over. The last answer is an example of the kind of result this model would give.

Just as there are many ways to answer the question of "What's 65 divided by 14?" there are many methods to coming up with each answer.  This model shows an interesting method that distributes the work of finding a solution among a group of separate agents.

DIVIDEND circles get spread out as evenly as possible among the DIVISOR colorful blocks. The result is QUOTIENT with REMAINDER left over on the black block to the far left. Note that REMAINDER is smaller than the number of colorful blocks. Otherwise, each of the colorful blocks could have at least one more circle.

## HOW IT WORKS

The blocks take turns acting trying to accomplish the goal of distributing the circles evenly.

The colorful blocks act as follows: The block that's acting compares how many circles it has on itself with how many circles are on another colorful block. If it has at least two more than the other block, it can afford to give the other block one, so it does. If the acting block has exactly one more circle than the other block, it sends that extra circle to the black block on the far left. Since the colorful blocks act in this way, no colorful block can have more circles on it than any other colorful block.

The black block is special. While the colorful blocks are trying to spread the circles out evenly, the black block is keeping the extra circles. When the black block acts, it checks how many circles it has on it. If it has enough at least enough to put one circle on each of the colorful blocks, it sends all of the circles on it out to the colorful blocks randomly. Since the black block acts in this way, the maximum number of circles will be spread evenly on the colorful blocks.

## HOW TO USE IT

Set DIVIDEND and DIVISOR to the desired numbers, press SETUP to set the problem up, and press GO to have the model find the answer.

DIVIDEND is the number of circles that will get spread out.

DIVISOR is the number of colorful blocks the circles will be spread out onto.

SETUP sets the problem up.

GO solves the problem.

When the model stops, the QUOTIENT monitor displays the number of circles on each of the colorful blocks.

When the model stops, the REMAINDER monitor displays the number of circles on the black block, which is the number of circles left over.

If CYCLE-IN-ORDER? is on, the blocks take turns acting in order. If it's off, the next block to act is chosen randomly.

IF NEXT-IS-OTHER? is on, the colorful blocks compare with the block to their right. If it's off, they compare with a random other colorful block.

To help watch the algorithm working, there are a few other monitors and a GO ONCE button that has just one block act at a time.

BLOCK-TO-GO shows which block is acting.

CIRCLES-ON-BLOCK-TO-GO show how many circles are on BLOCK-TO-GO.

OTHER-BLOCK shows which block BLOCK-TO-GO is comparing itself with.

CIRCLES-ON-OTHER-BLOCK shows how many circles are on OTHER-BLOCK.

## THINGS TO NOTICE

Sometimes, the circles are piled up on top of each other. When this happens, it may look like there are fewer circles on a block than there actually are.

The time it takes for the algorithm to work varies quite a bit, even with the same settings.

Different remainders change how long the algorithm takes. What remainders are faster? What remainders are slower?

What else effects how long the algorithm takes? In what ways?

## THINGS TO TRY

What switches, if any, make a difference in the time it takes for the algorithm to work and in what way?

Can you find settings that you know the algorithm will never work for? If so, how do you know?

## EXTENDING THE MODEL

The QUOTIENT monitor finds the average number of circles on the colorful blocks. Finding the average involves dividing by DIVISOR. Figure out another way to display the QUOTIENT that doesn't use division.

Make a way to time the model.

Don't allow more than one circle to be on a single patch within a block unless there's no other way to fit the circles onto the block.

Modify the algorithm to speed it up. Some ideas: Try having the blocks balance out with each other as much as possible before moving on to the next block. Try having the algorithm "settle twice", that is, go through once trying to share among the colorful blocks and once to send extras to the remainder.

## CREDITS AND REFERENCES

The algorithm that this model uses comes from a post to the starlogo-users mailing list by Ted Kaehler.

Thanks to Josh Unterman for his work on this model.
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

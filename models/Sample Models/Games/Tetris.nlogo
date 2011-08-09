globals [
  score
  level
  lines
  next-shape   ;; we number the different piece shapes 0 through 6
  game-over?
]
breed [ pieces piece ]   ;; pieces fall through the air...
breed [ blocks block ]   ;; ...and when they land, they become blocks

pieces-own [
  x y      ;; these are the piece's offsets relative to turtle 0
]
blocks-own [
  line     ;; the bottom line is line 0, above it is line 1, and so on
]

;;;;;;;;;;;;;
;;; Notes ;;;
;;;;;;;;;;;;;

;  It's a little tricky to make it so that the four turtles that make
;  up each piece always move together.  There are various possible ways
;  that you could arrange this, but here's how we chose to make it work.
;  We only ever make four turtles of the "pieces" breed, and we reuse
;  those same four turtles to make up each successive piece that falls.
;  Since they are the first four turtles created, we know that their
;  "who" numbers will be 0, 1, 2, and 3.  turtle 0 is special; it's
;  the turtle around which the others rotate when the user rotates
;  the piece.  The breed variables "x" and "y" hold each turtle's
;  offsets from turtle 0.

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;
to new
  clear-all
  set-default-shape turtles "square big"
  set score 0
  set level starting-level
  set lines 0
  set next-shape random 7
  set game-over? false
  ;; make the piece turtles first so they get who numbers of 0 through 3
  create-pieces 4
  [ set heading 180 ]
  ;; draw the board
  ask patches
  [ ifelse (pxcor = -9) or (pxcor > 3) or (pycor = min-pycor)
    [ set pcolor gray ]
    [ ;; make debris
      if ((pycor + max-pycor) <= debris-level and
          (pycor + max-pycor) > 0 and
          (random 2 = 0))
      [ sprout-blocks 1
        [ set line (pycor + max-pycor)
          set heading 180
          set color blue ]
      ]
    ]
  ]
  ;; make the "Next Piece" area
  ask patch 6 10 [ set plabel "Next" ]
  ask patches with [((pxcor > 4) and (pxcor < 9) and
                     (pycor > 5) and (pycor < 11))]
  [ set pcolor black ]
  ;; setup the new piece
  new-piece
end

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Interface Buttons ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;
to rotate-right
  if rotate-right-clear? and not game-over?
    [ ask pieces [ rotate-me-right ] ]
end

to rotate-me-right  ;; Piece Procedure
  let oldx x
  let oldy y
  set x oldy
  set y (- oldx)
  set xcor ([xcor] of turtle 0) + x
  set ycor ([ycor] of turtle 0) + y
end

to rotate-left
  if rotate-left-clear? and not game-over?
    [ ask pieces [ rotate-me-left ] ]
end

to rotate-me-left  ;; Piece Procedure
  let oldx x
  let oldy y
  set x (- oldy)
  set y oldx
  set xcor ([xcor] of turtle 0) + x
  set ycor ([ycor] of turtle 0) + y
end

to shift-right
  if (clear-at? 1 0) and not game-over?
    [ ask pieces [ set xcor xcor + 1 ] ]
end

to shift-left
  if (clear-at? -1 0) and not game-over?
    [ ask pieces [ set xcor xcor - 1 ] ]
end

to shift-down
  ifelse (clear-at? 0 -1) and not game-over?
    [ ask pieces [ fd 1 ] ]
    [ place-piece ]
end

to drop-down
  while [(clear-at? 0 -1) and not game-over?]
    [ ask pieces [ fd 1 ] display ]
  place-piece
end

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Runtime Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;
to play
  if game-over? [ stop ]
  every (0.01 + 0.12 * (10 - level))
    [ shift-down
      check-lines ]
  display
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Overlap prevention Reporters ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to-report clear? [p]  ;; p is a patch
  if p = nobody [ report false ]
  report (not any? blocks-on p) and ([pcolor] of p != gray)
end

to-report clear-at? [xoff yoff]
  report all? pieces [clear? patch-at xoff yoff]
end

to-report rotate-left-clear?
  report all? pieces [clear? patch-at (- y) x]
end

to-report rotate-right-clear?
  report all? pieces [clear? patch-at y (- x)]
end

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Blocks Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;
to check-lines
  let n 1
  let line-count 0
  repeat (2 * max-pycor)
  [ ifelse (count blocks with [line = n] = 12)
    [ delete-line n
      set line-count line-count + 1 ]
    [ set n n + 1 ]
  ]
  score-lines line-count
  set lines lines + line-count
  if (floor (lines / 10) > level)
  [ set level floor (lines / 10) ]
end

to delete-line [n]
  ask blocks with [line = n] [ die ]
  ask blocks with [line > n]
  [ fd 1
    set line (pycor + max-pycor) ]
end

to score-lines [n]
  let bonus 0
  if n = 1 [ set bonus 50   * (level + 1) ]
  if n = 2 [ set bonus 150  * (level + 1) ]
  if n = 3 [ set bonus 350  * (level + 1) ]
  if n = 4 [ set bonus 1000 * (level + 1) ]
  if (not any? blocks) and (n > 0)
  [ set bonus bonus + 2000 * (level + 1) ]
  set score score + bonus
end

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Pieces Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;
to place-piece
  ask pieces
  [ hatch-blocks 1
    [ set line (pycor + max-pycor) ]
  ]
  set score score + 10 * (level + 1)
  new-piece
end

to new-piece
  let new-shape next-shape
  set next-shape random 7
  if (show-next?) [ show-next-piece ]
  ask turtle 0 [ setxy -3 12 ]
  ask pieces
  [ setup-piece new-shape ]
  if any? pieces with [any? other turtles-here]
  [ user-message "Game Over"
    set game-over? true ]
end

to show-next-piece
  ask patches with [(pxcor > 4) and (pxcor < 9) and (pycor > 5) and (pycor < 11)]
  [ set pcolor black ]
  ask turtle 0 [ setxy 6 8 ]
  ask pieces
  [ setup-piece next-shape
    set pcolor color ]
end

to setup-piece [s]  ;; Piece Procedure
  if (s = 0) [ setupO  set color blue   ]
  if (s = 1) [ setupL  set color red    ]
  if (s = 2) [ setupJ  set color yellow ]
  if (s = 3) [ setupT  set color green  ]
  if (s = 4) [ setupS  set color orange ]
  if (s = 5) [ setupZ  set color sky    ]
  if (s = 6) [ setupI  set color violet ]
  setxy ([xcor] of turtle 0) + x
        ([ycor] of turtle 0) + y
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Pieces Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; The numbers 0123 show the relative positions of turtles
;; 0, 1, 2, and 3 within the overall shape.

;; O-Block
;; 01
;; 23
to setupO ;;Piece Procedure
  if (who = 1) [ set x 1 set y  0 ]
  if (who = 2) [ set x 0 set y -1 ]
  if (who = 3) [ set x 1 set y -1 ]
end

;; L-Block
;; 201
;; 3
to setupL ;;Piece Procedure
  if (who = 1) [ set x  1 set y  0 ]
  if (who = 2) [ set x -1 set y  0 ]
  if (who = 3) [ set x -1 set y -1 ]
end

;; J-Block
;; 102
;;   3
to setupJ ;;Piece Procedure
  if (who = 1) [ set x -1 set y  0 ]
  if (who = 2) [ set x  1 set y  0 ]
  if (who = 3) [ set x  1 set y -1 ]
end

;; T-Block
;; 201
;;  3
to setupT ;;Piece Procedure
  if (who = 1) [ set x  1 set y  0 ]
  if (who = 2) [ set x -1 set y  0 ]
  if (who = 3) [ set x  0 set y -1 ]
end

;; S-Block
;;  01
;; 23
to setupS ;;Piece Procedure
  if (who = 1) [ set x  1 set y  0 ]
  if (who = 2) [ set x -1 set y -1 ]
  if (who = 3) [ set x  0 set y -1 ]
end

;; Z-block
;; 10
;;  32
to setupZ ;;Piece Procedure
  if (who = 1) [ set x -1 set y  0 ]
  if (who = 2) [ set x  1 set y -1 ]
  if (who = 3) [ set x  0 set y -1 ]
end

;; I-Block
;; 1023
to setupI ;;Piece Procedure
  if (who = 1) [ set x -1 set y 0 ]
  if (who = 2) [ set x  1 set y 0 ]
  if (who = 3) [ set x  2 set y 0 ]
end
@#$#@#$#@
GRAPHICS-WINDOW
183
10
478
416
9
12
15.0
1
10
1
1
1
0
0
0
1
-9
9
-12
12
1
1
0
ticks

BUTTON
31
145
93
178
New
new
NIL
1
T
OBSERVER
NIL
N
NIL
NIL
1

MONITOR
31
42
155
87
Score
score
0
1
11

BUTTON
118
336
173
369
Right
shift-right
NIL
1
T
OBSERVER
NIL
L
NIL
NIL
1

BUTTON
8
336
63
369
Left
shift-left
NIL
1
T
OBSERVER
NIL
J
NIL
NIL
1

BUTTON
24
303
91
336
RotLeft
rotate-left
NIL
1
T
OBSERVER
NIL
S
NIL
NIL
1

BUTTON
91
303
158
336
RotRight
rotate-right
NIL
1
T
OBSERVER
NIL
D
NIL
NIL
1

BUTTON
63
369
118
402
Down
shift-down
NIL
1
T
OBSERVER
NIL
,
NIL
NIL
1

MONITOR
93
91
155
136
Level
level
0
1
11

MONITOR
31
91
93
136
Lines
lines
0
1
11

BUTTON
93
145
155
178
Play
play
T
1
T
OBSERVER
NIL
P
NIL
NIL
1

SLIDER
6
183
176
216
starting-level
starting-level
0
9
0
1
1
NIL
HORIZONTAL

SLIDER
6
216
176
249
debris-level
debris-level
0
10
0
1
1
NIL
HORIZONTAL

SWITCH
488
62
619
95
show-next?
show-next?
0
1
-1000

TEXTBOX
489
42
579
60
Game Options
11
0.0
0

BUTTON
63
336
118
369
Drop
drop-down
NIL
1
T
OBSERVER
NIL
K
NIL
NIL
1

@#$#@#$#@
## WHAT IS IT?

This is the classic puzzle game, Tetris.  The game involves falling pieces composed of four blocks in different configurations.  The object of the game is to complete horizontal rows of blocks in the well.

Any time a row is completed it disappears and the blocks above it fall down.  The more rows you clear with the placement of a single piece, the more points you receive.  If you clear enough rows, you move on to the next level.  The higher the level, the more points you receive for everything, but the pieces fall faster as well, increasing the challenge.

## HOW TO USE IT

Monitors:
-- SCORE shows your current score.
-- LINES shows the number of lines you have cleared.
-- LEVEL shows your current level.

Sliders:
-- STARTING-LEVEL selects the beginning level for the game.  Choosing a higher level to begin allows you to get more points faster and increase the initial falling speed.  Your level will not increase until your number of lines is 10*(level+1). (i.e. starting-level=3, level will stay 3 until 40 lines are cleared.)
-- DEBRIS-LEVEL sets how many lines of random blocks will be created at the bottom of the well at the beginning of the game.

Buttons:
-- NEW sets up a new game with the initial settings.
-- PLAY begins the game.

Controls:
-- ROTLEFT rotates the current piece 90 degrees to the left.
-- ROTRIGHT rotates the current piece 90 degrees the right.
-- LEFT moves the current piece one space left.
-- DROP causes the current piece to drop to the bottom of the well immediately.
-- RIGHT moves the current piece one space right.
-- DOWN moves the current piece one space down.

Options (Switches)
-- SHOW-NEXT-PIECE? toggles the option which causes the piece which will appear in the well after you place the current one to be shown in a small box to the right of the well.

## THINGS TO NOTICE

There are seven types of pieces.  These are all the shapes that can be made by four blocks stuck together.

       [][]      Square-Block - good filler in flat areas,
       [][]         hard to place in jagged areas

       [][][]    L-Block - fits well into deep holes
       []

         [][]    S-Block - good filler in jagged areas,
       [][]         hard to place in flat areas

       [][][]    T-Block - good average piece, can fit
         []         almost anywhere well

       [][]      Reverse S-Block (Or Z-Block) - good
         [][]       filler in jagged areas, hard to
                    place in flat areas

       [][][]    Reverse L-Block - fits well into
           []       deep holes

       [][][][]  I-Bar - Only piece that allows you to
                    clear 4 lines at once (aka a Tetris)

Scoring System:
Note: Points are scored using level + 1 so that points are still scored at level 0.
-- 1 Line  = 50*(level + 1) points
-- 2 Lines = 150*(level + 1) points
-- 3 Lines = 350*(level + 1) points
-- 4 Lines = 1000*(level + 1) points (aka a Tetris)
-- Clear the board = 2000*(level + 1)
-- Every piece = 10*(level + 1) points

## THINGS TO TRY

Beat your highest score.

## EXTENDING THE MODEL

Add options for changing the width and depth of the well.

Add the option of including pieces composed of more than four blocks, or fewer.

## NETLOGO FEATURES

This model makes use of turtle breeds.

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

square big
false
0
Rectangle -7500403 true true 0 -15 300 300

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
set starting-level 9
new
repeat 25 [ play wait 0.15 ]
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

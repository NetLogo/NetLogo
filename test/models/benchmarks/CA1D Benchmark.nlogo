globals
[
  row           ;; current row
  old-rule      ;; previous rule
  rules-shown?  ;; flag to check if rules have been displayed
  gone?         ;; flag to check if go has already been pressed
  result
]

patches-own
[on?]

to startup  ;; initially, nothing has been displayed
  set rules-shown? false
  set gone? false
  set old-rule rule
end

to benchmark
  random-seed 4378
  setup-random
  reset-timer
  repeat 10 * world-height [ go ]
  set result timer
end

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to setup-general  ;; setup general working environment
  cp ct
  set row max-pycor   ;; reset current row
  refresh-rules
  set gone? false
  set rules-shown? false  ;; rules are no longer shown since the screen has been cleared
end

to single-cell
  setup-general
  ask patches with [pycor = row] [set on? false set pcolor background]  ;; initialize top row
  ask patch 0 row [ set pcolor foreground
                    set on? true ]
  reset-ticks
end

to setup-random
  setup-general
  ask patches with [pycor = row]  ;; randomly place cells across the top of the screen
  [
    set on? ((random 100) < density)
    color-patch
  ]
  reset-ticks
end

to setup-continue
  let on?-list []
  if not gone?  ;; make sure go has already been called
    [ stop ]
  set on?-list map [[on?] of ?] sort patches with [pycor = row]  ;; copy cell states from the
                                                                 ;; current row to a list
  setup-general
  ask patches with [ pycor = row ]
  [
    set on? item (pxcor + max-pxcor) on?-list  ;; copy states from list to top row
    color-patch
  ]
  set gone? true
end


;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; GO Procedures      ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

to go
  if (rules-shown?)  ;; don't do unless we are properly set up
    [ stop ]
  if (row = min-pycor)  ;; if we reach the end, continue from the top or stop
  [
    ifelse auto-continue?
      [ display
        setup-continue ]
      [ stop ]
  ]
  ask patches with [ pycor = row ]  ;; apply rule
    [ do-rule ]
  set row (row - 1)
  ask patches with [ pycor = row ]  ;; color in changed cells
    [ color-patch ]
  set gone? true
  tick
end


to do-rule  ;; patch procedure
  let left-on? [on?] of patch-at -1 0  ;; set to true if the patch to the left is on
  let right-on? [on?] of patch-at 1 0  ;; set to true if the patch to the right is on

  ;; each of these lines checks the local area and (possibly)
  ;; sets the lower cell according to the corresponding switch
  let new-value
    (iii and left-on?       and on?       and right-on?)          or
    (iio and left-on?       and on?       and (not right-on?))    or
    (ioi and left-on?       and (not on?) and right-on?)          or
    (ioo and left-on?       and (not on?) and (not right-on?))    or
    (oii and (not left-on?) and on?       and right-on?)          or
    (oio and (not left-on?) and on?       and (not right-on?))    or
    (ooi and (not left-on?) and (not on?) and right-on?)          or
    (ooo and (not left-on?) and (not on?) and (not right-on?))
  ask patch-at 0 -1 [ set on? new-value ]
end


;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Utility Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

to color-patch  ;;patch procedure
  ifelse on?
    [ set pcolor foreground ]
    [ set pcolor background ]
end


to-report bindigit [number power-of-two]
  ifelse (power-of-two = 0)
    [ report floor number mod 2 ]
    [ report bindigit (floor number / 2) (power-of-two - 1) ]
end

to refresh-rules  ;; update either switches or slider depending on which has been changed last
  ifelse (rule = old-rule)
  [
    if (rule != calculate-rule)
      [ set rule calculate-rule ]
  ]
  [ extrapolate-switches ]
  set old-rule rule
end

to extrapolate-switches
  ;; set the switches based on the slider
  set ooo ((bindigit rule 0) = 1)
  set ooi ((bindigit rule 1) = 1)
  set oio ((bindigit rule 2) = 1)
  set oii ((bindigit rule 3) = 1)
  set ioo ((bindigit rule 4) = 1)
  set ioi ((bindigit rule 5) = 1)
  set iio ((bindigit rule 6) = 1)
  set iii ((bindigit rule 7) = 1)
end

to-report calculate-rule
  ;; set the slider based on the switches
  let rresult 0
  if ooo [ set rresult rresult +   1 ]
  if ooi [ set rresult rresult +   2 ]
  if oio [ set rresult rresult +   4 ]
  if oii [ set rresult rresult +   8 ]
  if ioo [ set rresult rresult +  16 ]
  if ioi [ set rresult rresult +  32 ]
  if iio [ set rresult rresult +  64 ]
  if iii [ set rresult rresult + 128 ]
  report rresult
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SHOW-RULES RELATED PROCEDURES ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to show-rules  ;; preview cell state transitions
  setup-general
  let rules list-rules

  ask patches with [pycor > max-pycor - 5]
    [ set pcolor gray ]

  ;; create 8 turtles evenly spaced across the screen
  ask patches with [ pycor = max-pycor and
                    ((pxcor + 1) mod (floor (world-width / 8))) = 0 ]
  [
    sprout 1
    [
      set heading 270
      fd 18  ;;16px offset + 2px
      print-block (item 0 (item who rules))  ;; right cell
      fd 2
      print-block (item 1 (item who rules))  ;; center cell
      fd 2
      print-block (item 2 (item who rules))  ;; left cell
      bk 2
      set heading 180
      fd 2
      set heading 90
      print-block (item 3 (item who rules))  ;; next cell state
      die
    ]
  ]
  set rules-shown? true
end

;; turtle procedure
to print-block [ state ]  ;; draw a 2x2 block of with a color determined by the state
  ifelse state
    [ set color foreground ]
    [ set color background ]
  set heading 90
  repeat 4
  [
    set pcolor color
    rt 90
    fd 1
  ]
end

to-report list-rules  ;; return a list of state-transition 4-tuples corresponding to the switches
  let rules []
  set rules (lput (lput ooo [false false false]) rules)
  set rules (lput (lput ooi [false false true ]) rules)
  set rules (lput (lput oio [false true  false]) rules)
  set rules (lput (lput oii [false true  true ]) rules)
  set rules (lput (lput ioo [true  false false]) rules)
  set rules (lput (lput ioi [true  false true ]) rules)
  set rules (lput (lput iio [true  true  false]) rules)
  set rules (lput (lput iii [true  true  true ]) rules)
  report rules
end
@#$#@#$#@
GRAPHICS-WINDOW
244
11
1055
643
400
300
1.0
1
10
1
1
1
0
1
1
1
-400
400
-300
300
1
1
1
ticks

BUTTON
6
10
114
43
Setup Single
single-cell
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
120
10
225
43
Setup Random
setup-random
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
5
187
103
220
OOO
OOO
0
1
-1000

SWITCH
103
187
203
220
OOI
OOI
1
1
-1000

SWITCH
5
220
103
253
OIO
OIO
1
1
-1000

SWITCH
103
220
203
253
OII
OII
0
1
-1000

SWITCH
5
253
103
286
IOO
IOO
1
1
-1000

SWITCH
103
253
203
286
IOI
IOI
0
1
-1000

SWITCH
5
286
103
319
IIO
IIO
0
1
-1000

SWITCH
103
286
203
319
III
III
1
1
-1000

TEXTBOX
6
133
96
151
Rule Switches:
11
0.0
0

SLIDER
7
390
122
423
foreground
foreground
0
139
55
1
1
NIL
HORIZONTAL

SLIDER
7
423
122
456
background
background
0
139
0
1
1
NIL
HORIZONTAL

TEXTBOX
10
371
100
389
Colors:
11
0.0
0

SLIDER
5
154
203
187
rule
rule
0
255
105
1
1
NIL
HORIZONTAL

SLIDER
119
46
225
79
density
density
0
100
10
1
1
%
HORIZONTAL

BUTTON
6
46
114
79
Setup Continue
setup-continue
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
160
83
225
116
Go
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

BUTTON
5
319
104
352
Show Rules
show-rules
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
7
83
147
116
auto-continue?
auto-continue?
0
1
-1000

BUTTON
123
321
242
516
NIL
benchmark
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
134
465
235
510
NIL
result
17
1
11

@#$#@#$#@
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
benchmark set result 0
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

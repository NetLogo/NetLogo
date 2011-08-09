globals
[
  row           ;; current row
  old-rule      ;; previous rule
  rules-shown?  ;; flag to check if rules have been displayed
  gone?         ;; flag to check if go has already been pressed
]

patches-own
[on?]

to startup  ;; initially, nothing has been displayed
  set rules-shown? false
  set gone? false
  set old-rule rule
end

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to setup-general  ;; setup general working environment
  cp ct
  set row max-pycor   ;; reset current row
  refresh-rules
  set gone? false
  set rules-shown? false  ;; rules are no longer shown since the view has been cleared
end

to single-cell
  setup-general
  reset-ticks
  ask patches with [pycor = row] [set on? false set pcolor background]  ;; initialize top row
  ask patch 0 row [ set pcolor foreground ]  ;; setup single cell in top center
  ask patch 0 row [ set on? true ]
end

to setup-random
  setup-general
  reset-ticks
  ask patches with [pycor = row]  ;; randomly place cells across the top of the world
  [
    set on? ((random-float 100) < density)
    color-patch
  ]
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
    [
        set gone? true
        display    ;; ensure everything gets drawn before we clear it
        setup-continue
    ]
    [
      ifelse gone?
        [ setup-continue ]       ;; a run has already been completed, so continue with another
        [ set gone? true stop ]  ;; otherwise just stop
    ]
  ]
  ask patches with [ pycor = row ]  ;; apply rule
    [ do-rule ]
  set row (row - 1)
  ask patches with [ pycor = row ]  ;; color in changed cells
    [ color-patch ]
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
  let result 0
  if ooo [ set result result +   1 ]
  if ooi [ set result result +   2 ]
  if oio [ set result result +   4 ]
  if oii [ set result result +   8 ]
  if ioo [ set result result +  16 ]
  if ioi [ set result result +  32 ]
  if iio [ set result result +  64 ]
  if iii [ set result result + 128 ]
  report result
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SHOW-RULES RELATED PROCEDURES ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to show-rules  ;; preview cell state transitions
  setup-general
  ask patches with [pycor > max-pycor - 12]
    [ set pcolor gray - 1 ]
  let i 0
  foreach list-rules
  [
    let px (min-pxcor + i * floor (world-width / 8) + floor (world-width / 16)) - 4
    ask patch px (max-pycor - 4)
    [
      sprout 1
      [
        set xcor xcor - 3
        print-block item 0 ?  ;; left cell
        set xcor xcor + 3
        print-block item 1 ?  ;; center cell
        set xcor xcor + 3
        print-block item 2 ?  ;; right cell
        setxy (xcor - 3) (ycor - 3)
        print-block item 3 ?  ;; next cell state
        die
      ]
    ]
    set i (i + 1)
  ]
  set rules-shown? true
end

;; turtle procedure
to print-block [print-on?]  ;; draw a 3x3 block with the color determined by the state
  ask patches in-radius 1.5  ;; like "neighbors", but includes the patch we're on too
  [
    set on? print-on?
    color-patch
  ]
end

to-report list-rules  ;; return a list of state-transition 4-tuples corresponding to the switches
  report (list lput ooo [false false false]
               lput ooi [false false true ]
               lput oio [false true  false]
               lput oii [false true  true ]
               lput ioo [true  false false]
               lput ioi [true  false true ]
               lput iio [true  true  false]
               lput iii [true  true  true ])
end
@#$#@#$#@
GRAPHICS-WINDOW
229
12
753
301
128
64
2.0
1
10
1
1
1
0
1
0
1
-128
128
-64
64
1
1
1
ticks

BUTTON
7
10
115
43
setup single
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
7
45
125
78
setup random
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
110
300
208
333
OOO
OOO
0
1
-1000

SWITCH
10
300
110
333
OOI
OOI
1
1
-1000

SWITCH
110
267
208
300
OIO
OIO
1
1
-1000

SWITCH
10
267
110
300
OII
OII
0
1
-1000

SWITCH
110
234
208
267
IOO
IOO
1
1
-1000

SWITCH
10
234
110
267
IOI
IOI
0
1
-1000

SWITCH
110
201
208
234
IIO
IIO
0
1
-1000

SWITCH
10
201
110
234
III
III
1
1
-1000

TEXTBOX
11
143
101
161
Rule Switches:
11
0.0
0

TEXTBOX
13
387
103
405
Colors:
11
0.0
0

SLIDER
10
164
208
197
rule
rule
0.0
255.0
105
1.0
1
NIL
HORIZONTAL

SLIDER
115
45
221
78
density
density
0.0
100.0
10
1.0
1
%
HORIZONTAL

BUTTON
8
92
79
125
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

BUTTON
12
342
111
375
show rules
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
82
92
222
125
auto-continue?
auto-continue?
1
1
-1000

INPUTBOX
14
406
134
466
foreground
55
1
0
Color

INPUTBOX
139
406
255
466
background
0
1
0
Color

@#$#@#$#@
## WHAT IS IT?

This program models one-dimensional cellular automata.  A cellular automaton (CA) is a computational machine that performs actions based on certain rules.  The automaton is divided into cells, like the square cells of a checkerboard.  Each cell can be either on or off (its "state").  The board is initialized with some cells on and some off.  At each time step (or "tick") some rules "fire" and this results in some cells turning "on" and some turning "off".

There are many kinds of cellular automata. In this model, we explore one-dimensional CA -- the simplest types of CA. In this case of one-dimensional cellular automata, each cell checks the state of itself and its neighbors to the left and right, and then sets the cell below itself to either "on" or "off", depending upon the rule.  This is done in parallel and continues until the bottom of the board.

This model explores all 256 possible CA rules that can be constructed by each cell checking only its immediate left and immediate right neighbor. Cellular automata can be created on any board size and dimension.

This model is one of a collection of 1D CA models. It is meant for the more sophisticated user. If you are seeing CA for the first time, we suggest you check out one of the simpler CA models such as CA 1D Rule 30.

In his book, "A New Kind of Science", Stephen Wolfram argues that simple computational devices such as CA lie at the heart of nature's patterns and that CAs are a better tool than mathematical equations for the purpose of scientifically describing the world.

## HOW TO USE IT

Initialization & Running:
- SETUP-SINGLE initializes the model with a single cell on in the center.
- SETUP-RANDOM initializes the model with a percentage of the cells "on." The percentage on is determined by the DENSITY slider.
- AUTO-CONTINUE? CA automatically wraps top the top once it reaches the last row when the toggle is on
- GO begins running the model with the currently set rule. It runs until the end of the screen.  If it is pressed again, the CA continues the current run from the top, stopping again at the end of the screen.
- FOREGROUND & BACKGROUND set the "on" and "off" cell colors respectively.
- SHOW-RULES clears the view and gives a graphical preview of the rules.  The 8 cases are displayed across the top of the world. To run the model, you must press either SETUP-SINGLE or SETUP-RANDOM and then click GO.

Rule Setup:
There are 8 switches, the names of which correspond to cell states.  "O" means off, "I" means on. For example, the top switch is called "OOO".  (NOTE: the switch names are composed of the letters "I" and "O", not the numbers zero or one).  If this switch is set to "on", then the following rule is created: when a cell is off, its left neighbor cell is off and its right neighbor cell is off, then the cell below it will be set "on" at the next time step.  If this switch is set to 0, then the cell below it will be set to "off" at the next time step.  So, since each switch has two settings and there are eight switches, there are 256 (2^8) possible rules.  The current rule number is shown by the "RULE" slider, and it is calculated by changing a switch's name from binary to decimal and taking the sum of all the switches that are set to one.  For example, if "011" and "001" are set to "on" and all the rest are set to "off", then the rule number is 4 (011 = 3, 001 = 1, and 3 + 1 = 4)).

The RULE slider can also be moved to set the current rule.  Doing so will change the current state of the switches in the same way the switches set the rule.

## THINGS TO NOTICE

What different patterns are formed by using a random setup versus a single setup?

Why do some rules always end up the same, regardless of the initial setup?

Are there rules that repeat a pattern indefinitely?

Are there rules that produce seemingly chaotic, random results?

Can all rules be classified into these above types? Are there more rule types than these?

Note that the pictures generated by this model do not exactly match the pictures in Wolfram's book, "A New Kind of Science". That's because Wolfram's book computes the CA as an infinite grid while the NetLogo model "wraps" around the horizontal boundaries. To get pictures closer to the ones in the book, you may need to increase the size of the world. You can increase the size of the world up to the available memory on your computer. However, the larger the world, the longer time it will take NetLogo to compute and display the results.

## THINGS TO TRY

Find some rules that converge to all cells "on" or to all cells "off".

Are there any rules that conditionally converge to all cells "on" or all cells "off", depending upon the initial percentage of on/off cells?

A classic automaton is used to compute various things.  Can these cellular automata be used to compute anything?  How?

Experiment with the density variable and various types of rules. What are some relationships?

## EXTENDING THE MODEL

What if a cell's neighborhood was five cells -- two to the left, itself, and two to the right?

Classical CA use an "infinite board". The CAs shown here "wrap" around the horizontal edges of the world (sometimes known as a periodic CA or CA with periodic boundary condition). How would you implement a CA in NetLogo that comes closer to the infinite board?

Try making a two-dimensional cellular automaton.  The neighborhood could be the eight cells around it, or just the cardinal cells (the cells to the right, left, above, and below).

Can you develop some tools to analyze the behavior of CAs?  Compare different iterations from one continuation to the next of the same CA?  Compare different rules?

## NETLOGO FEATURES

This model takes advantage of a special optimization in the NetLogo compiler that makes the expression "ask patches with [px/ycor = <local variable>]" run much faster than it would otherwise.

## RELATED MODELS

Life - an example of a two-dimensional cellular automaton
CA 1D Rule 30 - the basic rule 30 model
CA 1D Rule 30 Turtle - the basic rule 30 model implemented using turtles
CA 1D Rule 90 - the basic rule 90 model
CA 1D Rule 110 - the basic rule 110 model
CA 1D Rule 250 - the basic rule 250 model
CA 1D Totalistic - a model that shows all 2,187 possible 1D 3-color totalistic cellular automata.
CA Stochastic- the probabalistic counterpart to this model

## CREDITS AND REFERENCES

Thanks to Eytan Bakshy, Geoff Hulette, and Erich Neuwirth for their help with this model.

The first cellular automaton was conceived by John Von Neumann in the late 1940's for his analysis of machine reproduction under the suggestion of Stanislaw M. Ulam. It was later completed and documented by Arthur W. Burks in the 1960's. Other two-dimensional cellular automata, and particularly the game of "Life," were explored by John Conway in the 1970's. Many others have since researched CA's. In the late 1970's and 1980's Chris Langton, Tom Toffoli and Stephen Wolfram did some notable research. Wolfram classified all 256 one-dimensional two-state single-neighbor cellular automata. In his recent book, "A New Kind of Science," Wolfram presents many examples of cellular automata and argues for their fundamental importance in doing science.

See also:

Von Neumann, J. and Burks, A. W., Eds, 1966. Theory of Self-Reproducing Automata. University of Illinois Press, Champaign, IL.

Toffoli, T. 1977. Computation and construction universality of reversible cellular automata. J. Comput. Syst. Sci. 15, 213-231.

Langton, C. 1984. Self-reproduction in cellular automata. Physica D 10, 134-144

Wolfram, S. 1986. Theory and Applications of Cellular Automata: Including Selected Papers 1983-1986. World Scientific Publishing Co., Inc., River Edge, NJ.

Bar-Yam, Y. 1997. Dynamics of Complex Systems. Perseus Press. Reading, Ma.

Wolfram, S. 2002. A New Kind of Science. Wolfram Media Inc.  Champaign, IL.
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
setup-random repeat world-height - 1 [ go ]
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

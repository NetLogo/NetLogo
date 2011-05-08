turtles-own
[
  stem?     ;; true for stem cells, false for transitory cells
  age  ;; age of cell. changes color with age
  metastatic?  ;; false for progeny of stem cell 0, true for progeny of stem cell 1
]

globals
[
  cell-count
]

to setup
  clear-all
  set-default-shape turtles "circle 3"
  ask patches
    [ set pcolor gray ]
  set-stem
  evaluate-params
  reset-ticks
end

to set-stem   ;;create two stem cells
  crt 2
  [
    set size 2   ; easier to see
    setxy (min-pxcor / 2) 0
    set stem? true
    set metastatic? false
    set color blue
    set age 0
  ]
  ask turtle 1
  [
    set metastatic? true
    set heading 90            ;; stem cell 1 will move away
  ]
  set cell-count 2
end

to go
  ask turtles
  [
    ifelse leave-trail?
      [ pd ]
      [ pu ]
    if (who = 1) and (xcor < 25)
    [ fd 1 ]  ;stem cell movement
    set age age + 1
    move-transitional-cells
    mitosis
    death
  ]
  tick
  evaluate-params
end

;;transitional cells move and hatch more. Turtle proc.
to move-transitional-cells
  if (not stem?)
  [
    set color ( red + 0.25 * age )
    fd 1
    if (age < 6)
    [
      hatch 1
      [  ;amplification
        rt random-float 360
        fd 1
      ]
    ]
  ]
end

to mitosis ;; turtle proc. - stem cells only
  if stem?
  [
    hatch 1
    [
      fd 1
      set color red
      set stem? false
      ifelse (who = 1)
        [ set age 16 ]
        [ set age 0 ]
    ]
  ]
end

to death   ;; turtle proc.
  if (not stem?) and (not metastatic?) and (age > 20)
    [ die ]
  if (not stem?) and metastatic? and (age > 4)
    [ die ]
end

to evaluate-params
  set cell-count count turtles  ;cell count
  if (cell-count <= 0)
    [ stop ]
end

to kill-original-stem-cell
  ask turtle 0
    [ die ]
end

to kill-moving-stem-cell
  ask turtle 1
    [ die ]
end

to kill-transitory-cells
  ask turtles with [ age < 10 and not stem? ]
    [ die ]
end
@#$#@#$#@
GRAPHICS-WINDOW
309
10
731
453
51
51
4.0
1
10
1
1
1
0
0
0
1
-51
51
-51
51
1
1
1
ticks

BUTTON
12
21
67
54
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

BUTTON
71
21
126
54
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

MONITOR
201
141
272
186
cell-count
cell-count
0
1
11

BUTTON
42
111
190
144
kill transitory cells
kill-transitory-cells
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
57
65
176
98
leave-trail?
leave-trail?
0
1
-1000

BUTTON
42
179
190
212
kill original stem cell
kill-original-stem-cell
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

PLOT
9
225
297
406
Living Cells
Time
Cells Alive
0.0
200.0
0.0
2000.0
true
false
"" ""
PENS
"Cells Alive" 1.0 0 -16777216 true "" "plot cell-count"

BUTTON
42
145
190
178
kill moving stem cell
kill-moving-stem-cell
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

This model illustrates the growth of a tumor and how it resists chemical treatment.  A tumor consists of two kinds of cells: stem cells (blue turtles) and transitory cells (all other turtles).

## HOW IT WORKS

During mitosis, a stem cell can divide either asymmetrically or symmetrically. In asymmetric mitosis, one of the two daughter cells remains a stem cell, replacing its parent. So a stem cell effectively never dies - it is quasi reincarnated after each division. The other daughter cell turns into a transitory cell that moves outward.

Young transitory cells may divide, breeding other transitory cells.  The transitory cells stop dividing at a certain age and change color from red to white to black, eventually dying.

A stem cell may also divide symmetrically into two stem cells (blue turtles). In this example the original stem cell divides symmetrically only once. The first stem cell remains static, but the second stem cell moves to the right. This activity, in which the cell advances into distant sites and creates another tumor colony, is called metastasis. Notice that the metastasis is red. It is made of cells that die young, when they are still red, rather than ending as black dots as in the static tumor. As the disease progresses, cells die younger and younger.

## HOW TO USE IT

SETUP: Clears the world and creates two blue neoplastic (cancerous) stem cells.  One cell stays put and the other moves to the right.
GO: Runs the simulation.
KILL TRANSITORY CELLS: Kills transitory cells that are younger than 10 time steps.
KILL STEM CELL:  Kills a stem cell. If the adjacent KILL-MOVING-CELL switch is set to OFF, the original is eliminated. If the switch is set to ON, the moving stem cell is eliminated.
KILL-MOVING-CELL: Determines which stem cell is killed when the KILL STEM CELL button is pressed.
LEAVE-TRAIL: If it's ON, the cells trace their paths; if it's OFF, they do not.
CELL-COUNT: Displays the total number of living cells.
LIVING CELLS PLOT: plots the number of living cells.

## THINGS TO NOTICE

First, notice the blue dot. It represents a normal stem cell that has been transformed into a tumor stem cell.  Set the "leave-trail" switch to On, and click 'go'.  A tumor is formed as the stem cell creates transitory cells, which reproduce themselves.  It grows to a certain size.  As it grows, a bulge appears on the right side.  This is a tumor outgrowth, caused by symmetric mitosis of the stem cell. The outgrowth will turn into a metastasis and grow into remote regions.

After a while the tumor and metastasis appear to reach their ultimate size and nothing interesting seems to be happening. This illustrates how the tumor presents itself to the physician - as a solid cell mass. In reality, this seemingly solid mass conceals active cell turnover. To reveal it, set the leave-trail switch to Off, click on SETUP, and then click on GO to run the simulation again.

Slow down the model so you can follow individual steps.  Move the speed slider to the left, click on SETUP and GO, and observe the blue stem cell.  It divides into two blue stem cells.  One remains static, and the other moves to the right.

Look at the static stem cell (blue).  It breeds red cells which move outward and change their color as they age. When young, they are red and create more transitory cells. Then they turn white, then black, and then they die.

## THINGS TO TRY

Try a treatment: click on the 'kill transitory cells' button while the model is running. This simulates treatment with a chemical agent.  The agent eliminates young (red) cells that divide, and it spares older cells.  Note that the tumor shrinks and grows again.  Continue with this "chemotherapy" by clicking on the button again and watch the plot.  Repeat the treatment several times until you have understood why it fails.

Most of the chemotherapy drugs known as M- and S-poisons inhibit cell division, but generally do not cause cell death. Thus chemotherapy can be represented as killing the young (red) transitory cells, since only young cells divide. Older, non-dividing transitory cells are not affected; they continue aging and finally pass away.

The problem is that the stem cells maintain the tumor and propagate its metastases. Also known as clonogenic cells, stem cells are generally resistant to chemotherapy. They can be eliminated only with high doses of chemicals, which endanger healthy stem cells. The therapeutic margin of chemical drugs is extremely narrow. That is, they do about the same amount of harm as good.

Let the tumor grow again.  Click on 'kill moving stem cell' and continue running the model.  The right blue stem cell disappears. Its progeny live a bit longer and then die. Now click on "kill original stem cell", and watch the gradual disappearance of the tumor, as no new cells are created and existing cells continue aging until they die and disappear.

Any questions? Before asking, continue exploring the model until you grasp its behavior.  The model reveals a hidden dimension that is difficult to understand. It is the time dimension of tissues (and the tumor) in the body.

## EXTENDING THE MODEL

What alternative treatments would you suggest?  How would you model them here?

## CREDITS AND REFERENCES

The original StarLogoT Tumor model was contributed by Gershom Zajicek M.D., Professor of Experimental Medicine and Cancer Research at The Hebrew University-Hadassah Medical School, Jerusalem.
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

circle 3
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 105 105 90

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

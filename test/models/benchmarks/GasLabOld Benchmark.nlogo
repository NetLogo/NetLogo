globals [
  fast average slow               ;; current counts
  avg-speed avg-energy            ;; current averages
  avg-speed-init avg-energy-init  ;; initial averages
  vsplit vclock             ;; clock variables
  box-edge                        ;; patch coords of box's edge
  result
]

turtles-own [
  speed mass energy new-speed           ;; turtle info
  v1t v1l tmp-turtle                    ;; collision info (turtle 1)
  heading2 mass2 speed2 v2t v2l turtle2 ;; collision info (turtle 2)
  theta                                 ;; collision info (both turtles)
]

patches-own [ box? ]

to benchmark
  random-seed 361
  reset-timer
  setup
  repeat 20000 [ go ]
  set result timer
end

to setup
  ca reset-ticks
  set box-edge (round (max-pxcor * box-size-percent / 100))
  make-box
  set vclock  0
  cro number
  ask turtles [
    set new-speed initspeed
    set mass initmass
    if who != 0
      [ random-position ]
    rt random 360
    recolor-turtle turtle who
  ]
  update-variables
  set avg-speed-init avg-speed
  set avg-energy-init avg-energy
  setup-plotz
  setup-histograms
  do-plotting
  do-histograms
end

to update-variables
  ask turtles
    [ set speed new-speed
      set energy (0.5 * speed * speed * mass) ]
  set average  count turtles with [color = green ]
  set slow     count turtles with [color = blue]
  set fast     count turtles with [color = red]
  set avg-speed mean [speed ] of turtles
  set avg-energy mean [energy] of turtles
  set vsplit (round ((max [speed] of turtles) * 1.2))
end

to go
  ask turtles [ bounce ]
  ask turtles [ move ]
  set vclock (vclock + 1)
  if (vclock = vsplit)
  [ tick
    set vclock  0
    update-variables
    do-plotting
    do-histograms
    ask patches [ fade ]
  ]
end

to bounce ;;turtle procedure
  ; check: hitting top or bottom wall?
  if ((pycor = box-edge)       and ((heading > 270) or (heading < 90))) or
     ((pycor = (0 - box-edge)) and ((heading > 90) and (heading < 270)))
    ; if so, reflect heading around y axis
    [ set heading (180 - heading) ]
  ; check: hitting left or right wall?
  if ((pxcor = box-edge)       and ((heading > 0) and (heading < 180))) or
     ((pxcor = (0 - box-edge)) and ((heading > 180)))
    ; if so, reflect heading around x axis
    [ set heading (0 - heading) ]
end

to move  ;;turtle procedure
  jump ( speed / vsplit )
  check-for-collision
  if trace? and ( who = 0 ) and not box?
    [ set pcolor yellow]
end

to check-for-collision ;;turtle procedure
  if not box? and (count other turtles-here = 1)
    [ set tmp-turtle one-of other turtles-here
      if ((who > [who] of tmp-turtle) and (turtle2 != tmp-turtle))
        [ collide
          recolor-turtle turtle who ]
    ]
end

to collide  ;;turtle procedure
  get-turtle2-info
  calculate-velocity-components
  set-new-speed-and-headings
end

to get-turtle2-info  ;;turtle procedure
  set turtle2 tmp-turtle
  set mass2 [mass] of turtle2
  set speed2 [new-speed] of turtle2
  set heading2 [heading] of turtle2
end

to calculate-velocity-components
  set theta ( random 360 )
  set v1l ( new-speed * ( sin ( theta - heading ) ) )
  set v1t ( new-speed * ( cos ( theta - heading ) ) )
  set v2l ( speed2 * ( sin ( theta - heading2 ) ) )
  set v2t ( speed2 * ( cos ( theta - heading2 ) ) )
  let vcm ( ( ( mass * v1t ) + ( mass2 * v2t ) ) / ( mass + mass2 ) )
  set v1t ( vcm + vcm - v1t )
  set v2t ( vcm + vcm - v2t )
end

to set-new-speed-and-headings  ;;turtle procedure
  set new-speed  sqrt (( v1t * v1t ) + ( v1l * v1l ))
  ifelse ( ( v1l >= 0 ) and ( v1t >= 0 ) )
  [set heading ( theta - ( atan v1l v1t ) )]
  [ifelse ( ( v1l < 0 ) and ( v1t < 0 ) )
    [set heading ( ( theta + 180 ) - ( atan v1l v1t ) )]
    [ifelse ( ( v1l >= 0 ) and ( v1t < 0 ) )
      [set heading ( ( theta + 180 ) - ( atan v1l v1t ) ) ]
      [if ( ( v1l < 0 ) and ( v1t >= 0 ) )
        [set heading ( theta - ( atan v1l v1t ) )] ] ] ]
  let new-new-speed sqrt (( v2t * v2t) + ( v2l *  v2l))
  ask turtle2 [ set new-speed new-new-speed ]
  ifelse ( ( v2l >= 0 ) and ( v2t >= 0 ) )
  [set heading2 ( theta - ( atan v2l v2t ) )]
  [ifelse ( ( v2l < 0 ) and ( v2t < 0 ) )
    [set heading2 ( ( theta + 180 ) - ( atan v2l v2t ) )]
    [ifelse ( ( v2l >= 0 ) and ( v2t < 0 ) )
      [set heading2 ( ( theta + 180 ) - ( atan v2l v2t ) )]
      [if ( ( v2l < 0 ) and ( v2t >= 0 ) )
        [set heading2 ( theta - ( atan v2l v2t ) ) ] ] ] ]
  let new-heading heading2
  ask turtle2 [ set heading new-heading ]
  recolor-turtle turtle2
end

to recolor-turtle [the-turtle]
  ifelse [new-speed] of the-turtle < (0.5 * initspeed)
    [ ask the-turtle [ set color blue ] ]
    [ ifelse [new-speed] of the-turtle > (1.5 * initspeed)
        [ ask the-turtle [ set color red ] ]
        [ ask the-turtle [ set color green ] ] ]
end

to fade
  if (not box?) and (pcolor != black )
    [ set pcolor ( pcolor - 0.4 )
      if (round pcolor = 40)
        [ set pcolor black ] ]
end

to make-box
  ask patches
    [ set box? false
      if ((abs pxcor = box-edge) and (abs pycor <= box-edge)) or
         ((abs pycor = box-edge) and (abs pxcor <= box-edge))
        [ set pcolor red
          set box? true ] ]
end

to random-position ;; turtle procedure
  setxy ((1 - box-edge) + random ((2 * box-edge) - 2))
        ((1 - box-edge) + random ((2 * box-edge) - 2))
end

to setup-plotz
  set-current-plot "Speed Counts"
  set-plot-y-range 0 number
end

to do-plotting
  set-current-plot "Speed Counts"
  set-current-plot-pen "fast"
  plot fast
  set-current-plot-pen "average"
  plot average
  set-current-plot-pen "slow"
  plot slow
end

to setup-histograms
  set-current-plot "Speed Histogram"
  set-plot-x-range 0 (initspeed * 2)
  set-plot-y-range 0 round (number / 6)
  set-current-plot-pen "average"
  set-histogram-num-bars 32
  set-current-plot-pen "slow"
  set-histogram-num-bars 32
  set-current-plot-pen "fast"
  set-histogram-num-bars 32

  set-current-plot "Energy Histogram"
  set-plot-x-range 0 (0.5 * (initspeed * 2) * (initspeed * 2) * initmass)
  set-plot-y-range 0 round (number / 6)
  set-current-plot-pen "average"
  set-histogram-num-bars 32
  set-current-plot-pen "slow"
  set-histogram-num-bars 32
  set-current-plot-pen "fast"
  set-histogram-num-bars 32
end

to do-histograms
  set-current-plot "Speed Histogram"
  set-current-plot-pen "average"
  histogram [ speed ] of turtles with [ color = green ]
  set-current-plot-pen "slow"
  histogram [ speed ] of turtles with [ color = blue ]
  set-current-plot-pen "fast"
  histogram [ speed ] of turtles with [ color = red ]
  set-current-plot-pen "Vert-Line"
  plot-pen-reset
  draw-vert-line avg-speed gray
  draw-vert-line avg-speed-init black

  set-current-plot "Energy Histogram"
  set-current-plot-pen "average"
  histogram [ energy ] of turtles with [ color = green ]
  set-current-plot-pen "slow"
  histogram [ energy ] of turtles with [ color = blue ]
  set-current-plot-pen "fast"
  histogram [ energy ] of turtles with [ color = red ]
  set-current-plot-pen "Vert-Line"
  plot-pen-reset
  draw-vert-line avg-energy gray
  draw-vert-line avg-energy-init black
end

; draws a vertical line of color linecolor on plot plotname, at xval
to draw-vert-line [xval linecolor]
  set-plot-pen-color linecolor
  plotxy xval plot-y-min
  plot-pen-down
  plotxy xval plot-y-max
  plot-pen-up
end
@#$#@#$#@
GRAPHICS-WINDOW
284
10
578
325
35
35
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
-35
35
-35
35
1
1
1
ticks
30

MONITOR
62
229
148
274
 avg-speed
avg-speed
2
1
11

MONITOR
60
171
115
216
average
average
0
1
11

MONITOR
4
171
54
216
fast
fast
0
1
11

MONITOR
23
122
73
167
slow
slow
0
1
11

SWITCH
20
91
112
124
trace?
trace?
0
1
-1000

SLIDER
138
171
278
204
initmass
initmass
1
20
1
1
1
NIL
HORIZONTAL

SLIDER
132
91
283
124
number
number
0
500
200
1
1
NIL
HORIZONTAL

SLIDER
135
132
282
165
initspeed
initspeed
1
20
10
1
1
NIL
HORIZONTAL

BUTTON
70
49
125
82
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

BUTTON
9
49
64
82
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

PLOT
2
341
262
540
Speed Counts
time
count
0.0
20.0
0.0
200.0
true
false
"" ""
PENS
"fast" 1.0 0 -2674135 true "" ""
"average" 1.0 0 -10899396 true "" ""
"slow" 1.0 0 -13345367 true "" ""

SLIDER
132
52
282
85
box-size-percent
box-size-percent
0
100
90
1
1
NIL
HORIZONTAL

PLOT
271
341
520
540
Speed Histogram
speed
count
0.0
20.0
0.0
33.0
false
false
"" ""
PENS
"fast" 1.0 1 -2674135 true "" ""
"average" 1.0 1 -10899396 true "" ""
"slow" 1.0 1 -13345367 true "" ""
"Vert-Line" 1.0 0 -7500403 true "" ""

PLOT
526
341
775
540
Energy Histogram
energy
count
0.0
200.0
0.0
33.0
false
false
"" ""
PENS
"fast" 1.0 1 -2674135 true "" ""
"average" 1.0 1 -10899396 true "" ""
"slow" 1.0 1 -13345367 true "" ""
"Vert-Line" 1.0 0 -7500403 true "" ""

MONITOR
34
282
109
327
avg-energy
avg-energy
3
1
11

BUTTON
151
208
283
334
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
157
285
276
330
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

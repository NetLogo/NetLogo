breed [ lefts a-left ]
breed [ rights a-right ]
breed [ sums a-sum ]

turtles-own [ yvel-old yvel-new ypos-old ypos-new time ]
globals [ listening-point ]

to setup
  clear-all
  set-default-shape turtles "circle"
  ;; Create the turtles that represent the waves
  ;; We need three lines across the world, so it sets
  ;; their xcor based on their incrementing turtle id
  foreach sort patches with [pycor = 0]
    [ ask ? [ sprout-lefts 1 [ set color yellow ] ] ]
  foreach sort patches with [pycor = 0]
    [ ask ? [ sprout-rights 1 [ set color cyan ] ] ]
  foreach sort patches with [pycor = 0]
    [ ask ? [ sprout-sums 1 [ set color red ] ] ]

  ;; Initialize all variables to zero.  All of the turtles are stationary.
  set listening-point 0
  ask turtles
  [
    set yvel-old  0
    set ypos-old  0
    set time  0
  ]

  ;; The ends of the waves are special.  One side drives the wave, while
  ;; the other side anchors the waves- (prevents wrapping)
  ;; First define the driving turtles, which are colored green.
  ;; Next define the anchor turtles, which are colored blue
  ask lefts [
    if  ( xcor = max-pxcor )  [
      set color blue
    ]
    if ( xcor = min-pxcor ) [
      set color green
    ]
  ]
  ask rights [
    if  ( xcor = min-pxcor ) [
      set color blue
    ]
    if ( xcor = max-pxcor ) [
      set color green
    ]
  ]

  ;; draw the speakers, gray centered line, and listening point
  ask patches [
    if pycor = 0
      [ set pcolor gray ]
    draw-left-speaker
    draw-right-speaker
    if ( pxcor = listening-point and pycor > 0 and pycor < 4 )
      [ set pcolor white ]
  ]
  get-a-point
  reset-ticks
end


to go
  ;; Move all the wave turtles
  ask turtles [
    if  ( color = green )
      [ drive-force ]
    if  ( color = yellow or color = cyan )
      [ driven-force ]
    if  ( color = blue )
      [ update ]
    if  ( color = red )
      [ interfere ]
  ]

  get-a-point
  tick

  ;; Reset the velocities
  ask turtles [
    set yvel-old yvel-new
    set ypos-old ypos-new
  ]

  ;; Check if a hide/show switch changed
  show-or-hide
end


to drive-force ;; procedure for green turtles
  set time time + 1
  ifelse (breed = lefts)
    [ set ypos-new amplitude-left * ( sin (frequency-left * 0.1 * time )) ]
    [ set ypos-new amplitude-right * ( sin (frequency-right * 0.1 * time )) ]
  set ypos-old ypos-new
  set ycor ypos-new
end

to driven-force ;; procedure for yellow and cyan turtles
  set yvel-new yvel-old + ( [ypos-old] of turtle ( who - 1 ) )
                      - ypos-old  +  ( [ypos-old] of turtle ( who + 1 ) ) - ypos-old
  set yvel-new ( ( 1000 - friction ) / 1000 ) * yvel-new
  set ypos-new ypos-old + yvel-new
  set ycor ypos-new
end

to update ;; procedure for blue turtles
  ifelse ( breed = lefts )
    [ set ypos-new [ypos-old] of turtle ( who - 1 ) ]
    [ set ypos-new [ypos-old] of turtle ( who + 1 ) ]
  set ycor ypos-new
end

to interfere ;; procedure for red turtles
  set ypos-new ( ( [ypos-new] of turtle ( who - world-width ) )
              + ( [ypos-new] of turtle ( who - ( 2 * world-width ) ) ) )
  ifelse patch-at 0 (ypos-new - ycor) != nobody and show-sum?
    [ set ycor ypos-new
      show-turtle ]
    [ hide-turtle ]
end

to get-a-point
  ;; Changes the listening-point if the mouse is down
  if mouse-down? [
    ask patches with [ pxcor = listening-point and pycor > 0 and pycor < 4 ]
      [ set pcolor black ]
    set listening-point round mouse-xcor
    ask patches with [ pxcor = listening-point and pycor > 0 and pycor < 4 ]
      [ set pcolor white ]
  ]
end

to draw-right-speaker ;; patch procedure
  if ( pxcor = max-pxcor ) and ( pycor > ( -0.1 * max-pxcor ) ) and ( pycor < ( 0.1 * max-pxcor ) )
  [
    set pcolor orange
  ]
  if ( pxcor = round ( 0.9 * max-pxcor ) ) and ( pycor > ( -0.2 * max-pxcor ) ) and ( pycor < ( 0.2 * max-pxcor ) )
  [
    set pcolor orange
  ]
end

to draw-left-speaker ;; patch procedure
  if ( pxcor = min-pxcor ) and ( pycor > ( -0.1 * max-pxcor ) ) and ( pycor < ( 0.1 * max-pxcor ) )
  [
    set pcolor orange
  ]
  if ( pxcor = round ( - ( 0.9 * max-pxcor ) ) ) and ( pycor > ( -0.2 * max-pxcor ) ) and ( pycor < ( 0.2 * max-pxcor ) )
  [
    set pcolor orange
  ]
end

to show-or-hide
  ;; The sums are hidden in the interference procedure because they may
  ;; move outside the world
  ifelse show-left?
    [
      if any? lefts with [ hidden? ]
        [ ask lefts [ st ] ]
    ]
    [
      if any? lefts with [ not hidden? ]
        [ ask lefts [ ht ] ]
    ]

   ifelse show-right?
    [
      if any? rights with [ hidden? ]
        [ ask rights [ st ] ]
    ]
    [
      if any? rights with [ not hidden? ]
        [ ask rights [ ht ] ]
    ]
end
@#$#@#$#@
GRAPHICS-WINDOW
323
10
817
445
60
50
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
-60
60
-50
50
1
1
1
ticks
30.0

BUTTON
85
10
159
43
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
11
10
85
43
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

SLIDER
15
99
147
132
amplitude-left
amplitude-left
0.0
50.0
25
1.0
1
NIL
HORIZONTAL

SLIDER
176
11
308
44
friction
friction
0.0
100.0
0
1.0
1
NIL
HORIZONTAL

SLIDER
15
66
147
99
frequency-left
frequency-left
1.0
100.0
30
1.0
1
NIL
HORIZONTAL

MONITOR
16
144
113
189
listening-point
listening-point
3
1
11

SWITCH
212
474
316
507
plot?
plot?
0
1
-1000

PLOT
9
199
311
459
Speaker amplitude
Time
Amplitude
0.0
250.0
-61.0
50.0
false
true
"set-plot-y-range ((- amplitude-left) - amplitude-right)\n                  (  amplitude-left  + amplitude-right)\n" "if ticks mod 200 = 0 [\n  ifelse show-only-recent-plot?\n    [ set-plot-x-range max list 0 (ticks - 210) (ticks + 210)]\n    [ set-plot-x-range 0 (ticks + 200) ]\n]\n"
PENS
"left" 1.0 0 -1184463 true "" "if show-left? and any? lefts with [round xcor = listening-point]\n[ plotxy ticks [ypos-new] of one-of lefts with [ round xcor = listening-point ] ]"
"right" 1.0 0 -11221820 true "" "if show-right? and any? rights with [ round xcor = listening-point ]\n [ plotxy ticks [ypos-new] of one-of rights with [ round xcor = listening-point ] ]\n"
"sum" 1.0 0 -2674135 true "" "if show-sum? and any? sums with [ round xcor = listening-point ]\n  [ plotxy ticks [ypos-new] of one-of sums with [ round xcor = listening-point  ] ]\n"

SWITCH
11
474
209
507
show-only-recent-plot?
show-only-recent-plot?
0
1
-1000

SWITCH
10
509
122
542
show-left?
show-left?
0
1
-1000

SWITCH
245
509
362
542
show-right?
show-right?
0
1
-1000

SWITCH
123
509
244
542
show-sum?
show-sum?
0
1
-1000

SLIDER
169
66
301
99
frequency-right
frequency-right
0.0
100.0
30
1.0
1
NIL
HORIZONTAL

SLIDER
169
99
301
132
amplitude-right
amplitude-right
0.0
50.0
15
1.0
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This model simulates sound wave interference. There is one speaker at each end. A sinusoidal signal generator powers each speaker. The yellow line represents the sound level due to the left speaker, the cyan line represents the sound level due to the right speaker, and the red line represents the sum of the sound levels due to both speakers.

## HOW IT WORKS

Sound effect is due to pressure change spatially and temporally. The waveforms are made up of three lines of turtles. Each turtle acts as it were connected to its neighboring turtles with springs. When neighboring turtles are further away, they exert a stronger force.

When the left end of the sound level goes up, it "pulls up" the turtle to its right, which in turn pulls up the turtle to its right, and so on. In that way, a sound wave moves through the air.

The green turtles (speakers) continue to put more energy into the air. When there is no friction in the air, the waves in the air travel without losing amplitude.

## HOW TO USE IT

Click the SETUP button to set up the system. Then, click GO to turn on the speakers.

The FRICTION slider controls the amount of sound damping in the air. The FREQUENCY slider controls the frequency of the signal generator. The AMPLITUDE slider controls the sound level of the speakers.

There are three buttons to hide each curve and three buttons to show each curve, so that the curves can be observed individually or collectively.

Set the PLOT? switch to on and click anywhere on the horizontal line in the View and you will be able to observe the sound level vs. time at the position you selected. The LISTENING-POINT monitor shows the x coordinate of the point.  A white vertical line in the View also shows it.  Click on the line to move the LISTENING-POINT to different position. The SPEAKER AMPLITUDE plot will plot the sound levels at this listening point.

Set the SHOW-ONLY-RECENT-PLOT? switch to on when you want to see only how the wave has looked in the recent past. With the toggle off, you can see the waves over the whole running time of the model.

## THINGS TO NOTICE

How does the pattern of the left speaker wave and the right speaker wave change when you change the FREQUENCY slider? The AMPLITUDE slider?

When two speakers are turned on, the sound level at a certain point at a certain time is the sum of the sound levels produced by the two speakers at that time.  Its pattern may be quite different from either of the speaker sound patterns.

## THINGS TO TRY

Change the values on the sliders and observe what happens to the sum of the sound levels --- the red curve.

Try adding friction to see what it does to the waves.

Move the listening-point --- what do you observe in the plot window?

Try to create a "standing wave", in which some points on the lines do not move at all, and plot one of the points to see if the sum there is zero.

Try to create a flat red curve.

Compare the relationship between frequency and wavelength.

Find a way to measure the speed of the wave such that the relationship "speed = frequency * wavelength" is true.

## EXTENDING THE MODEL

Program the red turtles to find the sum of the absolute values of the two waves.

Make it possible to "fix" the waves to zero at some point along the line --- as if this were a string and you put your finger on it.

Make the waves "reflect" from each end instead of going on.

## NETLOGO FEATURES

In order to have three independent waves, three lines of turtles are created --- yellow, then cyan, and then red --- in order from left to right.  Special turtles are created to control the ends of these waves.  One end generates the wave (green) and the other end prevents the wave motion from wrapping (dark blue).

For this project, it does not make sense for the turtles to "wrap" when they get to the top or bottom of the world. So the y-position of the turtles is kept in a new variable (YPOS-NEW), and the turtle is hidden if its y-position moves outside the boundary of the world.

During each iteration of GO, each turtle looks at its neighbors and calculates a new speed and position accordingly.  The order in which this is done is not obvious, since the turtles are running in parallel.  It's important that the order in which the turtles look at their neighbors doesn't matter.  Therefore temporary variables are created, "ypos-old", and "yvel-old".   Each turtle looks at its neighbors in previous state and updates its own temporary variables "ypos-new" and "yvel-new".   Then all the turtles update their states together.

## A TRUE STORY

A CCL member was asked by an undergraduate student to help her with some physics experiment problems:

The experiment was about wave propagation and interference.  In the experiment, two speakers are put on a straight track one meter apart and facing each other.  The speakers are connected to a 1500 Hz sinusoidal signal generator.  The student is asked to use a microphone to measure the sound level along the track between the two speakers and write down the positions where the microphone readings are a minimum.

The student is asked to explain the results and to determine if the minimum readings should be zero or not.

The results of the experiment show that the average distance between two minimum readings is about one half of the wavelength. The CCL member could not explain the results and determine if the readings should be zero or not.

The ROPE sample model helped him to answer the student. In the rope model, one end of the rope is fixed.  So the model setup is similar to the experiment setup except for the length and the frequency.  The CCL member and the student then worked together to modify the rope model and change the meaning of the y coordinate -- changing it from representing the absolute value of the deflection, because the microphone reading is the root mean square value of the sound level. When they ran the program, they got the experimental results and, more importantly, it became very clear to them why the minimum readings should be zero and the distance between any two minima is one half of the wavelength.

Isn't it amazing that such a simple program can be so helpful?

Try and repeat what the student and CCL member did and answer the physical experiment problems.

## RELATED MODELS

Rope

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
NetLogo 5.0RC2
@#$#@#$#@
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

globals [
  membrane-edge-x  ;; horizontal distance from center to edge of membrane
  membrane-edge-y  ;; vertical distance from center to edge of membrane
]

turtles-own [
  edge?            ;; are we on the edge of the membrane?
  driver?          ;; are we part of the green driving plate?
  x                ;; position on x axis in space
  y                ;; position on y axis in space
  z                ;; position on z axis in space
  velocity         ;; velocity along z axis
  neighbor-turtles ;; agentset of turtles adjacent to us
]

to setup
  clear-all
  set membrane-edge-x floor (max-pxcor / 2)
  set membrane-edge-y floor (max-pycor / 2)
  set-default-shape turtles "circle"
  ask patches with [(abs pxcor <= membrane-edge-x) and
                    (abs pycor <= membrane-edge-y)]
    [ sprout 1
        [ set edge? (abs xcor = membrane-edge-x) or
                    (abs ycor = membrane-edge-y)
          if edge? [ set color blue ]
          set driver? (abs (xcor - driver-x) <= driver-size) and
                      (abs (ycor - driver-y) <= driver-size)
          if driver? [ set color green ]
          set x xcor
          set y ycor
          set z 0
          set velocity 0
          recolor ] ]
  ask turtles
    [ set neighbor-turtles turtles-on neighbors4 ]
  project
  reset-ticks
end

to recolor  ;; turtle procedure
  if not edge? and not driver?
    [ set color scale-color red z -20 20 ]
end

to go
  ask turtles with [not driver? and not edge?]
    [ propagate ]
  ask turtles
    [ ifelse driver?
        [ set z (driver-amplitude * (sin (0.1 * driver-frequency * ticks))) ]
        [ set z (z + velocity)
          recolor ] ]
  project
  tick
end

to propagate   ;; turtle procedure -- propagates the wave from neighboring turtles
  set velocity (velocity +
                 (stiffness * 0.01 *
                   (sum [z] of neighbor-turtles
                    - 4 * z)))
  set velocity (((1000 - friction) / 1000) * velocity)
end

;;; procedures for displaying in 2-D or 3-D

to project
  ifelse three-d?
    [ project-3d ]
    [ project-2d ]
end

to project-3d
  ask turtles [
    let xc (x + (cos view-angle) * y)
    let yc (z + (sin view-angle) * y)
    ifelse patch-at (xc - xcor) (yc - ycor) != nobody
    [ setxy  xc yc
      show-turtle ]
    [ hide-turtle ]
    recolor
  ]
end

to project-2d
  ;; Set our viewable x and y coordinates to be the same as our real
  ;; coordinates.  This is only needed for if the user turns THREE-D?
  ;; off while the model is running.
  ask turtles
    [ setxy x y
      recolor
      show-turtle ]
end
@#$#@#$#@
GRAPHICS-WINDOW
299
10
795
527
40
40
6.0
1
10
1
1
1
0
0
0
1
-40
40
-40
40
1
1
1
ticks
30

BUTTON
71
41
149
74
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
152
41
229
74
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
10
114
150
147
stiffness
stiffness
0
50
50
1
1
NIL
HORIZONTAL

SLIDER
11
181
151
214
driver-x
driver-x
-11
11
-11
1
1
NIL
HORIZONTAL

SLIDER
152
181
292
214
driver-y
driver-y
-11
11
0
1
1
NIL
HORIZONTAL

SWITCH
11
324
129
357
three-d?
three-d?
0
1
-1000

SLIDER
11
215
151
248
driver-size
driver-size
0
10
2
1
1
NIL
HORIZONTAL

SLIDER
152
215
292
248
driver-amplitude
driver-amplitude
0
30
9
1
1
NIL
HORIZONTAL

SLIDER
71
249
234
282
driver-frequency
driver-frequency
0
100
100
1
1
NIL
HORIZONTAL

SLIDER
151
114
292
147
friction
friction
0
99
10
1
1
NIL
HORIZONTAL

SLIDER
130
324
292
357
view-angle
view-angle
0
90
45
1
1
NIL
HORIZONTAL

TEXTBOX
14
92
135
112
Membrane settings
11
0.0
0

TEXTBOX
13
161
103
179
Driver settings
11
0.0
0

TEXTBOX
11
306
101
324
Display settings
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

This model simulates wave motion in a membrane. The four edges of the membrane are fixed to a frame. A green rectangular area represents a driver plate that moves up and down, exhibiting sinusoidal motion.

## HOW IT WORKS

The membrane is made up of lines of turtles. Each turtle acts as it were connected to its four neighboring turtles by springs. In this model, turtles move only up and down -- the force's direction IS only up and down. The greater the distance between a turtle and its neighbors, the stronger the force.

When the green turtles move up, they "pull up" the turtles which are their neighbors, which in turn pull up the turtles which are their neighbors, and so on. In that way, a wave moves along the membrane. When the wave reaches the edges of the membrane (the blue turtles), the wave is reflected back to the center of the membrane.

The amplitude of the green turtles is fixed regardless of the stiffness of the membrane. However, moving a stiff membrane requires a lot more force to move it the same amount as an unstiff membrane. So even as the stiffness of the membrane is increased, the wave height will remain the same because the amplitude is kept the same.

## HOW TO USE IT

Controls of membrane properties:

The FRICTION slider controls the amount of friction or attenuation in the membrane. The STIFFNESS slider controls the force exerted on a turtle by a unit deflection difference between the turtle and its four neighbors.

Controls of the driving force:

The DRIVER-FREQUENCY slider controls the frequency at which the green area of the membrane (the driving force) moves up and down. The DRIVER-AMPLITUDE slider controls the maximum height of the green area of the membrane.

The DRIVER-X and DRIVER-Y sliders control the position of the driver. The DRIVER-SIZE slider controls the size of the driver.

Controls for viewing:

The THREE-D? switch controls the view point of the projection.  OFF is for the top view (2-D looking down), and ON gives an isometric view, at an angle chosen with the VIEW-ANGLE slider.

## THINGS TO TRY

Click the SETUP button to set up the membrane. Click GO to make the selected area of the membrane (the green turtles) begin moving up and down.

Try different membranes. Soft membranes have smaller stiffness values and hard membranes have larger stiffness values.

Try different driving forces, or try changing the frequency or amplitude. It is very interesting to change the size and the position of the driving force to see symmetrical and asymmetrical wave motions.

Try to create a "standing wave," in which some points in the membrane do not move at all.

## EXTENDING THE MODEL

In this model, the movement of the turtles is only in the vertical direction, perpendicular to the membrane. Modify the model such that the movement is within the membrane plane, i.e. the x/y plane.

You can also try to add additional driving forces to make a multi-input membrane model. Another thing you can try is to apply different waveforms to the driving-force to see how the membrane reacts to different inputs. Try changing the overall shape of the driving force.

Try to build a solid model, that is, a model of waveforms within all three dimensions.

Instead of using amplitude to create the wave, change it to apply a fixed amount of force continuously.

## NETLOGO FEATURES

Note the use of the `turtles-on` reporter to find turtles on neighboring patches.

A key step in developing this model was to create an internal coordinate system. X, Y, and Z are just three turtles-own variables. You can imagine that turtles are situated in and move around in 3-space.  But to display the turtles in the view, which is two-dimensional, the turtle's three coordinates must be mapped into two.

In the 2-D view, the turtle's x and y coordinates are translated directly to NetLogo coordinates, and the z coordinate is indicated only by varying the color of the turtle using the `scale-color` primitive.

In the 3-D view, an isometric projection is used to translate x, y, and z (the turtle's real position) to xcor and ycor (its position in the view).  In this projection, a  point in the world may correspond to more than one point in the 3-dimensional coordinate system.  Thus in this projection we still vary the color of the turtle according to its z position, to help the eye discriminate.

In the 3-D version, it does not make sense for the turtles to "wrap" if they reach the top or bottom of the world nor does it make sense for them to remain at the top of the world, so turtles are hidden if their computed ycor exceeds the boundaries of the world.

## CREDITS AND REFERENCES

Thanks to Weiguo Yang for his help with this model.
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

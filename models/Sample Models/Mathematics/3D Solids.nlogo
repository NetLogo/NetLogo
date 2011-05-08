turtles-own [
  x-pos  ;; x-pos, y-pos, and z-pos are the cartesian coordinates
  y-pos  ;; don't confuse them with xcor and ycor, which are predefined
  z-pos  ;;   NetLogo variables for turtles
  p      ;; p, theta, and phi are the spherical coordinates
  theta
  phi
 ]

to setup
  clear-all
  set-default-shape turtles "circle"
end

to setup-sphere
  setup
  ;; generate a sphere of radius SHAPE-SIZE
  crt num-turtles
  [
    set p shape-size            ; all points are equal distance from the center
    set theta random-float 360  ; however, random distribution on the surface of the sphere
    set phi random-float 180
    render-turtle
  ]
  reset-ticks
end

; a filled 3D cube
to setup-cube-filled
  setup
  ;; generate a square with edge of length radius
  ;; placing a point randomly anywhere inside the square
  crt num-turtles
  [
    cartesian ((- shape-size) + 2 * (random-float shape-size))
              ((- shape-size) + 2 * (random-float shape-size))
              ((- shape-size) + 2 * (random-float shape-size))
    render-turtle
  ]
  reset-ticks
end

; cube with turtles only on its surface
to setup-cube-surface
  setup
  crt num-turtles
  [
    let temp-alpha shape-size * (1 - 2 * (random 2))   ; +-shape-size
    ; random distribution bounded by +-shape-size
    let temp-beta shape-size - 2 * (random-float shape-size)
    let temp-gamma (random 2)                          ; front/back or left/right?
    ifelse temp-gamma = 0                              ; generate front & back surfaces
    [
      cartesian (temp-alpha)
                (temp-beta)
                (shape-size - (2 * (random-float shape-size)))
    ]
    [
      cartesian (temp-beta)                             ; generating the side surfaces
                (temp-alpha)
                (shape-size - (2 * (random-float shape-size)))
    ]
    render-turtle
  ]
  reset-ticks
end


; 3D cone
to setup-cone
  setup
  crt num-turtles
  [
    set theta (random-float 360)        ; points have a random angle
    set p (random-float shape-size)
    cartesian (p * (cos theta))     ; x = r*cos(theta)
              (p * (sin theta))     ; y = r*sin(theta)
              (shape-size - 2 * p)  ; this centers the cone at the origin
                                   ; instead of it only being in positive space
    render-turtle
  ]
  reset-ticks
end

; vertical cylinder
to setup-cylinder-v
  setup
  ;the code is almost the same as the setup-cone code
  ;except the xy-plane radius remains constant
  crt num-turtles
  [
    let temp-alpha (random 3) - 1         ; which surface (left, right, or body?)
    set theta (random-float 360)
    ifelse temp-alpha = 0
    [
      cartesian (shape-size * (cos theta))
                (shape-size * (sin theta))
                ((- shape-size) + 2 * (random-float shape-size))
    ]
    [
      cartesian ((random-float shape-size) * (cos theta))
                ((random-float shape-size) * (sin theta))
                (temp-alpha * shape-size)
    ]
    render-turtle
  ]
  reset-ticks
end

; horizontal cylinder
to setup-cylinder-h
  setup
  ;generates a cylinder in a horizontal position with capped ends
  crt num-turtles
  [
    let temp-alpha (random 3) - 1      ; which surface (left, right, or body?)
    set theta (random-float 360)
    ifelse temp-alpha = 0              ; generating the actual cylinder
    [
      cartesian ((- shape-size) + 2 * (random-float shape-size))
                (shape-size * (cos theta))
                (shape-size * (sin theta))
    ]
    [
      cartesian (temp-alpha * shape-size)
                ((random-float shape-size) * (cos theta))
                ((random-float shape-size) * (sin theta))
    ]
    render-turtle
  ]
  reset-ticks
end

to setup-pyramid
  setup
  crt num-turtles
  [
    let temp-alpha (- shape-size) + 2 * (random-float shape-size)  ; z coordinate
    set theta (random 2)                         ; front/back or side?
    let temp-beta (shape-size - temp-alpha) / 2
    let temp-gamma -1 + 2 * (random 2)           ; left/right or front/back (-1 or 1)
    ifelse theta = 0
    [
      cartesian (temp-beta * temp-gamma)          ;  left & right surfaces
                ((- temp-beta) + 2 * (random-float temp-beta))
                (temp-alpha)
    ]
    [
      cartesian ((- temp-beta) + 2 * (random-float temp-beta)) ;  front & back surfaces
                (temp-beta * temp-gamma)
                (temp-alpha)
    ]
    render-turtle
  ]
  reset-ticks
end

;; convert from cartesian to spherical coordinates
to cartesian [x y z]
  set p sqrt((x ^ 2) + (y ^ 2) + (z ^ 2))
  set phi (atan sqrt((x ^ 2) + (y ^ 2)) z)
  set theta (atan y x)
end

to go
  ; rotate-turtles on z axis
  ask turtles
  [
    set theta (theta + theta-velocity) mod 360  ; increment angle to simulate rotation
    render-turtle
  ]
  tick
end

to render-turtle
  calculate-turtle-position
  set-turtle-position
end

;; convert from spherical to cartesian coordinates
to calculate-turtle-position
  set y-pos p * (sin phi) * (sin theta)
  set x-pos p * (sin phi) * (cos theta)
  set z-pos p * (cos phi)
end

;; set the turtle's position and color
to set-turtle-position
  ifelse view = "side"                                     ; sideview
  [
    setxy x-pos z-pos
    set color scale-color display-color y-pos (- shape-size) shape-size
  ]
  [
    ifelse view = "top"                                  ; topview
    [
      setxy x-pos y-pos
      set color scale-color display-color z-pos (- shape-size) shape-size
    ]
    [
      setxy (p * (sin phi) * (cos theta))              ; bottomview
            (- (p * (sin phi) * (sin theta)))
      set color scale-color display-color (- z-pos) (- shape-size) shape-size
    ]
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
298
10
671
404
60
60
3.0
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
-60
60
1
1
1
ticks
30

BUTTON
176
38
269
71
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

SLIDER
158
136
288
169
num-turtles
num-turtles
0
2000
800
1
1
NIL
HORIZONTAL

SLIDER
158
232
288
265
theta-velocity
theta-velocity
-10.0
10.0
2
1.0
1
NIL
HORIZONTAL

BUTTON
19
40
118
73
setup-sphere
setup-sphere
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
19
280
136
313
setup-cube-filled
setup-cube-filled
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
158
184
288
217
shape-size
shape-size
10.0
40.0
37.6
0.1
1
NIL
HORIZONTAL

BUTTON
19
88
118
121
setup-cone
setup-cone
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
19
136
136
169
setup-cylinder-v
setup-cylinder-v
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
19
184
134
217
setup-cylinder-h
setup-cylinder-h
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
19
232
150
265
setup-cube-surface
setup-cube-surface
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
19
328
118
361
setup-pyramid
setup-pyramid
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
158
88
288
121
display-color
display-color
5.0
135.0
25
10.0
1
NIL
HORIZONTAL

CHOOSER
200
358
292
403
view
view
"side" "top" "bottom"
0

@#$#@#$#@
## WHAT IS IT?

This model creates 3D shapes out of 2D turtles by mapping turtles between cartesian and spherical three-dimensional coordinates.

## HOW IT WORKS

To create the 3D shapes the program randomly generates turtles about the shell of the shape in either cartesian (x, y, z) or spherical (theta, phi, z) coordinates, depending on which is easier to accomplish but always stores the information converting when necessary in spherical coordinates.  To render the sphere in the NetLogo view, it translates the turtles from spherical to cartesian coordinates using color to simulate depth. The positions of the turtles are always stored as spherical coordinates because they are rotated on the z-axis, and the simplest way to do so is to increase theta in spherical coordinates.

Converting from cartesian to spherical coordinates:

>x = r * cos(theta) = p * sin(phi) * cos(theta)  
>y = r * sin(theta) = p * sin(phi) * sin(theta)  
>z = p * cos(theta)

theta:  angle of the turtle's projection on the x-y plane.  
phi:  turtles angle of incidence to the z axis.  
p: distance of the turtle from the origin.

## HOW TO USE IT

Click the different setup-SHAPE buttons to generate different 3D shapes. The turtles are randomly distributed about the surface of the shape. Click the go (forever) button to run the model.

GO starts rotating the model.

COLOR determines the color that is used to simulate depth in generating the various shapes (uses predefined NetLogo color constants).

NUM-TURTLES determines the number of turtles that are used to generate the various shapes.

SHAPE-SIZE determines the overall size of the shape.  Most often it is radius or edge length.

THETA-VELOCITY determines the speed at which the turtles are rotated.

(Rotating turtles in the rotate-turtles procedure is implemented simply by increasing each turtle's theta variable by theta-velocity!  *Rotating* turtles (around the z-axis) is easy in spherical coordinates.  However it's far easier to *transpose* turtles in cartesian coordinates.)

## THINGS TO NOTICE

Notice that turtles closer (positive) on the y-axis appear lighter in shade and turtles further away (negative) appear darker in shade.

## THINGS TO TRY

Try adjusting the theta-vel or render-color slider as the model is running.  This will provide real time feedback to your adjustments.

## EXTENDING THE MODEL

[EASY] Adjust the `setup-square` procedure to generate a rectangle.

Create a procedure to transpose turtle coordinates.  Remember that it is easier to transpose in cartesian coordinates.

Create a procedure to generate new 3D geometries.

Try animating the phi variable.  Conceptually why does this not make sense?

Create a procedure to rotate the geometries on a different axis.

[VERY DIFFICULT] Create a procedure to view the geometries at ANY angle instead of the present three.

## NETLOGO FEATURES

Notice the use of `scale-color` to show the depth of turtles and thereby simulate 3D.

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
NetLogo 5.0beta2
@#$#@#$#@
setup-sphere
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

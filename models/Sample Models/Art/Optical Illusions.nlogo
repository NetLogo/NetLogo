globals [caption]

;; runs automatically when model is opened
to startup
  set caption ""  ;; otherwise it would start out as 0
end

to illusion-1
  clear-all
  set caption "What color are the circles?"
  set-default-shape turtles "circle"
  ;; make the gray lines
  ask patches with [pxcor mod 5 = 0 or
                    pycor mod 5 = 0]
    [ set pcolor gray ]
  ;; make the circles
  ask patches with [pxcor mod 5 = 0 and
                    pycor mod 5 = 0]
    [ sprout 1
        [ set color white
          set size 1.7 ] ]
end

to illusion-2
  clear-all
  set caption "Are the thick lines parallel?"
  ask patches
    [ set pcolor white ]
  ;; make 2 parallel lines
  ask patches with [abs pycor = 4]
    [ set pcolor black ]
  ;; make diagonal lines that start at the center
  ;; and go to different parts of the world
  crt 60
    [ set heading who * 6
      set color black
      set shape "line"
      set size 100 ]
  ;; make a circle in the center
  crt 1
    [ set color black
      set shape "circle"
      set size 5 ]
end

to illusion-3
  clear-all
  set caption "Are the lines parallel?"
  set-default-shape turtles "square"
  ask patches
    [ set pcolor gray ]
  ;; every fourth patch creates a square, either black or white
  ask patches with [pxcor mod 4 = 0 and
                    pycor mod 4 = 0]
    [ sprout 1
        [ set heading 0
          set size 4.8
          ifelse pxcor mod 8 = 0
            [ set color white ]
            [ set color black ] ] ]
  ;; move every other row over 1 to the right
  ask turtles with [pycor mod 16 = 4]
    [ set xcor xcor + 1 ]
  ;; move every other row over 1 to the left
  ask turtles with [pycor mod 16 = 12 ]
    [ set xcor xcor - 1 ]
end

to illusion-4
  clear-all
  set caption "Which inner circle is bigger?"
  set-default-shape turtles "circle"

  ;; make right figure
  let counter 0
  ask patch -12 0
    [ ;; middle circle
      sprout 1
        [ set color white
          set size 5
          ;; 6 smaller circles around the middle circle
          hatch 6
            [ set size 2.5
              set heading 60 * counter
              fd 4
              set counter counter + 1 ] ] ]
  ;; make left figure
  set counter 0
  ask patch 6 0
    [ ;; middle circle
      sprout 1
        [ set color white
          set size 5
          ;; 6 larger circles around the middle circle
          hatch 6
            [ set size 8
              set heading 60 * counter
              fd 8
              set counter counter + 1 ] ] ]
end

to illusion-5
  clear-all
  set caption "Stare at the dot, then lean forward and back."
  ask patches
    [ set pcolor gray ]
  ;; make the inside circle of diamonds
  ;; use ordered turtles so the circle is evenly spaced
  create-ordered-turtles 32
    [ set size 2
      set shape "diamond1"
      fd 12 ]
  ;; make the outside circle of diamonds
  ;; use ordered turtles so the circle is evenly spaced
  create-ordered-turtles 40
    [ set size 2
      set shape "diamond2"
      fd 15 ]
  ;; make the dot in the middle
  crt 1
    [ set shape "circle"
      set color black ]
end

to illusion-6
  clear-all
  set caption "Feeling queasy?"
  ask patches [
    ;; use RGB colors since ordinary NetLogo colors aren't neon enough
    set pcolor one-of [[30 58 190] [133 10 166]]
    ;; place turtles only on patches where both coordinates are even
    if pxcor mod 2 = 0 and pycor mod 2 = 0 [
      sprout 1 [
        set color [99 187 64]
        set shape "lozenge"
        __set-line-thickness 0.15
        set size 2
        ;; make repeating "wave" pattern of headings
        set heading ((pxcor + pycor) / 3) mod 6 * 30
      ]
    ]
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
190
10
695
426
22
17
11.0
1
10
1
1
1
0
0
0
1
-22
22
-17
17
0
0
0
ticks

BUTTON
28
34
154
82
illusion #1
illusion-1
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
28
97
154
145
illusion #2
illusion-2
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
28
162
154
210
illusion #3
illusion-3
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
28
297
154
345
illusion #5
illusion-5
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
28
229
154
277
illusion #4
illusion-4
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
190
428
605
473
NIL
caption
3
1
11

BUTTON
28
365
154
413
illusion #6
illusion-6
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

This model presents six optical illusions.

## HOW IT WORKS

The optical illusions are constructed using a combination of NetLogo turtles and patches.

### Illusion #1

White circles on a gray background produce an illusion of the circles changing colors between black and white, depending on where you focus your eyes.

### Illusion #2

Diagonals moving away from the center create an illusion of depth, which makes the parallel horizontal lines seem to bend in the center.

### Illusion #3

The perpendicular lines that do not match row to row create an illusion of the lines between them being not parallel.

### Illusion #4

Relative sizes of other circles skew the perception of the middle circles, creating an illusion of difference in size.

### Illusion #5

Circles of diamonds seem to rotate as you focus your eyes on their center.

### Illusion #6

Intensely saturated colors create an illusion of rippling waves of motion.

## HOW TO USE IT

Press a button to choose an illusion, and it will be presented in the NetLogo view.

Some explanatory text will appear in the monitor below the view.

## EXTENDING THE MODEL

Add more illusions.

## NETLOGO FEATURES

Many of the illusions in the model, such as illusions 2, 4 and 5, involve turtles positioned in a circle around a middle point.  This shows how to use `forward` and `heading` to position objects in a pattern.

## RELATED MODELS

Other models that were created "for fun" include Frogger, Lunar Lander and other games included in the Games section of the Models Library.

## CREDITS AND REFERENCES

Illusion #6 is based on the cover of the album "Merriweather Post Pavilion" by Animal Collective, which in turn is based on an illusion by Akiyoshi Kitaoka.
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

diamond1
true
0
Line -1 false 32 62 226 61
Line -1 false 226 62 270 240
Line -16777216 false 33 65 75 241
Line -16777216 false 75 241 270 243

diamond2
true
0
Line -1 false 30 239 75 47
Line -1 false 75 46 256 46
Line -16777216 false 31 241 210 242
Line -16777216 false 210 242 256 47

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

lozenge
true
0
Polygon -7500403 true true 0 135 30 90 106 64 150 60 195 63 270 90 300 135 270 180 195 207 150 210 105 207 30 180
Line -1 false 0 135 30 90
Line -1 false 30 90 106 65
Line -1 false 106 65 151 59
Line -1 false 150 59 195 63
Line -1 false 197 63 271 91
Line -1 false 271 93 300 134
Line -16777216 false 1 137 29 181
Line -16777216 false 30 180 104 207
Line -16777216 false 106 206 150 211
Line -16777216 false 150 210 196 208
Line -16777216 false 196 207 270 181
Line -16777216 false 271 180 299 136

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
illusion-3
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

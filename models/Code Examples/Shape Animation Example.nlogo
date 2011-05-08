breed [ flowers flower ]
breed [ people person ]   ;; putting people last ensures people appear
                          ;; in front of flowers in the view

people-own [frame]        ;; ranges from 1 to 9
flowers-own [age]         ;; ranges from 1 to 16

;;
;; SETUP PROCEDURES
;;

to setup                          ;; executed when we press the SETUP button
  clear-all                       ;; clear all patches and turtles
  create-people 1 [
    set heading 90                ;; i.e. to the right
    set frame 1
    set shape "person-1"
  ]
  reset-ticks
end

;;
;; GO PROCEDURES
;;

to go
  animate
  tick
end

to animate
  move-person
  ask flowers [ age-flower ]
  if random 100 < 20
    [ make-flower ]
end

to move-person
  ask people [
    set shape (word "person-" frame)
    forward 1 / 20                ;; The shapes editor has a grid divided into 20 squares, which when
                                  ;; drawing made for useful points to reference the leg and shoe, to
                                  ;; make it look like the foot would be moving one square backward
                                  ;; when in contact with the ground (relative to the person), but
                                  ;; with a relative velocity of 0, when moving forward 1/20th of
                                  ;; a patch each frame
    set frame frame + 1
    if frame > 9
      [ set frame 1 ]             ;; go back to beginning of cycle of animation frames
  ]
end

to age-flower  ;; flower procedure
   set age (age + 1)              ;; age is used to keep track of how old the flower is
                                  ;; each older plant is a little bit taller with a little bit
                                  ;; larger leaves and flower.
   if (age >= 16) [ set age 16 ]  ;; we only have 16 frames of animation, so stop age at 16
   set shape (word "flower-" age)
end

to make-flower
  ;; if every patch has a flower on it, then kill all of them off
  ;; and start over
  if all? patches [any? flowers-here]
    [ ask flowers [ die ] ]
  ;; now that we're sure we have room, actually make a new flower
  ask one-of patches with [not any? flowers-here] [
    sprout 1 [
      set breed flowers
      set shape "flower-1"
      set age 0
      set color one-of [magenta sky yellow]
    ]
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
201
10
697
419
4
3
54.0
1
10
1
1
1
0
1
1
1
-4
4
-3
3
1
1
1
ticks
15

BUTTON
16
40
86
73
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
105
40
175
73
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
29
214
170
247
NIL
reset-perspective
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
29
180
170
213
NIL
follow person 0
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

This code example shows how to use shapes to create animations.  In this example, there is a walking person and growing flowers.

## HOW IT WORKS

A counter is used to increment the shape to be used in each animation.

For the person, each progressive frame of the person walking has a different shape.  There are 9 shapes total.  Each progressive shape is numbered as person_#, where the # is one higher then the shape that came before.  After final numbered shape (person_9), the shapes repeat from the start (person_1) and continue to cycle through the remaining shapes.  The person is moved forward a small bit with each new shape, to give the illusion of walking.

For the flowers, each progressive frame of their growth shows the development of the flower from a small seedling to a fully grown flower.  Each frame has a new shape.  Each progressive shape is numbered as flower_#, where the # is one higher then the shape that came before.  After final numbered shape (flower_16), the shapes stop incrementing and the flower is shown as fully grown.

## NETLOGO FEATURES

The model has a frame rate setting of 15 frames per second, for smooth animation that isn't too fast.  The speed can be further adjusted by the user using the speed slider.
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
Polygon -10899396 true false 150 300 165 300 165 285 165 270

flower-1
false
0
Polygon -10899396 true false 150 300 165 300 165 285 165 270

flower-10
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240
Polygon -10899396 true false 165 150 150 150 135 135 135 105 150 105 165 135
Polygon -7500403 true true 135 120 150 135 135 105

flower-11
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240
Polygon -10899396 true false 150 135 120 120 120 105 135 90 150 90 165 120
Polygon -7500403 true true 150 90 150 120 135 90
Polygon -7500403 true true 120 105 120 120 150 120
Line -7500403 true 150 90 150 120

flower-12
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Circle -10899396 true false 133 105 32
Circle -10899396 true false 152 90 28
Circle -10899396 true false 118 88 32
Circle -10899396 true false 133 73 32
Circle -7500403 true true 125 80 50
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240
Circle -16777216 true false 147 102 6

flower-13
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Circle -10899396 true false 133 120 32
Circle -10899396 true false 167 90 28
Circle -10899396 true false 103 88 32
Circle -10899396 true false 133 58 32
Circle -7500403 true true 120 75 60
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240
Circle -16777216 true false 144 99 12

flower-14
false
0
Circle -10899396 true false 103 58 32
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Circle -10899396 true false 103 120 32
Circle -7500403 true true 133 135 32
Circle -7500403 true true 182 90 28
Circle -10899396 true false 167 60 28
Circle -10899396 true false 167 122 28
Circle -7500403 true true 88 88 32
Circle -7500403 true true 133 43 32
Circle -7500403 true true 105 60 90
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240
Circle -16777216 true false 135 90 30

flower-15
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Circle -7500403 true true 90 137 28
Circle -7500403 true true 130 147 38
Circle -7500403 true true 192 85 38
Circle -7500403 true true 90 45 28
Circle -7500403 true true 182 45 28
Circle -7500403 true true 182 137 28
Circle -7500403 true true 70 85 38
Circle -7500403 true true 130 25 38
Circle -7500403 true true 96 51 108
Circle -16777216 true false 120 75 60
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240

flower-16
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

flower-2
false
0
Polygon -10899396 true false 150 300 180 255 180 270 165 300

flower-3
false
0
Polygon -10899396 true false 150 300 180 240 195 225 195 240 165 300

flower-4
false
0
Polygon -10899396 true false 150 300 180 240 180 210 195 225 195 240 165 300
Polygon -10899396 true false 180 255 165 240 150 240

flower-5
false
0
Polygon -10899396 true false 150 300 180 240 180 210 180 165 195 195 195 240 165 300
Polygon -10899396 true false 180 255 135 225 105 240 135 240

flower-6
false
0
Polygon -10899396 true false 150 300 180 240 180 210 165 165 165 150 195 195 195 240 165 300
Polygon -10899396 true false 180 255 135 210 120 210 90 225 105 240 135 240
Polygon -10899396 true false 185 235 210 210 222 208 210 225

flower-7
false
0
Polygon -10899396 true false 180 255 150 210 105 210 83 241 135 240
Polygon -10899396 true false 150 300 180 240 180 210 165 150 150 135 165 135 195 195 195 240 165 300
Polygon -10899396 true false 189 230 217 200 235 195 255 195 232 210

flower-8
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Polygon -10899396 true false 189 233 219 188 240 180 255 195 228 214
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240
Polygon -10899396 true false 150 135 135 135 135 120 150 120

flower-9
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Polygon -10899396 true false 189 233 219 188 240 180 270 195 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240
Polygon -10899396 true false 150 135 135 120 135 105 150 105
Line -7500403 true 135 105 150 120

flower12
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

man standing
false
0
Circle -13345367 true false 112 23 75
Rectangle -13345367 true false 136 91 164 209
Polygon -13345367 true false 136 107 106 107 91 149 105 149 120 121 136 121 181 121 195 150 211 150 195 106
Polygon -13345367 true false 136 209 106 225 106 255 90 255 90 269 121 269 121 240 150 225 180 240 180 269 211 269 211 256 196 256 196 225 165 210

pentagon
false
0
Polygon -7500403 true true 150 15 15 120 60 285 240 285 285 120

person-1
false
0
Polygon -7500403 true true 120 195 120 105 135 90 195 90 195 105 195 120 180 180
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 165 195 120 180 96 228 60 255 75 300 133 242
Polygon -7500403 true true 180 180 120 180 156 232 159 299 199 299 195 223
Polygon -7500403 true true 135 90 95 125 60 180 90 195 127 137 150 120
Polygon -7500403 true true 180 136 180 151 206 202 234 190 210 136 195 91

person-2
false
0
Polygon -7500403 true true 180 136 165 166 192 203 220 191 202 147 191 98
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 168 176 117 160 105 225 75 255 90 300 135 255
Polygon -7500403 true true 174 164 120 180 150 225 150 300 195 300 195 225
Polygon -7500403 true true 105 180 120 105 135 90 187 89 195 105 195 120 180 180
Polygon -7500403 true true 131 91 96 135 75 180 120 195 135 150 150 120

person-3
false
0
Polygon -7500403 true true 171 123 181 162 165 200 196 201 198 150 194 104
Polygon -7500403 true true 165 180 120 180 120 225 90 285 135 300 165 225
Polygon -7500403 true true 180 180 121 180 158 239 135 300 180 300 180 225
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 116 192 120 105 135 90 180 90 195 105 195 120 180 195
Polygon -7500403 true true 135 90 120 105 93 193 128 199 135 150 150 120

person-4
false
0
Polygon -7500403 true true 180 135 180 165 175 205 186 203 190 135 183 89
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 165 180 120 180 120 225 120 300 165 300 165 225
Polygon -7500403 true true 181 182 151 182 136 227 136 302 181 302 181 212
Polygon -7500403 true true 120 190 120 105 130 90 180 90 195 105 195 120 180 195
Polygon -7500403 true true 145 90 130 102 120 135 120 165 120 210 150 210

person-5
false
0
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 180 180 135 180 135 225 120 300 180 300 180 225
Polygon -7500403 true true 195 180 120 180 135 225 150 300 195 300 201 222
Polygon -7500403 true true 120 195 120 105 135 90 180 90 195 105 195 120 180 195
Polygon -7500403 true true 180 135 180 165 180 195 195 195 195 120 183 89
Polygon -7500403 true true 136 128 136 158 136 203 166 203 165 120 142 95

person-6
false
0
Polygon -7500403 true true 165 180 120 180 122 222 105 300 150 300 165 225
Polygon -7500403 true true 182 174 120 178 165 240 173 298 214 294 210 225
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 116 192 120 105 135 90 180 90 195 105 195 120 180 195
Polygon -7500403 true true 135 90 105 120 93 193 120 195 120 135 150 120
Polygon -7500403 true true 150 124 150 154 161 201 195 199 193 133 183 93

person-7
false
0
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 165 180 120 180 105 240 90 300 135 300 150 240
Polygon -7500403 true true 165 166 120 181 177 229 180 297 221 289 213 210
Polygon -7500403 true true 120 195 120 105 135 90 180 90 195 105 195 120 189 193
Polygon -7500403 true true 122 103 100 133 79 195 111 202 124 165 154 120
Polygon -7500403 true true 180 133 165 163 180 210 210 195 198 146 195 105

person-8
false
0
Polygon -7500403 true true 183 168 138 183 178 239 203 300 240 280 213 226
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 120 180 120 105 135 90 180 90 195 105 195 120 195 180
Polygon -7500403 true true 135 90 99 127 66 183 92 197 118 158 150 120
Polygon -7500403 true true 180 133 165 163 210 195 232 179 203 141 183 87
Polygon -7500403 true true 165 180 120 180 105 240 75 300 120 300 150 240

person-9
false
0
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 165 180 120 180 105 225 68 289 105 300 150 225
Polygon -7500403 true true 188 185 135 180 163 240 171 300 217 301 200 229
Polygon -7500403 true true 121 180 121 105 136 90 181 90 195 97 196 120 187 185
Polygon -7500403 true true 135 90 90 135 60 180 90 195 120 150 150 120
Polygon -7500403 true true 185 137 185 154 222 199 246 179 215 139 185 92

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
setup
repeat 199 [ animate ]
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

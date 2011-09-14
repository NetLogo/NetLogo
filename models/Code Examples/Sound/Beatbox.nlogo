;; this model uses the following command and reporters that
;; are part of the sound extension:
;;    drums -- reports a list of the names of all available MIDI drums
;;    play-drum -- hits a drum
;; and that's it!  see the "Sound" section of the NetLogo User Manual
;; for more details on the sound extension.
extensions [ sound ]

;; this is used to detect separate mouse clicks. once the mouse button is
;; pressed, we want that to be considered as only a single click until the
;; button is released again.
globals [mouse-released?]

;; the "drummers" are the little white circles that move across the graphics
;; window.  the "notes" are the colored squares.  when a drummer passes
;; over a note, the drummer plays its drum.
breed [ drummers drummer ]
breed [ notes note ]

;; "instrument" is the name of a drummer's drum
drummers-own [instrument]

to setup
  clear-all
  set mouse-released? true
  set-default-shape drummers "circle"
  set-default-shape notes "square"

  ;; make the grid lines to help the user see the structure of the beat
  ask patches
    [ set plabel-color gray + 1 ]
  ask patches with [pycor mod 2 = 0 and pxcor != min-pxcor ]
    [ set pcolor gray - 4.8 ]

  turn-all-drums-on
  create-drummers world-height - 1 [
    set color white
    set shape "circle"
    set size 0.2
    set xcor -1 * max-pxcor
    set heading 90
    ;; "who" is the turtle's id number.  id numbers are assigned
    ;; starting from zero.  each turtle has a different number, so
    ;; each turtle occupies a different row and gets a different drum.
    set ycor max-pycor - who - 1
    set instrument item who sound:drums
    ask patch-ahead 8 [ set plabel [instrument] of myself ]
  ]
  label-beats
  reset-ticks
end

;; this puts text in the top row and draws gray columns;
;; this is to help the user place drum hits
to label-beats
  let text ["1" "e" "+" "a" "2" "e" "+" "a"
            "3" "e" "+" "a" "4" "e" "+" "a"]
  ;; this gets a bit tricky.  basically we're stepping
  ;; through the string a character at a time, placing
  ;; one character on every other patch along the top edge.
  ;; when we hit a number, we turn that whole column
  ;; dark gray.
  let n 0
  foreach text [
    let x (-1 * max-pxcor + n + 1)
    ;; "?" is used inside foreach to refer to the
    ;; current item of the list we're looping through
    ask patch x max-pycor [ set plabel ? ]
    if member? ? ["1" "2" "3" "4"]
      [ ask patches with [pxcor = x]
          ;; subtracting from a color makes it darker
          [ set pcolor gray - 4.8 ] ]
    set n n + 2
  ]
end

to turn-all-drums-on
  ask patches with [pxcor = min-pxcor and pycor != max-pycor]
    [ set pcolor white ]
end

to turn-all-drums-off
  ask patches with [pxcor = min-pxcor]
    [ set pcolor black ]
end

to go
  ;; there are eight patches per "beat" (four beats to a measure)
  ;; and sixty seconds in a minute, hence the 7.5 here
  ;; (because 60 divided by 8 is 7.5)
  every 7.5 / bpm [
    ask drummers [
      ;; check the white square on the left to see if this drum
      ;; is "on" or not.  if it is, and if we're over a note,
      ;; then hit our drum
      if ([pcolor] of patch min-pxcor pycor = white) and
      (any? notes-here)
        [ sound:play-drum instrument velocity ]
      fd 1
      ;; when we wrap around the right edge of the world, we
      ;; need to move an extra square in order to skip the first column
      if xcor = -1 * max-pxcor
        [ fd 1 ]
    ]
    tick
  ]
  ;; the edit procedure handles mouse clicks
  edit
end

to edit
  if not mouse-down?
    [ set mouse-released? true ]
  if mouse-released? and mouse-down? [
    ask patch mouse-xcor mouse-ycor
      [ toggle ]
    set mouse-released? false
  ]
end

;; if the user clicks on a note, remove it.
;; if the user clicks where there is no note, make one.
;; if the user clicks on the left column, turn
;;   the patch white or black to indicate whether that drum
;;   is on or off.
to toggle  ;; patch procedure
  if pycor != max-pycor [
    ifelse pxcor = min-pxcor
      [ set pcolor ifelse-value (pcolor = black) [white] [black] ]
      [ ifelse any? notes-here
          [ ask notes-here [ die ] ]
          [ sprout-notes 1 ] ] ]
end

;; return to the beginning of the measure (the left
;; side of the view)
to rewind
  ask drummers [
    set xcor -1 * max-pxcor
  ]
  reset-ticks
end

to load-file
  ;; we don't want the value the user chose for "file" to change when
  ;; the user hits the "load-file" button.  "file" is a global variable,
  ;; and "import-world" wipes out the values of all global variables,
  ;; so we need to store the old value temporarily in a local variable,
  ;; in order to restore it once "import-world" is done.
  let old-value-of-file file
  import-world file
  set file old-value-of-file
  rewind
end

;; we could just tell the user to choose "Export World" on the File
;; menu, but it seems a bit friendlier to provide this in a button
to save-file
  export-world user-new-file
end
@#$#@#$#@
GRAPHICS-WINDOW
307
10
779
559
16
18
14.0
1
10
1
1
1
0
1
0
1
-16
16
-18
18
1
1
1
ticks
30.0

BUTTON
9
12
115
45
setup/clear
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
8
49
115
82
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
24
145
280
178
bpm
bpm
40.0
280.0
120
10.0
1
NIL
HORIZONTAL

TEXTBOX
192
14
304
77
(for steadier beats, freeze view using checkbox above)
11
0.0
0

BUTTON
167
379
290
412
NIL
load-file
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
24
182
280
215
velocity
velocity
0.0
127.0
80
10.0
1
NIL
HORIZONTAL

CHOOSER
11
373
159
418
file
file
"Beats/rock1.csv" "Beats/seth1.csv" "Beats/seth2.csv" "Beats/seth3.csv"
0

BUTTON
8
85
115
118
NIL
rewind
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
77
229
227
262
NIL
turn-all-drums-on
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
77
263
227
296
NIL
turn-all-drums-off
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
88
334
211
367
NIL
save-file
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

This is a drum machine made with NetLogo.  It uses the sound extension.  You can make your own beats with it.  Beats can be saved to disk and then loaded back in later.

## HOW IT WORKS

Colored squares represent drum hits.  The world is a "map" of the composition in which time moves from left to right.  Each row of patches is a particular drum, and each column represents a moment in time.

## HOW TO USE IT

To make a beat, first press the GO button to start the drum machine running.  Then click the mouse in the view to add and remove drum hits.

You can adjust the speed of the music with the BPM (beats per minute) slider, and the volume using the VELOCITY slider.  You can think of velocity as how fast the drumstick hits the drum.  A faster hit makes a louder sound.  The term "velocity" is standard in MIDI.  (MIDI is the underlying music technology that NetLogo's sound extension uses.)

To silence an individual drum, click the white square to the left of the drum's name.  To bring it back, click the same square again.  This lets you experiment with the effect of adding or removing different drums from your beat.

## NETLOGO FEATURES

For more information on the sound extension, and on extensions in general, see the "Sound" and "Extensions" sections of the NetLogo User Manual.

## RELATED MODELS

Composer
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
NetLogo 5.0beta4
@#$#@#$#@
import-world "Beats/seth3.csv"
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

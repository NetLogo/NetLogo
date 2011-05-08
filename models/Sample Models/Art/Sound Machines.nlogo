extensions [sound]

globals [
  selected    ;; lever that is currently selected
  dragged     ;; lever that is currently being dragged
]

breed [levers lever]
levers-own [
  neighbor1 neighbor2  ;; each holds an adjacent lever (or nobody)
  fixed?               ;; if true, lever only turns, never changes position
  spin                 ;; -1, 0, or 1
  len                  ;; this times SCALE slider equals size
  new-xcor new-ycor    ;; next values for xcor and ycor
  new-heading          ;; next value for heading
  xvel yvel            ;; x and y velocities
]

;;;
;;; SETUP PROCEDURES
;;;

to setup-begin
  clear-all
  set-default-shape levers "lever"
  create-ordered-levers num-levers [
    set heading 180
    set new-xcor 0
    set ycor ((who / num-levers) * world-height + min-pycor ) * -0.8
    set new-ycor ycor
    set fixed? false
    set len 0.5
    set neighbor1 turtle (who - 1)
    set neighbor2 turtle (who + 1)
  ]
  ask lever 0 [ set fixed? true ]
end

to setup-dangle
  setup-begin
  ask turtle (num-levers - 4) [
    set len len * 2
    set spin 1
  ]
  setup-finish
end

to setup-finish
  set selected last sort levers  ;; select last lever
  ask levers [ lever-display ]
  reset-ticks
end

to setup-chaos-tentacle
  setup-begin
  ask levers
    [ set len (num-levers - who) * 0.1
      set spin (who mod 2) * 2 - 1
    ]
  ask first sort levers
    [ set new-xcor 0
      set new-ycor 0
      setxy 0 0
      set fixed? true
    ]
  setup-finish
end

to setup-crazy-machine
  setup-begin
  ask levers
    [ ; choose random location, in the inner area of the world
      set new-xcor (random-float 1.6 - 0.8) * max-pxcor
      set new-ycor (random-float 1.6 - 0.8) * max-pycor
      setxy new-xcor new-ycor
      set fixed? (random 5 = 0)
      set spin one-of [-1 0 1]
      set len precision (0.5 + 0.5 * random 4) 1
      lever-display
    ]
  setup-finish
end

;;;
;;; RUNTIME PROCEDURES
;;;

to go
  ask levers [ monitor-mouse ]
  ask levers [ lever! ]
  ask levers [ lever-display ]
  tick
end

to lever!
  ifelse self = dragged
  [ ; if being dragged, then go there the mouse tells you
    set new-xcor mouse-xcor
    set new-ycor mouse-ycor
  ]
  [
    if not fixed?
    [ set xvel (xvel * f) + ((n1x2 - x1) + (n2x1 - x2)) * 0.5
      set yvel (yvel * f) + ((n1y2 - y1) + (n2y1 - y2)) * 0.5
      set yvel yvel - g * len
      set new-xcor xcor + xvel
      set new-ycor ycor + yvel
      if abs new-xcor > max-pxcor [ set xvel xvel * -0.99
                                    set new-xcor max-pxcor * sign new-xcor + xvel
                                    bonk! ycor ]
      if abs new-ycor > max-pycor [ set yvel yvel * -0.99
                                    set new-ycor max-pycor * sign new-ycor + yvel
                                    bonk! xcor ]
    ]
  ]
  ; calculate new heading of lever
  ; if node2 of neighbor1 is overlapping center, keep same heading
  ; otherwise, find heading to hook 2 of neighbor1
  let a 180 + heading
  let b heading
  if distancexy n1x2 n1y2 > 0
    [ set a 180 + towardsxy n1x2 n1y2 ]
  ; likewise hook 1 of neighbor2
  if distancexy n2x1 n2y1 > 0
    [ set b towardsxy n2x1 n2y1 ]
  ; use trig to take mean of a and b
  set new-heading atan (sin a + sin b)
                       (cos a + cos b)
                  + spin * spin-speed
end

to lever-display
  setxy new-xcor new-ycor
  set heading new-heading
  set size 2 * scale * len
  let -s ""
  ifelse self = selected
  [ set -s "-s"
    let new-label (word "selected: " who " - " (precision len 2) " ")
    ask patch max-pxcor max-pycor [ set plabel new-label ]
    set label (word who " - " (precision len 2) " "  " ---->     ")
  ]
  [ set label "" ]
  set shape (word "lever"
                  ifelse-value fixed? ["-f"] [""]
                  item (spin + 1) ["-ws" "" "-cw"]
                  -s)
end

;; lever procedures; these report the x and y coordinates
;; of this lever's hooks
to-report x1 report xcor - dx * scale * len end
to-report y1 report ycor - dy * scale * len end
to-report x2 report xcor + dx * scale * len end
to-report y2 report ycor + dy * scale * len end

;; lever procedures; these report the x and y coordinates
;; of our neighbor's hooks. if no neighbor, use own hook.
to-report n1x1 ifelse neighbor1 = nobody [ report x2 ] [ report [x1] of neighbor1 ] end
to-report n1y1 ifelse neighbor1 = nobody [ report y2 ] [ report [y1] of neighbor1 ] end
to-report n1x2 ifelse neighbor1 = nobody [ report x1 ] [ report [x2] of neighbor1 ] end
to-report n1y2 ifelse neighbor1 = nobody [ report y1 ] [ report [y2] of neighbor1 ] end
to-report n2x1 ifelse neighbor2 = nobody [ report x2 ] [ report [x1] of neighbor2 ] end
to-report n2y1 ifelse neighbor2 = nobody [ report y2 ] [ report [y1] of neighbor2 ] end
to-report n2x2 ifelse neighbor2 = nobody [ report x1 ] [ report [x2] of neighbor2 ] end
to-report n2y2 ifelse neighbor2 = nobody [ report y1 ] [ report [y2] of neighbor2 ] end

to-report sign [a]
  ifelse a = 0
    [ report 0 ]
    [ ifelse a < 0 [ report -1 ]
                   [ report 1 ] ]
end

;;;
;;; SELECTION & DRAGGING PROCEDURES
;;;

to monitor-mouse
  let mouse-here? abs (mouse-xcor - xcor) < scale / 2 and
                  abs (mouse-ycor - ycor) < scale / 2
  ifelse mouse-here?
  [ if color != white [ set color white ] ]
  [ if color = white  [ set color item (who mod 14) base-colors ] ]
  ifelse mouse-down?
  [ if dragged = nobody and mouse-here?
    [ set dragged self
      set selected self
    ]
  ]
  [ set dragged nobody ]
end

to select-lever [which-lever]
  let new lever (which-lever + [who] of selected)
  if new != nobody [ set selected new ]
end

;;;
;;; SOUND PROCEDURES
;;;

to bonk! [coordinate]
  sound:play-note my-instrument (pitch coordinate) 90 0.05
end

to-report pitch [coordinate]  ;; lever procedure
  report 16 + round (96 * (coordinate + max-pxcor) / world-width)
end

to-report my-instrument  ;; lever procedure
  report item ((who + instrument) mod length sound:instruments)
              sound:instruments
end
@#$#@#$#@
GRAPHICS-WINDOW
265
10
675
441
12
12
16.0
1
11
1
1
1
0
0
0
1
-12
12
-12
12
1
1
1
ticks
60

BUTTON
11
66
112
99
NIL
setup-dangle
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
696
50
868
83
f
f
0.9
0.9999
0.98
1.0E-4
1
NIL
HORIZONTAL

BUTTON
20
173
178
229
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
11
29
226
62
num-levers
num-levers
4.0
100.0
12
1.0
1
NIL
HORIZONTAL

SLIDER
696
118
868
151
scale
scale
0.1
12.0
1.7
0.1
1
NIL
HORIZONTAL

SLIDER
696
84
868
117
g
g
0.0
0.18
0.03
1.0E-4
1
NIL
HORIZONTAL

BUTTON
11
282
73
315
fix/free
ask selected [ set fixed? not fixed? ]
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
74
265
136
298
arm +++
ask selected\n  [ set len precision (len + 0.1) 1 ]
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
74
299
136
332
arm ---
ask selected\n  [ if len > 0.1\n      [ set len precision (len - 0.1) 1 ] ]\n
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
205
265
260
298
next
select-lever 1
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
20
363
243
396
spin-speed
spin-speed
-25.0
25.0
6
0.01
1
NIL
HORIZONTAL

BUTTON
137
265
197
298
spin +1?
ask selected\n  [ ifelse spin != 1\n      [ set spin 1 ]\n      [ set spin 0 ] ]
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
113
66
255
99
NIL
setup-crazy-machine
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
96
397
167
430
0
set spin-speed 0
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

TEXTBOX
11
249
147
267
modify selected lever:
11
0.0
1

BUTTON
169
397
232
430
-->
set spin-speed\n      precision ((round (spin-speed * 2)) / 2.0 + 0.5)\n                1
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
39
397
102
430
<--
set spin-speed\n      precision ((round (spin-speed * 2)) / 2.0 - 0.5)\n                1
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
137
299
197
332
spin -1?
ask selected\n  [ ifelse spin != -1\n      [ set spin -1 ]\n      [ set spin 0 ] ]
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
205
299
260
332
prev
select-lever -1
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
56
100
209
133
NIL
setup-chaos-tentacle
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
691
314
863
347
instrument
instrument
0.0
127.0
32
1.0
1
NIL
HORIZONTAL

MONITOR
691
347
863
392
First Instrument
item instrument sound:instruments
0
1
11

MONITOR
691
395
863
440
Last Instrument
item\n  ((instrument + num-levers) mod length sound:instruments)\n  sound:instruments
3
1
11

TEXTBOX
4
11
154
29
setup:
11
0.0
0

TEXTBOX
12
152
162
170
go:
11
0.0
0

TEXTBOX
694
32
844
50
behavior parameters:
11
0.0
0

TEXTBOX
688
295
838
313
sound parameters:
11
0.0
0

TEXTBOX
277
445
613
463
You may use the mouse to select levers and move them.
11
0.0
0

TEXTBOX
14
347
112
365
spin controls:
11
0.0
1

TEXTBOX
205
251
248
269
select:
11
0.0
1

@#$#@#$#@
## WHAT IS IT?

This model shows one way turtles can make interesting and varied sounds, or if you like, music.  It uses some simple physics to make "machines" that twist, spin, turn, twitch, and bounce.  When a part of the machine touches a wall, ceiling, or floor, it makes a sound.  The pitch of the sound depends on the location of the touch.

You can start with a standard machine, or generate a random one.  You can change and build machines yourself, by changing the characteristics of each part.  You can sit back and let the machines play themselves, or you can use the mouse to move them around to control the sound yourself.

## HOW IT WORKS

The machines are made up of levers.  Levers can be different sizes and are pulled down by gravity (in proportion to their size) and subject to friction.

Each lever has one or two "hooks" to connect it to adjacent levers.  Each lever rather simple-mindedly tries to move and rotate to keep its hooks attached (or at least pointing towards) its neighbors' hooks.

Some levers have additional constraints.  A lever can be fixed in position so it can't move.  It can also rotate, as if powered by a motor.

The rules aren't perfect; for example, sometimes the levers separate.  Nonetheless, the behavior is often surprisingly realistic, and is rich enough to generate interesting motion which generates interesting sounds.

## HOW TO USE IT

Press one of the SETUP... buttons to create a machine.  If you want, change NUM-LEVERS first to get more or fewer levers.

Press GO to start the machine going.  You should start hearing sounds, although if your machine never touches the wall (or ceiling or floor), you won't hear anything.

You can use the mouse to select a lever by clicking on it, or to move a lever by dragging it.  The selected lever has a circle around it.

Once you have selected a lever, a variety of controls on the left let you change that lever's properties.  They are not described in detail here; play with him and see for yourself what they do.

Some parameters affecting the entire system are on the bottom left and the right.  F is friction, G is gravity.  There are also controls for spin.

You can use the INSTRUMENT slider to pick a different musical instrument for the levers to play.

If you make a machine you especially like, you can use Export World and Import World on the File menu to save it out and load it back in again.

## THINGS TO NOTICE

The simple rules followed by the levers produce some quite realistic-looking behavior.

Some machines don't do anything.  Other machines make simple, repetitive motions.  And others are wildly chaotic.

## THINGS TO TRY

Try all of the different SETUP buttons.

Try all of the lever controls on the left.

Explore the effect of the parameters on the right.

See if you can make machines which are somewhat repetitive, but also somewhat unpredictable and chaotic.  Sometimes these make the most pleasing "music".

Using the command center, tell the turtles to put their pens down (using the `pen-down` command, abbreviated `pd`).  This generates some pleasing patterns.  Use `clear-drawing` (abbreviated `cd`) to start a new drawing.

## EXTENDING THE MODEL

There are endless possibilities for how the same basic physics engine could be used to generate sound.  The levers could:

- Play different instruments, instead of all the same one.
- Make percussion sounds in addition pitched sounds.
- Make sounds even when they don't hit the wall
- Make sounds that depend on their characteristics

And so on.

How "normal" and "musical" can you make the model sound?  Currently the rules of the model aren't based on scales or tonality; by choosing pitches more carefully, you might be able to produce music which is less strange, more like music composed by humans.  You could also try to produce more regular rhythms by timing sounds to coincide with a regular beat.

Or, you could take the opposite approach, and see how wild, different, and unusual can you make the sounds.  Make something that doesn't sound like anything you've ever heard before!

## NETLOGO FEATURES

The model uses NetLogo's sound extension.  The sound extension is described in the Sound section of the NetLogo User Manual.

## RELATED MODELS

- Beatbox
- Composer
- GasLab With Sound
- Sound Workbench
- Percussion Workbench

## CREDITS AND REFERENCES

This model is a streamlined variant of the Machines 2005 model created and submitted to the NetLogo User Community Models repository by James Steiner.  Machines 2005 is available from http://ccl.northwestern.edu/netlogo/models/community/machines-2005.

Thanks to James for creating an earlier version which was silent; Seth Tisue for first adding sound to it; and James again for his further improvements, and for releasing the model under a Creative Commons License.
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

ball
true
10
Circle -13345367 true true 15 15 270

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

lever
true
10
Circle -13345367 true true 120 -30 60
Circle -13345367 true true 121 270 58
Line -13345367 true 150 30 150 269
Polygon -13345367 true true 150 90 195 105 149 30 105 105

lever-cw
true
10
Circle -13345367 true true 120 -30 60
Circle -13345367 true true 121 270 58
Line -13345367 true 150 30 150 269
Polygon -13345367 true true 150 90 195 105 149 30 105 105
Line -13345367 true 61 30 240 30
Line -13345367 true 240 30 195 44
Line -13345367 true 238 29 193 15
Line -13345367 true 239 269 61 270
Line -13345367 true 61 270 106 255
Line -13345367 true 104 285 61 271

lever-cw-s
true
10
Circle -13345367 true true 120 -30 60
Circle -13345367 true true 121 270 58
Line -13345367 true 150 30 150 269
Polygon -13345367 true true 150 90 195 105 149 30 105 105
Line -13345367 true 61 30 240 30
Line -13345367 true 240 30 195 44
Line -13345367 true 238 29 193 15
Line -13345367 true 239 269 61 270
Line -13345367 true 61 270 106 255
Line -13345367 true 104 285 61 271
Circle -13345367 false true 29 29 242

lever-f
true
10
Circle -13345367 true true 120 -30 60
Circle -13345367 true true 121 270 58
Line -13345367 true 150 30 150 269
Polygon -13345367 true true 150 90 195 105 149 30 105 105
Rectangle -13345367 true true 135 135 166 166

lever-f-cw
true
10
Circle -13345367 true true 120 -30 60
Circle -13345367 true true 121 270 58
Line -13345367 true 150 30 150 269
Polygon -13345367 true true 150 90 195 105 149 30 105 105
Line -13345367 true 61 30 240 30
Line -13345367 true 240 30 195 44
Line -13345367 true 238 29 193 15
Line -13345367 true 239 269 61 270
Line -13345367 true 61 270 106 255
Line -13345367 true 104 285 61 271
Rectangle -13345367 true true 135 135 165 165

lever-f-cw-s
true
10
Circle -13345367 true true 120 -30 60
Circle -13345367 true true 121 270 58
Line -13345367 true 150 30 150 269
Polygon -13345367 true true 150 90 195 105 149 30 105 105
Line -13345367 true 61 30 240 30
Line -13345367 true 240 30 195 44
Line -13345367 true 238 29 193 15
Line -13345367 true 239 269 61 270
Line -13345367 true 61 270 106 255
Line -13345367 true 104 285 61 271
Rectangle -13345367 true true 135 135 165 165
Circle -13345367 false true 29 30 240

lever-f-s
true
10
Circle -13345367 true true 120 -30 60
Circle -13345367 true true 121 270 58
Line -13345367 true 150 30 150 269
Polygon -13345367 true true 150 90 195 105 149 30 105 105
Circle -13345367 false true 30 30 240
Rectangle -13345367 true true 135 135 165 165

lever-f-ws
true
10
Circle -13345367 true true 120 -30 60
Circle -13345367 true true 121 270 58
Line -13345367 true 150 30 150 269
Polygon -13345367 true true 150 90 195 105 149 30 105 105
Line -13345367 true 120 30 181 30
Line -13345367 true 181 30 233 54
Line -13345367 true 120 30 68 52
Line -13345367 true 68 52 91 15
Line -13345367 true 69 53 111 60
Line -13345367 true 120 270 180 270
Line -13345367 true 179 271 233 246
Line -13345367 true 233 246 210 285
Line -13345367 true 233 245 186 241
Line -13345367 true 120 270 68 247
Rectangle -13345367 true true 135 135 166 166

lever-f-ws-s
true
10
Circle -13345367 true true 120 -30 60
Circle -13345367 true true 121 270 58
Line -13345367 true 150 30 150 269
Polygon -13345367 true true 150 90 195 105 149 30 105 105
Line -13345367 true 120 30 181 30
Line -13345367 true 181 30 233 54
Line -13345367 true 120 30 68 52
Line -13345367 true 68 52 91 15
Line -13345367 true 69 53 111 60
Line -13345367 true 120 270 180 270
Line -13345367 true 179 271 233 246
Line -13345367 true 233 246 210 285
Line -13345367 true 233 245 186 241
Line -13345367 true 120 270 68 247
Circle -13345367 false true 30 30 240
Rectangle -13345367 true true 135 135 166 166

lever-s
true
10
Circle -13345367 true true 120 -30 60
Circle -13345367 true true 121 270 58
Line -13345367 true 150 30 150 269
Polygon -13345367 true true 150 90 195 105 149 30 105 105
Circle -13345367 false true 30 30 240

lever-ws
true
10
Circle -13345367 true true 120 -30 60
Circle -13345367 true true 121 270 58
Line -13345367 true 150 30 150 269
Polygon -13345367 true true 150 90 195 105 149 30 105 105
Line -13345367 true 120 30 181 30
Line -13345367 true 181 30 233 54
Line -13345367 true 120 30 68 52
Line -13345367 true 68 52 91 15
Line -13345367 true 69 53 111 60
Line -13345367 true 120 270 180 270
Line -13345367 true 179 271 233 246
Line -13345367 true 233 246 210 285
Line -13345367 true 233 245 186 241
Line -13345367 true 120 270 68 247

lever-ws-s
true
10
Circle -13345367 true true 120 -30 60
Circle -13345367 true true 121 270 58
Line -13345367 true 150 30 150 269
Polygon -13345367 true true 150 90 195 105 149 30 105 105
Line -13345367 true 120 30 181 30
Line -13345367 true 181 30 233 54
Line -13345367 true 120 30 68 52
Line -13345367 true 68 52 91 15
Line -13345367 true 69 53 111 60
Line -13345367 true 120 270 180 270
Line -13345367 true 179 271 233 246
Line -13345367 true 233 246 210 285
Line -13345367 true 233 245 186 241
Line -13345367 true 120 270 68 247
Circle -13345367 false true 30 30 240

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
set scale 2.5
random-seed 2
setup-crazy-machine
repeat 20 [ go ]
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

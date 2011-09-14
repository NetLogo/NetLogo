breed [ electrons electron ]
breed [ anodes anode ]
breed [ nuclei nucleus ]
breed [ cathodes cathode ]
globals [charge-flow-left charge-flow-right x voltage-left voltage-right]

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  set charge-flow-right 1
  set-default-shape electrons "circle 2"
  ;; create wire
  ask patches
  [ set pcolor gray ]
  ask patches with [pxcor < 0]
  [ set pcolor gray + 2 ]

  ;; create electrons
  crt 200 - resistance-right-wire
  [
    set breed electrons
    setxy random max-pxcor - 3 random-ycor
    set heading random 360
    set color orange - 2
    set size 1
  ]
  crt 200 - resistance-left-wire
  [
    set breed electrons
    setxy random min-pxcor + 3 random-ycor
    set heading random 360
    set color orange - 2
    set size 1
  ]
  ;; now set up the Battery-negative
  ask patches with [pxcor >= max-pxcor - 3 ]
  [
    set pcolor red
  ]

  ;; now set up the Battery-negative
  ask patches with [pxcor <= min-pxcor + 3 ]
  [
    set pcolor black
  ]

  ;; create labels for the battery terminals
  ask patches with [pxcor = min-pxcor + 1 and pycor = 0]
  [ sprout 1
    [
      set breed cathodes
      set shape "plus"
      set size 1.5
    ]
  ]
  ask patches with [pxcor = max-pxcor - 1 and pycor = 0]
  [ sprout 1
    [
      set breed anodes
      set shape "minus"
      set size 1.5
    ]
  ]
  ask n-of resistance-right-wire patches with [pxcor < max-pxcor - 4.5 and pxcor >= 2]
  [ sprout 1
    [
      set breed nuclei
      set size 2
      set shape "circle 2"
      set color blue
    ]
  ]
  ask n-of resistance-left-wire patches with [pxcor < (- 2) and pxcor > min-pxcor + 4.5]
  [ sprout 1
    [
      set breed nuclei
      set size 2
      set shape "circle 2"
      set color blue
    ]
  ]
  ask patches with [ pxcor < 1 and pxcor > ( - 1) ]
  [ set pcolor white ]
  set charge-flow-left 1
  reset-ticks
end

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Runtime Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

to go
  set x resistance-left-wire / ( resistance-right-wire + resistance-left-wire)
  set voltage-left x
  set voltage-right 1 - x
  ;; Rules for electrons
  ask electrons
  [
    ;; electrons-rules for performing simple point collisions
    ;; with nuclei in the wire and in between two collisions,
    ;; drifting steadily drifting forward due to the electric field
    move
    if pcolor = white
    [ set charge-flow-right charge-flow-right + 1
      set xcor (- 2) ]
  ]
  tick

  ;; Keep plotting
  if ticks > 20
  [ do-plot ]

end

;;;;;;;;;;;;;;;;;;;;;;;;;
;; rules for electrons ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

to move
  ifelse not any? nuclei-on neighbors
  [
    ;; drift due to voltage
    set heading 270
    if pcolor = gray
    [ fd (1 - x) ]
    if pcolor = gray + 2
    [ fd x ]
    if pcolor = red
    [ set heading 270 fd 3 ]

  ]
  [
    ;; collide with atoms
    set heading random 180
    if pcolor = gray
    [ fd (1 - x) ]
    if pcolor = gray + 2
    [ fd x ]
  ]
  if pcolor = black
  [ pen-up
    set charge-flow-left charge-flow-left + 1
    hatch 1
      [ set breed electrons
        set color orange - 2
        setxy max-pxcor - 4 random-ycor
        pen-up
      ]
    die
  ]
end


;;;;;;;;;;;;;;;;;;;;;;;;;
;; Plotting procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;;;
to do-plot
  set-current-plot "Current"
  set-current-plot-pen "left current"
  plotxy ticks charge-flow-left / ticks
  set-current-plot-pen "right current"
  plotxy ticks charge-flow-right / ticks
end
@#$#@#$#@
GRAPHICS-WINDOW
184
10
878
173
85
16
4.0
1
30
1
1
1
0
0
1
1
-85
85
-16
16
1
1
1
ticks
30

BUTTON
6
10
90
50
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
92
10
177
50
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

PLOT
391
175
663
359
Current
time
current
0.0
1005.0
0.0
0.35
true
true
"" ""
PENS
"left current" 1.0 0 -16777216 true "" ""
"right current" 1.0 0 -13345367 true "" ""

MONITOR
275
174
389
219
Current in Left wire
charge-flow-left / ticks
2
1
11

BUTTON
5
68
148
101
Watch An Electron
ask electrons [ set color orange - 2 pu ]\nclear-drawing \nask one-of electrons with [xcor > max-pxcor - 10]\n[if pcolor != black [ set color yellow pd \n  watch-me\n]]
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
5
103
124
136
Stop Watching
ask electrons [ pu set color orange - 2]\nreset-perspective\n
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
6
149
171
182
Hide Atoms
ask nuclei [ht]
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
6
182
171
215
Show Atoms
ask nuclei [st]
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
209
311
390
356
Number of Electrons in Left Wire
count electrons with [pcolor = gray + 2 ]
17
1
11

MONITOR
665
312
856
357
Number of Electrons in Right Wire
count electrons with [pcolor = gray ]
17
1
11

MONITOR
665
176
789
221
Current in Right Wire
charge-flow-right / ticks
2
1
11

MONITOR
275
219
389
264
Voltage - Left wire
voltage-left
2
1
11

MONITOR
665
222
789
267
Voltage - Right Wire
voltage-right
2
1
11

SLIDER
214
269
389
302
resistance-left-wire
resistance-left-wire
30
70
60
10
1
NIL
HORIZONTAL

SLIDER
665
271
851
304
resistance-right-wire
resistance-right-wire
30
70
30
10
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This is a simplified, microscopic model of electrical conduction in a series circuit with two resistors (wires). It is based on Drude's free electron theory. The primary purpose of the model is to illustrate how electric current in one wire gets to be equal to electric current in the other even when the wires have different resistances: higher number of electrons moving slowly (towards the battery positive) in one wire, and fewer electrons moving faster in the other wire.

## HOW IT WORKS

The wire in this model (represented by grey patches) is composed of atoms, which in turn are made of negatively charged electrons and positively charged nuclei.  According to the Bohr model of the atom, these electrons revolve in concentric shells around the nucleus.  However, in each atom, the electrons that are farthest away from the nucleus (i.e., the electrons that are in the outermost shell of each atom) behave as if they are free from the nuclear attraction.  These outermost electrons from each atom are called "free electrons".

These free electrons obey a specific set of rules that can be found in the "Procedures" tab.  These rules are as follows: The applied electric voltage due to the battery imparts a steady velocity to the electrons in the direction of the positive terminal. In addition to this drift, the electrons also collide with the atomic nuclei (represented by the blue atoms) in the wire giving rise to electrical resistance in the wire. During these collisions, electrons bounce back, scatter slightly, and then start drifting again in the direction of the battery-positive.

The voltage experienced by the electrons in each wire is inversely proportional to the resistance in each wire (The mechanism of how this emerges is beyond the scope of this model). Note that for simplicity, total voltage is set to 1, and the sum of the voltages in the two wires always equals 1.

Also note that the initial number of free-electrons in each wire is modeled to be inversely related to the resistance in each wire. This is because some metals with high resistance have both a higher number of atoms as well as fewer free-electrons compared to metals with low resistance. It is very important to note that this is an approximate measure of resistance, which in reality also depends on many other factors. The effects of this (and other) approximation(s) used in this model are discussed in the "THINGS TO NOTICE" section.

## HOW TO USE IT

The RESISTANCE-LEFT-WIRE and RESISTANCE-RIGHT-WIRE sliders determine how many atoms are in each wire, and also, the initial number of free-electrons in each wire.

The WATCH AN ELECTRON button highlights an electron and traces its path. Press STOP WATCHING to remove the highlighting.

Using HIDE ATOMS, as the name suggests, you can hide the atoms from view. This does not alter the underlying rules of the model, and is intended to make it easier for you to focus only on the electrons in each wire. The atoms can be brought back to view by clicking SHOW ATOMS.

## THINGS TO NOTICE

In some cases, electric current may be very close, but not exactly equal in both the wires. Also, when you change the resistance in one wire, the relative change in current in each wire (compared to the value of current prior to changing the resistance) may be slightly different than the value expected from Ohm's Law for a series circuit with two resistors.

These inconsistencies result from the following approximations used in the model: a) random placement of atoms within the wires, b) the greatly simplified measure of resistance, and c) the simplified representation of collisions between electrons and atoms. The collisions neglect the finite size of the electrons and atoms, and in addition, are not based on exact mathematical calculations of the velocities before and after the collision.

These approximations were designed on order to make the underlying NetLogo code easily understandable by users without a lot of background in mathematics or programming.

## THINGS TO TRY

1. Run the model with equal values of resistance in each wire. (Press SETUP every time you change the value of resistance in either wire.) Observe the current in both the wires.  Are these values equal? What about the number of electrons in each wire?

2. Increase the resistance in one of the wires.  (Press SETUP every time you change the value of resistance in either wire.) Note the current in both the wires. Is current in each wire still equal?  What about the number of electrons in each wire?

3. Set different values of resistance in each wire. Press SETUP and then run the model. Press WATCH AN ELECTRON.  Using a watch (or the value of TICKS displayed in the model), and note how much time the electron takes to travel through each wire.  Repeat this observation several times. Is the average time taken by electrons to travel through each wire different? If so, why?

4. How would you calculate the total current in the circuit? Is it the same as current in each wire? Or is it the sum of the two currents? What are the reasons for your answer?

## EXTENDING THE MODEL

Can you divide the region between the two battery terminals into three wires (segments) instead of two?

## NOTE TO ADVANCED USERS

1. Resistance is represented in NIELS models in two forms. In the first form of representation, which is used in the Current in a Wire model, the resistance of a material is represented by the number of atoms per unit square area. This representation foregrounds the rate of collisions suffered by free electrons making this the central mechanism of resistance.

In the second form of representation, which is used both in this model as well as in the Parallel Circuit model, resistance determines not only the number of atoms inside the wire, but also the number of free electrons. This is a simplified representation of the fact that some materials with higher resistances may have a fewer number of free electrons available per atom.

2. Both these forms of representations operate under what is known in physics as the "independent electron approximation". That is, both these forms of representations assume that the free-electrons inside the wire do not interact with each other or influence each other's behaviors.

3. It is important to note that both these representations of resistance are, at best, approximate representations of electrical resistance. For example, note that resistance of a conducting material also depends on its geometry and its temperature. This model does not address these issues, but can be modified and/or extended to do so.

If you are interested in further reading about the issues highlighted in this section, here are some references that you may find useful:

Ashcroft, J. N. & Mermin, D. (1976). Solid State Physics. Holt, Rinegart and Winston.

Chabay, R.W., & Sherwood, B. A. (2000). Matter & Interactions II: Electric & Magnetic Interactions. New York: John Wiley & Sons.

## NETLOGO FEATURES

Electrons wrap around the world vertically.

## RELATED MODELS

Electrostatics
Electron Sink
Current in a Wire
Parallel Circuit

## CREDITS AND REFERENCES

This model is a part of the NIELS curriculum. The NIELS curriculum has been and is currently under development at Northwestern's Center for Connected Learning and Computer-Based Modeling and the Mind, Matter and Media Lab at Vanderbilt University. For more information about the NIELS curriculum please refer to http://ccl.northwestern.edu/NIELS.
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

anode
false
14
Rectangle -7500403 true false 0 0 255 300
Rectangle -7500403 false false 30 0 285 300
Rectangle -2674135 true false 0 0 285 300
Rectangle -1184463 false false 0 0 300 300

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

cathode
false
0
Rectangle -13345367 true false 0 0 285 300
Rectangle -1184463 false false 0 0 285 300

circle
false
0
Circle -7500403 true true 0 0 300
Circle -7500403 false true -45 -45 180
Circle -16777216 false false -2 -2 304

circle 2
false
2
Circle -16777216 true false 0 0 300
Circle -955883 true true 30 30 240

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

link
true
0
Line -7500403 true 150 0 150 300

link direction
true
0
Line -7500403 true 150 150 30 225
Line -7500403 true 150 150 270 225

minus
false
14
Rectangle -1 true false 0 90 300 210

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

plus
false
0
Rectangle -1 true false 105 0 195 300
Rectangle -1 true false 0 105 300 195

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
need-to-manually-make-preview-for-this-model
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

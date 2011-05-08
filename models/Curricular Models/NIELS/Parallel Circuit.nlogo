breed [top-electrons top-electron]
breed [bottom-electrons bottom-electron]
breed [nuclei nucleus ]

globals [
  top-current bottom-current
  voltage
]

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  set-default-shape top-electrons "circle 2"
  set-default-shape bottom-electrons "circle 2"

  set-default-shape nuclei "circle 2"
  ;; create the top wire
  ask patches with [top-wire?]
    [ set pcolor gray ]
  ;; create the bottom wire
  ask patches with [not top-wire?]
    [ set pcolor gray + 3 ]

  ;; set up battery negative
  ask patches with [pxcor >= max-pxcor - 4]
    [ set pcolor red ]
  ask patch (max-pxcor - 1) (max-pycor / 2)
    [ set plabel "-" ]

  ;; set up battery positive
  ask patches with [pxcor <= min-pxcor + 4]
    [ set pcolor black ]
  ask patch (min-pxcor + 4) (max-pycor / 2)
    [ set plabel "+" ]

  ;; create electrons in top wire
  ask n-of (280 - resistance-top-wire) patches with [top-wire?]
    [ sprout-top-electrons 1 [
        set color orange - 2
        set size 1
        if pcolor = black [
        set xcor max-pxcor - 3
      ] ] ]
  ;; create electrons in bottom wire
  ask n-of (280 - resistance-bottom-wire) patches with [not top-wire?]
    [ sprout-bottom-electrons 1 [
        set color orange - 2
        set size 1
        if pcolor = black [
        set xcor max-pxcor - 3
      ] ] ]
  ask n-of (resistance-top-wire) patches with [top-wire? and pycor > (max-pycor / 2 + 1) and pxcor < (max-pxcor - 5) and pxcor > (min-pxcor + 5) and pcolor != black and pcolor != red]
    [ sprout-nuclei 1 [
        set color blue
        set size 2.5
         ] ]
  ask n-of (resistance-bottom-wire) patches with [not top-wire? and pycor < (max-pycor / 2 - 0.1) and pxcor < (max-pxcor - 5) and pxcor > (min-pxcor + 5)and pcolor != black and pcolor != red]
    [ sprout-nuclei 1 [
        set color blue
        set size 2.5
         ] ]

  setup-plots
end

to-report top-wire?  ;; turtle or patch procedure
  report pycor > max-pycor / 2
end

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Runtime Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

to go
  set voltage 1
  ask top-electrons
    [ move-electron
      ;; contribute to current measurement
      if pcolor = black [
        pen-up
        set top-current top-current + 1
        hatch 1
        [ setxy max-pxcor - 4 (max-pycor / 2) + random (max-pycor / 2)
          set color orange - 2
        ]
        die
      ] ]
  ask bottom-electrons
    [ move-electron
      ;; contribute to current measurement
      if pcolor = black [
        pen-up
        set bottom-current bottom-current + 1
        hatch 1
        [ setxy max-pxcor - 4 (max-pycor / 2) - random (max-pycor / 2)
          set color orange - 2
        ]
        die
      ] ]
  tick-advance 1  ;; advance tick counter without plotting
  if ticks > 50 [ update-plots ]
end

;; perform simple point collisions
;; with nuclei in the wire and steadily drifting
;; forward due to the electric field
to move-electron
  let old-patch patch-here
  let old-xcor xcor
  let old-ycor ycor
  ifelse not any? nuclei-on neighbors
  [
    ;; drift due to voltage
    set heading 270
    fd voltage
  ]
  [
    ;; collide with atom
    set heading random 180
    fd voltage
  ]
  ;; have we entered the wrong wire? if so, wrap
  if top-wire? != [top-wire?] of old-patch
    [ setxy old-xcor old-ycor ]
end
@#$#@#$#@
GRAPHICS-WINDOW
187
11
710
261
85
-1
3.0
1
30
1
1
1
0
0
0
1
-85
85
0
72
1
1
1
ticks

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
457
265
646
385
Current in bottom wire
time
current
50.0
0.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "plotxy ticks (bottom-current / ticks)"

TEXTBOX
192
265
236
299
Battery-positive
11
0.0
0

TEXTBOX
666
266
714
305
Battery-negative\n
11
0.0
0

PLOT
261
266
455
386
Current in top wire
time
current
50.0
0.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "plotxy ticks (top-current / ticks)"

MONITOR
712
55
808
100
no. of electrons
count top-electrons
3
1
11

MONITOR
713
187
805
232
no. of electrons
count bottom-electrons
3
1
11

MONITOR
711
10
842
55
Current In Top Wire
top-current / ticks
3
1
11

MONITOR
712
141
857
186
Current in Bottom Wire
bottom-current / ticks
3
1
11

MONITOR
813
56
917
101
Voltage - top wire
voltage
17
1
11

MONITOR
807
186
930
231
Voltage - Bottom Wire
voltage
17
1
11

SLIDER
11
99
183
132
resistance-top-wire
resistance-top-wire
50
150
50
25
1
NIL
HORIZONTAL

SLIDER
10
148
182
181
resistance-bottom-wire
resistance-bottom-wire
50
150
50
25
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This model offers a microscopic view of electrical conduction in two wires that are connected in parallel to each other across two terminals of a battery. It shows that current in each wire is not always equal to current in the other wire, unlike in a series circuit (see Series Circuit model). However, since each of the wires is connected across the same battery terminals, voltage is the same in each wire.

## HOW IT WORKS

Each wire in this model is composed of atoms, which in turn are made of negatively charged electrons and positively charged nuclei.  According to the Bohr model of the atom, these electrons revolve in concentric shells around the nucleus.  However, in each atom, the electrons that are farthest away from the nucleus (i.e., the electrons that are in the outermost shell of each atom) behave as if they are free from the nuclear attraction.  These outermost electrons from each atom are called "free electrons".  These free electrons obey a specific set of rules that can be found in the "Procedures" tab.  These rules are as follows: The applied electric voltage due to the battery imparts a steady velocity to the electrons in the direction of the positive terminal. In addition to this drift, the electrons also collide with the atomic nuclei (represented by the blue atoms) in the wire giving rise to electrical resistance in the wire. During these collisions, electrons bounce back, scatter slightly, and then start drifting again in the direction of the battery-positive.

Also note that the initial number of free-electrons in each wire is modeled to be inversely related to the resistance in each wire. This is because some metals with high resistance have both a higher number of atoms as well as fewer free-electrons compared to metals with low resistance. It is very important to note that this is an approximate measure of resistance, which in reality also depends on many other factors. The effects of this (and other) approximation(s) used in this model are discussed in the "THINGS TO NOTICE" section.

Also note that like in Series Circuit, there are two wires, each with its own resistance, but here the wires are connected side-by-side rather than end-to-end.  Electrons from one wire do not cross over into the other wire.

As mentioned earlier, the voltage is the same in each wire. This is a particular characteristic of parallel circuits. For simplicity, the voltage in the circuit is set to a constant value of 1.

The positive battery terminal (represented by black patches), which is actually an enormous collection of positive charges, acts as a sink for the negatively charged free-electrons. The negative battery terminal (represented by red patches) is a large source of negative charges or electrons. Note that electrons reappear on the other side at the negative terminal after entering the positive terminal of the battery.  This simplified representation of the continuous process of charge generation in the battery helps to maintain a constant voltage (or potential difference) between the two terminals.

## HOW TO USE IT

The RESISTANCE-TOP-WIRE and the RESISTANCE-BOTTOM-WIRE sliders control the number of atoms in each wire, as well as the number of free-electrons in each wire.

Note that you will need to press SETUP every time you alter the value of resistance in any wire, in order for the changes to take effect.

## THINGS TO NOTICE

Besides the representation of resistance, there are two other notable approximations in the models. First, the atoms are placed randomly inside the wire. That is, for the same model parameters, every time you press setup, a new configuration of atoms will result. This may result in slightly different values of electric current for the same settings.

Second, the rule for collisions between electrons and atoms is a much simplified, approximate representation. It is based on point collisions that neglect the size of electrons and atoms; in addition, these rules do not use exact mathematical formulae for calculating exact velocities before and after collisions.

[These approximations were designed in order to make the underlying NetLogo code easily understandable by users with little or no background in mathematics and/or programming.]

As a result of these approximations, values may not strictly adhere to Ohm's Law. For example, when you double the value of resistance in either wire, electric current may not be exactly half, as you would expect from Ohm's Law, even though it will be lower. Similarly, even when the wires have equal values of resistance, current in each wire may not be exactly equal (although they may be close).

## THINGS TO TRY

1. Run the model with equal resistance in each wire. Note the current in both the wires. Are these values equal? What about the number of electrons in each wire?

2. Increase the resistance in one of the wires. (Remember to press SETUP everytime you change the values of RESISTANCE-TOP-WIRE and RESISTANCE-BOTTOM-WIRE.) Note the current in both the wires. Does current in the other wire also change? Why or why not? Compare the results with the Series Circuit model.

3. How would you calculate the total current in the circuit?

## EXTENDING THE MODEL

Can you create another wire in series with any of these two wires?

## NOTE TO ADVANCED USERS

1. Resistance is represented in NIELS models in two forms. In the first form of representation, which is used in the Current in a Wire model, the resistance of a material is represented by the number of atoms per unit square area. This representation foregrounds the rate of collisions suffered by free electrons making this the central mechanism of resistance.

In the second form of representation, which is used both in this model as well as in the Series Circuit model, resistance determines not only the number of atoms inside the wire, but also the number of free electrons. This is a simplified representation of the fact that some materials with higher resistances may have a fewer number of free electrons available per atom.

2. Both these forms of representations operate under what is known in physics as the "independent electron approximation". That is, both these forms of representations assume that the free-electrons inside the wire do not interact with each other or influence each other's behaviors.

3. It is important to note that both these representations of resistance are, at best, approximate representations of electrical resistance. For example, note that resistance of a conducting material also depends on its geometry and its temperature. This model does not address these issues, but can be modified and/or extended to do so.

If you are interested in further reading about the issues highlighted in this section, here are some references that you may find useful:

Ashcroft, J. N. & Mermin, D. (1976). Solid State Physics. Holt, Rinegart and Winston.

Chabay, R.W., & Sherwood, B. A. (2000). Matter & Interactions II: Electric & Magnetic Interactions. New York: John Wiley & Sons.

## NETLOGO FEATURES

Electrons do not wrap around the world either horizontally or vertically. Special vertical wrap code is used to keep electrons from changing wires.

## RELATED MODELS

Electrostatics
Electron Sink
Current in a Wire
Series Circuit

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
Circle -7500403 false true -45 -45 180
Circle -16777216 false false -2 -2 304

circle 2
false
0
Circle -16777216 true false 0 0 300
Circle -7500403 true true 30 30 240

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
Line -7500403 true 150 150 30 225
Line -7500403 true 150 150 270 225

@#$#@#$#@
0
@#$#@#$#@

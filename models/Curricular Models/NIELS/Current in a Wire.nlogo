breed [ electrons electron ]
breed [ anodes anode ]
breed [ nuclei nucleus ]
breed [ cathodes cathode ]
globals [charge-flow ]

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  set-default-shape electrons "circle 2"

  ;; create wire
  ask patches
  [ set pcolor gray]

  ;; create electrons
  crt number-of-electrons
  [
    set breed electrons
    setxy random-xcor random-ycor
    set heading random 360
    set color orange - 2
    set size 1
  ]

  ;; now set up the Battery-negative
  ask patches with [pxcor >= max-pxcor - 3]
  [
    set pcolor red
  ]

  ;; now set up the Battery-negative
  ask patches with [pxcor <= min-pxcor + 3]
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

;; create atoms
  ask n-of ( resistance ^ 2 ) patches with [pxcor < max-pxcor - 4.5 and pxcor > min-pxcor + 4.5
                                            and not any? nuclei-here and not any? nuclei-on neighbors ]
  [ sprout 1
    [
      set breed nuclei
      set size 2
      set shape "circle 2"
      set color blue
    ]
  ]
  set charge-flow 0

  reset-ticks
end


;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Runtime Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

to go

  ;; update the size of battery-terminals with values of Voltage
  update-battery-size

  ;; Rules for electrons
  ask electrons
  [
    ;; electrons-rules for performing simple point collisions
    ;; with nuclei in the wire and in between two collisions,
    ;; drifting steadily drifting forward due to the electric field
    move
  ]
  tick

  ;; Keep plotting
  do-plot
end


;;;;;;;;;;;;;;;;;;;;;;;;;
;; rules for electrons ;;
;;;;;;;;;;;;;;;;;;;;;;;;;
to move
  ifelse not any? nuclei-on neighbors
   [
     set heading 270 fd voltage
   ]
   [
     ;; this is a much simplified representation indicating scattering due to point collision
     set heading random 180
     fd voltage
   ]

  ;; calculate current
  if pcolor = black
   [ pen-up
     set charge-flow charge-flow + 1

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

  ;; plot current vs. time
  set-current-plot "Current vs Time"
  plotxy ticks (charge-flow) / ticks

end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Procedures for Counting Current  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to update-battery-size
  ;; now update the size of Battery-negative
  ask cathodes
  [
    set size 1.5 + 0.5 * Voltage
  ]
  ask anodes
  [
    set size 1.5 + 0.5 * Voltage
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
185
14
879
177
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
7
14
91
54
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
14
177
54
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

TEXTBOX
186
186
298
204
Battery-positive (B+)
11
0.0
0

TEXTBOX
767
184
889
212
Battery-negative (B-)\n
11
0.0
0

BUTTON
22
206
156
239
Watch An Electron
ask electrons [ set color orange - 2 pu ]\nclear-drawing \nask one-of electrons with [xcor > max-pxcor - 6]\n[if pcolor != black [ set color yellow pd \n  watch-me\n]]
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
27
239
152
272
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

MONITOR
302
185
451
230
Electrons Arrived At B+
charge-flow
17
1
11

PLOT
453
185
755
353
Current vs Time
Time (Seconds)
Current
0.0
10.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

MONITOR
302
279
403
324
Current
charge-flow / ticks
2
1
11

SLIDER
6
138
178
171
resistance
resistance
10
20
10
2.5
1
NIL
HORIZONTAL

SLIDER
7
63
178
96
number-of-electrons
number-of-electrons
400
800
800
200
1
NIL
HORIZONTAL

SLIDER
7
101
178
134
voltage
voltage
2
6
6
1
1
NIL
HORIZONTAL

MONITOR
302
232
403
277
Timer
ticks
17
1
11

@#$#@#$#@
## WHAT IS IT?

This model shows a simplified microscopic picture of electrical conduction inside a wire connected across two battery terminals. It is based on Drude's free electron theory, and shows how electric current emerges from the collective movement of many electrons inside a wire.

It also shows how electric current depends on the number of free electrons and how fast these electrons are travelling towards the battery-positive. This speed, in turn, depends on a) the applied voltage, and b) the obstacles that the electrons encounter in their way, which are represented in this model by atoms.

## HOW IT WORKS

The wire in this model (represented by gray patches) is composed of atoms, which in turn are made of negatively charged electrons and positively charged nuclei.  According to the Bohr model of the atom, these electrons revolve in concentric shells around the nucleus.  However, in each atom, the electrons that are farthest away from the nucleus (i.e., the electrons that are in the outermost shell of each atom) behave as if they are free from the nuclear attraction.  These outermost electrons from each atom are called "free electrons".   These free electrons obey a specific set of rules that can be found in the "Procedures" tab.  These rules are as follows: The applied electric voltage due to the battery imparts a steady velocity to the electrons in the direction of the positive terminal. In addition to this drift, the electrons also collide with the atomic nuclei (represented by the blue atoms) in the wire giving rise to electrical resistance in the wire. During these collisions, electrons bounce back, scatter slightly, and then start drifting again in the direction of the battery-positive.

The positive battery terminal (represented by black patches), which is actually an enormous collection of positive charges, acts as a sink for the negatively charged free-electrons. The negative battery terminal (represented by red patches) is a large source of negative charges or electrons. Note that electrons reappear on the other side at the negative terminal after entering the positive terminal of the battery.  This simplified representation of the continuous process of charge generation in the battery helps to maintain a constant voltage (or potential difference) between the two terminals.

## HOW TO USE IT

The NUMBER-OF-ELECTRONS slider allows the user to select the total number of free electrons in the wire. This number is kept constant throughout a single run of the model.

The VOLTAGE slider indicates the magnitude of voltage between the battery terminals.  This voltage imparts a steady velocity to the electrons.

The RESISTANCE slider indicates how many atoms are in the wire. The number of atoms created is equal to the square of the value of this slider. Increasing this value will also increase the number of collisions that electrons will suffer inside the wire.

The button WATCH AN ELECTRON highlights a single electron (chosen randomly) in the model so that you can observe and trace its movement.  If you want to go back to the default settings (with all electrons red and no traces), you need to press SETUP. If you simply want to stop watching, press STOP WATCHING.

## THINGS TO NOTICE

This model uses several approximations.

First, the atoms are placed randomly inside the wire. That is, for the same value of RESISTANCE, every time you press setup, a new spatial distribution of atoms will result. This may result in slightly different values of electric current for the same model parameters. [The representation of RESISTANCE in the form of number of atoms is also an approximate representation. In this context, the advanced user may find the discussion in the section titled "NOTES FOR ADVANCED USERS" to be of interest.]

Second, the rule for collisions between electrons and atoms is a much simplified, approximate representation. It is based on point collisions that neglect the size of electrons and atoms; in addition, these rules do not use exact mathematical formulae for calculating exact velocities before and after collisions.

As a result of these approximations, values may not strictly adhere to Ohm's Law. For example, when you double the value of RESISTANCE, electric current may not be exactly half, as you would expect from Ohm's Law, even though it will be lower.

## THINGS TO TRY

1. Run the model for different values of NUMBER-OF-ELECTRONS, while keeping all the other sliders constant. (Remember to press SETUP every time you change the value). How does the value of current in the wire change?

2. Run the model for different values of VOLTAGE, while keeping all the other sliders constant. (Remember to press SETUP every time you change the value). How does the value of current in the wire change? How do you think VOLTAGE affects the motion of the electrons?

3. Run the model for different values of RESISTANCE, while keeping all the other sliders constant. (Remember to press SETUP every time you change the value). How does the value of current in the wire change? How do you think RESISTANCE affects the motion of the electrons?

4.  Press WATCH AN ELECTRON. Using the TIMER monitor, or a stopwatch, note how much time the electron takes to travel through the wire. Repeat this observation several times for the same model parameters. How do you think the average of these values is related to electric current?

## EXTENDING THE MODEL

Can you create a series circuit (with two wires in series) by extending this model?

## NOTE TO ADVANCED USERS

1. Resistance is represented in NIELS models in two forms. In the first form of representation, which is used in this model, the resistance of a material is represented by the number of atoms per unit square area. This representation foregrounds the rate of collisions suffered by free electrons making this the central mechanism that generates resistance.

In the second form of representation, which is used in both the Series Circuit and Parallel Circuit models, resistance determines not only the number of atoms inside the wire, but also the number of free electrons. This is a simplified representation of the fact that some materials with higher resistances may have a fewer number of free electrons available per atom.

2. Both these forms of representations operate under what is known in physics as the "independent electron approximation". That is, both these forms of representations assume that the free-electrons inside the wire do not interact with each other or influence each other's behaviors.

3. It is important to note that both these representations of resistance are, at best, approximate representations of electrical resistance. For example, note that resistance of a conducting material also depends on its geometry and its temperature. This model does not address these issues, but can be modified and/or extended to do so.

If you are interested in further reading about the issues highlighted in this section, here are some references that you may find useful:

Ashcroft, J. N. & Mermin, D. (1976). Solid State Physics. Holt, Rinegart and Winston.

Chabay, R.W., & Sherwood, B. A. (2000). Matter & Interactions II: Electric & Magnetic Interactions. New York: John Wiley & Sons.

## NETLOGO FEATURES

Electrons wrap around the world vertically.

## RELATED MODELS

Electrostatics, Electron Sink, Parallel Circuit, Series Circuit.

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

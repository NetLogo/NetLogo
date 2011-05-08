breed [waters water]            ;; water molecules
breed [hydroniums hydronium]    ;; hydronium ions (green)
breed [hydroxides hydroxide]    ;; base molecules (red)
breed [acids acid]              ;; parent acid molecules (yellow)
breed [conj-bases conj-base]    ;; conjugate base molecules of parent acid (orange)


globals [
  pH
  Ka             ;; variable for acid reactions
  base-added     ;; use to keep track of how much total base has been added
]

to setup
  clear-all
  set-default-shape waters "molecule2"
  set-default-shape hydroniums "molecule3"
  set-default-shape hydroxides "molecule1"
  set-default-shape acids "molecule2"
  set-default-shape conj-bases "molecule1"
  set Ka 1.9
  create-acids vol-acid                 ;; adds parent acid molecules
    [ set color yellow ]
  ask acids
    [ if random-float 100.0 < Ka              ;; creates initial dissociated acid
        [ hatch-hydroniums 1
            [ set color green
              fd 3 ]
         set breed conj-bases                 ;; parent turtle turns into a conj-base
         set color orange ] ]
  create-waters (100 - count hydroniums)
    [ set color blue ]
  ask turtles                   ;; randomize position and heading of turtles
    [ setxy random-xcor random-ycor ]
  set base-added 0
  calculate-pH
  reset-ticks
  plot-pH
end

to go
  ask hydroxides [ react-hydroxide-hydronium ]
  ask hydroxides [ react-hydroxide-acid ]
  ask acids [ react-acid ]
  ask hydroniums [ react-hydronium ]
  ;; move turtles randomly around the world
  ask turtles
    [ fd 1
      rt random 10
      lt random 10 ]
  tick
  calculate-pH
  plot-pH
end

;; adds base molecules to the solution
to add-base
  create-hydroxides vol-base
    [ fd 1
      set color red ]
  set base-added base-added + vol-base
end

;; hydroxide procedure
to react-hydroxide-hydronium
  let partner one-of hydroniums-here     ;; tells hydroxide to recognize hydronium
  if partner != nobody              ;; if you are a hydroxide molecule and you encounter a hydronium
    [ ;; then turn yourself and the hydronium into water molecules
      set breed waters
      set color blue
      ask partner
        [ set breed waters
          set color blue ] ]
end

;; hydroxide procedure
to react-hydroxide-acid
  if random-float 100.0 < Ka                  ;; react with acid molecules according to the acid strength
    [ let partner one-of acids-here
      if partner != nobody              ;; if you are a hydroxide molecule and you encounter an acid molecule
        ;; then turn the hydroxide into a conjugate base molecule
        ;; and turn yourself into a water molecule
        [ set breed waters
          set color blue
          ask partner
            [ set breed conj-bases
              set color orange ] ] ]
end

;; acid procedure
to react-acid
  if random-float 100.0 < Ka                  ;; acid molecules may dissociate to hydronium depending on their Ka
    [ let partner one-of waters-here
      if partner != nobody              ;; if you are an acid molecule and you encounter a water molecule
        ;; then turn the water molecule into a hydronium molecule
        ;; and turn yourself into a conjugate base molecule
        [ set breed conj-bases
          set color orange
          ask partner
            [ set breed hydroniums
              set color green ] ] ]
end

;; hydronium procedure
to react-hydronium
  if random-float 100.0 < Ka                  ;; hydronium molecules may recombine with conjugate base to form acid
    [ let partner one-of conj-bases-here
      if partner != nobody              ;; if you are a hydronium molecule and you encounter a conjugate base molecule
      ;; then turn the conjugate base molecule into an acid molecule
      ;; and turn yourself into a water molecule
        [ set breed waters
          set color blue
          ask partner
            [ set breed acids
              set color yellow ] ] ]
end

;; calculates the pH from the amount of the various ions in solution;
;; note that for simplicity the calculations don't take the true molar
;; concentration of water into account, but instead use an arbitrarily
;; chosen factor of 1000 to produce numbers lying in a reasonable range
to calculate-pH
  let volume count turtles
  let concH (count hydroniums / volume)
  let concOH (count hydroxides / volume)
  ifelse (concH = concOH)
    [ set pH 7 ]
    [ ifelse (concH > concOH)
      [ set pH (- log (concH / 1000) 10) ]
      [ let pOH (- log (concOH / 1000) 10)
        set pH 14 - pOH ] ]
end

;; plotting procedures

to plot-pH
  set-current-plot "pH Curve"
  plot pH
end

to record-pH
  set-current-plot "Titration Curve"
  plotxy base-added pH
end
@#$#@#$#@
GRAPHICS-WINDOW
335
10
695
391
12
12
14.0
1
10
1
1
1
0
1
1
1
-12
12
-12
12
1
1
1
ticks
30.0

MONITOR
265
197
324
242
pH
pH
2
1
11

SLIDER
6
64
187
97
vol-acid
vol-acid
0
100
100
1
1
molecules
HORIZONTAL

SLIDER
6
98
186
131
vol-base
vol-base
1
100
100
1
1
molecules
HORIZONTAL

BUTTON
9
21
70
54
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

BUTTON
76
21
137
54
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
217
79
293
112
add-base
add-base
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
257
320
333
353
record pH
record-pH
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

PLOT
11
312
254
488
Titration Curve
Volume base
pH
0.0
100.0
0.0
14.0
true
false
"" ""
PENS
"default" 1.0 0 -13345367 true "" ""

PLOT
11
145
254
310
pH Curve
Time
pH
0.0
100.0
0.0
14.0
true
false
"" ""
PENS
"default" 1.0 0 -2674135 true "" ""

@#$#@#$#@
## WHAT IS IT?

This model demonstrates the differences in the calculation of pH when evaluating a weak acid in solution. It is best viewed as the second model in the ACID-BASE package.

## HOW IT WORKS

Unlike a strong acid, a weak acid has a very low Ka (or acid dissociation constant). Consequently, very little of the acid is dissociated into hydronium ions and conjugate base as seen in the following reaction.

> H-A + H<sub>2</sub>O &lt;———(1%)———> H<sub>3</sub>O<sup>+</sup> + A<sup>-</sup>

Because so little hydronium ion is present, the pH of the solution cannot be calculated directly from the initial concentration of acid. Though the equation for calculating the pH is identical to that for a strong base,

> pH = - log [H<sub>3</sub>O<sup>+</sup>]

we must first calculate the amount of hydronium ion present in the solution. This amount is dependent on the Ka of the acid. Each acid has a unique Ka, which indicates the probability that an acid molecule will react with a water molecule to form a hydronium ion and an anion. The anion in such a reaction is known as the conjugate base of the original acid. Though the term conjugate base may seem strange, it makes sense if you look at the chemical equation above. There A- is the conjugate base of H-A because it can take a proton from the hydronium ion to generate the original acid.

This model models the composition of a weakly acidic solution and allows the user to observe how altering the species in solution affects the pH. In the model, the dissociation constant Ka, is much higher than the real Ka for an acid. In fact, the model uses the following formula to convert he real-world Ka into a simulated Ka for the model:

> Ka for MODEL = 11 - pKa (pKa = - log (real-world Ka)

## HOW TO USE IT

Decide how much acid should be present at the start of the simulation with the VOL-ACID slider and press SETUP. Turtles will distribute randomly across the world. BLUE turtles represent water molecules, GREEN turtles represent hydronium ions, YELLOW turtles are acid molecules, and finally ORANGE turtles are conjugate base molecules. A set amount of water molecules is added each time to the solution. In this model we are using the Ka of acetic, which means that approximately 1% of the original acid molecules are dissociated into 1 conjugate base molecule and 1 hydronium molecule.

Press GO. The turtles will move randomly across the world and the pH of the solution will by plotted over time on the pH Curve and displayed in the pH monitor.

Observe the effect of adding RED base molecules to the solution by setting the volume of base with the VOL-BASE (mL) slider and pressing ADD-BASE.

At any time while go is depressed, RECORD-PH can be pressed to plot the pH versus the amount of base added on the Titration Curve.

## THINGS TO NOTICE

Observe how the pH curve changes over time with the addition of base. How is this curve different from that seen in the strong acid model? Plot a titration curve and compare it with a strong acid.

Does the value of VOL-BASE affect the titration curve in any way? Is this different from the relationship between strong acids and strong bases?

Why are the base molecules not reacting with the water molecules? Does it matter?

## THINGS TO TRY

Notice that the pH of the solution seems to be an average rather than a constant. Can you explain this?

Start the model with various amounts of acid. How does this affect the titration curve? Is there any difference in the endpoint of the reaction?

Keep adding base until the pH rises to about 10. Notice the breeds of turtles present. How does the pH depend on the molecules present?

Notice that in our models acid molecules react with base molecules based on the value of Ka. Make a slider so that you can alter this value. Observe how the system changes with regard to this variable. Is your pH between 0-14?

Note that we have established a constant for Ka in the procedures window. You must remove that constant when you replace it with a slider of the same name.

## EXTENDING THE MODEL

Notice that the code requires hydroxide molecules to first react with hydronium molecules on a patch before they react with acid molecules. This is because hydroxide and hydronium react much more rapidly than hydroxide does with a weak acid. Reverse the code and observe the effect on the system.

Alter the code so that base turtles only react with hydronium molecules. What effect is observed? What additional changes do you need to make so that the pH continues to rise with the addition of base?

Increase the dissociation percentage so that more hydronium ions are generated at setup. What does this do to the pH? Can you predict what the Ka of a strong acid might be?

Try using the various pKa values listed below to determine the endpoint of each weak acid.

<table border>
<tr><th>weak acid<th>Ka<th>pKa<th>Ka for model
<tr><td>HCN<td>1.26 x 10<sup>-9</sup><td>9.1<td>1.9
<tr><td>HOAc<td>1.8 x 10<sup>-5</sup><td>4.8<td>6.2
<tr><td>H<sub>2</sub>CO<sub>3</sub><td>3.16 x 10<sup>-7</sup><td>6.5<td>4.5
<tr><td>HCO<sub>2</sub>H<td>2.0 x 10<sup>-4</sup><td>3.7<td>7.3
</table>

## NETLOGO FEATURES

Notice the large number of breeds used in this model. Because of the complex interactions between molecules in a weakly acidic solution, it is necessary to separate turtles according to their identity. This allows each breed of turtle to follow unique instructions, which allows the model to simulate the complexity of molecular interactions.

## CREDITS AND REFERENCES

Thanks to Mike Stieff for his work on this model.
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

molecule1
true
0
Circle -7500403 true true 80 80 142
Circle -1 true false 52 195 61

molecule2
true
0
Circle -7500403 true true 78 79 146
Circle -1 true false 40 41 69
Circle -1 true false 194 42 71

molecule3
true
0
Circle -7500403 true true 92 92 118
Circle -1 true false 120 32 60
Circle -1 true false 58 183 60
Circle -1 true false 185 183 60

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

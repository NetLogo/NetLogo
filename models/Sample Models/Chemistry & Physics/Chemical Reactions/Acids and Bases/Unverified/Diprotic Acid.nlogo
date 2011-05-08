breed [acids acid]           ;; acid molecules (yellow)
breed [hydroxides hydroxide] ;; base molecules (red)
breed [hydroniums hydronium]  ;; hydronium ions (green)
breed [waters water]         ;; water molecules (blue)
breed [con-bases con-base]   ;;first conjugate base molecules (magenta)
breed [con-base2s con-base2] ;;second conjugate base molecules (cyan)

globals
  [
   pH pOH                                         ;;variables for calculating pH
   mmolH mmolOH mmolA mmolA2 mmolHA               ;;variables for calculating amount of molecules
   Hconc OHconc Aconc HAconc A2conc volume        ;;variables for calculating concentrations
  ]

turtles-own [partner]                                  ;;variable to control reactions

to setup
  clear-all
  set-default-shape waters "water"
  set-default-shape hydroniums "hydronium"
  set-default-shape hydroxides "hydroxide"
  set-default-shape acids "acid"
  set-default-shape con-bases "conbase"
  set-default-shape con-base2s "conbase2"
  ;;creates variable amount of acid
  create-acids starting-acid
  ;;initializes the amount of dissociated acid
  ask acids
    [
     if ((random 100) > (99 - Ka1))
      [
       hatch-hydroniums 1 [fd 3]
       set breed con-bases
      ]
    ]
  ;;initializes the amount of dissociated conjugate base
  ask con-bases
    [
     if ((random 100) > (99 - Ka2))
      [
       hatch-hydroniums 1 [fd 3]
       set breed con-base2s
      ]
    ]
  if see-starting-water?
    [ create-waters (100 - (count hydroniums)) ]
  calculate-ions    ;;used to calculate pH
  set-colors    ;;assigns colors
  set mmolOH 0
  ;; calculates overall number of molecules (total volume)
  set volume count turtles
  ;;randomizes position and headings
  ask turtles [setxy random-xcor random-ycor]
  reset-ticks
  plot-molecules
  plot-ph
end

to go ;;main turtle procedure
  collide
  set-colors
  ask turtles [fd 1]
  calculate-ions
  tick
  if (ticks > 30) [plot-pH plot-molecules]
end


to collide ;;turtle procedure - tells molecules how to find other molecules
  ;; hydroxide molecules will react with all proton donor molecules in the model
  if (count hydroxides > 0)
    [
     ask hydroxides
       [
        if any? hydroniums-here [set partner one-of hydroniums-here react-bases]
        if any? acids-here [set partner one-of acids-here react-bases]
        if any? con-bases-here [set partner one-of con-bases-here react-bases]
       ]
    ]

  ;; acid molecules always have a chance to dissociate to hydroniums depending on their
  if (count acids > 0)
    [
     ask acids
       [
        if ((random 100) > (99 - Ka1))
          [if any? waters-here [set partner one-of waters-here react-acids]]
       ]
    ]

  ;; conjugate base molecules always have a chance to dissociate to hydroniums depending on their Ka
  if count con-bases > 0
    [
      ask con-bases
        [
         if ((random 100) > (99 - Ka2))
           [if any? waters-here [set partner one-of waters-here react-bases]]
        ]
    ]

  ;; hydroniums molecules always have a chance to recombine with conjugate base to form acid
  if count hydroniums > 0
    [
     ask hydroniums
          [
           if ((random 100) > Ka1)
             [if any? con-bases-here [set partner one-of con-bases-here react-acids]]
          ]
    ]

  ;; hydroniums molecules can also recombine with the second conjugate base to form conjugate base
  if count hydroniums > 0
    [
     ask hydroniums
            [
             if ((random 100) > Ka2)
               [if any? con-base2s-here [ set partner one-of con-base2s-here react-acids ]]
            ]
    ]
end

to set-colors ;;turtle procedure to assign graphics
  ask waters [set color blue]
  ask hydroniums [set color green]
  ask acids [set color yellow]
  ask con-bases [set color magenta]
  ask con-base2s [set color cyan]
  ask hydroxides [set color red]
end

to react-bases ;;turtle procedure that tells base molecules how to react

;; all hydroxide molecules will instantly react with hydroniums molecules
  if [breed] of partner = hydroniums
    [set breed waters
     ask partner [ set breed waters ]]

;; hydroxide molecules have a high probability of reacting with acid molecules
  if [breed] of partner = acids
    [if ((random 100) > (99 - Ka1)) [set breed waters
                                     ask partner [set breed con-bases]]]

;; hydroxide molecules have a low probablity of reacting with conjugate base
  if [breed] of partner = con-bases
    [if ((random 100) > (99 - Ka2)) [set breed waters
                                     ask partner [set breed con-base2s]]]

;; conjugate base molecules react with water to form hydroniums and con-base2
  if [breed] of partner = waters
    [set breed con-base2s
     ask partner [set breed hydroniums]]
end


to react-acids ;;turtle procedure that tells acid turtles how to react

;;acid molecules react with water to form hydroniums and con-base
if [breed] of partner = waters
  [set breed con-bases
   ask partner [set breed hydroniums]]

;;hydroniums molecules react with con-base to form acid and water
if [breed] of partner = con-bases
  [set breed waters
   rt random 360 fd 2
   ask partner [set breed acids]]

;;hydroniums molecules react with con-base2 to form con-base and water
if [breed] of partner = con-base2s
  [set breed waters rt random 360 fd 2
   ask partner [set breed con-bases]]
end


;; calculates variables for determining the pH
to calculate-ions
  set mmolH count hydroniums
  set mmolOH count hydroxides
  set mmolA count con-bases
  set mmolHA count acids
  set mmolA2 count con-base2s
  set volume count turtles
  set Hconc (mmolH / volume)
  set OHconc (mmolOH / volume)
  set Aconc (mmolA / volume)
  set A2conc (mmolA2 / volume)
  set HAconc (mmolHA / volume)
  calculate-pH
end


;; calculates the pH from the amount of the various ions in solution;
;; note that for simplicity the calculations don't take the true molar
;; concentration of water into account; instead we simply divide by
;; a factor of 1000 to bring the numbers into a reasonable range.
;; Above the endpoint, the pH is calculated as a strong acid.
to calculate-pH
  let concH 0
  let concOH 0
  set concH (count hydroniums / volume)
  set concOH (count hydroxides / volume)
  ifelse (concH = concOH)
    [ set pH 7 ]
    [ ifelse (concH > concOH)
      [ set pH (- log (concH / 1000) 10) ]
      [ set pOH (- log (concOH / 1000) 10)
        set pH 14 - pOH ] ]
end

;; adds more base to the system
to add-base
  create-hydroxides base-added
    [
     fd 1
    ]
  set-colors
end

;; plotting procedures

to record-pH
  set-current-plot "Titration Curve"
  set-plot-pen-interval base-added
  ; before next plotting, move along x-axis by magnitude of amount added
  plot pH
end

to plot-pH
  set-current-plot "pH Curve"
  plot pH
end

to plot-molecules
  set-current-plot "Molecule Counts"
  set-current-plot-pen "con-base"
  plot count con-bases
  set-current-plot-pen "acid"
  plot count acids
  set-current-plot-pen "hydroxide"
  plot count hydroxides
  set-current-plot-pen "hydronium"
  plot count hydroniums
  set-current-plot-pen "water"
  plot count waters
end
@#$#@#$#@
GRAPHICS-WINDOW
266
10
651
416
12
12
15.0
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

BUTTON
6
11
110
44
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
124
11
252
44
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
7
233
71
266
record pH
record-pH
NIL
1
T
OBSERVER
NIL
R
NIL
NIL
1

BUTTON
6
103
92
136
add base
add-base
NIL
1
T
OBSERVER
NIL
B
NIL
NIL
1

SLIDER
6
54
253
87
starting-acid
starting-acid
0
100
50
1
1
molecules
HORIZONTAL

SLIDER
100
103
253
136
base-added
base-added
0
100
15
1
1
molecules
HORIZONTAL

PLOT
9
465
253
625
pH Curve
Time
pH
0.0
100.0
0.0
14.0
true
true
"" ""
PENS
"ph" 1.0 0 -2674135 true "" ""

PLOT
8
269
253
438
Titration Curve
Vol base
pH
0.0
100.0
0.0
14.0
true
true
"" ""
PENS
"ph" 1.0 0 -13345367 true "" ""

PLOT
370
431
765
627
Molecule Counts
time
#
0.0
100.0
0.0
100.0
true
true
"" ""
PENS
"acid" 1.0 0 -1184463 true "" ""
"hydronium" 1.0 0 -10899396 true "" ""
"con-base" 1.0 0 -5825686 true "" ""
"hydroxide" 1.0 0 -2674135 true "" ""
"water" 1.0 0 -13345367 true "" ""

SWITCH
75
233
253
266
see-starting-water?
see-starting-water?
0
1
-1000

SLIDER
6
151
253
184
Ka1
Ka1
0
10
9
1
1
NIL
HORIZONTAL

SLIDER
7
188
253
221
Ka2
Ka2
0
10
1
1
1
NIL
HORIZONTAL

MONITOR
257
531
350
576
# hydronium
count hydroniums
0
1
11

MONITOR
257
571
350
616
pH
pH
1
1
11

MONITOR
257
481
350
526
# hydroxide
count hydroxides
0
1
11

@#$#@#$#@
## WHAT IS IT?

This is the fourth model of the Acid-Base subsection of the Connected Chemistry models. It is best explored after the Strong Acid, Weak Acid, and Buffer models. In this model, we have yet another variant on determining the pH of a solution.  This model depicts a diprotic acid, or an acid which can donate two atoms of hydrogen to a base.

## HOW IT WORKS

The value of pH, like many other chemical measurements, emerges from the interactions and relative ratios of the composite molecules within a solution. Specifically, pH is a measurement of the amount of hydronium ions (H+ or H3O+) that are present in a solution. Hydronium ions are generated when an acid molecule donates a proton to a water molecule. Bases have the opposite effect on water -- they take a hydrogen atom from a water molecule and generate hydroxide ions (OH-). The chemical reaction is shown below (for more detailed explanations about pH and acid-base reactions, please check the three aforementioned models).

                  Ka1      +      -  Ka2      +    2-
    H A  +  2H O  -->   H O  + H-A   -->  2H O  + A
     2        2          3                  3

We can see that the first proton is donated to water to make a hydronium ion. After the initial acid is consumed, the second proton is donated to form a second molecule of hydronium ion. It is important to note that the Ka of the first proton is much greater than the second Ka. This is because the loss of the first proton generates a negatively charged anion. It is very difficult for bases which have a negative charge or a high electron density to come in close proximity to this anion and pull off the second proton. Because of this, the first proton is most often consumed before the second proton can be donated. The pH for the reaction is determined using the Henderson-Hasselbach equation in two separate instances. While the original acid (H2A) is present, the pH is determined by:

> pH = pK1 + log ([H-A<sup>-</sup>] / [H<sub>2</sub>A])

Once the weak acid is depleted, the pH is then determined by:

> pH = pK2 + log ([A<sup>2-</sup>] / [H-A<sup>-</sup>])

The model uses a short-cut equation to approximate the pH of the solution. The equation, which can be used for each pK value of a polyprotic acid, transforms the above two equations into the following, respectively.

> pH = 0.5 * (pK1 + log [HA])
> pH = 0.5 * (pK2 + log [A])

## HOW TO USE IT

Decide how much acid should be present at the start of the simulation with the STARTING-ACID slider and press SETUP. Turtles will distribute randomly across the view. BLUE turtles represent water molecules, GREEN turtles represent hydronium ions, YELLOW turtles are acid molecules, and finally MAGENTA turtles are conjugate base molecules. A set amount of water molecules is added each time to the solution. In this model we are using the Ka of acetic acid, which means that approximately 1% of the original acid turtles are dissociated into one conjugate base molecule and one hydronium molecule.

Press GO. The turtles will move randomly across the view and the pH of the solution will be plotted over time on the pH Curve and displayed in the pH monitor. Also, you will see the counts of all the molecules present in the solution in the Molecule Counts plot

Observe the effect of adding base to the solution by setting the volume of base with the BASE-ADDED slider and pressing ADD BASE.

When the pH remains at a steady value, press RECORD-PH, which will plot the pH versus the amount of bases added on the titration curve.

## THINGS TO NOTICE

Run a titration and observe the curve. Is there anything unique about its shape?

Look for light blue (cyan) anions in the solution. How much base does it take before you start seeing them? Is this surprising?

Pay attention to how the molecules interact. Which molecules react with each other?

## THINGS TO TRY

Compare the titration curve of the diprotic acid with that of a buffer. Do you see any similarities? How can you alter the code to test if this diprotic acid acts as a buffer?

Increase the dissociation percentage so that more hydronium ions are generated at setup. What does this do to the pH? Is the Henderson-Hasselbach equation still valid with a large Ka?

Notice that the code requires hydroxide molecules to first react with hydronium molecules on a patch before they react with acid molecules. Can you explain why this is? Reverse the code and observe the effect on the system.

Can you alter the pH of the solution without adding base to the solution?

## EXTENDING THE MODEL

Alter the code so that base turtles only react with hydronium molecules. What effect is observed? What additional changes do you need to make so that the pH continues to rise with the addition of base?

Substitute the short-cut equation for calculating pH with the full equation. Are the values similar?

Try substituting the various pKa values below into the Henderson-Hasselbach equation and observe their effect on the titration curve. What affect does this have on the pH?

<table border>
<tr><th>weak acid<th>pK1<th>pK2
<tr><td>carbonic<td>6.5<td>10.2
<tr><td>oxalic<td>1.27<td>4.27
<tr><td>glycine<td>2.34<td>9.60
<tr><td>maleic<td>2.00<td>6.20
</table>

## RELATED MODELS

Strong Acid
Weak Acid
Buffer

## NETLOGO FEATURES

Notice that in the `calculate-pH` procedure the model makes use of the `count` primitive to convert the number of turtles in the world into concentrations that are often used in the chemistry laboratory.

## CREDITS AND REFERENCES

Thanks to Mike Stieff for his work on this model.
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

acid
true
0
Circle -1184463 true false 73 74 156
Circle -1 true false 48 199 55
Circle -1 true false 197 198 58

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

conbase
true
0
Circle -5825686 true false 78 79 146
Circle -1 true false 42 194 66

conbase2
true
0
Circle -11221820 true false 93 94 116
Circle -1 true false 62 185 57

cow
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

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

hydronium
true
0
Circle -10899396 true false 91 92 118
Circle -1 true false 66 187 50
Circle -1 true false 186 186 52
Circle -1 true false 127 43 49

hydroxide
true
0
Circle -2674135 true false 80 80 142
Circle -1 true false 63 199 55

leaf
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195

line
true
0
Line -7500403 true 150 0 150 300

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

water
true
0
Circle -13345367 true false 78 79 146
Circle -1 true false 40 41 69
Circle -1 true false 194 42 71

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

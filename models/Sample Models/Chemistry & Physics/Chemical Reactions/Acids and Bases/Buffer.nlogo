breed [waters water]             ;; water molecules
breed [hydroniums hydronium]     ;; hydronium ions (green)
breed [hydroxides hydroxide]     ;; base molecules (red)
breed [acids acid]               ;; parent acid molecules (yellow)
breed [conj-bases conj-base]     ;; conjugate base molecules of parent acid (orange)

globals [
  pH
]

to setup
  clear-all
  set-default-shape waters "molecule2"
  set-default-shape hydroniums "molecule3"
  set-default-shape hydroxides "molecule1"
  set-default-shape acids "molecule2"
  set-default-shape conj-bases "molecule1"
  create-acids starting-acid  ;; creates variable amount of acid
    [ set color yellow ]
  ask acids [
    ;; initializes amount of dissociated acid
    if random-float 100 > 98
      [ dissociate ] ]
  if see-starting-water?
    [ create-waters 100    ;; creates constant volume of water
        [ set color blue ] ]
  create-conj-bases starting-conj-base  ;; creates variable amount of conjugate base
    [ set color orange ]
  ask turtles                   ;; randomize position and heading of turtles
    [ setxy random-xcor random-ycor ]
  calculate-pH
  reset-ticks
end

;; dissociate an acid molecule into hydronium and conj-base
;; this should only be called by turtles that are acids
to dissociate  ;; turtle procedure
  hatch-hydroniums 1 [ set color green ]
  set breed conj-bases
  set color orange
end

to go
  ask hydroxides [ react-hydroxide ]
  ask hydroniums [ react-hydronium ]
  ;; move turtles randomly around the world
  ask turtles
    [ fd 1
      rt random 10
      lt random 10 ]
  tick
  calculate-pH
end

to react-hydroxide  ;; hydroxide procedure
  let partner one-of hydroniums-here
  ifelse partner != nobody
    [ react partner ]
    [ set partner one-of acids-here
      if partner != nobody
        [ react partner ] ]
end

to react-hydronium  ;; hydronium procedure
  let partner one-of hydroxides-here
  ifelse partner != nobody
    [ react partner ]
    [ set partner one-of conj-bases-here
      if partner != nobody
        [ react partner ] ]
end

to react [ partner ]  ;; turtle procedure
  ;; all hydroxide molecules will instantly react with
  ;; hydronium molecules and form water
  ifelse ([breed] of partner) = hydroniums or ([breed] of partner) = hydroxides
    [ set breed waters
      set color blue
      ask partner
        [ set breed waters
          set color blue ] ]
    [ ;; hydroxide molecules have a high probability of
      ;; reacting with acid molecules
      ifelse ([breed] of partner) = acids
        [ if random-float 100 > 80
          [ set breed waters
            set color blue
            ask partner
              [ set breed conj-bases
                set color orange ] ] ]
        ;; hydronium ions will react with conjugate base to form acid
        [ if ([breed] of partner) = conj-bases
          [ if random-float 100 > 80
            [ set breed waters
              set color blue
              ask partner
                [ set breed acids
                  set color yellow ] ] ] ] ]
end

;; calculates the pH from the amount of the various ions in solution;
;; note that for simplicity the calculations don't take the true molar
;; concentration of water into account; instead we simply divide by
;; a factor of 1000 to bring the numbers into a reasonable range
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

;; add more base to the system
to add-base
  create-hydroxides added-base
    [ set color red
      fd 1 ]
end

;; add more acid to the system
to add-acid
  create-hydroniums added-acid
    [ set color green
      fd 1 ]
end
@#$#@#$#@
GRAPHICS-WINDOW
279
10
664
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

SLIDER
17
116
260
149
starting-conj-base
starting-conj-base
1.0
200.0
100
1.0
1
molecules
HORIZONTAL

SLIDER
84
158
277
191
added-acid
added-acid
1.0
100.0
15
1.0
1
molecules
HORIZONTAL

BUTTON
20
37
121
70
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
132
37
262
70
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
158
79
191
add acid
add-acid
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
7
201
79
234
add base
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

PLOT
7
287
264
483
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
"default" 1.0 0 -2674135 true "" "plot pH"

SLIDER
84
201
277
234
added-base
added-base
1.0
100.0
15
1.0
1
molecules
HORIZONTAL

PLOT
279
427
665
646
Molecule Counts
time
#
0.0
100.0
0.0
10.0
true
true
"" ""
PENS
"hydronium" 1.0 0 -10899396 true "" "plot count hydroniums"
"conj. base" 1.0 0 -955883 true "" "plot count conj-bases"
"hydroxide" 1.0 0 -2674135 true "" "plot count hydroxides"
"acid" 1.0 0 -2500352 true "" "plot count acids"
"water" 1.0 0 -13345367 true "" "plot count waters"

SLIDER
17
80
261
113
starting-acid
starting-acid
1.0
200.0
100
1.0
1
molecules
HORIZONTAL

SWITCH
42
246
216
279
see-starting-water?
see-starting-water?
1
1
-1000

MONITOR
37
556
119
601
pH
pH
2
1
11

MONITOR
37
503
119
548
# hydronium
count hydroniums
0
1
11

MONITOR
133
502
212
547
# hydroxide
count hydroxides
0
1
11

@#$#@#$#@
## WHAT IS IT?

This model demonstrates the behavior of a buffered solution.  A buffer is a solution that resists change in pH when either acid or base are added into it, within limits.  It is best viewed as the third model in the ACID-BASE package.

Chemists and biologists use the properties of acids and bases to create buffer solutions.  It is often desirable to keep the acidity, or hydronium ion concentration of a solution, as nearly constant as possible.  Examples of such situation would be: measuring the velocity of a reaction, selectively dissolving certain salts, or studying the growth of bacteria or plants.   In our body, the blood has a buffering capacity, keeping the pH at 7.35.  If the pH goes above 7.7 (alkalosis) or below 7.0 (acidosis), the results are fatal.

## HOW IT WORKS

To accomplish this feat, buffers depend on the presence of both a weak acid and its conjugate base. With both of these species in the solution, additional acids and bases are neutralized according to the following chemical equations. H-A denotes the weak acid and A- denotes its conjugate base.

> H-A + OH<sup>-</sup> —> H<sub>2</sub>O + A<sup>-</sup>
> A<sup>-</sup> + H<sub>3</sub>O<sup>+</sup> —> H<sub>2</sub>O + H-A

The pH of a buffer is determined in the same manner that the pH of a weak acid is determined - by counting the number of hydronium and hydroxide ions and calculating their ratio with total number of ions and molecules in the solution.

When there are more hydroniums,  pH = - log ( hydronium concentration )

When there are more hydroxides,  pH = 14 - pOH = 14 - log ( hydroxide concentration )

Buffers are effective only within their unique buffering range. This range is determined by the concentrations of the weak acid and its conjugate base.  Outside of its buffering range, the solution behaves as a strong acid or base.

## HOW TO USE IT

Decide how many acid molecules should be present at the start of the simulation with the STARTING-ACID slider. Set the number of conjugate base molecules with the STARTING-CONJ-BASE slider.

Press SETUP.  The turtles will distribute randomly across the world.

YELLOW turtles are acid molecules (HA)  
ORANGE turtles are conjugate base molecules (A-)  
GREEN turtles represent hydronium ions (H30+)  
RED turtles are hydroxide molecules (OH-)  
BLUE turtles represent water molecules (H20)

In this model we are assuming that 2% of the original acid molecules are dissociated into 2 conjugate base molecules and 2 hydronium molecules.  This is true only before the molecules start interacting between themselves.

Press GO. The molecules will move randomly across the world.

When two turtles occupy the same patch, the following rules apply:  
1.  When a weak acid and a water molecule collide, the acid molecule dissociates into its conjugate base and the water molecule transforms to a hydronium ion.  
2.  When hydroxide and hydronium ions collide, they always form two water molecules.  
3.  When a weak acid and hydroxide collide, they have a high probability of transforming into a conjugate base and a water molecule.  
4.  When hydronium and conjugate base collide, they have a high probability of transforming into a weak acid molecule and a water molecule.

To observe the effect of adding base or acid to the solution, set the number of acid molecules you want to add with the ADDED-ACID slider and press ADD-ACID (H30+)  
Do the same for adding base with the ADDED-BASE slider and ADD-BASE button.

A number of plots and monitors can be observed:  
The pH of the solution is plotted over time on the PH plot, and at each time tick on the PH monitor.  pH is calculated using the ratio of the number of hydronium and hydroxide molecules to the total number of turtles.  This is different from the chemistry calculation that relates this number to solution volume.  
You can see the number of hydroniums and hydroxides in the solution in their monitors  (# HYDRONIUMS, # HYDROXIDES).  
You may follow the number of molecules of each species over time in the MOLECULE COUNTS plot.

You may choose to see the initial water molecules or not with the SEE-STARTING-WATER? switch.

## THINGS TO NOTICE

After you press SETUP, the buffer solution is not yet in equilibrium.  After the model starts running, the molecules react until the system is equilibrated, and the pH doesn't change anymore.

Observe the pH curve.  How are the numbers of hydronium and hydroxide molecules in the monitors below related to the pH?

Examine the shape of the pH curve. Notice how the pH changes with respect to the amount of base or acid added. Does the buffer resist change in pH?

Observe the number of molecules in the plot.   When acid or base is added to the solution, they quickly disappear as they react. Can you determine which molecules react with each other?  What is the relationship between the two plots: pH and number of molecules?

What happens when large amounts of base or acid are added to the system?  Is it the same as adding small amounts?  Does the pH curve reflect this?

## THINGS TO TRY

Add a large amount of acid or base to the solution and observe the effect.  Why does the pH change dramatically outside of the buffering range?  Would it be useful to add large amounts of base all at once to a solution in the laboratory if you were trying to adjust the pH?

Can you relate the idea of a "buffering range" to the molecules and their behavior in the model?

Try running the model with different amounts of acid or conjugate base.  The buffering range of the solution should shift.  How is the shift related to the changes you made?  Does changing the proportion between the number of weak acid and conjugate base molecules have an effect on the buffering capacity?

How does changing the amount of buffer molecules influence the buffering capacity?

Try pressing SETUP a number of times with the same initial settings, and observe the number of hydroniums.  Is the number always the same?  Press GO and watch the change in pH.  Why is the same equilibrium reached in each case?

## EXTENDING THE MODEL

Increase the amount of hydronium originally generated upon SETUP by increasing the chance of dissociation in the procedures.  What effect does this have on the initial pH?

Add a button and procedure to add more conjugate base to the reaction.  How could this help a chemist who is trying to keep the pH of a solution at a constant value?

Add a procedure that allows you to plot a titration curve for a buffer.  Is it similar to any other titration curves you have seen?  What additional information can you learn from the titration curve?

Additional interactions could take place in a solution in addition to those stated in the rules.  These were omitted because their probability of occurring is low.  Try including one of these rules to the procedure.  For example, two water molecules could transform into a hydronium and hydroxide ions.  Does it make a change?

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

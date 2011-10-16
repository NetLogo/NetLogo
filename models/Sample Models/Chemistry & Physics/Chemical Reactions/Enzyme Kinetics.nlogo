breed [ enzymes enzyme]      ;; red turtles that bind with and catalyze substrate
breed [ substrates substrate ]   ;; green turtles that bind with enzyme
breed [ inhibitors inhibitor ]   ;; yellow turtle that binds to enzyme, but does not react
breed [ products product ]     ;; blue turtle generated from enzyme catalysis


turtles-own [
  partner      ;; holds the turtle this turtle is complexed with,
               ;; or nobody if not complexed
]

globals [
  substrate-added ;; keeps track of how much substrate has been added
  v               ;; rate of complex formation at each time step
]

to startup
  setup-mm-plot
end

;; observer procedure to set up model
to setup
  clear-turtles                    ;; clears view -- don't use clear-all so MM plot doesn't clear
  set substrate-added 0
  set v 0
  add enzymes 150                   ;; starts with constant number of enzymes
  add substrates volume             ;; add substrate based on slider
  reset-ticks
end

;; observer procedure to add molecules to reaction
to add [kind amount]
  crt amount
    [ set breed kind
      setxy random-xcor random-ycor
      set partner nobody
      setshape ]
  if kind = substrates
    [ set substrate-added substrate-added + amount ]
end

;; procedure that assigns a specific shape to a turtle, and shows
;; or hides it, depending on its state
to setshape
  ifelse breed = enzymes
    [ set color red
      ifelse partner = nobody
        [ set shape "enzyme" ]
        [ ifelse ([breed] of (partner) = substrates)
            [ set shape "complex" ]
            [ set shape "inhib complex" ] ] ]
    [ ifelse breed = substrates
        [ set color green
          set shape "substrate"
          set hidden? (partner != nobody) ]
        [ ifelse breed = inhibitors
            [ set color yellow
              set shape "inhibitor"
              set hidden? (partner != nobody) ]
            [ if breed = products
                [ set color blue
                  set shape "substrate"
                  set hidden? false ] ] ] ]
end

;; main procedure
to go
  if pause? and (ticks >= 30)
    [ stop ]
  ask turtles [ move ]                ;; only non-complexed turtles will move
  ask enzymes [ form-complex ]         ;; enzyme may form complexes with substrate or inhibitor
  ask substrates [ react-forward ]     ;; complexed substrate may turn into product
  ask enzymes [ dissociate ]           ;; or complexes may just split apart
  calculate-velocity                  ;; calculate V for use in the Michaelis-Menten curve
  tick
end

to move  ;; turtle procedure
  if partner = nobody
    [ fd 1
      rt random-float 360 ]
end

;; An enzyme forms a complex by colliding on a patch with a substrate
;; or inhibitor.  If it collides with an inhibitor, it always forms
;; a complex.  If it collides with a substrate, Kc is its percent chance
;; of forming a complex.
to form-complex  ;; enzyme procedure
  if partner != nobody [ stop ]
  set partner one-of (other turtles-here with [partner = nobody])
  if partner = nobody [ stop ]
  if [partner] of partner != nobody [ set partner nobody stop ]  ;; just in case two enzymes grab the same partner
  ifelse ((([breed] of partner) = substrates) and ((random-float 100) < Kc))
     or (([breed] of partner) = inhibitors)
    [ ask partner [ set partner myself ]
      setshape
      ask partner [ setshape ] ]
    [ set partner nobody ]
end

;; substrate procedure that controls the rate at which complexed substrates
;; are converted into products and released from the complex
to react-forward
  if (partner != nobody) and (random-float 1000 < Kr)
    [ set breed products
      ask partner [ set partner nobody ]
      let old-partner partner
      set partner nobody
      setshape
      ask old-partner [ setshape ] ]
end

;; enzyme procedure that controls the rate at which complexed turtles break apart
to dissociate
  if partner != nobody
    [ if ([breed] of partner = substrates) and (random-float 1000 < Kd)
      [ ask partner [ set partner nobody ]
        let old-partner partner
        set partner nobody
        setshape
        ask old-partner [ setshape ] ] ]
end

to calculate-velocity
  let initial-conc substrate-added
  let current-conc count substrates with [partner = nobody]
  if ticks > 0
    [ set v (initial-conc - current-conc) / ticks ]
end

;;; plotting procedures

to setup-mm-plot
  set-current-plot "Michaelis-Menten Curve"
  clear-plot
end

;; allows user to plot the concentration versus the velocity on the Michaelis-Menten Curve
to do-mm-plot
  set-current-plot "Michaelis-Menten Curve"
  plotxy substrate-added v
end
@#$#@#$#@
GRAPHICS-WINDOW
289
10
649
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

BUTTON
4
41
68
74
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
69
41
130
74
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

SLIDER
133
146
287
179
Kr
Kr
0.0
100.0
0
1.0
1
NIL
HORIZONTAL

SLIDER
133
76
287
109
Kc
Kc
0.0
100.0
80
1.0
1
NIL
HORIZONTAL

SLIDER
133
111
287
144
Kd
Kd
0.0
100.0
62
1.0
1
NIL
HORIZONTAL

MONITOR
289
393
355
438
Velocity
v
3
1
11

BUTTON
20
146
120
179
add inhibitor
add inhibitors volume
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
20
111
120
144
add substrate
add substrates volume
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
3
181
287
359
Concentrations
time
C
0.0
50.0
0.0
50.0
true
true
"" ""
PENS
"Substrate" 1.0 0 -10899396 true "" "plot count substrates with [partner = nobody]"
"Complex" 1.0 0 -2674135 true "" "plot count enzymes with [partner != nobody]"
"Product" 1.0 0 -13345367 true "" "plot count products"

PLOT
3
360
287
540
Michaelis-Menten Curve
Substrate Conc.
V
0.0
10.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 2 -16777216 true "" ""

SLIDER
133
41
287
74
volume
volume
0.0
1000.0
50
25.0
1
molecules
HORIZONTAL

BUTTON
289
512
364
545
clear MM
setup-MM-plot
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

SWITCH
289
444
390
477
pause?
pause?
1
1
-1000

BUTTON
289
478
390
511
Record V
do-MM-plot
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

This model demonstrates the kinetics of single-substrate enzyme-catalysis. The interactions between enzymes and substrates are often difficult to understand and the model allows users to visualize the complex reaction.

The standard equation for this reaction is shown below.

                      Kc          Kr
            E + S <=======> E-S ------> E + P
                      Kd

Here E represents Enzyme, S Substrate, E-S Enzyme-Substrate complex, and P product.  The rate constants are Kc for complex formation, Kd for complex dissociation, Kr for catalysis.  The first step in catalysis is the formation of the E-S complex.  This can consist of either covalent or non-covalent bonding.  The rates of complex formation and dissociation are very fast because they are determined by collision and separation of the molecules.  The next step is for the enzyme to catalyze the conversion of substrate to product.  This rate is much slower because the energy required for catalysis is much higher than that required for collision or separation.

The model demonstrates several important properties of enzyme kinetics.  Enzyme catalysis is often assumed to be controlled by the rate of complex formation and dissociation, because it occurs much faster than the rate of catalysis. Thus, the reaction becomes dependent on the ratio of Kc / Kd.  The efficiency of catalysis can be studied by observing catalytic behavior at different substrate concentrations.

By measuring the rate of complex formation at different substrate concentrations, a Michaelis-Menten Curve can be plotted.  Analysis of the plot provides biochemists with the maximum rate (Vmax) at which the reaction can proceed. As can be seen from the model, this plot is linear at low levels of substrate, and non-linear at higher levels of substrate.  By examining the model, the reasons for this relationship can be seen easily.

Enzyme catalysis can also be controlled using inhibitors. Inhibitors are molecules that are structurally similar to substrate molecules that can complex with the enzyme and interfere with the E-S complex formation.  Subsequently, the shape of the Michaelis-Menten Curve will be altered. The model demonstrates the effects of inhibitors on catalysis.

## HOW TO USE IT

Choose the values of Kc, Kd, and Kr with appropriate sliders:
- Kc controls the rate at which substrates (green) and enzymes (red) stick together so that catalysis can occur
- Kd controls the rate at which they come unstuck
- Kr controls the rate of the forward reaction by which an enzyme (red) converts a substrate (green) to a product (blue)

Having chosen appropriate values of the constants, press SETUP to clear the world and create a constant initial number of enzyme (red) molecules. Play with several different values to observe variable effects on complex formation and catalysis.

Press GO to start the simulation.  A constant amount of enzyme (red) will be generated.  The concentrations of substrate, complex, and product are plotted in the CONCENTRATIONS window.

Experiment with using the ADD-SUBSTRATE and ADD-INHIBITOR buttons to observe the effects of adding more molecules to the system manually as it runs.  The default setting for Kr is 0, which means that no product (blue) will be generated unless you change Kr to a non-zero value.

Note that when complexes form they stop moving.  This isn't intended to be physically realistic; it just makes the formation of complexes easier to see.  (This shouldn't affect the overall behavior of the model.)

To plot the Michaelis-Menten Curve for your reaction conditions, you will have to perform several runs at different concentrations in order to measure the velocity for each run. To do this, set the PAUSE? switch ON.  When this switch is on, the model automatically stops after 30 time ticks.  Begin your assay by setting the substrate volume to zero and running the simulation.  When it stops, press RECORD V and a point will be plotted on the Michaelis-Menten Curve. Run another simulation with a higher concentration of substrate by changing the VOLUME slider, then hitting SETUP followed by GO, followed by RECORD V once the model stops.  Continue for several values of substrate concentrations until a curve is generated. If you wish to start over hit CLEAR MM to reset the plot.

## THINGS TO NOTICE

Watch the rate at which the enzyme and substrate stick together. How does this affect the conversion of substrate into product? What would happen if Kd is very high and Kc is very low? If Kr were the same order of magnitude as Kd and Kc?

Watch the Michaelis-Menten Curve. Does it match up with the discussion of enzyme kinetics discussed above? Why does the plot initially slope upward, then flatten out?

Which variables can alter the magnitude of v?

How does the magnitude of Kd and Kr affect the smoothness of the Michaelis-Menten Curve?

## THINGS TO TRY

Run the simulation with VOLUME set to various amounts. How does this affect the curve?

If Kr is MUCH greater than Kd, what affect does this have on the reaction?  How important does complex formation become in this situation?

If Kc is MUCH less than Kd, what does this mean in the real-world? How are the enzyme and substrate related under these conditions?

What effect does adding inhibitor to the model have on the plot? Is Vmax affected?

## EXTENDING THE MODEL

What would happen if yellow inhibitor molecules could react to form a product? How would this affect the plot?

Inhibitors can be irreversible or reversible. That is, they can bind to an enzyme and never let go, or they can stick and fall off. Currently, the model simulates irreversible inhibitors. Modify the code so that the yellow molecules reversibly bind to the enzyme. How does this affect catalysis?

Often, the product of catalysis is an inhibitor of the enzyme. This is called a feedback mechanism. In this model, product cannot complex with enzyme. Modify the procedures so that the product is a reversible inhibitor. How does this affect catalysis with and without yellow inhibitor?

Include a slider that allows you to change the concentration of enzyme.  What affect does this have on the plot?  Vmax?  Look closely!

## NETLOGO FEATURES

It is a little difficult to ensure that a reactant never participates in two reactions simultaneously.  In the future, a primitive called GRAB may be added to NetLogo; then the code in the FORM-COMPLEX procedure wouldn't need to be quite so tricky.

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

complex
true
0
Polygon -2674135 true false 76 47 197 150 76 254 257 255 257 47
Polygon -10899396 true false 79 46 198 148 78 254

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

enzyme
true
0
Polygon -2674135 true false 76 47 197 150 76 254 257 255 257 47

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

inhib complex
true
0
Polygon -2674135 true false 76 47 197 150 76 254 257 255 257 47
Polygon -1184463 true false 77 48 198 151 78 253 0 253 0 46

inhibitor
true
0
Polygon -1184463 true false 197 151 60 45 1 45 1 255 60 255

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

substrate
true
5
Polygon -10899396 true true 76 47 197 151 75 256

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
NetLogo 5.0RC2
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

breed [reactants reactant]     ;; reactant molecules (NO2), shown as green
breed [products product]       ;; product molecules (N2O4), shown as red

globals [
  Kf                 ;; when temp-effect is on, replaces Kb
  Kr                 ;; when temp-effect is on, replaces Ku
  Keq                ;; equilibrium constant
  react-conc         ;; concentration of reactant
  prod-conc          ;; concentration of product
  black-patches      ;; agentset of all the patches inside the reaction container
]

to setup
  clear-all
  set-default-shape reactants "NO2"
  set-default-shape products "N2O4"
  draw-box                                 ;; draws the reaction container
  create-reactants 200                     ;; generates reactants
    [ set color green
      move-to one-of black-patches ]       ;; distribute reactants around the world
  update-concentrations
  reset-ticks
end

to update-concentrations
  set react-conc (count reactants / count black-patches) * 100
  set prod-conc (count products / count black-patches) * 100
end

to go
  ifelse temp-effect?
    ;; if temp-effect is ON, Kf  replaces Kb and Kr replaces Ku
    ;; Kf and Kr are calculated with the Arrhenius Equation, k = ln A - Ea / RT .
    ;; ln A, Ea, and R are all constants for a reaction. Below, the constants have simulated
    ;; values that were chosen to produce the same qualitative results as the real-world
    ;; reaction. Because the number of molecules in the model is so small, we use simulated
    ;; constants to keep the values of each variable in a useful range for the model.
    ;; The important thing to note is how each K value varies according to the
    ;; temperature of the reaction.
    [ set Kf (5 + (3000 /(8 * (temp + 273))))
      set Kr (15 - (8000 / (8 * (temp + 273)))) ]
    ;; if temp-effect is OFF, set Keq based on concentrations
    [ set Keq (prod-conc / (react-conc ^ 2)) * 100 ]
  ask turtles
    [ bounce                        ;; bounce off walls
      fd 1                          ;; wander aimlessly
      rt random 10
      lt random 10 ]
  ask turtles
    [ ifelse (breed = reactants)
        [ react-forward ]
        [ react-backward ]
    ]
  update-concentrations
  tick
end

;; see explanations of "react-forward" and "react-backward" in the info window

to react-forward  ;; turtle procedure
  let chance 0
  ifelse temp-effect?
    [ set chance Kf ]
    [ set chance Kb ]
  if (any? other reactants-here) and (random-float 10.0 < chance)
    [ ask one-of other reactants-here
        [ die ]
      set breed products
      set color red
    ]
end

to react-backward  ;; turtle procedure
  let chance Ku
  if temp-effect?
    [ set chance Kr ]
  if random-float 1000.0 < chance
    [ set breed reactants
      set color green
      hatch 1
        [ rt 180 ] ]
end

;; turtle procedure to bounce molecules off the yellow walls
to bounce
  let box-edge edge-size + 1  ;; setting this first makes the calculations a bit simpler
  ; check: hitting top or bottom wall?
  if (patch-at 0 box-edge = nobody     and ((heading > 270) or (heading < 90))) or
     (patch-at 0 (- box-edge) = nobody and ((heading > 90) and (heading < 270)))
    ; if so, reflect heading around y axis
    [ set heading (180 - heading) ]
  ; check: hitting left or right wall?
  if (patch-at box-edge 0 = nobody     and ((heading > 0) and (heading < 180))) or
     (patch-at (- box-edge) 0 = nobody and ((heading > 180)))
    ; if so, reflect heading around x axis
    [ set heading (- heading) ]
end

;;observer procedures to add more molecules to the model
to add-reactant
  create-reactants 20
    [ set color green
      move-to one-of black-patches ]
end

to add-product
  create-products 20
    [ set color red
      move-to one-of black-patches ]
end

to draw-box
  ask patches
    [ ifelse (pxcor > (max-pxcor - edge-size)) or (pxcor < (min-pxcor + edge-size)) or
             (pycor > (max-pycor - edge-size)) or (pycor < (min-pycor + edge-size))
        [ set pcolor yellow ]
        [ set pcolor black ] ]
  set black-patches patches with [pcolor = black]
  ask turtles
  [
    move-to one-of black-patches
    rt random-float 360
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
255
10
640
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
30.0

BUTTON
4
21
79
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
84
21
153
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

BUTTON
5
228
101
261
redraw box
draw-box
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
5
194
101
227
edge-size
edge-size
1
10
1
1
1
NIL
HORIZONTAL

SWITCH
116
194
251
227
temp-effect?
temp-effect?
1
1
-1000

SLIDER
3
86
160
119
Kb
Kb
0
10
10
0.1
1
NIL
HORIZONTAL

SLIDER
3
120
160
153
Ku
Ku
0
100
20
1
1
NIL
HORIZONTAL

BUTTON
169
120
252
153
add reactant
add-reactant
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
169
86
252
119
add product
add-product
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
116
228
251
261
temp
temp
-200
200
200
1
1
deg C
HORIZONTAL

MONITOR
13
269
122
314
Reactant Conc
react-conc
1
1
11

MONITOR
123
269
226
314
Product Conc
prod-conc
1
1
11

PLOT
4
319
247
474
Concentrations
time
# molecules
0.0
10.0
0.0
10.0
true
true
"" ""
PENS
"Reactant" 1.0 0 -10899396 true "" "plot react-conc"
"Product" 1.0 0 -2674135 true "" "plot prod-conc"

TEXTBOX
5
164
102
193
Volume (pause model to change)
11
0.0
0

TEXTBOX
165
68
255
86
Concentration
11
0.0
0

TEXTBOX
117
176
207
194
Temperature
11
0.0
0

TEXTBOX
4
68
161
86
Rates (when temp-effect off)
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

This model demonstrates the properties of LeChatelier's Principle. This chemical principle states that if a system that is at equilibrium is perturbed, the system will readjust to establish a new equilibrium. For example, if you add reactants to a reversible reaction that is at equilibrium, the system will shift to generate more products and establish a new equilibrium. The principle can also be described with chemical equations.

Below is a generic equation which depicts two molecules of reactant A combining to form one molecule or product B. The reaction is reversible, meaning that the one molecule of B can break down into two molecules of A.

                      Kb
            A + A <=======> B
                      Ku

An example of such a reaction would be dimerization of the gas nitrous oxide:

                      Kb
            2 NO  <=======> N O
                2     Ku     2 4

This reaction is an example of a complex reaction which consists of two elementary reactions.  The forward bimolecular reaction

                     Kb
            A + A --------> B

is characterized by the constant Kb and the reverse unimolecular reaction

                  Ku
            B ---------> A + A

The equilibrium rate constant for the entire reaction (Keq) is equal to [B] / [A] ^ 2. Each of the rate constants in the equations above has units of s^-1. They are empirically derived constants that when combined with the reaction concentrations tell you how fast the reaction proceeds according to the reaction rate law. The rate law ultimately tells you how many Molar units of a molecule react per second. For the reaction above the forward rate law is RATE = Kb[A]^2 and the reverse rate law is RATE = Ku[B].

Note that because we are simulating the reaction, the values of Kb and Ku in this reaction are not real-world values.  It would be necessary to use several differential equations to calculate the real values of Kb, Ku and Keq, however, several qualitative features of their relationships can be seen using this model. Reaction equilibrium is reached when a system reaches a steady-state. This is not to say that reactions have stopped occurring!  Microscopic changes in equilibrium still take place, but to our eyes and our measurements the system appears stable because the forward and reverse rates are equal.

The rate at which a reaction reaches equilibrium as well as the state of the equilibrium system both depend upon the rate constants, the temperature, the concentration of reactants and products and, when a gas is involved, the volume of the container. When a system has reached equilibrium, changes to any of the variables above result in a change in the system to establish a new equilibrium. This effect is predicted using LeChatelier's Principle. We can use our model to discover the role of each variable (temperature, volume, concentration and rate constant) in LeChatelier's Principle.

## HOW TO USE IT

To start off:

Choose the values of Kb and Ku with appropriate sliders:  
- Kb controls the rate of the forward reaction by which two green molecules turn bimolecularly into a single red molecule.  
- Ku controls the rate of the reverse reaction, by which a red molecule turns unimolecularly into two green molecules.

Having chosen appropriate values of the constants, press SETUP to clear the world and create an initial number of green molecules.  Note: we do not create red molecules initially, although this can be done in principal.

Press RUN to start the simulation.

Set the size of the yellow box using the EDGE-SIZE slider. (If you would like to change the size while you are running a model. Press RUN to stop the model, adjust the EDGE-SIZE slider and redraw the box using the REDRAW BOX button. Resume the reaction by pressing RUN.)

After viewing the effects of several different rate constant values, use the other sliders and buttons to observe how concentration, volume, and temperature affect the equilibrium.

A note on the temperature variable. Temperature changes have a unique effect on equilibrium compared with the other variables. You can observe this effect by toggling the TEMP-EFFECT button on or off and using the slider to set the temperature of the reaction in centigrade.

## THINGS TO NOTICE

You will see molecules wandering around the world and changing color.  Pay more attention to the plot of the concentrations.  Do the plots soon reach stationary concentrations?

How does changing the concentrations of reactants and products in the system affect the equilibrium? Does it take more or less time to reach a stationary condition under various conditions?

What is the effect of temperature on the equilibrium of the system compared to volume or concentration?  In the Procedures window, note how rate constants are calculated based on the temperature.

Notice how the ratio of products to reactants changes with changes to the system. Does the ratio change much with each factor? Make a window that show the value of Keq to help you determine this.

Why do the traces of each breed eventual balance around a constant average? How come this value is an average and not a constant?

## THINGS TO TRY

How do the stationary concentrations depend on the values of Kb and Ku?   You can change Ku and Kb while the model is running.   See if you can predict what the stationary concentrations will be with various combinations of Kb and Ku.

Without adding additional reactants or products and with the temperature effect in the off position, note that more red product molecules accumulate when the volume decreases.  Can you explain why?

Observe the progress of the reaction at high and low temperatures.  Does this observed trend fit your expectations?

Try adding some molecules to the system that have no "breed", as an inert gas. Does this affect the equilibrium? Why or why not?

## EXTENDING THE MODEL

Try altering the code so that when two green molecules collide, they produce two red molecules instead of one. Likewise, alter it so that two red molecules must collide to form two green molecules. Observe the effect of volume on this system. Is the effect as you predicted?

What would the effect of adding a catalyst to the system be?  Add a catalyst breed that accelerates the reaction and observe the trend.  Are you surprised?

Add a monitor that measures the equilibrium constant for the system.  Is it really a constant?

## RELATED MODELS

Simple Kinetics 1, Simple Kinetics 3

## NETLOGO FEATURES

Notice the use of breeds in the model.

Notice how we store an agentset of patches in the `black-patches` variable.  Computing this agentset once ahead of time (at the time the box is drawn) is faster than recomputing it at every iteration.

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

n2o4
true
0
Circle -7500403 true true 61 107 88
Circle -7500403 true true 151 106 88
Circle -1 true false 20 64 64
Circle -1 true false 217 63 64
Circle -1 true false 216 171 63
Circle -1 true false 21 173 61

no2
true
0
Circle -7500403 true true 104 104 91
Circle -1 true false 194 119 63
Circle -1 true false 46 120 58

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
NetLogo 5.0beta3
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

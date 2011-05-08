;; To see the rest of this model, look in the System Dynamics Modeler
;; window.  (If the window isn't showing, you can open it from the
;; Tools menu.)

globals [ previous-carbon ]

to setup
  ca
  set previous-carbon 0
  system-dynamics-setup    ;; defined by the System Dynamics Modeler
  do-plot
end

to go
  system-dynamics-go       ;; defined by the System Dynamics Modeler
  do-plot
  set previous-carbon carbon
end

to do-plot
  set-current-plot "output"
  set-current-plot-pen "carbon"
  plotxy ticks carbon
  set-current-plot-pen "nitrogen"
  plotxy ticks nitrogen
  set-current-plot "productivity"
  set-current-plot-pen "productivity"
  plotxy ticks productivity
  set-current-plot "trees"
  system-dynamics-do-plot
 end

to-report pulse [volume initial interval]
  set interval abs interval
  let our-x ticks - initial
  let peak volume / dt
  let slope peak / ( dt / 2 )
  let offset abs our-x
  ;; if we have passed the initial pulse, then recalibrate
  ;; to the next pulse interval, if there IS an interval given
  if ( interval > 0 and our-x > ( dt / 2 ) )
    [ set offset  ( our-x mod interval )
      if ( offset > dt / 2 ) [ set offset 0 + offset - interval ]  ]
  ;; the down side of the pulse
  if ( offset >= 0 and offset <= dt / 2  )
     [ report peak - ( slope * offset ) ]
  ;; the upside of the pulse
  if ( offset < 0 and offset >= ( 0 - ( dt / 2 ) ) )
     [ report slope * min (list ( dt / 2 ) ( abs ( interval - offset ) ) ) ]
  report 0
end
@#$#@#$#@
GRAPHICS-WINDOW
594
346
793
440
1
0
63.0
1
10
1
1
1
0
1
1
1
-1
1
0
0
1
1
1
ticks

PLOT
5
369
516
541
productivity
time
1000kg per ha
0.0
0.0
0.0
0.0
true
false
"" ""
PENS
"productivity" 1.0 0 -16777216 true "" ""

PLOT
3
10
514
183
trees
NIL
NIL
0.0
0.0
0.0
0.0
true
true
"" ""
PENS
"tabonuco" 1.0 0 -13791810 true "" ""
"yagrumo" 1.0 0 -2064490 true "" ""

BUTTON
652
14
718
47
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
729
14
792
47
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
652
99
869
132
user-hurricane-frequency
user-hurricane-frequency
0
100
20
1
1
NIL
HORIZONTAL

BUTTON
651
58
823
91
NIL
setup repeat 250 [ go ]
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
520
187
666
232
NIL
amount-of-tabonuco
3
1
11

MONITOR
520
241
667
286
NIL
amount-of-yagrumo
3
1
11

SLIDER
652
139
863
172
user-hurricane-strength
user-hurricane-strength
0
1
0.4
0.1
1
NIL
HORIZONTAL

BUTTON
805
14
868
47
step
go
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
521
61
609
106
NIL
productivity
3
1
11

PLOT
4
187
515
359
output
time
NIL
0.0
0.0
0.0
0.0
true
true
"" ""
PENS
"nitrogen" 1.0 0 -1184463 true "" ""
"carbon" 1.0 0 -16777216 true "" ""

@#$#@#$#@
## WHAT IS IT?

This is a system dynamics model of a simple ecosystem.  Two species of trees -- tabonuco and yagrumo -- compete for space in a forest canopy.  This model illustrates the role of hurricane destruction in this ecosystem, as well as the resultant nitrogen and carbon produced by the ecosystem.

## HOW IT WORKS

The relations between the tree species and the amount of space into which they can grow are defined in the System Dynamics Modeler.  The TABONUCO and YAGRUMO stocks represent the population of the two tree species.  They can only grow in the gaps of the forest canopy.  These spaces are represented by the GAPS stock.  The rate at which the trees grow or die is represented by the TABONUCO-GROWS and YAGRUMO-GROWS flows.  The flows connect to the GAPS stock to the tree stocks.  If more trees grow, there are fewer gaps in the forest.  If trees die then there are more gaps.

Tabonuco trees grow slower than yagrumo trees, these rates are represented by the TABONUCO-GROWTH-RATE and YAGRUMO-GROWTH-RATE variables.  However, tabonuco trees outlive yagrumo trees.  This relationship is represented by the TABONUCO-OUTGROWS-YAGRUMO flow from the YAGRUMO stock to the TABONUCO stock.

The YAGRUMO-GROWS and TABONUCO-GROWS flows are also connected to the DISTURBANCE variable.  DISTURBANCE represents the destruction of trees by hurricanes.  The HURRICANE-STRENGTH and HURRICANE-FREQUENCY variables determine the timing and amount of disturbance, which can be controlled by sliders.

The model also tracks the amount of carbon and nitrogen produced by the trees.  The trees produce resources via photosynthesis.  The rest of the living things in the forest use these resources.  The CARBON and NITROGEN variables calculate how much of those chemicals are produced by the trees.

## HOW TO USE IT

Press the SETUP button to initialize the model.

To run the model continuously, press GO.

To run the model for one time step, press STEP.

Alternatively, you can run only 250 steps by pressing the
SETUP REPEAT 250 [ GO ] button.

Adjust the USER-HURRICANE-FREQUENCY slider to determine how many time units occur between hurricanes.

Adjust the USER-HURRICANE-STRENGTH slider to determine how many trees are destroyed by the hurricanes

## THINGS TO NOTICE

Hurricanes play an important role in the ecosystem.  The relative amount of each tree is highly sensitive to both the rate and strength of hurricanes.  Both species co-exist only within a certain range of hurricane frequency and strength.

## THINGS TO TRY

Change the USER-HURRICANE-FREQUENCY slider, observe what happens to the YAGRUMO population, and the productivity.  Do the same with USER-HURRICANE-STRENGTH.

Use BehaviorSpace (on the Tools menu) to sweep the parameter space for the USER-HURRICANE-FREQUENCY and USER-HURRICANE-STRENGTH.  An experiment that does this is included.

Use the GLOBALS monitor to observe the value off stocks, constants, model time, and model dt.

## EXTENDING THE MODEL

Add a third species of tree to the model.

Model the competition for the shared GAP stock in a different manner.

Vary the strength of the hurricanes, randomly, across an interval.

## NETLOGO FEATURES

This model uses the System Dynamics Modeler.

It includes a built-in BehaviorSpace experiment.

## RELATED MODELS

Tabonuco Yagrumo Hybrid

## CREDITS AND REFERENCES

This model is based on the work of The Learning Partnership in the Journey to El Yunque project (PI's include Steven McGee, Jess Zimmerman, and Steven Croft). To view the original materials or to learn more about that project, visit http://elyunque.net/journey.html.
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
NetLogo 5.0beta1
@#$#@#$#@
need-to-manually-make-preview-for-this-model
@#$#@#$#@
1.0
    org.nlogo.sdm.gui.AggregateDrawing 37
        org.nlogo.sdm.gui.StockFigure "attributes" "attributes" 1 "FillColor" "Color" 225 225 182 435 33 60 40
            org.nlogo.sdm.gui.WrappedStock "gaps" ";; since we don't know what order\n;; stocks are initialized in, we have \n;; to explicitly maintain a unity \n;; between gaps/tabonuco/yagrumo\n0.6" 1
        org.nlogo.sdm.gui.StockFigure "attributes" "attributes" 1 "FillColor" "Color" 225 225 182 46 345 60 40
            org.nlogo.sdm.gui.WrappedStock "tabonuco" ";; if you change this, be sure to update\n;; gaps initial value\n0.2" 1
        org.nlogo.sdm.gui.StockFigure "attributes" "attributes" 1 "FillColor" "Color" 225 225 182 826 347 60 40
            org.nlogo.sdm.gui.WrappedStock "yagrumo" ";; if you change this, be sure to update\n;; gaps initial value\n0.2" 1
        org.nlogo.sdm.gui.RateConnection 3 814 366 466 365 118 365 NULL NULL 0 0 0
            org.jhotdraw.standard.ChopBoxConnector REF 5
            org.jhotdraw.standard.ChopBoxConnector REF 3
            org.nlogo.sdm.gui.WrappedRate "tabonuco-growth-rate *  yagrumo   * tabonuco" "tabonuco-outgrows-yagrumo" REF 6 REF 4 0
        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 489 268 50 50
            org.nlogo.sdm.gui.WrappedConverter "user-hurricane-frequency" "hurricane-frequency"
        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 748 148 50 50
            org.nlogo.sdm.gui.WrappedConverter "0.20" "yagrumo-growth-rate"
        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 435 209 50 50
            org.nlogo.sdm.gui.WrappedConverter ";; We always have at least 0.001 disturbance,\n;; If hurricane frequency is above 0, then\n;; we have a hurricane strike at a regular\n;; otherwise we have a single hurricane strike\n;; at time 0\n0.001 + ifelse-value ( hurricane-frequency = 0 )\n  [ pulse hurricane-strength 0 0]\n  [ pulse hurricane-strength hurricane-frequency hurricane-frequency ]\n" "disturbance"
        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 291 271 50 50
            org.nlogo.sdm.gui.WrappedConverter "0.065" "tabonuco-growth-rate"
        org.nlogo.sdm.gui.BindingConnection 2 502 279 471 247 NULL NULL 0 0 0
            org.jhotdraw.contrib.ChopDiamondConnector REF 11
            org.jhotdraw.contrib.ChopDiamondConnector REF 15
        org.nlogo.sdm.gui.BindingConnection 2 118 365 466 365 NULL NULL 0 0 0
            org.jhotdraw.standard.ChopBoxConnector REF 3
            org.nlogo.sdm.gui.ChopRateConnector REF 7
        org.nlogo.sdm.gui.BindingConnection 2 814 366 466 365 NULL NULL 0 0 0
            org.jhotdraw.standard.ChopBoxConnector REF 5
            org.nlogo.sdm.gui.ChopRateConnector REF 7
        org.nlogo.sdm.gui.BindingConnection 2 333 303 466 365 NULL NULL 0 0 0
            org.jhotdraw.contrib.ChopDiamondConnector REF 17
            org.nlogo.sdm.gui.ChopRateConnector REF 7
        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 297 527 50 50
            org.nlogo.sdm.gui.WrappedConverter "tabonuco * 226.3" "amount-of-tabonuco"
        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 443 582 50 50
            org.nlogo.sdm.gui.WrappedConverter "2.96 * ( amount-of-tabonuco + 2.1 * amount-of-yagrumo )" "nitrogen"
        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 572 515 50 50
            org.nlogo.sdm.gui.WrappedConverter "yagrumo * 226.3" "amount-of-yagrumo"
        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 446 423 50 50
            org.nlogo.sdm.gui.WrappedConverter "amount-of-tabonuco + amount-of-yagrumo" "carbon"
        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 571 422 50 50
            org.nlogo.sdm.gui.WrappedConverter ";; this is the equivalent of derivn(carbon, 1)\n;; in Stella, however we have to explicitly\n;; track the previous value, see the GO procedure\nifelse-value ( previous-carbon = 0 )\n [ 0 ]\n [ max (list 0 ( ( carbon - previous-carbon) / dt ) )] " "productivity"
        org.nlogo.sdm.gui.BindingConnection 2 118 396 307 541 NULL NULL 0 0 0
            org.jhotdraw.standard.ChopBoxConnector REF 3
            org.jhotdraw.contrib.ChopDiamondConnector REF 31
        org.nlogo.sdm.gui.BindingConnection 2 814 395 611 529 NULL NULL 0 0 0
            org.jhotdraw.standard.ChopBoxConnector REF 5
            org.jhotdraw.contrib.ChopDiamondConnector REF 35
        org.nlogo.sdm.gui.BindingConnection 2 336 541 456 458 NULL NULL 0 0 0
            org.jhotdraw.contrib.ChopDiamondConnector REF 31
            org.jhotdraw.contrib.ChopDiamondConnector REF 37
        org.nlogo.sdm.gui.BindingConnection 2 340 558 449 600 NULL NULL 0 0 0
            org.jhotdraw.contrib.ChopDiamondConnector REF 31
            org.jhotdraw.contrib.ChopDiamondConnector REF 33
        org.nlogo.sdm.gui.BindingConnection 2 580 548 484 598 NULL NULL 0 0 0
            org.jhotdraw.contrib.ChopDiamondConnector REF 35
            org.jhotdraw.contrib.ChopDiamondConnector REF 33
        org.nlogo.sdm.gui.BindingConnection 2 582 529 485 458 NULL NULL 0 0 0
            org.jhotdraw.contrib.ChopDiamondConnector REF 35
            org.jhotdraw.contrib.ChopDiamondConnector REF 37
        org.nlogo.sdm.gui.BindingConnection 2 495 447 571 447 NULL NULL 0 0 0
            org.jhotdraw.contrib.ChopDiamondConnector REF 37
            org.jhotdraw.contrib.ChopDiamondConnector REF 39
        org.nlogo.sdm.gui.RateConnection 3 425 85 270 209 115 333 NULL NULL 0 0 0
            org.jhotdraw.standard.ChopBoxConnector REF 1
            org.jhotdraw.standard.ChopBoxConnector REF 3
            org.nlogo.sdm.gui.WrappedRate "tabonuco-growth-rate * tabonuco *  gaps - ( disturbance * tabonuco )" "tabonuco-grows" REF 2 REF 4 0
        org.nlogo.sdm.gui.RateConnection 3 504 85 660 210 816 335 NULL NULL 0 0 0
            org.jhotdraw.standard.ChopBoxConnector REF 1
            org.jhotdraw.standard.ChopBoxConnector REF 5
            org.nlogo.sdm.gui.WrappedRate "yagrumo-growth-rate * yagrumo * MAX (LIST 0 ( gaps - tabonuco-grows ) ) - ( disturbance * yagrumo )" "yagrumo-grows" REF 2 REF 6 0
        org.nlogo.sdm.gui.BindingConnection 2 307 279 270 209 NULL NULL 0 0 0
            org.jhotdraw.contrib.ChopDiamondConnector REF 17
            org.nlogo.sdm.gui.ChopRateConnector REF 62
        org.nlogo.sdm.gui.BindingConnection 2 754 179 660 210 NULL NULL 0 0 0
            org.jhotdraw.contrib.ChopDiamondConnector REF 13
            org.nlogo.sdm.gui.ChopRateConnector REF 66
        org.nlogo.sdm.gui.BindingConnection 2 425 85 270 209 NULL NULL 0 0 0
            org.jhotdraw.standard.ChopBoxConnector REF 1
            org.nlogo.sdm.gui.ChopRateConnector REF 62
        org.nlogo.sdm.gui.BindingConnection 2 504 85 660 210 NULL NULL 0 0 0
            org.jhotdraw.standard.ChopBoxConnector REF 1
            org.nlogo.sdm.gui.ChopRateConnector REF 66
        org.nlogo.sdm.gui.BindingConnection 2 437 231 270 209 NULL NULL 0 0 0
            org.jhotdraw.contrib.ChopDiamondConnector REF 15
            org.nlogo.sdm.gui.ChopRateConnector REF 62
        org.nlogo.sdm.gui.BindingConnection 2 482 231 660 210 NULL NULL 0 0 0
            org.jhotdraw.contrib.ChopDiamondConnector REF 15
            org.nlogo.sdm.gui.ChopRateConnector REF 66
        org.nlogo.sdm.gui.BindingConnection 2 115 333 270 209 NULL NULL 0 0 0
            org.jhotdraw.standard.ChopBoxConnector REF 3
            org.nlogo.sdm.gui.ChopRateConnector REF 62
        org.nlogo.sdm.gui.BindingConnection 2 816 335 660 210 NULL NULL 0 0 0
            org.jhotdraw.standard.ChopBoxConnector REF 5
            org.nlogo.sdm.gui.ChopRateConnector REF 66
        org.nlogo.sdm.gui.BindingConnection 2 270 209 660 210 NULL NULL 0 0 0
            org.nlogo.sdm.gui.ChopRateConnector REF 62
            org.nlogo.sdm.gui.ChopRateConnector REF 66
        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 422 125 50 50
            org.nlogo.sdm.gui.WrappedConverter "user-hurricane-strength" "hurricane-strength"
        org.nlogo.sdm.gui.BindingConnection 2 450 171 456 212 NULL NULL 0 0 0
            org.jhotdraw.contrib.ChopDiamondConnector REF 97
            org.jhotdraw.contrib.ChopDiamondConnector REF 15
@#$#@#$#@
<experiments>
  <experiment name="Sweep hurricane parameters" repetitions="1" runMetricsEveryStep="true">
    <setup>setup</setup>
    <go>go</go>
    <timeLimit steps="250"/>
    <exitCondition>yagrumo = 0 or tabonuco = 0</exitCondition>
    <metric>tabonuco</metric>
    <metric>yagrumo</metric>
    <metric>productivity</metric>
    <metric>carbon</metric>
    <metric>nitrogen</metric>
    <metric>gaps</metric>
    <metric>ticks</metric>
    <steppedValueSet variable="user-hurricane-strength" first="0" step="0.1" last="1"/>
    <steppedValueSet variable="user-hurricane-frequency" first="0" step="5" last="80"/>
  </experiment>
</experiments>
@#$#@#$#@
@#$#@#$#@
default
0.0
-0.2 0 1.0 0.0
0.0 1 1.0 0.0
0.2 0 1.0 0.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

@#$#@#$#@
0
@#$#@#$#@

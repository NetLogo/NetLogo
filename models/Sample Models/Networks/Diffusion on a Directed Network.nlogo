directed-link-breed [active-links active-link]
directed-link-breed [inactive-links inactive-link]

turtles-own [ val new-val ] ; a node's past and current quantity, represented as size
links-own [ current-flow ]  ; the amount of quantity that has passed through a link
                            ; in a given step

globals [
  total-val                 ; total quantity in the system
  max-val                   ; maximum quantity held by a single node in the system
  max-flow                  ; maximum quantity that has passed through a link in the system
  mean-flow                 ; average quantity that is passing through an arbitrary
                            ; link in the system
]

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  set-default-shape turtles "circle"
  set-default-shape links "small-arrow-link"
  ; create the grid of nodes
  ask patches with [abs pxcor < (grid-size / 2) and abs pycor < (grid-size / 2)]
    [ sprout 1 [ set color blue ] ]

  ; create a directed network such that each node has a LINK-CHANCE percent chance of
  ; having a link established from a given node to one of its neighbors
  ask turtles [
    set val 1
    let neighbor-nodes turtle-set [turtles-here] of neighbors4
    create-active-links-to neighbor-nodes
    [
      set current-flow 0
      if random-float 100 > link-chance
      [
        set breed inactive-links
        hide-link
      ]
    ]
  ]
  ; spread the nodes out
  ask turtles [
    setxy (xcor * (max-pxcor - 1) / (grid-size / 2 - 0.5))
          (ycor * (max-pycor - 1) / (grid-size / 2 - 0.5))
  ]
  update-globals
  update-visuals
  reset-ticks
end

;;;;;;;;;;;;;;;;;;;;;;;
;;; Main Procedure  ;;;
;;;;;;;;;;;;;;;;;;;;;;;

to go
  ask turtles [ set new-val 0 ]
  ask turtles [
    let recipients out-active-link-neighbors
    ifelse any? recipients [
      let val-to-keep val * (1 - diffusion-rate / 100)
      ; we keep some amount of our value from one turn to the next
      set new-val new-val + val-to-keep
      ; What we don't keep for ourselves, we divide evenly among our out-link-neighbors.
      let val-increment ((val - val-to-keep) / count recipients)
      ask recipients [
        set new-val new-val + val-increment
        ask in-active-link-from myself [ set current-flow val-increment ]
      ]
    ] [
      set new-val new-val + val
    ]
  ]
  ask turtles [ set val new-val ]
  update-globals
  update-visuals
  tick
end

to rewire-a-link
  if any? active-links [
    ask one-of active-links [
      set breed inactive-links
      hide-link
    ]
    ask one-of inactive-links [
      set breed active-links
      show-link
    ]
  ]
end

;;;;;;;;;;;;;;;;;;;;;;;
;;;     Updates     ;;;
;;;;;;;;;;;;;;;;;;;;;;;

to update-globals
  set total-val sum [ val ] of turtles
  set max-val max [ val ] of turtles
  if any? active-links [
    set max-flow max [current-flow] of active-links
    set mean-flow mean [current-flow] of active-links
  ]
end

to update-visuals
  ask turtles [ update-node-appearance ]
  ask active-links [ update-link-appearance ]
end

to update-node-appearance ; node procedure
  ; scale the size to be between 0.1 and 5.0
  set size 0.1 + 5 * sqrt (val / total-val)
end

to update-link-appearance ; link procedure
  ; scale color to be brighter when more value is flowing through it
  set color scale-color gray (current-flow / (2 * mean-flow + 0.00001)) -0.4 1
end
@#$#@#$#@
GRAPHICS-WINDOW
215
10
645
461
10
10
20.0
1
10
1
1
1
0
0
0
1
-10
10
-10
10
1
1
1
ticks
30.0

BUTTON
10
90
105
123
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
109
90
209
123
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
10
50
210
83
link-chance
link-chance
0
100
50
1
1
%
HORIZONTAL

SLIDER
10
10
210
43
grid-size
grid-size
3
19
9
2
1
NIL
HORIZONTAL

PLOT
10
210
210
426
Histogram
val
# of nodes
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" "set-plot-x-range 0 ceiling (max-val  + 0.5)\nset-histogram-num-bars ceiling (sqrt (count turtles))\nhistogram [val] of turtles"

SLIDER
10
170
210
203
diffusion-rate
diffusion-rate
0
100
10
1
1
%
HORIZONTAL

BUTTON
10
130
106
163
rewire-a-link
rewire-a-link\ndisplay
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
110
130
210
163
keep-rewiring
rewire-a-link\ndisplay
T
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

This model demonstrates diffusion of a quantity through a directed network. The quantity moves among nodes in the network only along established, directed links between two nodes.

The simple rules that drive this diffusion lead to interesting patterns related to the topology, density, and stability of the network. Furthermore, the model may be useful in understanding the basic properties of dynamic processes on networks, and provides a useful starting point for designing more complex and realistic network-based models.

## HOW IT WORKS

In each tick, each node shares some percentage (defined by the DIFFUSION-RATE slider) of its "value" quantity with its neighbors in the network.  Specifically, the amount of shared value is divided equally and sent along each of the outgoing links from each node to each other node.  If a node has no outgoing links, then it doesn't share any of it's value; it just accumulates any that its neighbors have provided via incoming links.

Note that because it is a directed network, node B may give value to node A even if node A doesn't give back.

The size of each node shows how much "value" that node has, where the area of the node is proportional to its value.  The brightness of a link represents how much value just flowed through that edge.

## HOW TO USE IT

Choose the size of network that you want to model using the GRID-SIZE slider.  
Choose the expected density of links in the network using the LINK-CHANCE slider.

To create the network with these properties, press SETUP.

The DIFFUSION-RATE slider controls how much "value" each node shares with its neighbors during a given time step.

Press the GO button to run the model.

The REWIRE-A-LINK button causes one link to disappear, and a new one to appear elsewhere in the grid.

The KEEP-REWIRING button causes a continual rewiring of links to occur.

The histogram displays the number of nodes whose values fall into certain ranges.  For instance, you might see that there are many nodes with nearly zero value, while there are just a few nodes with very large value.

## THINGS TO NOTICE

As time passes, the network tends toward an equilibrium state.  (Is that always the case, or is it possible for a network to never settle down?)

However, if you run both the GO and KEEP-REWIRING buttons at the same time, then the network will never completely settle down.  If you ran the model in this way for a long long time, would the distribution of value across the nodes in the network be uniform, if you averaged across time?  Or would nodes near the edges of the grid tend to have more or less value?

## THINGS TO TRY

Try running the model with a small 3x3 grid.  How many nodes end up with positive value (not approaching zero) after running the model for a while?   Sometimes just a single node ends up with all of the value, while other times every node in the network can sustain a positive value.  What properties of the network are necessary in order for every node to sustain a positive value?  Are these properties more or less likely to occur with large networks?

## EXTENDING THE MODEL

Imagine you are modeling a business economy, where each node is a business, and it has suppliers and customers (represented by directed links from that node).  Is it reasonable to assume that as a business accumulates more profit from sales, it will in turn purchase more from its suppliers?  In order to more accurately match this economic model, change the model into a supply chain model where each node also has an inventory level, and a price they are charging per unit.  Try to come up with reasonable rules for how many units each business decides to buy or sell.

How would you change this model to more accurately represent water flowing (or being pumped) through pipes?  Should the links be directed or undirected?  What if water is continually flowing in or out of the system at certain locations?

## NETLOGO FEATURES

This model works in a manner analogous to NetLogo's `diffuse` command, which causes patches to all share with their neighbors portions of the value of some variable.  
However, whereas the neighbor relationship in patches is symmetric, this model uses directed links, which can be used to create asymmetric relationships between agents.  If you used undirected links, the behavior of this model would more closely resemble the `diffuse` command, where the value of all the nodes would eventually become the same.

In this model, there are two link-breeds: one for active links (which are shown in the view) and another for inactive links (which are invisible).  This makes "rewiring" of links easier, because rather than killing a link and creating a new link, we can just change the breed of a link and hide or show it.

## RELATED MODELS

Virus on a Network

## CREDITS AND REFERENCES
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

small-arrow-link
0.0
-0.2 0 0.0 1.0
0.0 1 1.0 0.0
0.2 0 0.0 1.0
link direction
true
0
Line -7500403 true 150 150 120 180
Line -7500403 true 150 150 180 180

@#$#@#$#@
0
@#$#@#$#@

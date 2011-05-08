turtles-own
[
  ;; this is used to mark turtles we have already visited
  explored?
]

globals
[
  component-size          ;; number of turtles explored so far in the current component
  giant-component-size    ;; number of turtles in the giant component
  giant-start-node        ;; node from where we started exploring the giant component
]

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  set-default-shape turtles "circle"
  make-turtles
  ;; at this stage, all the components will be of size 1,
  ;; since there are no edges yet
  find-all-components
  color-giant-component
  reset-ticks
end

to make-turtles
  crt num-nodes
  layout-circle turtles max-pxcor - 1
end

;;;;;;;;;;;;;;;;;;;;;;
;;; Main Procedure ;;;
;;;;;;;;;;;;;;;;;;;;;;

to go
  ;; if the below condition is true then we have a fully connected network and we need to stop
  if ( (2 * count links ) >= ( (count turtles) * (count turtles - 1) ) ) [
    display
    user-message "Network is fully connected. No more edges can be added."
    stop
  ]
  add-edge
  find-all-components
  color-giant-component
  ask links [ set color [color] of end1 ]  ;; recolor all edges
  layout
  tick
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Network Exploration ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; to find all the connected components in the network, their sizes and starting turtles
to find-all-components
  ask turtles [ set explored? false ]
  ;; keep exploring till all turtles get explored
  loop
  [
    ;; pick a node that has not yet been explored
    let start one-of turtles with [ not explored? ]
    if start = nobody [ stop ]
    ;; reset the number of turtles found to 0
    ;; this variable is updated each time we explore an
    ;; unexplored node.
    set component-size 0
    ;; at this stage, we recolor everything to light gray
    ask start [ explore (gray + 2) ]
    ;; the explore procedure updates the component-size variable.
    ;; so check, have we found a new giant component?
    if component-size > giant-component-size
    [
      set giant-component-size component-size
      set giant-start-node start
    ]
  ]
end

;; Finds all turtles reachable from this node (and recolors them)
to explore [new-color]  ;; node procedure
  if explored? [ stop ]
  set explored? true
  set component-size component-size + 1
  ;; color the node
  set color new-color
  ask link-neighbors [ explore new-color ]
end

;; color the giant component red
to color-giant-component
  ask turtles [ set explored? false ]
  ask giant-start-node [ explore red ]
end

;;;;;;;;;;;;;;;;;;;;;;;
;;; Edge Operations ;;;
;;;;;;;;;;;;;;;;;;;;;;;

;; pick a random missing edge and create it
to add-edge
  let node1 one-of turtles
  let node2 one-of turtles
  ask node1 [
    ifelse link-neighbor? node2 or node1 = node2
    ;; if there's already an edge there, then go back
    ;; and pick new turtles
    [ add-edge ]
    ;; else, go ahead and make it
    [ create-link-with node2 ]
  ]

end

;;;;;;;;;;;;;;
;;; Layout ;;;
;;;;;;;;;;;;;;
to layout
  if not layout? [ stop ]
  ;; the number 10 here is arbitrary; more repetitions slows down the
  ;; model, but too few gives poor layouts
  repeat 10 [
    do-layout
    display  ;; so we get smooth animation
  ]
end

to do-layout
  layout-spring (turtles with [any? link-neighbors]) links 0.4 6 1
end
@#$#@#$#@
GRAPHICS-WINDOW
393
17
858
503
45
45
5.0
1
10
1
1
1
0
0
0
1
-45
45
-45
45
1
1
1
ticks

BUTTON
67
39
130
72
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
109
78
172
111
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
207
36
379
69
num-nodes
num-nodes
2
500
80
1
1
NIL
HORIZONTAL

PLOT
17
175
380
452
Growth of the giant component
Connections per node
Fraction in giant component
0.0
3.0
0.0
1.0
true
false
"" ""
PENS
"size" 1.0 0 -2674135 true "" ";; We multiply by 2 because every edge should be counted twice while calculating,\n;; the average, since an edge connects two turtles.\n;; We divide by the node count to normalize the y axis to a 0 to 1 range.\nplotxy (2 * count links / count turtles)\n       (giant-component-size / count turtles)\n"
"transition" 1.0 0 -7500403 true "plot-pen-up\nplotxy 1 0\nplot-pen-down\nplotxy 1 1\n" ""

MONITOR
226
114
359
159
Giant component size
giant-component-size
3
1
11

BUTTON
24
78
105
111
go once
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

SWITCH
227
73
359
106
layout?
layout?
0
1
-1000

BUTTON
47
116
149
149
redo layout
do-layout\ndisplay
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

In a network, a "component" is a group of nodes that are all connected to each other, directly or indirectly.  So if a network has a "giant component", that means almost every node is reachable from almost every other.  This model shows how quickly a giant component arises if you grow a random network.

## HOW IT WORKS

Initially we have nodes but no connections (edges) between them. At each step, we pick two nodes at random which were not directly connected before and add an edge between them.  All possible connections between them have exactly the same probability of occurring.

As the model runs, small chain-like "components" are formed, where the members in each component are either directly or indirectly connected to each other.  If an edge is created between nodes from two different components, then those two components merge into one. The component with the most members at any given point in time is the "giant" component and it is colored red.  (If there is a tie for largest, we pick a random component to color.)

## HOW TO USE IT

The NUM-NODES slider controls the size of the network.  Choose a size and press SETUP.

Pressing the GO ONCE button adds one new edge to the network.  To repeatedly add edges, press GO.

As the model runs, the nodes and edges try to position themselves in a layout that makes the structure of the network easy to see.  Layout makes the model run slower, though.  To get results faster, turn off the LAYOUT? switch.

The REDO LAYOUT button runs the layout-step procedure continuously to improve the layout of the network.

A monitor shows the current size of the giant component, and the plot shows how the giant component's size changes over time.

## THINGS TO NOTICE

The y-axis of the plot shows the fraction of all nodes that are included in the giant component.  The x-axis shows the average number of connections per node. The vertical line on the plot shows where the average number of connections per node equals 1.  What happens to the rate of growth of the giant component at this point?

The model demonstrates one of the early proofs of random graph theory by the mathematicians Paul Erdos and Alfred Renyi (1959).  They showed that the largest connected component of a network formed by randomly connecting two existing nodes per time step, rapidly grows after the average number of connections per node equals 1. In other words, the average number of connections has a "critical point" where the network undergoes a "phase transition" from a rather unconnected world of a bunch of small, fragmented components, to a world where most nodes belong to the same connected component.

## THINGS TO TRY

Let the model run until the end.  Does the "giant component" live up to its name?

Run the model again, this time slowly, a step at a time.  Watch how the components grow. What is happening when the plot is steepest?

Run it with a small number of nodes (like 10) and watch the plot.  How does it differ from the plot you get when you run it with a large number of nodes (like 300)?  If you do multiple runs with the same number of nodes, how much does the shape of the plot vary from run to run?  You can turn off the LAYOUT? switch to get results faster.

## EXTENDING THE MODEL

Right now the probability of any two nodes getting connected to each other is the same. Can you think of ways to make some nodes more attractive to connect to than others?  How would that impact the formation of the giant component?

## NETWORK CONCEPTS

Identification of the connected components is done using a standard search algorithm called "depth first search."  "Depth first" means that the algorithm first goes deep into a branch of connections, tracing them out all the way to the end.  For a given node it explores its neighbor's neighbors (and then their neighbors, etc) before moving on to its own next neighbor.  The algorithm is recursive so eventually all reachable nodes from a particular starting node will be explored.  Since we need to find every reachable node, and since it doesn't matter what order we find them in, another algorithm such as "breadth first search" would have worked equally well.  We chose depth first search because it is the simplest to code.

The position of the nodes is determined by the "spring" method, which is further described in the Preferential Attachment model.

## NETLOGO FEATURES

Both nodes and edges are turtles.  Edge turtles have the "line" shape.  The edge turtle's `size` variable is used to make the edge be the right length.

Lists are used heavily in this model.  Each node maintains a list of its neighboring nodes.  Lists are also used in the procedure that identifies the components.

## RELATED MODELS

See other models in the Networks section of the Models Library, such as Preferential Attachment.

See also Network Example, in the Code Examples section.

## CREDITS AND REFERENCES

This model is adapted from:
Duncan J. Watts. Six Degrees: The Science of a Connected Age (W.W. Norton & Company, New York, 2003), pages 43-47.

Watts' website is available at:  http://smallworld.columbia.edu/

The work Watts describes was originally published in:
P. Erdos and A. Renyi. On random graphs. Publ. Math. Debrecen, 6:290-297, 1959.

This paper has some additional analysis:
S. Janson, D.E. Knuth, T. Luczak, and B. Pittel. The birth of the giant component. Random Structures & Algorithms 4, 3 (1993), pages 233-358.
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

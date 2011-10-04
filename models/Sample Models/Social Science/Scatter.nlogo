turtles-own
[
  nearby-classmates         ;; agentset of turtles within some specified radius
  closest-classmate         ;; the nearest turtle
  stopped?                  ;; if the turtle hasn't moved
  rule                      ;; the turtle's rule (the rules are named by strings)
  origin                    ;; the turtle's original position
  close-classmates          ;; turtles within the too-close range
  far-classmates            ;; turtles within the too-far range
  total-distance-moved      ;; total distance moved by the turtle
  previous-patch            ;; patch the turtle was previously on
]

;;
;; SETUP AND HELPERS
;;

to setup
  clear-all
  set-default-shape turtles "circle"
  make-turtles
  reset-ticks
end

to make-turtles
  if [count patches in-radius initial-radius] of patch 0 0 < num-turtles-to-create
  [ user-message (word "There aren't enough patches in initial-radius to create the number "
                       "of turtles you've asked for.  Please make initial-radius larger or decrease "
                       "the number of turtles.")
    stop ]
  ask patch 0 0 [
      create-initial-turtles num-random-min   "random-min"   violet
      create-initial-turtles num-random       "random"       brown
      create-initial-turtles num-random-away  "random-away"  pink
      create-initial-turtles num-open-min     "open-min"     blue
      create-initial-turtles num-open-min-max "open-min-max" orange ]
  set-common-variables
end

;; reports the total number of turtles the user has requested
to-report num-turtles-to-create
  report num-random-min +
         num-random +
         num-random-away +
         num-open-min +
         num-open-min-max
end

;; ask n random patches without a turtle on them that are within initial-radius of the
;; asker to sprout a turtle with the rule set to turtle-rule and color to turtle-color
to create-initial-turtles [n turtle-rule turtle-color]
  ask n-of n (patches in-radius initial-radius with [not any? turtles-here])
  [ sprout 1
    [ set rule turtle-rule
      set color turtle-color ] ]
end

;; initializes turtle variables
;; also moves the turtles a bit so that they are a bit more randomly scattered
to set-common-variables
  ask turtles
  [ set xcor xcor - 0.5 + random-float 1
    set ycor ycor - 0.5 + random-float 1
    set origin patch-here
    set stopped? false
    set total-distance-moved 0 ]
end


;;
;; GO AND TURTLE STRATEGIES
;;

to go
  if all? turtles [stopped?] [ stop ]
  ask turtles [
    set previous-patch patch-here
    if rule = "random-min"   [ move-random-min ]
    if rule = "random"       [ move-random ]
    if rule = "random-away"  [ move-random-away ]
    if rule = "open-min"     [ move-open-min ]
    if rule = "open-min-max" [ move-open-min-max ]
    set total-distance-moved (total-distance-moved + (distance previous-patch))
  ]
  tick
end

;; set a random heading and move at
;; each time step, with no stopping condition
to move-random ;; turtle procedure
  rt random 360
  avoid-walls
  fd step-size
end

;; turtle procedure: set a random heading and move until
;; all other turtles are at least "too-close" away
to move-random-min
  set nearby-classmates other turtles in-radius too-close
  ifelse any? nearby-classmates  ; if there aren't turtles nearby, stop, you are scattered
  [ rt random 360
    avoid-walls
    fd step-size
    set stopped? false ]
  [ set stopped? true ]
end

;; set heading in the direction opposite
;; of the closest turtle, without stopping
to move-random-away ;; turtle procedure
  set closest-classmate min-one-of other turtles [distance myself]
  face closest-classmate
  rt 180
  avoid-walls
  fd step-size
  set stopped? false
end

;; set heading towards the largest open space,
;; stopping when all other turtles are at least "too-close" away
to move-open-min ;; turtle procedure
  set nearby-classmates other turtles in-radius too-close
  ifelse any? nearby-classmates
  [ facexy (mean [xcor] of nearby-classmates)
           (mean [ycor] of nearby-classmates)
    rt 180
    avoid-walls
    fd step-size
    set stopped? false ]
  [ set stopped? true ]
end

;; when turtles are too close, move to an open space,
;; when turtles get too far, move in towards a more populated area, and otherwise, stop
to move-open-min-max ;; turtle procedure
  set close-classmates other turtles in-radius too-close
  set far-classmates other turtles in-radius too-far
  ifelse any? close-classmates  ; move to an open space
  [ facexy (mean [xcor] of close-classmates)
    (mean [ycor] of close-classmates)
    rt 180
    avoid-walls
    fd step-size
    set stopped? false ]
  [ ifelse not any? far-classmates  ; move to a more populated space in the 'room'
    [ facexy mean [xcor] of other turtles
      mean [ycor] of other turtles
      avoid-walls
      fd step-size
      set stopped? false ]
    [ set stopped? true ] ]
end

;; if a turtle gets to a wall, it turns around
to avoid-walls ;; turtle procedure
  if not can-move? 1
  [ rt 180 ]
end

;;
;; PLOTTING
;;

;; if there are any turtles with turtle-rule as their rule, plot the
;; mean distance from the origin
to plot-rule-mean [turtle-rule]
  if any? (turtles with [rule = turtle-rule])
  [ plot mean [distance origin] of (turtles with [rule = turtle-rule]) ]
end
@#$#@#$#@
GRAPHICS-WINDOW
276
10
709
464
23
23
9.0
1
10
1
1
1
0
0
0
1
-23
23
-23
23
1
1
1
ticks
30.0

SLIDER
36
68
240
101
initial-radius
initial-radius
0
20
10
0.5
1
NIL
HORIZONTAL

SLIDER
724
166
919
199
num-random-min
num-random-min
0
200
100
1
1
NIL
HORIZONTAL

SLIDER
724
430
920
463
num-random
num-random
0
200
0
1
1
NIL
HORIZONTAL

BUTTON
20
18
90
59
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
97
18
167
59
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
724
362
921
395
num-random-away
num-random-away
0
200
0
1
1
NIL
HORIZONTAL

TEXTBOX
725
148
815
166
Violet
11
0.0
0

TEXTBOX
724
412
814
430
Brown
11
0.0
0

TEXTBOX
725
344
815
362
Pink
11
0.0
0

PLOT
14
148
263
327
Average Distance from Origin
time ticks
average distance
0.0
100.0
0.0
100.0
true
false
"set-plot-x-range 0 30\nset-plot-y-range  0 5 ; set initial y range to half of height" ""
PENS
"random-min" 1.0 0 -8630108 true "" "plot-rule-mean \"random-min\""
"random-away" 1.0 0 -2064490 true "" "plot-rule-mean \"random-away\""
"random" 1.0 0 -6459832 true "" "plot-rule-mean \"random\""
"open-min" 1.0 0 -13345367 true "" "plot-rule-mean \"open-min\""
"open-min-max" 1.0 0 -955883 true "" "plot-rule-mean \"open-min-max\""

SLIDER
724
36
919
69
too-close
too-close
0.1
5
1.5
0.1
1
NIL
HORIZONTAL

SLIDER
724
77
919
110
too-far
too-far
0.1
5
2
0.1
1
NIL
HORIZONTAL

TEXTBOX
724
278
814
296
Orange
11
0.0
0

SLIDER
724
294
920
327
num-open-min-max
num-open-min-max
0
200
50
1
1
NIL
HORIZONTAL

TEXTBOX
724
214
814
232
Blue
11
0.0
0

SLIDER
724
231
920
264
num-open-min
num-open-min
0
200
0
1
1
NIL
HORIZONTAL

SLIDER
37
107
241
140
step-size
step-size
0.1
2
1
0.1
1
NIL
HORIZONTAL

PLOT
14
330
263
516
Mobility of Scatterers
circle number
total distance moved
0.0
5.0
0.0
1080.0
true
false
"set-plot-x-range 0 5\nset-plot-y-range 0 (max-pxcor)" ";; a histogram of the total distance traveled by turtles,\n;; grouped in concentric circles from the center patch\nplot-pen-reset\n; between the center patch and max-pxcor/4\nset-plot-pen-color red\nif (any? turtles with [(distancexy 0 0) <= ((world-width - 1) / 8)])\n[ ask patch 0 0\n  [ plot mean [total-distance-moved] of (turtles with [(distancexy 0 0) <= ((world-width - 1) / 8)])\n                          ] ]\n; between max-pxcor/4 and max-pxcor/2\nif (any? turtles with [((distancexy 0 0) > ((world-width - 1) / 8)) and\n                       ((distancexy 0 0) <= ((world-width - 1) / 4))])\n[ set-plot-pen-color green\n  ask patch 0 0\n  [ plot mean [total-distance-moved] of (turtles with [((distancexy 0 0) > ((world-width - 1) / 8)) and\n                                         ((distancexy 0 0) <= ((world-width - 1) / 4))])\n                          ] ]\n; between max-pxcor/2 and (3/4)max-pxcor\nif (any? turtles with [((distancexy 0 0) > ((world-width - 1) / 4)) and\n                       ((distancexy 0 0) <= (3 * (world-width - 1) / 8))])\n[ set-plot-pen-color violet\n  ask patch 0 0\n  [ plot mean [total-distance-moved] of (turtles with [((distancexy 0 0) > ((world-width - 1) / 4)) and\n                                         ((distancexy 0 0) <= (3 * (world-width - 1) / 8))])\n                          ] ]\n; between (3/4)max-pxcor and max-pxcor\nif (any? turtles with [((distancexy 0 0) > (3 * ((world-width - 1) / 8)) and\n                       ((distancexy 0 0) <= ((world-width - 1) / 2 )))] )\n[ set-plot-pen-color orange\n  ask patch 0 0\n  [ plot mean [total-distance-moved] of (turtles with [((distancexy 0 0) > ( 3 * ((world-width - 1) / 8)) and\n                                         ((distancexy 0 0) <= (world-width - 1) / 2))])\n                          ] ]\n; everything outside of max-pxcor\nif (any? turtles with [(distancexy 0 0) > (world-width - 1) / 2])\n[ set-plot-pen-color blue\n  ask patch 0 0\n  [ plot mean [total-distance-moved] of (turtles with [(distancexy 0 0) > (world-width - 1) / 2])\n                          ] ]"
PENS
"default" 1.0 1 -13791810 true "" ""

@#$#@#$#@
## WHAT IS IT?

This model simulates students' ideas about scattering, which takes place just before exercising in gym. The students in a class start out all bunched up, and the teacher asks them to spread out or scatter.  This simulation shows the spread of the group when the individual students follow simple rules to decide whether to move and where.  The scatterers move according to rules that were gleaned from several interviews with sixth-grade students.

The students were asked: "At the beginning of a Physical Education class, the students are standing close together. The teacher tells the students to scatter so they may perform calisthenics. What happens? Can you describe and explain?"  The students described the scattering process verbally, using coins to simulate the process and drawing a series of pictures to depict the succeeding steps.

## HOW IT WORKS

This model implements several scattering rules, which the user can mix and match:

RANDOM-MIN: Move in a random direction until you are far enough away from all of your neighbors. Random-min turtles (the violet ones) set their heading and move in a random direction if and only if there are turtles that are too close.

OPEN-MIN: Move into the largest open nearby space until you are far enough away from all of your neighbors. Open-min turtles (blue) also stop when other turtles are too close. If there are turtles that are too close, they find the heading that will take them to the largest open space.

OPEN-MIN-MAX: Move into the largest open nearby space until you are far enough away, but not too far. If you're too close, move away. If you're too far, move closer. The open-min-max turtles (orange) move to the largest open space if other turtles are too close. If the other turtles are too far away (that is, there aren't turtles within a certain space), the turtle in question moves to a more populated area.

Two of the scattering rules do not have stopping conditions, so they continue scattering indefinitely:

RANDOM-AWAY: Move in a random direction away from the person that is closest too you. Random-away turtles (pink) look at the turtle closest to them and move in the opposite direction.

RANDOM: Move about randomly, disregarding all other scatterers. Random turtles (brown) just run around in a random direction, never looking at the turtles around them.

The scattering students move at equal speeds, if they move at all. Two students cannot occupy the same location. Whether or not to move and where to move depends on each of the rules that are described. The color of the scattering students reflects the rule that they are following.

## HOW TO USE IT

Use the sliders to the right of the view to specify the number of turtles to create with each rule.

With the first three rules, the TOO-CLOSE slider sets the distance, in patches, that the turtles must be away from other turtles before stopping. The NUM-RANDOM-MIN slider indicates the number of turtles to create that move in a random direction, stopping when they are at least the distance specified by TOO-CLOSE from other turtles. Use the NUM-OPEN-MIN slider to specify how many turtles to create that move to the largest open space if there are turtles within TOO-CLOSE.

NUM-OPEN-MIN-MAX specifies the number of turtles to create that must have turtles within a certain range in order to stop. TOO-CLOSE indicates how far away other turtles must be for a turtle to stop, and TOO-FAR indicates how close the turtle in question must be to at least another turtle for it to stop.

The NUM-RANDOM-AWAY slider indicates the number of turtles to create that move away from the closest turtle, without ever stopping. NUM-RANDOM indicates the number of turtles that move in random directions without stopping.

To indicate the radius within which you want the turtles to be initially scattered, use the INITIAL-RADIUS slider. INITIAL-RADIUS must be large enough to fit the number of turtles indicated by the above sliders. See NetLogo Features, below.

To vary the distance that a turtle moves each time step, change the STEP-SIZE slider.

Press SETUP to create the number of turtles indicated, in the INITIAL-RADIUS specified. Press GO to start the model and the plots.

The plot AVERAGE DISTANCE FROM ORIGIN shows the average distance that turtles of a certain rule have moved from their original position over time. It is color-coded to match the colors of the turtles. To observe the average distance of turtles of a certain rule, watch the line of the same color.

The MOBILITY OF SCATTERERS histogram displays the average total distance moved by turtles within a certain range away from the origin. Each of the bars plots the average total distance of turtles in five concentric circles radiating from the center patch. Note that this differs from that of the AVERAGE DISTANCE FROM ORIGIN plot, which measures the distance from the turtle's present spot to its original spot, in that it adds up each step taken by the turtle.

The TICKS monitor displays the number of clock cycles that GO has run.

## THINGS TO NOTICE

While the group is spreading, notice the individual turtles' routes.  You can use the "watch turtle" functionality (by clicking on a turtle) or pen-downing a single turtle to observe its path.  Try this with a number of turtles. How do the individual and group patterns relate?

Like two people walking towards each other down a hallway trying to avoid colliding, the rules of the turtles deciding their heading based on other turtles can conflict. For instance, several neighboring turtles moving to the largest open space may choose to move to the same open spot and, in doing so, move closer to the other turtles. This can form clusters of turtles, even while the group is spreading and even in the periphery of the group.

Certain rules get to a settled, or scattered, state more quickly than others do. Which rules are better for efficient scattering?

Observe the shape of the "Average distance from Origin" plot: Why is it shaped the way it is?  What other phenomena behave in a similar way?

When observing the MOBILITY OF SCATTERERS plot, notice how scatterers farther on the outside will sometimes move a greater total distance or a smaller total distance, depending on the rule.

There are often threshold values where the scatterers will never settle down, and the model will not stop. For instance, if TOO-CLOSE is too great and there are too many turtles, they will never have enough room to completely scatter.

Some of the rules produce a more ordered end formation than others. Why is this, and which ones exhibit this behavior?  Do any regions show higher densities than others?

When certain rules are mixed together, segregation occurs. That is, turtles of a certain rule tend to be surrounded by more turtles of the same rule than not. Which combinations of rules produce this segregation more than others?

## THINGS TO TRY

When experimenting with the move-into-range turtles, vary the TOO-CLOSE and TOO-FAR sliders concurrently with the STEP-SIZE slider. If the step size is too big and the TOO-CLOSE and TOO-FAR values creating too narrow of a range, will the scatterers ever settle?

Try mixing rules to explore how turtles of different rules interact with each other.

Watch what happens when a few random-scatter turtles run around a set of turtles with stopping conditions, like move-to-open-space turtles.

Vary the STEP-SIZE slider, and see what effect this has on the model. Does a bigger step size make for more or less efficient scattering?

Try varying INITIAL-RADIUS to make the starting formation tighter or more spread out.

Pen-down one turtle, and watch its path as it moves about the world. How does it compare with the group?

Which rule seems to simulate real situations of scattering people?

## EXTENDING THE MODEL

Try to think of other rules that scatterers follow. For example, figure out rules to get the turtles to fill up the entire space. Alternatively, turtles could move towards a friend or away from an enemy. Also, try to think of stopping conditions other than being a certain distance from other turtles.

Turtles could have a variable `stubbornness`, indicating how likely a turtle is to move or not. A `dawdle` or `friendliness` variable could indicate a likeliness to hang around other turtles.

Try giving the scatterers the ability to back track. For instance, if a scatterer moving to an open space finds that their movement takes them to a more populated area, they could move back to where they were before.

Allow turtle to have varying speeds or step sizes, simulating the differing stride lengths of individual scatterers

Devise other ways to model the scattering in the aggregate sense; for example, measure the density in a certain area.

What if there was a location (e.g. a fire) that people were trying to get away from while scattering?  Devise a way to model such a situation.  You can include physical structures, such as walls and doors.

## NETLOGO FEATURES

The model creates turtles by asking patches to `sprout` a turtle initialized with a certain rule. Because a patch can only `sprout` one turtle, only a certain number of turtles can fit in a certain radius. The model verifies that the user hasn't asked for more turtles than can fit in the initial-radius specified to avoid an error.

## CREDITS AND REFERENCES

The study is described in Levy, S.T., & Wilensky, U. (2004). Making sense of complexity: Patterns in forming causal connections between individual agent behaviors and aggregate group behaviors. In U. Wilensky (Chair) and S. Papert (Discussant) Networking and complexifying the science classroom: Students simulating and making sense of complex systems using the HubNet networked architecture. The annual meeting of the American Educational Research Association, San Diego, CA, April 12 - 16, 2004. http://ccl.northwestern.edu/papers/midlevel-AERA04.pdf

Thanks to Stephanie Bezold for her work on this model.
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
set too-close 1.5
set too-far 2.0
set num-open-min-max 50
setup
repeat 75 [ go ]
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

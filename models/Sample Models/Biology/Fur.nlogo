patches-own
[
  new-color       ;; currently, always either white or black
  inner-neighbors ;; other cells in a circle around the cell
  outer-neighbors ;; other cells in a ring around the cell (but usually not touching the cell)
]

to setup
  clear-all
  ;; computes inner and outer neighbors in an ellipse around each cell
  ask patches
  [
    set inner-neighbors ellipse-in inner-radius-x inner-radius-y
    ;; outer-neighbors needs more computation because we want only the cells in the circular ring
    set outer-neighbors ellipse-ring outer-radius-x outer-radius-y inner-radius-x inner-radius-y
  ]

  ifelse any? patches with [ count outer-neighbors = 0 ]
    [ user-message word "It doesn't make sense that 'outer' is equal to or smaller than 'inner.' "
                        " Please reset the sliders and press Setup again."
      stop]
    [restart]
  reset-ticks
end

;; this procedure sets approximately initial-density percent of the
;; cells white and the rest black; if initial-density is set at 50%
;; then about half the cells will be white and the rest black
to restart
  ask patches
    [ ifelse random-float 100.0 < initial-density
        [ set pcolor white ]
        [ set pcolor black ] ]
  reset-ticks
end

to go
  ask patches [ pick-new-color ]
  ask patches [ set pcolor new-color ]
  tick
end

to pick-new-color  ;; patch procedure
  let activator count inner-neighbors with [pcolor = white]
  let inhibitor count outer-neighbors with [pcolor = white]
  ;; we don't need to multiply 'activator' by a coefficient because
  ;; the ratio variable keeps the proportion intact
  let difference activator - ratio * inhibitor
  ifelse difference > 0
    [ set new-color white ]
    [ if difference < 0
        [ set new-color black ] ]
  ;; note that we did not deal with the case that difference = 0.
  ;; this is because we would then want cells not to change color.
end

;;; procedures for defining elliptical neighborhoods

to-report ellipse-in [x-radius y-radius]  ;; patch procedure
  report patches in-radius (max list x-radius y-radius)
           with [1.0 >= ((xdistance myself ^ 2) / (x-radius ^ 2)) +
                        ((ydistance myself ^ 2) / (y-radius ^ 2))]
end

to-report ellipse-ring [outx-radius outy-radius inx-radius iny-radius]  ;; patch procedure
  report patches in-radius (max list outx-radius outy-radius)
           with [1.0 >= ((xdistance myself ^ 2) / (outx-radius ^ 2)) +
                        ((ydistance myself ^ 2) / (outy-radius ^ 2))
             and 1.0 <  ((xdistance myself ^ 2) / (inx-radius ^ 2)) +
                        ((ydistance myself ^ 2) / (iny-radius ^ 2))
                ]
end

;; The following two reporter give us the x and y distance magnitude.
;; you can think of a point at the tip of a triangle determining how much
;; "to the left" it is from another point and how far "over" it is from
;; that same point. These two numbers are important for computing total distances
;; in elliptical "neighborhoods."

;; Note that it is important to use the DISTANCEXY primitive and not
;; just take the absolute value of the difference in coordinates,
;; because DISTANCEXY handles wrapping around world edges correctly,
;; if wrapping is enabled (which it is by default in this model)

to-report xdistance [other-patch]  ;; patch procedure
  report distancexy [pxcor] of other-patch
                    pycor
end

to-report ydistance [other-patch]  ;; patch procedure
  report distancexy pxcor
                    [pycor] of other-patch
end
@#$#@#$#@
GRAPHICS-WINDOW
309
10
624
346
30
30
5.0
1
10
1
1
1
0
1
1
1
-30
30
-30
30
1
1
1
ticks

BUTTON
205
60
294
93
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
218
254
289
287
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
7
221
179
254
initial-density
initial-density
0
100
50
1
1
%
HORIZONTAL

SLIDER
7
257
180
290
ratio
ratio
0
2
0.35
0.01
1
NIL
HORIZONTAL

BUTTON
217
216
289
249
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

BUTTON
216
179
289
212
NIL
restart
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
8
37
180
70
inner-radius-x
inner-radius-x
0
10
3
1
1
cells
HORIZONTAL

SLIDER
8
72
180
105
inner-radius-y
inner-radius-y
0
10
3
1
1
cells
HORIZONTAL

SLIDER
7
106
180
139
outer-radius-x
outer-radius-x
0
10
6
1
1
cells
HORIZONTAL

SLIDER
7
141
179
174
outer-radius-y
outer-radius-y
0
10
6
1
1
cells
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

Does a single mechanism underlies such diverse patterns such as the stripes on a zebra, the spots on a leopard, and the blobs on a giraffe?  This model is a possible explanation of how the patterns on animals' skin self-organize.  If the model is right, then even though the animals may appear to have altogether different patterns, the rules underlying the formation of these patterns are the same and only some of the parameters (the numbers that the rules work on) are slightly different.

Thinking of the formation of fur in terms of rules also helps us understand how offspring of animals may have the same type of pattern, but not the same exact pattern. This is because what they have inherited is the rules and the values rather than a fixed picture. The process by which the rules and values generate a pattern is affected by chance factors, so each individual's pattern is different, but as long as the offspring receive the same rules and values, their own fur will self organize into the same type of pattern as their parents'.

## HOW IT WORKS

We model the animal skin by a square array of many melanocytes (pigment cells) that are each in either of two states: colorful ('D' for differentiated) or not-colorful ('U' for undifferentiated).  The state of a cell can flip between D and U.  The color cells (the D's) secrete two types of 'morphogens': activators (A) and inhibitors (I).  Activators, on their own, cause a central cell to become colorful; inhibitors, on their own, cause the central cell to become not colorful.  These competing morphogens are secreted in all directions so you can think of each color cell as creating a puddle that grows around it, spreading to other cells.

Each cell, whether or not it is colorful, is itself the center of its own neighborhood. For now, suppose the neighborhood is a circle.  Say this circular neighborhood has a radius of 6 cells.  This means that the cell in the center can be affected by other cells that are as far as 6 cells away from it in any direction.  So if there is a D cell within this circle and it is secreting morphogens then these morphogens will diffuse as far as this central cell (but a D cell 7 cells away will not directly or immediately affect it).  Also, each cells has an inner circle of radius, say, 3 cells.

D cells within the inner circle each contributes morphogens of type A (activator) to the central cell.  Between the inner circle and the perimeter of the outer circle we have a ring of cells that are more than 3 cells away from the center but 6 or less cells away from the center.  Every D cell in this outer ring contributes morphogens of type I (inhibitor) to the central cell.  So at every moment each cell is affected both by activator and inhibitor cells in its circle and the question is will it ultimately be activated and become colorful or inhibited and lose its color (or just remain the way it was).  The logic is that if the power of the activators is bigger than the power of the inhibitors then the cell will become colorful and vice versa (and if the power is balanced then nothing happens).  The idea of "power" is that it's not enough to know how many morphogens there are of each type affecting a cell but one must multiply each cell by its "power" (or you can think of power in terms of the concentration of the morphogens in the inner and outer neighborhoods).  Another idea is that since we'll be multiplying both types of morphogens by their power, we might as well just call the power of the activators "1" and the power of the inhibitors "w * 1" or just w.  So w is the ratio between the power of the inhibitors and the activators.  If w is bigger than 1 that means the power of the inhibitors is greater than that of the activators (for instance, if w = 2 then the inhibitors are each double as strong as each of the activators and if w = 0.5 then the inhibitors are half as strong as the activators).  If w = 0.5 and if we have as many inhibitors as we have activators that are affecting the central cell, we would logically assume that the center cells would be more activated than inhibited and so would probably become (or remain) colorful on that step.  (A tricky point to notice is that while a certain D-cell is activating a neighboring cell, this same D-cell can be inhibiting a different cell further away.)

Here are the rules that summarize what we've been discussing: count up all the D cells in the ring and call this number D*I (for instance 2 inhibitors), and count up all the D cells in the circle of radius three and call this number D*A (for instance, 5 activators).  Then compute D*A - w*D*I, and:
* if it is > 0, set the central cell to D
* if it is < 0, set the central cell to U
* if it is = 0, leave the central cell unchanged

Note that this computation happens to all cells at the same time.  After the first step and once the cells have been set accordingly, the entire business starts over at the next step. Once again, the cells are counted up according to the same rule.  The rules have not changed but because some of the D cells are now U and vice versa we might get different counts and because of that -- different results of the "fight" between the A and I morphogens.

So what you see is that from step to step the individual cells often change from white (representing D or color cells) to black (representing U or no-color cells) and the overall impression is that the configuration of white and black changes as a whole.  But these configurations are not random.  You will see how these configurations often take form.  Understanding how each cell behaves, as we have explained above, can help understanding how these global patterns take form.

All these explanations were for circular neighborhoods.  In this model, the neighborhoods may be elliptical instead of circular.  This is needed to produce stripes instead of spots.

## HOW TO USE IT

In order that your first experiment will more-or-less match the explanations above, you should choose to set the initial-density slider to 50% (that gives each cell an equal chance of being white or black to start with and so the whole window will be roughly 50% white), set the INNER-RADIUS-X and INNER-RADIUS-Y sliders to 3 and the OUTER-RADIUS-X and OUTER-RADIUS-Y sliders to 6, and set RATIO to 0.35 (that means the I morphogens are 35% as powerful as the A morphogens).  Now press SETUP. (In later experiments you are welcome to change those settings in various combinations.)  It will take a while to complete.  If you press STEP the model will advance a single step.  If you press GO the model will keep stepping indefinitely.

It takes a while for the patches to determine their neighborhoods.  Because of this, only press SETUP when you change the radius sliders.  If you only change the INITIAL-DENSITY and RATIO sliders or if you'd like to run the model again with the same settings, press RESTART instead of SETUP.  The RESTART button doesn't ask the patches to recalculate their neighborhoods.

## THINGS TO NOTICE

As the model runs, patterns may begin to emerge.  Eventually, they stabilize.  (Well, sometimes the model will run into an endless flip-flop between two states, but we could call that dynamic stability.)  Even when it seems to come to a halt, the model is still running and executing the commands and carrying out the computations, but nothing is changing visibly.  This is because for each and every cell the power of activators is equal to that of the inhibitors, so nothing changes.

## THINGS TO TRY

Run the model with different INITIAL-DENSITY settings.  How, if at all, does the value of the INITIAL-DENSITY affect the emergent pattern?  Do you get the same pattern?  Do you get a different pattern?  Does it take longer?

Note how fragile the self organization of the cells is to slight changes in parameters. If you hold all other factors and slightly change just the RATIO, from trial to trial, you will note that for small ratios you will invariably get completely white fur and for high ratios you will invariably get completely black fur (why is that?).  For ratios in between it fluctuates.  That happens partially because the initial setting of black/white coloration has a random element to it (see the RESTART procedure in the code).

Try changing the sliders to have different values in the X and Y directions.

## EXTENDING THE MODEL

If you find a combination of slider and switch values that consistently give you the fur patterns of a favorite animal, you could create a button, for instance "Zebra," that sets the sliders to those values. That way, if you make several of these, you can go on a virtual safari tour by moving between your favorite animals. One such combination that you could set in a single button could be: INNER-RADIUS-X 3, INNER-RADIUS-Y 3, OUTER-RADIUS-X 6, OUTER-RADIUS-Y 6, INITIAL-DENSITY 50%, RATIO 0.35.

You could call this, perhaps, Fish.

How about adding more colors? What could be the logic here? If you introduced, say, red, you would have to decide on specific conditions under which that color would appear. Also, you'd have to decide how that color influences other cells.

## RELATED MODELS

Voting, in the Social Science section, is based on simpler rules but generates patterns that are similar in some respects.

## CREDITS AND REFERENCES

The mechanism of "diffusion-driven instability" was first proposed by Alan Turing in 1952. B.N. Nagorcka first proposed applying it to hair and fur. The particular variant presented in this model was proposed by David Young.

In building this model, we used information on this web site: http://classes.yale.edu/fractals/Panorama/Biology/Leopard/Leopard.html

Thanks to Seth Tisue and Dor Abrahamson for their work on this model.
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

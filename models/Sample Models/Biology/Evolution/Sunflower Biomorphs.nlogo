;; spawners are hidden turtles at the center of each "flower" that
;; are always hatching the petals you actually see
breed [spawners spawner]
breed [petals petal]

globals [
  first-parent     ;; the first parent chosen if sexual reproduction is being used
]

spawners-own [
  num-colors       ;; how many colors the petals will have
  step-size        ;; how fast petals move out (the flower's rate of growth)
  turn-increment   ;; how much each petal is rotated before moving out from
                   ;; the center; for example, a turn-increment of 0 will
                   ;; cause all the petals to move out on the same line
  size-modifier    ;; how quickly the petals grow as they move away from
                   ;; their starting location
]

petals-own [
  step-size        ;; same as for spawners
  size-modifier    ;; same as for spawners
  parent           ;; spawner that spawned this petal; distance from parent
                   ;; is used for calculating the petal's size as it grows
]

to setup
  clear-all
  create-spawners rows * columns
  [
    set num-colors random 14 + 1
    set step-size random-float 0.5
    set turn-increment random-float 4.0
    set size-modifier random-float 2.0
    hide-turtle        ;; we don't want to see the spawners
  ]
  arrange-spawners
  set first-parent nobody
  reset-ticks
end

to arrange-spawners
  ;; arrange the spawners around the world in a grid
  let i 0
  while [i < rows * columns]
  [
    ask turtle i
    [
      let x-int world-width / columns
      let y-int world-height / rows
      setxy (-1 * max-pxcor + x-int / 2 + (i mod columns) * x-int)
            (max-pycor + min-pycor / rows - int (i / columns) * y-int)
    ]
    set i i + 1
  ]
end

to go
  ask spawners
  [
    hatch-petals 1
    [
      set parent myself
      set color 10 * (ticks mod ([num-colors] of parent + 1)) + 15
      rt ticks * [turn-increment] of parent * 360
      set size 0
      show-turtle  ;; the petal inherits the hiddenness of its parent,
                   ;; so this makes it visible again
    ]
  ]
  ask petals
  [
    fd step-size
    set size size-modifier * sqrt distance parent
    ;; Kill the petals when they would start interfering with petals from other flowers.
    if abs (xcor - [xcor] of parent) > max-pxcor / (columns * 1.5) [ die ]
    if abs (ycor - [ycor] of parent) > max-pycor / (rows * 1.5) [ die ]
  ]
  tick
  if mouse-down? [ handle-mouse-down ]
end

to repopulate-from-two [parent1 parent2]
  ask petals [ die ]
  ask spawners
  [
    ;;if controlled-mutation? then the mutation a flower experiences is relative to its spawner's who number.
    if controlled-mutation? [set mutation who * 1 / (rows * columns)]

    ;; select one value from either parent for each of the four variables
    set num-colors ([num-colors] of one-of list parent1 parent2) + int random-normal 0 (mutation * 10) mod 15 + (1)
    set step-size ([step-size] of one-of list parent1 parent2) + random-normal 0 (mutation / 5)
    set turn-increment ([turn-increment] of one-of list parent1 parent2) + random-normal 0 (mutation / 20)
    set size-modifier ([size-modifier] of one-of list parent1 parent2) + random-normal 0 mutation

    ;;We clamp size-modifier so none of the sunflowers get too big.
    if size-modifier > 1.5 [set size-modifier 1.5]
  ]
end

to repopulate-from-one [parent1]
  ask petals [ die ]
  ask spawners
  [
    if controlled-mutation? [ set mutation who * 1 / (rows * columns) ]
    set num-colors ([num-colors] of parent1 + int random-normal 0 (mutation * 10)) mod 15 + 1
    set step-size [step-size] of parent1 + random-normal 0 (mutation / 5)
    set turn-increment [turn-increment] of parent1 + random-normal 0 (mutation / 20)
    set size-modifier [size-modifier] of parent1 + random-normal 0 mutation

    ;;We clamp size-modifier so none of the sunflowers get too big.
    if size-modifier > 1.5 [ set size-modifier 1.5 ]
  ]
end

to handle-mouse-down
  ;; get the spawner closest to where the user clicked
  let new-parent min-one-of spawners [distancexy mouse-xcor mouse-ycor]
  ifelse asexual?
  [ repopulate-from-one new-parent ]
  [
    ifelse first-parent != nobody
    [
      repopulate-from-two first-parent new-parent
      set first-parent nobody
      ask patches [ set pcolor black ]
    ]
    [
      set first-parent new-parent
      ask patches
      [
        ;; This is a little tricky; some patches may be equidistant
        ;; from more than one spawner, so we can't just ask for the
        ;; closest spawner, we have to ask for all the closest spawners
        ;; and then see if the clicked spawner is among them
        if member? new-parent spawners with-min [distance myself]
          [ set pcolor gray - 3 ]
      ]
    ]
  ]
  ;; wait for the user to release mouse button
  while [mouse-down?] [ ]
end
@#$#@#$#@
GRAPHICS-WINDOW
198
10
628
461
17
17
12.0
1
10
1
1
1
0
0
0
1
-17
17
-17
17
1
1
1
ticks
30.0

BUTTON
11
107
94
140
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
107
177
140
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
33
182
66
rows
rows
1.0
10.0
5
1.0
1
NIL
HORIZONTAL

SLIDER
10
67
182
100
columns
columns
1.0
10.0
5
1.0
1
NIL
HORIZONTAL

SLIDER
10
145
182
178
mutation
mutation
0.0
1.0
0.14
0.02
1
NIL
HORIZONTAL

SWITCH
10
193
132
226
asexual?
asexual?
0
1
-1000

SWITCH
10
227
190
260
controlled-mutation?
controlled-mutation?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

A model of evolution where the user provides the selective pressure.  The user picks one or two flowers from a grid of randomly generated animated flowers.  The selected flowers become the "parents" of the next generation.  Over time, the user's selections cause the characteristics of the population to change.

## HOW IT WORKS

Each flower has four genes that determine its form.  For example, one flower may have bigger petals or be more colorful than another.  When the user  selects the parent or parents of the next generation, and the screen is then cleared and the flowers repopulated with genes based on the genes of their parent(s).  The user can exert selection pressure towards colorful flowers, for example, by choosing the most colorful flower(s) available.

At the center of every "flower" is an invisible "spawner".  It remains in the center continuously hatching "petals" that move outward.  Each spawner holds the four variables: num-colors, step-size, size-modifier, and turn-increment.  (Since each petal needs to know how far forward to move each time and how large to grow, they have their own step-size and size-modifier that is identical to the spawner's value that spawned them.)  Each turn, each spawner hatches a new petal and turns and sets its color appropriately (based on the values for num-colors and turn-increment.)  Once the spawners have all hatched a new petal, each petal simply moves forward its step-size and adjusts its size based on the distance its traveled and its size-modifier.  If a petal moves outside the box for that flower, it dies.

The user may choose between asexual (one parent) and sexual (two parents) reproduction.  The next generation is created by mutating the values of the parent(s) for the four variables.  The existing petals are then killed off, and the next generation is created.

## HOW TO USE IT

Before clicking SETUP, set the ROWS and COLUMNS to the desired values.  These determine how many flowers are present in the model.

Then set the mutation rate appropriately.  The higher the mutation rate, the less flowers in the next generation will resemble their parents.  If CONTROLLED-MUTATION? is on, then the model controls the mutation by varying it within the population; in this mode, mutation varies among the individuals in the population; where the amount of MUTATION increases in each individual that is to the right of the previous one (or in the next row).  Thus, flowers near the top-left of the model will tend to more closely resemble their parents than flowers in the bottom right.

If asexual selection is selected (the ASEXUAL? switch is on), then every click will repopulate the flowers based on the values of the spawner nearest your click.  If sexual selection is selected, then the first click selects the first parent (and colors its background grey.)  When you click again, the two clicked spawners "mate" and populate the next generation.

## THINGS TO TRY

Try and get certain flowers patterns to emerge; for example, try to selectively breed towards a very tight spiral, a non-rotating "starfish", or a straight line.

Remember a particularly pleasing flower, then clear all of the flowers, and see if you can evolve your favorite flower again.

## EXTENDING THE MODEL

It might be neat to allow the user to change the number of flowers while the model is running.  For example, changing the model from a 2x2 grid to a 3x3 grid might put five new flowers into the population, all based on the previously selected parent.

Change the way the parents' genes are passed on in sexual reproduction and how they are expressed.

## NETLOGO FEATURES

This model uses a breed of invisible spawners to keep track of each flower.

## RELATED MODELS

Sunflower

## CREDITS AND REFERENCES

This model is loosely based on the Biomorphs discussed at length in Richard Dawkins' _The Blind Watchmaker_ (1986).

In Dawkins' original model, the user was presented with a series of "insects" that were drawn based on nine separate values.  For example, if insect A has a "leg-length" value of 2 and insect B has a "leg-length" value of 1, then insect A would be drawn with longer legs.  These 9 variables were thus the genotype of each insect-like creature, and the drawing based on those numbers was the phenotype.  If the user clicked on an insect (or "biomorph"), then all the insects would be erased and the chosen biomorph would be used as the basis for a new population of biomorphs.  Each variable would be mutated slightly in the new generation (representing the inheriting of a slightly higher or lower value for the genotype), and these mutated values would be used in the new population of the biomorphs.  In this manner, the new generation of  biomorphs resembled the previously chosen biomorph, with some variation.  For example, if you chose a biomorph with an exceptionally long abdomen, then, because they are all modified versions of the chosen biomorph, biomorphs in the next generation would tend to have longer abdomens than previously.

In this model, "flowers" are used as the biomorphs instead of the insect-like creatures Dawkins used; furthermore, these biomorphs only vary among four variables--num-color, step-size, size-modifier, and turn-increment--and not nine.  The idea is very similar, though.  The user is presented with a number of flowers.  By clicking on a flower, the user can choose the type of flower that will populate the next generation.  If ASEXUAL? is false, the user picks two biomorphs instead of just one; the next generation will be produced by selecting one the values for each of the four genotype variables from either one of the parents.

Thanks to Nate Nichols for his work on this model.
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
NetLogo 5.0beta3
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

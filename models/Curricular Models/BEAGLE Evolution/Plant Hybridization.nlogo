globals [
  greenhouse
  plant-shape-1
  plant-shape-2
  flower-shape-1
  flower-shape-2
  grabbed-turtle
]


breed [plants plant]
breed [seeds seed]

turtles-own [
  color-genotype
  height-genotype
  leaf-genotype
]

;;
;; Setup Procedures
;;

to setup
  clear-all
  set-default-shape seeds "seed"
  set greenhouse green - 2
  set plant-shape-1 "plant-tall"
  set plant-shape-2 "plant-short"

  set grabbed-turtle nobody

  setup-fields

  ask n-of initial-seeds patches [ add-one-wild-seed ]

  output-print "First press GO, Then click on different "
  output-print "locations on screen below to plant wild seeds."
  output-print "You can move seeds by clicking and dragging them"
  output-print "You can label seeds by enabling LABEL-SEEDS-AND-PLANTS? and clicking on the seed to label"
  output-print "When done planting, press NEXT SEASON."
  reset-ticks
end

to setup-fields
  ask patches [ set pcolor black ]
  ifelse use-greenhouse?
  [
    ask patches with [pxcor >= 6 and pycor >= 6] [ set pcolor greenhouse ]
    ask patch 11 6 [ set plabel "greenhouse plants" ]
  ]
  [ ask patch 11 6 [ set plabel "" ] ]
end


;;
;; Runtime Procedures
;;

to go
  run (word "do-" season-procedure)
end

to start-early-spring
  ask seeds
  [
    set shape "seed-sprouted"
    hatch-plants 1
    [
      set color phenotype-flower-color color-genotype
      set shape (word (phenotype-plant-height height-genotype) phenotype-leaf leaf-genotype)
      set size 0.2
    ]
  ]
end

to do-early-spring
  listen-select-field
  display
end

to start-late-spring
  repeat 40 [
    ask plants [ grow-plants ]
    display
  ]
  ask seeds [ die ]
end

to do-late-spring
  move-turtles plants patch mouse-xcor mouse-ycor
  display
end

to start-summer
  ask plants [ set size 1 ]
end

to do-summer
  listen-select-field
  display
end

to start-fall
  if user-yes-or-no? (word "Do you want to pollinate with bees?\n"
                           "(yes=randomly) (no=select which ones with mouse)")
  [ pollinate-with-bees ]
end

to do-fall
  listen-cross-fertilize-plants
  display
end

to start-winter
  clear-drawing
  ask plants with [not in-greenhouse?] [ die ]
end

to do-winter
  let p patch mouse-xcor mouse-ycor
  ifelse not any? seeds-on p and grabbed-turtle = nobody
  [ if add-wild-seeds? and mouse-inside?
    [ add-wild-seeds p ] ]
  [ move-turtles seeds p ]
  display
end

;;
;; Plant and Field Procedures
;;

to clear-field
  ask patches with [pcolor = blue]
  [
    ask plants-here [ die ]
    ask seeds-here [ die ]
  ]
  setup-fields
end

to add-wild-seeds [p]
  if not [in-greenhouse?] of p and not any? seeds-on p
  [
    ifelse mouse-down?
    [ ask patch mouse-xcor mouse-ycor [ add-one-wild-seed ] ]
    [ ask seeds [ set size 1 ] ]
  ]
end

to grow-plants
  if size < 1
  [ set size size + 1 / 50 ]
end

to pollinate-with-bees
  ask plants with [not in-greenhouse? and not any? in-link-neighbors]
  [
     let pollinator one-of plants
     pollinate pollinator
  ]
end

to-report in-greenhouse?
  report pcolor = greenhouse
end

;;
;; Mouse Procedures
;;

to listen-cross-fertilize-plants
  ifelse mouse-down?
  [
    if grabbed-turtle = nobody
    [ set grabbed-turtle one-of plants-on patch mouse-xcor mouse-ycor ]
  ]
  [
    if grabbed-turtle != nobody
    [
      let pollinated one-of (plants-on patch mouse-xcor mouse-ycor) with [not in-greenhouse? and not any? in-link-neighbors]
       if pollinated != nobody
      [ ask pollinated  [ if not any? seeds-here [pollinate grabbed-turtle ] ] ]
      set grabbed-turtle nobody
    ]
  ]
end

to listen-select-field
  if mouse-down?
  [
    setup-fields
    ask patch mouse-xcor mouse-ycor
    [
      set pcolor blue
      ask neighbors [ set pcolor blue ]
    ]
  ]
end

to move-turtles [b p]
  ifelse mouse-down? and mouse-inside?
  [
    ifelse label-seeds-and-plants?
    [
      ask (turtles-on p) with [breed = b]
      [ set label user-input (word "Label for " self ":") ]
    ]
    [
      ifelse grabbed-turtle = nobody
      [ set grabbed-turtle one-of (turtles-on p) with [breed = b] ]
      [ if not any? seeds-on p [ ask grabbed-turtle [ move-to p ] ] ]
    ]
    display
  ]
  [ set grabbed-turtle nobody ]
end


;;
;; Seed initialization procedures
;;

to pollinate [my-pollen]
  set shape (word shape "-fertilized")

  hatch-seeds 1
  [
    set color-genotype (word one-of-alleles [color-genotype] of my-pollen
                             one-of-alleles color-genotype)

    set height-genotype (word one-of-alleles [height-genotype] of my-pollen
                              one-of-alleles height-genotype)

    if random 100 < chance-mutation
    [ set height-genotype add-mutation-height height-genotype ]
    if random 100 < chance-mutation
    [ set color-genotype add-mutation-color color-genotype ]
  ]
  if show-fertilization-network?
  [
    ifelse my-pollen != self
    [ create-link-from my-pollen ]
    [
      hatch 1
      [

        set color gray  + 2
        set ycor ycor - 0.3
        pd

        set heading 90
        repeat 360
        [ fd (1 / 180) lt 1 ]
        die
      ]
    ]
  ]
end

to add-one-wild-seed
  sprout-seeds 1
  [
    set shape "seed-wild"
    set color-genotype assign-random-color-genotype
    set height-genotype assign-random-height-genotype
    set leaf-genotype assign-random-leaf-genotype
  ]
end

;;
;; Genotype reporters
;;

to-report assign-random-color-genotype
  report one-of ["WW" "RR" "WR" "RW"]
end

to-report assign-random-height-genotype
  report one-of ["TT" "tt" "Tt" "tT"]
end

to-report assign-random-leaf-genotype
  report one-of ["CC" "cc" "Cc" "cC"]
end

to-report add-mutation-color [genotype]
  report replace-item random 2 genotype one-of ["R" "W"]
end

to-report add-mutation-height [genotype]
  report replace-item random 2 genotype one-of ["T" "t"]
end

to-report add-mutation-leaf [genotype]
  report replace-item random 2 genotype one-of ["C" "c"]
end

to-report phenotype-flower-color [genotype]
  ifelse member? "R" genotype
  [
    ifelse member? "W" genotype
    [ report pink ]
    [ report red ]
  ]
  [ report white ]
end

to-report phenotype-plant-height [genotype]
  if genotype = "tt" [ report plant-shape-2]
  report plant-shape-1
end

to-report phenotype-leaf [genotype]
  if genotype = "cc" [report "-varigated"]
  report ""
end

to-report one-of-alleles [genotype]
  report item random 2 genotype
end

;;
;; Instruction-related reporters
;;

to next-season
  setup-fields
  tick
  print-instructions
  run (word "start-" season-procedure)
end

to print-instructions
  clear-output
  output-print (word "It is now " season-name ".")
  output-print instructions
  output-print "Or you may press NEXT SEASON.\n"
end

to-report instructions
  report item season
  (list (word "The plants have died and dropped their newly formed seeds.\n"
              "You may move the seeds to new locations or plant new wild\n"
              "seeds with using the mouse")
        "The seeds are germinating.\nYou may select a field of seeds to clear if you wish."
        "The plants are growing.\nYou may transplant the plants using the mouse."
        "You may select a field of plants to clear\nusing the mouse and the CLEAR SELECTED button."
        (word "The plants are ready for fertilization.\n"
              "If you did not pollinate with bees you can pollinate using the mouse.\n"
              "Click on the pollinator, hold the mouse button down and drag to the plant to pollinate\n"
              "When you are done pollinating you may press POLLINATE-WITH-BEES,\n"
              "which will randomly pollinate the remaining plants\n"
              "(note that plants that are not pollinated will not come back next year) or pre NEXT-SEASON") )
end

to-report season-name
  report item season [ "winter" "early spring" "late spring" "summer" "fall"]
end

to-report season-procedure
  report item season [ "winter" "early-spring" "late-spring" "summer" "fall"]
end

to-report season
  report ticks mod 5
end

to-report year
  report floor (ticks / 5)
end
@#$#@#$#@
GRAPHICS-WINDOW
243
189
709
676
-1
-1
38.0
1
14
1
1
1
0
0
0
1
0
11
0
11
1
1
0
ticks

BUTTON
40
80
110
113
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
115
80
185
113
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
25
20
200
53
initial-seeds
initial-seeds
0
100
50
1
1
NIL
HORIZONTAL

BUTTON
40
115
185
148
NIL
next-season
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
20
450
225
483
show-fertilization-network?
show-fertilization-network?
0
1
-1000

MONITOR
115
175
200
220
NIL
year
0
1
11

MONITOR
25
175
110
220
NIL
season-name
3
1
11

OUTPUT
226
10
918
173
12

SWITCH
35
355
195
388
add-wild-seeds?
add-wild-seeds?
0
1
-1000

SWITCH
20
483
225
516
label-seeds-and-plants?
label-seeds-and-plants?
1
1
-1000

BUTTON
40
285
185
318
clear-selected
clear-field
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
35
320
195
353
use-greenhouse?
use-greenhouse?
0
1
-1000

SLIDER
20
405
215
438
chance-mutation
chance-mutation
0
100
0
1
1
NIL
HORIZONTAL

BUTTON
40
250
185
283
NIL
pollinate-with-bees
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

This model lets you conduct experiments in Mendelian genetics with cross fertilization in a population of flowering plants.

A population of plants is used to show two sets of patterns of gene expression.  One of these set of patterns is between a dominant and recessive forms (alleles) of a gene.  The other is between two co-dominant genes.

This population of plants can be fertilized in the wild using bees to transmit pollen from one plant to another, or the user can artificially cross fertilize some or all the plants by hand (using the mouse to transfer pollen from one plant to another).  After fertilization, seeds immediately form.  Once fertilized, the same plant can not be fertilized again in that same growing season.

Plants drop the seeds they create in the same location the parent plant was growing.  The user can allow these seeds of the plants to grow in the same location or move, organize, and label the seeds to make better sense of the outcomes of various fertilization events, in the following season of plant growth.

Also the user can move plants to the greenhouse location to "over winter" the plants so that they do not die at the end of the growing season.  Keeping a plant in the greenhouse, ensures that it can be used in future seasons for additional cross fertilization experiments.  But plants in the greenhouse are only permitted to cross-fertilize their pollen to plants outside the greenhouse.  Greenhouse plants can not be fertilized, unless they are moved outside the greenhouse.

## HOW IT WORKS

Plants have two possible genes that determine their expression of flower color:  W or R.  The genotype and phenotype each plant follows the following co-dominant gene expression patterns:

WW - white flower
WR or RW - pink flower
RR - red flower

Plants have two possible genes that determine their expression of plant height:  T or t.  The genotype and phenotype each plant follows the following dominant-recessive gene expression patterns:

TT or Tt or tT - tall
tt - small

Plants have two possible genes that determine their expression of leaf pattern:  C or c.  The genotype and phenotype each plant follows the following dominant-recessive gene expression patterns:

CC or Cc or cC - solid green leaf
cc - variegated green and yellow leaf


## HOW TO USE IT

Buttons:
SETUP - sets up the initial conditions set on the sliders.
GO - runs and stops the model.
NEXT SEASON advances the season.  You must press this button to move the plants through their different stages of growth and reproduction.
CLEAR SELECTED - clears any plants or seeds from the currently selected field.
POLLINATE WITH BEES - randomly pollinates any plants that are not yet pollinated

Sliders:
INITIAL-SEEDS sets the number of initial seeds that appear randomly distributed in the world.
CHANCE-MUTATION sets the chance that a mutation is introduced when plants are fertilized from the known set of available genes W, R, T, t, C, and c.  If a mutation is added, the result may be the genotype does not actually change, if the gene that was randomly replaced is replaced with the same gene as before or if the overall genotype has a different order for the genes, but not a different combination.

Switches:
SHOW-FERTILIZATION-NETWORK shows lines between which plants have cross-fertilized.  A circle represents self-fertilization
LABEL-SEEDS-AND-PLANTS? allows the user to assign a label to any seed or plant they move.  The label will be inherited by the next generations of seeds the plant produces.
ADD-WILD-SEEDS? allows the user to add new random seeds during the fall or winter.
USE-GREENHOUSE?  when on a section of the world will be designated as the greenhouse when SETUP is pressed.

Running through the seasons:

On SETUP, a number of seeds (set by INITIAL-SEEDS) are randomly distributed about a garden.  These seeds carry three genes for each trait they express.  One pair of genes is for the expression of plant height.  The next pair of genes is for the expression of flower color.  The last pair of genes is for leaf pattern.

When the user presses GO and then clicks on the NEXT SEASON button, the seeds begin to advance through different stages of plant growth.  The five seasons used in the model are shown in the SEASON-NAME monitor:  early spring, late spring, summer, fall, and winter.

In each season the user can do different things with the plants.

In early spring, the seeds germinate and begin the process of forming a new plant.  In this season, the user can select any of the fields of plants and clear all the seeds out of that field.

In late spring, the plants grow large enough to see and the user can click and drag the plants to new locations (transplanting them).

In summer, the plants are fully grown and their expressed traits are now visible.  Here again the user can select any of the fields of plants and clear all the seeds out of that field.

In the fall the plants are ready to cross-fertilize or self-fertilize.  Plants have both female and male plant structures.  When you enter fall you will be asked if you want to pollinate with bees (randomly).  You can answer YES and all the plants will be cross-pollinate.  If you answer NO you can cross-pollinate using the mouse and you can later have the remaining plants cross-pollinate by pressing the POLLINATE WITH BEES button.  To pollinate using the mouse click on a plant, hold the mouse button down and drag to another plant. Once a plant is fertilized, its flower closes up and a seed appears.  It may no longer be fertilized by pollen, but it may continue to offer its pollen for the fertilization of other plants.  Plants in the greenhouse (the upper right green quadrant of the world) are only permitted to cross-fertilize their pollen to plants outside the greenhouse.  Greenhouse plants can not be fertilized, unless they are moved outside the greenhouse.

In the winter, the user can move the seeds that resulted from pollination to new locations by clicking and holding the mouse button down when the mouse cursor is positioned over a seed.  The seed may be dragged to an open patch, but may not be placed on a patch where a seed already is located.  Once the mouse button is released, the seed is dropped in the open patch under it.  If the user turns the ADD-WILD-SEEDS? switch on, then the user may also click on open patches to add new wild seeds to the garden plot.  These have a white box around them to help distinguish them from seeds that were created through a previous fertilization event on in the world.

In the early spring, the cycle of plant growth starts all over again from the seeds remaining in the garden

## THINGS TO NOTICE

What happens when you cross pollinate two pink plants?  Are the results always the same?

What must be true about the two parents for the offspring of them to always end up with a white flowered plant?

When you cross pollinate a tall plant with a tall plant, are all the offspring tall?  Are most?  What percentage or fraction of the offspring is tall?

Is there any fertilization plan you can find that will guarantee that you end up with all tall plants?

In the wild, what percentage or fraction of the plants are short and white?

## THINGS TO TRY

Try to self-pollinate many of the plants to see what outcome those give.  Continue self pollinating the plants until they breed true - which is to say, they consistently give the same characteristics in the next generation.

Move the plants and seeds into different areas of the screen to conduct different experiments in those areas.  Label your plants to describe the parent's characteristics.

## EXTENDING THE MODEL

Plants could drop multiple seeds (e.g. four or five) like in a pea pod of a pea plant.

Mendel used pea plants to conduct similar experiments.  In his peas, flower color was not co-dominant, it was dominant vs. recessive for white vs. violet.

Mendel also discovered other traits that follow these types of relationships:

The inflated vs. wrinkled seeds.
The color of the seed (green vs. yellow).
Inflated vs. wrinkled seed pod.
The color of the seed pod (green vs. yellow)
The distribution of the flowers along the stem.

## NETLOGO FEATURES

An output area is used to give instructions to the user.

The model makes heavy use of the MOUSE-* and USER-* primitives to allow the user to interact with the model.

## CREDITS AND REFERENCES

Other references to Gregor Mendel's 1865 paper "Versuche ueber Pflanzen-Hybriden" and a revised version of the English translation by C.T. Druery and William Bateson, "Experiments in Plant Hybridization", can be found at http://www.mendelweb.org/Mendel.html
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

arrow-head
true
0
Polygon -7500403 true true 150 120 120 165 180 165

arrow-line
true
0
Line -7500403 true 150 0 150 300

bee
true
0
Polygon -1184463 true false 195 150 105 150 90 165 90 225 105 270 135 300 165 300 195 270 210 225 210 165 195 150
Rectangle -16777216 true false 90 165 212 185
Polygon -16777216 true false 90 207 90 226 210 226 210 207
Polygon -16777216 true false 103 266 198 266 203 246 96 246
Polygon -6459832 true false 120 150 105 135 105 75 120 60 180 60 195 75 195 135 180 150
Polygon -6459832 true false 150 15 120 30 120 60 180 60 180 30
Circle -16777216 true false 105 30 30
Circle -16777216 true false 165 30 30
Polygon -7500403 true true 120 90 75 105 15 90 30 75 120 75
Polygon -16777216 false false 120 75 30 75 15 90 75 105 120 90
Polygon -7500403 true true 180 75 180 90 225 105 285 90 270 75
Polygon -16777216 false false 180 75 270 75 285 90 225 105 180 90
Polygon -7500403 true true 180 75 180 90 195 105 240 195 270 210 285 210 285 150 255 105
Polygon -16777216 false false 180 75 255 105 285 150 285 210 270 210 240 195 195 105 180 90
Polygon -7500403 true true 120 75 45 105 15 150 15 210 30 210 60 195 105 105 120 90
Polygon -16777216 false false 120 75 45 105 15 150 15 210 30 210 60 195 105 105 120 90
Polygon -16777216 true false 135 300 165 300 180 285 120 285

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
Circle -7500403 false true 15 15 270

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

greenhouse
false
0
Rectangle -7500403 false true 0 0 300 300
Line -7500403 true 45 0 0 45
Line -7500403 true 75 0 30 45
Line -7500403 true 105 0 60 45
Line -7500403 true 135 0 90 45
Line -7500403 true 165 0 120 45
Line -7500403 true 195 0 150 45
Line -7500403 true 225 0 180 45
Line -7500403 true 255 0 210 45
Line -7500403 true 285 0 240 45

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

line-half
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

plant-short
false
0
Circle -7500403 true true 100 162 38
Circle -7500403 true true 177 130 38
Circle -7500403 true true 100 100 38
Circle -7500403 true true 162 100 38
Circle -7500403 true true 85 130 38
Circle -7500403 true true 130 85 38
Polygon -10899396 true false 180 255 210 210 255 210 285 240 225 240
Polygon -10899396 true false 165 165 150 195 165 225 165 270 150 300 165 300 180 270 180 225 165 195
Polygon -10899396 true false 165 285 135 240 90 240 60 270 120 270
Circle -7500403 true true 130 177 38
Circle -7500403 true true 96 96 108
Circle -7500403 true true 162 162 38
Circle -16777216 true false 120 120 60

plant-short-fertilized
false
3
Polygon -10899396 true false 180 255 210 210 255 210 285 240 225 240
Polygon -10899396 true false 165 165 150 195 165 225 165 270 150 300 165 300 180 270 180 225 165 195
Polygon -10899396 true false 165 285 135 240 90 240 60 270 120 270

plant-short-varigated
false
0
Circle -7500403 true true 100 162 38
Circle -7500403 true true 177 130 38
Circle -7500403 true true 100 100 38
Circle -7500403 true true 162 100 38
Circle -7500403 true true 85 130 38
Circle -7500403 true true 130 85 38
Polygon -1184463 true false 180 255 210 210 255 210 285 240 225 240
Polygon -13840069 true false 165 165 150 195 165 225 165 270 150 300 165 300 180 270 180 225 165 195
Polygon -1184463 true false 165 285 135 240 90 240 60 270 120 270
Circle -7500403 true true 130 177 38
Circle -7500403 true true 96 96 108
Circle -7500403 true true 162 162 38
Circle -16777216 true false 120 120 60
Line -13840069 false 135 255 90 255
Line -10899396 false 210 225 255 225
Line -13840069 false 180 255 225 240
Line -13840069 false 225 240 285 240

plant-short-varigated-fertilized
false
3
Polygon -1184463 true false 180 255 210 210 255 210 285 240 225 240
Polygon -13840069 true false 165 165 150 195 165 225 165 270 150 300 165 300 180 270 180 225 165 195
Polygon -1184463 true false 165 285 135 240 90 240 60 270 120 270
Line -13840069 false 135 255 90 255
Line -13840069 false 255 225 210 225
Line -13840069 false 180 255 225 240
Line -13840069 false 225 240 285 240

plant-tall
false
0
Polygon -10899396 true false 165 150 150 120 150 75 135 120
Circle -7500403 true true 100 72 38
Circle -7500403 true true 130 87 38
Circle -7500403 true true 177 40 38
Circle -7500403 true true 100 10 38
Circle -7500403 true true 162 10 38
Circle -7500403 true true 162 72 38
Circle -7500403 true true 85 40 38
Circle -7500403 true true 130 -5 38
Circle -7500403 true true 96 6 108
Circle -16777216 true false 120 30 60
Polygon -10899396 true false 195 240 225 195 270 195 300 225 240 225
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 146
Polygon -10899396 true false 174 173 204 128 234 113 264 128 219 158
Polygon -10899396 true false 165 285 135 240 90 240 60 270 120 270
Polygon -10899396 true false 180 180 120 150 90 165 60 195 120 180

plant-tall-fertilized
false
3
Polygon -10899396 true false 165 150 150 120 150 75 135 120
Polygon -10899396 true false 195 240 225 195 270 195 300 225 240 225
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 146
Polygon -10899396 true false 174 173 204 128 234 113 264 128 219 158
Polygon -10899396 true false 165 285 135 240 90 240 60 270 120 270
Polygon -10899396 true false 180 180 120 150 90 165 60 195 120 180

plant-tall-varigated
false
0
Polygon -13840069 true false 165 150 150 120 150 75 135 120
Circle -7500403 true true 100 72 38
Circle -7500403 true true 130 87 38
Circle -7500403 true true 177 40 38
Circle -7500403 true true 100 10 38
Circle -7500403 true true 162 10 38
Circle -7500403 true true 162 72 38
Circle -7500403 true true 85 40 38
Circle -7500403 true true 130 -5 38
Circle -7500403 true true 96 6 108
Circle -16777216 true false 120 30 60
Polygon -1184463 true false 195 240 225 195 270 195 300 225 240 225
Polygon -13840069 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 146
Polygon -1184463 true false 174 173 204 128 234 113 264 128 219 158
Polygon -1184463 true false 165 285 135 240 90 240 60 270 120 270
Polygon -1184463 true false 180 180 120 150 90 165 60 195 120 180
Line -13840069 false 135 255 90 255
Line -13840069 false 180 165 225 135
Line -13840069 false 225 210 270 210
Line -13840069 false 135 165 105 165
Line -13840069 false 195 240 240 225
Line -13840069 false 240 225 300 225
Line -13840069 false 60 195 120 180
Line -13840069 false 120 180 180 180

plant-tall-varigated-fertilized
false
3
Polygon -13840069 true false 165 150 150 120 150 75 135 120
Polygon -1184463 true false 195 240 225 195 270 195 300 225 240 225
Polygon -13840069 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 146
Polygon -1184463 true false 174 173 204 128 234 113 264 128 219 158
Polygon -1184463 true false 165 285 135 240 90 240 60 270 120 270
Polygon -1184463 true false 180 180 120 150 90 165 60 195 120 180
Line -13840069 false 135 165 105 165
Line -13840069 false 135 255 90 255
Line -13840069 false 225 210 270 210
Line -13840069 false 180 165 225 135
Line -13840069 false 180 180 120 180
Line -13840069 false 120 180 60 195
Line -13840069 false 195 240 240 225
Line -13840069 false 240 225 300 225

pollen
true
0
Circle -7500403 true true 135 45 30
Circle -7500403 true true 180 105 30
Circle -7500403 true true 105 150 30
Circle -7500403 true true 195 165 30
Circle -7500403 true true 150 225 30
Circle -7500403 true true 30 135 30

seed
true
4
Circle -6459832 true false 120 120 60

seed-sprouted
true
4
Circle -6459832 true false 120 120 60
Polygon -10899396 true false 135 120 150 180 165 120

seed-wild
true
4
Circle -6459832 true false 120 120 60
Rectangle -1 false false 90 90 210 210

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
Line -7500403 true 150 150 120 165
Line -7500403 true 150 150 180 165

@#$#@#$#@
0
@#$#@#$#@

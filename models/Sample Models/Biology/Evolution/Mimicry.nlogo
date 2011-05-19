;; two breeds of butterflies
breed [ monarchs monarch ]
breed [ viceroys viceroy ]

breed [ birds bird ]
birds-own [ memory ]

globals [
  carrying-capacity-monarchs  ;; maximum population of monarchs in the world
  carrying-capacity-viceroys  ;; maximum population of viceroys in the world
  carrying-capacity-birds     ;; maximum population of birds in the world
  color-range-begin           ;; "lowest" color for a butterfly
  color-range-end             ;; "highest" color for a butterfly
  reproduction-chance         ;; The chance and individual has of
                              ;; reproducing (0 - 100) *after*
                              ;; the chance dependent on
                              ;; carrying capacity is evaluated.
]

;;
;; Setup Procedures
;;

to setup
  clear-all
  setup-variables
  setup-turtles
  reset-ticks
end

;; initialize constants
to setup-variables
  set carrying-capacity-monarchs 225
  set carrying-capacity-viceroys 225
  set carrying-capacity-birds 75
  set reproduction-chance 4
  set color-range-begin 15
  set color-range-end 109
end

;; create 75 birds and 450 butterflies of which half are
;; monarchs and half are viceroys.  Initially, the
;; monarchs are at the bottom of the color range and
;; the viceroys are at the top of the color range.
;; The patches are white for easy viewing.

to setup-turtles
  ask patches [ set pcolor white ]
  set-default-shape monarchs "butterfly monarch"
  set-default-shape viceroys "butterfly viceroy"
  create-birds carrying-capacity-birds
  [
    set color black
    set memory []
    set shape one-of ["bird 1" "bird 2"]
  ]
  create-monarchs carrying-capacity-monarchs [ set color red ]
  create-viceroys carrying-capacity-viceroys [ set color blue ]
  ;; scatter all three breeds around the world
  ask turtles [ setxy random-xcor random-ycor ]
end

;;
;; Runtime Procedures
;;

to go
  ask birds [ birds-move ]
  ;; turtles that are not birds are butterflies
  ask turtles with [breed != birds] [ butterflies-move ]
  ask turtles with [breed != birds] [ butterflies-get-eaten ]
  ask birds [ birds-forget ]
  ask turtles with [breed != birds] [ butterflies-reproduce ]
  tick
end

to birds-move ;; birds procedure
  ;; The birds are animated by alternating shapes
  ifelse shape = "bird 1"
  [ set shape "bird 2"]
  [ set shape "bird 1" ]
  set heading 180 + random 180
  fd 1
end

to butterflies-move ;; butterflies procedure
  rt random 100
  lt random 100
  fd 1
end

;; If there is a bird on this patch check the bird's memory
;; to see if this butterfly seems edible based on the color.
;; If the butterfly's color is not in the bird's memory
;; the butterfly dies.  If it's a monarch the bird remembers
;; that its color was yucky
to butterflies-get-eaten  ;; butterfly procedure
  let bird-here one-of birds-here
  if bird-here != nobody
  [
    if not [color-in-memory? [color] of myself] of bird-here
    [
      if breed = monarchs
        [ ask bird-here [ remember-color [color] of myself ] ]
      die
    ]
  ]
end

;; helper procedure that determines whether the given
;; color is in a bird's memory
to-report color-in-memory? [c] ;; bird procedure
  foreach memory [ if item 0 ? = c [ report true ] ]
  report false
end

;; put a color that was yucky in memory
to remember-color [c]  ;; bird procedure
  ;; birds can only remember 3 colors at a time
  ;; so if there are more than 3 in memory we
  ;; need to remove 1 we know that the first item
  ;; in the list will always be the oldest since
  ;; we add items to the back of the list and only
  ;; 1 item can be added per tick
  if length memory >= memory-size
  [ set memory but-first memory ]
  ;; put the new memory on the end of the list
  set memory lput (list c 0) memory
end

;; birds can only remember for so long, then
;; they forget. They remember colors for MEMORY-LENGTH
to birds-forget ;; bird procedure
  ;; first increment all of the times in memory
  set memory map [list (item 0 ?) (1 + item 1 ?)] memory
  ;; then remove any entries whose times have hit MEMORY-DURATION
  set memory filter [item 1 ? <= MEMORY-DURATION] memory
end

;; Each butterfly has an equal chance of reproducing
;; depending on how close to carrying capacity the
;; population is.
to butterflies-reproduce ;; butterfly procedure
  ifelse breed = monarchs
  [ if random count monarchs < carrying-capacity-monarchs - count monarchs
     [ hatch-butterfly ] ]
  [ if random count viceroys < carrying-capacity-viceroys - count viceroys
     [ hatch-butterfly ] ]
end

to hatch-butterfly ;; butterfly procedure
  if random-float 100 < reproduction-chance
  [
    hatch 1
    [
      fd 1
      ;; the chance that the butterfly will
      ;; have a random color is determined by
      ;; the MUTATION slider. select a base-color
      ;; between 15 and 105
      if random-float 100 < mutation-rate
      ;; make a list that contains only the base-color 15-105
      [ set color one-of sublist base-colors 1 10 ]
    ]
 ]
end
@#$#@#$#@
GRAPHICS-WINDOW
334
10
754
451
20
20
10.0
1
10
1
1
1
0
1
1
1
-20
20
-20
20
1
1
1
ticks
30.0

BUTTON
76
10
164
43
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
166
10
254
43
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
71
44
259
77
memory-duration
memory-duration
0
40
30
1
1
ticks
HORIZONTAL

SLIDER
71
78
259
111
mutation-rate
mutation-rate
0
100
5
1
1
NIL
HORIZONTAL

MONITOR
56
146
163
191
NIL
count monarchs
0
1
11

MONITOR
175
413
246
458
maximum
max [color] of monarchs
3
1
11

MONITOR
103
413
174
458
average
mean [color] of monarchs
3
1
11

MONITOR
164
146
271
191
NIL
count viceroys
0
1
11

MONITOR
175
459
246
504
maximum
max [color] of viceroys
3
1
11

MONITOR
103
459
174
504
average
mean [color] of viceroys
3
1
11

PLOT
33
192
310
412
Average Colors Over Time
Time
Average Color
0.0
100.0
0.0
105.0
true
true
"" ""
PENS
"Monarchs" 1.0 0 -2674135 true "" "plot mean [color] of monarchs"
"Viceroys" 1.0 0 -13345367 true "" "plot mean [color] of viceroys"

TEXTBOX
12
425
162
443
monarch colors:
11
0.0
1

TEXTBOX
17
470
167
488
viceroy colors:
11
0.0
1

MONITOR
247
413
318
458
minimum
min [color] of monarchs
17
1
11

MONITOR
247
459
318
504
minimum
min [color] of viceroys
17
1
11

SLIDER
71
112
259
145
memory-size
memory-size
0
10
3
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

Batesian mimicry is an evolutionary relationship in which a harmless species (the mimic) has evolved so that it looks very similar to a completely different species that isn't harmless (the model).  A classic example of Batesian mimicry is the similar appearance of monarch butterflies and viceroy moths. Monarchs and viceroys are unrelated species that are both colored similarly --- bright orange with black patterns. Their colorations are so similar, in fact, that the two species are virtually indistinguishable from one another.

The classic explanation for this phenomenon is that monarchs taste yucky.  Because monarchs eat milkweed, a plant full of toxins, they become essentially inedible to birds.  Researchers have documented birds vomiting within minutes of eating monarch butterflies.  The birds then remember the experience and avoid brightly colored orange butterfly/moth species.  Viceroys, although perfectly edible, avoid predation if they are colored bright orange because birds can't tell the difference.

Recent research now suggests that viceroys might also be unpalatable to bird predators, confusing this elegant explanation.  However, we have modeled the relationship anyway.  Batesian mimicry occurs in enough other situations (snakes, for example) that the explanation's general truth is unquestionable.  The monarch-viceroy story is so accessible --- and historically relevant --- that we believe it to be instructive even if its accuracy is now questioned.

## HOW IT WORKS

This model simulates the evolution of monarchs and viceroys from distinguishable, differently colored species to indistinguishable mimics and models.  At the simulation's beginning there are 450 monarchs and viceroys distributed randomly across the world.  The monarchs are all colored red, while the viceroys are all colored blue.  They are also distinguishable (to the human observer only) by their shape:  the letter "x" represents monarchs while the letter "o" represents viceroys.  Seventy-five birds are also randomly distributed across the world.

When the model runs, the birds and butterflies (for the remainder of this description "butterfly" will be used as a general term for monarchs and viceroys, even though viceroys are technically moths) move randomly across the world.  When a bird encounters a butterfly it eats the butterfly, unless it has a memory that the butterfly's color is "yucky."  If a bird eats a monarch, it acquires a memory of the butterfly's color as yucky.

As butterflies are eaten, they regenerate through asexual reproduction. Each turn, every butterfly must pass two "tests" in order to reproduce.  The first test is based on how many butterflies of that species already exist in the world. The carrying capacity of the world for each species is 225.  The chances of reproducing are smaller the closer to 225 each population gets.  The second test is simply a random test to keep reproduction in check (set to a 4% chance in this model).  When a butterfly does reproduce it either creates an offspring identical to itself or it creates a mutant.  Mutant offspring are the same species but have a random color between blue and red, but ending in five (e.g. color equals 15, 25, 35, 45, 55, 65, 75, 85, 95, 105).  Both monarchs and Viceroys have equal opportunities to reproduce mutants.

Birds can remember up to MEMORY-SIZE yucky colors at a time.  The default value is three.  If a bird has memories of three yucky colors and it eats a monarch with a new yucky color, the bird "forgets" its oldest memory and replaces it with the new one.  Birds also forget yucky colors after a certain amount of time.

## HOW TO USE IT

Each turn is called a TICK in this model.

The MEMORY-DURATION slider determines how long a bird can remember a color as being yucky.  The MEMORY-SIZE slider determines the number of memories a bird can hold in its memory at once.

The MUTATION-RATE slider determines the chances that a butterfly's offspring will be a mutant.  Setting the slider to 100 will make every offspring a mutant.  Setting the slider to 0 will make no offspring a mutant.

The SETUP button clears the world and randomly distributes the monarchs (all red), viceroys (all blue), and birds.  The GO button starts the simulation.

The number of monarchs and viceroys in the world are displayed in monitor as well as the maximum, minimum, and average colors for each type of butterfly.

The plot shows the average color of the monarchs and the average color of the viceroys plotted against time.

## THINGS TO NOTICE

Initially, the birds don't have any memory, so both monarchs and viceroys are eaten equally. However, soon the birds "learn" that red is a yucky color and this protects most of the monarchs.  As a result, the monarch population makes a comeback toward carrying capacity while the viceroy population continues to decline.  Notice also that as reproduction begins to replace eaten butterflies, some of the replacements are mutants and therefore randomly colored.

As the simulation progresses, birds continue to eat mostly butterflies that aren't red.  Occasionally, of course, a bird "forgets" that red is yucky, but a forgetful bird is immediately reminded when it eats another red monarch.  For the unlucky monarch that did the reminding, being red was no advantage, but every other red butterfly is safe from that bird for a while longer.  Monarch (non-red) mutants are therefore apt to be eaten.  Notice that throughout the simulation the average color of monarchs continues to be very close to its original value of 15.  A few mutant monarchs are always being born with random colors, but they never become dominant, as they and their offspring have a slim chance for survival.

Meanwhile, as the simulation continues, viceroys continue to be eaten, but as enough time passes, the chances are good that some viceroys will give birth to red mutants.  These butterflies and their offspring are likely to survive longer because they resemble the red monarchs.  With a mutation rate of 5%, it is likely that their offspring will be red too.  Soon most of the viceroy population is red.  With its protected coloration, the viceroy population will return to carrying capacity.

## THINGS TO TRY

If the MUTATION-RATE is high, advantageous color genes do not reproduce themselves.  Conversely, if MUTATION-RATE is too low, the chances of an advantageous mutant (red) viceroy being born are so slim that it may not happen enough, and the population may go extinct.  What is the most ideal setting for the MUTATION-RATE slider so that a stable state emerges most quickly in which there are red monarchs and viceroys co-existing in the world?  Why?

If the MEMORY-LENGTH slider is set too low, birds are unable to remember that certain colors are yucky.  How low can the MEMORY-LENGTH slider be set so that a stable state of co-existing red monarchs and viceroys emerges?

If you set MUTATION-RATE to 100 and MEMORY to 0, you will soon have two completely randomly colored populations.  Once the average color of both species is about 55, return the sliders to MUTATION-RATE equals 16 and MEMORY equals 30 without resetting the model.  Does a stable mimicry state emerge?  What is the "safe" color?

## EXTENDING THE MODEL

One very simple extension to this model is to add a RANDOM-COLOR button.  This button would give every butterfly in the world a random color.  The advantage of red would be gone, but some color (which could be red, or any other color) would eventually emerge as the advantageous color.  This models the evolutionary process from an earlier starting place, presumably when even monarchs had different colors.

It would be interesting to see what would happen if birds were made smarter than they are in this model.  A smart bird should probably continue to experiment with yucky colors a few times before being "convinced" that all butterflies of that color are indeed distasteful.

You could try to add variables that kept track of how many yucky individuals of the same color a bird ate.  Presumably if a bird has eaten several monarchs that are all the same color, it will be especially attentive to avoiding that color as compared to if it had just eaten one butterfly of that color.  Making changes of this nature would presumably make the proportion of models and mimics more in keeping with the predictions of theorists that there are generally more models than mimics.  In the current model, birds aren't smart enough to learn that most butterflies may be harmless in a given situation.

In a real world situation, the birds would also reproduce.  Young birds would not have the experiences necessary to know which colors to avoid.  Reproduction of birds, depending on how it happened and how often, might change the dynamics of this model considerably.

One could also refine the mutation-making procedures of the model so that a butterfly is more likely to reproduce a mutant that is only slightly differently colored than to reproduce a mutant that is completely differently colored.  In the current model, mutants' colors are simply random.

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

bird 1
false
0
Polygon -7500403 true true 2 6 2 39 270 298 297 298 299 271 187 160 279 75 276 22 100 67 31 0

bird 2
false
0
Polygon -7500403 true true 2 4 33 4 298 270 298 298 272 298 155 184 117 289 61 295 61 105 0 43

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

butterfly monarch
false
15
Line -1 true 0 0 424 424
Line -1 true 299 1 -128 424

butterfly viceroy
false
15
Circle -1 false true 34 34 232

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

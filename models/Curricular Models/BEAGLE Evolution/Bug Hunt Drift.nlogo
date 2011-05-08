breed [predators predator]
breed [bugs bug]
breed [wings wing]

bugs-own [color-variation] ;; either 1, 2, 3, 4, 5, or 6


globals [
    total-type-6-removed         ;; keeps track of the number of bugs caught with color-variation of 6
    total-type-5-removed         ;; keeps track of the number of bugs caught with color-variation of 5
    total-type-4-removed         ;; keeps track of the number of bugs caught with color-variation of 4
    total-type-3-removed         ;; keeps track of the number of bugs caught with color-variation of 3
    total-type-2-removed         ;; keeps track of the number of bugs caught with color-variation of 2
    total-type-1-removed         ;; keeps track of the number of bugs caught with color-variation of 1
    total-removed                ;; keeps track of total number of bugs removed
    total-offspring              ;; keeps track of total offspring
    mouse-event
    ]

;;

to setup
  clear-all
  set mouse-event ""
  set total-offspring 0
  set total-removed 0
  set   total-type-6-removed 0
  set   total-type-5-removed 0
  set   total-type-4-removed 0
  set   total-type-3-removed 0
  set   total-type-2-removed 0
  set   total-type-1-removed 0

  set-default-shape bugs "bug"
  set-default-shape predators "x"

  ask patches [ set pcolor white ]   ;; white background
  foreach [1 2 3 4 5 6] [
    create-bugs initial-bugs-each-variation [ set color-variation ? ]
  ]
  ask bugs [
    setxy random-xcor random-ycor
    show-variations
  ]
  ;; the predator breed contains one turtle that is used to represent
  ;; a predator of the bugs (a bird)
  create-predators 1 [
    set shape "bird"
    set color gray - 3
    set size 1.5
    set heading 315
    hide-turtle
  ]
  reset-ticks
end

to go
  ;; use EVERY to limit the overall color-variation of the model

  ifelse selection-mechanism = "keep randomly selecting"
  [every 0.25 [auto-select]]
  [every 0.03 [check-removed]]


  every 0.03 [
    move-predator
    ;; recolor the bugs in case the user changed color-variation-COLOR-MAP
    ask bugs [ show-variations move-bugs]
    ;; advance the clock
    tick
  ]

end

;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures
;;;;;;;;;;;;;;;;;;;;;


to move-bugs
    let candidate-predator nobody
    let target-heading 0

    ask bugs [
       right (random-float 5 - random-float 5)
       fd 1 * 0.005

     ]
end


to move-predator
  ask predators [
    setxy mouse-xcor mouse-ycor
    set hidden? not mouse-inside? or (selection-mechanism != "user selects bug to remove" and   ;; show predator if mouse pointer inside view
           selection-mechanism != "user selects which bug has offspring")
    if selection-mechanism = "user selects bug to remove" [
       set shape "x"
       set color gray - 3
    ]
        if selection-mechanism = "user selects which bug has offspring" [
       set shape "star"
       set color yellow - 1
    ]
  ]
end

to update-totals-removed
    if color-variation = 6 [ set total-type-6-removed total-type-6-removed + 1 ]
    if color-variation = 5 [ set total-type-5-removed total-type-5-removed + 1 ]
    if color-variation = 4 [ set total-type-4-removed total-type-4-removed + 1 ]
    if color-variation = 3 [ set total-type-3-removed total-type-3-removed + 1 ]
    if color-variation = 2 [ set total-type-2-removed total-type-2-removed + 1 ]
    if color-variation = 1 [ set total-type-1-removed total-type-1-removed + 1 ]
    set total-removed total-removed + 1
end


to check-removed
  if not mouse-down? or not mouse-inside? [
    set mouse-event "" stop ]
  let prey [bugs in-radius (size / 2)] of one-of predators
  let bug-to-die nobody
  let bug-to-reproduce nobody
    if not any? prey [
      stop ]       ;; no prey here? oh well
    if mouse-event != "selected" [
      ask one-of prey [            ;; eat only one of the bugs at the mouse location
        if selection-mechanism = "user selects bug to remove" [
         set bug-to-die self
         set bug-to-reproduce one-of bugs with [self != bug-to-die]
         set mouse-event "selected"

    ]
        if selection-mechanism = "user selects which bug has offspring" [
         set bug-to-reproduce self
         set bug-to-die one-of bugs with [self != bug-to-reproduce]
         set mouse-event "selected"
    ]                               ;; replace the eaten bug with a random offspring from the remaining population
  ask bug-to-reproduce  [
    hatch 1 [ rt random 360 ]
     set total-offspring total-offspring + 1]
  ask bug-to-die [
    update-totals-removed die]
    ]

  ]
end


to auto-select
  let bug-to-die nobody
  let bug-to-reproduce nobody
  set bug-to-reproduce one-of bugs
  set bug-to-die one-of bugs with [self != bug-to-reproduce]
  set mouse-event "selected"

  ;; replace the eaten bug with a random offspring from the remaining population
  ask bug-to-reproduce  [
    hatch 1 [ rt 30 + random 300 ]
    set total-offspring total-offspring + 1
    ]
  ask bug-to-die [
    update-totals-removed die]
end

to show-variations
   set color gray - 1 set label ""
  if variation-visualization = "color" [
    set color item (color-variation - 1) [violet blue green brown orange red] ]
  if  variation-visualization = "number" [
    set label-color item (color-variation - 1) [violet blue green brown orange red] set label word  color-variation "   " ]
end
@#$#@#$#@
GRAPHICS-WINDOW
325
15
835
546
12
12
20.0
1
16
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

MONITOR
5
190
105
235
bugs removed
total-removed
0
1
11

BUTTON
5
15
75
60
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
80
15
150
60
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

PLOT
5
240
315
372
Current Bug Population
variation
# Bugs
0.0
8.0
0.0
10.0
true
true
"" ";; the HISTOGRAM primitive can't make a multi-colored histogram,\n;; so instead we plot each bar individually, after clearing the plot\nclear-plot\n"
PENS
"1" 1.0 1 -8630108 true "" "plotxy 1 count bugs with [color-variation = 1]"
"2" 1.0 1 -13345367 true "" "plotxy 2 count bugs with [color-variation = 2]"
"3" 1.0 1 -10899396 true "" "plotxy 3 count bugs with [color-variation = 3]"
"4" 1.0 1 -6459832 true "" "plotxy 4 count bugs with [color-variation = 4]"
"5" 1.0 1 -955883 true "" "plotxy 5 count bugs with [color-variation = 5]"
"6" 1.0 1 -2674135 true "" "plotxy 6 count bugs with [color-variation = 6]"

MONITOR
215
190
315
235
total bugs
(count bugs)
0
1
11

PLOT
5
375
315
506
Bugs Removed
variation
# Bugs
0.0
8.0
0.0
10.0
true
true
"" "clear-plot"
PENS
"1" 1.0 1 -8630108 true "" "plotxy 1 total-type-1-removed"
"2" 1.0 1 -13345367 true "" "plotxy 2 total-type-2-removed"
"3" 1.0 1 -10899396 true "" "plotxy 3 total-type-3-removed"
"4" 1.0 1 -6459832 true "" "plotxy 4 total-type-4-removed"
"5" 1.0 1 -955883 true "" "plotxy 5 total-type-5-removed"
"6" 1.0 1 -2674135 true "" "plotxy 6 total-type-6-removed"

SLIDER
5
65
315
98
initial-bugs-each-variation
initial-bugs-each-variation
1
10
10
1
1
NIL
HORIZONTAL

CHOOSER
5
100
315
145
selection-mechanism
selection-mechanism
"keep randomly selecting" "user selects bug to remove" "user selects which bug has offspring"
1

MONITOR
110
190
210
235
offspring born
total-offspring
17
1
11

BUTTON
5
150
315
185
randomly remove & replace one
auto-select
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
5
510
315
643
Number of Bugs vs. Time
time
# Bugs
0.0
10.0
0.0
10.0
true
true
"" ""
PENS
"1" 1.0 0 -8630108 true "" "plotxy ticks count bugs with [color-variation = 1]"
"2" 1.0 0 -13345367 true "" "plotxy ticks count bugs with [color-variation = 2]"
"3" 1.0 0 -10899396 true "" "plotxy ticks count bugs with [color-variation = 3]"
"4" 1.0 0 -6459832 true "" "plotxy ticks count bugs with [color-variation = 4]"
"5" 1.0 0 -955883 true "" "plotxy ticks count bugs with [color-variation = 5]"
"6" 1.0 0 -2674135 true "" "plotxy ticks count bugs with [color-variation = 6]"

CHOOSER
155
15
315
60
variation-visualization
variation-visualization
"color" "number" "none"
1

@#$#@#$#@
## WHAT IS IT?

This is a genetic drift model that shows how gene frequencies change in a population due to purely random events.  The effect of random selection of certain individuals in a population (either through death or through reproduction), results in the loss or gains of an allele.  Over multiple generations this shift in gene distribution leads to alleles becoming more rare or more common (or disappearing completely) in a population. This effect is called genetic drift.

This mechanism of random selection is different than natural selection (where individual traits and genes are selected for the advantages they confer on the survival and reproduction of individuals).  Random selection, however, is one of the primary mechanisms which drives evolution.  It is also believed to be one of the primary mechanisms which leads to speciation.

The models supports the ability to contrast the outcome of intentional selection (e.g. selective breeding) vs. unintentional random selection.

## HOW IT WORKS

You assume the role of random selection mechanism (e.g. a predator or a a mate) amongst a population of bugs.  To begin your selection, press SETUP to create a population of bugs, determined by six times the INITIAL-BUGS-EACH-VARIATION slider.  These bugs that are created are randomly distributed around the world, each with one of the 6 possible variation they may be assigned.

When you press GO the bugs begin to move around.  When the VARIATION-VISUALIZATION is set to "color" or "number" you can see the different variation of the bugs as either a color or number for each bug.

As they move around, try to click on a bug to select it. When you select the bug one of two things will happen:

If the SELECTION-MECHANISM chooser is set to "user selects which bug has offspring", then the bug that is clicked on will produce an offspring that has an identical variation to the parent bug you selected.  At the same time, one bug will be randomly removed from the remaining population of bugs, to keep the population of bugs constant.

If the SELECTION-MECHANISM chooser is set to "user selects which bug to remove", then the bug that is clicked on will be removed from the population. At the same time, one bug will be randomly removed from the remaining population to produce an offspring of the parent bug that is identical to it, to keep the population of bugs constant.

Initially there are equal distributions of variations in the population (e.g. eight bugs at each of the 6 variations).  Over time, however, as you eat bugs, the distribution of the bugs will change as shown in the "Number of bugs" histogram.  In the histogram, you might see the distribution shift to the left (showing that more slow bugs are surviving) or to the right (showing that more fast bugs are surviving).  Sometimes one sub-population of a single speed of bug will be exterminated.  At this point, no other bugs of this speed can be created in the population.

To make this effect happen even more dramatically through "random selection", you as the user, can't really know or see what you are selecting.  Therefore, run the model again, with the VARIATION-VISUALIZATION chooser set to "none" to hide the visible color characteristic from you as select the bugs.  In this way, you will be selecting individuals to remove and replace in the population with a particular gene or trait, without you realizing which ones you are selecting.

Alternatively, you can press the button RANDOMLY REMOVE & REPLACE to have the model do both a random selection of which bug to remove and which bug will reproduce to make an offspring that is identical to it.  Or, if you wish to have this process further automated, you can change the SELECTION-MECHANISM chooser to "keep randomly selecting" and the model will repeat the process of randomly removing and replacing every .25 seconds.

## HOW TO USE IT

INITIAL-BUGS-EACH-COLOR is the number of bugs you start with in each of the six sub-populations.  The overall population of bugs is determined by multiplying this value by 6.

VARIATION-VISUALIZATION chooser switch help you visualize or hide the variation in the population of the bugs.  When set to "colors", each variation is assigned a color, when set to "number", each variation displays a number as its label, and when set to "none" the variation is not visible.

OFFSPRING BORN is a monitor showing the number of offspring created.  It should always be equal to the number of bugs removed, since with every selection of a bug removed and new bug is born.

TOTAL BUGS is a monitor reporting the total number of bugs in the population.  It should remain constant.

CURRENT BUG POPULATION is a histogram showing the distribution of bugs at different speeds.

BUGS REMOVED is a histogram showing the historical record of the distribution of bugs caught at different speeds.  A monitor of the same name is also included.

NUMBER BUGS VS. TIME is a graph showing the total number of each variation of bug vs. time.

SELECTION MECHANISM (see above)

If you are having trouble catching any bugs, because they move too fast.  Then try clicking and holding the mouse button down and with the mouse button down, run your cursor into a bug.  This will also select the bug.  But after selecting one you will have to click and hold down the mouse button again to select another bug.  If the bugs still are moving too fast for you to select them, try adjusting the speed slider by moving it to the left at the top of the model to make the model run slightly slower.

## THINGS TO NOTICE

When you the VARIATION-VISUALIZATION chooser is set to "none" and you can't see the trait variations you are selecting, the CURRENT BUG POPULATION histogram shifts over time.  As one trait or gene begins to dominate, it becomes the more likely one to end up as the only trait or gene in the populations.

Both selecting individuals out of the population and selecting individuals to reproduce result in this loss of diversity in the gene pool of the population, eventually leading to a single gene or trait in the population.

Sub-populations of bugs of each color fluctuate up and down when they are selected randomly for removal or reproduction.

## THINGS TO TRY

Try intentionally selecting a particular color to remove (or reproduce) and keep selecting to see how many selections it takes to remove all other color variants from the population.  Compare this approach to random selection.  Change the VARIATION-VISUALIZATION to "none" and start the model over.  See how many selections it now takes to remove all but one color variant form the population.

Alternately, select the bugs by closing your eyes and randomly clicking and holding the mouse button down for a couple seconds.  Then click and hold down again.  Clicking and holding the mouse button down will remove the first bug that runs into your cursor.  But you need to repeat this process to remove another bug.  Alternatively, press the RANDOMLY REMOVE & SELECT ONE.  Or, change the SELECTION-MECHANISM to "keep randomly selecting" and the computer will start selecting the bugs randomly to remove and replace from the population.

Try changing the number of INITIAL-BUGS-EACH-VARIATION to compare how population size affects how fast genetic drift occurs and how fast variants are removed from the population.

## EXTENDING THE MODEL

Add a 2nd or third trait to add to the population.

Add a mechanism of sexual reproduction and genetic recombination to the population.

Add walls to geographically isolate portions of the population from one another.

## RELATED MODELS

GenDrift models in the Genetic Drift folder, under Biology

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

bird
true
0
Polygon -7500403 true true 151 170 136 170 123 229 143 244 156 244 179 229 166 170
Polygon -16777216 true false 152 154 137 154 125 213 140 229 159 229 179 214 167 154
Polygon -7500403 true true 151 140 136 140 126 202 139 214 159 214 176 200 166 140
Polygon -16777216 true false 151 125 134 124 128 188 140 198 161 197 174 188 166 125
Polygon -7500403 true true 152 86 227 72 286 97 272 101 294 117 276 118 287 131 270 131 278 141 264 138 267 145 228 150 153 147
Polygon -7500403 true true 160 74 159 61 149 54 130 53 139 62 133 81 127 113 129 149 134 177 150 206 168 179 172 147 169 111
Circle -16777216 true false 144 55 7
Polygon -16777216 true false 129 53 135 58 139 54
Polygon -7500403 true true 148 86 73 72 14 97 28 101 6 117 24 118 13 131 30 131 22 141 36 138 33 145 72 150 147 147

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

suit heart
false
0
Circle -7500403 true true 135 43 122
Circle -7500403 true true 43 43 122
Polygon -7500403 true true 255 120 240 150 210 180 180 210 150 240 146 135
Line -7500403 true 150 209 151 80
Polygon -7500403 true true 45 120 60 150 90 180 120 210 150 240 154 135

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

wings
true
0
Polygon -7500403 true true 150 165 209 199 225 225 225 255 195 270 165 255 150 240
Polygon -7500403 true true 150 165 89 198 75 225 75 255 105 270 135 255 150 240
Polygon -7500403 true true 139 148 100 105 55 90 25 90 10 105 10 135 25 180 40 195 85 194 139 163
Polygon -7500403 true true 162 150 200 105 245 90 275 90 290 105 290 135 275 180 260 195 215 195 162 165

x
false
0
Polygon -7500403 true true 270 75 225 30 30 225 75 270
Polygon -7500403 true true 30 75 75 30 270 225 225 270

@#$#@#$#@
NetLogo 5.0beta1
@#$#@#$#@
setup
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
1
@#$#@#$#@

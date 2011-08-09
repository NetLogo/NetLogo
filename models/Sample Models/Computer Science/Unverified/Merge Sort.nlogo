breed [ mergers merger ]
breed [ elements element ]

elements-own [ value ]
mergers-own [ merge-group ]
globals
[
  group-count     ;; Number of groups (lists) of elements
  group-list      ;; List of lists of elements
  step-number     ;; Number of complete merge steps finished
  current-loc     ;; Group position of next element to be drawn, used by single-sort
  current-group   ;; Group number of next element to be drawn, used by single-sort
  current-count   ;; Element number of next element to be drawn, used by single-sort
]

;;;;;;;;;;;;;;;;;;;;;;
;; Setup Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;

to setup
  ca
  set current-count 1
  set current-loc 0
  set current-group 0
  set step-number 0
  set group-list []
  set group-count number-of-elements
  setup-elements
end

to setup-elements
  set-default-shape turtles "circle"
  create-elements number-of-elements
  [
    set size 5
    set value (random (4 * number-of-elements))
    ;; (list self) creates a list with its sole item being the turtle itself
    set group-list lput (list self) group-list
  ]
  draw
end

;;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;;

;; Do one set of group merges.  That is, have each pair of neighboring groups merge.
to step-row
  ;; Finish displaying current step if need be
  if (current-count > 1)
  [
    draw
    set current-count 1
    set current-loc 0
    set current-group 0
    stop
  ]
  ;; Stop if the first group contains all elements which means all elements
  ;; have been sorted.
  if (length (item 0 group-list) = number-of-elements)
    [ stop ]
  set step-number (step-number + 1)
  combine-groups
  draw
end

;;;;;;;;;;;;;;;;;;;;;;;;
;; Merging Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;;
to combine-groups
  let num 0
  ;; Create a merger for every two groups
  ;; Each merger will combine two groups
  create-mergers (group-count / 2)
  [
    set merge-group num
    set num (num + 2)
  ]
  ask mergers
  [
    merge (item merge-group group-list) (item (merge-group + 1) group-list) merge-group
    die
  ]
  ;; Remove empty groups (-1's) from our list
  set group-list remove -1 group-list
  set group-count length group-list
end

;; Merge lists 1 and 2 into one list, maintaining order
to merge [ list1 list2 location ] ;; mergers procedure
  let new-list []
  ;; Sort the lists into new-list until either list1 or list2 is empty.
  ;; The groups are merged into increasing/decreasing order depending on
  ;; whether the increasing-order switch in on/off.
  let item1 0
  let item2 0
  while [(not empty? list1) and (not empty? list2)]
  [
    set item1 item 0 list1
    set item2 item 0 list2
    ifelse ( [value] of item1 < [value] of item2 )
    [
      set new-list lput item1 new-list
      set list1 but-first list1
    ]
    [
      set new-list lput item2 new-list
      set list2 but-first list2
    ]
  ]
  ;; One of the lists is always going to be non-empty after the above loop.
  ;; Put the remainder of the non-empty list into new-list.
  ifelse (empty? list1)
    [ set new-list sentence new-list list2 ]
    [ set new-list sentence new-list list1 ]
  ;; Copy the new-list into the appropriate location in group-list.
  ;; [(a+b) b c d] becomes [(a+b) -1 c d]
  ;; The -1's will be removed once the entire step is complete.
  ;; We do this instead of removing it here to keep order and length intact.
  set group-list (replace-item location group-list new-list)
  set group-list (replace-item (location + 1) group-list -1)
end

;;;;;;;;;;;;;;;;;;;;;;;;
;; Display Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;;
to step-item
  ;; If we have finished this round of sorting, reset our values
  if (current-count > number-of-elements)
  [
    set current-count 1
    set current-loc 0
    set current-group 0
  ]
  ;; Do a round of sorting before we display if necessary
  if (current-count = 1)
  [
    ;; Stop if the first group contains all elements which means all elements
    ;; have been sorted.
    if (length (item 0 group-list) = number-of-elements)  [stop]
    set step-number (step-number + 1)
    combine-groups
    ;; To display the step number.
    ask patch (min-pxcor + 2) (max-pycor - 5 - (step-number * 10))
      [set plabel-color green set plabel step-number]
  ]
  ;; Display the current element with its new position and color.
  let tcolor [color] of first (item current-group group-list)
  ask (item current-loc (item current-group group-list))
  [
    set pcolor color
    set color tcolor
    set ycor (max-pycor - 5 - (10 * step-number))
    set xcor (min-pxcor + (current-count * ((2 * max-pxcor) / (number-of-elements + 1))))
    ask patch-at 0 4 [set plabel-color white set plabel [value] of myself]
  ]
  ;; Update information about which turtle to display next
  set current-count (current-count + 1)
  ifelse(length (item current-group group-list) = (current-loc + 1))
  [
    set current-loc 0
    set current-group (current-group + 1)
  ]
  [ set current-loc (current-loc + 1) ]
end

;; Move the turtles to their appropriate locations
to draw
  let list-loc 0
  let element-num 1
  ;; Evenly space the elements across the view
  let separation ((2 * max-pxcor) / (number-of-elements + 1))
  ;; To display the step number.
  ask patch (min-pxcor + 2) (max-pycor - 5 - (step-number * 10))
    [set plabel-color green set plabel step-number]
  while [list-loc < group-count]
  [
    let current-list item list-loc group-list
    let tcolor [color] of first current-list
    while [not empty? current-list]
    [
      ask (item 0 current-list)
      [
        ;; To keep track of what group an element belonged to before the current step,
        ;; we leave the color and display the value at it's previous place.
        if (step-number != 0) [ set pcolor color]
        set color tcolor
        set ycor (max-pycor - 5 - (10 * step-number))
        set xcor (min-pxcor + (element-num * separation))
        ask patch-at 0 4 [set plabel-color white set plabel [value] of myself]
      ]
      set element-num (element-num + 1)
      set current-list but-first current-list
    ]
    set list-loc (list-loc + 1)
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
220
10
836
347
50
25
6.0
1
16
1
1
1
0
0
0
1
-50
50
-25
25
0
0
0
ticks

BUTTON
58
74
132
109
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
35
208
149
243
step (1 row)
step-row
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
10
39
194
72
number-of-elements
number-of-elements
2
16
16
1
1
NIL
HORIZONTAL

BUTTON
35
169
149
202
step (1 item)
step-item
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

This model is a visual demonstration of a standard sort algorithm called merge sort.  The algorithm reorders, or permutes, n numbers into ascending order.  This is accomplished by dividing the numbers into groups and then merging smaller groups to form larger groups. Order is maintained as the lists are merged so when the algorithm finishes there is only one sorted list containing all n items.

Note that it is possible to express merge sort in NetLogo much more concisely than is done in this model.  Since this model aims to demonstrate the sort algorithm visually, the code is more complex than would be needed if the model only needed to sort the numbers.

## HOW IT WORKS

We start out with as many independent groups as we have elements.  As the algorithm progresses through the list, it merges each adjacent pair of groups; thus, after each pass, the number of groups is halved.

To merge two groups:
1. Compare the first elements of the two groups to each other
2. Place the smallest/largest element (depending on the increasing-order? switch) of the two in a third group
3. Remove that element from its source group
4. Repeat until one of the source groups is empty
5. Place all of the remaining elements from the non-empty source group onto the end of the third group
6. Substitute, in place, the third group for the two source groups

We do this merge repeatedly for each set of two groups until there is only one group left.  This final group is the original set of numbers in sorted order.

The number of steps required to sort n items using this algorithm is the ceiling of logarithm (base 2) of n.  Each step requires at most n comparisons between the numbers.  Therefore, the time it takes for the algorithm to run is about n log n.  Computer scientists often write this as O(n log n) where n is how many numbers are to be sorted.

## HOW TO USE IT

Change the value of the NUMBER-OF-ELEMENTS slider to modify how many numbers to sort.

Pressing SETUP creates NUMBER-OF-ELEMENTS random values to be sorted.

STEP (1 ITEM) merges one number into its new group.

STEP (1 ROW) does one full round of group merges.

## THINGS TO NOTICE

Groups are represented by color.  Numbers in the same group have the same color.  When two groups merge, the numbers take the color of the smallest/largest element in the new group.  Can you predict what would be the final color of all elements before starting?

Would merging more than two groups at a time lead to the elements getting sorted in fewer steps?  Would this change make the algorithm any faster?

## THINGS TO TRY

We stated above that the algorithm will take at most a constant factor times n log n time to execute.  Can you figure out why the constant factor is needed to make this statement accurate?

## EXTENDING THE MODEL

Can you make the elements draw their paths across the view?

There are many different sorting algorithms. You can find a few described at http://en.wikipedia.org/wiki/Sorting_algorithm.  Try implementing the different sorts in NetLogo and use BehaviorSpace to compare them.  Do different sorts perform better with different input sets (uniformly random, nearly sorted, reverse sorted, etc.)?

## NETLOGO FEATURES

This model uses lists extensively.

Note that NetLogo includes SORT and SORT-BY primitives; normally, you would just use one of these, rather than implementing a sort algorithm yourself.  SORT arranges items in ascending order; SORT-BY lets you specify how items are to be ordered.

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
NetLogo 5.0beta1
@#$#@#$#@
setup
repeat 2 [ step-row ]
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

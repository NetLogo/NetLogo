globals [
  group-sites    ;; agentset of patches where groups are located
  boring-groups  ;; how many groups are currently single-sex
]

turtles-own [
  happy?         ;; true or false
]

to setup
  clear-all
  set group-sites patches with [group-site?]
  set-default-shape turtles "person"
  create-turtles number [
    choose-sex                   ;; become a man or a woman
    set size 3                   ;; be easier to see
    move-to one-of group-sites
  ]
  ask turtles [ update-happiness ]
  count-boring-groups
  update-labels
  ask turtles [ spread-out-vertically ]
  reset-ticks
end

to go
  if all? turtles [happy?]
    [ stop ]  ;; stop the simulation if everyone is happy
  ask turtles [ set ycor 0 ]  ;; put all people back on the x-axis
  ask turtles [ update-happiness ]
  ask turtles [ leave-if-unhappy ]
  find-new-groups
  update-labels
  count-boring-groups
  ask turtles [ spread-out-vertically ]
  tick
end

to update-happiness  ;; turtle procedure
  let total count turtles-here
  let same count turtles-here with [color = [color] of myself]
  let opposite (total - same)
  ;; you are happy if the proportion of people of the opposite sex
  ;; does not exceed your tolerance
  set happy? (opposite / total) <= (tolerance / 100)
end

to leave-if-unhappy  ;; turtle procedure
  if not happy? [
    set heading one-of [90 270]  ;; randomly face right or left
    fd 1                         ;; leave old group
  ]
end

to find-new-groups
  display   ;; force display update so we see animation
  let malcontents turtles with [not member? patch-here group-sites]
  if not any? malcontents [ stop ]
  ask malcontents [ fd 1 ]
  find-new-groups
end

to-report group-site?  ;; patch procedure
  ;; if your pycor is 0 and your pxcor is where a group should be located,
  ;; then you're a group site.
  ;; In this model (0,0) is near the right edge, so pxcor is usually
  ;; negative.
  ;; first figure out how many patches apart the groups will be
  let group-interval floor (world-width / num-groups)
  report
    ;; all group sites are in the middle row
    (pycor = 0) and
    ;; leave a right margin of one patch, for legibility
    (pxcor <= 0) and
    ;; the distance between groups must divide evenly into
    ;; our pxcor
    (pxcor mod group-interval = 0) and
    ;; finally, make sure we don't wind up with too many groups
    (floor ((- pxcor) / group-interval) < num-groups)
end

to spread-out-vertically  ;; turtle procedure
  ifelse woman?
    [ set heading 180 ]  ;; face north
    [ set heading   0 ]  ;; face south
  fd 4                   ;; leave a gap
  while [any? other turtles-here] [ fd 1 ]
end

to count-boring-groups
  ask group-sites [
    ifelse boring?
      [ set plabel-color gray  ]
      [ set plabel-color white ]
  ]
  set boring-groups count group-sites with [plabel-color = gray]
end

to-report boring?  ;; patch procedure
  ;; To see whether this group is single sex, we collect the colors
  ;; of the turtles into a list, then remove all the duplicates
  ;; from the list.  If the result is a list with exactly one color
  ;; in it, then the group is single sex.
  report length remove-duplicates ([color] of turtles-here) = 1
end

to update-labels
  ask group-sites [ set plabel count turtles-here ]
end

;;;
;;; color procedures
;;;

;; Blue represents male, pink represents female. No stereotypes are meant
;; to be promoted. Simply change the colors right here if you'd like.

to choose-sex  ;; turtle procedure
  set color one-of [pink blue]
end

to-report woman?  ;; turtle procedure
  report color = pink
end
@#$#@#$#@
GRAPHICS-WINDOW
260
10
680
596
-1
55
5.0
1
14
1
1
1
0
1
0
1
-80
1
-55
55
1
1
1
ticks
30

BUTTON
15
110
83
143
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
110
234
143
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

BUTTON
84
110
165
143
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

SLIDER
15
157
234
190
tolerance
tolerance
0.0
99.0
25
1.0
1
%
HORIZONTAL

SLIDER
50
38
201
71
number
number
0
300
70
1
1
NIL
HORIZONTAL

PLOT
10
206
253
371
Number Happy
clock
NIL
0.0
10.0
0.0
150.0
true
false
"set-plot-y-range 0 number" ""
PENS
"Happy" 1.0 0 -10899396 true "" "plot count turtles with [happy?]"

PLOT
11
371
254
541
Single Sex Groups
clock
NIL
0.0
10.0
0.0
12.0
true
false
"" ""
PENS
"Single Sex" 1.0 0 -2674135 true "" "plot boring-groups"

SLIDER
50
72
201
105
num-groups
num-groups
5
20
10
1
1
NIL
HORIZONTAL

MONITOR
196
304
253
349
happy?
count turtles with [happy?]
3
1
11

MONITOR
192
474
254
519
groups
boring-groups
3
1
11

@#$#@#$#@
## WHAT IS IT?

This is a model of a cocktail party.  The men and women at the party form groups.  A party-goer becomes uncomfortable and switches groups if their current group has too many members of the opposite sex.  What types of group result?

## HOW IT WORKS

The party-goers have a TOLERANCE that defines their comfort level with a group that has members of the opposite sex.  If they are in a group that has a higher percentage of people of the opposite sex than their TOLERANCE allows, then they are considered "uncomfortable", and they leave that group to find another group.  Movement continues until everyone at the party is "comfortable" with their group.

## HOW TO USE IT

The NUMBER slider controls how many people are in the party, and the NUM-GROUPS slider controls how many groups form.

The SETUP button forms random groups.  To advance the model one step at a time, use the GO ONCE button. The GO button keeps the model running until everybody is comfortable.

The numbers in the view show the sizes of the groups.  White numbers are mixed groups and gray numbers are single-sex groups.

To set the tolerance of the people for the opposite sex, use the TOLERANCE slider.  You can move the slider while the model is running.  If the TOLERANCE slider is set to 75, then each person will tolerate being in a group with less than or equal to 75% people of the opposite sex.

The NUMBER HAPPY and SINGLE SEX GROUPS plots and monitors show how the party changes over time.  NUMBER HAPPY is how many party-goers are happy (that is, comfortable).  SINGLE SEX GROUPS shows the number groups containing only men or only women.

## THINGS TO NOTICE

At the end of the simulation (when everyone is happy), notice the number of single-sex groups.  Are there more than at the start?

## THINGS TO TRY

Try varying TOLERANCE.  Is there a critical tolerance at which each all groups end up being single-sex?  At different tolerance levels, does it take longer or shorter for everyone to become comfortable?

See how many mixed groups (not a single-sex group) you can get.

Using the GO ONCE button, experiment with different tolerances.  Watch how one unhappy person can disrupt the stability of other groups.

Is it possible to have an initial grouping such that the party never reaches a stable state?  (i.e. the model never stops running)

Observe real parties.  Is this model descriptive of real social settings?  What tolerance level do real people typically have?

## EXTENDING THE MODEL

Add more attributes to the model.  Instead of male/female, try a trait that has more than two types, like race or religion.  (You might use NetLogo's breeds feature to implement that.)

Allow each breed of person to have their own tolerance.

Complicate the tolerance rules: For example, the tolerance could go up as long as there are at least two of one breed.

Allow groups to subdivide, instead of finding new groups.

Set a maximum group size, so that if there are too many people in the group, they become unhappy.

## NETLOGO FEATURES

Most NetLogo models put the origin (0,0) in the center of the world, but here, we have placed the origin near the right edge of the world and most of the patches have negative X coordinates.  This simplifies the math for situating the groups.

Horizontal wrapping is enabled, but vertical wrapping is disabled.  Thus, the world topology is a "vertical cylinder".

Notice the use of the `mod` primitive to space out the groups evenly.  Setting up the groups in this manner allows for easy movement from group to group.

## RELATED MODELS

Segregation

## CREDITS AND REFERENCES

This model is based on the work of the pioneering economist Thomas Schelling:  
Schelling, T. (1978). Micro-motives and Macro-Behavior. New York: Norton.

See also:  
Resnick, M. & Wilensky, U. (1998). Diving into Complexity: Developing Probabilistic Decentralized Thinking through Role-Playing Activities. Journal of Learning Sciences, Vol. 7, No. 2.  http://ccl.northwestern.edu/papers/starpeople/
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
repeat 20 [ go ]
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

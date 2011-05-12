breed [ leaders leader ]
breed [ followers follower ]

globals [ choice the-leader ]

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;
to setup [n]
  clear-all
  set choice n
  setup-axes
  setup-leader
  setup-followers
  reset-ticks
end

to setup-axes
  ;; draw x-axis & y-axis as light gray
  ask patches with [pxcor = 0 or pycor = 0]
    [ set pcolor gray - 3 ]
end

to setup-leader
  create-leaders 1
    [ set the-leader self
      set color gray + 2
      set size 3
      set shape "circle"
      set xcor min-pxcor
      if not show-leader? [ ht ]
      update-leader-ycor ]
end

to setup-followers
  create-followers num-followers
  [ ;; if there are exactly 4 follower turtles, set them up in the four corners of the world
    ;; otherwise, make the followers' locations random
    set color color + 10
    ifelse num-followers = 4
    [ ask turtle 1 [ setxy max-pxcor max-pycor ]
      ask turtle 2 [ setxy max-pxcor min-pycor ]
      ask turtle 3 [ setxy min-pxcor max-pycor ]
      ask turtle 4 [ setxy min-pxcor min-pycor ]
    ]
    [ setxy ((random (2 * max-pxcor)) + min-pxcor)
            ((random (2 * max-pxcor)) + min-pxcor)
    ]
    set shape "circle"
    set size 2
    set pen-size 2
    pd
  ]
end

to reveal
  let old-value-of-show-trail? show-trail?
  set show-trail? true
  ask the-leader
    [ set xcor 0
      while [xcor < max-pxcor]
        [ move-leader ]
      st ]
  set show-trail? old-value-of-show-trail?
end


;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Runtime Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;
to go
  ;; stop if the lead turtle is about to go out of the world
  if [patch-at 1 0] of the-leader = nobody [ stop ]
  go-once
end

to go-once
  move-leader
  move-followers
  tick
end

to move-followers
  ask followers
  [ face the-leader
    ;; if we're about to catch up with the leader, don't pass the
    ;; leader, only pull even with it
    ifelse distance the-leader > step-size
      [ fd step-size ]
      [ move-to the-leader ]
  ]
end

to move-leader
  ask the-leader
  [ update-leader-ycor
    if show-trail?
      [ set size 1
        stamp
        set size 3 ]
    set xcor xcor + 1
  ]
end

to update-leader-ycor  ;; leader procedure
  if (choice = 0)  [ linear      ]  ;;  y = x
  if (choice = 1)  [ quad-up     ]  ;;  y = x^2
  if (choice = 2)  [ quad-down   ]  ;;  y = -x^2
  if (choice = 3)  [ cubic       ]  ;;  y = x^3
  if (choice = 4)  [ logarithm   ]  ;;  y = ln x
  if (choice = 5)  [ exponential ]  ;;  y = a^x
  if (choice = 6)  [ sine        ]  ;;  y = sin x
  if (choice = 7)  [ cosine      ]  ;;  y = cos x
  if (choice = 8)  [ hyperbolic  ]  ;;  y = 1/x
  if (choice = 9)  [ horizontal  ]  ;;  y = 0
end

to linear  ;; leader procedure
  set ycor xcor
end

to quad-up  ;; leader procedure
  set ycor ((xcor ^ 2) / max-pycor)
end

to quad-down  ;; leader procedure
  set ycor ((- (xcor ^ 2)) / max-pycor)
end

to cubic  ;; leader procedure
  set ycor ((xcor ^ 3) / (max-pycor ^ 2))
end

to logarithm  ;; leader procedure
  if (xcor <= 0)
  [ set xcor 1 ]
  set ycor ((ln (xcor / 10)) * 10)
end

to exponential  ;; leader procedure
  let scale-factor ((1.1 ^ max-pxcor) / max-pycor)
  set ycor ((1.1 ^ xcor) / scale-factor)
end

to sine  ;; leader procedure
  set ycor (max-pycor * (sin (xcor * 4)))
end

to cosine  ;; leader procedure
  set ycor (max-pycor * (cos (xcor * 4)))
end

to hyperbolic  ;; leader procedure
  ifelse (xcor = 0)
    [ set ycor 0 ]
    [ set ycor (max-pycor / xcor) ]
end

to horizontal  ;; leader procedure
  set ycor 0
end
@#$#@#$#@
GRAPHICS-WINDOW
310
10
803
524
80
80
3.0
1
10
1
1
1
0
0
0
1
-80
80
-80
80
1
1
1
ticks
15

BUTTON
110
332
165
365
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
26
87
149
120
step-size
step-size
1
5
1
1
1
NIL
HORIZONTAL

SLIDER
26
45
149
78
num-followers
num-followers
0
50
4
1
1
NIL
HORIZONTAL

SWITCH
160
45
300
78
show-leader?
show-leader?
1
1
-1000

SWITCH
160
87
300
120
show-trail?
show-trail?
1
1
-1000

BUTTON
39
165
113
198
random
setup random 11
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
39
198
113
231
linear
setup 0
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
119
165
201
198
quadratic
setup 1
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
119
198
201
231
-quadratic
setup 2
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
119
231
201
264
cubic
setup 3
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
119
264
201
297
logarithmic
setup 4
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
207
264
297
297
exponential
setup 5
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
207
231
297
264
hyperbolic
setup 8
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
39
231
113
264
horizontal
setup 9
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
207
165
297
198
sine
setup 6
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
207
198
297
231
cosine
setup 7
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
26
402
95
435
reveal
reveal
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
26
332
107
365
go once
go-once
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

TEXTBOX
17
140
159
158
Equation setup:
11
0.0
0

TEXTBOX
17
310
122
328
Execution:
11
0.0
0

TEXTBOX
17
384
276
402
Reveal the leader's trail and position:
11
0.0
0

TEXTBOX
17
16
107
34
Setup options:
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

In this model there is one leader turtle and a group of follower turtles.  Have a little fun -- hide the leader and try to guess the path it's moving along.  Watching the followers gives you clues to the leader's path.

## HOW IT WORKS

The leader moves along a path according to a preselected formula, such as y = x ^ 2.

The leader starts at the left edge of the world.

The leader always moves from left to right by one unit increments along the x-axis.  Be aware, for several functions, however, the function is scaled by some factor so that its shape fits well within the world.

The leader's y coordinate is based on the selected formula and the current x coordinate. For example, if the current formula is y = x ^ 2 and the leader's current xcor value is -3, the ycor value will be set to 9.

Each follower turns to face the leader, then moves forward by a fixed amount.

## HOW TO USE IT

Use the NUM-FOLLOWERS slider to select how many followers will pursue the leader.  If exactly four followers are selected, they will be placed at the four corners of the world.  If a different number is selected, the followers will be placed at random locations in the world.

Use the STEP-SIZE slider to decide how far from its current location a follower will move after each step of the leader.

When you press SETUP, the light gray lines that appear are the x-axis and the y-axis.

Leader Switches:  
SHOW-LEADER? makes the leader shown or hidden.  This switch must be set before SETUP is pressed.  
When the switch is set to on, the leader will be visible.  
When the switch is set to off, the leader will be hidden.

SHOW-TRAIL?, when turned on, causes the leader to leave a mark at each location determined by its formula.  This switch may be turned on and off at any time during the pursuit.  
When the switch is set to on, the trail will be visible.  
When the switch is set to off, the trail will be hidden.

Press RANDOM or any of the function buttons when all of the above selections have been made.  This will create the leader and the selected number of followers.

RANDOM chooses a random function for the leader to follow, and is best used with SHOW-LEADER? and SHOW-TRAIL? turned off so that the function can be guessed based on the movements of the followers.

The other buttons choose a specific function for the leader to follow.

Press GO-ONCE to make the leader increment its xcor value by one and to make the followers take one step toward the leader.

Press GO to make the leader and followers move continuously.  
To stop them, press the GO button again.

The SLOWDOWN slider controls how fast all of the turtles move.

Press REVEAL after the model is done to see what the path of the leader was if SHOW-TRAIL? was turned off.  You can use this to check your guess as to what the function was.

## HOW TO USE IT

Try starting with 4 followers with a step size of 1.  Do not show the leader or the leader's trail.

Use settings so that the world is square.

Press RANDOM then press GO.

See if you can guess the formula the leader is using by observing the path of each follower.

For each of the next questions, consider the follow-up questions "Why or why not?" and "How can you tell?"

Does the speed of the leader seem to change over time?

Does the speed of a follower seem to change over time?

Do all followers travel at the same speed?

What can you tell about the leader's formula based on the path of each follower?

What traits of each follower's path give you information about the leader's formula?  Which of these traits do you find most helpful?  Why?

To change the formula for the leader:  
A number of formulas have been stored in the procedures for this model.  To explicitly make a given formula active, choose the button for the formula you want instead of RANDOM.

See the EXTENDING THE MODEL section for instructions on how to add your own formulas to the model.

## THINGS TO NOTICE

There are several characteristics of each follower's path and the leader's trail that are worth noting.

Follower Path Slope:  
What does it mean if the slope of the path is increasing?  
What does it mean if the slope of the path is decreasing?  
What does it mean if one section of the path has a steeper slope than another part?  
What does it mean if the slope of the path is constant?

To think about the slope of a path, consider whether the path appears to be going 'uphill' or 'downhill' and consider whether the 'hill' is steep or flat.

Follower Path Concavity:  
What does it mean if the path has a section that is concave up?  
What does it mean if the path has a section that is concave down?  
What does it mean if the path has sections of both of these types?  
What does it mean if the path has neither concave up nor concave down sections?

A path that is concave up will be shaped like part of an upright coffee cup.  
A path that is concave down will be shaped like part of an upside down coffee cup.

Relationships Between Paths:  
Do the paths have any symmetry?  Would you expect them to?  Why or why not?

Distances Between the Leader and a Follower:  
Once you have determined the formula for the leader, run a simulation with the leader's trail turned on.  (Note that there are other suggestions to verify your answer in the THINGS TO TRY section.  Make sure you have tried at least some of these before you show the trail.  If you show the trail before you are really sure you are right, you might end up spoiling all your fun- there's no going back once you have seen the trail of the leader!)

Find a path where a follower seems to get close to the leader only to have the leader appear to speed up and escape from the follower.

Why does this happen?  What kinds of generalizations can you make about the formulas or relationships for which this happens?

How do the distances between the leader's trail marks relate to the perceived speed of the leader?

Leader Mark Proximity:  
Depending on the path that the leader is following, the leader's trail marks may not be evenly spaced.

What does it mean when the steps are evenly spaced?  
What does it mean when the steps are not evenly spaced?  
What does it mean when the marks are closer together?  
What does it mean when the marks are further apart?

## THINGS TO TRY

Try moving followers to specific locations after SETUP has been pressed but before GO has been pressed. Make predictions about how different locations would be helpful.

What can you learn if a follower starts in any of the following locations?  
- along the right edge of the world  
- along the left edge of the world  
- along the top edge of the world  
- along the bottom edge of the world  
- along the x-axis  
- along the y-axis  
- at the origin

What is the most helpful first location for a follower?  (The location on top of the leader is, of course, out of the question!)

What is the most helpful follow-up location for a single follower or for a group of followers?

Come up with a strategy for placing followers so that you can determine the path of the leader fairly quickly.  Describe your strategy.

You may use the command center, or the turtle window to move a follower.  The leader is turtle 0, the followers are all turtles with who > 0.

Try increasing the number of followers.  
(Even if you think you have the formula figured out, try using larger NUM-FOLLOWERS values before you show the leader or the leader's trail. )  
Why does using a larger NUM-FOLLOWERS value make it easier to guess the leader's formula?

Try increasing the STEP-SIZE of the followers.  
(Even if you think you have the formula figured out, try using larger STEP-SIZE values before you show the leader or the leader's trail. )  
Why does using a larger STEP-SIZE value make it easier to guess the leader's formula?

The above discussions all involve trying to guess the path of the leader.  Alternately, you can know the formula of the leader and try to guess the paths of the followers.

If you know a leader's formula and are trying to guess the pattern of the followers' paths, make sure to record you guess before you run the simulation.  Compare your predicted results with the actual results.  
- What reasoning led you to correct predictions?  
- What assumptions that you made need to be revised?

## EXTENDING THE MODEL

To add your own formulas, you need to add a new Leader Procedure to the model (the others are declared at the bottom of the Code tab.

Add your formula to the current list.  
- Within the set of commands you may need to scale the y-axis to keep the leader from wrapping.  (See for example, the cubic function.)  
- You must restrict the domain of the leader if your formula has values for which it would be undefined.  (See, for example, the logarithm function.)  
- Add formula-name to the MOVE-LEADER procedure and add a button for the choice.  
- Increment the number after `random` in the RANDOM button code to add your function to the list of possible functions that RANDOM will choose.

In this simulation, the leader uses only integer x coordinates.  For which formulas might the results be different if the leader moved along smaller intervals?

What would happen if the STEP-SIZE of the followers was always set to equal the distance the leader traveled during its most recent step?

Adjust the procedures so that rectangular worlds do not cause unexpected wrapping.

Do any of these changes impact answers to any of the questions asked above?

Create leader functions and pick a follower location to get the following shapes in the follower's path:  
- a straight line with positive slope  
- a straight line with negative slope  
- a horizontal line  
- a loop  
- a circle  
- a curve with one "hump"  
- a curve with two "humps"  
- a curve with three "humps"  
- a curve with n "humps"

## NETLOGO FEATURES

Turtle pens are used to draw the followers' paths.

The `stamp` command is used to draw the leader's path.

The `face` command is used to orient the followers.

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
NetLogo 5.0beta2
@#$#@#$#@
setup 6
repeat 175 [ go ]
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

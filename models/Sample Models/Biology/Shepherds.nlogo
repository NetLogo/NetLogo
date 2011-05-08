breed [sheep a-sheep]
breed [shepherds shepherd]

globals
[
  sheepless-neighborhoods       ;; how many patches have no sheep in any neighboring patches?
  herding-efficiency            ;; measures how well-herded the sheep are
]
patches-own
[
  sheep-nearby                  ;; how many sheep in neighboring patches?
]
shepherds-own
[
  carried-sheep         ;; the sheep I'm carrying (or nobody if I'm not carrying in)
  found-herd?           ;; becomes true when I find a herd to drop it in
]

to setup
  clear-all
  set-default-shape sheep "sheep"
  set-default-shape shepherds "person"
  ask patches
    [ set pcolor green + (random-float 0.8) - 0.4]   ;; varying the green just makes it look nicer
  create-sheep num-sheep
    [ set color white
      set size 1.5  ;; easier to see
      setxy random-xcor random-ycor ]
  create-shepherds num-shepherds
    [ set color brown
      set size 1.5  ;; easier to see
      set carried-sheep nobody
      set found-herd? false
      setxy random-xcor random-ycor ]
  reset-ticks
end

to update-sheep-counts
  ask patches
    [ set sheep-nearby (sum [count sheep-here] of neighbors) ]
  set sheepless-neighborhoods (count patches with [sheep-nearby = 0])
end

to calculate-herding-efficiency
  set herding-efficiency (sheepless-neighborhoods / (count patches with [not any? sheep-here])) * 100
end

to go
  ask shepherds
  [ ifelse carried-sheep = nobody
      [ search-for-sheep ]     ;; find a sheep and pick it up
    [ ifelse found-herd?
        [ find-empty-spot ]  ;; find an empty spot to drop the sheep
      [ find-new-herd ] ]  ;; find a herd to drop the sheep in
    wiggle
    fd 1
    if carried-sheep != nobody
    ;; bring my sheep to where I just moved to
    [ ask carried-sheep [ move-to myself ] ] ]
  ask sheep with [not hidden?]
  [ wiggle
    fd sheep-speed ]
  tick
end

to wiggle        ;; turtle procedure
  rt random 50 - random 50
end

to search-for-sheep ;; shepherds procedure
  set carried-sheep one-of sheep-here with [not hidden?]
  if (carried-sheep != nobody)
    [ ask carried-sheep
        [ ht ]           ;; make the sheep invisible to other shepherds
      set color blue     ;; turn shepherd blue while carrying sheep
      fd 1 ]
end

to find-new-herd ;; shepherds procedure
  if any? sheep-here with [not hidden?]
    [ set found-herd? true ]
end

to find-empty-spot ;; shepherds procedure
  if all? sheep-here [hidden?]
    [ ask carried-sheep
        [ st ]                ;; make the sheep visible again
      set color brown         ;; set my own color back to brown
      set carried-sheep nobody
      set found-herd? false
      rt random 360
      fd 20 ]
end
@#$#@#$#@
GRAPHICS-WINDOW
248
10
666
449
25
25
8.0
1
10
1
1
1
0
1
1
1
-25
25
-25
25
1
1
1
ticks

PLOT
7
241
237
414
Herding Efficiency
Time
Percent
0.0
300.0
0.0
100.0
true
false
"" ""
PENS
"efficiency" 1.0 0 -13345367 true "" "if ticks mod 50 = 0  ;; since the calculations are expensive\n[\n  update-sheep-counts\n  calculate-herding-efficiency\n  plotxy ticks herding-efficiency\n]\n"

SLIDER
38
115
208
148
num-sheep
num-sheep
0
500
150
1
1
NIL
HORIZONTAL

SLIDER
38
79
208
112
num-shepherds
num-shepherds
0
100
30
1
1
NIL
HORIZONTAL

BUTTON
55
38
112
71
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
133
38
190
71
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
38
152
208
185
sheep-speed
sheep-speed
0
0.2
0.02
0.01
1
NIL
HORIZONTAL

MONITOR
63
192
176
237
current efficiency
herding-efficiency
1
1
11

@#$#@#$#@
## WHAT IS IT?

This project is inspired by two simpler models: one of termites gathering wood chips into piles and one of moving sheep.  In this project, sheep wander randomly while shepherds circulate trying to herd them.  Whether or not the sheep eventually end up in a single herd depends on the number of shepherds and how fast they move compared to the sheep.

## HOW IT WORKS

The shepherds follow a set of simple rules.  Each shepherd starts wandering randomly.  If it bumps into a sheep, it picks the sheep up, and continues to wander randomly. When it bumps into another sheep, it finds a nearby empty space, puts its sheep down, and looks for another one.

## HOW TO USE IT

Click the SETUP button to set up the shepherds (brown) and sheep (white).  Click the GO button to start the simulation.   A shepherd turns blue when it is carrying a sheep.

There are three sliders.  NUM-SHEEP and NUM-SHEPHERDS control the numbers of sheep and shepherds, respectively.   Changes in these sliders do not take effect until the next setup.

The SHEEP-SPEED slider controls the speed of the sheep relative to the shepherds.  This slider can be changed while the model is running.

While the simulation runs, a plot of the shepherds' herding efficiency is displayed.  Herding efficiency is measured here by counting the number of patches that have no sheep in their neighborhood:

efficiency = sheepless neighborhoods / (# of patches - # of sheep)
[expressed as a percentage]

As the shepherds herd the sheep, more of the neighborhoods should be empty. The measure of efficiency is fairly arbitrary; other measures could be devised.

## THINGS TO NOTICE

As small herds of sheep begin to form, the herds are not "protected" in any way.  That is, shepherds sometimes take sheep away from existing herds.  That strategy might seem counter-productive. But if the herds were "protected", you would end up with lots of little herds, not several big ones.   Why is this?

In general, if there are enough shepherds and/or the shepherds move much faster than the sheep, the number of herds decreases with time. Why?  One explanation is as follows: some herds disappear, when shepherds carry away all of the sheep.  If sheep never moved, it would not be possible for a new herd to start from scratch, since shepherds always put their sheep near other sheep.  So the number of herds would necessarily decrease over time.  (The only way a "new" herd would start is when an existing herd splits into two.) However, since sheep move, they can form new herds. If they move too fast relative to the shepherds, the herding will break down.

If there are not enough shepherds, or if the sheep move fast enough relative to the shepherds, the shepherds cannot keep up with the wanderings of their sheep, and the sheep will disperse.

Are the final herds roughly round?  What other physical situations also produce round things?

This project is a good example of a probabilistic and decentralized strategy.  There is no shepherd in charge, and no special pre-designated site for the herds. The movement of the shepherds and sheep and thus their behavior is probabilistic. Each shepherd follows a set of simple rules, but the group as a whole accomplishes a rather sophisticated task.

## THINGS TO TRY

Can you find the minimum number of shepherds needed to herd a given number of sheep?  Which helps more, doubling the number of shepherds or doubling the speed of the existing shepherds (by cutting the SHEEP-SPEED in half)?

How many sheep can one individual shepherd keep in a group?

Start with a SHEEP-SPEED of zero (the sheep stay put), let the shepherd gather them into herds, and then slowly increase the SHEEP-SPEED.   How is the herding efficiency affected?   How high does SHEEP-SPEED need to be for the shepherds to be useless, that is, for the herding efficiency returns to its initial value?  This is the same as saying that the distribution of sheep is no better than random.

When there are just two or three herds left, which of them is most  likely to "win" as the single, final herd?  How often does  the larger of the two herds win?  If one herd has only a  single sheep, and the other herd has the rest of the sheep, what are the chances that the first herd will win?

Compare this model to "Termites".  It runs slower, but aside from that, are the results the same?

In both the Termites and the Shepherds models, if the turtles don't jump away from the piles/herds they make, piling/herding happens more slowly and to a lesser extent.  Does this make sense?  Experiment with different search commands that you might give the shepherds besides "fd 1" and "jump 20".

## EXTENDING THE MODEL

Can you find other ways to measure herding efficiency?

Can you extend the model so that sheep follow each other, tending to cluster?

Can you extend the model to have the shepherds sort white sheep from black sheep?

Can you change the model so that there's only ever one sheep on a patch?  Does it change the behavior of the model?

The way the model is currently written, multiple sheep are allowed to occupy the same physical location.  And, since all shepherds search for a sheep to pick up before any of them actually take their sheep away, a shepherd may come to a location with several sheep and, examining one at random, find that another shepherd has already laid a hold of that sheep.  Currently shepherds give up on all sheep at that location when this happens, rather than seeing if there are other sheep there which are still unattended.  (If shepherds did not check to see whether a sheep was attended, multiple shepherds might each pick up the same sheep and take it away, thereby cloning it!)  Can you find a way to make shepherds check all sheep at a location before leaving?

Real shepherds often use sheepdogs to help them with their herding.  A sheepdog in this context might put down some chemical which "scares" the sheep, i.e., wandering sheep try to avoid it and move down gradient.  Can you implement sheepdogs and see how helpful they are?  Can you come up with a rough equivalence of how many shepherds a sheepdog can replace (to maintain the same herding efficiency), or how many sheepdogs are needed to replace a single shepherd?

Since it would be difficult to force sheep-turtles to follow shepherd-turtles which have "picked them up", the mechanics of picking a sheep up actually involve "killing" the sheep and creating a sheep-shepherd collective which wanders around following shepherd rules until it finds a place to put the sheep down, at which point another sheep is "created" at that location, and the collective reverts to being a normal shepherd again. Can you change the model so the shepherds actually herd the sheep rather than killing them and recreating them?

## NETLOGO FEATURES

Compare this code to that of "Termites".  It's similar, except that sheep and shepherds are both turtles here, and active, while in Termites the wood chips are patches and remain passive.  As a result, the Termites model runs faster.

The two models are coded somewhat differently, however.  Termites uses a loops-within-GO structure that is worth noting.  GO is a turtle forever button, so each turtle executes the code in the GO function in parallel and independently of each other.  Since each turtle moves through the GO function at its own pace, it's OK for functions like search-for-chip to be written as loops, which execute repeatedly until a certain condition is satisfied.  Then each turtle goes on to the next loop.

But in this model, GO is an observer forever button, because we want to have separate "ask" blocks for the sheeps and the shepherds, and because we want to update the plot every so often.  Since the observer waits for all of the turtles to finish the "ask" before moving on to the next line of code, it wouldn't be OK for a turtle to take more than one step inside the "ask" -- because then all the other turtles would have to wait for it.  So instead of using loops, this model uses the boolean variables carrying-sheep? and found-herd? to keep track of whether each turtle is in sheep-finding mode, herd-finding mode, or empty-spot-finding mode.

## RELATED MODELS

* Termites
* Painted Desert Challenge
* State Machine Example

## CREDITS AND REFERENCES

Thanks to Christopher Kribs Zaleta for his help with converting the model from StarLogoT to NetLogo.
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

sheep
false
0
Rectangle -16777216 true false 166 225 195 285
Rectangle -16777216 true false 62 225 90 285
Rectangle -7500403 true true 30 75 210 225
Circle -7500403 true true 135 75 150
Circle -16777216 true false 180 76 116

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

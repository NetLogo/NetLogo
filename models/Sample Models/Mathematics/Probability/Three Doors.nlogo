globals [
  done?  ; indicates whether the race is over or not yet
]

turtles-own [
  guess      ; what each turtle guesses to be the right door
  prize      ; where the prize is located
  other-door ; the number of the door that the host or hostess does NOT open
  hunch      ; the bias each turtle has to switch or not to switch doors
]

to setup
  clear-all
  ask patches [ set pcolor green ]
  set done? false  ; the race is not over yet
  crt number
  [
    ifelse (gradation?)
      [ set hunch (random-float 100) ]             ; gradation is on, so give a hunch BETWEEN 0 and 100
      [ set hunch ((random 2) * 100 )]       ; gradation is off, so give a hunch of 0 or 100
    set color scale-color gray hunch 100 0 ; set the color based on the hunch of the turtles
    set heading 0
    ; spread the turtles out across the bottom of the world
    setxy random-xcor min-pycor
  ]
  reset-ticks
end

to go
  if done? [ stop ]
  ask turtles
  [
    make-choices  ; choose a door that the turtles will choose, a door for the prize, and set the other door
    do-switch?    ; should each turtle switch to the other door?
    award-prizes  ; make the winning turtles go forward and don't do anything if the race has been won
  ]
  tick
end

; choose a door that the turtles will choose (guess), a door for the prize (prize), and set the other door that is not opened (other)
to make-choices
  set prize (random 3)
  set guess (random 3)
  if (prize = 0)  ; the prize is behind door number 0
  [
    ifelse (guess = 0)
    ; the turtle guessed door 0
      [ set other-door ((random 2) + 1) ]  ; since the prize is in the door the turtle picked, pick door 1 or 2 to not be opened by the host or hostess
    ; the turtle guessed door 1 or 2
    ; it doesn't matter which door is picked to be not opened since both doors 1 and 2 have the junk behind them
      [ set other-door 0 ]
  ]
  if (prize = 1)  ; the prize is behind door number 1
  [
    ifelse (guess = 0)
    ; the turtle guessed door 0- a door with junk behind it
      [ set other-door 1 ]  ; so set the door not to be opened to door 1, the door with the prize behind it.  now door 2- with junk behind it- can be opened.
      [
        ifelse (guess = 1)
        ; the turtle guessed door 1
          [ set other-door ((random 2) * 2) ]  ; since the prize is in the door the turtle picked, pick door 0 or 2 to not be opened by the host or hostess
        ; the turtle guessed door 2- a door with junk behind it
          [ set other-door 1 ]  ; so set the door not to be opened to door 1, the door with the prize behind it.  now door 0- with junk behind it- can be opened.
      ]
  ]
  if (prize = 2)  ; the prize is behind door number 2
  [
    ifelse (guess = 0)
    ; the turtle guessed door 0- a door with junk behind it
      [ set other-door 2 ]  ; so set the door not to be opened to door 2, the door with the prize behind it.  now door 2- with junk behind it- can be opened.
      [
        ifelse (guess = 1)
        ; the turtle guessed door 1- a door with junk behind it
          [ set other-door 2 ]    ; so set the door not to be opened to door 2, the door with the prize behind it.  now door 0- with junk behind it- can be opened.
        ; the turtle guessed door 2
          [ set other-door (random 2) ]  ; since the prize is in the door the turtle picked, pick door 0 or 1 to not be opened by the host or hostess
      ]
  ]
end

; should each turtle switch to the other door?  this is based on the hunch in each turtle
to do-switch?
  if (random-float 100 < hunch)
    [ set guess other-door ]
end

; make the winning turtles go forward and don't do anything if the race has been won
to award-prizes
  if (guess = prize)
  [
    if (not done?)  ; if the race is not done, move the winners forward by 1 patch
    [ fd 1 ]
  ]
  ; if a turtle has reached the top of the world, set done? to be true
  if (ycor = max-pycor)
    [ set done? true ]
end
@#$#@#$#@
GRAPHICS-WINDOW
202
10
638
467
35
35
6.0
1
13
1
1
1
0
1
0
1
-35
35
-35
35
1
1
1
ticks
30

SLIDER
4
36
194
69
number
number
1
1000
300
1
1
NIL
HORIZONTAL

BUTTON
26
229
92
262
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
104
229
173
262
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

SWITCH
39
83
164
116
gradation?
gradation?
1
1
-1000

TEXTBOX
311
478
555
521
Darker turtles switch doors more often.\nLighter turtles switch doors less often.
11
0.0
0

TEXTBOX
19
131
174
225
if Gradation is off, all turtles stick with one strategy, either the \"stick\" or the \"switch\" strategy. If Gradation is on, they do mixed strategies.
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

You are a contestant on a game show.  You, of all people, have made it to the final round, where you have the chance to win some fabulous prize --- a car, a million dollars, eternal youth, etc...  The host or hostess of the game show takes you up on stage, where you stand before three doors, marked "0", "1", and "2".

"Your prize is behind one of these three doors.  Behind the other two are goats, and you don't want these goats.  So, which one will you pick?" she or he says.  You make your selection, and reach for the door -- but before you open it, the hostess or host says to you: "All right, now I'm going to give you a choice."  She or he opens another one of the doors, one you didn't pick.  And sure enough, there's an ugly old goat.

The hostess or host then points to the other door, saying: "If you want, you can change your mind, and pick the last door; the door you didn't pick that I didn't open.  So now, which will it be?  Your first selection?  Or the other door?"  You think for a brief moment...

Which will it be?  Will you stick with your first choice, or switch to the other door?  Does it matter?  Do you have better odds of winning the prize by switching or sticking?  Or is it even odds either way?  (We're assuming here that you want the prize and have an aversion to goats.  If you really would prefer the goat, then the prize is the goat, and the two other doors are empty, or contain mulch, whatever...)

This is a classic puzzle in thinking about probability.  It has several other names: the 'Monty Hall' puzzle (named after the host of the game show this is taken from: "Let's Make a Deal") and 'Goats-and-Car' being the two most common.  The puzzle is based around the questions being asked above, which really boil down to one question at the heart of the matter: which is it better to do, stick or switch?

## HOW IT WORKS

An arbitrary number of turtles (around several hundred, usually) gather together at the bottom of the world.  They then begin to play this three-doors game over and over again.  The prize they can win is a turtle-command: `fd 1`.  If they lose, nothing happens.  Thus, the turtles are having a race to the top of the world, and they can only advance in the race if they win a prize.

Turtles determine the results of each round of the contest by themselves, alternately acting as contestant and host or hostess.  Also, each turtle has a 'hunch' - a percentage chance that it will switch to the other door.  The value of 'hunch' determines a turtle's color.  A turtle that always sticks with its original choice of doors has a hunch of 0 and is colored white.  A turtle that always switches its choice of doors has a hunch of 100 and is colored black.

## HOW TO USE IT

Before you begin playing with the model, read the paragraphs above.  Ask yourself what *you* would do --- stick with your initial pick, or switch to the other door.  Why?  Try to come up with an argument for your decision.  And then, think about the alternative.  Suppose you think it's 'better' to switch.  Why do you think so?  How much better do you think it is?  Why might it be better to stick with your initial decision?  Think about those questions, and talk about them with some of your peers (explain the scenario if they haven't already heard it).

This model is quite easy to run.  First, select the number of turtles you want to be present with the NUMBER slider.  (The more, the better, but you don't want to sacrifice too much speed.)  When you are ready to begin, press the SETUP button.  This will spread turtles randomly across the bottom row of patches.

Make a prediction as to how the different colors of turtles will perform in their race.  The turtles will repeatedly play the three-doors game, until one turtle has made it to the top of the world.  Finally, when you are all set, press the GO button.

If the GRADATION? switch is on, each turtle will be assigned a random "hunch" *between* 0 and 100 (instead of exactly 0 or 100).  Turtles who tend to "stick" will have a lower hunch and be lighter. Turtles who tend to "switch" will have a larger hunch and be darker.

## THINGS TO NOTICE

The main point of this model is demonstrated by the results of the turtles' race.  Try to understand these results.  Run the model a second time, with the same value for NUMBER and see what happens.  Do you accept these results?  In light of what you have observed, ask yourself the above questions again.  Is it better for a turtle to 'play its hunch'?

Try running the model with many more turtles, or just a few turtles.  How does the number of turtles relate to the behavior?

In the first paragraph of HOW TO USE IT above, it asks you to discuss this puzzle with some of your peers.  Important 'things to notice' are their own arguments one way or another.  How vehement were their beliefs?  How sound were their arguments?  How did you react to what they said, and how did they react to the results of this project?

The host or hostess will always open a door that contains a goat --- we're assuming that he or she has perfect information about the location of the prize.  Of what importance is this to the model?  How would the model be different if she or he opened a random door instead of a goat-door?

## THINGS TO TRY

Turn the GRADATION? switch on. How does the pattern of turtles change?

Look at the code for this model, especially if you disagree with its results.  Try to step through the procedure `make-choices`.  Do you think this procedure, and the model as a whole, is fair?

Next, try writing your own procedure for one round of the three-doors contest.  Compare its performance with what you've seen here.

Find someone who hasn't yet seen this model, or thought about this problem before.  Ask them for their opinion, and listen to their argument in defense of their choice.  Try not to argue with them (yet), just hear them out.  Then have them run this model, and see what they think.  If you've looked at the code, try to explain to them what is really going on.

## EXTENDING THE MODEL

This model has been kept very simple for a good reason --- to allow you to expand upon it at your leisure.  There are several directions in which it could go.  First of all, as mentioned above, try writing a different set of procedures for running a round of the contest.

Change the number of doors from three to four, or five, or n (where n could be a slider value).  How will this change the model?  You also might consider having several doors lead to prizes.

If there are n doors, and m different kinds of prizes, as suggested above, you could assign different values to the different prizes.  Maybe create different breeds of turtles, each of which has a different set of prize values?

Regardless of what you do, always make sure to make a prediction about what you think will happen, and to compare the actual results with what you see.

Try inventing a way to display how well each rule does, on average.  You could use monitors or plots.

Create a plot of the effectiveness of the different strategies and setups of the problem described above.

## NETLOGO FEATURES

As the turtles are all competing with one another, it's important that they all take the same amount of time to 'do a round'.  If you have many turtles, it's possible that some turtles could cheat, and get ahead of the others, just by getting through the code quicker.  Thus, we make use of an `ask turtles` block in the `go` procedure.  Whenever you use `ask`, all the agents must finish the block of commands before any agents will continue.  This makes sure that all turtles have caught up to the end of the block.

Note that it does no harm to have a turtle determine the prize door itself --- it does not use this knowledge when deciding which door to pick.

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

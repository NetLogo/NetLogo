extensions [ sound ]

breed [ trucks truck ]
breed [ cars car ]
breed [ logs a-log ]
breed [ river-turtles river-turtle ]
breed [ pads pad ]
breed [ frogs frog ] ;; These are all the game pieces.

;;;;;;;;;;;;;;;
;; Variables ;;
;;;;;;;;;;;;;;;

globals [
  action            ;; Last button pressed. Prevent the player from moving the frog until the
                    ;; the game is running.  Checks the status of this button every loop.
  dead?             ;; True when no frog lives are left - used to stop the game
  lives             ;; Remaining lives
  level             ;; Current level
  jumps             ;; Current number of jumps
  time-left         ;; Time remaining
  pads-done         ;; Number of frogs that have successfully reached the pads
]

;; In NetLogo, all the breeds are "turtles".  This can be confusing because
;; there are also "turtles" in the game of Frogger -- they swim in the river.
;; To avoid confusion, we call those turtles "river-turtles".

turtles-own [
  speed            ;; The 'time' variable will be initialized to the value of 'speed' after the turtle moves
  time             ;; This keeps track of how many time loops have occurred since the turtle last moved.
                   ;; It actually counts down from 'speed' to zero.  Once it reaches zero, the turtle
                   ;; moves forward one space
]

river-turtles-own [
  dive?            ;; True when the turtle dives
]

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to startup            ;; Setup is the 'New Game' button, this will setup the game.
  setup
end

to setup              ;; Initializes the game
  ca
  set action 0
  set dead? false
  set lives start-lives
  set-default-shape frogs "frog"
  set-default-shape cars "car"
  set-default-shape logs "log"
  set-default-shape river-turtles "turtle"
  set level start-level
  next-level
end

to next-level        ;; This will call the appropriate level procedure, where the level is created
  draw-map
  if ( level = 1 )
    [ level-1 ]
  if ( level = 2 )
    [ level-2 ]
  if ( level = 3 )
    [ level-3 ]
  if ( level = 4 )
    [ level-4 ]
  if ( level = 5 )
    [ level-5 ]
  if ( level = 6 )
    [ user-message "Actually, that was the last level.\nPerhaps you should program some more :-)"
      set dead? true]
end

;; This will color the patches to make the grass, road, and river, and creates the frog.
;; The second line causes the grass to be various similar shades of green so it looks
;; more like real grass.

to draw-map
  cp ct
  ask patches
    [ set pcolor scale-color green ((random 500) + 5000) 0 9000 ]
  setup-pads
  ask patches with [pycor <= max-pycor and pycor >= 3]
    [ set pcolor blue ]
  ask patches with [pycor <= -1 and pycor >= -5]
    [ set pcolor gray ]
  set pads-done 0
  create-frogs 1
    [ set color 53
      reset-frog
    ]
end

;; Initializes the frog by setting it to the right patch and facing the right direction

to reset-frog
  setxy 0 min-pycor
  set heading 0
  set jumps 0
  set time-left start-time
end

;; Creates the five pads equally spaced at the top of the board.
;; The second line uses the modulus operation to determine which x-cor
;; is divisible by three.  This is an easy way to have a pad created every
;; three patches.

to setup-pads
  set-default-shape pads "pad"
  ask patches with [pycor = max-pycor and pxcor mod 3 = 0]
    [ sprout-pads 1 ]
end

to create-truck [ x y direction quickness ]   ;; Creates and initializes a truck
  let truckColor (random 13 + 1) * 10 + 3
  ask patches with [(pxcor = x or pxcor = (x + 1)) and pycor = y]
    [ sprout-trucks 1
        [ set color truckColor
          set heading direction
          set speed quickness
          set time speed
          ifelse ((pxcor = x) xor (direction = 90))
            [ set shape "truck" ]
            [ set shape "truck rear" ]
        ]
    ]
end

to create-car [x y direction quickness]     ;; Creates and initializes a car
  create-cars 1
    [ set color (random 13 + 1) * 10 + 3
      setxy x y
      set heading direction
      set speed quickness
      set time speed
    ]
end

;; Creates and initializes a log.

to create-log [x y leng quickness]
  ask patches with [pycor = y and pxcor >= x and pxcor < (x + leng)]
    [ sprout-logs 1
        [ set color brown
          set heading 90
          set speed quickness
          set time speed
        ]
    ]
end

to create-river-turtle [x y leng quickness]    ;; Creates and initializes a river-turtle
  ask patches with [pycor = y and pxcor >= x and pxcor < (x + leng)]
    [ sprout-river-turtles 1
        [ set heading 270
          set speed quickness
          set time speed
          set color 54
          set dive? false
        ]
    ]
end

to make-river-turtle-dive [num]    ;; Causes a random river-turtle(s) to dive underwater.
  repeat num
    [ ask one-of river-turtles with [not dive?]
        [ set dive? true ]
    ]
end


;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Runtime Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;


to go            ;; The main procedure
  if dead?
    [ stop ]
  move
end

;; This is the time loop: every 0.1 seconds it decrements every turtle's 'time'
;; variable and check to see if it should move (when it reaches zero).  It then will
;; reset the 'time' if it is zero.  The logs and river-turtles need their own special
;; procedure to move since they "carry" the frog with them.

to move
  move-frog
  every 0.1
    [ ask turtles
        [ decrement-time ]
      ask turtles with [time = 0.0 and breed != frogs]
        [ set time speed
          ifelse (breed = logs)
            [ move-log ]
            [ ifelse (breed = river-turtles)
                [ move-river-turtle ]
                [ fd 1 ]
            ]
        ]
      check-frog
    ]
  display
end

;; This will decrement the 'time' for all non-frogs and it will decrement the 'time-left'
;; global variable.  The precision function is needed to verify there is only one decimal
;; place on the time variables.

to decrement-time
  ifelse (breed = frogs)
    [ set time-left precision (time-left - 0.1) 1 ]
    [ set time precision (time - 0.1) 1 ]
end

;;  Every time loop, we need to see what the frog's status is (dead, on a pad, etc..)
;;  First it will need to see if it is on a pad and make sure there are no other frogs there
;;  (by checking the shape of the the pad).  Then you need to check to see if the frog is in
;;  a space where he should die.  Finally, it checks to see if the level is complete.

to check-frog
  ask frogs
    [ if any? pads-here with [shape = "pad"]
        [ sound:play-drum "CRASH CYMBAL 2" 97
          ask pads-here
            [ set shape "frog"
              set heading 0
              set color 54
              set pads-done (pads-done + 1)
            ]
          reset-frog
        ]
      if ((any? trucks-here) or (any? cars-here) or (time-left <= 0) or
         ((pcolor = blue) and
          (count pads-here = 0) and
          (count logs-here = 0) and
          (count river-turtles-here with [not hidden?] = 0)))
        [ kill-frog ]
    ]
  if ( pads-done = 5 )
    [ set level (level + 1)
      set pads-done 0
      user-message (word "Congrats, all your frogs are safe!\nOn to level " level "...")
      next-level
    ]
end

to kill-frog        ;; This is called when the frog dies, checks if the game is over
  set lives (lives - 1)
  ifelse (lives = 0)
    [ user-message "Your frog died!\nYou have no more frogs!\nGAME OVER!"
      set dead? true
      die
    ]
    [ user-message (word "Your frog died!\nYou have " lives " frogs left.")
      reset-frog
    ]
end

;; This is a special procedure to move a log.  It needs to move any frogs that
;; are on top of it.

to move-log
  ask frogs-here
    [ if (pxcor != max-pxcor)
        [ set xcor xcor + 1 ]
    ]
  fd 1
end

;; This is a special procedure to move the river-turtles.  It needs to move any frogs that
;; are on top of it.

to move-river-turtle
  fd 1
  ask frogs-at 1 0
    [ set xcor xcor - 1
      if (xcor = max-pxcor)
        [ set xcor xcor + 1 ]
    ]
  dive-river-turtle
end

;; If a river-turtle has been instructed to dive, this procedure will implement that.
;; It will also cause it to splash and rise back up.  It uses a random numbers to
;; determine when it should dive and rise back up.  Theoritically, it will dive about
;; every eighth move and stay down for about five moves, but this isn't always the case
;; (the randomness is added for increasing the challenge of the game)

to dive-river-turtle
  if dive?
    [ ifelse (hidden? and random 5 = 1)
        [ show-turtle ]
        [ if ( shape = "splash" )
            [ set shape "turtle"
              hide-turtle
            ]
          if (shape = "turtle" and random 8 = 1)
            [ set shape "splash" ]
        ]
    ]
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Interface Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to move-frog
  if (action != 0)
    [ if (action = 1)
        [ move-left ]
      if (action = 2)
        [ move-right ]
      if (action = 3)
        [ move-down ]
      if (action = 4)
        [ move-up ]
      sound:play-drum "LONG GUIRO" 50
      set action 0
    ]
end

to move-left
  ask frogs with [xcor != min-pxcor]
    [ set heading 270
      fd 1
      set jumps ( jumps + 1 )
    ]
  check-frog
end

to move-right
  ask frogs with [xcor != max-pxcor]
    [ set heading 90
      fd 1
      set jumps ( jumps + 1 )
    ]
  check-frog
end

to move-up
  ask frogs with [ycor != max-pycor]
    [ set heading 0
      fd 1
      set jumps ( jumps + 1 )
    ]
  check-frog
end

to move-down
  ask frogs with [ycor != min-pycor]
    [ set heading 180
      fd 1
      set jumps ( jumps + 1 )
    ]
  check-frog
end


;;;;;;;;;;;;;;
;;; Levels ;;;
;;;;;;;;;;;;;;

to level-1
  create-truck 5 -5 270 .9
  create-truck 0 -5 270 .9
  create-truck -8 -4 90 .9
  create-truck -5 -4 90 .9
  create-truck 2 -4 90 .9
  create-truck -3 -3 270 .8
  create-truck 6 -3 270 .8
  create-car 0 -2 90 .4
  create-car -4 -2 90 .4
  create-car 8 -1 270 .2
  create-car 3 -1 270 .2
  create-log 4 3 3 .6
  create-log -8 3 5 .6
  create-log 4 5 2 .7
  create-log -4 5 3 .7
  create-log 1 7 4 .3
  create-log -6 7 4 .3
  create-river-turtle 2 4 2 .4
  create-river-turtle -4 4 4 .4
  create-river-turtle 5 4 4 .4
  create-river-turtle -3 6 4 .5
  create-river-turtle 7 6 3 .5
end

to level-2
  create-truck 4 -5 270 .8
  create-truck -3 -5 270 .8
  create-truck 0 -4 90 .9
  create-truck -4 -4 90 .9
  create-truck -1 -3 270 .8
  create-truck 4 -3 270 .8
  create-truck -5 -3 270 .8
  create-car 0 -2 90 .2
  create-car -4 -2 90 .2
  create-car 8 -2 90 .2
  create-car 6 -1 270 .4
  create-car 2 -1 270 .4
  create-car -3 -1 270 .4
  create-car -6 -1 270 .4
  create-log 6 3 3 .6
  create-log -4 3 4 .6
  create-log 0 5 3 .3
  create-log -6 5 3 .3
  create-log 1 7 4 .5
  create-log 6 7 4 .5
  create-river-turtle 0 4 4 .3
  create-river-turtle 6 4 4 .3
  create-river-turtle 0 6 4 .4
  create-river-turtle 6 6 3 .4
  make-river-turtle-dive 1
end

to level-3
  create-truck -8 -5 270 .7
  create-truck -4 -5 270 .7
  create-truck 0 -5 270 .7
  create-truck -2 -4 90 .7
  create-truck 2 -4 90 .7
  create-truck -6 -4 90 .7
  create-truck -4 -3 270 .7
  create-truck 0 -3 270 .7
  create-truck 4 -3 270 .7
  create-car -3 -2 90 .2
  create-car -5 -2 90 .2
  create-car 5 -2 90 .2
  create-car 1 -2 90 .2
  create-car 0 -1 270 .3
  create-car 5 -1 270 .3
  create-car -7 -1 270 .3
  create-car -3 -1 270 .3
  create-log -6 3 4 .4
  create-log -2 5 3 .4
  create-log 5 5 3 .4
  create-log -4 7 2 .2
  create-log 0 7 2 .2
  create-log 4 7 2 .2
  create-river-turtle -4 4 4 .3
  create-river-turtle 5 4 4 .3
  create-river-turtle -1 6 3 .4
  create-river-turtle -8 6 3 .4
  make-river-turtle-dive 3
end

to level-4
  create-truck -8 -5 270 .5
  create-truck -2 -5 270 .5
  create-truck 6 -5 270 .5
  create-truck 4 -4 90 .6
  create-truck -1 -4 90 .6
  create-truck -6 -4 90 .6
  create-car -4 -3 270 .3
  create-car 0 -3 270 .3
  create-car 4 -3 270 .3
  create-car 7 -3 270 .3
  create-car -3 -2 90 .2
  create-car -5 -2 90 .2
  create-car 5 -2 90 .2
  create-car 1 -2 90 .2
  create-car 0 -1 270 .3
  create-car 5 -1 270 .3
  create-car -7 -1 270 .3
  create-car -3 -1 270 .3
  create-log -3 3 3 .3
  create-log -3 5 3 .3
  create-log -3 7 3 .3
  create-river-turtle -4 4 4 .3
  create-river-turtle 4 4 4 .3
  create-river-turtle -7 4 1 .3
  create-river-turtle -1 6 3 .4
  create-river-turtle -8 6 3 .4
  create-river-turtle 3 6 2 .4
  make-river-turtle-dive 4
end

to level-5
  create-car -4 -5 270 .3
  create-car 0 -5 270 .3
  create-car 4 -5 270 .3
  create-car 7 -5 270 .3
  create-car -3 -4 90 .2
  create-car -5 -4 90 .2
  create-car 5 -4 90 .2
  create-car 1 -4 90 .2
  create-car 8 -4 90 .2
  create-car -4 -3 270 .3
  create-car 0 -3 270 .3
  create-car 4 -3 270 .3
  create-car 7 -3 270 .3
  create-car -3 -2 90 .2
  create-car -5 -2 90 .2
  create-car 4 -2 90 .2
  create-car 1 -2 90 .2
  create-car 7 -2 90 .2
  create-car 0 -1 270 .3
  create-car 5 -1 270 .3
  create-car -7 -1 270 .3
  create-car -3 -1 270 .3
  create-log -5 3 2 .2
  create-log 0 5 2 .1
  create-log -5 7 2 .2
  create-river-turtle -4 4 2 .3
  create-river-turtle 4 4 3 .3
  create-river-turtle -7 4 2 .3
  create-river-turtle -1 6 2 .3
  create-river-turtle -8 6 2 .3
  create-river-turtle 3 6 3 .3
  make-river-turtle-dive 5
end
@#$#@#$#@
GRAPHICS-WINDOW
316
10
666
381
8
8
20.0
1
10
1
1
1
0
1
0
1
-8
8
-8
8
1
1
0
ticks

BUTTON
105
344
172
381
LEFT
set action 1
NIL
1
T
OBSERVER
NIL
J
NIL
NIL
1

BUTTON
236
344
303
381
RIGHT
set action 2
NIL
1
T
OBSERVER
NIL
L
NIL
NIL
1

BUTTON
171
307
237
344
UP
set action 4
NIL
1
T
OBSERVER
NIL
I
NIL
NIL
1

BUTTON
172
344
236
381
DOWN
set action 3
NIL
1
T
OBSERVER
NIL
K
NIL
NIL
1

BUTTON
23
24
126
57
New Game
setup
NIL
1
T
OBSERVER
NIL
N
NIL
NIL
1

BUTTON
169
23
272
56
Start
go
T
1
T
OBSERVER
NIL
S
NIL
NIL
1

MONITOR
168
80
281
125
Frogs Left
lives
0
1
11

MONITOR
168
131
281
176
NIL
Level
0
1
11

MONITOR
168
182
281
227
Time Left
time-left
3
1
11

SLIDER
8
80
157
113
start-lives
start-lives
1
5
5
1
1
NIL
HORIZONTAL

SLIDER
8
180
157
213
start-time
start-time
10
60
60
5
1
NIL
HORIZONTAL

SLIDER
8
129
157
162
start-level
start-level
1
5
1
1
1
NIL
HORIZONTAL

MONITOR
168
233
281
278
Frog Jumps
jumps
0
1
11

@#$#@#$#@
## WHAT IS IT?

This model is based on the classic arcade game, Frogger.  The object of the game is to get the frog, found at the bottom of the view, across the traffic and river to a safe lily pad on the other side.

## HOW IT WORKS

There are two main obstacles to overcome, the road and the river.  The road has cars and trucks moving at various speeds that are liable to run over the frog.  Once you have crossed the road safely, you must overcome the danger lurking in the river.  Unfortunately, you will die if you jump in the river, so you must keep moving towards the lily pads by jumping on the logs or sets of turtles moving back and forth in the river's current.

You must also avoid getting pushed off the edge by a log or turtle.  In addition, in the later levels, some of the turtles will dive under water -- if you happen to be standing on them you will drown!  Finally, you must also get across the board before the allotted amount of time runs out.

## HOW TO USE IT

Buttons

- NEW-GAME resets the game
- START starts the game
- The direction buttons (UP, DOWN, LEFT, RIGHT) will move your frog in that direction

Monitors

- FROGS LEFT tells you how many remaining lives you have
- LEVEL monitors the current level you are playing
- TIME LEFT shows you how much time remains
- FROG JUMPS tells you how many jumps you has taken

Sliders

- START-LIVES will determine how many lives you will start with
- START-TIME sets how much time you start out with
- START-LEVEL is used to determine which level you will start on

Cast of characters:

- Green frog: This is you.
- Truck: Avoid at all costs.  They are usually pretty slow.
- Car: Avoid at all costs.  They are usually fast.
- Brown squares: This is a log.  You need to jump onto these to get across the river.
- Turtle: You need to jump onto these.  Avoid ones that dive.
- Green circles: These are lily pads.  You want to get on these to win the level.
- Blue squares: This is the river.  You can't land on this.
- Gray squares: This is the road.  You can jump on this, but watch out for vehicles.

- Green Patches:   This is grass.  You are pretty safe here.

## THINGS TO TRY

See if you can get through all of the levels.

Try to beat your previous time.

Try to make as few jumps as possible in the time allotted.

Try to use as few lives as possible.

## THINGS TO NOTICE

Determine how many jumps it would take to get across the board without obstacles.

Determine how many jumps it would take to get across the board with obstacles.

How does each of the two questions above relate to the time it takes you to complete a level?

If you take just as many jumps with obstacles as without, why does it take different durations of time to get across?

## EXTENDING THE MODEL

Write your own levels by altering the code in the Code tab.

Add some bonuses or additional hazards.

Implement a scoring system.

Write a robot script that will move your frog automatically.

## NETLOGO FEATURES

This model uses breeds to implement the different moving game pieces.

The `every` command is used to control the speed of the game.

The `user-message` command presents messages to the user.

`mouse-down?`, `mouse-xcor`, and `mouse-ycor` are used to detect and handle mouse clicks.

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
true
0
Rectangle -7500403 true true 75 30 225 278
Polygon -7500403 true true 75 28 91 14 211 16 225 30 211 54 79 38
Rectangle -11221820 true false 93 114 209 128
Rectangle -11221820 true false 97 232 205 240
Line -16777216 false 97 144 97 212
Line -16777216 false 203 146 203 208
Line -16777216 false 99 36 199 36
Line -16777216 false 201 36 201 98
Line -16777216 false 97 36 97 92
Line -16777216 false 97 92 121 68
Line -16777216 false 121 68 171 68
Line -16777216 false 171 68 177 68
Line -16777216 false 177 68 199 90
Rectangle -7500403 true true 195 92 207 98
Rectangle -7500403 true true 199 94 205 100
Rectangle -7500403 true true 99 39 199 99
Line -16777216 false 111 91 183 91
Circle -1184463 true false 75 13 16
Circle -1184463 true false 209 15 16
Polygon -7500403 true true 75 27 89 15 109 15 109 31
Polygon -7500403 true true 91 23 79 27 81 31
Polygon -7500403 true true 209 15 225 31 219 37 187 27
Circle -1184463 true false 211 13 12
Polygon -7500403 true true 209 15 221 27 203 29
Line -16777216 false 99 37 99 39
Line -16777216 false 99 91 99 35
Circle -1184463 true false 75 11 13
Line -16777216 false 199 37 199 91
Rectangle -7500403 true true 90 33 98 95
Circle -1184463 true false 211 15 12
Rectangle -7500403 true true 195 31 205 95
Line -16777216 false 195 37 195 89
Rectangle -7500403 true true 93 35 101 95
Rectangle -7500403 true true 191 33 199 95
Rectangle -7500403 true true 95 33 101 43
Rectangle -7500403 true true 97 31 103 43
Rectangle -2674135 true false 77 273 89 277
Rectangle -2674135 true false 211 273 223 277
Rectangle -2674135 true false 77 269 97 275
Rectangle -2674135 true false 205 271 221 277
Rectangle -2674135 true false 207 269 223 275
Rectangle -2674135 true false 87 269 95 275
Rectangle -7500403 true true 87 39 217 89
Rectangle -7500403 true true 89 23 205 99
Rectangle -7500403 true true 128 61 214 111
Rectangle -7500403 true true 77 31 147 111
Rectangle -16777216 true false 89 15 209 17
Rectangle -16777216 true false 91 17 211 19
Rectangle -7500403 true true 91 51 213 113
Rectangle -7500403 true true 77 265 223 277
Rectangle -7500403 true true 97 16 199 24
Rectangle -16777216 true false 99 22 197 28

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

frog
true
0
Polygon -10899396 true false 149 12 115 32 89 60 75 134 89 210 131 224 149 224 169 224 211 210 225 134 209 56 179 26 149 12
Polygon -10899396 true false 93 60 35 72 17 114 33 118 47 80 87 74
Polygon -10899396 true false 81 180 43 214 67 260 85 258 57 216 85 192
Polygon -10899396 true false 209 62 261 68 283 110 267 110 253 78 211 72 209 62
Polygon -10899396 true false 219 172 261 212 231 260 215 250 245 216 215 184
Circle -1 true false 112 41 23
Circle -1 true false 165 40 23
Circle -16777216 true false 119 48 8
Circle -16777216 true false 173 48 8
Polygon -7500403 true true 149 11 115 33 87 59 31 69 11 119 35 125 49 89 85 83 75 133 81 179 39 215 65 263 87 259 61 215 83 193 87 209 127 223 171 223 211 211 217 185 239 215 209 249 231 263 263 209 217 169 225 131 215 73 251 85 263 115 287 115 263 63 207 53 179 25 149 11

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

log
false
0
Rectangle -7500403 true true 0 0 300 300

pad
false
0
Polygon -13345367 true false 15 296 35 188 17 118 30 43 51 24 87 19 204 17 229 30 272 23 275 73 266 99 268 185 273 251 256 261 278 298
Polygon -13345367 true false 205 17 260 24 272 251 267 281 209 223
Circle -10899396 true false 51 124 65
Circle -10899396 true false 73 180 120
Circle -13840069 true false 102 39 143
Circle -10899396 true false 172 127 91
Circle -13840069 true false 34 168 74
Circle -13840069 true false 177 207 82
Circle -10899396 true false 46 25 79
Rectangle -13345367 true false 9 13 32 298
Polygon -13345367 true false 42 175 32 204 25 159
Rectangle -13345367 true false 24 13 42 44
Rectangle -13345367 true false 29 13 275 19
Rectangle -13345367 true false 264 13 284 299
Rectangle -13345367 true false 10 296 284 304
Polygon -10899396 true false 111 295 155 295 148 298
Polygon -10899396 true false 110 296 145 298
Polygon -10899396 true false 144 298 115 299 109 295 110 290
Polygon -13345367 true false 102 17 82 22 41 35 35 19 39 15
Polygon -13345367 true false 215 15 271 15 267 29
Polygon -13345367 true false 214 17 240 14 243 23 223 25
Rectangle -13345367 true false 16 18 279 342
Circle -13840069 true false 29 44 119
Circle -10899396 true false 50 138 168
Circle -13840069 true false 170 149 101
Circle -10899396 true false 129 29 123

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

splash
false
0
Polygon -1 true false 147 119 96 57 55 64 29 69 68 71 110 87
Polygon -1 true false 115 134 56 137 28 166 71 149
Polygon -1 true false 195 131 249 93 271 125 235 128
Polygon -1 true false 169 171 222 187 250 251 215 196
Polygon -1 true false 134 161 78 205 85 260 98 206
Circle -1 true false 111 110 72
Polygon -1 true false 148 110 195 127 207 154 153 183 101 159 111 132

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
true
0
Rectangle -16777216 true false 45 60 255 196
Polygon -16777216 true false 45 58 75 28 225 28 255 58 235 90 63 90
Rectangle -11221820 true false 77 50 223 60
Rectangle -16777216 true false 121 194 181 216
Rectangle -16777216 true false 121 196 181 228
Rectangle -1 true false 45 224 255 302
Rectangle -7500403 true true 45 224 255 312
Rectangle -1 true false 45 224 253 302
Polygon -7500403 true true 45 196 253 196 253 54 225 26 77 26 43 60
Polygon -7500403 true true 43 60 43 196 75 196 75 56 43 60
Rectangle -11221820 true false 79 40 221 50
Circle -16777216 true false 51 168 20
Circle -16777216 true false 221 168 20
Rectangle -1 true false 203 224 255 312
Polygon -7500403 true true 233 196 255 196 255 56 225 26 245 170 245 190
Rectangle -11221820 true false 79 40 221 56

truck rear
true
0
Rectangle -1 true false 45 0 255 282
Rectangle -7500403 true true 45 0 255 282
Rectangle -1 true false 45 0 255 282

turtle
true
0
Circle -10899396 true false 90 52 124
Circle -10899396 true false 90 70 119
Circle -10899396 true false 94 129 112
Polygon -10899396 true false 91 99 93 187 207 187 213 112 91 99
Polygon -7500403 true true 149 14 123 27 126 53 129 57 151 52 174 57 182 28
Polygon -7500403 true true 95 85 51 76 48 101 89 101
Polygon -7500403 true true 93 191 54 212 76 235 99 206
Polygon -7500403 true true 207 88 248 65 257 98
Polygon -7500403 true true 252 96 207 98 208 88 245 84
Polygon -7500403 true true 206 188 250 204 240 233 200 201 206 188
Circle -16777216 true false 133 30 7
Circle -16777216 true false 161 30 6
Line -7500403 true 149 79 149 192
Line -7500403 true 116 89 117 190
Line -7500403 true 190 89 190 184
Line -7500403 true 118 192 190 189
Line -7500403 true 117 89 145 79
Line -7500403 true 151 80 190 93
Line -7500403 true 117 122 148 113
Line -7500403 true 151 113 185 122
Line -7500403 true 119 160 185 158
Line -7500403 true 120 145 146 136
Line -7500403 true 146 136 188 143

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
need-to-manually-make-preview-for-this-model
;; (because it uses the sound extension)
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

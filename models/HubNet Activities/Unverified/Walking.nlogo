;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variable declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

globals
[
  ;;clock             ;; keeps track of the number of times modulus total-intervals through the go procedure
  num-intervals     ;; the number of time intervals a walker can move

  ;; variables for leaving a trail
  up-trail-color    ;; color of the trail left behind by an walker as it moves upwards
  down-trail-color  ;; color of the trail left behind by an walker as it moves downwards

  ;; plotting variables
  walker-to-plot         ;; the walker to plot if plot-all-walkers? is false
]

breed [ footprints footprint ]
breed [ walkers walker ]

walkers-own
[
  user-id           ;; the name of the client corresponding to this turtle
  my-xcor           ;; the unwrapped horizontal position of a turtle
  xcor-initial      ;; the initial horizontal position of a turtle
  interval          ;; the current interval a turtle is moving through
  velocity          ;; current velocity of an walker
  velocities        ;; list of velocities one for each interval
  base-shape        ;; either person-forward- or person-backward- so when we are animating
                    ;; we just tack on the frame number to form the name of the appropriate shape
  shape-counter     ;; keep track of where in the animation we are so it looks smooth
]

footprints-own
[
  base-color  ;; keep track of the color so we know when it has faded enough to entirely disappear
]

patches-own
[
  base-pcolor       ;; the original color of the patch before any trail was drawn on it
]

;;;;;;;;;;;;;;;;;;;;;;
;; Setup Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;

to startup
  hubnet-reset
  setup-vars
  setup-patches
  set-default-shape walkers "person-forward-1"
  set-default-shape footprints "footprint"
  setup
end

to setup
  reset-ticks
  cd
  setup-patches
  ask walkers
  [
    pu
    set-position xcor-initial
  ]
  ask footprints [ die ]
  my-setup-plots
end

to reset-clock
  reset-ticks
  cd
  setup-patches
  ask walkers
  [ set-position xcor
    pu ]
  ask footprints [ die ]
  my-setup-plots
end

to set-random-positions
  reset-ticks
  setup-patches
  ask walkers [ set-position random-pxcor ]
  ask footprints [ die ]
  my-setup-plots
end

to set-uniform-positions
  reset-ticks
  setup-patches
  ask walkers [ set-position walker-position ]
  ask footprints [ die ]
  my-setup-plots
end

to set-position [x]
  set heading 90
  set xcor x
  set interval 0
  set my-xcor xcor
  st
  show-or-hide-id-labels
end

;; set up the patches to have the floor numbers down the left side of the
;; view and color all even floor a gray color
to setup-patches
  clear-output
  cp
  ;; give the floors some color to be able to distinguish one floor from another
  ask patches with [ pxcor mod 2 = 0 ]
  [ set pcolor gray - 2 ]
  ask patches with [ pxcor = 0 ]
  [ set pcolor gray - 1 ]
  ;; label each row of pycors with a floor number
  ask patches with [ pycor = min-pycor ]
  [ set plabel pxcor ]
  ask patches
  [ set base-pcolor pcolor ]
end

;; set variables to initial values
to setup-vars
  reset-ticks
  set num-intervals 9
  set up-trail-color green
  set down-trail-color red

  ;; by default have the walker to plot be nobody
  set walker-to-plot "everybody"
end

;;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;;

;; move the walkers one interval
to go
  if ticks >= num-intervals
  [ stop ]
  ;; see if there are any messages waiting before
  ;; we start moving
  listen-clients
  ;; we want to move to a new interval after delay seconds
  ;; where delay is a function of simulation speed
  every delay
  [
    if ticks < num-intervals
    [
        ask walkers
        [
          show-or-hide-id-labels  ;; keep labels in sync with the switches
          assign-values           ;; set the walkers' velocity to the appropriate value
          move-walkers            ;; move the walkers by their velocity for this interval
        ]
        do-plotting
        ;; depending on the visualizations used
        ;; we may have to do some fading at the end of each move.
        if trails?
        [
          ask patches with [ pcolor != base-pcolor ]
            [ fade-trail ]
        ]
        if footprints?
        [
          ask footprints
          [
            set color color - 0.4
            if color < base-color - 5
            [ die ]
          ]
        ]
      tick
    ]
  ]
end

;; calculate the delay in seconds based on the simulation speed
to-report delay
  ifelse simulation-speed <= 0
  [ report ln (10 / 0.001) ]
  [ report ln (10 / simulation-speed) ]
end

to show-or-hide-id-labels  ;; turtle procedure
  ifelse show-user-id?
  [ set label word user-id "     " ]
  [ set label "" ]
end

;; set the student selected walker velocity for the current interval
to assign-values  ;; turtle procedure
  ifelse interval >= length velocities
  [ set velocity 0 ]
  [ set velocity item interval velocities ]
end

to move-walkers  ;; turtle procedure
  let delta-pos 0
  let speed abs velocity

  if velocity != 0
  [ set delta-pos (velocity / speed ) ]
  ;; each step is divided into smaller steps
  ;; so we get the appearance of motion, and differing
  ;; velocities
  let inner-tick 0

  ;; depending on whether the person is moving
  ;; forward or back change the shape to reflect the movement
  ifelse velocity >= 0
  [ set base-shape "person-forward-"]
  [ set base-shape "person-backward-" ]

  if not animation?
  [ set shape word base-shape "1" ]

  ;; reduce the "frames" per step so we get smoother movement
  ;; the distance and relative speed they move is the
  ;; same only there are more intermediate positions
  ;; and shapes displayed.
  set delta-pos delta-pos / 4
  set speed speed * 4

  while [ inner-tick < speed ]
  [
    ;; divide the amount of time till the next interval into equal amounts
    ;; so as to be able to make the motion of an walker smooth
    every ( delay / speed )
    [
      if trails?
      [
        ;; change the patch color as we move
        ifelse velocity > 0
        [ set pcolor up-trail-color ]
        [ set pcolor down-trail-color ]
      ]
      ;; set the true x-coord to the new position, even if it goes outside the world
      set my-xcor (my-xcor + delta-pos)
      ;; if the new position is outside the world
      ;; hide the turtle, it's out of the view
      ;; otherwise show the turtle and move there.
      ifelse patch-at (my-xcor - xcor) 0 != nobody
      [ st
        set xcor my-xcor ]
      [ ht ]
      set inner-tick inner-tick + 1
      if animation?
      [
        ;; increment the shape to the next shape in the
        ;; series to create the illusion of motion.
        set shape (word base-shape shape-counter)
        set shape-counter ((shape-counter + 1) mod 9) + 1
      ]
      display
    ]
  ]

  if footprints?
  [
    ;; make a shape where we landed.
    ;; we can't use stamp shape because we want them to fade.
    hatch-footprints 1
    [
      set base-color color
      set label ""
      set color color - 0.4
      ;; make sure the footprint faces in the
      ;; direction the walker is headed
      ifelse [velocity] of myself >= 0
      [ set heading 90 ]
      [ set heading 270 ]
     ]
   ]

  set interval interval + 1
end

;; have any trails fade back to the base color of the patch
to fade-trail  ;; patch procedure
  set pcolor pcolor - 0.4
  if (pcolor mod 10 = 0)
  [ set pcolor base-pcolor ]
end

;;;;;;;;;;;;;;;;;;;;;;;
;; HubNet Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;

to listen-clients
  while [ hubnet-message-waiting? ]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ setup-walker display ]
    [
      ifelse hubnet-exit-message?
      [ remove-walker display ]
      [ ask walkers with [ user-id = hubnet-message-source ]
        [ execute-cmd hubnet-message-tag ] ]
    ]
  ]
end

to execute-cmd [ cmd ]
  ifelse cmd = "interval-1"
  [ set-velocity 0 ][
  ifelse cmd = "interval-2"
  [ set-velocity 1 ][
  ifelse cmd = "interval-3"
  [ set-velocity 2 ][
  ifelse cmd = "interval-4"
  [ set-velocity 3 ][
  ifelse cmd = "interval-5"
  [ set-velocity 4 ][
  ifelse cmd = "interval-6"
  [ set-velocity 5 ][
  ifelse cmd = "interval-7"
  [ set-velocity 6 ][
  ifelse cmd = "interval-8"
  [ set-velocity 7 ]
  [ if cmd = "interval-9" [ set-velocity 8 ] ]
  ] ] ] ] ] ] ]
end

to set-velocity [index]
  set velocities replace-item index velocities hubnet-message
end

to setup-walker
  let p one-of patches with [ pxcor = 0 and pycor > (min-pycor + 1) and not any? walkers-on patches with [ pycor = [pycor] of myself ] ]
  ifelse p = nobody
  [
    user-message "A user tried to join but there is no more space for another user."
  ]
  [
    create-walkers 1
    [
      set user-id hubnet-message-source
      set velocities [0 0 0 0 0 0 0 0 0]
      set heading 0
      set interval 0
      set color 5 + 10 * random 14
      set xcor-initial random-pxcor
      setxy xcor-initial [pycor] of p
      set my-xcor xcor
      set label-color yellow
      show-or-hide-id-labels
      set shape-counter 1
      set base-shape "person-forward-"
    ]
    my-setup-plots
  ]
end

to remove-walker
  ask walkers with [ user-id = hubnet-message-source ] [ die ]
  my-setup-plots
end

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Plotting Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

;; plot the positions and velocities for the walkers in the appropriate plot
to do-plotting
  ;; walker-to-plot is a string
  ;; assume we are plotting everyone
  let guys-to-plot walkers

  ;; if we're not get the agentset that includes the agents
  ;; we're plotting, right now it can only be one but the code
  ;; is simpler this way.
  if walker-to-plot != "everybody"
  [ set guys-to-plot walkers with [ user-id = walker-to-plot ] ]

  ask guys-to-plot
  [
    set-current-plot "Position vs. Intervals"
    set-current-plot-pen user-id
    plot my-xcor

    set-current-plot "Velocity vs. Intervals"
    set-current-plot-pen user-id
    plot velocity
  ]

  plot-x-axis "Velocity vs. Intervals"
  plot-x-axis "Position vs. Intervals"
end

;; plots a black line at the x-axis of the plot this-plot
to plot-x-axis [ this-plot ]
  set-current-plot this-plot
  set-current-plot-pen "x-axis"
  plotxy plot-x-min 0
  plotxy plot-x-max 0
end

to pick-walker-to-plot
  set walker-to-plot user-one-of
                    "Please select the walker to plot"
                     (fput "everybody" sort [user-id] of walkers )
end

;; setup the position and velocity plot
to my-setup-plots
  clear-all-plots

  ask walkers
  [
    set-current-plot "Position vs. Intervals"
    setup-pens false

    set-current-plot "Velocity vs. Intervals"
    setup-pens true

    ;; make sure to plot the initial position
    set-current-plot "Position vs. Intervals"
    set-current-plot-pen user-id
    plot my-xcor
  ]

  plot-x-axis "Velocity vs. Intervals"
  plot-x-axis "Position vs. Intervals"
end

;; create pens for each of the existing walkers and color the pens to be the same color as
;; their corresponding walker.  if bars? is true, set the pen mode to be 1 for bar mode.
to setup-pens [ bars? ]
  create-temporary-plot-pen user-id
  if bars?
  [ set-plot-pen-mode 1 ]
  set-plot-pen-color color
end
@#$#@#$#@
GRAPHICS-WINDOW
226
19
736
630
12
14
20.0
1
11
1
1
1
0
0
0
1
-12
12
-14
14
1
1
1
ticks
30.0

BUTTON
71
57
153
90
login
listen-clients
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
18
93
112
126
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

SLIDER
22
522
193
555
simulation-speed
simulation-speed
0
10
5
0.1
1
NIL
HORIZONTAL

PLOT
757
27
1031
256
Position vs. Intervals
Intervals
Position
0.0
9.0
-12.0
12.0
false
false
"" ""
PENS
"x-axis" 1.0 0 -16777216 false "" ""

PLOT
756
261
1030
490
Velocity vs. Intervals
Intervals
Velocity
0.0
9.0
-6.0
6.0
false
false
"" ""
PENS
"x-axis" 1.0 0 -16777216 false "" ""

BUTTON
805
551
974
584
NIL
pick-walker-to-plot
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
22
374
193
407
show-user-id?
show-user-id?
0
1
-1000

MONITOR
832
497
952
542
NIL
walker-to-plot
3
1
11

BUTTON
25
134
197
167
NIL
set-random-positions
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
25
204
197
237
walker-position
walker-position
-12
12
0
1
1
NIL
HORIZONTAL

BUTTON
25
169
197
202
NIL
set-uniform-positions
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
45
416
170
449
footprints?
footprints?
1
1
-1000

SWITCH
45
450
170
483
animation?
animation?
0
1
-1000

SWITCH
45
484
170
517
trails?
trails?
1
1
-1000

BUTTON
24
267
103
300
step
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

BUTTON
117
267
196
300
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

TEXTBOX
21
347
171
365
display options
11
0.0
0

BUTTON
114
93
208
126
NIL
reset-clock
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

Each student defines the motion of a walking character by setting its velocity on their client over time intervals.  The students have 9 different intervals for which they can set the velocity.  They can then send these velocities to their characters, where they see the character walking its route over the 9 intervals.  This is designed to help students understand the accumulation of distance as a function of time. This can serve as a jumping off point for advanced concepts ranging from derivatives and integrals to wave mechanics.

For further documentation, see the Participatory Simulations Guide found at http://ccl.northwestern.edu/ps/

## HOW IT WORKS

Each student has control of nine sliders, one for each interval over a period of time, the sliders determine the velocity of the character, walker, assigned to the student. Once instructions have been given and students have made selected their velocities for each time interval you can run the simulation.  The walkers will move across the view at the indicated rates possibly leaving behind a trail to make it easier to see the change in position over time.

There are two possible clients for this activity, one with a view and one without.  Walking includes the view, Walking Alternate does not.

## HOW TO USE IT

To start the activity press the SETUP button and then press LOGIN.  Have the students login and identify their location in the view. Students can then change the velocities of their character by moving the interval sliders on the client.  To run the entire set of nine intervals press the GO button, to move forward only one interval at a time press the STEP button.  You may leave the LOGIN button pressed at all times if you wish.

To reset the clock and reset the walkers to their initial positions, that is a position that was randomly selected at login, press the SETUP button.  If you just want to reset the clocks and allow the walkers to continue from their current positions press the RESET-CLOCK button. To reset walkers to a new random position press the SET-RANDOM-POSITIONS button. To set the position of all walkers to the same place select the location by setting the WALKER-POSITION slider and press the SET-UNIFORM-POSITIONS button.

By default the velocities and positions for all walkers are plotted in the POSITION V INTERVAL and VELOCITY V INTERVAL plots.  If you want to plot only one walker press the PICK-WALKER-TO-PLOT button and select a walker from the list, or select "everybody" to plot all walkers again.

There are a few settings that will help you and the participants to visualize the simulation. SIMULATION-SPEED adjusts how quickly to move through the intervals during GO. When SHOW-USER-ID? is true, all the turtles show their user-id as its label otherwise no label is shown. When FOOTPRINTS? is true the walkers leave a trail of footprints behind them, the footprints fade over time so you can track how long ago you were at each position. When TRAILS? is true the walkers change the pcolor along their path as they move indicating whether they are moving in a positive or negative direction. When ANIMATION? is true the walkers are animated like a flipbook as they move across the world.

## THINGS TO NOTICE

Notice if a walker moves more than 1 position during an interval, it equally divides the amount of time spent traveling through each position.

## THINGS TO TRY

Have your walker sit at x = 3 during the fourth time segment.  What is the same  and different between the solutions? Encourage students to come up with unique solutions.

Have students coordinate to make a traveling or a standing wave.

Have each student set their walker to have a velocity of +2 during the fourth time interval.

Have each student set their velocities so that the walker starts and ends in the same position.

## EXTENDING THE MODEL

A real person may not move at a constant speed.  He or she may build speed gradually until they have reached a constant speed.  Then before reaching their destination, they will slow down.  Try to make this happen as a part of the model.

## NETLOGO FEATURES

This activity uses incremental turtle shapes in order to simulate an animation.

## RELATED MODELS

Function

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

elevator
false
0
Rectangle -1 true false 45 14 255 286
Circle -7500403 true true 115 16 69
Rectangle -7500403 true true 139 82 159 101
Rectangle -7500403 true true 109 92 192 206
Polygon -7500403 true true 191 109 248 127 234 164 190 128
Polygon -7500403 true true 109 109 53 131 62 162 109 126
Rectangle -7500403 true true 118 205 146 278
Rectangle -7500403 true true 157 205 181 277
Rectangle -7500403 true true 180 264 203 277
Rectangle -7500403 true true 92 265 118 278

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

footprint
true
0
Polygon -7500403 true true 111 244 115 272 130 286 151 288 168 277 176 257 177 234 175 195 174 172 170 135 177 104 188 79 188 55 179 45 181 32 185 17 176 1 159 2 154 17 161 32 158 44 146 47 144 35 145 21 135 7 124 9 120 23 129 36 133 49 121 47 100 56 89 73 73 94 74 121 86 140 99 163 110 191
Polygon -7500403 true true 97 37 101 44 111 43 118 35 111 23 100 20 95 25
Polygon -7500403 true true 77 52 81 59 91 58 96 50 88 39 82 37 76 42
Polygon -7500403 true true 63 72 67 79 77 78 79 70 73 63 68 60 63 65

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

person-backward-1
false
0
Polygon -7500403 true true 180 195 180 105 165 90 105 90 105 105 105 120 120 180
Circle -7500403 true true 95 5 80
Rectangle -7500403 true true 117 73 153 90
Polygon -7500403 true true 135 195 180 180 204 228 240 255 225 300 167 242
Polygon -7500403 true true 120 180 180 180 144 232 141 299 101 299 105 223
Polygon -7500403 true true 165 90 205 125 240 180 210 195 173 137 150 120
Polygon -7500403 true true 120 136 120 151 94 202 66 190 90 136 105 91

person-backward-2
false
0
Polygon -7500403 true true 120 136 135 166 108 203 80 191 98 147 109 98
Circle -7500403 true true 95 5 80
Rectangle -7500403 true true 117 73 153 90
Polygon -7500403 true true 132 176 183 160 195 225 225 255 210 300 165 255
Polygon -7500403 true true 126 164 180 180 150 225 150 300 105 300 105 225
Polygon -7500403 true true 195 180 180 105 165 90 113 89 105 105 105 120 120 180
Polygon -7500403 true true 169 91 204 135 225 180 180 195 165 150 150 120

person-backward-3
false
0
Polygon -7500403 true true 129 123 119 162 135 200 104 201 102 150 106 104
Polygon -7500403 true true 135 180 180 180 180 225 210 285 165 300 135 225
Polygon -7500403 true true 120 180 179 180 142 239 165 300 120 300 120 225
Circle -7500403 true true 95 5 80
Rectangle -7500403 true true 117 73 153 90
Polygon -7500403 true true 184 192 180 105 165 90 120 90 105 105 105 120 120 195
Polygon -7500403 true true 165 90 180 105 207 193 172 199 165 150 150 120

person-backward-4
false
0
Polygon -7500403 true true 120 135 120 165 125 205 114 203 110 135 117 89
Circle -7500403 true true 95 5 80
Rectangle -7500403 true true 117 73 153 90
Polygon -7500403 true true 135 180 180 180 180 225 180 300 135 300 135 225
Polygon -7500403 true true 119 182 149 182 164 227 164 302 119 302 119 212
Polygon -7500403 true true 180 190 180 105 170 90 120 90 105 105 105 120 120 195
Polygon -7500403 true true 155 90 170 102 180 135 180 165 180 210 150 210

person-backward-5
false
0
Circle -7500403 true true 95 5 80
Rectangle -7500403 true true 117 73 153 90
Polygon -7500403 true true 120 180 165 180 165 225 180 300 120 300 120 225
Polygon -7500403 true true 105 180 180 180 165 225 150 300 105 300 99 222
Polygon -7500403 true true 180 195 180 105 165 90 120 90 105 105 105 120 120 195
Polygon -7500403 true true 120 135 120 165 120 195 105 195 105 120 117 89
Polygon -7500403 true true 164 128 164 158 164 203 134 203 135 120 158 95

person-backward-6
false
0
Polygon -7500403 true true 135 180 180 180 178 222 195 300 150 300 135 225
Polygon -7500403 true true 118 174 180 178 135 240 127 298 86 294 90 225
Circle -7500403 true true 95 5 80
Rectangle -7500403 true true 117 73 153 90
Polygon -7500403 true true 184 192 180 105 165 90 120 90 105 105 105 120 120 195
Polygon -7500403 true true 165 90 195 120 207 193 180 195 180 135 150 120
Polygon -7500403 true true 150 124 150 154 139 201 105 199 107 133 117 93

person-backward-7
false
0
Circle -7500403 true true 95 5 80
Rectangle -7500403 true true 117 73 153 90
Polygon -7500403 true true 135 180 180 180 195 240 210 300 165 300 150 240
Polygon -7500403 true true 135 166 180 181 123 229 120 297 79 289 87 210
Polygon -7500403 true true 180 195 180 105 165 90 120 90 105 105 105 120 111 193
Polygon -7500403 true true 178 103 200 133 221 195 189 202 176 165 146 120
Polygon -7500403 true true 120 133 135 163 120 210 90 195 102 146 105 105

person-backward-8
false
0
Polygon -7500403 true true 117 168 162 183 122 239 97 300 60 280 87 226
Circle -7500403 true true 95 5 80
Rectangle -7500403 true true 117 73 153 90
Polygon -7500403 true true 180 180 180 105 165 90 120 90 105 105 105 120 105 180
Polygon -7500403 true true 165 90 201 127 234 183 208 197 182 158 150 120
Polygon -7500403 true true 120 133 135 163 90 195 68 179 97 141 117 87
Polygon -7500403 true true 135 180 180 180 195 240 225 300 180 300 150 240

person-backward-9
false
0
Circle -7500403 true true 95 5 80
Rectangle -7500403 true true 117 73 153 90
Polygon -7500403 true true 135 180 180 180 195 225 232 289 195 300 150 225
Polygon -7500403 true true 112 185 165 180 137 240 129 300 83 301 100 229
Polygon -7500403 true true 179 180 179 105 164 90 119 90 105 97 104 120 113 185
Polygon -7500403 true true 165 90 210 135 240 180 210 195 180 150 150 120
Polygon -7500403 true true 115 137 115 154 78 199 54 179 85 139 115 92

person-forward-1
false
0
Polygon -7500403 true true 120 195 120 105 135 90 195 90 195 105 195 120 180 180
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 165 195 120 180 96 228 60 255 75 300 133 242
Polygon -7500403 true true 180 180 120 180 156 232 159 299 199 299 195 223
Polygon -7500403 true true 135 90 95 125 60 180 90 195 127 137 150 120
Polygon -7500403 true true 180 136 180 151 206 202 234 190 210 136 195 91

person-forward-2
false
0
Polygon -7500403 true true 180 136 165 166 192 203 220 191 202 147 191 98
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 168 176 117 160 105 225 75 255 90 300 135 255
Polygon -7500403 true true 174 164 120 180 150 225 150 300 195 300 195 225
Polygon -7500403 true true 105 180 120 105 135 90 187 89 195 105 195 120 180 180
Polygon -7500403 true true 131 91 96 135 75 180 120 195 135 150 150 120

person-forward-3
false
0
Polygon -7500403 true true 171 123 181 162 165 200 196 201 198 150 194 104
Polygon -7500403 true true 165 180 120 180 120 225 90 285 135 300 165 225
Polygon -7500403 true true 180 180 121 180 158 239 135 300 180 300 180 225
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 116 192 120 105 135 90 180 90 195 105 195 120 180 195
Polygon -7500403 true true 135 90 120 105 93 193 128 199 135 150 150 120

person-forward-4
false
0
Polygon -7500403 true true 180 135 180 165 175 205 186 203 190 135 183 89
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 165 180 120 180 120 225 120 300 165 300 165 225
Polygon -7500403 true true 181 182 151 182 136 227 136 302 181 302 181 212
Polygon -7500403 true true 120 190 120 105 130 90 180 90 195 105 195 120 180 195
Polygon -7500403 true true 145 90 130 102 120 135 120 165 120 210 150 210

person-forward-5
false
0
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 180 180 135 180 135 225 120 300 180 300 180 225
Polygon -7500403 true true 195 180 120 180 135 225 150 300 195 300 201 222
Polygon -7500403 true true 120 195 120 105 135 90 180 90 195 105 195 120 180 195
Polygon -7500403 true true 180 135 180 165 180 195 195 195 195 120 183 89
Polygon -7500403 true true 136 128 136 158 136 203 166 203 165 120 142 95

person-forward-6
false
0
Polygon -7500403 true true 165 180 120 180 122 222 105 300 150 300 165 225
Polygon -7500403 true true 182 174 120 178 165 240 173 298 214 294 210 225
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 116 192 120 105 135 90 180 90 195 105 195 120 180 195
Polygon -7500403 true true 135 90 105 120 93 193 120 195 120 135 150 120
Polygon -7500403 true true 150 124 150 154 161 201 195 199 193 133 183 93

person-forward-7
false
0
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 165 180 120 180 105 240 90 300 135 300 150 240
Polygon -7500403 true true 165 166 120 181 177 229 180 297 221 289 213 210
Polygon -7500403 true true 120 195 120 105 135 90 180 90 195 105 195 120 189 193
Polygon -7500403 true true 122 103 100 133 79 195 111 202 124 165 154 120
Polygon -7500403 true true 180 133 165 163 180 210 210 195 198 146 195 105

person-forward-8
false
0
Polygon -7500403 true true 183 168 138 183 178 239 203 300 240 280 213 226
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 120 180 120 105 135 90 180 90 195 105 195 120 195 180
Polygon -7500403 true true 135 90 99 127 66 183 92 197 118 158 150 120
Polygon -7500403 true true 180 133 165 163 210 195 232 179 203 141 183 87
Polygon -7500403 true true 165 180 120 180 105 240 75 300 120 300 150 240

person-forward-9
false
0
Circle -7500403 true true 125 5 80
Rectangle -7500403 true true 147 73 183 90
Polygon -7500403 true true 165 180 120 180 105 225 68 289 105 300 150 225
Polygon -7500403 true true 188 185 135 180 163 240 171 300 217 301 200 229
Polygon -7500403 true true 121 180 121 105 136 90 181 90 195 97 196 120 187 185
Polygon -7500403 true true 135 90 90 135 60 180 90 195 120 150 150 120
Polygon -7500403 true true 185 137 185 154 222 199 246 179 215 139 185 92

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
NetLogo 5.0beta5
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
VIEW
233
23
683
545
0
0
0
1
1
1
1
1
0
1
1
1
-12
12
-14
14

SLIDER
35
120
207
153
interval-1
interval-1
-6
6
0
1
1
NIL
HORIZONTAL

SLIDER
35
153
207
186
interval-2
interval-2
-6
6
0
1
1
NIL
HORIZONTAL

SLIDER
35
186
207
219
interval-3
interval-3
-6
6
0
1
1
NIL
HORIZONTAL

SLIDER
35
218
207
251
interval-4
interval-4
-6
6
0
1
1
NIL
HORIZONTAL

SLIDER
35
251
207
284
interval-5
interval-5
-6
6
0
1
1
NIL
HORIZONTAL

SLIDER
35
284
207
317
interval-6
interval-6
-6
6
0
1
1
NIL
HORIZONTAL

SLIDER
35
317
207
350
interval-7
interval-7
-6
6
0
1
1
NIL
HORIZONTAL

SLIDER
35
350
207
383
interval-8
interval-8
-6
6
0
1
1
NIL
HORIZONTAL

SLIDER
35
383
207
416
interval-9
interval-9
-6
6
0
1
1
NIL
HORIZONTAL

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

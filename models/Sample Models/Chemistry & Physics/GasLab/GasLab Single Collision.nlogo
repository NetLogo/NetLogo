globals
[
  tick-delta                      ;; how much we advance the tick counter this time through
  max-tick-delta                  ;; the largest tick-delta is allowed to be
  plot-clock                      ;; keeps track of the x-axis for the plot
  avg-speed                       ;; average speed of the two particles
  total-energy                    ;; total energy of the two particles
  x-center y-center               ;; coordinates of center of mass
  done?                           ;; becomes true when one particles is about to 'leave' the world
  after-collision?                ;; for graphing purposes
]

breed [ particles particle ]
breed [ centers-of-mass center-of-mass ]

particles-own
[
  speed mass energy                ;; particle variables
  last-collision
]

to setup
  clear-all
  set-default-shape particles "circle"
  set-default-shape centers-of-mass "x"
  set done? false
  set max-tick-delta 0.1073
  set after-collision? false
  make-particles
  create-centers-of-mass 1
    [ set size 3 ]
  update-variables
  clear-drawing  ;; erase the line made by initially moving the center of mass
  reset-ticks
end

to update-variables
  let total-mass sum [mass] of particles
  set x-center (sum [ xcor * mass ] of particles) / total-mass
  set y-center (sum [ ycor * mass ] of particles) / total-mass
  set avg-speed  mean [speed] of particles
  set total-energy sum [energy] of particles
  ask centers-of-mass
  [
    ifelse show-center-of-mass?                         ;; marks a gray path along the particles' center of mass
      [ show-turtle
        pen-down ]
      [ hide-turtle
        pen-up ]
    setxy x-center y-center
  ]
end

to go
  ask particles [ move ]

  ask particles                                   ;;  each particle checks if it's on the same patch as the other
  [ check-for-collision ]
  update-variables
  calculate-tick-delta
  tick-advance tick-delta
  display
  update-plots

end


to go-mode

if run-mode = "one-collision"  [go-once stop]
if run-mode = "all-collision-angles" [all-collision-angles]
if run-mode = "all-reflection-angles" [all-reflection-angles]

end


to go-once                                          ;; a single collision
  setup
  while [ not done? ]
  [ go
    ask particles
    [ if not can-move? 1
      [ set done? true ]
    ]
  ]
end

to all-collision-angles                            ;; activated when the reflection angle is constant and the collision angle is varied
  go-once
  ifelse collision-angle >= 345
    [ set collision-angle 15 ]
    [ set collision-angle collision-angle + 15 ]
end

to all-reflection-angles                           ;; activated when the collision angle is constant and the reflection angle is varied
  go-once
  set reflection-angle reflection-angle + 15
  if reflection-angle = 360
  [ set reflection-angle 0 ]
end

to calculate-tick-delta
  ;; tick-delta is calculated in such way that even the fastest
  ;; particle will jump at most 1 patch length in a tick. As
  ;; particles jump (speed * tick-delta) at every tick, making
  ;; tick length the inverse of the speed of the fastest particle
  ;; (1/max speed) assures that. Having each particle advance at most
  ;; one patch-length is necessary for them not to "jump over" each
  ;; other without colliding.
  ifelse any? particles with [speed > 0]
    [ set tick-delta min list (1 / (ceiling max [speed] of particles)) max-tick-delta ]
    [ set tick-delta max-tick-delta ]
end


to move  ;; particle procedure
  jump (speed * tick-delta)
end

to check-for-collision  ;; particle procedure
  if count other particles-here = 1
  [
    ;; the following conditions are imposed on collision candidates:
    ;;   1. they must have a lower who number than my own, because collision
    ;;      code is asymmetrical: it must always happen from the point of view
    ;;      of just one particle.
    ;;   2. they must not be the same particle that we last collided with on
    ;;      this patch, so that we have a chance to leave the patch after we've
    ;;      collided with someone.
    let candidate one-of other particles-here with
      [who < [who] of myself and myself != last-collision]
    ;; we also only collide if one of us has non-zero speed. It's useless
    ;; (and incorrect, actually) for two particles with zero speed to collide.
    if (candidate != nobody) and (speed > 0 or [speed] of candidate > 0)
    [
      collide-with candidate
      set last-collision candidate
      ask candidate [ set last-collision myself ]
      set after-collision? true
    ]
  ]
end

;; implements a collision with another particle.
;;
;; THIS IS THE HEART OF THE PARTICLE SIMULATION, AND YOU ARE STRONGLY ADVISED
;; NOT TO CHANGE IT UNLESS YOU REALLY UNDERSTAND WHAT YOU'RE DOING!
;;
;; The two particles colliding are self and other-particle, and while the
;; collision is performed from the point of view of self, both particles are
;; modified to reflect its effects. This is somewhat complicated, so I'll
;; give a general outline here:
;;   1. Do initial setup, and determine the heading between the reflected particles
;;      (call it theta).
;;   2. Convert the representation of the velocity of each particle from
;;      speed/heading to a theta-based vector whose first component is the
;;      particle's speed along theta, and whose second component is the speed
;;      perpendicular to theta.
;;   3. Modify the velocity vectors to reflect the effects of the collision.
;;      This involves:
;;        a. computing the velocity of the center of mass of the whole system
;;           along direction theta
;;        b. updating the along-theta components of the two velocity vectors.
;;   4. Convert from the theta-based vector representation of velocity back to
;;      the usual speed/heading representation for each particle.
;;   5. Perform final cleanup and update derived quantities.
to collide-with [ other-particle ] ;; particle procedure
  ;;; PHASE 1: initial setup

  ;; for convenience, grab some quantities from other-particle
  let mass2 [mass] of other-particle
  let speed2 [speed] of other-particle
  let heading2 [heading] of other-particle

  ;; since particles are modeled as zero-size points, theta isn't meaningfully
  ;; defined. we can assign it randomly without affecting the model's outcome.
  let theta reflection-angle

  ;;; PHASE 2: convert velocities to theta-based vector representation

  ;; now convert my velocity from speed/heading representation to components
  ;; along theta and perpendicular to theta
  let v1t (speed * cos (theta - heading))
  let v1l (speed * sin (theta - heading))

  ;; do the same for other-particle
  let v2t (speed2 * cos (theta - heading2))
  let v2l (speed2 * sin (theta - heading2))



  ;;; PHASE 3: manipulate vectors to implement collision

  ;; compute the velocity of the system's center of mass along theta
  let vcm (((mass * v1t) + (mass2 * v2t)) / (mass + mass2) )

  ;; now compute the new velocity for each particle along direction theta.
  ;; velocity perpendicular to theta is unaffected by a collision along theta,
  ;; so the next two lines actually implement the collision itself, in the
  ;; sense that the effects of the collision are exactly the following changes
  ;; in particle velocity.
  set v1t (2 * vcm - v1t)
  set v2t (2 * vcm - v2t)



  ;;; PHASE 4: convert back to normal speed/heading

  ;; now convert my velocity vector into my new speed and heading
  set speed sqrt ((v1t ^ 2) + (v1l ^ 2))
  set energy (0.5 * mass * speed ^ 2)
  ;; if the magnitude of the velocity vector is 0, atan is undefined. but
  ;; speed will be 0, so heading is irrelevant anyway. therefore, in that
  ;; case we'll just leave it unmodified.
  if v1l != 0 or v1t != 0
    [ set heading (theta - (atan v1l v1t)) ]

  ;; and do the same for other-particle
  ask other-particle [
    set speed sqrt ((v2t ^ 2) + (v2l ^ 2))
    set energy (0.5 * mass * (speed ^ 2))
    if v2l != 0 or v2t != 0
      [ set heading (theta - (atan v2l v2t)) ]
  ]
end

to recolor  ;; particle procedure
  ifelse speed < (0.5 * 10)
  [
    set color blue + 2
  ]
  [
    ifelse speed > (1.5 * 10)
      [ set color red ]
      [ set color green ]
  ]
end

;; creates initial particles
to make-particles
  create-particles 1 [
    set color pink
    set speed init-pink-speed
    set mass pink-mass
    set heading 180
    bk 2 * speed
  ]
  create-particles 1 [
    set color blue
    set speed init-blue-speed
    set mass blue-mass
    set heading 180 + collision-angle
    bk 2 * speed
  ]
  ask particles
  [
    setup-particle
  ]
  calculate-tick-delta
end


to setup-particle  ;; particle procedure
  pen-down
  set size 2
  set energy (0.5 * mass * speed ^ 2 )
  set last-collision nobody
end
@#$#@#$#@
GRAPHICS-WINDOW
399
10
772
404
60
60
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
-60
60
-60
60
1
1
1
ticks
30.0

MONITOR
85
323
198
368
average speed
avg-speed
1
1
11

MONITOR
200
323
313
368
total energy
total-energy
1
1
11

SLIDER
16
75
382
108
collision-angle
collision-angle
15
345
150
15
1
degrees
HORIZONTAL

SLIDER
15
151
195
184
init-pink-speed
init-pink-speed
1
20
15
1
1
NIL
HORIZONTAL

SLIDER
15
186
194
219
pink-mass
pink-mass
1
20
1
1
1
NIL
HORIZONTAL

SLIDER
16
112
382
145
reflection-angle
reflection-angle
0
345
225
15
1
degrees
HORIZONTAL

SLIDER
205
152
384
185
init-blue-speed
init-blue-speed
1
20
6
1
1
NIL
HORIZONTAL

SLIDER
205
187
384
220
blue-mass
blue-mass
1
20
4
1
1
NIL
HORIZONTAL

BUTTON
14
17
99
61
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

MONITOR
38
271
161
316
speed of pink
[speed] of turtle 0
1
1
11

MONITOR
231
269
359
314
speed of blue
[speed] of turtle 1
1
1
11

MONITOR
39
221
162
266
energy of pink
[energy] of turtle 0
1
1
11

MONITOR
231
222
359
267
energy of blue
[energy] of turtle 1
1
1
11

SWITCH
399
410
582
443
show-center-of-mass?
show-center-of-mass?
1
1
-1000

PLOT
30
376
366
559
Speeds
time
speed
0.0
5.0
0.0
20.0
true
true
"" "ifelse after-collision?\n   [set plot-clock (ticks - (tick-delta))]\n   [set plot-clock ticks]\n"
PENS
"pink" 1.0 0 -2064490 true "" "if [speed] of turtle 0 != [speed] of turtle 1\n  [ plotxy plot-clock ([speed] of turtle 0) ]"
"blue" 1.0 0 -13345367 true "" "if [speed] of turtle 0 != [speed] of turtle 1\n  [ plotxy plot-clock ([speed] of turtle 1) ]\n"
"both" 1.0 0 -16777216 true "" "if [speed] of turtle 0 = [speed] of turtle 1\n  [ plotxy plot-clock ([speed] of turtle 0) ]"

BUTTON
299
18
381
63
go
go-mode
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
0

CHOOSER
105
17
292
62
run-mode
run-mode
"one-collision" "all-collision-angles" "all-reflection-angles"
0

@#$#@#$#@
## WHAT IS IT?

This is one in a series of GasLab models that use the same basic rules for what happens when particles run into each other. Each one has different features in order to show different aspects of the behavior of gases.

This model is simplified to show the collision of only two particles, since this event is so hard to watch when there are many particles in the world: given the initial motions of two colliding particles, what can we learn about their final motions from the principles of conservation of momentum and energy?

## HOW IT WORKS

The particles are modeled as hard balls with no internal energy except that which is due to their motion.  Collisions between particles are elastic.  Particles are colored according to speed --wel- blue for slow, green for medium, and red for high speeds.

Coloring of the particles is with respect to one speed (10).  Particles with a speed less than 5 are blue, ones that are more than 15 are red, while all in those in-between are green.

Particles behave according to the following rules:  
1. A particle moves in a straight line without changing its speed, unless it collides with another particle or bounces off the wall.  The particles are aimed to hit each other at the origin.  
2. Two particles "collide" if they find themselves on the same patch (the world is composed of a grid of small squares called patches).  
3. A random axis is chosen, as if they are two balls that hit each other and this axis is the line connecting their centers.  
4. They exchange momentum and energy along that axis, according to the conservation of momentum and energy.  This calculation is done in the center of mass system.  
5. Each turtle is assigned its new velocity, energy, and heading.  
6. If a turtle finds itself on or very close to a wall of the container, it "bounces" -- that is, reflects its direction and keeps its same speed.

## HOW TO USE IT

Initial settings:  
- COLLISION-ANGLE: Sets the angle that separates the pink and blue particles before the collision.  
- REFLECTION-ANGLE: Sets the angle of the axis connecting the particles' centers when they collide with respect to the vertical axis. To calculate the outcome of the collision, the speeds of the two particles are projected onto this new axis and the new speeds and headings are computed. Other GasLab models use random values for "REFLECTION-ANGLE", but this model allows you to experiment with them one by one. This angle is called THETA in the code of the model.  
- INIT-PINK-SPEED (or BLUE): Sets the initial speed of the pink (or blue) particle.  
- PINK-MASS (or BLUE): Sets the mass of the pink (or blue) particle.

Other settings:  
- SHOW-CENTER-OF-MASS?: If ON, the center of mass of the system will be shown in gray.

Buttons for running the model:  
- SETUP  
- RUN-MODE: Chooses between ONE COLLISION (just one run), ALL-COLLISION-ANGLES (loops through all the collision angles with 15-degrees steps) and ALL-REFLECTION-ANGLES(loops through all the reflection angles with 15-degrees steps).  
- GO

Monitors:  
- ENERGY OF PINK (or -BLUE): Shows the current energy of the pink (or blue) particle.  
- SPEED OF PINK (or -BLUE): Shows the current speed of the pink (or blue) particle.  
- AVERAGE SPEED: Shows the average of the speeds of the two particles.  
- TOTAL ENERGY: Shows the sum of the energies of the two particles.

Plots:  
- SPEEDS: speed of each of the particles over time.

## THINGS TO TRY

Set the reflection-angle to zero.  Draw a picture representing the two balls as they collide, with their two faces touching.  Make the line connecting their centers be the same as theta.  Draw vectors representing their motion.

While running the following situations note the paths of the two particles.  Can you make sense of what they do?  Is it what you expected?

Choose a COLLISION-ANGLE and a REFLECTION-ANGLE and choose ONE-COLLISION to see one particular collision.

Choose a COLLISION-ANGLE and choose ALL-REFLECTION-ANGLES to cycle through all of the angles of reflection.

Choose a REFLECTION-ANGLE and choose ALL-COLLISION-ANGLES to cycle through all of the angles of collision.

## THINGS TO NOTICE

With COLLISION-ANGLE = 180 (directly across from each other) and REFLECTION-ANGLE = 90, it looks as if the two particles miss each other.  What is happening?

With REFLECTION-ANGLE = 45 degrees, the particles go off at right angles. Why?  Draw a picture of what is happening at the moment of collision.

With REFLECTION-ANGLE = 0 degrees, the two particles reverse direction. Why?

What is the motion of the center of mass?  What would you expect it to be?

## THINGS TO TRY

Have the masses of the two particles be different.

Have the initial speeds of the two particles be different.

Change the initial positions and headings of the two particles.  As a simple case, set one on the y-axis and the other on the x-axis, (COLLISION-ANGLE = 90) each one heading toward the origin.  The center of mass is no longer stationary. Note its path.  Is it what you would expect?

If the center of mass is not stationary, the two particles often have different speeds after they collide, even when they have identical initial speeds and masses!  Why does this happen?  How can this satisfy the conservation of both energy and momentum?

The fact that the velocities are not always the same after every kind of collision is essential to getting a distribution of velocities among identical particles after many collisions, which is what we observe with particles in a gas.

Does this seem like a reasonable model for colliding particles? When is it reasonably valid, and when is it decidedly NOT valid?

When two particles collide, should theta be picked randomly -- each theta has an equal probability --- or in some other way?  Would this change the eventual velocity distribution among many particles?

After you have gotten used to observing and understanding these simple collisions, go to the "Free Gas" or "Gas in a Box" model.  Especially watch the particle whose path is traced in gray.  Does it make sense?  Can you picture each collision?

Record the velocities of each particle after each collision. After you have several sets of velocities, look at the entire velocity distribution. What do you notice? Is it the Maxwell-Boltzmann distribution?

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

clock
true
0
Circle -7500403 true true 30 30 240
Polygon -16777216 true false 150 31 128 75 143 75 143 150 158 150 158 75 173 75
Circle -16777216 true false 135 135 30

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
NetLogo 5.0RC2
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

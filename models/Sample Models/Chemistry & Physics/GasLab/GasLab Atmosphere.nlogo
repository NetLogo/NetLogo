globals
[
  tick-delta                          ;; how much we advance the tick counter this time through
  max-tick-delta                      ;; the largest tick-delta is allowed to be
  avg-speed-init avg-energy-init      ;; initial averages
  avg-speed avg-energy                ;; current averages
  fast medium slow lost-particles     ;; current counts
  percent-medium percent-slow         ;; percentage of current counts
  percent-fast percent-lost-particles ;; percentage of current counts
  tracer
]

breed [ particles particle ]
breed [ flashes flash ]

flashes-own [birthday]

particles-own
[
  speed mass energy          ;; particle info
  last-collision
]


to setup
  clear-all
  set-default-shape particles "circle"
  set-default-shape flashes "plane"
  set max-tick-delta 0.1073
  ;; make floor
  ask patches with [ pycor = min-pycor ]
    [ set pcolor yellow ]
  make-particles
  update-variables
  set avg-speed-init avg-speed
  set avg-energy-init avg-energy
  reset-ticks
  set tracer one-of turtles
end

to go
  ask particles [ bounce ]
  ask particles [ move ]
  if not any? particles [stop]  ;; particles can die when they float too high
  ask particles
  [ if collide? [check-for-collision] ]
  if tracer = nobody
  [ set tracer one-of turtles ]
  ifelse trace?
  [ ask tracer [ pen-down ] ]
  [ ask tracer [ pen-up ] ]
  tick-advance tick-delta
  if floor ticks > floor (ticks - tick-delta)
  [
    update-variables
    update-plots
  ]
  calculate-tick-delta

  ask flashes with [ticks - birthday > 0.4]
    [ die ]
  display
end

to update-variables
  set lost-particles (number-of-particles - count particles)
  set percent-lost-particles (lost-particles / number-of-particles) * 100
  set medium count particles with [color = green]
  set slow count particles with [color = blue]
  set fast count particles with [color = red]
  set percent-medium (medium / (number-of-particles - lost-particles)) * 100
  set percent-slow (slow / (number-of-particles - lost-particles)) * 100
  set percent-fast (fast / (number-of-particles - lost-particles)) * 100
  set avg-speed  mean [speed] of particles
  set avg-energy  mean [energy] of particles
end

to calculate-tick-delta
  ;; tick-delta is calculated in such way that even the fastest
  ;; particle will jump at most 1 patch length in a tick. As
  ;; particles jump (speed * tick-delta) at every tick, making
  ;; tick length the inverse of the speed of the fastest particle
  ;; (1/max speed) assures that. Having each particle advance at most
  ;; one patch-length is necessary for it not to "jump over" a wall
  ;; or another particle.
  ifelse any? particles with [speed > 0]
    [ set tick-delta min list (1 / (ceiling max [speed] of particles)) max-tick-delta ]
    [ set tick-delta max-tick-delta ]
end


to bounce  ;; particle procedure
  ;; get the coordinates of the patch we'll be on if we go forward 1
  let new-patch patch-ahead 1
  ;; if we're not about to hit a wall, we don't need to do any further checks
  if new-patch = nobody or [pcolor] of new-patch != yellow
    [ stop ]

  let new-px [pxcor] of new-patch
  let new-py [pycor] of new-patch
  ;; if hitting the bottom, reflect heading around y axis
  if (new-py = min-pycor )
    [ set heading (180 - heading)]

  ask patch new-px new-py
  [ sprout-flashes 1 [
      set color pcolor - 2
      set birthday ticks
      set heading 0
    ]
  ]
end

to move  ;; particle procedure
  ;; In other GasLab models, we use "jump speed * tick-delta" to move the
  ;; turtle the right distance along its current heading.  In this
  ;; model, though, the particles are affected by gravity as well, so we
  ;; need to offset the turtle vertically by an additional amount.  The
  ;; easiest way to do this is to use "setxy" instead of "jump".
  ;; Trigonometry tells us that "jump speed * tick-delta" is equivalent to:
  ;;   setxy (xcor + dx * speed * tick-delta)
  ;;         (ycor + dy * speed * tick-delta)
  ;; so to take gravity into account we just need to alter ycor
  ;; by an additional amount given by the classical physics equation:
  ;;   y(t) = 0.5*a*t^2 + v*t + y(t-1)
  ;; but taking tick-delta into account, since tick-delta is a multiplier of t.
  let xdiff dx * speed * tick-delta
  let ydiff dy * speed * tick-delta - gravity-acceleration * (0.5 * (tick-delta ^ 2))
  if patch-at xdiff ydiff = nobody [ die ]
  setxy (xcor + xdiff) (ycor + ydiff)
  factor-gravity
end


to factor-gravity  ;; turtle procedure
  let vx (dx * speed)
  let vy (dy * speed) - (gravity-acceleration * tick-delta)
  set speed sqrt ((vy ^ 2) + (vx ^ 2))
  recolor
  set heading atan vx vy
end


to check-for-collision  ;; particle procedure
  ;; Here we impose a rule that collisions only take place when there
  ;; are exactly two particles per patch.

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
;;   1. Do initial setup, and determine the heading between particle centers
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
  ;; local copies of other-particle's relevant quantities:
  ;; mass2 speed2 heading2
  ;;
  ;; quantities used in the collision itself
  ;; theta   ;; heading of vector from my center to the center of other-particle.
  ;; v1t     ;; velocity of self along direction theta
  ;; v1l     ;; velocity of self perpendicular to theta
  ;; v2t v2l ;; velocity of other-particle, represented in the same way
  ;; vcm     ;; velocity of the center of mass of the colliding particles,
             ;;   along direction theta

  ;;; PHASE 1: initial setup

  ;; for convenience, grab some quantities from other-particle
  let mass2 [mass] of other-particle
  let speed2 [speed] of other-particle
  let heading2 [heading] of other-particle

  ;; since particles are modeled as zero-size points, theta isn't meaningfully
  ;; defined. we can assign it randomly without affecting the model's outcome.
  let theta (random-float 360)



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
  set energy (0.5 * mass * (speed ^ 2))
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

  ;; PHASE 5: final updates

  ;; now recolor, since color is based on quantities that may have changed
  recolor
  ask other-particle
    [ recolor ]
end

to recolor  ;; particle procedure
  ifelse speed < (0.5 * init-particle-speed)
  [
    set color blue
  ]
  [
    ifelse speed > (1.5 * init-particle-speed)
      [ set color red ]
      [ set color green ]
  ]
end

;;;
;;; drawing procedures
;;;


;; creates initial particles
to make-particles
  create-particles number-of-particles
  [
    setup-particle
    random-position
    recolor
  ]
  calculate-tick-delta
end


to setup-particle  ;; particle procedure
  set speed init-particle-speed
  set mass particle-mass
  set energy (0.5 * mass * (speed ^ 2))
  set last-collision nobody
end




to-report last-n [n the-list]
  ifelse n >= length the-list
    [ report the-list ]
    [ report last-n n butfirst the-list ]
end


;; place particle at random location inside the box.
to random-position ;; particle procedure
  setxy random-xcor random-float ( ( world-height  - 1 ) / 2 ) + 1
end


to draw-graph
  let n min-pycor
  repeat (max-pycor / 2)
  [ let x particles with [ (pycor >= n) and (pycor < (n + 4)) ]
    ifelse count x = 0
     [ plot 0 ]
     [ plot mean [energy] of x ]
    set n n + 4
  ]
end


;; histogram procedure
to draw-vert-line [ xval ]
  plotxy xval plot-y-min
  plot-pen-down
  plotxy xval plot-y-max
  plot-pen-up
end
@#$#@#$#@
GRAPHICS-WINDOW
315
10
649
365
40
-1
4.0
1
10
1
1
1
0
1
0
1
-40
40
0
80
1
1
1
ticks
30.0

BUTTON
7
43
93
76
go/stop
go
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
0

BUTTON
7
10
93
43
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

SLIDER
96
10
302
43
number-of-particles
number-of-particles
1
1000
100
1
1
NIL
HORIZONTAL

MONITOR
8
212
131
257
average speed
avg-speed
2
1
11

PLOT
613
390
902
586
Energy Histogram
energy
count
0.0
400.0
0.0
10.0
false
true
"set-plot-x-range 0 (0.5 * ((init-particle-speed * 3) ^ 2 ) * particle-mass)\nset-plot-y-range 0 ceiling (number-of-particles / 10)\n" ""
PENS
"fast" 1.0 1 -1893860 true "set-histogram-num-bars 45\n" "histogram [ energy ] of particles with [color = red]"
"medium" 1.0 1 -11686070 true "set-histogram-num-bars 45\n" "histogram [ energy ] of particles with [color = green]"
"slow" 1.0 1 -14979122 true "set-histogram-num-bars 45\n" "histogram [ energy ] of particles with [color = blue]"
"avg-energy" 1.0 0 -7500403 true "" "plot-pen-reset  draw-vert-line avg-energy"
"init-avg-energy" 1.0 0 -16777216 true "draw-vert-line avg-energy-init" ""

PLOT
10
390
305
586
Speed Counts
time
count (%)
0.0
20.0
0.0
100.0
true
true
"set-plot-y-range 0 100" ""
PENS
"fast" 1.0 0 -1893860 true "" "plotxy ticks percent-fast"
"medium" 1.0 0 -11686070 true "" "plotxy ticks percent-medium"
"slow" 1.0 0 -14979122 true "" "plotxy ticks percent-slow"

SWITCH
96
44
199
77
collide?
collide?
0
1
-1000

PLOT
311
390
605
586
Speed Histogram
speed
count
0.0
20.0
0.0
40.0
false
true
"set-plot-x-range 0 (init-particle-speed * 3)\nset-plot-y-range 0 ceiling (number-of-particles / 10)\n" ""
PENS
"fast" 5.0 1 -1893860 true "set-histogram-num-bars 45" "histogram [ speed ] of particles with [color = red]"
"medium" 5.0 1 -11686070 true "set-histogram-num-bars 45" "histogram [ speed ] of particles with [color = green]"
"slow" 5.0 1 -14979122 true "set-histogram-num-bars 45" "histogram [ speed ] of particles with [color = blue]"
"avg-speed" 1.0 0 -7500403 true "draw-vert-line avg-speed-init" "plot-pen-reset   draw-vert-line avg-speed"
"init-avg-speed" 1.0 0 -16777216 true "" ""

SLIDER
7
156
193
189
gravity-acceleration
gravity-acceleration
0
15
9.8
0.2
1
NIL
HORIZONTAL

SLIDER
7
82
193
115
init-particle-speed
init-particle-speed
1
20
10
1
1
NIL
HORIZONTAL

SLIDER
7
119
193
152
particle-mass
particle-mass
1
20
1
1
1
NIL
HORIZONTAL

SWITCH
199
44
302
77
trace?
trace?
0
1
-1000

PLOT
659
199
898
364
Density Histogram
height
count
0.0
160.0
0.0
160.0
true
false
"set-plot-x-range 0 (max-pycor * 2)\nset-plot-y-range 0 (max-pxcor * 2)\nset-histogram-num-bars round (max-pxcor / 2)" ""
PENS
"default" 1.0 1 -11686070 true "" "histogram [ycor + max-pycor] of particles"

PLOT
659
10
898
191
Energy vs. Height
height
energy
0.0
160.0
0.0
500.0
true
false
"set-plot-x-range 0 (max-pycor * 2)" ""
PENS
"default" 4.0 1 -11686070 true "" "plot-pen-reset draw-graph"

MONITOR
10
335
98
380
percent fast
percent-fast
0
1
11

MONITOR
103
336
207
381
percent medium
percent-medium
0
1
11

MONITOR
212
336
304
381
percent slow
percent-slow
0
1
11

MONITOR
9
274
132
319
percent lost particles
percent-lost-particles
1
1
11

MONITOR
143
212
261
257
average energy
avg-energy
1
1
11

BUTTON
199
77
302
110
clear trace
clear-drawing
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

This model is one in a series of GasLab models. They use the same basic rules for simulating the behavior of gases.  Each model integrates different features in order to highlight different aspects of gas behavior.

The basic principle of the models is that gas particles are assumed to have two elementary actions: they move and they collide --- either with other particles or with any other objects such as walls.

In this model, a gaseous atmosphere is placed above the surface of a "planet", represented by a yellow line at the bottom of the world.

## HOW IT WORKS

The basic principle of all GasLab models is the following algorithm (for more details, see the model "GasLab Gas in a Box"):

1) A particle moves in a straight line without changing its speed, unless it collides with another particle or bounces off the wall.  
2) Two particles "collide" if they find themselves on the same patch (NetLogo's View is composed of a grid of small squares called patches). In this model, two particles are aimed so that they will collide at the origin.  
3) An angle of collision for the particles is chosen, as if they were two solid balls that hit, and this angle describes the direction of the line connecting their centers.  
4) The particles exchange momentum and energy only along this line, conforming to the conservation of momentum and energy for elastic collisions.  
5) Each particle is assigned its new speed, heading and energy.  
6) If a particle finds itself on or very close to the surface of the planet, it "bounces" --- that is, reflects its direction and keeps its same speed.

In this model, the effect of gravity is calculated as follows: every particle is given additional velocity downward during each clock tick, as it would get in a gravitational field. The particles bounce off the "ground". They disappear if they reach the top of the world, as if they had escaped the planet's gravitational field. The percentage of lost particles is shown in the PERCENT LOST PARTICLES monitor.

## HOW TO USE IT

Initial settings:  
- NUMBER-OF-PARTICLES: number of gas particles  
- INIT-PARTICLE-SPEED: initial speed of each particle  
- PARTICLE-MASS: mass of each particle  
- GRAVITY-ACCELERATION: value of the gravitational acceleration

The SETUP button will set the initial conditions.  The GO button will run the simulation.

Other settings:  
- COLLIDE?: Turns collisions between particles on and off.  
- TRACE?: Traces the path of one of the particles.

Monitors:  
- AVERAGE SPEED: average speed of the particles.  
- PERCENT FAST, PERCENT MEDIUM, PERCENT SLOW: percentage of particles with different speeds - fast (red), medium (green), and slow (blue).  
- PERCENT LOST PARTICLES: percentage of particles that have disappeared at the top edge of the world.  
- CLOCK (inside the View): number of ticks that have run.

Plots:  
- SPEED COUNTS: plots the number of particles in each range of speed.  
- SPEED HISTOGRAM: speed distribution of all the particles.  The gray line is the average value, and the black line is the initial average.  
- ENERGY HISTOGRAM: distribution of energies of all the particles, using m*(v^2)/2.  
- DENSITY HISTOGRAM: shows the number of particles at each 'layer' of the atmosphere, i.e. its local density.  
- ENERGY VS. HEIGHT: shows the mean energy of the particles at each "layer" of the atmosphere.

## THINGS TO NOTICE

Try to predict what the view will look like after a while, and why.

Watch the gray path of one particle. What can you say about its motion? Turn COLLIDE? off and see if there are any differences.

Watch the change in density distribution as the model runs.

As the model runs, what happens to the average speed and kinetic energy of the particles?  If they gain energy, where does it come from?   What happens to the speed and energy distributions?

## THINGS TO TRY

What happens when gravity is increased or decreased?

Change the initial number, speed and mass.  What happens to the density distribution?

What factors affect how many particles escape this planet?

Does this model come to some sort of equilibrium?  How can you tell when it has been reached?

Try and find out if the distribution of the particles in this model is the same as what is predicted by conventional physical laws. Is this consistent, for instance, with the fact that high-altitude places have lower pressure ( and thus lower density of air)? Why are they colder?

Try making gravity negative.

## EXTENDING THE MODEL

Find a way to plot the relative "temperature" of the gas as a function of distance from the planet.

Try this model with particles of different masses.  You could color each mass differently to be able to see where they go.  Are their distributions different?  Which ones escape most easily?  What does this suggest about the composition of an atmosphere?

The fact that particles escape when they reach a certain height isn't completely realistic, especially in the case when the particle was about to turn back towards the planet.  Improve the model by allowing particles that have "escaped" to re-enter the atmosphere once gravity pulls them back down.  How does this change the behavior of the model?  Keeping track of actual losses (particles which reached the escape velocity of the planet) would be interesting.  Under what conditions will particles reach escape velocity at all?

Make the "planet" into a central point instead of a flat plane.

This basic model could be used to explore other situations where freely moving particles have forces on them --- e.g. a centrifuge or charged particles (ions) in an electrical field.

## NETLOGO FEATURES

Because of the influence of gravity, the particles follow curved paths.  Since NetLogo models time in discrete steps, these curved paths must be approximated with a series of short straight lines.  This is the source of a slight inaccuracy where the particles gradually lose energy if the model runs for a long time.  The effect is as though the collisions with the ground were slightly inelastic.  The inaccuracy can be reduced by increasing `vsplit`, but the model will run slower.

## RELATED MODELS

This model is part of the GasLab suite and curriculum.  
See, in particular, the models "Gas in a Box" and "Gravity Box", which is a modified version of the "Atmosphere" model, with a ceiling on the atmosphere.

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

plane
false
0
Rectangle -7500403 true true 30 30 270 270

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

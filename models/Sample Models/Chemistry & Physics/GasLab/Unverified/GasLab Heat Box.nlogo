globals
[
  tick-delta                      ;; how much we advance the tick counter this time through
  max-tick-delta                  ;; the largest tick-delta is allowed to be
  box-edge                        ;; distance of box edge from axes
  pressure                        ;; pressure in the box
  pressure-history
  zero-pressure-count             ;; how many zero entries are in pressure-history
  wall-hits-per-particle          ;; average number of wall hits per particle
  length-horizontal-surface       ;; the size of the wall surfaces that run horizontally - the top and bottom of the box
  length-vertical-surface         ;; the size of the wall surfaces that run vertically - the left and right of the box
  walls                           ;; agentset containing patches that are the walls of the box
  init-avg-speed init-avg-energy  ;; initial averages
  avg-speed avg-energy            ;; current averages
  fast medium slow                ;; current counts
  percent-slow percent-medium percent-fast   ;;  percentage of current counts
  outside-energy
]

breed [ particles particle ]
breed [ flashes flash ]

flashes-own [birthday]

particles-own
[
  speed mass energy          ;; particle info
  wall-hits                  ;; number of wall hits during this cycle ("big tick")
  momentum-difference        ;; used to calculate pressure from wall hits
  last-collision             ;; used to prevent particles from colliding multiple times
]

to setup
  clear-all
  set-default-shape particles "circle"
  set-default-shape flashes "plane"
  set max-tick-delta 0.1073
  ;; box has constant size...
  set box-edge (max-pxcor - 1)
  ;;; the length of the horizontal or vertical surface of
  ;;; the inside of the box must exclude the two patches
  ;; that are the where the perpendicular walls join it,
  ;;; but must also add in the axes as an additional patch
  ;;; example:  a box with an box-edge of 10, is drawn with
  ;;; 19 patches of wall space on the inside of the box
  set length-horizontal-surface  ( 2 * (box-edge - 1) + 1)
  set length-vertical-surface  ( 2 * (box-edge - 1) + 1)
  make-box
  make-particles
  set pressure-history [0 0 0]  ;; plotted pressure will be averaged over the past 3 entries
  set zero-pressure-count 0
  update-variables
  set init-avg-speed avg-speed
  set init-avg-energy avg-energy
  reset-ticks
end

to update-variables
  set medium count particles with [color = green]
  set slow   count particles with [color = blue]
  set fast   count particles with [color = red]
  set percent-medium (medium / (count particles)) * 100
  set percent-slow (slow / (count particles)) * 100
  set percent-fast (fast / (count particles)) * 100
  set avg-speed  mean [speed] of particles
  set avg-energy mean [energy] of particles
end

to go
  ask walls
    [set pcolor box-color]
  ask particles [ bounce ]
  ask particles [ move ]
  if collide? [
    ask particles
      [ check-for-collision ]
  ]
  tick-advance tick-delta
  if floor ticks > floor (ticks - tick-delta)
  [
    ifelse any? particles
      [ set wall-hits-per-particle mean [wall-hits] of particles ]
      [ set wall-hits-per-particle 0 ]
    ask particles
      [ set wall-hits 0 ]
    calculate-pressure
    update-variables
    update-plots
  ]
  calculate-tick-delta
  ask flashes with [ticks - birthday > 0.4]
    [ die ]
  set outside-energy outside-temperature
  display
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

;;; Pressure is defined as the force per unit area.  In this context,
;;; that means the total momentum per unit time transferred to the walls
;;; by particle hits, divided by the surface area of the walls.  (Here
;;; we're in a two dimensional world, so the "surface area" of the walls
;;; is just their length.)  Each wall contributes a different amount
;;; to the total pressure in the box, based on the number of collisions, the
;;; direction of each collision, and the length of the wall.  Conservation of momentum
;;; in hits ensures that the difference in momentum for the particles is equal to and
;;; opposite to that for the wall.  The force on each wall is the rate of change in
;;; momentum imparted to the wall, or the sum of change in momentum for each particle:
;;; F = SUM  [d(mv)/dt] = SUM [m(dv/dt)] = SUM [ ma ], in a direction perpendicular to
;;; the wall surface.  The pressure (P) on a given wall is the force (F) applied to that
;;; wall over its surface area.  The total pressure in the box is sum of each wall's
;;; pressure contribution.

to calculate-pressure
  ;; by summing the momentum change for each particle,
  ;; the wall's total momentum change is calculated
  set pressure 15 * sum [momentum-difference] of particles
  set pressure-history lput pressure but-first pressure-history
  ask particles
    [ set momentum-difference 0 ]  ;; once the contribution to momentum has been calculated
                                   ;; this value is reset to zero till the next wall hit
end

to bounce  ;; particle procedure
  ;; get the coordinates of the patch we'll be on if we go forward 1
  let new-patch patch-ahead 1
  let new-px [pxcor] of new-patch
  let new-py [pycor] of new-patch
  ;; if we're not about to hit a wall, we don't need to do any further checks
  if (abs new-px != box-edge and abs new-py != box-edge)
    [stop]
  ;; if hitting left or right wall, reflect heading around x axis
  if (abs new-px = box-edge)
    [ set heading (- heading)
      set wall-hits wall-hits + 1
  ;;  if the particle is hitting a vertical wall, only the horizontal component of the speed
  ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
  ;; due to the reversing of direction of travel from the collision with the wall
      set momentum-difference momentum-difference + (abs (dx * 2 * mass * speed) / length-vertical-surface) ]
  ;; if hitting top or bottom wall, reflect heading around y axis
  if (abs new-py = box-edge)
    [ set heading (180 - heading)
      set wall-hits wall-hits + 1
  ;;  if the particle is hitting a horizontal wall, only the vertical component of the speed
  ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
  ;; due to the reversing of direction of travel from the collision with the wall
      set momentum-difference momentum-difference + (abs (dy * 2 * mass * speed) / length-horizontal-surface)  ]

  if [heated-wall?] of patch new-px new-py  ;; check if the patch ahead of us is heated
  [

    set energy ((energy +  outside-energy ) / 2)
    set speed sqrt (2 * energy / mass )
    recolor
  ]

  ask patch new-px new-py
  [ sprout-flashes 1 [
      set color pcolor - 2
      set birthday ticks
      set heading 0
    ]
  ]
end

to move  ;; particle procedure
  if patch-ahead (speed * tick-delta) != patch-here
    [ set last-collision nobody ]
  jump (speed * tick-delta)
end

to check-for-collision  ;; particle procedure
  ;; Here we impose a rule that collisions only take place when there
  ;; are exactly two particles per patch.  We do this because when the
  ;; student introduces new particles from the side, we want them to
  ;; form a uniform wavefront.
  ;;
  ;; Why do we want a uniform wavefront?  Because it is actually more
  ;; realistic.  (And also because the curriculum uses the uniform
  ;; wavefront to help teach the relationship between particle collisions,
  ;; wall hits, and pressure.)
  ;;
  ;; Why is it realistic to assume a uniform wavefront?  Because in reality,
  ;; whether a collision takes place would depend on the actual headings
  ;; of the particles, not merely on their proximity.  Since the particles
  ;; in the wavefront have identical speeds and near-identical headings,
  ;; in reality they would not collide.  So even though the two-particles
  ;; rule is not itself realistic, it produces a realistic result.  Also,
  ;; unless the number of particles is extremely large, it is very rare
  ;; for three or more particles to land on the same patch (for example,
  ;; with 400 particles it happens less than 1% of the time).  So imposing
  ;; this additional rule should have only a negligible effect on the
  ;; aggregate behavior of the system.
  ;;
  ;; Why does this rule produce a uniform wavefront?  The particles all
  ;; start out on the same patch, which means that without the only-two
  ;; rule, they would all start colliding with each other immediately,
  ;; resulting in much random variation of speeds and headings.  With
  ;; the only-two rule, they are prevented from colliding with each other
  ;; until they have spread out a lot.  (And in fact, if you observe
  ;; the wavefront closely, you will see that it is not completely smooth,
  ;; because some collisions eventually do start occurring when it thins out while fanning.)

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
  ifelse speed < (0.5 * 10)
  [
    set color blue
  ]
  [
    ifelse speed > (1.5 * 10)
      [ set color red ]
      [ set color green ]
  ]
end

;; reports color of box according to temperature and position
;; if only one side is heated, the other walls will be yellow
to-report box-color
     ifelse heated-wall?
       [report scale-color red outside-temperature -200 600]
       [report yellow]

end

;; reports true if there is a heated wall at the given location
to-report heated-wall?
     ifelse one-side?
     [
       ifelse ((pxcor = (- box-edge)) and (abs pycor < box-edge))
        [report true]
        [report false]
     ]
     [
      if (( abs pxcor = box-edge) and (abs pycor <= box-edge)) or
                     ((abs pycor = box-edge) and (abs pxcor <= box-edge))
        [report true]
     ]
     report false
end

;;;
;;; drawing procedures
;;;

;; draws the box
to make-box
  set walls patches with [ ((abs pxcor = box-edge) and (abs pycor <= box-edge)) or
                     ((abs pycor = box-edge) and (abs pxcor <= box-edge)) ]
  ask walls
    [ set pcolor box-color ]
end

;; creates initial particles
to make-particles
  create-particles number-of-particles
  [
    setup-particle
    set speed random-float 20
    set energy (0.5 * mass * speed * speed)
    random-position
    recolor
  ]
  calculate-tick-delta
end


to setup-particle  ;; particle procedure
  set speed 10
  set mass 1.0
  set energy (0.5 * mass * speed * speed)
  set last-collision nobody
  set wall-hits 0
  set momentum-difference 0
end

;; place particle at random location inside the box.
to random-position ;; particle procedure
  setxy ((1 - box-edge) + random-float ((2 * box-edge) - 2))
        ((1 - box-edge) + random-float ((2 * box-edge) - 2))
end


;;; plotting procedures

to-report init-particle-speed
  ;;the initial speeds in this model cannot be faster than 20 at setup
  report 20
end

to-report particle-mass
  ;; each particle has a mass of 1
  report 1
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
325
11
659
366
40
40
4.0
1
10
1
1
1
0
0
0
1
-40
40
-40
40
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
318
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

PLOT
779
197
1017
376
Pressure vs. Time
time
pressure
0.0
20.0
0.0
100.0
true
false
"" ""
PENS
"default" 1.0 0 -955883 true "" "plotxy ticks (mean pressure-history)"

MONITOR
156
315
308
360
pressure
pressure
0
1
11

MONITOR
11
315
146
360
wall hits per particle
wall-hits-per-particle
2
1
11

PLOT
781
383
1019
568
Wall Hits per Particle
NIL
NIL
0.0
20.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "if ticks > 1[plotxy ticks wall-hits-per-particle]"

SWITCH
96
44
214
77
one-side?
one-side?
1
1
-1000

SLIDER
7
81
189
114
outside-temperature
outside-temperature
5
400
50
5
1
NIL
HORIZONTAL

MONITOR
11
260
146
305
average speed
avg-speed
1
1
11

PLOT
522
383
776
569
Energy Histogram
energy
count
0.0
800.0
0.0
10.0
false
false
"set-plot-x-range 0 (0.5 * (init-particle-speed * 2) * (init-particle-speed * 2) * particle-mass)\nset-plot-y-range 0 ceiling (number-of-particles / 4)" ""
PENS
"fast" 10.0 1 -2674135 true "set-histogram-num-bars 40" "histogram [ energy ] of particles with [color = red]"
"medium" 10.0 1 -10899396 true "set-histogram-num-bars 40" "histogram [ energy ] of particles with [color = green]"
"slow" 10.0 1 -13345367 true "set-histogram-num-bars 40" "histogram [ energy ] of particles with [color = blue]"
"avg-energy" 1.0 0 -7500403 true "" "plot-pen-reset\ndraw-vert-line avg-energy"
"init-avg-energy" 1.0 0 -16777216 true "draw-vert-line init-avg-energy" ""

MONITOR
156
260
307
305
average energy
avg-energy
1
1
11

PLOT
10
383
260
568
Speed Counts
time
count (%)
0.0
100.0
0.0
100.0
true
false
"set-plot-y-range 0 100" ""
PENS
"fast" 1.0 0 -2674135 true "" "plotxy ticks percent-fast"
"medium" 1.0 0 -10899396 true "" "plotxy ticks percent-medium"
"slow" 1.0 0 -13345367 true "" "plotxy ticks percent-slow"

PLOT
778
12
1017
194
Temperature vs. Time
time
temperature
0.0
100.0
0.0
100.0
true
false
"" ""
PENS
"outside" 1.0 0 -13345367 true "" "plotxy ticks outside-temperature"
"inside" 1.0 0 -6459832 true "" "plotxy ticks avg-energy"

SWITCH
214
44
317
77
collide?
collide?
0
1
-1000

PLOT
267
383
518
569
Speed Histogram
speed
count
0.0
50.0
0.0
100.0
false
false
"set-plot-x-range 0 (init-particle-speed * 2)\nset-plot-y-range 0 ceiling (number-of-particles / 6)" ""
PENS
"fast" 5.0 1 -2674135 true "set-histogram-num-bars 40" "histogram [ speed ] of particles with [color = red]"
"medium" 5.0 1 -10899396 true "set-histogram-num-bars 40" "histogram [ speed ] of particles with [color = green]"
"slow" 5.0 1 -13345367 true "set-histogram-num-bars 40" "histogram [ speed ] of particles with [color = blue]"
"avg-speed" 1.0 0 -7500403 true "" "plot-pen-reset\ndraw-vert-line avg-speed"
"init-avg-speed" 1.0 0 -16777216 true "draw-vert-line init-avg-speed" ""

@#$#@#$#@
## WHAT IS IT?

This model is one in a series of GasLab models. They use the same basic rules for simulating the behavior of gases.  Each model integrates different features in order to highlight different aspects of gas behavior.

The basic principle of the models is that gas particles are assumed to have two elementary actions: they move and they collide --- either with other particles or with any other objects such as walls.

This model is illustrates the relationship between temperature and pressure in  
a fixed volume gas container.

This model is part of the Connected Mathematics "Making Sense of Complex Phenomena" Modeling Project.

## HOW IT WORKS

The particles are modeled as hard balls with no internal energy except that which is due to their motion.  Collisions between particles are elastic.

1. A particle moves in a straight line without changing its speed, unless it collides with another particle or bounces off the wall.  
2. Two particles "collide" if they find themselves on the same patch. In this model, two turtles are aimed so that they will collide at the origin.  
3. An angle of collision for the particles is chosen, as if they were two solid balls that hit, and this angle describes the direction of the line connecting their centers.  
4. The particles exchange momentum and energy only along this line, conforming to the conservation of momentum and energy for elastic collisions.  
5. Each particle is assigned its new speed, heading and energy.  
6. If a particle finds itself on or very close to a wall of the container, it "bounces" --- that is, reflects its direction and keeps its same speed.

As the walls of the box are heated, the sides of the walls will change color from a deep red (cool) to a bright red, to pink to a pale pink white (hot).  The walls contain a constant heat value throughout the simulation.  If ONE-SIDE? is set to ON, only the left wall will be heated, while the other three walls remain yellow.

The exact way particles gain energy from the walls of the box is as follows:  
1. Particles check their state of energy.  
2. They hit or bounce off the wall.  
3. They find wall energy and recalculate their new energy.  
4. They change their speed and direction after the wall hit.

## HOW TO USE IT

### Initial settings

- NUMBER-OF-PARTICLES: number of particles within in the box

### Buttons

The SETUP button will set these initial conditions.  
The GO button will begin the simulation.

### Other Settings

-  OUTSIDE TEMPERATURE: temperature of the outside of the box and the wall of the box.  
-  ONE SIDE?: heats only the left wall if enabled.  the other walls are colored yellow, and do not affect the energy of the particles that bounce into it.  
- COLLIDE?: Turns collisions between particles on and off.

### Monitors

- PRESSURE: the pressure of the gas particles in the box  
- WALL HITS PER PARTICLE: number of times that each particle hit the walls  
- AVERAGE SPEED: average speed of the particles.  
- AVERAGE ENERGY: average kinetic energy of the particles.

### Plots

- SPEED COUNTS: plots the number of particles in each range of speed.  
- SPEED HISTOGRAM: speed distribution of all the particles.  The gray line is the average value, and the black line is the initial average.  
- ENERGY HISTOGRAM: distribution of energies of all the particles, calculated as m*(v^2)/2.  
- PRESSURE VS. TIME: plots average pressure of the inside of the box over time.  
- TEMPERATURE VS. TIME: plots particle temperature inside the box over time and wall temperature over time.  
- WALL HITS PER PARTICLE: plots average wall hits per particle over time.

## THINGS TO NOTICE

How does adding heat to the box walls affect the pressure?

How does adding heat to the wall affect the particle behavior?

How does the particle behavior or system response change with only one wall heated instead of all walls heated?

Does the system reach an equilibrium temperature faster when the wall is heated or cooled the same amount in comparison to the temperature of the particles?

## THINGS TO TRY

Try to get the inside temperature to reach the outside temperature.  Is this possible?

Try to increase the wall hits per particle.

## EXTENDING THE MODEL

Give the wall a mass and see how that affects the behavior of the model.

Close off the right side of the box.  Create two valves on either side to the wall that allow the user to "spurt" particles into the chambers to see how number of particles affects pressure.

Vary the width and length of the box, does this effect how fast the particle temperature changes?

## NETLOGO FEATURES

Notice how the collisions are detected by the turtles and how the code guarantees the same two particles do not collide twice.  What happens if we let the patches detect them?

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
NetLogo 5.0beta4
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

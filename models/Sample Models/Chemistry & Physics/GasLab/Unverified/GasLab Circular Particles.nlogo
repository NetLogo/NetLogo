globals [
  tick-delta        ;; how much simulation time will pass in this step
  box-edge          ;; distance of box edge from origin
  collisions        ;; list used to keep track of future collisions
  particle1         ;; first particle currently colliding
  particle2         ;; second particle currently colliding
  init-avg-speed init-avg-energy             ;; initial averages
  avg-speed avg-energy                       ;; current averages
  fast medium slow                           ;; current counts
  percent-slow percent-medium percent-fast   ;; percentage of current counts
]

breed [particles particle]
particles-own [
  speed
  mass
  energy          ;; particle info
]

;;;
;;; SETUP PROCEDURES
;;;

to setup
  clear-all
  set-default-shape particles "circle"
  set box-edge max-pxcor - 1
  ask patches with [(abs pxcor = box-edge or abs pycor = box-edge) and
                    abs pxcor <= box-edge and abs pycor <= box-edge]
    [ set pcolor yellow ]
  set avg-speed 1
  make-particles
  set particle1 nobody
  set particle2 nobody
  reset-ticks
  set collisions []
  ask particles [ check-for-wall-collision ]
  ask particles [ check-for-particle-collision ]
  update-variables
  set init-avg-speed avg-speed
  set init-avg-energy avg-energy
end

to make-particles
  create-particles number [
    set speed 1

    set size smallest-particle-size
             + random-float (largest-particle-size - smallest-particle-size)
    ;; set the mass proportional to the area of the particle
    set mass (size * size)
    set energy (0.5 * mass * speed * speed)

    recolor
  ]
  ;; When space is tight, placing the big particles first improves
  ;; our chances of eventually finding places for all of them.
  foreach sort-by [[size] of ?1 > [size] of ?2] particles [
    ask ? [
      position-randomly
      while [overlapping?] [ position-randomly ]
    ]
  ]
end

to position-randomly  ;; particle procedure
  ;; place particle at random location inside the box
  setxy one-of [1 -1] * random-float (box-edge - 0.5 - size / 2)
        one-of [1 -1] * random-float (box-edge - 0.5 - size / 2)
end

to-report overlapping?  ;; particle procedure
  ;; here, we use IN-RADIUS just for improved speed; the real testing
  ;; is done by DISTANCE
  report any? other particles in-radius ((size + largest-particle-size) / 2)
                              with [distance myself < (size + [size] of myself) / 2]
end

;;;
;;; GO PROCEDURES
;;;

to go
  choose-next-collision
  ask particles [ jump speed * tick-delta ]
  perform-next-collision
  tick-advance tick-delta
  recalculate-particles-that-just-collided
  if floor ticks > floor (ticks - tick-delta)
  [
    update-variables
    update-plots
  ]
end

to update-variables
  set medium count particles with [color = green]
  set slow   count particles with [color = blue]
  set fast   count particles with [color = red]
  set percent-medium (medium / ( count particles )) * 100
  set percent-slow (slow / (count particles)) * 100
  set percent-fast (fast / (count particles)) * 100
  set avg-speed  mean [speed] of particles
  set avg-energy mean [energy] of particles
end


to recalculate-particles-that-just-collided
  ;; Since only collisions involving the particles that collided most recently could be affected,
  ;; we filter those out of collisions.  Then we recalculate all possible collisions for
  ;; the particles that collided last.  The ifelse statement is necessary because
  ;; particle2 can be either a particle or a string representing a wall.  If it is a
  ;; wall, we don't want to invalidate all collisions involving that wall (because the wall's
  ;; position wasn't affected, those collisions are still valid.
  ifelse is-turtle? particle2
    [
      set collisions filter [item 1 ? != particle1 and
                             item 2 ? != particle1 and
                             item 1 ? != particle2 and
                             item 2 ? != particle2]
                            collisions
      ask particle2 [ check-for-wall-collision ]
      ask particle2 [ check-for-particle-collision ]
    ]
    [
      set collisions filter [item 1 ? != particle1 and
                             item 2 ? != particle1]
                            collisions
    ]
  if particle1 != nobody [ ask particle1 [ check-for-wall-collision ] ]
  if particle1 != nobody [ ask particle1 [ check-for-particle-collision ] ]
  ;; Slight errors in floating point math can cause a collision that just
  ;; happened to be calculated as happening again a very tiny amount of
  ;; time into the future, so we remove any collisions that involves
  ;; the same two particles (or particle and wall) as last time.
  set collisions filter [item 1 ? != particle1 or
                         item 2 ? != particle2]
                        collisions
  ;; All done.
  set particle1 nobody
  set particle2 nobody
end

;; check-for-particle-collision is a particle procedure that determines the time it takes
;; to the collision between two particles (if one exists).  It solves for the time by representing
;; the equations of motion for distance, velocity, and time in a quadratic equation of the vector
;; components of the relative velocities and changes in position between the two particles and
;; solves for the time until the next collision
to check-for-particle-collision
  let my-x xcor
  let my-y ycor
  let my-particle-size size
  let my-x-speed speed * dx
  let my-y-speed speed * dy
  ask other particles
  [
    let dpx (xcor - my-x)   ;; relative distance between particles in the x direction
    let dpy (ycor - my-y)    ;; relative distance between particles in the y direction
    let x-speed (speed * dx) ;; speed of other particle in the x direction
    let y-speed (speed * dy) ;; speed of other particle in the x direction
    let dvx (x-speed - my-x-speed) ;; relative speed difference between particles in x direction
    let dvy (y-speed - my-y-speed) ;; relative speed difference between particles in y direction
    let sum-r (((my-particle-size) / 2 ) + (([size] of self) / 2 )) ;; sum of both particle radii

    ;; To figure out what the difference in position (P1) between two particles at a future
    ;; time (t) will be, one would need to know the current difference in position (P0) between the
    ;; two particles and the current difference in the velocity (V0) between the two particles.
    ;;
    ;; The equation that represents the relationship is:
    ;;   P1 = P0 + t * V0
    ;; we want find when in time (t), P1 would be equal to the sum of both the particle's radii
    ;; (sum-r).  When P1 is equal to is equal to sum-r, the particles will just be touching each
    ;; other at their edges (a single point of contact).
    ;;
    ;; Therefore we are looking for when:   sum-r =  P0 + t * V0
    ;;
    ;; This equation is not a simple linear equation, since P0 and V0 should both have x and y
    ;; components in their two dimensional vector representation (calculated as dpx, dpy, and
    ;; dvx, dvy).
    ;;
    ;; By squaring both sides of the equation, we get:
    ;;   (sum-r) * (sum-r) =  (P0 + t * V0) * (P0 + t * V0)
    ;; When expanded gives:
    ;;   (sum-r ^ 2) = (P0 ^ 2) + (t * PO * V0) + (t * PO * V0) + (t ^ 2 * VO ^ 2)
    ;; Which can be simplified to:
    ;;   0 = (P0 ^ 2) - (sum-r ^ 2) + (2 * PO * V0) * t + (VO ^ 2) * t ^ 2
    ;; Below, we will let p-squared represent:   (P0 ^ 2) - (sum-r ^ 2)
    ;; and pv represent: (2 * PO * V0)
    ;; and v-squared represent: (VO ^ 2)
    ;;
    ;;  then the equation will simplify to:     0 = p-squared + pv * t + v-squared * t^2

    let p-squared   ((dpx * dpx) + (dpy * dpy)) - (sum-r ^ 2)   ;; p-squared represents difference
    ;; of the square of the radii and the square of the initial positions

    let pv  (2 * ((dpx * dvx) + (dpy * dvy)))  ;; vector product of the position times the velocity
    let v-squared  ((dvx * dvx) + (dvy * dvy)) ;; the square of the difference in speeds
    ;; represented as the sum of the squares of the x-component
    ;; and y-component of relative speeds between the two particles

    ;; p-squared, pv, and v-squared are coefficients in the quadratic equation shown above that
    ;; represents how distance between the particles and relative velocity are related to the time,
    ;; t, at which they will next collide (or when their edges will just be touching)

    ;; Any quadratic equation that is a function of time (t) can be represented as:
    ;;   a*t*t + b*t + c = 0,
    ;; where a, b, and c are the coefficients of the three different terms, and has solutions for t
    ;; that can be found by using the quadratic formula.  The quadratic formula states that if a is
    ;; not 0, then there are two solutions for t, either real or complex.
    ;; t is equal to (b +/- sqrt (b^2 - 4*a*c)) / 2*a
    ;; the portion of this equation that is under a square root is referred to here
    ;; as the determinant, D1.   D1 is equal to (b^2 - 4*a*c)
    ;; and:   a = v-squared, b = pv, and c = p-squared.
    let D1 pv ^ 2 -  (4 * v-squared * p-squared)

    ;; the next test tells us that a collision will happen in the future if
    ;; the determinant, D1 is > 0,  since a positive determinant tells us that there is a
    ;; real solution for the quadratic equation.  Quadratic equations can have solutions
    ;; that are not real (they are square roots of negative numbers).  These are referred
    ;; to as imaginary numbers and for many real world systems that the equations represent
    ;; are not real world states the system can actually end up in.

    ;; Once we determine that a real solution exists, we want to take only one of the two
    ;; possible solutions to the quadratic equation, namely the smaller of the two the solutions:
    ;;  (b - sqrt (b^2 - 4*a*c)) / 2*a
    ;;  which is a solution that represents when the particles first touching on their edges.
    ;;  instead of (b + sqrt (b^2 - 4*a*c)) / 2*a
    ;;  which is a solution that represents a time after the particles have penetrated
    ;;  and are coming back out of each other and when they are just touching on their edges.

    let time-to-collision  -1

    if D1 > 0
      [ set time-to-collision (- pv - sqrt D1) / (2 * v-squared) ]        ;; solution for time step

    ;; if time-to-collision is still -1 there is no collision in the future - no valid solution
    ;; note:  negative values for time-to-collision represent where particles would collide
    ;; if allowed to move backward in time.
    ;; if time-to-collision is greater than 1, then we continue to advance the motion
    ;; of the particles along their current trajectories.  They do not collide yet.

    if time-to-collision > 0
    [
      ;; time-to-collision is relative (ie, a collision will occur one second from now)
      ;; We need to store the absolute time (ie, a collision will occur at time 48.5 seconds.
      ;; So, we add clock to time-to-collision when we store it.
      ;; The entry we add is a three element list of the time to collision and the colliding pair.
      set collisions fput (list (time-to-collision + ticks) self myself)
                          collisions
    ]
  ]
end


;; determines when a particle will hit any of the four walls
to check-for-wall-collision  ;; particle procedure
  ;; right & left walls
  let x-speed (speed * dx)
  if x-speed != 0
    [ ;; solve for how long it will take particle to reach right wall
      let right-interval (box-edge - 0.5 - xcor - size / 2) / x-speed
      if right-interval > 0
        [ assign-colliding-wall right-interval "right wall" ]
      ;; solve for time it will take particle to reach left wall
      let left-interval ((- box-edge) + 0.5 - xcor + size / 2) / x-speed
      if left-interval > 0
        [ assign-colliding-wall left-interval "left wall" ] ]
  ;; top & bottom walls
  let y-speed (speed * dy)
  if y-speed != 0
    [ ;; solve for time it will take particle to reach top wall
      let top-interval (box-edge - 0.5 - ycor - size / 2) / y-speed
      if top-interval > 0
        [ assign-colliding-wall top-interval "top wall" ]
      ;; solve for time it will take particle to reach bottom wall
      let bottom-interval ((- box-edge) + 0.5 - ycor + size / 2) / y-speed
      if bottom-interval > 0
        [ assign-colliding-wall bottom-interval "bottom wall" ] ]
end


to assign-colliding-wall [time-to-collision wall]  ;; particle procedure
  ;; this procedure is used by the check-for-wall-collision procedure
  ;; to assemble the correct particle-wall pair
  ;; time-to-collision is relative (ie, a collision will occur one second from now)
  ;; We need to store the absolute time (ie, a collision will occur at time 48.5 seconds.
  ;; So, we add clock to time-to-collision when we store it.
  let colliding-pair (list (time-to-collision + ticks) self wall)
  set collisions fput colliding-pair collisions
end

to choose-next-collision
  if collisions = [] [ stop ]
  ;; Sort the list of projected collisions between all the particles into an ordered list.
  ;; Take the smallest time-step from the list (which represents the next collision that will
  ;; happen in time).  Use this time step as the tick-delta for all the particles to move through
  let winner first collisions
  foreach collisions [ if first ? < first winner [ set winner ? ] ]
  ;; winner is now the collision that will occur next
  let dt item 0 winner
  ;; If the next collision is more than 1 in the future,
  ;; only advance the simulation one tick, for smoother animation.
  set tick-delta dt - ticks
  if tick-delta > 1
    [ set tick-delta 1
      set particle1 nobody
      set particle2 nobody
      stop ]
  set particle1 item 1 winner
  set particle2 item 2 winner
end


to perform-next-collision
  ;; deal with 3 possible cases:
  ;; 1) no collision at all
  if particle1 = nobody [ stop ]
  ;; 2) particle meets wall
  if is-string? particle2
    [ if particle2 = "left wall" or particle2 = "right wall"
        [ ask particle1 [ set heading (- heading) ]
          stop ]
      if particle2 = "top wall" or particle2 = "bottom wall"
        [ ask particle1 [ set heading 180 - heading ]
          stop ] ]
  ;; 3) particle meets particle
  ask particle1 [ collide-with particle2 ]
end


to collide-with [other-particle]  ;; particle procedure
  ;;; PHASE 1: initial setup
  ;; for convenience, grab some quantities from other-particle
  let mass2 [mass] of other-particle
  let speed2 [speed] of other-particle
  let heading2 [heading] of other-particle
  ;; modified so that theta is heading toward other particle
  let theta towards other-particle

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
  set speed sqrt ((v1t * v1t) + (v1l * v1l))
  ;; if the magnitude of the velocity vector is 0, atan is undefined. but
  ;; speed will be 0, so heading is irrelevant anyway. therefore, in that
  ;; case we'll just leave it unmodified.
  set energy (0.5 * mass * speed ^ 2)

  if v1l != 0 or v1t != 0
    [ set heading (theta - (atan v1l v1t)) ]
  ;; and do the same for other-particle
  ask other-particle [
    set speed sqrt ((v2t ^ 2) + (v2l ^ 2))
    set energy (0.5 * mass * speed ^ 2)

    if v2l != 0 or v2t != 0
      [ set heading (theta - (atan v2l v2t)) ]
  ]

  ;; PHASE 5: recolor
  ;; since color is based on quantities that may have changed
  recolor
  ask other-particle [ recolor ]
end

;;;
;;; COLOR PROCEDURES
;;;


to recolor ;; particle procedure
  ;;let avg-speed 1
  ;; avg-speed is assumed to be 0.5, since particles are assigned a random speed between 0 and 1
  ;; particle coloring procedures for visualizing speed with a color palette,
  ;; red are fast particles, blue slow, and green in between.
  ifelse speed < (0.5 * avg-speed) ;; at lower than 50% the average speed
    [ set color blue ]      ;; slow particles colored blue
    [ ifelse speed > (1.5 * avg-speed) ;; above 50% higher the average speed
        [ set color red ]        ;; fast particles colored red
        [ set color green ] ]    ;; medium speed particles colored green
end


;;;
;;; AN EXPERIMENT IN TIME REVERSAL
;;;

;; Here's a procedure that demonstrates time-reversing the model.
;; You can run it from the command center.  When it finishes,
;; the final particle positions may be slightly different because
;; the amount of time that passes after the reversal might not
;; be exactly the same as the amount that passed before; this
;; doesn't indicate a bug in the model.
;; For larger values of n, you will start to notice larger
;; discrepancies, eventually causing the behavior of the system
;; to diverge totally. Unless the model has some bug we don't know
;; about, this is due to accumulating tiny inaccuracies in the
;; floating point calculations.  Once these inaccuracies accumulate
;; to the point that a collision is missed or an extra collision
;; happens, after that the reversed model will diverge rapidly.
to test-time-reversal [n]
  setup
  ask particles [ stamp ]
  while [ticks < n] [ go ]
  let old-ticks ticks
  reverse-time
  while [ticks < 2 * old-ticks] [ go ]
  ask particles [ set color white ]
end

to reverse-time
  ask particles [ rt 180 ]
  set collisions []
  ask particles [ check-for-wall-collision ]
  ask particles [ check-for-particle-collision ]
  ;; the last collision that happened before the model was paused
  ;; (if the model was paused immediately after a collision)
  ;; won't happen again after time is reversed because of the
  ;; "don't do the same collision twice in a row" rule.  We could
  ;; try to fool that rule by setting particle1 and
  ;; particle2 to nobody, but that might not always work,
  ;; because the vagaries of floating point math means that the
  ;; collision might be calculated to be slightly in the past
  ;; (the past that used to be the future!) and be skipped.
  ;; So to be sure, we force the collision to happen:
  perform-next-collision
end



;;; plotting procedures
to-report init-particle-speed
  report 1
end

to-report max-particle-mass
  report max [mass] of particles
end

to draw-vert-line [ xval ]
  plotxy xval plot-y-min
  plot-pen-down
  plotxy xval plot-y-max
  plot-pen-up
end
@#$#@#$#@
GRAPHICS-WINDOW
272
10
687
446
40
40
5.0
1
20
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
8
130
101
163
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
10
13
199
46
number
number
1
250
60
1
1
NIL
HORIZONTAL

BUTTON
107
130
194
163
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
10
92
198
125
largest-particle-size
largest-particle-size
1
10
6
0.5
1
NIL
HORIZONTAL

SLIDER
10
52
198
85
smallest-particle-size
smallest-particle-size
1
5
2
0.5
1
NIL
HORIZONTAL

MONITOR
5
167
101
212
average speed
avg-speed
2
1
11

PLOT
6
265
269
446
Speed Counts
NIL
NIL
0.0
10.0
0.0
10.0
true
false
"set-plot-y-range 0 100" ""
PENS
"fast" 1.0 0 -2674135 true "" "plotxy ticks percent-fast"
"medium" 1.0 0 -10899396 true "" "plotxy ticks percent-medium"
"slow" 1.0 0 -13345367 true "" "plotxy ticks percent-slow"

PLOT
6
449
340
627
Speed Histogram
NIL
NIL
0.0
10.0
0.0
10.0
true
false
"set-plot-x-range 0 ceiling (init-particle-speed * 2)\nset-plot-y-range 0 ceiling (count particles / 6)\nset-current-plot-pen \"medium\"\nset-histogram-num-bars 40\nset-current-plot-pen \"slow\"\nset-histogram-num-bars 40\nset-current-plot-pen \"fast\"\nset-histogram-num-bars 40\nset-current-plot-pen \"init-avg-speed\"\ndraw-vert-line init-avg-speed" ""
PENS
"fast" 1.0 1 -2674135 true "" "histogram [speed] of particles with [color = red]"
"medium" 1.0 1 -10899396 true "" "histogram [speed] of particles with [color = green]"
"slow" 1.0 1 -13345367 true "" "histogram [speed] of particles with [color = blue]"
"avg-speed" 1.0 0 -7500403 true "" "plot-pen-reset   draw-vert-line avg-speed"
"init-avg-speed" 1.0 0 -16777216 true "" ""

PLOT
343
449
687
627
Energy Histogram
NIL
NIL
0.0
200.0
0.0
10.0
true
false
"set-plot-x-range 0 ceiling (0.5 * (init-particle-speed * 2) * (init-particle-speed * 2) * max-particle-mass)\nset-plot-y-range 0 ceiling (count particles / 6)\n\nset-current-plot-pen \"particles\"\nset-histogram-num-bars 40\nset-current-plot-pen \"init-avg-energy\"\ndraw-vert-line init-avg-energy" ""
PENS
"particles" 1.0 1 -11053225 true "" "histogram [energy] of particles"
"avg-energy" 1.0 0 -7500403 true "" "plot-pen-reset   draw-vert-line avg-energy"
"init-avg-energy" 1.0 0 -16777216 true "" ""

MONITOR
102
166
205
211
average-energy
avg-energy
2
1
11

MONITOR
6
217
83
262
percent fast
percent-fast
0
1
11

MONITOR
86
217
182
262
percent medium
percent-medium
0
1
11

MONITOR
186
217
269
262
percent slow
percent-slow
0
1
11

@#$#@#$#@
## WHAT IS IT?

This model is one in a series of GasLab models. They use the same basic rules for simulating the behavior of gases.  Each model integrates different features in order to highlight different aspects of gas behavior.  This model is different from the other GasLab models in that the collision calculations take the circular shape and size of the particles into account, instead of modeling the particles as dimensionless points.

## HOW IT WORKS

The model determines the resulting motion of particles that collide, with no loss in their total momentum or total kinetic energy (an elastic collision).

To calculate the outcome of collision, it is necessary to calculate the exact time at which the edge of one particle (represented as a circle), would touch the edge of another particle (or the walls of a container) if the particles were allowed to continue with their current headings and speeds.

By performing such a calculation, one can determine when the next collision anywhere in the system would occur in time.  From this determination, the model then advances the motion of all the particles using their current headings and speeds that far in time until this next collision point is reached.  Exchange of kinetic energy and momentum between the two particles, according to conservation of kinetic energy and conservation of momentum along the collision axis (a line drawn between the centers of the two particles), is then calculated, and the particles are given new headings and speeds based on this outcome.

## HOW TO USE IT

NUMBER determines the number of gas particles used with SETUP.  If the world is too small or the particles are too large, the SETUP procedure of the particles will stop so as to prevent overlapping particles.

SMALLEST-PARTICLE-SIZE and LARGEST-PARTICLE-SIZE determines the range of particle sizes that will be created when SETUP is pressed.  (Particles are also assigned a mass proportional to the area of the particle that is created.)

The SETUP button will set the initial conditions.  
The GO button will run the simulation.

Monitors:  
- FAST, MEDIUM, SLOW: numbers of particles with different speeds: fast (red), medium (green), and slow (blue).  
- AVERAGE SPEED: average speed of the particles.  
- AVERAGE ENERGY: average kinetic energy of the particles.

Plots:  
- SPEED COUNTS: plots the number of particles in each range of speed.  
- SPEED HISTOGRAM: speed distribution of all the particles.  The gray line is the average value, and the black line is the initial average.  
- ENERGY HISTOGRAM: distribution of energies of all the particles, calculated as  m*(v^2)/2.  The gray line is the average value, and the black line is the initial average.

Initially, all the particles have the same speed but random directions. Therefore the first histogram plots of speed will show only one column.  If all the particles have the same size (and therefore the same mass), then the first histogram plot of energy will also show one column.  As the particles repeatedly collide, they exchange energy and head off in new directions, and the speeds are dispersed -- some particles get faster, some get slower.  The histogram distribution changes accordingly.

## THINGS TO NOTICE

Run the model with different masses, by setting the MAX-PARTICLE-SIZE larger than the MIN-PARTICLE-SIZE.  Mass is scaled linearly to the area of the particle, so that a particle that is twice the radius of another particle, has four the area and therefore four times the mass.

With many different mass particles colliding over time, different sized particles start to move at different speed ranges (in general).  The smallest mass particles will be usually moving faster (red) than the average particle speed and the largest mass particles will be usually slower (blue) than the average particle speed.  This emergent result is what happens in a gas that is a mixture of particles of different masses.  At any given temperature, the higher mass particles are moving slower (such as Nitrogen gas: N<sub>2</sub>) then the lower mass particles (such as water vapor: H<sub>2</sub>O).

The particle histograms quickly converge on the classic Maxwell-Boltzmann distribution.  What's special about these curves?  Why is the shape of the energy curve not the same as the speed curve?

Look at the other GasLab models to compare the particle histograms.  In those models, the particles are modeled as "point" particles, with an area of "zero".  The results of those models should match this model, even with that simplification.

With particles of different sizes, you may notice some fast moving particles have lower energy than medium speed particles.  How can the difference in the mass of the particles account for this?

## THINGS TO TRY

Setting all the particles to have a very slow speed (e.g. 0.001) and one particle to have a very fast speed helps show how kinetic energy is eventually transferred to all the particles through a series of collisions and would serve as a good model for energy exchange through conduction between hot and cold gases.

To see what the approximate mass of each particle is, type this in the command center:

    ask particles [ set label precision mass 0 ]

## EXTENDING THE MODEL

Collisions between boxes and circles could also be explored.  Variations in size between particles could investigated or variations in the mass of some of the particle could be made to explore other factors that affect the outcome of collisions.

## NETLOGO FEATURES

Instead of advancing one tick at a time as in most models, the tick counter takes on fractional values, using the `tick-advance` primitive.  (In the Interface tab, it is displayed as an integer, but if you make a monitor for `ticks` you'll see the exact value.)

## RELATED MODELS

Look at the other GasLab models to see collisions of "point" particles, that is, the particles are assumed to have an area or volume of zero.

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
Circle -7500403 true true 1 1 298

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

vector
true
0
Line -7500403 true 150 15 150 150
Polygon -7500403 true true 120 30 150 0 180 30 120 30

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

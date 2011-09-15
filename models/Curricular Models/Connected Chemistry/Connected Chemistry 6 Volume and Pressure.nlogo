globals
[
  tick-delta                 ;; how much we advance the tick counter this time through
  max-tick-delta             ;; the largest tick-delta is allowed to be
  box-edge                   ;; distance of box edge from axes
  instant-pressure           ;; the pressure at this tick or instant in time
  pressure-history           ;; a history of the four instant-pressure values
  pressure                   ;; the pressure average of the pressure-history (for curve smoothing in the pressure plots)
  volume
  collisions?
  maxparticles
  total-particle-number
  wall-hits-per-particle     ;; average number of wall hits per particle
  particles-to-add
  new-particles              ;; agentset of particles added via add-particles-middle
  delta-horizontal-surface   ;; the size of the wall surfaces that run horizontally - the top and bottom of the box
  delta-vertical-surface     ;; the size of the wall surfaces that run vertically - the left and right of the box
  piston-position            ;; xcor of piston wall
  piston-color               ;; color of piston
  wall-color                 ;; color of wall
  run-go?                    ;; flag of whether or not its safe for go to run.  Used for resuming after MOVE WALL
  temp-volume
  temp-target-wall           ;; location of where the wall will be moved to
  show-dark?                 ;; hides or shows the dark particles in the simulation.
                             ;; see NetLogo features Info tab for full explanation of
                             ;; what dark particles are and why they are used.
]

breed [ particles particle ]
breed [ dark-particles dark-particle ]
breed [ flashes flash ]
breed [ volume-target the-target ]      ;; used for the cursor that appears at the mouse coordinates in the
                             ;; WORLD & VIEW, when using MOVE WALL


flashes-own [birthday]

particles-own
[
  speed mass                 ;; particle info
  wall-hits                  ;; # of wall hits during this ticks cycle ("big tick")
  momentum-difference        ;; used to calculate pressure from wall hits
  momentum-instant           ;; used to calculate pressure
  last-collision             ;; keeps track of last particle this particle collided with
]

dark-particles-own
[
  speed mass                 ;; particle info
  wall-hits                  ;; # of wall hits during this ticks cycle ("big tick")
  momentum-difference        ;; used to calculate pressure from wall hits
  momentum-instant
  last-collision             ;; keeps track of last particle this particle collided with
]

to setup
  ca reset-ticks
  set show-dark? false
  set maxparticles 500
  set run-go? true
  set-default-shape particles "circle"
  set-default-shape dark-particles "nothing"
  set labels? false
  set particles-to-add 0
  ;; box has constant size...
  set box-edge (max-pycor - 1)
  ;;; the delta of the horizontal or vertical surface of
  ;;; the inside of the box must exclude the two patches
  ;; that are the where the perpendicular walls join it,
  ;;; but must also add in the axes as an additional patch
  ;;; example:  a box with an box-edge of 10, is drawn with
  ;;; 19 patches of wall space on the inside of the box
  set piston-position initial-wall-position - box-edge
  set piston-color orange
  set wall-color yellow
  set delta-horizontal-surface  ( 2 * (box-edge - 1) + 1) - (abs (piston-position - box-edge))
  set delta-vertical-surface  ( 2 * (box-edge - 1) + 1)
  make-box
  draw-piston
  make-particles maxparticles
  set pressure-history [0 0 0]  ;; plotted pressure will be averaged over the past 3 entries
  set volume (delta-horizontal-surface * delta-vertical-surface * 1)  ;;depth of 1
  set total-particle-number (count particles)
  setup-plot
  do-plotting
  set collisions? true
    ifelse labels?
      [turn-labels-on]
      [ask turtles [ set label ""]]

  create-volume-target 1 [set color white ht]  ;; cursor for targeting new location for volume using MOVE WALL
end

to go
  set total-particle-number (count particles)
  if not run-go? [stop]


    ask particles [ bounce ]
    ask particles [ move ]
    ask dark-particles[ bounce ]
    ask dark-particles[ move ]
    if collisions? [
    ask particles
      [ check-for-collision ]

    ask dark-particles
      [ check-for-collision ]
    ]
    tick-advance tick-delta

    calculate-instant-pressure

    if floor ticks > floor (ticks - tick-delta)
      [ ifelse any? particles
          [ set wall-hits-per-particle mean [wall-hits] of particles ]
          [ set wall-hits-per-particle 0 ]
        ask particles
          [ set wall-hits 0 ]
        calculate-pressure
        do-plotting ]
    calculate-tick-delta

    ask flashes with [ticks - birthday > 0.4]
      [ ifelse shade-of? pcolor wall-color
          [set pcolor wall-color]
          [set pcolor piston-color]
         die
      ]
    ifelse labels?
      [ask particles [ set label who set label-color orange + 3 ]]
      [ask particles [ set label ""]]
  ;; now recolor, since color is based on quantities that may have changed
    ask particles [  recolor ]
    display
end

to calculate-tick-delta
  ifelse any? particles with [speed > 0]
    [ set tick-delta 1 / (ceiling max [speed] of particles) ]
    [ set tick-delta 1 ]
end

;;; Pressure is defined as the force per unit area.  In this context,
;;; that means the total momentum per unit time transferred to the walls
;;; by particle hits, divided by the surface area of the walls.  (Here
;;; we're in a two dimensional world, so the "surface area" of the walls
;;; is just their delta.)  Each wall contributes a different amount
;;; to the total pressure in the box, based on the number of collisions, the
;;; direction of each collision, and the delta of the wall.  Conservation of momentum
;;; in hits ensures that the difference in momentum for the particles is equal to and
;;; opposite to that for the wall.  The force on each wall is the rate of change in
;;; momentum imparted to the wall, or the sum of change in momentum for each particle:
;;; F = SUM  [d(mv)/dt] = SUM [m(dv/dt)] = SUM [ ma ], in a direction perpendicular to
;;; the wall surface.  The pressure (P) on a given wall is the force (F) applied to that
;;; wall over its surface area.  The total pressure in the box is sum of each wall's
;;; pressure contribution.



to calculate-instant-pressure
  ;; by summing the momentum change for each particle,
  ;; the wall's total momentum change is calculated
  set instant-pressure 30 * 15 * sum [momentum-instant] of particles
  output-print precision instant-pressure 1
  ask particles
    [ set momentum-instant 0 ]  ;; once the contribution to momentum has been calculated
                                   ;; this value is reset to zero till the next wall hit
end


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
  let new-px 0
  let new-py 0

  ;; if we're not about to hit a wall (yellow patch), or if we're already on a
  ;; wall, we don't need to do any further checks
  if (shade-of? wall-color pcolor or not shade-of? wall-color [pcolor] of patch-at dx dy)
     and (shade-of? piston-color pcolor or not shade-of? piston-color [pcolor] of patch-at dx dy)
    [stop]
  ;; get the coordinates of the patch we'll be on if we go forward 1
  set new-px round (xcor + dx)
  set new-py round (ycor + dy)
  ;; if hitting left wall or piston (on right), reflect heading around x axis
  if ((abs new-px = box-edge or new-px = piston-position))
    [
      set heading (- heading)
      set wall-hits wall-hits + 1
  ;;  if the particle is hitting a vertical wall, only the horizontal component of the speed
  ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
  ;; due to the reversing of direction of travel from the collision with the wall
      set momentum-instant  (abs (dx * 2 * mass * speed) / delta-vertical-surface)
      set momentum-difference momentum-difference + momentum-instant
     ]
  ;; if hitting top or bottom wall, reflect heading around y axis
  if (abs new-py = box-edge)
    [ set heading (180 - heading)
      set wall-hits wall-hits + 1
  ;;  if the particle is hitting a horizontal wall, only the vertical component of the speed
  ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
  ;; due to the reversing of direction of travel from the collision with the wall
    set momentum-instant  (abs (dy * 2 * mass * speed) / delta-horizontal-surface)
    set momentum-difference momentum-difference + momentum-instant
    ]

      if ( breed = particles) [
      ask patch new-px new-py
    [ sprout 1 [ ht
                 set breed flashes
                 set birthday ticks
                 ifelse shade-of? ([pcolor] of patch-here) piston-color
                   [set pcolor piston-color - 3]
                   [set pcolor wall-color - 3] ]
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
  let others-here nobody

  ifelse( breed = particles )
  [ set others-here other particles-here ]
  [ set others-here other dark-particles-here ]

  if count others-here = 1
  [
    ;; the following conditions are imposed on collision candidates:
    ;;   1. they must have a lower who number than my own, because collision
    ;;      code is asymmetrical: it must always happen from the point of view
    ;;      of just one particle.
    ;;   2. they must not be the same particle that we last collided with on
    ;;      this patch, so that we have a chance to leave the patch after we've
    ;;      collided with someone.
    let candidate one-of others-here with
      [ who < [who] of myself and myself != last-collision ]
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
  let mass2 0
  let speed2 0
  let heading2 0
  let theta 0
  let v1t 0
  let v1l 0
  let v2t 0
  let v2l 0
  let vcm 0




  ;;; PHASE 1: initial setup

  ;; for convenience, grab some quantities from other-particle
  set mass2 [mass] of other-particle
  set speed2 [speed] of other-particle
  set heading2 [heading] of other-particle

  ;; since particles are modeled as zero-size points, theta isn't meaningfully
  ;; defined. we can assign it randomly without affecting the model's outcome.
  set theta (random-float 360)



  ;;; PHASE 2: convert velocities to theta-based vector representation

  ;; now convert my velocity from speed/heading representation to components
  ;; along theta and perpendicular to theta
  set v1t (speed * cos (theta - heading))
  set v1l (speed * sin (theta - heading))

  ;; do the same for other-particle
  set v2t (speed2 * cos (theta - heading2))
  set v2l (speed2 * sin (theta - heading2))



  ;;; PHASE 3: manipulate vectors to implement collision

  ;; compute the velocity of the system's center of mass along theta
  set vcm (((mass * v1t) + (mass2 * v2t)) / (mass + mass2) )

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
  if v1l != 0 or v1t != 0
    [ set heading (theta - (atan v1l v1t)) ]

  ;; and do the same for other-particle
  ask other-particle [
    set speed sqrt ((v2t ^ 2) + (v2l ^ 2))
    if v2l != 0 or v2t != 0
      [ set heading (theta - (atan v2l v2t)) ]
  ]

  ;; PHASE 5: final updates

  ;; now recolor, since color is based on quantities that may have changed
  recolor
end


;;;
;;; visualization procedures
;;;

to recolor
    if show-speed-as-color? = "red-green-blue" [ recolor-banded ]
    if show-speed-as-color? = "purple shades" [ recolor-shaded ]
    if show-speed-as-color?  = "one color" [ recolor-none ]
    if show-speed-as-color? = "custom color" [ ]
end

to recolor-banded  ;; particle procedure
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

to recolor-shaded
  ifelse speed < 27
  [ set color 111 + speed / 3 ]
  [ set color 119.999 ]
end

to recolor-none
  set color green - 1
end


to turn-labels-on
  ask turtles [ set label who set label-color orange + 3 ]
end

;;;
;;; drawing procedures
;;;

;; draws the box
to make-box
  ask patches with [ ((abs pxcor = box-edge) and (abs pycor <= box-edge)) or
                     ((abs pycor = box-edge) and (abs pxcor <= box-edge)) ]
    [ set pcolor wall-color ]
  ;;color the left side of the wall grey:
  ask patches with [ ((pxcor = ( box-edge)) and (abs pycor <= box-edge))]
    [set pcolor grey]
end

;; ------ Piston ----------
to move-piston
 set run-go? false
  if (mouse-down?)
   ;;note: if user clicks too far to the right, nothing will happen
   [ if (mouse-xcor >= piston-position and mouse-xcor < (box-edge))
      [ piston-out ceiling (mouse-xcor - piston-position) ]
     set run-go? true
      ask volume-target [ht]
     stop
   ]

    set temp-target-wall 0
   set temp-volume 0



    ifelse (mouse-xcor >= piston-position and mouse-xcor < box-edge - 2)
      [set temp-target-wall (( ( 2 * (box-edge - 1) + 1) - (abs (mouse-xcor - box-edge) - 1) )) ]  ;;depth of 1
      [ set temp-target-wall (2 * box-edge - 1) ]

        ifelse (mouse-xcor <= piston-position)
      [ set temp-volume volume ]
      [ set temp-volume (temp-target-wall * delta-vertical-surface * 1)]


      ask volume-target [st setxy mouse-xcor mouse-ycor set label (word "volume: " floor temp-volume)]

   if ((abs mouse-ycor > box-edge) or (abs mouse-xcor > box-edge)) [ask volume-target [ht set label ""]]
end

to piston-out [dist]
  if (dist > 0)
  [ ifelse ((piston-position + dist) < box-edge)
    [ undraw-piston
      set piston-position (piston-position + dist)
      draw-piston ]
    [ undraw-piston
      set piston-position (box-edge - 1)
      draw-piston ]
   set delta-horizontal-surface  ( 2 * (box-edge - 1) + 1) - (abs (piston-position - box-edge) - 1)
   set volume (delta-horizontal-surface * delta-vertical-surface * 1)   ;;depth of 1
  ]
end

to draw-piston
  ask patches with [ ((pxcor = (round piston-position)) and ((abs pycor) <= box-edge)) ]
    [ set pcolor piston-color ]
  ;; make sides of box that are to right right of the piston grey
  ask patches with [(pxcor > (round piston-position)) and (abs (pxcor) < box-edge)
                    and ((abs pycor) = box-edge)]
    [set pcolor grey]
end

to undraw-piston
  ask patches with [ (pxcor = round piston-position) and ((abs pycor) < box-edge) ]
    [ set pcolor black ]
    ask patches with [ (pxcor = round piston-position) and ((abs pycor) = box-edge) ]
    [ set pcolor yellow ]
  ask patches with [(pxcor > (round piston-position)) and (abs (pxcor) < box-edge)
                    and ((abs pycor) = box-edge)]
    [set pcolor wall-color]
  ask flashes with [ (xcor = round piston-position) and ((abs ycor) < box-edge) ]
    [ die ]
end

;; creates initial particles
to make-particles [n]

  create-particles number
  [
    setup-particle
    set speed random-float 20
    random-position
    recolor
  ]

  create-dark-particles ( n - number )
  [
    setup-particle
    set speed random-float 20
    random-position
    if show-dark? [set shape "default" set color green ]
  ]

  set total-particle-number number

  calculate-tick-delta
end

to setup-particle  ;; particle procedure
  set speed 10
  set mass 1.0
  set last-collision nobody
  set wall-hits 0
  set momentum-difference 0
end

;; place particle at random location inside the box.
to random-position ;; particle procedure
  setxy ((1 - box-edge)  + random-float (box-edge + piston-position - 3))
        ((1 - box-edge) + random-float (2 * box-edge - 2))
end


;;;
;;; reporters
;;;


;;;
;;; plotting procedures
;;;


to setup-plot
  set-current-plot "Volume vs. Time"
  plotxy ticks precision volume 0
end

to do-plotting
  set-current-plot "Pressure vs. Time"
  if length pressure-history > 0
    [ plotxy ticks (mean pressure-history) ]

  set-current-plot "Volume vs. Time"
  if ticks > 1
    [ plotxy ticks precision volume 0 ]

  set-current-plot "Wall Hits per Particle"
  if ticks > 1
    [ plotxy ticks wall-hits-per-particle ]
end
@#$#@#$#@
GRAPHICS-WINDOW
212
10
490
277
33
29
4.0
1
10
1
1
1
0
1
1
1
-33
33
-29
29
1
1
1
ticks
30

BUTTON
140
44
211
77
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
1

BUTTON
140
11
211
44
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
9
11
140
44
number
number
0
400
100
1
1
NIL
HORIZONTAL

PLOT
492
10
695
155
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
"default" 1.0 0 -16777216 true "" ""

MONITOR
9
174
137
219
wall hits per particle
wall-hits-per-particle
2
1
11

PLOT
492
152
695
307
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
"default" 1.0 0 -16777216 true "" ""

SLIDER
257
277
472
310
initial-wall-position
initial-wall-position
6
56
38
1
1
NIL
HORIZONTAL

BUTTON
121
92
211
125
move wall
move-piston
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

MONITOR
65
124
123
169
NIL
volume
3
1
11

PLOT
492
154
695
307
Volume vs. Time
time
volume
0.0
20.0
0.0
150.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

CHOOSER
6
44
140
89
show-speed-as-color?
show-speed-as-color?
"red-green-blue" "purple shades" "one color" "custom color"
1

OUTPUT
125
143
211
173
12

TEXTBOX
142
125
197
143
pressure
11
0.0
0

SWITCH
9
91
112
124
labels?
labels?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

This model explores the relationship between the volume of a gas container and the pressure of a gas in that container.  This model is part of the "Connected Chemistry" curriculum http://ccl.northwestern.edu/curriculum/ConnectedChemistry/ which explore the behavior of gases.

Most of the models in the Connected Chemistry curriculum use the same basic rules for simulating the behavior of gases.  Each model highlights different features of how gas behavior is related to gas particle behavior.

In all of the models, gas particles are assumed to move and to collide, both with each other and with objects such as walls.

In this model, the gas container has an adjustable volume.  The number of particles can also be changed.  Temperature is held constant throughout the model.  In this model, students can also look at the relationship between the number of particles and pressure, as well as the volume of the gas container and pressure.  Alternatively, they can change both the number of particles and the volume of the gas container, and see how these combined changes affect pressure.  These models have been adapted from the model GasLab Pressure Box.

## HOW IT WORKS

Particles are modeled as perfectly elastic with no energy except their kinetic energy, due to their motion.  Collisions between particles are elastic.  Particles can be color-coded by speed with the SHOW-SPEED-AS-COLOR? chooser.  For example, selecting red-green-blue makes colors slow particles in blue, medium-speed particles in green, and fast particles in red.

The exact way two particles collide is as follows:
1. Two turtles "collide" when they find themselves on the same patch.
2. A random axis is chosen, as if they are two balls that hit each other and this axis is the line connecting their centers.
3. They exchange momentum and energy along that axis, according to the conservation of momentum and energy.  This calculation is done in the center of mass system.
4. Each turtle is assigned its new velocity, energy, and heading.
5. If a turtle finds itself on or very close to a wall of the container, it "bounces," reflecting its direction but keeping its speed.

## HOW TO USE IT

Buttons:
SETUP - sets up the initial conditions set on the sliders.
GO/STOP - runs and stops the model.
MOVE WALL - allows the user to move the orange wall to the right of its current location (so as to permit adiabatic free expansion of the gas), by clicking with the mouse in the view.  If the model is currently running (i.e., if GO/STOP is depressed), clicking on MOVE WALL will stop it.  To continue running the model after moving the wall, press GO/STOP again.

Sliders:
NUMBER - sets the number of gas particles in the box when the simulation starts.
INITIAL-WALL-POSITION helps adjust the initial volume by setting the location of the orange box wall.

Switches:
LABELS? turn particle id labels on or off

Choosers:
SHOW-SPEED-AS-COLOR? allows you to visualize particle speed using a color palette.
- The "blue-green-red" setting shows the lower half of the speeds of the starting population as blue, and the upper half as red.
- The "violet shades" setting shows a gradient from dark violet (slow) to light violet (fast).
- The "all green" setting shows all particles in green, regardless of speed.
- The "custom color" setting, referenced in the Pedagogica version of this model, allows the user to modify the color of one or more particles, without having to worry that the particles will be recolored with each tick of the clock (as is the case for the other color options).

Monitors:
CLOCK - number of clock cycles that GO has run.
VOLUME - the volume of the box.
    Volume is computed based on what it would be using the 3D view.  The can be visualized as the inner gas volume (yellow walls and orange wall) that is 1 patch width deep in the z direction.

PRESSURE - the total pressure in the box.
WALL HITS PER PARTICLE - the average number of wall hits in one clock tick

Plots:
- VOLUME VS. TIME: plots the volume of the gas container over time.  Volume is computed based on what it would be using the 3D view.  The can be visualized as the inner gas volume (yellow walls and orange wall) that is 1 patch width deep in the z direction.
- PRESSURE VS. TIME: plots the average gas pressure inside of the box over time.

1. Adjust the INITIAL-NUMBER and/or the INITIAL-WALL-POSITION slider.
2. Press the SETUP button
3. Press GO/STOP and observe what happens.
4. Press MOVE WALL.  The particle motion will pause momentarily.
5. Then move your cursor to a spot inside the WORLD & VIEW, to the right of the current position of the orange wall.  Click on this spot and the wall will move and the particle motion will resume.
6. Observe the relationship between the Volume vs. Time graph and Pressure vs. Time graph.

## THINGS TO NOTICE

When you move the wall, the faster particles move toward it first.  Does this type of diffusion occur in reality?

There are combinations of NUMBER of particles and volumes for the container that yield the same pressure.  What do you notice about the density of the gas particles in these situations:  e.g. double the number of particles and double the volume?

## THINGS TO TRY

Find a mathematical model that related volume to pressure by recording and graphing various volume and pressure values in the model.

What combination of volume and number of particles gives the highest pressure?

## EXTENDING THE MODEL

Add an external force on the orange wall and a mass for the wall.  Allow it to move related to the forces that are on it from particle hits and the external force.

Model two gas chambers side both filled with gases side by side, with a movable wall in between.

Add a heated/cooled wall on the left side of the model.  If you combine this with an external force on the orange wall, what happens to the motion of the wall if you alternatively heat and cool the left wall?

## NETLOGO FEATURES

The Connected Chemistry models include invisible dark particles (the "dark-particles" breed), which only interact with each other and the walls of the yellow box. The inclusion of dark particles ensures that the speed of simulation remains constant, regardless of the number of particles visible in the simulation.

For example, if a model is limited to a maximum of 400 particles, then when there are 10 visible particles, there are 390 dark particles and when there are 400 visible particles, there are 0 dark particles.  The total number of particles in both cases remains 400, and the computational load of calculating what each of these particles does (collides, bounces, etc...) is close to the same.  Without dark particles, it would seem that small numbers of particles are faster than large numbers of particles -- when in reality, it is simply a reflection of the computational load.  Such behavior would encourage student misconceptions related to particle behavior.

## RELATED MODELS

See GasLab Models
See other Connected Chemistry models.

## CREDITS AND REFERENCES
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

circle
false
0
Circle -7500403 true true 35 35 230

clocker
true
0
Polygon -5825686 true false 150 30 105 195 135 180 135 270 165 270 165 180 195 195

nothing
true
0

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

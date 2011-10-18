globals
[
  tick-delta                 ;; how much we advance the tick counter this time through
  max-tick-delta             ;; the largest tick-delta is allowed to be
  box-edge                   ;; distance of box edge from axes
  delta-horizontal-surface   ;; the size of the wall surfaces that run horizontally - the top and bottom of the box
  delta-vertical-surface     ;; the size of the wall surfaces that run vertically - the left and right of the box
  total-particle-number
  labels?                    ;; show ID of particles
  collisions?
  instant-pressure           ;; the pressure at this tick or instant in time
  pressure-history           ;; a history of the four instant-pressure values
  pressure                   ;; the pressure average of the pressure-history (for curve smoothing in the pressure plots)
  wall-hits-per-particle     ;; average number of wall hits per particle
  maxparticles
  particles-to-add           ;; keeps track of particles to add after pressing ADD PARTICLES
  new-particles              ;; agentset of particles added via add-particles-middle
  show-dark?                 ;; hides or shows the dark particles in the simulation.
                             ;; see NetLogo features Info tab for full explanation of
                             ;; what dark-particles are and why they are used.
]

breed [ particles particle ]
breed [ flashes flash ]            ;; a breed which is used to mark the spot where a particle just hit the wall


flashes-own [birthday]       ;; flashes only last for a short period and then disappear.
                             ;; their birthday helps us keep track of when they were created and
                             ;; when we need to remove them.

particles-own
[
  speed mass                 ;; particle info
  wall-hits                  ;; # of wall hits during this ticks cycle
  momentum-difference        ;; used to calculate pressure from wall hits
  momentum-instant           ;; used to calculate pressure
  last-collision             ;; keeps track of last particle this particle collided with
  dark-particle?              ;; is this particle visible and interacting with visible particles?
]


to setup
  ca reset-ticks
  set pressure-history [0 0 0]  ;; plotted pressure will be averaged over the past 3 entries
  set show-dark? false
  set-default-shape particles "circle"
  set maxparticles 400
  set tick-delta 0
   ;; starting this at zero means that no particles will move until we've
   ;; calculated vsplit, which we won't even try to do until there are some
   ;; particles.
  set pressure 0
  set particles-to-add 0
  set labels? false

  ;; box has constant size...
  set box-edge (max-pxcor - 1)
   ;;; the delta of the horizontal or vertical surface of
   ;;; the inside of the box must exclude the two patches
   ;; that are the where the perpendicular walls join it,
   ;;; but must also add in the axes as an additional patch
   ;;; example:  a box with an box-edge of 10, is drawn with
   ;;; 19 patches of wall space on the inside of the box
  set delta-horizontal-surface  ( 2 * (box-edge - 1) + 1)
  set delta-vertical-surface  ( 2 * (box-edge - 1) + 1)
  draw-box
  set collisions? true
  make-particles maxparticles
  if labels? [turn-labels-on]

  reset-ticks
end


to go
  let old-ticks 0

    ask particles [ bounce ]
    ask particles [ move ]
    if collisions? [
    ask particles with [dark-particle? = false]
      [ check-for-collision-regular ]

    ask particles with [dark-particle? = true]
      [ check-for-collision-dark ]
    ]
    set old-ticks ticks
    tick-advance tick-delta
    calculate-instant-pressure

    if floor ticks > floor (ticks - tick-delta) [
       ifelse any? particles with [dark-particle? = false]
          [ set wall-hits-per-particle mean [wall-hits] of particles with [dark-particle? = false] ]
          [ set wall-hits-per-particle 0 ]
       ask particles  [ set wall-hits 0 ]
       calculate-pressure

       update-plots
       ]
    calculate-tick-delta
    ask flashes with [ticks - birthday > 0.4] [
      set pcolor yellow
      die
    ]

    ifelse labels?
      [turn-labels-on]
      [ask turtles [ set label ""]]

   ;; now recolor, since color is based on quantities that may have changed

    if show-speed-as-color? = "red-green-blue" [ask particles  [recolor]]
    if show-speed-as-color? = "purple shades" [ask particles  [recolorshade]]
    if show-speed-as-color? = "one color" [ask particles  [recolornone]]
    if (show-wall-hits? = false) [ ask flashes [ die ]]
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
  set instant-pressure 15 * sum [momentum-instant] of particles with [dark-particle? = false]
  output-print precision instant-pressure 1
  ask particles
    [ set momentum-instant 0 ]  ;; once the contribution to momentum has been calculated
                                   ;; this value is reset to zero till the next wall hit

end


to calculate-pressure
  ;; by summing the momentum change for each particle,
  ;; the wall's total momentum change is calculated

  set pressure 15 * sum [momentum-difference] of particles with [dark-particle? = false]
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
  if shade-of? yellow pcolor or not shade-of? yellow [pcolor] of patch-at dx dy
    [ stop ]
  ;; get the coordinates of the patch we'll be on if we go forward 1
  set new-px round (xcor + dx)
  set new-py round (ycor + dy)
  ;; if hitting left or right wall, reflect heading around x axis
  if (abs new-px = box-edge)
    [ set heading (- heading)
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
    set momentum-difference momentum-difference + momentum-instant  ]


  if (dark-particle? = false) [
  ask patch new-px new-py
    [ sprout 1 [ ht
                 set breed flashes
                 set birthday ticks
                 set pcolor yellow - 3 ] ]
      ]
end

to move  ;; particle procedure
  if patch-ahead (speed * tick-delta) != patch-here
    [ set last-collision nobody ]
  jump (speed * tick-delta)
end

to check-for-collision-regular  ;; particle procedure
  let candidate 0


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

  if count other particles-here with [dark-particle? = false] = 1
  [
    ;; the following conditions are imposed on collision candidates:
    ;;   1. they must have a lower who number than my own, because collision
    ;;      code is asymmetrical: it must always happen from the point of view
    ;;      of just one particle.
    ;;   2. they must not be the same particle that we last collided with on
    ;;      this patch, so that we have a chance to leave the patch after we've
    ;;      collided with someone.
    set candidate one-of other particles-here with
      [who < [who] of myself and myself != last-collision and dark-particle? = false]
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




to check-for-collision-dark  ;; particle procedure
  let candidate 0


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

  if count other particles-here with [dark-particle? = true] = 1
  [
    ;; the following conditions are imposed on collision candidates:
    ;;   1. they must have a lower who number than my own, because collision
    ;;      code is asymmetrical: it must always happen from the point of view
    ;;      of just one particle.
    ;;   2. they must not be the same particle that we last collided with on
    ;;      this patch, so that we have a chance to leave the patch after we've
    ;;      collided with someone.
    set candidate one-of other particles-here with
      [who < [who] of myself and myself != last-collision and dark-particle? = true]
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
end

;;;
;;; drawing procedures
;;;

;; draws the box
to draw-box
  ask patches with [ ((abs pxcor = box-edge) and (abs pycor <= box-edge)) or
                     ((abs pycor = box-edge) and (abs pxcor <= box-edge)) ]
    [ set pcolor yellow ]
  ask patches with [pycor = 0 and pxcor < (1 - box-edge)]
  [
    set pcolor yellow - 5  ;; trick the bounce code so particles don't go into the inlet
    ask patch-at 0  1 [ set pcolor yellow ]
    ask patch-at 0 -1 [ set pcolor yellow ]
  ]
end


;;;
;;; particle setup and addition procedures
;;;

;; creates initial particles
to make-particles [number]
  create-particles number
  [
    setup-particle
    set speed random-float 20
    random-position
    set color red
  ]

  set total-particle-number initial-number

  ask particles with [who < initial-number]
    [
      set shape "circle"
      set dark-particle? false
       if show-speed-as-color? = "red-green-blue" [ recolor ]
      if show-speed-as-color? = "purple shades" [ recolorshade ]
      if show-speed-as-color? = "one color" [ recolornone ]
    ]

  calculate-tick-delta
end


;; adds particles from the left (the valve from a pump)
to add-particles-side

       set particles-to-add number-to-add
  ifelse ((particles-to-add + total-particle-number ) > maxparticles)
    [user-message (word "The maximum number of particles allowed in this model is " maxparticles ".  You can not add " number-to-add
     " more particles to the " (count particles with [dark-particle? = false]) " you already have in the model")]
    [
      if particles-to-add > 0 [

       ask particles with [who < (total-particle-number + particles-to-add) and who >= total-particle-number]
        [ set dark-particle? false
          set shape "circle"
          setxy (- box-edge) 0
          set heading 90 ;; east
          rt 45 - random-float 90
          set speed 10
          if show-speed-as-color? = "red-green-blue" [ recolor ]
          if show-speed-as-color? = "purple shades" [ recolorshade ]
          if show-speed-as-color? = "one color" [ recolornone ]
        ]
      set total-particle-number (total-particle-number + particles-to-add)
      set particles-to-add 0

      calculate-tick-delta
      ]
     ]

end


to setup-particle  ;; particle procedure
  set speed 10
  set mass 1.0
  set last-collision nobody
  set wall-hits 0
  set momentum-difference 0
  set dark-particle? true
  ifelse show-dark?
    [set shape "default"]
    [set shape "nothing" set color green]
end


;; place particle at random location inside the box.
to random-position ;; particle procedure
  setxy ((1 - box-edge) + random-float ((2 * box-edge) - 2))
        ((1 - box-edge) + random-float ((2 * box-edge) - 2))
end


;;;
;;; visualization procedures
;;;

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

to recolorshade
  ifelse speed < 27
  [ set color 111 + speed / 3 ]
  [ set color 119.999 ]
end

to recolornone
  set color green - 1
end

to turn-labels-on
 ;; [ask turtles [ set label who set label-color orange + 3 ]]
end
@#$#@#$#@
GRAPHICS-WINDOW
212
10
487
306
26
26
5.0
1
10
1
1
1
0
1
1
1
-26
26
-26
26
1
1
1
ticks
30.0

BUTTON
140
42
212
75
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
10
212
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

PLOT
491
155
694
309
Number vs. Time
time
number
0.0
20.0
0.0
200.0
true
false
"set-plot-y-range 0 initial-number + 1" ""
PENS
"default" 1.0 0 -16777216 true "" "plotxy ticks (total-particle-number)"

SLIDER
2
78
125
111
number-to-add
number-to-add
0
100
50
1
1
NIL
HORIZONTAL

BUTTON
126
78
212
111
add particles
add-particles-side
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
2
10
139
43
initial-number
initial-number
0
100
50
1
1
NIL
HORIZONTAL

MONITOR
60
124
126
169
Number
count particles with [dark-particle? = false]
0
1
11

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
"default" 1.0 0 -16777216 true "" "if is-list? pressure-history [\n   plotxy ticks (mean pressure-history) \n  ]"

CHOOSER
3
177
137
222
show-speed-as-color?
show-speed-as-color?
"red-green-blue" "purple shades" "one color" "custom color"
2

OUTPUT
130
143
201
173
12

TEXTBOX
135
125
195
143
Pressure
11
0.0
0

SWITCH
2
44
139
77
show-wall-hits?
show-wall-hits?
1
1
-1000

MONITOR
51
226
209
271
average wall hits per particle
wall-hits-per-particle
2
1
11

PLOT
490
311
690
461
Avg. Wall Hits per particle
time
avg. wall hits
0.0
20.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "if ticks > 1 [ plotxy ticks wall-hits-per-particle ]"

@#$#@#$#@
## WHAT IS IT?

This model explores the relationship between the number of gas particles and the pressure of the gas in a container with a fixed volume.  This model is part of the "Connected Chemistry" curriculum http://ccl.northwestern.edu/curriculum/ConnectedChemistry/ which explore the behavior of gases.

Most of the models in the Connected Chemistry curriculum use the same basic rules for simulating the behavior of gases.  Each model highlights different features of how gas behavior is related to gas particle behavior.

In all of the models, gas particles are assumed to move and to collide, both with each other and with objects such as walls.

In this model, gas container (a bike tire represented by a box) has a fixed volume.  The number of particles can be varied initially (with the INITIAL-NUMBER slider) and by "pumping up the bike tire" by adding particles through a valve on the left wall of the box.

This model helps students study the representations of gas pressure in the model and the dynamics of the gas particles that lead to increases and decreases in pressure.
In this model, students can also look at the relationship between number of particles and pressure. In addition, one can follow the average number of wall hits in one model clock tick.  When the particles hit the walls the walls change their color temporarily.  These models have been adapted from the model GasLab Pressure Box.

These pressure models are part of a suite of models that students use to gain deeper insight on Gas Laws and particle behavior. In all of the Connected Chemistry Curriculum models, the same basic rules are used for expressing what happens when gas particles collide.  Each model has different features in order to show different aspects of the behavior of gases.

## HOW IT WORKS

Particles are modeled as perfectly elastic with no energy except their kinetic energy, due to their motion.  Collisions between particles are elastic.  Particles can be color-coded by speed with the SHOW-SPEED-AS-COLOR? chooser.  For example, selecting red-green-blue makes colors slow particles in blue, medium-speed particles in green, and fast particles in red.

The exact way two particles collide is as follows:
1. Two turtles "collide" when they find themselves on the same patch.
2. A random axis is chosen, as if they are two balls that hit each other and this axis is the line connecting their centers.
3. They exchange momentum and energy along that axis, according to the conservation of momentum and energy.  This calculation is done in the center of mass system.
4. Each turtle is assigned its new velocity, energy, and heading.
5. If a turtle finds itself on or very close to a wall of the container, it "bounces" -- that is, reflects its direction and keeps its same speed.

## HOW TO USE IT

Buttons:
SETUP - sets up the initial conditions set on the sliders.
GO/STOP - runs and stops the model.
ADD-PARTICLES - when pressed, releases NUMBER-TO-ADD particles into the box while the simulation is running.

Sliders:
INITIAL-NUMBER - sets the number of gas particles in the box when the simulation starts.
NUMBER-TO-ADD - the number of gas particles released into the box when the ADD-PARTICLES button is pressed.

Monitors:
CLOCK - number of clock cycles that GO has run.
NUMBER - the number of particles in the box.
PRESSURE - the total pressure in the box.
AVERAGE WALL HITS PER PARTICLE - the average number of wall hits in one clock tick

Plots:
PRESSURE VS TIME - plots the pressure in the box over time.
NUMBER VS TIME - plots the number of particles in the box over time.
AVG. WALL HITS PER PARTICLE - plots the average wall hits per particle over time.

Initially, the particles are not moving. Therefore the initial pressure is zero.  When the particles start moving, they repeatedly collide, exchange energy and head off in new directions, and the speeds are dispersed -- some particles get faster, some get slower.  When they hit the wall they change their heading, but not their speed.

1. Adjust the INITIAL-NUMBER slider.
2. Press the SETUP button
3. Press GO/STOP and observe what happens.
4. Adjust the NUMBER-TO-ADD slider.
5. Press the ADD PARTICLES button.
6. Observe the relationship between the Number vs. Time graph and Pressure vs. Time.

## THINGS TO NOTICE

Can you relate what you can see happening to the particles in the box with changes in pressure?

Why does the pressure change over time, even when the number of particles is the same?  How long does it take for the pressure to stabilize?

What happens to the wall hits per particle when particles are added to the box?

In what ways is this model an incorrect idealization of the real world?

What is the relationship between particle number and pressure?  Is it reciprocal, linear, quadratic, exponential?

Why is the average number of wall hits per particle, relatively constant, even if you change the number of particles in the model?

## THINGS TO TRY

Try different settings, especially the extremes.   Are the particles behaving in a similar way?  How does this affect the pressure?

You can pen-down a particle through the command center or by using the turtle menus.  What do you notice about a particle's path when there more and less particles in the box?

Build a mathematical model of number of particle vs. pressure, by recording and graphing data for various number and pressure combinations.

## EXTENDING THE MODEL

Add a way to adjust the volume of the container or the speed (or temperature of the particles).


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
Circle -7500403 true true 30 30 240

clocker
true
0
Polygon -5825686 true false 150 30 105 195 135 180 135 270 165 270 165 180 195 195

nothing
true
0

@#$#@#$#@
NetLogo 5.0beta3
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

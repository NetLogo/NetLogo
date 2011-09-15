globals
[
  tick-delta                      ;; how much we advance the tick counter this time through
  max-tick-delta                  ;; the largest tick-delta is allowed to be
  raw-width raw-height            ;; box size variables
  gravity                         ;; acceleration of the piston
  piston-energy total-energy      ;; current energies
  piston-height                   ;; piston variables
  piston-vel                      ;; piston speed
  pressure
  pressure-history
  length-horizontal-surface       ;; the size of the wall surfaces that run horizontally - the top and bottom of the box
  length-vertical-surface         ;; the size of the wall surfaces that run vertically - the left and right of the box
  init-avg-speed init-avg-energy  ;; initial averages
  avg-speed avg-energy            ;; current averages
  tot-particle-energy             ;; sum of the energy of all particles taken together
  piston-kinetic-energy           ;; piston's kinetic energy
  piston-potential-energy         ;; piston's potential energy
  fast medium slow                ;; current counts
]

breed [ particles particle ]
breed [ flashes flash ]
breed [ pistons piston]


flashes-own [birthday]

particles-own
[
  speed mass energy          ;; particle info
  momentum-difference        ;; used to calculate pressure from wall hits
  last-collision             ;; used to prevent particles from colliding multiple times

]

pistons-own
[
  speed mass energy
]

to setup
  clear-all
  set-default-shape particles "circle"
  set-default-shape flashes "plane"
  set max-tick-delta 0.1073
  ;; box has constant size.
  set raw-width  round (0.01 * box-width  * max-pxcor)
  set raw-height round (0.01 * box-height * max-pycor)
  set piston-height raw-height
  make-box
  make-piston
  set piston-vel 0
  set gravity 0.125
  ;;; the length of the horizontal or vertical surface of
  ;;; the inside of the box must exclude the two patches
  ;;; that are the where the perpendicular walls join it,
  ;;; but must also add in the axes as an additional patch
  ;;; example:  a box with an box-edge of 10, is drawn with
  ;;; 19 patches of wall space on the inside of the box
  set length-horizontal-surface  ( 2 * (raw-height - 1) + 1)
  set length-vertical-surface  (piston-height)
  make-box
  make-particles
  set pressure-history [0 0 0]  ;; plotted pressure will be averaged over the past 3 entries
  update-variables
  set init-avg-speed avg-speed
  set init-avg-energy avg-energy
  reset-ticks
end

to go
  if piston-height < 3
  [ user-message "The piston reached the bottom of the chamber. The simulation will stop."
    stop
  ]
  if piston-height >= 2 * raw-height - 1
  [ user-message "The piston reached the top of the chamber. The simulation will stop."
    stop
  ]
  ask particles [ bounce ]
  ask particles [ move ]
  ask particles [ check-for-collision ]
  move-piston
  tick-advance tick-delta
  if floor ticks > floor (ticks - tick-delta)
  [
    calculate-pressure
    update-variables
    update-plots
  ]
  calculate-tick-delta
  ;; we check for pcolor = black to make sure flashes that are left behind by the piston die
  ask flashes with [ticks - birthday > 0.4 or pcolor = black]
    [ die ]
  display
end

to update-variables
  ;; particle variables
  set medium count particles with [color = green]
  set slow   count particles with [color = blue]
  set fast   count particles with [color = red]
  set avg-speed  mean [speed] of particles
  set avg-energy mean [energy] of particles
  set tot-particle-energy sum [energy] of particles

  ;; piston Variables
  set piston-kinetic-energy (0.5 * piston-mass * (piston-vel ^ 2))
  set piston-potential-energy (piston-mass * gravity * piston-height)
  set piston-energy (piston-kinetic-energy + piston-potential-energy)

  ;; system Variables
  set total-energy (tot-particle-energy + piston-energy)
  set length-vertical-surface (piston-height)
  calculate-pressure
end

to calculate-tick-delta
  ;; tick-delta is calculated in such way that even the fastest
  ;; particle (or the piston) will jump at most 1 patch length in a
  ;; tick. As particles jump (speed * tick-delta) at every
  ;; tick, making tick length the inverse of the speed of the
  ;; fastest particle (1/max speed) assures that. Having each particle
  ;; advance at most one patch-length is necessary for them not to
  ;; "jump over" a wall or the piston.
  ifelse any? particles with [speed > 0]
    [ set tick-delta min list
                            (1 / (ceiling max (sentence ([speed] of particles) ([speed] of one-of pistons))))
                            max-tick-delta ]
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

to bounce  ;; particles procedure
  ;; get the coordinates of the patch we'll be on if we go forward 1
  let new-patch patch-ahead 1
  let new-px [pxcor] of new-patch
  let new-py [pycor] of new-patch
  ; if we're not about to hit a wall (yellow patch) or piston (orange patch)
  ; we don't need to do any further checks
  if ([pcolor] of new-patch != yellow and [pcolor] of new-patch != orange)
    [ stop ]

  ; check: hitting left or right wall?
  if (abs new-px = raw-width)
    ; if so, reflect heading around x axis
    [
      ;;  if the particle is hitting a vertical wall, only the horizontal component of the speed
      ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
      ;; due to the reversing of direction of travel from the collision with the wall
      set momentum-difference momentum-difference + (abs (dx * 2 * mass * speed) / length-vertical-surface)
      set heading (- heading) ]

  ; check: hitting top or bottom wall? (Should never hit top, but this would handle it.)
  if (abs new-py = raw-height)
  [
    ;;  if the particle is hitting a horizontal wall, only the vertical component of the speed
    ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
    ;; due to the reversing of direction of travel from the collision with the wall
    set momentum-difference momentum-difference + (abs (dy * 2 * mass * speed) / length-horizontal-surface)
    set heading (180 - heading)
  ]

  ; check: hitting piston?
  if (new-py = [pycor] of one-of pistons and (speed * dy) > piston-vel)
  [
    ;;  if the particle is hitting the piston, only the vertical component of the speed
    ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
    ;; due to the reversing of direction of travel from the collision with the wall
    ;; make sure that each particle finishes exchanging energy before any others can
    set momentum-difference momentum-difference + (abs (dy * 2 * mass * speed) / length-horizontal-surface)
    exchange-energy-with-piston
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

;;;
;;; drawing procedures
;;;

to make-box
  ask patches with [((abs pxcor = raw-width) and (abs pycor <= raw-height)) or
                    ((abs pycor = raw-height) and (abs pxcor <= raw-width))]
    [ set pcolor yellow ]
end

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
  set energy (0.5 * mass * (speed  ^ 2))
  set last-collision nobody
  set momentum-difference 0
end

;; place particle at random location inside the box.
to random-position  ;; particle procedure
  setxy ((1 - raw-width)  + random-float (2 * raw-width - 2))
        ((1 - raw-height) + random-float (raw-height - 2))
end


;; ------ Piston ----------
to make-piston
  ask patches with [pycor = 0 and (abs pxcor < raw-width)]
  [ sprout-pistons 1
    [ set color orange
      set heading 0
      set pcolor color
      ht
    ]
  ]
end

to move-piston
  let old-piston-vel piston-vel
  set piston-vel (old-piston-vel - gravity * tick-delta)    ;;apply gravity
  let movement-amount ((old-piston-vel * tick-delta) - (gravity * (0.5 * (tick-delta  ^ 2))))
  ;; Setting the pcolor makes the piston look like a wall to the particles.
  ask pistons
  [ set pcolor black
    while [(piston-vel * tick-delta) >= 1.0]
      [ calculate-tick-delta ]
    ifelse piston-height + movement-amount <= 2 * raw-height - 1
    [
      fd movement-amount
      set piston-height (raw-height + [ycor] of one-of pistons)
    ]
    [
      set ycor raw-height - 1
      set piston-height 2 * raw-height - 1
      if piston-vel > 0
        [ set piston-vel 0 ]
    ]
    set speed piston-vel ;; just used for tick-delta calculations
    set pcolor color
    if (piston-vel < 0) ;; piston can't hit particles when moving upwards
    [ if (any? particles-here with [(speed * dy) > piston-vel])
      [ ;; only bounce particles that are moving down slower than the piston
        ;; faster ones should outrun it
        ask particles-here with [(speed * dy) > piston-vel]
        [
          ;;  if the particle is hitting the piston, only the vertical component of the speed
          ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
          ;; due to the reversing of direction of travel from the collision with the wall
          ;; make sure that each particle finishes exchanging energy before any others can
          set momentum-difference momentum-difference + (abs (dy * 2 * mass * speed) / length-horizontal-surface)
          exchange-energy-with-piston
        ]
      ]
    ]
  ]
end


to exchange-energy-with-piston  ;; particle procedure -- piston and particle exchange energy
  let vx (speed * dx)         ;;only along x-axis
  let vy (speed * dy)         ;;only along y-axis
  let old-vy vy
  let old-piston-vel piston-vel
  set piston-vel ((((piston-mass - mass) / (piston-mass + mass)) * old-piston-vel) +
                  (((2 * mass) / (piston-mass + mass)) * old-vy))
  set vy ((((2 * piston-mass) / (piston-mass + mass)) * old-piston-vel) -
         (((piston-mass - mass) / (piston-mass + mass)) * old-vy))
  set speed (sqrt ((vx ^ 2) + (vy ^ 2)))
  set energy (0.5 * mass * (speed  ^ 2))
  set heading atan vx vy
end
@#$#@#$#@
GRAPHICS-WINDOW
294
10
628
365
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
95
10
290
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
651
171
889
350
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
16
386
122
431
average speed
avg-speed
1
1
11

MONITOR
16
439
123
484
average energy
avg-energy
1
1
11

SLIDER
8
108
183
141
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
9
156
181
189
particle-mass
particle-mass
1
20
1
1
1
NIL
HORIZONTAL

SLIDER
10
205
182
238
box-height
box-height
20
100
95
1
1
%
HORIZONTAL

SLIDER
10
253
182
286
box-width
box-width
20
100
80
1
1
%
HORIZONTAL

SLIDER
11
301
183
334
piston-mass
piston-mass
100
5000
3500
100
1
NIL
HORIZONTAL

PLOT
653
358
892
535
Energy vs. Time
time
energy
0.0
50.0
0.0
40000.0
true
true
"set-plot-y-range 0 max list 1 (tot-particle-energy * 2)" ""
PENS
"Total" 1.0 0 -16777216 true "" "plotxy ticks total-energy"
"Gas" 1.0 0 -2674135 true "" "plotxy ticks tot-particle-energy"
"Piston" 1.0 0 -13345367 true "" "plotxy ticks piston-energy"

PLOT
651
11
889
161
Piston Height vs. Time
time
height
0.0
50.0
0.0
70.0
true
false
"set-plot-y-range 0 (2 * raw-height)" ""
PENS
"height" 1.0 0 -13345367 true "" "plotxy ticks piston-height"

MONITOR
186
437
284
482
velocity
piston-vel
1
1
11

MONITOR
480
494
603
539
total system energy
total-energy
1
1
11

MONITOR
296
493
412
538
total energy
piston-energy
1
1
11

MONITOR
186
383
285
428
height
piston-height
1
1
11

MONITOR
16
493
122
538
total energy
tot-particle-energy
1
1
11

MONITOR
294
384
411
429
potential energy
piston-potential-energy
1
1
11

MONITOR
295
438
411
483
kinetic energy
piston-kinetic-energy
1
1
11

TEXTBOX
17
363
89
381
Particles
11
0.0
0

TEXTBOX
187
362
257
380
Piston
11
0.0
0

TEXTBOX
479
473
629
491
System
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

This model is one in a series of GasLab models. They use the same basic rules for simulating the behavior of gases.  Each model integrates different features in order to highlight different aspects of gas behavior.

The basic principle of the models is that gas particles are assumed to have two elementary actions: they move and they collide - either with other particles or with any other objects such as walls (see the model "GasLab Gas in a Box" for an introduction to the GasLab collection).

This particular model simulates the behavior of gas particles in a box with a movable piston. The piston has weight which pushes it down, and the gas particles push upward against the piston when they collide with it.

"Adiabatic" means "without loss or gain of heat".  In this model, no heat energy (such as heat loss through the walls of the box) is added to or removed from the system.

## HOW IT WORKS

The basic principle of all GasLab models is the following algorithm (for more details, see the model "GasLab Gas in a Box":

1) A particle moves in a straight line without changing its speed, unless it collides with another particle or bounces off the wall.  
2) Two particles "collide" if they find themselves on the same patch (NetLogo's View is composed of a grid of small squares called patches). In this model, two particles are aimed so that they will collide at the origin.  
3) An angle of collision for the particles is chosen, as if they were two solid balls that hit, and this angle describes the direction of the line connecting their centers.  
4) The particles exchange momentum and energy only along this line, conforming to the conservation of momentum and energy for elastic collisions.  
5) Each particle is assigned its new speed, heading and energy.  
6) If a particle finds itself on or very close to a wall of the container or the piston, it "bounces" --- that is, reflects its direction and keeps its same speed.

The piston has both potential energy (due to gravity) and kinetic energy (from its motion).

Each particle bounces off the sides and the bottom of the box without changing speed.  When it hits the piston, however, its speed does change.  If the piston is moving upward at that moment, the particle bounces off at a slightly smaller speed.  If the piston is moving downward, it gives the particle a kick and the particle speeds up.  This is the process by which the energy of the gas is changed by the motion of the piston.

The piston also changes speed with each collision.  The change is not large, because the piston is much heavier than each particle; but the accumulated effect of many particle collisions is enough to hold the piston up.

Gravity is incorporated in this model as a constant downwards acceleration on the piston.  In order to make the model simpler, this model doesn't include the effect of gravity on the particles.  See the "GasLab Atmosphere" and "GasLab Gravity Box" models if you are interested in the effect of gravity on the particles.

Pressure is calculated by adding up the momentum transferred to the walls of the box and the piston by the particles when they bounce off.  This is averaged over the surface area of the box to give the pressure.

## HOW TO USE IT

Initial settings:  
- NUMBER-OF-PARTICLES: number of gas particles.  
- INIT-PARTICLE-SPEED: initial speed of the particles.  
- PARTICLE-MASS: mass of each particle.  
- BOX-HEIGHT: height of the container (percentage of the world-height).  
- BOX-WIDTH: width of the container  (percentage of the world-width).  
- PISTON-MASS: mass of the piston, in the same "units" as the particle's mass.

The SETUP button will set the initial conditions.  
The GO button will run the simulation.

Other settings:  
- COLLIDE?: Turns collisions between particles on and off.

Monitors:  
- AVERAGE SPEED: average speed of the particles.  
- AVERAGE ENERGY: average kinetic energy per particle of the gas.  
- TOTAL ENERGY: total energy of the particles.  
- PISTON HEIGHT: piston's height above the bottom of the box.  
- PISTON VELOCITY: speed of the piston (up is positive).  
- PISTON POTENTIAL ENERGY: potential energy of the piston, due to gravity.  
- PISTON KINETIC ENERGY: kinetic energy of the piston, due to its motion.  
- PISTON TOTAL ENERGY: sum of potential and kinetic energy of the piston.  
- SYSTEM ENERGY: sum of particles' and the piston's total energy.

Plots:  
- PISTON HEIGHT VS. TIME: measured up from the bottom of the box.  
- PRESSURE VS. TIME: average pressure of the particles.  
- ENERGY OF PARTICLES, PISTON, AND TOTAL ENERGY: in terms of energy per particle.  The piston's energy is both kinetic (motion) and potential (height).

## THINGS TO NOTICE

Watch all the plots and notice how they change in relation to each other.

Does the piston reach an equilibrium position (as this might take a long time, so you could turn the display off to speed the process up)?  What is the pattern of its motion before that?  Why doesn't it keep oscillating, like a bouncing ball, if all of the collisions are elastic?

Would you expect that the pressure would settle at a stable value?  What would determine it?

The energy of the gas changes as the piston moves up and down.  How are the two related? Where does the energy come from and where does it go?

Can you infer what is happening to the temperature of the gas as the piston moves?

Explain in physical terms and in terms of the model's rules how the piston heats up the gas by pushing downward and cools it down when moving upward.

Gravity only affects the piston in this model.  Does this make sense?  If gravity were made to affect the particles as well would that significantly change the behavior of the model?  What if you were to think of the downwards acceleration of the piston as the atmospheric pressure pushing down from above the piston.  Would this make more sense?  Would you need to make any changes to the behavior of the model to have the force be atmospheric pressure instead of gravity?  Why or why not?

You can change the coloring of the particles while the model is running by moving the INIT-PARTICLE-SPEED slider.  This will change the meaning of the colors, but not the relative meanings of the colors or the behavior of the model.

## THINGS TO TRY

Change the initial particle mass and particle speed.  How do these variables affect the piston's motion and its equilibrium position?  Adjust the piston's mass to keep it inside the box.

Change the piston mass, leaving the gas alone.  What happens to all of the volume, pressure, and energy?  Note: if you do this while the model is running, the piston energy changes suddenly.  Why is this?

In this simulation, the piston and the particles exchange energy on every collision.  The model treats the wall collisions differently. Is this legitimate?  How is a piston different from a wall?

In this adiabatic system, neither pressure, volume, nor temperature are constant, so pressure and volume are not simply inversely proportional.  In fact it turns out that for two different states,

> (P'/P) = (V/V')^gamma,

where gamma depends on the number of degrees of freedom of the particles.   In this two-dimensional case, gamma = 2.  Confirm that this is roughly true by changing piston-mass (hence pressure) and noticing its effect on piston height (hence volume).

## EXTENDING THE MODEL

Add a heater in the box that changes the temperature of the gas.  What would happen if the gas were heated and nothing else were changed?

Combine this with the "Two Gas" model such that there are gases pushing on both sides of a piston, instead of gravity against a single gas.

Give the piston the ability to store thermal energy, so that it heats up instead of moving when the particles hit it.

## RELATED MODELS

Look at the other GasLab models, especially "GasLab Isothermal Piston" and "GasLab Moving Piston".

## CREDITS AND REFERENCES

Wilensky, U. (1999). GasLab--an Extensible Modeling Toolkit for Exploring Micro- and Macro- Views of Gases. In Roberts, N. , Feurzeig, W. & Hunter, B. (Eds.) Computer Modeling and Simulation in Science Education. Berlin: Springer Verlag.  (this is the best and most detailed source)

Wilensky, U. & Resnick, M. (1999). Thinking in Levels: A Dynamic Systems Perspective to Making Sense of the World. Journal of Science Education and Technology. Vol. 8 No. 1

Wilensky, U., Hazzard, E. & Froemke, R. (1999). An Extensible Modeling Toolkit for Exploring Statistical Mechanics Proceedings of the Seventh European Logo Conference - EUROLOGO'99, Sofia, Bulgaria.
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

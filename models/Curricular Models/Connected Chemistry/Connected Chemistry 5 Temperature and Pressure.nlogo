globals
[
  tick-delta                 ;; how much we advance the tick counter this time through
  max-tick-delta             ;; the largest tick-delta is allowed to be
  box-edge                   ;; distance of box edge from axes
  instant-pressure           ;; the pressure at this tick or instant in time
  pressure-history           ;; a history of the four instant-pressure values
  pressure                   ;; the pressure average of the pressure-history (for curve smoothing in the pressure plots)
  zero-pressure-count        ;; how many zero entries are in pressure-history
  wall-hits-per-particle     ;; average number of wall hits per particle
  length-horizontal-surface  ;; the size of the wall surfaces that run horizontally - the top and bottom of the box
  length-vertical-surface    ;; the size of the wall surfaces that run vertically - the left and right of the box
  walls                      ;; agentset containing patches that are the walls of the box
  maxparticles
  total-particle-number
  init-avg-speed init-avg-energy  ;; initial averages
  avg-speed avg-energy particle0-speed           ;; current averages
  outside-energy
  min-outside-energy
  max-outside-energy
  temp-increment
  volume
  temperature
  collide?
  show-dark?                 ;; hides or shows the dark particles in the simulation.
                             ;; see NetLogo features Info tab for full explanation of
                             ;; what dark-particles are and why they are used.
]

breed [ particles particle ]
breed [ dark-particles dark-particle ]
breed [ flashes flash ]

flashes-own [birthday]

particles-own
[
  speed mass energy          ;; particle info
  wall-hits                  ;; # of wall hits during this clock cycle ("big tick")
  momentum-difference        ;; used to calculate pressure from wall hits
  last-collision
  momentum-instant
]

dark-particles-own
[
  speed mass energy          ;; particle info
  wall-hits                  ;; # of wall hits during this clock cycle ("big tick")
  momentum-difference        ;; used to calculate pressure from wall hits
  last-collision
  momentum-instant
]

to setup
  ca reset-ticks
  set show-dark? false
  set collide? true
   set maxparticles 400
  set temp-increment 7.5
  set min-outside-energy 0
  set max-outside-energy 300
  set-default-shape particles "circle"
  set-default-shape dark-particles "nothing"
  set-default-shape flashes "square"
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
  set outside-energy 100
  make-box
  make-particles maxparticles
  set pressure-history [0 0 0]  ;; plotted pressure will be averaged over the past 3 entries
  set zero-pressure-count 0
  update-variables
  set temperature (avg-energy * 6)
  set init-avg-speed avg-speed

  ask particles with [ who = 0 ] [set particle0-speed speed]
  set init-avg-energy avg-energy

  do-plotting
  if speed-as-color? = "red-green-blue" [ ask particles [recolor-banded ] ]
  if speed-as-color? = "purple shades" [ ask particles [recolor-shaded ]]
  if speed-as-color? = "one color" [ ask particles [recolor-none ]]
  if speed-as-color? = "custom color" [ ]
end


to go
  ask walls
    [ set pcolor box-color ]
  ask particles [ bounce ]
  ask particles [ move ]
  ask dark-particles [ bounce-dark ]
  ask dark-particles [ move ]
  ask particles [ showlabel ]
  if collide?
  [
    ask particles
      [ check-for-collision ]

    ask dark-particles
      [ check-for-collision ]
  ]
  tick-advance tick-delta

  calculate-instant-pressure

  if floor ticks > floor (ticks - tick-delta)
  [
    ifelse any? particles
      [ set wall-hits-per-particle mean [wall-hits] of particles  ]
      [ set wall-hits-per-particle 0 ]
    ask particles
      [ set wall-hits 0 ]
    calculate-pressure
    update-variables
    do-plotting
  ]
  calculate-tick-delta
  ask particles with [ who = 0 ]
    [ set particle0-speed speed ]

  ask flashes with [ticks - birthday > 0.4]
    [ die ]

  if speed-as-color? = "red-green-blue"
    [ ask particles [recolor-banded ] ]
  if speed-as-color? = "blue shades"
    [ ask particles [recolor-shaded ] ]
  if speed-as-color? = "one color"
    [ ask particles [recolor-none ] ]
  set temperature (avg-energy * 6)
  display
end


to calculate-tick-delta
  ifelse any? particles with [ speed > 0 ]
    [ set tick-delta min list (1 / (ceiling max [speed] of particles )) max-tick-delta ]
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
    [ stop ]
  ;; if hitting left or right wall, reflect heading around x axis
  if (abs new-px = box-edge)
    [ set heading (- heading)
      set wall-hits wall-hits + 1
  ;;  if the particle is hitting a vertical wall, only the horizontal component of the speed
  ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
  ;; due to the reversing of direction of travel from the collision with the wall
      set momentum-instant  (abs (dx * 2 * mass * speed) / length-vertical-surface)
      set momentum-difference momentum-difference + momentum-instant
    ]
   ;; if hitting top or bottom wall, reflect heading around y axis
  if (abs new-py = box-edge)
    [ set heading (180 - heading)
      set wall-hits wall-hits + 1
  ;;  if the particle is hitting a horizontal wall, only the vertical component of the speed
  ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
  ;; due to the reversing of direction of travel from the collision with the wall
      set momentum-instant  (abs (dy * 2 * mass * speed) / length-horizontal-surface)
      set momentum-difference momentum-difference + momentum-instant
    ]
  if [heated-wall?] of patch new-px new-py   ;; check if the patch ahead of us is heated
    [ set energy ((energy +  outside-energy ) / 2)
      set speed sqrt (2 * energy / mass )
      recolor-banded
    ]


  ask patch new-px new-py
    [ sprout 1
      [ set breed flashes
        set color 11 ;;pcolor-of 11
        if (color < 10)
          [ set color 10 ]
        set birthday ticks
      ]
    ]

end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to bounce-dark  ;; particle procedure
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
      set momentum-instant  (abs (dx * 2 * mass * speed) / length-vertical-surface)
      set momentum-difference momentum-difference + momentum-instant
    ]
   ;; if hitting top or bottom wall, reflect heading around y axis
  if (abs new-py = box-edge)
    [ set heading (180 - heading)
      set wall-hits wall-hits + 1
  ;;  if the particle is hitting a horizontal wall, only the vertical component of the speed
  ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
  ;; due to the reversing of direction of travel from the collision with the wall
      set momentum-instant  (abs (dy * 2 * mass * speed) / length-horizontal-surface)
      set momentum-difference momentum-difference + momentum-instant
    ]
  if [heated-wall?] of patch new-px new-py   ;; check if the patch ahead of us is heated
    [ set energy ((energy +  outside-energy ) / 2)
      set speed sqrt (2 * energy / mass )
      recolor-banded
    ]


end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

  ifelse breed = particles
    [ set others-here other particles-here ]
    [ set others-here other dark-particles-here ]

  if count others-here = 1
    [ ;; the following conditions are imposed on collision candidates:
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
;;;
;;; visualization procedures
;;;

end



to update-variables
  set avg-speed  mean [speed] of particles
  set avg-energy mean [energy] of particles
end



;
to cool-walls
  ifelse ( outside-energy > 20 )
    [ set outside-energy outside-energy - temp-increment ]
    [ set outside-energy 0 ]
  if (outside-energy = 0)
    [ user-message "You are currently trying to cool the walls of the container below absolute zero (OK or -273C).  Absolute zero is the lowest theoretical temperature for all matter in the universe and has never been achieved in a real-world laboratory"]
end

to heat-walls
  set outside-energy outside-energy + temp-increment
    if (outside-energy > 300)
      [ set outside-energy 300
        user-message "You have reached the maximum allowable temperature for the walls of the container in this model."
      ]
end

;;
;;; visualization procedures
;;;


to showlabel
 ifelse labels?
   [ set label who
     set label-color white
     if (who = 0) [set label-color (orange + 1)]
   ]
   [ set label "" ]
end


to recolor-banded  ;; particle procedure
  ifelse speed < (0.5 * 10)
    [ set color blue ]
    [ ifelse speed > (1.5 * 10)
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
  ask turtles
    [ set label who
      set label-color orange + 3
    ]
end

;;;
;;; reporters
;;;



;; reports color of box according to temperature and position
;; if only one side is heated, the other walls will be yellow
to-report box-color
  ifelse heated-wall?
    [ report scale-color red outside-energy -60 340 ]
    [ report yellow ]
end

;; reports true if there is a heated wall at the given location
to-report heated-wall?
  if (( abs pxcor = box-edge) and (abs pycor <= box-edge)) or
     ((abs pycor = box-edge) and (abs pxcor <= box-edge))
    [ report true ]
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
to make-particles  [n]
  create-particles number
    [ setup-particle
      if speed-as-color? = "red-green-blue" [ recolor-banded ]
      if speed-as-color? = "blue shades" [ recolor-shaded ]
      if speed-as-color? = "one color" [ recolor-none ]
    ]

  create-dark-particles ( n - number )
    [ setup-particle
      set color red
      if show-dark? [set shape "default"]
    ]

  set total-particle-number number

  calculate-tick-delta
end


to setup-particle  ;; particle procedure
  set speed random-float 20
  set mass 1.0
  set energy (0.5 * mass * speed * speed)
  set last-collision nobody
  set wall-hits 0
  set momentum-difference 0
  random-position
end

;; place particle at random location inside the box.
to random-position ;; particle procedure
  setxy ((1 - box-edge) + random-float ((2 * box-edge) - 2))
        ((1 - box-edge) + random-float ((2 * box-edge) - 2))
end


;;; plotting procedures



to do-plotting
  set-current-plot "Pressure vs. Time"
  plotxy ticks (mean pressure-history)

  set-current-plot "Gas Temp. vs. Time"
  plotxy ticks avg-energy * 6

  set-current-plot "Average Speed vs. Time"
  plotxy ticks avg-speed
end


to calculate-instant-pressure
  ;; by summing the momentum change for each particle,
  ;; the wall's total momentum change is calculated
  set instant-pressure 15 * 30 * sum [momentum-instant] of particles
  output-print precision instant-pressure 1
  ask particles
    [ set momentum-instant 0 ]  ;; once the contribution to momentum has been calculated
                                   ;; this value is reset to zero till the next wall hit
end
@#$#@#$#@
GRAPHICS-WINDOW
232
10
510
309
33
33
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
-33
33
1
1
1
ticks
30

BUTTON
56
43
123
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
1

BUTTON
0
43
56
76
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
0
10
123
43
number
number
1
400
100
1
1
NIL
HORIZONTAL

PLOT
510
275
728
404
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
"default" 1.0 0 -955883 true "" ""

MONITOR
123
165
232
210
average speed
avg-speed
2
1
11

PLOT
510
10
728
140
Gas Temp. vs. Time
time
temp.
0.0
20.0
0.0
100.0
true
false
"" ""
PENS
"temperature" 1.0 0 -6459832 true "" ""

SWITCH
125
82
230
115
labels?
labels?
1
1
-1000

MONITOR
0
165
123
210
total kinetic energy
count particles * avg-energy
0
1
11

BUTTON
123
10
232
43
warm up walls
heat-walls
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
123
43
232
76
cool down walls
cool-walls
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
510
140
728
276
Average Speed vs. Time
time
avg. speed
0.0
20.0
0.0
12.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

CHOOSER
0
75
123
120
speed-as-color?
speed-as-color?
"red-green-blue" "blue shades" "one color" "custom color"
1

OUTPUT
131
135
223
165
12

TEXTBOX
150
116
215
134
pressure
11
0.0
0

MONITOR
0
120
123
165
gas temp.
temperature
1
1
11

@#$#@#$#@
## WHAT IS IT?

This model explores the relationship between the temperature of a gas and the pressure of a gas in a container with a fixed volume.  This model is part of the "Connected Chemistry" curriculum http://ccl.northwestern.edu/curriculum/ConnectedChemistry/ which explores the behavior of gases.

Most of the models in the Connected Chemistry curriculum use the same basic rules for simulating the behavior of gases.  Each model highlights different features of how gas behavior is related to gas particle behavior.

In all of the models, gas particles are assumed to move and to collide, both with each other and with objects such as walls.

In this model, the gas container (a bike tire represented by a yellow box) has a fixed volume. The number of particles can be varied initially and the temperature of the gas can be varied by warming and cooling the gas container walls.

This model helps students study the representations of gas pressure in the model and the dynamics of the gas particles that lead to increases and decreases in pressure.
In this model, students can also look at the relationship between the number of gas particles, the gas temperature, and the gas pressure.  Alternatively, they can make changes to both the number of particles and the temperature of the gas, and see what the combined affects of these changes are on pressure. In addition, one can follow the average number of wall hits in one model clock tick.  These models have been adapted from the model GasLab Pressure Box.

## HOW IT WORKS

The particles are modeled as hard balls with no internal energy except that which is due to their motion.  Collisions between particles are elastic.  Collisions between the wall are not.

1. A particle moves in a straight line without changing its speed, unless it collides with another particle or bounces off the wall.
2. Two particles "collide" if they find themselves on the same patch.In this model, two turtles are aimed so that they will collide at the origin.
3. An angle of collision for the particles is chosen, as if they were two solid balls that hit, and this angle describes the direction of the line connecting their centers.
4. The particles exchange momentum and energy only along this line, conforming to the conservation of momentum and energy for elastic collisions.
5. Each particle is assigned its new speed, heading and energy.

As the walls of the box are heated, the sides of the walls will change color from a deep red (cool) to a bright red, to pink to a pale pink white (hot).  The walls contain a constant heat value throughout the simulation.

The exact way particles gain energy from the walls of the box is as follows:
1. Particles check their state of energy (kinetic).
2. They hit or bounce off the wall.
3. They find wall energy and set their new energy to be the average of their old kinetic energy and the wall energy.
4. They change their speed and direction after the wall hit.

## HOW TO USE IT

Buttons:
SETUP - sets up the initial conditions set on the sliders.
GO/STOP - runs and stops the model.
WARM WALLS - incrementally warms the box walls each time it is pressed.
COOL WALLS - incrementally cools the box walls each time it is pressed.

Sliders:
NUMBER - sets the number of gas particles in the box when the simulation starts.

Choosers:
SHOW-SPEED-AS-COLOR? allows you to visualize particle speed using a color palette.
- The "blue-green-red" setting shows the lower half of the speeds of the starting population as blue, and the upper half as red.
- The "violet shades" setting shows a gradient from dark violet (slow) to light violet (fast).
- The "all green" setting shows all particles in green, regardless of speed.
- The "custom color" setting, referenced in the Pedagogica version of this model, allows the user to modify the color of one or more particles, without having to worry that the particles will be recolored with each tick of the clock (as is the case for the other color options).

Switches:
LABELS? turn particle id labels on or off.

Monitors:
CLOCK - number of clock cycles that GO has run.
PRESSURE - the total pressure in the box.
GAS TEMP. - the temperature of gas.
TOTAL KINETIC ENERGY - the total kinetic energy of the gas.
AVERAGE SPEED - the average speed of the gas particles.

Plots:
- TEMPERATURE VS. TIME: plots particle temperature inside the box over time.
- AVERAGE. SPEEDS VS. TIME: plots average speed of the gas particles inside the box over time over time.
- PRESSURE VS. TIME: plots the average gas pressure inside of the box over time.

1. Adjust the INITIAL-NUMBER slider.
2. Press the SETUP button
3. Press GO/STOP and observe what happens.
4. Wait until the gas temperature stabilizes.
5. Press WARM WALLS or COOL WALLS a few times.
5. Wait until the gas temperature stabilizes
6. Observe the relationship between the Temperature vs. Time graph and Average Speeds vs. Time and Pressure vs. Time.

## THINGS TO NOTICE

It takes a while for the gas temperature to stabilize after you press WARM WALLS or COOL WALLS.

Some particles move faster and slower than the average particles, at any gas temperature.

## THINGS TO TRY

Try to get the inside temperature to reach the outside temperature.  Is this possible?

How does adding heat to the box walls affect the speed of the particles?

What do you think temperature is a measure of in a gas?

How does adding heat to the box walls affect the pressure of the gas?

Why can't you stop the gas particles completely by cooling the walls?

How does the particle behavior or system response change with only one wall heated instead of all walls heated?

Does the system reach an equilibrium temperature faster when the wall is heated or cooled the same amount in comparison to the temperature of the particles?

## EXTENDING THE MODEL

Give the wall a mass and and see how that affects the behavior of the model.

Create two valves on either side to the wall that allow the user to "spurt" particles into the chambers to see how number of particles affects pressure.

Vary the width and length of the box, does this effect how fast the particle temperature changes?

What happens if you heat one wall and cool another wall?

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

clock
true
0
Circle -7500403 true true 30 30 240
Polygon -16777216 true false 150 31 128 75 143 75 143 150 158 150 158 75 173 75
Circle -16777216 true false 135 135 30

nothing
true
0

square
false
0
Rectangle -7500403 true true 0 0 297 299

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

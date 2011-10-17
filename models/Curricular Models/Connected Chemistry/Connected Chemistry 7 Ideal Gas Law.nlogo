globals
[
  tick-delta                  ;; how much we advance the tick counter this time through
  max-tick-delta              ;; the largest tick-delta is allowed to be
  box-edge-y                  ;; distance of box edge from axes
  box-edge
  instant-pressure           ;; the pressure at this tick or instant in time
  pressure-history           ;; a history of the four instant-pressure values
  pressure                   ;; the pressure average of the pressure-history (for curve smoothing in the pressure plots)
  particles-to-add
  zero-pressure-count        ;; how many zero entries are in pressure-history
  wall-hits-per-particle     ;; average number of wall hits per particle
  delta-horizontal-surface   ;; the size of the wall surfaces that run horizontally - the top and bottom of the box
  delta-vertical-surface     ;; the size of the wall surfaces that run vertically - the left and right of the box
  walls                      ;; agentset containing patches that are the walls of the box
  heated-walls
  piston-wall
  init-avg-speed init-avg-energy  ;; initial averages
  avg-speed avg-energy
  outside-energy
  min-outside-energy
  max-outside-energy
  temp-increment
  piston-position            ;; xcor of piston wall
  piston-color               ;; color of piston
  wall-color                 ;; color of wall
  run-go?                    ;; flag of whether or not its safe for go to run
  volume
  total-particle-number
  temperature
  temp-volume
  temp-target-wall
  maxparticles
  gravity-acceleration       ;; placeholder needed for logging in Modeling Across the Curriculum Activities

  show-dark?                 ;; hides or shows the dark particles in the simulation.
                             ;; see NetLogo features Info tab for full explanation of
                             ;; what dark particles are and why they are used.
]

breed [ particles particle ]
breed [ flashes flash ]
breed [ volume-target a-target ]
breed [ dark-particles dark-particle ]


flashes-own [birthday]

particles-own
[
  speed mass energy          ;; particle info
  wall-hits                  ;; # of wall hits during this ticks cycle ("big tick")
  momentum-difference        ;; used to calculate pressure from wall hits
  last-collision
  dark-particle?
   momentum-instant
]


dark-particles-own
[
  speed mass energy          ;; particle info
  wall-hits                  ;; # of wall hits during this ticks cycle ("big tick")
  momentum-difference        ;; used to calculate pressure from wall hits
  last-collision
  momentum-instant
]

to setup
  ca reset-ticks
  set maxparticles 400
  set run-go? true
  set show-dark? false
  set temp-increment 7.5
  set min-outside-energy 0
  set max-outside-energy 300
  set-default-shape particles "circle"
  set-default-shape flashes "square"
  set-default-shape dark-particles "nothing"
  set box-edge (max-pycor - 1)
  set particles-to-add 0
  set max-tick-delta 0.1073
  ;; box has constant size...
  set box-edge-y (max-pycor - 1)
  set box-edge (max-pxcor - 1)
  ;;; the delta of the horizontal or vertical surface of
  ;;; the inside of the box must exclude the two patches
  ;; that are the where the perpendicular walls join it,
  ;;; but must also add in the axes as an additional patch
  ;;; example:  a box with an box-edge of 10, is drawn with
  ;;; 19 patches of wall space on the inside of the box
  set piston-position init-wall-position - box-edge

  set delta-horizontal-surface  ( 2 * (box-edge - 1) + 1) - (abs (piston-position - box-edge))
  set delta-vertical-surface  ( 2 * (box-edge-y - 1) + 1)
  set volume (delta-horizontal-surface * delta-vertical-surface * 1)  ;;depth of 1
  set outside-energy 100
  set piston-color orange

  set wall-color (scale-color red outside-energy -60 340)
  draw-box-piston
  make-particles maxparticles
  set pressure-history [0 0 0]  ;; plotted pressure will be averaged over the past 3 entries
  set zero-pressure-count 0
  update-variables
  set init-avg-speed avg-speed
  set init-avg-energy avg-energy
  set temperature avg-energy * 6
  create-volume-target 1 [set color white ht]
  set total-particle-number initial-number
  reset-ticks
  recolor
end



to go
   if not run-go? [stop]

  set temperature avg-energy * 6
  ask particles [ bounce ]
  ask particles [ move ]
   ask particles [showlabel]
  if collide? [
    ask particles with [dark-particle? = false]
    [ check-for-collision]
    ask particles with [dark-particle? = true]
    [ check-for-collision-dark ]
  ]
  tick-advance tick-delta
  if floor ticks > floor (ticks - tick-delta)
  [
    ifelse any? particles
          [ set wall-hits-per-particle mean [wall-hits] of particles with [dark-particle? = false] ]
          [ set wall-hits-per-particle 0 ]
    ask particles
      [ set wall-hits 0 ]
    calculate-pressure
    update-variables
    update-plots
  ]
  calculate-tick-delta
  calculate-wall-color

    ask flashes with [ticks - birthday > 0.4]

      [ if ( shade-of? pcolor piston-color)
          [set pcolor piston-color]
        if ( shade-of? pcolor wall-color)
          [set pcolor wall-color]
         die
         ]
  ask patches with [pycor = 0 and pxcor < (1 - box-edge)]
  [
    set pcolor 10 ;; trick the bounce code so particles don't go into the inlet
  ]

    recolor
    display
end


to update-variables
  set avg-speed  mean [speed] of particles
  set avg-energy mean [energy] of particles
end



to cool
  ifelse ( outside-energy > 20 ) [ set outside-energy outside-energy - temp-increment ] [ set outside-energy 0 ]
  if (outside-energy = 0)
   [ user-message "You are currently trying to cool the walls of the container below absolute zero (OK or -273C).  Absolute zero is the lowest theoretical temperature for all matter in the universe and has never been achieved in a real-world laboratory"]
  calculate-wall-color
  ask heated-walls
      [set pcolor wall-color]
end

to heat
  set outside-energy outside-energy + temp-increment
    if (outside-energy > 300)
    [set outside-energy 300
    user-message "You have reached the maximum allowable temperature for the walls of the container in this model."]
    calculate-wall-color
    ask heated-walls
      [set pcolor wall-color]
end



to calculate-tick-delta
  ifelse any? particles with [speed > 0]
    [ set tick-delta min list (1 / (ceiling max [speed] of particles )) max-tick-delta ]
    [ set tick-delta max-tick-delta ]
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
  let new-patch 0
  let new-px 0
  let new-py 0

  ;; get the coordinates of the patch we'll be on if we go forward 1
  if (shade-of? black pcolor and shade-of? black [pcolor] of patch-at dx dy)
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
      set momentum-difference momentum-difference + (abs (dx * 2 * mass * speed) / delta-vertical-surface) ]
  ;; if hitting top or bottom wall, reflect heading around y axis
  if (abs new-py = box-edge-y)
    [ set heading (180 - heading)
      set wall-hits wall-hits + 1
  ;;  if the particle is hitting a horizontal wall, only the vertical component of the speed
  ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
  ;; due to the reversing of direction of travel from the collision with the wall
      set momentum-difference momentum-difference + (abs (dy * 2 * mass * speed) / delta-horizontal-surface)  ]

  if [heated-wall?] of patch new-px new-py  ;; check if the patch ahead of us is heated
  [
    set energy ((energy +  outside-energy ) / 2)
    set speed sqrt (2 * energy / mass )
    recolor
  ]
   if (dark-particle? = false) [
  ask patch new-px new-py
  [ sprout 1 [
                ht
                 set breed flashes
                 set birthday ticks
                 ifelse shade-of? ([pcolor] of patch-here) piston-color

                   [set pcolor piston-color - 3]
                   [set pcolor 11]
    ]   ]
  ]
end

to move  ;; particle procedure
  if patch-ahead (speed * tick-delta) != patch-here
    [ set last-collision nobody ]
  jump (speed * tick-delta)
end

to check-for-collision  ;; particle procedure
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
  set speed sqrt ((v1t ^ 2) + (v1l ^ 2))
  set energy (0.5 * mass * (speed ^ 2))
  ;; if the magnitude of the velocity vector is 0, atan is undefined. but
  ;; speed will be 0, so heading is irrelevant anyway. therefore, in that
  ;; case we'll just leave it unmodified.
  if v1l != 0 or v1t != 0
    [ set heading (theta - (atan v1l v1t)) ]

  ;; and do the same for other-particle
  ;; and do the same for other-particle
  ask other-particle [
    set speed sqrt ((v2t ^ 2) + (v2l ^ 2))
    set energy (0.5 * mass * (speed ^ 2))
    if v2l != 0 or v2t != 0
      [ set heading (theta - (atan v2l v2t)) ]
  ]



  ;; PHASE 5: final updates

  ;; now recolor, since color is based on quantities that may have changed
  ;;recolor
  ;;ask other-particle
  ;;  [ recolor ]
end


;;
;;; visualization procedures
;;;

to showlabel
 ifelse labels?
    [
      set label who
      set label-color white
      if (who = 0) [set label-color (orange + 1)]
    ]
    [set label ""]
end

to calculate-wall-color
  set wall-color (scale-color red outside-energy -60 340)
end

to recolor
   if speed-as-color? = "red-green-blue" [ ask particles [recolor-banded ] ]
    if speed-as-color? = "purple shades" [ ask particles [recolor-shaded ]]
    if speed-as-color? = "one color" [ ask particles [recolor-none ]]
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




;;;
;;; drawing procedures
;;;

;; draws the box
to make-box
  set heated-walls patches with [ ((pxcor = -1 * box-edge) and (abs pycor <= box-edge-y)) or
                     ((abs pycor = box-edge-y) and (pxcor <= piston-position) and (abs pxcor <= box-edge)) ]


  ask heated-walls  [ set pcolor white ]
  ;;color the left side of the wall grey:
  ask patches with [ ((pxcor = ( box-edge)) and (abs pycor <= box-edge-y))]
    [set pcolor grey]
end

;; creates initial particles
to make-particles [number]
  create-particles number
  [
    setup-particle
    set speed random-float 20
    set energy (0.5 * mass * speed * speed)
    random-position
     set dark-particle? true
  ]

  set total-particle-number initial-number

  ask particles with [who <= initial-number]
    [
      set dark-particle? false
      set shape "circle"
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
  set dark-particle? true
  ifelse show-dark? [set shape "default"][set shape "nothing" set color green]

end

;; place particle at random location inside the box.
to random-position ;; particle procedure
  setxy ((1 - box-edge)  + random-float (box-edge + piston-position - 3))
        ((1 - box-edge-y) + random-float (2 * box-edge-y - 2))
end



to add-particles
    set particles-to-add number-to-add

 ;;   show particles-to-add
 ;;   show total-particle-number
    ifelse ((particles-to-add + total-particle-number ) > maxparticles)
    [user-message (word "The maximum number of particles allowed in this model is " maxparticles ".  You can not add "  number-to-add
     " more particles to the " (count particles with [dark-particle? = false]) " you already have in the model")]
    [
    if number-to-add > 0 [
       ask particles with [who <= (total-particle-number + particles-to-add) and who >= total-particle-number and who <= maxparticles]
        [ set dark-particle? false
          set shape "circle"
          setxy (- box-edge + 1) 0
          set heading 90 ;; east
          rt 45 - random-float 90
          set speed 10
          recolor
        ]
      set total-particle-number (count particles with [dark-particle? = false])
      set particles-to-add 0

      calculate-tick-delta
      ]
    ]
end


;;
;; ------ Moveable wall procedures----------
;;

to move-piston

 set run-go? false
  if ((mouse-down?) and (mouse-ycor < (max-pycor - 1)))
   ;;note: if user clicks too far to the right, nothing will happen
   [ if (mouse-xcor >= piston-position and mouse-xcor < box-edge - 2)
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

   ask volume-target [st setxy mouse-xcor mouse-ycor set label (word "volume: "  floor temp-volume)]

   if (abs mouse-ycor > box-edge-y) [ask volume-target [ht set label ""]]
end

to piston-out [dist]
  if (dist > 0)
  [ ifelse ((piston-position + dist) < box-edge - 1)
    [ undraw-piston
      set piston-position (piston-position + dist)
      draw-box-piston ]
    [ undraw-piston
      set piston-position (box-edge - 1)
      draw-box-piston ]
   set delta-horizontal-surface  ( 2 * (box-edge - 1) + 1) - (abs (piston-position - box-edge) - 1)
   set volume (delta-horizontal-surface * delta-vertical-surface * 1)  ;;depth of 1
  ]
end



to draw-box-piston

  set heated-walls patches with [ ((pxcor = -1 * box-edge) and (abs pycor <= box-edge-y)) or
                     ((abs pycor = box-edge-y) and (pxcor <= piston-position) and (abs pxcor <= box-edge)) ]
  ask heated-walls  [ set pcolor wall-color ]

  set piston-wall patches with [ ((pxcor = (round piston-position)) and ((abs pycor) < box-edge-y)) ]
  ask piston-wall  [ set pcolor piston-color ]
  ;; make sides of box that are to right right of the piston grey

  ask patches with [pycor = 0 and pxcor < (1 - box-edge)]
  [
    set pcolor 10 ;; trick the bounce code so particles don't go into the inlet
    ask patch-at 0  1 [ set pcolor wall-color ]
    ask patch-at 0 -1 [ set pcolor wall-color ]
  ]

  ask patches with [(pxcor > (round piston-position)) and (abs (pxcor) < box-edge)
                    and ((abs pycor) = box-edge-y)]
    [set pcolor grey]
  ask patches with [ ((pxcor = ( box-edge)) and (abs pycor <= box-edge-y))]
    [set pcolor grey]
end

to undraw-piston
  ask patches with [ (pxcor = round piston-position) and ((abs pycor) < box-edge-y) ]
    [ set pcolor black ]

 ;; ask flashes with [ (xcor = round piston-position) and ((abs ycor) < box-edge) ]
 ;;   [ set color white ]

end



;; reports true if there is a heated wall at the given location
to-report heated-wall?

      if (( abs pxcor = -1 * box-edge) and (abs pycor <= box-edge-y)) or
                     ((abs pycor = box-edge-y) and (abs pxcor <= box-edge))
        [report true]
     ;]
     report false
end
@#$#@#$#@
GRAPHICS-WINDOW
1
122
415
277
50
15
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
-50
50
-15
15
1
1
1
ticks
30.0

BUTTON
56
22
127
55
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
23
56
56
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
55
127
88
initial-number
initial-number
1
200
100
1
1
NIL
HORIZONTAL

MONITOR
301
22
364
67
pressure
pressure
0
1
11

MONITOR
12
372
114
417
average speed
avg-speed
2
1
11

PLOT
440
379
662
499
1: Temperature vs. Time
time
temp.
0.0
100.0
0.0
100.0
true
false
"" ""
PENS
"temperature" 1.0 0 -2674135 true "" "plotxy ticks temperature"

SWITCH
119
323
221
356
collide?
collide?
0
1
-1000

SWITCH
223
323
313
356
labels?
labels?
1
1
-1000

MONITOR
113
372
216
417
average energy
avg-energy
2
1
11

MONITOR
216
372
318
417
total energy
count particles * avg-energy
0
1
11

SLIDER
0
88
127
121
number-to-add
number-to-add
0
200
50
5
1
NIL
HORIZONTAL

BUTTON
127
88
217
121
add particles
\nadd-particles
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
127
22
217
55
warm walls
heat
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
127
55
217
88
cool walls
cool
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

CHOOSER
2
322
120
367
speed-as-color?
speed-as-color?
"red-green-blue" "purple shades" "one color" "custom color"
0

SLIDER
21
278
405
311
init-wall-position
init-wall-position
4
96
20
1
1
NIL
HORIZONTAL

BUTTON
217
22
299
55
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
365
71
442
116
volume
volume
1
1
11

MONITOR
300
72
365
117
number
total-particle-number
3
1
11

MONITOR
365
22
442
67
temperature
temperature
0
1
11

PLOT
440
260
662
380
1: Volume vs. Time
time
volume
0.0
10.0
0.0
10.0
true
false
"plotxy 0 precision volume 0" ""
PENS
"default" 1.0 0 -955883 true "" "if ticks > 1 [ plotxy ticks volume ]"

PLOT
442
141
664
261
1: Number vs. Time
time
number
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 0 -10899396 true "" "plotxy ticks total-particle-number"

PLOT
443
22
665
142
1: Pressure vs. Time
time
pressure
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "if is-list? pressure-history \n  [ plotxy ticks (mean pressure-history) ]\n"

@#$#@#$#@
## WHAT IS IT?

This model explores the relationship between the variables in the ideal gas law (number of particles, container volume, gas pressure, and gas temperature).  This model is part of the "Connected Chemistry" curriculum http://ccl.northwestern.edu/curriculum/ConnectedChemistry/ which explore the behavior of gases.

Most of the models in the Connected Chemistry curriculum use the same basic rules for simulating the behavior of gases.  Each model highlights different features of how gas behavior is related to gas particle behavior.

In all of the models, gas particles are assumed to move and to collide, both with each other and with objects such as walls.

In this model, the gas container has an adjustable volume, adjustable number of particles, and adjustable temperature of the gas.

This model helps students study the representations of gas pressure in the model and the dynamics of the gas particles that lead to increases and decreases in pressure.
In this model, students can also look at all the relationships in the ideal gas law.

## HOW IT WORKS

The particles are modeled as hard balls with no internal energy except that which is due to their motion.  Collisions between particles are elastic.  Collisions with the wall are not.

Particles can be color-coded by speed with the SHOW-SPEED-AS-COLOR? chooser.  For example, selecting red-green-blue makes colors slow particles in blue, medium-speed particles in green, and fast particles in red.

The exact way two particles collide is as follows:
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
MOVE WALL -will temporarily "pause" the model when GO/STOP is running and wait until the user clicks in a new location in the World & View for the orange wall of the gas container.  The new location must be to the right of the current orange wall location (so as to permit adiabatic free expansion of the gas)
ADD-PARTICLES - when pressed releases particles into the box while the simulation is running.
WARM WALLS - incrementally warms the box walls each time it is pressed.
COOL WALLS - incrementally cools the box walls each time it is pressed.

Sliders:
INITIAL-NUMBER - sets the number of gas particles in the box when the simulation starts.
NUMBER-TO-ADD - the number of gas particles released into the box when the ADD-PARTICLES button is pressed.
INITIAL-WALL-POSITION helps adjust the initial volume by setting the location of the orange box wall.

Switches:
COLLIDE? turn particle collisions on or off
LABELS? turn particle id labels on or off

Choosers:
SHOW-SPEED-AS-COLOR? allows you to visualize particle speed using a color palette.
- The "blue-green-red" setting shows the lower half of the speeds of the starting population as blue, and the upper half as red.
- The "violet shades" setting shows a gradient from dark violet (slow) to light violet (fast).
- The "all green" setting shows all particles in green, regardless of speed.
- The "custom color" setting, referenced in the Pedagogica version of this model, allows the user to modify the color of one or more particles, without having to worry that the particles will be recolored with each tick of the clock (as is the case for the other color options).

Monitors:
CLOCK - number of clock cycles that GO has run.
PRESSURE - the total pressure in the box.
TEMPERATURE. - the temperature of gas.
VOLUME - the volume of the gas container.  Volume is computed based on what it would be using the 3D view.  The can be visualized as the inner gas volume (yellow walls and orange wall) that is 1 patch width deep in the z direction.
NUMBER - the number of gas particles in the container.
AVERAGE SPEED - the average speed of the gas particles.
TOTAL ENERGY - the total kinetic energy of the gas.
AVERAGE ENERGY - the average kinetic energy of the gas particles.

Plots:
- 1: TEMPERATURE VS. TIME: plots particle temperature inside the box over time.
- 1: NUMBER VS. TIME: plots the number of gas particles inside the box over time.
- 1: PRESSURE VS. TIME: plots the average gas pressure inside of the box over time.
- 1: VOLUME VS. TIME: plots the volume of the gas container over time.  Volume is computed based on what it would be using the 3D view.  The can be visualized as the inner gas volume (yellow walls and orange wall) that is 1 patch width deep in the z direction.

1. Adjust the INITIAL-NUMBER slider.
2. Press the SETUP button
3. Press GO/STOP and observe what happens.
4. Wait until the gas temperature stabilizes.
5. Press WARM WALLS or COOL WALLS a few times.
6. Wait until the gas temperature stabilizes
7. Press MOVE WALL.  The particle motion will pause momentarily.
8. Then move your cursor to a spot inside the WORLD & VIEW, to the right of the current position of the orange wall.  Click on this spot and the wall will move and the particle motion will resume.
8. Adjust the NUMBER-TO-ADD slider and press ADD PARTICLES.


## THINGS TO NOTICE

The ideal gas law relationships can be established in this model, with careful data gathering and mathematical modeling.

Why are there multiple combinations of volume, number of particles, and temperature of the gas that give the same pressure?

## THINGS TO TRY

What combination of variables gives the highest pressure?

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

globals
[
  tick-delta                        ;; how much we advance the tick counter this time through
  max-tick-delta                    ;; the largest tick-delta is allowed to be
  init-avg-speed init-avg-energy    ;; initial averages
  avg-speed avg-energy              ;; current average
  avg-energy-green
  avg-energy-orange
  avg-energy-purple
  particle-size
  toggle-red-state
  toggle-green-state
  min-particle-energy
  max-particle-energy
  particles-to-add
]

breed [ particles particle ]
breed [ walls wall ]
breed [ flashes flash ]
breed [ erasers eraser ]

breed [ arrowheads arrowhead ]

erasers-own [pressure?]

flashes-own [birthday]

particles-own
[
  speed mass energy          ;; particles info
  last-collision
  color-type
]

walls-own
[
  energy
  valve-1?
  valve-2?
  pressure?
  surface-energy
]

to setup
  ca reset-ticks
  set particle-size 1.0
  set max-tick-delta 0.02

  set particles-to-add 2

  set-default-shape flashes "square"
  set-default-shape walls "wall"
  set-default-shape erasers "eraser"
  set-default-shape arrowheads "default"

  set min-particle-energy 0
  set max-particle-energy 10000  ;;(.5 ) * ( max-dist-in-tick-delta  / max-tick-delta ) ^ 2

  create-erasers 1 [set hidden? true set pressure? true set size 3 set color white]

  make-box
  make-particles

  ask particles [ apply-speed-visualization]

  set init-avg-speed avg-speed
  set init-avg-energy avg-energy

  update-variables
  do-plotting
end


to go
  mouse-action
  if mouse-interaction = "none - let the particles interact"  [
  ask particles [ bounce ]
  ask particles [ move ]
  ask particles [ check-for-collision ]
  ask particles with [any? walls-here] [rewind-to-bounce]
  ask particles with [any? walls-here] [remove-from-walls]
]
  tick-advance tick-delta
  calculate-tick-delta

  ask flashes [apply-flash-visualization]
  ask particles [apply-speed-visualization]

  update-variables
  do-plotting
  display
end


to update-variables
  if any? particles [
    set avg-speed  mean [speed] of particles
    set avg-energy mean [energy] of particles
  ]

  if any? particles with [color-type = 55]
    [set avg-energy-green mean [energy] of particles with [color-type = 55]]
  if any? particles with [color-type = 25]
    [set avg-energy-orange mean [energy] of particles with [color-type = 25]]
 if any? particles with [color-type = 115]
      [set avg-energy-purple mean [energy] of particles with [color-type = 115]]


end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;WALL INTERACTION;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


to toggle-red-wall
  ifelse toggle-red-state = "closed"
     [ask walls with [valve-1?] [set hidden? true] set toggle-red-state "open"]
     [ask walls with [valve-1?] [set hidden? false] set toggle-red-state "closed"]
end

to toggle-green-wall
  ifelse toggle-green-state = "closed"
     [ask walls with [valve-2?] [set hidden? true] set toggle-green-state "open"]
     [ask walls with [valve-2?] [set hidden? false] set toggle-green-state "closed"]
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;GAS MOLECULES MOVEMENT;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to bounce  ;; particles procedure
  ;; get the coordinates of the patch we'll be on if we go forward 1
  let bounce-patch nobody
  let bounce-patches nobody
  let hit-angle 0
  let this-patch patch-here
  let new-px 0
  let new-py 0
  let visible-wall nobody

  set bounce-patch  min-one-of walls in-cone ((sqrt (2)) / 2) 180 with [myself != this-patch] [distance myself ]

  if bounce-patch != nobody [

    set new-px [pxcor] of bounce-patch
    set new-py [pycor] of bounce-patch
    set visible-wall walls-on bounce-patch

  if any? visible-wall with [not hidden?]  [
     set hit-angle towards bounce-patch
     ifelse (hit-angle <= 135 and hit-angle >= 45) or (hit-angle <= 315 and hit-angle >= 225)
       [set heading (- heading) ]
      [set heading (180 - heading) ]

       if show-wall-hits? [
    ask patch new-px new-py
    [ sprout 1 [
      set breed flashes
      set color gray - 2
      set birthday ticks
      ]
    ]
    ]
    ]]
end


to rewind-to-bounce  ;; particles procedure
  ;; attempts to deal with particle penetration by rewinding the particle path back to a point
  ;; where it is about to hit a wall
  ;; the particle path is reversed 49% of the previous tick-delta it made,
  ;; then particle collision with the wall is detected again.
  ;; and the particle bounces off the wall using the remaining 51% of the tick-delta.
  ;; this use of slightly more of the tick-delta for forward motion off the wall, helps
  ;; insure the particle doesn't get stuck inside the wall on the bounce.

  let bounce-patch nobody
  let bounce-patches nobody
  let hit-angle 0
  let this-patch nobody
  let new-px 0
  let new-py 0
  let visible-wall nobody

  bk (speed) * tick-delta * .49
  set this-patch  patch-here

  set bounce-patch  min-one-of walls in-cone ((sqrt (2)) / 2) 180 with [self != this-patch] [distance myself ]

  if bounce-patch != nobody [

    set new-px [pxcor] of bounce-patch
    set new-py [pycor] of bounce-patch
    set visible-wall walls-on bounce-patch


    if any? visible-wall with [not hidden?] [
      set hit-angle towards bounce-patch

      ifelse (hit-angle <= 135 and hit-angle >= 45) or (hit-angle <= 315 and hit-angle >= 225)
        [set heading (- heading) ]
        [set heading (180 - heading) ]

      if show-wall-hits? [
        ask patch new-px new-py
        [ sprout 1 [
            set breed flashes
            set color gray - 2
            set birthday ticks
          ]
        ]
      ]
    ]]
    fd (speed) * tick-delta * .51
end


to move  ;; particles procedure
  if patch-ahead (speed * tick-delta) != patch-here
    [ set last-collision nobody ]
  jump (speed * tick-delta)
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;GAS MOLECULES COLLISIONS;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;from GasLab

to calculate-tick-delta
  ;; tick-delta is calculated in such way that even the fastest
  ;; particles will jump at most 1 patch delta in a ticks tick. As
  ;; particles jump (speed * tick-delta) at every ticks tick, making
  ;; tick delta the inverse of the speed of the fastest particles
  ;; (1/max speed) assures that. Having each particles advance at most
   ; one patch-delta is necessary for it not to "jump over" a wall
   ; or another particles.
  ifelse any? particles with [speed > 0]
    [ set tick-delta min list (1 / (ceiling max [speed] of particles )) max-tick-delta ]
    [ set tick-delta max-tick-delta ]
end


to check-for-collision  ;; particles procedure
  ;; Here we impose a rule that collisions only take place when there
  ;; are exactly two particles per patch.  We do this because when the
  ;; student introduces new particles from the side, we want them to
  ;; form a uniform wavefront.
  ;;
  ;; Why do we want a uniform wavefront?  Because it is actually more
  ;; realistic.  (And also because the curriculum uses the uniform
  ;; wavefront to help teach the relationship between particles collisions,
  ;; wall hits, and pressure.)
  ;;
  ;; Why is it realistic to assume a uniform wavefront?  Because in reality,
  ;; whether a collision takes place would depend on the actual headings
  ;; of the particles, not merely on their proximity.  Since the particles
  ;; in the wavefront have identical speeds and near-identical headings,
  ;; in reality they would not collide.  So even though the two-particles
  ;; rule is not itself realistic, it produces a realistic result.  Also,
  ;; unless the number of particles is extremely large, it is very rare
  ;; for three or  particles to land on the same patch (for example,
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
  ;; because  collisions eventually do start occurring when it thins out while fanning.)

  if count  other particles-here  in-radius 1 = 1
  [
    ;; the following conditions are imposed on collision candidates:
    ;;   1. they must have a lower who number than my own, because collision
    ;;      code is asymmetrical: it must always happen from the point of view
    ;;      of just one particles.
    ;;   2. they must not be the same particles that we last collided with on
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

;; implements a collision with another particles.
;;
;; THIS IS THE HEART OF THE particles SIMULATION, AND YOU ARE STRONGLY ADVISED
;; NOT TO CHANGE IT UNLESS YOU REALLY UNDERSTAND WHAT YOU'RE DOING!
;;
;; The two particles colliding are self and other-particles, and while the
;; collision is performed from the point of view of self, both particles are
;; modified to reflect its effects. This is somewhat complicated, so I'll
;; give a general outline here:
;;   1. Do initial setup, and determine the heading between particles centers
;;      (call it theta).
;;   2. Convert the representation of the velocity of each particles from
;;      speed/heading to a theta-based vector whose first component is the
;;      particle's speed along theta, and whose second component is the speed
;;      perpendicular to theta.
;;   3. Modify the velocity vectors to reflect the effects of the collision.
;;      This involves:
;;        a. computing the velocity of the center of mass of the whole system
;;           along direction theta
;;        b. updating the along-theta components of the two velocity vectors.
;;   4. Convert from the theta-based vector representation of velocity back to
;;      the usual speed/heading representation for each particles.
;;   5. Perform final cleanup and update derived quantities.
to collide-with [ other-particles ] ;; particles procedure
  ;;; PHASE 1: initial setup

  ;; for convenience, grab  quantities from other-particles
  let mass2 [mass] of other-particles
  let speed2 [speed] of other-particles
  let heading2 [heading] of other-particles

  ;; since particles are modeled as zero-size points, theta isn't meaningfully
  ;; defined. we can assign it randomly without affecting the model's outcome.
  let theta (random-float 360)



  ;;; PHASE 2: convert velocities to theta-based vector representation

  ;; now convert my velocity from speed/heading representation to components
  ;; along theta and perpendicular to theta
  let v1t (speed * cos (theta - heading))
  let v1l (speed * sin (theta - heading))

  ;; do the same for other-particles
  let v2t (speed2 * cos (theta - heading2))
  let v2l (speed2 * sin (theta - heading2))



  ;;; PHASE 3: manipulate vectors to implement collision

  ;; compute the velocity of the system's center of mass along theta
  let vcm (((mass * v1t) + (mass2 * v2t)) / (mass + mass2) )

  ;; now compute the new velocity for each particles along direction theta.
  ;; velocity perpendicular to theta is unaffected by a collision along theta,
  ;; so the next two lines actually implement the collision itself, in the
  ;; sense that the effects of the collision are exactly the following changes
  ;; in particles velocity.
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
  ask other-particles [
    set speed sqrt ((v2t ^ 2) + (v2l ^ 2))
    set energy (0.5 * mass * (speed ^ 2))
    if v2l != 0 or v2t != 0
      [ set heading (theta - (atan v2l v2t)) ]
  ]
end



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  mouse interaction procedures
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


to mouse-action
   let snap-xcor 0
   let snap-ycor 0
   let orig-xcor 0
   let orig-ycor 0
   let eraser-window-walls nobody
   let eraser-window-particles nobody
   ifelse mouse-down? [
      set orig-xcor mouse-xcor
      set orig-ycor mouse-ycor
      set snap-xcor round orig-xcor
      set snap-ycor round orig-ycor

    ask patches with [pxcor = snap-xcor and pycor = snap-ycor] [
      set eraser-window-walls walls-on neighbors
      set eraser-window-walls eraser-window-walls with [not pressure?]
      set eraser-window-particles particles-on neighbors

      if mouse-interaction = "draw basic wall" [
        ask walls-here [die]
        sprout 1 [
          set breed walls set color gray
          initialize-this-wall
        ]
      ]

      if mouse-interaction = "draw red removable wall"  [
        set toggle-red-state "open"
        toggle-red-wall
        ask walls-here [die]
        sprout 1 [
          set breed walls set color red
          initialize-this-wall set valve-1? true
        ]
      ]

      if mouse-interaction = "draw green removable wall" [
        set toggle-green-state "open"
        toggle-green-wall
        ask walls-here [die]
        sprout 1 [
          set breed walls set color 55
          initialize-this-wall set valve-2? true
        ]
      ]


      if mouse-interaction = "big eraser" [
        ask erasers [
          set hidden? false
          set shape "eraser"
          setxy orig-xcor orig-ycor
        ]
        ask eraser-window-walls [die]
        ask eraser-window-particles [die]
      ]


      if mouse-interaction = "add purple particles"
      or mouse-interaction = "add green particles"
      or mouse-interaction = "add orange particles" [

        ask erasers [
          set hidden? false
          set shape "spray paint"
          setxy orig-xcor orig-ycor
        ]
        sprout particles-to-add [
          set breed particles
          setup-particles
          jump random-float 2
          if mouse-interaction = "add purple particles" [set color-type 115 color-particle-and-link]
          if mouse-interaction = "add orange particles" [set color-type 25 color-particle-and-link]
          if mouse-interaction = "add green particles" [set color-type 55  color-particle-and-link]
          apply-speed-visualization
        ]
      ]



      if mouse-interaction = "paint particles purple"
      or mouse-interaction = "paint particles orange"
      or mouse-interaction = "paint particles green" [

        ask erasers [
          set hidden? false
          set shape "spray paint"
          setxy orig-xcor orig-ycor
        ]
        ask eraser-window-particles [
          if mouse-interaction = "paint particles purple" [set color-type 115 color-particle-and-link]
          if mouse-interaction = "paint particles orange" [set color-type 25 color-particle-and-link]
          if mouse-interaction = "paint particles green" [set  color-type 55 color-particle-and-link]
          apply-speed-visualization
        ]
      ]


      if mouse-interaction = "speed up particles"  [

        ask erasers [
          set hidden? false
          set shape "spray paint"
          setxy orig-xcor orig-ycor
        ]
        ask eraser-window-particles [
          set energy (energy * 1.1)
          set energy limited-particle-energy
          set speed speed-from-energy
          apply-speed-visualization
        ]
      ]


      if mouse-interaction = "slow down particles" [

        ask erasers [
          set hidden? false
          set shape "spray paint"
          setxy orig-xcor orig-ycor
        ]
        ask eraser-window-particles [
          set energy (energy / 1.1)
          set energy limited-particle-energy
          set speed speed-from-energy
          apply-speed-visualization
        ]
      ]

    ]
    ask particles with [any? walls-here] [remove-from-walls] ;; deal with any walls drawn on top of particles
  ]
  [ask erasers [set hidden? true]]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; particle speed and flash visualization procedures
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to apply-flash-visualization
  set size (size * .95)
  if (ticks - birthday > 0.4)  [ die ]
end

to apply-speed-visualization
  if visualize-speed? = "arrows" [ scale-arrowheads]
  if visualize-speed? = "different shades" [ recolorshade ]
  if visualize-speed? = "none" [ recolornone ]

end


to color-particle-and-link

  let this-link my-out-links
  let this-color-type color-type
  set color this-color-type
  ask this-link [set color this-color-type]

end


to scale-arrowheads
  let this-xcor xcor
  let this-ycor ycor
  let this-speed speed
  let this-heading heading
  let this-arrowhead out-link-neighbors
  let this-link my-out-links
  ask this-link [set hidden? false]
  ask this-arrowhead [
    set xcor this-xcor
    set ycor this-ycor
    set heading this-heading
    fd .5 + this-speed / 3
  ]
end



to recolorshade
  let this-link my-out-links
  ask this-link [set hidden? true]
  ifelse speed < 27
  [ set color color-type - 3 + speed / 3 ]
  [ set color color-type + 4.999 ]
end

to recolornone
  let this-link my-out-links
  ask this-link [set hidden? true]
  set color 55
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  initialization procedures
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



to make-box
  ask patches with [(pycor = min-pycor or pycor = max-pycor or pxcor = min-pxcor or pxcor = max-pxcor) ]
    [ sprout 1 [set breed walls set color yellow initialize-this-wall set pressure? true] ]
end


to initialize-this-wall
  set valve-1? false
  set valve-2? false
  set pressure? false
end


to make-particles
  create-particles initial-#-particles
  [
    setup-particles
    random-position
  ]

end

to setup-particles  ;; particles procedure
  set shape "circle"
   set size particle-size
  set energy initial-gas-temperature
  set color-type 115
  set color color-type
  set mass (10)  ;; atomic masses of oxygen atoms
  hatch 1 [set breed arrowheads set hidden? true create-link-from myself [tie]]
  set speed speed-from-energy
  set last-collision nobody
end


;; Place particles at random, but they must not be placed on top of wall atoms.
;; This procedure takes into account the fact that wall molecules could have two possible arrangements,
;; i.e. high-surface area ot low-surface area.
to random-position ;; particles procedure
  let open-patches nobody
  let open-patch nobody
  set open-patches patches with [not any? turtles-here and pxcor != max-pxcor and pxcor != min-pxcor and pycor != min-pycor and pycor != max-pycor]
  set open-patch one-of open-patches

  ;; Reuven added the following "if" so that we can get through setup without a runtime error.
  if open-patch = nobody [
    user-message "No open patches found.  Exiting."
    stop
  ]

  setxy ([pxcor] of open-patch) ([pycor] of open-patch)
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; wall penetration error handling procedure
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; if particles actually end up within the wall
;;

to remove-from-walls
  let this-wall walls-here with [not hidden?]

  if count this-wall != 0 [

    let available-patches patches with [not any? walls-here]
    let closest-patch nobody
    if (any? available-patches) [
      set closest-patch min-one-of available-patches [distance myself]
      set heading towards closest-patch
      setxy ([pxcor] of closest-patch)  ([pycor] of closest-patch)

    ]
  ]
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;GRAPHS;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;




to do-plotting

  set-current-plot "Temperature of gases"

  if any? particles with [color-type = 55]
    [
    set-current-plot-pen "green"
    plotxy ticks avg-energy-green
  ]
  if any? particles with [color-type = 25]
  [
    set-current-plot-pen "orange"
    plotxy ticks avg-energy-orange
  ]
  if any? particles with [color-type = 115]
  [
    set-current-plot-pen "purple"
    plotxy ticks avg-energy-purple
  ]


end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;REPORTERS;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to-report speed-from-energy
   report sqrt (2 * energy / mass)
end

to-report energy-from-speed
   report (mass * speed * speed / 2)
end


to-report limited-particle-energy
  let limited-energy energy
  if limited-energy > max-particle-energy [set limited-energy max-particle-energy]
  if limited-energy < min-particle-energy [set limited-energy min-particle-energy]
  report limited-energy
end
@#$#@#$#@
GRAPHICS-WINDOW
320
10
726
437
16
16
12.0
1
10
1
1
1
0
0
0
1
-16
16
-16
16
1
1
1
ticks
30

BUTTON
90
10
165
43
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
10
10
85
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
10
45
315
78
initial-#-particles
initial-#-particles
0
100
50
1
1
NIL
HORIZONTAL

SLIDER
10
80
315
113
initial-gas-temperature
initial-gas-temperature
1
500
250
1
1
NIL
HORIZONTAL

PLOT
10
200
315
422
Temperature of gases
time
temp.
0.0
10.0
0.0
200.0
true
true
"" ""
PENS
"orange" 1.0 0 -955883 true "" ""
"purple" 1.0 0 -8630108 true "" ""
"green" 1.0 0 -10899396 true "" ""

CHOOSER
165
115
315
160
mouse-interaction
mouse-interaction
"none - let the particles interact" "draw basic wall" "draw red removable wall" "draw green removable wall" "big eraser" "slow down particles" "speed up particles" "paint particles purple" "paint particles green" "paint particles orange" "add green particles" "add purple particles" "add orange particles"
0

BUTTON
165
165
315
198
remove/replace green wall
toggle-green-wall
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
170
10
314
43
show-wall-hits?
show-wall-hits?
0
1
-1000

BUTTON
10
165
160
198
remove/replace red wall
toggle-red-wall
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
10
115
160
160
visualize-speed?
visualize-speed?
"none" "different shades" "arrows"
0

@#$#@#$#@
## WHAT IS IT?

This model supports a drawing style interface for "sketching" up representations of new systems to explore related to gas behavior and gas particles.  This model is part of the "Connected Chemistry" curriculum http://ccl.northwestern.edu/curriculum/ConnectedChemistry/ which explores the behavior of gases.

Most of the models in the Connected Chemistry curriculum use the same basic rules for simulating the behavior of gases.  Each model highlights different features of how gas behavior is related to gas particle behavior.

In all of the models, gas particles are assumed to move and to collide, both with each other and with objects such as walls.

In this model, particles can be added, color coded, and sped up or slowed down, by drawing with the mouse cursor in the WORLD & VIEW.  Also, additional types of removable and replaceable walls can be added to the WORLD.

This model enables students to draw a model of a real world system and then test that model.  A wide range of real world systems can be modeled with this simple interface (e.g. diffusion of perfume from an uncapped container, hot gas mixed with a cold gas, mixtures of gases).

## HOW IT WORKS

The particles are modeled as hard balls with no internal energy except that which is due to their motion.  Collisions between particles are elastic.  Collisions with the wall are not.

The exact way two particles collide is as follows:
1. A particle moves in a straight line without changing its speed, unless it collides with another particle or bounces off the wall.
2. Two particles "collide" if they find themselves on the same patch. In this model, two turtles are aimed so that they will collide at the origin.
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
MOUSE INTERACTION - when this is set to "none - let the particles interact" the particles will move and interact with each other and the surroundings.  When set to any other value you can then click in the WORLD & VIEW to paint, erase, color, or add various objects and properties.

Sliders:
INITIAL-#-PARTICLES - sets the number of gas particles in the box when the simulation starts.
INITIAL-GAS-TEMPERATURE sets the initial temperature of the gas.

Switches:
SHOW-WALL-HITS? turn visualization of when particles hits the walls (as flashes) on or off

Choosers:
VISUALIZE-SPEED? allows you to visualize particle speeds.  For example, selecting "arrows", creates a representation of each particle velocity using a scalar arrow.  Selecting "shades" creates representation of each particle speed using a brighter (faster) or darker (slower) shade of the particle's color.

MOUSE-INTERACTION  sets the type interaction the user can do with the mouse in the WORLD & VIEW.  Possible settings include:
"none - let the particles interact" - particles move about
"draw basic wall" - adds a gray wall under the mouse cursor
"draw red removable wall" - adds a red wall under the mouse cursor which can be alternatively removed and replaced (like a valve) using the REMOVE/REPLACE RED WALL.
"draw green removable wall" - adds a green wall under the mouse cursor which can be alternatively removed and replaced (like a valve) using the REMOVE/REPLACE GREEN WALL.
"big eraser" - erases all objects (except the yellow box boundary walls) under the mouse cursor.
"slow down particles" - increase the current speed of the particles by 10%.
"speed up particles" - reduces the current speed of the particles by 10%.
"paint particles green" - recolors the particles under the mouse cursor green (other settings include orange and purple)
"add green particles" - adds a couple of new particles under the mouse cursor (other settings include orange and purple)

Plots:
- 1: TEMPERATURE OF GASES VS. TIME: plots the temperature of the different gases in the model, as indicated by their color (orange particles, green particles, and purple particles)


## THINGS TO NOTICE

The mouse interaction can be used while the model is running as well as when it is stopped.

## THINGS TO TRY

Create a model of how odors move throughout a room.  Why do some people smell the odor before others?  Does the layout of furniture, large objects, and walls in the room effect the movement of the odor?  How about the temperature of the air in the room?

Create a model of diffusion of a perfume from a closed container.  How would you represent the different gases (the perfume and the surrounding air)?  What shape will the container be?  How will you model a removable cap or lid?

Create a model of room filled with cold air and a different room filled with warm air.  How will represent these different rooms of air?  What could you add to show what happens when they mix?

Create a model of heat transfer that shows what happens to the energy of one very fast moving gas particle when it hits a bunch of very slow moving gas particles.  What does this show happening to the energy of the initial gas particles?

## RELATED MODELS

See GasLab Models
See other Connected Chemistry models.

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

carbon
true
0
Circle -1184463 true false 68 83 134

carbon-activated
true
0
Circle -1184463 true false 68 83 134
Line -2674135 false 135 90 135 210

carbon2
true
0
Circle -955883 true false 30 45 210

circle
false
1
Circle -2674135 true true 30 30 240

circle 2
false
0
Circle -7500403 true true 16 16 270
Circle -16777216 true false 46 46 210

clock
true
0
Circle -7500403 true true 30 30 240
Polygon -16777216 true false 150 31 128 75 143 75 143 150 158 150 158 75 173 75
Circle -16777216 true false 135 135 30

co2
true
0
Circle -13791810 true false 83 165 134
Circle -13791810 true false 83 0 134
Circle -1184463 true false 83 83 134

cow
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

eraser
false
0
Rectangle -7500403 true true 0 0 300 300

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

heater-a
false
0
Rectangle -7500403 true true 0 0 300 300
Rectangle -16777216 true false 90 90 210 210

heater-b
false
0
Rectangle -7500403 true true 0 0 300 300
Rectangle -16777216 true false 30 30 135 135
Rectangle -16777216 true false 165 165 270 270

hex
false
0
Polygon -7500403 true true 0 150 75 30 225 30 300 150 225 270 75 270

hex-valve
false
0
Rectangle -7500403 false true 0 0 300 300
Polygon -7500403 false true 105 60 45 150 105 240 195 240 255 150 195 60

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

nitrogen
true
0
Circle -10899396 true false 83 135 134
Circle -10899396 true false 83 45 134

oxygen
true
0
Circle -13791810 true false 83 135 134
Circle -13791810 true false 83 45 134

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

spray paint
false
0
Rectangle -7500403 false true 0 0 300 300
Circle -7500403 false true 75 75 150

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

valve-1
false
0
Rectangle -7500403 false true 0 0 300 300
Rectangle -7500403 false true 120 120 180 180

valve-2
false
0
Rectangle -7500403 false true 0 0 300 300
Rectangle -7500403 false true 60 120 120 180
Rectangle -7500403 false true 165 120 225 180

valve-hex
false
0
Rectangle -7500403 false true 0 0 300 300
Polygon -7500403 false true 105 60 45 150 105 240 195 240 255 150 195 60

valve-triangle
false
0
Rectangle -7500403 true true 0 0 300 300
Polygon -16777216 true false 150 45 30 240 270 240

valves
false
0
Rectangle -7500403 false true 0 0 300 300

wall
false
0
Rectangle -7500403 true true 0 0 300 300

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
1
@#$#@#$#@

globals
[
  tick-delta                 ;; how much we advance the tick counter this time through
  max-tick-delta             ;; the largest tick-delta is allowed to be
  box-x box-y                ;; patch coords of box's upper right corner
  total-particle-number
  maxparticles
  show-dark?                 ;; hides or shows the dark particles in the simulation.
                             ;; see NetLogo features Info tab for full explanation of
                             ;; what dark particles are and why they are used.
]

breed [ particles particle]

particles-own
[
  speed mass                 ;; particle info
  last-collision             ;; keeps track of last particle this particle collided with
  darkparticle?              ;; is this particle visible and interacting with visible particles?
]

to setup
  ca reset-ticks
  set show-dark? false
  set-default-shape particles "circle"
  set maxparticles 400
  set tick-delta 0
  ;; starting this at zero means that no particles will move until we've
  ;; calculated vsplit, which we won't even try to do until there are some
  ;; particles.
  set total-particle-number 0
  setup-all-particles maxparticles 0 0
end

to go
  if bounce? [ ask particles [ bounce ] ] ;; all particles bounce
  ask particles [ move ]         ;; all particles move
  if collide?
    [
      ask particles
      [
        check-for-particlecollision
      ]
    ]
  tick-advance tick-delta
  ;; we need to check this down here as well, because if there are no
  ;; particles left, trying to set the new tick-delta will cause an error...
  set tick-delta 1 / (ceiling max [speed] of particles)
  display
end

to bounce  ;; particle procedure
  ;; if we're not about to hit a wall (yellow patch), or if we're already on a
  ;; wall, we don't need to do any further checks
  if pcolor = yellow or [pcolor] of patch-at dx dy != yellow [ stop ]
  ;; get the coordinates of the patch we'll be on if we go forward 1
  let new-px round (xcor + dx)
  let new-py round (ycor + dy)
  ;; if hitting left or right wall, reflect heading around x axis
  if (abs new-px = box-x)
    [ set heading (- heading) ]
  ;; if hitting top or bottom wall, reflect heading around y axis
  if (abs new-py = box-y)
    [ set heading (180 - heading) ]
end

to move  ;; particle procedure
  let next-patch patch-ahead (speed * tick-delta)
  ;; die if we're about to wrap...
  if ((pxcor != 0 and ([pxcor] of next-patch) = (- pxcor)) or
      (pycor != 0 and ([pycor] of next-patch) = (- pycor)))
    [ set darkparticle? true ifelse show-dark? [set shape "dark" set color red][set shape "nothing" set color green]]
  if next-patch != patch-here
    [ set last-collision nobody ]
  jump (speed * tick-delta)
end

to check-for-particlecollision ;; particle procedure
  if count other particles-here with [darkparticle? = [darkparticle?] of myself ] >= 1
  [
    ;; the following conditions are imposed on collision candidates:
    ;;   1. they must have a lower who number than my own, because collision
    ;;      code is asymmetrical: it must always happen from the point of view
    ;;      of just one particle.
    ;;   2. they must not be the same particle that we last collided with on
    ;;      this patch, so that we have a chance to leave the patch after we've
    ;;      collided with someone.
    let candidate one-of other particles-here with
      [who < [who] of myself and myself != last-collision and darkparticle? = [darkparticle?] of myself ]
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

end


;;;
;;; drawing procedures
;;;

;; allows the user to place the box by clicking on one corner.
to place-box
  undraw-box
  ask patch 0 0 [ set pcolor gray ]
  while [not mouse-down?] [wait 0.01]
  set box-x (abs round mouse-xcor)
  set box-y (abs round mouse-ycor)
  if box-x <= 2 [set box-x 2]
  if box-y <= 2 [set box-y 2]
  draw-box

  while [mouse-down?] [wait 0.1]
  ask patch 0 0 [ set pcolor black ]
end

;; removes the box.
to undraw-box
  ask patches [ set pcolor black ]
end

;; draws a square yellow box at distance box-edge from the origin
to draw-box
  ask patches with [((abs pxcor = box-x) and (abs pycor <= box-y)) or
                    ((abs pycor = box-y) and (abs pxcor <= box-x))]
    [ set pcolor yellow ]
end

;; allows the user to place a particle using the mouse
to place-particles
  if mouse-down?
  [
    paint-particles number-of-particles-to-add mouse-xcor mouse-ycor
    while [mouse-down?] [wait 0.1]
  ]
   set total-particle-number (count particles with  [darkparticle? = false])
  display
end


;; places all the n particles in a cluster around point (x,y).
to setup-all-particles [n x y]
  create-particles n
  [
    set darkparticle? true
    ifelse show-dark?
      [set shape "dark" set color red]
      [set shape "nothing" set color green]  ;; nothing is an actual shape with no components in it
                                             ;; this empty shape is included to make the computational load
                                             ;; of dark particles closer to that of visible particles
    setxy random-float x random-float y
    set speed 10
    set mass 2
    set last-collision nobody
    ;; if we're only placing one particle, use the exact position
    if n > 1
      [ jump random-float 5 ]
  ]
  set tick-delta 1 / (ceiling max [speed] of particles)
  set total-particle-number (count particles with [ darkparticle? = false])
end

;; places n particles in a cluster around point (x,y).
to paint-particles [n x y]
   ifelse ( count particles with [ darkparticle? = true ] >= n )
   [
     ;; turn some of the dark particles to visible particles
     ask n-of n particles with [darkparticle? = true]
     [
        set shape  "circle"
        setxy x y
        set speed 10
        set mass 2
        set color green
        set heading random-float 360
        set last-collision nobody
        set darkparticle? false
        ;; if we're only placing one particle, use the exact position
        if n > 1
        [ jump random-float 5 ]
     ]
  ]
  [user-message (word "The maximum number of particles allowed in this model is "  maxparticles  ".  You can not add "  n
  " more particles to the "  (count particles with [darkparticle? = false])  " you already have in the model")]
  set tick-delta 1 / (ceiling max [speed] of particles)
  set total-particle-number (count particles with  [darkparticle? = false])
end
@#$#@#$#@
GRAPHICS-WINDOW
236
10
481
253
26
26
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
-26
26
-26
26
1
1
1
ticks

SLIDER
14
47
227
80
number-of-particles-to-add
number-of-particles-to-add
1
50
50
1
1
NIL
HORIZONTAL

BUTTON
14
124
89
157
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
14
10
89
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

SWITCH
14
163
124
196
collide?
collide?
1
1
-1000

SWITCH
127
163
231
196
bounce?
bounce?
0
1
-1000

MONITOR
101
206
230
251
Number
count particles with [darkparticle? = false]
0
1
11

BUTTON
14
84
146
117
NIL
place-particles
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
106
10
207
43
NIL
place-box
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

This model introduces the behavior of gas particles trapped in a fixed-volume container (such as a bike tire) or free and unbounded.  This model is part of the "Connected Chemistry" curriculum http://ccl.northwestern.edu/curriculum/ConnectedChemistry/ which explore the behavior of gases.

Most of the models in the Connected Chemistry curriculum use the same basic rules for simulating the behavior of gases.  Each model highlights different features of how gas behavior is related to gas particle behavior.

In all of the models, gas particles are assumed to move and to collide, both with each other and with objects such as walls.

In this model, the fixed volume container (represented by a box), can be drawn in different sizes and proportions. The number of particles added to the inside or the outside of the box can be changed by painting particles.  And the rules of particle interactions (do they bounce off the walls? and do they collide with each other?) can be easily turned on and off).

This model helps students become acclimated to the user interface of NetLogo and evaluate modeling assumptions and representations, before they begin more analytical data analysis and mathematical modeling tasks associated with later models.

## HOW IT WORKS

When the COLLIDE? switch is on, the particles are modeled as hard balls with no internal energy except that which is due to their motion.  Collisions between particles are elastic.  When the BOUNCE? switch is on, the particle will then bounce off the wall in an elastic reflection (angle of incidence equals the angle of reflection).

Particles behave according to the following rules:
1. A particle moves in a straight line without changing its speed, unless it collides with another particle or bounces off the wall.
2. Two particles "collide" if COLLIDE switch is on and they find themselves on the same patch (the world is composed of a grid of small squares called patches).
3. A random axis is chosen, as if they are two balls that hit each other and this axis is the line connecting their centers.
4. They exchange momentum and energy along that axis, according to the conservation of momentum and energy.  This calculation is done in the center of mass system.
5. Each turtle is assigned its new velocity, energy, and heading.
6. If a turtle finds itself on or very close to a wall of the container, it "bounces" -- that is, reflects its direction and keeps its same speed.

## HOW TO USE IT

1. Press the SETUP button
2. Press the PLACE-BOX button.  (A small gray dot will be drawn)
3. Click anywhere in the black portion of the WORLD & VIEW and a yellow box will be drawn to make a corner of it where you click.
4. Repeat steps 1-3 to draw different boxes.
6. If there is no yellow box in your model, repeat steps 1-3.
7. Press the PLACE-PARTICLES button.  The button will remain a dark black color.
8. Click anywhere in the WORLD & VIEW to draw in some particles.
9. Press GO/STOP and observe what happens.
10. Turn the COLLIDE? switch off and repeat steps 7-9 and observe the effect.
11. Turn the BOUNCE? switch off and repeat steps 7-9 and observe the effect.

Initial settings:
- NUMBER-OF-PARTICLES-TO-ADD: the number of gas particles in the box when the simulation starts.

Monitors:
- CLOCK: the number of times the go procedure has been run
- NUMBER: the number of particles in the box

## THINGS TO NOTICE

Can you observe collisions with the walls as they happen (you can pendown a particle or slow down the model)?  For example, do the particles change their color?  Direction?

In what ways is this model a correct idealization of kinetic molecular theory (KMT)?

In what ways is this model an incorrect idealization of the real world?

## THINGS TO TRY

Turn the COLLIDE? switch off and repeat steps 7-9 and observe the effects.
Turn the BOUNCE? switch off and repeat steps 7-9 and observe the effects.

## EXTENDING THE MODEL

Can you "puncture" the box, so that particles will escape?

What would happen if the box were heated?  How would the particles behave?  How would this affect the pressure?  Add a slider and code that increases the temperature inside the box.

If you could change the shape of the box, so that the volume remains the same: Does the shape of the box make a difference in the way the particles behave, or the values of pressure?

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

dark
true
0
Rectangle -7500403 true true 100 12 197 284

nothing
true
0

@#$#@#$#@
NetLogo 5.0beta1
@#$#@#$#@
setup
set box-x 18
set box-y 19
draw-box
set bounce? true
paint-particles number-of-particles-to-add 0 0
paint-particles number-of-particles-to-add 0 0
set total-particle-number (count particles with  [darkparticle? = false])
repeat 75 [ go ]
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

globals 
[
  clock tick-length          ;; clock variables
  bounce?
  collisions?
  number
  box-x box-y                ;; patch coords of box's upper right corner
  total-particle-number
]

breed [ particles particle ]

particles-own 
[
  speed mass                 ;; particle info
  last-collision
]

to setup
  ca
  set clock 0
  ;; starting this at zero means that no particles will move until we've
  ;; calculated vsplit, which we won't even try to do until there are some
  ;; particles.
  set tick-length 0
  set total-particle-number 0
end

to go
  let old-clock 0
  if not any? particles
    ;; stop if all particles have left the screen
    [ stop ]
  if bounce?
    [ ask particles [ bounce ] ]
  ask particles [ move ]
  if collisions?
    [ ask particles
      [ check-for-collision ] ]
  set old-clock clock
  set clock clock + tick-length
  ;; we need to check this down here as well, because if there are no
  ;; particles left, trying to set the new tick-length will cause an error...
  if not any? particles
    ;; stop if all particles have left the screen
    [ stop ]
  set tick-length 1 / (ceiling max [speed] of particles)
  set total-particle-number (count particles)
end

to bounce  ;; particle procedure
  let new-px 0
  let new-py 0

  ;; if we're not about to hit a wall (yellow patch), or if we're already on a
  ;; wall, we don't need to do any further checks
  if pcolor = yellow or [pcolor] of patch-at dx dy != yellow 
    [ stop ]
  ;; get the coordinates of the patch we'll be on if we go forward 1
  set new-px round (xcor + dx)
  set new-py round (ycor + dy)
  ;; if hitting left or right wall, reflect heading around x axis
  if (abs new-px = box-x)
    [ set heading (- heading) ]
  ;; if hitting top or bottom wall, reflect heading around y axis
  if (abs new-py = box-y)
    [ set heading (180 - heading) ]
end

to move  ;; particle procedure
  let next-patch 0
  set next-patch patch-ahead (speed * tick-length)
  ;; die if we're about to wrap...
  if ((pxcor != 0 and ([pxcor] of next-patch) = (- pxcor)) or
      (pycor != 0 and ([pycor] of next-patch) = (- pycor)))
    [ die ]
  if next-patch != patch-here
    [ set last-collision nobody ]
  jump (speed * tick-length)
end

to check-for-collision  ;; particle procedure
  let candidate 0
  if count other particles-here >= 1
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
;;      particle's speed along theta, and whose second compenent is the speed
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
    ;; local copies of other-particle's relevant quantities
    let mass2 0
    let speed2 0
    let heading2 0 

    ;; quantities used in the collision itself
    let theta 0   ;; heading of vector from my center to the center of other-particle.
    let v1t 0     ;; velocity of self along direction theta
    let v1l 0     ;; velocity of self perpendicular to theta
    let v2t 0
    let v2l 0    ;; velocity of other-particle, represented in the same way
    let vcm 0    ;; velocity of the center of mass of the colliding particles,
                 ;;   along direction theta


  
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
  ask other-particle [ set speed sqrt ((v2t * v2t) + (v2l * v2l)) ]
  if v2l != 0 or v2t != 0
    [ ask other-particle [ set heading (theta - (atan v2l v2t)) ] ]



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

;; allows the user to place the box by clicking on one corner.
to place-box
  undraw-box
  ask patch 0 0 [ set pcolor gray ]
  while [not mouse-down?] [wait 0.01]
  set box-x (abs round mouse-xcor)
  set box-y (abs round mouse-ycor)
  ifelse box-x > 1 and box-y > 1
    [ draw-box ]
    [ user-message "The box must be larger." ]
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
    make-particles number mouse-xcor mouse-ycor
    while [mouse-down?] [wait 0.1]
  ]
end

;; places n particles in a cluster around point (x,y).
to make-particles [n x y]
  create-particles n
  [
    setxy x y
    set speed 10
    set mass 2
    set heading random-float 360
    set last-collision nobody
    ;; if we're only placing one particle, use the exact position
    if n > 1
      [ jump random-float 5 ]
    recolor
  ]
  set tick-length 1 / (ceiling max [speed] of particles)
  set total-particle-number (count particles)
end

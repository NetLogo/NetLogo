globals
[
  result ;; for benchmarking
  tick-length               ;; how much simulation will pass in this step
  box-edge                  ;; distance of box edge from axes
  colliding-particles
  sorted-colliding-particles
  colliding-particle-1
  colliding-particle-2
  original-tick-length
  last-view-update
  manage-view-updates?
  view-update-rate          ;; specifies the minimum amount of simulation time that must
                            ;; pass before the view is updated
]

breed [ particles particle ]

particles-own
[
  speed
  mass
]

;;;;;;;;;;;;;;;;

to benchmark
  random-seed 12345
  reset-timer
  setup
  set manage-view-updates? false
  repeat 3500 [ go ]
  set result timer
end

to setup
  ca reset-ticks
  set-default-shape particles "circle"
  set manage-view-updates? true
  set view-update-rate 0.2
  set box-edge (max-pxcor - 1)
  make-box
  make-particles

  ;; set variable tick length based on fastest particle.   If the fastest particle has a speed of 1,
  ;; then tick-length is 1.  If the fastest particles has a speed of 10, then tick-length is 1/10.
  set tick-length (1 / (ceiling max [speed] of particles))
  set original-tick-length tick-length
  set colliding-particle-1 nobody
  set colliding-particle-2 nobody
  rebuild-collision-list
end

to rebuild-collision-list
  set colliding-particles []
  ask particles [check-for-wall-collision]
  ask particles [check-for-particle-collision ]
end


to go
  ;;Since only collisions involving the particles that collided most recently could be affected,
  ;;we filter those out of colliding-particles.  Then we recalculate all possible collisions for the
  ;;particles that collided last.  The ifelse statement is necessary because colliding-particle-2
  ;;can be either a particle or a string representing a wall.  If it is a wall, we don't want to
  ;;invalidate all collisions involving that wall (because the wall's position wasn't affected, those
  ;;collisions are still valid.
  ifelse is-turtle? colliding-particle-2
  [
    set colliding-particles filter [item 1 ? != colliding-particle-1 and
                                    item 2 ? != colliding-particle-1 and
                                    item 1 ? != colliding-particle-2 and
                                    item 2 ? != colliding-particle-2]
                              colliding-particles
    ask colliding-particle-2 [check-for-wall-collision]
    ask colliding-particle-2 [check-for-particle-collision]
  ]
  [
    set colliding-particles filter [item 1 ? != colliding-particle-1 and
                                    item 2 ? != colliding-particle-1]
                              colliding-particles
  ]
  if colliding-particle-1 != nobody [ask colliding-particle-1 [check-for-wall-collision]]
  if colliding-particle-1 != nobody [ask colliding-particle-1 [check-for-particle-collision]]

  sort-collisions
  ask particles [ jump speed * tick-length ]
  collide-winners
  tick-advance tick-length

  ;; flag that updates display only after enough simulation time has passed.
  ;; the display-update-rate sets the minimum simulation time that must pass
  ;; before updating the display.  This avoids many redisplays of the view for
  ;; a series of small time steps in the simulation (which would make the view show
  ;; what looks like particles slowing down as they get near multiple collision points)
  if manage-view-updates? [
    if (ticks - last-view-update) > view-update-rate
    [ display
      set last-view-update ticks ]
      ]
end

to-report convert-heading-x [heading-angle]
  report sin heading-angle
end

to-report convert-heading-y [heading-angle]
  report cos heading-angle
end


to check-for-particle-collision

;; check-for-particle-collision is a particle procedure that determines the time it takes to the collision between
;; two particles (if one exists).  It solves for the time by representing the equations of motion for
;; distance, velocity, and time in a quadratic equation of the vector components of the relative velocities
;; and changes in position between the two particles and solves for the time until the next collision

  let my-x xcor
  let my-y ycor
  let my-particle-size size
  let my-x-speed (speed * convert-heading-x heading)
  let my-y-speed (speed * convert-heading-y heading)

  ask other particles
  [

         let dpx (xcor - my-x)   ;; relative distance between particles in the x direction
         let dpy (ycor - my-y)    ;; relative distance between particles in the y direction
         let x-speed (speed * convert-heading-x heading) ;; speed of other particle in the x direction
         let y-speed (speed * convert-heading-y heading) ;; speed of other particle in the x direction
         let dvx (x-speed - my-x-speed) ;; relative speed difference between particles in the x direction
         let dvy (y-speed - my-y-speed) ;; relative speed difference between particles in the y direction
         let sum-r (((my-particle-size) / 2 ) + (([size] of self) / 2 )) ;; sum of both particle radii



        ;; To figure out what the difference in position (P1) between two particles at a future time (t) would be,
        ;; one would need to know the current difference in position (P0) between the two particles
        ;; and the current difference in the velocity (V0) between of the two particles.

        ;; The equation that represents the relationship would be:   P1 = P0 + t * V0

        ;; we want find when in time (t), P1 would be equal to the sum of both the particle's radii (sum-r).
        ;; When P1 is equal to is equal to sum-r, the particles will just be touching each other at
        ;; their edges  (a single point of contact).

        ;; Therefore we are looking for when:   sum-r =  P0 + t * V0

        ;; This equation is not a simple linear equation, since P0 and V0 should both have x and y components
        ;;  in their two dimensional vector representation (calculated as dpx, dpy, and dvx, dvy).


        ;; By squaring both sides of the equation, we get:     (sum-r) * (sum-r) =  (P0 + t * V0) * (P0 + t * V0)

        ;;  When expanded gives:   (sum-r ^ 2) = (P0 ^ 2) + (t * PO * V0) + (t * PO * V0) + (t ^ 2 * VO ^ 2)

        ;;  Which can be simplified to:    0 = (P0 ^ 2) - (sum-r ^ 2) + (2 * PO * V0) * t + (VO ^ 2) * t ^ 2

        ;;  Below, we will let p-squared represent:   (P0 ^ 2) - (sum-r ^ 2)
        ;;  and pv represent: (2 * PO * V0)
        ;;  and v-squared represent: (VO ^ 2)

        ;;  then the equation will simplify to:     0 = p-squared + pv * t + v-squared * t^2


         let p-squared   ((dpx * dpx) + (dpy * dpy)) - (sum-r ^ 2)   ;; p-squared represents difference of the
                                                                     ;; square of the radii and the square
                                                                     ;; of the initial positions

         let pv  (2 * ((dpx * dvx) + (dpy * dvy)))  ;;the vector product of the position times the velocity
         let v-squared  ((dvx * dvx) + (dvy * dvy)) ;; the square of the difference in speeds
                                                    ;; represented as the sum of the squares of the x-component
                                                    ;; and y-component of relative speeds between the two particles


         ;; p-squared, pv, and v-squared are coefficients in the quadratic equation shown above that
         ;; represents how distance between the particles and relative velocity are related to the time,
         ;; t, at which they will next collide (or when their edges will just be touching)

         ;; Any quadratic equation that is the function of time (t), can represented in a general form as:
         ;;   a*t*t + b*t + c = 0,
         ;; where a, b, and c are the coefficients of the three different terms, and has solutions for t
         ;; that can be found by using the quadratic formula.  The quadratic formula states that if a is not 0,
         ;; then there are two solutions for t, either real or complex.

         ;; t is equal to (b +/- sqrt (b^2 - 4*a*c)) / 2*a

         ;; the portion of this equation that is under a square root is referred to here
         ;; as the determinant, D1.   D1 is equal to (b^2 - 4*a*c)
         ;; and:   a = v-squared, b = pv, and c = p-squared.


         let D1 pv ^ 2 -  (4 * v-squared * p-squared)


         ;; the next line next line tells us that a collision will happen in the future if
         ;; the determinant, D1 is >= 0,  since a positive determinant tells us that there is a
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

         if D1 >= 0
            [set time-to-collision (- pv - sqrt D1) / (2 * v-squared) ]        ;;solution for time step


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
              let colliding-pair (list (time-to-collision + ticks) self myself) ;; sets a three element list of
                                                        ;; time to collision and the colliding pair
              set colliding-particles fput colliding-pair colliding-particles  ;; adds above list to collection
                                                                               ;; of colliding pairs and time
                                                                               ;; steps
             ]
  ]

end


to check-for-wall-collision ;; particle procedure for determining if a particle will hit one of the
                            ;; four walls of the box

  let x-speed (speed * convert-heading-x heading)
  let y-speed (speed * convert-heading-y heading)
  let xpos-plane (box-edge - 0.5)      ;;inside boundary of right side of the box
  let xneg-plane (- box-edge + 0.5)    ;;inside boundary of left side of the box
  let ypos-plane (box-edge - 0.5)      ;;inside boundary of top side of the box
  let yneg-plane (- box-edge + 0.5)    ;;inside boundary of bottom side of the box

  ;; find point of contact on edge of circle
  ;; points of contact located at 1 radius above, below, to the left, and to the right
  ;; of the center of the particle

  let contact-point-xpos (xcor + (size / 2))
  let contact-point-xneg (xcor - (size / 2))
  let contact-point-ypos (ycor + (size / 2))
  let contact-point-yneg (ycor - (size / 2))

  ;; find difference in position between plane location and edge of circle

  let dpxpos (xpos-plane - contact-point-xpos)
  let dpxneg (xneg-plane - contact-point-xneg)
  let dpypos (ypos-plane - contact-point-ypos)
  let dpyneg (yneg-plane - contact-point-yneg)

  let t-plane-xpos 0

  ;; solve for the time it will take the particle to reach the wall by taking
  ;; the distance to the wall and dividing it by the speed in the direction to the wall

  ifelse  x-speed != 0 [set t-plane-xpos (dpxpos / x-speed)] [set t-plane-xpos 0]
   if t-plane-xpos > 0
      [
       assign-colliding-wall t-plane-xpos "plane-xpos"
      ]

  let t-plane-xneg 0
  ifelse  x-speed != 0 [set t-plane-xneg (dpxneg / x-speed)] [set t-plane-xneg 0]
   if t-plane-xneg > 0
      [
       assign-colliding-wall t-plane-xneg "plane-xneg"
      ]
  let t-plane-ypos 0
  ifelse  y-speed != 0 [set t-plane-ypos (dpypos / y-speed)] [set t-plane-ypos 0]
   if t-plane-ypos > 0
      [
       assign-colliding-wall t-plane-ypos "plane-ypos"
      ]

  let t-plane-yneg 0
  ifelse  y-speed != 0 [set t-plane-yneg (dpyneg / y-speed)] [set t-plane-yneg 0]
  if t-plane-yneg > 0
      [
       assign-colliding-wall t-plane-yneg "plane-yneg"
      ]

end

to assign-colliding-wall [time-to-collision wall]
  ;; this procedure is used by the check-for-wall-collision procedure
  ;; to assemble the correct particle-wall pair
  ;; time-to-collision is relative (ie, a collision will occur one second from now)
  ;; We need to store the absolute time (ie, a collision will occur at time 48.5 seconds.
  ;; So, we add clock to time-to-collision when we store it.

  let colliding-pair (list (time-to-collision + ticks) self wall)
  set colliding-particles fput colliding-pair colliding-particles

end


to sort-collisions
  ;; Slight errors in floating point math can cause a collision that just
  ;; happened to be calculated as happening again a very tiny amount of
  ;; time into the future, so we remove any collisions that involves
  ;; the same two particles (or particle and wall) as last time.
  set colliding-particles filter [item 1 ? != colliding-particle-1 or
                                  item 2 ? != colliding-particle-2]
                                 colliding-particles
  set colliding-particle-1 nobody
  set colliding-particle-2 nobody
  set tick-length original-tick-length
  if colliding-particles = [] [ stop ]
  ;; Sort the list of projected collisions between all the particles into an ordered list.
  ;; Take the smallest time-step from the list (which represents the next collision that will
  ;; happen in time).  Use this time step as the tick-length for all the particles to move through
  let winner first colliding-particles
  foreach colliding-particles [if first ? < first winner [set winner ?]]
  ;;winner is now the collision that will occur next
  let dt item 0 winner
  if dt > 0
  [
    ;;If the next collision is more than 1 in the future,
    ;;clear the winners and advance the simulation one tick.
    ;;This helps smooth the model on smaller particle counts.
    ifelse dt - ticks <= 1
    ;;We have to subtract clock back out because now we want the relative time until collision,
    ;;not the absolute time the collision will occur.
    [set tick-length dt - ticks
     set colliding-particle-1 item 1 winner
     set colliding-particle-2 item 2 winner]
    ;;Since there are no collisions in the next second, we will set winners to [] to keep from
    ;;mistakenly colliding any particles that shouldn't collide yet.
    [set tick-length 1]
  ]
end


to collide-winners  ;; deal with 3 possible cases of collisions:
                    ;; particle and one wall, particle and two walls, and two particles
    if colliding-particle-1 = nobody [ stop ]
    ;; deal with a case where the next collision in time is between a particle and a wall

    if colliding-particle-2 = "plane-xpos" or colliding-particle-2 = "plane-xneg"
         [ask colliding-particle-1 [set heading (- heading)]
          stop]
    if colliding-particle-2 = "plane-ypos" or colliding-particle-2 = "plane-yneg"
         [ask colliding-particle-1 [set heading (180 - heading)]
          stop]

    ;; deal with the remaining case of the next collision in time being between two particles.

    ask colliding-particle-1 [collide-with colliding-particle-2]

end


to collide-with [ other-particle ] ;; particle procedure

  ;;; PHASE 1: initial setup

    ;; for convenience, grab some quantities from other-particle
    let mass2 [mass] of other-particle
    let speed2 [speed] of other-particle
    let heading2 [heading] of other-particle

  ;;modified so that theta is heading toward other particle
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
  if v1l != 0 or v1t != 0
    [ set heading (theta - (atan v1l v1t)) ]

  ;; and do the same for other-particle
  ask other-particle [ set speed sqrt (((v2t * v2t) + (v2l * v2l))) ]
  if v2l != 0 or v2t != 0
    [ ask other-particle [ set heading (theta - (atan v2l v2t)) ] ]

  ;; PHASE 5: final updates

  ;; now recolor, since color is based on quantities that may have changed
  recolor ask other-particle [ recolor ]
end


to recolor
    if color-scheme = "red-green-blue" [ recolor-banded ]
    if color-scheme = "blue shades" [ recolor-shaded ]
    if color-scheme  = "one color" [ recolor-none ]
end


to recolor-banded  ;; particle procedure
  let avg-speed 1
  ;; avg-speed is assumed to be 0.5, since particles are assigned a random speed between 0 and 1
  ;; particle coloring procedures for visualizing speed with a color palette,
  ;; red are fast particles, blue slow, and green in between.

  ifelse speed < (0.5 * avg-speed) ;; at lower than 50% the average speed
  [
    set color blue       ;; slow particles colored blue
  ]
  [
    ifelse speed > (1.5 * avg-speed) ;; above 50% higher the average speed
      [ set color red ]        ;; fast particles colored blue
      [ set color green ]      ;; medium speed particles colored green
  ]

end


to recolor-shaded
  let avg-speed 1
 ;; avg-speed is assumed to be 0.5, since particles are assigned a random speed between 0 and 1
 ;; a particle shading gradient is applied to all particles less than speed 1.5,
 ;; the uppermost threshold speed to apply the shading gradient to.

  ifelse speed < (3 * avg-speed)
  [ set color (sky - 3.001) + (8 * speed / (3 * avg-speed)) ]
  [ set color (sky + 4.999)]
end

to recolor-none
  set color green - 1
end


;;;
;;; drawing procedures
;;;

to make-box
  ask patches with [ ((abs pxcor = box-edge) and (abs pycor <= box-edge)) or
                     ((abs pycor = box-edge) and (abs pxcor <= box-edge)) ]
    [ set pcolor yellow ]
end

;; creates some particles
to make-particles
  create-ordered-particles number [
    set speed 1
    set size smallest-particle-size + random-float (largest-particle-size - smallest-particle-size)
    set mass (size * size) ;; set the mass proportional to the area of the particle
    recolor
    set heading random-float 360
  ]
  arrange particles
end

;; If the number of particles requested by the user won't fit in the box,
;; this code will go into an infinite loop. To work around a NetLogo bug
;; where some kinds of infinite loops cannot be halted by Tools->Halt,
;; we put this code in a separate procedure and write it a certain way.
;; (It's necessary for the loop not to be within an ASK.  The reason
;; for this is very obscure.  We plan to fix the problem in a future
;; NetLogo version.)
to arrange [particle-set]
  if not any? particle-set [ stop ]
  ask particle-set [ random-position ]
  arrange particle-set with [overlapping?]
end

to-report overlapping?  ;; particle procedure
  ;; here, we use in-radius just for improved speed
  report any? other particles in-radius ((size + largest-particle-size) / 2)
                              with [distance myself < (size + [size] of myself) / 2]
end

;; place particle at random location inside the box.
to random-position  ;; particle procedure
  setxy one-of [1 -1] * random-float (box-edge - 0.5 - size / 2)
        one-of [1 -1] * random-float (box-edge - 0.5 - size / 2)
end


;;;
;;; procedure for reversing time
;;;

to reverse-time
  ask particles [ rt 180 ]
  rebuild-collision-list
  ;; the last collision that happened before the model was paused
  ;; (if the model was paused immediately after a collision)
  ;; won't happen again after time is reversed because of the
  ;; "don't do the same collision twice in a row" rule.  We could
  ;; try to fool that rule by setting colliding-particle-1 and
  ;; colliding-particle-2 to nobody, but that might not always work,
  ;; because the vagaries of floating point math means that the
  ;; collision might be calculated to be slightly in the past
  ;; (the past that used to be the future!) and be skipped.
  ;; So to be sure, we force the collision to happen:
  collide-winners
end

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
  let old-clock ticks
  reverse-time
  while [ticks < 2 * old-clock] [ go ]
  ask particles [ set color white ]
end
@#$#@#$#@
GRAPHICS-WINDOW
212
10
708
527
40
40
6.0
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

BUTTON
13
172
106
205
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
38
199
71
number
number
1
200
200
1
1
NIL
HORIZONTAL

BUTTON
112
172
199
205
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
117
198
150
largest-particle-size
largest-particle-size
1
10
4
0.5
1
NIL
HORIZONTAL

CHOOSER
14
226
145
271
color-scheme
color-scheme
"red-green-blue" "blue shades" "one color"
0

SLIDER
10
77
198
110
smallest-particle-size
smallest-particle-size
1
5
1
0.5
1
NIL
HORIZONTAL

BUTTON
23
359
200
504
NIL
benchmark
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

MONITOR
32
455
190
500
NIL
result
17
1
11

@#$#@#$#@
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
NetLogo 5.0beta1
@#$#@#$#@
benchmark set result 0
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

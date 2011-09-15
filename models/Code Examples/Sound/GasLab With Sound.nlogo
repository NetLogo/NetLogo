extensions [ sound ]

globals
[
  tick-delta                 ;; how much we advance the tick counter this time through
  max-tick-delta             ;; the largest tick-delta is allowed to be
  box-edge                   ;; distance of box edge from axes
  pressure
  pressure-history
  zero-pressure-count        ;; how many zero entries are in pressure-history
  wall-hits-per-particle     ;; average number of wall hits per particle
  particles-to-add
  new-particles              ;; agentset of particles added via add-particles-middle
  message-shown?             ;; whether we've shown the warning message yet
  length-horizontal-surface  ;; the size of the wall surfaces that run horizontally - the top and bottom of the box
  length-vertical-surface    ;; the size of the wall surfaces that run vertically - the left and right of the box
  three-speed                ;; current speed of particle 3 (music addition)
  pressure-now               ;; current pressure (music addition
]

breed [ particles particle ]
breed [ flashes flash ]

flashes-own [birthday]

particles-own
[
  speed mass                 ;; particle info
  wall-hits                  ;; # of wall hits during this clock cycle ("big tick")
  momentum-difference        ;; used to calculate pressure from wall hits
  last-collision
  new?                       ;; used to build the new-particles agentset; this is
                             ;; only ever set to true by add-particles-middle
]

to startup
  set message-shown? false
end

to setup
  sound:stop-music  ;; start music by closing previous one
  let tmp message-shown?
  ca
  set message-shown? tmp
  set-default-shape particles "circle"
  set-default-shape flashes "square"
  set particles-to-add 0
  ;; box has constant size...
  set box-edge (max-pxcor - 1)
  ;;; the length of the horizontal or vertical surface of
  ;;; the inside of the box must exclude the two patches
  ;; that are the where the perpendicular walls join it,
  ;;; but must also add in the axes as an additional patch
  ;;; example:  a box with a box-edge of 10, is drawn with
  ;;; 19 patches of wall space on the inside of the box
  set length-horizontal-surface  ( 2 * (box-edge - 1) + 1)
  set length-vertical-surface  ( 2 * (box-edge - 1) + 1)
  make-box
  make-particles initial-number
  set pressure-history []
  set zero-pressure-count 0
  reset-ticks
  set pressure-now pressure ;; setup for current pressure
  set three-speed [speed] of particle 3  ;; setup for current particle speed
end

to go
  if not single-particle-speed? and three-speed > 0  ;; making sure that particle speed is sung only when switch is on
        [ sound:stop-note "oboe" (80 - 120 / ( three-speed ))
          sound:stop-note "oboe" 50 ]
  if not pressure? and pressure > 0  ; making sure that pressure is sung only when switch is on
        [ sound:stop-note "recorder" ( 100 - 2000 / pressure )
          sound:stop-note "recorder" 20 ]
  if single-particle-speed?
     [ ask particle 3  ;; asking particle 3 to sing, changes note when speed changes, taking care of clock = 0
         [
           ifelse  ((abs (speed - three-speed) > 0 ) )
                     or (abs (speed - three-speed ) = speed )
              [ sing-one-particle-speed ]
              [ ifelse ticks > 0
                [ stop ]
                [ sound:start-note "oboe" (80 - 120 / ( [speed] of particle 3 )) single-particle-loudness - 10]
               ]
          ]
      ]
  if real-time? [ sing-real-time ]  ;; sing the real time ticker
  ask particles
      [ set new? false ]
  ask particles [ bounce ]
  ask particles [ move ]
  ask particles
    [ check-for-collision ]
  add-particles-side
  tick-advance tick-delta
  if floor ticks > floor (ticks - tick-delta)
      [ ifelse any? particles
          [ set wall-hits-per-particle mean [wall-hits] of particles ]
          [ set wall-hits-per-particle 0 ]
  ask particles
          [ set wall-hits 0 ]
  calculate-pressure
  update-plots
  if model-time? [ sound:play-note "nylon string guitar" 26 83 0.1 ] ] ;; sing model time
  calculate-tick-length
  ask flashes with [ticks - birthday > 0.4]
      [ set pcolor yellow
        die ]
  ifelse (single-particle-speed? or single-particle-collisions? or single-particle-wall-hits? )  ;; single particle speed is traced
      [ ask particle 3 [ pd ] ]
      [ ask particle 3 [ pu ] ]
  fade-patches ;; if the single particle is tracing, then the trace disappears after a while
end

to calculate-tick-length
  ifelse any? particles with [speed > 0]
    [ set tick-delta 1 / (ceiling max [speed] of particles) ]
    [ set tick-delta 1 ]
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
  set pressure-history lput pressure pressure-history
  set zero-pressure-count length filter [? = 0] pressure-history
  ask particles
    [ set momentum-difference 0 ]  ;; once the contribution to momentum has been calculated
                                   ;; this value is reset to zero till the next wall hit
  ifelse pressure? ;; when pressure changes, it sings
    [ sing-pressure ]
    [ ifelse pressure-now > 0
      [ sound:stop-note "recorder" ( 100 - 2000 / pressure-now )
        sound:stop-note "recorder" ( 30 ) ]
      [ stop ]
    ]
end

to bounce  ;; particle procedure
  let tone heading
  ;; if we're not about to hit a wall (yellow patch), or if we're already on a
  ;; wall, we don't need to do any further checks
  if shade-of? yellow pcolor or not shade-of? yellow [pcolor] of patch-at dx dy
    [ stop ]
  ;; get the coordinates of the patch we'll be on if we go forward 1
  let new-px round (xcor + dx)
  let new-py round (ycor + dy)
  ;; if hitting left or right wall, reflect heading around x axis
  if (abs new-px = box-edge)
    [ set heading (- heading)
      set wall-hits wall-hits + 1
      if (wall-hits?) [ sound:play-note "celesta" tone wall-hits-loudness 0.15 ] ;; when a particle hits the wall there's  sound (macro)
      if (single-particle-wall-hits? and who = 3) ;; when a particle hits the wall there's  sound (micro)
        [ ask particle 3 [ pd ] sound:play-note "clavi" ( 30 + heading / 5 ) wall-hits-loudness 0.2 ]

  ;;  if the particle is hitting a vertical wall, only the horizontal component of the speed
  ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
  ;; due to the reversing of direction of travel from the collision with the wall
      set momentum-difference momentum-difference + (abs (dx * 2 * mass * speed) / length-vertical-surface)
    ]
  ;; if hitting top or bottom wall, reflect heading around y axis
  if (abs new-py = box-edge)
    [ set heading (180 - heading)
      set wall-hits wall-hits + 1
      if wall-hits? [ sound:play-note "celesta" heading  single-particle-loudness 0.1 ]
  if (single-particle-wall-hits? and who = 3)
     [ ask particle 3 [ pd ] sound:play-note "clavi" ( 30 + heading / 5 ) single-particle-loudness 0.1 ]

  ;;  if the particle is hitting a horizontal wall, only the vertical component of the speed
  ;;  vector can change.  The change in velocity for this component is 2 * the speed of the particle,
  ;; due to the reversing of direction of travel from the collision with the wall
      set momentum-difference momentum-difference + (abs (dy * 2 * mass * speed) / length-horizontal-surface)  ]
  ask patch new-px new-py
    [ sprout-flashes 1 [ ht
                         set birthday ticks
                         set pcolor yellow - 3 ] ]
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
      if collisions? ;; make a sound when there's a collision that is a function of the sum of their speeds (macro)
        [ sound:play-note "telephone ring" 2 * ([speed] of self + [speed] of candidate) collisions-loudness 0.15 ]
      if (single-particle-collisions? and ( who = 3 or [who] of candidate = 3 )) ;; make a sound when there's a collision that is a function of the sum of their speeds (micro)
         [ ask particle 3 [ pd ] sound:play-note "glockenspiel" 69 single-particle-loudness + 40 0.2 ]

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

  ;; local copies of other-particle's relevant quantities
  ;mass2 speed2 heading2

  ;; quantities used in the collision itself
  ;theta   ;; heading of vector from my center to the center of other-particle.
  ;v1t     ;; velocity of self along direction theta
  ;v1l     ;; velocity of self perpendicular to theta
  ;v2t v2l ;; velocity of other-particle, represented in the same way
  ;vcm     ;; velocity of the center of mass of the colliding particles,
           ;;   along direction theta

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
  set speed sqrt ((v1t * v1t) + (v1l * v1l))
  ;; if the magnitude of the velocity vector is 0, atan is undefined. but
  ;; speed will be 0, so heading is irrelevant anyway. therefore, in that
  ;; case we'll just leave it unmodified.
  if v1l != 0 or v1t != 0
    [ set heading (theta - (atan v1l v1t)) ]

  ;; and do the same for other-particle
  ask other-particle [
    set speed sqrt ((v2t * v2t) + (v2l * v2l))
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

;; draws the box
to make-box
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

;; creates initial particles
to make-particles [number]
  create-particles number
  [
    setup-particle
    set speed random-float 20
    random-position
    recolor
  ]
  calculate-tick-length
end

;; adds particles from the left
to add-particles-side
  if particles-to-add > 0
    [ create-particles particles-to-add
        [ setup-particle
          setxy (- box-edge) 0
          set heading 90 ;; east
          rt 45 - random-float 90
          recolor

        ]
      if announce-add-particles? [ sound:play-note "tubular bells" 59 90 0.3 ]  ;;  announce when particles are added
      set particles-to-add 0
      calculate-tick-length
    ]
end

;; called by student from command center;
;; adds particles in middle
to add-particles-middle [n]
  create-particles n
    [ setup-particle
      set new? true
      recolor ]
  ;; add the new particles to an agentset, so they
  ;; are accessible to the student from the command
  ;; center, e.g. "ask new-particles [ ... ]"
  set new-particles particles with [new?]
  calculate-tick-length
end

to setup-particle  ;; particle procedure
  set new? false
  set speed 10
  set mass 1.0
  set last-collision nobody
  set wall-hits 0
  set momentum-difference 0
end

;; place particle at random location inside the box.
to random-position ;; particle procedure
  setxy ((1 - box-edge) + random-float ((2 * box-edge) - 2))
        ((1 - box-edge) + random-float ((2 * box-edge) - 2))
end

to-report last-n [n the-list]
 ifelse n >= length the-list
   [ report the-list ]
   [ report last-n n butfirst the-list ]
end

to fade-patches
  let trace-patches patches with [ shade-of? pcolor red or shade-of? pcolor  blue or shade-of? pcolor green ]
  if any? trace-patches
    [ ask trace-patches
      [ set pcolor ( pcolor - 0.05 )
        if (pcolor mod 10 < 1)
          [ set pcolor black ] ] ]
end

to sing-one-particle-speed     ;; procedure for listening to one particle's speed - tone is a function of speed
   ifelse single-particle-speed?
      [  ifelse (three-speed > 3)
             [ sound:stop-note "oboe" (80 - 120 / ( three-speed ))
               sound:stop-note "oboe" 50
               sound:start-note "oboe" (80 - 120 / ( [speed] of particle 3 )) single-particle-loudness
             ]
             [
               sound:stop-note "oboe" (80 - 120 / ( three-speed ))
               sound:stop-note "oboe" 50
               sound:start-note "oboe" 50 single-particle-loudness + 20
             ]
               set three-speed [speed] of particle 3
       ]
       [
         ifelse (three-speed > 3)
           [ sound:stop-note "oboe" (80 - 120 / ( three-speed ))]
           [ sound:stop-note "oboe" 50 ]
       ]
end

to sing-real-time ;; real time is drummed at a regular interval
       every real-time-pacer [ sound:play-note "sci-fi" 30 60 0.3 ]
end

to sing-pressure  ;;  pressure is sung by a recorder with the tone a function of pressure
  if ( abs ( pressure-now - pressure ) > 0 and pressure-now != 0)
          [ sound:stop-note "recorder" ( 100 - 2000 / pressure-now )
            sound:stop-note "recorder" ( 30 ) ]
  set pressure-now pressure
  ifelse (pressure > 30 )
        [ sound:start-note "recorder" ( 100 - 2000 / pressure ) pressure-loudness ]
        [ sound:start-note "recorder" ( 30 ) pressure-loudness + 10 ]
end
@#$#@#$#@
GRAPHICS-WINDOW
212
10
490
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
0
0
1
-33
33
-33
33
0
0
1
ticks

BUTTON
125
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
125
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
"default" 1.0 0 -16777216 true "" "plotxy ticks (count particles)"

SLIDER
0
78
124
111
number-to-add
number-to-add
0
100
25
1
1
NIL
HORIZONTAL

BUTTON
125
78
211
111
add particles
set particles-to-add\n    particles-to-add +\n      number-to-add
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
44
124
77
initial-number
initial-number
0
100
25
1
1
NIL
HORIZONTAL

MONITOR
1
112
99
157
Number
count particles
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
"default" 1.0 0 -16777216 true "" "if length pressure-history > 0\n[\n  ;; pressure plot is 'smoothed' by averaging 3 sequential values, only if switch is set this way\n  ifelse avg-plot-pressure?  \n    [ plotxy ticks (mean last-n 3 pressure-history) ]\n    [ plotxy ticks pressure ]\n]"

MONITOR
100
113
209
158
pressure
pressure
0
1
11

SWITCH
228
463
341
496
collisions?
collisions?
1
1
-1000

SWITCH
229
428
341
461
wall-hits?
wall-hits?
1
1
-1000

SWITCH
17
359
181
392
model-time?
model-time?
1
1
-1000

SWITCH
17
394
182
427
real-time?
real-time?
1
1
-1000

SWITCH
507
428
696
461
single-particle-speed?
single-particle-speed?
0
1
-1000

SWITCH
508
463
696
496
single-particle-wall-hits?
single-particle-wall-hits?
0
1
-1000

SWITCH
508
497
695
530
single-particle-collisions?
single-particle-collisions?
0
1
-1000

SLIDER
16
430
183
463
real-time-pacer
real-time-pacer
0.5
5
1.325
0.025
1
sec
HORIZONTAL

SWITCH
10
273
200
306
announce-add-particles?
announce-add-particles?
1
1
-1000

TEXTBOX
538
322
654
340
Listen to one particle
11
0.0
0

SWITCH
229
394
341
427
pressure?
pressure?
1
1
-1000

TEXTBOX
280
322
416
340
Listen to all the particles
11
0.0
0

TEXTBOX
38
321
128
339
Time in a model
11
0.0
0

SLIDER
342
463
469
496
collisions-loudness
collisions-loudness
0
100
30
5
1
NIL
HORIZONTAL

SLIDER
343
428
468
461
wall-hits-loudness
wall-hits-loudness
0
100
30
5
1
NIL
HORIZONTAL

SLIDER
343
393
469
426
pressure-loudness
pressure-loudness
0
120
60
5
1
NIL
HORIZONTAL

BUTTON
33
225
98
258
all on
set real-time? true \nset model-time? true \nset pressure? true \nset wall-hits? true \nset collisions? true \nset single-particle-speed? true \nset single-particle-collisions? true\nset single-particle-wall-hits? true \nset announce-add-particles? true
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
99
225
165
258
all off
sound:stop-music\nset collisions? false\nset single-particle-speed? false \nset single-particle-collisions? false\nset single-particle-wall-hits? false\nset announce-add-particles? false\nset model-time? false\nset real-time? false\nset pressure? false\nset wall-hits? false
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
507
394
696
427
single-particle-loudness
single-particle-loudness
0
80
30
5
1
NIL
HORIZONTAL

MONITOR
1
163
210
208
single speed
[speed] of particle 3
1
1
11

SWITCH
70
507
235
540
avg-plot-pressure?
avg-plot-pressure?
1
1
-1000

BUTTON
252
353
341
386
group on
set pressure? true set wall-hits? true set collisions? true
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
342
353
432
386
group off
set pressure? false set wall-hits? false set collisions? false
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
515
353
603
386
single on
set single-particle-speed? true set single-particle-collisions? true set single-particle-wall-hits? true
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
604
353
693
386
single off
set single-particle-speed? false \nset single-particle-collisions? false \nset single-particle-wall-hits? false
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

This model is included here in "Code Examples" as an example of taking an existing model and adding sound to it.  It is based on the GasLab models, in the Chemistry & Physics section of the Models Library.
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
NetLogo 5.0beta1
@#$#@#$#@
need-to-manually-make-preview-for-this-model
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

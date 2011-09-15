globals [
  fast average slow     ;; current counts
  avg-speed avg-energy  ;; current averages
  vsplit vclock         ;; clock variables
  raw-width raw-height  ;; box size variables
  piston-position       ;; position of the piston at any given time
  volume area           ;; because this is 2D, area is the perimeter and volume is the area
  total-pressure        ;; pressure per unit area
  pressure-history      ;; list of 'scale' previous total-pressures
  avg-pressure          ;; mean of the pressure-history
  initspeed initmass    ;; initial speed and initial mass of the all the particles-particle mass always stays the same, but the speed changes.
  going-down?           ;; flag for whether or not the piston is moving up or down
]

turtles-own [
  speed mass energy new-speed            ;; Turtle Info
  v1t v1l tmp-turtle                     ;; Turtle 1 -- Collide
  heading2 mass2 speed2 v2t v2l turtle2  ;; Turtle 2 -- Collide
  theta                                  ;; Turtles 1 + 2 -- Collide
  pressure                               ;; pressure variable
]

;; procedure that setup up initial variables
to setup
  clear-all
  set going-down? true
  set pressure-history []
  set initspeed 10.0
  set initmass 1.0
  set raw-width  round (0.01 * box-width  * max-pxcor)
  set raw-height round (0.01 * box-height * max-pycor)
  set piston-position 0.75 * raw-height
  set area ((4 * raw-width) + (2 * (piston-position + raw-height)))
  set volume ((2 * raw-width) * (piston-position + raw-height))
  make-box
  draw-piston
  ;;set vclock 0
  ;; create the gas molecules
  crt number [
     set new-speed initspeed
     set mass initmass
     random-position
     set shape "circle"
     recolor
     set pressure 0
  ]
  update-variables
  reset-ticks
  setup-histograms
end

;; updates variables after every time tick
to update-variables
  ;; Gas Variables
  ask turtles
    [ set speed new-speed
      set energy (0.5 * mass * speed * speed) ]
  set average count turtles with [ color = green ]
  set slow    count turtles with [ color = blue  ]
  set fast    count turtles with [ color = red   ]
  set avg-speed  mean [ speed  ] of turtles
  set avg-energy mean [ energy ] of turtles

  ;; System Variables
  calculate-pressure
  set vsplit (round (max [speed] of turtles * 1.2))
end

;; procedure that runs the model
to go
  ask turtles [bounce]
  ask turtles [move]
  ask turtles [check-for-collision]
  ;; control the piston's motion
  if piston-position < (-0.75 * raw-height)
  [ set going-down? false ]
  if piston-position > (0.75 * raw-height)
  [ set going-down? true ]
  ifelse going-down?
  [ piston-down piston-speed / vsplit ]
  [ piston-up piston-speed / vsplit ]

  set vclock vclock + 1
  ifelse (vclock = vsplit)
  [
    tick
    set vclock 0
    update-variables
    do-plotting
    do-histograms
  ]
  [ display ]
end


;; turtle procedure for bouncing off of the walls
to bounce
  ; if we're not about to hit a wall (yellow patch)
  ; or the piston (gray+2 patch),
  ; we don't need to do any further checks
  if ([pcolor] of patch-ahead 1 != yellow) and
     ([pcolor] of patch-ahead 1 != gray + 2) [ stop ]
  ; get the coordinates of the patch we'll be on if we go forward 1
  let new-px [pxcor] of patch-ahead 1
  let new-py [pycor] of patch-ahead 1
  ; check: hitting left or right wall?
  if (abs new-px = raw-width)
    ; if so, reflect heading around x axis
    [ set heading (- heading)
      set pressure pressure + abs (dx * mass * speed)
    ]
  ; check: hitting piston or bottom wall?
  if (abs new-py = raw-height) or (new-py = round piston-position)
    ; if so, reflect heading around y axis
    [ set heading (180 - heading)
      set pressure pressure + abs (dy * mass * speed)
    ]
end

;; turtle procedure that moves all the particles
to move
  jump (speed / vsplit)
end

;; turtle procedure to check to see if two particles collide
to check-for-collision
  if count other turtles-here = 1
    [ set tmp-turtle one-of other turtles-here
      if ((who > [who] of tmp-turtle) and (turtle2 != tmp-turtle))
        [ collide ]
    ]
end

;; turtle procedure for when two particles collide
to collide
  get-turtle2-info
  calculate-velocity-components
  set-new-speed-and-headings
end

;; turtle gets mass and speed info from turtle it is colliding with
to get-turtle2-info
  set turtle2 tmp-turtle
  set mass2 [mass] of turtle2
  set speed2 [new-speed] of turtle2
  set heading2 [heading] of turtle2
end

;; calculates new turtle velocity after the collision
to calculate-velocity-components
  set theta (random-float 360)
  set v1l (new-speed * sin (theta - heading))
  set v1t (new-speed * cos (theta - heading))
  set v2l (speed2 * sin (theta - heading2))
  set v2t (speed2 * cos (theta - heading2))
  let vcm (((mass * v1t) + (mass2 * v2t)) / (mass + mass2))
  set v1t (vcm + vcm - v1t)
  set v2t (vcm + vcm - v2t)
end

;; set new speed and headings of each turtles that has had a collision
to set-new-speed-and-headings
  set new-speed sqrt ((v1t * v1t) + (v1l * v1l))
  set heading (theta - (atan v1l v1t))

  let new-speed2 sqrt ((v2t * v2t) + (v2l * v2l))
  let new-heading (theta - (atan v2l v2t))
  ask turtle2 [
    set new-speed new-speed2
    set heading new-heading
  ]

  recolor
  ask turtle2 [ recolor ]
end

to recolor  ;; turtle procedure
  ifelse new-speed < (0.5 * initspeed)
    [ set color blue ]
    [ ifelse new-speed > (1.5 * initspeed)
        [ set color red ]
        [ set color green ] ]
end

;; patch procedure to make a box
to make-box
  ask patches with [ ((abs pxcor = raw-width) and (abs pycor <= raw-height)) or
                     ((abs pycor = raw-height) and (abs pxcor <= raw-width)) ]
    [ set pcolor yellow ]
end

;; turtle procedure to give turtles a random position within the confined area
to random-position
  setxy ((1 - raw-width)  + random-float (2 * raw-width - 2))
        ((1 - raw-height) + random-float (raw-height + piston-position - 2))
end


;; ------ Piston ----------
to piston-up [dist]
  if (dist > 0)
  [ ifelse ((piston-position + dist) < raw-height - 1)
    [ undraw-piston
      set piston-position (piston-position + dist)
      draw-piston ]
    [ undraw-piston
      set piston-position (raw-height - 1)
      draw-piston ]
    set volume ((2 * raw-width) * (piston-position + raw-height))
    set area ((4 * raw-width) + (2 * (piston-position + raw-height)))
  ]
end

to piston-down [dist]
  if (dist > 0)
  [ ifelse (piston-position - dist) > (2 - raw-height)
    [ undraw-piston
      set piston-position (piston-position - dist)
      ask turtles
      [ if (ycor >= (piston-position - 1))
        [ bounce-off-piston ] ]
      draw-piston ]
    [ undraw-piston
      set piston-position (3 - raw-height)
      ask turtles
      [ if (pycor >= 3 - raw-height)
        [ bounce-off-piston ] ]
      draw-piston ]
    set area ((4 * raw-width) + (2 * (piston-position + raw-height)))
    set volume ((2 * raw-width) * (piston-position + raw-height))
  ]
end

to draw-piston
  ask patches with [ ((pycor = (round piston-position)) and ((abs pxcor) < raw-width)) ]
    [ set pcolor gray + 2 ]
end

to undraw-piston
  ask patches with [ (pycor = round piston-position) and ((abs pxcor) < raw-width) ]
    [ set pcolor black ]
end

to bounce-off-piston  ;; Turtles procedure particle bounces off piston
  ifelse ((((2 * piston-position) - (ycor + 2)) < (1 - raw-height)) or
          (((2 * piston-position) - (ycor + 2)) > (piston-position - 2)))
   [ set ycor ((random (raw-height + piston-position - 2)) - (raw-height - 1)) ]
   [ set ycor ((2 * piston-position) - (ycor + 2)) ]
end

to calculate-pressure  ;; Observer procedure
  set total-pressure 100 * (sum [pressure] of turtles) / area
  ifelse (length pressure-history < scale)
  [ set pressure-history fput total-pressure pressure-history ]
  [ set pressure-history fput total-pressure but-last pressure-history ]
  set avg-pressure mean pressure-history
  ;; rezero pressures in preparation for the next cycle
  ask turtles [ set pressure 0 ]
end

;;; plotting procedures
to setup-histograms
  ;; Speed Histogram
  set-current-plot "Speed histogram"
  set-plot-x-range 0 (initspeed * 2)
  set-plot-y-range 0 ceiling (number / 6)
  set-current-plot-pen "average"
  set-histogram-num-bars 45
  set-current-plot-pen "fast"
  set-histogram-num-bars 45
  set-current-plot-pen "slow"
  set-histogram-num-bars 45

  ;; Energy histogram
  set-current-plot "Energy histogram"
  set-plot-x-range 0 (0.5 * (initspeed * 2) * (initspeed * 2) * initmass)
  set-plot-y-range 0 ceiling (number / 6)
  set-current-plot-pen "average"
  set-histogram-num-bars 45
  set-current-plot-pen "fast"
  set-histogram-num-bars 45
  set-current-plot-pen "slow"
  set-histogram-num-bars 45
end

;; does actual plotting (called in Go)
to do-plotting
  set-current-plot "Volume"
  plot volume
  set-current-plot "Pressure"
  plot avg-pressure
  set-current-plot "Temperature"
  plot avg-energy
  set-current-plot "Pressure vs. Volume"
  plotxy volume avg-pressure
  set-current-plot "Pressure * Volume"
  plot avg-pressure * volume / 1000
end

;; does actual histograms plotting (called in Go)
to do-histograms
  if (histogram?)
    [ histo-energy
      histo-speed ]
end

;; draw energy histogram
to histo-energy
  set-current-plot "Energy histogram"
  set-current-plot-pen "average"
  histogram [ energy ] of turtles with [ color = green ]
  set-current-plot-pen "slow"
  histogram [ energy ] of turtles with [ color = blue ]
  set-current-plot-pen "fast"
  histogram [ energy ] of turtles with [ color = red ]
  set-current-plot-pen "avg-energy"
  plot-pen-reset
  draw-vert-line avg-energy
end

;; draw speed histogram
to histo-speed
  set-current-plot "Speed histogram"
  set-current-plot-pen "average"
  histogram [ speed ] of turtles with [ color = green ]
  set-current-plot-pen "slow"
  histogram [ speed ] of turtles with [ color = blue ]
  set-current-plot-pen "fast"
  histogram [ speed ] of turtles with [ color = red ]
  set-current-plot-pen "avg-speed"
  plot-pen-reset
  draw-vert-line avg-speed
end

; draws a vertical line at xval on the current-plot with the current plot-pen
to draw-vert-line [xval]
  plotxy xval plot-y-min
  plot-pen-down
  plotxy xval plot-y-max
  plot-pen-up
end
@#$#@#$#@
GRAPHICS-WINDOW
433
10
767
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
100
10
187
43
go
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
9
10
91
43
setup
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
771
326
893
359
histogram?
histogram?
1
1
-1000

MONITOR
366
93
427
138
height
piston-position + raw-height
3
1
11

MONITOR
8
288
111
333
Pressure
avg-pressure
3
1
11

SLIDER
196
10
311
43
box-height
box-height
30
100
95
1
1
NIL
HORIZONTAL

SLIDER
311
10
427
43
box-width
box-width
20
80
75
1
1
NIL
HORIZONTAL

SLIDER
9
53
357
86
number
number
1
4000
500
1
1
particles
HORIZONTAL

PLOT
8
143
217
287
Volume
Time
Volume
0.0
20.0
0.0
4000.0
true
false
"" ""
PENS
"volume" 1.0 0 -6459832 true "" ""

PLOT
8
338
217
482
Pressure
Time
Pressure
0.0
20.0
0.0
3500.0
true
false
"" ""
PENS
"pressure" 1.0 0 -2674135 true "" ""

PLOT
771
10
1029
167
Speed histogram
Speed
Number
0.0
10.0
0.0
67.0
false
true
"" ""
PENS
"fast" 1.0 1 -2674135 true "" ""
"average" 1.0 1 -10899396 true "" ""
"slow" 1.0 1 -13345367 true "" ""
"avg-speed" 1.0 0 -7500403 true "" ""

PLOT
771
168
1029
325
Energy histogram
Energy
Number
0.0
200.0
0.0
67.0
false
true
"" ""
PENS
"fast" 1.0 1 -2674135 true "" ""
"average" 1.0 1 -10899396 true "" ""
"slow" 1.0 1 -13345367 true "" ""
"avg-energy" 1.0 0 -7500403 true "" ""

SLIDER
9
96
357
129
piston-speed
piston-speed
0
10
1
0.05
1
NIL
HORIZONTAL

PLOT
431
375
640
537
Temperature
Time
Temp
0.0
20.0
0.0
50.0
true
false
"" ""
PENS
"temperature" 1.0 0 -13345367 true "" ""

PLOT
218
303
427
482
Pressure * Volume
Time
PressureVolume
0.0
20.0
0.0
2000.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

PLOT
218
143
427
299
Pressure vs. Volume
Volume
Pressure
0.0
4000.0
0.0
3500.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

MONITOR
112
288
217
333
NIL
Volume
3
1
11

MONITOR
641
375
726
420
temperature
avg-energy
3
1
11

SLIDER
142
483
288
516
scale
scale
1
10
1
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This model simulates the behavior of gas particles as the volume changes. In this model, the volume is slowly changing over time by a piston that is rising and falling.  As the piston lowers, the volume of the box decreases and as the piston rises, the volume of the box increases.  This systematic motion of the piston does no work on the particles inside the box.  The piston only serves a mechanism to change the volume of the box.

The particles start with the same mass and speed upon the start of the simulation. The mass of the particles stays constant throughout the simulation, whereas, the speeds will change once particles start to collide.  Particles are in constant motion colliding with other particles and the walls. All collisions are modeled as elastic collisions, in that the total kinetic energy before and after the collision is conserved.  For example, when a fast moving particle collides with a slow moving particle, the fast moving particle will give some of its speed to the slow moving particle.  Therefore, the fast moving particle will leave the collision moving slower then when it entered the collision.  And the slow moving particle will speed up a bit.  The speed in a particle to particle collision is still conserved.  The collisions between a particle and a wall is modeled the same way.  When the particles hit the wall they transfer momentum to the wall.  After this transfer occurs, the particles then bounce off the wall with a different direction and speed. The system's pressure is calculated by averaging the number of collisions the particles have with the walls at each time step.

The Moving Piston model is one of a collection of GasLab models that use the same basic rules for expressing what happens when gas particles collide.  Each model in this collection has different features to show the different aspects of the Gas Laws.

Multiple adaptations of this model can be found in the Chemistry folder of the Curricular Models section under the names Chem Volume 1 and 2.  It is part of a suite of models used to teach students about the chemistry of the Gas Laws.

## HOW IT WORKS

The particles are modeled as single particles, all with the same mass and initial velocity.  Molecules are modeled as perfectly elastic particles with no internal energy except that which is due to their motion.  Collisions with the box and between molecules are elastic.  Particles are colored according to speed -- blue for slow, green for medium, and red for high speeds.

The exact way two particles collide is as follows:  
1. Two turtles "collide" if they find themselves on the same patch.  
2. A random axis is chosen, as if they were two billiard balls that hit and this axis was the line connecting their centers.  
3. They exchange momentum and energy along that axis, according to the conservation of momentum and energy.  This calculation is done in the center mass system.  
4. Each turtle is assigned its new speed, energy and heading.  
5. If a turtle finds itself on or very close to a wall of the container, it "bounces" -- that is, reflects its direction and keeps its same speed.

## HOW TO USE IT

### Buttons

SETUP - puts in the initial conditions you have set with the sliders.  Be sure to wait till the SETUP button stops before pushing GO.  
GO - runs the code again and again.  This is a "forever" button.

### Sliders

BOX-HEIGHT - height of the container  
BOX-WIDTH - width of the container  
NUMBER - number of particles  
PISTON-SPEED - rate of the piston  
SCALE - number of clock cycles over which to average the pressure

### Switch

HISTOGRAM? - turns histograms on or off

### Plots

VOLUME - plots the volume over time  
PRESSURE - plots the pressure over time  
PRESSURE VS. VOLUME - plots pressure over volume  
PRESSURE * VOLUME - plots the value of pressure * volume over time  
TEMPERATURE - plots the average temperature  
SPEED HISTOGRAM - illustrates the number of particles at their various speeds  
ENERGY HISTOGRAM - illustrates the number of particles at their various energy levels

### How to use it

Adjust the BOX-HEIGHT, BOX-WIDTH, NUMBER, and PISTON-SPEED variable before pressing SETUP.  The SETUP button will set the initial conditions.  The GO button will run the simulation.

In this model, though, the collisions of the piston with the particles are ignored. Note that there's a physical impossibility in the model here: in real life if you moved the piston down you would do work on the gas by compressing it, and its temperature would increase.  In this model, the energy and temperature are constant no matter how you manipulate the piston.  Nonetheless, the basic relationship between volume and pressure is correctly demonstrated here.

## THINGS TO NOTICE

How does the pressure change as the volume of the box changes?  Compare the two plots of volume and pressure.

How does the pressure change as the shape of the box changes?

Measure changes in pressure and volume. Is there a clear quantitative relationship?

How can the relationship between pressure and volume be explained in terms of the collisions of molecules?

How does more particles change the relationship between pressure and volume?

What shapes do the energy and speed histograms reach after a while?  Why aren't they the same?  Do the pressure and volume affect these shapes?

## THINGS TO TRY

How would you calculate pressure?  How does this code do it?

Change the number, mass, and initial velocity of the particles.  Does this affect the pressure?   Why? Do the results make intuitive sense?  Look at the extremes:  very few or very many molecules,  high or low volumes.

Figure out how many molecules there *really* are in a box this size --- say a 10-cm cube.  Look up or calculate the *real* mass and speed of a typical molecule.  When you compare those numbers to the ones in the model, are you surprised this model works as well as it does?

## EXTENDING THE MODEL

Are there other ways one might calculate pressure?

Create an isothermal piston example where the user can manually move the piston to any level in the box.

Add in a temperature variable that allows for the particles to move the piston to the appropriate volume.

## NETLOGO FEATURES

Notice how collisions are detected by the turtles and how the code guarantees that the same two particles do not collide twice.  What happens if we let the patches detect them?

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

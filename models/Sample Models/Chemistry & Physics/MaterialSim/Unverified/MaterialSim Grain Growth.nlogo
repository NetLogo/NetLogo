;; two different materials or phases
breed [ element1s element1 ]  ;; element1 is the main material
breed [ element2s element1 ]  ;; element2 is the materials which is
;; dispersed inside element1 (second-phase particles)


element1s-own
[
  neighbors-6   ;; agentset of 6 neighboring cells
  n             ;; used to store a count of second-phase neighbors
]

turtles-own
[
  temp                 ;; atom's temperature
  neighboring-turtles  ;; agentset of surrounding atoms
  sides-exposed        ;; number of sides exposed to walls  (between 0 and 4)
]

globals
[
  logtime                 ;; log of time
  colors                  ;; used both to color turtles, and for histogram
  xmax                    ;; max x size
  ymax                    ;; max y size
  intercepts              ;; used to calculate grain size (1/intercepts = average grain size)
  average-grain-size      ;; average grain size
  logaverage-grain-size   ;; log of average grain size (for plotting)
  orientation-for-intercept-count  ;; for grain size calculation (normally 90 degrees)
  initial-loggrain-size   ;; For grain growth exponent calculation and graphing
  initial-logtime         ;; For grain growth exponent calculation and graphing
  grain-growth-exponent   ;; Grain growth exponent
  total-atoms            ;; Total number of atoms in the system
]


to setup-hex-grid
  ;; setup the hexagonal grid in which atoms will be placed
  ;; and creates turtles
  set-default-shape element2s "square"

  ask patches
  [
    sprout 1
    [
      ifelse (fraction-element2 > random-float 100)
      ;; if there is a second element, create the corresponding atoms
      [
        ;; element2 is the fixed second-phase particle
        set breed element2s
        set color white
        set heading 360
      ]
      [
        ;; element1 is the main material, which grains growth
        set breed element1s
        set shape atom-shape
        set color random 139
        set heading color
      ]

      ;; shift even columns down
      if pxcor mod 2 = 0
      [ set ycor ycor - 0.5 ] ] ]

  ; ask element1s [ set shape3d "sphere" ]
  ; ask element2s [ set shape3d "cube" ]
  ; the two above lines are for NetLogo 3D. Uncomment them if you use that version.

  ;; now set up the neighbors6 agentsets

  ask element1s
  ;; define neighborhood of atoms
  [ ifelse pxcor mod 2 = 0
      [ set neighbors-6 element1s-on patches at-points [[0 1] [1 0] [1 -1] [0 -1] [-1 -1] [-1 0]] ]
    [ set neighbors-6 element1s-on patches at-points [[0 1] [1 1] [1  0] [0 -1] [-1  0] [-1 1]] ] ]
end

;; makes initial box for image import
to makes-initial-box
  setup-hex-grid
  reset-ticks
end

;; makes initial box for random arrangement
to makes-initial-box-random
  clear-all
  setup-hex-grid
  reset-ticks
end

;; import image into turtles
to import-image
  clear-all
  let file user-file
  if file = false [ stop ]
  ;; imports image into patches
  import-pcolors file
  ;; generates a white border around the image to avoid wrapping
  ;; converts the square grid to an hex grid
  makes-initial-box

  ;; transfers the image to the turtles. Rounds the color values to be integers.
  ask turtles
  [
    set color round pcolor
    set heading color
  ]

  ;; erases the patches (sets their color back to black),
  ask patches [ set pcolor black ]
  reset-ticks
end


to define-neighboring-turtles
  ;; defines neighboring turtles

  ask turtles
  [
    set neighboring-turtles (turtles at-points [[-1  1] [ 0  1] [1  1]
            [-1  0] [ 0  0] [1  0]
          [-1 -1] [ 0 -1] [1 -1]])


  ]

end

to grain-count
  ;; count number of grains based on the number of linear intercepts

  set orientation-for-intercept-count 90 ;; direction of intercepts count
  set intercepts 0
  set total-atoms count turtles

  ;;  ask patches

  ask turtles
  [
    ;; checks if turtle is before the last 'x' column and 'y' row
    if  ((xcor != (xmax - 1)) and (who < ((width * height) - 1)) and (who < total-atoms))
    [
      ;; checks if there is a turtle to the right for the intercept calculation
      let target-patch patch-at-heading-and-distance orientation-for-intercept-count 1
      ifelse target-patch != nobody and any? turtles-on target-patch
      [
        ;; If there is a turtle, checks if the heading is different.
        let right-neighbor-heading [heading] of
        (one-of
            turtles-on (patch-at-heading-and-distance
                orientation-for-intercept-count
                1))
        if (heading != (right-neighbor-heading))
        [
          ;; If heading is different, add 1 to 'intercepts'.
          set intercepts (intercepts + 1)
        ]
      ]
      [
        ;; if there is no turtle, simply add 1 to 'intercepts'.
        ;; A turtle/nothing interface is considered as grain boundary.
        set intercepts (intercepts + 1)
      ]
    ]
  ]
  ifelse intercepts = 0
    [set average-grain-size (total-atoms)] ;; grain size = area of the whole sample (to avoid division by zero)
    [set average-grain-size ((total-atoms) / intercepts)] ;; grain size = area / grain-grain interface
end

to do-plots

  set-current-plot "Grain Size (log-log)"
  plotxy logtime logaverage-grain-size

end


to go
  ;;initiates grain growth

  set total-atoms count turtles

  if average-grain-size >= total-atoms [stop]
  ;; stops when there is just one grain

  repeat (total-atoms)
  [
    ;;limits grain growth to element1, element2 represent the stationary second-phase particles
    ask one-of element1s [grain-growth]
  ]

  ;; advance Monte Carlo Steps (simulation time)
  ;; one Monte Carlo Step represents 'n' reorientation attemps,
  ;; where 'n' is the total number of atoms
  tick

  if remainder ticks measurement-frequency = 0
  ;; calculates grain size at a given frequency

  [
    set logtime log ticks 10
    grain-count
    if average-grain-size != 0
    [
      set logaverage-grain-size (log (average-grain-size) 10)
    ]
    ;; grain growth is better plotted on log-log scale
    do-plots
    if ticks = 20 [
      set initial-logtime logtime
      set initial-loggrain-size logaverage-grain-size
    ]
    ;; only initiates grain size calculation after MCS = 20
    if ticks > 20 [
      ;; calculate the angular coefficient of the grain growth curve
      ;; since it is a log-log plot, it's the grain growth exponent
      set grain-growth-exponent (-1 * ((logaverage-grain-size - initial-loggrain-size) /
              (initial-logtime - logtime)))
    ]
  ]

end

;; Grain growth procedure - free energy minimization
;; if another random crystallographic heading minimizes energy, switches headings, otherwise keeps the same.
to grain-growth
  ;; calculates the PRESENT free energy
  let present-heading (heading)
  let present-free-energy count neighbors-6 with [heading != present-heading]

  ;; chooses a random orientation
  let future-heading ([heading] of (one-of neighbors-6))

  ;; calculates the FUTURE free energy, with the random orientation just chosen
  let future-free-energy count neighbors-6 with [heading != future-heading]

  ;; compares PRESENT and FUTURE free-energies; the lower value "wins"
  ifelse future-free-energy <= present-free-energy
    [set heading future-heading]
    [if (annealing-temperature > random-float 100) [set heading (future-heading)]]
  ;; this last line simulates thermal agitation (adds more randomness to the simulation)

  ;;update the color of the atoms
  set color heading
end

;; drawing procedure
to turtle-draw
  if mouse-down?     ;; reports true or false to indicate whether mouse button is down
  [
    ask patch mouse-xcor mouse-ycor
      [ask turtles in-radius brush-size [set color draw-color set heading color]]
    display
  ]
end

;; in the drawing mode, erases the whole "canvas" with red
to erase-all

  ask turtles [if pcolor != white [set color red set heading color]]

end
@#$#@#$#@
GRAPHICS-WINDOW
477
10
879
433
24
24
8.0
1
10
1
1
1
0
0
0
1
-24
24
-24
24
1
1
1
ticks
60

SLIDER
234
37
462
70
width
width
3.0
501.0
45
2.0
1
atoms
HORIZONTAL

SLIDER
235
73
462
106
height
height
3.0
501.0
45
1.0
1
atoms
HORIZONTAL

TEXTBOX
12
17
205
35
(1) Simulation starting point
11
0.0
0

SLIDER
235
154
464
187
annealing-temperature
annealing-temperature
0.0
100.0
0
1.0
1
%
HORIZONTAL

BUTTON
237
313
392
352
measure grains now
grain-count
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
8
403
260
591
Grain Size (log-log)
log (time)
log (avg grain size)
0.0
1.0
0.0
1.0
true
false
"" ""
PENS
"average-grain-size" 1.0 0 -16777216 false "" ""

MONITOR
265
406
367
451
Grain Size
average-grain-size
3
1
11

BUTTON
11
325
111
365
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

SLIDER
235
190
463
223
fraction-element2
fraction-element2
0.0
10.0
0
1.0
1
NIL
HORIZONTAL

BUTTON
9
36
200
69
start with random arrangement
makes-initial-box-random
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
9
78
200
111
import image
import-image
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
265
498
367
543
Log Grain Size
logaverage-grain-size
2
1
11

MONITOR
265
452
367
497
Log time
logtime
2
1
11

MONITOR
265
544
368
589
Growth exponent
grain-growth-exponent
2
1
11

TEXTBOX
11
305
150
323
(4) Run simulation
11
0.0
0

TEXTBOX
236
132
326
150
Special features
11
0.0
0

SLIDER
235
270
423
303
measurement-frequency
measurement-frequency
1.0
100.0
1
1.0
1
NIL
HORIZONTAL

TEXTBOX
238
18
328
36
Simulation size
11
0.0
0

CHOOSER
10
140
111
185
atom-shape
atom-shape
"hex" "hexline" "thin-line" "line" "circ" "square" "spikes90" "default"
0

BUTTON
115
140
200
185
apply shape
ask turtles [set shape atom-shape]
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
10
214
79
247
draw
turtle-draw
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
253
79
296
erase all
erase-all
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
82
214
200
247
brush-size
brush-size
1.0
6.0
4
1.0
1
NIL
HORIZONTAL

TEXTBOX
11
195
101
213
(3) Draw grains
11
0.0
0

CHOOSER
82
252
200
297
draw-color
draw-color
45 55 85 105 15
0

TEXTBOX
237
249
367
267
Grain measurement
11
0.0
0

TEXTBOX
9
383
177
401
Grain size plot and calculations
11
0.0
0

TEXTBOX
10
120
186
138
(2) Change the shape of atoms
11
0.0
0

BUTTON
117
325
194
365
go once
go\n
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

Most materials are not continuous arrangements of atoms, but rather composed of thousands or millions of microscopic crystals, known as grains.  This model shows how the configuration and sizes of these grains change over time.  Grain size is a very important characteristic for evaluating the mechanical properties of materials; it is exhaustively studied in metallurgy and materials science.

Usually this kind of study is made by careful analysis and comparison of pictures taken in microscopes, sometimes with the help of image analysis software.  Recently, as the processing power of computers has increased, a new and promising approach has been made possible: computer simulation of grain growth.  Anderson, Srolovitz et al. proposed the most widely known and employed theory for computer modeling and simulation of grain growth, using the Monte Carlo method.  Instead of considering the grains as spheres, and being obliged to make numerous geometrical approximations, Anderson proposed that the computer would simulate the behavior of each individual atom in the system. Each atom would follow a very simple rule: it will always try to have, in its immediate neighborhood, as many atoms as possible with the same orientation as it.

This model is part of the MaterialSim (Blikstein & Wilensky, 2004) curricular package. To learn more about MaterialSim, see http://ccl.northwestern.edu/materialsim/.

## HOW IT WORKS

The basic algorithm of the simulation is simple: atoms are trying to be as stable as possible.  Their stability is based on the number of equal neighbors: the more equal neighbors (i.e. atoms with the same orientation) an atom has, the more stable it is.  If it has many different neighbors, it is unstable, and not likely to be in that position for long, because during the simulation atoms will try to relocate to more stable positions.  Therefore, the steps are:

1) Choose a random atom.
2) Calculate its present energy (which is correlated with the stability).  This calculation is done by simply counting the number of different neighbors.
3) Randomly choose a new orientation for the chosen atom, amongst the orientations of its neighbors.  We still don't know if that new attempted orientation will be maintained.  We have to calculate the energy in this new situation in order to know.
4) Calculate the free energy of the chosen element with the new, tentative orientation.  Again, we just count the number of different neighbors.
5) Comparison of the two values for free energy: the lowest value "wins", i.e., the less different neighbors an atom have, more stable it is.
6) Repeat steps 1-6.

The ANNEALING-TEMPERATURE slider controls the probability of maintaining an re-orientation which yields more instability.  The FRACTION-ELEMENT-2 slider defines the percentage of second-phase particles to be created when the user setups the simulation.  Those particles and not movable and are not subject to grain growth. Atoms see those particles as a different neighbor.

Note that the actual number of atoms is small compared to a real metal sample.  Also, real materials are three-dimensional, while this model is 2D.

## HOW TO USE IT

(1) Simulation starting point:
IMPORT IMAGE: Resets the simulation, and imports an image file in the JPG, BMP, GIF or PNG file formats.  The image will be automatically resized to fit into the world, but maintaining its original aspect ratio. Note that the image MUST HAVE THE SAME ASPECT RATIO AS THE WORLD. In other words, if the world is square, the image should be square as well. Prior to importing the image, it is recommended to clean it up using an image editing software (increase contrast, remove noise).  Try to experiment various combinations of values for the WIDTH and HEIGHT sliders, the view's size and the patch size to get the best results.
START WITH RANDOM ARRANGEMENT: Resets the simulation, and starts it with a random orientation for each atom.
GO-ONCE: Runs the simulation, one time step at a time.
GO: Runs the simulation continuously until either the GO button is pressed again, or just one grain survives.

(2) Change the shape of the atoms
The ATOM-SHAPE chooser has many different shapes, such as circle, hexagon, line, circle with spikes, thin line, and square.
After choosing the ATOM-SHAPE, click on APPLY to change the shape.  This can also be done during the simulation.

(3) Draw grains
You can draw grains with the mouse, using different brush sizes and colors.  The DRAW button activates drawing, the ERASE ALL button erases the view and sets all the atoms to red, the BRUSH-SIZE slider controls the radius of the brush and the DRAW-COLOR chooser changes the numeric value of the drawing color.

(4) Run simulation
GO: runs the simulation continuously
GO ONCE: runs the simulation, one step at a time.

### Simulation size

WIDTH: x (horizontal) dimension of the sample.
HEIGHT: y (vertical) dimension of the sample

### Special features

ANNEALING-TEMP: changes the probability of non-favorable orientation flips to happen.  A 10% value, for instance, means that 10% of non-favorable flips will be maintained.  This mimics the effect of higher temperatures.
FRACTION-ELEMENT2: This slider controls the amount of dispersed second-phase particles throughout the sample. Those particles slow down or stop grain growth.

### Grain measurement

MEASUREMENT-FREQUENCY: to increase the model's speed, the user can choose not to calculate grain size at every time step.  If grain size is calculated at every ten time units (20, 30, 40 etc.), the performance is slightly increased.  This only affects the plot and the monitors, but not the actual simulation.
MEASURE GRAINS NOW: if the MEASUREMENT-FREQUENCY is too large, and the user wants to evaluate grain size at a specific moment, this button can be used. Note that this does not alter the plot.

### Plots and monitors

Grain Size (log-log): Grain size vs. time, in a log-log scale.  Under normal conditions (ANNEALING-TEMP = 0 and FRACTION-ELEMENT-2 = 0), this plot should be a straight line with an angular coefficient of approximately 0.5.
SIMULATION TIME and LOG TIME: time steps of the simulation so far (and its log)
GRAIN SIZE and LOG GRAIN SIZE: grain size (is atoms) and its log.
GROWTH EXPONENT: the angular coefficient of the GRAIN SIZE plot.  This number should approach 0.5 with ANNEALING-TEMP = 0 and FRACTION-ELEMENT2 = 0.

## THINGS TO NOTICE

When you setup with a random orientation and run the simulation, notice that the speed of growth decreases with time.  Toward the end of the simulation, you might see just two or three grains that fight with each other for along time.  One will eventually prevail, but this logarithmic decrease of speed is an important characteristic of grain growth.  That is why the GRAIN SIZE plot is a straight line in a "log-log" scale.
Notice also that if you draw two grains, one concave and one convex, their boundary will tend to be a straight line, if you let the simulation run long enough.  Every curved boundary is unstable because many atoms at its interface will have more different than equal neighbors.

## THINGS TO TRY

Increase the value of the ANNEALING-TEMP slider. What happens to the GRAIN SIZE plot, and to the boundaries' shapes?

Try to increase the FRACTION-ELEMENT2 slider to 5%.  Then press START WITH RANDOM ARRANGEMENT and GO.  What happens to grain growth? Now try several values (1, 3, 5, 7, 9%), for instance.  What happens with the final grain size? What about the GRAIN SIZE plot and the GROWTH EXPONENT?

One advanced use of this model would be to get a digital picture of a real metallic sample, reduce noice and increase contrast with image editing programs, and load into this model using the IMPORT IMAGE button.  Don't forget to update the WIDTH and HEIGHT sliders and the view's size to accommodate the picture, and also to change the patch size in order to be able to see the whole sample.

## EXTENDING THE MODEL

This models assumes that the misorientation between two grains has no effect on their growth rates.  Two grains with a very similar crystallographic orientation have the same growth rate as grains which orientations differ by a lot.  Try to take the angular misorientation into consideration.

When we insert second-phase particles, all of them have the same size.  Try to create a slider that changes the size of the particles.

## NETLOGO FEATURES

This model uses some special features:
It uses a hexagonal grid (as opposed to a square one)
it uses different shapes for different visualization purposes
and it uses the `import-pcolors` primitive for image import.

## RELATED MODELS

Crystallization Basic
Crystallization Directed

## CREDITS AND REFERENCES

This model is part of the MaterialSim (Blikstein & Wilensky, 2004) curricular package. To learn more about MaterialSim, see http://ccl.northwestern.edu/materialsim/.

Two papers describing the use of this model in education are:
Blikstein, P. & Wilensky, U. (2005) Less is More: Agent-Based Simulation as a Powerful Learning Tool in Materials Science.  The IV International Conference on Autonomous Agents and Multiagent Systems. Utrecht, Netherlands.

Blikstein, P. & Wilensky, U. (2004) MaterialSim: An agent-based simulation toolkit for Materials Science learning. (PDF, 1.5 MB) Proceedings of the International Conference on Engineering Education. Gainesville, Florida.

The core algorithm of the model was developed at the University of Sao Paulo and published in:  Blikstein, P. and Tschiptschin, A. P. Monte Carlo simulation of grain growth (II). Materials Research, Sao Carlos, 2 (3), p. 133-138, jul. 1999.
Available for download at: http://www.blikstein.com/paulo/documents/papers/BliksteinTschiptschin-MonteCarlo-MaterialsResearch1999.pdf. See also http://www.pmt.usp.br/paulob/montecarlo for more information (in Portuguese).
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

circ
true
0
Circle -7500403 true true 10 11 278

circle
false
0
Circle -7500403 true true 0 0 300

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

hex
false
0
Polygon -7500403 true true 0 150 75 30 225 30 300 150 225 270 75 270

hexline
true
0
Polygon -7500403 true true 0 150 75 30 225 30 300 150 225 270 75 270
Rectangle -1 true false 121 47 182 252

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
Rectangle -7500403 true true 135 0 165 315

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

rectangle
true
0
Polygon -7500403 true true 67 36 67 262 235 262 235 35

spikes90
true
0
Circle -7500403 true true 61 62 177
Line -7500403 true 135 66 131 154
Rectangle -7500403 true true 135 -4 166 68
Rectangle -7500403 true true 142 132 219 134
Rectangle -7500403 true true 196 136 304 165
Rectangle -7500403 true true -9 135 68 166
Rectangle -7500403 true true 131 176 132 226
Rectangle -7500403 true true 135 225 165 313

square
false
0
Rectangle -7500403 true true 15 15 286 285
Line -1 false 6 6 293 6
Line -1 false 293 6 293 293
Line -1 false 293 292 8 292
Line -1 false 8 292 8 6

square 2
false
0
Rectangle -7500403 true true 30 30 270 270
Rectangle -16777216 true false 60 60 240 240

star
false
0
Polygon -7500403 true true 151 1 185 108 298 108 207 175 242 282 151 216 59 282 94 175 3 108 116 108

t
true
0
Rectangle -7500403 true true 46 47 256 75
Rectangle -7500403 true true 135 76 167 297

target
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60

thin-line
true
0
Line -7500403 true 150 0 150 300

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
makes-initial-box-random
repeat 50 [ go ]
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

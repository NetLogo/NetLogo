breed [bugs bug]
breed [players player]
breed [birds bird]
breed [vision-cones vision-cone]


bugs-own [speed vision]
birds-own [speed target eaten vision]


globals [
    total-caught                 ;; keeps track of total number of bugs caught
    total-speed-6-caught         ;; keeps track of the number of bugs caught with speed of 6
    total-speed-5-caught         ;; keeps track of the number of bugs caught with speed of 5
    total-speed-4-caught         ;; keeps track of the number of bugs caught with speed of 4
    total-speed-3-caught         ;; keeps track of the number of bugs caught with speed of 3
    total-speed-2-caught         ;; keeps track of the number of bugs caught with speed of 2
    total-speed-1-caught         ;; keeps track of the number of bugs caught with speed of 1
    old-color-map                ;; keeps track of the previous value of the SPEED-COLOR-MAP chooser
    histogram-interval-size      ;; the value of the interval size for each bar of the histogram
    max-vision                   ;; upper limit for the maximum vision allowable for birds or bugs
    max-speed                    ;; upper limit for the maximum speed allowable for birds or bugs
    old-show-initial-bug-vision-cone?
    old-vision-cone-distance
    avg-bug-speed
    avg-bird-speed
    avg-bug-vision
    avg-bird-vision
    reproduce-birds-after-eating
    speed-factor                 ;; scalar used to adjust the speed of the all the bugs to make the catching of bugs appropriately difficult for different speed computers
  ]


;;;;;;;;;;;;;;;;;;;;;
;; Setup Procedures
;;;;;;;;;;;;;;;;;;;;;


to setup
  ca
  set total-caught 0
  set histogram-interval-size 1
  set old-color-map speed-color-map   ;; holds the starting value of the chooser
  set old-show-initial-bug-vision-cone? 0
  set old-vision-cone-distance initial-bug-vision
  set reproduce-birds-after-eating 25
  set speed-factor 0.05
  set max-speed 10
  set max-vision 10

  ask patches [ set pcolor white ]   ;; white background
  create-bugs number-bugs [ set speed initial-bug-speed attach-vision-cone]

  ask bugs [
    set vision initial-bug-vision
    set shape "bug"
    setxy random-xcor random-ycor
  ]

  ;; the player breed contains one turtle that is used to represent
  ;;  a player of the bugs (a bird)
  create-players 1 [
    set shape "bird"
    set color brown
    set hidden? true
  ]

  create-birds number-birds [
    set vision initial-bird-vision
    set shape "bird-stationary"
    set color brown
    set hidden? false
    setxy random 100 random 100
    set speed initial-bird-speed
    attach-vision-cone
  ]

  ask bugs [set-colors]
  ask vision-cones [set-visualize-vision-cone]
  reset-ticks
  do-plots
end


to attach-vision-cone
  let parent-vision vision
  hatch 1 [

    set breed vision-cones
    create-link-from myself [tie]
    set shape "vision cone"
    set color gray

    set size parent-vision

    set-visualize-vision-cone

  ]
end


;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures
;;;;;;;;;;;;;;;;;;;;;


to go
  check-color-map-change
  check-visualize-vision-cone-change
  check-player-caught
  check-bird-catch
  move-player
  move-bugs
  move-birds
  reproduce-birds

  tick
  update-variables
  do-plots

end


to update-variables
  ifelse any? bugs
    [set avg-bug-speed mean [speed] of bugs   set avg-bug-vision mean [vision] of bugs ]
    [set avg-bug-speed 0]
  ifelse any? birds
    [set avg-bird-speed mean [speed] of birds    set avg-bird-vision mean [vision] of birds]
    [set avg-bird-speed 0]
end

to reproduce-birds
  let worst-bird nobody
  if (total-caught mod  reproduce-birds-after-eating = 0 and total-caught > 0 and any? birds) [
    set worst-bird min-one-of birds [eaten]
    ask worst-bird [
       ask out-link-neighbors [set color red die]
       die]
    reproduce-one-bird
  ]
end

to move-bugs
  let target-heading 0
  let candidate-predators nobody
  let predator nobody
  let all-predators (turtle-set birds players)
  ;; the speed factor is a scaling number used to adjust the amount all the bugs move
  ;; for example a speed-factor of 2, scales the speed of all the bugs so they are moving twice as fast
  ;; it is a useful slider to change for slower and faster computers, that might have the bugs
  ;; as a whole population moving too fast or too slow across the screen
  ask bugs [
    fd (speed * speed-factor)

    ifelse any? all-predators in-cone vision 120 [
       set candidate-predators all-predators in-cone vision  120

       if bug-flee-strategy = "any" and any? candidate-predators
         [set predator one-of candidate-predators]
       if bug-flee-strategy = "nearest" and any? candidate-predators
         [set predator min-one-of candidate-predators [distance myself]]

       set target-heading 180 + towards predator

       set heading target-heading
       set label-color black
       set label "!"
    ]
    [wiggle set label ""]

  ]
end

to move-birds
  let prey-agent nobody
  let candidate-bugs nobody
  let closest-bug nobody
  let assigned-target? false
  ;; the speed factor is a scaling number used to adjust the amount all the bugs move
  ;; for example a speed-factor of 2, scales the speed of all the bugs so they are moving twice as fast
  ;; it is a useful slider to change for slower and faster computers, that might have the bugs
  ;; as a whole population moving too fast or too slow across the screen
  ask birds [
    set candidate-bugs bugs in-cone initial-bird-vision  120

    ifelse any? candidate-bugs [
      set closest-bug min-one-of  candidate-bugs [distance myself]
      if (target = nobody and bug-pursuit-strategy = "lock on one") [
        set prey-agent closest-bug
        set target prey-agent
        set heading towards prey-agent
        set label-color red - 2
        set label "!"
        set assigned-target? true
      ]

      if (bug-pursuit-strategy = "closest" and target != closest-bug) [
        set prey-agent closest-bug
        set target prey-agent
                set heading towards prey-agent
        set label-color red - 2
        set label "!"
        set assigned-target? true
      ]

      if (assigned-target? != false) [
        set target nobody
        set label ""
        wiggle
      ]
    ]
    [
      set target nobody
      set label ""
      wiggle
    ]
    fd (speed * speed-factor)
  ]

end

to wiggle
  if wiggle? [
    right (random-float 30 * .05 / speed-factor)
    left (random-float 30 * .05 / speed-factor)
  ]
end

to move-player
  ifelse (mouse-inside?)
    [ ask players [ setxy mouse-xcor mouse-ycor set hidden? false] ]
    [ ask players [ set hidden? true]]
end

to check-player-caught
  let speed-of-caught 0
  let local-bugs 0
  ;; the mouse may move while we are doing calculations
  ;; so keep track of the current mouse position so
  ;; we do all the calculations with the same numbers
  let snap-mouse-xcor mouse-xcor
  let snap-mouse-ycor mouse-ycor
  if mouse-down? and mouse-inside? [
    if (any? bugs-on patch snap-mouse-xcor snap-mouse-ycor) [
      set local-bugs count bugs-on patch snap-mouse-xcor snap-mouse-ycor
      set total-caught (total-caught + local-bugs)
      ;; eat only one of the bugs at the mouse location
      ask n-of local-bugs bugs-on patch snap-mouse-xcor snap-mouse-ycor [
        set speed-of-caught speed
        if (speed-of-caught = 1) [ set total-speed-6-caught (total-speed-6-caught + 1) ]
        if (speed-of-caught = 2) [ set total-speed-5-caught (total-speed-5-caught + 1) ]
        if (speed-of-caught = 3) [ set total-speed-4-caught (total-speed-4-caught + 1) ]
        if (speed-of-caught = 4) [ set total-speed-3-caught (total-speed-3-caught + 1) ]
        if (speed-of-caught = 5) [ set total-speed-2-caught (total-speed-2-caught + 1) ]
        if (speed-of-caught = 6) [ set total-speed-1-caught (total-speed-1-caught + 1) ]
        ask out-link-neighbors [set color red die]
        die
      ]
      repeat local-bugs [reproduce-one-bug]  ;; replace the eaten bug with a random offspring from the remaining population
    ]
  ]
end


to check-bird-catch
  let speed-of-caught 0
  ask birds [
   if (any? bugs-here) [
      set total-caught (total-caught + 1)
      set eaten (eaten + 1)
      ;; eat only one of the bugs at the mouse location
      ask one-of bugs-here [
        set speed-of-caught speed
        if (speed-of-caught = 1) [ set total-speed-6-caught (total-speed-6-caught + 1) ]
        if (speed-of-caught = 2) [ set total-speed-5-caught (total-speed-5-caught + 1) ]
        if (speed-of-caught = 3) [ set total-speed-4-caught (total-speed-4-caught + 1) ]
        if (speed-of-caught = 4) [ set total-speed-3-caught (total-speed-3-caught + 1) ]
        if (speed-of-caught = 5) [ set total-speed-2-caught (total-speed-2-caught + 1) ]
        if (speed-of-caught = 6) [ set total-speed-1-caught (total-speed-1-caught + 1) ]
         ask out-link-neighbors [set color red die]
        die

      ]
      set target nobody
      reproduce-one-bug  ;; replace the eaten bug with a random offspring from the remaining population
    ]
  ]
end

;; reproduce one identical offspring from one
;; of the bugs remaining in the population
to reproduce-one-bug
  ask one-of bugs [
    hatch 1 [
    mutate-offspring-bug
    set heading (random-float 360)

    attach-vision-cone
    ]
  ]
end

to reproduce-one-bird
  let bird-energy-split 0
  if count birds > 0 [ask one-of birds [
    set bird-energy-split (eaten / 2)
    set eaten bird-energy-split
    hatch 1 [
    mutate-offspring-bird
    set heading (random-float 360)

    attach-vision-cone
    ]
  ]
  ]
end

to mutate-offspring-bug
  ifelse random 2 = 0
    [set vision (vision + random-float bug-vision-mutation)]
    [set vision (vision - random-float bug-vision-mutation)]

  if vision > max-vision [set vision max-vision]
  if vision < 0 [set vision 0]

  ifelse random 2 = 0
    [set speed (speed + random-float bug-speed-mutation )]
    [set speed (speed - random-float bug-speed-mutation )]

  if speed > max-speed [set speed max-speed]
  if speed < 0 [set speed 0]
end


to mutate-offspring-bird
  ifelse random 2 = 0
    [set vision (vision + random-float bird-vision-mutation )]
    [set vision (vision - random-float bird-vision-mutation )]

  if vision > max-vision [set vision max-vision]
  if vision < 0 [set vision 0]

  ifelse random 2 = 0
       [set speed (speed + random-float bird-speed-mutation)]
       [set speed (speed - random-float bird-speed-mutation)]

  if speed > max-speed [set speed max-speed]
  if speed < 0 [set speed 0]

end

;;;;;;;;;;;;;;;;;;;;;
;; Visualization Procedures
;;;;;;;;;;;;;;;;;;;;;

;; apply color map change only once each time
;; a new value for speed-color-map is selected
to check-color-map-change
  if (old-color-map != speed-color-map) [
    set old-color-map speed-color-map
    ask bugs [ set-colors ]
  ]
end


to check-visualize-vision-cone-change
  if (old-show-initial-bug-vision-cone? != show-vision-cone?) [
    set old-show-initial-bug-vision-cone? show-vision-cone?
    ask vision-cones [set-visualize-vision-cone]
  ]
  if (old-vision-cone-distance != initial-bug-vision) [
    set old-vision-cone-distance initial-bug-vision
    ask vision-cones [set-visualize-vision-cone]
  ]
end


to set-visualize-vision-cone
  let parent-vision [vision] of one-of in-link-neighbors
    ifelse show-vision-cone?
      [set hidden? false set size 2 * parent-vision]
      [set hidden? true set size 2 * parent-vision]

end

to set-colors ;; turtle procedure
  if (speed-color-map = "all green") [ set color green ]
  if (speed-color-map = "violet shades") [ recolor-shade  ]
  if (speed-color-map = "rainbow") [ recolor-rainbow ]
end


to recolor-shade
  ;; turtle procedure to set color of the bugs to various shapes of purple
  set color (111 + speed )
end

to recolor-rainbow ;; turtle procedure
  if (floor speed = 6) [ set color red ]
  if (floor speed = 5) [ set color orange ]
  if (floor speed = 4) [ set color (yellow - 1) ]  ;;  darken the yellow a bit for better visibility on white background
  if (floor speed = 3) [ set color green ]
  if (floor speed = 2) [ set color blue ]
  if (floor speed = 1) [ set color violet ]
  if (floor speed >= 7) [ set color gray - 2 ]
  if (floor speed < 1) [ set color gray + 2 ]
end

;;;;;;;;;;;;;;;;;;;;;;
;; Plotting Procedures
;;;;;;;;;;;;;;;;;;;;;;

to do-plots
  if ticks mod 100 = 1
  [
    set-current-plot "Avg. Vision vs. Time"
    set-current-plot-pen "bugs"

    if any? bugs [plotxy ticks avg-bug-vision]
    set-current-plot-pen "birds"
    if any? birds [plotxy ticks avg-bird-vision]

    set-current-plot "Avg. Speed vs. Time"
    set-current-plot-pen "bugs"
    if any? bugs [plotxy ticks avg-bug-speed]
    set-current-plot-pen "birds"
    if any? birds [plotxy ticks avg-bird-speed]

    set-current-plot "Speed of Bugs"
    plot-histograms-bugs-speed

    set-current-plot "Vision of Bugs"
    plot-histograms-initial-bug-vision

    set-current-plot "Speed of Birds"
    plot-histograms-initial-bird-speed

    set-current-plot "Vision of Birds"
    plot-histograms-initial-bird-vision
  ]
end

to plot-caught
  set-current-plot-pen "speed=1"
  plotxy ticks total-speed-1-caught
  set-current-plot-pen "speed=2"
  plotxy ticks total-speed-2-caught
  set-current-plot-pen "speed=3"
  plotxy ticks total-speed-3-caught
  set-current-plot-pen "speed=4"
  plotxy ticks total-speed-4-caught
  set-current-plot-pen "speed=5"
  plotxy ticks total-speed-5-caught
  set-current-plot-pen "speed=6"
  plotxy ticks total-speed-6-caught
end

to plot-populations

  set-current-plot-pen "speed=1"
  plot (count bugs with [ speed = 1 ])
  set-current-plot-pen "speed=2"
  plot (count bugs with [ speed = 2 ])
  set-current-plot-pen "speed=3"
  plot (count bugs with [ speed = 3 ])
  set-current-plot-pen "speed=4"
  plot (count bugs with [ speed = 4 ])
  set-current-plot-pen "speed=5"
  plot (count bugs with [ speed = 5 ])
  set-current-plot-pen "speed=6"
  plot (count bugs with [ speed = 6 ])
end

to plot-histograms-bugs-speed
  ;; creates 6 different histograms of different colors in the same graph
  ;; each histogram is color coded to the color mapping for when the
  ;; SPEED-COLOR-MAP chooser is set to "rainbow" value.


  set-histogram-num-bars 10
  set-current-plot-pen "#"
  set-plot-pen-interval histogram-interval-size
  histogram [ speed ] of bugs ;;with [speed >= 0 and speed < 1]

end

to plot-histograms-initial-bug-vision
  ;; creates 6 different histograms of different colors in the same graph
  ;; each histogram is color coded to the color mapping for when the
  ;; SPEED-COLOR-MAP chooser is set to "rainbow" value.

  set-histogram-num-bars 10
  set-current-plot-pen "#"
  set-plot-pen-interval (histogram-interval-size )
  histogram [ vision ] of bugs

end


to plot-histograms-initial-bird-speed
  ;; creates 6 different histograms of different colors in the same graph
  ;; each histogram is color coded to the color mapping for when the
  ;; SPEED-COLOR-MAP chooser is set to "rainbow" value.

  set-histogram-num-bars 10
  set-current-plot-pen "#"
  set-plot-pen-interval (histogram-interval-size )
  histogram [ speed ] of birds

end

to plot-histograms-initial-bird-vision
  ;; creates 6 different histograms of different colors in the same graph
  ;; each histogram is color coded to the color mapping for when the
  ;; SPEED-COLOR-MAP chooser is set to "rainbow" value.

  set-histogram-num-bars 10
  set-current-plot-pen "#"
  set-plot-pen-interval (histogram-interval-size )
  histogram [ vision ] of birds

end
@#$#@#$#@
GRAPHICS-WINDOW
548
10
958
441
12
12
16.0
1
10
1
1
1
0
1
1
1
-12
12
-12
12
1
1
1
ticks
30

MONITOR
90
52
168
97
total caught
total-caught
0
1
11

BUTTON
11
16
87
49
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

BUTTON
90
16
167
49
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

PLOT
6
373
185
493
Avg. Vision vs. Time
time
vision
0.0
1000.0
0.0
10.0
true
false
"" ""
PENS
"bugs" 1.0 0 -16777216 true "" ""
"birds" 1.0 0 -6459832 true "" ""

PLOT
185
253
364
373
Speed of Bugs
speed
frequency
0.0
10.0
0.0
50.0
true
false
"" ""
PENS
"#" 0.1 1 -16777216 true "" ""
"pen1" 0.1 1 -8630108 true "" ""
"pen2" 0.1 1 -13345367 true "" ""
"pen3" 0.1 1 -10899396 true "" ""
"pen4" 0.1 1 -3355648 true "" ""
"pen5" 0.1 1 -955883 true "" ""
"pen6" 0.1 1 -2674135 true "" ""

CHOOSER
429
16
541
61
speed-color-map
speed-color-map
"all green" "rainbow" "violet shades"
2

PLOT
6
253
185
373
Avg. Speed vs. Time
time
rate
0.0
1000.0
0.0
10.0
true
false
"" ""
PENS
"birds" 1.0 0 -6459832 true "" ""
"bugs" 1.0 0 -16777216 true "" ""

PLOT
364
253
542
373
Speed of Birds
speed
frequency
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"#" 1.0 1 -6459832 true "" ""
"speed=1" 1.0 0 -8630108 true "" ""
"speed=2" 1.0 0 -13345367 true "" ""
"speed=3" 1.0 0 -10899396 true "" ""
"speed=4" 1.0 0 -3355648 true "" ""
"speed=5" 1.0 0 -955883 true "" ""
"speed=6" 1.0 0 -2674135 true "" ""

MONITOR
11
52
87
97
alive bugs
(count bugs)
0
1
11

SLIDER
11
97
144
130
number-bugs
number-bugs
1
100
60
1
1
NIL
HORIZONTAL

SLIDER
12
179
147
212
number-birds
number-birds
0
10
10
1
1
NIL
HORIZONTAL

SLIDER
12
215
198
248
initial-bird-speed
initial-bird-speed
0
10
2
1
1
NIL
HORIZONTAL

SLIDER
200
215
383
248
initial-bird-vision
initial-bird-vision
0
5
0
.5
1
NIL
HORIZONTAL

SLIDER
199
132
382
165
initial-bug-vision
initial-bug-vision
0
6
0
.5
1
NIL
HORIZONTAL

SWITCH
271
16
427
49
show-vision-cone?
show-vision-cone?
0
1
-1000

SWITCH
170
16
269
49
wiggle?
wiggle?
0
1
-1000

SLIDER
386
180
541
213
bird-vision-mutation
bird-vision-mutation
0
1
0
.1
1
NIL
HORIZONTAL

SLIDER
386
215
541
248
bird-speed-mutation
bird-speed-mutation
0
1
0
.1
1
NIL
HORIZONTAL

SLIDER
385
98
541
131
bug-vision-mutation
bug-vision-mutation
0
1
0
.5
1
NIL
HORIZONTAL

PLOT
185
373
364
493
Vision of Bugs
vision
frequency
0.0
10.0
0.0
50.0
true
false
"" ""
PENS
"#" 1.0 1 -16777216 true "" ""

PLOT
364
373
542
493
Vision of Birds
vision
birds
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"#" 1.0 1 -6459832 true "" ""

SLIDER
385
133
541
166
bug-speed-mutation
bug-speed-mutation
0
1
0
.1
1
NIL
HORIZONTAL

CHOOSER
696
444
839
489
bug-pursuit-strategy
bug-pursuit-strategy
"lock on one" "nearest"
1

CHOOSER
552
444
690
489
bug-flee-strategy
bug-flee-strategy
"any" "nearest"
1

SLIDER
9
131
195
164
initial-bug-speed
initial-bug-speed
0
10
2
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This is a natural/artificial selection model that shows the interrelationship between natural selection forces in predator and prey that results from coevolution.

The model shows how genetic variability affects evolutionary processes and outcomes for the speed and sight of predators and prey.  The model demonstrates many different series of reciprocal changes between a predator (birds) and prey (bugs) which each act as an agent of natural selection for each other, particularly related to the successive adaptation of the bird for pursuing its prey and the bugs for fleeing its prey.

By also adjusting the 1) initial values for speed and vision in predators (birds) and prey (bugs) and the size and shape of the world 2) the rate at which offspring in the population of birds and bugs incur mutations in genetics, various evolutionary outcomes will emerge from the coevolution of the birds and bugs (slow moving bugs vs. fast moving bugs, optimally sighted bugs, etc...).

## HOW IT WORKS

Predator (bird) and prey (bug) populations coevolve as the selective pressures they exert on one another favor different attributes in vision and speed over time.

Press SETUP to create a population of bugs, determined by the six times the INITIAL-BUGS-EACH-SPEED slider.  The bugs that are created are randomly distributed around the world and assigned a speed.

When you press GO the bugs begin to move at their designated speeds.  As they move around any birds on the screen will try to eat as many bugs as they can by running into them.

The six different speeds that a bug might move at are distributed amongst six different sub-populations of the bugs.  These speeds are genetically inherited.  The bugs also have a sight value.  This sight value is the distance within which the bug can see birds in their cone of vision (120 degrees arc) AND for which they respond to the presence of the bird, by turning around to head in the opposite direction.  These vision values are genetically inherited.  With each bug eaten (by a bird), a new bug is randomly chosen from the population to reproduce one offspring.  This bug's new speed is copied from the parent and then randomly changed up or down by a small amount (determined by the BUG-SPEED-MUTATION slider) and the bug's new vision is copied from the parent and then randomly changed up or down by a small amount (determined by the BUG-VISION-MUTATION slider). The creation of this new offspring keeps the overall population of the bugs constant.

The number of birds that are in the world is determined by the INITIAL-BIRDS slider.  The birds have an initial speed (INITIAL-BIRD-SPEED) and initial vision (INITIAL-BIRD-VISION) values.  The birds respond to bugs in their vision cone by turning toward the bug they select using the specified BUG-PURSUIT-STRATEGY.  The speed and vision of the birds, like the bugs, is genetically inherited.  Variation is offspring speed and vision is controlled by BIRD-SPEED-MUTATION and BIRD-VISION-MUTATION.  An update to the bird population occurs after a fixed number of bugs is eaten by the total population.  At this point the bird who has eaten the least number of bugs since the last bird population update is removed (representing death from starvation), and a new bird is copied from an existing individual in the population (representing a new birth).  The creation of this new offspring keeps the overall population of the birds constant.

Initially there are equal numbers of each sub-population of bug (e.g. ten bugs at each of the 6 speeds).  This is the only inherited attribute that has a distribution of different values.  The other attributes (vision for bugs and birds, and speed for birds) has only a single starting value for all of the members of the population.
Over time, however, as bugs are eaten and new bugs are born and birds die and new birds are born, the distribution of the vision and speed for both birds and bugs will change as shown in the histograms.  In some scenarios you might see the speed distribution move to the left, representing that more slow individuals are surviving.  In other scenarios you may see the vision histogram shift to the right, representing that more far sighted individuals are surviving.  In some scenarios you will see the histogram shift toward some intermediate value and then remain relatively stable with a maximum number of individuals having that value in the histogram.  Comparing the proportions of individuals with different values for vision and speed in the histograms to the average values of these attributes for the population, will help you make important links between how the population is evolving and how the individuals are being selected for or against, and how proportions of traits in the population are changing over time.

In addition to the automated predators (the birds), you may also assume the role of an additional predator amongst a population of bugs.  To join in the pursuit of bugs as another bird simply use your mouse in the WORLD & VIEW to click on bugs and eat them as they move.  As they move around, try to eat as many bugs as fast as you can by clicking on them.  Alternatively, you may hold the mouse button down and move the mouse pointer over the bugs.

## HOW TO USE IT

INITIAL-BUGS-EACH-SPEED is the number of bugs you start with in each of the six sub-populations.  The overall population of bugs is determined by multiplying this value by 6.

INITIAL-BIRDS is the number of birds that you start with.

INITIAL-BIRD-SPEED is the speed that each bird starts with.

INITIAL-BIRD-VISION and INITIAL-BUG-VISION is the radius the vision cones that the birds and bugs initially start with.

BIRD-VISION-MUTATION, BIRD-SPEED-MUTATION, BUG-VISION-MUTATION, BUG-SPEED-MUTATION are each sliders that control the amount of maximum possible random mutation (up or down) in the value of the speed or vision that occur in the offspring.

SPEED-COLOR-MAP settings help you apply or remove color visualization to the speed of the bugs.  The "all green" setting does not show a different color for each bug based on its speed".  Keeping the color settings switched to something besides "all green" can tend to result in the predator (the user) unconsciously selecting bugs based on color instead of speed.
The "rainbow" setting shows 6 distinct colors for the 6 different speeds a bug might have.  These color settings correspond to the plot pen colors in the graphs.
The "purple shades" setting shows a gradient of dark purple to light purple for slow to fast bug speed.

SHOW-VISION-CONE? switch helps you visualize the vision detection and response cone of the birds and bugs.

BUG-FLEE-STRATEGY sets which predator the bug will turn away from - "nearest" or "any".
BIRD-PURSUIT-STRATEGY sets which bug the bird will turn toward - "nearest" or "lock on one" (which means the bird keeps following the same bug until it is either outside its vision cone or it catches it.

## THINGS TO NOTICE

The SPEED-OF-BUGS histogram tends to shift right if you assume the role of chasing easy prey and the average speed of the bugs increases over time.

The SPEED-OF-BUGS histogram tends to shift left if you assume the role of waiting for prey come to you and the average speed of the bugs decreases over time.  (The same effect is achieved with moving the mouse around the view randomly)

## THINGS TO TRY

Set the model up with INITIAL-BUGS-EACH-SPEED set to one and INITIAL-BIRDS to 0.  Slow the model down and watch where new bugs come from when you eat (by clicking the mouse on) a bug.  You should see a new bug hatch from one of the five remaining and it should be moving at the same speed as its parent.

Chase bugs around trying to catch the bug nearest you at any one time by holding the mouse button down and moving the cursor around the view after the nearest bug.

Try using the model with just you as the predator and INITIAL-BIRDS set to 0.  Watch how the bugs evolve over time in response to just you as a predator.

Then, watch how the bugs evolve in response to automated birds.

In each of these scenarios, use the "follow nearest" strategy setting in the choosers unless specified and set INITIAL-BIRDS to 10.

If you start with stationary birds (INITIAL-BIRD-SPEED 0) and the BIRD-SPEED-MUTATION slider to 0 and do not permit bug sight to evolve (BUG-VISION-MUTATION 0), but all other mutation sliders to .5 you will notice that over time that the bugs will evolve into very slow movers and bird sight will not change in a preferred direction.

If you start with moving birds (e.g. INITIAL-BIRD-SPEED 2) and no-sight birds and bugs (INITIAL-BUG-VISION 0 and INITIAL-BIRD-VISION 0), and do not permit bug sight to evolve (BUG-VISION-MUTATION 0) notice that the again, evolve into very slow movers at first, and then a bit later far seeing birds evolve.

If you start with moving birds and bugs with no sight, but allow the sights of both to mutate (but not speed), both birds and bugs will co-evolve their sight to a higher mid-level optimal value for sight in bugs and maximum sight in birds.

If you fix the vision of birds and bugs to 2 and also start with fast birds (10), the spread between the speed of bird and bugs will result in rapid evolution to slow moving bugs and then prevent any further evolution of speed OR vision of the bugs to higher levels.

If you fix the vision of birds and bugs to 2 and the start with a bird speed of 4 , but allow both bug and bird speed to evolve, this will result in coevolution toward maximum speed for both.

If you fix the vision fix the vision of both birds and bugs to 2 and start with a bird speed of 4 and allow everything to evolve, you will get speed co-evolution toward maximum, a short term evolution of short-sighted bugs at first, and then a turn around in direction of evolution toward a higher optimal mid-level of sight.

If you change the strategy of the predator to "lock-on-one" and repeat the previous experiment, the vision of the bugs evolves to a different optimal mid-level of sight than before (lower in value e.g. 3.5 vs. 5).  There will be a slower response in the speed maximization of birds and bugs, and little selective pressure on the vision of the birds (can be a wide range of values).

If you repeat the last scenario but change the size and shape of the world, (e.g. a narrow rectangle), you will get a different optimal level for bug and bird sight.  And you may get some other emergent behavior in the overall population of the bugs.  For example world dimension of 4 x 16 will yield very low vision bugs and high vision birds and the emergence of a type of "herding" or "flocking" pattern in the population of bugs.

## EXTENDING THE MODEL

Include inheritance of bugs' and predators' avoidance and pursuit strategies.

Include a primary producer (plants) that bugs need to eat to survive.  Include energy as a way to model when bugs and predators die or reproduce (see WOLF SHEEP PREDATION model in models library).  Give the plants reproducible characteristics.

Include territory boundaries to create geographically isolated sections of the world (like islands and mountain ranges) to see if subpopulations evolve in each region (see GENE DRIFT T INTERACT models in the models library).

## RELATED MODELS

All BEAGLE Evolution models.  In particular:  Bug Hunt Speeds, Bug Hunt Camouflage, Wolf Sheep Predation, and GENE DRIFT T INTERACT

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

bird
true
0
Polygon -7500403 true true 151 170 136 170 123 229 143 244 156 244 179 229 166 170
Polygon -16777216 true false 152 154 137 154 125 213 140 229 159 229 179 214 167 154
Polygon -7500403 true true 151 140 136 140 126 202 139 214 159 214 176 200 166 140
Polygon -16777216 true false 151 125 134 124 128 188 140 198 161 197 174 188 166 125
Polygon -7500403 true true 152 86 227 72 286 97 272 101 294 117 276 118 287 131 270 131 278 141 264 138 267 145 228 150 153 147
Polygon -7500403 true true 160 74 159 61 149 54 130 53 139 62 133 81 127 113 129 149 134 177 150 206 168 179 172 147 169 111
Circle -16777216 true false 144 55 7
Polygon -16777216 true false 129 53 135 58 139 54
Polygon -7500403 true true 148 86 73 72 14 97 28 101 6 117 24 118 13 131 30 131 22 141 36 138 33 145 72 150 147 147

bird-stationary
true
0
Polygon -7500403 true true 151 170 136 170 123 229 143 244 156 244 179 229 166 170
Polygon -16777216 true false 152 154 137 154 125 213 140 229 159 229 179 214 167 154
Polygon -7500403 true true 151 140 136 140 126 202 139 214 159 214 176 200 166 140
Polygon -16777216 true false 151 125 134 124 128 188 140 198 161 197 174 188 166 125
Polygon -7500403 true true 152 86 227 72 286 97 272 101 294 117 276 118 287 131 270 131 278 141 264 138 267 145 228 150 153 147
Polygon -7500403 true true 160 74 159 61 149 54 130 53 139 62 133 81 127 113 129 149 134 177 150 206 168 179 172 147 169 111
Circle -16777216 true false 144 55 7
Polygon -16777216 true false 129 53 135 58 139 54
Polygon -7500403 true true 148 86 73 72 14 97 28 101 6 117 24 118 13 131 30 131 22 141 36 138 33 145 72 150 147 147
Circle -16777216 false false 2 2 295

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

vision cone
true
2
Polygon -955883 false true 150 150 30 60 60 30 90 15 150 0 210 15 240 30 270 60

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
0
@#$#@#$#@

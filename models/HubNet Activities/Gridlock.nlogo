;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variable declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

globals
[
  grid-x-inc  ;; the amount of patches in between two roads in the x direction
  grid-y-inc  ;; the amount of patches in between two roads in the y direction
  acceleration  ;; the constant that controls how much a car speeds up or slows down by if it is to accelerate or decelerate
  phase  ;; keeps track of the phase
  num-cars-stopped  ;; the number of cars that are stopped during a single pass thru the go procedure
  old-display-which-metric  ;; holds the value of display-which-metric for the last time through the go procedure

  ;; patch agentsets
  intersections  ;; agentset containing the patches that are intersections
  roads  ;; agentset containing the patches that are roads

  ;;quick start instructions variables
  quick-start  ;; the current quickstart instruction displayed in the quickstart monitor
  qs-item  ;; the index of the current quickstart instruction
  qs-items  ;; the list of quickstart instructions
]

turtles-own
[
  speed  ;; the speed of the turtle
  up-car?  ;; this will be true if the turtle moves downwards and false if it moves to the right
  wait-time  ;; the amount of time since the last time a turtle has moved
]

patches-own
[
  intersection?  ;; this is true if the patch is at the intersection of two roads
  accident?  ;; this is true if a crash has occurred at this intersection.  this will never be true for a non-intersection patch
  green-light-up?  ;; this is true if the green light is above the intersection.  otherwise, it is false.  this is only true for patches that are intersections.
  my-row  ;; this holds the row of the intersection counting from the upper left corner of the view.  it is -1 for patches that are not intersections.
  my-column  ;; this holds the column of the intersection counting from the upper left corner of the view.  it is -1 for patches that are not intersections.
  user-id  ;; this holds the user-id that corresponds to the intersection.  it is -1 for patches that are not intersections.
  my-phase  ;; this holds the phase for the intersection.  it is -1 for patches that are not intersections.
]


;;;;;;;;;;;;;;;;;;;;;
;; Setup Functions ;;
;;;;;;;;;;;;;;;;;;;;;

to startup
  setup
  setup-quick-start
  hubnet-reset
end

;; Initialize the display by giving the global and patch variables initial values.
;; Create num-cars of turtles if there are enough road patches for one turtle to be created per road patch.
;; Setup the plots
;; All code in setup is done if full-setup? is true.  If it is false, then it doesn't clear the information
;; about the users; users still retain the same light that they had before.
;; "setup false" is done by the re-run button.
to setup
  clear-output
  clear-turtles
  clear-all-plots

  let full-setup? ((grid-x-inc != (world-width / grid-size-x))
      or (grid-y-inc != (world-height / grid-size-y)))

  setup-globals

  ifelse full-setup?
  [
    let users map [[user-id] of ?] sort patches with [is-string? user-id]
    let phases map [[my-phase] of ?] sort patches with [is-string? user-id]
    clear-patches
    setup-patches
    setup-intersections
    ;; reassign the clients to intersections
    (foreach users phases
    [
      get-free-intersection ?1
      ask intersections with [ user-id = ?1 ]
        [ set my-phase ?2 ]
    ])
  ]
  [ setup-intersections ]

  set-default-shape turtles "car"

  if (number > count roads)
  [
    user-message (word  "There are too many cars for the amount of road.  "
    "or GRID-SIZE-Y sliders, or decrease the number of cars by lowering "
    "the NUMBER slider.\nThe setup has stopped.")
    stop
  ]

  ;; Now create the turtles and have each created turtle call the functions setup-cars and set-car-color
  crt number
  [
    setup-cars
    set-car-color
    record-data
  ]

  ;; give the turtles an initial speed
  ask turtles
  [ set-car-speed ]

  reset-ticks
end

;; Initialize the global variables to appropriate values
to setup-globals
  set phase 0
  set num-cars-stopped 0
  set grid-x-inc world-width / grid-size-x
  set grid-y-inc world-height / grid-size-y

  ;; don't make acceleration 0.1 since we could get a rounding error and end up on a patch boundary
  set acceleration 0.099
end

;; Make the patches have appropriate colors, setup the roads and intersections agentsets,
;; and initialize the traffic lights to one setting
to setup-patches
  ;; initialize the patch-own variables and color the patches to a base-color
  ask patches
  [
    set intersection? false
    set accident? false
    set green-light-up? true
    set my-row -1
    set my-column -1
    set user-id -1
    set my-phase -1

    set pcolor brown + 3
  ]

  ;; initialize the global variables that hold patch agentsets
  set roads patches with [ (floor ((pxcor + max-pxcor - floor(grid-x-inc - 1)) mod grid-x-inc) = 0) or
                           (floor ((pycor + max-pycor) mod grid-y-inc) = 0) ]
  set intersections roads with [ (floor ((pxcor + max-pxcor - floor(grid-x-inc - 1)) mod grid-x-inc) = 0) and
                                 (floor ((pycor + max-pycor) mod grid-y-inc) = 0) ]

  ask roads
  [ set pcolor white ]
end

;; Give the intersections appropriate values for the intersection?, my-row, and my-column
;; patch variables.  Make all the traffic lights start off so that the lights are red
;; horizontally and green vertically.
to setup-intersections
  ask intersections
  [
    set intersection? true
    set green-light-up? true
    set my-phase 0
    set my-row floor ((pycor + max-pycor) / grid-y-inc )
    set my-column floor ((pxcor + max-pxcor) / grid-x-inc )
    set-signal-colors
  ]
end

;; Initialize the turtle variables to appropriate values and place the turtle on an empty road patch.
to setup-cars  ;; turtle procedure
  set speed 0
  set wait-time 0

  put-on-empty-road

  ifelse intersection?
  [
    ifelse random 2 = 1
    [ set up-car? true ]
    [ set up-car? false ]
  ]
  [
    ifelse (floor ((pxcor + max-pxcor - floor(grid-x-inc - 1)) mod grid-x-inc) = 0)
    [ set up-car? true ]
    [ set up-car? false ]
  ]

  ifelse up-car?
  [ set heading 180 ]
  [ set heading 90 ]
end

;; Find a road patch without any turtles on it and place the turtle there.
to put-on-empty-road  ;; turtle procedure
  move-to one-of roads
  if any? other turtles-here
  [ put-on-empty-road ]
end

;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;

;; receives information from the clients and runs the simulation
to go
  ;; get commands and data from the clients
  listen-clients

  every delay
  [
    ;; clear any accidents from the last time thru the go procedure
    clear-accidents

    ;; if there are any intersections that are to switch automatically, have them change their color
    set-signals
    set num-cars-stopped 0

    ;; set the turtles speed for this time thru the procedure, move them forward their speed,
    ;; record data for plotting, and set the color of the turtles
    ;; to an appropriate color based on their speed
    ask turtles
    [
      set-car-speed
      fd speed
      record-data
      set-car-color
    ]
    ;; crash the cars if crash? is true
    if crash?
    [ crash-cars ]

    ;; update the clock and the phase
    clock-tick
  ]
end

;; reports the amount of seconds by which to slow the model down
to-report delay
  ifelse simulation-speed <= 0
  [ report ln (10 / 0.001) ]
  [ report ln (10 / simulation-speed) ]
end

;; have the traffic lights change color if phase equals each intersections' my-phase
to set-signals
  ask intersections with
  [ phase = floor ((my-phase * ticks-per-cycle) / 100) and ( auto? or user-id = -1 ) ]
  [
    set green-light-up? (not green-light-up?)
    set-signal-colors
  ]
end

;; This procedure checks the variable green-light-up? at each intersection and sets the
;; traffic lights to have the green light up or the green light to the left.
to set-signal-colors  ;; intersection (patch) procedure
  ifelse power?
  [
    ifelse green-light-up?
    [
      ask patch-at -1 0 [ set pcolor red ]
      ask patch-at 0 1 [ set pcolor green ]
    ]
    [
      ask patch-at -1 0 [ set pcolor green ]
      ask patch-at 0 1 [ set pcolor red ]
    ]
  ]
  [
    ask patch-at -1 0 [ set pcolor white ]
    ask patch-at 0 1 [ set pcolor white ]
  ]
end

;; set any intersection's color that had an accident back to white and make accident? false
to clear-accidents
  if crash?
  [
    ask patches with [accident?]
    [
      set pcolor white
      set accident? false
    ]
  ]
end

;; set the turtles' speed based on whether they are at a red traffic light or the speed of the
;; turtle (if any) on the patch in front of them
to set-car-speed  ;; turtle procedure
  ifelse pcolor = red
  [ set speed 0 ]
  [
    ifelse up-car?
    [ set-speed 0 -1 ]
    [ set-speed 1 0 ]
  ]
end

;; set the speed variable of the turtle to an appropriate value (not exceeding the
;; speed limit) based on whether there are turtles on the patch in front of the turtle
to set-speed [delta-x delta-y]  ;; turtle procedure
  let turtles-ahead turtles-on patch-at delta-x delta-y

  ;; if there are turtles in front of the turtle, slow down
  ;; otherwise, speed up
  ifelse any? turtles-ahead
  [
    let up-cars?-ahead [up-car?] of turtles-ahead
    ifelse member? up-car? up-cars?-ahead and member? (not up-car?) up-cars?-ahead
    [
      if not crash?
      [ set speed 0 ]
    ]
    [
      set speed [speed] of one-of turtles-ahead
      slow-down
    ]
  ]
  [ speed-up ]
end

;; decrease the speed of the turtle
to slow-down  ;; turtle procedure
  ifelse speed <= 0  ;;if speed < 0
  [ set speed 0 ]
  [ set speed speed - acceleration ]
end

;; increase the speed of the turtle
to speed-up  ;; turtle procedure
  ifelse speed > speed-limit
  [ set speed speed-limit ]
  [ set speed speed + acceleration ]
end

;; set the color of the turtle to a different color based on how fast the turtle is moving
to set-car-color  ;; turtle procedure
  ifelse speed < (speed-limit / 2)
  [ set color blue ]
  [ set color cyan - 2 ]
end

;; keep track of the number of stopped turtles and the amount of time a turtle has been stopped
;; if its speed is 0
to record-data  ;; turtle procedure
  ifelse speed = 0
  [
    set num-cars-stopped num-cars-stopped + 1
    set wait-time wait-time + 1
  ]
  [ set wait-time 0 ]
end

;; crash any turtles at the same intersection going in different directions
to crash-cars
  ask intersections with [any? turtles-here with [up-car?] and any? turtles-here with [not up-car?]]
  [
    set accident? true
    set pcolor orange
  ]
end

;; increases the clock by 1 and cycles phase to the next appropriate value
to clock-tick
  tick
  ;; The phase cycles from 0 to ticks-per-cycle, then starts over.
  set phase phase + 1
  if phase mod ticks-per-cycle = 0
  [ set phase 0 ]
end

to hide-or-show-pen [name-of-plot]
  ifelse plots-to-display = "All three plots" or plots-to-display = name-of-plot
  [ __plot-pen-show ]
  [ __plot-pen-hide ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Quick Start functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; instructions to quickly setup the model, and clients to run this activity
to setup-quick-start
  set qs-item 0
  set qs-items
  [
    "Teacher: Follow these directions to setup the HubNet activity."
    "Optional: Zoom In (see Tools in the Menu Bar)"
    "Change the traffic grid (using the sliders GRID-SIZE-X and..."
      "GRID-SIZE-Y) to make enough lights for everyone."
    "Change any other of the settings that you would like to change."
    "For example, if you plan on running Gridlock in..."
      "the MANUAL mode, be sure to have AUTO? set to OFF."
    "Press the SETUP button."
    "Press the LOGIN button."
    "Everyone: Open up a HubNet Client on your machine and..."
      "type your user name, select this activity and press ENTER."
    "Teacher: Once everyone is logged in and has a light..."
      "stop the LOGIN button by pressing it again."
    "Everyone: Whichever mode AUTO? is set for in NetLogo,..."
      "you will control your intersection in a different way."
    "If you have chosen MANUAL,..."
      "you can change the state of your light by pressing..."
        "the CHANGE LIGHT button."
    "If you have chosen AUTO,..."
      "you can change the phase of your light by moving..."
        "the PHASE slider to a different position."
    "Teacher: Once everyone is ready,..."
      "start the simulation by pressing the GO button."
    "Teacher: You may want to view some of the plots."
      "Do this by changing the PLOTS-TO-DISPLAY chooser,..."
        "which changes the plot displayed for everyone."
    "Choose No Plots to turn off all the plots..."
      "Choose Stopped Cars to see the STOPPED CARS plot..."
        "Choose 2 Speed of Cars the AVERAGE SPEED plot..."
          "Choose 3 Average Wait Time of Cars the AVERAGE WAIT plot..."
            "or Choose 'All three plots' for all the plots..."

    "Teacher: To run the activity again with the same group,..."
      "stop the model by pressing the GO button, if it is on."
        "Change any of the settings that you would like."
          "Press the SETUP button."
    "Teacher: Once everyone is ready,..."
      "restart the simulation by pressing the GO button."

    "Teacher: To start the simulation over with a new group,..."
      "stop the model by pressing the GO button, if it is on..."
      "Press the RESET button in the Control Center"
        "and follow these instructions again from the beginning."
  ]
  set quick-start (item qs-item qs-items)
end

;; view the next item in the quickstart monitor
to view-next
  set qs-item qs-item + 1
  if qs-item >= length qs-items
  [ set qs-item length qs-items - 1 ]
  set quick-start (item qs-item qs-items)
end

;; view the previous item in the quickstart monitor
to view-prev
  set qs-item qs-item - 1
  if qs-item < 0
  [ set qs-item 0 ]
  set quick-start (item qs-item qs-items)
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Code for interacting with the clients ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; when a command is sent, find out which client sent it and then execute the command
to listen-clients
  while [hubnet-message-waiting?]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [
      give-intersection-coords
      wait 1  ;; we want to give some time for other clients to log in on this round
    ]
    [
      ifelse hubnet-exit-message?
      [
        abandon-intersection
      ]
      [
        ifelse hubnet-message-tag = "Change Light"
          [ manual hubnet-message-source ]
        [
          if hubnet-message-tag = "Phase"
            [ auto hubnet-message-source ]
        ]
      ]
    ]
  ]
end

;; when a new client logs in, if there are free intersections,
;; assign one of them to that client
;; if this current-id already has an intersection, give the client that intersection.
to give-intersection-coords
  let current-id hubnet-message-source
  ifelse not any? intersections with [user-id = current-id]
  [
      ;; the case where they tried logging in previously but there was no room for them
      ;; or they haven't logged in before
      get-free-intersection current-id
  ]
  [
    ;; otherwise, we already have an intersection for the current-id.
    ;; all we need to do is send where the light is located at
    ask intersections with [user-id = current-id]
    [ hubnet-send current-id "Located At:" (word "(" my-column "," my-row ")") ]
  ]
end

;; when a client disconnects, free up its intersection
to abandon-intersection
  ask intersections with [user-id = hubnet-message-source]
  [
    set user-id -1
    set my-phase 0
    ask patch-at -1 1 [ set plabel ""]
  ]
end

;; if there are any free intersections, pick one of them at random and give it to the current-id.
;; if there are not any free intersections, toss an error and put error values into the list
to get-free-intersection [current-id]
  ifelse any? intersections with [user-id = -1]
  [
    ;; pick a random intersection that hasn't been taken yet
    ask one-of intersections with [user-id = -1]
    [
      set user-id current-id
      ask patch-at -1 1
      [
        set plabel-color black
        set plabel current-id
      ]
      hubnet-send current-id "Located At:" (word "(" my-column "," my-row ")")
    ]
  ]
  [
    hubnet-send current-id "Located At:" "Not enough lights"
    user-message word "Not enough lights for student with id: " current-id
  ]
end

;; switch the traffic lights at the intersection for the client with user-id
to manual [current-id]
  if not auto?
  [
    ask intersections with [user-id = current-id]
    [
      set green-light-up? (not green-light-up?)
      set-signal-colors
    ]
  ]
end

;; change the value of the phase for the intersection at (xc,yc) to
;; the value passed by the client
to auto [current-id]
  ask intersections with [user-id = current-id]
  [
    set my-phase hubnet-message
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
283
96
663
497
18
18
10.0
1
10
1
1
1
0
1
1
1
-18
18
-18
18
1
1
1
ticks

TEXTBOX
7
427
266
449
This chooser determines which plot is drawn.
11
0.0
0

BUTTON
94
237
176
270
login
listen-clients
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
565
500
844
689
Average Wait Time of Cars
Time
Average Wait
0.0
100.0
0.0
5.0
true
false
"" ""
PENS
"default" 1.0 0 -1893860 true "" "hide-or-show-pen  \"Average Wait Time of Cars\" \nplot mean [wait-time] of turtles"

PLOT
283
500
562
689
Average Speed of Cars
Time
Average Speed
0.0
100.0
0.0
1.0
true
false
"set-plot-y-range 0 speed-limit" ""
PENS
"default" 1.0 0 -1893860 true "" "hide-or-show-pen \"Average Speed of Cars\" \nplot  mean [speed] of turtles"

BUTTON
288
60
406
93
Reset Instructions
setup-quick-start
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
574
60
658
93
NEXT >>>
view-next
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
497
60
575
93
<<< PREV
view-prev
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
288
10
658
55
Quick Start Instructions- More in Info Window
quick-start
0
1
11

SLIDER
142
41
281
74
grid-size-y
grid-size-y
1
9
5
1
1
NIL
HORIZONTAL

SLIDER
1
41
139
74
grid-size-x
grid-size-x
1
9
5
1
1
NIL
HORIZONTAL

SWITCH
98
113
188
146
auto?
auto?
1
1
-1000

SWITCH
190
113
280
146
crash?
crash?
1
1
-1000

SWITCH
1
113
96
146
power?
power?
0
1
-1000

SLIDER
1
77
280
110
number
number
0
400
200
1
1
cars
HORIZONTAL

PLOT
1
500
280
689
Stopped Cars
Time
Stopped Cars
0.0
100.0
0.0
100.0
true
false
"set-plot-y-range 0 number" ""
PENS
"default" 1.0 0 -1893860 true "" "hide-or-show-pen \"Stopped Cars\"\nplot num-cars-stopped\n"

BUTTON
177
237
259
270
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
1
149
153
182
simulation-speed
simulation-speed
0
10
7.5
0.1
1
NIL
HORIZONTAL

SLIDER
155
149
280
182
speed-limit
speed-limit
0
1
1
0.1
1
NIL
HORIZONTAL

BUTTON
11
237
93
270
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

MONITOR
175
185
280
230
Current Phase
phase
3
1
11

SLIDER
1
201
173
234
ticks-per-cycle
ticks-per-cycle
1
100
20
1
1
NIL
HORIZONTAL

CHOOSER
6
450
176
495
plots-to-display
plots-to-display
"No plots" "Stopped Cars" "Average Speed of Cars" "Average Wait Time of Cars" "All three plots"
4

BUTTON
182
458
274
491
Refresh Plots
update-plots
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
WHAT IS IT?
-----------
Students control traffic lights in a real-time traffic simulation.  The teacher controls overall variables, such as the speed limit and the number of cars.  This allows students to explore traffic dynamics, which can lead into many areas of study, from calculus to social studies.

Challenge the students to develop strategies to improve traffic and discuss the different ways to measure the quality of traffic.

The coordinates for the traffic lights are based on the first quadrant of the Cartesian plane.  Therefore, the traffic light with the coordinates (0,0) is in the lowest row and the left-most column.  The traffic light above it has coordinates (0,1) and the traffic light to the right of it has (1,0).

For further documentation, see the Participatory Simulations Guide found at http://ccl.northwestern.edu/ps/


HOW TO USE IT
-------------
Quickstart Instructions:
------------------------
Contains instructions as to how to quickly setup the model, and clients to run this activity.  The instructions can be found below and can be seen progressively in the Quick Start instructions monitor in the Interface:

Teacher: Follow these directions to setup the HubNet activity.
Optional: Zoom In (see Tools in the Menu Bar)
Change the traffic grid (using the sliders GRID-SIZE-X and GRID-SIZE-Y) to make enough lights for everyone.
Change any other of the settings that you would like to change.  For example, if you plan on running Gridlock in the MANUAL mode, be sure to have AUTO? set to OFF.

Press the SETUP button.
Press the LOGIN button.

Everyone: Open up a HubNet Client on your machine and input the IP Address of this computer, type your user name in the user name box and press ENTER.
Teacher: Once everyone is logged in and has a light, stop the LOGIN button by pressing it again.

Everyone: Whichever mode AUTO? is set for in NetLogo, you will control your intersection in a different way:
If you have chosen MANUAL, you can change the state of your light by pressing the CHANGE LIGHT button.
If you have chosen AUTO, you can change the phase of your light by moving the PHASE slider to a different position.

Teacher: Once everyone is ready, start the simulation by pressing the GO button.

Teacher: You may want to view some of the plots.  Do this by changing the PLOTS-TO-DISPLAY chooser, which changes the plot displayed for everyone.
- Choose 'No Plots' to turn off all the plots.
- Choose 'Stopped Cars' to see the STOPPED CARS plot.
- Choose 'Average Speed of Cars' for the AVERAGE SPEED OF CARS plot.
- Choose 'Average Wait Time of Cars' for the AVERAGE WAIT TIME OF CARS plot.
- Choose 'All three plots' for all the plots.

Teacher: To run the activity again with the same group, stop the model by pressing the GO button, if it is on.  Change the values of the sliders and switches to the values you want for the new run.  Press the SETUP button.  Once everyone is ready, restart the simulation by pressing the GO button.

Teacher: To start the simulation over with a new group, stop the model by pressing the GO button, if it is on, press the RESET button in the Control Center and follow these instructions again from the beginning.

Buttons:
--------
SETUP - generates a new traffic grid based on the current GRID-SIZE-X and GRID-SIZE-Y and NUM-CARS number of cars.  This also clears all the plots.  If the size of the grid has changed the clients will be assigned to new intersections.
GO - runs the simulation indefinitely
LOGIN - allows users to log into the activity without running the model or collecting data
REFRESH PLOTS - redraws the plots based on the current value of PLOTS-TO-DISPLAY.  Useful for looking at different plots when GO is off.

Sliders:
--------
SPEED-LIMIT - sets the maximum speed for the cars
NUMBER - the number of cars in the simulation (you must press the SETUP or RE-RUN buttons to see the change)
SIMULATION-SPEED - the speed at which the simulation runs
TICKS-PER-CYCLE - sets the maximum value that the phase can be.  This has no effect when the model is run with AUTO? false.  Also, the phase that each user chooses is scaled to be less than or equal to this value.
GRID-SIZE-X - sets the number of vertical roads there are (you must press the SETUP button to see the change)
GRID-SIZE-Y - sets the number of horizontal roads there are (you must press the SETUP button to see the change)

Choosers:
---------
PLOTS-TO-DISPLAY - determines which plot is drawn in NetLogo:
- No Plots.
- STOPPED CARS
- AVERAGE SPEED OF CARS
- AVERAGE WAIT TIME OF CARS
- All three plots.

Switches:
---------
CRASH? - toggles car crashing
POWER? - toggles the presence of traffic lights
AUTO? - toggles between automatic mode, where the students' lights change on a cycle, and manual in which students directly control the lights with their clients. Lights which aren't associated with clients always change on a cycle.

Plots:
------
STOPPED CARS - displays the number of stopped cars over time
AVERAGE SPEED OF CARS - displays the average speed of cars over time
AVERAGE WAIT TIME OF CARS - displays the average time cars are stopped over time

Client Information
------------------
After logging in, the client interface will appear for the students.  The controls for manual and automatic mode are both included, but which one works is based on the setting of the AUTO? switch in NetLogo.  In MANUAL mode, click the CHANGE LIGHT button to switch the state of the light you control.  In AUTO mode, move the PHASE slider to change the phase for your light.  The phase determines what percent of the way through the cycle to switch on.


THINGS TO NOTICE
----------------
When cars have stopped at a traffic light, and then they start moving again, the traffic jam will move backwards even though the cars are moving forwards.  Why is this?  Discuss in your class possible reasons for this phenomena.


THINGS TO TRY
-------------
Try changing the speed limit for the cars.  How does this affect the overall efficiency of the traffic flow?  Are fewer cars stopping for a shorter amount of time?  Is the average speed of the cars higher or lower than before?

Try changing the number of cars on the roads.  Does this affect the efficiency of the traffic flow?

How about changing the speed of the simulation?  Does this affect the efficiency of the traffic flow?

Using HubNet, try running this simulation with AUTO? being true and AUTO? being false.  Is it harder to make the traffic move well using one scheme or the other?  Why?

Using HubNet, try running this simulation with AUTO? being true.  Try to find a way of setting the phases of the traffic lights so that the average speed of the cars is the highest.  Now try to minimize the number of stopped cars.  Now try to decrease the average wait time of the cars.  Is there any correlation between these different metrics?


EXTENDING THE MODEL
-------------------
Currently, the maximum speed limit (found in the SPEED-LIMIT slider) for the cars is 1.0.  This is due to the fact that the cars must look ahead the speed that they are traveling to see if there are cars ahead of them.  If there aren't, they speed up.  If there are, they slow down.  Looking ahead for a value greater than 1 is a little bit tricky.  Try implementing the correct behavior for speeds greater than 1.


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

stoplight
false
0
Circle -7500403 true true 30 30 240

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
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
VIEW
292
10
662
380
0
0
0
1
1
1
1
1
0
1
1
1
-18
18
-18
18

BUTTON
162
68
286
101
Change Light
NIL
NIL
1
T
OBSERVER
NIL
C

SLIDER
162
101
286
134
Phase
Phase
0
99
0
1
1
NIL
HORIZONTAL

TEXTBOX
12
10
154
175
Affect the state of your light by pressing the Change Light button, or changing the value of the Phase slider. The Phase slider controls the point in the cycle at which your light with change, and represents a percentage of the total cycle time.
11
0.0
0

MONITOR
173
10
286
59
Located At:
NIL
3
1

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

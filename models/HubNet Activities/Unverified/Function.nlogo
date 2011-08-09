globals
[
  margin                ;; constant width of border around the world
  width                 ;; width of the grid
  height                ;; height of the grid
  patches-per-grid-x    ;; the number of patches per grid units in the x-direction
  patches-per-grid-y    ;; the number of patches per grid units in the y-direction
  grids-per-patch-x     ;; number of grid units per patch in the x-direction
  grids-per-patch-y     ;; number of grid units per patch in the y-direction

  grid                  ;; agentset of all the patches that make up the grid area (all patches - border patches)
  x                     ;; the current x variable set when evaluating the equation so it can be used by runresult
  x-axis                ;; x-axis in patch coordinates
  y-axis                ;; y-axis in patch coordinates

  rule                  ;; the text of a verbal rule, given by the teacher
  student-index         ;; the index of which item in the equation-list is visible to the teacher
  graphed-equation-list ;; list of equations currently visible in the graph
                        ;; each item consists of a list, first item is equation string
                        ;; the second is the color it is graphed in.

  all-shapes            ;; a list of the combinations of turtle shapes and colors
  available-shapes      ;; to prevent reusing shapes
  all-colors
  color-names

  old-legend?           ;; old values so we know if we need to toggle
  old-grid?
  old-linear-regression?

  lin-reg-eq            ;; a string of the linear regression equation
  r-square              ;; r^2, from linear regression
  slope                 ;; slope of linear regression
  y-intercept           ;; y-intercept of linear regression
]

patches-own
[
  border?            ;; whether or not the patch is located in the border of the graph
]

breed [students student]
students-own
[
  user-id            ;; unique id, input by student when they log in, to identify each point turtle
  my-x               ;; the x value of the student's position on the graph
  my-y               ;; the y value of the student's position on the graph
  shape-combo        ;; the string describing the shape and color of the student's turtle
  step-size          ;; the amount by which each student's turtle moves in a direction
  my-equation        ;; the last equation turned in by the student
]


;;;;;;;;;;;
;; Setup ;;
;;;;;;;;;;;

to startup
  clear-all
  hubnet-reset
  set margin 10
  set rule ""
  set graphed-equation-list []
  set student-index -1
  set old-grid? grid?
  set old-legend? reveal-legend?
  set y-intercept ""
  set slope ""
  set r-square ""
  set color-names [ "red" "orange" "yellow" "brown" "green" "blue" "pink" "purple" ]
  set all-colors [ red orange yellow brown green blue pink violet ]
  set all-shapes [ "heart" "star" "circle" "diamond" "square" "rhombus" "triangle" "heart" ]
  setup
end

to setup
  clear-equations
  setup-grid
  set available-shapes all-shapes
  ask students
   [
     set my-x random-grid-coord-x
     set my-y random-grid-coord-y
   ]
end

;;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;;

to go
  every 0.1
  [
    if any? turtles
    [
      ask students [ send-info-to-clients ]
    ]
    listen-clients
    ;; keep the display in sync with the
    ;; switches
    if old-grid? != grid?
    [
      ifelse grid?
      [
        draw-grid
        draw-axes
      ]
      [ cd ]
      set old-grid? grid?
    ]
    if old-legend? != reveal-legend?
    [
      ask grid [ set plabel "" ]
      if reveal-legend?
        [ draw-legend ]
      set old-legend? reveal-legend?
    ]
    display
  ]
end

;;
;; Student Equation Procedures
;;
to graph-student-equation
  let s current-student true
  if s = nobody
  [ stop ]
  let graphed-colors map [ item 1 ? ] graphed-equation-list
  if member? [color] of s graphed-colors
  [
    set graphed-equation-list remove-item (position [color] of s graphed-colors) graphed-equation-list
    ask grid [ set pcolor black ]
    foreach graphed-equation-list [ graph-equation item 0 ? false item 1 ? ]
  ]
  graph-equation [my-equation] of s true [color] of s
end

;; change the equation submitted by a student
to edit-student-equation
  let student current-student false
  if student = nobody
  [ stop ]

  let eq user-input [my-equation] of student

  ask student [ set my-equation eq ]
end

;; return the student in index, position of the sorted list
;; of the student agentset
to-report current-student [ errors? ]
  if not any? students with [ my-equation != "" ]
  [
    if errors?
    [ user-message "There are no student equations to graph." ]
    report nobody
  ]
  if student-index < 0
  [
    if errors?
    [ user-message "Please select a student equation using the < and > buttons."  ]
    report nobody
  ]

 report item student-index sort students with [ my-equation != "" ]
end

to-report current-student-name
  let s current-student false
  ifelse s = nobody
  [ report s ]
  [ report [user-id] of s ]
end

to-report current-student-equation
  let s current-student false
  ifelse s = nobody
  [ report "" ]
  [ report [my-equation] of s ]
end

;; advance to the next student equation
to right-direction
  ifelse (student-index + 1 >= count students with [ my-equation != "" ] )
    [ set student-index 0 ]
    [ set student-index student-index + 1 ]
end

;; go back to the previous student equation
to left-direction
  ifelse student-index = 0
    [ set student-index (count students with [ my-equation != "" ] - 1) ]
    [ set student-index student-index - 1 ]
end

;;
;; Graph Equations Procedures
;;
to enter-equation
  let phrase ""
  set phrase user-input "Enter equation in terms of y = f(x)"
  graph-equation phrase true white
end

;; report the operator used in a submitted equation
;; used when we do the graphing
to-report operator [phrase]
  if member? ">=" phrase
  [ report ">=" ]
  if member? "<=" phrase
  [ report "<=" ]
  if member? ">" phrase
  [ report ">" ]
  if member? "<" phrase
  [ report "<" ]
  report "="
end

;; strip off the y=, y< etc so we can estimate
;; the equation without using runresult on every
;; patch
to-report phrase-without-operator [phrase op]
  set phrase remove (word "y " op) phrase
  set phrase remove (word "y" op) phrase
  set phrase remove (word op " y") phrase
  set phrase remove (word op "y") phrase
  report phrase
end

to graph-equation [full-phrase add-to-list? current-color ]
  ;; if the equation has "y =" in some permutation strip it off
  ;; be flexible about it's presence, position, and spacing.
  let op operator full-phrase
  let phrase phrase-without-operator full-phrase op
  let my-message ""
  let success? false

  ;; estimate the equation at every value of x
  ;; going across the grid.
  let delta-x grids-per-patch-x
  set x x-minimum
  ;; if we're not close enough in the y direction from the last
  ;; point in the function draw we need to detect this and make
  ;; the lines appear to be connected.
  let last-py 0
  let last-defined? false

  while [ x <= x-maximum ]
  [
    ;; we do this in carefully for two reasons
    ;; first, there may be a syntax error in the equation.
    ;; second, there might be some points for which the
    ;; equation is undefined.
    carefully
    [
      ;; estimate y at this value of x
      let y runresult phrase
      let py round patch-y y

      ;; if we're inside the grid ask the patch with the nearest x and y coords to set pcolor
      ;; to the color of this equation.
      ifelse y <= y-maximum and y >= y-minimum ;; if we're outside the viewable world don't plot anything
      [
        if member? op "=" ;; plot the actual function
        [
          ask patch round patch-x x py ;; color the patch that is closest to the correct y-value
          [                            ;; note that we have to convert from grid coords to patch coords
            set pcolor current-color
            ;; if the y values are too far apart the line will appear broken
            ;; so fill in between.  though if the last y value was not
            ;; not defined, (outside the range or mathematically undefined
            ;; don't connect the points.
            if abs (last-py - py) > 1 and last-defined?
            [
              sprout 1
              [
                ifelse last-py < py
                [ set heading 180 ]
                [ set heading 0 ]
                repeat abs ( last-py - py ) - 1
                [
                  fd 1
                  set pcolor current-color
                ]
                die
              ]
            ]
          ]
        ]
        set last-defined? true
      ]
      [ set last-defined? false ]

      ;; keep the y inside the grid
      let new-y  min list y-maximum (max (list y-minimum y))
      ;; if the operator has an inequality portion
      ;; fill in the rest of the column in the appropriate
      ;; direction.
      ifelse member? ">" op
      [
        ask patch (round patch-x x) (round patch-y new-y)
        [ fill-inequality 0 current-color ]
      ]
      [
        if member? "<" op
        [
          ask patch (round patch-x x) (round patch-y new-y)
           [ fill-inequality 180 current-color ]
        ]
      ]
      set last-py py
      set success? true
    ]
    [
      ;; this is a bit tricky because we don't know why it failed
      ;; could be a compiler error could be just undefined at this point.
      ;; keep track of the message and mark that this point was not
      ;; defined so we don't try to connect to it if the next point is.
      set my-message error-message
      set last-defined? false
    ]
    set x x + delta-x
  ]

  ;; if at least one point was plotted successfully there was
  ;; not a compiler error and add the equation to the list.
  ifelse success?
  [
    ;; manage the graphed equation information
    ;; keep the legend up to date.
    if add-to-list?
    [
      ;; each item in the graphed-equation-list is a list, first item is the
      ;; equation phrase and the second is the color it is displayed in.
      set graphed-equation-list lput (list (word "y " op phrase) current-color) graphed-equation-list
      if reveal-legend?
        [ draw-legend ]
    ]
  ]
  [
    ;; if no points were plotted show the user the message so
    ;; s/he can fix the problem.
    user-message my-message
  ]
end

;; given a direction and a color
;; fill in the rest of the column
to fill-inequality [h c] ;; patch procedure
  sprout 1
  [
    set heading h
    loop
    [
      fd 1
      if border?
       [ die ]
      set pcolor c
    ]
  ]
end

;; clear the equations and the legend
to clear-equations
  ask patches [set plabel "" ]
  set graphed-equation-list []
  set old-linear-regression? false
end

;; clear only the last equation drawn
to clear-last-equation
  if (length graphed-equation-list > 0)
  [
    set graphed-equation-list butlast graphed-equation-list
    ask grid [ set pcolor black ]
    foreach graphed-equation-list [ graph-equation item 0 ? false item 1 ? ]
  ]

  if reveal-legend?
  [
    ask grid [ set plabel "" ]
    draw-legend
  ]
end

;; set the rule to the value that the user enters in an input dialog
to set-rule
  set rule user-input "Enter the Verbal Rule:"
end

;; send a list of the position of every student's turtle, to every student
to send-points-to-students
  let point-list ""
  ask students
  [
    let point-place (word "(" precision my-x 1 "," precision my-y 1 ")")
    set point-list (word point-list point-place " ")
  ]
  set point-list substring point-list 0 (length point-list - 1)
  ask students
  [ hubnet-send user-id "points" (point-list) ]
end

;;
;; Linear Regression Procedures
;;
to linear-regression
  ;; you need to have at least two points to make a line
  if (count students > 1)
  [
   setup
   draw-axes

  ;; initialize the variables
  let num-turtles count students
  let xcors [ xcor * grids-per-patch-x ] of students
  let ycors [ ycor * grids-per-patch-y ] of students
  let sum-x sum xcors
  let sum-y sum ycors
  let ave-x sum-x / num-turtles
  let ave-y sum-y / num-turtles
  let sum-xy sum [ xcor * grids-per-patch-x * ycor * grids-per-patch-y ] of students
  let sum-x-square sum [ xcor * grids-per-patch-x * xcor * grids-per-patch-x ] of students
  let sum-y-square sum [ ycor * grids-per-patch-y * ycor * grids-per-patch-y ] of students

  ;; compute and store the denominator for the slope and y-intercept so we don't have to do it twice
  let denominator ( sum-x-square - num-turtles * ( ave-x ^ 2 ) )

  ;; if the denominator = 0 or the min of xcors = the max of xcors, the turtles are in a vertical line
  ;; thus, the line of regression will be undefined and have a r^2 value of 1.0
  ifelse ( denominator = 0 ) or ( min xcors = max xcors )
  [
    set slope "Undefined"
    set y-intercept "Undefined"
    ;; since all the turtles are in a line r^2 must be 1
    set r-square 1
    ;; set all the patches in the column to be on the line
    ask grid with [ pxcor = [pxcor] of one-of turtles ]
      [ set pcolor red ]
  ]
  [
    ;; otherwise, we have some other type of line.  so find the y-intercept and slope of the line

    set y-intercept ( ( ave-y * sum-x-square ) - ( ave-x * sum-xy ) ) / denominator
    set slope ( sum-xy - num-turtles * ave-x * ave-y ) / denominator

    ;; compute the value of r^2
    ifelse ( ( sum-y-square - num-turtles * ( ave-y ^ 2 ) ) = 0 ) or ( min ycors = max ycors )
    [ set r-square 1 ]
    [
      set r-square ( ( sum-xy - num-turtles * ave-x * ave-y ) ^ 2 ) /
        ( ( sum-x-square - num-turtles * ( ave-x ^ 2 ) ) * ( sum-y-square - num-turtles * ( ave-y ^ 2 ) ) )
    ]
    set r-square precision r-square 3

    ;; set the equation to the appropriate string
    set lin-reg-eq (word (precision slope 3) " * x + " (precision y-intercept 3))

    graph-equation lin-reg-eq false red
   ]
  ]
end

;;
;; Grid Procedures
;;

to setup-grid
  cp cd
  set old-grid? grid?

  set width x-maximum - x-minimum
  set height y-maximum - y-minimum

  set grids-per-patch-x width / (world-width - (2 * margin))
  set grids-per-patch-y height / (world-height - (2 * margin))
  set patches-per-grid-x  (world-width - (2 * margin)) / width
  set patches-per-grid-y  (world-height - (2 * margin)) / height

  ;; set up border patches
  ask patches
  [
    ifelse (pxcor < min-pxcor + margin or pxcor > max-pxcor - margin or
        pycor < min-pycor + margin or pycor > max-pycor - margin)
    [
      set pcolor 2
      set border? true
    ]
    [
      set pcolor black
      set border? false
    ]
    set plabel ""
    set plabel-color white
  ]

  set grid patches with [ not border? ]

  ;; label x and y , minimums and maximums
  ask patches with [ pxcor = min-pxcor + margin + 3 and pycor = min-pycor + margin - 4 ]
    [ set plabel x-minimum ]
  ask patches with [ pxcor = max-pxcor - margin and pycor = min-pycor + margin - 4 ]
    [ set plabel x-maximum ]
  ask patches with [ pxcor = min-pxcor + (margin - 2) and pycor = min-pycor + margin ]
    [ set plabel y-minimum ]
  ask patches with [ pxcor = min-pxcor + (margin - 2) and pycor = max-pycor - margin ]
    [ set plabel y-maximum ]

  ;; show x and y axes
  set x-axis round ( abs y-minimum * patches-per-grid-x + min-pycor + margin - 0.5 )
  set y-axis round( abs x-minimum * patches-per-grid-y + min-pxcor + margin - 0.5 )

  ;; if range is from
  ;; if range is from -20 to 20 do every 10
  ;; from -10 to 10 do every 1
  ;; from -1 to 1 do every .1

  if grid?[ draw-grid ]

  draw-axes
end

to draw-axes
  ask patch round 0 x-axis [ draw-grid-line blue 90 (world-width - margin * 2) ]
  ask patch round y-axis 0 [ draw-grid-line blue 0 (world-height - margin * 2) ]
end

to draw-grid
    ;; do vertical lines on grid
    let patch-x-inc patches-per-grid-x

    ifelse width > 20
    [ set patch-x-inc (10 * patch-x-inc) ][
    if width <= 2
    [ set patch-x-inc (0.1 * patch-x-inc) ] ]

    ask grid with [ pycor = 0 and (pxcor - round x-axis) mod round patch-x-inc = 0 ]
      [ draw-grid-line 3 0 (world-height - margin * 2 ) ]

    ;; do horizontal lines on grid
    let patch-y-inc patches-per-grid-y

    ifelse height > 20
    [ set patch-y-inc (10 * patch-y-inc) ][
    if height <= 2
    [ set patch-y-inc (0.1 * patch-y-inc ) ] ]

    ask grid with [ pxcor = 0 and (pycor - round y-axis) mod round patch-y-inc = 0 ]
      [ draw-grid-line 3 90 (world-width - margin * 2) ]
end

to draw-grid-line [ c h s ] ;; patch procedure
  sprout 1 [
    set shape "line"
    set color c
    set heading h
    set size s
    stamp
    die
  ]
end

to-report patch-x [ the-x ]
  report the-x * patches-per-grid-x + y-axis
end

to-report patch-y [ the-y ]
  report the-y * patches-per-grid-y + x-axis
end

to-report random-grid-coord-x
  ifelse width > 10
  ;if the width is greater than 10, place students at whole numbers
  [ report random width + x-minimum ]
  ;if the range between 10 and 0, place students whole or half numbers
  [ report (random (width * 2)) / 2 + x-minimum ]
end

to-report random-grid-coord-y
  ifelse height > 10
  ;if the width is greater than 10, place students at whole numbers
  [ report random height + y-minimum ]
  ;if the range between 10 and 0, place students whole or half numbers
  [ report (random (height * 2)) / 2 + y-minimum ]
end

;; report the coordinates of the mouse, with respect to the range and scale of the graph
;; rather than the patch coordinate system
to-report mouse-coords
  report (word "(" ( precision ((mouse-xcor - y-axis) * grids-per-patch-x) 1)
         "," ( precision ((mouse-ycor - x-axis) * grids-per-patch-y ) 1 ) ")")
end

to draw-legend
  let x-coord max-pxcor - (margin * 2)
  let y-coord min-pycor + margin + 4
  let i 0
  foreach graphed-equation-list
    [
      ask patch x-coord y-coord
      [
        set plabel item 0 ?
        set plabel-color item 1 ?
      ]
      set y-coord y-coord + 5
      set i i + 1
    ]
  set old-legend? reveal-legend?
end

;;;;;;;;;;;;;;;;;;;;;;;
;; HubNet Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;

to listen-clients
  let at-least-one-message? false

  while [ hubnet-message-waiting? ]
  [
    set at-least-one-message? true
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ create-new-student ]
    [
      ifelse hubnet-exit-message?
      [ remove-student ]
      [ execute-command hubnet-message-tag ]
    ]
  ]

  if linear-regression? and (at-least-one-message? or not old-linear-regression?)
    [ linear-regression ]
  set old-linear-regression? linear-regression?
end

;; update the client monitors
to send-info-to-clients ;; turtle procedure
  hubnet-send user-id "Located at:" (word "(" precision (my-x) 2 "," precision (my-y) 2 ")")
  hubnet-send user-id "You are a:" (shape-combo)
  hubnet-send user-id "rule" (rule)
end

to create-new-student
  create-students 1
  [
    setup-student-vars
    send-info-to-clients
  ]
end

to remove-student
  ask students with [user-id = hubnet-message-source]
  [
    set available-shapes sentence available-shapes shape-combo ;; make sure to return the shape to available
    die
  ]
end

to execute-move [new-heading]
  ask students with [user-id = hubnet-message-source]
  [
    set heading new-heading
    let dist 0
    ifelse heading mod 180 = 0
    [
      set dist step-size * patches-per-grid-y
    ]
    [
      set dist step-size * patches-per-grid-x
    ]
    if can-move? dist and not [border?] of patch-ahead dist
    [ fd dist ]

    set my-x xcor * grids-per-patch-x
    set my-y ycor * grids-per-patch-y

    hubnet-send user-id "Located at:" (word "(" precision my-x 2 "," precision my-y 2 ")")
  ]
end

to execute-command [command]
  ifelse command = "Step-Size"
  [
    ask students with [user-id = hubnet-message-source]
      [ set step-size hubnet-message ]
  ][
  ifelse command = "up"
  [ execute-move 0 ][
  ifelse command = "down"
  [ execute-move 180 ][
  ifelse command = "right"
  [ execute-move 90 ][
  ifelse command = "left"
  [ execute-move 270 ][
  ifelse command = "Change Appearance"
  [ ask students with [ user-id = hubnet-message-source ]
      [ change-shape ] ][
  if command = "my-equation"
  [
    let eq hubnet-message
    ask students with [ user-id = hubnet-message-source ] [ set my-equation eq ] ]
  ] ] ] ] ] ]
end

to setup-student-vars  ;; turtle procedure
  set user-id hubnet-message-source

  set my-x random-grid-coord-x
  set my-y random-grid-coord-y

  ;translate my-x and my-y into patches
  setxy patch-x my-x
        patch-y my-y

  change-shape

  face one-of neighbors4
  set step-size 1
  set size 9
  set my-equation ""
end

to change-shape ;; turtle procedure
  set shape one-of all-shapes
  set color one-of all-colors
  set shape-combo (word (color-name color) " " shape)
  hubnet-send user-id "You are a:" shape-combo
end

to-report color-name [c]
  report item (position c all-colors) color-names
end
@#$#@#$#@
GRAPHICS-WINDOW
214
10
647
464
70
70
3.0
1
10
1
1
1
0
0
0
1
-70
70
-70
70
1
1
0
ticks

SLIDER
214
466
373
499
x-minimum
x-minimum
-50
0
-10
1
1
NIL
HORIZONTAL

SLIDER
488
466
647
499
x-maximum
x-maximum
0
50
10
1
1
NIL
HORIZONTAL

SLIDER
25
416
198
449
y-minimum
y-minimum
-50
0
-10
1
1
NIL
HORIZONTAL

SLIDER
18
47
191
80
y-maximum
y-maximum
0
50
10
1
1
NIL
HORIZONTAL

BUTTON
16
10
103
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

BUTTON
21
132
185
165
NIL
enter-equation
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
247
530
664
575
Rule
rule
3
1
11

BUTTON
142
539
231
572
NIL
set-rule
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
105
10
192
43
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

BUTTON
8
219
63
268
<
left-direction
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
152
219
207
268
>
right-direction
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
8
269
207
314
student equation
current-student-equation
3
1
11

MONITOR
65
219
151
264
name
current-student-name
3
1
11

SWITCH
656
392
810
425
reveal-legend?
reveal-legend?
0
1
-1000

MONITOR
378
466
483
511
NIL
mouse-coords
3
1
11

SWITCH
660
10
833
43
linear-regression?
linear-regression?
1
1
-1000

MONITOR
660
101
833
146
coefficient of correlation
r-square
3
1
11

MONITOR
751
48
833
93
y intercept
y-intercept
3
1
11

MONITOR
660
48
746
93
NIL
slope
3
1
11

TEXTBOX
48
539
138
577
Send students a rule to follow.
11
0.0
0

SWITCH
28
89
182
122
grid?
grid?
0
1
-1000

BUTTON
21
167
185
200
NIL
clear-last-equation
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
23
320
187
353
NIL
graph-student-equation
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
23
355
187
388
NIL
edit-student-equation
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

In this activity students can explore functions.  Each student controls one point in the function.  The activity supports several different kinds of exploration, with open ended possibilities for learning.

Students can try to make their points match an equation or description supplied by a teacher or student, or they can try to find an equation that best fits where their points are.  The class should be encouraged to find unique or interesting equations that fit the criteria, to enforce the ideas of equivalence and simplification.

## HOW IT WORKS

When students log in they are assigned a character in the coordinate system.  Students control the character, which is merely a point placed in the coordinate system.  The teacher can send instructions to students.

Teachers can challenge students to follow a rule like, "move until your y value equals your x value". Students can explore the space, pushing the boundaries of this rule, the point (1,1) works, but so does (-1,-1) and (-0.5, -0.5).

Then students can explore alternate ways to phrase the rule, the function y = x defines this space, but so does y = 3 * x - 2 * x.

Teachers can test students' functions, by graphing them over the class' set of points. You can use any valid NetLogo code to define the function in terms of y = f(x). You can also define inequalities in terms of x. See the How To Use It section for examples.

## HOW TO USE IT

To run the activity press the GO button. To start the activity over with the same group of students stop the GO button by pressing it again, press the SETUP button, and press GO again. To run the activity with a new group of students press the RESET button in the Control Center.

Buttons:
SETUP - clears the grid, if you have moved the x-maximum and minimum sliders the grid will automatically resize and redraw.
GO - starts the activity so students can move their characters around the grid.
ENTER-EQUATION - prompts you to enter an equation to graph in the form y = f(x), where any valid NetLogo code can be used in the function defining y, for example: "y = x"
or "y = 2 * pi * sin x". You can also define inequalities in terms of x for example: "y >= 3 * x + 1". You can use any math operations supported by NetLogo and you should use correct NetLogo syntax. So instead of 2x you should enter 2 * x.
CLEAR-LAST-EQUATION - removes the last equation graphed from the list and erases it from the grid

Sliders:
Y-MAXIMUM, Y-MINIMUM, X-MAXIMUM, X-MINIMUM - define the bounds of the grid the students work in. Note that this is different than the size of the world, this allows you to use different size spaces without the world growing too large. You could also increase the size of the world to get a higher resolution.

Monitors:
MOUSE POSITION - reports the position of the mouse in the grid.

Switches:
GRID? - toggles the grid lines on and off
REVEAL-LEGEND? - toggles the legend of equations in the lower right hand corner

Students' Equations:
Students can send equations to the teacher, they appear in the STUDENT-EQUATION monitor.  The teacher can browse through them using the "<" and ">" buttons.
GRAPH-STUDENT-EQUATION - graphs the equation displayed in the STUDENT-EQUATION monitor
EDIT-STUDENT-EQUATION - allows the teacher to change the student equation, either to correct the syntax or to give feedback to the student. The edits are sent to the student.

Linear Regression:
If LINEAR-REGRESSION? is on all other equations will be erased and a line will be interpreted, using the students as points. The SLOPE, Y-INTERCEPT, and COEFFICIENT OF CORRELATION are calculated and displayed in the corresponding monitors.

Send a Rule:
The teacher can also send a rule for students to follow by clicking the SET-RULE button and entering the "verbal" rule.  The current rule is displayed in the RULE monitor.

Client Interface:
Buttons:
Move your character around the grid by pressing the UP, DOWN, LEFT, and RIGHT buttons which move you STEP-SIZE in the specified direction.
CHANGE APPEARANCE - changes the way the characters looks in the view

Monitors:
RULE - the rule set by the teacher.
YOU ARE A: - a word description of your character
LOCATED AT: - your coordinates in the grid

Input Boxes:
MY-EQUATION - enter your equation to turn into the teacher here

## THINGS TO NOTICE

Turn the LINEAR-REGRESSION? switch to on, to see the linear regression of your set of points.

The legend can be turned on and off in the bottom right hand corner of the interface.

The bounds of the grid are controlled by the corresponding sliders.  (They are distinct from the maximum and minimum world coordinates; the grid coordinate system does not depend on the patch grid coordinates.)

## THINGS TO TRY

Ask the students to follow a verbal rule like "make your y coordinate match your x coordinate".  Then ask students to come up with an equation that fits the rule.  Graph the different equations.  Ask students to stretch the bounds of any given equation to come up with unique solutions.

Adjust the range sliders so the center of the world is not in the center of the view and graph the same equations.

## EXTENDING THE MODEL

Students' equations are not checked for syntax errors until the teacher attempts to graph them.  Change the code for handling student equations so that it automatically lets students know if what they have submitted does not compile.

## NETLOGO FEATURES

This model uses the runresult primitive to approximate the y value for each x value, thus, functions can use any valid NetLogo code in the function defining y.

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
Circle -7500403 true true 39 39 224

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

diamond
false
0
Polygon -7500403 true true 150 17 270 149 151 272 30 152

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

heart
false
0
Circle -7500403 true true 31 30 122
Circle -7500403 true true 147 32 120
Polygon -7500403 true true 51 135 151 243 250 133 264 105 169 84 108 84 44 118 44 126
Polygon -7500403 true true 46 131 150 242 49 114
Polygon -7500403 true true 44 130 150 242 38 105 36 112

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

rhombus
false
0
Polygon -7500403 true true 100 51 291 50 189 221 2 222

square
false
0
Rectangle -7500403 true true 48 40 249 238

square 2
false
0
Rectangle -7500403 true true 30 30 270 270
Rectangle -16777216 true false 60 60 240 240

star
false
0
Polygon -7500403 true true 152 2 196 105 297 106 215 176 248 277 151 209 56 278 87 172 4 107 108 104

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
Polygon -7500403 true true 151 8 285 232 11 236

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
BUTTON
79
118
171
151
up
NIL
NIL
1
T
OBSERVER
NIL
W

BUTTON
76
192
168
225
down
NIL
NIL
1
T
OBSERVER
NIL
S

BUTTON
122
155
214
188
right
NIL
NIL
1
T
OBSERVER
NIL
D

BUTTON
26
155
118
188
left
NIL
NIL
1
T
OBSERVER
NIL
A

INPUTBOX
245
134
470
214
my-equation
0
1
0
String (reporter)

MONITOR
155
61
245
110
Located at:
NIL
3
1

MONITOR
57
10
246
59
You are a:
NIL
3
1

CHOOSER
248
64
340
109
Step-Size
Step-Size
0.1 0.2 0.5 1 2 3 5
3

MONITOR
4
239
515
288
rule
NIL
3
1

BUTTON
249
16
438
49
Change Appearance
NIL
NIL
1
T
OBSERVER
NIL
NIL

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

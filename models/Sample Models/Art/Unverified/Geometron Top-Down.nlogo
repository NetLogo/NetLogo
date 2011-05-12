globals [
  curr-color-sep        ;; spread of the colors in the kaleidoscope
  counter
]

turtles-own [
  new?                 ;; was the turtle just created?
  type-a?              ;; used when turtles with different behaviors are hatched
]

to setup
  clear-all
  ;; some of the patterns assume
  ;; evenly spaced turtles
  create-ordered-turtles nturtles
  ask turtles
    [pen-down
     set new? false
    ]
  set curr-color-sep color-sep
  set counter 0
end

to lift-pen
  ask turtles
    [if (who >= nturtles)
       [die]
     pen-up
    ]
  clear-patches
end


to restore
  ask turtles
    [if (who >= nturtles)
       [die]
     pen-down
    ]
  clear-patches
end

to color-shift
  ifelse shift-direction = "increment"
    [set curr-color-sep (curr-color-sep + (random-float 1.0))]
    [set curr-color-sep (curr-color-sep - (random-float 1.0))]
  wait 1
end


;;PATTERN-1
;;---------
;;Turtles draw circles creating an overall circular design.

to pattern-1    ;;turtle procedure
  if (new?)
    [set color (who / curr-color-sep)
     if (follow-turtle)
       [if (who = (50 + nturtles))
          [pen-down]
       ]
     right-circle
     left-circle
     die
    ]
  if (( who mod 10 ) = 0)
    [rt direction
     fd 0.5
     if (count turtles + 1 <= max-num)
       [hatch 1
          [set new? true]
       ]
    ]
end


;;PATTERN-2
;;---------
;;Turtles draw a combination of hexagons and octagons, Overall shape determined by NTURTLES.

to pattern-2    ;;turtle procedure
  if (new?)
    [ifelse (type-a?)
       [set color (who / curr-color-sep)
        if (follow-turtle)
          [if (who = (60 + nturtles))
             [pen-down]
          ]
        hexagon
        die
       ]
       [set color (who / curr-color-sep)
        if (follow-turtle)
          [if (who = (50 + nturtles))
             [pen-down]
          ]
        octagon
        die
       ]
    ]
  ifelse ((who mod 2) = 0)
    [rt 1
     fd 1
     if (count turtles + 1 <= max-num)
       [hatch 1
          [set new? true
           set type-a? true
          ]
       ]
    ]
    [lt 1
     fd 1
     if (count turtles + 1 <= max-num)
       [hatch 1
          [set new? true
           set type-a? false
          ]
       ]
    ]
end


;;PATTERN-3
;;---------
;;Turtles create only pentagons, slight variations in their origin create the overall effect.

to pattern-3    ;;turtle procedure
  if (new?)
    [set color (who / curr-color-sep)
     if (follow-turtle)
       [if (who = (60 + nturtles))
          [pen-down]
       ]
     pentagon
     die
    ]
  if (who mod 5) = 0
    [rt direction
     fd 0.5
     if (count turtles + 1 <= max-num)
       [hatch 1
          [set new? true]
       ]
    ]
end


;;PATTERN-4
;;---------
;;Turtles draw ninegons and left circles creating an overall circular pattern.

to pattern-4    ;;turtle procedure
  if (new?)
    [ifelse (type-a?)
       [set color (who / curr-color-sep)
        if (follow-turtle)
          [if (who = (1583 + nturtles))
             [pen-down]
           if (who = (1087 + nturtles))
             [pen-down]
          ]
        nine-gon
        die
       ]
       [set color (who / curr-color-sep)
        if (follow-turtle)
          [if (who = (214 + nturtles))
             [pen-down]
          ]
        left-circle
        die
       ]
    ]
  ifelse ((who mod 3) = 0)
    [rt 1
     if (count turtles + 1 <= max-num)
       [hatch 1
          [set new? true
           set type-a? true
          ]
       ]
    ]
    [lt 1
     if (count turtles + 1 <= max-num)
       [hatch 1
          [set new? true
           set type-a? false
          ]
       ]
    ]
end


;;PATTERN-5
;;---------
;;Turtles draw a left square and then die.

to pattern-5    ;;turtle procedure
  if (new?)
    [set color (who / curr-color-sep)
     if (follow-turtle)
       [if (who = (80 + nturtles))
          [pen-down]
       ]
     left-square
     die
    ]
  if (count turtles + 1 <= max-num)
    [hatch 1
       [set new? true]
    ]
end


;;PATTERN-6
;;---------
;;Turtles draw several shapes, however overall design remains circular.

to pattern-6    ;;turtle procedure
  if count turtles > max-num
    [if (who > max-num)
       [die]
     stop
    ]
  if (new?)
    [ifelse (type-a?)
       [set color (who / curr-color-sep)
        if (follow-turtle)
          [if (who = (60 + nturtles))
             [pen-down]
          ]
        pentagon
        hexagon
        left-circle
        die
       ]
       [set color (who / curr-color-sep)
        if (follow-turtle)
          [if (who = (60 + nturtles))
             [pen-down]
          ]
        nine-gon
        octagon
        right-circle
        die
       ]
    ]
  if (count turtles + 1 <= max-num)
    [hatch 1
       [set new? true
        set type-a? true
       ]
    ]
  if (count turtles + 1 <= max-num)
    [hatch 1
       [set new? true
        set type-a? false
       ]
    ]
end


;;RIGHT-CIRCLE
;;------------
;;Performs the following procedure 180 times:
;;Move forward 1.5 steps and turn right by 2 degrees.
;;To see the shape that this function creates,
;;try calling it in the command center with one turtle with the pen down.
;;A turtle will create a circle heading in the right direction.

to right-circle    ;;turtle procedure
  repeat 180
    [fd 1.5
     rt 2
    ]
end


;;LEFT-CIRCLE
;;-----------
;;Performs the following procedure 180 times:
;;Move forward 1.5 steps and turn left by 2 degrees.
;;To see the shape that this function creates,
;;try calling it in the command center with 0one turtle with the pen down.
;;A turtle will create a circle heading in the left direction.

to left-circle    ;;turtle procedure
  repeat 180
    [fd 1.5
     lt 2
    ]
end


;;LEFT-SQUARE
;;-----------
;;Performs the following procedure 4 times:
;;Move forward EXPANDER steps and turn left by 90 degrees.
;;To see the shape that this function creates,
;;try calling it in the command center with one turtle with the pen down.
;;A turtle will create a square heading in the left direction.

to left-square    ;;turtle procedure
  repeat 4
    [fd expander
     lt 90
    ]
end


;;RIGHT-TRIANGLE
;;--------------
;;Performs the following procedure 3 times:
;;Move forward 35 steps and turn right by 120 degrees.
;;To see the shape that this function creates,
;;try calling it in the command center with one turtle with the pen down.
;;A turtle will create a triangle heading in the right direction.

to right-triangle    ;;turtle procedure
  repeat 3
    [fd expander
     rt 120
    ]
end


;;OCTAGON
;;-------
;;Performs the following procedure 8 times:
;;Move forward 30 steps and turn right by 45 degrees.
;;To see the shape that this function creates,
;;try calling it in the command center with one turtle with the pen down.
;;A turtle will create an octagon heading in the right direction.

to octagon    ;;turtle procedure
  repeat 8
    [fd 30
     lt 45
    ]
end


;;PENTAGON
;;--------
;;Performs the following procedure 5 times:
;;Move forward 35 steps and turn right by 72 degrees.
;;To see the shape that this function creates,
;;try calling it in the command center with one turtle with the pen down.
;;A turtle will create a pentagon heading in the right direction.

to pentagon    ;;turtle procedure
  repeat 5
    [fd 35
     rt 72
    ]
end


;;HEXAGON
;;-------
;;Performs the following procedure 6 times:
;;Move forward 30 steps and turn right by 60 degrees.
;;To see the shape that this function creates,
;;try calling it in the command center with one turtle with the pen down.
;;A turtle will create a hexagon heading in the right direction.

to hexagon    ;;turtle procedure
  repeat 6
    [fd 30
     rt 60
    ]
end


;;NINE-GON
;;--------
;;Performs the following procedure 9 times:
;;Move forward 35 steps and turn right by 40 degrees.
;;To see the shape that this function creates,
;;try calling it in the command center with one turtle with the pen down.
;;A turtle will create a nine-gon heading in the right direction.

to nine-gon    ;;turtle procedure
  repeat 9
  [fd 35
   lt 40
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
321
10
833
543
125
125
2.0
1
10
1
1
1
0
0
0
1
-125
125
-125
125
0
0
0
ticks

BUTTON
23
38
113
71
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

SLIDER
3
78
133
111
nturtles
nturtles
1
36
10
1
1
NIL
HORIZONTAL

SLIDER
3
118
133
151
color-sep
color-sep
0.0
30.0
2
0.1
1
NIL
HORIZONTAL

BUTTON
23
158
113
191
color-shift
color-shift
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
23
278
113
311
lift-pen
lift-pen
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
8
318
128
351
follow-turtle
follow-turtle
1
1
-1000

BUTTON
23
358
113
391
restore
restore
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
167
38
297
71
expander
expander
0.0
90.0
90
1.0
1
NIL
HORIZONTAL

SLIDER
167
78
297
111
direction
direction
0.0
10.0
10
0.1
1
NIL
HORIZONTAL

BUTTON
150
123
231
166
pattern 1
pattern-1
T
1
T
TURTLE
NIL
NIL
NIL
NIL
1

BUTTON
232
123
313
166
pattern 2
pattern-2
T
1
T
TURTLE
NIL
NIL
NIL
NIL
1

BUTTON
150
167
231
211
pattern 3
pattern-3
T
1
T
TURTLE
NIL
NIL
NIL
NIL
1

BUTTON
232
167
313
211
pattern 4
pattern-4
T
1
T
TURTLE
NIL
NIL
NIL
NIL
1

BUTTON
150
212
231
255
pattern 5
pattern-5
T
1
T
TURTLE
NIL
NIL
NIL
NIL
1

BUTTON
232
212
313
255
pattern 6
pattern-6
T
1
T
TURTLE
NIL
NIL
NIL
NIL
1

MONITOR
167
268
297
313
curr-color-sep
curr-color-sep
3
1
11

MONITOR
167
328
297
373
number of turtles
count turtles
3
1
11

SLIDER
167
398
297
431
max-num
max-num
256
8192
4096
256
1
NIL
HORIZONTAL

CHOOSER
22
197
114
242
shift-direction
shift-direction
"increment" "decrement"
0

@#$#@#$#@
## WHAT IS IT?

This model uses NetLogo turtles to repeatedly draw circles and other geometric shapes, turning periodically so that the display gives the impression of a kaleidoscope or a pinwheel.

## HOW TO USE IT

Set NTURTLES to a desired value between 0 and 36 (default value is 10).  NTURTLES determines how many initial turtles you want to start out with. These initial turtles will spawn other shape-making turtles.

Set COLOR-SEP to a desired value between 0 and 30 (default value is 2).  COLOR-SEP determines the range of colors that the turtles (and hence the kaleidoscope) will take on. The higher the value, the smaller the range.

Set MAX-NUM to a desired value between 1024 and 8192 (default value is 4096).  MAX-NUM determines the maximum number of turtles that can be in the world at any time.  Note however, a higher value will make the model run slower.

When you have set COLOR-SEP and NTURTLES, press the SETUP button to set-up the model.

Next, choose a pattern. In each pattern, the initial turtles hatch other turtles and those hatched turtles draw different geometric shapes.  Each pattern is represented by a forever button. The user cannot change PATTERN-2, PATTERN-4 or PATTERN-6.  PATTERN-1, PATTERN-3, and PATTERN-5, however, allow for user control using the DIRECTION and EXPANDER sliders.  For detailed descriptions of the instructions in each pattern, go to the end of the info-window.

The slider DIRECTION (default value is 2) is designed for PATTERN-1 and PATTERN-3.  At each cycle, DIRECTION determines the amount that the pattern-making turtles turn to the right.  In other words, if DIRECTION is set to 12, each turtle will turn 12 degrees to the right before moving forward.

The slider EXPANDER is designed specifically for pattern 5 (default value is 80).  At each cycle of PATTERN-5, EXPANDER determines how large each leg of the polygon will be.

The user also has control over the color distribution exhibited by each pattern. The COLOR-SHIFT button will continuously increase or decrease the value of 'curr-color-sep' by a small random amount. (Thus the slider value itself isn't changed, but the color value of the turtle is.)  The SHIFT-DIRECTION chooser determines if 'curr-color-sep' is increased or decreased.

Other features of the model are the LIFT-PEN and RESTORE buttons.  LIFT-PEN lifts the pen on all turtles and kills all turtles greater than NTURTLES.  RESTORE kills all turtles greater than NTURTLES and tells all turtles to put their pen down. Killing all turtles greater than NTURTLES restores the original number of turtles in the world.

A useful tool that can be used in conjunction with the LIFT-PEN and RESTORE buttons is a switch called FOLLOW-TURTLE. When you press LIFT-PEN without FOLLOW-TURTLE being on, you see the skeleton of the pattern since the turtles are moving without their pen down. But when the switch is turned on, one hatched turtle will put its pen down.  However, since turtles are constantly being born and dying it may take time for this particular turtle to appear.  It should also be understood that because some of the patterns divide the turtles into different groups and assign them different shapes to create, more than one turtle may be told to put its pen down in order to represent what all the turtles in the pattern are doing.

Two monitors are provided at the bottom of the Interface Window. COUNT-TURTLES displays the current number of turtles in the world. Likewise, CURR-COLOR-SEP displays that variable's value, so that you know when it has been altered, and by how much.

## THINGS TO NOTICE

An important thing to notice here is the number given in COUNT-TURTLES. Right away, it becomes much larger than NTURTLES, but quickly settles on some nice big number. Take a look at the Procedures Window. Initially, upon setup, there are NTURTLES turtles. Once one of the pattern buttons is pressed, each of these turtles repeatedly hatches a new turtle and turns by a specified degree.

It is important to understand that this phenomenon, the fact that initial NTURTLES is exploding into a number much larger than NTURTLES is because other turtles are being hatched in between the time a single turtle is hatched and the time it finishes drawing its given shape.  Thus, if we were to add a wait statement into one of the patterns, not as many turtles could be hatched.  In other words, the number of turtles greater than NTURTLES would decrease. As turtles execute their commands much quicker than the hatched turtles, they produce many turtles during one loop of a pattern; eventually, though, turtles start to die off. At this point, the number of turtles who are born is roughly equal to the number who die at any given step.

You also should notice how COLOR-SEP (known in the code as 'curr-color-sep') alters the appearance of the pattern. Turn COLOR-SHIFT on, and let 'curr-color-sep' become very large. Then watch what happens when it is small, maybe zero or some negative number.

## THINGS TO TRY

Try playing around with the DIRECTION slider on PATTERN-1 AND PATTERN-3 and the EXPANDER slider on PATTERN-5.  Observe what happens to the pattern as you change the values of each. GEOMETRON-TOP-DOWN is meant to be a visually pleasing model just to watch. See what different values of COLOR-SEP produce, and explore how COLOR-SHIFT changes the appearance of the kaleidoscope. What seems the best to you?

Try changing the code in the Code tab. Increase the size of the shapes drawn by each of the turtles, or try changing the size of the angle each of the turtles turns through.

Instead of each turtle moving or turning a given amount, what about having it move a small random amount (as in the changes to curr-color-sep from COLOR-SHIFT). How much randomness can you add to 'kaleidoscope' and still maintain some kind of overall structure?

After running one of the patterns, try changing the number of NTURTLES and then stop the pattern and push CLEAR-PATCHES-LIFT-PEN.  Then start the pattern again.  What happens?  Can you explain why this happens?

Try changing the MAX-NUM slider during a run, what happens?  Can you explain why this occurs?

## EXTENDING THE MODEL

Whenever a turtle is hatched by one of NTURTLES, it proceeds to draw a certain pattern. Change the commands in `hatch` so that the turtle draws some other shape or pattern. Try to predict what overall shape will emerge.

Try to write an entirely new kind of GEOMETRON-TOP-DOWN project. In the current project, turtles spin off from a center core of NTURTLES turtles. In your new project, maybe the drawing turtles could orbit around some fixed (or moving) point-look at the NetLogo projects 'n-bodies' and 'gravitation'.

## NETLOGO FEATURES

GEOMETRON-TOP-DOWN makes nice use of the turtle primitive `hatch`. Whenever a turtle is hatched, it executes the command list that follows the `hatch` command. Generally all it does is change its color or alter some variable- there's no reason it can't run some other, possibly lengthy, procedure. (Which is exactly what happens here.)

## NOTES ON THE SHAPES

The following is a list of all the basic shape functions that are used in this model to create the six patterns.  It is important to understand that the complexity of the patterns are actually nothing more than different combinations of these shape functions.  For example, pattern-1 utilizes the right-shape and left-shape functions.  Both these functions simply draw circles.  However, by adding slight variations to what the turtle does and which ones do it, it is possible to create the pattern that is represented by pattern-1.  This, in fact, is the basic algorithm that the model uses in order to create all the patterns.  Take the basic shape functions, add slight variations to what the turtles do and choose which turtles do it.

about RIGHT-CIRCLE:
Performs the following procedure 180 times:
Move forward 1.5 steps and turn right by 2 degrees.
To see the shape that this function creates, try calling it in the command center with one turtle with the pen down.
A turtle will create a circle heading in the right direction.

about LEFT-CIRCLE:
Performs the following procedure 180 times:
Move forward 1.5 steps and turn left by 2 degrees.
To see the shape that this function creates, try calling it in the command center with one turtle with the pen down.
A turtle will create a circle heading in the left direction.

about LEFT-SQUARE:
Performs the following procedure 4 times:
Move forward EXPANDER steps and turn right by 90 degrees.
To see the shape that this function creates, try calling it in the command center with one turtle with the pen down.
A turtle will create a square heading in the left direction.

about RIGHT-TRIANGLE:
Performs the following procedure 3 times:
Move forward 35 steps and turn right by 120 degrees.
To see the shape that this function creates, try calling it in the command center with one turtle with the pen down.
A turtle will create a triangle heading in the right direction.

about OCTAGON:
Performs the following procedure 8 times:
Move forward 30 steps and turn right by 45 degrees.
To see the shape that this function creates, try calling it in the command center with one turtle with the pen down.
A turtle will create an octagon heading in the right direction.

about PENTAGON:
Performs the following procedure 5 times:
Move forward 35 steps and turn right by 72 degrees.
To see the shape that this function creates, try calling it in the command center with one turtle with the pen down.
A turtle will create a pentagon heading in the right direction.

about HEXAGON:
Performs the following procedure 6 times:
Move forward 30 steps and turn right by 60 degrees.
To see the shape that this function creates, try calling it in the command center with one turtle with the pen down.
A turtle will create a hexagon heading in the right direction.

about NINE-GON:
Performs the following procedure 9 times:
Move forward 35 steps and turn right by 40 degrees.
To see the shape that this function creates, try calling it in the command center with one turtle with the pen down.
A turtle will create a nine-gon heading in the right direction.

## SLIDER DESCRIPTIONS

EXPANDER: Numbered from 0 to 90 and increments by 1.  Expands and contracts the square created by LEFT-SQUARE.  Associated with PATTERN-5.

DIRECTION: Numbered from 0 to 10 and increments by 0.1 or (1/10).  Turns a turtle to the right by the number of degrees specified by the slider.  Associated with PATTERN-3 and PATTERN-1.

## WHAT THE PATTERNS DO

about PATTERN-1:
In this pattern, every 10th turtle moves forward 0.5 steps and to the right by DIRECTION degrees.  Each call to the pattern hatches a turtle who performs the RIGHT-CIRCLE function, then the LEFT-CIRCLE function and then dies.  By increasing the DIRECTION, the turtles increase the degrees by which they turn to the right.  This means that at each forward movement the turtle turns tighter around a center point.  Thus, when DIRECTION  is set to 0, the turtle simply moves forward.  The more it is increased, say from 0 to 1 to 2 to 10, the more circular the turtles movement becomes.  Although the pattern changes, it must be understood that the underlying shape that is being created is a circle.

about PATTERN-2:
Every even numbered turtle moves forward 1 step and turns right by 1 degree.  It then hatches a turtle that performs the HEXAGON function and then dies.  Every odd numbered turtle moves forward 1 step and turns left by 1 degree. It then hatches a turtle that performs the OCTAGON  function and then dies.  It is interesting to note that when the nturtles slider is raised to a high number ,i.e., greater than or equal to 20, the pattern in the lower numbers becomes unrecognizable.  However, it must be understood that although the new pattern may appear to be different, still, the underlying shapes that are being made are the hexagon and octagon.

about PATTERN-3:
Every 5th turtle moves forward 0.5 steps and turns right by DIRECTION degrees.  It then hatches a turtle which performs the PENTAGON function and then dies.  PATTERN-3 and PATTERN-5 are probably the simplest patterns in this model in terms of explaining their behavior.  Basically, PATTERN-3 creates pentagons, but because the turtles move forward 0.5 and move to the right DIRECTION  number of degrees, this is what causes the pattern to drastically change.  The illusion is that the hatched turtles are somehow creating different shapes but the truth of the matter is that they are making pentagons and always will be making pentagons.  It is only the slight variations in outside influences that brings about a change in the pattern.

about PATTERN-4:
Every 3rd turtle turns right by 1 degree and then hatches a turtle that performs the NINE-GON function and then dies.  All the other turtles turn left by 1 degree and then hatch a turtle that performs the LEFT-CIRCLE function and then dies.

about PATTERN-5:
Every turtle hatches another turtle that performs the LEFT-SQUARE function and then dies.

about PATTERN-6:
Every turtle moves forward DIRECTION steps and then hatches a turtle which performs the following functions in the following order: NINE-GON, OCTAGON, RIGHT-CIRCLE, PENTAGON, HEXAGON, and LEFT-CIRCLE.  After performing these functions, each hatched turtle then dies.

## NOTES ON THE CODE

Notice that in each of these functions, the number of times the procedure is repeated multiplied by the number of degrees the turtle turns either to the right or to the left is equal to 360 degrees.  Which means that each of the turtles is creating a polygon.

I discovered that in the patterns where the turtle moves a number of steps forward and then turns right or left in some direction, this action significantly changes the pattern.  For example, in pattern-1, the fact that each hatching turtle moves 0.5 steps forward means that the resulting shape from the RIGHT-CIRCLE OR LEFT-CIRCLE function will resemble a kidney bean rather than a circle, which is what the RIGHT-CIRCLE  and LEFT-CIRCLE function are designed to produce.

It is important to understand that these patterns are able to be created because they obey certain basic rules about geometry.  For example, I mentioned above that all the functions create polygons. This is due to the fact that every polygon must have the interior sum of its angles equal to 360 degrees.  However, certain slight alterations like those mentioned above, are enough to change the shape and create a new one.  For example, if the DIRECTION slider started off at 0 in pattern-1, every hatching turtle would simply be moving forward by 0.5 steps as it was creating its pattern.  However, if we increase the slider by increments of 0.1, we notice that its movements seem to be getting tighter and tighter until it remains fixed in one place and keeps revolving around a point.

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
setup
ask turtles [ repeat 50 [ pattern-1 ] ]
ask turtles [ repeat 50 [ pattern-1 ] ]
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

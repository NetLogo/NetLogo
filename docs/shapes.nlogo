to setup
  clear-all
  foreach sort patches [
    ask ? [
      sprout 1 [
        if who >= length shapes [ die ]
        set shape item who shapes
        set heading 0
        set color gray
        set size 5 / 6
      ]
    ]
  ]
  if count turtles < length shapes 
    [ user-message "Not enough turtles." ]
end

to export
  export-view user-new-file
end
@#$#@#$#@
GRAPHICS-WINDOW
244
10
674
401
-1
-1
60.0
1
10
1
1
1
0
1
1
1
0
6
0
5
0
0
0
ticks

BUTTON
49
93
115
126
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

BUTTON
120
93
193
126
NIL
export
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL

MONITOR
76
42
169
87
NIL
length shapes
3
1
11

TEXTBOX
27
144
230
226
For 37 shapes, use a 7x6 world and a patch size of 60.\n\nFor 223 shapes, use an 11x21 world and a patch size of 36.
11
0.0
1

@#$#@#$#@
This model is used to generate the two images used in the Shapes section of the manual.
@#$#@#$#@
@#$#@#$#@
NetLogo 4.1RC1
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

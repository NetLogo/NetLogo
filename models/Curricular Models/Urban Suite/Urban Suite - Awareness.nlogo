turtles-own [ awareness ]
patches-own [ non-usage ]


to setup
  ca
  set-default-shape turtles "person"
  crt people

  ask turtles
    [ set color gray
      fd random 25
      setxy random-xcor random-ycor
      set awareness 0
    ]

  ask n-of centers patches
    [ set pcolor green
      set non-usage 0
    ]
  reset-ticks
end

to wander
  fd 1 lt random 50 rt random 50
end

to go
  ask turtles [ wander ]

  ;; adjust awareness according to our location
  ask turtles
    [ if ( pcolor = green or pcolor = blue ) [ set awareness awareness + 5 ]
      if ( pcolor = black ) [ set awareness awareness - 1 ]
      set non-usage 0
    ]

  ; inform others around us
  ask turtles with [ well-informed? or activist? ]
    [ ask other turtles-here with [ unaware? or aware? ]
        [ set awareness awareness + 1 ]
    ]

  ; place limits on the awareness value
  ask turtles with [ awareness > 15 ] [ set awareness 15 ]   ;; setting max awareness
  ask turtles with [awareness < 0 ] [ set awareness 0 ]      ;; setting minimum awareness

  color-turtles

  ;; create centers
  ask turtles with [ activist? ]
    [ let open-patches neighbors with [ pcolor = black ]
      if ( any? open-patches )
        [ ask one-of open-patches [ set pcolor blue ] ]
    ]  ;; a random patch turns green

  ;;
  ask patches with [ pcolor != black ]           ;;WE WANT NON-USED CENTERS TO DISAPPEAR
    [
      if ( not any? turtles-here ) [ set non-usage (non-usage + 1) ]
      if ( non-usage > non-usage-limit ) [ set pcolor black ]
    ]

 do-plotting

 tick
end

to color-turtles
  ask turtles [ ifelse ( activist? ) [ set color 64 ]
                [ ifelse ( well-informed? ) [ set color 66 ]
                  [ ifelse ( aware? ) [ set color 68 ]
                    [ set color gray ] ] ] ]
 end

to-report activist?
  report awareness >= 15
end

to-report well-informed?
  report awareness >= 10 and awareness < 15
end

to-report aware?
  report awareness >= 5 and awareness < 10
end

to-report unaware?
  report awareness >= 0 and awareness < 5
end

to place-centers
  if ( mouse-down? ) [ ask patch mouse-xcor mouse-ycor [ set pcolor green ] display ]
end

to do-plotting
  set-current-plot "Avg. Awareness"
  set-current-plot-pen "Awareness"
    plot mean [ awareness ] of turtles

  set-current-plot "Level of Awareness"
  set-current-plot-pen "Activist"
    plot count turtles with [ activist? ]
  set-current-plot-pen "Well Informed"
    plot count turtles with [ well-informed? ]
  set-current-plot-pen "Aware"
    plot count turtles with [ aware? ]
  set-current-plot-pen "Unaware"
    plot count turtles with [ unaware? ]

end
@#$#@#$#@
GRAPHICS-WINDOW
315
10
769
497
-1
-1
12.0
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
36
0
37
1
1
1
ticks
30

BUTTON
10
10
75
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

BUTTON
10
50
75
83
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

MONITOR
10
285
75
330
Activist
count turtles with [color = 64]
2
1
11

SLIDER
80
10
185
43
people
people
0
300
60
10
1
NIL
HORIZONTAL

MONITOR
235
285
305
330
Unaware
count turtles with [color = gray]
3
1
11

PLOT
10
135
305
285
Level of Awareness
Time
Amount
0.0
3.0
0.0
10.0
true
true
"" ""
PENS
"Activist" 1.0 0 -14439633 true "" ""
"Well Informed" 1.0 0 -11085214 true "" ""
"Aware" 1.0 0 -5509967 true "" ""
"Unaware" 1.0 0 -7500403 true "" ""

SLIDER
80
50
185
83
centers
centers
0
100
20
5
1
NIL
HORIZONTAL

MONITOR
10
345
110
390
Avg. Awareness
mean [awareness] of turtles
3
1
11

PLOT
110
345
305
510
Avg. Awareness
Time
Avg. awareness
0.0
50.0
0.0
20.0
true
false
"" ""
PENS
"Awareness" 1.0 0 -16777216 true "" ""

MONITOR
75
285
165
330
Well Informed
count turtles with [color = 66]
3
1
11

MONITOR
165
285
235
330
Aware
count turtles with [color = 68]
3
1
11

MONITOR
10
410
110
455
Centers
count patches with [ pcolor != black ]
3
1
11

MONITOR
10
455
110
500
Avg. Non-usage
mean [non-usage] of patches with [ pcolor != black ]
3
1
11

SLIDER
10
90
185
123
non-usage-limit
non-usage-limit
0
500
100
5
1
ticks
HORIZONTAL

BUTTON
195
10
305
43
NIL
place-centers
T
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

This model simulates points of information and/or resource exchange in an urban environment. An urban environment is assumed to be a pedestrian friendly city-space where people normally encounter one another on a face-to-face basis; and, typically encounter informational systems (such as advertising) and exchange systems (such as consumer based shopping).

The object of the model is to simulate people's awareness of the value of exchanging resources, and evaluate the influence "aware" people have on one another, and on their environment in an information-rich context such as a city.

## HOW IT WORKS

The model determines a person's theoretical level of "awareness" within an urban environment based upon a person's random encounter with information centers. In the model, information centers are any source of positive information exchange such as an advertisement (for a public good) or a recycling center. In general terms, "awareness" involves a person showing realization, perception, or knowledge.

In this model, each person has some amount of "awareness", which is measured in "awareness points".  There is a discrete set of "levels" of awareness that people may attain.  A person may be "unaware" (0 - 5 points), "aware" (5 - 10 points), "well-informed" (10 - 15 points), or an "activist" (more than 15 points).

To gain awareness, a person either runs into a center, where they gain five awareness points; or is influenced by a person who is well-informed or an activist, where they gain one awareness point. If one of these events does not occur during a given time step (tick), the person will lose one awareness point (down to zero).  In this model, there is no such thing as "negative awareness".

(The idea of negative awareness may sound ridiculous, but it could make sense in some situations -- for instance, if some faction is spreading information that is in direct conflict to another faction, and people may come into contact with information and advertising promoting either position.  That is, negative awareness might represent "subscription to an opposing and irreconcilable viewpoint".  For instance, in the United States, there are activists working both for and against the legality of abortion.)

When a person becomes an activist (15 awareness points), a new center is formed.  The new information centers are colored blue, whereas the initial information centers are green.

If no one comes into contact with a center for a specified amount of time (see the NON-USAGE-LIMIT slider), the center disappears from the world.  The intuition here is that if an information/advertising method or location is yielding no fruit, eventually it will be shut down.

## HOW TO USE IT

Press SETUP and then GO.

The PEOPLE slider determines how many people "agents" are randomly distributed in the initial setup phase of the model

The CENTERS slider determines how many information centers are randomly distributed in the initial setup of the model.

The NON-USAGE-LIMIT determines how many ticks a center can go unused before being shut down.

Use the PLACE-CENTERS button to manually place information centers on the view (by clicking their locations with the mouse, while the PLACE-CENTERS button is turned on).

There are also numerous monitors that display information about the current state of the world, such as the current breakdown of awareness in the population, via the ACTIVIST, "WELL INFORMED", AWARE, and UNAWARE monitors.

The CENTERS monitor tells how many information centers are present in the world.

The AVG. NON-USAGE monitor tells the average number of ticks it has been since each of the information centers has been used (i.e. influenced a person).

The AVERAGE STATE OF AWARENESS monitor tells the average number of awareness points that people in the population have.

The LEVELS OF AWARENESS plot shows the history of how many people were at each level of awareness at each tick of the model, and the AVG. AWARENESS plot keeps track of the average awareness of the population over time.

## THINGS TO NOTICE

The initial relative density of people to centers is vital to achieving systemic balance. The model simulates a complex system of data exchange by exploring positive feedback; and the model was created as a lens to describe one important process of emergent pattern formation in a sustainable city.  Specifically, the model allows us to study and discuss the important relationship between a population and its ability to learn and become participatory in the building of its own environment. Here are some questions to encourage discussion about the model and the topics it broaches.

Is there a minimum number of people or centers needed to eventually make everyone an activist?  Does it happen suddenly or gradually?  You can see this both visually, and it is represented in both the LEVELS OF AWARENESS plot, and the AVG. AWARENESS plot.

Where do new information centers tend to form?

What if you only look at the number of "aware" or "well-informed" people over time -- what does that plot look like?  Can you explain its shape?

## THINGS TO TRY

Run the model with 200 PEOPLE, 50 CENTERS, and 100 ticks for the NON-USAGE-LIMIT.  Now try decreasing the NON-USAGE-LIMIT slider.  How low can you go before global awareness isn't achieved?  Does it help to raise the initial number of people or centers?

Try manually placing 20 centers (using the PLACE-CENTERS button) spread out across the world, and run the model.  Now try manually placing just 5 centers, but in a tight cluster.  What are the results?  Do you think this result is realistic, or is indicative of a faulty model of how awareness and activism occurs?

## EXTENDING THE MODEL

Try changing the model so that it simulates two competing and opposed viewpoints (such as legalizing marijuana, or perhaps something more broad, such as Republican versus Democrat politics).  Do this by allowing negative awareness, and have people with less than -15 awareness points be anti-activists, etc.

What if there were more than two opposing points of view?

## NETLOGO FEATURES

It is very common in agent-based models to initialize the setup of the model by positioning agents randomly in the world.  NetLogo makes it easy to move an agent to a random location, with the following code: "SETXY RANDOM-XCOR RANDOM-YCOR".

## RELATED MODELS

This model is related to all of the other models in the "Urban Suite".

This model is also similar to the Rumor Mill model, which is found in the NetLogo models library.

## CREDITS AND REFERENCES

The original version of this model was developed during the Sprawl/Swarm Class at Illinois Institute of Technology in Fall 2006 under the supervision of Sarah Dunn and Martin Felsen, by the following students: Eileen Pedersen, Brian Reif, and Susana Odriozola.  See http://www.sprawlcity.us/ for more information about this course.

Further modifications and refinements were made by members of the Center for Connected Learning and Computer-Based Modeling before releasing it as an Urban Suite model.

The Urban Suite models were developed as part of the Procedural Modeling of Cities project, under the sponsorship of NSF ITR award 0326542, Electronic Arts & Maxis.

Please see the project web site ( http://ccl.northwestern.edu/cities/ ) for more information.
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

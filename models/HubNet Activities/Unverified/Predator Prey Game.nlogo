globals
[
  start-energy      ;; constant used to initialize turtles

  colors            ;; list that holds the colors used for students' turtles
  color-names       ;; list that holds the names of the colors used for students' turtles
  all-shapes        ;; list that holds all the possible shapes used in the game
]

breed [ icons icon ]
breed [ androids android ]
breed [ students student ]

androids-own
[
  predator?
  energy
]

students-own
[
  user-id
  my-icon
  predator?
  energy
  step-size
]

icons-own
[
  color-name
]

;;
;; Setup Procedures
;;
to startup
  ;; standard hubnet setup code
  hubnet-reset

  ;; setup the model itself
  setup-vars
  setup
end

;; initialize the global variables used throughout
;; this only needs to be called once during startup
to setup-vars
  set start-energy 10
  set colors (list white orange red yellow
                    (violet + 1) (sky + 1) pink )
  set color-names ["white" "orange" "red" "yellow"
                   "purple" "blue" "pink" ]
  set all-shapes [ "diamond" "heart" "pentagon" "square" "circle" "triangle" "rhombus" ]
end

;; return all students to initial state and randomize locations
;; kill all androids
to setup
  reset-ticks
  clear-all-plots
  setup-patches

  ;; reset the students to a new location with new energy
  ask students
  [
    move-to one-of patches
    face one-of neighbors4
    set energy start-energy
    ifelse predator?
    [ set shape "wolf" ]
    [ set shape "sheep" ]
    ask my-icon [ move-to myself ]
    send-info-to-client
  ]

  ;; kill all the androids
  ask androids
    [ die ]
end

;; make grass on half the patches.
to setup-patches
  ask patches
  [
    ifelse random 2 = 0
      [ set pcolor green - 3 ]
      [ set pcolor brown - 2 ]
  ]
end

;; create a new android prey
to add-prey
  create-androids 1
  [
    move-to one-of patches
    set color gray
    face one-of neighbors4
    set predator? false
    set shape "sheep"
    set energy start-energy
  ]
end

;; create a new android predator
to add-predators
  create-androids 1
  [
    move-to one-of patches
    face one-of neighbors4
    set color black
    set predator? true
    set energy start-energy
    set shape "wolf"
  ]
end

;;
;; Runtime Procedures
;;

;; the main procedure
to go
  ;; listen to the hubnet client
  every 0.1
  [
    listen-clients
    display
  ]

  ;; if wander? is true then the androids wander around the landscape
  if wander?
    [ androids-wander ]

  ;; the delay below keep plants from growing too fast
  ;; and predator/prey from losing points too fast
  every 3
  [
    if any? turtles
    [
      plants-regrow
      ask students
      [
        set energy energy - 0.5
        if energy <= 0
        [ student-die ]
        update-energy-monitor
      ]
      do-plot
    ]
    tick
  ]
end

;; android animals should move at random
to androids-wander

  ;; only execute at android-delay rate to keep the androids from moving too fast
  every android-delay
  [
    ask androids
    [
      face one-of neighbors4
      fd 1
      ifelse not predator?
      [
        eat-grass
        reproduce-android-prey
      ]
      [
        eat-prey
        reproduce-android-predator
      ]
      set energy energy - 0.5
      if energy <= 0
        [ die ]
    ]
  ]
end

;; turtle procedure
;; creates a new android near the old one
to reproduce-android-prey
  if random-float 100 < prey-reproduce [
    set energy (energy / 2)
    hatch 1 [ rt random-float 360 fd 1 ]
  ]
end

;; turtle procedure
;; creates a new android near the old one
to reproduce-android-predator
  if random-float 100 < predator-reproduce [
    set energy (energy / 2 )
    hatch 1 [ rt random-float 360 fd 1 ]
  ]
end

;; regrow the grass
to plants-regrow
  let brown-patches patches with [ pcolor = brown - 2 ]

  ;; only regrow until the entire world is full
  ifelse count brown-patches > plant-regrowth-rate
  [
    ask n-of plant-regrowth-rate brown-patches
      [ set pcolor green - 3 ]
  ]
  [
    ask brown-patches [ set pcolor green - 3 ]
  ]
end

;; turtle procedure
;; if you are a predator consume a prey
to eat-prey
  let eaten-prey one-of other turtles-here with [ breed != icons and not predator? and energy > 0 ]
  if eaten-prey != nobody
  [
    set energy energy + predator-gain-from-food
    ;; prey lose energy equal to one "life" when they are eaten
    ;; which is the amount of energy that everyone starts with.
    ask eaten-prey
      [ set energy max list 0 energy - start-energy ]
  ]
end

;; turtle procedure
;; if you are are a prey eat some grass
to eat-grass
  ;; make the colors a little darker to contrast
  ;; with the icon colors.
  if pcolor = green - 3
  [
    set energy energy + prey-gain-from-food
    set pcolor brown - 2
  ]
end

;; force all students to become prey
to make-students-prey
  ask students [
    set predator? false
    set shape "sheep"
    set color grey
    send-info-to-client
    hubnet-send user-id "status" "You are now prey"
  ]
end

;; force all students to become predators
to make-students-predators
  ask students [
    set predator? true
    set shape "wolf"
    set color black
    send-info-to-client
    hubnet-send user-id "status" "You are now a predator"
  ]
end

;;
;; HubNet Procedures
;;

;; get commands from the clients
to listen-clients
  while [ hubnet-message-waiting? ]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ create-new-student ]
    [
      ifelse hubnet-exit-message?
      [ remove-student ]
      [ execute-command hubnet-message-tag ]
    ]
  ]
end

;; how to execute a command that was received
to execute-command [ command ]
  ask students with [ user-id = hubnet-message-source ]
  [
    ;; animals can only move if they have energy
    ;; and can only recreate themselves if they are dead
    ifelse energy > 0
    [
      if command = "up"
      [ execute-move 0 ]
      if command = "down"
      [ execute-move 180 ]
      if command = "right"
      [ execute-move 90 ]
      if command = "left"
      [ execute-move 270 ]
    ]
    [
      if command = "recreate as predator"
      [
        setup-student-vars true
        send-info-to-client
        hubnet-send user-id "status" "You are now a predator"
      ]
      if command = "recreate as prey"
      [
        setup-student-vars false
        send-info-to-client
        hubnet-send user-id "status" "You are now prey"
      ]
    ]
    if command = "Change Appearance"
    [ execute-change-turtle ]
    if command = "step-size"
    [
      set step-size hubnet-message
    ]
  ]
end

to student-die
  set shape "face sad"
  send-info-to-client
  update-energy-monitor
end

;; create a new student agent
to create-new-student
  create-students 1
  [
    init-student
    send-info-to-client
  ]
end

;; turtle procedure
;; initialize the student agent
to init-student
  set user-id hubnet-message-source
  move-to one-of patches
  create-icon
  setup-student-vars random 2 = 0
end

;; to create the variables for a student agent
to setup-student-vars [is-predator?]  ;; turtle procedure
  set predator? is-predator?
  ifelse predator?
  [ set shape "wolf" ]
  [ set shape "sheep" ]
  face one-of neighbors4
  set step-size 1
  set energy start-energy
end

;; turtle procedure
;; create the icon for each student
to create-icon
  let parent self
  hatch-icons 1
  [
    ask parent [ set my-icon myself ]
    move-to parent
    set-unique-shape-and-color
    set size 1.5
  ]
end

;; turtle procedure
;; make sure each student has a unique shape and color
to set-unique-shape-and-color
  set color one-of colors
  set shape one-of all-shapes
  set color-name item (position color colors) color-names
end

;; sends the appropriate monitor information back to the client
to send-info-to-client
  hubnet-send user-id "You are a:" (word [color-name] of my-icon " " [shape] of my-icon)
  hubnet-send user-id "Located at:" (word "(" pxcor "," pycor ")")
  hubnet-send user-id "role" my-role
  update-energy-monitor
end

;; turtle procedure
;; specify the role
to-report my-role
  if energy <= 0
    [ report "dead" ]
  report ifelse-value (shape = "wolf") [ "predator" ][ "prey" ]
end

;; show the appropriate amount of energy
to update-energy-monitor
  hubnet-send user-id "energy" max list 0 precision energy 2
  if energy <= 0
  [
    hubnet-send user-id "role" my-role
    hubnet-send user-id "status" "You died, you may recreate as a predator or prey"
  ]
end

;; remove students that are no longer connected
to remove-student
  ask students with [ user-id = hubnet-message-source ]
  [
    ask my-icon [ die ]
    die
  ]
end

;; turtle procedure
;; move the student agent
to execute-move [ new-heading ]
  set heading new-heading
  fd step-size
  set energy energy - 0.5
  ask my-icon
  [
    set heading new-heading
    fd [step-size] of myself
  ]
  ifelse predator?
    [ eat-prey ]
    [ eat-grass ]
  update-energy-monitor
  hubnet-send user-id "Located at:" (word "(" pxcor "," pycor ")")
  if energy <= 0
  [ student-die ]
end

;; turtle procedure
;; change the icon
to execute-change-turtle
  show-turtle
  ask my-icon [ set-unique-shape-and-color ]
  hubnet-send user-id "You are a:" (word ([color-name] of my-icon) " " [shape] of my-icon)
end

;;
;; Plotting Procedure
;;
to do-plot
  let players turtles with [ breed != icons ]
  set-current-plot "Average Energy"  ;;this plots the total energy of prey, and of predators
  set-current-plot-pen "Predators"
  ifelse any? players with [ predator? ]
    [ plot mean [ energy ] of  players with [ predator? ] ]
    [ plot 0 ]

  set-current-plot-pen "Prey"
  ifelse any? players with [ not predator? ]
    [ plot mean [ energy ] of players with [ not predator? ] ]
    [ plot 0 ]

  set-current-plot "Population"
  set-current-plot-pen "Predators"
  plot count players with [ predator? ]

  set-current-plot-pen "Prey"
  plot count players with [ not predator? ]
end
@#$#@#$#@
GRAPHICS-WINDOW
438
41
973
597
10
10
25.0
1
10
1
1
1
0
0
0
1
-10
10
-10
10
1
1
1
ticks

BUTTON
119
21
204
57
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
206
21
291
57
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
65
91
204
124
NIL
add-prey
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
28
315
393
512
Average Energy
time
points
0.0
25.0
0.0
100.0
true
false
"" ""
PENS
"predators" 1.0 0 -2674135 true "" ""
"prey" 1.0 0 -10899396 true "" ""

SLIDER
206
91
400
124
android-delay
android-delay
0
10
0.4
0.1
1
seconds
HORIZONTAL

SWITCH
206
59
353
92
wander?
wander?
0
1
-1000

BUTTON
65
59
204
92
NIL
add-predators
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
181
212
406
245
predator-gain-from-food
predator-gain-from-food
0
20
10
1
1
energy
HORIZONTAL

SLIDER
181
245
406
278
prey-gain-from-food
prey-gain-from-food
0
10
2
1
1
energy
HORIZONTAL

SLIDER
93
280
324
313
plant-regrowth-rate
plant-regrowth-rate
0
100
5
1
1
patches
HORIZONTAL

MONITOR
130
162
204
207
predators
count androids with [ predator? ] + count students with [ predator? ]
0
1
11

MONITOR
206
162
279
207
prey
count androids with [ not predator? ] + count students with [ not predator? ]\n
3
1
11

BUTTON
14
245
180
278
NIL
make-students-prey
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
14
212
180
245
NIL
make-students-predators
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
28
514
393
711
Population
NIL
NIL
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"predators" 1.0 0 -2674135 true "" ""
"prey" 1.0 0 -10899396 true "" ""

SLIDER
10
126
204
159
predator-reproduce
predator-reproduce
0
100
5
0.1
1
%
HORIZONTAL

SLIDER
206
126
400
159
prey-reproduce
prey-reproduce
0
100
4
0.1
1
%
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This model simulates a predator-prey relationship.  The population consists of wolf packs (predators) and sheep herds (prey), some controlled by students via HubNet clients and some androids controlled by the computer.  The wolves gain energy from consuming sheep, and the sheep gain energy from consuming grass (a primary producer).  The model allows students to examine simple population dynamics like those modeled through the Lotka-Volterra equations in a participatory way.

## HOW IT WORKS

Predators and prey move around the world, searching for food.  Prey eat plants (green areas), while predators eat prey.  All animals gain energy by finding food and expend energy in the search for food.  Prey also lose energy when predators catch them. Each predator agent can be thought of as a pack of wolves and each prey can be thought of a herd of sheep.  Prey can be caught by predators a few at a time before they ultimately die out.  In other words, a prey agent does not die the very first time they run into a predator.  The grass is also a limited resource; it regrows at a fixed rate.  Android predators and prey can wander around the world at random, while student predators and prey are directed through the HubNet clients.

If an animal's energy gets too low it dies. If a student controls the animal, that student is given the option to "recreate" as a predator or prey.  Until the student "recreates" they cannot interact with the rest of the system, i.e. they cannot be eaten, they cannot eat, and they cannot move.  By recreating the students have some control over the populations in the world and can dramatically shift the population dynamics.  If the energy of an android goes below zero the android merely dies.  However, androids do have the ability to reproduce, which student agents do not.  Every tick each prey has a PREY-REPRODUCE chance of creating a new prey, and each predator has a PREDATOR-REPRODUCE chance of creating a new predator.

## HOW TO USE IT

To start the game first press the SETUP button then press the GO button and ask the students to log in.  You may also want to create android predators or prey using the ADD-PREDATORS and ADD-PREY buttons.  You can control the movement of the androids using the WANDER? switch (which determines whether they move at all) and the ANDROID-DELAY slider (which determines how many seconds elapse between android movements).

The total number of PREDATORS and PREY, including both students and androids are displayed in the corresponding monitors.  You can control the amount of energy each animal gains from consuming food by using the PREDATOR-GAIN-FROM-FOOD and the PREY-GAIN-FROM-FOOD sliders.  PLANT-REGROWTH-RATE brown areas regrow grass at every tick in the model.  Thus modifying this slider controls the amount of grass that is available for the sheep to consume.

If you wish to change all the students to either predators or prey press the MAKE-STUDENTS-PREDATORS or MAKE-STUDENTS-PREY buttons.  These buttons do not affect androids.

To start the game over press the SETUP button, this will return all students to initial energy levels and place them at a new random position.  It will kill all the androids and clear the plots.

AVERAGE ENERGY plots the average energy of all predators in red, and all prey in green, this includes androids.  POPULATION plots the total of each type of animal.

Client Information

After logging in, the client interface will appear for the students, and if GO is pressed in NetLogo they will be assigned a role as predator or prey at random.  Their character will consist of an animal indicator, a wolf if they are a predator or a sheep if they are prey, and an icon.  The icon is their unique identifier so it's easy to distinguish themselves from other animals.  A description of the icon will appear in the YOU ARE A: monitor.  If the student does not like the shape and/or color of their icon they can hit the CHANGE APPEARANCE button at any time to change to another random icon.

The student controls the movement of their turtle with the UP, DOWN, LEFT, and RIGHT buttons (they can also use the hot-keys as short cuts) and the STEP-SIZE slider.  Clicking any of the directional buttons (or using the hot-keys) will cause their turtle to move in the respective direction a distance of STEP-SIZE.

ENERGY for the appropriate animal is displayed in the monitor of same name.  If the energy is zero or less, then the agent dies.  The ROLE monitor will display "dead", the animal indicator shape will appear as a sad face, and the STATUS monitor will indicate that the student should recreate as a predator or prey.  After s/he selects one s/he cannot switch roles until the animal dies again.

## THINGS TO NOTICE

Predators may find it better to stay together.  Prey, however, may learn to stay more scattered, so as to not become easy prey to a large group of predators.

At first students may find it advantageous to be predators but if many students become predators, the balance will shift and it may be more advantageous to "recreate" as a prey.

## THINGS TO TRY

Try changing the amount of energy gained from food for both predators and prey, at what point does it cease to be advantageous to be a predator?

Try to balance out the system so that no predators or prey go hungry.

Change the PLANT-REGROWTH-RATE slider.  How does this affect the balance between the predators and prey?

## EXTENDING THE MODEL

Try making the androids move toward food rather than moving randomly.

There is no cost for changing the step-size for a student agent.  Is there a way a larger step-size could "cost" the agent something?

## NETLOGO FEATURES

Because so much information must be conveyed through the appearance of each turtle, each player is actually made up of two turtles; one turtle's shape is either a wolf or a sheep to indicate whether it is a predator or prey.  Behind the first turtle is an icon turtle that helps students distinguish themselves from other players (and androids).  Thus though each student is viewed as one agent, they are actually made up of two turtles.

## RELATED MODELS

The various Wolf-Sheep models are non-HubNet versions of a similar model.  Also see the Bug Hunter model and the Guppy model for other HubNet models that explore population dynamics.

## CREDITS AND REFERENCES
@#$#@#$#@
default
false
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

cat
false
0
Line -7500403 true 285 240 210 240
Line -7500403 true 195 300 165 255
Line -7500403 true 15 240 90 240
Line -7500403 true 285 285 195 240
Line -7500403 true 105 300 135 255
Line -16777216 false 150 270 150 285
Line -16777216 false 15 75 15 120
Polygon -7500403 true true 300 15 285 30 255 30 225 75 195 60 255 15
Polygon -7500403 true true 285 135 210 135 180 150 180 45 285 90
Polygon -7500403 true true 120 45 120 210 180 210 180 45
Polygon -7500403 true true 180 195 165 300 240 285 255 225 285 195
Polygon -7500403 true true 180 225 195 285 165 300 150 300 150 255 165 225
Polygon -7500403 true true 195 195 195 165 225 150 255 135 285 135 285 195
Polygon -7500403 true true 15 135 90 135 120 150 120 45 15 90
Polygon -7500403 true true 120 195 135 300 60 285 45 225 15 195
Polygon -7500403 true true 120 225 105 285 135 300 150 300 150 255 135 225
Polygon -7500403 true true 105 195 105 165 75 150 45 135 15 135 15 195
Polygon -7500403 true true 285 120 270 90 285 15 300 15
Line -7500403 true 15 285 105 240
Polygon -7500403 true true 15 120 30 90 15 15 0 15
Polygon -7500403 true true 0 15 15 30 45 30 75 75 105 60 45 15
Line -16777216 false 164 262 209 262
Line -16777216 false 223 231 208 261
Line -16777216 false 136 262 91 262
Line -16777216 false 77 231 92 261

circle
false
0
Circle -7500403 true true 0 0 300

cow
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

diamond
false
0
Polygon -7500403 true true 150 17 270 149 151 272 30 152

dog
false
0
Polygon -7500403 true true 300 165 300 195 270 210 183 204 180 240 165 270 165 300 120 300 0 240 45 165 75 90 75 45 105 15 135 45 165 45 180 15 225 15 255 30 225 30 210 60 225 90 225 105
Polygon -16777216 true false 0 240 120 300 165 300 165 285 120 285 10 221
Line -16777216 false 210 60 180 45
Line -16777216 false 90 45 90 90
Line -16777216 false 90 90 105 105
Line -16777216 false 105 105 135 60
Line -16777216 false 90 45 135 60
Line -16777216 false 135 60 135 45
Line -16777216 false 181 203 151 203
Line -16777216 false 150 201 105 171
Circle -16777216 true false 171 88 34
Circle -16777216 false false 261 162 30

face sad
false
1
Circle -7500403 true false 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 168 90 184 62 210 47 232 67 244 90 220 109 205 150 198 192 205 210 220 227 242 251 229 236 206 212 183

heart
false
0
Circle -7500403 true true 31 30 122
Circle -7500403 true true 147 32 120
Polygon -7500403 true true 51 135 151 243 250 133 264 105 169 84 108 84 44 118 44 126
Polygon -7500403 true true 46 131 150 242 49 114
Polygon -7500403 true true 44 130 150 242 38 105 36 112

monster
false
0
Polygon -7500403 true true 75 150 90 195 210 195 225 150 255 120 255 45 180 0 120 0 45 45 45 120
Circle -16777216 true false 165 60 60
Circle -16777216 true false 75 60 60
Polygon -7500403 true true 225 150 285 195 285 285 255 300 255 210 180 165
Polygon -7500403 true true 75 150 15 195 15 285 45 300 45 210 120 165
Polygon -7500403 true true 210 210 225 285 195 285 165 165
Polygon -7500403 true true 90 210 75 285 105 285 135 165
Rectangle -7500403 true true 135 165 165 270

pentagon
false
0
Polygon -7500403 true true 150 15 15 120 60 285 240 285 285 120

rhombus
false
0
Polygon -7500403 true true 100 51 291 50 189 221 2 222

shark
false
0
Polygon -7500403 true true 283 153 288 149 271 146 301 145 300 138 247 119 190 107 104 117 54 133 39 134 10 99 9 112 19 142 9 175 10 185 40 158 69 154 64 164 80 161 86 156 132 160 209 164
Polygon -7500403 true true 199 161 152 166 137 164 169 154
Polygon -7500403 true true 188 108 172 83 160 74 156 76 159 97 153 112
Circle -16777216 true false 256 129 12
Line -16777216 false 222 134 222 150
Line -16777216 false 217 134 217 150
Line -16777216 false 212 134 212 150
Polygon -7500403 true true 78 125 62 118 63 130
Polygon -7500403 true true 121 157 105 161 101 156 106 152

sheep
false
0
Rectangle -1 true false 30 75 210 225
Circle -1 true false 135 75 150
Rectangle -16777216 true false 166 225 219 286
Rectangle -16777216 true false 62 225 120 285
Circle -16777216 true false 207 88 92

square
false
0
Rectangle -7500403 true true 30 30 270 270

squirrel
false
0
Polygon -7500403 true true 87 267 106 290 145 292 157 288 175 292 209 292 207 281 190 276 174 277 156 271 154 261 157 245 151 230 156 221 171 209 214 165 231 171 239 171 263 154 281 137 294 136 297 126 295 119 279 117 241 145 242 128 262 132 282 124 288 108 269 88 247 73 226 72 213 76 208 88 190 112 151 107 119 117 84 139 61 175 57 210 65 231 79 253 65 243 46 187 49 157 82 109 115 93 146 83 202 49 231 13 181 12 142 6 95 30 50 39 12 96 0 162 23 250 68 275
Polygon -16777216 true false 237 85 249 84 255 92 246 95
Line -16777216 false 221 82 213 93
Line -16777216 false 253 119 266 124
Line -16777216 false 278 110 278 116
Line -16777216 false 149 229 135 211
Line -16777216 false 134 211 115 207
Line -16777216 false 117 207 106 211
Line -16777216 false 91 268 131 290
Line -16777216 false 220 82 213 79
Line -16777216 false 286 126 294 128
Line -16777216 false 193 284 206 285

star
false
0
Polygon -7500403 true true 152 2 196 105 297 106 215 176 248 277 151 209 56 278 87 172 4 107 108 104

triangle
false
0
Polygon -7500403 true true 150 30 15 255 285 255

turtle
true
0
Polygon -10899396 true false 215 204 240 233 246 254 228 266 215 252 193 210
Polygon -10899396 true false 195 90 225 75 245 75 260 89 269 108 261 124 240 105 225 105 210 105
Polygon -10899396 true false 105 90 75 75 55 75 40 89 31 108 39 124 60 105 75 105 90 105
Polygon -10899396 true false 132 85 134 64 107 51 108 17 150 2 192 18 192 52 169 65 172 87
Polygon -10899396 true false 85 204 60 233 54 254 72 266 85 252 107 210
Polygon -7500403 true true 119 75 179 75 209 101 224 135 220 225 175 261 128 261 81 224 74 135 88 99

wolf
false
0
Rectangle -16777216 true false 15 105 105 165
Rectangle -16777216 true false 45 90 105 105
Polygon -16777216 true false 60 90 83 44 104 90
Polygon -16777216 true false 67 90 82 59 97 89
Rectangle -1 true false 48 93 59 105
Rectangle -16777216 true false 51 96 55 101
Rectangle -16777216 true false 0 121 15 135
Rectangle -16777216 true false 15 136 60 151
Polygon -1 true false 15 136 23 149 31 136
Polygon -1 true false 30 151 37 136 43 151
Rectangle -16777216 true false 105 120 263 195
Rectangle -16777216 true false 108 195 259 201
Rectangle -16777216 true false 114 201 252 210
Rectangle -16777216 true false 120 210 243 214
Rectangle -16777216 true false 115 114 255 120
Rectangle -16777216 true false 128 108 248 114
Rectangle -16777216 true false 150 105 225 108
Rectangle -16777216 true false 132 214 155 270
Rectangle -16777216 true false 110 260 132 270
Rectangle -16777216 true false 210 214 232 270
Rectangle -16777216 true false 189 260 210 270
Line -16777216 false 263 127 281 155
Line -16777216 false 281 155 281 192

@#$#@#$#@
NetLogo 5.0beta1
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
VIEW
300
19
720
439
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
-10
10
-10
10

BUTTON
108
158
200
191
up
NIL
NIL
1
T
OBSERVER
NIL
W

BUTTON
108
226
200
259
down
NIL
NIL
1
T
OBSERVER
NIL
S

BUTTON
201
193
273
226
right
NIL
NIL
1
T
OBSERVER
NIL
D

BUTTON
35
192
107
225
left
NIL
NIL
1
T
OBSERVER
NIL
A

SLIDER
108
192
200
225
step-size
step-size
1
5
1
1
1
NIL
HORIZONTAL

MONITOR
158
106
250
155
Located at:
NIL
3
1

MONITOR
82
21
232
70
You are a:
NIL
3
1

BUTTON
83
72
233
105
Change Appearance
NIL
NIL
1
T
OBSERVER
NIL
NIL

MONITOR
108
260
200
309
energy
NIL
0
1

MONITOR
8
311
297
360
status
NIL
3
1

BUTTON
79
363
236
396
recreate as predator
NIL
NIL
1
T
OBSERVER
NIL
NIL

BUTTON
79
397
236
430
recreate as prey
NIL
NIL
1
T
OBSERVER
NIL
NIL

MONITOR
64
106
156
155
role
NIL
3
1

@#$#@#$#@
default
0.0
-0.2 0 1.0 0.0
0.0 1 1.0 0.0
0.2 0 1.0 0.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

@#$#@#$#@
0
@#$#@#$#@

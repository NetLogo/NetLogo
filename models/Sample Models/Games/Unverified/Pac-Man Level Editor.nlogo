breed       [ pacmans pacman ]
breed       [ ghosts ghost ]
breed       [ pellets pellet ]
breed       [ bonuses bonus ]
turtles-own [ home-pos ]
ghosts-own  [ eaten? ]
pellets-own [ powerup? ]
bonuses-own [ value countdown ]

patches-own [ pellet-grid? ]

globals [
  difficulty    ;; Slider in Pac-Man.nlogo
  level         ;; Current Level
  score         ;; Your Score
  lives         ;; Remaining Lives
  extra-lives   ;; Total Number of Extra Lives you've won
  scared        ;; Time until Ghosts aren't scared (0 means not scared)
  level-over?   ;; True when a level is complete
  dead?         ;; True when pacman is loses a life
  next-bonus-in ;; Time until next bonus is created
  tool          ;; The currently selected tool
]


;;;;;;;;;;;;;;;;;;;;;;
;; Setup Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;

;; Setup a new level
to new
  if user-yes-or-no? "Do you really want to clear the level?"
  [
    ca
    set difficulty 0
    set level 0
    set score 0
    set lives 3
    set extra-lives 0
    set scared 0
    set level-over? false
    set dead? false
    set next-bonus-in 0
    set tool "Eraser"

    create-pacmans 1
    [
      set color yellow
      set shape "pacman"
      setxy 10 10
    ]

    create-ghosts 4
    [
      set shape "ghost"
      setxy 0 3
      set heading 0
      set eaten? false
    ]

    ; set ghost colors and initial position
    ask turtle 1 [ set color 15  setxy 10 9 ]
    ask turtle 2 [ set color 26  setxy 10 8 ]
    ask turtle 3 [ set color 127 setxy 10 7 ]
    ask turtle 4 [ set color 85  setxy 10 6 ]

    ask turtles
    [ set home-pos list xcor ycor ]

    ask patches
    [ set pellet-grid? false ]
  ]
end


;;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;;

;; If the mouse is down, use the Current Tool on the patch the mouse is over
to draw
  if mouse-down?
  [
    ;; Eraser Tool - Clears Walls/Gates, Removes Pellets
    if tool = "Eraser"
    [ erase ]
    ;; Wall Tool - Draws a Wall - Neither Pac-Man nor Ghosts can move through walls
    if tool = "Draw Wall"
    [ draw-boundary blue ]
    ;; Gate Tool - Draws a Gate - Only Ghosts can move through Gates
    ;;             Gates also heal ghosts which have been eaten.
    if tool = "Draw Ghost Gate"
    [ draw-boundary gray ]
    ;; Pellet Tool - Places a pellet on the grid
    if tool = "Place Pellet"
    [ place-pellet ]
    ;; Power Pellet Toggle Tool - Changes a Pellet into a Power Pellet and vice versa.
    if tool = "Toggle Power Pellet"
    [ toggle-power-pellet ]
    ;; Pac-Man Tool - Changes Pac-Man's starting position
    if tool = "Place Pac-Man"
    [ place-pacman ]
    ;; Ghost Tool - Changes the starting position of the Ghost chosen by the WHICH-GHOST slider
    if tool = "Place Ghost"
    [ place-ghost ]
  ]
end

;;;;;;;;;;;;;;;;;;;;;
;; Tool Procedures ;;
;;;;;;;;;;;;;;;;;;;;;

;; Clears Walls/Gates, Removes Pellets from the patch the mouse is over
to erase
  ask patch (round mouse-xcor) (round mouse-ycor)
  [
    set pcolor black
    set pellet-grid? false
    ask pellets-here
    [ die ]
  ]
end

;; Draws a Wall or a Gate if there are not any pellets, or Ghosts, or Pac-Man on the patch the mouse is over
to draw-boundary [ boundary-color ]
  ask patch (round mouse-xcor) (round mouse-ycor)
  [
    ifelse not any? turtles-here
    [ set pcolor boundary-color ]
    [
      ifelse boundary-color = gray
      [
        ifelse any? pacmans-here or any? pellets-here
        [ user-message "You cannot place a gate on top of pacman or a pellet." ]
        [ set pcolor boundary-color ]
      ]
      [ user-message "You cannot place a wall on top of pacman, a ghost, or a pellet." ]
    ]
  ]
end

;; Place a pellet on a patch if the patch is not already a Wall or a Gate
to place-pellet
  ask patch (round mouse-xcor) (round mouse-ycor)
  [
    ifelse pcolor != black
    [ user-message "You cannot place a pellet on top of a wall or a gate." ]
    [
      if not any? turtles-here
      [
        sprout-pellets 1
        [
          set color white
          set powerup? false
          set shape "pellet"
        ]
        set pellet-grid? true
        set pcolor black
      ]
    ]
  ]
end

;; Changes a Pellet into a Power Pellet and vice versa.
to toggle-power-pellet
  if any? pellets-on patch mouse-xcor mouse-ycor
  [
    ask one-of pellets-on patch mouse-xcor mouse-ycor
    [
      set powerup? not powerup?
      ifelse powerup?
      [ set shape "circle" ]
      [ set shape "pellet" ]
      wait 0.1
    ]
  ]
end

;; Changes Pac-Man's starting position
to place-pacman
  ifelse [pcolor] of patch round mouse-xcor mouse-ycor != black
  [ user-message "You must place pacman on a corridor space, not a wall or a gate." ]
  [
    ask pacmans
    [ setxy (round mouse-xcor) (round mouse-ycor) ]
  ]
end

;; Changes the starting position of the Ghost chosen by the WHICH-GHOST slider
to place-ghost
  ifelse [pcolor] of patch mouse-xcor mouse-ycor = blue
  [ user-message "You must place a ghost on a corridor space or a gate, not a wall." ]
  [
    ask turtle which-ghost
    [ setxy (round mouse-xcor) (round mouse-ycor) ]
  ]
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Saving and Loading Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Change the Level
to set-level
  if level > 0
  [
    if not user-yes-or-no? "Are you sure you want to change the level number of this map?"
    [ stop ]
  ]
  let temp 0
  while[ temp <= 0 ]
  [
    set temp read-from-string user-input "Input New Level Number:"
    if temp <= 0
    [ user-message "The level must be a positive number." ]
  ]
  set level temp
end

;; If there are pellets and the level has been set, save the file
to save-level
  if not any? pellets
  [
    user-message "You must have at least 1 pellet in a level."
    stop
  ]
  if level <= 0
  [
    user-message "You must choose a positive level number before saving."
    set-level
  ]
  let filepath (word "../pacmap" level ".csv")
  ifelse user-yes-or-no? (word "File will be saved at: " filepath
     "\nIf this file already exists, it will be overwritten.\nAre you sure you want to save?")
  [
    export-world filepath
    user-message "File Saved."
  ]
  [ user-message "Save Canceled. File not saved." ]
end

;; Load a level
to load-level
  let choice 0
  while[ choice <= 0 ]
  [
    set choice read-from-string user-input "Load What Level? (1 or greater)"
    if choice <= 0
    [ user-message "You must choose a positive level number to load." ]
  ]
  let filepath (word "../pacmap" choice ".csv")
  ifelse user-yes-or-no? (word "Load File: " filepath
         "\nThis will clear your current level and replace it with the level loaded."
         "\nAre you sure you want to Load?")
  [
    import-world filepath
    set tool "Eraser"
    user-message "File Loaded."
  ]
  [ user-message "Load Canceled. File not loaded." ]
end
@#$#@#$#@
GRAPHICS-WINDOW
285
10
736
482
10
10
21.0
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
0
0
0
ticks

MONITOR
139
136
274
181
Current Tool
tool
0
1
11

BUTTON
139
261
274
294
Draw Wall
set tool \"Draw Wall\"
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
139
327
274
360
which-ghost
which-ghost
1
4
1
1
1
NIL
HORIZONTAL

BUTTON
139
294
274
327
Draw Gate
set tool \"Draw Ghost Gate\"
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
228
139
261
Place Pellet
set tool \"Place Pellet\"
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
261
139
294
Toggle Power Pellet
set tool \"Toggle Power Pellet\"
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
294
139
327
Place Pac-Man
set tool \"Place Pac-Man\"
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
327
139
360
Place Ghost:
set tool \"Place Ghost\"
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
136
139
185
Use Tool
draw
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
41
139
74
New Level
new
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
139
228
274
261
Eraser
set tool \"Eraser\"
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
216
74
274
119
NIL
level
0
1
11

TEXTBOX
12
202
102
220
Choose Tool:
11
0.0
0

BUTTON
9
74
139
107
Save Level
save-level
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
139
41
274
74
Load Level
load-level
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
139
74
216
123
Set Level
set-level
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

This is a level editor for the Pac-Man model.  It can be used to edit the included levels and to create new levels that can be played in the Pac-Man model.  Familiarity with the Pac-Man model will be very helpful before attempting to create or edit levels.

## HOW IT WORKS

Use the various tools to construct a level for the Pac-Man model.

## HOW TO USE IT

The following are for setup, loading, and saving:

NEW LEVEL - Clears the level and sets it up to start making a new level from scratch.
LOAD LEVEL - Prompts for a level number to load for editing.  The file opened will be the file "pacmap#.csv" (where # is the input level number), and the file must be in the Games folder in the Sample Models section of the Models Library.  (Files will not be visible in the Models Library Browser.)
-- With both NEW LEVEL and LOAD LEVEL, any unsaved changes to a level will be lost (you will be reminded of this and prompted to continue when using these buttons).
SAVE LEVEL - Prompts to save the current level to a file usable by the Pac-Man model.  The file will be saved in the Games folder in the Sample Models section of the Models Library with the Pac-Man model with the file name "pacmap#.csv" (where # is the currently set level number).  Saving a level with the same level number as a previously created level will overwrite the old level (it is not be possible to recover overwritten levels).
SET LEVEL - Sets the current value of 'level' which determines the filename of the level when it is saved with SAVE LEVEL.
LEVEL - This monitor shows the current value of level.

The following are the tools for actually editing levels:

USE TOOL - Allows you to use the current tool on a patch by clicking on it with the mouse.
CURRENT TOOL - Shows what the currently selected tool is.
WHICH-GHOST - This slider determines which Ghost will be moved by the PLACE GHOST tool.

-- The following buttons set the current tool to do different actions.
ERASER - This tool allows you to clear a patch of walls, gates, and pellets.
DRAW WALL - This tool allows you to draw a wall (blue) on the current patch.
DRAW GATE - This tool allows you to draw a gate (gray) on the current patch.
PLACE PELLET - This tool allows you to place a pellet on the grid.
TOGGLE POWER PELLET - This tool allows you to change a Pellet into a Power Pellet and vice versa.
PLACE PAC-MAN - This tool allows you to change Pac-Man's starting position.
PLACE GHOST - This tool allows you to change the starting position of the Ghost chosen by WHICH-GHOST.

## THINGS TO NOTICE

If Pac-Man goes off the edge of the maze he will wrap around to the other side.

Identifying Things in the Maze:
-- Yellow Circle with a mouth:  This is Pac-Man - The protagonist.
-- Small White Circles:
         These are Pellets - Pac-Man will have to collect all of these (including the Power-Pellets) to move on to the next level.

-- Large White Circles:
         These are Power-Pellets - They allow Pac-Man to eat the Ghosts for a limited amount of time.

-- Blue Squares:
                These are the walls of the maze - Neither Pac-Man nor the Ghosts can move through the walls.

-- Gray Squares:
                These are the Ghost Gates - Only Ghosts can move through them, and if they do so after having been eaten they will be healed.

-- Colorful Ghost with Eyes:
    These are the Ghosts - The antagonists.


## THINGS TO TRY

Make a maze by strategically placing gates that has corridors that only Ghosts can travel down, but Pac-Man has to circum-navigate.

## EXTENDING THE MODEL

The Pac-Man model suggests adding new features.  Add tools to this model to allow you to construct these new features in a level.

Add the ability to have a variable number of ghosts in a level (including more than four).

## NETLOGO FEATURES

This model makes use of breeds, create-<breed>, user-message, user-input, user-yes-or-no?, read-from-string, as well as the mouse primitives and import-world and export-world for loading and saving levels.

## RELATED MODELS

Pac-Man

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
Circle -7500403 true true 45 45 210

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

eyes
false
0
Circle -1 true false 62 75 57
Circle -1 true false 182 75 57
Circle -16777216 true false 79 93 20
Circle -16777216 true false 196 93 21

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

ghost
false
0
Circle -7500403 true true 61 30 179
Rectangle -7500403 true true 60 120 240 232
Polygon -7500403 true true 60 229 60 284 105 239 149 284 195 240 239 285 239 228 60 229
Circle -1 true false 81 78 56
Circle -16777216 true false 99 98 19
Circle -1 true false 155 80 56
Circle -16777216 true false 171 98 17

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

pacman
true
0
Circle -7500403 true true 0 0 300
Polygon -16777216 true false 105 0 148 149 195 0

pacman open
true
0
Circle -7500403 true true 0 0 300
Polygon -16777216 true false 255 0 149 152 45 0

pellet
true
0
Circle -7500403 true true 105 105 92

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

scared
false
0
Circle -13345367 true false 61 30 179
Rectangle -13345367 true false 60 120 240 232
Polygon -13345367 true false 60 229 60 284 105 239 149 284 195 240 239 285 239 228 60 229
Circle -16777216 true false 81 78 56
Circle -16777216 true false 155 80 56
Line -16777216 false 137 193 102 166
Line -16777216 false 103 166 75 194
Line -16777216 false 138 193 171 165
Line -16777216 false 172 166 198 192

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
import-world "../pacmap4.csv"
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

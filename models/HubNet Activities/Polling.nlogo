;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variable and Breed declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

globals
[
  current-question  ;; index of the current question in the question list
  question-list     ;; list of all the questions
  voted?-color      ;; color of turtles which have voted
  not-voted?-color  ;; color of turtles which haven't voted

  ;; quick start instructions variables
  quick-start  ;; current quickstart instruction displayed in the quickstart monitor
  qs-item      ;; index of the current quickstart instruction
  qs-items     ;; list of quickstart instructions
]

turtles-own
[
  user-id       ;; unique id, input by the client when they log in, to identify each student turtle
  slider-value  ;; the value of the client's choice slider
  my-choices    ;; list of my choices for each question
]


;;;;;;;;;;;;;;;;;;;;;
;; Setup Functions ;;
;;;;;;;;;;;;;;;;;;;;;

to startup
  hubnet-reset
  setup
end

;; Initializes the display, and creates a list that contains the names of the shapes
;; used by turtles in this activity.  The placement of the shape names in the last
;; corresponds to the numbers sent by calculators.  Also initializes the data lists.
to setup
  ca
  clear-output
  setup-vars
  setup-quick-start
end

;; initialize global variables
to setup-vars
  set not-voted?-color violet - 2
  set voted?-color violet + 2
  clear-all-data-and-questions
end

to clear-clients
  clear-plot
  cp ct
  clear-output
end

to clear-all-data-and-questions
  clear-plot
  hubnet-broadcast "Current Question" ""
  hubnet-broadcast "Current Choice" ""
  set current-question 0
  set question-list [""]
  ask turtles [ clear-my-data ]
end

to clear-my-data  ;; turtle procedure
  set color not-voted?-color
  set my-choices []
  repeat length question-list
  [ set my-choices lput false my-choices ]
end

;; give the user some information about what the setup button does so they can
;; know whether they want to proceed before actually doing the setup
to setup-prompt
  if user-yes-or-no? (word "The SETUP button should only be used when starting "
              "over with a new group (such as a new set of students) since "
              "all data is lost.\n"
              "Are you sure you want to SETUP?")
  [ setup ]
end

to clear-current-data
  clear-plot
  ask turtles
  [
    set color not-voted?-color
    set my-choices replace-item current-question my-choices false
    hubnet-send user-id "Current Choice" ""
  ]
end


;;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;;

to go
  listen-clients
  every 0.5
  [
    ask turtles [ wander ]
    do-plot
  ]
  display
end

to wander  ;; turtle procedure
  face one-of neighbors4
  if not any? turtles-on patch-ahead 1
    [ fd 1 ]
end

to add-question
  if question-list = [""]
  [ set question-list [] ]
  set question-list lput user-input "Input new question?" question-list
  set-current-question (length question-list - 1)
  clear-current-data
end

to set-current-question [n]
  if n >= 0 and n < length question-list
  [
    set current-question n
    ask turtles
    [
      while [length my-choices < current-question + 1]
      [ set my-choices lput false my-choices ]
      ifelse (item current-question my-choices) = false
      [
        set color not-voted?-color
        hubnet-send user-id "Current Choice" ""
      ]
      [
        set color voted?-color
        hubnet-send user-id "Current Choice" (item current-question my-choices)
      ]
    ]
    do-plot
    hubnet-broadcast "Current Question" (item current-question question-list)
  ]
end

to prev-question
  if current-question > 0
  [ set-current-question current-question - 1]
end

to next-question
  if current-question + 1 < length question-list
  [ set-current-question current-question + 1]
end

;;
;; HubNet Procedures
;;

to listen-clients
  while [hubnet-message-waiting?]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ execute-create ]
    [
      ifelse hubnet-exit-message?
      [
        ask turtles with [user-id = hubnet-message-source] [ die ]
        do-plot
      ]
      [
        if hubnet-message-tag = "Choose"
        [ execute-choose ]
        if hubnet-message-tag = "Choice"
        [ change-choice ]
      ]
    ]
  ]
end

to execute-create
  crt 1
  [
    let pos one-of patches with [not any? turtles-here]
    ifelse pos != nobody
    [ move-to pos ]
    [ user-message "Too many students. Make a bigger view." ]
    set user-id hubnet-message-source
    set label word user-id "   "
    set slider-value 0
    clear-my-data
    hubnet-send user-id "Current Question" (item current-question question-list)
  ]
end

to execute-choose
  ask turtles with [user-id = hubnet-message-source]
  [
    if allow-change? or color = not-voted?-color
    [
      set color voted?-color
      set my-choices replace-item current-question my-choices slider-value
      hubnet-send hubnet-message-source "Current Choice" slider-value
    ]
  ]
  do-plot
end

to change-choice
  ask turtles with [user-id = hubnet-message-source]
  [ set slider-value hubnet-message ]
end


;;
;; Plotting Procedures
;;

to do-plot
  clear-plot
  if show-plot?
  [
    let current-data [item current-question my-choices] of turtles
    set current-data remove false current-data
    if not empty? current-data
    [
      set-current-plot-pen "data"
      histogram current-data
      set-current-plot-pen "mean"
      plot-vline mean current-data
      set-current-plot-pen "median"
      plot-vline median current-data
      set-current-plot-pen "mode"
      plot-modes current-data
    ]
  ]
end

to plot-vline [value]
  plotxy value 0
  plotxy value plot-y-max
end

to plot-modes [lst]
  set lst remove false lst
  let values remove-duplicates lst
  ifelse length values = length lst
  [ histogram lst ]  ;; no duplicates so all items are modes
  [
    let counts []
    let i 0
    repeat length values
    [
      set counts lput ((length lst) - (length remove (item i values) lst)) counts
      set i i + 1
    ]
    let n max counts  ;; how many votes for the most frequent choice(s)?
    while [member? n counts]
    [
      set values (replace-item (position n counts) values false)
      set counts (replace-item (position n counts) counts 0)
    ]
    set values remove false values
    set i 0
    repeat length values
    [
      set lst (remove (item i values) lst)
      set i i + 1
    ]
    histogram lst
  ]
end

;;
;; Quick Start Instruction Procedures
;;

to setup-quick-start
  set qs-item 0
  set qs-items
  [
    "Teacher: Follow these directions to run the HubNet activity."
    "Optional: Zoom In (see Tools in the Menu Bar)"
    "Teacher: Open up the HubNet Control Center (see Tools in the Menu Bar)."
      "This will show you the IP Address of this computer."
      "Press the GO button and tell the students to login."
    "Everyone: Open up a HubNet Client on your machine and..."
      "type your user name, select this activity and press ENTER."

    "Teacher: If you don't want to allow students to change their mind,..."
      "switch ALLOW-CHANGE? off."
        "If you don't want to show the current data in the plot as..."
          "it's being collected, switch SHOW-DATA? off."
            "You can turn it back on at any time to see the data."

    "Teacher: Press the NEW QUESTION button to input a question for everyone to answer."
    "Everyone: Once you see the question, move your CHOICE slider to the value..."
      "you want to choose and press the CHOOSE button."
        "Once you have chosen, your choice will appear in your CURRENT CHOICE monitor,..."
          "and your turtle in NetLogo will get brighter."

    "Teacher: To input another question, press NEW QUESTION again."
      "The data and CURRENT QUESTION will be saved and then the question will change..."
        "to your new question and everyone will be able to make their choices on the new question."
          "To return to a previous question, use the <<<PREV QUESTION..."
            "and NEXT QUESTION>>> buttons to cycle through the questions."

    "Teacher: To rerun the activity with the same group, press..."
      "the CLEAR QUESTIONS button to erase all the questions and data."

    "Teacher: To start the simulation over with a new group but reuse the same questions..."
      "stop the model by pressing the NetLogo GO button, if it is on..."
        "and press the CLEAR CLIENTS button."

    "Teacher: To start the simulation over with a new group but with new questions..."
      "stop the GO button, if it is on, and press the SETUP button."

    "Follow these instructions from the beginning."
  ]
  set quick-start (item qs-item qs-items)
end

;; view the next item in the quickstart monitor
to view-next-quick-start
  set qs-item qs-item + 1
  if qs-item >= length qs-items
  [ set qs-item length qs-items - 1 ]
  set quick-start (item qs-item qs-items)
end

;; view the previous item in the quickstart monitor
to view-prev-quick-start
  set qs-item qs-item - 1
  if qs-item < 0
  [ set qs-item 0 ]
  set quick-start (item qs-item qs-items)
end
@#$#@#$#@
GRAPHICS-WINDOW
461
98
744
402
10
10
13.0
1
10
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
0
0
0
ticks

BUTTON
1
41
72
76
Setup
setup-prompt
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
78
41
149
76
Go
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
155
129
455
396
Data
choice
count
0.0
10.0
0.0
5.0
true
true
"" ""
PENS
"data" 1.0 1 -16777216 true "" ""
"mean" 1.0 0 -10899396 true "" ""
"median" 1.0 0 -13345367 true "" ""
"mode" 1.0 1 -2674135 true "" ""

SWITCH
1
224
149
257
allow-change?
allow-change?
0
1
-1000

BUTTON
1
129
149
164
Clear Current Data
clear-current-data
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
1
401
455
446
Current Question
item current-question question-list
3
1
11

BUTTON
1
450
149
483
New Question
add-question
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
1
363
149
396
Clear Questions
clear-all-data-and-questions
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
155
450
304
483
<<<Prev Question
prev-question
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
304
450
455
483
Next Question>>>
next-question
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
1
262
149
295
show-plot?
show-plot?
0
1
-1000

MONITOR
1
170
72
215
mean
mean [item current-question my-choices] of turtles
3
1
11

MONITOR
78
170
149
215
median
median [item current-question my-choices] of turtles
3
1
11

BUTTON
157
59
300
92
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
584
59
736
92
Next Instruction>>>
view-next-quick-start
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
432
59
584
92
<<<Prev Instruction
view-prev-quick-start
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
157
10
736
55
Quick Start Instructions - More in Info Window
quick-start
0
1
11

BUTTON
1
81
149
114
Clear Clients
clear-clients
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

This model can be used to poll data from a set of students using HubNet Clients.  The teacher can input questions to ask and then the students can input their choice in response to the question.  The collective data can then be plotted.

For further documentation, see the Participatory Simulations Guide found at http://ccl.northwestern.edu/ps/

## HOW TO USE IT

Quickstart Instructions:

Teacher: Follow these directions to run the HubNet activity.
Optional: Zoom In (see Tools in the Menu Bar)
Teacher: Open up the HubNet Control Center (see Tools in the Menu Bar).  This will show you the IP Address of this computer. Press the GO button and tell the students to login.
Everyone: Open up a HubNet Client on your machine and input the IP Address of this computer, type your user name in the user name box and press ENTER.

Teacher: If you don't want to allow students to change their mind, switch ALLOW-CHANGE? off.  If you don't want to show the current data in the plot as it's being collected, switch SHOW-PLOT? off.  You can turn it back on at any time to see the data.

Teacher: Press the NEW QUESTION button to input a question for everyone to answer.
Everyone: Once you see the question, move your CHOICE slider to the value you want to choose and press the CHOOSE button.  Once you have chosen, your choice will appear in your CURRENT CHOICE monitor, and your turtle in NetLogo will get brighter.

Teacher: To input another question, press NEW QUESTION again.  The data and CURRENT QUESTION will be saved and then the question will change to your new question and everyone will be able to make their choices on the new question.  To return to a previous question, use the <<<PREV QUESTION and NEXT QUESTION>>> buttons to cycle through the questions.

Teacher: To rerun the activity with the same group, press the CLEAR QUESTIONS button to erase all the questions and data.

Teacher: To start the simulation over with a new group but reuse the same questions, stop the model by pressing the NetLogo GO button, if it is on, and press the CLEAR CLIENTS button.

Teacher: To start the simulation over with a new group but with new questions, stop the GO button, if it is on, and press the SETUP button.

Follow these instructions from the beginning.

Buttons:

SETUP - clears everything including logged in clients.  This should only be pressed when starting out with a new group of users since all data and questions are lost.
GO - runs the simulation processing data from the clients.
CLEAR CURRENT DATA - clears everyone's answers on the current question allowing them to re-choose.
NEW QUESTION - allows you to write a new question to be asked.
CLEAR CLIENTS - removes all the clients and clears the data, but leaves the questions intact.  This should be used to start over with a new group but with the same set of questions.
CLEAR QUESTIONS - erase all the questions and the data associated with them.  This should be used if you want to start over with the same group or don't want clients to have to log back in.
NEXT QUESTION>>> - changes the current question to the next question.
<<<PREV QUESTION - changes the current question to the previous question.
RESET INSTRUCTIONS - resets the quick start instructions to the beginning.
NEXT INSTRUCTION>>> - advances the quick start instructions.
<<<PREV INSTRUCTION - goes backwards through the quick start instructions.

Switches:

ALLOW-CHANGE? - controls whether clients are able to change their choices after they've chosen.  Can be changed at any time.
SHOW-PLOT? - controls whether the data collected are shown in the plot.  Can be changed at any time.

Monitors:

MEAN - shows the mean of the current data (aka average)
MEDIAN - shows the median of the current data (the central value)
CURRENT QUESTION - shows the current question being voted on by the clients

Plots:

DATA - shows a histogram of all the clients' votes as well as the mean, median, and modes of the data.

Client Information:

CURRENT QUESTION - A monitor showing the current question to input a guess on.
CHOICE - A slider for inputing your choice in answer to the current question.
CHOOSE - A button which tells the server what your choice is.
CURRENT CHOICE - Shows the value that the server currently has as your choice for the current question.  It will be blank if you have not yet made a choice for the current question.
DATA - Identical to the plot of the same name in NetLogo.

## EXTENDING THE MODEL

Currently, this activity only allows numeric data to be entered by the client.  Try adding in support for yes/no or true/false answers to questions.

Right now the activity allows you to clear all the questions, clear all the clients and questions, and clear the data for one question.  However, it does not allow you to clear all the data for all the questions.  Make it do so.

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
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
BUTTON
76
127
203
176
Choose
NIL
NIL
1
T
OBSERVER
NIL
NIL

SLIDER
53
89
343
122
Choice
Choice
0
9
0
1
1
NIL
HORIZONTAL

MONITOR
4
35
389
84
Current Question
NIL
3
1

MONITOR
209
127
318
176
Current Choice
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

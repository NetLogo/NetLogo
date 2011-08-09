;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variable and Breed declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

globals [
  quick-start  ;; current quickstart instruction displayed in the quickstart monitor
  qs-item      ;; index of the current quickstart instruction
  qs-items     ;; list of quickstart instructions
]

breed [ students student ]
students-own
[
  user-id    ;; the name selected by each user
  fraction   ;; the fraction of current money the user has selected to invest
  investment ;; the amount the user has invested
  my-money   ;; the money that the user currently has to use
  return-investment ;; the interest plus the principle returned on the investment
  cars        ;; the number of cars purchased
  invested?    ;; boolean variable indicating whether the user has invested this round
]

;;;;;;;;;;;;;;;;;;;;;
;; Setup Functions ;;
;;;;;;;;;;;;;;;;;;;;;

to startup
  ca
  ask patches [ set pcolor gray ]
  hubnet-reset
end

;; reset all the clients to begin the activity again
to setup
  reset-ticks
  ask students
    [
      reset-client
      send-info-to-clients
    ]
end

;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;

to go
  every .1
  [ listen-clients ] ;; get commands and data from the clients
end

;; move on the the next round, calculate interest and update the variables on the clients
to next-round
  tick
  ask students [ update-student ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Code for interacting with the clients ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; standard hubnet procedure to collect data sent by clients
to listen-clients
  while [ hubnet-message-waiting? ]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ create-new-student display ]
    [
      ifelse hubnet-exit-message?
      [ remove-student display ]
      [ execute-command hubnet-message-tag ]
    ]
  ]
end

;; interpret each hubnet message as a command
to execute-command [command]
  if command = "fraction-to-invest" ;; every time a student adjusts their fraction to invest button update the turtle variable
  [
    ask students with [user-id = hubnet-message-source]
     [
        if (invested? = false)
        [ set fraction hubnet-message ]
     ]
  ]

  if command = "invest"
    [
      ask students with [user-id = hubnet-message-source] ;; when the student press the INVEST button move the money from My Money into investment
         [
           if (invested? = false) ;; students may only invest once per round
             [
               set investment my-money * fraction
               set my-money my-money - investment
               set return-investment 0
               hubnet-send user-id "investment" precision investment 2
               hubnet-send user-id "My Money" precision my-money 2
               hubnet-send user-id "investment-return" precision return-investment 2
               set invested? true
             ]
         ]
     ]

  if command = "buy-car"
  [
     ask students with [user-id = hubnet-message-source]
      [
        if (my-money >= car-cost)
        [
          set cars cars + 1
          set my-money my-money - car-cost
          hubnet-send user-id "cars" cars
          hubnet-send user-id "My Money" precision my-money 2
        ]
     ]
  ]
end

;; manage the turtles as clients enter and exit
to create-new-student
  let p one-of patches with [ count neighbors = 8 and not any? turtles-here and
                                     not any? neighbors4 with [ any? turtles-here ] ]
  ifelse p = nobody
  [ user-message "All of the spaces in the view are full, no more students can join." ]
  [ create-students 1
    [
      setup-student-vars p
      reset-client
      send-info-to-clients
    ]
  ]
end

to remove-student
  ask students with [user-id = hubnet-message-source] [ die ]
end

;; sets the turtle variables to appropriate initial values

;;;;;;;;;;;;;;;;;;;;;;;
;; Turtle Procedures ;;
;;;;;;;;;;;;;;;;;;;;;;;

;; setup the visual components of each turtle
to setup-student-vars [ p ]
  set user-id hubnet-message-source
  set shape "circle"
  set color one-of [ red blue pink cyan green ]
  set label-color black
  setxy [pxcor] of p + 0.5 [pycor] of p
  set label word user-id ", "
end

;; initialize the turtle variables
to reset-client
  set my-money 100
  set invested? false
  set return-investment 0
  set cars 0
  set label ( word  user-id ", " precision my-money 2 )
end

;; calculate investments and send the results to the clients
to update-student
  set return-investment ( investment * ( interest-rate + 1 ) )
  set my-money ( my-money + return-investment )
  set label ( word  user-id ", " precision my-money 2 ", " cars)
  set invested? false
  set investment 0
  send-info-to-clients
end

;; send the appropriate monitor information back to the client
to send-info-to-clients
  hubnet-send user-id "I am:" user-id
  hubnet-send user-id "Round" ticks
  hubnet-send user-id "My Money" precision my-money 2
  hubnet-send user-id "investment" precision investment 2
  hubnet-send user-id "investment-return" precision return-investment 2
  hubnet-send user-id "cars" cars
end
@#$#@#$#@
GRAPHICS-WINDOW
265
10
795
561
6
6
40.0
1
12
1
1
1
0
0
0
1
-6
6
-6
6
1
1
1
ticks

BUTTON
129
91
224
124
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
40
213
208
246
interest-rate
interest-rate
0
1
0.15
0.01
1
NIL
HORIZONTAL

BUTTON
76
131
180
164
NIL
next-round
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
41
171
208
204
car-cost
car-cost
0
100
50
1
1
NIL
HORIZONTAL

BUTTON
31
91
126
124
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

@#$#@#$#@
## WHAT IS IT?

This model is a simple activity designed to teach compound interest.  Students have a choice between investing their money with a teacher-controlled interest rate or using their money to purchase a car at a teacher defined price.

## HOW TO USE IT

Quickstart Instructions:

Teacher: Follow these directions to run the HubNet activity.
Optional: Zoom In (see Tools in the Menu Bar)
Teacher: Open up the HubNet Control Center (see Tools in the Menu Bar).  This will show you the IP Address of this computer.
Everyone: Open up a HubNet Client on your machine and input the IP Address of this computer, type your user name in the user name box, and press ENTER.

Teacher: Setup the activity by selecting a cost for cars and an interest rate using the CAR-COST and INTEREST-RATE sliders and press the GO button.
Everyone: Decide how to use your money by either investing, adjusting the FRACTION-TO-INVEST slider and pressing the INVEST button, or buying a car with the BUY-CAR button.

Teacher: Press the NEXT-ROUND button. Students who have invested will receive the calculated interest.

Teacher: To the activity again with the same group, press the SETUP button to return all students back to the initial conditions.
To run the activity with a different group of students press the RESET button in the Control Center and follow these instructions from the beginning.

Buttons:

RESET - Returns all current users to the initial state.  This should be used to restart the activity.
GO - runs the activity
NEXT-ROUND - Students who have invested will receive the calculated interest and all students will be given the option to invest again.

Sliders:

CAR-COST - Sets the cost of each car.
INTEREST-RATE - Sets the interest rate for all investors.

Monitors:

ROUND - Indicates the number of investment rounds have already been completed.

Client Information

After logging in, if GO is pressed in NetLogo the client interface will appear for the students and the student will be assigned a turtle and 100 dollars which will be displayed in the MY MONEY monitor.  The current round will be displayed in the ROUND monitor starting out with one. The money they have currently invested will appear in the INVESTMENT monitor, the return from their previous investment will be in the INVESTMENT-RETURN monitor, and the number of cars they own will be in the CARS monitor.  These will all be initialized to zero.

Each round the student manages their money by either selecting a fraction to invest with the FRACTION-TO-INVEST slider and pressing the INVEST button, or buying a car with the BUY-CAR button.

## THINGS TO NOTICE

There are at least two possible ways to run this model with a a group of students. One way is to tell students the number of rounds in the activity. Another way is not to tell them. If you try both ways, students may realize that it is important to figure out a strategy, and that knowing how the market behaves is helpful for choosing a strategy. Other activity options are to change the cost of the car and/or the interest rate in the middle of the activity. Note the difference between these two changes. Students know the price of a car before buying it. But students who invest money are taking a risk, because the teacher can wait for everyone to invest and only then change the interest rate. If students think this is unfair, they should discuss what the rules of the game should be and how these rules would reflect (or not) a real market.

## THINGS TO TRY

The teacher should try both telling students the number of rounds in the activity and ending after a randomly selected number of rounds without informing the students.  The students should think about how they should adjust their strategy for each set of conditions.  The teacher should also adjust the interest rate and the cost of a car throughout the game to simulate an indeterminate market.

## EXTENDING THE MODEL

Currently all of the conditions of the world are controlled by input from the teacher; these variables, cost of car, interest rate, and number of rounds, could be controlled automatically by the model. There is only one type of investment now, try adding low or high risk investments so the rate of return is not always the same.

## RELATED MODELS

Public Good

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
VIEW
276
10
796
530
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
-6
6
-6
6

BUTTON
96
176
171
209
invest
NIL
NIL
1
T
OBSERVER
NIL
NIL

MONITOR
25
10
141
59
I am:
NIL
3
1

MONITOR
72
68
187
117
My Money
NIL
2
1

SLIDER
43
131
215
164
fraction-to-invest
fraction-to-invest
0
1
0
0.01
1
NIL
HORIZONTAL

MONITOR
139
223
257
272
investment-return
NIL
2
1

MONITOR
17
223
119
272
investment
NIL
2
1

MONITOR
97
342
178
391
cars
NIL
0
1

MONITOR
183
10
241
59
Round
NIL
0
1

BUTTON
96
295
177
328
buy-car
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

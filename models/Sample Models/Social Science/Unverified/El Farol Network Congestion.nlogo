breed [ patrons patron ]

;; the 'b', 'a', and 'r' breeds are single turtles that spell 'bar'
breed [ bs b ]
breed [ as a ]
breed [ rs r ]

;;;;;;;;;;;;;;;
;; Variables ;;
;;;;;;;;;;;;;;;

globals [
  attendance          ;; number of patrons currently attending bar
  sum-attendance      ;; sum of attendance after N days
  average-attendance  ;; sum-attendance / days
  regulars            ;; patrons who attend at least every other day
  casuals             ;; patrons who do not attend every other day
]

patrons-own [
  attending?          ;; whether or not a patron attended during the previous day
  ;; how often the patron wishes to go to the bar, which is the
  ;; variable c in the Bell and Sethares paper
  attendance-frequency
  ;; the number of days remaining until the patron attends next,
  ;; which is the variable p in the Bell and Sethares paper
  phase
]

;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  setup-world
  setup-patrons
  reset-ticks
end

;; creates the colorful background images
to setup-world
  set sum-attendance 0
  set average-attendance 0
  ask patches
    [ set pcolor scale-color green ((random 500) + 5000) 0 9000 ]
  create-street
  create-bar
end

;; creates a street that runs vertically in the middle of the world
to create-street
  ask patches with [pxcor >= -1 and pxcor <= 1]
    [set pcolor black]
  ask patches with [pxcor = 0 and pycor mod 2 = 0]
    [set pcolor yellow]
end

;; draws a black bar with the word "bar" inside
to create-bar
  ask patches with [pycor < 2 and pycor > -2 and pxcor >= 9 and pxcor < 12]
    [set pcolor black]
  create-bs 1
    [set shape "b"
      setxy (max-pxcor - 2) 0]
  create-as 1
    [set shape "a"
      setxy (max-pxcor - 1) 0]
  create-rs 1
    [set shape "r"
      setxy max-pxcor 0]
end

;; creates patrons and places them in unique patches
to setup-patrons
  if population >= (max-pxcor - 1) * world-height
    [user-message (word "The population is too large for the size of the world."
                        "  Either decrease the population or increase the number of "
                        "patches in the world.")
      stop]
  create-patrons population
    [ set shape "person"
      set color sky
      set size 1
      set attending? false
      set attendance 0
      find-patch
      set attendance-frequency (1 + random 20)
      set phase (1 + random attendance-frequency) ]
end

;; locate unoccupied patches for patrons
to find-patch
  setxy ((random min-pxcor ) - 1)
        random-pycor
  while [any? other turtles-here]
      [find-patch]    ;; keeps running until each patron is alone on patch
end

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Runtime Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

to go
  phase-step
  tick
  check-attendance
  phase-update
end

;; counts down phase. if, following this count down, the phase has been completed,
;; the patron will attend now
to phase-step
  ask patrons
    [if (phase > 1) [set phase (phase - 1)]
      if phase <= 1  [set attending? true]]
end

;; checks whether or not the bar is overcrowded
to check-attendance
  calculate-attendance-by-type
  calculate-average-attendance
  attendees-skip-across-street
  display  ;; so we see them moving
  ;; Patrons who are in the bar always have perfect information about attendance
  ;; If the perfect-information? is On, then also patrons who are outside have
  ;; this information
  ifelse perfect-information?
    [ask patrons [update-attendance-frequency] ]
    [ask patrons with [attending?] [update-attendance-frequency] ]
end

;; finds the number of patrons who are attending and determines if they are casuals or regulars
to calculate-attendance-by-type
  set attendance count patrons with [attending?]
  set regulars count patrons with [attendance-frequency < 3]
  ask patrons with [attendance-frequency < 3]
    [set color red]
  set casuals (population - regulars)
  ask patrons with [attendance-frequency >= 3]
    [set color sky]
end

;; determines the cumulative ratio between total attendance and days since the onset of the
;; current run of the model
to calculate-average-attendance
  set sum-attendance (sum-attendance + attendance)
  set average-attendance (sum-attendance / ticks)
end

;; skips attending customers to the other side of the world across from the vertical axis
to attendees-skip-across-street
  ask patrons with [attending?]
    [setxy (0 - xcor) ycor]
end

;; patrons update the frequency of their individual attendance schedule (the phase or cycle).
;; if the bar was crowded, they'll come back less frequently than before, and vice versa
;; returning less frequently means increasing the phase, and vice versa
;; 'frequency-update' is the interface slider value for increasing/decreasing the phase
to update-attendance-frequency ;; patron procedure
  if attendance < (Equilibrium - dead-zone)
    [if (attendance-frequency > 1)
      [set attendance-frequency (attendance-frequency - frequency-update)]]
  if attendance > Equilibrium
    [set attendance-frequency (attendance-frequency + frequency-update)]
end

;; updates by setting phase to be attendance-frequency
to phase-update
  ;; now that the day is over, attending patrons return across the street
  ;; these patrons now begin counting down all over again from their current personal
  ;; value of 'attendance-frequency'
  attendees-skip-across-street
  ask patrons with [attending?]
    [set attending? false
      set phase attendance-frequency]
end
@#$#@#$#@
GRAPHICS-WINDOW
299
10
792
524
11
11
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
-11
11
-11
11
1
1
1
ticks
10

BUTTON
14
37
78
70
Setup
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
191
37
283
70
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

SLIDER
43
115
229
148
population
population
0
200
200
1
1
patrons
HORIZONTAL

SLIDER
43
149
229
182
equilibrium
equilibrium
0
100
40
1
1
patrons
HORIZONTAL

SLIDER
43
183
230
216
dead-zone
dead-zone
0
7
2
1
1
patrons
HORIZONTAL

PLOT
5
257
279
404
Attendance History
Time
People
0.0
100.0
0.0
100.0
true
true
"set-plot-y-range 0 population" ""
PENS
"now" 1.0 0 -6459832 true "" "plot attendance"
"cumulative" 1.0 0 -14835848 true "" "plot average-attendance"

SLIDER
43
217
230
250
frequency-update
frequency-update
0
50
1
1
1
NIL
HORIZONTAL

BUTTON
97
37
182
70
Go Once
go
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
5
405
279
543
Attendance Type
Type
Patrons
0.0
2.0
0.0
100.0
true
true
"set-plot-y-range 0 population" ""
PENS
"casuals" 1.0 1 -13791810 true "" "plot-pen-reset\nplotxy 0 casuals"
"regulars" 1.0 1 -2674135 true "" "plot-pen-reset\nplotxy 1 regulars"

SWITCH
43
81
229
114
perfect-information?
perfect-information?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

El Farol is a bar in Santa Fe.  The bar is popular, but becomes overcrowded when too many patrons attend.  Patrons are happy when less than a certain amount of patrons attend, say 60 for example, but they are unhappy when more than some other amount of patrons attend, say 70.  What will happen as time passes and people have pleasant or unpleasant experiences?

Is it always true that the more a bar is popular, the higher its attendance?  Does it makes sense to say a bar has its ups and downs, but on the whole it's stable?

This model problematizes a seemingly simple situation of social interaction to reveal that it is not that simple.  Working in this model, we encounter and appreciate the inherent coordination challenges that can arise in complex dynamic systems involving agents with intention and specified needs and criteria of satisfaction.  Can patrons of a bar somehow self organize to optimize overall satisfaction?

## HOW IT WORKS

El Farol is a fun place to be, and patrons keep returning.  But it can get crowded there.  If it is crowded, patrons may not come back as often as they had previously, but if it happens not to be crowded, the patrons will come again sooner.

Patrons of El Farol each have initial inclinations to visit the bar or not.  These personal inclinations are expressed in the frequencies of each patron's visits (the variable ATTENDANCE-FREQUENCY.  For example, a value of 9 means they go to the bar every 9 days).  ATTENDANCE-FREQUENCY is the variable c in the Bell and Sethares paper.

When the model starts running, patrons have different times until their next visit (stored in the variable PHASE. For example, a value of 5 means the patron will go to the bar in 5 days time).  PHASE is the variable p in the Bell and Sethares paper.  For each patron, the initial values of ATTENDANCE-FREQUENCY and PHASE are chosen randomly, within a designated range determined by the other settings of the model.  Each patron's PHASE value decreases as the model runs until it drops below 1, at which time the patron goes to the bar.  While in the bar, the patron determines how crowded the bar is and changes the value of ATTENDANCE-FREQUENCY accordingly.  If attendance at the bar exceeds the EQUILIBRIUM value, i.e. bar is overcrowded, the patron increases ATTENDANCE-FREQUENCY by the FREQUENCY-UPDATE value.  That means that in the future, this patron will wait longer before returning to this bar.  If attendance is below the critical value (EQUILIBRIUM - DEAD-ZONE), i.e. bar is not crowded, the patron decreases ATTENDANCE-FREQUENCY by the FREQUENCY-UPDATE value. ATTENDANCE-FREQUENCY remains unchanged if attendance falls within the DEAD-ZONE range.  The PHASE is then reset to the new value of ATTENDANCE-FREQUENCY.

## HOW TO USE IT

Sliders:  
POPULATION: the number of patrons that will be created in this experiment  
EQUILIBRIUM: the number of patrons beyond which the bar becomes overcrowded  
DEAD-ZONE: determines the range below the equilibrium at which the bar is perceived neither as crowded nor as not crowded  
FREQUENCY-UPDATE: value to update the ATTENDANCE-FREQUENCY by in response to a positive or negative experience at the bar

Buttons:  
SETUP - initiates variables towards a new run  
GO ONCE - runs the model for one time tick, so we get the behavior over a single 'day'  
GO - runs the model repeatedly, until it is stopped by pressing again

Switches:  
PERFECT-INFORMATION?: if turned on, agents will have access to attendance information whether they are attending or not and will adjust their preferences accordingly

Plots:  
ATTENDANCE HISTORY: shows how many patrons are currently in the bar and the cumulative ratio of total patrons to total days  
ATTENDANCE TYPE: shows the current totals of two types of patrons -- casuals and regulars (including those in and out of the bar)

After choosing the variables, click the SETUP button to setup the model. All patrons start on the left side of the world. This means that none of them are attending the bar at this moment. If they choose to attend, they will move to the right side of the world. Patrons are colored sky by default. This means that they are 'casual' patrons (less than every other day). If they attend more than every other day, they will turn red to show that they are now 'regular' patrons.

You can choose between GO ONCE and GO to run the model. Also, for initial runs, you may want to slow down the model, using the speed slider above the View, so as to see the patrons attending and leaving the bar.

## THINGS TO NOTICE

Try different settings and examine both plots.  Note that the ATTENDANCE HISTORY plot tends to converge on some value.  Note how the ATTENDANCE TYPE plot occasionally spikes up or down.  This means that there are a lot of casual patrons who are attending the bar often, and once in a while, they all happen to attend on the same day.  With PERFECT-INFORMATION? set to Off, the numbers of casuals and regulars tend to converge on some stable values. You will also notice that the average attendance seems to converge toward the (EQUILIBRIUM - DEAD-ZONE) number.

When PERFECT-INFORMATION? is turned On, everyone has access to the same information about attendance, and they all respond uniformly by updating their ATTENDANCE-FREQUENCY values.  So the group as a whole usually either all go or they do not go at all.  Thus, an increase in information seems to prevent some wiggle room that would make more patrons satisfied.

## THINGS TO TRY

How does changing the value on the FREQUENCY-UPDATE slider affect the behavior of the system? Try to guess, then run the model under different values for that slider and examine the results.

Play with the relation between values in the POPULATION and EQUILIBRIUM sliders.  Try to imagine how these settings would impact your own behavior, and see if the model matches your expectations.

## EXTENDING THE MODEL

Currently, all patrons have the same tolerance to crowds.  Assign random EQUILIBRIUM values to the attending turtles and evaluate how this modification affects the group behavior.

Invent and implement advanced patron strategies for optimizing their experience at the bar.  One idea is to have patrons remember historical data such as "it is always crowded every 7th day" or "every time it is crowded 4 days in a row, the next day is always not crowded."  What do you expect such an extension might do?  Can we, in principle, guarantee more satisfied patrons by making them more savvy?  Should we give this power of prediction to all patrons, or should we control who has these strategies (just as we controlled who has perfect information)?  Is it fair to favor some patrons over others?

Improve the visual features.  Perhaps the bar could be made to look more like an actual bar.

A final idea would be to create a HubNet version of the El Farol simulation.

## NETLOGO FEATURES

Look at the code for procedure attendees-skip-across-street.

      to attendees-skip-across-street
        ask patrons with [attending?]
          [ setxy (- xcor) ycor ]
      end

The code asks each patron that has a true value for the 'attending?' (true/false) variable to set their x coordinate.  Yet, this procedure is called both when the patron crosses the street to the right so as to enter the bar and again when the patron exits the bar and crosses back to the left.  How can it be that the same code both sends a patron to the right and to the left?

The answer lies in the meaning of xcor and how it relates to the minus sign (`-`) just before it. xcor reports a value representing the right/left position of a turtle.  To the right of central vertical line (the y-axis, where the street is in this model), the xcor values are positive, and to the left of the axis the values are negative.  When a patron is in the left section of the world, subtracting the xcor flips the xcor to a positive value.  For instance, if the patron is at (-5, 3), the patron will be sent to (+5, 3).  When this patron has completed attending, this same code sends the patron back from (+5, 3) to (-5, 3).

## RELATED MODELS

The Social Science models Party and Segregation each deal with situations in which individuals have specified preferences and act upon these preferences.

## CREDITS AND REFERENCES

Original implementation: Eric Cheng, for the Center for Connected Learning and Computer-Based Modeling.

This model is based on a paper by Ann Bell and William Sethares, "The El Farol Problem and the Internet: Congestion and Coordination Failure".
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

a
false
0
Line -1 false 54 249 150 55
Line -1 false 150 55 253 245
Line -1 false 105 148 201 148

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

arrow
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

b
false
0
Line -1 false 68 56 70 247
Line -1 false 69 58 114 41
Line -1 false 114 41 143 39
Line -1 false 144 39 166 49
Line -1 false 166 49 187 72
Line -1 false 186 74 190 94
Line -1 false 189 97 193 113
Line -1 false 190 118 185 132
Line -1 false 183 132 162 141
Line -1 false 160 142 137 150
Line -1 false 131 151 108 154
Line -1 false 104 155 73 159
Line -1 false 118 153 145 157
Line -1 false 145 157 170 168
Line -1 false 170 168 187 186
Line -1 false 186 188 195 211
Line -1 false 195 213 192 227
Line -1 false 190 229 178 240
Line -1 false 173 241 163 244
Line -1 false 156 245 137 248
Line -1 false 134 248 117 249
Line -1 false 116 249 107 249
Line -1 false 101 249 85 247
Line -1 false 85 247 73 245

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

r
false
0
Line -1 false 71 53 72 252
Line -1 false 73 56 98 42
Line -1 false 98 42 113 41
Line -1 false 113 41 131 44
Line -1 false 131 45 151 53
Line -1 false 151 53 166 68
Line -1 false 166 69 173 91
Line -1 false 173 92 176 116
Line -1 false 174 118 168 144
Line -1 false 163 145 141 155
Line -1 false 138 156 123 161
Line -1 false 119 161 110 162
Line -1 false 107 162 88 165
Line -1 false 85 166 79 166
Line -1 false 115 162 196 256

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

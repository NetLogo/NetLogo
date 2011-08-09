;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variable and Breed declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

globals
[
  day                ;; number of days so far

  colors             ;; list that holds the colors used for students' turtles
  color-names        ;; list that holds the names of the colors used for
                     ;; students' turtles
  num-colors         ;; number of colors in the color list
  used-colors        ;; list that holds the shape-color pairs that are
                     ;; already being used

  n/a                ;; unset variable indicator

  ;; quick start instructions variables
  quick-start        ;; current quickstart instruction displayed in the
                     ;; quickstart monitor
  qs-item            ;; index of the current quickstart instruction
  qs-items           ;; list of quickstart instructions

  grass-max          ;; grass capacity
  food-max           ;; grass collection capacity
  bite-size          ;; amount of grass collected at each move
]

patches-own
[
  grass-stored       ;; amount of grass currently stored
]

breed [ goats goat ]  ;; creation controlled by farmers
breed [ farmers farmer ] ;; created and controlled by clients


goats-own
[
  food-stored        ;; amount of grass collected from grazing
  owner#             ;; the user-id of the farmer who owns the goat
]

farmers-own
[
  user-id            ;; unique user-id, input by the client when they log in,
                     ;; to identify each student turtle
  num-goats-to-buy   ;; desired quantity of goats to purchase
  revenue-lst        ;; list of each days' revenue collection
  total-assets       ;; total of past revenue, minus expenses
  current-revenue    ;; the revenue collected at the end of the last day
]


;;;;;;;;;;;;;;;;;;;;;
;; Setup Functions ;;
;;;;;;;;;;;;;;;;;;;;;

to startup
  setup-quick-start
  hubnet-reset
  setup
end

;; initializes the display
;; but does not clear already created farmers
to setup
  setup-globals
  setup-patches
  clear-output
  clear-all-plots
  ask farmers
    [ reset-farmers-vars ]
  hubnet-broadcast "Goat Seller Says:"
    (word "Everyone starts with " init-num-goats/farmer " goats.")
  hubnet-broadcast "num-goats-to-buy" 1
  broadcast-system-info
end

;; initialize global variables
to setup-globals
  reset-ticks
  set day 0

  set grass-max 50
  set food-max 50
  ;; why this particular calculation?
  set bite-size (round (100 / (grazing-period - 1)))

  set colors      [ white   gray   orange   brown    yellow    turquoise
                    cyan    sky    blue     violet   magenta   pink ]
  set color-names ["white" "gray" "orange" "brown"  "yellow"  "turquoise"
                   "cyan"  "sky"  "blue"   "violet" "magenta" "pink"]
  set used-colors []
  set num-colors length colors
  set n/a "n/a"
end

;; initialize grass supply for each patch
to setup-patches
  ask patches
  [
    ;; set amount of food at each patch
    set grass-stored grass-max
    color-patches
  ]
end

;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;

to go
  ;; get command and data from client
  listen-to-clients

  every .1
  [
    every .5
      [ broadcast-system-info ]

    if not any? farmers
    [
      user-message word "There are no farmers.  GO is stopping.  "
          "Press GO again when people have logged in."
      stop
    ]

    tick

    ;; when not milking time
    ifelse (ticks mod grazing-period) != 0
    [
      ask goats
        [ graze ]
    ]
    [
      set day day + 1
      ask farmers
        [ milk-goats ]
      go-to-market ;; to buy goats
      plot-graph
    ]

    reset-patches
  ]
end

;; goat move along the common looking for best patch of grass
to graze  ;; goat procedure
  if (food-stored != food-max) or (other goats-here = nobody)
  [
    let new-food-amt (food-stored + get-amt-eaten)
    ifelse (new-food-amt < food-max)
      [ set food-stored new-food-amt ]
      [ set food-stored food-max ]
  ]
  rt (random-float 90)
  lt (random-float 90)
  fd 1
end

;; returns amount of grass eaten at patch and
;; sets the patch grass amount accordingly
to-report get-amt-eaten  ;; goat procedure
  let reduced-amt (grass-stored - bite-size)
  ifelse (reduced-amt < 0)
  [
    set grass-stored 0
    report grass-stored
  ]
  [
    set grass-stored reduced-amt
    report bite-size
  ]
end

;; collect milk and sells them at market ($1 = 1 gallon)
to milk-goats  ;; farmer procedure
  set current-revenue
    (round-to-place (sum [food-stored] of my-goats) 10)
  ask my-goats
    [ set food-stored 0 ]
  set revenue-lst (fput current-revenue revenue-lst)
  set total-assets total-assets + current-revenue
  send-personal-info
end

;; the goat market setup
to go-to-market
  ask farmers
  [
    if num-goats-to-buy > 0
      [ buy-goats num-goats-to-buy ]
    if num-goats-to-buy < 0
      [ lose-goats (- num-goats-to-buy) ]
    if num-goats-to-buy = 0
      [ hubnet-send user-id "Goat Seller Says:" "You did not buy any goats." ]
    send-personal-info
  ]
end

;; farmers buy goats at market
to buy-goats [ num-goats-desired ]  ;; farmer procedure
  let got-number-desired? true
  let num-goats-afford (int (total-assets / cost/goat))
  let num-goats-purchase num-goats-desired
  if (num-goats-afford < num-goats-purchase)
  [
    set num-goats-purchase num-goats-afford
    set got-number-desired? false
  ]
  let cost-of-purchase num-goats-purchase * cost/goat
  set total-assets (total-assets - cost-of-purchase)
  hubnet-send user-id "Goat Seller Says:"
    (seller-says got-number-desired? num-goats-desired num-goats-purchase)

  ;; create the goats purchased by the farmer
  hatch num-goats-purchase
    [ setup-goats user-id ]
end

;; farmers eliminate some of their goats (with no gain in assets)
to lose-goats [ num-to-lose ]  ;; farmer procedure
  if ((count my-goats) < num-to-lose)
    [ set num-to-lose (count my-goats) ]
  hubnet-send user-id "Goat Seller Says:"
    (word "You lost " num-to-lose " goats.")

  ;; eliminate the goats ditched by the farmer
  ask (n-of num-to-lose my-goats)
    [ die ]
end

;; reports the appropriate information on the transaction of purchasing goats
to-report seller-says [ success? desired purchased ]
  let seller-message ""
  let cost purchased * cost/goat
  ifelse success?
  [
    ifelse (purchased > 1)
      [ set seller-message (word "Here are your " purchased " goats.  ") ]
      [ set seller-message "Here is your goat.  " ]
    set seller-message (word seller-message "You have spent $" cost ".")
  ]
  [
    set seller-message (word "You do not have enough to buy " desired ".  "
      "You can afford " purchased " for $" cost ".")
  ]
  report seller-message
end

;; initializes goat variables
to setup-goats [ farmer# ]  ;; turtle procedure
  set breed goats
  setxy random-xcor random-ycor
  set shape "goat"
  set food-stored 0
  set owner# farmer#
  st
end

;; updates patches' color and increase grass supply with growth rate
to reset-patches
  ask patches with [grass-stored < grass-max]
  [
    let new-grass-amt (grass-stored + grass-growth-rate)
    ifelse (new-grass-amt > grass-max)
      [ set grass-stored grass-max ]
      [ set grass-stored new-grass-amt ]
    color-patches
  ]
end

;; colors patches according to amount of grass on the patch
to color-patches  ;; patch procedure
  set pcolor (scale-color green grass-stored -5 (2 * grass-max))
end


;;;;;;;;;;;;;;;;;;;;;;;;
;; Plotting Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;

;; plots the graph of the system
to plot-graph
  plot-value "Milk Supply" milk-supply
  plot-value "Grass Supply" grass-supply
  plot-value "Goat Population" count goats
  plot-value "Average Revenue" avg-revenue
end

;; plot value on the plot called name-of-plot
to plot-value [ name-of-plot value ]
  set-current-plot name-of-plot
  plot value
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Calculation Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

to-report milk-supply
  ;; we can just compute this from revenue, since the price of milk is
  ;; fixed at $1/1 gallon.
  report sum [ current-revenue ] of farmers
end

to-report grass-supply
  report sum [ grass-stored ] of patches
end

to-report avg-revenue
  report mean [ current-revenue ] of farmers
end

;; returns agentset that of goats of a particular farmer
to-report my-goats  ;; farmer procedure
  report goats with [ owner# = [user-id] of myself ]
end

;; rounds given number to certain decimal-place
to-report round-to-place [ num decimal-place ]
  report (round (num * decimal-place)) / decimal-place
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Quick Start functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; instructions to quickly setup the model, and clients to run this activity
to setup-quick-start
  set qs-item 0
  set qs-items
  [
    "Teacher: Follow these directions to run the HubNet activity."
      "Optional: Zoom In (see Tools in the Menu Bar)"
      "Optional: Change any of the settings...."
      "If you do change the settings, press the SETUP button."
      "Press the LOGIN button to allow people to login."
      "Everyone: Open up a HubNet Client on your machine and..."
        "type your user name, select this activity and press ENTER."

    "Teacher: Once everyone has logged in,..."
        "turn off the LOGIN button by pressing it again."
      "Have the students acquaint themselves with the information..."
        "available to them in the monitors, buttons, and sliders."
      "Then press the GO button to start the simulation."
      "Please note that you may adjust the length of time..."
        "GRAZING-PERIOD, that goats are allowed to graze each day."
      "For a quicker demonstration, reduce the..."
        "GRASS-GROWTH-RATE slider."
      "To curb buying incentives of the students, increase..."
        "the COST/GOAT slider."
      "Any of the above mentioned parameters - ..."
        "GRAZING-PERIOD, GRASS-GROWTH-RATE, and COST/GOAT -..."
        "may be altered without stopping the simulation."

    "Teacher: To run the activity again with the same group,..."
        "stop the model by pressing the GO button, if it is on."
        "Change any of the settings that you would like."
      "Press the SETUP button."

    "Teacher: Restart the simulation by pressing the GO button again."

    "Teacher: To start the simulation over with a new group,..."
        "stop the model by pressing the GO button if it is on..."
        "press the RESET button in the Control Center"
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

;; determines which client sent a command, and what the command was
to listen-to-clients
  while [ hubnet-message-waiting? ]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [
      create-new-farmer hubnet-message-source
    ]
    [
      ifelse hubnet-exit-message?
        [ remove-farmer hubnet-message-source ]
        [ execute-command hubnet-message-tag ]
    ]
  ]
end

;; NetLogo knows what each student turtle is supposed to be
;; doing based on the tag sent by the node:
;; num-goats-to-buy - determine quantity of student's desired purchase
to execute-command [command]
  if command = "num-goats-to-buy"
  [
    ask farmers with [user-id = hubnet-message-source]
      [ set num-goats-to-buy hubnet-message ]
    stop
  ]
end

to create-new-farmer [ id ]
  create-farmers 1
  [
    set user-id id
    setup-farm
    set-unique-color
    reset-farmers-vars
    hubnet-send id "num-goats-to-buy" num-goats-to-buy
    send-system-info
  ]
end

;; situates the farmer in particular location
to setup-farm  ;; farmer procedure
  setxy ((random-float (world-width - 2)) + min-pxcor + 1)
        ((random-float (world-height - 2)) + min-pycor + 1)
  ht
end

;; pick a color for the turtle
to set-unique-color  ;; turtle procedure
  let code random num-colors
  while [member? code used-colors and count farmers < num-colors]
    [ set code random num-colors ]
  set used-colors (lput code used-colors)
  set color item code colors
end

;; set farmer variables to initial values
to reset-farmers-vars  ;; farmer procedure
  ;; reset the farmer variable to initial values
  set revenue-lst []
  set num-goats-to-buy 1
  set total-assets cost/goat
  set current-revenue 0

  ;; get rid of existing goats
  ask my-goats
    [ die ]

  ;; create new goats for the farmer
  hatch init-num-goats/farmer
    [ setup-goats user-id ]

  send-personal-info
end

;; sends the appropriate monitor information back to the client
to send-personal-info  ;; farmer procedure
  hubnet-send user-id "My Goat Color" (color->string color)
  hubnet-send user-id "Current Revenue" current-revenue
  hubnet-send user-id "Total Assets" total-assets
  hubnet-send user-id "My Goat Population" count my-goats
end

;; returns string version of color name
to-report color->string [ color-value ]
  report item (position color-value colors) color-names
end

;; sends the appropriate monitor information back to one client
to send-system-info  ;; farmer procedure
  hubnet-send user-id "Milk Amt" milk-supply
  hubnet-send user-id "Grass Amt" grass-supply
  hubnet-send user-id "Cost per Goat" cost/goat
  hubnet-send user-id "Day" day
end

;; broadcasts the appropriate monitor information back to all clients
to broadcast-system-info
  hubnet-broadcast "Milk Amt" milk-supply
  hubnet-broadcast "Grass Amt" (int grass-supply)
  hubnet-broadcast "Cost per Goat" cost/goat
  hubnet-broadcast "Day" day
end

;; delete farmers once client has exited
to remove-farmer [ id ]
  let old-color 0
  ask farmers with [user-id = id]
  [
    set old-color color
    ask my-goats
      [ die ]
    die
  ]
  if not any? farmers with [color = old-color]
    [ set used-colors remove (position old-color colors) used-colors ]
end
@#$#@#$#@
GRAPHICS-WINDOW
462
96
1018
673
10
10
26.0
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
1
1
1
ticks

BUTTON
270
11
363
44
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
6
48
224
81
init-num-goats/farmer
init-num-goats/farmer
0
10
5
1
1
goats
HORIZONTAL

SLIDER
6
82
224
115
grass-growth-rate
grass-growth-rate
0
2
0.5
0.1
1
oz/cycle
HORIZONTAL

SLIDER
225
82
444
115
cost/goat
cost/goat
1
2000
893
1
1
$
HORIZONTAL

MONITOR
239
345
337
390
Avg-Revenue
avg-revenue
1
1
11

PLOT
233
174
448
342
Average Revenue
Day
Revenue
0.0
20.0
0.0
1000.0
true
false
"" ""
PENS
"revenue" 1.0 0 -16777216 true "" ""

PLOT
233
398
448
556
Goat Population
Day
Goats
0.0
20.0
0.0
100.0
true
false
"" ""
PENS
"num-goats" 1.0 0 -16777216 true "" ""

MONITOR
341
345
441
390
Goat Population
count goats
0
1
11

MONITOR
42
345
118
390
Milk Supply
milk-supply
0
1
11

PLOT
15
174
229
342
Milk Supply
Day
Milk (Gallons)
0.0
20.0
0.0
100.0
true
false
"" ""
PENS
"milk-amt" 1.0 0 -16777216 true "" ""

PLOT
15
398
229
556
Grass Supply
Day
Grass (lbs)
0.0
20.0
0.0
100.0
true
false
"" ""
PENS
"grass-amt" 1.0 0 -16777216 true "" ""

SLIDER
225
48
443
81
grazing-period
grazing-period
2
50
34
1
1
NIL
HORIZONTAL

MONITOR
121
345
207
390
Grass Supply
grass-supply
0
1
11

BUTTON
75
11
164
44
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
16
121
79
166
Day
Day
3
1
11

BUTTON
462
61
580
94
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
798
61
882
94
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
720
61
798
94
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
462
10
882
55
Quick Start Instructions- More in Info Window
quick-start
0
1
11

BUTTON
165
11
269
44
login
listen-to-clients
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

This model simulates the utilization of a common resource by multiple users.  In this example, the common resource is represented by the common grazing area, used by goat farmers to feed their goats.  Depending on the actions of the participants, the outcome may demonstrate a phenomenon called the "tragedy of the commons", where a common good or resource is over-utilized.

This is a counterexample to the efficient (as defined in "Pareto efficiency") market theorem.  (The efficient market theorem states that agents, looking out for their own best interest [i.e., everyone trying to increase their own wealth], leads to the most efficient social outcome [i.e., the highest quantity of milk].)

## HOW IT WORKS

The students act as the farmers.  They own INIT-NUM-GOATS/FARMER when they join the simulation.

The goats move around the screen for a time span of GRAZING-PERIOD to graze and feed themselves.  The amount of grass they eat is equivalent to how much milk they can produce (and ultimately, the amount of profit they produce for the farmer).

After the GRAZING-PERIOD, the farmers may choose to buy more goats to increase their own wealth.

Initially, the abundance of GRASS-SUPPLY of the patches can sustain the goats and their grazing and leads to increasing milk-supply and increasing revenue.  But suppose, then, that due to the farmers' own incentive to increase their own wealth and each farmer's indifference to the other farmers' decision to purchase goats, each farmer continues to buy more goats.  With the increase of the GOAT-POPULATION, GRASS-SUPPLY gradually decreases.  Ultimately, the common grazing area does not contain enough grass to sustain the overcrowding of goats, and the milk-supply as well as the farmers' revenues decline.  This is the "tragedy of the commons".

## HOW TO USE IT

Quickstart Instructions:

Teacher: Follow these directions to run the HubNet activity.
Optional: Zoom In (see Tools in the Menu Bar)
Optional: Change any of the settings.  If you do change the settings, press the SETUP button.
Press the LOGIN button to allow people to login.
Everyone: Open up a HubNet Client on your machine and enter your name, select this activity, and press ENTER.

Teacher: Once everyone has logged in, turn off the LOGIN button by pressing it again.
Have the students acquaint themselves with the various information available to them in the monitors, buttons, and sliders.
Then press the GO button to start the simulation.
Please note that you may adjust the length of time, GRAZING-PERIOD, that goats allowed to graze each day.
For a quicker demonstration, reduce the GRASS-GROWTH-RATE slider.
To curb buying incentives of the students, increase the COST/GOAT slider.
Any of the above mentioned parameters - GRAZING-PERIOD, GRASS-GROWTH-RATE, and COST/GOAT - may be altered without stopping the simulation.

Teacher: To run the activity again with the same group, stop the model by pressing the GO button, if it is on.
Change any of the settings that you would like.
Press the SETUP button.
Restart the simulation by pressing the GO button.

Teacher: To start the simulation over with a new group, stop the model by pressing the GO button if it is on, press the RESET button in the Control Center to kick out all the clients  and follow these instructions again from the beginning.

Buttons:

SETUP - returns all farmers, goats, and patches to initial condition and clears the plot.
LOGIN - allows users to log into the activity without running the model or collecting data
GO - runs the simulation

Sliders:

INIT-NUM-GOATS/FARMER - initial number of goats per farmer at the beginning of the simulation
GRASS-GROWTH-RATE - amount of grass growth for each tick of the clock
COST/GOAT - cost for a goat
GRAZING-PERIOD - the time frame in which goats are allowed to graze each day

Monitors:

GRASS SUPPLY - amount of grass available to eat
MILK SUPPLY - amount of milk produced each day
AVG-PROFIT/DAY - amount of revenue collected from milk sale per day
GOAT POPULATION - number of goats grazing in the common area

Plots:

GRASS SUPPLY - amount of grass available to graze upon over time (in days)
MILK SUPPLY - amount of milk produced over time (in days)
AVERAGE REVENUE - average revenue of all farmers over time (in days)
GOAT POPULATION - number of goats on grazing field over time (in days)

Client Information

After logging in, the client interface will appear for the students, and if GO is pressed in NetLogo, they will be assigned a farmer which will be described by the color in the MY GOAT COLOR monitor.  The MY GOAT POPULATION monitor will display the number of goats each student owns.  Their revenue for the last day will be displayed in the CURRENT REVENUE monitor and their total assets in the TOTAL ASSETS monitor.

The global Cost per Goat can be viewed in the COST PER GOAT monitor.  The performance of the society, measured by the amount of grass available for food and the amount of milk produced can be measured from the GRASS AMT and MILK AMT monitors, respectively.  The current day, a day being defined as one grazing period followed by a milking session, is displayed in the DAY monitor.

The student manages his/her goat population.  During the course of each grazing period, the student must decide what action to take for the day, whether to buy or to discard some goats.  To buy or to discard goats, the student must adjust NUM-GOATS-TO-BUY slider to his/her desired quantity.  At the end of the day, the specified transaction will be executed automatically.  The transaction of the purchase will be described in the GOAT SELLER SAYS: monitor, which will inform the client of the action taken and how many goats were purchased, if any.

The progress of the community's welfare as measured by the grass available for grazing, average revenue of farmers, and milk supply is plotted in the GRASS SUPPLY, AVERAGE REVENUE, and MILK SUPPLY plots (if present), which are each identical to the plot of the same name in NetLogo.

## THINGS TO NOTICE

Note that if the students do not confer with each other, the tragedy of the commons arises.  The general patterns of the plots for average revenue and milk supply are similar -- the curves eventually peak and are followed by a decrease.  After the initial run, ask the students which parameters helped them to decide to purchase or not purchase goats.  Ask the students to switch objectives for the simulation; instead of maximizing total-assets, ask them to maximize milk-supply.  Also, ask the students to confer and act as cooperative unit.

## THINGS TO TRY

Use the model with the entire class to serve as an introduction to the topic.  Upon running the initial set of simulations, ask the students to switch objective - instead of maximizing their own total-assets, ask them to maximize milk-supply for the entire community.  Ask the students to confer and act as a cooperative unit.  Observe and discuss the changes in AVG-REV/FARMER and MILK-SUPPLY from the initial simulation and the later simulation with altered objectives.  Continue to discuss how the "Tragedy of the Commons" can be avoided and why or why not a society would want to avoid this phenomenon.  Then, have students use the NetLogo model individually, in a computer lab, to explore the effects of the various parameters.  Discuss what they find, observe, and can conclude from this model.

## EXTENDING THE MODEL

Is there other information about the state of the simulation that the farmers might want access to? Do you think this would change the outcome of the simulation? From what other phenomena (e.g. prisoner's dilemma or free-rider problem) would the student's behavior to not sell the goats arise (if it arises)?

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

goat
false
0
Polygon -7500403 true true 97 116 125 128 166 130 209 119 243 136 244 171 231 188 215 193 215 243 205 244 203 192 169 182 131 185 130 245 116 245 115 184 87 138
Polygon -7500403 true true 243 146 260 152 259 180 253 155
Polygon -7500403 true true 97 113 82 93 61 93 34 134 43 145 59 137 79 133 86 138
Polygon -7500403 true true 58 136 65 150 55 161 58 146
Rectangle -7500403 true true 88 123 103 138
Rectangle -7500403 true true 88 120 100 133
Rectangle -7500403 true true 88 119 100 131
Rectangle -7500403 true true 86 119 95 123
Rectangle -7500403 true true 88 115 98 124
Rectangle -7500403 true true 87 134 90 140
Polygon -6459832 true false 69 91 76 65 114 57 120 68 109 64 83 71 80 93
Polygon -7500403 true true 203 193 201 242 204 243
Polygon -7500403 true true 205 217 201 242 205 242
Polygon -7500403 true true 105 168 81 186 90 223 101 222 94 188 118 183
Polygon -7500403 true true 184 186 173 197 186 223 196 213 183 196
Polygon -6459832 true false 68 92 82 92

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
VIEW
460
10
880
430
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

MONITOR
251
30
356
79
Current Revenue
NIL
3
1

MONITOR
126
30
244
79
My Goat Population
NIL
3
1

MONITOR
99
218
170
267
Grass Amt
NIL
0
1

MONITOR
6
218
96
267
Cost per Goat
NIL
3
1

MONITOR
363
30
454
79
Total Assets
NIL
3
1

MONITOR
5
30
121
79
My Goat Color
NIL
3
1

MONITOR
173
218
230
267
Milk Amt
NIL
3
1

TEXTBOX
8
199
125
217
System Variables:
11
0.0
0

TEXTBOX
7
10
124
28
Personal Variables:
11
0.0
0

SLIDER
8
95
157
128
num-goats-to-buy
num-goats-to-buy
-5
5
0
1
1
NIL
HORIZONTAL

MONITOR
383
217
440
266
Day
NIL
3
1

MONITOR
8
145
454
194
Goat Seller Says:
NIL
3
1

TEXTBOX
163
98
441
130
Selecting a negative number here will eliminate some of your goats.
11
0.0
0

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

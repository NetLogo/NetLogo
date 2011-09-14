;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variable and Breed declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

globals [
 day                  ;; number of days so far

 ;; Color globals
 colors               ;; list that holds the colors for the student's restaurant
 color-names          ;; list that holds the names of the colors used for the student's restaurants
 num-colors           ;; number of different colors in the color list
 used-colors          ;; list that holds the colors that are already being used

 ;; quick start instructions variables
 quick-start          ;; current quickstart instruction displayed in the quickstart monitor
 qs-item              ;; index of the current quickstart instruction
 qs-items             ;; list of quickstart instructions

 n/a                  ;; unset variable indicator
]

patches-own [ ]

breed [ restaurants restaurant ]         ;; controlled by the clients
breed [ customers customer ]             ;; created by the server

customers-own [
 ;; Customer Preferences
 customer-cuisine        ;; the preferred cuisine type
 customer-taste          ;; the preferred quality of the cuisine
 customer-money          ;; the maximum amount of money the customer can spend on a meal

 ;; Restaurant Appeal
 appeal               ;; how appealing the restaurant is to the customer
 persuaded?           ;; has the customer been persuaded to go to a restaurant
 my-restaurant           ;; by which restaurant has the customer been persuaded

 ;; Eating Patterns
 energy               ;; amount of energy the customer has
]

restaurants-own [
 ;; Owner Information
 user-id              ;; unique user-id, input by the client when they log in, to identify each student's restaurant
 auto?                ;; is the owner automated
 bankrupt?            ;; is the owner bankrupt
 account-balance      ;; total amount of money the owner has

 ;; Ranking Statistics
 received-rank?       ;; if given a rank, ranked? is true, otherwise false
 rank                 ;; rank number according to account balance

 ;; Restaurant Information
 restaurant-color        ;; color of the restaurant

 ;; Restaurant Taste Profile
 restaurant-cuisine      ;; the type of cuisine the restaurant serves
 restaurant-service      ;; the quality of the service
 restaurant-quality      ;; the quality of the food
 restaurant-price        ;; the price of a meal at the restaurant

 ;; Restaurant Statistics
 days-revenue         ;; amount of revenue generated so far today
 days-cost            ;; amount of costs accumulated so far today
 days-profit          ;; profit made so far today
 num-customers        ;; number of customers today
 profit-customer  ;; avg profit made per customer
]

;;;;;;;;;;;;;;;;;;;;;
;; Setup Functions ;;
;;;;;;;;;;;;;;;;;;;;;

to startup
  hubnet-reset
  setup
end

;; initializes the display and
;; set parameters for the system
to setup
  cp ct
  clear-output
  setup-quick-start
  reset
end

;; initializes the display (but does not clear already created restaurants)
to reset
  setup-globals
  setup-consumers
  clear-all-plots
  ask restaurants
  [ reset-owner-variables ]
  broadcast-system-info
end

;; initializes the global variables
to setup-globals
  reset-ticks
  set day 0

  set-default-shape customers "person"

  ;; Set the available colors  and their names
  set colors      [ lime   orange   brown   yellow  turquoise  cyan   sky   blue
                   violet   magenta   pink  red  green  gray  12 62 102 38 ]
  set color-names ["lime" "orange" "brown" "yellow" "turquoise" "cyan" "sky" "blue"
                   "violet" "magenta" "pink" "red" "green" "gray" "maroon" "hunter green" "navy" "sand"]
  set used-colors []
  set num-colors length colors
  set n/a "n/a"
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Customer Setup Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; initializes and creates the customers
to setup-consumers

  ask customers
  [ die ]

  create-customers Num-Consumer
    [ set energy Consumer-Energy
    set persuaded? false
    set my-restaurant -1

    setxy random-xcor random-ycor

    set appeal 0
    let chance random 3

    ;; initialize the customer's preferences
    set customer-money (20 + random 81)
    set customer-taste (customer-money - 20)
    ifelse (chance = 0)
    [ set color red
      set customer-cuisine "American" ]
    [ ifelse (chance = 1)
      [ set color yellow
        set customer-cuisine "Asian" ]
      [ set color cyan
        set customer-cuisine "European" ] ] ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Automated Restaurants Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; creates automated owners
to create-automated-restaurants [ number ]
  create-restaurants number
  [ set user-id who
    reset-owner-variables
    set auto? true
    set color 32
    set size 2
    setup-automated-restaurant
    setup-location ]
end

;; initializes the automated owner's variables
to setup-automated-restaurant

  let chance (random 3)

  ;; initializes the automated owner's settings
  set restaurant-service 5
  set restaurant-quality (25 + random 50)
  set restaurant-price (restaurant-quality + 10)
  ifelse (chance = 0)
  [ set restaurant-cuisine "American"
    set shape "restaurant american" ]
  [ ifelse (chance = 1)
    [ set restaurant-cuisine "Asian"
      set shape "restaurant asian" ]
    [ set restaurant-cuisine "European"
      set shape "restaurant european" ] ]
end

;;;;;;;;;;;;;;;;;;
;; Setup Prompt ;;
;;;;;;;;;;;;;;;;;;

;; give the user some information about what the setup button does so they can
;; know whether they want to proceed before actually doing the setup
to setup-prompt
 if user-yes-or-no? (word "The SETUP button should only be used when starting "
             "over with a new group (such as a new set of students) since "
             "all data is lost.  Use the RE-RUN button for continuing with "
             "an existing group."
             "\n\nDo you really want to setup the model?")
 [ user-message (word "Before closing this dialog, please do the following:"
                "\n  -Have everyone that is currently logged in, log off and "
                "then kick all remaining clients with the HubNet Console.")
   setup ]
end

;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;

to go
  listen-to-clients
  every .5
  [ broadcast-system-info
    ask restaurants with [ auto? = false ]
    [ send-personal-info ] ]

  if not any? restaurants
  [ user-message "There are no restaurant owners. Log people in or create restaurants."
    stop ]

  ask restaurants with [ bankrupt? = false ] ;; Let the Restaurants work
  [ serve-customers
    attract-customers ]

  ask customers ;; Move the customers
  [ move-customers ]

  if (ticks mod day-length) = 0 ;; Is it time to end the day?
  [ set day day + 1
   plot-disgruntled-customers
   plot-restaurant-statistics
   ask restaurants with [ bankrupt? = false ]
   [ end-day ]
   if show-rank? and any? restaurants with [auto? = false]
    [ rank-restaurants ] ]
  tick
end

to serve-customers ;; turtle procedure
 let restaurant# user-id
 let new-customers 0

 ;; customers update the information of the restaurant where they have decided to dine
 ask customers with [ (persuaded? = true) and (my-restaurant = restaurant#) ] in-radius 1
 [ set new-customers new-customers + 1
   set persuaded? false
   set my-restaurant -1
   set appeal 0
   set energy Consumer-Energy ]

  set num-customers (num-customers + new-customers)
  set days-revenue (days-revenue + (new-customers * restaurant-price))
  set days-cost round (days-cost + (new-customers * Service-Cost * restaurant-service) + (new-customers * Quality-Cost * restaurant-quality))
  set days-profit round (days-revenue - days-cost)
end

to attract-customers ;; turtle procedure
  let restaurant# user-id
  let r-x xcor
  let r-y ycor
  let r-cuisine restaurant-cuisine
  let adj-price (restaurant-price - 0.15 * restaurant-service)
  let adj-quality (restaurant-quality + 0.15 * restaurant-service)
  let util-price false
  let util-quality false
  let restaurant-appeal false

  ;; Try and persuade customers that are within range
  ask customers with [ (energy < Consumer-Threshold) and (customer-cuisine = r-cuisine) ] in-radius 7
  [
    set util-price (customer-money - adj-price)
    set util-quality (adj-quality - customer-taste)
    if (util-price >= 0) and (util-quality >= 0)
    [
       set restaurant-appeal (util-price + util-quality) * 5
       if (restaurant-appeal > appeal)
       [ set appeal restaurant-appeal
         set persuaded? true
         set my-restaurant restaurant#
         facexy r-x r-y ] ] ]
end

;; makes the customers move
to move-customers ;; customer procedure
 if persuaded? = false
 [ rt random-float 45 - random-float 45 ]
 set energy energy - 1
 fd 1
end

;; makes the owner calculate end-of-day figures and initializes
;; the personal variables for the next day
to end-day ;; turtle procedure

  if (auto? = false)
  [ hubnet-send user-id "Number of Customers" num-customers
    hubnet-send user-id "Day's Profit" days-profit
    hubnet-send user-id "Day's Revenue" days-revenue
    hubnet-send user-id "Day's Cost" days-cost ]

  set account-balance round (account-balance + days-profit)
  set days-cost Rent-Cost
  set days-revenue 0
  set days-profit (days-revenue - days-cost)
  set num-customers 0

  if (Bankruptcy?) ;; If the owner is bankrupt shut his restaurant down
  [ if (account-balance < 0)
  [ set bankrupt? true ] ]
end

;;;;;;;;;;;;;;;;;;;;;;;
;; Ranking Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;

;; ranks owners by their account balance. if there are three players and two of them are tied with the
;; lower account balance, then they will both be ranked as 3rd place.
to rank-restaurants
  let num-ranks (length (remove-duplicates ([account-balance] of restaurants)))
  let rank# count restaurants

  repeat num-ranks
  [ let min-rev min [account-balance] of restaurants with [not received-rank?]
    let rankee restaurants with [account-balance = min-rev]
    let num-tied count rankee
    ask rankee
    [ set rank rank#
      set received-rank? true ]
    set rank# rank# - num-tied ]

  ask restaurants
  [ set received-rank? false ]
end

;;;;;;;;;;;;;;;;;;;;;;;;
;; Plotting Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;

;; plot the number of disgruntled customers
to plot-disgruntled-customers
  set-current-plot "Disgruntled Customers"
  plot disgruntled-consumers
end

;; plot the restaurant statistics for the user controlled restaurants
to plot-restaurant-statistics
    ask restaurants with [ auto? = false ]
    [ set-current-plot "Profits"
      set-current-plot-pen user-id
      plot days-profit

      set-current-plot "# Customers"
      set-current-plot-pen user-id
      plot num-customers
    ]

    set-current-plot "Profits"
    set-current-plot-pen "avg-profit"
    plot mean [days-profit] of restaurants

    set-current-plot "# Customers"
    set-current-plot-pen "avg-custs"
    plot mean [num-customers] of restaurants

    set-current-plot "Customer Satisfaction"
    set-current-plot-pen "min."
    plot min [appeal] of customers
    set-current-plot-pen "avg."
    plot mean [appeal] of customers
    set-current-plot-pen "max."
    plot max [appeal] of customers
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Calculation Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; reports the number of tavern restaurants in the marketplace
to-report american-cuisines
  report count restaurants with [ restaurant-cuisine = "American" ]
end

;; reports the number of fine dining restaurants in the marketplace
to-report asian-cuisines
  report count restaurants with [ restaurant-cuisine = "Asian" ]
end

;; reports the number of fast food restaurants in the marketplace
to-report european-cuisines
  report count restaurants with [ restaurant-cuisine = "European" ]
end

;; reports the avg profit from all the owners on the current day
to-report avg-profit/owner
  report mean [ days-profit ] of restaurants
end

to-report avg-customers/owner
  report mean [ num-customers ] of restaurants
end

;; reports the avg energy of the customers
to-report avg-energy/customer
  report mean [ energy ] of customers
end

;; reports the number of customers that can't find a restaurant that they want to eat at
to-report disgruntled-consumers
  report count customers with [ energy < 0 ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Quick Start functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; instructions to quickly setup the model, and clients to run this activity
to setup-quick-start
  set qs-item 0
  set qs-items
  [ "Teacher: Follow these directions to run the HubNet activity."
    "Press the SETUP button, then Press the INITIAL LOGIN button."
    "Everyone: Open up a HubNet Client on your machine, input the IP Address of this computer, press ENTER, type your name, and press ENTER."
    "Teacher: Once everyone has logged in, turn off the INITIAL LOGIN button by pressing it again."
    "Have the students acquaint themselves with their interface."
    "Teacher: Press GO to start the simulation."
    "Each student is a restaurateur. The customers are independent computer agents."
    "Following are some additional features you might want to adjust for later runs:"
    "NUM-CUSTOMERS determines the number of customers: set this before you run the game."
    "The following conditions can be changed either before or while the game is running:"
    "If SHOW-RANK? is on, the students are able to see their ranking amongst all restaurateurs."
    "If BANKRUPCY? is on, then students might go bankrupt."
    "Use the cost sliders, QUALITY-COST, SERVICE-COST and RENT-COST to adjust costs."
    "CUSTOMER-ENERGY determines the beginning energy of the customer."
    "CUSTOMER-THRESHOLD determines the threshold at which a customer gets hungry."
    "To create some automated restaurants set the AUTO-RESTAURANTS slider and press CREATE-AUTOMATED-RESTAURANTS."
    "Teacher: To rerun the activity with the same group, un-press GO, adjust settings, press RE-RUN then GO."
    "Teacher: To start the simulation over with a new group, follow our instruction set again."]
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
  [ hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ create-new-restaurant hubnet-message-source ]
    [ ifelse hubnet-exit-message?
      [ remove-restaurant hubnet-message-source ]
      [ execute-command hubnet-message-tag ] ] ]
end

;; NetLogo knows what each student's restaurant patch is supposed to be
;; doing based on the tag sent by the name Cuisine, Service, Price and Quality
to execute-command [command]
  if command = "Cuisine"
  [ ask restaurants with [ user-id = hubnet-message-source ]
    [ set restaurant-cuisine hubnet-message
      ifelse (restaurant-cuisine = "American")
      [ set shape "restaurant american" ]
      [ ifelse (restaurant-cuisine = "Asian")
        [ set shape "restaurant asian" ]
        [ set shape "restaurant european" ] ] ]
    stop ]
  if command = "Service"
  [ ask restaurants with [ user-id = hubnet-message-source ]
    [ set restaurant-service hubnet-message
      set profit-customer round (restaurant-price - ((Service-Cost * restaurant-service) + (Quality-Cost * restaurant-quality))) ]
    stop ]
  if command = "Quality"
  [ ask restaurants with [ user-id = hubnet-message-source ]
    [ set restaurant-quality hubnet-message
      set profit-customer round (restaurant-price - ((Service-Cost * restaurant-service) + (Quality-Cost * restaurant-quality))) ]
    stop ]
  if command = "Price"
  [ ask restaurants with [ user-id = hubnet-message-source ]
    [ set restaurant-price hubnet-message
      set profit-customer round (restaurant-price - ((Service-Cost * restaurant-service) + (Quality-Cost * restaurant-quality))) ]
    stop ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Owner Setup Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; creates a new owner
to create-new-restaurant [ id ]
  create-restaurants 1
  [ set user-id id
    set auto? false
    reset-owner-variables
    setup-restaurant
    setup-location
    send-personal-info
  ]
end

;; sets up the owners personal variables and location
to setup-restaurant ;; Restaurant procedure
  let helplist remove used-colors colors

  ifelse empty? helplist
  [set restaurant-color one-of colors ]
  [
   set restaurant-color one-of helplist
   set used-colors lput restaurant-color used-colors
  ]
  set color restaurant-color
  set size 2
  set shape one-of ["restaurant american" "restaurant asian" "restaurant european" ]
  ifelse shape = "restaurant american"
  [set restaurant-cuisine "American"]
  [
   ifelse shape = "restaurant asian"
   [set restaurant-cuisine "Asian"]
   [set restaurant-cuisine "European"]
  ]
  reset-owner-variables
end

;; sets up the restaurant's location and premises
to setup-location   ;; owner procedure
  setxy ((random (world-width - 2)) + 1)
        ((random (world-height - 2)) + 1)
  if any? other restaurants in-radius 3
  [ setup-location ]
end

;; reset owners variables to initial values
to reset-owner-variables  ;; owner procedure
  set rank n/a
  set received-rank? false
  set bankrupt? false
  set account-balance 2000
  set days-revenue 0
  set days-cost Rent-Cost
  set days-profit 0
  set profit-customer 100
  set num-customers 0
  set restaurant-price random 50
  set restaurant-service 50 + random 50
  set restaurant-quality 50 + random 50
  if (auto? = false) ;; send the personal info only to clients
  [ send-personal-info ]
  ask restaurants with [auto? = false]
  [
    ;; Setup the plot pens for the restaurant
    set-current-plot "Profits"
    create-temporary-plot-pen user-id
    set-plot-pen-color restaurant-color

    set-current-plot "# Customers"
    create-temporary-plot-pen user-id
    set-plot-pen-color restaurant-color
  ]
end

;; delete restaurant once client has exited
to remove-restaurant [ id ] ;; owner procedure
  let old-color false
  ask restaurants with [user-id = id] ;; remove the owner's turtle
  [ set old-color restaurant-color
    die ]

  if not any? restaurants with [ color = old-color ] ;; make the unused color available again
  [ set used-colors remove (position old-color colors) used-colors ]
end

;; sends the appropriate monitor information back to the client
to send-personal-info ;; restaurant procedure
  hubnet-send user-id "Restaurant Color" (color->string color)
  hubnet-send user-id "Account Balance" account-balance
  hubnet-send user-id "Profit / Customer" profit-customer
  hubnet-send user-id "Rank" rank
  hubnet-send user-id "Bankrupt?" bankrupt?
  hubnet-send user-id "Cuisine" restaurant-cuisine
  hubnet-send user-id "Service" restaurant-service
  hubnet-send user-id "Quality" restaurant-quality
  hubnet-send user-id "Price" restaurant-price
end

;; returns string version of color name
to-report color->string [ color-value ]
  report item (position color-value colors) color-names
end

;; sends the appropriate monitor information back to one client
to send-system-info ;; owner procedure
  hubnet-send user-id "Day" day
end

;; broadcasts the appropriate monitor information back to all clients
to broadcast-system-info
  hubnet-broadcast "Day" day
end
@#$#@#$#@
GRAPHICS-WINDOW
432
100
820
509
10
10
18.0
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

SLIDER
3
95
158
128
Num-Consumer
Num-Consumer
0
500
350
1
1
NIL
HORIZONTAL

SLIDER
3
129
158
162
Consumer-Energy
Consumer-Energy
25
50
50
1
1
NIL
HORIZONTAL

BUTTON
3
60
67
93
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
162
60
227
93
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

MONITOR
3
10
819
55
Quick Start Instructions - More in Info Window
quick-start
0
1
11

BUTTON
433
59
543
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
670
59
744
92
<< Prev
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

BUTTON
745
59
819
92
Next >>
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
68
60
158
93
Initial Login
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

SWITCH
299
129
425
162
Bankruptcy?
Bankruptcy?
1
1
-1000

SWITCH
299
95
425
128
Show-Rank?
Show-Rank?
0
1
-1000

PLOT
217
391
427
511
Profits
Days
$$
0.0
20.0
0.0
100.0
true
false
"" ""
PENS
"avg-profit" 1.0 0 -16777216 true "" ""

PLOT
3
269
215
389
Customer Satisfaction
Day
Satis.
0.0
10.0
0.0
100.0
true
false
"" ""
PENS
"max." 1.0 0 -955883 true "" ""
"avg." 1.0 0 -2674135 true "" ""
"min." 1.0 0 -7500403 true "" ""

PLOT
217
269
427
389
Disgruntled Customers
Day
Custs.
0.0
10.0
0.0
100.0
true
false
"" ""
PENS
"custs." 1.0 0 -13345367 true "" ""

MONITOR
165
218
215
263
Day
day
3
1
11

BUTTON
229
60
321
93
Re-Run
reset
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
163
158
196
Consumer-Threshold
Consumer-Threshold
10
30
30
1
1
NIL
HORIZONTAL

SLIDER
162
95
295
128
Service-Cost
Service-Cost
0.01
0.5
0.2
0.01
1
$
HORIZONTAL

SLIDER
162
163
295
196
Quality-Cost
Quality-Cost
0.01
1
0.2
0.01
1
$
HORIZONTAL

SLIDER
162
129
295
162
Rent-Cost
Rent-Cost
0
200
100
10
1
$
HORIZONTAL

SLIDER
3
200
157
233
#Auto-Restaurants
#Auto-Restaurants
1
5
5
1
1
NIL
HORIZONTAL

BUTTON
3
234
157
267
Create-Restaurants
create-automated-restaurants #Auto-Restaurants
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
225
218
289
263
American
american-cuisines
3
1
11

MONITOR
291
218
355
263
Asian
asian-cuisines
3
1
11

MONITOR
356
218
420
263
European
european-cuisines
3
1
11

PLOT
4
391
214
511
# Customers
Day
Cust.
0.0
10.0
0.0
100.0
true
false
"" ""
PENS
"avg-custs" 1.0 0 -16777216 true "" ""

TEXTBOX
233
200
430
218
Number of restaurants by cuisine:
11
0.0
0

SLIDER
299
163
425
196
day-length
day-length
1
50
5
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This simulates the competition in a a single industry, in this case the restaurant industry. Each restaurant is controlled by an owner trying to maximize profit. Depending on the owners' decisions, the outcome may demonstrate the Efficient Market Theorem ("Pareto efficiency"): if all the agents within a market look out for their own best interest, it will lead to the most efficient outcome. In this case it means that if the restaurant owners try to maximize their own wealth it will also maximize the customer satisfaction.

## HOW IT WORKS

Students act as restaurant owners. Each student is given one restaurant to control and starts with 2000 dollars in his/her account. There are computer-controlled consumers that move around and may choose to become customers in restaurants of their choice. Their color shows which food they like. For instance, a red client likes food that can be found in the red restaurants ('American' cuisine).

Each day the customers decide to eat at a restaurant. In order to try and persuade the customers to come and eat at their restaurants, the owners (the students) have several options they can control: PRICE, QUALITY, SERVICE, and CUISINE. The PRICE slider sets the price of a meal at the restaurant. The QUALITY slider sets the quality of the meal at the restaurant. The SERVICE slider affects the quality of service and appeal of the restaurant (staff and decor quality). Finally the CUISINE determines the type of food the restaurant serves. The owners may change these variables freely during the day.

The quality and service that restaurant offer as well as renting the space all come at a price to the restaurant owners. These prices per quality and per service are set on the server interface. At the end of the day the profit of each owner is calculated and added to his/her account balance and the process repeats.

Initially, each participant will be feeling their way around the market place with different settings. Then as competition continues with each owner trying to maximize profits, the various restaurants proceed to establish themselves in the market place. While the owners continue to aggressively look to attract the most customers and maximize profits, the customers actually begin to benefit. This can be seen in the three plots: average profit (of restaurants), disgruntled customers, and customer satisfaction. The average profit of the restaurants will decrease as the competition heats up and, due to the competition, the number of disgruntled customers will drop and customer satisfaction will rise.

## HOW TO USE IT

Quickstart Instructions: (these instructions also appear at the top of the Interface)
Teacher: Follow these directions to run the HubNet activity.
Press the SETUP button, then Press the INITIAL LOGIN button.

Everyone: Open up a HubNet Client on your machine, input the IP Address of this computer, press ENTER, type your name, and press ENTER.

Teacher: Once everyone has logged in, turn off the INITIAL LOGIN button by pressing it again.
Have the students acquaint themselves with their interface.
Teacher: Press GO to start the simulation.
Each student is a restaurateur. The customers are independent computer agents.
Following are some additional features you might want to adjust for later runs:
NUM-CUSTOMERS determines the number of customers: set this before you run the game.
The following conditions can be changed either before or while the game is running:
If SHOW-RANK? is on, the students are able to see their ranking amongst all restaurateurs.
If BANKRUPCY? is on, then students might go bankrupt.
Use the cost sliders, QUALITY-COST, SERVICE-COST and RENT-COST to adjust costs.
CUSTOMER-ENERGY determines the beginning energy of the customer.
CUSTOMER-THRESHOLD determines the threshold at which a customer gets hungry.
To create some automated restaurants set the AUTO-RESTAURANTS slider and press CREATE-AUTOMATED-RESTAURANTS.
Teacher: To rerun the activity with the same group, un-press GO, adjust settings, press RE-RUN then GO.
Teacher: To start the simulation over with a new group, follow our instruction set again.

Buttons:
SETUP - clears all turtles, patches and plots.  The setup button should only be pressed when beginning a new simulation with a new group of users, otherwise all the data from the old simulation will be lost.
INITIAL LOGIN - allows users to log into the activity without having to start the simulation.
GO - runs the simulation.
RE-RUN - sets up the model to be ready for another run of the simulation with the same users.
CREATE AUTOMATED RESTAURANTS - creates as many automated restaurants as the '#AUTO-RESTAURANTS' slider is set to.
RESET INSTRUCTIONS - resets the Quickstart instruction menu to the beginning of the instructions.
PREV - displays the previous line of the Quickstart instructions in the monitor.
NEXT - displays the next line of the Quickstart instructions in the monitor.

Sliders:
NUMBER-CONSUMERS - the total number of consumers in the market place.
QUALITY-COST - the cost of increasing the quality of the restaurant.
RENT-COST - the amount of money rent costs each day.
SERVICE-COST - the cost increase per each point spent on service.
CUSTOMER-THRESHOLD - the energy level at which customers become hungry.
CUSTOMER-ENERGY - the energy level at which customers start at and are restored to after eating.
\#AUTO-RESTAURANTS - the number of automated owners to create. (To create the owners press the CREATE AUTOMATED RESTAURANTS button after adjusting the slider).

Switches:
SHOW-RANK? - when on, ranks the restaurant owners by their account balance (the rank appears in the clients' monitors only).  When off, the ranks of the owners are not displayed in the clients' monitors.
BANKRUPTCY? - when on, allows owners to go bankrupt when their account balance goes below zero.

Monitors:
AMERICAN CUISINES - the number of American cuisines in the market place.
ASIAN CUISINES - the number of Asian cuisines in the market place.
EUROPEAN CUISINES - the number of European cuisines in the market place.
DAY - what day the simulation is in.

Plots:
CUSTOMER SATISFACTION - plots the maximum, minimum and average restaurant satisfaction of the customers.
DISGRUNTLED CUSTOMERS - plots the number of consumers who cannot find a restaurant to their liking each day.
PROFITS - plots the profits over a period of a day of all the user-controlled restaurants as well as an average (in black).
\# CUSTOMERS - plots the number of customers who attended each user-controlled restaurant over the period of a day as well as an average (in black).

Client Information:
After login is completed the Restaurants client interface will appear for each of the participants, including a restaurant color which will be displayed in the RESTAURANT COLOR monitor. Participant are each credited with 2000 in their individuals accounts balance and the starting settings PRICE, QUALITY, SERVICE and RESTAURANT TYPE are set to random variables.

The client interface contains a number of monitors which contain personal information regarding the participant. ACCOUNT BALANCE states the amount of money that the participant currently has, and RANK shows the participants ranking out of all the owners based on account balance when the SHOW-RANK? switch is in the 'On' position. When the BANKRUPTCY? switch is on, the BANKRUPT monitor shows whether the restaurant is bankrupt. The PROFIT / CUSTOMER monitor shows the amount of money that is made from each additional customer that the restaurant receives for the current day. DAYS REVENUE and DAYS COST show the amount of revenue and costs accumulated during the current day. The RESTAURANT PROFITS and RESTAURANT CUSTOMERS plots show graphs of all the user controlled restaurants (these two plots mirror two of the four plots on the server interface). Finally, DAY shows the day that the simulation is currently on.

The participant is able to control the restaurant by using the SERVICE, PRICE, and QUALITY sliders as well as the CUISINE choice. The participant is able to change all these settings at any time during the simulation. The SERVICE slider controls the quality of the service (waiting staff) at the restaurant. By increasing the SERVICE slider customers will perceive a better meal and a lower price for the meal, all in all increasing the restaurant's appeal. The choice of CUISINE is crucial as it determines which niche the participant enters in the market. The PRICE and QUALITY sliders set the price and the quality of the meal in the restaurant.

The participants can track their own progress in the RANK monitors and aim for the greatest possible account balance.

## THINGS TO NOTICE

The two things to notice in this simulation are in the two plots. As the simulation goes on there should be a downward trend of the avg. profit due to the increased competition between the restaurants, and more importantly there should be a constant increase in the average customer satisfaction value throughout the simulation. The maximum customer satisfaction should also increase along with the average value, the minimum is dependent however on how many automated owners are used. If the amount of actual participants is fairly low, below 7, and the rest are automated, then the minimum value will never rise, as some customers will be eating at the automated restaurants which don't change their variable. The increase of the average customer satisfaction is the important aspect as it is the emergent phenomena of the free market theorem.

## THINGS TO TRY

Initial Settings:
Number-Customers: 350
Show-Rank?: on
Bankruptcy?: off

Quality-Cost: 0.20
Rent-Cost: 100
Service-Cost: 0.20

Customer-Threshold 30
Customer-Energy 50

Initial Run:
The first time the simulation is run with the participants, do not allow any collusion between the participants. In other words, make sure that they at least try not to reveal their variable values to their neighbors. This will make the environment more like a free market system.

However you can choose to run the simulation a second time and tell the participants that they are allowed to share their personal variables with all the other participants if they would like.

What differences appear between the plots in the two different simulations? How did the ability to collude with others change the results of the second simulation?

## EXTENDING THE MODEL

Currently, the automated restaurants do not change their product the way that real users of this simulation so. If they did, it would allow the simulation to run smoother with a lower number of actual participants. Add code so that the automated owners will change their variables when they notice their profit decrease by a significant amount.

## NETLOGO FEATURES

This model uses the create-temporary-plot-pen function to plot the the day's profit for all of the user-controlled restaurants.

## RELATED MODELS

See the Social Science models in the NetLogo models library. 'Wealth Distribution,' for example, deals with similar content as this model -- the human economical behavior in a free market. Also see the HubNet activity 'Tragedy of the Commons' for a simulation of economical aggression under the conditions of limited resources.

## CREDITS AND REFERENCES

This model simulates an Economics concept associated with Vilfredo Pareto.
See V. Pareto, Manuale d'economia politico (Milan,1906).

Thanks to Ben Neidhart for his work on this model.
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

restaurant american
false
0
Circle -6459832 true false 88 13 123
Rectangle -6459832 true false 38 204 261 275
Circle -6459832 true false 19 49 79
Circle -6459832 true false 200 49 83
Rectangle -955883 true false 8 95 293 247
Rectangle -1 true false 46 75 253 117
Rectangle -955883 true false 57 86 240 128
Rectangle -955883 true false 32 95 269 139
Rectangle -1 true false 23 236 278 263
Rectangle -955883 true false 15 214 286 246
Rectangle -955883 true false 37 237 259 255
Rectangle -955883 true false 42 73 254 110
Rectangle -955883 true false 18 235 282 265
Rectangle -1184463 true false 17 105 283 235
Rectangle -1184463 true false 31 225 267 252
Rectangle -1184463 true false 58 84 236 119
Circle -1184463 true false 94 49 110
Rectangle -2674135 true false 15 105 284 235
Rectangle -2674135 true false 55 83 239 121
Rectangle -2674135 true false 27 227 270 254
Circle -2674135 true false 96 41 105
Circle -7500403 true true 99 114 95
Circle -16777216 false false 105 120 90

restaurant asian
false
0
Circle -6459832 true false 88 13 123
Rectangle -6459832 true false 38 204 261 275
Circle -6459832 true false 19 49 79
Circle -6459832 true false 200 49 83
Rectangle -955883 true false 8 95 293 247
Rectangle -1 true false 46 75 253 117
Rectangle -955883 true false 57 86 240 128
Rectangle -955883 true false 32 95 269 139
Rectangle -1 true false 23 236 278 263
Rectangle -955883 true false 15 214 286 246
Rectangle -955883 true false 37 237 259 255
Rectangle -955883 true false 42 73 254 110
Rectangle -955883 true false 18 235 282 265
Rectangle -1184463 true false 17 105 283 235
Rectangle -1184463 true false 31 225 267 252
Rectangle -1184463 true false 58 84 236 119
Circle -1184463 true false 94 49 110
Circle -7500403 true true 105 118 89
Circle -16777216 false false 105 120 90

restaurant european
false
0
Circle -6459832 true false 88 13 123
Rectangle -6459832 true false 38 204 261 275
Circle -6459832 true false 19 49 79
Circle -6459832 true false 200 49 83
Rectangle -955883 true false 8 95 293 247
Rectangle -1 true false 46 75 253 117
Rectangle -955883 true false 57 86 240 128
Rectangle -955883 true false 32 95 269 139
Rectangle -1 true false 23 236 278 263
Rectangle -955883 true false 15 214 286 246
Rectangle -955883 true false 37 237 259 255
Rectangle -955883 true false 42 73 254 110
Rectangle -955883 true false 18 235 282 265
Rectangle -2674135 true false 7 94 293 247
Rectangle -2674135 true false 41 73 254 101
Rectangle -2674135 true false 16 243 282 265
Rectangle -11221820 true false 16 104 282 238
Rectangle -11221820 true false 52 81 241 112
Rectangle -11221820 true false 26 229 270 258
Circle -11221820 true false 101 46 94
Circle -7500403 true true 100 118 93
Circle -16777216 false false 105 120 90

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
349
11
727
389
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

SLIDER
6
167
134
200
Service
Service
1
100
50
1
1
NIL
HORIZONTAL

SLIDER
6
237
134
270
Price
Price
1
100
50
1
1
NIL
HORIZONTAL

SLIDER
6
202
134
235
Quality
Quality
1
100
50
1
1
NIL
HORIZONTAL

CHOOSER
6
120
134
165
Cuisine
Cuisine
"American" "Asian" "European"
0

MONITOR
9
10
113
59
Restaurant Color
NIL
0
1

MONITOR
114
10
216
59
Account Balance
NIL
0
1

MONITOR
145
122
279
171
Number of Customers
NIL
0
1

MONITOR
9
63
59
112
Day
NIL
0
1

MONITOR
264
64
341
113
Day's Profit
NIL
0
1

MONITOR
218
10
281
59
Bankrupt?
NIL
3
1

MONITOR
145
173
279
222
Profit / Customer
NIL
2
1

PLOT
173
272
344
392
Profits
Days
$$
0.0
20.0
0.0
10.0
true
false
"" ""
PENS
"avg-profit" 1.0 0 -16777216 true

MONITOR
284
10
341
59
Rank
NIL
3
1

PLOT
6
272
170
392
# Customers
Day
Cust.
0.0
10.0
0.0
100.0
true
false
"" ""
PENS
"avg-custs" 1.0 0 -16777216 true

MONITOR
86
64
183
113
Day's Revenue
NIL
0
1

MONITOR
184
64
257
113
Day's Cost
NIL
0
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

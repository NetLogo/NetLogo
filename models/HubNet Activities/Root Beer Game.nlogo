globals [
  colors         ;; the available team colors (don't use all of them for aesthetic reasons)
  color-names    ;; the colors in words
  roles          ;; the names of the possible roles on each team
]

;; each client is represented by a player turtle
breed [ players player ]
players-own
[
  user-id
  inventory
  back-orders       ;; at the end of each week back-orders will be calculated if
                    ;; your inventory does not meet the demand
  my-team
  order             ;; a direct reflection of the value in the ORDERS-TO-PLACE
                    ;; slider on the client this changes any time the user moves the slider
  last-received     ;; the last number of cases added to your inventory
  order-placed?     ;; whether or not you've placed an order this week, once you have placed an
                    ;; order you cannot change it until the next week. Once all players on a team
                    ;; have placed an order the week is advanced.
  role              ;; factory, distributor, wholesaler, or retailer depending on
                    ;; your place in the chain
  ordered           ;; the number of cases of root beer ordered this week,
                    ;; once you have pressed the PLACE-ORDER button on the client
                    ;; you cannot change this number until next week
]

;; there are four players per team the team breed
;; is mainly used to keep track of information
breed [ teams team ]
teams-own
[
  members       ;; the number of members on the team
  cost          ;; the instantaneous cost of the entire team at the end of the last week.
  demand        ;; the number of cases requested by the public
  last-player   ;; keep track of the last player in the chain so it's easy to add players
  color-name
  clock         ;; each team has their own clock since the end of the week occurs
                ;; only when all players have placed an order
  last-received ;; the last number of cases added to inventory
  inventory     ;; the number of cases currently stored by this player
]

;; there are two types of links in the game
;; there are links that players use to request root beer
;; from the players up the chain, the demand links,
;; and the supply links move the goods from
;; one player to another.

directed-link-breed [ supply-links supply-link ]
supply-links-own
[
  orders-filled ;; a list PERIODS-OF-DELAY long, each time the source player ships the current order
                ;; is placed on the end of the list and the order at the beginning of the list is
                ;; popped off and added to the destination player's inventory
]

directed-link-breed [ demand-links demand-link ]
demand-links-own
[
  orders-placed  ;; the number of cases requested by the player at the source,
                 ;; this is the demand for the player at the destination end of the link
]

;;
;; Setup Procedures
;;
to startup
  hubnet-reset
  ;; setup basic appearance globals
  set-default-shape players "circle"
  set-default-shape teams "square"
  set colors [ red blue green violet pink orange brown yellow ]
  set color-names [ "red" "blue" "green" "violet" "pink" "orange" "brown" "yellow" ]
  set roles [ "retailer" "distributor" "wholesaler" "factory" ]
  setup
end


;; start the game over with the same players
;; initial conditions, each player has placed an order of 4
;; and has an inventory of 12.
to setup
  ask supply-links
    [ set orders-filled n-values periods-of-delay [ 0 ]  ]
  ask demand-links
    [ set orders-placed 4 ]
  ask teams [
    set cost 0
    set clock 1
    if not any? out-demand-link-neighbors
    [ die ]
  ]
  ask players
  [
    set inventory 12
    set back-orders 0
    set last-received 0
    set order-placed? false
    set color [color] of my-team + 2
    set ordered 4
    update-player
  ]
  reset-plot
end

;;
;; Runtime Procedures
;;

to go
  ;; the flow of the activity is completely controlled by actions
  ;; of the clients. when everyone on the team has placed an order
  ;; the entire team moves on the next week. Teams may be on different days
  listen-to-clients
  every 0.1
  [
    display
  ]
end

to listen-to-clients
  while [ hubnet-message-waiting? ]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ create-new-student ]
    [
      ifelse hubnet-exit-message?
      [ remove-student ]
      [
        execute-command hubnet-message-tag
      ]
    ]
  ]
end

to end-week ;; team procedure
  ;; calculate the cost for the entire team
  set cost sum [ inventory * 0.5 + back-orders ] of
                   players with [ my-team = myself ]

  plot-cost
  plot-shipped
  ;; the last player is the factory
  ask last-player [ plot-orders ]

  ;; update the external demand to the retailer that drives the game
  ask my-out-demand-links
  [
    ;; the demand starts at 4 cases per week. In week 7 it rises to
    ;; 8 cases and remains there the rest of the time.
    set orders-placed ifelse-value ([clock] of myself <= 5) [ 4 ][ 8 ]
  ]

  ;; produce the goods at the factory level
  let orders-requested sum [ orders-placed ] of my-in-demand-links

  ask my-out-supply-links
  [ set orders-filled lput orders-requested orders-filled ]

  ;; move goods into the factory, even though the factory
  ;; has the ability to produce an infinite amount of goods
  ;; there are still PERIODS-OF-DELAY weeks between the time
  ;; they place the order for goods and the time it is produced.
  ask out-supply-link-neighbors
  [
     set last-received sum [ first orders-filled  ] of my-in-supply-links
     ask my-in-supply-links [ set orders-filled but-first orders-filled ]
     set inventory inventory + last-received
  ]

  ;; everything is purchased on the retailer level
  set inventory 0

  ;; advance the week
  set clock clock + 1
  ;; update the client information for the next round
  ask players with [ my-team = myself ]
  [
    set order-placed? false
    set color [color] of my-team + 2
    ask my-out-demand-links [ set orders-placed [ordered] of myself ]
  ]
end

to place-order ;; player procedure
  ;; only change the order if we haven't
  ;; done so this week as each player can only
  ;; place 1 order per day and cannot change it
  ;; once it is placed
  if not order-placed?
  [
    set order-placed? true
    set color [color] of my-team
    set ordered order
    update-player
  ]

  let teammates players with [ my-team = [my-team] of myself ]

  ;; once the last player has placed an order
  ;; all players ship and advance the week
  if not any? teammates with [ not order-placed? ]
  [
    ask teammates
    [
      ship
      ask my-out-demand-links
        [ set orders-placed [ordered] of myself ]
    ]
    ask my-team
    [
      if clock <= weeks-of-simulation
      [ end-week ]
    ]
    ;; make sure the info on the client gets updated
    ask teammates
    [ update-player ]
  ]
end

to ship
  ;; move all the supplies along the chain.
  ask out-supply-link-neighbors
  [
    ;; grab the shipment at the beginning of the list
    ;; and add it to my inventory
    set last-received sum [ first orders-filled  ] of my-in-supply-links
    ask my-in-supply-links
      [ set orders-filled but-first orders-filled ]
    set inventory inventory + last-received
  ]

  ;; determine how many orders you need to send upstream
  ;; this will be no more than the number of orders requested
  ;; plus back orders or your inventory whichever is smaller
  let new-orders [orders-placed] of one-of my-in-demand-links
  let orders-requested new-orders + back-orders

  ;; determine how much we can send given our current inventory
  let orders-to-ship min list orders-requested inventory

  ;; if there's not enough inventory they become back-orders
  set back-orders max list 0 ( back-orders - inventory + new-orders )

  ;; add the shipment to the shipping queue
  ask my-out-supply-links
    [ set orders-filled lput orders-to-ship orders-filled ]
  set inventory inventory - orders-to-ship
end

;;
;; HubNet Procedures
;;

to create-new-student
  ;; if there are no incomplete teams
  ;; create a new one
  if not any? teams with [ members < 4 ]
  [ create-team ]

  add-student-to one-of teams with [ members < 4 ]
end

to create-team
 ;; there is one team per row in the world, if
 ;; we've run out of rows the user should
 ;; just make the size of the world larger
 let p one-of patches with [ pxcor = max-pxcor and not any? teams-here ]
 ifelse p != nobody
 [
  create-teams 1
  [
    set members 0
    set cost 0
    set demand 4
    set last-player self
    move-to p
    set-color
    set size 0.5
    create-plot-pens
    set clock 1
  ]
 ]
 [ user-message "There is no more space for a new team, please increase the size of the world" ]
end

;; grab a random color from the list of distinguishable colors
to set-color
  ;; if the size of the world is increased we have to reuse colors
  if length colors = 0
  [
    set colors [ red blue green violet pink orange brown yellow ]
    set color-names [ "red" "blue" "green" "violet" "pink" "orange" "brown" "yellow" ]
  ]
  let index random length colors
  set color item index colors
  set color-name item index color-names
  set colors remove-item index colors
  set color-names remove-item index color-names
end

to add-student-to [team]
  create-players 1
  [
    set user-id hubnet-message-source
    set label user-id
    set inventory 12
    set back-orders 0
    set order 4
    set ordered 0
    ;; attach the new player to the end of the chain.
    ask [last-player] of team
    [
       create-demand-link-to myself
         [ set orders-placed 4 ]
       create-supply-link-from myself
         [ set orders-filled n-values periods-of-delay [ 0 ] ]
    ]
    ;; set up directly left of the current last player
    setxy [xcor] of [last-player] of team - 1 [ycor] of [last-player] of team
    set my-team team
    set role my-role [members] of my-team
    ask team
    [
      set last-player myself
      set members members + 1
      ask my-out-supply-links [ die ]
      ask my-in-demand-links [ die ]
      create-supply-link-to myself
        [ set orders-filled n-values periods-of-delay [ 0 ] hide-link ]
      create-demand-link-from myself
        [ set orders-placed 4 hide-link ]
    ]
    set order-placed? false
    set color [color] of team + 2
    init-player
  ]
end

;; set size appropriate to your position in the chain.
;; factories are biggest and retailers smallest
;; report the name of the role
to-report my-role [degree]
  set size degree * 0.1 + 0.5
  report item degree roles
end

;; if a player leaves the game we may need to
;; get new roles since the new player will be
;; added to the end rather than where the last player left.
to reassign-role
  let degree [xcor] of my-team - xcor - 1
  set role my-role degree
  set role item degree roles
end

to remove-student
  ask players with [ user-id = hubnet-message-source ]
    [ remove-self ]
end

to remove-self
  ;; if I am the last player make my neighbor
  ;; to the right the last player
  if [last-player] of my-team = self
  [
    ask my-team
    [
      set last-player [ one-of out-supply-link-neighbors ] of myself
    ]
  ]

  ask my-team
  [
    set members members - 1
    ;; if I am the last player take the team with me.
    if members = 0
    [
      ;; return the colors to the available list
      set colors lput color colors
      set color-names lput color-name color-names
      die
      ;; we've shifted perspective to make the above code
      ;; simpler so we must use myself to kill the player
      ;; as well as the team.  if we're the last player in
      ;; the team we don't have to do the rest of the bookkeeping
      ;; below so we do want to abort immediately
      ask myself [ die ]
    ]
  ]

  ;; if this was the last player on the team this code never
  ;; gets executed because the above code kills the turtle

  ;; players up the chain from me move into their
  ;; new position so there aren't gaps
  let affected-players players with [ my-team = [my-team] of myself and xcor < [xcor] of myself ]
  ask affected-players
    [ set xcor xcor + 1 ]

  ;; link my supply and demand neighbors to
  ;; each other instead of me.
  let n one-of out-supply-link-neighbors
  ask in-supply-link-neighbors
  [
    if n != nobody and not out-supply-link-neighbor? n
    [ create-supply-link-to n
      [ set orders-filled n-values periods-of-delay [ 0 ] ] ]
  ]

  set n one-of out-demand-link-neighbors
  ask in-demand-link-neighbors
  [
    if n != nobody and not out-demand-link-neighbor? n
    [ create-demand-link-to n [] ]
  ]

  ask affected-players
  [
    reassign-role
    init-player
  ]
  die
end

;; there are only two client actions.
;; moving the ORDERS-TO-PLACE slider
;; and placing the order.
to execute-command [cmd]
  ask players with [ user-id = hubnet-message-source ]
  [
    ;; don't let players keep going once they've reached the
    ;; end of the simulation
    if [clock] of my-team <= weeks-of-simulation
    [
      ;; catch changes to the slider
      ifelse hubnet-message-tag = "orders-to-place"
      [ set order hubnet-message ]
      ;; lock in the order
      [ if hubnet-message-tag = "place-order"
        [
          ask players with [ user-id = hubnet-message-source ]
            [ place-order ]
        ]
      ]
    ]
  ]
end

;; send everything
to init-player
  hubnet-send user-id "team" [color-name] of my-team
  hubnet-send user-id "role" role
  update-player
end

;; send the stuff that changes
to update-player
  if [clock] of my-team <= weeks-of-simulation
  [
    hubnet-send user-id "demand" [orders-placed] of one-of my-in-demand-links
    hubnet-send user-id "inventory" inventory
    hubnet-send user-id "back-orders" back-orders
    hubnet-send user-id "my-cost" inventory * 0.5 + back-orders
    hubnet-send user-id "last-amount-shipped" sum [ last orders-filled ] of my-out-supply-links

    hubnet-send user-id "last-amount-received" round last-received
    hubnet-send user-id "week" round [clock] of my-team
    hubnet-send user-id "order-placed?" order-placed?
    hubnet-send user-id "order-placed" ordered
  ]
end

;;
;; Plotting Procedures
;;

;; use temporary plot pens for each team
;; so the legend is neat and the plot pen color
;; matches the color of the team
to create-plot-pens
  create-plot-pen "Orders to Factory"
  create-plot-pen "Cost"
  create-plot-pen "Orders Shipped"
end

to create-plot-pen [my-plot]
  set-current-plot my-plot
  create-temporary-plot-pen color-name
  set-plot-pen-color color
end

to plot-orders
  set-current-plot "Orders to Factory"
  plot last-received
end

to plot-cost
  set-current-plot "Cost"
  set-current-plot-pen color-name
  plot cost
end

to plot-shipped
  set-current-plot "Orders Shipped"
  set-current-plot-pen color-name
  plot sum [ last orders-filled ] of my-in-supply-links
end

;; clearing the plot also clears the temporary
;; plot pens, so recreate them.
to reset-plot
  clear-all-plots
  ask teams [ create-plot-pens ]
end
@#$#@#$#@
GRAPHICS-WINDOW
317
10
727
441
-1
-1
50.0
1
12
1
1
1
0
0
0
1
0
7
0
7
1
1
0
ticks

BUTTON
58
62
161
95
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

BUTTON
164
62
267
95
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
75
131
256
164
weeks-of-simulation
weeks-of-simulation
0
100
30
1
1
NIL
HORIZONTAL

PLOT
317
446
616
656
Orders to Factory
weeks
orders
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

PLOT
14
231
313
441
Cost
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
"default" 1.0 0 -16777216 true "" ""

PLOT
14
446
313
655
Orders Shipped
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
"default" 1.0 0 -16777216 true "" ""

SLIDER
75
96
256
129
periods-of-delay
periods-of-delay
1
10
2
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This is an adaptation of a popular game created at MIT in the early 1960s that shows how small delays in a distribution system can create big problems.  The participants take on one of four roles in a distribution network for root beer -- the factory, the distributor, the wholesaler, or the retailer.  Each participant places and ships orders while trying to keep their costs to minimum. Costs include the holding inventory as well as missing out on sales because you produced too little root beer.

## HOW IT WORKS

The game is played in one-week rounds and the players are dividing into teams of four.  Each team is a supply chain that consists of a retailer, a wholesaler, a distributor, and a factory.  At the beginning of each week, every player on a team faces a demand for root beer that comes from the person downstream from him or her in the supply chain. The demand originates from the (invisible) root beer drinkers who create demand for the retailer every week.

At the end of each week, if you have enough inventory to meet your demand, then the demanded quantity is automatically shipped out of your inventory to the player downstream from you. If you don't have enough cases in inventory to meet demand from the player upstream from you (or from the public if you are the retailer), then a backorder is created.  Each backordered case costs the player $1.00 in lost sales opportunity.  Each case in inventory costs the player $0.50 to store.  The goal of the game is to minimize cost.

As a player, each week you must decide how many cases to order from the person upstream from you. Placing orders creates additional demand for your upstream supplier the next week, but it does not immediately replenish your inventory. There is time lag because of a shipping delay between you and your upstream supplier. (If you are the factory, placing an order is equivalent to producing the root beer, and the lag is due to the time to produce the product.) Once all players have placed orders for the week, time advances to the next week and the product you are shipping goes out the door.  Your cost for the week is also calculated and reported.

## HOW TO USE IT

Press the SETUP button and then the GO button and have all the participants login. Explain that each week, each role needs to ship cases of root beer based on the demand they face, as well as place orders to replenish their inventory to the desired level.  Explain also how costs are accrued and that the goal is to minimize costs.  It is a good idea to play a few weeks together until the participants get acquainted with the client interface and the actions needed in each round.

Buttons and Sliders:
SETUP - Resets the simulation to the initial conditions and the clock to zero.

GO - Listens for input from clients. Should be pressed to run the simulation.

WEEKS-OF-SIMULATION - Sets the number of rounds in the game.

PERIODS-OF-DELAY - Set the number of weeks between when one player sends an order and the next player in the line receives the order

Plots:
COST - Plots the instantaneous cost of each team where cost equals $0.50 per case in inventory plus $1.00 per back-ordered case.
ORDERS SHIPPED - Plots the number of orders shipped from the retailer each week.
ORDERS TO FACTORY - Plots the number of order produced by the factory each week.

Client Interface:
Each client is a member of a TEAM identified by a color; all teams have four members. Each client is assigned a ROLE in that team which is determined by his or her position in the supply chain, the first player to login is the retailer and the last is the factory.  Players start out with an initial INVENTORY of 12 and an initial order of 4.

To change the amount of the order you want to place, move the ORDERS-TO-PLACE slider and press the PLACE-ORDER button.  The amount of your last order placed appears in the ORDER-PLACED monitor.  You can only place one order per week, once you have done so the ORDER-PLACED? monitor will display true, and it will not let you place another order until everyone on your team as placed an order, which ends the week. When the week ends everyone ships and you will receive goods from your supplier the amount you receive will be displayed in the LAST-AMOUNT-RECEIVED monitor, and it will be added to your inventory. The amount you ship is displayed in the LAST-AMOUNT-SHIPPED monitor. At the end of the week if your inventory does not meet the demand the extra orders will become BACK-ORDERS. Finally, the order placed by the player that you supply in the previous week will become your new DEMAND, unless you are the retailer, then you will see the true demand of the public.

Your instantaneous cost is displayed in the MY-COST monitor, this includes all goods that you have shipped but have not yet arrived at their intended destination.

## THINGS TO NOTICE

Look at the resulting orders shipped and orders to factory plots.  Are the levels fairly constant or fluctuating?  Have the retailer record the demand he or she faces each week. Are the changes in the orders shipped and orders to the factory plots map similar to the fluctuations in demand?

## THINGS TO TRY

As a team, can you create a strategy that keeps your inventory from fluctuating more than the customer demand? Does it get easier or harder if you increase the PERIODS-OF-DELAY?

## EXTENDING THE MODEL

Create a supply chain that is not linear and has multiple players for each role, perhaps introducing competition within roles.

Create a non-HubNet version of this game using the System Dynamics Modeler.

## NETLOGO FEATURES

This activity uses NetLogo's network support to move the goods from stage to stage.  Each player as a supply link from the player below them in the chain and a demand link from the play above them in the chain.  Orders are placed on the demand links and received on the supply links.

## RELATED MODELS

Investments
Public Good

## ## CREDITS AND REFERENCES

For a discussion of the original MIT Beer Game and additional suggestions for discussion points, see http://web.mit.edu/jsterman/www/SDG/beergame.html

For a bibliography of the beer game in academic publications (last revised July 1992), see http://www.sol-ne.org/repository/download/bibl.html?item_id=456327

For instructions and materials for running the original board game version, see http://www.sol-ne.org/repository/download/instr.html?item_id=456354
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

demand link
true
0
Line -7500403 true 150 0 105 150
Line -7500403 true 150 300 105 150

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
MONITOR
159
61
266
110
demand
NIL
3
1

MONITOR
209
113
316
162
inventory
NIL
3
1

MONITOR
268
61
375
110
back-orders
NIL
3
1

MONITOR
20
10
127
59
team
NIL
3
1

SLIDER
295
223
467
256
orders-to-place
orders-to-place
0
100
4
1
1
NIL
HORIZONTAL

TEXTBOX
326
174
477
202
cost = $0.50 * inventory + $1.00 * back-orders
11
0.0
0

MONITOR
210
163
317
212
my-cost
NIL
3
1

MONITOR
69
113
207
162
last-amount-received
NIL
3
1

MONITOR
318
113
458
162
last-amount-shipped
NIL
3
1

BUTTON
189
223
294
256
place-order
NIL
NIL
1
T
OBSERVER
NIL
O

MONITOR
294
10
401
59
week
NIL
3
1

MONITOR
404
10
512
59
order-placed?
NIL
3
1

MONITOR
131
10
238
59
role
NIL
3
1

MONITOR
82
215
187
264
order-placed
NIL
3
1

@#$#@#$#@
default
0.5
-0.2 0 0.0 1.0
0.0 1 1.0 0.0
0.2 0 0.0 1.0
link direction
true
0

@#$#@#$#@
0
@#$#@#$#@

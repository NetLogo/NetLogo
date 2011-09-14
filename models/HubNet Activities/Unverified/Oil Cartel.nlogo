globals [
  num-sellers
  monopoly-quantity        ;; the amount that would be supplied by the cartel if it behaved as a monopoly
  monopoly-price           ;; the price expected if monopoly-quantity is supplied
  monopoly-profit          ;; the maximum profit that the cartel could get if it behaved as a monopolist
  total-supply             ;; sum of the actual quantity supplied by all sellers
  perfect-market-quantity  ;; the amount that would be supplied under perfect competition
  perfect-market-price     ;; the price expected under perfect competition
  individual-quota         ;; the quota for each individual seller based on the cartel agreement
  marginal-cost            ;; the cost of producing one more unit of oil
  avg-price                ;; the average price of oil across all transactions
  min-price                ;; the minimum price available for oil on the market
  low-sellers              ;; the agentset of sellers selling oil for min-price
  color-list-1             ;; used to distribute colors for display of sellers
  color-list-2             ;; used to distribute colors display of sellers
  shape-1?                 ;; used to toggle between shapes for display of sellers
  age-of-news              ;; age of news item sent to sellers.   used to clear old news
  time-period              ;; tracks current time period
]

breed [ sellers seller ]         ;; every hubnet client is a seller
breed [ buyers buyer ]


sellers-own [
  user-id                         ;; unique user-id, input by the client when they log in
  last-sale-profit                ;; how much money the user made last time oil was sold at market
  price-1                         ;; the price of the primary, public offer
  price-2                         ;; the price of the side, private offer
  p1-quantity-offered             ;; amount of oil offered per time period at price-1
  p2-quantity-offered             ;; amount of oil offered per time period at price-2
  p1-quantity-available           ;; amount of oil at price-1 available within a time period as buyers start buying
  p2-quantity-available           ;; amount of oil at price-2available within a time period as buyers start buying
  p1-quantity-sold                ;; amount of oil actually sold at the end of the time period at price-1
  p2-quantity-sold                ;; amount of oil actually sold at the end of the time period at price-1
  prev-p2-quantity-sold           ;; amount of oil sold at price-2 during the previous time period
  profit-needed                   ;; profit needed by each cartel member. varies by seller
  strategy                        ;; set by client.  explained in "Make Offer" procedure
  extra-output                    ;; set by client.  part of "Quota-Plus" strategy. explained in "Make Offer" procedure
  reduced-output                  ;; set by client.  part of "Quota-Minus" strategy. explained in "Make Offer" procedure
]

buyers-own [
  quantity-demanded               ;; amount demanded by a buyer at any given moment in a time period
  quantity-bought                 ;; amount bought by a buyer at any given moment in a time period
  seller-bought-from              ;; the seller who sold to the buyer last
  max-price                       ;; the maximum amount a buyer is willing to pay for their ideal quantity
  already-chosen?                 ;; only used to distribute different levels of max-price across buyers
]

;; ---------------------------------------------------------
;; STARTUP - auto-setup when the model is opened
;; ---------------------------------------------------------
to startup

  hubnet-reset

  ;; Create buyers
  create-buyers num-buyers [
    setxy random-pxcor random-pycor
    set color green
  ]

  ;; set up colors and shape options for sellers
  initialize-shapes

  ;; make Current Profit a plot that can vary by client
 ;; __hubnet-make-plot-narrowcast "Current Profit Plot"

end

;; ---------------------------------------------------------
;; RESET - reset everything for a new simulation
;; ---------------------------------------------------------
to reset

  ;; clear out current buyers
  ask buyers [die]

  ;; Create new buyers
  create-buyers num-buyers [
    setxy random-pxcor random-pycor
    set color green
  ]

  ;; initialize values
  initial-login

  ;; initialize colors and shape options for seller shapes
  initialize-shapes

  ;; Reset plots
  set-current-plot "Current Profit Plot"
  ;;ask sellers [ __hubnet-clear-plot user-id ]

  set-current-plot "Oil Sold on Market"
  clear-plot

  set-current-plot "Average Price of Oil"
  clear-plot

end

;; ---------------------------------------------------------
;; INIT-GLOBALS - initialize our global variables
;; ---------------------------------------------------------
to init-globals
  set age-of-news 0
  set avg-price 0
  set min-price 0
  set monopoly-profit ( monopoly-quantity * ( monopoly-price - marginal-cost) )
  set time-period 0
end


;; ---------------------------------------------------------
;; INITIALIZE-SHAPES - listen for clients and establish them as
;;                 sellers
;; ---------------------------------------------------------
to initialize-shapes
  ;; set up colors for seller shapes
  set color-list-1      (list ["White" white]  ["Red" (red)]  ["Pink" pink] ["Lime" lime] ["Sky Blue" sky] ["Magenta" magenta]
                              ["Cyan" cyan] ["Turquoise" turquoise]  ["Brown" brown] ["Blue" blue])
  set color-list-2 reverse color-list-1
  set shape-1? true
end


;; ---------------------------------------------------------
;; INITIAL-LOGIN - listen for clients and establish them as
;;                 sellers
;; ---------------------------------------------------------
to initial-login

  ;; Clear all of our buyer, seller, and global variables
  init-globals
  init-seller-vars
  init-buyer-vars

  ;; Listen for logins, etc.
  listen-to-clients

  ;; set cartel agreement
  update-cartel-agreement

  ;; Calculate market conditions
  compute-market-data

  ;;initialize seller information
  update-client-monitors
  if num-sellers != 0 [ hubnet-broadcast "Current Profit" precision (monopoly-profit / num-sellers) 0]

  ;; initialize seller strategies
  hubnet-broadcast "Strategy" "Agreement"
  hubnet-broadcast "extra-output" 0
  hubnet-broadcast "reduced-output" 0
end


;; ---------------------------------------------------------
;; RUN-MARKET - run the market simulation
;; ---------------------------------------------------------
to run-market

  ;; Make sure there are sellers before running the market
  if num-sellers = 0 [
   user-message "Can't run market with no sellers.  Please start a hubnet client."
   stop
  ]

  ;; get current strategies from sellers
  listen-to-clients

  ;; ask sellers to make offers based on their strategies
  make-offers

  ;; ask buyers to identify seller(s) with the lowest price
  find-lowest-offer

  ;; ask buyers to purchase oil from sellers
  execute-transactions

  ;; calculate market conditions based on buyers and sellers most recent actions
  compute-market-data

  ;; ask sellers to update individual seller profit information
  update-sellers

  ;; ask buyers to update individual buyers demand satisfaction information
  update-buyers

  ;; send updated information to client monitors
  update-client-monitors

  ;; do plots
  plot-current-profit
  plot-oil-amounts
  plot-oil-price

  ;; update time-period
  set time-period time-period + 1

end

;; ---------------------------------------------------------
;; MAKE-OFFERS - Sellers make concrete offers to market
;;               based on the strategy they selected. The
;;               strategies are implemented by offering up to two
;;               simultaneous but separate offers to the buyers.
;; ---------------------------------------------------------
to make-offers
  ask sellers [

    ;; save off needed info from previous offer before creating a new one
    let prev-quantity-offered (p1-quantity-offered + p2-quantity-offered)

    ;; "Agreement" Strategy:  Produce and price exactly in accordance with
    ;; the cartel agreement
    ifelse strategy = "Agreement"
    [ set price-1 monopoly-price
      set p1-quantity-offered individual-quota
      set price-2 monopoly-price
      set p2-quantity-offered 0
     ]
    ;; "Quota-Plus" Strategy: Consistently produce "extra-output" amt beyond the quota and
    ;; offer it to the mkt for a price a little lower than the official agreement price
    [ ifelse strategy = "Quota-Plus"
      [ set price-1 monopoly-price
        set p1-quantity-offered individual-quota
        set price-2 (monopoly-price - 100)
        set p2-quantity-offered extra-output
       ]

      ;; "Quota-Minus" Strategy:  Consistently produce "reduced-output" amt below the quota
      ;; in an effort to keep prices up
      [ ifelse strategy = "Quota-Minus"
        [ set price-1 monopoly-price
          set p1-quantity-offered (individual-quota - reduced-output)
          set price-2 monopoly-price
          set p2-quantity-offered 0
        ]

        ;; "Flood Market" Strategy:  Saturate the market with low cost oil to punish cheaters.
        [ ifelse strategy = "Flood Market"
          [  set price-1 monopoly-price
             set p1-quantity-offered 0
             set price-2 marginal-cost
             set p2-quantity-offered perfect-market-quantity
             send-news-item word user-id " unleashes reserves as warning"
          ]

          ;; "Price > MC" Strategy: Keep producing and offering additional output as long
          ;; as the price you have to offer to sell that unit is still higher than the cost
          ;; to produce it.
          [ set price-1 monopoly-price
            set p1-quantity-offered 0
            if time-period = 0 [
              set p2-quantity-offered individual-quota
              set price-2 monopoly-price
              set prev-quantity-offered p2-quantity-offered
            ]

            ;; if you didn't sell more this time than last time, undercut your own price
            ;; and try the same amount again.
            ifelse (p2-quantity-sold <= prev-p2-quantity-sold)
            [ ifelse (price-2 - 10) > marginal-cost
               [ set price-2 (price-2 - 10) ]
               [ set price-2 marginal-cost ]  ;; but don't go as far as pricing below cost
              set p2-quantity-offered prev-quantity-offered
            ]
            ;;if you did sell more that last time, increase production even a little more
            ;; (as long as price > mc)
            [ if price-2  > marginal-cost [
                set p2-quantity-offered (p2-quantity-offered + 10)  ;; amt offered keeps increasing
               ]
            ]

          ]
       ]
    ]
   ]

   ;; initialize amounts available for sale in the next time period to
   ;; the total amount you are willing to offer
   set p1-quantity-available p1-quantity-offered
   set p2-quantity-available p2-quantity-offered

  ]
end



;; ---------------------------------------------------------
;; COMPUTE-MARKET-DATA - recompute the market
;;                        using the latest info
;; ---------------------------------------------------------
to compute-market-data

  ;; Check the number of sellers
  set num-sellers count sellers

  ;; The actual amount supplied
  set total-supply (sum [p1-quantity-sold] of sellers + sum [p2-quantity-sold] of sellers)

  ;; Calculate the average selling price
  ifelse total-supply != 0
    [ set avg-price ( (sum [price-1 * p1-quantity-sold] of sellers with [p1-quantity-sold > 0] +
                       sum [price-2 * p2-quantity-sold] of sellers with [p2-quantity-sold > 0])
                       / total-supply )
    ]
    [ set avg-price 0 ]

  ;; Calculate hypothetical quantity and price under perfect competition
  ;; Economic theory predicts that point is where price equals marginal cost
  set perfect-market-price marginal-cost
  set perfect-market-quantity filter-zero-or-greater (num-buyers * ideal-quantity - perfect-market-price)

end

;; ---------------------------------------------------------
;; UPDATE-SELLERS - update seller information
;; ---------------------------------------------------------
to update-sellers
  ask sellers [

    ;; figure out how much, if any, extra production there was
    let unused-p1-qty (p1-quantity-offered - p1-quantity-sold) ;; amount produced but not sold at price 1
    let extra-produced filter-zero-or-greater (p2-quantity-offered - unused-p1-qty)

    ;; update profit info
    set last-sale-profit int ( (p1-quantity-sold * price-1) + (p2-quantity-sold * price-2)
                           - ((p1-quantity-offered + extra-produced) * marginal-cost) )
   ]
end

;; ---------------------------------------------------------
;; UPDATE-BUYERS - update buyer information
;; ---------------------------------------------------------
to update-buyers
  ask buyers [
    ;; update color
    ifelse quantity-bought > 0
      [ set color green ]
      [ ifelse perfect-market-price <= max-price
        [ set color yellow ]
        [ set color red ]
      ]
  ]
end


;; ---------------------------------------------------------
;; LISTEN-TO-CLIENTS - handle connecting clients
;; ---------------------------------------------------------
to listen-to-clients
  while [ hubnet-message-waiting? ]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ create-new-seller hubnet-message-source ]
    [
      ifelse hubnet-exit-message?
      [ remove-seller hubnet-message-source ]
      [ execute-command hubnet-message-tag ]
    ]
  ]
end


;; ---------------------------------------------------------
;; EXECUTE-COMMAND - execute the command received
;; ---------------------------------------------------------
to execute-command [command]
    ifelse command = "Strategy"
    [ ask sellers with [user-id = hubnet-message-source]
        [ set strategy hubnet-message ]
          ;set p1-quantity-offered hubnet-message
          ;set p1-quantity-available p1-quantity-offered]
    ]
    [ ifelse command = "extra-output"
      [ ask sellers with [user-id = hubnet-message-source]
          [ set extra-output hubnet-message ]
            ;set p2-quantity-offered hubnet-message
            ;set p2-quantity-available p2-quantity-offered]
      ]
      [ if command = "reduced-output"
        [ ask sellers with [user-id = hubnet-message-source]
            [ set reduced-output hubnet-message ]
        ]
      ]
    ]
end

;; ---------------------------------------------------------
;; CREATE-NEW-SELLER - create a new seller when a client
;;                     joins
;; ---------------------------------------------------------
to create-new-seller [ id ]
  let max-jumps 10000
  let color-name ""
  let shape-name ""

  create-sellers 1
  [
    set user-id id     ;; remember which client this is
    ifelse shape-1?
      [ set shape "plant1"
        set color item 1 item 0 color-list-1
        set color-name item 0 item 0 color-list-1
        set shape-name "Circle"
        set color-list-1 but-first color-list-1
      ]
      [ set shape "plant2"
        set color item 1 item 0 color-list-2
        set color-name item 0 item 0 color-list-2
        set shape-name "Square"
        set color-list-2 but-first color-list-2
      ]

    set shape-1? not shape-1?
    set size 3

    ;; locate seller
    setxy random-xcor random-ycor
    while [ any? sellers in-radius 3 and max-jumps > 0 ]
          [ rt random 360
            jump random 100
            set max-jumps (max-jumps - 1) ]

    init-seller-vars
    set heading 0

    ;; send color to client
    hubnet-send user-id "You Are:" (word "The " color-name " " shape-name)

    ;; Announce the arrival of a new seller
    send-news-item (word "A new seller '" user-id "' has arrived.")

  ]
end

;; ---------------------------------------------------------
;; INIT-SELLER-VARS - initialize the seller's variables
;; ---------------------------------------------------------
to init-seller-vars    ;; seller procedure

  ask sellers [
    set last-sale-profit 0
    set p1-quantity-sold 0
    set p2-quantity-sold 0
    set price-1 monopoly-price
    set price-2 price-1
    set marginal-cost 500
    set p1-quantity-offered monopoly-quantity
    set p1-quantity-available p1-quantity-offered
    set p2-quantity-available p2-quantity-offered
    set prev-p2-quantity-sold 0

    ;; initialize profit needed by each seller to an equal share of the monopoly profit
    if num-sellers != 0 [ set profit-needed (monopoly-profit / num-sellers) ]
  ]

  ;; make some sellers require more than an equal share of the monopoly profit, some less
  if num-sellers != 0 [
    ask n-of round (num-sellers / 4) sellers [
      set profit-needed (1.25 * profit-needed)
    ]
    ask n-of round (num-sellers / 4) sellers with [ profit-needed = (monopoly-profit / num-sellers )] [
      set profit-needed (0.25 * profit-needed)
    ]

  ]

end

;; ---------------------------------------------------------
;; REMOVE-SELLER - remove a seller from the simulation
;; ---------------------------------------------------------
to remove-seller [ id ]
  ask sellers with [ user-id = id ] [ die ]
end

;; ---------------------------------------------------------
;; INIT-BUYER-VARS - initialize the buyer's variables
;; ---------------------------------------------------------
to init-buyer-vars
  ask buyers [
    set quantity-demanded ideal-quantity
    set quantity-bought 0
    set already-chosen? false
  ]


  ;; Vary the maximum amount a buyer is willing to pay for oil across buyers
  ;; ( Note:  The distribution below results in a aggregate relationship between
  ;; price and quantity that is linear and has a negative slope.  This simple aggregate
  ;; relationship comes in handy when calculating the cartel agreement and the perfect
  ;; competition amounts. )
  let i 0
  let buyers-remaining buyers with [ not already-chosen? ]
  while [ any? buyers-remaining ] [
    ask one-of buyers-remaining [
      set max-price (ideal-quantity * i)
      set already-chosen? true
    ]
    set i (i + 1)
    set buyers-remaining buyers with [ not already-chosen? ]
   ]
end


;; ---------------------------------------------------------
;; FIND-LOWEST-OFFER -
;; ---------------------------------------------------------
to find-lowest-offer
  let sellers-p2-avail (sellers with [p2-quantity-available > 0])

  ifelse any? sellers-p2-avail
   [ set min-price min ([price-2] of sellers-p2-avail)  ]
   [ set min-price monopoly-price ]

  ;; identify the seller(s) offering the lowest price
  set low-sellers sellers with [ (price-1 = min-price and p1-quantity-available > 0) or
                                 (price-2 = min-price and p2-quantity-available > 0) ]

end

;; ---------------------------------------------------------
;; EXECUTE-TRANSACTIONS -
;; ---------------------------------------------------------
to execute-transactions

   ;; before executing transactions, ask sellers to record how much they sold last time
    ;; and to initialize their quantities this time period
    ask sellers [
      set prev-p2-quantity-sold p2-quantity-sold
      set p1-quantity-sold 0
      set p2-quantity-sold 0
    ]

    ask buyers [
    set quantity-bought 0  ;; initialize quantity-bought this time period

    ifelse min-price <= max-price
      [ set quantity-demanded ideal-quantity ]
      [ set quantity-demanded 0 ]

    ;; try to buy amount you demand from sellers
    buy-from-sellers

    ;; if you bought oil this round, move close to your seller
    ifelse quantity-bought > 0
    [ setxy ([one-of (list (xcor + random-float 1.5) (xcor - random-float 1.5))]
          of seller-bought-from)
      ([one-of (list (ycor + random-float 1.5) (ycor - random-float 1.5))]
          of seller-bought-from)
    ]
      [ setxy random-pxcor random-pycor ]
   ]
end

to buy-from-sellers
     let amt-just-sold 0
     let lucky-one one-of low-sellers ;; if more than one seller offers lowest price, pick one randomly
     let avail-from-seller 0

     ;; figure out the capacity available from the low seller at the min price
     ;; but need to check to see if official price or the side price is lower first
      ifelse [price-1] of lucky-one = min-price
        [ set avail-from-seller [p1-quantity-available] of lucky-one ]
        [ ifelse [price-2] of lucky-one = min-price
          [ set avail-from-seller [p2-quantity-available] of lucky-one ]
          [ set avail-from-seller 0 ]
        ]

     ;; if the current low seller has enough capacity, buy all you need
     ;; otherwise, buy what you can from him
     ifelse avail-from-seller >= quantity-demanded
       [ set quantity-bought (quantity-bought + quantity-demanded) ]
       [ set quantity-bought (quantity-bought + avail-from-seller) ]

     ;; update info of seller you just bought from
     set amt-just-sold quantity-bought
     ask lucky-one [
      ifelse [price-1] of lucky-one = min-price
         [ set p1-quantity-available (p1-quantity-available - amt-just-sold)  ;; decrement seller's remaining amt available at p1
           set p1-quantity-sold (p1-quantity-sold + amt-just-sold)   ;; increment seller's amt purchased by buyers at p1
         ]
         [ if [price-2] of lucky-one = min-price
           [ set p2-quantity-available (p2-quantity-available - amt-just-sold)  ;; decrement seller's remaining amt available at p2
             set p2-quantity-sold (p2-quantity-sold + amt-just-sold)   ;; increment seller's purchased by buyers at p2
           ]
         ]
     ]

   ;; update your own info
   set quantity-demanded (quantity-demanded - quantity-bought)
   set seller-bought-from lucky-one

   ;; if your demand is still not satisfied, try the next seller (if any)
   if quantity-demanded > 0
   [ ifelse any? sellers with [p1-quantity-available > 0 or p2-quantity-available > 0]
         [ find-lowest-offer                   ;; of the sellers with capacity, find the lowest priced ones
           buy-from-sellers                    ;; try to buy from them
         ]
         [stop]  ;; you've tried all the sellers
   ]

end

;; ---------------------------------------------------------
;; UPDATE-CLIENT-MONITORS - update the info in the clients' displays
;; ---------------------------------------------------------
to update-client-monitors

  ;; Send current cartel agreement and market data to all clients
  hubnet-broadcast "Official Price" monopoly-price
  hubnet-broadcast "My Quota" precision individual-quota 0
  hubnet-broadcast "# Suppliers" num-sellers

  ;; Send individual seller data
  ask sellers [
    hubnet-send user-id "Current Profit" precision last-sale-profit 0
    hubnet-send user-id "Marginal Cost" marginal-cost
    hubnet-send user-id "Profit Needed" precision profit-needed 0
    hubnet-send user-id "Qty Sold at Official Price" precision p1-quantity-sold 0
    hubnet-send user-id "Extra Qty Sold" precision p2-quantity-sold 0
    hubnet-send user-id "Actual Qty" precision (p1-quantity-sold + p2-quantity-sold) 0
    ifelse p2-quantity-offered != 0
      [ hubnet-send user-id "Price of Extra Qty" precision price-2 0 ]
      [ hubnet-send user-id "Price of Extra Qty" 0 ]

 ]

  ;; Clear old news items
  if age-of-news > 15 [ send-news-item " " ]
  set age-of-news age-of-news + 1
end

;; ---------------------------------------------------------
;; UPDATE-CARTEL-AGREEMENT - set ideal quantity for cartel
;; ---------------------------------------------------------
to update-cartel-agreement

  ;; Find profit-maximizing quantity assuming cartel behaves as a unitary monopolist.
  ;; Economic theory prescribes that to maximize profits a firm should produce up to the point where
  ;; Marginal Revenue (1st derivative of demand a firm faces) equals Marginal Cost (1st derivative of total cost).
  ;; The eqn below comes from setting MR = MC and solving for monopoly-quantity.
  set monopoly-quantity filter-zero-or-greater (num-buyers * ideal-quantity - marginal-cost) / 2
  set monopoly-price filter-zero-or-greater (num-buyers * ideal-quantity - monopoly-quantity)

  if num-sellers != 0 [ set individual-quota int (monopoly-quantity / num-sellers) ]
end



;; ---------------------------------------------------------
;; UTILITY PROCEDURES - useful stuff
;; ---------------------------------------------------------

to-report filter-zero-or-greater [ value ]
  ifelse (value >= 0)
    [ report value ]
    [ report 0 ]
end

to send-news-item [ msg-text ]
  hubnet-broadcast "Market News" msg-text
  set age-of-news 0
end


;; ---------------------------------------------------------
;; PLOTTING PROCEDURES
;; ---------------------------------------------------------
to plot-oil-amounts
  set-current-plot "Oil Sold On Market"

  set-current-plot-pen "Agreement"
  plot monopoly-quantity

  set-current-plot-pen "Competitive"
  plot perfect-market-quantity

  set-current-plot-pen "Actual"
  plot total-supply
end

to plot-oil-price
  set-current-plot "Average Price of Oil"

  set-current-plot-pen "Average"
  plot avg-price

  set-current-plot-pen "MC"
  plot marginal-cost

  set-current-plot-pen "Agreement"
  plot monopoly-price

end

to plot-current-profit
  set-current-plot "Current Profit Plot"

  ask sellers [
    ;;__hubnet-plot user-id last-sale-profit
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
497
15
907
446
12
12
16.0
1
10
1
1
1
0
1
1
1
-12
12
-12
12
1
1
1
ticks

MONITOR
15
231
78
276
# Sellers
num-sellers
0
1
11

TEXTBOX
10
113
178
131
Buyer Information:
11
0.0
0

BUTTON
9
30
106
63
initial-login
initial-login
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
8
169
180
202
ideal-quantity
ideal-quantity
0
100
30
10
1
NIL
HORIZONTAL

BUTTON
9
67
183
100
run-market
run-market
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
202
15
476
220
Oil Sold on Market
Time
Quantity
0.0
200.0
0.0
1000.0
true
true
"" ""
PENS
"Actual" 1.0 0 -13345367 true "" ""
"Competitive" 1.0 0 -10899396 true "" ""
"Agreement" 1.0 0 -2674135 true "" ""

BUTTON
109
30
182
63
reset
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

TEXTBOX
8
10
98
28
Activity:
11
0.0
0

TEXTBOX
17
293
196
311
Current Cartel Agreement:
11
0.0
0

PLOT
201
226
476
412
Average Price of Oil
NIL
NIL
0.0
200.0
0.0
100.0
true
true
"" ""
PENS
"Average" 1.0 0 -13345367 true "" ""
"MC" 1.0 0 -10899396 true "" ""
"Agreement" 1.0 0 -2674135 true "" ""

MONITOR
16
367
166
412
Target Total Barrels
monopoly-quantity
3
1
11

MONITOR
15
316
167
361
Official Price
monopoly-price
3
1
11

SLIDER
8
132
180
165
num-buyers
num-buyers
30
300
200
10
1
NIL
HORIZONTAL

PLOT
200
419
476
539
Current Profit Plot
NIL
NIL
0.0
500.0
0.0
2.0E7
true
false
"" ""
PENS
"default" 1.0 0 -13345367 true "" ""

BUTTON
16
419
166
452
Update Agreement
update-cartel-agreement
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

TEXTBOX
14
212
164
230
Cartel Information:
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

This is a collaborative exploration of the economics of a market with imperfect competition. As members of a cartel, participants experience how jointly determined price and quantity decisions can be advantageous to suppliers, harmful to consumers, but also why a cartel is so difficult to sustain. In this version of Oil Cartel, cartel members face differing profit expectations, and set production and pricing strategies in an attempt to meet those expectations.  They respond to each other's behavior by altering their strategies.

## HOW IT WORKS

The demand for oil is determined by the number of buyers (NUM-BUYERS) and how much oil they each desire (IDEAL-QUANTITY).  A buyer follows two rules.  One, they always buy from the seller offering the minimum price.  Two, they only buy if the minimum price available does not exceed their maximum willingness to pay (which varies across the buyers).

Each HubNet Client is a member of the cartel.  The cartel currently has an agreement in place to limit overall production, resulting in one common official price, and a quota for each member. Each member of this cartel independently decides whether to abide by the agreement or to "cheat" on the agreement and try to boost profits by producing and selling beyond their quota. Furthermore, cartel members face different revenue demands ("Profit-Needed") from their home government, as they come from countries of differing levels of economic prosperity.  More specifically, each cartel member can choose from the following production and pricing strategies, which they can alter depending on the response of buyers and other cartel members:

1. "Agreement" Strategy:  Always produce and price exactly in accordance with the cartel agreement

2. "Quota-Plus" Strategy: Produce a little bit more than the quota and offer it to the excess amount to the market for a price a little less than the agreement price.  (The additional amount produced is set by the "extra-output" slider. )

3. "Quota-Minus" Strategy:  Produce a little bit less than the quota and offer it to the market at the agreement price. (The amount less is set by the "reduced-output" slider.) A member might do this in an effort to keep prices up and the cartel together.
4. "Price > MC" Strategy: Keep producing and offering additional output as long
as the price you have to offer to the buyers in order to sell that unit is still higher than the cost to produce it.

5.  "Flood Market" Strategy:  Saturate the market with low cost oil to punish or send a warning to cheaters.

## HOW TO USE IT

Set the number of buyers  (NUM-BUYERS) and amount of oil each buyer demands (IDEAL-QUANTITY).  Press the INITIAL-LOGIN forever button and leave pressed until all HubNet Client log in. Ask clients to set their initial strategies, paying attention to the the current agreement, current profit, as well as their own profit needed.  Once all clients have selected their initial strategies, press the RUN-MARKET button.  The overall behavior of the market can be witness on the View and Plots on the server, which are described below.  Each individual client's information is displayed on their own monitors, including a plot of their current profit.

Buttons:

INITIAL-LOGIN - Allows sellers to log in to the activity. This forever button should be turned off before beginning the simulation.

RUN-MARKET - Runs the simulation. Clients will only be selling oil while the market simulation is running. Turning off this forever button will stop all market activity, although it can be resumed from the same point by clicking the button again.

RESET - Resets the simulation to its initial state.

UPDATE-AGREEMENT - Updates the current cartel agreement based on current buyer demand.  Only useful if IDEAL-QUANTITY changes, but the simulation is not reset.

Sliders:

NUM-BUYERS - Total number of buyers in the market.  Changing this value requires a reset.  It cannot be changed during the simulation.

IDEAL-QUANTITY -  The amount of oil demanded by each buyer per time period.

Monitors:

"# Sellers" - The number of sellers participating in the activity.

"Target Total Barrels" - This is the amount of total production agreed upon by the cartel. It is the total of all oil produced by suppliers that would maximize profits if the cartel behaved as a unitary monopolist, given the current buyer demand and supplier marginal cost. This quantity divided by the number of sellers is each sellers quota.

"Official Price" - This is the price of the current cartel agreement. It is the price of oil that corresponds with supplying "Target Total Barrels" given the current buyer demand and supplier marginal cost.

Plots:

"Oil Sold at Market" - Plots the quantities of oil sold at market over time. Also on the graph are the amounts that would be supplied under perfect competition and under a monopoly. Anything less than the amount of perfect competiton and above or equal to the monopoly line is an improvement is better for the cartel than a situation with no collusion.

"Average Price of Oil" - Plots the average price of oil across all transactions in the current time period.  Also on the graph are plots of Official Price of the cartel agreement and the marginal cost of suppliers.  Anything above marginal cost and equal to or below the agreement line is better for the cartel than a situation with no collusion.

View:

The view shows all the sellers and buyers.  The sellers are represented by by a colored circle or square. The buyers are the multitude of green, yellow, and red  agents in the window.  Green agents are those buyers who were able to make a purchase n the time period.  They move to the seller from which they last purchased oil. Yellow agents are buyers who did not make a purchase, but they would have if it were priced at the level of a perfectly competitive market.  They represent the inefficiency caused by the cartel behavior.  Red agents are buyers who did not purchase oil, nor would they even in a perfectly competitive market.  Price would have to be even more favorable than the perfectly competitive price for these buyers to purchase.

Client Information:

"You Are:" - Identifies you on the View (e.g., The White Circle)

"Market News" - Information of note.

"Marginal Cost" - The cost of producing one more unit.

"# Suppliers" - The number of sellers who are currently in the market.

"Official Price" - Price from the current cartel agreement.

"Qty Sold at Official Price" -- Amount of oil sold at the agreement price.

"Price of Extra Qty" - The price at which you offer the extra oil you produce if you choose not to abide by the agreement.  It is determined by the strategy selected.

"Extra Qty Sold" - Amount of oil purchased by buyers at the "Price of Extra Qty"

"My Quota" - Individual member quota based on cartel agreement.

"Actual Qty" - The total amount sold regardless of price.

"Current Profit" - The profit made by the seller last time oil was sold at market.

"Profit-Needed" - The profit expected from you by your home country.

"Current Profit Plot" - A plot over time of the cartel members profit.

"Strategy" - Drop box used to select one of the strategies mentioned above.

"extra-output" -  Slider used to set the extra amount produced if using the "Quota-Plus" strategy.

"reduced-output - Slider used to set the amount production is reduced from quota if using the "Quota-Minus" strategy.

## THINGS TO NOTICE

The cartel can usually withstand a little bit of cheating, even if all the members indulge. If all members pursue the "P > MC" strategy, the market quickly reaches the perfect competition equilibrium price and quantity.  Even if only one member becomes too aggressive, the market price of oil will drop, and the cartel will most likely crumble.

## THINGS TO TRY

Try changing the values of NUM-BUYERS and IDEAL-QUANTITY? Does cheating by cartel members become more or less noticable?

Can you reach an equilibrium point where everyone is exceeded their Profit Needed?

The "Quota-Minus" strategy seems like an unattractive one.  Can you find a situation where it is a strategy ever actually worth pursuing?

## EXTENDING THE MODEL

Here are some suggestions for ways to extend the model:

- Create new strategies!  Current strategies are only a fraction of the possible ways a cartel member might behave over time.

- Give cartel members the capacity to build up reserves which they can then use strategically. Reserves could be a function of a producers maximum capacity, and would limit a sellers ability to "flood the market."

## NETLOGO FEATURES

Client specific plotting (experimental).

## RELATED MODELS

Tragedy of the Commons, Gridlock, Prisoner's Dilemma HubNet

## CREDITS AND REFERENCES

For an introduction to firm behavior in imperfectly competitive markets see:
Nicholson, Walter. Intermediate Microeconomics and Its Application.  Thomson Learning, 2004.

For an in-depth discussion of models of oil cartel behavior see:
Scherer, F.M. Industry Structure, Strategy, and Public Policy. Chapter 3. Addison-Wesley, 1996.
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

ant
true
0
Polygon -7500403 true true 136 61 129 46 144 30 119 45 124 60 114 82 97 37 132 10 93 36 111 84 127 105 172 105 189 84 208 35 171 11 202 35 204 37 186 82 177 60 180 44 159 32 170 44 165 60
Polygon -7500403 true true 150 95 135 103 139 117 125 149 137 180 135 196 150 204 166 195 161 180 174 150 158 116 164 102
Polygon -7500403 true true 149 186 128 197 114 232 134 270 149 282 166 270 185 232 171 195 149 186
Polygon -7500403 true true 225 66 230 107 159 122 161 127 234 111 236 106
Polygon -7500403 true true 78 58 99 116 139 123 137 128 95 119
Polygon -7500403 true true 48 103 90 147 129 147 130 151 86 151
Polygon -7500403 true true 65 224 92 171 134 160 135 164 95 175
Polygon -7500403 true true 235 222 210 170 163 162 161 166 208 174
Polygon -7500403 true true 249 107 211 147 168 147 168 150 213 150

arrow
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

bee
true
0
Polygon -1184463 true false 152 149 77 163 67 195 67 211 74 234 85 252 100 264 116 276 134 286 151 300 167 285 182 278 206 260 220 242 226 218 226 195 222 166
Polygon -16777216 true false 150 149 128 151 114 151 98 145 80 122 80 103 81 83 95 67 117 58 141 54 151 53 177 55 195 66 207 82 211 94 211 116 204 139 189 149 171 152
Polygon -7500403 true true 151 54 119 59 96 60 81 50 78 39 87 25 103 18 115 23 121 13 150 1 180 14 189 23 197 17 210 19 222 30 222 44 212 57 192 58
Polygon -16777216 true false 70 185 74 171 223 172 224 186
Polygon -16777216 true false 67 211 71 226 224 226 225 211 67 211
Polygon -16777216 true false 91 257 106 269 195 269 211 255
Line -1 false 144 100 70 87
Line -1 false 70 87 45 87
Line -1 false 45 86 26 97
Line -1 false 26 96 22 115
Line -1 false 22 115 25 130
Line -1 false 26 131 37 141
Line -1 false 37 141 55 144
Line -1 false 55 143 143 101
Line -1 false 141 100 227 138
Line -1 false 227 138 241 137
Line -1 false 241 137 249 129
Line -1 false 249 129 254 110
Line -1 false 253 108 248 97
Line -1 false 249 95 235 82
Line -1 false 235 82 144 100

bird1
false
0
Polygon -7500403 true true 2 6 2 39 270 298 297 298 299 271 187 160 279 75 276 22 100 67 31 0

bird2
false
0
Polygon -7500403 true true 2 4 33 4 298 270 298 298 272 298 155 184 117 289 61 295 61 105 0 43

boat1
false
0
Polygon -1 true false 63 162 90 207 223 207 290 162
Rectangle -6459832 true false 150 32 157 162
Polygon -13345367 true false 150 34 131 49 145 47 147 48 149 49
Polygon -7500403 true true 158 33 230 157 182 150 169 151 157 156
Polygon -7500403 true true 149 55 88 143 103 139 111 136 117 139 126 145 130 147 139 147 146 146 149 55

boat2
false
0
Polygon -1 true false 63 162 90 207 223 207 290 162
Rectangle -6459832 true false 150 32 157 162
Polygon -13345367 true false 150 34 131 49 145 47 147 48 149 49
Polygon -7500403 true true 157 54 175 79 174 96 185 102 178 112 194 124 196 131 190 139 192 146 211 151 216 154 157 154
Polygon -7500403 true true 150 74 146 91 139 99 143 114 141 123 137 126 131 129 132 139 142 136 126 142 119 147 148 147

boat3
false
0
Polygon -1 true false 63 162 90 207 223 207 290 162
Rectangle -6459832 true false 150 32 157 162
Polygon -13345367 true false 150 34 131 49 145 47 147 48 149 49
Polygon -7500403 true true 158 37 172 45 188 59 202 79 217 109 220 130 218 147 204 156 158 156 161 142 170 123 170 102 169 88 165 62
Polygon -7500403 true true 149 66 142 78 139 96 141 111 146 139 148 147 110 147 113 131 118 106 126 71

box
true
0
Polygon -7500403 true true 45 255 255 255 255 45 45 45

butterfly1
true
0
Polygon -16777216 true false 151 76 138 91 138 284 150 296 162 286 162 91
Polygon -7500403 true true 164 106 184 79 205 61 236 48 259 53 279 86 287 119 289 158 278 177 256 182 164 181
Polygon -7500403 true true 136 110 119 82 110 71 85 61 59 48 36 56 17 88 6 115 2 147 15 178 134 178
Polygon -7500403 true true 46 181 28 227 50 255 77 273 112 283 135 274 135 180
Polygon -7500403 true true 165 185 254 184 272 224 255 251 236 267 191 283 164 276
Line -7500403 true 167 47 159 82
Line -7500403 true 136 47 145 81
Circle -7500403 true true 165 45 8
Circle -7500403 true true 134 45 6
Circle -7500403 true true 133 44 7
Circle -7500403 true true 133 43 8

circle
false
0
Circle -7500403 true true 35 35 230

person
false
0
Circle -7500403 true true 155 20 63
Rectangle -7500403 true true 158 79 217 164
Polygon -7500403 true true 158 81 110 129 131 143 158 109 165 110
Polygon -7500403 true true 216 83 267 123 248 143 215 107
Polygon -7500403 true true 167 163 145 234 183 234 183 163
Polygon -7500403 true true 195 163 195 233 227 233 206 159

plant1
true
15
Circle -1 true true 2 2 295
Rectangle -7500403 true false 90 135 120 240
Rectangle -7500403 true false 120 75 135 240
Rectangle -7500403 true false 135 105 180 240
Rectangle -7500403 true false 195 120 225 240
Rectangle -7500403 true false 180 45 195 240
Polygon -2674135 true false 105 60 120 75 135 75 105 60
Polygon -2674135 true false 165 30 180 45 195 45 165 30

plant2
true
15
Rectangle -1 true true 15 15 285 285
Rectangle -7500403 true false 90 135 120 240
Rectangle -7500403 true false 120 75 135 240
Rectangle -7500403 true false 135 105 180 240
Rectangle -7500403 true false 195 120 225 240
Rectangle -7500403 true false 180 45 195 240
Polygon -2674135 true false 105 60 120 75 135 75 105 60
Polygon -2674135 true false 165 30 180 45 195 45 165 30

sheep
false
15
Rectangle -1 true true 90 75 270 225
Circle -1 true true 15 75 150
Rectangle -16777216 true false 81 225 134 286
Rectangle -16777216 true false 180 225 238 285
Circle -16777216 true false 1 88 92

spacecraft
true
0
Polygon -7500403 true true 150 0 180 135 255 255 225 240 150 180 75 240 45 255 120 135

thin-arrow
true
0
Polygon -7500403 true true 150 0 0 150 120 150 120 293 180 293 180 150 300 150

truck
true
0
Polygon -7500403 true true 225 30 225 270 120 270 105 210 60 180 45 30 105 60 105 30
Polygon -8630108 true false 195 75 195 120 240 120 240 75
Polygon -8630108 true false 195 225 195 180 240 180 240 225

truck-down
false
0
Polygon -7500403 true true 225 30 225 270 120 270 105 210 60 180 45 30 105 60 105 30
Polygon -8630108 true false 195 75 195 120 240 120 240 75
Polygon -8630108 true false 195 225 195 180 240 180 240 225

truck-left
false
0
Polygon -7500403 true true 120 135 225 135 225 210 75 210 75 165 105 165
Polygon -8630108 true false 90 210 105 225 120 210
Polygon -8630108 true false 180 210 195 225 210 210

truck-right
false
0
Polygon -7500403 true true 180 135 75 135 75 210 225 210 225 165 195 165
Polygon -8630108 true false 210 210 195 225 180 210
Polygon -8630108 true false 120 210 105 225 90 210

turtle
true
0
Polygon -7500403 true true 138 75 162 75 165 105 225 105 225 142 195 135 195 187 225 195 225 225 195 217 195 202 105 202 105 217 75 225 75 195 105 187 105 135 75 142 75 105 135 105

wolf
false
0
Rectangle -7500403 true true 15 105 105 165
Rectangle -7500403 true true 45 90 105 105
Polygon -7500403 true true 60 90 83 44 104 90
Polygon -16777216 true false 67 90 82 59 97 89
Rectangle -1 true false 48 93 59 105
Rectangle -16777216 true false 51 96 55 101
Rectangle -16777216 true false 0 121 15 135
Rectangle -16777216 true false 15 136 60 151
Polygon -1 true false 15 136 23 149 31 136
Polygon -1 true false 30 151 37 136 43 151
Rectangle -7500403 true true 105 120 263 195
Rectangle -7500403 true true 108 195 259 201
Rectangle -7500403 true true 114 201 252 210
Rectangle -7500403 true true 120 210 243 214
Rectangle -7500403 true true 115 114 255 120
Rectangle -7500403 true true 128 108 248 114
Rectangle -7500403 true true 150 105 225 108
Rectangle -7500403 true true 132 214 155 270
Rectangle -7500403 true true 110 260 132 270
Rectangle -7500403 true true 210 214 232 270
Rectangle -7500403 true true 189 260 210 270
Line -7500403 true 263 127 281 155
Line -7500403 true 281 155 281 192

wolf-left
false
3
Polygon -6459832 true true 117 97 91 74 66 74 60 85 36 85 38 92 44 97 62 97 81 117 84 134 92 147 109 152 136 144 174 144 174 103 143 103 134 97
Polygon -6459832 true true 87 80 79 55 76 79
Polygon -6459832 true true 81 75 70 58 73 82
Polygon -6459832 true true 99 131 76 152 76 163 96 182 104 182 109 173 102 167 99 173 87 159 104 140
Polygon -6459832 true true 107 138 107 186 98 190 99 196 112 196 115 190
Polygon -6459832 true true 116 140 114 189 105 137
Rectangle -6459832 true true 109 150 114 192
Rectangle -6459832 true true 111 143 116 191
Polygon -6459832 true true 168 106 184 98 205 98 218 115 218 137 186 164 196 176 195 194 178 195 178 183 188 183 169 164 173 144
Polygon -6459832 true true 207 140 200 163 206 175 207 192 193 189 192 177 198 176 185 150
Polygon -6459832 true true 214 134 203 168 192 148
Polygon -6459832 true true 204 151 203 176 193 148
Polygon -6459832 true true 207 103 221 98 236 101 243 115 243 128 256 142 239 143 233 133 225 115 214 114

wolf-right
false
3
Polygon -6459832 true true 170 127 200 93 231 93 237 103 262 103 261 113 253 119 231 119 215 143 213 160 208 173 189 187 169 190 154 190 126 180 106 171 72 171 73 126 122 126 144 123 159 123
Polygon -6459832 true true 201 99 214 69 215 99
Polygon -6459832 true true 207 98 223 71 220 101
Polygon -6459832 true true 184 172 189 234 203 238 203 246 187 247 180 239 171 180
Polygon -6459832 true true 197 174 204 220 218 224 219 234 201 232 195 225 179 179
Polygon -6459832 true true 78 167 95 187 95 208 79 220 92 234 98 235 100 249 81 246 76 241 61 212 65 195 52 170 45 150 44 128 55 121 69 121 81 135
Polygon -6459832 true true 48 143 58 141
Polygon -6459832 true true 46 136 68 137
Polygon -6459832 true true 45 129 35 142 37 159 53 192 47 210 62 238 80 237
Line -16777216 false 74 237 59 213
Line -16777216 false 59 213 59 212
Line -16777216 false 58 211 67 192
Polygon -6459832 true true 38 138 66 149
Polygon -6459832 true true 46 128 33 120 21 118 11 123 3 138 5 160 13 178 9 192 0 199 20 196 25 179 24 161 25 148 45 140
Polygon -6459832 true true 67 122 96 126 63 144

@#$#@#$#@
NetLogo 5.0beta1
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
MONITOR
20
371
147
420
Current Profit
NIL
0
1

MONITOR
151
144
287
193
# Suppliers
NIL
3
1

MONITOR
15
78
287
127
Market News
NIL
0
1

MONITOR
16
144
147
193
Marginal Cost
NIL
0
1

MONITOR
15
201
147
250
Official Price
NIL
3
1

MONITOR
17
315
147
364
My Quota
NIL
3
1

PLOT
316
10
605
231
Oil Sold on Market
NIL
NIL
0.0
500.0
0.0
6000.0
true
false
"" ""
PENS
"Actual" 1.0 0 -13345367 true
"Competitive" 1.0 0 -10899396 true
"Agreement" 1.0 0 -2674135 true

MONITOR
18
10
286
59
You Are:
NIL
3
1

MONITOR
152
371
288
420
Profit Needed
NIL
0
1

CHOOSER
316
251
608
296
Strategy
Strategy
"Price >= MC" "Agreement" "Quota-Plus" "Quota-Minus" "Flood Market"
0

SLIDER
316
326
607
359
extra-output
extra-output
0
2000
0
10
1
NIL
HORIZONTAL

TEXTBOX
316
305
527
323
Extra Ouput for \"Quota-Plus\" Strategy:
11
0.0
0

TEXTBOX
315
371
562
389
Reduced Output for \"Quota-Minus\" Strategy:
11
0.0
0

SLIDER
316
389
607
422
reduced-output
reduced-output
0
2000
0
10
1
NIL
HORIZONTAL

MONITOR
151
201
287
250
Qty Sold at Official Price
NIL
0
1

MONITOR
151
256
287
305
Extra Qty Sold
NIL
0
1

MONITOR
151
315
287
364
Actual Qty
NIL
0
1

MONITOR
15
257
146
306
Price of Extra Qty
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

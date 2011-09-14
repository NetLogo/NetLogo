globals [
  num-sellers

  ;; market internals
  monopoly-quantity        ;; the amount that would be supplied by a monopoly
  monopoly-price           ;; the price at a monopoly equilibrium
  real-supply              ;; the actual quantity supplied
  current-price            ;; the price last time oil was sold at market
  perfect-market-quantity  ;; the amount that would be supplied under perfect competition
  perfect-market-price     ;; the price at equilibrium in perfect competition
  market-inefficiency      ;; the deadweight loss

  ;; is oil going to market?
  market-running?

  ;; news-related variables (for sending market news to the client)
  age-of-news              ;; age of news, so we can clear old news
  caught-cheating          ;; list of who was caught cheating

  ;; gotta keep track of these to know when to redraw S&D graphs
  last-demand-sensitivity
  last-base-demand
  last-marginal-cost

  ;; quick start instructions variables
  quick-start  ;; current quickstart instruction displayed in the quickstart monitor
  qs-item      ;; index of the current quickstart instruction
  qs-items     ;; list of quickstart instructions
]

breed [ sellers seller ]         ;; every client is a seller
breed [ buyers buyer ]           ;; consumers running around the market

buyers-own [
  supplied?       ;; is the market supplying this buyer?
]

sellers-own [
  user-id                         ;; unique user-id, input by the client when they log in

  amount-to-cheat                 ;; last known value of the client's amount-to-cheat slider
  anti-cheat-investment           ;; last known value of the client's anti-cheat-investment slider

  real-amount                     ;; the actual amount of oil that was sold last time to market

  rank                            ;; rank number according to bank account balance
  received-rank?                  ;; blech, there has got to be a better way to do ranks

  bank-account                    ;; amount of money in the user's bank account
  last-sale-profit                ;; how much money the user made last time oil was sold at market
  penalties-paid                  ;; how much in total penalties has the user paid?
  income-from-penalties           ;; how much total income has the user made from catching cheaters?
]

;; ---------------------------------------------------------
;; STARTUP - auto-setup when the model is opened
;; ---------------------------------------------------------
to startup
  hubnet-reset

  ;; Set up the Quick Start window
  quick-start-reset

  ;; Set up global variables
  init-globals
end

;; ---------------------------------------------------------
;; RE-RUN - reset everything for a new simulation
;; ---------------------------------------------------------
to re-run
  ;; Reset all of our market and seller variables
  ask sellers [ init-seller-vars ]
  init-globals

  ;; Update the client displays and notify them of the reset
  update-clients
  send-news-item "The simulation has been reset!"

  ;; Reset the "amount supplied" plot
  set-current-plot "Oil Sold at Market"
  clear-plot

end

;; ---------------------------------------------------------
;; INIT-GLOBALS - initialize our global variables
;; ---------------------------------------------------------
to init-globals
  set num-sellers 0
  set market-running? false
  set perfect-information? true
  set age-of-news 0
  set caught-cheating []
end


;; ---------------------------------------------------------
;; INITIAL-LOGIN - listen for clients and establish them as
;;                 sellers
;; ---------------------------------------------------------
to initial-login
  set market-running? false

  ;; Listen for logins, etc.
  listen-to-clients

  ;; Recalculate market conditions
  recompute-market-internals

  update-clients
  run-market-buyers

  ;; Keep the supply and demand graph updated
  if market-characteristics-changed? [ plot-supply-and-demand ]
  log-market-characteristics
end


;; ---------------------------------------------------------
;; RUN-MARKET - run the market simulation
;; ---------------------------------------------------------
to run-market
  if (market-running? != true) [ plot-supply-and-demand ]

  ;; Turn off the display if needed
  ifelse (perfect-information? = true)
    [ display ]
    [ no-display ]

  set market-running? true

  ;; Check if clients have moved their sliders
  listen-to-clients

  ;; Calculate market conditions
  recompute-market-internals

  ;; Sell the oil and distribute the profits
  sell-oil-at-market

  ;; The cartel members may investigate cheaters on every turn
  catch-cheaters

  ;; Do a visualization of the market's inefficiencies
  run-market-buyers

  ;; Plot the amount of oil that was sold
  plot-oil-amounts

  ;; Keep the supply and demand graph updated
  if market-characteristics-changed? [ plot-supply-and-demand ]
  log-market-characteristics

  ;; Update seller ranks
  rank-sellers

  ;; Update client monitors, etc.
  update-clients

end


;; ---------------------------------------------------------
;; RECOMPUTE-MARKET-INTERNALS - recompute the market
;;                              using the latest info...
;;                              no oil is actually sold
;; ---------------------------------------------------------
to recompute-market-internals

  ;; Check the number of sellers
  set num-sellers count sellers

  ;; Determine the profit-maximizing total quantity the cartel should produce if it were
  ;; behaving as a unitary monopolist.  Economic theory predicts that is will be were MR = MC
  ;; Solving for quantity results in the equations below
  set monopoly-quantity filter-zero-or-greater (round ((base-demand - marginal-cost) / (2 * demand-sensitivity)))
  set monopoly-price filter-zero-or-greater (base-demand - (demand-sensitivity * monopoly-quantity))

  ;; The actual amount supplied, including cheating
  ifelse num-sellers > 0 and market-running? = true
    [ set real-supply monopoly-quantity + sum [amount-to-cheat] of sellers]
    [ set real-supply 0 ]

  ;; The current price
  set current-price filter-zero-or-greater (base-demand - (demand-sensitivity * real-supply))

  ;; Determine what the total quantity would be if this were a perfectly competitive market
  ;; Economic theory predicts that price = mc.  Solving for quantity results in the equations below
  set perfect-market-quantity filter-zero-or-greater (round ((base-demand - marginal-cost) / demand-sensitivity))
  set perfect-market-price (base-demand - (demand-sensitivity * perfect-market-quantity))

  ;; Calculate the market's inefficiency, the "deadweight loss"
  set market-inefficiency (0.5 * (perfect-market-quantity - real-supply) * (current-price - perfect-market-price))
end


;; ---------------------------------------------------------
;; SELL OIL AT MARKET - complete the sales transactions
;; ---------------------------------------------------------
to sell-oil-at-market
  let base-amount-to-sell 0
  ifelse (num-sellers > 0)
    [ set base-amount-to-sell (monopoly-quantity / num-sellers)]
    [ set base-amount-to-sell 0 ]

  ask sellers [
    ;; Figure out how much to really sell
    set real-amount base-amount-to-sell + amount-to-cheat
    set last-sale-profit (real-amount * current-price) - (real-amount * marginal-cost)
    set bank-account bank-account + last-sale-profit
  ]

end


;; ---------------------------------------------------------
;; CATCH-CHEATERS - cartel members may investigate whether
;;                  anybody else is cheating
;; ---------------------------------------------------------
to catch-cheaters
  ;; Only those sellers with an anti-cheat investment may investigate
  ask sellers with [ anti-cheat-investment > 0 ] [

    ;; Charge the seller for the anti-cheat investment -- percentage of gross revenues
    let investigation-cost ((anti-cheat-investment / 100) * real-amount * current-price)
    set last-sale-profit last-sale-profit - investigation-cost
    set bank-account bank-account - investigation-cost

    ;; Conduct an investigation of potential cheaters (other cartel members)
    ask other sellers with [ amount-to-cheat > 0] [

      ;; Chance of getting caught is directly related to how much was invested in catching the cheater
      if (random 100) <= ([anti-cheat-investment] of myself) [

        ;; Caught! Calculate the penalty
        let penalty penalty-severity * amount-to-cheat * current-price

        ;; Remember who was caught for the news
        set caught-cheating lput user-id caught-cheating

        ;; Take the penalty from the cheater...
        set last-sale-profit last-sale-profit - penalty
        set bank-account bank-account - penalty
        set penalties-paid penalties-paid + penalty

        ;; And give it to the investigator.
        ask myself
        [
          set last-sale-profit last-sale-profit + [penalty] of myself
          set bank-account bank-account + [penalty] of myself
          set income-from-penalties income-from-penalties + [penalty] of myself
        ]
      ]
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
    ifelse command = "amount-to-cheat"
    [
      ask sellers with [user-id = hubnet-message-source]
        [ set amount-to-cheat hubnet-message ]
    ]
    [if command = "anti-cheat-investment"
    [
      ask sellers with [user-id = hubnet-message-source]
        [ set anti-cheat-investment hubnet-message ]
    ]]
end


;; ---------------------------------------------------------
;; CREATE-NEW-SELLER - create a new seller when a client
;;                     joins
;; ---------------------------------------------------------
to create-new-seller [ id ]
  create-sellers 1
  [
    set user-id id     ;; remember which client this is
    ht
    init-seller-vars

    ;; Replot supply-and-demand so it gets mirrored
    ;; plot-supply-and-demand      ;; too slow w/mirroring, commented out for now

    ;; Announce the arrival of a new seller
    send-news-item (word "A new seller '" user-id "' has arrived.")
  ]
end


;; ---------------------------------------------------------
;; INIT-SELLER-VARS - initialize the seller's variables
;; ---------------------------------------------------------
to init-seller-vars    ;; seller procedure
    set bank-account 0
    set bank-account 0
    set received-rank? false
    set rank "N/A"
    set last-sale-profit 0
    set income-from-penalties 0
    set penalties-paid 0
end


;; ---------------------------------------------------------
;; UPDATE-CLIENTS - update the info in the clients' displays
;; ---------------------------------------------------------
to update-clients

  ;; Broadcast some market data
  hubnet-broadcast "Price of Oil" precision current-price 2
  if not empty? caught-cheating [ send-news-item word "Caught cheating: " caught-cheating ]

  ;; Send individual data
  ask sellers [
    hubnet-send user-id "# Suppliers" precision num-sellers 0
    hubnet-send user-id "Bank Account" precision (bank-account / 1000000) 3
    hubnet-send user-id "Current Profit" precision last-sale-profit 4
    hubnet-send user-id "Penalties Paid" precision (penalties-paid / 1000000) 3
    hubnet-send user-id "Income From Penalties" precision (income-from-penalties / 1000000) 3
    hubnet-send user-id "Rank" rank
    hubnet-send user-id "Severity of Penalties" penalty-severity
  ]

  ;; Clear old news items
  if age-of-news > 15 [ send-news-item " " ]
  set age-of-news age-of-news + 1
  set caught-cheating []
end

;; ---------------------------------------------------------
;; REMOVE-SELLER - remove a seller from the simulation
;; ---------------------------------------------------------
to remove-seller [ id ]
  ask sellers with [ user-id = id ] [ die ]
end


;; ---------------------------------------------------------
;; RUN-MARKET-BUYERS - use the graphics display as a visual
;;                      representation of the market's
;;                      inefficiency
;; ---------------------------------------------------------
to run-market-buyers

  ;; How many buyers should we draw?
  let num-buyers-to-draw max list perfect-market-quantity real-supply

  ;; How many buyers are unable to buy wanted supplies?
  let num-unsupplied-buyers max list (perfect-market-quantity - real-supply) 0

  ;; Adjust our buyer quantities
  ifelse (count buyers) < num-buyers-to-draw [  ;; need more buyers
    create-buyers (num-buyers-to-draw - (count buyers)) [
      ;; set shape "truck"        ;; this shape is no good...
      fd random (max-pxcor * 2)
      set color green
    ]
  ][  ;; need fewer buyers
    ask n-of ((count buyers) - num-buyers-to-draw) buyers [ die ]
  ]

  ;; If oversupplied, change all undersupplied buyers green
  if real-supply > perfect-market-quantity [ ask buyers with [ color = red or color = yellow ] [ set color green ]]

  ;; If undersupplied, change all oversupplied buyers green
  if real-supply < perfect-market-quantity [ask buyers with [ color = magenta ] [ set color green ]]

  ;; Adjust our buyer colors
  adjust-buyer-color-population red green num-unsupplied-buyers
  adjust-buyer-color-population magenta green (filter-zero-or-greater (real-supply - perfect-market-quantity))

  ;; Move the buyers around
  ask buyers with [color = green or color = yellow or color = magenta] [
    ;; Don't go forward if there's a red buyer there
    ifelse count (buyers-at dx dy) with [color = red] > 0 [
      rt random 10       ;; better try to turn around...
      set color yellow   ;; indicate that we're being inconvenienced by under-supplied buyers
    ][
      fd 1
      if color != magenta [ set color green ]
    ]
  ]
end

;; ---------------------------------------------------------
;; UTILITY PROCEDURES - useful stuff
;; ---------------------------------------------------------
to-report buyers-in-the-way? [ range ]  ;; buyer procedure
  let counter 0

  while [ counter < range ] [
    set counter (counter + 1)

    ;; Check if there's a red buyer there
    if count ((buyers-at (xcor + (counter * dx)) (ycor + (counter * dy))) with [color = red]) > 0 [
      report true
    ]
  ]
  report false
end

to-report filter-zero-or-greater [ value ]
  ifelse (value >= 0)
    [ report value ]
    [ report 0 ]
end

to send-news-item [ msg-text ]
  hubnet-broadcast "Market News" msg-text
  set age-of-news 0
end

to-report market-characteristics-changed?
  report (demand-sensitivity != last-demand-sensitivity) or
         (base-demand != last-base-demand) or
         (marginal-cost != last-marginal-cost)
end

to log-market-characteristics
  set last-demand-sensitivity demand-sensitivity
  set last-base-demand base-demand
  set last-marginal-cost marginal-cost
end

;; Adjust the buyer population's colors so that there is the specified number of the
;; desired color, with those that have been pushed out set to the neutral color
to adjust-buyer-color-population [ color-desired neutral-color num-desired ]
  let original-num count buyers with [ color = color-desired ]

  if num-desired = original-num [ stop ]

  ifelse (num-desired < original-num) [  ;; too many "color-desired" buyers
    repeat (original-num - num-desired) [
      ask one-of (buyers with [color = color-desired]) [ set color neutral-color ]
    ]
  ][  ;; not enough "color-desired" buyers
    repeat (num-desired - original-num) [
      ask one-of (buyers with [color != color-desired]) [ set color color-desired ]
    ]
  ]
end

;; Rank who's winning--code borrowed from the TOTC
to rank-sellers
  let num-ranks (length (remove-duplicates ([bank-account] of sellers)))
  let rank# count sellers
  repeat num-ranks
  [
    let min-rev min [bank-account] of sellers with [not received-rank?]
    let rankee sellers with [bank-account = min-rev]
    let num-tied count rankee
    ask rankee
    [
      set rank rank#
      set received-rank? true
    ]
    set rank# rank# - num-tied
  ]
  ask sellers
    [set received-rank? false]
end

;; ---------------------------------------------------------
;; PLOTTING PROCEDURES
;; ---------------------------------------------------------
to plot-oil-amounts
  set-current-plot "Oil Sold At Market"

  set-current-plot-pen "Monopoly"
  plot monopoly-quantity

  set-current-plot-pen "Competitive"
  plot perfect-market-quantity

  set-current-plot-pen "Real"
  plot real-supply
end

to plot-supply-and-demand
  ;; Clients don't get to see this if perfect information is off
  if perfect-information? = false [
    stop
  ]

  set-current-plot "Supply and Demand"
  clear-plot

  ;; Plot until demand crosses the x-axis (until demand's price is 0)
  let quantity 0
  while [ (base-demand - (demand-sensitivity * quantity)) >= 0 ] [

    ;; Plot demand
    set-current-plot-pen "Demand"
    plot (base-demand - (demand-sensitivity * quantity))

    ;; Plot marginal revenue
    set-current-plot-pen "Marginal Revenue"
    if base-demand - (2 * demand-sensitivity * quantity) >= 0 [
      plot (base-demand - (2 * demand-sensitivity * quantity))
    ]

    ;; Plot marginal cost
    set-current-plot-pen "Marginal Cost"
    plot marginal-cost

    set quantity (quantity + 1)
  ]

  set-plot-x-range 0 quantity
end

;; ---------------------------------------------------------
;; QUICK START PROCEDURES - Code to run the Quick Start
;;    info tab for teachers.
;; ---------------------------------------------------------

;; Instructions to quickly setup the model, and clients to run this activity
to quick-start-reset
  set qs-item 0
  set qs-items
  [
    "Teacher: Follow these directions to run the HubNet activity."
    "Optional: Zoom In (see Tools in the Menu Bar)"
    "Teacher: Open 'HubNet Control Center' from 'Tools' menu..."
       "and check 'Mirror Plots on Clients' and..."
       "check 'Mirror View on Clients.'"
    "Teacher: Press the INITIAL-LOGIN button."
    "Everyone: Open up a HubNet Client on your machine and..."
      "input the IP Address of this computer, press ENTER and..."
      "type your user name in the box and press ENTER."
    "Optional: Change any of the settings."
    "Teacher: Once all users are logged in..."
       "turn off the INITIAL-LOGIN button."
    "Teacher: Press the RUN-MARKET button to start."
    "Everyone: If you would like to sell extra oil..."
       "change the value of the AMOUNT-TO-CHEAT slider."
    "Everyone: If you would like to catch other sellers cheating..."
       "change the value of the ANTI-CHEAT-INVESTMENT slider."
    "Teacher: To rerun the activity with the same group..."
       "press the RE-RUN button."
    "[end of Quick Start instructions]"
  ]
  set quick-start (item qs-item qs-items)
end

;; view the next item in the quickstart monitor
to quick-start-next
  set qs-item qs-item + 1
  if qs-item >= length qs-items
  [ set qs-item length qs-items - 1 ]
  set quick-start (item qs-item qs-items)
end

;; view the previous item in the quickstart monitor
to quick-start-prev
  set qs-item qs-item - 1
  if qs-item < 0
  [ set qs-item 0 ]
  set quick-start (item qs-item qs-items)
end
@#$#@#$#@
GRAPHICS-WINDOW
518
101
864
468
10
10
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
-10
10
-10
10
1
1
1
ticks

SLIDER
9
355
181
388
demand-sensitivity
demand-sensitivity
0.25
5
2.75
0.05
1
NIL
HORIZONTAL

MONITOR
9
275
84
320
# Sellers
num-sellers
0
1
11

MONITOR
88
275
183
320
Deadw. Loss
market-inefficiency
3
1
11

TEXTBOX
7
335
97
353
Buyers:
11
0.0
0

TEXTBOX
7
437
97
455
Sellers:
11
0.0
0

TEXTBOX
6
204
132
222
Market information:
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
9
392
181
425
base-demand
base-demand
0
1000
130
10
1
NIL
HORIZONTAL

BUTTON
7
142
181
175
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

SLIDER
9
456
181
489
marginal-cost
marginal-cost
0
100
57.5
0.25
1
$
HORIZONTAL

PLOT
206
10
501
189
Oil Sold At Market
Time
Quantity
0.0
20.0
0.0
5.0
true
false
"" ""
PENS
"Monopoly" 1.0 0 -2674135 true "" ""
"Competitive" 1.0 0 -10899396 true "" ""
"Real" 1.0 0 -8630108 true "" ""

MONITOR
357
401
501
446
Competitive Price
perfect-market-price
2
1
11

MONITOR
9
222
84
267
Price
current-price
2
1
11

MONITOR
209
476
352
521
Monopoly Quantity
monopoly-quantity
3
1
11

TEXTBOX
7
79
169
97
Market:
11
0.0
0

MONITOR
209
401
352
446
Competitive Quantity
perfect-market-quantity
2
1
11

MONITOR
87
222
183
267
Quantity Sold
real-supply
0
1
11

PLOT
206
194
501
376
Supply and Demand
Quantity
Price
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"Demand" 1.0 0 -2674135 true "" ""
"Marginal Revenue" 1.0 0 -13345367 true "" ""
"Marginal Cost" 1.0 0 -10899396 true "" ""

BUTTON
109
30
182
63
re-run
re-run
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
9
492
181
525
penalty-severity
penalty-severity
0
25
7
1
1
x
HORIZONTAL

SWITCH
7
106
181
139
perfect-information?
perfect-information?
0
1
-1000

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
206
383
400
401
Perfect Competition Equilibrium:
11
0.0
0

TEXTBOX
206
458
385
476
Monopoly Equilibrium:
11
0.0
0

MONITOR
357
476
501
521
Monopoly Price
monopoly-price
2
1
11

MONITOR
518
11
862
56
Quick Start Instructions - More in Info Window
quick-start
0
1
11

BUTTON
518
64
646
97
Reset Instructions
quick-start-reset
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
692
64
775
97
<<< Prev
quick-start-prev
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
781
64
862
97
Next >>>
quick-start-next
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

This activity explores the economics of a market with imperfect competition. As members of a cartel, participants experience how jointly determined price and quantity decisions can be advantageous to suppliers and harmful to consumers, but also why a cartel is so difficult to sustain. In this alternate version of Oil Cartel, members can also explicitly make investments to detect and penalize other members who "cheat" on their agreement, in order to explore the role of accurate information in maintaining a cartel.

## HOW IT WORKS

The basic behavior of the model is controlled by a set of equations defining aggregate consumer demand, the marginal revenue (MR) curve faced by suppliers as a whole, and the marginal cost curve of suppliers.

1.  Aggregate consumer demand is represented by a demand curve that describes how many units of oil the consumers will purchase at any given price-point.  From the perspective of the cartel, it can be thought of as providing what price consumers will pay given a particular level of total quantity produced by all its members. In this case, market demand is a downward-sloping linear function of price, where "base-demand" is the price of each unit of oil as the total quantity approaches 0 (i.e., the y-intercept of the linear demand function), and "demand-sensitivity" is the slope of the linear demand function:

DEMAND: Price = base-demand - (demand-sensitivity * Quantity)

2.  MR is the change in total revenue as a result of producing one more unit of oil:

MARGINAL REVENUE: MR = base-demand - (2 * demand-sensitivity * Quantity)

This function is the slope of the Total Revenue curve for the cartel.  The Total Revenue curve can be determined by multiplying price (i.e., the demand function), by quantity.

3.  Marginal cost (MC) is the cost of producing one more unit of oil, assumed to be constant, and controlled by a slider in the server interface.

As long as MR is greater than MC, the cartel will be profitable.  Indeed, economic theory predicts that the cartel as a whole will be most profitable if it keeps producing right up to the point where MR = MC. This equilibrium is given displayed on the interface as the "Monopoly Equilibrium".  At the Monopoly Equilibrium, the price available for selling an additional unit of product at the Monopoly Equilibrium is usually much higher than the additional cost of producing that product (MC).  Consequently at the individual supplier level, each cartel member can make a little more profit by "cheating" and selling a more product.  Any change in production by an individual seller changes the total cartel quantity produced, which in turn impacts the overall market price.

To prevent cheating, there are two components to the enforcement mechanisms available to the cartel. Clients control their anti-cheat investment using a slider, which represents a percentage of their gross revenue what will be invested to catch cheaters. In return for this investment, the seller has the same percentage chance of catching any other sellers who are cheating. If caught cheating, a seller must pay a penalty to every seller that caught them cheating. The amount of the penalty is the gross revenues from the excess supply multiplied by the penalty-severity, which is controlled by a slider in the server interface:

COST TO INVESTIGATE CHEATERS = cheat-investment-% * gross-revenue
PENALTY FOR CHEATING = penalty-severity * gross-revenue-from-cheating

## HOW TO USE IT

Quickstart Instructions:

Teacher: Follow these directions to run the HubNet activity.

Optional: Zoom In (see Tools in the Menu Bar)

Teacher: Open 'HubNet Control Center' from 'Tools' menu and check 'Mirror Plots on Clients' and check 'Mirror View on Clients.'"

Teacher: Press the INITIAL-LOGIN button.

Everyone: Open up a HubNet Client on your machine and input the IP Address of this computer, press ENTER and type your user name in the box and press ENTER.

Optional: Change any of the settings.

Teacher: Once all users are logged in turn off the INITIAL-LOGIN button.

Teacher: Press the RUN-MARKET button to start.

Everyone: If you would like to sell extra oil change the value of the AMOUNT-TO-CHEAT slider.

Everyone: If you would like to catch other sellers cheating change the value of the ANTI-CHEAT-INVESTMENT slider.

Teacher: To rerun the activity with the same group press the RE-RUN button.

Buttons:

INITIAL-LOGIN - Allows sellers to log in to the activity. This forever button should be turned off before beginning the simulation.

RUN-MARKET - Runs the simulation. Clients will only be selling oil while the market simulation is running. Turning off this forever button will stop all market activity, although it can be resumed from the same point by clicking the button again.

RE-RUN - Resets the bank-accounts (and other variables) of participants so that another experiment may be run with a clean slate.

Sliders:

DEMAND-SENSITIVITY - Adjusts the sensitivity of the simulated buyers' demand curve. A low value makes the market demand less sensitive to changes in price. High values makes market demand more sensitive to price.

BASE-DEMAND - Adjusts the price where the demand curve crosses the y-axis (when quantity is equal to zero). A high base-demand will yield relatively higher prices when a small amount of oil is supplied.

MARGINAL-COST - Adjusts the firm's cost to produce one additional unit of oil. This is assumed to be constant regardless of the supplier's current level of production.

PENALTY-SEVERITY - Adjusts the severity of the penalty for cartel members who are caught cheating. A severity of 0 means that that is no penalty. The higher the severity, the more the penalty will cost the seller that is caught cheating. The penalty is calculated as PENALTY-SEVERITY multiplied by the gross revenue from cheating.

Switches:

PERFECT-INFORMATION? - Controls whether the sellers receive the View information depicting the market's efficiency and the supply-and-demand plot.

Monitors:

"Price" - The last price for which oil sold at market.

"Quantity Sold" - The quantity of oil that was most recently sold at market.

"# Sellers" - The number of sellers participating in the activity.

"Deadw. Loss" - The deadweight loss is a measure of the inefficiency of the market. More specifically it is the value of goods that could have been produced and consumed by the market at the Perfect Competition Equilibrium minus the value of goods that could have been produced and consumed by the market the Monopoly Equilibrium.

"Competitive Quantity" - The amount of oil that would be sold in a perfectly competitive market given the aggregate demand and supply schedules.

"Competitive Price" - The price that oil would be sold for in a perfectly competitive market given the aggregate demand and supply schedules.  Economic theory predicts that this price equals marginal cost under perfect competition.

"Monopoly Quantity" - The total of all oil produced by suppliers that would maximize profits if the cartel behaved as a unitary monopolist, given the aggregate demand and supply schedules.

"Monopoly Price" - The price of oil that corresponds to the Monopoly Quantity.

Plots:

"Oil Sold at Market" - Plots the quantities of oil sold at market over time. Also on the graph are the amounts that would be supplied under perfect competition and under a monopoly. Anything less than the amount of perfect competiton and above or equal to the monopoly line is an improvement is better for the cartel than a situation with no collusion, and worse for consumers.

"Supply and Demand" - A plot representation of the supply and demand curves described using the parameters in the server interface.

View:

The view illustrates the market conditions by showing turtles, which represent consumers in the market. The view can be turned on and off to simulate the effects on the market of accurate information. The number of turtles on screen represents the number of consumers who would purchase oil in a perfectly competitive market. GREEN turtles are able to buy the oil and are free to drive their cars around the window. RED turtles cannot move because they are unable to buy oil due to the cartel's output restrictions -- they represent the inefficiencies caused by the cartel. YELLOW turtles are able to buy the oil, but are mired in gridlock -- they are being inconvenienced by the unsupplied red turtles (e.g., waiting in lines at the gas station). Yellow turtles are intended solely as a demonstration of the negative effects of collusive or monopolistic behavior. MAGENTA turtles are the beneficiaries of oil supplied beyond the perfectly competitive equilibrium output. Although they would not be able to afford the oil in a competitive market, the cartel is subsidizing the magenta turtles by operating at a loss. They will only appear if there are enough units of oil sold to drive the price of a unit of oil below the cost of producing that unit.

Client Information

AMOUNT-TO-CHEAT - A slider that determines how much (if any) oil the seller wants to supply beyond the cartel's agreed-upon monopolistic output.

ANTI-CHEAT-INVESTMENT - A slider that determines what percentage of the seller's gross revenues should be invested to catch cheaters. Penalties will be given out to cheaters and paid to the seller if any cheaters are caught.

"Bank Account" - A monitor that displays the balance of the seller's bank account. All profits are placed in the seller's bank account.

"Rank" - The user's current rank in order of highest bank account balance.

"Current Profit" - The profit made by the seller last time oil was sold at market.

"Penalties Paid" - The total amount of penalties the user has paid for cheating.

"Income From Penalties" - The total amount the user has earned by catching other sellers cheating.

"# Suppliers" - The number of sellers who are currently in the market.

"Price of Oil" - The most recent price of a single unit of oil at market.

"Severity of Penalties" - The server's penalty-severity setting.

"Market News" - Information of note.

"Supply and Demand" - A plot representation of the supply and demand curves described using the parameters in the server interface.

## THINGS TO NOTICE

If there is too much cheating going on by the member, the market price of oil will precipitously drop.  In fact, it is even possible that the supply may temporarily move beyond the perfect competition equilibrium output. At this point, the suppliers are actually operating at a loss. Magenta turtles in the View are an indication that this is happening.

## THINGS TO TRY

MODIFY THE SUPPLY AND DEMAND CURVES - The temptation to cheat for a cartel member is in part determined by the demand curve and supply curves. Experiment with these market characteristics using the DEMAND-SENSITIVITY, BASE-DEMAND, and MARGINAL-COST sliders in the server interface. In real life, demand for oil is relatively inelastic. How does demand sensitivity affect the incentive to cheat?

TURN OFF PERFECT INFORMATION - Perfect information about the market helps sustain a cartel. Does turning of PERFECT-INFORMATION? (preventing clients from seeing the View representation of market inefficiencies) change the behavior of the cartel members?

EXPERIMENT WITH PENALTY SEVERITY - The more severe the penalty is for cheating, the less incentive there is for a firm to cheat. Penalties can be changed using the PENALTY-SEVERITY slider in the server interface.

## EXTENDING THE MODEL

Here are some suggestions for ways to extend the model:

- Convert from a HubNet model to a NetLogo model. The core mechanics of the HubNet model would also make for an interesting single-user exploration of the theory of monopoly. A similar effect can be gained by running this model with only one client connected.

- Introduce a notion of "bankruptcy." There are no consequences if a seller's bank account balance goes below zero. If a negative balance led to removal from the market, new competitive strategies would become possible. For example, a seller might flood the market at a loss in order to drive out other sellers and then be more secure reverting to monopoly-like output levels after they are gone.

-  Make demand more "agent-based". In a real market, aggregate demand is not  determined the interaction between buyers and sellers.  This would require replacing the functions that control demand with individual buyers that follow their own rules.

- Cartel behavior can be model as a prisoner's dilemma situation where the member is always faced with the decision to cooperate or defect in each round.  As such, one could create a model where cartel members select strategies over time, as opposed to instantaneous pricing decisions.

## NETLOGO FEATURES

Plotting the Supply and Demand Curves:
Displaying the supply and demand curves in a plot window is more complicated than a traditional plot. To do so, a while loop iterates through quantity plotting demand, marginal revenue, and marginal cost until the demand curve crosses the x-axis.

## RELATED MODELS

Tragedy of the Commons, Gridlock

## CREDITS AND REFERENCES

Original implementation: Greg Dunham, for the Center for Connected Learning and Computer-Based Modeling.
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
307
149
643
485
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
7
167
284
200
amount-to-cheat
amount-to-cheat
0
100
0
1
1
NIL
HORIZONTAL

MONITOR
7
36
107
85
Bank Account
NIL
0
1

MONITOR
111
36
168
85
Rank
NIL
0
1

TEXTBOX
8
147
170
165
Output activities:
11
0.0
0

TEXTBOX
8
18
216
36
Your information:
11
0.0
0

SLIDER
8
205
284
238
anti-cheat-investment
anti-cheat-investment
0
100
0
1
1
%
HORIZONTAL

TEXTBOX
306
15
432
33
Market information:
11
0.0
0

MONITOR
172
36
273
85
Current Profit
NIL
0
1

MONITOR
307
35
389
84
# Suppliers
NIL
3
1

MONITOR
7
90
109
139
Penalties Paid
NIL
0
1

MONITOR
112
90
274
139
Income From Penalties
NIL
0
1

MONITOR
307
91
652
140
Market News
NIL
0
1

MONITOR
394
35
513
84
Price of Oil
NIL
2
1

PLOT
9
256
288
515
Supply and Demand
Quantity
Price
0.0
10.0
0.0
10.0
true
true
"" ""
PENS
"Demand" 1.0 0 -2674135 true
"Marginal Revenue" 1.0 0 -13345367 true
"Marginal Cost" 1.0 0 -10899396 true

MONITOR
517
35
652
84
Severity of Penalties
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

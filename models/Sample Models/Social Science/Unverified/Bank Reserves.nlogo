globals [
  bank-loans
  bank-reserves
  bank-deposits
  bank-to-loan
  x-max
  y-max
  rich
  poor
  middle-class
  rich-threshold
]


turtles-own [
  savings
  loans
  wallet
  temp-loan
  wealth
  customer
]


to setup
  clear-all
  initialize-variables
  ask patches [set pcolor black]
  set-default-shape turtles "person"
  crt people [setup-turtles]
  setup-bank
  set x-max 300
  set y-max 2 * money-total
  reset-ticks
end


to setup-turtles  ;; turtle procedure
  set color blue
  setxy random-xcor random-ycor
  set wallet (random rich-threshold) + 1 ;;limit money to threshold
  set savings 0
  set loans 0
  set wealth 0
  set customer -1
end


to setup-bank ;;initialize bank
  set bank-loans 0
  set bank-reserves 0
  set bank-deposits 0
  set bank-to-loan 0
end


to initialize-variables
  set rich 0
  set middle-class 0
  set poor 0
  set rich-threshold 10
end


to get-shape  ;;turtle procedure
  if (savings > 10)  [set color green]
  if (loans > 10) [set color red]
  set wealth (savings - loans)
end


to go
  ;;tabulates each distinct class population
  set rich (count turtles with [savings > rich-threshold])
  set poor (count turtles with [loans > 10])
  set middle-class (count turtles - (rich + poor))
  ask turtles [
    ifelse ticks mod 3 = 0
      [do-business] ;;first cycle, "do business"
      [ifelse ticks mod 3 = 1  ;;second cycle, "balance books" and "get shape"
         [balance-books
          get-shape]
         [bank-balance-sheet] ;;third cycle, "bank balance sheet"
      ]
  ]
  tick
end


to do-business  ;;turtle procedure
  rt random-float 360
  fd 1

  if ((savings > 0) or (wallet > 0) or (bank-to-loan > 0))
    [set customer one-of other turtles-here
     if customer != nobody
     [if (random 2) = 0                      ;; 50% chance of trading with customer
           [ifelse (random 2) = 0            ;; 50% chance of trading $5 or $2
              [ask customer [set wallet wallet + 5] ;;give 5 to customer
               set wallet (wallet - 5) ] ;;take 5 from wallet
              [ask customer [set wallet wallet + 2] ;;give 2 to customer
               set wallet (wallet - 2) ] ;;take 2 from wallet
           ]
        ]
     ]
end



;; First checks balance of the turtle's wallet, and then either puts
;; a positive balance in savings, or tries to get a loan to cover
;; a negative balance.  If it cannot get a loan (if bank-to-loan < 0)
;; then it maintains the negative balance until the next round.  It
;; then checks if it has loans and money in savings, and if so, will
;; proceed to pay as much of that loan off as possible from the money
;; in savings.

to balance-books
  ifelse (wallet < 0)
    [ifelse (savings >= (- wallet))
       [withdraw-from-savings (- wallet)]
       [if (savings > 0)
          [withdraw-from-savings savings]

        set temp-loan bank-to-loan           ;;temp-loan = amount available to borrow
        ifelse (temp-loan >= (- wallet))
          [take-out-loan (- wallet)]
          [take-out-loan temp-loan]
       ]
     ]
    [deposit-to-savings wallet]

  if (loans > 0 and savings > 0)            ;; when there is money in savings to payoff loan
    [ifelse (savings >= loans)
       [withdraw-from-savings loans
        repay-a-loan loans]
       [withdraw-from-savings savings
        repay-a-loan wallet]
    ]
end


;; Sets aside required amount from liabilities into
;; reserves, regardless of outstanding loans.  This may
;; result in a negative bank-to-loan amount, which
;; means that the bank will be unable to loan money
;; until it can set enough aside to account for reserves.

to bank-balance-sheet ;;update monitors
  set bank-deposits sum [savings] of turtles
  set bank-loans sum [loans] of turtles
  set bank-reserves (reserves / 100) * bank-deposits
  set bank-to-loan bank-deposits - (bank-reserves + bank-loans)
end


to deposit-to-savings [amount] ;;fundamental procedures
  set wallet wallet - amount
  set savings savings + amount
end


to withdraw-from-savings [amount] ;;fundamental procedures
  set wallet (wallet + amount)
  set savings (savings - amount)
end


to repay-a-loan [amount] ;;fundamental procedures
  set loans (loans - amount)
  set wallet (wallet - amount)
  set bank-to-loan (bank-to-loan + amount)
end


to take-out-loan [amount] ;;fundamental procedures
  set loans (loans + amount)
  set wallet (wallet + amount)
  set bank-to-loan (bank-to-loan - amount)
end


to-report savings-total
  report sum [savings] of turtles
end


to-report loans-total
  report sum [loans] of turtles
end


to-report wallets-total
  report sum [wallet] of turtles
end


to-report money-total
  report sum [wallet + savings] of turtles
end
@#$#@#$#@
GRAPHICS-WINDOW
262
10
578
347
8
8
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
-8
8
-8
8
1
1
1
ticks
30

SLIDER
138
84
260
117
People
People
0.0
200.0
57
1.0
1
NIL
HORIZONTAL

SLIDER
2
84
137
117
Reserves
Reserves
0.0
100.0
52
1.0
1
NIL
HORIZONTAL

BUTTON
47
41
136
78
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
149
41
236
78
go
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
4
350
247
548
Money & Loans
Time
Mny + Lns
0.0
300.0
-50.0
600.0
true
true
"set-plot-x-range 0 x-max\nset-plot-y-range -50 y-max\n" ""
PENS
"money" 1.0 0 -16777216 true "" "plot money-total"
"loans" 1.0 0 -2674135 true "" "plot loans-total"

MONITOR
144
173
259
218
WALLETS TOTAL
wallets-total
2
1
11

MONITOR
144
124
259
169
SAVINGS TOTAL
savings-total
2
1
11

MONITOR
144
222
259
267
LOANS TOTAL
loans-total
2
1
11

MONITOR
19
124
144
169
MONEY TOTAL
money-total
2
1
11

MONITOR
19
222
144
267
BANK RESERVES
bank-reserves
2
1
11

MONITOR
19
173
144
218
BANK TO LOAN
bank-to-loan
2
1
11

PLOT
250
350
510
548
Savings & Wallets
Time
Svngs + Wllts
0.0
300.0
-50.0
600.0
true
true
"set-plot-x-range 0 x-max\nset-plot-y-range -50 y-max" ""
PENS
"savings" 1.0 0 -13345367 true "" "plot savings-total"
"wallets" 1.0 0 -10899396 true "" "plot wallets-total"

PLOT
581
144
844
342
Income Dist
Time
People
0.0
300.0
0.0
57.0
true
true
"set-plot-x-range 0 x-max\nset-plot-y-range 0 (count turtles)" ""
PENS
"rich" 1.0 0 -10899396 true "" "plot rich"
"middle" 1.0 0 -16777216 true "" "plot middle-class"
"poor" 1.0 0 -2674135 true "" "plot poor"

PLOT
514
350
827
548
Wealth Distribution Histogram
poor <--------> rich
People
0.0
100.0
0.0
57.0
false
false
"set-plot-y-range 0 (count turtles)" ""
PENS
"hist" 1.0 0 -13345367 true "" "if( ticks mod 10 = 1 ) [\n  let max-wealth max [wealth] of turtles\n  let min-wealth min [wealth] of turtles\n  let one-fifth-wealth 0.2 * (max-wealth - min-wealth)\n  let num-bins 10\n  let index 1\n  let interval round ((plot-x-max - plot-x-min) / num-bins)\n  plot-pen-reset\n  repeat num-bins [\n    plotxy ((index - 1) * interval + 0.002)\n                 (count turtles with [\n                      wealth < (min-wealth + index * one-fifth-wealth) and\n                      wealth >= (min-wealth + (index - 1) * one-fifth-wealth)\n                  ]\n                 )\n\n    plotxy  (index * interval)\n                 (count turtles with [\n                      wealth < (min-wealth + index * one-fifth-wealth) and\n                      wealth >= (min-wealth + (index - 1) * one-fifth-wealth)\n                  ]\n                 )\n\n    plotxy (index * interval + 0.001) 0\n    set index index + 1\n  ]\n]\n"

@#$#@#$#@
## WHAT IS IT?

This program models the creation of money in an economy through a private banking system. As most of the money in the economy is kept in banks but only little of it needs to be used (i.e. in cash form) at any one time, the banks need only keep a small portion of their savings on-hand for those transactions. This portion of the total savings is known as the banks' reserves.

The banks are then able to loan out the rest of their savings. The government (the user in this case) sets a reserve ratio mandating how much of the banks' holdings must be kept in reserve at a given time. One 'super-bank' is used in this model to represent all banks in an economy. As this model demonstrates, the reserve ratio is the key determiner of how much money is created in the system.

## HOW IT WORKS

In each round, people (represented by turtles) interact with each other to simulate everyday economic activity. Given a randomly selected number, when a person is on the same patch as someone else it will either give the person two or five dollars, or no money at all. After this, people must then sort out the balance of their wallet with the bank. People will put a positive wallet balance in savings, or pay off a negative balance from funds already in savings. If the savings account is empty and the wallet has a negative balance, a person will take out a loan from the bank if funds are available to borrow (if bank-to-loan > 0). Otherwise the person maintains the negative balance until the next round. Lastly, if someone has money in savings and money borrowed from the bank, that person will pay off as much of the loan as possible using the savings.

## HOW TO USE IT

The RESERVES slider sets the banking reserve ratio (the percentage of money that a bank must keep in reserve at a given time). The PEOPLE slider sets the number of people that will be created in the model when the SETUP button is pressed. The SETUP button resets the model: it redistributes the patch colors, creates PEOPLE people and initializes all stored values. The GO button starts and stops the running of the model and the plotter.

There are numerous display windows in the interface to help the user see where money in the economy is concentrated at a given time. SAVINGS-TOTAL indicates the total amount of money currently being kept in savings (and thus, in the banking system). The bank must then allocate this money among three accounts: LOANS-TOTAL is the amount the bank has lent out, BANK-TO-LOAN is the amount that the bank has available for loan, and BANK-RESERVES is the amount the bank has been mandated to keep in reserve. When the bank must recall loans (i.e. after the reserve ratio has been raised) BANK-TO-LOAN will read a negative amount until enough of the lent money has been paid off. WALLETS-TOTAL gives an indication of the total amount of money kept in peoples' wallets. This figure may also be negative at times when the bank has no money to loan (the turtle will maintain at a negative wallet balance until a loan is possible). MONEY-TOTAL indicates the total-amount of money currently in the economy (SAVINGS-TOTAL + WALLETS-TOTAL).  Because WALLETS-TOTAL is generally kept at 0 in this model (we are assuming that everyone deposits all they can in savings), MONEY-TOTAL and SAVINGS TOTAL tend to be the the same.

A person's color tells us whether it has money in savings (green) or is in debt (red).

## THINGS TO NOTICE

Note how much money is in MONEY-TOTAL after pressing SETUP, but before pressing GO. The total amount of money that can be created will be his figure (the initial money in the system) * (1 / RESERVES). If the RESERVES remains constant through the run of the model, notice how the plot levels off at this value. Why is this equation descriptive of the system?

Once the amount of money in the system has hit the maximum (as calculated be the above equation), watch what happens when the RESERVES slider is set to 100. Now try setting the RESERVES slider back. Why does this happen?

The three monitors on the left of the interface (LOANS-TOTAL, BANK-TO-LOAN and RESERVES) represent the distribution of the bank's money at a given time. Try and track this distribution against SAVINGS-TOTAL, WALLETS-TOTAL and MONEY-TOTAL to understand fluctuations of money in the  system as they happen.

What effect does an increase in RESERVES generally have on TOTAL-MONEY?

Why do SAVINGS-TOTAL (yellow), LOANS-TOTAL (red) and MONEY-TOTAL (green) tend to rise and fall proportionately on the plot?

What happens to TOTAL-MONEY when the reserve ratio is initially set to 100 percent? Why?

## THINGS TO TRY

Vary the RESERVES rate as the model runs, and watch the effect this has on MONEY-TOTAL.

Set RESERVES initially to 100, and watch the effect on TOTAL-MONEY. Now try lowering RESERVES.

Try setting the reserve rate to 0. What would happen if this were done in a real economy?

## EXTENDING THE MODEL

Try extending the model to include payments of interest in the banking system. People with money deposited in savings should be credited with interest on their account (at a certain rate) from the bank from time to time. People with money on loan should make interest payments on their account to the bank from time to time.

This model has turtles interact in a very simple way to have money change hands (and create a need for loans). Try changing the model so that money moves around the system in a different way.

## RELATED MODELS

Wealth Distribution looks at how the reserve ratio affects the distribution of wealth.

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

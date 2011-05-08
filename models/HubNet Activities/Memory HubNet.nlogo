;; We represent each card using two turtles, one for the front and one for the
;; back.  The card-back turtles are on top of the card-front turtles, covering
;; them up.  To reveal a card-front, we hide the card-back turtle on top of it.
breed [card-backs card-back]
breed [cards card]

;; Players are invisible turtles that we use to store information about
;; the participants in the game.
breed [players player]
players-own [
  user-name   ;; a string
  matches     ;; a number, how many successful matches so far
  attempts    ;; a number, how may total tries so far
]

globals [
  current-player  ;; holds a player turtle
]

;;
;; Setup Procedures
;;

to startup
  clear-all
  setup
  hubnet-reset
end

to setup
  ask patches [ set pcolor gray + 3 ]  ;; light gray
  deal
end

to deal
  ask cards [ die ]
  ask card-backs [ die ]
  ask players
  [
    set attempts 0
    set matches 0
  ]
  set current-player 0

  let card-shapes remove "default" shapes
  let y-max floor (pairs-in-deck * 2 / world-width)
  let x-max pairs-in-deck * 2 mod world-width
  let bounded-patches (patch-set patches with [pycor < y-max]
                                 patches with [pycor = y-max and pxcor < x-max])
  ask n-of pairs-in-deck bounded-patches
  [
    sprout-cards 1
    [
      set shape one-of card-shapes
      set card-shapes remove shape card-shapes
      set size 0.5
    ]
  ]
  ask cards
  [ hatch 1 [ move-to one-of bounded-patches with [not any? cards-here] ] ]
  ask cards
  [
    hatch-card-backs 1
    [
      set color white
      set size 0.8
    ]
    hide-turtle
  ]
  if any? players
  [ hubnet-broadcast "Whose turn?" [user-name] of item current-player sort players ]
  hubnet-broadcast "Your matches" 0
  hubnet-broadcast "Your turns" 0
  hubnet-broadcast "Success %" 0
  hubnet-broadcast "Cards remaining" 0
  hubnet-broadcast "Leader" ""
  hubnet-broadcast "Leader's matches" 0
end

;;
;; Runtime Procedures
;;

to go
  if not any? cards [
    __hubnet-broadcast-user-message winner-message
    user-message winner-message
    stop
  ]
  listen-clients
end

to-report winner-message
  let winners players with-max [matches]
  ifelse count winners > 1
  [ report (word "It's a " count winners "-way tie." ) ]
  [ report (word [user-name] of one-of winners " wins!") ]
end

;;
;; HubNet Procedures
;;

to listen-clients
  while [hubnet-message-waiting?]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ add-player hubnet-message-source ][
      ifelse hubnet-exit-message?
      [ remove-player hubnet-message-source ]
      [
        let p item current-player sort players
        if hubnet-message-tag = "View" and [user-name] of p = hubnet-message-source
        [ ask p [ select-card first hubnet-message last hubnet-message ] ]
      ]
    ]
  ]
end

to add-player [name]
  create-players 1
  [
    hide-turtle
    set user-name name
    set matches 0
    set attempts 0
    hubnet-send user-name "Your name" user-name
    hubnet-send user-name "Whose turn?" [user-name] of item current-player sort players
    hubnet-send user-name "Your matches" matches
    hubnet-send user-name "Your turns" attempts
    hubnet-send user-name "Success %" 0
  ]
end

to remove-player [name]
  let p one-of players with [user-name = name]
  if p != nobody
  [
    ifelse position p sort players <= current-player
    [
      ask p [ die ]
      increment-current-player
    ]
    [ ask p [ die ] ]
  ]
end

to select-card [x y]
  if count cards with [not hidden?] < 2
  [
    let my-card one-of [cards-here] of patch x y
    if my-card != nobody
    [
      ask my-card [ show-turtle ]
      display
      let selected-cards cards with [not hidden?]
      if count selected-cards = 2
      [
        set attempts attempts + 1
        hubnet-send user-name "Your turns" attempts
        wait pair-up-delay
        ifelse 1 = length (remove-duplicates [shape] of selected-cards)
        [
          ask selected-cards [ ask card-backs-here [ die ] die ]
          set matches matches + 1
          hubnet-send user-name "Your matches" matches
          hubnet-broadcast "Cards remaining" count cards
          hubnet-broadcast "Leader" leader
          hubnet-broadcast "Leader's matches" max [matches] of players
        ]
        [
          ask selected-cards [ hide-turtle ]
          increment-current-player
        ]
        hubnet-send user-name "Success %" precision (matches / attempts) 3
        display
      ]
    ]
  ]
end

to increment-current-player
  ifelse count players = 0
  [ set current-player 0
    hubnet-broadcast "Whose turn?" "" ]
  [ set current-player (current-player + 1) mod count players
    hubnet-broadcast "Whose turn?" [user-name] of item current-player sort players ]
end

to-report leader
  let leaders players with-max [matches]
  ifelse any? leaders
  [
    ifelse count leaders = 1
    [ report [user-name] of one-of leaders ]
    [ report (word count leaders "-way tie") ]
  ]
  [ report "nobody" ]
end
@#$#@#$#@
GRAPHICS-WINDOW
320
10
928
409
-1
-1
46.0
1
20
1
1
1
0
1
1
1
0
12
0
7
1
1
1
ticks

BUTTON
38
120
145
153
NIL
deal
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
148
120
254
153
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
77
84
234
117
pairs-in-deck
pairs-in-deck
2
floor (count patches / 2)
26
1
1
NIL
HORIZONTAL

MONITOR
28
215
144
260
Cards remaining
count cards
0
1
11

BUTTON
155
203
282
236
show all cards
ask cards [ show-turtle ]\n
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
240
282
273
hide all cards
ask cards [ hide-turtle ]\n
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
73
156
230
189
pair-up-delay
pair-up-delay
0
5
2
0.1
1
NIL
HORIZONTAL

MONITOR
163
279
281
324
number of players
count players
0
1
11

MONITOR
26
279
160
324
current-player
[user-name] of item current-player sort players
0
1
11

BUTTON
97
48
203
81
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

This is a HubNet version of the classic card game Memory. The game tests players' short term memory of where they last saw an image appear. Players can test different mental strategies for remembering.

A deck of cards made up of card pairs of matching images are dealt face down. Each player takes turns flipping over two cards.  If they match, the player keeps the cards and goes again.  If the two cards don't match they are put face down again.

At first, the players have no information about the cards. The more cards that are turned over, the more information the players have -- if they can only remember it!

## HOW IT WORKS

Buttons:
SETUP initializes the world but does not remove any of the players
DEAL starts a new game
GO allows the players who have joined to play the game.  Each player plays the game in a sequential order, but all players can see the cards that other player flip over when they are face up.
SHOW-ALL-CARDS turns over all the cards so all the faces are revealed and all the matches are visible.
HIDE-ALL-CARDS turns all the cards face down so the matches are no longer visible.

Sliders:
PAIRS-IN-DECK determines how many shapes are used (up to 52, though you could add more if you were to resize the world and add more shapes).
PAIR-UP-DELAY indicates how long you want the turned over cards to display before hiding them again.

Monitors:
CARDS REMAINING - the number of cards that have not yet been matched.
CURRENT-PLAYER - the name of the player who is taking his or her turn.
NUMBER-OF-PLAYERS - the number of people currently logged in and playing the game.

## HOW TO USE IT

Press SETUP. Ask the players to login using the HubNet client.  When everyone has successfully logged in press the GO button.  The name of the current player will appear in the CURRENT-PLAYER monitor on the server and the WHOSE TURN monitor on the clients.  Only that client will be allowed to select two cards to turn over by clicking on them with the mouse.  If the cards match the cards will be removed and that player will receive a point and another turn, if not the CURRENT-PLAYER will be incremented to the next player. The game ends when all pairs of cards are matched and removed from the board.

## THINGS TO NOTICE

Try to remember where all the cards you see are located.  The game tests your ability to memorize where you last saw an image.

## THINGS TO TRY

Try different mental strategies for choosing cards and for remembering cards. You can't remember everything, so how will you choose which cards to remember?  How many can you remember perfectly?  How many can you remember well enough to at least make good guesses?

Adjust the PAIR-UP-DELAY to a higher number to give players a longer or shorter time to study where the flipped up cards are located.

Smaller decks are easier.  Adjust the PAIRS-IN-DECK slider to set the difficulty level.

The game is most fun if it's neither too easy nor too hard.  What's the best deck size for two players?  Three players?  Four players?  Etc.

## EXTENDING THE MODEL

Change the shapes used or add more using the Turtle Shapes Editor.

Change the number of cards in each set, have three or four cards of each shape.

## NETLOGO FEATURES

The use of invisible turtles to represent players is a little unusual.  The location of these turtles in the world is irrelevant; they are used just to store information.

We use the DISPLAY primitive to control what the players see when, and for how long.

## ## CREDITS AND REFERENCES
@#$#@#$#@
default
false
0
Rectangle -7500403 true true 15 15 285 285

1
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

10
false
0
Rectangle -7500403 true true 45 120 255 285
Rectangle -1 true false 120 210 180 285
Polygon -7500403 true true 15 120 150 15 285 120
Line -1 false 30 120 270 120

11
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195

12
false
0
Polygon -7500403 true true 150 15 15 120 60 285 240 285 285 120

13
false
0
Circle -7500403 true true 110 5 80
Polygon -7500403 true true 105 90 120 195 90 285 105 300 135 300 150 225 165 300 195 300 210 285 180 195 195 90
Rectangle -7500403 true true 127 79 172 94
Polygon -7500403 true true 195 90 240 150 225 180 165 105
Polygon -7500403 true true 105 90 60 150 75 180 135 105

14
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

15
false
0
Polygon -7500403 true true 151 1 185 108 298 108 207 175 242 282 151 216 59 282 94 175 3 108 116 108

16
false
0
Circle -7500403 true true 0 0 300
Circle -1 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -1 true false 90 90 120
Circle -7500403 true true 120 120 60

17
false
0
Circle -7500403 true true 118 3 94
Rectangle -6459832 true false 120 195 180 300
Circle -7500403 true true 65 21 108
Circle -7500403 true true 116 41 127
Circle -7500403 true true 45 90 120
Circle -7500403 true true 104 74 152

18
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

19
true
0
Polygon -10899396 true false 215 204 240 233 246 254 228 266 215 252 193 210
Polygon -10899396 true false 195 90 225 75 245 75 260 89 269 108 261 124 240 105 225 105 210 105
Polygon -10899396 true false 105 90 75 75 55 75 40 89 31 108 39 124 60 105 75 105 90 105
Polygon -10899396 true false 132 85 134 64 107 51 108 17 150 2 192 18 192 52 169 65 172 87
Polygon -10899396 true false 85 204 60 233 54 254 72 266 85 252 107 210
Polygon -7500403 true true 119 75 179 75 209 101 224 135 220 225 175 261 128 261 81 224 74 135 88 99

2
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

20
false
0
Circle -7500403 true true 3 3 294
Circle -1 true false 30 30 240
Line -7500403 true 150 285 150 15
Line -7500403 true 15 150 285 150
Circle -7500403 true true 120 120 60
Line -7500403 true 216 40 79 269
Line -7500403 true 40 84 269 221
Line -7500403 true 40 216 269 79
Line -7500403 true 84 40 221 269

21
false
0
Polygon -7500403 true true 270 75 225 30 30 225 75 270
Polygon -7500403 true true 30 75 75 30 270 225 225 270

22
true
0
Polygon -7500403 true true 150 19 120 30 120 45 130 66 144 81 127 96 129 113 144 134 136 185 121 195 114 217 120 255 135 270 165 270 180 255 188 218 181 195 165 184 157 134 170 115 173 95 156 81 171 66 181 42 180 30
Polygon -7500403 true true 150 167 159 185 190 182 225 212 255 257 240 212 200 170 154 172
Polygon -7500403 true true 161 167 201 150 237 149 281 182 245 140 202 137 158 154
Polygon -7500403 true true 155 135 185 120 230 105 275 75 233 115 201 124 155 150
Line -7500403 true 120 36 75 45
Line -7500403 true 75 45 90 15
Line -7500403 true 180 35 225 45
Line -7500403 true 225 45 210 15
Polygon -7500403 true true 145 135 115 120 70 105 25 75 67 115 99 124 145 150
Polygon -7500403 true true 139 167 99 150 63 149 19 182 55 140 98 137 142 154
Polygon -7500403 true true 150 167 141 185 110 182 75 212 45 257 60 212 100 170 146 172

23
false
1
Line -7500403 false 163 183 228 184
Circle -7500403 false false 213 184 22
Circle -7500403 false false 156 187 16
Circle -16777216 false false 28 148 95
Circle -16777216 false false 24 144 102
Circle -16777216 false false 174 144 102
Circle -16777216 false false 177 148 95
Polygon -2674135 true true 75 195 90 90 98 92 97 107 192 122 207 83 215 85 202 123 211 133 225 195 165 195 164 188 214 188 202 133 94 116 82 195
Polygon -2674135 true true 208 83 164 193 171 196 217 85
Polygon -2674135 true true 165 188 91 120 90 131 164 196
Line -7500403 false 159 173 170 219
Line -7500403 false 155 172 166 172
Line -7500403 false 166 219 177 219
Polygon -16777216 true false 187 92 198 92 208 97 217 100 231 93 231 84 216 82 201 83 184 85
Polygon -7500403 true false 71 86 98 93 101 85 74 81
Rectangle -16777216 true false 75 75 75 90
Polygon -16777216 true false 70 87 70 72 78 71 78 89
Circle -7500403 false false 153 184 22
Line -7500403 false 159 206 228 205

24
false
0
Polygon -7500403 true true 0 120 45 90 75 90 105 120 150 120 240 135 285 120 285 135 300 150 240 150 195 165 255 195 210 195 150 210 90 195 60 180 45 135
Circle -16777216 true false 38 98 14

25
true
0
Polygon -16777216 true false 111 244 115 272 130 286 151 288 168 277 176 257 177 234 175 195 174 172 170 135 177 104 188 79 188 55 179 45 181 32 185 17 176 1 159 2 154 17 161 32 158 44 146 47 144 35 145 21 135 7 124 9 120 23 129 36 133 49 121 47 100 56 89 73 73 94 74 121 86 140 99 163 110 191
Polygon -16777216 true false 97 37 101 44 111 43 118 35 111 23 100 20 95 25
Polygon -16777216 true false 77 52 81 59 91 58 96 50 88 39 82 37 76 42
Polygon -16777216 true false 63 72 67 79 77 78 79 70 73 63 68 60 63 65

26
false
0
Polygon -7500403 true true 30 195 150 255 270 135 150 75
Polygon -7500403 true true 30 135 150 195 270 75 150 15
Polygon -7500403 true true 30 135 30 195 90 150
Polygon -1 true false 39 139 39 184 151 239 156 199
Polygon -1 true false 151 239 254 135 254 90 151 197
Line -7500403 true 150 196 150 247
Line -7500403 true 43 159 138 207
Line -7500403 true 43 174 138 222
Line -7500403 true 153 206 248 113
Line -7500403 true 153 221 248 128
Polygon -1 true false 159 52 144 67 204 97 219 82

27
false
0
Polygon -7500403 true true 150 11 30 221 270 221
Polygon -1 true false 151 90 92 221 212 221
Line -7500403 true 150 30 150 225

28
false
0
Line -7500403 true 285 240 210 240
Line -7500403 true 195 300 165 255
Line -7500403 true 15 240 90 240
Line -7500403 true 285 285 195 240
Line -7500403 true 105 300 135 255
Line -16777216 false 150 270 150 285
Polygon -7500403 true true 300 15 285 30 255 30 225 75 195 60 255 15
Polygon -7500403 true true 285 135 210 135 180 150 180 45 285 90
Polygon -7500403 true true 120 45 120 210 180 210 180 45
Polygon -7500403 true true 180 195 165 300 240 285 255 225 285 195
Polygon -7500403 true true 180 225 195 285 165 300 150 300 150 255 165 225
Polygon -7500403 true true 195 195 195 165 225 150 255 135 285 135 285 195
Polygon -7500403 true true 15 135 90 135 120 150 120 45 15 90
Polygon -7500403 true true 120 195 135 300 60 285 45 225 15 195
Polygon -7500403 true true 120 225 105 285 135 300 150 300 150 255 135 225
Polygon -7500403 true true 105 195 105 165 75 150 45 135 15 135 15 195
Polygon -7500403 true true 285 120 270 90 285 15 300 15
Line -7500403 true 15 285 105 240
Polygon -7500403 true true 15 120 30 90 15 15 0 15
Polygon -7500403 true true 0 15 15 30 45 30 75 75 105 60 45 15
Line -1 false 164 262 209 262
Line -1 false 223 231 208 261
Line -1 false 136 262 91 262
Line -1 false 77 231 92 261

29
false
0
Polygon -7500403 true true 0 90 15 120 285 120 300 90
Polygon -7500403 true true 30 135 45 165 255 165 270 135
Polygon -7500403 true true 60 180 75 210 225 210 240 180
Polygon -7500403 true true 150 285 15 45 285 45
Polygon -1 true false 75 75 150 210 225 75

3
false
0
Polygon -7500403 true true 150 285 285 225 285 75 150 135
Polygon -7500403 true true 150 135 15 75 150 15 285 75
Polygon -7500403 true true 15 75 15 225 150 285 150 135
Line -1 false 150 285 150 135
Line -1 false 150 135 15 75
Line -1 false 150 135 285 75

30
false
0
Polygon -7500403 true true 55 138 22 155 53 196 72 232 91 288 111 272 136 258 147 220 167 174 208 113 280 24 257 7 192 78 151 138 106 213 87 182

31
false
0
Polygon -7500403 true true 150 90 75 105 60 150 75 210 105 285 195 285 225 210 240 150 225 105
Polygon -16777216 true false 150 150 90 195 90 150
Polygon -16777216 true false 150 150 210 195 210 150
Polygon -16777216 true false 105 285 135 270 150 285 165 270 195 285
Polygon -7500403 true true 240 150 263 143 278 126 287 102 287 79 280 53 273 38 261 25 246 15 227 8 241 26 253 46 258 68 257 96 246 116 229 126
Polygon -7500403 true true 60 150 37 143 22 126 13 102 13 79 20 53 27 38 39 25 54 15 73 8 59 26 47 46 42 68 43 96 54 116 71 126

32
false
0
Rectangle -7500403 true true 45 165 255 240
Polygon -7500403 true true 45 165 30 60 90 165 90 60 132 166 150 60 169 166 210 60 210 165 270 60 255 165
Circle -16777216 true false 222 192 22
Circle -16777216 true false 56 192 22
Circle -16777216 true false 99 192 22
Circle -16777216 true false 180 192 22
Circle -16777216 true false 139 192 22

33
false
0
Polygon -7500403 true true 300 165 300 195 270 210 183 204 180 240 165 270 165 300 120 300 0 240 45 165 75 90 75 45 105 15 135 45 165 45 180 15 225 15 255 30 225 30 210 60 225 90 225 105
Polygon -16777216 true false 0 240 120 300 165 300 165 285 120 285 10 221
Line -16777216 false 210 60 180 45
Line -16777216 false 90 45 90 90
Line -16777216 false 90 90 105 105
Line -16777216 false 105 105 135 60
Line -16777216 false 90 45 135 60
Line -16777216 false 135 60 135 45
Line -16777216 false 181 203 151 203
Line -16777216 false 150 201 105 171
Circle -16777216 true false 171 88 34
Circle -16777216 false false 261 162 30

34
false
0
Rectangle -1 true false 45 45 255 255
Circle -16777216 true false 69 69 42
Circle -16777216 true false 129 129 42
Circle -16777216 true false 69 189 42
Circle -16777216 true false 189 69 42
Circle -16777216 true false 189 189 42
Rectangle -16777216 false false 45 45 255 255

35
false
0
Polygon -7500403 true true 30 165 13 164 -2 149 0 135 -2 119 0 105 15 75 30 75 58 104 43 119 43 134 58 134 73 134 88 104 73 44 78 14 103 -1 193 -1 223 29 208 89 208 119 238 134 253 119 240 105 238 89 240 75 255 60 270 60 283 74 300 90 298 104 298 119 300 135 285 135 285 150 268 164 238 179 208 164 208 194 238 209 253 224 268 239 268 269 238 299 178 299 148 284 103 269 58 284 43 299 58 269 103 254 148 254 193 254 163 239 118 209 88 179 73 179 58 164
Line -16777216 false 189 253 215 253
Circle -16777216 true false 102 30 30
Polygon -16777216 true false 165 105 135 105 120 120 105 105 135 75 165 75 195 105 180 120
Circle -16777216 true false 160 30 30

36
false
0
Rectangle -7500403 true true 90 120 285 150
Rectangle -7500403 true true 255 135 285 195
Rectangle -7500403 true true 180 135 210 195
Circle -7500403 true true 0 60 150
Circle -1 true false 30 90 90

37
false
0
Polygon -7500403 true true 120 135 90 195 135 195 105 300 225 165 180 165 210 105 165 105 195 0 75 135

38
true
0
Circle -1 true false 183 63 84
Circle -16777216 false false 183 63 84
Circle -7500403 true true 75 75 150
Circle -16777216 false false 75 75 150
Circle -1 true false 33 63 84
Circle -16777216 false false 33 63 84

39
false
0
Polygon -7500403 true true 75 150 90 195 210 195 225 150 255 120 255 45 180 0 120 0 45 45 45 120
Circle -16777216 true false 165 60 60
Circle -16777216 true false 75 60 60
Polygon -7500403 true true 225 150 285 195 285 285 255 300 255 210 180 165
Polygon -7500403 true true 75 150 15 195 15 285 45 300 45 210 120 165
Polygon -7500403 true true 210 210 225 285 195 285 165 165
Polygon -7500403 true true 90 210 75 285 105 285 135 165
Rectangle -7500403 true true 135 165 165 270

4
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

40
false
1
Polygon -7500403 true false 61 150 76 180 91 195 103 214 91 240 76 255 61 270 76 270 106 255 132 209 151 210 181 210 211 240 196 255 181 255 166 247 151 255 166 270 211 270 241 255 240 210 270 225 285 165 256 135 226 105 166 90 91 105
Polygon -7500403 true false 75 164 94 104 70 82 45 89 19 104 4 149 19 164 37 162 59 153
Polygon -7500403 true false 64 98 96 87 138 26 130 15 97 36 54 86
Polygon -7500403 true false 49 89 57 47 78 4 89 20 70 88
Circle -16777216 true false 37 103 16
Line -16777216 false 44 150 104 150
Line -16777216 false 39 158 84 175
Line -16777216 false 29 159 57 195
Polygon -5825686 true false 0 150 15 165 15 150
Polygon -5825686 true false 76 90 97 47 130 32
Line -16777216 false 180 210 165 180
Line -16777216 false 165 180 180 165
Line -16777216 false 180 165 225 165
Line -16777216 false 180 210 210 240

41
true
0
Polygon -13345367 true false 120 165 75 285 135 255 165 255 225 285 180 165
Polygon -2674135 true false 135 285 105 135 105 105 120 45 135 15 150 0 165 15 180 45 195 105 195 135 165 285
Rectangle -13345367 true false 147 176 153 288
Polygon -13345367 true false 120 45 180 45 165 15 150 0 135 15
Line -1 false 105 105 135 120
Line -1 false 135 120 165 120
Line -1 false 165 120 195 105
Line -1 false 105 135 135 150
Line -1 false 135 150 165 150
Line -1 false 165 150 195 135

42
false
0
Circle -5825686 true false 135 43 122
Circle -5825686 true false 43 43 122
Polygon -5825686 true false 255 120 240 150 210 180 180 210 150 240 146 135
Line -5825686 false 150 209 151 80
Polygon -5825686 true false 45 120 60 150 90 180 120 210 150 240 154 135

43
false
0
Polygon -1184463 true false 0 240 15 270 285 270 300 240 165 15 135 15
Polygon -16777216 true false 180 75 120 75 135 180 165 180
Circle -16777216 true false 129 204 42
Polygon -16777216 false false 0 240 15 270 285 270 300 240 165 15 135 15

44
false
0
Circle -7500403 true true 75 75 150
Polygon -7500403 true true 300 150 240 120 240 180
Polygon -7500403 true true 150 0 120 60 180 60
Polygon -7500403 true true 150 300 120 240 180 240
Polygon -7500403 true true 0 150 60 120 60 180
Polygon -7500403 true true 60 195 105 240 45 255
Polygon -7500403 true true 60 105 105 60 45 45
Polygon -7500403 true true 195 60 240 105 255 45
Polygon -7500403 true true 240 195 195 240 255 255

45
false
0
Polygon -7500403 true true 165 15 240 15 240 45 195 75 195 240 240 255 240 285 165 285
Polygon -7500403 true true 135 15 60 15 60 45 105 75 105 240 60 255 60 285 135 285

46
false
0
Polygon -7500403 false true 148 30 107 33 74 44 33 58 15 105 0 150 30 240 105 285 135 285 150 270 165 285 195 285 255 255 300 150 268 62 225 43 196 36
Polygon -955883 true false 33 58 0 150 30 240 105 285 135 285 150 270 165 285 195 285 255 255 300 150 268 62 226 43 194 36 148 32 105 35
Polygon -16777216 false false 108 40 75 57 42 101 32 177 79 253 124 285 133 285 147 268 122 222 103 176 107 131 122 86 140 52 154 42 193 66 204 101 216 158 212 209 188 256 164 278 163 283 196 285 234 255 257 199 268 137 251 84 229 52 191 41 163 38 151 41
Polygon -6459832 true false 133 50 171 50 167 32 155 15 146 2 117 10 126 23 130 33
Polygon -16777216 false false 117 10 127 26 129 35 132 49 170 49 168 32 154 14 145 1

47
false
0
Rectangle -6459832 true false 120 225 180 300
Polygon -10899396 true false 150 240 240 270 150 135 60 270
Polygon -10899396 true false 150 75 75 210 150 195 225 210
Polygon -10899396 true false 150 7 90 157 150 142 210 157 150 7

48
false
1
Polygon -7500403 true false 130 158 105 180 93 205 119 196 142 173
Polygon -16777216 true false 121 112 111 128 109 143 112 158 123 175 138 184 156 189 169 188 186 177 199 158 139 98
Circle -11221820 true false 126 86 90
Polygon -13345367 true false 159 103 152 114 151 125 152 135 158 144 169 150 182 151 194 149 207 142 238 111 191 72
Polygon -16777216 true false 187 56 177 72 175 87 178 102 189 119 204 128 222 133 235 132 252 121 265 102 205 42
Circle -11221820 true false 190 30 90

49
false
0
Polygon -7500403 true true 75 225 97 249 112 252 122 252 114 242 102 241 89 224 94 181 64 113 46 119 31 150 32 164 61 204 57 242 85 266 91 271 101 271 96 257 89 257 70 242
Polygon -7500403 true true 216 73 219 56 229 42 237 66 226 71
Polygon -7500403 true true 181 106 213 69 226 62 257 70 260 89 285 110 272 124 234 116 218 134 209 150 204 163 192 178 169 185 154 189 129 189 89 180 69 166 63 113 124 110 160 111 170 104
Polygon -6459832 true false 252 143 242 141
Polygon -6459832 true false 254 136 232 137
Line -16777216 false 75 224 89 179
Line -16777216 false 80 159 89 179
Polygon -6459832 true false 262 138 234 149
Polygon -7500403 true true 50 121 36 119 24 123 14 128 6 143 8 165 8 181 7 197 4 233 23 201 28 184 30 169 28 153 48 145
Polygon -7500403 true true 171 181 178 263 187 277 197 273 202 267 187 260 186 236 194 167
Polygon -7500403 true true 187 163 195 240 214 260 222 256 222 248 212 245 205 230 205 155
Polygon -7500403 true true 223 75 226 58 245 44 244 68 233 73
Line -16777216 false 89 181 112 185
Line -16777216 false 31 150 47 118
Polygon -16777216 true false 235 90 250 91 255 99 248 98 244 92
Line -16777216 false 236 112 246 119
Polygon -16777216 true false 278 119 282 116 274 113
Line -16777216 false 189 201 203 161
Line -16777216 false 90 262 94 272
Line -16777216 false 110 246 119 252
Line -16777216 false 190 266 194 274
Line -16777216 false 218 251 219 257
Polygon -16777216 true false 230 67 228 54 222 62 224 72
Line -16777216 false 246 67 234 64
Line -16777216 false 229 45 235 68
Line -16777216 false 30 150 30 165

5
false
1
Circle -1184463 true false 30 30 240
Circle -7500403 false false 30 30 240
Polygon -16777216 true false 50 82 54 90 59 107 64 140 64 164 63 189 59 207 54 222 68 236 76 220 81 195 84 163 83 139 78 102 72 83 63 67
Polygon -16777216 true false 250 82 246 90 241 107 236 140 236 164 237 189 241 207 246 222 232 236 224 220 219 195 216 163 217 139 222 102 228 83 237 67
Polygon -1 true false 247 79 243 86 237 106 232 138 232 167 235 199 239 215 244 225 236 234 229 221 224 196 220 163 221 138 227 102 234 83 240 71
Polygon -1 true false 53 79 57 86 63 106 68 138 68 167 65 199 61 215 56 225 64 234 71 221 76 196 80 163 79 138 73 102 66 83 60 71

50
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

51
false
0
Circle -955883 true false 15 15 270
Circle -16777216 false false 22 21 256
Line -16777216 false 165 180 192 196
Line -16777216 false 42 140 83 140
Line -16777216 false 37 151 91 151
Line -16777216 false 218 167 265 167
Polygon -16777216 false false 148 265 75 229 86 207 113 191 120 175 109 162 109 136 86 124 137 96 176 93 210 108 222 125 203 157 204 174 190 191 232 230
Polygon -16777216 false false 212 142 182 128 154 132 140 152 149 162 144 182 167 204 187 206 193 193 190 189 202 174 193 158 202 175 204 158
Line -16777216 false 164 154 182 152
Line -16777216 false 193 152 202 153
Polygon -16777216 false false 60 75 75 90 90 75 105 75 90 45 105 45 120 60 135 60 135 45 120 45 105 45 135 30 165 30 195 45 210 60 225 75 240 75 225 75 210 90 225 75 225 60 210 60 195 75 210 60 195 45 180 45 180 60 180 45 165 60 150 60 150 45 165 45 150 45 150 30 135 30 120 60 105 75

52
true
0
Polygon -7500403 true true 165 210 165 225 135 255 105 270 90 270 75 255 75 240 90 210 120 195 135 165 165 135 165 105 150 75 150 60 135 60 120 45 120 30 135 15 150 15 180 30 180 45 195 45 210 60 225 105 225 135 210 150 210 165 195 195 180 210
Line -16777216 false 135 255 90 210
Line -16777216 false 165 225 120 195
Line -16777216 false 135 165 180 210
Line -16777216 false 150 150 201 186
Line -16777216 false 165 135 210 150
Line -16777216 false 165 120 225 120
Line -16777216 false 165 106 221 90
Line -16777216 false 157 91 210 60
Line -16777216 false 150 60 180 45
Line -16777216 false 120 30 96 26
Line -16777216 false 124 0 135 15

6
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

7
false
0
Polygon -13345367 true false 44 131 21 87 15 86 0 120 15 150 0 180 13 214 20 212 45 166
Polygon -13345367 true false 135 195 119 235 95 218 76 210 46 204 60 165
Polygon -13345367 true false 75 45 83 77 71 103 86 114 166 78 135 60
Polygon -7500403 true true 30 136 151 77 226 81 280 119 292 146 292 160 287 170 270 195 195 210 151 212 30 166
Circle -16777216 true false 215 106 30

8
false
0
Rectangle -7500403 true true 60 15 75 300
Polygon -7500403 true true 90 150 270 90 90 30
Line -7500403 true 75 135 90 135
Line -7500403 true 75 45 90 45

9
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

@#$#@#$#@
NetLogo 5.0beta1
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
MONITOR
133
15
240
64
Cards remaining
NIL
0
1

MONITOR
35
124
142
173
Your matches
NIL
0
1

MONITOR
143
149
249
198
Success %
NIL
1
1

MONITOR
25
15
132
64
Whose turn?
NIL
3
1

MONITOR
15
241
140
290
Leader
NIL
3
1

MONITOR
141
241
266
290
Leader's matches
NIL
0
1

MONITOR
82
74
189
123
Your name
NIL
0
1

MONITOR
35
174
142
223
Your turns
NIL
0
1

VIEW
269
13
867
381
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
0
12
0
7

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

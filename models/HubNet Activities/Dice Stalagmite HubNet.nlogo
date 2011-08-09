globals [
  generators             ;; patches where the dice (both singles & pairs) appear
  single-outcomes        ;; list of all the single dice rolls
  pair-outcomes          ;; list of all the sums from rolling pairs of dice
  top-row                ;; agentset of just the top row of patches
  rolls                  ;; the total number of rolls performed either by clients or random rolls
]

breed [paired-dice paired-die]   ;; dice that are paired (go to the right)
breed [single-dice single-die]   ;; dice that are alone (go to the left)
breed [stacked-dice stacked-die] ;; dice that are stationary in the histogram
breed [clients client]

patches-own [
  column     ;; what number (single die or sum of pair) this column of patches is for
]

paired-dice-own [
  die-value   ;; value of the die 1-6
  pair-sum    ;; sum with partner 2-12
]

single-dice-own [
  die-value   ;; value of the die 1-6
]

clients-own
[
  user-id     ;; hubnet identifier
  user-color  ;; color assigned to this user (though colors may be reused)
  die1        ;; values currently entered in the
  die2        ;; client choosers
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
  ;; get rid of all the existing dice
  ask single-dice [ die ]
  ask paired-dice [ die ]
  ask stacked-dice [ die ]
  clear-all-plots
  set single-outcomes []
  set pair-outcomes []
  set rolls 0

 ;; assign outcomes to columns
  ask patches with [pxcor > 0] [
    set column floor ((pxcor + 1) / 2)
  ]
  ask patches with [pxcor < 0] [
    set column pxcor - min-pxcor  + 1
  ]

  ;; color patches
  ask patches [ set pcolor gray + 2 ]
  ask patches with [column != 0 and (column != 1 or pxcor < 0) ] [
    ifelse column mod 2 = 0
      [ set pcolor gray - 3 ]
      [ set pcolor red - 4 ]
  ]

  ;; patches in the 1 column of the pairs histogram
  ;; are removed since 1 is not a possible sum.
  ask patches with [ pcolor = gray + 2 ]
  [ set column 0 ]

  ;; set up agentsets
  set top-row patches with [pycor = max-pycor]
  set generators top-row with [pxcor = 0 or pxcor = 1]
end

;;
;; Runtime Procedures
;;

to go
  bump-down stacked-dice with [pxcor < 0]
  bump-down stacked-dice with [pxcor > 0]

  while [any? single-dice or any? paired-dice] [
    move-paired-dice
    move-single-dice
    display    ;; force the view to update, so we see the dice move smoothly
  ]

  ;; check to see if clients have sent any messages
  listen-clients

  display ;; force the view to update, so we see the dice move smoothly
end

to move-paired-dice
  ;; if either of the two dice isn't at the right column yet,
  ;; both dice move
  ask paired-dice
  [
    ifelse pair-sum != column
    [ fd 2 ]
    [
        ;; if at the bottom of the view, check if we should go "underwater"
        if pycor = min-pycor
        [ paired-die-check-visible ]
        fall
     ]
  ]
end

to move-single-dice
  ;; two single dice may be falling in the same column, so we have
  ;; to make sure that the bottom one moves before the top one,
  ;; otherwise they could get confused
  let how-many count single-dice
  if how-many > 0 [
    ask min-one-of single-dice [pycor] [ move-single-die ]
  ]
  if how-many > 1 [
    ask max-one-of single-dice [pycor] [ move-single-die ]
  ]
end

to move-single-die  ;; single-die procedure
  ifelse die-value != column
    [ fd 1 ]
    [ ;; if at the bottom of the view, check if we should go "underwater"
      if pycor = min-pycor [ single-die-check-visible ]
      fall
    ]
end

to fall  ;; single-die or paired-die procedure
  set heading 180
  ifelse (pycor > min-pycor) and (not any? stacked-dice-on patch-ahead 1)
    [ fd 1 ]
    ;; stop falling
    [ ;; changing breeds resets our shape, so we have to remember our old shape
      let old-shape shape
      set breed stacked-dice
      set shape old-shape
    ]
end

;; determines if my column is tall enough to be seen
to single-die-check-visible  ;; single-die procedure
  if single-outcomes = [] [ stop ]
  let mode first modes single-outcomes
  let height-of-tallest-column length filter [? = mode] single-outcomes
  let height-of-my-column length filter [? = die-value] single-outcomes
  if (height-of-tallest-column - height-of-my-column) >= world-height - 2 [ die ]
end

;; determines if my column is tall enough to be seen
to paired-die-check-visible  ;; paired-die procedure
  if pair-outcomes = [] [ stop ]
  let mode first modes pair-outcomes
  let height-of-tallest-column length filter [? = mode] pair-outcomes
  let height-of-my-column length filter [? = pair-sum] pair-outcomes
  if (height-of-tallest-column - height-of-my-column) >= world-height - 2 [ die ]
end

to bump-down [candidates]
  while [any? candidates with [pycor = max-pycor - 2]] [
    ask candidates [
      if pycor = min-pycor [ die ]
      fd 1
    ]
  ]
end

;;creates a new pair of dice (both singles and pairs) [given the die values]
to generate-roll [ die-1 die-2 the-color]
  let die-values (list die-1 die-2)
  let leftmost-pxcor min [pxcor] of generators

  ;;ask each generator patch to create a pair (one die, with breed pair), with the values
  ask generators [
    let index pxcor - leftmost-pxcor
    sprout-paired-dice 1 [
      set color the-color
      set die-value item index die-values
      set shape word "die " die-value
      set heading 90
    ]
  ]

  ;; clone the paired dice to make the single dice
  ask paired-dice [
    hatch-single-dice 1 [
      set heading 270
      ;; changing breeds resets our shape, so we must explicitly adopt
      ;; our parent's shape
      set shape [shape] of myself
    ]
  ]

  ;; set the sum variable of the pairs
  let total sum [die-value] of paired-dice
  ask paired-dice
  [
    set pair-sum total
  ]
  ;; add to outcomes lists
  set pair-outcomes lput total pair-outcomes
  ask single-dice [ set single-outcomes lput die-value single-outcomes ]

  set rolls rolls + 1
end

;;
;; Procedures for generating sample outputs
;;

to random-roll
  ;; generates a random pair
  generate-roll (1 + random 6) (1 + random 6) white
  update-plots
end

;; keep doing a random-roll continuously
to auto-fill
  random-roll
  repeat 3 [ go ]
end

;; runs the simulation num times
to simulate [ num ]
  repeat num
  [
    random-roll
    repeat 2 [ go ]
  ]
 repeat 70 [ go ]
end

;;
;; HubNet Procedures
;;

to listen-clients
  if hubnet-message-waiting?
  [
    hubnet-fetch-message
    ;;creates a new client turtle if new HubNet client joining
    ifelse hubnet-enter-message?
    [ create-client ]
    [
      ;;if a HubNet client quit, kill client turtle
      ifelse hubnet-exit-message?
      [
         ask clients with [ user-id = hubnet-message-source ]
          [ die ]
      ]
      ;;if just a message from the client, execute appropriately
      [
        ask clients with [ user-id = hubnet-message-source ]
          [ exe-cmd hubnet-message ]
      ]
    ]
  ]
end

to create-client
  ;; makes a turtle to store the data specific to this particular
  ;; client , sets the values of dice to a default and sets
  ;; the user-id variable to the hubnet-source
  create-clients 1
  [
    ht
    set user-id hubnet-message-source
    set user-color item (count clients mod length base-colors) base-colors
    set die1 "--"
    set die2 "--"
  ]
end

to exe-cmd [ message ] ;; client procedure
  if hubnet-message-tag = "Die_A"
  [ set die1 message stop ]
  if hubnet-message-tag = "Die_B"
  [ set die2 message stop ]
  if hubnet-message-tag = "Submit"
  [
    ;; if one of the choices is not a number
    ;; report an error
    ifelse die1 = "--" or die2 = "--"
    [
      hubnet-send user-id "Message:" "Please select values for both dice."
    ]
    [
      ;; clear the user's interface
      hubnet-send user-id "Die_A" "--"
      hubnet-send user-id "Die_B" "--"

      ;; generate the pair and the singles
      generate-roll die1 die2 ifelse-value colored-dice? [ user-color ][ white ]
      ;; send confirmation message to user
      hubnet-send user-id "Message:" (word "Thank you. Your input was " die1 "-" die2 ".")

      ;; reset the client-turtle's variables
      set die1 "--"
      set die2 "--"
      update-plots
    ]
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
170
60
614
525
-1
-1
14.0
1
10
1
1
1
0
0
0
1
-6
24
0
30
1
1
0
Rolls
30.0

BUTTON
39
15
134
48
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
139
15
234
48
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

PLOT
5
60
165
525
Single Dice
Die Value
Count
1.0
7.0
0.0
51.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" "histogram single-outcomes"

PLOT
620
60
806
525
Pair Sums
Dice Total
Count
2.0
13.0
0.0
51.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" "histogram pair-outcomes"

BUTTON
239
15
334
48
random-roll
if not any? generators with [ any? single-dice-here ]\n[ random-roll ]
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
624
16
752
49
colored-dice?
colored-dice?
1
1
-1000

BUTTON
335
15
444
48
NIL
auto-fill
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
554
10
611
55
NIL
rolls
3
1
11

@#$#@#$#@
## WHAT IS IT?

Dice Stalagmite HubNet is a Participatory Simulation Activity (PSA) in probability for exploring dependent and independent events.  Specifically, you compare the outcome distribution of a compound event, the sum of two randomly "rolled" dice, to the outcome distribution of independent events, the values of these same dice, taken one die at a time.

This model is a part of the ProbLab curriculum.  The ProbLab curriculum is currently under development at the CCL.  For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

Participants each roll their own pair of dice (preferably dice of different colors) and input the outcome, for example, "5 2", through the client interface.  Each outcome appears at the top middle of the server interface, in the form of the same two dice.  Then, this pair is duplicated, and one pair moves to the right (the pair), and the other pair moves to the left (the singles).  On each side, there is a "picture bar chart": on the right, the bar chart is for sums of dice (2 through 12) and on the left, the bar chart is for individual die values (1 through 6).  The dice have to find their columns and slide down those columns.  On the right, the dice find the column of their sum, and on the left, the dice each finds its own column of its value.  The events in each of the charts are mirrored in their respective histograms.

As the dice stack up, you begin to notice different typical distributions in each bar chart.  The "singles" chart (SINGLE DICE), on the left, grows to become equally distributed.  It's never really "flat," but we can never predict in advance which column will "win."  On the other hand, the "pairs" bar chart (PAIR SUMS), on the right, always grows to the shape of a triangle -- the closer a column is to the middle of the of chart, the taller it will be, eventually.

This triangle of pairs emerges as a result of the probabilities of rolling each sum with a pair of dice.  These probabilities can be found by examining the number of ways there are to roll each sum, as shown below.

                               61
                            51 52 62
                         41 42 43 53 63
                      31 32 33 34 44 54 64
                   21 22 23 24 25 35 45 55 65
                11 12 13 14 15 16 26 36 46 56 66

For example, there are four ways to roll a 5 (14, 23, 32 and 41), and thus the probability of rolling a 5 is 4/36, or 1/9.

A printable sheet graphically depicting all of the combinations can be found here:  
http://ccl.northwestern.edu/curriculum/ProbLab/dice-total-distrib.doc

## HOW TO USE IT

Make sure all participants are connected and have their clients.  Press GO.  Participants each roll their own pair of dice and then use their interface to input the result of the roll.  It is helpful (but not absolutely important) to have dice of different colors, because then you can differentiate between, say, a "2 5" and a "5 2."  That is helpful for comparing the outcomes to the combinatorial analysis of a pair of dice.

Make sure your participants understand the difference between the two charts and how the dice find their columns (independently, on the left, and as a pair, on the right).  You can use the RANDOM-ROLL button and the speed slider to show this.

Once you see that the sums chart is beginning to grow its "bump" in the middle, ask the participants if they are noticing anything.  If not, keep playing until participants notice the bump.  If you have worked on the combinatorial analysis of a pair of dice, participants may realize that they are getting in the pairs chart (PAIR SUMS) the same triangular shape they got in that analysis (see the ProbLab materials for this triangular shaped combinatorial space of a pair of dice).  A useful discussion could be around why an experiment with random outcomes mirrors the shape of a chart you built through careful analysis.  Note that the combinatorial-analysis chart has no duplicates -- each combination appears only once -- whereas the experiment does have duplicates.

IMPORTANT NOTE: Because there is limited space in the view, when dice are stacking up near the top of either the singles area or the pairs area, the program bumps the die columns down, so that they can keep growing.  A useful way to think about this is that you are always looking at only the top section of each dice stack.  The plots, however, keep track of all the dice that have been rolled.

Buttons:  
SETUP -- reset the dice in the world but keep the logged in clients.

GO -- GO needs to be running so that participants can submit their dice rolls, and so that the dice can move (e.g. from RANDOM-ROLL).

RANDOM-ROLL -- creates a random pair of dice that behaves the same as though it had been input by a client.  This is useful for preparing for class, for demonstrations to the classroom, and for boosting the activity of a relatively small group of participants.

AUTO-FILL -- creates random pairs in a loop.

Switches:  
COLORED-DICE? -- when set to "On", the dice that are created will be color-coded (from a set of 12 different colors) based on what client "rolled" them.

Monitors:  
ROLLS -- the total number of rolls performed including random rolls and rolls from clients.

Plots:  
SINGLE DICE -- histogram of independent outcomes of rolling dice (each die value is a separate event).

PAIR SUMS -- histogram of compound outcomes from rolling dice (each sum of a pair of dice is an event).

To start the activity over with the same group of students stop the GO button by pressing it again, press the SETUP button, and press GO again.  To run the activity with a new group of students press the RESET button in the Control Center.

## THINGS TO NOTICE

The more events you stack up in the charts, the more the PAIR SUMS chart will take on a triangular shape.  Eventually, the '7' column will be the tallest, and then, in descending order, the '5' and '8,' the '4' and '9,' the '3' and '10,' the '2' and '11,' and the '1' and '12.'  The chart will not necessarily be perfect, but the more input the program gets, the closer the chart will approach this shape.

At the same time, the SINGLE DICE graph approaches the shape of an equal distribution.  
The SINGLE DICE is never exactly equally distributed.  If you look at the chart, you'll see that one of the columns is "beating" the other columns.  So why do we call it "equally distributed" if it's not really equal?  Well, it may depend on whether we are attending to the differences between the heights of the columns (an "additive" approach) or if we're attending to the proportions of the heights (a "multiplicative" approach).  Once you've accumulated hundreds of pairs of dice, one column may be over ten rows taller than another, but when you look at its plot, SINGLE DIE, this difference will not appear as large as it is in the bar chart of the display.

## THINGS TO TRY

If you are working alone on this PSA, for instance, if you are preparing for class, press GO and then type in the Command Center: simulate 1000.  This will allow you to see how the charts will look after, say, 30 students have each input 34 rolls.

## EXTENDING THE MODEL

Why settle for a pair of dice?  How would the charts look for three dice?  Do you expect any difference in the shapes of the outcome distributions?  Would the SINGLE DICE graph change at all?

## NETLOGO FEATURES

This model animates the movement of the dice and uses the `display` command to show the intermediate states.

## RELATED MODELS

See the ProbLab model Dice Stalagmite that is essentially the same activity but is designed to be used by a single person.  See the ProbLab model 9-Block Stalagmite that conveys the same ideas as this model does but uses a different stochastic object (not a pair of dice).

## CREDITS AND REFERENCES

Thanks to Dor Abrahamson for his work on the design of this model and the ProbLab curriculum.  Thanks to Josh Unterman for building Dice Stalagmite and to Steve Gorodetskiy for converting it to the Dice Stalagmite HubNet.
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
Circle -7500403 true true 30 30 240

circle 2
false
0
Circle -7500403 true true 16 16 270
Circle -16777216 true false 46 46 210

cow
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

die 1
false
0
Rectangle -7500403 true true 45 45 255 255
Circle -16777216 true false 129 129 42

die 2
false
0
Rectangle -7500403 true true 45 45 255 255
Circle -16777216 true false 69 69 42
Circle -16777216 true false 189 189 42

die 3
false
0
Rectangle -7500403 true true 45 45 255 255
Circle -16777216 true false 69 69 42
Circle -16777216 true false 129 129 42
Circle -16777216 true false 189 189 42

die 4
false
0
Rectangle -7500403 true true 45 45 255 255
Circle -16777216 true false 69 69 42
Circle -16777216 true false 69 189 42
Circle -16777216 true false 189 69 42
Circle -16777216 true false 189 189 42

die 5
false
0
Rectangle -7500403 true true 45 45 255 255
Circle -16777216 true false 69 69 42
Circle -16777216 true false 129 129 42
Circle -16777216 true false 69 189 42
Circle -16777216 true false 189 69 42
Circle -16777216 true false 189 189 42

die 6
false
0
Rectangle -7500403 true true 45 45 255 255
Circle -16777216 true false 84 69 42
Circle -16777216 true false 84 129 42
Circle -16777216 true false 84 189 42
Circle -16777216 true false 174 69 42
Circle -16777216 true false 174 129 42
Circle -16777216 true false 174 189 42

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
NetLogo 5.0beta5
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
CHOOSER
0
62
117
107
Die_A
Die_A
"--" 1 2 3 4 5 6
0

CHOOSER
145
62
263
107
Die_B
Die_B
"--" 1 2 3 4 5 6
0

BUTTON
95
113
164
146
Submit
NIL
NIL
1
T
OBSERVER
NIL
NIL

MONITOR
0
10
263
59
Message:
NIL
0
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

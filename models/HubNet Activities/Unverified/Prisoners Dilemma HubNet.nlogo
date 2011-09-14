;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variable and Breed declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

breed [ students student ]
breed [ androids android ]

globals [
  COOPERATE
  DEFECT

  number-of-plays
  strategy-list

  strategy-totals        ;; list of totals for selected strategies, for plotting
  strategy-totals-count  ;; number of times that strategy has been used
  defect-total           ;; total number of times turtles have defected
  cooperate-total        ;; total number of times turtles have cooperated

  ;; Shapes
  shape-names            ;; list of names of the non-sick shapes a client's turtle can have
  colors                 ;; list of colors used for clients' turtles
  color-names            ;; list of names of colors used for students' turtles
  used-shape-colors      ;; list of shape-color pairs that are in use
  max-possible-codes     ;; total number of unique shape/color combinations
]

turtles-own [
  score                  ;; my current score
  defect-now?            ;; what will I do this round?
  partner                ;; the who of my partner
  total-defects          ;; counts how many times the partner has defected, used for certain strategies
  selected-strategy      ;; string that contains  the user's selected strategy

  total                  ;; the total score of the turtle, following all plays

  play-history           ;; stores all of your moves in this play
  play-partner-history   ;; stores all of your partners moves in this play

  base-shape             ;; original shape of a turtle

  user-code              ;; students create custom strategies, which are stored here
  code-changed?          ;; is true when the user changes given strategies
]

students-own [
  user-id                ;; unique id, input by the client when they log in, to identify each student turtle
]

;;;;;;;;;;;;;;;;;;;;;
;; Setup Functions ;;
;;;;;;;;;;;;;;;;;;;;;

to startup
  hubnet-reset
  setup
end

to setup
  clear-all
  setup-shapes
  setup-vars
  reset-ticks
end

to setup-shapes
  ;; most of these are to handle shapes and colors for uniqueness of students

  set shape-names [
                    "airplane" "android" "box" "butterfly" "cactus"
                    "car" "cat" "cow skull" "dog" "ghost"  "heart"
                    "key" "leaf" "monster" "moon" "star" "target" "wheel"
                  ]
  set colors      [ white gray brown yellow green lime turquoise
                    cyan sky blue violet ]

  ;; adjust a few colors so they don't blend in with the red infection dot too much
  set colors lput (orange + 1) colors
  set colors lput (magenta + 0.5) colors
  set colors lput (pink + 2.5) colors
  set color-names [ "white" "gray" "brown" "yellow" "green" "lime" "turquoise"
                    "cyan" "sky" "blue" "violet" "orange" "magenta" "pink" ]
  set max-possible-codes (length colors * length shape-names)
  set used-shape-colors []
end

to setup-vars

  ;; these are constant, for use when students code their own strategies
  set COOPERATE false
  set DEFECT true

  set number-of-plays 10

  ;; this is used for the androids, in interpreting their strategy
  set strategy-list [ "random" "cooperate" "defect" "go-by-majority" "tit-for-tat"
                      "suspicious-tit-for-tat" "tit-for-two-tats" "pavlov"
                      "unforgiving" "custom-strategy" ]

  set strategy-totals []
  set strategy-totals-count []
  foreach strategy-list [
    set strategy-totals fput 0 strategy-totals
    set strategy-totals-count fput 0 strategy-totals-count
  ]
  set defect-total 0
  set cooperate-total 0

end

to create-android-player
  create-androids 1 [
    set total 0
    set partner nobody
    set base-shape "android"
    set shape base-shape
    set color blue
    setup-turtle-vars
    set selected-strategy one-of but-last strategy-list
    set label selected-strategy
    set-code
  ]
end

;; Places turtles in random locations again, ready to find new partners.
to rerun
  ask turtles [
    setup-turtle-vars
    set total 0
    set score 3
  ]
end

to setup-turtle-vars  ;; turtle procedure
  set partner nobody
  set defect-now? false
  set play-history []
  set play-partner-history []
  set total-defects 0
  set size 1
  set total (total + score)
  set score 3
  setxy random-xcor
        random-ycor
end

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Runtime Procedures;;;
;;;;;;;;;;;;;;;;;;;;;;;;;

to play-n-times
    listen-clients
  if (any? turtles) [
    do-plots
    find-partners

    every 0.3 [
      play-a-round
    ]

    ask turtles with [ length play-history = number-of-plays ] [
      ifelse breed = students [
        ;; students may have changed strategy during the round, and this change can only be made afterwards
        if not code-changed? [
          let strategy-index position selected-strategy strategy-list
          set strategy-totals (replace-item strategy-index strategy-totals ((item strategy-index strategy-totals) + score))
          set strategy-totals-count (replace-item strategy-index strategy-totals-count ((item strategy-index strategy-totals-count) + 1))
        ]
      ]
      [
        let strategy-index position selected-strategy strategy-list
        set strategy-totals (replace-item strategy-index strategy-totals ((item strategy-index strategy-totals) + score))
        set strategy-totals-count (replace-item strategy-index strategy-totals-count ((item strategy-index strategy-totals-count) + 1))
      ]
      setup-turtle-vars
      set shape base-shape
    ]
    display
  ]
end

to play-a-round  ;; determines the actions of turtles each turn
  ask turtles with [ partner != nobody ] [
    custom-strategy
    get-payoff                 ;; after the strategies are determined, the results of the round are determined
    if breed = students
      [ send-info-to-clients ]
  ]
end


;; test user strategy.
to custom-strategy ;; turtle procedure

  carefully [
    set defect-now? (run-result user-code)
  ] [
    ifelse (breed = students) [
      hubnet-send user-id "Errors:" (error-message)
      output-show "bad strategy survived!"
    ] [
      output-show (word "Problem on android " who ": " error-message)
    ]
  ]
end

to find-partners
  ;;  In this example, only turtles that haven't found a partner can move around.
  listen-clients
  every 0.1 [
    ask turtles with [partner = nobody] [
      ;;  randomly move about the view
      if breed != students [
        rt random-normal 0 20
      ]
      fd 0.5
    ]
  ]

  ;; Ask unpartnered turtles to check if they are on a patch with a turtle that
  ;; also don't have a partner.
  ask turtles [
    if (partner = nobody) and (any? other turtles-here with [partner = nobody]) [
      set partner one-of other turtles-here with [partner = nobody]
      ask partner [ set partner myself ]
    ]
  ]
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;     Some Helpful Functions    ;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to set-defect-shape
  ifelse defect-now? [
    set shape word "sick " base-shape
  ] [
    set shape word "good " base-shape
  ]
end

to-report test-strategy [ snippet ]
  carefully [
    let tester (run-result snippet)
    ifelse not (tester = false or tester = true) [
      hubnet-send user-id "Errors:" ("the output must be either true, or false") report false
    ] [
      report true
    ]
  ] [
    hubnet-send user-id "Errors:" (error-message)
    report false
  ]
end

to set-code  ;; outputs the code to the input box, for students to see and modify
  if selected-strategy = "random"
    [ set user-code ("ifelse-value (random 2 = 0)\n  [DEFECT] \n[COOPERATE]") stop ]
  if selected-strategy = "cooperate"
    [ set user-code ("COOPERATE") stop ]
  if selected-strategy = "defect"
    [ set user-code ("DEFECT") stop ]
  if selected-strategy = "go-by-majority"
    [ set user-code ("ifelse-value (empty? play-history)\n  [COOPERATE] \n[ \n  ifelse-value (total-defects / (length play-history) > 0.5)\n    [DEFECT] \n  [COOPERATE] \n]") stop ]
  if selected-strategy = "tit-for-tat"
    [ set user-code ("ifelse-value (empty? play-history)\n  [COOPERATE] \n[ \n  ifelse-value (last play-partner-history = DEFECT)\n    [DEFECT]\n  [COOPERATE] \n]") stop ]
  if selected-strategy = "suspicious-tit-for-tat"
    [ set user-code ("ifelse-value (empty? play-history)\n  [DEFECT] \n[ \n  ifelse-value (last play-partner-history = DEFECT)\n    [DEFECT]\n  [COOPERATE] \n]") stop ]
  if selected-strategy = "tit-for-two-tats"
    [ set user-code ("ifelse-value (length play-history < 2 )\n  [COOPERATE] \n[ \n  ifelse-value ((last play-partner-history = DEFECT) and item (length play-partner-history - 2) play-partner-history = DEFECT)\n    [DEFECT] \n  [COOPERATE] \n]") stop ]
  if selected-strategy = "pavlov"
    [ set user-code ("ifelse-value (empty? play-history) \n[ \n  ifelse-value (random 2 = 0) [DEFECT] [COOPERATE] \n] \n[ \n  ifelse-value (last play-partner-history = DEFECT) \n  [\n    ifelse-value (last play-history = DEFECT)\n      [COOPERATE]\n    [DEFECT]\n  ]\n  [\n    ifelse-value (last play-history = DEFECT)\n      [DEFECT]\n    [COOPERATE]\n  ]\n]") stop ]
  if selected-strategy = "unforgiving"
    [ set user-code ("ifelse-value (empty? play-history)\n  [COOPERATE] \n[ \n  ifelse-value ((last play-partner-history = DEFECT) or (last play-history = DEFECT))\n    [DEFECT] \n  [COOPERATE] \n]") stop ]
end


;;;;;;;;;;;;;;;;;;;;;;
;;; End Strategies ;;;
;;;;;;;;;;;;;;;;;;;;;;

to get-payoff ;;Turtle Procedure
  ifelse [defect-now?] of partner        ;; if the partner has defected
  [
    set play-partner-history lput true play-partner-history  ;; it is recorded in the history of his partner
    ifelse defect-now?                 ;; if this player has defected
    [
       set score score + D-D
       set play-history lput true play-history  ;; it is recorded in this player's history
    ]
    [
       set score score + C-D
       set play-history lput false play-history
    ]
    set total-defects total-defects + 1  ;; used for go-by-majority strategy
    set defect-total defect-total + 1    ;; when detecting a partner's defecting, it is totaled for plotting
  ]
  [
    set play-partner-history lput false play-partner-history
    ifelse defect-now?
    [
      set score score + D-C
      set play-history lput true play-history
    ]
    [
      set score score + C-C
      set play-history lput false play-history
    ]
    set cooperate-total cooperate-total + 1  ;;cooperates are totaled for plotting
  ]
  set-defect-shape
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;            Plotting           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to do-plots  ;;firsts plots the average turnout for provided strategies, then the rest
  plot-strategies
  plot-cooperate-defect
end

to plot-strategies  ;;plots the average scores for each of the given strategies
  set-current-plot "Strategies"

  let i 0
  foreach (but-last strategy-list)
  [
    set-current-plot-pen ?
    if ((item i strategy-totals-count) != 0)
    [
      plot-pen-reset
      set-plot-pen-mode 1
      plotxy i ( (item i strategy-totals) / (item i strategy-totals-count) )
    ]
    set i ( i + 1 )
  ]
end

to plot-cooperate-defect ;;plots the total number of times that turtles have cooperated or defected
  set-current-plot "C-D Plot"
  set-current-plot-pen "cooperate"
  plot cooperate-total
  set-current-plot-pen "defect"
  plot defect-total
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Code for interacting with the clients ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; determines which client sent a command, and what the command was
to listen-clients
  while [ hubnet-message-waiting? ]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ create-new-student ]
    [
      ifelse hubnet-exit-message?
      [ remove-student ]
      [ execute-command hubnet-message-tag ]
    ]
  ]
end

;; NetLogo knows what each student turtle is supposed to be
;; doing based on the tag sent by the node:
to execute-command [command]
  if command = "strategy-choice"  ;; the user wants to change his/her strategy, by selecting a pre-made strategy
  [
    ask students with [ user-id = hubnet-message-source ]
    [
      ifelse (allow-strategy-change?) ;; if this is permitted under the current game-mode
      [
        hubnet-send user-id "Errors:" ("")
        set selected-strategy hubnet-message
        set-code
        hubnet-send user-id "strategy" user-code
        send-info-to-clients
        set code-changed? false
      ]
      [
        hubnet-send user-id "Errors:" ("You cannot change your strategy while playing")
        hubnet-send user-id "strategy-choice" (selected-strategy)
      ]
    ]
  ]
  if command = "strategy"             ;; the user has modified the code of the current strategy
  [
     ask students with [ user-id = hubnet-message-source ]
     [
       if (hubnet-message = user-code or not allow-strategy-change?) [ stop ]
       hubnet-send user-id "Errors:" ("")
       ;; code is taken, and tested for accuracy, so students can make more changes before playing another round
       if ( test-strategy hubnet-message )
       [
         set user-code hubnet-message
         set code-changed? true
       ]
     ]
  ]
  if command = "Up"
    [ execute-move 0 ]
  if command = "Down"
    [ execute-move 180 ]
  if command = "Left"
    [ execute-move 270 ]
  if command = "Right"
    [ execute-move 90 ]
  if command = "Up-Left"
    [ execute-move 315 ]
  if command = "Up-Right"
    [ execute-move 45 ]
  if command = "Down-Left"
    [ execute-move 225 ]
  if command = "Down-Right"
    [ execute-move 135 ]
  if command = "Change Appearance"
    [ execute-change-turtle  ]
end

;; Create a turtle, set its shape, color, and position
;; and tell the node what its turtle looks like and where it is
to create-new-student
  create-students 1
  [
    setup-student-vars
    hubnet-send user-id "strategy" user-code
    send-info-to-clients
  ]
end

to execute-move [angle]
  ask students with [user-id = hubnet-message-source ]
  [
      set heading angle
  ]
end

;; sets the turtle variables to appropriate initial values
to setup-student-vars  ;; turtle procedure
  set user-id hubnet-message-source
  set total 0
  setup-turtle-vars
  set selected-strategy "random"
  set code-changed? false
  set-code
  set-unique-shape-and-color
end

;; pick a base-shape and color for the turtle
to set-unique-shape-and-color
  let code random max-possible-codes
  while [member? code used-shape-colors and count students < max-possible-codes]
  [
    set code random max-possible-codes
  ]
  set used-shape-colors (lput code used-shape-colors)
  set base-shape item (code mod length shape-names) shape-names
  set shape base-shape
  set color item (code / length shape-names) colors
end

;; report the string version of the turtle's color
to-report color-string [color-value]
  report item (position color-value colors) color-names
end

;; sends the appropriate monitor information back to the client
to send-info-to-clients
  hubnet-send user-id "You are a:" (word (color-string color) " " base-shape)
  hubnet-send user-id "Your Score:" (score)
  hubnet-send user-id "Your Total:" (total)
  ifelse partner != nobody
  [
    hubnet-send user-id "Partner's Score:" ([score] of partner)
    hubnet-send user-id "Partner's History:" (map [ ifelse-value (? = true) ["D "] ["C "] ] play-partner-history)
    hubnet-send user-id "Your History:" ( map [ ifelse-value (? = true) ["D "] ["C "] ] play-history)
    hubnet-send user-id "Points:" (map [ifelse-value ((?1 = false) and (?2 = false)) [C-C] [ifelse-value ((?1 = false) and (?2 = true)) [C-D] [ ifelse-value ((?1 = true) and (?2 = false)) [D-C] [D-D]]]] play-history play-partner-history)
  ]
  [
    hubnet-send user-id "Partner's Score:" ("")
    hubnet-send user-id "Partner's History:" ("")
    hubnet-send user-id "Your History:" ("")
  ]

end

;; Kill the turtle, set its shape, color, and position
;; and tell the node what its turtle looks like and where it is
to remove-student
  ask students with [user-id = hubnet-message-source]
  [
    set used-shape-colors remove my-code used-shape-colors
    die
  ]
end

;; translates a student turtle's shape and color into a code
to-report my-code
  report (position base-shape shape-names) + (length shape-names) * (position color colors)
end

;; users might want to change their shape and color, so that they can find themselves more easily
to execute-change-turtle
  ask students with [user-id = hubnet-message-source]
  [
    set used-shape-colors remove my-code used-shape-colors
    show-turtle
    set-unique-shape-and-color
    hubnet-send user-id "You are a:" (word (color-string color) " " base-shape)
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
541
10
941
431
7
7
26.0
1
12
1
1
1
0
1
1
1
-7
7
-7
7
1
1
0
ticks
30.0

BUTTON
27
10
103
43
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

SWITCH
339
10
525
43
allow-strategy-change?
allow-strategy-change?
0
1
-1000

BUTTON
27
54
143
90
Create android
create-android-player
NIL
1
T
OBSERVER
NIL
A
NIL
NIL
1

BUTTON
110
10
187
43
Rerun
rerun
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
194
10
287
43
Play
play-n-times
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

TEXTBOX
330
60
457
102
Payoffs \n(prisoner-opponent)
11
0.0
0

SLIDER
330
106
422
139
C-C
C-C
-5
5
1
1
1
NIL
HORIZONTAL

SLIDER
426
107
518
140
C-D
C-D
-5
5
-4
1
1
NIL
HORIZONTAL

SLIDER
330
139
422
172
D-C
D-C
-5
5
5
1
1
NIL
HORIZONTAL

SLIDER
426
140
518
173
D-D
D-D
-5
5
-3
1
1
NIL
HORIZONTAL

PLOT
10
99
308
401
Strategies
NIL
NIL
-1.0
10.0
0.0
10.0
true
false
"" ""
PENS
"random" 1.0 0 -16777216 true "" ""
"cooperate" 1.0 0 -6459832 true "" ""
"defect" 1.0 0 -13345367 true "" ""
"go-by-majority" 1.0 0 -955883 true "" ""
"tit-for-tat" 1.0 0 -11221820 true "" ""
"suspicious-tit-for-tat" 1.0 0 -5825686 true "" ""
"tit-for-two-tats" 1.0 0 -13840069 true "" ""
"pavlov" 1.0 0 -1184463 true "" ""
"unforgiving" 1.0 0 -2674135 true "" ""
"0" 1.0 0 -16777216 false "" "plotxy 0 0"

PLOT
328
200
521
400
C-D Plot
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
"cooperate" 1.0 0 -10899396 true "" ""
"defect" 1.0 0 -13345367 true "" ""

OUTPUT
11
411
521
465
12

@#$#@#$#@
## WHAT IS IT?

This model is a HubNet version of the Prisoner's Dilemma.  The Prisoner's Dilemma is a famous game-theory situation that models the costs and benefits of collaboration or treason between free agents where there is a struggle over some capital.

The game has implications for a variety of social situations that involve negotiations in the absence of absolute trust between agents, such as in politics and economics. The game is a succession of interactions between agents, with each agent trying to maximize personal gains over all interactions. In this game, aggressive behavior is not necessarily the best strategy for maximizing personal gains. The rules of the game are such that agents are penalized when behaving aggressively in interacting with another aggressive agent, At each interaction between agents, each agent decides whether to 'cooperate' or 'defect.' These household terms in economics come from the following hypothetical situation.

The Prisoner's Dilemma presents an interesting problem: if you and your accomplice in crime are imprisoned, then in order to minimize the overall jail time you should cooperate with your partner by remaining silent and not confessing.  However, you may choose to defect the unsaid "contract" with your partner by confessing.Let's assume you have confessed. If your partner does not confess you will go free.  If your partner confesses, you will go to jail for three years, much better than the five you would have earned had you refused to confess.  Unfortunately, your partner is in the same position.  Acting rationally in the short term, you will both be worse off. For more introductory information, please refer in the NetLogo models library to the PD BASIC model found in the PRISONER'S DILEMMA suite. Note also that in this model, the traditional year values, such as '3 years' or '5 years,' are given for modification.

## HOW IT WORKS

This model allows for many students within a classroom to challenge each other over a network.  Students will keep switching partners by moving their agents around on the screen and they can switch strategies. Students compete to see who has gained the maximum points over all interactions.

Moreover, this model allows students to gain a grasp of computer programming. An input box on the screen allows students to edit and author strategies. Following are the strategies that students can choose from (see the STRATEGY-CHOICE choice button):

Strategies:  
Random - randomly cooperate or defect  
Cooperate - cooperate always  
Defect - defect always  
Go-by-Majority - Totals the number of times that the partner has defected. {against you or regardless of whom the partner had been playing against? Andrei, please choose and phrase}  If the majority of the time, up to that point, the partner has defected, defect.  Otherwise, cooperate.  
Tit-for-Tat - If the opponent cooperates this round cooperate next round.  If the opponent defects this round, defect next round.  Initially cooperate. {again-- against me or anyone?}  
Suspicious-Tit-for-Tat - The Tit-for-Tat strategy, except initially defects  
Tit-for-Two-Tats - If the opponent cooperates this round cooperate next round.  If the opponent defects two rounds in a row, defect the next round.  Initially cooperate.  
Pavlov - If the previous tactic (cooperate or defect) resulted in good {Andrei, what does this mean? Any positive point? The maximum possible?} points, stick with that tactic.  Otherwise, switch strategies.  Currently, "Success" happens when the partner cooperates, so this will keep switching if the opponent is always defecting.  Initially random.  
Unforgiving - Cooperate always unless the opponent defects once.  Upon opponent defection retaliate by defecting always.

Rules for the Iterated Prisoner's Dilemma

1.  This game will consist of matches in which each student competes against one opponent at a time.  Students may search for partners on their own.  Once paired, students will play 10 rounds, and then separate and look for other partners.

2.  Each round the student and his/her partner will earn or lose points by either cooperating (C) or defecting (D) based on the rules set in their individual client. Each students' strategy is either selected (the STRATEGY-CHOICE ) or edited on the HubNet client. {Andrei, can players change their strat while in a given interaction?}

3.  The point system is determined by the administrator, using the 4 sliders under "PAYOFF." That is, the administrator does not change the basic game but the value of the prizes and penalties.

## HOW TO USE IT

Buttons:

SETUP: Begin playing the iterated prisoner's dilemma.  
RERUN: All players set their total back to zero, and are ready to replay  
PLAY: When students pair up, they will play a user-defined number of turns before separating  
CREATE ANDROID: Creates a computer player to compete with students

Switches:

ALLOW-STRATEGY-CHANGE?: If on, students will be able to change their strategies. If off, they will be locked into the last working strategy in their client until it is turned on again.

Sliders:

COMPUTER STRATEGY - Select the computer's strategy from the drop-down list below.

Plots:

STRATEGY PLOTS: Shows the total points accumulated for each given strategy.  Once a strategy is modified by a student, the results of that student's success/failure will not be plotted on this.

C-D PLOT: Plots the total number of times players have cooperated, and the total number of times players have defected.

## THINGS TO NOTICE

Watch the plots for the different strategies.  Is there a serious difference between tit-for-tat and suspicious-tit-fot-tat?  Does one strategy really beat out the rest?

See how often students cooperate and defect.  In the long run, do students learn to switch to more trusting strategies?  Or do students all too often defect?

## THINGS TO TRY

1.  Experiment with playing different strategies one against the other.  Which strategies do the best?  Which do the worst?  Why?

2. Let it run for a while with hundreds of computer players to see which strategies win under these conditions.

3. There is a mode in which students cannot change their strategies.  Encourage them to code their own strategies, and then make them stick to that.  Which strategy seemed to work the best?

## EXTENDING THE MODEL

Students know the history of their last round, but not any of the previous rounds.  Think of a way to have clients be able to store the history of previous plays, and know what their current partner has done in other rounds.

## NETLOGO FEATURES

Note the use of the turtle variable `label` to display each turtle's average score in the view.

## RELATED MODELS

PD Basic, PD N-Person Iterated, PD Basic Evolutionary, PD 2-Person Iterated

## CREDITS AND REFERENCES
@#$#@#$#@
default
false
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
false
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

android
false
0
Polygon -7500403 true true 210 90 240 195 210 210 165 90
Circle -7500403 true true 110 3 80
Polygon -7500403 true true 105 88 120 193 105 240 105 298 135 300 150 210 165 300 195 298 195 240 180 193 195 88
Rectangle -7500403 true true 127 81 172 96
Rectangle -16777216 true false 119 33 181 56
Polygon -7500403 true true 90 90 60 195 90 210 135 90

box
false
0
Polygon -7500403 true true 150 285 285 225 285 75 150 135
Polygon -7500403 true true 150 135 15 75 150 15 285 75
Polygon -7500403 true true 15 75 15 225 150 285 150 135
Line -16777216 false 150 285 150 135
Line -16777216 false 150 135 15 75
Line -16777216 false 150 135 285 75

butterfly
false
0
Rectangle -7500403 true true 92 135 207 224
Circle -7500403 true true 158 53 134
Circle -7500403 true true 165 180 90
Circle -7500403 true true 45 180 90
Circle -7500403 true true 8 53 134
Line -16777216 false 43 189 253 189
Rectangle -7500403 true true 135 60 165 285
Circle -7500403 true true 165 15 30
Circle -7500403 true true 105 15 30
Line -7500403 true 120 30 135 60
Line -7500403 true 165 60 180 30
Line -16777216 false 135 60 135 285
Line -16777216 false 165 285 165 60

cactus
false
0
Rectangle -7500403 true true 135 30 175 177
Rectangle -7500403 true true 67 105 100 214
Rectangle -7500403 true true 217 89 251 167
Rectangle -7500403 true true 157 151 220 185
Rectangle -7500403 true true 94 189 148 233
Rectangle -7500403 true true 135 162 184 297
Circle -7500403 true true 219 76 28
Circle -7500403 true true 138 7 34
Circle -7500403 true true 67 93 30
Circle -7500403 true true 201 145 40
Circle -7500403 true true 69 193 40

car
false
0
Polygon -7500403 true true 300 180 279 164 261 144 240 135 226 132 213 106 203 84 185 63 159 50 135 50 75 60 0 150 0 165 0 225 300 225 300 180
Circle -16777216 true false 180 180 90
Circle -16777216 true false 30 180 90
Polygon -16777216 true false 162 80 132 78 134 135 209 135 194 105 189 96 180 89
Circle -7500403 true true 47 195 58
Circle -7500403 true true 195 195 58

cat
false
0
Line -7500403 true 285 240 210 240
Line -7500403 true 195 300 165 255
Line -7500403 true 15 240 90 240
Line -7500403 true 285 285 195 240
Line -7500403 true 105 300 135 255
Line -16777216 false 150 270 150 285
Line -16777216 false 15 75 15 120
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
Line -16777216 false 164 262 209 262
Line -16777216 false 223 231 208 261
Line -16777216 false 136 262 91 262
Line -16777216 false 77 231 92 261

cow skull
false
0
Polygon -7500403 true true 150 90 75 105 60 150 75 210 105 285 195 285 225 210 240 150 225 105
Polygon -16777216 true false 150 150 90 195 90 150
Polygon -16777216 true false 150 150 210 195 210 150
Polygon -16777216 true false 105 285 135 270 150 285 165 270 195 285
Polygon -7500403 true true 240 150 263 143 278 126 287 102 287 79 280 53 273 38 261 25 246 15 227 8 241 26 253 46 258 68 257 96 246 116 229 126
Polygon -7500403 true true 60 150 37 143 22 126 13 102 13 79 20 53 27 38 39 25 54 15 73 8 59 26 47 46 42 68 43 96 54 116 71 126

dog
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

ghost
false
0
Polygon -7500403 true true 30 165 13 164 -2 149 0 135 -2 119 0 105 15 75 30 75 58 104 43 119 43 134 58 134 73 134 88 104 73 44 78 14 103 -1 193 -1 223 29 208 89 208 119 238 134 253 119 240 105 238 89 240 75 255 60 270 60 283 74 300 90 298 104 298 119 300 135 285 135 285 150 268 164 238 179 208 164 208 194 238 209 253 224 268 239 268 269 238 299 178 299 148 284 103 269 58 284 43 299 58 269 103 254 148 254 193 254 163 239 118 209 88 179 73 179 58 164
Line -16777216 false 189 253 215 253
Circle -16777216 true false 102 30 30
Polygon -16777216 true false 165 105 135 105 120 120 105 105 135 75 165 75 195 105 180 120
Circle -16777216 true false 160 30 30

good airplane
false
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15
Circle -13840069 true false 156 156 108

good android
false
0
Polygon -7500403 true true 210 90 240 195 210 210 165 90
Circle -7500403 true true 110 3 80
Polygon -7500403 true true 105 88 120 193 105 240 105 298 135 300 150 210 165 300 195 298 195 240 180 193 195 88
Rectangle -7500403 true true 127 81 172 96
Rectangle -16777216 true false 119 33 181 56
Polygon -7500403 true true 90 90 60 195 90 210 135 90
Circle -13840069 true false 161 167 108

good box
false
0
Polygon -7500403 true true 150 285 270 225 270 90 150 150
Polygon -7500403 true true 150 150 30 90 150 30 270 90
Polygon -7500403 true true 30 90 30 225 150 285 150 150
Line -16777216 false 150 285 150 150
Line -16777216 false 150 150 30 90
Line -16777216 false 150 150 270 90
Circle -13840069 true false 170 178 108

good butterfly
false
0
Rectangle -7500403 true true 92 135 207 224
Circle -7500403 true true 158 53 134
Circle -7500403 true true 165 180 90
Circle -7500403 true true 45 180 90
Circle -7500403 true true 8 53 134
Line -16777216 false 43 189 253 189
Rectangle -7500403 true true 135 60 165 285
Circle -7500403 true true 165 15 30
Circle -7500403 true true 105 15 30
Line -7500403 true 120 30 135 60
Line -7500403 true 165 60 180 30
Line -16777216 false 135 60 135 285
Line -16777216 false 165 285 165 60
Circle -13840069 true false 156 171 108

good cactus
false
0
Rectangle -7500403 true true 135 30 175 177
Rectangle -7500403 true true 67 105 100 214
Rectangle -7500403 true true 217 89 251 167
Rectangle -7500403 true true 157 151 220 185
Rectangle -7500403 true true 94 189 148 233
Rectangle -7500403 true true 135 162 184 297
Circle -7500403 true true 219 76 28
Circle -7500403 true true 138 7 34
Circle -7500403 true true 67 93 30
Circle -7500403 true true 201 145 40
Circle -7500403 true true 69 193 40
Circle -13840069 true false 156 171 108

good car
false
0
Polygon -7500403 true true 285 208 285 178 279 164 261 144 240 135 226 132 213 106 199 84 171 68 149 68 129 68 75 75 15 150 15 165 15 225 285 225 283 174 283 176
Circle -16777216 true false 180 180 90
Circle -16777216 true false 30 180 90
Polygon -16777216 true false 195 90 135 90 135 135 210 135 195 105 165 90
Circle -7500403 true true 47 195 58
Circle -7500403 true true 195 195 58
Circle -13840069 true false 171 156 108

good cat
false
0
Line -7500403 true 285 240 210 240
Line -7500403 true 195 300 165 255
Line -7500403 true 15 240 90 240
Line -7500403 true 285 285 195 240
Line -7500403 true 105 300 135 255
Line -16777216 false 150 270 150 285
Line -16777216 false 15 75 15 120
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
Line -16777216 false 164 262 209 262
Line -16777216 false 223 231 208 261
Line -16777216 false 136 262 91 262
Line -16777216 false 77 231 92 261
Circle -13840069 true false 186 186 108

good cow skull
false
0
Polygon -7500403 true true 150 90 75 105 60 150 75 210 105 285 195 285 225 210 240 150 225 105
Polygon -16777216 true false 150 150 90 195 90 150
Polygon -16777216 true false 150 150 210 195 210 150
Polygon -16777216 true false 105 285 135 270 150 285 165 270 195 285
Polygon -7500403 true true 240 150 263 143 278 126 287 102 287 79 280 53 273 38 261 25 246 15 227 8 241 26 253 46 258 68 257 96 246 116 229 126
Polygon -7500403 true true 60 150 37 143 22 126 13 102 13 79 20 53 27 38 39 25 54 15 73 8 59 26 47 46 42 68 43 96 54 116 71 126
Circle -13840069 true false 156 186 108

good dog
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
Circle -13840069 true false 126 186 108

good ghost
false
0
Polygon -7500403 true true 30 165 13 164 -2 149 0 135 -2 119 0 105 15 75 30 75 58 104 43 119 43 134 58 134 73 134 88 104 73 44 78 14 103 -1 193 -1 223 29 208 89 208 119 238 134 253 119 240 105 238 89 240 75 255 60 270 60 283 74 300 90 298 104 298 119 300 135 285 135 285 150 268 164 238 179 208 164 208 194 238 209 253 224 268 239 268 269 238 299 178 299 148 284 103 269 58 284 43 299 58 269 103 254 148 254 193 254 163 239 118 209 88 179 73 179 58 164
Line -16777216 false 189 253 215 253
Circle -16777216 true false 102 30 30
Polygon -16777216 true false 165 105 135 105 120 120 105 105 135 75 165 75 195 105 180 120
Circle -16777216 true false 160 30 30
Circle -13840069 true false 156 171 108

good heart
false
0
Circle -7500403 true true 152 19 134
Polygon -7500403 true true 150 105 240 105 270 135 150 270
Polygon -7500403 true true 150 105 60 105 30 135 150 270
Line -7500403 true 150 270 150 135
Rectangle -7500403 true true 135 90 180 135
Circle -7500403 true true 14 19 134
Circle -13840069 true false 171 156 108

good key
false
0
Rectangle -7500403 true true 90 120 300 150
Rectangle -7500403 true true 270 135 300 195
Rectangle -7500403 true true 195 135 225 195
Circle -7500403 true true 0 60 150
Circle -16777216 true false 30 90 90
Circle -13840069 true false 156 171 108

good leaf
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195
Circle -13840069 true false 141 171 108

good monster
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
Circle -13840069 true false 141 141 108

good moon
false
0
Polygon -7500403 true true 160 7 68 36 10 108 12 186 64 250 119 271 190 274 266 239 192 233 137 216 98 185 89 132 95 77 117 51
Circle -13840069 true false 171 171 108

good star
false
0
Polygon -7500403 true true 151 1 185 108 298 108 207 175 242 282 151 216 59 282 94 175 3 108 116 108
Circle -13840069 true false 156 171 108

good target
true
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60
Circle -13840069 true false 163 163 95

good wheel
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
Circle -13840069 true false 156 156 108

heart
false
0
Circle -7500403 true true 152 19 134
Polygon -7500403 true true 150 105 240 105 270 135 150 270
Polygon -7500403 true true 150 105 60 105 30 135 150 270
Line -7500403 true 150 270 150 135
Rectangle -7500403 true true 135 90 180 135
Circle -7500403 true true 14 19 134

key
false
0
Rectangle -7500403 true true 90 120 300 150
Rectangle -7500403 true true 270 135 300 195
Rectangle -7500403 true true 195 135 225 195
Circle -7500403 true true 0 60 150
Circle -16777216 true false 30 90 90

leaf
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195

monster
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

moon
false
0
Polygon -7500403 true true 175 7 83 36 25 108 27 186 79 250 134 271 205 274 281 239 207 233 152 216 113 185 104 132 110 77 132 51

sick airplane
false
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15
Circle -2674135 true false 156 156 108

sick android
false
0
Polygon -7500403 true true 210 90 240 195 210 210 165 90
Circle -7500403 true true 110 3 80
Polygon -7500403 true true 105 88 120 193 105 240 105 298 135 300 150 210 165 300 195 298 195 240 180 193 195 88
Rectangle -7500403 true true 127 81 172 96
Rectangle -16777216 true false 119 33 181 56
Polygon -7500403 true true 90 90 60 195 90 210 135 90
Circle -2674135 true false 161 167 108

sick box
false
0
Polygon -7500403 true true 150 285 270 225 270 90 150 150
Polygon -7500403 true true 150 150 30 90 150 30 270 90
Polygon -7500403 true true 30 90 30 225 150 285 150 150
Line -16777216 false 150 285 150 150
Line -16777216 false 150 150 30 90
Line -16777216 false 150 150 270 90
Circle -2674135 true false 170 178 108

sick butterfly
false
0
Rectangle -7500403 true true 92 135 207 224
Circle -7500403 true true 158 53 134
Circle -7500403 true true 165 180 90
Circle -7500403 true true 45 180 90
Circle -7500403 true true 8 53 134
Line -16777216 false 43 189 253 189
Rectangle -7500403 true true 135 60 165 285
Circle -7500403 true true 165 15 30
Circle -7500403 true true 105 15 30
Line -7500403 true 120 30 135 60
Line -7500403 true 165 60 180 30
Line -16777216 false 135 60 135 285
Line -16777216 false 165 285 165 60
Circle -2674135 true false 156 171 108

sick cactus
false
0
Rectangle -7500403 true true 135 30 175 177
Rectangle -7500403 true true 67 105 100 214
Rectangle -7500403 true true 217 89 251 167
Rectangle -7500403 true true 157 151 220 185
Rectangle -7500403 true true 94 189 148 233
Rectangle -7500403 true true 135 162 184 297
Circle -7500403 true true 219 76 28
Circle -7500403 true true 138 7 34
Circle -7500403 true true 67 93 30
Circle -7500403 true true 201 145 40
Circle -7500403 true true 69 193 40
Circle -2674135 true false 156 171 108

sick car
false
0
Polygon -7500403 true true 285 208 285 178 279 164 261 144 240 135 226 132 213 106 199 84 171 68 149 68 129 68 75 75 15 150 15 165 15 225 285 225 283 174 283 176
Circle -16777216 true false 180 180 90
Circle -16777216 true false 30 180 90
Polygon -16777216 true false 195 90 135 90 135 135 210 135 195 105 165 90
Circle -7500403 true true 47 195 58
Circle -7500403 true true 195 195 58
Circle -2674135 true false 171 156 108

sick cat
false
0
Line -7500403 true 285 240 210 240
Line -7500403 true 195 300 165 255
Line -7500403 true 15 240 90 240
Line -7500403 true 285 285 195 240
Line -7500403 true 105 300 135 255
Line -16777216 false 150 270 150 285
Line -16777216 false 15 75 15 120
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
Line -16777216 false 164 262 209 262
Line -16777216 false 223 231 208 261
Line -16777216 false 136 262 91 262
Line -16777216 false 77 231 92 261
Circle -2674135 true false 186 186 108

sick cow skull
false
0
Polygon -7500403 true true 150 90 75 105 60 150 75 210 105 285 195 285 225 210 240 150 225 105
Polygon -16777216 true false 150 150 90 195 90 150
Polygon -16777216 true false 150 150 210 195 210 150
Polygon -16777216 true false 105 285 135 270 150 285 165 270 195 285
Polygon -7500403 true true 240 150 263 143 278 126 287 102 287 79 280 53 273 38 261 25 246 15 227 8 241 26 253 46 258 68 257 96 246 116 229 126
Polygon -7500403 true true 60 150 37 143 22 126 13 102 13 79 20 53 27 38 39 25 54 15 73 8 59 26 47 46 42 68 43 96 54 116 71 126
Circle -2674135 true false 156 186 108

sick dog
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
Circle -2674135 true false 126 186 108

sick ghost
false
0
Polygon -7500403 true true 30 165 13 164 -2 149 0 135 -2 119 0 105 15 75 30 75 58 104 43 119 43 134 58 134 73 134 88 104 73 44 78 14 103 -1 193 -1 223 29 208 89 208 119 238 134 253 119 240 105 238 89 240 75 255 60 270 60 283 74 300 90 298 104 298 119 300 135 285 135 285 150 268 164 238 179 208 164 208 194 238 209 253 224 268 239 268 269 238 299 178 299 148 284 103 269 58 284 43 299 58 269 103 254 148 254 193 254 163 239 118 209 88 179 73 179 58 164
Line -16777216 false 189 253 215 253
Circle -16777216 true false 102 30 30
Polygon -16777216 true false 165 105 135 105 120 120 105 105 135 75 165 75 195 105 180 120
Circle -16777216 true false 160 30 30
Circle -2674135 true false 156 171 108

sick heart
false
0
Circle -7500403 true true 152 19 134
Polygon -7500403 true true 150 105 240 105 270 135 150 270
Polygon -7500403 true true 150 105 60 105 30 135 150 270
Line -7500403 true 150 270 150 135
Rectangle -7500403 true true 135 90 180 135
Circle -7500403 true true 14 19 134
Circle -2674135 true false 171 156 108

sick key
false
0
Rectangle -7500403 true true 90 120 300 150
Rectangle -7500403 true true 270 135 300 195
Rectangle -7500403 true true 195 135 225 195
Circle -7500403 true true 0 60 150
Circle -16777216 true false 30 90 90
Circle -2674135 true false 156 171 108

sick leaf
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195
Circle -2674135 true false 141 171 108

sick monster
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
Circle -2674135 true false 141 141 108

sick moon
false
0
Polygon -7500403 true true 160 7 68 36 10 108 12 186 64 250 119 271 190 274 266 239 192 233 137 216 98 185 89 132 95 77 117 51
Circle -2674135 true false 171 171 108

sick star
false
0
Polygon -7500403 true true 151 1 185 108 298 108 207 175 242 282 151 216 59 282 94 175 3 108 116 108
Circle -2674135 true false 156 171 108

sick target
true
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60
Circle -2674135 true false 163 163 95

sick wheel
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
Circle -2674135 true false 156 156 108

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

@#$#@#$#@
NetLogo 5.0beta5
@#$#@#$#@
setup
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
INPUTBOX
13
182
438
409
strategy
null
1
0
String

MONITOR
17
13
136
62
You are a:
NIL
3
1

MONITOR
17
64
136
113
Your Total:
NIL
3
1

MONITOR
182
13
286
62
Your Score:
NIL
3
1

MONITOR
290
13
632
62
Your History:
NIL
3
1

MONITOR
290
63
632
112
Partner's History:
NIL
3
1

MONITOR
182
64
285
113
Partner's Score:
NIL
3
1

CHOOSER
13
134
175
179
strategy-choice
strategy-choice
"random" "cooperate" "defect" "go-by-majority" "tit-for-tat" "suspicious-tit-for-tat" "tit-for-two-tats" "pavlov" "unforgiving"
0

MONITOR
13
414
501
463
Errors:
NIL
3
1

BUTTON
508
185
578
218
Up
NIL
NIL
1
T
OBSERVER
NIL
W

BUTTON
439
218
509
251
Left
NIL
NIL
1
T
OBSERVER
NIL
A

BUTTON
577
218
649
251
Right
NIL
NIL
1
T
OBSERVER
NIL
D

BUTTON
508
250
578
283
Down
NIL
NIL
1
T
OBSERVER
NIL
X

BUTTON
439
185
509
218
Up-Left
NIL
NIL
1
T
OBSERVER
NIL
Q

BUTTON
577
185
649
218
Up-Right
NIL
NIL
1
T
OBSERVER
NIL
E

BUTTON
439
250
509
283
Down-Left
NIL
NIL
1
T
OBSERVER
NIL
Z

BUTTON
577
250
649
283
Down-Right
NIL
NIL
1
T
OBSERVER
NIL
C

TEXTBOX
519
297
890
526
A strategy returns COOPERATE or DEFECT\n\nThe following variables are available:\n\n* play-history - a list of your moves, as \"D\" and \"C\"\n* play-partner-history - a list of your partner's moves as \"D\" and \"C\"\n\n\nThe following NetLogo primitives may be useful:\n\n* length\n* item \n* empty\n* filter \n
11
0.0
0

MONITOR
290
113
632
162
Points:
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

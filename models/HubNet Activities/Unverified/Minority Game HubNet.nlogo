;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variable and Breed declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

globals
[
  ;; these may be different because the memory of players and androids may be different
  player-history    ;; the history of which number was the minority (encoded into a binary number), seen by the players
  android-history   ;; the history of which number was the minority (encoded into a binary number), seen by the androids

  minority          ;; the current number in the minority
  avg-score         ;; keeps track of all turtles' average score
  stdev-score       ;; keeps track of the standard deviation of the turtles' scores
  avg-success       ;; keeps track of all turtles' average success

  ;; lists used to create the various turtles
  shape-names        ;; shapes available for players
  colors             ;; colors available for players
  color-names        ;; names of colors available for players
  used-shape-colors  ;; shape-color combinations used

  ;; quick start instructions and variables
  quick-start  ;; current quickstart instruction displayed in the quickstart monitor
  qs-item      ;; index of the current quickstart instruction
  qs-items     ;; list of quickstart instructions

  score-list ;; for plotting
  success-list ;; for plotting
]

turtles-own
[
  score          ;; each turtle's score
  choice         ;; each turtle's current choice either 1 or 0
]

;; each client is represented by a player turtle
breed [ players player ]
players-own
[
  user-id        ;; name entered by the user in the client
  chosen-sides?  ;; true/false to tell if player has made current choice we don't move to the next round until everyone has chosen
  choices-made   ;; the number of choices each turtle has made
]

;; androids are players in the game that are controlled by the computer
breed [ androids android  ]
androids-own
[
  strategies        ;; each android's strategies (a list of lists)
  current-strategy  ;; each android's current strategy (index for the above list)
  strategies-scores ;; the accumulated virtual scores for each of the turtle's strategies
]

;;;;;;;;;;;;;;;;;;;;;
;; Setup Procedures
;;;;;;;;;;;;;;;;;;;;;

;; startup of the model
to startup
  clear-all
  setup
  hubnet-reset
end

;; setup for the overall program, will require clients to re-login
to setup
  ;; prevent an infinite loop from occurring in assign-strategies
  if (android-memory = 1 and strategies-per-android > 4 )
  [
    user-message word "You need to increase the memory variable or\n"
                      "decrease the strategies-per-agent variable"
    stop
  ]
  setup-quick-start
  initialize-system
  initialize-androids
  ask patches with [pxcor = 0 ]
  [ set pcolor white ]
  ask players
  [
    clear-my-data
    set ycor 0
  ]
  set score-list map [ [score] of ? ] sort turtles
  update-success-list
  clear-all-plots
  reset-ticks
end

;; initializes system variables
to initialize-system
  ;; when generating a random history to start out with
  ;; first fill the longer memory then take a subsection
  ;; of that memory and give it to the other group
  let temp-history random (2  ^ (max list player-memory android-memory))

  ifelse player-memory >= android-memory
  [
    set player-history temp-history
    set android-history (reduce-memory (full-history temp-history player-memory) android-memory)
  ]
  [
    set android-history temp-history
    set player-history (reduce-memory (full-history temp-history android-memory) player-memory)
  ]

  reset-ticks
  set avg-score 0
  set stdev-score 0

  set shape-names [ "airplane" "bug" "butterfly" "car" "fish" "monster"
                    "star" "turtle" "bird" "crown" "ufo" "sun" "train" ]
  set colors      [ white   brown   yellow   green
                    sky   violet   orange   pink  red ]
  set color-names [ "white" "brown" "yellow"  "green"
                   "blue" "purple" "orange" "pink" "red" ]
  set used-shape-colors []
end

;; given a list reduce the length to that of the given
;; memory and return that list as a decimal number
to-report reduce-memory [history memory]
    report decimal sublist history (length history - memory) (length history - 1)
end

;; reports the history in binary format (with padding if needed)
to-report full-history [ agent-history agent-memory ]
  let full binary agent-history
  while [length full < agent-memory]
    [ set full fput 0 full ]
  report full
end

;; converts a decimal number to a binary number (stored in a list of 0's and 1's)
to-report binary [ decimal-num ]
  let binary-num []
  loop
    [ set binary-num fput (decimal-num mod 2) binary-num
      set decimal-num int (decimal-num / 2)
      if (decimal-num = 0)
        [ report binary-num ] ]
end

;; converts a binary number (stored in a list of 0's and 1's) to a decimal number
to-report decimal [ binary-num ]
  report reduce [(2 * ?1) + ?2] binary-num
end

;; remove existing turtles and create number-of-androids androids
to initialize-androids
  ask androids
    [ die ]
  set-default-shape androids "person"
  create-androids (number-of-participants - count players)
  [
    set color gray
    set xcor random-xcor
    set heading 0
    assign-strategies
    set current-strategy random strategies-per-android
    set choice item android-history (item current-strategy strategies)
    set score 0
    set strategies-scores n-values strategies-per-android [0]
  ]
  let num-picked-zero count turtles with [choice = 0]
  ifelse (num-picked-zero <= (count turtles - 1) / 2)
    [ set minority 0 ]
    [ set minority 1 ]
  set score-list map [ [score] of ? ] sort turtles
  setup-plots
end

;; gives the androids their allotted number of unique strategies
to assign-strategies  ;; android procedure
  let temp-strategy false
  set strategies []
  repeat strategies-per-android
  [
    ;; make sure there are no duplicate strategies in the list
    set temp-strategy create-strategy
    while [ member? temp-strategy strategies ]
    [ set temp-strategy create-strategy ]
    set strategies fput temp-strategy strategies
  ]
end

;; creates a strategy (a binary number stored in a list of
;; length 2 ^ android-memory)
to-report create-strategy
  report n-values (2 ^ android-memory) [random 2]
end

;; reset a player to some initial values
to clear-my-data  ;; players procedure
  set xcor random-xcor
  set choice random 2
  set score 0
  set chosen-sides? false
  set choices-made 0
  send-info-to-clients
  update-client
end

;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures
;;;;;;;;;;;;;;;;;;;;;;

to go
  every 0.1
  [
     ;; get commands and data from the clients
     listen-clients
     ;; determine if the system should be updated (advanced in time)
     ;; that is, if every player has chosen a side for this round
     if any? turtles and not any? players with [ not chosen-sides? ]
     [
       update-system
       update-scores-and-strategies
       advance-system
       update-choices
       update-success-list
       set score-list map [ [score] of ? ] sort turtles
       update-plots
       let scores [score] of turtles
       ask turtles [ move max scores min scores ]
       ask players
       [
         set chosen-sides? false
         update-client
       ]
       tick
    ]
  ]
end

to update-success-list
  set success-list map [ [ score / choices-made ] of ? ] sort players with [ choices-made > 0 ]
  if ticks > 0
  [ set success-list sentence success-list map [ [score / ticks] of ? ] sort androids ]
end

;; updates system variables such as minority, avg-score, and stdev-score globals
to update-system
  let num-picked-zero count turtles with [choice = 0]
  ifelse num-picked-zero <= (count turtles - 1) / 2
  [ set minority 0 ]
  [ set minority 1 ]
  set-current-plot "Number Picking Zero"
  plot num-picked-zero
  set avg-score mean [score] of turtles
  set stdev-score standard-deviation [score] of turtles
  if ticks > 0
  [ set avg-success mean (sentence [score / ticks] of androids [score / choices-made] of players with [ choices-made > 0 ]) ]
end

;; ask all participants to update their strategy and scores
to update-scores-and-strategies
  ask androids
    [ update-androids-scores-and-strategies ]
  ask players
    [ update-score ]
end

;; updates android's score and their strategies' virtual scores
to update-androids-scores-and-strategies  ;; androids procedure
  ;; here we use MAP to simultaneously walk down both the list
  ;; of strategies, and the list of those strategies' scores.
  ;; ?1 is the current strategy, and ?2 is the current score.
  ;; For each strategy, we check to see if that strategy selected
  ;; the minority.  If it did, we increase its score by one,
  ;; otherwise we leave the score alone.
  set strategies-scores (map
    [ ifelse-value (item android-history ?1 = minority)
        [?2 + 1]
        [?2] ]
    strategies strategies-scores)
  let max-score max strategies-scores
  let max-strategies []
  let counter 0
  ;; this picks a strategy with the largest virtual score
  foreach strategies-scores
  [
    if ? = max-score
    [ set max-strategies lput counter max-strategies ]
    set counter counter + 1
  ]
  set current-strategy one-of max-strategies
  update-score
end

;; if the turtle is in the minority, increase its score
to update-score  ;; turtle procedure
  if choice = minority
    [ set score score + 1 ]
end

;; advances the system forward in time and updates the history
to advance-system
  ;; remove the oldest entry in the memories and place the new one on the end
  set player-history decimal (lput minority but-first full-history player-history player-memory)
  set android-history decimal (lput minority but-first full-history android-history android-memory)
  ;; send the updated info to the clients
  ask players
  [ hubnet-send user-id "history" full-history player-history player-memory ]
end

;; ask all participants to update their choice
to update-choices
  update-androids-choices
  ask players [ update-client ]
end

;; ask the androids to pick a new choice
to update-androids-choices
  ask androids
    [ set choice (item android-history (item current-strategy strategies)) ]
end

;; move turtles according to their success (a visual aid to see their collective behavior)
to move [low-score high-score]
  if low-score != high-score
  [ set ycor (((score - low-score) / (high-score - low-score )) * (world-height - 1) ) + min-pycor ]
  ifelse choice = 0
  [
    if xcor > 0
    [ set xcor random-float (min-pxcor + 1) - 1 ]
  ]
  [
    if xcor < 0
    [ set xcor random-float (max-pxcor - 1) + 1 ]
  ]
end

;;;;;;;;;;;;;;;;;;;;;;
;; HubNet Procedures
;;;;;;;;;;;;;;;;;;;;;;

;; listen for hubnet client activity
to listen-clients
  while [hubnet-message-waiting?]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
     [ execute-create ]
     [ ifelse hubnet-exit-message?
       [
         ;; when players log out we don't kill off the turtles
         ;; instead we just turn them into androids since it's
         ;; important to have an odd number of players. This keeps
         ;; the total population constant
         ask players with [user-id = hubnet-message-source]
         [
           set breed androids
           set color gray
           assign-strategies
           set current-strategy random strategies-per-android
           set choice item android-history (item current-strategy strategies)
           set strategies-scores n-values strategies-per-android [0]
           set score 0
           set size 1
           display
         ]
       ]
       [
         if hubnet-message-tag = "0"
           [ choose-value 0 ]
         if hubnet-message-tag = "1"
           [ choose-value 1 ]
       ]
     ]
   ]
end

;; create a client player upon login
to execute-create
  ;; to make sure that we always have an odd number of
  ;; participants so there is always a true minority
  ;; so just change one of the androids into a player
  ;; (you can only create an odd number of androids)
  ;; if there aren't enough androids make two and update
  ;; the slider.
  if not any? androids
  [
    create-androids 2
    [
      set heading 0
      set xcor random-xcor
    ]
    set number-of-participants number-of-participants + 2
  ]
  ask one-of androids
  [
    set breed players
    set user-id hubnet-message-source
    set size 2
    set-unique-shape-and-color
    clear-my-data
  ]
  display
end

;; assigns a shape that is not currently in use to
;; a player turtle
to set-unique-shape-and-color  ;; player procedure
  let max-possible-codes (length colors * length shape-names)
  let code random max-possible-codes
  while [member? code used-shape-colors and count turtles < max-possible-codes]
    [ set code random max-possible-codes ]
  set used-shape-colors (lput code used-shape-colors)
  set shape item (code mod length shape-names) shape-names
  set color item (code / length shape-names) colors
end

;; to tell the clients what they look like
to send-info-to-clients  ;; player procedure
  hubnet-send user-id "You are a:" identity
end

;; report the string version of the turtle's identity (color + shape)
to-report identity  ;; turtle procedure
  report (word (color-string color) " " shape)
end

;; report the string version of the turtle's color
to-report color-string [color-value]
  report item (position color-value colors) color-names
end

;; send information to the clients
to update-client  ;; player procedure
  hubnet-send user-id "chosen-sides?" chosen-sides?
  hubnet-send user-id "last choice" choice
  hubnet-send user-id "current choice" choice
  hubnet-send user-id "history" full-history player-history player-memory
  hubnet-send user-id "score" score
  hubnet-send user-id "success rate" precision ifelse-value (choices-made > 0) [ score / choices-made ] [ 0 ] 2
end

;; the client chooses 0 or 1
to choose-value [ value-chosen ]
  ask players with [user-id = hubnet-message-source]
  [ if not chosen-sides?
    [
      hubnet-send user-id "last choice" choice
      set choice value-chosen
      set chosen-sides? true
      set choices-made choices-made + 1
      hubnet-send user-id "current choice" choice
      hubnet-send user-id "chosen-sides?" chosen-sides?
    ]
  ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Quick Start Procedures
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; instructions to quickly setup the model, and clients to run this activity
to setup-quick-start
  set qs-item 0
  set qs-items
  [
    "Teacher: Follow these directions to run the HubNet activity."
    "Optional: Zoom In (see Tools in the Menu Bar)"
    "Optional: Change any of the settings...."
      "If you did change settings, press the SETUP button."
    "Teacher: Press the LOG-IN button."
    "Everyone: Open up a HubNet Client on your machine and..."
      "choose a user-name and..."
        "connect to this activity."
    "Teacher: Once everyone has started their client..."
      "press the LOG-IN button, then press GO."
    "Everyone: Watch your clock and choose 0 or 1."

    "Teacher: To rerun the activity with the same group,..."
      "stop the model by pressing the GO button, if it is on."
        "Change any of the settings that you would like."
          "Press the RE-RUN button."
    "Teacher: Restart the simulation by pressing the GO button again."

    "Teacher: To start the simulation over with a new group,..."
      "stop the model by pressing the GO button, if it is on..."
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
to view-previous
  set qs-item qs-item - 1
  if qs-item < 0
  [ set qs-item 0 ]
  set quick-start (item qs-item qs-items)
end
@#$#@#$#@
GRAPHICS-WINDOW
429
96
964
652
17
17
15.0
1
10
1
1
1
0
0
0
1
-17
17
-17
17
1
1
1
ticks
30.0

BUTTON
117
118
187
151
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
263
118
333
151
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

SLIDER
116
10
295
43
number-of-participants
number-of-participants
3
501
55
2
1
NIL
HORIZONTAL

SLIDER
215
44
393
77
player-memory
player-memory
1
12
7
1
1
NIL
HORIZONTAL

SLIDER
36
44
214
77
android-memory
android-memory
1
12
3
1
1
NIL
HORIZONTAL

PLOT
220
411
426
561
Success rate
time
success rate
0.0
1.0
0.0
0.05
true
true
"" ""
PENS
"max" 1.0 0 -2674135 true "" "if length success-list > 0 [ plot max success-list ]"
"min" 1.0 0 -13345367 true "" "if length success-list > 0 [ plot min success-list ]"
"avg" 1.0 0 -10899396 true "" "if length success-list > 0 [ plot mean success-list ]"

PLOT
12
411
218
561
Number Picking Zero
time
number
0.0
1.0
0.0
25.0
true
false
"ifelse any? turtles\n    [ set-plot-y-range 0 count turtles ]\n    [ set-plot-y-range 0 1 ]" ""
PENS
"number" 1.0 0 -10899396 true "" ""

MONITOR
143
157
316
202
history
full-history player-history player-memory
3
1
11

PLOT
12
259
218
409
Scores
time
score
0.0
1.0
0.0
1.0
true
true
"" ""
PENS
"max" 1.0 0 -2674135 true "" "if length score-list > 0 [ plot max score-list ]"
"min" 1.0 0 -13345367 true "" "if length score-list > 0 [ plot min score-list ]"
"avg" 1.0 0 -10899396 true "" "if length score-list > 0 [ plot mean score-list ]"

PLOT
220
259
426
409
Success Rates
score
number
0.0
1.0
0.0
10.0
true
true
"set-histogram-num-bars 25" ""
PENS
"default" 0.02 1 -16777216 false "" "if length success-list > 0 [ histogram success-list ]"

SLIDER
35
78
213
111
strategies-per-android
strategies-per-android
1
15
5
1
1
NIL
HORIZONTAL

MONITOR
429
10
817
55
Quick Start Instructions-More in Info Window
quick-start
3
1
11

BUTTON
429
61
577
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
734
61
817
94
NEXT>>>
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
649
61
733
94
<<<PREV
view-previous
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
47
211
218
256
high score
max [score] of turtles
3
1
11

MONITOR
220
211
391
256
low score
min [score] of turtles
3
1
11

BUTTON
190
118
260
151
login
listen-clients
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

Minority Game is a simplified model of an economic market.  In each round agents choose to join one of two sides, 0 or 1. Those on the minority side at the end of a round earn a point.  This game is inspired by the "El Farol" bar problem.

Each round, the live participants must choose 0 or 1. They can view their choice history for a specified number of previous turns, and may employ a finite set of strategies to make their decision. The record available to them shows which side, 0 or 1, was in the minority in previous rounds.

This HubNet version of the model allows players to play against each other and a set of androids.  The androids' intelligence (and thus the difficulty of the game) can be increased through the ANDROID-MEMORY slider.

## HOW IT WORKS

Each player begins with a score of 0 and must choose a side, 0 or 1, during each round. The round ends when all the human participants have made a choice.

Each computer agent begins with a score of 0 and STRATEGIES-PER-AGENT strategies. Each of these strategies is a string of 0 and 1 choices, such as [0 1 1 1 0 0 1] that together represent the agents' possible plan of action (first choose 0, next time choose 1, next time choose 1, etc.). Initially, they choose a random one of these strategies to use. If their current strategy correctly predicted whether 0 or 1 would be the minority, they add one point to their score.  Each strategy also earns virtual points according to whether it would have been correct or not.  From then on, the agents will use their strategy with the highest virtual point total to predict whether they should select 0 or 1. Thus, for each android, the "fittest" strategies survive.

This strategy consists of a list of 1's and 0's that is 2^ANDROID-MEMORY long.  The choice the computer agent then makes is based off of the history of past choices.  This history is also a list of 1's and 0's that is ANDROID-MEMORY long, but it is encoded into a binary number.  The binary number is then used as an index into the strategy list to determine the choice.

This means that if there are only computer agents and no human participants, once the number of computer agents, the number of strategies, and the length of the historical record are chosen, all parameters are fixed and the behavior of the system is of interest.

## HOW TO USE IT

Quickstart Instructions:

Teacher: Follow these directions to run the HubNet activity.  
Optional: Zoom In (see Tools in the Menu Bar)  
Optional: Change any of the settings.  If you did change settings, press the SETUP button.  
Teacher: Press the LOGIN button

Everyone: Open up a HubNet Client on your machine and choose a username and connect to this activity.

Teacher: When everyone is logged in press the LOGIN button again and press the GO button when you are ready to start.

Everyone: Choose 0 or 1, when everyone has chosen the view will update to show the relative scores of all the players and androids

Teacher: To run the activity again with the same group, stop the model by pressing the GO button, if it is on. Change any of the settings that you would like.  
Press the SETUP button.  
Teacher: Restart the simulation by pressing the GO button again.

Teacher: To start the simulation over with a new group, have all the clients log out (or boot them using the KICK button in the Control Center) and press SETUP

Buttons:

SETUP: Resets the simulation according to the parameters set by the sliders all logged-in clients will remain logged-in but their scores will be reset to 0  
LOGIN: Allows clients to log in but not to start playing the game.  
GO: Starts and stops the model.

Sliders:

NUMBER-OF-PARTICIPANTS: sets the total number of participants in the game, which includes androids and human participants, as clients log in androids will automatically turn into human players.  This is to ensure that there is always an odd number of participants in the world so there is always a true minority.  
PLAYER-MEMORY: The length of the history the players can view to help choose sides.  
ANDROID-MEMORY: Sets the length of the history which the computer agents use to predict their behavior.  One gets most interesting between 3 and 12, though there is some interesting behavior at 1 and 2.  Note that when using an ANDROID-MEMORY of 1, the  
STRATEGIES-PER-AGENT needs to be 4 or less.  
STRATEGIES-PER-AGENT: Sets the number of strategies each computer agent has in their toolbox.  Five is typically a good value.  However, this can be changed for investigative purposes using the slider, if desired.

Monitors:

HIGH SCORE and LOW SCORE show the maximum and minimum scores.  
HISTORY: shows the most recent minority values.  The number of values shown is determined by the PLAYER-MEMORY slider.

Plots:

SCORES: displays the minimum, maximum, and average scores over time  
SUCCESS RATES HISTOGRAM: a histogram of the successes per attempts for players and androids.  
NUMBER PICKING ZERO: plots the number of players and androids that picked zero during the last round  
SUCCESS RATE: displays the minimum, maximum, and average success rate over time

Quickstart

NEXT >>> - shows the next quick start instruction  
<<< PREVIOUS - shows the previous quick start instruction  
RESET INSTRUCTIONS - shows the first quick start instruction

Client Interface

Buttons:

0: press this button if you wish to choose 0 for a particular round.  
1: press this button if you wish to choose 1 for a particular round.

Monitors:

YOU ARE A: displays the shape and color of your turtle in the view  
SCORE: displays how many times you have chosen a value that has been in the minority  
SUCCESS RATE: the number of times you have been in the minority divided by the number of selections you have participated in.  
LAST CHOICE: the value you chose in the last round  
HISTORY: the values that were in the minority in the most recent rounds  
CURRENT CHOICE: the value that you have chosen for this current round  
CHOSEN-SIDES?: Tells you whether or not you have chosen this round

## THINGS TO NOTICE

There are two extremes possible for each turn: the size of the minority is 1 agent or (NUMBER-OF-AGENTS - 1)/2 agents (since NUMBER-OF-AGENTS is always odd).  The former would represent a "wasting of resources" while the latter represents a situation which is more "for the common good."  However, each agent acts in an inherently selfish manner, as they care only if they and they alone are in the minority.  Nevertheless, the latter situation is prevalent in the system without live players.  Does this represent unintended cooperation between agents, or merely coordination and well developed powers of prediction?

The agents in the view move according to how successful they are relative to the mean success rate.  After running for about 100 time steps (at just about any parameter setting), how do the fastest and slowest agents compare?  What does this imply?

Playing against others, what strategies seem to be the most effective?  What would happen if you simply chose randomly?

Look at the plot "Success Rates." As the game runs, the success rates converge. Can you explain this? At the time, the graph lines in the plot "Scores" diverge. Why is that?

## THINGS TO TRY

What strategy works to maximize your own score?

Would you perform better against only computer agents than against humans?

What strategy works better to try to reach social equity?

## EXTENDING THE MODEL

Maybe you could add computer agents with different strategies, or more dynamically evolutionary strategies.  Could you figure out a strategy that works the best against these computer agents?  You could code in multiple dynamic strategies that play against each other. Who would emerge victorious?

## NETLOGO FEATURES

One feature which was instrumental to this program being feasible was the `n-values` primitive.  When setting up strategies for each computer agent, they are binary numbers (stored in lists) of 2^ANDROID-MEMORY values. If this was done by starting with an empty list and using `fput` 2^ANDROID-MEMORY times, for each agent and for each strategy, during setup you would need to use `fput` N*S*(2^ANDROID-MEMORY) times.  Using `n-values` sped this up by about 2 or 3 orders of magnitude.

The list primitives `map` and `reduce` were also used to simplify code.

## RELATED MODELS

Prisoner's Dilemma  
Altruism  
Cooperation  
El Farol  
Restaurants

## CREDITS AND REFERENCES

Original implementation: Daniel B. Stouffer, for the Center for Connected Learning and Computer-Based Modeling.

This model was based upon studies by Dr. Damien Challet et al.  Information can be found on the web at http://www.unifr.ch/econophysics/minority/

Challet, D. and Zhang, Y.-C. Emergence of Cooperation and Organization in an Evolutionary Game. Physica A 246, 407 (1997).

Zhang, Y.-C. Modeling Market Mechanism with Evolutionary Games. Europhys. News 29, 51 (1998).
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

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

arrow 2
true
0
Polygon -7500403 true true 150 0 0 150 120 150 120 293 180 293 180 150 300 150

bird
false
0
Polygon -7500403 true true 135 165 90 270 120 300 180 300 210 270 165 165
Rectangle -7500403 true true 120 105 180 237
Polygon -7500403 true true 135 105 120 75 105 45 121 6 167 8 207 25 257 46 180 75 165 105
Circle -16777216 true false 128 21 42
Polygon -7500403 true true 163 116 194 92 212 86 230 86 250 90 265 98 279 111 290 126 296 143 298 158 298 166 296 183 286 204 272 219 259 227 235 240 241 223 250 207 251 192 245 180 232 168 216 162 200 162 186 166 175 173 171 180
Polygon -7500403 true true 137 116 106 92 88 86 70 86 50 90 35 98 21 111 10 126 4 143 2 158 2 166 4 183 14 204 28 219 41 227 65 240 59 223 50 207 49 192 55 180 68 168 84 162 100 162 114 166 125 173 129 180

boat
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

crown
false
0
Rectangle -7500403 true true 45 165 255 240
Polygon -7500403 true true 45 165 30 60 90 165 90 60 132 166 150 60 169 166 210 60 210 165 270 60 255 165
Circle -16777216 true false 222 192 22
Circle -16777216 true false 56 192 22
Circle -16777216 true false 99 192 22
Circle -16777216 true false 180 192 22
Circle -16777216 true false 139 192 22

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

spacecraft
true
0
Polygon -7500403 true true 150 0 180 135 255 255 225 240 150 180 75 240 45 255 120 135

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

sun
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

target
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60

train
false
0
Rectangle -7500403 true true 30 105 240 150
Polygon -7500403 true true 240 105 270 30 180 30 210 105
Polygon -7500403 true true 195 180 270 180 300 210 195 210
Circle -7500403 true true 0 165 90
Circle -7500403 true true 240 225 30
Circle -7500403 true true 90 165 90
Circle -7500403 true true 195 225 30
Rectangle -7500403 true true 0 30 105 150
Rectangle -16777216 true false 30 60 75 105
Polygon -7500403 true true 195 180 165 150 240 150 240 180
Rectangle -7500403 true true 135 75 165 105
Rectangle -7500403 true true 225 120 255 150
Rectangle -16777216 true false 30 203 150 218

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
Polygon -7500403 true true 180 135 75 135 75 210 225 210 225 165 195 165
Polygon -8630108 true false 210 210 195 225 180 210
Polygon -8630108 true false 120 210 105 225 90 210

turtle
true
0
Polygon -10899396 true false 215 204 240 233 246 254 228 266 215 252 193 210
Polygon -10899396 true false 195 90 225 75 245 75 260 89 269 108 261 124 240 105 225 105 210 105
Polygon -10899396 true false 105 90 75 75 55 75 40 89 31 108 39 124 60 105 75 105 90 105
Polygon -10899396 true false 132 85 134 64 107 51 108 17 150 2 192 18 192 52 169 65 172 87
Polygon -10899396 true false 85 204 60 233 54 254 72 266 85 252 107 210
Polygon -7500403 true true 119 75 179 75 209 101 224 135 220 225 175 261 128 261 81 224 74 135 88 99

ufo
false
0
Polygon -1 true false 0 150 15 180 60 210 120 225 180 225 240 210 285 180 300 150 300 135 285 120 240 105 195 105 150 105 105 105 60 105 15 120 0 135
Polygon -16777216 false false 105 105 60 105 15 120 0 135 0 150 15 180 60 210 120 225 180 225 240 210 285 180 300 150 300 135 285 120 240 105 210 105
Polygon -7500403 true true 60 131 90 161 135 176 165 176 210 161 240 131 225 101 195 71 150 60 105 71 75 101
Circle -16777216 false false 255 135 30
Circle -16777216 false false 180 180 30
Circle -16777216 false false 90 180 30
Circle -16777216 false false 15 135 30
Circle -7500403 true true 15 135 30
Circle -7500403 true true 90 180 30
Circle -7500403 true true 180 180 30
Circle -7500403 true true 255 135 30
Polygon -16777216 false false 150 59 105 70 75 100 60 130 90 160 135 175 165 175 210 160 240 130 225 100 195 70

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

wolf
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
VIEW
328
10
706
388
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
-17
17
-17
17

BUTTON
74
234
155
267
0
NIL
NIL
1
T
OBSERVER
NIL
NIL

MONITOR
38
22
276
71
You are a:
NIL
3
1

MONITOR
117
179
304
228
chosen-sides?
NIL
3
1

MONITOR
117
127
305
176
history
NIL
3
1

MONITOR
52
74
151
123
score
NIL
3
1

MONITOR
154
74
254
123
success rate
NIL
3
1

MONITOR
7
127
114
176
last choice
NIL
3
1

MONITOR
7
179
114
228
current choice
NIL
3
1

BUTTON
158
234
239
267
1
NIL
NIL
1
T
OBSERVER
NIL
NIL

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

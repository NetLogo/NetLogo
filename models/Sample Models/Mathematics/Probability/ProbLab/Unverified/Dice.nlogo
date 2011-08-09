globals [
          patches-in-set-block     ;; agent-set of patches where you set the dice
          patches-in-guess-block   ;; agent-set of patches where the computer guesses the dice
          dice-list            ;; list of shape names of all six dice
          instructions         ;; the user instructions that appear in the monitor on the top left

     ;; Booleans to preempt run-time errors and/or user confusions
          am-I-set-up?
          combi-exists?
          abort-pick-dice?
          stop?
          got-single-success?
          permis-success?
          revealed?

     ;; Booleans used in identifying favored events amongst all outcomes
          all-combi-match?     ;; used to mark whether an outcome is the same as the original combination
          all-permis-match?  ;; used to mark whether an outcome is the same as the original combination
                             ;; or its permutation

     ;; variables that record the dice set by the user
          dice-choices ;; list of dice face values of the combination that the user set

     ;; variables and Lists that record different interpretations of the outcomes
          count-steps                           ;; counts the number of outcomes in the current sample
          #combi-successes-per-sample-list      ;; list of outcomes per sample that were exactly like
                                                ;; the original combination
          #permis-successes-per-sample-list   ;; list of outcomes per sample that are the original combination
                                              ;; or its permutation
          samples-counter                       ;; how many sample have elapsed in an experiment
          count-combi-successes       ;; counts up hits under the combi condition
          count-permis-successes      ;; counts up hits under the permutation condition
          mean-combi-per-sample       ;; mean number of same-order outcomes per sample
          mean-permis-per-sample      ;; mean number of either-order outcomes per sample

     ;; Boolean associated with the Hide/Reveal button
          hide?
        ]

breed [ frames frame ] ;; turtles that are the black outlines around the two dice patches
breed [ dice a-die]   ;; turtles that are the dice face values

patches-own [ my-guess-block-buddy ] ;; each die in the set-block has a buddy die in the guess-block

dice-own [
          my-shape               ;; there are six different shapes that are the 1-thru-6 dice faces
          my-set-number          ;; the numerical value associated with the dice face you pick
          my-current-number      ;; the numerical value associated with the dice face the model guessed
         ]

to startup
    initialize
    ask patches [ set pcolor white ]
    set instructions "Hi! Please read the Info tab to learn about this model, or just press Setup."
end

to initialize
  ca
  set dice-list [ "one" "two" "three" "four" "five" "six" ]
  set all-combi-match? false
  set all-permis-match?  false
  set combi-exists? false
  set am-I-set-up? false
  set stop? false
  set got-single-success? false
  set revealed? false
  set bars? true
  set abort-pick-dice? false
  set hide? true
  set dice-choices []
  set #combi-successes-per-sample-list []
  set #permis-successes-per-sample-list []
end

to setup
  set-default-shape frames "frame"
  initialize
  create-set-block   ;; the set-block is the group of patches to be used in setting the combination
  create-guess-block ;; the set-block is the group of patches to be used in guessing the combination
  ask patches-in-set-block [ setup-frames ]
  ask patches-in-guess-block [ setup-frames ]
  set instructions "OK. Now press Pick Dice to set the combination of dice faces."
  set am-I-set-up? true
end

to create-set-block
  set patches-in-set-block patches with [ pycor = 1 and pxcor > -1 ]
  ask patches-in-set-block [ set pcolor green ]
  ask patches with [ pcolor != green ] [ set pcolor white ]
end

;; To create the guess-block of patches, each patch in the set-block assigns a value to
;; a patch 3 patches lower down.  The patch variable my-color is used in this context
;; even though it is not a color context, to save an agentset variable
to create-guess-block
  ask patches-in-set-block [
    set my-guess-block-buddy patch-at 0 -2
    ask my-guess-block-buddy [ set my-guess-block-buddy "buddy" ]
  ]
  set patches-in-guess-block patches with [ my-guess-block-buddy = "buddy" ]
end

to setup-frames  ;;patches procedure
  sprout 1 [
    set breed frames
    set color black
  ]
end

to pick-dice
  if abort-pick-dice? [ stop]
  if not am-I-set-up? [ alert-setup stop ]
  set instructions (word "Click on the green squares to create your combination. "
                         "It goes from 1 to 6 and over again. "
                         " Next, unpress Pick Dice.")
  assign-face
  set dice-choices []

  ;; permis-choice is a list of the dice face-values;
  ;; This list will serve us later in the "check-permis" procedure,
  ;; where we compare between what the model guesses and what we picked
  set dice-choices [ 1 +  position shape dice-list ] of dice with [ pycor > 0 ]
  set combi-exists? true
end

;; Every time you click on a die face,
;; the procedure identifies the die's current face value, moves one step
;; forward in the list of 6 face-value options and assigns this new value to the current die.
to assign-face
  if mouse-down? [
    ask patches-in-set-block with [ ( pxcor = round mouse-xcor ) and ( pycor = round mouse-ycor ) ] [
      ifelse not any? dice-here [
        make-dice
      ] [
        ;; loops through the list of dice
        ask dice-here [set shape ( item ((1 + position shape dice-list) mod 6) dice-list ) ]
      ]
      wait 0.3  ;; this wait gives the user a chance to lift the finger after clicking so
                ;; that the procedure doesn't go again
    ]
  ]
  ask patches-in-set-block
    [ask dice-here [ set my-shape shape ] ]
  ask patches-in-set-block [
    ask my-guess-block-buddy [
      if not any? dice-here [ make-dice ]
      ask dice-here [ ht ]
    ]
  ]
end

to make-dice ;; patch procedure
  sprout 1 [
    set breed dice
    set color black
    set shape "one"
  ]
end

;; the core super-procedure for generating random dice and searching for matches with the dice you picked
to search
  if samples-counter = total-samples [stop]
  if ( count dice with [ pycor > 0 ] ) < 2 [ alert-not-2-dice stop ]
  ;; managing the user-code interface
  if not am-I-set-up? or not combi-exists? [ alert-setup stop ]
  set abort-pick-dice? true
  set instructions word "The program guesses combinations randomly and tracks "
                        "the number of times it discovers the dice you picked."

  ;; These two Boolean variables track whether a search is successful. The logic is that by default the search is
  ;; considered a success until proved otherwise.
  set all-combi-match? true
  set all-permis-match? true
  ask patches-in-set-block [ guess ]
  ask patches-in-set-block [ check ]
  check-permis
  set count-steps count-steps + 1

  if all-combi-match?
    [ set count-combi-successes count-combi-successes + 1  ]

  if all-permis-match?
     [ set count-permis-successes count-permis-successes + 1 ]

  ;; for 'single-success?' true, we want the program to stop after matching dice were found
  if single-success? [
    ifelse Analysis-Type = "Both" [
      if all-combi-match? [
        congratulate-combi
        stop
      ]
      if all-permis-match? [
        congratulate-permi
        stop
      ]
    ] [
      ifelse Analysis-Type = "Combination" [
        if all-combi-match? [
          congratulate-combi
          stop
        ]
      ] [
        if  all-permis-match? [
          congratulate-permi
          stop
        ]
      ]
    ]
  ]
  if count-steps = sample-size [ plot-sample ]
end

;; if a non-correspondence was found between the guessed and original combi,
;; this specific search trial is registered as failed
to check  ;; patches-in-set-block procedure
  let my-shape-help [my-shape] of one-of dice-here
  ask my-guess-block-buddy
    [ ask dice-here [ if shape != my-shape-help [ set all-combi-match? false ] ] ]
end

to check-permis
  ;; checking to see whether the current dice pair is the same as the original pair or a permutation on it
  ;; the face-values of the dice are compared both to the original pair [A; B] and to its reverse order [B; A]
  let current-dice ( [ my-current-number ] of dice with [ pycor < 0 ] )
  ifelse ( current-dice = dice-choices ) or
         ( current-dice = reverse dice-choices )
    [ set all-permis-match? true ]
    [ set all-permis-match? false ]
end

to congratulate-combi
  let calc-combi-help  ( length #combi-successes-per-sample-list  * sample-size ) + count-steps
  set instructions (word "Congratulations! "
                         "You discovered the hidden combination in " calc-combi-help
                         " steps. You can press Search again.")
  little-setup
end

to congratulate-permi
  let calc-permis-help  ( length #permis-successes-per-sample-list  * sample-size ) + count-steps
  set instructions (word "Congratulations!  You discovered a permutation of the hidden combination in "
                         calc-permis-help " steps. You can press Search again.")
  wait 1
  little-setup
end

to little-setup
  set count-steps 0
  set count-combi-successes 0
  set #combi-successes-per-sample-list []
  set count-permis-successes 0
  set #permis-successes-per-sample-list []
end

;; the model guesses random dice
to guess  ;; patches-in-set-block procedure
  ask my-guess-block-buddy
  [
     ask dice-here
     [
       set shape item ( random 6 ) dice-list
       set my-current-number ( 1 +  position shape dice-list )
       st
     ]
  ]
end

;; toggles between hiding and revealing the patches-in-set-block
to hide/reveal
  ifelse hide?
    [ hide-set-block ]
    [ reveal-set-block ]
end

to hide-set-block
  ask patches-in-set-block [ ask dice-here [ ht ] ]
  ask patches-in-set-block [ set pcolor gray - 2 ]
  set hide? false
end

;; procedure that re-assigns to patches-in-set-block the appearance properties of the dice you picked
to reveal-set-block
 ask dice with [ pycor > 0 ] [ set pcolor green st ]
 set hide? true
end

;; ALERTS FOR THE USER

to alert-setup
   set instructions "Please work this way: Press Setup and then Pick Dice"
end

to alert-not-2-dice
  set stop? true
  user-message word "To run the experiment, you must create two dice: "
                    "To start over, press 'OK' then Setup."
end

;; PLOTTING CODE

to plot-sample
  set samples-counter samples-counter + 1
  set #combi-successes-per-sample-list fput count-combi-successes #combi-successes-per-sample-list
  set #permis-successes-per-sample-list fput count-permis-successes #permis-successes-per-sample-list
  set-current-plot "Successes-per-Sample Distributions"
  ifelse Analysis-Type = "Both"
  [
    ;; this line regulates the appearance of the plot -- it centers the two histograms
    set-plot-x-range 0  max ( list ( round 1.5 * ceiling ( mean  #permis-successes-per-sample-list ) )
                                  ( 1 + max #permis-successes-per-sample-list ) .1 )
  ]
  [
    ifelse Analysis-Type = "Combination"
      [set-plot-x-range 0 max ( list ( 2 * ceiling ( mean  #combi-successes-per-sample-list ) )
                                    ( 1 + max #combi-successes-per-sample-list ) .1 ) ]
      [set-plot-x-range 0 max (list ( 2 * ceiling ( mean  #permis-successes-per-sample-list ) )
                                    ( 1 + max #permis-successes-per-sample-list ) .1 ) ]
  ]

  ;; In order to collapse two procedures into one, we use (below) the do-plot procedure.
  ;; Here, we assign values for this procedure according to the two conditions of search (combi and permis).
  if Analysis-Type != "Permutations"
    [ do-plot #combi-successes-per-sample-list "Combination" ]
  if Analysis-Type != "Combination"
    [ do-plot #permis-successes-per-sample-list "Permutations" ]
end

;; plotting procedure
to do-plot [ event-list current-pen-name ]
  if Analysis-Type = "Combination" [ set-current-plot-pen "Combination" plot-pen-reset ]
  if Analysis-Type = "Permutations"  [ set-current-plot-pen "Permutations" plot-pen-reset ]
  set-current-plot-pen current-pen-name
  ifelse bars? [ set-plot-pen-mode 1 ] [ set-plot-pen-mode 0 ]
  histogram event-list
  set count-steps 0
  set count-permis-successes 0
  set count-combi-successes 0
end

to-report ratio
  ;; we want the ratio to be rounded after two decimal points
  let ratio-help precision ( mean #permis-successes-per-sample-list / mean #combi-successes-per-sample-list ) 2
  report word "1 : " ratio-help
end
@#$#@#$#@
GRAPHICS-WINDOW
428
61
654
308
1
1
72.0
1
10
1
1
1
0
1
1
1
-1
1
-1
1
0
0
0
ticks

MONITOR
82
196
164
241
+  Outcomes
count-steps
3
1
11

MONITOR
202
195
294
240
Combinations
count-combi-successes
3
1
11

SWITCH
128
62
271
95
single-success?
single-success?
1
1
-1000

MONITOR
9
196
79
241
#Samples
samples-counter
0
1
11

MONITOR
297
195
390
240
Permutations
count-permis-successes
3
1
11

TEXTBOX
198
176
354
194
Successes in this sample:
11
0.0
0

BUTTON
7
61
115
94
NIL
Setup
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
7
96
115
129
Pick Dice
Pick-Dice
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

BUTTON
7
131
115
164
Search
Search
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
7
10
655
55
NIL
instructions
0
1
11

MONITOR
45
392
161
437
Mean-Combination
mean #combi-successes-per-sample-list
2
1
11

PLOT
9
246
389
390
Successes-per-Sample Distributions
successes per sample
count
0.0
16.0
0.0
50.0
true
true
"" ""
PENS
"Combination" 1.0 1 -16777216 true "" ""
"Permutations" 1.0 1 -2674135 true "" ""

MONITOR
162
392
278
437
Mean-Permutations
mean #permis-successes-per-sample-list
2
1
11

MONITOR
291
392
388
437
Combi : Permis
ratio
2
1
11

SLIDER
128
97
271
130
sample-size
sample-size
10
1000
1000
10
1
NIL
HORIZONTAL

SLIDER
128
132
271
165
total-samples
total-samples
0
10000
5000
10
1
NIL
HORIZONTAL

CHOOSER
273
120
389
165
Analysis-Type
Analysis-Type
"Permutations" "Combination" "Both"
2

SWITCH
297
356
388
389
bars?
bars?
0
1
-1000

BUTTON
526
160
631
193
Hide/Reveal
Hide/Reveal
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
444
103
504
151
Pick your dice here\n      ----->
11
0.0
0

TEXTBOX
438
236
506
303
The model guesses your dice here ----->
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

"Dice" is a virtual laboratory for learning about probability through conducting and analyzing experiments. You use two dice in the view to perform the experiments. You set up an experiment by choosing a combination consisting of a face for each die, for instance 3 and 4 (we will use this example throughout). Then you "roll" these dice repeatedly and study how often the dice match your chosen combination.

The dice can match your initial combination in several different ways: they can show the same numbers in the same order; they can show the same numbers regardless of order; or the sum of both dice can match.  The model collects statistics on all of these kinds of matches.  It also keeps track of how often you get each of the 11 possible dice sums.  The different plots and monitors in the model give you different perspectives on the accumulated data.

This model is a part of the ProbLab curriculum. The ProbLab curriculum is currently under development at the CCL. For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## PEDAGOGICAL NOTE

This model introduces tools, concepts, representations, and vocabulary for describing random events.  Among the concepts introduced are "sampling" and "distribution".

The various ProbLab models use virtual computer-based "objects" to teach probability.  In this model, the computer-based objects are "virtual" dice, modeled on the familiar physical ones. By using familiar objects, we hope to help learners form a "bridge" from their everyday experience to more abstract concepts. Other ProbLab models use virtual "objects" that are less familiar. Using dice first helps prepare students for that.

Facilitators are encouraged to introduce this model as an enhancement of experiments with real dice. The model has several advantages over real dice. Virtual dice roll faster, and the computer can record the results instantly and accurately from multiple perspectives simultaneously.

The model attempts to involve the learner by allowing them to choose a combination before running the experiment.

## HOW TO USE IT

Press SETUP, then press PICK DICE.  By clicking on the green squares, you set a pair, for instance [3; 4].  Press SEARCH to begin the experiment, in which the computer generates random dice faces. If you set the SINGLE-SUCCESS? switch to 'On,' the experiment will stop the moment the combination you had created is discovered. If this switch is set to 'Off,' the experiment will keep running as many times as you have set the values of the SAMPLE-SIZE and TOTAL-SAMPLES sliders. In the plot window, one or two histograms start stacking up, showing how many times the model has discovered your pair in its original order ("Combinations") and how many times it has discovered you pair in any order ("Permutation"). Are there always more permutations than combinations?

Buttons:
SETUP -- begins new experiment
PICK DICE -- allows you to use the mouse to click on squares so as to pick dice for.  Clicking repeatedly on the same square loops the die-faces / colors through an option cycle that has as many options as the value you set for the HOW-MANY-CHOICES slider.  For instance, if the slider is set to '3' and you are working with colors, clicking repeatedly on a single square will give the colors green, blue, pink, green, blue, pink, green, etc.
HIDE/REVEAL -- toggles between hiding and revealing the dice you picked. This function is useful when you pose a riddle to a friend and you do not want them to know what dice you chose.
SEARCH - activates the random search. The program generates random dice-faces and matches the outcome against the combination you had created.

Switches:
SINGLE-SUCCESS? -- stops the search after the combination has been matched once.
BARS?  -- toggles between two graphing options: "On" is a histogram, and "Off" gives a line graph.

Choices:
Analysis-Type --
- "Permutations" - That is, order does not matter, so '1 2 3' is considered the same as its permutation '3 2 1' (it is registered as a favored event)
-  "Combination" - That is, order matters, so '1 2 3' is not accepted as the same as its permutation '3 2 1' (it is not registered as a favored event)
-  "Both" - That is, the experiment will analyze outcomes from both the "Permutations" and "Combination" perspectives, and each will be represented in the plot.

Sliders:
SAMPLE-SIZE -- the number of dice rolls in a sample
TOTAL-SAMPLES -- the number of sample you are taking all in all in an experiment.

Monitors:
\#SAMPLES -- shows how many samples have been taken up to this point in this experiment
+ OUTCOMES -- shows how many single outcomes have occurred within the current sample.
COMBINATION -- shows how many successes (hit guesses) the program has performed in this sample according to the conditions that order matters
PERMUTATIONS -- shows how many successes (hit guesses) the program has performed in this sample according to the conditions that order does not matter
MEAN-COMBINATIONS -- the sample mean of favored events according to the 'combination' interpretation, by which a favored event is only the exact combination you created.
MEAN-PERMUTATIONS -- the sample mean of favored events according to the 'permutations' interpretation, by which a favored event is either the exact combination you created or its reverse
COMBI : PERMIS -- shows the ratio between the mean values of the sample outcome distributions corresponding to the conditions 'combination' and 'permutation,' respectively. This monitor updates each time a sample has been completed.

Plots:
SUCCESSES-PER-SAMPLE DISTRIBUTION -- displays the count of the number of favored events (successes) per sample over all samples you have taken. For instance, if on the first five samples you have taken, the combination was matched 3 times, 2 times, 4 times, 7 times, and 4 times, then the "Combinations" histogram will be the same height over 2, 3, and 7, but it will be twice as higher over the 4, because 4 occurred twice.

## THINGS TO NOTICE

As the experiment runs, the distributions of outcomes in the plots gradually take on a bell-shaped curve.

As the search procedure is running, look at the monitor #STEPS IN THIS SAMPLES. See how it is updating much faster than the monitor to its left, #SAMPLES.  The number in #SAMPLES increases by 1 each time #STEPS IN THIS SAMPLES reaches the number that is set in the slider SAMPLE SIZE.

As the search procedure is running, watch the monitors COMBINATION and PERMUTATIONS. Note whether or not they are updating their values at the same pace. For most combinations that you set, PERMUTATIONS updates much faster. This is because PERMUTATIONS registers a success each time the model hits on the set of colors / dice-faces you selected even if they appear in a different order form what you had selected.

As the search procedure is running, watch the monitor COMBI : PERMI ratio. At first, it changes rapidly, and then it changes less and less. Eventually, it seems to stabilize on some value. Why is this so?

Unless the red histogram ('Permutations') covers the black histogram ('Combination') entirely, you will see that the 'Permutations' histogram always becomes both wider and shorter than the 'combinations' histogram. Also, the 'Permutations' histogram (red) typically stretches over a greater range of values as compared to the 'combination' histogram (black).  We say of the wider histogram that it has a greater 'variance' as compared to the narrower histogram. Try to explain why the Permutations distribution has greater variance than the Combinations distribution.

Also, you may notice that the 'permutations' and 'combinations' histograms cover the same area. That is because the total area of each histogram, irrespective of their location along the horizontal axis and irrespective of their shape, indicates the number of samples they represent. We know that the two histograms represent the same number of samples. Therefore, they have the same area.

## THINGS TO TRY

Run an experiment with a sample size of 20 and then run it with the same settings but with a sample size of 100 or more. In each case, look at the distribution of the SUCCESSES-PER-SAMPLE DISTRIBUTIONN. See how the experiment with the small sample resulted in half-a-bell curve, whereas the experiment with the larger sample results in a whole-bell curve. Why is this so?

Pressing HIDE/REVEAL after you create a combination allows you to setup an experiment for a friend to run. Your friend will not know what the combination is and will have to analyze the graphs and monitors to make an informed guess. You may find that some combinations are harder to guess than others. Why is this so? For instance, compare the case of the combination [1; 1] and [3; 4]. Is there any good way to figure out if we are dealing with a double or not? This question is also related to the following thing to try.

For certain dice you pick, if you run the search under the "Both" option of the ANALYSIS-TYPE choice, you will see only a single histogram in the SUCCESSES-PER-SAMPLE DISTRIBUTION plot. Try to pick dice that produce a single histogram, then try to find others. What do these dice pairs have in common? Why do you think you observe only a single histogram? Where is the other histogram? How do the monitors behave when you have a single histogram?

When the Combination and Permutations histograms do not overlap, we can speak of the distance between their means along the x-axis. Which element in the model can affect this distance between them? For instance, what should you do in order to get a bigger distance between these histograms? What makes for narrow histograms? Are they really narrower, or is it just because the maximum x-axis value is greater and so the histograms are "crowded?"

Set the SAMPLE-SIZE at 360 and TOTAL-SAMPLES at its maximum value. Pick the dice [3; 4], and run the experiment. You will get a mean of about 10 for the Combination condition (in which order matters, so only [3; 4] is considered a favored event), and you will get a mean of about 20 for the Permutations condition (where the order does not matter, so both [3; 4] and [4; 3] are considered favored events). Why 10 and 20? There are 6*6=36 different dice pairs when we care for the order: [1; 1] [1; 2] [1; 3] [1; 4] [1; 5] ... [6; 4] [6; 5] [6; 6]. So samples of 36 rolls have on average a single occurrence of [3; 4] and a single occurrence of [4; 3]. Thus, samples of 360 have 10 times that: 10 occurrences of [3; 4] and 10 of [4; 3], on average.

## EXTENDING THE MODEL

A challenge: Add a 7th die face. Then you can run experiments with 7-sided dice!

Add a plot of the ratio between Combinations and Permutations.

Is the program searching for the dice you picked in the most efficient way? Think of more efficient search procedures and implement them in this model.

It should be interesting to track how long it takes the model from one success to another. Add code, monitors, and a plot to do so.

Following is an extension idea for applying this model towards thinking about search algorithms. Currently, the program guesses combinations randomly. This could be improved upon so that the program finds the combination in less guesses. For instance, the moment one of the squares has the correct die face, the program would continue guessing only the other die. Another idea might be to create a systematic search procedure.

## RELATED MODELS

The ProbLab model Random Combinations and Permutations builds on Dice. There, you can work with more than just 2 dice at a time. Also, you can work with colors instead of dice faces.

## NETLOGO FEATURES

An interesting feature of "Dice," that does not appear in many other models, is the procedure for selecting a die's face value. To you, it is obvious that three dots means "3," but the program doesn't "know" this unless you "tell" it. Each time you click on a die, Look in the Shapes Editor that is in the Tools dropdown menu. You will find six die shapes: 1, 2, 3, 4, 5, and 6. The names of these shapes form a list: ["one" "two" "three" "four" "five" "six"]. Each time you click on a die, a procedure maps between each name and the numerical value corresponding to it.

## CREDITS AND REFERENCES

This model is a part of the ProbLab curriculum. The ProbLab Curriculum is currently under development at Northwestern's Center for Connected Learning and Computer-Based Modeling. . For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.
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

five
false
0
Rectangle -16777216 true false 46 45 255 255
Circle -1 true false 129 130 41
Circle -1 true false 75 76 41
Circle -1 true false 185 182 38
Circle -1 true false 182 180 41
Rectangle -16777216 true false 120 120 180 178
Circle -1 true false 76 178 41
Circle -1 true false 180 77 40
Rectangle -1 true false 200 97 206 106
Circle -1 true false 131 130 39

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

four
false
0
Rectangle -16777216 true false 46 45 255 255
Circle -1 true false 129 130 41
Circle -1 true false 75 76 41
Circle -1 true false 185 182 38
Circle -1 true false 182 180 41
Rectangle -16777216 true false 120 120 180 178
Circle -1 true false 76 178 41
Circle -1 true false 180 77 40
Rectangle -1 true false 200 97 206 106
Circle -1 true false 131 130 39
Rectangle -16777216 true false 121 114 178 179

frame
false
14
Rectangle -16777216 true true -8 2 18 298
Rectangle -16777216 true true 1 0 300 16
Rectangle -16777216 true true 283 2 299 300
Rectangle -16777216 true true 1 285 300 299

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

one
false
0
Rectangle -16777216 true false 46 45 255 255
Circle -1 true false 129 130 41
Circle -1 true false 75 76 41
Circle -1 true false 185 182 38
Rectangle -16777216 true false 67 62 119 123
Rectangle -16777216 true false 186 182 223 227
Rectangle -16777216 true false 177 194 198 211
Rectangle -16777216 true false 58 55 128 136
Rectangle -16777216 true false 181 191 247 239

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

six
false
0
Rectangle -16777216 true false 46 45 255 255
Circle -1 true false 129 130 41
Circle -1 true false 75 76 41
Circle -1 true false 185 182 38
Circle -1 true false 182 180 41
Rectangle -16777216 true false 120 120 180 178
Circle -1 true false 76 178 41
Circle -1 true false 180 77 40
Rectangle -1 true false 200 97 206 106
Circle -1 true false 74 130 40
Circle -1 true false 181 129 40

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

three
false
0
Rectangle -16777216 true false 46 45 255 255
Circle -1 true false 129 130 41
Circle -1 true false 75 76 41
Circle -1 true false 185 182 38
Circle -1 true false 182 180 41

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

two
false
0
Rectangle -16777216 true false 46 45 255 255
Circle -1 true false 129 130 41
Circle -1 true false 75 76 41
Circle -1 true false 185 182 38
Circle -1 true false 182 180 41
Rectangle -16777216 true false 122 120 177 175

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
need-to-manually-make-preview-for-this-model
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

globals [
          instructions       ;; The messages that appear in the instructions monitor

     ;; Booleans to preempt run-time errors and/or user confusions
          am-I-set-up?
          combi-exists?
          abort-create-combi?

          patches-in-set-block     ;; agentset of patches where the user sets up the game
          patches-in-guess-block   ;; agentset of patches where the random guesses are generated
          num-choices
          color-list           ;; the source options of colors to choose from
          dice-list            ;; the source options of dice faces to choose from
          color-rotation       ;; the list of colors available to choose from in a particular experiment
          dice-rotation        ;; the list of dice faces available to choose from in a particular experiment

     ;; variables for keeping track of the accumulating statistical data
          count-steps                   ;; counts the number of outcomes in the current sample
          #combi-successes-per-sample-list      ;; list of outcomes per sample that were exactly like
                                                ;; the original combination
          #permis-successes-per-sample-list   ;; list of outcomes per sample that are the original
                                              ;; combination or its permutation
          samples-counter             ;; how many sample have elapsed in an experiment
          permis-choices              ;; list of choices in original combination irrespective of their original order
          permis-choices-check        ;; list of choices in a guessed combination irrespective of their original order
          permis-success?             ;; Boolean that tracks whether there has been a hit under the permutation condition
          count-combi-successes       ;; counts up hits under the combi condition
          count-permis-successes      ;; counts up hits under the permutation condition
          mean-combi-per-sample       ;; mean number of same-order outcomes per sample
          mean-permis-per-sample      ;; mean number of either-order outcomes per sample
          all-combi-match?            ;; Boolean that is true when a combination has been discovered
          all-permis-match?           ;; Boolean that is true when a permutation of a combination has been discovered

     ;; Boolean associated with the Hide/Reveal button
          hide?
        ]

patches-own
        [
          my-color    ;; color property for the experimental mode in which the combinations consist of colors
          my-shape    ;; shape property for the experimental mode in which the combinations consist of dice face.
                      ;; patches register the shape of the dice-turtles that are in the original combination
          my-guess-block-buddy  ;; each patch in the set-block has a buddy patch in the guess-block
        ]

breed [ frames frame ] ;; frames are the black perimeters of patches that help the user distinguish between
                       ;; neighboring patches of same color. The frames are constant throughout the experiment
                       ;; and are never part of the statistical analysis.
breed [ dice a-die ]         ;; dice are the dice-face turtles

to startup
    initialize
    ask patches [ set pcolor brown ]
    set instructions "Hi! Press Setup or set new values in the 'width' and 'height' sliders and then press Setup."
end

to initialize
  ca

  ;; building the optional colors for the color combinations
  set color-list [ green blue magenta cyan pink yellow ]

  ;; building the optional shapes for the dice combinations
  set dice-list [ "one" "two" "three" "four" "five" "six" ]

  ;; These two variables track the combination search according to the two interpretation
  ;; of what a success is (see Info tab)
  set all-combi-match? false
  set all-permis-match?  false

  ;; Variables for managing use of the model
  set combi-exists? false
  set am-I-set-up? false

  ;; List variables for selecting and searching values
  set color-rotation []
  set dice-rotation []

  ;; List variables for accumulating the experimental outcomes
  set permis-choices []
  set #combi-successes-per-sample-list []
  set #permis-successes-per-sample-list []

  set bars? true
  set abort-create-combi? false
  set num-choices #choices
  set hide? true
end

;;--------------------------------------------------------------------------------------------------------
;;--------------------------------------------------------------------------------------------------------

to setup
  set-default-shape frames "frame"
  set am-I-set-up? false
  initialize
  create-set-block   ;; the set-block is the group of patches to be used in setting the combination
  create-guess-block ;; the set-block is the group of patches to be used in guessing the combination
  ask patches-in-set-block [ setup-frames ]
  ask patches-in-guess-block [ setup-frames ]
  set instructions (word "OK, you have created a " width "-by-" height
                         " block with " count patches-in-set-block
                         " squares in it.  Now press Create Combi.")
  set am-I-set-up? true
end


;; The following procedure is perhaps more elaborate than is called for here, but it is powerful in that
;; it will work if you decide to modify the 'width' and 'height' setting to greater maximum values
to create-set-block
  let x-pos false
  let x-neg false
  let y-pos false
  let y-neg false
  ifelse ( width / 2 ) = floor ( width / 2 )
    [ set x-pos  (width / 2)          set x-neg ( - ( ( width / 2 ) - 1 ) ) ]
    [ set x-pos floor (width / 2)     set x-neg ( - ( floor ( width / 2 ) ) ) ]
  ifelse ( height / 2 ) = floor ( height / 2 )
    [ set y-pos ( height / 2 )           set y-neg ( - ( ( height / 2 ) - 1 ) ) ]
    [ set y-pos floor ( height / 2 )     set y-neg ( - ( floor ( height / 2 ) ) ) ]
  set patches-in-set-block patches with [ pxcor <= x-pos and pxcor >= x-neg and
                                          pycor <= y-pos + 1 and pycor >= y-neg + 1 ]
  ask patches-in-set-block [ set pcolor green ]
  ask patches with [ pcolor != green ] [ set pcolor brown ]
end

;; To create the guess-block of patches, each patch in the set-block assigns a value to
;; a patch 3 patches lower down.  The patch variable my-color is used in this context
;; even though it is not a color context, to save an agentset variable
to create-guess-block
  ask patches-in-set-block
  [
    set my-guess-block-buddy patch-at 0 -3
    ask my-guess-block-buddy [ set my-color "buddy" ]
  ]
  set patches-in-guess-block patches with [ my-color = "buddy" ]
end

to setup-frames  ;;patches procedure
  sprout-frames 1
  [ set color black ]
end

;; procedure for choosing the combination
to create-combi
  if not am-I-set-up? [ wait .1 alert-setup stop ]
  if not dice? [ask dice [ die ] ]
  if abort-create-combi? [ stop ]

  set instructions word "Click on the green squares repeatedly to create your combination. "
                        "Then Unpress Create Combi."
  ifelse dice? [ set-dice-rotation ] [ set-color-rotation ]
  assign-color-or-image

  ;; explaining to the user the order of actions that is suitable for running this model
  if ( dice? and ( #choices != num-choices ) ) or
     ( not dice? and ( #choices != num-choices ) )
       [
        set #choices num-choices
        set instructions word "Sorry.  To change the '#choices' slider,"
                              " unpress Create Combi, set the slider and press SETUP."
        wait 5
       ]
  how-many-of-each-choice?
  set combi-exists? true
end

;; coordinating between user's clicks on the patches in the combi and shapes/colors of these patches
to assign-color-or-image
  if mouse-down?
  [
    ask patches-in-set-block with [ ( pxcor = round mouse-xcor ) and ( pycor = round mouse-ycor ) ]
    [
       ifelse dice?
        [
          ifelse not any? dice-here
          [
             make-dice
          ]
          [
            ;; see NETLOGO FEATURE in the Info tab.
            ask dice-here [ set shape item ( ( 1 + position shape dice-rotation ) mod num-choices ) dice-rotation ]
          ]
        ]
        [
         ;; see NETLOGO FEATURE in the Info tab.
         set pcolor item ( ( 1 + position pcolor color-rotation ) mod num-choices ) color-rotation
        ]
       wait 0.3  ;; this wait gives the user a chance to lift the finger after clicking
                 ;; so that the procedure doesn't go again
    ]
  ]
  ask patches-in-set-block
    [
      ifelse dice?
        [ ask dice-here [ set my-shape shape ] ]
        [ set my-color pcolor ]
    ]
  if dice?
  [
    ask patches-in-set-block
    [
      ask my-guess-block-buddy
      [
        if not any? dice-here [ make-dice ]
        ask dice-here [ ht ]
      ]
    ]
  ]
end

to make-dice
  sprout-dice 1
    [
     set color black
     set shape "one"
    ]
end

;; Creates a new list with items from the color-list. For instance, if #choices is 3, then the
;; new list will contain the first 3 color names from the color-list
;; See also NETLOGO FEATURE in the Info tab.
to set-color-rotation
  let color-list-counter 0
  ifelse length color-rotation = num-choices [ stop ] [set color-rotation [] ]
  repeat num-choices
  [
    set color-rotation lput ( item color-list-counter color-list ) color-rotation
    set color-list-counter color-list-counter + 1
  ]
end

;; Creates list from part of the dice-list. See also NETLOGO FEATURE in the Info tab.
to set-dice-rotation
 let dice-list-counter 0
 ifelse length dice-rotation = num-choices [ stop ] [ set dice-rotation [] ]
 repeat num-choices
  [
    set dice-rotation lput ( item dice-list-counter dice-list ) dice-rotation
    set dice-list-counter dice-list-counter + 1
  ]
end

;; For the permutations condition, counts up how many times each choice appears
to how-many-of-each-choice?
  let rotation-counter 0
  set permis-choices []
  repeat num-choices
  [
    ifelse dice?
      [ set permis-choices lput count dice with
        [ ( pycor > 0 ) and (shape = item rotation-counter dice-rotation ) ] permis-choices ]
      [ set permis-choices lput count patches-in-set-block with
        [pcolor = (item rotation-counter color-rotation ) ] permis-choices ]
    set rotation-counter rotation-counter + 1
  ]
end

;; the core super-procedure for generating random combinations and searching for matches
;; with the original combination
to search-combi
  if ( dice? ) and ( sum permis-choices != count patches-in-set-block ) [ alert-forgot-choice stop ]

  ;; managing the user-code interface
  if not am-I-set-up? or not combi-exists? [ alert-setup stop ]
  set abort-create-combi? true
  set instructions word "The program guesses combinations randomly and tracks"
                        " the number of times it discovers your combination."
  if dice? and count dice < width * height
    [ set instructions "You are in dice mode.  Please first set up all your dice or change to color mode" stop ]
  ifelse dice?
    [ set #choices length dice-rotation ]
    [ set #choices length color-rotation ]

  ;; These two Boolean variables track whether a search is successful. The point is that by default the search is
  ;; considered a success, as if the guessed combi matches the user's combi. Later, in the check procedure, if
  ;; there is a mismatch it is found out.
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

  ;; for 'single-success?' true, we want the program to stop after a combination has been found that
  ;; matches the user's combination
  if single-success? [
    ifelse Analysis-Type = "both" [
      if all-combi-match? [
        congratulate-combi
        stop
      ]
      if all-permis-match? [
        congratulate-permi
        stop
      ]
    ] [
      ifelse Analysis-Type = "combination" [
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

to congratulate-combi
  let calc-combi-help  ( length #combi-successes-per-sample-list  * sample-size ) + count-steps
  set instructions (word "Congratulations! You discovered the hidden combination in "
                         calc-combi-help " steps. Set up and try again.")
  little-setup
end

to congratulate-permi
  let calc-permis-help  ( length #permis-successes-per-sample-list  * sample-size ) + count-steps
  set instructions (word "Congratulations!  You discovered a permutation of the hidden combination in "
                         calc-permis-help " steps. Set up and try again.")
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

;; the model guesses by trying out a combination of random values from the dice or color lists
to guess  ;; patches-in-set-block procedure
  ifelse dice?
  [
    ask my-guess-block-buddy
    [
       ask dice-here
       [
         set shape item ( random length dice-rotation ) dice-rotation
         st
       ]
    ]
  ]
  [
    let new-pcolor item ( random length color-rotation ) color-rotation
    ask my-guess-block-buddy [ set pcolor new-pcolor ]
  ]
end

;; if a non-correspondence was found between the guessed and original combi, this specific
;; search trial is registered as failed
to check  ;; patches-in-set-block procedure
  let my-color-help my-color
  let my-shape-help my-shape
  ifelse dice?
  [
    ask my-guess-block-buddy
      [ ask dice-here [ if shape != my-shape-help [ set all-combi-match? false ] ] ]
  ]
  [
     ask my-guess-block-buddy
       [ if pcolor != my-color-help [ set all-combi-match? false ] ]
  ]
end

;; For the permutation search, we have earlier made a list of how many times each choice appears
;; in the combination. Now we create the guessed-combination list, then compare these two lists.
to check-permis
  let rotation-counter 0
  set permis-choices-check []
  repeat num-choices
  [
    ifelse dice?
      [ set permis-choices-check lput count dice with
        [ ( pycor < 0 )  and ( shape = item rotation-counter dice-rotation ) ] permis-choices-check ]
      [ set permis-choices-check lput count patches-in-guess-block with
        [ pcolor =  item rotation-counter color-rotation ] permis-choices-check ]
    set rotation-counter rotation-counter + 1
  ]

  set rotation-counter 0
  ifelse permis-choices-check = permis-choices
    [ set all-permis-match? true ]
    [ set all-permis-match? false ]
end

;; toggles between hiding and revealing the patches-in-set-block
to hide/reveal
  ifelse not combi-exists? [
     user-message "Please first create a combination." stop
   ] [
     ifelse hide?
       [ hide-set-block ]
       [ reveal-set-block]
   ]
end

to hide-set-block
  if dice?
    [ ask patches-in-set-block [ ask dice-here [ ht ] ] ]
  ask patches-in-set-block [set pcolor gray - 2 ]
 set hide? false
end

;; procedure that re-assigns to patches-in-set-block the appearance properties of the original combination
to reveal-set-block
  ifelse dice?
    [ ask dice with [ pycor > 0 ] [ set pcolor green st ] ]
    [ ask patches-in-set-block [ set pcolor my-color ] ]
 set hide? true
end

to alert-forgot-choice
  set instructions word "You are in Dice mode."
                        " Please make sure you pick dice faces for each and every one of the squares"
end

to alert-setup
   set instructions "Please work this way: press Setup, and then Create Combi."
end

to plot-sample
  set samples-counter samples-counter + 1
  set #combi-successes-per-sample-list fput count-combi-successes #combi-successes-per-sample-list
  set #permis-successes-per-sample-list fput count-permis-successes #permis-successes-per-sample-list
  set-current-plot "Successes per Sample Distribution"
  ifelse Analysis-Type = "both"
  [
    ;; this line regulates the appearance of the plot -- it centers the two histograms
    set-plot-x-range 0  max ( list ( round 1.5 * ceiling ( mean  #permis-successes-per-sample-list ) )
                                  ( 1 + max #permis-successes-per-sample-list ) .1 )
  ]
  [
    ifelse Analysis-Type = "combination"
      [set-plot-x-range 0 max ( list ( 2 * ceiling ( mean  #combi-successes-per-sample-list ) )
                                    ( 1 + max #combi-successes-per-sample-list ) .1 ) ]
      [set-plot-x-range 0 max (list ( 2 * ceiling ( mean  #permis-successes-per-sample-list ) )
                                    ( 1 + max #permis-successes-per-sample-list ) .1 ) ]
  ]

  ;; In order to collapse two procedures into one, we use (below) the do-plot procedure.
  ;; Here, we assign values for this procedure according to the two conditions of search (combi and permis).
  if Analysis-Type != "permutations"
    [ do-plot #combi-successes-per-sample-list "combination" ]
  if Analysis-Type != "combination"
    [ do-plot #permis-successes-per-sample-list "permutations" ]
end

;; plotting procedure
to do-plot [ event-list current-plot-name ]
  if Analysis-Type = "combination" [ set-current-plot-pen "combination" plot-pen-reset ]
  if Analysis-Type = "permutations"  [ set-current-plot-pen "permutations" plot-pen-reset ]
  set-current-plot-pen current-plot-name
  ifelse bars? [ set-plot-pen-mode 1 ] [ set-plot-pen-mode 0 ]
  histogram event-list
  set count-steps 0
  set count-permis-successes 0
  set count-combi-successes 0
end

;; procedure for running repeatedly between the #1 thru #6 monitors and updating their values.
to-report # [ index ]
  ifelse ( num-choices >= index )
    [ report item ( index - 1 ) permis-choices ]
    [ report "N/A" ]
end

to-report ratio
  ;; we want the ratio to be rounded after two decimal points
  let ratio-help precision ( mean #permis-successes-per-sample-list / mean #combi-successes-per-sample-list ) 2
  report word "1 : " ratio-help
end
@#$#@#$#@
GRAPHICS-WINDOW
508
61
718
292
2
2
40.0
1
10
1
1
1
0
1
1
1
-2
2
-2
2
0
0
0
ticks

SLIDER
254
104
346
137
#choices
#choices
2
6
2
1
1
NIL
HORIZONTAL

MONITOR
568
405
715
450
#Steps in This Sample
count-steps
3
1
11

MONITOR
8
270
102
315
Combination
count-combi-successes
3
1
11

SWITCH
8
208
143
241
single-success?
single-success?
1
1
-1000

MONITOR
497
405
569
450
#Samples
samples-counter
0
1
11

SWITCH
8
456
106
489
bars?
bars?
0
1
-1000

MONITOR
417
321
474
366
NIL
# 1
3
1
11

MONITOR
465
321
522
366
NIL
# 2
3
1
11

MONITOR
516
321
573
366
NIL
# 3
3
1
11

MONITOR
566
321
623
366
NIL
# 4
3
1
11

MONITOR
616
321
673
366
NIL
# 5
3
1
11

MONITOR
667
321
724
366
NIL
# 6
3
1
11

MONITOR
103
270
205
315
Permutation
count-permis-successes
3
1
11

TEXTBOX
408
301
703
319
Sample Space and Numbers of Choices by Type:
11
0.0
0

BUTTON
7
60
143
93
Setup
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
95
143
128
Create Combi
Create-Combi
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
130
143
163
Search Combi
search-combi
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
718
55
NIL
instructions
0
1
11

SLIDER
210
70
302
103
width
width
1
5
3
1
1
NIL
HORIZONTAL

SLIDER
304
70
396
103
height
height
1
2
1
1
1
NIL
HORIZONTAL

MONITOR
291
270
396
315
Combi : Permis
ratio
2
1
11

PLOT
8
321
389
454
Successes per Sample Distribution
successes per sample
count
0.0
100.0
0.0
10.0
true
true
"" ""
PENS
"combination" 1.0 1 -16777216 true "" ""
"permutations" 1.0 1 -2674135 true "" ""

SLIDER
499
456
715
489
sample-size
sample-size
100
100000
1000
100
1
NIL
HORIZONTAL

SWITCH
8
172
143
205
dice?
dice?
0
1
-1000

CHOOSER
212
173
390
218
Analysis-Type
Analysis-Type
"permutations" "combination" "both"
2

TEXTBOX
483
384
643
402
Samples and Steps This Run:
11
0.0
0

TEXTBOX
436
87
508
135
Set your combination here ----->
11
0.0
0

TEXTBOX
436
203
504
282
The model guesses your combination here ----->
11
0.0
0

BUTTON
413
134
506
167
Hide/Reveal
hide/reveal
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
9
252
159
270
Successes in This Sample:
11
0.0
0

TEXTBOX
223
252
409
270
Ratio Between Outcome Means:
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

"Random Combinations and Permutations" is a virtual laboratory for learning about probability, and specifically about combinations and permutations, sample space, samples, favored events, and outcome distributions. The model invites you to pick a secret combination and then see how long it takes the computer to find it. The computer discovers your secret combination by just guessing blindly until it happens to guess correctly.

The user defines the size of the combinatorial sample space and then picks a particular combination from this sample space. Next, at the user's command, the model generates random combinations selected out of the sample space. The model both matches each random combination against the user's secret combination and checks whether the random combination is a permutation of the original combination. Results of these two checks accumulate and are shown both in real-time, in monitors, and at the completion of samples, in the plot (the user can choose whether to only observe one of these measures or both of them).

This model is a part of the ProbLab curriculum. The ProbLab Curriculum is currently under development at the CCL. For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## PEDAGOGICAL NOTE

In this model, you will:
- Create a sample space by choosing how many objects you will be experimenting with and how many different appearances each of these can have, e.g., how many dice you will be rolling and how many different faces these dice have. For example, you can work with 3 dice that each has 4 faces. (Alternatively, you can work with colors instead of dice faces.)
- Select from this sample space a particular combination that will be the favored event out of all outcomes of the experiment. For instance, you may create the dice combination "4, 1, 3."
- Distinguish between combinations and permutations and understand the implications of this distinction to the interpretation of the experimental outcomes. For instance, you decide to accept as a favored event ('success' or 'hit') only the original order of you combination, "4, 1, 3." However, you may decide that the order is not important  and accept
- Set the size of the sample, for instance 1,000 outcomes. That is, the experiment will rapidly generate one outcome after another, and every 1,000 outcomes it will show in the plot how many of these were the favored event. So the endless stream of outcomes is parsed into linked sections of equal length
- Run a simulated experiment in empirical probability. The program will choose randomly from the sample space that you have set, creating combinations that you will see in the view. The program monitors for outcomes that are your favored event.
- Analyze distribution plots and monitors to understand relations between the sample space you created and the experimental outcomes. For instance, 3 dice each with 4 faces create a sample space of 4*4*4 = 64. Whatever your favored event was, for instance, "4, 1, 3," it will occur once in every 64 rolls, on average. So if your sample was of size 1,000 rolls, it should occur every 1,000/64 = 15.625 rolls, on average.
- Understand more deeply the sample space you initially created, in order to calculate and anticipate sample outcomes. That is, why do 3 dice each with 4 faces create a sample space of 4*4*4? Why not, for instance, 3*3*3*3? Or just 3*4? 3+4?...
- Distinguish between combinations and permutations and understand the implications of this distinction to the interpretation of the experimental outcomes. You will see that whether you are looking just for the original permutations of your combination ("4, 1, 3") or for any permutation ("4, 1, 3," "4, 3, 1," 3, 4, 1" etc.), the distributions of favored-events-per-sample will be different. These distributions will differ in their central-tendency indices: both in their mean and in their 'shape' (range and variance).
- Master the model by integrating all the above in understanding the ratio between the means of the two distributions

## HOW TO USE IT

Buttons
SETUP -- initializes variables and assigns as objects in the experiment as many squares as you have set with the WIDTH and HEIGHT sliders.
CREATE COMBI - allows you to use the mouse to click on squares so as to select the dice/colors for your combination of choice.  Clicking repeatedly on the same square loops the die-faces / colors through an option cycle that has as many options as the value you set for the HOW-MANY-CHOICES slider.  For instance, if the slider is set to "3" and you are working with colors, clicking repeatedly on a single square will give the colors green, blue, pink, green, blue, pink, green, etc.
HIDE/REVEAL COMBI -- toggles between hiding and revealing the secret combination. This function is useful when you pose a riddle to a friend and you do not want them to know what your chosen combination is.
SEARCH COMBI - activates the random search. The program generates random dice-faces / colors and matches the outcome against the combination you had created.

Sliders:
\#CHOICES - sets the number of dice-face / colors you wish to include in the sample space. You do not have to use for your combination as many different faces / colors as you set this slider to. For instance, you might want to set this slider to 5 but then only use two of these options.
WIDTH & HEIGHT -- sliders for setting the values of the width and height dimensions, respectively, of the combination you are creating.
SAMPLE SIZE -- sets the number of program-generated guesses per sample. At the end of each sample, the plot is updated as well as the RATIO monitor (see below)

Choices:
Analysis-Type --
- "Permutations" - That is, order does not matter, so '1 2 3' is considered the same as its permutation '3 2 1' (it is registered as a favored event)
-  "Combination" - That is, order matters, so '1 2 3' is not accepted as the same as its permutation '3 2 1' (it is not registered as a favored event)
-  "both" - That is, the experiment will analyze outcomes from both the 'Permutations' and 'Combination' perspectives, and each will be represented in the plot.

Switches:
SINGLE-SUCCESS? -- If "On," the program will stop running after it has guessed the combination correctly once.  If "Off," the program will continue running indefinitely.
DICE? - toggles between two options:
- "On" means you will be creating and searching dice faces
- "Off" means you will be creating and searching colors
BARS? -- "On" gives you a histogram in the plot window; "Off" gives you a line graph.

Monitors:
\#SAMPLES -- shows how many samples have run up to this moment in the experiment
\#STEPS IN THIS SAMPLES -- shows how many guesses (or "attempts") the program has conducted in this sample, regardless of whether or not they were successful.
COMBINATION -- shows how many successes (hit guesses) the program has performed in this sample according to the conditions that order matters
PERMUTATIONS -- shows how many successes (hit guesses) the program has performed in this sample according to the conditions that order does not matter
'#1' thru '#6' -- When you have set up your combination, these monitors will accumulate your setting according to type.  For example, if you have set DICE? to "On" and the #CHOICES to 4 and you have set up the five dice faces as  '4 2 3 3 2,' then
- Monitor #1 will show "0," because there are no '1's in your combination, even though there could have been.
- Monitor #2 will show "2" because there are two '2's in your combination.
- Monitor #3 will show "2" because there are two '3's in your combination
- Monitor #4 will show "1" because there is just a single '3' in your code.
- Monitors #5 and #6 show "N/A" ("not available") -- telling us that there cannot be any number here, since the '5' and the '6' dice faces were not in your pool of choices.
COMBI : PERMIS -- shows the ratio between the mean values of the sample outcome distributions corresponding to the conditions "combination" and "permutation," respectively. This monitor updates each time a sample has been completed.

Plot
SUCCESSES PER SAMPLE DISTRIBUTION -- shows the counts of successes per sample for the conditions selected in the ANALYSIS-TYPE slider.

Follow the instructions in the "instructions" monitor window, which will lead you through the following process:
- set the WIDTH and HEIGHT sliders or just use the default settings that are 1 by 3
- press SETUP
- set the values of the #CHOICES slider (default is 2)
- select your choice in the ANALYSIS-TYPE choice (you can change this later, too)
- press CREATE COMBI.
- set the DICE? switch either to "On," to work with dice, or "Off", to work with colors
- click on each green square repeatedly until you are happy with your combination
- optionally, press HIDE/REVEAL (you can always press this again to see your combination)
- if you have set the SINGLE-SUCCESS?' switch to "On," then the search will stop the moment it has matched your original combination, according to your choice of ANALYSIS-TYPE
- if you have set the SINGLE-SUCCESS?' switch to "Off," then the program will begin a new search and will generate random combinations on and on until you stop it. While it runs, monitors constantly update you on the progress of this process. A plot helps you track the accumulation of outcomes from the search in terms of your ANALYSIS-TYPE
- press SEARCH COMBI

## THINGS TO NOTICE

As the search procedure is running, look at the monitor #STEPS IN THIS SAMPLES. See how it is updating much faster than the monitor to its left, #SAMPLES.  The number in #SAMPLES increases by 1 each time #STEPS IN THIS SAMPLES reaches the number that is set in the slider SAMPLE SIZE.

After you have setup a combination, look at the six monitors under the view. Some of them, perhaps all of them, have numbers in them. Some might have 'N/A' ('not available') in them. In any case, there are exactly as many monitors with numbers in them as the number of choices you have set up in the slider #CHOICES.

As the search procedure is running, watch the monitors COMBINATION and PERMUTATIONS. Note whether or not they are updating their values at the same pace. For most combinations that you set, PERMUTATIONS updates much faster. This is because PERMUTATIONS registers a successeach time the model hits on the set of colors / dice-faces you selected even if they appear in a different order form what you had selected.

As the search procedure is running, watch the monitor COMBI-TO-PERMI RATIO. At first, it changes rapidly, and then it changes less and less. Eventually, it seems to stabilize on some value. Why is this so?

Unless the red histogram ("permutations") covers the black histogram ("combination") entirely, you will see that the "permutations" histogram always becomes both wider and shorter than the "combinations" histogram. What does this mean? Why is this so? We say of the wider histogram that it has a greater "variance" as compared to the narrower histogram. The "permutations" histogram (red) typically stretches over a greater range of values as compared to the "combination" histogram (black).

Also, you may notice that the "permutations" and "combination" histograms cover the same area. That is because the total area of each histogram, irrespective of their location along the horizontal axis and irrespective of their shape, indicates the number of samples they represent. We know that the two histograms represent the same number of samples. Therefore, they have the same area.

## THINGS TO TRY

Find a combination for which the monitors COMBINATION and PERMUTATIONS change at the same pace. What other features on the interface are unique for this combination?  For instance, is the plot behaving the same as before? How is the COMBI-TO-PERMIS RATIO monitor behaving?

For different settings, we get different distances between the two histograms. Which settings are these and how do the affect the distance??

If you work with a friend, then the friend can try to set up combinations on your computer.  You will need to guess what the combination is by looking at the information in the view as the program is running. What would make for a difficult combination? What would make for an easy combination? That is, are there some combinations that are harder to guess than others? Which are they?

Set the WIDTH slider at 2 and the HEIGHT slider at 1. Set the #CHOICES slider at 2. Create a combination with one green square and one blue square. Press SEARCH COMBI. Watch the plot and the COMBI-TO-PERMIS RATIO monitor. You will get a 1:2 ratio between the "combinations" and "permutation" distribution means. Now set the model again, changing only the #CHOICES slider to 3. Run the experiment. Once again, you will get a 1:2 ratio between the "combinations" and "permutation" distribution means. Has anything at all changed in your experimental results?

Create a combination and then try to figure out what the value of COMBI-TO-PERMIS RATIO will be before you run the experiment. For instance, what should the ratio be under the following settings:
- #CHOICES at 4
- In your combination, two of the squares are green, one is blue, and one is pink

## NETLOGO FEATURES

The model uses the same "looping" list containing the choices of colors (or dice faces) both for building and for searching for the combination. There are 6 optional colors or dice faces. These lists are created in the "initialize" procedure:

        set color-list [ green blue magenta cyan pink yellow]
        set dice-list ["one" "two" "three" "four" "five" "six"]

Once the user has set how many choices are wanted, part of this list is copied onto a new list -- either the 'color-rotation' list or the 'dice-rotation' list (see the "set- color-rotation " and "set-dice-rotation" procedures, respectively). For instance, for a HOW-MAY-CHOICES? setting of 3, the model will copy 'green blue magenta.' Later, in the search-combi procedure (actually, in the 'guess' sub-procedure), the program will randomly select an item from the color-rotation or dice-rotation lists. To create shorter lists form longer lists, we use a local variable, color-list-counter, that counts up as many choices as the user has set. For every count, an additional item from the list is copied onto the new list:

       repeat num-choices
       [
         set color-rotation lput (item color-list-counter color-list)
                                 color-rotation
         set color-list-counter color-list-counter + 1
       ]

These lists are "rotating" lists because in the assign-color-or-image procedure we move along the list and when we get to its end we start over from the beginning of the list, as in a loop. For instance, a patch command in the assign-color-or-image procedure is the following:

        set pcolor item ((1 + position pcolor color-rotation) mod num-choices)
                        color-rotation

The critical code word in the above line is "mod." If we have 3 colors in the color-rotation list, then "1 + position etc." could exceed the limit of only 3 colors and be 4. But there is no 4th color in the list. "mod" works like this: '5 mod 3' is 2 because 5 has 2 more than 3. But '8 mod 3' is also 2, because 8 is 2 greater than 6, which is the greatest integer multiple of 3 that is contained in 8. Try typing "show 4 mod 3" in the command center, then try "show 7 mod 3" etc.

## EXTENDING THE MODEL

Create a plot that tracks, over time, the value that is shown in the RATIO monitor.

It should be interesting to track how long it takes the model from one success to another. Add code, monitors,  and a plot to do so.

Following is an extension idea for applying this model towards thinking about search algorithms. Currently, the program guesses combinations randomly. This could be improved upon so that the program finds the combination in less guesses. For instance, the moment one of the squares has the correct color or dice face, the program would continue guessing only for the other squares. Another idea might be to create a systematic search procedure.

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

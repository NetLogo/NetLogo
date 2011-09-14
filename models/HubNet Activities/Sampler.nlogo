globals
[
   guess-averages ;; a list of the average guess for each round
   guesses        ;; a list of the guesses for the current round
   max-$$         ;; constant used for the $$-Game
]

patches-own [ true-color ] ;; value of the color of each patch is either green or blue but sometimes
                           ;; the displayed color is gray keep track of the real color here

;; client turtles keep state information about the clients
breed [ clients client ]
clients-own
[
  user-id                 ;; uniquely identifies each client
  my-sample-size          ;; current value of the my-sample-size slider on the client
  my-sampling-allowance   ;; starts at SAMPLING-ALLOWANCE and is deduced with each client sample
  my-guess                ;; current value of the MY-GUESS slider in the client
  my-sampled-patches      ;; a patch-set of all the patches currently sampled on the client
  ;; used for the $$ game
  my-go-with-group?       ;; indication whether user should be scored with his/her own guess or the group guess
  my-$$                   ;; the current score of the user
  submitted?
]

;;
;; Setup Procedures
;;

to startup
  hubnet-reset
  setup
end


to setup
  ;; don't clear the turtles since they contain
  ;; the information we need to communicate with the clients
  clear-all-plots

  ;; return variables to initial state
  ;; that means clearing information from previous rounds
  setup-variables

  cd

  ;; if random-%-green is on choose a random value
  ;; otherwise, use the %-GREEN
  let actual-%-green %-green

  if random-%-green?
  [ set actual-%-green random 101
    ;; when we're using a random-%-green
    ;; hide the slider bar so it's not misleading
    set %-green -10 ]

  ask patches
  [
    ;; colors are hidden by default
    set pcolor white - 2
    set true-color blue - 2.5
  ]

  ask n-of (actual-%-green * count patches / 100) patches
    [ set true-color green - 1 ]

  ;; cluster the colors a bit since we
  ;; don't always want a uniform distribution
  apply-abnormality-distribution

  ;; return clients to initial state
  ask clients [ setup-client ]
end

to setup-variables
  set max-$$ 100
  set guess-averages []
  set guesses []
end

to apply-abnormality-distribution
  ask n-of (4 + random 4) patches   ;; choose a slightly variable number of clusters
  [
    repeat (20 * abnormality)   ;; the larger the abnormality do more clustering
    [
      let p2 one-of patches in-radius 8 with [true-color = blue - 2.5]   ;; find a blue patch near me
      let p1 one-of patches in-radius 16 with [true-color = green] ;; find a green patch maybe not quite as near me
      if p1 != nobody and p2 != nobody  ;; if there are some
      [
        ;; swap 'em
        ask p1 [ set true-color blue - 2.5]
        ask p2 [ set true-color green - 1]
      ]
    ]
  ]
end

;;
;; Runtime Procedures
;;

to go
  ;; let the teacher sample at any time
  if mouse-down?
  [
    ;; if we're not keeping samples cover up the old one first
    if not keep-samples?
    [ ask patches [ set pcolor white - 2 ] ]
    ;; uncover a new sample at the mouse click
    ask sample-patches mouse-xcor mouse-ycor sample-size
    [ set pcolor true-color ]
  ]

  ;; process messages from the client
  listen-clients

  every 0.1 [ display ]
end

;; control the patch colors

to show-population
  ifelse organize?
  [ organize-population ]
  [ ask patches [ set pcolor true-color ] ]
end

to hide-population
  ask patches [ set pcolor white - 2 ]
end

to organize-population
  let green-fraction count patches with [true-color = green - 1] / count patches
  set %-green green-fraction * 100
  ;; this will always work out to a whole number as there are 100 columns and
  ;; only whole number percents are allowed.
  let xcor-of-divider min-pxcor + ( world-width * green-fraction )

  ask patches
  [
    ifelse pxcor < xcor-of-divider
    [ set pcolor green - 1 ]
    [ set pcolor blue - 2.5 ]
  ]
end

;; show the samples made by all the clients
to pool-samples
  ask clients
  [
    ask my-sampled-patches
    [ set pcolor true-color ]
  ]
end

;; for the $$ game, give everyone more $$
to replenish-$$
  ask clients
  [ set my-$$ max-$$ ]
  hubnet-broadcast "$$" max-$$
end

;; update the score on clients for the $$ game
to update-$$ [guess-mean]
  let group-error abs( %-green - guess-mean)
  ask clients
  [
    ;; if a client chose "go with group" use that as the error
    ;; otherwise calculate his/her own error
    let err ifelse-value my-go-with-group? [group-error][abs( %-green - my-guess )]
    ;; subtract from the score if outside the margin of error
    if err > margin-of-error
    [ set my-$$ my-$$ - ( err - margin-of-error) ]
    ;; update the client monitor
    hubnet-send user-id "$$" my-$$
  ]
end

;;
;; Plotting Procedure
;;

;; do all the plotting and bookkeeping needed to end a round
;; get ready for the next one
to plot-guesses
  let submitted-clients clients with [submitted?]
  if not any? submitted-clients
  [ user-message "No clients have submitted answers."
    stop ]
  if count submitted-clients < count clients and
     not user-yes-or-no? "There are some clients that have not submitted an answer. Do you want to continue?"
  [ stop ]

  set-current-plot "Student Guesses"

  ;; we're going to redraw the entire plot
  clear-plot

  set guesses [my-guess] of clients with [submitted?]
  let guess-mean mean guesses

  set-current-plot-pen "guesses"
  ;; sometimes we want to dump multiple ranks in a single bin
  set-histogram-num-bars 100 / ranks-per-bin
  ;; do it!
  histogram guesses

  ;; draw a vertical line for the mean
  set-current-plot-pen "mean-of-guesses"
  plot-pen-up
  plotxy guess-mean plot-y-min
  plot-pen-down
  plotxy guess-mean plot-y-max

  ;; update the $$ game stats
  update-$$ guess-mean

  ;; add the current average to the average history
  set guess-averages fput guess-mean guess-averages

  ;; plot the average history as lines
  set-current-plot-pen "means"
  foreach guess-averages
  [
    plot-pen-up
    plotxy ?  0
    plot-pen-down
    plotxy ? 25
  ]

  ;; draw a vertical line for the historical mean
  set-current-plot-pen "mean-of-means"
  plot-pen-up
  plotxy mean guess-averages 0
  plot-pen-down
  plotxy mean guess-averages 25

  ;; return clients to the initial state
  ask clients [ setup-client ]
end


;;
;; HubNet Procedures
;;

to listen-clients
  while [ hubnet-message-waiting? ]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ create-client ]
    [
      ifelse hubnet-exit-message?
      [ remove-client ]
      [ ask clients with [user-id = hubnet-message-source] [ execute-command hubnet-message-tag ] ]
    ]
  ]
end

to create-client
  create-clients 1
  [
    ;; client turtles do not appear in the view
    ;; they are only used to save state from the clients
    hide-turtle
    set user-id hubnet-message-source
    setup-client
  ]
end

;; set client variables to initial values
;; and update their monitors
to setup-client
  set my-sample-size sample-size
  set my-sampling-allowance sampling-allowance
  set my-guess 50
  set my-sampled-patches no-patches
  set my-go-with-group? false
  set my-$$ max-$$
  set submitted? false
  hubnet-send user-id "Sampling Allowance" my-sampling-allowance
  hubnet-send user-id "$$" my-$$
  hubnet-send user-id "%-green" my-guess
  hubnet-send user-id "submitted?" submitted?
  hubnet-clear-overrides user-id
end

to remove-client
 ask clients with [user-id = hubnet-message-source]
   [ die ]
end

to execute-command [cmd] ;; client procedure
  if cmd = "View" and student-sampling?
  [
    let x item 0 hubnet-message
    let y item 1 hubnet-message
    ;; get the sample for the mouse click the sample size is determined either
    ;; by my-sample-size on the client or the sample-size on the server
    let sample sample-patches x y ifelse-value student-sample-size? [my-sample-size][sample-size]
    ;; if I have enough sampling allowance left show me the patches
    if my-sampling-allowance > count sample
    [
      ;; if we're not keeping samples clear the
      ;; overrides first
      if not keep-samples?
      [
        hubnet-clear-overrides user-id
        set my-sampled-patches no-patches
      ]
      ;; send the override
      hubnet-send-override user-id sample "pcolor" [true-color]
      ;; keep track of the patches I am viewing
      set my-sampled-patches (patch-set my-sampled-patches sample)
      ;; update the sample allowance
      set my-sampling-allowance my-sampling-allowance - count sample
      ;; update the corresponding monitor
      hubnet-send user-id "Sampling Allowance" my-sampling-allowance
    ]
    stop
  ]
  ;; update the state related to interface changes on the client
  if cmd = "my-sample-size"
  [ set my-sample-size hubnet-message stop ]
  if cmd = "%-green"
  [ set my-guess hubnet-message stop ]
  if cmd = "go with group"
  [ set my-go-with-group? true stop ]
  if cmd = "submit-answer"
  [ set submitted? true
    hubnet-send user-id "submitted?" submitted?
    stop ]
end

;; give the clients their allowance
to replenish-sampling-allowance
  ask clients
  [
    set my-sampling-allowance sampling-allowance
    hubnet-send user-id "Sampling Allowance" my-sampling-allowance
  ]
end

;; get the patch agentset of the sample
;; this is moore neighborhood with radius
;; sample-size of the clicked patch
to-report sample-patches [x y width]
  let radius ( width - 1 ) / 2
  report [patches at-points n-values (width ^ 2)
          [list (? mod width - radius)
           (floor (? / width) - radius)]] of patch x y
end
@#$#@#$#@
GRAPHICS-WINDOW
41
44
451
475
-1
-1
4.0
1
10
1
1
1
0
1
1
1
0
99
0
99
1
1
0
ticks
30

SLIDER
38
10
457
43
%-green
%-green
0
100
50
1
1
NIL
HORIZONTAL

SWITCH
459
10
630
43
random-%-green?
random-%-green?
1
1
-1000

BUTTON
552
45
630
78
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
632
182
771
215
NIL
show-population
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
632
45
710
78
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
21
476
566
619
Student Guesses
%-green
# students
0.0
100.0
0.0
6.0
true
true
"" ""
PENS
"guesses" 1.0 1 -2674135 true "" ""
"mean-of-guesses" 1.0 0 -5825686 true "" ""
"means" 1.0 0 -6459832 true "" ""
"mean-of-means" 1.0 0 -11221820 true "" ""

SLIDER
632
10
804
43
abnormality
abnormality
0
10
0
1
1
NIL
HORIZONTAL

SLIDER
459
114
630
147
sample-size
sample-size
1
11
3
2
1
NIL
HORIZONTAL

BUTTON
632
216
771
249
NIL
hide-population
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
459
80
630
113
student-sampling?
student-sampling?
1
1
-1000

SWITCH
459
148
630
181
student-sample-size?
student-sample-size?
1
1
-1000

SWITCH
459
182
630
215
keep-samples?
keep-samples?
1
1
-1000

SLIDER
507
253
738
286
sampling-allowance
sampling-allowance
0
500
200
25
1
NIL
HORIZONTAL

BUTTON
507
287
738
320
NIL
replenish-sampling-allowance
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
459
216
630
249
NIL
pool-samples
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
753
343
924
376
margin-of-error
margin-of-error
0
10
1
1
1
NIL
HORIZONTAL

BUTTON
753
377
924
410
NIL
replenish-$$
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
453
395
553
440
# students
count clients
17
1
11

BUTTON
562
339
675
372
NIL
plot-guesses
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
632
80
771
113
show-grid
ask patches with [pycor = max-pycor]\n[\n   sprout 1\n   [\n     set xcor xcor + 0.4\n     set heading 180\n     set color white - 2\n     pd\n     jump world-height\n     die\n   ]\n]\n\nask patches with [pxcor = min-pxcor]\n[\n   sprout 1\n   [\n     set ycor ycor + 0.4\n     set heading 90\n     set color white - 2\n     pd\n     jump world-width\n     die\n   ]\n]
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
632
114
771
147
hide-grid
cd
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
452
442
668
475
ranks-per-bin
ranks-per-bin
1
100
2
1
1
NIL
HORIZONTAL

MONITOR
578
488
678
533
# guesses
length guesses
17
1
11

MONITOR
625
534
724
579
# rounds
length guess-averages
17
1
11

MONITOR
726
534
826
579
mean all rounds
mean guess-averages
17
1
11

MONITOR
679
488
779
533
mean this round
mean guesses
17
1
11

MONITOR
780
488
880
533
standard dev
standard-deviation guesses
4
1
11

TEXTBOX
755
326
905
344
$$ Game
11
0.0
1

MONITOR
789
411
885
456
mean $$
mean [my-$$] of clients
17
1
11

SWITCH
632
148
771
181
organize?
organize?
1
1
-1000

@#$#@#$#@
## WHAT IS IT?

Sampler is a HubNet Participatory Simulation in statistics. It is part of the ProbLab curricular models. Students engage in statistical analysis as individuals and as a classroom. Through these activities, students discover the meaning and use of basic concepts in statistics.

Students take samples from a hidden population and experience the mathematics of statistics, such as mean, distribution, margin of error, etc. The graphics in the SAMPLER interface are designed to ground students' understanding of statistics in proportional judgments of color distribution. The collaborative tools are designed to help students appreciate the power of large numbers for making inferences about populations. Students experience distributions both at an individual level -- variation in their own samples -- and at a group level -- variation in all students' guesses. This analogy is designed for students to appreciate the diversity of opinions in the classroom and the power of embracing everyone to achieve a complex task.

Learning Statistics:

In SAMPLER, statistics is presented as a task of making inferences about a population under conditions of uncertainty and limited resources. For example, if you wanted to know what percentage of students in your city speak a language other than English, how would you go about it? Would it be enough to measure the distribution of this variable in your own class? If yes, then how sure could you be that your statistic is representative of the whole city? If not, why not? Are there certain groups of people that it would make more sense to use as a sample? Are there other groups it would make no sense to use? For instance, would it make sense to stand outside a movie house that is showing a French film with no subtitles and ask each patron whether they speak a second language? Is this a representative sample? Should we look at certain parts of town? Would all parts of town be the same? Oh, and by the way, what is an average (a mean)? A variable? A value? What does it mean to measure a distribution of a variable within a population?

Many students have a very difficult time understanding statistics -- not only in middle and high school, but also in college and beyond. Yet on the other hand, there are certain visual-mental capabilities we all have--even very young children -- that could be thought of as naive statistics. These capabilities are the proportional judgments we make constantly. We make proportional judgments when we need to decide how to maximize the utility of our actions. For instance, when we come to a new place we may say, "People in this town are very nice." How did we decide that? Or, "Don't buy fruit there -- it's often overripe." How did we infer that? Or, "To get to school, take Main street -- it's the fastest route in the morning; but drive back through High street, I find that's faster in the afternoon."

## HOW IT WORKS

The teacher works in NetLogo and acts as the server for the students (the "clients") who each have their own client interface on their computer screens. Students see the teacher's interface projected on the classroom screen, and they can instruct the teacher to manipulate settings of the microworld that they do not have on their own client interfaces. The view in the projected interface features a square "population" of 3600 squares. Individual patches are either green or blue. The squares' color is the attribute we measure in SAMPLER. So, the SAMPLER color is a variable that can have one of two values: green or blue (a dichotomous variable, like a coin). In a basic SAMPLER activity, students and/or the teacher reveal(s) parts of or all the population and students discuss, approximate, take samples, and input their individual guesses as to the percentage of green patches within the revealed sector of the population. All participating students' inputs are collected, pooled, and represented in monitors and in the plot. Thus, each student constitutes a data-point agent and can experience impacting the class statistics.

Through collaboration, students are to achieve, as a class, the best possible approximation of the population.

The $$ game: At the beginning of every round and later, whenever the facilitator decides, all clients receive max-points, for instance $100. Now, students can bet either on their own guess or on the group guess. They pay 1 point for every percentile their bet is away from the truth or from the margin of error that they agree upon. The winner of a $$ game is the player with the highest points remaining after all of the rounds. This is an optional feature.

## HOW TO USE IT

Basic Activity:
If you change %-GREEN, RANDOM-%-GREEN? or ABNORMALITY you will need to press SETUP for the changes to take effect, you may also press SETUP if you want to get a new population with the current settings.

Press the GO button.  You will now be able to reveal samples of the population by clicking in the view.  However, students will not be able to take samples until STUDENT-SAMPLING? is set to true.

Before the students start sampling you might want to present questions to them, such as: 'What is this?' 'How green is this?' 'How could we figure out?'

When users login they receive their own interface. To change their personal guess for the percent green they should move the %-GREEN slider. When the user has a final guess, s/he should press the SUBMIT-ANSWER button (otherwise the guess will not be counted).

After all the students have submitted guesses press the PLOT-GUESSES button which will plot all data from this round and advance to the next.  You cannot advance the activity if no students have submitted the answers.  If there are any students that have no submitted answers you will be warned, though you may continue if you wish. Each round is simply a period in which students may make guesses about the greenness of the population.  When a new round begins the students' submitted? flag will be reset to false so they can make another guess.  The plots are kept from round to round and the population does not change.  If you wish to change the population press the SETUP button (this will clear all plotted data too).

$$ Game:
The procedure to play the $$ game is similar to the basic activity, take samples, guess the % green and press the SUBMIT button.  Then the student should also decide to either bet on that guess or on the average guess among all students. By default students are scored using their own guesses.  To change this they should press the GO WITH GROUP button.  Students will be scored on how close their bet is to the actual percent green in the population.

Buttons:
SETUP - Creates a new patch population with a new %-green (or random percent green if RANDOM-%-GREEN? is enabled) and the new value of abnormality.  Clears the plot and data from all rounds. Students need not log out, user names and student scores will not be lost.

GO - Starts the activity, the teacher can always reveal samples by clicking in the view. The students can only take samples if STUDENT-SAMPLING? is enabled.

SHOW/HIDE-GRID - Turns on and off the grid that shows clear dividing lines between patches.

SHOW/HIDE-POPULATION - reveal the true color (green or blue) of each patch, or return any sampled patches to gray. If ORGANIZE? is true all the green patches will appear on the left and all the blue patches on the right.  If you want to "disorganize" the population, turn the ORGANIZE? switch off and press SHOW-POPULATION again.

POOL-SAMPLES - reveal all the samples taken by the server and the client.

PLOT-GUESSES - histograms the collected guesses in the plot. Does the bookkeeping required at the end of a round and prepares for the next round. Once you have pressed PLOT-GUESSES the current round has ended and the next round has begun.

REPLENISH-SAMPLING-ALLOWANCE - resets each of the clients' sampling allowance to SAMPLING-ALLOWANCE.

Sliders:

%-GREEN - controls the percent of patches that are green if RANDOM-%-GREEN? is off.

ABNORMALITY - controls to what extent the distribution deviates from 'normal' (for a given percent green you'll get larger clumps for a larger setting).

SAMPLING-ALLOWANCE - The total number of patches clients are allowed to reveal. The teacher may REPLENISH-SAMPLING-ALLOWANCE to set all clients back to SAMPLING-ALLOWANCE.

SAMPLE-SIZE - determines the number of patches on a side of a sample block. For instance, SAMPLE-SIZE of 5 reveals a block of 25 patches. If STUDENT-SAMPLE-SIZE? is off this is also the sample size on the clients.

Switches:

STUDENT-SAMPLING? - if true, students can sample; otherwise not.

STUDENT-SAMPLE-SIZE? - if true, students can size of their samples; otherwise not.

RANDOM-%-GREEN? - if true when SETUP is pressed, a random percentage green patches is chosen. Otherwise %-green is used.

KEEP-SAMPLES? - when sampling, if true, old samples are still displayed.  If false, old samples are removed and cannot be seen.

ORGANIZE? - if true all the green patches will be pushed to the left and the blue will be pushed to the right when you press the SHOW-POPULATION button. Otherwise, the patches will be show as their true colors.

Monitors:

\# STUDENTS - shows the number of connected clients.

\# GUESSES - shows how many guesses were collected when you last pressed PLOT GUESSES.

MEAN THIS ROUND - shows the average of guesses that are currently plotted in the histogram.

STANDARD DEV - shows the standard deviation of guesses plotted in the histogram.

\# ROUNDS - shows how many rounds have been played since the last time SETUP was pressed.  A round is a period in which students may make guess about the greenness of a given population.  A round ends and a new one begins each time the PLOT-GUESSES button is pressed. This is reset when you press SETUP.

MEAN ALL ROUNDS - the cumulative average for all rounds per this population (since you last pressed SETUP).

Plots:

AVERAGES OF STUDENT GUESSES - X-axis is %-GREEN and Y-axis is # STUDENTS. Here you see four statistics as displayed by four different plot pens:

  1. GUESSES: Students' collected guesses for a round represented in histograms.
  2. MEAN-OF-GUESSES: the average value of guesses for the recent round.
  3. MEANS: the average values from successive rounds.
  4. MEAN-OF-MEANS: the average value of 'means'.

Client Interface:

%-GREEN - The user's guess for the percent green.
SAMPLING ALLOWANCE - the number of patches left in the user's sampling allowance.
MY-SAMPLE-SIZE - the width of the sample blocks given that STUDENT-SAMPLE-SIZE? is on.
SUBMIT-ANSWER - let the server know that you've locked in the current value of %-GREEN as your guess for this round.
SUBMITTED? - false until the user presses SUBMIT-ANSWER this round.

For the $$ Game only:

REPLENISH $$ - resets each of the client's my-$$ to the starting quantity.  Clients' $$-REMAINING are never replenished unless you press this button.

MARGIN-OF-ERROR - This determines how accurate the guess has to be in order to be correct. For example, if it's set at 3 and the greenness is 70 then you can guess between 67 and 73 and not have points taken off, but if you guess 74 or 66 you get 1 point off, etc.

CLASS MEAN $$ - shows the mean of students' MY-$$.

$$ Game on the client:

GO WITH GROUP - When scoring use the group guess rather than this individual's guess.
$$ - the $$ remaining for this client (essentially his/her score).

## THINGS TO NOTICE

When you set ORGANIZE? to on and press SHOW-POPULATION , the green patches move left and the blue patches move right in the view, forming a contour line.  This line should fall directly below the slider handle above it and similarly should line up with the mean line in the plot. The reason we can compare these three features directly is because the 0 and 'whole' (100%) of each of these features are aligned. That is, the sliders, view, and plot have all been placed carefully so as to subtend each other precisely.

The abnormality distribution feature does not take much code to write, but is effective. Look at the code and try to understand it.

## THINGS TO TRY

Set RANDOM-%-GREEN? to true, press SETUP, and take samples. What is the minimal number of samples you need in order to get a good idea of the distribution of colors in the population? How 'good' must a good idea be? Can you think of a way of describing this 'goodness'? What is a good way of spreading the samples on the population?

Try setting the ABNORMALITY slider to different values and press SETUP over and over for the same percentage green, for instance 50%. Can you think of situations in the world where a certain attribute is distributed in a population in a way that corresponds to a high value of ABNORMALITY? What do we mean when we speak of a 'uniform distribution' within a population? For instance, is a distribution of ABNORMALITY = 0 uniform? Or must there be strict order, for instance stripes of target-color, in order for you to feel that the distribution is uniform? Also, is there a difference between your sense of uniformity whether you're looking at the whole population or just at certain parts of it? If you threw a handful of pebbles onto a square area, would you say they fell 'uniformly'? What kinds of patterns are natural, and what kinds of patterns would you think of as coincidental?

## EXTENDING THE MODEL

What other quantitative aspects of sampling might a teacher or student need so as to understand and do more in this activity? Perhaps the class would want to keep a record of how well they are doing over an entire lesson. How would you quantify such performance and how would you display it? Would a plot be useful for this or just a list of numbers?

## NETLOGO FEATURES

Since one of the most common configurations of this model is a 50-50 split between green and blue, the world has an even number of columns and rows so that there are exactly 50% of the patches that are green rather than a close approximation. Since an even grid is required the origin was moved to the lower left corner instead of being slightly off-center near the middle of the world.

This activity uses HUBNET-SEND-OVERRIDE to reveal the samples in the client views.

## RELATED MODELS

All models in ProbLab deal with probability and statistics in ways that may enrich student understanding of sample space, randomness, and distributions. In particular, many models share with SAMPLER the 3-by-3 sample that we call a "9-block."

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

frame
false
14
Rectangle -7500403 false false 0 1 296 296
Rectangle -7500403 false false 0 2 297 297
Rectangle -7500403 false false 0 0 298 298
Rectangle -7500403 false false 1 3 295 295
Rectangle -7500403 false false 1 1 299 299

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
VIEW
12
10
512
510
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
99
0
99

SLIDER
64
603
205
636
my-sample-size
my-sample-size
1
11
5
2
1
NIL
HORIZONTAL

MONITOR
206
597
324
646
Sampling Allowance
NIL
3
1

SLIDER
6
513
521
546
%-green
%-green
0
100
50
1
1
NIL
HORIZONTAL

BUTTON
324
655
435
688
go with group
NIL
NIL
1
T
OBSERVER
NIL
NIL

MONITOR
206
647
323
696
$$
NIL
3
1

BUTTON
325
557
436
590
submit-answer
NIL
NIL
1
T
OBSERVER
NIL
NIL

MONITOR
206
547
324
596
submitted?
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

globals [
         ;; colors of the background of the view and of the two possible colors in samples
         background-color column-color target-color other-color
         num-columns ;; how many columns there are in the graphics-window histogram
         num-target-color  ;; how many of the squares (patches) in the sample are of the favored color
         sample-right-xcor ;; the x-coordinate of the moving sample (not the magnified sample)
         sample-location-patch-agentset ;; patches where the moving sample will sprout
         token-sample-dude ;; bottom-left turtle in the moving sample
         stop-all? ;; Boolean variable for stopping the experiment
         side ;; tells how big the side of the block is--2 for 4-block

         popping?
         ]

patches-own [ column ]
breed [ column-kids column-kid ]
breed [ sample-dudes sample-dude ]
breed [ baby-dudes baby-dude ]

to Go-org
  if stop-all? [stop]
    super-go
    organize-results
end

to super-go
  if stop-all? [stop]
  ifelse popping? [
    no-display
    unpop
    go
    pop
    display
  ] [
    go
    display
  ]
end

;; the popping? global controls the popping visuals
to pop
  set popping? true
  recolor-columns
end

to unpop
  set popping? false
  recolor-columns
end

;; jwu - different color for each sample-summary-value
to-report popping-color ; sample-organizers procedure
  report 15 + ((sample-summary-value * 10) mod 120)
end

;; jwu - instead of having the sample-dudes stamp, they're going to create
;; a sample-organizer. the sample-organizers are going to have a better idea
;; of which specific sample the sample-dudes represented.
breed [ sample-organizers ]
sample-organizers-own [
  sample-values
  original-pycor
]

to-report sample-summary-value ; sample-organizers reporter
  let result 0
  let power-of-two 3
  foreach sample-values [
    if ? = 1 [
      set result result + 2 ^ power-of-two
    ]
    set power-of-two power-of-two - 1
  ]
  report result
end

to-report sample-patches ; sample-organizers procedure
  let result []
  foreach n-values side [?][
    let i ?
    foreach n-values side [?] [
      set result lput patch-at ? (- i) result
    ]
  ]
  report result
end

to display-sample ; sample-organizers procedure
  let patch-popping-color popping-color
  (foreach sample-values sample-patches [
    ask ?2 [
      ifelse popping? [
        set pcolor patch-popping-color
      ] [
        ifelse ?1 = 1 [
          set pcolor target-color
        ] [
          set pcolor other-color
        ]
      ]
    ]
  ])
end

to recolor-columns
  reset-column-colors
  ask sample-organizers [
    display-sample
  ]
end

to reset-column-colors ; column-kids procedure
  ask column-kids [
    ask patches with [ pcolor != black and
                       (pxcor = [pxcor] of myself or
                        pxcor = [pxcor] of myself - 1) ] [
      set pcolor [pcolor] of myself
    ]
  ]
end

to make-a-sample-organizer ; sample-dudes procedure
  hatch-sample-organizers 1 [
    ht
    set sample-values map [ ifelse-value ([color] of ? = target-color) [1] [0] ]
                          sorted-sample-dudes
    display-sample
  ]
end

to organize-results
  ask sample-organizers [
    if original-pycor = 0 [
      set original-pycor pycor
    ]
  ]
  ask column-kids [
    organize-column
  ]
  recolor-columns
end

to organize-column ; column-kids procedure
  let column-organizers sample-organizers with [ pxcor + 1 = [pxcor] of myself ]
  (foreach sort-by [ [ sample-summary-value ] of ?1 <=
                     [ sample-summary-value ] of ?2
                   ] [self] of column-organizers
           sort [ pycor ] of column-organizers
           [ ask ?1 [set ycor ?2] ])
end

to disorganize-results
 ask sample-organizers [
   set ycor original-pycor
 ]
 recolor-columns
end

sample-dudes-own [ distance-for-jump ]
column-kids-own [ binomial-coefficient sample-list ]

to startup
  ;set total-samples
end

;; This procedure colors the view, divides patches into columns of equal length ( plus a single partition column),
;; and numbers these columns, beginning from the left, 0, 1, 2, 3, etc.
to setup
  clear-all
  set-default-shape turtles "square big"
  clear-output
  set background-color white - 1;black
  set column-color grey
  set target-color green
  set other-color blue
  set side 2

  set popping? false

  ;; num-columns is how many columns (bars) there are in the graphics-window histogram.
  ;; We need side ^ 2 + 1 columns in a histogram. For example, 3-by-3 samples (9 patches)
  ;; have 10 -- that is, 3 ^ 2 + 1 -- different possible counts of target-color (0, 1, 2, 3, ...9).
  set num-columns ( side ^ 2 + 1)

  ;; determines the location of the sample array beginning one column to the left of the histogram
  set sample-right-xcor -1 * round ( ( num-columns / 2 ) * ( side + 1 ) )

  ;; assigns each patch with a column number. Each column is as wide as the value set in the 'side' slider
  ask patches
  [
    set pcolor background-color
    ;; The following both centers the columns and assigns a column number to each patch
    ;; We use "side + 1" and not just "side" so as to create an empty column between samples
    set column floor ( ( pxcor + ( ( num-columns * ( side + 1 ) ) / 2 ) ) / ( side + 1 ) )
    if column < 0 or column >= num-columns
      [ set column -100 ]
  ]

  ;; leave one-patch strips between the columns empty
  ask patches with
  [ [column] of patch-at -1 0 != column ]
  [
    set column -100  ;; so that they do not take part in commands that report relevant column numbers
  ]

   ;; colors the columns with two shades of some color, alternately
   ask patches
   [
     if column != -100
     [
       ifelse int ( column / 2 ) = column / 2
       [ set pcolor column-color ][ set pcolor column-color - 1 ]
     ]
   ]

  ;; This draws the  base-line and creates a sample-kids turtle at the base of each column
  ask patches with
  [ ( pycor = -1 * max-pycor + side + 3 ) and  ;; The base line is several patches above the column labels.
    ( column != -100 ) ]
  [
    set pcolor black
    if [column] of patch-at -1 0 != column   ;; find the leftmost patch in the column...
    [
      ask patch (pxcor + side - 1)  ;; ...then move over to the right of the column
                ( -1 * max-pycor + 1 )
        [ set plabel column ]
      ask patch (pxcor + floor (side / 2))  ;; ...then move over to the middle of the column
                ( -1 * max-pycor + 1 )
      [
       sprout 1
        [
          hide-turtle
          set color pcolor
          set breed column-kids
          set sample-list []

          ;; each column-kid knows how many different combinations his column has
          set binomial-coefficient item column binomrow (num-columns - 1)
        ]
      ]
    ]
  ]
  set stop-all? false
  set num-target-color false
  reset-ticks
end

to go
  if stop-all? [stop]
  ;; The model keeps track of which different combinations have been discovered. Each
  ;; column-kid reports whether or not its column has all the possible combinations. When bound? is true,
  ;; a report from ALL column-kids that their columns are full will stop the run.
  if stop-at-all-found? [if count column-kids with [length remove-duplicates sample-list = binomial-coefficient] = count column-kids
    [stop]]
  sample
  ifelse magnify? [ magnify-on-side ] [ ask baby-dudes [ die ] ]
  drop-in-bin

  tick
  if plot? [ histogram-blocks ]
end

;; This procedure creates a square sample of dimensions side-times-side, e.g., 3-by-3,
;; located to the left of the columns. Each patch in this sample sprouts a turtle.
;; The color of the sample-dudes in this square are either target-color or other-color,
;; based on a random algorithm (see below)
to sample
  ;; creates a square agentset of as many sample-dudes as determined by the 'side' slider,
  ;; positioning these sample sample-dudes at the top of the screen and to the left of the histogram columns
  set sample-location-patch-agentset patches with
  [
    ( pxcor <= sample-right-xcor ) and
    ( pxcor > sample-right-xcor - side ) and
    ( pycor > ( max-pycor - side ) ) ]

  foreach sort sample-location-patch-agentset
  [
    ask ?
    [
      sprout 1
      [
        ht
        set breed sample-dudes
        setxy pxcor pycor

      ;; Each turtle in the sample area chooses randomly between the target-color and the other color.
      ;; The higher you have set the probability slider, the higher the chance the turtle will get the target color
      ifelse random 100 < probability-to-be-target-color
        [ set color target-color ]
        [ set color other-color ]
      st
      ]
    ]
  ]
  ;; num-target-color reports how many sample-dudes in the random sample are of the target color
  set num-target-color count sample-dudes with [ color = target-color ]
end

;; procedure in which the sample turtles create an enlarged duplicate on the left side of the screen and this enlarged sample makes
;; a large duplicate of the sample. This helps users see the samples that may otherwise be too small to see comfortably.
;; Samples are small for side = 3 because we want the entire sample space to fit into the view.
to magnify-on-side
  ask baby-dudes [die] ;; clears the way for new magnified sample
  ask sample-dudes
  [
    hatch-a-big-baby
  ]
end

to hatch-a-big-baby ;; sample-dudes procedure
  hatch 1
  [
    set breed baby-dudes
    set size 12 * ( 8 - side ) / side
    ;; This is tricky.  We want to center the new turtles vertically, and
    ;; put them to the right of all the columns, with a little space between.
    ;; The code is complicated, because it is supposed to work for other x-blocks, too
    setxy -1 * ( size * .35 ) + ( sample-right-xcor + ( .35 * size * ( xcor - sample-right-xcor ) ) )
          ( ( side - 1 ) * size / 2 ) + ( .35 * size * ( ycor + min-pycor ) )
    set size size * .33
  ]
end

;; This procedure moves the random sample sideways to its column and then down above other previous samples
;; in that column.
to drop-in-bin
  find-your-column
  descend
end

;; The random sample moves to the right until it is in its correct column, that is, until it is in the column
;; that collects samples which have exactly as many sample-dudes of the target color as this sample has.
;; The rationale is that the as long as the sample is not in its column, it keeps moving sideways.
;; So, if the sample has 9 sample-dudes (3-by-3) and is moving sideways, but 6 of them are not yet in their correct column,
;; the sample keeps moving. When all of the 9 sample-dudes are the sample's correct column, this procedure stops.
to find-your-column
  ask sample-dudes [ set heading 90 ]
  while
  [ count sample-dudes with [ column = num-target-color ] != side ^ 2  ]
  [
    ask sample-dudes
    [ fd 1 ]
  ]
end

;; Moves the sample downwards along the column until it is either on the base line or
;; exactly over another sample in that column.
to descend
  let lowest-in-sample min [ pycor ] of sample-dudes
  ask sample-dudes
  [ set heading 180 ]

  ;; The lowest row in the square sample is in charge of checking whether or not the sample has arrived all the way down
  ;; In order to determine who this row is -- as the samples keeps moving down -- we find a turtle with the lowest y coordinate
  ;; checks whether the row directly below the sample's lowest row is available to keep moving down
  set token-sample-dude one-of sample-dudes with [ pycor = lowest-in-sample ]
  while
  [
    ( [ [pcolor] of patch-at 0 -2 ] of token-sample-dude ) != black  and
    ( [ [pcolor] of patch-at 0 -2 ] of token-sample-dude ) != target-color and
    ( [ [pcolor] of patch-at 0 -2 ] of token-sample-dude ) != other-color
  ]
  [
    ;; As in find-your-column, shift the sample one row down
    ask sample-dudes
    [ fd 1 ]

  ;; Instead of establishing again the lowest row in the sample, the y coordinate of the row
  ;; gets smaller by 1 because the sample is now one row lower than when it started this 'while' procedure
  set lowest-in-sample ( lowest-in-sample - 1 )
  ]

  ;; Once sample-dudes have reached as low down in the column as they can go (they are on top of either the base line
  ;; or a previous sample) they might color the patch with their own color before they "die."
  finish-off

  ;; If the column has been stacked up so far that it is near the top of the screen, the whole supra-procedure stops
  ;; and so the experiment ends
  if max-pycor - lowest-in-sample < ( side + 1 ) [ set stop-all? true ]
end


;; we can't sort by who number, because who numbers get reused in weird ways, it seems.
to-report sorted-sample-dudes
;report sort sample-dudes
report sort-by [
         (([pxcor] of ?1 < [pxcor] of ?2) and ([pycor] of ?1 = [pycor] of ?2)) or
         (([pycor] of ?1 > [pycor] of ?2))
       ] sample-dudes
end

to finish-off
  ;; creates local list of the colors of this specific sample, for instance the color combination of a 9-square,
  ;; beginning from its top-left corner and running to the right and then taking the next row and so on
  ;; might be "green green red green red green"
  ;; jwu - need to use map and sort instead of values-from cause of
  ;; the new randomized agentsets in 3.1pre2
  let sample-color-combination map [ [color] of ? ] sorted-sample-dudes

  ;; determines which turtle lives at the bottom of the column where the sample is
  let this-column-kid one-of column-kids with [ column = [ column ] of token-sample-dude ]

    ;; make the upper left sample-dude create a sample-organizer
    let the-sample-sample-dude max-one-of (sample-dudes with-min [ pxcor ]) [ pycor ]

  ;; accepts to list only new samples and makes a previously encountered sample if keep-duplicates? is on
  ifelse not member? sample-color-combination [sample-list] of this-column-kid
  [
    ask the-sample-sample-dude [
      make-a-sample-organizer
    ]
    ask sample-dudes
    [ die ]
  ]
  [
    ifelse keep-repeats? [
    ask the-sample-sample-dude [
      make-a-sample-organizer
    ]
    ask sample-dudes
    [ die ]
    ] [
      ask sample-dudes
        [  die ]
    ]
  ]
    ask this-column-kid
    [ set sample-list fput sample-color-combination sample-list ]

end

;; procedure for calculating the row of coefficients
;; column-kids needs their coefficient so as to judge if their column has all the possible different combinations
to-report binomrow [n]
  if n = 0 [ report [1] ]
  let prevrow binomrow (n - 1)
  report (map [?1 + ?2] fput 0 prevrow
                        lput 0 prevrow)
end

;;if the model has been run, report the number of patches
;;with the target-color -- otherwise, display "--"
to-report #-target-color
  ifelse ticks != 0
  [ report count patches with [ pcolor = target-color ] ]
  [ report "--" ]
end

;;if has been run, report the number of patches
;;with the other-color -- otherwise, display "--"
to-report #-other-color
  ifelse ticks != 0
  [ report count patches with [ pcolor = other-color ] ]
  [ report "--" ]
end

;; reports the proportion of the sample space that has been generated up to now
to-report %-full
  ifelse samples-found = 0
    [ report precision 0 0 ]
    [ report precision ( samples-found / ( 2 ^ ( side ^ 2 ) ) ) 3 ]
end

to-report samples-found
  report sum [ length remove-duplicates sample-list ] of column-kids
end

to-report total-samples-to-find
  report precision ( 2 ^ ( side ^ 2 ) ) 0
end

to histogram-blocks
  let sample-value-summaries [ sample-summary-value ] of sample-organizers
  let possible-values n-values (2 ^ (side * side)) [?]
  let results []
  foreach possible-values [
    let i ?
    set results lput length filter [? = i] sample-value-summaries results
  ]

  set-current-plot "Events by Number of Outcomes"
  let max-results max results
  if mean results > 0 [ set-plot-x-range 0 (max-results + 1) ]
  set-current-plot-pen "Histogram"
  histogram results
  set-current-plot-pen "Mean"
  let mean-results mean results
  plot-pen-reset
  plotxy mean-results 0
  plotxy mean-results plot-y-max
end
@#$#@#$#@
GRAPHICS-WINDOW
270
15
553
571
45
87
3.0
1
10
1
1
1
0
1
1
1
-45
45
-87
87
0
0
1
ticks
30

SLIDER
300
570
529
603
probability-to-be-target-color
probability-to-be-target-color
0
100
50
1
1
%
HORIZONTAL

BUTTON
140
60
265
103
Go
super-go
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
10
15
265
58
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

SWITCH
50
250
230
283
keep-repeats?
keep-repeats?
0
1
-1000

MONITOR
70
360
209
405
events found
(word samples-found \"/\" total-samples-to-find \"=\" %-full)
3
1
11

SWITCH
50
320
230
353
magnify?
magnify?
1
1
-1000

SWITCH
50
285
230
318
stop-at-all-found?
stop-at-all-found?
1
1
-1000

BUTTON
10
60
135
103
Go Once
super-go
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
10
115
135
151
Organize
Organize-Results
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
10
155
135
191
Disorganize
Disorganize-Results
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
140
115
265
190
Go-Org
Go-org
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
10
205
135
238
Paint
pop
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
140
205
265
238
Unpaint
unpop
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

PLOT
10
455
260
605
Events by Number of Outcomes
Number of Outcomes per Event
# Events
0.0
20.0
0.0
15.0
true
false
"" ""
PENS
"histogram" 1.0 1 -16777216 true "" ""
"Mean" 1.0 0 -2674135 true "" ""

SWITCH
90
415
193
448
plot?
plot?
0
1
-1000

@#$#@#$#@
## WHAT IS IT?

4-Block Stalagmites is part of the ProbLab middle-school curricular material for learning probability. Related materials are a random generator called a "marbles scooper" and a sample space called a "combinations tower". In classroom activities, students working with 4-Block Stalagmites will have interacted with the marbles box and built its sample space and thus would have inferred expectations as to outcome distributions in hypothetical experiments with the marbles box.

4-Block Stalagmites is designed to enable users to experience insights into the binomial phenomenon and in particular to witness and understand the emergence of experimental outcome distributions that, by and large, are both consistent and in accord with proportions in the sample space. The model includes an interactive simulation of a binomial experiment with a sample size of four, which is comparable to an experiment of tossing four coins over and over, only that the four "coins" can land on green or blue, not heads or tails, and each "coin" has a fixed position in a 2-by-2 table that we call a 4-Block, unlike a set of four coins that has not inherent structure and lands "all over the place" on the table.

A unique feature of the model is that the outcome distribution is composed of the actual experimental samples themselves that are stacked one above the other in their corresponding columns. This is different from classical histograms that do not record which specific samples were taken but only their aggregate properties. For example, the particular 4-Blocks sampled appear in the distribution, rather than as just a record of the fact that a 4-Block with 3 green and 1 blue squares (in any order) were sampled.

The outcome distribution is in the form of "stalagmites" of stacked samples that have "dripped" down into their correct column.  This creates a picto-graph histogram that grows bottom-up like a stalagmite.  When the probability in the model is set at 0.5, this stalagmite will grow to 1:4:6:4:1 proportions.  For other p values, the stalagmite will be tailed.

## PEDAGOGICAL NOTE

There are four unique 4-blocks that each has exactly one green square, but there are six unique 4-blocks with exactly two green squares each.  So, for a p value of .5 (when independent squares are equally likely to be green or blue), it is 1.5 times more likely to draw a two-green 4-block than a one-green 4-block (the ratio value of 6 to 4 is 1.5).  This is worthy of attention, because students often need help in understanding how permutations are relevant to combinatorial analysis and, moreover, how combinatorial analysis is relevant to predicting the outcome distribution.
When the sorting and coloring effects are activated as the simulation is running, the visual effect of the growing stalagmite is as though it is "stretching" the sample space of 16 elemental events into an outcome distribution of about 160 samples.
Within columns we always expect all the elemental events to occur as frequently. However, this is true between columns only for a p value of .5. Note that we can change the p value and thus affect the overall shape of the stalagmites. For example, for the p value of .6, the two-green 4 Blocks will not occur as often as they would for a p value of .5, but the three-green 4 Blocks will occur more often than for a p value of .5. Due to the specifics of this change, the two-green and three-green columns are anticipated to be equally tall.

## HOW IT WORKS

4-Blocks are randomly generated by asking each square to choose a color with a 'probability-to-be-target-color' chance of being green.  Each 4-Block sample "drips" down one of the five columns in accord to its number of green squares.  The stalagmite distribution can be sorted by type, even as it grows.  There are 16 unique outcomes, so sorting the experimental outcomes by type results in 16 groups.  These 16 groups are typically of uneven size, even for the p value of .5, but most often their sizes revolve around the average.  For example for 160 samples taken, most groups will contain roughly 10 outcomes.  You can "paint" these groups to enhance their visual groupiness.

## HOW TO USE IT

Buttons:
SETUP-initializes the View, essentially "emptying" the columns, and resets the variables and monitors.
GO ONCE-generates a single 4-Block and sends it down its respective chute, whereas GO does so forever until one of the columns reaches the top of the display.
GO-ORG-begins a run in which the samples sort themselves by type (see SORT OUTCOMES, below)
ORGANIZE -rearranges outcomes within each column so that identical 4-blocks are grouped;
DISORGANIZE -undoes this rearrangement.
PAINT-colors outcomes by type so that identical 4-blocks appear of uniform color (the colors themselves are arbitrary-there is no inherent meaning or scaling);
UNPAINT-returns the 4-blocks to their original appearance.

Switches:
KEEP-REPEATS?-when Off, repeated outcomes are discarded from the Stalagmite.  For example, say the simulation has already generated a 4-block with a single green square in the top-left corner.  Any time later in the run, if the simulation generates another identical 4-block, it will descend the column and then disappear the moment it hits the stalagmite.  But a 4-block with a single green square in the bottom-left corner would be kept, if it had not been generated.  When On, repetitions are kept (as in standard outcome distributions).
STOP-AT-ALL-FOUND?-when On, the run will end as soon as all 16 unique outcomes of the sample space have been randomly sampled.  When Off, the run will continue until one of the columns reaches the top of the display.
MAGNIFY?-when 'On,' a blown-up version of newly created 4-blocks is displayed to the side of the column.  This helps, because the samples themselves are small and move fast.  When Off, no blown-up sample is displayed.

Slider:
PROBABILITY-TO-BE-TARGET-COLOR-determines the chance that each independent square in a 4-block will be green.  For example, a value of 50 (50% or .5) means that each square has an equal chance of being green or blue, whereas a value of 80 means that each square has a 80% chance of being green and 20% chance of being blue.
Monitors:
EVENTS FOUND-keeps track of how many of the 16 possible 4-block outcomes have been randomly sampled.

Plot:
EVENTS BY NUMBER OF OUTCOMES-shows how the sixteen elemental events are distributed by the number of outcomes sampled for each. When the first sample is taken, that event would be a '1' whereas all the other fifteen events are still at zero.

## THINGS TO NOTICE

Setup the model in its default settings (with the 'probability' slider set to the value of 0.5 and the 'magnify?' switch set 'On'), slow down the model, using the speed slider above the View, and press 'Go'.  See how a random 4-block sample is generated at the top of the View, just to the left of the stalagmite columns.  Count up the number of green squares in this 4-block and see that the 4-block descends down a column bearing the corresponding numeral at the bottom.  For example, if there are exactly two green squares in the random 4-block, it will go down the column with a "2" at the base.

Keep running the model slowly.  See how samples are stacked on top of each other in the columns.  Look closely at these samples and see if you can locate repeated outcomes, for example see if the 4-block with exactly two green squares in a particular diagonal formation occurred at least twice.

## THINGS TO TRY

Set KEEP-REPEATS? to 'On' and STOP-AT-ALL-FOUND? to 'Off'. Press 'Go.'  The columns will fill up until one of them hits the top, causing the run to stop.  Compare the heights of the columns.  What might you say about the relationship between these heights?  Repeat this experiment and see whether any general pattern recurs.

Press SETUP then GO and wait until the run ends.  Now press ORGANIZE.  What happened? Press DISORGANIZE and then ORGANIZE, and watch the effect on the outcomes in the columns.  Now press PAINT under each of the ORGANIZE and ORGANIZE conditions.  When the outcomes are both organized and painted, what can you say about the relation among the sizes of the colored groups?  That is, over repeated trials, is there any pattern in the relative sizes of these groups, or is it completely arbitrary?

Set KEEP-REPEATS? to 'Off' and STOP-AT-ALL-FOUND? to On.  When you press GO the model will keep running until it has randomly sampled all of the unique outcomes in the sample space.  How many samples, on average, are required in order to fill the entire sample space?  Does this number change according to the settings of the probability?  For example, if the probability is set at 80%, does it take as many trials to fill the sample space as compared to a setting of 50%? If not, why not?  How about the extreme cases of 0% or 100%?

## EXTENDING THE MODEL

Add monitors and/or graphs to explore aspects of the experiments that are difficult to see in the current version.  For instance:
o
      How many trials does it take for the experiment to produce an all-green 4-block?  How is this dependent on the various settings?

o
       Are there more samples with an even number of green squares as compared to those with an odd number of green squares?

o  How symmetrical is the set of stalagmites?  How would you define "symmetry?"  How would you quantify and display its changes over time?

## NETLOGO FEATURES

## RELATED MODELS

Some of the other ProbLab (curricular) models, including SAMPLER-a HubNet Participatory Simulation-feature related visuals and activities.  In Stochastic Patchwork and especially in Sample Stalagmite you will see larger blocks, such as an arrays of green and blue squares.  In the Stochastic Patchwork model and especially in 9-Blocks model, we see frequency distribution histograms.  These histograms compare in interesting ways with the shape of the stalagmites in this model.

## CREDITS AND REFERENCES

Thanks to Dor Abrahamson for the design and of this model as well as the implementation of the original model. Thanks to Josh Unterman for implementing the advanced procedures.
This model is a part of the ProbLab Curriculum, originally under development at Northwestern's Center for Connected Learning and Computer-Based Modeling and now also at the Embodied Design Research Laboratory at UC Berkeley.  For more information about ProbLab, please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.
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

link
true
0
Line -7500403 true 150 0 150 300

link direction
true
0
Line -7500403 true 150 150 30 225
Line -7500403 true 150 150 270 225

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

square big
false
0
Rectangle -7500403 true true 0 0 300 300

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
setup
repeat 150 [ go ]
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
1
@#$#@#$#@

globals [
         ;; colors of the background of the view and of the two possible colors in samples
         background-color column-color target-color other-color
         num-target-color  ;; how many of the squares (patches) in the sample are of the favored color
         sample-right-xcor ;; the x-coordinate of the moving sample (not the magnified sample)
         sample-location-patch-agentset ;; patches where the moving sample will sprout
         token-right-sample-dude ;; bottom-left turtle in the moving sample
         token-left-sample-dude ;; bottom-left turtle in the moving sample
         stop-all? ;; Boolean variable for stopping the experiment

         popping?

         left-sample-summary-value ; helps figure out which column to drop in

         ; for the histograms
         individual-4-blocks-list
         categorized-4-blocks-list
         ]

patches-own [ column ]
breed [ column-kids column-kid ]
breed [ sample-dudes sample-dude ]
breed [ baby-dudes baby-dude ]

;; JWU: added these two breeds to make the left-hand stuff
breed [ left-column-kids left-column-kid ]
breed [ left-sample-dudes left-sample-dude ]
breed [ left-dummies left-dummy ] ; they're hatched when the left-sample-dudes die. they stick around (unless "bumped")


to Go-org
  super-go
  organize-results
end

to super-go
  if stop-all? [
      ifelse stop-at-top? [
      stop
    ] [
      bump-down
    ]
  ]

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
  plot-it
end

;; jwu - the popping? global controls the popping visuals
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
  ;;report 15 + ((4 * sample-summary-value * 10) mod 120)
  report 15 + ((sample-summary-value * 10) mod 120)
end

;; jwu - instead of having the sample-dudes stamp, they're going to create
;; a sample-organizer. the sample-organizers are going to have a better idea
;; of which specific sample the sample-dudes represented.
;; also, this way we can kill the old sample dudes to help the new ones find their place.
breed [ sample-organizers sample-organizer ]
sample-organizers-own [
  sample-values
  original-pycor
]

;; each 4-block gets a value greenness in the individual spots count for this much:
;; 84
;; 21
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
  foreach n-values 2 [?][
    let i ?
    foreach n-values 2 [?] [
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
    set heading 180

    set categorized-4-blocks-list lput column categorized-4-blocks-list
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
;    set original-pycor 0
 ]
 recolor-columns
end

sample-dudes-own [distance-for-jump ]
column-kids-own [ binomial-coefficient sample-list ]

to startup
  ;set total-samples
end

;; This procedure colors the view, divides patches into columns of equal length ( plus a single partition column),
;; and numbers these columns, beginning from the left, 0, 1, 2, 3, etc.
to setup
  ca reset-ticks
  clear-output
  set background-color white - 1;black
  set-default-shape left-dummies "big square"
  set column-color grey
  set target-color green
  set other-color blue

  set popping? false

  ;; determines the location of the sample array beginning one column to the left of the histogram
  set sample-right-xcor 12 ; -1 * round ( ( num-columns / 2 ) * ( side + 1 ) )
; JWU: the above used to be like this before i screwed with it to add the stuff on the left
; set sample-right-xcor -1 * round ( ( num-columns / 2 ) * ( side + 1 ) )

  ;; assigns each patch with a column number. Each column is as wide as the value set in the 'side' slider
  ask patches
  [
    set pcolor background-color
    ;; The following both centers the columns and assigns a column number to each patch
    ;; We use "side + 1" and not just "side" so as to create an empty column between samples
    set column floor ( ( pxcor - ( ( 5 * ( 3 ) ) / 1 ) ) / ( 3 ) )
; JWU:   the above used to be like this before i screwed with it to add the stuff on the left
;    set column floor ( ( pxcor + ( ( num-columns * ( side + 1 ) ) / 2 ) ) / ( side + 1 ) )
    if ((column < 0 and column > -3 )or column >= 5 or column < -3 - 15)
; JWU:   the above used to be like this before i screwed with it to add the stuff on the left
;    if column < 0 or column >= num-columns
      [ set column -100 ]

     if column < 0 and column != -100 [
       set column column + 18

     ]
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


        ;; JWU: do some funky switching of columns so that the 2-zs are next the the 2-zs, etc.
     if column < 0 and column != -100 [
        set column item position column [
          0    1 2 3 4    5 6 7 8  9 10    11 12 13 14      15
        ] [
          0    1 2 4 8    3 5 9 6 10 12     7 11 13 14      15
        ]

     ]
   ]
  ]

  ;; This draws the  base-line and creates a sample-kids turtle at the base of each column
  ask patches with
  [ ( pycor = -1 * max-pycor + 5 ) and  ;; The base line is several patches above the column labels.
    ( column != -100 ) ]
  [


         ;; JWU: make sure only the grouped guys do this
          if pxcor > 9
          [

    set pcolor black
    if [column] of patch-at -1 0 != column   ;; find the leftmost patch in the column...
    [
      ask patch (pxcor + 2 - 1)  ;; ...then move over to the right of the column
                ( -1 * max-pycor + 1 )
        [ set plabel column ]
      ask patch (pxcor + floor 1)  ;; ...then move over to the middle of the column
                ( -1 * max-pycor + 1 )
      [
       sprout 1
        [
          hide-turtle
          set color pcolor
          set breed column-kids
          set sample-list []
          ;; each column-kid knows how many different combinations his column has
          set binomial-coefficient item column binomrow 4
        ]
      ]
    ]
           ]


    ;; JWU: get the ones on the left fixed up.
    if pxcor < 9 [
          set pcolor black
          if [column] of patch-at -1 0 != column   ;; find the leftmost patch in the column...
          [
            ask patch (pxcor + floor 1 )  ;; ...then move over to the middle of the column
                      ( -1 * max-pycor + 1 )
            [
             sprout 1
              [
                hide-turtle
                set color pcolor
                set breed left-column-kids
              ]
            ]
          ]

    ]
  ]

  set stop-all? false
  set num-target-color false

  ;; again, instead of just accumulating and calling histogram, we have to build our own
  ;; because of the averaging pen
  set individual-4-blocks-list n-values 16 [0]
  set categorized-4-blocks-list []

end

to bump-down
  ;; last row of grouped guys
  ask sample-organizers with-min [ pycor ] [ die ]
  ;; last row of individual guys means doing the following twice
  ask left-dummies with-min [ pycor ] [ die ]
  ask left-dummies with-min [ pycor ] [ die ]

  ;; then move everyone down
  ask sample-organizers [ fd 3 ]
  ask left-dummies [ fd 3 ]
  recolor-columns
end

to go
  ;; The model keeps track of which different combinations have been discovered. Each
  ;; column-kid reports whether or not its column has all the possible combinations. When bound? is true,
  ;; a report from ALL column-kids that their columns are full will stop the run.
  sample
  drop-in-bin

  tick
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
    ( pxcor > sample-right-xcor - 2 ) and
    ( pycor > ( max-pycor - 2 ) ) ]
  ;;; jwu - this used to be an ask. that was wrong because now agentsets are randomized.
  foreach sort sample-location-patch-agentset
  [
    ask ?
    [
      sprout 1
      [
        ; output-show "here comes turtle: " + who
        ; output-show "pxcor: " + pxcor
        ; output-show "pycor: " + pycor
        ht
        set breed sample-dudes
        setxy pxcor pycor

      ;; Each turtle in the sample area chooses randomly between the target-color and the other color.
      ;; The higher you have set the probability slider, the higher the chance the turtle will get the target color
      ifelse random 100 < probability-to-be-target-color
        [ set color target-color ]
        [ set color other-color ]
      ; output-show color = target-color
      st
      ]

      ;; JWU: left-sample-dudes stuff
      sprout 1
      [
        ht
        set breed left-sample-dudes
        setxy pxcor pycor
        set color [color] of one-of sample-dudes-here

      ; output-show color = target-color
      st
      ]


    ]
  ]

  ;; to find the correct column to go to
  set left-sample-summary-value calculate-left-sample-summary-value

  ; output-show sorted-sample-dudes
  ; output-show map [ color-of ? = target-color ] sorted-sample-dudes
  ;; num-target-color reports how many sample-dudes in the random sample are of the target color

  set num-target-color count sample-dudes with [ color = target-color ]
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
  ask left-sample-dudes [ set heading -90 ]
  while [
    count sample-dudes with [ column = num-target-color ] != 4  or
    count left-sample-dudes with [ column = left-sample-summary-value ] != 4
  ]
  [
    if count sample-dudes with [ column = num-target-color ] != 4 [
      ask sample-dudes [
        fd 1
      ]
    ]
    if count left-sample-dudes with [ column = left-sample-summary-value ] != 4 [
      ask left-sample-dudes [
        fd 1
      ]
    ]
  ]

end


;; Moves the sample downwards along the column until it is either on the base line or
;; exactly over another sample in that column.
to descend
  let right-lowest-in-sample min [ pycor ] of sample-dudes
  let left-lowest-in-sample min [ pycor ] of left-sample-dudes
  ask sample-dudes
  [ set heading 180 ]
  ask left-sample-dudes
  [ set heading 180 ]

  ;; The lowest row in the square sample is in charge of checking whether or not the sample has arrived all the way down
  ;; In order to determine who this row is -- as the samples keeps moving down -- we find a turtle with the lowest y coordinate
  ;; checks whether the row directly below the sample's lowest row is available to keep moving down
  set token-right-sample-dude one-of sample-dudes with [ pycor = right-lowest-in-sample ]
  set token-left-sample-dude one-of left-sample-dudes with [ pycor = left-lowest-in-sample ]
  while
  [
   (( [ [ pcolor ] of patch-at 0 -2 ] of token-right-sample-dude ) != black  and
    ( [ [ pcolor ] of patch-at 0 -2 ] of token-right-sample-dude ) != target-color and
    ( [ [ pcolor ] of patch-at 0 -2 ] of token-right-sample-dude ) != other-color)
              or
   (( [ [ pcolor ] of patch-at 0 -2 ] of token-left-sample-dude ) != black  and
    ( [ [ pcolor ] of patch-at 0 -2 ] of token-left-sample-dude ) != target-color and
    ( [ [ pcolor ] of patch-at 0 -2 ] of token-left-sample-dude ) != other-color and
    ( [ [ any? left-dummies-here ] of patch-at 0 -2 ] of token-left-sample-dude ) != true)
  ]
  [
    if (( [ [ pcolor ] of patch-at 0 -2 ] of token-right-sample-dude ) != black  and
        ( [ [ pcolor ] of patch-at 0 -2 ] of token-right-sample-dude ) != target-color and
        ( [ [ pcolor ] of patch-at 0 -2 ] of token-right-sample-dude ) != other-color)
    [
      ;; As in find-your-column, shift the sample one row down
      ask sample-dudes
      [ fd 1 ]
      ;; Instead of establishing again the lowest row in the sample, the y coordinate of the row
      ;; gets smaller by 1 because the sample is now one row lower than when it started this 'while' procedure
      set right-lowest-in-sample ( right-lowest-in-sample - 1 )
    ]

    if (( [ [ pcolor ] of patch-at 0 -2 ] of token-left-sample-dude ) != black  and
        ( [ [ pcolor ] of patch-at 0 -2 ] of token-left-sample-dude ) != target-color and
        ( [ [ pcolor ] of patch-at 0 -2 ] of token-left-sample-dude ) != other-color and
        ( [ [ any? left-dummies-here ] of patch-at 0 -2 ] of token-left-sample-dude ) != true)
    [
      ask left-sample-dudes
      [ fd 1 ]

      ;; Instead of establishing again the lowest row in the sample, the y coordinate of the row
      ;; gets smaller by 1 because the sample is now one row lower than when it started this 'while' procedure
      set left-lowest-in-sample ( left-lowest-in-sample - 1 )
    ]
  ]
    ; keep track of results for the histogram
    ask one-of left-sample-dudes [
      set individual-4-blocks-list replace-item (position column [0  1 2 4 8  3 5 9 6 10 12  7 11 13 14  15])
                                                 individual-4-blocks-list
                                               ((item (position column [0  1 2 4 8  3 5 9 6 10 12  7 11 13 14  15]) individual-4-blocks-list) + 1)
    ]
    ask left-sample-dudes [
      hatch 1 [
        set breed left-dummies
        set size 1.25
        set heading 180
      ]
      die
    ]


  ;; Once sample-dudes have reached as low down in the column as they can go (they are on top of either the base line
  ;; or a previous sample) they might color the patch with their own color before they "die."
  finish-off

  ;; If the column has been stacked up so far that it is near the top of the screen, the whole supra-procedure stops
  ;; and so the experiment ends
  ifelse (max-pycor - right-lowest-in-sample < ( 3 ))
              or
         (max-pycor - left-lowest-in-sample < ( 3 ))
         [ set stop-all? true ] [ set stop-all? false ]
end

;; jwu - we can't sort by who number, because who numbers get reused in weird ways, it seems. (5/10/06)
to-report sorted-sample-dudes
;report sort sample-dudes
report sort-by [
         (([pxcor] of ?1 < [pxcor] of ?2) and ([pycor] of ?1 = [pycor] of ?2)) or
         (([pycor] of ?1 > [pycor] of ?2))
       ] sample-dudes
end

;; using lots of code from the grouped side stuff
to-report calculate-left-sample-summary-value
  let sorted-left-sample-dudes
  sort-by [
         (([pxcor] of ?1 < [pxcor] of ?2) and ([pycor] of ?1 = [pycor] of ?2)) or
         (([pycor] of ?1 > [pycor] of ?2))
       ] left-sample-dudes
  let left-sample-values map [ ifelse-value ([color] of ? = target-color) [1] [0] ]
                          sorted-left-sample-dudes
  let result 0
  let power-of-two 3
  foreach left-sample-values [
    if ? = 1 [
      set result result + 2 ^ power-of-two
    ]
    set power-of-two power-of-two - 1
  ]
  report result
end

to finish-off
  ;; creates local list of the colors of this specific sample, for instance the color combination of a 9-square,
  ;; beginning from its top-left corner and running to the right and then taking the next row and so on
  ;; might be "green green red green red green"
  ;; jwu - need to use map and sort instead of values-from cause of
  ;; the new randomized agentsets in 3.1pre2
  let sample-color-combination map [ [color] of ? ] sorted-sample-dudes

  ;; determines which turtle lives at the bottom of the column where the sample is
  let this-column-kid one-of column-kids with [ column = [ column ] of token-right-sample-dude ]

    ;; jwu - make the upper left sample-dude create a sample-organizer
    let the-sample-sample-dude max-one-of sample-dudes with-min [ pxcor ] [ pycor ]

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
    ask the-sample-sample-dude [
      make-a-sample-organizer
    ]
    ask sample-dudes
    [ die ]
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

;; reports the proportion of the sample space that has been generated up to now
to-report %-full
  ifelse samples-found = 0
    [ report precision 0 0 ]
    [ report precision ( samples-found / ( 2 ^ ( 4 ) ) ) 3 ]
end

to-report samples-found
  report sum [ length remove-duplicates sample-list ] of column-kids
end

to-report total-samples-to-find
  report precision ( 2 ^ ( 4 ) ) 0
end

to plot-it
  set-current-plot "Individual 4-Blocks"
  set-current-plot-pen "default"
  plot-pen-reset
  ; have to go through instead of calling histogram, because of the averaging pen
  foreach individual-4-blocks-list [
    plot ?
  ]
  set-plot-y-range 0 max individual-4-blocks-list

  set-current-plot "Categorized 4-Blocks"
  histogram categorized-4-blocks-list
  let maxbar modes categorized-4-blocks-list
  let maxrange filter [ ? = item 0 maxbar ] categorized-4-blocks-list
  set-plot-y-range 0 length maxrange
end
@#$#@#$#@
GRAPHICS-WINDOW
455
15
738
595
45
91
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
-91
91
0
0
1
ticks

SLIDER
10
15
245
48
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
170
95
245
128
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
90
95
165
128
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
135
165
170
NIL
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
175
165
210
NIL
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
170
135
245
210
Go-org
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
215
125
248
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
130
215
245
248
Un-Paint
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

SWITCH
45
55
208
88
stop-at-top?
stop-at-top?
1
1
-1000

PLOT
250
15
450
594
Individual 4-Blocks
NIL
NIL
0.0
16.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" ""

PLOT
745
15
945
594
Categorized 4-Blocks
NIL
NIL
0.0
5.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" ""

BUTTON
10
95
85
128
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

@#$#@#$#@
## WHAT IS IT?

This model is designed to help students understand the "shape" of the binomial distribution as resulting from how the elemental events are pooled into aggregate events. The model simulates a probability experiment that involves taking samples consisting of four independent binomial events that are each like a coin. Actual experimental outcomes from running this simulation are shown simultaneously in two representations.

Both representations are stacked dot plots of the samples themselves ("stalagmites"), and each of these has a corresponding histogram to its side. There are two stalagmites, because one stalagmite stacks the samples according to their particular configuration (permutation), whereas the other stalagmite stacks them by pooling together unique configurations with similar numbers of singleton events of the same value (combinations). The user is to appreciate relations between the two pairs by noting that they are transformations of each other.

## PEDAGOGICAL NOTE

If you toss four coins, what is the chance of getting exactly three heads? To figure out the answer with precision, we need to know all the possible compound events in this experiment, that is, all the unique configurations of four coin states -- whether each is heads (H) or tails (T). To make sense of the list, below, imagine that you have tagged the coins with little identifiers, such as "A", "B", "C", and "D", and you always list the state of these four coins according to the order "ABCD".

## | HHHH

## | HHHT

     HHTH
     HTHH
     THHH

## | HHTT

     HTHT
     HTTH
     THTH
     TTHH
     THHT

## | TTTH

     TTHT
     THTT
     HTTT

## | TTTT

Assuming fair coins, all the sixteen compound events, above, are equally likely (equiprobable).  But we could pool them indiscriminately into their five sets so as to form five aggregates that are heteroprobable:

## | 4H0T

     3H1T
     2H2T
     1H3T
     0H4T

The likelihood of the four coins landing as each of these five aggregate events are related as 1:4:6:4:1, reflecting the number of unique compounds events in each.  And yet, most aggregate representations, such as histograms, do not make explicit this relation between the two different ways of parsing the sample space -- as sixteen equiprobable elemental events or as five heteroprobable aggregate events.  Consequently, students are liable to miss out on opportunities to make sense of the conventional aggregate representation

This model is a part of the ProbLab curriculum.  The ProbLab Curriculum is currently under development at the CCL.  For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

When you press GO, a sample of four tiny squares, which each can be either green or blue in accord with the probability setting, is "born" at the top of the View, between the two stalagmite sets.  This compound sample is then "cloned", with one clone traveling left to the 16-columned stalagmite set, and the other clone traveling right toward the 5-columned stalagmite set.  For each set, the model computes the appropriate chute for that compound event, in accord with its unique spatial configuration of green and blue squares (for the left-hand set) or only the number of green squares in the compound (in the right-hand set).  (See the section above, "PEDAGOGICAL NOTE", for examples of two such sets, for the case of four coins.)  As the sample accumulate in their respective columns, in each of the two stalagmite sets, it gradually becomes apparent that the sets have different "destinies".  For example, for a P = .5 setting, the left-hand set is converging on a flat distribution, whereas the right-hand set takes on the characteristic binomial shape.  The two histograms that flank the View offer the conventional representations of these two simultaneous parsings of the experiment.

## HOW TO USE IT

Buttons:
SETUP prepares the view, including erasing the coloration from a previous run of the experiment.
GO continually runs experiments according to the values you have set in the sliders.
GO ONCE runs only one experiment.
GO-ORG runs experiments while grouping samples in each chute by the particular combination each 4-block exhibits.
DISORGANIZE-RESULTS groups results in each chute by combination.
ORGANIZE-RESULTS ungroups results so that the blocks show up in each chute according to the order they appeared.

Sliders:
PROBABILITY-TO-BE-TARGET-COLOR determines the chance of each square (turtle) in the sample compound event to be green.  A value of 50 means that each turtle has an equal chance of being green or blue, a value of 80 means that each turtle has a 80% chance of being green (and a 20% chance of being blue).

Switches:
STOP-AT-TOP? determines whether experiments will continue to be run once the samples in any given column reach the top of the View frame.

Plots:
INDIVIDUAL 4-BLOCKS plots in sixteen columns the number of times each one of the sixteen elemental compound events has been sampled.
CATEGORIZED 4-BLOCKS plots in five columns the number of times each of the five aggregated compound events has been sampled (0green-4blue, 1green-3blue, 2green-2blue, 3green-1blue, 4green-0blue).

Set the sliders according to the values you want, press SETUP, and then press GO.

## THINGS TO NOTICE

The histograms that flank the View each correspond with the stalagmite set closest to them. Both the histogram and the stalagmite set on the left each has 16 columns, whereas the histogram and the stalagmite set on the right each has 5 columns.

## THINGS TO TRY

Slow the model down considerably and now press Setup and then Go. Look up at the top of the View between the two stalagmite sets. Locate the four-squared sample that sprouts there and follow as it is cloned into samples that travel left and right and then fall down their appropriate chute.

Using the default switch and slider settings, press Setup and then Go. Look closely at the stalagmite on the right-hand side of the View. Note what happens when one of the columns reaches the top. Because there is no more room for this column to grow, all the other columns fall down a row. Eventually, only one or two columns (according to the probability settings and the number of samples you have taken), will remain visible.

Set the probability slider at .5 and press Go. What are you noticing about the shape of each of the two histograms?

Set KEEP-REPEATS? to Off, STOP-AT-ALL-FOUND? to On, and STOP-AT-TOP? to Off. Try to guess what you will get in each of the stalagmite sets, and then press Go.

## EXTENDING THE MODEL

## NETLOGO FEATURES

## RELATED MODELS

Some of the other ProbLab (curricular) models, including SAMPLER -- HubNet Participatory Simulation -- feature related visuals and activities.  In Stochastic Patchwork and especially in 9-Blocks you will see the same 3-by-3 array of green-or-blue squares.  In the 9-Block Stalagmite model, when 'keep-duplicates?' is set to 'Off' and the y-axis value of the view is set to 260, we get the whole sample space without any duplicates.  In the Stochastic Patchwork model and especially in 9-Blocks model, we see frequency distribution histograms.  These histograms compare in interesting ways with the shape of the combinations tower in this model.

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

big square
true
0
Rectangle -7500403 true true 0 0 300 300

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
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

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

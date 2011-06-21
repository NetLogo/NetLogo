globals [
  ;; the patch color that will signify a favored event.
  target-color

  ;; the patch color that will signify an unfavored event
  other-color

  ;; list of colors of all patches in the graphic window.
  pcolor-list

  ;; list of positions, in the graphic window, of all patches with the target-color
  ;; (the positions begin at the top-left corner, run across to the left, and then
  ;; hop to the beginning of the second-from-top row, and so on)
  target-color-list

  ;; list of the differences between each two consecutive patches with the target color
  ;; for instance, if the first three items in the target-color-list are 4, 9, and 11,
  ;; then the first two items in the target-color-differences-list will be 5 (9 - 4) and 2 (11 - 9)
  target-color-differences-list

  ;; cumulative list of consecutive blocks of successes of 1 or more
  target-color-successive-list

  just-started? ;; Boolean variable that indicates in Go if this is the first time through

  shape-names              ;; list of shapes of prizes
  colors                   ;; list of colors of prizes
  max-possible-codes       ;; number of shape/color combinations
  ]

;; lines are either straight "lines" with a circle in the center, representing a failed event
;; or "outlines" if they are a target event
;; prizes are the icons that appear inside the outlines
breed [ lines line ]
breed [ prizes prize ]


patches-own [
  ;; num is the a running number beginning from 0 at the top-left corner,
  ;; then running across the top row, then to the next row, etc.
  num

  next-patch ;; patch that is the "right" neighbor
  previous-patch ;;patch that is the "left" neighbor

  ;; steps-to-me shows, for a white patch, how many steps there are from the previous white patch up to it
  steps-to-me
]


to initialize
  clear-all
  set target-color yellow
  set other-color black
  set target-color-differences-list []
  set target-color-successive-list []
  set just-started? true
  setup-colors-and-shapes
  set-plot-range
end

to setup
  initialize
  ask patches  ;;set up
  [
    set steps-to-me 0
    sprout 1 [ set breed lines set shape "lines" set label-color black ]
    sprout 1 [ set breed prizes set size 0.5 set shape "blank"]
  ]
  assign-neighbors
  distribute-prizes
  count-steps-to-colored-patches
  collect-data
  re-label
  reset-ticks
  do-plot
end

to go
  ask patches [ set steps-to-me 0 ]

  if just-started?
  [
    clear-plot
    set-plot-range
    set just-started? false
    set target-color-differences-list []
    set target-color-successive-list []
  ]

  shuffle-prizes
  tick
  count-steps-to-colored-patches
  re-label
  ask prizes [
  ifelse prize-shapes?
    [ show-turtle ]
    [ hide-turtle ]
  ]
  collect-data
  do-plot
end

to setup-colors-and-shapes
  set shape-names ["box" "star" "target" "cat" "dog"
                   "butterfly" "leaf" "car" "airplane"
                   "monster" "key" "cow skull" "ghost"
                   "cactus" "moon" "heart"]
  set colors      [ gray   brown   green  sky  blue violet orange ]
  set colors lput ( gray - 2 ) colors
  set colors lput ( green - 2 ) colors
  set colors lput ( blue + 2 ) colors
  set colors lput ( red - 2 ) colors
  set colors lput ( turquoise - 1 ) colors
  set colors lput ( lime - 1 ) colors
  set colors lput ( cyan - 2 ) colors
  set colors lput ( magenta - 1 ) colors
end

;; assign patches "next" and "previous" patches
;; patches essentially form a circular doubly linked list
to assign-neighbors
  let i 0
  ask patches
    [ set num i
      set i i + 1

      ;;if we are at the edge, wrap around to determine the NEXT neighbor
      ifelse pxcor = max-pxcor
      [
        ifelse pycor = min-pycor
          ;;if we are in the lower right corner, next neighbor is upper left
          [ set next-patch patch min-pxcor max-pycor ]
          ;;if we are all the way to the right, the next neighbor is one row below, on right
          [ set next-patch patch min-pxcor ( pycor - 1 ) ]
      ]
      [ set next-patch patch-at 1 0 ]

      ;;if we are at the edge, wrap around to determine the PREVIOUS neighbor
      ifelse pxcor = min-pxcor
      [
        ifelse pycor = ( max-pycor )
          ;;if we are in the upper left corner, previous neighbor is lower right
          [ set previous-patch patch ( max-pxcor ) min-pycor ]
          ;;if we are in the upper left corner, previous neighbor is lower right
          [ set previous-patch patch ( max-pxcor ) ( pycor + 1 ) ]
      ]
        [ set previous-patch patch-at -1 0 ]
    ]
end

;; creates a uniform distribution of prizes patches according to the setting of 'average-distance'
to distribute-prizes
  ask patches with [ num mod average-distance = 0 ] [
      ask one-of lines-here [ set shape "outline" ]
      ask one-of prizes-here [
         set shape one-of shape-names set color one-of colors
         if (not prize-shapes?)
            [hide-turtle]
     ]
      set pcolor target-color
    ]
end


;; each target-colored patch swaps its prize's shape and color with a randomly chosen
;; other-colored patch. the target-colored cell is then replaced with a line shape and other-color.
to shuffle-prizes
  ask patches with [pcolor = target-color]
  [
    let prize1 one-of prizes-here
    ask one-of patches with [pcolor = other-color]
    [
      set pcolor target-color
      ask one-of lines-here [ set shape "outline" ]
      ask one-of prizes-here [ set color [color] of prize1
                               set shape [shape] of prize1 ]
    ]
    set pcolor other-color
    ask one-of lines-here [ set shape "lines" ]
    ask one-of prizes-here [ set shape "blank" ]
  ]
end

;; gathers histogram information
to collect-data
  set target-color-differences-list
    sentence ( [ steps-to-me ] of patches with [ steps-to-me > 0 ] ) target-color-differences-list
  set target-color-successive-list
    sentence ( [ consecutive-targets 0 ] of patches with [ pcolor = target-color and patch-at -1 0 != target-color ]
    ) target-color-successive-list
end

to count-steps-to-colored-patches
  ask patches with [ pcolor = target-color ] [ set steps-to-me step-count 0 ]
end


;; patch procedure
;; counts the number of previous patches that aren't the target-color
to-report step-count [previous-count]
   ifelse [pcolor] of previous-patch != target-color
     [ report [ 1 + step-count previous-count ] of previous-patch ]
     [ report 1 ]
end

;; patch procedure
;; counts the number of consecutive patches with prizes
to-report consecutive-targets [num-previous-targets]
   ifelse [pcolor] of next-patch = target-color
     [ report [ 1 + consecutive-targets num-previous-targets ] of next-patch ]
     [ report 1 ]
end

;; procedures for determining the mean ratio between consecutive columns in a histogram list
;; The logic is to create one list of all the columns heights and from that list create
;; a copy of it with the first item missing. Then we can 'map' one list onto the other
;; so that each item in one list divides an item in the other list that is in the corresponding position.
;; Also, we don't want columns that have zero items in them, because we cannot divide by zero.
;; Finally, we don't use more columns than is indicated by the value of the 'truncate-columns' slider.
to-report relative-heights [ listerama ]
  if listerama = []
    [ report "N/A" ]
  let binned-list but-first bin-list listerama
  let stop-bin min (list (position 0 binned-list) (position 1 binned-list) truncate-after-column)
  if stop-bin = 0  ;; if the smallest bin is 0, we cannot calculate relative-heights
    [ report "N/A" ]
  let list-of-divisors first-n stop-bin binned-list
  let list-of-dividends list-of-divisors
  set list-of-divisors but-last list-of-divisors
  set list-of-dividends but-first list-of-dividends
  if empty? list-of-divisors  ;; if there are no divisors (or similarly, dividends)
    [ report "N/A" ]            ;; it is not possible to calculate relative-heights
  report mean ( map [ ?1 / ?2 ] list-of-dividends list-of-divisors )
end

to-report first-n [index full-list]
  ifelse (index > 0 and not empty? full-list)
    [ report sentence ( first full-list ) ( first-n ( index - 1 ) ( butfirst full-list ) ) ]
    [ report [] ]
end

to-report bin-list [input-list]
  if input-list = [] [ report [] ]
  let result []
  let bin 0
  foreach n-values ( max input-list + 1 ) [?] [
    set bin ?
    set result lput ( length filter [ ? = bin ] input-list ) result
  ]
  report result
end

;; after a shuffle, the number labels are updated
to re-label
  ask lines [
    ifelse label? [
      if steps-to-me > 0 [
        set color black
        set label steps-to-me
      ]
    ]
    [ set label "" ]
  ]
end

;; code for plotting outcomes as histograms
to set-plot-range
  set-current-plot "Frequency of Distances to Prizes"
  set-plot-x-range 1 ( 5 * average-distance )
  set-current-plot "Frequency of Streaks by Length"
  ;; the following line uses a "magic number". The code used to be "15 - average-distance"
  ;; but that crashes when a user resets the average-distance slider to a higher maximum.
  set-plot-x-range 1 ( average-distance + round ( 20 / average-distance ) )
end

to do-plot
  set-current-plot "Frequency of Distances to Prizes"
  histogram target-color-differences-list
  let maxbar modes target-color-differences-list
  let maxrange length ( filter [ ? = item 0 maxbar ] target-color-differences-list )
  set-plot-y-range 0 max list 10 maxrange

  set-current-plot "Frequency of Streaks by Length"
  histogram target-color-successive-list
  set maxbar modes target-color-successive-list
  set maxrange length ( filter [ ? = item 0 maxbar ] target-color-successive-list )
  set-plot-y-range 0 max list 10 maxrange
end
@#$#@#$#@
GRAPHICS-WINDOW
302
10
752
481
5
5
40.0
1
12
1
1
1
0
1
1
1
-5
5
-5
5
1
1
1
ticks
30.0

SLIDER
111
10
294
43
average-distance
average-distance
2
10
5
1
1
NIL
HORIZONTAL

BUTTON
12
10
103
43
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
111
81
202
130
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
0

PLOT
13
133
293
253
Frequency of Distances to Prizes
Distances to Prizes
Freq
1.0
20.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 false "" ""

BUTTON
11
81
102
130
go once
Go
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
0

MONITOR
191
257
293
302
Columns Factor
relative-heights target-color-differences-list
2
1
11

SLIDER
13
257
183
290
truncate-after-column
truncate-after-column
2
30
12
1
1
NIL
HORIZONTAL

PLOT
14
309
293
429
Frequency of Streaks by Length
Length of Lucky Streak
Freq
1.0
10.0
0.0
100.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 false "" ""

SWITCH
11
45
173
78
prize-shapes?
prize-shapes?
0
1
-1000

SWITCH
181
45
293
78
label?
label?
0
1
-1000

MONITOR
189
431
293
476
Columns Factor
relative-heights target-color-successive-list
2
1
11

TEXTBOX
18
445
176
491
Columns factor is the mean ratio of consecutive columns
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

Shuffle Board investigates distributions of favored events in a series of outcomes.  The basic analogy is that there are a lot of candy boxes, but only some have prizes in them. You are buying one candy box after another, waiting for prizes, and keeping track of two things: how many boxes do you need to buy until you get a prize? When you had lucky streaks, how long were they?

A possibly counter-intuitive result in this simulation is that the shorter the "waiting time," the more frequently it occurs.  Another result is that the shorter the "lucky streak," the more frequently it occurs.

Shuffle Board explores the relation between the length of the "waiting time" and its frequency, and between the length of a lucky streak and its frequency.  This exploration is designed to help learners understand the mathematical functions associated with these distributions.

This model is a part of the ProbLab curriculum. The ProbLab curriculum is currently under development at the CCL. For more information about the ProbLab curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

The basic analogy underlying Shuffle Board is that there are a lot of candy boxes, but only some of them have prizes in them (like a blue teddy bear). You know how long it should take you, on average, to get a prize, because this information is announced by the candy manufacturer. You are buying one candy box after another and keeping track of two things:  
- How many boxes did you need to buy since last prize until you got the next prize?  
- When you had lucky streaks, how long were they?

Both are plotted automatically in the "Frequency of Distances to Prizes" and "Frequency of Streaks by Length" graphs.

The view features yellow lines that are broken by blue dots (candy boxes without prizes) and colored squares (candy boxes with prizes). Each blue dot or colored square is an "outcome". So think of a very long string with 121 outcomes that has been chopped up into 11 shorter strings, each with 11 outcomes, so as to fit into the View. The string runs from the top left spot on the board (the first outcome) towards the right. When it gets to the end of the top row, it skips to the left-most spot on the second row from top, and so on. When it gets to the bottom-right corner, it counts on to the top left corner, as though the string is closed in a necklace.

When you first open the model and whenever you press Setup, prizes are distributed uniformly across the View according to the settings of the slider 'average-distance'. For instance, if this slider is set to '5', then literally every 5th outcome will be a prize. Next, the prizes shuffle randomly to new locations on the board, so this initial uniform distribution is upset.

Why do we shuffle?  We are exploring for hidden distribution patterns. Sometimes it takes a lot of data to find hidden patterns.  If we had space in the view for thousands of boxes, we would have enough data to find any patterns that might be there. But there is not enough room in the view to make the boxes big enough so you can see the prizes.  So, instead, we shuffle.  It is as though each time we get another bit out of a larger population -- it is as though each shuffle gives us a new sample.  After each shuffle, you can track individual prizes as they move from one location to another.

Note that the string of outcomes is forms a loop. So if at the tail end of the string -- towards the bottom-right corner -- there are 4 boxes without prizes, these will count towards the first prize in the top row.

## HOW TO USE IT

Sliders:  
AVERAGE-DISTANCE -- If set at, say, 5, then every 5th square will bear a prize, when you press Setup.  
TRUNCATE-AFTER-COLUMN -- determines how many of the "Distances to Prizes" histogram columns, beginning from left, will be included in the calculation of the mean ratio between consecutive columns. For instance, if this slider is set at "4", then the program will calculate the quotients of Column 2 divided by Column 1, Column 3 divided by Column 2, and Column 4 divided by Column 3. Next, the program will determine the mean of these three quotients and report them in the "Columns-Factor" monitor.

Switches:  
PRIZE-SHAPES? -- if set to 'On', you will see what prize is waiting for you in each candy box. When set to 'Off', you will know that there is a prize in the box, but you will not know what it is.  
LABELS? -- if set to 'On', each prize box will show how many boxes had to be bought since the previous box so as to get this prize.

Buttons:  
SETUP -- initializes variables, creates a collection of prizes in accordance with the value of 'average-distance', and represents the distances to prizes and the lucky streaks in their respective plots.  
GO ONCE -- Runs the program through a single 'Go' procedure, in which prizes are shuffled and their distances and streaks are calculated and plotted.  
GO -- Runs the program over and over.

Monitors:  
Columns Factor -- the height of each column in the histogram is divided by the height of the column immediately to its left. The monitor shows the mean of all these quotients. The total number of columns included in this calculation is determined by the slider 'truncate-after-column'.

Plots:  
FREQUENCY OF DISTANCES TO PRIZES -- shows the accumulating distribution of distances between prizes in repeated samples.  
FREQUENCY OF STREAKS BY LENGTH -- shows the accumulating distribution of streaks of consecutive successes  
Note that after you press Setup, when you first click on 'Go' or 'Go Once', the plots initialize, but later they accumulate information from previous runs.

Set the 'average-distance' slider and press 'Setup'. Now press 'Go Once' and watch the histograms appear in the plots. If you press 'Go', the program will run indefinitely, until you press 'Go' again.


## THINGS TO NOTICE

When you press 'Go Once' the prizes rearrange on the board. Choose your favorite prize and track it. This way, you'll be sure that no prizes vanish!

After a single shuffle, what is the most common distance between every two consecutive prizes? You can see this in the View -- the number labels show these distances, and so you can count up how many "1"s you see, and how many "2"s, "3"s, etc. Also, look at the plot to see which bar is the highest. Keep looking at the plot over more runs. See the typical graph shape that forms.

As the program runs over and over, the value in the Column Factor monitor gradually converges.

## THINGS TO TRY

When you press Setup, you get a uniform distribution of prizes. After you shuffle the prizes, the distribution is not uniform. But what is the average distance between prizes now? In the Command Center, type

    show precision mean target-color-differences-list 2

Now press Enter. This code calculates for you the mean distance between each two consecutive white patches. Can you explain the value you received?

Watch the value in the 'Column Factor' monitor. Can you find a relation between these relative heights and the 'average-distance' setting?

Play with the 'truncate-columns' slider. What does including more columns do as compared to including fewer columns? What, if any, is the relation of these actions to the value in the 'Columns Factor' monitor?

What is the relation between the value of average-distance and the curves you get in the plot 'Frequency of Streaks by Length'? Does this make sense to you?

Here is an experiment you can try without NetLogo that might shed more light on the model:  
Set up a deck of cards that has 13 face cards and 39 number cards. That is a 1:3 ratio of faces to numbers. One fourth of the cards are faces. Thus, the probability that any random card drawn from this deck is a face is 1 out of 4 -- that is, a 25% chance. Now distribute the face cards uniformly in the deck so that literally every fourth card is a face. Next, shuffle the cards thoroughly. Still, every fourth card on average will be a face. But what is hiding behind this sense of "average" in "every fourth card on average is a face"? Count up to each face card and create a histogram of the frequencies of each number of attempts until success. What have you learned? Can you explain this? Note that two decks put together with the same "number:face" ratio (for a total of 26:78) will bring quicker results.

Shuffle Board is a NetLogo analogy to shuffling a deck of cards and then going through the cards one by one and counting how long it takes to find each face.  To be precise, the shuffling procedure in this model is less rule governed than standard shuffling of cards (see for example http://www.sciencenews.org/articles/20001014/mathtrek.asp).

## EXTENDING THE MODEL

Add a plot to show how the Columns Factor value changes over time.

Add pens to the plots to represent the averages of the outcomes.

Add a switch that flips the orientation of the shuffle board so that distances to prizes are counted vertically instead of horizontally.

Choose a specific location on the View (one of the "patches") and monitor how often that location gets a prize over repeated shuffles. Design an experiment to determine any possible relationships between the frequency of getting a prize in that location and the setting of the 'average-distance' slider.

## RELATED MODELS

The model is closely related to Prob Graphs Basic, and especially to the middle graph in that model. That middle graph looks at distances or 'attempt-until-success' ("waiting time") between consecutive "hits" that are determined randomly according to a sample-space setting. Shuffle Board shows the same idea, but it does so using a more visible analogy that allows you to scan for yourself the distances between all of the favored events. This helps us understand that the shorter distances are more ubiquitous as compared to the longer distances. In particular, we see that the distance "1" is relatively dominant.

## CREDITS AND REFERENCES

This model is a part of the ProbLab curriculum. The ProbLab Curriculum is currently under development at Northwestern's Center for Connected Learning and Computer-Based Modeling. . For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

Thanks to Ethan Bakshy for his extensive work on this model.
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

android
false
0
Rectangle -7500403 true true 105 74 210 239
Polygon -7500403 true true 104 78 34 129 47 148 114 89
Polygon -7500403 true true 198 81 274 108 258 142 192 104
Polygon -7500403 true true 115 239 115 289 133 289 133 237
Polygon -7500403 true true 176 235 176 287 192 287 192 234
Rectangle -7500403 true true 119 12 194 73
Rectangle -16777216 true false 129 22 147 36
Rectangle -16777216 true false 164 23 184 37
Rectangle -16777216 true false 151 113 163 125
Rectangle -16777216 true false 153 142 164 154
Rectangle -16777216 true false 154 171 166 184

ant
false
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

bear
false
0
Rectangle -7500403 true true 119 35 194 92
Circle -7500403 true true 172 17 18
Circle -7500403 true true 153 17 19
Rectangle -7500403 true true 189 58 220 89
Rectangle -7500403 true true 199 95 202 95
Rectangle -7500403 true true 108 93 208 104
Rectangle -7500403 true true 101 101 217 114
Rectangle -7500403 true true 95 107 222 130
Rectangle -7500403 true true 91 122 226 240
Rectangle -7500403 true true 96 236 223 249
Rectangle -7500403 true true 120 89 194 104
Rectangle -7500403 true true 102 247 220 258
Rectangle -7500403 true true 192 255 215 281
Rectangle -7500403 true true 105 252 131 283
Rectangle -7500403 true true 129 276 142 283
Rectangle -7500403 true true 210 276 226 281
Circle -7500403 true true 77 201 35
Circle -16777216 true false 176 40 13
Rectangle -7500403 true true 213 163 249 184
Rectangle -7500403 true true 247 166 255 169
Rectangle -7500403 true true 248 172 258 173
Rectangle -7500403 true true 247 178 262 183
Circle -7500403 true true 208 62 20

big boat
false
0
Polygon -6459832 true false 1 196 43 296 193 296 297 194
Rectangle -1 true false 135 14 149 194
Polygon -7500403 true true 151 14 173 18 193 30 211 48 239 88 251 118 271 170 271 184 253 176 227 170 199 172 177 180 161 190 165 160 169 122 165 78
Polygon -7500403 true true 133 36 115 50 77 86 47 122 7 152 33 156 57 164 77 178 91 188
Rectangle -7500403 true true 30 206 234 220
Rectangle -7500403 true true 52 224 234 236
Rectangle -7500403 true true 78 240 234 250

blank
false
0

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

cow skull
false
0
Polygon -7500403 true true 150 90 75 105 60 150 75 210 105 285 195 285 225 210 240 150 225 105
Polygon -16777216 true false 150 150 90 195 90 150
Polygon -16777216 true false 150 150 210 195 210 150
Polygon -16777216 true false 105 285 135 270 150 285 165 270 195 285
Polygon -7500403 true true 240 150 263 143 278 126 287 102 287 79 280 53 273 38 261 25 246 15 227 8 241 26 253 46 258 68 257 96 246 116 229 126
Polygon -7500403 true true 60 150 37 143 22 126 13 102 13 79 20 53 27 38 39 25 54 15 73 8 59 26 47 46 42 68 43 96 54 116 71 126

cylinder
false
0
Circle -7500403 true true 0 0 300

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

ghost
false
0
Polygon -7500403 true true 30 165 13 164 -2 149 0 135 -2 119 0 105 15 75 30 75 58 104 43 119 43 134 58 134 73 134 88 104 73 44 78 14 103 -1 193 -1 223 29 208 89 208 119 238 134 253 119 240 105 238 89 240 75 255 60 270 60 283 74 300 90 298 104 298 119 300 135 285 135 285 150 268 164 238 179 208 164 208 194 238 209 253 224 268 239 268 269 238 299 178 299 148 284 103 269 58 284 43 299 58 269 103 254 148 254 193 254 163 239 118 209 88 179 73 179 58 164
Line -16777216 false 189 253 215 253
Circle -16777216 true false 102 30 30
Polygon -16777216 true false 165 105 135 105 120 120 105 105 135 75 165 75 195 105 180 120
Circle -16777216 true false 160 30 30

heart
false
0
Circle -7500403 true true 152 19 134
Polygon -7500403 true true 150 105 240 105 270 135 150 270
Polygon -7500403 true true 150 105 60 105 30 135 150 270
Line -7500403 true 150 270 150 135
Rectangle -7500403 true true 135 90 180 135
Circle -7500403 true true 14 19 134

house
false
0
Rectangle -7500403 true true 45 120 255 285
Rectangle -16777216 true false 120 210 180 285
Polygon -7500403 true true 15 120 150 15 285 120
Line -16777216 false 30 120 270 120

key
false
0
Rectangle -7500403 true true 90 120 285 150
Rectangle -7500403 true true 255 135 285 195
Rectangle -7500403 true true 180 135 210 195
Circle -7500403 true true 0 60 150
Circle -16777216 true false 30 90 90

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

lines
false
2
Rectangle -7500403 true false -30 135 300 150
Rectangle -16777216 true false 0 270 300 270
Rectangle -16777216 true false 0 0 315 45
Rectangle -16777216 true false 0 270 300 315
Rectangle -13791810 true false 75 105 225 180

lobster
false
0
Polygon -7500403 true true 105 106 122 238 100 259 100 276 118 275 150 240 180 275 197 275 197 258 174 238 196 105 178 77 123 77 105 106
Polygon -7500403 true true 112 103 84 78 105 60 94 14 85 57 78 19 67 59 76 83 110 118
Polygon -7500403 true true 192 120 228 85 241 57 231 22 223 59 217 17 205 61 216 79 192 108
Rectangle -7500403 true true 71 125 117 133
Rectangle -7500403 true true 73 149 119 157
Rectangle -7500403 true true 78 178 123 187
Rectangle -7500403 true true 183 131 219 138
Rectangle -7500403 true true 180 152 216 160
Rectangle -7500403 true true 173 176 217 184
Rectangle -7500403 true true 127 56 136 82
Rectangle -7500403 true true 160 55 169 81

monster
false
0
Rectangle -7500403 true true 77 100 213 245
Rectangle -7500403 true true 189 243 213 273
Rectangle -7500403 true true 78 242 102 271
Rectangle -7500403 true true 59 119 83 239
Rectangle -7500403 true true 203 118 225 239
Rectangle -7500403 true true 225 123 264 158
Rectangle -7500403 true true 262 124 272 131
Rectangle -7500403 true true 263 133 273 140
Rectangle -7500403 true true 263 145 271 156
Rectangle -7500403 true true 19 122 61 156
Rectangle -7500403 true true 9 125 19 130
Rectangle -7500403 true true 11 135 20 140
Rectangle -7500403 true true 9 146 20 151
Rectangle -7500403 true true 89 79 198 101
Rectangle -7500403 true true 112 35 176 82
Rectangle -16777216 true false 122 41 136 53
Rectangle -16777216 true false 149 39 167 53
Circle -1 true false 82 118 117
Polygon -16777216 true false 122 62 132 77 157 77 168 62
Polygon -1 true false 140 63 135 72 127 62
Polygon -1 true false 145 61 151 70 159 62
Polygon -7500403 true true 123 17 115 34 131 34
Polygon -7500403 true true 165 15 149 33 172 34

moon
false
0
Polygon -7500403 true true 175 7 83 36 25 108 27 186 79 250 134 271 205 274 281 239 207 233 152 216 113 185 104 132 110 77 132 51

moose
false
0
Polygon -7500403 true true 74 121 210 121 240 136 239 181 194 195 90 195 45 181 45 135
Rectangle -7500403 true true 225 180 239 268
Rectangle -7500403 true true 196 187 211 275
Rectangle -7500403 true true 75 186 90 269
Rectangle -7500403 true true 46 178 59 275
Polygon -7500403 true true 238 138 240 107 277 107 283 102 282 85 276 82 246 79 234 63 213 64 206 128
Circle -16777216 true false 225 72 12
Polygon -7500403 true true 74 120 22 138 16 185 35 188 40 142
Polygon -7500403 true true 235 65 230 50
Polygon -7500403 true true 232 64 240 49 283 49 293 41 294 12 278 40 262 40 265 14 251 39 234 39 236 16 222 66 232 64
Polygon -7500403 true true 224 64 203 17 205 38 189 38 178 16 179 38 164 38 148 11 149 38 156 47 203 47 214 66

outline
false
0
Rectangle -7500403 true true 0 0 300 15
Rectangle -7500403 true true -15 -15 30 300
Rectangle -7500403 true true -15 -30 315 30
Rectangle -7500403 true true 0 -75 15 0
Rectangle -7500403 true true 0 285 300 315

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

pickup truck
false
0
Polygon -7500403 true true 285 208 285 178 279 164 261 144 229 132 217 132 213 106 199 84 171 68 149 68 129 68 129 148 1 148 1 156 19 164 19 222 285 222 283 174 283 176
Circle -16777216 true false 40 185 71
Circle -16777216 true false 192 191 66
Circle -7500403 true true 195 194 59
Circle -7500403 true true 43 188 64
Polygon -16777216 true false 197 94 149 94 157 128 209 128 205 112 203 102 197 94
Polygon -7500403 true true 21 142 139 142 139 136 13 136

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
false
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

target
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60

teddy bear
false
0
Circle -7500403 true true 110 21 81
Circle -7500403 true true 97 7 39
Circle -7500403 true true 171 5 39
Polygon -7500403 true true 133 88 95 117 95 225 119 247 188 248 215 224 215 116 170 91
Circle -7500403 true true 134 238 34
Polygon -7500403 true true 197 113 249 88 261 91 268 106 262 116 205 139 197 113
Polygon -7500403 true true 115 116 66 90 54 93 45 110 50 117 103 145 115 116
Polygon -7500403 true true 104 204 54 233 54 244 63 257 71 256 117 227
Polygon -7500403 true true 194 228 240 255 248 254 260 238 257 231 204 207 194 228
Circle -1 true false 124 41 20
Circle -1 true false 158 42 20
Line -16777216 false 127 75 150 85
Line -16777216 false 151 85 177 72
Polygon -1 true false 152 204 115 167 131 150 150 168 168 152 184 167

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

wolf
false
0
Rectangle -7500403 true true 195 105 285 165
Rectangle -7500403 true true 195 90 255 105
Polygon -7500403 true true 240 90 217 44 196 90
Polygon -16777216 true false 233 90 218 59 203 89
Rectangle -1 true false 241 93 252 105
Rectangle -16777216 true false 245 96 249 101
Rectangle -16777216 true false 285 121 300 135
Rectangle -16777216 true false 240 136 285 151
Polygon -1 true false 285 136 277 149 269 136
Polygon -1 true false 270 151 263 136 257 151
Rectangle -7500403 true true 37 120 195 195
Rectangle -7500403 true true 41 195 192 201
Rectangle -7500403 true true 48 201 186 210
Rectangle -7500403 true true 57 210 180 214
Rectangle -7500403 true true 45 114 185 120
Rectangle -7500403 true true 52 108 172 114
Rectangle -7500403 true true 75 105 150 108
Rectangle -7500403 true true 145 214 168 270
Rectangle -7500403 true true 168 260 190 270
Rectangle -7500403 true true 68 214 90 270
Rectangle -7500403 true true 90 260 111 270
Line -7500403 true 37 127 19 155
Line -7500403 true 19 155 19 192

x
false
0
Polygon -7500403 true true 270 75 225 30 30 225 75 270
Polygon -7500403 true true 30 75 75 30 270 225 225 270

@#$#@#$#@
NetLogo 5.0beta4
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

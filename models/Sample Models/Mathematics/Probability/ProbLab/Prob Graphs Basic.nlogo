globals [
  event
  total-attempts
  total-successes
  counter
  counter-list
  successes-per-sample-list
  attempts-this-sample
  successes-this-sample
  samples-counter ]

to setup  ;; resets everything to appropriate initial values
  clear-all
  set event "-"
  set total-attempts 0
  set total-successes 0
  set counter 0
  set counter-list []
  set attempts-this-sample 0
  set successes-this-sample 0
  set samples-counter 0
  set successes-per-sample-list []
  reset-ticks
end

to go
  if samples-counter = how-many-samples? [stop]
  set total-attempts total-attempts + 1
  set counter counter + 1
  select-and-check
  tick
  update-and-plot
end

to select-and-check
  ;; This procedure simulates a chance event by randomly selecting a number between 1 and
  ;; sample-space-size, for instance between 1 and 5, as if you are rolling a die with 5
  ;; sides. Next, the procedure checks to see if this event (what you "rolled") happens to
  ;; be '1.' A '1' is a success. Note that 'random' reports a number between 0 and value,
  ;; so "random 1" is only 0, and "random 2" is 0 or 1. That is why we have to add 1.
  set event ( 1 + random sample-space-size )
  if  event = 1
    [
     set total-successes total-successes + 1
     set counter-list lput counter counter-list
     set counter 0
     set successes-this-sample successes-this-sample + 1
    ]
end

to update-and-plot  ;; updates values for each of the three plots
  update-and-plot-m/n
  update-and-plot-attempts
  update-and-plot-successes
end

to update-and-plot-m/n
  set-current-plot "m/n convergence to limiting value"
  plot (total-successes / total-attempts)
end

to update-and-plot-attempts
  if length counter-list = 0 [stop]
  set-current-plot "Attempts-until-Success Distribution"

  ;; setting the range just beyond the maximum value (e.g.,5 beyond but it could be more or less)
  ;; helps the eye pick up that the right-most value is indeed the maximum value
  set-plot-x-range 0 ( (max counter-list) + 5)
  histogram counter-list
  let maxbar modes counter-list
  let maxrange length filter [ ? = item 0 maxbar ] counter-list
  set-plot-y-range 0 max list 10 maxrange
end

to update-and-plot-successes
  set attempts-this-sample attempts-this-sample + 1
  if attempts-this-sample = sample-size
  [
    set successes-per-sample-list lput successes-this-sample successes-per-sample-list
    set-current-plot "Successes-per-Sample Distribution"

    ;; This line adjusts the top range of the x-axis so as to stabilize and centralize
    ;; the distribution. The idea is to try and keep the emergent graph shape in the
    ;; middle of the plot. The 'ceiling' primitive keeps the maximum range value an integer.
    set-plot-x-range 0 ( max ( list plot-x-max
                                    ( 3 + ( ceiling ( 2 * mean successes-per-sample-list ) ) ) ) )
    histogram successes-per-sample-list
    let maxbar modes successes-per-sample-list
    let maxrange length filter [ ? = item 0 maxbar ] successes-per-sample-list
    set-plot-y-range 0 max list 25 maxrange
    set attempts-this-sample 0
    set successes-this-sample 0
    set samples-counter samples-counter + 1
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
243
10
498
48
17
0
7.0
1
10
1
1
1
0
1
1
1
-17
17
0
0
1
1
1
ticks
30.0

BUTTON
102
35
181
81
Go
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

BUTTON
7
35
86
81
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

PLOT
204
35
546
218
m/n convergence to limiting value
Attempts
Successes per Attempts
0.0
100.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

PLOT
204
219
546
379
Attempts-until-Success Distribution
Run Length
Frequency
1.0
50.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" ""

SLIDER
9
179
184
212
sample-space-size
sample-space-size
1
10
2
1
1
NIL
HORIZONTAL

PLOT
204
378
546
539
Successes-per-Sample Distribution
Successes
Frequency
0.0
10.0
0.0
25.0
true
false
"" ""
PENS
"default" 1.0 1 -16777216 true "" ""

SLIDER
12
461
184
494
sample-size
sample-size
0
100
10
5
1
NIL
HORIZONTAL

SLIDER
10
375
184
408
how-many-samples?
how-many-samples?
0
1000
1000
10
1
NIL
HORIZONTAL

MONITOR
550
112
649
157
NIL
total-attempts
3
1
11

MONITOR
680
66
737
111
rate
total-successes / total-attempts
3
1
11

MONITOR
550
35
649
80
total-successes
total-successes
0
1
11

TEXTBOX
551
89
677
107
________________     =
11
0.0
0

MONITOR
549
379
696
424
NIL
successes-this-sample
3
1
11

MONITOR
12
495
184
540
NIL
attempts-this-sample
3
1
11

MONITOR
549
219
606
264
NIL
counter
3
1
11

MONITOR
11
409
184
454
NIL
samples-counter
3
1
11

MONITOR
445
540
546
585
max
max successes-per-sample-list
3
1
11

MONITOR
204
540
308
585
min
min successes-per-sample-list
3
1
11

MONITOR
325
540
429
585
mean
mean successes-per-sample-list
3
1
11

TEXTBOX
9
87
139
178
The program will record and plot the random occurrence of the event \"1\" among other integers in the sample space of size...
11
0.0
0

MONITOR
549
270
606
315
mean
mean counter-list
3
1
11

MONITOR
143
98
193
143
NIL
event
3
1
11

@#$#@#$#@
## WHAT IS IT?

Prob Graphs Basic is a basic introduction to probability and statistics.

A sample space is the collection of all possible outcomes in an experiment. An example of a sample space is the numbers "1, 2, 3, 4, 5, 6, 7."  An event is what you get when you run an experiment. For example, if I am running an experiment that randomly selects a single number out of the sample space "1, 2, 3, 4, 5, 6, 7," then an event might be "5." A sample is a collection of events that occur in an experiment. You could have a sample of size 1 that contains just 1 event, but you could have a sample of size 4 that contains 4 events, e.g., "5, 3, 3, 7."

In this model, 3 graphs monitor a single experiment as it unfolds. The experiment here is finding how often the number "1" shows up when you randomly select a number within a range that you define. This range could be, for example, between 1 and 2. An example of a sample space of only two values is a coin that can be either 'heads' or 'tails.' An example of a sample space of 6 values is a die that can land on the values 1 thru 6. Through observing this simple experiment through 3 different graphs, you will learn of 3 different ways of making sense of the phenomenon.

The top graph, "m/n convergence to limiting value," shows how the rate settles down to the expected- or mathematical probability. For instance, the limiting value of a coin falling on "heads" is .5 because it happens 1/2 of the time. So, the unit of analysis is a single trial and the rate is always informed by all previous trials. To explain this further, lets think of "batting average." The sample space in batting is a 'hit' or a 'no hit,' which is much the same as whether a coin falls on "heads" or on "tails" (only of course batting is not random like tossing a coin or otherwise Babe Ruth's average would have been the same as anyone's). So there are exactly 2 possible outcomes. The "batting average" keeps track, over time, of how many "hits" occurred out of all attempts to hit, known as "at bats." So the "batting average" is calculated as

> Hits / At-Bats  =  Batting Average

For instance, using "H" for hit and "N" for no-hit, a baseball player's at-bat events may look like this, over 20 attempts:

    N N N H H N N N N H N H N N H H H N N H

'Hits' are called 'favored events' because when we do the statistics, what we care about, count, and calculate is all about how often 'hits' occurred out of all the at-bat events. The m/n interpretation (favored events / total events) would interpret this string of events as 8 hits / 20 at bats, .4 probability (the same as .400), or a score of 400 (out of 1000).

You may be familiar with the fact that as the baseball season progresses, it is more and more difficult for an individual player to change his "average." This model may help you understand or at least simulate this phenomenon. But remember that a batter, unlike a coin or a die, is not behaving randomly. But in this model the behavior will be random. We have discussed batting only to give you context for thinking about the graph. A truer context, though, would be a coin that has 2 sides.  In fact, this model can simulate not just objects with 2 sides, but with more. You know all about dice that have 6 sides, right? If you have set the size of your sample space to 5, then the model will simulate an experiment in which a die of 5 sides is rolled over and over again.

The middle graph, "Attempts-until-Success Distribution" counts how many trials it takes for the favored event to occur. For instance, if you're tossing a coin, it takes on average 2 tosses to get "heads," and if you're rolling a die it takes on average 6 rolls to get a "5." This graph is tracking the exact same experiment as the top graph; only it is "parsing" the events differently, that is, it is using a different rule to divide up the sequence of events over time. (We will continue using "N" and "H" but you can think of the coin with 2 sides or of the die with as many sides as you want.)

    N N N H    H    N N N N H    N H    N N H    H    H    N N H

So the unit of analysis in this interpretation of the experiment's results is the number of events leading up to and including a hit. As you see, the number of events per unit changes. In this example the string of numbers is [4; 1; 5; 2; 3; 1; 1; 3]. Note that in this string the numeral "1" appears 3 times, the numeral "2" appears 1 time, the numeral "3" appears 2 times, the numeral "4" appears 1 time, and the numeral "5" appears 1 time. The histogram of this string would peak over '1' (this peak will be of height 3), then go down to '2' (frequency of 1), etc.  Perhaps this interpretation is a bit like what a batter's fans feel -- their suspense grows over failed hits until there is a hit, they are relieved and happy, and then they start counting again. So according to the context you are in -- what you're interested in finding, how you're feeling -- the world can appear different.

The bottom graph, "Successes-per-Sample distribution," takes yet another perspective on the experiment, namely a sampling perspective. The sampling perspective is used in statistics. Lets analyze the same string of events from our experiment, this time chopping it up into samples of equal size, say size 5.

    N N N H H   N N N N H   N H N N H   H H N N H

See that in the first sample there are 2 hits, in the second sample there is 1 hit, in the third sample there are 2 hits, and in the last sample there are 3 hits. This observation could be summed up as [2; 1; 2; 3]. A histogram of this result would show a frequency of 0 (y axis) over the 0 (x axis), because all samples had at least a single 'H.' Then over the '1' there will be a column of height 1, over the '2' there will be a column of height 2, and over the '3' there will be a column of height 1.

Understanding the differences and relations between these 3 graphs will give you a strong head start in studying Probability and Statistics.

This model is a part of the ProbLab curriculum. The ProbLab Curriculum is currently under development at the CCL. For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

The model first generates a random value between 1 and sample-space-size inclusive.  The number of attempts (trials) is increased by one.  If the random value is equal to 1, then the number of successes (favored events or "hits") is also increased by one.  The number of attempts and the number of successes are interpreted in three different ways with each way shown in a graph as follows: (1) single attempt (trial) and single success; (2) trials (attempts) thru to each success; or (3) successes in each sample (fixed number of trials).  Each of the graphs comes to be associated with typical shapes.

## HOW TO USE IT

Begin with the default settings. If you have changed them, then do the following: set the sample-space-size to 2 (so outcomes are either '1' or '2'), set the sample-size to '10' (so each sample will be a string of 10 events), and set the 'how-many-samples?' slider to 300 (so that the experiment will run a total of 300 samples of size 10 each, making a total of 3,000 trials). Press 'setup' to be sure all the variables are initialized, so that you will not have leftover values from a previous experiment). Press 'go.' Watch the 'event' monitor to see the number that the randomized procedure has reported. It will be either '1' or '2' because you have set the value to 2.

You may want to use the speed slider above the view to slow down the simulation. As you become more comfortable with understanding what you are seeing, you can speed up the simulation by moving the slider farther right.

Note how the event does not necessarily alternate between '1' and '2' according to any particular pattern.  Rather, only in the long run do you see what the constant is in the phenomenon you are observing. "In the long run" is precisely what this experiment shows. You can control how long this run will be by increasing or decreasing both the 'sample-size' and/or the 'how-many-samples?' slider.

### Buttons

'setup' -- initializes all variables. Press this button to begin a new experiment.  
'go' -- begins the simulation running. You can press it again to pause the model.

### Sliders

'sample-space-size' - set the size of the sample space (in integers).  
'sample-size' - set the number of trials per sample.  
'how-many-samples?'- set the number of samples you wish to run in the experiment.

### Monitors

'event' -- the number that the randomized procedure has generated this trial.  
'total-successes' -- total number of favored events over all trials.  
'total-attempts' -- total number of trials.  
'rate' -- total-successes / total-attempts.  
'counter' -- shows how many trials have passed since last success (or, if you've only just set up and run the model, then it will show how many trials have passed since the model began running).  
'attempts-this-sample' -- counts how many trials there have been since the last success (or, if you've only just set up and run the model, then it will show how many trials have passed since the model began running).  
'successes-this-sample' -- counts how many successes there have been since the last success (or, if you've only just set up and run the model, then it will show how many trials have passed since the model began running).  
'samples counter' -- counts how many samples there have been since the beginning of this experiment  
'min', 'mean', 'max' -- the minimum, mean, and maximum values of the Successes-per-Sample distribution

### Plots

m/n convergence to limiting value -- cumulative rate of successes (hits or favored events) per total trials.  
Attempts-until-Success Distribution -- histogram of number of trials it takes until each success.  
Successes-per-Sample Distribution -- histogram of number of successes within each sample.

## THINGS TO NOTICE

What are the characteristic shapes of each graph?

Look at the 'rate' monitor. What can you say about the fluctuation of numbers? What can you say about the value it settles on? What other settings in the model can you relate to this rate value?

The "Attempts-until-Success Distribution" never has values for 0, whereas the other plots sometimes do. Why is that?  
Also, what can you say about the mean of this distribution? Does this make sense to you?

## THINGS TO TRY

A sample-size of 10 that is run 300 times and a sample-size of 300 that is run 10 times both produce 3000 trials, because 10 and 300 are the factors of 3000 regardless of their order in a context. Run the experiment under both combination conditions. Did this make any difference? If so, which of the three graphs did it affect and which did it not affect? Run the experiment under other pairs of combination conditions. How different do the factors have to be to cause any difference in the graphs? How does the sample-space-size play in with all this?

By now you may have noticed the typically bell-shaped histogram of the Successes-per-Sample distribution.  Try to find settings that do not create this shape and analyze why this is the case.

## EXTENDING THE MODEL

As a beginning, try adding monitors to show values from variables you are interested in tracking. For instance, you may want to know the minimum, mean, and maximum values of the "Attempts-until-Success Distribution." Also, you may want to change parameters of the sliders.

Challenge: Add to the "Attempt-until-Success" plot a line that indicates the mean.

Challenge: Think of modification that keeps the 'random' reporter, but "helps" the program have more hits. Of course, this will change completely the nature of the simulation, so you can think of what you have created, and give the program a new name.

## NETLOGO FEATURES

This model is unusual in that it doesn't use the view at all.  Everything that happens visually happens in the plots and monitors.

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
NetLogo 5.0beta3
@#$#@#$#@
need-to-manually-make-preview-for-this-model
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
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

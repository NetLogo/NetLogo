globals [
  sample-mean-list  ;; list of means of sample taken from the population
  sample-sum-list   ;; list of sums of sample totals
  regular-color     ;; color of specimens in the population
  chosen-color      ;; color of sampled specimens
  ranger            ;; holds value of range so that the range slider can be used for guessing
]

to setup
  clear-all
  set regular-color red + 3
  set chosen-color green
  set sample-mean-list []
  set sample-sum-list []
  ask patches [ set pcolor white - 2 ]
  create-x-line-labels
  reset-ticks
end

;; the View shows a "picture bar chart." Bottom patches display the 'x value" of this chart
to create-x-line-labels
  ask patches with [pycor = min-pycor]
    [
      set plabel-color black
      ;; to avoid congestion of labels, we ask only every other patch to display a label
      if pxcor mod 2 = 1 [ set plabel ( pxcor + max-pxcor ) ]
    ]
end

to create-population ;; CREATE-RANDOM-PEOPLE
  setup
  set ranger range + 1

  ;; colors patches in the range white, others gray
  ask patches
  [
    ifelse pxcor <= ranger + min-pxcor - 1
      [ set pcolor white ]
      [ set pcolor white - 2 ]
  ]

  ;; creates for each column of patches, beginning from the left and moving to the end of the range,
  ;; a random number of specimens ("people"). These are stacked up.
  let counter min-pxcor
  let column-help 0

  ;; ranger is 1 more than range. We add 1 to the range, because the first "x value" is 0
  repeat ranger
  [
    set column-help random-pycor
    ask patches with [ (pxcor = counter) and (pycor > min-pycor)]
      [
        if pycor < column-help
          [ sprout-person ]
      ]
    set counter counter + 1
  ]
end

to sprout-person
   sprout 1 [
    set shape "face neutral"
    set color regular-color ]
end

;; procedure allowing users to select columns where new specimens are created
to draw-your-own-people  ;; CREATE-MY-OWN-PEOPLE button
  ask patches [ set pcolor white]
  create-x-line-labels
  set ranger range + 1

  ;; we use a temp-mouse-xcor to avoid confusion when the user moves the mouse rapidly
  let temp-mouse-xcor "N/A"
  ;; each column has a "top-patch." It will be the lowest patch that does not have a person turtle in it
  let top-patch "N/A"

  ;; if there still is room for a new person in the column, a new person will appear just above the highest person there
  if mouse-down? [
      if not ( round mouse-ycor = min-pxcor ) [
      set temp-mouse-xcor mouse-xcor
      ;; locates the top-most patch, in the column where you click, that has a person in it, and assigns the patch above it
      ;; If there are no persons in the column, the top-patch is the bottom patch in the column
      ifelse any? patches with [ ( any? turtles-here ) and ( pxcor = round temp-mouse-xcor) ]
        [
          set top-patch patch (round temp-mouse-xcor)
            ;; we do not want to assign top-patch a pycor of a patch outside the world
             min list ( max-pycor )
                     (1 + max [ pycor ] of patches with [ ( any? turtles-here ) and ( pxcor = round temp-mouse-xcor) ] )
        ]
        [
          set top-patch patch (round temp-mouse-xcor) (1 + min-pycor)
        ]  ;; there is a possibility that the very top patch is already occupied, so in that case we do not create a new turtle
        ask top-patch [ if not any? turtles-here [ sprout-person ] ]
      ]
    ]

  display
  wait .2
end

to go
  reset-turtles

  ;; we check to make sure that there are enough turtles to sample
  ifelse sample-size <= count turtles
  [
    ask n-of sample-size turtles
    [
      set shape "face happy"
      set color chosen-color
    ]
  ]
  [
    user-message word "There are not enough people to take a sample of this size."
                      "\n\nEither create more people or choose a smaller sample size"
    stop
  ]
  tick
  calculate-and-plot-sample-stuff
end

to reset-turtles
  ask turtles
  [
    set shape "face neutral"
    set color regular-color
  ]
end

to calculate-and-plot-sample-stuff
  ;; gets the mean and the sum of the sample, then plots these
  ;; we add max-pxcor to compensate for the negative values of xcor
  let temp-mean mean [ xcor + max-pxcor ] of turtles with [ color = chosen-color ]
  set sample-mean-list ( lput temp-mean sample-mean-list )
  let temp-sum sum [ xcor + max-pxcor ] of turtles with [ color = chosen-color ]
  set sample-sum-list ( lput temp-sum sample-sum-list )

  ifelse also-sums?
  [
    ;; adjusts the range of the plot to include all the values from the sums list
    ;; There is a possibility that the maximum sum is less than the range, so we include it, too
    set-plot-x-range 0 (1 + max sentence sample-sum-list (ranger - 1) )
    set-current-plot-pen "sums"
    histogram sample-sum-list
    ;; plots the histogram of the sums of sample means
    set-current-plot-pen "sums-mean"
    plot-pen-reset
    plot-pen-up
    plotxy (mean sample-sum-list) 0
    plot-pen-down
    plotxy (mean sample-sum-list) plot-y-max
  ]
  [
    ;; clears the sums histogram and mean, then adjusts the range of the plot
    set-current-plot-pen "sums"
    plot-pen-reset
    set-current-plot-pen "sums-mean"
    plot-pen-reset
    set-plot-x-range 0 ranger
  ]

  ;; plots the histogram of the means of sample means as well as their mean
  set-current-plot-pen "means"
  histogram sample-mean-list
  set-current-plot-pen "means-mean"
  plot-pen-reset
  plot-pen-up
  plotxy (mean sample-mean-list) 0
  plot-pen-down
  plotxy (mean sample-mean-list) plot-y-max
end

;; the presets are suggested population distributions
to preset-setup
  setup
  set range 30
  set ranger range + 1
  ask patches [ set pcolor white]
end

to preset1
  preset-setup
  ask patches with [ (pycor > 0 + min-pycor) and (pycor < 0) ] [ sprout-person ]
end

to preset2
  preset-setup
  ask patches with [ (pycor > 0 + min-pycor) and
                     (pycor < min-pxcor + abs pxcor ) ] [ sprout-person ]
end

to preset3
  preset-setup
  ask patches with [ (pycor > 0 + min-pycor) and (pxcor mod 2 = 1) ] [ sprout-person ]
end

to preset4
  preset-setup
  ask patches with [ (pycor > 0 + min-pycor) and (abs pxcor = max-pxcor) ] [ sprout-person ]
end

to preset5
  preset-setup
  ask patches with [ (pycor > 0 + min-pycor) and (pycor < 2 + (- abs pxcor)) ] [ sprout-person ]
end

to preset6
  preset-setup
  ask patches with [ (pycor > 0 + min-pycor) and (pycor <  1 + pxcor) ] [ sprout-person ]
end

to-report std-dev-sums
  ifelse also-sums?
  [ report standard-deviation sample-sum-list ]
  [ report "N/A" ]
end

to-report expected-value
  ;; from each column of patches, we get the number of turtles multiplied by the patch's "x-value"
  ;; Next, we calculate the mean of this list, to get the expected value of the population
  let columns-list [ ( count turtles with [ xcor = [pxcor] of myself ] ) * ( pxcor + max-pxcor ) ] of patches with [pycor = max-pycor]

  if show-EV? [ report (sum columns-list) / (count turtles ) ]
end
@#$#@#$#@
GRAPHICS-WINDOW
171
10
553
413
15
15
12.0
1
10
1
1
1
0
1
1
1
-15
15
-15
15
1
1
1
ticks
30

BUTTON
8
10
74
43
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

SLIDER
176
411
553
444
range
range
0
30
30
1
1
NIL
HORIZONTAL

PLOT
146
444
577
596
Sample-Data Distribution
Mean Value
Count
0.0
31.0
0.0
10.0
true
false
"" ""
PENS
"means" 1.0 1 -955883 true "" ""
"means-mean" 1.0 0 -10899396 true "" ""
"sums" 1.0 1 -7500403 true "" ""
"sums-mean" 1.0 0 -13345367 true "" ""

SLIDER
7
275
161
308
sample-size
sample-size
1
10
10
1
1
NIL
HORIZONTAL

BUTTON
91
361
164
414
NIL
Go
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
361
84
414
Go Once
Go
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
9
546
107
591
std-dev-means
standard-deviation sample-mean-list
2
1
11

MONITOR
9
443
106
488
num-samples
length sample-mean-list
3
1
11

BUTTON
8
104
161
137
Create My Own People
draw-your-own-people
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
48
160
81
Create Random People
create-population
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
63
85
116
103
and/or
11
0.0
0

TEXTBOX
26
257
150
275
Sampling Commands:
11
0.0
0

BUTTON
15
145
81
178
Preset 1
Preset1
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
85
145
152
178
Preset 2
Preset2
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
15
181
81
214
Preset 3
Preset3
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
85
181
152
214
Preset 4
Preset4
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
15
218
82
251
Preset 5
Preset5
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
85
218
152
251
Preset 6
Preset6
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
20
319
148
352
also-sums?
also-sums?
1
1
-1000

MONITOR
581
547
689
592
std-dev-sums
std-dev-sums
2
1
11

MONITOR
581
445
689
490
Expected Value
expected-value
2
1
11

SWITCH
581
496
689
529
show-EV?
show-EV?
0
1
-1000

@#$#@#$#@
## WHAT IS IT?

This demonstrates relations between population distributions and their sample mean distributions as well as the affect of sample size on this relation. In this model, a population is distributed by some variable, for instance by their total assets in thousands of dollars. The population is distributed randomly -- not necessarily 'normally' -- but sample means from this population nevertheless accumulate in a distribution that approaches a normal curve. The program allows for repeated sampling of individual specimens in the population.

This model is a part of the ProbLab curriculum. The ProbLab curriculum is currently under development at the CCL. For more information about the ProbLab Curriculum please refer to http://ccl.northwestern.edu/curriculum/ProbLab/.

## HOW IT WORKS

Either the program or the user creates a population that ranges along some dimension, such as total assets. In the View, we see this population arranged in a "picture bar chart." For instance, poorer people are farther to the left, and richer people are farther to the right. Next, a group of individual specimens from this population is selected as a sample (these sampled people are painted in a different color). The program calculates the mean value of this sample -- their average assets -- and plots this mean in the histogram below the view. We can set the program to sample repeatedly, and we can observe the emergence of the distribution of sample means. We can also look at the corresponding histogram of the sums of the samples. This allows us to study the relation between the sums and the means in terms of properties of their distributions.

## HOW TO USE IT

Press SETUP, and then press either CREATE RANDOM PEOPLE or CREATE MY OWN PEOPLE or just press one of the PRESET buttons. If you've created random people or if you have pressed one of the presets, you are now ready to press either GO ONCE or GO. But if you've pressed CREATE MY OWN PEOPLE, you now need to click on the View to create these people. Only then you should press either GO ONCE or GO. You can also vary the setting of the sliders. Watch results in the plot and explore relations between the settings and the results. If you'd like to guess the mean value of the sample means before you begin sampling, you can use the RANGE slider to indicate the location of this mean.

Buttons:
SETUP -- initialize variables
CREATE RANDOM PEOPLE -- activates SETUP and then creates a random population in the View
CREATE MY OWN PEOPLE -- allows the user to create persons by clicking on the View
PRESET 1 - PRESET 6 -- creates populations with special patterns of possible interest
GO ONCE -- marks a sample in the population and calculates and plot their mean value.
GO -- repeats GO ONCE indefinitely.

Sliders:
SAMPLER SIZE -- determines the number of specimens sampled at each run through the Go procedure
RANGE -- determines the range of values the members of the population can take.

Switches:
ALSO-SUMS? -- if set to "On," the sums of the samples (and the means of these sums) is plotted as well as the means of the samples (and the mean of these means). If set to "Off," only the means are plotted.
SHOW-EV? -- if set to "On," the EXPECTED VALUE (EV) monitor will display a value

Monitors:
NUM-SAMPLES -- shows the total number of samples taken this the last 'setup.'
EXPECTED VALUE -- calculates the mean x-value of the population. For each column, the program calculates the product of the number of turtles and the x-value of that column. Next, all these products are summed up and divided by the total number of turtles.
STD-DEV-MEANS -- shows the standard deviation of the histogram of sample means. This is an index of how "diffuse" the distribution is. The smaller the number, the tighter (narrower, more clustered) is the distribution of sample means.
STD-DEV-MEANS -- shows the standard deviation of the histogram of sample sums.

Plots:
SAMPLE-MEAN DISTRIBUTION -- distribution of the mean values of all samples taken and the mean sum of all samples taken.

## THINGS TO NOTICE

The property we are looking at is indexed by the "x-value" of the people in the bar chart that is in the View. A person's x-value can be seen in the numerical label at the bottom of its column in the view. For instance, the x-value of people in the left-most column is 0. There could, in principle, be no person with the x-value "0," there could be a single person with that value, or there could be two or more. They all share the same value, because they are all in the same column.

Members of the population that turn green when you press GO or GO ONCE are the 'sample.' Their mean x-value, for instance, their savings, is plotted in the histogram below the view. For example, if a sample of three people is taken and their x-values are 7, 8, and 12, then the histogram column "9" will bump up by one unit, because 9 is the mean of 7, 8, and 12.

For some settings of the population, the more samples you take the more likely you are to get a rare sample. So the distribution you get after only a few samples is not necessarily reflective of all possible mean values.

## THINGS TO TRY

Can you see any connections between the distribution of the population (in the graphics display window) and the mean value of the histogram (in the plot window)? For instance, if there happen to be more population specimens ("people") on the left side of the range, where do you expect to see most of the sample means?

Try running the model with a SAMPLE-SIZE of just 1. What do you get. Now try with a SAMPLE-SIZE of 2. Has anything changed? How about a larger sample size?

Are there any connections between SAMPLE SIZE, RANGE, and STD-DEV? One way to explore this question is to keep two of these variables constant and examine what happens when you change the third variable. You may want to take an equal number of samples for each of these trials.

If you set the model to a sample size that is larger than the total size of the population, you will receive a message telling you cannot do this. However, you may set a sample size that is larger than most columns. This means that the entire sample cannot fit into those columns. Is this a problem? What does this do to the distribution of sample means?

Using the CREATE MY OWN PEOPLE option, build some "unusual" populations. Some of these have already been put into the PRESET buttons. For instance, you could create people only in one or two columns, or you could make the population "U-shaped" (more on the outside and less and less as you go towards the middle). What are your findings?

Again, using the CREATE MY OWN PEOPLE option, build one very tall column off on the right side of the view (about at x-value 8) and build a few very short columns. Set the SAMPLE-SIZE to 10. Press GO ONCE. What can you say about the number of persons that happened to be chosen from the tall column? Try this again and then press GO. Look at the plot. Do you see any connection between the chance of getting samples from the tall column and the location of the mean in the plot?

Set ALSO-SUMS? to "On," and activate the program. Can you explain the similarities and differences between the two histograms you get? For instance, you can look at their range, the total area they cover, their height, and their shape. Try to explain the transformation between the two histograms. For instance, why is the histogram on the left taller than the histogram on the right? Look at the standard deviation of the means and of the sums. What is the ratio between these two values? Does this ratio relate to any other value in the settings of this model?

Relating to the two histograms, can you find a case in which the two histograms converge to a single histogram?

Press SETUP, press CREATE MY OWN PEOPLE and make 6 columns of the height 2 (two persons), set the sample size to 2, and set the ALSO-SUMS? to "On." Now press GO. It is interesting to compare between this statistics activity and a probability activity in which you are rolling a pair of dice. For instance, how many possible columns are there in the sums histogram? In fact, such a comparison can help us think through similarities and differences between statistics and probability.

What is the relation between the number of samples you take, the size of each sample, and the resulting distribution of sample means? For instance, if you have a budge to sample 1,000 people, should you take 10 samples of size 100 each or 100 samples of size 10 each? What do you gain and what do you, perhaps, lose, in each of these choices? For instance, in terms of confidence or in terms of information about the population you are sampling from. To explore this question, you may want to extend the range of the sample size. You may also want to resize the view so as to allow for more specimens in your population. Finally, it may be helpful to have a slider and corresponding code for controlling the total number of samples you are taking.

## PEDAGOGICAL NOTE

The first thing to remember is that in reality we do not know the distribution of the population from which we are sampling. We only have the plot, so to speak. So as you are interacting with this model, you should recall that in applied statistics the Graphic Display does not exist. In this model, however, we are simulating the population -- as if we do know its distribution -- in order to understand the relation between population metrics and their sample means distributions.

You may have noticed that, almost regardless of the shape of your population, the histogram always eventually takes on a certain shape. This shape is called a "normal curve" or "bell curve" or "bell-shaped curve." We say that the histogram "approaches" the normal curve as one takes more and more samples. For special population distributions, we may get special cases of this curve. For instance, if you have created a population that has all the people in the same column, your histogram will be an extreme case of a bell curve -- it itself will consist only of a single column.

Often, people say that, "a population is distributed...etc," but it could be that, sometimes, what they actually mean to say is that, "the sample-means of the population are distributed...etc." This does not imply that the second figure of speech is necessarily preferable, but only that we should understand the difference between these two ideas. In this model, the view shows how the population itself is distributed, whereas the plot shows how the sample means are distributed. Working with this model, one may be struck by the contrast between these two distributions.

The plot shows both the distribution of the sample means and the mean of these means. This mean of means converges on the expected value of sampling from this population. It can be calculated as the average x-value. That is, multiply the x and y values of each column, add these products, and divide the sum by the total number of data points ("persons"). For instance, if there are 3 persons over the 0 value, 2 persons over the 1 value, and 5 persons over the 2 value, the sum of the three x and y products is:
3*0 + 2*1 + 5*2 = 12. We now divide 12 by 10 (the total number of persons).
12 : 10 = 1.2. So if we sample from this population, the mean of the sample means will converge on 1.2. Try this. You can use the above example or any other example you invent.

The biggest challenge is to use this model so as to come up with an explanation of why, we almost always get a bell curve when we take enough samples.

Please note the following point of potential confusion. In order to enable close examination of the sampling process, the populations in this model contains fewer specimens than most populations that are commonly studied by researchers using statistical analysis. For instance, there might be no more than 5 individual specimens in a population who share the same x-value (that is, who are all in the same column). Therefore, a sample that is larger in size than the number of specimens in that column, say a sample of size 8, can never include specimens exclusively from that column. In "real life," it could theoretically happen that an entire random sample is taken from a single column. This means that if you use large sample sizes you should expect to get narrower sample-mean distributions than what one would otherwise expect. For instance, if the left-most column is not tall enough to contain the entire sample, you will never receive a sample mean that is equal in value to the x-value of that column. This is because some of the sample will "spill over" to the right of that column, resulting in a greater sample mean. Because the same logic holds for samples taken from the far right, the sample mean values will be closer to the center than they would be for small samples sizes.

## EXTENDING THE MODEL

The current version of the model allows for repeated sampling of individual specimens. That means that if a person was selected randomly in the first sample, it can be sampled again in the second sample, etc. If we did not allow this repeated sampling, would the sample-mean distribution be affected at all? If so, how? Add code that allows the repeated sampling only as an option and compare the outcomes between the two options.

How do medians behave? The same way as means? Add an option to see the median of both the population and the sample data.

Add a monitor that shows the ratio between the two standard deviations represented in this model.

This model shows means and sums of sampled data. It may be interesting to look at other analyses of the samples. For instance, the product of all values of sampled people as well as the n-th root of this product (for a sample of size n).

How does the standard deviation change as we collect more and more samples? To examine this, you can add a plot of the standard deviation over "time" (over samples).

## NETLOGO FEATURES

In the procedure `draw` we used a temporary variable, `temp-mouse-xcor`. This variable assures that the program won't become "confused." Without this variable, the program might enter a clause in the `ifelse` code that no longer satisfies what the user actually meant when s/he clicked with the mouse. This could occur, because the user is moving the mouse rapidly. So the program selects the `ifelse` clause that is correct at that moment, but meanwhile the mouse click would already be some place else, and so the clause selection would no longer be suitable. The temporary variable avoids this by going through with the instructions as though the mouse were still clicked down where it had been a moment ago.

In the current version, you can create the population, but you cannot experiment with the sampling. Build a procedure that allows you to take your own samples.

## RELATED MODELS

Several ProbLab models look at the emergence of bell-shaped curves through the accumulation of sample means. See, for example, Prob Graphs Basic and Random Basic Advanced. To look closer at the idea of expected value, see the models Expected Value and Expected Value Advanced.

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
NetLogo 5.0beta1
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

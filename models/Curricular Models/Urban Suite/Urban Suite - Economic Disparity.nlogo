breed [ rich a-rich ]
breed [ poor a-poor ]
breed [ jobs job ] ;; jobs are places of employment held by many people

rich-own [utilityr]
poor-own [utilityp]
jobs-own [utility]
patches-own [quality price sddist]
globals [counter view-mode min-poor-util max-poor-util min-rich-util max-rich-util ]

;;
;; Setup Procedures
;;

to setup
  ca
  set view-mode "quality"
  setup-jobs
  setup-patches
  setup-rich
  setup-poor
  ask patches [ update-patch-color ]
  reset-ticks
end

to setup-jobs
  create-jobs 1
  ask jobs
  [
    set color red
    set shape "circle"
    set size 2
  ]
end

to setup-patches
  ask patches [
    set quality 40
    set price 40
  ]
  ask patches
  [
    set sddist min [distance myself] of jobs
  ]
end

to setup-rich
  create-rich 5
  ask rich
  [
    set color 126
    set shape "box"
    let radius 10
    setxy ( ( radius / 2 ) - random-float ( radius * 1.0 ) ) ( ( radius / 2 ) - random-float ( radius * 1.0 ) )
    raise-price
    raise-value
  ]
end

to setup-poor
  create-poor 5
  ask poor
  [
    set color 105
    set shape "box"
    let radius 10
    setxy ( ( radius / 2 ) - random-float ( radius * 1.0 ) ) ( ( radius / 2 ) - random-float ( radius * 1.0 ) )
    decrease-price
    decrease-value
  ]

end

to decrease-value
  ask patch-here [ set quality ( quality * 0.95 ) ]
  ask patches in-radius 1 [ set quality ( quality * 0.96 ) ]
  ask patches in-radius 2 [ set quality ( quality * 0.97 ) ]
  ask patches in-radius 3 [ set quality ( quality * 0.98 ) ]
  ask patches in-radius 4 [ set quality ( quality * 0.99 )
    if (quality < 1) [ set quality 1]
  ]

end

to raise-price
  ask patch-here [ set price ( price * 1.05 ) ]
  ask patches in-radius 1 [ set price ( price * 1.04 ) ]
  ask patches in-radius 2 [ set price ( price * 1.03 ) ]
  ask patches in-radius 3 [ set price ( price * 1.02 ) ]
  ask patches in-radius 4 [ set price ( price * 1.01 )
   if price > 100 [ set price 100 ] ]
end

to raise-value
  ask patch-here [ set quality ( quality * 1.05 ) ]
  ask patches in-radius 1 [ set quality ( quality * 1.04 ) ]
  ask patches in-radius 2 [ set quality ( quality * 1.03 ) ]
  ask patches in-radius 3 [ set quality ( quality * 1.02 ) ]
  ask patches in-radius 4 [ set quality ( quality * 1.01 )
    if quality > 100 [ set quality 100 ]
  ]
end

to decrease-price
  ask patch-here [ set price ( price * 0.95 ) ]
  ask patches in-radius 1 [ set price ( price * 0.96 ) ]
  ask patches in-radius 2 [ set price ( price * 0.97 ) ]
  ask patches in-radius 3 [ set price ( price * 0.98 ) ]
  ask patches in-radius 4 [ set price ( price * 0.99 )
    if (price < 1) [ set price 1]
  ]
end

;;
;; Runtime Procedures
;;

to go
  locate-poor
  locate-rich
  if counter > residents-per-job
  [
    locate-service
    set counter 0
  ]
  if count (rich) >= 20 [kill-rich]
  if count (poor) >= 20 [kill-poor]
  if count (jobs) >= max-jobs [kill-service]
  update-view
  do-plots
  tick
end

to locate-poor
  set counter ( counter + poor-per-step )
  create-poor poor-per-step
  [
    set color 105
    set shape "box"
    evaluate-poor
    decrease-value
    decrease-price
  ]
end

to locate-rich
  set counter ( counter + rich-per-step )
  create-rich rich-per-step
  [
    set color 126
    set shape "box"
    evaluate-rich
    raise-price
    raise-value
  ]
end

to evaluate-poor
  let candidate-patches n-of number-of-tests patches
  set candidate-patches candidate-patches with [ not any? turtles-here ]
  if (not any? candidate-patches)
    [ stop ]

  ;; we use a hedonistic utility function for our agents, shown below
  ;; basically, poor people are looking for inexpensive real estate, close to jobs
  let best-candidate max-one-of candidate-patches
    [ patch-utility-for-poor ]
  move-to best-candidate
  set utilityp [ patch-utility-for-poor ] of best-candidate
end

to-report patch-utility-for-poor
    report ( ( 1 / (sddist / 100 + 0.1) ) ^ ( 1 - poor-price-priority ) ) * ( ( 1 / price ) ^ ( 1 + poor-price-priority ) )
end

to evaluate-rich
  let candidate-patches n-of number-of-tests patches
  set candidate-patches candidate-patches with [ not any? turtles-here ]
  if (not any? candidate-patches)
    [ stop ]

  ;; we use a hedonistic utility function for our agents, shown below
  ;; basically, rich people are looking for good quality real estate, close to jobs
  let best-candidate max-one-of candidate-patches
        [ patch-utility-for-rich ]
  move-to best-candidate
  set utilityr [ patch-utility-for-rich ] of best-candidate
end

to-report patch-utility-for-rich
  report ( ( 1 / (sddist + 0.1) ) ^ ( 1 - rich-quality-priority ) ) * ( quality ^ ( 1 + rich-quality-priority) )
end

to kill-poor
  repeat ( death-rate )
  [
    ;always kill the person that's been around the longest
    ask min-one-of poor [who]
      [ die ]
  ]
end

to kill-rich
  repeat ( death-rate)
  [
    ;always kill the person that's been around the longest
    ask min-one-of rich [who]
      [ die ]
  ]
end

to kill-service
  ; always kill the oldest job
  ask min-one-of jobs [who]
    [ die ]
  ask patches
    [ set sddist min [distance myself + .01] of jobs ]
end

to locate-service
  let empty-patches patches with [ not any? turtles-here ]

  if any? empty-patches
  [
    ask one-of empty-patches
    [
      sprout-jobs 1
      [
        set color red
        set shape "circle"
        set size 2
        evaluate-job
      ]
    ]
    ask patches
      [ set sddist min [distance myself + .01] of jobs ]
  ]
end

to evaluate-job
  let candidate-patches n-of number-of-tests patches
  set candidate-patches candidate-patches with [ not any? turtles-here ]
  if (not any? candidate-patches)
    [ stop ]

  ;; In this model, we assume that jobs move toward where the money is.
  ;; The validity of this assumption in a real-world setting is worthy of skepticism.
  ;;
  ;; However, it may not be entirely unreasonable. For instance, places with higher real
  ;; estate values are more likely to have affluent people nearby that will spend money
  ;; at retail commercial shops.
  ;;
  ;; On the other hand, companies would like to pay less rent, and so they may prefer to buy
  ;; land at low real-estate values
  ;; (particularly true for industrial sectors, which have no need for consumers neraby)
  let best-candidate max-one-of candidate-patches [ price ]
  move-to best-candidate
  set utility [ price ] of best-candidate
end

;;
;; Visualization Procedures
;;

to update-view
  if (view-mode = "poor-utility" or view-mode = "rich-utility")
  [
    let poor-util-list [ patch-utility-for-poor ] of patches
    set min-poor-util min poor-util-list
    set max-poor-util max poor-util-list

    let rich-util-list [ patch-utility-for-rich ] of patches
    set min-rich-util min rich-util-list
    set max-rich-util max rich-util-list
  ]

  ask patches [ update-patch-color ]
end

to update-patch-color
  ;; the particular constants we use to scale the colors in the display
  ;; are mainly chosen for visual appeal
  ifelse view-mode = "quality"
  [
    set pcolor scale-color green quality 1 100
  ][
  ifelse view-mode = "price"
  [
    set pcolor scale-color yellow price 0 100
  ][
  ifelse view-mode = "dist"
  [
    set pcolor scale-color blue sddist  ( 0.45 * ( max-pxcor * 1.414 ) ) ( 0.05 * ( max-pxcor * 1.414 ) )
  ][
  ifelse view-mode = "poor-utility"
  [
    ; use a logarithm for coloring, so we see better gradation
    set pcolor scale-color sky ln patch-utility-for-poor ln min-poor-util ln max-poor-util
  ][
  if view-mode = "rich-utility"
  [
    ; use a logarithm for coloring, so we see better gradation
    set pcolor scale-color pink ln patch-utility-for-rich ln min-rich-util ln max-rich-util
  ]]]]]
end

;;
;; Plotting Procedure
;;

to do-plots
  let rtotal 0
  let ptotal 0
  let step 0
  let rtime 0
  let ptime 0

  set-current-plot "Travel Distance"
  set rtotal 0
  set rtime 0
  set ptotal 0
  set ptime 0
  set-current-plot-pen "rich"
  plot median [ min [distance myself] of jobs ] of rich

  set-current-plot-pen "poor"
  plot median [ min [distance myself] of jobs ] of poor
end
@#$#@#$#@
GRAPHICS-WINDOW
323
11
778
487
44
44
5.0
1
10
1
1
1
0
0
0
1
-44
44
-44
44
1
1
1
ticks
30

BUTTON
41
24
119
57
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
121
24
197
57
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

SLIDER
10
150
160
183
number-of-tests
number-of-tests
0
30
15
1
1
NIL
HORIZONTAL

PLOT
25
296
305
481
Travel Distance
time
# cells
0.0
100.0
0.0
20.0
true
true
"" ""
PENS
"rich" 1.0 0 -3508570 true "" ""
"poor" 1.0 0 -14070903 true "" ""

BUTTON
199
24
277
57
go-once
go
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
39
74
118
107
view price
set view-mode \"price\"\nupdate-view
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
120
74
196
107
view quality
set view-mode \"quality\"\nupdate-view
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
10
185
160
218
residents-per-job
residents-per-job
0
500
100
10
1
NIL
HORIZONTAL

SLIDER
162
220
312
253
poor-per-step
poor-per-step
0
15
5
1
1
NIL
HORIZONTAL

SLIDER
162
255
312
288
rich-per-step
rich-per-step
0
15
5
1
1
NIL
HORIZONTAL

BUTTON
198
74
275
107
view dist
set view-mode \"dist\"\nupdate-view
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
162
150
312
183
poor-price-priority
poor-price-priority
-1
1
0
.1
1
NIL
HORIZONTAL

SLIDER
162
185
312
218
rich-quality-priority
rich-quality-priority
-1
1
0
0.1
1
NIL
HORIZONTAL

SLIDER
10
220
160
253
max-jobs
max-jobs
5
20
10
1
1
NIL
HORIZONTAL

SLIDER
10
255
160
288
death-rate
death-rate
0
15
4
1
1
NIL
HORIZONTAL

MONITOR
783
12
858
57
# of jobs
count jobs
17
1
11

MONITOR
783
62
858
107
population
count poor + count rich
17
1
11

BUTTON
39
109
154
142
view rich-utility
set view-mode \"rich-utility\"\nupdate-view
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
156
109
275
142
view poor-utility
set view-mode \"poor-utility\"\nupdate-view
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
783
112
858
157
poor pop
count poor
17
1
11

MONITOR
783
162
858
207
rich pop
count rich
17
1
11

@#$#@#$#@
## WHAT IS IT?

This model explores residential land-usage patterns from an economic perspective, using the socio-economic status of the agents to determine their preferences for choosing a location to live.  It models the growth of two populations, one rich and one poor, who settle based on three properties of the landscape: the perceived quality, the cost of living, and the proximity to services (large red dots). These same properties then change based on where the different populations settle.

The model ultimately shows the segregation of populations based on income, the clustering of services in more affluent areas, and how people's attitude can lead either to a cluster condition (emphasis on proximity), or a condition of sprawl (emphasis on cost or quality).

## HOW IT WORKS

Job sites (shown as red circles on the map) are created and destroyed.  People (shown as small blue and pink squares) move in and move out.  These people want to live near to jobs, but also consider the price (cost of living) and quality of prospective locations.  But let's get more specific.

When a new place of employment comes into the world, it randomly samples some number of locations (controlled by the NUMBER-OF-TESTS slider), and chooses the one with the highest price (i.e. land-value).  This may seem irrational at first, but the assumption this model makes is that jobs move toward where the wealth is.  If there is more money in a certain area, then there are more affluent people to spend that money on goods and services.

The validity of this assumption in a real-world setting is worthy of skepticism.  For instance, companies also wish to pay less rent or property tax, and so alternatively one could argue that they would be seeking the least expensive piece of land to occupy.  This seems particularly true for the industrial sector, which has no need for consumers to be nearby.

In any case, the reader is encouraged to carefully examine all assumptions, challenge them, and perhaps extend the model to incorporate assumptions that are more plausible in his/her view.  A model's predictions are only as good as the assumptions that underlie them.

Each "tick" of the model, some number of new poor people (controlled by the POOR-PER-STEP slider) and new rich people (controlled by the RICH-PER-STEP slider) enter into the world.  When people enter the world, they randomly sample some number of locations, and choose to inhabit the one that maximizes "utility" for them, which is given by a hedonistic utility function.

There are two fundamentally different types of people in this model -- "poor" people (shown in blue) and "rich" people (shown in pink), and they have differing priorities.  Both types of people wish to be located close to a place of employment.  However, rich people seek a location that has good quality, heedless of price, whereas poor people seek locations with low price, disregarding quality.

The last important rule of the model is the effect that agents have on the land they inhabit.  Rich people moving into an area cause the land price and quality to increase, whereas poor people cause the land price and quality to decrease.  Nearby land attributes are affected as well, with the effect diminishing over distance.

## HOW TO USE IT

Click the SETUP button first, to set up the model.  All land in the world has the same price and quality.  One job location is placed in the middle of the world, and several rich and poor people are spread out nearby it, which immediately affect the quality and price of the land they inhabit, as well as nearby land.

Click the GO button to start the simulation.  To step through the simulation one "tick" at a time, use the GO-ONCE button.

There are five view modes, which are controlled by the buttons VIEW PRICE, VIEW QUALITY, VIEW DIST., VIEW RICH-UTILITY, and VIEW-POOR-UTILITY

VIEW PRICE displays the land-price value of each location, with white being a high price, black being a low price, and the various shades of yellow are in between.

VIEW QUALITY displays the quality value of each location, with white being a high price, black being a low price, and the various shades of green are in between.

VIEW DIST. displays the distance from each location to a place of employment.  Brighter colors demonstrate closeness.

VIEW RICH-UTILITY displays the utility that rich people assign to each location on the map.  Lighter values designate better utility, and darker values designate worse utility.  Note that the highest utility areas may still be vacant, since each agent only samples a small set of the patches in the world

VIEW-POOR-UTILITY displays the utility that poor people assign to each location on the map.  Lighter values designate better utility, and darker values designate worse utility.

The NUMBER-OF-TESTS slider affects how many locations each agent looks at when choosing a location that optimizes the agent's utility.

The RESIDENTS-PER-JOB slider determines how often a new place of employment is created in the world.  For every RESIDENTS-PER-JOB people, a new place of employment appears.

There is,  however,  a maximum number of places of employment, which is controlled by the MAX-JOBS slider.

Some number of poor people enter the world each time step, as determined by the POOR-PER-STEP slider.  Likewise, some number of rich people (determined by the RICH-PER-STEP slider) enter the world.

Some number of poor people and rich people disappear from the world each turn, as well, which is determined by the DEATH-RATE slider.  Although it is called "death rate", it should not be taken literally -- it merely represents the disappearance of agents from the world that is being considered by the model, which could be caused by a number of factors (such as emigration).  If DEATH-RATE is set to 5, this means that both 5 rich people and 5 poor people disappear each time step.  The agents removed are always those that have been in the world for the longest period of time.

The priorities of the poor people can be adjusted with the POOR-PRICE-PRIORITY slider.  If this slider is set to -1, this means that poor people do not care about price at all, and are only interested in being close to employment.  If this slider is set to 1, then poor people only care about price, and do not concern themselves with job locations.  Setting the slider at 0 balances these two factors.

Similarly, the priorities of rich people can be adjusted with the RICH-QUALITY-PRIORITY slider.  On this slider, -1 means that rich people care only about having a short commute to their jobs, and not about the quality of the land, whereas 1 means that they care only about quality, and are not concerned with distance to employment.  Again, 0 represents an equal balance of these priorities.

The TRAVEL DISTANCE plot shows the average distance that poor and rich people must travel to reach the nearest point of employment.  Apart from the interesting visual patterns that form in the view, this plot is the most important output of the model.

The # OF JOBS monitor tells how many places of employment are currently in the world.

The POPULATION monitor tells how many total people there are in the world, and the POOR POP and RICH POP monitors give the poor and rich population sizes respectively.

## THINGS TO NOTICE

Do the VIEW PRICE mode and the VIEW QUALITY mode look very similar?  Apart from the fact that one is green, and the other is yellow, they might be showing identical values?  To test this, you can right click (or control-click on a Mac computer) on one of the patches in the view and choose "inspect patch XX YY".  You can do this for several patches, and you will find that the price and quality are always the same.  In this model, whenever quality goes up or down, price changes in direct proportion.

What if NUMBER-OF-TESTS is small?  Is the population more or less densely centered around the jobs?  What if NUMBER-OF-TESTS is large?

Change the viewing mode to distance-from-jobs, by clicking the VIEW DIST button.  Watch the model run, and the lines that form where gradients from two different jobs come together and overlap.  These shapes are related to Voronoi polygons.  You can learn more about them by looking at the "Voronoi" model in the NetLogo model library.

Even if the DEATH-RATE is set such that more people are leaving the world than entering it, the model does not allow the population to die out entirely -- instead, the population will stay small.  If you grow the population for a while, and then raise the DEATH-RATE to balance out rich and poor people entering the world, then you can hold the population constant.  In this case, you might view the scenario as the same people relocating within the model, rather than new people entering and old people leaving.

## THINGS TO TRY

After letting the model run for a while, try switching back and forth between the VIEW POOR-UTILITY and VIEW RICH-UTILITY display modes.  How many places that are dark for rich are bright for poor?  Is there usually an inverse relationship?  Are there places which both rich and poor find desirable, and if so, where are they?  What if you move both priority sliders to the left?

Try drastically changing the POOR-PRICE-PRIORITY and RICH-QUALITY-PRIORITY sliders.  Do rich people always have the shorter distances to employment, or do poor people sometimes have the shorter distances?

## EXTENDING THE MODEL

As noted above in the THINGS TO NOTICE section, in this model price and quality of land are locked together, always holding the same values.  Extend this model so that this isn't always the case.  For example, you might make it so that when new people move into an area of the model, they only affect the quality of nearby locations in a small radius, whereas they affect the price of a broader circle of cells.

## NETLOGO FEATURES

This model makes use of NetLogo's breeds to differentiate rich agents, poor agents, and job agents.

Extensive use is also made of the SCALE-COLOR primitive, which allows for the three different view modes of the model.

## RELATED MODELS

This model is related to all of the other models in the "Urban Suite".  In particular, this model shows elements of the concept of positive feedback, which is demonstrated in the "Urban Suite - Positive Feedback" model.

It might also be interesting to compare it to the models "Wealth Distribution" and "Voronoi".

## CREDITS AND REFERENCES

This model was loosely based on a model originally written by William Rand and Derek Robinson as part of the Sluce Project at the University of Michigan (http://www.cscs.umich.edu/sluce).  For more about the original model (SOME) that was the basis for this model, please see:

Brown D.G., Robinson D.T., Nassauer J.I., An L., Page S.E., Low B., Rand W., Zellner M., and R. Riolo (In Press) "Exurbia from the Bottom-Up: Agent-Based Modeling and Empirical Requirements." Geoforum.

This model was developed during the Sprawl/Swarm Class at Illinois Institute of Technology in Fall 2006 under the supervision of Sarah Dunn and Martin Felsen, by the following group of students: Danil Nagy and Bridget Dodd.  See http://www.sprawlcity.us/ for more details.

Further modifications and refinements were made by members of the Center for Connected Learning and Computer-Based Modeling before releasing it as an Urban Suite model.

The Urban Suite models were developed as part of the Procedural Modeling of Cities project, under the sponsorship of NSF ITR award 0326542, Electronic Arts & Maxis.

Please see the project web site ( http://ccl.northwestern.edu/cities/ ) for more information.
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

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

bee
true
0
Polygon -1184463 true false 151 152 137 77 105 67 89 67 66 74 48 85 36 100 24 116 14 134 0 151 15 167 22 182 40 206 58 220 82 226 105 226 134 222
Polygon -16777216 true false 151 150 149 128 149 114 155 98 178 80 197 80 217 81 233 95 242 117 246 141 247 151 245 177 234 195 218 207 206 211 184 211 161 204 151 189 148 171
Polygon -7500403 true true 246 151 241 119 240 96 250 81 261 78 275 87 282 103 277 115 287 121 299 150 286 180 277 189 283 197 281 210 270 222 256 222 243 212 242 192
Polygon -16777216 true false 115 70 129 74 128 223 114 224
Polygon -16777216 true false 89 67 74 71 74 224 89 225 89 67
Polygon -16777216 true false 43 91 31 106 31 195 45 211
Line -1 false 200 144 213 70
Line -1 false 213 70 213 45
Line -1 false 214 45 203 26
Line -1 false 204 26 185 22
Line -1 false 185 22 170 25
Line -1 false 169 26 159 37
Line -1 false 159 37 156 55
Line -1 false 157 55 199 143
Line -1 false 200 141 162 227
Line -1 false 162 227 163 241
Line -1 false 163 241 171 249
Line -1 false 171 249 190 254
Line -1 false 192 253 203 248
Line -1 false 205 249 218 235
Line -1 false 218 235 200 144

bird1
false
0
Polygon -7500403 true true 2 6 2 39 270 298 297 298 299 271 187 160 279 75 276 22 100 67 31 0

bird2
false
0
Polygon -7500403 true true 2 4 33 4 298 270 298 298 272 298 155 184 117 289 61 295 61 105 0 43

boat1
false
0
Polygon -1 true false 63 162 90 207 223 207 290 162
Rectangle -6459832 true false 150 32 157 162
Polygon -13345367 true false 150 34 131 49 145 47 147 48 149 49
Polygon -7500403 true true 158 33 230 157 182 150 169 151 157 156
Polygon -7500403 true true 149 55 88 143 103 139 111 136 117 139 126 145 130 147 139 147 146 146 149 55

boat2
false
0
Polygon -1 true false 63 162 90 207 223 207 290 162
Rectangle -6459832 true false 150 32 157 162
Polygon -13345367 true false 150 34 131 49 145 47 147 48 149 49
Polygon -7500403 true true 157 54 175 79 174 96 185 102 178 112 194 124 196 131 190 139 192 146 211 151 216 154 157 154
Polygon -7500403 true true 150 74 146 91 139 99 143 114 141 123 137 126 131 129 132 139 142 136 126 142 119 147 148 147

boat3
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

butterfly1
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

circle
false
0
Circle -7500403 true true 35 35 230

link
true
0
Line -7500403 true 150 0 150 300

link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

person
false
0
Circle -7500403 true true 155 20 63
Rectangle -7500403 true true 158 79 217 164
Polygon -7500403 true true 158 81 110 129 131 143 158 109 165 110
Polygon -7500403 true true 216 83 267 123 248 143 215 107
Polygon -7500403 true true 167 163 145 234 183 234 183 163
Polygon -7500403 true true 195 163 195 233 227 233 206 159

sheep
false
15
Rectangle -1 true true 90 75 270 225
Circle -1 true true 15 75 150
Rectangle -16777216 true false 81 225 134 286
Rectangle -16777216 true false 180 225 238 285
Circle -16777216 true false 1 88 92

spacecraft
true
0
Polygon -7500403 true true 150 0 180 135 255 255 225 240 150 180 75 240 45 255 120 135

thin-arrow
true
0
Polygon -7500403 true true 150 0 0 150 120 150 120 293 180 293 180 150 300 150

truck-down
false
0
Polygon -7500403 true true 225 30 225 270 120 270 105 210 60 180 45 30 105 60 105 30
Polygon -8630108 true false 195 75 195 120 240 120 240 75
Polygon -8630108 true false 195 225 195 180 240 180 240 225

truck-left
false
0
Polygon -7500403 true true 120 135 225 135 225 210 75 210 75 165 105 165
Polygon -8630108 true false 90 210 105 225 120 210
Polygon -8630108 true false 180 210 195 225 210 210

truck-right
false
0
Polygon -7500403 true true 180 135 75 135 75 210 225 210 225 165 195 165
Polygon -8630108 true false 210 210 195 225 180 210
Polygon -8630108 true false 120 210 105 225 90 210

turtle
true
0
Polygon -7500403 true true 138 75 162 75 165 105 225 105 225 142 195 135 195 187 225 195 225 225 195 217 195 202 105 202 105 217 75 225 75 195 105 187 105 135 75 142 75 105 135 105

wolf
false
0
Rectangle -7500403 true true 15 105 105 165
Rectangle -7500403 true true 45 90 105 105
Polygon -7500403 true true 60 90 83 44 104 90
Polygon -16777216 true false 67 90 82 59 97 89
Rectangle -1 true false 48 93 59 105
Rectangle -16777216 true false 51 96 55 101
Rectangle -16777216 true false 0 121 15 135
Rectangle -16777216 true false 15 136 60 151
Polygon -1 true false 15 136 23 149 31 136
Polygon -1 true false 30 151 37 136 43 151
Rectangle -7500403 true true 105 120 263 195
Rectangle -7500403 true true 108 195 259 201
Rectangle -7500403 true true 114 201 252 210
Rectangle -7500403 true true 120 210 243 214
Rectangle -7500403 true true 115 114 255 120
Rectangle -7500403 true true 128 108 248 114
Rectangle -7500403 true true 150 105 225 108
Rectangle -7500403 true true 132 214 155 270
Rectangle -7500403 true true 110 260 132 270
Rectangle -7500403 true true 210 214 232 270
Rectangle -7500403 true true 189 260 210 270
Line -7500403 true 263 127 281 155
Line -7500403 true 281 155 281 192

wolf-left
false
3
Polygon -6459832 true true 117 97 91 74 66 74 60 85 36 85 38 92 44 97 62 97 81 117 84 134 92 147 109 152 136 144 174 144 174 103 143 103 134 97
Polygon -6459832 true true 87 80 79 55 76 79
Polygon -6459832 true true 81 75 70 58 73 82
Polygon -6459832 true true 99 131 76 152 76 163 96 182 104 182 109 173 102 167 99 173 87 159 104 140
Polygon -6459832 true true 107 138 107 186 98 190 99 196 112 196 115 190
Polygon -6459832 true true 116 140 114 189 105 137
Rectangle -6459832 true true 109 150 114 192
Rectangle -6459832 true true 111 143 116 191
Polygon -6459832 true true 168 106 184 98 205 98 218 115 218 137 186 164 196 176 195 194 178 195 178 183 188 183 169 164 173 144
Polygon -6459832 true true 207 140 200 163 206 175 207 192 193 189 192 177 198 176 185 150
Polygon -6459832 true true 214 134 203 168 192 148
Polygon -6459832 true true 204 151 203 176 193 148
Polygon -6459832 true true 207 103 221 98 236 101 243 115 243 128 256 142 239 143 233 133 225 115 214 114

wolf-right
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

@#$#@#$#@
NetLogo 5.0beta1
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

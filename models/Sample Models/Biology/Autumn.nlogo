breed [leaves leaf]
breed [dead-leaves dead-leaf]
breed [raindrops raindrop]
breed [suns sun]

leaves-own [
  water-level       ;; amount of water in the leaf
  sugar-level       ;; amount of sugar in the leaf
  attachedness      ;; how attached the leaf is to the tree
  chlorophyll       ;; level of chemical making the leaf green
  carotene          ;; level of chemical making the leaf yellow
  anthocyanin       ;; level of chemical making the leaf red
]

raindrops-own [
  location          ;; either "falling", "in root", "in trunk", or "in leaves"
  amount-of-water
]

globals [
  bottom-line        ;; controls where the ground is
  evaporation-temp   ;; temperature at which water evaporates
]

;; ---------------------------------------
;; setup
;; ---------------------------------------

to setup
  clear-all
  set bottom-line min-pycor + 1
  set evaporation-temp 30
  set-default-shape raindrops "circle"
  set-default-shape suns "circle"

  ;; Create sky and grass
  ask patches
    [ set pcolor blue - 2 ]
  ask patches with [pycor < min-pycor + 2]
    [ set pcolor green ]

  ;; Create leaves
  create-leaves number-of-leaves [
    set chlorophyll 50 + random 50
    set water-level 75 + random 25
    ;; the sugar level is drawn from a normal distribution based on user inputs
    set sugar-level random-normal start-sugar-mean start-sugar-stddev
    set carotene random 100
    change-color
    set attachedness 100 + random 50
    ;; using sqrt in the next command makes the turtles be
    ;; evenly distributed; if we just said "fd random-float 10"
    ;; there'd be more turtles near the center of the tree,
    ;; which would look funny
    fd sqrt random-float 100
  ]

  ;; Create trunk and branches
  ask patches with [pxcor = 0 and pycor <= 5 or
                    abs pxcor = (pycor + 2) and pycor < 4 or
                    abs pxcor = (pycor + 8) and pycor < 3]
    [ set pcolor brown ]

  ;; Create the sun
  create-suns 1 [
    setxy max-pxcor - 2
          max-pycor - 3
    ;; change appearance based on intensity
    show-intensity
  ]

  ;; plot the initial state
  reset-ticks
end


;; ---------------------------------------
;; go
;; ---------------------------------------

to go
  ;; Stop if all of the leaves are dead
  if not any? leaves [ stop ]

  ;; Have the wind blow and rain fall;
  ;; move any water in the sky, on the ground, and in the tree;
  ;; set the appearance of the sun on the basis of its intensity.
  make-wind-blow
  make-rain-fall
  move-water
  ask suns [ show-intensity ]

  ;; Now our leaves respond accordingly
  ask attached-leaves
  [
    adjust-water
    adjust-chlorophyll
    adjust-sugar
    change-color
    change-shape
  ]

  ;; if the leaves are falling keep falling
  ask leaves [ fall-if-necessary ]

  ;; Leaves on the bottom should be killed off
  ask leaves with [ycor <= bottom-line]
    [ set breed dead-leaves ]

  ;; Leaves without water should also be killed off
  ask leaves with [water-level < 1]
    [ set attachedness 0 ]

  ;; Make sure that values remain between 0 - 100
  ask leaves [
    if chlorophyll < 0 [ set chlorophyll 0 ]
    if chlorophyll > 100 [ set chlorophyll 100 ]
    if water-level < 0 [ set water-level 0 ]
    if water-level > 100 [ set water-level 100 ]
    if sugar-level < 0 [ set sugar-level 0 ]
    if sugar-level > 100 [ set sugar-level 100 ]
    if carotene < 0 [ set carotene 0 ]
    if carotene > 100 [ set carotene 100 ]
    if anthocyanin < 0 [ set anthocyanin 0 ]
    if anthocyanin > 100 [ set anthocyanin 100 ]
    if attachedness < 0 [ set attachedness 0 ]
    if attachedness > 100 [ set attachedness 100 ]
  ]

  ;; increment the tick counter
  tick
end


;; ---------------------------------------
;; make-wind-blow: When the wind blows,
;; the leaves move around a little bit
;; (for a nice visual effect), and
;; reduce their attachedness by the wind factor.
;; This means that leaves will fall off more
;; rapidly in stronger winds.
;; ---------------------------------------

to make-wind-blow
  ask leaves [ ifelse random 2 = 1
                 [ rt 10 * wind-factor ]
                 [ lt 10 * wind-factor ]
               set attachedness attachedness - wind-factor
             ]
end


;; ---------------------------------------
;; make-rain-fall: rain is a separate breed
;; of small turtles that come from the top of the world.
;; ---------------------------------------

to make-rain-fall
  ;; Create new raindrops at the top of the world
  create-raindrops rain-intensity [
    setxy random-xcor max-pycor
    set heading 180
    fd 0.5 - random-float 1.0
    set size .3
    set color gray
    set location "falling"
    set amount-of-water 10
  ]
  ;; Now move all the raindrops, including
  ;; the ones we just created.
  ask raindrops [ fd random-float 2 ]
end


;; --------------------------------------------------------
;; move-water: water goes from raindrops -> ground,
;; ground -> trunk/branches, and trunk/branches to leaves.
;; --------------------------------------------------------

to move-water

  ;; We assume that the roots extend under the entire grassy area; rain flows through
  ;; the roots to the trunk
  ask raindrops with [location = "falling" and pcolor = green] [
    set location "in roots"
    face patch 0 ycor
  ]

  ;; Water flows from the trunk up to the central part of the tree.
  ask raindrops with [location = "in roots" and pcolor = brown] [
    face patch 0 0
    set location "in trunk"
  ]

  ;; Water flows out from the trunk to the leaves.  We're not going to
  ;; simulate branches here in a serious way
  ask raindrops with [location = "in trunk" and patch-here = patch 0 0] [
    set location "in leaves"
    set heading random 360
  ]

  ;; if the raindrop is in the leaves and there is nothing left disappear
  ask raindrops with [location = "in leaves" and amount-of-water <= 0.5] [
    die
  ]

  ;; if the raindrops are in the trunk or leaves and they are at a place
  ;; where they can no longer flow into a leaf then disappear
  ask raindrops with [
    (location = "in trunk" or location = "in leaves")
     and (ycor > max [ycor] of leaves or
          xcor > max [xcor] of leaves or
          xcor < min [xcor] of leaves) ] [
    die
  ]

end

;;---------------------------------------------------------
;; Turtle Procedures
;; --------------------------------------------------------

;; --------------------------------------------------------
;; show-intensity: Change how the sun looks to indicate
;; intensity of sunshine.
;; --------------------------------------------------------

to show-intensity  ;; sun procedure
  set color scale-color yellow sun-intensity 0 150
  set size sun-intensity / 10
  set label word sun-intensity "%"
  ifelse sun-intensity < 50
    [ set label-color yellow ]
    [ set label-color black  ]
end

;; --------------------------------------------------------
;; adjust-water: Handle the ups and downs of water within the leaf
;; --------------------------------------------------------

to adjust-water
  ;; Below a certain temperature, the leaf does not absorb
  ;; water any more.  Instead, it converts sugar and and water
  ;; to anthocyanin, in a proportion
  if temperature < 10 [ stop  ]

  ;; If there is a raindrop near this leaf with some water
  ;; left in it, then absorb some of that water
  let nearby-raindrops raindrops in-radius 2 with [location = "in leaves" and amount-of-water >= 0]

  if any? nearby-raindrops [
    let my-raindrop min-one-of nearby-raindrops [distance myself]
    set water-level water-level + ([amount-of-water] of my-raindrop * 0.20)
    ask my-raindrop [
      set amount-of-water (amount-of-water * 0.80)
    ]
  ]

  ;; Reduce the water according to the temperature
  if temperature > evaporation-temp
    [ set water-level water-level - (0.5 * (temperature - evaporation-temp)) ]

  ;; If the water level goes too low, reduce the attachedness
  if water-level < 25
    [ set attachedness attachedness - 1 ]

end


;; ---------------------------------------
;; adjust-chlorophyll: It's not easy being green.
;; Chlorophyll gets reduces when the temperature is
;; low, or when the sun is strong.  It increases when
;; the temperature is normal and the sun is shining.
;; ---------------------------------------

to adjust-chlorophyll

  ;; If the temperature is low, then reduce the chlorophyll
  if temperature < 15
    [ set chlorophyll chlorophyll - (.5 * (15 - temperature)) ]

  ;; If the sun is strong, then reduce the chlorophyll
  if sun-intensity > 75
    [ set chlorophyll chlorophyll - (.5 * (sun-intensity - 75)) ]

  ;; New chlorophyll comes from water and sunlight
  if temperature > 15 and sun-intensity > 20
    [ set chlorophyll chlorophyll + 1 ]

end


;; ---------------------------------------
;; adjust-sugar: water + sunlight + chlorophyll = sugar
;; ---------------------------------------

to adjust-sugar
  ;; If there is enough water and sunlight, reduce the chlorophyll
  ;; and water, and increase the sugar
  if water-level > 1 and sun-intensity > 20 and chlorophyll > 1
    [ set water-level water-level - 0.5
      set chlorophyll chlorophyll - 0.5
      set sugar-level sugar-level + 1
      set attachedness attachedness + 5
    ]

  ;; Every tick of the clock, we reduce the sugar by 1
  set sugar-level sugar-level - 0.5
end

;; ---------------------------------------
;; fall-if-necessary:  If a leaf is above the bottom row, make it fall down
;; If it hits the bottom line, make it a dead-leaf
;; ---------------------------------------

to fall-if-necessary
  if attachedness > 0 [ stop ]
  if ycor > bottom-line
    [
      let target-xcor (xcor + random-float wind-factor
                            - random-float wind-factor)
      facexy target-xcor bottom-line
      fd random-float (.7 * max (list wind-factor .5))
     ]
end


;; ---------------------------------------
;; change-color: Because NetLogo has a limited color scheme,
;; we need very simple rules
;; ---------------------------------------

to change-color
  ;; If the temperature is low, then we turn the
  ;; sugar into anthocyanin
  if temperature < 20 and sugar-level > 0 and water-level > 0
    [ set sugar-level sugar-level - 1
      set water-level water-level - 1
      set anthocyanin anthocyanin + 1 ]

  ;; If we have more than 50 percent chlorophyll, then
  ;; we are green, and scale the color accordingly
  ifelse chlorophyll > 50
       [ set color scale-color green chlorophyll 150 -50 ]

  ;; If we are lower than 50 percent chlorophyll, then
  ;; we have yellow (according to the carotene), red (according
  ;; to the anthocyanin), or orange (if they are about equal).

       ;; If we have roughly equal anthocyanin and carotene,
       ;; then the leaves should be in orange.
       [ if abs (anthocyanin - carotene ) < 10
           [ set color scale-color orange carotene 150 -50 ]

         if anthocyanin > carotene + 10
           [ set color scale-color red anthocyanin 170 -50 ]

         if carotene > anthocyanin + 10
           [ set color scale-color yellow carotene 150 -50 ]
       ]
end

to change-shape
  ifelse leaf-display-mode = "solid"
    [ set shape "default" ]
  [ if leaf-display-mode = "chlorophyll"
      [ set-shape-for-value chlorophyll ]
    if leaf-display-mode = "water"
      [ set-shape-for-value water-level ]
    if leaf-display-mode = "sugar"
      [ set-shape-for-value sugar-level ]
    if leaf-display-mode = "carotene"
      [ set-shape-for-value carotene ]
    if leaf-display-mode = "anthocyanin"
      [ set-shape-for-value anthocyanin ]
    if leaf-display-mode = "attachedness"
      [ set-shape-for-value attachedness ]
  ]
end

;; returns all leaves still attached
to-report attached-leaves
  report leaves with [attachedness > 0]
end

;; makes the leaf appear to be more or less filled depending on value
to set-shape-for-value [value]
  ifelse value > 75 [ set shape "default" ]
  [ ifelse value <= 25 [ set shape "default one-quarter" ]
    [ ifelse value <= 50 [ set shape "default half" ]
                         [ set shape "default three-quarter" ]]]
end
@#$#@#$#@
GRAPHICS-WINDOW
350
154
710
535
17
17
10.0
1
12
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

SLIDER
10
10
341
43
number-of-leaves
number-of-leaves
1
2500
278
1
1
NIL
HORIZONTAL

BUTTON
122
83
188
116
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

SLIDER
349
79
554
112
wind-factor
wind-factor
0
10
3
1
1
NIL
HORIZONTAL

BUTTON
191
83
254
116
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
349
10
556
43
temperature
temperature
0
40
10
1
1
degrees C
HORIZONTAL

SLIDER
349
44
555
77
rain-intensity
rain-intensity
0
30
19
1
1
NIL
HORIZONTAL

SLIDER
349
115
555
148
sun-intensity
sun-intensity
0
100
97
1
1
%
HORIZONTAL

PLOT
13
122
344
252
Leaves
Time
NIL
0.0
10.0
0.0
100.0
true
true
"" ""
PENS
"leaves" 1.0 0 -10899396 true "" "plot count leaves"
"dead leaves" 1.0 0 -6459832 true "" "plot count dead-leaves"

PLOT
13
259
345
379
Weather conditions
Time
NIL
0.0
10.0
0.0
10.0
true
true
"" ""
PENS
"temperature" 1.0 0 -2674135 true "" "plot temperature"
"rain" 1.0 0 -13345367 true "" "plot rain-intensity"
"wind" 1.0 0 -16777216 true "" "plot wind-factor"
"sunlight" 1.0 0 -1184463 true "" "plot sun-intensity"

PLOT
13
378
344
509
Leaf averages
Time
NIL
0.0
10.0
0.0
10.0
true
true
"" ""
PENS
"chlorophyll" 1.0 0 -10899396 true "" "if any? leaves [ plot mean [chlorophyll] of leaves ]"
"water" 1.0 0 -13345367 true "" "if any? leaves [ plot mean [water-level] of leaves ]"
"sugar" 1.0 0 -7500403 true "" "if any? leaves [  plot mean [sugar-level] of leaves ]"
"carotene" 1.0 0 -1184463 true "" "if any? leaves [  plot mean [carotene] of leaves ]"
"anthocyanin" 1.0 0 -2674135 true "" "if any? leaves [ plot mean [anthocyanin] of leaves ]"
"attachedness" 1.0 0 -16777216 true "" "if any? leaves [  plot mean [attachedness] of leaves ]"

SLIDER
10
46
187
79
start-sugar-mean
start-sugar-mean
0
100
50
1
1
NIL
HORIZONTAL

SLIDER
191
45
341
78
start-sugar-stddev
start-sugar-stddev
0
50
25
1
1
NIL
HORIZONTAL

CHOOSER
560
10
704
55
leaf-display-mode
leaf-display-mode
"solid" "chlorophyll" "water" "sugar" "carotene" "anthocyanin" "attachedness"
6

@#$#@#$#@
## WHAT IS IT?

If you live in a climate that is warm during the summer and cold in the winter, then you are probably familiar with the beautiful autumn phenomenon in which leaves turn color before dying and falling off of the tree.  This model simulates the ways in which leaves change their colors and fall, making it possible to explore and understand this beautiful annual spectacle.

## HOW IT WORKS

Why and how leaves change colors and fall is surprisingly complicated, and has to do with a combination of sunlight, heat, and rain.  (Leaves can be blown off by strong winds even if they have not yet changed color, so wind has a role too.)

The colors that we see in each leaf stem from the presence of natural substances that are produced and stored in each leaf. Three substances contribute to a leaf's color:

- Green comes from chlorophyll (or a set of related substances known as chlorophylls), which converts sunlight and water into sugar.  Chlorophyll molecules are destroyed and not replenished when they are exposed to excessive sunlight and when temperatures are low.  Thus, cold sunny fall days make the overall concentration of chlorophyll decrease.  Overall chlorophyll concentration rises again in the sunlight (as long as there is not too much!) and when there is water.

- Yellow comes from a substance called carotene.  Carotene molecules help give color to carrots and sweet potatoes.  The concentration of carotene remains constant throughout a leaf's life.  However, the yellow color is often masked by the chlorophyll's green color.  A leaf with lots more chlorophyll (typical in the summer) will be exclusively green, albeit with strong yellow tints masked behind the green.  As the chlorophyll dies, however, the presence of carotene becomes apparent, resulting in a yellow leaf.

- Red comes from a substance called anthocyanins.  Anthocyanin molecules are created in the presence of high sugar concentrations and water concentrations in the leaf.  (The higher the concentration of sugar, the more anthocyanins are produced.)  Sugar concentration increases when cold weather causes the tree to shut down its water circulation to the rest of the tree; whatever water and sugar are trapped in the leaf are then converted into anthocyanins.

Each tick of the clock in the model consists of two stages: (1) Weather (rain, wind, sun) affects the leaves, adding or removing sugar, water, or chlorophyll as appropriate, and (2) the leaf reacts to its environment, adding anthocyanins as appropriate, and changing color to reflect the modified environment.

Water does not enter each leaf directly, but is absorbed by the tree's roots, from which it is pulled up the trunk and into the branches and leaves.  In this model, the entire ground is assumed to contain tree roots, and thus all raindrops flow toward the trunk once they reach the ground.  Similarly, all of the raindrops travel up the trunk of the tree, and then along the branches (which are not represented in the model), out to the leaves.  Leaves collect water from nearby raindrops. Raindrops disappear when they have no more water left in them.

Leaves in the model have an "attachedness" attribute, which the model uses to indicate how strongly the leaf is clinging to the tree.  Attachedness rises with water, and declines in wind and rain.  (On a very windy day, leaves may blow off even if they're completely green.)

Because the NetLogo color space does not include all possible colors, this model uses a threshold algorithm to determine the color of the leaf, as an approximation of the real color.  Whenever chlorophyll is above 50%, the leaf is green.  Below that, the leaf is given a yellow, red, or orange color, depending on if there is a majority of carotene, anthocyanins, or both, respectively.

Note that the intensity of the leaf colors varies with the level of chlorophyll, carotene, and/or anthocyanins.  So a chlorophyll-laden tree will have dark-green leaves, whereas one with only a little bit will have light-green leaves.

## HOW TO USE IT

To use the model, press SETUP.  This will create the tree trunk and branches, and will also create the leaves of the tree, which are the main agents in this model.  Press GO to set the system in motion.

Now comes the interesting and/or tricky part, namely adjusting the sliders under the view so that the weather produces conditions you want to explore or study.  If the leaves appear to be losing chlorophyll due to a lack of water (which you can monitor in the "Leaf averages" plot), you can make it rain by adjusting the RAIN-INTENSITY slider. To make the wind blow, adjust the WIND-FACTOR slider.

The sun's strength (in terms of intensity of sunlight) is set with the SUN-INTENSITY slider.  As noted above, leaves need sunlight to produce chlorophyll and sugar -- but too much sunlight may begin to destroy these chemicals.

Finally, you can change the TEMPERATURE slider.  If the temperature is too cold, the chlorophyll molecules will be destroyed, setting in motion the creation of anthocyanins.

Once you have mastered the basics of the model, you might want to consider setting the two sliders in the top left corner, START-SUGAR-MEAN and START-SUGAR-STDDEV, which influence the mean and spread of the initial distribution of sugar among the leaves.  Maple leaves, for example, tend to have a lot of sugar in them to begin with, which means that they'll turn redder than other leaves under similar conditions.

The LEAF-DISPLAY-MODE in the lower right corner changes the way in which leaves are depicted.  Normally, each leaf is painted as a solid NetLogo "default" turtle wedge.  This is the case when LEAF-DISPLAY-MODE is set to "solid," the default.  Selecting a different value for LEAF-DISPLAY-MODE then changes each leaf to show an empty, half-full, or full shape, according to the variable that was chosen.  Thus if LEAF-DISPLAY-MODE is set to "water", each leaf will be shown as empty (if low on water), half-full (if somewhat stocked with water), or full (if relatively full of water).

## THINGS TO NOTICE

Leaves absorb water when they are hit by raindrops -- which means that with light rain and many leaves, the "inside" leaves will fail to get enough rain.

The stronger the wind, the more each leaf shakes.

You can simulate a minor hurricane by turning the wind and rain up to their maximum values.  Watch the leaves all fall off at once!

## THINGS TO TRY

Can you get all of the leaves to turn red before falling?  How about to turn orange before falling?  How about yellow?  Now try to mix things up, such that the same tree will have leaves of many different colors.  (Hint: You will probably need to adjust the sun, rain, and temperature sliders several times to get the combination to look right.)

Try to get the leaves to turn yellow and then green again.

In some climates trees do not lose their leaves.  Can you adjust the climate conditions so that you can make the tree keep some of its leaves?

## EXTENDING THE MODEL

In real life, leaves that face the sun tend to be yellower than those that don't.  Change the model such that the sun can be positioned (and moved), and such that leaves facing the sun are more sensitive to sunlight than those that don't.

The model is very simplistic in its approach to water and sugar -- namely, that all water comes from rain, that raindrops can affect multiple leaves, and that raindrops disappear as they hit the ground.  A more realistic model would have raindrops disappear as soon as they hit a leaf, but would also allow for water to travel from the ground to the leaves, via the trunk and branches.  Also a more realistic model would model the transport of sugars between leaves and the storage of excess sugar in the roots.

Allow for the budding and growth of new leaves, in addition to the death of mature ones.

Add day-night cycles, in which the temperature drops and the sun goes down for several ticks of the clock.  In the dark, the tree would then consume sugars and produce water through respiration.

## NETLOGO FEATURES

Because NetLogo's color space is incomplete, we needed to fake color pigmentation blending a bit.  Notice how we handle setting colors by means of thresholds.  This means that there can be sudden, jarring color changes if the weather conditions are a bit extreme.

Also note that the NetLogo color scheme, and the `scale-color` primitive, produce colors that range from white to black.  Because in this model we wants to vary hues without getting too close to either black or white, we used the `scale-color` primitive but on a -50 to 150 scale, rather than the usual 0 to 100.

When a leaf dies, its breed changes to `dead-leaves`.  This keeps it in the system, but allows us to issue instructions to only those leaves that are alive.

Notice how the sun's color changes as SUN-INTENSITY changes.  What happens to the label when the sun becomes dark and small?

## RELATED MODELS

Plant Growth is in some ways a model of the opposite process, namely how do leaves grow, as opposed to how do leaves die.

## CREDITS AND REFERENCES

- http://scifun.chem.wisc.edu/chemweek/fallcolr/fallcolr.html
- http://www.the-scientist.com/article/display/12772/

Thanks to Reuven Lerner for his work on this model.
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

1
true
0
Polygon -7500403 true true 150 7 150 204 203 130
Line -7500403 true 150 7 42 248
Line -7500403 true 41 250 149 204
Line -7500403 true 149 204 259 248
Line -7500403 true 150 8 260 249
Polygon -7500403 true true 150 8 150 205 42 252

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

default empty
true
0
Line -7500403 true 150 7 42 248
Line -7500403 true 41 250 149 204
Line -7500403 true 149 204 259 248
Line -7500403 true 150 8 260 249

default half
true
0
Line -7500403 true 150 7 42 248
Line -7500403 true 41 250 149 204
Line -7500403 true 149 204 259 248
Line -7500403 true 150 8 260 249
Polygon -7500403 true true 150 7 150 204 42 251

default one-quarter
true
0
Line -7500403 true 150 7 42 248
Line -7500403 true 41 250 149 204
Line -7500403 true 149 204 259 248
Line -7500403 true 150 8 260 249
Polygon -7500403 true true 91 133 151 203 42 252

default three-quarter
true
0
Line -7500403 true 150 7 42 248
Line -7500403 true 41 250 149 204
Line -7500403 true 149 204 259 248
Line -7500403 true 150 8 260 249
Polygon -7500403 true true 150 7 150 204 211 140
Polygon -7500403 true true 150 7 150 204 42 251

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
setup
repeat 30 [ go ]
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

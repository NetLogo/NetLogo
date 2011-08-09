globals
[
  ;; used to keep score
  mate-leader
  mates-found
  predator-leader
  prey-found
  total-mates-found
  total-prey-found

  ;; used for different aspects of the appearance
  wander-angle
  old-show-age?

  ;; used to mutate the colors of the fish
  min-size
  max-size
  chance-mutate-color
  chance-mutate-size
  max-fish-size
  max-size-mutation-step
  max-color-mutation-step
]

breed [ fish a-fish ]
breed [ fish-parts fish-part ]
breed [ rocks rock ]
breed [ players player ]

players-own [user-name found attempts role]

;; gene-frequencies are used to determine the color of the fish
;; (or fish parts). When fish reproduce the gene-frequencies
;; will mutate making a slightly different color
fish-own [
    red-pigment-gene-frequency
    blue-pigment-gene-frequency
    green-pigment-gene-frequency
    age      ;; fish cannot reproduce until they have reached MIN-AGE-REPRODUCTION
    reproduce?
    eaten?
    my-parts ;; agentset including tail and fins for easy access
  ]

fish-parts-own [
  owned-by    ;; parent fish
  body-part   ;; which part of the body this turtle displays (tail, top or bottom fins)
  red-pigment-gene-frequency
  blue-pigment-gene-frequency
  green-pigment-gene-frequency
]


;;;;;;;;;;;;;;;;;;;
;; Setup Procedures
;;;;;;;;;;;;;;;;;;;
to startup
  hubnet-reset
  init-globals
  setup-environment
  make-initial-fish
end

;; set constants once at the very beginning.
to init-globals
  set wander-angle 40
  set chance-mutate-color 20
  set chance-mutate-size 20
  set max-fish-size 3
  set min-size 1
  set max-size 5
  set max-size-mutation-step 1
  set max-color-mutation-step 50
end

;; reset the fish, predators, and mates
;; to their initial conditions to start
;; the simulation over
to setup
  reset-ticks
  ask fish [ die ]
  ask fish-parts [ die ]
  ask rocks  [ die ]
  clear-all-plots
  init-globals
  setup-environment
  make-initial-fish
  ask players
  [
    init-player-variables
    update-player
  ]
end

to setup-environment
  ask patches [ set pcolor background-color ]
  if (rock-shelters?)
  [
    create-rocks 1 [
      set size world-width
      set color (brown - 3)
      set shape "rocks"
    ]
  ]
 import-drawing background
end

;; each fish consists of several turtles, one for the body
;; one for the tail, and one for each of the fins
;; so the colors can vary separately
to make-initial-fish
  create-fish carrying-capacity [
    set shape "fish-body"
    assign-initial-body-genotype-and-phenotype
    setxy random-xcor random-ycor
    set size (max-fish-size / 2)
    assign-initial-fish-parts
    set age 0
    set eaten? false
    set reproduce? false
    if show-age?
    [
      set label-color red
      set label (word age "      ")
    ]
  ]
end

to assign-initial-body-genotype-and-phenotype
   set red-pigment-gene-frequency 150
   set blue-pigment-gene-frequency 150
   set green-pigment-gene-frequency 150
   set-phenotype-color
end

;; make the fish parts and
;; use the my-part turtle variable to
;; have a quick reference to the turtles
;; that make up the tail and fins.
to assign-initial-fish-parts
  hatch-fish-part "top"
  hatch-fish-part "bottom"
  hatch-fish-part "tail"
  set my-parts fish-parts with [ owned-by = myself ]
end

to hatch-fish-part [name]
  hatch-fish-parts 1 [
    set body-part name
    set owned-by myself
    set shape word "fish-" name
    assign-initial-part-genotype-and-phenotype
  ]
end

to assign-initial-part-genotype-and-phenotype
  set red-pigment-gene-frequency (red-pigment-gene-frequency  + random-float 20)
  set green-pigment-gene-frequency (green-pigment-gene-frequency  + random-float 20)
  set blue-pigment-gene-frequency (blue-pigment-gene-frequency  + random-float 20)
  set-phenotype-color
end

;; convert the genetic representation of gene frequency
;; into a phenotype using the rgb primitive
to set-phenotype-color  ;; turtle procedure
  set color rgb red-pigment-gene-frequency
                green-pigment-gene-frequency
                blue-pigment-gene-frequency
end

to toggle-labels
  ;; only change the labels when the switch is
  ;; changed so they don't flicker and slow the
  ;; model down.
  if old-show-age? != show-age?
  [
    ask fish [
      ifelse show-age?
      [
        set label-color red
        set label (word age "      ") ;; add spaces at the end to the label is shifted to the left a bit
      ]
      [
        set label ""
      ]
    ]
    set old-show-age? show-age?
  ]
end

;; the rock shelter is used to hide newborn
;; fish so they don't clump up too much.
to make-rock-shelter
  ;; keep the existence of the rock shelters in
  ;; sync with the switch.
  if rock-shelters? and count rocks = 0
  [
    if (rock-shelters?)
     [
       create-rocks 1 [
         set size world-width
         set color ( brown - 3)
         set shape "rocks"
       ]
     ]
  ]
  if not rock-shelters? and count rocks > 0
  [ ask rocks [ die ] ]
end

;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures
;;;;;;;;;;;;;;;;;;;;;

to go
  ask fish [ set age age + 1 ]
  ask fish [ move-fish ]
  ask fish [ update-fish ]

  ;; keep the world in a state consistent
  ;; with the model settings
  toggle-labels
  make-rock-shelter
  enforce-capacity

  every 0.01 [ listen-clients ]
  tick
  update-plots
end

to move-fish ;; fish procedure
  let move-right random wander-angle
  let move-left  random wander-angle
  let move-forward 0

  ;; fish-speed-scale slows down or speeds up fish
  ;; to make the game more playable without slowing
  ;; down the entire model.
  ifelse (count fish <= 50)
  [ set move-forward fish-speed-scale * 0.001 * (count fish) ]
  [ set move-forward fish-speed-scale * 0.001 * 50 ]

  ;; move myself and my fins and tail.
  ask my-parts
  [
    right move-right
    left move-left
    fd move-forward
  ]
  right move-right
  left move-left
  fd move-forward
end

to update-fish ;; fish procedure
  ;; remove fish that are tagged to die
  ;; and all associated parts
  if eaten?
  [
    ask my-parts [ die ]
    die
  ]

 ;; reproduce fish that are tagged to reproduce
 if reproduce?
 [
    make-one-offspring
    set age 0
    set reproduce? false
 ]
end

to enforce-capacity
 ;; keeps the world population
 ;; constant so there is no population
 ;; boom or bust.
 if enforce-capacity? [
   if count fish > 0
   [
     while [ count fish < carrying-capacity ]
       [ reproduce-fish ]
     while [ count fish with [ eaten? = false ]  > carrying-capacity ]
       [ eat-one-random-fish ]
   ]
 ]
end

to eat-one-random-fish
  ask one-of fish [ set eaten? true ]
end

to reproduce-fish
  ask one-of fish [
    make-one-offspring
    set age 0
  ]
end

to make-one-offspring ;; fish procedure
  set age 0
  hatch 1
  [
    set reproduce? false
    make-offspring-parts myself
  ]
end

to make-offspring-parts [parent] ;; fish procedure
  let baby-xcor random-pxcor
  let baby-ycor (1 + min-pycor)

  if rock-shelters? [
    setxy baby-xcor baby-ycor
    set heading 0
  ]

  ;; keep track of the fish that is hatching the parts
  ;; because we get double perspective change below and
  ;; we can't get back to the parent fish.
  let owner self

  ask [my-parts] of parent  [
    hatch 1 [
     set owned-by owner
     set red-pigment-gene-frequency color-mutation red-pigment-gene-frequency
     set green-pigment-gene-frequency color-mutation green-pigment-gene-frequency
     set blue-pigment-gene-frequency color-mutation blue-pigment-gene-frequency
     set-phenotype-color
     size-mutation
     if rock-shelters?
     [
       setxy baby-xcor baby-ycor
       set heading 0
     ]
    ]
   ]

   set my-parts fish-parts with [ owned-by = myself ]
end

;; everytime a predator or mate catches a fish update
;; his found variable and the totals for that role
to update-found-stats
  ask players with [ user-name = hubnet-message-source ]
  [
    set found found + 1
    if role = "predator"
    [ set total-prey-found total-prey-found + 1 ]
    if role = "mate"
    [ set total-mates-found total-mates-found + 1 ]
  ]
end

;; update the stats for the leaders in the game
to update-mate-leaders
  let mates players with [ role = "mate" ]
  let leaders mates with-max [ found ]

  ifelse count leaders > 1
  [ set mate-leader (word count leaders "-way tie") ]
  [ set mate-leader [user-name] of one-of leaders ]
  set mates-found [found] of one-of leaders
end

to update-predator-leaders
  let predators players with [ role = "predator" ]
  let leaders predators with-max [ found ]

  ifelse count leaders > 1
  [ set predator-leader (word count leaders "-way tie") ]
  [ set predator-leader [user-name] of one-of leaders ]
  set prey-found [found] of one-of leaders
end

;; extract the coordinates of the mouse event
;; on client, find any fish on that location
;; and mark them for reproduction or eating
to select-fish
  let owner nobody
  let clicked-xcor  (round item 0 hubnet-message)
  let clicked-ycor  (round item 1 hubnet-message)
  let this-players-role ""

  let fishies-here fish-on patch clicked-xcor clicked-ycor

  ask players with [user-name = hubnet-message-source]
  [
    set attempts attempts + 1
    if any? fishies-here [
      update-found-stats
      ;; predators or mates only get one of the fish here
      ;; each time they click.
      ask one-of fishies-here [
        ifelse [role] of myself = "mate"
        [ if age > min-age-reproduction
          [ set reproduce? true ] ]
        [ set eaten? true ]
      ]
      update-leader-stats

      ;; every time any player finds a fish update the
      ;; monitors on all the players with the same
      ;; role, so the leader stats don't get out of date
      ;; if they don't click for awhile
      ask players with [ role = [role] of myself ]
        [ update-player ]
    ]
  ]
end

;; mutate the size, don't let the size go outside
;; the range min-size - max-size
to size-mutation
  if random 100 < chance-mutate-size
  [
    set size size + random-float max-size-mutation-step
    if size < min-size
      [ set size min-size ]
    if size > max-size
      [ set size max-size ]
  ]
end

;; mutate the color but don't let it mutate
;; outside the random 1 - 10
to-report color-mutation [gene-frequency]
  let mutation (random (max-color-mutation-step * 2)) - max-color-mutation-step
  if random 100 < chance-mutate-color [
    set gene-frequency gene-frequency + mutation
  ]
  if gene-frequency < 0
    [ set gene-frequency 0 ]
  if gene-frequency > 255
    [ set gene-frequency 255 ]
   report gene-frequency
end

to update-leader-stats
  if any? players with [role = "mate"]
  [ update-mate-leaders ]
  if any? players with [role = "predator"]
  [ update-predator-leaders ]
end

;;
;; HubNet Procedures
;;
to listen-clients
  while [hubnet-message-waiting?]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ add-player ]
    [
      ifelse hubnet-exit-message?
      [ remove-player ]
      [
        if hubnet-message-tag = "View"
        [ select-fish ]
      ]
    ]
  ]
end

;; report name of the leader of a players group
to-report my-leader ;; player procedure
  ifelse role = "mate"
  [ report mate-leader ]
  [ report predator-leader ]
end

to-report my-leaders-score ;; player procedure
  report max [ found ] of players with [ role = [role] of myself ]
end

;; update all the monitors that change on the clients
to update-player ;; player procedure
  hubnet-send user-name "Your role" role
  let max-found my-leaders-score
  hubnet-send user-name "Your leader" my-leader
  hubnet-send user-name "Leader found" max-found
  hubnet-send user-name "You found" found
  ifelse max-found = 0
  [ hubnet-send user-name "Success %"  100  ]
  [ hubnet-send user-name "Success %"  precision ((found / max-found) * 100)  2 ]
end

to add-player
  create-players 1
  [
    set user-name hubnet-message-source
    init-player-variables
    hubnet-send user-name "Your name" user-name
    update-player
  ]
end

;; initialize one player
to init-player-variables ;; player procedure
  set attempts 0
  set found 0
  if player-roles = "all predators"
  [ set role "predator" ]
  if player-roles = "all mates"
  [ set role "mate" ]
  if player-roles = "predators v. mates" [
    ifelse random 2 = 0
    [ set role "predator" ]
    [ set role "mate" ]
  ]
  set hidden? true
end

to remove-player
  ask players with [user-name = hubnet-message-source ]
    [ die ]
end
@#$#@#$#@
GRAPHICS-WINDOW
274
35
690
472
14
14
14.0
1
10
1
1
1
0
1
1
1
-14
14
-14
14
1
1
1
ticks
30.0

BUTTON
39
23
142
56
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
144
23
247
56
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
54
156
235
189
carrying-capacity
carrying-capacity
0
50
14
1
1
fish
HORIZONTAL

BUTTON
141
312
261
345
clear background
clear-drawing
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
50
78
227
111
fish-speed-scale
fish-speed-scale
0
10
7.8
0.2
1
NIL
HORIZONTAL

SLIDER
49
354
223
387
background-color
background-color
93
98
97
0.1
1
NIL
HORIZONTAL

MONITOR
780
66
870
111
fish
count fish
3
1
11

SLIDER
54
190
235
223
min-age-reproduction
min-age-reproduction
0
1000
200
1
1
NIL
HORIZONTAL

SWITCH
68
122
217
155
enforce-capacity?
enforce-capacity?
0
1
-1000

SWITCH
64
393
210
426
rock-shelters?
rock-shelters?
1
1
-1000

SWITCH
71
225
220
258
show-age?
show-age?
1
1
-1000

CHOOSER
27
288
134
333
background
background
"aquarium.jpg" "underwater.jpg"
0

BUTTON
141
276
261
310
change background
import-drawing background
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

CHOOSER
747
17
901
62
player-roles
player-roles
"all predators" "all mates" "predators v. mates"
1

MONITOR
722
118
812
163
 mates
count players with [role = \"mate\"]
0
1
11

MONITOR
842
118
932
163
predators
count players with [role = \"predator\"]
0
1
11

MONITOR
704
171
827
216
NIL
mate-leader
0
1
11

MONITOR
704
218
827
263
NIL
mates-found
0
1
11

MONITOR
832
171
951
216
NIL
predator-leader
0
1
11

MONITOR
832
218
951
263
NIL
prey-found
0
1
11

MONITOR
704
265
827
310
NIL
total-mates-found
3
1
11

MONITOR
832
265
951
310
NIL
total-prey-found
3
1
11

PLOT
701
320
955
479
Found v. Time
Time
Found
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"prey" 1.0 0 -16777216 true "" "plot  total-prey-found"
"mates" 1.0 0 -8630108 true "" "plot  total-mates-found"

@#$#@#$#@
## WHAT IS IT?

This selection model shows how sexual attraction and predation change the coloration and patterns in guppy's population.

If you have not seen guppies you can see some at the "Sex and the Single Guppy" webpage:  
http://www.pbs.org/wgbh/evolution/sex/guppy/low_bandwidth.html

When you run the model, you can either play the role of a predator or the role of a mate.

As a predator, you will probably notice the more brightly colored guppy males.  In other words, the more colored the guppy is, the more likely he will be seen by you, the predator. In this model, (as happens in the wild in streams where predators are plentiful), male guppies become increasingly drab over generations, pushed by predation pressure toward greater camouflage.

You can notice that many guppies are very colorful, have garish patterns and large tails, even if it makes them more noticeable to predators. You might ask yourself: Why doesn't a guppy remain camouflaged and discreet in order to avoid the detection by a predator?

The answer lies in the fact that guppies are driven by more than only a survival instinct. Guppies also desire to reproduce with other guppies and to do this they must be noticed by their mates. The "flashier" a male guppy is, the likelier a female guppy will choose him as a mate, passing his genes to the next generation. This is sexual selection at work, and it is the force that drives guppy's coloration toward conspicuousness just as hard as predation pushes coloration toward drabness.

Thus as a mate, you will again probably notice the more brightly colored guppy males.  
When you click on a colorful mate, he will hatch an offspring, which will likely create another colorful guppy and guppies will increasingly become colorful over generations, pushed by breeding pressure.

Quoting from "Sex and the Single Guppy"  [2]:  
There may be several evolutionary reasons why female guppies prefer flashy males. On the most basic level, the male with the biggest, brightest tail spot announces most loudly, "Hey, I'm over here" to any female it can see. Flashy colors are simply easier to locate.  However, there is also research to suggest that bright colors serve as an indicator of good genes in the way the strong physique of a human athlete is a direct indicator of that individual's health and vitality.  Or, bright coloration may signal to a potential mate that he's got something else going for him. After all, he's been able to survive the very handicap -- conspicuousness to predators -- that his flashiness creates.

Whatever the reasons, it is clear from the research of Endler and other evolutionary biologists that male guppies live in the crossfire between their enemies and their would-be mates, with the opposing forces of predation and sexual selection forever pushing the guppy coloration in opposite directions.

## HOW IT WORKS

You can assume either the role of a predator or the role of a mate.

When GO is pressed, if you are a predator you should try to click on the guppies, as fast as you can, in order to eat them.  Each time you click on a guppy it will be removed from the guppy population.  At that point, another guppy in the population will hatch an offspring to replace the one that was caught (keeping the population of guppies constant).

If you are a mate (a female guppy), you should try to click on the guppies as fast as you can (they are all males).  When you click on a guppy that is old enough to mate, he will hatch an offspring.  When the population of guppies exceeds the capacity, a random guppy will be removed.

Each new guppy may undergo small mutations in its genetics for each of its three fins.  These mutations will results in changes in the size and the three pigments that make up the color of each fin.

Predators prey on the most brightly colored or patterned individuals more often than the less colored ones since they are easier to spot and eliminate them from the gene pool. Thus, predators cause guppy populations to remain relatively drab (with respect to colors and patterns of the environment they live in).

However, guppies looking for a mate exert the opposite selection.  Relatively drab guppies are hard to find and mate with, while guppies with garish colors and patterns are easier to find and mate with.  As these guppies reproduce, the frequency of their genes increases in the gene pool.

Guppy populations are evolving to match, and/or stand out, from their environment depending on which of the selective pressures are stronger.

## HOW TO USE IT

To run the activity press the GO button.  To start the activity over with the same group of students stop the GO button by pressing it again, press the SETUP button, and press GO again.  To run the activity with a new group of students press the RESET button in the Control Center.

Buttons:  
SETUP - Clears the world and populates the world with fish. All players are set to initial values.  
GO - Runs the simulation, students can login and start eating, or mating with the fish population.  
CHANGE BACKGROUND - loads the image selected in the BACKGROUND chooser into the drawing.  
CLEAR BACKGROUND - erases the drawing so the patches show through.

Sliders:  
FISH-SPEED-SCALE - controls how quickly the fish move around the world  
CARRYING-CAPACITY - the simulation will automatically keep CARRYING-CAPACITY fish in the world at all times.  If there are too many fish it will randomly kill some, however, if there are too few fish, a random fish already will automatically be reproduced.  Note that CARRYING-CAPACITY will only be active when the ENFORCE-CAPACITY? switch is in the "on" position.  
MIN-AGE-REPRODUCTION - The minimum amount of time before a fish can reproduce after it is born and since the last time it reproduced.  
BACKGROUND-COLOR - the value of the color of the background (patches), which is only visible when there is no image loaded in the drawing.

Switches:  
ENFORCE-CAPACITY? - When it is on, the simulation automatically maintains number of fish in the world at CARRYING-CAPACITY.  
SHOW-AGE? - When it is on, set the label of each fish to its age.  
ROCK-SHELTERS? - When it is on, a rock shelter is placed on top of the world, as new fish are born they emerge from the rocks rather than appearing where its parent was at the time of reproduction.

Monitors:  
FISH - The number of fish in the world.  
MATES - The number of students logged in as mates.  
PREDATORS - The number of students logged in as predators.  
MATES-LEADER - The name of the student (or indication of a tie) with the highest number of mates found.  
PREDATOR-LEADER - The name of the student (or indication of a tie) with the highest number of prey found.  
MATES-FOUND - The number of mates found by the leader.  
PREY-FOUND - The number of prey found by the leader.  
TOTAL-MATES-FOUND - The number of mates found by all mates.  
TOTAL-PREY-FOUND - The number of prey found by all predators.

Choosers:  
BACKGROUND - Select the name of the background image to use in the world.  
PLAYER-ROLES - Select the type of game to play so you can explore the affects of the two forces (mates and predators) separately and in competition with each other.

Plots:  
FOUND V TIME - The number of fish found by both predators and mates over time.

Global variables in the procedures to change:  
WANDER-ANGLE - The amount that the fish will wiggle when they move around the world.  
MAX-COLOR-MUTATION-STEP - the maximum amount that a color gene can change by in one step  
CHANCE-MUTATE-COLOR - The percent chance the fish parts will slightly change color when reproduced  
CHANCE-MUTATE-SIZE - The percent chance the fish parts will slightly change size when reproduced  
MAX-FISH-SIZE - A limit on the size fish can grow to, to keep the simulation reasonable  
MAX-SIZE-MUTATION-STEP - the maximum amount that a size gene can change by in one step

## THINGS TO TRY

Select ALL MATES in the PLAYER-ROLES chooser, run the activity notice the results, Are the fish very colorful or are they drab?

Do the same with ALL PREDATORS and MATES V PREDATORS, which force wins out in the end?

Try different backgrounds to see if the drab color of the guppies becomes closer to the common objects in the background (backlit seawater, rocky bottoms, green plants, etc.)

Try to run the model without using a backdrop, instead adjust the color of the patches using the BACKGROUND-COLOR slider.

## EXTENDING THE MODEL

It can sometimes be difficult to click on the fish because catching fish is dependent on patch boundaries, change it so it uses in-radius instead.

## NETLOGO FEATURES

This model uses `import-drawing` to load high resolution backdrops into the drawing layer. However, the fish do not directly interact with the background; it only affects how the users see the world.

## RELATED MODELS

Bug Hunt Pursuit  
Peppered Moths

## CREDITS AND REFERENCES

[1] Inspired by Sex and the Single Guppy http://www.pbs.org/wgbh/evolution/sex/guppy/low_bandwidth.html  
[2] Sex and the Single Guppy. Conclusion: Exhibitionism Explained http://www.pbs.org/wgbh/evolution/sex/guppy/conclusion.html
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
Polygon -7500403 true true 150 135 150 180 240 210 300 150 240 90 150 120
Polygon -7500403 true true 30 150 0 90 150 150 0 210
Circle -1 true false 219 114 42
Circle -16777216 true false 225 119 32
Polygon -7500403 true true 150 150 90 210 90 300 150 240
Polygon -7500403 true true 150 150 90 90 90 0 150 60

fish-body
false
0
Polygon -7500403 true true 150 135 150 180 240 210 300 150 240 90 150 120

fish-bottom
false
0
Polygon -7500403 true true 150 150 90 210 90 300 150 240

fish-tail
false
0
Polygon -7500403 true true 30 150 0 90 150 150 0 210

fish-top
false
0
Polygon -7500403 true true 150 150 90 90 90 0 150 60

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

hawk
true
0
Polygon -7500403 true true 166 230 165 255 138 289 158 304 171 304 194 289 165 255
Polygon -16777216 true false 166 222 165 247 143 285 155 295 176 294 189 285 165 247
Polygon -7500403 true true 175 164 174 151 164 144 145 143 154 152 148 171 142 203 150 240 154 262 165 296 175 264 180 240 184 201
Polygon -7500403 true true 153 175 224 159 285 179 271 183 293 199 275 200 286 213 269 213 277 223 263 220 266 227 227 232 152 229
Circle -16777216 true false 159 145 7
Polygon -16777216 true false 144 143 150 148 154 144
Polygon -7500403 true true 168 171 94 158 35 177 51 182 27 197 45 198 34 211 51 211 43 221 57 218 54 225 93 230 168 227
Polygon -7500403 true true 165 214 164 239 142 277 154 287 175 286 188 277 164 239
Polygon -16777216 true false 171 251 164 246 143 270 155 280 176 279 189 270 161 251
Polygon -7500403 true true 165 201 164 226 149 261 154 274 175 273 181 260 164 226
Polygon -16777216 false false 145 142 154 152 149 168 92 158 33 176 49 182 25 196 45 197 33 211 52 211 42 221 57 218 52 225 93 231 147 228 150 240 150 252 149 267 143 271 145 272 141 276 145 281 137 289 150 300 177 300 194 289 184 279 189 275 185 274 188 271 179 262 181 258 176 253 181 240 181 230 228 233 267 227 261 219 277 222 267 213 286 213 275 200 292 198 271 182 285 178 226 159 175 170 174 151 164 144

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

moth
false
0
Rectangle -7500403 false true 0 0 300 300
Circle -1 true false 225 15 60
Circle -16777216 true false 240 30 30

moth-black
true
0
Polygon -16777216 true false 150 165 209 199 225 225 225 255 195 270 165 255 150 240
Polygon -16777216 true false 150 165 89 198 75 225 75 255 105 270 135 255 150 240
Polygon -16777216 true false 139 148 100 105 55 90 25 90 10 105 10 135 25 180 40 195 85 194 139 163
Polygon -16777216 true false 162 150 200 105 245 90 275 90 290 105 290 135 275 180 260 195 215 195 162 165
Polygon -16777216 true false 150 255 135 225 120 150 135 120 150 105 165 120 180 150 165 225
Circle -16777216 true false 135 90 30
Line -16777216 false 150 105 195 60
Line -16777216 false 150 105 105 60

moth-white
true
0
Polygon -1 true false 150 165 209 199 225 225 225 255 195 270 165 255 150 240
Polygon -1 true false 150 165 89 198 75 225 75 255 105 270 135 255 150 240
Polygon -1 true false 139 148 100 105 55 90 25 90 10 105 10 135 25 180 40 195 85 194 139 163
Polygon -1 true false 162 150 200 105 245 90 275 90 290 105 290 135 275 180 260 195 215 195 162 165
Polygon -1 true false 150 255 135 225 120 150 135 120 150 105 165 120 180 150 165 225
Circle -1 true false 135 90 30
Line -1 false 150 105 195 60
Line -1 false 150 105 105 60

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

rocks
true
0
Polygon -7500403 true true 0 300 0 0 15 0 18 38 19 84 28 105 24 123 22 134 16 171 28 201 41 219 35 236 54 251 67 245 90 251 109 247 137 248 150 255 152 301
Polygon -7500403 true true 149 255 179 266 197 249 210 254 226 238 243 246 249 247 265 254 269 242 270 230 252 203 261 185 270 168 277 150 287 113 282 97 286 71 282 -1 304 -8 299 300 151 300

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
NetLogo 5.0beta5
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
VIEW
150
10
556
416
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
-14
14
-14
14

MONITOR
13
265
139
314
You found
NIL
0
1

MONITOR
13
316
139
365
Success %
NIL
1
1

MONITOR
13
163
139
212
Your leader
NIL
3
1

MONITOR
13
214
139
263
Leader found
NIL
0
1

MONITOR
13
61
139
110
Your name
NIL
0
1

MONITOR
13
112
139
161
Your role
NIL
3
1

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

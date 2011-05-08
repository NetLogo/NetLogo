globals [
  infection-chance  ;; The chance out of 100 that an infected person will pass on
                    ;;   infection during one week of couplehood.
  symptoms-show     ;; How long a person will be infected before symptoms occur
                    ;;   which may cause the person to get tested.
  slider-check-1    ;; Temporary variables for slider values, so that if sliders
  slider-check-2    ;;   are changed on the fly, the model will notice and
  slider-check-3    ;;   change people's tendencies appropriately.
  slider-check-4
]

turtles-own [
  infected?          ;; If true, the person is infected.  It may be known or unknown.
  known?             ;; If true, the infection is known (and infected? must also be true).
  infection-length   ;; How long the person has been infected.
  coupled?           ;; If true, the person is in a sexually active couple.
  couple-length      ;; How long the person has been in a couple.
  ;; the next four values are controlled by sliders
  commitment         ;; How long the person will stay in a couple-relationship.
  coupling-tendency  ;; How likely the person is to join a couple.
  condom-use         ;; The percent chance a person uses protection.
  test-frequency     ;; Number of times a person will get tested per year.
  partner            ;; The person that is our current partner in a couple.
]

;;;
;;; SETUP PROCEDURES
;;;

to setup
  clear-all
  setup-globals
  setup-people
  reset-ticks
end

to setup-globals
  set infection-chance 50    ;; if you have unprotected sex with an infected partner,
                             ;; you have a 50% chance of being infected
  set symptoms-show 200.0    ;; symptoms show up 200 weeks after infection
  set slider-check-1 average-commitment
  set slider-check-2 average-coupling-tendency
  set slider-check-3 average-condom-use
  set slider-check-4 average-test-frequency
end

;; Create carrying-capacity number of people half are righty and half are lefty
;;   and some are sick.  Also assigns colors to people with the ASSIGN-COLORS routine.

to setup-people
  crt initial-people
    [ setxy random-xcor random-ycor
      set known? false
      set coupled? false
      set partner nobody
      ifelse random 2 = 0
        [ set shape "person righty" ]
        [ set shape "person lefty" ]
      ;; 2.5% of the people start out infected, but they don't know it
      set infected? (who < initial-people * 0.025)
      if infected?
        [ set infection-length random-float symptoms-show ]
      assign-commitment
      assign-coupling-tendency
      assign-condom-use
      assign-test-frequency
      assign-color ]
end

;; Different people are displayed in 3 different colors depending on health
;; green is not infected
;; blue is infected but doesn't know it
;; red is infected and knows it

to assign-color  ;; turtle procedure
  ifelse not infected?
    [ set color green ]
    [ ifelse known?
      [ set color red ]
      [ set color blue ] ]
end

;; The following four procedures assign core turtle variables.  They use
;; the helper procedure RANDOM-NEAR so that the turtle variables have an
;; approximately "normal" distribution around the average values set by
;; the sliders.

to assign-commitment  ;; turtle procedure
  set commitment random-near average-commitment
end

to assign-coupling-tendency  ;; turtle procedure
  set coupling-tendency random-near average-coupling-tendency
end

to assign-condom-use  ;; turtle procedure
  set condom-use random-near average-condom-use
end

to assign-test-frequency  ;; turtle procedure
  set test-frequency random-near average-test-frequency
end

to-report random-near [center]  ;; turtle procedure
  let result 0
  repeat 40
    [ set result (result + random-float center) ]
  report result / 20
end

;;;
;;; GO PROCEDURES
;;;

to go
  if all? turtles [known?]
    [ stop ]
  check-sliders
  ask turtles
    [ if infected?
        [ set infection-length infection-length + 1 ]
      if coupled?
        [ set couple-length couple-length + 1 ] ]
  ask turtles
    [ if not coupled?
        [ move ] ]
  ;; Righties are always the ones to initiate mating.  This is purely
  ;; arbitrary choice which makes the coding easier.
  ask turtles
    [ if not coupled? and shape = "person righty" and (random-float 10.0 < coupling-tendency)
        [ couple ] ]
  ask turtles [ uncouple ]
  ask turtles [ infect ]
  ask turtles [ test ]
  ask turtles [ assign-color ]
  tick
end

;; Each tick a check is made to see if sliders have been changed.
;; If one has been, the corresponding turtle variable is adjusted

to check-sliders
  if (slider-check-1 != average-commitment)
    [ ask turtles [ assign-commitment ]
      set slider-check-1 average-commitment ]
  if (slider-check-2 != average-coupling-tendency)
    [ ask turtles [ assign-coupling-tendency ]
      set slider-check-2 average-coupling-tendency ]
  if (slider-check-3 != average-condom-use)
    [ ask turtles [ assign-condom-use ]
      set slider-check-3 average-condom-use ]
  if (slider-check-4 != average-test-frequency )
    [ ask turtles [ assign-test-frequency ]
      set slider-check-4 average-test-frequency ]
end

;; People move about at random.

to move  ;; turtle procedure
  rt random-float 360
  fd 1
end

;; People have a chance to couple depending on their tendency to have sex and
;; if they meet.  To better show that coupling has occurred, the patches below
;; the couple turn gray.

to couple  ;; turtle procedure -- righties only!
  let potential-partner one-of (turtles-at -1 0)
                          with [not coupled? and shape = "person lefty"]
  if potential-partner != nobody
    [ if random-float 10.0 < [coupling-tendency] of potential-partner
      [ set partner potential-partner
        set coupled? true
        ask partner [ set coupled? true ]
        ask partner [ set partner myself ]
        move-to patch-here ;; move to center of patch
        move-to patch-here ;; partner moves to center of patch
        set pcolor gray - 3
        ask (patch-at -1 0) [ set pcolor gray - 3 ] ] ]
end

;; If two peoples are together for longer than either person's commitment variable
;; allows, the couple breaks up.

to uncouple  ;; turtle procedure
  if coupled? and (shape = "person righty")
    [ if (couple-length > commitment) or
         ([couple-length] of partner) > ([commitment] of partner)
        [ set coupled? false
          set couple-length 0
          ask partner [ set couple-length 0 ]
          set pcolor black
          ask (patch-at -1 0) [ set pcolor black ]
          ask partner [ set partner nobody ]
          ask partner [ set coupled? false ]
          set partner nobody ] ]
end

;; Infection can occur if either person is infected, but the infection is unknown.
;; This model assumes that people with known infections will continue to couple,
;; but will automatically practice safe sex, regardless of their condom-use tendency.
;; Note also that for condom use to occur, both people must want to use one.  If
;; either person chooses not to use a condom, infection is possible.  Changing the
;; primitive to AND in the third line will make it such that if either person
;; wants to use a condom, infection will not occur.

to infect  ;; turtle procedure
  if coupled? and infected? and not known?
    [ if random-float 11 > condom-use or
         random-float 11 > ([condom-use] of partner)
        [ if random-float 100 < infection-chance
            [ ask partner [ set infected? true ] ] ] ]
end

;; People have a tendency to check out their health status based on a slider value.
;; This tendency is checked against a random number in this procedure. However, after being infected for
;; some amount of time called SYMPTOMS-SHOW, there is a 5% chance that the person will
;; become ill and go to a doctor and be tested even without the tendency to check.

to test  ;; turtle procedure
  if random-float 52 < test-frequency
    [ if infected?
        [ set known? true ] ]
  if infection-length > symptoms-show
    [ if random-float 100 < 5
        [ set known? true ] ]
end

;;;
;;; MONITOR PROCEDURES
;;;

to-report %infected
  ifelse any? turtles
    [ report (count turtles with [infected?] / count turtles) * 100 ]
    [ report 0 ]
end
@#$#@#$#@
GRAPHICS-WINDOW
288
10
723
466
12
12
17.0
1
10
1
1
1
0
1
1
1
-12
12
-12
12
1
1
1
weeks

BUTTON
12
81
95
114
setup
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
96
81
179
114
go
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

MONITOR
184
74
267
119
% infected
%infected
2
1
11

SLIDER
7
37
276
70
initial-people
initial-people
50
500
300
1
1
NIL
HORIZONTAL

SLIDER
7
162
276
195
average-commitment
average-commitment
1
200
50
1
1
weeks
HORIZONTAL

SLIDER
7
127
276
160
average-coupling-tendency
average-coupling-tendency
0
10
5
1
1
NIL
HORIZONTAL

SLIDER
7
197
276
230
average-condom-use
average-condom-use
0
10
0
1
1
NIL
HORIZONTAL

SLIDER
7
232
276
265
average-test-frequency
average-test-frequency
0
2
0
0.01
1
times/year
HORIZONTAL

PLOT
7
268
276
467
Populations
weeks
people
0.0
52.0
0.0
350.0
true
true
"set-plot-y-range 0 (initial-people + 50)" ""
PENS
"HIV-" 1.0 0 -10899396 true "" "plot count turtles with [not infected?]"
"HIV+" 1.0 0 -2674135 true "" "plot count turtles with [known?]"
"HIV?" 1.0 0 -13345367 true "" "plot count turtles with [infected?] - count turtles with [known?]"

@#$#@#$#@
## WHAT IS IT?

This model simulates the spread of the human immunodeficiency virus (HIV), via sexual transmission, through a small isolated human population.  It therefore illustrates the effects of certain sexual practices across a population.

As is well known now, HIV is spread in a variety of ways of which sexual contact is only one.  HIV can also be spread by needle-sharing among injecting drug users, through blood transfusions (although this has become very uncommon in countries like the United States in which blood is screened for HIV antibodies), or from HIV-infected women to their babies either before or during birth, or afterwards through breast-feeding.  This model focuses only on the spread of HIV via sexual contact.

The model examines the emergent effects of four aspects of sexual behavior.  The user controls the population's tendency to practice abstinence, the amount of time an average "couple" in the population will stay together, the population's tendency to use condoms, and the population's tendency to get tested for HIV.  Exploration of the first and second variables may illustrate how changes in sexual mores in our society have contributed to increases in the prevalence of sexually transmitted diseases, while exploration of the third and fourth may provide contemporary solutions to the problem.

By allowing the user to control how often an average individual will choose to be tested, the user can explore an important dimension of HIV's threat to public health.  Because the virus does not make itself immediately known in its host, individuals are often infected for some time before a test or immune deficiency symptoms (which leads to a test) identifies them as such.  Regular identification of individuals infected by the virus could have significant public health impacts if knowledge of the infection positively affected sexual behaviors.  This model explores this possibility by making all individuals who know of their positive HIV status always practice safe sex.

## HOW IT WORKS

The model uses "couples" to represent two people engaged in sexual relations.  Individuals wander around the world when they are not in couples.  Upon coming into contact with a suitable partner, there is a chance the two individuals will "couple" together.  When this happens, the two individuals no longer move about, they stand next to each other holding hands as a representation of two people in a sexual relationship.

The presence of the virus in the population is represented by the colors of individuals. Three colors are used: green individuals are uninfected, blue individuals are infected but their infection is unknown, and red individuals are infected and their infection is known.

## HOW TO USE IT

The SETUP button creates individuals with particular behavioral tendencies according to the values of the interface's five sliders (described below).  Once the simulation has been setup, you are now ready to run it, by pushing the GO button.  GO starts the simulation and runs it continuously until GO is pushed again.  During a simulation initiated by GO, adjustments in sliders can affect the behavioral tendencies of the population.

A monitor shows the percent of the population that is infected by HIV.  In this model each time-step is considered one week; the number of weeks that have passed is shown in the toolbar.

Here is a summary of the sliders in the model.  They are explained in more detail below.

- INITIAL-PEOPLE: How many people simulation begins with.
- AVERAGE-COUPLING-TENDENCY: General likelihood member of population has sex (0--10).
- AVERAGE-COMMITMENT: How many weeks sexual relationships typically lasts (0--200).
- AVERAGE-CONDOM-USE: General chance member of population uses a condom. (0--10).
- AVERAGE-TEST-FREQUENCY: Average frequency member of population will check their HIV status in a 1-year time period (0--2).

The total number of individuals in the simulation is controlled by the slider INITIAL-PEOPLE (initialized to vary between 50--500), which must be set before SETUP is pushed.

During the model's setup procedures, all individuals are given "tendencies" according to four additional sliders.  These tendencies are generally assigned in a normal distribution, so that, for instance, if a tendency slider is set at 8, the most common value for that tendency in the population will be 8.  Less frequently, individuals will have values 7 or 9 for that tendency, and even less frequently will individuals have values 6 or 10 (and so on).

The slider AVERAGE-COUPLING-TENDENCY (0--10) determines the tendency of the individuals to become involved in couples (as stated earlier, all couples are presumed to be sexually active). When the AVERAGE-COUPLING-TENDENCY slider is set at zero, coupling is unlikely, although (because tendencies are assigned in a normal distribution) it is still possible.  Note that when deciding to couple, both individuals involved must be "willing" to do so, so high coupling tendencies in two individuals are actually compounded (i.e. two individuals with a 50% chance of coupling actually only have a 25% of coupling in a given tick).

The slider AVERAGE-COMMITMENT (1--200) determines how long individuals are likely to stay in a couple (in weeks).  Again, the tendencies of both individuals in a relationship are considered; the relationship only lasts as long as is allowed by the tendency of the partner with a shorter commitment tendency.

The slider AVERAGE-CONDOM-USE (0--10) determines the tendency of individuals in the population to practice safe sex.  If an individual uses a condom, it is assumed in this model that no infection by HIV is possible.  Note that this tendency (like the others) is probabilistic at several levels.  For instance, when AVERAGE-CONDOM-USE is set to 9, most of the individuals have a CONDOM-USE value of 9, although some have CONDOM-USE values of 8 or 10, and fewer yet have CONDOM-USE values of 7 or 11 (11 would be off-scale and the same as 10 for all practical purposes).  Also, an individual with a CONDOM-USE value of 9 will still sometimes not choose to use a condom (10% of the time, roughly). Simulation of condom-use is further complicated by the fact that if one partner "wants" to use a condom while the other partner does not, the couple does not use condoms.  This characteristic of the model is representative of the dynamics of some sexual relations, but not others.  The decision was somewhat arbitrary, and the user is invited to play with this characteristic and others in the "Extending the Model" section of this tab.

The slider AVERAGE-TEST-FREQUENCY (0--2) is the final slider of the interface.  It determines the average frequency of an individual to get tested for HIV in a one-year time span.  Set at 1.0, the average person will get tested for HIV about once a year.  Set at 0.2, the average individual will only get tested about every five years.  This tendency has significant impact because the model assumes that individuals who know that they are infected always practice safe sex, even if their tendency as healthy individuals was different.  Again, this characteristic of the model is only represented of the behaviors of some individuals.  The model was designed in this way to highlight the public health effects associated with frequent testing *and* appropriate responses to knowledge of one's HIV status.  To explore the impact of alternative behaviors on public health, the model code can be changed relatively painlessly.  These changes are described in the section, "Extending the Model."

The model's plot shows the total number of uninfected individuals (green), infected individuals whose positive status is not known (blue), and infected individuals whose positive status is known (red).

## THINGS TO NOTICE

Set the INITIAL-PEOPLE slider to 300, AVERAGE-COUPLING-TENDENCY to 10, AVERAGE-COMMITMENT to 100, and the other two sliders to 0. Push SETUP and then push GO. Notice that many individuals rapidly pair up into stationary "couples", with the patches behind them turned a dark gray.  These couples represent sexual activity between the two individuals.  Individuals that continue to move about (and do not have a gray patch behind them) are not engaging in sexual relations.  With RELATIONSHIP-DURATION set to 100, an average individual will have monogamous sexual relations with a partner for about 100 weeks (approximately 2 years) before ending the sexual relationship and searching out a new partner.

Stop the simulation (by pressing the GO button once again), move the AVERAGE-COUPLING-TENDENCY slider to 0, push SETUP, and start the simulation again (with the GO button).  Notice that this time, couples are not forming.  With a low COUPLING-TENDENCY, individuals do not choose to have sex, which in this model is depicted by the graphical representation of couplehood.  Notice that with these settings, HIV does not typically spread. However, spreading could happen since the population's tendency is set according to a normal distribution and a few people will probably have COUPLING-TENDENCY values above 0 at this setting.

Again reset the simulation, this time with AVERAGE-COUPLING-TENDENCY set back to 10 and AVERAGE-COMMITMENT set to 1.  Notice that while individuals do not stand still next to each other for any noticeable length of time, coupling is nevertheless occurring.  This is indicated by the occasional flash of dark gray behind individuals that are briefly next to one another.  Notice that the spread of HIV is actually faster when RELATIONSHIP-DURATION is very short compared to when it is very long.

Now run a simulation with AVERAGE-COMMITMENT equal to 1, AVERAGE-COUPLING-TENDENCY set to 1, AVERAGE-CONDOM-USE set to 10, and AVERAGE-TEST-FREQUENCY set to 1.0. With negligible couple formation and high condom use anyway, there should be no spread of HIV.  Notice how pale blue "infection unknown" individuals turn red much quicker in this simulation.  When the individuals turn red, they know their HIV status.  Some individuals turn red because they have been infected for long enough that they develop symptoms, which alerts them to the need for an HIV test.  Others become red because they choose to be tested.  With AVERAGE-TEST-FREQUENCY set to 1.0, healthy individuals are also being tested, but their color does not change since the tests come back negative.

When all the individuals in the simulation are either green or red, change the sliders without stopping the simulation.  Set AVERAGE-COUPLING-TENDENCY to 10, AVERAGE-COMMITMENT to 100, AVERAGE-CONDOM-USE to 0, and AVERAGE-TEST-FREQUENCY to 0.  Notice that despite the immediate formation of couples and the fact that condom-use tendency is presumably very low, some between healthy (green) individuals and infected (red) individuals, no spread of HIV occurs.  This is because while the model expects HIV positive individuals to continue to engage in sexual relations, it presumes that those individuals will always use condoms and that the condoms will always work.  The rationale for this design is discussed briefly in the "What is it?" section of this tab.

Finally, set INITIAL-PEOPLE to 500 to notice that couples can form on top of each other.  Watch the simulation until you see individuals by themselves, but standing still and with a gray patch behind them indicating coupling.  Underneath one of its neighbors, is the individuals partner.  This apparent bug in the program is necessary because individuals need to be able to couple fairly freely.  If space constraints prohibited coupling, unwanted emergent behavior would occur with high population densities.

Couples are formed by a partnership of "righty" and "lefty" shapes which is not immediately noticeable.  These shapes are not intended to represent genders in any fashion, but merely to provide a useful way to depict couple graphics. In order for the hands of a righty and lefty to match up, both must be off-centered in their patch.  Without this feature, two couples next to each other would appear to be a line of four individuals (instead of two groups of two).  It is intended that the differences between righty and lefty shapes not be especially apparent in order to prevent unintended associations with gender.  Any righty or lefty shape can be thought of as male or female or neither.

## THINGS TO TRY

Run a number of experiments with the GO button to find out the effects of different variables on the spread of HIV.  Try using good controls in your experiment.  Good controls are when only one variable is changed between trials.  For instance, to find out what effect the average duration of a relationship has, run four experiments with the AVERAGE-COMMITMENT slider set at 1 the first time, 2 the second time, 10 the third time, and 50 the last.  How much does the prevalence of HIV increase in each case?  Does this match your expectations?

Are the effects of some slider variables mediated by the effects of others?  For instance, if lower AVERAGE-COMMITMENT values seem to increase the spread of HIV when all other sliders are set at 0, does the same thing occur if all other sliders are set at 10?  You can run many experiments to test different hypotheses regarding the emergent public health effects associated with individual sexual behaviors.

## EXTENDING THE MODEL

Like all computer simulations of human behaviors, this model has necessarily simplified its subject area substantially.  The model therefore provides numerous opportunities for extension:

The model depicts sexual activity as two people standing next to each other.  This suggests that all couples have sex and that abstinence is only practiced in singlehood.  The model could be changed to reflect a more realistic view of what couples are.  Individuals could be in couples without having sex.  To show sex, then, a new graphical representation would have to be employed.  Perhaps sex could be symbolized by having the patches beneath the couple flash briefly to a different color.

The model does not distinguish between genders.  This is an obvious over-simplification chosen because making an exclusively heterosexual model was untenable while making one that included a variety of sexual preferences might have distracted from the public health issues which it was designed to explore.  However, extending the model by adding genders would make the model more realistic.

The model assumes that individuals who are aware that they are infected always practice safe sex.  This portrayal of human behavior is clearly not entirely realistic, but it does create interesting emergent behavior that has genuine relevance to certain public health debate.  However, an interesting extension of the model would be to change individuals' reactions to knowledge of HIV status.

The model assumes that condom use is always 100% effective.  In fact, responsible condom use is actually slightly less than ideal protection from the transmission of HIV.  Add a line of code to the INFECT procedure to check for a slight random chance that a particular episode of condom-use is not effective.  Another change that can be made in the INFECT procedure regards a couple's choice of condom-use.  In this model, when the two partners of a couple "disagree" about whether or not to use a condom, the partner that doesn't wish to use a condom makes the choice.  The opposite possibility is also valid.

Finally, certain significant changes can easily be made in the model by simply changing the value of certain global variables in the procedure SETUP-GLOBALS.  Two variables set in this procedure that are especially worthy of investigation are INFECTION-CHANCE and SYMPTOMS-SHOW.  The former controls what chance an infection has of spreading from an infected to an uninfected partner if no protection is used.  The variable is currently set to 50, meaning that in a week of sexual relations, the chance of infection occurring is 50%.  It is not clear at this time how realistic that figure is. SYMPTOMS-SHOW is the variable that controls how long, on average, a person will have the HIV virus before symptoms begin to appear, alerting that person to the presence of some health problem.  It is currently set to 200 (weeks) in this model.

## NETLOGO FEATURES

Notice that the four procedures that assign the different tendencies generate many small random numbers and add them together.  This produces a normal distribution of tendency values.  A random number between 0 and 100 is as likely to be 1 as it is to be 99. However, the sum of 20 numbers between 0 and 5 is much more likely to be 50 than it is to be 99.

Notice that the global variables SLIDER-CHECK-1, SLIDER-CHECK-2, and so on are assigned with the values of the various sliders so that the model can check the sliders against the variables while the model is running.  Every time-step, a slider's value is checked against a global variable that holds the value of what the slider's value was the time-step before.  If the slider's current value is different than the global variable, NetLogo knows to call procedures that adjust the population's tendencies.  Otherwise, those adjustment procedures are not called.

## CREDITS AND REFERENCES

Special thanks to Steve Longenecker for model development.
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

person lefty
false
0
Circle -7500403 true true 170 5 80
Polygon -7500403 true true 165 90 180 195 150 285 165 300 195 300 210 225 225 300 255 300 270 285 240 195 255 90
Rectangle -7500403 true true 187 79 232 94
Polygon -7500403 true true 255 90 300 150 285 180 225 105
Polygon -7500403 true true 165 90 120 150 135 180 195 105

person righty
false
0
Circle -7500403 true true 50 5 80
Polygon -7500403 true true 45 90 60 195 30 285 45 300 75 300 90 225 105 300 135 300 150 285 120 195 135 90
Rectangle -7500403 true true 67 79 112 94
Polygon -7500403 true true 135 90 180 150 165 180 105 105
Polygon -7500403 true true 45 90 0 150 15 180 75 105

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

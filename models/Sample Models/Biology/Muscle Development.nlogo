breed [ muscle-fibers muscle-fiber ]

muscle-fibers-own [
  fiber-size   ;; different from built-in "size" because it uses different units
  max-size
]

patches-own [
  anabolic-hormone  ;; muscle building hormone
  catabolic-hormone ;; muscle breaking down hormone
]

globals [
  muscle-mass  ;; stores sum of muscle-fiber sizes
  ;; hormone bounds to ensure a realistic environment
  anabolic-hormone-max
  anabolic-hormone-min
  catabolic-hormone-max
  catabolic-hormone-min
  ;; the rate at which hormones from one fiber diffuse to others
  hormone-diffuse-rate
]

to setup
  ca
  set-default-shape muscle-fibers "circle"
  initialize-hormones
  new-muscle-fibers
  set muscle-mass sum [fiber-size] of muscle-fibers
  reset-ticks
end

to initialize-hormones
  ;; constants chosen align the model time frame for muscle development
  ;; with realistic values
  set hormone-diffuse-rate 0.75
  ask patches [
    set anabolic-hormone-max 200
    set catabolic-hormone-max 250
    set anabolic-hormone-min 50
    set catabolic-hormone-min 52
    set anabolic-hormone 50
    set catabolic-hormone 52
  ]
  regulate-hormones
end

to new-muscle-fibers
  ask patches [
    sprout-muscle-fibers 1 [
      set max-size 4
      ;; create a normalized distribution of maximum muscle fiber sizes
      ;; with median dependent on % of slow twitch fibers.
      repeat 20  [
        if random-float 100 > %-slow-twitch-fibers [
          set max-size max-size + 1
        ]
      ]
      ;; provide non-uniform starting sizes for varied results, everyone's different
      set fiber-size (0.2 + random-float 0.4) * max-size
      regulate-muscle-fibers
    ]
  ]
end

to go
  ;; note the use of the LOG primitive in the procedures called below
  ;; to simulate a natural system's tendency to adapt less and less
  ;; to each additional unit of some biological substance
  perform-daily-activity
  if lift? and (ticks mod days-between-workouts = 0)
    [ lift-weights ]
  sleep
  regulate-hormones
  develop-muscle
  set muscle-mass sum [fiber-size] of muscle-fibers
  tick
end

to perform-daily-activity
  ;; simulate hormonal effect of lifestyle
  ;; activities like watching TV and working
  ask muscle-fibers [
    set catabolic-hormone catabolic-hormone + 2.0 * (log fiber-size 10)
    set anabolic-hormone anabolic-hormone + 2.5 * (log fiber-size 10)
  ]
end

to lift-weights
  ;; simulate hormonal effect of weight training
  ask muscle-fibers [
    if( random-float 1.0 < intensity / 100 * intensity / 100 ) [
      set anabolic-hormone anabolic-hormone + (log fiber-size 10) * 55
      set catabolic-hormone catabolic-hormone + (log fiber-size 10) * 44
    ]
  ]
end

to sleep
  ;; simulate hormonal effect of sleeping
  ask patches [
    set catabolic-hormone catabolic-hormone - 0.5 * (log catabolic-hormone 10) * hours-of-sleep
    set anabolic-hormone anabolic-hormone - 0.48 * (log anabolic-hormone 10) * hours-of-sleep
  ]
end

to develop-muscle
  ask muscle-fibers [
    grow
    regulate-muscle-fibers
  ]
end

to grow  ;; turtle procedure
  ;; catabolic hormones must prepare the fibers for growth before the
  ;; anabolic hormones may add mass to the fibers
  set fiber-size (fiber-size - 0.20 * (log catabolic-hormone 10))
  set fiber-size (fiber-size + 0.20 * min (list (log anabolic-hormone 10)
                                                (1.05 * log catabolic-hormone 10)))
end

to regulate-muscle-fibers ;;turtle procedure
  ;; simulate the body's natural limits on minimum and maximum fiber sizes
  if (fiber-size  < 1) [ set fiber-size 1 ]
  if (fiber-size > max-size) [ set fiber-size max-size ]
  set color scale-color red fiber-size (-0.5 * max-size) (3 * max-size)
  ;; base the visible size of the turtle on its fiber-size
  set size max list 0.2 (min list 1 (fiber-size / 20))
end

to regulate-hormones ;;patch procedure
  ;; hormones spread to neighboring fibers
  diffuse anabolic-hormone hormone-diffuse-rate
  diffuse catabolic-hormone hormone-diffuse-rate
  ;; if there are to many or to few hormones in an area,
  ;; the body will try very hard to restore a balance
  ask patches [
    set anabolic-hormone min (list anabolic-hormone anabolic-hormone-max)
    set anabolic-hormone max (list anabolic-hormone anabolic-hormone-min)
    set catabolic-hormone min (list catabolic-hormone catabolic-hormone-max)
    set catabolic-hormone max (list catabolic-hormone catabolic-hormone-min)
    ;;color patches based on hormone concentrations
    set pcolor approximate-rgb ((catabolic-hormone / catabolic-hormone-max) * 255)
                   ((anabolic-hormone / anabolic-hormone-max)  * 255)
                   0
  ]
end
@#$#@#$#@
GRAPHICS-WINDOW
329
11
730
433
8
8
23.0
1
10
1
1
1
0
1
1
1
-8
8
-8
8
1
1
1
ticks

BUTTON
43
22
112
55
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

SLIDER
5
139
221
172
days-between-workouts
days-between-workouts
1
30
5
1
1
days
HORIZONTAL

SLIDER
5
106
221
139
hours-of-sleep
hours-of-sleep
0
12
8
0.5
1
hours
HORIZONTAL

PLOT
5
209
321
354
Muscle Development
Time
Muscle Mass
0.0
60.0
0.0
20.0
true
false
"" ""
PENS
"muscle" 1.0 0 -2674135 true "" "plot muscle-mass / 100"

PLOT
5
354
321
505
Hormones
Time
Hormones
0.0
60.0
0.0
30.0
true
true
"" ""
PENS
"anabolic" 1.0 0 -16777216 true "" "plot mean [anabolic-hormone] of patches"
"catabolic" 1.0 0 -1184463 true "" "plot mean [catabolic-hormone] of patches"

SLIDER
5
73
221
106
intensity
intensity
50
100
95
1
1
NIL
HORIZONTAL

BUTTON
127
22
196
55
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

SWITCH
226
73
321
106
lift?
lift?
0
1
-1000

SLIDER
5
172
221
205
%-slow-twitch-fibers
%-slow-twitch-fibers
0
100
50
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This is an exercise physiology model.  It is intended to help you understand the factors involved in generating the appropriate hormonal balance to develop muscle from weight lifting.  These factors include:

Frequency: How often you lift weights must be managed appropriately in order to see gains in muscle mass.  If you lift too frequently, you will not have time to recover and then rebuild stronger muscles.  If you fail to lift frequently enough, there will not be enough stimuli to elicit long term gains in muscle.

Sleep: The body performs most of its recovery while sleeping.  If you don't get enough sleep, you will not be providing enough opportunity for recovery, so you will find it difficult to gain muscle.

Intensity: How hard you works in the weight room affects how effective you are at recruiting all of your muscle fibers.  The greater the number of fibers recruited, the greater the growth stimulus, assuming appropriate recovery is provided for.

Genetic: The ratio of slow twitch to fast twitch muscle fibers plays a large role in how much muscle an individual is capable of developing.  Someone with a majority of fibers that exhibit slow twitch characteristics will have high endurance, but the potential to develop only moderate muscle mass.  An individual with a majority of fibers with fast twitch characteristics will have the potential to develop considerable muscle mass, but low endurance.

Diet: A poor diet can prevent muscle growth.  In this model we assume perfect diet.

All five of these factors must be understood and put into balance with one another in order to achieve optimal muscular development.  The appropriate combination is highly dependent on the individual and their current, unique state.  It will change over time.

## HOW IT WORKS

Ultimately, the effects of resistance training occur as a result of the hormonal responses it elicits from the body.  The hormones essential to muscle development can be separated into two broad classes: catabolic hormones and anabolic hormones.  Catabolic hormones break down the muscle fiber to prepare it to be rebuilt stronger by the anabolic hormones.  Note: catabolic hormones play a vital role, as muscle fibers must be broken down before they can be built back up.

This model attempts to simulate these effects with a cross sectional portrayal of a muscle at the level of the muscle fibers.  When the observer activates a muscle fiber through resistance training, the fiber releases a chemical signal that results in a surge of hormones at the location of the fiber.  These hormones affect the fiber development as mentioned above and will dissipate over time.

The circles represent muscle fibers.  The background they appear against may be thought of as the cellular fluid that contains the anabolic and catabolic hormones.  The brighter the green, the more anabolic (muscle building) the environment.  The brighter the yellow, the more catabolic (muscle destroying) the environment.

## HOW TO USE IT

Buttons:
SETUP: Sets up the model
GO: Runs the model

Switches:
LIFT?: Decides whether or not the person is actively weight lifting

Sliders:
INTENSITY: How hard the lifter is working.  The greater the intensity, the greater the number of muscle fibers that will be fatigued each workout session.
HOURS-OF-SLEEP: The amount of sleep a person gets affects how quickly the body breaks down the hormones.

DAYS-BETWEEN-WORKOUTS: The frequency of the workouts effects how much time the body has to recover and then over-compensate from the last workout
%-SLOW-TWITCH-FIBERS: How likely each fiber is to possess slow twitch characteristics.

Plots:
MUSCLE DEVELOPMENT VS. TIME: The sum of all fiber sizes over time
HORMONES VS. TIME: The average hormone content near each fiber

## THINGS TO TRY

Steps one through three should be run with %-SLOW-TWITCH slider set to 50.

1.  Run the model at its default settings.  What eventually happens to the amount of muscle mass?  Why?

2.  Overtraining occurs when the body is not allowed to recover completely from the last exercise session before being trained again.  This causes stagnation of muscular development, and in extreme cases, muscle loss.  What types of conditions can lead to overtraining?  What is the best way to recover from overtraining?  What steps can be taken to avoid it?

3.  Many undertake weight training in an effort to build the maximum amount of muscle they are capable of.  Find the best method for achieving this.  How must it vary over time?  Why is it important to take one's current level of condition into consideration when choosing a resistance training program?

4.  An issue rarely addressed in conventional training is that of genetic ability.  A major factor effecting this the proportion of slow twitch vs. fast twitch muscle fibers a person possesses.  Slow twitch fibers provide for greater endurance, fast twitch greater strength and size.

Attempt to obtain maximal muscular development with the %-SLOW-TWITCH slider set at 90% and then 10%. How do the results one can obtain vary with genetic ability?  Training methods?  What does this suggest about the average person following the routines of genetically gifted professional bodybuilders?

## EXTENDING THE MODEL

1. In order to ensure you have achieved maximum muscular development, add a pen named "max-muscle" to the "Muscle Development" plot.  Now modify the DO-PLOTTING procedure to use the "max-muscle" pen to plot the sum of the MAX-SIZE value of all the muscle fibers.

2. Nutritional quality can have a major influence on the results obtained from training.  Modify the model to allow for this influence.  Add a NUTRIENT variable to the MUSCLE-FIBER breed.  Add a NUTRITIONAL-QUALITY slider to the model.  Now modify the GO procedure to call a OBTAIN-NUTRITION function that releases nutrients into the patches.  Finally, alter the GROW procedure to use the available nutrients when adding size to a muscle fiber.

3.  Real life creates many inconsistencies in the average person's strength training program.  Add a variance function to the model that randomly generates nights of less sleep and extra rest days between workouts to reflect these inconsistencies.  Add a switch to allow the user to turn this variance on or off.  Add a slider to allow the user to adjust the level of variance.  What effect do these inconsistencies have on muscular development?

## THINGS TO NOTICE

The human body is an incredibly complex system.  In order to simulate the piece of it in which we are interested, assumptions have been made about the behavior of other pieces.  This can be seen in the hard-coding of various parameters, such as the hormone limits and the maximum muscle fiber sizes.  These assumptions allow us to focus upon gaining an understanding of the overall process of muscle development without becoming burdened with excessive information.

## NETLOGO FEATURES

Note the use of the `repeat` primitive and %-SLOW-TWITCH-FIBERS variable in the `new-muscle-fibers` procedure to generate a normal distribution of the maximum muscle fiber sizes centered at a median influenced by the %-SLOW-TWITCH-FIBERS value.

Note the use of the `log` primitive in the procedures which regulate hormonal release and balance.  This allows us to more closely mimic the natural tendency for each additional unit of a biological component to illicit less of an adaptive change from the system than the one before it.

Note the use of the `rgb` primitive in the `regulate-hormones` procedure to color patches based on hormone quantities and provide a smooth visual transition from an anabolic to a catabolic state.

## CREDITS AND REFERENCES

Original implementation: Scott Styles, for the Center for Connected Learning and Computer-Based Modeling.
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

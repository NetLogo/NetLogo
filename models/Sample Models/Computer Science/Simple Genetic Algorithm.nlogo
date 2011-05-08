;; Each potential solution is represented by a turtle.

turtles-own [
  bits           ;; list of 0's and 1's
  fitness
]

globals [
  winner         ;; turtle that currently has the best solution
]

to setup
  clear-all
  create-turtles population-size [
    set bits n-values world-width [one-of [0 1]]
    calculate-fitness
    hide-turtle  ;; the turtles' locations are not used, so hide them
  ]
  update-display
  reset-ticks
end

to go
  if [fitness] of winner = world-width
    [ stop ]
  create-next-generation
  update-display
  tick
end

to update-display
  set winner max-one-of turtles [fitness]
  ask patches [
    ifelse item pxcor ([bits] of winner) = 1
      [ set pcolor white ]
      [ set pcolor black ]
  ]
end

;; ===== Generating Solutions

;; Each solution has its "fitness score" calculated.
;; Higher scores mean "more fit", and lower scores mean "less fit".
;; The higher a fitness score, the more likely that this solution
;;   will be chosen to reproduce and create offspring solutions
;;   in the next generation.
;;
to calculate-fitness       ;; turtle procedure
  ;; For the "ALL-ONES" problem, the fitness is simply equal to the number of ones
  ;; that occur in this solution's bits.
  ;; However, you could solve more interesting problems by changing this procedure
  ;; to evaluate the bits in other ways.  For instance, the bits might
  ;; encode rules for how a turtle should move across the world in a search for food.
  set fitness length (remove 0 bits)
end

;; This procedure does the main work of the genetic algorithm.
;; We start with the old generation of solutions.
;; We choose solutions with good fitness to produce offspring
;; through crossover (sexual recombination), and to be cloned
;; (asexual reproduction) into the next generation.
;; There is also a chance of mutation occurring in each individual.
;; After a full new generation of solutions has been created,
;; the old generation dies.
to create-next-generation
  ; The following line of code looks a bit odd, so we'll explain it.
  ; if we simply wrote "LET OLD-GENERATION TURTLES",
  ; then OLD-GENERATION would mean the set of all turtles, and when
  ; new solutions were created, they would be added to the breed, and
  ; OLD-GENERATION would also grow.  Since we don't want it to grow,
  ; we instead write "TURTLES WITH [TRUE]", which makes OLD-GENERATION
  ; an agentset, which doesn't get updated when new solutions are created.
  let old-generation turtles with [true]

  ; Some number of the population is created by crossover each generation
  ; we divide by 2 because each time through the loop we create two children.
  let crossover-count  (floor (population-size * crossover-rate / 100 / 2))

  repeat crossover-count
  [
    ; We use "tournament selection", with tournament size = 3.
    ; This means, we randomly pick 3 solutions from the previous generation
    ; and select the best one of those 3 to reproduce.

    let parent1 max-one-of (n-of 3 old-generation) [fitness]
    let parent2 max-one-of (n-of 3 old-generation) [fitness]

    let child-bits crossover ([bits] of parent1) ([bits] of parent2)

    ; create the two children, with their new genetic material
    ask parent1 [ hatch 1 [ set bits item 0 child-bits ] ]
    ask parent2 [ hatch 1 [ set bits item 1 child-bits ] ]
  ]

  ; the remainder of the population is created by cloning
  ; selected members of the previous generation
  repeat (population-size - crossover-count * 2)
  [
    ask max-one-of (n-of 3 old-generation) [fitness]
      [ hatch 1 ]
  ]

  ask old-generation [ die ]

  ; now we're just talking to the new generation of solutions here
  ask turtles
  [
    ; there's a chance of mutations occurring
    mutate
    ; finally we update the fitness value for this solution
    calculate-fitness
  ]
end

;; ===== Mutations

;; This reporter performs one-point crossover on two lists of bits.
;; That is, it chooses a random location for a splitting point.
;; Then it reports two new lists, using that splitting point,
;; by combining the first part of bits1 with the second part of bits2
;; and the first part of bits2 with the second part of bits1;
;; it puts together the first part of one list with the second part of
;; the other.
to-report crossover [bits1 bits2]
  let split-point 1 + random (length bits1 - 1)
  report list (sentence (sublist bits1 0 split-point)
                        (sublist bits2 split-point length bits2))
              (sentence (sublist bits2 0 split-point)
                        (sublist bits1 split-point length bits1))
end

;; This procedure causes random mutations to occur in a solution's bits.
;; The probability that each bit will be flipped is controlled by the
;; MUTATION-RATE slider.
to mutate   ;; turtle procedure
  set bits map [ifelse-value (random-float 100.0 < mutation-rate) [1 - ?] [?]]
               bits
end

;; ===== Diversity Measures

;; Our diversity measure is the mean of all-pairs Hamming distances between
;; the genomes in the population.
to-report diversity
  let distances []
  ask turtles [
    let bits1 bits
    ask turtles with [self > myself] [
      set distances fput (hamming-distance bits bits1) distances
    ]
  ]
  ; The following  formula calculates how much 'disagreement' between genomes
  ; there could possibly be, for the current population size.
  ; This formula may not be immediately obvious, so here's a sketch of where
  ; it comes from.  Imagine a population of N turtles, where N is even, and each
  ; turtle has  only a single bit (0 or 1).  The most diverse this population
  ; can be is if half the turtles have 0 and half have 1 (you can prove this
  ; using calculus!). In this case, there are (N / 2) * (N / 2) pairs of bits
  ; that differ.  Showing that essentially the same formula (rounded down by
  ; the floor function) works when N is odd, is left as an exercise to the reader.
  let max-possible-distance-sum floor (count turtles * count turtles / 4)

  ; Now, using that number, we can normalize our diversity measure to be
  ; between 0 (completely homogeneous population) and 1 (maximally heterogeneous)
  report (sum distances) / max-possible-distance-sum
end

;; The Hamming distance between two bit sequences is the fraction
;; of positions at which the two sequences have different values.
;; We use MAP to run down the lists comparing for equality, then
;; we use LENGTH and REMOVE to count the number of inequalities.
to-report hamming-distance [bits1 bits2]
  report (length remove true (map [?1 = ?2] bits1 bits2)) / world-width
end
@#$#@#$#@
GRAPHICS-WINDOW
20
12
530
63
-1
-1
5.0
1
10
1
1
1
0
1
1
1
0
99
0
3
1
1
1
ticks
30

BUTTON
108
108
193
141
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

BUTTON
20
68
193
101
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
20
148
192
181
population-size
population-size
5
200
100
5
1
NIL
HORIZONTAL

PLOT
200
68
530
218
Fitness Plot
gen #
raw fitness
0.0
20.0
0.0
101.0
true
true
"" ""
PENS
"best" 1.0 0 -2674135 true "" "plot max [fitness] of turtles"
"avg" 1.0 0 -10899396 true "" "plot mean [fitness] of turtles"
"worst" 1.0 0 -13345367 true "" "plot min [fitness] of turtles"

BUTTON
20
108
105
141
step
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

SLIDER
20
228
192
261
mutation-rate
mutation-rate
0
10
0.5
0.1
1
NIL
HORIZONTAL

PLOT
200
223
532
380
Diversity Plot
gen #
diversity
0.0
20.0
0.0
1.0
true
false
"" ""
PENS
"diversity" 1.0 0 -8630108 true "" "if plot-diversity? [ plot diversity ]"

SWITCH
20
268
192
301
plot-diversity?
plot-diversity?
0
1
-1000

SLIDER
20
188
192
221
crossover-rate
crossover-rate
0
100
70
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This model demonstrates the use of a genetic algorithm on a very simple problem.  Genetic algorithms (GAs) are a biologically-inspired computer science technique that combine notions from Mendelian genetics and Darwinian evolution to search for good solutions to problems (including difficult problems).  The GA works by generating a random population of solutions to a problem, evaluating those solutions and then using cloning, recombination and mutation to create new solutions to the problem.

In this model we use the simple "ALL-ONES" problem to demonstrate how this is possible. We use such a simple problem in this model in order to highlight the solution technique only. The idea of the "ALL-ONES" problem is to find a string of bits (that is, a sequence of just ones and zeros) that contains all ones, and no zeros.  Thus the string that best solves this problem is "111111...111".

## HOW IT WORKS

The genetic algorithm is composed of the following steps.

1) A population of random solutions is created.  Each solution consists of a string of randomly mixed "1"s and "0"s.

2) Each solution is evaluated on the basis of how well it solves the problem.  This measure of the "goodness" of the solution is called its "fitness".  In this model, our goal is simply to find a solution that consists of all "1"s.  (In real-world applications of the genetic algorithm, the goals are much more complex, but the solutions are still usually encoded as binary strings.)

3) A new generation of solutions is created from the old generation, where solutions that have a higher fitness scores are more likely to be chosen as "parent" solutions than those that have low fitness scores.

A) The selection method used in this model is called "tournament selection", with a tournament size of 3.  This means that 3 solutions are drawn randomly from the old generation, and the one with the highest fitness is chosen to become a parent.

B) Either one or two parents are chosen to create children.  With one parent, the child is a clone or copy of the parent.  With two parents, the process is the digital analog of sexual recombination -- the two children inherit part of their genetic material from one parent and part from the other.

C) There is also a chance that mutation will occur, and some of the child's bits will be changed from "1"s to "0"s, or vice versa.

4) Steps 2 and 3 above are repeated until a solution is found that successfully solves the problem.

## HOW TO USE IT

Press the SETUP button to create an initial random population of solutions.

Press the STEP button to have one new generation created from the old generation.

Press the GO button to have the genetic algorithm run until a solution has been found.

The best solution found in each generation is displayed in the VIEW.  Each white column represents a "1"-bit and each black column represents a "0"-bit.

=== Parameters ===

The POPULATION-SIZE slider controls the number of solutions that are present in each generation.

The CROSSOVER-RATE slider controls what percent of each new generation is created through sexual reproduction (recombination or crossover between two parents' genetic material), and what percent (100 - CROSSOVER-RATE) is created through asexual reproduction (cloning of one parent's genetic material).

The MUTATION-RATE slider controls the percent chance of mutation.  This chance applies to each position in the string of bits of a new individual.  For instance, if the string is 100 bits long, and the mutation-rate is set at 1%, then on average one bit will be changed during the creation of each new individual.

The PLOT-DIVERSITY? switch controls whether the amount of diversity within the population of solutions is plotted each generation, shown in the "Diversity Plot".  Turning off PLOT-DIVERSITY? significantly increases the speed of the model because calculating diversity requires a lot of computation.

The "Fitness Plot" is used to show the best, average, and worst fitness values of the solutions at each generation.

## THINGS TO NOTICE

Step through the model slowly, and look at the visual representation of the best solution found in each generation, displayed in the VIEW.  How often does the best solution in Generation X+1 appear to be the offspring of the best solution in Generation X?

As the fitness in the population increases, the diversity decreases.  Why is this?

## THINGS TO TRY

Explore the effects of larger or smaller population sizes on the number of generations it takes to solve the problem completely.  What happens if you measure the amount of time (in seconds) that it takes to solve the problem completely?

How does asexual reproduction compare to sexual reproduction for solving this problem?  (What if CLONING-RATE is 100, or CLONING-RATE is 0?)

How much mutation is beneficial for the genetic algorithm?  Can the genetic algorithm find a perfect solution if there is MUTATION-RATE is 0?  What about if MUTATION-RATE is 10.0?  Can you find an optimal MUTATION-RATE?

## EXTENDING THE MODEL

Many variations on this simple genetic algorithm exist.  For example, some genetic algorithms include "elitism".  In this case, the best X% of solutions from the old generation are always copied directly into the new generation.  Modify this model so that it uses elitism.

Another type of selection for reproduction that is sometimes used in genetic algorithms is called "roulette selection".  In this case, you may imagine each solution in the population being assigned a wedge of a large roulette wheel.  The size of the wedge is determined by dividing the fitness of each solution by the sum of the fitnesses of all solutions in the population.  Thus, the probability of selecting any given solution to reproduce is directly proportional to its fitness.  Try implementing this type of selection, and compare its performance to the "tournament selection" method that is currently used in this model.

As noted above, the "ALL-ONES" problem is a toy problem that is not very interesting in its own right.  A natural extension of this model is to use the genetic algorithm to solve a problem that is significantly more interesting.  Fortunately, you can change the problem that the genetic algorithm is solving by only modifying one thing, the "fitness function", which evaluates how good a given string of bits is at solving whatever problem you are trying to solve.  For example, you could evolve rules for how a turtle should move, in order to maximize its food collection as it travels through the world.  To do so, you might change the `ga-calculate-fitness` procedure to run a little simulation where a turtle moves in the world (according to some rules that are defined by the string of "1"s and "0"s), count how much food the turtle collects, and then set the fitness accordingly.

## NETLOGO FEATURES

Note that NetLogo's powerful ability to work with agentsets makes it very easy to code the "tournament selection" used in this model.  The following code is sufficient:

    max-one-of (n-of 3 old-generation) [ga-fitness]

## RELATED MODELS

Echo is another model that is inspired by the work of John H. Holland.  It examines issues of evolutionary fitness and natural selection.

There are several NetLogo models that examine principles of evolution from a more biological standpoint, including Altruism, Bug Hunt Camouflage, Cooperation, Mimicry, Peppered Moths, as well as the set of Genetic Drift models.

Sunflower Biomorph uses an artistic form of simulated evolution, driven by aesthetic choices made by the user.

## CREDITS AND REFERENCES

This model is based off of work by John H. Holland, who is widely regarded as the father of the genetic algorithms.  See Holland's book "Adaptation in Natural and Artificial Systems", 1992, MIT Press.

Additional information about genetic algorithms is available from a plethora of sources online.
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
NetLogo 5.0beta2
@#$#@#$#@
need-to-manually-make-preview-for-this-model
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

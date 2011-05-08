breed [philosophers philosopher]
philosophers-own [
  state                   ;; my current state: "HUNGRY", "EATING", or "THINKING"
  left-fork right-fork    ;; the forks on my right and left
  total-eaten             ;; how much I've had to eat
]

breed [forks fork]
forks-own [
  home-xpos home-ypos home-heading     ;; where I belong when I'm on the table
  owner                                ;; the philosopher that currently owns me (if any)
  marked?                              ;; whether I'm currently marked
]

to setup
  clear-all
  ;; set up the model
  make-turtles
  recolor
  reset-ticks
end

;; create all the turtles, place them, and associate forks with philosophers
to make-turtles
  set-default-shape philosophers "person torso"
  set-default-shape forks "fork"
  ;; create-ordered-<breed> equally spaces the headings of the turtles,
  ;; in who number order
  create-ordered-philosophers num-philosophers [
    set size 0.1
    jump 0.35
    set state "THINKING"
  ]
  create-ordered-forks num-philosophers [
    rt 180 / num-philosophers
    jump 0.25
    rt 180
    set size 0.1
    set marked? false
    set owner nobody
    ;; save my position and heading, so the philosophers can replace me later
    set home-xpos xcor
    set home-ypos ycor
    set home-heading heading
  ]
  ask philosophers [
    set left-fork fork (who + num-philosophers)
    ifelse who = 0
      [ set right-fork fork (2 * num-philosophers - 1) ]
      [ set right-fork fork (who + num-philosophers - 1) ]
  ]
  ask one-of forks [ set marked? true ]
end

to go
  ask one-of philosophers [ update ]
  recolor
  tick
end

;; everybody gets a new color.
to recolor
  ask philosophers [
    ;; look up the color in the colors list indexed by our current state
    ifelse state = "THINKING"
      [ set color blue ]
      [ ifelse state = "HUNGRY"
        [ set color red ]
        [ set color green ] ]
  ]
  ask forks [
    ;; we'll indicate marked forks only if cooperation is on.
    ifelse cooperation? and marked?
      [ set color magenta ]
      [ set color white ]
  ]
end

;; here's where philosophers actually do their thing. note that a philosopher
;; can go through several states in the same call to update.
to update  ;; philosopher procedure
  if state = "THINKING" [
    if random-float 1.0 < hungry-chance [
      set state "HUNGRY"
    ]
    stop
  ]
  if state = "EATING" [
    ;; keep track of how much we're eating.
    set total-eaten (total-eaten + 1)
    if random-float 1.0 < full-chance [
      ;; put down forks
      ifelse cooperation?
        [ release-forks-smart ]
        [ release-forks-naive ]
      ;; continue thinking
      set state "THINKING"
    ]
    stop
  ]
  if state = "HUNGRY" [
    ; try to pick up the forks.
    ifelse cooperation?
      [ acquire-forks-smart ]
      [ acquire-forks-naive ]
    ; if we've got both forks, eat.
    if got? left-fork and got? right-fork
      [ set state "EATING" ]
    stop
  ]
end

;; just drop the forks.
to release-forks-naive  ;; philosopher procedure
  release left-fork
  release right-fork
end

;; a more sophisticated strategy for releasing the forks, which switches any
;; marks to the other fork. see the info tab for details.
to release-forks-smart  ;; philosopher procedure
  ;; check left fork
  ifelse [marked?] of left-fork [
    ask left-fork [ set marked? false ]
    ask right-fork [ set marked? true ]
  ]
  [
    ;; otherwise, check right fork.
    if [marked?] of right-fork [
      ask right-fork [ set marked? false ]
      ask left-fork [ set marked? true ]
    ]
  ]
  ;; release the forks.
  release left-fork
  release right-fork
end

;; to release a fork, we set its owner to nobody and replace it on the table.
to release [fork]  ;; philosopher procedure
  ask fork [
    if owner != nobody [
      set owner nobody
      setxy home-xpos home-ypos
      set heading home-heading
    ]
  ]
end

;; just try to pick each fork up. if I get only one, i'll just hold it
;; until I get the other one.
to acquire-forks-naive  ;; philosopher procedure
  if [owner] of left-fork = nobody
    [ acquire-left ]
  if [owner] of right-fork = nobody
    [ acquire-right ]
end

;; a more sophisticated strategy for acquiring the forks. see the info tab
;; for details.
to acquire-forks-smart  ;; philosopher procedure
  if [owner] of left-fork = nobody [
    if ([not marked?] of left-fork) or got? right-fork
      [ acquire-left ]
  ]
  if [owner] of right-fork = nobody [
    if ([not marked?] of right-fork) or got? left-fork
      [ acquire-right ]
  ]
end

;; grab the left fork. set its owner to me and move it
to acquire-left  ;; philosopher procedure
  ask left-fork [
    set owner myself
    move-to owner
    set heading [heading] of owner
    rt 8  fd 0.1
  ]
end

;; grab the right fork. set its owner to me and move it
to acquire-right  ;; philosopher procedure
  ask right-fork [
    set owner myself
    move-to owner
    set heading [heading] of owner
    lt 8  fd 0.1
  ]
end

;; I've got a fork if it's owned by me.
to-report got? [fork]  ;; philosopher procedure
  report self = [owner] of fork
end
@#$#@#$#@
GRAPHICS-WINDOW
354
10
764
441
0
0
400.0
1
10
1
1
1
0
0
0
1
0
0
0
0
1
1
1
ticks
30

BUTTON
10
65
79
98
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
85
65
155
98
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
30
183
63
num-philosophers
num-philosophers
2.0
40.0
20
1.0
1
NIL
HORIZONTAL

SLIDER
10
105
183
138
hungry-chance
hungry-chance
0.0
1.0
0.5
0.01
1
NIL
HORIZONTAL

SLIDER
10
140
183
173
full-chance
full-chance
0.0
1.0
0.5
0.01
1
NIL
HORIZONTAL

PLOT
8
183
345
346
Spaghetti consumed
Philosopher ID
Spaghetti
0.0
100.0
0.0
25.0
true
false
"set-plot-x-range 0 (count philosophers + 1)" ""
PENS
"default" 1.0 1 -13345367 false "" "plot-pen-reset\nask philosophers [ plotxy who total-eaten ]"

PLOT
8
348
345
540
Resource allocation
Time
No. of philosophers
0.0
100.0
0.0
100.0
true
true
"set-plot-y-range 0 (count philosophers)" ""
PENS
"Thinking" 1.0 0 -13345367 true "" "plot count philosophers with [state = \"THINKING\"]"
"Hungry" 1.0 0 -2674135 true "" "plot count philosophers with [state = \"HUNGRY\"]"
"Eating" 1.0 0 -10899396 true "" "plot count philosophers with [state = \"EATING\"]"

SWITCH
190
105
341
138
cooperation?
cooperation?
1
1
-1000

BUTTON
160
65
236
98
go once
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

@#$#@#$#@
## WHAT IS IT?

The Dining Philosophers problem is a classic case study in the synchronization of concurrent processes.  It will be familiar to many students of Computer Science, but is applicable to many situations in which several independent processes must coordinate the use of shared resources.

The problem is fairly simple.  Suppose there is a group of philosophers sitting at a round table eating spaghetti.  These are boring philosophers: they do nothing but think, get hungry and eat.  In particular, they do not communicate with one another.

A fork sits on the table in between each pair of philosophers, so there are exactly as many forks as philosophers.  However, the spaghetti is quite messy, so in order to eat, each philosopher needs to be holding two forks, both the fork to her left and the fork to her right.  Clearly, if all the philosophers are to get some spaghetti, they'll have to share the forks.

There are many ways that this can go wrong.  A given philosopher can pick up both forks and begin eating, and never stop.  This guarantees that (at least) her immediate neighbors will never get to eat.  (Though at least SOMEONE gets to eat!)

What would happen if every philosopher immediately picked up the fork to her right, then waited for the fork to her left to become available?  This situation is called "deadlock," and it is the bane of designers of concurrent systems.

The goal of the problem is to come up with a strategy that the philosophers can use to guarantee that:  
1. At least one hungry philosopher can always eat.  
2. On average, all the philosophers get the same amount to eat.

There is one other feature of the system that aids in finding a solution: while a philosopher is holding a fork, she has the ability to place a mark on it or to remove an existing mark.  These marks are visible to any philosopher who inspects the fork.  One random fork will always start out marked, but in order to avoid confusion, marked forks are not visually distinguished unless cooperation is enabled (in which case they are a different color).

Can you think of a way to feed the philosophers?

Remember that the philosophers shouldn't, in principle, communicate (apart from marking forks, though that arguably constitutes a communication channel).  This means that the assignment of global group properties (such as "even/odd-numbered philosophers" or "first philosopher") is not allowed.  The astute reader will note that the initial marking of a single fork violates this rule by assigning a globally unique property to a single philosopher.  In the absence of such an initially distinguished fork, can you think of a way to feed the philosophers?

## HOW IT WORKS

Philosophers know which fork is on their left and which fork is on their right.  They also know what state they're in (thinking, hungry or eating) and how much they've eaten in total.  Forks know who they're currently being held by, if anyone, and whether or not they're marked.

To pick up a fork, a philosopher must first check that the fork isn't already being held by his associate, then set the fork's owner to himself.

To release a fork, a philosopher simply sets the fork's owner to nobody.

All the philosophers are initially thinking (blue).  At each time step, a thinking philosopher may become hungry (red) with probability hungry-chance.  A hungry philosopher will try to acquire both forks, and until she has done so will remain hungry.  A hungry philosopher with both forks immediately begins eating (green).  An eating philosopher may become full with probability full-chance, at which point she will release both forks and resume thinking (blue).

The value of the cooperation? switch determines which strategy is used to acquire and release the forks. With cooperation off, the following naive strategy is used to pick up the forks:  

1. If the left fork is available, take it.  
2. If the right fork is available, take it.  
3. If you have both forks, begin eating. Otherwise, try again.  

When full, the forks are simply released. Marks are completely ignored.

With cooperation on, a more sophisticated strategy using marks is used. To acquire the forks:

1. If the left fork is available, take it.  
2. If you have the left fork and it is marked and you're not already holding the right fork, release the left fork.  
3. If the right fork is available, take it.  
4. If you have the right fork and it is marked and you're not already holding the left fork, release the right fork.  
5. If you have both forks, begin eating. Otherwise, try again.
  
Once you are done eating, to release the forks:  

1. If either fork is marked, unmark it and mark the other fork.  
2. Release the forks.

## HOW TO USE IT

Initial settings:  
- num-philosophers: how many philosophers you'd like to feed.

The setup button will set the initial conditions. The go button will run the simulation, and the "go once" button will run the simulation for just one step, allowing you to watch what happens in more detail.

Other settings:  
- hungry-chance: The probability of any thinking philosopher becoming hungry at any step.  
- full-chance: The probability of any eating philosopher becoming full at any step.  
- cooperation?: If off, the philosophers will use a naive strategy to acquire their forks; if on, they'll use a more sophisticated strategy. See HOW IT WORKS above.

Plots:  
- Spaghetti consumed: plots the amount of spaghetti each philosopher has consumed (based on how many time steps she has spent in the eating state).  
- Resource allocation: plots the number of philosophers in each state over time.

## THINGS TO NOTICE

Play with different configurations of hungry-chance and full-chance and different numbers of philosophers.  See how different combinations stress the system in different ways.

What settings produce deadlock more often?  (You may want to use the speed slider to fast forward the graphics so you can do longer runs more quickly.)

Notice how, although the system works well under certain circumstances, more stressful circumstances may expose a weakness.  This demonstrates the importance of "stress testing" when assessing the scalability of a system, particularly in the presence of concurrency.

## THINGS TO TRY

Experiment with cooperation in combination with different settings for hungry-chance and full-chance.  See if you can find a situation where there is a striking contrast between the behaviors of the cooperating philosophers and the naive philosophers.

Try running the system for a long time in a variety of different configurations.  Does it ever seem to perform well at first, but eventually degrade (and maybe even deadlock)? What about vice versa?  What do you think this shows about the value of "longevity testing" when assessing the stability and performance of a concurrent system?

## EXTENDING THE MODEL

Try to think of a different strategy for the philosophers, then implement it and see how well it works!  You will probably want to make use of marks, so remember that they are not visible unless cooperation is enabled; you may wish to change this.  Can you come up with a simpler strategy than the one we demonstrate?

Can you think of other configurations of processes and resources that might be interesting to experiment with?  For example, suppose there is one salt shaker on the table where all the philosophers can reach it, and suppose that each time a philosopher has acquired both forks, she must acquire the salt shaker and salt her spaghetti before she begins eating.  She can release the salt shaker after only one time step (i.e., before she finishes eating her pasta and releases the forks), so several philosophers can still eat at once.  Can this modification lead to deadlock? What if there are both salt and pepper?  Test your intuition!

There are many, many other such possibilities, and many are directly analogous to situations that frequently arise in practical resource allocation problems.

## CREDITS AND REFERENCES

Thanks to Matt Hellige for his work on this model.
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

fork
true
0
Polygon -7500403 true true 160 247 150 251 140 248 147 107 129 97 133 29 137 86 141 86 147 38 151 86 155 84 159 39 166 96 154 105

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

person torso
true
0
Circle -7500403 true true 106 17 88
Polygon -7500403 true true 140 95 141 115 97 125 63 222 57 232 57 248 67 250 75 245 77 237 111 174 120 257 182 257 192 175 225 241 227 248 235 251 239 251 248 242 243 231 239 232 206 126 163 114 161 92

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

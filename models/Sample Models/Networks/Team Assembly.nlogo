globals
[
  newcomer              ;; an agent who has never collaborated
  component-size        ;; current running size of component being explored
  giant-component-size  ;; size of largest connected component
  components            ;; list of connected components
]

turtles-own
[
  incumbent?   ;; true if an agent has collaborated before
  in-team?     ;; true if an agent belongs to the new team being constructed
  downtime     ;; the number of time steps passed since the agent last collaborated
  explored?    ;; used to compute connected components in the graph
]

links-own
[
  new-collaboration?  ;; true if the link represents the first time two agents collaborated
]


;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to make-newcomer
  create-turtles 1
  [
    set color blue + 1
    set size 1.8
    set incumbent? false
    set in-team? false
    set newcomer self
    set downtime 0
    set explored? false
  ]
end


to setup
  clear-all
  set-default-shape turtles "circle"

  ;; assemble the first team
  repeat team-size [ make-newcomer ]
  ask turtles
  [
    set in-team? true
    set incumbent? true
  ]
  tie-collaborators
  color-collaborations

  ask turtles  ;; arrange turtles in a regular polygon
  [
    set heading (360 / team-size) * who
    fd 1.75
    set in-team? false
  ]
  find-all-components
  reset-ticks
end


;;;;;;;;;;;;;;;;;;;;;;;
;;; Main Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;

to go
  ;; all existing turtles are now considered incumbents
  ask turtles [set incumbent? true set color gray - 1.5 set size 0.9]
  ask links [set new-collaboration? false]

  ;; assemble a new team
  pick-team-members
  tie-collaborators
  color-collaborations

  ;; age turtles
  ask turtles
  [
    ;; agents drop out of the collaboration network when they become inactive for max-downtime steps
    if downtime > max-downtime
      [die]

    set in-team? false
    set downtime downtime + 1
  ]

  if layout? [ layout ]
  find-all-components
  tick
end


;; choose turtles to be in a new team
to pick-team-members
  let new-team-member nobody
  repeat team-size
  [
    ifelse random-float 100.0 >= p  ;;with a probability P, make a newcomer
    [
      make-newcomer
      set new-team-member newcomer
    ]
    [
      ;; with a probability Q, choose a new team member who was a previous collaborator of an existing team member
      ;; if the current team has at least one previous collaborator.
      ;; otherwise collaborate with a previous incumbent
      ifelse random-float 100.0 < q and any? (turtles with [in-team? and (any? link-neighbors with [not in-team?])])
        [set new-team-member one-of turtles with [not in-team? and (any? link-neighbors with [in-team?])]]
        [set new-team-member one-of turtles with [not in-team?]]
    ]
    ask new-team-member  ;; specify turtle to become a new team member
    [
      set in-team? true
      set downtime 0
      set size 1.8
      set color ifelse-value incumbent? [yellow + 2] [blue + 1]
    ]
  ]
end


;; forms a link between all unconnected turtles with in-team? = true
to tie-collaborators
  ask turtles with [in-team?]
  [
    create-links-with other turtles with [in-team?]
    [
      set new-collaboration? true  ;; specifies newly-formed collaboration between two members
      set thickness 0.3
    ]
  ]
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Visualization Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; color links according to past experience
to color-collaborations
    ask links with [[in-team?] of end1 and [in-team?] of end2]
    [
      ifelse new-collaboration?
      [
        ifelse ([incumbent?] of end1) and ([incumbent?] of end2)
        [
          set color yellow       ;; both members are incumbents
        ]
        [
          ifelse ([incumbent?] of end1) or ([incumbent?] of end2)
            [ set color green ]  ;; one member is an incumbent
            [ set color blue ]   ;; both members are newcomers
        ]
      ]
      [
        set color red            ;; members are previous collaborators
      ]
    ]
end

;; perform spring layout on all turtles and links
to layout
  repeat 12 [
    layout-spring turtles links 0.18 0.01 1.2
    display
  ]
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Network Exploration ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; to find all the connected components in the network, their sizes and starting turtles
to find-all-components
  set components []
  set giant-component-size 0

  ask turtles [ set explored? false ]
  ;; keep exploring till all turtles get explored
  loop
  [
    ;; pick a turtle that has not yet been explored
    let start one-of turtles with [ not explored? ]
    if start = nobody [ stop ]
    ;; reset the number of turtles found to 0
    ;; this variable is updated each time we explore an
    ;; unexplored turtle.
    set component-size 0
    ask start [ explore ]
    ;; the explore procedure updates the component-size variable.
    ;; so check, have we found a new giant component?
    if component-size > giant-component-size
    [
      set giant-component-size component-size
    ]
    set components lput component-size components
  ]
end

;; finds all turtles reachable from this turtle
to explore ;; turtle procedure
  if explored? [ stop ]
  set explored? true
  set component-size component-size + 1
  ask link-neighbors [ explore ]
end
@#$#@#$#@
GRAPHICS-WINDOW
347
10
761
445
50
50
4.0
1
10
1
1
1
0
1
1
1
-50
50
-50
50
1
1
1
ticks
30.0

BUTTON
8
20
113
53
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
67
219
100
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
8
67
113
100
go once
go\nrepeat 3 [layout]
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
229
22
330
55
layout?
layout?
0
1
-1000

BUTTON
229
67
331
100
redo layout
layout
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
177
269
210
p
p
0.0
100.0
40
1.0
1
%
HORIZONTAL

SLIDER
10
236
267
269
q
q
0.0
100.0
65
1.0
1
%
HORIZONTAL

SLIDER
230
110
332
143
team-size
team-size
3
8
4
1
1
NIL
HORIZONTAL

TEXTBOX
14
158
229
177
probability of choosing an incumbent
11
0.0
0

TEXTBOX
11
218
277
236
probability of choosing a previous collaborator
11
0.0
0

PLOT
12
277
334
466
Link counts
time
cumulative count
0.0
300.0
0.0
10.0
true
false
"" ";; plot stacked histogram of link types\nlet total 0\nset-current-plot-pen \"previous collaborators\"\nplot-pen-up plotxy ticks total\nset total total + count links with [color = red]\nplot-pen-down plotxy ticks total\nset-current-plot-pen \"incumbent-incumbent\"\nplot-pen-up plotxy ticks total\nset total total + count links with [color = yellow]\nplot-pen-down plotxy ticks total\nset-current-plot-pen \"newcomer-incumbent\"\nplot-pen-up plotxy ticks total\nset total total + count links with [color = green]\nplot-pen-down plotxy ticks total\nset-current-plot-pen \"newcomer-newcomer\"\nplot-pen-up plotxy ticks total\nset total total + count links with [color = blue]\nplot-pen-down plotxy ticks total"
PENS
"newcomer-newcomer" 1.0 0 -13345367 true "" ""
"newcomer-incumbent" 1.0 0 -10899396 true "" ""
"incumbent-incumbent" 1.0 0 -1184463 true "" ""
"previous collaborators" 1.0 0 -2674135 true "" ""

SWITCH
122
21
222
54
plot?
plot?
0
1
-1000

SLIDER
8
110
220
143
max-downtime
max-downtime
7
100
40
1
1
NIL
HORIZONTAL

PLOT
795
35
1060
234
% of agents in the giant component
Time
% of all agents
0.0
10.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "plotxy ticks (giant-component-size / (count turtles))"

PLOT
795
257
1060
456
Average component size
Time
Number of agents
0.0
10.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" "plotxy ticks (mean components)"

@#$#@#$#@
## WHAT IS IT?

This model of collaboration networks illustrates how the behavior of individuals in assembling small teams for short-term projects can give rise to a variety of large-scale network structures over time.  It is an adaptation of the team assembly model presented by Guimera, Uzzi, Spiro & Amaral (2005).  The rules of the model draw upon observations of collaboration networks ranging from Broadway productions to scientific publications in psychology and astronomy.

Many of the general features found in the networks of creative enterprises can be captured by the Team Assembly model with two simple parameters: the proportion of newcomers participating in a team and the propensity for past collaborators to work again with one another.

## HOW IT WORKS

At each tick a new team is assembled.  Team members are either inexperienced "newcomers" — people who have not previously participated in any teams -- or are established "incumbents" — experienced people who have previously participated on a team.  Each member is chosen sequentially.  The P slider gives the probability that a new team member will be an incumbent.  If the new member is not a newcomer, then with a probability given by the Q slider, an incumbent will be chosen at random from the pool of previous collaborators of an incumbent already on the team.  Otherwise, a new member will just be randomly chosen from all incumbents.  When a team is created, all members are linked to one another.  If an agent does not participate in a new team for a prolonged period of time, the agent and her links are removed from the network.

Agents in a newly assembled team are colored blue if they are newcomers and yellow if they are incumbents.  Smaller grey circles represent those that are not currently collaborating.  Links indicate members' experience at their most recent time of collaboration.  For example, blue links between agents indicate that two agents collaborated as newcomers.  Green and yellow links correspond to one-time newcomer-incumbent and incumbent-incumbent collaborations, respectively.  Finally, red links indicate that agents have collaborated with one another multiple times.

## HOW TO USE IT

Click the SETUP button to start with a single team.  Click GO ONCE to assemble an additional team.  Click GO to indefinitely assemble new teams.  You may wish to use the GO ONCE button for the first few steps to get a better sense of how the parameters affect the assembly of teams.

### Visualization Controls
- LAYOUT?: controls whether or not the spring layout algorithm runs at each tick.  This procedure attempts to move the nodes around to make the structure of the network easier to see.  Switching off LAYOUT? will significantly increase the speed of the model.  
- PLOT?: switches on and off the plots. Again, off speeds up the model.

The REDO LAYOUT button lets you run the layout algorithm without assembling new teams.

### Parameters
- TEAM-SIZE: the number of agents in a newly assembled team.  
- MAX-DOWNTIME: the number of steps an agent will remain in the world without collaborating before it retires.  
- P: the probability an incumbent is chosen to become a member of a new team  
- Q: the probability that the team being assembled will include a previous collaborator of an incumbent on the team, given that the team has at least one incumbent.

### Plots
- LINK COUNTS: plots a stacked histogram of the number of links in the collaboration network over time.  The colors correspond to collaboration ties as follows:  
-- Blue: two newcomers  
-- Green: a newcomer and an incumbent  
-- Yellow: two incumbents that have not previously collaborated with one another  
-- Red: Repeat collaborators  
-  % OF AGENTS IN THE GIANT COMPONENT: plots the percentage of agents belonging to the largest connected component network over time.  
- AVERAGE COMPONENT SIZE: plots the average size of isolated collaboration networks as a fraction of the total number of agents

Using the plots, one can observe important features of the network, like the distribution of link types or the connectivity of the network vary over time.

## THINGS TO NOTICE

The model captures two basic features of collaboration networks that can influence or stifle innovation in creative enterprises by varying the values of P and Q.  First is the distribution of the type of the connection between collaborators, which can be seen in the LINK COUNTS plot. An overabundance of newcomer-newcomer (blue) links might indicate that a field is not taking advantage of experienced members. On the other hand, a multitude of repeat collaborations (red) and incumbent-incumbent (yellow) links may indicate a lack of diversity in ideas or experiences.

Second is the overall connectivity of the collaboration network.  For example, many academic fields are said to be comprised of an "invisible college" of loosely connected academic communities.  By contrast, patent networks tend to consist of isolated clusters or chains of inventors.  You can see one measure of this on the % OF AGENTS IN THE GIANT COMPONENT plot -- the giant component being the size of the largest connected chain of collaborators.

You can also see the different emergent topologies in the display.   New collaborations or synergy among teams naturally tend to the center of the display. Teams or clusters of teams with few connections to new collaborations naturally "float" to the edges of the world. Newcomers always start in the center of the world. Incumbents, which are chosen at random, may be located in any part of the screen. Thus, collaborations amongst newcomers and or distant team components tend toward the center, and disconnected clusters are repelled from the centered.

Finally, note that the structure of collaboration networks in the model can change dramatically over time. Initially, only new teams are generated; the collaborative field has not existed long enough for members to retire. However, after a period of time (MAX-DOWNTIME), inactive agents begin to retire, and the number of agents becomes relatively stable -- the emergent effects of P and Q become more apparent in this equilibrium stage.  Note also that the end of the growth stage is often marked by a drop in the connectivity of the network.

## THINGS TO TRY

Keeping Q fixed at 40%, how does the structure of collaboration networks vary with P?  For example, which values of P produce isolated clusters of agents?  As P increases, how do these clusters combine to form more complex structures?  Over which values of P does the transition from a disconnected network to a fully connected network occur?

Set P to 40% and Q to 100%, so that all incumbents choose to work with past collaborators.  Press SETUP, then GO, and let the model run for about 100 steps after the number of agents in the network stops growing.  What happens to the connectivity of the collaboration network?  Keeping P fixed, continue to lower Q in decrements of 5-10%.

Try keeping P and Q constant and varying TEAM-SIZE.  How does the global structure of the network change with larger or smaller team sizes?  Under which ranges of P and Q does this relation hold?

## EXTENDING THE MODEL

What happens when the size of new teams are not constant?  Try changing the rules so that team sizes vary randomly from a distribution or increase over time.

How do P and Q relate to the global clustering coefficient of the network?  You may wish to use code from the Small Worlds model in the Networks section of Sample Models.

Can you modify the model so that agents are more likely to collaborate with collaborators of collaborators?

Collaboration networks can alternatively be thought of as a network consisting of individuals linked to projects.  For example, one can represent a scientific journal with two types of nodes, scientists and publications.  Ties between scientists and publications represent authorship.  Thus, links between a publication multiple scientists specify co-authorship.  More generally, a collaborative project may be represented one type of node, and participants another type.  Can you modify the model to assemble teams using bipartite networks?

## RELATED MODELS

Preferential Attachment - gives a generative explanation of how general principles of attachment can give rise to a network structure common to many technological and biological systems.

Giant Component - shows how critical points exist in which a network can transition from a rather disconnected topology to a fully connected topology

## CREDITS AND REFERENCES

This model is based on:  
R Guimera, B Uzzi, J Spiro, L Amaral; Team Assembly Mechanisms Determine Collaboration Network Structure and Team Performance. Science 2005, V308, N5722, p697-702  
http://amaral.northwestern.edu/Publications/Papers/Guimera-2005-Science-308-697.pdf
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
NetLogo 5.0RC2
@#$#@#$#@
set layout? false
setup repeat 175 [ go ]
repeat 35 [ layout ]
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

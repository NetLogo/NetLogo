breed [ pages page ]
breed [ surfers surfer ]

pages-own [
  rank new-rank ; for the diffusion approach
  visits ; for the random-surfer approach
]

surfers-own [ current-page ]

globals [ total-rank max-rank ]

;;
;; Setup Procedures
;;

to setup
  clear-all
  set-default-shape pages "circle"

  ifelse network-choice = "Example 1"
  [ create-network-example-1 ][
    ifelse network-choice = "Example 2"
    [ create-network-example-2 ][
      ifelse network-choice = "Preferential Attachment"
      [ create-network-preferential 100 2 ]
      [ user-message word "Error: unknown network-choice: " network-choice ] ] ]

  ask patches [ set pcolor white ]
  ask pages
  [ set rank 1 / count pages ]
  update-globals
  ask pages
  [
    setxy random-xcor random-ycor
    set label-color black
    update-page-appearance
  ]

  repeat 300 [ do-layout ]

  ask links [ set shape "curved" ]
  reset-ticks
end

to create-network-example-1
  create-pages 11
  ask page 0 [ set color blue create-link-from page 3 ]
  ask page 1 [ set color red create-links-from (turtle-set page 2 page 3 page 4 page 5 page 6 page 7 page 8 ) ]
  ask page 2 [ set color orange create-link-from page 1 ]
  ask page 3 [ set color green create-link-from page 4 ]
  ask page 4 [ set color yellow create-links-from (turtle-set page 5 page 6 page 7 page 8 page 9 page 10) ]
  ask page 5 [ set color green create-link-from page 4 ]
  ask pages with [who > 5] [ set color violet ]
end

to create-network-example-2
  create-pages 8
  ask page 0 [ die ]
  ask page 1 [ create-links-from (turtle-set page 2 page 3 page 5 page 6) ]
  ask page 2 [ create-links-from (turtle-set page 1 page 3 page 4) ]
  ask page 3 [ create-links-from (turtle-set page 1 page 4 page 5) ]
  ask page 4 [ create-links-from (turtle-set page 1 page 5) ]
  ask page 5 [ create-links-from (turtle-set page 1 page 4 page 6 page 7) ]
  ask page 6 [ create-links-from (turtle-set page 5) ]
  ask page 7 [ create-links-from (turtle-set page 1) ]
end

to create-network-preferential [ n k ]
  create-pages n [ set color sky ]
  link-preferentially pages k
end

; The parameter k (always an integer) gives the number of edges to add at
; each step (e.g. k=1 builds a tree)
to link-preferentially [nodeset k]
  ;; get the nodes in sorted order
  let node-list sort nodeset

  ;; get a sublist of the nodes from 0 to k
  let neighbor-choice-list sublist node-list 0 k

  ;; ask the kth node...
  ask item k node-list
  [
    ;; to make a link either to or from each preceding
    ;; node in the sorted list.
    foreach neighbor-choice-list
    [
      ifelse random 2 = 0
      [ create-link-to ? ]
      [ create-link-from ? ]
    ]
    ;; add k copies of this node to the beginning of the sublist
    set neighbor-choice-list sentence (n-values k [self]) neighbor-choice-list
  ]

  ;; ask each node after the kth node in order...
  foreach sublist node-list (k + 1) (length node-list)
  [
    ask ?
    [
      ;; ...to make k links
      let temp-neighbor-list neighbor-choice-list
      repeat k
      [
        ;; link to one of the nodes in the neighbor list
        ;; we remove that node from the list once it's been linked to
        ;; however, there may be more than one copy of some nodes
        ;; since those nodes have a higher probability of being linked to
        let neighbor one-of temp-neighbor-list
        set temp-neighbor-list remove neighbor temp-neighbor-list
        ;; when we've linked to a node put another copy of it on the
        ;; master neighbor choice list as it's now more likely to be
        ;; linked to again
        set neighbor-choice-list fput neighbor neighbor-choice-list
        ifelse random 2 = 0
        [ create-link-to neighbor ]
        [ create-link-from neighbor ]
      ]
      set neighbor-choice-list sentence (n-values k [self]) neighbor-choice-list
    ]
  ]
end

to do-layout
  layout-spring pages links 0.2 20 / (sqrt count pages) 0.5
end

;;
;; Runtime Procedures
;;

to go
  ifelse calculation-method = "diffusion"
  [
    if any? surfers [ ask surfers [ die ] ] ;; remove surfers if the calculation-method is changed

    ;; return links and pages to initial state
    ask links [ set color gray set thickness 0 ]
    ask pages [ set new-rank 0 ]

    ask pages
    [
      ifelse any? out-link-neighbors
      [
        ;; if a node has any out-links divide current rank
        ;; equally among them.
        let rank-increment rank / count out-link-neighbors
        ask out-link-neighbors [
          set new-rank new-rank + rank-increment
        ]
      ]
      [
        ;; if a node has no out-links divide current
        ;; rank equally among all the nodes
        let rank-increment rank / count pages
        ask pages [
          set new-rank new-rank + rank-increment
        ]
      ]
    ]

    ask pages
    [
      ;; set current rank to the new-rank and take the damping-factor into account
      set rank (1 - damping-factor) / count pages + damping-factor * new-rank
    ]
  ]
  [ ;;; "random-surfer" calculation-method
    ; surfers are created or destroyed on the fly if users move the
    ; NUMBER-OF-SURFERS slider while the model is running.
    if count surfers < number-of-surfers
    [
      create-surfers number-of-surfers - count surfers
      [
        set current-page one-of pages
        ifelse watch-surfers?
        [ move-surfer ]
        [ hide-turtle ]
      ]
    ]
    if count surfers > number-of-surfers
    [
      ask n-of (count surfers - number-of-surfers) surfers
        [ die ]
    ]
    ;; return links to their initial state
    ask links [ set color gray set thickness 0 ]

    ask surfers [
      let old-page current-page
      ;; increment the visits on the page we're on
      ask current-page [ set visits visits + 1 ]
      ;; with a probability depending on the damping-factor either go to a
      ;; random page or a random one of the pages that this page is linked to
      ifelse random-float 1.0 <= damping-factor and any? [my-out-links] of current-page
      [ set current-page one-of [out-link-neighbors] of current-page ]
      [ set current-page one-of pages ]

      ;; update the visualization
      ifelse watch-surfers?
      [
        show-turtle
        move-surfer
        let surfer-color color
        ask old-page [
          let traveled-link out-link-to [current-page] of myself
          if traveled-link != nobody [
            ask traveled-link [ set color surfer-color set thickness 0.08 ]
          ]
        ]
      ]
      [ hide-turtle ]
    ]
    ;; update the rank of each page
    let total-visits sum [visits] of pages
    ask pages [
      set rank visits / total-visits
    ]
  ]

  update-globals
  ask pages [ update-page-appearance ]
  tick
end

to move-surfer ;; surfer procedure
  face current-page
  move-to current-page
end

to update-globals
  set total-rank sum [rank] of pages
  set max-rank max [rank] of pages
end

to update-page-appearance ;; page procedure
  ; keep size between 0.1 and 5.0
  set size 0.2 + 4 * sqrt (rank / total-rank)
  ifelse show-page-ranks?
  [ set label word (precision rank 3) "     " ]
  [ set label "" ]
end
@#$#@#$#@
GRAPHICS-WINDOW
255
10
685
461
10
10
20.0
1
10
1
1
1
0
0
0
1
-10
10
-10
10
1
1
1
ticks
30

BUTTON
15
95
90
128
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
170
95
240
128
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
25
165
225
198
damping-factor
damping-factor
0
1.00
0.85
0.01
1
NIL
HORIZONTAL

CHOOSER
25
235
225
280
calculation-method
calculation-method
"diffusion" "random-surfer"
1

SWITCH
35
370
215
403
watch-surfers?
watch-surfers?
0
1
-1000

SLIDER
25
285
225
318
number-of-surfers
number-of-surfers
1
100
5
1
1
NIL
HORIZONTAL

CHOOSER
15
35
240
80
network-choice
network-choice
"Example 1" "Example 2" "Preferential Attachment"
0

SWITCH
35
415
215
448
show-page-ranks?
show-page-ranks?
0
1
-1000

BUTTON
95
95
165
128
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

@#$#@#$#@
## WHAT IS IT?

PageRank is an algorithm/metric that was developed at Stanford University by Larry Page and Sergey Brin, who went on to create the Google search engine (and company) based on this method.  PageRank is a technique for ranking the relevancy of web pages on the internet, through analysis of the hyperlink structure that links pages together.

This model demonstrates two distinct (though related) agent-based methods for calculating the PageRank of interconnected web pages.  The use of an agent-based perspective attempts to provide a deeper understanding of this algorithm and the mathematics behind it.

Early web search engines often focused on the content of web pages, such as matching appropriate keywords or words found in the text of the page.  PageRank, on the other hand, is interested in ranking sites based on their general usefulness in the web (apart from any specific search query or topic).  Because Google uses PageRank as one component of its immensely popular internet search engine, it is easy to mistakenly call PageRank a search algorithm.  However, it is technically a ranking algorithm, which provides importance weights for each page in a network.  However, these rankings turn out to be very useful when performing an internet search, because they can be used to help determine the order in which search results are displayed to the user.  Suppose someone searches for "emu" -- there are millions of web sites which contain the word "emu".  Therefore, it is important to figure out which of those sites are more likely to provide the user with useful information.

## HOW IT WORKS

PageRank depends heavily on one basic premise about the structure of the world wide web: Web pages that are more useful to people will also be more popular, and will accordingly have more hyperlinks pointing to them from other web pages.

If this is true, a very simple approach to figure out which sites are most useful/important would be to count the number of incoming links to each page, and use that as a ranking score.  However, this would be assuming that every link counts equally, which is quite wrong.  A single link from a important web site (e.g. from yahoo.com or whitehouse.gov) should count for much more than a link from some little-known page that presumably no one seems interested in.  Thus, a page is important if many (and/or important) pages link to it.  This appears to be a rather circular definition of importance, and begs the question: how can we tell which pages are important to begin with?

PageRank handles this problem by initially ranking all pages as equally important, but then it repeatedly performs a process on the rank scores of the pages that will cause the importance rankings to change.  This PageRank NetLogo model presents two different ways of calculating PageRank, both of which would eventually converge to the exact same rankings being assigned to each web site, if you could let the algorithm run forever.

### Method 1: The "random-surfer" approach

Imagine that you have a small army of robotic random web surfers.  All they do is surf the web, going from one page to another, by randomly clicking on hyperlink after hyperlink.  They don't actually read the web pages, and they spend the same amount of time (perhaps 1 millisecond) at each page before moving on to a new page.  Occasionally instead of following a link, they choose to jump to a new page somewhere on the internet, chosen entirely at random.  (How often they do this is based on the DAMPING-FACTOR parameter) They will also do a random jump if they reach a dead-end web page that has no outgoing links.  But otherwise they are following links, and because we assume that links are more likely to lead to more important web sites, these random surfer robots are likely to spend more time at important pages than at the unimportant ones (which will have few incoming links).  For each web page, suppose you count up the number of times that some random surfer visited that page, then divide that number by the total number of pages that all the random surfers visited.  What you have calculated is the PageRank for that web site.  (In more formal mathematical terminology, the resulting PageRanks can be viewed as the stationary distribution for a certain "Markov chain", where each page is a state, and there are transitional probabilities specified between each pair of states based on the hyperlinks between them).

### Method 2:  The "diffusion" approach

In the previous approach, our primary agents were the robotic web surfers, while the web pages themselves were mostly passive agents, simply acting as counters, incrementing a number each time a robot browsed them.  In the "diffusion" approach, the web pages themselves are the central agents we are concerned with.  Each web page starts with some RANK value, which is a measure of how important it is in the network.  Initially, every page gets the same RANK value as every other page, and the sum of all the pages RANK values is 1.  Then, in each time step, every web page distributes its RANK value (importance) to those web sites that it has outgoing hyperlinks to.  Each page's new RANK value will thus be based on how much rank it receives from each of the sites that link to it, combined in a weighted average with a baseline amount of RANK value which each website gets each time step regardless of its neighbors.  (The weight of the baseline amount is determined by the DAMPING-FACTOR parameter.)  Over time, this process causes the RANK values of each page to converge to the actual PageRank values for each page.  (In more formal mathematical terminology, this method is similar to using the "power method" for finding the principal eigenvector associated with a modified adjacency matrix of the directed hyperlink graph.)

## HOW TO USE IT

First, you can decide what hyperlink network you would like to calculate the PageRank algorithm for, using the NETWORK-CHOICE chooser.  Choices include two simple example networks, or a larger network that is created using a "preferential attachment" algorithm.  The preferential attachment mechanism creates networks with scale-free degree distributions, which is one characteristic found to be true of the real world wide web.  (For more information,  see the Preferential Attachment model in the Models Library).

Then press SETUP to create the network.

Press GO to run the model, and watch as the PageRank is calculated.  The area of each node is roughly proportional to its PageRank value, and you can see the PageRank numbers if the SHOW-PAGE-RANKS? switch is turned ON.

The DAMPING-FACTOR slider controls a parameter of the PageRank algorithm that affects how much the rank values are affected purely by the link structure.  With DAMPING-FACTOR = 0, the link structure is completely damped and doesn't matter at all, and all pages will receive equal ranks regardless of who is linked to whom.  With DAMPING-FACTOR = 1, there is no damping of the effect of link structure, meaning that pages with no inbound hyperlinks will receive 0 for PageRanks.

For the "random-surfer" method, the DAMPING-FACTOR controls the probability that a surfer robot will follow a link, as opposed to jumping randomly to a new page in the web.  For the "diffusion" method, the DAMPING-FACTOR controls what fraction of a page's RANK value is determined by incoming links (as opposed to being given out gratis).

The CALCULATION-METHOD chooser controls whether the model will use the "random-surfer" or "diffusion" method to calculate the PageRank.

If the "random-surfer" method is chosen, the NUMBER-OF-SURFERS slider can be adjusted to change the number of surfing robots that are wandering the web.  If the WATCH-SURFERS? switch is ON, then you can watch the surfer robots move around, and each time a random surfer robot follows a hyperlink, that link will be colored the same color as the random surfer that just followed it, to help you visually follow the movement of the surfers.  If the WATCH-SURFERS? switch is OFF, then you will only see the PageRank values (and node sizes) adjusting over time, because the surfers are hidden.

Note: you may want to use the speed slider to slow down the model, so you can better examine what's happening.

## THINGS TO NOTICE

In the "Example 1" network, five of the pages have no inbound links, meaning that nobody else in the network links to them.  And yet, they usually still end up with a positive PageRank score.  Why is this?  Is there any scenario where they would end up with zero for their PageRank score?

Which calculation method ("random-surfer" or "diffusion") converges more quickly?  Is one tick of the diffusion method comparable to one tick of the random-surfer method?

Which calculation method do you think is more amenable to being extended (with the goal of developing a ranking algorithm that provides better relevancy than PageRank does)?  Why?

Is there an advantage to using more than one robot at a time when using the "random-surfer" method?  Do you think this algorithm could actually be run in parallel on very large networks?  Why or why not?

## THINGS TO TRY

It is fairly common for the damping factor for the PageRank algorithm to be set somewhere near 0.85.  Why do you think this is?   Why do you think the creators of the algorithm included a damping factor?  (What happens to the rankings if the damping factor is very low, or very high?)

When the damping-factor is set to 1.0, for some network configurations, the "diffusion" method and the "random-surfer" methods can arrive at different resulting PageRanks, and will never converge to the same thing, regardless of how long you let them run.  Why?

The two example networks (Example 1 and Example 2) are the same every time, but the "Preferential Attachment" network is randomly generated, so each time you set it up, it is very likely to be different.  How large is the PageRank of the most important page when you use the Preferential Attachment network?  How much does this fluctuate between when the network changes?

## EXTENDING THE MODEL

How does PageRank work if the whole network is not connected?  What if there are 2 (or more) separate components of the network, that can never reach each other by browsing through hyperlinks?  Extend this model by designing additional network configurations for the user to try out with the NETWORK-CHOICE chooser.

The random-surfer method offers an oversimplified view of how someone might surf the internet.  Given only the structural information about the network (and not any content, such as topics, keywords, etc), can you come up with any more realistic behavior for a surfer to follow?  Do you think this would produce better or worse relevancy measurements than the PageRank algorithm?

There are now many people who make a living by doing "search engine optimization" (SEO), to help improve the visibility of company web sites.  While some of the methods used to improve search engine scores are completely legitimate and legal, people will sometimes engage in more ethically questionable practices (so-called "black hat SEO"), essentially trying to "game the system" and artificially increase the PageRank of their sites.  Google (and other search engines) must spend considerable effort trying to counter tactics for unfairly manipulating search results.  How might you extend this model to consider some attempts at "gaming" the PageRank algorithm (such as creating new nodes or links).  You could also measure the effectiveness of these manipulations, and consider counter-measures to prevent them.

## NETLOGO FEATURES

While NetLogo has a built-in `diffuse` primitive that can be used with patches, there is no equivalent primitive for diffusing a value through a network.  However, it is not too difficult to write with NetLogo code, but we need to be careful to make sure everything updates at the same time, so that the total sum of PageRank values across the network remains constant.  This can be accomplished by having two `turtles-own` variables `rank` and `new-rank`.  First we compute the `new-rank` for each of the pages based on the old `rank` variable, before updating the `rank` variable of all the turtles using the `new-rank` value.  (You'll also see this kind of "synchronous updating" in cellular automata models such as "Life", as well as many other models.)

## RELATED MODELS

Preferential Attachment, Diffusion on a Directed Network, Link Walking Turtles Example (Code Example)

## CREDITS AND REFERENCES

The network configurations given in "Example 1" (with DAMPING-FACTOR 0.85) and "Example 2" (with DAMPING-FACTOR 1.0) are the same as the examples given in the figures of http://en.wikipedia.org/wiki/PageRank (as of January 2009).

See also: Page et al. (1998) "The PageRank Citation Ranking: Bringing Order to the Web." Technical report, Stanford Digital Library Technologies Project.
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

curved
1.0
-0.2 0 0.0 1.0
0.0 1 1.0 0.0
0.2 0 0.0 1.0
link direction
true
0
Line -7500403 true 150 150 105 210
Line -7500403 true 150 150 195 210

@#$#@#$#@
1
@#$#@#$#@

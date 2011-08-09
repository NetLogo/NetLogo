;;;;;;;;;;;;;;;;;;;;;;;;
;;; Setup Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;;

to setup
  clear-all
  set-default-shape turtles "circle"
  ;; make the initial network of two turtles and an edge
  make-node nobody        ;; first node, unattached
  make-node turtle 0      ;; second node, attached to first node
  reset-ticks
end

;;;;;;;;;;;;;;;;;;;;;;;
;;; Main Procedures ;;;
;;;;;;;;;;;;;;;;;;;;;;;

to go
  ;; new edge is green, old edges are gray
  ask links [ set color gray ]
  make-node find-partner         ;; find partner & use it as attachment
                                 ;; point for new node
  tick
  if layout? [ layout ]
end

;; used for creating a new node
to make-node [old-node]
  crt 1
  [
    set color red
    if old-node != nobody
      [ create-link-with old-node [ set color green ]
        ;; position the new node near its partner
        move-to old-node
        rt random 360
        fd 8
      ]
  ]
end

;; This code is borrowed from Lottery Example (in the Code Examples
;; section of the Models Library).
;; The idea behind the code is a bit tricky to understand.
;; Basically we take the sum of the degrees (number of connections)
;; of the turtles, and that's how many "tickets" we have in our lottery.
;; Then we pick a random "ticket" (a random number).  Then we step
;; through the turtles to figure out which node holds the winning ticket.
to-report find-partner
  let total random-float sum [count link-neighbors] of turtles
  let partner nobody
  ask turtles
  [
    let nc count link-neighbors
    ;; if there's no winner yet...
    if partner = nobody
    [
      ifelse nc > total
        [ set partner self ]
        [ set total total - nc ]
    ]
  ]
  report partner
end

;;;;;;;;;;;;;;
;;; Layout ;;;
;;;;;;;;;;;;;;

;; resize-nodes, change back and forth from size based on degree to a size of 1
to resize-nodes
  ifelse not any? turtles with [size > 1]
  [
    ;; a node is a circle with diameter determined by
    ;; the SIZE variable; using SQRT makes the circle's
    ;; area proportional to its degree
    ask turtles [ set size sqrt count link-neighbors ]
  ]
  [
    ask turtles [ set size 1 ]
  ]
end

to layout
  ;; the number 3 here is arbitrary; more repetitions slows down the
  ;; model, but too few gives poor layouts
  repeat 3 [
    layout-spring turtles links 0.5 1.0 0.4
  ]
end

to-report bad-intersections [t1 t2]
  let i intersection t1 t2
  if length i = 0
    [ report  false ]
  set i map [ precision ? 1 ] i
  if i = [list precision xcor 1 precision ycor 1] of end1
    [ report false ]
  if i = [list precision xcor 1 precision ycor 1] of end2
    [ report false ]
  show (word t1 " " t2 " " i)
  report true  
end

to-report intersection [t1 t2]
  let m1 [tan (90 - link-heading)] of t1
  let m2 [tan (90 - link-heading)] of t2
  ;; treat parallel/collinear lines as non-intersecting
  if m1 = m2 [ report [] ]
  ;; is t1 vertical? if so, swap the two turtles
  if abs m1 = tan 90
  [ 
    ifelse abs m2 = tan 90
      [ report [] ]
      [ report intersection t2 t1 ]
  ]
  ;; is t2 vertical? if so, handle specially
  if abs m2 = tan 90 [
     ;; represent t1 line in slope-intercept form (y=mx+c)
      let c1 [link-ycor - link-xcor * m1] of t1
      ;; t2 is vertical so we know x already
      let x [link-xcor] of t2
      ;; solve for y
      let y m1 * x + c1
      ;; check if intersection point lies on both segments
      if not [x-within? x] of t1 [ report [] ]
      if not [y-within? y] of t2 [ report [] ]
      report list x y
  ]
  ;; now handle the normal case where neither turtle is vertical;
  ;; start by representing lines in slope-intercept form (y=mx+c)
  let c1 [link-ycor - link-xcor * m1] of t1
  let c2 [link-ycor - link-xcor * m2] of t2
  ;; now solve for x
  let x (c2 - c1) / (m1 - m2)
  ;; check if intersection point lies on both segments
  if not [x-within? x] of t1 [ report [] ]
  if not [x-within? x] of t2 [ report [] ]
  report list x (m1 * x + c1)
end

to-report x-within? [x]  ;; turtle procedure
  report abs (link-xcor - x) <= abs (link-length / 2 * sin link-heading)
end

to-report y-within? [y]  ;; turtle procedure
  report abs (link-ycor - y) <= abs (link-length / 2 * cos link-heading)
end

to-report link-xcor
  report ([xcor] of end1 + [xcor] of end2) / 2
end

to-report link-ycor
  report ([ycor] of end1 + [ycor] of end2) / 2
end

@#$#@#$#@
GRAPHICS-WINDOW
345
10
810
496
45
45
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
-45
45
-45
45
1
1
1
ticks

BUTTON
6
25
72
58
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

BUTTON
93
64
170
97
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

BUTTON
6
64
91
97
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

SWITCH
187
64
333
97
layout?
layout?
1
1
-1000

MONITOR
237
100
316
145
# of nodes
count turtles
3
1
11

BUTTON
7
102
109
135
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

BUTTON
115
102
225
135
resize nodes
resize-nodes
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL

@#$#@#$#@
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

circle
false
0
Circle -7500403 true true 0 0 300

@#$#@#$#@
NetLogo 4.2pre1
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

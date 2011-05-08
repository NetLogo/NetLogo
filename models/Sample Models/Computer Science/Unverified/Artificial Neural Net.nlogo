links-own [weight]

breed [bias-nodes bias-node]
breed [input-nodes input-node]
breed [output-nodes output-node]
breed [hidden-nodes hidden-node]

turtles-own [activation err]

globals [
  epoch-error
  input-node-1   ;; keep the input and output nodes
  input-node-2   ;; in global variables so we can
  output-node-1  ;; refer to them directly
]

;;;
;;; SETUP PROCEDURES
;;;

to setup
  clear-all
  ask patches [ set pcolor gray + 2 ]
  set-default-shape bias-nodes "bias-node"
  set-default-shape input-nodes "circle"
  set-default-shape output-nodes "output-node"
  set-default-shape hidden-nodes "output-node"
  setup-nodes
  setup-links
  propagate
  reset-ticks
end

to setup-nodes
  create-bias-nodes 1 [ setxy -5 5 ]
  ask bias-nodes [ set activation 1 ]
  create-input-nodes 1
  [ setxy -5 -1
    set input-node-1 self ]
  create-input-nodes 1
  [ setxy -5 1
    set input-node-2 self ]
  ask input-nodes [ set activation random 2 ]
  create-hidden-nodes 1 [ setxy 0 -1 ]
  create-hidden-nodes 1 [ setxy 0 1 ]
  ask hidden-nodes
  [ set activation random 2
    set size 1.5 ]
  create-output-nodes 1
  [ setxy 5 0
    set output-node-1 self ]
  ask output-nodes [ set activation random 2 ]
end

to setup-links
  connect-all bias-nodes hidden-nodes
  connect-all bias-nodes output-nodes
  connect-all input-nodes hidden-nodes
  connect-all hidden-nodes output-nodes
end

to connect-all [nodes1 nodes2]
  ask nodes1 [
    create-links-to nodes2 [
      set weight random-float 0.2 - 0.1
    ]
  ]
end

to recolor
  ask turtles [
    set color item (step activation) [black white]
  ]
  ask links [
    set thickness 0.1 * abs weight
    ifelse weight > 0
      [ set color red ]
      [ set color blue ]
  ]
end

;;;
;;; TRAINING PROCEDURES
;;;

to train
  set epoch-error 0
  repeat examples-per-epoch [
    ask input-nodes [ set activation random 2 ]
    propagate
    back-propagate
  ]
  tick
  set epoch-error epoch-error / examples-per-epoch
  plotxy ticks epoch-error
end

;;;
;;; FUNCTIONS TO LEARN
;;;

to-report target-answer
  let a [activation] of input-node-1 = 1
  let b [activation] of input-node-2 = 1
  report ifelse-value run-result
    (word "a " target-function " b") [1][0]
end

;;;
;;; PROPAGATION PROCEDURES
;;;

;; carry out one calculation from beginning to end
to propagate
  ask hidden-nodes [ set activation new-activation ]
  ask output-nodes [ set activation new-activation ]
  recolor
end

to-report new-activation  ;; node procedure
  report sigmoid sum [[activation] of end1 * weight] of my-in-links
end

;; changes weights to correct for errors
to back-propagate
  let example-error 0
  let answer target-answer

  ask output-node-1 [
    set err activation * (1 - activation) * (answer - activation)
    set example-error example-error + ( (answer - activation) ^ 2 )
  ]
  set epoch-error epoch-error + example-error
  ask hidden-nodes [
    set err activation * (1 - activation) * sum [weight * [err] of end2] of my-out-links
  ]
  ask links [
    set weight weight + learning-rate * [err] of end2 * [activation] of end1
  ]
end

;;;
;;; MISC PROCEDURES
;;;

;; computes the sigmoid function given an input value and the weight on the link
to-report sigmoid [input]
  report 1 / (1 + e ^ (- input))
end

;; computes the step function given an input value and the weight on the link
to-report step [input]
  report ifelse-value (input > 0.5) [1][0]
end

;;;
;;; TESTING PROCEDURES
;;;

;; test runs one instance and computes the output
to test
  ;; output the result
  ifelse test-success? input-1 input-2
    [ user-message "Correct." ]
    [ user-message "Incorrect." ]
end

to-report test-success? [n1 n2]
  ask input-node-1 [ set activation n1 ]
  ask input-node-2 [ set activation n2 ]
  propagate
  report target-answer = step [activation] of one-of output-nodes
end
@#$#@#$#@
GRAPHICS-WINDOW
222
10
538
275
8
-1
18.0
1
10
1
1
1
0
0
0
1
-8
8
-5
7
1
1
1
ticks
30

BUTTON
133
31
214
64
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
126
85
211
118
train
train
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
557
64
620
97
test
test
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
556
105
648
150
input-1
input-1
0 1
1

CHOOSER
556
159
648
204
input-2
input-2
0 1
1

MONITOR
373
280
430
325
output
[precision activation 2] of output-nodes
3
1
11

SLIDER
14
128
214
161
learning-rate
learning-rate
0.0
1.0
0.2
1.0E-4
1
NIL
HORIZONTAL

PLOT
13
209
213
359
Error vs. Epochs
Epochs
Error
0.0
10.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

BUTTON
13
85
107
118
train once
train
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
14
168
213
201
examples-per-epoch
examples-per-epoch
1.0
1000.0
500
1.0
1
NIL
HORIZONTAL

CHOOSER
223
281
361
326
target-function
target-function
"or" "xor"
1

TEXTBOX
16
38
133
56
1. Setup Neural Net
11
0.0
0

TEXTBOX
15
63
109
81
2. Train Net
11
0.0
0

TEXTBOX
556
32
706
50
3. Test Net
11
0.0
0

@#$#@#$#@
## WHAT IS IT?

This is a model of a very small neural network.  It is based on the Perceptron model, but instead of one layer, this network has two layers of "perceptrons".  That means it can learn operations a single layer cannot.

The goal of a network is to take input from its input nodes on the far left and classify those inputs appropriately in the output nodes on the far right.  It does this by being given a lot of examples and attempting to classify them, and having a supervisor tell it if the classification was right or wrong.  Based on this information the neural network updates its weight until it correctly classifies all inputs correctly.

## HOW IT WORKS

Initially the weights on the links of the networks are random.  When inputs are fed into the network on the far left, those inputs times the random weights are added up to create the activation for the next node in the network.  The next node then sends out an activation along its output link.  These link weights and activations are summed up by the final output node which reports a value.  This activation is passed through a sigmoid function, which means that values near 0 are assigned values close to 0, and vice versa for 1.  The values increase nonlinearly between 0 and 1 with a sharp transition at 0.5.

To train the network a lot of inputs are presented to the network along with how the network should correctly classify the inputs.  The network uses a back-propagation algorithm to pass error back from the output node and uses this error to update the weights along each link.

## HOW TO USE IT

To use it press SETUP to create the network and initialize the weights to small random numbers.

Press TRAIN ONCE to run one epoch of training.  The number of examples presented to the network during this epoch is controlled by EXAMPLES-PER-EPOCH slider.

Press TRAIN to continually train the network.

In the view, the larger the size of the link the greater the weight it has.  If the link is red then its a positive weight.  If the link is blue then its a negative weight.

To test the network, set INPUT-1 and INPUT-2, then press the TEST button.  A dialog box will appear telling you whether or not the network was able to correctly classify the input that you gave it.

LEARNING-RATE controls how much the neural network will learn from any one example.

TARGET-FUNCTION allows you to choose which function the network is trying to solve.

## THINGS TO NOTICE

Unlike the Perceptron model, this model is able to learn both OR and XOR.  It is able to learn XOR because the hidden layer (the middle nodes) in a way allows the network to draw two lines classifying the input into positive and negative regions.  As a result one of the nodes will learn essentially the OR function that if either of the inputs is on it should be on, and the other node will learn an exclusion function that if both of the inputs or on it should be on (but weighted negatively).

However unlike the perceptron model, the neural network model takes longer to learn any of the functions, including the simple OR function.  This is because it has a lot more that it needs to learn.  The perceptron model had to learn three different weights (the input links, and the bias link).  The neural network model has to learn ten weights (4 input to hidden layer weights, 2 hidden layer to output weight and the three bias weights).

## THINGS TO TRY

Manipulate the LEARNING-RATE parameter.  Can you speed up or slow down the training?

Switch back and forth between OR and XOR several times during a run.  Why does it take less time for the network to return to 0 error the longer the network runs?

## EXTENDING THE MODEL

Add additional functions for the network to learn beside OR and XOR.  This may require you to add additional hidden nodes to the network.

Back-propagation using gradient descent is considered somewhat unrealistic as a model of real neurons, because in the real neuronal system there is no way for the output node to pass its error back.  Can you implement another weight-update rule that is more valid?

## NETLOGO FEATURES

This model uses the link primitives.  It also makes heavy use of lists.

## RELATED MODELS

This is the second in the series of models devoted to understanding artificial neural networks.  The first model is Perceptron.

## CREDITS AND REFERENCES

The code for this model is inspired by the pseudo-code which can be found in Tom M. Mitchell's "Machine Learning" (1997).

Thanks to Craig Brozefsky for his work in improving this model.
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

bias-node
false
0
Circle -16777216 true false 0 0 300
Circle -7500403 true true 30 30 240
Polygon -16777216 true false 120 60 150 60 165 60 165 225 180 225 180 240 135 240 135 225 150 225 150 75 135 75 150 60

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

link
true
0
Line -7500403 true 150 0 150 300

link direction
true
0

output-node
false
1
Circle -7500403 true false 0 0 300
Circle -2674135 true true 30 30 240
Polygon -7500403 true false 195 75 90 75 150 150 90 225 195 225 195 210 195 195 180 210 120 210 165 150 120 90 180 90 195 105 195 75

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
setup repeat 100 [ train ]
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

@#$#@#$#@
0
@#$#@#$#@

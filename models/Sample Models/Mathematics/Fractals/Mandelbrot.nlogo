turtles-own
[
  previous-count  ; the patch color of the previous patch- used in the turtles' movement
]

patches-own
[
  c-real       ; real portion of the constant complex number c
  c-imaginary  ; imaginary portion of the constant complex number c
  z-real       ; real portion of the complex number z
  z-imaginary  ; imaginary portion of the complex number z
  counter      ; keeps track of the color that the patch is supposed to be
]

;;; Initialization Procedures

to setup
  clear-all
  setup-turtles
  setup-patches
  reset-ticks
end

to setup-turtles
  crt num-turtles
  [
    set color green
    setxy random-xcor random-ycor
    set size 3  ;; easier to see
  ]
end

to setup-patches
  ask patches
  [
    ; set the real portion of c to be the x coordinate of the patch
    set c-real (pxcor / scale-factor)
    ; set the imaginary portion of c to be the y coordinate of the patch
    set c-imaginary (pycor / scale-factor)
    ; have the initial value of z be 0 + 0i
    set z-real 0
    set z-imaginary 0
    set counter 0
  ]
end

;;; Run-Time Procedures

to go
  mandelbrot-calc-and-color
  step
  wiggle
  climb
  tick
end

; calculate the equation of the mandelbrot fractal for each patch with a turtle on it and change
; its color to be an appropriate color.
to mandelbrot-calc-and-color
  ; if the distance of a patch's z from the origin (0,0) is less than 2 and its counter is less
  ; than 256 perform another iteration the equation f(z) = z^2 + c.
  ask turtles with [(modulus z-real z-imaginary <= 2.0) and (counter < 256)]
  [
    let temp-z-real z-real
    set z-real c-real + (rmult z-real z-imaginary z-real z-imaginary)
    set z-imaginary c-imaginary + (imult temp-z-real z-imaginary temp-z-real z-imaginary)
    set counter counter + 1
    set pcolor counter
  ]
end

;ask each turtle to move forward by 1
to step
  ask turtles
  [ ifelse can-move? 1
    [ fd 1 ]
    [ setxy random-xcor random-ycor ] ]
end

;ask each turtle to change its direction slightly
to wiggle
  ask turtles
  [
    rt random 10
    lt random 10
  ]
end

;ask the turtles to climb up the counter gradient
to climb
  ask turtles
  [
    ifelse counter >= previous-count
    [
      set previous-count counter
      set color yellow
      jump throw
    ]
    [
      set previous-count counter
      set color blue
      rt 180
    ]
  ]
end

;;; Real and Imaginary Arithmetic Operators

to-report rmult [real1 imaginary1 real2 imaginary2]
  report real1 * real2 - imaginary1 * imaginary2
end

to-report imult [real1 imaginary1 real2 imaginary2]
  report real1 * imaginary2 + real2 * imaginary1
end

to-report modulus [real imaginary]
  report sqrt (real ^ 2 + imaginary ^ 2)
end
@#$#@#$#@
GRAPHICS-WINDOW
220
10
832
643
150
150
2.0
1
10
1
1
1
0
0
0
1
-150
150
-150
150
1
1
1
ticks
30

BUTTON
42
50
108
83
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
110
50
176
83
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
6
98
215
131
num-turtles
num-turtles
0
1500
1000
1
1
NIL
HORIZONTAL

SLIDER
6
148
215
181
throw
throw
0.0
99.0
30
1.0
1
NIL
HORIZONTAL

SLIDER
6
199
215
232
scale-factor
scale-factor
1
200
100
1
1
NIL
HORIZONTAL

BUTTON
59
254
161
287
hide turtles
hide-turtle
NIL
1
T
TURTLE
NIL
NIL
NIL
NIL
1

@#$#@#$#@
## WHAT IS IT?

This model draws a mathematical object called the Mandelbrot set, named after its discoverer, Benoit Mandelbrot.  It demonstrates an interesting technique for generating the design as well as providing a nice example of hill climbing.

A number of fractal generation turtles do a random walk across a complex plane, computing one additional iteration of F(Z) = Z<sup>2</sup> + C each time they cross a patch, where C is the coordinates of the patch, and Z is a complex number from the result of the last iteration.  A count is maintained of the number of iterations computed at each patch before the complex number at that patch reaches a maximum.  This count is then translated into a color, giving the Mandelbrot set its distinctive look.

An interesting way to view the emerging set is that you are looking straight down on one of the Hawaiian Islands.  The center is extremely high (infinitely so, in fact), simply because no fixed number of iterations at these points will cause the associated complex number to reach a pre-determined maximum.  The edges of the set are steeply sloped, and the "sea" around the set is very shallow.

## HOW IT WORKS

In case you are not familiar with complex numbers, here as an introduction to what they are and how to calculate with them.

In this model, the world becomes a complex plane.  This plane is similar to the real or Cartesian plane that people who have taken an algebra course in middle school or high school should be familiar with.  The real plane is the combination of two real lines placed perpendicularly to each other.  Each point on the real plane can be described by a pair of numbers such as (0,0) or (12,-6).  The complex plane is slightly different from the real plane in that there is no such thing as a complex number line.  Each point on a complex plane can still be thought of as a pair of numbers, but the pair has a different meaning.  Before we describe this meaning, let us describe what a complex number looks like and how it differs from a real one.

As you may know, a complex number is made up of two parts, a real number and an imaginary number.  Traditionally, a complex number is written as 4 + 6i or -7 - 17i.  Sometimes, a complex number can be written in the form of a pair, (4,6) or (-7,-17).  In general, a complex number could be written as a + bi or (a,b) in the other way of writing complex numbers, where both a and b are real numbers.  So, basically a complex number is two real numbers added together with one of them multiplied by i.  You are probably asking yourself, what is this i?  i is called the imaginary number and is a constant equivalent to the square root of -1.

Getting back to the complex plane, it is now easier to see, if we use the paired version of writing complex numbers described above, that we let the real part of the complex number be the horizontal coordinate (x coordinate) and the imaginary part be the vertical coordinate (y coordinate).  Thus, the complex number 5 - 3i would be located at (5,-3) on the complex plane.  Thus, since the patches make up a complex plane, in each patch, the pxcor corresponds to the real part and the pycor corresponds to the imaginary part of a complex number.  A quick word on complex arithmetic and you will be set to understand this model completely.

Two complex numbers are added or subtracted by combining the real portions and then combining the imaginary portions.  For example, if we were to add the two complex numbers 4 + 9i and -3 + 11i, we would get 1 + 20i, since 4 - 3 = 1 and 9 + 11 = 20.  If we were to subtract the first number from the second number, we would get -7 + 2i, since -3 - 4 = -7 and 11 - 9 = 2.  Multiplication is a bit harder to do.  Just remember three things.  First, remember that i * i = -1.  Second, be sure to follow the addition and subtraction rules supplied above.  Third, remember this scheme First Outside Inside Last or FOIL for short.  In other words, you multiply the first parts of each number, add this to the product of the outside two parts of each number, add this to the product of the inside two parts of each number, and add this to the product of the last two parts of each number.  In general, this means given two complex numbers a + bi and c + di, we would multiply the numbers in the following manner:

(a * c) + (a * di) + (bi * c) + (bi * di) = ((a * c) - (b * d)) + ((a * d) + (b * c))i

If we were to multiply the same two numbers from above, we would get -12 + 44i - 27i - 99 = -111 + 17i, since 4 * -3 = -12, 4 * 11i = 44i, 9i * -3 = -27i, and 9i * 11i = -99.

## HOW TO USE IT

Click on SETUP to create NUM-TURTLES fractal generation turtles, place them in the middle of the world (at complex coordinate (0,0)), and scale the 101,101 world to approx -1 to 1 on both the real and complex planes.

To start the calculation, start the slider THROW at 0, press the GO button. Note that the system seems to stall, with each turtle "stuck" on a local maximum hill.

Changing THROW to 7 will "throw" each turtle a distance of 7 each time they reach the top of a hill, essentially giving them a second chance to climb an even greater hill.  The classic Mandelbrot shape will begin to appear fairly quickly.

The slider SCALE-FACTOR scales the fractal so that you can see more or less of it.  The higher the value, the less of the entire fractal you will see.  Be aware that you sacrifice resolution for the price of being able to see more of the fractal.

## THINGS TO NOTICE

Notice that the "aura" around the Mandelbrot set begins to appear first, then the details along the edges become more and more crisply defined.  Finally, the center fills out and slowly changes to black.

Notice how different values for THROW change the speed and precision of the project. Also, try running the model with different values for NUM-TURTLES.

## THINGS TO TRY

It's fairly easy to hack at the NetLogo code to change the scale factor --- it's set as the global variable `factor`.  You might also think about adjusting the viewport in the plane, to allow for a larger picture (although the smaller sized picture might look better and emerge quicker).

You might also play with the colors to experiment with different visual effects.

Notice also what happens when you turn off climbing and/or wiggling.

## EXTENDING THE MODEL

Try to produce some of the other complex sets --- the Julia set for instance.  There are many other fractals commonly known today.  Just about any book on them will have several nice pictures you can try to duplicate.

## NETLOGO FEATURES

To accomplish the hill climbing, the code uses `current-count` and `previous-count` turtle variables, comparing them to one another to establish a gradient to guide turtle movement.  The goal of each turtle is to move up the emerging gradient, "booting itself up" to the ever growing center of the set.

Note that complex arithmetic is not built in to NetLogo, so the basic operations needed to be provided as NetLogo routines at the end of the code. These complex arithmetic routines are also used in other fractal calculations and can be tailored to your own explorations.

## CREDITS AND REFERENCES

You may find more information on fractals in the following locations:  
This site offers an introduction to fractals.  
http://www.cs.wpi.edu/~matt/courses/cs563/talks/cbyrd/pres1.html

An introduction to complex mathematics and the Mandelbrot set.  
http://www.olympus.net/personal/dewey/mandelbrot.html

An introductory online textbook for Complex Analysis.  
(Note: This is a college level text, but the first chapter or so should be accessible to people with only some algebra background.)  
http://www.math.gatech.edu/~cain/winter99/complex.html

_The Fractal Geometry of Nature_ by Benoit Mandelbrot
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
setup
repeat 1200 [ go ]
ask turtles [ ht ]
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

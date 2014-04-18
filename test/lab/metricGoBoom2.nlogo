to setup
  clear-all
  create-turtles 1
  reset-ticks
end

to go
  if not any? turtles [ stop ]
  if ticks = 10 [ ask turtles [ die ] ]
  tick
end
@#$#@#$#@
GRAPHICS-WINDOW
210
10
649
470
16
16
13.0
1
10
1
1
1
0
1
1
1
-16
16
-16
16
0
0
1
ticks
30.0

@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
NetLogo 5.0
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
<experiments>
  <experiment name="experiment" repetitions="100" runMetricsEveryStep="true">
    <setup>setup</setup>
    <go>go</go>
    <timeLimit steps="20"/>
    <metric>mean [who] of turtles</metric>
  </experiment>
</experiments>
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@

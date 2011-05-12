breed [breed0s breed0]
breed [breed1s breed1]
breed [breed2s breed2]
breed [breed3s breed3]
breed [breed4s breed4]
breed [breed5s breed5]
breed [breed6s breed6]
breed [breed7s breed7]
breed [breed8s breed8]
breed [breed9s breed9]
breed [breed10s breed10]
breed [breed11s breed11]
breed [breed12s breed12]
breed [breed13s breed13]
breed [breed14s breed14]
breed [breed15s breed15]
breed [breed16s breed16]
breed [breed17s breed17]
breed [breed18s breed18]
breed [breed19s breed19]
breed [breed20s breed20]
breed [breed21s breed21]
breed [breed22s breed22]
breed [breed23s breed23]
breed [breed24s breed24]
breed [breed25s breed25]
breed [breed26s breed26]
breed [breed27s breed27]
breed [breed28s breed28]
breed [breed29s breed29]
breed [breed30s breed30]
breed [breed31s breed31]
breed [breed32s breed32]
breed [breed33s breed33]
breed [breed34s breed34]
breed [breed35s breed35]
breed [breed36s breed36]
breed [breed37s breed37]
breed [breed38s breed38]
breed [breed39s breed39]
breed [breed40s breed40]
breed [breed41s breed41]
breed [breed42s breed42]
breed [breed43s breed43]
breed [breed44s breed44]
breed [breed45s breed45]
breed [breed46s breed46]
breed [breed47s breed47]
breed [breed48s breed48]
breed [breed49s breed49]
breed [breed50s breed50]
breed [breed51s breed51]
breed [breed52s breed52]
breed [breed53s breed53]
breed [breed54s breed54]
breed [breed55s breed55]
breed [breed56s breed56]
breed [breed57s breed57]
breed [breed58s breed58]
breed [breed59s breed59]
breed [breed60s breed60]
breed [breed61s breed61]
breed [breed62s breed62]
breed [breed63s breed63]
breed [breed64s breed64]
breed [breed65s breed65]
breed [breed66s breed66]
breed [breed67s breed67]
breed [breed68s breed68]
breed [breed69s breed69]
breed [breed70s breed70]
breed [breed71s breed71]
breed [breed72s breed72]
breed [breed73s breed73]
breed [breed74s breed74]
breed [breed75s breed75]
breed [breed76s breed76]
breed [breed77s breed77]
breed [breed78s breed78]
breed [breed79s breed79]
breed [breed80s breed80]
breed [breed81s breed81]
breed [breed82s breed82]
breed [breed83s breed83]
breed [breed84s breed84]
breed [breed85s breed85]
breed [breed86s breed86]
breed [breed87s breed87]
breed [breed88s breed88]
breed [breed89s breed89]
breed [breed90s breed90]
breed [breed91s breed91]
breed [breed92s breed92]
breed [breed93s breed93]
breed [breed94s breed94]
breed [breed95s breed95]
breed [breed96s breed96]
breed [breed97s breed97]
breed [breed98s breed98]
breed [breed99s breed99]
breed [breed100s breed100]
breed [breed101s breed101]
breed [breed102s breed102]
breed [breed103s breed103]
breed [breed104s breed104]
breed [breed105s breed105]
breed [breed106s breed106]
breed [breed107s breed107]
breed [breed108s breed108]
breed [breed109s breed109]
breed [breed110s breed110]
breed [breed111s breed111]
breed [breed112s breed112]
breed [breed113s breed113]
breed [breed114s breed114]
breed [breed115s breed115]
breed [breed116s breed116]
breed [breed117s breed117]
breed [breed118s breed118]
breed [breed119s breed119]
breed [breed120s breed120]
breed [breed121s breed121]
breed [breed122s breed122]
breed [breed123s breed123]
breed [breed124s breed124]
breed [breed125s breed125]
breed [breed126s breed126]
breed [breed127s breed127]
breed [breed128s breed128]
breed [breed129s breed129]
breed [breed130s breed130]
breed [breed131s breed131]
breed [breed132s breed132]
breed [breed133s breed133]
breed [breed134s breed134]
breed [breed135s breed135]
breed [breed136s breed136]
breed [breed137s breed137]
breed [breed138s breed138]
breed [breed139s breed139]
breed [breed140s breed140]
breed [breed141s breed141]
breed [breed142s breed142]
breed [breed143s breed143]
breed [breed144s breed144]
breed [breed145s breed145]
breed [breed146s breed146]
breed [breed147s breed147]
breed [breed148s breed148]
breed [breed149s breed149]
breed [breed150s breed150]
breed [breed151s breed151]
breed [breed152s breed152]
breed [breed153s breed153]
breed [breed154s breed154]
breed [breed155s breed155]
breed [breed156s breed156]
breed [breed157s breed157]
breed [breed158s breed158]
breed [breed159s breed159]
breed [breed160s breed160]
breed [breed161s breed161]
breed [breed162s breed162]
breed [breed163s breed163]
breed [breed164s breed164]
breed [breed165s breed165]
breed [breed166s breed166]
breed [breed167s breed167]
breed [breed168s breed168]
breed [breed169s breed169]
breed [breed170s breed170]
breed [breed171s breed171]
breed [breed172s breed172]
breed [breed173s breed173]
breed [breed174s breed174]
breed [breed175s breed175]
breed [breed176s breed176]
breed [breed177s breed177]
breed [breed178s breed178]
breed [breed179s breed179]
breed [breed180s breed180]
breed [breed181s breed181]
breed [breed182s breed182]
breed [breed183s breed183]
breed [breed184s breed184]
breed [breed185s breed185]
breed [breed186s breed186]
breed [breed187s breed187]
breed [breed188s breed188]
breed [breed189s breed189]
breed [breed190s breed190]
breed [breed191s breed191]
breed [breed192s breed192]
breed [breed193s breed193]
breed [breed194s breed194]
breed [breed195s breed195]
breed [breed196s breed196]
breed [breed197s breed197]
breed [breed198s breed198]
breed [breed199s breed199]

to go
  clear-all
  file-close
  carefully [ file-delete "coords.txt" ] [ ]
  file-open "coords.txt"
  file-print "["
  repeat size-of-test [
    file-write (list random-breed random-xcor random-ycor)
    file-print ""
  ]
  file-print "]"
  file-close
  output-print "done writing file"
  no-display
  reset-timer
  file-open "coords.txt"
  let coords file-read
  output-print word "done reading file: " timer
  file-close
  foreach coords [
    crt 1 [ set breed runresult item 0 ? setxy item 1 ? item 2 ? ]
  ]
  output-print word "done: " timer
  output-print word "count turtles: " count turtles
  output-print word "count breed195s: " count breed195s
  ct
  display
end

to-report random-breed
  report (word "breed" random 200 "s")
end
@#$#@#$#@
GRAPHICS-WINDOW
282
10
721
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
1
0
1
ticks

BUTTON
75
100
199
138
NIL
go
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL

OUTPUT
11
174
268
328
12

CHOOSER
67
50
205
95
size-of-test
size-of-test
10 100 1000 10000 100000 300000
5

@#$#@#$#@
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
NetLogo 4.0beta5
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

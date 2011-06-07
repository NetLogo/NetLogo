extensions [ gis ]
globals [ cities-dataset
          rivers-dataset
          countries-dataset
          elevation-dataset ]
breed [ city-labels city-label ]
breed [ country-labels country-label ]
breed [ country-vertices country-vertex ]
breed [ river-labels river-label ]
patches-own [ population country-name elevation ]

to setup
  clear-turtles
  clear-patches
  clear-drawing
  clear-all-plots
  clear-output
  ; Note that setting the coordinate system here is optional, as
  ; long as all of your datasets use the same coordinate system.
  gis:load-coordinate-system (word "data/" projection ".prj")
  ; Load all of our datasets
  set cities-dataset gis:load-dataset "data/cities.shp"
  set rivers-dataset gis:load-dataset "data/rivers.shp"
  set countries-dataset gis:load-dataset "data/countries.shp"
  set elevation-dataset gis:load-dataset "data/world-elevation.asc"
  ; Set the world envelope to the union of all of our dataset's envelopes
  gis:set-world-envelope (gis:envelope-union-of (gis:envelope-of cities-dataset)
                                                (gis:envelope-of rivers-dataset)
                                                (gis:envelope-of countries-dataset)
                                                (gis:envelope-of elevation-dataset))
end

; Drawing point data from a shapefile, and optionally loading the
; data into turtles, if label-cities is true
to display-cities
  ask city-labels [ die ]
  foreach gis:feature-list-of cities-dataset
  [ gis:set-drawing-color scale-color red (gis:property-value ? "POPULATION") 5000000 1000
    gis:fill ? 2.0
    if label-cities
    [ ; a feature in a point dataset may have multiple points, so we
      ; have a list of lists of points, which is why we need to use
      ; first twice here
      let location gis:location-of (first (first (gis:vertex-lists-of ?)))
      ; location will be an empty list if the point lies outside the
      ; bounds of the current NetLogo world, as defined by our current
      ; coordinate transformation
      if not empty? location
      [ create-city-labels 1
        [ set xcor item 0 location
          set ycor item 1 location
          set size 0
          set label gis:property-value ? "NAME" ] ] ] ]
end

; Drawing polyline data from a shapefile, and optionally loading some
; of the data into turtles, if label-rivers is true
to display-rivers
  ask river-labels [ die ]
  gis:set-drawing-color blue
  gis:draw rivers-dataset 1
  if label-rivers
  [ foreach gis:feature-list-of rivers-dataset
    [ let centroid gis:location-of gis:centroid-of ?
      ; centroid will be an empty list if it lies outside the bounds
      ; of the current NetLogo world, as defined by our current GIS
      ; coordinate transformation
      if not empty? centroid
      [ create-river-labels 1
          [ set xcor item 0 centroid
            set ycor item 1 centroid
            set size 0
            set label gis:property-value ? "NAME" ] ] ] ]
end

; Drawing polygon data from a shapefile, and optionally loading some
; of the data into turtles, if label-countries is true
to display-countries
  ask country-labels [ die ]
  gis:set-drawing-color white
  gis:draw countries-dataset 1
  if label-countries
  [ foreach gis:feature-list-of countries-dataset
    [ let centroid gis:location-of gis:centroid-of ?
      ; centroid will be an empty list if it lies outside the bounds
      ; of the current NetLogo world, as defined by our current GIS
      ; coordinate transformation
      if not empty? centroid
      [ create-country-labels 1
        [ set xcor item 0 centroid
          set ycor item 1 centroid
          set size 0
          set label gis:property-value ? "CNTRY_NAME" ] ] ] ]
end

; Loading polygon data into turtles connected by links
to display-countries-using-links
  ask country-vertices [ die ]
  foreach gis:feature-list-of countries-dataset
  [ foreach gis:vertex-lists-of ?
    [ let previous-turtle nobody
      let first-turtle nobody
      ; By convention, the first and last coordinates of polygons
      ; in a shapefile are the same, so we don't create a turtle
      ; on the last vertex of the polygon
      foreach but-last ?
      [ let location gis:location-of ?
        ; location will be an empty list if it lies outside the
        ; bounds of the current NetLogo world, as defined by our
        ; current GIS coordinate transformation
        if not empty? location
        [ create-country-vertices 1
          [ set xcor item 0 location
            set ycor item 1 location
            ifelse previous-turtle = nobody
            [ set first-turtle self ]
            [ create-link-with previous-turtle ]
            set hidden? true
            set previous-turtle self ] ] ]
      ; Link the first turtle to the last turtle to close the polygon
      if first-turtle != nobody and first-turtle != previous-turtle
      [ ask first-turtle
        [ create-link-with previous-turtle ] ] ] ]
end

; Using gis:intersecting to find the set of patches that intersects
; a given vector feature (in this case, a river).
to display-rivers-in-patches
  ask patches [ set pcolor black ]
  ask patches gis:intersecting rivers-dataset
  [ set pcolor cyan ]
end

; Using gis:apply-coverage to copy values from a polygon dataset
; to a patch variable
to display-population-in-patches
  gis:apply-coverage countries-dataset "POP_CNTRY" population
  ask patches
  [ ifelse (population > 0)
    [ set pcolor scale-color red population 500000000 100000 ]
    [ set pcolor blue ] ]
end

; Using find-one-of to find a particular VectorFeature, then using
; gis:intersects? to do something with all the features from another
; dataset that intersect that feature.
to draw-us-rivers-in-green
  let united-states gis:find-one-feature countries-dataset "CNTRY_NAME" "United States"
  gis:set-drawing-color green
  foreach gis:feature-list-of rivers-dataset
  [ if gis:intersects? ? united-states
    [ gis:draw ? 1 ] ]
end

; Using find-greater-than to find a list of VectorFeatures by value.
to highlight-large-cities
  let united-states gis:find-one-feature countries-dataset "CNTRY_NAME" "United States"
  gis:set-drawing-color yellow
  foreach gis:find-greater-than cities-dataset "POPULATION" 10000000
  [ gis:draw ? 3 ]
end

; Drawing a raster dataset to the NetLogo drawing layer, which sits
; on top of (and obscures) the patches.
to display-elevation
  gis:paint elevation-dataset 0
end

to display-elevation-in-patches
  ; This is the preferred way of copying values from a raster dataset
  ; into a patch variable: in one step, using gis:apply-raster.
  gis:apply-raster elevation-dataset elevation
  ; Now, just to make sure it worked, we'll color each patch by its
  ; elevation value.
  let min-elevation gis:minimum-of elevation-dataset
  let max-elevation gis:maximum-of elevation-dataset
  ask patches
  [ ; note the use of the "<= 0 or >= 0" technique to filter out
    ; "not a number" values, as discussed in the documentation.
    if (elevation <= 0) or (elevation >= 0)
    [ set pcolor scale-color black elevation min-elevation max-elevation ] ]
end

; This is a second way of copying values from a raster dataset into
; patches, by asking for a rectangular sample of the raster at each
; patch. This is somewhat slower, but it does produce smoother
; subsampling, which is desirable for some types of data.
to sample-elevation-with-patches
  let min-elevation gis:minimum-of elevation-dataset
  let max-elevation gis:maximum-of elevation-dataset
  ask patches
  [ set elevation gis:raster-sample elevation-dataset self
    if (elevation <= 0) or (elevation >= 0)
    [ set pcolor scale-color black elevation min-elevation max-elevation ] ]
end

; This is an example of how to select a subset of a raster dataset
; whose size and shape matches the dimensions of the NetLogo world.
; It doesn't actually draw anything; it just modifies the coordinate
; transformation to line up patch boundaries with raster cell
; boundaries. You need to call one of the other commands after calling
; this one to see its effect.
to match-cells-to-patches
  gis:set-world-envelope gis:raster-world-envelope elevation-dataset 0 0
  cd
  ct
end

; This command also demonstrates the technique of creating a new, empty
; raster dataset and filling it with values from a calculation.
;
; This command uses the gis:convolve primitive to compute the horizontal
; and vertical Sobel gradients of the elevation dataset, then combines
; them using the square root of the sum of their squares to compute an
; overall "image gradient". This is really more of an image-processing
; technique than a GIS technique, but I've included it here to show how
; it can be easily done using the GIS extension.
to display-gradient-in-patches
  let horizontal-gradient gis:convolve elevation-dataset 3 3 [ 1 0 -1 2 0 -2 1 0 -1 ] 1 1
  let vertical-gradient gis:convolve elevation-dataset 3 3 [ 1 2 1 0 0 0 -1 -2 -1 ] 1 1
  let gradient gis:create-raster gis:width-of elevation-dataset gis:height-of elevation-dataset gis:envelope-of elevation-dataset
  let x 0
  repeat (gis:width-of gradient)
  [ let y 0
    repeat (gis:height-of gradient)
    [ let gx gis:raster-value horizontal-gradient x y
      let gy gis:raster-value vertical-gradient x y
      if ((gx <= 0) or (gx >= 0)) and ((gy <= 0) or (gy >= 0))
      [ gis:set-raster-value gradient x y sqrt ((gx * gx) + (gy * gy)) ]
      set y y + 1 ]
    set x x + 1 ]
  let min-g gis:minimum-of gradient
  let max-g gis:maximum-of gradient
  gis:apply-raster gradient elevation
  ask patches
  [ if (elevation <= 0) or (elevation >= 0)
    [ set pcolor scale-color black elevation min-g max-g ] ]
end
@#$#@#$#@
GRAPHICS-WINDOW
185
10
775
333
72
36
4.0
1
8
1
1
1
0
1
1
1
-72
72
-36
36
0
0
1
ticks
30.0

BUTTON
5
100
175
133
NIL
display-cities
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
5
260
175
293
NIL
display-countries
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
5
60
175
93
NIL
setup\n
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
5
140
175
173
label-cities
label-cities
1
1
-1000

SWITCH
5
300
175
333
label-countries
label-countries
1
1
-1000

BUTTON
5
180
175
213
NIL
display-rivers
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
5
220
175
253
label-rivers
label-rivers
1
1
-1000

BUTTON
5
340
175
373
NIL
display-rivers-in-patches
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
5
380
175
413
NIL
display-population-in-patches
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
185
340
365
373
NIL
draw-us-rivers-in-green
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
185
380
365
413
NIL
highlight-large-cities
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
5
10
175
55
projection
projection
"WGS_84_Geographic" "US_Orthographic" "Lambert_Conformal_Conic"
0

BUTTON
5
420
175
453
NIL
display-countries-using-links
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
395
340
580
373
NIL
display-elevation
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
395
420
580
453
NIL
sample-elevation-with-patches
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
395
380
580
413
NIL
display-elevation-in-patches
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
590
380
775
413
NIL
match-cells-to-patches
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
590
340
775
373
NIL
display-gradient-in-patches
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
185
420
365
453
NIL
clear-drawing
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
## VERSION

$Id: GIS General Examples.nlogo 47711 2011-01-20 15:50:16Z russell734 $

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
NetLogo 5.0beta3
@#$#@#$#@
setup
display-cities
display-countries
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

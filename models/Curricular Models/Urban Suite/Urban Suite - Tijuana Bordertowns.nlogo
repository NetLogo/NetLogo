breed [ employment-centers employment-center ]
breed [ service-centers service-center ]
breed [ builders builder ]
breed [ second-order-builders second-order-builder ]
breed [ third-order-builders third-order-builder ]
breed [ migrants migrant ]
breed [ rings ring ]
breed [ nodes node ]


migrants-own [ income savings works origin resident-since living-expenses ]
patches-own [ land-value water electricity occupied transport ctr elevation colonia]
builders-own [ too-close energy weight block-size origin]
second-order-builders-own [ energy weight origin]
third-order-builders-own [ energy weight origin ]

globals [ values weights marginal-value marginal-weight crossed third-phase max-cost]

to setup
  reset-ticks
  tick

  generate-cityscape

  if count employment-centers > 0 and count migrants = 0
     [ initial-condition ]

  if count migrants > 0 and count migrants with [ size = .65 ] > 0
     [ ask patches [ set land-value precision (land-value) 2 ] initial-condition-2 ]

  if third-phase = "go"
     [ economics ]

  if third-phase = "stop"
     [ ask patches [ set land-value precision (land-value) 2 ]
       reset-ticks stop ]
end

to generate-cityscape

    if ticks = 1
      [ generate-topography
        let center-region n-of #-service-centers patches with
           [abs pxcor < ( max-pxcor - 20 ) and
            abs pycor < ( max-pycor - 20 ) ]
         ask patches [ set water 10.0 set electricity 1.0 set occupied "no" set transport 1.5 ]
         ask center-region
            [ set land-value 35 ask neighbors [ set land-value 35 ]
              ask patches in-radius-nowrap 20 [ set elevation 9.9 ]
              set water 2.0 set electricity 3.0 set transport 1.0 ask neighbors [set water 2.0 set electricity 3.0 set transport 1.0]
              sprout-service-centers 1 [set shape "eyeball" set size 5 set color 4 ]
              sprout-builders round (random-normal 4 1)
            [ set label (who + 1)
              set heading random-normal (label * (360 / count builders )) 20
              set energy random-normal 75 15
              set block-size round (random-normal 2 .15)
              set weight .9
              set pen-size 1.5 pd ] ]

    ask service-centers [ let my-roads builders with [ xcor = [xcor] of myself and ycor = [ycor] of myself ]
                          ask my-roads [ set origin (list myself) ] ]

    towards-other-centers ]

    ask builders
      [set too-close []
       fd 1
       set color 6
       if ( ticks mod 20 = 0 ) [ set heading random-normal heading 3 ]
       set weight weight - .02
       set weight precision weight 3
       set land-value (weight * 12)
       set transport .75
       set ctr "+"
       ask patches in-radius-nowrap 8 [ set ctr "+" ] ]

     ask builders [if ticks = block-size [ generate-second-order ]
                   if ticks = 3 * block-size [ generate-second-order ]
                   if ticks = 7 * block-size [ generate-second-order ]
                   if ticks = 10 * block-size [ generate-second-order ]
                   if [transport] of (patch-ahead 2 ) = .75
                        or [transport] of (patch-right-and-ahead 1 2) = .75
                        or [transport] of (patch-left-and-ahead 1 2) = .75
                        [fd 2 die]    ]

     let first-phase-limit round (random-normal 100 10)


     ask second-order-builders [
               set label ""
               if energy != 0 [ fd .5 ]
               pd
               set color 7
               set pen-size 1
               set weight weight - .001
               set weight precision weight 3
               set land-value (weight * 7)
               set water 2.0 set electricity 3.0 set transport 1.0
               if weight < 0 [ set weight 0 ]
               if round heading mod 15 < 5 or round heading mod 15 > 10
                   [ set heading heading + round (random-normal 5 10) ]

               if ( ctr = "+" ) and (distance-nowrap one-of service-centers) < 60
                   [ set energy energy - .5 ]
               if ctr != "+"
                   [ set energy energy - 1 ]
               if ctr != "+" and (distance-nowrap one-of service-centers) > 60
                   [ set energy energy - 2 ]
               if energy < .5 [ set energy 0 ]

               if energy = 0 [ die ]

               if [elevation] of (patch-ahead 1 ) < 9.9
                        [fd .5 die]

               if [land-value] of (patch-ahead 1 ) != 0
                        or [land-value] of (patch-right-and-ahead 1 1) != 0
                        or [land-value] of (patch-left-and-ahead 1 1) != 0
                        [fd .5 die]

               if [transport] of (patch-ahead 1 ) = .75
                        or [transport] of (patch-right-and-ahead 1 1) = .75
                        or [transport] of (patch-left-and-ahead 1 1) = .75
                        [fd 1 die]

               if ticks < first-phase-limit [ if energy = 0 [ die ] ]

               if ticks mod round (random-normal 5 1) = 0 and energy != 0
                 [ hatch-third-order-builders 1 [set heading ([heading] of myself + 90) set energy (random-normal [energy] of myself 2) + 2]
                   hatch-third-order-builders 1 [set heading ([heading] of myself - 90) set energy (random-normal [energy] of myself 2) + 2] ] ]


    ask third-order-builders [
               set label ""
               if energy != 0 [ fd .5 ]
               pd
               set color 7
               set pen-size 1
               set weight weight - .0007
               set weight precision weight 3
               set land-value (weight * 7)

               ifelse weight > .3
                   [ set water 2.0 set electricity 3.0 set transport 1.0 ask patches in-radius 2 [ set electricity 3.0 ] ]
                   [ set water 2.0 set electricity 3.0 set transport 1.0 ]

               if weight < 0 [ set weight 0 ]
               if round heading mod 15 < 5 or round heading mod 15 > 10
                   [ set heading heading + round (random-normal 5 10) ]

               if ctr = "+" and (distance-nowrap one-of service-centers) < 60
                   [ set energy energy - .5 ]
               if ctr != "+"
                   [ set energy energy - 1 ]
               if (distance-nowrap one-of service-centers) > 60
                   [ set energy energy - 2 ]
               if energy < .5 [ set energy 0 ]

               if ticks mod round (random-normal 3.75 .25) = 0 and energy != 0
                  [ hatch 1 [set heading ([heading] of myself + 90) set energy (random-normal [energy] of myself 2) + 2 ]
                    hatch 1 [set heading ([heading] of myself - 90) set energy (random-normal [energy] of myself 2) + 2 ] ]

                      if [elevation] of (patch-ahead 1 ) < 9.9
                      [die]

                      if [transport] of (patch-ahead 1 ) = .75
                        or [transport] of (patch-right-and-ahead 1 1) = .75
                        or [transport] of (patch-left-and-ahead 1 1) = .75
                        [fd .5 die]

                      if [land-value] of (patch-ahead 1 ) != 0
                        or [land-value] of (patch-right-and-ahead 1 1) != 0
                        or [land-value] of (patch-left-and-ahead 1 1) != 0
                        [fd 1 die]    ]

    ask builders with [ round abs xcor = max-pxcor or round abs ycor = max-pycor] [die]
    ask second-order-builders with [ round abs xcor = max-pxcor or round abs ycor = max-pycor] [die]
    ask third-order-builders with [ round abs xcor = max-pxcor or round abs ycor = max-pycor] [die]

    if ticks > 10 and count third-order-builders != 0 [
    set values ( mean [ land-value ] of third-order-builders  )
    set weights ( mean [ weight ] of third-order-builders ) ]

    if count builders = 0 and ( count third-order-builders with [ energy != 0 ] ) = 0 and count third-order-builders != 0
      [ no-display
        adjust
        display ]

    if count employment-centers > 0 [ stop ]
end


to adjust
    repeat 7 [diffuse land-value 1]
    ask patches with [ land-value < 0 ] [ set land-value 0 ]
    ask patches [ set land-value precision ( land-value ) 2 ]
    set values ( mean [ land-value ] of third-order-builders )
    set marginal-value ( mean [ land-value ] of third-order-builders )
    set weights ( mean [ weight ] of third-order-builders )
    set marginal-weight ( mean [ weight ] of third-order-builders )
    ask third-order-builders [ die ]
    if #-service-centers > 1 [ ask service-centers [ set size round (land-value - 3)
        if size < 2 [ set size 2 ] ] ]
    maquiladoras
    stop
end


to generate-topography
    ask patches [ set elevation 9.9 ]
    let topography n-of ((random 4) + 2) patches with
            [abs pxcor < ( max-pxcor - 20 ) and
             abs pycor < ( max-pycor - 20 ) ]
    repeat 3 [ ask topography [ set elevation 8 ask n-of 15 patches in-radius-nowrap (random-normal 15 3)
                  [ set elevation 8 ] ask one-of neighbors
                  [ set elevation 8 ask n-of 13 patches in-radius-nowrap (random-normal 8 2)
                  [ set elevation 8 ] ] ] ]
    repeat 2 [ ask patches with [ (count neighbors with [ elevation = 8 ]) > 2 ] [ set elevation 8 ] ]
    ask patches with [ elevation = 8 and (count neighbors with [ elevation = 8 ]) < 2 ] [ set elevation 9.9 ]
    repeat 1 [ diffuse elevation 1 ]
end


 to towards-other-centers
     if #-service-centers > 1 [ ask service-centers
                        [ let my-roads builders with [ (member? myself ([origin] of self)) = true ]
                          ask one-of my-roads
                               [ set heading towards-nowrap one-of service-centers with
                               [ (member? self ( [origin] of myself )) = false ] ] ] ]
 end




 to generate-second-order
     hatch-second-order-builders 1 [set heading heading + 90 set energy (random [energy] of myself) ]
     hatch-second-order-builders 1 [set heading heading - 90 set energy (random [energy] of myself) ]
 end





to maquiladoras
    let roads patches with [ transport = .75 ]
    let industrial-candidate patches with
                    [ land-value < .75 and land-value > .05
                      and abs pycor < (max-pycor - 5)
                      and abs pxcor < (max-pxcor - 5)
                      and [land-value] of (max-one-of neighbors [land-value]) < .6 and land-value > .015
                      and transport != .75
                      and elevation = 9.9
                      and ( count roads with [ distance-nowrap myself < 6 ] > 0 )
                      and ( count roads with [ distance-nowrap myself < 3 ] = 0 )
                      and transport != .75
                      ]

    if ( count industrial-candidate < #-maquiladoras )
       [ user-message "Warning: unable to create all employment centers, insufficient candidate patches." ]

    create-employment-centers #-maquiladoras [ set shape "factory"
                    set label ""
                    set color 95
                    set size 7
                    let industrial-site one-of industrial-candidate
                        with [ count employment-centers with [ distance-nowrap myself < 5 ] = 0 ]
                    if industrial-site != nobody
                    [ setxy ( [pxcor] of industrial-site ) ( [pycor] of industrial-site ) ]
                    set water "0.0"
                    set electricity "0.0"
                    set transport "0.0" ]

    ask employment-centers [ ask employment-centers with
                    [ self != myself and xcor = [xcor] of myself and ycor = [ycor] of myself ] [ die ] ]

    ;ask employment-centers with [ count roads with [ distance-nowrap myself < 6 ] = 0 ] [ die ]

    ask industrial-candidate
                    with [ ( any? employment-centers-here ) = true ]
                   [set occupied "maquiladora" ask neighbors [ set occupied "maquiladora"
                    ask neighbors [ set occupied "maquiladora" ] ] ]

end





 to initial-condition
      let potential-irregular-lots patches with
          [occupied = "no" and occupied != "maquiladora" and transport != .75 and not any? employment-centers in-radius 10
            and elevation > 8.5
            and land-value <= ( values - .35  )
            and land-value > ( values ) / 45
            and (count patches with [ water = 2 and (distance-nowrap myself) < 6 ]) > 0 ]
      ask potential-irregular-lots [ set ctr "p-i-lots" ]

      let floating-population ( round (((count patches with [ water = 2 and electricity = 3]) * init-density) / 6) )

      ask n-of ( floating-population / 5 ) potential-irregular-lots
          [sprout-migrants 1
                [ set size .65
                  set shape "circle"
                  set origin one-of ["oaxaca" "jalisco" "zacatecas" "mexico"]
                  set occupied [origin] of self
                  set works [] set (works) lput (min-one-of employment-centers [ distance-nowrap myself ]) (works)
                  set resident-since round (random-normal 8 2)
                  ]]
      stop
end




to initial-condition-2
      if count migrants with [ size = .65 ] != 0 [
      ask one-of migrants with [ size = .65 ]
                  [ set size .6
                    repeat 4 [ ask one-of patches in-radius-nowrap 3 with [ occupied = "no" and occupied != "maquiladora"
                    and elevation > 8.5 ]
                    [ sprout-migrants 1 [
                    set shape "circle"
                    set size .6
                    set color red
                    set origin one-of [ "oaxaca" "jalisco" "zacatecas" "mexico"]
                    set occupied [origin] of self
                    set color red
                    set works [] set (works) lput (min-one-of employment-centers [ distance-nowrap myself ]) (works)
                    set resident-since round (random-normal 8 2)
                    get-costs ] ] ] ] ]

      ask migrants [ ifelse color-code? = true
      [ if origin = "oaxaca" [set color pink]
        if origin = "jalisco" [set color orange]
        if origin = "zacatecas" [set color blue]
        if origin = "mexico" [set color green] ]
        [ set color black ] ]

      ask migrants [ set occupied [origin] of self ]
      if (count migrants with [ size = .65 ]) = 0 [ set third-phase "go" stop ]
end

to economics
   ask migrants [ ask patches in-radius-nowrap (4 - (colonia-size) / 2) [ set colonia colonia - .5 ]
   get-costs
   set max-cost max [ living-expenses ] of migrants
   set income precision (random-normal max-cost 3) 2
   set income precision (income) 2 set living-expenses precision (living-expenses) 2 ]
   ask migrants with [ income < living-expenses ] [ while [ income < living-expenses ] [set living-expenses living-expenses - 1 ] ]
   if (count migrants with [ income < living-expenses ]) = 0
   [ set third-phase "stop" stop ]
end

to update-display
     if visual-update = "off" [ ask patches [set pcolor white] ]
     if visual-update = "land value gradient" [ ask patches [set pcolor land-value] ]
     if visual-update = "units with no water" [ ask patches with [ water = 10 and (any? migrants-here) = true ]
                                        [ set pcolor 67 ] ]
     if visual-update = "units with no electricity" [ ask patches with [ electricity = 1 and (any? migrants-here) = true ]
                                              [ set pcolor 127 ] ]
     if visual-update = "elevation" [ask patches [ set pcolor elevation + 50 ] ]
     if visual-update = "colonias" [ ask patches with [ colonia = 0 ] [ set colonia 9.9 ]
                          ask patches with [ colonia != 0 and colonia > -10 ] [set pcolor colonia + 10]
                          ask patches with [ colonia < -9.9 ] [ set pcolor 0 ] ]

  ifelse color-code? = true
  [ ask migrants [
      if origin = "oaxaca" [set color pink]
      if origin = "jalisco" [set color orange]
      if origin = "zacatecas" [set color blue]
      if origin = "mexico" [set color green] ] ]
  [ ifelse visual-update = "land value gradient"
      [ ask migrants with [ shape = "circle" ] [ set color white ] ]
      [ ask migrants with [ shape = "circle" ] [ set color black ] ] ]

end




to go
    tick
    if count third-order-builders != 0 [
      set values ( mean [ land-value ] of third-order-builders )
      set weights ( mean [ weight ] of third-order-builders ) ]
    set max-cost max [ living-expenses ] of migrants

    update-display
    ask migrants [set savings precision (savings) 2 set living-expenses precision (living-expenses) 2 ]
    ;ask migrants [ set size (size + 1 / (savings + 1)) ]

    ;show "About to migrate"
    if ( ticks mod migration-ticks = 0 ) [migrate-static]


    ask migrants [ set occupied [origin] of self ]


    if ( ticks mod 15 = 0 )
        [ ask migrants [set resident-since resident-since + 1 set resident-since round resident-since ] ]



    ;show "About to grow the city"
    ifelse city-growth? = true [ ask third-order-builders [ set hidden? false ] ]
                              [ ask third-order-builders [ set hidden? true ] ]

    ;show "About to regulate"
    if city-growth? = true [ regulate ]

    ;show "About to call grow-city"
    if city-growth? = true [ if ( ticks mod building-ticks = 0  ) [ grow-city ] ]

    ;show "About to call on third-order builders"
    if ( city-growth? = true and ticks mod ( building-ticks * 2 ) = 0 ) [ ask third-order-builders [
                hatch 1 [set heading ([heading] of myself + 90) set weight marginal-weight ]
                hatch 1 [set heading ([heading] of myself - 90) set weight marginal-weight ] ] ]
    ;show "About to update patch values"
    ask patches [
      if ( land-value < 0 ) [ set land-value 0 ]
      if ( not any? migrants-here and occupied != "maquiladora" ) [ set occupied "no" ]
      if ( colonia > -.1 ) [ set colonia 9.9 ]
    ]

    ask turtles with [ round abs xcor = max-pxcor or round abs ycor = max-pycor] [die]

    ;show "About to regularize"
    regularize

    ;show "About to cross borders"
    cross

    ;show "About to earn and spend"
    ask migrants [ earn-spend ]

    ;show "About to move"
    move
end




to migrate-static
    ask one-of migrants
        [ let mypatch patch-here
          let newcomer one-of patches in-radius-nowrap 1.5 with
            [ occupied = "no"
              and elevation > 8.5
              and land-value <= [land-value] of mypatch ]
          if newcomer != nobody
          [ hatch 1 [ setxy ( [pxcor] of newcomer ) ( [pycor] of newcomer ) ]
            set occupied [origin] of self
            set shape "circle"
            set resident-since 0
            get-costs
            set income precision (random-normal 55 3) 2
            set savings precision (random-normal 15 5) 2
            get-costs
            ask patches in-radius-nowrap (4 - colonia-size) [ set colonia colonia - .5 ] ] ]
end


to move
    ask migrants with [ resident-since > 5  ]
    [ if water = 10.0 and savings > required-capital
    [ let somewhere-nice one-of patches in-radius-nowrap 50 with
        [ water = 2.0 and electricity = 3.0
          and land-value < ([savings] of myself * 0.0010) ]
      if somewhere-nice != nobody [
          face-nowrap somewhere-nice
          jump distance-nowrap somewhere-nice
          set color red set size .8
          set savings ( savings - ( land-value / 0.0010) )
          get-costs ] ] ]
end



to cross
    if ticks mod crossing-ticks = 0
      [ ask one-of migrants [ set color 108 set size 15 die ] set crossed crossed + 1 ]
end



to regularize

    let colonias one-of patches with [ colonia < (-14 - colonia-size)
                                       and (any? migrants-on neighbors) = true
                                       and pxcor < (max-pxcor - 4) and pycor < (max-pycor - 4)
                                       and (count rings with [ distance-nowrap myself < 15 ]) = 0
                                     ]

    if (count rings) < 5 and colonias != nobody
        [ ask colonias
                   [ sprout-rings 1 [
                     set color 27
                     set shape "loop" ] ] ]



    ask rings [ set size abs ((mean [ colonia ] of (migrants in-radius-nowrap 10 )) ^ 2 ) / 3
               if size < 3 [ die ]
               if (count patches in-radius-nowrap 8 with [ water = 10 ]) < 5  [ die ]
               if (count third-order-builders in-radius-nowrap 6) < 1
                       [ let buildit
                               one-of patches in-radius-nowrap size with
                             [ (abs (land-value) - values) < .25
                               and any? migrants-here = true
                               and water = 10
                               and not any? third-order-builders-here ]
                         if buildit != nobody [ ask buildit
                             [ sprout-third-order-builders 1 [ set size 1
                                              set color red
                                              set weight marginal-weight
                                              face-nowrap min-one-of migrants [ distance-nowrap myself ]
                                              set origin myself] ] ] ] ]
end



to grow-city
    ask third-order-builders [
         fd .75
         pd
         set pen-size 1
         set weight weight - .05
         set weight precision weight 3
         ask other migrants-here [ get-costs ]
         set land-value (weight * 3)
         set water 2.0
         set electricity 3.0
         set transport 1.0
         set colonia colonia + .15 ask neighbors [ set colonia (colonia + .15) ] ]

end

to regulate
    ask third-order-builders [
        if [transport] of (patch-ahead 1) = .75
             or [transport] of (patch-right-and-ahead 1 1) = .75
             or [transport] of (patch-left-and-ahead 1 1) = .75
             [fd 1 die]

        if [land-value] of (patch-ahead 1) > land-value
             or [land-value] of (patch-right-and-ahead 1 1) > land-value
             or [land-value] of (patch-left-and-ahead 1 1) > land-value
             [fd .5 die]

        if [occupied] of (patch-ahead 1) = "maquiladora"
              [ die ]

        ;if ( count third-order-builders-on patch-ahead 1 > 0 )
        ;      [ die ]

        if not any? migrants-on patch-ahead 1
              [ die ]

        if not any? rings in-radius 8
              [ die ] ]
end




to earn-spend
      set savings precision (savings + income - living-expenses) 2
      if savings < 7 [ set savings 0.0 ]
end




to get-costs
       let food 27.0
       let other-utilities 8.0
       let my-workplace one-of employment-centers with [ ( member? self ( [works] of myself )  ) = true ]
       let transport-costs ( transport * ( ( distance-nowrap one-of service-centers / 30 ) +
                                             distance-nowrap my-workplace / 25 ) )

       set living-expenses ( land-value + electricity + water + food + transport-costs + other-utilities )
end
@#$#@#$#@
GRAPHICS-WINDOW
276
10
970
501
85
57
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
-85
85
-57
57
1
1
1
ticks

SLIDER
5
150
170
183
#-maquiladoras
#-maquiladoras
1
8
8
1
1
NIL
HORIZONTAL

SLIDER
5
185
170
218
#-service-centers
#-service-centers
1
3
3
1
1
NIL
HORIZONTAL

BUTTON
175
10
249
43
3  go
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
75
10
170
43
2  cityscape
setup
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
5
10
70
43
1  clear
without-interruption\n[ no-display\nca\nask patches [ set pcolor white ]\ndisplay ]
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
5
80
170
113
migration-ticks
migration-ticks
1
20
3
1
1
ticks
HORIZONTAL

CHOOSER
5
410
145
455
visual-update
visual-update
"off" "elevation" "land value gradient" "colonias" "units with no water" "units with no electricity"
0

SWITCH
150
418
265
451
color-code?
color-code?
0
1
-1000

SWITCH
5
460
145
493
city-growth?
city-growth?
1
1
-1000

MONITOR
276
505
336
550
builders
count third-order-builders
3
1
11

MONITOR
341
505
396
550
values
values
2
1
11

SLIDER
5
360
170
393
building-ticks
building-ticks
0.5
6
6
0.5
1
NIL
HORIZONTAL

SLIDER
5
220
170
253
init-density
init-density
0.5
1
0.7
0.1
1
NIL
HORIZONTAL

MONITOR
531
505
589
550
no water
( ( count patches with [ (any? migrants-here) = \ntrue and water = 10.0\n ] ) * 100) / \n((count patches with [ water = 2.0 and \nelectricity = 3.0]) * init-density)\n
0
1
11

MONITOR
596
505
661
550
no power
( ( count patches with [ occupied != \"no\" and \noccupied != \"maquiladora\" and electricity = 1.0\n ] ) * 100) / \n((count patches with [ water = 2.0 and \nelectricity = 3.0]) * init-density)\n
0
1
11

MONITOR
666
505
771
550
irregular settlers
count migrants
3
1
11

SLIDER
5
290
170
323
colonia-size
colonia-size
0
2
2
0.25
1
NIL
HORIZONTAL

SLIDER
5
115
170
148
crossing-ticks
crossing-ticks
1
20
15
1
1
ticks
HORIZONTAL

MONITOR
401
505
461
550
marginal
marginal-value
1
1
11

MONITOR
776
505
836
550
NIL
crossed
0
1
11

MONITOR
466
505
524
550
NIL
weights
3
1
11

BUTTON
5
45
70
78
inspect
inspect min-one-of migrants [ income ]\ninspect one-of migrants with [ savings != 0 ]\ninspect one-of patches with [ count migrants-here > 0]\n
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
5
255
170
288
required-capital
required-capital
150
1200
500
10
1
NIL
HORIZONTAL

BUTTON
75
45
170
78
add node
if mouse-down? \n    [  ask patch mouse-xcor mouse-ycor [\n       sprout-nodes 1 [ set size 3 set shape \"loop\" \n       set color red\n       set water 2.0 set electricity 3.0 set transport 1.0\n       set land-value (weights)\n       ask patches in-radius-nowrap carrying-capacity with\n     [ occupied != \"maquiladora\" and land-value < marginal-value ]\n        [ set water 2.0 set electricity 3.0 set transport 1.0\n          set land-value (weights) \n          - (((distance-nowrap min-one-of nodes [ \n           distance-nowrap myself ]) / carrying-capacity) * .15) \n           ] ] ]\n       display ]
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
5
325
170
358
carrying-capacity
carrying-capacity
1
6
6
1
1
NIL
HORIZONTAL

MONITOR
5
506
115
551
savings
mean [ savings  ] of migrants with [ size = .6 ]
0
1
11

MONITOR
121
506
238
551
savings node
mean [ savings  ] of migrants with [ size = .8 ]
3
1
11

BUTTON
150
460
241
493
update-now
update-display
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

This model simulates various socio-economic realities of low-income residents of the City of Tijuana for the purpose of creating propositional design interventions.

The Tijuana Bordertowns model allows input of migration rates and border crossing rates relative to employment and service centers in order to define population types (i.e. migrants or full-time residents). Population densities relative to block sizes are adjustable via the interface in order to steer the simulation toward specific land-uses such as urban centers or peripheral (rural) development. The rate of residential building is adjustable based upon relative community size, land value (approximate), required (per-capita) capital and the carrying capacity of (potentially) existing infrastructure.

The model also simulates detailed information about each agent in the simulation. During or after the simulation is run, specific agents can be inspected to determine their current income (if any), savings (if any), living expenses, job (if any), origin (from where they migrated) and the time (in years) they have lived in Tijuana. In addition, during or after the simulation is run, patches the agents occupy can also be inspected to identify the existence of infrastructural features such as water, electricity and roads. In all cases, information about agents and patches continually changes during the simulation based upon interrelated feedback.

## HOW IT WORKS

A CITYSCAPE is generated, spreading out from a city center.  Each patch is assigned a land-value, and a level of electrical, water and transportation service.  A road network is drawn, and maquiladoras are placed at the edge of the city.

An initial set of migrants are created at the edge of the city on "irregular" patches, meaning those patches with a low land-value, near water and away from maquiladoras.  A second set of migrants is created in the neighboring patches.  This establishes the base population of the irregular settlements.  The migrants also have a state of original, such as Jaliso or Oaxaca.

Then each migrant is assigned a living-expanses values, which is determined by the value of the land they occupy, food and other utility costs, as well as the electrical, water, and transportation.  Food and other utility costs are constant for all agents.  The electrical and water costs are determined by the patches values.  Transportation is determined by their distance to service centers (like shopping centers), the distance to the maquiladoras they work at, and the access to transportation services their patch has.

With each model tick, new migrants move into patches next to existing migrants, some migrants cross the border, some migrants move into nicer locations once they have sufficient savings, and all of them participate in the economy, earning and spending money, as well as saving if possible.

New migrants will enter in unoccupied spaces adjacent to migrants who came from their home state.

Migrants that move will look for a patch in their area with electrical and water services, which is affordable to them.

Groups of migrants form colonias.  The size of these colonias is determined by the COLONIA-SIZE slider.  The larger the value, the larger the colonia.  Colonias with sufficient density will be targeted for regularization.  New electrical, water and transportation services will be developed for them.

## HOW TO USE IT

Press CLEAR to clear the screen and prepare for the simulation

Press CITYSCAPE to draw an initial city.  After it completes the city the button will de-activate.  Do not use the GO button until after CITYSCAPE has completed its process, and the button has deactivated!

Press GO to run the simulation. (Only after you have run CLEAR, and let CITYSCAPE run to completion).

The INSPECT button pops up agent inspectors for the migrant with the least income, one migrant with savings above 0, and a patch with migrants living on it.

The ADD-NODE forever button allows you to click on a patch and create a service node, which has electrical, water, and transportation services.

MIGRATION-TICKS determines how many ticks of the model between waves of immigrants.

CROSSING-TICKS determined how many ticks of the model before a percentage of residents cross the border and leave the town.

\#-MAQUILADORAS sets the number of maquiladoras, which are created at the edge of the city when the city is created during the CITYSCAPE step.

\#-SERVICE-CENTERS determines the number of service centers (shown as large circles in the view) that are created when the city is created.

INIT-DENSITY determines the initial number of migrants at the stat of a model run.

REQUIRED-CAPITAL determines how much savings a migrant must have before moving from their current location to a new location.

COLONIA-SIZE determines the size of a region that can become a colonia.  A larger size means larger colonias.

CARRYING-CAPACITY determines how large of an area a new node, created with the ADD-NODE button, effects.  A new node will add services to patches in that area.

BUILDING-TICKS determines how many ticks of the model between building when areas are regularized

The VISUAL-UPDATE chooser allows additional visualization of various aspects of the state of the model, including "elevation", "land value gradient", "colonias", "units with no water", and "units with no electricity".  If GO is running, then the view will update in real-time.  Otherwise, push the UPDATE-NOW button after choosing a new visualization mode.

If the COLOR-CODE? switch is ON, then migrants will be colored on the basis of their  origin ( "oaxaca" is pink, "jalisco" is orange, "zacatecas" is blue, and "mexico" is green]).  If it is OFF, then all migrants are shown in black (or white, in "land value gradient" visualization mode).

The CITY-GROWTH? switch determines whether the city continues to grow as the model progresses.

## THINGS TO NOTICE

Colonias will eventually build their own infrastructure if CITY-GROWTH is on.

Adding a node with the ADD-NODE button will attract migrants to that area.

## THINGS TO TRY

Try setting the MIGRATION-TICKS to a lower value, thus increasing the number of migrants.  Increate CROSSING-TICKS so that fewer people leave.

Try using the ADD-NODE button to add a bunch of nodes to an area to make it attractive.

## EXTENDING THE MODEL

Add a button that allows the creation of colonias in explicit locations.

## RELATED MODELS

This model is related to all of the other models in the "Urban Suite".

## CREDITS AND REFERENCES

The original version of this model was developed during the Sprawl/Swarm Class at Illinois Institute of Technology in Fall 2006 under the supervision of Sarah Dunn and Martin Felsen, by the following student: Federico Diaz De Leon.  See http://www.sprawlcity.us/ for more information about this course.

Further modification and documentation was performed by members of the Center for Connected Learning and Computer-Based Modeling before releasing it as an Urban Suite model.

The Urban Suite models were developed as part of the Procedural Modeling of Cities project, under the sponsorship of NSF ITR award 0326542, Electronic Arts & Maxis.

Please see the project web site ( http://ccl.northwestern.edu/cities/ ) for more information.
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

eyeball
false
0
Circle -7500403 true true 15 13 262
Circle -1 true false 22 20 248
Circle -7500403 true true 63 61 162
Circle -1 true false 93 91 102

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

factory
false
0
Circle -7500403 true true 162 14 23
Circle -7500403 true true 129 19 42
Circle -7500403 true true 105 40 32
Circle -7500403 true true 37 73 32
Circle -7500403 true true 55 38 54
Rectangle -7500403 true true 76 194 285 270
Rectangle -1 true false 90 210 270 240
Line -7500403 true 90 195 90 255
Line -7500403 true 120 195 120 255
Line -7500403 true 150 195 150 240
Line -7500403 true 180 195 180 255
Line -7500403 true 210 210 210 240
Line -7500403 true 240 210 240 240
Line -7500403 true 90 225 270 225
Circle -1 true false 60 43 44
Rectangle -7500403 true true 14 228 78 270
Circle -1 true false 57 75 12
Circle -1 true false 41 77 24
Rectangle -7500403 true true 36 100 59 231
Circle -7500403 true true 96 21 42
Circle -1 true false 101 26 32
Circle -1 true false 96 44 12
Circle -1 true false 99 49 12
Circle -1 true false 110 45 22
Circle -1 true false 134 24 32
Circle -1 true false 126 36 16
Circle -1 true false 166 18 14
Circle -1 true false 162 26 8

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

loop
false
0
Circle -7500403 false true 28 28 244
Circle -7500403 false true 44 44 212
Circle -7500403 false true 43 43 214
Circle -7500403 false true 42 42 216
Circle -7500403 false true 41 41 218
Circle -7500403 false true 40 40 220
Circle -7500403 false true 39 39 222
Circle -7500403 false true 38 38 224
Circle -7500403 false true 37 37 226
Circle -7500403 false true 36 36 228
Circle -7500403 false true 35 35 230
Circle -7500403 false true 34 34 232
Circle -7500403 false true 33 33 234
Circle -7500403 false true 32 32 236
Circle -7500403 false true 31 31 238
Circle -7500403 false true 30 30 240
Circle -7500403 false true 29 29 242
Circle -7500403 false true 27 27 246
Circle -7500403 false true 26 26 248
Circle -7500403 false true 25 25 250

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
ask patches [ set pcolor white ]
setup while [ticks > 0] [ setup ]
repeat 25 [ go ]
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

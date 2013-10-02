// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api

class TestClimateModel extends DockingSuite {

  // differences from version in models library:
  // - breeds removed
  //   - use a string "kind" instead
  //   - all breed-owns variables become turtles-own
  // - no set-default-shape, use "set shape" instead
  // - sliders replaced with global variables
  // - no plotting or monitors
  // - no vertical cylinder support yet, so "my-can-move?"
  //   substitutes for can-move? primitive
  // - random-normal replaced with my-random-normal
  //   since Random.nextGaussian might be hard to implement in browser
  // - use all-lower-case shape names because of some unknown problem with
  //   with capital letters in shape names
  // - use separate ask blocks to kill turtles, since killing a turtle
  //   doesn't prevent it executing the rest of the block yet

  // A note on performance: once we get the model working, if we find that performance is poor, one
  // obvious possible culprit is the turtles-here primitive.  In JVM NetLogo, turtles register and
  // deregister themselves as they enter and leave patches, so turtles-here is O(1)-ish (except in
  // the pathological case where most or all of the turtles are on a single patch; I call it
  // pathological but it's not that hard to hit, which is why there's an open issue on it at
  // https://github.com/NetLogo/NetLogo/issues/148). Currently in Tortoise, we don't have code like
  // that, so turtles-here has to check the location of every turtle, which is O(n).

  test("climate") { implicit fixture => import fixture._
    val src =
      """|globals [
         |  sky-top      ;; y coordinate of top row of sky
         |  earth-top    ;; y coordinate of top row of earth
         |  temperature  ;; overall temperature
         |  sun-brightness ;; slider
         |  albedo         ;; slider
         |]
         |
         |turtles-own [
         |  kind            ;; "ray" or "IR" or "heat" or "CO2"
         |  cloud-speed     ;; N/A unless kind is "cloud"
         |  cloud-id        ;; ditto
         |]
         |
         |;;
         |;; Setup Procedures
         |;;
         |
         |to setup
         |  clear-all
         |  setup-sliders
         |  setup-world
         |  set temperature 12
         |  reset-ticks
         |end
         |
         |to setup-sliders
         |  set sun-brightness 1.0
         |  set albedo 0.60
         |end
         |
         |to setup-world
         |  set sky-top max-pycor - 5
         |  set earth-top 0
         |  ask patches [  ;; set colors for the different sections of the world
         |    if pycor > sky-top [  ;; space
         |      set pcolor scale-color white pycor 22 15
         |    ]
         |    if pycor <= sky-top and pycor > earth-top [ ;; sky
         |      set pcolor scale-color blue pycor -20 20
         |    ]
         |    if pycor < earth-top
         |      [ set pcolor red + 3 ] ;; earth
         |    if pycor = earth-top ;; earth surface
         |      [ update-albedo ]
         |  ]
         |end
         |
         |;;
         |;; Runtime Procedures
         |;;
         |
         |to go
         |  ask clouds [ fd cloud-speed ]  ; move clouds along
         |  run-sunshine   ;; step sunshine
         |  ;; if the albedo slider has moved update the color of the "earth surface" patches
         |  ask patches with [pycor = earth-top]
         |    [ update-albedo ]
         |  run-heat  ;; step heat
         |  run-IR    ;; step IR
         |  run-CO2   ;; moves CO2 molecules
         |  tick
         |end
         |
         |to update-albedo ;; patch procedure
         |  set pcolor scale-color green albedo 0 1
         |end
         |
         |to add-cloud            ;; erase clouds and then create new ones, plus one
         |  let sky-height sky-top - earth-top
         |  ;; find a random altitude for the clouds but
         |  ;; make sure to keep it in the sky area
         |  let y earth-top + (random-float (sky-height - 4)) + 2
         |  ;; no clouds should have speed 0
         |  let speed (random-float 0.1) + 0.01
         |  let x random-xcor
         |  let id 0
         |  ;; we don't care what the cloud-id is as long as
         |  ;; all the turtles in this cluster have the same
         |  ;; id and it is unique among cloud clusters
         |  if any? clouds
         |  [ set id max [cloud-id] of clouds + 1 ]
         |
         |  create-turtles 3 + random 20
         |  [
         |    set kind "cloud"
         |    set shape "cloud"
         |    set cloud-speed speed
         |    set cloud-id id
         |    ;; all the cloud turtles in each larger cloud should
         |    ;; be nearby but not directly on top of the others so
         |    ;; add a little wiggle room in the x and ycors
         |    setxy x + random 9 - 4
         |          ;; the clouds should generally be clustered around the
         |          ;; center with occasional larger variations
         |          y + my-random-normal 2.5 1
         |    set color white
         |    ;; varying size is also purely for visualization
         |    ;; since we're only doing patch-based collisions
         |    set size 2 + random 2
         |    set heading 90
         |  ]
         |end
         |
         |to remove-cloud       ;; erase clouds and then create new ones, minus one
         |  if any? clouds [
         |    let doomed-id one-of remove-duplicates [cloud-id] of clouds
         |    ask clouds with [cloud-id = doomed-id]
         |      [ die ]
         |  ]
         |end
         |
         |to run-sunshine
         |  ask rays [
         |    if not my-can-move? 0.3 [ die ]  ;; kill them off at the edge
         |  ]
         |  ask rays [
         |    fd 0.3                        ;; otherwise keep moving
         |  ]
         |  create-sunshine  ;; start new sun rays from top
         |  reflect-rays-from-clouds  ;; check for reflection off clouds
         |  encounter-earth   ;; check for reflection off earth and absorption
         |end
         |
         |to create-sunshine
         |  ;; don't necessarily create a ray each tick
         |  ;; as brightness gets higher make more
         |  if 10 * sun-brightness > random 50 [
         |    create-turtles 1 [
         |      set kind "ray"
         |      set shape "ray"
         |      set heading 160
         |      set color yellow
         |      ;; rays only come from a small area
         |      ;; near the top of the world
         |      setxy (random 10) + min-pxcor max-pycor
         |    ]
         |  ]
         |end
         |
         |to reflect-rays-from-clouds
         | ask rays with [any? clouds-here] [   ;; if ray shares patch with a cloud
         |   set heading 180 - heading   ;; turn the ray around
         | ]
         |end
         |
         |to encounter-earth
         |  ask rays with [ycor <= earth-top] [
         |    ;; depending on the albedo either
         |    ;; the earth absorbs the heat or reflects it
         |    ifelse 100 * albedo > random 100
         |      [ set heading 180 - heading  ] ;; reflect
         |      [ rt random 45 - random 45 ;; absorb into the earth
         |        set color red - 2 + random 4
         |        set kind "heat"
         |        set shape "dot" ]
         |  ]
         |end
         |
         |to run-heat    ;; advances the heat energy turtles
         |  ;; the temperature is related to the number of heat turtles
         |  set temperature 0.99 * temperature + 0.01 * (12 + 0.1 * count heats)
         |  ask heats
         |  [
         |    let dist 0.5 * random-float 1
         |    ifelse my-can-move? dist
         |      [ fd dist ]
         |      [ set heading 180 - heading ] ;; if we're hitting the edge of the world, turn around
         |    if ycor >= earth-top [  ;; if heading back into sky
         |      ifelse temperature > 20 + random 40
         |              ;; heats only seep out of the earth from a small area
         |              ;; this makes the model look nice but it also contributes
         |              ;; to the rate at which heat can be lost
         |              and xcor > 0 and xcor < max-pxcor - 8
         |        [ set kind "IR"                    ;; let some escape as IR
         |          set shape "ray"
         |          set heading 20
         |          set color magenta ]
         |        [ set heading 100 + random 160 ] ;; return them to earth
         |    ]
         |  ]
         |end
         |
         |to run-IR
         |  ask IRs [
         |    if not my-can-move? 0.3 [ die ]
         |  ]
         |  ask IRs [
         |    fd 0.3
         |    if ycor <= earth-top [   ;; convert to heat if we hit the earth's surface again
         |      set breed heats
         |      rt random 45
         |      lt random 45
         |      set color red - 2 + random 4
         |    ]
         |    if any? CO2s-here    ;; check for collision with CO2
         |      [ set heading 180 - heading ]
         |  ]
         |end
         |
         |to add-CO2  ;; randomly adds 25 CO2 molecules to atmosphere
         |  let sky-height sky-top - earth-top
         |  create-turtles 25 [
         |    set kind "CO2"
         |    set shape "co2-molecule"
         |    set color green
         |    ;; pick a random position in the sky area
         |    setxy random-xcor
         |          earth-top + random-float sky-height
         |  ]
         |end
         |
         |to remove-CO2 ;; randomly remove 25 CO2 molecules
         |  repeat 25 [
         |    if any? CO2s [
         |      ask one-of CO2s [ die ]
         |    ]
         |  ]
         |end
         |
         |to run-CO2
         |  ask CO2s [
         |    rt random 51 - 25 ;; turn a bit
         |    let dist 0.05 + random-float 0.1
         |    ;; keep the CO2 in the sky area
         |    if [not shade-of? blue pcolor] of patch-ahead dist
         |      [ set heading 180 - heading ]
         |    fd dist ;; move forward a bit
         |  ]
         |end
         |
         |;;; compensate for lack of breeds in Tortoise
         |
         |to-report CO2s
         |  report turtles with [kind = "CO2"]
         |end
         |to-report CO2s-here
         |  report turtles-here with [kind = "CO2"]
         |end
         |
         |to-report clouds
         |  report turtles with [kind = "cloud"]
         |end
         |to-report clouds-here
         |  report turtles-here with [kind = "cloud"]
         |end
         |
         |to-report rays
         |  report turtles with [kind = "ray"]
         |end
         |
         |to-report heats
         |  report turtles with [kind = "heat"]
         |end
         |
         |to-report IRs
         |  report turtles with [kind = "IR"]
         |end
         |
         |;;; compensate for lack of can-move? in Tortoise
         |
         |;; this is only just good enough for this model only
         |to-report my-can-move? [amount]
         |  let new-y ycor + amount * cos heading
         |  ifelse ycor > 0
         |    [ report new-y < max-pycor + 0.5 ]
         |    [ report new-y >= min-pycor - 0.5 ]
         |end
         |
         |;;; compensate for lack of random-normal in Tortoise
         |
         |to-report my-random-normal [center sdev]
         |  ;; not a bell curve, just a triangle
         |  report center - random-float sdev + random-float sdev
         |end
      """.stripMargin
    declare(src, api.WorldDimensions(-24, 24, -8, 22))
    for (shape <- Seq("cloud", "ray", "dot", "co2-molecule"))
      workspace.world.turtleShapeList.add(new api.DummyShape(shape))
    testCommand("setup")
    testCommand("add-cloud")
    testCommand("add-cloud")
    for (_ <- 1 to 10)
      testCommand("add-CO2")
    testCommand("remove-cloud")
    testCommand("add-cloud")
    for (_ <- 1 to 10) {
      testCommand("add-CO2")
      testCommand("go")
      testCommand("remove-CO2")
    }
    testCommand("remove-cloud")
    for (_ <- 1 to 4)
      testCommand("repeat 50 [ go ]")
    testCommand("output-print temperature")
    testCommand("""ask turtles [ output-print (word kind " " xcor " "  ycor " ") ]""")
  }

}

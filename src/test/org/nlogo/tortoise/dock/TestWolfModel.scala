// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api

class TestWolfModel extends DockingSuite {

  test("wolf") { implicit fixture => import fixture._
    // differences from models library version:
    // - sliders and switches replaced with global variables
    // - no set-default-shape, use "set shape" instead
    // - breeds removed:
    //   - use a string "kind" instead
    // - stop condition commented out
    val src =
      """|globals [grass grass? grass-regrowth-time initial-number-sheep initial-number-wolves sheep-gain-from-food wolf-gain-from-food sheep-reproduce wolf-reproduce show-energy?]
         |turtles-own [energy kind]       ;; both wolves and sheep have energy
         |patches-own [countdown]
         |
         |to setup
         |  clear-all
         |  set grass? true
         |  set grass-regrowth-time 30
         |  set initial-number-sheep 100
         |  set initial-number-wolves 50
         |  set sheep-gain-from-food 4
         |  set wolf-gain-from-food 20
         |  set sheep-reproduce 4
         |  set wolf-reproduce 5
         |  set show-energy? false
         |  ask patches [ set pcolor green ]
         |  ;; check GRASS? switch.
         |  ;; if it is true, then grass grows and the sheep eat it
         |  ;; if it false, then the sheep don't need to eat
         |  if grass? [
         |    ask patches [
         |      set pcolor one-of [green brown]
         |      if-else pcolor = green
         |        [ set countdown grass-regrowth-time ]
         |        [ set countdown random grass-regrowth-time ] ;; initialize grass grow clocks randomly for brown patches
         |    ]
         |  ]
         |  create-turtles initial-number-sheep  ;; create the sheep, then initialize their variables
         |  [
         |    set kind "sheep"
         |    set shape "sheep"
         |    set color white
         |    set size 1.5  ;; easier to see
         |    set label-color blue - 2
         |    set energy random (2 * sheep-gain-from-food)
         |    setxy random-xcor random-ycor
         |  ]
         |  create-turtles initial-number-wolves  ;; create the wolves, then initialize their variables
         |  [
         |    set kind "wolf"
         |    set shape "wolf"
         |    set color black
         |    set size 2  ;; easier to see
         |    set energy random (2 * wolf-gain-from-food)
         |    setxy random-xcor random-ycor
         |  ]
         |  display-labels
         |  set grass count patches with [pcolor = green]
         |  reset-ticks
         |end
         |
         |to go
         |  ;; if not any? turtles [ stop ]
         |  ask sheep [
         |    move
         |    if grass? [
         |      set energy energy - 1  ;; deduct energy for sheep only if grass? switch is on
         |      eat-grass
         |    ]
         |    death
         |    reproduce-sheep
         |  ]
         |  ask wolves [
         |    move
         |    set energy energy - 1  ;; wolves lose energy as they move
         |    catch-sheep
         |    death
         |    reproduce-wolves
         |  ]
         |  if grass? [ ask patches [ grow-grass ] ]
         |  set grass count patches with [pcolor = green]
         |  tick
         |  display-labels
         |end
         |
         |to move  ;; turtle procedure
         |  rt random 50
         |  lt random 50
         |  fd 1
         |end
         |
         |to eat-grass  ;; sheep procedure
         |  ;; sheep eat grass, turn the patch brown
         |  if pcolor = green [
         |    set pcolor brown
         |    set energy energy + sheep-gain-from-food  ;; sheep gain energy by eating
         |  ]
         |end
         |
         |to reproduce-sheep  ;; sheep procedure
         |  if random-float 100 < sheep-reproduce [  ;; throw "dice" to see if you will reproduce
         |    set energy (energy / 2)                ;; divide energy between parent and offspring
         |    ;; hatch 1 [ rt random-float 360 fd 1 ]   ;; hatch an offspring and move it forward 1 step
         |  ]
         |end
         |
         |to reproduce-wolves  ;; wolf procedure
         |  if random-float 100 < wolf-reproduce [  ;; throw "dice" to see if you will reproduce
         |    set energy (energy / 2)               ;; divide energy between parent and offspring
         |    ;; hatch 1 [ rt random-float 360 fd 1 ]  ;; hatch an offspring and move it forward 1 step
         |  ]
         |end
         |
         |to catch-sheep  ;; wolf procedure
         |  let prey one-of sheep-here                    ;; grab a random sheep
         |  if prey != nobody                             ;; did we get one?  if so,
         |    [ ask prey [ die ]                          ;; kill it
         |      set energy energy + wolf-gain-from-food ] ;; get energy from eating
         |end
         |
         |to death  ;; turtle procedure
         |  ;; when energy dips below zero, die
         |  if energy < 0 [ die ]
         |end
         |
         |to grow-grass  ;; patch procedure
         |  ;; countdown on brown patches: if reach 0, grow some grass
         |  if pcolor = brown [
         |    ifelse countdown <= 0
         |      [ set pcolor green
         |        set countdown grass-regrowth-time ]
         |      [ set countdown countdown - 1 ]
         |  ]
         |end
         |
         |to display-labels
         |  ask turtles [ set label "" ]
         |  if show-energy? [
         |    ask wolves [ set label round energy ]
         |    if grass? [ ask sheep [ set label round energy ] ]
         |  ]
         |end
         |
         |;;; compensate for lack of breeds in Tortoise
         |
         |to-report sheep
         |  report turtles with [kind = "sheep"]
         |end
         |to-report sheep-here
         |  report turtles-here with [kind = "sheep"]
         |end
         |
         |to-report wolves
         |  report turtles with [kind = "wolf"]
         |end
         |to-report wolves-here
         |  report turtles-here with [kind = "wolf"]
         |end
         |
         |""".stripMargin
    declare(src, api.WorldDimensions(-25, 25, -25, 25))
    for (shape <- Seq("wolf", "sheep"))
      workspace.world.turtleShapeList.add(new api.DummyShape(shape))
    testCommand("setup")
    // TODO needs "hatch" - ST 9/20/13
    // testCommand("go")
    // testCommand("repeat 10 [ go ]")
    testCommand("output-print count wolves output-print count sheep output-print grass")
  }

}

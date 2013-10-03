// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api

class TestWolfModel extends DockingSuite {

  test("wolf") { implicit fixture => import fixture._
    open("models/test/tortoise/Wolf Sheep Predation.nlogo")
    testCommand("setup")
    testCommand("repeat 20 [ go ]")
    testCommand("output-print count wolves output-print count sheep output-print grass")
  }

//  test("wolf real") { implicit fixture => import fixture._
//    val src =
//      """|globals [grass grass? grass-regrowth-time initial-number-sheep initial-number-wolves sheep-gain-from-food wolf-gain-from-food sheep-reproduce wolf-reproduce show-energy?]
//         |;; Sheep and wolves are both breeds of turtle.
//         |breed [sheep a-sheep]  ;; sheep is its own plural, so we use "a-sheep" as the singular.
//         |breed [wolves wolf]
//         |turtles-own [energy]       ;; both wolves and sheep have energy
//         |patches-own [countdown]
//         |
//         |to setup
//         |  clear-all
//         |  ask patches [ set pcolor green ]
//         |  ;; check GRASS? switch.
//         |  ;; if it is true, then grass grows and the sheep eat it
//         |  ;; if it false, then the sheep don't need to eat
//         |  if grass? [
//         |    ask patches [
//         |      set pcolor one-of [green brown]
//         |      if-else pcolor = green
//         |        [ set countdown grass-regrowth-time ]
//         |        [ set countdown random grass-regrowth-time ] ;; initialize grass grow clocks randomly for brown patches
//         |    ]
//         |  ]
//         |  set-default-shape sheep "sheep"
//         |  create-sheep initial-number-sheep  ;; create the sheep, then initialize their variables
//         |  [
//         |    set color white
//         |    set size 1.5  ;; easier to see
//         |    set label-color blue - 2
//         |    set energy random (2 * sheep-gain-from-food)
//         |    setxy random-xcor random-ycor
//         |  ]
//         |  set-default-shape wolves "wolf"
//         |  create-wolves initial-number-wolves  ;; create the wolves, then initialize their variables
//         |  [
//         |    set color black
//         |    set size 2  ;; easier to see
//         |    set energy random (2 * wolf-gain-from-food)
//         |    setxy random-xcor random-ycor
//         |  ]
//         |  display-labels
//         |  set grass count patches with [pcolor = green]
//         |  reset-ticks
//         |end
//         |
//         |to go
//         |  ;;if not any? turtles [ stop ]
//         |  ask sheep [
//         |    move
//         |    if grass? [
//         |      set energy energy - 1  ;; deduct energy for sheep only if grass? switch is on
//         |      eat-grass
//         |    ]
//         |    death
//         |    reproduce-sheep
//         |  ]
//         |  ask wolves [
//         |    move
//         |    set energy energy - 1  ;; wolves lose energy as they move
//         |    catch-sheep
//         |    death
//         |    reproduce-wolves
//         |  ]
//         |  if grass? [ ask patches [ grow-grass ] ]
//         |  set grass count patches with [pcolor = green]
//         |  tick
//         |  display-labels
//         |end
//         |
//         |to move  ;; turtle procedure
//         |  rt random 50
//         |  lt random 50
//         |  fd 1
//         |end
//         |
//         |to eat-grass  ;; sheep procedure
//         |  ;; sheep eat grass, turn the patch brown
//         |  if pcolor = green [
//         |    set pcolor brown
//         |    set energy energy + sheep-gain-from-food  ;; sheep gain energy by eating
//         |  ]
//         |end
//         |
//         |to reproduce-sheep  ;; sheep procedure
//         |  if random-float 100 < sheep-reproduce [  ;; throw "dice" to see if you will reproduce
//         |    set energy (energy / 2)                ;; divide energy between parent and offspring
//         |    hatch 1 [ rt random-float 360 fd 1 ]   ;; hatch an offspring and move it forward 1 step
//         |  ]
//         |end
//         |
//         |to reproduce-wolves  ;; wolf procedure
//         |  if random-float 100 < wolf-reproduce [  ;; throw "dice" to see if you will reproduce
//         |    set energy (energy / 2)               ;; divide energy between parent and offspring
//         |    hatch 1 [ rt random-float 360 fd 1 ]  ;; hatch an offspring and move it forward 1 step
//         |  ]
//         |end
//         |
//         |to catch-sheep  ;; wolf procedure
//         |  let prey one-of sheep-here                    ;; grab a random sheep
//         |  if prey != nobody                             ;; did we get one?  if so,
//         |    [ ask prey [ die ]                          ;; kill it
//         |      set energy energy + wolf-gain-from-food ] ;; get energy from eating
//         |end
//         |
//         |to death  ;; turtle procedure
//         |  ;; when energy dips below zero, die
//         |  if energy < 0 [ die ]
//         |end
//         |
//         |to grow-grass  ;; patch procedure
//         |  ;; countdown on brown patches: if reach 0, grow some grass
//         |  if pcolor = brown [
//         |    ifelse countdown <= 0
//         |      [ set pcolor green
//         |        set countdown grass-regrowth-time ]
//         |      [ set countdown countdown - 1 ]
//         |  ]
//         |end
//         |
//         |to display-labels
//         |  ask turtles [ set label "" ]
//         |  if show-energy? [
//         |    ask wolves [ set label round energy ]
//         |    if grass? [ ask sheep [ set label round energy ] ]
//         |  ]
//         |end""".stripMargin
//    declare(src, api.WorldDimensions(-25, 25, -25, 25))
//    for (shape <- Seq("wolf", "sheep"))
//      workspace.world.turtleShapeList.add(new api.DummyShape(shape))
//    testCommand("set grass? true")
//    testCommand("set grass-regrowth-time 30")
//    testCommand("set initial-number-sheep 100")
//    testCommand("set initial-number-wolves 50")
//    testCommand("set sheep-gain-from-food 4")
//    testCommand("set wolf-gain-from-food 20")
//    testCommand("set sheep-reproduce 4")
//    testCommand("set wolf-reproduce 5")
//    testCommand("set show-energy? false")
//    testCommand("setup")
//    testCommand("repeat 30 [ go ]")
//    testCommand("output-print count wolves output-print count sheep output-print grass")
//  }
}

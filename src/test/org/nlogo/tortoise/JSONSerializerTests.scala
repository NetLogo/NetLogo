package org.nlogo.tortoise

import org.json4s.JsonDSL.string2jvalue
import org.json4s.native.JsonMethods.{ compact, pretty, parse, render => jsRender }
import org.json4s.string2JsonInput
import org.nlogo.mirror._, Mirroring._, Mirrorables._
import org.nlogo.api, api.Version
import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.nlogo.headless._, lang._

class JSONSerializerTests extends FixtureSuite with Matchers {

  def mirrorables(implicit fixture: Fixture): Iterable[Mirrorable] =
    Mirrorables.allMirrorables(fixture.workspace.world, Seq())

  test("JSONSerializer basic commands") { implicit fixture =>
    val commands = Seq(
      "cro 1" ->
        """|{
           |  "turtles":{
           |    "0":{
           |      "WHO":0,
           |      "COLOR":5,
           |      "HEADING":0,
           |      "XCOR":0,
           |      "YCOR":0,
           |      "SHAPE":"default",
           |      "LABEL":"",
           |      "LABEL-COLOR":9.9,
           |      "BREED":"TURTLES",
           |      "HIDDEN?":false,
           |      "SIZE":1,
           |      "PEN-SIZE":1,
           |      "PEN-MODE":"up"
           |    }
           |  },
           |  "patches":{
           |
           |  }
           |}""".stripMargin,
      "ask turtles [ fd 1 ]" ->
        """|{
           |  "turtles":{
           |    "0":{
           |      "YCOR":1
           |    }
           |  },
           |  "patches":{
           |
           |  }
           |}""".stripMargin,
      "ask turtles [ die ]" ->
        """|{
           |  "turtles":{
           |    "0":{
           |      "WHO":-1
           |    }
           |  },
           |  "patches":{
           |
           |  }
           |}""".stripMargin)
      .map {
        case (cmd, json) => // prettify:
          (cmd, jsRender(parse(json)))
      }

    ModelCreator.open(fixture.workspace, api.WorldDimensions.square(1))
    val (initialState, _) = Mirroring.diffs(Map(), mirrorables)
    commands.foldLeft(initialState) {
      case (previousState, (cmd, expectedJSON)) =>
        fixture.workspace.command(cmd)
        val (nextState, update) = Mirroring.diffs(previousState, mirrorables)
        val json = jsRender(parse(JSONSerializer.serialize(update)))
        val format = compact _
        format(json) should equal(format(expectedJSON))
        nextState
    }
  }

}

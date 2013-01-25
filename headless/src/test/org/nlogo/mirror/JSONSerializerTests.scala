package org.nlogo.mirror

import org.json4s.JsonDSL.string2jvalue
import org.json4s.native.JsonMethods.{ compact, pretty, parse, render }
import org.json4s.string2JsonInput
import org.nlogo.headless.TestMirroring.withWorkspace
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class TestJSONSerializer extends FunSuite with ShouldMatchers {

  test("JSONSerializer basic commands") {
    val commands = Seq(
      "cro 1" ->
        """|{
           |  "turtles":{
           |    "0":{
           |      "WHO":0.0,
           |      "COLOR":5.0,
           |      "HEADING":0.0,
           |      "XCOR":0.0,
           |      "YCOR":0.0,
           |      "SHAPE":"default",
           |      "LABEL":"",
           |      "LABEL-COLOR":9.9,
           |      "BREED":"TURTLES",
           |      "HIDDEN?":false,
           |      "SIZE":1.0,
           |      "PEN-SIZE":1.0,
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
           |      "YCOR":1.0
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
          (cmd, render(parse(json)))
      }

    withWorkspace { (ws, mirrorables) =>
      ws.initForTesting(1)
      val (initialState, _) = Mirroring.diffs(Map(), mirrorables())
      commands.foldLeft(initialState) {
        case (previousState, (cmd, expectedJSON)) =>
          ws.command(cmd)
          val (nextState, update) = Mirroring.diffs(previousState, mirrorables())
          val json = render(parse(JSONSerializer.serialize(update)))
          val format = compact _
          format(json) should equal(format(expectedJSON))
          nextState
      }
    }
  }

}

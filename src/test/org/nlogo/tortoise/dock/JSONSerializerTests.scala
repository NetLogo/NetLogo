package org.nlogo.tortoise.dock

import org.json4s.JsonDSL.string2jvalue
import org.json4s.native.JsonMethods.{ compact, pretty, parse, render => jsRender }
import org.json4s.string2JsonInput
import org.nlogo.mirror._, Mirroring._, Mirrorables._
import org.nlogo.api, api.Version
import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.nlogo.headless._, lang._
import org.nlogo.shape.VectorShape
import org.nlogo.tortoise.json.JSONSerializer

import collection.JavaConverters._

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
           |  "world": {
           |  }
           |  "links": {
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
           |  "world": {
           |  }
           |  "links": {
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
           |  "world": {
           |  }
           |  "links": {
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

  test("JSONSerializer shapes") { implicit fixture =>
    val shapeList = new api.ShapeList(
      api.AgentKind.Turtle, 
      VectorShape.parseShapes(api.ModelReader.defaultShapes.toArray, api.Version.version).asScala
    )

    val shapes = Seq(
        "default" -> 
          """|{
             |  "rotate":true,
             |  "elements":[{
             |      "xcors":[150.0,40.0,150.0,260.0],
             |      "ycors":[5.0,250.0,205.0,250.0],
             |      "type":"polygon",
             |      "color":"rgba(141, 141, 141, 1.0)",
             |      "filled":true,
             |      "marked":true
             |  }]
             |}""".stripMargin,
        "person"  -> 
          """|{
             |  "rotate":false,
             |  "elements":[{
             |      "x":110.0,
             |      "y":5.0,
             |      "diam":80.0,
             |      "type":"circle",
             |      "color":"rgba(141, 141, 141, 1.0)",
             |      "filled":true,
             |      "marked":true
             |    },{
             |      "xcors":[105.0,120.0,90.0,105.0,135.0,150.0,165.0,195.0,210.0,180.0,195.0],
             |      "ycors":[90.0,195.0,285.0,300.0,300.0,225.0,300.0,300.0,285.0,195.0,90.0],
             |      "type":"polygon",
             |      "color":"rgba(141, 141, 141, 1.0)",
             |      "filled":true,
             |      "marked":true
             |    },{
             |      "xmin":127.0,
             |      "ymin":79.0,
             |      "xmax":172.0,
             |      "ymax":94.0,
             |      "type":"rectangle",
             |      "color":"rgba(141, 141, 141, 1.0)",
             |      "filled":true,
             |      "marked":true
             |    },{
             |      "xcors":[195.0,240.0,225.0,165.0],
             |      "ycors":[90.0,150.0,180.0,105.0],
             |      "type":"polygon",
             |      "color":"rgba(141, 141, 141, 1.0)",
             |      "filled":true,
             |      "marked":true
             |    },{
             |      "xcors":[105.0,60.0,75.0,135.0],
             |      "ycors":[90.0,150.0,180.0,105.0],
             |      "type":"polygon",
             |      "color":"rgba(141, 141, 141, 1.0)",
             |      "filled":true,
             |      "marked":true
             |  }]
             |}""".stripMargin
      ) map {
        case (shapeName, json) =>
          (shapeList.shape(shapeName), jsRender(parse(json)))
      }
  
    shapes foreach {
      case (shape, expectedJSON) =>
        val json = jsRender(parse(JSONSerializer.serialize(shape)))
        pretty(json) should equal(pretty(expectedJSON))
    }
  }

}

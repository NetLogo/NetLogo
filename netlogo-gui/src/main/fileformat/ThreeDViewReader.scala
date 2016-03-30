// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.parboiled.scala._
import org.nlogo.api.WorldDimensions3D
import org.nlogo.core.{ UpdateMode, View, WorldDimensions }
import org.nlogo.core.model.WidgetReader

import scala.reflect.ClassTag

object ThreeDViewReader extends WidgetReader with DefaultParboiledWidgetParser {
  class ThreeDParser extends Parser with ParboiledWidgetParser.RichRule {

    def ThreeDViewWidget: Rule1[View] = rule {
      GraphicsWindow ~ NewLine ~
        push(View(dimensions = new WorldDimensions3D(0, 0, 0, 0, 0, 0))) mapThrough
        ViewDimensions ~
        nTimes(2, IgnoredLine) mapThrough
        PatchSize ~
        IgnoredLine mapThrough
        FontSize ~
        nTimes(4, IgnoredLine) mapThrough
        WrappingInXAndY ~
        IgnoredLine mapThrough
        PCors mapThrough
        WrappingInZ mapThrough
        UpdateModeView mapThrough
        ShowTickCounter mapThrough
        TickCounterLabel mapThrough
        FrameRate
    }

    def dimensionTransform[B <: WorldDimensions3D](f: WorldDimensions3D => WorldDimensions3D)(v: View): View = {
      v.dimensions match {
        case w: WorldDimensions3D => v.copy(dimensions = f(w))
        case _ => throw new Exception("Error parsing 3D view!")
      }
    }

    def FontSize: Rule1[View => View] = rule { (IntValue ~ NewLine) ~~> ((fontSize: Int) =>
      ((v: View) => v.copy(fontSize = fontSize))) }

    def FrameRate: Rule1[View => View] = rule {
      (DoubleValue ~ optional(NewLine)) ~~>
        ((rate: Double) => ((v: View) => v.copy(frameRate = rate)))
    }

    def PatchSize: Rule1[View => View] = rule { (DoubleValue ~ NewLine) ~~> ((size: Double) =>
      dimensionTransform(_.copyThreeD(patchSize = size)))
    }

    def PCors: Rule1[View => View] = rule {
      nTimes(6, IntValue ~ NewLine) ~~> (cors =>
          dimensionTransform(_.copyThreeD(
            minPxcor = cors(0), maxPxcor = cors(1),
            minPycor = cors(2), maxPycor = cors(3),
            minPzcor = cors(4), maxPzcor = cors(5))))
    }

    def ShowTickCounter: Rule1[View => View] = rule {
      (BooleanDigit ~ NewLine) ~~> (show => ((v: View) => v.copy(showTickCounter = show)))
    }

    def TickCounterLabel: Rule1[View => View] = rule {
      group(StringValue ~ NewLine) ~> ((s: String) => { (v: View) =>
          val trimmed = s.stripSuffix("\n")
          if (trimmed == "NIL")
            v.copy(tickCounterLabel = None)
          else
            v.copy(tickCounterLabel = Some(trimmed))
      })
    }

    def UpdateModeValue: Rule1[UpdateMode] = rule {
      rule { ("0" | "1") ~ NewLine } ~>
        (i => UpdateMode.load(i.take(1).toInt))
    }

    def UpdateModeView: Rule1[View => View] = rule {
      UpdateModeValue ~~> (u => ((v: View) => v.copy(updateMode = u)))
    }

    def ViewDimensions: Rule1[View => View] = rule {
      nTimes(4, IntValue ~ NewLine) ~~> (dims =>
          ((v: View) => v.copy(left = dims(0), top = dims(1), right = dims(2), bottom = dims(3))))
    }

    def WrappingInXAndY: Rule1[View => View] = rule { (BooleanDigit ~ NewLine ~ BooleanDigit ~ NewLine) ~~> ((wrapsInX: Boolean, wrapsInY: Boolean) =>
      dimensionTransform(_.copyThreeD(wrappingAllowedInX = wrapsInX, wrappingAllowedInY = wrapsInY)))
    }

    def WrappingInZ: Rule1[View => View] = rule {
      (BooleanDigit ~ NewLine) ~~> (allowed =>
          dimensionTransform(_.copyThreeD(wrappingAllowedInZ = allowed)))
    }

    def GraphicsWindow: Rule0 = rule { "GRAPHICS-WINDOW" }
  }

  type T = View

  type ParsedWidget = View

  def classTag: ClassTag[T] = ClassTag(classOf[View])

  def parser = new ThreeDParser {
    override val buildParseTree = true
  }

  override def parseRule =
    parser.ThreeDViewWidget

  def format(t: View): String = {
    val dimensions = t.dimensions.asInstanceOf[WorldDimensions3D]
    Seq(
      "GRAPHICS-WINDOW",
      t.left.toString,
      t.top.toString,
      t.right.toString,
      t.bottom.toString,
      "-1",
      "-1",
      dimensions.patchSize.toString,
      "1", //shapesOn
      t.fontSize.toString,
      "1", // no longer used
      "1", // ""
      "1", // ""
      "0", // ""
      if (t.wrappingAllowedInX) "1" else "0",
      if (t.wrappingAllowedInY) "1" else "0",
      "1", // thin turtle pens
      t.minPxcor.toString,
      t.maxPxcor.toString,
      t.minPycor.toString,
      t.maxPycor.toString,
      dimensions.minPzcor.toString,
      dimensions.maxPzcor.toString,
      if (dimensions.wrappingAllowedInZ) "1" else "0",
      t.updateMode.save.toString,
      if (t.showTickCounter) "1" else "0",
      t.tickCounterLabel.getOrElse("NIL"),
      t.frameRate).mkString("", "\n", "\n")

  }
}

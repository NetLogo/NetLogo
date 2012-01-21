// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import org.nlogo.api
import org.nlogo.api.LogoList
import org.nlogo.api.Color.{ getRGBAListByARGB, getARGBIntByRGBAList }
import java.io.{ DataInputStream, DataOutputStream, IOException }

object TurtleData {

  val DEAD           = 0x0000
  val XCOR           = 0x0001
  val YCOR           = 0x0002
  val SHAPE          = 0x0004
  val COLOR          = 0x0008
  val HEADING        = 0x0010
  val SIZE           = 0x0020
  val HIDDEN         = 0x0040
  val LABEL          = 0x0080
  val LABEL_COLOR    = 0x0100
  val BREED_INDEX    = 0x0200
  val LINE_THICKNESS = 0x0400

  val COMPLETE =
    XCOR | YCOR | SHAPE | COLOR | HEADING | SIZE | HIDDEN | LABEL |
    LABEL_COLOR | BREED_INDEX | LINE_THICKNESS

  val OverrideVariables = Array(
    "COLOR", "LABEL", "LABEL-COLOR", "LINE-THICKNESS", "HIDDEN?", "HEADING", "SHAPE", "SIZE")
  val OverrideGetters = Array(
    "color", "label", "labelColor", "lineThickness", "hidden", "heading", "shape", "size")
  val OverrideSetters = Array(
    "color_$eq", "label_$eq", "labelColor_$eq", "lineThickness_$eq", "hidden_$eq", "heading_$eq", "shape_$eq", "size_$eq")

  def getOverrideIndex(varName: String): Int =
    Overridable.getOverrideIndex(OverrideVariables, varName)

  @throws(classOf[java.io.IOException])
  def fromStream(in: DataInputStream) = {
    val id = in.readLong()
    val mask = in.readShort()
    new TurtleData(
      id, mask.toInt,
      if ((mask & XCOR) == XCOR)
        in.readDouble() else 0,
      if ((mask & YCOR) == YCOR)
        in.readDouble() else 0,
      if ((mask & SHAPE) == SHAPE)
        in.readUTF() else null,
      if ((mask & COLOR) == COLOR)
        getRGBAListByARGB(in.readInt()) else null,
      if ((mask & HEADING) == HEADING)
        in.readDouble() else 0,
      if ((mask & SIZE) == SIZE)
        in.readDouble() else 0,
      if ((mask & HIDDEN) == HIDDEN)
        in.readBoolean() else false,
      if ((mask & LABEL) == LABEL)
        in.readUTF() else "",
      if ((mask & LABEL_COLOR) == LABEL_COLOR)
        getRGBAListByARGB(in.readInt()) else null,
      // this is breedIndex. why always include it? Josh tried treating it like other vars in
      // 26d595ba but it hasn't been road-tested
      in.readInt(),
      if ((mask & LINE_THICKNESS) == LINE_THICKNESS)
        in.readDouble() else 0)
  }
}

class TurtleData(val id: Long, var mask: Int, var xcor: Double, var ycor: Double, var shape: String, var _color: LogoList,
                 var heading: Double, var size: Double, var hidden: Boolean, var label: String, var _labelColor: LogoList,
                 var breedIndex: Int, var lineThickness: Double)
extends AgentData with api.Turtle {

  def this(id: Long, mask: Int, xcor: Double, ycor: Double, shape: String, color: AnyRef,
           heading: Double, size: Double, hidden: Boolean, label: String, labelColor: AnyRef,
           breedIndex: Int, lineThickness: Double) =
    this(id, mask, xcor, ycor, shape, AgentData.toLogoList(color),
         heading, size, hidden, label, AgentData.toLogoList(labelColor),
         breedIndex, lineThickness)

  def this(id: Long) =
    this(id, TurtleData.DEAD, 0, 0, "", LogoList.Empty, 0, 0, false, "", LogoList.Empty, 0, 0)

  def this(turtle: TurtleStamp) =
    this(0, TurtleData.COMPLETE, turtle.xcor, turtle.ycor, turtle.shape, turtle.color,
         turtle.heading, turtle.size, turtle.hidden, "", null, 0, turtle.lineThickness)

  import TurtleData._

  def getterName(index: Int) =
    TurtleData.OverrideGetters(index)

  def setterName(index: Int) =
    TurtleData.OverrideSetters(index)

  def color = _color
  def color_=(c: AnyRef) {
    _color = AgentData.toLogoList(c)
  }

  def labelColor = _labelColor
  def labelColor_=(c: AnyRef) {
    _labelColor = AgentData.toLogoList(c)
  }

  def label_=(_label: AnyRef) {
    label = _label.toString
  }
  def hasLabel = Option(label).exists(_.nonEmpty)
  def labelString = label

  def getBreedIndex = breedIndex
  override def spotlightSize = size * 2
  override def wrapSpotlight = true

  def stringRep =
    mask match {
      case COMPLETE =>
        "Turtle " + id + " (" + xcor + ", " + ycor + ", " + shape + ", " +
        getARGBIntByRGBAList(color) + ", " + heading + ", " + size + ", " +
        hidden + ", " + label + ", " + getARGBIntByRGBAList(labelColor) + ", " +
        breedIndex + ", " + lineThickness + ")"
      case DEAD =>
        "Turtle " + id + " (dead)"
      case _ =>
        "Turtle " + id + " update (mask " + java.lang.Integer.toBinaryString(mask) + ")"
    }

  def isComplete = (mask & COMPLETE) == COMPLETE
  def isDead = mask == DEAD

  /**
   * updates this TurtleData to include any changes specified by other.  Returns a new TurtleData
   * representing only items which actually differed between the two. If no changes are required,
   * returns null.
   */
  def updateFrom(other: TurtleData): TurtleData = {
    require(!other.isDead)
    // start out with a "dead" turtle, but we'll fill it in...
    val diffs = new TurtleData(id)
    // update the values...
    if ((other.mask & XCOR) == XCOR && xcor != other.xcor) {
      xcor = other.xcor
      diffs.mask |= XCOR
      diffs.xcor = xcor
    }
    if ((other.mask & YCOR) == YCOR && ycor != other.ycor) {
      ycor = other.ycor
      diffs.mask |= YCOR
      diffs.ycor = ycor
    }
    if ((other.mask & SHAPE) == SHAPE && shape != other.shape) {
      shape = other.shape
      diffs.mask |= SHAPE
      diffs.shape = shape
    }
    if ((other.mask & COLOR) == COLOR && color != other.color) {
      color = other.color
      diffs.mask |= COLOR
      diffs.color = color
    }
    if ((other.mask & HEADING) == HEADING && heading != other.heading) {
      heading = other.heading
      diffs.mask |= HEADING
      diffs.heading = heading
    }
    if ((other.mask & SIZE) == SIZE && size != other.size) {
      size = other.size
      diffs.mask |= SIZE
      diffs.size = size
    }
    if ((other.mask & HIDDEN) == HIDDEN && hidden != other.hidden) {
      hidden = other.hidden
      diffs.mask |= HIDDEN
      diffs.hidden = hidden
    }
    if ((other.mask & LABEL) == LABEL && label != other.label) {
      label = other.label
      diffs.mask |= LABEL
      diffs.label = label
    }
    if ((other.mask & LABEL_COLOR) == LABEL_COLOR && labelColor != other.labelColor) {
      labelColor = other.labelColor
      diffs.mask |= LABEL_COLOR
      diffs.labelColor = labelColor
    }
    if ((other.mask & LINE_THICKNESS) == LINE_THICKNESS && lineThickness != other.lineThickness) {
      lineThickness = other.lineThickness
      diffs.mask |= LINE_THICKNESS
      diffs.lineThickness = lineThickness
    }

    // include breed index whether it changed or not
    breedIndex = other.breedIndex
    diffs.breedIndex = breedIndex

    if (diffs.mask == DEAD) null
    else diffs
  }

  @throws(classOf[IOException])
  override def serialize(out: DataOutputStream) {
    out.writeLong(id)
    out.writeShort(mask)
    if ((mask & XCOR) == XCOR)
      out.writeDouble(xcor)
    if ((mask & YCOR) == YCOR)
      out.writeDouble(ycor)
    if ((mask & SHAPE) == SHAPE)
      out.writeUTF(shape)
    if ((mask & COLOR) == COLOR)
      out.writeInt(getARGBIntByRGBAList(color))
    if ((mask & HEADING) == HEADING)
      out.writeDouble(heading)
    if ((mask & SIZE) == SIZE)
      out.writeDouble(size)
    if ((mask & HIDDEN) == HIDDEN)
      out.writeBoolean(hidden)
    if ((mask & LABEL) == LABEL)
      out.writeUTF(label)
    if ((mask & LABEL_COLOR) == LABEL_COLOR)
      out.writeInt(getARGBIntByRGBAList(labelColor))
    out.writeInt(breedIndex)
    if ((mask & LINE_THICKNESS) == LINE_THICKNESS)
      out.writeDouble(lineThickness)
  }

  override def getBreed = unsupported
  override def world = unsupported
  override def getPatchHere = unsupported
  override def jump(d: Double) = unsupported
  override def heading(d: Double) = unsupported
  override def classDisplayName = unsupported
  override def getVariable(vn: Int) = unsupported
  override def setVariable(vn: Int, value: AnyRef) = unsupported
  override def variables = unsupported
  override def alpha = unsupported
  override def isPartiallyTransparent = unsupported

  private def unsupported = throw new UnsupportedOperationException
}

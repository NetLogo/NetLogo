// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import org.nlogo.api
import org.nlogo.api.LogoList
import org.nlogo.api.Color.{ getRGBAListByARGB, getARGBIntByRGBAList }
import java.io.{ DataInputStream, DataOutputStream, IOException }

object LinkData {
  
  val DEAD             = 0x0000
  val ENDS             = 0x0001
  val X1               = 0x0002
  val Y1               = 0x0004
  val X2               = 0x0008
  val Y2               = 0x0010
  val SHAPE            = 0x0020
  val COLOR            = 0x0040
  val HIDDEN           = 0x0080
  val LABEL            = 0x0100
  val LABEL_COLOR      = 0x0200
  val LINE_THICKNESS   = 0x0400
  val DESTINATION_SIZE = 0x0800
  val HEADING          = 0x1000
  val SIZE             = 0x2000
  val BREED            = 0x4000

  val COMPLETE = ENDS | X1 | Y1 | X2 | Y2 | SHAPE | COLOR | HIDDEN | LABEL | LABEL_COLOR |
    LINE_THICKNESS | DESTINATION_SIZE | HEADING | SIZE | BREED

  val OverrideVariables = Array(
    "COLOR", "LABEL", "LABEL-COLOR", "LINE-THICKNESS", "HIDDEN?", "SHAPE")
  val OverrideGetters = Array(
    "color", "label", "labelColor", "lineThickness", "hidden", "shape")
  val OverrideSetters = Array(
    "color_$eq", "label_$eq", "labelColor_$eq", "lineThickness_$eq", "hidden_$eq", "shape_$eq")

  def getOverrideIndex(varName: String): Int =
    Overridable.getOverrideIndex(OverrideVariables, varName)

  @throws(classOf[java.io.IOException])
  def fromStream(in: DataInputStream) = {
    val id = in.readLong()
    val mask = in.readShort()
    new LinkData(
      id,
      if ((mask & ENDS) == ENDS)
        in.readLong() else 0L,
      if ((mask & ENDS) == ENDS)
        in.readLong() else 0L,
      mask,
      if ((mask & X1) == X1)
        in.readDouble() else 0d,
      if ((mask & Y1) == Y1)
        in.readDouble() else 0d,
      if ((mask & X2) == X2)
        in.readDouble() else 0d,
      if ((mask & Y2) == Y2)
        in.readDouble() else 0d,
      if ((mask & SHAPE) == SHAPE)
        in.readUTF() else null,
      if ((mask & COLOR) == COLOR)
        getRGBAListByARGB(in.readInt()) else null,
      if ((mask & HIDDEN) == HIDDEN)
        in.readBoolean() else false,
      if ((mask & LABEL) == LABEL)
        in.readUTF() else "",
      if ((mask & LABEL_COLOR) == LABEL_COLOR)
        getRGBAListByARGB(in.readInt()) else null,
      if ((mask & LINE_THICKNESS) == LINE_THICKNESS)
        in.readDouble() else 0d,
      if ((mask & BREED) == BREED)
        in.readBoolean() else false,
      if ((mask & DESTINATION_SIZE) == DESTINATION_SIZE)
        in.readDouble() else 0d,
      if ((mask & HEADING) == HEADING)
        in.readDouble() else 0d,
      if ((mask & SIZE) == SIZE)
        in.readDouble() else 0d,
      if ((mask & BREED) == BREED)
        in.readInt() else 0)
  }
}

class LinkData(val id: Long, var end1Id: Long, var end2Id: Long, var mask: Int,
               var x1: Double, var y1: Double, var x2: Double, var y2: Double,
               var shape: String, var _color: LogoList, var hidden: Boolean,
               var label: String, var _labelColor: LogoList, var lineThickness: Double, var isDirectedLink: Boolean,
               var linkDestinationSize: Double, var heading: Double, var size: Double, var breedIndex: Int)
extends AgentData with api.Link {

  def this(link: LinkStamp) =
    this(0, 0, 0, LinkData.COMPLETE,
         link.x1, link.y1, link.x2, link.y2,
         link.shape, AgentData.toLogoList(link.color), link.hidden,
         "", LogoList.Empty, link.lineThickness, link.directed,
         0, link.heading, link.size, 0)

  /**
   * represents a link that has died.
   */
  def this(id: Long) =
    this(id, 0, 0, LinkData.DEAD,
         0, 0, 0, 0,
         "", LogoList.Empty, false,
         "", LogoList.Empty, 0, false,
         0, 0, 0, 0)
    
  import LinkData._

  def getterName(index: Int) =
    LinkData.OverrideGetters(index)

  def setterName(index: Int) =
    LinkData.OverrideSetters(index)

  override def toString =
    "link " + id + " " + breedIndex

  def color = _color
  def color_=(c: AnyRef) {
    _color = AgentData.toLogoList(c)
  }

  def labelColor = _labelColor
  def labelColor_=(c: AnyRef) {
    _labelColor = AgentData.toLogoList(c)
  }
  def label_=(_label: Any) {
    label = _label.toString
  }
  def labelString = label
  def hasLabel = Option(label).exists(_.nonEmpty)

  private[mirroring] def getKey: ClientWorldS.LinkKey =
    new ClientWorldS.LinkKey(id, end1Id, end2Id, breedIndex)

  def getBreedIndex = breedIndex

  override def spotlightSize = 1
  override def wrapSpotlight = false

  def midpointX = (x1 + x2) / 2
  def midpointY = (y1 + y2) / 2

  def stringRep =
    mask match {
      case COMPLETE =>
        "Link " + id + " (" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ", " +
          shape + ", " + org.nlogo.api.Color.getARGBIntByRGBAList(color) + ", " + ", " + hidden + ", " +
          label + ", " + org.nlogo.api.Color.getARGBIntByRGBAList(labelColor) + ", " + lineThickness + ")"
      case DEAD =>
        "Link " + id + " (dead)"
      case _ =>
        "Link " + id + " update (mask " + java.lang.Integer.toBinaryString(mask) + ")"
    }

  def isComplete = (mask & COMPLETE) == COMPLETE
  def isDead = mask == DEAD

  /**
   * updates this LinkData to include any changes specified by other.  Returns a new LinkData
   * representing only items which actually differed between the two. If no changes are required,
   * returns null.
   */
  def updateFrom(other: LinkData): LinkData = {
    require(!other.isDead)
    // start out with a "dead" link, but we'll fill it in...
    val diffs = new LinkData(id)
    // update the values...
    if ((other.mask & ENDS) == ENDS && end1Id != other.end1Id) {
      end1Id = other.end1Id
      end2Id = other.end2Id
      diffs.mask |= ENDS
      diffs.end1Id = end1Id
      diffs.end2Id = end2Id
    }
    if ((other.mask & X1) == X1 && x1 != other.x1) {
      x1 = other.x1
      diffs.mask |= X1
      diffs.x1 = x1
    }
    if ((other.mask & Y1) == Y1 && y1 != other.y1) {
      y1 = other.y1
      diffs.mask |= Y1
      diffs.y1 = y1
    }
    if ((other.mask & X2) == X2 && x2 != other.x2) {
      x2 = other.x2
      diffs.mask |= X2
      diffs.x2 = x2
    }
    if ((other.mask & Y2) == Y2 && y2 != other.y2) {
      y2 = other.y2
      diffs.mask |= Y2
      diffs.y2 = y2
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
    if ((other.mask & DESTINATION_SIZE) == DESTINATION_SIZE && linkDestinationSize != other.linkDestinationSize) {
      linkDestinationSize = other.linkDestinationSize
      diffs.mask |= DESTINATION_SIZE
      diffs.linkDestinationSize = linkDestinationSize
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
    if ((other.mask & BREED) == BREED && breedIndex != other.breedIndex) {
      breedIndex = other.breedIndex
      isDirectedLink = other.isDirectedLink
      diffs.mask |= BREED
      diffs.breedIndex = breedIndex
      diffs.isDirectedLink = isDirectedLink
    }
    if (diffs.mask == DEAD) null
    else diffs
  }

  @throws(classOf[IOException])
  override def serialize(out: DataOutputStream) = {
    out.writeLong(id)
    out.writeShort(mask)
    if ((mask & ENDS) == ENDS) {
      out.writeLong(end1Id)
      out.writeLong(end2Id)
    }
    if ((mask & X1) == X1)
      out.writeDouble(x1)
    if ((mask & Y1) == Y1)
      out.writeDouble(y1)
    if ((mask & X2) == X2)
      out.writeDouble(x2)
    if ((mask & Y2) == Y2)
      out.writeDouble(y2)
    if ((mask & SHAPE) == SHAPE)
      out.writeUTF(shape)
    if ((mask & COLOR) == COLOR)
      out.writeInt(getARGBIntByRGBAList(color))
    if ((mask & HIDDEN) == HIDDEN)
      out.writeBoolean(hidden)
    if ((mask & LABEL) == LABEL)
      out.writeUTF(label)
    if ((mask & LABEL_COLOR) == LABEL_COLOR)
      out.writeInt(getARGBIntByRGBAList(labelColor))
    if ((mask & LINE_THICKNESS) == LINE_THICKNESS)
      out.writeDouble(lineThickness)
    if ((mask & DESTINATION_SIZE) == DESTINATION_SIZE)
      out.writeDouble(linkDestinationSize)
    if ((mask & HEADING) == HEADING)
      out.writeDouble(heading)
    if ((mask & SIZE) == SIZE)
      out.writeDouble(size)
    if ((mask & BREED) == BREED) {
      out.writeInt(breedIndex)
      out.writeBoolean(isDirectedLink)
    }
  }

  override def xcor = unsupported
  override def ycor = unsupported
  override def getBreed = unsupported
  override def world = unsupported
  override def classDisplayName = unsupported
  override def setVariable(vn: Int, value: AnyRef) = unsupported
  override def getVariable(vn: Int) = unsupported
  override def variables = unsupported
  override def alpha = unsupported
  override def isPartiallyTransparent = unsupported
  override def end1 = unsupported
  override def end2 = unsupported

  private def unsupported = throw new UnsupportedOperationException
}

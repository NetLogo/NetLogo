// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import org.nlogo.api
import org.nlogo.api.LogoList
import org.nlogo.api.Color.{ getARGBIntByRGBAList, getRGBAListByARGB }
import java.io.{ DataInputStream, DataOutputStream, IOException }

object PatchData {

  val DEAD         = 0x0000
  val PCOLOR       = 0x0001
  val PLABEL       = 0x0002
  val PLABEL_COLOR = 0x0004
  val PXCOR        = 0x0008
  val PYCOR        = 0x0010
  val COMPLETE = PCOLOR | PLABEL | PLABEL_COLOR | PXCOR | PYCOR

  val OverrideVariables = Array("PCOLOR", "PLABEL", "PLABEL-COLOR")
  val OverrideMethods = Array("pcolor", "plabel", "plabelColor")
  
  def getOverrideIndex(varName: String): Int =
    Overridable.getOverrideIndex(OverrideVariables, varName)

  @throws(classOf[java.io.IOException])
  def fromStream(in: DataInputStream) = {
    val id = in.readLong()
    val mask = in.readShort()
    new PatchData(
      id, mask,
      if ((mask & PXCOR) == PXCOR)
        in.readInt() else 0,
      if ((mask & PYCOR) == PYCOR)
        in.readInt() else 0,
      if ((mask & PCOLOR) == PCOLOR)
        getRGBAListByARGB(in.readInt()) else null,
      if ((mask & PLABEL) == PLABEL)
        in.readUTF() else null,
      if ((mask & PLABEL_COLOR) == PLABEL_COLOR)
        getRGBAListByARGB(in.readInt()) else null)
  }

}

/**
 * The mask specifies which patch attributes are described by this PatchData object, as the bitwise
 * or of all the description masks that apply to this PatchData.
 *
 * For example: a PatchData that describes the color and the label color of a patch, but not the
 * label itself should have mask <code>PCOLOR | LABEL_COLOR</code>.
 */
class PatchData private (val id: Long, var mask: Int, var pxcor: Int, var pycor: Int, var _pcolor: LogoList, var plabel: String, var _plabelColor: LogoList, var patchColors: Array[Int] = null)
extends AgentData {

  def this(id: Long, mask: Int, pxcor: Int, pycor: Int, pcolor: AnyRef, plabel: String, plabelColor: AnyRef) =
    this(id, mask, pxcor, pycor, AgentData.toLogoList(pcolor), plabel, AgentData.toLogoList(plabelColor), null)

  def this(id: Long) =
    this(id, PatchData.DEAD, 0, 0, null, null, null, null)

  import PatchData._

  def plabel_=(_label: AnyRef) {
    plabel = api.Dump.logoObject(_label)
  }

  def hasLabel = plabel.nonEmpty

  def methodName(index: Int) =
    PatchData.OverrideMethods(index)

  def pcolor = _pcolor
  def pcolor_=(pc: AnyRef) {
    _pcolor = AgentData.toLogoList(pc)
    if (patchColors != null)
      patchColors(id.toInt) = getARGBIntByRGBAList(_pcolor)
  }

  def plabelColor = _plabelColor
  def plabelColor_=(plc: AnyRef) { _plabelColor = AgentData.toLogoList(plc) }

  override def xcor = pxcor
  override def ycor = pycor
  override def spotlightSize = 1
  override def wrapSpotlight = false

  def stringRep =
    "Patch " + pxcor + " " + pycor + " (" + getARGBIntByRGBAList(pcolor) +
    ", " + plabel + ", " + getARGBIntByRGBAList(plabelColor) + ")"

  def isComplete =
    (mask & COMPLETE) == COMPLETE

  /**
   * Updates this PatchData with data from another PatchData.
   *
   * @param otherPatch another PatchData from which to update this PatchData
   * @return a new PatchData representing the changes made to
   *         this PatchData (i.e. the difference between the two objects).
   *         If nothing was changed, returns null.
   */
  def updateFrom(otherPatch: PatchData): PatchData = {
    // start out with a "dead" patch, but we'll fill it in...
    val diffs = new PatchData(id)
    if ((otherPatch.mask & PXCOR) == PXCOR && pxcor != otherPatch.pxcor) {
      pxcor = otherPatch.pxcor
      diffs.mask |= PXCOR
      diffs.pxcor = pxcor
    }
    if ((otherPatch.mask & PYCOR) == PYCOR && pycor != otherPatch.pycor) {
      pycor = otherPatch.pycor
      diffs.mask |= PYCOR
      diffs.pycor = pycor
    }
    if ((otherPatch.mask & PCOLOR) == PCOLOR && !pcolor.equals(otherPatch.pcolor)) {
      _pcolor = otherPatch.pcolor
      diffs.mask |= PCOLOR
      diffs._pcolor = pcolor
    }
    if ((otherPatch.mask & PLABEL) == PLABEL && !plabel.equals(otherPatch.plabel)) {
      plabel = otherPatch.plabel
      diffs.mask |= PLABEL
      diffs.plabel = plabel
    }
    if ((otherPatch.mask & PLABEL_COLOR) == PLABEL_COLOR && !plabelColor.equals(otherPatch.plabelColor)) {
      plabelColor = otherPatch.plabelColor
      diffs.mask |= PLABEL_COLOR
      diffs.plabelColor = plabelColor
    }
    if (diffs.mask != DEAD)
      diffs else null
  }

  override def serialize(out: DataOutputStream) {
    out.writeLong(id)
    out.writeShort(mask.toShort)
    if ((mask & PXCOR) == PXCOR)
      out.writeInt(pxcor)
    if ((mask & PYCOR) == PYCOR)
      out.writeInt(pycor)
    if ((mask & PCOLOR) == PCOLOR)
      out.writeInt(getARGBIntByRGBAList(pcolor))
    if ((mask & PLABEL) == PLABEL)
      out.writeUTF(plabel)
    if ((mask & PLABEL_COLOR) == PLABEL_COLOR)
      out.writeInt(api.Color.getARGBIntByRGBAList(plabelColor))
  }
}

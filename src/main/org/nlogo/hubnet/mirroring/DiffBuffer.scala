// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import java.awt.image.BufferedImage

object DiffBuffer {
  val EMPTY       = 0x0000
  val MINX        = 0x0001
  val MINY        = 0x0002
  val SHAPES      = 0x0004
  val FONT_SIZE   = 0x0008
  val MAXX        = 0x0010
  val MAXY        = 0x0020
  val TURTLES     = 0x0040
  val PATCHES     = 0x0080
  val LINKS       = 0x0100
  val DRAWING     = 0x0200
  val WRAPX       = 0x0400
  val WRAPY       = 0x0800
  val PERSPECTIVE = 0x1000
  val EVERYTHING = (MINX | MINY | MAXX | MAXY | SHAPES | FONT_SIZE |
    TURTLES | PATCHES | LINKS | WRAPX | WRAPY | PERSPECTIVE).toShort
}

class DiffBuffer {

  import DiffBuffer._

  private var mask = EMPTY
  private var minPxcor = 0
  private var maxPxcor = 0
  private var minPycor = 0
  private var maxPycor = 0
  private var fontSize = 0
  private var xWrap = false
  private var yWrap = false
  private var shapes = false
  private var perspective: AgentPerspective = null
  private var drawing: java.awt.image.BufferedImage = null

  private val patchDiffs  = collection.mutable.ArrayBuffer[PatchData]()
  private val turtleDiffs = collection.mutable.ArrayBuffer[TurtleData]()
  private val linkDiffs   = collection.mutable.ArrayBuffer[LinkData]()

  def isEmpty = mask == EMPTY

  def addMinX(minPxcor: Int) { mask |= MINX; this.minPxcor = minPxcor }
  def addMinY(minPycor: Int) { mask |= MINY; this.minPycor = minPycor }
  def addMaxX(maxPxcor: Int) { mask |= MAXX; this.maxPxcor = maxPxcor }
  def addMaxY(maxPycor: Int) { mask |= MAXY; this.maxPycor = maxPycor }

  def addWrapX(xWrap: Boolean) { mask |= WRAPX; this.xWrap = xWrap }
  def addWrapY(yWrap: Boolean) { mask |= WRAPY; this.yWrap = yWrap }
  def addShapes(shapes: Boolean) { mask |= SHAPES; this.shapes = shapes }
  def addFontSize(fontSize: Int) { mask |= FONT_SIZE; this.fontSize = fontSize }
  def addTurtle(diffs: TurtleData) { mask |= TURTLES; turtleDiffs += diffs }
  def addLink(diffs: LinkData) { mask |= LINKS; linkDiffs += diffs }
  def addPatch(diffs: PatchData) { mask |= PATCHES; patchDiffs += diffs }
  def addDrawing(drawing: BufferedImage) { mask |= DRAWING; this.drawing = drawing }
  def addPerspective(perspective: AgentPerspective) { mask |= PERSPECTIVE; this.perspective = perspective }

  def serialize(out: java.io.DataOutputStream) {
    def serializeSeq(diffs: Seq[AgentData]) {
      out.writeInt(diffs.size)
      diffs.foreach(_.serialize(out))
    }
    out.writeShort(mask.toShort)
    if ((mask & MINX) == MINX) out.writeInt(minPxcor)
    if ((mask & MINY) == MINY) out.writeInt(minPycor)
    if ((mask & MAXX) == MAXX) out.writeInt(maxPxcor)
    if ((mask & MAXY) == MAXY) out.writeInt(maxPycor)
    if ((mask & SHAPES) == SHAPES) out.writeBoolean(shapes)
    if ((mask & FONT_SIZE) == FONT_SIZE) out.writeInt(fontSize)
    if ((mask & WRAPX) == WRAPX) out.writeBoolean(xWrap)
    if ((mask & WRAPY) == WRAPY) out.writeBoolean(yWrap)
    if ((mask & PERSPECTIVE) == PERSPECTIVE) perspective.serialize(out)
    if ((mask & PATCHES) == PATCHES) serializeSeq(patchDiffs)
    if ((mask & TURTLES) == TURTLES) serializeSeq(turtleDiffs)
    if ((mask & LINKS) == LINKS) serializeSeq(linkDiffs)
    if ((mask & DRAWING) == DRAWING)
      javax.imageio.ImageIO.write(drawing, "PNG", out)
  }

  def toByteArray = {
    val out = new java.io.ByteArrayOutputStream
    serialize(new java.io.DataOutputStream(out))
    out.toByteArray
  }

}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, AgentKind }

@annotation.strictfp
class Torus3D(world: World3D) extends Torus(world) with Topology3D {

  def distanceWrap(dx: Double, dy: Double, dz: Double,
      x1: Double, y1: Double, z1: Double,
      x2: Double, y2: Double, z2: Double): Double = {
    val dx2 =
      if (x1 > x2)
        x2 + world.worldWidth - x1
      else
        x2 - world.worldWidth - x1
    val dx3 =
      if (StrictMath.abs(dx2) < StrictMath.abs(dx))
        dx2
      else
        dx
    val dy2 =
      if (y1 > y2)
        y2 + world.worldHeight - y1
      else
        y2 - world.worldHeight - y1
    val dy3 =
      if (StrictMath.abs(dy2) < StrictMath.abs(dy))
        dy2
      else
        dy
    val dz2 =
      if (z1 > z2)
        z2 + world.worldDepth - z1
      else
        z2 - world.worldDepth - z1
    val dz3 =
      if (StrictMath.abs(dz2) < StrictMath.abs(dz))
        dz2
      else
        dz
    StrictMath.sqrt(dx3 * dx3 + dy3 * dy3 + dz3 * dz3)
  }

  def towardsPitchWrap(dx: Double, dy: Double, dz: Double): Double = {
    val dx2 = Topology.wrap(dx,
      world.worldWidth / -2.0,
      world.worldWidth / 2.0)
    val dy2 = Topology.wrap(dy,
      world.worldHeight / -2.0,
      world.worldHeight / 2.0)
    val dz2 = Topology.wrap(dz,
      world.worldDepth / -2.0,
      world.worldDepth / 2.0)
    (360 + StrictMath.toDegrees
        (StrictMath.atan(dz2 / StrictMath.sqrt(dx2 * dx2 + dy2 * dy2)))) % 360
  }

  def getNeighbors3d(source: Patch3D): AgentSet =
    AgentSet.fromArray(AgentKind.Patch,
        Array[Agent](
          getPN(source), getPE(source),
          getPS(source), getPW(source),
          getPNE(source), getPSE(source),
          getPSW(source), getPNW(source),
          getPU(source), getPD(source),
          getPNU(source), getPEU(source),
          getPSU(source), getPWU(source),
          getPNEU(source), getPSEU(source),
          getPSWU(source), getPNWU(source),
          getPND(source), getPED(source),
          getPSD(source), getPWD(source),
          getPNED(source), getPSED(source),
          getPSWD(source), getPNWD(source)))

  def getNeighbors6(source: Patch3D): AgentSet =
    AgentSet.fromArray(AgentKind.Patch,
      Array[Agent](
        getPN(source), getPE(source),
        getPS(source), getPW(source),
        getPU(source), getPD(source)))

  override def getPN(source: Patch): Patch =
    world.fastGetPatchAt(
      source.pxcor,
      if (source.pycor == world.maxPycor)
        world.minPycor
      else
        source.pycor + 1,
      source.asInstanceOf[Patch3D].pzcor)
  override def getPE(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.maxPxcor)
        world.minPxcor
      else
        source.pxcor + 1,
      source.pycor,
      source.asInstanceOf[Patch3D].pzcor)
  override def getPS(source: Patch): Patch =
    world.fastGetPatchAt(
      source.pxcor,
      if (source.pycor == world.minPycor)
        world.maxPycor
      else
        source.pycor - 1,
      source.asInstanceOf[Patch3D].pzcor)
  override def getPW(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.minPxcor)
        world.maxPxcor
      else
        source.pxcor - 1,
      source.pycor,
      source.asInstanceOf[Patch3D].pzcor)
  override def getPNE(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.maxPxcor)
        world.minPxcor
      else
        source.pxcor + 1,
      if (source.pycor == world.maxPycor)
        world.minPycor
      else
        source.pycor + 1,
      source.asInstanceOf[Patch3D].pzcor)
  override def getPSE(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.maxPxcor)
        world.minPxcor
      else
        source.pxcor + 1,
      if (source.pycor == world.minPycor)
        world.maxPycor
      else
        source.pycor - 1,
      source.asInstanceOf[Patch3D].pzcor)
  override def getPSW(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.minPxcor)
        world.maxPxcor
      else
        source.pxcor - 1,
      if (source.pycor == world.minPycor)
        world.maxPycor
      else
        source.pycor - 1,
      source.asInstanceOf[Patch3D].pzcor)
  override def getPNW(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.minPxcor)
        world.maxPxcor
      else
        source.pxcor - 1,
      if (source.pycor == world.maxPycor)
        world.minPycor
      else
        source.pycor + 1,
      source.asInstanceOf[Patch3D].pzcor)

  def getPNU(source: Patch3D): Patch =
    getPN(getPU(source))
  def getPEU(source: Patch3D): Patch =
    getPE(getPU(source))
  def getPSU(source: Patch3D): Patch =
    getPS(getPU(source))
  def getPWU(source: Patch3D): Patch =
    getPW(getPU(source))
  def getPNEU(source: Patch3D): Patch =
    getPNE(getPU(source))
  def getPSEU(source: Patch3D): Patch =
    getPSE(getPU(source))
  def getPSWU(source: Patch3D): Patch =
    getPSW(getPU(source))
  def getPNWU(source: Patch3D): Patch =
    getPNW(getPU(source))
  def getPND(source: Patch3D): Patch =
    getPN(getPD(source))
  def getPED(source: Patch3D): Patch =
    getPE(getPD(source))
  def getPSD(source: Patch3D): Patch =
    getPS(getPD(source))
  def getPWD(source: Patch3D): Patch =
    getPW(getPD(source))
  def getPNED(source: Patch3D): Patch =
    getPNE(getPD(source))
  def getPSED(source: Patch3D): Patch =
    getPSE(getPD(source))
  def getPSWD(source: Patch3D): Patch =
    getPSW(getPD(source))
  def getPNWD(source: Patch3D): Patch =
    getPNW(getPD(source))

  def wrapZ(z: Double): Double =
    Topology.wrap(z, world.minPzcor - 0.5, world.maxPzcor + 0.5)

  def getPU(source: Patch3D): Patch =
    world.fastGetPatchAt(source.pxcor, source.pycor,
      if (source.pzcor == world.maxPzcor)
        world.minPzcor
      else
        source.pzcor + 1)

  def getPD(source: Patch3D): Patch =
    world.fastGetPatchAt(source.pxcor, source.pycor,
      if (source.pzcor == world.minPzcor)
        world.maxPzcor
      else
        source.pzcor - 1)

  def shortestPathZ(z1: Double, z2: Double): Double = {
    val depth = world.worldDepth
    val zprime =
      if (z1 > z2)
        z2 + depth
       else
        z2 - depth
    if (StrictMath.abs(z2 - z1) > StrictMath.abs(zprime - z1))
      zprime
    else
      z2
  }

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  override def diffuse(amount: Double, vn: Int) {
    val xx = world.worldWidth
    val xx2 = xx * 2
    val yy = world.worldHeight
    val yy2 = yy * 2
    val zz = world.worldDepth
    val zz2 = zz * 2
    val scratch = world.getPatchScratch3d
    var x, y, z = 0
    try while (z < zz) {
      y = 0
      while (y < yy) {
        x = 0
        while (x < xx) {
          scratch(x)(y)(z) =
            world.fastGetPatchAt(wrapX(x).toInt, wrapY(y).toInt, wrapZ(z).toInt)
              .getPatchVariable(vn)
              .asInstanceOf[java.lang.Double].doubleValue
          x += 1
        }
        y += 1
      }
      z += 1
    }
    catch { case _: ClassCastException =>
        throw new PatchException(
          world.fastGetPatchAt(wrapX(x).toInt, wrapY(y).toInt, wrapZ(z).toInt)) }
    z = zz
    while (z < zz2) {
      y = yy
      while (y < yy2) {
        x = xx
        while (x < xx2) {
          val sum =
            scratch((x - 1) % xx)((y - 1) % yy)((z) % zz) +
            scratch((x - 1) % xx)((y) % yy)((z) % zz) +
            scratch((x - 1) % xx)((y + 1) % yy)((z) % zz) +
            scratch((x) % xx)((y - 1) % yy)((z) % zz) +
            scratch((x) % xx)((y + 1) % yy)((z) % zz) +
            scratch((x + 1) % xx)((y - 1) % yy)((z) % zz) +
            scratch((x + 1) % xx)((y) % yy)((z) % zz) +
            scratch((x + 1) % xx)((y + 1) % yy)((z) % zz) +
            scratch((x - 1) % xx)((y - 1) % yy)((z - 1) % zz) +
            scratch((x - 1) % xx)((y) % yy)((z - 1) % zz) +
            scratch((x - 1) % xx)((y + 1) % yy)((z - 1) % zz) +
            scratch((x) % xx)((y - 1) % yy)((z - 1) % zz) +
            scratch((x) % xx)((y + 1) % yy)((z - 1) % zz) +
            scratch((x + 1) % xx)((y - 1) % yy)((z - 1) % zz) +
            scratch((x + 1) % xx)((y) % yy)((z - 1) % zz) +
            scratch((x + 1) % xx)((y + 1) % yy)((z - 1) % zz) +
            scratch((x) % xx)((y) % yy)((z - 1) % zz) +
            scratch((x - 1) % xx)((y - 1) % yy)((z + 1) % zz) +
            scratch((x - 1) % xx)((y) % yy)((z + 1) % zz) +
            scratch((x - 1) % xx)((y + 1) % yy)((z + 1) % zz) +
            scratch((x) % xx)((y - 1) % yy)((z + 1) % zz) +
            scratch((x) % xx)((y + 1) % yy)((z + 1) % zz) +
            scratch((x + 1) % xx)((y - 1) % yy)((z + 1) % zz) +
            scratch((x + 1) % xx)((y) % yy)((z + 1) % zz) +
            scratch((x + 1) % xx)((y + 1) % yy)((z + 1) % zz) +
            scratch((x) % xx)((y) % yy)((z + 1) % zz)
          val oldval = scratch(x - xx)(y - yy)(z - zz)
          val newval = oldval * (1.0 - amount) + (sum / 26) * amount
          if (newval != oldval)
            world.getPatchAt(x - xx, y - yy, z - zz)
                .setPatchVariable(vn, Double.box(newval))
          x += 1
        }
        y += 1
      }
      z += 1
    }
  }

}

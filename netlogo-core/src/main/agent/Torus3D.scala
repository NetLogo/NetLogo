// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.{ core, api},
  core.AgentKind,
  api.AgentException
import Topology.wrap

class Torus3D(_world3d: World3D) extends Torus(_world3d) with Topology3D {
  def getNeighbors3d(source: Patch3D): IndexedAgentSet =
    AgentSet.fromArray(AgentKind.Patch,
      Array[Agent](getPN(source), getPE(source),
        getPS(source), getPW(source),
        getPNE(source), getPSE(source),
        getPSW(source), getPNW(source),
        getPatchUp(source), getPatchDown(source),
        getPNU(source), getPEU(source),
        getPSU(source), getPWU(source),
        getPNEU(source), getPSEU(source),
        getPSWU(source), getPNWU(source),
        getPND(source), getPED(source),
        getPSD(source), getPWD(source),
        getPNED(source), getPSED(source),
        getPSWD(source), getPNWD(source)
        ))

  def getNeighbors6(source: Patch3D): IndexedAgentSet =
    AgentSet.fromArray(AgentKind.Patch,
      Array[Agent](getPN(source), getPE(source),
        getPS(source), getPW(source),
        getPatchUp(source), getPatchDown(source)))

  override def getPN(source: Patch): Patch =
    _world3d.fastGetPatchAt(
      source.pxcor,
      if (source.pycor == world.maxPycor)
        world.minPycor
      else
        source.pycor + 1,
      source.asInstanceOf[Patch3D].pzcor)
  override def getPE(source: Patch): Patch =
    _world3d.fastGetPatchAt(
      if (source.pxcor == world.maxPxcor)
        world.minPxcor
      else
        source.pxcor + 1,
      source.pycor,
      source.asInstanceOf[Patch3D].pzcor)
  override def getPS(source: Patch): Patch =
    _world3d.fastGetPatchAt(
      source.pxcor,
      if (source.pycor == world.minPycor)
        world.maxPycor
      else
        source.pycor - 1,
      source.asInstanceOf[Patch3D].pzcor)
  override def getPW(source: Patch): Patch =
    _world3d.fastGetPatchAt(
      if (source.pxcor == world.minPxcor)
        world.maxPxcor
      else
        source.pxcor - 1,
      source.pycor,
      source.asInstanceOf[Patch3D].pzcor)
  override def getPNE(source: Patch): Patch =
    _world3d.fastGetPatchAt(
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
    _world3d.fastGetPatchAt(
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
    _world3d.fastGetPatchAt(
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
    _world3d.fastGetPatchAt(
      if (source.pxcor == world.minPxcor)
        world.maxPxcor
      else
        source.pxcor - 1,
      if (source.pycor == world.maxPycor)
        world.minPycor
      else
        source.pycor + 1,
      source.asInstanceOf[Patch3D].pzcor)

  def getPNU(source: Patch3D): Patch = getPN(getPatchUp(source))
  def getPEU(source: Patch3D): Patch = getPE(getPatchUp(source))
  def getPSU(source: Patch3D): Patch = getPS(getPatchUp(source))
  def getPWU(source: Patch3D): Patch = getPW(getPatchUp(source))
  def getPNEU(source: Patch3D): Patch = getPNE(getPatchUp(source))
  def getPSEU(source: Patch3D): Patch = getPSE(getPatchUp(source))
  def getPSWU(source: Patch3D): Patch = getPSW(getPatchUp(source))
  def getPNWU(source: Patch3D): Patch = getPNW(getPatchUp(source))
  def getPND(source: Patch3D): Patch = getPN(getPatchDown(source))
  def getPED(source: Patch3D): Patch = getPE(getPatchDown(source))
  def getPSD(source: Patch3D): Patch = getPS(getPatchDown(source))
  def getPWD(source: Patch3D): Patch = getPW(getPatchDown(source))
  def getPNED(source: Patch3D): Patch = getPNE(getPatchDown(source))
  def getPSED(source: Patch3D): Patch = getPSE(getPatchDown(source))
  def getPSWD(source: Patch3D): Patch = getPSW(getPatchDown(source))
  def getPNWD(source: Patch3D): Patch = getPNW(getPatchDown(source))

  def getPatchUp(source: Patch3D): Patch = {
    if (source.pzcor == _world3d.maxPzcor)
      _world3d.fastGetPatchAt(source.pxcor, source.pycor, _world3d.minPzcor)
    else
      _world3d.fastGetPatchAt(source.pxcor, source.pycor, source.pzcor + 1)
  }

  def getPatchDown(source: Patch3D): Patch = {
    if (source.pzcor == _world3d.minPzcor)
      _world3d.fastGetPatchAt(source.pxcor, source.pycor, _world3d.maxPzcor)
    else
      _world3d.fastGetPatchAt(source.pxcor, source.pycor, source.pzcor - 1)
  }

  def wrapZ(z: Double): Double =
    wrap(z, _world3d.minPzcor - 0.5, _world3d.maxPzcor + 0.5)

  def shortestPathZ(z1: Double, z2: Double): Double = {
    val depth = _world3d.worldDepth
    val zprime =
      if (z1 > z2) z2 + depth
      else         z2 - depth

    if (StrictMath.abs(z2 - z1) > StrictMath.abs(zprime - z1)) zprime
    else z2
  }

  def observerZ: Double =
    world.observer.ozcor

  def distanceWrap(_dx: Double, _dy: Double, _dz: Double,
    x1: Double, y1: Double, z1: Double,
    x2: Double, y2: Double, z2: Double): Double = {
      val dx2 =
        if (x1 > x2) (x2 + world.worldWidth) - x1
        else         (x2 - world.worldWidth) - x1
      val dx =
        if (StrictMath.abs(dx2) < StrictMath.abs(_dx)) dx2
        else _dx

    val dy2 =
      if (y1 > y2) (y2 + world.worldHeight) - y1
      else         (y2 - world.worldHeight) - y1;
    val dy =
      if (StrictMath.abs(dy2) < StrictMath.abs(_dy)) dy2
      else _dy

    val dz2 =
      if (z1 > z2) (z2 + _world3d.worldDepth) - z1
      else         (z2 - _world3d.worldDepth) - z1
    val dz =
      if (StrictMath.abs(dz2) < StrictMath.abs(_dz)) dz2
      else _dz

    StrictMath.sqrt(dx * dx + dy * dy + dz * dz)
  }

  def towardsPitchWrap(_dx: Double, _dy: Double, _dz: Double): Double = {
    val w = world.asInstanceOf[World3D]
    val dx = wrap(_dx, (-world.worldWidth.toDouble / 2.0),  (world.worldWidth / 2.0))
    val dy = wrap(_dy, (-world.worldHeight.toDouble / 2.0), (world.worldHeight / 2.0))
    val dz = wrap(_dz, (-w.worldDepth.toDouble / 2.0),      (w.worldDepth / 2.0))

    ((360 + StrictMath.toDegrees
        (StrictMath.atan(dz / StrictMath.sqrt(dx * dx + dy * dy)))) % 360)
  }

  @throws(classOf[AgentException])
  def getPatchAt(xc: Double, yc: Double, zc: Double): Patch =
    _world3d.getPatchAt(xc, yc, zc)

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  override def diffuse(diffuseparam: Double, vn: Int): Unit ={
    val w = _world3d

    val xx = w.worldWidth
    val xx2 = xx * 2
    val yy = w.worldHeight
    val yy2 = yy * 2
    val zz = w.worldDepth
    val zz2 = zz * 2
    val scratch = w.getPatchScratch3d
    var z = 0
    var y = 0
    var x = 0
    try {
      z = 0
      while (z < zz) {
        y = 0
        while (y < yy) {
          x = 0
          while (x < xx) {
            scratch(x)(y)(z) =
              w.fastGetPatchAt(wrapX(x).toInt, wrapY(y).toInt, wrapZ(z).toInt)
                .getPatchVariable(vn)
                .asInstanceOf[Number]
                .doubleValue
            x += 1
          }
          y += 1
        }
        z += 1
      }
    } catch {
      case ex: ClassCastException =>
        throw new PatchException(w.fastGetPatchAt(wrapX(x).toInt, wrapY(y).toInt, wrapZ(z).toInt))
    }

    z = zz
    while (z < zz2) {
      y = yy
      while (y < yy2) {
        x = xx
        while (x < xx2) {
          var sum: Double = 0.0
          sum =  scratch((x - 1) % xx)((y - 1) % yy)(z       % zz)
          sum += scratch((x - 1) % xx)(y       % yy)(z       % zz)
          sum += scratch((x - 1) % xx)((y + 1) % yy)(z       % zz)
          sum += scratch(x       % xx)((y - 1) % yy)(z       % zz)
          sum += scratch(x       % xx)((y + 1) % yy)(z       % zz)
          sum += scratch((x + 1) % xx)((y - 1) % yy)(z       % zz)
          sum += scratch((x + 1) % xx)(y       % yy)(z       % zz)
          sum += scratch((x + 1) % xx)((y + 1) % yy)(z       % zz)
          sum += scratch((x - 1) % xx)((y - 1) % yy)((z - 1) % zz)
          sum += scratch((x - 1) % xx)(y       % yy)((z - 1) % zz)
          sum += scratch((x - 1) % xx)((y + 1) % yy)((z - 1) % zz)
          sum += scratch(x       % xx)((y - 1) % yy)((z - 1) % zz)
          sum += scratch(x       % xx)((y + 1) % yy)((z - 1) % zz)
          sum += scratch((x + 1) % xx)((y - 1) % yy)((z - 1) % zz)
          sum += scratch((x + 1) % xx)(y       % yy)((z - 1) % zz)
          sum += scratch((x + 1) % xx)((y + 1) % yy)((z - 1) % zz)
          sum += scratch(x       % xx)(y       % yy)((z - 1) % zz)
          sum += scratch((x - 1) % xx)((y - 1) % yy)((z + 1) % zz)
          sum += scratch((x - 1) % xx)(y       % yy)((z + 1) % zz)
          sum += scratch((x - 1) % xx)((y + 1) % yy)((z + 1) % zz)
          sum += scratch(x       % xx)((y - 1) % yy)((z + 1) % zz)
          sum += scratch(x       % xx)((y + 1) % yy)((z + 1) % zz)
          sum += scratch((x + 1) % xx)((y - 1) % yy)((z + 1) % zz)
          sum += scratch((x + 1) % xx)(y       % yy)((z + 1) % zz)
          sum += scratch((x + 1) % xx)((y + 1) % yy)((z + 1) % zz)
          sum += scratch(x       % xx)(y       % yy)((z + 1) % zz)

          val oldval = scratch(x - xx)(y - yy)(z - zz)
          val newval = oldval * (1.0 - diffuseparam) + (sum / 26) * diffuseparam
          if (newval != oldval) {
            w.getPatchAt(x - xx, y - yy, z - zz)
              .setPatchVariable(vn, Double.box(newval))
          }

          x += 1
        }
        y += 1
      }
      z += 1
    }
  }
}

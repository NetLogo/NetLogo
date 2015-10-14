// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;

public final strictfp class Torus3D
    extends Torus
    implements Topology3D {
  public Torus3D(World3D world) {
    super(world);
  }

  public double distanceWrap(double dx, double dy, double dz,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2) {
    double dx2 = x1 > x2 ? (x2 + world.worldWidth()) - x1 :
        (x2 - world.worldWidth()) - x1;
    dx = StrictMath.abs(dx2) < StrictMath.abs(dx) ? dx2 : dx;

    double dy2 = y1 > y2 ? (y2 + world.worldHeight()) - y1 :
        (y2 - world.worldHeight()) - y1;
    dy = StrictMath.abs(dy2) < StrictMath.abs(dy) ? dy2 : dy;

    double dz2 = z1 > z2 ? (z2 + ((World3D) world).worldDepth()) - z1 :
        (z2 - ((World3D) world).worldDepth()) - z1;
    dz = StrictMath.abs(dz2) < StrictMath.abs(dz) ? dz2 : dz;

    return StrictMath.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public double towardsPitchWrap(double dx, double dy, double dz) {
    dx = wrap(dx, (-(double) world.worldWidth() / 2.0),
        (world.worldWidth() / 2.0));

    dy = wrap(dy, (-(double) world.worldHeight() / 2.0),
        (world.worldHeight() / 2.0));

    dz = wrap(dz, (-(double) ((World3D) world).worldDepth() / 2.0),
        (((World3D) world).worldDepth() / 2.0));

    return ((360 + StrictMath.toDegrees
        (StrictMath.atan(dz / StrictMath.sqrt(dx * dx + dy * dy)))) % 360);
  }

  public Patch getPatchAt(double xc, double yc, double zc)
      throws AgentException {
    return ((World3D) world).getPatchAt(xc, yc, zc);
  }

  @Override
  public void diffuse(double diffuseparam, int vn)
      throws AgentException, PatchException {
    World3D w = (World3D) world;

    int xx = w.worldWidth();
    int xx2 = xx * 2;
    int yy = w.worldHeight();
    int yy2 = yy * 2;
    int zz = w.worldDepth();
    int zz2 = zz * 2;
    double[][][] scratch = w.getPatchScratch3d();
    int x = 0, y = 0, z = 0;
    try {
      for (z = 0; z < zz; z++) {
        for (y = 0; y < yy; y++) {
          for (x = 0; x < xx; x++) {
            scratch[x][y][z] =
                ((Number) w.fastGetPatchAt((int) wrapX(x),
                    (int) wrapY(y),
                    (int) wrapZ(z))
                    .getPatchVariable(vn))
                    .doubleValue();
          }
        }
      }
    } catch (ClassCastException ex) {
      throw new PatchException(w.fastGetPatchAt
          ((int) wrapX(x), (int) wrapY(y), (int) wrapZ(z)));
    }

    for (z = zz; z < zz2; z++) {
      for (y = yy; y < yy2; y++) {
        for (x = xx; x < xx2; x++) {
          double sum;
          sum = scratch[(x - 1) % xx][(y - 1) % yy][(z) % zz];
          sum += scratch[(x - 1) % xx][(y) % yy][(z) % zz];
          sum += scratch[(x - 1) % xx][(y + 1) % yy][(z) % zz];
          sum += scratch[(x) % xx][(y - 1) % yy][(z) % zz];
          sum += scratch[(x) % xx][(y + 1) % yy][(z) % zz];
          sum += scratch[(x + 1) % xx][(y - 1) % yy][(z) % zz];
          sum += scratch[(x + 1) % xx][(y) % yy][(z) % zz];
          sum += scratch[(x + 1) % xx][(y + 1) % yy][(z) % zz];
          sum += scratch[(x - 1) % xx][(y - 1) % yy][(z - 1) % zz];
          sum += scratch[(x - 1) % xx][(y) % yy][(z - 1) % zz];
          sum += scratch[(x - 1) % xx][(y + 1) % yy][(z - 1) % zz];
          sum += scratch[(x) % xx][(y - 1) % yy][(z - 1) % zz];
          sum += scratch[(x) % xx][(y + 1) % yy][(z - 1) % zz];
          sum += scratch[(x + 1) % xx][(y - 1) % yy][(z - 1) % zz];
          sum += scratch[(x + 1) % xx][(y) % yy][(z - 1) % zz];
          sum += scratch[(x + 1) % xx][(y + 1) % yy][(z - 1) % zz];
          sum += scratch[(x) % xx][(y) % yy][(z - 1) % zz];
          sum += scratch[(x - 1) % xx][(y - 1) % yy][(z + 1) % zz];
          sum += scratch[(x - 1) % xx][(y) % yy][(z + 1) % zz];
          sum += scratch[(x - 1) % xx][(y + 1) % yy][(z + 1) % zz];
          sum += scratch[(x) % xx][(y - 1) % yy][(z + 1) % zz];
          sum += scratch[(x) % xx][(y + 1) % yy][(z + 1) % zz];
          sum += scratch[(x + 1) % xx][(y - 1) % yy][(z + 1) % zz];
          sum += scratch[(x + 1) % xx][(y) % yy][(z + 1) % zz];
          sum += scratch[(x + 1) % xx][(y + 1) % yy][(z + 1) % zz];
          sum += scratch[(x) % xx][(y) % yy][(z + 1) % zz];

          double oldval = scratch[x - xx][y - yy][z - zz];
          double newval =
              oldval * (1.0 - diffuseparam)
                  + (sum / 26) * diffuseparam;
          if (newval != oldval) {
            w.getPatchAt(x - xx, y - yy, z - zz)
                .setPatchVariable(vn, Double.valueOf(newval));
          }
        }
      }
    }
  }

  public AgentSet getNeighbors3d(Patch3D source) {
    return new ArrayAgentSet(Patch.class,
        new Agent[]{getPatchNorth(source), getPatchEast(source),
            getPatchSouth(source), getPatchWest(source),
            getPatchNorthEast(source), getPatchSouthEast(source),
            getPatchSouthWest(source), getPatchNorthWest(source),
            getPatchUp(source), getPatchDown(source),
            getPNU(source), getPEU(source),
            getPSU(source), getPWU(source),
            getPNEU(source), getPSEU(source),
            getPSWU(source), getPNWU(source),
            getPND(source), getPED(source),
            getPSD(source), getPWD(source),
            getPNED(source), getPSED(source),
            getPSWD(source), getPNWD(source)
        },
        world);
  }

  public AgentSet getNeighbors6(Patch3D source) {
    return new ArrayAgentSet(Patch.class,
        new Agent[]{getPatchNorth(source), getPatchEast(source),
            getPatchSouth(source), getPatchWest(source),
            getPatchUp(source), getPatchDown(source)
        },
        world);
  }

  public Patch getPNU(Patch3D source) {
    return getPatchNorth(getPatchUp(source));
  }

  public Patch getPEU(Patch3D source) {
    return getPatchEast(getPatchUp(source));
  }

  public Patch getPSU(Patch3D source) {
    return getPatchSouth(getPatchUp(source));
  }

  public Patch getPWU(Patch3D source) {
    return getPatchWest(getPatchUp(source));
  }

  public Patch getPNEU(Patch3D source) {
    return getPatchNorthEast(getPatchUp(source));
  }

  public Patch getPSEU(Patch3D source) {
    return getPatchSouthEast(getPatchUp(source));
  }

  public Patch getPSWU(Patch3D source) {
    return getPatchSouthWest(getPatchUp(source));
  }

  public Patch getPNWU(Patch3D source) {
    return getPatchNorthWest(getPatchUp(source));
  }

  public Patch getPND(Patch3D source) {
    return getPatchNorth(getPatchDown(source));
  }

  public Patch getPED(Patch3D source) {
    return getPatchEast(getPatchDown(source));
  }

  public Patch getPSD(Patch3D source) {
    return getPatchSouth(getPatchDown(source));
  }

  public Patch getPWD(Patch3D source) {
    return getPatchWest(getPatchDown(source));
  }

  public Patch getPNED(Patch3D source) {
    return getPatchNorthEast(getPatchDown(source));
  }

  public Patch getPSED(Patch3D source) {
    return getPatchSouthEast(getPatchDown(source));
  }

  public Patch getPSWD(Patch3D source) {
    return getPatchSouthWest(getPatchDown(source));
  }

  public Patch getPNWD(Patch3D source) {
    return getPatchNorthWest(getPatchDown(source));
  }


  public double observerZ() {
    return world.observer().ozcor();
  }

  public double wrapZ(double z) {
    World3D w = (World3D) world;
    return wrap(z, w.minPzcor() - 0.5, w.maxPzcor() + 0.5);
  }

  Patch getPatchUp(Patch3D source) {
    World3D w = (World3D) world;

    if (source.pzcor == w.maxPzcor()) {
      return w.fastGetPatchAt(source.pxcor, source.pycor, w.minPzcor());
    } else {
      return w.fastGetPatchAt(source.pxcor, source.pycor, source.pzcor + 1);
    }
  }

  Patch getPatchDown(Patch3D source) {
    World3D w = (World3D) world;

    if (source.pzcor == w.minPzcor()) {
      return w.fastGetPatchAt(source.pxcor, source.pycor, w.maxPzcor());
    } else {
      return w.fastGetPatchAt(source.pxcor, source.pycor, source.pzcor - 1);
    }
  }

  public double shortestPathZ(double z1, double z2) {
    double zprime;
    double depth = ((World3D) world).worldDepth();
    if (z1 > z2) {
      zprime = z2 + depth;
    } else {
      zprime = z2 - depth;
    }

    if (StrictMath.abs(z2 - z1) > StrictMath.abs(zprime - z1)) {
      z2 = zprime;
    }

    return z2;
  }
}

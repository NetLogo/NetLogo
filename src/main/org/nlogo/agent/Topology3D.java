package org.nlogo.agent;

import org.nlogo.api.AgentException;

public interface Topology3D {
  double distanceWrap(double dx, double dy, double dz,
                      double x1, double y1, double z1,
                      double x2, double y2, double z2);

  double towardsPitchWrap(double dx, double dy, double dz);

  Patch getPatchAt(double xc, double yc, double zc) throws AgentException;

  AgentSet getNeighbors3d(Patch3D source);

  AgentSet getNeighbors6(Patch3D source);

  Patch getPNU(Patch3D source);

  Patch getPEU(Patch3D source);

  Patch getPSU(Patch3D source);

  Patch getPWU(Patch3D source);

  Patch getPNEU(Patch3D source);

  Patch getPSEU(Patch3D source);

  Patch getPSWU(Patch3D source);

  Patch getPNWU(Patch3D source);

  Patch getPND(Patch3D source);

  Patch getPED(Patch3D source);

  Patch getPSD(Patch3D source);

  Patch getPWD(Patch3D source);

  Patch getPNED(Patch3D source);

  Patch getPSED(Patch3D source);

  Patch getPSWD(Patch3D source);

  Patch getPNWD(Patch3D source);

  double observerZ();

  double wrapZ(double z);

  double shortestPathZ(double z1, double z2);
}

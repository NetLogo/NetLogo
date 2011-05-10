package org.nlogo.agent;

import org.nlogo.api.AgentException;

public interface Agent3D {
  Patch3D getPatchAtOffsets(double dx, double dy, double dz) throws AgentException;
}

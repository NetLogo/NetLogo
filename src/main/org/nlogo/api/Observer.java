package org.nlogo.api;

/**
 * Interface provides access to NetLogo observer.
 */

public interface Observer extends Agent {
  /**
   * Returns the currently watched or followed agent (or nobody)
   */
  Agent targetAgent();

  /**
   * Returns the current perspective
   */
  Perspective perspective();

  /**
   * Returns the current distance behind the followed turtle the 3D view is displaying
   */
  int followDistance();

  double dist();

  double heading();

  double pitch();

  double roll();

  void setPerspective(Perspective p, Agent a);

  double oxcor();

  double oycor();

  double ozcor();

  double dx();

  double dy();

  double dz();
}

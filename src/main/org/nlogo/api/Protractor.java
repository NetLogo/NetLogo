package org.nlogo.api;

public interface Protractor {
  double towards(double fromX, double fromY,
                 double toX, double toY,
                 boolean wrap)
      throws AgentException;

  double towardsPitch(double fromX, double fromY, double fromZ,
                      double toX, double toY, double toZ,
                      boolean wrap)
      throws AgentException;
}

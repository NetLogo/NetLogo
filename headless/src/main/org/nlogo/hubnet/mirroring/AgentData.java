// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

public abstract strictfp class AgentData
    extends Overridable {
  public abstract double xcor();

  public abstract double ycor();

  public abstract double spotlightSize();

  public abstract boolean wrapSpotlight();
}

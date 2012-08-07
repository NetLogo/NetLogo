// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

public strictfp class HubNetPlotPoint
    implements java.io.Serializable {
  static final long serialVersionUID = 0L;

  /**
   * indicates whether or not this point specifies its X coordinate.
   * Points may leave the X coordinate unspecified, in which case the
   * next X coordinate in the client-side plot will be used.
   */
  private final boolean specifiesXCor;

  private double xcor = 0;
  private double ycor = 0;

  public HubNetPlotPoint(double x, double y) {
    specifiesXCor = true;
    xcor(x);
    ycor(y);
  }

  public HubNetPlotPoint(double y) {
    specifiesXCor = false;
    ycor(y);
  }

  public boolean specifiesXCor() {
    return specifiesXCor;
  }

  void xcor(double xcor) {
    this.xcor = xcor;
  }

  public double xcor() {
    return xcor;
  }

  void ycor(double ycor) {
    this.ycor = ycor;
  }

  public double ycor() {
    return ycor;
  }

  @Override
  public String toString() {
    return "plot-point@" + xcor + "," + ycor;
  }
}

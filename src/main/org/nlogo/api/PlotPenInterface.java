package org.nlogo.api;

public interface PlotPenInterface {
  int MIN_MODE = 0;
  int MAX_MODE = 2;

  void isDown_$eq(boolean isDown);

  void mode_$eq(int mode);

  void interval_$eq(double interval);

  void color_$eq(int color);

  void x_$eq(double x);

  void plot(double x, double y, int color, boolean isDown);

  String name();
}

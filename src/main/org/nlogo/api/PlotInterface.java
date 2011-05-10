package org.nlogo.api;

public interface PlotInterface {
  void xMin_$eq(double xmin);

  void xMax_$eq(double xmax);

  void yMin_$eq(double ymin);

  void yMax_$eq(double ymax);

  void autoPlotOn_$eq(boolean autoPlot);

  void legendIsOpen_$eq(boolean open);

  void currentPen_$eq(String pen);

  scala.Option<PlotPenInterface> getPen(String pen);

  String name();

  void makeDirty();
}


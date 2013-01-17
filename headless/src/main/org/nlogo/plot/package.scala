// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo

/**
 * This package contains classes for plotting.
 *
 * The lowest level class is PlotPoint.  Each point has x and y coordinates, a color, and a boolean for
 * whether the pen was down when the point was plotted.
 *
 * The next class up is PlotPen.  A PlotPen has a name.  The pen has a bunch of state variables (with
 * both default and current values) and remembers all the PlotPoints it has plotted.  The pen's mode
 * (e.g. line or bar) applies to all of its points.  A pen may be temporary or permanent; temporary
 * pens go away when the plot is cleared.
 *
 * A collection of PlotPens is a PlotPensList.  The pens are ordered, so they show up in a consistent
 * order in the legend.
 *
 * The main class is Plot.  A Plot has a name and a PlotPensList, and keeps track of which pen in the
 * PlotPensList is the current pen.  A Plot has both default and current values for its ranges (x min
 * and max, y min and max).  "Auto plot" (whether the range expands automatically to the fit the data)
 * is a boolean with both default and current values.
 *
 * PlotExporter is used to export the plot data in CSV format.
 *
 */

package object plot

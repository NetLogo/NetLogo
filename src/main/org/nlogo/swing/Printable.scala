// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

// this is just like in java.awt.print.Printable, but also takes a fileName.

trait Printable {
  def print(g: java.awt.Graphics, pageFormat: java.awt.print.PageFormat, pageIndex: Int, printer: PrinterManager): Int
}

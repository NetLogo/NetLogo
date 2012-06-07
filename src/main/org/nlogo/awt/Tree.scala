// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.{ Component, Container }

/** Sometimes useful when debugging */

object Tree {

  /**
   * Prints the component hierarchy to stdout.
   *
   * @param root where to begin
   */
  def printComponentTree(root: Component) {
    walkComponentTree(root, 0, printWalker)
  }

  val printWalker =
    new ComponentTreeWalker {
      override def touch(comp: Component, level: Int) {
        println(indent(level * 2) + comp.getClass + ", " + "bounds: " + comp.getBounds)
      }}

  /// helpers useful in tree walkers

  def indent(n: Int): String =
    List.fill(n)(' ').mkString

  trait ComponentTreeWalker {
    def touch(comp: Component, level: Int);
  }

  def walkComponentTree(comp: Component, level: Int, walker: ComponentTreeWalker) {
    walker.touch(comp, level)
    comp match {
      case container: Container =>
        for(subcomp <- container.getComponents)
          walkComponentTree(subcomp, level + 1, walker)
      case _ =>
    }
  }

}

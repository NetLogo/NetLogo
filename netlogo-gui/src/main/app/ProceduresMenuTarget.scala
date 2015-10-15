// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

// Normally we create interfaces like these to resolve an inter-package dependency problem. In this
// case, we're only keeping ProceduresMenu from depending on ProceduresTab, which maybe isn't
// strictly necessary since they're both in the app package, but it's still kind of nice. - ST
// 2/2/09

import org.nlogo.api.{CompilerServices}
import org.nlogo.core.Program

trait ProceduresMenuTarget {
  def compiler: CompilerServices
  def select(pos1: Int, pos2: Int): Unit
  def getText: String
  def program: Program
}

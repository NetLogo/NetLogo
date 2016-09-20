// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.Reference

trait Referencer extends Command {
  def referenceIndex: Int
  def applyReference(ref: Reference): Command
}

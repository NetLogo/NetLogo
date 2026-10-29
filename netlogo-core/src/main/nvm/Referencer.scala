// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ AgentKind, Reference }

trait Referencer extends Command {
  def referenceIndex: Int
  def referenceKind: Option[AgentKind]
  def applyReference(ref: Reference): Command
}

trait ReferencerReporter extends Reporter {
  def referenceIndex: Int
  def referenceKind: Option[AgentKind]
  def applyReference(ref: Reference): Reporter
}

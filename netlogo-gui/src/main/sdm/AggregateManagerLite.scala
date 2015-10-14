// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import org.nlogo.api.{ AggregateManagerInterface, CompilerServices }

class AggregateManagerLite extends AggregateManagerInterface {

  private def unsupported = throw new UnsupportedOperationException

  def load(lines: String, compiler: CompilerServices) {
    source = Loader.load(lines, compiler)
  }

  /// implementations of SourceOwner methods
  var source = ""
  def innerSource = source
  def innerSource(s: String) = unsupported
  def classDisplayName = "System Dynamics"
  def agentClass = unsupported
  def headerSource = ""

  /// these AggregateManagerInterface methods aren't relevant when running headless
  def showEditor() = unsupported
  def save() = unsupported

}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import org.nlogo.api.{ AggregateManagerInterface, ParserServices }

class AggregateManagerLite extends AggregateManagerInterface {

  private def unsupported = throw new UnsupportedOperationException

  def load(lines: String, parser: ParserServices) {
    source = Loader.load(lines, parser)
  }

  /// implementations of SourceOwner methods
  var source = ""
  def innerSource = source
  def innerSource_=(s: String) = unsupported
  def classDisplayName = "System Dynamics"
  def kind = unsupported
  def headerSource = ""

  /// these AggregateManagerInterface methods aren't relevant when running headless
  def showEditor() = unsupported
  def save() = unsupported

}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// start and end are measured in tokens, relative to the beginning of the procedure body

case class Let(name: String, start: Int, end: Int)

object Let {
  // sometimes we're only using a Let for its identity, so we don't need
  // meaningful values in the fields
  def apply(): Let = apply(null, -1, -1)
}

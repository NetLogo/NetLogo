// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core

/**
  * Source owners are usually parts of the UI.  Code comes from buttons, from monitors, from the
  * Code tab, from the Command Center, and so on.
  *
  * SourceOwner has mainly to do with compilation and with what to do if an error occurs during
  * compilation.
  *
  * "Header source" is distinguished from "inner source" because the latter is the code the user
  * actually wrote, while header and footer source is extra code we wrapped around it.  This matters
  * when showing code and showing positions of errors in the UI; we never want to show the user
  * header/footer source, but error locations need to take the length of the header source into
  * account.
  *
  * See also JobOwner, which extends SourceOwner and adds methods having to do with runtime behavior
  * and runtime error-handling.
  */

trait SourceOwner {
  def classDisplayName: String
  def headerSource: String
  def innerSource: String
  def innerSource(s: String)
  def source: String
  def kind: core.AgentKind
}

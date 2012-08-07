// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.util.MersenneTwisterFast

class SimpleJobOwner(override val displayName: String,
                     override val random: MersenneTwisterFast,
                     override val kind: AgentKind = AgentKind.Observer)
extends JobOwner {
  override def isButton = false
  override def isTurtleForeverButton = false
  override def isLinkForeverButton = false
  override def ownsPrimaryJobs = false
  override def isCommandCenter = false
  override def classDisplayName = kind.toString
  override def headerSource = ""
  override def innerSource = ""
  override def source = ""
  override def innerSource(s: String) { }
}

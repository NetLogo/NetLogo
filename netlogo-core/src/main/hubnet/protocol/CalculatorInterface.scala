// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.protocol

import org.nlogo.api.HubNetInterface.ClientInterface

@SerialVersionUID(0)
case class CalculatorInterface(activityName: String, tagSet: Seq[String]) extends ClientInterface {
  def containsViewWidget = false
  def containsWidgetTag(tag: String) = tagSet.contains(tag)
}

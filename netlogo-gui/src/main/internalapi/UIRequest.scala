// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.api.Agent

case class UIRequest(display: UIDisplay, continue: SuspendableJob)

sealed trait UIDisplay

case class ShowMessage(message: String) extends UIDisplay

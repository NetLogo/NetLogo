// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

class ClientAWTEvent(source: AnyRef, val info: AnyRef, val receivedData: Boolean = false)
  extends java.awt.AWTEvent(source, java.awt.AWTEvent.RESERVED_ID_MAX + 3)

class ClientAWTExceptionEvent(source: AnyRef, e: Exception, val sendingException: Boolean)
  extends ClientAWTEvent(source, e, false)

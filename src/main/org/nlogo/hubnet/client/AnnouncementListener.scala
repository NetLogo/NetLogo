// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.hubnet.protocol.DiscoveryMessage

/**
 * Defines an object that listens for discovery messages
 * @see DiscoveryListener
 */
trait AnnouncementListener {
  /**
   * Called when a HubNet server announcement is received
   */
  def announcementReceived(message: DiscoveryMessage)
}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.connection

import java.net.{ InetAddress, Inet4Address, NetworkInterface }
import java.util.prefs.{ Preferences => JavaPreferences }

import scala.jdk.CollectionConverters.EnumerationHasAsScala

object NetworkUtils {
  private val hubnetPrefs = JavaPreferences.userRoot.node("/org/nlogo/HubNet")
  val NetworkPreferenceKey = "preferred_network_interface"

  def findViableInterfaces: Seq[(NetworkInterface, InetAddress)] = {
    NetworkInterface.getNetworkInterfaces
      .asScala
      .toSeq
      .filter(i => !i.isLoopback && i.isUp)
      .flatMap(i => i.getInetAddresses.asScala.toSeq.map(a => (i, a)))
      .collect {
        case (i, a: Inet4Address) => (i, a)
      }
  }

  def loopbackInterface: Option[(NetworkInterface, InetAddress)] = {
    NetworkInterface.getNetworkInterfaces
      .asScala
      .toSeq
      .filter(_.isLoopback)
      .flatMap(i => i.getInetAddresses.asScala.toSeq.map(a => (i, a)))
      .collect {
        case (i, a: Inet4Address) => (i, a)
      }
      .headOption
  }

  def rememberNetworkInterface(n: NetworkInterface): Unit = {
    hubnetPrefs.put(NetworkPreferenceKey, n.getName)
  }

  def forgetNetworkInterface(): Unit = {
    hubnetPrefs.put(NetworkPreferenceKey, "")
  }

  def recallNetworkInterface: Option[NetworkInterface] =
    Option(NetworkInterface.getByName(hubnetPrefs.get(NetworkPreferenceKey, "")))
}

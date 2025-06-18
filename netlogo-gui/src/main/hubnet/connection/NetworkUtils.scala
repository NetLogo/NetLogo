// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.connection

import java.net.{ InetAddress, Inet4Address, NetworkInterface }

import scala.jdk.CollectionConverters.EnumerationHasAsScala

import org.nlogo.core.NetLogoPreferences

object NetworkUtils {
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
    NetLogoPreferences.put(NetworkPreferenceKey, n.getName)
  }

  def forgetNetworkInterface(): Unit = {
    NetLogoPreferences.put(NetworkPreferenceKey, "")
  }

  def recallNetworkInterface: Option[NetworkInterface] =
    Option(NetworkInterface.getByName(NetLogoPreferences.get(NetworkPreferenceKey, "")))
}

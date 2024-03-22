// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.connection

import java.net.{ Inet4Address, NetworkInterface }

import org.scalatest.funsuite.AnyFunSuite

class NetworkUtilsTest extends AnyFunSuite {
  test("findViableInterfaces should not return empty") {
    assert(! NetworkUtils.findViableInterfaces.isEmpty)
  }
  test("findViableInterfaces does not return loopback interfaces") {
    assert(NetworkUtils.findViableInterfaces.forall(i => ! i._1.isLoopback))
  }
  test("findViableInterfaces does not return disabled interfaces") {
    assert(NetworkUtils.findViableInterfaces.forall(i => i._1.isUp))
  }
  test("findViableInterfaces does not return interfaces with only IP6 addresses") {
    assert(NetworkUtils.findViableInterfaces.forall(i => i._2.isInstanceOf[Inet4Address]))
  }

  test("no interface is recalled after being forgotten") {
    NetworkUtils.forgetNetworkInterface()
    assertResult(None)(NetworkUtils.recallNetworkInterface)
  }

  test("an interface can be recalled after it has been remembered") {
    import scala.collection.JavaConverters._

    NetworkUtils.forgetNetworkInterface()
    val ni = NetworkInterface.getNetworkInterfaces.asScala.next()
    NetworkUtils.rememberNetworkInterface(ni)
    assertResult(Some(ni))(NetworkUtils.recallNetworkInterface)
  }
}

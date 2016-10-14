// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.core.{ DummyExtensionManager, DummyCompilationEnvironment }
import org.nlogo.api.ConfigurableModelLoader
import org.nlogo.fileformat.{ defaultConverter, NLogoFormat, NLogoHubNetFormat }
import org.nlogo.hubnet.connection.HubNetException
import org.nlogo.workspace.DummyAbstractWorkspace
import org.scalatest.FunSuite

class HubNetManagerTests extends FunSuite {

  hubnetTest("test Update View False") { (manager, connectionManager) =>
    HubNetUtils.viewMirroring=false
    // we need to reset after changing clientsHaveView.
    manager.reset()
    manager.incrementalUpdateFromEventThread()
    assert("[]" === connectionManager.getResults)
  }
  hubnetTest("test Update View True") { (manager, connectionManager) =>
    HubNetUtils.viewMirroring=true
    connectionManager._nodesHaveView=true
    //we need to reset after changing clientsHaveView.
    manager.reset()
    manager.incrementalUpdateFromEventThread()
    // all patches are dirty, since they've just been created.
    assert("[UPDATE]" === connectionManager.getResults)
  }
  hubnetTest("test Full Update View False") { (manager, connectionManager) =>
    HubNetUtils.viewMirroring=false
    // we need to reset after changing clientsHaveView.
    manager.reset()
    manager.incrementalUpdateFromEventThread()
    assert("[]" === connectionManager.getResults)
  }
  hubnetTest("test Full Update View True") { (manager, connectionManager) =>
    HubNetUtils.viewMirroring=true
    connectionManager._nodesHaveView=true
    // we need to reset after changing clientsHaveView.
    manager.reset()
    manager.incrementalUpdateFromEventThread()
    // still empty, since no patches are dirty...
    assert("[UPDATE]" === connectionManager.getResults)
  }
  hubnetTest("test Broadcast Valid Tag True") { (m, cm) =>
    testBroadcastValidTag(m, cm)(validTag=true, useSend=false)
  }
  hubnetTest("test Broadcast Valid Tag False") { (m, cm) =>
    testBroadcastValidTag(m, cm)(validTag=false, useSend=false)
  }
  hubnetTest("test Send Valid Tag True With Client") { (m, cm) =>
    testValidTag(m, cm)(validTag=true, useSend=true, clientsExist=true)
  }
  hubnetTest("test Send Valid Tag False With Client") { (m, cm) =>
    testValidTag(m, cm)(validTag=false, useSend=true, clientsExist=true)
  }
  hubnetTest("test Send Valid Tag True WO Client") { (m, cm) =>
    testValidTag(m, cm)(validTag=true, useSend=true, clientsExist=false)
  }
  hubnetTest("test Send Valid Tag False WO Client") { (m, cm) =>
    testValidTag(m, cm)(validTag=false, useSend=true, clientsExist=false)
  }

  def hubnetTest(name: String)(f: (HubNetManager, MockConnectionManager) => Unit) {
    test(name){
      val workspace = new DummyAbstractWorkspace
      val loader = new ConfigurableModelLoader()
        .addFormat[Array[String], NLogoFormat](new NLogoFormat)
        .addSerializer[Array[String], NLogoFormat](new NLogoHubNetFormat(workspace))
      val manager = new HeadlessHubNetManager(workspace, loader, defaultConverter) {
        override val connectionManager = new MockConnectionManager(this, workspace)
      }
      manager.reset()
      f(manager, manager.connectionManager.asInstanceOf[MockConnectionManager])
      workspace.dispose()
    }
  }
  def testBroadcastValidTag(manager: HubNetManager, connectionManager: MockConnectionManager)
                           (validTag: Boolean, useSend: Boolean) {
    // broadcasts always attempt to send the message to all the clients,
    // so clientExist should be true for all broadcasts
    testValidTag(manager, connectionManager)(validTag, useSend, true)
  }
  def testValidTag(manager: HubNetManager, connectionManager: MockConnectionManager)
                  (validTag: Boolean, useSend: Boolean, clientsExist: Boolean) {
    connectionManager.validTag=validTag
    try {
      if(useSend) manager.send(if(clientsExist) List("foo") else List(), "fish", "bar")
      else manager.broadcast("fish", "bar")
      assert(!clientsExist || validTag)
    }
    catch { case hex: HubNetException => assert(clientsExist && ! validTag) }
  }
}

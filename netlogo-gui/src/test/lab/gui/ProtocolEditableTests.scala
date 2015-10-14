// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.scalatest.FunSuite

import org.nlogo.api.DummyCompilerServices
import org.nlogo.lab.{ ProtocolLoader, ProtocolSaver }
import org.nlogo.nvm.CompilerInterface
import org.nlogo.util.Femto

class ProtocolEditableTests extends FunSuite {
  val compiler = Femto.scalaSingleton(classOf[CompilerInterface],
    "org.nlogo.compiler.Compiler")
  // make sure all the protocols in test/protocols.xml survive a round trip through conversion to
  // ProtocolEditable and back. It would be possible to cut the dependency on the compiler
  // just by beefing up DummyCompilerServices a little, since all we're using the compiler for is to
  // parse nested lists of numbers and strings, but it doesn't seem worth the effort, especially
  // given that the whole thing where you write your value sets as lists is a temporary kludge.
  // (Admittedly a temporary kludge that, if it were a human child, would be in first grade right
  // now, I think...) - ST 12/30/08, 1/19/09
  test("round trip") {
    val workspace = new DummyCompilerServices {
      override def readFromString(s: String): AnyRef =
        compiler.readFromString(s, false)
    }
    val protocols =
      new ProtocolLoader(workspace).loadAll(
        new java.io.File("test/lab/protocols.xml"))
    assert(ProtocolSaver.save(protocols) ===
      ProtocolSaver.save(
        protocols.map(
          new ProtocolEditable(_, null, workspace, new AnyRef).get.get)))
  }
}

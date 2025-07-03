// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.scalatest.funsuite.AnyFunSuite

import org.nlogo.api.{ DummyCompilerServices, FileIO, GlobalsIdentifier, NetLogoLegacyDialect, NetLogoThreeDDialect,
                       Version }
import org.nlogo.core.{ Femto, LiteralParser }
import org.nlogo.fileformat.NLogoLabFormat
import org.nlogo.nvm.PresentationCompilerInterface
import org.nlogo.window.EditorColorizer

class ProtocolEditableTests extends AnyFunSuite {
  val compiler = Femto.get[PresentationCompilerInterface](
    "org.nlogo.compile.Compiler", if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect)
  val literalParser = Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")
  // make sure all the protocols in test/protocols.xml survive a round trip through conversion to
  // ProtocolEditable and back. It would be possible to cut the dependency on the compiler
  // just by beefing up DummyCompilerServices a little, since all we're using the compiler for is to
  // parse nested lists of numbers and strings, but it doesn't seem worth the effort, especially
  // given that the whole thing where you write your value sets as lists is a temporary kludge.
  // (Admittedly a temporary kludge that, if it were a human child, would be in high school right
  // now, I think...) - ST 12/30/08, 1/19/09, 5/12/16
  test("round trip") {
    val workspace = new DummyCompilerServices with GlobalsIdentifier {
      override def readFromString(s: String): AnyRef =
        compiler.readFromString(s)

      // required since ProtocolEditable's GUI instance needs to be able to check the validity of a variable
      // specification, but not meaningful in this context, so just pretend everything is valid (Isaac B 7/3/25)
      override def isGlobalVariable(name: String): Boolean =
        true
    }
    val protocolLines = FileIO.fileToString("test/lab/protocols.xml").linesIterator.toArray
    val protocols = new NLogoLabFormat(literalParser).load(protocolLines, None).get
    protocols.foreach { protocol =>
      val editedProtocol = new ProtocolEditable(protocol, null, workspace, new EditorColorizer(workspace), new AnyRef,
                                                Seq()).get.get
      assert(protocol == editedProtocol)
    }
  }
}

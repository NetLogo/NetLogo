// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.util.List

import org.nlogo.api.{ DummyCompilerServices, FileIO, GlobalsIdentifier, LabProtocol, NetLogoLegacyDialect,
                       NetLogoThreeDDialect, Version }
import org.nlogo.core.{ Femto, LiteralParser, WorldDimensions, WorldDimensions3D }
import org.nlogo.fileformat.FileFormat
import org.nlogo.nvm.PresentationCompilerInterface
import org.nlogo.util.AnyFunSuiteEx
import org.nlogo.window.EditorColorizer

class ProtocolEditableTests extends AnyFunSuiteEx {
  private val compiler = Femto.get[PresentationCompilerInterface](
    "org.nlogo.compile.Compiler", if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect)

  private val literalParser = Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")

  private val workspace = new DummyCompilerServices with GlobalsIdentifier {
    override def readFromString(s: String): AnyRef =
      compiler.readFromString(s)

    // required since ProtocolEditable's GUI instance needs to be able to check the validity of a variable
    // specification, but not meaningful in this context, so just pretend everything is valid (Isaac B 7/3/25)
    override def checkGlobalVariable(name: String, values: List[AnyRef]): Unit = {}
  }

  private val loader = FileFormat.standardAnyLoader(true, literalParser)

  // make sure all the protocols in test/protocols.xml survive a round trip through conversion to
  // ProtocolEditable and back. It would be possible to cut the dependency on the compiler
  // just by beefing up DummyCompilerServices a little, since all we're using the compiler for is to
  // parse nested lists of numbers and strings, but it doesn't seem worth the effort, especially
  // given that the whole thing where you write your value sets as lists is a temporary kludge.
  // (Admittedly a temporary kludge that, if it were a human child, would be in high school right
  // now, I think...) - ST 12/30/08, 1/19/09, 5/12/16
  test("round trip") {
    foreachProtocol("test/lab/protocols.xml") { protocol =>
      val editedProtocol = new ProtocolEditable(protocol, null, workspace, new EditorColorizer(workspace), new AnyRef,
                                                new WorldDimensions(-16, 16, -16, 16)).get.get

      assert(protocol == editedProtocol)
    }
  }

  test("rejects invalid world dimensions") {
    foreachProtocol("test/lab/protocols-invalid-dims.xml") { protocol =>
      val editable = new ProtocolEditable(protocol, null, workspace, new EditorColorizer(workspace), new AnyRef,
                                          new WorldDimensions(-16, 16, -16, 16))

      if (editable.errorString.isEmpty)
        fail(s"Experiment \"${protocol.name}\" did not reject invalid world dimensions.")
    }
  }

  if (Version.is3D) {
    test("rejects invalid world dimensions 3D") {
      foreachProtocol("test/lab/protocols-invalid-dims-3d.xml") { protocol =>
        val editable = new ProtocolEditable(protocol, null, workspace, new EditorColorizer(workspace), new AnyRef,
                                            WorldDimensions3D.box(16))

        if (editable.errorString.isEmpty)
          fail(s"Experiment \"${protocol.name}\" did not reject invalid world dimensions.")
      }
    }
  }

  private def foreachProtocol(file: String)(func: LabProtocol => Unit): Unit = {
    loader.readExperiments(FileIO.fileToString(file), false, Set()).get._1.foreach(func)
  }
}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.LogoListBuilder
import org.nlogo.core.{ AgentKind, I18N, Reference }
import org.nlogo.nvm.{ Context, ReferencerReporter, Reporter, RuntimePrimitiveException }

class _reference extends Reporter with ReferencerReporter {
  override def referenceIndex = 0

  private var reference: Reference = null

  override def applyReference(ref: Reference): Reporter = {
    reference = ref
    this
  }

  override def report(context: Context): AnyRef = {
    if (reference == null) {
      throw new RuntimePrimitiveException(context, this, I18N.errors.getN("compiler.LetVariable.notDefined", args(0).token.text))
    } else {
      val builder = new LogoListBuilder()
      val kindString =
        reference.kind match {
          case AgentKind.Turtle   => "TURTLE"
          case AgentKind.Observer => "OBSERVER"
          case AgentKind.Patch    => "PATCH"
          case AgentKind.Link     => "LINK"
        }
      val varNames =
        (reference.kind match {
          case AgentKind.Turtle   => world.program.turtleVars.keys
          case AgentKind.Observer => world.program.globals
          case AgentKind.Patch    => world.program.patchVars.keys
          case AgentKind.Link     => world.program.linkVars.keys
        }).toSeq
      builder.add(kindString)
      builder.add(Double.box(reference.vn))
      builder.add(varNames(reference.vn))
      builder.toLogoList
    }
  }
}

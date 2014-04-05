// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.api.CompilerException
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _readfromstring extends Reporter {
  override def syntax =
    SyntaxJ.reporterSyntax(
      Array(Syntax.StringType),
      Syntax.ReadableType)
  override def report(context: Context): AnyRef =
    try workspace.readFromString(argEvalString(context, 0))
    catch {
      case e: CompilerException =>
        throw new EngineException(context, this, e.getMessage)
    }
}

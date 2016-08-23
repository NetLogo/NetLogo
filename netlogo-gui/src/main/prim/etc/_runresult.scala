// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ LogoException, AnonymousReporter }
import org.nlogo.core.{ I18N, Syntax }
import org.nlogo.core.CompilerException
import org.nlogo.nvm.{ Activation, AnonymousProcedure, ArgumentTypeException, Context, EngineException, Reporter }

class _runresult extends Reporter {

  override def report(context: Context) =
    args(0).report(context) match {
      case s: String =>
        if(args.size > 1)
          throw new EngineException(context, this,
            token.text + " doesn't accept further inputs if the first is a string")
        try {
          val procedure = workspace.compileForRun(s, context, true)
          val newActivation = new Activation(
            procedure, context.activation, context.ip)
          newActivation.setUpArgsForRunOrRunresult()
          val result = context.callReporterProcedure(newActivation)
          if (result == null)
            throw new EngineException(context, this, "failed to report a result")
          result
        } catch {
          case ex: CompilerException =>
            throw new EngineException(context, this, ex.getMessage.stripPrefix(CompilerException.RuntimeErrorAtCompileTimePrefix))
          case ex: EngineException =>
            throw new EngineException(context, ex.instruction, ex.getMessage)
          case ex: LogoException =>
            throw new EngineException(context, this, ex.getMessage)
        }
      case rep: AnonymousReporter =>
        val n = args.size - 1
        if (n < rep.syntax.minimum)
          throw new EngineException(context, this, AnonymousProcedure.missingInputs(rep, n))
        val actuals = new Array[AnyRef](n)
        var i = 0
        while(i < n) {
          actuals(i) = args(i + 1).report(context)
          i += 1
        }
        rep.report(context, actuals)
      case obj =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.ReporterType | Syntax.StringType, obj)
    }

}

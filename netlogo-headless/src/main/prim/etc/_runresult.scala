// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ LogoException, AnonymousReporter }
import org.nlogo.core.{ CompilerException, Syntax }
import org.nlogo.nvm.{ Activation, AnonymousProcedure, ArgumentTypeException, Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _runresult extends Reporter {

  override def report(context: Context): AnyRef =
    args(0).report(context) match {
      case s: String =>
        if(args.size > 1)
          throw new RuntimePrimitiveException(context, this,
            token.text + " doesn't accept further inputs if the first is a string")
        try {
          val procedure = workspace.compileForRun(
            argEvalString(context, 0), context, true)
          val newActivation = Activation.forRunOrRunresult(procedure, context.activation, context.ip)
          val result = context.callReporterProcedure(newActivation)
          if (result == null)
            throw new RuntimePrimitiveException(context, this, "failed to report a result")
          result
        } catch {
          case ex: CompilerException =>
            throw new RuntimePrimitiveException(
              context, this, ex.getMessage)
          case ex: RuntimePrimitiveException => throw ex
          case ex: LogoException =>
            throw new RuntimePrimitiveException(context, this, ex.getMessage)
        }
      case task: AnonymousReporter =>
        val n = args.size - 1
        if (n < task.syntax.minimum)
          throw new RuntimePrimitiveException(context, this, AnonymousProcedure.missingInputs(task, n))
        val actuals = new Array[AnyRef](n)
        var i = 0
        while(i < n) {
          actuals(i) = args(i + 1).report(context)
          i += 1
        }
        task.report(context, actuals)
      case obj =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.ReporterType | Syntax.StringType, obj)
    }

}

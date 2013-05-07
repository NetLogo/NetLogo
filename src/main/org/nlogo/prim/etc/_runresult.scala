// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ CompilerException, LogoException, Syntax }
import org.nlogo.nvm.{ Activation, ArgumentTypeException, Context, EngineException, Reporter, ReporterTask }

class _runresult extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.StringType | Syntax.ReporterTaskType,
        Syntax.RepeatableType | Syntax.WildcardType),
      Syntax.WildcardType,
      1)

  override def report(context: Context) =
    args(0).report(context) match {
      case s: String =>
        if(args.size > 1)
          throw new EngineException(context, this,
            token.text + " doesn't accept further inputs if the first is a string")
        try {
          val procedure = workspace.compileForRun(
            argEvalString(context, 0), context, true)
          val newActivation = new Activation(
            procedure, context.activation, context.ip)
          newActivation.setUpArgsForRunOrRunresult()
          val result = context.callReporterProcedure(newActivation)
          if (result == null)
            throw new EngineException(context, this, "failed to report a result")
          result
        } catch {
          case ex: CompilerException =>
            throw new EngineException(
              context, this, ex.getMessage)
          case ex: EngineException =>
            throw new EngineException(context, ex.instruction, ex.getMessage)
          case ex: LogoException =>
            throw new EngineException(context, this, ex.getMessage)
        }
      case task: ReporterTask =>
        val n = args.size - 1
        if(n < task.formals.size)
          throw new EngineException(
            context, this, task.missingInputs(n))
        val actuals = new Array[AnyRef](n)
        var i = 0
        while(i < n) {
          actuals(i) = args(i + 1).report(context)
          i += 1
        }
        task.report(context, actuals)
      case obj =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.ReporterTaskType | Syntax.StringType, obj)
    }

}

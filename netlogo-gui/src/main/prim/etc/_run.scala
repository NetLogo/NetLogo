// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ CompilerException, I18N, Syntax }
import org.nlogo.api.AnonymousCommand
import org.nlogo.nvm.{ Activation, AnonymousProcedure, ArgumentTypeException,
  Command, Context, RuntimePrimitiveException, NonLocalExit, Procedure }

class _run extends Command {
  override def perform(context: Context) {
    args(0).report(context) match {
      case s: String =>
        if(args.size > 1)
          throw new RuntimePrimitiveException(context, this,
            token.text + " doesn't accept further inputs if the first is a string")
        try {
          val procedure = workspace.compileForRun(s, context, false)
          // the procedure returned by compileForRun is executed without switching Contexts, only
          // activations.  so we create a new activation...
          context.activation = Activation.forRunOrRunresult(procedure, context.activation, next)
          // put the instruction pointer at the beginning of the new procedure.  note that when we made
          // the new Activation above, we passed the proper return address to the constructor so the
          // flow execution will resume in the right place.
          context.ip = 0
        } catch {
          case error: CompilerException =>
            throw new RuntimePrimitiveException(context, this, error.getMessage)
        }
      case cmd: AnonymousCommand =>
        val n = args.size - 1
        if (n < cmd.syntax.minimum)
          throw new RuntimePrimitiveException(context, this, AnonymousProcedure.missingInputs(cmd, n))
        val actuals = new Array[AnyRef](n)
        var i = 0
        while(i < n) {
          actuals(i) = args(i + 1).report(context)
          i += 1
        }
        try {
          cmd.perform(context, actuals)
          context.ip = next
        } catch {
          case _: NonLocalExit if ! context.activation.procedure.isReporter =>
            context.stop()
        }
      case obj =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.CommandType | Syntax.StringType, obj)
    }
  }

}

package org.nlogo.prim

import org.nlogo.api.{ Let, Syntax }
import org.nlogo.nvm.{ CommandLambda, Context, Procedure, Reporter }

class _lambda(proc: Procedure) extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Syntax.CommandTaskType)

  override def toString =
    super.toString + ":" + proc.displayName

  override def report(c: Context): AnyRef =
    CommandLambda(procedure = proc,
                  formals = proc.lambdaFormals.reverse.dropWhile(_ == null).reverse.toArray,
                  lets = c.letBindings,
                  locals = c.activation.args)

}

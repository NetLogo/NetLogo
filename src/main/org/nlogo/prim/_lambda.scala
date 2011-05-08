package org.nlogo.prim

import org.nlogo.api.Let
import org.nlogo.nvm._

class _lambda(proc: Procedure) extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_COMMAND_LAMBDA)

  override def toString =
    super.toString + ":" + proc.displayName

  override def report(c: Context): AnyRef =
    CommandLambda(procedure = proc,
                  formals = proc.lambdaFormals.reverse.dropWhile(_ == null).reverse.toArray,
                  lets = c.letBindings,
                  locals = c.activation.args)

}

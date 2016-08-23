// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _lambdavariable(val varName: String) extends Reporter {

  override def toString =
    super.toString + ":" + varName

  override def report(context: Context) = {
    val let = context.allLets.find(_.let.name == varName).get
    let.value // LambdaVariableVisitor compiles us out of existence
  }
}

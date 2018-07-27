package org.nlogo.prim.etc

import org.nlogo.agent.AgentSet
import org.nlogo.core.{I18N, LogoList, Syntax}
import org.nlogo.nvm.{ArgumentTypeException, Context, Reporter, RuntimePrimitiveException}

class _uptonof extends Reporter {
  override def report(ctx: Context): AnyRef = {
    val n = argEvalIntValue(ctx, 0)
    if (n < 0)
      throw new RuntimePrimitiveException(ctx, this,
        I18N.errors.getN("org.nlogo.prim.etc.$common.firstInputCantBeNegative",
          displayName))
    args(1).report(ctx) match {
      case l: LogoList =>
        if (n >= l.size) l
        else l.randomSubset(n, ctx.getRNG)
      case s: AgentSet =>
        val count = s.count
        if (n >= count) s
        else s.randomSubset(n, count, ctx.getRNG)
      case x => new ArgumentTypeException(ctx, this, 1,
        Syntax.ListType | Syntax.AgentsetType, x)
    }
  }
}

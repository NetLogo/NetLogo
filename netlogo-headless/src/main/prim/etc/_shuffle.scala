// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import java.util.{ ArrayList, Collections }

import org.nlogo.core.LogoList
import org.nlogo.nvm.{ Context, Reporter }

class _shuffle extends Reporter {
  def report(context: Context): AnyRef = {
    val result = new ArrayList[AnyRef](argEvalList(context, 0).toJava)
    Collections.shuffle(result, context.job.random)
    LogoList.fromJava(result)
  }

  def report_1(context: Context, l0: LogoList): LogoList = {
    val result = new ArrayList[AnyRef](l0.toJava)
    Collections.shuffle(result, context.job.random)
    LogoList.fromJava(result)
  }
}

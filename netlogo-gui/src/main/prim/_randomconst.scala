// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Context, Reporter }

class _randomconst(private val _n: Long) extends Reporter {
  def n = _n

  override def toString: String = super.toString + ":" + n

  override def report(context: Context): java.lang.Double =
    report_1(context)

  def report_1(context: Context): Double =
    context.job.random.nextLong(_n).toDouble
}

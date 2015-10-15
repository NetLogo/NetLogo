// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Context, Reporter }

class _random extends Reporter {

  override def report(context: Context): java.lang.Double =
    Double.box(report_1(context, argEvalDoubleValue(context, 0)))

  def report_1(context: Context, maxDouble: Double): Double = {
    var maxLong = validLong(maxDouble)
    if (maxDouble != maxLong)
      maxLong += (if (maxDouble >= 0) 1 else -1)
    if (maxLong > 0)
      context.job.random.nextLong(maxLong).toDouble
    else if (maxLong < 0)
      (-context.job.random.nextLong(-maxLong)).toDouble
    else 0
  }

}

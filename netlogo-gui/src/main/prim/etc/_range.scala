package org.nlogo.prim.etc

import org.nlogo.core.LogoList
import org.nlogo.nvm.{Context, Pure, Reporter, RuntimePrimitiveException}
import org.nlogo.util.PragmaticRange

/**
  * Created by bryan on 11/2/16.
  */
class _range extends Reporter {

  override def report(context: Context): LogoList = {
    val (start, stop, step) = args.length match {
      case 1 => (0.0, argEvalDoubleValue(context, 0), 1.0)
      case 2 => (argEvalDoubleValue(context, 0), argEvalDoubleValue(context, 1), 1.0)
      case 3 => (argEvalDoubleValue(context, 0), argEvalDoubleValue(context, 1), argEvalDoubleValue(context, 2))
      case _ => throw new RuntimePrimitiveException(context, this, "range expects at most three arguments")
    }
    if (step == 0) throw new RuntimePrimitiveException(context, this, "The step-size for range must be non-zero")
    LogoList.fromVector(PragmaticRange(start, stop, step).map(Double.box))
  }
}

package org.nlogo.prim.etc

import org.nlogo.api.{DoubleRange}
import org.nlogo.core.{I18N, LogoList}
import org.nlogo.nvm.{Context, Reporter, RuntimePrimitiveException}

class _range extends Reporter {

  override def report(context: Context): LogoList = {
    val (start, stop, step) = args.length match {
      case 1 => (0.0, argEvalDoubleValue(context, 0), 1.0)
      case 2 => (argEvalDoubleValue(context, 0), argEvalDoubleValue(context, 1), 1.0)
      case 3 => (argEvalDoubleValue(context, 0), argEvalDoubleValue(context, 1), argEvalDoubleValue(context, 2))
      case _ => throw new RuntimePrimitiveException(context, this, "range expects at most three arguments")
    }
    if (step == 0)
      throw new RuntimePrimitiveException(context, this, I18N.errors.get("org.nlogo.prim.etc._range.zeroStep"))
    LogoList.fromVector(DoubleRange(start, stop, step).map(Double.box).toVector)
  }
}

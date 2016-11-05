package org.nlogo.prim.etc

import org.nlogo.api.LogoRange
import org.nlogo.core.{I18N, LogoList}
import org.nlogo.nvm.{Context, Reporter, RuntimePrimitiveException}

class _until extends Reporter {
  override def report(context: Context): LogoList = {
    val start = argEvalDoubleValue(context, 0)
    val stop = argEvalDoubleValue(context, 1)
    val step = if (stop >= start) 1 else -1
    new LogoRange(start, stop, step)
  }
}

class _through extends Reporter {
  override def report(context: Context): LogoList = {
    val start = argEvalDoubleValue(context, 0)
    val stop = argEvalDoubleValue(context, 1)
    val step = if (stop >= start) 1 else -1
    new LogoRange(start, stop, step, inclusive = true)
  }
}

class _by extends Reporter {
  override def report(context: Context): LogoList = {
    val lr: LogoRange = argEvalList(context, 0) match {
      case l: LogoRange => l
      case _ => throw new RuntimePrimitiveException(
        context, this, I18N.errors.get("org.nlogo.prim.etc._by.notRange"))
    }
    val step = argEvalDoubleValue(context, 1)
    if (step == 0) throw new RuntimePrimitiveException(
      context, this, I18N.errors.get("org.nlogo.prim.etc._by.zeroStep"))
    new LogoRange(lr.start, lr.stop, step, lr.inclusive)
  }
}

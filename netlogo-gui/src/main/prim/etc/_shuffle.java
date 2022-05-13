// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

import java.util.ArrayList;
import java.util.Collections;

public final class _shuffle
    extends Reporter {
  @Override
  public Object report(final Context context) throws LogoException {
    // we can't call Collections.shuffle() on a LogoList because
    // LogoList.Iterator doesn't support set() - ST 8/1/06
    ArrayList<Object> result =
        new ArrayList<Object>(argEvalList(context, 0).toJava());
    Collections.shuffle(result, context.job.random);
    return LogoList.fromJava(result);
  }



  public LogoList report_1(Context context, LogoList l0) {
    ArrayList<Object> result =
        new ArrayList<Object>(l0.toJava());
    Collections.shuffle(result, context.job.random);
    return LogoList.fromJava(result);
  }
}

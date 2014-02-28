// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

import java.util.ArrayList;
import java.util.Collections;

public final strictfp class _shuffle
    extends Reporter {
  @Override
  public Object report(final Context context) {
    // we can't call Collections.shuffle() on a LogoList because
    // LogoList.Iterator doesn't support set() - ST 8/1/06
    ArrayList<Object> result =
        new ArrayList<Object>(argEvalList(context, 0));
    Collections.shuffle(result, context.job.random);
    return LogoList.fromJava(result);
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(new int[]{Syntax.ListType()},
        Syntax.ListType());
  }

  public LogoList report_1(Context context, LogoList l0) {
    ArrayList<Object> result =
        new ArrayList<Object>(l0);
    Collections.shuffle(result, context.job.random);
    return LogoList.fromJava(result);
  }
}

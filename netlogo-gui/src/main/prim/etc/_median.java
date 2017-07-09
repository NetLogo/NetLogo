// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final strictfp class _median
    extends Reporter
    implements org.nlogo.core.Pure {
  @Override
  public Object report(final Context context) throws LogoException {
    LogoList list = argEvalList(context, 0);
    int badElts = 0;
    List<Double> nums =
        new ArrayList<Double>(list.size());
    for (Object elt : list.javaIterable()) {
      if (!(elt instanceof Double)) {
        ++badElts;
        continue;
      }
      nums.add((Double) elt);
    }
    int listSize = list.size();
    if (listSize == badElts) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc.median.cantFindMedianOfListWithNoNumbers", Dump.logoObject(list)));
    }
    Collections.sort(nums);
    int medianPos = (listSize - badElts) / 2;
    if ((listSize - badElts) % 2 == 1) {
      return nums.get(medianPos);
    }
    Double middle1 = nums.get(medianPos - 1);
    Double middle2 = nums.get(medianPos);
    return newValidDouble(middle1.doubleValue() / 2 + middle2.doubleValue() / 2, context);
  }


}

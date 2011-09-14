package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.agent.Turtle;
import org.nlogo.agent.Agent;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Link;
import org.nlogo.api.Nobody$;
import org.nlogo.api.Equality;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Pure;

public final strictfp class _notequal
    extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    int left = Syntax.WildcardType();
    int[] right = {Syntax.WildcardType()};
    int ret = Syntax.BooleanType();
    return Syntax.reporterSyntax(left, right, ret,
        org.nlogo.api.Syntax.NormalPrecedence() - 5);
  }

  @Override
  public Object report(Context context) throws LogoException {
    Object o1 = args[0].report(context);
    Object o2 = args[1].report(context);
    return report_1(context, o1, o2)
        ? Boolean.TRUE
        : Boolean.FALSE;
  }

  public boolean report_1(Context context, Object arg0, Object arg1) {
    return !Equality.equals(arg0, arg1);
  }

  public boolean report_2(Context context, double d0, double d1) {
    return d0 != d1;
  }

  public boolean report_3(Context context, double d0, Object arg1) {
    return !(arg1 instanceof Double) || d0 != ((Double) arg1).doubleValue();
  }

  public boolean report_4(Context context, Object arg0, double d1) {
    return !(arg0 instanceof Double) || d1 != ((Double) arg0).doubleValue();
  }

  public boolean report_5(Context context, boolean arg0, boolean arg1) {
    return arg0 != arg1;
  }

  public boolean report_6(Context context, boolean arg0, Object arg1) {
    return !(arg1 instanceof Boolean) ||
        (arg0 != ((Boolean) arg1).booleanValue());
  }

  public boolean report_7(Context context, Object arg0, boolean arg1) {
    return !(arg0 instanceof Boolean) ||
        (arg1 != ((Boolean) arg0).booleanValue());
  }

  public boolean report_8(Context context, String arg0, String arg1) {
    return !arg0.equals(arg1);
  }

  public boolean report_9(Context context, Object arg0, String arg1) {
    return !arg1.equals(arg0);
  }

  public boolean report_10(Context context, String arg0, Object arg1) {
    return !arg0.equals(arg1);
  }

  public boolean report_11(Context context, Turtle arg0, Turtle arg1) {
    return arg0.id != arg1.id;
  }

  public boolean report_12(Context context, Patch arg0, Patch arg1) {
    return arg0.id != arg1.id;
  }

  public boolean report_13(Context context, Link arg0, Link arg1) {
    return arg0.id != arg1.id;
  }

  public boolean report_14(Context context, Nobody$ arg0, Agent arg1) {
    return arg1.id != -1;
  }

  public boolean report_15(Context context, Agent arg0, Nobody$ arg1) {
    return arg0.id != -1;
  }

  public boolean report_16(Context context, Object arg0, Nobody$ arg1) {
    return !(arg0 == Nobody$.MODULE$) &&
        (!(arg0 instanceof Agent) || ((Agent) arg0).id != -1);
  }

  public boolean report_17(Context context, Nobody$ arg0, Object arg1) {
    return !(arg1 == Nobody$.MODULE$) &&
        (!(arg1 instanceof Agent) || ((Agent) arg1).id != -1);
  }
}

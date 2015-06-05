// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import java.util.List;
import org.nlogo.api.Syntax;
import org.nlogo.api.Token;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _constcodeblock extends Reporter implements Pure {
  final List<Token> value;

  public _constcodeblock(List<Token> value) {
    this.value = value;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.CodeBlockType());
  }

  @Override
  public String toString() {
    return super.toString() + ":\"" + value + "\"";
  }

  @Override
  public Object report(Context context) {
    return value;
  }

  /*
  public List<Token> report_1(Context context) {
    return value;
  }*/
}

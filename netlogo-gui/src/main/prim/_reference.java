// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.core.Reference;
import org.nlogo.nvm.Reporter;

public final class _reference
    extends Reporter {
  public final Reference reference;

  public _reference(Reference reference) {
    this.reference = reference;
  }

  @Override
  public String toString() {
    return super.toString() + ":" + reference.kind().toString() + "," + reference.vn();
  }

  @Override
  public Object report(final Context context) {
    // we're always supposed to get compiled out of existence
    throw new UnsupportedOperationException();
  }
}

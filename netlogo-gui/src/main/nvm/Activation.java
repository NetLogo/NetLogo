// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

public final strictfp class Activation {

  public final Procedure procedure;
  public final Activation parent;
  final int returnAddress;
  // not final so ReporterTask can swap in the definition-site args - ST 2/5/11
  public Object[] args;

  private static final Object[] NO_ARGS = new Object[]{};

  public Activation(Procedure procedure, Activation parent,
                    int returnAddress) {
    this.procedure = procedure;
    this.parent = parent;
    this.returnAddress = returnAddress;
    int size = procedure.size();
    args = (size > 0) ? new Object[size] : NO_ARGS;
  }

  public void setUpArgsForRunOrRunresult() {
    // if there's a reason we make a copy rather than just using the
    // original, I no longer remember it - ST 2/6/11
    System.arraycopy(parent.args, 0, args, 0,
        parent.procedure.args().size());
  }

  @Override
  public String toString() {
    String result = super.toString();
    result += ":" + procedure.name() + "(" + args.length + " args";
    result += ", return address = " + returnAddress + ")\n";
    for (int i = 0; i < args.length; i++) {
      result += "  arg " + i + " = " + args[i] + "\n";
    }
    return result;
  }

}

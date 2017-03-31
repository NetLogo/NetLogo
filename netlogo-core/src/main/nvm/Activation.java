// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

public final strictfp class Activation implements org.nlogo.api.Activation {
  private static final Object[] NO_ARGS = new Object[]{};

  public final Procedure procedure;
  public final Activation parent;
  final int returnAddress;
  final public Object[] args;
  final Command[] code; // this is cached from procedure for speed
  public Binding binding;
  public Object result = null; // used to store the return value of this procedure call

  public Activation(Procedure procedure, Activation parent, Object[] args, int returnAddress, Binding binding) {
    this.procedure = procedure;
    this.parent = parent;
    this.args = args;
    this.returnAddress = returnAddress;
    this.code = procedure._code;
    this.binding = binding;
  }

  public Activation(Procedure procedure, Activation parent, Object[] args, int returnAddress) {
    this.procedure = procedure;
    this.code = procedure._code;
    this.parent = parent;
    this.args = args;
    this.returnAddress = returnAddress;
    this.binding = new Binding();
  }

  public Activation(Procedure procedure, Activation parent, int returnAddress) {
    this.procedure = procedure;
    this.code = procedure._code;
    this.parent = parent;
    this.returnAddress = returnAddress;
    this.binding = new Binding();
    if (procedure.size() == 0)
      this.args = NO_ARGS;
    else
      this.args = new Object[procedure.size()];
  }

  public scala.Option<Activation> parent() {
    return scala.Option.apply(parent);
  }

  public org.nlogo.core.FrontEndProcedure procedure() {
    return procedure;
  }

  Activation parentOrNull() {
    return parent;
  }

  public static Activation forRunOrRunresult(Procedure procedure, Activation parent, int returnAddress) {
    // When the procedure is compiled from a string, it will have 0 arguments, which
    // means references to the parent procedures arguments will error.
    // We have to copy so we don't mess with the parent activation's locals. - RG 1/13/17
    Object[] args = new Object[Math.max(procedure.size(), parent.args.length)];
    System.arraycopy(parent.args, 0, args, 0, parent.procedure.args().size());
    return new Activation(procedure, parent, args, returnAddress, parent.binding);
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

  public Activation nonLambdaActivation() {
    if (procedure.isLambda() && parent != null)
      return parent.nonLambdaActivation();
    else
      return this;
  }
}

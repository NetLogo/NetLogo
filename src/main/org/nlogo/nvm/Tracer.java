package org.nlogo.nvm;

public abstract strictfp class Tracer {

  // Tracer control interface
  public abstract void enable();

  public abstract void disable();

  public abstract void reset();

  public abstract void dump(java.io.PrintStream stream);

  // Tracer call recording interface
  public abstract void openCallRecord(Context context, Activation activation);

  public abstract void closeCallRecord(Context context, Activation activation);

  public abstract long calls(String name);

  public abstract long exclusiveTime(String name);

  public abstract long inclusiveTime(String name);
}

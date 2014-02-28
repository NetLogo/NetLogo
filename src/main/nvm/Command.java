// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

public abstract strictfp class Command
    extends Instruction {

  public boolean callsOtherCode() {
    return false;
  }

  public abstract void perform(Context context);

  // the assembler will change this to the ip value for the next instruction
  public int next = -1;
  // during compilation, this is a relative offset.  during assembly, it is changed
  // to an absolute ip value
  public int offset = 0;
  // for speed, cache this value. we can't be calling syntax()
  // in a tight loop like Context.step() - ST 1/27/09
  boolean switches = false;

  @Override
  public void init(Workspace workspace) {
    super.init(workspace);
    switches = syntax().switches();
  }
}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.api.LogoException;

public strictfp class EngineException
    extends org.nlogo.api.LogoException {

  protected boolean hasBeenResolved = false;

  public Context context = null;
  public Instruction instruction = null;

  // force caller to provide a context, in theory it got
  // set later but it was sometimes the wrong context, thus, error
  // messages were not correct. ev 8/3/05
  public EngineException(Context context, Instruction instruction,
                         String message) {
    super(message, null);
    this.context = context;
    this.instruction = instruction;
  }

  // With the new bytecode generation compiler stuff, we need
  // be able to create EngineExceptions without an instruction.
  // Later, when we catch the exception, we can use line number
  // information to figure out what token the error happened on.
  public EngineException(Context context, String message) {
    super(message, null);
    if (!org.nlogo.api.Version.useGenerator()) {
      throw new IllegalStateException();
    }
    this.context = context;
    instruction = null;
  }

  // previously this method was flattening all LogoExceptions into EngineExceptions
  // however, we definitely don't want to flatten halt exceptions since we handle them
  // differently later. ev 8/5/05
  static void rethrow(LogoException ex, Context context, Instruction instruction)
      throws LogoException {
    if (ex instanceof EngineException) {
      EngineException ee = (EngineException) ex;
      if (ee.context == null) {
        ee.context = context;
      }
      if (ee.instruction == null) {
        ee.instruction = instruction;
      }
      if (!ee.hasBeenResolved) {
        ee.resolveErrorInstruction();
      }

      throw ee;
    } else if (ex instanceof HaltException) {
      throw ex;
    } else {
      EngineException newEx = new EngineException
          (context, instruction, ex.getMessage());
      newEx.resolveErrorInstruction();
      throw newEx;
    }
  }

  // GeneratedInstructions have multiple Instructions stored inside them
  // and we need to resolve which instruction was actually executing
  // when the error occurred.  ~Forrest (10/24/2006)
  protected void resolveErrorInstruction() {
    if (hasBeenResolved) {
      throw new IllegalStateException("An EngineException must only be 'resolved' once!");
    }
    hasBeenResolved = true;
    instruction = instruction.extractErrorInstruction(this);
  }

  scala.Option<String> cachedRuntimeErrorMessage = scala.Option.apply(null);

  @Override
  public Throwable fillInStackTrace() {
    super.fillInStackTrace();
    if(context != null && !cachedRuntimeErrorMessage.isDefined()) {
      cachedRuntimeErrorMessage = scala.Option.apply(
        context.buildRuntimeErrorMessage(
          instruction, this));
    }
    return this;
  }

}

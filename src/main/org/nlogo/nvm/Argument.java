package org.nlogo.nvm;

import org.nlogo.agent.AgentSet;
import org.nlogo.api.Dump;

/**
 * Passes arguments to extension primitives.
 */
public strictfp class Argument
    implements org.nlogo.api.Argument {
  private final Context context;
  protected final Reporter arg;
  private Object cached = null;

  public Argument(Context context, Reporter arg) {
    this.context = context;
    this.arg = arg;
  }

  public Object get()
      throws org.nlogo.api.LogoException {
    if (cached == null) {
      cached = arg.report(context);
    }
    if (cached instanceof org.nlogo.agent.Agent &&
        ((org.nlogo.agent.Agent) cached).id == -1) {
      cached = org.nlogo.api.Nobody$.MODULE$;
    }
    return cached;
  }

  public AgentSet getAgentSet()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (AgentSet) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException
          (getExceptionMessage
              (Syntax.TYPE_AGENTSET, obj));
    }
  }


  public org.nlogo.api.Agent getAgent()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.Agent) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(getExceptionMessage
          (org.nlogo.nvm.Syntax.TYPE_AGENT, obj));
    }
  }

  public Boolean getBoolean()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (Boolean) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(getExceptionMessage
          (org.nlogo.nvm.Syntax.TYPE_BOOLEAN, obj));
    }
  }

  public boolean getBooleanValue()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return ((Boolean) obj).booleanValue();
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(getExceptionMessage
          (org.nlogo.nvm.Syntax.TYPE_BOOLEAN, obj));
    }
  }

  public double getDoubleValue()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return ((Double) obj).doubleValue();
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(getExceptionMessage
          (org.nlogo.nvm.Syntax.TYPE_NUMBER, obj));
    }
  }

  public int getIntValue()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return ((Double) obj).intValue();
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(getExceptionMessage
          (org.nlogo.nvm.Syntax.TYPE_NUMBER, obj));
    }
  }

  public org.nlogo.api.LogoList getList()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.LogoList) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(getExceptionMessage
          (org.nlogo.nvm.Syntax.TYPE_LIST, obj));
    }
  }

  public org.nlogo.api.Patch getPatch()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.Patch) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(getExceptionMessage
          (org.nlogo.nvm.Syntax.TYPE_PATCH, obj));
    }
  }

  public String getString()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (String) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(getExceptionMessage
          (org.nlogo.nvm.Syntax.TYPE_STRING, obj));
    }
  }


  public org.nlogo.api.Turtle getTurtle()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.Turtle) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(getExceptionMessage
          (org.nlogo.nvm.Syntax.TYPE_TURTLE, obj));
    }
  }

  public org.nlogo.api.Link getLink()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.Link) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(getExceptionMessage
          (org.nlogo.nvm.Syntax.TYPE_LINK, obj));
    }
  }

  /**
   * <i>Special (undocumented) method for the cities extension. </i>
   * Returns the argument reporter without evaluating.
   */
  public Reporter getReporter() {
    return arg;
  }


  private String getExceptionMessage(int wantedType, Object badValue) {
    String result = "Expected this input to be "
        + org.nlogo.nvm.Syntax.aTypeName(wantedType) + " but got "
        + (badValue == org.nlogo.api.Nobody$.MODULE$
        ? "NOBODY"
        : "the " + org.nlogo.nvm.Syntax.typeName(badValue)
        + " " + Dump.logoObject(badValue))
        + " instead.";

    return result;

  }

}

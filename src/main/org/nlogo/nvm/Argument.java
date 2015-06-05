// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.agent.AgentSet;
import org.nlogo.api.Dump;
import org.nlogo.api.Syntax;
import org.nlogo.api.TypeNames;
import java.util.List;

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
           (Syntax.AgentsetType(), obj));
    }
  }


  public org.nlogo.api.Agent getAgent()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.Agent) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.AgentType(), obj));
    }
  }

  public Boolean getBoolean()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (Boolean) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.BooleanType(), obj));
    }
  }

  public boolean getBooleanValue()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return ((Boolean) obj).booleanValue();
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.BooleanType(), obj));
    }
  }

  public double getDoubleValue()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return ((Double) obj).doubleValue();
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.NumberType(), obj));
    }
  }

  public int getIntValue()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return ((Double) obj).intValue();
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.NumberType(), obj));
    }
  }

  public org.nlogo.api.LogoList getList()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.LogoList) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.ListType(), obj));
    }
  }

  public org.nlogo.api.Patch getPatch()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.Patch) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.PatchType(), obj));
    }
  }

  public String getString()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (String) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.StringType(), obj));
    }
  }


  public org.nlogo.api.Turtle getTurtle()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.Turtle) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.TurtleType(), obj));
    }
  }

  public org.nlogo.api.Link getLink()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.Link) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.LinkType(), obj));
    }
  }

  public org.nlogo.api.ReporterTask getReporterTask()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.ReporterTask) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.ReporterTaskType(), obj));
    }
  }

  public org.nlogo.api.CommandTask getCommandTask()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.CommandTask) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.CommandTaskType(), obj));
    }
  }

  @SuppressWarnings("unchecked") public List<org.nlogo.api.Token> getCode() 
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (List<org.nlogo.api.Token>) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.CodeBlockType(), obj));
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
        + TypeNames.aName(wantedType) + " but got "
        + (badValue == org.nlogo.api.Nobody$.MODULE$
        ? "NOBODY"
        : "the " + TypeNames.name(badValue)
        + " " + Dump.logoObject(badValue))
        + " instead.";

    return result;

  }

}

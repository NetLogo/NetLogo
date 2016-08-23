// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.agent.AgentSet;
import org.nlogo.api.Dump;
import org.nlogo.api.TypeNames;
import org.nlogo.core.Syntax;
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
      cached = org.nlogo.core.Nobody$.MODULE$;
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

  public org.nlogo.core.LogoList getList()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.core.LogoList) obj;
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

  public org.nlogo.api.AnonymousReporter getReporter()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.AnonymousReporter) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.ReporterType(), obj));
    }
  }

  public org.nlogo.api.AnonymousCommand getCommand()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.api.AnonymousCommand) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.CommandType(), obj));
    }
  }

  @SuppressWarnings("unchecked") public List<org.nlogo.core.Token> getCode()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (List<org.nlogo.core.Token>) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.CodeBlockType(), obj));
    }
  }

  public org.nlogo.core.Token getSymbol()
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    Object obj = get();
    try {
      return (org.nlogo.core.Token) obj;
    } catch (ClassCastException ex) {
      throw new org.nlogo.api.ExtensionException(
        getExceptionMessage(Syntax.SymbolType(), obj));
    }
  }

  // if you're looking for the cities extension's <code>getReporter</code>
  // method, that has been removed. Change the extension and use
  // <code>getSymbol</code> instead.

  private String getExceptionMessage(int wantedType, Object badValue) {
    String result = "Expected this input to be "
        + TypeNames.aName(wantedType) + " but got "
        + (badValue == org.nlogo.core.Nobody$.MODULE$
        ? "NOBODY"
        : "the " + TypeNames.name(badValue)
        + " " + Dump.logoObject(badValue))
        + " instead.";

    return result;

  }

}

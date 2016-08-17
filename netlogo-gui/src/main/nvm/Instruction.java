// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.core.Reference;
import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Observer;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.AgentKind;
import org.nlogo.core.AgentKindJ;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.Token;
import org.nlogo.nvm.Thunk;

import java.util.ArrayList;
import java.util.List;

public abstract strictfp class Instruction
    extends AbstractScalaInstruction {

  public Workspace workspace;
  public org.nlogo.agent.World world;  // public so the engine can get to World.comeUpForAir easily

  // for extra-efficient agent class checking
  public String agentClassString = "OTPL";
  public int agentBits = 0;
  public Reporter[] args = new Reporter[0];

  // When we convert this to scala, this can use accessor methods

  // for some instructions two tokens are relevant for example
  // _setturtlevariable, the SET is what we want to report
  // for runtime errors since it's expecting a command
  // however, the type is actually limited by the variable
  // name not the set and we want to compiler to report that ev 7/13/07
  private org.nlogo.core.Token token2 = null;

  public org.nlogo.core.Token tokenLimitingType() {
    return token2 == null ? token() : token2;
  }

  public void tokenLimitingType(org.nlogo.core.Token token) {
    this.token2 = token;
  }

  // the bytecode generator uses these to store text for dump() to print
  public java.lang.reflect.Method chosenMethod = null;
  public String source;     // contains the source of this instruction only
  public String fullSource; // contains the source of this instruction and all arguments
  public Thunk<String> disassembly =
      new Thunk<String>() {
        @Override
        public String compute() {
          return "";
        }
      };

  // for primitives which use ReferenceType
  public Reference reference = null;

  /// store frequently used stuff where it's fast to get at

  public void init(Workspace workspace) {
    if (workspace != null) {
      this.workspace = workspace;
      world = workspace.world();
    }
    for (int i = 0; i < args.length; i++) {
      args[i].init(workspace);
    }
    agentBits =
        ((agentClassString.indexOf('O') != -1) ? Observer.BIT : 0) |
            ((agentClassString.indexOf('T') != -1) ? Turtle.BIT : 0) |
            ((agentClassString.indexOf('P') != -1) ? Patch.BIT : 0) |
            ((agentClassString.indexOf('L') != -1) ? Link.BIT : 0);
  }

  /// display methods

  // note: don't call this if token() is null - ST 2/12/04
  public int[] getPositionAndLength() {
    int begin = getSourceStartPosition();
    int end = getSourceEndPosition();
    return new int[]{begin, end - begin};
  }

  // Note:  We are not caching these for efficiency
  // (that would make no sense!).  Instead, the reason we are
  // caching these start/end positions, is because the Inliner
  // phase of the compiler may copy instruction to new locations,
  // but we want all the Instructions to precompute their correct
  // start and end positions before the Inliner goes to work.
  // ~Forrest (10/12/2006)
  public int storedSourceStartPosition = -1;
  public int storedSourceEndPosition = -1;
  public String storedFilename = null;

  public int getSourceStartPosition() {
    if (storedSourceStartPosition > -1) {
      return storedSourceStartPosition;
    }
    if (token() == null) {
      return -1;
    }
    int begin = token().start();
    for (int i = 0; i < args.length; i++) {
      if (args[i].token() != null) {
        int argBegin = args[i].getSourceStartPosition();
        begin = StrictMath.min(begin, argBegin);
      }
    }
    storedSourceStartPosition = begin;
    return begin;
  }

  public int getSourceEndPosition() {
    if (storedSourceEndPosition > -1) {
      return storedSourceEndPosition;
    }
    if (token() == null) {
      return -1;
    }
    int end = token().end();
    for (int i = 0; i < args.length; i++) {
      if (args[i].token() != null) {
        int argEnd = args[i].getSourceEndPosition();
        end = StrictMath.max(end, argEnd);
      }
    }
    storedSourceEndPosition = end;
    return end;
  }

  public String getFilename() {
    if (token() != null) {
      return token().filename();
    } else {
      return storedFilename;
    }
  }

  // We want this information for creating some error messages
  // (particularly ArgumentTypeExceptions) ~Forrest (11/10/2006)
  private String storedSourceSnippet = "";

  public void setSourceSnippet(String s) {
    storedSourceSnippet = s;
  }

  public String getSourceSnippet() {
    return storedSourceSnippet;
  }

  /*
    * This method is handled specially by MethodRipper.
    * Thus, when called by rejiggered report_X() methods, it gives the right result,
    * instead returning the displayName() of the GeneratedInstruction. ~Forrest (summer 2006)
    */
  public String displayName() {
    if (token() != null) {
      return token().text().toUpperCase();
    } else {
      // well, returning some weird ugly internal class name
      // is better than nothing, I guess
      String result = "." + getClass().getName();
      return result.substring(result.lastIndexOf('.') + 1);
    }
  }

  @Override
  public String toString() {
    String result = getClass().getName();
    int dotPos = result.lastIndexOf('.');
    return dotPos == -1
        ? result
        : result.substring(dotPos + 1);
  }

  public String dump() {
    return dump(3);
  }

  public String dump(int indentLevel) {
    StringBuilder buf = new StringBuilder(toString());
    if (source != null) {
      buf.append(" \"");
      buf.append(fullSource);
      buf.append('"');
    }
    if (chosenMethod != null) {
      buf.append(' ');
      buf.append(describeMethod(chosenMethod));
    }
    if (args.length > 0) {
      buf.append('\n');
      for (int i = 0; i < args.length; i++) {
        for (int j = 0; j < indentLevel * 2; j++) {
          buf.append(' ');
        }
        buf.append(args[i].dump(indentLevel + 1));
        if (i < args.length - 1) {
          buf.append('\n');
        }
      }
    }
    return buf.toString();
  }

  private String describeMethod(java.lang.reflect.Method m) {
    Class<?>[] types = m.getParameterTypes();
    StringBuilder buf = new StringBuilder();
    for (int i = 1; i < types.length; i++) {
      if (i > 1) {
        buf.append(',');
      }
      buf.append(shortClassName(types[i].getName()));
    }
    if (types.length > 1) {
      buf.append(' ');
    }
    buf.append("=> ");
    buf.append(shortClassName(m.getReturnType().getName()));
    return buf.toString();
  }

  private String shortClassName(String s) {
    String[] parts = s.split("\\.");
    return parts[parts.length - 1];
  }

  // checking of breed directedness

  protected void mustNotBeDirected(AgentSet breed, Context context)
      throws EngineException {
    if (breed.isDirected()) {
      throw new EngineException
          (context, this,
              breed.printName() + " is a directed breed.");
    }
  }

  protected void mustNotBeUndirected(AgentSet breed, Context context)
      throws EngineException {
    if (breed.isUndirected()) {
      throw new EngineException
          (context, this,
              breed.printName() + " is an undirected breed.");
    }
  }

  protected void checkForBreedCompatibility(AgentSet breed, Context context)
      throws EngineException {
    if (!world.linkManager.checkBreededCompatibility(breed == world.links())) {
      throw new EngineException
          (context, this, I18N.errorsJ().get("org.nlogo.agent.Link.cantHaveBreededAndUnbreededLinks"));
    }
  }

  // checking of numeric types

  public long validLong(double d) throws LogoException {
    // 9007199254740992 is the largest/smallest integer
    // exactly representable in a double - ST 1/29/08
    if (d > 9007199254740992L || d < -9007199254740992L) {
      throw new EngineException
          (null, this,
              d + " is too large to be represented exactly as an integer in NetLogo");
    }
    return (long) d;
  }

  public static boolean isValidLong(double d) {
    // 9007199254740992 is the largest/smallest integer
    // exactly representable in a double - ST 1/29/08
    return d <= 9007199254740992L && d >= -9007199254740992L;
  }

  public Double newValidDouble(double d) throws LogoException {
    if (Double.isInfinite(d) || Double.isNaN(d)) {
      invalidDouble(d);
    }
    return Double.valueOf(d);
  }

  public double validDouble(double d) throws LogoException {
    // yeah, this line is repeated from the previous method,
    // but factoring it out would cost us a method call, and this
    // is extremely efficiency-critical code, so... - ST 11/1/04
    if (Double.isInfinite(d) || Double.isNaN(d)) {
      invalidDouble(d);
    }
    // Returning d makes it easier to insert validDouble() calls into
    // expressions without having to break those expressions up into
    // multiple statements.  The caller is free to ignore the return
    // value. - ST 11/1/04
    return d;
  }

  private void invalidDouble(double d) throws LogoException {
    // it's hard to get a context here in some situations because
    // of optimizations. the context will get set later.
    throw new EngineException
        (null, this,
            "math operation produced "
                + (Double.isInfinite(d)
                ? "a number too large for NetLogo"
                : "a non-number"));
  }

  protected static String agentClassDescription(AgentKind agentKind) {
    if (agentKind == AgentKindJ.Observer()) {
      return "the observer";
    } else if (agentKind == AgentKindJ.Turtle()) {
      return "a turtle";
    } else if (agentKind == AgentKindJ.Patch()) {
      return "a patch";
    } else if (agentKind == AgentKindJ.Link()) {
      return "a link";
    }
    return null;
  }

  // These methods are for evaluating arguments --
  // they serve the same sort of purpose that the "reportX()" methods
  // in Reporter used to serve.  ~Forrest(11/10/2006)
  //
  // Convenience methods that do type checking and casting

  public org.nlogo.agent.Agent argEvalAgent(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      org.nlogo.agent.Agent agent = (org.nlogo.agent.Agent) obj;
      if (agent.id == -1) {
        throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
      }
      return agent;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.AgentType(), obj);
    }
  }

  public org.nlogo.agent.AgentSet argEvalAgentSet(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      return (org.nlogo.agent.AgentSet) obj;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.AgentsetType(), obj);
    }
  }

  public org.nlogo.agent.AgentSet argEvalAgentSet(Context context, int argIndex, AgentKind kind)
      throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      AgentSet set = (org.nlogo.agent.AgentSet) obj;
      if (set.kind() != kind) {
        throw new ArgumentTypeException(context, this, argIndex,
            getAgentSetMask(kind), obj);
      }
      return set;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.AgentsetType(), obj);
    }
  }

  public Boolean argEvalBoolean(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      return (Boolean) obj;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.BooleanType(), obj);
    }
  }

  public boolean argEvalBooleanValue(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      return ((Boolean) obj).booleanValue();
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.BooleanType(), obj);
    }
  }

  public double argEvalDoubleValue(Context context, int argIndex) throws LogoException {
    return argEvalDouble(context, argIndex).doubleValue();
  }

  public int argEvalIntValue(Context context, int argIndex) throws LogoException {
    return argEvalDouble(context, argIndex).intValue();
  }

  public org.nlogo.core.LogoList argEvalList(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      return (LogoList) obj;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.ListType(), obj);
    }
  }

  public org.nlogo.agent.Patch argEvalPatch(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      return (org.nlogo.agent.Patch) obj;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.PatchType(), obj);
    }
  }

  public String argEvalString(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      return (String) obj;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.StringType(), obj);
    }
  }

  public Double argEvalDouble(Context context, int argIndex) throws LogoException {
    Reporter arg = args[argIndex];
    Object obj = arg.report(context);
    try {
      return (Double) obj;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.NumberType(), obj);
    }
  }

  public org.nlogo.agent.Turtle argEvalTurtle(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      return (org.nlogo.agent.Turtle) obj;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.TurtleType(), obj);
    }
  }

  public org.nlogo.agent.Link argEvalLink(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      return (org.nlogo.agent.Link) obj;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.LinkType(), obj);
    }
  }

  public org.nlogo.api.ReporterTask argEvalReporterTask(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      return (org.nlogo.api.ReporterTask) obj;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.ReporterTaskType(), obj);
    }
  }

  public org.nlogo.api.CommandTask argEvalCommandTask(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      return (org.nlogo.api.CommandTask) obj;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.CommandTaskType(), obj);
    }
  }

  public Token argEvalSymbol(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      return (Token) obj;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.SymbolType(), obj);
    }
  }

  @SuppressWarnings("unchecked")
  public List<Token> argEvalCodeBlock(Context context, int argIndex) throws LogoException {
    Object obj = args[argIndex].report(context);
    try {
      return (List<Token>) obj;
    } catch (ClassCastException ex) {
      throw new ArgumentTypeException(context, this, argIndex, Syntax.CodeBlockType(), obj);
    }
  }

  private static int getAgentSetMask(AgentKind kind) {
    if (AgentKindJ.Turtle() == kind) {
      return Syntax.TurtlesetType();
    }
    if (AgentKindJ.Patch() == kind) {
      return Syntax.PatchsetType();
    }
    if (AgentKindJ.Link() == kind) {
      return Syntax.LinksetType();
    }
    return Syntax.AgentsetType();
  }

  public void copyFieldsFrom(Instruction sourceInstr) {
    this.workspace = sourceInstr.workspace;
    this.world = sourceInstr.world;
    this.token_$eq(sourceInstr.token());
  }

  // overridden by GeneratedInstruction
  public Instruction extractErrorInstruction(EngineException ex) {
    return this;
  }

  public void copyMetadataFrom(Instruction srcInstr) {
    this.token_$eq(srcInstr.token());
    this.agentClassString = srcInstr.agentClassString;
    this.source = srcInstr.source;
    this.fullSource = srcInstr.fullSource;
    this.storedSourceStartPosition = srcInstr.storedSourceStartPosition;
    this.storedSourceEndPosition = srcInstr.storedSourceEndPosition;
  }
}

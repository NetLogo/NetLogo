package org.nlogo.extensions.table;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.AgentSetBuilder;
import org.nlogo.api.AnonymousReporter;
import org.nlogo.api.Argument;
import org.nlogo.api.Command;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;
import org.nlogo.api.Reporter;
import org.nlogo.core.CompilerException;
import org.nlogo.core.ExtensionObject;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.nvm.ExtensionContext;

import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.List;

public class TableExtension
    extends org.nlogo.api.DefaultClassManager {

  public void load(org.nlogo.api.PrimitiveManager primManager) {
    primManager.addPrimitive("clear", new Clear());
    primManager.addPrimitive("get", new Get());
    primManager.addPrimitive("get-or-default", new GetOrDefault());
    primManager.addPrimitive("has-key?", new HasKey());
    primManager.addPrimitive("keys", new Keys());
    primManager.addPrimitive("length", new Length());
    primManager.addPrimitive("make", new Make());
    primManager.addPrimitive("put", new Put());
    primManager.addPrimitive("remove", new Remove());
    primManager.addPrimitive("from-list", new FromList());
    primManager.addPrimitive("counts", new Counts());
    primManager.addPrimitive("to-list", new ToList());
    primManager.addPrimitive("values", new Values());
    primManager.addPrimitive("group-items", new GroupItems());
    primManager.addPrimitive("group-agents", new GroupAgents());
    primManager.addPrimitive("from-json-file", new JsonPrims.FromJsonFile());
    primManager.addPrimitive("from-json", new JsonPrims.FromJson());
    primManager.addPrimitive("to-json", new JsonPrims.ToJson());
  }

  public void clearAll() {
    tables.clear();
    Table.resetNext();
  }
  static WeakHashMap<Table, Long> tables = new WeakHashMap<Table, Long>();

  public StringBuilder exportWorld() {
    StringBuilder buffer = new StringBuilder();
    for (Table table : tables.keySet()) {
      buffer.append
          (Dump.csv().encode
              (Dump.extensionObject(table, true, true, false)) + "\n");
    }
    return buffer;
  }

  public void importWorld(List<String[]> lines, org.nlogo.api.ExtensionManager reader,
                          org.nlogo.api.ImportErrorHandler handler)
      throws ExtensionException {
    for (String[] line : lines) {
      try {
        reader.readFromString(line[0]);
      } catch (CompilerException e) {
        handler.showError("Error importing arrays", e.getMessage(), "This array will be ignored");
      }
    }
  }

  public static class Clear implements Command {
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax
          (new int[]{Syntax.WildcardType()});
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Table t = getTable(args[0]);
      t.clear();
    }
  }

  public static class Get implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.WildcardType(), Syntax.WildcardType()},
              Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Table t = getTable(args[0]);
      Object key = args[1].get();
      Object result = t.get(key);
      if (result == null) {
        throw new ExtensionException
            ("No value for " + Dump.logoObject(key)
                + " in table.");
      }
      return result;
    }
  }

  public static class GetOrDefault implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(
              new int[] {Syntax.WildcardType(), Syntax.WildcardType(), Syntax.WildcardType()},
              Syntax.WildcardType()
      );
    }

    public Object report(Argument args[], Context context) throws ExtensionException {
      Table t = getTable(args[0]);
      Object key = args[1].get();
      return t.getOrDefault(key, args[2].get());
    }
  }

  public static class HasKey implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.WildcardType(), Syntax.WildcardType()},
              Syntax.BooleanType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Table t = getTable(args[0]);
      return Boolean.valueOf(t.containsKey(args[1].get()));
    }
  }

  public static class Keys implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.WildcardType()},
              Syntax.ListType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Table t = getTable(args[0]);
      return LogoList.fromJava(t.keySet());
    }
  }

  public static class Length implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.WildcardType()},
              Syntax.NumberType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Table t = getTable(args[0]);
      return Double.valueOf(t.size());
    }
  }

  public static class Make implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Table t = new Table();
      tables.put(t, t.id);
      return t;
    }

  }

  public static class Put implements Command {
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax
          (new int[]{Syntax.WildcardType(), Syntax.WildcardType(),
              Syntax.WildcardType()});
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Table t = getTable(args[0]);
      Object key = args[1].get();
      ensureKeyValidity(key);
      t.put(key, args[2].get());
    }
  }

  public static class Remove implements Command {
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax
          (new int[]{Syntax.WildcardType(), Syntax.WildcardType()});
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Table t = getTable(args[0]);
      t.remove(args[1].get());
    }
  }

  public static class ToList implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.WildcardType()},
              Syntax.ListType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Table t = getTable(args[0]);
      return t.toList();

    }
  }

    public static class Values implements Reporter {
      public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax
            (new int[]{Syntax.WildcardType()},
                  Syntax.ListType());
      }

      public String getAgentClassString() {
          return "OTPL";
      }

      public Object report(Argument args[], Context context)
          throws ExtensionException, LogoException {
        Table t = getTable(args[0]);
        return t.valuesList();
      }
    }

  public static class FromList implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.ListType()},
              Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      LogoList alist = args[0].getList();
      Table t = Table.fromList(alist);
      tables.put(t, t.id);
      return t;
    }
  }

  public static class Counts implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(new int[]{Syntax.ListType()}, Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context) throws ExtensionException, LogoException {
      LogoList lst = args[0].getList();
      Table t = new Table();
      tables.put(t, t.id);
      for (Object obj : lst.javaIterable()) {
        t.put(obj, 1.0 + (Double)t.getOrDefault(obj, 0.0));
      }
      return t;
    }
  }

  public static class GroupItems implements Reporter {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(
              new int[] {Syntax.ListType(), Syntax.ReporterType() },
              Syntax.WildcardType()
      );
    }

    @Override
    public Object report(Argument args[], Context context) throws ExtensionException, LogoException {
      LogoList lst = args[0].getList();
      AnonymousReporter classifier = args[1].getReporter();
      Table t = new Table();
      tables.put(t, t.id);
      for (Object x : lst.toJava()) {
        Object group = classifier.report(context, new Object[] {x});
        ensureKeyValidity(group);
        t.put(group, ((LogoList) t.getOrDefault(group, LogoList.Empty())).lput(x));
      }
      return t;
    }
  }

  public static class GroupAgents implements Reporter {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(
              new int[] {Syntax.AgentsetType(), Syntax.ReporterBlockType()},
              Syntax.WildcardType(),
              "OTPL",
              "-TPL"
      );
    }

    @Override
    public Object report(Argument args[], Context context) throws ExtensionException, LogoException {
      AgentSet agents = (AgentSet) args[0].getAgentSet();
      org.nlogo.nvm.Reporter classifier = ((org.nlogo.nvm.Argument) args[1]).unevaluatedArgument();
      org.nlogo.nvm.Context childContext = new org.nlogo.nvm.Context(((ExtensionContext) context).nvmContext(), agents);
      AgentIterator agentIter = agents.shufflerator(context.getRNG());
      Table t = new Table();
      tables.put(t, t.id);
      while (agentIter.hasNext()) {
        Agent agent = agentIter.next();
        Object group = childContext.evaluateReporter(agent, classifier);
        ensureKeyValidity(group);
        ((AgentSetBuilder) t.computeIfAbsent(group, k -> new AgentSetBuilder(agents.kind()))).add(agent);
      }
      t.replaceAll((k,v) -> ((AgentSetBuilder) v).build());
      return t;
    }
  }

  public ExtensionObject readExtensionObject(org.nlogo.api.ExtensionManager reader,
                                                           String typeName, String value)
      throws ExtensionException {
    try {
      String[] s = value.split(":", 2);
      long id = Long.parseLong(s[0]);
      Table table = getOrCreateTableFromId(id);
      if (s.length > 1) {
        table.addAll((LogoList) reader.readFromString(s[1]));
      }
      return table;
    } catch (CompilerException ex) {
      throw new ExtensionException(ex.getMessage());
    }
  }

  private Table getOrCreateTableFromId(long id) {
    for (Table table : tables.keySet()) {
      if (table.id == id) {
        return table;
      }
    }
    Table t = new Table(id);
    tables.put(t, t.id);
    return t;
  }

  /// helpers

  public static Table getTable(Argument arg) throws ExtensionException {
    Object maybeTable = arg.get();
    if (!(maybeTable instanceof Table)) {
      throw new ExtensionException("not a table: " + Dump.logoObject(maybeTable));
    }
    return (Table)maybeTable;
  }

  private static boolean isValidKey(Object key) {
    return
      key instanceof Double ||
      key instanceof String ||
      key instanceof Boolean ||
      (key instanceof LogoList && containsOnlyValidKeys((LogoList) key));
  }

  private static void ensureKeyValidity(Object key) throws ExtensionException {
    if (!isValidKey(key)) {
      throw new ExtensionException
        (Dump.logoObject(key) + " is not a valid table key "
          + "(a table key may only be a number, a string, true or false, or a list "
          + "whose items are valid keys)");

    }
  }

  private static boolean containsOnlyValidKeys(LogoList list) {
    for (Iterator<Object> it = list.javaIterator(); it.hasNext();) {
      Object o = it.next();
      if (!isValidKey(o)) {
        return false;
      }
    }
    return true;
  }


}

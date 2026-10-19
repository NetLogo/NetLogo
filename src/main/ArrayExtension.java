package org.nlogo.extensions.array;

import org.nlogo.core.CompilerException;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.core.LogoList;
import org.nlogo.api.LogoException;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Argument;
import org.nlogo.api.Dump;
import org.nlogo.api.Context;
import org.nlogo.api.Reporter;
import org.nlogo.api.Command;

import java.util.Iterator;

public class ArrayExtension
    extends org.nlogo.api.DefaultClassManager {
  // essentially we want a WeakHashSet but Java has no such class so we use a map but we don't
  // care what the values are so we use nulls - ST 6/30/09
  private static final java.util.WeakHashMap<LogoArray, Object> arrays =
      new java.util.WeakHashMap<LogoArray, Object>();

  private static long next = 0;

  private static class LogoArray
      extends java.util.ArrayList<Object>
      // new NetLogo data types defined by extensions must implement
      // this interface
      implements org.nlogo.core.ExtensionObject {
    private final long id;

    LogoArray(long id) {
      this.id = id;
      arrays.put(this, null);
      next = StrictMath.max(next, id + 1);
    }

    LogoArray(java.util.Collection<?> collection) {
      super(collection);
      this.id = next;
      arrays.put(this, null);
      next++;
    }

    // if we're going to use LogoArrays as keys in a WeakHashMap, we need to make
    // sure they obey reference equality, otherwise if we have large numbers of
    // identical arrays the WeakHashMap lookups will take linear time - ST 6/30/09
    private final Object hashKey = new Object();

    @Override
    public int hashCode() {
      return hashKey.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj;
    }

    public String dump(boolean readable, boolean exporting, boolean reference) {
      StringBuilder buf = new StringBuilder();
      if (exporting) {
        buf.append(id);
        if (!reference) {
          buf.append(": ");
        }
      }
      if (!(reference && exporting)) {
        boolean first = true;
        for (Iterator<Object> it = iterator(); it.hasNext();) {
          if (!first) {
            buf.append(" ");
          }
          first = false;
          buf.append
              (Dump.logoObject(it.next(), true, exporting));
        }
      }
      return buf.toString();
    }

    public String getExtensionName() {
      return "array";
    }

    public String getNLTypeName() {
      // since this extension only defines one type, we don't
      // need to give it a name; "array:" is enough,
      // "array:array" would be redundant
      return "";
    }

    public boolean recursivelyEqual(Object o) {
      if (!(o instanceof LogoArray)) {
        return false;
      }
      LogoArray otherArray = (LogoArray) o;
      if (size() != otherArray.size()) {
        return false;
      }
      Iterator<Object> iter1 = iterator();
      Iterator<Object> iter2 = otherArray.iterator();
      while (iter1.hasNext()) {
        if (!org.nlogo.api.Equality.equals
            (iter1.next(), iter2.next())) {
          return false;
        }
      }
      return true;
    }
  }

  public void clearAll() {
    arrays.clear();
    next = 0;
  }

  public StringBuilder exportWorld() {
    StringBuilder buffer = new StringBuilder();
    for (LogoArray array : arrays.keySet()) {
      buffer.append
          (Dump.csv().encode
              (Dump.extensionObject(array, true, true, false)) + "\n");
    }
    return buffer;
  }

  public void importWorld(java.util.List<String[]> lines, org.nlogo.api.ExtensionManager reader,
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

  ///

  public void load(org.nlogo.api.PrimitiveManager primManager) {
    primManager.addPrimitive("item", new Item());
    primManager.addPrimitive("set", new Set());
    primManager.addPrimitive("length", new Length());
    primManager.addPrimitive("to-list", new ToList());
    primManager.addPrimitive("from-list", new FromList());
  }

  ///


  public org.nlogo.core.ExtensionObject readExtensionObject(org.nlogo.api.ExtensionManager reader,
                                                           String typeName, String value)
      throws org.nlogo.api.ExtensionException, CompilerException {
    String[] s = value.split(":", 2);
    long id = Long.parseLong(s[0]);
    LogoArray array = getOrCreateArrayFromId(id);
    if (s.length > 1) {
      array.addAll(((LogoList) reader.readFromString("[ " + s[1] + " ]")).toJava());
    }
    return array;
  }

  private LogoArray getOrCreateArrayFromId(long id) {
    for (LogoArray array : arrays.keySet()) {
      if (array.id == id) {
        return array;
      }
    }
    return new LogoArray(id);
  }

  public static class Item implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.WildcardType(),
              Syntax.NumberType()},
              Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Object arg0 = args[0].get();
      if (!(arg0 instanceof LogoArray)) {
        throw new org.nlogo.api.ExtensionException
            ("not an array: " + Dump.logoObject(arg0));
      }
      LogoArray array = (LogoArray) arg0;
      int index = args[1].getIntValue();
      if (index < 0 || index >= array.size()) {
        throw new org.nlogo.api.ExtensionException
            (index + " is not a valid index into an array of length "
                + array.size());
      }
      return array.get(index);
    }
  }

  public static class Set implements Command {
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax
          (new int[]{Syntax.WildcardType(),
              Syntax.NumberType(),
              Syntax.WildcardType()});
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Object arg0 = args[0].get();
      if (!(arg0 instanceof LogoArray)) {
        throw new org.nlogo.api.ExtensionException
            ("not an array: " + Dump.logoObject(arg0));
      }
      LogoArray array = (LogoArray) arg0;
      int index = args[1].getIntValue();
      if (index < 0 || index >= array.size()) {
        throw new org.nlogo.api.ExtensionException
            (index + " is not a valid index into an array of length "
                + array.size());
      }
      array.set(index, args[2].get());
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
      Object arg0 = args[0].get();
      if (!(arg0 instanceof LogoArray)) {
        throw new org.nlogo.api.ExtensionException
            ("not an array: " + Dump.logoObject(arg0));
      }
      return Double.valueOf(((LogoArray) arg0).size());
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
      Object arg0 = args[0].get();
      if (!(arg0 instanceof LogoArray)) {
        throw new org.nlogo.api.ExtensionException
            ("not an array: " + Dump.logoObject(arg0));
      }
      return LogoList.fromJava((LogoArray) arg0);
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
      return new LogoArray(args[0].getList().toJava());
    }
  }

}

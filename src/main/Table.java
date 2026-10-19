package org.nlogo.extensions.table;

import org.nlogo.api.Dump;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.ExtensionObject;
import org.nlogo.core.LogoList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.internal.LinkedTreeMap;

// It's important that we extend LinkedHashMap here, rather than
// plain HashMap, because we want model results to be reproducible
// crossplatform.

public class Table
    extends LinkedHashMap<Object, Object>
    implements ExtensionObject
{

  private static long next = 0;
  public static void resetNext() { next = 0; }

  public final long id;

  public static Table fromList(LogoList alist) throws ExtensionException {
    Table table = new Table();

    table.addAll(alist);

    return table;
  }

  public static Table fromMap(Map<?,?> map) {
    Table table = new Table();

    for (Map.Entry<?,?> entry : map.entrySet()) {
      table.put(entry.getKey(), table.getTableValue(entry.getValue()));
    }

    return table;
  }

  public Table() {
    this.id = Table.next;
    Table.next++;
  }

  public void addAll(LogoList alist)
      throws ExtensionException {
    for (Iterator<Object> it = alist.javaIterator(); it.hasNext();) {
      Object pair = it.next();
      if ((pair instanceof LogoList
          && ((LogoList) pair).size() < 2)
          || (!(pair instanceof LogoList))) {
        throw new ExtensionException
            ("expected a two-element list: " +
                Dump.logoObject(pair));
      }
      this.put(((LogoList) pair).first(),
          ((LogoList) pair).butFirst().first());
    }
  }

  public Table(long id) {
    this.id = id;
    next = StrictMath.max(next, id + 1);
  }

  private Object getTableValue(Object value) {
    // return the value to be added in a table being constructed from a Map
    if (value instanceof LinkedTreeMap) {
      return Table.fromMap((Map<?,?>)value);
    } else if (value instanceof ArrayList) {
      LogoListBuilder alist = new LogoListBuilder();
      ((ArrayList<?>)value).forEach((temp) -> {
        alist.add(getTableValue(temp));
      });
      return alist.toLogoList();
    } else {
      return value;
    }
  }

  public boolean equals(Object obj) {
    return this == obj;
  }

  public LogoList toList() {
    LogoListBuilder alist = new LogoListBuilder();
    for (Iterator<Map.Entry<Object, Object>> entries = entrySet().iterator(); entries.hasNext();) {
      Map.Entry<Object, Object> entry = entries.next();
      LogoListBuilder pair = new LogoListBuilder();
      pair.add(entry.getKey());
      pair.add(entry.getValue());
      alist.add(pair.toLogoList());
    }
    return alist.toLogoList();
  }

  public LogoList valuesList() {
    LogoListBuilder alist = new LogoListBuilder();
    for (Iterator<Map.Entry<Object, Object>> entries = entrySet().iterator(); entries.hasNext();) {
      Map.Entry<Object, Object> entry = entries.next();
      alist.add(entry.getValue());
    }
    return alist.toLogoList();
  }

  public String dump(boolean readable, boolean exportable, boolean reference) {
    if (exportable && reference) {
      return ("" + id);
    } else {
      return (exportable ? (id + ": ") : "") + Dump.logoObject(this.toList(), true, exportable);
    }
  }

  public String getExtensionName() {
    return "table";
  }

  public String getNLTypeName() {
    // since this extension only defines one type, we don't
    // need to give it a name; "table:" is enough,
    // "table:table" would be redundant
    return "";
  }

  public boolean recursivelyEqual(Object o) {
    if (!(o instanceof Table)) {
      return false;
    }
    Table otherTable = (Table) o;
    if (size() != otherTable.size()) {
      return false;
    }
    for (Iterator<Object> iter = keySet().iterator(); iter.hasNext();) {
      Object key = iter.next();
      if (!otherTable.containsKey(key)
          || !org.nlogo.api.Equality.equals(get(key),
          otherTable.get(key))) {
        return false;
      }
    }
    return true;
  }
}  // closing bracked for implements

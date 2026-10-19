package org.nlogo.extensions.profiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Arrays;

import java.util.Set;
import org.nlogo.nvm.Activation;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Procedure;
import org.nlogo.core.LogoList;

public class QuickTracer extends org.nlogo.nvm.Tracer {
  private static class Record {
    Procedure procedure;  // procedure that this record refers to.
    int running = 0;    // number of currently running instances.
    long inclusive = 0;  // accumulated inclusive time.
    long exclusive = 0;  // accumulated exclusive time.
    long invoketime = 0;  // when first currently running instance invoked.
    long callcount = 0;  // number of calls.
  }

  private boolean enabled;
  private HashMap<Procedure, Record> records;
  private HashMap<String, Procedure> procedureNames;
  private Record current;  // currently executing procedure.
  private Activation frame;  // current activation
  private long resumetime;   // when current procedure resumed.

  public QuickTracer() {
    reset();
  }

  private Record addProcedure(Procedure p, long t) {
    Record r = new Record();
    r.procedure = p;
    r.inclusive = 0;
    r.exclusive = 0;
    r.running = 1;
    r.invoketime = t;
    r.callcount = 0;
    records.put(p, r);
    if (p != null) {
      procedureNames.put(p.name().toUpperCase(), p);
    }
    return r;
  }

  public void reset() {
    records = new HashMap<Procedure, Record>();
    procedureNames = new HashMap<String, Procedure>();
    current = null;
    frame = null;
    enabled = false;
    addProcedure(null, 0L);  // add entry for outermost level
  }

  public void enable() {
    current = records.get(null);
    frame = null;
    long t = System.nanoTime();
    resumetime = t;
    current.invoketime = t;
    enabled = true;
  }

  public void disable() {
    // close calls for all procedures in the stack,
    // as we can't keep track from now on.
    if (enabled) {
      Record toplev = records.get(null);
      while (current != toplev) {
        closeCallRecord(null, frame);
      }
      long t = System.nanoTime();
      toplev.exclusive += t - resumetime;
      toplev.inclusive += t - toplev.invoketime;
      enabled = false;
      current = null;
    }
  }

  public void openCallRecord(Context context, Activation activation) {
    if (enabled) {
      long t = System.nanoTime();
      Procedure p = activation.procedure;
      Record r = records.get(p);
      if (r == null) {
        r = addProcedure(p, t);
      } else if (r.running++ == 0) {
        r.invoketime = t;
      }
      current.exclusive += t - resumetime;
      r.callcount++;
      current = r;
      frame = activation;
      resumetime = t;
    }
  }

  public void closeCallRecord(Context context, Activation activation) {
    if (enabled) {
      long t = System.nanoTime();
      Record r = records.get(activation.procedure);
      // we can get close without open if profiler is started within a procedure.
      if (r != null) {
        if (--r.running == 0) {
          r.inclusive += t - r.invoketime;
        }
        current.exclusive += t - resumetime;
        frame = activation.parent().getOrElse(null);
        current = records.get(frame.procedure);
        // if we're ascending to a procedure we didn't start in,
        // substitute our own top-level instead.
        if (current == null || current.running == 0) {
          current = records.get(null);
          frame = null;
        }
        resumetime = t;
      }
    }
  }

  public static class ExclRecordComparator implements Comparator<Record> {
    public int compare(Record r1, Record r2) {
      if (r2.exclusive > r1.exclusive) {
        return 1;
      }
      if (r1.exclusive > r2.exclusive) {
        return -1;
      }
      return 0;
    }
  }

  public static class InclRecordComparator implements Comparator<Record> {
    public int compare(Record r1, Record r2) {
      if (r2.inclusive > r1.inclusive) {
        return 1;
      }
      if (r1.inclusive > r2.inclusive) {
        return -1;
      }
      return 0;
    }
  }

  public static class CallsRecordComparator implements Comparator<Record> {
    public int compare(Record r1, Record r2) {
      if (r2.callcount > r1.callcount) {
        return 1;
      }
      if (r1.callcount > r2.callcount) {
        return -1;
      }
      return 0;
    }
  }

  public void dump(java.io.PrintStream s) {
    // remove the root record so it doesn't show up in the results.
    Record toplev = records.remove(null);
    Record[] recs = records.values().toArray(new Record[0]);
    s.println("BEGIN PROFILING DUMP");
    s.println("Sorted by Exclusive Time");
    s.format("%-30s%10s %10s %10s %10s\n",
        "Name", "Calls", "Incl T(ms)", "Excl T(ms)", "Excl/calls");
    dumpProcedures(s, recs, new ExclRecordComparator());
    s.println("");
    s.println("Sorted by Inclusive Time");
    dumpProcedures(s, recs, new InclRecordComparator());
    s.println("");
    s.println("Sorted by Number of Calls");
    dumpProcedures(s, recs, new CallsRecordComparator());
    s.println("END PROFILING DUMP");
    records.put(null, toplev);
  }

  private void dumpProcedures(java.io.PrintStream s, Record[] recs,
                              java.util.Comparator<Record> c) {
    java.util.Arrays.sort(recs, c);
    for (int i = 0; i < recs.length; i++) {
      dumpProcedure(s, recs[i]);
    }
  }

  private void dumpProcedure(java.io.PrintStream s, Record r) {
    s.format("%-30s%10d %10.3f %10.3f %10.3f\n",
        r.procedure.name(),
        r.callcount,
        (double) r.inclusive / 1000000.0,
        (double) r.exclusive / 1000000.0,
        ((double) r.exclusive / (double) r.callcount) / 1000000.0);
  }

  public long calls(String procedure) {
    return findProcedure(procedure).callcount;
  }

  public long inclusiveTime(String procedure) {
    return findProcedure(procedure).inclusive;
  }

  public long exclusiveTime(String procedure) {
    return findProcedure(procedure).exclusive;
  }

  private Record findProcedure(String procedure) {
    Procedure proc = procedureNames.get(procedure);
    if (proc != null) {
      return records.get(proc);
    }
    return new Record();
  }

  public Set<String> procedureNames() {
    return Collections.unmodifiableSet(procedureNames.keySet());
  }
}

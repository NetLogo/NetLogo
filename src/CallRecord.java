package org.nlogo.extensions.profiler;

public class CallRecord {
  public String name;
  public long jobID;
  public long contextID;
  public String agent;
  public long startTime;
  public long stopTime;
  public CallRecord caller;
  public java.util.Vector<CallRecord> called;
  public String[] argDescriptions;

  public CallRecord(String name,
                    String agent,
                    String[] argDescriptions) {
    this.name = name;
    this.agent = agent;
    this.argDescriptions = argDescriptions;
    this.called = new java.util.Vector<CallRecord>();
  }

  public long exclusiveTime() {
    long base = this.inclusiveTime();

    int i;
    int calledSize = called.size();

    for (i = 0; i < calledSize; i++) {
      base = base - called.get(i).inclusiveTime();
    }
    return base;
  }

  public long inclusiveTime() {
    return stopTime - startTime;
  }

  public String toString() {
    return "CallRecord[" + name + "][" + agent + "] " + this.inclusiveTime() + " " +
        // exclusive time is ill-defined at present due to way profiler hooks into _return
        // primitive, results in more than one Call collecting "time" at once when used
        // in an ask or other mechanism which steps thru multiple contexts
        // + this.exclusiveTime() + " ("
        +this.startTime + "/" + this.stopTime + ")";
  }
}

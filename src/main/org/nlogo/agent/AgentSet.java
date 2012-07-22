// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentKind;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.LogoList;

public abstract strictfp class AgentSet
    implements org.nlogo.api.AgentSet {

  private final byte agentBit;

  public byte getAgentBit() {
    return agentBit;
  }

  private final AgentKind _kind;
  public AgentKind kind() {
    return _kind;
  }

  final World world;

  public World world() {
    return world;
  }

  private boolean isDirected = false;

  public boolean isDirected() {
    return isDirected;
  }

  private boolean isUndirected = false;

  public boolean isUndirected() {
    return isUndirected;
  }

  public void setDirected(boolean directed) {
    isDirected = directed;
    isUndirected = !directed;
  }

  public void clearDirected() {
    isDirected = false;
    isUndirected = false;
  }

  // true only for the the TURTLES, PATCHES, and BREED AgentSets;
  // used by iterator() to discern which special cases to be aware of
  final boolean removableAgents;

  public abstract int count();

  AgentSet(AgentKind kind, World world, String printName, boolean removableAgents) {
    _kind = kind;
    this.world = world;
    this.printName = printName;
    this.removableAgents = removableAgents;
    if (kind == AgentKindJ.Patch()) {
      agentBit = Patch.BIT;
    } else if (kind == AgentKindJ.Turtle()) {
      agentBit = Turtle.BIT;
    } else if (kind == AgentKindJ.Link()) {
      agentBit = Link.BIT;
    } else if (kind == AgentKindJ.Observer()) {
      agentBit = Observer.BIT;
    } else {
      throw new IllegalStateException("unknown kind: " + kind);
    }
  }

  public boolean equalAgentSets(org.nlogo.api.AgentSet otherSet) {
    return this == otherSet ||
        (kind() == otherSet.kind() &&
            count() == otherSet.count() &&
            equalAgentSetsHelper(otherSet));
  }

  abstract boolean equalAgentSetsHelper(org.nlogo.api.AgentSet otherSet);

  final String printName;

  public String printName() {
    return printName;
  }

  public abstract boolean isEmpty();

  public abstract Agent agent(long i);

  abstract Agent getAgent(Object id);

  public abstract void add(Agent agent);

  abstract void remove(Object key);

  abstract void clear();

  public abstract boolean contains(Agent agent);

  public abstract Agent randomOne(int precomputedCount, int random);

  abstract Agent[] randomTwo(int precomputedCount, int random1, int random2);

  abstract Agent[] randomSubsetGeneral(int resultSize, int precomputedCount,
                                       org.nlogo.util.MersenneTwisterFast randomerizer);

  public AgentSet randomSubset(int resultSize, int precomputedCount,
                               org.nlogo.util.MersenneTwisterFast randomerizer) {
    Agent[] result;
    if (resultSize == 0) {
      result = new Agent[0];
    } else if (resultSize == 1) {
      result = new Agent[]{randomOne(precomputedCount,
          randomerizer.nextInt(precomputedCount))};
    } else if (resultSize == 2) {
      result = randomTwo(precomputedCount, randomerizer.nextInt(precomputedCount),
          randomerizer.nextInt(precomputedCount - 1));
    } else {
      result = randomSubsetGeneral(resultSize, precomputedCount, randomerizer);
    }
    return new ArrayAgentSet(kind(), result, world);
  }


  public abstract LogoList toLogoList();

  public abstract Agent[] toArray();

  // I'd really prefer we use Iterator<Agent>, especially since then
  // we could be an Iterable<Agent>, but it's no good because type
  // erasure means if we use Iterator<Agent> there are typecasts
  // going on under the hood, and I found that this actually impacts
  // performance on benchmarks (to the tune of 5% or so on Life
  // Benchmark, for example).  Sigh... - ST 2/9/09

  public interface Iterator {
    boolean hasNext();
    Agent next();
    void remove();
  }

  public abstract Iterator iterator();

  public abstract Iterator shufflerator(org.nlogo.util.MersenneTwisterFast random);

  // from org.nlogo.api.Dump though, we really need to get a regular iterator
  // in order not to depend on org.nlogo.agent, so we provide this method
  public Iterable<org.nlogo.api.Agent> agents() {
    return new Iterable<org.nlogo.api.Agent>() {
      Iterator it = AgentSet.this.iterator();

      public java.util.Iterator<org.nlogo.api.Agent> iterator() {
        return new java.util.Iterator<org.nlogo.api.Agent>() {
          public boolean hasNext() {
            return it.hasNext();
          }

          public Agent next() {
            return it.next();
          }

          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

}

// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentKind;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.LogoList;

import java.util.Map;
import java.util.TreeMap;

// Used only for the all-turtles, all-links and the breed agentsets.

public strictfp class TreeAgentSet
    extends AgentSet {

  // we use a tree map here so that regardless of what order the turtles
  // are put in they come out in the same order (by who number) otherwise
  // we get different results after an import and export. since we don't
  // know the order that the turtles entered the breed agentset.
  final Map<Object, Agent> agents = new TreeMap<Object, Agent>();

  @Override
  public int count() {
    return agents.size();
  }

  @Override
  public boolean isEmpty() {
    return agents.isEmpty();
  }

  // This assumes we've already checked that the counts
  // are equal. - ST 7/6/06
  @Override
  boolean equalAgentSetsHelper(org.nlogo.api.AgentSet otherSet) {
    for (org.nlogo.api.Agent a : otherSet.agents()) {
      if (!contains((Agent) a)) {
        return false;
      }
    }
    return true;
  }

  public TreeAgentSet(AgentKind kind, String printName, World world) {
    super(kind, world, printName, true);
  }

  @Override
  public Agent agent(long i) {
    Double index = Double.valueOf(i);
    if (kind() == AgentKindJ.Turtle() || kind() == AgentKindJ.Link()) {
      Agent agent = agents.get(index);
      if (agent == null) {
        return null;
      }
      if (agent.id == -1) {
        agents.remove(index);
        return null;
      } else {
        return agent;
      }
    } else {
      return agents.get(index);
    }
  }

  @Override
  Agent getAgent(Object id) {
    return agents.get(id);
  }

  private long nextIndex = 0;

  /**
   * It is the caller's responsibility not to add an agent that
   * is already in the set.
   */
  @Override
  public void add(Agent agent) {
    if (agent.kind() != kind()) {
      throw new IllegalStateException();
    }
    agents.put(agent.agentKey(), agent);
    nextIndex = StrictMath.max(nextIndex, agent.id + 1);
  }

  // made public for mutable agentset operations
  @Override
  public void remove(Object key) {
    agents.remove(key);
  }

  @Override
  void clear() {
    agents.clear();
  }

  @Override
  public boolean contains(Agent agent) {
    return agents.containsValue(agent);
  }

  // the next few methods take precomputedCount as an argument since
  // we want to avoid _randomoneof and _randomnof resulting in
  // more than one total call to count(), since count() can
  // be O(n) - ST 2/27/03

  @Override
  public Agent randomOne(int precomputedCount, int random) {
    // note: we can assume agentset is nonempty , since _randomoneof.java checks for that
    AgentSet.Iterator iter = iterator();
    for (int i = 0; i < random; i++) {
      iter.next(); // skip to the right place
    }
    return iter.next();
  }

  // This is used to optimize the special case of randomSubset where
  // size == 2
  @Override
  Agent[] randomTwo(int precomputedCount, int random1, int random2) {
    Agent[] result = new Agent[2];

    // we know precomputedCount, or this method would not have been called.
    // see randomSubset().
    if (random2 >= random1) {
      // if random2 >= random1, we need to increment random2 to choose a
      // later agent.
      random2++;
    } else {
      // if random2 < random1, we swap them so our indices are in order.
      int tmp = random1;
      random1 = random2;
      random2 = tmp;
    }
    if (precomputedCount == nextIndex) {
      result[0] = agents.get(Double.valueOf(random1));
      result[1] = agents.get(Double.valueOf(random2));
    } else {
      AgentSet.Iterator iter = iterator();
      int i = 0;
      while (i++ < random1) {
        iter.next(); // skip to the first place
      }
      result[0] = iter.next();
      while (i++ < random2) {
        iter.next(); // skip to the next place
      }
      result[1] = iter.next();
    }
    return result;
  }

  @Override
  Agent[] randomSubsetGeneral(int resultSize, int precomputedCount,
                              org.nlogo.util.MersenneTwisterFast randomerator) {
    Agent result[] = new Agent[resultSize];
    AgentSet.Iterator iter = iterator();
    for (int i = 0, j = 0; j < resultSize; i++) {
      Agent next = iter.next();
      if (randomerator.nextInt(precomputedCount - i)
          < resultSize - j) {
        result[j] = next;
        j++;
      }
    }
    return result;
  }

  @Override
  public LogoList toLogoList() {
    return LogoList.fromJava(agents.values());
  }

  @Override
  public Agent[] toArray() {
    return agents.values().toArray(new Agent[agents.size()]);
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder("TreeAgentSet");
    s = s.append("\n...... kind: ");
    s = s.append(kind() == null ? "null" : kind().toString());
    s = s.append("\n...... count(): " + count());
    s = s.append("\n...... agents: ");
    for (AgentSet.Iterator iter = iterator(); iter.hasNext();) {
      s = s.append("\n" + iter.next().toString());
    }
    return s.toString();
  }

  // parent enumeration class
  public class Iterator
      implements AgentSet.Iterator {
    java.util.Iterator<Agent> iter = agents.values().iterator();

    public boolean hasNext() {
      return iter.hasNext();
    }

    public Agent next() {
      return iter.next();
    }

    public void remove() {
      throw new UnsupportedOperationException
          ("remove() not supported");
    }
  }

  // returns an Iterator object of the appropriate class
  @Override
  public AgentSet.Iterator iterator() {
    return new Iterator();
  }

  /// shuffling iterator = shufflerator! (Google hits: 0)

  @Override
  public AgentSet.Iterator shufflerator(org.nlogo.util.MersenneTwisterFast random) {
    // note it at the moment (and this should probably be fixed)
    // Job.runExclusive() counts on this making a copy of the
    // contents of the agentset - ST 12/15/05
    return new Shufflerator(random);
  }

  private class Shufflerator
      extends Iterator {
    private int i = 0;
    private final Agent[] copy;
    private Agent next;
    private final org.nlogo.util.MersenneTwisterFast random;

    Shufflerator(org.nlogo.util.MersenneTwisterFast random) {
      copy = agents.values().toArray(new Agent[agents.size()]);
      this.random = random;
      fetch();
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public Agent next() {
      Agent result = next;
      fetch();
      return result;
    }

    private void fetch() {
      if (i >= copy.length) {
        next = null;
      } else {
        if (i < copy.length - 1) {
          int r = i + random.nextInt(copy.length - i);
          next = copy[r];
          copy[r] = copy[i];
        } else {
          next = copy[i];
        }
        i++;
      }
    }
  }
}

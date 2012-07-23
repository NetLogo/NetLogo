// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentKind;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.LogoList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

// ArrayAgentSets are only used for agentsets which are never added to
// after they are initially created.  However note that turtles and
// links can die, so we may end up with an array containing some dead
// agents (agents with id -1).  There is some code below that attempts
// to replace dead agents with nulls (so the dead agents can be
// garbage colleted), but that's not guaranteed to happen, so the
// contents of the array may be any mixture of live agents, dead
// agents, and nulls. - ST 7/24/07

public final strictfp class ArrayAgentSet
    extends AgentSet {
  Agent[] agents;

  private int size = 0;

  @Override
  public int count() {
    if ((kind() == AgentKindJ.Turtle() || kind() == AgentKindJ.Link()) && !removableAgents) {
      // some of the turtles might be dead, so we need
      // to actually count them - ST 2/27/03
      int result = 0;
      for (AgentSet.Iterator iter = iterator(); iter.hasNext();) {
        iter.next();
        result++;
      }
      return result;
    } else {
      return size;
    }
  }

  // This assumes we've already checked that the counts
  // are equal. - ST 7/6/06
  @Override
  boolean equalAgentSetsHelper(org.nlogo.api.AgentSet otherSet) {
    HashSet<Agent> set = new HashSet<Agent>();
    for (AgentSet.Iterator iter = iterator(); iter.hasNext();) {
      set.add(iter.next());
    }
    for (org.nlogo.api.Agent a : otherSet.agents()) {
      if (!set.contains(a)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isEmpty() {
    if ((kind() == AgentKindJ.Turtle() || kind() == AgentKindJ.Link()) && !removableAgents) {
      // all of the turtles might be dead, so we need
      // to actually scan them - ST 2/27/03
      return !iterator().hasNext();
    } else {
      return size == 0;
    }
  }

  private final int initialCapacity;

  private int capacity = 0;

  int capacity() {
    return capacity;
  }

  public ArrayAgentSet(AgentKind kind, int initialCapacity, boolean removableAgents, World world) {
    super(kind, world, null, removableAgents);
    this.initialCapacity = initialCapacity;
    agents = new Agent[initialCapacity];
    capacity = initialCapacity;
  }

  public ArrayAgentSet(AgentKind kind, Agent[] agents, World world) {
    super(kind, world, null, false);
    initialCapacity = agents.length;
    this.agents = agents;
    capacity = initialCapacity;
    // note: we're assuming the array passed in has no nulls
    size = initialCapacity;
  }

  public ArrayAgentSet(AgentKind kind, Agent[] agents, String printName, World world) {
    super(kind, world, printName, false);
    initialCapacity = agents.length;
    this.agents = agents;
    capacity = initialCapacity;
    // note: we're assuming the array passed in has no nulls
    size = initialCapacity;
  }

  ArrayAgentSet(AgentKind kind, int initialCapacity, String printName, boolean removableAgents, World world) {
    super(kind, world, printName, removableAgents);
    this.initialCapacity = initialCapacity;
    agents = new Agent[initialCapacity];
    capacity = initialCapacity;
  }

  @Override
  public Agent agent(long i) {
    if (kind() == AgentKindJ.Turtle() || kind() == AgentKindJ.Link()) {
      Agent agent = agents[(int) i];
      if (agent.id == -1) {
        agents[(int) i] = null;
        return null;
      } else {
        return agent;
      }
    } else {
      return agents[(int) i];
    }
  }

  @Override
  Agent getAgent(Object id) {
    return agents[((Double) id).intValue()];
  }

  @Override
  public void add(Agent agent) {
    if (size < capacity) {
      agents[size] = agent;
      size++;
    } else {
      Agent[] newagents = new Agent[capacity * 2];
      System.arraycopy(agents, 0, newagents, 0, capacity);
      agents = newagents;
      capacity *= 2;
      add(agent);
    }
  }

  @Override
  void remove(Object id) {
    throw new IllegalStateException
        ("Cannot call remove() from an  ArrayAgentSet");
  }

  @Override
  void clear() {
    if (!removableAgents) // this case would confuse iterator()
    {
      throw new IllegalStateException
          ("Cannot call remove() on an AgentSet with removableAgents set to false");
    }
    capacity = initialCapacity;
    agents = new Agent[capacity];
    size = 0;
  }

  @Override
  public boolean contains(Agent agent) {
    for (AgentSet.Iterator iter = iterator(); iter.hasNext();) {
      if (iter.next() == agent) {
        return true;
      }
    }
    return false;
  }

  // the next few methods take precomputedCount as an argument since
  // we want to avoid _randomoneof and _randomnof resulting in
  // more than one total call to count(), since count() can
  // be O(n) - ST 2/27/03

  @Override
  public Agent randomOne(int precomputedCount, int random) {
    // note: we can assume agentset is nonempty , since _randomoneof.java checks for that
    if ((size == capacity) &&
        !((kind() == AgentKindJ.Turtle() || kind() == AgentKindJ.Link()) && !removableAgents)) {
      return agents[random];
    } else {
      AgentSet.Iterator iter = iterator();
      for (int i = 0; i < random; i++) {
        iter.next(); // skip to the right place
      }

      return iter.next();
    }
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
    if ((size == capacity) &&
        !((kind() == AgentKindJ.Turtle() || kind() == AgentKindJ.Link()) && !removableAgents))

    {
      result[0] = agents[random1];
      result[1] = agents[random2];
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
                              org.nlogo.util.MersenneTwisterFast random) {
    Agent result[] = new Agent[resultSize];
    if (precomputedCount == capacity) {
      for (int i = 0, j = 0; j < resultSize; i++) {
        if (random.nextInt(precomputedCount - i)
            < resultSize - j) {
          result[j] = agents[i];
          j++;
        }
      }
    } else {
      AgentSet.Iterator iter = iterator();
      for (int i = 0, j = 0; j < resultSize; i++) {
        Agent next = iter.next();
        if (random.nextInt(precomputedCount - i)
            < resultSize - j) {
          result[j] = next;
          j++;
        }
      }
    }
    return result;
  }

  @Override
  public LogoList toLogoList() {
    ArrayList<Agent> result = new ArrayList<Agent>();
    for (AgentSet.Iterator iter = iterator(); iter.hasNext();) {
      Agent agent = iter.next();
      result.add(agent);
    }
    Collections.sort(result);
    return LogoList.fromJava(result);
  }

  @Override
  public Agent[] toArray() {
    return agents;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder("AgentSet");
    s = s.append("\n...... kind: ");
    s = s.append(kind() == null ? "null" : kind().toString());
    s = s.append("\n...... size: " + size);
    s = s.append("\n...... count(): " + count());
    s = s.append("\n...... capacity: " + capacity);
    s = s.append("\n...... agents: ");
    for (AgentSet.Iterator iter = iterator(); iter.hasNext();) {
      s = s.append("\n" + iter.next().toString());
    }
    return s.toString();
  }

  // parent enumeration class
  public class Iterator
      implements AgentSet.Iterator {
    int index;

    public boolean hasNext() {
      return index < size;
    }

    public Agent next() {
      return agents[index++];
    }

    public void remove() {
      throw new UnsupportedOperationException
          ("remove() not supported");
    }
  }

  // extended to skip dead agents
  private class IteratorWithDead extends Iterator {
    IteratorWithDead() {
      // skip initial dead agents
      while (index < size && agents[index].id == -1) {
        index++;
      }
    }

    @Override
    public Agent next() {
      int resultIndex = index;
      // skip to next live agent
      do {
        index++;
      }
      while (index < size && agents[index].id == -1);
      return agents[resultIndex];
    }
  }

  // returns an Iterator object of the appropriate class
  @Override
  public AgentSet.Iterator iterator() {
    if (kind() == AgentKindJ.Patch()) {
      return new Iterator();
    } else {
      return new IteratorWithDead();
    }
  }

  /// shuffling iterator = shufflerator! (Google hits: 0)
  /// Update: Now 5 Google hits, the first 4 of which are NetLogo related,
  /// and the last one is a person named "SHUFFLER, Ator", which Google thought
  /// was close enough!  ;-)  ~Forrest (10/3/2008)

  @Override
  public AgentSet.Iterator shufflerator(org.nlogo.util.MersenneTwisterFast random) {
    // note it at the moment (and this should probably be fixed)
    // Job.runExclusive() counts on this making a copy of the
    // contents of the agentset - ST 12/15/05
    return new Shufflerator(random);
  }

  private class Shufflerator extends Iterator {
    private int i = 0;
    private final Agent[] copy = new Agent[size];
    private Agent next;
    private final org.nlogo.util.MersenneTwisterFast random;

    Shufflerator(org.nlogo.util.MersenneTwisterFast random) {
      this.random = random;
      System.arraycopy(agents, 0, copy, 0, size);
      while (i < copy.length && copy[i] == null) {
        i++;
      }
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
        // we could have a bunch of different Shufflerator subclasses
        // the same way we have Iterator subclasses in order to avoid
        // having to do both checks, but I'm not
        // sure it's really worth the effort - ST 3/15/06
        if (next == null || next.id == -1) {
          fetch();
        }
      }
    }
  }
}

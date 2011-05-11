package org.nlogo.agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;

public strictfp class NetworkMetrics {

  private final LinkManager linkManager;

  NetworkMetrics(LinkManager linkManager) {
    this.linkManager = linkManager;
  }

  /**
   * This method performs a BFS from the sourceNode,
   * following the network imposed by the given linkBreed,
   * going up to radius layers out, and only collecting
   * nodes that are members of sourceSet.
   * <p/>
   * Note: this method follows directed links both directions.
   * But we could change its functionality when dealing with
   * directed links -- I'm not sure what the right thing is.
   * ~Forrest (5/11/2007)
   */
  public Set<Turtle> inNetworkRadius(Turtle sourceNode, AgentSet sourceSet,
                                     double radius, AgentSet linkBreed) {
    HashSet<Turtle> seen = new HashSet<Turtle>();
    HashSet<Turtle> visited = new HashSet<Turtle>();
    LinkedList<Turtle> queue = new LinkedList<Turtle>();
    queue.addLast(sourceNode);
    seen.add(sourceNode);
    // we use null to mark radius-layer boundaries
    queue.addLast(null);

    int layer = 0;
    while (layer <= radius) {
      Turtle curNode = queue.removeFirst();
      if (curNode == null) {
        if (queue.isEmpty()) {
          break;
        }
        layer++;
        queue.addLast(null);
        continue;
      }
      visited.add(curNode);
      AgentSet neighborSet = linkManager.findLinkedWith(curNode,
          linkBreed);
      for (AgentSet.Iterator it = neighborSet.iterator(); it.hasNext();) {
        Turtle toAdd = (Turtle) it.next();
        if (!seen.contains(toAdd)) {
          seen.add(toAdd);
          queue.add(toAdd);
        }
      }
    }
    queue.clear();
    seen.clear();

    HashSet<Turtle> result = new HashSet<Turtle>();
    // filter, so we only have agents from sourceSet
    for (Turtle node : visited) {
      if (sourceSet.contains(node)) {
        result.add(node);
      }
    }
    return result;
  }

  /**
   * This method performs a BFS from the sourceNode,
   * following the network imposed by the given linkBreed,
   * to find the distance to destNode.
   * Directed links are only followed in the "forward" direction.
   * It returns -1 if there is no path between the two nodes.
   * ~Forrest (5/11/2007)
   */
  public int networkDistance(Turtle sourceNode, Turtle destNode,
                             AgentSet linkBreed) {
    boolean isDirectedBreed = linkBreed.isDirected();
    HashSet<Turtle> seen = new HashSet<Turtle>();
    LinkedList<Turtle> queue = new LinkedList<Turtle>();
    queue.addLast(sourceNode);
    seen.add(sourceNode);
    // we use null to mark radius-layer boundaries
    queue.addLast(null);

    int layer = 0;
    while (true) {
      Turtle curNode = queue.removeFirst();
      if (curNode == null) {
        if (queue.isEmpty()) {
          break;
        }
        layer++;
        queue.addLast(null);
        continue;
      }
      if (curNode == destNode) {
        return layer;
      }
      AgentSet neighborSet;
      if (isDirectedBreed) {
        neighborSet = linkManager.findLinkedFrom(curNode, linkBreed);
      } else {
        neighborSet = linkManager.findLinkedWith(curNode, linkBreed);
      }
      for (AgentSet.Iterator it = neighborSet.iterator(); it.hasNext();) {
        Turtle toAdd = (Turtle) it.next();
        if (!seen.contains(toAdd)) {
          seen.add(toAdd);
          queue.add(toAdd);
        }
      }
    }
    return -1;
  }

  /**
   * This method performs a BFS from the sourceNode,
   * following the network imposed by the given linkBreed,
   * to find the shortest path to destNode.
   * Directed links are only followed in the "forward" direction.
   * <p/>
   * It returns an empty list if there is no path between the two nodes.
   * The BFS proceeds in a random order, so if there are multiple
   * shortest paths, a random one will be returned.
   * Note, however, that the probability distribution of this random
   * choice is subtly different from if we had enumerated *all* shortest
   * paths, and chose one of them uniformly at random.
   * I don't think there is an efficient way to implement it that other way.
   * ~Forrest (5/11/2007)
   */
  public LogoList networkShortestPathNodes(org.nlogo.util.MersenneTwisterFast random,
                                           Turtle sourceNode, Turtle destNode,
                                           AgentSet linkBreed) {
    LogoListBuilder path = new LogoListBuilder();
    if (sourceNode.equals(destNode)) {
      path.add(sourceNode);
      return path.toLogoList();
    }
    boolean isDirectedBreed = linkBreed.isDirected();
    // we use this HashMap to track which nodes have been seen
    // by the BFS, as well as who their "parents" are, so we can
    // walk the path back to the source.
    HashMap<Turtle, Turtle> seenParents = new HashMap<Turtle, Turtle>();
    LinkedList<Turtle> queue = new LinkedList<Turtle>();
    queue.addLast(sourceNode);
    seenParents.put(sourceNode, null);

    while (!queue.isEmpty()) {
      Turtle curNode = queue.removeFirst();

      AgentSet neighborSet;
      if (isDirectedBreed) {
        neighborSet = linkManager.findLinkedFrom(curNode, linkBreed);
      } else {
        neighborSet = linkManager.findLinkedWith(curNode, linkBreed);
      }
      for (AgentSet.Iterator it = neighborSet.shufflerator(random);
           it.hasNext();) {
        Turtle toAdd = (Turtle) it.next();
        if (toAdd.equals(destNode)) {
          path.add(destNode);
          Turtle agt = curNode;
          while (agt != null) {
            path.add(agt);
            agt = seenParents.get(agt);
          }
          return path.toLogoList();
        }
        if (!seenParents.containsKey(toAdd)) {
          seenParents.put(toAdd, curNode);
          queue.add(toAdd);
        }
      }
    }
    return LogoList.Empty();
  }

  public LogoList networkShortestPathLinks(org.nlogo.util.MersenneTwisterFast random,
                                           Turtle sourceNode, Turtle destNode, AgentSet linkBreed) {
    LogoList pathNodes = networkShortestPathNodes(random, sourceNode,
        destNode, linkBreed);
    LogoListBuilder pathLinks = new LogoListBuilder();
    if (pathNodes.size() <= 1) {
      return pathLinks.toLogoList(); // empty
    }
    Iterator<Object> it = pathNodes.iterator();
    Turtle t1 = (Turtle) it.next();
    while (it.hasNext()) {
      Turtle t2 = (Turtle) it.next();
      pathLinks.add(linkManager.findLink(t1, t2, linkBreed, true));
      t1 = t2;
    }

    return pathLinks.toLogoList();
  }


  /**
   * Calculates the average shortest-path length between all (distinct) pairs
   * of nodes in the given nodeSet, by traveling along links of the given linkBreed.
   * <p/>
   * It returns -1 if any two nodes in nodeSet are not connected by a path.
   * <p/>
   * Note: this method follows directed links both directions.
   * But we could change its functionality when dealing with
   * directed links -- I'm not sure what the right thing is.
   * Seems like often the average path length (when only following
   * links "forward)in a directed-graph would be undefined.
   * <p/>
   * ~Forrest (5/11/2007)
   */
  public double averagePathLength(AgentSet nodeSet, AgentSet linkBreed) {
    HashSet<Turtle> seen = new HashSet<Turtle>();
    LinkedList<Turtle> queue = new LinkedList<Turtle>();
    long totalSum = 0;

    for (AgentSet.Iterator it2 = nodeSet.iterator(); it2.hasNext();) {
      Turtle agt = (Turtle) it2.next();
      int nodeSetVisitedCount = 0;
      seen.clear();
      seen.add(agt);
      queue.addLast(agt);
      // we use null to mark radius-layer boundaries
      queue.addLast(null);

      int layer = 0;
      while (true) {
        Turtle curNode = queue.removeFirst();
        if (curNode == null) {
          if (queue.isEmpty()) {
            break;
          }
          layer++;
          queue.addLast(null);
          continue;
        }
        if (nodeSet.contains(curNode)) {
          totalSum += layer;
          nodeSetVisitedCount++;
        }

        AgentSet neighborSet = linkManager.findLinkedWith(curNode, linkBreed);
        for (AgentSet.Iterator it = neighborSet.iterator(); it.hasNext();) {
          Turtle toAdd = (Turtle) it.next();
          if (!seen.contains(toAdd)) {
            seen.add(toAdd);
            queue.add(toAdd);
          }
        }
      }
      if (nodeSetVisitedCount != nodeSet.count()) {
        return -1.0;
      }
    }
    int nodeCount = nodeSet.count();
    if (nodeCount == 1) {
      return 0;
    }
    return (double) totalSum / (nodeCount * (nodeCount - 1));
  }

}

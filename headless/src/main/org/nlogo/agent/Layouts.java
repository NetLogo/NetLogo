// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.LogoList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// All methods in this class assume their AgentSet arguments
// have already been checked that they are turtle sets, not
// patch sets. - ST 3/10/06

public final strictfp class Layouts {

  // this class is not instantiable
  private Layouts() {
    throw new IllegalStateException();
  }

  /// circle

  public static void circle(World world, LogoList nodes, double radius)
      throws AgentException {
    int i = 0;
    int n = nodes.size();
    int midx = world.minPxcor() + (int) StrictMath.floor(world.worldWidth() / 2);
    int midy = world.minPycor() + (int) StrictMath.floor(world.worldHeight() / 2);
    for (Iterator<Object> it = nodes.iterator(); it.hasNext(); i++) {
      Object obj = it.next();
      if (obj instanceof Turtle) {
        Turtle t = (Turtle) obj;
        double heading = (i * 360) / n;
        // precheck so turtles don't end up in weird places.
        world.protractor().getPatchAtHeadingAndDistance(midx, midy, heading, radius);
        t.xandycor(midx, midy);
        t.heading(heading);
        t.jump(radius);
      }
    }
  }

  public static void circle(World world, AgentSet nodes, double radius,
                            org.nlogo.util.MersenneTwisterFast random)
      throws AgentException {
    int i = 0;
    int n = nodes.count();
    int midx = world.minPxcor() + (int) StrictMath.floor(world.worldWidth() / 2);
    int midy = world.minPycor() + (int) StrictMath.floor(world.worldHeight() / 2);
    for (AgentIterator it = nodes.shufflerator(random); it.hasNext(); i++) {
      Turtle t = (Turtle) it.next();
      double heading = (i * 360) / n;
      // precheck so turtles don't end up in weird places.
      world.protractor().getPatchAtHeadingAndDistance(midx, midy, heading, radius);
      t.xandycor(midx, midy);
      t.heading(heading);
      t.jump(radius);
    }
  }

  /// spring
  public static void spring(World world, AgentSet nodeset, AgentSet linkset,
                            double spr, double len, double rep,
                            org.nlogo.util.MersenneTwisterFast random) {
    spring2D(world, nodeset, linkset, spr, len, rep, random);
  }

  public static void spring2D(World world, AgentSet nodeset, AgentSet linkset,
                              double spr, double len, double rep,
                              org.nlogo.util.MersenneTwisterFast random) {
    int nodeCount = nodeset.count();
    if (nodeCount == 0) {
      return;
    }
    double[] ax = new double[nodeCount];
    double[] ay = new double[nodeCount];
    int i = 0;
    HashMap<Turtle, Integer> tMap =
        new HashMap<Turtle, Integer>();
    int[] degCount = new int[nodeCount];

    Turtle[] agt = new Turtle[nodeCount];
    for (AgentIterator it = nodeset.shufflerator(random); it.hasNext(); i++) {
      Turtle t = (Turtle) it.next();
      agt[i] = t;
      tMap.put(t, Integer.valueOf(i));
      ax[i] = 0.0;
      ay[i] = 0.0;
    }

    for (AgentIterator it = linkset.iterator(); it.hasNext(); i++) {
      Link link = (Link) it.next();
      Turtle t1 = link.end1();
      Turtle t2 = link.end2();
      if (tMap.containsKey(t1)) {
        int t1Index = tMap.get(t1).intValue();
        degCount[t1Index]++;
      }
      if (tMap.containsKey(t2)) {
        int t2Index = tMap.get(t2).intValue();
        degCount[t2Index]++;
      }
    }

    for (AgentIterator it = linkset.iterator(); it.hasNext(); i++) {
      Link link = (Link) it.next();
      double dx = 0;
      double dy = 0;
      Turtle t1 = link.end1();
      Turtle t2 = link.end2();
      int t1Index = -1;
      int degCount1 = 0;
      if (tMap.containsKey(t1)) {
        t1Index = tMap.get(t1).intValue();
        degCount1 = degCount[t1Index];
      }
      int t2Index = -1;
      int degCount2 = 0;
      if (tMap.containsKey(t2)) {
        t2Index = tMap.get(t2).intValue();
        degCount2 = degCount[t2Index];
      }
      double dist = world.protractor().distance(t1, t2, false);
      // links that are connecting high degree nodes should not
      // be as springy, to help prevent "jittering" behavior
      double div = (degCount1 + degCount2) / 2.0;
      div = StrictMath.max(div, 1.0);

      if (dist == 0) {
        dx += (spr * len) / div; // arbitrary x-dir push-off

      } else {
        double f = spr * (dist - len) / div;
        dx = dx + (f * (t2.xcor() - t1.xcor()) / dist);
        dy = dy + (f * (t2.ycor() - t1.ycor()) / dist);
      }
      if (t1Index != -1) {
        ax[t1Index] += dx;
        ay[t1Index] += dy;
      }
      if (t2Index != -1) {
        ax[t2Index] -= dx;
        ay[t2Index] -= dy;
      }
    }

    for (i = 0; i < nodeCount; i++) {
      Turtle t1 = agt[i];
      for (int j = i + 1; j < nodeCount; j++) {
        Turtle t2 = agt[j];
        double dx = 0.0;
        double dy = 0.0;
        double div = (degCount[i] + degCount[j]) / 2.0;
        div = StrictMath.max(div, 1.0);

        if (t2.xcor() == t1.xcor() && t2.ycor() == t1.ycor()) {
          double ang = 360 * random.nextDouble();
          dx = -(rep / div * StrictMath.sin(StrictMath.toRadians(ang)));
          dy = -(rep / div * StrictMath.cos(StrictMath.toRadians(ang)));
        } else {
          double dist = world.protractor().distance(t1, t2, false);
          double f = rep / (dist * dist) / div;
          dx = -(f * (t2.xcor() - t1.xcor()) / dist);
          dy = -(f * (t2.ycor() - t1.ycor()) / dist);
        }
        ax[i] += dx;
        ay[i] += dy;
        ax[j] -= dx;
        ay[j] -= dy;
      }
    }

    // we need to bump some node a small amount, in case all nodes
    // are stuck on a single line
    if (nodeCount > 1) {
      double perturbAmt = (world.worldWidth() + world.worldHeight()) / (1.0E10);
      ax[0] += random.nextDouble() * perturbAmt - perturbAmt / 2.0;
      ay[0] += random.nextDouble() * perturbAmt - perturbAmt / 2.0;
    }

    // try to choose something that's reasonable perceptually --
    // for temporal aliasing, don't want to jump too far on any given timestep.
    double limit = (world.worldWidth() + world.worldHeight()) / 50.0;

    for (i = 0; i < nodeCount; i++) {
      Turtle t = agt[i];
      double fx = ax[i];
      double fy = ay[i];

      if (fx > limit) {
        fx = limit;
      } else if (fx < -limit) {
        fx = -limit;
      }
      if (fy > limit) {
        fy = limit;
      } else if (fy < -limit) {
        fy = -limit;
      }

      double newx = t.xcor() + fx;
      double newy = t.ycor() + fy;

      if (newx > world.maxPxcor()) {
        newx = world.maxPxcor();
      } else if (newx < world.minPxcor()) {
        newx = world.minPxcor();
      }

      if (newy > world.maxPycor()) {
        newy = world.maxPycor();
      } else if (newy < world.minPycor()) {
        newy = world.minPycor();
      }
      try {
        t.xandycor(newx, newy);
      } catch (AgentException ex) {
        // should never happen, because we already checked bounds
        throw new IllegalStateException(ex);
      }
    }
  }

  /// Tutte

  public static void tutte(World world, AgentSet nodeset, AgentSet linkset,
                           double radius, org.nlogo.util.MersenneTwisterFast random)
      throws AgentException {
    java.util.ArrayList<Turtle> anchors = new java.util.ArrayList<Turtle>();
    for (AgentIterator iter = linkset.iterator();
         iter.hasNext();) {
      Link link = (Link) iter.next();
      if (!nodeset.contains(link.end1()) && !anchors.contains(link.end1())) {
        anchors.add(link.end1());
      }
      if (!nodeset.contains(link.end2()) && !anchors.contains(link.end2())) {
        anchors.add(link.end2());
      }
    }
    circle(world, LogoList.fromJava(anchors), radius);
    int n = nodeset.count();

    Turtle[] agt = new Turtle[n];
    double[] ax = new double[n];
    double[] ay = new double[n];
    int ctr2 = 0;

    for (AgentIterator iter = nodeset.shufflerator(random); iter.hasNext(); ctr2++) {
      agt[ctr2] = (Turtle) iter.next();
    }

    for (int i = 0; i < n; i++) {
      Turtle t = agt[i];
      double fx = 0, fy = 0;
      int degree = 0;
      for (AgentIterator it = world.links().shufflerator(random); it.hasNext();) {
        Link link = (Link) it.next();
        if ((link.end1() == t || link.end2() == t)
            && linkset.contains(link)) {
          Turtle other = link.end1();
          if (t == link.end1()) {
            other = link.end2();
          }
          fx = fx + other.xcor();
          fy = fy + other.ycor();
          degree++;
        }
      }
      fx = fx / degree;
      fy = fy / degree;
      fx = fx - t.xcor();
      fy = fy - t.ycor();

      double limit = 100;
      if (fx > limit) {
        fx = limit;
      } else {
        if (fx < -limit) {
          fx = -limit;
        }
      }
      if (fy > limit) {
        fy = limit;
      } else {
        if (fy < -limit) {
          fy = -limit;
        }
      }
      fx += t.xcor();
      fy += t.ycor();
      if (fx > world.maxPxcor()) {
        fx = world.maxPxcor();
      } else {
        if (fx < world.minPxcor()) {
          fx = world.minPxcor();
        }
      }
      if (fy > world.maxPycor()) {
        fy = world.maxPycor();
      } else {
        if (fy < world.minPycor()) {
          fy = world.minPycor();
        }
      }
      ax[i] = fx;
      ay[i] = fy;
    }
    reposition(agt, ax, ay);
  }

  /// helpers

  private static void reposition(Turtle[] agents, double[] x, double[] y) {
    try {
      for (int i = 0; i < agents.length; i++) {
        agents[i].xandycor(x[i], y[i]);
      }
    } catch (AgentException ex) {
      // should not be possible! the methods that call us are supposed
      // to have done their own bounds checking already
      throw new IllegalStateException(ex);
    }
  }

  public static void radial(World world, AgentSet nodeset, AgentSet linkset, Turtle root)
      throws AgentException {
    double rootX = (world.minPxcor() + world.maxPxcor()) / 2.0;
    double rootY = (world.minPycor() + world.maxPycor()) / 2.0;

    LinkManager linkManager = world.linkManager();

    Map<Turtle, TreeNode> nodeTable =
        new HashMap<Turtle, TreeNode>(nodeset.count());
    ArrayList<TreeNode> queue =
        new ArrayList<TreeNode>(nodeset.count());
    TreeNode rootNode = new TreeNode(root, null);
    queue.add(rootNode);
    nodeTable.put(rootNode.val, rootNode);

    // used to find the maximum depth
    TreeNode lastNode = rootNode;

    while (!queue.isEmpty()) {
      TreeNode node = queue.remove(0);
      lastNode = node;
      scala.collection.Iterator<Turtle> iter = linkManager.findLinkedWith(node.val, linkset);
      while (iter.hasNext()) {
        Turtle t = iter.next();
        if (nodeset.contains(t) && !nodeTable.containsKey(t)) {
          TreeNode child = new TreeNode(t, node);
          node.children.add(child);
          nodeTable.put(t, child);
          queue.add(child);
        }
      }
    }

    rootNode.layoutRadial(0.0, 360.0);

    double maxDepth = lastNode.getDepth() + 0.2;
    if (maxDepth < 1.0) {
      maxDepth = 1.0;
    }
    double xDistToEdge = StrictMath.min(world.maxPxcor() - rootX, rootX - world.minPxcor());
    double yDistToEdge = StrictMath.min(world.maxPycor() - rootY, rootY - world.minPycor());
    double distToEdge = StrictMath.min(xDistToEdge, yDistToEdge);
    double layerGap = distToEdge / maxDepth;

    Iterator<TreeNode> it = nodeTable.values().iterator();
    while (it.hasNext()) {
      TreeNode node = it.next();
      Turtle t = node.val;
      t.heading(node.angle);
      t.xandycor(rootX, rootY);
      t.jump(node.getDepth() * layerGap);
    }
  }

  private static strictfp class TreeNode {
    public Turtle val;
    public TreeNode parent;
    public List<TreeNode> children = new ArrayList<TreeNode>(10);
    public double angle = 0.0;

    public TreeNode(Turtle val, TreeNode parent) {
      this.val = val;
      this.parent = parent;
    }

    public int getDepth() {
      int i = 0;
      TreeNode myParent = parent;
      while (myParent != null) {
        myParent = myParent.parent;
        i++;
      }
      return i;
    }

    // A heuristic used to decide how much space each
    // node should be alotted, based on its children.
    public double getWeight() {
      double myWeight = children.size() + 1;
      double maxChildWeight = 0.0;
      for (TreeNode child : children) {
        double cweight = child.getWeight();
        if (cweight > maxChildWeight) {
          maxChildWeight = cweight;
        }
      }
      maxChildWeight = maxChildWeight * 0.8;

      return StrictMath.max(myWeight, maxChildWeight);
    }

    public void layoutRadial(double arcStart, double arcEnd) {
      angle = (arcStart + arcEnd) / 2.0;

      double weightSum = 0.0;

      for (TreeNode child : children) {
        weightSum += child.getWeight();
      }

      double childStart = arcStart;
      for (TreeNode child : children) {
        double childEnd = childStart + (arcEnd - arcStart) * child.getWeight() / weightSum;
        child.layoutRadial(childStart, childEnd);
        childStart = childEnd;
      }
    }
  }
}

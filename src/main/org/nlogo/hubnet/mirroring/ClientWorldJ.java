// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.nlogo.hubnet.mirroring.ClientWorldS.TurtleKey;
import static org.nlogo.hubnet.mirroring.ClientWorldS.TurtleKeyComparator;
import static org.nlogo.hubnet.mirroring.ClientWorldS.LinkKey;
import static org.nlogo.hubnet.mirroring.ClientWorldS.LinkKeyComparator;

public abstract strictfp class ClientWorldJ
    implements org.nlogo.api.World {

  /**
   * whether or not to print messages for protocol errors.
   * false is useful when unit testing.
   */
  public final boolean printErrors;

  // these are package protected for unit testing...

  // since we want to keep the turtles sorted, but we also
  // want to be able to look them up just by who number, we
  // keep two parallel maps, one sorted, one not -- AZS 11/16/04

  java.util.SortedMap<TurtleKey, TurtleData> sortedTurtles;
  Map<Long, TurtleKey> turtleKeys;

  Map<Long, TurtleData> uninitializedTurtles = new HashMap<Long, TurtleData>();

  java.util.SortedMap<LinkKey, LinkData> sortedLinks;
  Map<Long, LinkKey> linkKeys;

  Map<Long, LinkData> uninitializedLinks = new HashMap<Long, LinkData>();

  public abstract int[] patchColors();

  /**
   * Returns descriptions of the patches in this world.
   */
  public PatchData[] getPatches() {
    return patchData();
  }

  abstract PatchData[] patchData();

  public ClientWorldJ(boolean printErrors) {
    this.printErrors = printErrors;
    sortedTurtles = new TreeMap<TurtleKey, TurtleData>(new TurtleKeyComparator());
    turtleKeys = new HashMap<Long, TurtleKey>();
    sortedLinks = new TreeMap<LinkKey, LinkData>(new LinkKeyComparator());
    linkKeys = new HashMap<Long, LinkKey>();
  }

  abstract protected void createPatches(int numPatches);

  protected org.nlogo.api.TrailDrawerInterface trailDrawer;

  public void setTrailDrawer(org.nlogo.api.TrailDrawerInterface trailDrawer) {
    this.trailDrawer = trailDrawer;
  }

  /**
   * Returns descriptions of the turtles in this world.
   * In the correct order for drawing.
   */
  public Iterable<TurtleData> getTurtles() {
    return sortedTurtles.values();
  }

  /**
   * Returns descriptions of the turtles in this world.
   * In the correct order for drawing.
   */
  public Iterable<LinkData> getLinks() {
    return sortedLinks.values();
  }

  public abstract int minPxcor();
  public abstract int maxPxcor();
  public abstract int minPycor();
  public abstract int maxPycor();

  public int worldWidth() {
    return maxPxcor() - minPxcor() + 1;
  }

  public int worldHeight() {
    return maxPycor() - minPycor() + 1;
  }

  private double patchSize = 13;

  public void patchSize(double patchSize) {
    this.patchSize = patchSize;
  }

  public double patchSize() {
    return perspectiveMode() == PerspectiveMode.SERVER ? patchSize : ((StrictMath.max(viewWidth, viewHeight)) / ((radius() * 2) + 1));
  }

  public double zoom() {
    return patchSize() / patchSize;
  }

  // for now we're not keeping track of this on the client,
  // but we could ev 4/24/08
  public boolean patchesAllBlack() {
    return false;
  }

  // used by TestClientWorld only
  TurtleData getTurtleDataByWho(long who) {
    TurtleKey key = turtleKeys.get(Long.valueOf(who));
    if (key != null) {
      return sortedTurtles.get(key);
    }
    return null;
  }

  LinkData getLink(Long id) {
    LinkKey key = linkKeys.get(id);
    if (key == null) {
      LinkData link = uninitializedLinks.get(id);
      if (link == null) {
        link = new LinkData(id);
        uninitializedLinks.put(id, link);
      }
      return link;
    } else {
      return sortedLinks.get(key);
    }
  }

  TurtleData getTurtle(Long id) {
    TurtleKey key = turtleKeys.get(id);
    if (key == null) {
      TurtleData turtle = uninitializedTurtles.get(id);
      if (turtle == null) {
        turtle = new TurtleData(id);
        uninitializedTurtles.put(id, turtle);
      }
      return turtle;
    } else {
      return sortedTurtles.get(key);
    }
  }

  public enum PerspectiveMode {
    SERVER, CLIENT;
  }

  private int viewWidth;
  private int viewHeight;

  public void viewWidth(int viewWidth) {
    this.viewWidth = viewWidth;
  }

  public void viewHeight(int viewHeight) {
    this.viewHeight = viewHeight;
  }

  public double viewWidth() {
    return (viewWidth / patchSize());
  }

  public double viewHeight() {
    return (viewHeight / patchSize());
  }

  AgentData getAgent(Agent agent) {
    if (agent.tyype() == AgentTypeJ.TURTLE()) {
      return getTurtle(Long.valueOf(agent.id()));
    }
    if (agent.tyype() == AgentTypeJ.PATCH()) {
      return getPatches()[(int) agent.id()];
    }
    if (agent.tyype() == AgentTypeJ.LINK()) {
      return getLink(Long.valueOf(agent.id()));
    }
    return null;
  }

  public abstract double radius();
  public abstract PerspectiveMode perspectiveMode();
  public abstract void updateServerPerspective(AgentPerspective p);

}
